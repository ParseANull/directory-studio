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


import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.eclipse.osgi.util.NLS;


// ── CLASS: AttributeTypeWrapper — Vader's Suit on the Star Destroyer ─────────
// Darth Vader's obsidian suit is not Vader himself — it is the shell that makes
// the broken Anakin Skywalker presentable to the Empire: life-support systems,
// armour plating, voice modulator, all encasing a deeply powerful being underneath.
// AttributeTypeWrapper is exactly that suit: the {@link AttributeType} (the real power)
// is sealed inside, and the wrapper adds the TreeViewer-friendly exterior — the
// parent link, the equals/hashCode contract, the toString display — that JFace
// needs to render it in a tree row without ever touching the LDAP model directly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Adapts an {@link AttributeType} so it can live as a node in the Schema Editor's JFace TreeViewer.
 * An {@link AttributeType} knows about OIDs, syntax rules, and matching rules — but it knows
 * nothing about tree parents, child lists, or display equality.
 * We snap this wrapper around it and the TreeViewer treats it like any other tree node,
 * while we keep the raw {@link AttributeType} accessible via {@link #getAttributeType()}.
 * Think of it as Vader's suit: powerful schema definition on the inside, TreeViewer-compatible
 * shell on the outside.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeTypeWrapper extends AbstractTreeNode
{
    /** The wrapped AttributeType */
    private AttributeType attributeType;


    // ── The Suit Is Sealed Around Vader — No Parent Yet ──────────────────────
    // In the medical bay on Coruscant, the suit snaps shut around Anakin before
    // anyone knows where he will stand in the Imperial hierarchy — no commander
    // above him yet, just the raw Sith Lord inside the armour.
    // This constructor wraps an AttributeType as a root-level node (parent = null).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a root-level wrapper around the given {@link AttributeType} with no parent.
     * Use this when the attribute type sits at the top of a subtree — for example
     * when building a flat list rather than a nested hierarchy.
     *
     * <p>For example — Vader's suit sealed before his place in the hierarchy is assigned:</p>
     * <pre>
     *   AttributeTypeWrapper wrapper = new AttributeTypeWrapper( cn );
     *   // wrapper.getParent() == null — this is a top-level node
     * </pre>
     *
     * @param at  the {@link AttributeType} to wrap; must not be {@code null}
     */
    public AttributeTypeWrapper( AttributeType at )
    {
        super( null );
        attributeType = at;
    }


    // ── The Suit Marches Onto the Bridge Under Grand Moff Tarkin ─────────────
    // Once the Death Star is operational, Vader strides onto the bridge and takes
    // his place under Tarkin — powerful, unmistakable, positioned within the
    // Imperial command structure.
    // This constructor wraps an AttributeType and attaches it to its parent node.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a wrapper around the given {@link AttributeType} and positions it under a parent node.
     * Use this when adding an attribute type inside a schema folder or another containing node —
     * the parent link lets the TreeViewer navigate upward from this node.
     *
     * <p>For example — Vader taking his station on the Death Star bridge under Tarkin:</p>
     * <pre>
     *   AttributeTypeWrapper wrapper = new AttributeTypeWrapper( cn, schemaFolder );
     *   // wrapper.getParent() == schemaFolder
     * </pre>
     *
     * @param at      the {@link AttributeType} to wrap; must not be {@code null}
     * @param parent  the parent {@link TreeNode} in the tree hierarchy
     */
    public AttributeTypeWrapper( AttributeType at, TreeNode parent )
    {
        super( parent );
        attributeType = at;
    }


    // ── Unclasping the Suit to Reach the Sith Lord Inside ────────────────────
    // At Cloud City, the Empire's technicians carefully remove panels of Vader's
    // armour to access the systems underneath — the real Anakin, the real power.
    // getAttributeType() is that access hatch: it hands back the raw LDAP model
    // object so callers can inspect OIDs, syntax rules, matching rules, and names.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link AttributeType} that this wrapper holds.
     * Callers that need to inspect the actual LDAP schema definition — OID, syntax,
     * names, matching rules — use this to reach past the wrapper layer.
     *
     * <p>For example — reaching inside the armour to access the Sith Lord directly:</p>
     * <pre>
     *   AttributeType at = wrapper.getAttributeType();
     *   String oid = at.getOid();  // "2.5.4.3" for the 'cn' attribute
     * </pre>
     *
     * @return  the wrapped {@link AttributeType}; never {@code null} if the wrapper was built correctly
     */
    public AttributeType getAttributeType()
    {
        return attributeType;
    }


    // ── Two Suits on the Bridge — Are They the Same Vader? ────────────────────
    // If two identical black suits appear on the bridge, the crew checks: same
    // voice modulator frequency, same parent command post.  Only then are they
    // considered the same officer.
    // equals() checks parent (via super) and then the wrapped AttributeType itself.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if {@code obj} is an {@link AttributeTypeWrapper} that wraps the same
     * {@link AttributeType} and sits under the same parent node.
     * We call {@code super.equals()} first to handle the parent comparison, then compare the
     * wrapped attribute types to ensure structural identity.
     *
     * <p>For example — the bridge crew verifying it's really the same Vader at the same post:</p>
     * <pre>
     *   wrapper1.equals( wrapper2 );
     *   // true only if same AttributeType AND same parent node
     * </pre>
     *
     * @param obj  the object to compare against
     * @return     {@code true} if the wrappers represent the same attribute type at the same tree position
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof AttributeTypeWrapper )
        {
            if ( super.equals( obj ) )
            {
                AttributeTypeWrapper atw = ( AttributeTypeWrapper ) obj;

                if ( ( attributeType != null ) && ( !attributeType.equals( atw.getAttributeType() ) ) )
                {
                    return false;
                }

                return true;
            }
        }

        // Default
        return false;
    }


    // ── The Suit's Unique Imperial Identification Code ────────────────────────
    // Every piece of Imperial hardware carries a serialised identification code —
    // armour plates, voice modulators, all stamped with a numeric hash so logistics
    // can track them across the fleet.
    // Our hashCode() folds both the parent's code and the AttributeType's code together.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Computes a hash code that combines the parent's hash (from {@code super.hashCode()})
     * with the wrapped {@link AttributeType}'s hash.
     * Required to keep the equals/hashCode contract: if two wrappers are equal, their
     * hash codes must match.
     *
     * <p>For example — stamping the armour plate with both the officer's and the ship's codes:</p>
     * <pre>
     *   int hash = wrapper.hashCode();
     *   // 37 * parentHash + attributeType.hashCode()
     * </pre>
     *
     * @return  a hash code consistent with {@link #equals}
     */
    public int hashCode()
    {
        int result = super.hashCode();

        if ( attributeType != null )
        {
            result = 37 * result + attributeType.hashCode();
        }

        return result;
    }


    // ── The Suit Announces Itself on the Comm Channel ────────────────────────
    // When Vader opens a comm channel, the first thing everyone hears is the
    // iconic breathing and then his title and position — immediate identification.
    // toString() gives the TreeViewer (and logging tools) a readable label for this node.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this wrapper for debugging and display.
     * Uses the NLS message bundle to format a string that includes the wrapped
     * {@link AttributeType} and its parent node — useful in log output and
     * toString()-based tree diagnostics.
     *
     * <p>For example — Vader identifying himself and his position over the comm:</p>
     * <pre>
     *   System.out.println( wrapper );
     *   // "AttributeTypeWrapper[cn, parent=coreSchema]"
     * </pre>
     *
     * @return  a formatted string describing this wrapper and its parent context
     */
    public String toString()
    {
        return NLS.bind(
            Messages.getString( "AttributeTypeWrapper.AttributeTypeWrapper" ), new Object[] { attributeType, fParent } ); //$NON-NLS-1$
    }
}
