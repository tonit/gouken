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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.okidokiteam.gouken.KernelException;
import com.okidokiteam.gouken.KernelWorkflowException;
import com.okidokiteam.gouken.Vault;
import com.okidokiteam.gouken.VaultConfiguration;
import com.okidokiteam.gouken.VaultHandle;
import org.apache.commons.discovery.tools.DiscoverSingleton;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.ops4j.pax.repository.Artifact;
import org.ops4j.pax.repository.RepositoryException;

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
 * @author Toni Menzel
 * @since Mar 4, 2010
 */
public class CoreVault implements Vault
{

    private static Log LOG = LogFactory.getLog( CoreVault.class );
    private static final String META_INF_GOUKEN_KERNEL_PROPERTIES = "/META-INF/gouken/kernel.properties";

    private final VaultConfiguration m_configuration;

    // accessed by shutdownhook and remote access
    private volatile Framework m_framework;
    private File m_workDir;

    public CoreVault( VaultConfiguration initialConfiguration, File workDir )
    {
        m_configuration = initialConfiguration;
        m_workDir = workDir;
    }

    public synchronized VaultHandle start()
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

            // Start sequence:
            loadAndStartFramework( p );
            BundleContext context = m_framework.getBundleContext();

            startBundles( context.getBundles() );

            update( m_configuration );
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
        return new VaultHandle()
        {
        };
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

        for( Object key : descriptor.keySet() )
        {
            p.put( (String) key, descriptor.getProperty( (String) key ) );
        }
        return p;
    }

    public void update( VaultConfiguration configuration )
        throws KernelException
    {
        try
        {
            // create a deployment package and install

            //installBundles( m_framework.getBundleContext(), configuration.getArtifacts() );
        } catch( Exception e )
        {
            throw new KernelException( "Problem while update() using configuration: " + configuration, e );
        }


    }

    public synchronized void stop( VaultHandle handle )
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


    }

    private Bundle[] installBundles( BundleContext ctx, Artifact... artifacts )
        throws IOException, BundleException, RepositoryException
    {
        List<Bundle> bundles = new ArrayList<Bundle>( artifacts.length );
        for( Artifact artifact : artifacts )
        {
            LOG.info( "Installing " + artifact.getName() );
            bundles.add( ctx.installBundle( artifact.getName(), artifact.getContent().get() ) );
        }
        return bundles.toArray( new Bundle[ bundles.size() ] );
    }

    private void startBundles( Bundle... bundles )
    {
        for( Bundle b : bundles )
        {
            try
            {
                b.start();
                LOG.debug( "Started: " + b.getSymbolicName() );
            } catch( Exception e )
            {
                LOG.warn( "Not started: " + b.getSymbolicName() + " - " + e.getMessage() );

            }
        }
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

    public boolean isRunning()
    {
        return ( m_framework != null );
    }
}
