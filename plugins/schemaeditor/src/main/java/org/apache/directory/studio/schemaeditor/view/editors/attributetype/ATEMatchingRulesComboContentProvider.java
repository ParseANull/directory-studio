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


import java.util.Collections;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.view.editors.NonExistingMatchingRule;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: ATEMatchingRulesComboContentProvider — LANDO RUNNING CLOUD CITY ──────────
// Lando Calrissian runs Cloud City with an iron fist and a velvet glove: he knows
// every resident, every resource, and every deal in play.  When visitors arrive, he
// presents them with exactly the right set of options — sorted, curated, ready to go.
// This content provider is Lando.  When the ComboViewer asks "give me the list," we
// pull every matching rule from the schema handler, add a "(None)" entry at the top,
// sort the whole lot alphabetically, and hand it back — ready for the user to pick from.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * JFace IStructuredContentProvider that populates the equality, ordering, and substring
 * matching-rules combo boxes on the Attribute Type Editor's Overview page.
 * We ask the schema handler for every known matching rule, prepend a "(None)" sentinel,
 * sort everything alphabetically via {@link ATEMatchingRulesComboComparator}, and return
 * the sorted array for the ComboViewer to display.
 * Think of Lando: he knows the full inventory of Cloud City and always presents a
 * well-organised menu of options to his guests.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ATEMatchingRulesComboContentProvider implements IStructuredContentProvider
{
    // ── Lando Presents the Full Inventory to His Guest ───────────────────────────────
    // A visitor walks into Cloud City and Lando personally walks them through every
    // available service — starting with "(None) / no deal" as the default, then every
    // real option, sorted so the visitor can browse quickly.
    // We lazy-load the children into the input object on the first call so the schema
    // handler is only queried once per editor session.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the sorted array of combo items for the given input element.
     * If the input's children list is empty (first call), we populate it: first a
     * {@link NonExistingMatchingRule} "(None)" sentinel, then every matching rule known
     * to the schema handler.  Subsequent calls re-sort and return the already-populated
     * list, which may include extra {@link NonExistingMatchingRule} entries added by the
     * Overview page for unresolvable references.
     * Returns an empty array if the input isn't an {@link ATEMatchingRulesComboInput}.
     *
     * <p>For example — Lando's welcome tour:</p>
     * <pre>
     *   // First call: populate with (None) + all schema matching rules, then sort.
     *   // Second call: list is already populated, just sort and return.
     * </pre>
     *
     * @param inputElement  the {@link ATEMatchingRulesComboInput} that acts as our
     *                      container; if it's anything else we return an empty array
     * @return              a sorted Object[] of {@link MatchingRule} and
     *                      {@link NonExistingMatchingRule} instances ready for the combo
     */
    public Object[] getElements( Object inputElement )
    {
        if ( inputElement instanceof ATEMatchingRulesComboInput )
        {
            ATEMatchingRulesComboInput input = ( ATEMatchingRulesComboInput ) inputElement;

            if ( input.getChildren().isEmpty() )
            {
                // Creating the '(None)' item
                input.addChild( new NonExistingMatchingRule( NonExistingMatchingRule.NONE ) );

                // Creating Children
                List<MatchingRule> equalityMatchingRules = Activator.getDefault().getSchemaHandler()
                    .getMatchingRules();
                for ( MatchingRule matchingRule : equalityMatchingRules )
                {
                    input.addChild( matchingRule );
                }
            }

            // Getting Children
            List<Object> children = input.getChildren();

            // Sorting Children
            Collections.sort( children, new ATEMatchingRulesComboComparator() );

            return children.toArray();
        }

        // Default
        return new Object[0];
    }


    // ── Lando Closes Up Shop ─────────────────────────────────────────────────────────
    // When Cloud City winds down operations, Lando doesn't need to do anything special
    // — the inventory lives in the input object, not here.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the viewer is disposed; nothing to clean up here because
     * we hold no resources ourselves — all state is in the {@link ATEMatchingRulesComboInput}.
     */
    public void dispose()
    {
    }


    // ── Lando Updates His Records When the Guest List Changes ────────────────────────
    // When the guest roster changes (a new input is set on the viewer), Lando notes
    // the swap but doesn't need to do anything extra — the new input carries its own
    // inventory and {@link #getElements} will populate it on demand.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the viewer's input changes (e.g. when the editor refreshes
     * and sets a new {@link ATEMatchingRulesComboInput} on the combo viewer).
     * We don't need to react here — the next call to {@link #getElements} will populate
     * the new input's children list lazily.
     *
     * @param viewer    the ComboViewer whose input just changed
     * @param oldInput  the previous input object (no longer used)
     * @param newInput  the new input object (an ATEMatchingRulesComboInput)
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
    }
}
