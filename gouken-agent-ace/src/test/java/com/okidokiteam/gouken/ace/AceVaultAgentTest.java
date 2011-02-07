/*
 * Copyright 2010 Toni Menzel.
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
package com.okidokiteam.gouken.ace;

import java.io.IOException;
import org.junit.Test;
import org.ops4j.pax.repository.RepositoryException;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class AceVaultAgentTest {

    public final static int ENTRIES_IN_SAMPLE = 4;

    @Test
    public void testLoadingPropertiesCorrectly()
        throws IOException, RepositoryException
    {
        AceManagementAgent a;
        AceManagementAgent agent = new AceManagementAgent();
        assertThat( agent.getRuntimeParts().length, is( ENTRIES_IN_SAMPLE ) );
    }
}
