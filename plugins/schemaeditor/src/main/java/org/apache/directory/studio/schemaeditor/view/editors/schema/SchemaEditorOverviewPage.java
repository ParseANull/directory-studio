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

package org.apache.directory.studio.schemaeditor.view.editors.schema;


import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandlerAdapter;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandlerListener;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.apache.directory.studio.schemaeditor.view.editors.attributetype.AttributeTypeEditor;
import org.apache.directory.studio.schemaeditor.view.editors.attributetype.AttributeTypeEditorInput;
import org.apache.directory.studio.schemaeditor.view.editors.objectclass.ObjectClassEditor;
import org.apache.directory.studio.schemaeditor.view.editors.objectclass.ObjectClassEditorInput;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;


// ── CLASS: SchemaEditorOverviewPage — LUKE'S BINARY SUNSET FROM THE DESERT RIDGE ──
// Luke stands on the ridge above the Lars homestead as the twin suns set —
// on his left, the moisture vaporators (attribute types, the structural
// elements); on his right, the trade goods (object classes, the semantic units).
// Both sections spread across his field of view at the same time, each one
// a sorted catalogue of what this schema contains.
// This page is that panorama: two side-by-side sections showing all attribute
// types and all object classes in the selected schema, sortable and clickable.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Overview page inside the {@link SchemaEditor}.
 * It displays all {@link AttributeType} and {@link ObjectClass} definitions in
 * a schema side by side in two table sections. The page is read-only — users
 * navigate to individual editors by double-clicking a row. It refreshes
 * automatically when the underlying schema changes.
 * Think of it as Luke's horizon view: the full inventory of the schema laid
 * out at a glance, both halves visible simultaneously.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaEditorOverviewPage extends FormPage
{
    /** The page ID */
    public static final String ID = SchemaEditor.ID + "overviewPage"; //$NON-NLS-1$

    /** The associated schema */
    private Schema originalSchema;

    private SchemaHandlerListener schemaHandlerListener = new SchemaHandlerAdapter()
    {
        public void attributeTypeAdded( AttributeType at )
        {
            refreshUI();
        }


        public void attributeTypeModified( AttributeType at )
        {
            refreshUI();
        }


        public void attributeTypeRemoved( AttributeType at )
        {
            refreshUI();
        }


        public void matchingRuleAdded( MatchingRule mr )
        {
            refreshUI();
        }


        public void matchingRuleModified( MatchingRule mr )
        {
            refreshUI();
        }


        public void matchingRuleRemoved( MatchingRule mr )
        {
            refreshUI();
        }


        public void objectClassAdded( ObjectClass oc )
        {
            refreshUI();
        }


        public void objectClassModified( ObjectClass oc )
        {
            refreshUI();
        }


        public void objectClassRemoved( ObjectClass oc )
        {
            refreshUI();
        }


        public void schemaAdded( Schema schema )
        {
            refreshUI();
        }


        public void schemaRemoved( Schema schema )
        {
            if ( !schema.equals( originalSchema ) )
            {
                refreshUI();
            }
        }


        public void schemaRenamed( Schema schema )
        {
            refreshUI();
        }


        public void syntaxAdded( LdapSyntax syntax )
        {
            refreshUI();
        }


        public void syntaxModified( LdapSyntax syntax )
        {
            refreshUI();
        }


        public void syntaxRemoved( LdapSyntax syntax )
        {
            refreshUI();
        }
    };

    // UI Fields
    private Section attributeTypesSection;
    private TableViewer attributeTypesTableViewer;
    private Section objectClassesSection;
    private TableViewer objectClassesTableViewer;

    // Listeners
    /** The listener of the Attribute Types TableViewer */
    private IDoubleClickListener attributeTypesTableViewerListener = new IDoubleClickListener()
    {
        /**
         * {@inheritDoc}
         */
        public void doubleClick( DoubleClickEvent event )
        {
            StructuredSelection selection = ( StructuredSelection ) event.getSelection();

            if ( !selection.isEmpty() )
            {
                AttributeType at = ( AttributeType ) selection.getFirstElement();

                try
                {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
                        new AttributeTypeEditorInput( at ), AttributeTypeEditor.ID );
                }
                catch ( PartInitException exception )
                {
                    PluginUtils.logError( Messages.getString( "SchemaEditorOverviewPage.ErrorOpenEditor" ), exception ); //$NON-NLS-1$
                    ViewUtils.displayErrorMessageDialog(
                        Messages.getString( "SchemaEditorOverviewPage.Error" ), Messages //$NON-NLS-1$
                            .getString( "SchemaEditorOverviewPage.ErrorOpenEditor" ) ); //$NON-NLS-1$
                }
            }
        }
    };

    /** The listener of the Object Classes TableViewer */
    private IDoubleClickListener objectClassesTableViewerListener = new IDoubleClickListener()
    {
        /**
         * {@inheritDoc}
         */
        public void doubleClick( DoubleClickEvent event )
        {
            StructuredSelection selection = ( StructuredSelection ) event.getSelection();

            if ( !selection.isEmpty() )
            {
                ObjectClass oc = ( ObjectClass ) selection.getFirstElement();

                try
                {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
                        new ObjectClassEditorInput( oc ), ObjectClassEditor.ID );
                }
                catch ( PartInitException exception )
                {
                    PluginUtils.logError( Messages.getString( "SchemaEditorOverviewPage.ErrorOpenEditor" ), exception ); //$NON-NLS-1$
                    ViewUtils.displayErrorMessageDialog(
                        Messages.getString( "SchemaEditorOverviewPage.Error" ), Messages //$NON-NLS-1$
                            .getString( "SchemaEditorOverviewPage.ErrorOpenEditor" ) ); //$NON-NLS-1$
                }
            }
        }
    };


    // ── Luke Steps Out to the Ridge ───────────────────────────────────────────
    // Luke walks up to his favorite spot on the ridge, registers his position
    // with the homestead sensors, and gets ready to take in the full sunset.
    // We do the same: register this page with the parent editor, set up the
    // tab label, and hook into the schema handler so we update whenever the
    // schema changes while the editor is open.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the Overview page and registers it with the parent editor and schema handler.
     * We attach a {@link SchemaHandlerListener} here so the page refreshes automatically
     * whenever any schema element is added, modified, or removed in the live schema set.
     *
     * @param editor  the parent {@link SchemaEditor} that owns this page
     */
    public SchemaEditorOverviewPage( FormEditor editor )
    {
        super( editor, ID, Messages.getString( "SchemaEditorOverviewPage.Overview" ) ); //$NON-NLS-1$
        Activator.getDefault().getSchemaHandler().addListener( schemaHandlerListener );
    }


    // ── Luke's View Expands to Fill the Horizon ───────────────────────────────
    // As Luke's eyes adjust to the fading light, the full horizon opens up:
    // attribute types on the left panel, object classes on the right panel —
    // both populated from the same schema, both wired to navigate on double-click.
    // This method builds that two-panel UI, populates it, and wires the listeners.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the Overview page UI: two side-by-side sections, one for attribute types
     * and one for object classes, each backed by a sortable {@link TableViewer}.
     * After construction we populate the tables from the schema and attach double-click
     * listeners so the user can navigate to individual element editors.
     *
     * @param managedForm  the Eclipse Forms managed form hosting this page
     */
    protected void createFormContent( IManagedForm managedForm )
    {
        // Getting the associated schema
        originalSchema = ( ( SchemaEditor ) getEditor() ).getSchema();

        // Creating the base UI
        ScrolledForm form = managedForm.getForm();
        FormToolkit toolkit = managedForm.getToolkit();
        GridLayout layout = new GridLayout( 2, true );
        form.getBody().setLayout( layout );

        createAttributeTypesSection( form.getBody(), toolkit );

        createObjectClassesSection( form.getBody(), toolkit );

        // Initializes the UI from the schema
        fillInUiFields();

        // Listeners initialization
        addListeners();

        // Help Context for Dynamic Help
        PlatformUI.getWorkbench().getHelpSystem().setHelp( form, PluginConstants.PLUGIN_ID + "." + "schema_editor" ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── Luke Scans the Left Half — Moisture Vaporators ────────────────────────
    // On his left, Luke sees all the moisture vaporators — the structural
    // workhorses of the farm, each one carefully catalogued by name.
    // This method builds the "Attribute Types" section panel on the left side
    // of the form, with a table viewer that will list all the schema's attribute types.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the "Attribute Types" form section.
     * This section contains a single sortable table listing all attribute types
     * in the schema. Double-clicking a row opens the {@link AttributeTypeEditor}
     * for that type.
     *
     * @param parent   the parent composite that contains both sections
     * @param toolkit  the {@link FormToolkit} used to create styled widgets
     */
    private void createAttributeTypesSection( Composite parent, FormToolkit toolkit )
    {
        // Attribute Types Section
        attributeTypesSection = toolkit.createSection( parent, Section.DESCRIPTION | Section.EXPANDED
            | Section.TITLE_BAR );
        attributeTypesSection.setDescription( "" ); //$NON-NLS-1$
        attributeTypesSection.setText( Messages.getString( "SchemaEditorOverviewPage.AttributeTypes" ) ); //$NON-NLS-1$

        // Creating the layout of the section
        Composite attributeTypesSectionClient = toolkit.createComposite( attributeTypesSection );
        attributeTypesSectionClient.setLayout( new GridLayout() );
        toolkit.paintBordersFor( attributeTypesSectionClient );
        attributeTypesSection.setClient( attributeTypesSectionClient );
        attributeTypesSection.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        attributeTypesTableViewer = new TableViewer( attributeTypesSectionClient, SWT.SINGLE | SWT.H_SCROLL
            | SWT.V_SCROLL | SWT.BORDER );
        attributeTypesTableViewer.setContentProvider( new SchemaEditorTableViewerContentProvider() );
        attributeTypesTableViewer.setLabelProvider( new SchemaEditorTableViewerLabelProvider() );
        attributeTypesTableViewer.getTable().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    }


    // ── Luke Scans the Right Half — Trade Goods ───────────────────────────────
    // On his right, Luke sees the storage bins of optional trade goods — the
    // object classes that define the semantic types the schema supports.
    // This method builds the "Object Classes" section panel on the right side
    // of the form, with a table viewer that will list all the schema's object classes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the "Object Classes" form section.
     * This section contains a single sortable table listing all object classes
     * in the schema. Double-clicking a row opens the {@link ObjectClassEditor}
     * for that class.
     *
     * @param parent   the parent composite that contains both sections
     * @param toolkit  the {@link FormToolkit} used to create styled widgets
     */
    private void createObjectClassesSection( Composite parent, FormToolkit toolkit )
    {
        // Attribute Types Section
        objectClassesSection = toolkit.createSection( parent, Section.DESCRIPTION | Section.EXPANDED
            | Section.TITLE_BAR );
        objectClassesSection.setDescription( "" );//$NON-NLS-1$
        objectClassesSection.setText( Messages.getString( "SchemaEditorOverviewPage.ObjectClasses" ) ); //$NON-NLS-1$

        // Creating the layout of the section
        Composite objectClassesSectionClient = toolkit.createComposite( objectClassesSection );
        objectClassesSectionClient.setLayout( new GridLayout() );
        toolkit.paintBordersFor( objectClassesSectionClient );
        objectClassesSection.setClient( objectClassesSectionClient );
        objectClassesSection.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        objectClassesTableViewer = new TableViewer( objectClassesSectionClient, SWT.SINGLE | SWT.H_SCROLL
            | SWT.V_SCROLL | SWT.BORDER );
        objectClassesTableViewer.setContentProvider( new SchemaEditorTableViewerContentProvider() );
        objectClassesTableViewer.setLabelProvider( new SchemaEditorTableViewerLabelProvider() );
        objectClassesTableViewer.getTable().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    }


    // ── Luke Reads Every Station on the Horizon ───────────────────────────────
    // Luke takes it all in: each vaporator on the left and each trade bin on
    // the right, their names updated in the section headers and their full
    // lists loaded into the table viewers.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates both table viewers from the associated schema.
     * We also update each section's description text with the schema name so the
     * user can see at a glance which schema's contents are being displayed.
     */
    private void fillInUiFields()
    {
        attributeTypesSection.setDescription( NLS.bind(
            Messages.getString( "SchemaEditorOverviewPage.SchemaAttribute" ), new String[] //$NON-NLS-1$
            { originalSchema.getSchemaName() } ) );
        objectClassesSection.setDescription( NLS.bind( Messages
            .getString( "SchemaEditorOverviewPage.SchemaObjectClasses" ), new String[] //$NON-NLS-1$
            { originalSchema.getSchemaName() } ) );
        attributeTypesTableViewer.setInput( originalSchema.getAttributeTypes() );
        objectClassesTableViewer.setInput( originalSchema.getObjectClasses() );
    }


    // ── Luke Wires Up His Field Binoculars ────────────────────────────────────
    // Luke raises his binoculars and locks onto each station — now when he
    // double-taps a vaporator or a trade bin, he can zoom in for details.
    // We do the same: attach double-click listeners to both table viewers so
    // the user can navigate to individual element editors.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches double-click listeners to both table viewers.
     * A double-click on an attribute type row opens its {@link AttributeTypeEditor};
     * a double-click on an object class row opens its {@link ObjectClassEditor}.
     */
    private void addListeners()
    {
        attributeTypesTableViewer.addDoubleClickListener( attributeTypesTableViewerListener );
        objectClassesTableViewer.addDoubleClickListener( objectClassesTableViewerListener );
    }


    // ── Luke Lowers His Binoculars ────────────────────────────────────────────
    // When Luke is done scanning, he lowers his binoculars — deactivating the
    // zoom-in feature so stray double-clicks don't accidentally open editors
    // after the page has been refreshed or disposed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes double-click listeners from both table viewers.
     * We call this before a programmatic UI refresh to avoid spurious editor-open
     * events, and on disposal to prevent memory leaks.
     */
    private void removeListeners()
    {
        attributeTypesTableViewer.removeDoubleClickListener( attributeTypesTableViewerListener );
        objectClassesTableViewer.removeDoubleClickListener( objectClassesTableViewerListener );
    }


    // ── Luke Heads Back Inside — Sunset Complete ──────────────────────────────
    // The twin suns have set. Luke turns away from the horizon, deregisters
    // his sensors from the homestead feed, and heads inside — no lingering
    // connections to the now-dark landscape.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Cleans up this page when it is disposed.
     * We remove event listeners and our schema handler listener to prevent
     * memory leaks and stale callbacks after the editor tab is closed.
     */
    public void dispose()
    {
        removeListeners();

        Activator.getDefault().getSchemaHandler().removeListener( schemaHandlerListener );

        super.dispose();
    }


    // ── Luke Does a Fresh Scan of the Horizon ─────────────────────────────────
    // When the light changes — when something in the schema shifts — Luke does
    // a fresh scan: he lowers his binoculars, reloads the current data, and
    // raises them again so the view is accurate.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the page's table viewers from the current schema state.
     * We temporarily remove listeners while repopulating to avoid spurious
     * event loops, then reattach them once the data is fresh.
     */
    public void refreshUI()
    {
        removeListeners();
        fillInUiFields();
        addListeners();
    }
}
