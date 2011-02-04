package com.okidokiteam.gouken.simpleagent;

import com.okidokiteam.gouken.GoukenRuntimeArtifact;
import com.okidokiteam.gouken.ManagementAgent;
import org.ops4j.pax.repository.RepositoryException;

public class SimpleManagementAgent implements ManagementAgent<GoukenRuntimeArtifact> {

    public SimpleManagementAgent()
    {
    }

    @Override
    public String toString()
    {
        return "[SimpleManagementAgent]";
    }

    public GoukenRuntimeArtifact[] getRuntimeParts()
        throws RepositoryException
    {
        return new GoukenRuntimeArtifact[ 0 ];
    }
}
