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
import java.util.ArrayList;
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

import javax.naming.directory.SearchControls;

import org.apache.commons.lang3.StringUtils;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.Referral;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.connection.core.ConnectionCoreConstants;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionManager;
import org.apache.directory.studio.connection.core.ILdapLogger;
import org.apache.directory.studio.connection.core.ReferralsInfo;
import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.connection.core.io.StudioLdapException;
import org.apache.directory.studio.ldifparser.LdifFormatParameters;
import org.apache.directory.studio.ldifparser.model.container.LdifContentRecord;
import org.apache.directory.studio.ldifparser.model.lines.LdifAttrValLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifCommentLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifDnLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifLineBase;
import org.apache.directory.studio.ldifparser.model.lines.LdifSepLine;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;


// ── CLASS: LdifSearchLogger — THE FALCON'S SENSOR SWEEP BLACK BOX ─────────────
// Every time R2 fires the sensors (LDAP search), the black box records the
// request details (LDAP URL, command line, scope, filter, attributes, controls)
// and each result entry that comes back.  This helps with debugging complex
// search scenarios and is displayed in the Search Logs view.
// The log rotates between N files of K kb each, one per connection, using
// Java util.logging FileHandler.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * {@link ILdapLogger} implementation that records LDAP search requests, result entries,
 * result references, and search-done messages to per-connection rotating LDIF log files.
 * Each log entry includes LDIF comment headers ({@code #!SEARCH REQUEST},
 * {@code #!SEARCH RESULT ENTRY}, {@code #!SEARCH RESULT DONE}, etc.) with
 * {@code #!RESULT}, {@code #!CONNECTION}, and {@code #!DATE} lines.
 * Sensitive attribute values are replaced with {@code "**********"}.
 * File count and size are configurable via Eclipse preferences
 * ({@code PREFERENCE_SEARCHLOGS_FILE_COUNT} and {@code _FILE_SIZE}).
 * Think of this as R2's sensor sweep recorder: every search request and every
 * result entry is written to the black box.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifSearchLogger implements ILdapLogger
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
    // When the user changes file count or size preferences, we close all handlers
    // and clean up rotation files beyond the new count.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link LdifSearchLogger} and registers a preference change listener
     * that resets all file handlers when the log file count or size changes.
     */
    public LdifSearchLogger()
    {
        IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode( ConnectionCoreConstants.PLUGIN_ID );
        prefs.addPreferenceChangeListener( event -> {
            if ( ConnectionCoreConstants.PREFERENCE_SEARCHLOGS_FILE_COUNT.equals( event.getKey() )
                || ConnectionCoreConstants.PREFERENCE_SEARCHLOGS_FILE_SIZE.equals( event.getKey() ) )
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


    // ── INIT SEARCH LOGGER — CREATE LOGGER + FILE HANDLER ─────────────────────────
    // We create an anonymous logger and attach a rotating FileHandler for this
    // connection's search log file pattern.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the per-connection search logger on first use.
     *
     * @param connection  The connection to initialise logging for.
     */
    private void initSearchLogger( Connection connection )
    {
        Logger logger = Logger.getAnonymousLogger();
        loggers.put( connection.getId(), logger );
        logger.setLevel( Level.ALL );

        String logfileName = ConnectionManager.getSearchLogFileName( connection );
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
    // We prepend the result, connection URL, and date comment lines, then append
    // the content text.  The type parameter provides the log category label
    // (e.g. "SEARCH REQUEST (1)").
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Writes a formatted search log entry to the connection's log file.
     *
     * @param text        The pre-formatted LDIF content string.
     * @param type        The log category label (e.g. {@code "SEARCH REQUEST (1)"}).
     * @param ex          The exception, or {@code null} if the operation succeeded.
     * @param connection  The connection that performed the search.
     */
    private void log( String text, String type, StudioLdapException ex, Connection connection )
    {
        String id = connection.getId();
        if ( !loggers.containsKey( id ) )
        {
            if ( connection.getName() != null )
            {
                initSearchLogger( connection );
            }
        }

        if ( loggers.containsKey( id ) )
        {
            StringJoiner lines = new StringJoiner( "" );
            DateFormat df = new SimpleDateFormat( ConnectionCoreConstants.DATEFORMAT );
            df.setTimeZone( ConnectionCoreConstants.UTC_TIME_ZONE );

            if ( ex != null )
            {
                lines.add( LdifCommentLine.create( "#!" + type + " ERROR" ) //$NON-NLS-1$//$NON-NLS-2$
                    .toFormattedString( LdifFormatParameters.DEFAULT ) );
            }
            else
            {
                lines.add( LdifCommentLine.create( "#!" + type + " OK" ) //$NON-NLS-1$ //$NON-NLS-2$
                    .toFormattedString( LdifFormatParameters.DEFAULT ) );
            }

            lines.add(
                LdifCommentLine
                    .create( "#!CONNECTION ldap://" + connection.getHost() + ":" + connection.getPort() ) //$NON-NLS-1$//$NON-NLS-2$
                    .toFormattedString( LdifFormatParameters.DEFAULT ) );
            lines.add( LdifCommentLine.create( "#!DATE " + df.format( new Date() ) ) //$NON-NLS-1$
                .toFormattedString( LdifFormatParameters.DEFAULT ) );

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


    // ── LOG SEARCH REQUEST — RECORD THE SEARCH PARAMETERS ─────────────────────────
    // R2 fires the sensors — we log the full search parameters: base DN, scope,
    // filter, attributes, alias dereferencing, size/time limits, and controls.
    // We also log the equivalent LDAP URL and ldapsearch command line.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Logs the LDAP search request parameters including the LDAP URL and equivalent
     * {@code ldapsearch} command line as LDIF comment lines.
     */
    public void logSearchRequest( Connection connection, String searchBase, String filter,
        SearchControls searchControls, AliasDereferencingMethod aliasesDereferencingMethod,
        Control[] controls, long requestNum, StudioLdapException ex )
    {
        if ( !isSearchRequestLogEnabled() )
        {
            return;
        }

        String scopeAsString = searchControls.getSearchScope() == SearchControls.SUBTREE_SCOPE ? "wholeSubtree (2)" //$NON-NLS-1$
            : searchControls.getSearchScope() == SearchControls.ONELEVEL_SCOPE ? "singleLevel (1)" : "baseObject (0)"; //$NON-NLS-1$ //$NON-NLS-2$
        String attributesAsString = searchControls.getReturningAttributes() == null ? "*" //$NON-NLS-1$
            : searchControls
                .getReturningAttributes().length == 0 ? "1.1" //$NON-NLS-1$
                    : StringUtils.join( searchControls.getReturningAttributes(),
                        " " );
        String aliasAsString = aliasesDereferencingMethod == AliasDereferencingMethod.ALWAYS ? "derefAlways (3)" //$NON-NLS-1$
            : aliasesDereferencingMethod == AliasDereferencingMethod.FINDING ? "derefFindingBaseObj (2)" //$NON-NLS-1$
                : aliasesDereferencingMethod == AliasDereferencingMethod.SEARCH ? "derefInSearching (1)" //$NON-NLS-1$
                    : "neverDerefAliases (0)"; //$NON-NLS-1$

        // build LDAP URL
        LdapUrl url = Utils.getLdapURL( connection, searchBase, searchControls.getSearchScope(), filter, searchControls
            .getReturningAttributes() );

        // build command line
        String cmdLine = Utils.getLdapSearchCommandLine( connection, searchBase, searchControls.getSearchScope(),
            aliasesDereferencingMethod, searchControls.getCountLimit(), searchControls.getTimeLimit(), filter,
            searchControls.getReturningAttributes() );

        Collection<LdifLineBase> lines = new ArrayList<LdifLineBase>();
        lines.add( LdifCommentLine.create( "# LDAP URL     : " + url.toString() ) ); //$NON-NLS-1$
        lines.add( LdifCommentLine.create( "# command line : " + cmdLine.toString() ) ); //$NON-NLS-1$
        lines.add( LdifCommentLine.create( "# baseObject   : " + searchBase ) ); //$NON-NLS-1$
        lines.add( LdifCommentLine.create( "# scope        : " + scopeAsString ) ); //$NON-NLS-1$
        lines.add( LdifCommentLine.create( "# derefAliases : " + aliasAsString ) ); //$NON-NLS-1$
        lines.add( LdifCommentLine.create( "# sizeLimit    : " + searchControls.getCountLimit() ) ); //$NON-NLS-1$
        lines.add( LdifCommentLine.create( "# timeLimit    : " + searchControls.getTimeLimit() ) ); //$NON-NLS-1$
        lines.add( LdifCommentLine.create( "# typesOnly    : " + "False" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        lines.add( LdifCommentLine.create( "# filter       : " + filter ) ); //$NON-NLS-1$
        lines.add( LdifCommentLine.create( "# attributes   : " + attributesAsString ) ); //$NON-NLS-1$
        if ( controls != null )
        {
            for ( Control control : controls )
            {
                lines.add( LdifCommentLine.create( "# control      : " + control.getOid() ) ); //$NON-NLS-1$
            }
        }
        lines.add( LdifSepLine.create() );

        String formattedString = ""; //$NON-NLS-1$
        for ( LdifLineBase line : lines )
        {
            formattedString += line.toFormattedString( LdifFormatParameters.DEFAULT );
        }

        log( formattedString, "SEARCH REQUEST (" + requestNum + ")", ex, connection ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── LOG SEARCH RESULT ENTRY — RECORD EACH RETURNED ENTRY ─────────────────────
    // R2 reports each sensor hit — we log it as a content LDIF record
    // (DN + all attributes), masking any sensitive attribute values.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Logs a single LDAP search result entry as a content LDIF record.
     * Sensitive attribute values are replaced with {@code "**********"}.
     */
    public void logSearchResultEntry( Connection connection, StudioSearchResult studioSearchResult, long requestNum,
        StudioLdapException ex )
    {
        if ( !isSearchResultEntryLogEnabled() )
        {
            return;
        }

        String formattedString;
        if ( studioSearchResult != null )
        {
            Set<String> maskedAttributes = getMaskedAttributes();
            Entry entry = studioSearchResult.getEntry();

            LdifContentRecord record = new LdifContentRecord( LdifDnLine.create( entry.getDn().getName() ) );
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
            formattedString = record.toFormattedString( LdifFormatParameters.DEFAULT );
        }
        else
        {
            formattedString = LdifFormatParameters.DEFAULT.getLineSeparator();
        }

        log( formattedString, "SEARCH RESULT ENTRY (" + requestNum + ")", ex, connection ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── LOG SEARCH RESULT REFERENCE — RECORD A REFERRAL IN THE RESULTS ────────────
    // The server returned a search result reference (a referral mid-search) —
    // we log its URL list as a comment line.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Logs a search result reference (mid-search referral) as a comment LDIF record.
     */
    public void logSearchResultReference( Connection connection, Referral referral,
        ReferralsInfo referralsInfo, long requestNum, StudioLdapException ex )
    {
        if ( !isSearchResultEntryLogEnabled() )
        {
            return;
        }

        Collection<LdifLineBase> lines = new ArrayList<LdifLineBase>();
        lines
            .add( LdifCommentLine.create( "# reference : " + ( referral != null ? referral.getLdapUrls() : "null" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
        lines.add( LdifSepLine.create() );

        String formattedString = ""; //$NON-NLS-1$
        for ( LdifLineBase line : lines )
        {
            formattedString += line.toFormattedString( LdifFormatParameters.DEFAULT );
        }
        log( formattedString, "SEARCH RESULT REFERENCE (" + requestNum + ")", ex, connection ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── LOG SEARCH RESULT DONE — RECORD THE SEARCH COMPLETION ─────────────────────
    // R2's sensor sweep is done — we log how many entries were returned.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Logs the search-done event, including the total entry count.
     */
    public void logSearchResultDone( Connection connection, long count, long requestNum, StudioLdapException ex )
    {
        if ( !isSearchRequestLogEnabled() )
        {
            return;
        }

        Collection<LdifLineBase> lines = new ArrayList<LdifLineBase>();
        lines.add( LdifCommentLine.create( "# numEntries : " + count ) ); //$NON-NLS-1$
        lines.add( LdifSepLine.create() );

        String formattedString = ""; //$NON-NLS-1$
        for ( LdifLineBase line : lines )
        {
            formattedString += line.toFormattedString( LdifFormatParameters.DEFAULT );
        }
        log( formattedString, "SEARCH RESULT DONE (" + requestNum + ")", ex, connection ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── GET FILES — EXPOSE LOG FILES TO THE UI ─────────────────────────────────────
    /**
     * Returns the search log files for the given connection.
     * Initialises the logger if it has not been created yet.
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
                initSearchLogger( connection );
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


    // ── GET LOG FILES — MATCH ROTATION FILES BY NAME PATTERN ──────────────────────
    /**
     * Returns all rotation log files matching the given connection's search log
     * file name pattern.
     *
     * @param connection  The connection whose log files to list.
     * @return  Sorted array of matching {@link File}s.
     */
    private static File[] getLogFiles( Connection connection )
    {
        String logfileNamePattern = ConnectionManager.getSearchLogFileName( connection );
        File file = new File( logfileNamePattern );
        String pattern = file.getName().replace( "%u", "\\d+" ).replace( "%g", "\\d+" );
        File dir = file.getParentFile();
        File[] files = dir.listFiles( ( d, f ) -> {
            return f.matches( pattern );
        } );
        Arrays.sort( files );
        return files;
    }


    // ── PREFERENCES HELPERS ────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if search request logging is enabled.
     */
    private boolean isSearchRequestLogEnabled()
    {
        return ConnectionCorePlugin.getDefault().isSearchRequestLogsEnabled();
    }


    /**
     * Returns {@code true} if search result entry logging is enabled.
     */
    private boolean isSearchResultEntryLogEnabled()
    {
        return ConnectionCorePlugin.getDefault().isSearchResultEntryLogsEnabled();
    }


    /**
     * Returns the number of log rotation files per connection.
     *
     * @return  The configured file count.
     */
    private int getFileCount()
    {
        return ConnectionCorePlugin.getDefault().getSearchLogsFileCount();
    }


    /**
     * Returns the maximum size per log file in kilobytes.
     *
     * @return  The configured maximum file size in kB.
     */
    private int getFileSizeInKb()
    {
        return ConnectionCorePlugin.getDefault().getSearchLogsFileSize();
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
