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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.schema.AbstractSchemaObject;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.MatchingRuleUse;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.ObjectClassTypeEnum;
import org.apache.directory.api.ldap.model.schema.UsageEnum;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.eclipse.osgi.util.NLS;


// ── CLASS: SchemaUtils — R2-D2 PLUGGING INTO THE JEDI ARCHIVES COMPUTER ──────
// R2-D2 is the indispensable sidekick: when you need to cross-reference the Jedi
// Archives — find all must-attributes, check whether a syntax is binary, walk the
// object class hierarchy — you call R2-D2.  SchemaUtils is that sidekick: a bag
// of static helpers that answer questions about schema elements without modifying
// them, always using the live Schema and BrowserCorePlugin preferences.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Static utility methods for querying and navigating LDAP schema elements.
 * Covers name/OID extraction, binary/operational classification, transitive
 * hierarchy walking, and entry-completeness validation.
 *
 * <p>Think of this as R2-D2 plugging into the Jedi Archives computer — every
 * tricky schema question gets routed through here.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaUtils
{

    /** The well-known operational attributes */
    public static final Set<String> OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES = new HashSet<String>();
    static
    {
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.CREATE_TIMESTAMP_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.CREATE_TIMESTAMP_AT_OID ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.CREATORS_NAME_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.CREATORS_NAME_AT_OID ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.MODIFY_TIMESTAMP_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.MODIFY_TIMESTAMP_AT_OID ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.MODIFIERS_NAME_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.MODIFIERS_NAME_AT_OID ) );

        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.SUBSCHEMA_SUBENTRY_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.SUBSCHEMA_SUBENTRY_AT_OID ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.STRUCTURAL_OBJECT_CLASS_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES
            .add( Strings.toLowerCase( SchemaConstants.STRUCTURAL_OBJECT_CLASS_AT_OID ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.GOVERNING_STRUCTURE_RULE_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings
            .toLowerCase( SchemaConstants.GOVERNING_STRUCTURE_RULE_AT_OID ) );

        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.ENTRY_UUID_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.ENTRY_UUID_AT_OID ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.ENTRY_CSN_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.ENTRY_CSN_AT_OID ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.ENTRY_DN_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.ENTRY_DN_AT_OID ) );

        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.OBJECT_CLASSES_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.OBJECT_CLASSES_AT_OID ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.ATTRIBUTE_TYPES_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.ATTRIBUTE_TYPES_AT_OID ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.LDAP_SYNTAXES_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.LDAP_SYNTAXES_AT_OID ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.MATCHING_RULES_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.MATCHING_RULES_AT_OID ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.MATCHING_RULE_USE_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.MATCHING_RULE_USE_AT_OID ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.DIT_CONTENT_RULES_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.DIT_CONTENT_RULES_AT_OID ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.DIT_STRUCTURE_RULES_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.DIT_STRUCTURE_RULES_AT_OID ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.NAME_FORMS_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.NAME_FORMS_AT_OID ) );

        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.HAS_SUBORDINATES_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.HAS_SUBORDINATES_AT_OID ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.NUM_SUBORDINATES_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.SUBORDINATE_COUNT_AT ) );

        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.VENDOR_NAME_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.VENDOR_NAME_AT_OID ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.VENDOR_VERSION_AT ) );
        OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.VENDOR_VERSION_AT_OID ) );
    }

    /** The well-known non-modifiable attributes */
    public static final Set<String> NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES = new HashSet<String>();
    static
    {
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.CREATE_TIMESTAMP_AT ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.CREATE_TIMESTAMP_AT_OID ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.CREATORS_NAME_AT ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.CREATORS_NAME_AT_OID ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.MODIFY_TIMESTAMP_AT ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.MODIFY_TIMESTAMP_AT_OID ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.MODIFIERS_NAME_AT ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.MODIFIERS_NAME_AT_OID ) );

        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.SUBSCHEMA_SUBENTRY_AT ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.SUBSCHEMA_SUBENTRY_AT_OID ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.STRUCTURAL_OBJECT_CLASS_AT ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES
            .add( Strings.toLowerCase( SchemaConstants.STRUCTURAL_OBJECT_CLASS_AT_OID ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.GOVERNING_STRUCTURE_RULE_AT ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings
            .toLowerCase( SchemaConstants.GOVERNING_STRUCTURE_RULE_AT_OID ) );

        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.ENTRY_UUID_AT ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.ENTRY_UUID_AT_OID ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.ENTRY_CSN_AT ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.ENTRY_CSN_AT_OID ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.ENTRY_DN_AT ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.ENTRY_DN_AT_OID ) );

        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.HAS_SUBORDINATES_AT ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.HAS_SUBORDINATES_AT_OID ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.NUM_SUBORDINATES_AT ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.SUBORDINATE_COUNT_AT ) );

        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.VENDOR_NAME_AT ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.VENDOR_NAME_AT_OID ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.VENDOR_VERSION_AT ) );
        NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES.add( Strings.toLowerCase( SchemaConstants.VENDOR_VERSION_AT_OID ) );
    }

    private static final Comparator<String> nameAndOidComparator = new Comparator<String>()
    {
        public int compare( String s1, String s2 )
        {
            return s1.compareToIgnoreCase( s2 );
        }
    };

    private static final Comparator<AbstractSchemaObject> schemaElementNameComparator = new Comparator<AbstractSchemaObject>()
    {
        public int compare( AbstractSchemaObject s1, AbstractSchemaObject s2 )
        {
            return SchemaUtils.toString( s1 ).compareToIgnoreCase( SchemaUtils.toString( s2 ) );
        }
    };


    // ── R2-D2 Collects Every Alias Name From A Set Of Schema Elements ────────────
    // R2-D2 iterates the schema object collection and gathers every name alias.
    // Names are case-insensitively sorted via nameAndOidComparator in a TreeSet.
    // Used by Schema.parseSchemaRecord to build the extensibleObject may-list.
    // Returns an empty sorted set when the input collection is empty.
    /**
     * Gets the names of the given schema elements.
     *
     * @param asds the schema elements
     * @return the sorted set of all alias names across all elements
     */
    public static Collection<String> getNames( Collection<? extends AbstractSchemaObject> asds )
    {
        Set<String> nameSet = new TreeSet<String>( nameAndOidComparator );
        for ( AbstractSchemaObject asd : asds )
        {
            nameSet.addAll( asd.getNames() );
        }
        return nameSet;
    }


    // ── R2-D2 Returns The Same Name Collection As A String Array ─────────────────
    // A convenience wrapper around getNames for callers that need a String[].
    // R2-D2 delegates to getNames and converts the sorted set to an array.
    // The array is sorted in the same case-insensitive order as getNames.
    // Returns an empty array when the input collection is empty.
    /**
     * Gets the names of the given schema elements as a String array.
     *
     * @param asds the schema elements
     * @return the sorted array of all alias names across all elements
     */
    public static String[] getNamesAsArray( Collection<? extends AbstractSchemaObject> asds )
    {
        return getNames( asds ).toArray( new String[0] );
    }


    // ── R2-D2 Extracts Only The Numeric OIDs From A Schema Collection ────────────
    // Some callers need the raw OIDs rather than the friendly alias names.
    // R2-D2 iterates the collection and collects each element's getOid() value.
    // Duplicates are not filtered; callers should use a Set or de-duplicate.
    // Returns an empty set when the input collection is empty.
    /**
     * Gets the numeric OIDs of the given schema descriptions.
     *
     * @param descriptions the schema descriptions
     * @return the set of numeric OIDs
     */
    public static Collection<String> getNumericOids( Collection<? extends AbstractSchemaObject> descriptions )
    {
        Set<String> oids = new HashSet<String>();
        for ( AbstractSchemaObject asd : descriptions )
        {
            oids.add( asd.getOid() );

        }
        return oids;
    }


    // ── R2-D2 Builds A Lowercase Set Of All OID And Name Identifiers ─────────────
    // To do case-insensitive containment checks, R2-D2 gathers every way the
    // schema element could be referred to: numeric OID + all alias names.
    // Each identifier is lowercased before adding; null names are skipped.
    // This set is used extensively by binary/operational classification checks.
    /**
     * Gets all lowercase identifiers (OID and names) of the given schema element.
     *
     * @param asd the schema description
     * @return the set of lowercase identifiers
     */
    public static Collection<String> getLowerCaseIdentifiers( AbstractSchemaObject asd )
    {
        Set<String> identiers = new HashSet<String>();
        if ( asd.getOid() != null )
        {
            identiers.add( Strings.toLowerCase( asd.getOid() ) );
        }
        if ( asd.getNames() != null && !asd.getNames().isEmpty() )
        {
            for ( String name : asd.getNames() )
            {
                if ( name != null )
                {
                    identiers.add( Strings.toLowerCase( name ) );
                }
            }
        }
        return identiers;
    }


    // ── R2-D2 Picks The Most Human-Readable Name For A Schema Element ────────────
    // When the UI needs to display a schema element, R2-D2 finds the best label.
    // He prefers the first alias name (e.g. "cn") over the raw OID.
    // Falls back to getOid() only when no alias names have been defined.
    // Used by schemaElementNameComparator and by UI display code.
    /**
     * Gets the friendly identifier of the given schema description.
     * Returns the first name if available, otherwise the numeric OID.
     *
     * @param asd the schema description
     * @return the friendly identifier
     */
    public static String getFriendlyIdentifier( AbstractSchemaObject asd )
    {
        if ( asd.getNames() != null && !asd.getNames().isEmpty() )
        {
            return asd.getNames().get( 0 );
        }
        return asd.getOid();
    }


    // ── R2-D2 Filters The Schema For All Operational Attribute Types ──────────────
    // Operational attributes are the Death Star's automated systems — invisible to
    // ordinary users but vital to the installation: createTimestamp, entryDN, etc.
    // R2-D2 iterates the schema and returns only those marked as operational by
    // the isOperational predicate (usage, well-known set, or dummy flag).
    /**
     * Gets all operational attribute type descriptions.
     *
     * @param schema the schema
     * @return all operational attribute types
     */
    public static Collection<AttributeType> getOperationalAttributeDescriptions( Schema schema )
    {
        Set<AttributeType> operationalAtds = new HashSet<AttributeType>();
        for ( AttributeType atd : schema.getAttributeTypeDescriptions() )
        {
            if ( isOperational( atd ) )
            {
                operationalAtds.add( atd );
            }
        }
        return operationalAtds;
    }


    // ── R2-D2 Filters The Schema For All User-Visible Attribute Types ────────────
    // User attributes are the blueprint panels that ordinary crew can read:
    // cn, mail, telephoneNumber — the everyday data fields.
    // R2-D2 returns only those for which isOperational returns false.
    // Used by parseSchemaRecord to populate extensibleObject's may-list.
    /**
     * Gets all user (non-operational) attribute type descriptions.
     *
     * @param schema the schema
     * @return all user attribute type descriptions
     */
    public static Collection<AttributeType> getUserAttributeDescriptions( Schema schema )
    {
        Set<AttributeType> userAtds = new HashSet<AttributeType>();
        for ( AttributeType atd : schema.getAttributeTypeDescriptions() )
        {
            if ( !isOperational( atd ) )
            {
                userAtds.add( atd );
            }
        }
        return userAtds;
    }


    // ── Mace Windu Tests Whether An Attribute Is A Restricted System Channel ──────
    // Mace confronts each attribute: "are you a system channel or a user field?"
    // An attribute is operational if its usage is not USER_APPLICATIONS, if it
    // appears in the well-known operational set (covering AD/Samba4 gaps), or if
    // it is a dummy placeholder lacking a real schema declaration.
    /**
     * An attribute type is marked as operational if either
     * <ul>
     * <li>the usage differs from userApplications; or</li>
     * <li>it is a well-known operational attribute (covering AD/Samba4 gaps); or</li>
     * <li>it is undeclared in the schema and carries the dummy extension.</li>
     * </ul>
     *
     * @param atd the attribute type description
     * @return {@code true} if the attribute type is operational
     */
    public static boolean isOperational( AttributeType atd )
    {
        return !UsageEnum.USER_APPLICATIONS.equals( atd.getUsage() )
            || Schema.DUMMY_EXTENSIONS.equals( atd.getExtensions() )
            || CollectionUtils.containsAny( OPERATIONAL_ATTRIBUTES_OIDS_AND_NAMES, getLowerCaseIdentifiers( atd ) );
    }


    // ── Mace Windu Checks Whether An Attribute Can Be Modified By A User ─────────
    // Mace confronts the question: "can a user actually write to this field?"
    // Null ATD, non-user-modifiable flag, or membership in NON_MODIFIABLE set
    // all trigger an immediate false — Han would shoot before writing those.
    // Returns true only when all three barriers are cleared.
    /**
     * Checks whether the given attribute type can be modified by a user.
     * Returns {@code false} if the ATD is {@code null}, not user-modifiable,
     * or in the well-known non-modifiable set.
     *
     * @param atd the attribute type description
     * @return {@code true} if the attribute type is user-modifiable
     */
    public static boolean isModifiable( AttributeType atd )
    {
        if ( atd == null )
        {
            return false;
        }

        if ( !atd.isUserModifiable() )
        {
            return false;
        }

        // Check some default no-user-modification attributes
        // e.g. Siemens DirX doesn't provide a good schema.
        // TODO: make default no-user-modification attributes configurable
        if ( CollectionUtils.containsAny( NON_MODIFIABLE_ATTRIBUTE_OIDS_AND_NAMES, getLowerCaseIdentifiers( atd ) ) )
        {
            return false;
        }

        return true;
    }


    // ── R2-D2 Collects Every Mandatory Attribute For An Entry's Object Classes ────
    // R2-D2 walks every object class of the entry and collects MUST attributes
    // transitively through the class hierarchy (via getMustATDNamesTransitive).
    // Each name is then resolved to its AttributeType via the schema lookup.
    // Returns an empty set when the entry has no object class descriptions.
    /**
     * Gets the must attribute type descriptions of all object class descriptions
     * of the given entry (transitively).
     *
     * @param entry the entry
     * @return the must attribute type descriptions
     */
    public static Collection<AttributeType> getMustAttributeTypeDescriptions( IEntry entry )
    {
        Schema schema = entry.getBrowserConnection().getSchema();
        Collection<AttributeType> atds = new HashSet<AttributeType>();
        Collection<ObjectClass> ocds = entry.getObjectClassDescriptions();
        if ( ocds != null )
        {
            for ( ObjectClass ocd : entry.getObjectClassDescriptions() )
            {
                Collection<String> musts = getMustAttributeTypeDescriptionNamesTransitive( ocd, schema );
                for ( String must : musts )
                {
                    AttributeType atd = schema.getAttributeTypeDescription( must );
                    atds.add( atd );
                }
            }
        }
        return atds;
    }


    // ── R2-D2 Collects Every Optional Attribute For An Entry's Object Classes ─────
    // Like getMustAttributeTypeDescriptions but for MAY (optional) attributes.
    // R2-D2 walks every object class transitively and gathers the may-list names.
    // Each name is resolved to its AttributeType via the schema lookup.
    // Returns an empty set when the entry has no object class descriptions.
    /**
     * Gets the may attribute type descriptions of all object class descriptions
     * of the given entry (transitively).
     *
     * @param entry the entry
     * @return the may attribute type descriptions
     */
    public static Collection<AttributeType> getMayAttributeTypeDescriptions( IEntry entry )
    {
        Schema schema = entry.getBrowserConnection().getSchema();
        Collection<AttributeType> atds = new HashSet<AttributeType>();
        Collection<ObjectClass> ocds = entry.getObjectClassDescriptions();
        if ( ocds != null )
        {
            for ( ObjectClass ocd : entry.getObjectClassDescriptions() )
            {
                Collection<String> mays = getMayAttributeTypeDescriptionNamesTransitive( ocd, schema );
                for ( String may : mays )
                {
                    AttributeType atd = schema.getAttributeTypeDescription( may );
                    atds.add( atd );
                }
            }
        }
        return atds;
    }


    // ── R2-D2 Returns The Combined Must-And-May Attribute Set For An Entry ────────
    // For entry-completeness validation, all allowed attributes = MUST union MAY.
    // R2-D2 delegates to both getMust and getMay helpers and merges the results.
    // The union is computed in a HashSet to avoid duplicates.
    // Used by getEntryIncompleteMessages to detect unallowed attributes.
    /**
     * Gets all attribute type descriptions (must and may) of all object class
     * descriptions of the given entry.
     *
     * @param entry the entry
     * @return all attribute type descriptions
     */
    public static Collection<AttributeType> getAllAttributeTypeDescriptions( IEntry entry )
    {
        Collection<AttributeType> atds = new HashSet<AttributeType>();
        atds.addAll( getMustAttributeTypeDescriptions( entry ) );
        atds.addAll( getMayAttributeTypeDescriptions( entry ) );
        return atds;
    }


    ////////////////////////////////////////////////////////
    // ── Mace Windu Checks Whether A Syntax Carries Readable Text ─────────────────
    // Mace confronts the syntax: "are your values human-readable text?"
    // This is simply the logical negation of isBinary(LdapSyntax).
    // A syntax is a string if it is NOT in the binary syntax OID preferences set.
    // Used by the value display layer to choose a text vs. hex renderer.
    /**
     * Checks whether the given LDAP syntax carries string (not binary) values.
     *
     * @param lsd the LDAP syntax description
     * @return {@code false} if the syntax is defined as binary
     */
    public static boolean isString( LdapSyntax lsd )
    {
        return !isBinary( lsd );
    }


    // ── Mace Windu Checks Whether A Syntax Carries Opaque Binary Bytes ───────────
    // Mace confronts the syntax: "are your values raw binary blobs?"
    // He queries the BrowserCorePlugin preference store for the user-defined set
    // of binary syntax OIDs (uppercased for case-insensitive lookup).
    // Returns true if the syntax OID appears in that preference set.
    /**
     * Checks whether the given LDAP syntax carries binary (not string) values.
     *
     * @param lsd the LDAP syntax description
     * @return {@code true} if the syntax is defined as binary
     */
    public static boolean isBinary( LdapSyntax lsd )
    {
        // check user-defined binary syntaxes
        Set<String> binarySyntaxOids = BrowserCorePlugin.getDefault().getCorePreferences()
            .getUpperCasedBinarySyntaxOids();
        return binarySyntaxOids.contains( lsd.getOid().toUpperCase() );
    }


    // ── Mace Windu Checks Whether An Attribute Carries Readable Text ─────────────
    // Mace confronts the attribute: "are your values human-readable text?"
    // This is simply the logical negation of isBinary(AttributeType, Schema).
    // Used by the value display layer to choose a text vs. hex renderer.
    // Returns true for attribute types not flagged as binary in any way.
    /**
     * Checks whether the given attribute type carries string (not binary) values.
     *
     * @param atd the attribute type description
     * @param schema the schema
     * @return {@code false} if the attribute type is defined as binary
     */
    public static boolean isString( AttributeType atd, Schema schema )
    {
        return !isBinary( atd, schema );
    }


    // ── Mace Windu Checks Whether An Attribute Carries Opaque Binary Bytes ───────
    // Mace runs three checks in order: OID in the binary attribute preference set,
    // any alias name in that set, and then the attribute's transitive syntax OID
    // compared against the binary syntax preference set via isBinary(LdapSyntax).
    // Returns true on the first positive match; returns false if all three pass.
    /**
     * Checks whether the given attribute type carries binary (not string) values.
     * Three checks are performed: binary attribute OID, binary attribute name,
     * and binary syntax (transitively resolved).
     *
     * @param atd the attribute type description
     * @param schema the schema
     * @return {@code true} if the attribute type is defined as binary
     */
    public static boolean isBinary( AttributeType atd, Schema schema )
    {
        // check user-defined binary attribute types
        Set<String> binaryAttributeOidsAndNames = BrowserCorePlugin.getDefault().getCorePreferences()
            .getUpperCasedBinaryAttributeOidsAndNames();
        if ( binaryAttributeOidsAndNames.contains( atd.getOid().toUpperCase() ) )
        {
            return true;
        }
        for ( String name : atd.getNames() )
        {
            if ( binaryAttributeOidsAndNames.contains( name.toUpperCase() ) )
            {
                return true;
            }
        }

        // check user-defined binary syntaxes
        String syntax = getSyntaxNumericOidTransitive( atd, schema );
        if ( syntax != null && schema.hasLdapSyntaxDescription( syntax ) )
        {
            LdapSyntax lsd = schema.getLdapSyntaxDescription( syntax );
            return isBinary( lsd );
        }

        return false;
    }


    // ── R2-D2 Finds All Attribute Types That Use A Given LDAP Syntax ─────────────
    // The schema browser asks: "which attributes carry values of this syntax?"
    // R2-D2 walks all attribute types and checks their transitive syntax OID.
    // Results are sorted by name using schemaElementNameComparator.
    // Used by the schema browser to populate the "Used By" panel for a syntax.
    /**
     * Gets all attribute type descriptions that use the given syntax description
     * (transitively, following superior attribute types).
     *
     * @param lsd the LDAP syntax description
     * @param schema the schema
     * @return the sorted set of attribute type descriptions using this syntax
     */
    public static Collection<AttributeType> getUsedFromAttributeTypeDescriptions( LdapSyntax lsd,
        Schema schema )
    {
        Set<AttributeType> usedFroms = new TreeSet<AttributeType>( schemaElementNameComparator );
        for ( AttributeType atd : schema.getAttributeTypeDescriptions() )
        {
            String syntax = getSyntaxNumericOidTransitive( atd, schema );
            if ( syntax != null && lsd.getOid() != null
                && Strings.toLowerCase( syntax ).equals( Strings.toLowerCase( lsd.getOid() ) ) )
            {
                usedFroms.add( atd );
            }
        }
        return usedFroms;
    }


    // ── R2-D2 Finds All Attribute Types That Use A Given Matching Rule ───────────
    // The schema browser asks: "which attributes use this matching rule?"
    // R2-D2 checks equality, substring, and ordering rules for each ATD.
    // Any match against the rule's lowercase identifiers adds the ATD to the set.
    // Results are sorted by name using schemaElementNameComparator.
    /**
     * Gets all attribute type descriptions that use the given matching rule for
     * equality, substring, or ordering matching (transitively resolved).
     *
     * @param mrd the matching rule description
     * @param schema the schema
     * @return the sorted set of attribute type descriptions using this matching rule
     */
    public static Collection<AttributeType> getUsedFromAttributeTypeDescriptions(
        MatchingRule mrd, Schema schema )
    {
        Set<AttributeType> usedFromSet = new TreeSet<AttributeType>( schemaElementNameComparator );
        for ( AttributeType atd : schema.getAttributeTypeDescriptions() )
        {
            Collection<String> lowerCaseIdentifiers = getLowerCaseIdentifiers( mrd );
            String emr = getEqualityMatchingRuleNameOrNumericOidTransitive( atd, schema );
            String smr = getSubstringMatchingRuleNameOrNumericOidTransitive( atd, schema );
            String omr = getOrderingMatchingRuleNameOrNumericOidTransitive( atd, schema );
            if ( emr != null && lowerCaseIdentifiers.contains( Strings.toLowerCase( emr ) ) )
            {
                usedFromSet.add( atd );
            }
            if ( smr != null && lowerCaseIdentifiers.contains( Strings.toLowerCase( smr ) ) )
            {
                usedFromSet.add( atd );
            }
            if ( omr != null && lowerCaseIdentifiers.contains( Strings.toLowerCase( omr ) ) )
            {
                usedFromSet.add( atd );
            }
        }
        return usedFromSet;
    }


    // ── R2-D2 Walks Up The ATD Hierarchy To Find The Equality Matching Rule ──────
    // If the attribute has its own equality OID, R2-D2 returns it immediately.
    // Otherwise he climbs the superior chain looking for the first inherited one.
    // The climb stops when there is no superior or the superior is not in schema.
    // Returns null if no equality matching rule is found anywhere in the chain.
    /**
     * Gets the equality matching rule name or OID of the given attribute type,
     * walking up the superior chain transitively if not set directly.
     *
     * @param atd the attribute type description
     * @param schema the schema
     * @return the equality matching rule name or OID, may be {@code null}
     */
    public static String getEqualityMatchingRuleNameOrNumericOidTransitive( AttributeType atd, Schema schema )
    {
        if ( atd.getEqualityOid() != null )
        {
            return atd.getEqualityOid();
        }

        if ( atd.getSuperiorOid() != null && schema.hasAttributeTypeDescription( atd.getSuperiorOid() ) )
        {
            AttributeType superior = schema.getAttributeTypeDescription( atd.getSuperiorOid() );
            return getEqualityMatchingRuleNameOrNumericOidTransitive( superior, schema );
        }

        return null;
    }


    // ── R2-D2 Walks Up The ATD Hierarchy To Find The Substring Matching Rule ─────
    // If the attribute has its own substring OID, R2-D2 returns it immediately.
    // Otherwise he climbs the superior chain looking for the first inherited one.
    // The climb stops when there is no superior or the superior is not in schema.
    // Returns null if no substring matching rule is found anywhere in the chain.
    /**
     * Gets the substring matching rule name or OID of the given attribute type,
     * walking up the superior chain transitively if not set directly.
     *
     * @param atd the attribute type description
     * @param schema the schema
     * @return the substring matching rule name or OID, may be {@code null}
     */
    public static String getSubstringMatchingRuleNameOrNumericOidTransitive( AttributeType atd, Schema schema )
    {
        if ( atd.getSubstringOid() != null )
        {
            return atd.getSubstringOid();
        }

        if ( atd.getSuperiorOid() != null && schema.hasAttributeTypeDescription( atd.getSubstringOid() ) )
        {
            AttributeType superior = schema.getAttributeTypeDescription( atd.getSubstringOid() );
            return getSubstringMatchingRuleNameOrNumericOidTransitive( superior, schema );
        }

        return null;
    }


    // ── R2-D2 Walks Up The ATD Hierarchy To Find The Ordering Matching Rule ──────
    // If the attribute has its own ordering OID, R2-D2 returns it immediately.
    // Otherwise he climbs the superior chain looking for the first inherited one.
    // The climb stops when there is no superior or the superior is not in schema.
    // Returns null if no ordering matching rule is found anywhere in the chain.
    /**
     * Gets the ordering matching rule name or OID of the given attribute type,
     * walking up the superior chain transitively if not set directly.
     *
     * @param atd the attribute type description
     * @param schema the schema
     * @return the ordering matching rule name or OID, may be {@code null}
     */
    public static String getOrderingMatchingRuleNameOrNumericOidTransitive( AttributeType atd, Schema schema )
    {
        if ( atd.getOrderingOid() != null )
        {
            return atd.getOrderingOid();
        }

        if ( atd.getSuperiorOid() != null && schema.hasAttributeTypeDescription( atd.getSuperiorOid() ) )
        {
            AttributeType superior = schema.getAttributeTypeDescription( atd.getSuperiorOid() );
            return getOrderingMatchingRuleNameOrNumericOidTransitive( superior, schema );
        }

        return null;
    }


    // ── R2-D2 Walks Up The ATD Hierarchy To Find The Syntax OID ─────────────────
    // If the attribute declares its own syntax OID, R2-D2 returns it immediately.
    // Otherwise he climbs the superior chain looking for the first inherited syntax.
    // The climb stops when there is no superior or the superior is not in schema.
    // Returns null if no syntax OID is found anywhere in the inheritance chain.
    /**
     * Gets the syntax numeric OID of the given attribute type, walking up the
     * superior chain transitively if not set directly.
     *
     * @param atd the attribute type description
     * @param schema the schema
     * @return the syntax numeric OID, may be {@code null}
     */
    public static String getSyntaxNumericOidTransitive( AttributeType atd, Schema schema )
    {
        if ( atd.getSyntaxOid() != null )
        {
            return atd.getSyntaxOid();
        }

        if ( atd.getSuperiorOid() != null && schema.hasAttributeTypeDescription( atd.getSuperiorOid() ) )
        {
            AttributeType superior = schema.getAttributeTypeDescription( atd.getSuperiorOid() );
            return getSyntaxNumericOidTransitive( superior, schema );
        }

        return null;
    }


    // ── R2-D2 Walks Up The ATD Hierarchy To Find The Syntax Length Limit ────────
    // If the attribute declares its own syntax length (non-zero), R2-D2 returns it.
    // Otherwise he climbs the superior chain looking for an inherited length.
    // The climb stops when there is no superior or the superior is not in schema.
    // Returns -1 if no non-zero syntax length is found in the inheritance chain.
    /**
     * Gets the syntax length of the given attribute type, walking up the superior
     * chain transitively if not set directly.
     *
     * @param atd the attribute type description
     * @param schema the schema
     * @return the syntax length, or {@code -1} if not set in the hierarchy
     */
    public static long getSyntaxLengthTransitive( AttributeType atd, Schema schema )
    {
        if ( atd.getSyntaxLength() != 0 )
        {
            return atd.getSyntaxLength();
        }

        if ( atd.getSuperiorOid() != null && schema.hasAttributeTypeDescription( atd.getSuperiorOid() ) )
        {
            AttributeType superior = schema.getAttributeTypeDescription( atd.getSuperiorOid() );
            return getSyntaxLengthTransitive( superior, schema );
        }

        return -1;
    }


    // ── R2-D2 Finds All Matching Rule Uses That Apply To An Attribute Type ───────
    // The schema browser asks: "which matching rule uses reference this attribute?"
    // R2-D2 iterates the schema's MRUD list, intersecting each rule's applicable
    // attribute set with the ATD's lowercase identifiers.
    // Results are returned as sorted rule names using nameAndOidComparator.
    /**
     * Gets all matching rule description names that the given attribute type
     * appears in according to the schema's matching rule use descriptions.
     *
     * @param atd the attribute type description
     * @param schema the schema
     * @return the sorted set of matching rule description names
     */
    public static Collection<String> getOtherMatchingRuleDescriptionNames( AttributeType atd, Schema schema )
    {
        Set<String> otherMatchingRules = new TreeSet<String>( nameAndOidComparator );
        for ( MatchingRuleUse mrud : schema.getMatchingRuleUseDescriptions() )
        {
            Collection<String> atdSet = toLowerCaseSet( mrud.getApplicableAttributeOids() );
            if ( atdSet.removeAll( getLowerCaseIdentifiers( atd ) ) )
            {
                otherMatchingRules.addAll( mrud.getNames() );
            }
        }
        return otherMatchingRules;
    }


    // ── R2-D2 Finds All Attribute Types That Inherit From A Given Superior ────────
    // The schema browser asks: "which attribute types are derived from this one?"
    // R2-D2 walks all ATDs and checks if their superiorOid matches any of the
    // given ATD's lowercase identifiers.
    // Results are sorted by name using schemaElementNameComparator.
    /**
     * Gets all attribute type descriptions that declare the given attribute type
     * as their superior.
     *
     * @param atd the attribute type description
     * @param schema the schema
     * @return the sorted set of derived attribute type descriptions
     */
    public static Collection<AttributeType> getDerivedAttributeTypeDescriptions(
        AttributeType atd, Schema schema )
    {
        Set<AttributeType> derivedAtds = new TreeSet<AttributeType>( schemaElementNameComparator );
        for ( AttributeType derivedAtd : schema.getAttributeTypeDescriptions() )
        {
            String superType = derivedAtd.getSuperiorOid();
            if ( superType != null && getLowerCaseIdentifiers( atd ).contains( Strings.toLowerCase( superType ) ) )
            {
                derivedAtds.add( derivedAtd );
            }
        }
        return derivedAtds;
    }


    // ── R2-D2 Finds All Object Classes That Mandate A Given Attribute ────────────
    // The schema browser asks: "which object classes require this attribute?"
    // R2-D2 walks all OCDs, collecting their transitive MUST attribute names,
    // then intersects against the ATD's lowercase identifiers.
    // Results are sorted by name using schemaElementNameComparator.
    /**
     * Gets all object class descriptions that declare the given attribute type
     * as a mandatory (MUST) attribute (transitively).
     *
     * @param atd the attribute type description
     * @param schema the schema
     * @return the sorted set of object class descriptions using the ATD as must
     */
    public static Collection<ObjectClass> getUsedAsMust( AttributeType atd, Schema schema )
    {
        Collection<String> lowerCaseIdentifiers = getLowerCaseIdentifiers( atd );
        Set<ObjectClass> ocds = new TreeSet<ObjectClass>( schemaElementNameComparator );
        for ( ObjectClass ocd : schema.getObjectClassDescriptions() )
        {
            Collection<String> mustSet = toLowerCaseSet( getMustAttributeTypeDescriptionNamesTransitive( ocd, schema ) );
            if ( mustSet.removeAll( lowerCaseIdentifiers ) )
            {
                ocds.add( ocd );
            }
        }
        return ocds;
    }


    // ── R2-D2 Finds All Object Classes That Allow A Given Attribute ──────────────
    // The schema browser asks: "which object classes permit (but don't require) this?"
    // R2-D2 walks all OCDs, collecting their transitive MAY attribute names,
    // then intersects against the ATD's lowercase identifiers.
    // Results are sorted by name using schemaElementNameComparator.
    /**
     * Gets all object class descriptions that declare the given attribute type
     * as an optional (MAY) attribute (transitively).
     *
     * @param atd the attribute type description
     * @param schema the schema
     * @return the sorted set of object class descriptions using the ATD as may
     */
    public static Collection<ObjectClass> getUsedAsMay( AttributeType atd, Schema schema )
    {
        Collection<String> lowerCaseIdentifiers = getLowerCaseIdentifiers( atd );
        Set<ObjectClass> ocds = new TreeSet<ObjectClass>( schemaElementNameComparator );
        for ( ObjectClass ocd : schema.getObjectClassDescriptions() )
        {
            Collection<String> mustSet = toLowerCaseSet( getMayAttributeTypeDescriptionNamesTransitive( ocd, schema ) );
            if ( mustSet.removeAll( lowerCaseIdentifiers ) )
            {
                ocds.add( ocd );
            }
        }
        return ocds;
    }


    // ── R2-D2 Resolves Only The Superior OCDs That Exist In The Schema ───────────
    // When walking the OCD hierarchy, some superiors may be unknown (dummy).
    // R2-D2 filters the raw superior OID list to only those the schema has filed.
    // Unknown superiors are silently skipped to keep the walk bounded.
    // This private helper is used by the transitive must/may walkers.
    private static Collection<ObjectClass> getExistingSuperiorObjectClassDescription(
        ObjectClass ocd, Schema schema )
    {
        List<ObjectClass> superiorList = new ArrayList<ObjectClass>();
        for ( String superior : ocd.getSuperiorOids() )
        {
            if ( schema.hasObjectClassDescription( superior ) )
            {
                superiorList.add( schema.getObjectClassDescription( superior ) );
            }
        }
        return superiorList;
    }


    // ── R2-D2 Resolves All Direct Superior Object Classes (Including Dummies) ─────
    // The schema browser asks: "what are this OC's direct parent classes?"
    // Unlike getExistingSuperiorObjectClassDescription, this method includes dummy
    // placeholders for unknown superiors so the full declared list is returned.
    // Each superior OID is resolved via schema.getObjectClassDescription.
    /**
     * Gets the superior object class descriptions of the given object class
     * description (including dummy placeholders for unknowns).
     *
     * @param ocd the object class description
     * @param schema the schema
     * @return the list of superior object class descriptions
     */
    public static List<ObjectClass> getSuperiorObjectClassDescriptions( ObjectClass ocd,
        Schema schema )
    {
        List<ObjectClass> superiorList = new ArrayList<ObjectClass>();
        for ( String superior : ocd.getSuperiorOids() )
        {
            superiorList.add( schema.getObjectClassDescription( superior ) );
        }
        return superiorList;
    }


    // ── R2-D2 Finds All Object Classes That Inherit From A Given Parent ──────────
    // The schema browser asks: "which object classes extend this one?"
    // R2-D2 walks all OCDs, lowercasing their superiorOids list and checking
    // whether any match the given OCD's identifiers.
    // Returns a flat list; does not recurse into grandchildren.
    /**
     * Gets all object class descriptions that declare the given object class
     * as a direct superior (i.e. immediate sub-classes).
     *
     * @param ocd the object class description
     * @param schema the schema
     * @return the list of sub object class descriptions
     */
    public static List<ObjectClass> getSubObjectClassDescriptions( ObjectClass ocd, Schema schema )
    {
        List<ObjectClass> subOcds = new ArrayList<ObjectClass>();
        for ( ObjectClass testOcd : schema.getObjectClassDescriptions() )
        {
            Collection<String> superiorNames = toLowerCaseSet( testOcd.getSuperiorOids() );
            if ( superiorNames.removeAll( getLowerCaseIdentifiers( ocd ) ) )
            {
                subOcds.add( testOcd );
            }
        }
        return subOcds;
    }


    // ── R2-D2 Collects MUST Attribute Names Transitively Up The OC Hierarchy ─────
    // R2-D2 gathers the OC's own must-list, then recurses into each existing
    // superior class, accumulating must-names from the full ancestor chain.
    // The result is a case-insensitively sorted TreeSet with no duplicates.
    // Used by getMustAttributeTypeDescriptions(IEntry) and entry validation.
    /**
     * Gets the must attribute type description names of the given and all
     * superior object class descriptions, transitively.
     *
     * @param ocd the object class description
     * @param schema the schema
     * @return the sorted set of must attribute type description names
     */
    public static Collection<String> getMustAttributeTypeDescriptionNamesTransitive( ObjectClass ocd,
        Schema schema )
    {
        Set<String> musts = new TreeSet<String>( nameAndOidComparator );
        musts.addAll( ocd.getMustAttributeTypeOids() );
        Collection<ObjectClass> superiors = getExistingSuperiorObjectClassDescription( ocd, schema );
        for ( ObjectClass superior : superiors )
        {
            musts.addAll( getMustAttributeTypeDescriptionNamesTransitive( superior, schema ) );
        }
        return musts;
    }


    // ── R2-D2 Collects MAY Attribute Names Transitively Up The OC Hierarchy ──────
    // Like getMustAttributeTypeDescriptionNamesTransitive but for MAY attributes.
    // R2-D2 gathers the OC's own may-list, then recurses into existing superiors.
    // The result is a case-insensitively sorted TreeSet with no duplicates.
    // Used by getMayAttributeTypeDescriptions(IEntry) and entry validation.
    /**
     * Gets the may attribute type description names of the given and all
     * superior object class descriptions, transitively.
     *
     * @param ocd the object class description
     * @param schema the schema
     * @return the sorted set of may attribute type description names
     */
    public static Collection<String> getMayAttributeTypeDescriptionNamesTransitive( ObjectClass ocd,
        Schema schema )
    {
        Set<String> mays = new TreeSet<String>( nameAndOidComparator );
        mays.addAll( ocd.getMayAttributeTypeOids() );
        Collection<ObjectClass> superiors = getExistingSuperiorObjectClassDescription( ocd, schema );
        for ( ObjectClass superior : superiors )
        {
            mays.addAll( getMayAttributeTypeDescriptionNamesTransitive( superior, schema ) );
        }
        return mays;
    }


    // ── R2-D2 Retrieves The Raw LDIF Line Stored In A Schema Element's Extension ──
    // When a schema element is parsed, its raw LDIF definition string is stored
    // as an extension under the key RAW_SCHEMA_DEFINITION_LDIF_VALUE.
    // R2-D2 extracts the first value from that extension list.
    // Returns null if the extension is absent or its list is empty.
    /**
     * Gets the LDIF line of the given schema element stored as a
     * {@link Schema#RAW_SCHEMA_DEFINITION_LDIF_VALUE} extension.
     *
     * @param asd the schema element
     * @return the LDIF line, or {@code null} if not present
     */
    public static String getLdifLine( AbstractSchemaObject asd )
    {
        List<String> ldifLines = asd.getExtensions().get( Schema.RAW_SCHEMA_DEFINITION_LDIF_VALUE );
        String ldifLine = ldifLines != null && !ldifLines.isEmpty() ? ldifLines.get( 0 ) : null;
        return ldifLine;
    }


    // ── R2-D2 Converts A Name Collection To A Lowercase HashSet ──────────────────
    // Before containment checks, R2-D2 lowercases all names for case-insensitivity.
    // Null input is handled gracefully by returning an empty set.
    // Each name in the input is individually lowercased before adding.
    // This private helper is used throughout the OCD hierarchy walkers.
    private static Collection<String> toLowerCaseSet( Collection<String> names )
    {
        Set<String> set = new HashSet<String>();
        if ( names != null )
        {
            for ( String name : names )
            {
                set.add( Strings.toLowerCase( name ) );
            }
        }
        return set;
    }


    // ── C-3PO Translates A Schema Element Into A Human-Readable String ───────────
    // C-3PO handles two cases: LdapSyntax uses its description (or OID if blank),
    // while everything else concatenates its alias names separated by ", ".
    // An empty schema object returns an empty string — C-3PO stays polite.
    // Used by schemaElementNameComparator for sorted display in the schema browser.
    /**
     * Gets the string representation of the given schema element.
     * For syntax descriptions: the description text (or OID if blank).
     * For everything else: the comma-separated alias names.
     *
     * @param asd the schema element
     * @return the string representation
     */
    public static String toString( AbstractSchemaObject asd )
    {
        StringBuffer sb = new StringBuffer();
        if ( asd instanceof LdapSyntax )
        {
            if ( asd.getDescription() != null && asd.getDescription().length() > 0 )
            {
                sb.append( asd.getDescription() );
            }
            else
            {
                sb.append( asd.getOid() );
            }
        }
        else
        {
            boolean first = true;
            for ( String name : asd.getNames() )
            {
                if ( !first )
                {
                    sb.append( ", " ); //$NON-NLS-1$
                }
                sb.append( name );
                first = false;
            }
        }
        return sb.toString();
    }


    // ── Mace Windu Confronts The Entry And Demands It Justify Its Existence ──────
    // Mace runs the full checklist: objectClass present? structural OC present?
    // Every MUST attribute present? No unallowed attributes? No empty values?
    // Each failure generates a localised error message added to the result list.
    // An empty list means the entry has passed all tests — even Mace is satisfied.
    /**
     * Checks whether the given entry is complete and returns a collection of
     * warning messages for each violation found.  An empty collection means the
     * entry is complete.  Checks: objectClass present, structural object class
     * present, all mandatory attributes present, no unallowed attributes, no
     * empty values.
     *
     * @param entry the entry to validate
     * @return a collection of warning messages, empty if the entry is complete
     */
    public static Collection<String> getEntryIncompleteMessages( IEntry entry )
    {
        Collection<String> messages = new ArrayList<String>();
        if ( entry != null )
        {
            // check objectClass attribute
            IAttribute ocAttribute = entry.getAttribute( SchemaConstants.OBJECT_CLASS_AT );
            if ( ocAttribute == null )
            {
                messages.add( Messages.getString( "SchemaUtils.NoObjectClass" ) ); //$NON-NLS-1$
            }
            String[] ocValues = ocAttribute.getStringValues();
            boolean structuralObjectClassAvailable = false;
            for ( String ocValue : ocValues )
            {
                ObjectClass ocd = entry.getBrowserConnection().getSchema().getObjectClassDescription(
                    ocValue );
                if ( ocd.getType() == ObjectClassTypeEnum.STRUCTURAL )
                {
                    structuralObjectClassAvailable = true;
                    break;
                }
            }
            if ( !structuralObjectClassAvailable )
            {
                messages.add( Messages.getString( "SchemaUtils.NoStructuralObjectClass" ) ); //$NON-NLS-1$
            }

            // check must-attributes
            Collection<AttributeType> mustAtds = getMustAttributeTypeDescriptions( entry );
            for ( AttributeType mustAtd : mustAtds )
            {
                AttributeHierarchy ah = entry.getAttributeWithSubtypes( mustAtd.getOid() );
                if ( ah == null )
                {
                    messages.add( NLS.bind( Messages.getString( "SchemaUtils.MandatoryAttributeIsMissing" ), //$NON-NLS-1$ 
                        getLowerCaseIdentifiers( mustAtd ) ) );
                }
            }

            // check unallowed attributes
            Collection<AttributeType> allAtds = getAllAttributeTypeDescriptions( entry );
            for ( IAttribute attribute : entry.getAttributes() )
            {
                if ( !attribute.isOperationalAttribute() )
                {
                    AttributeType atd = attribute.getAttributeTypeDescription();
                    if ( !allAtds.contains( atd ) )
                    {
                        messages.add( NLS.bind( Messages.getString( "SchemaUtils.AttributeNotAllowed" ), attribute //$NON-NLS-1$
                            .getDescription() ) );
                    }
                }
            }

            // check empty attributes and empty values
            for ( IAttribute attribute : entry.getAttributes() )
            {
                for ( IValue value : attribute.getValues() )
                {
                    if ( value.isEmpty() )
                    {
                        messages.add( NLS.bind( Messages.getString( "SchemaUtils.EmptyValue" ), //$NON-NLS-1$
                            attribute.getDescription() ) );
                    }
                }
            }
        }

        return messages;
    }

}
