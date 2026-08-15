/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.apache.directory.studio.connection.ui.widgets;


import org.apache.commons.lang3.StringUtils;
import org.apache.directory.api.ldap.model.constants.SaslQoP;
import org.apache.directory.api.ldap.model.constants.SaslSecurityStrength;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.api.ldap.model.url.LdapUrl.Extension;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.apache.directory.studio.common.ui.HistoryUtils;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCoreConstants;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.apache.directory.studio.connection.core.ConnectionParameter.AuthenticationMethod;
import org.apache.directory.studio.connection.core.ConnectionParameter.Krb5Configuration;
import org.apache.directory.studio.connection.core.ConnectionParameter.Krb5CredentialConfiguration;
import org.apache.directory.studio.connection.core.PasswordsKeyStoreManager;
import org.apache.directory.studio.connection.core.jobs.CheckBindRunnable;
import org.apache.directory.studio.connection.ui.AbstractConnectionParameterPage;
import org.apache.directory.studio.connection.ui.ConnectionUIConstants;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.connection.ui.PasswordsKeyStoreManagerUtils;
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;


// ── CLASS: AuthenticationParameterPage — REBEL PILOT AUTHENTICATION CONSOLE ──────
// Before an X-Wing is cleared to launch, the base computer must verify the pilot's
// identity.  Simple call signs (bind DN + password), Kerberos TGTs (GSSAPI), and
// SASL challenge-response (DIGEST-MD5, CRAM-MD5) are all valid.  This class is
// that authentication console — the "Authentication" tab shown when creating or
// editing a connection.  It owns three UI areas: auth-method combo, bind
// DN/password group, and a scrolled area with collapsible SASL/KRB5 sections.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * {@link AbstractConnectionParameterPage} for authentication settings.
 *
 * <p>This is the second tab in the New Connection Wizard and the Connection
 * Properties dialog.  It supports five authentication methods:</p>
 * <ul>
 *   <li>Anonymous (no credentials)</li>
 *   <li>Simple (bind DN + password)</li>
 *   <li>SASL DIGEST-MD5 (bind DN + password + optional realm)</li>
 *   <li>SASL CRAM-MD5 (bind DN + password)</li>
 *   <li>SASL GSSAPI (Kerberos — optional bind DN if obtaining TGT)</li>
 * </ul>
 *
 * <p>All SASL-specific settings (QOP, security strength, mutual authentication)
 * live in a collapsible "SASL Options" section.  GSSAPI-specific settings
 * (credential provider, KRB5 config source) live in a separate collapsible
 * "KRB5 Options" section.</p>
 *
 * <p>All parameters are serialised to/from LDAP URL X-extensions for
 * copy-paste portability.</p>
 *
 * <p>Think of this class as the Rebel base authentication console that every
 * pilot must clear before launching — it knows every credential format the
 * Alliance accepts.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AuthenticationParameterPage extends AbstractConnectionParameterPage
{
    // ── X-LDAP-URL EXTENSION CONSTANTS ────────────────────────────────────────────

    /** X-LDAP-URL extension key for the authentication method. */
    private static final String X_AUTH_METHOD = "X-AUTH-METHOD"; //$NON-NLS-1$

    /** X_AUTH_METHOD value for anonymous binding. */
    private static final String X_AUTH_METHOD_ANONYMOUS = "Anonymous"; //$NON-NLS-1$

    /** X_AUTH_METHOD value for simple binding. */
    private static final String X_AUTH_METHOD_SIMPLE = "Simple"; //$NON-NLS-1$

    /** X_AUTH_METHOD value for SASL DIGEST-MD5. */
    private static final String X_AUTH_METHOD_DIGEST_MD5 = "DIGEST-MD5"; //$NON-NLS-1$

    /** X_AUTH_METHOD value for SASL CRAM-MD5. */
    private static final String X_AUTH_METHOD_CRAM_MD5 = "CRAM-MD5"; //$NON-NLS-1$

    /** X_AUTH_METHOD value for SASL GSSAPI. */
    private static final String X_AUTH_METHOD_GSSAPI = "GSSAPI"; //$NON-NLS-1$

    /** X-LDAP-URL extension key for the bind principal (DN or user name). */
    private static final String X_BIND_USER = "X-BIND-USER"; //$NON-NLS-1$

    /** X-LDAP-URL extension key for the bind password. */
    private static final String X_BIND_PASSWORD = "X-BIND-PASSWORD"; //$NON-NLS-1$

    /** X-LDAP-URL extension key for the SASL realm. */
    private static final String X_SASL_REALM = "X-SASL-REALM"; //$NON-NLS-1$

    /** X-LDAP-URL extension key for the SASL quality-of-protection level. */
    private static final String X_SASL_QOP = "X-SASL-QOP"; //$NON-NLS-1$

    /** X_SASL_QOP value for auth-int. */
    private static final String X_SASL_QOP_AUTH_INT = "AUTH-INT"; //$NON-NLS-1$

    /** X_SASL_QOP value for auth-int-priv (auth-conf in the API). */
    private static final String X_SASL_QOP_AUTH_INT_PRIV = "AUTH-INT-PRIV"; //$NON-NLS-1$

    /** X-LDAP-URL extension key for SASL security strength. */
    private static final String X_SASL_SEC_STRENGTH = "X-SASL-SEC-STRENGTH"; //$NON-NLS-1$

    /** X_SASL_SEC_STRENGTH value for medium. */
    private static final String X_SASL_SEC_STRENGTH_MEDIUM = "MEDIUM"; //$NON-NLS-1$

    /** X_SASL_SEC_STRENGTH value for low. */
    private static final String X_SASL_SEC_STRENGTH_LOW = "LOW"; //$NON-NLS-1$

    /** X-LDAP-URL extension key: present when mutual authentication is disabled. */
    private static final String X_SASL_NO_MUTUAL_AUTH = "X-SASL-NO-MUTUAL-AUTH"; //$NON-NLS-1$

    /** X-LDAP-URL extension key for KRB5 credential configuration. */
    private static final String X_KRB5_CREDENTIALS_CONF = "X-KRB5-CREDENTIALS-CONF"; //$NON-NLS-1$

    /** X_KRB5_CREDENTIALS_CONF value for "obtain TGT". */
    private static final String X_KRB5_CREDENTIALS_CONF_OBTAIN_TGT = "OBTAIN-TGT"; //$NON-NLS-1$

    /** X-LDAP-URL extension key for KRB5 configuration type. */
    private static final String X_KRB5_CONFIG = "X-KRB5-CONFIG"; //$NON-NLS-1$

    /** X_KRB5_CONFIG value for "file". */
    private static final String X_KRB5_CONFIG_FILE = "FILE"; //$NON-NLS-1$

    /** X-LDAP-URL extension key for the KRB5 config file path. */
    private static final String X_KRB5_CONFIG_FILE_FILE = "X-KRB5-CONFIG-FILE"; //$NON-NLS-1$

    /** X_KRB5_CONFIG value for "manual". */
    private static final String X_KRB5_CONFIG_MANUAL = "MANUAL"; //$NON-NLS-1$

    /** X-LDAP-URL extension key for the manual KRB5 realm. */
    private static final String X_KRB5_CONFIG_MANUAL_REALM = "X-KRB5-REALM"; //$NON-NLS-1$

    /** X-LDAP-URL extension key for the manual KDC hostname. */
    private static final String X_KRB5_CONFIG_MANUAL_KDC_HOST = "X-KRB5-KDC-HOST"; //$NON-NLS-1$

    /** X-LDAP-URL extension key for the manual KDC port. */
    private static final String X_KRB5_CONFIG_MANUAL_KDC_PORT = "X-KRB5-KDC-PORT"; //$NON-NLS-1$


    // ── UI FIELDS ─────────────────────────────────────────────────────────────────

    /** Read-only combo for the authentication method. */
    private Combo authenticationMethodCombo;

    /** Editable combo with bind DN / user history. */
    private Combo bindPrincipalCombo;

    /** Password text field (masked). */
    private Text bindPasswordText;

    /** Checkbox: persist the bind password on disk (or in the keystore). */
    private Button saveBindPasswordButton;

    /** Button: run a test bind to verify credentials. */
    private Button checkPrincipalPasswordAuthButton;

    // SASL-specific widgets (inside the SASL ExpandableComposite)
    /** The SASL section composite (direct parent of all SASL controls). */
    private Composite saslComposite;
    /** Editable combo with SASL realm history. */
    private Combo saslRealmText;
    /** Read-only combo: auth / auth-int / auth-int-priv. */
    private Combo saslQopCombo;
    /** Read-only combo: high / medium / low. */
    private Combo saslSecurityStrengthCombo;
    /** Checkbox: enable mutual authentication. */
    private Button saslMutualAuthenticationButton;

    // KRB5-specific widgets (inside the KRB5 ExpandableComposite)
    /** The KRB5 section composite. */
    private Composite krb5Composite;
    /** Radio: use native Kerberos credentials. */
    private Button krb5CredentialConfigurationUseNativeButton;
    /** Radio: obtain a new TGT (prompts for bind DN + password). */
    private Button krb5CredentialConfigurationObtainTgtButton;
    /** Radio: use the OS default KRB5 config file. */
    private Button krb5ConfigDefaultButton;
    /** Radio: use a custom KRB5 config file. */
    private Button krb5ConfigFileButton;
    /** Text field for the custom config file path. */
    private Text krb5ConfigFileText;
    /** Radio: enter KDC details manually. */
    private Button krb5ConfigManualButton;
    /** Text field for the manual KRB5 realm. */
    private Text krb5ConfigManualRealmText;
    /** Text field for the manual KDC hostname. */
    private Text krb5ConfigManualHostText;
    /** Text field for the manual KDC port (digits only, default 88). */
    private Text krb5ConfigManualPortText;


    // ── PRIVATE GETTERS ───────────────────────────────────────────────────────────

    // ── Reading The Pilot's Auth Badge ──────────────────────────────────────────────
    // The base computer scans the pilot's clearance badge at the hangar door.
    // Each badge type maps to a different clearance level — anonymous walk-in,
    // simple password, SASL challenge, or full Kerberos ticket.
    // We translate the combo's integer index back into the typed enum constant.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the authentication method selected in the combo.
     *
     * <p>Combo index mapping: 0=NONE, 1=SIMPLE, 2=SASL_DIGEST_MD5,
     * 3=SASL_CRAM_MD5, 4=SASL_GSSAPI.</p>
     *
     * <p>For example — the base computer reads Han's badge and confirms he is
     * cleared for "Simple" access (he gave a bind DN and password):</p>
     * <pre>
     *   combo index = 1  →  AuthenticationMethod.SIMPLE
     *   Han: "It's me, Han Solo.  Alliance clearance 7731."
     *   Base: "SIMPLE method confirmed.  Proceed to bay 94."
     * </pre>
     *
     * @return The selected {@link ConnectionParameter.AuthenticationMethod}.
     */
    private ConnectionParameter.AuthenticationMethod getAuthenticationMethod()
    {
        switch ( authenticationMethodCombo.getSelectionIndex() )
        {
            case 1:
                return ConnectionParameter.AuthenticationMethod.SIMPLE;

            case 2:
                return ConnectionParameter.AuthenticationMethod.SASL_DIGEST_MD5;

            case 3:
                return ConnectionParameter.AuthenticationMethod.SASL_CRAM_MD5;

            case 4:
                return ConnectionParameter.AuthenticationMethod.SASL_GSSAPI;

            default:
                return ConnectionParameter.AuthenticationMethod.NONE;
        }
    }


    // ── Reading The Pilot's Call Sign ────────────────────────────────────────────────
    // The control tower asks every pilot for their call sign before launch clearance.
    // The pilot types it into the console's "Bind DN / User" field.
    // We just retrieve whatever they typed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the bind principal (DN or user name) from the combo.
     *
     * <p>For example — Leia types her distinguished name into the console:</p>
     * <pre>
     *   "cn=Leia Organa,ou=Rebellion,dc=alderaan,dc=org"
     *   → returned as-is to be used as the LDAP bind DN.
     * </pre>
     *
     * @return The bind principal string.
     */
    private String getBindPrincipal()
    {
        return bindPrincipalCombo.getText();
    }


    // ── Retrieving The Encrypted Passphrase ──────────────────────────────────────────
    // Every Rebel pilot who wants to prove their identity must also supply a passphrase.
    // But if the pilot didn't ask us to save their passphrase, we treat it as null —
    // Han would never write his password on a sticky note in the cockpit anyway.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the bind password, or {@code null} if "Save Password" is unchecked.
     *
     * <p>We only return the password if the user explicitly opted to save it.
     * Otherwise we return {@code null} so nothing leaks into persistence.</p>
     *
     * <p>For example — Han checks the "Save password" box, so we hand his
     * passphrase to the connection parameter bean:</p>
     * <pre>
     *   saveBindPasswordButton.isSelected() = true
     *   bindPasswordText.getText()          = "nerfherder77"
     *   → returns "nerfherder77"
     * </pre>
     *
     * @return The bind password, or {@code null}.
     */
    private String getBindPassword()
    {
        return isSaveBindPassword() ? bindPasswordText.getText() : null;
    }


    // ── Reading The SASL Realm Identifier ────────────────────────────────────────────
    // In DIGEST-MD5, the server and client agree on a shared realm — like the sector
    // code of the Rebel base.  If the pilot left the realm field blank, there is no
    // realm constraint and we return null.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SASL realm, or {@code null} if the field is empty.
     *
     * <p>SASL DIGEST-MD5 uses a realm string to namespace the credentials.
     * An empty realm is treated as absent (null), not as an empty-string realm.</p>
     *
     * <p>For example — the Alliance's SASL realm is "REBEL.BASE":</p>
     * <pre>
     *   saslRealmText.getText() = "REBEL.BASE"  → returns "REBEL.BASE"
     *   saslRealmText.getText() = ""             → returns null
     * </pre>
     *
     * @return The SASL realm string, or {@code null}.
     */
    private String getSaslRealm()
    {
        return Strings.isEmpty( saslRealmText.getText() ) ? null : saslRealmText.getText();
    }


    // ── Reading The Quality-Of-Protection Level ───────────────────────────────────────
    // The Alliance's comm link has three security grades: authentication only,
    // authentication + integrity, or full encryption (auth-conf).  The pilot selects
    // one from the combo, and we translate combo index to the enum.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SASL quality-of-protection level selected in the combo.
     *
     * <p>Index 0=AUTH (authentication only), 1=AUTH_INT (add integrity),
     * 2=AUTH_CONF (add confidentiality/encryption).</p>
     *
     * <p>For example — Cassian selects AUTH_INT so his transmissions can't be
     * tampered with in flight:</p>
     * <pre>
     *   combo index 1  →  SaslQoP.AUTH_INT
     *   "All rebel transmissions signed.  Tampering will be detected."
     * </pre>
     *
     * @return The selected {@link SaslQoP}.
     */
    private SaslQoP getSaslQop()
    {
        switch ( saslQopCombo.getSelectionIndex() )
        {
            case 1:
                return SaslQoP.AUTH_INT;

            case 2:
                return SaslQoP.AUTH_CONF;

            default:
                return SaslQoP.AUTH;
        }
    }


    // ── Reading The Security Strength Tier ───────────────────────────────────────────
    // The Alliance grades every comm channel HIGH, MEDIUM, or LOW depending on how
    // strong the encryption cipher must be.  HIGH is the default — we don't let
    // pilots connect on a weak channel without explicitly opting down.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SASL security-strength level selected in the combo.
     *
     * <p>Index 0=HIGH (strong cipher required), 1=MEDIUM, 2=LOW (weak cipher
     * permitted).  HIGH is the default.</p>
     *
     * <p>For example — the Rebel base refuses LOW-grade ciphers for sensitive
     * data:</p>
     * <pre>
     *   combo index 0  →  SaslSecurityStrength.HIGH
     *   "Only AES-256 accepted.  RC4 rejected."
     * </pre>
     *
     * @return The selected {@link SaslSecurityStrength}.
     */
    private SaslSecurityStrength getSaslSecurityStrength()
    {
        switch ( saslSecurityStrengthCombo.getSelectionIndex() )
        {
            case 1:
                return SaslSecurityStrength.MEDIUM;

            case 2:
                return SaslSecurityStrength.LOW;

            default:
                return SaslSecurityStrength.HIGH;
        }
    }


    // ── Checking The Kerberos Credential Source ───────────────────────────────────────
    // The Rebel Alliance can use the OS's cached Kerberos ticket (USE_NATIVE — trust
    // the platform) or request that the user type in a DN + password so we obtain a
    // fresh TGT ourselves (OBTAIN_TGT).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the KRB5 credential-provider selection.
     *
     * <p>USE_NATIVE delegates to the operating system's Kerberos ticket cache.
     * OBTAIN_TGT prompts the user for a bind DN + password and requests a ticket
     * from the KDC ourselves.</p>
     *
     * <p>For example — Luke picks USE_NATIVE because his X-Wing's onboard
     * computer already holds a valid TGT from the base:</p>
     * <pre>
     *   krb5CredentialConfigurationUseNativeButton.isSelected() = true
     *   → Krb5CredentialConfiguration.USE_NATIVE
     * </pre>
     *
     * @return {@link Krb5CredentialConfiguration#USE_NATIVE} or
     *         {@link Krb5CredentialConfiguration#OBTAIN_TGT}.
     */
    private Krb5CredentialConfiguration getKrb5CredentialProvider()
    {
        if ( krb5CredentialConfigurationUseNativeButton.getSelection() )
        {
            return Krb5CredentialConfiguration.USE_NATIVE;
        }
        else
        {
            return Krb5CredentialConfiguration.OBTAIN_TGT;
        }
    }


    // ── Reading The Kerberos Config Source ───────────────────────────────────────────
    // The KDC (Key Distribution Center) address can come from the OS default krb5.conf,
    // a custom config file the pilot points to, or manual realm/host/port fields they
    // fill in themselves.  We read which radio button is selected.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the KRB5 configuration type selected by the radio buttons.
     *
     * <p>DEFAULT uses the OS krb5.conf.  FILE uses a path the user provides.
     * MANUAL uses the realm, KDC host, and port fields on the page.</p>
     *
     * <p>For example — on a Rebel ship without a system krb5.conf, the navigator
     * selects MANUAL and types in the KDC address by hand:</p>
     * <pre>
     *   krb5ConfigManualButton.isSelected() = true
     *   → Krb5Configuration.MANUAL
     * </pre>
     *
     * @return {@link Krb5Configuration#DEFAULT}, {@link Krb5Configuration#FILE},
     *         or {@link Krb5Configuration#MANUAL}.
     */
    private Krb5Configuration getKrb5Configuration()
    {
        if ( krb5ConfigDefaultButton.getSelection() )
        {
            return Krb5Configuration.DEFAULT;
        }
        else if ( krb5ConfigFileButton.getSelection() )
        {
            return Krb5Configuration.FILE;
        }
        else
        {
            return Krb5Configuration.MANUAL;
        }
    }


    // ── Reading The KDC Port Number ──────────────────────────────────────────────────
    // The KDC listens on port 88 by default, but on a custom Rebel installation it
    // might be different.  We parse the text field as an int; an empty field means
    // port 0 (caller treats 0 as "use default").
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the KDC port as an integer, or {@code 0} if the field is empty.
     *
     * <p>The port text field only accepts digits (enforced by a VerifyListener),
     * so Integer.parseInt() will not throw here.  An empty field means "not
     * specified" and is returned as 0.</p>
     *
     * <p>For example — the Rebel base KDC runs on port 1088 instead of 88:</p>
     * <pre>
     *   krb5ConfigManualPortText.getText() = "1088"
     *   → returns 1088
     * </pre>
     *
     * @return The KDC port number, or {@code 0} if the field is empty.
     */
    private int getKdcPort()
    {
        String krb5ConfigPort = krb5ConfigManualPortText.getText();

        if ( Strings.isEmpty( krb5ConfigPort ) )
        {
            return 0;
        }
        else
        {
            return Integer.parseInt( krb5ConfigPort );
        }
    }


    // ── Checking The Save-Password Switch ────────────────────────────────────────────
    // Pilots can choose whether the base stores their passphrase.  If they do, the
    // password persists across sessions (in the keystore or in the parameter bean).
    // If not, they re-type it every launch.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the "Save Password" checkbox is checked.
     *
     * <p>When true, the bind password will be saved to disk (either the JKS
     * keystore or the ConnectionParameter bean, depending on preferences).
     * When false, the connection always prompts for a password.</p>
     *
     * <p>For example — Han checks "Save password" so the Falcon doesn't make him
     * re-authenticate every jump:</p>
     * <pre>
     *   saveBindPasswordButton.getSelection() = true
     *   → bind password will be persisted.
     * </pre>
     *
     * @return {@code true} if the bind password should be saved.
     */
    public boolean isSaveBindPassword()
    {
        return saveBindPasswordButton.getSelection();
    }


    // ── Assembling A Test-Launch Connection ───────────────────────────────────────────
    // Before fully committing to launch, the pilot can fire off a short test ping
    // to the server.  We assemble a temporary Connection using the current page
    // values (collected from ALL parameter pages via the listener, not just this one).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds a temporary {@link Connection} from all current page parameters for
     * use by the "Check Authentication" button.
     *
     * <p>Delegates to {@link #connectionParameterPageModifyListener} to collect
     * parameters from all pages (not just this one), so the test connection
     * reflects the hostname and port set on the Network Parameter page.</p>
     *
     * <p>For example — Luke clicks "Check Authentication" to confirm his
     * credentials before the real mission:</p>
     * <pre>
     *   connectionParameterPageModifyListener.getTestConnectionParameters()
     *   → ConnectionParameter{ host="yavin4.rebel.org", port=389,
     *       authMethod=SIMPLE, bindPrincipal="cn=Luke Skywalker,..." }
     *   → new Connection( parameter )  // disposable test connection
     * </pre>
     *
     * @return A disposable {@link Connection} for testing.
     */
    private Connection getTestConnection()
    {
        ConnectionParameter connectionParameter = connectionParameterPageModifyListener.getTestConnectionParameters();

        return new Connection( connectionParameter );
    }


    // ── CREATE COMPOSITE ──────────────────────────────────────────────────────────

    // ── Building The Authentication Console Panel ─────────────────────────────────────
    // The authentication console has three distinct areas, built from top to bottom:
    // (1) the method selector, (2) the bind-DN/password group, and (3) a scrolled
    // area with collapsible SASL and KRB5 sections.  SWT GridLayout manages the
    // vertical stack.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the page UI.
     *
     * <p>Creates three sections stacked vertically in {@code parent}:</p>
     * <ol>
     *   <li>An "Authentication Method" group with a single read-only combo.</li>
     *   <li>An "Authentication Parameter" group with bind DN combo, bind password
     *       text, "Save Password" checkbox, and "Check Authentication" button.</li>
     *   <li>A {@link ScrolledComposite} containing two {@link ExpandableComposite}
     *       sections: "SASL Options" and "KRB5 Options".</li>
     * </ol>
     *
     * <p>For example — the base tech builds each station on the authentication
     * console before the pilot sits down:</p>
     * <pre>
     *   Station 1: auth method combo  ("Anonymous / Simple / DIGEST-MD5 / ...")
     *   Station 2: bind DN + password fields + test button
     *   Station 3: scrollable SASL + KRB5 expandable panels
     * </pre>
     *
     * @param parent The parent composite to create the page content inside.
     */
    protected void createComposite( Composite parent )
    {
        // ── AUTHENTICATION METHOD GROUP ───────────────────────────────────────────
        Composite composite1 = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        Group group1 = BaseWidgetUtils.createGroup( composite1, Messages
            .getString( "AuthenticationParameterPage.AuthenticationMethod" ), 1 ); //$NON-NLS-1$
        Composite groupComposite = BaseWidgetUtils.createColumnContainer( group1, 1, 1 );

        String[] authMethods = new String[]
            {
                Messages.getString( "AuthenticationParameterPage.AnonymousAuthentication" ), //$NON-NLS-1$
                Messages.getString( "AuthenticationParameterPage.SimpleAuthentication" ), //$NON-NLS-1$
                Messages.getString( "AuthenticationParameterPage.DigestMD5" ), //$NON-NLS-1$
                Messages.getString( "AuthenticationParameterPage.CramMD5" ), //$NON-NLS-1$
                Messages.getString( "AuthenticationParameterPage.GSSAPI" ) //$NON-NLS-1$
            };

        authenticationMethodCombo = BaseWidgetUtils.createReadonlyCombo( groupComposite, authMethods, 1, 2 );

        // ── AUTHENTICATION PARAMETER GROUP ────────────────────────────────────────
        Composite composite2 = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        Group group2 = BaseWidgetUtils.createGroup( composite2, Messages
            .getString( "AuthenticationParameterPage.AuthenticationParameter" ), 1 ); //$NON-NLS-1$
        Composite composite = BaseWidgetUtils.createColumnContainer( group2, 3, 1 );

        BaseWidgetUtils.createLabel( composite, Messages.getString( "AuthenticationParameterPage.BindDNOrUser" ), 1 ); //$NON-NLS-1$
        String[] dnHistory = HistoryUtils.load( ConnectionUIPlugin.getDefault().getDialogSettings(),
            ConnectionUIConstants.DIALOGSETTING_KEY_PRINCIPAL_HISTORY );
        bindPrincipalCombo = BaseWidgetUtils.createCombo( composite, dnHistory, -1, 2 );

        BaseWidgetUtils.createLabel( composite, Messages.getString( "AuthenticationParameterPage.BindPassword" ), 1 ); //$NON-NLS-1$
        bindPasswordText = BaseWidgetUtils.createPasswordText( composite, StringUtils.EMPTY, 2 ); //$NON-NLS-1$

        BaseWidgetUtils.createSpacer( composite, 1 );
        saveBindPasswordButton = BaseWidgetUtils.createCheckbox( composite, Messages
            .getString( "AuthenticationParameterPage.SavePassword" ), 1 ); //$NON-NLS-1$
        saveBindPasswordButton.setSelection( true );

        checkPrincipalPasswordAuthButton = new Button( composite, SWT.PUSH );
        GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
        gridData.horizontalAlignment = SWT.RIGHT;
        checkPrincipalPasswordAuthButton.setLayoutData( gridData );
        checkPrincipalPasswordAuthButton.setText( Messages
            .getString( "AuthenticationParameterPage.CheckAuthentication" ) ); //$NON-NLS-1$
        checkPrincipalPasswordAuthButton.setEnabled( false );

        // ── SCROLLED COMPOSITE WITH EXPANDABLE SECTIONS ───────────────────────────
        ScrolledComposite scrolledComposite = new ScrolledComposite( parent, SWT.H_SCROLL | SWT.V_SCROLL );
        scrolledComposite.setLayout( new GridLayout() );
        scrolledComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        Composite contentComposite = BaseWidgetUtils.createColumnContainer( scrolledComposite, 1, 1 );
        scrolledComposite.setContent( contentComposite );

        // ── SASL OPTIONS SECTION ──────────────────────────────────────────────────
        ExpandableComposite saslExpandableComposite = createExpandableSection( contentComposite, Messages
            .getString( "AuthenticationParameterPage.SaslOptions" ), 1 ); //$NON-NLS-1$
        saslComposite = BaseWidgetUtils.createColumnContainer( saslExpandableComposite, 2, 1 );
        saslExpandableComposite.setClient( saslComposite );
        createSaslControls();

        // ── KRB5 OPTIONS SECTION ──────────────────────────────────────────────────
        ExpandableComposite krb5ExpandableComposite = createExpandableSection( contentComposite, Messages
            .getString( "AuthenticationParameterPage.Krb5Options" ), 1 ); //$NON-NLS-1$
        krb5Composite = BaseWidgetUtils.createColumnContainer( krb5ExpandableComposite, 1, 1 );
        krb5ExpandableComposite.setClient( krb5Composite );
        createKrb5Controls();

        contentComposite.setSize( contentComposite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
    }


    // ── CREATE EXPANDABLE SECTION ─────────────────────────────────────────────────

    // ── Installing A Collapsible Briefing Panel ───────────────────────────────────────
    // The Rebel briefing room has retractable holographic panels.  When you flick the
    // TWISTIE toggle, the panel expands to show mission details or collapses to save
    // screen space.  Each expansion/collapse event recalculates the scroll bar so
    // nothing gets clipped off the bottom.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a collapsible {@link ExpandableComposite} section inside the
     * scrolled area.
     *
     * <p>The section starts collapsed and uses a TWISTIE disclosure widget.
     * An {@link ExpansionAdapter} recomputes the scrolled composite's content
     * size whenever the section expands or collapses so the scroll bar tracks
     * correctly.</p>
     *
     * <p>For example — Mon Mothma expands the "SASL Options" briefing panel to
     * reveal mission-critical cipher settings:</p>
     * <pre>
     *   user clicks TWISTIE ▶
     *   expansionStateChanged() fires
     *   → contentComposite.setSize( computeSize(...) )
     *   → scroll bar updated to show the newly revealed controls
     * </pre>
     *
     * @param parent   The parent composite to create the section inside.
     * @param label    The section header label text.
     * @param nColumns The number of grid columns for the section's content.
     * @return The newly created {@link ExpandableComposite}.
     */
    protected ExpandableComposite createExpandableSection( Composite parent, String label, int nColumns )
    {
        ExpandableComposite excomposite = new ExpandableComposite( parent, SWT.NONE, ExpandableComposite.TWISTIE
            | ExpandableComposite.CLIENT_INDENT );
        excomposite.setText( label );
        excomposite.setExpanded( false );
        excomposite.setFont( JFaceResources.getFontRegistry().getBold( JFaceResources.DIALOG_FONT ) );
        excomposite.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false, nColumns, 1 ) );
        excomposite.addExpansionListener( new ExpansionAdapter()
        {
            /**
             * {@inheritDoc}
             *
             * Recomputes the scrolled composite's content size when the section
             * expands or collapses so the scroll bar is updated.
             */
            @Override
            public void expansionStateChanged( ExpansionEvent event )
            {
                ExpandableComposite excomposite = ( ExpandableComposite ) event.getSource();
                excomposite.getParent().setSize( excomposite.getParent().computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
            }
        } );

        return excomposite;
    }


    // ── CREATE SASL CONTROLS ──────────────────────────────────────────────────────

    // ── Wiring Up The SASL Negotiation Console ────────────────────────────────────────
    // The SASL section is a mini control panel for the challenge-response protocol.
    // Realm, quality-of-protection, security strength, and mutual-auth are the four
    // dials the operator adjusts before the handshake begins.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the SASL Options section with realm combo, QOP combo, security
     * strength combo, and mutual-authentication checkbox.
     *
     * <p>All widgets are added to {@link #saslComposite} which was created by
     * {@link #createComposite}.  This is only called once during UI construction.</p>
     *
     * <p>For example — Cassian wires up the four SASL dials on his comms
     * console before initiating a DIGEST-MD5 session:</p>
     * <pre>
     *   Realm:           "REBEL.BASE"
     *   QoP:             AUTH-INT  (signed messages)
     *   SecurityStrength: HIGH     (AES-256 only)
     *   MutualAuth:       ✓        (server must prove itself too)
     * </pre>
     */
    private void createSaslControls()
    {
        BaseWidgetUtils.createLabel( saslComposite, Messages.getString( "AuthenticationParameterPage.SaslRealm" ), 1 ); //$NON-NLS-1$
        String[] saslHistory = HistoryUtils.load( ConnectionUIPlugin.getDefault().getDialogSettings(),
            ConnectionUIConstants.DIALOGSETTING_KEY_REALM_HISTORY );
        saslRealmText = BaseWidgetUtils.createCombo( saslComposite, saslHistory, -1, 1 );

        BaseWidgetUtils.createLabel( saslComposite, Messages.getString( "AuthenticationParameterPage.SaslQop" ), 1 ); //$NON-NLS-1$

        String[] qops = new String[]
            {
                Messages.getString( "AuthenticationParameterPage.SaslQopAuth" ), //$NON-NLS-1$
                Messages.getString( "AuthenticationParameterPage.SaslQopAuthInt" ), //$NON-NLS-1$
                Messages.getString( "AuthenticationParameterPage.SaslQopAuthIntPriv" ) //$NON-NLS-1$
            };

        saslQopCombo = BaseWidgetUtils.createReadonlyCombo( saslComposite, qops, 0, 1 );

        BaseWidgetUtils.createLabel( saslComposite, Messages
            .getString( "AuthenticationParameterPage.SaslSecurityStrength" ), 1 ); //$NON-NLS-1$

        String[] securityStrengths = new String[]
            {
                Messages.getString( "AuthenticationParameterPage.SaslSecurityStrengthHigh" ), //$NON-NLS-1$
                Messages.getString( "AuthenticationParameterPage.SaslSecurityStrengthMedium" ), //$NON-NLS-1$
                Messages.getString( "AuthenticationParameterPage.SaslSecurityStrengthLow" ) //$NON-NLS-1$
            };

        saslSecurityStrengthCombo = BaseWidgetUtils.createReadonlyCombo( saslComposite, securityStrengths, 0, 1 );

        saslMutualAuthenticationButton = BaseWidgetUtils.createCheckbox( saslComposite, Messages
            .getString( "AuthenticationParameterPage.SaslMutualAuthentication" ), 2 ); //$NON-NLS-1$
    }


    // ── CREATE KRB5 CONTROLS ──────────────────────────────────────────────────────

    // ── Installing The Kerberos Ticket Station ────────────────────────────────────────
    // The Kerberos section has two sub-groups: how we get the ticket (native OS cache
    // or request a fresh TGT) and where the KDC lives (OS default, a config file, or
    // manual realm/host/port).  This builds all those radio buttons and text fields.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the KRB5 Options section with credential-provider radio buttons
     * and KRB5 configuration source radio buttons (default / file / manual).
     *
     * <p>All widgets are added to {@link #krb5Composite} which was created by
     * {@link #createComposite}.  This is only called once during UI construction.</p>
     *
     * <p>For example — Luke configures his X-Wing's Kerberos station to use the
     * Alliance's central KDC at Yavin 4 with a manually-entered address:</p>
     * <pre>
     *   Credential config: OBTAIN_TGT  (get a fresh ticket)
     *   KRB5 config:       MANUAL
     *     Realm:  REBELLION.ORG
     *     Host:   kdc.yavin4.rebel.org
     *     Port:   88
     * </pre>
     */
    private void createKrb5Controls()
    {
        Group credentialProviderGroup = BaseWidgetUtils.createGroup( krb5Composite, Messages
            .getString( "AuthenticationParameterPage.Krb5CredentialConf" ), 1 ); //$NON-NLS-1$
        Composite credentialProviderComposite = BaseWidgetUtils.createColumnContainer( credentialProviderGroup, 1, 1 );
        krb5CredentialConfigurationUseNativeButton = BaseWidgetUtils.createRadiobutton( credentialProviderComposite,
            Messages.getString( "AuthenticationParameterPage.Krb5CredentialConfUseNative" ), 1 ); //$NON-NLS-1$
        krb5CredentialConfigurationUseNativeButton.setToolTipText( Messages
            .getString( "AuthenticationParameterPage.Krb5CredentialConfUseNativeTooltip" ) ); //$NON-NLS-1$
        krb5CredentialConfigurationUseNativeButton.setSelection( true );
        krb5CredentialConfigurationObtainTgtButton = BaseWidgetUtils.createRadiobutton( credentialProviderComposite,
            Messages.getString( "AuthenticationParameterPage.Krb5CredentialConfObtainTgt" ), 1 ); //$NON-NLS-1$
        krb5CredentialConfigurationObtainTgtButton.setToolTipText( Messages
            .getString( "AuthenticationParameterPage.Krb5CredentialConfObtainTgtTooltip" ) ); //$NON-NLS-1$

        Group configGroup = BaseWidgetUtils.createGroup( krb5Composite, Messages
            .getString( "AuthenticationParameterPage.Krb5Config" ), 1 ); //$NON-NLS-1$
        Composite configComposite = BaseWidgetUtils.createColumnContainer( configGroup, 3, 1 );
        krb5ConfigDefaultButton = BaseWidgetUtils.createRadiobutton( configComposite, Messages
            .getString( "AuthenticationParameterPage.Krb5ConfigDefault" ), 3 ); //$NON-NLS-1$
        krb5ConfigDefaultButton.setSelection( true );
        krb5ConfigFileButton = BaseWidgetUtils.createRadiobutton( configComposite, Messages
            .getString( "AuthenticationParameterPage.Krb5ConfigFile" ), 1 ); //$NON-NLS-1$
        krb5ConfigFileText = BaseWidgetUtils.createText( configComposite, StringUtils.EMPTY, 2 ); //$NON-NLS-1$
        krb5ConfigManualButton = BaseWidgetUtils.createRadiobutton( configComposite, Messages
            .getString( "AuthenticationParameterPage.Krb5ConfigManual" ), 1 ); //$NON-NLS-1$
        BaseWidgetUtils.createLabel( configComposite, Messages.getString( "AuthenticationParameterPage.Krb5Realm" ), //$NON-NLS-1$
            1 );
        krb5ConfigManualRealmText = BaseWidgetUtils.createText( configComposite, StringUtils.EMPTY, 1 ); //$NON-NLS-1$
        BaseWidgetUtils.createSpacer( configComposite, 1 );
        BaseWidgetUtils.createLabel( configComposite,
            Messages.getString( "AuthenticationParameterPage.Krb5KdcHost" ), 1 ); //$NON-NLS-1$
        krb5ConfigManualHostText = BaseWidgetUtils.createText( configComposite, StringUtils.EMPTY, 1 ); //$NON-NLS-1$
        BaseWidgetUtils.createSpacer( configComposite, 1 );
        BaseWidgetUtils.createLabel( configComposite,
            Messages.getString( "AuthenticationParameterPage.Krb5KdcPort" ), 1 ); //$NON-NLS-1$
        krb5ConfigManualPortText = BaseWidgetUtils.createText( configComposite, "88", 1 ); //$NON-NLS-1$
        krb5ConfigManualPortText.setTextLimit( 5 );
    }


    // ── VALIDATE ──────────────────────────────────────────────────────────────────

    // ── Running Pre-Launch Security Checks ───────────────────────────────────────────
    // Before the base clears the pilot for launch, the authentication console runs a
    // full check: which controls should be enabled for the chosen auth method, are
    // required fields filled in, and what message (if any) to show in the wizard chrome.
    // Think of it as the launch checklist — everything must be green before the
    // blast doors open.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Enables/disables all SASL and KRB5 controls based on the selected
     * authentication method and the global "Use KRB5 system properties" preference.
     * Then validates required fields and sets the wizard's message/infoMessage/errorMessage.
     *
     * <p>Called every time any field changes via {@link #connectionPageModified()}.
     * The wizard's Next/Finish button is gated on {@code errorMessage} being null.</p>
     *
     * <p>For example — when the pilot switches to GSSAPI, the bind-DN field
     * enables only if OBTAIN_TGT is selected, the SASL controls all light up,
     * and the KRB5 controls are checked against the global KRB5 preference:</p>
     * <pre>
     *   authMethod = SASL_GSSAPI, krb5ObtainTgt = false, useKrb5SysProp = false
     *   → bindPrincipalCombo.enabled = false
     *   → krb5Controls.enabled = true
     *   → message = null  (no required field missing)
     * </pre>
     */
    protected void validate()
    {
        // ── SASL CONTROLS: ENABLE/DISABLE ─────────────────────────────────────────
        if ( saslComposite != null )
        {
            for ( Control c : saslComposite.getChildren() )
            {
                c.setEnabled( isSaslEnabled() );
            }
            saslRealmText.setEnabled( isSaslRealmTextEnabled() );
        }

        // ── KRB5 CONTROLS: ENABLE/DISABLE ─────────────────────────────────────────
        // TODO: get setting from global preferences.
        Preferences preferences = ConnectionCorePlugin.getDefault().getPluginPreferences();
        boolean useKrb5SystemProperties = preferences
            .getBoolean( ConnectionCoreConstants.PREFERENCE_USE_KRB5_SYSTEM_PROPERTIES );

        if ( krb5Composite != null )
        {
            krb5CredentialConfigurationUseNativeButton.setEnabled( isGssapiEnabled() && !useKrb5SystemProperties );
            krb5CredentialConfigurationObtainTgtButton.setEnabled( isGssapiEnabled() && !useKrb5SystemProperties );

            krb5ConfigDefaultButton.setEnabled( isGssapiEnabled() && !useKrb5SystemProperties );
            krb5ConfigFileButton.setEnabled( isGssapiEnabled() && !useKrb5SystemProperties );
            krb5ConfigManualButton.setEnabled( isGssapiEnabled() && !useKrb5SystemProperties );

            krb5ConfigFileText.setEnabled( isGssapiEnabled() && krb5ConfigFileButton.getSelection()
                && !useKrb5SystemProperties );
            krb5ConfigManualRealmText.setEnabled( isGssapiEnabled() && krb5ConfigManualButton.getSelection()
                && !useKrb5SystemProperties );
            krb5ConfigManualHostText.setEnabled( isGssapiEnabled() && krb5ConfigManualButton.getSelection()
                && !useKrb5SystemProperties );
            krb5ConfigManualPortText.setEnabled( isGssapiEnabled() && krb5ConfigManualButton.getSelection()
                && !useKrb5SystemProperties );
        }

        // ── PRINCIPAL/PASSWORD CONTROLS: ENABLE/DISABLE ───────────────────────────
        bindPrincipalCombo.setEnabled( isPrincipalPasswordEnabled() );
        bindPasswordText.setEnabled( isPrincipalPasswordEnabled() && isSaveBindPassword() );
        saveBindPasswordButton.setEnabled( isPrincipalPasswordEnabled() );
        checkPrincipalPasswordAuthButton
            .setEnabled( ( isPrincipalPasswordEnabled() && isSaveBindPassword()
                && !bindPrincipalCombo.getText().equals( StringUtils.EMPTY )
                && !bindPasswordText.getText().equals( StringUtils.EMPTY ) ) || isGssapiEnabled() ); //$NON-NLS-1$ //$NON-NLS-2$

        // ── VALIDATION MESSAGES ───────────────────────────────────────────────────
        message = null;
        infoMessage = null;
        errorMessage = null;

        if ( isPrincipalPasswordEnabled() )
        {
            if ( isSaveBindPassword() && Strings.isEmpty( bindPasswordText.getText() ) ) //$NON-NLS-1$
            {
                message = Messages.getString( "AuthenticationParameterPage.PleaseEnterBindPassword" ); //$NON-NLS-1$
            }

            if ( Strings.isEmpty( bindPrincipalCombo.getText() ) && !isGssapiEnabled() ) //$NON-NLS-1$
            {
                message = Messages.getString( "AuthenticationParameterPage.PleaseEnterBindDNOrUser" ); //$NON-NLS-1$
            }
        }

        if ( isSaslRealmTextEnabled() && Strings.isEmpty( saslRealmText.getText() ) ) //$NON-NLS-1$
        {
            infoMessage = Messages.getString( "AuthenticationParameterPage.PleaseEnterSaslRealm" ); //$NON-NLS-1$
        }

        if ( isGssapiEnabled() && krb5ConfigFileButton.getSelection()
            && Strings.isEmpty( krb5ConfigFileText.getText() ) ) //$NON-NLS-1$
        {
            message = Messages.getString( "AuthenticationParameterPage.PleaseEnterKrb5ConfigFile" ); //$NON-NLS-1$
        }

        if ( isGssapiEnabled() && krb5ConfigManualButton.getSelection() )
        {
            if ( Strings.isEmpty( krb5ConfigManualPortText.getText() ) ) //$NON-NLS-1$
            {
                message = Messages.getString( "AuthenticationParameterPage.PleaseEnterKrb5Port" ); //$NON-NLS-1$
            }

            if ( Strings.isEmpty( krb5ConfigManualHostText.getText() ) ) //$NON-NLS-1$
            {
                message = Messages.getString( "AuthenticationParameterPage.PleaseEnterKrb5Host" ); //$NON-NLS-1$
            }

            if ( Strings.isEmpty( krb5ConfigManualRealmText.getText() ) ) //$NON-NLS-1$
            {
                message = Messages.getString( "AuthenticationParameterPage.PleaseEnterKrb5Realm" ); //$NON-NLS-1$
            }
        }
    }


    // ── IS PRINCIPAL PASSWORD ENABLED ─────────────────────────────────────────────

    // ── Deciding If The Pilot Needs A Passphrase ─────────────────────────────────────
    // Anonymous pilots don't need a passphrase.  GSSAPI pilots using the native
    // Kerberos cache don't need one either.  Everyone else does — Simple, DIGEST-MD5,
    // CRAM-MD5, and GSSAPI-with-TGT-request all require a bind DN + password.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when the bind DN / password fields should be enabled.
     *
     * <p>They are enabled for Simple, DIGEST-MD5, CRAM-MD5, and GSSAPI when the
     * user chose to obtain a new TGT (which requires credentials).</p>
     *
     * <p>For example — Han (Simple auth) always needs a password; R2-D2
     * (GSSAPI + native TGT) does not:</p>
     * <pre>
     *   Han:  authMethod=SIMPLE                          → true  (show fields)
     *   R2:   authMethod=SASL_GSSAPI, useNative=true     → false (hide fields)
     *   Luke: authMethod=SASL_GSSAPI, obtainTGT=true     → true  (show fields)
     * </pre>
     *
     * @return {@code true} if the bind principal and password fields are active.
     */
    private boolean isPrincipalPasswordEnabled()
    {
        return ( getAuthenticationMethod() == AuthenticationMethod.SIMPLE )
            || ( getAuthenticationMethod() == AuthenticationMethod.SASL_DIGEST_MD5 )
            || ( getAuthenticationMethod() == AuthenticationMethod.SASL_CRAM_MD5 )
            || ( getAuthenticationMethod() == AuthenticationMethod.SASL_GSSAPI
                && krb5CredentialConfigurationObtainTgtButton
                    .getSelection() );
    }


    // ── Checking If SASL Realm Field Is Active ────────────────────────────────────────
    // Only DIGEST-MD5 uses a separate realm identifier.  CRAM-MD5 and GSSAPI don't
    // need one.  We enable that field exactly when the method is DIGEST-MD5.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when the SASL realm field should be enabled.
     *
     * <p>Only DIGEST-MD5 uses a separate realm field.  For all other methods
     * the realm input is hidden or disabled.</p>
     *
     * <p>For example — Cassian selects DIGEST-MD5, so the realm field unlocks:</p>
     * <pre>
     *   authMethod = SASL_DIGEST_MD5  → true  (realm field enabled)
     *   authMethod = SASL_CRAM_MD5    → false (realm field disabled)
     * </pre>
     *
     * @return {@code true} if the SASL realm text field should be editable.
     */
    private boolean isSaslRealmTextEnabled()
    {
        return getAuthenticationMethod() == AuthenticationMethod.SASL_DIGEST_MD5;
    }


    // ── Checking If Any SASL Mechanism Is Active ──────────────────────────────────────
    // SASL options (QOP, security strength, mutual auth) only make sense when a SASL
    // mechanism is selected.  We enable the whole SASL section for DIGEST-MD5,
    // CRAM-MD5, and GSSAPI, and disable it for Anonymous and Simple.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when any SASL mechanism is selected.
     *
     * <p>Used to enable or disable the entire SASL Options expandable section.</p>
     *
     * <p>For example — when the pilot switches from Simple to DIGEST-MD5, the
     * SASL panel lights up:</p>
     * <pre>
     *   authMethod = SASL_DIGEST_MD5  → true
     *   authMethod = SIMPLE           → false
     * </pre>
     *
     * @return {@code true} for DIGEST-MD5, CRAM-MD5, or GSSAPI.
     */
    private boolean isSaslEnabled()
    {
        AuthenticationMethod authenticationMethod = getAuthenticationMethod();

        return ( authenticationMethod == AuthenticationMethod.SASL_DIGEST_MD5 )
            || ( authenticationMethod == AuthenticationMethod.SASL_CRAM_MD5 )
            || ( authenticationMethod == AuthenticationMethod.SASL_GSSAPI );
    }


    // ── Checking If Kerberos Mode Is Active ───────────────────────────────────────────
    // GSSAPI (Kerberos) is a completely different credential pathway.  When it is
    // active, the KRB5 section enables and several other fields behave differently.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when GSSAPI (Kerberos) is selected.
     *
     * <p>Used to gate whether the KRB5 Options section is enabled and whether
     * the "Check Authentication" button should be active without a password.</p>
     *
     * <p>For example — Luke switches to GSSAPI, the KRB5 section unlocks:</p>
     * <pre>
     *   authMethod = SASL_GSSAPI  → true
     *   authMethod = SIMPLE       → false
     * </pre>
     *
     * @return {@code true} if the selected method is SASL_GSSAPI.
     */
    private boolean isGssapiEnabled()
    {
        return getAuthenticationMethod() == AuthenticationMethod.SASL_GSSAPI;
    }


    // ── LOAD PARAMETERS ───────────────────────────────────────────────────────────

    // ── Loading Saved Credentials Into The Console ────────────────────────────────────
    // When a pilot reopens an existing connection's properties, the base computer
    // needs to reload all their saved credentials into the console fields.  Bind DN,
    // password (from the keystore if enabled, or from the parameter bean), SASL
    // realm, QOP, security strength, mutual-auth flag, and all KRB5 settings.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates all fields from the given {@link ConnectionParameter}.
     *
     * <p>Reads the bind password from the keystore if the keystore is enabled and
     * loaded; otherwise reads it from the parameter directly.  The "Save Password"
     * checkbox is checked if and only if a password was stored.</p>
     *
     * <p>For example — when Leia reopens her LDAP connection properties, her saved
     * credentials flow back into the form:</p>
     * <pre>
     *   parameter.bindPrincipal = "cn=Leia Organa,dc=alderaan,dc=org"
     *   keystore.getConnectionPassword(id) = "r3b3ll1on"
     *   → bindPrincipalCombo.text   = "cn=Leia Organa,dc=alderaan,dc=org"
     *   → bindPasswordText.text     = "r3b3ll1on"
     *   → saveBindPasswordButton    = checked
     * </pre>
     *
     * @param parameter The existing connection parameters to load.
     */
    protected void loadParameters( ConnectionParameter parameter )
    {
        connectionParameter = parameter;
        AuthenticationMethod authenticationMethod = parameter.getAuthMethod();

        int index = authenticationMethod.getValue();
        authenticationMethodCombo.select( index );
        bindPrincipalCombo.setText( CommonUIUtils.getTextValue( parameter.getBindPrincipal() ) );

        String bindPassword = null;

        // ── READ PASSWORD FROM KEYSTORE OR PARAMETER ──────────────────────────────
        if ( PasswordsKeyStoreManagerUtils.isPasswordsKeystoreEnabled() )
        {
            PasswordsKeyStoreManager passwordsKeyStoreManager = ConnectionCorePlugin.getDefault()
                .getPasswordsKeyStoreManager();

            if ( passwordsKeyStoreManager.isLoaded() )
            {
                bindPassword = passwordsKeyStoreManager.getConnectionPassword( parameter.getId() );
            }
        }
        else
        {
            bindPassword = parameter.getBindPassword();
        }

        bindPasswordText.setText( CommonUIUtils.getTextValue( bindPassword ) );

        // ── SAVE PASSWORD BUTTON ──────────────────────────────────────────────────
        // The button is selected (checked) if and only if there is a password stored.
        saveBindPasswordButton.setSelection( bindPassword != null );

        // ── SASL REALM ────────────────────────────────────────────────────────────
        saslRealmText.setText( CommonUIUtils.getTextValue( parameter.getSaslRealm() ) );

        // ── SASL QOP ──────────────────────────────────────────────────────────────
        int qopIndex;

        SaslQoP saslQop = parameter.getSaslQop();

        switch ( saslQop )
        {
            case AUTH_INT:
                qopIndex = 1;
                break;

            case AUTH_CONF:
                qopIndex = 2;
                break;

            default:
                qopIndex = 0;
                break;
        }

        saslQopCombo.select( qopIndex );

        // ── SASL SECURITY STRENGTH ────────────────────────────────────────────────
        int securityStrengthIndex;

        SaslSecurityStrength securityStrength = parameter.getSaslSecurityStrength();

        switch ( securityStrength )
        {
            case MEDIUM:
                securityStrengthIndex = 1;
                break;

            case LOW:
                securityStrengthIndex = 2;
                break;

            default:
                securityStrengthIndex = 0;
                break;
        }

        saslSecurityStrengthCombo.select( securityStrengthIndex );

        // ── MUTUAL AUTHENTICATION ─────────────────────────────────────────────────
        saslMutualAuthenticationButton.setSelection( parameter.isSaslMutualAuthentication() );

        // ── KRB5 ──────────────────────────────────────────────────────────────────
        krb5CredentialConfigurationUseNativeButton
            .setSelection( parameter.getKrb5CredentialConfiguration() == Krb5CredentialConfiguration.USE_NATIVE );
        krb5CredentialConfigurationObtainTgtButton
            .setSelection( parameter.getKrb5CredentialConfiguration() == Krb5CredentialConfiguration.OBTAIN_TGT );
        krb5ConfigDefaultButton.setSelection( parameter.getKrb5Configuration() == Krb5Configuration.DEFAULT );
        krb5ConfigFileButton.setSelection( parameter.getKrb5Configuration() == Krb5Configuration.FILE );
        krb5ConfigManualButton.setSelection( parameter.getKrb5Configuration() == Krb5Configuration.MANUAL );
        krb5ConfigFileText.setText( CommonUIUtils.getTextValue( parameter.getKrb5ConfigurationFile() ) ); //$NON-NLS-1$
        krb5ConfigManualRealmText.setText( CommonUIUtils.getTextValue( parameter.getKrb5Realm() ) ); //$NON-NLS-1$
        krb5ConfigManualHostText.setText( CommonUIUtils.getTextValue( parameter.getKrb5KdcHost() ) ); //$NON-NLS-1$
        krb5ConfigManualPortText.setText( CommonUIUtils.getTextValue( parameter.getKrb5KdcPort() ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── INIT LISTENERS ────────────────────────────────────────────────────────────

    // ── Wiring All The Console's Change Listeners ─────────────────────────────────────
    // Every knob, switch, and text field on the authentication console needs to report
    // back to the wizard whenever the pilot makes a change.  We wire a listener to
    // each one so validate() and the wizard chrome stay in sync.  The KDC port field
    // gets an extra VerifyListener that rejects non-digit characters.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Wires listeners on all fields so the wizard chrome updates whenever any
     * value changes.
     *
     * <p>The KDC port field only accepts digits (enforced by a VerifyListener).
     * The "Save Password" checkbox clears the password text when unchecked so the
     * password is not silently retained in memory.</p>
     *
     * <p>For example — the base tech connects every console switch to the main
     * alarm system at Yavin 4:</p>
     * <pre>
     *   authMethodCombo → connectionPageModified()
     *   bindPrincipalCombo → connectionPageModified()
     *   saveBindPasswordButton → clears password text if unchecked, then notify
     *   checkAuthButton → runs CheckBindRunnable, shows result dialog
     *   kdcPortText → rejects non-digits via VerifyListener
     * </pre>
     */
    protected void initListeners()
    {
        authenticationMethodCombo.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                connectionPageModified();
            }
        } );

        bindPrincipalCombo.addModifyListener( event -> connectionPageModified() );

        bindPasswordText.addModifyListener( event -> connectionPageModified() );

        saveBindPasswordButton.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             *
             * Clears the password field when the user unchecks "Save Password"
             * so the password is not silently retained in memory.
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                if ( !saveBindPasswordButton.getSelection() )
                {
                    // ── CLEAR SAVED PASSWORD ──────────────────────────────────────
                    bindPasswordText.setText( StringUtils.EMPTY ); //$NON-NLS-1$
                }

                connectionPageModified();
            }
        } );

        // ── CHECK AUTHENTICATION BUTTON ───────────────────────────────────────────
        checkPrincipalPasswordAuthButton.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             *
             * Runs {@link CheckBindRunnable} in a modal progress context.  On
             * success, opens an information dialog confirming that the bind
             * succeeded.
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                Connection connection = getTestConnection();
                CheckBindRunnable runnable = new CheckBindRunnable( connection );
                IStatus status = RunnableContextRunner.execute( runnable, runnableContext, true );

                if ( status.isOK() )
                {
                    MessageDialog.openInformation( Display.getDefault().getActiveShell(), Messages
                        .getString( "AuthenticationParameterPage.CheckAuthentication" ), //$NON-NLS-1$
                        Messages.getString( "AuthenticationParameterPage.AuthenticationSuccessfull" ) ); //$NON-NLS-1$
                }
            }
        } );

        saslRealmText.addModifyListener( event -> connectionPageModified() );

        saslQopCombo.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                connectionPageModified();
            }
        } );

        saslSecurityStrengthCombo.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                connectionPageModified();
            }
        } );

        saslMutualAuthenticationButton.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                connectionPageModified();
            }
        } );

        krb5CredentialConfigurationUseNativeButton.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                connectionPageModified();
            }
        } );

        krb5CredentialConfigurationObtainTgtButton.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                connectionPageModified();
            }
        } );

        krb5ConfigDefaultButton.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                connectionPageModified();
            }
        } );

        krb5ConfigFileButton.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                connectionPageModified();
            }
        } );

        krb5ConfigFileText.addModifyListener( event -> connectionPageModified() );

        krb5ConfigManualButton.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                connectionPageModified();
            }
        } );

        krb5ConfigManualRealmText.addModifyListener( event -> connectionPageModified() );

        krb5ConfigManualHostText.addModifyListener( event -> connectionPageModified() );

        // ── KDC PORT: DIGITS ONLY ─────────────────────────────────────────────────
        krb5ConfigManualPortText.addVerifyListener( event -> {
            if ( !event.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
            {
                event.doit = false;
            }
        } );

        krb5ConfigManualPortText.addModifyListener( event -> connectionPageModified() );
    }


    // ── SAVE PARAMETERS ───────────────────────────────────────────────────────────

    // ── Committing The Credentials To The Connection Bean ────────────────────────────
    // Once the pilot has filled in everything and clicks Finish, we flush every field
    // value into the ConnectionParameter bean.  The bind password goes into the
    // keystore if it is enabled, or straight into the bean if not.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Writes all authentication parameters into the given {@link ConnectionParameter}.
     *
     * <p>The bind password is stored in the keystore (if enabled and loaded) or
     * directly in the parameter bean.  All SASL and KRB5 settings are written
     * unconditionally so the bean always reflects the current UI state.</p>
     *
     * <p>For example — Han clicks Finish and his credentials are locked into the
     * connection record:</p>
     * <pre>
     *   parameter.authMethod    = SIMPLE
     *   parameter.bindPrincipal = "cn=Han Solo,ou=pilots,dc=rebellion,dc=org"
     *   keystore.store(id, "nerfherder77")   // if keystore enabled
     * </pre>
     *
     * @param parameter The parameter bean to populate.
     */
    public void saveParameters( ConnectionParameter parameter )
    {
        parameter.setAuthMethod( getAuthenticationMethod() );
        parameter.setBindPrincipal( getBindPrincipal() );

        // ── SAVE PASSWORD ─────────────────────────────────────────────────────────
        if ( PasswordsKeyStoreManagerUtils.isPasswordsKeystoreEnabled() )
        {
            PasswordsKeyStoreManager passwordsKeyStoreManager = ConnectionCorePlugin.getDefault()
                .getPasswordsKeyStoreManager();

            if ( passwordsKeyStoreManager.isLoaded() )
            {
                passwordsKeyStoreManager.storeConnectionPassword( parameter.getId(), getBindPassword() );
            }
        }
        else
        {
            parameter.setBindPassword( getBindPassword() );
        }

        parameter.setSaslRealm( getSaslRealm() );
        parameter.setSaslQop( getSaslQop() );
        parameter.setSaslSecurityStrength( getSaslSecurityStrength() );
        parameter.setSaslMutualAuthentication( saslMutualAuthenticationButton.getSelection() );

        parameter.setKrb5CredentialConfiguration( getKrb5CredentialProvider() );
        parameter.setKrb5Configuration( getKrb5Configuration() );
        parameter.setKrb5ConfigurationFile( krb5ConfigFileText.getText() );
        parameter.setKrb5Realm( krb5ConfigManualRealmText.getText() );
        parameter.setKrb5KdcHost( krb5ConfigManualHostText.getText() );
        parameter.setKrb5KdcPort( getKdcPort() );
    }


    // ── SAVE DIALOG SETTINGS ──────────────────────────────────────────────────────

    // ── Storing The History For Next Time ────────────────────────────────────────────
    // After a successful connection setup the base computer logs the bind principal
    // and (if DIGEST-MD5) the SASL realm in the dialog history so they appear in the
    // combo drop-downs next time the pilot opens the New Connection Wizard.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Persists the bind principal and (for DIGEST-MD5) the SASL realm to the
     * dialog settings history.
     *
     * <p>Stored entries appear in the combo drop-downs next time the wizard opens,
     * saving the pilot from retyping the same DN every session.</p>
     *
     * <p>For example — after Leia finishes creating her connection, the base
     * logs her DN so it pre-fills next time:</p>
     * <pre>
     *   DIALOGSETTING_KEY_PRINCIPAL_HISTORY ← "cn=Leia Organa,dc=alderaan,dc=org"
     *   DIALOGSETTING_KEY_REALM_HISTORY     ← "REBEL.BASE"  (if DIGEST-MD5)
     * </pre>
     */
    public void saveDialogSettings()
    {
        IDialogSettings dialogSettings = ConnectionUIPlugin.getDefault().getDialogSettings();

        HistoryUtils.save( dialogSettings, ConnectionUIConstants.DIALOGSETTING_KEY_PRINCIPAL_HISTORY,
            bindPrincipalCombo.getText() );

        if ( getAuthenticationMethod().equals( AuthenticationMethod.SASL_DIGEST_MD5 ) )
        {
            HistoryUtils.save( dialogSettings, ConnectionUIConstants.DIALOGSETTING_KEY_REALM_HISTORY,
                saslRealmText.getText() );
        }
    }


    // ── SET FOCUS ─────────────────────────────────────────────────────────────────

    // ── Pointing The Cursor At The Bind-DN Field ──────────────────────────────────────
    // When the authentication tab becomes visible, we put focus on the bind principal
    // combo so the pilot can start typing their DN right away without an extra click.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Gives keyboard focus to the bind principal combo.
     *
     * <p>Called by the wizard framework when this tab page becomes visible.
     * Putting focus on the DN field means the pilot can start typing immediately.</p>
     *
     * <p>For example — the Authentication tab opens and the cursor lands in the
     * "Bind DN or User" field ready for Leia to type:</p>
     * <pre>
     *   bindPrincipalCombo.setFocus()
     *   → "cn=|" (cursor ready in the bind DN box)
     * </pre>
     */
    public void setFocus()
    {
        bindPrincipalCombo.setFocus();
    }


    // ── ARE PARAMETERS MODIFIED ───────────────────────────────────────────────────

    // ── Detecting Whether Auth Settings Changed ───────────────────────────────────────
    // After editing a connection's properties, the wizard needs to know if anything
    // important changed.  For authentication, any change at all requires reconnection,
    // so we simply delegate to isReconnectionRequired().
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if any authentication field changed.
     *
     * <p>Any change to auth settings requires reconnection, so we delegate directly
     * to {@link #isReconnectionRequired()} — there is no "harmless" auth change.</p>
     *
     * <p>For example — Han changes his bind password, so the wizard marks the
     * connection as needing a reconnect:</p>
     * <pre>
     *   areParametersModifed()  →  true
     *   wizard: "Connection will be closed and re-opened on Finish."
     * </pre>
     *
     * @return {@code true} if any field was modified.
     */
    public boolean areParametersModifed()
    {
        return isReconnectionRequired();
    }


    // ── IS RECONNECTION REQUIRED ──────────────────────────────────────────────────

    // ── Checking Whether The Hyperdrive Needs A Full Restart ──────────────────────────
    // Changing the auth method, bind DN, password, or any SASL/KRB5 setting while
    // connected requires closing and re-opening the connection — there is no way to
    // renegotiate mid-flight.  We compare every auth field against the saved parameter.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if any authentication or SASL/KRB5 setting changed,
     * which would require closing and re-opening the connection.
     *
     * <p>We compare every auth-related field against the saved
     * {@link #connectionParameter}.  A null parameter (new connection) also
     * returns true so the caller knows there is no existing connection to reuse.</p>
     *
     * <p>For example — Leia changes her bind DN mid-session, so the connection
     * must restart:</p>
     * <pre>
     *   connectionParameter.bindPrincipal = "cn=Leia,dc=alderaan,dc=org"
     *   getBindPrincipal()                = "cn=Leia Organa,dc=alderaan,dc=org"
     *   → not equal  →  isReconnectionRequired() = true
     * </pre>
     *
     * @return {@code true} if the connection must be closed and reopened.
     */
    public boolean isReconnectionRequired()
    {
        return connectionParameter == null || connectionParameter.getAuthMethod() != getAuthenticationMethod()
            || !StringUtils.equals( connectionParameter.getBindPrincipal(), getBindPrincipal() )
            || !StringUtils.equals( connectionParameter.getBindPassword(), getBindPassword() )
            || !StringUtils.equals( connectionParameter.getSaslRealm(), getSaslRealm() )
            || connectionParameter.getSaslQop() != getSaslQop()
            || connectionParameter.getSaslSecurityStrength() != getSaslSecurityStrength()
            || connectionParameter.isSaslMutualAuthentication() != saslMutualAuthenticationButton.getSelection()
            || connectionParameter.getKrb5CredentialConfiguration() != getKrb5CredentialProvider()
            || connectionParameter.getKrb5Configuration() != getKrb5Configuration()
            || !StringUtils.equals( connectionParameter.getKrb5ConfigurationFile(), krb5ConfigFileText.getText() )
            || !StringUtils.equals( connectionParameter.getKrb5Realm(), krb5ConfigManualRealmText.getText() )
            || !StringUtils.equals( connectionParameter.getKrb5KdcHost(), krb5ConfigManualHostText.getText() )
            || connectionParameter.getKrb5KdcPort() != getKdcPort();
    }


    // ── MERGE PARAMETERS TO LDAP URL ──────────────────────────────────────────────

    // ── Encoding Credentials Into The LDAP URL ────────────────────────────────────────
    // An LDAP URL can carry extra connection settings as X-extension attributes.
    // We serialise every non-default auth parameter here so the whole connection can
    // be copy-pasted as a single URL — like encoding mission orders into a hyperspace
    // beacon broadcast.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Serialises all authentication parameters into an LDAP URL using X-LDAP-URL
     * extensions.
     *
     * <p>Only non-default values are emitted to keep the URL short.  Defaults are:
     * auth method = NONE (no X-AUTH-METHOD if no bind user), QOP = AUTH,
     * security strength = HIGH, mutual auth = on, KRB5 config = DEFAULT,
     * KRB5 credentials = USE_NATIVE.</p>
     *
     * <p>For example — Leia's SIMPLE connection with a bind DN serialises to:</p>
     * <pre>
     *   ldap://yavin4.rebel.org/dc=rebellion,dc=org
     *     ?X-BIND-USER=cn%3DLeia+Organa%2Cdc%3Dalderaan%2Cdc%3Dorg
     *     &amp;X-BIND-PASSWORD=r3b3ll1on
     * </pre>
     *
     * @param parameter The connection parameters to serialise.
     * @param ldapUrl   The LDAP URL to write extensions into.
     */
    public void mergeParametersToLdapURL( ConnectionParameter parameter, LdapUrl ldapUrl )
    {
        // ── AUTH METHOD ───────────────────────────────────────────────────────────
        switch ( parameter.getAuthMethod() )
        {
            case SASL_CRAM_MD5:
                ldapUrl.getExtensions().add( new Extension( false, X_AUTH_METHOD, X_AUTH_METHOD_CRAM_MD5 ) );
                break;

            case SASL_DIGEST_MD5:
                ldapUrl.getExtensions().add( new Extension( false, X_AUTH_METHOD, X_AUTH_METHOD_DIGEST_MD5 ) );
                break;

            case SASL_GSSAPI:
                ldapUrl.getExtensions().add( new Extension( false, X_AUTH_METHOD, X_AUTH_METHOD_GSSAPI ) );
                break;

            case SIMPLE:
                if ( StringUtils.isEmpty( parameter.getBindPrincipal() ) )
                {
                    // default if bind user is present
                    ldapUrl.getExtensions().add( new Extension( false, X_AUTH_METHOD, X_AUTH_METHOD_SIMPLE ) );
                }

                break;

            case NONE:
                if ( StringUtils.isNotEmpty( parameter.getBindPrincipal() ) )
                {
                    // default if bind user is absent
                    ldapUrl.getExtensions().add( new Extension( false, X_AUTH_METHOD, X_AUTH_METHOD_ANONYMOUS ) );
                }

                break;
        }

        // ── BIND PRINCIPAL AND PASSWORD ───────────────────────────────────────────
        if ( StringUtils.isNotEmpty( parameter.getBindPrincipal() ) )
        {
            ldapUrl.getExtensions().add( new Extension( false, X_BIND_USER, parameter.getBindPrincipal() ) );
        }

        if ( StringUtils.isNotEmpty( parameter.getBindPassword() ) )
        {
            ldapUrl.getExtensions().add( new Extension( false, X_BIND_PASSWORD, parameter.getBindPassword() ) );
        }

        // ── SASL SETTINGS ─────────────────────────────────────────────────────────
        switch ( parameter.getAuthMethod() )
        {
            case SASL_CRAM_MD5:
            case SASL_DIGEST_MD5:
            case SASL_GSSAPI:
                if ( StringUtils.isNotEmpty( parameter.getSaslRealm() ) )
                {
                    ldapUrl.getExtensions().add( new Extension( false, X_SASL_REALM, parameter.getSaslRealm() ) );
                }

                switch ( parameter.getSaslQop() )
                {
                    case AUTH:
                        // default
                        break;

                    case AUTH_INT:
                        ldapUrl.getExtensions().add( new Extension( false, X_SASL_QOP, X_SASL_QOP_AUTH_INT ) );
                        break;

                    case AUTH_CONF:
                        ldapUrl.getExtensions().add( new Extension( false, X_SASL_QOP, X_SASL_QOP_AUTH_INT_PRIV ) );
                        break;
                }

                switch ( parameter.getSaslSecurityStrength() )
                {
                    case HIGH:
                        // default
                        break;

                    case MEDIUM:
                        ldapUrl.getExtensions().add(
                            new Extension( false, X_SASL_SEC_STRENGTH, X_SASL_SEC_STRENGTH_MEDIUM ) );
                        break;

                    case LOW:
                        ldapUrl.getExtensions().add(
                            new Extension( false, X_SASL_SEC_STRENGTH, X_SASL_SEC_STRENGTH_LOW ) );
                        break;
                }

                if ( !parameter.isSaslMutualAuthentication() )
                {
                    ldapUrl.getExtensions().add( new Extension( false, X_SASL_NO_MUTUAL_AUTH, null ) );
                }

                break;

            default:
                break;
        }

        // ── KRB5 SETTINGS ─────────────────────────────────────────────────────────
        if ( parameter.getAuthMethod() == AuthenticationMethod.SASL_GSSAPI )
        {
            switch ( parameter.getKrb5CredentialConfiguration() )
            {
                case USE_NATIVE:
                    // default
                    break;

                case OBTAIN_TGT:
                    ldapUrl.getExtensions().add(
                        new Extension( false, X_KRB5_CREDENTIALS_CONF, X_KRB5_CREDENTIALS_CONF_OBTAIN_TGT ) );
                    break;
            }

            switch ( parameter.getKrb5Configuration() )
            {
                case DEFAULT:
                    // default
                    break;

                case FILE:
                    ldapUrl.getExtensions().add( new Extension( false, X_KRB5_CONFIG, X_KRB5_CONFIG_FILE ) );
                    ldapUrl.getExtensions().add(
                        new Extension( false, X_KRB5_CONFIG_FILE_FILE, parameter.getKrb5ConfigurationFile() ) );
                    break;

                case MANUAL:
                    ldapUrl.getExtensions().add( new Extension( false, X_KRB5_CONFIG, X_KRB5_CONFIG_MANUAL ) );
                    ldapUrl.getExtensions().add(
                        new Extension( false, X_KRB5_CONFIG_MANUAL_REALM, parameter.getKrb5Realm() ) );
                    ldapUrl.getExtensions().add(
                        new Extension( false, X_KRB5_CONFIG_MANUAL_KDC_HOST, parameter.getKrb5KdcHost() ) );
                    ldapUrl.getExtensions().add(
                        new Extension( false, X_KRB5_CONFIG_MANUAL_KDC_PORT,
                            Integer.toString( parameter.getKrb5KdcPort() ) ) ); //$NON-NLS-1$
                    break;
            }
        }
    }


    // ── MERGE LDAP URL TO PARAMETERS ─────────────────────────────────────────────

    // ── Decoding Credentials From A Pasted LDAP URL ───────────────────────────────────
    // When the user pastes an LDAP URL into the connection editor, we need to decode
    // all the X-extension attributes back into the ConnectionParameter bean.  It is
    // the reverse journey — from URL beacon back to mission briefing.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads back all authentication parameters from an LDAP URL.
     *
     * <p>Auth method defaults to SIMPLE if X-BIND-USER is present but
     * X-AUTH-METHOD is absent, or NONE otherwise.  SASL and KRB5 fields default
     * to their preferred values (AUTH, HIGH, mutual auth on, USE_NATIVE, DEFAULT)
     * if absent from the URL.</p>
     *
     * <p>For example — Leia pastes a connection URL and her credentials are
     * decoded automatically:</p>
     * <pre>
     *   X-BIND-USER    = "cn=Leia Organa,dc=alderaan,dc=org"
     *   X-BIND-PASSWORD = "r3b3ll1on"
     *   (no X-AUTH-METHOD + bind user present) → authMethod = SIMPLE
     * </pre>
     *
     * @param ldapUrl   The LDAP URL to read extensions from.
     * @param parameter The connection parameter bean to populate.
     */
    public void mergeLdapUrlToParameters( LdapUrl ldapUrl, ConnectionParameter parameter )
    {
        // ── BIND USER AND PASSWORD ────────────────────────────────────────────────
        String principal = ldapUrl.getExtensionValue( X_BIND_USER );

        if ( principal == null )
        {
            principal = StringUtils.EMPTY;
        }

        parameter.setBindPrincipal( principal );

        String password = ldapUrl.getExtensionValue( X_BIND_PASSWORD );
        parameter.setBindPassword( password );

        // ── AUTH METHOD ───────────────────────────────────────────────────────────
        String authMethod = ldapUrl.getExtensionValue( X_AUTH_METHOD );

        if ( StringUtils.isNotEmpty( authMethod ) && X_AUTH_METHOD_ANONYMOUS.equalsIgnoreCase( authMethod ) )
        {
            parameter.setAuthMethod( ConnectionParameter.AuthenticationMethod.NONE );
        }
        else if ( StringUtils.isNotEmpty( authMethod ) && X_AUTH_METHOD_SIMPLE.equalsIgnoreCase( authMethod ) )
        {
            parameter.setAuthMethod( ConnectionParameter.AuthenticationMethod.SIMPLE );
        }
        else if ( StringUtils.isNotEmpty( authMethod ) && X_AUTH_METHOD_DIGEST_MD5.equalsIgnoreCase( authMethod ) )
        {
            parameter.setAuthMethod( ConnectionParameter.AuthenticationMethod.SASL_DIGEST_MD5 );
        }
        else if ( StringUtils.isNotEmpty( authMethod ) && X_AUTH_METHOD_CRAM_MD5.equalsIgnoreCase( authMethod ) )
        {
            parameter.setAuthMethod( ConnectionParameter.AuthenticationMethod.SASL_CRAM_MD5 );
        }
        else if ( StringUtils.isNotEmpty( parameter.getBindPrincipal() ) )
        {
            parameter.setAuthMethod( ConnectionParameter.AuthenticationMethod.SIMPLE );
        }
        else
        {
            parameter.setAuthMethod( ConnectionParameter.AuthenticationMethod.NONE );
        }

        // ── SASL REALM ────────────────────────────────────────────────────────────
        String saslRealm = ldapUrl.getExtensionValue( X_SASL_REALM );

        if ( StringUtils.isNotEmpty( saslRealm ) )
        {
            parameter.setSaslRealm( saslRealm );
        }

        // ── SASL QOP ──────────────────────────────────────────────────────────────
        String saslQop = ldapUrl.getExtensionValue( X_SASL_QOP );

        if ( StringUtils.isNotEmpty( saslQop ) && X_SASL_QOP_AUTH_INT.equalsIgnoreCase( saslQop ) )
        {
            parameter.setSaslQop( SaslQoP.AUTH_INT );
        }
        else if ( StringUtils.isNotEmpty( saslQop ) && X_SASL_QOP_AUTH_INT_PRIV.equalsIgnoreCase( saslQop ) )
        {
            parameter.setSaslQop( SaslQoP.AUTH_CONF );
        }
        else
        {
            parameter.setSaslQop( SaslQoP.AUTH );
        }

        // ── SASL SECURITY STRENGTH ────────────────────────────────────────────────
        String saslSecStrength = ldapUrl.getExtensionValue( X_SASL_SEC_STRENGTH );

        if ( StringUtils.isNotEmpty( saslSecStrength )
            && X_SASL_SEC_STRENGTH_MEDIUM.equalsIgnoreCase( saslSecStrength ) )
        {
            parameter.setSaslSecurityStrength( SaslSecurityStrength.MEDIUM );
        }
        else if ( StringUtils.isNotEmpty( saslSecStrength )
            && X_SASL_SEC_STRENGTH_LOW.equalsIgnoreCase( saslSecStrength ) )
        {
            parameter.setSaslSecurityStrength( SaslSecurityStrength.LOW );
        }
        else
        {
            parameter.setSaslSecurityStrength( SaslSecurityStrength.HIGH );
        }

        // ── SASL MUTUAL AUTHENTICATION ────────────────────────────────────────────
        // Mutual auth is ON by default; the extension is present only when it is OFF.
        Extension saslNoMutualAuth = ldapUrl.getExtension( X_SASL_NO_MUTUAL_AUTH );
        parameter.setSaslMutualAuthentication( saslNoMutualAuth == null );

        // ── KRB5 CREDENTIALS ──────────────────────────────────────────────────────
        String krb5CredentialsConf = ldapUrl.getExtensionValue( X_KRB5_CREDENTIALS_CONF );

        if ( StringUtils.isNotEmpty( krb5CredentialsConf )
            && X_KRB5_CREDENTIALS_CONF_OBTAIN_TGT.equalsIgnoreCase( krb5CredentialsConf ) )
        {
            parameter.setKrb5CredentialConfiguration( Krb5CredentialConfiguration.OBTAIN_TGT );
        }
        else
        {
            parameter.setKrb5CredentialConfiguration( Krb5CredentialConfiguration.USE_NATIVE );
        }

        // ── KRB5 CONFIGURATION ────────────────────────────────────────────────────
        String krb5Config = ldapUrl.getExtensionValue( X_KRB5_CONFIG );

        if ( StringUtils.isNotEmpty( krb5Config ) && X_KRB5_CONFIG_FILE.equalsIgnoreCase( krb5Config ) )
        {
            parameter.setKrb5Configuration( Krb5Configuration.FILE );
        }
        else if ( StringUtils.isNotEmpty( krb5Config ) && X_KRB5_CONFIG_MANUAL.equalsIgnoreCase( krb5Config ) )
        {
            parameter.setKrb5Configuration( Krb5Configuration.MANUAL );
        }
        else
        {
            parameter.setKrb5Configuration( Krb5Configuration.DEFAULT );
        }

        parameter.setKrb5ConfigurationFile( ldapUrl.getExtensionValue( X_KRB5_CONFIG_FILE_FILE ) );
        parameter.setKrb5Realm( ldapUrl.getExtensionValue( X_KRB5_CONFIG_MANUAL_REALM ) );
        parameter.setKrb5KdcHost( ldapUrl.getExtensionValue( X_KRB5_CONFIG_MANUAL_KDC_HOST ) );

        String kdcPort = ldapUrl.getExtensionValue( X_KRB5_CONFIG_MANUAL_KDC_PORT );

        try
        {
            parameter.setKrb5KdcPort( Integer.valueOf( kdcPort ) );
        }
        catch ( NumberFormatException e )
        {
            parameter.setKrb5KdcPort( 88 );
        }
    }
}
