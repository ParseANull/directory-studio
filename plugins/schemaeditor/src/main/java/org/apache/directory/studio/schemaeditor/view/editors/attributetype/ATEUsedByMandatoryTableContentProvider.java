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


// ── CLASS: ATEUsedByMandatoryTableContentProvider — R2-D2 PLUGGING INTO DEATH STAR ──
// R2-D2 jacks into the Death Star's computer network on Leia's behalf and queries the
// entire facility: "Which rooms require a keycard with her ID to enter?"  He scans
// every door, every security profile, looks for her name in the mandatory-access lists,
// compiles the results, and sorts them so they're easy to present.
// This content provider does exactly that for LDAP schema: given an attribute type, it
// scans every object class in the schema and returns only those that list this attribute
// type in their MUST (mandatory) attribute list — i.e. the classes that require this
// attribute on every entry.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * JFace IStructuredContentProvider that populates the "Used As Mandatory Attribute" table
 * on the Attribute Type Editor's "Used By" page.
 * Given an {@link AttributeType} as the input element, we scan every {@link ObjectClass}
 * in the schema and collect those whose MUST list contains this attribute type's name
 * (matched case-insensitively, because LDAP names are case-insensitive).  The collected
 * classes are sorted alphabetically by first name before being returned.
 * Think of R2-D2: he searches the entire database, applies a precise filter, and hands
 * back a clean, sorted result.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ATEUsedByMandatoryTableContentProvider implements IStructuredContentProvider
{
    // ── R2 Runs the Mandatory-Access Query ──────────────────────────────────────────
    // R2 plugs in and executes the query: "Which object classes have this attribute
    // type in their MUST list?"  He normalises all names to lowercase for comparison,
    // collects every matching class, sorts them, and returns the array.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an alphabetically sorted array of object classes that mandate the given
     * attribute type (i.e. the attribute type appears in their MUST list).
     * We normalise all names to lowercase before comparing so that "cn" matches "CN"
     * and any other capitalisation variant — LDAP is case-insensitive for attribute names.
     * Returns null if the input element isn't an {@link AttributeType} (the JFace
     * convention for "nothing to display").
     *
     * <p>For example — R2's mandatory-access scan:</p>
     * <pre>
     *   // Attribute type: "cn"
     *   // Scans all object classes → finds "person" has MUST: [sn, cn]
     *   //                          → finds "organizationalPerson" also has "cn" in MUST
     *   // Returns sorted: [organizationalPerson, person]
     * </pre>
     *
     * @param inputElement  the {@link AttributeType} to search for in MUST lists; any
     *                      other type yields null
     * @return              a sorted Object[] of {@link ObjectClass} instances whose MUST
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
                List<String> musts = oc.getMustAttributeTypeOids();
                if ( musts != null )
                {
                    for ( String must : musts )
                    {
                        if ( names.contains( Strings.toLowerCase( must ) ) )
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
    // R2 unplugs from the network when done — nothing to clean up, he doesn't hold
    // any resources of his own.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the viewer is disposed; nothing to release here — we don't
     * hold any resources ourselves, just delegate to the schema handler on each call.
     */
    public void dispose()
    {
    }


    // ── R2 Notes That the Query Target Changed ───────────────────────────────────────
    // If the editor swaps in a different attribute type as the query target, R2 notes
    // the change but doesn't re-run the query until explicitly asked.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the viewer's input changes; no action needed — the next
     * {@link #getElements} call will run against the new input automatically.
     *
     * @param viewer    the TableViewer whose input changed
     * @param oldInput  the previous AttributeType (now superseded)
     * @param newInput  the new AttributeType to query against on the next getElements call
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
    }
}
