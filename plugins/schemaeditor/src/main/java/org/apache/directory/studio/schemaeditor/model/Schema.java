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
package org.apache.directory.studio.schemaeditor.model;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.registries.DefaultSchema;


// ── CLASS: Schema — Lando Running Cloud City ──────────────────────────────────
// Cloud City doesn't run on vibes — Lando maintains four distinct operations:
// the tibanna gas refineries (attribute types), the atmospheric processing
// plants (object classes), the weather-control systems (matching rules), and
// the communications syntax arrays (syntaxes). He can add or remove any unit,
// look one up by name or OID, and hand you the full roster for any department.
// Each department is just a list; Lando keeps them tidy.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents a single named schema that groups attribute types, object classes,
 * matching rules, and syntaxes together under one roof. Extends {@link DefaultSchema}
 * to participate in the Apache Directory API's schema registry infrastructure.
 * Think of this class as Lando managing Cloud City: each department (attribute
 * types, object classes, matching rules, syntaxes) is a list he can add to,
 * remove from, and query by name or OID.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Schema extends DefaultSchema
{
    /** The project */
    private Project project;

    /** The AttributeType List */
    private List<AttributeType> attributeTypes = new ArrayList<AttributeType>();

    /** The ObjectClass List */
    private List<ObjectClass> objectClasses = new ArrayList<ObjectClass>();

    /** The MatchingRule List */
    private List<MatchingRule> matchingRules = new ArrayList<MatchingRule>();

    /** The Syntax List */
    private List<LdapSyntax> syntaxes = new ArrayList<LdapSyntax>();


    // ── Lando Opens A New City With A Name On The Charter ───────────────────────
    // Lando files the incorporation papers — the city gets a name, and all four
    // department rosters start empty. The null parent arg tells DefaultSchema
    // this schema has no registry-managed parent.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new, empty schema with the given name. All four element lists
     * (attribute types, object classes, matching rules, syntaxes) start empty and
     * are populated via the add* methods.
     *
     * @param name  the schema name (e.g. "inetOrgPerson", "core"); used to identify
     *              this schema in export filenames and dependency computation
     */
    public Schema( String name )
    {
        super( null, name );
    }


    // ── Lando Hires A New Refinery Worker ────────────────────────────────────────
    // Cloud City adds a tibanna-gas refinery unit (attribute type) to the roster.
    // The boolean return tells the caller whether the registration succeeded.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds an attribute type to this schema's list. Callers should also register
     * the type with the project's SchemaHandler so it shows up in lookups.
     * Returns false if the underlying list rejected the element (shouldn't happen
     * with an ArrayList, but we propagate it for correctness).
     *
     * @param at  the attribute type to register in this schema
     * @return    true if the add succeeded
     */
    public boolean addAttributeType( AttributeType at )
    {
        return attributeTypes.add( at );
    }


    // ── Lando Adds A Matching-Rule Technician To The Roster ─────────────────────
    // Cloud City brings on a new weather-control technician (matching rule). They
    // handle the sorting and comparison logic for the refinery.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a matching rule to this schema's list. Matching rules define how
     * attribute values are compared and sorted — equality, ordering, substring.
     *
     * @param mr  the matching rule to register in this schema
     * @return    true if the add succeeded
     */
    public boolean addMatchingRule( MatchingRule mr )
    {
        return matchingRules.add( mr );
    }


    // ── Lando Registers A New Command-Module Crew ────────────────────────────────
    // A new command module (object class) comes online in Cloud City, adding its
    // own set of required attributes to the city's operations.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds an object class to this schema's list. Object classes define what kind
     * of entries exist in the directory and which attributes they must or may carry.
     *
     * @param oc  the object class to register in this schema
     * @return    true if the add succeeded
     */
    public boolean addObjectClass( ObjectClass oc )
    {
        return objectClasses.add( oc );
    }


    // ── Lando Activates A New Communications Array ───────────────────────────────
    // A new syntax array (syntax) goes live in the city, adding a new supported
    // data-format spec to the communications infrastructure.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a syntax to this schema's list. Syntaxes define the allowed data format
     * for attribute values (e.g. IA5String, Integer, DN).
     *
     * @param syntax  the syntax to register in this schema
     * @return        true if the add succeeded
     */
    public boolean addSyntax( LdapSyntax syntax )
    {
        return syntaxes.add( syntax );
    }


    // ── Lando Looks Up A Refinery Unit By Badge Number ───────────────────────────
    // Lando checks the refinery roster for a worker identified by their name or OID
    // badge. He checks every alias and the OID directly, returning the unit if found.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up an attribute type by name or OID, case-insensitively. We check all
     * aliases before falling back to the OID so "cn" and "commonName" both resolve.
     * Returns null if nothing matches — callers should handle that gracefully.
     *
     * @param id  the name (alias) or OID of the attribute type to find
     * @return    the matching attribute type, or null if not found in this schema
     */
    public AttributeType getAttributeType( String id )
    {
        for ( AttributeType at : attributeTypes )
        {
            List<String> aliases = at.getNames();
            if ( aliases != null )
            {
                for ( String alias : aliases )
                {
                    if ( alias.equalsIgnoreCase( id ) )
                    {
                        return at;
                    }
                }
            }
            if ( at.getOid().equalsIgnoreCase( id ) )
            {
                return at;
            }
        }

        return null;
    }


    // ── Lando Hands Over The Full Refinery Roster ────────────────────────────────
    // The whole tibanna-gas crew, unfiltered. Callers who need to iterate all
    // attribute types get the live list back.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the complete list of attribute types in this schema. The list is
     * live — modifications to it directly affect the schema, so treat with care.
     * Callers typically iterate this for export or dependency computation.
     *
     * @return  all attribute types in this schema; never null, may be empty
     */
    public List<AttributeType> getAttributeTypes()
    {
        return attributeTypes;
    }


    // ── Lando Looks Up A Matching-Rule Tech By Badge ─────────────────────────────
    // Lando checks the weather-control roster for a technician by name or OID.
    // Same alias-first lookup logic as for attribute types.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a matching rule by name or OID, case-insensitively. Returns null
     * if nothing matches. Used during dependency computation and schema export.
     *
     * @param id  the name or OID of the matching rule
     * @return    the matching rule, or null if not found in this schema
     */
    public MatchingRule getMatchingRule( String id )
    {
        for ( MatchingRule mr : matchingRules )
        {
            List<String> aliases = mr.getNames();
            if ( aliases != null )
            {
                for ( String alias : aliases )
                {
                    if ( alias.equalsIgnoreCase( id ) )
                    {
                        return mr;
                    }
                }
            }
            if ( mr.getOid().equalsIgnoreCase( id ) )
            {
                return mr;
            }
        }

        return null;
    }


    // ── Lando Hands Over The Full Matching-Rule Roster ───────────────────────────
    // The whole weather-control crew, unfiltered. Used during export to serialise
    // all matching rules in this schema.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the complete list of matching rules in this schema. Live list —
     * handle carefully. Typically iterated during export or registry load.
     *
     * @return  all matching rules in this schema; never null, may be empty
     */
    public List<MatchingRule> getMatchingRules()
    {
        return matchingRules;
    }


    // ── Lando Checks Which Project Owns This City ────────────────────────────────
    // Cloud City belongs to a specific project. Lando checks the ownership papers
    // so callers know which top-level container this schema lives in.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Project that this schema belongs to. Used by the SchemaHandler
     * and UI to navigate from a schema back to its containing project. May be null
     * if the schema hasn't been associated with a project yet.
     *
     * @return  the owning project, or null if not yet assigned
     */
    public Project getProject()
    {
        return project;
    }


    // ── Lando Looks Up A Command Module By Badge ─────────────────────────────────
    // Lando checks the command-module roster for an object class by name or OID.
    // Alias-first, then OID — same pattern as the other lookups.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up an object class by name or OID, case-insensitively. Returns null if
     * nothing matches. Used by SchemaHandler and DependenciesComputer to resolve
     * superior class references.
     *
     * @param id  the name or OID of the object class
     * @return    the object class, or null if not found in this schema
     */
    public ObjectClass getObjectClass( String id )
    {
        for ( ObjectClass oc : objectClasses )
        {
            List<String> aliases = oc.getNames();
            if ( aliases != null )
            {
                for ( String alias : aliases )
                {
                    if ( alias.equalsIgnoreCase( id ) )
                    {
                        return oc;
                    }
                }
            }
            if ( oc.getOid().equalsIgnoreCase( id ) )
            {
                return oc;
            }
        }

        return null;
    }


    // ── Lando Hands Over The Full Command-Module Roster ──────────────────────────
    // All command modules, unfiltered. Callers iterate this during export and
    // dependency computation.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the complete list of object classes in this schema. Live list.
     * Typically iterated during export, dependency computation, or UI display.
     *
     * @return  all object classes in this schema; never null, may be empty
     */
    public List<ObjectClass> getObjectClasses()
    {
        return objectClasses;
    }


    // ── Lando Looks Up A Communications Array By Spec Code ───────────────────────
    // Lando checks the comms array roster for a syntax by name or OID.
    // Returns null if the spec code isn't registered.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a syntax by name or OID, case-insensitively. Returns null if not
     * found. Used during dependency computation to verify that an attribute type's
     * declared syntax actually exists.
     *
     * @param id  the name or OID of the syntax
     * @return    the syntax, or null if not found in this schema
     */
    public LdapSyntax getSyntax( String id )
    {
        for ( LdapSyntax syntax : syntaxes )
        {
            List<String> aliases = syntax.getNames();
            if ( aliases != null )
            {
                for ( String alias : aliases )
                {
                    if ( alias.equalsIgnoreCase( id ) )
                    {
                        return syntax;
                    }
                }
            }
            if ( syntax.getOid().equalsIgnoreCase( id ) )
            {
                return syntax;
            }
        }

        return null;
    }


    // ── Lando Hands Over The Full Comms Array Roster ─────────────────────────────
    // All syntax arrays, unfiltered. Used during export to serialise the schema's
    // syntax definitions.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the complete list of syntaxes in this schema. Live list.
     * Typically iterated during export or registry load.
     *
     * @return  all syntaxes in this schema; never null, may be empty
     */
    public List<LdapSyntax> getSyntaxes()
    {
        return syntaxes;
    }


    // ── Lando Dismisses A Refinery Worker ────────────────────────────────────────
    // A tibanna-gas refinery unit is decommissioned and removed from the city roster.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes an attribute type from this schema. The caller is responsible for
     * also removing it from the project's SchemaHandler and updating any UI views.
     *
     * @param at  the attribute type to remove
     * @return    true if it was present and was removed
     */
    public boolean removeAttributeType( AttributeType at )
    {
        return attributeTypes.remove( at );
    }


    // ── Lando Dismisses A Matching-Rule Technician ───────────────────────────────
    // A weather-control technician is let go and struck from the city roster.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a matching rule from this schema. Rarely needed in normal UI flows —
     * more common during import cleanup or merge operations.
     *
     * @param mr  the matching rule to remove
     * @return    true if it was present and was removed
     */
    public boolean removeMatchingRule( MatchingRule mr )
    {
        return matchingRules.remove( mr );
    }


    // ── Lando Decommissions A Command Module ─────────────────────────────────────
    // A command module goes dark and is struck from the city roster.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes an object class from this schema. The caller is responsible for also
     * removing it from the SchemaHandler and refreshing any relevant UI views.
     *
     * @param oc  the object class to remove
     * @return    true if it was present and was removed
     */
    public boolean removeObjectClass( ObjectClass oc )
    {
        return objectClasses.remove( oc );
    }


    // ── Lando Shuts Down A Communications Array ──────────────────────────────────
    // A comms array goes offline and is struck from the city roster.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a syntax from this schema. Again, rarely needed in normal flows —
     * syntaxes are typically loaded once and kept for the project lifetime.
     *
     * @param syntax  the syntax to remove
     * @return        true if it was present and was removed
     */
    public boolean removeSyntax( LdapSyntax syntax )
    {
        return syntaxes.remove( syntax );
    }


    // ── Lando Renames The City ───────────────────────────────────────────────────
    // The corporate office sends down a new brand name. Lando updates the sign above
    // the docking bay (the inherited {@code name} field from DefaultSchema).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Changes the name of this schema. We write directly to the inherited field
     * because DefaultSchema doesn't provide a setter — this is a known quirk of
     * extending that class. Callers should follow up with a SchemaHandler rename
     * event if one exists.
     *
     * @param schemaName  the new schema name
     */
    public void setSchemaName( String schemaName )
    {
        this.name = schemaName;
    }


    // ── Lando Registers The Project That Owns This City ──────────────────────────
    // Cloud City is incorporated into a larger holding project. Lando files the
    // ownership paperwork so everyone knows who the boss is.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Associates this schema with its owning Project. Called during project open
     * and import flows so that the schema can navigate back to its container.
     *
     * @param project  the project that owns this schema
     */
    public void setProject( Project project )
    {
        this.project = project;
    }


    // ── Lando Reads The City Name Off The Sign ───────────────────────────────────
    // "Cloud City." Simple. Lando returns the schema name for display in tree views
    // and as the key in any map that uses schema names.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the schema name — used as the tree-view label and as the key when
     * schemas are stored in maps. Delegates to {@link DefaultSchema#getSchemaName()}.
     *
     * @return  the schema name string
     */
    public String toString()
    {
        return getSchemaName();
    }
}
