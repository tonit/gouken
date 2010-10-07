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
package com.okidokiteam.gouken.macosx;

import java.util.HashMap;
import java.util.Map;
import com.okidokiteam.gouken.macosx.activity.RestartCommand;
import com.okidokiteam.gouken.macosx.activity.StartCommand;
import com.okidokiteam.gouken.macosx.activity.StatusCommand;
import com.okidokiteam.gouken.macosx.activity.UnknownCommand;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.okidokiteam.gouken.macosx.activity.StopCommand;

/**
 * @author Toni Menzel
 * @since Mar 18, 2010
 */
public class CommandController
{

    private static Log LOG = LogFactory.getLog( Main.class );

    /**
     * Usually you want to make a framework:
     * 1. run
     * 2. shutdown
     * 3. re-start
     *
     * @param args commandline arguments
     *
     * @return a command
     */
    public Command mapCommand( String[] args )
    {
        Map<String, String> map = new HashMap<String, String>();
        mapArgs( args, map );
        if( map.containsKey( "start" ) )
        {
            return new StartCommand( map );
        }
        else if( map.containsKey( "stop" ) )
        {
            return new StopCommand();

        }
        else if( map.containsKey( "restart" ) )
        {
            return new RestartCommand( new StartCommand( map ), new StopCommand() );
        }
        else if( map.containsKey( "status" ) )
        {
            return new StatusCommand( map );
        }
        else
        {
            return new UnknownCommand( map );
        }
    }

    private static void mapArgs( String[] args, Map<String, String> map )
    {
        for( String s : args )
        {
            if( s != null && s.length() > 0 )
            {
                int eq = s.indexOf( "=" );
                if( eq < 0 )
                {
                    map.put( s, "true" );
                }
                else
                {
                    String key = s.substring( 0, eq );
                    String value = s.substring( eq + 1 );
                    map.put( key, value );

                }
            }
        }
    }
}
