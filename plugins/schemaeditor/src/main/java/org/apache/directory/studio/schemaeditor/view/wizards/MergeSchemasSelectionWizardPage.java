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
package org.apache.directory.studio.schemaeditor.view.wizards;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.ProjectType;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;


// ── CLASS: MergeSchemasSelectionWizardPage — Luke At The Cave On Dagobah ─────
// Luke stands at the entrance to the dark side cave on Dagobah, facing a choice:
// which path do I take?  The cave shows him all the possibilities branching before
// him — projects, schemas within them, and individual attribute types and object
// classes within those schemas.  He picks the branches he wants to bring back with
// him to the Rebellion.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The first page of {@link MergeSchemasWizard} where the user selects the schema
 * elements to merge into the currently open project.
 * The page displays a hierarchical checkbox tree: projects → schemas → attribute-type
 * and object-class folders → individual schema elements.  The user can check at any
 * level; the wizard resolves what that selection means during the actual merge.
 * Think of this page as Luke at the Dagobah cave: a branching tree of choices spreads
 * before him, and he must pick at least one branch before he can proceed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MergeSchemasSelectionWizardPage extends AbstractWizardPage
{
    /** The selected projects */
    private Project[] selectedProjects = new Project[0];

    // UI Fields
    private CheckboxTreeViewer projectsTreeViewer;


    // ── Luke Steps Up To The Cave Entrance ───────────────────────────────────
    // Luke pauses at the cave entrance: Yoda has already told him what to expect,
    // so Luke sets his mental frame (page title, description, image) before he
    // steps inside.  He knows what he's looking for; he just doesn't know which
    // branch to take yet.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the selection page and sets its title, description, and banner image.
     * The page ID we pass to the superclass uniquely identifies this page within the
     * wizard so Eclipse can manage navigation between pages.
     */
    protected MergeSchemasSelectionWizardPage()
    {
        super( "MergeSchemasSelectionWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "MergeSchemasSelectionWizardPage.ImportSchemasFromProjects" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "MergeSchemasSelectionWizardPage.PleaseSelectElements" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SCHEMAS_IMPORT_WIZARD ) );
    }


    // ── Luke Sees The Full Tree Of Paths ─────────────────────────────────────
    // The cave reveals all the branching paths: top-level projects, schemas within
    // them, folders of attribute types and object classes inside each schema, and
    // individual elements at the leaves.  Luke can tick any node — a whole project,
    // a single schema, or a single attribute type buried three levels deep.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the hierarchical checkbox tree viewer and populates it with all
     * available schema projects, then wires validation to fire on every check-state
     * change.
     * The tree is five levels deep: project → schema → attribute-type folder or
     * object-class folder → individual wrapper objects.  We use inner classes
     * ({@link AttributeTypeFolder}, {@link ObjectClassFolder}, etc.) as tree nodes
     * since the underlying schema model doesn't have folder objects natively.
     *
     * <p>For example — Luke sees all branching paths in the Dagobah cave:</p>
     * <pre>
     *   ▶ Project: rebellion-schemas
     *     ▶ Schema: inetOrgPerson
     *       ▶ Attribute Types
     *         ☐ cn
     *         ☐ mail
     *       ▶ Object Classes
     *         ☐ inetOrgPerson
     * </pre>
     *
     * @param parent  the parent composite provided by the wizard framework.
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );

        // Projects TreeViewer
        Label projectsLabel = new Label( composite, SWT.NONE );
        projectsLabel.setText( Messages.getString( "MergeSchemasSelectionWizardPage.SelectElements" ) ); //$NON-NLS-1$
        projectsLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 1, 1 ) );
        projectsTreeViewer = new CheckboxTreeViewer( new Tree( composite, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION ) );
        GridData projectsTableViewerGridData = new GridData( SWT.FILL, SWT.FILL, true, true, 1, 1 );
        projectsTableViewerGridData.widthHint = 450;
        projectsTableViewerGridData.heightHint = 250;
        projectsTreeViewer.getTree().setLayoutData( projectsTableViewerGridData );
        projectsTreeViewer.setContentProvider( new ITreeContentProvider()
        {
            public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
            {
            }


            public void dispose()
            {
            }


            public Object[] getElements( Object inputElement )
            {
                return getChildren( inputElement );
            }


            public boolean hasChildren( Object element )
            {
                return getChildren( element ).length > 0;
            }


            public Object getParent( Object element )
            {
                return null;
            }


            public Object[] getChildren( Object parentElement )
            {
                if ( parentElement instanceof List<?> )
                {
                    return ( ( List<?> ) parentElement ).toArray();
                }
                if ( parentElement instanceof Project )
                {
                    Project project = ( Project ) parentElement;
                    List<Schema> schemas = project.getSchemaHandler().getSchemas();
                    return schemas.toArray();
                }
                if ( parentElement instanceof Schema )
                {
                    Schema schema = ( Schema ) parentElement;
                    Object[] children = new Object[]
                        { new AttributeTypeFolder( schema ), new ObjectClassFolder( schema ) };
                    return children;
                }
                if ( parentElement instanceof AttributeTypeFolder )
                {
                    AttributeTypeFolder folder = ( AttributeTypeFolder ) parentElement;
                    List<AttributeTypeWrapper> attributeTypeWrappers = new ArrayList<AttributeTypeWrapper>();
                    for ( AttributeType attributeType : folder.schema.getAttributeTypes() )
                    {
                        attributeTypeWrappers.add( new AttributeTypeWrapper( attributeType, folder ) );
                    }
                    return attributeTypeWrappers.toArray();
                }
                if ( parentElement instanceof ObjectClassFolder )
                {
                    ObjectClassFolder folder = ( ObjectClassFolder ) parentElement;
                    List<ObjectClassWrapper> objectClassWrappers = new ArrayList<ObjectClassWrapper>();
                    for ( ObjectClass objectClass : folder.schema.getObjectClasses() )
                    {
                        objectClassWrappers.add( new ObjectClassWrapper( objectClass, folder ) );
                    }
                    return objectClassWrappers.toArray();
                }

                return new Object[0];
            }
        } );
        projectsTreeViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                if ( element instanceof Project )
                {
                    return ( ( Project ) element ).getName();
                }
                else if ( element instanceof Schema )
                {
                    return ( ( Schema ) element ).getSchemaName();
                }
                else if ( element instanceof ObjectClassFolder )
                {
                    return Messages.getString( "MergeSchemasSelectionWizardPage.ObjectClasses" ); //$NON-NLS-1$
                }
                else if ( element instanceof AttributeTypeFolder )
                {
                    return Messages.getString( "MergeSchemasSelectionWizardPage.AttributeTypes" ); //$NON-NLS-1$
                }
                else if ( element instanceof AttributeTypeWrapper )
                {
                    AttributeType at = ( ( AttributeTypeWrapper ) element ).attributeType;
                    List<String> names = at.getNames();
                    if ( ( names != null ) && ( names.size() > 0 ) )
                    {
                        return names.get( 0 );
                    }
                    else
                    {
                        return at.getOid();
                    }
                }
                else if ( element instanceof ObjectClassWrapper )
                {
                    ObjectClass oc = ( ( ObjectClassWrapper ) element ).objectClass;
                    List<String> names = oc.getNames();
                    if ( ( names != null ) && ( names.size() > 0 ) )
                    {
                        return names.get( 0 );
                    }
                    else
                    {
                        return oc.getOid();
                    }
                }

                // Default
                return super.getText( element );
            }


            public Image getImage( Object element )
            {
                if ( element instanceof Project )
                {
                    ProjectType type = ( ( Project ) element ).getType();
                    switch ( type )
                    {
                        case OFFLINE:
                            return Activator.getDefault().getImage( PluginConstants.IMG_PROJECT_OFFLINE_CLOSED );
                        case ONLINE:
                            return Activator.getDefault().getImage( PluginConstants.IMG_PROJECT_ONLINE_CLOSED );
                    }
                }
                else if ( element instanceof Schema )
                {
                    return Activator.getDefault().getImage( PluginConstants.IMG_SCHEMA );
                }
                else if ( element instanceof ObjectClassFolder )
                {
                    return Activator.getDefault().getImage( PluginConstants.IMG_FOLDER_OC );
                }
                else if ( element instanceof AttributeTypeFolder )
                {
                    return Activator.getDefault().getImage( PluginConstants.IMG_FOLDER_AT );
                }
                else if ( element instanceof AttributeTypeWrapper )
                {
                    return Activator.getDefault().getImage( PluginConstants.IMG_ATTRIBUTE_TYPE );
                }
                else if ( element instanceof ObjectClassWrapper )
                {
                    return Activator.getDefault().getImage( PluginConstants.IMG_OBJECT_CLASS );
                }

                // Default
                return super.getImage( element );
            }
        } );
        projectsTreeViewer.setSorter( new ViewerSorter() );
        projectsTreeViewer.addCheckStateListener( new ICheckStateListener()
        {
            /**
             * Notifies of a change to the checked state of an element.
             *
             * @param event
             *      event object describing the change
             */
            public void checkStateChanged( CheckStateChangedEvent event )
            {
                dialogChanged();
            }
        } );

        initFields();

        setControl( composite );
    }


    // ── Luke Takes Stock Of The Cave Before Stepping In ──────────────────────
    // Before Luke chooses a path, he surveys the full landscape: all projects
    // sorted alphabetically so they're easy to scan, and any previously chosen
    // projects already ticked so he doesn't lose his bearings if he goes back
    // a step.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates the tree with all known projects (sorted alphabetically) and
     * restores any pre-selected projects from {@link #selectedProjects}.
     * Also resets the error message and marks the page as incomplete until the user
     * actually checks something.
     */
    private void initFields()
    {
        // Filling the Schemas table
        List<Project> projects = new ArrayList<Project>();
        projects.addAll( Activator.getDefault().getProjectsHandler().getProjects() );
        Collections.sort( projects, new Comparator<Project>()
        {
            public int compare( Project o1, Project o2 )
            {
                return o1.getName().compareToIgnoreCase( o2.getName() );
            }

        } );
        projectsTreeViewer.setInput( projects );

        // Setting the selected projects
        projectsTreeViewer.setCheckedElements( selectedProjects );

        displayErrorMessage( null );
        setPageComplete( false );
    }


    // ── Luke Checks Whether He's Actually Chosen A Path ──────────────────────
    // Luke can't walk two paths at once, but he does need to choose at least one.
    // If nothing is checked, Yoda quietly reminds him: "Choose, you must."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Validates that at least one element is checked in the tree, and updates the
     * page's error message and completion state accordingly.
     * Called every time the user toggles a checkbox so the Next/Finish buttons
     * stay in sync.
     */
    private void dialogChanged()
    {
        // Schemas table
        if ( projectsTreeViewer.getCheckedElements().length == 0 )
        {
            displayErrorMessage( Messages.getString( "MergeSchemasSelectionWizardPage.ErrorNoElementsSelected" ) ); //$NON-NLS-1$
            return;
        }

        displayErrorMessage( null );
    }


    // ── Luke Gathers Everything He's Chosen From The Cave ────────────────────
    // Luke exits the cave carrying every element he chose to confront: projects,
    // schemas, folders, individual types.  The wizard takes this raw list and
    // figures out what to actually merge.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full set of checked tree nodes as a raw object array.
     * The wizard's {@code performFinish()} will cast and dispatch each element
     * by type ({@link Project}, {@link Schema}, {@link AttributeTypeFolder}, etc.)
     * to perform the correct merge action.
     *
     * @return  a non-null array of all currently checked tree elements.
     */
    public Object[] getSelectedObjects()
    {
        Object[] selectedObjects = projectsTreeViewer.getCheckedElements();
        return selectedObjects;
    }


    // ── Yoda Pre-Selects The Path For Luke ───────────────────────────────────
    // Before Luke enters the cave, Yoda can pre-place markers on the paths he
    // thinks Luke should explore — for instance, when the wizard is opened with
    // a selection already active in the projects view.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Pre-selects specific projects in the tree, useful when the wizard is
     * launched with a context selection (e.g., right-clicking a project in the
     * Schema Projects view).
     * Must be called before the page becomes visible; {@link #initFields()} applies
     * these to the viewer when it runs.
     *
     * @param projects  the {@link Project} array to pre-check; may be empty but
     *                  must not be null.
     */
    public void setSelectedProjects( Project[] projects )
    {
        selectedProjects = projects;
    }

    // ── CLASS: ObjectClassFolder — A Fork In The Cave Labeled "Object Classes" ─
    // One of the two signposted paths inside the cave: this one leads to all object
    // class definitions within a given schema.  It's a virtual folder node — the
    // schema model has no such class, so we create this lightweight wrapper to give
    // the tree viewer something to expand.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * A virtual tree node representing the "Object Classes" folder within a schema.
     * We need this because the tree viewer expects concrete Java objects as nodes,
     * but the schema model has no folder concept — this wrapper bridges that gap.
     */
    class ObjectClassFolder
    {
        Schema schema;


        // ── Luke Marks The Object-Class Fork ─────────────────────────────────
        // Luke plants a marker at the fork labeled "Object Classes" so he can
        // navigate back to this branch of the cave when needed.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Creates the folder node for the given schema.
         *
         * @param schema  the schema whose object classes this folder node represents.
         */
        public ObjectClassFolder( Schema schema )
        {
            this.schema = schema;
        }
    }

    // ── CLASS: AttributeTypeFolder — A Fork In The Cave Labeled "Attribute Types" ─
    // The other signposted fork: this path leads to all attribute type definitions
    // within a schema.  Same virtual-folder pattern as {@link ObjectClassFolder}.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * A virtual tree node representing the "Attribute Types" folder within a schema.
     * Mirrors {@link ObjectClassFolder} — exists purely to give the tree viewer a
     * concrete object to expand into individual attribute type entries.
     */
    class AttributeTypeFolder
    {
        Schema schema;


        // ── Luke Marks The Attribute-Type Fork ───────────────────────────────
        // Luke plants a marker at the fork labeled "Attribute Types" inside the schema
        // branch — one more path to explore.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Creates the folder node for the given schema.
         *
         * @param schema  the schema whose attribute types this folder node represents.
         */
        public AttributeTypeFolder( Schema schema )
        {
            this.schema = schema;
        }
    }

    // ── CLASS: ObjectClassWrapper — A Specific Vision Luke Sees At A Leaf ────
    // At the end of the object-class path, Luke encounters a specific vision —
    // a concrete object class definition.  This wrapper pairs it with the folder
    // that led here so the wizard can trace back which schema it belongs to.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * A leaf tree node wrapping a single {@link ObjectClass} instance and its
     * containing {@link ObjectClassFolder}.
     * We need the folder reference so the merge logic can determine which target
     * schema to put this object class into.
     */
    class ObjectClassWrapper
    {
        ObjectClass objectClass;
        ObjectClassFolder folder;


        // ── Luke Faces A Specific Vision ─────────────────────────────────────
        // Luke encounters a specific figure at the end of the path — an object
        // class, clearly named, clearly belonging to the folder above.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Creates a leaf node for the given object class within the given folder.
         *
         * @param objectClass  the schema object class this node represents.
         * @param folder       the {@link ObjectClassFolder} this node lives under;
         *                     used to resolve the parent schema during merging.
         */
        public ObjectClassWrapper( ObjectClass objectClass, ObjectClassFolder folder )
        {
            this.objectClass = objectClass;
            this.folder = folder;
        }
    }

    // ── CLASS: AttributeTypeWrapper — A Specific Vision At The Attribute Leaf ─
    // At the end of the attribute-type path, Luke faces a specific attribute type
    // definition.  The wrapper keeps a reference to its folder so the merge logic
    // knows which schema it came from.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * A leaf tree node wrapping a single {@link AttributeType} instance and its
     * containing {@link AttributeTypeFolder}.
     * Mirrors {@link ObjectClassWrapper} — the folder reference lets the merge
     * wizard resolve the source schema for this attribute type.
     */
    class AttributeTypeWrapper
    {
        AttributeType attributeType;
        AttributeTypeFolder folder;


        // ── Luke Faces A Specific Attribute Vision ───────────────────────────
        // Luke faces the attribute type at the end of its path — clearly itself,
        // clearly connected to the folder and schema above it.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Creates a leaf node for the given attribute type within the given folder.
         *
         * @param attributeType  the schema attribute type this node represents.
         * @param folder         the {@link AttributeTypeFolder} this node lives under;
         *                       used to resolve the parent schema during merging.
         */
        public AttributeTypeWrapper( AttributeType attributeType, AttributeTypeFolder folder )
        {
            this.attributeType = attributeType;
            this.folder = folder;
        }
    }
}
