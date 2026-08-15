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

package org.apache.directory.studio.connection.ui.preferences;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.core.ConnectionCoreConstants;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.connection.ui.widgets.CertificateListComposite;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


// ── CLASS: CertificateValidationPreferencePage — MON MOTHMA'S TRUSTED-AGENT DOSSIER
// Mon Mothma keeps two lists: agents she trusts permanently, and agents she trusts
// only for the duration of the current mission.  She can also flip a master switch
// to stop verifying credentials altogether — useful if the Rebellion is in a hurry,
// but risky.
// This preference page is exactly that: a "Validate certificates" master checkbox, and
// two tabbed CertificateListComposites — one for permanently-trusted certs, one for
// session-trusted certs.  When the master checkbox is unchecked the tabs are disabled
// (greyed out) because they would be irrelevant.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Preference page for managing TLS certificate validation settings.
 *
 * <p>Contains:</p>
 * <ul>
 *   <li>A "Validate certificates" master checkbox.  When unchecked, all server
 *       certificates are accepted without inspection and the tab folder below is
 *       disabled.</li>
 *   <li>A {@link TabFolder} with two {@link CertificateListComposite} tabs:
 *       <ol>
 *         <li>"Permanent Trusted" — backed by the permanent trust-store manager.</li>
 *         <li>"Temporary Trusted" — backed by the session trust-store manager.</li>
 *       </ol>
 *   </li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CertificateValidationPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    // ── UI WIDGETS ────────────────────────────────────────────────────────────────

    /** Master "Validate certificates" checkbox. */
    private Button verifyCertificatesButton;

    /** Tab folder containing the permanent and session trusted-certificate lists. */
    private TabFolder tabFolder;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link CertificateValidationPreferencePage}.
     *
     * <p>Sets the page title from the message bundle and wires up the preference
     * store from the connection UI plugin.</p>
     */
    public CertificateValidationPreferencePage()
    {
        super( Messages.getString( "CertificateValidationPreferencePage.CertificateValidation" ) ); //$NON-NLS-1$
        super.setPreferenceStore( ConnectionUIPlugin.getDefault().getPreferenceStore() );
    }


    // ── INIT ──────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Nothing to initialise here — the workbench is not needed.
     */
    public void init( IWorkbench workbench )
    {
        // Nothing to do.
    }


    // ── CREATE CONTENTS ───────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the page body:
     * <ol>
     *   <li>A "Validate certificates" checkbox wired to enable/disable the tab folder.</li>
     *   <li>A tab folder with "Permanent Trusted" and "Temporary Trusted" tabs, each
     *       backed by a {@link CertificateListComposite}.</li>
     * </ol>
     */
    protected Control createContents( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        // ── MASTER CHECKBOX ───────────────────────────────────────────────────────
        // Read the stored preference and initialise the checkbox accordingly.
        // ──────────────────────────────────────────────────────────────────────────
        Preferences preferences = ConnectionCorePlugin.getDefault().getPluginPreferences();
        boolean validateCertificates = preferences
            .getBoolean( ConnectionCoreConstants.PREFERENCE_VALIDATE_CERTIFICATES );
        verifyCertificatesButton = BaseWidgetUtils.createCheckbox( composite, Messages
            .getString( "CertificateValidationPreferencePage.ValidateCertificates" ), 1 ); //$NON-NLS-1$
        verifyCertificatesButton.setSelection( validateCertificates );
        verifyCertificatesButton.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                // ── SYNC TAB FOLDER WITH CHECKBOX ─────────────────────────────────
                // When validation is disabled the cert lists are irrelevant, so grey
                // them out.
                // ──────────────────────────────────────────────────────────────────
                tabFolder.setEnabled( verifyCertificatesButton.getSelection() );
            }
        } );

        // ── TAB FOLDER: PERMANENT + SESSION TRUSTED CERTIFICATES ─────────────────
        tabFolder = new TabFolder( composite, SWT.TOP );
        tabFolder.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

        // "Permanent Trusted" tab
        CertificateListComposite permanentCLComposite = new CertificateListComposite( tabFolder, SWT.NONE );
        permanentCLComposite.setInput( ConnectionCorePlugin.getDefault().getPermanentTrustStoreManager() );
        TabItem permanentTab = new TabItem( tabFolder, SWT.NONE, 0 );
        permanentTab.setText( Messages.getString( "CertificateValidationPreferencePage.PermanentTrusted" ) ); //$NON-NLS-1$
        permanentTab.setControl( permanentCLComposite );

        // "Temporary Trusted" (session) tab
        CertificateListComposite sessionCLComposite = new CertificateListComposite( tabFolder, SWT.NONE );
        sessionCLComposite.setInput( ConnectionCorePlugin.getDefault().getSessionTrustStoreManager() );
        TabItem sessionTab = new TabItem( tabFolder, SWT.NONE, 1 );
        sessionTab.setText( Messages.getString( "CertificateValidationPreferencePage.TemporaryTrusted" ) ); //$NON-NLS-1$
        sessionTab.setControl( sessionCLComposite );

        tabFolder.setEnabled( verifyCertificatesButton.getSelection() );
        return composite;
    }


    // ── PERFORM DEFAULTS ──────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Resets the "validate certificates" checkbox to the platform default and
     * persists the change.
     */
    @Override
    protected void performDefaults()
    {
        verifyCertificatesButton.setSelection( ConnectionCorePlugin.getDefault().getPluginPreferences()
            .getDefaultBoolean( ConnectionCoreConstants.PREFERENCE_VALIDATE_CERTIFICATES ) );
        ConnectionCorePlugin.getDefault().savePluginPreferences();
        super.performDefaults();
    }


    // ── PERFORM OK ────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Persists the current checkbox state to the connection core plugin
     * preferences and saves them to disk.
     *
     * @return Always {@code true}.
     */
    public boolean performOk()
    {
        ConnectionCorePlugin.getDefault().getPluginPreferences().setValue(
            ConnectionCoreConstants.PREFERENCE_VALIDATE_CERTIFICATES, verifyCertificatesButton.getSelection() );
        ConnectionCorePlugin.getDefault().savePluginPreferences();
        return true;
    }
}
