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


import org.apache.directory.studio.common.ui.widgets.ViewFormWidget;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;


// ── CLASS: EntryEditorWidget — Han at the Falcon's Nav Console ────────────────
// In The Empire Strikes Back, Han Solo works at the Millennium Falcon's
// navigation console: he pulls up nav database entries one by one, reads their
// coordinates, edits jump vectors, adds new destinations, and deletes stale
// routes. The whole interface — the scrollable table of entries, the column
// headers, the quick-search panel, the sort controls — is right in front of him.
// That's this class: the complete, reusable entry-editor widget that any view
// or dialog in Directory Studio can embed to let users browse and edit the
// attributes of an LDAP entry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Reusable widget that displays and edits the attributes of a single LDAP entry
 * in a two-column tree table (attribute name on the left, value on the right).
 * We assemble the full editing experience here: an instant-search filter bar,
 * a sortable and filterable {@link TreeViewer}, column-width auto-sizing, cell
 * editors for in-place value editing, and a hook into the plugin's sorter,
 * filter, and preference machinery.
 *
 * <p>This widget is used by the main entry editor view, the multi-valued dialog,
 * the LDIF entry editor dialog, and the new-entry attributes wizard page — anywhere
 * we need to show and edit an entry's attributes.</p>
 *
 * <p>Think of this class as Han Solo's nav console: the whole editing cockpit
 * laid out and ready. Plug in an LDAP entry, and the user can read and modify
 * its attributes just as Han reads and modifies nav coordinates.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorWidget extends ViewFormWidget
{

    /** The configuration. */
    private EntryEditorWidgetConfiguration configuration;

    /** The quick filter widget. */
    private EntryEditorWidgetQuickFilterWidget quickFilterWidget;

    /** The tree. */
    private Tree tree;

    /** The viewer. */
    private TreeViewer viewer;


    // ── Han Takes His Seat at the Nav Console ───────────────────────────────────
    // Han drops into the pilot's chair and notes which nav-computer configuration
    // he's working with — content providers, sorters, filters, value editors. He
    // doesn't wire anything up yet; that happens when createContent() is called.
    // We just store the configuration for later use.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new entry editor widget using the given configuration. We store
     * the configuration here and use it later in {@link #createContent} to wire
     * up providers, sorters, filters, and value editors. The SWT widgets are not
     * created until the parent calls {@code createContent()}.
     *
     * <p>For example — Han checks which nav-computer build he's sitting at:</p>
     * <pre>
     *   this.configuration = configuration
     *   // Nav console ready to be wired up on first use
     * </pre>
     *
     * @param configuration  Provides all the pluggable pieces — content provider,
     *                       label provider, sorter, filter, value editors, and
     *                       preferences — that define how this widget behaves.
     */
    public EntryEditorWidget( EntryEditorWidgetConfiguration configuration )
    {
        this.configuration = configuration;
    }


    // ── Han Wires Up Every Panel on the Nav Console ──────────────────────────────
    // Han flips open the access panels one by one: he installs the quick-search
    // display at the top, then builds the main nav-entry table with its columns,
    // hooks up the auto-resizing control, and finally connects the sorter, filter,
    // and value-editor machinery. When he's done, the console is fully operational.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds all the SWT widgets that make up the entry editor and wires them
     * together. We create them in order:
     * <ol>
     *   <li>The quick-filter bar at the top of the composite.</li>
     *   <li>The SWT {@link Tree} widget (multi-select, virtual, full-row
     *       selection, scrollable) that holds the attribute rows.</li>
     *   <li>Two {@link TreeColumn}s — "Key" and "Value" — both resizable.</li>
     *   <li>A {@link ControlAdapter} that keeps the value column filling the
     *       remaining horizontal space whenever the widget is resized.</li>
     *   <li>The {@link ValueEditorManager} and all JFace providers (content,
     *       label, cell modifier, cell editors).</li>
     * </ol>
     *
     * <p>For example — Han commissions the nav console system by system:</p>
     * <pre>
     *   quickFilterWidget.createComposite(parent)    → install search bar
     *   new Tree(parent, SWT.VIRTUAL | SWT.MULTI...) → build the entry table
     *   new TreeColumn("Key"), new TreeColumn("Value") → two-column layout
     *   tree.addControlListener(...)                 → auto-size value column
     *   configuration.getSorter().connect(viewer)    → enable sorting
     *   viewer.setContentProvider(...)               → plug in data source
     * </pre>
     *
     * @param parent  The SWT composite into which we add our widgets; must not be
     *                {@code null} and must have a suitable layout already set.
     * @return        The root SWT {@link Control} of this widget's content area,
     *                which is the {@link Tree} itself.
     */
    protected Control createContent( Composite parent )
    {
        // Create the filter widget
        quickFilterWidget = new EntryEditorWidgetQuickFilterWidget( configuration.getFilter(), this );
        quickFilterWidget.createComposite( parent );

        // create tree widget and viewer
        tree = new Tree( parent, SWT.VIRTUAL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION
            | SWT.HIDE_SELECTION );
        tree.setData( "org.eclipse.e4.ui.css.CssClassName", "studio-entry-tree" );
        GridData data = new GridData( GridData.FILL_BOTH );
        data.widthHint = 450;
        data.heightHint = 250;
        tree.setLayoutData( data );
        tree.setHeaderVisible( true );
        tree.setLinesVisible( true );
        viewer = new TreeViewer( tree );
        viewer.setUseHashlookup( true );

        // set tree columns. We have 2 : one for the KEY, one for the Value
        for ( int i = 0; i < EntryEditorWidgetTableMetadata.COLUM_NAMES.length; i++ )
        {
            TreeColumn column = new TreeColumn( tree, SWT.LEFT, i );
            column.setText( EntryEditorWidgetTableMetadata.COLUM_NAMES[i] );
            column.setWidth( 200 );
            column.setResizable( true );

        }

        viewer.setColumnProperties( EntryEditorWidgetTableMetadata.COLUM_NAMES );

        tree.addControlListener( new ControlAdapter()
        {
            public void controlResized( ControlEvent e )
            {
                if ( tree.getClientArea().width > 0 )
                {
                    int width = tree.getClientArea().width - 2 * tree.getBorderWidth();

                    if ( tree.getVerticalBar().isVisible() )
                    {
                        width -= tree.getVerticalBar().getSize().x;
                    }

                    tree.getColumn( EntryEditorWidgetTableMetadata.VALUE_COLUMN_INDEX ).setWidth(
                        width - tree.getColumn( EntryEditorWidgetTableMetadata.KEY_COLUMN_INDEX ).getWidth() );
                }
            }
        } );

        // setup sorter, filter and layout
        configuration.getSorter().connect( viewer );
        configuration.getFilter().connect( viewer );
        configuration.getPreferences().connect( viewer );

        // Get the ValueEditorManager
        ValueEditorManager valueEditorManager = configuration.getValueEditorManager( viewer );

        // setup providers
        viewer.setContentProvider( configuration.getContentProvider( this ) );
        viewer.setLabelProvider( configuration.getLabelProvider( valueEditorManager, viewer ) );

        // set table cell editors
        viewer.setCellModifier( configuration.getCellModifier( valueEditorManager ) );
        CellEditor[] editors = new CellEditor[EntryEditorWidgetTableMetadata.COLUM_NAMES.length];
        viewer.setCellEditors( editors );

        return tree;
    }


    // ── Han Leans In and Takes the Controls ─────────────────────────────────────
    // Han slides forward in the pilot's chair, grabs the controls, and the rest of
    // the crew steps back — he has focus. Keyboard events now flow to the nav table
    // so the user can immediately start navigating with arrow keys.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Transfers keyboard focus to the tree viewer's SWT tree widget. Call this
     * when the entry editor panel becomes active so the user can immediately
     * navigate the attribute rows with the keyboard without having to click first.
     *
     * <p>For example — Han grabs the controls as the panel activates:</p>
     * <pre>
     *   viewer.getTree().setFocus()
     *   → keyboard input now goes to the attribute table
     * </pre>
     */
    public void setFocus()
    {
        viewer.getTree().setFocus();
    }


    // ── Han Powers Down the Nav Console ─────────────────────────────────────────
    // The Falcon lands and Han shuts everything down in reverse order: first the
    // quick-search display, then the nav table, then the main configuration stack.
    // We null every reference as we go so the garbage collector can reclaim it all.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Tears down all SWT widgets and releases every object reference held by this
     * widget. We dispose in dependency order: configuration first (it may hold
     * references to the viewer), then the quick-filter widget, then the tree and
     * viewer. Call this when the owning view or dialog is closing — failing to do
     * so leaks SWT handles and JFace resources.
     *
     * <p>For example — Han shuts down the nav console on landing:</p>
     * <pre>
     *   configuration.dispose()      → release providers and value editors
     *   quickFilterWidget.dispose()  → remove the filter bar
     *   tree.dispose()               → free the SWT tree handle
     *   viewer = null                → drop the JFace reference
     *   super.dispose()              → ViewFormWidget cleanup
     * </pre>
     */
    public void dispose()
    {
        if ( viewer != null )
        {
            configuration.dispose();
            configuration = null;

            if ( quickFilterWidget != null )
            {
                quickFilterWidget.dispose();
                quickFilterWidget = null;
            }

            tree.dispose();
            tree = null;
            viewer = null;
        }

        super.dispose();
    }


    // ── Han Points to the Nav Display ───────────────────────────────────────────
    // "There's your readout," Han says, pointing at the main holographic display.
    // Callers — the universal listener, the action group — need a reference to the
    // JFace TreeViewer to hook up event listeners and set input.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the JFace {@link TreeViewer} that backs the attribute table. Other
     * components — like {@link EntryEditorWidgetUniversalListener} and
     * {@link EntryEditorWidgetActionGroup} — need this reference to register
     * listeners and update the viewer's input when the selected entry changes.
     *
     * <p>For example — Han points at the nav readout screen:</p>
     * <pre>
     *   entryEditorWidget.getViewer()
     *   → the TreeViewer showing attribute rows
     * </pre>
     *
     * @return  The {@link TreeViewer} for this widget; {@code null} after
     *          {@link #dispose()} has been called.
     */
    public TreeViewer getViewer()
    {
        return viewer;
    }


    // ── Han Points to the Quick-Search Panel ────────────────────────────────────
    // "That little screen up top," Han says, gesturing to the compact search
    // display on the console. The quick-filter widget is a separate sub-component;
    // we expose it so the ShowQuickFilterAction can show or hide it on command.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the quick-filter widget that sits above the attribute tree. The
     * {@link ShowQuickFilterAction} uses this reference to show or hide the filter
     * bar when the user clicks the filter toggle button.
     *
     * <p>For example — Han points out the search display:</p>
     * <pre>
     *   entryEditorWidget.getQuickFilterWidget()
     *   → the EntryEditorWidgetQuickFilterWidget above the tree
     * </pre>
     *
     * @return  The quick-filter widget; {@code null} after {@link #dispose()}.
     */
    public EntryEditorWidgetQuickFilterWidget getQuickFilterWidget()
    {
        return quickFilterWidget;
    }


    // ── Han Toggles the Nav Console's Master Power Switch ───────────────────────
    // Han reaches up and flips the master power toggle: console on, console off.
    // When disabled, the tree becomes read-only and non-interactive — useful when
    // the editor is showing data that shouldn't be edited right now.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the underlying SWT tree widget. When disabled, the tree
     * is grayed out and the user can't interact with it — no clicks, no keyboard
     * navigation, no cell editors. We use this when the entry editor is in a
     * read-only context (e.g., showing a referral entry that can't be modified).
     *
     * <p>For example — Han switches the console on or off:</p>
     * <pre>
     *   setEnabled(true)  → tree responds to user input
     *   setEnabled(false) → tree is grayed and unresponsive
     * </pre>
     *
     * @param enabled  {@code true} to make the tree interactive; {@code false} to
     *                 gray it out and block all user input.
     */
    public void setEnabled( boolean enabled )
    {
        tree.setEnabled( enabled );
    }

}
