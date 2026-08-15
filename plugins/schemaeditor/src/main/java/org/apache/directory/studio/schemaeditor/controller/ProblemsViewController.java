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
package org.apache.directory.studio.schemaeditor.controller;


import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.model.schemachecker.SchemaCheckerListener;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.apache.directory.studio.schemaeditor.view.editors.attributetype.AttributeTypeEditor;
import org.apache.directory.studio.schemaeditor.view.editors.attributetype.AttributeTypeEditorInput;
import org.apache.directory.studio.schemaeditor.view.editors.objectclass.ObjectClassEditor;
import org.apache.directory.studio.schemaeditor.view.editors.objectclass.ObjectClassEditorInput;
import org.apache.directory.studio.schemaeditor.view.views.ProblemsView;
import org.apache.directory.studio.schemaeditor.view.widget.Folder;
import org.apache.directory.studio.schemaeditor.view.wrappers.SchemaErrorWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.SchemaWarningWrapper;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ProblemsViewController — MACE WINDU CONFRONTS PALPATINE ──────────
// Mace Windu strides into Palpatine's office, four Masters behind him, and
// demands to inspect what's wrong — he's here specifically to surface the
// threat, not ignore it.  This controller does the same for schema problems:
// it watches the SchemaChecker and surfaces every error and warning in the
// ProblemsView so nothing slips past unnoticed.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Controller for the Problems View in the Schema Editor.
 * We listen to the SchemaChecker and reload the view whenever validation
 * results change, and we wire up double-click navigation so users can jump
 * directly to the offending attribute type or object class.
 * Think of this class as Mace Windu — we confront schema violations head-on
 * and make sure they're visible and actionable.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ProblemsViewController
{
    /** The associated view */
    private ProblemsView view;

    /** The SchemaCheckerListener */
    private SchemaCheckerListener schemaCheckerListener = new SchemaCheckerListener()
    {
        public void schemaCheckerUpdated()
        {
            Display.getDefault().asyncExec( new Runnable()
            {
                public void run()
                {
                    view.reloadViewer();
                }
            } );
        }
    };


    // ── Mace Assembles His Task Force ────────────────────────────────────────
    // Mace doesn't walk into Palpatine's office alone — he lines up three
    // Masters behind him and registers a double-click listener with the Senate.
    // We do the same: attach the SchemaCheckerListener and the double-click
    // handler so the view is fully operational from the first moment it opens.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the controller and wires up all listeners for the ProblemsView.
     * We immediately register with the SchemaChecker so we're notified of any
     * validation changes, and we set up double-click navigation so users can
     * open an editor on the problem's source object.
     *
     * <p>For example — Mace doesn't wait for a second invitation:</p>
     * <pre>
     *   schemaChecker.addListener( schemaCheckerListener );
     *   initDoubleClickListener();
     *   // View is live from this point on
     * </pre>
     *
     * @param view  the ProblemsView we are controlling; must not be null
     */
    public ProblemsViewController( ProblemsView view )
    {
        this.view = view;

        // SchemaCheckerListener
        Activator.getDefault().getSchemaChecker().addListener( schemaCheckerListener );

        initDoubleClickListener();
    }


    // ── Mace Points To Each Violation In Turn ────────────────────────────────
    // Mace gestures at each piece of evidence in Palpatine's office, one by one,
    // demanding that the Chancellor answer for each — double-click is our gesture,
    // and the editor is the answer.
    // When the user double-clicks an error or warning we open the editor on the
    // source schema object so they can fix the problem directly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers the double-click listener on the ProblemsView tree viewer.
     * A double-click on a SchemaErrorWrapper or SchemaWarningWrapper navigates
     * to the editor for the source attribute type or object class.
     * Double-clicking a Folder just toggles its expanded state instead of
     * trying to open an editor (there's nothing to edit for a folder node).
     *
     * <p>For example — Mace pinpoints exactly which rule was broken:</p>
     * <pre>
     *   doubleClick( SchemaErrorWrapper )   → open AttributeTypeEditor or ObjectClassEditor
     *   doubleClick( SchemaWarningWrapper ) → open AttributeTypeEditor or ObjectClassEditor
     *   doubleClick( Folder )               → toggle expand/collapse
     * </pre>
     */
    private void initDoubleClickListener()
    {
        view.getViewer().addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( DoubleClickEvent event )
            {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

                TreeViewer viewer = view.getViewer();

                // What we get from the treeViewer is a StructuredSelection
                StructuredSelection selection = ( StructuredSelection ) event.getSelection();

                // Here's the real object (an AttributeTypeWrapper, ObjectClassWrapper or IntermediateNode)
                Object objectSelection = selection.getFirstElement();
                IEditorInput input = null;
                String editorId = null;

                // Selecting the right editor and input
                if ( objectSelection instanceof SchemaErrorWrapper )
                {
                    SchemaObject object = ( ( SchemaErrorWrapper ) objectSelection ).getLdapSchemaException()
                        .getSourceObject();

                    if ( object instanceof AttributeType )
                    {
                        input = new AttributeTypeEditorInput( Activator.getDefault().getSchemaHandler()
                            .getAttributeType( object.getOid() ) );
                        editorId = AttributeTypeEditor.ID;
                    }
                    else if ( object instanceof ObjectClass )
                    {
                        input = new ObjectClassEditorInput( Activator.getDefault().getSchemaHandler()
                            .getObjectClass( object.getOid() ) );
                        editorId = ObjectClassEditor.ID;
                    }
                }
                else if ( objectSelection instanceof SchemaWarningWrapper )
                {
                    SchemaObject object = ( ( SchemaWarningWrapper ) objectSelection ).getSchemaWarning().getSource();

                    if ( object instanceof AttributeType )
                    {
                        input = new AttributeTypeEditorInput( ( AttributeType ) object );
                        editorId = AttributeTypeEditor.ID;
                    }
                    else if ( object instanceof ObjectClass )
                    {
                        input = new ObjectClassEditorInput( ( ObjectClass ) object );
                        editorId = ObjectClassEditor.ID;
                    }
                }
                else if ( ( objectSelection instanceof Folder ) )
                {
                    // Here we don't open an editor, we just expand the node.
                    viewer.setExpandedState( objectSelection, !viewer.getExpandedState( objectSelection ) );
                }

                // Let's open the editor
                if ( input != null )
                {
                    try
                    {
                        page.openEditor( input, editorId );
                    }
                    catch ( PartInitException e )
                    {
                        PluginUtils.logError( Messages.getString( "ProblemsViewController.ErrorOpeningEditor" ), e ); //$NON-NLS-1$
                        ViewUtils.displayErrorMessageDialog( Messages.getString( "ProblemsViewController.Error" ), //$NON-NLS-1$
                            Messages.getString( "ProblemsViewController.ErrorOpeningEditor" ) ); //$NON-NLS-1$
                    }
                }
            }
        } );
    }


    // ── Mace Stands Down After The Confrontation ──────────────────────────────
    // The confrontation is over — Mace withdraws his lightsaber and dismisses
    // the other Masters; there's nothing left to watch for here.
    // We deregister our SchemaCheckerListener so we stop receiving updates
    // for a view that has already been closed and disposed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes all listeners when the view is disposed.
     * We deregister from the SchemaChecker to stop receiving validation
     * updates for a view that no longer exists — skipping this would cause
     * callbacks to fire on a dead view and likely produce NPEs.
     *
     * <p>For example — Mace doesn't keep standing guard after the threat is resolved:</p>
     * <pre>
     *   schemaChecker.removeListener( schemaCheckerListener );
     * </pre>
     */
    public void dispose()
    {
        // SchemaCheckerListener
        Activator.getDefault().getSchemaChecker().removeListener( schemaCheckerListener );
    }
}
