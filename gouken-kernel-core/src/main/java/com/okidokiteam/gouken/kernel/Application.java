package com.okidokiteam.gouken.kernel;import javax.inject.Inject;import com.okidokiteam.gouken.ManagementAgent;import com.okidokiteam.gouken.Vault;/** * Gouken Client Component. */public class Application<T> {    @Inject    Vault<T> vault;    @Inject    ManagementAgent agent;    public Vault<T> getVault()    {        return vault;    }    public ManagementAgent getAgent()    {        return agent;    }}