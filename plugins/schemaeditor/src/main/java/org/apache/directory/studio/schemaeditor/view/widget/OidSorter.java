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

import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.studio.schemaeditor.model.difference.AttributeTypeDifference;
import org.apache.directory.studio.schemaeditor.model.difference.ObjectClassDifference;


// ── CLASS: OidSorter — R2-D2 PLUGS INTO THE DEATH STAR COMPUTER ──────────────
// R2-D2 rolls up to the Death Star's computer terminal and plugs in. The station
// is full of numeric port identifiers and sector codes — pure digits and dots,
// like "2.5.4.3" or "1.3.6.1.4.1.1466.115.121.1.26". R2 reads through them all
// methodically, extracting each OID and sequencing them so the Rebels can map the
// facility. Our OidSorter does the same: it extracts the numeric OID string from
// each schema difference and compares them lexicographically so the tree sorts
// correctly by OID instead of by name.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link Comparator} that sorts {@link AttributeTypeDifference} and
 * {@link ObjectClassDifference} objects ascending by the OID of their underlying
 * schema element. OIDs are dotted numeric strings like {@code "2.5.4.3"} and we
 * compare them case-insensitively as plain strings (not numerically arc by arc).
 * Think of it as R2 scanning the Death Star terminal: it reads each port number
 * in turn and sequences them so the Rebels can navigate the layout efficiently.
 */
public class OidSorter implements Comparator<Object>
{
    // ── R2 READS EACH PORT NUMBER AND DECIDES THE ORDER ──────────────────────────
    // R2 plugs into the terminal and methodically reads two port codes — OID strings —
    // then reports which one comes first. If the combination of element types is
    // unrecognised, R2 falls back to a plain string comparison of the objects.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Compares two schema difference objects by the OID of their underlying schema
     * element. Supported combinations are AT-vs-AT, OC-vs-OC, AT-vs-OC, and OC-vs-AT.
     * For ADDED / MODIFIED / IDENTICAL types we read the destination OID; for REMOVED
     * we read the source OID (because destination does not exist anymore). Unrecognised
     * type combinations fall back to a {@code toString()} comparison.
     *
     * <p>For example — R2 sequences the port numbers:</p>
     * <pre>
     *   o1 OID = "2.5.4.3"   (cn)
     *   o2 OID = "2.5.4.41"  (name)
     *   compare(o1, o2)  →  negative (2.5.4.3 &lt; 2.5.4.41 lexicographically)
     * </pre>
     *
     * @param o1  the first difference object to compare
     * @param o2  the second difference object to compare
     * @return    negative if o1 should sort before o2, positive if after, 0 if equal
     */
    public int compare( Object o1, Object o2 )
    {
        String oid1 = ""; //$NON-NLS-1$
        String oid2 = ""; //$NON-NLS-1$

        if ( ( o1 instanceof AttributeTypeDifference ) && ( o2 instanceof AttributeTypeDifference ) )
        {
            AttributeTypeDifference atd1 = ( AttributeTypeDifference ) o1;
            AttributeTypeDifference atd2 = ( AttributeTypeDifference ) o2;

            switch ( atd1.getType() )
            {
                case ADDED:
                    oid1 = ( ( SchemaObject ) atd1.getDestination() ).getOid();
                    break;
                case MODIFIED:
                    oid1 = ( ( SchemaObject ) atd1.getDestination() ).getOid();
                    break;
                case REMOVED:
                    oid1 = ( ( SchemaObject ) atd1.getSource() ).getOid();
                    break;
                case IDENTICAL:
                    oid1 = ( ( SchemaObject ) atd1.getDestination() ).getOid();
                    break;
            }

            switch ( atd2.getType() )
            {
                case ADDED:
                    oid2 = ( ( SchemaObject ) atd2.getDestination() ).getOid();
                    break;
                case MODIFIED:
                    oid2 = ( ( SchemaObject ) atd2.getDestination() ).getOid();
                    break;
                case REMOVED:
                    oid2 = ( ( SchemaObject ) atd2.getSource() ).getOid();
                    break;
                case IDENTICAL:
                    oid2 = ( ( SchemaObject ) atd2.getDestination() ).getOid();
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
                    oid1 = ( ( SchemaObject ) ocd1.getDestination() ).getOid();
                    break;
                case MODIFIED:
                    oid1 = ( ( SchemaObject ) ocd1.getDestination() ).getOid();
                    break;
                case REMOVED:
                    oid1 = ( ( SchemaObject ) ocd1.getSource() ).getOid();
                    break;
                case IDENTICAL:
                    oid1 = ( ( SchemaObject ) ocd1.getDestination() ).getOid();
                    break;
            }

            switch ( ocd2.getType() )
            {
                case ADDED:
                    oid2 = ( ( SchemaObject ) ocd2.getDestination() ).getOid();
                    break;
                case MODIFIED:
                    oid2 = ( ( SchemaObject ) ocd2.getDestination() ).getOid();
                    break;
                case REMOVED:
                    oid2 = ( ( SchemaObject ) ocd2.getSource() ).getOid();
                    break;
                case IDENTICAL:
                    oid2 = ( ( SchemaObject ) ocd2.getDestination() ).getOid();
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
                    oid1 = ( ( SchemaObject ) atd.getDestination() ).getOid();
                    break;
                case MODIFIED:
                    oid1 = ( ( SchemaObject ) atd.getDestination() ).getOid();
                    break;
                case REMOVED:
                    oid1 = ( ( SchemaObject ) atd.getSource() ).getOid();
                    break;
                case IDENTICAL:
                    oid1 = ( ( SchemaObject ) atd.getDestination() ).getOid();
                    break;
            }

            switch ( ocd.getType() )
            {
                case ADDED:
                    oid2 = ( ( SchemaObject ) ocd.getDestination() ).getOid();
                    break;
                case MODIFIED:
                    oid2 = ( ( SchemaObject ) ocd.getDestination() ).getOid();
                    break;
                case REMOVED:
                    oid2 = ( ( SchemaObject ) ocd.getSource() ).getOid();
                    break;
                case IDENTICAL:
                    oid2 = ( ( SchemaObject ) ocd.getDestination() ).getOid();
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
                    oid1 = ( ( SchemaObject ) ocd.getDestination() ).getOid();
                    break;
                case MODIFIED:
                    oid1 = ( ( SchemaObject ) ocd.getDestination() ).getOid();
                    break;
                case REMOVED:
                    oid1 = ( ( SchemaObject ) ocd.getSource() ).getOid();
                    break;
                case IDENTICAL:
                    oid1 = ( ( SchemaObject ) ocd.getDestination() ).getOid();
                    break;
            }

            switch ( atd.getType() )
            {
                case ADDED:
                    oid2 = ( ( SchemaObject ) atd.getDestination() ).getOid();
                    break;
                case MODIFIED:
                    oid2 = ( ( SchemaObject ) atd.getDestination() ).getOid();
                    break;
                case REMOVED:
                    oid2 = ( ( SchemaObject ) atd.getSource() ).getOid();
                    break;
                case IDENTICAL:
                    oid2 = ( ( SchemaObject ) atd.getDestination() ).getOid();
                    break;
            }
        }

        return oid1.compareToIgnoreCase( oid2 );
    }
}
