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

package org.apache.directory.studio.schemaeditor.view.editors.attributetype;


import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandlerAdapter;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandlerListener;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.apache.directory.studio.schemaeditor.view.editors.objectclass.ObjectClassEditor;
import org.apache.directory.studio.schemaeditor.view.editors.objectclass.ObjectClassEditorInput;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;


// ── CLASS: AttributeTypeEditorUsedByPage — LANDO SURVEYS CLOUD CITY'S MANIFEST ───────
// Lando Calrissian stands on Cloud City's observation deck and looks out over his domain.
// He can see at a glance which areas of the station depend on which services — "the
// life-support wing makes the pressuriser mandatory; the carbonite bay uses it optionally."
// He keeps two up-to-date lists: mandatory dependencies and optional ones.  If a new
// tenant moves in, both lists update immediately.
// This page is Lando's observation deck.  It shows two tables: object classes that
// require this attribute type (MUST list), and object classes that optionally allow it
// (MAY list).  Double-clicking a row opens that object class's editor — Lando's way of
// drilling down into a specific dependent area.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * The "Used By" tab page of the Attribute Type Editor.
 * It displays two read-only tables showing which object classes reference this attribute
 * type: the mandatory-attribute table (object classes that MUST include this attribute on
 * every entry) and the optional-attribute table (object classes that MAY include it).
 * The page reacts to schema handler events so the tables stay current when the schema
 * changes while the editor is open.  Double-clicking a row opens the corresponding
 * object class editor.
 * Think of this as Lando's observation deck: a live, up-to-date view of every dependent
 * in the station.
 */
public class AttributeTypeEditorUsedByPage extends AbstractAttributeTypeEditorPage
{
    /** The page ID */
    public static final String ID = AttributeTypeEditor.ID + "usedByPage"; //$NON-NLS-1$

    /** The Schema listener */
    private SchemaHandlerListener schemaHandlerListener = new SchemaHandlerAdapter()
    {
        /**
         * {@inheritDoc}
         */
        public void attributeTypeAdded( AttributeType at )
        {
            refreshTableViewers();
        }


        /**
         * {@inheritDoc}
         */
        public void attributeTypeModified( AttributeType at )
        {
            refreshTableViewers();
        }


        /**
         * {@inheritDoc}
         */
        public void attributeTypeRemoved( AttributeType at )
        {
            refreshTableViewers();
        }


        /**
         * {@inheritDoc}
         */
        public void objectClassAdded( ObjectClass oc )
        {
            refreshTableViewers();
        }


        /**
         * {@inheritDoc}
         */
        public void objectClassModified( ObjectClass oc )
        {
            refreshTableViewers();
        }


        /**
         * {@inheritDoc}
         */
        public void objectClassRemoved( ObjectClass oc )
        {
            refreshTableViewers();
        }


        /**
         * {@inheritDoc}
         */
        public void schemaAdded( Schema schema )
        {
            refreshTableViewers();
        }


        /**
         * {@inheritDoc}
         */
        public void schemaRemoved( Schema schema )
        {
            refreshTableViewers();
        }
    };

    // UI Widgets
    private Table mandatoryAttributeTable;
    private TableViewer mandatoryAttributeTableViewer;
    private Table optionalAttibuteTable;
    private TableViewer optionalAttibuteTableViewer;

    // Listeners
    /** The listener of the Mandatory Attribute Type Table*/
    private MouseAdapter mandatoryAttributeTableListener = new MouseAdapter()
    {
        public void mouseDoubleClick( MouseEvent e )
        {
            Object selectedItem = ( ( StructuredSelection ) mandatoryAttributeTableViewer.getSelection() )
                .getFirstElement();

            if ( selectedItem instanceof ObjectClass )
            {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                try
                {
                    page.openEditor( new ObjectClassEditorInput( ( ObjectClass ) selectedItem ),
                        ObjectClassEditor.ID );
                }
                catch ( PartInitException exception )
                {
                    PluginUtils.logError( "error when opening the editor", exception ); //$NON-NLS-1$
                }
            }
        }
    };

