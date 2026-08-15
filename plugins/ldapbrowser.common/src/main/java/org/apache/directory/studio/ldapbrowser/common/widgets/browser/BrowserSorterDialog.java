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

package org.apache.directory.studio.ldapbrowser.common.widgets.browser;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// ── CLASS: BrowserSorterDialog — Lando's Cloud City Priority Meeting ──────────
// In The Empire Strikes Back, Lando calls his Cloud City administrators into the
// conference room to set the rules: who gets priority docking, how the guest
// ledger is ordered, and when the system is overloaded enough to stop sorting at
// all. This dialog is that meeting — it lets the user set every sort preference
// for the LDAP browser tree in one place and saves them when they click OK.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A modal dialog that lets the user configure all browser sort preferences:
 * entry grouping (leaf-first, container-first, mixed), sort field (none, RDN,
 * RDN value), ascending/descending order for entries, searches and bookmarks,
 * and the maximum number of entries to sort at once (the sort limit).
 * When the user clicks OK we write every setting to the Eclipse preference
 * store so they persist across sessions.
 * Think of this dialog as Lando's priority-rules meeting for Cloud City.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserSorterDialog extends Dialog
{
    /** The dialog title. */
    public static final String DIALOG_TITLE = Messages.getString( "BrowserSorterDialog.BrowserSorting" ); //$NON-NLS-1$

    /** The Constant SORT_BY_NONE. */
    public static final String SORT_BY_NONE = Messages.getString( "BrowserSorterDialog.NoSorting" ); //$NON-NLS-1$

    /** The Constant SORT_BY_RDN. */
    public static final String SORT_BY_RDN = Messages.getString( "BrowserSorterDialog.RDN" ); //$NON-NLS-1$

    /** The Constant SORT_BY_RDN_VALUE. */
    public static final String SORT_BY_RDN_VALUE = Messages.getString( "BrowserSorterDialog.RDNValue" ); //$NON-NLS-1$

    /** The browser preferences. */
    private BrowserPreferences preferences;

    /** The leaf entries first button. */
    private Button leafEntriesFirstButton;

    /** The container entries first button. */
    private Button containerEntriesFirstButton;

    /** The mixed button. */
    private Button mixedButton;

    /** The meta entries last button. */
    private Button metaEntriesLastButton;

    /** The sort entries by combo. */
    private Combo sortEntriesByCombo;

    /** The sort entries ascending button. */
    private Button sortEntriesAscendingButton;

    /** The sort entries descending button. */
    private Button sortEntriesDescendingButton;

    /** The sort searches ascending button. */
    private Button sortSearchesAscendingButton;

    /** The sort searches descending button. */
    private Button sortSearchesDescendingButton;

    /** The sort searches none button. */
    private Button sortSearchesNoSortingButton;

    /** The sort bookmarks ascending button. */
    private Button sortBookmarksAscendingButton;

    /** The sort bookmarks descending button. */
    private Button sortBookmarksDescendingButton;

    /** The sort bookmarks none button. */
    private Button sortBookmarksNoSortingButton;

    /** The sort limit text. */
    private Text sortLimitText;


    // ── LANDO OPENS THE CONFERENCE ROOM DOOR ──────────────────────────────────
    // Lando unlocks the Cloud City boardroom and lays out the agenda binder.
    // The preferences object is his copy of the current rules — he'll read
    // from it to pre-populate every control, and write back to the store on OK.
    // The parentShell is the Eclipse workbench window that owns this dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new sort-settings dialog, pre-populated with the current
     * preferences so the user can see what's already configured.
     * The dialog is modal — it blocks the parent shell until dismissed.
     *
     * @param parentShell  The Eclipse shell that this dialog should be centred
     *                     on and parented to.
     * @param preferences  The current browser preferences from which we read
     *                     initial values and to which (via the preference store)
     *                     we save changes on OK.
     */
    public BrowserSorterDialog( Shell parentShell, BrowserPreferences preferences )
    {
        super( parentShell );
        this.preferences = preferences;
    }


    // ── LANDO WRITES THE AGENDA TITLE ON THE WHITEBOARD ───────────────────────
    // Before the meeting begins, Lando grabs a marker and writes "Cloud City
    // Priority Rules" at the top of the board so everyone knows why they're here.
    // We do the same: set the dialog window title so the user knows what they're
    // configuring before they even read the first control.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the window title to {@link #DIALOG_TITLE} so the user sees
     * "Browser Sorting" in the title bar immediately.
     * We call the superclass first to handle all the standard shell setup
     * (modality, size hints, etc.) and then stamp our title on top.
     *
     * @param newShell  The freshly created shell for this dialog.
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( DIALOG_TITLE );
    }


    // ── LANDO ADJOURNS THE MEETING AND PUBLISHES THE NEW RULES ────────────────
    // When the administrators click "Agreed," Lando collects every decision from
    // the whiteboard and publishes them city-wide: leaf order, sort field,
    // ascending/descending, searches order, bookmarks order, sort limit. If they
    // hit "Cancel" instead, he tears up the notes — no changes saved.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Intercepts the dialog button click. When OK is pressed, every widget
     * value is read and written to the Eclipse {@link IPreferenceStore} so
     * the {@link BrowserSorter} picks up the new settings on the next refresh.
     * When Cancel is pressed we skip the write — the preferences are unchanged.
     *
     * <p>For example — Lando finalises the priority schedule:</p>
     * <pre>
     *   if (buttonId == OK) {
     *       publish(leafFirst, sortBy, order, searchesOrder, bookmarksOrder, limit);
     *   }
     *   // else: crumple the notes, leave the old rules in place
     * </pre>
     *
     * @param buttonId  The JFace dialog button constant, typically
     *                  {@link IDialogConstants#OK_ID} or
     *                  {@link IDialogConstants#CANCEL_ID}.
     */
    protected void buttonPressed( int buttonId )
    {
        if ( buttonId == IDialogConstants.OK_ID )
        {
            int sortLimit = preferences.getSortLimit();
            try
            {
                sortLimit = Integer.parseInt( sortLimitText.getText().trim() );
            }
            catch ( NumberFormatException nfe )
            {
            }

            IPreferenceStore store = BrowserCommonActivator.getDefault().getPreferenceStore();

            store.setValue( BrowserCommonConstants.PREFERENCE_BROWSER_LEAF_ENTRIES_FIRST, leafEntriesFirstButton
                .getSelection() );
            store.setValue( BrowserCommonConstants.PREFERENCE_BROWSER_CONTAINER_ENTRIES_FIRST,
                containerEntriesFirstButton.getSelection() );
            store.setValue( BrowserCommonConstants.PREFERENCE_BROWSER_META_ENTRIES_LAST, metaEntriesLastButton
                .getSelection() );

            store.setValue( BrowserCommonConstants.PREFERENCE_BROWSER_SORT_ORDER,
                sortEntriesDescendingButton.getSelection() ? BrowserCoreConstants.SORT_ORDER_DESCENDING
                    : BrowserCoreConstants.SORT_ORDER_ASCENDING );
            store.setValue( BrowserCommonConstants.PREFERENCE_BROWSER_SORT_BY,
                sortEntriesByCombo.getSelectionIndex() == 2 ? BrowserCoreConstants.SORT_BY_RDN_VALUE
                    : sortEntriesByCombo
                        .getSelectionIndex() == 1 ? BrowserCoreConstants.SORT_BY_RDN
                        : BrowserCoreConstants.SORT_BY_NONE );
            store.setValue( BrowserCommonConstants.PREFERENCE_BROWSER_SORT_LIMIT, sortLimit );

            if ( sortSearchesAscendingButton.getSelection() )
            {
                store.setValue( BrowserCommonConstants.PREFERENCE_BROWSER_SORT_SEARCHES_ORDER,
                    BrowserCoreConstants.SORT_ORDER_ASCENDING );
            }
            else if ( sortSearchesDescendingButton.getSelection() )
            {
                store.setValue( BrowserCommonConstants.PREFERENCE_BROWSER_SORT_SEARCHES_ORDER,
                    BrowserCoreConstants.SORT_ORDER_DESCENDING );
            }
            else if ( sortSearchesNoSortingButton.getSelection() )
            {
                store.setValue( BrowserCommonConstants.PREFERENCE_BROWSER_SORT_SEARCHES_ORDER,
                    BrowserCoreConstants.SORT_ORDER_NONE );
            }

            if ( sortBookmarksAscendingButton.getSelection() )
            {
                store.setValue( BrowserCommonConstants.PREFERENCE_BROWSER_SORT_BOOKMARKS_ORDER,
                    BrowserCoreConstants.SORT_ORDER_ASCENDING );
            }
            else if ( sortBookmarksDescendingButton.getSelection() )
            {
                store.setValue( BrowserCommonConstants.PREFERENCE_BROWSER_SORT_BOOKMARKS_ORDER,
                    BrowserCoreConstants.SORT_ORDER_DESCENDING );
            }
            else if ( sortBookmarksNoSortingButton.getSelection() )
            {
                store.setValue( BrowserCommonConstants.PREFERENCE_BROWSER_SORT_BOOKMARKS_ORDER,
                    BrowserCoreConstants.SORT_ORDER_NONE );
            }
        }
        else
        {
            // no changes
        }

        super.buttonPressed( buttonId );
    }


    // ── LANDO SETS OUT ALL THE AGENDA SECTIONS ────────────────────────────────
    // Before the meeting can start, the conference room needs to be laid out:
    // one section for entry grouping, one for entry sort order, one for searches,
    // one for bookmarks, and one for the sort limit. Lando arranges the tables
    // and chairs — we arrange the SWT groups inside the dialog composite.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the entire dialog content area by calling each
     * {@code createXxxGroup} helper in order.
     * We size the composite to at least {@link IDialogConstants#MINIMUM_MESSAGE_AREA_WIDTH}
     * wide so the groups don't look squished on small screens.
     *
     * @param parent  The parent composite provided by the Dialog framework.
     * @return  The top-level composite containing all our controls.
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        composite.setLayoutData( gd );

        createGroupEntriesGroup( composite );
        createSortEntriesGroup( composite );
        createSortSearchesGroup( composite );
        createSortBookmarksGroup( composite );
        createSortLimitGroup( composite );

        applyDialogFont( composite );
        return composite;
    }


    // ── LANDO SETS UP THE "GUEST GROUPING" SECTION ────────────────────────────
    // The first agenda item: do leaf-node citizens come before container-node
    // administrators, or the other way around, or mixed? And do meta-system
    // droids always go last? Lando puts three radio buttons on the table and
    // a separate checkbox for the "droids last" policy.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Group Entries" section of the dialog with radio buttons for
     * leaf-first, container-first, and mixed modes, plus a checkbox to send
     * meta entries (aliases, referrals, RootDSE) to the bottom of the list.
     * Each control is pre-selected based on the current preferences.
     *
     * @param composite  The parent composite to add this group to.
     */
    private void createGroupEntriesGroup( Composite composite )
    {
        // Group entries group and composite
        Group groupEntriesGroup = BaseWidgetUtils.createGroup( composite, Messages
            .getString( "BrowserSorterDialog.GroupEntries" ), 1 ); //$NON-NLS-1$
        Composite groupEntriesButtonsComposite = BaseWidgetUtils.createColumnContainer( groupEntriesGroup, 3, 1 );

        // Leaf entries first button
        leafEntriesFirstButton = BaseWidgetUtils.createRadiobutton( groupEntriesButtonsComposite, Messages
            .getString( "BrowserSorterDialog.LeafEntriesFirst" ), 1 ); //$NON-NLS-1$
        leafEntriesFirstButton.setToolTipText( Messages.getString( "BrowserSorterDialog.LeafEntriesFirstToolTip" ) ); //$NON-NLS-1$
        leafEntriesFirstButton.setSelection( preferences.isLeafEntriesFirst() );

        // Container entries first button
        containerEntriesFirstButton = BaseWidgetUtils.createRadiobutton( groupEntriesButtonsComposite, Messages
            .getString( "BrowserSorterDialog.ContainerEntriesFirst" ), 1 ); //$NON-NLS-1$
        containerEntriesFirstButton.setToolTipText( Messages
            .getString( "BrowserSorterDialog.ContainerEntriesFirstToolTip" ) ); //$NON-NLS-1$
        containerEntriesFirstButton.setSelection( preferences.isContainerEntriesFirst() );

        // Mixed button
        mixedButton = BaseWidgetUtils.createRadiobutton( groupEntriesButtonsComposite,
            Messages.getString( "BrowserSorterDialog.Mixed" ), 1 ); //$NON-NLS-1$
        mixedButton.setToolTipText( Messages.getString( "BrowserSorterDialog.MixedToolTip" ) ); //$NON-NLS-1$
        mixedButton.setSelection( !preferences.isLeafEntriesFirst() && !preferences.isContainerEntriesFirst() );

        // Meta entries last button
        metaEntriesLastButton = BaseWidgetUtils.createCheckbox( groupEntriesGroup, Messages
            .getString( "BrowserSorterDialog.MetaEntriesLast" ), 1 ); //$NON-NLS-1$
        metaEntriesLastButton.setToolTipText( Messages.getString( "BrowserSorterDialog.MetaEntriesLastToolTip" ) ); //$NON-NLS-1$
        metaEntriesLastButton.setSelection( preferences.isMetaEntriesLast() );
    }


    // ── LANDO SETS UP THE "ENTRY ORDER" SECTION ───────────────────────────────
    // Second agenda item: when we do sort entries, what do we sort by —
    // nothing, the full RDN string, or just the RDN's value? And ascending or
    // descending? Lando puts a dropdown and two radio buttons on the table.
    // The ascending/descending buttons disable automatically when "no sort" is
    // selected, because direction only makes sense when there's something to sort.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Sort Entries" section with a combo to pick the sort field
     * (none / RDN / RDN value) and radio buttons for ascending vs. descending.
     * The ascending/descending buttons are greyed out when "no sorting" is
     * selected because a direction setting has no meaning without a sort key.
     * All controls are pre-selected from the current preferences.
     *
     * @param composite  The parent composite to add this group to.
     */
    private void createSortEntriesGroup( Composite composite )
    {
        // Sort entries group and composite
        Group sortEntriesGroup = BaseWidgetUtils.createGroup( composite, Messages
            .getString( "BrowserSorterDialog.SortEntries" ), 1 ); //$NON-NLS-1$
        Composite sortByComposite = BaseWidgetUtils.createColumnContainer( sortEntriesGroup, 4, 1 );

        // Sort entries by combo
        BaseWidgetUtils.createLabel( sortByComposite, Messages.getString( "BrowserSorterDialog.SortBy" ), 1 ); //$NON-NLS-1$
        sortEntriesByCombo = BaseWidgetUtils.createReadonlyCombo( sortByComposite, new String[]
            { SORT_BY_NONE, SORT_BY_RDN, SORT_BY_RDN_VALUE }, 0, 1 );
        sortEntriesByCombo.select( preferences.getSortEntriesBy() == BrowserCoreConstants.SORT_BY_RDN_VALUE ? 2
            : preferences
                .getSortEntriesBy() == BrowserCoreConstants.SORT_BY_RDN ? 1 : 0 );
        sortEntriesByCombo.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                sortEntriesAscendingButton.setEnabled( sortEntriesByCombo.getSelectionIndex() != 0 );
                sortEntriesDescendingButton.setEnabled( sortEntriesByCombo.getSelectionIndex() != 0 );
            }
        } );

        // Sort entries ascending button
        sortEntriesAscendingButton = BaseWidgetUtils.createRadiobutton( sortByComposite, Messages
            .getString( "BrowserSorterDialog.Ascending" ), 1 ); //$NON-NLS-1$
        sortEntriesAscendingButton
            .setSelection( preferences.getSortEntriesOrder() == BrowserCoreConstants.SORT_ORDER_ASCENDING );
        sortEntriesAscendingButton.setEnabled( sortEntriesByCombo.getSelectionIndex() != 0 );

        // Sort entries descending button
        sortEntriesDescendingButton = BaseWidgetUtils.createRadiobutton( sortByComposite, Messages
            .getString( "BrowserSorterDialog.Descending" ), 1 ); //$NON-NLS-1$
        sortEntriesDescendingButton
            .setSelection( preferences.getSortEntriesOrder() == BrowserCoreConstants.SORT_ORDER_DESCENDING );
        sortEntriesDescendingButton.setEnabled( sortEntriesByCombo.getSelectionIndex() != 0 );
    }


    // ── LANDO SETS UP THE "SEARCH ORDER" SECTION ──────────────────────────────
    // Third agenda item: how should saved searches be ordered in the tree?
    // Ascending alphabetically, descending, or left unsorted in the order they
    // were created? Lando puts three radio buttons on the next section of the table.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Sort Searches" section with three radio buttons for
     * ascending, descending, and no-sort ordering of saved searches in the tree.
     * Pre-selected from the current preference.
     *
     * @param parent  The parent composite to add this group to.
     */
    private void createSortSearchesGroup( Composite parent )
    {
        // Sort searches group and composite
        Group sortSearchesGroup = BaseWidgetUtils.createGroup( parent, Messages
            .getString( "BrowserSorterDialog.SortSearches" ), 1 ); //$NON-NLS-1$
        Composite sortSearchesComposite = BaseWidgetUtils.createColumnContainer( sortSearchesGroup, 3, 1 );

        // Sort searches ascending button
        sortSearchesAscendingButton = BaseWidgetUtils.createRadiobutton( sortSearchesComposite, Messages
            .getString( "BrowserSorterDialog.Ascending" ), 1 ); //$NON-NLS-1$
        sortSearchesAscendingButton
            .setSelection( preferences.getSortSearchesOrder() == BrowserCoreConstants.SORT_ORDER_ASCENDING );

        // Sort searches descending button
        sortSearchesDescendingButton = BaseWidgetUtils.createRadiobutton( sortSearchesComposite, Messages
            .getString( "BrowserSorterDialog.Descending" ), 1 ); //$NON-NLS-1$
        sortSearchesDescendingButton
            .setSelection( preferences.getSortSearchesOrder() == BrowserCoreConstants.SORT_ORDER_DESCENDING );

        // Sort searches none button
        sortSearchesNoSortingButton = BaseWidgetUtils.createRadiobutton( sortSearchesComposite, Messages
            .getString( "BrowserSorterDialog.NoSorting" ), 1 ); //$NON-NLS-1$
        sortSearchesNoSortingButton
            .setSelection( preferences.getSortSearchesOrder() == BrowserCoreConstants.SORT_ORDER_NONE );
    }


    // ── LANDO SETS UP THE "BOOKMARK ORDER" SECTION ────────────────────────────
    // Fourth agenda item: same question but for bookmarks — the user's favourite
    // LDAP entries pinned for quick access. Same three options: ascending,
    // descending, or keep them in the order the user added them.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Sort Bookmarks" section with three radio buttons for
     * ascending, descending, and no-sort ordering of bookmarks in the tree.
     * Pre-selected from the current preference.
     *
     * @param parent  The parent composite to add this group to.
     */
    private void createSortBookmarksGroup( Composite parent )
    {
        // Sort bookmarks group and composite
        Group sortBookmarksGroup = BaseWidgetUtils.createGroup( parent, Messages
            .getString( "BrowserSorterDialog.SortBookmarks" ), 1 ); //$NON-NLS-1$
        Composite sortBookmarksComposite = BaseWidgetUtils.createColumnContainer( sortBookmarksGroup, 3, 1 );

        // Sort bookmarks ascending button
        sortBookmarksAscendingButton = BaseWidgetUtils.createRadiobutton( sortBookmarksComposite, Messages
            .getString( "BrowserSorterDialog.Ascending" ), 1 ); //$NON-NLS-1$
        sortBookmarksAscendingButton
            .setSelection( preferences.getSortBookmarksOrder() == BrowserCoreConstants.SORT_ORDER_ASCENDING );

        // Sort bookmarks descending button
        sortBookmarksDescendingButton = BaseWidgetUtils.createRadiobutton( sortBookmarksComposite, Messages
            .getString( "BrowserSorterDialog.Descending" ), 1 ); //$NON-NLS-1$
        sortBookmarksDescendingButton
            .setSelection( preferences.getSortBookmarksOrder() == BrowserCoreConstants.SORT_ORDER_DESCENDING );

        // Sort bookmarks none button
        sortBookmarksNoSortingButton = BaseWidgetUtils.createRadiobutton( sortBookmarksComposite, Messages
            .getString( "BrowserSorterDialog.NoSorting" ), 1 ); //$NON-NLS-1$
        sortBookmarksNoSortingButton
            .setSelection( preferences.getSortBookmarksOrder() == BrowserCoreConstants.SORT_ORDER_NONE );
    }


    // ── LANDO SETS THE CITY'S CAPACITY LIMIT ──────────────────────────────────
    // Final agenda item: "How many guests can we sort before the system locks up?"
    // Lando writes a number on the board — say 10,000 — and once the crowd
    // exceeds that, we skip sorting to keep things moving. The VerifyListener
    // enforces digits-only so no one writes "many" in the box.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Sort Limit" section with a text field for entering the
     * maximum number of elements to sort at once.
     * When the count of tree children exceeds this limit, {@link BrowserSorter}
     * skips sorting entirely to keep the UI responsive. A
     * {@link VerifyListener} enforces that only digits can be typed.
     *
     * @param composite  The parent composite to add this group to.
     */
    private void createSortLimitGroup( Composite composite )
    {
        // Sort limit group and composite
        Group sortLimitGroup = BaseWidgetUtils.createGroup( composite, Messages
            .getString( "BrowserSorterDialog.SortLimit" ), 1 ); //$NON-NLS-1$
        Composite sortLimitComposite = BaseWidgetUtils.createColumnContainer( sortLimitGroup, 2, 1 );

        // Sort limit text
        String sortLimitTooltip = Messages.getString( "BrowserSorterDialog.SortLimitToolTip" ); //$NON-NLS-1$
        Label sortLimitLabel = BaseWidgetUtils.createLabel( sortLimitComposite, Messages
            .getString( "BrowserSorterDialog.SortLimitColon" ), 1 ); //$NON-NLS-1$
        sortLimitLabel.setToolTipText( sortLimitTooltip );
        sortLimitText = BaseWidgetUtils.createText( sortLimitComposite, "" + preferences.getSortLimit(), 5, 1 ); //$NON-NLS-1$
        sortLimitText.setToolTipText( sortLimitTooltip );
        sortLimitText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
                {
                    e.doit = false;
                }
            }
        } );
    }
}
