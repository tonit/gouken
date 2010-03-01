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
package org.ops4j.pax.vault.builder;

import java.io.InputStream;
import org.ops4j.pax.vault.api.VaultWeaver;

/**
 * @author Toni Menzel
 * @since Jan 13, 2010
 */
public class URLWeaver implements VaultWeaver
{

    public InputStream weave( InputStream inp )
    {
        // add resources to incoming war and stream result out to result.
        // use base-store as an intermed step.
        return null;
    }
}
