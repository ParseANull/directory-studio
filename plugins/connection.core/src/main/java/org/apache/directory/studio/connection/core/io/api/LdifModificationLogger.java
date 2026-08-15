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

package org.apache.directory.studio.connection.core.io.api;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCoreConstants;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionManager;
import org.apache.directory.studio.connection.core.Controls;
import org.apache.directory.studio.connection.core.ILdapLogger;
import org.apache.directory.studio.connection.core.io.StudioLdapException;
import org.apache.directory.studio.ldifparser.LdifFormatParameters;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeAddRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeDeleteRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeModDnRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeModifyRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifModSpec;
import org.apache.directory.studio.ldifparser.model.lines.LdifAttrValLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifChangeTypeLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifCommentLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifControlLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifDeloldrdnLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifDnLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifModSpecSepLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifNewrdnLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifNewsuperiorLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifSepLine;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;


// ── CLASS: LdifModificationLogger — THE FALCON'S WRITE-OPERATION BLACK BOX ───
// R2's black box doesn't just record flight telemetry — it also logs every
// modification to the ship's manifest: adds, deletes, modify, renameEntry.
// This logger does the same for LDAP write operations.  Each operation is
// formatted as a proper LDIF change record (changetype: add/delete/modify/moddn),
// annotated with a result comment (#!RESULT OK or ERROR), connection URL,
// timestamp, and error message if any.
// The log rotates between N files of K kb each, one per connection, using the
// Java util.logging FileHandler.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * {@link ILdapLogger} implementation that records LDAP write operations
 * (add, delete, modify, modDN) to per-connection rotating LDIF log files.
 * Each log entry includes LDIF comment headers ({@code #!RESULT}, {@code #!CONNECTION},
 * {@code #!DATE}) followed by the change record in canonical LDIF format.
 * Attribute values whose names appear in {@link #getMaskedAttributes()} are
 * replaced with {@code "**********"} to avoid logging sensitive data.
 * The number and size of log files are configurable via Eclipse preferences
 * ({@code PREFERENCE_MODIFICATIONLOGS_FILE_COUNT} and {@code _FILE_SIZE}).
 * Think of this as the Falcon's black box for write operations: every time
 * Han fires a torpedo (modifies an entry), R2 records it — result, target, and
 * timestamp.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifModificationLogger implements ILdapLogger
{

    /** Extension point ID of this logger instance. */
    private String id;

    /** Human-readable name of this logger instance. */
    private String name;

    /** Description of this logger instance. */
    private String description;

    /** Per-connection FileHandlers keyed by connection UUID. */
    private Map<String, FileHandler> fileHandlers = new HashMap<String, FileHandler>();

    /** Per-connection java.util.logging.Loggers keyed by connection UUID. */
    private Map<String, Logger> loggers = new HashMap<String, Logger>();


    // ── CONSTRUCTOR — WIRE UP THE PREFERENCE CHANGE LISTENER ──────────────────────
    // When the user changes the log file count or size in preferences, we need to
    // close all existing handlers and clean up files that are now beyond the new
    // count limit.  We listen for those two preference keys and react.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link LdifModificationLogger} and registers a preference change
     * listener that resets all file handlers when the log file count or size changes.
     */
    public LdifModificationLogger()
    {
        IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode( ConnectionCoreConstants.PLUGIN_ID );
        prefs.addPreferenceChangeListener( event -> {
            if ( ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_FILE_COUNT.equals( event.getKey() )
                || ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_FILE_SIZE.equals( event.getKey() ) )
            {
                // dispose all loggers/handlers
                for ( Logger logger : loggers.values() )
                {
                    for ( Handler handler : logger.getHandlers() )
                    {
                        handler.close();
                    }
                }

                // delete files with index greater than new file count
                Connection[] connections = ConnectionCorePlugin.getDefault().getConnectionManager().getConnections();
                for ( Connection connection : connections )
                {
                    try
                    {
                        File[] logFiles = getLogFiles( connection );
                        for ( int i = getFileCount(); i < logFiles.length; i++ )
                        {
                            if ( logFiles[i] != null && logFiles[i].exists() )
                            {
                                logFiles[i].delete();
                            }
                        }
                    }
                    catch ( Exception e )
                    {
                    }
                }

                loggers.clear();
            }
        } );
    }


    // ── INIT MODIFICATION LOGGER — CREATE LOGGER + FILE HANDLER ─────────────────
    // We create an anonymous logger (no name collisions with other loggers),
    // attach a rotating FileHandler with our simple passthrough formatter,
    // and register both in our per-connection maps.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the per-connection logger on first use.
     * Creates a rotating {@link FileHandler} that writes to the connection's
     * modification log file pattern.
     *
     * @param connection  The connection to initialise logging for.
     */
    private void initModificationLogger( Connection connection )
    {
        Logger logger = Logger.getAnonymousLogger();
        loggers.put( connection.getId(), logger );
        logger.setLevel( Level.ALL );

        String logfileName = ConnectionManager.getModificationLogFileName( connection );
        try
        {
            FileHandler fileHandler = new FileHandler( logfileName, getFileSizeInKb() * 1000, getFileCount(), true );
            fileHandlers.put( connection.getId(), fileHandler );
            fileHandler.setFormatter( new Formatter()
            {
                public String format( LogRecord record )
                {
                    return record.getMessage();
                }
            } );
            logger.addHandler( fileHandler );
        }
        catch ( SecurityException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }


    // ── DISPOSE — CLOSE HANDLERS AND DELETE LOG FILES ─────────────────────────────
    // When a connection is deleted, we close its logger's file handlers and remove
    // the log files from disk so orphaned logs don't pile up.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Closes all file handlers for the given connection and deletes its log files.
     * Called when a connection is removed from the connection manager.
     *
     * @param connection  The connection being disposed.
     */
    public void dispose( Connection connection )
    {
        String id = connection.getId();
        if ( loggers.containsKey( id ) )
        {
            Handler[] handlers = loggers.get( id ).getHandlers();
            for ( Handler handler : handlers )
            {
                handler.close();
            }

            File[] files = getLogFiles( connection );
            for ( File file : files )
            {
                deleteFileWithRetry( file );
            }

            loggers.remove( id );
        }
    }


    // ── LOG — WRITE A FORMATTED LDIF RECORD + HEADERS TO THE FILE ─────────────────
    // We prepend #!RESULT, #!CONNECTION, and #!DATE comment lines, then append
    // the LDIF change record text.  If there's an error, we also add #!ERROR.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Writes a formatted modification log entry to the connection's log file.
     * Initialises the logger on first use.
     *
     * @param text        The pre-formatted LDIF change record string.
     * @param ex          The exception, or {@code null} if the operation succeeded.
     * @param connection  The connection that performed the modification.
     */
    private void log( String text, StudioLdapException ex, Connection connection )
    {
        String id = connection.getId();
        if ( !loggers.containsKey( id ) )
        {
            if ( connection.getName() != null )
            {
                initModificationLogger( connection );
            }
        }

        if ( loggers.containsKey( id ) )
        {
            StringJoiner lines = new StringJoiner( "" );
            DateFormat df = new SimpleDateFormat( ConnectionCoreConstants.DATEFORMAT );
            df.setTimeZone( ConnectionCoreConstants.UTC_TIME_ZONE );

            if ( ex != null )
            {
                lines.add( LdifCommentLine
                    .create( "#!RESULT ERROR" ).toFormattedString( LdifFormatParameters.DEFAULT ) ); //$NON-NLS-1$
            }
            else
            {
                lines.add( LdifCommentLine
                    .create( "#!RESULT OK" ).toFormattedString( LdifFormatParameters.DEFAULT ) ); //$NON-NLS-1$
            }

            lines.add(
                LdifCommentLine
                    .create( "#!CONNECTION ldap://" + connection.getHost() + ":" + connection.getPort() ) //$NON-NLS-1$//$NON-NLS-2$
                    .toFormattedString( LdifFormatParameters.DEFAULT ) );
            lines.add( LdifCommentLine
                .create( "#!DATE " + df.format( new Date() ) ).toFormattedString( LdifFormatParameters.DEFAULT ) ); //$NON-NLS-1$

            if ( ex != null )
            {
                String errorComment = "#!ERROR " + ex.getMessage(); //$NON-NLS-1$
                errorComment = errorComment.replaceAll( "\r", " " ); //$NON-NLS-1$ //$NON-NLS-2$
                errorComment = errorComment.replaceAll( "\n", " " ); //$NON-NLS-1$ //$NON-NLS-2$
                LdifCommentLine errorCommentLine = LdifCommentLine.create( errorComment );
                lines.add( errorCommentLine.toFormattedString( LdifFormatParameters.DEFAULT ) );
            }

            lines.add( text );
            Logger logger = loggers.get( id );
            logger.log( Level.ALL, lines.toString() );
        }
    }


    // ── LOG CHANGETYPE ADD — RECORD AN LDAP ADD OPERATION ─────────────────────────
    // Han adds a new entry to the directory — we record it as changetype: add
    // with all the attributes, masking any sensitive ones.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Logs an LDAP add operation as a {@code changetype: add} LDIF record.
     * Sensitive attribute values are replaced with {@code "**********"}.
     */
    public void logChangetypeAdd( Connection connection, final Entry entry, final Control[] controls,
        StudioLdapException ex )
    {
        if ( !isModificationLogEnabled() )
        {
            return;
        }

        Set<String> maskedAttributes = getMaskedAttributes();
        LdifChangeAddRecord record = new LdifChangeAddRecord( LdifDnLine.create( entry.getDn().getName() ) );
        addControlLines( record, controls );
        record.setChangeType( LdifChangeTypeLine.createAdd() );
        for ( Attribute attribute : entry )
        {
            String attributeName = attribute.getUpId();
            for ( Value value : attribute )
            {
                if ( maskedAttributes.contains( Strings.toLowerCaseAscii( attributeName ) ) )
                {
                    record.addAttrVal( LdifAttrValLine.create( attributeName, "**********" ) ); //$NON-NLS-1$
                }
                else
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
        }
        record.finish( LdifSepLine.create() );

        String formattedString = record.toFormattedString( LdifFormatParameters.DEFAULT );
        log( formattedString, ex, connection );
    }


    // ── LOG CHANGETYPE DELETE — RECORD AN LDAP DELETE OPERATION ──────────────────
    // Han fires a torpedo at the target entry — we record it as changetype: delete.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Logs an LDAP delete operation as a {@code changetype: delete} LDIF record.
     */
    public void logChangetypeDelete( Connection connection, final Dn dn, final Control[] controls,
        StudioLdapException ex )
    {
        if ( !isModificationLogEnabled() )
        {
            return;
        }

        LdifChangeDeleteRecord record = new LdifChangeDeleteRecord( LdifDnLine.create( dn.getName() ) );
        addControlLines( record, controls );
        record.setChangeType( LdifChangeTypeLine.createDelete() );
        record.finish( LdifSepLine.create() );

        String formattedString = record.toFormattedString( LdifFormatParameters.DEFAULT );
        log( formattedString, ex, connection );
    }


    // ── LOG CHANGETYPE MODIFY — RECORD AN LDAP MODIFY OPERATION ──────────────────
    // Han modifies the entry's cargo manifest — we record every modification
    // as an add/delete/replace modSpec, masking sensitive attribute values.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Logs an LDAP modify operation as a {@code changetype: modify} LDIF record.
     * Sensitive attribute values are replaced with {@code "**********"}.
     */
    public void logChangetypeModify( Connection connection, final Dn dn,
        final Collection<Modification> modifications, final Control[] controls, StudioLdapException ex )
    {
        if ( !isModificationLogEnabled() )
        {
            return;
        }

        Set<String> maskedAttributes = getMaskedAttributes();
        LdifChangeModifyRecord record = new LdifChangeModifyRecord( LdifDnLine.create( dn.getName() ) );
        addControlLines( record, controls );
        record.setChangeType( LdifChangeTypeLine.createModify() );
        for ( Modification item : modifications )
        {
            String attributeName = item.getAttribute().getUpId();
            LdifModSpec modSpec;
            switch ( item.getOperation() )
            {
                case ADD_ATTRIBUTE:
                    modSpec = LdifModSpec.createAdd( attributeName );
                    break;
                case REMOVE_ATTRIBUTE:
                    modSpec = LdifModSpec.createDelete( attributeName );
                    break;
                case REPLACE_ATTRIBUTE:
                    modSpec = LdifModSpec.createReplace( attributeName );
                    break;
                default:
                    continue;
            }
            for ( Value value : item.getAttribute() )
            {
                if ( maskedAttributes.contains( Strings.toLowerCaseAscii( attributeName ) ) )
                {
                    modSpec.addAttrVal( LdifAttrValLine.create( attributeName, "**********" ) ); //$NON-NLS-1$
                }
                else
                {
                    if ( value.isHumanReadable() )
                    {
                        modSpec.addAttrVal( LdifAttrValLine.create( attributeName, value.getString() ) );
                    }
                    else
                    {
                        modSpec.addAttrVal( LdifAttrValLine.create( attributeName, value.getBytes() ) );
                    }
                }
            }
            modSpec.finish( LdifModSpecSepLine.create() );

            record.addModSpec( modSpec );
        }
        record.finish( LdifSepLine.create() );

        String formattedString = record.toFormattedString( LdifFormatParameters.DEFAULT );
        log( formattedString, ex, connection );
    }


    // ── LOG CHANGETYPE MODDN — RECORD AN LDAP RENAME/MOVE OPERATION ───────────────
    // Han moves an entry to new coordinates — we record it as changetype: moddn
    // with the new RDN, delete-old-rdn flag, and newsuperior.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Logs an LDAP modDN (rename/move) operation as a {@code changetype: moddn} LDIF record.
     */
    public void logChangetypeModDn( Connection connection, final Dn oldDn, final Dn newDn,
        final boolean deleteOldRdn, final Control[] controls, StudioLdapException ex )
    {
        if ( !isModificationLogEnabled() )
        {
            return;
        }

        Rdn newrdn = newDn.getRdn();
        Dn newsuperior = newDn.getParent();

        LdifChangeModDnRecord record = new LdifChangeModDnRecord( LdifDnLine.create( oldDn.getName() ) );
        addControlLines( record, controls );
        record.setChangeType( LdifChangeTypeLine.createModDn() );
        record.setNewrdn( LdifNewrdnLine.create( newrdn.getName() ) );
        record.setDeloldrdn( deleteOldRdn ? LdifDeloldrdnLine.create1() : LdifDeloldrdnLine.create0() );
        record.setNewsuperior( LdifNewsuperiorLine.create( newsuperior.getName() ) );
        record.finish( LdifSepLine.create() );

        String formattedString = record.toFormattedString( LdifFormatParameters.DEFAULT );
        log( formattedString, ex, connection );
    }


    // ── ADD CONTROL LINES — APPEND LDIF CONTROL LINES TO THE RECORD ───────────────
    // We convert each LDAP control (OID + criticality + encoded value) into a
    // proper LDIF "control:" line and add it to the change record.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends LDIF {@code control:} lines to the given change record for each
     * LDAP control in the array.
     *
     * @param record    The LDIF change record to annotate.
     * @param controls  The LDAP controls to add (may be {@code null}).
     */
    private static void addControlLines( LdifChangeRecord record, Control[] controls )
    {
        if ( controls != null )
        {
            for ( Control control : controls )
            {
                String oid = control.getOid();
                boolean isCritical = control.isCritical();
                byte[] bytes = Controls.getEncodedValue( control );
                LdifControlLine controlLine = LdifControlLine.create( oid, isCritical, bytes );
                record.addControl( controlLine );
            }
        }
    }


    // ── GET FILES — EXPOSE LOG FILES TO THE UI ─────────────────────────────────────
    // The UI (Modification Logs view) calls this to get the log file paths so it can
    // display or open them.  We lazy-init the logger if needed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the log files for the given connection.
     * Initialises the logger if it has not been created yet.
     * Returns an empty array if no files are found.
     *
     * @param connection  The connection whose log files to retrieve.
     * @return  An array of log {@link File}s, sorted by name.
     */
    public File[] getFiles( Connection connection )
    {
        String id = connection.getId();
        if ( !loggers.containsKey( id ) )
        {
            if ( connection.getName() != null )
            {
                initModificationLogger( connection );
            }
        }

        try
        {
            return getLogFiles( connection );
        }
        catch ( Exception e )
        {
            return new File[0];
        }
    }


    // ── GET LOG FILES — PATTERN MATCH FILES IN THE LOG DIRECTORY ──────────────────
    // Java's FileHandler uses %g (generation) and %u (unique) placeholders in
    // the file name pattern.  We replace those with regex wildcards and match
    // all rotation files in the directory.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all rotation log files matching the given connection's modification log
     * file name pattern.
     *
     * @param connection  The connection whose log files to list.
     * @return  Sorted array of matching {@link File}s.
     */
    private static File[] getLogFiles( Connection connection )
    {
        String logfileNamePattern = ConnectionManager.getModificationLogFileName( connection );
        File file = new File( logfileNamePattern );
        String pattern = file.getName().replace( "%u", "\\d+" ).replace( "%g", "\\d+" );
        File dir = file.getParentFile();
        File[] files = dir.listFiles( ( d, f ) -> {
            return f.matches( pattern );
        } );
        Arrays.sort( files );
        return files;
    }


    // ── IS MODIFICATION LOG ENABLED — CHECK THE PREFERENCE ────────────────────────
    /**
     * Returns {@code true} if the modification log is enabled in Eclipse preferences.
     *
     * @return  {@code true} if logging is enabled.
     */
    private boolean isModificationLogEnabled()
    {
        return Platform.getPreferencesService().getBoolean( ConnectionCoreConstants.PLUGIN_ID,
            ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_ENABLE, true, null );
    }


    // ── GET FILE COUNT — READ THE ROTATION FILE COUNT PREFERENCE ──────────────────
    /**
     * Returns the number of log rotation files to maintain per connection.
     *
     * @return  The configured file count (default: 10).
     */
    private int getFileCount()
    {
        return Platform.getPreferencesService().getInt( ConnectionCoreConstants.PLUGIN_ID,
            ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_FILE_COUNT, 10, null );
    }


    // ── GET FILE SIZE IN KB — READ THE MAX FILE SIZE PREFERENCE ───────────────────
    /**
     * Returns the maximum size per log file in kilobytes.
     *
     * @return  The configured maximum file size in kB (default: 100).
     */
    private int getFileSizeInKb()
    {
        return Platform.getPreferencesService().getInt( ConnectionCoreConstants.PLUGIN_ID,
            ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_FILE_SIZE, 100, null );
    }


    /**
     * {@inheritDoc}
     */
    public String getId()
    {
        return id;
    }


    /**
     * {@inheritDoc}
     */
    public void setId( String id )
    {
        this.id = id;
    }


    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return name;
    }


    /**
     * {@inheritDoc}
     */
    public void setName( String name )
    {
        this.name = name;
    }


    /**
     * {@inheritDoc}
     */
    public String getDescription()
    {
        return description;
    }


    /**
     * {@inheritDoc}
     */
    public void setDescription( String description )
    {
        this.description = description;
    }

}
