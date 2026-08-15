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


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: ATEUsedByOptionalTableContentProvider — R2-D2 PLUGGING INTO DEATH STAR ────
// R2-D2 runs a second query against the Death Star's computer: "Which areas allow
// optional access with her ID — not required, but permitted?"  He's querying the MAY
// list this time, not the MUST list.  Same scan, same sorting, different filter predicate.
// This content provider does the same: given an attribute type, it scans every object
// class and returns those that list this attribute type in their MAY (optional) list —
// classes that allow but don't require this attribute on their entries.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * JFace IStructuredContentProvider that populates the "Used As Optional Attribute" table
 * on the Attribute Type Editor's "Used By" page.
 * Given an {@link AttributeType} as the input element, we scan every {@link ObjectClass}
 * in the schema and collect those whose MAY list contains this attribute type's name
 * (case-insensitive, just like LDAP itself).  Results are sorted alphabetically.
 * Think of R2-D2: same database query as the mandatory provider, but this time we're
 * looking at the optional-access lists instead of the mandatory ones.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ATEUsedByOptionalTableContentProvider implements IStructuredContentProvider
{
    // ── R2 Runs the Optional-Access Query ────────────────────────────────────────────
    // R2 plugs in and asks: "Which object classes permit this attribute type as an
    // optional field?"  He checks the MAY lists, collects matches, sorts them, returns
    // the array — same pattern as the mandatory query, different list to check.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an alphabetically sorted array of object classes that permit the given
     * attribute type as an optional attribute (i.e. the attribute type appears in their
     * MAY list).
     * We normalise names to lowercase for case-insensitive comparison before checking
     * membership in the MAY list.  Returns null if the input element is not an
     * {@link AttributeType}.
     *
     * <p>For example — R2's optional-access scan:</p>
     * <pre>
     *   // Attribute type: "description"
     *   // Scans all OCs → finds "person" has MAY: [description, ...]
     *   //              → finds "organizationalUnit" also has "description" in MAY
     *   // Returns sorted: [organizationalUnit, person]
     * </pre>
     *
     * @param inputElement  the {@link AttributeType} to search for in MAY lists; any
     *                      other type yields null
     * @return              a sorted Object[] of {@link ObjectClass} instances whose MAY
     *                      list includes this attribute type, or null if input is wrong type
     */
    public Object[] getElements( Object inputElement )
    {
        if ( inputElement instanceof AttributeType )
        {
            List<ObjectClass> results = new ArrayList<ObjectClass>();
            AttributeType inputAT = ( AttributeType ) inputElement;
            SchemaHandler schemaHandler = Activator.getDefault().getSchemaHandler();

            List<String> names = new ArrayList<String>();

            List<String> atNames = inputAT.getNames();
            if ( atNames != null )
            {
                for ( String name : atNames )
                {
                    names.add( Strings.toLowerCase( name ) );
                }
            }

            List<ObjectClass> objectClasses = schemaHandler.getObjectClasses();
            for ( ObjectClass oc : objectClasses )
            {
                List<String> mays = oc.getMayAttributeTypeOids();
                if ( mays != null )
                {
                    for ( String may : mays )
                    {
                        if ( names.contains( Strings.toLowerCase( may ) ) )
                        {
                            results.add( oc );
                        }
                    }
                }
            }

            // Sorting Results
            Collections.sort( results, new Comparator<ObjectClass>()
            {
                public int compare( ObjectClass oc1, ObjectClass oc2 )
                {
                    if ( oc1 instanceof ObjectClass && oc1 instanceof ObjectClass )
                    {
                        List<String> oc1Names = ( ( ObjectClass ) oc1 ).getNames();
                        List<String> oc2Names = ( ( ObjectClass ) oc2 ).getNames();

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


    // ── R2 Disconnects from the Terminal ─────────────────────────────────────────────
    // R2 unplugs when the query session is over — nothing to clean up.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the viewer is disposed; nothing to release here.
     */
    public void dispose()
    {
    }


    // ── R2 Notes That the Query Target Changed ───────────────────────────────────────
    // New attribute type input? R2 notes it but waits for the next explicit query call.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the viewer's input changes; no action needed here — the
     * next {@link #getElements} call will run against the new input.
     *
     * @param viewer    the TableViewer whose input changed
     * @param oldInput  the previous AttributeType
     * @param newInput  the new AttributeType to query on the next getElements call
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
    }
}
