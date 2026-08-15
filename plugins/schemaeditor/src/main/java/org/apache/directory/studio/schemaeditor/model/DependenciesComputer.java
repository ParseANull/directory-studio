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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.eclipse.osgi.util.NLS;


// ── CLASS: DependenciesComputer — Construction of the Second Death Star ──────
// Moff Jerjerrod oversees a massive construction project: every section of the
// battle station must be assembled in the right order — you can't install the
// superlaser before the power core, and you can't bolt on a power core before
// its reactor feeds are in place. That dependency graph has to be computed up
// front so the build order is safe.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Figures out the dependency graph for a set of schemas and sorts them into
 * a safe load order. We need this because schemas reference each other —
 * an attribute type might inherit from another schema's type, so we have to
 * load the parent schema first.
 * Think of this class as Moff Jerjerrod's construction schedule: every
 * structural dependency between Death Star sections is mapped before a single
 * panel is welded, so nothing gets assembled out of sequence.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DependenciesComputer
{
    /** The schemas List */
    private List<Schema> schemasList;

    /** The dependency ordered schemas List */
    private List<Schema> dependencyOrderedSchemasList;

    /** The SchemaHandler */
    private SchemaHandler schemaHandler;

    // The dependencies MultiMaps
    private MultiValuedMap schemasDependencies;
    private MultiValuedMap attributeTypesDependencies;
    private MultiValuedMap objectClassesDependencies;


    // ── Jerjerrod Opens The Construction Ledger ──────────────────────────────────
    // Moff Jerjerrod receives the full parts manifest for the second Death Star and
    // immediately sits down to draw the wiring diagram — which section depends on
    // which other section before it can be installed.
    // He walks every attribute type and object class, mapping each dependency edge,
    // then sorts the schemas into a build order that guarantees no section tries to
    // attach to a neighbour that hasn't been placed yet.
    // That sorted list is our deliverable: call getDependencyOrderedSchemasList()
    // and hand it straight to the export or commit pipeline.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dependency graph for the given list of schemas and computes a
     * safe load order. We walk every attribute type and object class in every
     * schema, recording which schemas they pull from, then do a topological sort.
     * If a referenced type or schema can't be found we throw immediately — a broken
     * dependency is a hard stop, just like a missing reactor component.
     *
     * <p>For example — Jerjerrod maps the construction order:</p>
     * <pre>
     *   schemas = [core, inetOrgPerson, custom]
     *   custom depends on inetOrgPerson, inetOrgPerson depends on core
     *   → dependencyOrderedSchemasList = [core, inetOrgPerson, custom]
     * </pre>
     *
     * @param schemas  the full list of schemas to analyse; may be null (treated as empty)
     * @throws DependencyComputerException  if any referenced superior type, syntax,
     *         matching rule, or schema cannot be resolved in the provided set
     */
    public DependenciesComputer( List<Schema> schemas ) throws DependencyComputerException
    {
        this.schemasList = schemas;

        // Creating the SchemaHandler
        schemaHandler = new SchemaHandler();

        // Creating the dependencies MultiMaps
        schemasDependencies = new ArrayListValuedHashMap<>();
        attributeTypesDependencies = new ArrayListValuedHashMap<>();
        objectClassesDependencies = new ArrayListValuedHashMap<>();

        if ( schemas != null )
        {
            // Adding the schemasList in the SchemaHandler
            for ( Schema schema : this.schemasList )
            {
                schemaHandler.addSchema( schema );
            }

            // Computing dependencies
            for ( Schema schema : this.schemasList )
            {
                List<AttributeType> attributeTypes = schema.getAttributeTypes();
                if ( attributeTypes != null )
                {
                    for ( AttributeType attributeType : attributeTypes )
                    {
                        computeDependencies( schema, attributeType );
                    }
                }

                List<ObjectClass> objectClasses = schema.getObjectClasses();
                if ( objectClasses != null )
                {
                    for ( ObjectClass objectClass : objectClasses )
                    {
                        computeDependencies( schema, objectClass );
                    }
                }
            }

            // Ordering the schemas
            orderSchemasBasedOnDependencies();
        }
    }


    // ── Jerjerrod Audits Each Structural Panel ───────────────────────────────────
    // An attribute type is a structural panel: it might bolt onto a superior type,
    // require a specific syntax conduit, or plug into equality/ordering/substring
    // matching-rule feeds — each of those is a dependency edge.
    // Jerjerrod's team inspects every panel and writes down which other sections it
    // must attach to before it can be safely installed.
    // If a required neighbour is missing from the manifest, the whole build halts.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Records every dependency that the given attribute type has — superior type,
     * syntax, equality matching rule, ordering matching rule, substring matching rule.
     * For each one we also call {@link #computeSchemaDependency} to record the
     * schema-level edge if the referenced object lives in a different schema.
     *
     * <p>For example — Jerjerrod inspects the panel:</p>
     * <pre>
     *   attributeType "telephoneNumber" has:
     *     syntax      → 1.3.6.1.4.1.1466.115.121.1.50 (core schema)
     *     equality    → telephoneNumberMatch (core schema)
     *   → edges added: schema "custom" depends on schema "core"
     * </pre>
     *
     * @param schema         the schema that owns this attribute type
     * @param attributeType  the attribute type to analyse
     * @throws DependencyComputerException  if any referenced superior, syntax, or
     *         matching rule cannot be resolved
     */
    private void computeDependencies( Schema schema, AttributeType attributeType )
        throws DependencyComputerException
    {
        // Superior
        String superior = attributeType.getSuperiorOid();
        if ( superior != null )
        {
            AttributeType superiorAT = schemaHandler.getAttributeType( superior );
            if ( superiorAT == null )
            {
                throw new DependencyComputerException( NLS.bind( Messages
                    .getString( "DependenciesComputer.SuperiorAttribute" ), new String[] { superior } ) ); //$NON-NLS-1$
            }
            else
            {
                // Adding a dependency on the superior attribute type
                attributeTypesDependencies.put( attributeType, superiorAT );

                // Computing the schema dependency
                computeSchemaDependency( schema, superiorAT );
            }
        }

        // Syntax OID
        String syntaxOID = attributeType.getSyntaxOid();
        if ( syntaxOID != null )
        {
            LdapSyntax syntax = schemaHandler.getSyntax( syntaxOID );
            if ( syntax == null )
            {
                throw new DependencyComputerException( NLS.bind(
                    Messages.getString( "DependenciesComputer.SyntaxOID" ), new String[] { syntaxOID } ) ); //$NON-NLS-1$
            }
            else
            {
                // Adding a dependency on the syntax
                attributeTypesDependencies.put( attributeType, syntax );

                // Computing the schema dependency
                computeSchemaDependency( schema, syntax );
            }
        }

        // Equality Matching Rule
        String equalityName = attributeType.getEqualityOid();
        if ( equalityName != null )
        {
            MatchingRule equalityMatchingRule = schemaHandler.getMatchingRule( equalityName );
            if ( equalityMatchingRule == null )
            {
                throw new DependencyComputerException( NLS.bind(
                    Messages.getString( "DependenciesComputer.Equality" ), new String[] { equalityName } ) ); //$NON-NLS-1$
            }
            else
            {
                // Adding a dependency on the syntax
                attributeTypesDependencies.put( attributeType, equalityMatchingRule );

                // Computing the schema dependency
                computeSchemaDependency( schema, equalityMatchingRule );
            }
        }

        // Ordering Matching Rule
        String orderingName = attributeType.getOrderingOid();
        if ( orderingName != null )
        {
            MatchingRule orderingMatchingRule = schemaHandler.getMatchingRule( orderingName );
            if ( orderingMatchingRule == null )
            {
                throw new DependencyComputerException( NLS.bind(
                    Messages.getString( "DependenciesComputer.Ordering" ), new String[] { orderingName } ) ); //$NON-NLS-1$
            }
            else
            {
                // Adding a dependency on the syntax
                attributeTypesDependencies.put( attributeType, orderingMatchingRule );

                // Computing the schema dependency
                computeSchemaDependency( schema, orderingMatchingRule );
            }
        }

        // Substring Matching Rule
        String substringName = attributeType.getSubstringOid();
        if ( substringName != null )
        {
            MatchingRule substringMatchingRule = schemaHandler.getMatchingRule( substringName );
            if ( substringMatchingRule == null )
            {
                throw new DependencyComputerException( NLS.bind(
                    Messages.getString( "DependenciesComputer.Substring" ), new String[] { substringName } ) ); //$NON-NLS-1$
            }
            else
            {
                // Adding a dependency on the syntax
                attributeTypesDependencies.put( attributeType, substringMatchingRule );

                // Computing the schema dependency
                computeSchemaDependency( schema, substringMatchingRule );
            }
        }
    }


    // ── Jerjerrod Audits Each Command Module ─────────────────────────────────────
    // An object class is the command module: it might inherit from one or more
    // superior classes and require certain attribute-type conduits (MUST/MAY lists).
    // Each of those is a dependency that has to be satisfied before this module
    // can go online. Jerjerrod lists them all, noting which foreign schema provides
    // each referenced piece.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Records every dependency that the given object class has — superior object
     * classes, optional attribute types (MAY), and mandatory attribute types (MUST).
     * For each one we also record the schema-level edge if the referenced object
     * lives in a different schema.
     *
     * <p>For example — Jerjerrod maps the command module:</p>
     * <pre>
     *   objectClass "inetOrgPerson" extends "organizationalPerson" (core)
     *   MAY: mail (core), telephoneNumber (core)
     *   → schema "inetOrgPerson" depends on schema "core"
     * </pre>
     *
     * @param schema       the schema that owns this object class
     * @param objectClass  the object class to analyse
     * @throws DependencyComputerException  if any referenced superior class or
     *         attribute type cannot be resolved
     */
    private void computeDependencies( Schema schema, ObjectClass objectClass ) throws DependencyComputerException
    {
        // Super Classes
        List<String> superClassesNames = objectClass.getSuperiorOids();
        if ( superClassesNames != null )
        {
            for ( String superClassName : superClassesNames )
            {
                ObjectClass superObjectClass = schemaHandler.getObjectClass( superClassName );
                if ( superObjectClass == null )
                {
                    throw new DependencyComputerException( NLS.bind( Messages
                        .getString( "DependenciesComputer.SuperiorObject" ), new String[] { superClassName } ) ); //$NON-NLS-1$
                }
                else
                {
                    // Adding a dependency on the syntax
                    objectClassesDependencies.put( objectClass, superObjectClass );

                    // Computing the schema dependency
                    computeSchemaDependency( schema, superObjectClass );
                }
            }
        }

        // Optional attribute types
        List<String> optionalAttributeTypes = objectClass.getMayAttributeTypeOids();
        if ( optionalAttributeTypes != null )
        {
            for ( String optionalAttributeTypeName : optionalAttributeTypes )
            {
                AttributeType optionalAttributeType = schemaHandler.getAttributeType( optionalAttributeTypeName );
                if ( optionalAttributeType == null )
                {
                    throw new DependencyComputerException( NLS.bind( Messages
                        .getString( "DependenciesComputer.Optional" ), new Object[] { optionalAttributeType } ) ); //$NON-NLS-1$
                }
                else
                {
                    // Adding a dependency on the syntax
                    objectClassesDependencies.put( objectClass, optionalAttributeType );

                    // Computing the schema dependency
                    computeSchemaDependency( schema, optionalAttributeType );
                }
            }
        }

        // Mandatory attribute types
        List<String> mandatoryAttributeTypes = objectClass.getMustAttributeTypeOids();
        if ( mandatoryAttributeTypes != null )
        {
            for ( String mandatoryAttributeTypeName : mandatoryAttributeTypes )
            {
                AttributeType mandatoryAttributeType = schemaHandler.getAttributeType( mandatoryAttributeTypeName );
                if ( mandatoryAttributeType == null )
                {
                    throw new DependencyComputerException( NLS.bind( Messages
                        .getString( "DependenciesComputer.Mandatory" ), new String[] //$NON-NLS-1$
                        { mandatoryAttributeTypeName } ) );
                }
                else
                {
                    // Adding a dependency on the syntax
                    objectClassesDependencies.put( objectClass, mandatoryAttributeType );

                    // Computing the schema dependency
                    computeSchemaDependency( schema, mandatoryAttributeType );
                }
            }
        }
    }


    // ── Jerjerrod Notes Which Foreign Sector Supplies The Part ───────────────────
    // When a panel from sector A needs a component manufactured in sector B,
    // Jerjerrod adds sector A → sector B to the inter-sector dependency ledger.
    // If sector B isn't even on the manifest, that's a hard stop — can't build
    // around a supplier that doesn't exist.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a schema-level dependency edge: if {@code object} lives in a different
     * schema than {@code schema}, we record that {@code schema} depends on that
     * foreign schema. We throw if the foreign schema isn't in our set at all.
     *
     * <p>For example — Jerjerrod notes the cross-sector supply:</p>
     * <pre>
     *   schema "custom" references object from schema "core"
     *   → schemasDependencies.put(custom, core)
     * </pre>
     *
     * @param schema  the schema that references the object
     * @param object  the schema object being referenced; its {@code schemaName}
     *                tells us which schema owns it
     * @throws DependencyComputerException  if the foreign schema cannot be found
     *         in the handler
     */
    private void computeSchemaDependency( Schema schema, SchemaObject object ) throws DependencyComputerException
    {
        String schemaName = object.getSchemaName();
        if ( !schemaName.equalsIgnoreCase( schema.getSchemaName() ) )
        {
            Schema schemaFromSuperiorAT = schemaHandler.getSchema( schemaName );
            if ( schemaFromSuperiorAT == null )
            {
                throw new DependencyComputerException( NLS.bind(
                    Messages.getString( "DependenciesComputer.Schema" ), new String[] { schemaName } ) ); //$NON-NLS-1$
            }
            else
            {
                // Adding a dependency on the schema of schema object
                schemasDependencies.put( schema, schemaFromSuperiorAT );
            }
        }
    }


    // ── Jerjerrod Sequences The Build Schedule ───────────────────────────────────
    // With all dependency edges recorded, Jerjerrod sorts the sector list so that
    // every sector appears after all the sectors it depends on — a classic
    // topological sort by repeated passes until the ordered list is full.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Performs a topological sort of our schemas based on the dependency edges
     * we computed earlier. We iterate until every schema has been placed in the
     * ordered list, skipping any schema whose dependencies haven't been placed yet
     * on each pass.
     *
     * <p>For example — Jerjerrod sequences the build:</p>
     * <pre>
     *   pass 1: core has no deps → add core
     *   pass 1: inetOrgPerson needs core (done) → add inetOrgPerson
     *   pass 1: custom needs inetOrgPerson (done) → add custom
     *   result: [core, inetOrgPerson, custom]
     * </pre>
     */
    private void orderSchemasBasedOnDependencies()
    {
        dependencyOrderedSchemasList = new ArrayList<Schema>();

        int counter = 0;
        schemasLoop: while ( dependencyOrderedSchemasList.size() != schemasList.size() )
        {
            Schema schema = schemasList.get( counter );

            if ( !dependencyOrderedSchemasList.contains( schema ) )
            {

                List<Schema> dependencies = getDependencies( schema );
                if ( dependencies == null )
                {
                    dependencyOrderedSchemasList.add( schema );
                }
                else
                {
                    for ( Schema dependency : dependencies )
                    {
                        if ( !dependencyOrderedSchemasList.contains( dependency ) )
                        {
                            counter = ++counter % schemasList.size();

                            continue schemasLoop;
                        }
                    }

                    dependencyOrderedSchemasList.add( schema );
                }
            }

            counter = ++counter % schemasList.size();
        }

    }


    // ── Jerjerrod Checks Which Sectors This One Needs ────────────────────────────
    // When planning a sector's installation, Jerjerrod checks the ledger to see
    // which other sectors must already be in place. The result is de-duped so
    // each dependency appears only once.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of schemas that the given schema depends on, de-duplicated.
     * This is the schema-level edge list from our dependency map. Callers use it
     * during the topological sort to decide whether a schema is ready to be placed.
     *
     * <p>For example — Jerjerrod checks the ledger:</p>
     * <pre>
     *   getDependencies(inetOrgPerson) → [core]
     *   getDependencies(core)          → []
     * </pre>
     *
     * @param schema  the schema whose dependencies we want
     * @return        a de-duplicated list of schemas that {@code schema} depends on;
     *                never null, but may be empty
     */
    @SuppressWarnings("unchecked")
    public List<Schema> getDependencies( Schema schema )
    {
        List<Schema> dependencies = ( List<Schema> ) schemasDependencies.get( schema );

        HashSet<Schema> set = new HashSet<Schema>();

        if ( dependencies != null )
        {
            set.addAll( dependencies );
        }

        return Arrays.asList( set.toArray( new Schema[0] ) );
    }


    // ── Jerjerrod Hands Over The Build Schedule ──────────────────────────────────
    // Once the topological sort is done, Jerjerrod passes the ordered sector list
    // to the construction crews — they work from top to bottom, never touching a
    // sector before its suppliers are in place.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the schemas sorted in dependency order — parents before children.
     * Hand this list to any exporter or schema-loader that needs to process schemas
     * in a safe sequence. The list is populated during construction.
     *
     * @return  the dependency-ordered list of schemas; never null after the
     *          constructor completes successfully
     */
    public List<Schema> getDependencyOrderedSchemasList()
    {
        return dependencyOrderedSchemasList;
    }


    // ── Jerjerrod Retrieves The Inter-Sector Dependency Map ──────────────────────
    // The full ledger of which schema depends on which other schema — useful for
    // callers that need to inspect the raw edge data rather than just the sorted list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw schema-to-schema dependency multi-map. Keys are schemas;
     * values are the schemas they depend on. Mostly useful for debugging or
     * for building a visual dependency graph in the UI.
     *
     * @return  the schemas-level dependency multi-map; never null
     */
    public MultiValuedMap getSchemasDependencies()
    {
        return schemasDependencies;
    }


    // ── Jerjerrod Retrieves The Attribute-Type Dependency Map ────────────────────
    // The full ledger of which attribute type depends on which other schema objects
    // (superiors, syntaxes, matching rules). Raw data for inspection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw attribute-type dependency multi-map. Keys are attribute types;
     * values are the schema objects they reference (superior types, syntaxes,
     * matching rules). Useful for callers that need fine-grained dependency info.
     *
     * @return  the attribute-types dependency multi-map; never null
     */
    public MultiValuedMap getAttributeTypesDependencies()
    {
        return attributeTypesDependencies;
    }


    // ── Jerjerrod Retrieves The Object-Class Dependency Map ──────────────────────
    // The full ledger of which object class depends on which other schema objects
    // (superior classes, MUST/MAY attribute types). Raw data for inspection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw object-class dependency multi-map. Keys are object classes;
     * values are the schema objects they reference (superior classes, attribute types).
     * Useful for callers that need fine-grained dependency info.
     *
     * @return  the object-classes dependency multi-map; never null
     */
    public MultiValuedMap getObjectClassesDependencies()
    {
        return objectClassesDependencies;
    }

    // ── CLASS: DependencyComputerException — Missing Supply On The Manifest ──────
    // Jerjerrod discovers a referenced sector that doesn't exist on the manifest —
    // the build cannot proceed, so he halts everything and files an error report.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Signals that a dependency could not be resolved during computation — a
     * referenced type, syntax, matching rule, or schema was not found in the
     * provided schema set.
     * Think of this as Jerjerrod's halt order: "We cannot proceed — sector not on
     * the manifest."
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public class DependencyComputerException extends Exception
    {
        private static final long serialVersionUID = 1L;


        // ── Jerjerrod Files The Error Report ─────────────────────────────────────────
        // Jerjerrod writes down exactly what's missing and why the build had to stop,
        // so the engineering team can track down the problem.
        // ────────────────────────────────────────────────────────────────────────────────
        /**
         * Creates a new exception with a human-readable message describing which
         * dependency was missing and why the computation had to stop.
         *
         * @param message  a plain-English description of what couldn't be resolved
         */
        public DependencyComputerException( String message )
        {
            super( message );
        }
    }
}
