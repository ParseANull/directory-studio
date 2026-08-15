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


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: SchemaEditorTableViewerContentProvider — R2-D2 IN THE DEATH STAR NETWORK ──
// R2-D2 plugs into the Death Star's main computer network and retrieves a
// complete inventory: all systems, all decks, all droids — pulled as a raw list,
// sorted alphabetically so the Rebels can scan the results quickly.
// This class does the same for the Schema Editor: given a list of
// {@link AttributeType} or {@link ObjectClass} objects from a schema, it copies
// the list, sorts it case-insensitively by primary name, and returns the array
// for the table viewer to display.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Content provider for the attribute types and object classes table viewers in the
 * {@link SchemaEditorOverviewPage}.
 * It accepts a {@code List<?>} of {@link AttributeType} or {@link ObjectClass} objects,
 * sorts them alphabetically by their first name (case-insensitive), and returns a
 * sorted {@code Object[]} for the JFace table viewer.
 * Think of it as R2 pulling the full inventory from the Death Star mainframe —
 * raw list in, sorted display array out.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaEditorTableViewerContentProvider implements IStructuredContentProvider
{
    // ── R2 Retrieves and Sorts the Inventory ──────────────────────────────────
    // R2 pulls the full system inventory from the Death Star computer, copies
    // it into his working memory, and sorts the entries by name so the Rebels
    // can find what they need without scanning an unsorted mess.
    // We do the same: copy the input list, sort by primary name, and return
    // the array for the table viewer to render row by row.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Accepts a {@code List<?>} of {@link AttributeType} or {@link ObjectClass} objects,
     * sorts them case-insensitively by their first available name, and returns the
     * sorted array. Items without any name fall to an equal-comparison result of 0
     * and maintain their relative order.
     *
     * @param inputElement  expected to be a {@code List<?>} of schema elements
     * @return              a sorted {@code Object[]} of the same elements, or {@code null}
     *                      if the input is not a {@code List}
     */
    public Object[] getElements( Object inputElement )
    {
        if ( inputElement instanceof List<?> )
        {
            List<?> list = ( List<?> ) inputElement;

            List<Object> results = new ArrayList<Object>();
            results.addAll( list );

            // Sorting Elements
            Collections.sort( results, new Comparator<Object>()
            {
                public int compare( Object o1, Object o2 )
                {
                    if ( o1 instanceof AttributeType && o2 instanceof AttributeType )
                    {
                        List<String> at1Names = ( ( AttributeType ) o1 ).getNames();
                        List<String> at2Names = ( ( AttributeType ) o2 ).getNames();

                        if ( ( at1Names != null ) && ( at2Names != null ) && ( at1Names.size() > 0 )
                            && ( at2Names.size() > 0 ) )
                        {
                            return at1Names.get( 0 ).compareToIgnoreCase( at2Names.get( 0 ) );
                        }
                    }
                    else if ( o1 instanceof ObjectClass && o2 instanceof ObjectClass )
                    {
                        List<String> oc1Names = ( ( ObjectClass ) o1 ).getNames();
                        List<String> oc2Names = ( ( ObjectClass ) o2 ).getNames();

                        if ( ( oc1Names != null ) && ( oc2Names != null ) && ( oc1Names.size() > 0 )
                            && ( oc2Names.size() > 0 ) )
                        {
                            return oc1Names.get( 0 ).compareToIgnoreCase( oc2Names.get( 0 ) );
                        }
                    }

                    return 0;
                }
            } );

            return results.toArray();
        }

        // Default
        return null;
    }


    // ── R2 Retracts His Interface Arm ─────────────────────────────────────────
    // When R2 finishes the inventory pull, he retracts his interface arm from
    // the network socket cleanly — no dangling connections, no lingering state.
    // We have nothing to release here, but the method satisfies the contract.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Nothing to release — this provider holds no stateful resources.
     * This method exists to satisfy the {@link IStructuredContentProvider} contract.
     */
    public void dispose()
    {
    }


    // ── R2 Acknowledges the New Data Feed ─────────────────────────────────────
    // When the Death Star swaps out the active data sector, R2 acknowledges
    // the switch — but since we don't cache any input-specific state, there's
    // nothing to invalidate or reinitialize here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Called by the framework when the viewer's input changes.
     * We cache no input-specific state, so there's nothing to do here.
     *
     * @param viewer    the table viewer whose input just changed
     * @param oldInput  the previous input object
     * @param newInput  the new input object
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
    }
}
