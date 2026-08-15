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

package org.apache.directory.studio.ldapbrowser.common.dialogs;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserActionGroup;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserConfiguration;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserUniversalListener;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: SelectEntryDialog — THE HOLOGRAPHIC STAR MAP PROJECTING KAMINO ────
// In the Jedi Archives, Obi-Wan is searching for a planet whose coordinates have
// been deleted from the database.  He consults the holographic star map — a
// navigable 3-D tree of the galaxy — and must locate Kamino by browsing the
// surrounding star systems.  He can pick any entry on the map; the one he
// highlights is the one the mission will target.
// The LDAP directory is also a tree: each entry has a position (its DN) and
// children.  This dialog shows a navigable browser tree rooted at a given entry
// and lets the user pick one (or more) entries from it — exactly like Obi-Wan
// navigating the star map to pinpoint a destination.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A dialog that displays a navigable LDAP directory tree (rooted at a given
 * entry) and lets the user select one or more entries.  It embeds the same
 * {@link BrowserWidget} used in the main LDAP Browser view so the navigation
 * experience is identical.  Both single ({@link #getSelectedEntry()}) and multi
 * ({@link #getSelectedEntries()}) selection are supported — the latter via
 * Ctrl/Shift-click in the tree.
 * Think of this class as Obi-Wan consulting the holographic star map: browse,
 * find what you need, and confirm your destination.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SelectEntryDialog extends Dialog
{

    /** The dialog title. */
    private String title;

    /** The root entry. */
    private IEntry rootEntry;

    /** The initial entry. */
    private IEntry initialEntry;

    /** The selected entry. */
    private IEntry selectedEntry;

    /** All selected entries (supports multi-selection). */
    private List<IEntry> selectedEntries;

    /** The browser configuration. */
    private BrowserConfiguration browserConfiguration;

    /** The browser universal listener. */
    private BrowserUniversalListener browserUniversalListener;

    /** The browser action group. */
    private BrowserActionGroup browserActionGroup;

    /** The browser widget. */
    private BrowserWidget browserWidget;


    // ── OBI-WAN APPROACHES THE STAR MAP ──────────────────────────────────────
    // Obi-Wan walks up to the holographic map stand with a starting location in
    // mind — the Outer Rim — and a hint about which star system he is looking
    // for.  The map projects from the root downward; whatever entry he is
    // pointed to is highlighted as the initial selection.
    // We capture the root, initial, and clear the selection fields — they will be
    // filled in once the user navigates the tree.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SelectEntryDialog.  The tree is rooted at {@code rootEntry}
     * and the {@code initialEntry} is pre-selected and revealed so the user
     * starts near the area of interest.  Both may be {@code null} — passing a
     * null initial entry just means nothing is pre-selected.
     *
     * <p>For example — Obi-Wan approaches the star map:</p>
     * <pre>
     *   SelectEntryDialog dialog = new SelectEntryDialog(
     *       shell, "Select Target Entry", rootEntry, initialEntry);
     *   if (dialog.open() == OK) {
     *       IEntry chosen = dialog.getSelectedEntry();
     *   }
     * </pre>
     *
     * @param parentShell  the shell that owns this dialog
     * @param title        dialog title shown in the title bar
     * @param rootEntry    the entry to use as the root of the browser tree
     * @param initialEntry the entry to pre-select and reveal; may be {@code null}
     */
    public SelectEntryDialog( Shell parentShell, String title, IEntry rootEntry, IEntry initialEntry )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.title = title;
        this.rootEntry = rootEntry;
        this.initialEntry = initialEntry;
        this.selectedEntry = null;
        this.selectedEntries = new ArrayList<>();
    }


    // ── THE ARCHIVIST LABELS THE MAP CONSOLE ─────────────────────────────────
    // The Jedi Archive archivist stamps the console title — "Select Target Entry
    // — Jedi Archives Star Map."  Without the label Obi-Wan would not know which
    // console he is at.
    // We apply the window title from the constructor parameter.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog window title before the shell is shown.
     *
     * <p>For example — the archivist labels the console:</p>
     * <pre>
     *   shell.setText("Select Target Entry");
     * </pre>
     *
     * @param shell  the shell Eclipse hands us to configure
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( title );
    }


    // ── THE MAP POWERS DOWN AFTER OBI-WAN LEAVES ─────────────────────────────
    // After Obi-Wan steps away the holographic map folds back into the pedestal:
    // all subsystems power off in the right order so nothing is left running.
    // We dispose the browser widget stack (configuration, actions, listener) to
    // release all SWT and event resources.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up the {@link BrowserWidget} and related infrastructure when the
     * dialog closes.  We dispose in reverse creation order: deactivate action
     * handlers, dispose action group, universal listener, and finally the widget.
     *
     * <p>For example — the map powers down after the session:</p>
     * <pre>
     *   browserActionGroup.deactivateGlobalActionHandlers();
     *   browserActionGroup.dispose();
     *   browserUniversalListener.dispose();
     *   browserWidget.dispose();
     * </pre>
     *
     * @return  {@code true} if the dialog closed successfully
     */
    public boolean close()
    {
        if ( browserWidget != null )
        {
            browserConfiguration.dispose();
            browserConfiguration = null;
            browserActionGroup.deactivateGlobalActionHandlers();
            browserActionGroup.dispose();
            browserActionGroup = null;
            browserUniversalListener.dispose();
            browserUniversalListener = null;
            browserWidget.dispose();
            browserWidget = null;
        }
        return super.close();
    }


    // ── OBI-WAN LOCKS IN HIS DESTINATION ─────────────────────────────────────
    // Obi-Wan points to Kamino on the star map and confirms: "That is where I
    // need to go."  The archivist logs the coordinates.
    // On OK we snapshot the current tree selection into {@code selectedEntries}
    // and set {@code selectedEntry} to the first one (or the initial entry if
    // nothing was selected).
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user presses OK.  We read the current selection from the
     * browser viewer — handling both {@link IEntry} and {@link ISearchResult}
     * objects — and store the results.  If nothing is selected we fall back to
     * the last highlighted {@code initialEntry} so the caller never gets null
     * unexpectedly.
     *
     * <p>For example — Obi-Wan confirms Kamino's coordinates:</p>
     * <pre>
     *   for (Object o : viewer.getSelection().toList()) {
     *       selectedEntries.add(toEntry(o));
     *   }
     *   selectedEntry = selectedEntries.isEmpty() ? initialEntry : selectedEntries.get(0);
     * </pre>
     */
    protected void okPressed()
    {
        selectedEntries = new ArrayList<>();
        if ( browserWidget != null )
        {
            IStructuredSelection sel = ( IStructuredSelection ) browserWidget.getViewer().getSelection();
            for ( Object o : sel.toList() )
            {
                if ( o instanceof IEntry )
                {
                    selectedEntries.add( ( IEntry ) o );
                }
                else if ( o instanceof ISearchResult )
                {
                    selectedEntries.add( ( ( ISearchResult ) o ).getEntry() );
                }
            }
        }
        selectedEntry = selectedEntries.isEmpty() ? initialEntry : selectedEntries.get( 0 );
        super.okPressed();
    }


    // ── OBI-WAN WALKS AWAY WITHOUT A DESTINATION ─────────────────────────────
    // Obi-Wan steps back from the console and says "Never mind — I'll look
    // elsewhere."  No coordinates are logged; selectedEntry is null.
    // On Cancel we explicitly null the selection so callers reliably know no
    // entry was chosen.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user cancels.  We set {@code selectedEntry} to {@code null}
     * so callers reliably see no selection was made.
     *
     * <p>For example — Obi-Wan walks away without choosing:</p>
     * <pre>
     *   selectedEntry = null;
     *   super.cancelPressed();
     * </pre>
     */
    protected void cancelPressed()
    {
        selectedEntry = null;
        super.cancelPressed();
    }


    // ── THE ARCHIVIST PREPARES CONFIRM AND ABORT BUTTONS ─────────────────────
    // Two buttons on the console: "Lock Coordinates" (OK) and "Cancel Mission"
    // (Cancel).  Neither is the default — Obi-Wan must click deliberately.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates OK and Cancel buttons, neither flagged as the dialog default.
     * This prevents accidental Enter-key confirmation before the user has
     * actually navigated to the right entry.
     *
     * <p>For example — the archivist readies both console buttons:</p>
     * <pre>
     *   createButton(OK,     defaultButton=false);
     *   createButton(CANCEL, defaultButton=false);
     * </pre>
     *
     * @param parent  the button-bar composite Eclipse provides
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    // ── THE HOLOGRAPHIC MAP PROJECTS THE GALAXY ───────────────────────────────
    // The pedestal hums to life and projects the galaxy in glowing blue light —
    // every star system visible and navigable.  A selection-changed listener
    // tracks which system Obi-Wan is pointing at, so the archivist always has
    // the current candidate coordinates ready.  The initial system is revealed
    // and highlighted before Obi-Wan starts browsing.
    // We build the full BrowserWidget stack, wire selection listeners, expand
    // the tree two levels, and reveal + select the initial entry.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog content area: a full {@link BrowserWidget} with toolbar,
     * context menu, and action group, plus a selection-changed listener that
     * keeps the list of selected entries current as the user navigates.  The
     * tree is expanded two levels from the root and the initial entry is revealed
     * and selected.
     *
     * <p>For example — the holographic map projects the galaxy:</p>
     * <pre>
     *   browserWidget.setInput(new IEntry[]{ rootEntry });
     *   viewer.expandToLevel(2);
     *   viewer.reveal(initialEntry);
     *   viewer.setSelection(new StructuredSelection(initialEntry), true);
     * </pre>
     *
     * @param parent  the parent composite Eclipse provides
     * @return        the completed content area control
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        gd.heightHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        composite.setLayoutData( gd );

        // create configuration
        browserConfiguration = new BrowserConfiguration();

        // create main widget
        browserWidget = new BrowserWidget( browserConfiguration, null );
        browserWidget.createWidget( composite );
        browserWidget.setInput( new IEntry[]
            { rootEntry } );

        // create actions and context menu (and register global actions)
        browserActionGroup = new BrowserActionGroup( browserWidget, browserConfiguration );
        browserActionGroup.fillToolBar( browserWidget.getToolBarManager() );
        browserActionGroup.fillMenu( browserWidget.getMenuManager() );
        browserActionGroup.fillContextMenu( browserWidget.getContextMenuManager() );
        browserActionGroup.activateGlobalActionHandlers();

        // create the listener
        browserUniversalListener = new BrowserUniversalListener( browserWidget );

        browserWidget.getViewer().addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                if ( !event.getSelection().isEmpty() )
                {
                    IStructuredSelection sel = ( IStructuredSelection ) event.getSelection();
                    selectedEntries = new ArrayList<>();
                    for ( Object o : sel.toList() )
                    {
                        if ( o instanceof IEntry )
                        {
                            selectedEntries.add( ( IEntry ) o );
                        }
                        else if ( o instanceof ISearchResult )
                        {
                            selectedEntries.add( ( ( ISearchResult ) o ).getEntry() );
                        }
                    }
                    if ( !selectedEntries.isEmpty() )
                    {
                        initialEntry = selectedEntries.get( 0 );
                    }
                }
            }
        } );

        browserWidget.getViewer().expandToLevel( 2 );
        if ( initialEntry != null )
        {
            IEntry entry = this.initialEntry;
            browserWidget.getViewer().reveal( entry );
            browserWidget.getViewer().refresh( entry, true );
            browserWidget.getViewer().setSelection( new StructuredSelection( entry ), true );
            browserWidget.getViewer().setSelection( new StructuredSelection( entry ), true );
        }

        applyDialogFont( composite );

        browserWidget.setFocus();

        return composite;
    }


    // ── THE ARCHIVIST HANDS OBI-WAN THE COORDINATES ──────────────────────────
    // After Obi-Wan confirms, the archivist reads back the single highlighted
    // coordinate: "Kamino — twelve parsecs south of the Rishi Maze."
    // Callers call this to get the primary selection after OK.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the primary selected entry — the first one in the selection, or
     * the initial entry if the user confirmed without navigating.  Returns
     * {@code null} if the dialog was cancelled.
     *
     * <p>For example — the archivist hands over Kamino's coordinates:</p>
     * <pre>
     *   IEntry target = dialog.getSelectedEntry();
     *   // "ou=Kamino,ou=OuterRim,dc=galaxy,dc=org"
     * </pre>
     *
     * @return  the selected entry, or {@code null} if cancelled
     */
    public IEntry getSelectedEntry()
    {
        return selectedEntry;
    }


    // ── THE ARCHIVIST LISTS ALL HIGHLIGHTED SYSTEMS ───────────────────────────
    // For a multi-destination mission Obi-Wan Ctrl-clicked several systems.
    // The archivist reads out all highlighted coordinates.
    // Callers use this for multi-selection scenarios — e.g. moving several
    // entries to the same parent.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns all selected entries.  In single-selection scenarios this is a
     * one-element list containing the same entry as {@link #getSelectedEntry()}.
     * In multi-selection scenarios (Ctrl/Shift-click) this contains all clicked
     * entries.  Never returns {@code null} — an empty list means nothing was
     * selected.
     *
     * <p>For example — the archivist lists all highlighted destinations:</p>
     * <pre>
     *   List&lt;IEntry&gt; targets = dialog.getSelectedEntries();
     *   // [Kamino, Geonosis, Utapau]
     * </pre>
     *
     * @return  list of all selected {@link IEntry} objects; never {@code null}
     */
    public List<IEntry> getSelectedEntries()
    {
        return selectedEntries;
    }

}
