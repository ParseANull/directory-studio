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

import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.view.editors.NonExistingObjectClass;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: ObjectClassEditorSuperiorsTableContentProvider — LANDO RUNNING CLOUD CITY ──
// Lando Calrissian manages Cloud City — a complex web of tenants, suppliers,
// and workers. Some of them are right there in the city, fully registered;
// others are references to parties whose records can't be found in the system.
// Lando still keeps a tidy, sorted list of everyone involved.
// This class manages the Superiors list in the same spirit: given a list of
// parent object class names or OIDs, it resolves each to a real {@link ObjectClass}
// (or a {@link NonExistingObjectClass} stub), sorts them, and hands the list
// to the table so the user sees who this class inherits from.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Content provider for the Superiors table in the {@link ObjectClassEditorOverviewPage}.
 * It receives a {@code List<String>} of superior object class names or OIDs, resolves
 * each against the live schema via {@link SchemaHandler}, and returns a sorted array of
 * {@link ObjectClass} (or {@link NonExistingObjectClass} for unresolvable names).
 * Think of it as Lando's Cloud City ledger: raw contract IDs in, resolved tenants out,
 * sorted alphabetically and ready for inspection.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ObjectClassEditorSuperiorsTableContentProvider implements IStructuredContentProvider
{
    /** The SchemaHandler */
    private SchemaHandler schemaHandler;


    // ── Lando Opens the Cloud City Ledger ─────────────────────────────────────
    // Lando pulls out his ledger and connects to Cloud City's central registry
    // so every subsequent lookup can be answered quickly.
    // We grab the schema handler singleton here so we can resolve parent
    // object class names during every call to {@link #getElements}.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new content provider and connects to the schema handler.
     * The schema handler is the central registry of all schema elements in the session;
     * we use it to look up {@link ObjectClass} objects by name or OID.
     */
    public ObjectClassEditorSuperiorsTableContentProvider()
    {
        schemaHandler = Activator.getDefault().getSchemaHandler();
    }


    // ── Lando Resolves Each Party on the List ─────────────────────────────────
    // Lando reviews each name on his supplier list: for known parties he pulls
    // their full record; for unknown references he creates a "missing party"
    // placeholder and keeps it in the list so nothing silently disappears.
    // Then he sorts everyone alphabetically for easy browsing.
    // We do the same with superior object class OID strings: resolve or stub,
    // sort, and return the array the table viewer expects.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Resolves a {@code List<String>} of superior object class names/OIDs into schema objects.
     * For each name, we ask the schema handler for the matching {@link ObjectClass}.
     * If it can't be found — perhaps it lives in a schema we don't currently have loaded —
     * we substitute a {@link NonExistingObjectClass} placeholder so the row still appears.
     * The final result is sorted case-insensitively by the first available name.
     *
     * @param inputElement  expected to be a {@code List<String>} of superior OC names or OIDs
     * @return              a sorted {@code Object[]} of {@link ObjectClass} and/or
     *                      {@link NonExistingObjectClass} instances, or {@code null}
     *                      if the input is not a {@code List}
     */
    @SuppressWarnings("unchecked")
    public Object[] getElements( Object inputElement )
    {
        if ( inputElement instanceof List<?> )
        {
            List<Object> results = new ArrayList<Object>();

            List<String> superiors = ( List<String> ) inputElement;
            for ( String superior : superiors )
            {
                ObjectClass oc = schemaHandler.getObjectClass( superior );
                if ( oc != null )
                {
                    results.add( oc );
                }
                else
                {
                    results.add( new NonExistingObjectClass( superior ) );
                }
            }

            // Sorting Elements
            Collections.sort( results, new Comparator<Object>()
            {
                public int compare( Object o1, Object o2 )
                {
                    if ( o1 instanceof ObjectClass && o2 instanceof ObjectClass )
                    {
                        List<String> oc1Names = ( ( ObjectClass ) o1 ).getNames();
                        List<String> oc2Names = ( ( ObjectClass ) o2 ).getNames();

                        if ( ( oc1Names != null ) && ( oc2Names != null ) && ( oc1Names.size() > 0 )
                            && ( oc2Names.size() > 0 ) )
                        {
                            return oc1Names.get( 0 ).compareToIgnoreCase( oc2Names.get( 0 ) );
                        }
                    }
                    else if ( o1 instanceof ObjectClass && o2 instanceof NonExistingObjectClass )
                    {
                        List<String> oc1Names = ( ( ObjectClass ) o1 ).getNames();
                        String oc2Name = ( ( NonExistingObjectClass ) o2 ).getName();

                        if ( ( oc1Names != null ) && ( oc2Name != null ) && ( oc1Names.size() > 0 ) )
                        {
                            return oc1Names.get( 0 ).compareToIgnoreCase( oc2Name );
                        }
                    }
                    else if ( o1 instanceof NonExistingObjectClass && o2 instanceof ObjectClass )
                    {
                        String oc1Name = ( ( NonExistingObjectClass ) o1 ).getName();
                        List<String> oc2Names = ( ( ObjectClass ) o2 ).getNames();

                        if ( ( oc1Name != null ) && ( oc2Names != null ) && ( oc2Names.size() > 0 ) )
                        {
                            return oc1Name.compareToIgnoreCase( oc2Names.get( 0 ) );
                        }
                    }
                    else if ( o1 instanceof NonExistingObjectClass && o2 instanceof NonExistingObjectClass )
                    {
                        String oc1Name = ( ( NonExistingObjectClass ) o1 ).getName();
                        String oc2Name = ( ( NonExistingObjectClass ) o2 ).getName();

                        if ( ( oc1Name != null ) && ( oc2Name != null ) )
                        {
                            return oc1Name.compareToIgnoreCase( oc2Name );
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


    // ── Lando Closes the Ledger ───────────────────────────────────────────────
    // When Cloud City winds down, Lando puts the ledger away — no lingering
    // resources, no held references. Nothing to clean up here, but we
    // implement the method to satisfy the IStructuredContentProvider contract.
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


    // ── Lando Acknowledges the Ledger Swap ────────────────────────────────────
    // When Cloud City gets a new directory listing, Lando acknowledges the
    // update — but we don't cache any state between calls, so there's nothing
    // to invalidate or rebuild here.
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
