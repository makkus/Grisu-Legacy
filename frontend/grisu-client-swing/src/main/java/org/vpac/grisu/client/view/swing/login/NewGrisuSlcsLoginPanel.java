package org.vpac.grisu.client.view.swing.login;

import grisu.jcommons.interfaces.SlcsListener;
import grith.gsindl.SLCS;
import grith.jgrith.CredentialHelpers;
import grith.sibboleth.ShibListener;
import grith.sibboleth.ShibLoginPanel;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.globus.common.CoGProperties;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.X509ExtensionSet;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.bc.BouncyCastleX509Extension;
import org.globus.gsi.proxy.ext.GlobusProxyCertInfoExtension;
import org.globus.gsi.proxy.ext.ProxyCertInfo;
import org.globus.gsi.proxy.ext.ProxyPolicy;
import org.ietf.jgss.GSSCredential;
import org.python.core.PyInstance;
import org.vpac.grisu.client.control.login.LoginException;
import org.vpac.grisu.client.control.login.LoginHelpers;
import org.vpac.grisu.client.control.utils.ClientPropertiesManager;
import org.vpac.grisu.client.model.login.LoginPanelsHolder;
import org.vpac.grisu.client.view.swing.utils.Utils;
import org.vpac.grisu.control.ServiceInterface;
import org.vpac.helpDesk.model.Person;
import org.vpac.helpDesk.model.PersonException;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class NewGrisuSlcsLoginPanel extends JPanel implements SlcsListener,
ShibListener {

	public static final String DEFAULT_SLCS_URL = "https://slcs1.arcs.org.au/SLCS/login";

	public static GSSCredential init(X509Certificate userCert, PrivateKey userKey,
			int lifetime_in_hours) throws GeneralSecurityException {

		CoGProperties props = CoGProperties.getDefault();

		BouncyCastleCertProcessingFactory factory = BouncyCastleCertProcessingFactory
		.getDefault();

		int proxyType = GSIConstants.GSI_2_PROXY;
		// int proxyType = GSIConstants.GSI_3_IMPERSONATION_PROXY;

		ProxyPolicy policy = new ProxyPolicy(ProxyPolicy.IMPERSONATION);
		ProxyCertInfo proxyCertInfo = new ProxyCertInfo(policy);

		BouncyCastleX509Extension certInfoExt = new GlobusProxyCertInfoExtension(
				proxyCertInfo);

		X509ExtensionSet extSet = null;
		if (proxyCertInfo != null) {
			extSet = new X509ExtensionSet();

			// old OID
			extSet.add(new GlobusProxyCertInfoExtension(proxyCertInfo));
		}

		GlobusCredential proxy = factory.createCredential(
				new X509Certificate[] { userCert }, userKey, props
				//						.getProxyStrength(), props.getProxyLifeTime() * 3600
				.getProxyStrength(), 3600
				* lifetime_in_hours, proxyType, extSet);

		return CredentialHelpers.wrapGlobusCredential(proxy);

	}
	private LoginPanelsHolder loginPanelHolder = null;
	private ShibLoginPanel shibLoginPanel;
	private JButton button;

	private final SLCS slcs;

	/**
	 * Create the panel.
	 */
	public NewGrisuSlcsLoginPanel() {

		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("450px:grow"),
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("108px"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC, }));
		add(getShibLoginPanel_1(), "2, 2, fill, fill");
		add(getButton(), "2, 4, right, bottom");

		getShibLoginPanel_1().addShibListener(this);
		slcs = new SLCS(getShibLoginPanel_1());
		slcs.addSlcsListener(this);

	}

	private JButton getButton() {
		if (button == null) {
			button = new JButton("Login");
			button.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					getShibLoginPanel_1().login();

				}
			});
		}
		return button;
	}

	private ShibLoginPanel getShibLoginPanel_1() {
		if (shibLoginPanel == null) {
			shibLoginPanel = new ShibLoginPanel(DEFAULT_SLCS_URL);
		}
		return shibLoginPanel;
	}

	private Person getUser() {

		String username = "anonymous";
		Person user = null;
		try {
			user = new Person(ClientPropertiesManager.getClientConfiguration(),
					username + " Grisu Shibboleth user");
		} catch (Exception e) {
			try {
				user = new Person("Anonymous");
			} catch (PersonException e1) {
				e1.printStackTrace(System.err);
				// this should never happen
			}
		}
		user.setRole(Person.USER_ROLE);
		user.setNickname(username);
		return user;
	}

	private void lockUI(boolean lock) {
		getButton().setEnabled(!lock);
		if ( lock ) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		} else {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public void setParamsHolder(LoginPanel loginPanel) {

		this.loginPanelHolder = loginPanel;
	}

	public void shibLoginComplete(PyInstance arg0) {


	}

	public void shibLoginFailed(Exception arg0) {

		lockUI(false);
		Utils.showErrorMessage(getUser(), NewGrisuSlcsLoginPanel.this,
				"loginError", arg0);



	}

	public void shibLoginStarted() {
		lockUI(true);
	}

	public void slcsLoginComplete(X509Certificate cert, PrivateKey privateKey) {

		ServiceInterface si;

		try {
			GSSCredential proxy;
			if (loginPanelHolder != null) {
				proxy = init(slcs.getCertificate(), slcs
						.getPrivateKey(), 24 * 10);
			} else {
				return;
			}

			si = LoginHelpers.login(loginPanelHolder.getLoginParams(), proxy);

			loginPanelHolder.loggedIn(si);

		} catch (LoginException e1) {
			Utils.showErrorMessage(getUser(), NewGrisuSlcsLoginPanel.this,
					"loginError", e1);
			lockUI(false);
			return;
		} catch (Exception e) {
			Utils.showErrorMessage(getUser(), NewGrisuSlcsLoginPanel.this,
					"loginError", e);
			lockUI(false);
			return;
		}

		if (si != null) {
			lockUI(false);
			loginPanelHolder.loggedIn(si);
		} else {
			lockUI(false);
			Utils.showErrorMessage(getUser(), NewGrisuSlcsLoginPanel.this,
					"unknownLoginError", null);
		}


	}

	public void slcsLoginFailed(String message, Exception optionalException) {

		lockUI(false);
		Utils.showErrorMessage(getUser(), NewGrisuSlcsLoginPanel.this,
				"loginError", optionalException);

	}

}

