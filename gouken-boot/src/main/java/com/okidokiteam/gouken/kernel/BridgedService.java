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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class BridgedService<T>
{

    private static Logger LOG = LoggerFactory.getLogger( BridgedService.class );

    private BundleContext m_context;

    // Injected
    private volatile T m_deployment;

    private Component m_comp;
    private Class<T> m_class;

    private DependencyManager m_dep;

    public BridgedService( BundleContext context, Class<T> clazz )
    {
        m_context = context;
        m_class = clazz;
    }

    public BridgedService<T> startMe()
    {

        wireServices( m_context, m_class );
        m_comp.start();
        LOG.info( "Tracking " + m_class.getName() + " with " + m_comp );

        return this;
    }

    public void stop()
    {
        m_comp.stop();
    }

    private void wireServices( BundleContext context, Class<T> c )
    {
        m_dep = new DependencyManager( context );
        LOG.info( " + " + c.getName() );

        m_comp = m_dep.createComponent().setImplementation( this )
            .add( m_dep.createServiceDependency().setService( c ).setRequired( true ) );
    }

    public T getService()
    {
        return m_deployment;
    }
}
