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

package org.apache.directory.studio.ldapbrowser.core.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;


// ── CLASS: AttributeDescription — C-3PO PARSING A JAWA DIALECT PHRASE ───────
// C-3PO hears "cn;lang-de;lang-en" and immediately breaks it apart: "That's
// the Common Name attribute, spoken in German with an English fallback."  He
// identifies the base word (cn), lists the dialect tags (lang-de, lang-en),
// and notes any other option flags.  He can also render the phrase as a pure
// numeric OID code rather than the friendly name — useful when the other
// droid doesn't know any attribute names.
// This class implements RFC 4512, section 2.5: an attribute description is
// an attribute type (OID or short name) optionally followed by semicolon-
// delimited option tags such as language tags (lang-xx) or binary (;binary).
// We parse the string once in the constructor and expose the pieces separately.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Implements an attribute description as specified in RFC 4512, section 2.5:
 *
 * <pre>
 *   attributedescription = attributetype options
 *   attributetype = oid
 *   options = *( SEMI option )
 *   option = 1*keychar
 * </pre>
 *
 * <p>Examples of valid attribute descriptions:</p>
 * <ul>
 *   <li>{@code 2.5.4.0}</li>
 *   <li>{@code cn;lang-de;lang-en}</li>
 *   <li>{@code owner}</li>
 * </ul>
 *
 * <p>Think of this as C-3PO parsing a Jawa phrase: the base word is the
 * attribute type, and the semicolon-delimited suffixes are the dialect options
 * (language tags and binary flags).</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeDescription implements Serializable
{

    private static final long serialVersionUID = 1L;

    /** The user provided description. */
    private String description;

    /** The parsed attribute type. */
    private String parsedAttributeType;

    /** The parsed language tag option list. */
    private List<String> parsedLangList;

    /** The parsed option list, except the language tags. */
    private List<String> parsedOptionList;


    // ── C-3PO Splits The Phrase Into Base Word And Dialect Tags ──────────────────
    // "cn;lang-de;lang-en — base word: cn, language tags: [lang-de, lang-en],
    // other options: []."  The parsing happens once here, up front.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of AttributeDescription by parsing the given
     * attribute description string into its attribute type and options.
     *
     * <p>For example:</p>
     * <pre>
     *   AttributeDescription ad = new AttributeDescription("cn;lang-de;lang-en");
     *   // ad.getParsedAttributeType() == "cn"
     *   // ad.getParsedLangList()      == ["lang-de", "lang-en"]
     * </pre>
     *
     * @param description the attribute description string; must not be {@code null}.
     */
    public AttributeDescription( String description )
    {
        this.description = description;

        String[] attributeDescriptionComponents = description.split( IAttribute.OPTION_DELIMITER );
        this.parsedAttributeType = attributeDescriptionComponents[0];
        this.parsedLangList = new ArrayList<String>();
        this.parsedOptionList = new ArrayList<String>();
        for ( int i = 1; i < attributeDescriptionComponents.length; i++ )
        {
            String component = attributeDescriptionComponents[i];
            if ( component.startsWith( IAttribute.OPTION_LANG_PREFIX ) )
            {
                this.parsedLangList.add( component );
            }
            else
            {
                this.parsedOptionList.add( component );
            }
        }
    }


    // ── C-3PO Reads Back The Original Phrase ─────────────────────────────────────
    // "The original string, as I received it: 'cn;lang-de;lang-en'."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the user provided description.
     *
     * @return the user provided description
     */
    public String getDescription()
    {
        return description;
    }


    // ── C-3PO Reads The Base Word ─────────────────────────────────────────────────
    // "The core attribute type is: cn."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the parsed attribute type.
     *
     * @return the parsed attribute type
     */
    public String getParsedAttributeType()
    {
        return parsedAttributeType;
    }


    // ── C-3PO Lists The Language Dialect Tags ─────────────────────────────────────
    // "Dialect tags found: [lang-de, lang-en] — German first, English fallback."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the list of parsed language tags.
     *
     * @return the list of parsed language tags
     */
    public List<String> getParsedLangList()
    {
        return parsedLangList;
    }


    // ── C-3PO Lists The Non-Language Options ──────────────────────────────────────
    // "Other option flags found: [binary] — requesting binary encoding."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the list of parsed options, except the language tags.
     *
     * @return the list of parsed options, except the language tags
     */
    public List<String> getParsedOptionList()
    {
        return parsedOptionList;
    }


    // ── C-3PO Translates The Phrase To Numeric OID Format ────────────────────────
    // "In case the other droid only speaks OID, I'll rephrase as 2.5.4.3;lang-de."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute description with the numeric OID
     * instead of the descriptive attribute type.
     *
     * <p>For example, {@code cn;lang-de} becomes {@code 2.5.4.3;lang-de}.</p>
     *
     * @param schema the schema used to look up OIDs; if {@code null}, the
     *               original description is returned unchanged.
     * @return the attribute description with the numeric OID
     */
    public String toOidString( Schema schema )
    {
        if ( schema == null )
        {
            return description;
        }

        AttributeType atd = schema.getAttributeTypeDescription( parsedAttributeType );
        String oidString = atd.getOid();

        if ( !parsedLangList.isEmpty() )
        {
            for ( Iterator<String> it = parsedLangList.iterator(); it.hasNext(); )
            {
                String element = it.next();
                oidString += element;

                if ( it.hasNext() || !parsedOptionList.isEmpty() )
                {
                    oidString += IAttribute.OPTION_DELIMITER;
                }
            }
        }
        if ( !parsedOptionList.isEmpty() )
        {
            for ( Iterator<String> it = parsedOptionList.iterator(); it.hasNext(); )
            {
                String element = it.next();
                oidString += element;

                if ( it.hasNext() )
                {
                    oidString += IAttribute.OPTION_DELIMITER;
                }
            }
        }

        return oidString;
    }


    // ── C-3PO Checks Whether One Phrase Is A Dialect Sub-Type Of Another ─────────
    // "Is 'givenName;lang-de' a more specific form of 'name'?  Yes — givenName
    // inherits from name, and lang-de satisfies no language constraint on name."
    // This matters for returned-attributes matching: if a search asks for 'name',
    // should we display 'givenName;lang-de'?  Only if it's a subtype.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks if the given attribute description is subtype of
     * this attribute description.
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code name.isSubtypeOf(givenName)} → {@code false} (name is the supertype)</li>
     *   <li>{@code givenName.isSubtypeOf(name)} → {@code true}</li>
     *   <li>{@code givenName;lang-de.isSubtypeOf(givenName)} → {@code true}</li>
     *   <li>{@code givenName;lang-en.isSubtypeOf(name;lang-de)} → {@code false}</li>
     * </ul>
     *
     * @param other  the other (potential supertype) attribute description.
     * @param schema the schema for type hierarchy lookups.
     * @return {@code true} if {@code other} is a supertype of {@code this}.
     */
    public boolean isSubtypeOf( AttributeDescription other, Schema schema )
    {
        // this=name, other=givenName;lang-de -> false
        // this=name;lang-en, other=givenName;lang-de -> false
        // this=givenName, other=name -> true
        // this=givenName;lang-de, other=givenName -> true
        // this=givenName;lang-de, other=name -> true
        // this=givenName;lang-en, other=name;lang-de -> false
        // this=givenName, other=givenName;lang-de -> false

        // check equal descriptions
        if ( this.toOidString( schema ).equals( other.toOidString( schema ) ) )
        {
            return false;
        }

        AttributeType myAtd = schema.getAttributeTypeDescription( this.getParsedAttributeType() );
        AttributeType otherAtd = schema.getAttributeTypeDescription( other.getParsedAttributeType() );

        // special case *: all user attributes (RFC4511)
        if ( SchemaConstants.ALL_USER_ATTRIBUTES.equals( other.description ) && !SchemaUtils.isOperational( myAtd ) )
        {
            return true;
        }

        // special case +: all operational attributes (RFC3673)
        if ( SchemaConstants.ALL_OPERATIONAL_ATTRIBUTES.equals( other.description )
            && SchemaUtils.isOperational( myAtd ) )
        {
            return true;
        }

        // special case @: attributes by object class (RFC4529)
        if ( other.description.length() > 1 && other.description.startsWith( "@" ) ) //$NON-NLS-1$
        {
            String objectClass = other.description.substring( 1 );
            ObjectClass ocd = schema.getObjectClassDescription( objectClass );
            ocd.getMayAttributeTypes();
            ocd.getMustAttributeTypes();

            Collection<String> names = new HashSet<String>();
            names.addAll( SchemaUtils.getMayAttributeTypeDescriptionNamesTransitive( ocd, schema ) );
            names.addAll( SchemaUtils.getMustAttributeTypeDescriptionNamesTransitive( ocd, schema ) );
            for ( String name : names )
            {
                AttributeType atd = schema.getAttributeTypeDescription( name );
                if ( myAtd == atd )
                {
                    return true;
                }
            }
        }

        // check type
        if ( myAtd != otherAtd )
        {
            AttributeType superiorAtd = null;
            String superiorName = myAtd.getSuperiorOid();
            while ( superiorName != null )
            {
                superiorAtd = schema.getAttributeTypeDescription( superiorName );
                if ( superiorAtd == otherAtd )
                {
                    break;
                }
                superiorName = superiorAtd.getSuperiorOid();
            }
            if ( superiorAtd != otherAtd )
            {
                return false;
            }
        }

        // check options
        List<String> myOptionsList = new ArrayList<String>( this.getParsedOptionList() );
        List<String> otherOptionsList = new ArrayList<String>( other.getParsedOptionList() );
        otherOptionsList.removeAll( myOptionsList );
        if ( !otherOptionsList.isEmpty() )
        {
            return false;
        }

        // check language tags
        List<String> myLangList = new ArrayList<String>( this.getParsedLangList() );
        List<String> otherLangList = new ArrayList<String>( other.getParsedLangList() );
        for ( String myLang : myLangList )
        {
            for ( Iterator<String> otherIt = otherLangList.iterator(); otherIt.hasNext(); )
            {
                String otherLang = otherIt.next();
                if ( otherLang.endsWith( "-" ) ) //$NON-NLS-1$
                {
                    if ( Strings.toLowerCase( myLang ).startsWith( Strings.toLowerCase( otherLang ) ) )
                    {
                        otherIt.remove();
                    }
                }
                else
                {
                    if ( myLang.equalsIgnoreCase( otherLang ) )
                    {
                        otherIt.remove();
                    }
                }
            }
        }
        if ( !otherLangList.isEmpty() )
        {
            return false;
        }

        return true;
    }

}
