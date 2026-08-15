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


import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.view.dialogs.RenameAttributeTypeDialog;
import org.apache.directory.studio.schemaeditor.view.dialogs.RenameObjectClassDialog;
import org.apache.directory.studio.schemaeditor.view.dialogs.RenameSchemaDialog;
import org.apache.directory.studio.schemaeditor.view.editors.EditorsUtils;
import org.apache.directory.studio.schemaeditor.view.wrappers.AttributeTypeWrapper;
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


// ── CLASS: RenameSchemaElementAction — Clone Troopers Reassigning Imperial Records ──────────
// Order 66 doesn't just mean "eliminate" — it also means updating the Imperial registry
// with the new official designation after the fact. A clone trooper receives the command,
// confirms exactly one target is selected, then issues the rename through the proper channels.
// We save dirty editors first so no stale data conflicts with the rename.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * An action that opens a rename dialog for whichever schema element (schema, attribute type,
 * or object class) is currently selected in the tree viewer.
 * It's the single entry point for all "rename" operations in the schema editor, routing
 * to the appropriate dialog based on the type of the selected element.
 * Think of this class as a clone trooper that receives an Order 66 variant: confirm the
 * target, open the rename channel, and commit the updated designation to the Imperial registry.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RenameSchemaElementAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated viewer */
    private TreeViewer viewer;


    // ── Trooper Receives Rename Orders, Locks On Target ──────────────────────────────────────
    // A clone trooper stands at attention as Order 66 reaches his comlink — but this variant
    // says "rename, don't remove."  He wires up a selection listener so the action stays
    // disabled until exactly one renameable element is highlighted in the roster.
    // "One target, Commander. Standing by."
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of RenameSchemaElementAction, wired to the given tree viewer.
     * We register a selection listener immediately so the action's enabled state tracks
     * the selection — only single selections of a schema, attribute type, or object class
     * will enable the action.
     *
     * <p>For example — a trooper monitors the target roster:</p>
     * <pre>
     *   Trooper: "Order received. Rename protocol active."
     *   Selection changes → trooper checks: exactly one renameable target?
     *   If yes: "Ready to rename, sir." (action enabled)
     *   If no: "No valid target. Standing down." (action disabled)
     * </pre>
     *
     * @param viewer  the tree viewer whose selection determines which element gets renamed
     */
    public RenameSchemaElementAction( TreeViewer viewer )
    {
        super( Messages.getString( "RenameSchemaElementAction.RenameSchemaElementAction" ) ); //$NON-NLS-1$
        setToolTipText( getText() );
        setId( PluginConstants.CMD_RENAME_SCHEMA_ELEMENT );
        setActionDefinitionId( PluginConstants.CMD_RENAME_SCHEMA_ELEMENT );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_RENAME ) );
        setEnabled( false );
        this.viewer = viewer;
        this.viewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                StructuredSelection selection = ( StructuredSelection ) event.getSelection();
                setEnabled( ( selection.size() == 1 )
                    && ( ( selection.getFirstElement() instanceof SchemaWrapper )
                        || ( selection.getFirstElement() instanceof AttributeTypeWrapper )
                        || ( selection.getFirstElement() instanceof ObjectClassWrapper ) ) );
            }
        } );
    }


    // ── Trooper Executes Rename On Confirmed Target ───────────────────────────────────────────
    // The trooper strides toward the confirmed target, datapad in hand.  First he ensures
    // all dirty editors are saved — no half-written Imperial memos allowed before the
    // official rename is filed.  Then he routes to the correct rename dialog based on
    // whether the target is a schema, attribute type, or object class.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the rename — saves dirty editors, then opens the appropriate rename dialog
     * depending on whether the selected element is a schema, attribute type, or object class.
     * If the user confirms the dialog, we push the new name into the schema handler.
     * Nothing happens if the selection is empty or multi-element.
     *
     * <p>For example — the trooper files the rename across three possible registries:</p>
     * <pre>
     *   Target is a Schema    → open RenameSchemaDialog, update schemaHandler
     *   Target is AttributeType → open RenameAttributeTypeDialog, clone + modify
     *   Target is ObjectClass → open RenameObjectClassDialog, clone + modify
     * </pre>
     */
    public void run()
    {
        StructuredSelection selection = ( StructuredSelection ) viewer.getSelection();
        if ( ( !selection.isEmpty() ) && ( selection.size() == 1 ) )
        {
            Object selectedElement = selection.getFirstElement();

            // Saving all dirty editors before processing the renaming
            if ( EditorsUtils.saveAllDirtyEditors() )
            {
                // SCHEMA
                if ( selectedElement instanceof SchemaWrapper )
                {
                    Schema schema = ( ( SchemaWrapper ) selectedElement ).getSchema();

                    RenameSchemaDialog dialog = new RenameSchemaDialog( schema.getSchemaName() );
                    if ( dialog.open() == RenameSchemaDialog.OK )
                    {
                        Activator.getDefault().getSchemaHandler().renameSchema( schema, dialog.getNewName() );
                    }
                }
                // ATTRIBUTE TYPE
                else if ( selectedElement instanceof AttributeTypeWrapper )
                {
                    AttributeType attributeType = ( ( AttributeTypeWrapper ) selectedElement ).getAttributeType();

                    RenameAttributeTypeDialog dialog = new RenameAttributeTypeDialog( attributeType.getNames() );
                    if ( dialog.open() == RenameAttributeTypeDialog.OK )
                    {
                        AttributeType modifiedAttributeType = PluginUtils.getClone( attributeType );
                        modifiedAttributeType.setNames( dialog.getAliases() );
                        Activator.getDefault().getSchemaHandler()
                            .modifyAttributeType( attributeType, modifiedAttributeType );
                    }
                }
                // OBJECT CLASS
                else if ( selectedElement instanceof ObjectClassWrapper )
                {
                    ObjectClass objectClass = ( ( ObjectClassWrapper ) selectedElement ).getObjectClass();

                    RenameObjectClassDialog dialog = new RenameObjectClassDialog( objectClass.getNames() );
                    if ( dialog.open() == RenameObjectClassDialog.OK )
                    {
                        ObjectClass modifiedObjectClass = PluginUtils.getClone( objectClass );
                        modifiedObjectClass.setNames( dialog.getAliases() );
                        Activator.getDefault().getSchemaHandler()
                            .modifyObjectClass( objectClass, modifiedObjectClass );
                    }
                }
            }
        }
    }


    // ── Trooper Relays Command Through Workbench Delegate ─────────────────────────────────────
    // When Eclipse routes the action through the IWorkbenchWindowActionDelegate path,
    // the trooper simply passes the relay baton to the main run() method — same order, same result.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} when this action is invoked via the workbench action delegate path.
     * We don't use the {@code action} parameter — it's just Eclipse's wrapper; the real work is in run().
     *
     * @param action  the workbench action proxy; we ignore it and call our own run() directly
     */
    public void run( IAction action )
    {
        run();
    }


    // ── Trooper Stands Down, No Cleanup Required ──────────────────────────────────────────────
    // The mission is complete; the trooper is dismissed.  We hold no resources that need
    // explicit disposal, so the method body stays empty — a clean stand-down.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes this action's resources when the workbench is done with it.
     * We don't allocate anything that needs explicit cleanup, so this is intentionally empty.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Trooper Reports In But Has Nothing To Set Up ──────────────────────────────────────────
    // The trooper acknowledges the window assignment but needs no briefing — all context
    // is already wired through the constructor.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action is bound to a workbench window.
     * We get everything we need from the constructor, so there's nothing to initialise here.
     *
     * @param window  the workbench window this action is associated with; unused
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── Trooper Ignores Workbench Selection Signals ───────────────────────────────────────────
    // The trooper already has a selection listener wired directly to the TreeViewer, so the
    // workbench-level selection signal is redundant — he keeps his own watch.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench selection changes.
     * We handle selection changes via our own listener registered in the constructor, so
     * this workbench-level callback is intentionally unused.
     *
     * @param action     the workbench action proxy; unused
     * @param selection  the current workbench selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
