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
package org.apache.directory.studio.combinededitor.preferences;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.apache.directory.studio.combinededitor.CombinedEditorPlugin;
import org.apache.directory.studio.combinededitor.CombinedEditorPluginConstants;
import org.apache.directory.studio.templateeditor.EntryTemplatePlugin;


// ── CLASS: CombinedEntryEditorPreferencePage — The Tantive IV Pre-Mission Config ──
// Before the Tantive IV launches, Leia and Antilles sit down in the captain's
// briefing room and decide: which display mode should come up first?  If the
// Template view isn't available for a particular entry, which fallback display
// should activate automatically — the Table console or the LDIF comms screen?
// CombinedEntryEditorPreferencePage is that briefing session: the user chooses
// the default tab (Template / Table / LDIF) and configures the auto-switch
// fallback.  These settings are stored in the plugin's preference store and
// read by the combined editor every time it opens a new entry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Eclipse preference page for the Combined Entry Editor.
 * Lets the user choose which editor tab (Template, Table, or LDIF) opens
 * by default and whether to auto-switch to Table or LDIF when no template
 * matches the current entry's object class.
 * Think of this as the Tantive IV's pre-mission briefing room where Leia
 * configures each bridge station's default behaviour before launch.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CombinedEntryEditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    /** The preferences store */
    private IPreferenceStore store;

    // UI Fields
    private Button defaultEditorTemplateRadioButton;
    private Button autoSwitchToOtherEditorCheckbox;
    private Button autoSwitchToTableEditorRadioButton;
    private Button autoSwitchToLDIFEditorRadioButton;
    private Label autoSwitchLabel;
    private Button defaultEditorTableRadioButton;
    private Button defaultEditorLDIFRadioButton;


    // ── Leia Walks Into the Briefing Room — Page Created ─────────────────────
    // Leia enters the briefing room, sets the description text on the whiteboard,
    // and retrieves both preference stores she'll need for the session: the
    // template plugin's store (for template preferences) and the combined editor's
    // own store (for the default-editor and auto-switch settings).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the preference page.
     * We set the preference store to the template plugin's store (required by the
     * parent class) and store a reference to the combined editor plugin's own
     * preference store for reading and writing the editor-specific settings.
     */
    public CombinedEntryEditorPreferencePage()
    {
        super();
        super.setPreferenceStore( EntryTemplatePlugin.getDefault().getPreferenceStore() );
        super.setDescription( Messages.getString( "CombinedEntryEditorPreferencePage.PrefPageDescription" ) ); //$NON-NLS-1$

        store = CombinedEditorPlugin.getDefault().getPreferenceStore();
    }


    // ── Workbench Tells Leia the Room is Ready — Nothing Extra Needed ─────────
    // The briefing room is ready; Leia just acknowledges the signal.  We don't
    // need the IWorkbench reference for anything in this page.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * No-op workbench initialisation hook.
     * This preference page doesn't need the workbench reference.
     *
     * @param workbench  the workbench — not used.
     */
    public void init( IWorkbench workbench )
    {
        // Nothing to do
    }


    // ── The Whiteboard Gets All Its Checkboxes and Radio Buttons ─────────────
    // Leia draws the briefing room's configuration options on the whiteboard:
    // the radio buttons for "Template / Table / LDIF as default", the auto-switch
    // checkbox, and the fallback radio buttons (Table or LDIF).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT controls for this preference page.
     * We call {@link #createUI(Composite)} to lay out the widgets, then
     * {@link #initListeners()} to wire selection events, and {@link #initUI()}
     * to populate them from the current stored preferences.
     *
     * @param parent  the parent composite provided by the Eclipse preference framework.
     * @return        the top-level composite we created.
     */
    protected Control createContents( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayout( new GridLayout() );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        createUI( composite );
        initListeners();
        initUI();

        return composite;
    }


    // ── Leia Draws All the Config Options on the Whiteboard ──────────────────
    // Each radio button and checkbox gets its own row on the whiteboard with a
    // clear label — Template, Table, LDIF — and the auto-switch sub-options
    // are indented under the Template radio button where they belong.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Lays out all the SWT widgets for the preference page.
     * We use a group box labelled "Use as Default Editor" containing three radio
     * buttons (Template / Table / LDIF) and — indented under Template — an
     * auto-switch checkbox and its own pair of radio buttons (Table or LDIF
     * fallback).
     *
     * @param parent  the parent composite to attach the UI to.
     */
    private void createUI( Composite parent )
    {
        // Main Composite
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        // Default Editor Group
        Group defaultEditorGroup = BaseWidgetUtils.createGroup( composite, Messages
            .getString( "CombinedEntryEditorPreferencePage.UseAsDefaultEditor" ), 1 ); //$NON-NLS-1$
        defaultEditorGroup.setLayout( new GridLayout( 4, false ) );

        // Template Editor Radio Button
        defaultEditorTemplateRadioButton = BaseWidgetUtils.createRadiobutton( defaultEditorGroup, Messages
            .getString( "CombinedEntryEditorPreferencePage.TemplateEditor" ), 1 ); //$NON-NLS-1$
        defaultEditorTemplateRadioButton.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 4, 1 ) );

        // Indent
        BaseWidgetUtils.createRadioIndent( defaultEditorGroup, 1 );

        // Auto Switch Checkbox
        autoSwitchToOtherEditorCheckbox = BaseWidgetUtils.createCheckbox( defaultEditorGroup, Messages
            .getString( "CombinedEntryEditorPreferencePage.AutoSwitchToFollowingTabNoTemplateAvailable" ), 1 ); //$NON-NLS-1$
        autoSwitchToOtherEditorCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 3, 1 ) );

        // Indent
        BaseWidgetUtils.createRadioIndent( defaultEditorGroup, 1 );

        // Indent
        BaseWidgetUtils.createRadioIndent( defaultEditorGroup, 1 );

        // Table Editor Radio Button
        autoSwitchToTableEditorRadioButton = BaseWidgetUtils.createRadiobutton( defaultEditorGroup, Messages
            .getString( "CombinedEntryEditorPreferencePage.TableEditor" ), 1 ); //$NON-NLS-1$

        // Table Editor Radio Button
        autoSwitchToLDIFEditorRadioButton = BaseWidgetUtils.createRadiobutton( defaultEditorGroup, Messages
            .getString( "CombinedEntryEditorPreferencePage.LDIFEditor" ), 1 ); //$NON-NLS-1$

        // Indent
        BaseWidgetUtils.createRadioIndent( defaultEditorGroup, 1 );

        // Auto Switch Label
        autoSwitchLabel = BaseWidgetUtils.createLabel( defaultEditorGroup, Messages
            .getString( "CombinedEntryEditorPreferencePage.AutoSwitchNote" ), //$NON-NLS-1$
            1 );
        autoSwitchLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 3, 1 ) );

        // Table Editor Radio Button
        defaultEditorTableRadioButton = BaseWidgetUtils.createRadiobutton( defaultEditorGroup, Messages
            .getString( "CombinedEntryEditorPreferencePage.TableEditor" ), 1 ); //$NON-NLS-1$
        defaultEditorTableRadioButton.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 4, 1 ) );

        // LDIF Editor Radio Button
        defaultEditorLDIFRadioButton = BaseWidgetUtils.createRadiobutton( defaultEditorGroup, Messages
            .getString( "CombinedEntryEditorPreferencePage.LDIFEditor" ), 1 ); //$NON-NLS-1$
        defaultEditorLDIFRadioButton.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 4, 1 ) );
    }


    // ── Each Button Gets a Handler — Briefing Room Comes Alive ───────────────
    // Antilles walks around the briefing room and assigns a handler to each
    // button: "When you're clicked, call this method."  From this point the
    // UI is interactive.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Wires up selection listeners on all radio buttons and the checkbox.
     * Each widget gets a {@link SelectionAdapter} that calls the corresponding
     * private action method to update the enabled/selection state of related
     * widgets consistently.
     */
    private void initListeners()
    {
        defaultEditorTemplateRadioButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                defaultEditorTemplateAction();
            }
        } );

        autoSwitchToOtherEditorCheckbox.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                autoSwitchToOtherEditorAction();
            }
        } );

        autoSwitchToTableEditorRadioButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                autoSwitchToTableEditorAction();
            }
        } );

        autoSwitchToLDIFEditorRadioButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                autoSwitchToLDIFEditorAction();
            }
        } );

        defaultEditorTableRadioButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                defaultEditorTableAction();
            }
        } );

        defaultEditorLDIFRadioButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                defaultEditorLDIFAction();
            }
        } );
    }


    // ── "Template is our Default" — Enable the Auto-Switch Sub-Options ────────
    // When Leia selects Template as the default display, the auto-switch sub-panel
    // becomes active: the "auto-switch when no template" checkbox is enabled, and
    // if it's already checked, the fallback radio buttons are also enabled.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the Template radio button is selected.
     * We make sure only the Template button is selected and enable the auto-switch
     * sub-options (checkbox and fallback radio buttons).
     */
    private void defaultEditorTemplateAction()
    {
        defaultEditorTemplateRadioButton.setSelection( true );
        defaultEditorTableRadioButton.setSelection( false );
        defaultEditorLDIFRadioButton.setSelection( false );

        autoSwitchToOtherEditorCheckbox.setEnabled( true );
        autoSwitchToTableEditorRadioButton.setEnabled( autoSwitchToOtherEditorCheckbox.getSelection() );
        autoSwitchToLDIFEditorRadioButton.setEnabled( autoSwitchToOtherEditorCheckbox.getSelection() );
        autoSwitchLabel.setEnabled( true );
    }


    // ── Checkbox Toggled — Enable or Disable the Fallback Radio Buttons ───────
    // When the "auto-switch when no template available" checkbox is toggled,
    // the two fallback radio buttons (Table and LDIF) are enabled or disabled
    // to match, so the user can only pick a fallback if auto-switch is on.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the auto-switch checkbox is toggled.
     * We enable or disable the fallback radio buttons and the note label
     * based on the checkbox state.
     */
    private void autoSwitchToOtherEditorAction()
    {
        autoSwitchToTableEditorRadioButton.setEnabled( autoSwitchToOtherEditorCheckbox.getSelection() );
        autoSwitchToLDIFEditorRadioButton.setEnabled( autoSwitchToOtherEditorCheckbox.getSelection() );
        autoSwitchLabel.setEnabled( autoSwitchToOtherEditorCheckbox.getSelection() );
    }


    // ── "Fall Back to Table" — Select Table as the Auto-Switch Target ─────────
    // When Antilles picks "Table" as the fallback he ensures that radio button
    // is selected and the LDIF fallback radio button is cleared.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the "auto-switch to Table" radio button is selected.
     * Ensures mutual exclusion with the LDIF fallback radio button.
     */
    private void autoSwitchToTableEditorAction()
    {
        autoSwitchToTableEditorRadioButton.setSelection( true );
        autoSwitchToLDIFEditorRadioButton.setSelection( false );
    }


    // ── "Fall Back to LDIF" — Select LDIF as the Auto-Switch Target ──────────
    // When Antilles picks "LDIF" as the fallback he selects that radio button
    // and clears the Table fallback radio button.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the "auto-switch to LDIF" radio button is selected.
     * Ensures mutual exclusion with the Table fallback radio button.
     */
    private void autoSwitchToLDIFEditorAction()
    {
        autoSwitchToTableEditorRadioButton.setSelection( false );
        autoSwitchToLDIFEditorRadioButton.setSelection( true );
    }


    // ── "Table is our Default" — Disable the Auto-Switch Sub-Options ─────────
    // When Table is the default editor there's no template involved and therefore
    // no need for auto-switch options — the sub-panel is disabled entirely.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the Table radio button is selected as the default editor.
     * We clear the other radio buttons and disable all auto-switch sub-options
     * since there's no template-fallback concept when Table is the default.
     */
    private void defaultEditorTableAction()
    {
        defaultEditorTemplateRadioButton.setSelection( false );
        defaultEditorTableRadioButton.setSelection( true );
        defaultEditorLDIFRadioButton.setSelection( false );

        autoSwitchToOtherEditorCheckbox.setEnabled( false );
        autoSwitchToTableEditorRadioButton.setEnabled( false );
        autoSwitchToLDIFEditorRadioButton.setEnabled( false );
        autoSwitchLabel.setEnabled( false );
    }


    // ── "LDIF is our Default" — Disable the Auto-Switch Sub-Options ──────────
    // Same as the Table case: when LDIF is the default there's no template
    // fallback needed, so the auto-switch panel is fully disabled.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the LDIF radio button is selected as the default editor.
     * We clear the other radio buttons and disable all auto-switch sub-options.
     */
    private void defaultEditorLDIFAction()
    {
        defaultEditorTemplateRadioButton.setSelection( false );
        defaultEditorTableRadioButton.setSelection( false );
        defaultEditorLDIFRadioButton.setSelection( true );

        autoSwitchToOtherEditorCheckbox.setEnabled( false );
        autoSwitchToTableEditorRadioButton.setEnabled( false );
        autoSwitchToLDIFEditorRadioButton.setEnabled( false );
        autoSwitchLabel.setEnabled( false );
    }


    // ── Briefing Room Gets the Current Stored Settings ───────────────────────
    // Leia reads the stored mission parameters from the preference store and
    // populates the whiteboard accordingly — whichever radio button matches
    // the stored value gets selected, and the sub-options are enabled or
    // disabled to match the current auto-switch configuration.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the UI from the current (stored) preference values.
     * We read {@code PREF_DEFAULT_EDITOR}, {@code PREF_AUTO_SWITCH_TO_ANOTHER_EDITOR},
     * and {@code PREF_AUTO_SWITCH_EDITOR} from the store and delegate to
     * {@link #initUI(int, boolean, int)}.
     */
    private void initUI()
    {
        initUI( store.getInt( CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR ), store
            .getBoolean( CombinedEditorPluginConstants.PREF_AUTO_SWITCH_TO_ANOTHER_EDITOR ), store
            .getInt( CombinedEditorPluginConstants.PREF_AUTO_SWITCH_EDITOR ) );
    }


    // ── "Restore Defaults" — Reset to Factory Settings ───────────────────────
    // Antilles hits the "Restore Defaults" button in the briefing room and the
    // whiteboard resets to the factory defaults — out of the box, Template is
    // the default editor with auto-switch enabled.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Resets the UI to the default preference values.
     * We read the default values from the store (rather than the currently stored
     * values) and repopulate the UI, then call the parent to handle the standard
     * "Restore Defaults" behaviour.
     */
    protected void performDefaults()
    {
        initUI( store.getDefaultInt( CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR ), store
            .getDefaultBoolean( CombinedEditorPluginConstants.PREF_AUTO_SWITCH_TO_ANOTHER_EDITOR ), store
            .getDefaultInt( CombinedEditorPluginConstants.PREF_AUTO_SWITCH_EDITOR ) );

        super.performDefaults();
    }


    // ── Populate the Whiteboard from Specific Values ──────────────────────────
    // Given explicit values for the three settings, Leia marks the correct radio
    // buttons as selected and enables or disables the sub-panel controls to match.
    // This helper is called both from initUI() (stored values) and performDefaults()
    // (default values).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the UI controls from the given preference values.
     * We set the three radio-button groups and the checkbox, then update the
     * enabled state of the sub-panel controls to reflect the effective state.
     *
     * @param defaultEditor            one of the {@code PREF_DEFAULT_EDITOR_*} constants.
     * @param autoSwitchToOtherEditor  {@code true} if auto-switch is enabled.
     * @param autoSwitchEditor         one of the {@code PREF_AUTO_SWITCH_EDITOR_*} constants.
     */
    private void initUI( int defaultEditor, boolean autoSwitchToOtherEditor, int autoSwitchEditor )
    {
        // Default Editor
        if ( defaultEditor == CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR_TEMPLATE )
        {
            defaultEditorTemplateRadioButton.setSelection( true );
            defaultEditorTableRadioButton.setSelection( false );
            defaultEditorLDIFRadioButton.setSelection( false );
        }
        else if ( defaultEditor == CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR_TABLE )
        {
            defaultEditorTemplateRadioButton.setSelection( false );
            defaultEditorTableRadioButton.setSelection( true );
            defaultEditorLDIFRadioButton.setSelection( false );
        }
        else if ( defaultEditor == CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR_LDIF )
        {
            defaultEditorTemplateRadioButton.setSelection( false );
            defaultEditorTableRadioButton.setSelection( false );
            defaultEditorLDIFRadioButton.setSelection( true );
        }

        // Auto Switch
        autoSwitchToOtherEditorCheckbox.setEnabled( defaultEditorTemplateRadioButton.getSelection() );
        autoSwitchToOtherEditorCheckbox.setSelection( autoSwitchToOtherEditor );

        // Auto Switch Editor
        autoSwitchToTableEditorRadioButton.setEnabled( defaultEditorTemplateRadioButton.getSelection()
            && autoSwitchToOtherEditorCheckbox.getSelection() );
        autoSwitchToLDIFEditorRadioButton.setEnabled( defaultEditorTemplateRadioButton.getSelection()
            && autoSwitchToOtherEditorCheckbox.getSelection() );
        if ( autoSwitchEditor == CombinedEditorPluginConstants.PREF_AUTO_SWITCH_EDITOR_TABLE )
        {
            autoSwitchToTableEditorRadioButton.setSelection( true );
            autoSwitchToLDIFEditorRadioButton.setSelection( false );
        }
        else if ( autoSwitchEditor == CombinedEditorPluginConstants.PREF_AUTO_SWITCH_EDITOR_LDIF )
        {
            autoSwitchToTableEditorRadioButton.setSelection( false );
            autoSwitchToLDIFEditorRadioButton.setSelection( true );
        }
        autoSwitchLabel.setEnabled( defaultEditorTemplateRadioButton.getSelection() );
    }


    // ── Leia Signs Off the Mission Plan — Save the Settings ──────────────────
    // When Leia clicks OK she signs off the mission plan: the selected radio
    // button values are written back to the preference store and the combined
    // editor will read them the next time it opens an entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Persists the current UI selections to the preference store.
     * We read the selected radio buttons and checkbox and write the corresponding
     * constant values ({@code PREF_DEFAULT_EDITOR}, {@code PREF_AUTO_SWITCH_*})
     * to the plugin's preference store.
     *
     * @return  always {@code true} — there is no validation that can fail here.
     */
    public boolean performOk()
    {
        // Default Editor
        if ( defaultEditorTemplateRadioButton.getSelection() )
        {
            store.setValue( CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR,
                CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR_TEMPLATE );
        }
        else if ( defaultEditorTableRadioButton.getSelection() )
        {
            store.setValue( CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR,
                CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR_TABLE );
        }
        else if ( defaultEditorLDIFRadioButton.getSelection() )
        {
            store.setValue( CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR,
                CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR_LDIF );
        }

        if ( defaultEditorTemplateRadioButton.getSelection() )
        {
            store.setValue( CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR,
                CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR_TEMPLATE );
        }

        // Auto Switch
        store.setValue( CombinedEditorPluginConstants.PREF_AUTO_SWITCH_TO_ANOTHER_EDITOR,
            autoSwitchToOtherEditorCheckbox.getSelection() );

        // Auto Switch Editor
        if ( autoSwitchToTableEditorRadioButton.getSelection() )
        {
            store.setValue( CombinedEditorPluginConstants.PREF_AUTO_SWITCH_EDITOR,
                CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR_TEMPLATE );
        }
        else if ( autoSwitchToLDIFEditorRadioButton.getSelection() )
        {
            store.setValue( CombinedEditorPluginConstants.PREF_AUTO_SWITCH_EDITOR,
                CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR_TABLE );
        }

        return true;
    }
}
