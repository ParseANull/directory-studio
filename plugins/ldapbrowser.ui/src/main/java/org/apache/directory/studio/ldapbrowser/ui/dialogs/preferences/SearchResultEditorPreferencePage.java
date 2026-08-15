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
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


// ── CLASS: SearchResultEditorPreferencePage — PALPATINE CONFIGURES THE RESULTS DISPLAY ──
// Palpatine's intelligence apparatus didn't just capture search results — it also
// dictated how they were presented to analysts: should the entry's DN appear as the
// first column?  Should DNs be clickable hyperlinks?  How many results can be
// sorted or filtered before performance degrades?
// This preference page is that display configuration: show/hide the DN column,
// toggle DN hyperlinks, and set the sort/filter ceiling.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse preference page for configuring how the Search Result Editor displays results.
 * Controls three settings: whether the DN appears as the first column, whether DNs
 * render as clickable links, and the maximum number of entries that can be sorted or
 * filtered before the operation is skipped for performance reasons.
 * Think of this page as Palpatine configuring the analyst's result display — what
 * gets shown, how it's formatted, and what the system refuses to do past a limit.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultEditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    /** The show Dn button. */
    private Button showDnButton;

    /** The show links button. */
    private Button showLinksButton;

    /** The sort/filter limit text */
    private Text sortFilterLimitText;


    // ── PALPATINE OPENS THE RESULTS DISPLAY CONFIGURATION ROOM ───────────────
    // The room is labeled "Search Result Editor" and the description board
    // explains what the analysts will be configuring when they step inside.
    // We wire the page to the BrowserUI preference store and set its title.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the preference page with a localized title and description,
     * wired to the BrowserUI plugin's preference store.
     * Eclipse calls this when the user navigates to this page in the Preferences dialog.
     *
     * <p>For example — Palpatine labels the display configuration room:</p>
     * <pre>
     *   title: "Search Result Editor"
     *   description: "General settings for the search result editor"
     * </pre>
     */
    public SearchResultEditorPreferencePage()
    {
        super( Messages.getString( "SearchResultEditorPreferencePage.ResultEditor" ) ); //$NON-NLS-1$
        super.setPreferenceStore( BrowserUIPlugin.getDefault().getPreferenceStore() );
        super.setDescription( Messages.getString( "SearchResultEditorPreferencePage.GeneralSettings" ) ); //$NON-NLS-1$
    }


    // ── AIDE CONFIRMS READINESS ───────────────────────────────────────────────
    // Required by the interface; no workbench initialization needed here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Required by {@link IWorkbenchPreferencePage} but we have nothing to do here.
     * All setup happens in the constructor and {@link #createContents}.
     *
     * @param workbench  The Eclipse workbench; ignored.
     */
    public void init( IWorkbench workbench )
    {
    }


    // ── PALPATINE LAYS OUT THE DISPLAY CONFIGURATION CONTROLS ────────────────
    // Three controls on the analyst's display panel: show-DN checkbox, show-links
    // checkbox, and a numeric sort/filter limit field guarded by a verify listener
    // so analysts can't type letters into a number field.
    // Current preference values are read at build time to initialize each control.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the preference page UI: two checkboxes and a numeric text field.
     * Each widget is initialized from the current preference store value.
     * The sort/filter limit field rejects non-digit input via a verify listener
     * but doesn't call validate() — an empty field is handled gracefully in
     * {@link #performOk()} by falling back to the stored value.
     *
     * <p>For example — Palpatine configures the results display panel:</p>
     * <pre>
     *   [✓] Show DN as first column
     *   [✓] Show DN as hyperlink
     *   Sort/filter limit: [10000]
     * </pre>
     *
     * @param parent  The parent composite provided by Eclipse's preference dialog.
     * @return        The root composite we constructed.
     */
    protected Control createContents( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 2, 1 );

        BaseWidgetUtils.createSpacer( composite, 2 );

        // Show Dn
        showDnButton = BaseWidgetUtils.createCheckbox( composite, Messages
            .getString( "SearchResultEditorPreferencePage.DNAsFirst" ), 2 ); //$NON-NLS-1$
        showDnButton.setSelection( getPreferenceStore().getBoolean(
            BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SHOW_DN ) );

        // Show DN As Link
        showLinksButton = BaseWidgetUtils.createCheckbox( composite, Messages
            .getString( "SearchResultEditorPreferencePage.DNAsLink" ), 2 ); //$NON-NLS-1$
        showLinksButton.setSelection( getPreferenceStore().getBoolean(
            BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SHOW_LINKS ) );

        // Sort/Filter Limit
        String sortFilterLimitTooltip = Messages.getString( "SearchResultEditorPreferencePage.SortFilterLimitToolTip" ); //$NON-NLS-1$
        Label sortFilterLimitLabel = BaseWidgetUtils.createLabel( composite, Messages
            .getString( "SearchResultEditorPreferencePage.SortFilterLimitColon" ), 1 ); //$NON-NLS-1$
        sortFilterLimitLabel.setToolTipText( sortFilterLimitTooltip );
        sortFilterLimitText = BaseWidgetUtils.createText( composite, "" + getPreferenceStore().getInt(
            BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SORT_FILTER_LIMIT ), 5, 1 ); //$NON-NLS-1$
        sortFilterLimitText.setToolTipText( sortFilterLimitTooltip );
        sortFilterLimitText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
                {
                    e.doit = false;
                }
            }
        } );

        applyDialogFont( composite );
        return composite;
    }


    // ── PALPATINE SAVES THE UPDATED DISPLAY DIRECTIVES ───────────────────────
    // The three configuration values are written to the preference store.
    // The sort/filter limit parses the text field defensively — if parsing fails
    // we keep the previously stored value rather than storing garbage.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Saves all three display settings to the preference store.
     * The sort/filter limit value is parsed from the text field; if the field
     * is empty or contains a non-integer we silently keep the previously stored
     * value rather than crashing or storing zero.
     *
     * <p>For example — Palpatine saves the analyst display configuration:</p>
     * <pre>
     *   showDn=true, showLinks=false, sortFilterLimit=5000 → stored
     * </pre>
     *
     * @return  Always {@code true}.
     */
    public boolean performOk()
    {
        // Show Dn
        getPreferenceStore().setValue( BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SHOW_DN,
            showDnButton.getSelection() );

        // Show DN As Link
        getPreferenceStore().setValue( BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SHOW_LINKS,
            showLinksButton.getSelection() );

        // Sort/Filter Limit
        int sortFilterLimit = getPreferenceStore().getInt(
            BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SORT_FILTER_LIMIT );
        try
        {
            sortFilterLimit = Integer.parseInt( sortFilterLimitText.getText().trim() );
        }
        catch ( NumberFormatException nfe )
        {
        }
        getPreferenceStore().setValue( BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SORT_FILTER_LIMIT,
            sortFilterLimit );

        return true;
    }


    // ── PALPATINE REVERTS THE DISPLAY TO FACTORY DEFAULTS ────────────────────
    // When the custom display configuration is revoked, the checkboxes snap back
    // to their factory defaults; the sort/filter limit field is handled by the
    // superclass's performDefaults call.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Resets the two checkboxes to their default preference values.
     * The sort/filter limit isn't explicitly reset here — the superclass handles
     * it by restoring the preference store's default value.
     *
     * <p>For example — Palpatine reverts the analyst display to factory settings:</p>
     * <pre>
     *   showDn default=true → checkbox checked
     *   showLinks default=true → checkbox checked
     * </pre>
     */
    protected void performDefaults()
    {
        showDnButton.setSelection( getPreferenceStore().getDefaultBoolean(
            BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SHOW_DN ) );
        showLinksButton.setSelection( getPreferenceStore().getDefaultBoolean(
            BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SHOW_LINKS ) );
        super.performDefaults();
    }

}
