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

package org.apache.directory.studio.ldapbrowser.core.utils;


import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.LookupTranslator;
import org.apache.directory.api.ldap.model.name.Ava;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.connection.core.ConnectionParameter.EncryptionMethod;
import org.apache.directory.studio.connection.core.StudioControl;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection.ModifyMode;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection.ModifyOrder;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.apache.directory.studio.ldifparser.LdifFormatParameters;
import org.apache.directory.studio.ldifparser.LdifUtils;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeModifyRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifModSpec;
import org.apache.directory.studio.ldifparser.model.lines.LdifAttrValLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifChangeTypeLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifControlLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifDnLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifModSpecSepLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifSepLine;
import org.eclipse.core.runtime.Preferences;


// ── CLASS: Utils — R2-D2'S MAIN TOOLKIT FOR THE LDAP BROWSER CORE ────────────
// R2-D2 carries the whole toolkit: normalize DNs to OID strings, compare byte
// arrays, serialize beans to XML, format file sizes, build LDAP URLs, compute
// LDIF diffs between two entries, and encode/decode RFC 4517 postal addresses.
// Utils is the general-purpose droids' helper that everything else reaches for
// when the task doesn't fit neatly into any other class.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * General-purpose static utility methods for the ldapbrowser.core plugin.
 * Covers DN normalisation, byte-array comparison, XML serialisation,
 * string helpers, LDIF formatting, LDAP URL construction, entry diff
 * computation, and RFC 4517 postal address encoding.
 *
 * <p>Think of this as R2-D2's main toolkit — everything the other classes
 * reach for when they need a cross-cutting utility.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Utils
{

    // ── R2-D2 Normalises A DN To A Lowercase OID-Keyed String ────────────────────
    // R2-D2 replaces each RDN attribute type name with its numeric OID
    // and lowercases the attribute value so the string is suitable as a schema
    // cache key (stable regardless of name aliases or case differences).
    // Example: "surname=Bar" becomes "2.5.4.4=bar".
    /**
     * Transforms the given DN into a normalised OID string usable as a schema
     * cache key.  Each RDN attribute type is replaced by its OID; values are
     * trimmed and lowercased.
     *
     * @param dn the DN
     * @param schema the schema used for OID resolution
     * @return the normalised OID string, e.g. {@code 2.5.4.4=bar,2.5.4.3=smith}
     */
    public static String getNormalizedOidString( Dn dn, Schema schema )
    {
        StringBuilder sb = new StringBuilder();

        boolean isFirst = true;
        
        for ( Rdn rdn : dn )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                sb.append( ',' );
            }

            sb.append( getOidString( rdn, schema ) );
        }

        return sb.toString();
    }


    // ── R2-D2 Converts A Single RDN To Its OID Form ──────────────────────────────
    // Each AVA in the RDN is converted to OID=value; multi-valued RDNs join with +.
    // Private helper called by getNormalizedOidString for each RDN component.
    private static String getOidString( Rdn rdn, Schema schema )
    {
        StringBuilder sb = new StringBuilder();

        boolean isFirst = true;
        
        for ( Ava ava : rdn )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                sb.append( '+' );
            }

            sb.append( getOidString( ava, schema ) );
        }

        return sb.toString();
    }


    // ── R2-D2 Converts A Single AVA To Its OID=value Form ────────────────────────
    // Resolves the AVA's norm type to its numeric OID via the schema (or uses the
    // norm type directly if schema is null), then appends the lowercased trimmed value.
    // Private helper called by getOidString(Rdn, Schema).
    private static String getOidString( Ava ava, Schema schema )
    {
        String oid = schema != null ? schema.getAttributeTypeDescription( ava.getNormType() ).getOid() : ava
            .getNormType();
        return Strings.toLowerCaseAscii( Strings.trim( oid ) )
            + "=" + Strings.trim( ava.getValue().getString() ).toLowerCase(); //$NON-NLS-1$
    }


    // ── R2-D2 Joins A String Array Into A Comma-Separated Display String ─────────
    // Null or empty arrays become the empty string.
    // Elements are joined with ", " separators.
    // Used for display-only output; not for LDAP filter construction.
    /**
     * Converts a String array to a comma-separated display string.
     *
     * @param array the array to join
     * @return the comma-separated string, or {@code ""} if null or empty
     */
    public static String arrayToString( String[] array )
    {
        if ( array == null || array.length == 0 )
        {
            return ""; //$NON-NLS-1$
        }
        else
        {
            StringBuilder sb = new StringBuilder( array[0] );
            
            for ( int i = 1; i < array.length; i++ )
            {
                sb.append( ", " ); //$NON-NLS-1$
                sb.append( array[i] );
            }
            
            return sb.toString();
        }
    }


    // ── R2-D2 Compares Two Byte Arrays Byte-By-Byte ──────────────────────────────
    // Handles reference equality, null, length mismatch, and element comparison.
    // Used by the Value class to compare binary values without java.util.Arrays.
    // Returns true only when both arrays contain identical bytes in identical order.
    /**
     * Compares two byte arrays for equality.
     *
     * @param data1 the first byte array
     * @param data2 the second byte array
     * @return {@code true} if the arrays are equal
     */
    public static boolean equals( byte[] data1, byte[] data2 )
    {
        if ( data1 == data2 )
            return true;
        if ( data1 == null || data2 == null )
            return false;
        if ( data1.length != data2.length )
            return false;
        for ( int i = 0; i < data1.length; i++ )
        {
            if ( data1[i] != data2[i] )
                return false;
        }
        return true;
    }


    // ── R2-D2 Truncates A Long String With An Ellipsis For Display ───────────────
    // If value exceeds length characters, R2-D2 cuts it at exactly length chars
    // and appends "..." to signal truncation.
    // Returns an empty string if value is null or not longer than length.
    /**
     * Returns a shortened version of the given string, appending {@code ...}
     * if the value exceeds the given length.
     *
     * @param value the string to shorten
     * @param length the maximum length before truncation
     * @return the shortened string, or {@code ""} if null/not over length
     */
    public static String getShortenedString( String value, int length )
    {
        StringBuilder sb = new StringBuilder();
        
        if ( ( value != null ) && ( value.length() > length ) )
        {
            sb.append( value.substring( 0, length ) ).append( "..." ); //$NON-NLS-1$
        }

        return sb.toString();
    }


    // ── R2-D2 Serialises A Java Bean To An XML String Via XMLEncoder ─────────────
    // R2-D2 sets the context class loader to avoid OSGi ClassLoader issues,
    // then uses XMLEncoder to write the object graph to a ByteArrayOutputStream,
    // and returns the UTF-8 decoded XML string.
    // Used for persisting preferences and configuration beans.
    /**
     * Serialises a Java bean to an XML string using {@link XMLEncoder}.
     *
     * @param o the bean to serialise
     * @return the XML string representation
     */
    public static String serialize( Object o )
    {
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        
        try
        {
            Thread.currentThread().setContextClassLoader( Utils.class.getClassLoader() );
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLEncoder encoder = new XMLEncoder( baos );
            encoder.writeObject( o );
            encoder.close();

            return LdifUtils.utf8decode( baos.toByteArray() );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( ccl );
        }
    }


    // ── R2-D2 Deserialises An XML String Back To A Java Bean ─────────────────────
    // R2-D2 sets the context class loader, UTF-8 encodes the string to bytes,
    // and uses XMLDecoder to reconstruct the original object graph.
    // Used for loading persisted preferences and configuration beans.
    /**
     * Deserialises a Java bean from an XML string using {@link XMLDecoder}.
     *
     * @param s the XML string
     * @return the deserialised object
     */
    public static Object deserialize( String s )
    {
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        
        try
        {
            Thread.currentThread().setContextClassLoader( Utils.class.getClassLoader() );
            ByteArrayInputStream bais = new ByteArrayInputStream( LdifUtils.utf8encode( s ) );
            XMLDecoder decoder = new XMLDecoder( bais );
            Object o = decoder.readObject();
            decoder.close();
            
            return o;
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( ccl );
        }
    }


    // ── R2-D2 Returns A Safe Display String For Any Object ───────────────────────
    // C-3PO would panic at a null reference; R2-D2 calmly returns "-" instead.
    // For non-null objects, calls toString() and returns the result.
    // Used throughout the UI to avoid NullPointerExceptions in display code.
    /**
     * Returns the string representation of the object, or {@code "-"} if null.
     *
     * @param o the object
     * @return the string representation or {@code "-"}
     */
    public static String getNonNullString( Object o )
    {
        return o == null ? "-" : o.toString(); //$NON-NLS-1$
    }


    // ── R2-D2 Formats A Byte Count As A Human-Readable File Size String ──────────
    // R2-D2 auto-selects the best unit: megabytes > 1 MB, kilobytes > 1 KB,
    // bytes for everything else.  The raw byte count is always shown in parentheses.
    // Uses the NLS Messages class for localised unit strings (Byte, Bytes, KB, MB).
    /**
     * Formats a byte count as a human-readable file size string.
     *
     * @param bytes the number of bytes
     * @return a human-readable string, e.g. {@code "1 MB (1048576 Bytes)"}
     */
    public static String formatBytes( long bytes )
    {
        String size = ""; //$NON-NLS-1$
        
        if ( bytes > 1024 * 1024 )
        {
            size += ( bytes / 1024 / 1024 )
                + " " + Messages.Utils_MegaBytes + " (" + bytes + " " + Messages.Utils_Bytes + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-6$
        }
        else if ( bytes > 1024 )
        {
            size += ( bytes / 1024 ) + " " + Messages.Utils_KiloBytes + " (" + bytes + " " + Messages.Utils_Bytes + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-6$
        }
        else if ( bytes > 1 )
        {
            size += bytes + " " + Messages.Utils_Bytes; //$NON-NLS-1$
        }
        else
        {
            size += bytes + " " + Messages.Utils_Byte; //$NON-NLS-1$
        }
        
        return size;
    }


    // ── Mace Windu Checks If A Collection Contains A String Regardless Of Case ───
    // Mace asks: "does this collection hold this identifier, in any case?"
    // Null collection or null target string both return false immediately.
    // Elements are compared using equalsIgnoreCase — no toLowerCase needed.
    // Used for case-insensitive LDAP attribute name containment checks.
    /**
     * Checks whether the collection contains the given string, ignoring case.
     *
     * @param c the collection to search
     * @param s the string to find
     * @return {@code true} if any element equals {@code s} ignoring case
     */
    public static boolean containsIgnoreCase( Collection<String> c, String s )
    {
        if ( c == null || s == null )
        {
            return false;
        }

        for ( String string : c )
        {
            if ( string.equalsIgnoreCase( s ) )
            {
                return true;
            }
        }

        return false;
    }


    // ── R2-D2 Reads The LDIF Formatting Preferences From The Plugin Store ────────
    // R2-D2 queries the BrowserCorePlugin preferences for three settings:
    // space-after-colon, line width, and line separator.
    // Returns a LdifFormatParameters object pre-configured from those preferences.
    // Used by saveToLdif and all LDIF export paths.
    /**
     * Builds a {@link LdifFormatParameters} instance from the current plugin
     * preferences (space-after-colon, line width, line separator).
     *
     * @return the LDIF format parameters
     */
    public static LdifFormatParameters getLdifFormatParameters()
    {
        Preferences store = BrowserCorePlugin.getDefault().getPluginPreferences();
        boolean spaceAfterColon = store.getBoolean( BrowserCoreConstants.PREFERENCE_LDIF_SPACE_AFTER_COLON );
        int lineWidth = store.getInt( BrowserCoreConstants.PREFERENCE_LDIF_LINE_WIDTH );
        String lineSeparator = store.getString( BrowserCoreConstants.PREFERENCE_LDIF_LINE_SEPARATOR );

        return new LdifFormatParameters( spaceAfterColon, lineWidth, lineSeparator );
    }


    // ── Han Builds An LDAP URL From A BrowserConnection ──────────────────────────
    // Han plots the jump coordinates: scheme (ldap or ldaps), host, and port.
    // If the connection uses LDAPS encryption, the ldaps:// scheme is chosen.
    // The DN and search parameters are not included; callers add those separately.
    // Returns an empty LdapUrl if the connection has no underlying Connection.
    /**
     * Builds an {@link LdapUrl} from a {@link IBrowserConnection}, setting the
     * scheme (ldap/ldaps), host, and port.
     *
     * @param browserConnection the browser connection
     * @return the LDAP URL
     */
    public static LdapUrl getLdapURL( IBrowserConnection browserConnection )
    {
        LdapUrl url = new LdapUrl();
        
        if ( browserConnection.getConnection() != null )
        {
            if ( browserConnection.getConnection().getEncryptionMethod() == EncryptionMethod.LDAPS )
            {
                url.setScheme( LdapUrl.LDAPS_SCHEME );
            }
            else
            {
                url.setScheme( LdapUrl.LDAP_SCHEME );
            }
            
            url.setHost( browserConnection.getConnection().getHost() );
            url.setPort( browserConnection.getConnection().getPort() );
        }
        
        return url;
    }


    // ── Han Builds An LDAP URL For A Specific Entry In The Directory ─────────────
    // Han delegates to getLdapURL(IBrowserConnection) for the connection details,
    // then adds the entry's DN as the URL path component.
    // The result uniquely identifies this entry in the directory.
    // Used for copy-as-URL and drag-and-drop operations in the UI.
    /**
     * Builds an {@link LdapUrl} for the given entry (scheme, host, port, DN).
     *
     * @param entry the entry
     * @return the LDAP URL
     */
    public static LdapUrl getLdapURL( IEntry entry )
    {
        LdapUrl url = getLdapURL( entry.getBrowserConnection() );
        url.setDn( entry.getDn() );
        
        return url;
    }


    // ── Han Builds An LDAP URL For A Search Mission Briefing ─────────────────────
    // Han delegates to getLdapURL(IBrowserConnection), then adds the search base,
    // returning attributes, scope, and filter to the URL.
    // The result encodes the full search as an RFC 4516 LDAP URL.
    // Used for copy-as-URL and shareable search link operations in the UI.
    /**
     * Builds an {@link LdapUrl} for the given search (scheme, host, port, base DN,
     * attributes, scope, filter).
     *
     * @param search the search
     * @return the LDAP URL
     */
    public static LdapUrl getLdapURL( ISearch search )
    {
        LdapUrl url = getLdapURL( search.getBrowserConnection() );
        url.setDn( search.getSearchBase() );
        
        if ( search.getReturningAttributes() != null )
        {
            url.setAttributes( Arrays.asList( search.getReturningAttributes() ) );
        }
        
        url.setScope( search.getScope().getScope() );
        url.setFilter( search.getFilter() );
        
        return url;
    }


    // ── R2-D2 Computes The LDIF Diff Between Two Entry Snapshots ─────────────────
    // R2-D2 walks every attribute description that appears in either entry,
    // decides whether to delete, add, or replace each attribute's values based on
    // the connection's ModifyMode and ModifyOrder preferences, and builds an
    // LdifChangeModifyRecord expressing the minimal required modification.
    // Returns null if there are no differences — no empty LDIF sent to the server.
    /**
     * Computes the LDIF change-modify record needed to transform {@code oldEntry}
     * into {@code newEntry}.  Returns {@code null} if the two entries are identical.
     * Respects the connection's {@link ModifyMode} and {@link ModifyOrder} settings.
     *
     * @param oldEntry the original entry
     * @param newEntry the modified entry
     * @return the LDIF file containing the modify record, or {@code null} if no change
     */
    public static LdifFile computeDiff( IEntry oldEntry, IEntry newEntry )
    {
        // get connection parameters
        ModifyMode modifyMode = oldEntry.getBrowserConnection().getModifyMode();
        ModifyMode modifyModeNoEMR = oldEntry.getBrowserConnection().getModifyModeNoEMR();
        ModifyOrder modifyAddDeleteOrder = oldEntry.getBrowserConnection().getModifyAddDeleteOrder();

        // get all attribute descriptions
        Set<String> attributeDescriptions = new LinkedHashSet<>();
        
        for ( IAttribute oldAttr : oldEntry.getAttributes() )
        {
            attributeDescriptions.add( oldAttr.getDescription() );
        }
        
        for ( IAttribute newAttr : newEntry.getAttributes() )
        {
            attributeDescriptions.add( newAttr.getDescription() );
        }

        // prepare the LDIF record containing the modifications
        LdifChangeModifyRecord record = new LdifChangeModifyRecord( LdifDnLine.create( newEntry.getDn().getName() ) );
        
        if ( newEntry.isReferral() )
        {
            record.addControl( LdifControlLine.create( StudioControl.MANAGEDSAIT_CONTROL.getOid(),
                StudioControl.MANAGEDSAIT_CONTROL.isCritical(), StudioControl.MANAGEDSAIT_CONTROL.getControlValue() ) );
        }
        record.setChangeType( LdifChangeTypeLine.createModify() );

        // check all the attributes
        for ( String attributeDescription : attributeDescriptions )
        {
            // get attribute type schema information
            Schema schema = oldEntry.getBrowserConnection().getSchema();
            AttributeType atd = schema.getAttributeTypeDescription( attributeDescription );
            boolean hasEMR = SchemaUtils.getEqualityMatchingRuleNameOrNumericOidTransitive( atd, schema ) != null;
            boolean isReplaceForced = ( hasEMR && modifyMode == ModifyMode.REPLACE )
                || ( !hasEMR && modifyModeNoEMR == ModifyMode.REPLACE );
            boolean isAddDelForced = ( hasEMR && modifyMode == ModifyMode.ADD_DELETE )
                || ( !hasEMR && modifyModeNoEMR == ModifyMode.ADD_DELETE );
            boolean isOrderedValue = atd.getExtensions().containsKey( "X-ORDERED" ) //$NON-NLS-1$
                && atd.getExtensions().get( "X-ORDERED" ).contains( "VALUES" ); //$NON-NLS-1$ //$NON-NLS-2$

            // get old an new values for comparison
            IAttribute oldAttribute = oldEntry.getAttribute( attributeDescription );
            Set<String> oldValues = new HashSet<>();
            Map<String, LdifAttrValLine> oldAttrValLines = new LinkedHashMap<>();
            
            if ( oldAttribute != null )
            {
                for ( IValue value : oldAttribute.getValues() )
                {
                    LdifAttrValLine attrValLine = computeDiffCreateAttrValLine( value );
                    oldValues.add( attrValLine.getUnfoldedValue() );
                    oldAttrValLines.put( attrValLine.getUnfoldedValue(), attrValLine );
                }
            }
            
            IAttribute newAttribute = newEntry.getAttribute( attributeDescription );
            Set<String> newValues = new HashSet<>();
            Map<String, LdifAttrValLine> newAttrValLines = new LinkedHashMap<>();
            
            if ( newAttribute != null )
            {
                for ( IValue value : newAttribute.getValues() )
                {
                    LdifAttrValLine attrValLine = computeDiffCreateAttrValLine( value );
                    newValues.add( attrValLine.getUnfoldedValue() );
                    newAttrValLines.put( attrValLine.getUnfoldedValue(), attrValLine );
                }
            }

            // check what to do
            if ( oldAttribute != null && newAttribute == null )
            {
                // attribute only exists in the old entry: delete all values
                LdifModSpec modSpec;
                
                if ( isReplaceForced )
                {
                    // replace (empty value list)
                    modSpec = LdifModSpec.createReplace( attributeDescription );
                }
                else
                // addDelForced or default
                {
                    // delete all
                    modSpec = LdifModSpec.createDelete( attributeDescription );
                    for ( IValue value : oldAttribute.getValues() )
                    {
                        modSpec.addAttrVal( computeDiffCreateAttrValLine( value ) );
                    }
                }
                
                modSpec.finish( LdifModSpecSepLine.create() );
                record.addModSpec( modSpec );
            }
            else if ( oldAttribute == null && newAttribute != null )
            {
                // attribute only exists in the new entry: add all values
                LdifModSpec modSpec;
                
                if ( isReplaceForced )
                {
                    // replace (all values)
                    modSpec = LdifModSpec.createReplace( attributeDescription );
                }
                else
                // addDelForced or default 
                {
                    // add (all new values)
                    modSpec = LdifModSpec.createAdd( attributeDescription );
                }
                
                for ( IValue value : newAttribute.getValues() )
                {
                    modSpec.addAttrVal( computeDiffCreateAttrValLine( value ) );
                }
                
                modSpec.finish( LdifModSpecSepLine.create() );
                record.addModSpec( modSpec );
            }
            else if ( oldAttribute != null && newAttribute != null && !oldValues.equals( newValues ) )
            {
                // attribute exists in both entries, check modifications
                if ( isReplaceForced )
                {
                    // replace (all new values)
                    LdifModSpec modSpec = LdifModSpec.createReplace( attributeDescription );
                    
                    for ( IValue value : newAttribute.getValues() )
                    {
                        modSpec.addAttrVal( computeDiffCreateAttrValLine( value ) );
                    }
                    
                    modSpec.finish( LdifModSpecSepLine.create() );
                    record.addModSpec( modSpec );
                }
                else
                {
                    // compute diff
                    List<LdifAttrValLine> toDel = new ArrayList<>();
                    List<LdifAttrValLine> toAdd = new ArrayList<>();

                    for ( Map.Entry<String, LdifAttrValLine> entry : oldAttrValLines.entrySet() )
                    {
                        if ( !newValues.contains( entry.getKey() ) )
                        {
                            toDel.add( entry.getValue() );
                        }
                    }
                    
                    for ( Map.Entry<String, LdifAttrValLine> entry : newAttrValLines.entrySet() )
                    {
                        if ( !oldValues.contains( entry.getKey() ) )
                        {
                            toAdd.add( entry.getValue() );
                        }
                    }

                    /*
                     *  we use add/del in the following cases:
                     *  - add/del is forced in the connection configuration
                     *  - for attributes w/o X-ORDERED 'VALUES'
                     *  
                     *  we use replace in the following cases:
                     *  - for attributes with X-ORDERED 'VALUES'
                     */
                    if ( isAddDelForced || !isOrderedValue )
                    {
                        // add/del del/add
                        LdifModSpec addModSpec = LdifModSpec.createAdd( attributeDescription );
                        
                        for ( LdifAttrValLine attrValLine : toAdd )
                        {
                            addModSpec.addAttrVal( attrValLine );
                        }
                        
                        addModSpec.finish( LdifModSpecSepLine.create() );
                        LdifModSpec delModSpec = LdifModSpec.createDelete( attributeDescription );
                        
                        for ( LdifAttrValLine attrValLine : toDel )
                        {
                            delModSpec.addAttrVal( attrValLine );
                        }
                        
                        delModSpec.finish( LdifModSpecSepLine.create() );

                        if ( modifyAddDeleteOrder == ModifyOrder.DELETE_FIRST )
                        {
                            if ( delModSpec.getAttrVals().length > 0 )
                            {
                                record.addModSpec( delModSpec );
                            }
                            
                            if ( addModSpec.getAttrVals().length > 0 )
                            {
                                record.addModSpec( addModSpec );
                            }
                        }
                        else
                        {
                            if ( addModSpec.getAttrVals().length > 0 )
                            {
                                record.addModSpec( addModSpec );
                            }
                            
                            if ( delModSpec.getAttrVals().length > 0 )
                            {
                                record.addModSpec( delModSpec );
                            }
                        }
                    }
                    else
                    {
                        // replace (all new values)
                        LdifModSpec modSpec = LdifModSpec.createReplace( attributeDescription );
                        
                        for ( LdifAttrValLine attrValLine : newAttrValLines.values() )
                        {
                            modSpec.addAttrVal( attrValLine );
                        }
                        
                        modSpec.finish( LdifModSpecSepLine.create() );
                        record.addModSpec( modSpec );
                    }
                }
            }

        }

        record.finish( LdifSepLine.create() );

        LdifFile model = new LdifFile();
        
        if ( record.isValid() && record.getModSpecs().length > 0 )
        {
            model.addContainer( record );
        }
        
        return model.getRecords().length > 0 ? model : null;
    }


    // ── R2-D2 Creates An LDIF Attribute-Value Line For One Entry Value ────────────
    // Chooses binary or string encoding based on the attribute's isBinary flag.
    // Private helper used within computeDiff to build the mod spec value lines.
    private static LdifAttrValLine computeDiffCreateAttrValLine( IValue value )
    {
        IAttribute attribute = value.getAttribute();
        
        if ( attribute.isBinary() )
        {
            return LdifAttrValLine.create( attribute.getDescription(), value.getBinaryValue() );
        }
        else
        {
            return LdifAttrValLine.create( attribute.getDescription(), value.getStringValue() );
        }
    }


    // ── C-3PO Decodes RFC 4517 Postal Address Syntax Into Readable Lines ─────────
    // C-3PO replaces the "$" delimiter with the caller's separator (e.g. newline),
    // un-escapes "\24" back to "$", and un-escapes "\5C" (or "\5c") back to "\".
    // The resulting translator can be applied to a PostalAddress attribute value.
    // Used by the postal address value editor to display multi-line addresses.
    /**
     * Creates a {@link CharSequenceTranslator} for decoding the RFC 4517 Postal
     * Address syntax.  Replaces {@code $} with the given separator, and
     * un-escapes {@code \24} and {@code \5C}.
     *
     * @param separator the string to insert between address lines
     * @return a translator for decoding postal address values
     */
    public static CharSequenceTranslator createPostalAddressDecoder( String separator )
    {
        return new LookupTranslator( Map.of(
            "$", separator, //$NON-NLS-1$
            "\\24", "$", //$NON-NLS-1$ //$NON-NLS-2$
            "\\5C", "\\", //$NON-NLS-1$ //$NON-NLS-2$
            "\\5c", "\\" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── C-3PO Encodes Display Lines Into RFC 4517 Postal Address Syntax ──────────
    // C-3PO performs the inverse of createPostalAddressDecoder:
    // escapes "\" as "\5C", escapes "$" as "\24", and replaces the separator
    // (e.g. newline) with the single "$" delimiter character.
    // Used by the postal address value editor when saving back to the directory.
    /**
     * Creates a {@link CharSequenceTranslator} for encoding the RFC 4517 Postal
     * Address syntax.  Escapes {@code \} as {@code \5C}, {@code $} as {@code \24},
     * and replaces the given separator with {@code $}.
     *
     * @param separator the string separating address lines in the input
     * @return a translator for encoding postal address values
     */
    public static CharSequenceTranslator createPostalAddressEncoder( String separator )
    {
        return new LookupTranslator( Map.of(
            "\\", "\\5C", //$NON-NLS-1$ //$NON-NLS-2$
            "$", "\\24", //$NON-NLS-1$ //$NON-NLS-2$
            separator, "$" ) ); //$NON-NLS-1$
    }
}
