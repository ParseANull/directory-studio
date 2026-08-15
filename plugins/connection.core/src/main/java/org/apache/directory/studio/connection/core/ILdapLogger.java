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
package org.apache.directory.studio.connection.core;


import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.naming.directory.SearchControls;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.Referral;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.connection.core.io.StudioLdapException;
import org.apache.directory.studio.connection.core.io.api.StudioSearchResult;
import org.eclipse.core.runtime.Platform;


// ── CLASS: ILdapLogger — THE FALCON'S BLACK BOX FLIGHT RECORDER ──────────────
// Every good starship has a black box: it records every maneuver, every
// weapon discharge, and every close call so the crew can review the mission
// afterwards.  When something goes wrong, you pull the black box and see
// exactly what happened.
// This interface is the Falcon's black box API: it records every LDAP operation
// — add, delete, modify, moddn, search — including controls, results, referrals,
// and any exceptions that occurred.  Multiple implementations can be plugged in
// (LDIF modification logger, LDIF search logger, etc.) via Eclipse extension points.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Plugin-point interface for LDAP operation loggers.
 * Implementations are registered via the {@code ldaplogger} Eclipse extension point
 * and receive callbacks for every LDAP write and search operation executed by
 * the connection machinery.
 * All logging methods have default no-op implementations so loggers only need to
 * override the operations they care about.
 * Two built-in implementations exist: one that writes modification changelogs
 * in LDIF format, and one that writes search requests and results in LDIF format.
 * Think of this interface as the Falcon's black box: any module that wants to
 * record traffic plugs in here and gets told about everything that happens.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ILdapLogger
{

    // ── LOG CHANGETYPE ADD — BLACK BOX RECORDS AN ENTRY CREATION ─────────────────
    // The Falcon fires its ion cannons (adds an LDAP entry) and the black box
    // records the shot: what was added, any controls sent, and whether it succeeded.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called after a {@code changetype: add} operation completes.
     * Loggers that record modifications (e.g. LDIF change-log logger) override this.
     *
     * @param connection  The connection on which the operation ran.
     * @param entry       The entry that was (or was attempted to be) added.
     * @param controls    Any LDAP controls attached to the add request.
     * @param ex          The LDAP exception if the operation failed, or {@code null} on success.
     */
    default void logChangetypeAdd( Connection connection, final Entry entry, final Control[] controls,
        StudioLdapException ex )
    {
    }


    // ── LOG CHANGETYPE DELETE — BLACK BOX RECORDS AN ENTRY REMOVAL ───────────────
    // The Falcon fires its proton torpedoes (deletes an LDAP entry) and the black
    // box records the target DN, controls, and outcome.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called after a {@code changetype: delete} operation completes.
     * Loggers that record modifications override this.
     *
     * @param connection  The connection on which the operation ran.
     * @param dn          The DN of the entry that was (or was attempted to be) deleted.
     * @param controls    Any LDAP controls attached to the delete request.
     * @param ex          The LDAP exception if the operation failed, or {@code null} on success.
     */
    default void logChangetypeDelete( Connection connection, final Dn dn, final Control[] controls,
        StudioLdapException ex )
    {

    }


    // ── LOG CHANGETYPE MODIFY — BLACK BOX RECORDS AN ATTRIBUTE CHANGE ────────────
    // The Falcon's crew reprograms the navigation computer (modifies an entry) and
    // the black box records the DN, the modification list, controls, and result.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called after a {@code changetype: modify} operation completes.
     * Loggers that record modifications override this.
     *
     * @param connection     The connection on which the operation ran.
     * @param dn             The DN of the modified entry.
     * @param modifications  The list of attribute modifications applied.
     * @param controls       Any LDAP controls attached to the modify request.
     * @param ex             The LDAP exception if the operation failed, or {@code null} on success.
     */
    default void logChangetypeModify( Connection connection, final Dn dn,
        final Collection<Modification> modifications, final Control[] controls, StudioLdapException ex )
    {
    }


    // ── LOG CHANGETYPE MODDN — BLACK BOX RECORDS A RENAME/MOVE ───────────────────
    // The Falcon jumps to a new hyperspace coordinate (entry is renamed or moved)
    // and the black box records the old and new DNs plus the deleteOldRdn flag.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called after a {@code changetype: moddn} (rename or move) operation completes.
     * Loggers that record modifications override this.
     *
     * @param connection      The connection on which the operation ran.
     * @param oldDn           The original DN of the entry.
     * @param newDn           The new DN after the rename/move.
     * @param deleteOldRdn    Whether the old RDN attribute value was deleted.
     * @param controls        Any LDAP controls attached to the modDN request.
     * @param ex              The LDAP exception if the operation failed, or {@code null} on success.
     */
    default void logChangetypeModDn( Connection connection, final Dn oldDn, final Dn newDn,
        final boolean deleteOldRdn, final Control[] controls, StudioLdapException ex )
    {
    }


    // ── SET ID — LABELS THE LOGGER PLUGIN ────────────────────────────────────────
    // We stamp an identifier on this logger so the plugin registry can track it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets this logger's plugin-registry ID.
     * The framework calls this after instantiating the logger from the extension point.
     *
     * @param id  The unique ID string assigned by the plugin registry.
     */
    void setId( String id );


    // ── GET ID — READS THE LOGGER'S REGISTRY ID ───────────────────────────────────
    // We retrieve the identifier that was stamped on this logger.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns this logger's plugin-registry ID.
     *
     * @return  The ID string, as set by {@link #setId(String)}.
     */
    String getId();


    // ── SET NAME — GIVES THE LOGGER A HUMAN-READABLE NAME ────────────────────────
    // We give the logger a display name so users can identify it in preferences.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets this logger's human-readable display name.
     *
     * @param name  The display name string.
     */
    void setName( String name );


    // ── GET NAME — READS THE LOGGER'S DISPLAY NAME ────────────────────────────────
    // We return the display name for this logger.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns this logger's display name.
     *
     * @return  The name string, as set by {@link #setName(String)}.
     */
    String getName();


    // ── SET DESCRIPTION — GIVES THE LOGGER A DESCRIPTION ─────────────────────────
    // We store a description so users know what this logger does when they see it
    // in the preferences UI.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets a short description of what this logger does.
     *
     * @param description  The description string.
     */
    void setDescription( String description );


    // ── GET DESCRIPTION — READS THE LOGGER'S DESCRIPTION ─────────────────────────
    // We return the description string for display.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns this logger's description.
     *
     * @return  The description string, as set by {@link #setDescription(String)}.
     */
    String getDescription();


    // ── LOG SEARCH REQUEST — BLACK BOX RECORDS THE SEARCH WE SENT ────────────────
    // The Falcon's sensor array fires off a scan (LDAP search request) and the
    // black box records the target coordinates, scope, filter, and controls.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called just before or after a search request is sent to the server.
     * Search loggers (e.g. the LDIF search logger) override this to record the
     * outgoing search parameters.
     *
     * @param connection                   The connection used.
     * @param searchBase                   The base DN for the search.
     * @param filter                       The LDAP filter string.
     * @param searchControls               The scope, size limit, time limit, and attribute list.
     * @param aliasesDereferencingMethod   How aliases should be dereferenced.
     * @param controls                     LDAP controls attached to the search request.
     * @param requestNum                   A sequence number correlating request to results.
     * @param ex                           The LDAP exception if the request failed, or {@code null}.
     */
    default void logSearchRequest( Connection connection, String searchBase, String filter,
        SearchControls searchControls, AliasDereferencingMethod aliasesDereferencingMethod,
        Control[] controls, long requestNum, StudioLdapException ex )
    {
    }


    // ── LOG SEARCH RESULT ENTRY — BLACK BOX RECORDS EACH INCOMING ENTRY ──────────
    // Each time a search entry beams in from the server, the black box logs it:
    // which entry arrived and which request it belongs to.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called for each search result entry received from the server.
     * Search loggers override this to write each entry's attributes.
     *
     * @param connection          The connection used.
     * @param studioSearchResult  The result entry received.
     * @param requestNum          Correlates this result to its originating request.
     * @param ex                  The LDAP exception if retrieval failed, or {@code null}.
     */
    default void logSearchResultEntry( Connection connection, StudioSearchResult studioSearchResult, long requestNum,
        StudioLdapException ex )
    {
    }


    // ── LOG SEARCH RESULT REFERENCE — BLACK BOX RECORDS A REFERRAL RETURNED ──────
    // The server redirects us (returns a referral in the search results) and the
    // black box records the referral URLs and the full referrals-info context.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when a search result reference (referral) is received during a search.
     * Search loggers override this to record referral URLs in the log.
     *
     * @param connection    The connection used.
     * @param referral      The referral returned by the server.
     * @param referralsInfo Context about all pending and processed referrals.
     * @param requestNum    Correlates this referral to its originating request.
     * @param ex            The LDAP exception if there was a problem, or {@code null}.
     */
    default void logSearchResultReference( Connection connection, Referral referral,
        ReferralsInfo referralsInfo, long requestNum, StudioLdapException ex )
    {
    }


    // ── LOG SEARCH RESULT DONE — BLACK BOX RECORDS THE END OF A SEARCH ───────────
    // The sensor sweep finishes and the black box records the final tally:
    // how many entries came back and the overall outcome.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when a search operation is complete (all entries received or truncated).
     * Search loggers override this to write a summary or close the log file.
     *
     * @param connection  The connection used.
     * @param count       Total number of entries received.
     * @param requestNum  Correlates this done-event to its originating request.
     * @param ex          The LDAP exception if the search failed, or {@code null} on success.
     */
    default void logSearchResultDone( Connection connection, long count, long requestNum, StudioLdapException ex )
    {
    }


    // ── GET MASKED ATTRIBUTES — BLACK BOX REDACTS SENSITIVE FIELDS ───────────────
    // The black box is smart enough to redact sensitive fields (like userPassword)
    // before writing them into the log — you don't want plain-text passwords in a
    // file anyone can read.
    // We read the preference list and return a set of lower-cased attribute names
    // that should be replaced with "***" in logs.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the set of attribute names that must be redacted in log output.
     * We read the {@link ConnectionCoreConstants#PREFERENCE_MODIFICATIONLOGS_MASKED_ATTRIBUTES}
     * preference (a comma-separated list) and return it as a lower-cased string set.
     * Loggers should call this and replace matching attribute values with
     * a placeholder such as {@code "***"}.
     *
     * @return  A set of lower-cased attribute name strings to mask.
     */
    default Set<String> getMaskedAttributes()
    {
        Set<String> maskedAttributes = new HashSet<String>();

        String maskedAttributeString = Platform.getPreferencesService().getString( ConnectionCoreConstants.PLUGIN_ID,
            ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_MASKED_ATTRIBUTES, "", null );
        String[] splitted = maskedAttributeString.split( "," ); //$NON-NLS-1$

        for ( String s : splitted )
        {
            maskedAttributes.add( Strings.toLowerCaseAscii( s ) );
        }

        return maskedAttributes;
    }


    // ── DELETE FILE WITH RETRY — BLACK BOX CLEANS UP OLD LOG FILES ───────────────
    // Windows sometimes locks files briefly after writing, so a plain delete()
    // can fail even though we're done with the file.  We retry up to 6 times
    // with 500 ms pauses — like Han nudging the Falcon's motivator until it works.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Deletes a file with up to 6 retry attempts and 500 ms pauses between tries.
     * This works around Windows file-locking issues that can cause {@code File.delete()}
     * to fail immediately after closing a stream.
     * Loggers call this when rotating or cleaning up old log files.
     *
     * @param file  The file to delete; silently ignored if {@code null} or missing.
     */
    default void deleteFileWithRetry( File file )
    {
        for ( int i = 0; i < 6; i++ )
        {
            if ( file != null && file.exists() )
            {
                if ( file.delete() )
                {
                    break;
                }
                try
                {
                    Thread.sleep( 500L );
                }
                catch ( InterruptedException e )
                {
                }
            }
        }
    }
}