    /** The listener of the Optional Attribute Type Table*/
    private MouseAdapter optionalAttibuteTableListener = new MouseAdapter()
    {
        public void mouseDoubleClick( MouseEvent e )
        {
            Object selectedItem = ( ( StructuredSelection ) optionalAttibuteTableViewer.getSelection() )
                .getFirstElement();

            if ( selectedItem instanceof ObjectClass )
            {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                try
                {
                    page.openEditor( new ObjectClassEditorInput( ( ObjectClass ) selectedItem ),
                        ObjectClassEditor.ID );
                }
                catch ( PartInitException exception )
                {
                    PluginUtils.logError( "error when opening the editor", exception ); //$NON-NLS-1$
                }
            }
        }
    };


    // ── Lando Opens the Observation Deck and Registers with Station Control ───────────
    // Lando steps onto the observation deck, identifies which attribute type he's
    // watching over, and registers himself with Cloud City's central monitoring system
    // so he'll be notified whenever the tenant roster changes.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new "Used By" page and registers a schema handler listener so the
     * tables stay current as the schema changes.
     * The listener is registered here (in the constructor) so it's active before the
     * form is built; {@link #dispose()} removes it symmetrically.
     *
     * <p>For example — Lando takes his post:</p>
     * <pre>
     *   // Constructor runs: register schemaHandlerListener → any future schema event
     *   // triggers refreshTableViewers() → tables stay up-to-date.
     * </pre>
     *
     * @param editor  the {@link AttributeTypeEditor} that owns this page; provides
     *                access to the attribute type we're tracking
     */
    public AttributeTypeEditorUsedByPage( AttributeTypeEditor editor )
    {
        super( editor, ID, Messages.getString( "AttributeTypeEditorUsedByPage.UsedBy" ) ); //$NON-NLS-1$
        Activator.getDefault().getSchemaHandler().addListener( schemaHandlerListener );
    }


    // ── Lando Arranges the Two Display Boards ────────────────────────────────────────
    // Lando installs two side-by-side display boards on the observation deck:
    // the left one shows tenants that have this attribute as mandatory access,
    // the right one shows tenants with optional access.  Both boards get filled
    // immediately and start listening for external updates.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the "Used By" page UI — two side-by-side table sections.
     * We create a two-column grid layout and place the mandatory-attribute section on
     * the left and the optional-attribute section on the right.  Both tables are
     * immediately populated and mouse listeners are attached for double-click navigation.
     *
     * <p>For example — Lando's two display boards:</p>
     * <pre>
     *   // Left board: "Used as Mandatory Attribute" → object classes with AT in MUST
     *   // Right board: "Used as Optional Attribute"  → object classes with AT in MAY
     * </pre>
     *
     * @param managedForm  the Eclipse-managed form container for this page
     */
    protected void createFormContent( IManagedForm managedForm )
    {
        // Creating the base UI
        ScrolledForm form = managedForm.getForm();
        FormToolkit toolkit = managedForm.getToolkit();
        GridLayout layout = new GridLayout( 2, true );
        form.getBody().setLayout( layout );

        // As Mandatory Attribute Section
        createAsMandatoryAttributeSection( form.getBody(), toolkit );

        // As Optional Attribute Section
        createAsOptionalAttributeSection( form.getBody(), toolkit );

        // Filling the UI with values from the attribute type
        fillInUiFields();

        // Listeners initialization
        addListeners();

        // Help Context for Dynamic Help
        PlatformUI.getWorkbench().getHelpSystem().setHelp( form,
            PluginConstants.PLUGIN_ID + "." + "attribute_type_editor" ); //$NON-NLS-1$ //$NON-NLS-2$

        initialized = true;
    }


    // ── Lando Installs the Mandatory-Access Board ────────────────────────────────────
    // Lando installs the left display board: "Which areas require this access card?"
    // The board has a title and a description that names the attribute type, and a
    // scrollable list of dependent object classes below it.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Used As Mandatory Attribute" section with its table viewer.
     * The section title and description are dynamically set to reference the attribute
     * type's name (or OID if it has no names) so the user knows exactly which AT we're
     * talking about.  We attach the mandatory content provider and shared label provider.
     *
     * <p>For example — the mandatory board installation:</p>
     * <pre>
     *   // Section description: "The following object classes use 'cn' as a mandatory attribute"
     *   // Table: populated by ATEUsedByMandatoryTableContentProvider
     * </pre>
     *
     * @param parent   the parent SWT composite (the form body)
     * @param toolkit  the FormToolkit used to create Forms-styled widgets
     */
    private void createAsMandatoryAttributeSection( Composite parent, FormToolkit toolkit )
    {
        AttributeType modifiedAttributeType = getModifiedAttributeType();

        // As Mandatory Attribute Section
        Section mandatoryAttributeSection = toolkit.createSection( parent, Section.DESCRIPTION | Section.EXPANDED
            | Section.TITLE_BAR );
        List<String> names = modifiedAttributeType.getNames();
        if ( ( names != null ) && ( names.size() > 0 ) )
        {
            mandatoryAttributeSection
                .setDescription( NLS
                    .bind(
                        Messages.getString( "AttributeTypeEditorUsedByPage.AttributeTypeMandatory" ), new String[] { ViewUtils.concateAliases( names ) } ) ); //$NON-NLS-1$
        }
        else
        {
            mandatoryAttributeSection
                .setDescription( NLS
                    .bind(
                        Messages.getString( "AttributeTypeEditorUsedByPage.AttributeTypeMandatory" ), new String[] { modifiedAttributeType.getOid() } ) ); //$NON-NLS-1$
        }
        mandatoryAttributeSection.setText( Messages.getString( "AttributeTypeEditorUsedByPage.AsMandatoryAttribute" ) ); //$NON-NLS-1$

        // Creating the layout of the section
        Composite mandatoryAttributeSectionClient = toolkit.createComposite( mandatoryAttributeSection );
        mandatoryAttributeSectionClient.setLayout( new GridLayout() );
        toolkit.paintBordersFor( mandatoryAttributeSectionClient );
        mandatoryAttributeSection.setClient( mandatoryAttributeSectionClient );
        mandatoryAttributeSection.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        mandatoryAttributeTable = toolkit.createTable( mandatoryAttributeSectionClient, SWT.NONE );
        GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true );
        gridData.heightHint = 1;
        mandatoryAttributeTable.setLayoutData( gridData );
        mandatoryAttributeTableViewer = new TableViewer( mandatoryAttributeTable );
        mandatoryAttributeTableViewer.setContentProvider( new ATEUsedByMandatoryTableContentProvider() );
        mandatoryAttributeTableViewer.setLabelProvider( new ATEUsedByTablesLabelProvider() );
    }


