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

package org.apache.directory.studio.ldapbrowser.common.dialogs.preferences;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


// ── CLASS: BrowserPreferencePage — GENERAL DODONNA'S TACTICAL BRIEFING ───────
// In the Yavin 4 briefing room, General Dodonna stands at the holotable and
// walks every pilot through the tactical parameters for the Death Star run:
// how to label your targeting display (DN, RDN, or just the value), how many
// entries to fold into a page before paginating, whether to pre-expand base
// entries, and whether to probe each node for children before rendering the tree.
// Get these settings right and the pilots navigate efficiently; get them wrong
// and they're flying blind through the trench.  This page is that briefing.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Eclipse preference page for controlling how the LDAP browser tree behaves:
 * entry label format, abbreviation limits, folding/pagination thresholds, and
 * whether the browser checks for child entries proactively.
 * Think of this class as General Dodonna's briefing — the navigation parameters
 * that every pilot (browser view) follows on every run.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{

    private static final String DN = Messages.getString( "BrowserPreferencePage.DN" ); //$NON-NLS-1$

    private static final String RDN = Messages.getString( "BrowserPreferencePage.RDN" ); //$NON-NLS-1$

    private static final String RDN_VALUE = Messages.getString( "BrowserPreferencePage.RDNValue" ); //$NON-NLS-1$

    private Combo entryLabelCombo;

    private Button entryAbbreviateButton;

    private Text entryAbbreviateMaxLengthText;

    private Combo searchResultLabelCombo;

    private Button searchResultAbbreviateButton;

    private Text searchResultAbbreviateMaxLengthText;

    private Button enableFoldingButton;

    private Label foldingSizeLabel;

    private Text foldingSizeText;

    private Button expandBaseEntriesButton;;

    private Button checkForChildrenButton;


    // ── DODONNA CALLS THE BRIEFING TO ORDER ──────────────────────────────────────
    // General Dodonna steps up to the podium, announces "Browser — General
    // Settings," and points the room to the right operational manual so that
    // any changes written here end up in the correct preferences file.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the page, sets its title and description, and wires it to the
     * correct preference store.  Eclipse calls this when the user opens the
     * Browser node in the preference tree.
     */
    public BrowserPreferencePage()
    {
        super( Messages.getString( "BrowserPreferencePage.Browser" ) ); //$NON-NLS-1$
        super.setPreferenceStore( BrowserCommonActivator.getDefault().getPreferenceStore() );
        super.setDescription( Messages.getString( "BrowserPreferencePage.GeneralSettings" ) ); //$NON-NLS-1$
    }


    // ── DODONNA ACKNOWLEDGES THE ROOM ─────────────────────────────────────────────
    // Dodonna glances at the assembled pilots, nods, and gets on with it.
    // Nothing actionable — just satisfying the interface contract.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Required by {@link IWorkbenchPreferencePage} — not used here because we
     * don't need anything from the workbench at construction time.
     *
     * @param workbench  The Eclipse workbench instance — not used.
     */
    public void init( IWorkbench workbench )
    {
    }


    // ── DODONNA WALKS THE PILOTS THROUGH THE TACTICAL DISPLAY ────────────────────
    // Dodonna gestures at the holotable: "Entry labels — use DN, RDN, or just the
    // value?  Abbreviate if longer than N characters?  Same for search results.
    // Folding — how many children before we paginate?  Expand base entries on
    // connection?  Check for children proactively?"  Each section is its own
    // Group widget so pilots can focus on one concern at a time.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the full preference page UI: three groups (entry label, search result
     * label, folding) plus two standalone checkboxes (expand base entries, check for
     * children).  Pre-fills every control from the current stored preference values.
     *
     * <p>For example — Dodonna's briefing panels:</p>
     * <pre>
     *   Entry Label:         [DN ▼]  [x] Limit to 50 chars
     *   Search Result Label: [RDN ▼] [ ] Limit
     *   Folding:             [x] Enable  Size: [100]
     *   [x] Expand base entries on connect
     *   [x] Check for children before rendering
     * </pre>
     *
     * @param parent  The parent composite provided by Eclipse.
     * @return        The composite containing all browser preference widgets.
     */
    protected Control createContents( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );
        Group entryLabelGroup = BaseWidgetUtils.createGroup( BaseWidgetUtils.createColumnContainer( composite, 1, 1 ),
            Messages.getString( "BrowserPreferencePage.EntryLabel" ), 1 ); //$NON-NLS-1$

        Composite entryLabelComposite = BaseWidgetUtils.createColumnContainer( entryLabelGroup, 3, 1 );
        BaseWidgetUtils.createLabel( entryLabelComposite,
            Messages.getString( "BrowserPreferencePage.UseAsEntryLabel1" ), 1 ); //$NON-NLS-1$
        entryLabelCombo = BaseWidgetUtils.createCombo( entryLabelComposite, new String[]
            { DN, RDN, RDN_VALUE }, 0, 1 );
        entryLabelCombo.setLayoutData( new GridData() );
        entryLabelCombo
            .select( getPreferenceStore().getInt( BrowserCommonConstants.PREFERENCE_BROWSER_ENTRY_LABEL ) == BrowserCommonConstants.SHOW_RDN_VALUE ? 2
                : getPreferenceStore().getInt( BrowserCommonConstants.PREFERENCE_BROWSER_ENTRY_LABEL ) == BrowserCommonConstants.SHOW_RDN ? 1
                    : 0 );
        BaseWidgetUtils.createLabel( entryLabelComposite,
            Messages.getString( "BrowserPreferencePage.UseAsEntryLabel2" ), 1 ); //$NON-NLS-1$

        Composite entryAbbreviateComposite = BaseWidgetUtils.createColumnContainer( entryLabelGroup, 3, 1 );
        entryAbbreviateButton = BaseWidgetUtils.createCheckbox( entryAbbreviateComposite, Messages
            .getString( "BrowserPreferencePage.LimitLabelLength1" ), 1 ); //$NON-NLS-1$
        entryAbbreviateButton.setSelection( getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_ENTRY_ABBREVIATE ) );
        entryAbbreviateButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                updateEnabled();
            }
        } );
        entryAbbreviateMaxLengthText = BaseWidgetUtils.createText( entryAbbreviateComposite, getPreferenceStore()
            .getString( BrowserCommonConstants.PREFERENCE_BROWSER_ENTRY_ABBREVIATE_MAX_LENGTH ), 3, 1 );
        entryAbbreviateMaxLengthText.setEnabled( entryAbbreviateButton.getSelection() );
        entryAbbreviateMaxLengthText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
                {
                    e.doit = false;
                }
                if ( "".equals( entryAbbreviateMaxLengthText.getText() ) && e.text.matches( "[0]" ) ) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    e.doit = false;
                }
            }
        } );
        BaseWidgetUtils.createLabel( entryAbbreviateComposite, Messages
            .getString( "BrowserPreferencePage.LimitLabelLength2" ), 1 ); //$NON-NLS-1$

        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );
        Group searchResultLabelGroup = BaseWidgetUtils.createGroup( BaseWidgetUtils.createColumnContainer( composite,
            1, 1 ), Messages.getString( "BrowserPreferencePage.SearchResultLabel" ), 1 ); //$NON-NLS-1$

        Composite searchResultLabelComposite = BaseWidgetUtils.createColumnContainer( searchResultLabelGroup, 3, 1 );
        BaseWidgetUtils.createLabel( searchResultLabelComposite, Messages
            .getString( "BrowserPreferencePage.UseAsSearchResultLabel1" ), 1 ); //$NON-NLS-1$
        searchResultLabelCombo = BaseWidgetUtils.createCombo( searchResultLabelComposite, new String[]
            { DN, RDN, RDN_VALUE }, 0, 1 );
        searchResultLabelCombo.setLayoutData( new GridData() );
        searchResultLabelCombo
            .select( getPreferenceStore().getInt( BrowserCommonConstants.PREFERENCE_BROWSER_SEARCH_RESULT_LABEL ) == BrowserCommonConstants.SHOW_RDN_VALUE ? 2
                : getPreferenceStore().getInt( BrowserCommonConstants.PREFERENCE_BROWSER_SEARCH_RESULT_LABEL ) == BrowserCommonConstants.SHOW_RDN ? 1
                    : 0 );
        BaseWidgetUtils.createLabel( searchResultLabelComposite, Messages
            .getString( "BrowserPreferencePage.UseAsSearchResultLabel2" ), 1 ); //$NON-NLS-1$

        Composite searchResultAbbreviateComposite = BaseWidgetUtils
            .createColumnContainer( searchResultLabelGroup, 3, 1 );
        searchResultAbbreviateButton = BaseWidgetUtils.createCheckbox( searchResultAbbreviateComposite, Messages
            .getString( "BrowserPreferencePage.LimitLabelLength1" ), 1 ); //$NON-NLS-1$
        searchResultAbbreviateButton.setSelection( getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_SEARCH_RESULT_ABBREVIATE ) );
        searchResultAbbreviateButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                updateEnabled();
            }
        } );
        searchResultAbbreviateMaxLengthText = BaseWidgetUtils.createText( searchResultAbbreviateComposite,
            getPreferenceStore().getString(
                BrowserCommonConstants.PREFERENCE_BROWSER_SEARCH_RESULT_ABBREVIATE_MAX_LENGTH ), 3, 1 );
        searchResultAbbreviateMaxLengthText.setEnabled( searchResultAbbreviateButton.getSelection() );
        searchResultAbbreviateMaxLengthText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
                {
                    e.doit = false;
                }
                if ( "".equals( searchResultAbbreviateMaxLengthText.getText() ) && e.text.matches( "[0]" ) ) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    e.doit = false;
                }
            }
        } );
        BaseWidgetUtils.createLabel( searchResultAbbreviateComposite, Messages
            .getString( "BrowserPreferencePage.LimitLabelLength2" ), 1 ); //$NON-NLS-1$

        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );
        Group foldingGroup = BaseWidgetUtils.createGroup( BaseWidgetUtils.createColumnContainer( composite, 1, 1 ),
            Messages.getString( "BrowserPreferencePage.Folding" ), 1 ); //$NON-NLS-1$
        Composite pagingGroupComposite = BaseWidgetUtils.createColumnContainer( foldingGroup, 2, 1 );
        enableFoldingButton = BaseWidgetUtils.createCheckbox( pagingGroupComposite, Messages
            .getString( "BrowserPreferencePage.EnableFolding" ), 2 ); //$NON-NLS-1$
        enableFoldingButton.setSelection( getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_ENABLE_FOLDING ) );
        enableFoldingButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                updateEnabled();
            }
        } );
        foldingSizeLabel = BaseWidgetUtils.createLabel( pagingGroupComposite, Messages
            .getString( "BrowserPreferencePage.FoldingSize" ), 1 ); //$NON-NLS-1$
        foldingSizeLabel.setEnabled( enableFoldingButton.getSelection() );
        foldingSizeText = BaseWidgetUtils.createText( pagingGroupComposite, getPreferenceStore().getString(
            BrowserCommonConstants.PREFERENCE_BROWSER_FOLDING_SIZE ), 4, 1 );
        foldingSizeText.setEnabled( enableFoldingButton.getSelection() );
        foldingSizeText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
                {
                    e.doit = false;
                }
                if ( "".equals( foldingSizeText.getText() ) && e.text.matches( "[0]" ) ) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    e.doit = false;
                }
            }
        } );

        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );
        expandBaseEntriesButton = BaseWidgetUtils.createCheckbox( composite, Messages
            .getString( "BrowserPreferencePage.ExpandBaseEntries" ), 1 ); //$NON-NLS-1$
        expandBaseEntriesButton.setSelection( getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_EXPAND_BASE_ENTRIES ) );
        Preferences coreStore = BrowserCorePlugin.getDefault().getPluginPreferences();
        checkForChildrenButton = BaseWidgetUtils.createCheckbox( composite, Messages
            .getString( "BrowserPreferencePage.CheckForChildren" ), 1 ); //$NON-NLS-1$
        checkForChildrenButton
            .setSelection( coreStore.getBoolean( BrowserCoreConstants.PREFERENCE_CHECK_FOR_CHILDREN ) );

        updateEnabled();

        applyDialogFont( composite );

        return composite;
    }


    private void updateEnabled()
    {
        entryAbbreviateMaxLengthText.setEnabled( entryAbbreviateButton.getSelection() );
        searchResultAbbreviateMaxLengthText.setEnabled( searchResultAbbreviateButton.getSelection() );
        foldingSizeText.setEnabled( enableFoldingButton.getSelection() );
        foldingSizeLabel.setEnabled( enableFoldingButton.getSelection() );
    }


    // ── DODONNA COMMITS THE FLIGHT PARAMETERS TO THE MISSION LOG ─────────────────
    // Dodonna's adjutant writes every finalized parameter into the mission log:
    // label format, abbreviation limits, folding thresholds, expand-on-connect
    // flag, and child-probe setting.  These become the standing orders for all
    // browser views until the next briefing.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Saves all browser preference values to the preference store when the user
     * clicks OK or Apply.  We write to both the common preference store (for
     * UI settings) and the core plugin preferences (for child-probing behavior).
     *
     * @return  Always true — there's no blocking validation preventing a save.
     */
    public boolean performOk()
    {
        Preferences coreStore = BrowserCorePlugin.getDefault().getPluginPreferences();
        coreStore.setValue( BrowserCoreConstants.PREFERENCE_CHECK_FOR_CHILDREN, checkForChildrenButton.getSelection() );
        BrowserCorePlugin.getDefault().savePluginPreferences();

        getPreferenceStore().setValue( BrowserCommonConstants.PREFERENCE_BROWSER_EXPAND_BASE_ENTRIES,
            expandBaseEntriesButton.getSelection() );

        getPreferenceStore().setValue( BrowserCommonConstants.PREFERENCE_BROWSER_ENABLE_FOLDING,
            enableFoldingButton.getSelection() );
        getPreferenceStore().setValue( BrowserCommonConstants.PREFERENCE_BROWSER_FOLDING_SIZE,
            foldingSizeText.getText().trim() );

        getPreferenceStore().setValue(
            BrowserCommonConstants.PREFERENCE_BROWSER_ENTRY_LABEL,
            entryLabelCombo.getSelectionIndex() == 2 ? BrowserCommonConstants.SHOW_RDN_VALUE : entryLabelCombo
                .getSelectionIndex() == 1 ? BrowserCommonConstants.SHOW_RDN : BrowserCommonConstants.SHOW_DN );
        getPreferenceStore().setValue( BrowserCommonConstants.PREFERENCE_BROWSER_ENTRY_ABBREVIATE,
            entryAbbreviateButton.getSelection() );
        getPreferenceStore().setValue( BrowserCommonConstants.PREFERENCE_BROWSER_ENTRY_ABBREVIATE_MAX_LENGTH,
            entryAbbreviateMaxLengthText.getText().trim() );

        getPreferenceStore().setValue(
            BrowserCommonConstants.PREFERENCE_BROWSER_SEARCH_RESULT_LABEL,
            searchResultLabelCombo.getSelectionIndex() == 2 ? BrowserCommonConstants.SHOW_RDN_VALUE
                : searchResultLabelCombo.getSelectionIndex() == 1 ? BrowserCommonConstants.SHOW_RDN
                    : BrowserCommonConstants.SHOW_DN );
        getPreferenceStore().setValue( BrowserCommonConstants.PREFERENCE_BROWSER_SEARCH_RESULT_ABBREVIATE,
            searchResultAbbreviateButton.getSelection() );
        getPreferenceStore().setValue( BrowserCommonConstants.PREFERENCE_BROWSER_SEARCH_RESULT_ABBREVIATE_MAX_LENGTH,
            searchResultAbbreviateMaxLengthText.getText().trim() );

        return true;
    }


    // ── DODONNA REINSTATES THE ORIGINAL MISSION PARAMETERS ───────────────────────
    // Dodonna pulls out the original briefing document — the one that came with
    // the manual — and resets every control to its factory-default value.  The
    // pilots are back to standard parameters, as if the customizations never happened.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Resets all browser preference controls to their plugin-defined default values.
     * Called when the user clicks "Restore Defaults."  We repopulate every widget
     * from the default values in the preference stores so the UI reflects what
     * will be saved.
     */
    protected void performDefaults()
    {
        entryLabelCombo
            .select( getPreferenceStore().getDefaultInt( BrowserCommonConstants.PREFERENCE_BROWSER_ENTRY_LABEL ) == BrowserCommonConstants.SHOW_RDN_VALUE ? 2
                : getPreferenceStore().getDefaultInt( BrowserCommonConstants.PREFERENCE_BROWSER_ENTRY_LABEL ) == BrowserCommonConstants.SHOW_RDN ? 1
                    : 0 );
        entryAbbreviateButton.setSelection( getPreferenceStore().getDefaultBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_ENTRY_ABBREVIATE ) );
        entryAbbreviateMaxLengthText.setText( getPreferenceStore().getDefaultString(
            BrowserCommonConstants.PREFERENCE_BROWSER_ENTRY_ABBREVIATE_MAX_LENGTH ) );

        searchResultLabelCombo
            .select( getPreferenceStore().getDefaultInt( BrowserCommonConstants.PREFERENCE_BROWSER_SEARCH_RESULT_LABEL ) == BrowserCommonConstants.SHOW_RDN_VALUE ? 2
                : getPreferenceStore().getDefaultInt( BrowserCommonConstants.PREFERENCE_BROWSER_SEARCH_RESULT_LABEL ) == BrowserCommonConstants.SHOW_RDN ? 1
                    : 0 );
        searchResultAbbreviateButton.setSelection( getPreferenceStore().getDefaultBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_SEARCH_RESULT_ABBREVIATE ) );
        searchResultAbbreviateMaxLengthText.setText( getPreferenceStore().getDefaultString(
            BrowserCommonConstants.PREFERENCE_BROWSER_SEARCH_RESULT_ABBREVIATE_MAX_LENGTH ) );

        enableFoldingButton.setSelection( getPreferenceStore().getDefaultBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_ENABLE_FOLDING ) );
        foldingSizeText.setText( getPreferenceStore().getDefaultString(
            BrowserCommonConstants.PREFERENCE_BROWSER_FOLDING_SIZE ) );

        expandBaseEntriesButton.setSelection( getPreferenceStore().getDefaultBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_EXPAND_BASE_ENTRIES ) );
        Preferences coreStore = BrowserCorePlugin.getDefault().getPluginPreferences();
        checkForChildrenButton.setSelection( coreStore
            .getDefaultBoolean( BrowserCoreConstants.PREFERENCE_CHECK_FOR_CHILDREN ) );

        updateEnabled();

        super.performDefaults();
    }

}
