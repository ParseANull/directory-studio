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

import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: ObjectClassSelectionDialogContentProvider — R2 SEARCHES THE OC DATABASE ─
// R2-D2 jacks back into the Death Star terminal — this time he's after the station's
// object-class registry instead of the attribute-type files. Same technique: wildcard
// regex, alphabetical sort, exclude the already-used classes, return the clean list.
// He's browsing a different section of the archive than last time, but the drill is
// identical: pull everything, filter by pattern, skip the suppressed entries.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Content provider for the {@link ObjectClassSelectionDialog}'s table viewer.
 * JFace calls {@link #getElements} every time the viewer's input changes (i.e., every
 * time the user types in the search box). We convert the search string to a wildcard
 * regex, query the schema handler for all known object classes, sort them by primary
 * name, filter out hidden ones, and return the matching array.
 * Think of this class as R2-D2 running a second archive query — same Death Star
 * terminal, different filing cabinet (object classes instead of attribute types).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ObjectClassSelectionDialogContentProvider implements IStructuredContentProvider
{
    /** The schema handler */
    private SchemaHandler schemaHandler;

    /** The hidden object classes */
    private List<ObjectClass> hiddenObjectClasses;


    // ── R2 Plugs Into the Object-Class Cabinet ───────────────────────────────
    // R2 navigates to the correct section of the archive and grabs a handle to the
    // schema handler that manages all object classes. He also takes note of which
    // classes the caller has flagged as off-limits so he skips them in every result.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the content provider, wiring it to the active schema handler and
     * recording the set of object classes that should never appear in search results.
     *
     * @param hiddenObjectClasses  classes to exclude from every result set, even if they match;
     *                             typically these are already used as superclasses elsewhere
     */
    public ObjectClassSelectionDialogContentProvider( List<ObjectClass> hiddenObjectClasses )
    {
        schemaHandler = Activator.getDefault().getSchemaHandler();
        this.hiddenObjectClasses = hiddenObjectClasses;
    }


    // ── R2 Runs the Query Against the Object-Class Files ─────────────────────
    // R2 converts Leia's wildcard search into a regex, scans every object class in
    // the schema registry, sorts them alphabetically by primary name, skips the
    // suppressed ones, and dumps back a clean sorted result set — matching against
    // both the class's human-readable names and its numeric OID.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the array of {@link ObjectClass} objects that match the given search string,
     * sorted alphabetically by primary name, with hidden classes excluded.
     * The search string supports {@code *} (any sequence of non-whitespace) and
     * {@code ?} (a single non-whitespace character) wildcards converted to a
     * case-insensitive regex. Matching runs against both names and OID.
     * An empty string (with the implicit trailing {@code *}) matches everything.
     *
     * @param inputElement  expected to be a {@code String} search term; anything else yields an empty array
     * @return              a sorted, filtered {@code Object[]} of {@link ObjectClass} instances
     */
    public Object[] getElements( Object inputElement )
    {
        if ( inputElement instanceof String )
        {
            ArrayList<ObjectClass> results = new ArrayList<ObjectClass>();

            String searchText = ( String ) inputElement;

            String searchRegexp;

            searchText += "*"; //$NON-NLS-1$
            searchRegexp = searchText.replaceAll( "\\*", "[\\\\S]*" ); //$NON-NLS-1$ //$NON-NLS-2$
            searchRegexp = searchRegexp.replaceAll( "\\?", "[\\\\S]" ); //$NON-NLS-1$ //$NON-NLS-2$

            Pattern pattern = Pattern.compile( searchRegexp, Pattern.CASE_INSENSITIVE );

            List<ObjectClass> ocList = schemaHandler.getObjectClasses();

            // Sorting the list
            Collections.sort( ocList, new Comparator<ObjectClass>()
            {
                public int compare( ObjectClass oc1, ObjectClass oc2 )
                {
                    List<String> oc1Names = ( ( ObjectClass ) oc1 ).getNames();
                    List<String> oc2Names = ( ( ObjectClass ) oc2 ).getNames();

                    if ( ( oc1Names == null || oc1Names.size() == 0 ) && ( oc2Names == null || oc2Names.size() == 0 ) )
                    {
                        return 0;
                    }
                    else if ( ( oc1Names == null || oc1Names.size() == 0 )
                        && ( oc2Names != null && oc2Names.size() > 0 ) )
                    {
                        return "".compareToIgnoreCase( oc2Names.get( 0 ) ); //$NON-NLS-1$
                    }
                    else if ( ( oc1Names != null && oc1Names.size() > 0 )
                        && ( oc2Names == null || oc2Names.size() == 0 ) )
                    {
                        return oc1Names.get( 0 ).compareToIgnoreCase( "" ); //$NON-NLS-1$
                    }
                    else
                    {
                        return oc1Names.get( 0 ).compareToIgnoreCase( oc2Names.get( 0 ) );
                    }
                }
            } );

            // Searching for all matching elements
            for ( ObjectClass oc : ocList )
            {
                for ( String name : oc.getNames() )
                {
                    Matcher m = pattern.matcher( name );
                    if ( m.matches() )
                    {
                        if ( !hiddenObjectClasses.contains( oc ) )
                        {
                            if ( !results.contains( oc ) )
                            {
                                results.add( oc );
                            }
                        }
                        break;
                    }
                }
                Matcher m = pattern.matcher( oc.getOid() );
                if ( m.matches() )
                {
                    if ( !hiddenObjectClasses.contains( oc ) )
                    {
                        if ( !results.contains( oc ) )
                        {
                            results.add( oc );
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


    // ── R2 Retracts His Interface Cable ──────────────────────────────────────
    // Mission complete — R2 pulls his interface cable free and rolls back toward
    // the Falcon. No cleanup needed on our end; the schema handler is a shared
    // singleton we don't own.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by JFace when the viewer this provider is attached to is disposed.
     * We hold no resources that need explicit cleanup, so this is intentionally empty.
     */
    public void dispose()
    {
    }


    // ── R2 Doesn't Care Which Viewer He Feeds ────────────────────────────────
    // R2 pipes his results to whatever display the crew has available — it could
    // be the Falcon's monitor or a portable datapad. He doesn't need to know
    // which viewer changed; his query logic is the same either way.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by JFace when the input to the attached viewer changes.
     * We don't need to react here because {@link #getElements} reads the input
     * directly from its parameter every time it is called.
     *
     * @param viewer    the viewer whose input just changed
     * @param oldInput  the previous input object
     * @param newInput  the new input object
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
    }
}
