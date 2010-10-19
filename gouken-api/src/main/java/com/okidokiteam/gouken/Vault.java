package com.okidokiteam.gouken;

/**
 * Vault is an abstraction of a dynamic module framework, commonly implemented using OSGi.
 * But it does not have to be OSGi. It is a subset.
 *
 * For example it does not allow installing single raw artifacts (bundles) or managing low level bundle lifecycle on a user level.
 * Instead, it delegates those types to a management agent installed.
 *
 * A vault can be started and stopped. Thats all. Upon startup you get a interaction handle that you can use to authenticate a trusted
 * management agent client.
 */
public interface Vault
{

    /**
     * Start the underlying vault
     *
     * @return VaultHandle instance.
     *
     * @throws KernelException         in case of an unexpected problem.
     * @throws KernelWorkflowException Raised e.g. when starting a already running vault again.
     */
    VaultHandle start()
        throws KernelWorkflowException, KernelException;

    /**
     * Stops a vault. Also invalidates
     *
     * @param handle that has been derived when starting the vault.
     *
     * @throws KernelException         in case of an unexpected problem.
     * @throws KernelWorkflowException For example when handle is not valid. And/Or vault is not running.
     */
    void stop( VaultHandle handle )
        throws KernelWorkflowException, KernelException;
}
