package com.okidokiteam.gouken;

/**
 * Vault
 */
public interface Vault
{

    void start()
        throws KernelException;



    void stop()
        throws KernelException;
}
