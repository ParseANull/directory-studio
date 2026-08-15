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


import org.apache.directory.api.ldap.model.exception.LdapSchemaException;


// ── CLASS: SchemaErrorWrapper — Han Shoots Greedo in the Mos Eisley Cantina ──
// Greedo has Han dead to rights in the cantina booth — the blaster is drawn,
// the threat is real, and there is zero room for hesitation. Han acts first,
// decisively and without ceremony, neutralising the danger before it can
// propagate further down the evening.
// SchemaErrorWrapper does the same: it intercepts a live {@link LdapSchemaException}
// — a hard, fatal schema inconsistency — wraps it in a tree node, and puts it
// squarely in front of the user in the Problems View so they cannot miss it.
// No ambiguity, no children, just the error standing alone on the table.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Wraps an {@link LdapSchemaException} so it can be displayed as a leaf node
 * in the Schema Editor's Problems View tree.
 * Schema errors are fatal inconsistencies — for example, an attribute type that
 * references a non-existent syntax OID. We surface them here as tree nodes
 * (always leaves — no children) so the user sees exactly what is broken and where.
 * Think of this as Han's decisive shot: the error is put in plain sight and handled
 * immediately, rather than being buried somewhere in the schema model.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaErrorWrapper extends AbstractTreeNode
{
    /** The wrapper {@link LdapSchemaException} */
    private LdapSchemaException ldapSchemaException;


    // ── Han Spots the Threat Before Greedo Can Act — No Parent Context ────────
    // Han clocks Greedo the moment he sits down — the danger is self-contained,
    // it belongs to this moment in this booth, with no higher command structure.
    // This constructor wraps a schema error as a root-level node (parent = null).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a root-level wrapper around the given {@link LdapSchemaException} with no parent.
     * Use this when constructing an error node that is not yet placed in a tree hierarchy —
     * for example during a standalone schema validation pass.
     *
     * <p>For example — Han sizing up the threat before a seat at the table is even chosen:</p>
     * <pre>
     *   SchemaErrorWrapper wrapper = new SchemaErrorWrapper( schemaException );
     *   // wrapper.getParent() == null
     * </pre>
     *
     * @param ldapSchemaException  the schema error to wrap; must not be {@code null}
     */
    public SchemaErrorWrapper( LdapSchemaException ldapSchemaException )
    {
        super( null );
        this.ldapSchemaException = ldapSchemaException;
    }


    // ── Han Acts — The Error Node Takes Its Place Under a Parent ─────────────
    // In the cantina, that shot has consequences that ripple up: the bar owner
    // knows, the Empire knows. The error has a "parent" — a schema element or
    // folder it belongs to in the Problems View hierarchy.
    // This constructor wraps a schema error and places it under a parent tree node.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a wrapper around the given {@link LdapSchemaException} and positions it
     * under a parent node in the Problems View tree.
     * The parent is typically a folder or schema wrapper that groups related errors together,
     * giving the user context about which schema element is broken.
     *
     * <p>For example — the cantina incident logged under the Mos Eisley district record:</p>
     * <pre>
     *   SchemaErrorWrapper wrapper = new SchemaErrorWrapper( schemaException, errorFolder );
     *   // wrapper.getParent() == errorFolder
     * </pre>
     *
     * @param ldapSchemaException  the schema error to wrap; must not be {@code null}
     * @param parent               the parent {@link TreeNode} in the Problems View hierarchy
     */
    public SchemaErrorWrapper( LdapSchemaException ldapSchemaException, TreeNode parent )
    {
        super( parent );
        this.ldapSchemaException = ldapSchemaException;
    }


    // ── Examining the Blaster Bolt — What Exactly Went Wrong? ────────────────
    // The cantina bouncer examines the aftermath — what exactly happened, who fired,
    // what charge was used, what the trajectory was.
    // getLdapSchemaException() hands back the raw exception so callers can read the
    // error message, the offending element, and the reason the schema is broken.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link LdapSchemaException} held inside this wrapper.
     * Callers — typically the Problems View's label provider — use this to extract
     * the human-readable error message and display it next to the error icon in the tree.
     *
     * <p>For example — the bouncer reading the incident report after Han's shot:</p>
     * <pre>
     *   LdapSchemaException ex = wrapper.getLdapSchemaException();
     *   String message = ex.getMessage();  // "Syntax OID 1.3.6.1.4.1.1466.115.121.1.99 not found"
     * </pre>
     *
     * @return  the wrapped {@link LdapSchemaException}; never {@code null} if constructed correctly
     */
    public LdapSchemaException getLdapSchemaException()
    {
        return ldapSchemaException;
    }


    // ── The Blaster Bolt Stops Here — No Further Consequences ────────────────
    // The incident in the cantina booth is contained — it doesn't branch out into
    // sub-incidents, sub-investigations, or nested error structures. It's done.
    // Error nodes are always leaves in the Problems View — they cannot have children.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} — schema error nodes are leaves in the tree.
     * An error itself cannot contain sub-nodes; it is a terminal row in the Problems View.
     * This overrides {@link AbstractTreeNode#hasChildren()} to save the viewer from
     * even checking the (always empty) children list.
     *
     * <p>For example — the cantina incident has no sub-incidents beneath it:</p>
     * <pre>
     *   errorWrapper.hasChildren();  // always false
     * </pre>
     *
     * @return  {@code false}, always — schema errors are leaf nodes
     */
    public boolean hasChildren()
    {
        return false;
    }


    // ── Is This the Same Shot That Was Fired? ─────────────────────────────────
    // Two witnesses both report the incident — but are they describing the same
    // blaster bolt? The investigators compare every detail of the exception object
    // and the parent context to confirm.
    // equals() checks parent (via super) then the LdapSchemaException identity.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if {@code obj} is a {@link SchemaErrorWrapper} that wraps the same
     * {@link LdapSchemaException} and sits under the same parent node.
     *
     * <p>For example — two witnesses confirming it was the same cantina incident:</p>
     * <pre>
     *   wrapper1.equals( wrapper2 );
     *   // true only if same LdapSchemaException AND same parent node
     * </pre>
     *
     * @param obj  the object to compare against
     * @return     {@code true} if the wrappers represent the same error at the same tree position
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof SchemaErrorWrapper )
        {
            if ( super.equals( obj ) )
            {
                SchemaErrorWrapper sww = ( SchemaErrorWrapper ) obj;

                if ( ( ldapSchemaException != null ) && ( !ldapSchemaException.equals( sww.getLdapSchemaException() ) ) )
                {
                    return false;
                }

                return true;
            }
        }

        // Default
        return false;
    }


    // ── The Incident's Unique Case Number ─────────────────────────────────────
    // Every cantina incident gets a unique case number in the Imperial records —
    // combining the location (parent) and the specifics of what occurred (exception).
    // hashCode() encodes both into a single integer for use in collections.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Computes a hash code combining the parent's hash (from {@code super.hashCode()})
     * with the wrapped {@link LdapSchemaException}'s hash.
     * Consistent with {@link #equals} — equal wrappers produce the same hash.
     *
     * <p>For example — the Imperial case number combining location and incident details:</p>
     * <pre>
     *   int hash = wrapper.hashCode();
     *   // 37 * parentHash + ldapSchemaException.hashCode()
     * </pre>
     *
     * @return  a hash code consistent with {@link #equals}
     */
    public int hashCode()
    {
        int result = super.hashCode();

        if ( ldapSchemaException != null )
        {
            result = 37 * result + ldapSchemaException.hashCode();
        }

        return result;
    }
}
