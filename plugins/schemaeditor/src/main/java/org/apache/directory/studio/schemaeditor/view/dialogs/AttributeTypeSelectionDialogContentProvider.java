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

package org.apache.directory.studio.schemaeditor.view.dialogs;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: AttributeTypeSelectionDialogContentProvider — R2-D2 IN THE DEATH STAR ─
// R2-D2 rolls up to the Death Star's computer terminal, jacks in his interface cable,
// and starts pulling files — running a wildcard search through the entire station's
// database in seconds. He filters out the things the Rebellion has already used,
// sorts the results alphabetically, and hands Luke exactly the right set of records.
// We do the same: given a search string (with optional * wildcards), we query the
// schema handler for all attribute types, sort them, filter out any hidden ones,
// and return a clean array for the dialog's table viewer to display.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Content provider for the {@link AttributeTypeSelectionDialog}'s table viewer.
 * JFace calls {@link #getElements} every time the viewer's input changes — that is,
 * every time the user types in the search box. We take the search string, convert it
 * to a wildcard regex, query the schema handler for all known attribute types, sort
 * them by primary name, filter out any hidden types, and return the matching ones.
 * Think of this class as R2-D2 at the Death Star terminal: fast, quiet, and ruthlessly
 * efficient at filtering a huge dataset down to exactly what's needed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeTypeSelectionDialogContentProvider implements IStructuredContentProvider
{
    /** The Schema Pool */
    private SchemaHandler schemaHandler;

    /** The hidden Object Classes */
    private List<AttributeType> hiddenAttributeTypes;


    // ── R2 Plugs Into the Terminal and Pulls the Index ────────────────────────
    // R2 jacks in and immediately grabs a reference to the station's master index
    // (the schema handler). He also memorises which files he's not allowed to return
    // — the hidden attribute types that the caller wants excluded from results.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the content provider, wiring it to the active schema handler and
     * recording the set of attribute types that should never appear in search results.
     * The schema handler is the singleton that owns all loaded attribute types for the
     * current schema project.
     *
     * @param hiddenAttributeTypes  types to exclude from every result set, even if they match the search;
     *                              typically these are already in use elsewhere in the current definition
     */
    public AttributeTypeSelectionDialogContentProvider( List<AttributeType> hiddenAttributeTypes )
    {
        this.hiddenAttributeTypes = hiddenAttributeTypes;
        schemaHandler = Activator.getDefault().getSchemaHandler();
    }


    // ── R2 Runs the Wildcard Query and Returns Matching Records ───────────────
    // R2 takes the search string Leia embedded in her message, converts it from
    // the human-friendly wildcard format (*, ?) into proper regex, then scans
    // every file in the Death Star's attribute-type registry. He sorts the results
    // alphabetically by name so the list is predictable, skips any files already
    // flagged as "in use," and dumps the clean result set back to the caller.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the array of {@link AttributeType} objects that match the given search string,
     * sorted alphabetically by primary name, with hidden types excluded.
     * The search string supports {@code *} (any sequence of non-whitespace) and
     * {@code ?} (a single non-whitespace character) wildcards — we convert those to
     * a case-insensitive regex before matching against both names and OID.
     * An empty search string with the implicit trailing {@code *} appended matches everything.
     *
     * @param inputElement  expected to be a {@code String} search term; anything else returns an empty array
     * @return              a sorted, filtered {@code Object[]} of {@link AttributeType} instances
     */
    public Object[] getElements( Object inputElement )
    {
        if ( inputElement instanceof String )
        {
            ArrayList<AttributeType> results = new ArrayList<AttributeType>();

            String searchText = ( String ) inputElement;

            String searchRegexp;

            searchText += "*"; //$NON-NLS-1$
            searchRegexp = searchText.replaceAll( "\\*", "[\\\\S]*" ); //$NON-NLS-1$ //$NON-NLS-2$
            searchRegexp = searchRegexp.replaceAll( "\\?", "[\\\\S]" ); //$NON-NLS-1$ //$NON-NLS-2$

            Pattern pattern = Pattern.compile( searchRegexp, Pattern.CASE_INSENSITIVE );

            List<AttributeType> atList = schemaHandler.getAttributeTypes();

            // Sorting the list
            Collections.sort( atList, new Comparator<AttributeType>()
            {
                public int compare( AttributeType at1, AttributeType at2 )
                {
                    List<String> at1Names = ( ( AttributeType ) at1 ).getNames();
                    List<String> at2Names = ( ( AttributeType ) at2 ).getNames();

                    if ( ( at1Names == null || at1Names.size() == 0 ) && ( at2Names == null || at2Names.size() == 0 ) )
                    {
                        return 0;
                    }
                    else if ( ( at1Names == null || at1Names.size() == 0 )
                        && ( at2Names != null && at2Names.size() > 0 ) )
                    {
                        return "".compareToIgnoreCase( at2Names.get( 0 ) ); //$NON-NLS-1$
                    }
                    else if ( ( at1Names != null && at1Names.size() > 0 )
                        && ( at2Names == null || at2Names.size() == 0 ) )
                    {
                        return at1Names.get( 0 ).compareToIgnoreCase( "" ); //$NON-NLS-1$
                    }
                    else
                    {
                        return at1Names.get( 0 ).compareToIgnoreCase( at2Names.get( 0 ) );
                    }
                }
            } );

            // Searching for all matching elements
            for ( AttributeType at : atList )
            {
                for ( String name : at.getNames() )
                {
                    Matcher m = pattern.matcher( name );
                    if ( m.matches() )
                    {
                        if ( !hiddenAttributeTypes.contains( at ) )
                        {
                            if ( !results.contains( at ) )
                            {
                                results.add( at );
                            }
                        }
                        break;
                    }
                }
                Matcher m = pattern.matcher( at.getOid() );
                if ( m.matches() )
                {
                    if ( !hiddenAttributeTypes.contains( at ) )
                    {
                        if ( !results.contains( at ) )
                        {
                            results.add( at );
                        }
                    }
                }
            }

            // Returns the results
            return results.toArray();
        }

        // Default
        return new Object[0];
    }


    // ── R2 Disconnects the Interface Cable ────────────────────────────────────
    // When the data retrieval mission is complete, R2 yanks his interface cable
    // out of the terminal and rolls away. There's nothing to clean up on our end —
    // the schema handler is a shared singleton and we don't own it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by JFace when the viewer this provider is attached to is disposed.
     * We hold no resources that need explicit cleanup, so this is intentionally empty.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── R2 Ignores Viewer Swaps ───────────────────────────────────────────────
    // If someone switches out the terminal R2 is plugged into mid-mission, R2
    // doesn't care — his search logic works the same regardless of which viewer
    // is consuming the results. We don't need to react to viewer changes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by JFace when the input to the attached viewer changes from {@code oldInput}
     * to {@code newInput}. We don't need to react here because {@link #getElements} reads
     * the input directly from its parameter every time it's called.
     *
     * @param viewer    the viewer that changed input
     * @param oldInput  the previous input object
     * @param newInput  the new input object
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
        // Nothing to do
    }
}
