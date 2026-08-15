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


import org.apache.directory.studio.schemaeditor.model.Schema;
import org.eclipse.osgi.util.NLS;


// ── CLASS: SchemaWrapper — Leia's Hologram Message to Obi-Wan ────────────────
// Aboard the Tantive IV, Princess Leia records herself into R2-D2 — an urgent
// message destined for Obi-Wan Kenobi. The hologram is not Leia herself; it is
// a representation of her, packaged in a form that R2 can carry and project
// wherever he goes, so anyone with the right projector can see and understand her.
// SchemaWrapper is that hologram: the real {@link Schema} object (Leia) is sealed
// inside, and the wrapper (R2's projection unit) presents it to the JFace TreeViewer
// in a form the UI can navigate, compare, and display as a tree row.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Adapts a {@link Schema} so it can live as a top-level node in the Schema Editor's TreeViewer.
 * A {@link Schema} is a named container of attribute types and object classes — it knows
 * nothing about being a tree node, having a parent, or producing a hash code for JFace.
 * We seal it inside this wrapper, which adds all of that infrastructure so the TreeViewer
 * can expand the schema row to reveal its attribute types and object classes underneath.
 * Think of it as Leia's hologram: the rich schema definition (Leia) is inside, and the
 * wrapper (the projector) delivers it in exactly the format the viewer needs.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaWrapper extends AbstractTreeNode
{
    /** The wrapped Schema */
    private Schema schema;


    // ── Leia Records the Hologram — Not Yet Assigned a Recipient ─────────────
    // Leia activates the recording unit and speaks her message into R2, but at
    // this moment she does not know which Rebel base R2 will end up at —
    // no "parent" destination set yet, just the message sealed inside.
    // This constructor creates a root-level SchemaWrapper with no parent.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a root-level wrapper around the given {@link Schema} with no parent node.
     * Use this when the schema sits at the top of the tree and has no parent to navigate to —
     * for example when it is directly under a SchemaViewRoot.
     *
     * <p>For example — Leia recording the hologram before a destination is known:</p>
     * <pre>
     *   SchemaWrapper wrapper = new SchemaWrapper( coreSchema );
     *   // wrapper.getParent() == null
     * </pre>
     *
     * @param schema  the {@link Schema} to wrap; must not be {@code null}
     */
    public SchemaWrapper( Schema schema )
    {
        super( null );
        this.schema = schema;
    }


    // ── R2 Carries the Hologram to the Designated Rebel Base ─────────────────
    // R2-D2 arrives at the Lars moisture farm — the hologram now has a "parent"
    // location, a place in the Rebel hierarchy where it is meant to be delivered.
    // This constructor wraps a Schema and positions it under a parent node in the tree.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a wrapper around the given {@link Schema} and positions it under a parent node.
     * Use this when building the full schema tree — the schema sits inside a
     * {@link SchemaViewRoot}, and the parent link lets the viewer navigate upward.
     *
     * <p>For example — R2 arriving at the Lars farm, hologram now assigned to a location:</p>
     * <pre>
     *   SchemaWrapper wrapper = new SchemaWrapper( coreSchema, schemaViewRoot );
     *   // wrapper.getParent() == schemaViewRoot
     * </pre>
     *
     * @param schema  the {@link Schema} to wrap; must not be {@code null}
     * @param parent  the parent {@link TreeNode} in the tree hierarchy
     */
    public SchemaWrapper( Schema schema, TreeNode parent )
    {
        super( parent );
        this.schema = schema;
    }


    // ── Obi-Wan Watches the Hologram and Sees Leia Herself ───────────────────
    // "Help me, Obi-Wan Kenobi — you're my only hope." Obi-Wan watches, and
    // through the hologram he sees the real Leia, the real message, the real Schema.
    // getSchema() hands back the genuine {@link Schema} object to callers who need it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link Schema} held inside this wrapper.
     * Callers that need to work with the actual schema definition — adding attribute
     * types, listing object classes, reading the schema name — use this to get past
     * the wrapper and reach the real model object.
     *
     * <p>For example — Obi-Wan seeing Leia through the hologram projection:</p>
     * <pre>
     *   Schema s = wrapper.getSchema();
     *   String name = s.getSchemaName();  // "core" or "inetorgperson" etc.
     * </pre>
     *
     * @return  the wrapped {@link Schema}; never {@code null} if the wrapper was constructed correctly
     */
    public Schema getSchema()
    {
        return schema;
    }


    // ── Is This the Same Hologram Message? ───────────────────────────────────
    // If two R2 units roll into the room both playing a hologram, the Rebels check:
    // same schema name, same parent base. Only then do they conclude it's one message.
    // equals() calls super first for the parent check, then compares the Schema objects.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if {@code obj} is a {@link SchemaWrapper} that wraps the same
     * {@link Schema} and sits under the same parent node in the tree.
     * We delegate the parent comparison to {@code super.equals()}, then compare
     * the wrapped schemas for structural identity.
     *
     * <p>For example — the Rebels checking that two hologram messages are identical transmissions:</p>
     * <pre>
     *   wrapper1.equals( wrapper2 );
     *   // true only if same Schema AND same parent node
     * </pre>
     *
     * @param obj  the object to compare against
     * @return     {@code true} if the wrappers represent the same schema at the same tree position
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof SchemaWrapper )
        {
            if ( super.equals( obj ) )
            {
                SchemaWrapper sw = ( SchemaWrapper ) obj;

                if ( ( schema != null ) && ( !schema.equals( sw.getSchema() ) ) )
                {
                    return false;
                }

                return true;
            }
        }

        // Default
        return false;
    }


    // ── The Hologram's Unique Transmission Code ───────────────────────────────
    // Every message R2 carries has a unique frequency signature — the combination
    // of Leia's voice encoding and the destination beacon frequency — so the Rebel
    // network can route it to the right base without confusion.
    // hashCode() encodes schema identity + parent position into a single integer.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Computes a hash code combining the parent's hash (from {@code super.hashCode()})
     * with the wrapped {@link Schema}'s own hash code.
     * Must be consistent with {@link #equals}: equal wrappers must produce the same hash.
     *
     * <p>For example — the unique frequency signature of Leia's hologram transmission:</p>
     * <pre>
     *   int hash = wrapper.hashCode();
     *   // 37 * parentHash + schema.hashCode()
     * </pre>
     *
     * @return  a hash code consistent with {@link #equals}
     */
    public int hashCode()
    {
        int result = super.hashCode();

        if ( schema != null )
        {
            result = 37 * result + schema.hashCode();
        }

        return result;
    }


    // ── R2 Identifies the Hologram's Contents When Asked ─────────────────────
    // When the Lars family asks R2 "What message are you carrying?", he plays
    // a brief excerpt — enough for them to know it's from Leia and where she is.
    // toString() gives the viewer and log tools a brief, readable identification.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this wrapper, including the wrapped {@link Schema}
     * and its parent node — useful for logging and debugging tree structure issues.
     *
     * <p>For example — R2 playing a brief excerpt to identify the hologram's origin:</p>
     * <pre>
     *   System.out.println( wrapper );
     *   // "SchemaWrapper[core, parent=schemaViewRoot]"
     * </pre>
     *
     * @return  a formatted string describing this wrapper and its parent context
     */
    public String toString()
    {
        return NLS.bind( Messages.getString( "SchemaWrapper.SchemaWrapper" ), new Object[] { schema, fParent } ); //$NON-NLS-1$
    }
}
