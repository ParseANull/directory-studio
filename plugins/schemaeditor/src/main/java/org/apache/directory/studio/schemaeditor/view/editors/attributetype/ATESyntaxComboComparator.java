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

import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.studio.schemaeditor.view.editors.NonExistingSyntax;


// ── CLASS: ATESyntaxComboComparator — MACE WINDU CONFRONTING PALPATINE ───────────────
// When Mace Windu walks into Palpatine's office, he has to rank everyone in the room:
// who poses the greatest threat, who goes where in the confrontation sequence.  He does
// this calmly, decisively, by description — every opponent assessed by what they are.
// This comparator does the same for LDAP syntaxes: given any two items from the syntax
// combo — real LdapSyntax objects or NonExistingSyntax placeholders — it ranks them
// alphabetically by their description so the list is consistently sorted.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * Comparator that sorts the items in the "Syntax" combo box of the Attribute Type
 * Editor into alphabetical order by their description string.
 * We handle all four combinations of {@link LdapSyntax} and {@link NonExistingSyntax}
 * objects.  LDAP syntaxes are typically identified by OID but described by a human-
 * readable string like "Directory String" — we sort by description so the user sees a
 * meaningful alphabetical list rather than a pile of OIDs.
 * Think of Mace: clear ranking by observable characteristics, regardless of type.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ATESyntaxComboComparator implements Comparator<Object>
{
    // ── Mace Ranks Two Opponents by Their Description ────────────────────────────────
    // Mace looks at the two entities before him, reads their descriptions, and determines
    // who goes where in the confrontation sequence — cleanly, across all four type pairings.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Compares two syntax combo items by their description string, case-insensitively.
     * The syntax description is the human-readable label ("Directory String", "Integer",
     * etc.); we extract it from whichever type each object is and compare the two strings.
     * Returns 0 if both descriptions are null or can't be extracted — a safe fallback
     * that leaves the pair in their current relative order rather than throwing.
     *
     * <p>For example — Mace's assessment:</p>
     * <pre>
     *   // LdapSyntax("Directory String") vs LdapSyntax("Integer")
     *   //   → "Directory String" &lt; "Integer" → negative → d comes first
     *   // NonExistingSyntax("Zzz") vs LdapSyntax("Directory String")
     *   //   → "Zzz" &gt; "Directory String" → positive → d comes first
     * </pre>
     *
     * @param o1  the first item — a {@link LdapSyntax} or {@link NonExistingSyntax}
     * @param o2  the second item — a {@link LdapSyntax} or {@link NonExistingSyntax}
     * @return    negative if o1 sorts before o2, zero if equal, positive if o1 sorts after
     */
    public int compare( Object o1, Object o2 )
    {
        String syntax1Description = null;
        String syntax2Description = null;

        if ( o1 instanceof LdapSyntax && o2 instanceof LdapSyntax )
        {
            syntax1Description = ( ( LdapSyntax ) o1 ).getDescription();
            syntax2Description = ( ( LdapSyntax ) o2 ).getDescription();

        }
        else if ( o1 instanceof LdapSyntax && o2 instanceof NonExistingSyntax )
        {
            syntax1Description = ( ( LdapSyntax ) o1 ).getDescription();
            syntax2Description = ( ( NonExistingSyntax ) o2 ).getDescription();
        }
        else if ( o1 instanceof NonExistingSyntax && o2 instanceof LdapSyntax )
        {
            syntax1Description = ( ( NonExistingSyntax ) o1 ).getDescription();
            syntax2Description = ( ( LdapSyntax ) o2 ).getDescription();
        }
        else if ( o1 instanceof NonExistingSyntax && o2 instanceof NonExistingSyntax )
        {
            syntax1Description = ( ( NonExistingSyntax ) o1 ).getDescription();
            syntax2Description = ( ( NonExistingSyntax ) o2 ).getDescription();
        }

        if ( ( syntax1Description != null ) && ( syntax2Description != null ) )
        {
            return syntax1Description.compareToIgnoreCase( syntax2Description );
        }

        return 0;
    }
}
