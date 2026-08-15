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
package org.apache.directory.studio.apacheds.configuration.editor;


import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.server.config.beans.AuthenticationInterceptorBean;
import org.apache.directory.server.config.beans.DirectoryServiceBean;
import org.apache.directory.server.config.beans.InterceptorBean;
import org.apache.directory.server.config.beans.PasswordPolicyBean;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;


// ── CLASS: PasswordPoliciesPage — THE PASSWORD POLICIES COMMAND TAB ──────────────────────
// The Emperor has dedicated an entire tab of his configuration console to imperial
// password clearance rules — who can log in, for how long, and what happens when they
// fail too many times.  This class is that tab: one page in the multi-page editor that
// hosts the full master/details view for every configured password policy.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * The "Password Policies" tab page of the ApacheDS Server Configuration Editor.
 * It owns the {@link PasswordPoliciesMasterDetailsBlock} and also provides a handful of
 * static helper methods that the rest of the plugin uses to find, create, or check
 * password policy beans inside a {@link DirectoryServiceBean}.
 * Think of this class as the Empire's password-policy command tab — the single screen
 * where all clearance rules are defined and managed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PasswordPoliciesPage extends ServerConfigurationEditorPage
{
    /** The authentication interceptor name */
    private static final String AUTHENTICATION_INTERCEPTOR_ID = "authenticationInterceptor";

    /** Default name for the passwordPolicy */
    private static final String PASSWORD_POLICY_ID_DEFAULT = "default";

    /** The Page ID*/
    public static final String ID = PasswordPoliciesPage.class.getName();

    /** The Page Title */
    private static final String TITLE = Messages.getString( "PasswordPoliciesPage.PasswordPolicies" ); //$NON-NLS-1$

    /** The Master Details Block */
    private PasswordPoliciesMasterDetailsBlock masterDetailsBlock;


    // ── Commissioning The Password Policies Tab ───────────────────────────────────────────
    // Grand Moff Tarkin opens the configuration editor and the Password Policies tab slides
    // into the tab strip, ready to govern every login rule in the Empire.
    // We call super to register the page with the editor under its unique ID and title.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new PasswordPoliciesPage and registers it with the given editor.
     * Passing {@link #ID} and {@link #TITLE} to the parent lets Eclipse place the tab
     * correctly in the multi-page editor's tab strip.
     *
     * <p>For example — the tab is commissioned into the editor's command console:</p>
     * <pre>
     *   Tarkin opens the server config editor and the "Password Policies" tab appears.
     *   From that point, any click on that tab routes here.
     * </pre>
     *
     * @param editor  the parent {@link ServerConfigurationEditor} that hosts this page
     */
    public PasswordPoliciesPage( ServerConfigurationEditor editor )
    {
        super( editor, ID, TITLE );
    }


    // ── Deploying The Master/Details View ─────────────────────────────────────────────────
    // The tab's UI crew arrives and installs the master/details block — the split-screen
    // that shows the policy roster on the left and the selected policy's settings on the right.
    // We create the block and ask it to build itself inside the managed form.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds this page's visual content by creating the
     * {@link PasswordPoliciesMasterDetailsBlock} and calling its
     * {@code createContent} method against our managed form.
     * Eclipse calls this once when the tab is first shown.
     *
     * <p>For example — the UI crew wires up the split-screen display:</p>
     * <pre>
     *   The block arrives and builds both the roster list and the detail panel.
     *   From that moment the tab is fully interactive.
     * </pre>
     *
     * @param parent   the SWT composite that will host the page content
     * @param toolkit  the Eclipse Forms toolkit for styling widgets
     */
    protected void createFormContent( Composite parent, FormToolkit toolkit )
    {
        masterDetailsBlock = new PasswordPoliciesMasterDetailsBlock( this );
        masterDetailsBlock.createContent( getManagedForm() );
    }


    // ── Refreshing The Policy Roster ──────────────────────────────────────────────────────
    // When the Imperial commander loads a new config file the roster board needs to repaint
    // so it shows the freshly loaded policies instead of whatever was there before.
    // We guard against refreshing before the page has initialised to avoid a null crash.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Asks the master/details block to reload and repaint itself from the current model.
     * We skip the refresh if the page hasn't finished initialising yet — the block's
     * components may not exist at that point.
     *
     * <p>For example — the commander reloads the roster after opening a new config:</p>
     * <pre>
     *   A new config file is loaded; the roster needs to show its policies, not the old ones.
     *   If the page UI hasn't been built yet we skip it — nothing to refresh.
     * </pre>
     */
    protected void refreshUI()
    {
        if ( isInitialized() )
        {
            masterDetailsBlock.refreshUI();
        }
    }


    // ── Fetching Or Creating The Default Policy Bean ──────────────────────────────────────
    // The Emperor always has a default clearance policy on file; if it has somehow gone
    // missing, this method drafts a fresh one and files it before returning.
    // That way callers always get a non-null bean without having to check themselves.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the default {@link PasswordPolicyBean} for the given directory service,
     * creating and registering one if it doesn't already exist.
     * Use this when you need a guaranteed non-null default policy bean — for instance,
     * when the General Settings page reads password-quality defaults.
     *
     * <p>For example — the Emperor's aide fetches the default clearance form:</p>
     * <pre>
     *   She searches the interceptor's cabinet for a policy named "default".
     *   If found, she hands it over; if not, she drafts a new one and files it first.
     * </pre>
     *
     * @param directoryServiceBean  the root config bean that owns the interceptor chain
     * @return                      the default password policy bean, never {@code null}
     */
    public static PasswordPolicyBean getPasswordPolicyBean( DirectoryServiceBean directoryServiceBean )
    {
        // Finding the password policy
        PasswordPolicyBean passwordPolicyBean = findPasswordPolicyBean( directoryServiceBean );

        if ( passwordPolicyBean == null )
        {
            addPasswordPolicyBean( directoryServiceBean );
        }

        return passwordPolicyBean;
    }


    // ── Searching The Interceptor For The Default Policy ──────────────────────────────────
    // A quiet Imperial aide digs through the authentication interceptor's filing cabinet
    // looking for the policy whose ID is "default" — not announcing herself if she fails.
    // We delegate to the two private helpers: find the interceptor, then find the policy.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up the default password policy without creating one if it's missing.
     * This is the private counterpart to {@link #getPasswordPolicyBean(DirectoryServiceBean)};
     * it returns {@code null} if the default policy hasn't been registered yet.
     *
     * <p>For example — the aide searches the cabinet without making noise:</p>
     * <pre>
     *   She checks the interceptor's policy list for an entry called "default".
     *   If she doesn't find it she returns empty-handed — no side effects.
     * </pre>
     *
     * @param directoryServiceBean  the root config bean
     * @return                      the default password policy bean, or {@code null}
     */
    private static PasswordPolicyBean findPasswordPolicyBean( DirectoryServiceBean directoryServiceBean )
    {
        return getPasswordPolicyBean( getAuthenticationInterceptorBean( directoryServiceBean ) );
    }


    // ── Locating The Authentication Checkpoint ────────────────────────────────────────────
    // The Imperial agent scans the interceptor chain for the one post with the badge
    // "authenticationInterceptor" — the single checkpoint that owns all password policies.
    // If found we return it cast to AuthenticationInterceptorBean; otherwise null.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Finds the {@link AuthenticationInterceptorBean} in the directory service's interceptor
     * chain by ID, returning {@code null} if the chain doesn't include one.
     * We need this bean as the owner of the password policy list.
     *
     * <p>For example — the agent checks every checkpoint badge in sequence:</p>
     * <pre>
     *   She walks the interceptor list checking each ID against "authenticationInterceptor".
     *   The first match that is actually an AuthenticationInterceptorBean gets returned.
     * </pre>
     *
     * @param directoryServiceBean  the root config bean whose interceptor list we scan
     * @return                      the authentication interceptor bean, or {@code null}
     */
    private static AuthenticationInterceptorBean getAuthenticationInterceptorBean(
        DirectoryServiceBean directoryServiceBean )
    {
        // Looking for the authentication interceptor
        for ( InterceptorBean interceptor : directoryServiceBean.getInterceptors() )
        {
            if ( AUTHENTICATION_INTERCEPTOR_ID.equalsIgnoreCase( interceptor.getInterceptorId() )
                && ( interceptor instanceof AuthenticationInterceptorBean ) )
            {
                return ( AuthenticationInterceptorBean ) interceptor;
            }
        }

        return null;
    }


    // ── Finding The Default Policy In The Interceptor ────────────────────────────────────
    // Inside the interceptor's cabinet the aide flips through each clearance dossier
    // checking whether its ID equals "default" — the Empire's baseline clearance.
    // We return the first match, or null if the interceptor is null or the policy is absent.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Searches the given {@link AuthenticationInterceptorBean}'s policy list for the one
     * whose ID equals {@code "default"} (case-insensitive).
     * Returns {@code null} if the interceptor is {@code null} or no matching policy exists.
     *
     * <p>For example — the aide flips through the interceptor's filing cabinet:</p>
     * <pre>
     *   Each dossier gets a quick ID check; the one marked "default" wins.
     *   If none match, she returns empty-handed.
     * </pre>
     *
     * @param authenticationInterceptor  the interceptor whose policy list to search
     * @return                           the default policy bean, or {@code null}
     */
    private static PasswordPolicyBean getPasswordPolicyBean( AuthenticationInterceptorBean authenticationInterceptor )
    {
        // Looking for the default password policy
        if ( authenticationInterceptor != null )
        {
            for ( PasswordPolicyBean passwordPolicy : authenticationInterceptor.getPasswordPolicies() )
            {
                if ( PASSWORD_POLICY_ID_DEFAULT.equalsIgnoreCase( passwordPolicy.getPwdId() ) )
                {
                    return passwordPolicy;
                }
            }
        }

        return null;
    }


    // ── Drafting The Empire's Baseline Clearance Policy ───────────────────────────────────
    // When no default policy exists yet an Imperial clerk drafts a new one from scratch,
    // filling in the Empire's standard defaults (5-char minimum, 5 max failures, etc.)
    // and filing it in the authentication interceptor's cabinet.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new default {@link PasswordPolicyBean} with sensible out-of-the-box settings
     * and registers it with the authentication interceptor.
     * Only called when {@link #findPasswordPolicyBean} returns {@code null}, so we don't
     * accidentally create duplicates.
     *
     * <p>For example — the Imperial clerk drafts the baseline clearance dossier:</p>
     * <pre>
     *   She fills in ID = "default", attribute = userPassword, min length = 5.
     *   She enables lockout with a 5-failure threshold and a 30-second reset window.
     *   The completed dossier goes straight into the interceptor's policy cabinet.
     * </pre>
     *
     * @param directoryServiceBean  the root config bean whose interceptor will own the new policy
     */
    private static void addPasswordPolicyBean( DirectoryServiceBean directoryServiceBean )
    {
        AuthenticationInterceptorBean authenticationInterceptor = getAuthenticationInterceptorBean( directoryServiceBean );

        if ( authenticationInterceptor != null )
        {
            // Creating the password policy
            PasswordPolicyBean passwordPolicy = new PasswordPolicyBean();

            // Configuring the password policy
            passwordPolicy.setPwdId( PASSWORD_POLICY_ID_DEFAULT );
            passwordPolicy.setPwdAttribute( SchemaConstants.USER_PASSWORD_AT );
            passwordPolicy.setPwdMinAge( 0 );
            passwordPolicy.setPwdMaxAge( 0 );
            passwordPolicy.setPwdInHistory( 5 );
            passwordPolicy.setPwdCheckQuality( 1 );
            passwordPolicy.setPwdMinLength( 5 );
            passwordPolicy.setPwdMaxLength( 0 );
            passwordPolicy.setPwdExpireWarning( 600 );
            passwordPolicy.setPwdGraceAuthNLimit( 5 );
            passwordPolicy.setPwdGraceExpire( 0 );
            passwordPolicy.setPwdLockout( true );
            passwordPolicy.setPwdLockoutDuration( 0 );
            passwordPolicy.setPwdMaxFailure( 5 );
            passwordPolicy.setPwdFailureCountInterval( 30 );
            passwordPolicy.setPwdMustChange( false );
            passwordPolicy.setPwdAllowUserChange( true );
            passwordPolicy.setPwdMinDelay( 0 );
            passwordPolicy.setPwdMaxDelay( 0 );
            passwordPolicy.setPwdMaxIdle( 0 );
            passwordPolicy
                .setPwdValidator( "org.apache.directory.server.core.api.authn.ppolicy.DefaultPasswordValidator" );

            // Adding the password policy to the authentication interceptor
            authenticationInterceptor.addPasswordPolicies( passwordPolicy );
        }
    }


    // ── Checking If This Is The Emperor's Own Policy ──────────────────────────────────────
    // The Emperor's personal clearance rule is special — it cannot be deleted or renamed.
    // This method checks whether a given policy is the one with the sacred ID "default".
    // We use it throughout the UI to disable Delete and rename controls for that entry.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the given password policy is the default one (i.e., its ID
     * equals {@code "default"} case-insensitively), {@code false} otherwise.
     * We use this to decide whether to disable the Delete button and the ID/description
     * fields in the detail panel — the default policy is protected.
     *
     * <p>For example — a security guard checks whether a dossier bears the Emperor's seal:</p>
     * <pre>
     *   She reads the ID field on the dossier.
     *   If it says "default" she stands aside: this one cannot be touched.
     *   Any other ID is fair game for editing or deletion.
     * </pre>
     *
     * @param passwordPolicy  the policy bean to inspect (may be {@code null})
     * @return                {@code true} if this is the default policy
     */
    public static boolean isDefaultPasswordPolicy( PasswordPolicyBean passwordPolicy )
    {
        if ( passwordPolicy != null )
        {
            return PASSWORD_POLICY_ID_DEFAULT.equalsIgnoreCase( passwordPolicy.getPwdId() );
        }

        return false;
    }
}
