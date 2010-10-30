/*
 * Copyright (C) 2010 Okidokiteam
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.okidokiteam.gouken.kernel;

import org.apache.felix.dm.Component;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.deploymentadmin.DeploymentAdmin;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class OSGiBridge
{

    private static Logger LOG = LoggerFactory.getLogger( OSGiBridge.class );

    private BundleContext m_context;

    // Injected
    private DeploymentAdmin m_deployment;

    private Component m_comp;
    private Class m_class;

    private DependencyManager m_dep;
    private PackageAdmin m_pack;

    public OSGiBridge( BundleContext context, Class clazz )
    {
        m_context = context;
        m_class = clazz;
    }

    public OSGiBridge startMe()
    {
        LOG.info( "startMe" );
        wireServices( m_context, m_class );
        m_comp.start();
        LOG.info( "startMe done" );

        return this;
    }

    public void stop()
    {
        m_comp.stop();
    }

    private void wireServices( BundleContext context, Class c )
    {
        m_dep = new DependencyManager( context );
        LOG.info( " + " + c.getName() );

        m_comp = m_dep.createComponent().setImplementation( this )
            .add( m_dep.createServiceDependency().setService( DeploymentAdmin.class ).setRequired( true ) )
            .add( m_dep.createServiceDependency().setService( PackageAdmin.class ).setRequired( true )
            );

        

        // LOG.info( "Service: " + m_deployment );
    }

    DeploymentAdmin getService()
    {
        LOG.info( "-- " + m_deployment );
        LOG.info( "-- " + m_pack );

        return m_deployment;
    }


}
