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


import java.util.Comparator;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.studio.schemaeditor.view.editors.NonExistingMatchingRule;


// ── CLASS: ATEMatchingRulesComboComparator — MACE WINDU CONFRONTING PALPATINE ───────
// When Mace Windu arrives in Palpatine's office with a squad of Jedi, he has to
// make a judgement call: who's stronger, who outranks whom, and in what order should
// he address the threat?  He sizes up each opponent and ranks them decisively.
// This comparator does the same thing for matching rules: given any two items from the
// matching-rules combo — real MatchingRule objects, NonExistingMatchingRule placeholders,
// or a mix of both — it decides which comes first alphabetically so the list is
// consistently ordered for the user.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * Comparator that sorts the items in the matching-rules combo box of the Attribute Type
 * Editor into alphabetical order by their first display name.
 * The combo can contain real {@link MatchingRule} objects (loaded from the schema) and
 * {@link NonExistingMatchingRule} placeholders (for referenced-but-missing rules), so we
 * handle all four combinations of those two types.
 * Think of this as Mace Windu sizing up adversaries: regardless of their origin, he
 * ranks them in the right order before deciding how to proceed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ATEMatchingRulesComboComparator implements Comparator<Object>
{
    // ── Mace Sizes Up Two Opponents ──────────────────────────────────────────────────
    // Mace looks at the two Jedi/Sith before him, reads their names, and determines
    // which one ranks higher in the pecking order — purely by name, case-insensitively,
    // across all four possible pairings of real vs. phantom opponents.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Compares two combo items by their first name, case-insensitively, so the matching
     * rules list sorts A-Z regardless of whether each entry is a real MatchingRule or
     * a NonExistingMatchingRule placeholder.
     * If either object's name list is empty or null we treat them as equal (return 0)
     * rather than blowing up — missing names are unusual but shouldn't crash the sort.
     *
     * <p>For example — Mace ranks two opponents:</p>
     * <pre>
     *   // MatchingRule("caseIgnoreMatch") vs MatchingRule("exactMatch")
     *   //   → "caseIgnoreMatch" &lt; "exactMatch" → negative result → c comes first
     *   // MatchingRule("xyz") vs NonExistingMatchingRule("abc")
     *   //   → "xyz" &gt; "abc" → positive result → a comes first
     * </pre>
     *
     * @param o1  the first combo item — either a {@link MatchingRule} or a
     *            {@link NonExistingMatchingRule}
     * @param o2  the second combo item — either a {@link MatchingRule} or a
     *            {@link NonExistingMatchingRule}
     * @return    a negative integer if o1 sorts before o2, zero if equal, positive if
     *            o1 sorts after o2; returns 0 when names can't be compared
     */
    public int compare( Object o1, Object o2 )
    {
        if ( o1 instanceof MatchingRule && o2 instanceof MatchingRule )
        {
            List<String> mr1Names = ( ( MatchingRule ) o1 ).getNames();
            List<String> mr2Names = ( ( MatchingRule ) o2 ).getNames();

            if ( ( mr1Names != null ) && ( mr2Names != null ) && ( mr1Names.size() > 0 ) && ( mr2Names.size() > 0 ) )
            {
                return mr1Names.get( 0 ).compareToIgnoreCase( mr2Names.get( 0 ) );
            }
        }
        else if ( o1 instanceof MatchingRule && o2 instanceof NonExistingMatchingRule )
        {
            List<String> mr1Names = ( ( MatchingRule ) o1 ).getNames();
            String mr2Name = ( ( NonExistingMatchingRule ) o2 ).getName();

            if ( ( mr1Names != null ) && ( mr2Name != null ) && ( mr1Names.size() > 0 ) )
            {
                return mr1Names.get( 0 ).compareToIgnoreCase( mr2Name );
            }
        }
        else if ( o1 instanceof NonExistingMatchingRule && o2 instanceof MatchingRule )
        {
            String mr1Name = ( ( NonExistingMatchingRule ) o1 ).getName();
            List<String> mr2Names = ( ( MatchingRule ) o2 ).getNames();

            if ( ( mr1Name != null ) && ( mr2Names != null ) && ( mr2Names.size() > 0 ) )
            {
                return mr1Name.compareToIgnoreCase( mr2Names.get( 0 ) );
            }
        }
        else if ( o1 instanceof NonExistingMatchingRule && o2 instanceof NonExistingMatchingRule )
        {
            String mr1Name = ( ( NonExistingMatchingRule ) o1 ).getName();
            String mr2Name = ( ( NonExistingMatchingRule ) o2 ).getName();

            if ( ( mr1Name != null ) && ( mr2Name != null ) )
            {
                return mr1Name.compareToIgnoreCase( mr2Name );
            }
        }

        return 0;
    }
}
