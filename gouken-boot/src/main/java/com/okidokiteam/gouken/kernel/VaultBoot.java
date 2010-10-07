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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import com.okidokiteam.gouken.KernelException;
import com.okidokiteam.gouken.Vault;
import org.apache.commons.discovery.tools.DiscoverSingleton;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.ops4j.io.FileUtils;

/**
 * @author Toni Menzel
 * @since Mar 4, 2010
 */
public class VaultBoot implements Vault
{

    private static Log LOG = LogFactory.getLog( VaultBoot.class );

    private static final String WORK = ".gouken";

    private final File m_folder;

    // accessed by shutdownhook and remote access
    private volatile Framework m_framework;

    private File m_liveFolder;

    public VaultBoot( Map<String, String> map )
    {
        String t = map.get( "--target" );
        if( t == null )
        {
            t = ".";
        }
        File base = new File( t ).getAbsoluteFile();
        String live = map.get( "--live" );
        if( live != null )
        {
            m_liveFolder = new File( base, live );
            LOG.info( "Live folder: " + m_liveFolder.getAbsolutePath() );
        }

        m_folder = base.getAbsoluteFile();
    }

    private File getWorkDir()
    {
        return new File( m_folder, WORK );
    }

    public synchronized void stop()
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

            FileUtils.delete( getWorkDir() );
            System.gc();

            LOG.info( "Shutdown complete." );


        } catch( BundleException e )
        {
            LOG.error( "Problem stopping framework.", e );
        }

    }

    public String status()
        throws RemoteException
    {

        if( m_framework == null )
        {
            return "Framework is not running. (instance is null).";

        }
        else
        {
            return "Framework is running.";
        }
    }

    public synchronized void start()
    {
        // foo
        getWorkDir().mkdirs();
        installShutdownHook();
        ClassLoader parent = null;
        try
        {
            InputStream ins = getClass().getResourceAsStream( "/META-INF/gouken/provisioning.properties" );
            Properties descriptor = new Properties();
            if( ins != null )
            {
                descriptor.load( ins );
            }

            final Map<String, String> p = new HashMap<String, String>();
            File worker = new File( getWorkDir(), "framework" );
            FileUtils.delete( worker );
            p.put( "org.osgi.framework.storage", worker.getAbsolutePath() );

            for( Object key : descriptor.keySet() )
            {
                p.put( (String) key, descriptor.getProperty( (String) key ) );
            }

            parent = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader( null );

            FrameworkFactory factory = (FrameworkFactory) DiscoverSingleton.find( FrameworkFactory.class );

            m_framework = factory.newFramework( p );

            m_framework.init();

            LOG.info( "Phase 1 done: Initialized OSGi container." );
            BundleContext context = m_framework.getBundleContext();

            String bundles = descriptor.getProperty( "bundles" );
            install( bundles, context );

            m_framework.start();
            for( Bundle b : m_framework.getBundleContext().getBundles() )
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
            Thread.currentThread().setContextClassLoader( parent );
            LOG.info( "Phase 2 done: Installed management bundles." );

        } catch( Exception e )
        {
            e.printStackTrace();
            tryShutdown();
        } finally
        {
            if( parent != null )
            {
                Thread.currentThread().setContextClassLoader( parent );

            }
        }
    }

    private void installShutdownHook()
    {
        final VaultBoot vaultBoot = this;

        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    vaultBoot.stop();
                } catch( KernelException e )
                {
                    e.printStackTrace();
                }
            }
        }
        );
    }

    private void install( String cp, BundleContext context )
        throws IOException, BundleException
    {
        LOG.info( "Found initial provisioning descriptor.." );

        if( cp != null )
        {
            for( String s : cp.split( "," ) )
            {
                if( s != null && !s.equals( "." ) )
                {
                    LOG.info( "+ " + s );

                    // UpdateLocation: try to find corporating bundle locations
                    String embeddedPath = "/META-INF/gouken/" + s;

                    if( m_liveFolder != null )
                    {

                        URI path = findLocation( embeddedPath );
                        if( path != null )
                        {
                            Bundle b = context.installBundle( path.toASCIIString(), path.toURL().openStream() );
                        }
                        else
                        {
                            Bundle b = context.installBundle( s, getClass().getResourceAsStream( embeddedPath ) );

                        }
                    }
                    else
                    {
                        Bundle b = context.installBundle( s, getClass().getResourceAsStream( embeddedPath ) );
                    }
                }
            }
        }
    }

    private URI findLocation( String path )
        throws IOException
    {
        // read in order to get matcher information:
        Manifest man = getManifest( getClass().getResourceAsStream( path ) );
        String bundleSymbolicName = getBundleSymbolicName( man );
        URI location = findMatching( bundleSymbolicName );
        if( location != null )
        {
            //   LOG.info( "Found live location: " + location.toASCIIString() + " for bundle " + bundleSymbolicName );
            return location;
        }
        else
        {
            //     LOG.info( "No live location found for " + bundleSymbolicName );
        }
        return null;
    }

    private Manifest getManifest( InputStream jar )
    {
        Manifest man = null;

        JarInputStream jin = null;
        try
        {
            jin = new JarInputStream( jar );
            man = jin.getManifest();

        } catch( IOException e )
        {
            // don't care
        } finally
        {
            try
            {
                jin.close();
            } catch( IOException e )
            {

            }
        }
        return man;
    }

    private URI findMatching( String bundleSymbolicName )
        throws FileNotFoundException
    {
        // traverse folder in order to locate a matching bundle:
        return findMatching( m_liveFolder, bundleSymbolicName );

    }

    private URI findMatching( File folder, String bundleSymbolicName )
        throws FileNotFoundException
    {
        for( File f : folder.listFiles() )
        {
            if( f.isDirectory() && !f.isHidden() && !f.getName().startsWith( "." ) && !f.getName().equals( "classes" ) )
            {
                URI res = findMatching( f, bundleSymbolicName );
                if( res != null )
                {
                    return res;
                }
            }
            else
            {
                if( f.getName().endsWith( ".jar" ) )
                {
                    try
                    {
                        Manifest man = getManifest( new FileInputStream( f ) );
                        String sym = getBundleSymbolicName( man );
                        if( sym != null && sym.equals( bundleSymbolicName ) )
                        {
                            // found !
                            return f.toURI();
                        }
                    } catch( IOException ioE )
                    {

                    }
                }
            }
        }
        return null;
    }

    private String getBundleSymbolicName( Manifest man )
    {
        return man.getMainAttributes().getValue( "Bundle-SymbolicName" );
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

}
