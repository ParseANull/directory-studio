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
package org.apache.directory.studio.schemaeditor.model.io;


import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaObjectRenderer;
import org.apache.directory.api.ldap.model.schema.SchemaObjectSorter;
import org.apache.directory.studio.schemaeditor.model.Schema;


// ── CLASS: OpenLdapSchemaFileExporter — Yoda Lifting Luke's X-Wing ───────────
// Luke's X-wing is buried in the Dagobah swamp; it exists, it's real, but it's
// in a form nobody can use.  Yoda concentrates, uses the Force, and raises it
// clear of the murk — transforming it from "sunken object" to "flyable ship"
// without changing a single bolt.  OpenLdapSchemaFileExporter does the same:
// it takes a Schema object (the X-wing buried in Java memory) and lifts it into
// OpenLDAP .schema text format — the form that slapd and other tools understand.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Converts in-memory {@link Schema} objects into OpenLDAP {@code .schema} file
 * format source code, ready to be written to disk or sent over the network.
 * We delegate the actual rendering to {@link SchemaObjectRenderer#OPEN_LDAP_SCHEMA_RENDERER}
 * and use {@link SchemaObjectSorter} to emit attribute types and object classes
 * in a topological order that respects inheritance.
 * Think of this class as Yoda: it takes something valuable that's in the wrong
 * form (Java objects) and transforms it into something usable (text) without
 * altering the underlying reality.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapSchemaFileExporter
{
    // ── Yoda Lifts the Entire X-Wing Fleet from the Swamp ────────────────────
    // Yoda doesn't just lift one wing — he raises the whole ship, properly
    // oriented, from nose to tail.  We emit attribute types first (sorted
    // hierarchically so supertypes precede subtypes), then object classes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts a whole Schema to its OpenLDAP {@code .schema} file representation.
     * Attribute types are emitted in hierarchical order; object classes follow.
     * The resulting string can be written directly to a {@code .schema} file.
     *
     * @param schema  the schema to convert — must not be null
     * @return        the complete OpenLDAP schema file content as a String
     */
    public static String toSourceCode( Schema schema )
    {
        StringBuffer sb = new StringBuffer();

        for ( AttributeType at : SchemaObjectSorter.hierarchicalOrdered( schema.getAttributeTypes() ) )
        {
            sb.append( toSourceCode( at ) );
            sb.append( "\n" ); //$NON-NLS-1$
        }

        for ( ObjectClass oc : SchemaObjectSorter.sortObjectClasses( schema.getObjectClasses() ) )
        {
            sb.append( toSourceCode( oc ) );
            sb.append( "\n" ); //$NON-NLS-1$
        }

        return sb.toString();
    }


    // ── Yoda Lifts a Single Wing Panel ───────────────────────────────────────
    // Just one component of the ship — an attribute type — raised from the
    // swamp into its proper OpenLDAP attributeType directive form.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts a single {@link AttributeType} to its OpenLDAP schema directive string.
     * The output looks like {@code attributeType ( 2.5.4.3 NAME 'cn' ... )}.
     *
     * @param at  the attribute type to convert — must not be null
     * @return    the OpenLDAP attributeType directive as a String
     */
    public static String toSourceCode( AttributeType at )
    {
        return SchemaObjectRenderer.OPEN_LDAP_SCHEMA_RENDERER.render( at );
    }


    // ── Yoda Lifts an Object-Class Hull Section ───────────────────────────────
    // Another component — this time an object class — raised into its proper
    // OpenLDAP objectClass directive form.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts a single {@link ObjectClass} to its OpenLDAP schema directive string.
     * The output looks like {@code objectClass ( 2.5.6.0 NAME 'top' ... )}.
     *
     * @param oc  the object class to convert — must not be null
     * @return    the OpenLDAP objectClass directive as a String
     */
    public static String toSourceCode( ObjectClass oc )
    {
        return SchemaObjectRenderer.OPEN_LDAP_SCHEMA_RENDERER.render( oc );
    }
}
