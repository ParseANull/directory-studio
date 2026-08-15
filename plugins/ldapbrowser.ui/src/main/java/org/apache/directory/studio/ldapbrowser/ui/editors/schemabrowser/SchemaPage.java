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

package org.apache.directory.studio.ldapbrowser.ui.editors.schemabrowser;


import org.apache.directory.api.ldap.model.schema.AbstractSchemaObject;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;


// ── CLASS: SchemaPage — The Death Star Blueprints R2 Is Carrying ──────────────
// Every major system on the Death Star has its own blueprint section — but they
// all share the same physical format: a master index on the left (the list of
// components), a detail readout on the right, a filter field to search the index,
// and a connection indicator at the top showing which installation these plans
// belong to.  SchemaPage defines that common layout.  Concrete subclasses fill
// in the specific index contents (object classes, attribute types, etc.) while
// we provide the master/detail sash, the filter text, the toolbar actions, and
// the selection wiring.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class that defines the shared master/detail layout for every
 * tab in the schema browser.
 * A concrete subclass provides a content provider, label provider, sorter,
 * filter, and details page specific to one schema element type; this class
 * builds the sash form, table viewer, filter text, and toolbar actions that
 * are identical across all five tabs.
 * Think of this class as the standard blueprint frame: same border, same
 * index-on-left/detail-on-right layout, same toolbar — only the diagrams
 * inside differ.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class SchemaPage
{

    /** The connection widget */
    protected BrowserConnectionWidgetContributionItem connectionCombo;

    /** The show default schema action */
    protected ShowDefaultSchemaAction showDefaultSchemaAction;

    /** The reload schema action */
    protected ReloadSchemaAction reloadSchemaAction;

    /** The schema browser */
    protected SchemaBrowser schemaBrowser;

    /** The toolkit used to create controls */
    protected FormToolkit toolkit;

    /** The outer form */
    protected Form form;

    /** The sash form, used to split the master and detail form */
    protected SashForm sashForm;

    /** The master form, contains the schema element list */
    protected ScrolledForm masterForm;

    /** The detail form, contains the schema details */
    protected ScrolledForm detailForm;

    /** The schema details page */
    protected SchemaDetailsPage detailsPage;

    /** The section of the master form */
    protected Section section;

    /** The filter field of the master form */
    protected Text filterText;

    /** The list with all schema elements */
    protected TableViewer viewer;

    /** Flag indicating if the viewer's selection is changed programatically */
    protected boolean inChange;


    // ── R2 Attaches To The Schema Browser ─────────────────────────────────────────
    // R2 receives a reference to which Death Star installation these blueprints
    // belong to, so every panel he creates stays linked to the correct parent.
    // We store the schemaBrowser reference and reset the inChange flag so the
    // selection listener knows when we are changing things programmatically.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new schema page wired to the given schema browser.
     * Subclasses call this via {@code super(schemaBrowser)} as the first statement
     * in their constructors.
     *
     * @param schemaBrowser  the schema browser editor that hosts this page
     */
    public SchemaPage( SchemaBrowser schemaBrowser )
    {
        this.schemaBrowser = schemaBrowser;
        this.inChange = false;
    }


    // ── R2 Reloads The Index From His Storage ─────────────────────────────────────
    // After connecting to a new installation R2 discards the stale index and
    // projects a fresh list of components from the current schema.
    // We update the viewer's input (the Schema object), clear the selection,
    // and call viewer.refresh() to repopulate the table.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reloads the master list with the currently applicable schema.
     * Uses the default schema if the "Show Default Schema" toggle is on,
     * otherwise uses the active connection's schema.
     * Does nothing visually if the input has not actually changed, to avoid
     * unnecessary flicker.
     *
     * <p>For example — R2 reloads the component index:</p>
     * <pre>
     *   refresh();
     *   // viewer now shows all attribute types from the server's schema
     * </pre>
     */
    public void refresh()
    {
        Schema schema = null;
        if ( showDefaultSchemaAction.isChecked() )
        {
            schema = Schema.DEFAULT_SCHEMA;
        }
        else if ( getConnection() != null )
        {
            schema = getConnection().getSchema();
        }

        if ( viewer.getInput() != schema )
        {
            viewer.setInput( schema );
            viewer.setSelection( StructuredSelection.EMPTY );
        }

        form.setText( getTitle() );
        viewer.refresh();
    }


    // ── R2 Reports The Panel Title ─────────────────────────────────────────────────
    // Each blueprint panel has a heading that names the system it covers —
    // "Targeting Systems," "Power Grid," etc.
    // Subclasses return a localised title string used in the form header and section.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised title for this schema page, shown in the form header
     * and the master section.
     * Implemented by each concrete subclass.
     *
     * @return the page title, e.g. "Attribute Types"
     */
    protected abstract String getTitle();


    // ── R2 Provides The Filter Hint ───────────────────────────────────────────────
    // Below the panel title R2 shows a brief instruction: "Enter component name
    // to filter the index."  Subclasses return their specific filter hint.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the description shown below the section title to hint what the
     * filter text field is for (e.g. "Select an attribute type").
     * Implemented by each concrete subclass.
     *
     * @return the filter hint description
     */
    protected abstract String getFilterDescription();


    // ── R2 Supplies The Component Index ───────────────────────────────────────────
    // The content provider feeds the table viewer with the actual list of schema
    // elements — the index of all components in this blueprint section.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the JFace content provider that supplies the table viewer with schema
     * elements from the active schema.
     * Implemented by each concrete subclass.
     *
     * @return the content provider for this schema element type
     */
    protected abstract IStructuredContentProvider getContentProvider();


    // ── R2 Supplies The Display Labels ────────────────────────────────────────────
    // The label provider turns each schema object into the display string the
    // viewer shows in its rows — like rendering the component names in the index.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the JFace label provider used to render schema element rows in the
     * table viewer.
     * Implemented by each concrete subclass.
     *
     * @return the table label provider
     */
    protected abstract ITableLabelProvider getLabelProvider();


    // ── R2 Sorts The Component Index ──────────────────────────────────────────────
    // The sorter determines the order in which component names appear in the index —
    // alphabetical by default.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the JFace viewer sorter used to order the schema element list.
     * Implemented by each concrete subclass.
     *
     * @return the viewer sorter
     */
    protected abstract ViewerSorter getSorter();


    // ── R2 Applies The Filter ─────────────────────────────────────────────────────
    // The filter hides components whose names do not match the text the user typed
    // in the filter field — narrowing the index to matching entries only.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the JFace viewer filter that hides elements not matching the filter
     * text typed by the user.
     * Implemented by each concrete subclass.
     *
     * @return the viewer filter
     */
    protected abstract ViewerFilter getFilter();


    // ── R2 Creates The Detail Panel ───────────────────────────────────────────────
    // Each blueprint section has its own detail readout format — the attribute type
    // panel is different from the object class panel.  Subclasses return their
    // specific detail page implementation.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and returns the {@link SchemaDetailsPage} specific to this schema
     * element type.
     * Called once during {@link #createControl(Composite)}.
     * Implemented by each concrete subclass.
     *
     * @return the details page for this schema element type
     */
    protected abstract SchemaDetailsPage getDetailsPage();


    // ── R2 Builds The Master Index Panel ──────────────────────────────────────────
    // R2 assembles the left-hand index panel: a title section, a filter field,
    // and a scrollable table of component names.
    // We create the Section, the filter Text, and the TableViewer here; the actual
    // content comes from the abstract content/label/sorter/filter methods.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the master (left-hand) section of the master/detail split.
     * Creates the section with title and filter description, a filter text widget
     * that live-refreshes the table as the user types, and the scrollable table
     * viewer populated by the subclass-supplied providers.
     *
     * @param parent  the composite to build the master section inside
     */
    //protected abstract void createMaster( Composite body );
    private void createMaster( Composite parent )
    {
        // create section
        section = toolkit.createSection( parent, Section.DESCRIPTION );
        section.marginWidth = 10;
        section.marginHeight = 12;
        section.setText( getTitle() );
        section.setDescription( getFilterDescription() );
        toolkit.createCompositeSeparator( section );

        // create client
        Composite client = toolkit.createComposite( section, SWT.WRAP );
        GridLayout layout = new GridLayout( 2, false );
        layout.marginWidth = 5;
        layout.marginHeight = 5;
        client.setLayout( layout );
        section.setClient( client );

        // create filter field
        toolkit.createLabel( client, Messages.getString( "SchemaPage.Filter" ) ); //$NON-NLS-1$
        this.filterText = toolkit.createText( client, "", SWT.NONE | SWT.SEARCH | SWT.CANCEL ); //$NON-NLS-1$
        this.filterText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        this.filterText.setData( FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER );
        this.filterText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                viewer.refresh();
            }
        } );

        // create table
        Table t = toolkit.createTable( client, SWT.NONE );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.horizontalSpan = 2;
        gd.heightHint = 20;
        gd.widthHint = 100;
        t.setLayoutData( gd );
        toolkit.paintBordersFor( client );

        // setup viewer
        viewer = new TableViewer( t );
        viewer.setContentProvider( getContentProvider() );
        viewer.setLabelProvider( getLabelProvider() );
        viewer.setSorter( getSorter() );
        viewer.addFilter( getFilter() );
    }


    // ── R2 Builds The Detail Readout Panel ────────────────────────────────────────
    // On the right side of the blueprint display R2 mounts the detail readout
    // where the full specification of the selected component will be shown.
    // We instantiate the subclass detail page and let it build its own UI.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the detail (right-hand) section of the master/detail split by
     * asking the subclass for its {@link SchemaDetailsPage} and calling
     * {@link SchemaDetailsPage#createContents(ScrolledForm)} on it.
     *
     * @param body  the composite to build the detail panel inside
     */
    private void createDetail( Composite body )
    {
        detailsPage = getDetailsPage();
        detailsPage.createContents( this.detailForm );
    }


    // ── R2 Jumps To A Requested Component In The Index ────────────────────────────
    // The officer says "Show me component CN-2187" — R2 scrolls the index to that
    // entry and highlights it, updating the detail readout on the right.
    // If the item is not visible (filtered out) we clear the filter first.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Selects the given schema element in the master list, scrolling to reveal it
     * and updating the details page.
     * If the element is filtered out (not visible in the table), we clear the
     * filter text so it becomes visible, then set the selection.
     * We use the {@code inChange} flag to suppress the "user changed selection"
     * code path while making programmatic changes.
     *
     * <p>For example — R2 highlights the requested component:</p>
     * <pre>
     *   select(myObjectClass);
     *   // viewer scrolls to myObjectClass and selects it
     *   // details page updates to show myObjectClass details
     * </pre>
     *
     * @param obj  the schema element to select; must be a type known to the content provider
     */
    public void select( Object obj )
    {
        ISelection newSelection = new StructuredSelection( obj );
        ISelection oldSelection = this.viewer.getSelection();

        if ( !newSelection.equals( oldSelection ) )
        {
            inChange = true;
            this.viewer.setSelection( newSelection, true );
            if ( this.viewer.getSelection().isEmpty() )
            {
                this.filterText.setText( "" ); //$NON-NLS-1$
                this.viewer.setSelection( newSelection, true );
            }
            inChange = false;
        }
    }


    // ── R2 Powers Down His Blueprint Panel ────────────────────────────────────────
    // R2 shuts down the detail readout, then releases his reference to the
    // schema browser and the toolkit, making everything eligible for GC.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes the detail page, nulls the schema browser reference, and disposes
     * the toolkit so SWT resources are released when this tab closes.
     *
     * <p>For example — R2 shuts down the panel:</p>
     * <pre>
     *   dispose();
     *   // detailsPage disposed, schemaBrowser = null, toolkit disposed
     * </pre>
     */
    public void dispose()
    {
        this.detailsPage.dispose();

        this.schemaBrowser = null;
        this.toolkit.dispose();
        this.toolkit = null;
    }


    // ── R2 Assembles The Full Blueprint Frame ─────────────────────────────────────
    // R2 snaps the master index panel and the detail readout into the sash frame,
    // wires up the selection listener so clicking a component reveals its detail,
    // and mounts the toolbar actions (connection picker, default schema toggle,
    // reload button).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the complete UI for this schema page: a sash form with the master
     * list on the left and the detail pane on the right, plus a toolbar with the
     * connection dropdown, "Show Default Schema" toggle, and "Reload Schema" button.
     * Also wires the selection listener that drives the details page.
     * Called once by {@link SchemaBrowser#createPartControl(Composite)}.
     *
     * <p>For example — R2 assembles the full display:</p>
     * <pre>
     *   Control control = createControl(tabFolder);
     *   // master list + detail pane + toolbar all created and linked
     * </pre>
     *
     * @param parent  the parent composite (a CTabFolder item's control area)
     * @return        the root control of this page, to be set on the CTabItem
     */
    Control createControl( Composite parent )
    {
        this.toolkit = new FormToolkit( parent.getDisplay() );
        this.form = this.toolkit.createForm( parent );
        this.form.getBody().setLayout( new FillLayout() );

        this.sashForm = new SashForm( this.form.getBody(), SWT.HORIZONTAL );
        this.sashForm.setLayout( new FillLayout() );

        this.masterForm = this.toolkit.createScrolledForm( this.sashForm );
        this.detailForm = new ScrolledForm( this.sashForm, SWT.V_SCROLL | this.toolkit.getOrientation() );
        this.detailForm.setExpandHorizontal( true );
        this.detailForm.setExpandVertical( true );
        this.detailForm.setBackground( this.toolkit.getColors().getBackground() );
        this.detailForm.setForeground( this.toolkit.getColors().getColor( IFormColors.TITLE ) );
        this.detailForm.setFont( JFaceResources.getHeaderFont() );
        this.sashForm.setWeights( new int[]
            { 50, 50 } );

        this.masterForm.getBody().setLayout( new FillLayout() );
        this.createMaster( this.masterForm.getBody() );

        this.detailForm.getBody().setLayout( new FillLayout() );
        this.createDetail( this.detailForm.getBody() );
        viewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                ISelection selection = event.getSelection();
                if ( selection.isEmpty() )
                {
                    detailsPage.setInput( null );
                }
                else
                {
                    Object obj = ( ( StructuredSelection ) selection ).getFirstElement();
                    detailsPage.setInput( obj );

                    // Do not set the input of the schema browser if
                    // the selection was changed programatically.
                    if ( !inChange && obj instanceof AbstractSchemaObject )
                    {
                        schemaBrowser.setInput( new SchemaBrowserInput( getConnection(),
                            ( AbstractSchemaObject ) obj ) );
                    }
                }
            }
        } );

        connectionCombo = new BrowserConnectionWidgetContributionItem( this );
        this.form.getToolBarManager().add( connectionCombo );
        this.form.getToolBarManager().add( new Separator() );
        showDefaultSchemaAction = new ShowDefaultSchemaAction( schemaBrowser );
        this.form.getToolBarManager().add( showDefaultSchemaAction );
        this.form.getToolBarManager().add( new Separator() );
        reloadSchemaAction = new ReloadSchemaAction( this );
        this.form.getToolBarManager().add( reloadSchemaAction );
        this.form.updateToolBar();

        this.refresh();

        return this.form;
    }


    // ── R2 Reports His Current Installation Reference ─────────────────────────────
    // Other components that need to query R2 for information ask "which schema
    // browser are you attached to?" so they can coordinate their updates.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent schema browser editor that this page belongs to.
     * Other code occasionally needs to navigate the browser (e.g. hyperlink
     * clicks) and requires this reference to call {@code setInput()}.
     *
     * @return the schema browser; may be null after {@link #dispose()} is called
     */
    public SchemaBrowser getSchemaBrowser()
    {
        return schemaBrowser;
    }


    // ── R2 Reports His Active Connection ──────────────────────────────────────────
    // The toolbar dropdown holds the currently selected LDAP connection; we
    // delegate to it rather than maintaining a separate field.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP connection currently selected in the toolbar dropdown.
     * The connection drives which schema is shown in the master list and which
     * server the reload action targets.
     *
     * @return the active browser connection, or null if none is selected
     */
    public IBrowserConnection getConnection()
    {
        return connectionCombo.getConnection();
    }


    // ── R2 Re-Points To A New Ship's Computer ─────────────────────────────────────
    // When the crew switches ships R2 updates the toolbar dropdown to show the new
    // connection, refreshes the enabled state of the reload action, and reloads
    // the blueprint index from the new server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Switches this page to display the schema from the given connection.
     * Updates the connection dropdown, refreshes the enabled state of the reload
     * action, and calls {@link #refresh()} to reload the master list.
     *
     * @param connection  the LDAP connection to show; null clears the page
     */
    public void setConnection( IBrowserConnection connection )
    {
        connectionCombo.setConnection( connection );
        reloadSchemaAction.updateEnabledState();
        refresh();
    }


    // ── R2 Reports Whether Default Blueprint Mode Is On ───────────────────────────
    // The technician asks "are you in default-installation mode?" — R2 checks
    // his toggle and reports.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the "Show Default Schema" toggle is currently active,
     * meaning the page is showing the built-in default schema rather than a
     * live server schema.
     *
     * @return true if the default schema toggle is checked
     */
    public boolean isShowDefaultSchema()
    {
        return showDefaultSchemaAction.isChecked();
    }


    // ── R2 Flips The Default Blueprint Mode Switch ────────────────────────────────
    // The technician flips the master switch to "default installation" mode; R2
    // updates the toggle, greys out the connection widget, disables reload,
    // and reloads the index from the factory defaults.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Switches this page between the live connection schema and the built-in
     * default schema.
     * Updates the toggle state, enables/disables the connection dropdown and
     * reload action accordingly, then calls {@link #refresh()} to reload the list.
     *
     * @param b  true to show the default schema, false to show the connection schema
     */
    public void setShowDefaultSchema( boolean b )
    {
        showDefaultSchemaAction.setChecked( b );
        connectionCombo.updateEnabledState();
        reloadSchemaAction.updateEnabledState();
        refresh();
    }
}
