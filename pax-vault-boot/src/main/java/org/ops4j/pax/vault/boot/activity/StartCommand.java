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

import java.io.File;
import java.util.Map;
import com.sun.akuma.Daemon;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.vault.boot.Command;
import org.ops4j.pax.vault.boot.Main;
import org.ops4j.pax.vault.boot.VaultBoot;

/**
 * @author Toni Menzel
 * @since Mar 4, 2010
 */
public class StartCommand implements Command
{

    private static Log LOG = LogFactory.getLog( Main.class );

    public StartCommand( Map<String, String> map )
    {

    }

    public void execute()
    {
        File base = new File( "." ).getAbsoluteFile();
        try
        {
            // root folder must be given:
            VaultBoot boot = new VaultBoot( base, "" );
            boot.init();
            boot.start();
        } catch( Throwable e )
        {
            LOG.fatal( "Error: " + e );
        }
    }

    private void daemonize()
        throws Exception
    {
        Daemon d = new Daemon();
        if( d.isDaemonized() )
        {
            // perform initialization as a daemon
            // this involves in closing file descriptors, recording PIDs, etc.
            d.init();

        }
        else
        {
            // if you are already daemonized, no point in daemonizing yourself again,
            // so do this only when you aren't daemonizing.
            d.daemonize();
            // exit parent (real stuff is forked)
            System.exit( 0 );
        }

        LOG.info( "Vault STARTER" );
    }
}
