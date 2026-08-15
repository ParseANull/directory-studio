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

package org.apache.directory.studio.schemaeditor.view.wrappers;


import java.util.Comparator;


// ── CLASS: ProjectSorter — Han Shoots First in the Mos Eisley Cantina ────────
// When two bounty hunters walk through the same cantina door, Han Solo does not
// deliberate. He checks who has the higher price on their head and makes a
// snap decision — one of them leaves first, the other stays seated, end of story.
// ProjectSorter is that snap decision: given two project wrappers, it looks at
// each project's name and instantly decides the alphabetical order — no ceremony,
// no nested logic, just a direct string comparison and a result.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Sorts {@link TreeNode} objects that wrap projects in ascending alphabetical order by project name.
 * When the Projects View renders its list of open schema projects, they may arrive
 * in any order. This comparator imposes a case-insensitive alphabetical sort so the
 * user always sees a predictable list.
 * Think of Han sizing up the two bounty hunters: quick judgement, decisive result,
 * no hesitation — the one with the earlier-alphabetical name goes first.
 */
public class ProjectSorter implements Comparator<TreeNode>
{
    // ── Han Glances at the Two Hunters and Makes His Call ────────────────────
    // Han looks at the two figures at the bar: he sizes them up by name and rep —
    // the one who costs less trouble goes first, the other waits. Fast, definitive.
    // compare() does the same for two ProjectWrapper nodes: compare names, return result.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Compares two {@link TreeNode} objects by the name of the project each wraps.
     * Both nodes must be {@link ProjectWrapper} instances; if either is not, we return 0
     * (treat them as equal) since we have no basis for comparison.
     * The comparison is case-insensitive so "MyProject" and "myproject" sort together.
     *
     * <p>For example — Han deciding which bounty hunter gets the easier job:</p>
     * <pre>
     *   sorter.compare( alphaProjectWrapper, betaProjectWrapper );
     *   // "Alpha" vs "Beta" — Alpha comes first; negative value returned
     * </pre>
     *
     * @param tn1  the first tree node to compare; expected to be a {@link ProjectWrapper}
     * @param tn2  the second tree node to compare; expected to be a {@link ProjectWrapper}
     * @return     a negative integer if {@code tn1} sorts before {@code tn2}, zero if not
     *             both ProjectWrappers or if names are equal, positive if {@code tn1} sorts after
     */
    public int compare( TreeNode tn1, TreeNode tn2 )
    {
        if ( ( tn1 instanceof ProjectWrapper ) && ( tn2 instanceof ProjectWrapper ) )
        {
            ProjectWrapper pw1 = ( ProjectWrapper ) tn1;
            ProjectWrapper pw2 = ( ProjectWrapper ) tn2;

            return pw1.getProject().getName().compareToIgnoreCase( pw2.getProject().getName() );
        }

        // Default
        return 0;
    }
}
