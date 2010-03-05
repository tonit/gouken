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
package org.ops4j.pax.vault.boot.activity;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.ops4j.pax.vault.boot.Vault;

/**
 * @author Toni Menzel
 * @since Mar 6, 2010
 */
public class RemoteCommand
{

    private static final long WAIT_FOREVER = 0;
    private long m_timeout = 2000;

    public Vault getRbc()
    {
        int port = getPort();
        long startedTrying = System.currentTimeMillis();
        //!! Absolutely necesary for RMI class loading to work
        // TODO maybe use ContextClassLoaderUtils.doWithClassLoader
        //    Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader() );
        Throwable reason = null;
        try
        {
            Registry registry;
            String host = getHost();
            if( host == null )
            {
                registry = LocateRegistry.getRegistry( port );
            }
            else
            {
                registry = LocateRegistry.getRegistry( host, port );
            }

            do
            {
                try
                {
                    return (Vault) registry.lookup( Vault.class.getName() );
                }
                catch( ConnectException e )
                {
                    reason = e;
                }
                catch( NotBoundException e )
                {
                    reason = e;
                }
            }
            while( ( getTimeout() == WAIT_FOREVER
                     || System.currentTimeMillis() < startedTrying + getTimeout() ) );
        }
        catch( RemoteException e )
        {
            reason = e;
        }

        throw new RuntimeException( "Cannot get the remote bundle context", reason );
    }

    private String getHost()
    {
        return null; //"localhost";
    }

    public long getTimeout()
    {
        return m_timeout;
    }

    private int getPort()
    {
        return 1412;
    }
}
