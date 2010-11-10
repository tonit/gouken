/*
 * Copyright 2009 Toni Menzel.
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import com.okidokiteam.gouken.KernelException;
import com.okidokiteam.gouken.KernelWorkflowException;
import com.okidokiteam.gouken.Vault;
import com.okidokiteam.gouken.VaultConfiguration;
import org.apache.commons.discovery.tools.DiscoverSingleton;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.repository.ArtifactIdentifier;
import org.ops4j.pax.repository.RepositoryException;
import org.ops4j.pax.repository.RepositoryResolver;

import static org.ops4j.pax.repository.resolver.RepositoryFactory.*;

/**
 * This Vault actually knows about OSGi, it actually boots a fw, provisions it and manages its lifecycle.
 * Beyond this, we should not have the notion of osgi other than DeploymentPackages. (also with another name as we probably just want a subset of that spec).
 *
 * This Vault uses:
 * - Apache Felix as underlying OSGi implementation
 * - DeploymentAdmin from Felix as management agent implementation
 * - Tinybundles to transfer update() into units recognizable by the DeploymentAdmin
 *
 * This makes the update mechanism dynamic (OSGi), atomic (DeploymentAdmin) and configuration agnostic (Tinybundles).
 *
 * Because we deal with DP like Apache ACE does, we may or may not use the ACE MA here.
 * Difference is that we just have ONE management agent with one single server.(plus one client).. all in this one vault.
 * So we may make things simpler..
 *
 * @author Toni Menzel
 * @since Mar 4, 2010
 */
public class CoreVault implements Vault
{

    private static Logger LOG = LoggerFactory.getLogger( CoreVault.class );
    public static final String META_INF_GOUKEN_KERNEL_PROPERTIES = "/META-INF/gouken/kernel.properties";
    public static final String BUNDLE_DEPLOYMENTADMIN = "org.apache.felix:org.apache.felix.dependencymanager:3.0.0-SNAPSHOT";
    public static final String BUNDLE_DM = "org.apache.felix:org.apache.felix.deploymentadmin:0.9.0-SNAPSHOT";

    private final VaultConfiguration m_configuration;

    // accessed by shutdownhook and remote access
    private volatile Framework m_framework;
    private File m_workDir;
    private RepositoryResolver m_resolver;

    public CoreVault( VaultConfiguration initialConfiguration,
                      File workDir,
                      RepositoryResolver resolver,
                      String... extraPackages )
    {
        assert initialConfiguration != null : "VaultConfiguration must not be null.";
        assert workDir != null : "workDir must not be null.";
        assert resolver != null : "resolver must not be null.";

        m_configuration = initialConfiguration;
        m_workDir = workDir;
        m_resolver = resolver;
    }

    public synchronized VaultConfiguration start()
        throws KernelWorkflowException, KernelException
    {
        if( isRunning() )
        {
            throw new KernelWorkflowException( "Vault is already running." );
        }

        ClassLoader parent = null;
        try
        {
            final Map<String, String> p = getFrameworkConfig();
            parent = Thread.currentThread().getContextClassLoader();

            Thread.currentThread().setContextClassLoader( null );
            loadAndStartFramework( p );
            Thread.currentThread().setContextClassLoader( parent );
        } catch( Exception e )
        {
            // kind of a clean the mess up..
            tryShutdown();
            throw new KernelException( "Problem starting the Vault", e );

        } finally
        {
            if( parent != null )
            {
                Thread.currentThread().setContextClassLoader( parent );

            }
        }
        return new VaultConfiguration() { };
    }

    private void install( BundleContext context, String... artifacts )
        throws RepositoryException, IOException, BundleException
    {
        for( String artifact : artifacts )
        {
            ArtifactIdentifier a = parseFromURL( artifact );
            context.installBundle( a.getName(), m_resolver.find( a ).getContent().get() );
        }
        for( Bundle b : context.getBundles() )
        {
            try
            {
                b.start();
                LOG.info( "Installed: " + b.getSymbolicName() + " --> " + b.getState() );
            } catch( Exception e )
            {
                LOG.warn( "Not started: " + b.getSymbolicName() + " - " + e.getMessage() );

            }
        }
    }

    private Map<String, String> getFrameworkConfig()
        throws IOException
    {
        InputStream ins = getClass().getResourceAsStream( META_INF_GOUKEN_KERNEL_PROPERTIES );
        Properties descriptor = new Properties();
        if( ins != null )
        {
            descriptor.load( ins );
        }

        final Map<String, String> p = new HashMap<String, String>();
        File worker = new File( m_workDir, "framework" );

        p.put( "org.osgi.framework.storage", worker.getAbsolutePath() );
        // TODO: make exposing compendium packages automatic.
        p.put( "org.osgi.framework.system.packages.extra", "org.osgi.service.log;version=1.3.0,org.osgi.service.event;version=1.1.0,org.osgi.service.metatype;version=1.1.0,org.osgi.service.deploymentadmin;version=1.0,org.osgi.service.cm;version=1.3" );
        p.put( "felix.log.level", "1" );
        for( Object key : descriptor.keySet() )
        {
            p.put( (String) key, descriptor.getProperty( (String) key ) );
        }
        return p;
    }

    public void update( VaultConfiguration configuration )
        throws KernelException
    {
        new CoreVaultUpdate( m_framework.getBundleContext(), m_resolver ).invoke( configuration );
    }

    public synchronized void stop( )
        throws KernelException
    {
        try
        {
            LOG.info( "Stop hook triggered." );
            if( m_framework != null )
            {
                BundleContext ctx = m_framework.getBundleContext();
                Bundle systemBundle = ctx.getBundle( 0 );
                systemBundle.stop();
                m_framework = null;
            }
            System.gc();
            LOG.info( "Shutdown complete." );
        } catch( BundleException e )
        {
            LOG.error( "Problem stopping framework.", e );
        }

    }

    private void loadAndStartFramework( Map<String, String> p )
        throws BundleException, IOException, RepositoryException, KernelException
    {
        FrameworkFactory factory = (FrameworkFactory) DiscoverSingleton.find( FrameworkFactory.class );
        m_framework = factory.newFramework( p );
        m_framework.init();

        m_framework.start();

        // install MA
        install( m_framework.getBundleContext(), BUNDLE_DM, BUNDLE_DEPLOYMENTADMIN );

    }

    private void tryShutdown()
    {
        if( m_framework != null )
        {
            try
            {
                m_framework.stop();

            } catch( Exception e )
            {
                // dont care.
            }
        }
    }

    private boolean isRunning()
    {
        return ( m_framework != null );
    }
}
