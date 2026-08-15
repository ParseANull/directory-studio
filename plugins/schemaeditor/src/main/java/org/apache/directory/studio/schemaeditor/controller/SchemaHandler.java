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


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.schemaeditor.model.Schema;


// ── CLASS: SchemaHandler — LANDO RUNNING CLOUD CITY ─────────────────────────
// Lando Calrissian doesn't just run the docking bays — he oversees the entire
// economy of Cloud City: the gas mining platforms (schemas), the resident
// guilds (attribute types, object classes), the legal codes (matching rules),
// and the city's communication protocols (syntaxes).  When anything in the
// city changes, Lando broadcasts it on the appropriate channel.
// This class is that entire administration: it maintains the full schema
// registry across five dimensions and notifies every listener on any change.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Central registry for all schema objects in the currently active project.
 * We maintain five parallel data structures — list + multi-valued map — for
 * schemas, attribute types, object classes, matching rules, and syntaxes,
 * and we notify registered SchemaHandlerListeners after every mutation.
 * Think of this class as Lando — we run the whole city, track every resident,
 * and broadcast announcements the instant anything changes.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaHandler
{
    //
    // The Lists
    //
    /** The schemas List */
    private List<Schema> schemasList;
    /** The attribute types List */
    private List<AttributeType> attributeTypesList;
    /** The matching rules List */
    private List<MatchingRule> matchingRulesList;
    /** The object classes List */
    private List<ObjectClass> objectClassesList;
    /** The syntaxes List */
    private List<LdapSyntax> syntaxesList;

    //
    // The MultiMap (for fast searching)
    //
    /** The schemas MultiMap */
    private MultiValuedMap<String, Schema> schemasMap;
    /** The attribute types MultiMap */
    private MultiValuedMap<String, AttributeType> attributeTypesMap;
    /** The matching rules MultiMap */
    private MultiValuedMap<String, MatchingRule> matchingRulesMap;
    /** The object classes MultiMap */
    private MultiValuedMap<String, ObjectClass> objectClassesMap;
    /** The syntaxes MultiMap */
    private MultiValuedMap<String, LdapSyntax> syntaxesMap;

    //
    // The Listeners Lists
    //
    private List<SchemaHandlerListener> schemaHandlerListeners;


    // ── Lando Opens The City's Data Center ───────────────────────────────────
    // Before the first ship lands on Cloud City's platform, someone has to
    // initialize the gas-mining ledger, the resident registry, the legal
    // codebook, the comm directory, and the announcement subscriber list.
    // We set up five lists and five multi-maps so every schema object type
    // has both an ordered view (the list) and a fast-lookup view (the map).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new, empty SchemaHandler with all data structures initialized.
     * Call this once when a project is opened; discard it when the project closes.
     * The multi-maps allow the same object to be found by OID or by any of its
     * aliases — that's why we use multi-valued maps rather than simple hashmaps.
     *
     * <p>For example — Lando opens every ledger before the city comes to life:</p>
     * <pre>
     *   SchemaHandler handler = new SchemaHandler();
     *   // All five lists and five maps are empty and ready to accept schema objects
     * </pre>
     */
    public SchemaHandler()
    {
        // Lists
        schemasList = new ArrayList<Schema>();
        attributeTypesList = new ArrayList<AttributeType>();
        matchingRulesList = new ArrayList<MatchingRule>();;
        objectClassesList = new ArrayList<ObjectClass>();
        syntaxesList = new ArrayList<LdapSyntax>();

        // Maps
        schemasMap = new ArrayListValuedHashMap<>();
        attributeTypesMap = new ArrayListValuedHashMap<>();
        matchingRulesMap = new ArrayListValuedHashMap<>();
        objectClassesMap = new ArrayListValuedHashMap<>();
        syntaxesMap = new ArrayListValuedHashMap<>();

        // Listeners
        schemaHandlerListeners = new ArrayList<SchemaHandlerListener>();
    }


    // ── Lando Reads The Attribute-Type Guild Roster ───────────────────────────
    // The guild master hands Lando the full list of attribute-type residents —
    // every name, every face, in the order they arrived on the city.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full ordered list of all attribute types across all schemas.
     * The list is in insertion order; don't modify it externally.
     *
     * @return  the live attribute types list; never null, may be empty
     */
    public List<AttributeType> getAttributeTypes()
    {
        return attributeTypesList;
    }


    // ── Lando Reads The Matching-Rule Legal Codebook ─────────────────────────
    // Lando flips open the legal codebook and reads every statute currently
    // in force across Cloud City.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full ordered list of all matching rules across all schemas.
     * The list is in insertion order; don't modify it externally.
     *
     * @return  the live matching rules list; never null, may be empty
     */
    public List<MatchingRule> getMatchingRules()
    {
        return matchingRulesList;
    }


    // ── Lando Reads The Object-Class Resident Categories ─────────────────────
    // The census officer hands Lando the list of every resident category on
    // Cloud City — Human, Ugnaught, Besalisk — every object class in order.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full ordered list of all object classes across all schemas.
     * The list is in insertion order; don't modify it externally.
     *
     * @return  the live object classes list; never null, may be empty
     */
    public List<ObjectClass> getObjectClasses()
    {
        return objectClassesList;
    }


    // ── Lando Reads The Full Platform Registry ────────────────────────────────
    // All of Cloud City's gas platforms are logged here — every schema name,
    // every ownership record, in the order the platforms were commissioned.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full ordered list of all schemas.
     * The list is in insertion order; don't modify it externally.
     *
     * @return  the live schemas list; never null, may be empty
     */
    public List<Schema> getSchemas()
    {
        return schemasList;
    }


    // ── Lando Reads The Communications Protocol Directory ────────────────────
    // Every comm protocol in use on Cloud City is logged here — the city
    // couldn't function without knowing what transmission formats are valid.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full ordered list of all syntaxes across all schemas.
     * The list is in insertion order; don't modify it externally.
     *
     * @return  the live syntaxes list; never null, may be empty
     */
    public List<LdapSyntax> getSyntaxes()
    {
        return syntaxesList;
    }


    // ── Lando Looks Up A Resident By ID Badge ────────────────────────────────
    // A visitor asks "where is resident 2.5.4.3?" — Lando flips to that page
    // in the registry and returns the first match.  OID or alias both work.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the first attribute type matching the given OID or alias, or null if not found.
     * The lookup is case-insensitive.  If multiple attribute types share the same
     * OID (a conflict), we silently return the first registered one.
     *
     * <p>For example — Lando looks up resident "cn" or "2.5.4.3" — same person:</p>
     * <pre>
     *   AttributeType cn = handler.getAttributeType( "cn" );
     *   AttributeType cn = handler.getAttributeType( "2.5.4.3" );
     * </pre>
     *
     * @param id  an OID or an alias (case-insensitive)
     * @return    the matching attribute type, or null if none found
     */
    public AttributeType getAttributeType( String id )
    {
        List<?> list = getAttributeTypeList( Strings.toLowerCase( id ) );

        if ( ( list != null ) && ( list.size() >= 1 ) )
        {
            return ( AttributeType ) list.get( 0 );
        }
        else
        {
            return null;
        }
    }


    // ── Lando Looks Up All Residents With That Badge ──────────────────────────
    // In a conflict situation, two residents might share the same ID — Lando
    // returns the full list so the caller can decide what to do with duplicates.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns all attribute types matching the given OID or alias, or an empty collection.
     * Normally there's exactly one, but duplicates can occur in a multi-schema setup
     * where two schemas define the same OID — use this to detect conflicts.
     *
     * @param id  an OID or an alias (case-insensitive)
     * @return    a list of matching attribute types; may be null or empty
     */
    public List<?> getAttributeTypeList( String id )
    {
        return ( List<?> ) attributeTypesMap.get( Strings.toLowerCase( id ) );
    }


    // ── Lando Looks Up A Legal Statute By Code ───────────────────────────────
    // "What does statute MR-42 say?" — Lando opens the codebook, finds the
    // first matching rule for that OID or alias, and reads it back.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the first matching rule for the given OID or alias, or null if not found.
     * Case-insensitive lookup.
     *
     * @param id  an OID or an alias (case-insensitive)
     * @return    the matching rule, or null if none found
     */
    public MatchingRule getMatchingRule( String id )
    {
        List<?> list = getMatchingRuleList( Strings.toLowerCase( id ) );

        if ( ( list != null ) && ( list.size() >= 1 ) )
        {
            return ( MatchingRule ) list.get( 0 );
        }
        else
        {
            return null;
        }
    }


    // ── Lando Lists All Statutes Under That Code ──────────────────────────────
    // If two platforms share the same legal code (a conflict), Lando returns
    // both so the caller can flag the duplicate.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns all matching rules for the given OID or alias.
     * Use this to detect conflicts where two schemas define the same matching rule OID.
     *
     * @param id  an OID or an alias (case-insensitive)
     * @return    a list of matching rules; may be null or empty
     */
    public List<?> getMatchingRuleList( String id )
    {
        return ( List<?> ) matchingRulesMap.get( Strings.toLowerCase( id ) );
    }


    // ── Lando Looks Up A Resident Category By Name ───────────────────────────
    // "Show me the record for category 'inetOrgPerson'." — Lando consults
    // the census and returns the first object class with that name or OID.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the first object class matching the given OID or alias, or null if not found.
     * Case-insensitive lookup.
     *
     * @param id  an OID or an alias (case-insensitive)
     * @return    the object class, or null if none found
     */
    public ObjectClass getObjectClass( String id )
    {
        List<?> list = getObjectClassList( Strings.toLowerCase( id ) );

        if ( ( list != null ) && ( list.size() >= 1 ) )
        {
            return ( ObjectClass ) list.get( 0 );
        }
        else
        {
            return null;
        }
    }


    // ── Lando Lists All Categories Under That Name ────────────────────────────
    // If two schemas define the same object class name — a conflict — Lando
    // returns both so the caller can decide how to handle the ambiguity.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns all object classes matching the given OID or alias.
     * Use this to detect conflicts where multiple schemas share the same object class name.
     *
     * @param id  an OID or an alias (case-insensitive)
     * @return    a list of object classes; may be null or empty
     */
    public List<?> getObjectClassList( String id )
    {
        return ( List<?> ) objectClassesMap.get( Strings.toLowerCase( id ) );
    }


    // ── Lando Looks Up A Platform By Name ────────────────────────────────────
    // "Which platform is 'core'?" — Lando consults the platform registry and
    // returns the first schema with that name.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the first schema matching the given name, or null if not found.
     * Case-insensitive lookup.
     *
     * @param name  the schema name (case-insensitive)
     * @return      the schema, or null if not found
     */
    public Schema getSchema( String name )
    {
        List<?> list = getSchemaList( Strings.toLowerCase( name ) );

        if ( ( list != null ) && ( list.size() >= 1 ) )
        {
            return ( Schema ) list.get( 0 );
        }
        else
        {
            return null;
        }
    }


    // ── Lando Lists All Platforms With That Name ──────────────────────────────
    // If two platforms somehow share a name (unlikely, but possible), Lando
    // returns both so the caller can resolve the conflict.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns all schemas matching the given name.
     * Normally there's exactly one; duplicates indicate a naming conflict.
     *
     * @param name  the schema name (case-insensitive)
     * @return      a list of schemas; may be null or empty
     */
    public List<?> getSchemaList( String name )
    {
        return ( List<?> ) schemasMap.get( Strings.toLowerCase( name ) );
    }


    // ── Lando Looks Up A Comm Protocol By ID ─────────────────────────────────
    // "What is syntax 1.3.6.1.4.1.1466.115.121.1.15?" — Lando checks the
    // protocol directory and returns the first matching syntax definition.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the first syntax matching the given OID or alias, or null if not found.
     * Case-insensitive lookup.
     *
     * @param id  an OID or an alias (case-insensitive)
     * @return    the syntax, or null if none found
     */
    public LdapSyntax getSyntax( String id )
    {
        List<?> list = getSyntaxList( Strings.toLowerCase( id ) );

        if ( ( list != null ) && ( list.size() >= 1 ) )
        {
            return ( LdapSyntax ) list.get( 0 );
        }
        else
        {
            return null;
        }
    }


    // ── Lando Lists All Syntaxes With That ID ────────────────────────────────
    // If two schemas define the same syntax OID, Lando returns both so the
    // caller can flag the conflict.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns all syntaxes matching the given OID or alias.
     * Use this to detect conflicts where two schemas define the same syntax.
     *
     * @param id  an OID or an alias (case-insensitive)
     * @return    a list of syntaxes; may be null or empty
     */
    public List<?> getSyntaxList( String id )
    {
        return ( List<?> ) syntaxesMap.get( Strings.toLowerCase( id ) );
    }


    // ── Lando Adds A Department To The City Broadcast List ───────────────────
    // A new department head tunes in to the city-wide broadcast — from now on
    // they'll hear every schema change announcement.
    // We guard against duplicates so the same listener doesn't get called twice
    // for a single event.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers a SchemaHandlerListener to receive future schema mutation events.
     * Duplicate registrations are silently ignored.
     * Always call {@link #removeListener(SchemaHandlerListener)} when the
     * listening component is disposed.
     *
     * @param listener  the listener to register; must not be null
     */
    public void addListener( SchemaHandlerListener listener )
    {
        if ( !schemaHandlerListeners.contains( listener ) )
        {
            schemaHandlerListeners.add( listener );
        }
    }


    // ── Lando Removes A Department From The Broadcast List ───────────────────
    // The department head clocks out and tunes off the city-wide frequency —
    // no more announcements for them.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Deregisters a previously added SchemaHandlerListener.
     * Call this when the listening component is disposed to prevent callbacks
     * from firing into a closed view.
     *
     * @param listener  the listener to remove; no-op if not registered
     */
    public void removeListener( SchemaHandlerListener listener )
    {
        schemaHandlerListeners.remove( listener );
    }


    // ── Lando Commissions A Whole New Gas Platform ───────────────────────────
    // A new gas-mining platform comes online: Lando registers it in the master
    // ledger, logs all its resident guilds (attribute types, object classes,
    // matching rules, syntaxes), and broadcasts the arrival city-wide.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds an entire schema and all its contained types to the handler.
     * We register every attribute type, matching rule, object class, and syntax
     * from the schema into the appropriate list and map, then fire schemaAdded
     * on all listeners.
     *
     * <p>For example — Lando commissions a whole new platform in one announcement:</p>
     * <pre>
     *   handler.addSchema( coreSchema );
     *   // All types in coreSchema are now findable by OID and alias
     * </pre>
     *
     * @param schema  the schema to add, with all its contained types
     */
    public void addSchema( Schema schema )
    {
        // Adding the schema
        schemasList.add( schema );
        schemasMap.put( Strings.toLowerCase( schema.getSchemaName() ), schema );

        // Adding its attribute types
        for ( AttributeType at : schema.getAttributeTypes() )
        {
            addSchemaObject( at );
        }

        // Adding its matching rules
        for ( MatchingRule mr : schema.getMatchingRules() )
        {
            addSchemaObject( mr );
        }

        // Adding its object classes
        for ( ObjectClass oc : schema.getObjectClasses() )
        {
            addSchemaObject( oc );
        }

        // Adding its syntaxes
        for ( LdapSyntax syntax : schema.getSyntaxes() )
        {
            addSchemaObject( syntax );
        }

        notifySchemaAdded( schema );
    }


    // ── Lando Registers A Single New Resident ────────────────────────────────
    // A new resident arrives on Cloud City; Lando logs their name (and every
    // alias they go by) in the appropriate guild registry and the master ledger.
    // We dispatch on the concrete type — attribute type, matching rule, object
    // class, or syntax — and index by OID plus all declared aliases.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds a single schema object to the appropriate list and map.
     * We index by OID and by every alias in the object's names list, so lookups
     * by any identifier work immediately after this call.
     *
     * @param object  the schema object to register; must be an AttributeType,
     *                MatchingRule, ObjectClass, or LdapSyntax
     */
    private void addSchemaObject( SchemaObject object )
    {
        if ( object instanceof AttributeType )
        {
            AttributeType at = ( AttributeType ) object;
            attributeTypesList.add( at );
            List<String> names = at.getNames();
            if ( names != null )
            {
                for ( String name : names )
                {
                    attributeTypesMap.put( Strings.toLowerCase( name ), at );
                }
            }
            attributeTypesMap.put( at.getOid(), at );
        }
        else if ( object instanceof MatchingRule )
        {
            MatchingRule mr = ( MatchingRule ) object;
            matchingRulesList.add( mr );
            List<String> names = mr.getNames();
            if ( names != null )
            {
                for ( String name : names )
                {
                    matchingRulesMap.put( Strings.toLowerCase( name ), mr );
                }
            }
            matchingRulesMap.put( mr.getOid(), mr );
        }
        else if ( object instanceof ObjectClass )
        {
            ObjectClass oc = ( ObjectClass ) object;
            objectClassesList.add( oc );
            List<String> names = oc.getNames();
            if ( names != null )
            {
                for ( String name : names )
                {
                    objectClassesMap.put( Strings.toLowerCase( name ), oc );
                }
            }
            objectClassesMap.put( oc.getOid(), oc );
        }
        else if ( object instanceof LdapSyntax )
        {
            LdapSyntax syntax = ( LdapSyntax ) object;
            syntaxesList.add( syntax );
            List<String> names = syntax.getNames();
            if ( names != null )
            {
                for ( String name : names )
                {
                    syntaxesMap.put( Strings.toLowerCase( name ), syntax );
                }
            }
            syntaxesMap.put( syntax.getOid(), syntax );
        }
    }


    // ── Lando Decommissions A Gas Platform ───────────────────────────────────
    // A platform is shut down; Lando removes it from the master ledger, clears
    // all its residents from the guild registries, and broadcasts city-wide
    // that the platform is gone.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes an entire schema and all its contained types from the handler.
     * We deregister every attribute type, matching rule, object class, and syntax
     * from the appropriate list and map, then fire schemaRemoved on all listeners.
     *
     * <p>For example — Lando mothballs a platform and clears all its records:</p>
     * <pre>
     *   handler.removeSchema( coreSchema );
     *   // All types that were only in coreSchema are no longer findable
     * </pre>
     *
     * @param schema  the schema to remove
     */
    public void removeSchema( Schema schema )
    {
        // Removing the schema
        schemasList.remove( schema );
        schemasMap.remove( Strings.toLowerCase( schema.getSchemaName() ) );

        // Removing its attribute types
        for ( AttributeType at : schema.getAttributeTypes() )
        {
            removeSchemaObject( at );
        }

        // Removing its matching rules
        for ( MatchingRule mr : schema.getMatchingRules() )
        {
            removeSchemaObject( mr );
        }

        // Removing its object classes
        for ( ObjectClass oc : schema.getObjectClasses() )
        {
            removeSchemaObject( oc );
        }

        // Removing its syntaxes
        for ( LdapSyntax syntax : schema.getSyntaxes() )
        {
            removeSchemaObject( syntax );
        }

        notifySchemaRemoved( schema );
    }


    // ── Lando Evicts A Single Resident ───────────────────────────────────────
    // One resident is asked to leave Cloud City; Lando strikes their name and
    // every alias from the guild registry and the master ledger.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes a single schema object from the appropriate list and map.
     * We remove by OID and by every alias so no stale lookup entries remain.
     *
     * @param object  the schema object to remove; must be an AttributeType,
     *                MatchingRule, ObjectClass, or LdapSyntax
     */
    private void removeSchemaObject( SchemaObject object )
    {
        if ( object instanceof AttributeType )
        {
            AttributeType at = ( AttributeType ) object;
            attributeTypesList.remove( at );
            List<String> names = at.getNames();
            if ( names != null )
            {
                for ( String name : names )
                {
                    attributeTypesMap.remove( Strings.toLowerCase( name ) );
                }
            }
            attributeTypesMap.remove( at.getOid() );
        }
        else if ( object instanceof MatchingRule )
        {
            MatchingRule mr = ( MatchingRule ) object;
            matchingRulesList.remove( mr );
            List<String> names = mr.getNames();
            if ( names != null )
            {
                for ( String name : names )
                {
                    matchingRulesMap.remove( Strings.toLowerCase( name ) );
                }
            }
            matchingRulesMap.remove( mr.getOid() );
        }
        else if ( object instanceof ObjectClass )
        {
            ObjectClass oc = ( ObjectClass ) object;
            objectClassesList.remove( oc );
            List<String> names = oc.getNames();
            if ( names != null )
            {
                for ( String name : names )
                {
                    objectClassesMap.remove( Strings.toLowerCase( name ) );
                }
            }
            objectClassesMap.remove( oc.getOid() );
        }
        else if ( object instanceof LdapSyntax )
        {
            LdapSyntax syntax = ( LdapSyntax ) object;
            syntaxesList.remove( syntax );
            List<String> names = syntax.getNames();
            if ( names != null )
            {
                for ( String name : names )
                {
                    syntaxesMap.remove( Strings.toLowerCase( name ) );
                }
            }
            syntaxesMap.remove( syntax.getOid() );
        }
    }


    // ── Lando Renames A Platform In The Registry ──────────────────────────────
    // The platform formerly known as "tibanna-7" is now officially "tibanna-prime";
    // Lando removes the old entry from the ledger, updates the platform record,
    // writes the new name, cascades the rename to every resident, and broadcasts
    // the change city-wide.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Renames a schema and cascades the new name to all its contained types.
     * We update the map key, the schema object's name, and every child type's
     * schema-name field, then fire schemaRenamed on all listeners.
     *
     * <p>For example — Lando renames a platform and updates every resident's badge:</p>
     * <pre>
     *   handler.renameSchema( schema, "new-name" );
     *   // schema.getSchemaName() == "new-name"
     *   // every attribute type in the schema has schemaName == "new-name"
     * </pre>
     *
     * @param schema   the schema to rename; must be registered
     * @param newName  the new name for the schema
     */
    public void renameSchema( Schema schema, String newName )
    {
        schemasMap.remove( Strings.toLowerCase( schema.getSchemaName() ) );
        schema.setSchemaName( newName );
        schemasMap.put( Strings.toLowerCase( schema.getSchemaName() ), schema );

        // Removing its attribute types
        for ( AttributeType at : schema.getAttributeTypes() )
        {
            at.setSchemaName( newName );
        }

        // Removing its matching rules
        for ( MatchingRule mr : schema.getMatchingRules() )
        {
            mr.setSchemaName( newName );
        }

        // Removing its object classes
        for ( ObjectClass oc : schema.getObjectClasses() )
        {
            oc.setSchemaName( newName );
        }

        // Removing its syntaxes
        for ( LdapSyntax syntax : schema.getSyntaxes() )
        {
            syntax.setSchemaName( newName );
        }

        notifySchemaRenamed( schema );
    }


    // ── Lando Registers A New Guild Member ───────────────────────────────────
    // A new attribute type joins the residents of an existing platform;
    // Lando adds them to the guild roster and announces the arrival.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds a single attribute type to its parent schema and to the handler's indices.
     * The schema is looked up by the attribute type's schema name, so that name must
     * already be registered before calling this.  After adding, attributeTypeAdded
     * fires on all listeners.
     *
     * @param at  the attribute type to add; its schemaName must match a registered schema
     */
    public void addAttributeType( AttributeType at )
    {
        Schema schema = getSchema( at.getSchemaName() );

        schema.addAttributeType( at );
        addSchemaObject( at );

        // Notifying the listeners
        notifyAttributeTypeAdded( at );
    }


    // ── Lando Updates A Resident's File ──────────────────────────────────────
    // A guild member's records need updating: Lando first evicts the old entry
    // (in case the name or OID changed), then writes the new details in and
    // re-registers all aliases before broadcasting the change.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Updates the source attribute type in place with values from the destination.
     * We remove all old index entries first (because names/OID may have changed),
     * copy every field from {@code at2} into {@code at1}, then re-index and notify.
     *
     * <p>For example — Lando updates a resident's file without evicting them:</p>
     * <pre>
     *   handler.modifyAttributeType( existing, updated );
     *   // existing now has all values from updated; listeners are notified
     * </pre>
     *
     * @param at1  the attribute type to update (the live object in the schema)
     * @param at2  the attribute type carrying the new values (typically from the editor)
     */
    public void modifyAttributeType( AttributeType at1, AttributeType at2 )
    {
        // Removing the references (in case of the names or oid have changed)
        removeSchemaObject( at1 );

        // Updating the attribute type
        at1.setNames( at2.getNames() );
        at1.setOid( at2.getOid() );
        at1.setDescription( at2.getDescription() );
        at1.setSuperiorOid( at2.getSuperiorOid() );
        at1.setUsage( at2.getUsage() );
        at1.setSyntaxOid( at2.getSyntaxOid() );
        at1.setSyntaxLength( at2.getSyntaxLength() );
        at1.setObsolete( at2.isObsolete() );
        at1.setSingleValued( at2.isSingleValued() );
        at1.setCollective( at2.isCollective() );
        at1.setUserModifiable( at2.isUserModifiable() );
        at1.setEqualityOid( at2.getEqualityOid() );
        at1.setOrderingOid( at2.getOrderingOid() );
        at1.setSubstringOid( at2.getSubstringOid() );

        // Adding the references (in case of the names or oid have changed)
        addSchemaObject( at1 );

        // Notifying the listeners
        notifyAttributeTypeModified( at1 );
    }


    // ── Lando Evicts A Guild Member ───────────────────────────────────────────
    // A guild member is asked to leave the platform; Lando removes them from
    // the schema's roster, strikes their name from all registries, and
    // broadcasts the departure.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes a single attribute type from its parent schema and from the handler's indices.
     * After removal, attributeTypeRemoved fires on all listeners.
     *
     * @param at  the attribute type to remove
     */
    public void removeAttributeType( AttributeType at )
    {
        Schema schema = getSchema( at.getSchemaName() );

        schema.removeAttributeType( at );
        removeSchemaObject( at );

        // Notifying the listeners
        notifyAttributeTypeRemoved( at );
    }


    // ── Lando Registers A New Resident Category ──────────────────────────────
    // A new species arrives on Cloud City and is added to the census — a new
    // object class is registered in the schema and indexed for fast lookup.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds a single object class to its parent schema and to the handler's indices.
     * After adding, objectClassAdded fires on all listeners.
     *
     * @param oc  the object class to add; its schemaName must match a registered schema
     */
    public void addObjectClass( ObjectClass oc )
    {
        Schema schema = getSchema( oc.getSchemaName() );

        schema.addObjectClass( oc );
        addSchemaObject( oc );

        // Notifying the listeners
        notifyObjectClassAdded( oc );
    }


    // ── Lando Updates A Resident Category's Census Record ────────────────────
    // The census record for a resident category changes — perhaps a new
    // mandatory attribute, perhaps a different supertype.  Lando re-indexes
    // and broadcasts the update.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Updates the source object class in place with values from the destination.
     * We remove all old index entries first, copy every field, re-index, then notify.
     *
     * @param oc1  the object class to update (the live object in the schema)
     * @param oc2  the object class carrying the new values (typically from the editor)
     */
    public void modifyObjectClass( ObjectClass oc1, ObjectClass oc2 )
    {
        // Removing the references (in case of the names or oid have changed)
        removeSchemaObject( oc1 );

        // Updating the object class
        oc1.setNames( oc2.getNames() );
        oc1.setOid( oc2.getOid() );
        oc1.setDescription( oc2.getDescription() );
        oc1.setSuperiorOids( oc2.getSuperiorOids() );
        oc1.setType( oc2.getType() );
        oc1.setObsolete( oc2.isObsolete() );
        oc1.setMustAttributeTypeOids( oc2.getMustAttributeTypeOids() );
        oc1.setMayAttributeTypeOids( oc2.getMayAttributeTypeOids() );

        // Adding the references (in case of the names or oid have changed)
        addSchemaObject( oc1 );

        // Notifying the listeners
        notifyObjectClassModified( oc1 );
    }


    // ── Lando Removes A Resident Category ────────────────────────────────────
    // A resident category is dissolved; Lando strikes it from the census, the
    // guild ledger, and all indexes, then broadcasts the removal.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes a single object class from its parent schema and from the handler's indices.
     * After removal, objectClassRemoved fires on all listeners.
     *
     * @param oc  the object class to remove
     */
    public void removeObjectClass( ObjectClass oc )
    {
        Schema schema = getSchema( oc.getSchemaName() );

        schema.removeObjectClass( oc );
        removeSchemaObject( oc );

        notifyObjectClassRemoved( oc );
    }


    // ── Lando Broadcasts "New Platform Online" ────────────────────────────────
    // Lando keys the city-wide comm: "Attention — a new gas platform is online."
    // Every subscribed department hears it and updates their boards.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fires schemaAdded on all registered SchemaHandlerListeners.
     * We snapshot the listener list with toArray to avoid ConcurrentModificationException
     * if a listener modifies the list during callback.
     *
     * @param schema  the schema that was added
     */
    private void notifySchemaAdded( Schema schema )
    {
        for ( SchemaHandlerListener listener : schemaHandlerListeners.toArray( new SchemaHandlerListener[0] ) )
        {
            listener.schemaAdded( schema );
        }
    }


    // ── Lando Broadcasts "Platform Decommissioned" ───────────────────────────
    // The city-wide comm crackles: "Attention — gas platform seven is now
    // offline."  Every department updates accordingly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fires schemaRemoved on all registered SchemaHandlerListeners.
     *
     * @param schema  the schema that was removed
     */
    private void notifySchemaRemoved( Schema schema )
    {
        for ( SchemaHandlerListener listener : schemaHandlerListeners.toArray( new SchemaHandlerListener[0] ) )
        {
            listener.schemaRemoved( schema );
        }
    }


    // ── Lando Broadcasts The Platform's New Name ──────────────────────────────
    // The city-wide comm announces: "Platform seven is now officially named
    // 'tibanna-prime'."  Every department updates their signs and ledgers.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fires schemaRenamed on all registered SchemaHandlerListeners.
     *
     * @param schema  the schema that was renamed
     */
    private void notifySchemaRenamed( Schema schema )
    {
        for ( SchemaHandlerListener listener : schemaHandlerListeners.toArray( new SchemaHandlerListener[0] ) )
        {
            listener.schemaRenamed( schema );
        }
    }


    // ── Lando Announces A New Attribute-Type Guild Member ────────────────────
    // Lando keys the guild channel: "New member registered in the attribute-
    // type guild."  All subscribers on that channel update their rosters.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fires attributeTypeAdded on all registered SchemaHandlerListeners.
     *
     * @param at  the attribute type that was added
     */
    private void notifyAttributeTypeAdded( AttributeType at )
    {
        // SchemaHandler Listeners
        for ( SchemaHandlerListener listener : schemaHandlerListeners.toArray( new SchemaHandlerListener[0] ) )
        {
            listener.attributeTypeAdded( at );
        }
    }


    // ── Lando Announces An Attribute-Type Record Update ───────────────────────
    // The guild channel crackles: "Member record updated — check your boards."
    // Every listener refreshes its cached view of the modified attribute type.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fires attributeTypeModified on all registered SchemaHandlerListeners.
     *
     * @param at  the attribute type that was modified
     */
    private void notifyAttributeTypeModified( AttributeType at )
    {
        // SchemaHandler Listeners
        for ( SchemaHandlerListener listener : schemaHandlerListeners.toArray( new SchemaHandlerListener[0] ) )
        {
            listener.attributeTypeModified( at );
        }
    }


    // ── Lando Announces A Guild Member Has Departed ───────────────────────────
    // The guild channel broadcasts: "Member has left Cloud City — remove from
    // your rosters."  Every listener drops its reference to the removed type.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fires attributeTypeRemoved on all registered SchemaHandlerListeners.
     *
     * @param at  the attribute type that was removed
     */
    private void notifyAttributeTypeRemoved( AttributeType at )
    {
        // SchemaHandler Listeners
        for ( SchemaHandlerListener listener : schemaHandlerListeners.toArray( new SchemaHandlerListener[0] ) )
        {
            listener.attributeTypeRemoved( at );
        }
    }


    // ── Lando Announces A New Object Class In The Census ─────────────────────
    // The census channel announces: "New resident category registered —
    // update your population counts."  Listeners add it to their displays.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fires objectClassAdded on all registered SchemaHandlerListeners.
     *
     * @param oc  the object class that was added
     */
    private void notifyObjectClassAdded( ObjectClass oc )
    {
        // SchemaHandler Listeners
        for ( SchemaHandlerListener listener : schemaHandlerListeners.toArray( new SchemaHandlerListener[0] ) )
        {
            listener.objectClassAdded( oc );
        }
    }


    // ── Lando Announces A Census Record Update ────────────────────────────────
    // The census channel broadcasts: "Category record updated — please refresh
    // your population tables."  Listeners repaint the modified object class.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fires objectClassModified on all registered SchemaHandlerListeners.
     *
     * @param oc  the object class that was modified
     */
    private void notifyObjectClassModified( ObjectClass oc )
    {
        // SchemaHandler Listeners
        for ( SchemaHandlerListener listener : schemaHandlerListeners.toArray( new SchemaHandlerListener[0] ) )
        {
            listener.objectClassModified( oc );
        }
    }


    // ── Lando Announces A Category Has Been Dissolved ────────────────────────
    // The census channel: "Resident category dissolved — remove from records."
    // Every listener drops its reference to the removed object class.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fires objectClassRemoved on all registered SchemaHandlerListeners.
     *
     * @param oc  the object class that was removed
     */
    private void notifyObjectClassRemoved( ObjectClass oc )
    {
        // SchemaHandler Listeners
        for ( SchemaHandlerListener listener : schemaHandlerListeners.toArray( new SchemaHandlerListener[0] ) )
        {
            listener.objectClassRemoved( oc );
        }
    }


    // ── Lando Checks For OID Conflicts Before Registration ───────────────────
    // Before stamping a new resident's ID badge, Lando cross-checks every
    // existing guild, legal codebook, and comm directory to make sure the
    // proposed OID isn't already in use somewhere.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the given OID is already registered by any schema object.
     * We check attribute types, object classes, matching rules, and syntaxes.
     * Use this before creating a new schema object to catch OID conflicts early.
     *
     * @param oid  the OID to check (case-insensitive)
     * @return     true if the OID is already in use by any schema object
     */
    public boolean isOidAlreadyTaken( String oid )
    {
        String lowerCasedOid = Strings.toLowerCase( oid );
        if ( attributeTypesMap.containsKey( lowerCasedOid ) )
        {
            return true;
        }
        else if ( objectClassesMap.containsKey( lowerCasedOid ) )
        {
            return true;
        }
        else if ( matchingRulesMap.containsKey( lowerCasedOid ) )
        {
            return true;
        }
        else if ( syntaxesMap.containsKey( lowerCasedOid ) )
        {
            return true;
        }

        return false;
    }


    // ── Lando Checks The Attribute Guild For A Name Conflict ─────────────────
    // Before printing a name badge for the attribute-type guild, Lando checks
    // whether that name is already taken by another guild member.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the given alias is already used by any attribute type.
     * Case-insensitive.  Use this before creating or renaming an attribute type.
     *
     * @param alias  the alias to check
     * @return       true if already in use by an attribute type
     */
    public boolean isAliasAlreadyTakenForAttributeType( String alias )
    {
        return attributeTypesMap.containsKey( Strings.toLowerCase( alias ) );
    }


    // ── Lando Checks The Object Class Census For A Name Conflict ─────────────
    // Before registering a new resident category, Lando checks whether that
    // category name already exists in the census.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the given alias is already used by any object class.
     * Case-insensitive.  Use this before creating or renaming an object class.
     *
     * @param alias  the alias to check
     * @return       true if already in use by an object class
     */
    public boolean isAliasAlreadyTakenForObjectClass( String alias )
    {
        return objectClassesMap.containsKey( Strings.toLowerCase( alias ) );
    }


    // ── Lando Checks The Platform Registry For A Name Conflict ───────────────
    // Before commissioning a new gas platform, Lando checks whether that
    // platform name is already in the master ledger.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if a schema with the given name already exists.
     * Case-insensitive.  Use this before creating or renaming a schema.
     *
     * @param name  the schema name to check
     * @return      true if a schema with that name is already registered
     */
    public boolean isSchemaNameAlreadyTaken( String name )
    {
        return schemasMap.containsKey( Strings.toLowerCase( name ) );
    }
}
