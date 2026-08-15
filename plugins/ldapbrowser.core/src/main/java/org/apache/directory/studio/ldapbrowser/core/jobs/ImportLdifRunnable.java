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


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.exception.LdapSchemaException;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCoreConstants;
import org.apache.directory.studio.connection.core.Controls;
import org.apache.directory.studio.connection.core.io.StudioLdapException;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.events.BulkModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.utils.ModelConverter;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.apache.directory.studio.ldifparser.LdifFormatParameters;
import org.apache.directory.studio.ldifparser.model.LdifEnumeration;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeAddRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeDeleteRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeModDnRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeModifyRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifContentRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifModSpec;
import org.apache.directory.studio.ldifparser.model.container.LdifRecord;
import org.apache.directory.studio.ldifparser.model.lines.LdifAttrValLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifCommentLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifControlLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifModSpecTypeLine;
import org.apache.directory.studio.ldifparser.parser.LdifParser;


// ── CLASS: ImportLdifRunnable — CLONE TROOPER EXECUTING LDIF ATTACK ORDERS ───
// Order 66 arrives as an LDIF file.  This runnable parses every record and
// dispatches each operation (content-add, change-add, change-delete,
// change-modify, change-modDN) to the live LDAP server.  All outcomes —
// successes and failures — are appended to an optional LDIF log file.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Runnable used to import an LDIF file.
 *
 * <p>Think of this as a clone trooper executing Order 66 from a plain-text
 * LDIF battle plan — each record is dispatched to the live server in sequence;
 * every success and failure is chronicled in a log file so the commanding
 * officer can audit the campaign.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportLdifRunnable implements StudioConnectionBulkRunnableWithProgress
{
    /** The browser connection. */
    private IBrowserConnection browserConnection;

    /** The LDIF file. */
    private File ldifFile;

    /** The log file. */
    private File logFile;

    /** The update if entry exists flag. */
    private boolean updateIfEntryExists;

    /** The continue on error flag. */
    private boolean continueOnError;


    // ── Clone Trooper Receives Full LDIF Import Orders With Log File ─────────────
    // Stores all mission parameters: connection, source LDIF file, optional log
    // file, whether existing entries should be updated on LDAP error 68,
    // and whether the import should continue past individual record errors.
    /**
     * Creates a new instance of ImportLdifRunnable.
     *
     * @param browserConnection the browser connection
     * @param ldifFile the LDIF file to import
     * @param logFile the log file (may be {@code null})
     * @param updateIfEntryExists true to REPLACE-modify an existing entry rather than failing
     * @param continueOnError true to continue past individual record errors
     */
    public ImportLdifRunnable( IBrowserConnection browserConnection, File ldifFile, File logFile,
        boolean updateIfEntryExists, boolean continueOnError )
    {
        this.browserConnection = browserConnection;
        this.ldifFile = ldifFile;
        this.logFile = logFile;
        this.continueOnError = continueOnError;
        this.updateIfEntryExists = updateIfEntryExists;
    }


    // ── Clone Trooper Receives LDIF Import Orders Without A Log File ─────────────
    // Convenience overload that delegates to the five-argument constructor
    // with a null log file — operation outcomes are not persisted.
    /**
     * Creates a new instance of ImportLdifRunnable without a log file.
     *
     * @param connection the browser connection
     * @param ldifFile the LDIF file to import
     * @param updateIfEntryExists true to REPLACE-modify an existing entry rather than failing
     * @param continueOnError true to continue past individual record errors
     */
    public ImportLdifRunnable( IBrowserConnection connection, File ldifFile, boolean updateIfEntryExists,
        boolean continueOnError )
    {
        this( connection, ldifFile, null, updateIfEntryExists, continueOnError );
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


    // ── Clone Trooper Reports The Human-Readable LDIF Import Mission Name ────────
    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return BrowserCoreMessages.jobs__import_ldif_name;
    }


    // ── Clone Trooper Locks The LDIF File Against Concurrent Import Missions ─────
    /**
     * {@inheritDoc}
     */
    public Object[] getLockedObjects()
    {
        List<Object> l = new ArrayList<Object>();
        l.add( browserConnection.getUrl() + "_" + DigestUtils.shaHex( ldifFile.toString() ) ); //$NON-NLS-1$
        return l.toArray();
    }


    // ── Clone Trooper Returns The Error Message If The LDIF Import Mission Fails ─
    /**
     * {@inheritDoc}
     */
    public String getErrorMessage()
    {
        return BrowserCoreMessages.jobs__import_ldif_error;
    }


    // ── Clone Trooper Executes Order 66: Parse LDIF And Import All Records ────────
    // Opens the LDIF file with LdifParser, opens or creates a no-op log writer,
    // then delegates to importLdif() for the main import loop.
    // Closes all I/O streams after the import completes or fails.
    /**
     * {@inheritDoc}
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask( BrowserCoreMessages.jobs__import_ldif_task, 2 );
        monitor.reportProgress( " " ); //$NON-NLS-1$
        monitor.worked( 1 );

        try
        {
            Reader ldifReader = new BufferedReader( new FileReader( this.ldifFile ) );
            LdifParser parser = new LdifParser();
            LdifEnumeration enumeration = parser.parse( ldifReader );

            Writer logWriter;
            if ( this.logFile != null )
            {
                logWriter = new BufferedWriter( new FileWriter( this.logFile ) );
            }
            else
            {
                logWriter = new Writer()
                {
                    public void close() throws IOException
                    {
                    }


                    public void flush() throws IOException
                    {
                    }


                    public void write( char[] cbuf, int off, int len ) throws IOException
                    {
                    }
                };
            }

            importLdif( browserConnection, enumeration, logWriter, updateIfEntryExists, continueOnError, monitor );

            logWriter.close();
            ldifReader.close();
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


    // ── Clone Trooper Iterates The LDIF Enumeration And Executes Each Record ─────
    // Main import loop: dispatches each LdifRecord to importLdifRecord(), logs
    // successes and failures, invalidates affected cache entries after each op,
    // and either stops or continues past errors depending on continueOnError.
    /**
     * Imports the LDIF enumeration.
     *
     * @param browserConnection the browser connection
     * @param enumeration the LDIF enumeration to iterate
     * @param logWriter the log writer
     * @param updateIfEntryExists true to update on LDAP error 68
     * @param continueOnError true to continue past individual record errors
     * @param monitor the progress monitor
     */
    static void importLdif( IBrowserConnection browserConnection, LdifEnumeration enumeration, Writer logWriter,
        boolean updateIfEntryExists, boolean continueOnError, StudioProgressMonitor monitor )
    {
        if ( browserConnection == null )
        {
            return;
        }

        StudioProgressMonitor dummyMonitor = new StudioProgressMonitor( monitor );
        int importedCount = 0;
        int errorCount = 0;
        try
        {
            while ( !monitor.isCanceled() && enumeration.hasNext() )
            {
                LdifContainer container = enumeration.next();

                if ( container instanceof LdifRecord )
                {
                    LdifRecord record = ( LdifRecord ) container;
                    try
                    {
                        dummyMonitor.reset();
                        importLdifRecord( browserConnection, record, updateIfEntryExists, dummyMonitor );
                        if ( dummyMonitor.errorsReported() )
                        {
                            errorCount++;
                            logModificationError( browserConnection, logWriter, record, dummyMonitor.getException(),
                                monitor );

                            if ( !continueOnError )
                            {
                                monitor.reportError( dummyMonitor.getException() );
                                return;
                            }
                        }
                        else
                        {
                            importedCount++;
                            logModification( browserConnection, logWriter, record, monitor );

                            // update cache and adjust attribute/children initialization flags
                            Dn dn = new Dn( record.getDnLine().getValueAsString() );
                            IEntry entry = browserConnection.getEntryFromCache( dn );
                            Dn parentDn = dn.getParent();
                            IEntry parentEntry = null;
                            while ( parentEntry == null && parentDn != null )
                            {
                                parentEntry = browserConnection.getEntryFromCache( parentDn );
                                parentDn = parentDn.getParent();
                            }

                            if ( record instanceof LdifChangeDeleteRecord )
                            {
                                if ( entry != null )
                                {
                                    entry.setAttributesInitialized( false );
                                    browserConnection.uncacheEntryRecursive( entry );
                                }
                                if ( parentEntry != null )
                                {
                                    parentEntry.setChildrenInitialized( false );
                                }
                            }
                            else if ( record instanceof LdifChangeModDnRecord )
                            {
                                if ( entry != null )
                                {
                                    entry.setAttributesInitialized( false );
                                    browserConnection.uncacheEntryRecursive( entry );
                                }
                                if ( parentEntry != null )
                                {
                                    parentEntry.setChildrenInitialized( false );
                                }
                                LdifChangeModDnRecord modDnRecord = ( LdifChangeModDnRecord ) record;
                                if ( modDnRecord.getNewsuperiorLine() != null )
                                {
                                    Dn newSuperiorDn = new Dn( modDnRecord.getNewsuperiorLine()
                                        .getValueAsString() );
                                    IEntry newSuperiorEntry = browserConnection.getEntryFromCache( newSuperiorDn );
                                    if ( newSuperiorEntry != null )
                                    {
                                        newSuperiorEntry.setChildrenInitialized( false );
                                    }
                                }
                            }
                            else if ( record instanceof LdifChangeAddRecord || record instanceof LdifContentRecord )
                            {
                                if ( entry != null )
                                {
                                    entry.setAttributesInitialized( false );
                                }
                                if ( parentEntry != null )
                                {
                                    parentEntry.setChildrenInitialized( false );
                                    parentEntry.setHasChildrenHint( true );
                                }
                            }
                            else
                            {
                                if ( entry != null )
                                {
                                    entry.setAttributesInitialized( false );
                                }
                            }
                        }
                    }
                    catch ( Exception e )
                    {
                        logModificationError( browserConnection, logWriter, record, e, monitor );
                        errorCount++;

                        if ( !continueOnError )
                        {
                            monitor.reportError( e );
                            return;
                        }
                    }

                    monitor.reportProgress( BrowserCoreMessages.bind(
                        BrowserCoreMessages.ldif__imported_n_entries_m_errors, new String[]
                            { "" + importedCount, "" + errorCount } ) ); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else
                {
                    logWriter.write( container.toRawString() );
                }
            }

            if ( errorCount > 0 )
            {
                monitor.reportError( BrowserCoreMessages.bind( BrowserCoreMessages.ldif__n_errors_see_logfile,
                    new String[]
                        { "" + errorCount } ) ); //$NON-NLS-1$
            }
        }
        catch ( Exception e )
        {
            monitor.reportError( e );
        }
    }


    // ── Clone Trooper Dispatches One LDIF Record To The Correct LDAP Operation ───
    // Han shoots first: validates the record, then routes it to createEntry,
    // deleteEntry, modifyEntry, or renameEntry based on its concrete type.
    // If a content-add fails with error 68 and updateIfEntryExists is set,
    // falls back to a REPLACE modification batch on the same DN.
    /**
     * Imports a single LDIF record by dispatching it to the appropriate LDAP operation.
     *
     * @param browserConnection the browser connection
     * @param record the LDIF record to import
     * @param updateIfEntryExists true to fall back to a REPLACE modify on LDAP error 68
     * @param monitor the progress monitor
     * @throws LdapException if the LDAP operation fails
     */
    static void importLdifRecord( IBrowserConnection browserConnection, LdifRecord record, boolean updateIfEntryExists,
        StudioProgressMonitor monitor ) throws LdapException
    {
        if ( !record.isValid() )
        {
            throw new LdapSchemaException( BrowserCoreMessages.bind( BrowserCoreMessages.model__invalid_record,
                record.getInvalidString() ) );
        }

        String dn = record.getDnLine().getValueAsString();

        if ( record instanceof LdifContentRecord || record instanceof LdifChangeAddRecord )
        {
            IEntry dummyEntry;
            if ( record instanceof LdifContentRecord )
            {
                LdifContentRecord attrValRecord = ( LdifContentRecord ) record;
                try
                {
                    dummyEntry = ModelConverter.ldifContentRecordToEntry( attrValRecord, browserConnection );
                }
                catch ( LdapInvalidDnException e )
                {
                    monitor.reportError( e );
                    return;
                }
            }
            else
            {
                LdifChangeAddRecord changeAddRecord = ( LdifChangeAddRecord ) record;
                try
                {
                    dummyEntry = ModelConverter.ldifChangeAddRecordToEntry( changeAddRecord, browserConnection );
                }
                catch ( LdapInvalidDnException e )
                {
                    monitor.reportError( e );
                    return;
                }
            }

            Entry entry = ModelConverter.toLdapApiEntry( dummyEntry );
            browserConnection.getConnection().getConnectionWrapper()
                .createEntry( entry, getControls( record ), monitor, null );

            if ( monitor.errorsReported() && updateIfEntryExists
                && StudioLdapException.isEntryAlreadyExistsException( monitor.getException() ) )
            {
                // creation failed with Error 68, now try to update the existing entry
                monitor.reset();

                Collection<Modification> modifications = ModelConverter.toReplaceModifications( entry );
                browserConnection.getConnection().getConnectionWrapper()
                    .modifyEntry( new Dn( dn ), modifications, getControls( record ), monitor, null );
            }
        }
        else if ( record instanceof LdifChangeDeleteRecord )
        {
            LdifChangeDeleteRecord changeDeleteRecord = ( LdifChangeDeleteRecord ) record;
            browserConnection.getConnection().getConnectionWrapper()
                .deleteEntry( new Dn( dn ), getControls( changeDeleteRecord ), monitor, null );
        }
        else if ( record instanceof LdifChangeModifyRecord )
        {
            LdifChangeModifyRecord modifyRecord = ( LdifChangeModifyRecord ) record;
            LdifModSpec[] modSpecs = modifyRecord.getModSpecs();
            Collection<Modification> modifications = new ArrayList<>();
            for ( int ii = 0; ii < modSpecs.length; ii++ )
            {
                LdifModSpecTypeLine modSpecType = modSpecs[ii].getModSpecType();
                LdifAttrValLine[] attrVals = modSpecs[ii].getAttrVals();

                DefaultAttribute attribute = new DefaultAttribute( modSpecType.getUnfoldedAttributeDescription() );
                for ( int x = 0; x < attrVals.length; x++ )
                {
                    Object valueAsObject = attrVals[x].getValueAsObject();
                    if ( valueAsObject instanceof String )
                    {
                        attribute.add( ( String ) valueAsObject );
                    }
                    else if ( valueAsObject instanceof byte[] )
                    {
                        attribute.add( ( byte[] ) valueAsObject );
                    }
                }

                if ( modSpecType.isAdd() )
                {
                    modifications.add( new DefaultModification( ModificationOperation.ADD_ATTRIBUTE, attribute ) );
                }
                else if ( modSpecType.isDelete() )
                {
                    modifications.add( new DefaultModification( ModificationOperation.REMOVE_ATTRIBUTE, attribute ) );
                }
                else if ( modSpecType.isReplace() )
                {
                    modifications.add( new DefaultModification( ModificationOperation.REPLACE_ATTRIBUTE, attribute ) );
                }
            }

            browserConnection.getConnection().getConnectionWrapper()
                .modifyEntry( new Dn( dn ), modifications, getControls( modifyRecord ), monitor, null );
        }
        else if ( record instanceof LdifChangeModDnRecord )
        {
            LdifChangeModDnRecord modDnRecord = ( LdifChangeModDnRecord ) record;
            if ( modDnRecord.getNewrdnLine() != null && modDnRecord.getDeloldrdnLine() != null )
            {
                String newRdn = modDnRecord.getNewrdnLine().getValueAsString();
                boolean deleteOldRdn = modDnRecord.getDeloldrdnLine().isDeleteOldRdn();

                Dn newDn;
                if ( modDnRecord.getNewsuperiorLine() != null )
                {
                    newDn = new Dn( newRdn, modDnRecord.getNewsuperiorLine().getValueAsString() );
                }
                else
                {
                    Dn dnObject = new Dn( dn );
                    Dn parent = dnObject.getParent();
                    newDn = new Dn( newRdn, parent.getName() );
                }

                browserConnection.getConnection().getConnectionWrapper()
                    .renameEntry( new Dn( dn ), newDn, deleteOldRdn, getControls( modDnRecord ), monitor, null );
            }
        }
    }


    // ── Clone Trooper Extracts LDAP Controls From An LDIF Change Record ──────────
    // Reads LdifControlLine items from a LdifChangeRecord and converts them to
    // LDAP API Control objects ready for the connection wrapper.
    // Returns null for non-change (content) records that carry no controls.
    /**
     * Gets the LDAP controls embedded in the given LDIF record.
     *
     * @param record the LDIF record
     * @return the controls array, or {@code null} if none are present
     */
    private static Control[] getControls( LdifRecord record )
    {
        Control[] controls = null;
        if ( record instanceof LdifChangeRecord )
        {
            LdifChangeRecord changeRecord = ( LdifChangeRecord ) record;
            LdifControlLine[] controlLines = changeRecord.getControls();
            controls = new Control[controlLines.length];
            for ( int i = 0; i < controlLines.length; i++ )
            {
                LdifControlLine line = controlLines[i];
                controls[i] = Controls.create( line.getUnfoldedOid(), line.isCritical(),
                    line.getControlValueAsBinary() );
            }
        }
        return controls;
    }


    // ── Clone Trooper Writes A "#!RESULT ERROR" Entry To The Log File ────────────
    // Records the connection URL, timestamp, error message, and the failing
    // LDIF record so the log file is a complete audit trail of what went wrong.
    // Any IOException during log writing is forwarded to the progress monitor.
    /**
     * Logs a modification error to the given writer.
     *
     * @param browserConnection the browser connection
     * @param logWriter the log writer
     * @param record the LDIF record that caused the error
     * @param exception the exception that was thrown
     * @param monitor the progress monitor
     */
    private static void logModificationError( IBrowserConnection browserConnection, Writer logWriter,
        LdifRecord record, Throwable exception, StudioProgressMonitor monitor )
    {
        try
        {
            LdifFormatParameters ldifFormatParameters = Utils.getLdifFormatParameters();
            DateFormat df = new SimpleDateFormat( ConnectionCoreConstants.DATEFORMAT );

            String errorComment = "#!ERROR " + exception.getMessage(); //$NON-NLS-1$
            errorComment = errorComment.replaceAll( "\r", " " ); //$NON-NLS-1$ //$NON-NLS-2$
            errorComment = errorComment.replaceAll( "\n", " " ); //$NON-NLS-1$ //$NON-NLS-2$
            LdifCommentLine errorCommentLine = LdifCommentLine.create( errorComment );

            logWriter.write( LdifCommentLine.create( "#!RESULT ERROR" ) //$NON-NLS-1$
                .toFormattedString( LdifFormatParameters.DEFAULT ) ); //$NON-NL LdifFormatParameters.DEFAULTS-1$
            logWriter
                .write( LdifCommentLine
                    .create(
                        "#!CONNECTION ldap://" + browserConnection.getConnection().getHost() + ":" + browserConnection.getConnection().getPort() ).toFormattedString( LdifFormatParameters.DEFAULT ) ); //$NON-NLS-1$ //$NON-NLS-2$
            logWriter.write( LdifCommentLine
                .create( "#!DATE " + df.format( new Date() ) ).toFormattedString( LdifFormatParameters.DEFAULT ) ); //$NON-NLS-1$
            logWriter.write( errorCommentLine.toFormattedString( LdifFormatParameters.DEFAULT ) );
            logWriter.write( record.toFormattedString( ldifFormatParameters ) );
        }
        catch ( IOException ioe )
        {
            monitor.reportError( BrowserCoreMessages.model__error_logging_modification, ioe );
        }
    }


    // ── Clone Trooper Writes A "#!RESULT OK" Entry To The Log File ───────────────
    // Records the connection URL, timestamp, and the successfully imported LDIF
    // record so the log file is a complete audit trail of what succeeded.
    /**
     * Logs a successful modification to the given writer.
     *
     * @param browserConnection the browser connection
     * @param logWriter the log writer
     * @param record the LDIF record that was successfully imported
     * @param monitor the progress monitor
     */
    private static void logModification( IBrowserConnection browserConnection, Writer logWriter, LdifRecord record,
        StudioProgressMonitor monitor )
    {
        try
        {
            LdifFormatParameters ldifFormatParameters = Utils.getLdifFormatParameters();
            DateFormat df = new SimpleDateFormat( ConnectionCoreConstants.DATEFORMAT );
            logWriter.write( LdifCommentLine.create( "#!RESULT OK" ).toFormattedString( ldifFormatParameters ) ); //$NON-NLS-1$
            logWriter
                .write( LdifCommentLine
                    .create(
                        "#!CONNECTION ldap://" + browserConnection.getConnection().getHost() + ":" + browserConnection.getConnection().getPort() ).toFormattedString( ldifFormatParameters ) ); //$NON-NLS-1$ //$NON-NLS-2$
            logWriter.write( LdifCommentLine
                .create( "#!DATE " + df.format( new Date() ) ).toFormattedString( ldifFormatParameters ) ); //$NON-NLS-1$
            logWriter.write( record.toFormattedString( ldifFormatParameters ) );
        }
        catch ( IOException ioe )
        {
            monitor.reportError( BrowserCoreMessages.model__error_logging_modification, ioe );
        }
    }
}
