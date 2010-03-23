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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.DefaultArtifactCollector;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.FileUtils;
import org.ops4j.io.StreamUtils;

/**
 * Woodoo
 *
 * @goal gouken
 * @requiresDependencyResolution test
 * @description build an gouken assembly
 */
public class BuilderPlugin extends AbstractMojo
{

    protected static final String SEPARATOR = "/";

    /**
     * The directory for the generated JAR.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private String buildDirectory;

    /**
     * Project types which this plugin supports.
     *
     * @parameter
     */
    private List supportedProjectTypes = Arrays.asList( new String[]
        { "gouken" }
    );

    /**
     * @component
     */
    private ArtifactHandlerManager m_artifactHandlerManager;

    /**
     * Classifier type of the bundle to be installed.  For example, "jdk14".
     * Defaults to none which means this is the project's main bundle.
     *
     * @parameter
     */
    protected String classifier;

    /**
     * @component
     */
    private MavenProjectHelper m_projectHelper;

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

    private File provisioningFile;

    /**
     * The file to generate
     *
     * @parameter default-value="${project.build.directory}/classes/META-INF/gouken/"
     */

    private File folder;

    /**
     * The file to generate
     *
     * @parameter default-value="${project.build.directory}/classes/"
     */

    private File kernelFolder;

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
        String projectType = project.getArtifact().getType();
        // ignore unsupported project types, useful when bundleplugin is configured in parent pom
        if( !supportedProjectTypes.contains( projectType ) )
        {
            getLog().warn(
                "Ignoring project type " + projectType + " - supportedProjectTypes = " + supportedProjectTypes
            );
            return;
        }
        getLog().info( "Execute " + BuilderPlugin.class.getName() );
        OutputStream out = null;
        try
        {
            provisioningFile.getParentFile().mkdirs();
            out = new FileOutputStream( provisioningFile );
            StringBuilder sb = new StringBuilder();
            Properties p = new Properties();

            for( Object dep : project.getDependencyArtifacts() )
            {
                Artifact a = (Artifact) dep;
                if( a.getScope().equals( "provided" ) )
                {
                    populateProperties( sb, a.getFile().getName() );
                    populateFile( a.getFile() );
                }
            }

            p.put( "bundles", sb.toString() );
            p.store( out, "Written at " + new Date() );

            extractFile( getKernel() );

            File jarFile = new File( getBuildDirectory(), getBundleName( project ) );
            exportAssembly( jarFile );

            Artifact mainArtifact = project.getArtifact();

            // workaround for MNG-1682: force maven to install artifact using the "jar" handler
            mainArtifact.setArtifactHandler( m_artifactHandlerManager.getArtifactHandler( "jar" ) );

            if( null == classifier || classifier.trim().length() == 0 )
            {
                mainArtifact.setFile( jarFile );
            }
            else
            {
                m_projectHelper.attachArtifact( project, jarFile, classifier );
            }

            getLog().info( "Created: " + jarFile );


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
                    getLog().info( "Failed to close: " + provisioningFile + ". Reason: " + e, e );
                }
            }
        }
    }

    private void exportAssembly( File jarFile )
        throws IOException
    {
        // Just zip folder and write to file as jar. Done.
        JarOutputStream out = new JarOutputStream( new FileOutputStream( jarFile ) );
        try
        {
            File[] index = getFiles();

            for( File f : index )
            {
                //sum them up all
                String rel = relative( f );
                //  System.out.println( "+ " + rel );
                ZipEntry entry = new ZipEntry( rel );
                out.putNextEntry( entry );
                if( f.isFile() )
                {
                    InputStream fis = new FileInputStream( f );
                    try
                    {
                        StreamUtils.copyStream( fis, out, false );
                    } finally
                    {
                        fis.close();
                    }
                }
            }
        } finally
        {
            out.close();
        }
    }

    private String relative( File f )
        throws IOException
    {
        String base = kernelFolder.getCanonicalPath();
        return f.getCanonicalPath().substring( base.length() + 1 );
    }

    private File[] getFiles()
    {
        List<File> list = new ArrayList<File>();
        addAll( list, kernelFolder );
        // push certain keys up:
        File[] arr = list.toArray( new File[list.size()] );
        // sort:
        Arrays.sort( arr, new Comparator<File>()
        {

            public int compare( File file, File file1 )
            {
                if( file.getName().endsWith( "MANIFEST.MF" ) )
                {
                    return -1;
                }
                else
                {
                    if( file.getAbsolutePath().contains( "META-INF" ) )
                    {
                        if( file1.getName().endsWith( "MANIFEST.MF" ) )
                        {
                            return 1;
                        }
                        else
                        {
                            return -1;
                        }
                    }
                    else
                    {
                        return 1;
                    }

                }
            }
        }
        );
        return arr;
    }

    /**
     * recursive !
     *
     * @param list
     * @param folder
     */
    private void addAll( List<File> list, File folder )
    {
        for( File f : folder.listFiles() )
        {
            if( f.isFile() )
            {
                list.add( f );
            }
            else
            {
                list.add( f );
                addAll( list, f );
            }
        }
    }

    protected String getBundleName( MavenProject currentProject )
    {
        String finalName = currentProject.getBuild().getFinalName();
        if( null != classifier && classifier.trim().length() > 0 )
        {
            return finalName + '-' + classifier + ".jar";
        }

        return finalName + ".jar";
    }

    private void extractFile( File kernel )
        throws IOException
    {
        ZipInputStream in = new ZipInputStream( new FileInputStream( kernel ) );
        ZipEntry entry = null;
        while( ( entry = in.getNextEntry() ) != null )
        {
            File sub = new File( entry.getName() );
            if( !entry.isDirectory() )
            {
                File dest = new File( kernelFolder.getAbsolutePath() + "/" + entry.getName() );
                dest.getParentFile().mkdirs();
                OutputStream out = new FileOutputStream( dest );
                try
                {
                    StreamUtils.copyStream( in, out, false );
                } finally
                {
                    out.close();
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

    public File getKernel()
    {
        // latest local kernel for now
        File f = new File( "/Users/tonit/devel/com.okidokiteam/gouken/gouken-kernel/target/gouken-kernel-0.1.0-SNAPSHOT.jar" );
        return f;
    }

    protected String getBuildDirectory()
    {
        return buildDirectory;
    }

    protected void setBuildDirectory( String _buildirectory )
    {
        buildDirectory = _buildirectory;
    }
}
