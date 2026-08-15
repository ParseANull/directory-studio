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

package org.apache.directory.studio.ldapbrowser.common.widgets.search;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.ui.HistoryUtils;
import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.core.DnUtils;
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.dialogs.SelectEntryDialog;
import org.apache.directory.studio.ldapbrowser.core.jobs.ReadEntryRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: EntryWidget — R2-D2 Pinpointing the Exact Node in the Death Star Directory ──
// In A New Hope, R2-D2 plugs into the Death Star's computer terminal and navigates the
// massive directory structure to find Detention Block AA-23 — the exact DN-path to
// Princess Leia's cell. He can type the full path if he already knows it, climb up
// one level to the parent corridor, or browse the entire directory tree visually.
// This widget does exactly that: type a DN, step up to the parent, or browse the tree.
// It also supports a multi-select mode for when R2 needs to pinpoint multiple cells.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * An SWT widget for selecting one or more LDAP entries by their Distinguished Name (DN).
 * A DN is the full path to an entry in the directory tree, like a file path — for
 * example {@code cn=Leia,ou=Rebels,dc=galaxy,dc=far,dc=away}.
 * Think of this class as R2-D2 at the Death Star terminal: he can type the exact path,
 * step up one corridor at a time with the "Up" button, or browse the full tree.
 *
 * <p>Two modes are supported:</p>
 * <ul>
 *   <li><b>Single-select</b> — a combo field, an Up button, and a Browse button.</li>
 *   <li><b>Multi-select</b> — a scrollable list with Add and Remove buttons.</li>
 * </ul>
 * Used by {@link SearchPageWrapper} as the search-base picker.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryWidget extends AbstractWidget
{
    // ---- single-select fields ----

    /** The Dn combo. */
    private Combo dnCombo;

    /** The up button. */
    private Button upButton;

    /** The entry browse button. */
    private Button entryBrowseButton;

    /** The selected Dn. */
    private Dn dn;

    /** The suffix. */
    private Dn suffix;

    /** Flag indicating if using local name for the dn */
    boolean useLocalName;

    // ---- multi-select fields ----

    /** True when the widget operates in multi-select mode. */
    private boolean multiSelect;

    /** SWT list showing the selected DNs (multi-select mode). */
    private org.eclipse.swt.widgets.List dnListControl;

    /** Ordered list of DNs managed in multi-select mode. */
    private List<Dn> dns;

    /** Button to open the entry browser and add entries (multi-select mode). */
    private Button addButton;

    /** Button to remove the currently highlighted entries (multi-select mode). */
    private Button removeButton;

    // ---- shared fields ----

    /** The connection. */
    private IBrowserConnection browserConnection;


    // ── R2 Boots Up With No Destination in Memory ────────────────────────────────────
    // R2-D2 has just been reset — his memory banks are clear and he has no connection
    // to any Death Star sector, no target corridor, and no mission parameters yet.
    // We create a blank single-select widget; the caller will set connection and DN later.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a blank single-select widget with no connection and no DN pre-loaded.
     * Use this when building a fresh search dialog where the user will fill everything in.
     *
     * <p>For example — R2 starts up from a cold boot:</p>
     * <pre>
     *   R2.connection = null;
     *   R2.targetPath = null;
     *   R2.mode = SINGLE_SELECT;
     * </pre>
     */
    public EntryWidget()
    {
        browserConnection = null;
        dn = null;
        multiSelect = false;
    }


    // ── R2 Arrives With Leia's Exact Cell Location Already Loaded ────────────────────
    // Obi-Wan has already briefed R2: "Detention Block AA-23, level 5." R2 knows exactly
    // which server to connect to and which entry path to start at.
    // We store connection and DN so the widget shows them immediately on render.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a single-select widget pre-loaded with a connection and a starting DN.
     * Good for editing an existing saved search that already has a search base set.
     *
     * <p>For example — R2 arrives briefed:</p>
     * <pre>
     *   R2.connection = deathStarTerminal;
     *   R2.targetPath = "ou=DetentionBlock,dc=deathstar";
     * </pre>
     *
     * @param browserConnection  The LDAP server connection to browse against.
     * @param dn                 The Distinguished Name to pre-populate in the combo field.
     */
    public EntryWidget( IBrowserConnection browserConnection, Dn dn )
    {
        this( browserConnection, dn, null, false );
    }


    // ── R2 Gets a Relative Path Inside a Known Sector ────────────────────────────────
    // R2 is given a path relative to the prison sector's root, not the entire Death Star.
    // A suffix tells him where that root is; the useLocalName flag says whether to trim
    // the suffix back off when reporting the result to the Rebels.
    // We forward to the four-arg constructor for full initialisation.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a single-select widget with a connection, a DN, a suffix DN (the root of
     * the subtree we're browsing within), and a flag for local-name mode.
     * Local-name mode strips the suffix from the DN before displaying it, which is
     * handy when a schema partition has its own root that shouldn't clutter the UI.
     *
     * <p>For example — R2 navigates relative to the prison sector root:</p>
     * <pre>
     *   suffix       = "ou=PrisonSector,dc=deathstar";
     *   targetPath   = "ou=DetentionBlock";     // local, without suffix
     *   useLocalName = true;
     * </pre>
     *
     * @param browserConnection  The LDAP server connection to use.
     * @param dn                 The initial DN (may be local if useLocalName is true).
     * @param suffix             The DN prefix that identifies the subtree root. May be null.
     * @param useLocalName       When true, the suffix is stripped from display and from
     *                           values returned to callers.
     */
    public EntryWidget( IBrowserConnection browserConnection, Dn dn, Dn suffix, boolean useLocalName )
    {
        this.browserConnection = browserConnection;
        this.dn = dn;
        this.suffix = suffix;
        this.useLocalName = useLocalName;
        this.multiSelect = false;
    }


    // ── R2 Gets a Hit-List of Multiple Cells to Find ─────────────────────────────────
    // The Rebel briefing assigns R2 a list of targets across multiple detention blocks.
    // He needs to track all of them and let the mission commander add or remove cells
    // from the list as the plan evolves.
    // We switch to multi-select mode and seed the list from the provided DNs.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a multi-select widget pre-loaded with a list of DNs.
     * Use this when the form needs the user to manage several entries at once —
     * for example, when assigning multiple members to an LDAP group.
     *
     * <p>For example — R2 is handed a target list:</p>
     * <pre>
     *   targets = [ "ou=BlockAA23", "ou=BlockAA24", "ou=BlockAA25" ];
     *   R2.mode = MULTI_SELECT;
     *   R2.targets.addAll( targets );
     * </pre>
     *
     * @param browserConnection  The LDAP server connection used by the Add dialog.
     * @param initialDns         The DNs to pre-populate the list with. May be null or empty.
     */
    public EntryWidget( IBrowserConnection browserConnection, Dn[] initialDns )
    {
        this.browserConnection = browserConnection;
        this.dns = new ArrayList<>( initialDns != null ? Arrays.asList( initialDns ) : new ArrayList<Dn>() );
        this.multiSelect = true;
    }


    // ── R2 Chooses His Interface Mode and Wires the Controls ─────────────────────────
    // R2 looks at his mission parameters: single target or a hit-list? He picks the
    // matching terminal interface — compact combo with nav buttons, or list with Add/Remove.
    // We branch on the multiSelect flag and delegate to the appropriate builder method.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds and lays out all SWT controls inside the given parent composite.
     * Delegates to {@link #createSingleSelectWidget} or {@link #createMultiSelectWidget}
     * depending on the mode chosen at construction time. Call this exactly once.
     *
     * <p>For example — R2 selects the right interface panel:</p>
     * <pre>
     *   if ( singleTarget ) buildComboWithNavButtons( parent );
     *   else                buildListWithAddRemove( parent );
     * </pre>
     *
     * @param parent  The SWT composite that will host the controls.
     */
    public void createWidget( final Composite parent )
    {
        if ( multiSelect )
        {
            createMultiSelectWidget( parent );
        }
        else
        {
            createSingleSelectWidget( parent );
        }
    }


    // ── R2 Builds the Single-Target Navigation Panel ──────────────────────────────────
    // R2 installs a combo field for the path, an Up arrow to climb to the parent corridor,
    // and a Browse button to pull up the full directory tree. The combo also remembers
    // previously visited paths via the dialog history system.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Internal helper that creates the single-select layout: a DN combo with history
     * drop-down, an Up button that navigates to the parent DN, and a Browse button
     * that opens {@link SelectEntryDialog} for tree-based selection.
     * Not intended to be called directly — use {@link #createWidget} instead.
     *
     * <p>For example — R2 builds his single-target console:</p>
     * <pre>
     *   pathField  = new Combo();       // type or pick from history
     *   upButton   = new Button( "^" ); // climb one level
     *   browseBtn  = new Button( "Browse" ); // open tree dialog
     * </pre>
     *
     * @param parent  The SWT composite to place controls into.
     */
    private void createSingleSelectWidget( final Composite parent )
    {
        // Dn combo
        Composite textAndUpComposite = BaseWidgetUtils.createColumnContainer( parent, 2, 1 );
        dnCombo = BaseWidgetUtils.createCombo( textAndUpComposite, new String[0], -1, 1 );
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 1;
        gd.widthHint = 30;
        dnCombo.setLayoutData( gd );

        // Dn history
        String[] history = HistoryUtils.load( BrowserCommonActivator.getDefault().getDialogSettings(),
            BrowserCommonConstants.DIALOGSETTING_KEY_DN_HISTORY );
        dnCombo.setItems( history );
        dnCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                try
                {
                    dn = new Dn( dnCombo.getText() );
                }
                catch ( LdapInvalidDnException e1 )
                {
                    dn = null;
                }

                internalSetEnabled();
                notifyListeners();
            }
        } );

        // Up button
        upButton = new Button( textAndUpComposite, SWT.PUSH );
        upButton.setToolTipText( Messages.getString( "EntryWidget.Parent" ) ); //$NON-NLS-1$
        upButton.setImage( BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_PARENT ) );
        upButton.setEnabled( false );
        upButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                if ( !Dn.isNullOrEmpty( dn ) )
                {
                    dn = dn.getParent();

                    dnChanged();
                    internalSetEnabled();
                    notifyListeners();
                }
            }
        } );

        // Browse button
        entryBrowseButton = BaseWidgetUtils.createButton( parent, Messages.getString( "EntryWidget.BrowseButton" ), 1 ); //$NON-NLS-1$
        entryBrowseButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                if ( browserConnection != null )
                {
                    // get root entry
                    IEntry rootEntry = browserConnection.getRootDSE();

                    if ( suffix != null && suffix.size() > 0 )
                    {
                        rootEntry = browserConnection.getEntryFromCache( suffix );

                        if ( rootEntry == null )
                        {
                            ReadEntryRunnable runnable = new ReadEntryRunnable( browserConnection, suffix );
                            RunnableContextRunner.execute( runnable, null, true );
                            rootEntry = runnable.getReadEntry();
                        }
                    }

                    // calculate initial Dn
                    Dn initialDn = dn;

                    if ( useLocalName && suffix != null && suffix.size() > 0 )
                    {
                        if ( initialDn != null && initialDn.size() > 0 )
                        {
                            try
                            {
                                initialDn = initialDn.add( suffix );
                            }
                            catch ( LdapInvalidDnException lide )
                            {
                                // Do nothing
                            }
                        }
                    }

                    // get initial entry
                    IEntry entry = rootEntry;

                    if ( initialDn != null && initialDn.size() > 0 )
                    {
                        entry = browserConnection.getEntryFromCache( initialDn );

                        if ( entry == null )
                        {
                            ReadEntryRunnable runnable = new ReadEntryRunnable( browserConnection, initialDn );
                            RunnableContextRunner.execute( runnable, null, true );
                            entry = runnable.getReadEntry();
                        }
                    }

                    // open dialog
                    SelectEntryDialog dialog = new SelectEntryDialog( parent.getShell(), Messages
                        .getString( "EntryWidget.SelectDN" ), rootEntry, entry ); //$NON-NLS-1$
                    dialog.open();
                    IEntry selectedEntry = dialog.getSelectedEntry();

                    // get selected Dn
                    if ( selectedEntry != null )
                    {
                        dn = selectedEntry.getDn();

                        if ( useLocalName && suffix != null && suffix.size() > 0 )
                        {
                            dn = DnUtils.getPrefixName( dn, suffix );
                        }

                        dnChanged();
                        internalSetEnabled();
                        notifyListeners();
                    }
                }
            }
        } );

        dnChanged();
        internalSetEnabled();
    }


    // ── R2 Builds the Multi-Target Hit-List Panel ─────────────────────────────────────
    // For the mission with multiple targets, R2 puts up a scrollable list and two
    // side buttons: "Add Target" opens the tree browser so the commander can pick more,
    // and "Remove" deletes whatever is highlighted in the list.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Internal helper that creates the multi-select layout: a scrollable DN list
     * and Add/Remove buttons arranged in a two-column composite.
     * Not intended to be called directly — use {@link #createWidget} instead.
     *
     * <p>For example — R2 builds his multi-target mission board:</p>
     * <pre>
     *   targetList = new ScrollableList();
     *   addBtn     = new Button( "Add Target" );
     *   removeBtn  = new Button( "Remove" );
     * </pre>
     *
     * @param parent  The SWT composite to place controls into.
     */
    private void createMultiSelectWidget( final Composite parent )
    {
        // DN list
        dnListControl = new org.eclipse.swt.widgets.List( parent,
            SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
        GridData listGd = new GridData( GridData.FILL_BOTH );
        listGd.heightHint = 100;
        listGd.widthHint = 200;
        dnListControl.setLayoutData( listGd );

        for ( Dn d : dns )
        {
            dnListControl.add( d.getName() );
        }

        dnListControl.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                internalSetEnabled();
            }
        } );

        // Buttons composite (stacked vertically in the second column)
        Composite buttonsComposite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );
        GridData bgd = new GridData( SWT.FILL, SWT.TOP, false, false );
        buttonsComposite.setLayoutData( bgd );

        addButton = BaseWidgetUtils.createButton( buttonsComposite,
            Messages.getString( "EntryWidget.AddButton" ), 1 ); //$NON-NLS-1$
        addButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                if ( browserConnection != null )
                {
                    IEntry rootEntry = browserConnection.getRootDSE();
                    SelectEntryDialog dialog = new SelectEntryDialog( parent.getShell(),
                        Messages.getString( "EntryWidget.SelectDN" ), rootEntry, null ); //$NON-NLS-1$
                    dialog.open();
                    for ( IEntry entry : dialog.getSelectedEntries() )
                    {
                        Dn entryDn = entry.getDn();
                        if ( !dns.contains( entryDn ) )
                        {
                            dns.add( entryDn );
                            dnListControl.add( entryDn.getName() );
                        }
                    }
                    internalSetEnabled();
                    notifyListeners();
                }
            }
        } );

        removeButton = BaseWidgetUtils.createButton( buttonsComposite,
            Messages.getString( "EntryWidget.RemoveButton" ), 1 ); //$NON-NLS-1$
        removeButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                int[] indices = dnListControl.getSelectionIndices();
                // remove in reverse order so earlier indices stay valid
                for ( int i = indices.length - 1; i >= 0; i-- )
                {
                    dns.remove( indices[i] );
                    dnListControl.remove( indices[i] );
                }
                internalSetEnabled();
                notifyListeners();
            }
        } );

        internalSetEnabled();
    }


    // ── R2 Refreshes the Terminal Display After Navigating ───────────────────────────
    // R2 just climbed to a new corridor — he updates the path display on the terminal
    // so the Rebels can see exactly where in the Death Star he's parked.
    // We push the current dn value back into the combo text field.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Synchronises the DN combo field's text with the current value of the {@code dn}
     * field. Called internally after navigating up or selecting from the browser dialog.
     * Only meaningful in single-select mode.
     *
     * <p>For example — R2 updates the terminal readout:</p>
     * <pre>
     *   terminalDisplay.setText( currentPath != null ? currentPath : "" );
     * </pre>
     */
    private void dnChanged()
    {
        if ( ( dnCombo != null ) && ( entryBrowseButton != null ) )
        {
            dnCombo.setText( dn != null ? dn.getName() : "" ); //$NON-NLS-1$
        }
    }


    // ── R2 Powers Up or Down His Navigation Controls ─────────────────────────────────
    // When the Death Star's power is cut to R2's sector, his buttons go dark.
    // When power is restored, they light back up and he can navigate again.
    // We propagate the public enabled flag and then let internalSetEnabled fine-tune it.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the entire widget. In single-select mode the combo and
     * Up/Browse buttons are toggled. In multi-select mode the list and Add/Remove
     * buttons are toggled. Pass {@code false} to grey everything out.
     *
     * <p>For example — R2's controls go dark when power is cut:</p>
     * <pre>
     *   pathField.setEnabled( false );
     *   upButton.setEnabled( false );
     *   browseButton.setEnabled( false );
     * </pre>
     *
     * @param enabled  {@code true} to enable; {@code false} to disable.
     */
    public void setEnabled( boolean enabled )
    {
        if ( multiSelect )
        {
            if ( dnListControl != null )
            {
                dnListControl.setEnabled( enabled );
            }
            internalSetEnabled();
        }
        else
        {
            dnCombo.setEnabled( enabled );

            if ( enabled )
            {
                this.dnChanged();
            }

            internalSetEnabled();
        }
    }


    // ── R2 Fine-Tunes Which Buttons Are Live ─────────────────────────────────────────
    // R2 checks: is there a connection? Is there a valid path? Is the list enabled?
    // Based on those conditions he selectively re-enables or dims the action buttons.
    // We separately control Up, Browse, Add, and Remove based on current widget state.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Internal method that adjusts individual button enabled states based on the
     * current runtime state: whether a connection exists, whether a DN is set,
     * and whether any items are selected in the list. Called after any state change.
     *
     * <p>For example — R2 checks each button's prerequisites:</p>
     * <pre>
     *   upButton.setEnabled( dn != null and combo is live );
     *   browseButton.setEnabled( connection != null );
     *   addButton.setEnabled( connection != null );
     *   removeButton.setEnabled( something is selected in list );
     * </pre>
     */
    private void internalSetEnabled()
    {
        if ( multiSelect )
        {
            if ( addButton != null )
            {
                addButton.setEnabled( browserConnection != null
                    && ( dnListControl == null || dnListControl.isEnabled() ) );
            }
            if ( removeButton != null )
            {
                removeButton.setEnabled( dnListControl != null
                    && dnListControl.isEnabled()
                    && dnListControl.getSelectionCount() > 0 );
            }
        }
        else
        {
            upButton.setEnabled( !Dn.isNullOrEmpty( dn ) && dnCombo.isEnabled() );
            entryBrowseButton.setEnabled( browserConnection != null && dnCombo.isEnabled() );
        }
    }


    // ── R2 Commits His Navigation History to Persistent Memory ───────────────────────
    // Before powering down after the mission, R2 saves the path he visited so it
    // shows up in the history drop-down next time the same terminal session opens.
    // We persist the current combo text into the dialog settings history store.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the currently typed DN to the dialog history so it appears in the combo's
     * drop-down next time the user opens this dialog. Only applies in single-select mode.
     * Call this when the user confirms the dialog (OK / Search).
     *
     * <p>For example — R2 logs Detention Block AA-23 to his mission journal:</p>
     * <pre>
     *   missionJournal.append( currentPath );
     * </pre>
     */
    public void saveDialogSettings()
    {
        if ( !multiSelect && dnCombo != null )
        {
            HistoryUtils.save( BrowserCommonActivator.getDefault().getDialogSettings(),
                BrowserCommonConstants.DIALOGSETTING_KEY_DN_HISTORY, this.dnCombo.getText() );
        }
    }


    // ── R2 Reports the Suffix Sector Root ────────────────────────────────────────────
    // Leia asks: "Which sector is this path relative to?" R2 checks his briefing notes
    // and reports back the sector root DN — or null if no relative root was given.
    // We simply return the stored suffix field.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the suffix DN — the subtree root that local-name mode is relative to —
     * or {@code null} if no suffix was configured. Callers use this to re-initialise
     * the widget after a connection change.
     *
     * <p>For example — R2 reports the sector root:</p>
     * <pre>
     *   sectorRoot = R2.getSuffixSector(); // "ou=PrisonSector,dc=deathstar"
     * </pre>
     *
     * @return  The suffix Dn, or {@code null} if not set.
     */
    public Dn getSuffix()
    {
        return suffix;
    }


    // ── R2 Reports the Current Target Location ───────────────────────────────────────
    // "Where are you pointing?" Luke shouts. R2 beeps the path of the current target —
    // or a sad descending tone if the path is invalid (bad DN syntax in the combo).
    // We return the parsed DN object, or null if parsing failed.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the currently selected DN in single-select mode, or {@code null} if the
     * text in the combo field is not a valid LDAP Distinguished Name. The search engine
     * uses this as the search base.
     *
     * <p>For example — R2 reports his target:</p>
     * <pre>
     *   target = R2.getCurrentTarget(); // "ou=DetentionBlock,dc=deathstar", or null
     * </pre>
     *
     * @return  The selected {@link Dn}, or {@code null} if the field is empty or invalid.
     */
    public Dn getDn()
    {
        return dn;
    }


    // ── R2 Dumps the Full Target List ────────────────────────────────────────────────
    // The mission commander asks for all targets at once. In single-target mode R2
    // wraps his one target in a one-element array; in multi-target mode he dumps the
    // whole list. Either way the answer is always an array, never null.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all selected DNs as an array. In single-select mode this is a one-element
     * array (or empty if no valid DN is set). In multi-select mode it returns all items
     * currently in the list. Never returns null.
     *
     * <p>For example — R2 dumps his complete target manifest:</p>
     * <pre>
     *   allTargets = R2.getAllTargets();
     *   // [ "ou=BlockAA23", "ou=BlockAA24" ]
     * </pre>
     *
     * @return  Array of selected {@link Dn} objects; empty array if none are selected.
     */
    public Dn[] getDns()
    {
        if ( multiSelect )
        {
            return dns.toArray( new Dn[0] );
        }
        else
        {
            return dn != null ? new Dn[]{ dn } : new Dn[0];
        }
    }


    // ── R2 Reports Which Terminal He's Plugged Into ───────────────────────────────────
    // "Which Death Star system are you connected to, R2?" He beeps back the terminal ID.
    // We return the stored IBrowserConnection reference.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IBrowserConnection} this widget is currently associated with.
     * The Browse button uses this to root the entry-selection dialog at the right server.
     *
     * <p>For example — R2 reports his terminal connection:</p>
     * <pre>
     *   terminal = R2.getTerminalConnection(); // deathStarMainframe
     * </pre>
     *
     * @return  The active browser connection, or {@code null} if none is set.
     */
    public IBrowserConnection getBrowserConnection()
    {
        return browserConnection;
    }


    // ── R2 Gets Redirected to a New Terminal With a New Target ───────────────────────
    // Mid-mission, Leia changes R2's orders: "Different terminal, different cell."
    // R2 unplugs from the old terminal, connects to the new one, and updates his target.
    // We update connection and DN (without suffix) and refresh the combo display.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the active connection and DN without changing suffix or local-name mode.
     * A convenience overload of {@link #setInput(IBrowserConnection, Dn, Dn, boolean)}.
     * Use this when the search base changes but the subtree root stays the same.
     *
     * <p>For example — R2 switches to a new terminal and a new target:</p>
     * <pre>
     *   R2.redirectTo( newTerminal, newCellPath );
     * </pre>
     *
     * @param dn                 The new DN to select.
     * @param browserConnection  The new connection to use.
     */
    public void setInput( IBrowserConnection browserConnection, Dn dn )
    {
        setInput( browserConnection, dn, null, false );
    }


    // ── R2 Gets Full New Mission Parameters ──────────────────────────────────────────
    // The Rebel command centre beams R2 a complete new mission brief: new terminal,
    // new target cell, new sector root, and whether to report paths in local or absolute.
    // We update all four fields and refresh the DN combo display if something changed.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates all four input parameters — connection, DN, suffix, and local-name flag —
     * and refreshes the combo display if any value changed. This is the full setter;
     * the two-arg overload delegates here with nulls for suffix and false for localName.
     *
     * <p>For example — R2 receives a complete new mission brief:</p>
     * <pre>
     *   R2.setMission( newTerminal, newCellPath, newSectorRoot, useLocalName=true );
     * </pre>
     *
     * @param browserConnection  The LDAP connection to browse against.
     * @param dn                 The entry path to display and use as the search base.
     * @param suffix             The subtree root for local-name mode. May be null.
     * @param useLocalName       Whether to strip the suffix from displayed/returned DNs.
     */
    public void setInput( IBrowserConnection browserConnection, Dn dn, Dn suffix, boolean useLocalName )
    {
        if ( ( this.browserConnection != browserConnection ) || ( this.dn != dn ) || ( this.suffix != suffix ) )
        {
            this.browserConnection = browserConnection;
            this.dn = dn;
            this.suffix = suffix;
            this.useLocalName = useLocalName;
            dnChanged();
        }
    }
}
