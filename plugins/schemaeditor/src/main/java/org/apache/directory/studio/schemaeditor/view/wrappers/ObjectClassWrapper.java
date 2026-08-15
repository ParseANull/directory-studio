/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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


import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.eclipse.osgi.util.NLS;


// ── CLASS: ObjectClassWrapper — C-3PO Translating for the Rebel Alliance ─────
// In the Rebels' war room, C-3PO stands between Princess Leia and the Jawas,
// converting alien beeps and chirps into fluent Basic so the Alliance can act on
// the information without needing to understand Jawa at all.
// ObjectClassWrapper plays the same role: an {@link ObjectClass} speaks the language
// of LDAP — OIDs, must-have attributes, may-have attributes — but the JFace
// TreeViewer only speaks "tree nodes." C-3PO steps in, wraps the ObjectClass, and
// hands the viewer something it can display in a row, expand, and compare.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Adapts an {@link ObjectClass} so it can appear as a node in the Schema Editor's JFace TreeViewer.
 * An {@link ObjectClass} carries all the LDAP schema definition details (OID, superclasses,
 * required and optional attributes) but nothing about tree position or display identity.
 * We wrap it here, adding parent-child navigation and the equals/hashCode/toString contract
 * that JFace needs to render and compare rows in a tree.
 * Think of it as C-3PO translating between LDAP's object-class vocabulary and the TreeViewer's
 * node-based UI language.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ObjectClassWrapper extends AbstractTreeNode
{
    /** The wrapped ObjectClass */
    private ObjectClass objectClass;


    // ── C-3PO Steps Up Without a Commanding Officer Yet ──────────────────────
    // C-3PO reports to the Rebel base, fluent in six million forms of communication,
    // but not yet assigned to a particular mission or command chain.
    // This constructor wraps an ObjectClass as a root-level node with no parent.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a root-level wrapper around the given {@link ObjectClass} with no parent node.
     * Use this when displaying an object class in a flat list rather than inside a schema folder.
     *
     * <p>For example — C-3PO announcing his capabilities at the Rebel base, unassigned:</p>
     * <pre>
     *   ObjectClassWrapper wrapper = new ObjectClassWrapper( person );
     *   // wrapper.getParent() == null — top-level, no folder above it
     * </pre>
     *
     * @param oc  the {@link ObjectClass} to wrap; must not be {@code null}
     */
    public ObjectClassWrapper( ObjectClass oc )
    {
        super( null );
        objectClass = oc;
    }


    // ── C-3PO Takes His Station Beside Princess Leia ─────────────────────────
    // In the Rebel briefing room, C-3PO stands at Leia's side — assigned to her
    // command, part of a larger structure, ready to translate within that hierarchy.
    // This constructor wraps an ObjectClass and places it under a parent tree node.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a wrapper around the given {@link ObjectClass} and positions it under a parent node.
     * Use this when an object class lives inside a schema folder in the tree —
     * the parent link lets the TreeViewer navigate upward.
     *
     * <p>For example — C-3PO assigned to Leia's command on the Tantive IV:</p>
     * <pre>
     *   ObjectClassWrapper wrapper = new ObjectClassWrapper( person, schemaFolder );
     *   // wrapper.getParent() == schemaFolder
     * </pre>
     *
     * @param oc      the {@link ObjectClass} to wrap; must not be {@code null}
     * @param parent  the parent {@link TreeNode} in the tree hierarchy
     */
    public ObjectClassWrapper( ObjectClass oc, TreeNode parent )
    {
        super( parent );
        objectClass = oc;
    }


    // ── C-3PO Reads the Original Jawa Script Directly ────────────────────────
    // When the Rebels need the raw data — the actual Jawa dialect, not C-3PO's
    // translation — they ask him to hand over the data scroll itself.
    // getObjectClass() does the same: it gives callers the underlying LDAP model object.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ObjectClass} held inside this wrapper.
     * Callers that need to inspect the actual LDAP schema definition — OID, superclass
     * chain, required attributes, optional attributes — use this to reach past the
     * wrapper and work with the raw model object directly.
     *
     * <p>For example — the Rebels asking C-3PO for the original data scroll:</p>
     * <pre>
     *   ObjectClass oc = wrapper.getObjectClass();
     *   String oid = oc.getOid();  // "2.5.6.6" for the 'person' object class
     * </pre>
     *
     * @return  the wrapped {@link ObjectClass}; never {@code null} if the wrapper was constructed correctly
     */
    public ObjectClass getObjectClass()
    {
        return objectClass;
    }


    // ── Are These Two C-3POs from the Same Mission? ───────────────────────────
    // If two golden droids appear at the briefing table, the Rebels check:
    // same mission assignment (parent), same object-class definition.
    // Only then do they conclude it's the same droid at the same post.
    // equals() delegates parent comparison to super, then checks the ObjectClass.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if {@code obj} is an {@link ObjectClassWrapper} that wraps the same
     * {@link ObjectClass} and sits under the same parent node.
     * We call {@code super.equals()} first to handle parent comparison, then compare
     * the wrapped object classes for structural identity.
     *
     * <p>For example — the Alliance checking that two golden droids are the same unit at the same post:</p>
     * <pre>
     *   wrapper1.equals( wrapper2 );
     *   // true only if same ObjectClass AND same parent node
     * </pre>
     *
     * @param obj  the object to compare against
     * @return     {@code true} if the wrappers represent the same object class at the same tree position
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof ObjectClassWrapper )
        {
            if ( super.equals( obj ) )
            {
                ObjectClassWrapper ocw = ( ObjectClassWrapper ) obj;

                if ( ( objectClass != null ) && ( !objectClass.equals( ocw.getObjectClass() ) ) )
                {
                    return false;
                }

                return true;
            }
        }

        // Default
        return false;
    }


    // ── C-3PO's Unique Serial Number ─────────────────────────────────────────
    // Every droid rolling off the Cybot Galactica production line gets a unique
    // serial number etched into its chassis — distinguishing one C-3PO unit from
    // another across the entire galaxy.
    // hashCode() gives each wrapper a numeric fingerprint based on parent and ObjectClass.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Computes a hash code that combines the parent's hash (from {@code super.hashCode()})
     * with the wrapped {@link ObjectClass}'s own hash code.
     * Must be consistent with {@link #equals}: two equal wrappers must produce the same hash.
     *
     * <p>For example — the serial number etched into C-3PO's chassis identifies his model and assignment:</p>
     * <pre>
     *   int hash = wrapper.hashCode();
     *   // 37 * parentHash + objectClass.hashCode()
     * </pre>
     *
     * @return  a hash code consistent with {@link #equals}
     */
    public int hashCode()
    {
        int result = super.hashCode();

        if ( objectClass != null )
        {
            result = 37 * result + objectClass.hashCode();
        }

        return result;
    }


    // ── C-3PO Introduces Himself Over the Comm ───────────────────────────────
    // "I am C-3PO, human-cyborg relations, fluent in over six million forms of
    // communication." When asked to identify himself, C-3PO gives a clear, full
    // self-description that includes his role and context.
    // toString() does the same for this wrapper: a readable label for logs and debuggers.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this wrapper, including the wrapped
     * {@link ObjectClass} and its parent node.
     * Useful for debugging tree structure issues and for tools that call toString()
     * on tree nodes to produce labels.
     *
     * <p>For example — C-3PO introducing himself with full context:</p>
     * <pre>
     *   System.out.println( wrapper );
     *   // "ObjectClassWrapper[person, parent=coreSchema]"
     * </pre>
     *
     * @return  a formatted string describing this wrapper and its parent context
     */
    public String toString()
    {
        return NLS.bind(
            Messages.getString( "ObjectClassWrapper.ObjectWrapperClass" ), new Object[] { objectClass, fParent } ); //$NON-NLS-1$
    }
}
