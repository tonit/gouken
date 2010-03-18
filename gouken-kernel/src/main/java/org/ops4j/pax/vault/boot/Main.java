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

import java.util.HashMap;
import java.util.Map;
import com.sun.akuma.Daemon;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.vault.boot.activity.RestartCommand;
import org.ops4j.pax.vault.boot.activity.RemoteCommand;
import org.ops4j.pax.vault.boot.activity.StartCommand;
import org.ops4j.pax.vault.boot.activity.StatusCommand;
import org.ops4j.pax.vault.boot.activity.StopCommand;
import org.ops4j.pax.vault.boot.activity.UnknownCommand;

/**
 * @author Toni Menzel
 * @since Mar 1, 2010
 */
public class Main
{

    public static void main( String[] args )
        throws Exception
    {
        new CommandController().mapCommand( args ).execute();
    }


}
