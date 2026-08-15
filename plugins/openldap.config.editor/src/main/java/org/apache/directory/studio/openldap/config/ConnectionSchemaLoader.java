/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.apache.directory.studio.openldap.config;


import java.io.IOException;
import java.util.List;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.ObjectClassTypeEnum;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.api.ldap.model.schema.UsageEnum;
import org.apache.directory.api.ldap.model.schema.registries.DefaultSchema;
import org.apache.directory.api.ldap.model.schema.registries.Schema;
import org.apache.directory.api.ldap.model.schema.registries.SchemaLoader;
import org.apache.directory.api.ldap.schema.loader.JarLdifSchemaLoader;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;


// ── CLASS: ConnectionSchemaLoader — R2-D2 Plugs Into The Death Star ──────────
// In Episode IV, R2-D2 rolls up to a Death Star computer access port, jacks in,
// and starts pulling out everything the Rebels need — detention block locations,
// tractor beam controls, ship schematics. He reads the live system and translates
// raw binary into actionable intelligence for the team.
// We do the same thing here: connect to a live OpenLDAP server and suck out its
// schema (the "olc*" attribute types and object classes) so the editor can work
// with them as proper typed objects.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link SchemaLoader} that reads schema definitions directly from a live OpenLDAP
 * server connection rather than from a bundled LDIF file.
 * Sits on top of {@link JarLdifSchemaLoader} (which handles the base schemas from
 * ApacheDS like "system", "core", "apache") and layers the live "olc*" config-specific
 * attribute types and object classes on top.
 * Think of us as R2-D2 at the Death Star computer: we plug into the live connection,
 * extract what we need, and translate it into a form the rest of the plugin can
 * understand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionSchemaLoader extends JarLdifSchemaLoader
{
    private static final String M_COLLECTIVE = "m-collective";
    private static final String M_DESCRIPTION = "m-description";
    private static final String M_EQUALITY = "m-equality";
    private static final String M_LENGTH = "m-length";
    private static final String M_MAY = "m-may";
    private static final String M_MUST = "m-must";
    private static final String M_NAME = "m-name";
    private static final String M_NO_USER_MODIFICATION = "m-noUserModification";
    private static final String M_OBSOLETE = "m-obsolete";
    private static final String M_OID = "m-oid";
    private static final String M_ORDERING = "m-ordering";
    private static final String M_SINGLE_VALUE = "m-singleValue";
    private static final String M_SUBSTR = "m-substr";
    private static final String M_SUP_ATTRIBUTE_TYPE = "m-supAttributeType";
    private static final String M_SUP_OBJECT_CLASS = "m-supObjectClass";
    private static final String M_SYNTAX = "m-syntax";
    private static final String M_TYPE_OBJECT_CLASS = "m-typeObjectClass";
    private static final String M_USAGE = "m-usage";
    private static final String TRUE = "TRUE";

    /** The name we register under in the schemaMap — "connectionSchema". */
    public static final String CONNECTION_SCHEMA_NAME = "connectionSchema";

    /** OpenLDAP config attributes all start with "olc" — we filter on this prefix. */
    private static final String CONFIG_PREFIX = "olc";

    /** The live browser-side schema pulled from the connected OpenLDAP server. */
    private org.apache.directory.studio.ldapbrowser.core.model.schema.Schema browserConnectionSchema;


    // ── R2 Finds The Access Port And Plugs In ────────────────────────────────
    // R2-D2 locates the right computer terminal on the Death Star, rolls up,
    // and inserts his interface arm — establishing a live session with the ship's
    // main computer before he can pull any data.
    // We do the same: we find the browser's representation of this connection,
    // grab its live schema, then let initializeSchema() register our virtual
    // "connectionSchema" schema so downstream loaders know it exists.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new loader tied to a specific live connection.
     * We immediately grab the browser connection's schema (the live data from the
     * OpenLDAP server) and call initializeSchema() to register our virtual schema.
     * After this constructor returns, we're ready to serve attribute types and
     * object classes on demand.
     *
     * @param connection  the active Directory Studio connection to the OpenLDAP server;
     *                    must already be established — we'll pull the schema from it
     * @throws Exception  if the parent JarLdifSchemaLoader can't initialize, or if
     *                    anything goes wrong grabbing the browser connection schema
     */
    public ConnectionSchemaLoader( Connection connection ) throws Exception
    {
        super();

        // Getting the browser connection associated with the connection
        browserConnectionSchema = BrowserCorePlugin.getDefault().getConnectionManager()
            .getBrowserConnection( connection ).getSchema();

        initializeSchema();
    }


    // ── R2 Establishes His Session In The Databanks ──────────────────────────
    // Having plugged in, R2 sets up his working context inside the Death Star's
    // filing system — registering which data banks he depends on before he starts
    // pulling files.
    // We create a DefaultSchema named "connectionSchema", declare that it depends
    // on "system", "core", and "apache" (the base ApacheDS schemas), and add it
    // to the shared schemaMap so loadAttributeTypes / loadObjectClasses can find it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets up our virtual "connectionSchema" schema in the parent's schemaMap.
     * We declare dependencies on the three base ApacheDS schemas so the schema
     * manager loads them first before loading our connection-derived objects.
     * This is called once during construction and never again.
     */
    private void initializeSchema()
    {
        Schema schema = new DefaultSchema( null, CONNECTION_SCHEMA_NAME );

        schema.addDependencies( "system", "core", "apache" );
        schemaMap.put( schema.getSchemaName(), schema );
    }


    // ── R2 Pulls Attribute Records From The Databanks ────────────────────────
    // R2 has finished registering his session and now starts pulling data files.
    // He first grabs everything the parent computer already knows (the base
    // schemas), then asks the Death Star's config section specifically for all
    // attribute definitions that start with "olc" — OpenLDAP config attributes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full list of attribute type entries for the requested schemas.
     * We call the parent implementation first to get the base Apache DS attribute
     * types, then for any schema named "connectionSchema" we append all the
     * "olc*" attribute types we found in the live server's schema.
     *
     * @param schemas  the schemas whose attribute types we should load
     * @return         combined list of attribute type entries — base schemas plus
     *                 any live "olc*" types from the server
     * @throws LdapException  if building a schema entry fails
     * @throws IOException    if the underlying jar-based loader can't read its files
     */
    @Override
    public List<Entry> loadAttributeTypes( Schema... schemas ) throws LdapException, IOException
    {
        List<Entry> attributeTypes = super.loadAttributeTypes( schemas );

        for ( Schema schema : schemas )
        {
            if ( CONNECTION_SCHEMA_NAME.equals( schema.getSchemaName() ) )
            {
                for ( AttributeType attributeType : browserConnectionSchema.getAttributeTypeDescriptions() )
                {
                    if ( attributeType.getName().startsWith( CONFIG_PREFIX ) )
                    {
                        attributeTypes.add( convert( attributeType ) );
                    }
                }
            }
        }

        return attributeTypes;
    }


    // ── R2 Pulls Object Class Records From The Databanks ─────────────────────
    // Same as pulling attribute records, but now R2 is fetching the object class
    // definitions — the structural "what shape is this entry?" information that
    // defines which attributes belong to each config object type.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full list of object class entries for the requested schemas.
     * Just like {@link #loadAttributeTypes}, we start with the parent's base
     * object classes and then add all "olc*" object classes from the live server.
     *
     * @param schemas  the schemas whose object classes we should load
     * @return         combined list of object class entries
     * @throws LdapException  if building a schema entry fails
     * @throws IOException    if the underlying jar-based loader can't read its files
     */
    @Override
    public List<Entry> loadObjectClasses( Schema... schemas ) throws LdapException, IOException
    {
        List<Entry> objectClasses = super.loadObjectClasses( schemas );

        for ( Schema schema : schemas )
        {
            if ( CONNECTION_SCHEMA_NAME.equals( schema.getSchemaName() ) )
            {
                for ( ObjectClass objectClass : browserConnectionSchema.getObjectClassDescriptions() )
                {
                    if ( objectClass.getName().startsWith( CONFIG_PREFIX ) )
                    {
                        objectClasses.add( convert( objectClass ) );
                    }
                }
            }
        }

        return objectClasses;
    }


    // ── R2 Translates Raw Data Into Readable LDAP Entries ────────────────────
    // R2 intercepts a raw data packet from the Death Star's computer and has to
    // translate it from internal binary format into a structured, labeled record
    // that the Rebel Alliance command can actually read and act on.
    // We take an AttributeType object (the raw data) and convert it into an LDAP
    // Entry (the structured record) that the schema manager can register and use.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts a live {@link AttributeType} object into an LDAP schema entry.
     * The schema manager works with Entry objects (essentially key-value bags),
     * not strongly-typed Java objects, so we have to serialize each field of the
     * AttributeType into the appropriate LDAP attribute inside a new Entry.
     *
     * @param attributeType  the live attribute type to convert
     * @return               an Entry representation suitable for the schema manager
     * @throws LdapException  if adding any attribute to the entry fails
     */
    private Entry convert( AttributeType attributeType ) throws LdapException
    {
        // Creating a blank entry
        Entry entry = new DefaultEntry();

        // Setting calculated DN
        entry.setDn( getDn( attributeType, SchemaConstants.ATTRIBUTE_TYPES_PATH ) );

        // Values common to all schema objects
        addSchemaObjectValues( attributeType, SchemaConstants.META_ATTRIBUTE_TYPE_OC, entry );

        // Superior value
        addSuperiorValue( attributeType, entry );

        // Equality matching rule value
        addEqualityValue( attributeType, entry );

        // Ordering matching rule value
        addOrderingValue( attributeType, entry );

        // Substrings matching rule value
        addSubstrValue( attributeType, entry );

        // Syntax value
        addSyntaxValue( attributeType, entry );

        // Single value value
        addSingleValueValue( attributeType, entry );

        // Collective value
        addCollectiveValue( attributeType, entry );

        // No user modification value
        addNoUserModificationValue( attributeType, entry );

        // Usage value
        addUsageValue( attributeType, entry );

        return entry;
    }


    // ── R2 Translates An Object Class Record ─────────────────────────────────
    // Same translation task, but this time R2 is working with an object class
    // definition instead of an attribute type — the "shape" of a config entry
    // rather than a single field definition.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts a live {@link ObjectClass} object into an LDAP schema entry.
     * Object classes describe the structure of LDAP entries (which attributes
     * are required, which are optional, which superclass they inherit from).
     * We serialize all that into an Entry the schema manager can consume.
     *
     * @param objectClass  the live object class to convert
     * @return             an Entry representation suitable for the schema manager
     * @throws LdapException  if adding any attribute to the entry fails
     */
    private Entry convert( ObjectClass objectClass ) throws LdapException
    {
        // Creating a blank entry
        Entry entry = new DefaultEntry();

        // Setting calculated DN
        entry.setDn( getDn( objectClass, SchemaConstants.OBJECT_CLASSES_PATH ) );

        // Values common to all schema objects
        addSchemaObjectValues( objectClass, SchemaConstants.META_OBJECT_CLASS_OC, entry );

        // Superiors value
        addSuperiorsValue( objectClass, entry );

        // Class type value
        addClassTypeValue( objectClass, entry );

        // Musts value
        addMustsValue( objectClass, entry );

        // Mays value
        addMaysValue( objectClass, entry );

        return entry;
    }


    // ── R2 Computes The File Location In The Death Star's Filing System ───────
    // Having decoded a record, R2 needs to figure out exactly where in the Death
    // Star's hierarchical data structure this record lives — its full coordinate
    // path so retrieval commands can find it later.
    // We build the canonical LDAP DN for a schema object so the schema manager
    // can store it in the right spot in the directory tree.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the LDAP DN for a given schema object within a given container path.
     * For example, an attribute type with OID 1.2.3.4 ends up at something like:
     * {@code m-oid=1.2.3.4,ou=attributeTypes,cn=connectionSchema,ou=schema}.
     * This DN is what the schema manager uses to locate the entry in the virtual
     * schema tree.
     *
     * @param so         the schema object (attribute type or object class)
     * @param container  the container path — e.g. "ou=attributeTypes"
     * @return           the fully qualified DN for this schema object
     * @throws LdapInvalidDnException                if the DN can't be built
     * @throws LdapInvalidAttributeValueException    if the OID string is malformed
     */
    private Dn getDn( SchemaObject so, String container ) throws LdapInvalidDnException, LdapInvalidAttributeValueException
    {
        return Dn.EMPTY_DN
            .add( new Rdn( SchemaConstants.OU_SCHEMA ) )
            .add( new Rdn( SchemaConstants.CN_AT, Rdn.escapeValue( CONNECTION_SCHEMA_NAME ) ) )
            .add( new Rdn( container ) )
            .add( new Rdn( "m-oid", so.getOid() ) );
    }


    // ── R2 Stamps The Common Fields Onto Every Record ────────────────────────
    // Every data file R2 pulls from the Death Star has the same header: object
    // class, identifier, names, description, and obsolete flag. He stamps these
    // onto every record before filling in the type-specific details.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates the fields that every schema object (attribute type or object class)
     * shares: objectClass, OID, names, description, and obsolete flag.
     * We call this for every entry we're building before calling the
     * type-specific add methods.
     *
     * @param schemaObject      the source schema object to read from
     * @param objectClassValue  the specific objectClass value for this type of schema object
     * @param entry             the target entry we're populating
     * @throws LdapException    if adding any attribute value fails
     */
    private static void addSchemaObjectValues( SchemaObject schemaObject, String objectClassValue, Entry entry )
        throws LdapException
    {
        // ObjectClass
        addObjectClassValue( schemaObject, objectClassValue, entry );

        // OID
        addOidValue( schemaObject, entry );

        // Names
        addNamesValue( schemaObject, entry );

        // Description
        addDescriptionValue( schemaObject, entry );

        // Obsolete
        addObsoleteValue( schemaObject, entry );
    }


    // ── R2 Stamps The Object Class Identity Badge ─────────────────────────────
    // Every file in the Death Star's system has a type header — R2 adds the
    // "top", "metaTop", and specific object class identifiers so the system
    // knows how to interpret the record.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the objectClass attribute to the entry with the standard schema hierarchy.
     * Every schema entry needs "top" and "metaTop" as superclasses, plus the
     * specific type (e.g. "metaAttributeType" or "metaObjectClass").
     *
     * @param schemaObject      the schema object being converted (used for context, not read here)
     * @param objectClassValue  the most-specific objectClass value for this entry
     * @param entry             the entry to populate
     * @throws LdapException    if adding the attribute fails
     */
    private static void addObjectClassValue( SchemaObject schemaObject, String objectClassValue, Entry entry )
        throws LdapException
    {
        Attribute objectClassAttribute = new DefaultAttribute( SchemaConstants.OBJECT_CLASS_AT );
        entry.add( objectClassAttribute );
        objectClassAttribute.add( SchemaConstants.TOP_OC );
        objectClassAttribute.add( SchemaConstants.META_TOP_OC );
        objectClassAttribute.add( objectClassValue );
    }


    // ── R2 Records The Unique Identifier ─────────────────────────────────────
    // R2 stamps the object's unique OID onto the record — without it, the schema
    // manager has no way to uniquely identify this definition among the thousands
    // of others in the system.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the m-oid attribute to the entry if the schema object has a non-empty OID.
     * OIDs (Object Identifiers) are globally unique dotted-numeric strings like
     * "1.3.6.1.4.1.4203.1.5.1" that uniquely identify a schema element. If the
     * object has no OID we just skip this step.
     *
     * @param schemaObject  the schema object to read the OID from
     * @param entry         the entry to add the OID to
     * @throws LdapException  if adding the attribute fails
     */
    private static void addOidValue( SchemaObject schemaObject, Entry entry ) throws LdapException
    {
        String oid = schemaObject.getOid();

        if ( !Strings.isEmpty( oid ) )
        {
            Attribute attribute = new DefaultAttribute( M_OID, oid );
            entry.add( attribute );
        }
    }


    // ── R2 Logs The Human-Readable Names ─────────────────────────────────────
    // OIDs are unique but unreadable — R2 also grabs the friendly names (like
    // "olcDatabase" or "cn") so humanoids can work with them without memorizing
    // long number strings.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the m-name attribute(s) to the entry for each of the schema object's
     * human-readable names. Schema elements can have multiple names (e.g. "cn"
     * and "commonName" both refer to the same attribute type). We skip this if
     * there are no names.
     *
     * @param schemaObject  the schema object to read names from
     * @param entry         the entry to add the names to
     * @throws LdapException  if adding the attribute fails
     */
    private static void addNamesValue( SchemaObject schemaObject, Entry entry ) throws LdapException
    {
        List<String> names = schemaObject.getNames();

        if ( ( names != null ) && !names.isEmpty() )
        {
            Attribute attribute = new DefaultAttribute( M_NAME );
            entry.add( attribute );

            for ( String name : names )
            {
                attribute.add( name );
            }
        }
    }


    // ── R2 Appends The Description Field ─────────────────────────────────────
    // R2 adds any available human-readable description to the record — the
    // free-text annotation explaining what this attribute or class is for.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the m-description attribute to the entry if the schema object has one.
     * Descriptions are free-text notes embedded in the schema definition — handy
     * for understanding what an attribute is for but not required for correctness.
     *
     * @param schemaObject  the schema object to read the description from
     * @param entry         the entry to add the description to
     * @throws LdapException  if adding the attribute fails
     */
    private static void addDescriptionValue( SchemaObject schemaObject, Entry entry ) throws LdapException
    {
        String description = schemaObject.getDescription();

        if ( !Strings.isEmpty( description ) )
        {
            Attribute attribute = new DefaultAttribute( M_DESCRIPTION, description );
            entry.add( attribute );
        }
    }


    // ── R2 Flags Obsolete Records ─────────────────────────────────────────────
    // Some entries in the Death Star's databanks are marked as legacy — still
    // present for historical reasons but no longer used. R2 faithfully copies
    // that flag into the new record.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the m-obsolete attribute ("TRUE") to the entry if the schema object is
     * marked as obsolete. An obsolete schema element exists in the schema for
     * backwards compatibility but shouldn't be used in new entries.
     *
     * @param schemaObject  the schema object to check for obsolescence
     * @param entry         the entry to add the flag to
     * @throws LdapException  if adding the attribute fails
     */
    private static void addObsoleteValue( SchemaObject schemaObject, Entry entry ) throws LdapException
    {
        if ( schemaObject.isObsolete() )
        {
            Attribute attribute = new DefaultAttribute( M_OBSOLETE, TRUE );
            entry.add( attribute );
        }
    }


    // ── R2 Records The Parent Attribute In The Hierarchy ─────────────────────
    // The Death Star's data structure is hierarchical — some attribute types
    // inherit from parent types. R2 records which parent this type derives from
    // so the schema manager can build the inheritance chain correctly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the m-supAttributeType attribute to the entry if the attribute type
     * has a superior (parent) type. Attribute type inheritance means a child type
     * inherits syntax and matching rules from its parent unless it overrides them.
     *
     * @param attributeType  the attribute type to check for a superior
     * @param entry          the entry to add the superior name to
     * @throws LdapException  if adding the attribute fails
     */
    private static void addSuperiorValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        String superior = attributeType.getSuperiorName();

        if ( !Strings.isEmpty( superior ) )
        {
            Attribute attribute = new DefaultAttribute( M_SUP_ATTRIBUTE_TYPE, superior );
            entry.add( attribute );
        }
    }


    // ── R2 Records The Equality Matching Rule ────────────────────────────────
    // R2 notes how the Death Star's system tests whether two values of this
    // attribute type are equal — because "cn=Luke" and "cn=luke" might or might
    // not be the same depending on the rule chosen.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the m-equality attribute to the entry if the attribute type has an
     * equality matching rule defined. This rule controls how LDAP equality filters
     * (e.g. "(cn=Luke)") are evaluated. Without it, the attribute might not be
     * searchable with equality filters.
     *
     * @param attributeType  the attribute type to check
     * @param entry          the entry to add the equality rule name to
     * @throws LdapException  if adding the attribute fails
     */
    private static void addEqualityValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        String equality = attributeType.getEqualityName();

        if ( !Strings.isEmpty( equality ) )
        {
            Attribute attribute = new DefaultAttribute( M_EQUALITY, equality );
            entry.add( attribute );
        }
    }


    // ── R2 Records The Ordering Matching Rule ────────────────────────────────
    // The Death Star's computer can sort records by certain fields — R2 records
    // the ordering rule so range queries (like "find all entries where index > 5")
    // work correctly for this attribute type.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the m-ordering attribute to the entry if the attribute type has an
     * ordering matching rule. This enables LDAP greater-than/less-than filters
     * on this attribute type. Many string attributes don't define ordering.
     *
     * @param attributeType  the attribute type to check
     * @param entry          the entry to add the ordering rule name to
     * @throws LdapException  if adding the attribute fails
     */
    private static void addOrderingValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        String ordering = attributeType.getOrderingName();

        if ( !Strings.isEmpty( ordering ) )
        {
            Attribute attribute = new DefaultAttribute( M_ORDERING, ordering );
            entry.add( attribute );
        }
    }


    // ── R2 Records The Substring Matching Rule ───────────────────────────────
    // R2 notes the substring matching rule — the rule that makes wildcard searches
    // like "(cn=L*ke)" work. Not all attribute types support substring searches.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the m-substr attribute to the entry if the attribute type has a
     * substring matching rule. This is what powers LDAP wildcard searches
     * (e.g. "(cn=Sky*er)"). Attributes without this rule can't be wildcard-searched.
     *
     * @param attributeType  the attribute type to check
     * @param entry          the entry to add the substring rule name to
     * @throws LdapException  if adding the attribute fails
     */
    private static void addSubstrValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        String substr = attributeType.getSubstringName();

        if ( !Strings.isEmpty( substr ) )
        {
            Attribute attribute = new DefaultAttribute( M_SUBSTR, substr );
            entry.add( attribute );
        }
    }


    // ── R2 Records The Data Format And Max Length ─────────────────────────────
    // R2 captures the syntax (the data format — like "must be a DN" or "must be
    // an integer") plus any length constraint so the server knows what valid
    // values look like for this attribute.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the m-syntax (and optionally m-length) attributes to the entry.
     * The syntax OID tells the server what kind of value this attribute holds
     * (distinguished name, integer, boolean, generalized time, etc.). The length
     * constraint, if present, caps the number of characters allowed.
     *
     * @param attributeType  the attribute type to read syntax info from
     * @param entry          the entry to add syntax (and length) to
     * @throws LdapException  if adding any attribute fails
     */
    private static void addSyntaxValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        String syntax = attributeType.getSyntaxName();

        if ( !Strings.isEmpty( syntax ) )
        {
            Attribute attribute = new DefaultAttribute( M_SYNTAX, syntax );
            entry.add( attribute );

            long syntaxLength = attributeType.getSyntaxLength();

            if ( syntaxLength != -1 )
            {
                attribute = new DefaultAttribute( M_LENGTH, Long.toString( syntaxLength ) );
                entry.add( attribute );
            }
        }
    }


    // ── R2 Notes If The Field Can Only Hold One Value ─────────────────────────
    // Some fields in the Death Star's records are single-value fields (like a
    // serial number) while others are multi-value (like a list of callsigns).
    // R2 flags single-value fields explicitly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the m-singleValue attribute ("TRUE") to the entry if this attribute
     * type is single-valued. By default LDAP attributes are multi-valued, so we
     * only need to add this flag when it's explicitly single-valued.
     *
     * @param attributeType  the attribute type to check
     * @param entry          the entry to add the flag to
     * @throws LdapException  if adding the attribute fails
     */
    private static void addSingleValueValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        if ( attributeType.isSingleValued() )
        {
            Attribute attribute = new DefaultAttribute( M_SINGLE_VALUE, TRUE );
            entry.add( attribute );
        }
    }


    // ── R2 Notes If The Field Is A Collective Attribute ───────────────────────
    // Some attributes are "collective" — they're inherited by all entries in a
    // subtree from a special collective entry. R2 flags these since they behave
    // differently from regular attributes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the m-collective attribute ("TRUE") to the entry if this attribute
     * type is marked as collective. Collective attributes are a specialized LDAP
     * feature (RFC 3671) — they flow down the DIT from special "collective"
     * subtree specification entries. Rare in practice but we need to preserve the flag.
     *
     * @param attributeType  the attribute type to check
     * @param entry          the entry to add the collective flag to
     * @throws LdapException  if adding the attribute fails
     */
    private static void addCollectiveValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        if ( attributeType.isCollective() )
        {
            Attribute attribute = new DefaultAttribute( M_COLLECTIVE, TRUE );
            entry.add( attribute );
        }
    }


    // ── R2 Marks Operational (Read-Only) Attributes ───────────────────────────
    // Some fields in the Death Star's records are system-controlled and can't be
    // changed by users — like a creation timestamp. R2 records this restriction
    // so the server enforces it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the m-noUserModification attribute ("TRUE") to the entry if this
     * attribute type is not user-modifiable. These are called "operational"
     * attributes — things like "createTimestamp" that the server manages
     * automatically and clients aren't allowed to modify directly.
     *
     * @param attributeType  the attribute type to check
     * @param entry          the entry to add the restriction flag to
     * @throws LdapException  if adding the attribute fails
     */
    private static void addNoUserModificationValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        if ( !attributeType.isUserModifiable() )
        {
            Attribute attribute = new DefaultAttribute( M_NO_USER_MODIFICATION, TRUE );
            entry.add( attribute );
        }
    }


    // ── R2 Records The Attribute's Intended Purpose ───────────────────────────
    // R2 stamps the "usage" field: is this attribute for user applications, or
    // is it an internal operational attribute used by directory system agents or
    // distributed operations? Anything that's not plain user data gets flagged.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the m-usage attribute to the entry if the usage is not USER_APPLICATIONS.
     * The four possible values are: USER_APPLICATIONS (default, skip), DIRECTORY_OPERATION,
     * DISTRIBUTED_OPERATION, and DSA_OPERATION. We only serialize the non-default
     * value since USER_APPLICATIONS is implied when the attribute is absent.
     *
     * @param attributeType  the attribute type to check
     * @param entry          the entry to add the usage value to
     * @throws LdapException  if adding the attribute fails
     */
    private static void addUsageValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        UsageEnum usage = attributeType.getUsage();

        if ( usage != UsageEnum.USER_APPLICATIONS )
        {
            Attribute attribute = new DefaultAttribute( M_USAGE, usage.render() );
            entry.add( attribute );
        }
    }


    // ── R2 Records The Object Class Parent Hierarchy ──────────────────────────
    // Object class definitions have parent classes they inherit from — R2 notes
    // all the superclasses so the schema manager can build the full inheritance
    // chain and know which attributes each class inherits.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the m-supObjectClass attribute(s) to the entry for each of the object
     * class's superiors. Object classes can have multiple superclasses (multiple
     * inheritance), which is why this takes a list rather than a single value.
     *
     * @param objectClass  the object class to read superiors from
     * @param entry        the entry to add the superior names to
     * @throws LdapException  if adding the attribute fails
     */
    private static void addSuperiorsValue( ObjectClass objectClass, Entry entry ) throws LdapException
    {
        List<String> superiors = objectClass.getSuperiorOids();

        if ( ( superiors != null ) && !superiors.isEmpty() )
        {
            Attribute attribute = new DefaultAttribute( M_SUP_OBJECT_CLASS );
            entry.add( attribute );

            for ( String superior : superiors )
            {
                attribute.add( superior );
            }
        }
    }


    // ── R2 Flags Non-Structural Class Types ───────────────────────────────────
    // Not all object classes are structural (the primary class of an entry).
    // Some are auxiliary (supplemental attributes you mix in) or abstract
    // (templates you can't instantiate directly). R2 records non-structural types.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the m-typeObjectClass attribute to the entry if the class type is
     * not STRUCTURAL. STRUCTURAL is the default so we skip it to keep the entry
     * clean. AUXILIARY and ABSTRACT classes need the flag explicitly set.
     *
     * @param objectClass  the object class to check the type of
     * @param entry        the entry to add the class type to
     * @throws LdapException  if adding the attribute fails
     */
    private static void addClassTypeValue( ObjectClass objectClass, Entry entry ) throws LdapException
    {
        ObjectClassTypeEnum classType = objectClass.getType();

        if ( classType != ObjectClassTypeEnum.STRUCTURAL )
        {
            Attribute attribute = new DefaultAttribute( M_TYPE_OBJECT_CLASS, classType.toString() );
            entry.add( attribute );
        }
    }


    // ── R2 Records The Mandatory Attributes ───────────────────────────────────
    // Every object class has a list of attributes that MUST be present — if they're
    // missing, the entry is invalid. R2 faithfully lists them so the schema manager
    // can enforce them during add/modify operations.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the m-must attribute(s) to the entry for each required attribute in
     * the object class. An entry of this class must have all "MUST" attributes
     * present, or the server will reject it as invalid.
     *
     * @param objectClass  the object class to read required attributes from
     * @param entry        the entry to add the must-attribute list to
     * @throws LdapException  if adding the attribute fails
     */
    private static void addMustsValue( ObjectClass objectClass, Entry entry ) throws LdapException
    {
        List<String> musts = objectClass.getMustAttributeTypeOids();

        if ( ( musts != null ) && !musts.isEmpty() )
        {
            Attribute attribute = new DefaultAttribute( M_MUST );
            entry.add( attribute );

            for ( String must : musts )
            {
                attribute.add( must );
            }
        }
    }


    // ── R2 Records The Optional Attributes ────────────────────────────────────
    // Beyond the mandatory attributes, object classes also list optional ones
    // that MAY be present. R2 copies the whole may-list so the schema manager
    // knows which attributes are valid (even if not required) for entries of
    // this class.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the m-may attribute(s) to the entry for each optional attribute in
     * the object class. MAY attributes can be present or absent — they're valid
     * but not required. The server uses this list to reject unknown attributes.
     *
     * @param objectClass  the object class to read optional attributes from
     * @param entry        the entry to add the may-attribute list to
     * @throws LdapException  if adding the attribute fails
     */
    private static void addMaysValue( ObjectClass objectClass, Entry entry ) throws LdapException
    {
        List<String> mays = objectClass.getMayAttributeTypeOids();

        if ( ( mays != null ) && !mays.isEmpty() )
        {
            Attribute attribute = new DefaultAttribute( M_MAY );
            entry.add( attribute );

            for ( String may : mays )
            {
                attribute.add( may );
            }
        }
    }
}
