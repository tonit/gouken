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
package org.ops4j.pax.vault.internal;

import java.util.Dictionary;
import java.util.Hashtable;
import javax.servlet.Filter;
import javax.servlet.ServletException;
import org.apache.felix.http.api.ExtHttpService;
import org.apache.wicket.protocol.http.WicketFilter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.ops4j.pax.vault.web.WicketApplication;
/**
 * @author Toni Menzel
 * @since Mar 4, 2010
 */

/**
 * @author Toni Menzel
 * @since Jan 13, 2010
 */
public class Activator implements BundleActivator
{

    private ServiceTracker m_tracker;

    public void start( BundleContext bundleContext )
        throws Exception
    {
        m_tracker = new ServiceTracker( bundleContext, ExtHttpService.class.getName(), null )
        {
            @Override
            public Object addingService( ServiceReference reference )
            {
                ExtHttpService service = (ExtHttpService) super.addingService( reference );
                deploy( service );
                return service;
            }


        };
        m_tracker.open();
    }

    public void stop( BundleContext bundleContext )
        throws Exception
    {
        m_tracker.close();
    }

    public void deploy( ExtHttpService httpService )
    {
        Dictionary dict = new Hashtable();
        ClassLoader parent = Thread.currentThread().getContextClassLoader();

        try
        {
            dict.put( "applicationClassName", WicketApplication.class.getName() );
            //   Thread.currentThread().setContextClassLoader( null );
            Filter filter = new WicketFilter()
            {
                @Override
                protected ClassLoader getClassLoader()
                {
                    return Activator.class.getClassLoader();
                }

            };

            httpService.registerFilter( filter, "/*", dict, 0, httpService.createDefaultHttpContext() );
        } catch( ServletException e )
        {
            throw new RuntimeException( e );
        } finally
        {
            Thread.currentThread().setContextClassLoader( parent );

        }

    }
}
