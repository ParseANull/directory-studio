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
package org.apache.directory.studio.aciitemeditor.model;


import org.apache.directory.api.ldap.aci.UserClass;
import org.apache.directory.studio.aciitemeditor.valueeditors.SubtreeValueEditor;
import org.apache.directory.studio.valueeditors.dn.DnValueEditor;


// ── CLASS: UserClassWrapperFactory — THE ISB PERSONNEL-CATEGORY PRINT SHOP ───
// Before an ISB officer can review a clearance manifest, the print shop
// pre-prints one row for every possible user category: all users, this entry,
// parent, named users, groups, and subtrees.
// This factory is that print shop for the six user-class rows.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Factory that creates the full set of {@link UserClassWrapper} instances for
 * the user-classes table viewer.
 * Produces one wrapper per ACI user-class category (6 in total), each pre-wired
 * with the appropriate value editor so the table is ready to use immediately.
 * Think of this class as the ISB personnel-category print shop: it stamps out
 * every user-class row before the officer sits down to tick and configure them.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class UserClassWrapperFactory
{
    // ── STAMP OUT ALL 6 USER-CLASS ROWS ──────────────────────────────────────
    // The print shop runs through all six user-class categories — allUsers,
    // thisEntry, parentOfEntry, name, userGroup, subtree — and produces one
    // pre-printed row for each, binding the correct value editor where needed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates and returns all 6 {@link UserClassWrapper} instances, one for each
     * ACI user-class category, ready to be fed to the user-classes table viewer.
     * Categories with no configurable value ({@code allUsers}, {@code thisEntry},
     * {@code parentOfEntry}) receive a {@code null} editor; {@code name} and
     * {@code userGroup} use a DN editor; {@code subtree} uses a subtree-spec editor.
     *
     * <p>For example — the table gets its rows during dialog initialisation:</p>
     * <pre>
     *   UserClassWrapper[] rows = UserClassWrapperFactory.createUserClassWrappers();
     *   tableViewer.setInput(rows);
     * </pre>
     *
     * @return an array of 6 {@link UserClassWrapper} objects in canonical ACI order
     */
    public static UserClassWrapper[] createUserClassWrappers()
    {
        UserClassWrapper[] userClassWrappers = new UserClassWrapper[]
            {
                // allUsers
                new UserClassWrapper( UserClass.AllUsers.class, "", //$NON-NLS-1$
                    "", //$NON-NLS-1$
                    null ),

                // thisEntry
                new UserClassWrapper( UserClass.ThisEntry.class, "", //$NON-NLS-1$
                    "", //$NON-NLS-1$
                    null ),

                // parentOfEntry
                new UserClassWrapper( UserClass.ParentOfEntry.class, "", //$NON-NLS-1$
                    "", //$NON-NLS-1$
                    null ),

                // name
                new UserClassWrapper( UserClass.Name.class, "\"", //$NON-NLS-1$
                    "\"", //$NON-NLS-1$
                    new DnValueEditor() ),

                // userGroup
                new UserClassWrapper( UserClass.UserGroup.class, "\"", //$NON-NLS-1$
                    "\"", //$NON-NLS-1$
                    new DnValueEditor() ),

                // subtree
                new UserClassWrapper( UserClass.Subtree.class, "", //$NON-NLS-1$
                    "", //$NON-NLS-1$
                    new SubtreeValueEditor( false, false ) ) };

        return userClassWrappers;
    }
}
