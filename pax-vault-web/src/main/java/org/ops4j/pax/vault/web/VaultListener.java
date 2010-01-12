package org.ops4j.pax.vault.web; /**
 * @author Toni Menzel
 * @since Jan 12, 2010
 */

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionBindingEvent;
import org.apache.commons.discovery.tools.DiscoverSingleton;
import org.ops4j.pax.vault.api.Vault;
import org.ops4j.pax.vault.api.VaultFactory;

public class VaultListener implements ServletContextListener,
                                      HttpSessionListener, HttpSessionAttributeListener
{

    private Vault m_vault;

    public VaultListener()
    {
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized( ServletContextEvent sce )
    {
        System.out.println( "INIT VAULT !" );
        VaultFactory factory = (VaultFactory) DiscoverSingleton.find( VaultFactory.class );
        m_vault = factory.newInstance();
        m_vault.start();
        /* This method is called when the servlet context is
           initialized(when the Web application is deployed). 
           You can initialize servlet context related data here.
        */
    }

    public void contextDestroyed( ServletContextEvent sce )
    {
        System.out.println( "KILL VAULT !" );
        m_vault.stop();
        /* This method is invoked when the Servlet Context 
           (the Web application) is undeployed or 
           Application Server shuts down.
        */
    }

    // -------------------------------------------------------
    // HttpSessionListener implementation
    // -------------------------------------------------------
    public void sessionCreated( HttpSessionEvent se )
    {
        /* Session is created. */
    }

    public void sessionDestroyed( HttpSessionEvent se )
    {
        /* Session is destroyed. */
    }

    // -------------------------------------------------------
    // HttpSessionAttributeListener implementation
    // -------------------------------------------------------

    public void attributeAdded( HttpSessionBindingEvent sbe )
    {
        /* This method is called when an attribute 
           is added to a session.
        */
    }

    public void attributeRemoved( HttpSessionBindingEvent sbe )
    {
        /* This method is called when an attribute
           is removed from a session.
        */
    }

    public void attributeReplaced( HttpSessionBindingEvent sbe )
    {
        /* This method is invoked when an attibute
           is replaced in a session.
        */
    }
}
