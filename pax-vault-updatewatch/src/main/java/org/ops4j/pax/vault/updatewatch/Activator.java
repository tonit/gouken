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
package org.ops4j.pax.vault.updatewatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.dependencymanager.DependencyActivatorBase;
import org.apache.felix.dependencymanager.DependencyManager;
import org.apache.felix.dependencymanager.Service;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

/**
 * @author Toni Menzel
 * @since Jan 21, 2010
 */
public class Activator extends DependencyActivatorBase
{

    //Log log = LogFactory.getLog( Activator.class );

    @Override
    public void init( BundleContext bundleContext, DependencyManager dependencyManager )
        throws Exception
    {

        WatchService ws = new WatchService( bundleContext );

        Service ourService = createService().setImplementation( ws )
            .add( createServiceDependency().setService( LogService.class ) );
        ourService.addStateListener( ws );

        dependencyManager.add( ourService );
    }

    @Override
    public void destroy( BundleContext bundleContext, DependencyManager dependencyManager )
        throws Exception
    {

    }


}
