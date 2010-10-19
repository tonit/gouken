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
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import com.okidokiteam.gouken.KernelException;
import com.okidokiteam.gouken.KernelWorkflowException;
import com.okidokiteam.gouken.Vault;
import com.okidokiteam.gouken.VaultHandle;
import org.apache.commons.discovery.tools.DiscoverSingleton;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * @author Toni Menzel
 * @since Mar 4, 2010
 */
public class VaultBoot implements Vault
{

    private static Log LOG = LogFactory.getLog( VaultBoot.class );
    private static final String META_INF_GOUKEN_KERNEL_PROPERTIES = "/META-INF/gouken/kernel.properties";

    private final VaultConfiguration m_configuration;

    // accessed by shutdownhook and remote access
    private volatile Framework m_framework;

    private File m_liveFolder;

    public VaultBoot( VaultConfiguration configuration )
    {
        m_configuration = configuration;
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

    public synchronized VaultHandle start()
        throws KernelWorkflowException
    {
        if( isRunning() )
        {
            throw new KernelWorkflowException( "" );
        }

        ClassLoader parent = null;
        try
        {
            // come from specific kernel implementation..
            InputStream ins = getClass().getResourceAsStream( META_INF_GOUKEN_KERNEL_PROPERTIES );
            Properties descriptor = new Properties();
            if( ins != null )
            {
                descriptor.load( ins );
            }

            final Map<String, String> p = new HashMap<String, String>();
            File worker = new File( m_configuration.getWorkDir(), "framework" );

            p.put( "org.osgi.framework.storage", worker.getAbsolutePath() );

            for( Object key : descriptor.keySet() )
            {
                p.put( (String) key, descriptor.getProperty( (String) key ) );
            }

            parent = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader( null );
            loadAndStartFramework( p );
            Thread.currentThread().setContextClassLoader( parent );
        } catch( Exception e )
        {
            e.printStackTrace();
            // kind of a clean the mess up..
            tryShutdown();
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

    private void loadAndStartFramework( Map<String, String> p )
        throws BundleException
    {
        FrameworkFactory factory = (FrameworkFactory) DiscoverSingleton.find( FrameworkFactory.class );
        m_framework = factory.newFramework( p );

        m_framework.init();

        LOG.info( "Phase 1 done: Initialized OSGi container." );
        BundleContext context = m_framework.getBundleContext();
        // TODO install management agent.
        m_framework.start();
        startBundles( context );
    }

    private void startBundles( BundleContext context )
    {
        for( Bundle b : context.getBundles() )
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
