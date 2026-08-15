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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.view.wrappers.AttributeTypeWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.ObjectClassWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.SchemaWrapper;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


// ── CLASS: DeleteSchemaElementAction — Clone Troopers Clear The Archive ──────
// After Order 66, clone troopers sweep through the Jedi Archives and remove
// specific records — holochrons, lineage files, individual scrolls — systematically
// and with care not to double-delete things already purged as part of a larger set.
// This action mirrors that precision: it deletes selected schemas, attribute types,
// and object classes from the schema handler, making sure items inside a deleted
// schema aren't redundantly removed a second time.
// ────────────────────────────────────────────────────────────────────────────
/**
 * Deletes selected schemas, attribute types, or object classes from the schema view.
 * We handle mixed selections carefully: if a whole schema and some of its child
 * elements are both selected, we remove the schema once and skip redundant child
 * deletes, because removing the schema already takes care of them.
 * Think of this as clone troopers clearing the Jedi Archive: they collect the full
 * list, deduplicate, and remove each record exactly once.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DeleteSchemaElementAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated viewer */
    private TreeViewer viewer;


    // ── Troopers Study The Archive Floor Plan ─────────────────────────────────
    // The clone squad is briefed on the Archive layout — which shelves hold
    // holochrons, which hold lineage scrolls, and which are whole collections.
    // They'll only accept a mission targeting items they're trained to handle.
    // We register a selection listener that enables us only when every selected
    // item is a Schema, AttributeType, or ObjectClass wrapper — no random nodes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new DeleteSchemaElementAction wired to the given tree viewer.
     * We attach a selection listener that enables the action only when every
     * selected item is a type we know how to delete (Schema, AttributeType,
     * or ObjectClass wrapper); anything else disables us.
     *
     * @param viewer  the TreeViewer showing schema elements; we watch its selection
     */
    public DeleteSchemaElementAction( TreeViewer viewer )
    {
        super( Messages.getString( "DeleteSchemaElementAction.DeleteAction" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "DeleteSchemaElementAction.DeleteToolTip" ) ); //$NON-NLS-1$
        setId( PluginConstants.CMD_DELETE_SCHEMA_ELEMENT );
        setActionDefinitionId( PluginConstants.CMD_DELETE_SCHEMA_ELEMENT );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_DELETE ) );
        setEnabled( true );
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


    // ── Troopers Execute: Confirm, Deduplicate, Remove ────────────────────────
    // The squad confirms the target list with the archivist, then works through
    // it methodically: whole collections first, then individual scrolls — but
    // never twice for the same item.
    // We sort selected items into schemas and individual schema objects, show a
    // confirmation dialog, remove the standalone objects (skipping those that
    // belong to a to-be-deleted schema), then remove the schemas themselves.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Confirms with the user, then deletes the selected schema elements.
     * The confirmation message varies by type and count; for single items we name
     * the type, for multiple items we give the count.
     * We avoid double-deleting schema objects that belong to a schema also being
     * deleted — the schema removal takes care of its own children.
     */
    public void run()
    {
        StructuredSelection selection = ( StructuredSelection ) viewer.getSelection();

        if ( !selection.isEmpty() )
        {
            StringBuilder message = new StringBuilder();

            int count = selection.size();

            if ( count == 1 )
            {
                Object firstElement = selection.getFirstElement();
                if ( firstElement instanceof AttributeTypeWrapper )
                {
                    message.append( Messages.getString( "DeleteSchemaElementAction.SureDeleteAttributeType" ) ); //$NON-NLS-1$
                }
                else if ( firstElement instanceof ObjectClassWrapper )
                {
                    message.append( Messages.getString( "DeleteSchemaElementAction.SureDeleteObjectClass" ) ); //$NON-NLS-1$
                }
                else if ( firstElement instanceof SchemaWrapper )
                {
                    message.append( Messages.getString( "DeleteSchemaElementAction.SureDeleteSchema" ) ); //$NON-NLS-1$
                }
                else
                {
                    message.append( Messages.getString( "DeleteSchemaElementAction.SureDeleteItem" ) ); //$NON-NLS-1$
                }
            }
            else
            {
                message.append( NLS.bind(
                    Messages.getString( "DeleteSchemaElementAction.SureDeleteItems" ), new Object[] { count } ) ); //$NON-NLS-1$
            }

            // Showing the confirmation window
            if ( MessageDialog.openConfirm( viewer.getControl().getShell(),
                Messages.getString( "DeleteSchemaElementAction.DeleteTitle" ), message.toString() ) ) //$NON-NLS-1$
            {
                Map<String, Schema> schemasMap = new HashMap<String, Schema>();
                List<SchemaObject> schemaObjectsList = new ArrayList<SchemaObject>();

                for ( Iterator<?> iterator = selection.iterator(); iterator.hasNext(); )
                {
                    Object selectedItem = iterator.next();
                    if ( selectedItem instanceof SchemaWrapper )
                    {
                        Schema schema = ( ( SchemaWrapper ) selectedItem ).getSchema();
                        schemasMap.put( Strings.toLowerCase( schema.getSchemaName() ), schema );
                    }
                    else if ( selectedItem instanceof AttributeTypeWrapper )
                    {
                        AttributeType at = ( ( AttributeTypeWrapper ) selectedItem ).getAttributeType();
                        schemaObjectsList.add( at );
                    }
                    else if ( selectedItem instanceof ObjectClassWrapper )
                    {
                        ObjectClass oc = ( ( ObjectClassWrapper ) selectedItem ).getObjectClass();
                        schemaObjectsList.add( oc );
                    }
                }

                SchemaHandler schemaHandler = Activator.getDefault().getSchemaHandler();
                // Removing schema objects
                for ( SchemaObject schemaObject : schemaObjectsList )
                {
                    if ( !schemasMap.containsKey( Strings.toLowerCase( schemaObject.getSchemaName() ) ) )
                    {
                        // If the schema object is not part of deleted schema, we need to delete it.
                        // But, we don't delete schema objects that are part of a deleted schema, since
                        // deleting the schema will also delete this schema object.
                        if ( schemaObject instanceof AttributeType )
                        {
                            schemaHandler.removeAttributeType( ( AttributeType ) schemaObject );
                        }
                        else if ( schemaObject instanceof ObjectClass )
                        {
                            schemaHandler.removeObjectClass( ( ObjectClass ) schemaObject );
                        }
                    }
                }

                // Removing schemas
                for ( Schema schema : schemasMap.values() )
                {
                    schemaHandler.removeSchema( schema );
                }
            }
        }
    }


    // ── Order Relayed Through Imperial Comms ──────────────────────────────────
    // The squad's orders arrive through the standard Imperial relay — same mission,
    // same execution, regardless of which comm channel carried the signal.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} when called via the workbench action delegate channel.
     *
     * @param action  the workbench action proxy; we ignore it and call our own run()
     */
    public void run( IAction action )
    {
        run();
    }


    // ── Archive Cleared: Nothing Left To Release ──────────────────────────────
    // The squad withdraws from the Archive — mission done, no equipment left behind.
    // We hold no resources that need cleanup.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op dispose — we hold no resources that need explicit cleanup.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Squad Reports In: No Window-Specific Orders ───────────────────────────
    // The squad leader checks in at the command post but receives no instructions
    // tied to this particular workbench window — standing orders suffice.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op init — we don't need the workbench window reference.
     *
     * @param window  the workbench window; not used here
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── Comms Idle: Workbench Selection Ignored ────────────────────────────────
    // The squad's own viewer listener already handles selection updates; the
    // workbench-level selection callback carries nothing useful for us here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op — we track selection through our own viewer listener, not this callback.
     *
     * @param action     the workbench action proxy; not used
     * @param selection  the workbench selection; not used
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
