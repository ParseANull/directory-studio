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
package org.apache.directory.studio.aciitemeditor.model;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.directory.api.ldap.aci.ACIItemParser;
import org.apache.directory.api.ldap.aci.ItemFirstACIItem;
import org.apache.directory.api.ldap.aci.ProtectedItem;
import org.apache.directory.api.ldap.aci.protectedItem.AllAttributeValuesItem;
import org.apache.directory.api.ldap.aci.protectedItem.AllUserAttributeTypesAndValuesItem;
import org.apache.directory.api.ldap.aci.protectedItem.AllUserAttributeTypesItem;
import org.apache.directory.api.ldap.aci.protectedItem.AttributeTypeItem;
import org.apache.directory.api.ldap.aci.protectedItem.AttributeValueItem;
import org.apache.directory.api.ldap.aci.protectedItem.ClassesItem;
import org.apache.directory.api.ldap.aci.protectedItem.EntryItem;
import org.apache.directory.api.ldap.aci.protectedItem.MaxImmSubItem;
import org.apache.directory.api.ldap.aci.protectedItem.MaxValueCountElem;
import org.apache.directory.api.ldap.aci.protectedItem.MaxValueCountItem;
import org.apache.directory.api.ldap.aci.protectedItem.RangeOfValuesItem;
import org.apache.directory.api.ldap.aci.protectedItem.RestrictedByElem;
import org.apache.directory.api.ldap.aci.protectedItem.RestrictedByItem;
import org.apache.directory.api.ldap.aci.protectedItem.SelfValueItem;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
import org.eclipse.osgi.util.NLS;


