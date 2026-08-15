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

package org.apache.directory.studio.schemaeditor.view.views;


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: SchemaViewSortingDialog — Leia's Hologram Message ─────────────────
// In A New Hope, Princess Leia records a desperate hologram message asking Obi-Wan
// Kenobi for help: "Help me, Obi-Wan Kenobi — you're my only hope." The message
// pops up unexpectedly, asks a specific question, waits for a decision, and then
// acts on whatever is decided. It's a transient plea for guidance.
// This dialog is exactly that: a small popup that appears when the user clicks
// "Sort/Group," asks how they want the Schema View organized (in folders or mixed?
// sort by name or OID? ascending or descending?), and then acts on the answer
// by writing the choices to the preference store.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A small modal dialog that lets users configure how the Schema View groups and sorts
 * its tree. The user picks between "in folders" grouping (separate Attribute Types
 * and Object Classes folders) or mixed grouping, chooses a sort field (first name or OID),
 * and picks sort order (ascending or descending). On OK we write all three choices to
 * the plugin's preference store. Think of it as Leia's hologram: a transient popup
 * asking for a decision, then acting on whatever the user decides.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaViewSortingDialog extends Dialog
{
    // UI Fields
    private Button inFoldersButton;
    private Button mixedButton;
    private Combo sortingCombo;
    private Button ascendingButton;
    private Button descendingButton;


    // ── Leia Records the Message ──────────────────────────────────────────────
    // Before the hologram plays, Leia records it and packages it up with a target
    // recipient (the parent shell). The constructor takes the parent shell that owns
    // this dialog so it can center itself and block interaction with the parent window.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new sorting dialog, bound to the given parent shell.
     * Passing the shell to {@code super} lets JFace center this dialog over
     * the parent window and make it modal (nothing else in that window responds
     * until the user clicks OK or Cancel).
     *
     * <p>For example — recording the message:</p>
     * <pre>
     *   new SchemaViewSortingDialog(parentShell)
     *   → super(parentShell)  // "this message is for you, Obi-Wan"
     * </pre>
     *
     * @param parentShell  the SWT shell that owns this dialog
     */
    public SchemaViewSortingDialog( Shell parentShell )
    {
        super( parentShell );
    }


    // ── The Hologram Gets a Title ─────────────────────────────────────────────
    // R2's projection doesn't just appear in silence — the shell gets labeled
    // "Message from Princess Leia." Here we set the dialog window's title so
    // the user knows they're in the sorting configuration dialog.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Sets the title of the dialog shell.
     * Called by JFace before the dialog is shown. We set the window title to the
     * localized "View Sorting" string so users know what they're configuring.
     *
     * @param newShell  the SWT shell for this dialog window
     */
    @Override
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "SchemaViewSortingDialog.ViewSorting" ) ); //$NON-NLS-1$
    }


    // ── The Message Plays — Presenting the Question ──────────────────────────
    // Leia's hologram shimmers into view and presents her request clearly:
    // "Help me, Obi-Wan." The dialog content area is the hologram itself —
    // two groups of controls (Grouping and Sorting) laid out clearly so the
    // user can make their choices. We also pre-populate from saved preferences
    // so it remembers their last choices.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog's content area with two option groups: Grouping and Sorting.
     * The Grouping group lets users choose between folders-based grouping (Attribute
     * Types and Object Classes get separate sub-folders) or mixed grouping (all
     * types appear together). The Sorting group lets them choose the sort key
     * (first name or OID) and direction (ascending or descending). Fields are
     * pre-filled from the preference store via {@link #initFieldsFromPreferences()}.
     *
     * <p>For example — the message plays:</p>
     * <pre>
     *   createDialogArea(parent)
     *     → Grouping group: [• In folders] [ Mixed]
     *     → Sorting group:  SortBy:[FirstName v] [• Ascending] [ Descending]
     *     → initFieldsFromPreferences()  // pre-select last used options
     * </pre>
     *
     * @param parent  the composite to build the dialog area inside
     * @return        the configured dialog area composite
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // Grouping Group
        Group groupingGroup = new Group( composite, SWT.NONE );
        groupingGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        groupingGroup.setText( Messages.getString( "SchemaViewSortingDialog.Grouping" ) ); //$NON-NLS-1$
        groupingGroup.setLayout( new GridLayout() );

        // Attribute Types first Button
        inFoldersButton = new Button( groupingGroup, SWT.RADIO );
        inFoldersButton.setText( Messages.getString( "SchemaViewSortingDialog.GroupTypesAndClasses" ) ); //$NON-NLS-1$
        inFoldersButton.setEnabled( true );

        // Mixed Button
        mixedButton = new Button( groupingGroup, SWT.RADIO );
        mixedButton.setText( Messages.getString( "SchemaViewSortingDialog.Mixed" ) ); //$NON-NLS-1$
        mixedButton.setEnabled( true );

        // Sorting Group
        Group sortingGroup = new Group( composite, SWT.NONE );
        sortingGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        sortingGroup.setText( Messages.getString( "SchemaViewSortingDialog.Sorting" ) ); //$NON-NLS-1$
        sortingGroup.setLayout( new GridLayout() );
        Composite sortingGroupComposite = new Composite( sortingGroup, SWT.NONE );
        GridLayout gl = new GridLayout( 4, false );
        gl.marginHeight = gl.marginWidth = 0;
        sortingGroupComposite.setLayout( gl );
        sortingGroupComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Sort by Label
        Label sortByLabel = new Label( sortingGroupComposite, SWT.NONE );
        sortByLabel.setText( Messages.getString( "SchemaViewSortingDialog.SortBy" ) ); //$NON-NLS-1$

        // Sorting Combo
        sortingCombo = new Combo( sortingGroupComposite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER );
        sortingCombo.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        sortingCombo
            .setItems( new String[]
                {
                    Messages.getString( "SchemaViewSortingDialog.FirstName" ), Messages.getString( "SchemaViewSortingDialog.OID" ) } ); //$NON-NLS-1$ //$NON-NLS-2$
        sortingCombo.setEnabled( true );

        // Ascending Button
        ascendingButton = new Button( sortingGroupComposite, SWT.RADIO );
        ascendingButton.setText( Messages.getString( "SchemaViewSortingDialog.Ascending" ) ); //$NON-NLS-1$
        ascendingButton.setEnabled( true );

        // Descending Button
        descendingButton = new Button( sortingGroupComposite, SWT.RADIO );
        descendingButton.setText( Messages.getString( "SchemaViewSortingDialog.Descending" ) ); //$NON-NLS-1$
        descendingButton.setEnabled( true );

        initFieldsFromPreferences();

        applyDialogFont( composite );
        return composite;
    }


    // ── Obi-Wan Recalls the Last Known State ─────────────────────────────────
    // Before Obi-Wan decides what to do, he reviews what he already knows:
    // the last time he saw Leia's family, what the Rebellion was up to, what
    // orders he'd received. initFieldsFromPreferences does the same: it reads
    // the preference store to find the user's last chosen grouping, sort field,
    // and sort order, then pre-selects those options in the UI.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Pre-populates the dialog's controls with the user's previously saved preferences.
     * We read the grouping mode, sort field, and sort order from the plugin's preference
     * store and set the appropriate radio buttons and combo selection. This way the dialog
     * opens showing the current configuration, not some default.
     */
    private void initFieldsFromPreferences()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        int grouping = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING );
        if ( grouping == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_FOLDERS )
        {
            inFoldersButton.setSelection( true );
        }
        else if ( grouping == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_MIXED )
        {
            mixedButton.setSelection( true );
        }

        int sortingBy = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SORTING_BY );
        if ( sortingBy == PluginConstants.PREFS_SCHEMA_VIEW_SORTING_BY_FIRSTNAME )
        {
            sortingCombo.select( 0 );
        }
        else if ( sortingBy == PluginConstants.PREFS_SCHEMA_VIEW_SORTING_BY_OID )
        {
            sortingCombo.select( 1 );
        }

        int sortingOrder = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SORTING_ORDER );
        if ( sortingOrder == PluginConstants.PREFS_SCHEMA_VIEW_SORTING_ORDER_ASCENDING )
        {
            ascendingButton.setSelection( true );
        }
        else if ( sortingOrder == PluginConstants.PREFS_SCHEMA_VIEW_SORTING_ORDER_DESCENDING )
        {
            descendingButton.setSelection( true );
        }

    }


    // ── Obi-Wan Answers and Acts ──────────────────────────────────────────────
    // After Leia's message finishes, Obi-Wan makes his decision and acts on it.
    // "I must go to Alderaan." buttonPressed handles the OK case by reading the
    // user's final choices and writing them to the preference store, so the
    // Schema View will immediately pick up the new settings on its next refresh.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Handles button clicks in the dialog — specifically the OK button.
     * When the user clicks OK, we read the current state of all controls and
     * write the chosen grouping mode, sort field, and sort order to the plugin's
     * preference store. The preference store change fires a property change event
     * that the Schema View's controller is listening for, triggering a refresh.
     * Cancel just closes the dialog without saving anything.
     *
     * @param buttonId  the ID of the button that was clicked (from {@link IDialogConstants})
     */
    @Override
    protected void buttonPressed( int buttonId )
    {
        if ( buttonId == IDialogConstants.OK_ID )
        {
            IPreferenceStore store = Activator.getDefault().getPreferenceStore();
            if ( ( inFoldersButton.getSelection() ) && ( !mixedButton.getSelection() ) )
            {
                store.setValue( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING,
                    PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_FOLDERS );
            }
            else if ( ( !inFoldersButton.getSelection() ) && ( mixedButton.getSelection() ) )
            {
                store.setValue( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING,
                    PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_MIXED );
            }

            if ( sortingCombo.getItem( sortingCombo.getSelectionIndex() ).equals(
                Messages.getString( "SchemaViewSortingDialog.FirstName" ) ) ) //$NON-NLS-1$
            {
                store.setValue( PluginConstants.PREFS_SCHEMA_VIEW_SORTING_BY,
                    PluginConstants.PREFS_SCHEMA_VIEW_SORTING_BY_FIRSTNAME );
            }
            else if ( sortingCombo.getItem( sortingCombo.getSelectionIndex() ).equals(
                Messages.getString( "SchemaViewSortingDialog.OID" ) ) ) //$NON-NLS-1$
            {
                store.setValue( PluginConstants.PREFS_SCHEMA_VIEW_SORTING_BY,
                    PluginConstants.PREFS_SCHEMA_VIEW_SORTING_BY_OID );
            }

            if ( ascendingButton.getSelection() && !descendingButton.getSelection() )
            {
                store.setValue( PluginConstants.PREFS_SCHEMA_VIEW_SORTING_ORDER,
                    PluginConstants.PREFS_SCHEMA_VIEW_SORTING_ORDER_ASCENDING );
            }
            else if ( !ascendingButton.getSelection() && descendingButton.getSelection() )
            {
                store.setValue( PluginConstants.PREFS_SCHEMA_VIEW_SORTING_ORDER,
                    PluginConstants.PREFS_SCHEMA_VIEW_SORTING_ORDER_DESCENDING );
            }
        }

        super.buttonPressed( buttonId );
    }
}
