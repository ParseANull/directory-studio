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
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.directory.api.dsmlv2.DsmlDecorator;
import org.apache.directory.api.dsmlv2.request.AddRequestDsml;
import org.apache.directory.api.dsmlv2.request.BatchRequestDsml;
import org.apache.directory.api.dsmlv2.response.BatchResponseDsml;
import org.apache.directory.api.dsmlv2.response.SearchResponseDsml;
import org.apache.directory.api.dsmlv2.response.SearchResultDoneDsml;
import org.apache.directory.api.dsmlv2.response.SearchResultEntryDsml;
import org.apache.directory.api.dsmlv2.response.SearchResultReferenceDsml;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.codec.api.LdapApiServiceFactory;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapURLEncodingException;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.MessageTypeEnum;
import org.apache.directory.api.ldap.model.message.Response;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.SearchResultDone;
import org.apache.directory.api.ldap.model.message.SearchResultDoneImpl;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.io.api.StudioSearchResultEnumeration;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.SearchParameter;
import org.apache.directory.studio.ldapbrowser.core.utils.JNDIUtils;


// ── CLASS: ExportDsmlRunnable — CLONE TROOPER FILING XML DSML DISPATCH ORDERS ─
// Order 66: collect matching LDAP entries and encode them as DSML XML, either
// as a batch-response (search results) or a batch-request (add operations).
// Referral entries are identified and emitted as SearchResultReference elements
// rather than SearchResultEntry elements, keeping the XML semantically correct.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Runnable for exporting a part of an LDAP server into a DSML file.
 *
 * <p>Think of this as a clone trooper executing Order 66 to harvest directory
 * entries and encode them in XML DSML format — either as a search-response
 * batch or as an add-request batch, depending on the mission type.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportDsmlRunnable implements StudioConnectionRunnableWithProgress
{
    private static final String OBJECTCLASS_OBJECTCLASS_OID = "objectClass"; //$NON-NLS-1$
    private static final String OBJECTCLASS_OBJECTCLASS_NAME = "2.5.4.0"; //$NON-NLS-1$

    private static final String REFERRAL_OBJECTCLASS_OID = "2.16.840.1.113730.3.2.6"; //$NON-NLS-1$
    private static final String REFERRAL_OBJECTCLASS_NAME = "referral"; //$NON-NLS-1$

    private static final String REF_ATTRIBUTETYPE_OID = "2.16.840.1.113730.3.1.34"; //$NON-NLS-1$
    private static final String REF_ATTRIBUTETYPE_NAME = "ref"; //$NON-NLS-1$

    /** The name of the DSML file to export to */
    private String exportDsmlFilename;

    /** The connection to use */
    private IBrowserConnection browserConnection;

    /** The Search Parameter of the export*/
    private SearchParameter searchParameter;

    /** The type of the export */
    private ExportDsmlJobType type = ExportDsmlJobType.RESPONSE;

    /** 
     * The LDAP Codec - for now need by the DSML Parser 
     * @TODO - this should be removed - no reason why the DSML parser needs it
     * @TODO - hate to make it static like this but methods are static
     */
    private static LdapApiService codec = LdapApiServiceFactory.getSingleton();

    /**
     * This enum contains the two possible export types.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public enum ExportDsmlJobType
    {
        RESPONSE, REQUEST
    }


    // ── Clone Trooper Receives DSML Mission Orders And Ensures 'ref' Is Requested ─
    // Stores the target filename, connection, search parameters, and export type.
    // Automatically adds 'ref' attribute name and OID to the returning-attributes
    // list so referral entries can be detected and serialised correctly.
    // Both REQUEST and RESPONSE export types are supported.
    /**
     * Creates a new instance of ExportDsmlRunnable.
     *
     * @param exportDsmlFilename the name of the DSML file to export to
     * @param connection the connection to use
     * @param searchParameter the search parameter of the export
     * @param type the export type (RESPONSE or REQUEST)
     */
    public ExportDsmlRunnable( String exportDsmlFilename, IBrowserConnection connection,
        SearchParameter searchParameter, ExportDsmlJobType type )
    {
        this.exportDsmlFilename = exportDsmlFilename;
        this.browserConnection = connection;
        this.searchParameter = searchParameter;
        this.type = type;

        // Adding the name and OID of the 'ref' attribute to the list of returning attributes
        // for handling externals correctly
        List<String> returningAttributes = new ArrayList<>( Arrays.asList( searchParameter
            .getReturningAttributes() ) );
        returningAttributes.add( REF_ATTRIBUTETYPE_NAME );
        returningAttributes.add( REF_ATTRIBUTETYPE_OID );
        searchParameter.setReturningAttributes( returningAttributes.toArray( new String[0] ) );
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


    // ── Clone Trooper Reports The Human-Readable DSML Mission Name ────────────────
    // Returns the localised job name shown in the Eclipse progress dialog.
    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return BrowserCoreMessages.jobs__export_dsml_name;
    }


    // ── Clone Trooper Locks The Target DSML File Against Concurrent Missions ─────
    // A SHA hash of the filename combined with the connection URL forms the lock.
    /**
     * {@inheritDoc}
     */
    public Object[] getLockedObjects()
    {
        List<String> l = new ArrayList<>();
        l.add( browserConnection.getUrl() + "_" + DigestUtils.shaHex( exportDsmlFilename ) ); //$NON-NLS-1$
        return l.toArray();
    }


    // ── Clone Trooper Returns The Error Message If The DSML Mission Fails ────────
    // Han shoots first: if the mission fails, return a localised error message.
    /**
     * {@inheritDoc}
     */
    public String getErrorMessage()
    {
        return BrowserCoreMessages.jobs__export_dsml_error;
    }


    // ── Clone Trooper Executes Order 66: Search And Write DSML To Disk ───────────
    // Searches the directory via SearchRunnable.search(), processes results as
    // either a DSML response or request batch, then writes the XML string to file.
    // Uses a dummy monitor to separate search errors from file-write errors.
    /**
     * {@inheritDoc}
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask( BrowserCoreMessages.jobs__export_dsml_task, 4 );
        monitor.reportProgress( " " ); //$NON-NLS-1$
        monitor.worked( 1 );

        try
        {

            // Creating a dummy monitor that will be used to check if something
            // went wrong when executing the request
            StudioProgressMonitor dummyMonitor = new StudioProgressMonitor( monitor );

            // Searching for the requested entries
            StudioSearchResultEnumeration ne = SearchRunnable.search( browserConnection, searchParameter, dummyMonitor );
            monitor.worked( 1 );

            // Getting the DSML string associated to the search
            // and the type of answer the user is expecting
            String dsmlExportString = null;
            
            switch ( type )
            {
                case RESPONSE:
                    dsmlExportString = processAsDsmlResponse( ne, dummyMonitor );
                    break;
                case REQUEST:
                    dsmlExportString = processAsDsmlRequest( ne, dummyMonitor );
                    break;
            }
            
            monitor.worked( 1 );

            // Writing the DSML string to the final destination file.
            if ( dsmlExportString != null )
            {
                try ( FileOutputStream fos = new FileOutputStream( exportDsmlFilename ) )
                {
                    try ( OutputStreamWriter osw = new OutputStreamWriter( fos, "UTF-8" ) ) //$NON-NLS-1$
                    {
                        try ( BufferedWriter bufferedWriter = new BufferedWriter( osw ) )
                        {
                            bufferedWriter.write( dsmlExportString );
                        }
                    }
                }
            }
            monitor.worked( 1 );
        }
        catch ( Exception e )
        {
            monitor.reportError( e );
        }
    }


    // ── Clone Trooper Wraps Search Results In A DSML BatchResponse Envelope ──────
    // Creates a BatchResponseDsml, delegates to the public static overload,
    // then serialises the result to a DSML XML string.
    // This is the RESPONSE export type: what the server returned, in XML.
    /**
     * Processes the {@link StudioSearchResultEnumeration} as a DSML response.
     *
     * @param sre the search result enumeration
     * @param monitor the monitor
     * @return the associated DSML string
     * @throws LdapException if a DSML conversion error occurs
     */
    private String processAsDsmlResponse( StudioSearchResultEnumeration sre, StudioProgressMonitor monitor ) throws LdapException
    {
        // Creating the batch reponse
        BatchResponseDsml batchResponse = new BatchResponseDsml();

        processAsDsmlResponse( sre, batchResponse, monitor, searchParameter );

        // Returning the associated DSML
        return batchResponse.toDsml();
    }


    // ── Clone Trooper Populates An Existing BatchResponse With All Result Rows ───
    // Streams every search result through convertSearchResultToDsml() and adds
    // the DSML decorator to a SearchResponseDsml.  Size-limit LDAP codes are
    // tolerated; a SearchResultDone element is appended regardless of outcome.
    // Public and static so ImportDsmlRunnable can reuse it during round-trip tests.
    /**
     * Processes the {@link StudioSearchResultEnumeration} as a DSML response,
     * populating the given {@link BatchResponseDsml}.
     *
     * @param sre the search result enumeration
     * @param batchResponse the batch response to populate
     * @param monitor the monitor
     * @param searchParameter the search parameter
     * @throws LdapException if a DSML conversion error occurs
     */
    public static void processAsDsmlResponse( StudioSearchResultEnumeration sre, BatchResponseDsml batchResponse,
        StudioProgressMonitor monitor, SearchParameter searchParameter ) throws LdapException
    {
        // Creating and adding the search response
        SearchResponseDsml sr = new SearchResponseDsml( codec );
        batchResponse.addResponse( sr );

        try
        {
            int count = 0;

            if ( !monitor.errorsReported() )
            {
                // Creating and adding a search result entry or reference for each result
                while ( sre.hasMore() )
                {
                    Entry entry = sre.next().getEntry();
                    sr.addResponse( convertSearchResultToDsml( entry ) );

                    count++;
                    monitor.reportProgress( BrowserCoreMessages.bind( BrowserCoreMessages.jobs__export_progress,
                        new String[]
                            { Integer.toString( count ) } ) );
                }
            }
        }
        catch ( LdapException e )
        {
            int ldapStatusCode = JNDIUtils.getLdapStatusCode( e );
            if ( ldapStatusCode == 3 || ldapStatusCode == 4 || ldapStatusCode == 11 )
            {
                // ignore
            }
            else
            {
                monitor.reportError( e );
            }
        }

        // Creating and adding a search result done at the end of the results
        SearchResultDone srd = new SearchResultDoneImpl();
        LdapResult ldapResult = srd.getLdapResult();
        if ( !monitor.errorsReported() )
        {
            ldapResult.setResultCode( ResultCodeEnum.SUCCESS );
        }
        else
        {
            // Getting the exception
            Throwable t = monitor.getException();

            // Setting the result code
            ldapResult.setResultCode( ResultCodeEnum.getBestEstimate( t, MessageTypeEnum.SEARCH_REQUEST ) );

            // Setting the error message if there's one
            if ( t.getMessage() != null )
            {
                ldapResult.setDiagnosticMessage( t.getMessage() );
            }
        }
        sr.addResponse( new SearchResultDoneDsml( codec, srd ) );
    }


    // ── Clone Trooper Decides Whether An Entry Is A Referral Or A Regular Entry ──
    // Referral entries emit a SearchResultReferenceDsml with 'ref' URLs collected.
    // Regular entries emit a SearchResultEntryDsml with the full entry attached.
    // This is Han shooting first: decide type before writing the DSML element.
    /**
     * Converts the given entry to a {@link SearchResultEntryDsml} or
     * {@link SearchResultReferenceDsml} depending on whether it is a referral.
     *
     * @param entry the entry to convert
     * @return the associated DSML decorator
     * @throws LdapException if conversion fails
     */
    private static DsmlDecorator<? extends Response> convertSearchResultToDsml( Entry entry )
        throws LdapException
    {
        if ( isReferral( entry ) )
        {
            // The search result is a referral
            SearchResultReferenceDsml srr = new SearchResultReferenceDsml( codec );

            // Getting the 'ref' attribute
            Attribute refAttribute = entry.get( ExportDsmlRunnable.REF_ATTRIBUTETYPE_NAME );
            if ( refAttribute == null )
            {
                // If we did not get it by its name, let's get it by its OID
                refAttribute = entry.get( ExportDsmlRunnable.REF_ATTRIBUTETYPE_OID );
            }

            // Adding references
            if ( refAttribute != null )
            {
                for ( Value value : refAttribute )
                {
                    srr.addSearchResultReference( new LdapUrl( ( String ) value.getString() ) );
                }
            }

            return srr;
        }
        else
        {
            // The search result is NOT a referral
            SearchResultEntryDsml sre = new SearchResultEntryDsml( codec );
            sre.setEntry( entry );

            return sre;
        }
    }


    // ── Clone Trooper Checks An Entry's objectClass For The 'referral' Value ─────
    // Mace Windu confronts Palpatine: the check is done both by name and OID
    // to handle schemas that may return either form.  Returns true only if the
    // 'referral' objectClass is present, signalling that this is a reference.
    /**
     * Indicates if the given entry is a referral.
     *
     * @param entry the entry to inspect
     * @return {@code true} if the entry is a referral, {@code false} otherwise
     */
    private static boolean isReferral( Entry entry )
    {
        if ( entry != null )
        {
            // Getting the 'objectClass' Attribute
            Attribute objectClassAttribute = entry.get( ExportDsmlRunnable.OBJECTCLASS_OBJECTCLASS_NAME );
            if ( objectClassAttribute == null )
            {
                objectClassAttribute = entry.get( ExportDsmlRunnable.OBJECTCLASS_OBJECTCLASS_OID );
            }

            if ( objectClassAttribute != null )
            {
                // Checking if the 'objectClass' attribute contains the 
                // 'referral' object class as value
                return ( ( objectClassAttribute.contains( ExportDsmlRunnable.REFERRAL_OBJECTCLASS_NAME ) ) || ( objectClassAttribute
                    .contains( ExportDsmlRunnable.REFERRAL_OBJECTCLASS_OID ) ) );
            }
        }

        return false;
    }


    // ── Clone Trooper Converts Search Results Into DSML Add-Request Operations ───
    // Instead of a search response, each entry becomes an AddRequest DSML element
    // inside a BatchRequest — suitable for replaying entries on another server.
    // Size-limit LDAP codes are tolerated; the batch is serialised at the end.
    /**
     * Processes the {@link StudioSearchResultEnumeration} as a DSML request.
     *
     * @param sre the search result enumeration
     * @param monitor the monitor
     * @return the associated DSML string
     * @throws LdapException if conversion fails
     */
    private String processAsDsmlRequest( StudioSearchResultEnumeration sre, StudioProgressMonitor monitor )
        throws LdapException
    {
        // Creating the batch request
        BatchRequestDsml batchRequest = new BatchRequestDsml();

        try
        {
            int count = 0;

            if ( !monitor.errorsReported() )
            {
                // Creating and adding an add request for each result
                while ( sre.hasMore() )
                {
                    Entry entry = sre.next().getEntry();
                    AddRequestDsml arDsml = convertToAddRequestDsml( entry );
                    batchRequest.addRequest( arDsml );

                    count++;
                    monitor.reportProgress( BrowserCoreMessages.bind( BrowserCoreMessages.jobs__export_progress,
                        new String[]
                            { Integer.toString( count ) } ) );
                }
            }
        }
        catch ( LdapException e )
        {
            int ldapStatusCode = JNDIUtils.getLdapStatusCode( e );
            if ( ldapStatusCode == 3 || ldapStatusCode == 4 || ldapStatusCode == 11 )
            {
                // ignore
            }
            else
            {
                monitor.reportError( e );
            }
        }

        // Returning the associated DSML
        return batchRequest.toDsml();
    }


    // ── Clone Trooper Wraps One Entry As An AddRequestDsml Operation ─────────────
    // Creates a new AddRequestDsml, attaches the full entry, and returns it
    // ready to be added to the batch-request document.
    /**
     * Converts the given entry to an {@link AddRequestDsml}.
     *
     * @param entry the entry to convert
     * @return the associated {@link AddRequestDsml}
     * @throws LdapException if the entry cannot be attached
     */
    private AddRequestDsml convertToAddRequestDsml( Entry entry )
        throws LdapException
    {
        AddRequestDsml ar = new AddRequestDsml( codec );
        ar.setEntry( entry );

        return ar;
    }
}
