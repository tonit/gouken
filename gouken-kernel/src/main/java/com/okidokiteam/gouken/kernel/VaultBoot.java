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
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
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
    private volatile Registry m_registry;
    private TDaemon m_daemon;

    private File m_liveFolder;

    public VaultBoot( TDaemon d, Map<String, String> map )
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
        m_daemon = d;
        m_folder = base.getAbsoluteFile();
    }

    public void init()
        throws Exception
    {

        if( m_daemon.isDaemonized() )
        {
            // perform initialization as a daemon
            // this involves in closing file descriptors, recording PIDs, etc.
            LOG.info( "Starting vault: " + m_folder.getAbsolutePath() );
            if( m_folder.exists() )
            {
                if( !getWorkDir().exists() )
                {
                    // fine !
                }
                else
                {
                    throw new RuntimeException( "Vault is locked!" );
                }
            }
            else
            {
                throw new RuntimeException( "Workdir " + m_folder.getAbsolutePath() + " does not exist." );

            }
            m_daemon.init();

        }
        else
        {
            // if you are already daemonized, no point in daemonizing yourself again,
            // so do this only when you aren't daemonizing.
            m_daemon.daemonize();
            // exit parent (real stuff is forked)
            System.exit( 0 );
        }


    }

    private File getWorkDir()
    {
        return new File( m_folder, WORK );
    }

    public synchronized void stop()
        throws RemoteException
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
            if( m_registry != null )
            {
                m_registry.unbind( Vault.class.getName() );
                UnicastRemoteObject.unexportObject( this, true );
                UnicastRemoteObject.unexportObject( m_registry, true );
                m_registry = null;
            }
            FileUtils.delete( getWorkDir() );
            System.gc();

            LOG.info( "Shutdown complete." );


        } catch( BundleException e )
        {
            LOG.error( "Problem stopping framework.", e );
        } catch( NotBoundException e )
        {
            //LOG.error( "Problem stopping framework.", e );
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
            final Map<String, String> p = new HashMap<String, String>();
            File worker = new File( getWorkDir(), "framework" );
            FileUtils.delete( worker );
            p.put( "org.osgi.framework.storage", worker.getAbsolutePath() );
            //p.put( "org.osgi.framework.system.packages.extra", "org.ops4j.pax.exam.raw.extender;version=" + Info.getPaxExamVersion() );

            // TODO fix ContextClassLoaderUtils.doWithClassLoader() and replace logic with it.
            parent = Thread.currentThread().getContextClassLoader();
            //    Thread.currentThread().setContextClassLoader( null );

            FrameworkFactory factory = (FrameworkFactory) DiscoverSingleton.find( FrameworkFactory.class );

            m_framework = factory.newFramework( p );

            m_framework.init();
            bind();
            LOG.info( "Phase 1 done: Initialized OSGi container." );
            BundleContext context = m_framework.getBundleContext();

            InputStream ins = getClass().getResourceAsStream( "/META-INF/gouken/provisioning.properties" );
            if( ins != null )
            {
                install( ins, context );
            }

            m_framework.start();
            for( Bundle b : m_framework.getBundleContext().getBundles() )
            {
                b.start();
                LOG.debug( "Started: " + b.getSymbolicName() );
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
                } catch( RemoteException e )
                {
                    e.printStackTrace();
                }
            }
        }
        );
    }

    private void install( InputStream ins, BundleContext context )
        throws IOException, BundleException
    {
        LOG.info( "Found initial provisioning descriptor.." );

        Properties desc = new Properties();
        desc.load( ins );
        String cp = (String) desc.getProperty( "bundles" );
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
        LOG.info( "Trying to find a live location for " + path );
        Manifest man = getManifest( getClass().getResourceAsStream( path ) );
        String bundleSymbolicName = getBundleSymbolicName( man );
        URI location = findMatching( bundleSymbolicName );
        if( location != null )
        {
            LOG.info( "Found live location: " + location.toASCIIString() + " for bundle " + bundleSymbolicName );
            return location;
        }
        else
        {
            LOG.info( "No live location found for " + bundleSymbolicName );
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
                        LOG.info( "Considering " + f.getAbsolutePath() + " as location for " + bundleSymbolicName );
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

    private void bind()
    {
        try
        {
            // try to find port from property
            int port = getPort();
            LOG.debug( "Starting up RMI registry on port [" + port + "]" );
            m_registry = LocateRegistry.createRegistry( port );
            LOG.debug( "Binding " + m_framework.getClass() + " to RMI registry" );
            m_registry.bind(
                Vault.class.getName(),
                UnicastRemoteObject.exportObject(
                    (Vault) this,
                    port
                )
            );
            LOG.info( "RMI registry started on port [" + port + "]" );
        }
        catch( Exception e )
        {
            throw new RuntimeException( "Cannot setup RMI registry", e );
        }
    }

    private int getPort()
    {
        return 1412;
    }


}
