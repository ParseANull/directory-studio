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


// ── CLASS: SearchLogsPreferencePage — PALPATINE CONFIGURES SEARCH MONITORING ─
// Palpatine's intelligence apparatus separately tracked every search request
// made against the galactic directory — who was looking for what, and what
// results they received.  This preference page configures the equivalent:
// whether to log outgoing search requests, whether to log the entries that
// come back, and how large/numerous the rolling log files should be.
// Two independent enable checkboxes because sometimes you care about the
// request traffic but not the full response payload, or vice versa.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse preference page for configuring the Search Logs view behavior.
 * Controls whether search request traffic and/or search result entries are
 * logged, and how log file rotation works (count × size in KB).
 * Think of this page as Palpatine's search-monitoring configuration: decide
 * which traffic gets recorded and how long to retain the files.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchLogsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{

    private Button enableSearchRequestLogging;
    private Button enableSearchResultEntryLogging;
    private Text logFileCountText;
    private Text logFileSizeText;

    // ── PALPATINE OPENS THE SEARCH MONITORING ROOM ───────────────────────────
    // The search monitoring station gets its own labeled door and description
    // plaque so officers know exactly what they're configuring when they enter.
    // We wire the page to the BrowserUI preference store and set title/description.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the preference page with a localized title and description,
     * wired to the BrowserUI plugin's preference store.
     * Eclipse calls this when the user navigates to this page in the Preferences dialog.
     *
     * <p>For example — Palpatine labels the search monitoring room:</p>
     * <pre>
     *   title: "Search Logs"
     *   description: "General settings for the Search Logs view"
     * </pre>
     */
    public SearchLogsPreferencePage()
    {
        super( Messages.getString( "SearchLogsPreferencePage.SearchLogs" ) ); //$NON-NLS-1$
        super.setPreferenceStore( BrowserUIPlugin.getDefault().getPreferenceStore() );
        super.setDescription( Messages.getString( "SearchLogsPreferencePage.GeneralSettings" ) ); //$NON-NLS-1$
    }


    // ── AIDE CONFIRMS THE ROOM IS READY ──────────────────────────────────────
    // Required by the interface; we have nothing to initialize before the UI is
    // built, so this is intentionally empty.
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


    // ── PALPATINE LAYS OUT THE SEARCH MONITORING CONTROLS ────────────────────
    // The monitoring station has two independent enable switches (requests and
    // results) plus a rotation panel.  Both numeric fields guard against non-digit
    // input and fire validate() so the OK button stays grayed out for bad values.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the preference page UI: two checkboxes (enable request logging,
     * enable result-entry logging) and a log file rotation group with count
     * and size text fields.
     * Numeric-only verify listeners prevent the user from typing non-digits
     * or a leading zero in the rotation fields.
     *
     * <p>For example — Palpatine configures the monitoring station:</p>
     * <pre>
     *   [✓] Enable search request logging
     *   [ ] Enable search result entry logging
     *   Log file rotation: Use [10] log files each [100] KB
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
        enableSearchRequestLogging = BaseWidgetUtils.createCheckbox( composite, Messages
            .getString( "SearchLogsPreferencePage.EnableRequestLogs" ), 1 ); //$NON-NLS-1$
        enableSearchResultEntryLogging = BaseWidgetUtils.createCheckbox( composite, Messages
            .getString( "SearchLogsPreferencePage.EnableResultLogs" ), 1 ); //$NON-NLS-1$

        Group rotateGroup = BaseWidgetUtils.createGroup( BaseWidgetUtils.createColumnContainer( composite, 1, 1 ),
            Messages.getString( "SearchLogsPreferencePage.LogFileRotation" ), 1 ); //$NON-NLS-1$
        Composite rotateComposite = BaseWidgetUtils.createColumnContainer( rotateGroup, 5, 1 );
        BaseWidgetUtils.createLabel( rotateComposite, Messages.getString( "SearchLogsPreferencePage.Use" ), 1 ); //$NON-NLS-1$
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
        BaseWidgetUtils.createLabel( rotateComposite, Messages.getString( "SearchLogsPreferencePage.LogFilesEach" ), //$NON-NLS-1$
            1 );
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
        BaseWidgetUtils.createLabel( rotateComposite, Messages.getString( "SearchLogsPreferencePage.KB" ), 1 ); //$NON-NLS-1$

        setValues();

        applyDialogFont( composite );
        return composite;
    }


    // ── PALPATINE READS THE CURRENT MONITORING CONFIGURATION ─────────────────
    // Before the briefing, an aide loads the current monitoring configuration
    // into the Emperor's console so everything starts from the actual live state.
    // We populate all four widgets from the ConnectionCorePlugin's stored values.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates all UI widgets from currently stored preference values.
     * Called once from {@link #createContents} and again after "Restore Defaults."
     *
     * <p>For example — Palpatine reads the live monitoring state:</p>
     * <pre>
     *   requestLogging=true → checkbox checked
     *   resultLogging=false → checkbox unchecked
     *   count=10, size=100 → fields populated
     * </pre>
     */
    private void setValues()
    {
        enableSearchRequestLogging.setSelection( ConnectionCorePlugin.getDefault().isSearchRequestLogsEnabled() );
        enableSearchResultEntryLogging
            .setSelection( ConnectionCorePlugin.getDefault().isSearchResultEntryLogsEnabled() );
        logFileCountText.setText( "" + ConnectionCorePlugin.getDefault().getSearchLogsFileCount() );
        logFileSizeText.setText( "" + ConnectionCorePlugin.getDefault().getSearchLogsFileSize() );
    }


    // ── PALPATINE CHECKS THE ROTATION PARAMETERS ARE SENSIBLE ────────────────
    // Palpatine cross-checks the rotation parameters before approving — a zero
    // or blank file count would mean no rotation at all, which would fill the
    // disk and make the intelligence network useless.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Validates that both the file count and file size fields contain valid
     * positive integers, and enables or disables the page accordingly.
     * Called by the modify listeners wired to both text fields.
     *
     * <p>For example — Palpatine checks the numbers before signing off:</p>
     * <pre>
     *   count="5", size="500" → setValid(true)
     *   count=""              → setValid(false) → OK grayed out
     * </pre>
     */
    public void validate()
    {
        setValid( logFileCountText.getText().matches( "[0-9]+" ) && logFileSizeText.getText().matches( "[0-9]+" ) );
    }


    // ── PALPATINE TRANSMITS THE UPDATED MONITORING DIRECTIVES ─────────────────
    // The finalized configuration is transmitted to the instance-scope preference
    // store and flushed so the Search Logs view picks up the new settings on next
    // use without requiring a restart.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Saves all four monitoring settings to the instance-scoped Eclipse preferences
     * and flushes them to disk.
     *
     * <p>For example — Palpatine transmits finalized monitoring directives:</p>
     * <pre>
     *   requestEnabled=true, resultEnabled=false, count=5, size=500 → stored + flushed
     * </pre>
     *
     * @return  Always {@code true}.
     */
    public boolean performOk()
    {
        IEclipsePreferences instancePreferences = ConnectionCorePlugin.getDefault().getInstanceScopePreferences();
        instancePreferences.putBoolean( ConnectionCoreConstants.PREFERENCE_SEARCHREQUESTLOGS_ENABLE,
            enableSearchRequestLogging.getSelection() );
        instancePreferences.putBoolean( ConnectionCoreConstants.PREFERENCE_SEARCHRESULTENTRYLOGS_ENABLE,
            enableSearchResultEntryLogging.getSelection() );
        instancePreferences.putInt( ConnectionCoreConstants.PREFERENCE_SEARCHLOGS_FILE_COUNT,
            Integer.parseInt( logFileCountText.getText() ) );
        instancePreferences.putInt( ConnectionCoreConstants.PREFERENCE_SEARCHLOGS_FILE_SIZE,
            Integer.parseInt( logFileSizeText.getText() ) );
        ConnectionCorePlugin.getDefault().flushInstanceScopePreferences();
        return true;
    }


    // ── PALPATINE'S MONITORING OVERRIDES ARE REVOKED ─────────────────────────
    // When the custom monitoring configuration is revoked, Palpatine removes all
    // instance-scope overrides so the system falls back to plugin defaults.
    // We remove all four keys, flush, and repopulate the UI from the defaults.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes all instance-scope overrides for search logging preferences,
     * flushes the store, then repopulates the UI from the resulting defaults.
     *
     * <p>For example — monitoring overrides revoked and defaults restored:</p>
     * <pre>
     *   four instance-scope keys removed → flushed →
     *   setValues() reads plugin defaults → UI shows default configuration
     * </pre>
     */
    protected void performDefaults()
    {
        IEclipsePreferences instancePreferences = ConnectionCorePlugin.getDefault().getInstanceScopePreferences();
        instancePreferences.remove( ConnectionCoreConstants.PREFERENCE_SEARCHREQUESTLOGS_ENABLE );
        instancePreferences.remove( ConnectionCoreConstants.PREFERENCE_SEARCHRESULTENTRYLOGS_ENABLE );
        instancePreferences.remove( ConnectionCoreConstants.PREFERENCE_SEARCHLOGS_FILE_COUNT );
        instancePreferences.remove( ConnectionCoreConstants.PREFERENCE_SEARCHLOGS_FILE_SIZE );
        ConnectionCorePlugin.getDefault().flushInstanceScopePreferences();
        setValues();
        super.performDefaults();
    }

}
