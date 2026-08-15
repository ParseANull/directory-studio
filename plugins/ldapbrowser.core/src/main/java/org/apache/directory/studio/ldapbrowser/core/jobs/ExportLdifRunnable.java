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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.controls.PagedResults;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.io.api.StudioSearchResultEnumeration;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.SearchParameter;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DummyEntry;
import org.apache.directory.studio.ldapbrowser.core.utils.AttributeComparator;
import org.apache.directory.studio.ldapbrowser.core.utils.JNDIUtils;
import org.apache.directory.studio.ldapbrowser.core.utils.ModelConverter;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.apache.directory.studio.ldifparser.LdifFormatParameters;
import org.apache.directory.studio.ldifparser.model.LdifEnumeration;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifContentRecord;
import org.apache.directory.studio.ldifparser.model.lines.LdifAttrValLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifDnLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifSepLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifVersionLine;


// ── CLASS: ExportLdifRunnable — CLONE TROOPER WRITING LDIF INTELLIGENCE REPORTS
// Order 66: march through every matching LDAP entry and serialise each one as
// a standards-compliant LDIF content record.  Values are attribute-sorted so
// the output is deterministic; an optional version header line is prepended.
// Pagination (LDAP Paged Results control) is handled transparently.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Runnable to export directory content to an LDIF file.
 *
 * <p>Think of this as a clone trooper executing Order 66 to collect every
 * matching entry and write it to a standard LDIF intelligence report —
 * attributes sorted, values base-64 encoded where needed, pagination
 * followed automatically until the last page.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportLdifRunnable implements StudioConnectionRunnableWithProgress
{
    /** The filename of the LDIF file. */
    private String exportLdifFilename;

    /** The browser connection. */
    private IBrowserConnection browserConnection;

    /** The search parameter. */
    private SearchParameter searchParameter;


    // ── Clone Trooper Receives LDIF Mission Orders ────────────────────────────────
    // Stores the target LDIF filename, browser connection, and search parameters
    // that define which entries to collect during Order 66 execution.
    /**
     * Creates a new instance of ExportLdifRunnable.
     *
     * @param exportLdifFilename the filename of the LDIF file
     * @param browserConnection the browser connection
     * @param searchParameter the search parameter
     */
    public ExportLdifRunnable( String exportLdifFilename, IBrowserConnection browserConnection,
        SearchParameter searchParameter )
    {
        this.exportLdifFilename = exportLdifFilename;
        this.browserConnection = browserConnection;
        this.searchParameter = searchParameter;
    }


    // ── Clone Trooper Reports The LDAP Connection This Mission Uses ───────────────
    // Returns the single connection required by the job scheduler.
    /**
     * {@inheritDoc}
     */
    public Connection[] getConnections()
    {
        return new Connection[]
            { browserConnection.getConnection() };
    }


    // ── Clone Trooper Reports The Human-Readable LDIF Mission Name ────────────────
    // Returns the localised job name shown in the Eclipse progress dialog.
    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return BrowserCoreMessages.jobs__export_ldif_name;
    }


    // ── Clone Trooper Locks The Target LDIF File Against Concurrent Missions ─────
    // A SHA hash of the filename combined with the connection URL forms the lock.
    /**
     * {@inheritDoc}
     */
    public Object[] getLockedObjects()
    {
        List<Object> l = new ArrayList<Object>();
        l.add( browserConnection.getUrl() + "_" + DigestUtils.shaHex( exportLdifFilename ) ); //$NON-NLS-1$
        return l.toArray();
    }


    // ── Clone Trooper Returns The Error Message If The LDIF Mission Fails ────────
    // Han shoots first: if the mission fails, return a localised error message.
    /**
     * {@inheritDoc}
     */
    public String getErrorMessage()
    {
        return BrowserCoreMessages.jobs__export_ldif_error;
    }


    // ── Clone Trooper Executes Order 66: Open File And Export All Entries ────────
    // Opens the LDIF file for writing, calls export() to stream matching entries,
    // then closes all file streams.  Errors are forwarded to the progress monitor.
    /**
     * {@inheritDoc}
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask( BrowserCoreMessages.jobs__export_ldif_task, 2 );
        monitor.reportProgress( " " ); //$NON-NLS-1$
        monitor.worked( 1 );

        try
        {
            // open file
            FileWriter fileWriter = new FileWriter( exportLdifFilename );
            BufferedWriter bufferedWriter = new BufferedWriter( fileWriter );

            // export
            int count = 0;
            export( browserConnection, searchParameter, bufferedWriter, count, monitor );

            // close file
            bufferedWriter.close();
            fileWriter.close();

        }
        catch ( Exception e )
        {
            monitor.reportError( e );
        }
    }


    // ── Clone Trooper Streams Each LDAP Entry Into The LDIF File ─────────────────
    // Optionally writes a "version: 1" header, then iterates the LdifEnumeration,
    // converting each LdifContentRecord to a sorted DummyEntry and back to LDIF.
    // Size-limit LDAP codes are tolerated; other LDAP errors propagate.
    // Progress is reported after each record so the UI stays responsive.
    private static void export( IBrowserConnection browserConnection, SearchParameter searchParameter,
        BufferedWriter bufferedWriter, int count, StudioProgressMonitor monitor ) throws IOException
    {
        try
        {
            LdifEnumeration enumeration = search( browserConnection, searchParameter, monitor );
            LdifFormatParameters ldifFormatParameters = Utils.getLdifFormatParameters();

            // add version spec
            if ( BrowserCorePlugin.getDefault().getPluginPreferences()
                .getBoolean( BrowserCoreConstants.PREFERENCE_LDIF_INCLUDE_VERSION_LINE ) )
            {
                LdifVersionLine ldifVersionLine = LdifVersionLine.create();
                String ldifVersionLineString = ldifVersionLine.toFormattedString( ldifFormatParameters );
                bufferedWriter.write( ldifVersionLineString );
                LdifSepLine ldifSepLine = LdifSepLine.create();
                String ldifSepLineString = ldifSepLine.toFormattedString( ldifFormatParameters );
                bufferedWriter.write( ldifSepLineString );
            }

            // add the records
            while ( !monitor.isCanceled() && !monitor.errorsReported() && enumeration.hasNext() )
            {
                LdifContainer container = enumeration.next();

                if ( container instanceof LdifContentRecord )
                {
                    LdifContentRecord record = ( LdifContentRecord ) container;
                    LdifDnLine dnLine = record.getDnLine();
                    LdifSepLine sepLine = record.getSepLine();

                    // sort and format
                    DummyEntry entry = ModelConverter.ldifContentRecordToEntry( record, browserConnection );
                    List<IValue> sortedValues = AttributeComparator.toSortedValues( entry );
                    LdifContentRecord newRecord = new LdifContentRecord( dnLine );
                    for ( IValue value : sortedValues )
                    {
                        newRecord.addAttrVal( ModelConverter.valueToLdifAttrValLine( value ) );
                    }
                    newRecord.finish( sepLine );
                    String s = newRecord.toFormattedString( ldifFormatParameters );

                    // String s = record.toFormattedString();
                    bufferedWriter.write( s );

                    count++;
                    monitor.reportProgress( BrowserCoreMessages.bind( BrowserCoreMessages.jobs__export_progress,
                        new String[]
                            { Integer.toString( count ) } ) );
                }
            }
        }
        catch ( LdapException loe )
        {
            int ldapStatusCode = JNDIUtils.getLdapStatusCode( loe );
            if ( ldapStatusCode == 3 || ldapStatusCode == 4 || ldapStatusCode == 11 )
            {
                // ignore
            }
            else
            {
                monitor.reportError( loe );
            }
        }
    }


    // ── Clone Trooper Launches The LDAP Search And Returns A LdifEnumeration ─────
    // Delegates to SearchRunnable.search() and wraps the raw
    // StudioSearchResultEnumeration in a DefaultLdifEnumeration that handles
    // paged-results continuations automatically.
    static LdifEnumeration search( IBrowserConnection browserConnection, SearchParameter parameter,
        StudioProgressMonitor monitor )
    {
        StudioSearchResultEnumeration result = SearchRunnable.search( browserConnection, parameter, monitor );
        return new DefaultLdifEnumeration( result, browserConnection, parameter, monitor );
    }

    static class DefaultLdifEnumeration implements LdifEnumeration
    {

        private StudioSearchResultEnumeration enumeration;

        private IBrowserConnection browserConnection;

        private SearchParameter parameter;

        private StudioProgressMonitor monitor;


        public DefaultLdifEnumeration( StudioSearchResultEnumeration enumeration, IBrowserConnection browserConnection,
            SearchParameter parameter, StudioProgressMonitor monitor )
        {
            this.enumeration = enumeration;
            this.browserConnection = browserConnection;
            this.parameter = parameter;
            this.monitor = monitor;
        }


        public boolean hasNext() throws LdapException
        {
            if ( enumeration != null )
            {
                if ( enumeration.hasMore() )
                {
                    return true;
                }

                for ( Control responseControl : enumeration.getResponseControls() )
                {
                    if ( responseControl instanceof PagedResults )
                    {
                        PagedResults prc = ( PagedResults ) responseControl;
                        if ( ArrayUtils.isNotEmpty( prc.getCookie() ) )
                        {
                            // search again: pass the response control cookie to the request control
                            byte[] cookie = prc.getCookie();
                            for ( Control requestControl : parameter.getControls() )
                            {
                                if ( requestControl instanceof PagedResults )
                                {
                                    ( ( PagedResults ) requestControl ).setCookie( cookie );
                                }
                            }
                            enumeration = SearchRunnable.search( browserConnection, parameter, monitor );
                            return enumeration != null && enumeration.hasMore();
                        }
                    }
                }
            }

            return false;
        }


        public LdifContainer next() throws LdapException
        {
            Entry entry = enumeration.next().getEntry();
            Dn dn = entry.getDn();
            LdifContentRecord record = LdifContentRecord.create( dn.getName() );

            for ( Attribute attribute : entry )
            {
                String attributeName = attribute.getUpId();
                for ( Value value : attribute )
                {
                    if ( value.isHumanReadable() )
                    {
                        record.addAttrVal( LdifAttrValLine.create( attributeName, value.getString() ) );
                    }
                    else
                    {
                        record.addAttrVal( LdifAttrValLine.create( attributeName, value.getBytes() ) );
                    }
                }
            }

            record.finish( LdifSepLine.create() );

            return record;
        }

    }
}