    // ── Lando Installs the Optional-Access Board ─────────────────────────────────────
    // Lando installs the right display board: "Which areas optionally support this
    // access card?"  Same structure as the mandatory board, different content provider.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Used As Optional Attribute" section with its table viewer.
     * Mirrors {@link #createAsMandatoryAttributeSection} but uses
     * {@link ATEUsedByOptionalTableContentProvider} to populate with object classes
     * whose MAY list (rather than MUST list) includes this attribute type.
     *
     * <p>For example — the optional board installation:</p>
     * <pre>
     *   // Section description: "The following object classes use 'cn' as an optional attribute"
     *   // Table: populated by ATEUsedByOptionalTableContentProvider
     * </pre>
     *
     * @param parent   the parent SWT composite (the form body)
     * @param toolkit  the FormToolkit used to create Forms-styled widgets
     */
    private void createAsOptionalAttributeSection( Composite parent, FormToolkit toolkit )
    {
        AttributeType modifiedAttributeType = getModifiedAttributeType();

        // Matching Rules Section
        Section optionalAttributeSection = toolkit.createSection( parent, Section.DESCRIPTION | Section.EXPANDED
            | Section.TITLE_BAR );
        List<String> names = modifiedAttributeType.getNames();
        if ( ( names != null ) && ( names.size() > 0 ) )
        {
            optionalAttributeSection
                .setDescription( NLS
                    .bind(
                        Messages.getString( "AttributeTypeEditorUsedByPage.AttributeTypeOptional" ), new String[] { ViewUtils.concateAliases( names ) } ) ); //$NON-NLS-1$
        }
        else
        {
            optionalAttributeSection
                .setDescription( NLS
                    .bind(
                        Messages.getString( "AttributeTypeEditorUsedByPage.AttributeTypeOptional" ), new String[] { modifiedAttributeType.getOid() } ) ); //$NON-NLS-1$
        }
        optionalAttributeSection.setText( Messages.getString( "AttributeTypeEditorUsedByPage.AsOptionalAttribute" ) ); //$NON-NLS-1$

        // Creating the layout of the section
        Composite optionalAttributeSectionClient = toolkit.createComposite( optionalAttributeSection );
        optionalAttributeSectionClient.setLayout( new GridLayout() );
        toolkit.paintBordersFor( optionalAttributeSectionClient );
        optionalAttributeSection.setClient( optionalAttributeSectionClient );
        optionalAttributeSection.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        optionalAttibuteTable = toolkit.createTable( optionalAttributeSectionClient, SWT.NONE );
        GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true );
        gridData.heightHint = 1;
        optionalAttibuteTable.setLayoutData( gridData );
        optionalAttibuteTableViewer = new TableViewer( optionalAttibuteTable );
        optionalAttibuteTableViewer.setContentProvider( new ATEUsedByOptionalTableContentProvider() );
        optionalAttibuteTableViewer.setLabelProvider( new ATEUsedByTablesLabelProvider() );
    }


