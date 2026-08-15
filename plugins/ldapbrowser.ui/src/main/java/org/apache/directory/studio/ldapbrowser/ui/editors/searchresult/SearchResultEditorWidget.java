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

package org.apache.directory.studio.ldapbrowser.ui.editors.searchresult;


import org.apache.directory.studio.common.ui.widgets.ViewFormWidget;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;


// ── CLASS: SearchResultEditorWidget — The Imperial Census Terminal Screen ─────
// The data terminal in the Imperial archive room has a screen divided into two
// parts: a filter bar at the top (where the operator can narrow the displayed
// records) and the main record grid below (the virtual table of LDAP entries).
// The filter bar can slide open or retract; the table always occupies the rest
// of the space.  This widget is that terminal screen — it owns the SWT Table,
// the TableViewer, and the quick-filter composite.  The configuration object
// supplies the content/label/cell-modifier providers.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The main SWT widget for the search result editor.
 * Extends {@link ViewFormWidget} (which provides the toolbar and menu via its
 * ViewForm container) and creates two children inside its content area:
 * <ol>
 *   <li>A {@link SearchResultEditorQuickFilterWidget} — collapsible filter bar</li>
 *   <li>A virtual {@link Table} / {@link TableViewer} — the search result grid</li>
 * </ol>
 * The content/label/cell-modifier providers come from the
 * {@link SearchResultEditorConfiguration} passed at construction time.
 * Think of this as the terminal screen in the Imperial archive: filter bar up top,
 * records grid below.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultEditorWidget extends ViewFormWidget
{

    /** The configuration. */
    private SearchResultEditorConfiguration configuration;

    /** The quick filter widget. */
    private SearchResultEditorQuickFilterWidget quickFilterWidget;

    /** The table. */
    private Table table;

    /** The viewer. */
    private TableViewer viewer;


    // ── Terminal Receives Its Configuration ───────────────────────────────────
    // The configuration is the lazy factory for all providers.  We store a
    // reference so createContent() can wire them up when Eclipse calls it.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new widget with the given configuration.
     * The SWT controls are not created until {@link #createWidget(Composite)} is called.
     *
     * @param configuration the configuration that supplies all JFace providers
     */
    public SearchResultEditorWidget( SearchResultEditorConfiguration configuration )
    {
        this.configuration = configuration;
    }


    // ── Terminal Builds the Screen ────────────────────────────────────────────
    // Called by the ViewFormWidget base class to populate the content area.
    // We create the quick-filter widget first (so it sits at the top), then
    // the virtual SWT Table and its TableViewer.  We wire up the providers
    // from the configuration and return the table as the "main" control.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the quick-filter widget and the virtual table viewer inside the
     * given parent composite.
     * The table uses {@link SWT#VIRTUAL} so rows are fetched lazily via the
     * {@link SearchResultEditorContentProvider}.
     * Returns the table control so the base class can set focus correctly.
     *
     * @param parent the parent composite provided by the ViewFormWidget base class
     * @return the primary control (the SWT table) for focus routing
     */
    protected Control createContent( Composite parent )
    {
        // create quick filter
        quickFilterWidget = new SearchResultEditorQuickFilterWidget( configuration.getFilter() );
        quickFilterWidget.createComposite( parent );

        // create table widget and viewer
        table = new Table( parent, SWT.BORDER | SWT.HIDE_SELECTION | SWT.VIRTUAL );
        table.setData( "org.eclipse.e4.ui.css.CssClassName", "studio-search-table" );
        table.setHeaderVisible( true );
        table.setLinesVisible( true );
        table.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        viewer = new TableViewer( table );
        viewer.setUseHashlookup( true );

        // setup providers
        viewer.setContentProvider( configuration.getContentProvider( this ) );
        viewer.setLabelProvider( configuration.getLabelProvider( viewer ) );

        // set table cell editors
        viewer.setCellModifier( configuration.getCellModifier( viewer ) );

        return table;
    }


    // ── Terminal Loads New Records ────────────────────────────────────────────
    // Forwards a new input (typically an ISearch) to the underlying viewer.
    // The content provider will react and re-populate the table.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the viewer input, causing the content provider to repopulate the table.
     *
     * @param input the new input, typically an {@link org.apache.directory.studio.ldapbrowser.core.model.ISearch}
     */
    public void setInput( Object input )
    {
        viewer.setInput( input );
    }


    // ── Terminal Moves Focus to the Record Grid ───────────────────────────────
    // Delegates to the cursor (the TableCursor widget sitting on top of the table)
    // which is the actual focus target for keyboard navigation.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Moves keyboard focus to the table cursor.
     * The cursor is obtained from the configuration singleton.
     */
    public void setFocus()
    {
        configuration.getCursor( viewer ).setFocus();
    }


    // ── Terminal Shuts Down ────────────────────────────────────────────────────
    // Release the quick-filter widget and null the table and viewer references.
    // The configuration is disposed by the editor — we don't own it.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes the quick-filter widget and releases table/viewer references.
     * The configuration is not disposed here — the editor owns that lifecycle.
     */
    public void dispose()
    {
        if ( viewer != null )
        {
            configuration.dispose();

            if ( quickFilterWidget != null )
            {
                quickFilterWidget.dispose();
                quickFilterWidget = null;
            }

            table = null;
            viewer = null;
        }

        super.dispose();
    }


    // ── Terminal Exposes Its Viewer ────────────────────────────────────────────
    // Most sub-systems need the TableViewer reference to install listeners,
    // read column widths, or access the underlying SWT Table.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link TableViewer} for the search result table.
     * Used by the configuration, universal listener, and action group.
     *
     * @return the table viewer; {@code null} after disposal
     */
    public TableViewer getViewer()
    {
        return viewer;
    }


    // ── Terminal Exposes Its Filter Bar ───────────────────────────────────────
    // The action group needs a reference to the quick-filter widget so the
    // ShowQuickFilterAction can toggle it.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the quick-filter widget.
     * Used by {@link SearchResultEditorActionGroup} to create the
     * {@link ShowQuickFilterAction}.
     *
     * @return the quick-filter widget; {@code null} after disposal
     */
    public SearchResultEditorQuickFilterWidget getQuickFilterWidget()
    {
        return quickFilterWidget;
    }

}
