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
package com.okidokiteam.gouken.updatewatch;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.felix.dependencymanager.Service;
import org.apache.felix.dependencymanager.ServiceStateListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.log.LogService;

/**
 * @author Toni Menzel
 * @since Jan 21, 2010
 */
public class WatchService implements ServiceStateListener, Runnable
{

    private volatile LogService logService;
    private volatile BundleContext m_context;
// just accessed from inside t
    private Map<Long, String> changes;

    private Thread m_t;

    public WatchService( BundleContext context )
    {
        m_context = context;

    }

    public void started( Service service )
    {
        m_t = new Thread( this );
        m_t.start();
    }

    public void stopping( Service service )
    {

    }

    public void starting( Service service )
    {

    }

    public void stopped( Service service )
    {
        m_t.interrupt();

        m_t = null;
    }

    public void run()
    {
        logService.log( LogService.LOG_INFO, "UpdateWatch is beginning its service." );
        changes = new HashMap<Long, String>();
        try
        {
            while( !m_t.isInterrupted() )
            {

                for( Bundle b : m_context.getBundles() )
                {

                    if( m_t.isInterrupted() )
                    {
                        break;
                    }

                    if( needsUpdate( b ) )
                    {
                        try
                        {
                            b.update();
                            logService.log( LogService.LOG_INFO, "Bundle " + b.getBundleId() + " has been updated." );
                        } catch( BundleException e )
                        {
                            this.logService.log( LogService.LOG_ERROR, "Problem updating " + b );
                        }
                    }
                }
                Thread.sleep( 500 );
            }
        } catch( InterruptedException e )
        {
            e.printStackTrace();
        }
    }

    private boolean needsUpdate( Bundle bundle )
    {
        String val = getCurrent( bundle );
        String in = changes.get( bundle.getBundleId() );
        changes.put( bundle.getBundleId(), val );

        if( in != null )
        {
            if( !val.equals( in ) )
            {
                return true;
            }
        }
        else
        {
            System.out.println( "Bundle " + bundle.getBundleId() + " Location: " + bundle.getLocation() );

        }
        return false;

    }

    private String getCurrent( Bundle bundle )
    {
        try
        {
            URL url = new URL( bundle.getLocation() );
            File f = new File( url.toURI() );
            return String.valueOf( f.lastModified() );
        } catch( MalformedURLException e )
        {

        } catch( URISyntaxException e )
        {

        } catch( Exception e )
        {

        }

        return bundle.getLocation();
    }
}
