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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.discovery.tools.DiscoverSingleton;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.vault.api.Vault;

/**
 * @author Toni Menzel
 * @since Jan 12, 2010
 */
public class DefaultVault implements Vault
{

    private Framework m_framework;
    private String[] m_bundles = new String[0];
    private Log LOG = LogFactory.getLog( DefaultVault.class );

    public void start()
    {
        ClassLoader parent = null;
        try
        {
            
            final Map<String, String> p = new HashMap<String, String>();
            String folder = System.getProperty( "user.home" ) + File.separator + "osgi";
            FileUtils.delete( new File( folder ) );
            p.put( "org.osgi.framework.storage", folder );
            p.put( "org.osgi.framework.system.packages.extra", "org.ops4j.pax.vault.api;version=0.1.0.SNAPSHOT" );

            // TODO fix ContextClassLoaderUtils.doWithClassLoader() and replace logic with it.
            parent = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader( null );

            FrameworkFactory factory = (FrameworkFactory) DiscoverSingleton.find( FrameworkFactory.class );

            m_framework = factory.newFramework( p );
            m_framework.init();

            BundleContext context = m_framework.getBundleContext();
            for( String bundle : m_bundles )
            {
                Bundle b = context.installBundle( bundle );
            }
            m_framework.start();
            LOG.debug( "Started: " + m_framework.getSymbolicName() );

            for( Bundle b : m_framework.getBundleContext().getBundles() )
            {
                b.start();
                LOG.debug( "Started: " + b.getSymbolicName() );
            }
            Thread.currentThread().setContextClassLoader( parent );
        } catch( Exception e )
        {
            e.printStackTrace();
        } finally
        {
            if( parent != null )
            {
                Thread.currentThread().setContextClassLoader( parent );

            }
        }
    }

    public int state()
    {
        return 0;
    }

    public void stop()
    {
        try
        {
            m_framework.stop();

            m_framework.waitForStop( 1000 );
        } catch( BundleException e )
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch( InterruptedException e )
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
