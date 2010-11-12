package com.okidokiteam.gouken;

/**
 * Vault is an abstraction of a dynamic module framework, commonly implemented using OSGi.
 * But it does not have to be OSGi. It is a subset.
 *
 * For example it does not allow installing single raw artifacts (bundles) or managing low level bundle lifecycle on a user level.
 * Instead, it delegates those types to a management agent installed.
 * The ubstract notion for unit of deployment is {@link com.okidokiteam.gouken.VaultConfiguration} though its up to the vault implementation to accept, transform it.
 * Also its up to the vault if it for example passes the content in configuration direclty to the technical framework (installing bundles) or issuing it to
 * a real management agent like a DeploymentPackage based one.
 *
 * A vault can be started and stopped. Thats all. Upon startup you get a interaction handle that you can use to authenticate a trusted
 * management agent client.
 */
public interface Vault extends VaultAgent
{

    /**
     * Start the underlying vault
     *
     * @return VaultConfigurationSource instance. Change and pass commits to the vault update method (see VaultAgent)
     *
     * @throws KernelException         in case of an unexpected problem.
     * @throws KernelWorkflowException Raised e.g. when starting a already running vault again.
     */
    VaultConfigurationSource start()
        throws KernelWorkflowException, KernelException;


    /**
     * Stops a vault. Also invalidates
     *
     * @throws KernelException         in case of an unexpected problem.
     * @throws KernelWorkflowException For example when handle is not valid. And/Or vault is not running.
     */
    void stop( )
        throws KernelWorkflowException, KernelException;
}
