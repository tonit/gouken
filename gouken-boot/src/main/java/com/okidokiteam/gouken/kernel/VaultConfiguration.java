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

import java.io.File;
import org.ops4j.io.FileUtils;

/**
 *
 */
public class VaultConfiguration
{

    private static final String WORK = "target/.gouken/";

    private File m_workDir;

    public VaultConfiguration( boolean clean )
    {
        this( new File( WORK ), clean );
    }

    public VaultConfiguration( File workDir, boolean clean )
    {
        m_workDir = workDir;

        if( clean )
        {
            FileUtils.delete( m_workDir );
        }
        getWorkDir().mkdirs();
    }

    /**
     * The uppper most work folder. Will contain cache stuff and other operational files.
     * No need to back this up.
     */
    public File getWorkDir()
    {
        return m_workDir;
    }
}
