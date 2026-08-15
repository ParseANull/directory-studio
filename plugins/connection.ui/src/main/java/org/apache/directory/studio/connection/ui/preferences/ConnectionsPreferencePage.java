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
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


// ── CLASS: ConnectionsPreferencePage — THE REBEL BASE KERBEROS CONFIG TERMINAL ────
// Deep in the Rebel base, someone needs to configure how the fleet authenticates with
// the Empire's Kerberos realm — whether to trust the Java system properties for KRB5
// (e.g. set by IT) or to specify the JAAS login-module class name manually.
// This preference page handles exactly those two settings:
//   1. useKrb5SystemProperties — let Java figure it out from system properties.
//   2. krb5LoginModule — the JAAS context-factory class name if we're managing it.
// When "Use KRB5 system properties" is checked the manual text field is disabled
// because we won't need it.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Preference page for general connection settings — currently covering Kerberos
 * (KRB5 / GSSAPI) authentication configuration.
 *
 * <p>There are two settings:</p>
 * <ul>
 *   <li><b>Use KRB5 system properties</b> — when checked, the JVM's system
 *       properties ({@code java.security.krb5.conf} etc.) are used for Kerberos
 *       configuration.  The login-module text field is disabled.</li>
 *   <li><b>KRB5 login module</b> — the JAAS login-module class name used when
 *       system properties are not in play.  Pre-filled from the platform default.</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    // ── UI WIDGETS ────────────────────────────────────────────────────────────────

    /** Checkbox: whether to delegate KRB5 config to Java system properties. */
    private Button useKrb5SystemPropertiesButton;

    /** Note label showing the platform-detected default login module class name. */
    private Label krb5LoginModuleNoteLabel;

    /** Text field for the JAAS KRB5 login module class name. */
    private Text krb5LoginModuleText;

    /** Label for the login-module text field. */
    private Label krb5LoginModuleLabel;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link ConnectionsPreferencePage}.
     *
     * <p>Sets the page title and description from the message bundle, and points
     * the preference store at the connection UI plugin's store.</p>
     */
    public ConnectionsPreferencePage()
    {
        super( Messages.getString( "ConnectionsPreferencePage.Connections" ) ); //$NON-NLS-1$
        super.setPreferenceStore( ConnectionUIPlugin.getDefault().getPreferenceStore() );
        super.setDescription( Messages.getString( "ConnectionsPreferencePage.GeneralSettings" ) ); //$NON-NLS-1$
    }


    // ── INIT ──────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Nothing to initialise — the workbench reference is not needed here.
     */
    public void init( IWorkbench workbench )
    {
        // Nothing to do.
    }


    // ── CREATE CONTENTS ───────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the page body: a "KRB5 Settings" group with a checkbox and a
     * conditionally-enabled login-module text field.  The text field starts
     * disabled if "Use KRB5 system properties" is already checked in the stored
     * preferences.
     */
    protected Control createContents( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );

        Preferences preferences = ConnectionCorePlugin.getDefault().getPluginPreferences();

        // ── KRB5 SETTINGS GROUP ───────────────────────────────────────────────────
        Group krb5SettingsGroup = BaseWidgetUtils.createGroup(
            BaseWidgetUtils.createColumnContainer( composite, 1, 1 ), Messages
                .getString( "ConnectionsPreferencePage.Krb5Settings" ), 1 ); //$NON-NLS-1$

        // "Use KRB5 system properties" checkbox
        boolean useKrb5SystemProperties = preferences
            .getBoolean( ConnectionCoreConstants.PREFERENCE_USE_KRB5_SYSTEM_PROPERTIES );
        useKrb5SystemPropertiesButton = BaseWidgetUtils.createCheckbox( krb5SettingsGroup, Messages
            .getString( "ConnectionsPreferencePage.UseKrb5SystemProperties" ), 1 ); //$NON-NLS-1$
        useKrb5SystemPropertiesButton.setToolTipText( Messages
            .getString( "ConnectionsPreferencePage.UseKrb5SystemPropertiesTooltip" ) ); //$NON-NLS-1$
        useKrb5SystemPropertiesButton.setSelection( useKrb5SystemProperties );

        // KRB5 login-module text field + auto-detected default note
        krb5LoginModuleLabel = BaseWidgetUtils.createLabel( krb5SettingsGroup, Messages
            .getString( "ConnectionsPreferencePage.Krb5LoginModule" ), 1 ); //$NON-NLS-1$
        String krb5LoginModule = preferences.getString( ConnectionCoreConstants.PREFERENCE_KRB5_LOGIN_MODULE );
        String defaultKrb5LoginModule = preferences
            .getDefaultString( ConnectionCoreConstants.PREFERENCE_KRB5_LOGIN_MODULE );
        String krb5LoginModuleNote = NLS.bind( Messages
            .getString( "ConnectionsPreferencePage.SystemDetectedContextFactory" ), defaultKrb5LoginModule ); //$NON-NLS-1$
        krb5LoginModuleText = BaseWidgetUtils.createText( krb5SettingsGroup, krb5LoginModule, 1 );
        krb5LoginModuleNoteLabel = BaseWidgetUtils.createWrappedLabel( krb5SettingsGroup, krb5LoginModuleNote, 1 );

        // ── SELECTION LISTENER ────────────────────────────────────────────────────
        // Re-validate (enable/disable the text field) whenever the checkbox toggles.
        // ──────────────────────────────────────────────────────────────────────────
        useKrb5SystemPropertiesButton.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                validate();
            }
        } );

        validate();

        return composite;
    }


    // ── VALIDATE ──────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the KRB5 login-module label, text field, and note label
     * based on whether "Use KRB5 system properties" is checked.
     *
     * <p>When system properties are used we don't need the manual class name, so
     * we grey out those controls to make it clear they have no effect.</p>
     */
    private void validate()
    {
        // ── DISABLE WHEN SYSTEM PROPERTIES ARE ACTIVE ─────────────────────────────
        // If the checkbox is ticked the JVM handles KRB5 config — the text field
        // would be ignored, so disable it to avoid confusion.
        // ──────────────────────────────────────────────────────────────────────────
        krb5LoginModuleLabel.setEnabled( !useKrb5SystemPropertiesButton.getSelection() );
        krb5LoginModuleText.setEnabled( !useKrb5SystemPropertiesButton.getSelection() );
        krb5LoginModuleNoteLabel.setEnabled( !useKrb5SystemPropertiesButton.getSelection() );
    }


    // ── PERFORM DEFAULTS ──────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Resets both settings to their platform defaults and re-renders the page.
     */
    @Override
    protected void performDefaults()
    {
        // ── RESTORE FACTORY DEFAULTS ──────────────────────────────────────────────
        krb5LoginModuleText.setText( ConnectionCorePlugin.getDefault().getPluginPreferences().getDefaultString(
            ConnectionCoreConstants.PREFERENCE_KRB5_LOGIN_MODULE ) );
        useKrb5SystemPropertiesButton.setSelection( ConnectionCorePlugin.getDefault().getPluginPreferences()
            .getDefaultBoolean( ConnectionCoreConstants.PREFERENCE_USE_KRB5_SYSTEM_PROPERTIES ) );

        super.performDefaults();
    }


    // ── PERFORM OK ────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Persists the current field values to the connection core plugin preferences
     * and saves them to disk.
     *
     * @return Always {@code true}.
     */
    public boolean performOk()
    {
        // ── SAVE BOTH SETTINGS ────────────────────────────────────────────────────
        ConnectionCorePlugin.getDefault().getPluginPreferences().setValue(
            ConnectionCoreConstants.PREFERENCE_KRB5_LOGIN_MODULE, krb5LoginModuleText.getText() );
        ConnectionCorePlugin.getDefault().getPluginPreferences()
            .setValue( ConnectionCoreConstants.PREFERENCE_USE_KRB5_SYSTEM_PROPERTIES,
                useKrb5SystemPropertiesButton.getSelection() );

        ConnectionCorePlugin.getDefault().savePluginPreferences();

        return true;
    }
}
