/*
 * Copyright 2010 Toni Menzel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.okidokiteam.gouken.kernel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import com.okidokiteam.gouken.*;
import org.apache.commons.discovery.tools.DiscoverSingleton;
import org.ops4j.pax.repository.RepositoryException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Vault actually knows about OSGi, it actually boots a fw, provisions it and manages its lifecycle.
 * Beyond this, we should not have the notion of osgi other than DeploymentPackages. (also with another name as we probably just want a subset of that spec).
 * <p/>
 * This Vault uses an OSGi Core R 4.2+ compatible OSGi implementation
 * <p/> *
 *
 * @author Toni Menzel
 * @since Mar 4, 2010
 */
public class CoreVault<PUSHTYPE> implements Vault<PUSHTYPE> {

    private static final Logger LOG = LoggerFactory.getLogger( CoreVault.class );
    private static final String META_INF_GOUKEN_KERNEL_PROPERTIES = "/META-INF/gouken/kernel.properties";

    // accessed by shutdownhook and remote access
    private volatile Framework m_framework;
    private final VaultSettings m_settings;
    private Class<PUSHTYPE> m_pushServiceType;

    private GoukenResolver m_resolver = null;

    @Inject
    public CoreVault( GoukenResolver resolver, VaultSettings settings, Class<PUSHTYPE> pushService )
    {
        assert resolver != null : "resolver must not be null.";
        assert settings != null : "settings must not be null.";
        m_settings = settings;
        m_resolver = resolver;
        m_pushServiceType = pushService;
    }

    @Inject
    public CoreVault( GoukenResolver resolver, VaultSettings settings )
    {
        this( resolver, settings, null );
    }

    public PUSHTYPE start( ManagementAgent agent )
        throws KernelWorkflowException, KernelException
    {
        if( isRunning() ) {
            throw new KernelWorkflowException( "Vault is already running." );
        }

        ClassLoader parent = null;
        try {
            final Map<String, Object> p = getFrameworkConfig();
            parent = Thread.currentThread().getContextClassLoader();

            Thread.currentThread().setContextClassLoader( null );
            loadAndStartFramework( p );
            Thread.currentThread().setContextClassLoader( parent );

            installAgent( agent );

        } catch( Exception e ) {
            // kind of a clean the mess up..
            tryShutdown();
            throw new KernelException( "Problem starting the Vault", e );

        } finally {
            if( parent != null ) {
                Thread.currentThread().setContextClassLoader( parent );
            }
        }

        // create a dynamic proxy for T that looks T up on demand and invokes stuff on it.
        if( m_framework == null ) {
            return (PUSHTYPE) new Object();
        }
        return createProxyService();
    }

    private void installAgent( ManagementAgent agent )

        throws KernelException
    {

        ArtifactReference[] artifacts;
        try {
            artifacts = agent.getRuntimeParts();
        } catch( RepositoryException e ) {
            throw new KernelException( "Problem getting artifacts from agent: " + agent, e );
        }

        int i = 0;
        List<Bundle> bundles = new ArrayList<Bundle>();

        for( ArtifactReference artifact : artifacts ) {
            i++;
            try {
                bundles.add( m_framework.getBundleContext().installBundle( "MA" + i, m_resolver.find( artifact ).get() ) );
            } catch( BundleException e ) {
                throw new KernelException( "Problem installing management agent resources. Artifact: " + artifact, e );
            } catch( RepositoryException e ) {
                throw new KernelException( "Problem loading management agent resources. Artifact: " + artifact, e );
            }
        }

        for( Bundle b : bundles ) {
            try {
                b.start();
            } catch( BundleException e ) {
                throw new KernelException( "One of the Management Agent Bundles could not be started. Bundle ID: " + b.getBundleId(), e );
            }
        }
    }

