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


import java.util.ArrayList;
import java.util.List;


// ── CLASS: ATEMatchingRulesComboInput — MILLENNIUM FALCON HIDDEN COMPARTMENTS ────────
// Under the Millennium Falcon's floor plates there are hidden cargo compartments where
// Han stashed his goods — and, more famously, where the Rebel heroes hid from Imperial
// stormtroopers.  The compartments are just hollow spaces that hold whatever you put
// in them; they don't care whether it's spice or Jedi in training.
// This class is those compartments: it's a plain container that holds the list of
// matching-rule combo items (real MatchingRules and NonExistingMatchingRule placeholders)
// that the content provider assembles and the ComboViewer displays.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * Simple container that acts as the JFace input object for the matching-rules combo
 * boxes on the Attribute Type Editor's Overview page.
 * The content provider ({@link ATEMatchingRulesComboContentProvider}) populates this
 * object's children list on the first call to {@code getElements}, and then returns
 * the already-populated list on subsequent calls — the input object is the memory between
 * calls.  Each combo (equality, ordering, substring) gets its own fresh instance so
 * they don't share state.
 * Think of this as the Falcon's hidden compartments: neutral storage that holds whatever
 * items the content provider puts in them.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ATEMatchingRulesComboInput
{
    /** The children */
    private List<Object> children;


    // ── Han Stashes Cargo in the Compartment ────────────────────────────────────────
    // Han opens the floor hatch and drops another item into the hidden hold.  The hold
    // is lazy-initialised — the list doesn't exist until the first item is added.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a single item to the children list.
     * We lazy-initialise the list on the first call to avoid creating an ArrayList
     * when the input is constructed but never populated (e.g. if the combo page is
     * never opened).
     *
     * <p>For example — Han stashes cargo:</p>
     * <pre>
     *   input.addChild( new NonExistingMatchingRule( NonExistingMatchingRule.NONE ) );
     *   input.addChild( someMatchingRule );  // real MatchingRule from the schema
     * </pre>
     *
     * @param child  the item to add — either a {@link org.apache.directory.api.ldap.model.schema.MatchingRule}
     *               or a {@link org.apache.directory.studio.schemaeditor.view.editors.NonExistingMatchingRule};
     *               must not be null
     */
    public void addChild( Object child )
    {
        if ( children == null )
        {
            children = new ArrayList<Object>();
        }

        children.add( child );
    }


    // ── Han Opens the Hatch and Lists the Contents ───────────────────────────────────
    // Han lifts the floor plates and reads out what's in the hold.  If nothing was ever
    // stashed, he returns an empty list — no drama, just an empty compartment.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the mutable list of children currently held in this input.
     * The content provider calls this to read and sort the list before returning it
     * to the ComboViewer.  If nothing has been added yet, we return an empty ArrayList
     * (which is also what triggers the lazy-population logic in the content provider).
     * The caller is allowed to mutate the returned list — that's intentional, so the
     * Overview page can add extra {@link org.apache.directory.studio.schemaeditor.view.editors.NonExistingMatchingRule}
     * entries for unresolvable references.
     *
     * @return  the live children list; never null, may be empty
     */
    public List<Object> getChildren()
    {
        if ( children == null )
        {
            children = new ArrayList<Object>();
        }

        return children;
    }
}
