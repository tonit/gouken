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
package org.ops4j.pax.vault.boot;

import java.io.IOException;
import com.sun.akuma.Daemon;
import com.sun.akuma.JavaVMArguments;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Toni Menzel
 * @since Mar 6, 2010
 */
public class TDaemon extends Daemon
{

    private static Log LOG = LogFactory.getLog( TDaemon.class );

    private boolean m_daemonizeEnabled;

    public TDaemon( boolean daemonizeEnabled )
    {
        super();
        m_daemonizeEnabled = daemonizeEnabled;
        LOG.info( "TDaemon set to " + m_daemonizeEnabled );
    }

    @Override
    public void init()
        throws Exception
    {
        if( m_daemonizeEnabled )

        {
            super.init();
        }
    }

    @Override
    public void daemonize()
        throws IOException
    {
        if( m_daemonizeEnabled )

        {
            super.daemonize();
        }
    }

    @Override
    public void daemonize( JavaVMArguments args )
    {
        if( m_daemonizeEnabled )
        {
            super.daemonize( args );
        }
    }

    @Override
    public boolean isDaemonized()
    {
        if( m_daemonizeEnabled )
        {
            return super.isDaemonized();
        }
        else
        {
            return true;
        }
    }
}
