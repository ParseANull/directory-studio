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


// ── CLASS: SchemaHandlerAdapter — C-3PO TRANSLATING FOR R2-D2 ───────────────
// C-3PO knows every protocol and every language, so when R2-D2 broadcasts a
// signal on one of fifteen channels, C-3PO provides a default, courteous
// response for every channel — even the ones nobody currently cares about.
// This adapter gives you no-op implementations of all fifteen SchemaHandler-
// Listener methods so you only override the one or two events you need,
// without the compiler forcing you to stub out the rest.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Convenience abstract base class providing empty default implementations of
 * all SchemaHandlerListener methods.
 * Subclass this and override only the event callbacks you care about; the rest
 * do nothing rather than forcing you to write fifteen empty method bodies.
 * Think of this class as C-3PO — always politely ready on every frequency,
 * even when the only correct response is dignified silence.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class SchemaHandlerAdapter implements SchemaHandlerListener
{
    // ── C-3PO Nods At The New Attribute Type ─────────────────────────────────
    // R2 beeps "new attribute type added"; C-3PO inclines his golden head
    // with practiced diplomacy and says nothing — default silence.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default no-op — override to react when an attribute type is added.
     *
     * @param at  the newly added attribute type
     */
    public void attributeTypeAdded( AttributeType at )
    {
    }


    // ── C-3PO Registers The Modification Politely ────────────────────────────
    // R2 whistles a modification signal; C-3PO nods with the same courteous
    // silence — no action needed unless a subclass says otherwise.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default no-op — override to react when an attribute type is modified.
     *
     * @param at  the modified attribute type
     */
    public void attributeTypeModified( AttributeType at )
    {
    }


    // ── C-3PO Notes The Removal With A Polite Nod ────────────────────────────
    // R2 signals a removal; C-3PO files it under "acknowledged" and waits
    // in polite, golden silence unless asked to do more.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default no-op — override to react when an attribute type is removed.
     *
     * @param at  the removed attribute type
     */
    public void attributeTypeRemoved( AttributeType at )
    {
    }


    // ── C-3PO Acknowledges The New Matching Rule ──────────────────────────────
    // A new matching rule has arrived on the diplomatic frequency; C-3PO logs
    // it and waits for a subclass to give it meaning.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default no-op — override to react when a matching rule is added.
     *
     * @param mr  the newly added matching rule
     */
    public void matchingRuleAdded( MatchingRule mr )
    {
    }


    // ── C-3PO Files The Matching Rule Change ─────────────────────────────────
    // The modification signal comes in on the matching-rule channel; C-3PO
    // logs it and does nothing further unless overridden.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default no-op — override to react when a matching rule is modified.
     *
     * @param mr  the modified matching rule
     */
    public void matchingRuleModified( MatchingRule mr )
    {
    }


    // ── C-3PO Acknowledges The Removal With A Bow ────────────────────────────
    // The matching rule departs; C-3PO offers a courteous farewell and
    // returns to diplomatic standby.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default no-op — override to react when a matching rule is removed.
     *
     * @param mr  the removed matching rule
     */
    public void matchingRuleRemoved( MatchingRule mr )
    {
    }


    // ── C-3PO Greets The New Object Class ────────────────────────────────────
    // "How do you do?" — C-3PO greets the new object class with impeccable
    // manners and then waits in silence until a subclass has something to say.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default no-op — override to react when an object class is added.
     *
     * @param oc  the newly added object class
     */
    public void objectClassAdded( ObjectClass oc )
    {
    }


    // ── C-3PO Notes The Change In Protocol ───────────────────────────────────
    // The object class's definition has shifted; C-3PO updates his mental
    // register and waits politely unless a subclass handles it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default no-op — override to react when an object class is modified.
     *
     * @param oc  the modified object class
     */
    public void objectClassModified( ObjectClass oc )
    {
    }


    // ── C-3PO Bids Farewell To The Departing Class ───────────────────────────
    // The object class exits the diplomatic chamber; C-3PO bows graciously
    // and files it under "departed" — unless overridden.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default no-op — override to react when an object class is removed.
     *
     * @param oc  the removed object class
     */
    public void objectClassRemoved( ObjectClass oc )
    {
    }


    // ── C-3PO Welcomes A Whole New Delegation ────────────────────────────────
    // An entire delegation arrives at the Senate — a whole schema with all its
    // members.  C-3PO bows to the lead delegate and waits for a subclass to act.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default no-op — override to react when an entire schema is added.
     *
     * @param schema  the newly added schema
     */
    public void schemaAdded( Schema schema )
    {
    }


    // ── C-3PO Logs The Delegation's Departure ────────────────────────────────
    // The whole delegation leaves the Senate floor; C-3PO notes their exit
    // with a dignified nod — unless a subclass has cleanup to do.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default no-op — override to react when an entire schema is removed.
     *
     * @param schema  the schema that was removed
     */
    public void schemaRemoved( Schema schema )
    {
    }


    // ── C-3PO Updates The Delegation's Name Badge ────────────────────────────
    // The delegation's official name has changed in the Senate register;
    // C-3PO updates his own records and waits in courteous silence.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default no-op — override to react when a schema is renamed.
     *
     * @param schema  the schema that was renamed
     */
    public void schemaRenamed( Schema schema )
    {
    }


    // ── C-3PO Acknowledges The New Syntax ────────────────────────────────────
    // A new language protocol has been registered; C-3PO adds it to his
    // internal linguistic database and waits unless overridden.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default no-op — override to react when a syntax is added.
     *
     * @param syntax  the newly added syntax
     */
    public void syntaxAdded( LdapSyntax syntax )
    {
    }


    // ── C-3PO Notes The Syntax Update ────────────────────────────────────────
    // The language protocol has been revised; C-3PO updates his records and
    // responds with characteristic golden silence — unless overridden.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default no-op — override to react when a syntax is modified.
     *
     * @param syntax  the syntax that was modified
     */
    public void syntaxModified( LdapSyntax syntax )
    {
    }


    // ── C-3PO Marks The Syntax As Deprecated ─────────────────────────────────
    // The language protocol has been struck from the register; C-3PO archives
    // it with his usual thoroughness — unless a subclass needs to do more.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default no-op — override to react when a syntax is removed.
     *
     * @param syntax  the syntax that was removed
     */
    public void syntaxRemoved( LdapSyntax syntax )
    {
    }
}
