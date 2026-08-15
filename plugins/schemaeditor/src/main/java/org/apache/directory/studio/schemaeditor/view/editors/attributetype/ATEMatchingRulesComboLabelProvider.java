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


import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.studio.schemaeditor.view.editors.NonExistingMatchingRule;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;


// ── CLASS: ATEMatchingRulesComboLabelProvider — C-3PO TRANSLATES FOR R2 ─────────────
// C-3PO's entire job is translation: R2-D2 hands him a cryptic binary blurt and
// C-3PO converts it into something a human crew member can understand and act on.
// He knows the difference between R2's native binary and a protocol droid's spoken
// output — two types, two translation paths, one fluent result.
// This label provider does the same: the ComboViewer hands it an opaque Object — either
// a real MatchingRule or a NonExistingMatchingRule placeholder — and we translate it
// into the human-readable text string that appears in the combo dropdown.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * JFace LabelProvider that converts matching-rule combo items into display strings for
 * the equality, ordering, and substring combo boxes on the Attribute Type Editor.
 * For a real {@link MatchingRule} we format it as "name  -  (OID)"; for a
 * {@link NonExistingMatchingRule} we delegate to its own {@code getDisplayName()} which
 * either returns "(None)" or appends a missing-rule warning.
 * Think of this as C-3PO: he knows how to speak "MatchingRule" and "NonExistingMatchingRule"
 * and translates both into plain English for the user.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ATEMatchingRulesComboLabelProvider extends LabelProvider
{
    // ── C-3PO Translates the Binary Blurt Into English ──────────────────────────────
    // R2 chirps something at C-3PO who then turns to the crew and says "He says the
    // matching rule is 'caseIgnoreMatch  -  (2.5.13.2)'."  If what R2 handed over is
    // a placeholder for a missing rule, C-3PO reads its display name instead — still
    // comprehensible, still informative.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display string for a single combo item.
     * For a {@link MatchingRule}: we show the rule's first name followed by its OID in
     * parentheses, e.g. "caseIgnoreMatch  -  (2.5.13.2)".  If the rule has no name at
     * all (unusual but possible for schema fragments), we fall back to a localised
     * "(no name)  -  (OID)" format via NLS.
     * For a {@link NonExistingMatchingRule}: we return its {@code getDisplayName()} which
     * is either "(None)" or the raw name with a warning annotation.
     * For anything else: we return null (the JFace default "nothing to show").
     *
     * <p>For example — C-3PO delivers the translation:</p>
     * <pre>
     *   // obj = MatchingRule with name "caseIgnoreMatch", OID "2.5.13.2"
     *   // → "caseIgnoreMatch  -  (2.5.13.2)"
     *
     *   // obj = NonExistingMatchingRule("(None)")
     *   // → "(None)"
     *
     *   // obj = NonExistingMatchingRule("badRule")
     *   // → "badRule   (This matching rule doesnt exist)"
     * </pre>
     *
     * @param obj  the combo item — a {@link MatchingRule} or {@link NonExistingMatchingRule}
     * @return     the human-readable display string, or null if the type is unrecognised
     */
    public String getText( Object obj )
    {
        if ( obj instanceof MatchingRule )
        {
            MatchingRule mr = ( MatchingRule ) obj;

            String name = mr.getName();
            if ( name != null )
            {
                return name + "  -  (" + mr.getOid() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            else
            {
                return NLS.bind(
                    Messages.getString( "ATEMatchingRulesComboLabelProvider.None" ), new String[] { mr.getOid() } ); //$NON-NLS-1$
            }
        }
        else if ( obj instanceof NonExistingMatchingRule )
        {
            return ( ( NonExistingMatchingRule ) obj ).getDisplayName();
        }

        // Default
        return null;
    }
}
