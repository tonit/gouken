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
package com.okidokiteam.gouken.macosx.activity;

import java.util.Map;
import com.okidokiteam.gouken.kernel.StaticVaultConfiguration;
import com.okidokiteam.gouken.kernel.CoreVault;
import com.okidokiteam.gouken.macosx.Command;
import com.okidokiteam.gouken.macosx.MacOSBoot;
import com.okidokiteam.gouken.macosx.TDaemon;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.repository.Artifact;

/**
 * @author Toni Menzel
 * @since Mar 4, 2010
 */
public class StartCommand implements Command
{

    private static Log LOG = LogFactory.getLog( StartCommand.class );
    private Map<String, String> m_map;
    private static final String DEFAULT_TARGET = ".gouken";

    public StartCommand( Map<String, String> map )
    {
        m_map = map;
    }

    public void execute()
    {

        try
        {
            boolean daemonize = true;
            // root folder must be given:
            String daem = m_map.get( "--daemon" );
            if( daem != null && daem.equals( "false" ) )
            {
                daemonize = false;
            }
            StaticVaultConfiguration conf = new StaticVaultConfiguration( true, new Artifact[ 0 ] );
            MacOSBoot boot = new MacOSBoot( new TDaemon( daemonize ), new CoreVault( conf ) );
            boot.init();
            boot.start();
        } catch( Throwable e )
        {
            LOG.fatal( "Error: " + e );
        }
    }


}
