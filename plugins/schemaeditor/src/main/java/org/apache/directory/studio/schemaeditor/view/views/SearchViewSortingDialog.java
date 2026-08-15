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


// ── CLASS: SearchViewSortingDialog — Palpatine Issuing Order 66 ───────────────
// In Revenge of the Sith, Emperor Palpatine transmits Order 66 to clone
// commanders across the galaxy: a specific, precise configuration command that
// reorganizes everything — who is a target, in what order, immediately. Every
// commander receives the same instruction and executes it uniformly. The order
// changes how the whole system behaves.
// This dialog is that configuration command for the Search View: it lets the user
// configure how search results should be grouped (attribute types first, object
// classes first, or mixed) and in what sort order (name or OID, ascending or
// descending). OK writes the command to the preference store and the Search View
// reorganizes accordingly — immediately.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A modal dialog for configuring how search results are grouped and sorted in the
 * Search View. Users choose a grouping mode (attribute types first, object classes
 * first, or mixed), a sort field (first name or OID), and a sort direction
 * (ascending or descending). On OK we write all three to the plugin's preference
 * store, which fires change events that cause the Search View to re-sort immediately.
 * Think of it as Palpatine's Order 66: a precise configuration command that
 * reorganizes the whole result set.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchViewSortingDialog extends Dialog
{
    // UI Fields
    private Button attributeTypesFirst;
    private Button objectClassesFirst;
    private Button mixedButton;
    private Combo sortingCombo;
    private Button ascendingButton;
    private Button descendingButton;


    // ── The Emperor Drafts the Order ─────────────────────────────────────────
    // Palpatine doesn't transmit an order without first knowing who he's sending
    // it to — the parent shell binds the dialog to the right window. The constructor
    // captures that binding so JFace can center and modalize the dialog properly.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new sorting dialog bound to the given parent shell.
     * The parent shell is passed to the JFace {@link Dialog} superclass so this dialog
     * is modal (nothing in that window responds until OK or Cancel is clicked) and
     * centered over the parent.
     *
     * <p>For example — the Emperor addresses the right commanders:</p>
     * <pre>
     *   new SearchViewSortingDialog(parentShell)
     *   → super(parentShell)  // "attention: this order targets you"
     * </pre>
     *
     * @param parentShell  the SWT shell that owns this dialog
     */
    public SearchViewSortingDialog( Shell parentShell )
    {
        super( parentShell );
    }


    // ── The Order Receives Its Designation ───────────────────────────────────
    // Order 66 has a name — every order has a designation. configureShell sets
    // the window title so users see "View Sorting" rather than a generic title
    // bar when the dialog appears.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Sets the window title for this dialog.
     * JFace calls this before displaying the dialog. We set it to the localized
     * "View Sorting" string so the purpose of the dialog is immediately clear.
     *
     * @param newShell  the SWT shell for this dialog window
     */
    @Override
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "SearchViewSortingDialog.ViewSorting" ) ); //$NON-NLS-1$
    }


    // ── The Order's Contents Are Specified ───────────────────────────────────
    // Order 66 is detailed: which units, what action, with what priority.
    // The dialog content area specifies the same level of precision: three
    // grouping options (AT first, OC first, or mixed), the sort field combo,
    // and ascending vs. descending radio buttons. Pre-populated from saved prefs.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog's content area with Grouping and Sorting option groups.
     * The Grouping group has three radio buttons: Attribute Types first, Object Classes
     * first, or mixed. The Sorting group has a combo for sort field (first name or OID)
     * and radio buttons for ascending/descending. Pre-filled from preferences.
     *
     * <p>For example — the order's contents:</p>
     * <pre>
     *   createDialogArea(parent)
     *     → Grouping: [• AT first] [ OC first] [ Mixed]
     *     → Sorting:  SortBy:[FirstName v] [• Ascending] [ Descending]
     *     → initFieldsFromPreferences()
     * </pre>
     *
     * @param parent  the composite to build the dialog area inside
     * @return        the configured dialog area composite
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( SWT.FILL, SWT.FILL, true, true );
        composite.setLayoutData( gd );

        // Grouping Group
        Group groupingGroup = new Group( composite, SWT.NONE );
        groupingGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        groupingGroup.setText( Messages.getString( "SearchViewSortingDialog.Grouping" ) ); //$NON-NLS-1$
        groupingGroup.setLayout( new GridLayout() );

        // Attribute Types first Button
        attributeTypesFirst = new Button( groupingGroup, SWT.RADIO );
        attributeTypesFirst.setText( Messages.getString( "SearchViewSortingDialog.TypesFirst" ) ); //$NON-NLS-1$
        attributeTypesFirst.setEnabled( true );

        // Object Classes first Button
        objectClassesFirst = new Button( groupingGroup, SWT.RADIO );
        objectClassesFirst.setText( Messages.getString( "SearchViewSortingDialog.ClassesFirst" ) ); //$NON-NLS-1$
        objectClassesFirst.setEnabled( true );

        // Mixed Button
        mixedButton = new Button( groupingGroup, SWT.RADIO );
        mixedButton.setText( Messages.getString( "SearchViewSortingDialog.Mixed" ) ); //$NON-NLS-1$
        mixedButton.setEnabled( true );

        // Sorting Group
        Group sortingGroup = new Group( composite, SWT.NONE );
        sortingGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        sortingGroup.setText( Messages.getString( "SearchViewSortingDialog.Sorting" ) ); //$NON-NLS-1$
        sortingGroup.setLayout( new GridLayout() );
        Composite sortingGroupComposite = new Composite( sortingGroup, SWT.NONE );
        GridLayout gl = new GridLayout( 4, false );
        gl.marginHeight = gl.marginWidth = 0;
        sortingGroupComposite.setLayout( gl );
        sortingGroupComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Sort by Label
        Label sortByLabel = new Label( sortingGroupComposite, SWT.NONE );
        sortByLabel.setText( Messages.getString( "SearchViewSortingDialog.SortBy" ) ); //$NON-NLS-1$

        // Sorting Combo
        sortingCombo = new Combo( sortingGroupComposite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER );
        sortingCombo.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        sortingCombo
            .setItems( new String[]
                {
                    Messages.getString( "SearchViewSortingDialog.FirstName" ), Messages.getString( "SearchViewSortingDialog.OID" ) } ); //$NON-NLS-1$ //$NON-NLS-2$
        sortingCombo.setEnabled( true );

        // Ascending Button
        ascendingButton = new Button( sortingGroupComposite, SWT.RADIO );
        ascendingButton.setText( Messages.getString( "SearchViewSortingDialog.Ascending" ) ); //$NON-NLS-1$
        ascendingButton.setEnabled( true );

        // Descending Button
        descendingButton = new Button( sortingGroupComposite, SWT.RADIO );
        descendingButton.setText( Messages.getString( "SearchViewSortingDialog.Descending" ) ); //$NON-NLS-1$
        descendingButton.setEnabled( true );

        initFieldsFromPreferences();

        applyDialogFont( composite );
        return composite;
    }


    // ── The Emperor Reviews the Current Standing Orders ──────────────────────
    // Before Palpatine issues a new order, he knows what the standing orders are.
    // This method reads the current saved preferences and pre-selects the matching
    // radio buttons and combo item so the user sees where things currently stand.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Pre-populates the dialog controls with the currently saved preference values.
     * We read grouping mode, sort field, and sort order from the preference store and
     * set the matching radio buttons and combo selection. This way users see their current
     * configuration when the dialog opens, not some arbitrary default.
     */
    private void initFieldsFromPreferences()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        int grouping = store.getInt( PluginConstants.PREFS_SEARCH_VIEW_GROUPING );
        if ( grouping == PluginConstants.PREFS_SEARCH_VIEW_GROUPING_ATTRIBUTE_TYPES_FIRST )
        {
            attributeTypesFirst.setSelection( true );
        }
        else if ( grouping == PluginConstants.PREFS_SEARCH_VIEW_GROUPING_OBJECT_CLASSES_FIRST )
        {
            objectClassesFirst.setSelection( true );
        }
        else if ( grouping == PluginConstants.PREFS_SEARCH_VIEW_GROUPING_MIXED )
        {
            mixedButton.setSelection( true );
        }

        int sortingBy = store.getInt( PluginConstants.PREFS_SEARCH_VIEW_SORTING_BY );
        if ( sortingBy == PluginConstants.PREFS_SEARCH_VIEW_SORTING_BY_FIRSTNAME )
        {
            sortingCombo.select( 0 );
        }
        else if ( sortingBy == PluginConstants.PREFS_SEARCH_VIEW_SORTING_BY_OID )
        {
            sortingCombo.select( 1 );
        }

        int sortingOrder = store.getInt( PluginConstants.PREFS_SEARCH_VIEW_SORTING_ORDER );
        if ( sortingOrder == PluginConstants.PREFS_SEARCH_VIEW_SORTING_ORDER_ASCENDING )
        {
            ascendingButton.setSelection( true );
        }
        else if ( sortingOrder == PluginConstants.PREFS_SEARCH_VIEW_SORTING_ORDER_DESCENDING )
        {
            descendingButton.setSelection( true );
        }

    }


    // ── Execute Order 66 ─────────────────────────────────────────────────────
    // "Execute Order 66." The clone commanders carry out the order the moment it
    // arrives. buttonPressed handles OK by reading the dialog's final control state
    // and writing those choices to the preference store — which immediately fires
    // change events that the Search View picks up to re-sort its results.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Handles button clicks — specifically, saves configuration choices on OK.
     * When the user clicks OK, we read all control states and write the chosen
     * grouping mode, sort field, and sort order to the preference store. The store
     * fires property change events that the Search View controller listens for,
     * triggering an immediate re-sort of the displayed results. Cancel exits
     * without saving anything.
     *
     * @param buttonId  the ID of the button clicked (from {@link IDialogConstants})
     */
    @Override
    protected void buttonPressed( int buttonId )
    {
        if ( buttonId == IDialogConstants.OK_ID )
        {
            IPreferenceStore store = Activator.getDefault().getPreferenceStore();
            if ( ( attributeTypesFirst.getSelection() ) && ( !objectClassesFirst.getSelection() )
                && ( !mixedButton.getSelection() ) )
            {
                store.setValue( PluginConstants.PREFS_SEARCH_VIEW_GROUPING,
                    PluginConstants.PREFS_SEARCH_VIEW_GROUPING_ATTRIBUTE_TYPES_FIRST );
            }
            else if ( ( !attributeTypesFirst.getSelection() ) && ( objectClassesFirst.getSelection() )
                && ( !mixedButton.getSelection() ) )
            {
                store.setValue( PluginConstants.PREFS_SEARCH_VIEW_GROUPING,
                    PluginConstants.PREFS_SEARCH_VIEW_GROUPING_OBJECT_CLASSES_FIRST );
            }
            else if ( ( !attributeTypesFirst.getSelection() ) && ( !objectClassesFirst.getSelection() )
                && ( mixedButton.getSelection() ) )
            {
                store.setValue( PluginConstants.PREFS_SEARCH_VIEW_GROUPING,
                    PluginConstants.PREFS_SEARCH_VIEW_GROUPING_MIXED );
            }

            if ( sortingCombo.getItem( sortingCombo.getSelectionIndex() ).equals(
                Messages.getString( "SearchViewSortingDialog.FirstName" ) ) ) //$NON-NLS-1$
            {
                store.setValue( PluginConstants.PREFS_SEARCH_VIEW_SORTING_BY,
                    PluginConstants.PREFS_SEARCH_VIEW_SORTING_BY_FIRSTNAME );
            }
            else if ( sortingCombo.getItem( sortingCombo.getSelectionIndex() ).equals(
                Messages.getString( "SearchViewSortingDialog.OID" ) ) ) //$NON-NLS-1$
            {
                store.setValue( PluginConstants.PREFS_SEARCH_VIEW_SORTING_BY,
                    PluginConstants.PREFS_SEARCH_VIEW_SORTING_BY_OID );
            }

            if ( ascendingButton.getSelection() && !descendingButton.getSelection() )
            {
                store.setValue( PluginConstants.PREFS_SEARCH_VIEW_SORTING_ORDER,
                    PluginConstants.PREFS_SEARCH_VIEW_SORTING_ORDER_ASCENDING );
            }
            else if ( !ascendingButton.getSelection() && descendingButton.getSelection() )
            {
                store.setValue( PluginConstants.PREFS_SEARCH_VIEW_SORTING_ORDER,
                    PluginConstants.PREFS_SEARCH_VIEW_SORTING_ORDER_DESCENDING );
            }
        }

        super.buttonPressed( buttonId );
    }
}
