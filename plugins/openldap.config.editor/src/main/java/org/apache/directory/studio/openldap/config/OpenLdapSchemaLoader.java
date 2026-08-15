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
package org.apache.directory.studio.openldap.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.apache.directory.api.ldap.model.schema.registries.Schema;
import org.apache.directory.api.ldap.model.schema.registries.DefaultSchema;
import org.apache.directory.api.ldap.schema.loader.JarLdifSchemaLoader;

// ── CLASS: OpenLdapSchemaLoader — R2-D2 Reads From The Bundled Data Cartridge ─
// In Episode IV, R2-D2 doesn't just plug into live systems — he also carries
// Princess Leia's pre-recorded message and the Death Star plans on his own storage.
// When plugged in, he can serve that data directly without reaching out to an
// external system. He parses the raw binary from his built-in cartridge, classifies
// each record (attribute type vs. object class), and makes it available on demand.
// We do the same thing here: instead of reading the OpenLDAP schema from a live
// server, we read it from the openldapconfig.ldif file bundled inside our own JAR.
// We parse it once during construction, sort entries into attribute types and object
// classes, and serve them when the schema manager asks.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link JarLdifSchemaLoader} that loads the OpenLDAP cn=config schema from a
 * bundled LDIF file (openldapconfig.ldif) packaged inside this plugin's JAR.
 * Unlike {@link ConnectionSchemaLoader}, we don't need a live connection — all
 * schema data comes from our own bundled resource, which we parse at construction
 * time and cache in two lists.
 * Think of us as R2-D2 reading from his own data cartridge: self-contained,
 * no external dependencies needed for the schema itself.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapSchemaLoader extends JarLdifSchemaLoader
{
    /** The name we register the schema under — "openldapconfig". */
    public static final String OPENLDAPCONFIG_SCHEMA_NAME = "openldapconfig";

    /** The bundled LDIF file that holds all the OpenLDAP config schema definitions. */
    private static final String OPENLDAPCONFIG_SHEMA_LDIF = "openldapconfig.ldif";

    /** Regex to recognize DN entries that describe attribute types in our schema. */
    private static final Pattern ATTRIBUTE_TYPE_PATTERN = Pattern.compile( "m-oid\\s*=\\s*[0-9\\.]*\\s*"
        + ",\\s*ou\\s*=\\s*attributetypes\\s*,\\s*cn\\s*=\\s*" + OPENLDAPCONFIG_SCHEMA_NAME + "\\s*,\\s*ou=schema\\s*",
        Pattern.CASE_INSENSITIVE );

    /** Regex to recognize DN entries that describe object classes in our schema. */
    private static final Pattern OBJECT_CLASS_PATTERN = Pattern.compile( "m-oid\\s*=\\s*[0-9\\.]*\\s*"
        + ",\\s*ou\\s*=\\s*objectclasses\\s*,\\s*cn\\s*=\\s*" + OPENLDAPCONFIG_SCHEMA_NAME + "\\s*,\\s*ou=schema\\s*",
        Pattern.CASE_INSENSITIVE );

    /** Pre-parsed attribute type entries from the bundled LDIF, ready to serve. */
    private List<Entry> attributeTypesEntries = new ArrayList<>();

    /** Pre-parsed object class entries from the bundled LDIF, ready to serve. */
    private List<Entry> objectClassesEntries = new ArrayList<>();


    // ── R2 Boots Up And Reads His Data Cartridge ─────────────────────────────
    // R2-D2 powers on, runs his self-test, then reads the contents of the data
    // cartridge into memory — classifying each record as a Death Star schematic
    // (attribute type) or a structural blueprint (object class) so he can serve
    // them instantly when asked.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the loader and immediately reads the bundled openldapconfig.ldif
     * into memory. We call initializeSchema() to register our "openldapconfig" schema,
     * then initializeSchemaObjects() to parse the LDIF and split its entries into
     * attribute types and object classes. After construction, we're ready to serve.
     *
     * @throws Exception  if the parent loader can't initialize, the LDIF can't be read,
     *                    or the LDIF content is invalid
     */
    public OpenLdapSchemaLoader() throws Exception
    {
        super();
        initializeSchema();
        initializeSchemaObjects();
    }


    // ── R2 Registers His Schema In The Fleet Directory ────────────────────────
    // Before R2 can serve any data, the fleet's computer needs to know his schema
    // exists — who it is, what it depends on (system, core, apache base data).
    // He registers his "openldapconfig" schema with those dependencies declared.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers the "openldapconfig" schema in the parent's schemaMap with the
     * correct dependencies on "system", "core", and "apache". The schema manager
     * uses this registration to know what order to load schemas in — our schema
     * depends on the base ApacheDS schemas, so it loads last.
     */
    private void initializeSchema()
    {
        Schema schema = new DefaultSchema( this, OPENLDAPCONFIG_SCHEMA_NAME );
        schema.addDependencies( "system", "core", "apache" );
        schemaMap.put( schema.getSchemaName(), schema );
    }


    // ── R2 Reads And Classifies Every Record In The Cartridge ────────────────
    // R2 reads through his data cartridge entry by entry. For each record, he
    // checks the DN: does it match the attribute type pattern, or the object class
    // pattern? He sorts them into the right bins so they can be retrieved quickly
    // later without re-reading the cartridge.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads the bundled openldapconfig.ldif and populates {@code attributeTypesEntries}
     * and {@code objectClassesEntries} by pattern-matching each entry's DN.
     * We use pre-compiled regex patterns to classify entries as attribute types or
     * object classes. Any entry that doesn't match either pattern is silently ignored
     * (it's a container entry like "ou=attributeTypes,cn=openldapconfig,ou=schema").
     *
     * @throws LdapException  if an entry in the LDIF is malformed
     * @throws IOException    if the bundled LDIF file can't be read from the classpath
     */
    private void initializeSchemaObjects() throws LdapException, IOException
    {
        // Reading the schema file
        try ( LdifReader ldifReader = new LdifReader( OpenLdapSchemaLoader.class.getResourceAsStream( OPENLDAPCONFIG_SHEMA_LDIF ) ) )
        {
            // Looping on all entries
            while ( ldifReader.hasNext() )
            {
                // Getting the LDIF entry and DN
                Entry entry = ldifReader.next().getEntry();
                String dn = entry.getDn().getName();

                // Checking if the entry is an attribute type
                if ( ATTRIBUTE_TYPE_PATTERN.matcher( dn ).matches() )
                {
                    attributeTypesEntries.add( entry );
                }
                // Checking if the entry is an object class
                else if ( OBJECT_CLASS_PATTERN.matcher( dn ).matches() )
                {
                    objectClassesEntries.add( entry );
                }
            }
        }
    }


    // ── R2 Serves The Attribute Type Records On Request ──────────────────────
    // Admiral Ackbar asks for the attribute type data. R2 first checks what the
    // base loader already has, then for the openldapconfig schema specifically, he
    // adds all the attribute types he parsed from his cartridge.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the combined list of attribute type entries for the requested schemas.
     * We start with whatever the parent loader provides, then for any schema named
     * "openldapconfig" we add all the attribute type entries we parsed from the
     * bundled LDIF.
     *
     * @param schemas  the schemas whose attribute types to load
     * @return         the combined list of attribute type entries
     * @throws LdapException  if the parent loader encounters a problem
     * @throws IOException    if the parent loader can't read its jar resources
     */
    @Override
    public List<Entry> loadAttributeTypes( Schema... schemas ) throws LdapException, IOException
    {
        // Getting the attribute types from the supertype implementation
        List<Entry> attributeTypes = super.loadAttributeTypes( schemas );

        for ( Schema schema : schemas )
        {
            // Checking if this is the OpenLDAP schema
            if ( OPENLDAPCONFIG_SCHEMA_NAME.equals( schema.getSchemaName() ) )
            {
                // Add all attribute types
                attributeTypes.addAll( attributeTypesEntries );
            }
        }

        return attributeTypes;
    }


    // ── R2 Serves The Object Class Records On Request ─────────────────────────
    // Same as above but for object class entries. The schema manager asks for
    // object classes; R2 gives the base ones plus everything from his cartridge
    // for the openldapconfig schema.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the combined list of object class entries for the requested schemas.
     * We start with whatever the parent loader provides, then for any schema named
     * "openldapconfig" we add all the object class entries we parsed from the
     * bundled LDIF.
     *
     * @param schemas  the schemas whose object classes to load
     * @return         the combined list of object class entries
     * @throws LdapException  if the parent loader encounters a problem
     * @throws IOException    if the parent loader can't read its jar resources
     */
    @Override
    public List<Entry> loadObjectClasses( Schema... schemas ) throws LdapException, IOException
    {
        // Getting the object classes from the supertype implementation
        List<Entry> objectClasses = super.loadObjectClasses( schemas );

        for ( Schema schema : schemas )
        {
            // Checking if this is the OpenLDAP schema
            if ( OPENLDAPCONFIG_SCHEMA_NAME.equals( schema.getSchemaName() ) )
            {
                // Add all object classes
                objectClasses.addAll( objectClassesEntries );
            }
        }

        return objectClasses;
    }
}
