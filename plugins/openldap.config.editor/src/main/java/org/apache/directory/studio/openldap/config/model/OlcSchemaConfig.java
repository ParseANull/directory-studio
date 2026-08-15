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
package org.apache.directory.studio.openldap.config.model;


import java.util.ArrayList;
import java.util.List;


// ── CLASS: OlcSchemaConfig — The Death Star Structural Blueprints ─────────────
// The Death Star schematics define every component's dimensions, connections,
// and material specifications — nothing gets built without matching the specs.
// The LDAP schema is exactly that: it defines the structure of every entry in the
// directory. Object classes, attribute types, syntaxes, matching rules — they're
// all in here. OlcSchemaConfig holds the schema entries that live under cn=schema
// in the cn=config DIT, defining what attributes exist and what they look like.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Holds the 'OlcSchemaConfig' configuration entry — the schema definitions that
 * OpenLDAP loads at startup. This includes attribute type definitions, object class
 * definitions, LDAP syntaxes, OID assignments, and DIT content rules.
 * Think of this class as the Death Star engineering blueprints: every structural
 * rule for the directory is encoded here.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcSchemaConfig extends OlcConfig
{
    /**
     * Field for the 'cn' attribute.
     */
    @ConfigurationElement(attributeType = "cn", isRdn = true, version="2.4.0")
    private List<String> cn = new ArrayList<>();

    /**
     * Field for the 'olcAttributeTypes' attribute.
     */
    @ConfigurationElement(attributeType = "olcAttributeTypes", version="2.4.0")
    private List<String> olcAttributeTypes = new ArrayList<>();

    /**
     * Field for the 'olcDitContentRules' attribute.
     */
    @ConfigurationElement(attributeType = "olcDitContentRules", version="2.4.0")
    private List<String> olcDitContentRules = new ArrayList<>();

    /**
     * Field for the 'olcLdapSyntaxes' attribute.
     */
    @ConfigurationElement(attributeType = "olcLdapSyntaxes", version="2.4.12")
    private List<String> olcLdapSyntaxes = new ArrayList<>();

    /**
     * Field for the 'olcObjectClasses' attribute.
     */
    @ConfigurationElement(attributeType = "olcObjectClasses", version="2.4.0")
    private List<String> olcObjectClasses = new ArrayList<>();

    /**
     * Field for the 'olcObjectIdentifier' attribute.
     */
    @ConfigurationElement(attributeType = "olcObjectIdentifier", version="2.4.0")
    private List<String> olcObjectIdentifier = new ArrayList<>();


    // ── Clear CN — Wipe the Blueprint Cover Page ──────────────────────────────────
    // The engineers erase the old cover page identifier before stamping a new one.
    // This resets the cn list so a fresh value can be loaded.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all cn values. Useful when replacing the entire naming attribute list.
     *
     * <p>For example — engineers clear the old blueprint label:</p>
     * <pre>
     *   schemaConfig.clearCn();
     *   schemaConfig.addCn( "{0}core" );
     * </pre>
     */
    public void clearCn()
    {
        cn.clear();
    }


    // ── Clear Attribute Types — Wipe Attribute Specs ──────────────────────────────
    // The engineers tear out all the attribute type specification pages to start fresh.
    // Clearing before a wholesale reload avoids stale duplicates.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all olcAttributeTypes entries.
     *
     * <p>For example — engineers clear the attribute type pages:</p>
     * <pre>
     *   schemaConfig.clearOlcAttributeTypes();
     * </pre>
     */
    public void clearOlcAttributeTypes()
    {
        olcAttributeTypes.clear();
    }


    // ── Clear DIT Content Rules — Wipe the Entry Structure Rules ─────────────────
    // The engineers tear out the DIT content rule pages — rules that say which
    // auxiliary classes an entry may carry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all olcDitContentRules entries.
     *
     * <p>For example — engineers clear the entry structure rules:</p>
     * <pre>
     *   schemaConfig.clearOlcDitContentRules();
     * </pre>
     */
    public void clearOlcDitContentRules()
    {
        olcDitContentRules.clear();
    }


    // ── Clear LDAP Syntaxes — Wipe the Syntax Definitions ────────────────────────
    // The engineers remove all custom syntax definitions before reloading.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all olcLdapSyntaxes entries.
     *
     * <p>For example — engineers remove all syntax spec pages:</p>
     * <pre>
     *   schemaConfig.clearOlcLdapSyntaxes();
     * </pre>
     */
    public void clearOlcLdapSyntaxes()
    {
        olcLdapSyntaxes.clear();
    }


    // ── Clear Object Classes — Wipe the Component Blueprints ─────────────────────
    // The engineers clear out all object class definitions before reloading a fresh set.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all olcObjectClasses entries.
     *
     * <p>For example — engineers clear the object class pages:</p>
     * <pre>
     *   schemaConfig.clearOlcObjectClasses();
     * </pre>
     */
    public void clearOlcObjectClasses()
    {
        olcObjectClasses.clear();
    }


    // ── Clear Object Identifiers — Wipe the OID Assignments ─────────────────────
    // The engineers remove all OID-to-name assignments before reloading fresh ones.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all olcObjectIdentifier entries.
     *
     * <p>For example — engineers clear the OID assignment pages:</p>
     * <pre>
     *   schemaConfig.clearOlcObjectIdentifier();
     * </pre>
     */
    public void clearOlcObjectIdentifier()
    {
        olcObjectIdentifier.clear();
    }


    // ── Add CN — Stamp a New Blueprint Label ─────────────────────────────────────
    // Engineers stamp a new identifier on the blueprint cover so it can be located
    // in the DIT tree (cn=schema is the usual value here).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more cn values (the entry's naming attribute) to our list.
     *
     * <p>For example — engineers stamp a blueprint label:</p>
     * <pre>
     *   schemaConfig.addCn( "{0}core" );
     * </pre>
     *
     * @param strings  one or more cn values to add
     */
    public void addCn( String... strings )
    {
        for ( String string : strings )
        {
            cn.add( string );
        }
    }


    // ── Add Attribute Types — Register New Attribute Specs ───────────────────────
    // Engineers add new attribute type definition pages to the blueprint binder.
    // Each string is an RFC-4512 attribute type definition.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more olcAttributeTypes definition strings.
     * These are RFC-4512 formatted attribute type descriptions
     * (e.g., "( 0.9.2342.19200300.100.1.3 NAME 'mail' ... )").
     *
     * <p>For example — engineers add new attribute spec pages:</p>
     * <pre>
     *   schemaConfig.addOlcAttributeTypes( "( 1.2.3 NAME 'myAttr' ... )" );
     * </pre>
     *
     * @param strings  attribute type definition strings to append
     */
    public void addOlcAttributeTypes( String... strings )
    {
        for ( String string : strings )
        {
            olcAttributeTypes.add( string );
        }
    }


    // ── Add DIT Content Rules — Register Entry Structure Rules ───────────────────
    // Engineers add rules that govern which auxiliary classes an entry of a given
    // structural class may or must carry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more olcDitContentRules definition strings.
     *
     * <p>For example — engineers add entry structure rules:</p>
     * <pre>
     *   schemaConfig.addOlcDitContentRules( "( 2.5.6.6 AUX labeledURIObject )" );
     * </pre>
     *
     * @param strings  DIT content rule definition strings to append
     */
    public void addOlcDitContentRules( String... strings )
    {
        for ( String string : strings )
        {
            olcDitContentRules.add( string );
        }
    }


    // ── Add LDAP Syntaxes — Register Custom Syntaxes ─────────────────────────────
    // Engineers add new syntax specification pages defining how attribute values
    // are encoded and validated.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more olcLdapSyntaxes definition strings.
     *
     * <p>For example — engineers add a custom syntax definition:</p>
     * <pre>
     *   schemaConfig.addOlcLdapSyntaxes( "( 1.3.6.1.4.1.1466.115.121.1.15 DESC 'Directory String' )" );
     * </pre>
     *
     * @param strings  LDAP syntax definition strings to append
     */
    public void addOlcLdapSyntaxes( String... strings )
    {
        for ( String string : strings )
        {
            olcLdapSyntaxes.add( string );
        }
    }


    // ── Add Object Classes — Register New Component Blueprints ───────────────────
    // Engineers add object class definitions — the structural blueprints that define
    // what attributes an entry of that class must or may carry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more olcObjectClasses definition strings.
     *
     * <p>For example — engineers add a new object class blueprint:</p>
     * <pre>
     *   schemaConfig.addOlcObjectClasses( "( 2.5.6.6 NAME 'person' MUST cn MAY ... )" );
     * </pre>
     *
     * @param strings  object class definition strings to append
     */
    public void addOlcObjectClasses( String... strings )
    {
        for ( String string : strings )
        {
            olcObjectClasses.add( string );
        }
    }


    // ── Add Object Identifiers — Register OID Name Assignments ───────────────────
    // Engineers add OID shorthand aliases so schema definitions can use readable
    // names rather than full dotted-decimal OID strings.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more olcObjectIdentifier definition strings
     * (OID name assignments, e.g., "mySchema 1.2.3.4").
     *
     * <p>For example — engineers add OID alias assignments:</p>
     * <pre>
     *   schemaConfig.addOlcObjectIdentifier( "mySchema 1.2.3.4" );
     * </pre>
     *
     * @param strings  OID name assignment strings to append
     */
    public void addOlcObjectIdentifier( String... strings )
    {
        for ( String string : strings )
        {
            olcObjectIdentifier.add( string );
        }
    }


    // ── Get CN — Read the Blueprint Label ────────────────────────────────────────
    // The engineer reads the label stamped on the blueprint cover and returns a copy.
    // He never hands over his original binder.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the cn list.
     *
     * @return  a copy of the cn list; never null
     */
    public List<String> getCn()
    {
        return copyListString( cn );
    }


    // ── Get Attribute Types — Read the Attribute Spec Pages ──────────────────────
    // The engineer reads out the attribute type definitions and hands over a copy.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the olcAttributeTypes list.
     *
     * @return  a copy of the attribute type definitions; never null
     */
    public List<String> getOlcAttributeTypes()
    {
        return copyListString( olcAttributeTypes );
    }


    // ── Get DIT Content Rules — Read the Entry Structure Rules ───────────────────
    // The engineer reads out the DIT content rules and hands over a copy.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the olcDitContentRules list.
     *
     * @return  a copy of the DIT content rule definitions; never null
     */
    public List<String> getOlcDitContentRules()
    {
        return copyListString( olcDitContentRules );
    }


    // ── Get LDAP Syntaxes — Read the Syntax Definitions ──────────────────────────
    // The engineer reads out the LDAP syntax definitions and hands over a copy.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the olcLdapSyntaxes list.
     *
     * @return  a copy of the LDAP syntax definitions; never null
     */
    public List<String> getOlcLdapSyntaxes()
    {
        return copyListString( olcLdapSyntaxes );
    }


    // ── Get Object Classes — Read the Component Blueprints ───────────────────────
    // The engineer reads out all object class definitions and hands over a copy.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the olcObjectClasses list.
     *
     * @return  a copy of the object class definitions; never null
     */
    public List<String> getOlcObjectClasses()
    {
        return copyListString( olcObjectClasses );
    }


    // ── Get Object Identifiers — Read the OID Assignments ────────────────────────
    // The engineer reads out the OID name assignments and hands over a copy.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the olcObjectIdentifier list.
     *
     * @return  a copy of the OID name assignments; never null
     */
    public List<String> getOlcObjectIdentifier()
    {
        return copyListString( olcObjectIdentifier );
    }


    // ── Set CN — Load New Blueprint Label ────────────────────────────────────────
    // The engineer stamps a new cover label onto the blueprint binder, replacing
    // the old one with a defensive copy of the provided list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the cn list with a defensive copy of the given list.
     *
     * @param cn  the new cn values; may be null (treated as empty)
     */
    public void setCn( List<String> cn )
    {
        this.cn = copyListString( cn );
    }


    // ── Set Attribute Types — Load New Attribute Specs ───────────────────────────
    // The engineer replaces all attribute type definition pages with a fresh set.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the olcAttributeTypes list with a defensive copy of the given list.
     *
     * @param olcAttributeTypes  the new attribute type definitions; may be null
     */
    public void setOlcAttributeTypes( List<String> olcAttributeTypes )
    {
        this.olcAttributeTypes = copyListString( olcAttributeTypes );
    }


    // ── Set DIT Content Rules — Load New Entry Structure Rules ───────────────────
    // The engineer replaces all DIT content rules with a fresh set.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the olcDitContentRules list with a defensive copy of the given list.
     *
     * @param olcDitContentRules  the new DIT content rules; may be null
     */
    public void setOlcDitContentRules( List<String> olcDitContentRules )
    {
        this.olcDitContentRules = copyListString( olcDitContentRules );
    }


    // ── Set LDAP Syntaxes — Load New Syntax Definitions ──────────────────────────
    // The engineer replaces all LDAP syntax definitions with a fresh set.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the olcLdapSyntaxes list with a defensive copy of the given list.
     *
     * @param olcLdapSyntaxes  the new LDAP syntax definitions; may be null
     */
    public void setOlcLdapSyntaxes( List<String> olcLdapSyntaxes )
    {
        this.olcLdapSyntaxes = copyListString( olcLdapSyntaxes );
    }


    // ── Set Object Classes — Load New Component Blueprints ───────────────────────
    // The engineer replaces all object class definitions with a fresh set.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the olcObjectClasses list with a defensive copy of the given list.
     *
     * @param olcObjectClasses  the new object class definitions; may be null
     */
    public void setOlcObjectClasses( List<String> olcObjectClasses )
    {
        this.olcObjectClasses = copyListString( olcObjectClasses );
    }


    // ── Set Object Identifiers — Load New OID Assignments ────────────────────────
    // The engineer replaces all OID name assignments with a fresh set.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the olcObjectIdentifier list with a defensive copy of the given list.
     *
     * @param olcObjectIdentifier  the new OID name assignments; may be null
     */
    public void setOlcObjectIdentifier( List<String> olcObjectIdentifier )
    {
        this.olcObjectIdentifier = copyListString( olcObjectIdentifier );
    }
}
