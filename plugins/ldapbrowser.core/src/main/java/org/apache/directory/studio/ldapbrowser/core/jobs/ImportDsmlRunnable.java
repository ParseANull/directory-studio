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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.directory.SearchControls;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.directory.api.dsmlv2.DsmlDecorator;
import org.apache.directory.api.dsmlv2.Dsmlv2Parser;
import org.apache.directory.api.dsmlv2.request.BatchRequestDsml;
import org.apache.directory.api.dsmlv2.request.Dsmlv2Grammar;
import org.apache.directory.api.dsmlv2.response.AddResponseDsml;
import org.apache.directory.api.dsmlv2.response.BatchResponseDsml;
import org.apache.directory.api.dsmlv2.response.BindResponseDsml;
import org.apache.directory.api.dsmlv2.response.CompareResponseDsml;
import org.apache.directory.api.dsmlv2.response.DelResponseDsml;
import org.apache.directory.api.dsmlv2.response.ExtendedResponseDsml;
import org.apache.directory.api.dsmlv2.response.ModDNResponseDsml;
import org.apache.directory.api.dsmlv2.response.ModifyResponseDsml;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.codec.api.LdapApiServiceFactory;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.exception.LdapURLEncodingException;
import org.apache.directory.api.ldap.model.message.AddRequest;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.CompareRequest;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.DeleteRequest;
import org.apache.directory.api.ldap.model.message.ExtendedRequest;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.MessageTypeEnum;
import org.apache.directory.api.ldap.model.message.ModifyDnRequest;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.Request;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.connection.core.Connection.ReferralHandlingMethod;
import org.apache.directory.studio.connection.core.io.LdapRuntimeException;
import org.apache.directory.studio.connection.core.io.api.StudioSearchResultEnumeration;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.events.BulkModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.SearchParameter;


