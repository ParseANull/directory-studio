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


import org.apache.directory.studio.schemaeditor.model.schemachecker.SchemaWarning;


// ── CLASS: SchemaWarningWrapper — Obi-Wan Senses a Disturbance in the Force ──
// On the Millennium Falcon, Obi-Wan Kenobi suddenly pauses and closes his eyes.
// Something is wrong — not catastrophic, not a blaster bolt to the chest, but
// a tremor in the Force that demands attention. "I felt a great disturbance," he
// says quietly. It is not the end of the world, but it should not be ignored.
// SchemaWarningWrapper is that moment: a {@link SchemaWarning} is not a hard error
// that breaks everything — it is something worth noticing and addressing, surfaced
// as a leaf node in the Problems View so the developer can decide what to do about it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Wraps a {@link SchemaWarning} so it can be displayed as a leaf node in the
 * Schema Editor's Problems View tree.
 * Schema warnings represent soft inconsistencies — things that do not prevent the
 * schema from loading, but which hint at potential problems: duplicate names,
 * non-standard OID arcs, missing descriptions.
 * Think of it as Obi-Wan's disturbance: not fatal, but something you should sense
 * and act on before it becomes a real problem.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaWarningWrapper extends AbstractTreeNode
{
    /** The wrapped SchemaWarning */
    private SchemaWarning schemaWarning;


    // ── Obi-Wan Senses It Alone, in Silence — No Parent Context Yet ──────────
    // On the Falcon, Obi-Wan feels the disturbance by himself before he tells
    // anyone else — the warning exists in isolation, not yet placed in any
    // larger hierarchy of concerns.
    // This constructor wraps a SchemaWarning as a root-level node with no parent.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a root-level wrapper around the given {@link SchemaWarning} with no parent node.
     * Use this when constructing a warning node outside of a specific tree context —
     * for example during a standalone schema validation scan.
     *
     * <p>For example — Obi-Wan sensing the disturbance alone, before reporting it:</p>
     * <pre>
     *   SchemaWarningWrapper wrapper = new SchemaWarningWrapper( warning );
     *   // wrapper.getParent() == null
     * </pre>
     *
     * @param warning  the {@link SchemaWarning} to wrap; must not be {@code null}
     */
    public SchemaWarningWrapper( SchemaWarning warning )
    {
        super( null );
        schemaWarning = warning;
    }


    // ── Obi-Wan Reports the Disturbance to the Rebel Command Structure ────────
    // Obi-Wan turns to Luke and Han — now the disturbance has a context, a
    // "parent" audience that needs to hear about it and decide what to do.
    // This constructor wraps a SchemaWarning and positions it under a parent node.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a wrapper around the given {@link SchemaWarning} and positions it under
     * a parent node in the Problems View tree.
     * The parent is typically a warnings folder that groups all soft issues together,
     * giving the user context about which part of the schema the warning relates to.
     *
     * <p>For example — Obi-Wan reporting the disturbance to the crew of the Falcon:</p>
     * <pre>
     *   SchemaWarningWrapper wrapper = new SchemaWarningWrapper( warning, warningFolder );
     *   // wrapper.getParent() == warningFolder
     * </pre>
     *
     * @param warning  the {@link SchemaWarning} to wrap; must not be {@code null}
     * @param parent   the parent {@link TreeNode} in the Problems View hierarchy
     */
    public SchemaWarningWrapper( SchemaWarning warning, TreeNode parent )
    {
        super( parent );
        schemaWarning = warning;
    }


    // ── What Exactly Did Obi-Wan Sense? ──────────────────────────────────────
    // Luke presses Obi-Wan: "What did you feel?" Obi-Wan describes it — the nature
    // of the disturbance, what kind of event it was, where in the Force it originated.
    // getSchemaWarning() hands back the raw warning so callers can read its message.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link SchemaWarning} held inside this wrapper.
     * Callers — typically the Problems View's label provider — use this to extract
     * the warning's human-readable message and display it next to the warning icon.
     *
     * <p>For example — Luke asking Obi-Wan to describe what he sensed:</p>
     * <pre>
     *   SchemaWarning warning = wrapper.getSchemaWarning();
     *   String message = warning.getMessage();  // "Attribute type has no description"
     * </pre>
     *
     * @return  the wrapped {@link SchemaWarning}; never {@code null} if constructed correctly
     */
    public SchemaWarning getSchemaWarning()
    {
        return schemaWarning;
    }


    // ── The Disturbance Doesn't Branch — It's a Single Moment ────────────────
    // Obi-Wan's sense of the disturbance is a single, contained observation —
    // there are no sub-disturbances beneath it, no nested layers of unease.
    // Warning nodes are always leaves in the Problems View tree.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} — schema warning nodes are leaves in the tree.
     * A warning has no sub-nodes beneath it; it is a terminal row in the Problems View.
     * Overriding this saves the viewer from checking the (always empty) children list.
     *
     * <p>For example — the disturbance is one moment, no further branches beneath it:</p>
     * <pre>
     *   warningWrapper.hasChildren();  // always false
     * </pre>
     *
     * @return  {@code false}, always — schema warnings are leaf nodes
     */
    public boolean hasChildren()
    {
        return false;
    }


    // ── Is This the Same Disturbance Obi-Wan Felt? ───────────────────────────
    // If two Jedi both report a disturbance, the Council checks: same tremor,
    // same origin, same magnitude? equals() verifies parent and SchemaWarning identity.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if {@code obj} is a {@link SchemaWarningWrapper} that wraps the same
     * {@link SchemaWarning} and sits under the same parent node in the tree.
     *
     * <p>For example — the Jedi Council confirming two reports describe the same disturbance:</p>
     * <pre>
     *   wrapper1.equals( wrapper2 );
     *   // true only if same SchemaWarning AND same parent node
     * </pre>
     *
     * @param obj  the object to compare against
     * @return     {@code true} if the wrappers represent the same warning at the same tree position
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof SchemaWarningWrapper )
        {
            if ( super.equals( obj ) )
            {
                SchemaWarningWrapper sww = ( SchemaWarningWrapper ) obj;

                if ( ( schemaWarning != null ) && ( !schemaWarning.equals( sww.getSchemaWarning() ) ) )
                {
                    return false;
                }

                return true;
            }
        }

        // Default
        return false;
    }


    // ── Every Disturbance Has a Unique Signature in the Force ─────────────────
    // Master Yoda can tell one tremor from another by its resonance — a unique
    // identifier woven into the fabric of the Force by the specific event that
    // caused it. hashCode() gives each wrapper a numeric version of that signature.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Computes a hash code combining the parent's hash (from {@code super.hashCode()})
     * with the wrapped {@link SchemaWarning}'s hash.
     * Consistent with {@link #equals}: equal wrappers produce identical hash codes.
     *
     * <p>For example — Yoda identifying a disturbance by its unique Force resonance:</p>
     * <pre>
     *   int hash = wrapper.hashCode();
     *   // 37 * parentHash + schemaWarning.hashCode()
     * </pre>
     *
     * @return  a hash code consistent with {@link #equals}
     */
    public int hashCode()
    {
        int result = super.hashCode();

        if ( schemaWarning != null )
        {
            result = 37 * result + schemaWarning.hashCode();
        }

        return result;
    }
}
