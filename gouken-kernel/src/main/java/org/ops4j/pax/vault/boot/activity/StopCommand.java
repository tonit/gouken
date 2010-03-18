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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.vault.boot.Command;
import org.ops4j.pax.vault.boot.Vault;

/**
 * @author Toni Menzel
 * @since Mar 4, 2010
 */
public class StopCommand implements Command
{

    private static Log LOG = LogFactory.getLog( StopCommand.class );

    public StopCommand()
    {
    }

    public void execute()
    {
        try
        {
            Vault vault = new RemoteCommand().getRbc();

            vault.stop();
            LOG.info( "Stopped." );
        } catch( Exception e )
        {
            LOG.info( "Framework is already stopped." );
        }

    }


}
