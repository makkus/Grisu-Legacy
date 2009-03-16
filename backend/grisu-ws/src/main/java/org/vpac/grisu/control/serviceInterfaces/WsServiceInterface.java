

package org.vpac.grisu.control.serviceInterfaces;

import javax.jws.WebService;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.annotations.EnableMTOM;
import org.codehaus.xfire.service.invoker.AbstractInvoker;
import org.ietf.jgss.GSSException;
import org.vpac.grisu.control.ServiceInterface;
import org.vpac.grisu.control.ServiceTemplateManagement;
import org.vpac.grisu.control.exceptions.NoValidCredentialException;
import org.vpac.grisu.control.utils.ServerPropertiesManager;
import org.vpac.grisu.credential.model.ProxyCredential;
import org.w3c.dom.Document;

/**
 * This class implements a {@link ServiceInterface} to use for a web service. 
 * The credential is written into the session so that it can be retrieved easily when needed.
 * 
 * @author Markus Binsteiner
 *
 */
@EnableMTOM
@WebService(endpointInterface = "org.vpac.grisu.control.ServiceInterface", targetNamespace = "http://grisu.vpac.org/grisu-ws" )
public class WsServiceInterface extends AbstractServiceInterface implements
		ServiceInterface {
	
	private ProxyCredential credential = null;
	
	/**
	 * Gets the credential from memory or the session context if the one from memory is already expired or about to expire.
	 * 
	 * @return the credential
	 * @throws NoValidCredentialException
	 */
	protected ProxyCredential getCredential() throws NoValidCredentialException {

		MessageContext context = AbstractInvoker.getContext();
//		MessageContext context = MessageContextHelper.getContext();
		
		if ( this.credential == null || ! this.credential.isValid() ) {
			myLogger.debug("No valid credential in memory. Fetching it from session context...");
			this.credential = (ProxyCredential)(context.getSession().get("credential")); 
			if ( this.credential == null || ! this.credential.isValid() ) {
				throw new NoValidCredentialException("Could not get credential from session context.");
			}
			getUser().cleanCache();
		} else
			// check whether min lifetime as configured in server config file is reached
			try {
				long oldLifetime = this.credential.getGssCredential().getRemainingLifetime();
				if ( oldLifetime < ServerPropertiesManager.getMinProxyLifetimeBeforeGettingNewProxy() ) {
					myLogger.debug("Credential reached minimum lifetime. Getting new one from session. Old lifetime: "+oldLifetime);
					this.credential = (ProxyCredential)(context.getSession().get("credential")); 
					if ( this.credential == null || ! this.credential.isValid() ) {
						throw new NoValidCredentialException("Could not get credential from session context.");
					}
					user.cleanCache();
					myLogger.debug("Success. New lifetime: "+this.credential.getGssCredential().getRemainingLifetime());
				}
			} catch (GSSException e) {
				myLogger.error("Could not read remaining lifetime from GSSCredential. Retrieving new one from session context.");
				if ( this.credential == null || ! this.credential.isValid() ) {
					throw new NoValidCredentialException("Could not get credential from session context.");
				}
				this.credential = (ProxyCredential)(context.getSession().get("credential")); 
				user.cleanCache();
			}
		
		return this.credential;
	}

	// not needed here because username and password is already in the http header
	public void login(String username, char[] password)
			throws NoValidCredentialException {
		
		// nothing to do here anymore because all the myproxy stuff is now
		// in the inhandler of the web service
		
	}

	// only destroys the session. maybe I should do more here?
	public String logout() {

		myLogger.debug("Exiting...");
		this.credential.destroy();
		return null;
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vpac.grisu.control.ServiceInterface#getTemplate(java.lang.String)
	 */
	public Document getTemplate(String application) {
		return ServiceTemplateManagement.getAvailableTemplate(application);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vpac.grisu.control.ServiceInterface#getTemplate(java.lang.String,
	 *      java.lang.String)
	 */
	public Document getTemplate(String application, String version) {
		// not properly implemented yet
		return ServiceTemplateManagement.getAvailableTemplate(application);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vpac.grisu.control.ServiceInterface#listHostedApplications()
	 */
	public String[] listHostedApplicationTemplates() {
		return ServiceTemplateManagement.getAllAvailableApplications();
	}
	
}