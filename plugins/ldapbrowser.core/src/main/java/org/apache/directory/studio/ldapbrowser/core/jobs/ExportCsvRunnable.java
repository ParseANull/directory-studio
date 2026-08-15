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

package org.apache.directory.studio.ldapbrowser.core.jobs;


import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeDescription;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.SearchParameter;
import org.apache.directory.studio.ldapbrowser.core.utils.JNDIUtils;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.apache.directory.studio.ldifparser.LdifUtils;
import org.apache.directory.studio.ldifparser.model.LdifEnumeration;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifContentRecord;
import org.apache.directory.studio.ldifparser.model.lines.LdifAttrValLine;
import org.eclipse.core.runtime.Preferences;


// ── CLASS: ExportCsvRunnable — CLONE TROOPER FILING THE EMPIRE'S CSV DOSSIERS ─
// Order 66 has been received: marshal all matching LDAP entries from the galaxy
// and write them to a CSV file, one row per trooper (entry), one column per
// attribute.  Multi-valued attributes are joined with the configured delimiter;
// binary values are base-64 or hex-encoded so the dossiers remain readable.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Runnable to export directory content to a CSV file.
 *
 * <p>Think of this as a clone trooper executing Order 66 to collect every
 * matching entry from the LDAP directory and file it in a comma-separated
 * dossier — row by row, attribute by attribute, until the mission is complete
 * or the progress monitor signals a cancellation.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportCsvRunnable implements StudioConnectionRunnableWithProgress
{
    /** The filename of the CSV file. */
    private String exportCsvFilename;

    /** The browser connection. */
    private IBrowserConnection browserConnection;

    /** The search parameter. */
    private SearchParameter searchParameter;

    /** The export dn flag. */
    private boolean exportDn;


    // ── Clone Trooper Receives Mission Orders For The CSV Export ─────────────────
    // The trooper is briefed with the target filename, the LDAP connection,
    // the search parameters defining which entries to collect, and whether the
    // distinguished name column should appear in the final report.
    // All mission parameters are stored for execution in run().
    /**
     * Creates a new instance of ExportCsvRunnable.
     *
     * @param exportCsvFilename the filename of the csv file
     * @param browserConnection the browser connection
     * @param searchParameter the search parameter
     * @param exportDn true to export the Dn
     */
    public ExportCsvRunnable( String exportCsvFilename, IBrowserConnection browserConnection,
        SearchParameter searchParameter, boolean exportDn )
    {
        this.exportCsvFilename = exportCsvFilename;
        this.browserConnection = browserConnection;
        this.searchParameter = searchParameter;
        this.exportDn = exportDn;
    }


    // ── Clone Trooper Reports Which LDAP Connection This Mission Uses ─────────────
    // The Empire needs to know which Star Destroyer (connection) this trooper
    // will board before the mission begins, so the connection is returned here.
    /**
     * {@inheritDoc}
     */
    public Connection[] getConnections()
    {
        return new Connection[]
            { browserConnection.getConnection() };
    }


    // ── Clone Trooper Reports The Human-Readable Mission Name ─────────────────────
    // Returns the localised job name shown in the Eclipse progress dialog.
    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return BrowserCoreMessages.jobs__export_csv_name;
    }


    // ── Clone Trooper Locks The Target File To Prevent Concurrent Missions ────────
    // A SHA digest of the filename combined with the connection URL creates a
    // unique lock token — two troopers cannot write the same CSV simultaneously.
    /**
     * {@inheritDoc}
     */
    public Object[] getLockedObjects()
    {
        return new Object[]
            { browserConnection.getUrl() + "_" + DigestUtils.shaHex( exportCsvFilename ) }; //$NON-NLS-1$
    }


    // ── Clone Trooper Returns The Error Message If The Mission Fails ──────────────
    // Han shoots first: when something goes wrong the trooper reports a
    // localised error message so the UI can display it to the operator.
    /**
     * {@inheritDoc}
     */
    public String getErrorMessage()
    {
        return BrowserCoreMessages.jobs__export_cvs_error;
    }


    // ── Clone Trooper Executes Order 66: Write All Matching Entries To CSV ────────
    // Opens the target file, writes a header row of attribute names, then streams
    // every matching LDAP entry through exportToCsv() row by row.
    // On completion or error the file streams are closed and errors reported.
    /**
     * {@inheritDoc}
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask( BrowserCoreMessages.jobs__export_csv_task, 2 );
        monitor.reportProgress( " " ); //$NON-NLS-1$
        monitor.worked( 1 );

        Preferences coreStore = BrowserCorePlugin.getDefault().getPluginPreferences();

        String attributeDelimiter = coreStore.getString( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_ATTRIBUTEDELIMITER );
        String valueDelimiter = coreStore.getString( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_VALUEDELIMITER );
        String quoteCharacter = coreStore.getString( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_QUOTECHARACTER );
        String lineSeparator = coreStore.getString( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_LINESEPARATOR );
        String encoding = coreStore.getString( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_ENCODING );
        int binaryEncoding = coreStore.getInt( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_BINARYENCODING );
        String[] exportAttributes = this.searchParameter.getReturningAttributes();

        try
        {
            // open file
            FileOutputStream fos = new FileOutputStream( exportCsvFilename );
            OutputStreamWriter osw = new OutputStreamWriter( fos, encoding );
            BufferedWriter bufferedWriter = new BufferedWriter( osw );

            // header
            if ( this.exportDn )
            {
                bufferedWriter.write( "dn" ); //$NON-NLS-1$
                if ( exportAttributes == null || exportAttributes.length > 0 )
                    bufferedWriter.write( attributeDelimiter );
            }
            for ( int i = 0; i < exportAttributes.length; i++ )
            {
                bufferedWriter.write( exportAttributes[i] );
                if ( i + 1 < exportAttributes.length )
                    bufferedWriter.write( attributeDelimiter );
            }
            bufferedWriter.write( BrowserCoreConstants.LINE_SEPARATOR );

            // export
            int count = 0;
            exportToCsv( browserConnection, searchParameter, bufferedWriter, count, monitor, exportAttributes,
                attributeDelimiter, valueDelimiter, quoteCharacter, lineSeparator, encoding, binaryEncoding, exportDn );

            // close file
            bufferedWriter.close();
            osw.close();
            fos.close();

        }
        catch ( Exception e )
        {
            monitor.reportError( e );
        }
    }


    // ── Clone Trooper Streams All Search Results Into The CSV Writer ─────────────
    // Uses ExportLdifRunnable.search() to obtain an LDAP result enumeration,
    // then transforms each LdifContentRecord into a CSV row via recordToCsv().
    // Tolerated LDAP size-limit codes (3, 4, 11) are swallowed; others propagate.
    // Progress is reported after each row so the UI stays responsive.
    /**
     * Exports to CSV.
     *
     * @param browserConnection the browser connection
     * @param searchParameter the search parameter
     * @param bufferedWriter the buffered writer
     * @param count the count
     * @param monitor the monitor
     * @param attributes the attributes
     * @param attributeDelimiter the attribute delimiter
     * @param valueDelimiter the value delimiter
     * @param quoteCharacter the quote character
     * @param lineSeparator the line separator
     * @param encoding the encoding
     * @param binaryEncoding the binary encoding
     * @param exportDn the export dn
     * @throws IOException if the writer raises an I/O error
     */
    private static void exportToCsv( IBrowserConnection browserConnection, SearchParameter searchParameter,
        BufferedWriter bufferedWriter, int count, StudioProgressMonitor monitor, String[] attributes,
        String attributeDelimiter, String valueDelimiter, String quoteCharacter, String lineSeparator, String encoding,
        int binaryEncoding, boolean exportDn ) throws IOException
    {
        try
        {
            LdifEnumeration enumeration = ExportLdifRunnable.search( browserConnection, searchParameter, monitor );
            while ( !monitor.isCanceled() && !monitor.errorsReported() && enumeration.hasNext() )
            {
                LdifContainer container = enumeration.next();

                if ( container instanceof LdifContentRecord )
                {

                    LdifContentRecord record = ( LdifContentRecord ) container;
                    bufferedWriter.write( recordToCsv( browserConnection, record, attributes, attributeDelimiter,
                        valueDelimiter, quoteCharacter, lineSeparator, encoding, binaryEncoding, exportDn ) );

                    count++;
                    monitor.reportProgress( BrowserCoreMessages.bind( BrowserCoreMessages.jobs__export_progress,
                        new String[]
                            { Integer.toString( count ) } ) );
                }
            }
        }
        catch ( LdapException ce )
        {
            int ldapStatusCode = JNDIUtils.getLdapStatusCode( ce );
            if ( ldapStatusCode == 3 || ldapStatusCode == 4 || ldapStatusCode == 11 )
            {
                // nothing
            }
            else
            {
                monitor.reportError( ce );
            }
        }
    }


    // ── Clone Trooper Formats One LDIF Record As A Single CSV Row ────────────────
    // Builds an attribute map (OID → joined values) from the record, then outputs
    // each requested attribute column in order, quoting every value.
    // Postal address values are decoded from their $-delimited format.
    // The row is terminated with the configured line separator, not the system one.
    /**
     * Transforms an LDIF record to CSV.
     *
     * @param browserConnection the browser connection
     * @param record the record
     * @param attributes the attributes
     * @param attributeDelimiter the attribute delimiter
     * @param valueDelimiter the value delimiter
     * @param quoteCharacter the quote character
     * @param lineSeparator the line separator
     * @param encoding the encoding
     * @param binaryEncoding the binary encoding
     * @param exportDn the export dn
     * @return the formatted CSV row string
     */
    private static String recordToCsv( IBrowserConnection browserConnection, LdifContentRecord record,
        String[] attributes, String attributeDelimiter, String valueDelimiter, String quoteCharacter,
        String lineSeparator, String encoding, int binaryEncoding, boolean exportDn )
    {
        CharSequenceTranslator decoder = Utils.createPostalAddressDecoder( lineSeparator );

        // group multi-valued attributes
        Map<String, String> attributeMap = getAttributeMap( browserConnection, record, valueDelimiter, encoding,
            binaryEncoding );

        // print attributes
        StringBuffer sb = new StringBuffer();
        if ( exportDn )
        {
            String value = record.getDnLine().getValueAsString();
            appendValue( quoteCharacter, sb, value );

            if ( attributes == null || attributes.length > 0 )
                sb.append( attributeDelimiter );
        }
        for ( int i = 0; i < attributes.length; i++ )
        {

            String attributeName = attributes[i];
            AttributeDescription ad = new AttributeDescription( attributeName );
            String oidString = ad.toOidString( browserConnection.getSchema() );
            if ( attributeMap.containsKey( oidString ) )
            {
                String value = attributeMap.get( oidString );
                AttributeType type = browserConnection.getSchema().getAttributeTypeDescription( attributeName );
                if ( SchemaConstants.POSTAL_ADDRESS_SYNTAX.equals( type.getSyntaxOid() ) )
                {
                    value = decoder.translate( value );
                }
                appendValue( quoteCharacter, sb, value );
            }

            // delimiter
            if ( i + 1 < attributes.length )
            {
                sb.append( attributeDelimiter );
            }

        }
        sb.append( lineSeparator );

        return sb.toString();
    }


    // ── Clone Trooper Safely Quotes A Cell Value For The CSV File ────────────────
    // Any embedded quote characters are doubled so CSV parsers interpret them
    // as literal quotes.  Values starting with "=" are prefixed with a single
    // quote to prevent spreadsheet applications from evaluating them as formulas.
    // The value is always wrapped in the configured quote character.
    private static void appendValue( String quoteCharacter, StringBuffer sb, String value )
    {
        // escape quote character
        value = value.replaceAll( quoteCharacter, quoteCharacter + quoteCharacter );

        // prefix values starting with '=' with a single quote to avoid interpretation as formula
        if ( value.startsWith( "=" ) )
        {
            value = "'" + value;
        }

        // always quote
        sb.append( quoteCharacter );
        sb.append( value );
        sb.append( quoteCharacter );
    }


    // ── Clone Trooper Collapses All Attribute Values Into One Map ────────────────
    // Lando runs Cloud City: each attribute OID maps to all its values joined by
    // the value delimiter.  Attribute names are normalised to OID strings so that
    // aliases resolve to the same map key.  Binary values that can't be encoded
    // in the target charset fall back to base-64, hex, or "(BINARY)" placeholder.
    /**
     * Gets the attribute map (OID &rarr; joined values) from an LDIF content record.
     *
     * @param browserConnection the browser connection
     * @param record the record
     * @param valueDelimiter the value delimiter
     * @param encoding the encoding
     * @param binaryEncoding the binary encoding
     * @return the attribute map
     */
    static Map<String, String> getAttributeMap( IBrowserConnection browserConnection, LdifContentRecord record,
        String valueDelimiter, String encoding, int binaryEncoding )
    {
        Map<String, String> attributeMap = new HashMap<String, String>();
        LdifAttrValLine[] lines = record.getAttrVals();
        for ( int i = 0; i < lines.length; i++ )
        {
            String attributeName = lines[i].getUnfoldedAttributeDescription();
            if ( browserConnection != null )
            {
                // convert attributeName to oid
                AttributeDescription ad = new AttributeDescription( attributeName );
                attributeName = ad.toOidString( browserConnection.getSchema() );
            }
            String value = lines[i].getValueAsString();
            if ( !Charset.forName( encoding ).newEncoder().canEncode( value ) )
            {
                if ( binaryEncoding == BrowserCoreConstants.BINARYENCODING_BASE64 )
                {
                    value = LdifUtils.base64encode( lines[i].getValueAsBinary() );
                }
                else if ( binaryEncoding == BrowserCoreConstants.BINARYENCODING_HEX )
                {
                    value = LdifUtils.hexEncode( lines[i].getValueAsBinary() );
                }
                else
                {
                    value = BrowserCoreConstants.BINARY;
                }

                if ( attributeMap.containsKey( attributeName ) )
                {
                    String oldValue = ( String ) attributeMap.get( attributeName );
                    attributeMap.put( attributeName, oldValue + valueDelimiter + value );
                }
                else
                {
                    attributeMap.put( attributeName, value );
                }
            }
            else
            {
                if ( attributeMap.containsKey( attributeName ) )
                {
                    String oldValue = ( String ) attributeMap.get( attributeName );
                    attributeMap.put( attributeName, oldValue + valueDelimiter + value );
                }
                else
                {
                    attributeMap.put( attributeName, value );
                }
            }
        }
        return attributeMap;
    }
}
