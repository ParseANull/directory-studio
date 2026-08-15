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

package org.apache.directory.studio.connection.ui.dnd;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.widgets.Tree;


// ── CLASS: DragConnectionListener — REBEL SHIPS LIFTING OFF FROM THE HANGAR ──────
// When a Rebel pilot grabs the joystick and lifts the X-Wing off the hangar floor,
// someone on the ground has to record exactly which ships are leaving.  That's this
// class.  The moment the user presses the mouse button on a tree row (dragStart),
// we snapshot the current selection.  When the OS asks "what data are you moving?"
// (dragSetData), we hand over only the Connection and ConnectionFolder objects from
// that snapshot — nothing else gets into the transfer payload.
// dragFinished is called when the drag is over; we have nothing to clean up.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * {@link DragSourceListener} for drag-and-drop operations within the Connections view.
 *
 * <p>On drag start we snapshot the viewer's current selection.  In
 * {@link #dragSetData} we filter that snapshot down to only {@link Connection}
 * and {@link ConnectionFolder} objects, then hand the array to the
 * {@link ConnectionTransfer} for serialisation.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DragConnectionListener implements DragSourceListener
{
    // ── FIELDS ────────────────────────────────────────────────────────────────────

    /** The tree viewer that owns the drag source. */
    private TreeViewer treeViewer;

    /**
     * The selection captured in {@link #dragStart} and consumed in
     * {@link #dragSetData}.  {@code null} until the first drag gesture.
     */
    private StructuredSelection selection = null;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link DragConnectionListener} attached to the given viewer.
     *
     * @param viewer The {@link TreeViewer} whose selection we will serialise during
     *               a drag gesture.
     */
    public DragConnectionListener( TreeViewer viewer )
    {
        treeViewer = viewer;
    }


    // ── DRAG START ────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Snapshots the viewer's current selection.  We set {@code event.doit = false}
     * if the selection is empty to prevent an empty drag gesture from proceeding.</p>
     */
    public void dragStart( DragSourceEvent event )
    {
        // ── SNAPSHOT THE SELECTION ────────────────────────────────────────────────
        // We save a reference here so dragSetData can use it without querying the
        // viewer again (the selection may have changed by then).
        // ──────────────────────────────────────────────────────────────────────────
        selection = ( StructuredSelection ) treeViewer.getSelection();
        event.doit = !selection.isEmpty();
    }


    // ── DRAG SET DATA ─────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Populates {@code event.data} with the array of {@link Connection} and
     * {@link ConnectionFolder} objects from the saved selection.  Only those two
     * types are included; any other selection element is silently skipped.</p>
     *
     * <p>Does nothing if the transfer type is not supported or if the drag source
     * control is not a {@link Tree}.</p>
     */
    public void dragSetData( DragSourceEvent event )
    {
        if ( ConnectionTransfer.getInstance().isSupportedType( event.dataType ) &&
            ( event.widget instanceof DragSource ) )
        {
            DragSource dragSource = ( DragSource ) event.widget;

            if ( dragSource.getControl() instanceof Tree )
            {
                // ── FILTER TO CONNECTION / FOLDER TYPES ONLY ──────────────────────
                // We walk the saved selection and keep only the types our Transfer
                // protocol knows how to serialise.
                // ──────────────────────────────────────────────────────────────────
                List<Object> objectList = new ArrayList<>();

                if ( selection != null )
                {
                    for ( Iterator<?> iterator = selection.iterator(); iterator.hasNext(); )
                    {
                        Object item = iterator.next();

                        if ( ( item instanceof Connection ) || ( item instanceof ConnectionFolder ) )
                        {
                            objectList.add( item );
                        }
                    }
                }

                event.data = objectList.toArray();
            }
        }
    }


    // ── DRAG FINISHED ─────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Nothing to clean up — the actual move/copy is performed by the
     * {@link DropConnectionListener} on the receiving end.</p>
     */
    public void dragFinished( DragSourceEvent event )
    {
        // Nothing to do — the drop listener handles the structural changes.
    }
}
