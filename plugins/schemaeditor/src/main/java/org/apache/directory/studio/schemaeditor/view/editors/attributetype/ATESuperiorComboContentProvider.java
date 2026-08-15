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


import java.util.Collections;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.view.editors.NonExistingAttributeType;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: ATESuperiorComboContentProvider — R2-D2 SEARCHES THE DEATH STAR COMPUTER ─
// R2-D2 plugs into the Death Star's computer terminal and queries the entire facility
// looking for the tractor-beam controls — but he's smart enough to filter the results:
// he doesn't want to hand Luke a map to his own cell.  He scans every node, applies a
// filter, and returns only the useful subset.
// This content provider does the same: it queries the schema handler for all known
// attribute types, but filters out any that are sub-types of the attribute being edited
// (you can't make an attribute type its own ancestor — that would be circular inheritance).
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * JFace IStructuredContentProvider that populates the "Superior Type" combo box on the
 * Attribute Type Editor's Overview page.
 * We pull every attribute type from the schema handler, filter out any that are
 * descendants of the attribute type currently being edited (to prevent circular
 * inheritance), prepend a "(None)" sentinel, sort the result, and return it.
 * Think of R2-D2: he searches the whole database but returns only the safe, relevant
 * entries.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ATESuperiorComboContentProvider implements IStructuredContentProvider
{
    /** The Schema Handler */
    private SchemaHandler schemaHandler;


    // ── R2 Plugs In and Initialises the Connection ──────────────────────────────────
    // R2-D2 locates the nearest computer terminal and establishes a connection to the
    // schema database before anyone asks him to run a query.  We grab the schema handler
    // at construction time so it's ready for the first getElements call.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Grabs a reference to the active schema handler at construction time.
     * We cache it rather than calling {@code Activator.getDefault().getSchemaHandler()}
     * on every {@code getElements} call because the handler reference doesn't change
     * during a single editor session.
     *
     * <p>For example — R2 establishes his connection before the mission starts:</p>
     * <pre>
     *   // ATESuperiorComboContentProvider provider = new ATESuperiorComboContentProvider();
     *   // provider is now ready to query the schema handler for attribute types.
     * </pre>
     */
    public ATESuperiorComboContentProvider()
    {
        schemaHandler = Activator.getDefault().getSchemaHandler();
    }


    // ── R2 Runs the Query and Returns the Filtered Results ───────────────────────────
    // R2 searches the Death Star computer: "give me all attribute types."  But before
    // handing the list to Luke, he strips out anything that is a sub-type of the current
    // attribute type — you can't make an AT its own ancestor.  He adds a "(None)" option
    // at the top, sorts the rest, and returns the filtered set.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the sorted, filtered array of attribute types for the "Superior Type" combo.
     * On the first call (empty children list) we populate the input: a "(None)" sentinel
     * first, then every attribute type from the schema handler that is NOT a sub-type of
     * the attribute type currently being edited.  The sub-type filter prevents the user
     * from creating a circular ancestry chain (AT A → AT B → AT A).
     * Returns an empty array if the input element is not an {@link ATESuperiorComboInput}.
     *
     * <p>For example — R2's filtered database query:</p>
     * <pre>
     *   // Editing "name" AT: all subtypes of "name" (cn, sn, ...) are excluded.
     *   // Only ATs that are not subtypes of "name" appear — no circular loops possible.
     * </pre>
     *
     * @param inputElement  the {@link ATESuperiorComboInput} carrying the attribute type
     *                      being edited and the lazily-populated children list
     * @return              a sorted Object[] of {@link AttributeType} and
     *                      {@link NonExistingAttributeType} instances ready for the combo
     */
    public Object[] getElements( Object inputElement )
    {
        if ( inputElement instanceof ATESuperiorComboInput )
        {
            ATESuperiorComboInput input = ( ATESuperiorComboInput ) inputElement;

            if ( input.getChildren().isEmpty() )
            {
                AttributeType editorAT = input.getAttributeType();

                // Creating the '(None)' item
                input.addChild( new NonExistingAttributeType( NonExistingAttributeType.NONE ) );

                // Creating Children
                List<AttributeType> ats = schemaHandler.getAttributeTypes();
                for ( AttributeType at : ats )
                {
                    if ( !isSubType( at, editorAT ) )
                    {
                        input.addChild( at );
                    }
                }
            }

            // Getting Children
            List<Object> children = input.getChildren();

            // Sorting Children
            Collections.sort( children, new ATESuperiorComboComparator() );

            return children.toArray();
        }

        // Default
        return new Object[0];
    }


    // ── R2 Logs Off the Terminal ─────────────────────────────────────────────────────
    // R2 unplugs from the terminal when the mission is done.  Nothing to clean up here
    // — the schema handler is a shared service; we don't own it.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the viewer is disposed; nothing to release here because
     * the schema handler is a shared singleton that we don't own.
     */
    public void dispose()
    {
    }


    // ── R2 Notes that the Query Parameters Changed ──────────────────────────────────
    // If someone swaps in a different set of query parameters, R2 notes the change
    // but waits for the next explicit query call before doing any work.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the viewer's input changes; no action needed because the
     * next {@link #getElements} call will lazily populate the new input.
     *
     * @param viewer    the ComboViewer whose input changed
     * @param oldInput  the previous ATESuperiorComboInput (now discarded)
     * @param newInput  the new ATESuperiorComboInput (will be populated on demand)
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
    }


    // ── R2 Checks the Lineage Records ───────────────────────────────────────────────
    // R2 has access to the genealogy database: he walks the ancestry chain of AT "at1"
    // and checks whether AT "at2" appears anywhere in that chain.  If he finds it, the
    // answer is "yes, at1 is a sub-type of at2 — don't show it as a valid superior."
    // This is a recursive walk: at1 → at1's sup → sup's sup → ... until we hit a null
    // superior or find at2.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Recursively checks whether {@code at1} is a sub-type (direct or indirect) of
     * {@code at2}.
     * We use this to filter the superior-type combo: an attribute type must not be
     * selectable as its own ancestor because that would create a circular inheritance loop.
     * A type is always considered a sub-type of itself (the base case returns true when
     * at1 equals at2) so the editor's own type is also excluded from the list.
     *
     * <p>For example — R2 traces the ancestry chain:</p>
     * <pre>
     *   // "cn" → sup is "name" → sup is "directoryString" → sup is null → done.
     *   // isSubType("cn", "name")        → true   (cn inherits from name)
     *   // isSubType("cn", "telephoneNumber") → false (no shared ancestry)
     * </pre>
     *
     * @param at1  the candidate sub-type to check — we walk its ancestor chain
     * @param at2  the potential ancestor we are looking for in that chain
     * @return     {@code true} if at1 is at2 or inherits from at2 directly or indirectly,
     *             {@code false} otherwise or if the ancestry chain leads to an unresolvable
     *             OID
     */
    private boolean isSubType( AttributeType at1, AttributeType at2 )
    {
        if ( at1.equals( at2 ) )
        {
            return true;
        }
        else
        {
            String sup = at1.getSuperiorOid();

            if ( sup == null )
            {
                return false;
            }
            else
            {
                AttributeType supAT = schemaHandler.getAttributeType( sup );
                if ( supAT == null )
                {
                    return false;
                }
                else
                {
                    return isSubType( supAT, at2 );
                }
            }
        }
    }
}
