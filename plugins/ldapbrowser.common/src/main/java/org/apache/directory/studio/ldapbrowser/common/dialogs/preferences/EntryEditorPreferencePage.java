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
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


// ── CLASS: EntryEditorPreferencePage — LUKE'S X-WING PRE-FLIGHT CHECKLIST ────
// In the Yavin 4 hangar, Luke Skywalker sits in Red Five's cockpit working
// through his pre-flight checklist before the Death Star run: autosave enabled
// for single-tab and multi-tab sessions?  Folding turned on so the instrument
// panel doesn't drown in attributes?  Threshold set so small entries stay flat
// while huge ones collapse into scannable groups?  Auto-expand folded sections
// so you don't have to click every group?  Every pilot runs this checklist before
// launch.  This preference page is that checklist for the LDAP entry editor.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Eclipse preference page that controls the entry editor's runtime behaviour:
 * autosave policies for single-tab and multi-tab modes, attribute folding (to
 * avoid scrolling through hundreds of values), and auto-expand on open.
 * Think of this class as Luke's pre-flight checklist — every setting confirmed
 * before we commit to the mission.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{

    private Button autosaveSingleTabButton;

    private Button autosaveMultiTabButton;

    private Button enableFoldingButton;

    private Label foldingThresholdLabel;

    private Text foldingThresholdText;

    private Button autoExpandFoldedAttributesButton;


    // ── LUKE STRAPS IN AND CALLS UP THE CHECKLIST ─────────────────────────────────
    // Luke settles into Red Five's seat, calls up the pre-flight checklist on the
    // instrument panel, announces the page title ("Entry Editor"), and confirms he's
    // reading from the right mission manual (the common preference store).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the page with its title and description text, and wires it to
     * the correct preference store.  Eclipse calls this when the user navigates
     * to the Entry Editor node in the preference tree.
     */
    public EntryEditorPreferencePage()
    {
        super( Messages.getString( "EntryEditorPreferencePage.EntryEditor" ) ); //$NON-NLS-1$
        super.setPreferenceStore( BrowserCommonActivator.getDefault().getPreferenceStore() );
        super.setDescription( Messages.getString( "EntryEditorPreferencePage.GeneralSettings" ) ); //$NON-NLS-1$
    }


    // ── LUKE ACKNOWLEDGES GROUND CONTROL ──────────────────────────────────────────
    // Luke gives a thumbs-up to the Yavin 4 ground crew.  No information is needed
    // from the workbench at this stage — just satisfying the interface contract.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Required by {@link IWorkbenchPreferencePage} — not used here.
     *
     * @param workbench  The Eclipse workbench instance — not used.
     */
    public void init( IWorkbench workbench )
    {
    }


    // ── LUKE WORKS THROUGH THE PRE-FLIGHT CHECKLIST ──────────────────────────────
    // Luke checks each item in sequence: folding group (enable? threshold? auto-
    // expand?), then autosave group (single-tab? multi-tab?).  He sets each control
    // from the current stored value so the checklist reflects what's actually
    // configured, not just the defaults.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the page UI: a folding group with enable/threshold/auto-expand
     * controls, and an autosave group with single-tab and multi-tab checkboxes.
     * All controls are pre-filled from the current preference store values.
     *
     * <p>For example — Luke's checklist panels:</p>
     * <pre>
     *   Folding:
     *     [x] Enable folding
     *         Threshold: [100]
     *         [x] Auto-expand folded attributes
     *   Autosave:
     *     [x] Autosave single-tab editor
     *     [ ] Autosave multi-tab editor
     * </pre>
     *
     * @param parent  The parent composite provided by Eclipse.
     * @return        The composite containing all entry editor preference widgets.
     */
    protected Control createContents( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );
        String foldingTooltip = Messages.getString( "EntryEditorPreferencePage.FoldingToolTip" ); //$NON-NLS-1$
        Group foldingGroup = BaseWidgetUtils.createGroup( BaseWidgetUtils.createColumnContainer( composite, 1, 1 ),
            Messages.getString( "EntryEditorPreferencePage.Folding" ), 1 ); //$NON-NLS-1$
        Composite pagingGroupComposite = BaseWidgetUtils.createColumnContainer( foldingGroup, 3, 1 );
        enableFoldingButton = BaseWidgetUtils.createCheckbox( pagingGroupComposite, Messages
            .getString( "EntryEditorPreferencePage.EnableFolding" ), 3 ); //$NON-NLS-1$
        enableFoldingButton.setToolTipText( foldingTooltip );
        enableFoldingButton.setSelection( getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_ENABLE_FOLDING ) );
        enableFoldingButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                updateEnabled();
            }
        } );
        BaseWidgetUtils.createRadioIndent( pagingGroupComposite, 1 );
        foldingThresholdLabel = BaseWidgetUtils.createLabel( pagingGroupComposite, Messages
            .getString( "EntryEditorPreferencePage.FoldingThreshold" ), 1 ); //$NON-NLS-1$
        foldingThresholdLabel.setToolTipText( foldingTooltip );
        foldingThresholdLabel.setEnabled( enableFoldingButton.getSelection() );
        foldingThresholdText = BaseWidgetUtils.createText( pagingGroupComposite, getPreferenceStore().getString(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_FOLDING_THRESHOLD ), 4, 1 );
        foldingThresholdText.setToolTipText( foldingTooltip );
        foldingThresholdText.setEnabled( enableFoldingButton.getSelection() );
        foldingThresholdText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
                {
                    e.doit = false;
                }
                if ( "".equals( foldingThresholdText.getText() ) && e.text.matches( "[0]" ) ) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    e.doit = false;
                }
            }
        } );
        BaseWidgetUtils.createRadioIndent( pagingGroupComposite, 1 );
        autoExpandFoldedAttributesButton = BaseWidgetUtils.createCheckbox( pagingGroupComposite, Messages
            .getString( "EntryEditorPreferencePage.AutoExpandFoldedAttributes" ), 2 ); //$NON-NLS-1$
        autoExpandFoldedAttributesButton.setEnabled( enableFoldingButton.getSelection() );
        autoExpandFoldedAttributesButton.setSelection( getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTO_EXPAND_FOLDED_ATTRIBUTES ) );

        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );
        Group autosaveGroup = BaseWidgetUtils.createGroup( BaseWidgetUtils.createColumnContainer( composite, 1, 1 ),
            Messages.getString( "EntryEditorPreferencePage.Autosave" ), 1 ); //$NON-NLS-1$
        Composite autosaveComposite = BaseWidgetUtils.createColumnContainer( autosaveGroup, 1, 1 );
        autosaveSingleTabButton = BaseWidgetUtils.createCheckbox( autosaveComposite, Messages
            .getString( "EntryEditorPreferencePage.AutosaveSingleTab" ), 1 ); //$NON-NLS-1$
        autosaveSingleTabButton.setSelection( getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTOSAVE_SINGLE_TAB ) );
        autosaveMultiTabButton = BaseWidgetUtils.createCheckbox( autosaveComposite, Messages
            .getString( "EntryEditorPreferencePage.AutosaveMultiTab" ), 1 ); //$NON-NLS-1$
        autosaveMultiTabButton.setSelection( getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTOSAVE_MULTI_TAB ) );

        updateEnabled();

        applyDialogFont( composite );

        return composite;
    }


    private void updateEnabled()
    {
        foldingThresholdText.setEnabled( enableFoldingButton.getSelection() );
        foldingThresholdLabel.setEnabled( enableFoldingButton.getSelection() );
        autoExpandFoldedAttributesButton.setEnabled( enableFoldingButton.getSelection() );
    }


    // ── LUKE SIGNS OFF THE CHECKLIST AND POWERS UP ───────────────────────────────
    // Luke ticks the last item on the checklist, radios "Red Five standing by,"
    // and commits every confirmed parameter to the mission log.  Autosave modes,
    // folding settings — all written to the preference store for persistence.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Saves all entry editor preferences when the user clicks OK or Apply.
     * We persist autosave flags, folding enable/threshold, and auto-expand to
     * the common preference store.
     *
     * @return  Always true — there's no blocking validation at save time.
     */
    public boolean performOk()
    {
        getPreferenceStore().setValue( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTOSAVE_SINGLE_TAB,
            autosaveSingleTabButton.getSelection() );
        getPreferenceStore().setValue( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTOSAVE_MULTI_TAB,
            autosaveMultiTabButton.getSelection() );

        getPreferenceStore().setValue( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_ENABLE_FOLDING,
            enableFoldingButton.getSelection() );
        getPreferenceStore().setValue( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_FOLDING_THRESHOLD,
            foldingThresholdText.getText() );
        getPreferenceStore().setValue( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTO_EXPAND_FOLDED_ATTRIBUTES,
            autoExpandFoldedAttributesButton.getSelection() );

        return true;
    }


    // ── LUKE RESETS TO FACTORY CONFIGURATION ─────────────────────────────────────
    // The ground crew hands Luke the original Red Five configuration card — every
    // instrument reset to the default values from the factory.  Luke goes down the
    // list and resets each control so the UI matches what will be written when he
    // clicks OK.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Resets all entry editor preference controls to the plugin's factory defaults.
     * Called when the user clicks "Restore Defaults."  We read defaults from the
     * preference store and repopulate every control.
     */
    protected void performDefaults()
    {
        autosaveSingleTabButton.setSelection( getPreferenceStore().getDefaultBoolean(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTOSAVE_SINGLE_TAB ) );
        autosaveMultiTabButton.setSelection( getPreferenceStore().getDefaultBoolean(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTOSAVE_MULTI_TAB ) );

        enableFoldingButton.setSelection( getPreferenceStore().getDefaultBoolean(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_ENABLE_FOLDING ) );
        foldingThresholdText.setText( getPreferenceStore().getDefaultString(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_FOLDING_THRESHOLD ) );
        autoExpandFoldedAttributesButton.setSelection( getPreferenceStore().getDefaultBoolean(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTO_EXPAND_FOLDED_ATTRIBUTES ) );

        updateEnabled();

        super.performDefaults();
    }

}
