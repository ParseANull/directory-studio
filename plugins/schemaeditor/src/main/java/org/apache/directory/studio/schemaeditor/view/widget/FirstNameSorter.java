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

package org.apache.directory.studio.schemaeditor.view.widget;


import java.util.Comparator;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.studio.schemaeditor.model.difference.AttributeTypeDifference;
import org.apache.directory.studio.schemaeditor.model.difference.ObjectClassDifference;


// ── CLASS: FirstNameSorter — HAN SHOOTS FIRST ────────────────────────────────
// In the Mos Eisley cantina, Han Solo does not wait to see who blinks first —
// he acts, decisively and without hesitation. Greedo never gets a turn.
// Our comparator operates the same way: when two schema difference objects are
// compared, we extract the first name of each and decide the winner immediately,
// case-insensitively, with no second-guessing.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link Comparator} that sorts {@link AttributeTypeDifference} and
 * {@link ObjectClassDifference} objects ascending by their first registered name.
 * "First name" here means {@code SchemaObject.getNames().get(0)}, which is the
 * primary human-readable identifier (e.g. "cn", "sn", "inetOrgPerson").
 * Think of it as Han shooting first: the first name wins the comparison fast,
 * without inspecting secondary aliases or OIDs.
 */
public class FirstNameSorter implements Comparator<Object>
{
    // ── HAN DRAWS AND DECIDES WHO GOES FIRST ─────────────────────────────────────
    // Two bounty hunters walk into the cantina. Han looks at the first name on each
    // one's wanted poster and decides instantly who ranks higher — alphabetically,
    // case-insensitively. If a poster has no name at all, Han treats it as an empty
    // string and it loses. Objects he does not recognise fall back to toString().
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Compares two schema difference objects by the first name of their underlying
     * schema element. The comparison is case-insensitive and ascending. Supported
     * combinations are AT-vs-AT, OC-vs-OC, AT-vs-OC, and OC-vs-AT — any other pair
     * falls back to a plain {@code toString()} comparison.
     *
     * <p>For example — Han decides the order in the cantina:</p>
     * <pre>
     *   o1 = AttributeTypeDifference("cn")   firstName "cn"
     *   o2 = AttributeTypeDifference("sn")   firstName "sn"
     *   compare(o1, o2)  →  negative (cn &lt; sn alphabetically)
     *
     *   o1 has no names  →  treated as ""  →  sorts before anything with a name
     * </pre>
     *
     * @param o1  the first difference object to compare
     * @param o2  the second difference object to compare
     * @return    negative if o1 should sort before o2, positive if after, 0 if equal
     */
    public int compare( Object o1, Object o2 )
    {
        List<String> o1Names = null;
        List<String> o2Names = null;

        if ( ( o1 instanceof AttributeTypeDifference ) && ( o2 instanceof AttributeTypeDifference ) )
        {
            AttributeTypeDifference atd1 = ( AttributeTypeDifference ) o1;
            AttributeTypeDifference atd2 = ( AttributeTypeDifference ) o2;

            switch ( atd1.getType() )
            {
                case ADDED:
                    o1Names = ( ( SchemaObject ) atd1.getDestination() ).getNames();
                    break;
                case MODIFIED:
                    o1Names = ( ( SchemaObject ) atd1.getDestination() ).getNames();
                    break;
                case REMOVED:
                    o1Names = ( ( SchemaObject ) atd1.getSource() ).getNames();
                    break;
                case IDENTICAL:
                    o1Names = ( ( SchemaObject ) atd1.getDestination() ).getNames();
                    break;
            }

            switch ( atd2.getType() )
            {
                case ADDED:
                    o2Names = ( ( SchemaObject ) atd2.getDestination() ).getNames();
                    break;
                case MODIFIED:
                    o2Names = ( ( SchemaObject ) atd2.getDestination() ).getNames();
                    break;
                case REMOVED:
                    o2Names = ( ( SchemaObject ) atd2.getSource() ).getNames();
                    break;
                case IDENTICAL:
                    o2Names = ( ( SchemaObject ) atd2.getDestination() ).getNames();
                    break;
            }
        }
        else if ( ( o1 instanceof ObjectClassDifference ) && ( o2 instanceof ObjectClassDifference ) )
        {
            ObjectClassDifference ocd1 = ( ObjectClassDifference ) o1;
            ObjectClassDifference ocd2 = ( ObjectClassDifference ) o2;

            switch ( ocd1.getType() )
            {
                case ADDED:
                    o1Names = ( ( SchemaObject ) ocd1.getDestination() ).getNames();
                    break;
                case MODIFIED:
                    o1Names = ( ( SchemaObject ) ocd1.getDestination() ).getNames();
                    break;
                case REMOVED:
                    o1Names = ( ( SchemaObject ) ocd1.getSource() ).getNames();
                    break;
                case IDENTICAL:
                    o1Names = ( ( SchemaObject ) ocd1.getDestination() ).getNames();
                    break;
            }

            switch ( ocd2.getType() )
            {
                case ADDED:
                    o2Names = ( ( SchemaObject ) ocd2.getDestination() ).getNames();
                    break;
                case MODIFIED:
                    o2Names = ( ( SchemaObject ) ocd2.getDestination() ).getNames();
                    break;
                case REMOVED:
                    o2Names = ( ( SchemaObject ) ocd2.getSource() ).getNames();
                    break;
                case IDENTICAL:
                    o2Names = ( ( SchemaObject ) ocd2.getDestination() ).getNames();
                    break;
            }
        }
        else if ( ( o1 instanceof AttributeTypeDifference ) && ( o2 instanceof ObjectClassDifference ) )
        {
            AttributeTypeDifference atd = ( AttributeTypeDifference ) o1;
            ObjectClassDifference ocd = ( ObjectClassDifference ) o2;

            switch ( atd.getType() )
            {
                case ADDED:
                    o1Names = ( ( SchemaObject ) atd.getDestination() ).getNames();
                    break;
                case MODIFIED:
                    o1Names = ( ( SchemaObject ) atd.getDestination() ).getNames();
                    break;
                case REMOVED:
                    o1Names = ( ( SchemaObject ) atd.getSource() ).getNames();
                    break;
                case IDENTICAL:
                    o1Names = ( ( SchemaObject ) atd.getDestination() ).getNames();
                    break;
            }

            switch ( ocd.getType() )
            {
                case ADDED:
                    o2Names = ( ( SchemaObject ) ocd.getDestination() ).getNames();
                    break;
                case MODIFIED:
                    o2Names = ( ( SchemaObject ) ocd.getDestination() ).getNames();
                    break;
                case REMOVED:
                    o2Names = ( ( SchemaObject ) ocd.getSource() ).getNames();
                    break;
                case IDENTICAL:
                    o2Names = ( ( SchemaObject ) ocd.getDestination() ).getNames();
                    break;
            }
        }
        else if ( ( o1 instanceof ObjectClassDifference ) && ( o2 instanceof AttributeTypeDifference ) )
        {
            ObjectClassDifference ocd = ( ObjectClassDifference ) o1;
            AttributeTypeDifference atd = ( AttributeTypeDifference ) o2;

            switch ( ocd.getType() )
            {
                case ADDED:
                    o1Names = ( ( SchemaObject ) ocd.getDestination() ).getNames();
                    break;
                case MODIFIED:
                    o1Names = ( ( SchemaObject ) ocd.getDestination() ).getNames();
                    break;
                case REMOVED:
                    o1Names = ( ( SchemaObject ) ocd.getSource() ).getNames();
                    break;
                case IDENTICAL:
                    o1Names = ( ( SchemaObject ) ocd.getDestination() ).getNames();
                    break;
            }

            switch ( atd.getType() )
            {
                case ADDED:
                    o2Names = ( ( SchemaObject ) atd.getDestination() ).getNames();
                    break;
                case MODIFIED:
                    o2Names = ( ( SchemaObject ) atd.getDestination() ).getNames();
                    break;
                case REMOVED:
                    o2Names = ( ( SchemaObject ) atd.getSource() ).getNames();
                    break;
                case IDENTICAL:
                    o2Names = ( ( SchemaObject ) atd.getDestination() ).getNames();
                    break;
            }
        }

        // Comparing the First Name
        if ( ( o1Names != null ) && ( o2Names != null ) )
        {
            if ( ( o1Names.size() > 0 ) && ( o2Names.size() > 0 ) )
            {
                return o1Names.get( 0 ).compareToIgnoreCase( o2Names.get( 0 ) );
            }
            else if ( ( o1Names.size() == 0 ) && ( o2Names.size() > 0 ) )
            {
                return "".compareToIgnoreCase( o2Names.get( 0 ) ); //$NON-NLS-1$
            }
            else if ( ( o1Names.size() > 0 ) && ( o2Names.size() == 0 ) )
            {
                return o1Names.get( 0 ).compareToIgnoreCase( "" ); //$NON-NLS-1$
            }
        }

        // Default
        return o1.toString().compareToIgnoreCase( o2.toString() );
    }
}
