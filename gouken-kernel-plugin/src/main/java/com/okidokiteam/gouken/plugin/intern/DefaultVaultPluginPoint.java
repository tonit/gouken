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
package com.okidokiteam.gouken.plugin.intern;

import com.okidokiteam.gouken.plugin.VaultPluginPoint;

/**
 * 
 */
public class DefaultVaultPluginPoint<T> implements VaultPluginPoint
{
    // todo: really needed ?
    private final Class<T> m_type;

    public DefaultVaultPluginPoint(Class<T> type ) {
        m_type = type;
    }

    public T getType()
    {
        return null;
    }

    public T[] getPlugins()
    {
        return null;
    }
}
