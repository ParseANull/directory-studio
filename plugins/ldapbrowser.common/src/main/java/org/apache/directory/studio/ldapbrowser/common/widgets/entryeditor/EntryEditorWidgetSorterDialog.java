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

package org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;


// -- CLASS: EntryEditorWidgetSorterDialog -- LANDO CONVENES A CLOUD CITY PRIORITY MEETING
// Lando Calrissian calls together the Cloud City dock administrators to formally agree on
// sorting protocols: should VIP ships always dock first? Should maintenance be last?
// Sort by call sign or by ship class? Ascending or descending?
// Each administrator casts a vote (checks a checkbox, picks from a dropdown, clicks a radio)
// and when they press OK, Lando records the decisions in the official protocol register
// (the Eclipse preference store). Next time the dock opens, those rules are in effect.
// ---------------------------------------------------------------------------------
/**
 * A modal dialog for configuring the entry editor's default sort preferences.
 * The user can choose how attributes are grouped (objectClass/must first, operational last),
 * which column drives the default sort (attribute name or value), and the sort direction
 * (ascending or descending). Pressing OK saves the choices to the Eclipse preference store.
 * Think of this class as Lando's Cloud City administrators' meeting: everyone votes on the
 * priority rules, and the result gets written into the official protocol register.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorWidgetSorterDialog extends Dialog
{
    /** The Constant DIALOG_TITLE. */
    public static final String DIALOG_TITLE = Messages.getString( "EntryEditorWidgetSorterDialog.EntryEditorSorting" ); //$NON-NLS-1$

    /** The Constant SORT_BY_NONE. */
    public static final String SORT_BY_NONE = Messages.getString( "EntryEditorWidgetSorterDialog.NoDefaultSorting" ); //$NON-NLS-1$

    /** The Constant SORT_BY_ATTRIBUTE. */
    public static final String SORT_BY_ATTRIBUTE = Messages
        .getString( "EntryEditorWidgetSorterDialog.AttributeDescription" ); //$NON-NLS-1$

    /** The Constant SORT_BY_VALUE. */
    public static final String SORT_BY_VALUE = Messages.getString( "EntryEditorWidgetSorterDialog.Value" ); //$NON-NLS-1$

    /** The preferences. */
    private EntryEditorWidgetPreferences preferences;

    /** The object class and must attributes first button. */
    private Button objectClassAndMustAttributesFirstButton;

    /** The operational attributes last button. */
    private Button operationalAttributesLastButton;

    /** The sort by combo. */
    private Combo sortByCombo;

    /** The sort acending button. */
    private Button sortAcendingButton;

    /** The sort descending button. */
    private Button sortDescendingButton;


    // -- LANDO OPENS THE MEETING ROOM -------------------------------------------
    // Lando greets the administrators and sets the agenda: we're here to review the
    // current dock-priority rules (read from preferences) and vote on any changes.
    // We store the preferences reference so createDialogArea() can pre-populate
    // the controls with the existing settings.
    // ---------------------------------------------------------------------------------
    /**
     * Creates a new sorter dialog ready to display and edit the current sort preferences.
     * The {@code preferences} object is queried during {@link #createDialogArea} to
     * pre-populate the controls with whatever the user has configured so far.
     *
     * <p>For example -- Lando opens the meeting:</p>
     * <pre>
     *   Lando: "Welcome, administrators. Today we review the dock-priority protocol.
     *            I have the current settings right here. Let's go through them."
     * </pre>
     *
     * @param parentShell  the SWT shell to use as the dialog parent
     * @param preferences  the current entry editor preferences -- read to populate controls,
     *                     then written to when the user presses OK
     */
    public EntryEditorWidgetSorterDialog( Shell parentShell, EntryEditorWidgetPreferences preferences )
    {
        super( parentShell );
        this.preferences = preferences;
    }


    // -- LANDO NAMES THE MEETING ROOM -------------------------------------------
    // Before the meeting starts, Lando stencils the room name on the door:
    // "Entry Editor Sorting."  This sets the title bar on the dialog shell so the
    // user knows what they're looking at.
    // ---------------------------------------------------------------------------------
    /**
     * Sets the title bar text on the dialog shell.
     * Called by the JFace Dialog framework before the dialog opens.
     *
     * <p>For example -- Lando labels the meeting room door:</p>
     * <pre>
     *   Lando stencils: "ENTRY EDITOR SORTING" on the conference room door.
     *   Administrators know exactly which meeting they're walking into.
     * </pre>
     *
     * @param newShell  the dialog's shell window to configure
     */
    @Override
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( DIALOG_TITLE );
    }


    // -- LANDO RECORDS THE VOTE AND ADJOURNS -----------------------------------
    // When the administrators press OK, Lando writes each decision into the
    // official protocol register (the Eclipse preference store). The updated
    // preferences take effect the next time the sorter runs. Cancel leaves
    // everything unchanged and just closes the meeting.
    // ---------------------------------------------------------------------------------
    /**
     * Saves the dialog's current control values to the Eclipse preference store when OK is pressed.
     * Each checkbox, radio button, and combo selection is persisted so the sorter picks
     * up the new rules on the next viewer refresh. Cancel falls through to the superclass
     * without writing anything.
     *
     * <p>For example -- Lando records the unanimous decisions:</p>
     * <pre>
     *   ✓ objectClass and must attributes first: YES
     *   ✓ operational attributes last: YES
     *   ✓ default sort by: Attribute Description, Ascending
     *   Lando signs the register. "Meeting adjourned. New protocols are in effect."
     * </pre>
     *
     * @param buttonId  the ID of the button pressed; {@link IDialogConstants#OK_ID} triggers the save
     */
    @Override
    protected void buttonPressed( int buttonId )
    {
        if ( buttonId == IDialogConstants.OK_ID )
        {
            IPreferenceStore store = BrowserCommonActivator.getDefault().getPreferenceStore();
            store.setValue( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_OBJECTCLASS_AND_MUST_ATTRIBUTES_FIRST,
                objectClassAndMustAttributesFirstButton.getSelection() );
            store.setValue( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_OPERATIONAL_ATTRIBUTES_LAST,
                operationalAttributesLastButton.getSelection() );
            store.setValue( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_DEFAULT_SORT_ORDER, sortDescendingButton
                .getSelection() ? BrowserCoreConstants.SORT_ORDER_DESCENDING
                : BrowserCoreConstants.SORT_ORDER_ASCENDING );
            store.setValue( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_DEFAULT_SORT_BY, sortByCombo
                .getSelectionIndex() == 2 ? BrowserCoreConstants.SORT_BY_VALUE
                : sortByCombo.getSelectionIndex() == 1 ? BrowserCoreConstants.SORT_BY_ATTRIBUTE_DESCRIPTION
                    : BrowserCoreConstants.SORT_BY_NONE );
        }

        super.buttonPressed( buttonId );
    }


    // -- LANDO LAYS OUT THE MEETING AGENDA -------------------------------------
    // Lando arranges the room: one section for grouping rules (objectClass first?
    // operational last?), one for sort column and direction. Each control is
    // pre-populated from the current preferences so the administrators can see
    // what the current settings are before voting to change them.
    // ---------------------------------------------------------------------------------
    /**
     * Builds the dialog's content area with two groups of controls.
     * The first group handles attribute-grouping preferences (objectClass/must first,
     * operational last). The second group handles sort column and direction.
     * All controls are pre-populated from the current {@code preferences} object.
     *
     * <p>For example -- Lando arranges the meeting agenda on the whiteboard:</p>
     * <pre>
     *   Group 1 "Attribute grouping":
     *     [✓] objectClass and must attributes first
     *     [✓] Operational attributes last
     *   Group 2 "Sort attributes":
     *     Sort by: [Attribute Description v]  (o) Ascending  ( ) Descending
     *   (Hint: You can also click column headers to sort.)
     * </pre>
     *
     * @param parent  the SWT composite provided by the Dialog framework as the dialog area
     * @return        the top-level composite containing all the controls
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );

        Group group = BaseWidgetUtils.createGroup( composite, Messages
            .getString( "EntryEditorWidgetSorterDialog.GroupAttributes" ), 1 ); //$NON-NLS-1$
        GridData gd = new GridData( GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        group.setLayoutData( gd );

        objectClassAndMustAttributesFirstButton = BaseWidgetUtils.createCheckbox( group, Messages
            .getString( "EntryEditorWidgetSorterDialog.ObjectClassAndMustAttributesFirst" ), 1 ); //$NON-NLS-1$
        objectClassAndMustAttributesFirstButton.setSelection( preferences.isObjectClassAndMustAttributesFirst() );

        operationalAttributesLastButton = BaseWidgetUtils.createCheckbox( group, Messages
            .getString( "EntryEditorWidgetSorterDialog.OperationalAttributesLast" ), 1 ); //$NON-NLS-1$
        operationalAttributesLastButton.setSelection( preferences.isOperationalAttributesLast() );

        Group sortingGroup = BaseWidgetUtils.createGroup( composite, Messages
            .getString( "EntryEditorWidgetSorterDialog.SortAttributes" ), 1 ); //$NON-NLS-1$

        Composite sortByComposite = BaseWidgetUtils.createColumnContainer( sortingGroup, 4, 1 );
        BaseWidgetUtils.createLabel( sortByComposite, Messages.getString( "EntryEditorWidgetSorterDialog.SortBy" ), 1 ); //$NON-NLS-1$
        sortByCombo = BaseWidgetUtils.createReadonlyCombo( sortByComposite, new String[]
            { SORT_BY_NONE, SORT_BY_ATTRIBUTE, SORT_BY_VALUE }, 0, 1 );
        sortByCombo.select( preferences.getDefaultSortBy() == BrowserCoreConstants.SORT_BY_VALUE ? 2 : preferences
            .getDefaultSortBy() == BrowserCoreConstants.SORT_BY_ATTRIBUTE_DESCRIPTION ? 1 : 0 );
        sortByCombo.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                sortAcendingButton.setEnabled( sortByCombo.getSelectionIndex() != 0 );
                sortDescendingButton.setEnabled( sortByCombo.getSelectionIndex() != 0 );
            }
        } );

        sortAcendingButton = BaseWidgetUtils.createRadiobutton( sortByComposite, Messages
            .getString( "EntryEditorWidgetSorterDialog.Ascending" ), 1 ); //$NON-NLS-1$
        sortAcendingButton
            .setSelection( preferences.getDefaultSortOrder() == BrowserCoreConstants.SORT_ORDER_ASCENDING );
        sortAcendingButton.setEnabled( sortByCombo.getSelectionIndex() != 0 );

        sortDescendingButton = BaseWidgetUtils.createRadiobutton( sortByComposite, Messages
            .getString( "EntryEditorWidgetSorterDialog.Descending" ), 1 ); //$NON-NLS-1$
        sortDescendingButton
            .setSelection( preferences.getDefaultSortOrder() == BrowserCoreConstants.SORT_ORDER_DESCENDING );
        sortDescendingButton.setEnabled( sortByCombo.getSelectionIndex() != 0 );

        BaseWidgetUtils.createSpacer( composite, 2 );

        BaseWidgetUtils.createLabel( composite, Messages.getString( "EntryEditorWidgetSorterDialog.SortTableHint" ), 1 ); //$NON-NLS-1$

        applyDialogFont( composite );
        return composite;
    }
}