// ── CLASS: ProtectedItemWrapper — ISB RESOURCE-CATEGORY ROW ──────────────────
// The Imperial Security Bureau clearance manifest lists every category of
// resource that can be guarded: the entire entry, specific attribute types,
// attribute values, self-values, and more.
// Each row in that manifest corresponds to one ProtectedItemWrapper; the officer
// checks or unchecks it and fills in any required parameters for that category.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Table-viewer row object representing one {@link ProtectedItem} category in the
 * ACI visual editor.
 * Values are stored as raw strings; the {@link #getProtectedItem()} method
 * round-trips them through a dummy ACI string and the {@link ACIItemParser} to
 * produce a typed {@link ProtectedItem} instance.
 * Think of this class as a single row on the ISB resource manifest: one category
 * (e.g. {@code attributeType}), its current values, and the inline editor used
 * to change them.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ProtectedItemWrapper
{
    /** This map contains all possible protected item identifiers */
    public static final Map<Class<? extends ProtectedItem>, String> CLASS_TO_IDENTIFIER_MAP;
    static
    {
        Map<Class<? extends ProtectedItem>, String> map = new HashMap<Class<? extends ProtectedItem>, String>();
        map.put( EntryItem.class, "entry" ); //$NON-NLS-1$
        map.put( AllUserAttributeTypesItem.class, "allUserAttributeTypes" ); //$NON-NLS-1$
        map.put( AttributeTypeItem.class, "attributeType" ); //$NON-NLS-1$
        map.put( AllAttributeValuesItem.class, "allAttributeValues" ); //$NON-NLS-1$
        map.put( AllUserAttributeTypesAndValuesItem.class, "allUserAttributeTypesAndValues" ); //$NON-NLS-1$
        map.put( AttributeValueItem.class, "attributeValue" ); //$NON-NLS-1$
        map.put( SelfValueItem.class, "selfValue" ); //$NON-NLS-1$
        map.put( RangeOfValuesItem.class, "rangeOfValues" ); //$NON-NLS-1$
        map.put( MaxValueCountItem.class, "maxValueCount" ); //$NON-NLS-1$
        map.put( MaxImmSubItem.class, "maxImmSub" ); //$NON-NLS-1$
        map.put( RestrictedByItem.class, "restrictedBy" ); //$NON-NLS-1$
        map.put( ClassesItem.class, "classes" ); //$NON-NLS-1$
        CLASS_TO_IDENTIFIER_MAP = Collections.unmodifiableMap( map );
    }

    /** This map contains all protected item display values */
    public static final Map<Class<? extends ProtectedItem>, String> CLASS_TO_DISPLAY_MAP;
    static
    {
        Map<Class<? extends ProtectedItem>, String> map = new HashMap<Class<? extends ProtectedItem>, String>();
        map.put( EntryItem.class, Messages.getString( "ProtectedItemWrapper.protectedItem.entry.label" ) ); //$NON-NLS-1$
        map.put( AllUserAttributeTypesItem.class, Messages
            .getString( "ProtectedItemWrapper.protectedItem.allUserAttributeTypes.label" ) ); //$NON-NLS-1$
        map.put( AttributeTypeItem.class, Messages
            .getString( "ProtectedItemWrapper.protectedItem.attributeType.label" ) ); //$NON-NLS-1$
        map.put( AllAttributeValuesItem.class, Messages
            .getString( "ProtectedItemWrapper.protectedItem.allAttributeValues.label" ) ); //$NON-NLS-1$
        map.put( AllUserAttributeTypesAndValuesItem.class, Messages
            .getString( "ProtectedItemWrapper.protectedItem.allUserAttributeTypesAndValues.label" ) ); //$NON-NLS-1$
        map.put( AttributeValueItem.class, Messages
            .getString( "ProtectedItemWrapper.protectedItem.attributeValue.label" ) ); //$NON-NLS-1$
        map.put( SelfValueItem.class, Messages
            .getString( "ProtectedItemWrapper.protectedItem.selfValue.label" ) ); //$NON-NLS-1$
        map.put( RangeOfValuesItem.class, Messages
            .getString( "ProtectedItemWrapper.protectedItem.rangeOfValues.label" ) ); //$NON-NLS-1$
        map.put( MaxValueCountItem.class, Messages
            .getString( "ProtectedItemWrapper.protectedItem.maxValueCount.label" ) ); //$NON-NLS-1$
        map.put( MaxImmSubItem.class, Messages
            .getString( "ProtectedItemWrapper.protectedItem.maxImmSub.label" ) ); //$NON-NLS-1$
        map.put( RestrictedByItem.class, Messages
            .getString( "ProtectedItemWrapper.protectedItem.restrictedBy.label" ) ); //$NON-NLS-1$
        map.put( ClassesItem.class, Messages.getString( "ProtectedItemWrapper.protectedItem.classes.label" ) ); //$NON-NLS-1$
        CLASS_TO_DISPLAY_MAP = Collections.unmodifiableMap( map );
    }

    /** A dummy ACI to check syntax of the protectedItemValue */
    private static final String DUMMY = "{ identificationTag \"id1\", precedence 1, authenticationLevel simple, " //$NON-NLS-1$
        + "itemOrUserFirst itemFirst: { protectedItems  { #identifier# #values# }, " //$NON-NLS-1$
        + "itemPermissions { { userClasses { allUsers }, grantsAndDenials { grantRead } } } } }"; //$NON-NLS-1$

    /** The class of the protected item, never null. */
    private final Class<? extends ProtectedItem> clazz;

    /** The protected item values, may be empty. */
    private List<String> values;

    /** The value prefix, prepended to the value. */
    private final String valuePrefix;

    /** The value suffix, appended to the value. */
    private final String valueSuffix;

    /** The value editor, null means no value. */
    private AbstractDialogStringValueEditor valueEditor;

    /** The multivalued. */
    private final boolean multivalued;


    // ── INITIALISE THE MANIFEST ROW ───────────────────────────────────────────
    // An ISB clerk fills in the row template: category class, whether multiple
    // values are allowed, how values are wrapped in syntax (prefix/suffix), and
    // which data-entry terminal handles editing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ProtectedItemWrapper} for a specific protected-item category.
     *
     * <p>For example — wrapping the {@code attributeType} category:</p>
     * <pre>
     *   new ProtectedItemWrapper(AttributeTypeItem.class, true, "", "", attributeTypeEditor)
     *   // multivalued, no extra prefix/suffix, edited via attributeTypeEditor
     * </pre>
     *
     * @param clazz        the Java class of the {@link ProtectedItem} this row represents
     * @param multivalued  {@code true} if the syntax allows multiple values wrapped in {@code { }}
     * @param valuePrefix  string prepended before each value in the serialised ACI
     * @param valueSuffix  string appended after each value in the serialised ACI
     * @param valueEditor  the dialog value editor for this category; {@code null} means no editable value
     */
    public ProtectedItemWrapper( Class<? extends ProtectedItem> clazz, boolean multivalued, String valuePrefix,
        String valueSuffix, AbstractDialogStringValueEditor valueEditor )
    {
        this.clazz = clazz;
        this.multivalued = multivalued;
        this.valuePrefix = valuePrefix;
        this.valueSuffix = valueSuffix;
        this.valueEditor = valueEditor;

        this.values = new ArrayList<String>();
    }


    // ── ROUND-TRIP PARSE TO TYPED OBJECT ─────────────────────────────────────
    // The ISB manifest row holds its data as plain text; to submit it to the
    // security council the clerk must convert it to the official typed form by
    // running it through the parser.
    // We embed the raw values into a dummy ACI string and let ACIItemParser do
    // the work, then pull the typed ProtectedItem back out.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Parses the current raw values and returns a typed {@link ProtectedItem}.
     * We inject the identifier and flat value into a minimal dummy ACI string,
     * parse it with {@link ACIItemParser}, and extract the first protected item.
     *
     * <p>For example — converting an {@code attributeType} row back to Java:</p>
     * <pre>
     *   wrapper.getValues().add("cn");
     *   ProtectedItem item = wrapper.getProtectedItem();
     *   // item is an AttributeTypeItem containing the "cn" attribute type
     * </pre>
     *
     * @return the parsed {@link ProtectedItem} corresponding to the current values
     * @throws ParseException if the raw values produce invalid ACI syntax
     */
    public ProtectedItem getProtectedItem() throws ParseException
    {
        String flatValue = getFlatValue();
        String spec = DUMMY;
        spec = spec.replaceAll( "#identifier#", getIdentifier() ); //$NON-NLS-1$
        spec = spec.replaceAll( "#values#", flatValue ); //$NON-NLS-1$
        ACIItemParser parser = new ACIItemParser( null );
        ItemFirstACIItem aci = null;
        try
        {
            aci = ( ItemFirstACIItem ) parser.parse( spec );
        }
        catch ( ParseException e )
        {

            String msg = NLS
                .bind(
                    Messages.getString( "ProtectedItemWrapper.error.message" ), new String[] { getIdentifier(), flatValue } ); //$NON-NLS-1$
            throw new ParseException( msg, 0 );
        }
        ProtectedItem item = aci.getProtectedItems().iterator().next();
        return item;
    }


    // ── LOAD VALUES FROM A TYPED OBJECT ──────────────────────────────────────
    // When the dialog opens with an existing ACI, the ISB manifest row needs to
    // be populated from the already-parsed typed object.
    // We switch on the concrete subclass and extract each field into plain strings
    // so the row can be displayed and re-edited.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates this wrapper's raw-string values from an existing typed
     * {@link ProtectedItem}.
     * Clears the current values list first, then dispatches on the concrete
     * subclass to extract its fields as strings.
     *
     * <p>For example — loading an existing {@code maxValueCount} item:</p>
     * <pre>
     *   wrapper.setProtectedItem(existingMaxValueCountItem);
     *   // wrapper.getValues() now contains the serialised tuples
     * </pre>
     *
     * @param item  the typed protected item to load; must be an instance of {@link #getClazz()}
     */
    public void setProtectedItem( ProtectedItem item )
    {
        assert item.getClass() == getClazz();

        // first clear values
        values.clear();

        // switch on userClass type
        // no value in ProtectedItem.Entry, ProtectedItem.AllUserAttributeTypes and ProtectedItem.AllUserAttributeTypesAndValues
        if ( item instanceof AttributeTypeItem )
        {
            AttributeTypeItem at = ( AttributeTypeItem ) item;

            for ( Iterator<AttributeType> it = at.iterator(); it.hasNext(); )
            {
                AttributeType attributeType = it.next();
                values.add( attributeType.getName() );
            }
        }
        else if ( item instanceof AllAttributeValuesItem )
        {
            AllAttributeValuesItem aav = ( AllAttributeValuesItem ) item;

            for ( Iterator<AttributeType> it = aav.iterator(); it.hasNext(); )
            {
                AttributeType attributeType = it.next();
                values.add( attributeType.getName() );
            }
        }
        else if ( item instanceof AttributeValueItem )
        {
            AttributeValueItem av = ( AttributeValueItem ) item;

            for ( Iterator<Attribute> it = av.iterator(); it.hasNext(); )
            {
                Attribute entryAttribute = it.next();
                values.add( entryAttribute.getId() + "=" + entryAttribute.get() ); //$NON-NLS-1$
            }
        }
        else if ( item instanceof SelfValueItem )
        {
            SelfValueItem sv = ( SelfValueItem ) item;

            for ( Iterator<AttributeType> it = sv.iterator(); it.hasNext(); )
            {
                AttributeType attributeType = it.next();
                values.add( attributeType.getName() );
            }
        }
        else if ( item instanceof RangeOfValuesItem )
        {
            RangeOfValuesItem rov = ( RangeOfValuesItem ) item;
            values.add( rov.getRefinement().toString() );
        }
        else if ( item instanceof MaxValueCountItem )
        {
            MaxValueCountItem mvc = ( MaxValueCountItem ) item;

            for ( Iterator<MaxValueCountElem> it = mvc.iterator(); it.hasNext(); )
            {
                MaxValueCountElem mvci = it.next();
                values.add( mvci.toString() );
            }
        }
        else if ( item instanceof MaxImmSubItem )
        {
            MaxImmSubItem mis = ( MaxImmSubItem ) item;
            values.add( Integer.toString( mis.getValue() ) );
        }
        else if ( item instanceof RestrictedByItem )
        {
            RestrictedByItem rb = ( RestrictedByItem ) item;

            for ( Iterator<RestrictedByElem> it = rb.iterator(); it.hasNext(); )
            {
                RestrictedByElem rbe = it.next();
                values.add( rbe.toString() );
            }
        }
        else if ( item instanceof ClassesItem )
        {
            ClassesItem classes = ( ClassesItem ) item;
            StringBuilder sb = new StringBuilder();
            classes.getClasses().printRefinementToBuffer( sb );
            values.add( sb.toString() );
        }
    }


    // ── FORMAT THE ROW FOR TABLE DISPLAY ─────────────────────────────────────
    // The ISB manifest prints each row in a human-readable summary: category name
    // followed by a truncated preview of the values.
    // Long values are trimmed to 40 characters with "..." in the middle so the
    // table stays readable without blowing out column widths.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable summary for display in the protected-items table.
     * Shows the category display name followed by a colon-separated value preview,
     * truncated to 40 characters (20 from each end with "..." in the middle) for
     * readability.
     *
     * <p>For example — an {@code attributeType} row with a long list:</p>
     * <pre>
     *   "Attribute Type: { cn, sn...telephoneNumber, mobile }"
     * </pre>
     *
     * @return a display string for use in the table viewer
     */
    public String toString()
    {
        String flatValue = getFlatValue();

        if ( flatValue.length() > 0 )
        {
            flatValue = flatValue.replace( '\r', ' ' );
            flatValue = flatValue.replace( '\n', ' ' );
            flatValue = ": " + flatValue; //$NON-NLS-1$

            if ( flatValue.length() > 40 )
            {
                String temp = flatValue;
                flatValue = temp.substring( 0, 20 );
                flatValue = flatValue + "..."; //$NON-NLS-1$
                flatValue = flatValue + temp.substring( temp.length() - 20, temp.length() );
            }
        }

        return getDisplayName() + " " + flatValue; //$NON-NLS-1$
    }


    // ── SERIALISE VALUES TO FLAT STRING ──────────────────────────────────────
    // Before injecting into the dummy ACI template, we flatten the values list
    // into a single string — wrapping in braces if multivalued, and adding the
    // configured prefix/suffix around each value.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises the current values list to a flat ACI-syntax string for injection
     * into the dummy ACI template.
     * Returns an empty string if there is no value editor or no values set.
     *
     * @return the serialised value string, e.g. {@code "{ cn, sn }"} for multivalued
     */
    private String getFlatValue()
    {
        if ( valueEditor == null || values.isEmpty() )
        {
            return ""; //$NON-NLS-1$
        }

        StringBuilder sb = new StringBuilder();

        if ( isMultivalued() )
        {
            sb.append( "{ " ); //$NON-NLS-1$
        }
        for ( Iterator<String> it = values.iterator(); it.hasNext(); )
        {
            sb.append( valuePrefix );
            String value = it.next();
            sb.append( value );
            sb.append( valueSuffix );
            if ( it.hasNext() )
            {
                sb.append( ", " ); //$NON-NLS-1$
            }
        }
        if ( isMultivalued() )
        {
            sb.append( " }" ); //$NON-NLS-1$
        }
        return sb.toString();
    }


    // ── EXPOSE THE LIVE VALUES LIST ───────────────────────────────────────────
    // The checkbox composite mutates this list directly when the user edits the
    // row; we return the live reference intentionally.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the mutable list of raw string values for this protected-item row.
     * Callers (e.g. the multi-valued dialog) may add, remove, or replace entries
     * in this list directly.
     *
     * @return the live, modifiable list of raw value strings
     */
    public List<String> getValues()
    {
        return values;
    }


    // ── LOOK UP THE DISPLAY LABEL ─────────────────────────────────────────────
    // The table viewer calls this to display the category name in the first
    // column of the protected-items table.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised display name for this protected-item category,
     * sourced from {@link #CLASS_TO_DISPLAY_MAP}.
     *
     * @return the display name, e.g. "Attribute Type"
     */
    public String getDisplayName()
    {
        return CLASS_TO_DISPLAY_MAP.get( clazz );
    }


    // ── LOOK UP THE ACI IDENTIFIER ────────────────────────────────────────────
    // When serialising back to ACI syntax, we need the official token name —
    // e.g. "attributeType" — for injection into the dummy template.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the ACI syntax identifier for this protected-item category,
     * sourced from {@link #CLASS_TO_IDENTIFIER_MAP}.
     *
     * @return the ACI token, e.g. {@code "attributeType"}
     */
    public String getIdentifier()
    {
        return CLASS_TO_IDENTIFIER_MAP.get( clazz );
    }


    // ── EXPOSE THE JAVA CLASS ─────────────────────────────────────────────────
    // Used by setProtectedItem() to assert we are receiving the correct subtype
    // and by the factory to build typed instances during table population.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Java class of the {@link ProtectedItem} subtype this row represents.
     *
     * @return the protected-item class, never {@code null}
     */
    public Class<? extends ProtectedItem> getClazz()
    {
        return clazz;
    }


    // ── CHECK IF THE ROW IS EDITABLE ─────────────────────────────────────────
    // Some categories (e.g. "entry") have no configurable value; the table should
    // show them as read-only rows.  We know this by whether a value editor exists.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this row has an associated value editor.
     * Categories without a value editor (e.g. {@code entry}) are displayed
     * as non-editable rows in the protected-items table.
     *
     * @return {@code true} when a value editor is present
     */
    public boolean isEditable()
    {
        return valueEditor != null;
    }


    // ── PROVIDE THE VALUE EDITOR ──────────────────────────────────────────────
    // The protected-items composite retrieves the editor to open when the user
    // double-clicks or presses Edit on a row.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the dialog-based value editor for this row, or {@code null} if the
     * category has no editable value.
     *
     * @return the value editor, may be {@code null}
     */
    public AbstractDialogStringValueEditor getValueEditor()
    {
        return valueEditor;
    }


    // ── CHECK IF MULTIPLE VALUES ARE ALLOWED ─────────────────────────────────
    // Multi-valued rows wrap their values in ACI curly-brace syntax; single-
    // valued rows do not.  This flag drives that choice in getFlatValue().
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this protected-item category allows multiple values
     * wrapped in curly braces in the ACI syntax.
     *
     * @return {@code true} for multi-valued categories
     */
    public boolean isMultivalued()
    {
        return multivalued;
    }
}
