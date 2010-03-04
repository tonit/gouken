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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.vault.boot.activity.CompositeCommand;
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

    private static Log LOG = LogFactory.getLog( Main.class );

    /**
     * Usually you want to make a framework:
     * 1. run
     * 2. shutdown
     * 3. re-start
     *
     * @param args
     */
    public static void main( String[] args )
        throws Exception
    {
        new Main().mapCommand( args ).execute();
    }

    private void eval( String[] args )
    {

        {

        }
    }

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
            return new StopCommand( map );

        }
        else if( map.containsKey( "restart" ) )
        {
            return new CompositeCommand( new StopCommand( map ), new StartCommand( map ) );

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
            if( s != null && !s.isEmpty() )
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

    private static void buildContent( JFrame aFrame )
    {
        JPanel panel = new JPanel();

        panel.add( new JLabel( "Hello" ) );

        JButton ok = new JButton( "OK" );
        ok.addActionListener( new ShowDialog( aFrame ) );
        panel.add( ok );

        aFrame.getContentPane().add( panel );
    }

    private static final class ShowDialog implements ActionListener
    {

        /**
         * Defining the dialog's owner JFrame is highly recommended.
         */
        ShowDialog( JFrame aFrame )
        {
            fFrame = aFrame;
        }

        public void actionPerformed( ActionEvent aEvent )
        {
            JOptionPane.showMessageDialog( fFrame, "This is a dialog" );
        }

        private JFrame fFrame;
    }
}
