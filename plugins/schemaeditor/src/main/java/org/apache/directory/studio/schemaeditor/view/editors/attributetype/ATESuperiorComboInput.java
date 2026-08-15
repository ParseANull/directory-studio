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

import org.apache.directory.api.ldap.model.schema.AttributeType;


// ── CLASS: ATESuperiorComboInput — MILLENNIUM FALCON HIDDEN CARGO WITH A MANIFEST ────
// The Millennium Falcon's hidden compartments hold cargo — but for the superior-type
// combo we also need to know whose cargo it is.  Before the Rebel mission, Han knows
// the identity of his passengers (Luke, Obi-Wan) and keeps a manifest alongside the
// hold.  This class is that manifest-plus-hold: it stores the attribute type currently
// being edited (the "owner" that determines which items can't appear in the list) and
// the lazy-populated list of candidate superior types.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * Container that acts as the JFace input object for the "Superior Type" combo box on
 * the Attribute Type Editor's Overview page.
 * Unlike {@link ATEMatchingRulesComboInput}, this input also holds a reference to the
 * attribute type being edited — the content provider needs this to run the sub-type
 * filter and exclude circular ancestors from the combo list.
 * Think of this as the Falcon's cargo hold with a manifest: the hold (children list)
 * stores the combo items, while the manifest (attributeType) identifies whose type
 * we're editing so we can apply the right filter.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ATESuperiorComboInput
{
    /** The attribute type */
    private AttributeType at;

    /** The children */
    private List<Object> children;


    // ── Han Logs the Passenger and Prepares the Hold ─────────────────────────────────
    // Han notes who the passengers are before they board — he needs to know their
    // identities to decide which cargo bays are off-limits for them later.
    // Here we record which attribute type is being edited; the content provider will
    // use it to filter out illegal superior candidates.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new input instance for the given attribute type.
     * We store the attribute type reference so the content provider can call
     * {@link #getAttributeType()} and use it in the sub-type circularity check.
     * The children list is not initialised yet — it will be lazily populated on the
     * first {@code getElements} call.
     *
     * <p>For example — Han logs the passenger manifest:</p>
     * <pre>
     *   // We're editing the "cn" attribute type:
     *   ATESuperiorComboInput input = new ATESuperiorComboInput( cnAttributeType );
     *   // The content provider can now call input.getAttributeType() to know what to filter.
     * </pre>
     *
     * @param at  the AttributeType currently being edited; the content provider uses it
     *            to exclude that type and all its descendants from the combo list
     */
    public ATESuperiorComboInput( AttributeType at )
    {
        this.at = at;
    }


    // ── Han Checks the Passenger Manifest ───────────────────────────────────────────
    // "Who's on the ship?" — Han pulls out his manifest and reads off the registered
    // passenger.  The content provider asks this question to know which types to filter.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute type currently being edited.
     * The content provider uses this to determine which attribute types must be excluded
     * from the combo (those that are sub-types of this one, to prevent circular ancestry).
     *
     * @return  the AttributeType passed to the constructor; never null if constructed
     *          with a non-null argument
     */
    public AttributeType getAttributeType()
    {
        return at;
    }


    // ── Han Loads Another Crate into the Hold ────────────────────────────────────────
    // The ground crew rolls another crate up the ramp and Han checks it in.  The hold
    // list is lazy-initialised on the first load.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a single item to the children list.
     * We lazy-initialise the list on the first call.  Items are either real
     * {@link AttributeType} objects (valid superior candidates) or
     * {@link org.apache.directory.studio.schemaeditor.view.editors.NonExistingAttributeType}
     * placeholders for unresolvable references.
     *
     * <p>For example — loading the cargo:</p>
     * <pre>
     *   input.addChild( new NonExistingAttributeType( NonExistingAttributeType.NONE ) );
     *   input.addChild( someAttributeType );
     * </pre>
     *
     * @param child  the item to add to the children list; must not be null
     */
    public void addChild( Object child )
    {
        if ( children == null )
        {
            children = new ArrayList<Object>();
        }

        children.add( child );
    }


    // ── Han Opens the Hold and Reads the Manifest ────────────────────────────────────
    // Han opens the cargo-bay doors and reads out everything stored inside.  If the
    // hold was never loaded, he returns an empty bay — the content provider interprets
    // this as "nothing populated yet, go fill it."
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the mutable list of children currently in this input.
     * An empty list signals the content provider to lazily populate it on the next call.
     * The caller (content provider or Overview page) may add extra items to the returned
     * list — for example to inject an unresolvable reference as a NonExistingAttributeType.
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