// ── CLASS: ImportDsmlRunnable — CLONE TROOPER EXECUTING DSML ATTACK ORDERS ───
// The Empire transmits attack orders as a DSML XML batch request file.
// This runnable parses the file, dispatches each operation (add, delete,
// modify, modifyDN, search, bind, compare, extended) to the live LDAP server,
// and optionally writes a DSML batch-response file logging every result.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Runnable to import a DSML file into an LDAP server.
 *
 * <p>Think of this as a clone trooper executing Order 66 from a DSML XML
 * battle plan — each operation in the batch is dispatched to the live server,
 * the result captured in a DSML response batch, and any errors tallied so the
 * commanding officer (user) knows how many operations failed.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportDsmlRunnable implements StudioConnectionBulkRunnableWithProgress
{
    /** The connection to use */
    private IBrowserConnection browserConnection;

    /** The DSML file to use */
    private File dsmlFile;

    /** The Save file to use */
    private File responseFile;

    /** 
     * LDAP Codec used by DSML parser
     * @TODO by Alex - this should be removed completely
     */
    private LdapApiService codec = LdapApiServiceFactory.getSingleton();


    // ── Clone Trooper Receives Full DSML Import Orders With Response File ────────
    // Stores the LDAP connection, the DSML request file, and the optional
    // response file path.  When responseFile is non-null a DSML batch-response
    // is written after the mission so every operation result is logged.
    /**
     * Creates a new instance of ImportDsmlRunnable.
     *
     * @param connection the connection to use
     * @param dsmlFile the DSML file to read from
     * @param saveFile the file to write the DSML response to (may be {@code null})
     */
    public ImportDsmlRunnable( IBrowserConnection connection, File dsmlFile, File saveFile )
    {
        this.browserConnection = connection;
        this.dsmlFile = dsmlFile;
        this.responseFile = saveFile;
    }


    // ── Clone Trooper Receives DSML Import Orders Without A Response File ────────
    // Convenience overload that delegates to the three-argument constructor
    // with a null response file — results are not persisted after the mission.
    /**
     * Creates a new instance of ImportDsmlRunnable without a response file.
     *
     * @param connection the connection to use
     * @param dsmlFile the DSML file to read from
     */
    public ImportDsmlRunnable( IBrowserConnection connection, File dsmlFile )
    {
        this( connection, dsmlFile, null );
    }


    // ── Clone Trooper Reports The LDAP Connection This Mission Uses ───────────────
    /**
     * {@inheritDoc}
     */
    public Connection[] getConnections()
    {
        return new Connection[]
            { browserConnection.getConnection() };
    }


    // ── Clone Trooper Reports The Human-Readable DSML Import Mission Name ────────
    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return BrowserCoreMessages.jobs__import_dsml_name;
    }


    // ── Clone Trooper Locks The DSML File Against Concurrent Import Missions ─────
    /**
     * {@inheritDoc}
     */
    public Object[] getLockedObjects()
    {
        List<Object> l = new ArrayList<Object>();
        l.add( browserConnection.getUrl() + "_" + DigestUtils.shaHex( dsmlFile.toString() ) ); //$NON-NLS-1$
        return l.toArray();
    }


    // ── Clone Trooper Returns The Error Message If The DSML Import Mission Fails ─
    /**
     * {@inheritDoc}
     */
    public String getErrorMessage()
    {
        return BrowserCoreMessages.jobs__import_dsml_error;
    }


    // ── Clone Trooper Executes Order 66: Parse And Execute All DSML Operations ───
    // Parses the DSML file with Dsmlv2Parser, iterates each request in the batch,
    // dispatches via processRequest(), tallies errors, and optionally writes the
    // batch-response file.  A dummy monitor is used to isolate per-request errors.
    /**
     * {@inheritDoc}
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask( BrowserCoreMessages.jobs__import_dsml_task, 2 );
        monitor.reportProgress( " " ); //$NON-NLS-1$
        monitor.worked( 1 );

        try
        {
            // Parsing the file
            Dsmlv2Grammar grammar = new Dsmlv2Grammar();
            Dsmlv2Parser parser = new Dsmlv2Parser( grammar );
            parser.setInput( new FileInputStream( dsmlFile ), "UTF-8" ); //$NON-NLS-1$
            parser.parseAllRequests();

            // Getting the batch request
            BatchRequestDsml batchRequest = parser.getBatchRequest();

            // Creating a DSML batch response (only if needed)
            BatchResponseDsml batchResponseDsml = null;
            if ( responseFile != null )
            {
                batchResponseDsml = new BatchResponseDsml();
            }

            // Setting the errors counter
            int errorsCount = 0;

            // Creating a dummy monitor that will be used to check if something
            // went wrong when executing the request
            StudioProgressMonitor dummyMonitor = new StudioProgressMonitor( monitor );

            // Processing each request
            List<DsmlDecorator<? extends Request>> requests = batchRequest.getRequests();
            for ( DsmlDecorator<? extends Request> request : requests )
            {
                // Processing the request
                processRequest( request, batchResponseDsml, dummyMonitor );

                // Verifying if any error has been reported
                if ( dummyMonitor.errorsReported() )
                {
                    errorsCount++;
                }

                dummyMonitor.reset();
            }

            // Writing the DSML response file to its final destination file.
            if ( responseFile != null )
            {
                FileOutputStream fos = new FileOutputStream( responseFile );
                OutputStreamWriter osw = new OutputStreamWriter( fos, "UTF-8" ); //$NON-NLS-1$
                BufferedWriter bufferedWriter = new BufferedWriter( osw );
                bufferedWriter.write( batchResponseDsml.toDsml() );
                bufferedWriter.close();
                osw.close();
                fos.close();
            }

            // Displaying an error message if we've had some errors
            if ( errorsCount > 0 )
            {
                monitor.reportError( BrowserCoreMessages.bind(
                    BrowserCoreMessages.dsml__n_errors_see_responsefile, new String[]
                        { "" + errorsCount } ) ); //$NON-NLS-1$
            }
        }
        catch ( Exception e )
        {
            monitor.reportError( e );
        }
    }


    // ── Clone Trooper Fires A Bulk-Modification Event After The Mission ──────────
    // Obi-Wan senses a disturbance in the Force: the EventRegistry notifies all
    // listeners that bulk modifications occurred on this connection.
    /**
     * {@inheritDoc}
     */
    public void runNotification( StudioProgressMonitor monitor )
    {
        EventRegistry.fireEntryUpdated( new BulkModificationEvent( browserConnection ), this );
    }


    // ── Clone Trooper Routes Each DSML Request To Its Specialist Handler ─────────
    // Han shoots first: a switch on the request type dispatches immediately to
    // the correct processXxx() method.  Unknown request types throw immediately
    // rather than silently failing.
    /**
     * Dispatches the given DSML request to the appropriate handler.
     *
     * @param request the DSML request to process
     * @param batchResponseDsml the DSML batch response (may be {@code null})
     * @param monitor the progress monitor
     * @throws LdapURLEncodingException if a URL in the request is invalid
     * @throws LdapException if an LDAP error occurs
     */
    private void processRequest( DsmlDecorator<? extends Request> request, BatchResponseDsml batchResponseDsml,
        StudioProgressMonitor monitor )
        throws LdapURLEncodingException, LdapException
    {
        switch ( request.getDecorated().getType() )
        {
            case BIND_REQUEST:
                processBindRequest( ( BindRequest ) request, batchResponseDsml, monitor );
                break;
            case ADD_REQUEST:
                processAddRequest( ( AddRequest ) request, batchResponseDsml, monitor );
                break;
            case COMPARE_REQUEST:
                processCompareRequest( ( CompareRequest ) request, batchResponseDsml, monitor );
                break;
            case DEL_REQUEST:
                processDelRequest( ( DeleteRequest ) request, batchResponseDsml, monitor );
                break;
            case EXTENDED_REQUEST:
                processExtendedRequest( ( ExtendedRequest ) request, batchResponseDsml, monitor );
                break;
            case MODIFY_REQUEST:
                processModifyRequest( ( ModifyRequest ) request, batchResponseDsml, monitor );
                break;
            case MODIFYDN_REQUEST:
                processModifyDNRequest( ( ModifyDnRequest ) request, batchResponseDsml, monitor );
                break;
            case SEARCH_REQUEST:
                processSearchRequest( ( SearchRequest ) request, batchResponseDsml, monitor );
                break;
            default:
                throw new IllegalArgumentException(
                    BrowserCoreMessages.dsml__should_not_be_encountering_request
                        + request.getDecorated().getType() );
        }
    }


    // ── Clone Trooper Responds With UNWILLING_TO_PERFORM For Bind Requests ───────
    // The Empire does not support bind re-negotiation mid-batch — Han shoots first
    // and returns an UNWILLING_TO_PERFORM result rather than attempting the bind.
    /**
     * Processes a bind request (currently unsupported — returns UNWILLING_TO_PERFORM).
     *
     * @param request the bind request
     * @param batchResponseDsml the DSML batch response (may be {@code null})
     * @param monitor the progress monitor
     */
    private void processBindRequest( BindRequest request, BatchResponseDsml batchResponseDsml,
        StudioProgressMonitor monitor )
    {
        // We can not support extended requests at the moment,
        // we need a more advanced connection wrapper.

        // Creating the response
        if ( batchResponseDsml != null )
        {
            BindResponseDsml authResponseDsml = new BindResponseDsml( codec );
            LdapResult ldapResult = authResponseDsml.getLdapResult();
            ldapResult.setResultCode( ResultCodeEnum.UNWILLING_TO_PERFORM );
            ldapResult.setDiagnosticMessage( BrowserCoreMessages.dsml__kind_request_not_supported );
            batchResponseDsml.addResponse( authResponseDsml );
        }
    }


    // ── Clone Trooper Executes An LDAP Add Operation And Invalidates The Cache ───
    // Sends the entry to the server via createEntry(), then marks the new entry's
    // parent as un-initialised so the browser re-fetches children on next view.
    /**
     * Processes an add request.
     *
     * @param request the add request
     * @param batchResponseDsml the DSML batch response (may be {@code null})
     * @param monitor the progress monitor
     */
    private void processAddRequest( AddRequest request, BatchResponseDsml batchResponseDsml,
        StudioProgressMonitor monitor )
    {
        // Executing the add request
        Entry entry = request.getEntry();
        browserConnection
            .getConnection()
            .getConnectionWrapper()
            .createEntry( entry, getControls( request ), monitor, null );

        // Creating the response
        if ( batchResponseDsml != null )
        {
            AddResponseDsml addResponseDsml = new AddResponseDsml( codec );
            LdapResult ldapResult = addResponseDsml.getLdapResult();
            setLdapResultValuesFromMonitor( ldapResult, monitor, MessageTypeEnum.ADD_REQUEST );
            ldapResult.setMatchedDn( entry.getDn() );
            batchResponseDsml.addResponse( addResponseDsml );
        }

        // Update cached entries
        Dn dn = entry.getDn();
        IEntry e = browserConnection.getEntryFromCache( dn );
        Dn parentDn = dn.getParent();
        IEntry parentEntry = parentDn != null ? browserConnection.getEntryFromCache( parentDn ) : null;
        if ( e != null )
        {
            e.setAttributesInitialized( false );
        }
        if ( parentEntry != null )
        {
            parentEntry.setChildrenInitialized( false );
        }
    }


    // ── Clone Trooper Declines A Compare Request With UNWILLING_TO_PERFORM ───────
    // Like bind, compare requests are unsupported in this importer.
    /**
     * Processes a compare request (currently unsupported — returns UNWILLING_TO_PERFORM).
     *
     * @param request the compare request
     * @param batchResponseDsml the DSML batch response (may be {@code null})
     * @param monitor the progress monitor
     */
    private void processCompareRequest( CompareRequest request, BatchResponseDsml batchResponseDsml,
        StudioProgressMonitor monitor )
    {
        // We can not support extended requests at the moment,
        // we need a more advanced connection wrapper.

        // Creating the response
        if ( batchResponseDsml != null )
        {
            CompareResponseDsml compareResponseDsml = new CompareResponseDsml( codec );
            LdapResult ldapResult = compareResponseDsml.getLdapResult();
            ldapResult.setResultCode( ResultCodeEnum.UNWILLING_TO_PERFORM );
            ldapResult.setDiagnosticMessage( BrowserCoreMessages.dsml__kind_request_not_supported );
            batchResponseDsml.addResponse( compareResponseDsml );
        }
    }


    // ── Clone Trooper Executes An LDAP Delete And Purges The Entry From Cache ────
    // Sends the delete to the server, marks the entry and its parent as
    // un-initialised, then removes the entry from the in-memory cache tree.
    /**
     * Processes a delete request.
     *
     * @param request the delete request
     * @param batchResponseDsml the DSML batch response (may be {@code null})
     * @param monitor the progress monitor
     */
    private void processDelRequest( DeleteRequest request, BatchResponseDsml batchResponseDsml,
        StudioProgressMonitor monitor )
    {
        // Executing the del request
        browserConnection.getConnection().getConnectionWrapper()
            .deleteEntry( request.getName(), getControls( request ), monitor, null );

        // Creating the response
        if ( batchResponseDsml != null )
        {
            DelResponseDsml delResponseDsml = new DelResponseDsml( codec );
            LdapResult ldapResult = delResponseDsml.getLdapResult();
            setLdapResultValuesFromMonitor( ldapResult, monitor, MessageTypeEnum.ADD_REQUEST );
            delResponseDsml.getLdapResult().setMatchedDn( request.getName() );
            batchResponseDsml.addResponse( delResponseDsml );
        }

        // Update cached entries
        Dn dn = request.getName();
        IEntry e = browserConnection.getEntryFromCache( dn );
        Dn parentDn = dn.getParent();
        IEntry parentEntry = parentDn != null ? browserConnection.getEntryFromCache( parentDn ) : null;
        if ( e != null )
        {
            e.setAttributesInitialized( false );
            browserConnection.uncacheEntryRecursive( e );
        }
        if ( parentEntry != null )
        {
            parentEntry.setChildrenInitialized( false );
        }
    }


    // ── Clone Trooper Declines An Extended Request With UNWILLING_TO_PERFORM ─────
    // Extended requests are beyond the current connection wrapper's capabilities.
    /**
     * Processes an extended request (currently unsupported — returns UNWILLING_TO_PERFORM).
     *
     * @param request the extended request
     * @param batchResponseDsml the DSML batch response (may be {@code null})
     * @param monitor the progress monitor
     */
    private void processExtendedRequest( ExtendedRequest request, BatchResponseDsml batchResponseDsml,
        StudioProgressMonitor monitor )
    {
        // We can not support extended requests at the moment,
        // we need a more advanced connection wrapper.

        // Creating the response
        if ( batchResponseDsml != null )
        {
            ExtendedResponseDsml extendedResponseDsml = new ExtendedResponseDsml( codec );
            LdapResult ldapResult = extendedResponseDsml.getLdapResult();
            ldapResult.setResultCode( ResultCodeEnum.UNWILLING_TO_PERFORM );
            ldapResult.setDiagnosticMessage( BrowserCoreMessages.dsml__kind_request_not_supported );
            batchResponseDsml.addResponse( extendedResponseDsml );
        }
    }


    // ── Clone Trooper Executes An LDAP Modify And Invalidates The Cached Entry ───
    // Sends the modifications to the server via modifyEntry(), then marks the
    // affected cache entry as un-initialised so attributes are refreshed.
    /**
     * Processes a modify request.
     *
     * @param request the modify request
     * @param batchResponseDsml the DSML batch response (may be {@code null})
     * @param monitor the progress monitor
     */
    private void processModifyRequest( ModifyRequest request, BatchResponseDsml batchResponseDsml,
        StudioProgressMonitor monitor )
    {
        // Executing the modify request
        browserConnection
            .getConnection()
            .getConnectionWrapper()
            .modifyEntry( request.getName(), request.getModifications(), getControls( request ), monitor, null );

        // Creating the response
        if ( batchResponseDsml != null )
        {
            ModifyResponseDsml modifyResponseDsml = new ModifyResponseDsml( codec );
            LdapResult ldapResult = modifyResponseDsml.getLdapResult();
            setLdapResultValuesFromMonitor( ldapResult, monitor, MessageTypeEnum.ADD_REQUEST );
            modifyResponseDsml.getLdapResult().setMatchedDn( request.getName() );
            batchResponseDsml.addResponse( modifyResponseDsml );
        }

        Dn dn = request.getName();
        IEntry e = browserConnection.getEntryFromCache( dn );
        if ( e != null )
        {
            e.setAttributesInitialized( false );
        }
    }


    // ── Clone Trooper Executes An LDAP ModifyDN (Rename Or Move) Operation ───────
    // Constructs the new DN (move or rename), calls renameEntry(), then invalidates
    // the old entry, its parent, and the new superior entry in the cache.
    /**
     * Processes a modifyDN request.
     *
     * @param request the modifyDN request
     * @param batchResponseDsml the DSML batch response (may be {@code null})
     * @param monitor the progress monitor
     */
    private void processModifyDNRequest( ModifyDnRequest request, BatchResponseDsml batchResponseDsml,
        StudioProgressMonitor monitor )
    {
        Dn newDn;
        try
        {
            if ( request.isMove() )
            {
                newDn = new Dn( request.getNewRdn(), request.getNewSuperior() );
            }
            else
            {
                newDn = new Dn( request.getNewRdn(), request.getName().getParent() );
            }
        }
        catch ( LdapInvalidDnException e )
        {
            throw new LdapRuntimeException( e );
        }

        // Executing the modify Dn request
        browserConnection
            .getConnection()
            .getConnectionWrapper()
            .renameEntry( request.getName(), newDn, request.getDeleteOldRdn(),
                getControls( request ), monitor, null );

        // Creating the response
        if ( batchResponseDsml != null )
        {
            ModDNResponseDsml modDNResponseDsml = new ModDNResponseDsml( codec );
            LdapResult ldapResult = modDNResponseDsml.getLdapResult();
            setLdapResultValuesFromMonitor( ldapResult, monitor, MessageTypeEnum.ADD_REQUEST );
            modDNResponseDsml.getLdapResult().setMatchedDn( request.getName() );
            batchResponseDsml.addResponse( modDNResponseDsml );
        }

        // Update cached entries
        Dn dn = request.getName();
        IEntry e = browserConnection.getEntryFromCache( dn );
        Dn parentDn = dn.getParent();
        IEntry parentEntry = parentDn != null ? browserConnection.getEntryFromCache( parentDn ) : null;
        if ( e != null )
        {
            e.setAttributesInitialized( false );
            browserConnection.uncacheEntryRecursive( e );
        }
        if ( parentEntry != null )
        {
            parentEntry.setChildrenInitialized( false );
        }
        if ( request.getNewSuperior() != null )
        {
            Dn newSuperiorDn = request.getNewSuperior();
            IEntry newSuperiorEntry = browserConnection.getEntryFromCache( newSuperiorDn );
            if ( newSuperiorEntry != null )
            {
                newSuperiorEntry.setChildrenInitialized( false );
            }
        }
    }


    // ── Clone Trooper Executes An LDAP Search And Encodes Results As DSML ────────
    // Only executed when a response file is requested (optimisation — no point
    // searching if results won't be recorded).  Delegates result encoding to
    // ExportDsmlRunnable.processAsDsmlResponse().
    /**
     * Processes a search request.
     *
     * @param request the search request
     * @param batchResponseDsml the DSML batch response (may be {@code null})
     * @param monitor the progress monitor
     * @throws LdapURLEncodingException if a URL in the results is invalid
     * @throws LdapException if an LDAP error occurs
     */
    private void processSearchRequest( SearchRequest request, BatchResponseDsml batchResponseDsml,
        StudioProgressMonitor monitor ) throws LdapURLEncodingException, LdapException
    {
        // Creating the response
        if ( batchResponseDsml != null )
        {
            // [Optimization] We're only searching if we need to produce a response
            StudioSearchResultEnumeration sre = browserConnection
                .getConnection()
                .getConnectionWrapper()
                .search( request.getBase().getName(), request.getFilter().toString(),
                    getSearchControls( request ), getAliasDereferencingMethod( request ),
                    ReferralHandlingMethod.IGNORE, getControls( request ), monitor, null );

            SearchParameter sp = new SearchParameter();
            sp.setReferralsHandlingMethod( browserConnection.getReferralsHandlingMethod() );
            ExportDsmlRunnable.processAsDsmlResponse( sre, batchResponseDsml, monitor, sp );
        }
    }


    // ── Clone Trooper Translates DSML Search Parameters Into JNDI SearchControls ─
    // Maps the DSML scope (OBJECT/ONELEVEL/SUBTREE), returning attributes, count
    // limit, and time limit onto a JNDI SearchControls object.
    /**
     * Translates the given {@link SearchRequest} into a JNDI {@link SearchControls}.
     *
     * @param request the search request
     * @return the equivalent SearchControls
     */
    private SearchControls getSearchControls( SearchRequest request )
    {
        SearchControls controls = new SearchControls();

        // Scope
        switch ( request.getScope() )
        {
            case OBJECT:
                controls.setSearchScope( SearchControls.OBJECT_SCOPE );
                break;
            case ONELEVEL:
                controls.setSearchScope( SearchControls.ONELEVEL_SCOPE );
                break;
            case SUBTREE:
                controls.setSearchScope( SearchControls.SUBTREE_SCOPE );
                break;
            default:
                controls.setSearchScope( SearchControls.ONELEVEL_SCOPE );
        }

        // Returning attributes
        List<String> returningAttributes = new ArrayList<String>();
        for ( String attribute : request.getAttributes() )
        {
            returningAttributes.add( attribute );
        }
        // If the returning attributes are empty, we need to return the user attributes
        // [Cf. RFC 2251 - "There are two special values which may be used: an empty 
        //  list with no attributes, and the attribute description string '*'.  Both of 
        //  these signify that all user attributes are to be returned."]
        if ( returningAttributes.size() == 0 )
        {
            returningAttributes.add( "*" ); //$NON-NLS-1$
        }

        controls.setReturningAttributes( returningAttributes.toArray( new String[0] ) );

        // Size Limit
        controls.setCountLimit( request.getSizeLimit() );

        // Time Limit
        controls.setTimeLimit( request.getTimeLimit() );

        return controls;
    }


    // ── Clone Trooper Maps DSML Alias Dereferencing Mode To Studio Enum ─────────
    // Translates the DSML DerefAliasesEnum to the Studio AliasDereferencingMethod
    // enum used by the connection wrapper.
    /**
     * Translates the alias-dereferencing mode in the given {@link SearchRequest}.
     *
     * @param request the search request
     * @return the equivalent {@link AliasDereferencingMethod}
     */
    private AliasDereferencingMethod getAliasDereferencingMethod( SearchRequest request )
    {
        switch ( request.getDerefAliases() )
        {
            case NEVER_DEREF_ALIASES:
                return AliasDereferencingMethod.NEVER;
            case DEREF_ALWAYS:
                return AliasDereferencingMethod.ALWAYS;
            case DEREF_FINDING_BASE_OBJ:
                return AliasDereferencingMethod.FINDING;
            case DEREF_IN_SEARCHING:
                return AliasDereferencingMethod.SEARCH;
            default:
                return AliasDereferencingMethod.NEVER;
        }
    }


    // ── Clone Trooper Extracts LDAP Controls From A DSML Request Message ─────────
    // Returns the request's attached controls as an array, or null if empty,
    // ready for passing to the connection wrapper.
    private Control[] getControls( Message request ) {
        Collection<Control> controls = request.getControls().values();
        if ( controls != null ) {
            return controls.toArray( new Control[0] );
        }
        return null;
    }


    // ── Clone Trooper Records The Operation Outcome In An LdapResult ─────────────
    // Han shoots first: if no errors, result code is SUCCESS; otherwise the
    // exception's best-estimate result code and diagnostic message are set.
    /**
     * Populates the given {@link LdapResult} with values derived from the monitor.
     *
     * @param ldapResult the result object to populate
     * @param monitor the progress monitor
     * @param messageType the message type used to estimate the result code on failure
     */
    private void setLdapResultValuesFromMonitor( LdapResult ldapResult, StudioProgressMonitor monitor,
        MessageTypeEnum messageType )
    {
        if ( !monitor.errorsReported() )
        {
            ldapResult.setResultCode( ResultCodeEnum.SUCCESS );
        }
        else
        {
            // Getting the exception
            Throwable t = monitor.getException();

            // Setting the result code
            ldapResult.setResultCode( ResultCodeEnum.getBestEstimate( t, messageType ) );

            // Setting the error message if there's one
            if ( t.getMessage() != null )
            {
                ldapResult.setDiagnosticMessage( t.getMessage() );
            }
        }
    }
}
