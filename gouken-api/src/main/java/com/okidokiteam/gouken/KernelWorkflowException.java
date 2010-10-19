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
package com.okidokiteam.gouken;

/**
 * Covers workflow errors. This is usually problem with the client using vault api (like starting an already running vault)
 * and not about problems with vault internals.
 * Whenever this exception is being raised, its certainly an api user error.
 */
public class KernelWorkflowException extends Exception
{

    public KernelWorkflowException( String message )
    {
        super( message );    
    }
}
