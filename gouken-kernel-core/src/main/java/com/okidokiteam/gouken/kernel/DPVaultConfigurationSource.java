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

import java.io.IOException;
import java.io.InputStream;
import com.okidokiteam.gouken.KernelException;
import com.okidokiteam.gouken.VaultConfiguration;
import com.okidokiteam.gouken.VaultConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.base.io.InputStreamSource;
import org.ops4j.pax.swissbox.tinybundles.dp.DP;
import org.ops4j.pax.swissbox.tinybundles.dp.TinyDP;

/**
 * Stateful tracker of versions being issued to a vault.
 */
public class DPVaultConfigurationSource implements VaultConfigurationSource
{

    private static Logger LOG = LoggerFactory.getLogger( DPVaultConfigurationSource.class );

    private TinyDP m_tinyDP;

    private volatile long version;

    public DPVaultConfigurationSource()
    {
        // its a DP!
        version = 0l;
        m_tinyDP = DP.newDeploymentPackage();
    }

    private synchronized String getNextVersion()
    {
        return "" + ( ++version );
    }

    public synchronized DPVaultConfigurationSource set( String bundle, InputStream in )
        throws IOException
    {
        m_tinyDP.setBundle( bundle, in );
        return this;
    }

    public synchronized VaultConfiguration build()
        throws KernelException
    {
        m_tinyDP.setSymbolicName( "Gouken-Update-Package" ).setVersion( getNextVersion() );
        return new VaultConfiguration()
        {

            public InputStreamSource get()
            {
                return new InputStreamSource()
                {

                    public InputStream get()
                        throws IOException
                    {
                        return m_tinyDP.build();
                    }
                };
            }
        };


    }
}
