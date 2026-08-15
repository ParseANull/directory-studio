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
package org.apache.directory.studio.openldap.config.acl.model;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPlugin;

// ── CLASS: AclAttribute — DEATH STAR MANIFEST: SINGLE ATTRIBUTE DESCRIPTOR ───
// Each item in Tarkin's attribute list is a descriptor that identifies one
// LDAP attribute or special token. It can be a plain attribute name (uid, cn),
// an ObjectClass prefixed with "@" (@inetOrgPerson), an ObjectClass exclusion
// prefixed with "!" (!groupOfNames), or one of the two magic tokens "entry"
// (the entry itself) or "children" (the entry's children pseudo-attribute).
// The smart setter in setName() reads the prefix and sets the right flags
// automatically, even attempting a schema lookup if a connection is available.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents a single attribute specification in an ACL what-clause's
 * {@code attrs=} list. Handles four flavours:
 * <ul>
 *   <li>Attribute type — plain name like {@code uid}</li>
 *   <li>ObjectClass — prefixed with {@code @} like {@code @inetOrgPerson}</li>
 *   <li>ObjectClass exclusion — prefixed with {@code !} like {@code !groupOfNames}</li>
 *   <li>Special tokens — {@code entry} or {@code children}</li>
 * </ul>
 * Think of this class as one line item on Tarkin's attribute manifest — each
 * line says what kind of schema element it refers to and carries a live
 * schema reference when a connection is available.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclAttribute
{
    /** The special "entry" constant */
    public static final String ENTRY = "entry";

    /** The special "children" constant */
    public static final String CHILDREN = "children";

    /** The special ExtensibleObject constant*/
    public static final String EXTENSIBLE_OBJECT = "extensibleObject";

    /** The prefix for ObjectClass */
    public static final char OC = '@';

    /** The prefix for ObjectClass exclusion */
    public static final char OC_EX = '!';

    /** The AttributeType, if we know about it */
    private AttributeType attributeType;

    /** The ObjectClass, if we know about it */
    private ObjectClass objectClass;

    /** The attributeName */
    private String name;

    /** A flag set when we are storing an AttributeType */
    private boolean isAttributeType = false;

    /** A flag set when we are storing an ObjectClass */
    private boolean isObjectClass = false;

    /** A flag set when we are storing an ObjectClass with control on non allowed attributes */
    private boolean isObjectClassNotAllowed = false;

    /** A flag set when we stored the special 'entry' attribute */
    private boolean isEntry = false;

    /** A flag set when we stored the special 'children' attribute */
    private boolean isChildren = false;

    /** The Connection to the LDAP server */
    private IBrowserConnection connection;


    // ── Creating an AclAttribute With a Connection But No Name ────────────────
    // When the widget creates a new entry it starts with no name and lets
    // setName() default to extensibleObject. The connection is stored so that
    // a subsequent setName() can look up the schema element.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an AclAttribute with no name (defaults to extensibleObject) and
     * with the given connection for schema lookups.
     *
     * <p>For example — the widget creating a blank slot in the attribute list:</p>
     * <pre>
     *   AclAttribute attr = new AclAttribute(connection);
     *   attr.getName() // → "extensibleObject"
     * </pre>
     *
     * @param connection  The LDAP browser connection used for schema lookups;
     *                    may be {@code null}.
     */
    public AclAttribute( IBrowserConnection connection )
    {
        this.connection = connection;
        setName( "" );
    }


    // ── Creating an AclAttribute With a Name but No Connection ────────────────
    // When there is no live connection (e.g. in tests or when parsing offline)
    // we create the attribute with just a name. No schema lookup is done.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an AclAttribute with the given name but no connection. The prefix
     * logic in {@link #setName(String)} still runs to set the correct flags,
     * but no schema registry lookup is performed.
     *
     * @param name  The attribute name or special token (may include @ or ! prefix).
     */
    public AclAttribute( String name )
    {
        this.connection = null;
        setName( name );
    }


    // ── Creating an AclAttribute With Both Name and Connection ────────────────
    // The full constructor: name sets the flags and triggers a schema lookup
    // if the connection is present.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an AclAttribute with the given name and connection. The name is
     * processed immediately by {@link #setName(String)} which sets type flags and,
     * if a connection is present, attempts a schema registry lookup.
     *
     * @param name        The attribute name or special token.
     * @param connection  The LDAP browser connection for schema lookups; may be {@code null}.
     */
    public AclAttribute( String name, IBrowserConnection connection )
    {
        this.connection = connection;
        setName( name );
    }


    // ── Reading the Schema AttributeType Object ───────────────────────────────
    // If a schema lookup succeeded, this returns the live AttributeType object
    // so callers can read description, syntax, etc. without re-looking it up.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the schema {@link AttributeType} resolved from the schema registry,
     * or {@code null} if no connection was available or the name was not found
     * in the schema.
     *
     * @return  The {@link AttributeType}; may be {@code null}.
     */
    public AttributeType getAttributeType()
    {
        return attributeType;
    }


    // ── Reading the Schema ObjectClass Object ─────────────────────────────────
    // If a schema lookup succeeded for an ObjectClass, this returns it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the schema {@link ObjectClass} resolved from the schema registry,
     * or {@code null} if no connection was available or this is not an
     * ObjectClass attribute.
     *
     * @return  The {@link ObjectClass}; may be {@code null}.
     */
    public ObjectClass getObjectClass()
    {
        return objectClass;
    }


    // ── Reading the Stored Name ────────────────────────────────────────────────
    // Tarkin reads the stored name (without any prefix character) from the
    // attribute descriptor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the name stored in this attribute descriptor, without any prefix
     * character (the @ and ! are stripped by {@link #setName(String)}).
     *
     * <p>For example — reading the name:</p>
     * <pre>
     *   attr.setName("@inetOrgPerson");
     *   attr.getName() // → "inetOrgPerson"   (prefix stripped)
     *   attr.isObjectClass() // → true
     * </pre>
     *
     * @return  The attribute or class name, without any prefix character.
     */
    public String getName()
    {
        return name;
    }


    // ── Smart Name Setter: Detects Type From Prefix and Looks Up Schema ───────
    // This is the heart of the class. Tarkin's manifest parser reads the raw
    // string, detects whether it starts with "@" (ObjectClass), "!" (excluded
    // ObjectClass), is "entry" or "children", or is a plain attribute name.
    // It sets the right boolean flags and optionally fetches the live schema
    // element if a connection is available.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the name and updates all type flags accordingly. The logic is:
     * <ul>
     *   <li>Empty string → defaults to extensibleObject ObjectClass</li>
     *   <li>Starts with {@code @} → ObjectClass, name is the rest</li>
     *   <li>Starts with {@code !} → ObjectClass exclusion, name is the rest</li>
     *   <li>{@code "entry"} (case-insensitive) → isEntry = true</li>
     *   <li>{@code "children"} (case-insensitive) → isChildren = true</li>
     *   <li>Anything else → isAttributeType = true</li>
     * </ul>
     * After setting flags, if a connection is available and the type is
     * attributeType or objectClass, we attempt a schema registry lookup to
     * populate {@link #attributeType} or {@link #objectClass}.
     *
     * <p>For example — C-3PO parsing Tarkin's attribute list entry:</p>
     * <pre>
     *   attr.setName("@inetOrgPerson"); // → isObjectClass=true, name="inetOrgPerson"
     *   attr.setName("entry");          // → isEntry=true, name="entry"
     *   attr.setName("uid");            // → isAttributeType=true, name="uid"
     * </pre>
     *
     * @param name  The raw name string from the ACL text (may include prefix).
     */
    public void setName( String name )
    {
        if ( Strings.isEmpty( name ) )
        {
            //The default is externalObject ObjectClass
            isObjectClass = true;
            this.name = EXTENSIBLE_OBJECT;
        }
        else
        {
            if ( Strings.isCharASCII( name, 0, OC ) )
            {
                isObjectClass = true;
                this.name = name.substring( 1 );
            }
            else if ( Strings.isCharASCII( name, 0, OC_EX ) )
            {
                isObjectClassNotAllowed = true;
                this.name = name.substring( 1 );
            }
            else
            {
                if ( ENTRY.equalsIgnoreCase( name ) )
                {
                    isEntry = true;
                }
                else if ( CHILDREN.equalsIgnoreCase( name ) )
                {
                    isChildren = true;
                }
                else
                {
                    isAttributeType = true;
                }

                this.name = name;
            }
        }

        if ( ( isAttributeType || isObjectClass || isObjectClassNotAllowed ) && ( connection != null ) )
        {
            // Try to find the element in the schema
            try
            {
                SchemaManager schemaManager = OpenLdapConfigurationPlugin.getDefault().getSchemaManager();

                if ( schemaManager != null )
                {
                    if ( isAttributeType )
                    {
                        attributeType = schemaManager.lookupAttributeTypeRegistry( this.name );
                    }
                    else
                    {
                        // It's an ObjectClass
                        objectClass = schemaManager.lookupObjectClassRegistry( this.name );
                    }
                }
            }
            catch ( Exception e )
            {
                // Nothing to do
            }
        }
    }


    // ── Checking Whether This Is an AttributeType ─────────────────────────────
    // Tarkin checks whether this descriptor is a plain attribute type (no prefix).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this descriptor represents a plain LDAP attribute
     * type (no {@code @} or {@code !} prefix, not a special token).
     *
     * @return  {@code true} if this is an attribute type descriptor.
     */
    public boolean isAttributeType()
    {
        return isAttributeType;
    }


    // ── Checking Whether This Is an ObjectClass ───────────────────────────────
    // Tarkin checks whether this descriptor is an ObjectClass (@ prefix).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this descriptor represents an LDAP ObjectClass
     * (prefixed with {@code @}).
     *
     * @return  {@code true} if this is an ObjectClass descriptor.
     */
    public boolean isObjectClass()
    {
        return isObjectClass;
    }


    // ── Checking Whether This Is the "entry" Special Token ────────────────────
    // The "entry" token targets the entry itself, not any specific attribute.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this descriptor is the special {@code "entry"}
     * token, which targets the entry object itself rather than any attribute.
     *
     * @return  {@code true} if this is the entry pseudo-attribute.
     */
    public boolean isEntry()
    {
        return isEntry;
    }


    // ── Checking Whether This Is the "children" Special Token ─────────────────
    // The "children" token targets the pseudo-attribute that controls whether
    // child entries can be added under this entry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this descriptor is the special {@code "children"}
     * token, which controls the ability to create child entries beneath this one.
     *
     * @return  {@code true} if this is the children pseudo-attribute.
     */
    public boolean isChildren()
    {
        return isChildren;
    }


    // ── Checking Whether This Is an ObjectClass Exclusion ─────────────────────
    // The "!" prefix means "not allowed ObjectClass" — exclude attributes
    // not permitted by this objectClass.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this descriptor is an ObjectClass exclusion
     * (prefixed with {@code !}), which restricts to attributes NOT allowed by
     * that objectClass.
     *
     * @return  {@code true} if this is an ObjectClass exclusion descriptor.
     */
    public boolean isObjectClassNotAllowed()
    {
        return isObjectClassNotAllowed;
    }


    // ── Serialising the Attribute Descriptor to ACL Text ─────────────────────
    // Tarkin's adjutant writes the attribute line back into the manifest text,
    // prepending "@" for ObjectClass or "!" for exclusion when needed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises this attribute descriptor to the form expected in an ACL
     * {@code attrs=} list. Returns just the name for entry, children, and plain
     * attribute types; prepends {@code @} for ObjectClass or {@code !} for
     * ObjectClass exclusion.
     *
     * <p>For example — serialising different attribute descriptor types:</p>
     * <pre>
     *   attr("uid").toString()            // → "uid"
     *   attr("entry").toString()          // → "entry"
     *   attr("@inetOrgPerson").toString() // → "@inetOrgPerson"
     *   attr("!groupOfNames").toString()  // → "!groupOfNames"
     * </pre>
     *
     * @return  The attribute descriptor string for use in ACL text.
     */
    public String toString()
    {
        if ( isEntry || isChildren || isAttributeType )
        {
            return name;
        }

        StringBuilder buffer = new StringBuilder();

        if ( isObjectClass )
        {
            buffer.append( OC );
        }
        else
        {
            buffer.append( OC_EX );
        }

        buffer.append( name );
        return buffer.toString();
    }
}