    public synchronized void stop()
        throws KernelException
    {
        try {
            LOG.info( "Stop hook triggered." );
            if( m_framework != null ) {
                BundleContext ctx = m_framework.getBundleContext();
                Bundle systemBundle = ctx.getBundle( 0 );
                systemBundle.stop();
                m_framework = null;
            }
            System.gc();
            LOG.info( "Shutdown complete." );
        } catch( BundleException e ) {
            LOG.error( "Problem stopping framework.", e );
        }

    }

    private Map<String, Object> getFrameworkConfig()
        throws IOException
    {
        InputStream ins = getClass().getResourceAsStream( META_INF_GOUKEN_KERNEL_PROPERTIES );
        Properties descriptor = new Properties();
        if( ins != null ) {
            descriptor.load( ins );
        }

        final Map<String, Object> p = new HashMap<String, Object>();
        File worker = new File( m_settings.getWorkingFolder(), "framework" );

        p.put( "org.osgi.framework.storage", worker.getAbsolutePath() );
        // p.put( "felix.log.level", "1" );

        configureBridgingServiceDelegation( p );

        for( Object key : descriptor.keySet() ) {
            p.put( (String) key, descriptor.getProperty( (String) key ) );
        }
        return p;
    }

    private void configureBridgingServiceDelegation( Map<String, Object> p )
    {
        if( m_pushServiceType != null ) {
            String pushServicePackage = m_pushServiceType.getPackage().getName();

            p.put( "org.osgi.framework.system.packages.extra", pushServicePackage );
            p.put( "org.osgi.framework.bootdelegation", pushServicePackage );
            p.put( "org.osgi.framework.bundle.parent", "framework" );

        }
    }

    private void loadAndStartFramework( Map<String, Object> p )
        throws BundleException, IOException, RepositoryException, KernelException
    {
        FrameworkFactory factory = (FrameworkFactory) DiscoverSingleton.find( FrameworkFactory.class );
        m_framework = factory.newFramework( p );

        // p.put( FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, Arrays.asList( new Activator() ) );
        // m_framework = new Felix( p );
        m_framework.init();
        m_framework.start();
    }

    private void tryShutdown()
    {
        if( m_framework != null ) {
            try {
                m_framework.stop();

            } catch( Exception e ) {
                // dont care.
            }
        }
    }

    private boolean isRunning()
    {
        return ( m_framework != null );
    }

    /**
     * Dynamic proxy around a service that is being used from the outside.
     * Its usually being used to provide some kind of push functionality of the management agent.
     *
     * @return proxy for t. Will delegate to underlying osgi service registry upon each call.
     */
    @SuppressWarnings( "unchecked" )
    private PUSHTYPE createProxyService()
    {
        return (PUSHTYPE) Proxy.newProxyInstance(
            m_framework.getClass().getClassLoader(),
            new Class<?>[]{ m_pushServiceType },
            new InvocationHandler() {
                /**
                 * {@inheritDoc} Delegates the call to remote bundle context.
                 */
                public Object invoke( final Object proxy,
                                      final Method method,
                                      final Object[] params )
                    throws Throwable
                {
                    try {
                        return dynamicService(
                            method,
                            params
                        );
                    } catch( Exception e ) {
                        throw new RuntimeException( "Invocation exception", e );
                    }
                }

                private Object dynamicService( Method method, Object[] params )
                    throws InvalidSyntaxException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
                {
                    // find that service and invoke the method:
                    ServiceReference ref = null;
                    LOG.info( "Trying to locate service " + m_pushServiceType.getName() + " for method " + method.getName() );
                    while( ref == null ) {

                        BundleContext ctx = m_framework.getBundleContext();
                        ref = ctx.getServiceReference( m_pushServiceType.getName() );
                        if( ref != null ) {
                            Object o = ctx.getService( ref );
                            try {
                                return method.invoke( o, params );
                            } finally {
                                ctx.ungetService( ref );
                            }
                        }
                    }

                    return null;
                }

            }
        );
    }


}
