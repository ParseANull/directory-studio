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

package org.apache.directory.studio.ldapbrowser.core.model.schema;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.MatchingRuleUse;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.UsageEnum;
import org.apache.directory.api.ldap.model.schema.parsers.AttributeTypeDescriptionSchemaParser;
import org.apache.directory.api.ldap.model.schema.parsers.LdapSyntaxDescriptionSchemaParser;
import org.apache.directory.api.ldap.model.schema.parsers.MatchingRuleDescriptionSchemaParser;
import org.apache.directory.api.ldap.model.schema.parsers.MatchingRuleUseDescriptionSchemaParser;
import org.apache.directory.api.ldap.model.schema.parsers.ObjectClassDescriptionSchemaParser;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeDescription;
import org.apache.directory.studio.ldifparser.LdifFormatParameters;
import org.apache.directory.studio.ldifparser.model.LdifEnumeration;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifContentRecord;
import org.apache.directory.studio.ldifparser.model.lines.LdifAttrValLine;
import org.apache.directory.studio.ldifparser.parser.LdifParser;


// ── CLASS: Schema — THE JEDI ARCHIVES ────────────────────────────────────────
// The Jedi Archives hold the authoritative record of everything the Order knows:
// object classes, attribute types, LDAP syntaxes, matching rules, and their uses.
// Schema is that archive: loaded from an LDIF subschema entry, it provides fast
// lookup by name or OID, falls back to the default schema, and fabricates dummy
// entries rather than returning null when an unknown element is requested.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Central access point to all LDAP schema information for a single directory
 * connection.  Parses a subschema LDIF record into five lookup maps (object
 * classes, attribute types, LDAP syntaxes, matching rules, matching rule uses)
 * keyed by lowercase name or OID.  Falls back to {@link #DEFAULT_SCHEMA} and
 * synthesises dummy entries for unknowns rather than throwing.
 *
 * <p>Think of this as the Jedi Archives — one vault containing every known
 * schema element, with a fallback to the default holocron and a dummy entry
 * for anything the Archives have never heard of.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Schema
{

    public static final String SCHEMA_FILTER = "(objectClass=subschema)"; //$NON-NLS-1$

    public static final String RAW_SCHEMA_DEFINITION_LDIF_VALUE = "X-RAW-SCHEMA-DEFINITION"; //$NON-NLS-1$

    public static final String DN_SYNTAX_OID = "1.3.6.1.4.1.1466.115.121.1.12"; //$NON-NLS-1$

    public static final LdapSyntax DUMMY_LDAP_SYNTAX;
    static
    {
        DUMMY_LDAP_SYNTAX = new LdapSyntax( "", "" ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static final HashMap<String, List<String>> DUMMY_EXTENSIONS;
    static
    {
        DUMMY_EXTENSIONS = new HashMap<String, List<String>>();
        List<String> dummyValues = new ArrayList<String>();
        dummyValues.add( "DUMMY" ); //$NON-NLS-1$
        DUMMY_EXTENSIONS.put( "X-DUMMY", dummyValues ); //$NON-NLS-1$
    }

    public static final Schema DEFAULT_SCHEMA;
    static
    {
        Schema defaultSchema = null;

        try
        {
            URL url = Schema.class.getClassLoader().getResource( "default_schema.ldif" ); //$NON-NLS-1$
            InputStream is = url.openStream();
            Reader reader = new InputStreamReader( is );

            defaultSchema = new Schema();
            defaultSchema.defaultSchema = true;
            defaultSchema.loadFromLdif( reader );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        DEFAULT_SCHEMA = defaultSchema;
    }

    private boolean defaultSchema = false;


    // ── Mace Windu Checks Whether These Are The Default Jedi Archives ────────────
    // Mace confronts the question: "are you the original holographic Archives?"
    // The default schema is the bundled default_schema.ldif baked into the plugin.
    // Non-default schemas are loaded live from a server and may lack some elements.
    // Getter logic uses this flag to decide whether to fall back or fabricate dummies.
    /**
     * Returns whether this is the bundled default schema instance.
     *
     * @return {@code true} if this schema was loaded from the built-in
     *         {@code default_schema.ldif} resource
     */
    public boolean isDefault()
    {
        return this.defaultSchema;
    }

    private LdifContentRecord schemaRecord;

    private Dn dn;

    private String createTimestamp;

    private String modifyTimestamp;

    private Map<String, ObjectClass> ocdMapByNameOrNumericOid;

    private Map<String, AttributeType> atdMapByNameOrNumericOid;

    private Map<String, LdapSyntax> lsdMapByNumericOid;

    private Map<String, MatchingRule> mrdMapByNameOrNumericOid;

    private Map<String, MatchingRuleUse> mrudMapByNameOrNumericOid;


    // ── Archivist Opens Five Empty Vaults In The Jedi Archives ───────────────────
    // When a new schema wing is commissioned, five vaults are prepared:
    // object classes, attribute types, LDAP syntaxes, matching rules, and uses.
    // Each vault starts empty; elements are filed via load methods or direct add.
    // The schemaRecord, dn, and timestamp fields are null until loadFromLdif runs.
    /**
     * Creates a new instance of Schema.
     */
    public Schema()
    {
        this.schemaRecord = null;
        this.dn = null;
        this.createTimestamp = null;
        this.modifyTimestamp = null;
        this.ocdMapByNameOrNumericOid = new HashMap<String, ObjectClass>();
        this.atdMapByNameOrNumericOid = new HashMap<String, AttributeType>();
        this.lsdMapByNumericOid = new HashMap<String, LdapSyntax>();
        this.mrdMapByNameOrNumericOid = new HashMap<String, MatchingRule>();
        this.mrudMapByNameOrNumericOid = new HashMap<String, MatchingRuleUse>();
    }


    // ── R2-D2 Plugs Into The LDIF Holocron And Streams Schema Into The Vaults ────
    // R2-D2 inserts his interface into a raw LDIF stream — the schema holocron.
    // He iterates every LdifContentRecord inside and hands each to parseSchemaRecord.
    // Errors are logged to stdout rather than re-thrown; the vaults stay partial.
    // This is the preferred ingestion path when reading schema from a file or URL.
    /**
     * Loads all schema elements from the given reader.  The input must be in
     * LDIF format.
     *
     * @param reader the reader supplying LDIF-formatted schema data
     */
    public void loadFromLdif( Reader reader )
    {
        try
        {
            LdifParser parser = new LdifParser();
            LdifEnumeration enumeration = parser.parse( reader );
            while ( enumeration.hasNext() )
            {
                LdifContainer container = enumeration.next();
                if ( container instanceof LdifContentRecord )
                {
                    LdifContentRecord schemaRecord = ( LdifContentRecord ) container;
                    parseSchemaRecord( schemaRecord );
                }
            }
        }
        catch ( Exception e )
        {
            // TODO: exception handling
            System.out.println( "Schema#loadFromLdif: " + e.toString() ); //$NON-NLS-1$
        }
    }


    // ── R2-D2 Loads Schema From A Pre-Parsed LDIF Record ────────────────────────
    // When the connection layer has already parsed the subschema entry into an
    // LdifContentRecord, R2-D2 skips the LDIF parsing pass and feeds it directly.
    // This is the live-connection ingestion path: one record, one call to parse.
    // Errors are logged to stdout; the vaults stay partially filled on failure.
    /**
     * Loads all schema elements from the given schema record.
     *
     * @param schemaRecord the pre-parsed LDIF content record holding schema data
     */
    public void loadFromRecord( LdifContentRecord schemaRecord )
    {
        try
        {
            parseSchemaRecord( schemaRecord );
        }
        catch ( Exception e )
        {
            // TODO: exception handling
            System.out.println( "Schema#loadFromRecord: " + e.toString() ); //$NON-NLS-1$
        }
    }


    // ── R2-D2 Writes The Jedi Archives Back To A Holocron ────────────────────────
    // R2-D2 serialises the entire in-memory schema record back to LDIF format.
    // He writes the raw schemaRecord in its default pretty-print layout.
    // Any IO error is caught and printed to stdout rather than re-thrown.
    // The writer is not flushed or closed by this method; callers must do that.
    /**
     * Saves the schema in LDIF format to the given writer.
     *
     * @param writer the writer to receive the LDIF-formatted schema output
     */
    public void saveToLdif( Writer writer )
    {
        try
        {
            writer.write( getSchemaRecord().toFormattedString( LdifFormatParameters.DEFAULT ) );
        }
        catch ( Exception e )
        {
            // TODO: exception handling
            System.out.println( "Schema#saveToLdif: " + e.toString() ); //$NON-NLS-1$
        }
    }


    // ── C-3PO Reads The Subschema Entry And Files Every Element Into Its Vault ────
    // C-3PO scans every attribute-value line of the subschema LDIF record.
    // He recognises five types — objectClasses, attributeTypes, ldapSyntaxes,
    // matchingRules, matchingRuleUse — and routes each to its add helper.
    // After filing, he patches up missing syntaxes and matching rules with dummies,
    // then sets extensibleObject's may-list from all known user attributes.
    /**
     * Parses the schema record and populates the five internal lookup maps.
     *
     * @param schemaRecord the LDIF content record holding the subschema entry
     * @throws Exception if DN parsing or an individual schema element parse fails
     */
    private void parseSchemaRecord( LdifContentRecord schemaRecord ) throws Exception
    {
        setSchemaRecord( schemaRecord );
        setDn( new Dn( schemaRecord.getDnLine().getValueAsString() ) );

        ObjectClassDescriptionSchemaParser ocdPparser = new ObjectClassDescriptionSchemaParser();
        ocdPparser.setQuirksMode( true );
        AttributeTypeDescriptionSchemaParser atdParser = new AttributeTypeDescriptionSchemaParser();
        atdParser.setQuirksMode( true );
        LdapSyntaxDescriptionSchemaParser lsdParser = new LdapSyntaxDescriptionSchemaParser();
        lsdParser.setQuirksMode( true );
        MatchingRuleDescriptionSchemaParser mrdParser = new MatchingRuleDescriptionSchemaParser();
        mrdParser.setQuirksMode( true );
        MatchingRuleUseDescriptionSchemaParser mrudParser = new MatchingRuleUseDescriptionSchemaParser();
        mrudParser.setQuirksMode( true );

        LdifAttrValLine[] lines = schemaRecord.getAttrVals();
        for ( int i = 0; i < lines.length; i++ )
        {
            LdifAttrValLine line = lines[i];
            String attributeName = line.getUnfoldedAttributeDescription();
            String value = line.getValueAsString();
            List<String> ldifValues = new ArrayList<String>( 1 );
            ldifValues.add( value );

            try
            {
                if ( attributeName.equalsIgnoreCase( SchemaConstants.OBJECT_CLASSES_AT ) )
                {
                    ObjectClass ocd = ocdPparser.parse( value );
                    ocd.addExtension( RAW_SCHEMA_DEFINITION_LDIF_VALUE, ldifValues );
                    addObjectClass( ocd );
                }
                else if ( attributeName.equalsIgnoreCase( SchemaConstants.ATTRIBUTE_TYPES_AT ) )
                {
                    AttributeType atd = atdParser.parse( value );
                    atd.addExtension( RAW_SCHEMA_DEFINITION_LDIF_VALUE, ldifValues );
                    addAttributeType( atd );
                }
                else if ( attributeName.equalsIgnoreCase( SchemaConstants.LDAP_SYNTAXES_AT ) )
                {
                    LdapSyntax lsd = lsdParser.parse( value );
                    if ( StringUtils.isEmpty( lsd.getDescription() )
                        && Utils.getOidDescription( lsd.getOid() ) != null )
                    {
                        lsd.setDescription( Utils.getOidDescription( lsd.getOid() ) );
                    }
                    lsd.addExtension( RAW_SCHEMA_DEFINITION_LDIF_VALUE, ldifValues );
                    addLdapSyntax( lsd );
                }
                else if ( attributeName.equalsIgnoreCase( SchemaConstants.MATCHING_RULES_AT ) )
                {
                    MatchingRule mrd = mrdParser.parse( value );
                    mrd.addExtension( RAW_SCHEMA_DEFINITION_LDIF_VALUE, ldifValues );
                    addMatchingRule( mrd );
                }
                else if ( attributeName.equalsIgnoreCase( SchemaConstants.MATCHING_RULE_USE_AT ) )
                {
                    MatchingRuleUse mrud = mrudParser.parse( value );
                    mrud.addExtension( RAW_SCHEMA_DEFINITION_LDIF_VALUE, ldifValues );
                    addMatchingRuleUse( mrud );
                }
                else if ( attributeName.equalsIgnoreCase( SchemaConstants.CREATE_TIMESTAMP_AT ) )
                {
                    setCreateTimestamp( value );
                }
                else if ( attributeName.equalsIgnoreCase( SchemaConstants.MODIFY_TIMESTAMP_AT ) )
                {
                    setModifyTimestamp( value );
                }
            }
            catch ( Exception e )
            {
                // TODO: exception handling
                System.out.println( "Error reading schema: " + attributeName + " = " + value ); //$NON-NLS-1$ //$NON-NLS-2$
                System.out.println( e.getMessage() );
            }
        }

        for ( AttributeType atd : getAttributeTypeDescriptions() )
        {
            // assume all received syntaxes in attributes are valid -> create pseudo syntaxes if missing
            String syntaxOid = atd.getSyntaxOid();
            if ( syntaxOid != null && !hasLdapSyntaxDescription( syntaxOid ) )
            {
                LdapSyntax lsd = new LdapSyntax( syntaxOid );
                lsd.setDescription( Utils.getOidDescription( syntaxOid ) );
                addLdapSyntax( lsd );
            }

            // assume all received matching rules in attributes are valid -> create pseudo matching rules if missing
            String emr = atd.getEqualityOid();
            String omr = atd.getOrderingOid();
            String smr = atd.getSubstringOid();
            checkMatchingRules( emr, omr, smr );
        }

        // set extensibleObject may attributes
        ObjectClass extensibleObjectOcd = this.getObjectClassDescription( SchemaConstants.EXTENSIBLE_OBJECT_OC );
        Collection<AttributeType> userAtds = SchemaUtils.getUserAttributeDescriptions( this );
        Collection<String> atdNames = SchemaUtils.getNames( userAtds );
        List<String> atdNames2 = new ArrayList<String>( atdNames );
        extensibleObjectOcd.setMayAttributeTypeOids( atdNames2 );
    }


    // ── Mace Windu Checks Each Matching Rule And Creates A Dummy If Missing ──────
    // Mace confronts each matching rule name in turn: "do you appear in the Archives?"
    // If not, he forges a minimal placeholder — a MatchingRule with just the OID —
    // and files it so no caller ever gets null back on a lookup.
    // This prevents NullPointerExceptions when servers use custom matching rules.
    private void checkMatchingRules( String... matchingRules )
    {
        for ( String matchingRule : matchingRules )
        {
            if ( matchingRule != null && !hasMatchingRuleDescription( matchingRule ) )
            {
                MatchingRule mrd = new MatchingRule( matchingRule );
                mrd.addName( matchingRule );
                addMatchingRule( mrd );
            }
        }
    }


    // ── Archivist Retrieves The Original Subschema LDIF Record ───────────────────
    // The master queries the archives for the raw holocron record used during load.
    // This is the unmodified LdifContentRecord from the subschema subentry.
    // It is null until loadFromLdif or loadFromRecord has been called.
    // Used by saveToLdif and by callers that need the raw attribute lines.
    /**
     * Gets the schema record.
     *
     * @return the schema record when the schema was created using
     *         {@link #loadFromLdif} or {@link #loadFromRecord}, {@code null} otherwise
     */
    public LdifContentRecord getSchemaRecord()
    {
        return schemaRecord;
    }


    // ── Archivist Files The Raw Subschema Record In The Master Vault ─────────────
    // Called by parseSchemaRecord to store the live record as soon as parsing starts.
    // Also used by external code that needs to swap the backing record.
    // The record is retained verbatim; no parsing or indexing happens here.
    // After this call getSchemaRecord returns the same reference.
    /**
     * Sets the schema record.
     *
     * @param schemaRecord the new schema record
     */
    public void setSchemaRecord( LdifContentRecord schemaRecord )
    {
        this.schemaRecord = schemaRecord;
    }


    // ── Archivist Returns The Location DN Of The Jedi Archives ───────────────────
    // The subschema subentry has a distinguished address in the directory tree.
    // This DN identifies where the schema lives — typically "cn=schema" on LDAP v3.
    // It is null until parseSchemaRecord has parsed the DN line from the record.
    // Used by callers that need to display or re-query the schema subentry.
    /**
     * Gets the DN of the schema record.
     *
     * @return the DN of the schema record, may be {@code null}
     */
    public Dn getDn()
    {
        return dn;
    }


    // ── Archivist Updates The Location Address Of The Jedi Archives ──────────────
    // Called by parseSchemaRecord immediately after parsing the subschema DN line.
    // The DN is stored verbatim for later retrieval; no validation is performed.
    // External code may also call this to override the DN post-construction.
    // After this call getDn returns the updated DN reference.
    /**
     * Sets the DN.
     *
     * @param dn the new DN
     */
    public void setDn( Dn dn )
    {
        this.dn = dn;
    }


    // ── Archivist Reads The Date The Archives Were First Commissioned ────────────
    // Every Jedi Archives entry carries a foundation date (createTimestamp).
    // This is the GeneralizedTime string from the schema subentry's createTimestamp.
    // It is null until parseSchemaRecord encounters the createTimestamp attribute.
    // Used by the UI to display the schema's age and detect stale caches.
    /**
     * Gets the create timestamp of the schema record.
     *
     * @return the create timestamp of the schema record, may be {@code null}
     */
    public String getCreateTimestamp()
    {
        return createTimestamp;
    }


    // ── Archivist Records The Foundation Date Of The Archives ────────────────────
    // Called by parseSchemaRecord when it encounters the createTimestamp attribute.
    // Stores the raw GeneralizedTime string exactly as received from the server.
    // External callers may also set this during offline or constructed schemas.
    // After this call getCreateTimestamp returns the updated string.
    /**
     * Sets the create timestamp.
     *
     * @param createTimestamp the new create timestamp
     */
    public void setCreateTimestamp( String createTimestamp )
    {
        this.createTimestamp = createTimestamp;
    }


    // ── Archivist Reads The Last Time The Archives Were Updated ──────────────────
    // Every Jedi Archives update is timestamped — modifyTimestamp tracks the latest.
    // This is the GeneralizedTime string from the subschema subentry's modifyTimestamp.
    // It is null until parseSchemaRecord encounters the modifyTimestamp attribute.
    // Used by the connection layer to decide whether a cached schema needs refresh.
    /**
     * Gets the modify timestamp of the schema record.
     *
     * @return the modify timestamp of the schema record, may be {@code null}
     */
    public String getModifyTimestamp()
    {
        return modifyTimestamp;
    }


    // ── Archivist Updates The Last-Modified Timestamp In The Archive Log ─────────
    // Called by parseSchemaRecord when it encounters the modifyTimestamp attribute.
    // Stores the raw GeneralizedTime string exactly as received from the server.
    // External callers may also set this during offline or constructed schemas.
    // After this call getModifyTimestamp returns the updated string.
    /**
     * Sets the modify timestamp.
     *
     * @param modifyTimestamp the new modify timestamp
     */
    public void setModifyTimestamp( String modifyTimestamp )
    {
        this.modifyTimestamp = modifyTimestamp;
    }


    ////////////////////// Object Class Description //////////////////////

    // ── Lando Files A New Object Class Into The Archives Under All Its Names ─────
    // Lando keeps Cloud City running by indexing every incoming cargo manifest.
    // Each object class is indexed under its numeric OID and every alias name,
    // all stored in lowercase for case-insensitive lookup later.
    // Both OID and names are optional; null values are silently skipped.
    /**
     * Adds an object class description to the internal lookup map.
     *
     * @param ocd the object class description to add
     */
    private void addObjectClass( ObjectClass ocd )
    {
        if ( ocd.getOid() != null )
        {
            ocdMapByNameOrNumericOid.put( Strings.toLowerCase( ocd.getOid() ), ocd );
        }
        if ( ocd.getNames() != null && !ocd.getNames().isEmpty() )
        {
            for ( String ocdName : ocd.getNames() )
            {
                ocdMapByNameOrNumericOid.put( Strings.toLowerCase( ocdName ), ocd );
            }
        }
    }


    // ── Lando Reads The Full Object Class Manifest From Cloud City ───────────────
    // The master asks: "what object classes does this schema define?"
    // Lando de-dupes the map values into a HashSet (the map has OID + name keys
    // that share the same ObjectClass instance) and returns the unique set.
    // Callers should not assume any particular iteration order.
    /**
     * Gets all unique object class descriptions.
     *
     * @return the object class descriptions (de-duplicated)
     */
    public Collection<ObjectClass> getObjectClassDescriptions()
    {
        Set<ObjectClass> set = new HashSet<ObjectClass>( ocdMapByNameOrNumericOid.values() );
        return set;
    }


    // ── Mace Windu Checks Whether An Object Class Is In The Archives ─────────────
    // Mace confronts the archive index: "does this name or OID appear here?"
    // The check is case-insensitive; a null argument returns false immediately.
    // Returns true only for elements actually loaded into this schema instance.
    // Falls through to DEFAULT_SCHEMA happens only in the getter, not here.
    /**
     * Checks if an object class description with the given name or OID exists.
     *
     * @param nameOrOid the name or numeric OID of the object class description
     * @return {@code true} if an object class description with the given name
     *         or OID exists in this schema
     */
    public boolean hasObjectClassDescription( String nameOrOid )
    {
        if ( nameOrOid != null )
        {
            return ocdMapByNameOrNumericOid.containsKey( Strings.toLowerCase( nameOrOid ) );
        }

        return false;
    }


    // ── Archivist Retrieves An Object Class Or Fabricates A Dummy ────────────────
    // The archivist first checks this schema's vault; if found, returns it.
    // If not found and this is not the default schema, defers to DEFAULT_SCHEMA.
    // If even the default Archives lack it, the archivist fabricates a dummy OCD
    // stamped with DUMMY_EXTENSIONS so callers can detect it is synthetic.
    // Never returns null — Han shoots before null can cause a NullPointerException.
    /**
     * Returns the object class description with the given name or OID.  Falls
     * back to {@link #DEFAULT_SCHEMA} and then to a dummy instance rather than
     * returning {@code null}.
     *
     * @param nameOrOid the name or numeric OID of the object class description
     * @return the object class description, the default, or a dummy
     */
    public ObjectClass getObjectClassDescription( String nameOrOid )
    {
        if ( ocdMapByNameOrNumericOid.containsKey( Strings.toLowerCase( nameOrOid ) ) )
        {
            return ocdMapByNameOrNumericOid.get( Strings.toLowerCase( nameOrOid ) );
        }
        else if ( !isDefault() )
        {
            return DEFAULT_SCHEMA.getObjectClassDescription( nameOrOid );
        }
        else
        {
            // DUMMY
            List<String> names = new ArrayList<String>();
            names.add( nameOrOid );
            ObjectClass ocd = new ObjectClass( nameOrOid );
            ocd.setNames( names );
            ocd.setExtensions( DUMMY_EXTENSIONS );
            return ocd;
        }
    }


    ////////////////////// Attribute Type Description //////////////////////

    // ── Lando Files A New Attribute Type Into The Archives Under All Its Names ────
    // Each attribute type has an OID and possibly many names (cn, commonName, …).
    // Lando indexes every alias in lowercase so all variants resolve to the same ATD.
    // Both OID and names lists are guarded for null and emptiness before filing.
    // This private method is called only from parseSchemaRecord during loading.
    /**
     * Adds an attribute type description to the internal lookup map.
     *
     * @param atd the attribute type description to add
     */
    private void addAttributeType( AttributeType atd )
    {
        if ( atd.getOid() != null )
        {
            atdMapByNameOrNumericOid.put( Strings.toLowerCase( atd.getOid() ), atd );
        }
        if ( atd.getNames() != null && !atd.getNames().isEmpty() )
        {
            for ( String atdName : atd.getNames() )
            {
                atdMapByNameOrNumericOid.put( Strings.toLowerCase( atdName ), atd );
            }
        }
    }


    // ── Lando Reads The Full Attribute Type Manifest From Cloud City ─────────────
    // Callers ask: "what attribute types does this schema define?"
    // Lando de-dupes the map values into a HashSet and returns the unique set.
    // The map holds OID + name keys pointing at the same ATD instance.
    // Callers should not assume any particular iteration order.
    /**
     * Gets all unique attribute type descriptions.
     *
     * @return the attribute type descriptions (de-duplicated)
     */
    public Collection<AttributeType> getAttributeTypeDescriptions()
    {
        Set<AttributeType> set = new HashSet<AttributeType>( atdMapByNameOrNumericOid.values() );
        return set;
    }


    // ── Mace Windu Checks Whether An Attribute Type Is In The Archives ───────────
    // Mace scans the attribute type vault: "is this name or OID present here?"
    // The check is case-insensitive; null returns false without touching the map.
    // Returns true only for elements in this schema instance, not the default.
    // Used by parseSchemaRecord to decide whether to fabricate a dummy syntax.
    /**
     * Checks if an attribute type description with the given name or OID exists.
     *
     * @param nameOrOid the name or numeric OID of the attribute type description
     * @return {@code true} if an attribute type description with the given name
     *         or OID exists in this schema
     */
    public boolean hasAttributeTypeDescription( String nameOrOid )
    {
        if ( nameOrOid != null )
        {
            return atdMapByNameOrNumericOid.containsKey( Strings.toLowerCase( nameOrOid ) );
        }

        return false;
    }


    // ── Archivist Retrieves An Attribute Type Or Fabricates A Dummy ──────────────
    // The archivist strips options from the description (cn;lang-en → cn),
    // then checks this vault; falls back to DEFAULT_SCHEMA; finally fabricates
    // a user-modifiable dummy ATD stamped with DUMMY_EXTENSIONS if all else fails.
    // Never returns null — the dummy is the last-resort so callers stay safe.
    /**
     * Returns the attribute type description with the given name or OID.  Falls
     * back to {@link #DEFAULT_SCHEMA} and then to a dummy instance.
     *
     * @param nameOrOid the name or numeric OID of the attribute type description
     * @return the attribute type description, the default, or a dummy
     */
    public AttributeType getAttributeTypeDescription( String nameOrOid )
    {
        AttributeDescription ad = new AttributeDescription( nameOrOid );
        String attributeType = ad.getParsedAttributeType();

        if ( atdMapByNameOrNumericOid.containsKey( Strings.toLowerCase( attributeType ) ) )
        {
            return atdMapByNameOrNumericOid.get( Strings.toLowerCase( attributeType ) );
        }
        else if ( !isDefault() )
        {
            return DEFAULT_SCHEMA.getAttributeTypeDescription( attributeType );
        }
        else
        {
            // DUMMY
            List<String> attributeTypes = new ArrayList<String>();
            attributeTypes.add( attributeType );
            AttributeType atd = new AttributeType( attributeType );
            atd.setNames( attributeTypes );
            atd.setUserModifiable( true );
            atd.setUsage( UsageEnum.USER_APPLICATIONS );
            atd.setExtensions( DUMMY_EXTENSIONS );
            return atd;
        }
    }


    //////////////////////// LDAP Syntax Description ////////////////////////

    // ── Lando Files A New LDAP Syntax Into The Archives By OID ───────────────────
    // Unlike object classes and attributes, syntaxes have only an OID, no name.
    // Lando stores the lowercase OID as the sole key in the syntax vault.
    // Null OIDs are silently skipped to avoid NPE in the map.
    // This private method is called both from parseSchemaRecord and from the
    // post-parse loop that creates pseudo-syntaxes for unknown ATD syntaxOids.
    /**
     * Adds an LDAP syntax description to the internal lookup map.
     *
     * @param lsd the LDAP syntax description to add
     */
    private void addLdapSyntax( LdapSyntax lsd )
    {
        if ( lsd.getOid() != null )
        {
            lsdMapByNumericOid.put( Strings.toLowerCase( lsd.getOid() ), lsd );
        }
    }


    // ── Lando Reads The Full LDAP Syntax Manifest From Cloud City ────────────────
    // Returns a de-duplicated set of all LdapSyntax instances in the vault.
    // Because the syntax map is OID-keyed (one entry per syntax), the set equals
    // the map's value collection without further de-duplication.
    // Callers should not assume any particular iteration order.
    /**
     * Gets all unique LDAP syntax descriptions.
     *
     * @return the LDAP syntax descriptions
     */
    public Collection<LdapSyntax> getLdapSyntaxDescriptions()
    {
        Set<LdapSyntax> set = new HashSet<LdapSyntax>( lsdMapByNumericOid.values() );
        return set;
    }


    // ── Mace Windu Checks Whether An LDAP Syntax OID Is In The Archives ─────────
    // Mace scans the syntax vault: "is this OID registered here?"
    // The check is case-insensitive; null returns false without touching the map.
    // Called by parseSchemaRecord to decide whether a pseudo-syntax is needed.
    // Returns true only for syntaxes in this schema instance, not the default.
    /**
     * Checks if an LDAP syntax description with the given OID exists.
     *
     * @param numericOid the numeric OID of the LDAP syntax description
     * @return {@code true} if an LDAP syntax description with the given OID
     *         exists in this schema
     */
    public boolean hasLdapSyntaxDescription( String numericOid )
    {
        if ( numericOid != null )
        {
            return lsdMapByNumericOid.containsKey( Strings.toLowerCase( numericOid ) );
        }

        return false;
    }


    // ── Archivist Retrieves An LDAP Syntax Or Fabricates A Dummy ─────────────────
    // A null OID returns the DUMMY_LDAP_SYNTAX sentinel immediately.
    // Otherwise, the vault is checked; if not found, DEFAULT_SCHEMA is consulted.
    // If even the default is missing it, a dummy LdapSyntax is fabricated.
    // The dummy is stamped with DUMMY_EXTENSIONS so callers can detect it.
    /**
     * Returns the LDAP syntax description with the given OID.  Falls back to
     * {@link #DEFAULT_SCHEMA} and then to a dummy instance.
     *
     * @param numericOid the numeric OID of the LDAP syntax description
     * @return the LDAP syntax description, the default, or a dummy
     */
    public LdapSyntax getLdapSyntaxDescription( String numericOid )
    {
        if ( numericOid == null )
        {
            return DUMMY_LDAP_SYNTAX;
        }
        else if ( lsdMapByNumericOid.containsKey( Strings.toLowerCase( numericOid ) ) )
        {
            return lsdMapByNumericOid.get( Strings.toLowerCase( numericOid ) );
        }
        else if ( !isDefault() )
        {
            return DEFAULT_SCHEMA.getLdapSyntaxDescription( numericOid );
        }
        else
        {
            // DUMMY
            LdapSyntax lsd = new LdapSyntax( numericOid );
            lsd.setExtensions( DUMMY_EXTENSIONS );
            return lsd;
        }
    }


    ////////////////////////// Matching Rule Description //////////////////////////

    // ── Lando Files A New Matching Rule Into The Archives Under All Its Names ─────
    // Matching rules govern how attribute values are compared — case-exact, case-
    // ignore, substring, etc.  Lando indexes each by OID and every alias name.
    // Both OID and names are guarded for null before filing.
    // Called from parseSchemaRecord and from checkMatchingRules for pseudo-rules.
    /**
     * Adds a matching rule description to the internal lookup map.
     *
     * @param mrd the matching rule description to add
     */
    private void addMatchingRule( MatchingRule mrd )
    {
        if ( mrd.getOid() != null )
        {
            mrdMapByNameOrNumericOid.put( Strings.toLowerCase( mrd.getOid() ), mrd );
        }
        if ( mrd.getNames() != null && !mrd.getNames().isEmpty() )
        {
            for ( String mrdName : mrd.getNames() )
            {
                mrdMapByNameOrNumericOid.put( Strings.toLowerCase( mrdName ), mrd );
            }
        }
    }


    // ── Lando Reads The Full Matching Rule Manifest From Cloud City ──────────────
    // Returns the unique set of all MatchingRule instances in the vault.
    // The map has OID + name keys pointing at the same MRD instance, so a
    // HashSet is used to de-duplicate before returning.
    // Callers should not assume any particular iteration order.
    /**
     * Gets all unique matching rule descriptions.
     *
     * @return the matching rule descriptions (de-duplicated)
     */
    public Collection<MatchingRule> getMatchingRuleDescriptions()
    {
        Set<MatchingRule> set = new HashSet<MatchingRule>( mrdMapByNameOrNumericOid.values() );
        return set;
    }


    // ── Mace Windu Checks Whether A Matching Rule Is In The Archives ─────────────
    // Mace scans the matching rule vault: "is this name or OID registered?"
    // The check is case-insensitive; null returns false without touching the map.
    // Called by checkMatchingRules to decide whether a pseudo-rule is needed.
    // Returns true only for rules in this schema instance, not the default.
    /**
     * Checks if a matching rule description with the given name or OID exists.
     *
     * @param nameOrOid the name or numeric OID of the matching rule description
     * @return {@code true} if a matching rule description with the given name
     *         or OID exists in this schema
     */
    public boolean hasMatchingRuleDescription( String nameOrOid )
    {
        if ( nameOrOid != null )
        {
            return mrdMapByNameOrNumericOid.containsKey( Strings.toLowerCase( nameOrOid ) );
        }

        return false;
    }


    // ── Archivist Retrieves A Matching Rule Or Fabricates A Dummy ────────────────
    // Checks this vault first, then defers to DEFAULT_SCHEMA if not found.
    // If even the default is missing the rule, a dummy MatchingRule is fabricated
    // and stamped with DUMMY_EXTENSIONS so callers can detect the synthetic result.
    // Never returns null — Han shoots before null can cause a NullPointerException.
    /**
     * Returns the matching rule description with the given name or OID.  Falls
     * back to {@link #DEFAULT_SCHEMA} and then to a dummy instance.
     *
     * @param nameOrOid the name or numeric OID of the matching rule description
     * @return the matching rule description, the default, or a dummy
     */
    public MatchingRule getMatchingRuleDescription( String nameOrOid )
    {
        if ( mrdMapByNameOrNumericOid.containsKey( Strings.toLowerCase( nameOrOid ) ) )
        {
            return mrdMapByNameOrNumericOid.get( Strings.toLowerCase( nameOrOid ) );
        }
        else if ( !isDefault() )
        {
            return DEFAULT_SCHEMA.getMatchingRuleDescription( nameOrOid );
        }
        else
        {
            // DUMMY
            MatchingRule mrd = new MatchingRule( nameOrOid );
            mrd.setExtensions( DUMMY_EXTENSIONS );
            return mrd;
        }
    }


    //////////////////////// Matching Rule Use Description ////////////////////////

    // ── Lando Files A New Matching Rule Use Into The Archives Under All Its Names ─
    // MatchingRuleUse defines which attributes a matching rule applies to.
    // Lando indexes it by OID and every alias name in lowercase for fast lookup.
    // Both OID and names are guarded for null before filing into the vault.
    // This private method is called only from parseSchemaRecord during loading.
    /**
     * Adds a matching rule use description to the internal lookup map.
     *
     * @param mrud the matching rule use description to add
     */
    private void addMatchingRuleUse( MatchingRuleUse mrud )
    {
        if ( mrud.getOid() != null )
        {
            mrudMapByNameOrNumericOid.put( Strings.toLowerCase( mrud.getOid() ), mrud );
        }
        if ( mrud.getNames() != null && !mrud.getNames().isEmpty() )
        {
            for ( String mrudName : mrud.getNames() )
            {
                mrudMapByNameOrNumericOid.put( Strings.toLowerCase( mrudName ), mrud );
            }
        }
    }


    // ── Lando Reads The Full Matching Rule Use Manifest From Cloud City ──────────
    // Returns the unique set of all MatchingRuleUse instances in the vault.
    // The map has OID + name keys pointing at the same MRUD instance,
    // so a HashSet de-duplicates them before the collection is returned.
    // Callers should not assume any particular iteration order.
    /**
     * Gets all unique matching rule use descriptions.
     *
     * @return the matching rule use descriptions (de-duplicated)
     */
    public Collection<MatchingRuleUse> getMatchingRuleUseDescriptions()
    {
        Set<MatchingRuleUse> set = new HashSet<MatchingRuleUse>( mrudMapByNameOrNumericOid
            .values() );
        return set;
    }


    // ── Mace Windu Checks Whether A Matching Rule Use Is In The Archives ─────────
    // Mace scans the matching rule use vault: "is this name or OID present?"
    // The check is case-insensitive; null returns false immediately.
    // Returns true only for rule uses in this schema instance.
    // Fallback to DEFAULT_SCHEMA happens only in the getter, not here.
    /**
     * Checks if a matching rule use description with the given name or OID exists.
     *
     * @param nameOrOid the name or numeric OID of the matching rule use description
     * @return {@code true} if a matching rule use description with the given name
     *         or OID exists in this schema
     */
    public boolean hasMatchingRuleUseDescription( String nameOrOid )
    {
        if ( nameOrOid != null )
        {
            return mrudMapByNameOrNumericOid.containsKey( Strings.toLowerCase( nameOrOid ) );
        }

        return false;
    }


    // ── Archivist Retrieves A Matching Rule Use Or Fabricates A Dummy ────────────
    // Checks this vault first, then defers to DEFAULT_SCHEMA if not found.
    // If even the default lacks the rule use, a dummy MatchingRuleUse is fabricated
    // and stamped with DUMMY_EXTENSIONS so callers can detect the synthetic result.
    // Never returns null — Han shoots before null triggers a NullPointerException.
    /**
     * Returns the matching rule use description with the given name or OID.
     * Falls back to {@link #DEFAULT_SCHEMA} and then to a dummy instance.
     *
     * @param nameOrOid the name or numeric OID of the matching rule use description
     * @return the matching rule use description, the default, or a dummy
     */
    public MatchingRuleUse getMatchingRuleUseDescription( String nameOrOid )
    {
        if ( mrudMapByNameOrNumericOid.containsKey( Strings.toLowerCase( nameOrOid ) ) )
        {
            return mrudMapByNameOrNumericOid.get( Strings.toLowerCase( nameOrOid ) );
        }
        else if ( !isDefault() )
        {
            return DEFAULT_SCHEMA.getMatchingRuleUseDescription( nameOrOid );
        }
        else
        {
            // DUMMY
            MatchingRuleUse mrud = new MatchingRuleUse( nameOrOid );
            mrud.setExtensions( DUMMY_EXTENSIONS );
            return mrud;
        }
    }

}
