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
package com.okidokiteam.gouken.kernel;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.StringTokenizer;

import org.apache.felix.dm.ComponentDeclaration;
import org.apache.felix.dm.ComponentDependencyDeclaration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class MAReport
{
    private static final BundleIdSorter SORTER = new BundleIdSorter();


    public void collect( BundleContext m_context, PrintStream out, PrintStream err )
    {
        boolean nodeps = false;
        boolean notavail = false;
        boolean compact = false;

        ServiceReference[] references = null;
        try
        {
            references = m_context.getServiceReferences( ComponentDeclaration.class.getName(), null );
        } catch (InvalidSyntaxException e)
        {
            e.printStackTrace();
        }
        // show their state
        if (references != null)
        {
            Arrays.sort( references, SORTER );
            long lastBundleId = -1;
            for (int i = 0; i < references.length; i++)
            {
                ServiceReference ref = references[i];
                ComponentDeclaration sc = (ComponentDeclaration) m_context.getService( ref );
                if (sc != null)
                {
                    String name = sc.getName();
                    int state = sc.getState();
                    Bundle bundle = ref.getBundle();
                    long bundleId = bundle.getBundleId();

                    if (!notavail || ( notavail && sc.getState() == ComponentDeclaration.STATE_UNREGISTERED ))
                    {
                        if (lastBundleId != bundleId)
                        {
                            lastBundleId = bundleId;
                            if (compact)
                            {
                                out.println( "[" + bundleId + "] " + compactName( bundle.getSymbolicName() ) );
                            } else
                            {
                                out.println( "[" + bundleId + "] " + bundle.getSymbolicName() );
                            }
                        }
                        if (compact)
                        {
                            out.print( " " + compactName( name ) + " " + compactState( ComponentDeclaration.STATE_NAMES[state] ) );
                        } else
                        {
                            out.println( "  " + name + " " + ComponentDeclaration.STATE_NAMES[state] );
                        }
                        if (!nodeps)
                        {
                            ComponentDependencyDeclaration[] dependencies = sc.getComponentDependencies();
                            if (dependencies != null && dependencies.length > 0)
                            {
                                if (compact)
                                {
                                    out.print( '(' );
                                }
                                for (int j = 0; j < dependencies.length; j++)
                                {
                                    ComponentDependencyDeclaration dep = dependencies[j];
                                    String depName = dep.getName();
                                    String depType = dep.getType();
                                    int depState = dep.getState();
                                    if (compact)
                                    {
                                        if (j > 0)
                                        {
                                            out.print( ' ' );
                                        }
                                        out.print( compactName( depName ) + " " + compactState( depType ) + " " + compactState( ComponentDependencyDeclaration.STATE_NAMES[depState] ) );
                                    } else
                                    {
                                        out.println( "    " + depName + " " + depType + " " + ComponentDependencyDeclaration.STATE_NAMES[depState] );
                                    }
                                }
                                if (compact)
                                {
                                    out.print( ')' );
                                }
                            }
                        }
                        if (compact)
                        {
                            out.println();
                        }

                    }
                    m_context.ungetService( ref );
                }
            }
        }

    }

    /**
     * Compact names that look like state strings. State strings consist of
     * one or more words. Each word will be shortened to the first letter,
     * all letters concatenated and uppercased.
     */
    private String compactState( String input )
    {
        StringBuffer output = new StringBuffer();
        StringTokenizer st = new StringTokenizer( input );
        while (st.hasMoreTokens())
        {
            output.append( st.nextToken().toUpperCase().charAt( 0 ) );
        }
        return output.toString();
    }

    /**
     * Compacts names that look like fully qualified class names. All packages
     * will be shortened to the first letter, except for the last one. So
     * something like "org.apache.felix.MyClass" will become "o.a.f.MyClass".
     */
    private String compactName( String input )
    {
        StringBuffer output = new StringBuffer();
        int lastIndex = 0;
        for (int i = 0; i < input.length(); i++)
        {
            char c = input.charAt( i );
            switch (c) {
            case '.':
                output.append( input.charAt( lastIndex ) );
                output.append( '.' );
                lastIndex = i + 1;
                break;
            case ' ':
            case ',':
                if (lastIndex < i)
                {
                    output.append( input.substring( lastIndex, i ) );
                }
                output.append( c );
                lastIndex = i + 1;
                break;
            }
        }
        if (lastIndex < input.length())
        {
            output.append( input.substring( lastIndex ) );
        }
        return output.toString();
    }

    public String getName()
    {
        return "dm";
    }

    public String getShortDescription()
    {
        return "list dependency manager component diagnostics.";
    }

    public String getUsage()
    {
        return "dm [nodeps] [notavail] [compact] [<bundleid> ...]";
    }
}

final class BundleIdSorter implements Comparator
{
    public int compare( Object o1, Object o2 )
    {
        ServiceReference r1 = (ServiceReference) o1;
        ServiceReference r2 = (ServiceReference) o2;
        long id1 = r1.getBundle().getBundleId();
        long id2 = r2.getBundle().getBundleId();
        return id1 > id2 ? 1 : -1;
    }
}
