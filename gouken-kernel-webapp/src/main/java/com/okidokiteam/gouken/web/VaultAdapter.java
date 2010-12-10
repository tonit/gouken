package com.okidokiteam.gouken.web;

import java.io.InputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import com.okidokiteam.gouken.KernelException;
import com.okidokiteam.gouken.KernelWorkflowException;
import com.okidokiteam.gouken.Vault;
import com.okidokiteam.gouken.VaultAgent;
import com.okidokiteam.gouken.kernel.CoreVault;
import com.okidokiteam.gouken.simpleagent.SimpleVaultAgent;
import sun.nio.cs.FastCharsetProvider;
import org.ops4j.pax.repository.Resolver;
import org.ops4j.pax.repository.maven.FastLocalM2Resolver;
import org.ops4j.store.Store;
import org.ops4j.store.StoreFactory;

/**
 *
 */
public class VaultAdapter implements ServletContextListener
{

    private Vault m_vault;
    private Store<InputStream> m_store = StoreFactory.defaultStore();

    public void contextInitialized( ServletContextEvent servletContextEvent )
    {
        // find an agent:

        VaultAgent agent = findAgent( servletContextEvent.getServletContext() );
        m_vault = new CoreVault<Void>( new WebVaultSettings() );
        try
        {
            m_vault.start( agent );
        } catch( Exception e )
        {
            e.printStackTrace();
            throw new RuntimeException( "Noooooo!", e );
        }
    }

    public void contextDestroyed( ServletContextEvent servletContextEvent )
    {
        try
        {
            m_vault.stop();
        } catch( KernelWorkflowException e )
        {
            e.printStackTrace();
        } catch( KernelException e )
        {
            e.printStackTrace();
        }
        m_vault = null;
    }

    private VaultAgent findAgent( ServletContext servletContext )
    {
        return new SimpleVaultAgent( getResolver( servletContext ), m_store );
    }

    public Resolver getResolver( ServletContext servletContext )
    {
        return new FastLocalM2Resolver();
        // return new ServletContextResourceResolver( m_store, servletContext );
    }


}
