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
package org.apache.directory.studio.schemaeditor.controller;


import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.model.Schema;


// ── CLASS: SchemaHandlerListener — OBI-WAN SENSES A DISTURBANCE ─────────────
// Aboard the Falcon, Obi-Wan suddenly closes his eyes: the Force ripples
// around him as events cascade — a world collapses, a rule changes, a new
// presence emerges.  He catalogues each disturbance precisely: attribute type
// added here, object class removed there, schema renamed somewhere else.
// This interface lets components register that same Force-awareness for every
// kind of schema mutation the SchemaHandler can produce.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Callback interface for receiving fine-grained schema mutation events.
 * Implementors hear about every add, modify, and remove on attribute types,
 * object classes, matching rules, syntaxes, and schemas themselves.
 * Think of this as Obi-Wan's ability to feel every disturbance in the Force —
 * register once and the changes come to you, precisely categorized.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface SchemaHandlerListener
{
    // ── Obi-Wan Feels A New Attribute Arrive ─────────────────────────────────
    // Obi-Wan opens his eyes slightly: a new presence has joined the Force.
    // An attribute type has been added to the active schema — listeners should
    // update any view or index that tracks attribute types.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when an attribute type is added to the schema.
     * Refresh any view or data structure that maintains a list of attribute types.
     *
     * @param at  the newly added attribute type
     */
    void attributeTypeAdded( AttributeType at );


    // ── Obi-Wan Senses A Shift In A Known Presence ───────────────────────────
    // Something familiar has changed — not gone, just different: an attribute
    // type has been modified.  Listeners should re-read the type's properties
    // and repaint any cached display of it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when an existing attribute type is modified.
     * Update any cached representation of this attribute type in views or indices.
     *
     * @param at  the attribute type that was modified
     */
    void attributeTypeModified( AttributeType at );


    // ── Obi-Wan Feels A Presence Disappear ───────────────────────────────────
    // A familiar ripple in the Force suddenly goes quiet — an attribute type
    // has been removed.  Listeners should drop any reference to it and refresh.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when an attribute type is removed from the schema.
     * Drop any references to this attribute type and refresh affected views.
     *
     * @param at  the attribute type that was removed
     */
    void attributeTypeRemoved( AttributeType at );


    // ── Obi-Wan Recognizes A New Matching Rule Emerge ────────────────────────
    // A new, structured pattern emerges in the Force — a matching rule has
    // arrived.  Views that display matching rules should add it to their list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a matching rule is added to the schema.
     *
     * @param mr  the newly added matching rule
     */
    void matchingRuleAdded( MatchingRule mr );


    // ── Obi-Wan Notes A Pattern Has Changed ──────────────────────────────────
    // The structured pattern shifts — a matching rule has been modified.
    // Listeners should refresh any display that shows this matching rule.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a matching rule is modified.
     *
     * @param mr  the matching rule that was modified
     */
    void matchingRuleModified( MatchingRule mr );


    // ── Obi-Wan Watches A Pattern Dissolve ───────────────────────────────────
    // The structured pattern vanishes — a matching rule has been removed.
    // Listeners should clean up references and refresh.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a matching rule is removed from the schema.
     *
     * @param mr  the matching rule that was removed
     */
    void matchingRuleRemoved( MatchingRule mr );


    // ── Obi-Wan Senses A New Class Of Being ──────────────────────────────────
    // A new family of life has joined the galaxy — an object class has been
    // added.  Listeners displaying object classes should add it immediately.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when an object class is added to the schema.
     * Refresh any view or index that tracks object classes.
     *
     * @param oc  the newly added object class
     */
    void objectClassAdded( ObjectClass oc );


    // ── Obi-Wan Watches A Class Evolve ───────────────────────────────────────
    // The nature of a known class of being has shifted — an object class was
    // modified.  Listeners should re-read its definition and repaint.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when an existing object class is modified.
     * Update any cached representation of this object class in views or indices.
     *
     * @param oc  the object class that was modified
     */
    void objectClassModified( ObjectClass oc );


    // ── Obi-Wan Watches A Class Vanish ───────────────────────────────────────
    // An entire class of being has been wiped from the galaxy — the object class
    // is gone.  Listeners must drop references and refresh their views.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when an object class is removed from the schema.
     * Drop any references to this object class and refresh affected views.
     *
     * @param oc  the object class that was removed
     */
    void objectClassRemoved( ObjectClass oc );


    // ── Obi-Wan Senses A Whole Galaxy Added ──────────────────────────────────
    // An entirely new star system has joined the Republic — a whole schema,
    // with all its types and rules, has been added at once.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when an entire schema is added (including all its contained types).
     * Listeners should refresh their full schema tree or list.
     *
     * @param schema  the newly added schema
     */
    void schemaAdded( Schema schema );


    // ── Obi-Wan Feels A Star System Go Dark ──────────────────────────────────
    // A whole star system has been removed from the galactic map — an entire
    // schema is gone.  Listeners should purge all references and refresh.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when an entire schema is removed.
     * Listeners should remove all UI representations and drop cached references.
     *
     * @param schema  the schema that was removed
     */
    void schemaRemoved( Schema schema );


    // ── Obi-Wan Notices A Star System Has A New Name ─────────────────────────
    // The galactic registry has updated a star system's official name — a
    // schema has been renamed.  Listeners should refresh any label showing it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a schema is renamed.
     * Refresh any UI element that displays the schema's name.
     *
     * @param schema  the schema that was renamed
     */
    void schemaRenamed( Schema schema );


    // ── Obi-Wan Senses A New Syntax Crystallize ──────────────────────────────
    // A new law of language has been codified in the Republic — a syntax has
    // been added to the schema.  Listeners should add it to their displays.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a syntax is added to the schema.
     *
     * @param syntax  the newly added syntax
     */
    void syntaxAdded( LdapSyntax syntax );


    // ── Obi-Wan Notices A Syntax Shift ───────────────────────────────────────
    // The law of language has changed slightly — a syntax has been modified.
    // Listeners should refresh any view showing this syntax's definition.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a syntax is modified.
     *
     * @param syntax  the syntax that was modified
     */
    void syntaxModified( LdapSyntax syntax );


    // ── Obi-Wan Watches A Syntax Dissolve ────────────────────────────────────
    // A law of language has been struck from the books — the syntax has been
    // removed.  Listeners should drop references and refresh.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a syntax is removed from the schema.
     *
     * @param syntax  the syntax that was removed
     */
    void syntaxRemoved( LdapSyntax syntax );
}
