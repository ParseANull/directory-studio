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

package org.apache.directory.studio.ldapbrowser.ui.dialogs.preferences;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.core.ConnectionCoreConstants;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


// ── CLASS: ModificationLogsPreferencePage — PALPATINE CONFIGURES THE SURVEILLANCE SYSTEM ──
// Palpatine's Order 66 didn't just direct who to hunt — it also configured how
// the Empire tracked every LDAP modification: which attributes to mask (passwords,
// sensitive data), how many rolling log files to keep, and how large each file
// could grow before rotating.  This preference page is that configuration panel:
// flip the switch to enable modification logging, redact sensitive attributes,
// and tune the file rotation so the logs don't eat the disk.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse preference page for configuring the Modification Logs view behavior.
 * Controls whether modifications are logged at all, which attributes are masked
 * in the log (e.g. {@code userPassword}), and how log file rotation works
 * (count × size limit).
 * Think of this page as Palpatine's surveillance configuration: decide what gets
 * recorded, what gets redacted, and how long the records are kept.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ModificationLogsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{

    private Button enableModificationLogging;
    private Text logFileCountText;
    private Text logFileSizeText;
    private Text maskedAttributesText;

    // ── PALPATINE OPENS THE SURVEILLANCE CONFIGURATION PANEL ─────────────────
    // The Emperor doesn't want a generic title on his surveillance room door —
    // he wants it labeled "Modification Logs" and with a description that tells
    // every officer exactly what they're configuring when they walk in.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the preference page with a localized title and description,
     * and wires it to the BrowserUI plugin's preference store.
     * Eclipse calls this constructor when the user navigates to this page in
     * the Preferences dialog.
     *
     * <p>For example — Palpatine labels the room:</p>
     * <pre>
     *   title: "Modification Logs"
     *   description: "General settings for the Modification Logs view"
     * </pre>
     */
    public ModificationLogsPreferencePage()
    {
        super( Messages.getString( "ModificationLogsPreferencePage.ModificationLogs" ) ); //$NON-NLS-1$
        super.setPreferenceStore( BrowserUIPlugin.getDefault().getPreferenceStore() );
        super.setDescription( Messages.getString( "ModificationLogsPreferencePage.GeneralSettingsModificationLogs" ) ); //$NON-NLS-1$
    }


    // ── PALPATINE'S AIDE CONFIRMS READINESS ───────────────────────────────────
    // An aide pokes their head in — "Ready, my Lord?" — but there's nothing to
    // do in this room before the meeting starts; all setup is in the UI build.
    // IWorkbenchPreferencePage requires this method but we have no workbench
    // initialization to perform, so we leave it empty.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Required by {@link IWorkbenchPreferencePage} but we have nothing to do here.
     * All initialization happens in the constructor and {@link #createContents}.
     *
     * @param workbench  The Eclipse workbench; ignored.
     */
    public void init( IWorkbench workbench )
    {
    }


    // ── PALPATINE LAYS OUT THE SURVEILLANCE CONFIGURATION CONTROLS ────────────
    // The surveillance room has three stations: a master enable switch, a list of
    // attributes to redact, and a log rotation panel specifying how many files to
    // keep and at what size they roll over.  Each station has its own widget,
    // wired to validate() so the OK button only lights up when inputs are legal.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the preference page UI: an enable checkbox, a masked-attributes
     * text field, and a log file rotation group with count and size text fields.
     * Numeric-only verification listeners guard the count and size fields.
     * {@link #setValues()} is called at the end to populate the widgets from
     * the currently stored preferences.
     *
     * <p>For example — Palpatine configures the surveillance station:</p>
     * <pre>
     *   [✓] Enable modification logging
     *   Masked attributes: userPassword, unicodePwd
     *   Log file rotation: Use [5] log files each [1000] KB
     * </pre>
     *
     * @param parent  The parent composite provided by Eclipse's preference dialog.
     * @return        The root composite of our UI.
     */
    protected Control createContents( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );
        enableModificationLogging = BaseWidgetUtils.createCheckbox( composite, Messages
            .getString( "ModificationLogsPreferencePage.EnableModificationLogs" ), 1 ); //$NON-NLS-1$

        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );

        Group maskedAttributesGroup = BaseWidgetUtils.createGroup( BaseWidgetUtils.createColumnContainer( composite, 1,
            1 ), Messages.getString( "ModificationLogsPreferencePage.MaskedAttributes" ), 1 ); //$NON-NLS-1$
        Composite maskedAttributesComposite = BaseWidgetUtils.createColumnContainer( maskedAttributesGroup, 1, 1 );
        maskedAttributesText = BaseWidgetUtils.createText( maskedAttributesComposite, "", 1 ); //$NON-NLS-1$
        String maskedAttributesHelp = Messages.getString( "ModificationLogsPreferencePage.CommaSeparatedList" ); //$NON-NLS-1$
        BaseWidgetUtils.createWrappedLabel( maskedAttributesComposite, maskedAttributesHelp, 1 );

        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );

        Group rotateGroup = BaseWidgetUtils.createGroup( BaseWidgetUtils.createColumnContainer( composite, 1, 1 ),
            Messages.getString( "ModificationLogsPreferencePage.LogFileRotation" ), 1 ); //$NON-NLS-1$
        Composite rotateComposite = BaseWidgetUtils.createColumnContainer( rotateGroup, 5, 1 );
        BaseWidgetUtils.createLabel( rotateComposite, Messages.getString( "ModificationLogsPreferencePage.Use" ), 1 ); //$NON-NLS-1$
        logFileCountText = BaseWidgetUtils.createText( rotateComposite, "", 3, 1 ); //$NON-NLS-1$
        logFileCountText.addVerifyListener( e -> {
            if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
            {
                e.doit = false;
            }
            if ( "".equals( logFileCountText.getText() ) && e.text.matches( "[0]" ) ) //$NON-NLS-1$ //$NON-NLS-2$
            {
                e.doit = false;
            }
        } );
        logFileCountText.addModifyListener( e -> validate() );
        BaseWidgetUtils.createLabel( rotateComposite, Messages
            .getString( "ModificationLogsPreferencePage.LogFilesEach" ), 1 ); //$NON-NLS-1$
        logFileSizeText = BaseWidgetUtils.createText( rotateComposite, "", 5, 1 ); //$NON-NLS-1$
        logFileSizeText.addVerifyListener( e -> {
            if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
            {
                e.doit = false;
            }
            if ( "".equals( logFileSizeText.getText() ) && e.text.matches( "[0]" ) ) //$NON-NLS-1$ //$NON-NLS-2$
            {
                e.doit = false;
            }
        } );
        logFileSizeText.addModifyListener( e -> validate() );
        BaseWidgetUtils.createLabel( rotateComposite, Messages.getString( "ModificationLogsPreferencePage.KB" ), 1 ); //$NON-NLS-1$

        setValues();

        applyDialogFont( composite );
        return composite;
    }


    // ── PALPATINE LOADS THE CURRENT SURVEILLANCE SETTINGS ────────────────────
    // Before the briefing, an aide reads the current surveillance configuration
    // into the Emperor's notes so the discussion starts from the real current
    // state rather than guesswork.
    // We populate all four widgets from the ConnectionCorePlugin's stored prefs.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates all UI widgets from the currently stored preference values.
     * Called once during {@link #createContents} and again after a "Restore Defaults"
     * to reset the display without rebuilding the whole UI.
     *
     * <p>For example — Palpatine reads from the current configuration:</p>
     * <pre>
     *   logging enabled: true → checkbox checked
     *   masked attributes: "userPassword" → text field populated
     *   log file count: 5, size: 1000 → rotation fields populated
     * </pre>
     */
    private void setValues()
    {
        enableModificationLogging.setSelection( ConnectionCorePlugin.getDefault().isModificationLogsEnabled() );
        maskedAttributesText.setText( ConnectionCorePlugin.getDefault().getMModificationLogsMaskedAttributes() );
        logFileCountText.setText( "" + ConnectionCorePlugin.getDefault().getModificationLogsFileCount() );
        logFileSizeText.setText( "" + ConnectionCorePlugin.getDefault().getModificationLogsFileSize() );
    }


    // ── PALPATINE CHECKS THE INPUTS ARE IN RANGE ─────────────────────────────
    // Before signing off on the surveillance directives, the Emperor checks that
    // the file count and size fields contain actual numbers — a blank or zero
    // would break log rotation entirely, which would be embarrassing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Validates that both the file count and file size fields contain valid
     * positive integers, then enables or disables the page (and its OK button)
     * accordingly.
     * Called by the modify listeners on both text fields.
     *
     * <p>For example — Palpatine checks the numbers before approving:</p>
     * <pre>
     *   logFileCountText = "5", logFileSizeText = "1000" → setValid(true)
     *   logFileCountText = "" → setValid(false) → OK grayed out
     * </pre>
     */
    public void validate()
    {
        setValid( logFileCountText.getText().matches( "[0-9]+" ) && logFileSizeText.getText().matches( "[0-9]+" ) );
    }


    // ── PALPATINE TRANSMITS THE UPDATED SURVEILLANCE DIRECTIVES ──────────────
    // Once the briefing concludes and the commander approves, Palpatine transmits
    // all four configuration values to the instance-scoped preference store and
    // flushes it to disk so the changes survive a restart.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Saves the current UI values to the instance-scoped Eclipse preferences and
     * flushes them to disk.
     * Instance-scope preferences survive across IDE restarts and are per-workspace,
     * which is what we want for connection-specific logging behavior.
     *
     * <p>For example — Palpatine transmits the finalized directives:</p>
     * <pre>
     *   enabled=true, masked="userPassword", count=5, size=1000 → stored + flushed
     * </pre>
     *
     * @return  Always {@code true}; we always accept the save.
     */
    public boolean performOk()
    {
        IEclipsePreferences instancePreferences = ConnectionCorePlugin.getDefault().getInstanceScopePreferences();
        instancePreferences.putBoolean( ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_ENABLE,
            enableModificationLogging.getSelection() );
        instancePreferences.put( ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_MASKED_ATTRIBUTES,
            maskedAttributesText.getText() );
        instancePreferences.putInt( ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_FILE_COUNT,
            Integer.parseInt( logFileCountText.getText() ) );
        instancePreferences.putInt( ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_FILE_SIZE,
            Integer.parseInt( logFileSizeText.getText() ) );
        ConnectionCorePlugin.getDefault().flushInstanceScopePreferences();
        return true;
    }


    // ── PALPATINE REVOKES HIS CUSTOM SURVEILLANCE DIRECTIVES ─────────────────
    // When the custom surveillance configuration is revoked, Palpatine removes
    // all his overrides from the instance-scope store and the system falls back
    // to whatever defaults the plugin declares.
    // We remove all four instance-scope keys, flush, and repopulate the UI from
    // the now-reverted (default) values.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes all instance-scope preference overrides for modification logging,
     * flushes the store, then repopulates the UI from the resulting default values.
     * After this the page shows exactly what the plugin ships with.
     *
     * <p>For example — Palpatine's surveillance overrides are revoked:</p>
     * <pre>
     *   custom keys removed → flushed → setValues() reads plugin defaults →
     *   UI shows: logging=false, masked="", count=10, size=100
     * </pre>
     */
    protected void performDefaults()
    {
        IEclipsePreferences instancePreferences = ConnectionCorePlugin.getDefault().getInstanceScopePreferences();
        instancePreferences.remove( ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_ENABLE );
        instancePreferences.remove( ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_MASKED_ATTRIBUTES );
        instancePreferences.remove( ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_FILE_COUNT );
        instancePreferences.remove( ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_FILE_SIZE );
        ConnectionCorePlugin.getDefault().flushInstanceScopePreferences();
        setValues();
        super.performDefaults();
    }

}
