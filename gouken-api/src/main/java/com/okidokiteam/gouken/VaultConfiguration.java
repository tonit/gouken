package com.okidokiteam.gouken;

import java.io.File;
import org.ops4j.pax.repository.Artifact;

/**
 *
 */
public interface VaultConfiguration
{

    /**
     * @return The uppper most work folder. Will contain cache stuff and other operational files.
     *         No need to back this up.
     */
    public File getWorkDir();

    /**
     * @return Bundles that make up the management agent.
     */
    public Artifact[] getSystemBundles();
}