    // ── Lando Updates Both Display Boards with Current Data ─────────────────────────
    // Lando refreshes both display boards by feeding them the current attribute type —
    // the content providers will query the schema for the current MUST/MAY relationships.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates both table viewers with the current working-copy attribute type.
     * We set the same modified attribute type as the input for both viewers; each
     * viewer's content provider then queries the schema handler independently to
     * discover which object classes reference it in their MUST or MAY lists.
     */
    protected void fillInUiFields()
    {
        AttributeType modifiedAttributeType = getModifiedAttributeType();

        mandatoryAttributeTableViewer.setInput( modifiedAttributeType );
        optionalAttibuteTableViewer.setInput( modifiedAttributeType );
    }


    // ── Lando Stations Guards at Both Display Boards ─────────────────────────────────
    // Lando assigns a guard at each display board who watches for double-clicks (someone
    // wanting to drill into a specific object class) and opens the corresponding editor.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches double-click mouse listeners to both tables so the user can navigate
     * directly to an object class editor by double-clicking its row.
     */
    protected void addListeners()
    {
        addMouseListener( mandatoryAttributeTable, mandatoryAttributeTableListener );
        addMouseListener( optionalAttibuteTable, optionalAttibuteTableListener );
    }


    // ── Lando Stands Down the Guards ─────────────────────────────────────────────────
    // Between schema reloads, Lando recalls the guards so they don't fire double-click
    // events while the tables are being repopulated.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Detaches the double-click listeners from both tables.
     * Called before a UI refresh so that programmatic table updates don't trigger
     * navigation events.
     */
    protected void removeListeners()
    {
        removeMouseListener( mandatoryAttributeTable, mandatoryAttributeTableListener );
        removeMouseListener( optionalAttibuteTable, optionalAttibuteTableListener );
    }


    // ── Lando Orders an Immediate Update to Both Boards ─────────────────────────────
    // When Cloud City's central monitoring system fires an alert (schema changed),
    // Lando immediately orders both boards refreshed without waiting for the next
    // regular update cycle.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes both table viewers from the schema immediately.
     * Called from the schema handler listener when any relevant schema event fires
     * (object class added/modified/removed, attribute type added/modified/removed,
     * or schema added/removed).
     * We null-guard each viewer in case this is called before the form has been created.
     *
     * <p>For example — Lando orders an immediate board refresh:</p>
     * <pre>
     *   // Schema event: "objectClass 'person' was modified" → refreshTableViewers()
     *   // Both tables re-query the schema and update their displayed rows.
     * </pre>
     */
    public void refreshTableViewers()
    {
        if ( mandatoryAttributeTableViewer != null )
        {
            mandatoryAttributeTableViewer.refresh();
        }
        if ( optionalAttibuteTableViewer != null )
        {
            optionalAttibuteTableViewer.refresh();
        }
    }


    // ── Lando Closes the Observation Deck ───────────────────────────────────────────
    // When the attribute type editor closes, Lando deregisters from the central
    // monitoring system — no point receiving alerts for a page that no longer exists.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up on page disposal: removes the schema handler listener (symmetrically
     * matching the registration done in the constructor) and delegates to the parent
     * dispose for SWT resource cleanup.
     * Always remove the listener before calling super.dispose() to avoid a listener
     * callback arriving after the widgets are gone.
     */
    public void dispose()
    {
        Activator.getDefault().getSchemaHandler().removeListener( schemaHandlerListener );

        super.dispose();
    }
}
