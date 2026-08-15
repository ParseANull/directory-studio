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
package org.apache.directory.studio.openldap.config.editor.wrappers;

// ── CLASS: DatabaseWrapperViewerComparator — The Fleet Admiral Reviews the Order of Battle ──
// Grand Admiral Thrawn lines up his fleet in strict sequence: ships with explicit
// position numbers come first, ranked by those numbers; ships without numbers
// fall to the back.  DatabaseWrapperViewerComparator does the same thing for the
// database table — databases that carry an olcDatabase ordering prefix are ranked
// by that prefix; databases without a prefix trail at the end.  The result is the
// list the user sees in the Databases section, in the exact order OpenLDAP loads
// them at startup.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A JFace {@link ViewerComparator} that sorts {@link DatabaseWrapper} entries
 * by their olcDatabase ordering prefix.  Databases with a lower numeric prefix
 * appear earlier; databases without a prefix are ordered after all numbered
 * databases.  This mirrors the loading order OpenLDAP uses at startup.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DatabaseWrapperViewerComparator extends ViewerComparator
{
    // ── compare — Sort Two Databases by Their Fleet Position ──────────────────
    // Thrawn assigns every ship a numbered position in the battle line.  We
    // compare the two position numbers and return their relative order; ships
    // without a number fall to the back of the line.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare( Viewer viewer, Object e1, Object e2 )
    {
        if ( ( e1 instanceof DatabaseWrapper ) && ( e2 instanceof DatabaseWrapper ) )
        {
            OlcDatabaseConfig database1 = ( ( DatabaseWrapper ) e1 ).getDatabase();
            OlcDatabaseConfig database2 = ( ( DatabaseWrapper ) e2 ).getDatabase();
            boolean db1HasOrderingPrefix = OpenLdapConfigurationPluginUtils.hasOrderingPrefix( database1
                .getOlcDatabase() );
            boolean db2HasOrderingPrefix = OpenLdapConfigurationPluginUtils.hasOrderingPrefix( database2
                .getOlcDatabase() );

            if ( db1HasOrderingPrefix && db2HasOrderingPrefix )
            {
                int orderingPrefix1 = OpenLdapConfigurationPluginUtils.getOrderingPrefix( database1
                    .getOlcDatabase() );
                int orderingPrefix2 = OpenLdapConfigurationPluginUtils.getOrderingPrefix( database2
                    .getOlcDatabase() );

                if ( orderingPrefix1 > orderingPrefix2 )
                {
                    return Integer.MAX_VALUE;
                }
                else if ( orderingPrefix1 < orderingPrefix2 )
                {
                    return Integer.MIN_VALUE;
                }
                else
                {
                    return 0;
                }
            }
            else if ( db1HasOrderingPrefix )
            {
                return Integer.MIN_VALUE;
            }
            else if ( db2HasOrderingPrefix )
            {
                return Integer.MAX_VALUE;
            }
            else
            {
                return 1;
            }
        }

        return super.compare( viewer, e1, e2 );
    }
}
