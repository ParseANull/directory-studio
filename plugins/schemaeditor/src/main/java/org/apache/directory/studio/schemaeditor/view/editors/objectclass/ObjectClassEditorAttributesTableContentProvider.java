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
package org.apache.directory.studio.schemaeditor.view.editors.objectclass;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.view.editors.NonExistingAttributeType;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: ObjectClassEditorAttributesTableContentProvider — R2-D2 IN THE DEATH STAR ──
// R2-D2 plugs his interface arm into the Death Star's computer network and
// pulls out a list of every corridor, detention block, and tractor beam
// control — resolving raw location codes into actual accessible systems.
// This class does the same: given a list of bare attribute-type name/OID
// strings from an object class definition, it queries the schema handler to
// resolve each one into a real {@link AttributeType} object (or a placeholder
// if the attribute type isn't found), then hands the sorted result to the table.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Content provider for the Mandatory and Optional Attributes tables in the
 * {@link ObjectClassEditorOverviewPage}.
 * It receives a {@code List<String>} of attribute type names or OIDs, resolves each
 * against the live schema via {@link SchemaHandler}, and returns a sorted array of
 * {@link AttributeType} (or {@link NonExistingAttributeType} for unresolvable names).
 * Think of it as R2 pulling the floor plans out of the Death Star mainframe —
 * raw codes in, usable objects out.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ObjectClassEditorAttributesTableContentProvider implements IStructuredContentProvider
{
    /** The Schema Pool */
    private SchemaHandler schemaHandler;


    // ── R2-D2 Plugs Into the Network ──────────────────────────────────────────
    // R2-D2 extends his interface arm and connects to the Death Star mainframe,
    // establishing the link he'll need to answer every subsequent query.
    // We do the same: grab the schema handler singleton so we're ready to
    // resolve any attribute type name the table throws at us.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new content provider and connects to the schema handler.
     * The schema handler is the central registry of all known schema elements
     * (attribute types, object classes, syntaxes, etc.) in the current session.
     * We grab it once here and reuse it for every {@link #getElements} call.
     */
    public ObjectClassEditorAttributesTableContentProvider()
    {
        schemaHandler = Activator.getDefault().getSchemaHandler();
    }


    // ── R2 Decodes the Location Codes and Sorts the Results ───────────────────
    // R2 retrieves a list of corridor IDs from the Death Star database, resolves
    // each ID to an actual room or system, marks any unrecognized codes as unknown,
    // and then sorts the whole list alphabetically so the Rebels can scan it quickly.
    // We do the same: take the raw list of attribute type names/OIDs, resolve each
    // to an AttributeType (or a NonExistingAttributeType stub), sort, and return.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Resolves a {@code List<String>} of attribute type names/OIDs into schema objects.
     * For each name, we ask the schema handler for the matching {@link AttributeType}.
     * If it's not found — maybe it comes from an imported schema we don't have locally —
     * we create a {@link NonExistingAttributeType} placeholder so the table still shows
     * the name rather than silently dropping it.
     * The result is sorted case-insensitively by the first available name.
     *
     * @param inputElement  expected to be a {@code List<String>} of attribute type names or OIDs
     * @return              a sorted {@code Object[]} of {@link AttributeType} and/or
     *                      {@link NonExistingAttributeType} instances, or {@code null}
     *                      if the input is not a {@code List}
     */
    @SuppressWarnings("unchecked")
    public Object[] getElements( Object inputElement )
    {
        if ( inputElement instanceof List<?> )
        {
            List<Object> results = new ArrayList<Object>();

            List<String> attributes = ( List<String> ) inputElement;
            for ( String attribute : attributes )
            {
                AttributeType at = schemaHandler.getAttributeType( attribute );
                if ( at != null )
                {
                    results.add( at );
                }
                else
                {
                    results.add( new NonExistingAttributeType( attribute ) );
                }
            }

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
                    else if ( o1 instanceof AttributeType && o2 instanceof NonExistingAttributeType )
                    {
                        List<String> at1Names = ( ( AttributeType ) o1 ).getNames();
                        String at2Name = ( ( NonExistingAttributeType ) o2 ).getName();

                        if ( ( at1Names != null ) && ( at2Name != null ) && ( at1Names.size() > 0 ) )
                        {
                            return at1Names.get( 0 ).compareToIgnoreCase( at2Name );
                        }
                    }
                    else if ( o1 instanceof NonExistingAttributeType && o2 instanceof AttributeType )
                    {
                        String at1Name = ( ( NonExistingAttributeType ) o1 ).getName();
                        List<String> at2Names = ( ( AttributeType ) o2 ).getNames();

                        if ( ( at1Name != null ) && ( at2Names != null ) && ( at2Names.size() > 0 ) )
                        {
                            return at1Name.compareToIgnoreCase( at2Names.get( 0 ) );
                        }
                    }
                    else if ( o1 instanceof NonExistingAttributeType && o2 instanceof NonExistingAttributeType )
                    {
                        String at1Name = ( ( NonExistingAttributeType ) o1 ).getName();
                        String at2Name = ( ( NonExistingAttributeType ) o2 ).getName();

                        if ( ( at1Name != null ) && ( at2Name != null ) )
                        {
                            return at1Name.compareToIgnoreCase( at2Name );
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


    // ── R2 Disconnects From the Network ───────────────────────────────────────
    // When R2 is done with the Death Star mainframe, he retracts his interface
    // arm cleanly — no dangling connections, no resource leaks.
    // We have nothing to release here, but we implement the method to satisfy
    // the {@link IStructuredContentProvider} contract.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Nothing to release — our schema handler reference is a managed singleton.
     * This method exists to satisfy the {@link IStructuredContentProvider} contract.
     */
    public void dispose()
    {
    }


    // ── R2 Acknowledges a New Data Feed ───────────────────────────────────────
    // When the Death Star swaps out its active data sector, R2 acknowledges the
    // change and reorients to the new feed — but in our case there's nothing to
    // cache or invalidate, so the acknowledgement is a no-op.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Called by the framework when the viewer's input changes.
     * We don't cache any input-specific state, so there's nothing to do here.
     *
     * @param viewer    the table viewer whose input just changed
     * @param oldInput  the previous input object
     * @param newInput  the new input object
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
    }
}
