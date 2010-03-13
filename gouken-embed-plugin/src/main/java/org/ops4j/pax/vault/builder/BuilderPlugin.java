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
package org.ops4j.pax.vault.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.DefaultArtifactCollector;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Generates the dependencies properties file
 *
 * @version $Id: $
 * @goal generate
 * @phase generate-resources
 * @requiresDependencyResolution test
 * @description Generates the initial provisioning descriptor for gouken based on configured data and pom dependencies.
 */
public class BuilderPlugin extends AbstractMojo
{

    protected static final String SEPARATOR = "/";

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * @parameter expression='true'
     * @required
     */
    protected boolean recursive;

    /**
     * The file to generate
     *
     * @parameter default-value="${project.build.directory}/classes/META-INF/gouken/provisioning.properties"
     */

    private File outputFile;

    /**
     * The file to generate
     *
     * @parameter default-value="${project.build.directory}/classes/META-INF/gouken/"
     */

    private File folder;

    /**
     * @parameter default-value="${localRepository}"
     */
    protected ArtifactRepository localRepo;

    /**
     * @parameter default-value="${project.remoteArtifactRepositories}"
     */
    protected List remoteRepos;

    /**
     * @component
     */
    protected ArtifactMetadataSource artifactMetadataSource;

    /**
     * @component
     */
    protected ArtifactResolver resolver;

    protected ArtifactCollector collector = new DefaultArtifactCollector();

    /**
     * @component
     */
    protected ArtifactFactory factory;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        OutputStream out = null;
        try
        {
            outputFile.getParentFile().mkdirs();
            out = new FileOutputStream( outputFile );
            StringBuilder sb = new StringBuilder();
            Properties p = new Properties();
            Set<Artifact> artifacts = project.getArtifacts();
            for( Artifact a : artifacts )
            {
                if( a.getScope().equals( "provided" ) )
                {
                    populateProperties( sb, a.getFile().getName() );
                    populateFile( a.getFile() );
                }
            }

            p.put( "bundles", sb.toString() );
            p.store( out, "Written at " + new Date() );
            getLog().info( "Created: " + outputFile );
        } catch( Exception e )
        {
            throw new MojoExecutionException(
                "Unable to create dependencies file: " + e, e
            );
        } finally
        {
            if( out != null )
            {
                try
                {
                    out.close();
                } catch( IOException e )
                {
                    getLog().info( "Failed to close: " + outputFile + ". Reason: " + e, e );
                }
            }
        }
    }

    private void populateFile( File file )
        throws IOException
    {
        // Basically: copy file to new destination
        FileUtils.copyFile( file, new File( folder, file.getName() ) );
        getLog().info( "Copied file " + file.getName() );
    }

    protected void populateProperties( StringBuilder sb, String s )
        throws IOException
    {
        sb.append( s );
        sb.append( "," );
    }


}
