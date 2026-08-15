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
import java.util.List;
import java.util.Map;

import org.apache.directory.api.ldap.aci.ACIItemParser;
import org.apache.directory.api.ldap.aci.UserClass;
import org.apache.directory.api.ldap.aci.UserFirstACIItem;
import org.apache.directory.api.ldap.model.subtree.SubtreeSpecification;
import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
import org.eclipse.osgi.util.NLS;


// ── CLASS: UserClassWrapper — ISB USER-CATEGORY ROW ──────────────────────────
// The ISB clearance manifest also lists every category of user that can be
// granted or denied access: all users, the entry itself, its parent, specific
// names, groups, and subtrees.
// Each row is a UserClassWrapper; the officer checks it and supplies any
// required parameters (DNs, subtree specs) for that category.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Table-viewer row object representing one {@link UserClass} category in the
 * ACI visual editor.
 * Values are stored as raw strings; {@link #getUserClass()} round-trips them
 * through a dummy ACI string and the {@link ACIItemParser} to produce a typed
 * {@link UserClass} instance.
 * Think of this class as a single row on the ISB user manifest: one category
 * (e.g. {@code subtree}), its current values, and the inline editor to change them.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class UserClassWrapper
{
    /** This map contains all possible user class identifiers */
    public static final Map<Class<? extends UserClass>, String> CLASS_TO_IDENTIFIER_MAP;

    static
    {
        Map<Class<? extends UserClass>, String> map = new HashMap<>();
        map.put( UserClass.AllUsers.class, "allUsers" ); //$NON-NLS-1$
        map.put( UserClass.ThisEntry.class, "thisEntry" ); //$NON-NLS-1$
        map.put( UserClass.ParentOfEntry.class, "parentOfEntry" ); //$NON-NLS-1$
        map.put( UserClass.Name.class, "name" ); //$NON-NLS-1$
        map.put( UserClass.UserGroup.class, "userGroup" ); //$NON-NLS-1$
        map.put( UserClass.Subtree.class, "subtree" ); //$NON-NLS-1$
        CLASS_TO_IDENTIFIER_MAP = Collections.unmodifiableMap( map );
    }

    /** This map contains all user class display values */
    public static final Map<Class<? extends UserClass>, String> CLASS_TO_DISPLAY_MAP;

    static
    {
        Map<Class<? extends UserClass>, String> map = new HashMap<>();
        map.put( UserClass.AllUsers.class, Messages.getString( "UserClassWrapper.userClass.allUsers.label" ) ); //$NON-NLS-1$
        map.put( UserClass.ThisEntry.class, Messages.getString( "UserClassWrapper.userClass.thisEntry.label" ) ); //$NON-NLS-1$
        map.put( UserClass.ParentOfEntry.class, Messages.getString( "UserClassWrapper.userClass.parentOfEntry.label" ) ); //$NON-NLS-1$
        map.put( UserClass.Name.class, Messages.getString( "UserClassWrapper.userClass.name.label" ) ); //$NON-NLS-1$
        map.put( UserClass.UserGroup.class, Messages.getString( "UserClassWrapper.userClass.userGroup.label" ) ); //$NON-NLS-1$
        map.put( UserClass.Subtree.class, Messages.getString( "UserClassWrapper.userClass.subtree.label" ) ); //$NON-NLS-1$
        CLASS_TO_DISPLAY_MAP = Collections.unmodifiableMap( map );
    }

    /** A dummy ACI to check syntax of the userClassValue. */
    private static final String DUMMY = "{ identificationTag \"id1\", precedence 1, authenticationLevel simple, " //$NON-NLS-1$
        + "itemOrUserFirst userFirst: { userClasses  { #identifier# #values# }, " //$NON-NLS-1$
        + "userPermissions { { protectedItems { entry }, grantsAndDenials { grantRead } } } } }"; //$NON-NLS-1$

    /** The class of the user class, never null. */
    private final Class<? extends UserClass> clazz;

    /** The user class values, may be empty. */
    private final List<String> values;

    /** The value prefix, prepended to the value. */
    private final String valuePrefix;

    /** The value suffix, appended to the value. */
    private final String valueSuffix;

    /** The value editor, null means no value. */
    private final AbstractDialogStringValueEditor valueEditor;


    // ── INITIALISE THE USER-CATEGORY ROW ─────────────────────────────────────
    // An ISB clerk sets up the row template: which user category it represents,
    // how values are wrapped in ACI syntax (prefix/suffix), and which terminal
    // is used to edit those values.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code UserClassWrapper} for a specific user-class category.
     *
     * <p>For example — wrapping the {@code subtree} category:</p>
     * <pre>
     *   new UserClassWrapper(UserClass.Subtree.class, "{ ", " }", subtreeEditor)
     *   // values wrapped in braces, edited via the subtree-specification editor
     * </pre>
     *
     * @param clazz        the Java class of the {@link UserClass} this row represents
     * @param valuePrefix  string prepended before each value in the serialised ACI
     * @param valueSuffix  string appended after each value in the serialised ACI
     * @param valueEditor  the dialog value editor; {@code null} means no editable value
     */
    public UserClassWrapper( Class<? extends UserClass> clazz, String valuePrefix, String valueSuffix,
        AbstractDialogStringValueEditor valueEditor )
    {
        this.clazz = clazz;
        this.valuePrefix = valuePrefix;
        this.valueSuffix = valueSuffix;
        this.valueEditor = valueEditor;

        this.values = new ArrayList<>();
    }


    // ── ROUND-TRIP PARSE TO TYPED OBJECT ─────────────────────────────────────
    // The ISB manifest row holds its data as plain text; to submit to the
    // security council the clerk converts it to the official typed form.
    // We embed the raw values into a dummy ACI string and let ACIItemParser
    // produce a typed UserClass, then extract it from the parsed result.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Parses the current raw values and returns a typed {@link UserClass}.
     * Injects the identifier and flat value into a minimal dummy ACI string,
     * parses with {@link ACIItemParser}, and extracts the first user class.
     *
     * <p>For example — converting a {@code name} row back to Java:</p>
     * <pre>
     *   wrapper.getValues().add("uid=admin,dc=example,dc=com");
     *   UserClass uc = wrapper.getUserClass();
     *   // uc is a UserClass.Name containing the DN
     * </pre>
     *
     * @return the parsed {@link UserClass} corresponding to the current values
     * @throws ParseException if the raw values produce invalid ACI syntax
     */
    public UserClass getUserClass() throws ParseException
    {
        String flatValue = getFlatValue();
        String spec = DUMMY;
        spec = spec.replaceAll( "#identifier#", getIdentifier() ); //$NON-NLS-1$
        spec = spec.replaceAll( "#values#", flatValue ); //$NON-NLS-1$
        ACIItemParser parser = new ACIItemParser( null );
        UserFirstACIItem aci = null;

        try
        {
            aci = ( UserFirstACIItem ) parser.parse( spec );
        }
        catch ( ParseException e )
        {
            String msg = NLS.bind(
                Messages.getString( "UserClassWrapper.error.message" ), new String[] { getIdentifier(), flatValue } ); //$NON-NLS-1$
            throw new ParseException( msg, 0 );
        }

        return aci.getUserClasses().iterator().next();
    }


    // ── LOAD VALUES FROM A TYPED OBJECT ──────────────────────────────────────
    // When the dialog opens with an existing ACI, the row is populated from the
    // already-parsed typed object by extracting its fields as plain strings.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates this wrapper's raw-string values from an existing typed
     * {@link UserClass}.
     * Clears the current list, then dispatches on the concrete subclass to extract
     * its fields as strings (DNs for Name/UserGroup, serialised specs for Subtree).
     *
     * <p>For example — loading an existing {@code userGroup} class:</p>
     * <pre>
     *   wrapper.setUserClass(existingUserGroupClass);
     *   // wrapper.getValues() now contains the group DNs as strings
     * </pre>
     *
     * @param userClass  the typed user class to load; must be an instance of {@link #getClazz()}
     */
    public void setUserClass( UserClass userClass )
    {
        assert userClass.getClass() == getClazz();

        // first clear values
        values.clear();

        // switch on userClass type
        // no value in UserClass.AllUsers and UserClass.ThisEntry
        if ( userClass.getClass() == UserClass.Name.class )
        {
            UserClass.Name name = ( UserClass.Name ) userClass;

            for ( String jndiName : name.getNames() )
            {
                values.add( jndiName );
            }
        }
        else if ( userClass.getClass() == UserClass.UserGroup.class )
        {
            UserClass.UserGroup userGroups = ( UserClass.UserGroup ) userClass;

            for ( String jndiName : userGroups.getNames() )
            {
                values.add( jndiName );
            }
        }
        else if ( userClass.getClass() == UserClass.Subtree.class )
        {
            UserClass.Subtree subtree = ( UserClass.Subtree ) userClass;

            for ( SubtreeSpecification subtreeSpecification : subtree.getSubtreeSpecifications() )
            {
                StringBuilder buffer = new StringBuilder();
                subtreeSpecification.toString( buffer );
                values.add( buffer.toString() );
            }
        }
    }


    // ── FORMAT THE ROW FOR TABLE DISPLAY ─────────────────────────────────────
    // The ISB manifest prints each row as: category label followed by a short
    // preview of the current values, truncated to keep the table readable.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable summary for display in the user-classes table.
     * Shows the category display name followed by a colon-separated value preview,
     * truncated to 40 characters with "..." in the middle for very long values.
     *
     * @return a display string for use in the table viewer
     */
    public String toString()
    {
        String flatValue = getFlatValue();
        StringBuilder sb = new StringBuilder();

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

        return sb.append( getDisplayName() ).append( ' ' ).append( flatValue ).toString(); //$NON-NLS-1$
    }


    // ── SERIALISE VALUES TO FLAT STRING ──────────────────────────────────────
    // We wrap all values in braces (user classes are always multi-valued in ACI)
    // and join them with commas, adding the configured prefix/suffix around each.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises the current values list to a flat ACI-syntax string wrapped in
     * curly braces, for injection into the dummy ACI template.
     * Returns an empty string if there is no value editor or no values.
     *
     * @return the serialised value string, e.g. {@code "{ \"uid=admin,dc=example,dc=com\" }"}
     */
    private String getFlatValue()
    {
        if ( ( valueEditor == null ) || values.isEmpty() )
        {
            return ""; //$NON-NLS-1$
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append( "{ " ); //$NON-NLS-1$

        boolean isFirst = true;

        for ( String value : values )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                buffer.append( ", " ); //$NON-NLS-1$
            }

            buffer.append( valuePrefix );
            buffer.append( value );
            buffer.append( valueSuffix );
        }

        buffer.append( " }" ); //$NON-NLS-1$

        return buffer.toString();
    }


    // ── EXPOSE THE LIVE VALUES LIST ───────────────────────────────────────────
    // The multi-valued dialog and user-classes composite mutate this list directly;
    // we return the live reference intentionally.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the mutable list of raw string values for this user-class row.
     * Callers (e.g. the multi-valued dialog) may add, remove, or replace entries.
     *
     * @return the live, modifiable list of raw value strings
     */
    public List<String> getValues()
    {
        return values;
    }


    // ── LOOK UP THE DISPLAY LABEL ─────────────────────────────────────────────
    // The table viewer needs the category's human-readable label for the
    // first column; we fetch it from the pre-built display map.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised display name for this user-class category,
     * sourced from {@link #CLASS_TO_DISPLAY_MAP}.
     *
     * @return the display name, e.g. "All Users"
     */
    public String getDisplayName()
    {
        return CLASS_TO_DISPLAY_MAP.get( clazz );
    }


    // ── LOOK UP THE ACI IDENTIFIER ────────────────────────────────────────────
    // When serialising back to ACI syntax, we need the official token name for
    // injection into the dummy template.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the ACI syntax identifier for this user-class category,
     * sourced from {@link #CLASS_TO_IDENTIFIER_MAP}.
     *
     * @return the ACI token, e.g. {@code "subtree"}
     */
    public String getIdentifier()
    {
        return CLASS_TO_IDENTIFIER_MAP.get( clazz );
    }


    // ── EXPOSE THE JAVA CLASS ─────────────────────────────────────────────────
    // Used by setUserClass() to assert correct subtype and by the factory when
    // building typed instances during table population.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Java class of the {@link UserClass} subtype this row represents.
     *
     * @return the user-class class, never {@code null}
     */
    public Class<? extends UserClass> getClazz()
    {
        return clazz;
    }


    // ── CHECK IF THE ROW IS EDITABLE ─────────────────────────────────────────
    // Categories without a configurable value (e.g. "allUsers", "thisEntry")
    // should appear as non-editable in the table.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this row has an associated value editor.
     * Categories without a value editor are displayed as read-only rows.
     *
     * @return {@code true} when a value editor is present
     */
    public boolean isEditable()
    {
        return valueEditor != null;
    }


    // ── PROVIDE THE VALUE EDITOR ──────────────────────────────────────────────
    // The user-classes composite retrieves this editor to open when the user
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
}
