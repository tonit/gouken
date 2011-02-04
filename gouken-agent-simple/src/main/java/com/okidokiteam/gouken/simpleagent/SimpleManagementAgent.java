package com.okidokiteam.gouken.simpleagent;

import com.okidokiteam.gouken.ArtifactReference;
import com.okidokiteam.gouken.ManagementAgent;
import org.ops4j.pax.repository.RepositoryException;

public class SimpleManagementAgent implements ManagementAgent {

    public SimpleManagementAgent()
    {
    }

    @Override
    public String toString()
    {
        return "[SimpleManagementAgent]";
    }

    public ArtifactReference[] getRuntimeParts()
        throws RepositoryException
    {
        return new ArtifactReference[ 0 ];
    }
}
