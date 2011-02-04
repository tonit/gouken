package com.okidokiteam.gouken;

/**
 * Vault is an abstraction of a dynamic module framework, commonly implemented using OSGi.
 * But it does not have to be OSGi. It is a subset.
 *
 * For example it does not allow installing single raw artifacts (bundles) or managing low level bundle lifecycle on a user level.
 * Instead, it delegates those types to a management agent installed.
 *
 * A vault can be started and stopped. Thats all. Upon startup you get a interaction handle  (PUSHTYPE) that was registered by the ManagementAgent passed upon startup.
 */
public interface Vault<PUSHTYPE>
{

    /**
     * Start the underlying vault
     *
     * @param agent Agent that manages this vault
     *
     * @return VaultConfigurationSource instance. Change and pass commits to the vault update method (see ManagementAgent)
     *
     * @throws KernelException         in case of an unexpected problem.
     * @throws KernelWorkflowException Raised e.g. when starting a already running vault again.
     */
    PUSHTYPE start( ManagementAgent agent )
        throws KernelWorkflowException, KernelException;

    /**
     * Stops a vault. Also invalidates
     *
     * @throws KernelException         in case of an unexpected problem.
     * @throws KernelWorkflowException For example when handle is not valid. And/Or vault is not running.
     */
    void stop()
        throws KernelWorkflowException, KernelException;
}
