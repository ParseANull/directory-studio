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
package org.apache.directory.studio.combinededitor.editor;


import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidget;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidgetConfiguration;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidgetUniversalListener;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;


// ── CLASS: TableEditorPage — The Tantive IV's Tactical Attribute Display ──────
// The Tantive IV bridge has a dedicated tactical console that shows all ship
// systems in a structured table: row by row, system name on the left, current
// value on the right.  An officer can select any row and edit the value inline
// using the appropriate tool for that system type.
// TableEditorPage is that console: it shows every LDAP attribute of the current
// entry in a tree/table view (attribute name + value), lets the user add, edit,
// or delete attribute values inline, and propagates every change back to the
// shared working copy so the LDIF and Template tabs stay in sync.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The "Table Editor" tab page in the combined entry editor.
 * Renders the current LDAP entry as a structured attribute table — each row is
 * one attribute type / value pair.  The user can add, edit, or delete values
 * inline using the same entry editor widget used elsewhere in the browser.
 * Think of this as the Tantive IV's tactical console: structured, row-by-row
 * view of every system on the ship, with in-place editing.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TableEditorPage extends AbstractCombinedEntryEditorPage
{
    /** The entry editor widget */
    private EntryEditorWidget entryEditorWidget;

    /** The listener associated with the entry editor widget */
    private EntryEditorWidgetUniversalListener listener;


    // ── Tactical Console Reports for Duty on the Bridge ──────────────────────
    // The tactical officer takes her seat, labels her station in the tab rack,
    // and readies the console — but doesn't load any data yet (lazy init).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the Table Editor page and its tab item in the editor's tab folder.
     * We create the tab immediately so it appears in the tab strip, but we defer
     * creating the heavy entry editor widget until {@link #init()} is called when
     * the user first selects this tab.
     *
     * @param editor  the combined editor that owns this page.
     */
    public TableEditorPage( CombinedEntryEditor editor )
    {
        super( editor );

        // Creating and assigning the tab item
        CTabItem tabItem = new CTabItem( editor.getTabFolder(), SWT.NONE );
        tabItem.setText( Messages.getString( "TableEditorPage.TableEditor" ) ); //$NON-NLS-1$
        tabItem
            .setImage( BrowserUIPlugin.getDefault().getImage( BrowserUIConstants.IMG_BROWSER_SINGLETAB_ENTRYEDITOR ) );
        setTabItem( tabItem );
    }


    // ── Tactical Console Comes Online — Attribute Table Ready ────────────────
    // The officer powers up the tactical console: the entry editor widget is
    // created, the action group is wired up (toolbar, menu, context menu), the
    // current entry is loaded, and focus is handed to the viewer.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the entry editor widget and loads the current entry's attributes.
     * Called lazily the first time the user selects this tab.  We create the
     * widget with a custom {@link EditorConfiguration}, wire up the
     * {@link TableEditorPageActionGroup}, and populate the table from the shared
     * working copy.
     */
    public void init()
    {
        super.init();

        EditorConfiguration configuration = new EditorConfiguration();

        entryEditorWidget = new EntryEditorWidget( configuration );
        entryEditorWidget.createWidget( getEditor().getTabFolder() );

        TableEditorPageActionGroup entryEditorActionGroup = new TableEditorPageActionGroup( getEditor(),
            entryEditorWidget, configuration );

        entryEditorActionGroup.fillToolBar( entryEditorWidget.getToolBarManager() );
        entryEditorActionGroup.fillMenu( entryEditorWidget.getMenuManager() );
        entryEditorActionGroup.fillContextMenu( entryEditorWidget.getContextMenuManager() );

        setInput();

        getEditor().getSite().setSelectionProvider( entryEditorWidget.getViewer() );
        listener = new EntryEditorWidgetUniversalListener( entryEditorWidget.getViewer(), configuration,
            entryEditorActionGroup, entryEditorActionGroup.getOpenDefaultEditorAction() );

        entryEditorActionGroup.setInput( getEditor().getEntryEditorInput().getSharedWorkingCopy( getEditor() ) );

        getEditor().getSite().setSelectionProvider( entryEditorWidget.getViewer() );

        getTabItem().setControl( entryEditorWidget.getControl() );
    }


    // ── Load the Current Entry into the Attribute Table ──────────────────────
    // The tactical officer dials up the current entry from the shared working
    // copy and feeds it into the table viewer so all attributes are displayed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the table viewer's input to the current shared working copy entry.
     * If the widget doesn't exist yet (page not initialised) this is a no-op.
     */
    private void setInput()
    {
        if ( entryEditorWidget != null )
        {
            entryEditorWidget.getViewer().setInput(
                getEditor().getEntryEditorInput().getSharedWorkingCopy( getEditor() ) );
        }
    }


    // ── Status Update — Refresh the Attribute Rows ───────────────────────────
    // When a sister station changes an attribute (via the LDIF or Template tabs)
    // the tactical console refreshes its rows so the officer always sees the
    // current state of the ship's systems.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the table viewer to show the latest attribute values.
     * Called when another editor page modifies the shared working copy.
     * We just call {@link TreeViewer#refresh()} — the viewer re-reads from the
     * model (the working copy entry) and repaints all rows.
     */
    public void update()
    {
        if ( entryEditorWidget != null )
        {
            entryEditorWidget.getViewer().refresh();
        }
    }


    // ── Officer Takes the Helm — Keyboard Focus to Table ─────────────────────
    // When the user clicks the Table tab, Antilles points at the tactical console
    // and says "You're on."  The console takes keyboard focus so the user can
    // start navigating attribute rows immediately.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Moves keyboard focus to the entry editor widget.
     * The user can navigate and edit attribute rows without an extra click.
     */
    public void setFocus()
    {
        if ( entryEditorWidget != null )
        {
            entryEditorWidget.setFocus();
        }
    }


    // ── Console Powers Down — Release All Resources ──────────────────────────
    // When the editor tab is closed the tactical console shuts down cleanly:
    // the entry editor widget and its universal listener are both disposed
    // so we don't leak SWT resources or selection listeners.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes the entry editor widget and the universal listener.
     * Called by {@link CombinedEntryEditor#dispose()} when the editor closes.
     */
    public void dispose()
    {
        if ( entryEditorWidget != null )
        {
            entryEditorWidget.dispose();
        }

        if ( listener != null )
        {
            listener.dispose();
        }
    }


    // ── New Mission Coordinates — Reload the Attribute Table ─────────────────
    // When the editor switches to a different entry the tactical officer needs
    // to reload the attribute table for the new target — but only if the console
    // is already online.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the editor's input switches to a different LDAP entry.
     * If this page has already been initialised we reload the table with the
     * new entry's attributes; if not we wait for the user to select this tab.
     */
    public void editorInputChanged()
    {
        if ( isInitialized() )
        {
            setInput();
        }
    }


    // ── Custom Configuration — Value Editors for the Tactical Console ─────────
    // The tactical console needs a special value editor manager that can inline-edit
    // any attribute value type (text, binary, DN, certificate, etc.).  We override
    // the default configuration to supply a manager tuned for table-style editing.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Custom entry editor widget configuration for the Table Editor page.
     * We override {@link #getValueEditorManager} to create a
     * {@link ValueEditorManager} that enables in-table editing (the first
     * argument is the tree widget; {@code true} enables the entry value editor).
     */
    class EditorConfiguration extends EntryEditorWidgetConfiguration
    {
        // ── Pick the Right Tool for Each System Type ──────────────────────────
        // The tactical officer has a toolkit for every ship system type — the
        // right tool is automatically selected based on the attribute's syntax.
        // ────────────────────────────────────────────────────────────────────────
        /**
         * Returns (creating if necessary) the value editor manager for this table.
         * We create it once and cache it in the parent class field.  The manager
         * picks the correct inline value editor for each attribute based on its
         * syntax (string, binary, DN, etc.).
         *
         * @param viewer  the tree viewer this configuration belongs to.
         * @return        the value editor manager.
         */
        public ValueEditorManager getValueEditorManager( TreeViewer viewer )
        {
            if ( valueEditorManager == null )
            {
                valueEditorManager = new ValueEditorManager( viewer.getTree(), true, false );
            }

            return valueEditorManager;
        }
    }
}
