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
package org.apache.directory.studio.schemaeditor.controller.actions;


import java.util.Iterator;

import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.apache.directory.studio.schemaeditor.view.editors.attributetype.AttributeTypeEditor;
import org.apache.directory.studio.schemaeditor.view.editors.attributetype.AttributeTypeEditorInput;
import org.apache.directory.studio.schemaeditor.view.editors.objectclass.ObjectClassEditor;
import org.apache.directory.studio.schemaeditor.view.editors.objectclass.ObjectClassEditorInput;
import org.apache.directory.studio.schemaeditor.view.editors.schema.SchemaEditor;
import org.apache.directory.studio.schemaeditor.view.editors.schema.SchemaEditorInput;
import org.apache.directory.studio.schemaeditor.view.wrappers.AttributeTypeWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.Folder;
import org.apache.directory.studio.schemaeditor.view.wrappers.ObjectClassWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.SchemaWrapper;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


// ── CLASS: OpenElementAction — Clone Troopers Execute Order 66 ────────────────
// Commander Cody receives the encrypted hologram from Palpatine: "Execute Order
// 66." The troopers don't hesitate — they identify the target (the selected
// schema element in the tree), assess what kind it is, and act immediately,
// opening the right editor for it.
// Each selected item in the Schema View is a potential target: attribute type,
// object class, schema, or folder — each gets routed to the appropriate editor.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the selected schema element(s) from the Schema View tree into the appropriate editor.
 * Depending on what's selected — an attribute type, object class, schema, or folder — we
 * open the matching editor or expand the node.
 * Think of this as clone troopers executing Order 66: each trooper identifies their
 * specific target type and dispatches it precisely, without hesitation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenElementAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated viewer */
    private TreeViewer viewer;


    // ── Trooper Receives Assignment And Identifies Valid Targets ──────────────────
    // Commander Cody briefs the squad: "Only Jedi are targets — don't touch anyone
    // else." The troopers then monitor the battlefield and toggle their readiness
    // based on whether there's a valid target in their sights.
    // Our constructor wires a selection listener: we're enabled only when every
    // selected item is a schema wrapper, attribute type wrapper, or object class
    // wrapper — anything else (Folders, etc.) disables us.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OpenElementAction tied to the given tree viewer.
     * We attach a selection listener immediately so we can enable/disable ourselves
     * as the user changes what's highlighted in the tree. We start disabled.
     *
     * @param viewer  the Schema View's tree viewer we watch for selection changes
     */
    public OpenElementAction( TreeViewer viewer )
    {
        super( Messages.getString( "OpenElementAction.OpenAction" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "OpenElementAction.OpenToolTip" ) ); //$NON-NLS-1$
        setId( PluginConstants.CMD_OPEN_ELEMENT );
        setActionDefinitionId( PluginConstants.CMD_OPEN_ELEMENT );
        setEnabled( false );
        this.viewer = viewer;
        this.viewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                StructuredSelection selection = ( StructuredSelection ) event.getSelection();

                if ( selection.size() > 0 )
                {
                    boolean enabled = true;

                    for ( Iterator<?> iterator = selection.iterator(); iterator.hasNext(); )
                    {
                        Object selectedItem = iterator.next();

                        if ( !( selectedItem instanceof SchemaWrapper )
                            && !( selectedItem instanceof AttributeTypeWrapper )
                            && !( selectedItem instanceof ObjectClassWrapper ) )
                        {
                            enabled = false;
                            break;
                        }
                    }

                    setEnabled( enabled );
                }
                else
                {
                    setEnabled( false );
                }
            }
        } );
    }


    // ── Order 66 Executed — Each Target Dispatched To Their Fate ─────────────────
    // The clone troopers fan out across the galaxy: some head to the Jedi Temple,
    // some to Utapau, some to Kashyyyk — each trooper knows exactly which target
    // is theirs and strikes without confusion.
    // We iterate the selection and route each item to its specific editor:
    // AttributeTypeWrapper → AttributeTypeEditor, ObjectClassWrapper →
    // ObjectClassEditor, SchemaWrapper → SchemaEditor, Folder → expand in tree.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens each selected schema tree element in its appropriate editor.
     * Attribute types open in the attribute type editor, object classes in the object
     * class editor, schemas in the schema editor, and folders just get expanded.
     * If opening an editor fails we log the error and show the user a dialog.
     */
    public void run()
    {
        StructuredSelection selection = ( StructuredSelection ) viewer.getSelection();
        for ( Iterator<?> iterator = selection.iterator(); iterator.hasNext(); )
        {
            Object selectedItem = iterator.next();
            if ( selectedItem instanceof AttributeTypeWrapper )
            {
                try
                {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
                        new AttributeTypeEditorInput( ( ( AttributeTypeWrapper ) selectedItem ).getAttributeType() ),
                        AttributeTypeEditor.ID );
                }
                catch ( PartInitException e )
                {
                    PluginUtils.logError( Messages.getString( "OpenElementAction.ErrorOpeningEditor" ), e ); //$NON-NLS-1$
                    ViewUtils
                        .displayErrorMessageDialog(
                            Messages.getString( "OpenElementAction.Error" ), Messages.getString( "OpenElementAction.ErrorOpeningEditor" ) ); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            else if ( selectedItem instanceof ObjectClassWrapper )
            {
                try
                {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
                        new ObjectClassEditorInput( ( ( ObjectClassWrapper ) selectedItem ).getObjectClass() ),
                        ObjectClassEditor.ID );
                }
                catch ( PartInitException e )
                {
                    PluginUtils.logError( Messages.getString( "OpenElementAction.ErrorOpeningEditor" ), e ); //$NON-NLS-1$
                    ViewUtils
                        .displayErrorMessageDialog(
                            Messages.getString( "OpenElementAction.Error" ), Messages.getString( "OpenElementAction.ErrorOpeningEditor" ) ); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            else if ( selectedItem instanceof SchemaWrapper )
            {
                try
                {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
                        new SchemaEditorInput( ( ( SchemaWrapper ) selectedItem ).getSchema() ), SchemaEditor.ID );
                }
                catch ( PartInitException e )
                {
                    PluginUtils.logError( Messages.getString( "OpenElementAction.ErrorOpeningEditor" ), e ); //$NON-NLS-1$
                    ViewUtils
                        .displayErrorMessageDialog(
                            Messages.getString( "OpenElementAction.Error" ), Messages.getString( "OpenElementAction.ErrorOpeningEditor" ) ); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            else if ( selectedItem instanceof Folder )
            {
                viewer.setExpandedState( selectedItem, true );
            }
        }
    }


    // ── Commander Relays The Order ────────────────────────────────────────────────
    // Cody retransmits the Emperor's order verbatim down the chain — no
    // modification, just faithful delegation.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} so Eclipse's command framework can invoke us.
     *
     * @param action  the IAction proxy; unused
     */
    public void run( IAction action )
    {
        run();
    }


    // ── Squad Stands Down ─────────────────────────────────────────────────────────
    // After Order 66 is complete, the troops stand down and return to barracks —
    // nothing to clean up in our action either.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Releases any resources held by this action. We hold none, so this is a no-op.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Troops Receive Their Window Assignment ────────────────────────────────────
    // The clone battalion is assigned to Sector 7 of the Senate district — they
    // know their operational window but need no special per-window setup.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when this action is bound to a workbench window. No window-specific
     * initialization needed.
     *
     * @param window  the workbench window; unused
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── Battlefield Changes, Readiness Managed By Listener ───────────────────────
    // The battlefield shifts — but our selection-changed listener on the tree viewer
    // already handles enable/disable, so this IWorkbenchWindowActionDelegate callback
    // has nothing extra to do.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench selection changes. Our tree viewer's own
     * listener handles this for us, so we don't need to act here.
     *
     * @param action     the IAction proxy; unused
     * @param selection  the current selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
