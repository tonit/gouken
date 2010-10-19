/*
 * Copyright (C) 2010 Okidokiteam
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.okidokiteam.gouken.macosx;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import com.okidokiteam.gouken.KernelException;
import com.okidokiteam.gouken.KernelWorkflowException;
import com.okidokiteam.gouken.Vault;
import com.okidokiteam.gouken.VaultHandle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Special deamon wrapper for a vault
 */
public class MacOSBoot implements RemoteVault
{

    private static Log LOG = LogFactory.getLog( MacOSBoot.class );

    private static final String WORK = ".gouken";

    private final File m_folder;
    // accessed by shutdownhook and remote access
    private volatile Registry m_registry;
    private TDaemon m_daemon;

    private Vault m_vault;

    public MacOSBoot( TDaemon d, Vault vault )
    {
        m_daemon = d;
        m_vault = vault;
        m_folder = new File( "" ); // TODO foo
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
            // after this, the VM will restart.

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

    public synchronized void stop( VaultHandle handle )
        throws KernelException
    {
        try
        {
            LOG.info( "Stop hook triggered." );

            if( m_registry != null )
            {
                m_registry.unbind( Vault.class.getName() );
                UnicastRemoteObject.unexportObject( this, true );
                UnicastRemoteObject.unexportObject( m_registry, true );
                m_registry = null;
            }
            System.gc();

            LOG.info( "Shutdown complete." );


        } catch( Exception e )
        {
            LOG.error( "Problem stopping framework.", e );
        }
    }

    public synchronized VaultHandle start()
        throws KernelException, KernelWorkflowException
    {
        // make a RMI Handle:

        VaultHandle handle = m_vault.start();
        bind( handle );
        return handle;
    }

    private void installShutdownHook( final VaultHandle handle )
    {

        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    m_vault.stop( handle );
                } catch( Exception e )
                {
                    e.printStackTrace();
                }
            }
        }
        );
    }

    private void bind( VaultHandle handle )
    {
        try
        {
            // try to find port from property
            int port = getPort();
            LOG.debug( "Starting up RMI registry on port [" + port + "]" );
            m_registry = LocateRegistry.createRegistry( port );
            LOG.debug( "Binding " + m_vault.getClass() + " to RMI registry" );
            m_registry.bind(
                Vault.class.getName(),
                UnicastRemoteObject.exportObject(
                    this,
                    port
                )
            );
            LOG.info( "RMI registry started on port [" + port + "]" );
        } catch( Exception e )
        {
            throw new RuntimeException( "Cannot setup RMI registry", e );
        }
    }

    private int getPort()
    {
        return 1412;
    }


}


