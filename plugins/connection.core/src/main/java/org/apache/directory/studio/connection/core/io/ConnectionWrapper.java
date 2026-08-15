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
package org.apache.directory.studio.connection.core.io;


import java.util.Collection;

import javax.naming.directory.SearchControls;
import javax.net.ssl.SSLSession;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.ExtendedRequest;
import org.apache.directory.api.ldap.model.message.ExtendedResponse;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.connection.core.Connection.ReferralHandlingMethod;
import org.apache.directory.studio.connection.core.ReferralsInfo;
import org.apache.directory.studio.connection.core.io.api.StudioSearchResultEnumeration;


// ── CLASS: ConnectionWrapper — THE MILLENNIUM FALCON'S HYPERDRIVE UNIT ─────────
// The Falcon's hyperdrive is the actual engine that does the work: connecting
// to hyperspace (the LDAP server), firing weapons (sending requests), and
// returning sensor data (search results).  Han pilots it, but the hyperdrive
// unit is what makes the jump happen.
// This interface is the hyperdrive API: every real directory connection
// implementation (currently the Apache Directory API wrapper) plugs in here.
// The Connection model class delegates all actual LDAP wire operations to its
// ConnectionWrapper, which means the core model never touches sockets directly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Interface representing the physical connection to an LDAP directory server.
 * Every {@link org.apache.directory.studio.connection.core.Connection} has one
 * {@code ConnectionWrapper} that handles all LDAP wire protocol operations:
 * connecting, authenticating, searching, modifying, creating, deleting, and renaming.
 * The only production implementation is {@code DirectoryApiConnectionWrapper}
 * (in the io.api subpackage), which uses the Apache Directory API.
 * Think of this interface as the Falcon's hyperdrive unit: the {@link Connection}
 * model is the ship; this wrapper is the engine that makes it fly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ConnectionWrapper
{
    // ── CONNECT — ESTABLISH THE NETWORK CONNECTION ─────────────────────────────────
    // Han punches the hyperdrive button and the Falcon leaps into hyperspace.
    // We open the TCP/TLS socket to the server but don't authenticate yet.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Establishes the network connection to the directory server without authenticating.
     * For TLS connections, the TLS handshake (including certificate verification) happens here.
     * On failure, the monitor receives the error.
     *
     * @param monitor  Progress monitor for cancellation and error reporting.
     */
    void connect( StudioProgressMonitor monitor );


    // ── DISCONNECT — CUT THE ENGINES ──────────────────────────────────────────────
    // Han cuts the Falcon's engines and they drop out of hyperspace.
    // We close the socket and clean up the underlying connection resources.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Closes the network connection to the server.
     * Resources (sockets, buffers) are released.
     */
    void disconnect();


    // ── BIND — AUTHENTICATE AGAINST THE SERVER ────────────────────────────────────
    // Han presents his access credentials at the Imperial checkpoint.
    // We send the LDAP bind request using the connection's configured auth method.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Authenticates (binds) against the directory server using the credentials
     * configured in the connection parameters.
     * For anonymous binds, no credentials are sent.
     * On failure, the monitor receives the error.
     *
     * @param monitor  Progress monitor for cancellation and error reporting.
     */
    void bind( StudioProgressMonitor monitor );


    // ── UNBIND — SIGN OUT ─────────────────────────────────────────────────────────
    // Han signs out from the Imperial checkpoint, releasing his session.
    // We send an LDAP unbind request.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sends an LDAP unbind request, terminating the authenticated session.
     * After unbinding, the underlying connection is typically closed.
     */
    void unbind();


    // ── IS CONNECTED — CHECK IF THE ENGINE IS RUNNING ─────────────────────────────
    // We check whether the socket is open and the connection is live.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the underlying connection is currently open and active.
     *
     * @return  {@code true} if connected; {@code false} otherwise.
     */
    boolean isConnected();


    // ── IS SECURED — CHECK IF THE SHIELDS ARE UP ──────────────────────────────────
    // We check whether TLS/LDAPS is protecting the connection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the connection is protected by TLS (LDAPS or StartTLS).
     *
     * @return  {@code true} if TLS is active; {@code false} for a plain connection.
     */
    boolean isSecured();


    // ── GET SSL SESSION — READ THE TLS SESSION DETAILS ────────────────────────────
    // We return the active TLS session so callers can inspect the certificate chain
    // and cipher suite.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the active {@link SSLSession} if the connection is secured by TLS.
     * Useful for inspecting the server certificate chain or the negotiated cipher suite.
     *
     * @return  The {@link SSLSession}, or {@code null} if the connection is not secured.
     */
    SSLSession getSslSession();


    // ── SET BINARY ATTRIBUTES — TELL THE CODEC WHICH ATTRS ARE BINARY ─────────────
    // We tell the connection which attribute types contain raw binary values so
    // it can handle them correctly instead of trying to parse them as strings.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Registers the set of attribute type names that contain binary (non-string) values.
     * The connection wrapper uses this to correctly encode/decode attribute values
     * on the wire.
     *
     * @param binaryAttributes  Collection of attribute type name strings.
     */
    void setBinaryAttributes( Collection<String> binaryAttributes );


    // ── SEARCH — FIRE THE SENSOR ARRAY ────────────────────────────────────────────
    // R2's sensor array fires off a scan: target coordinates, scope, filter, limits.
    // We send an LDAP search request and return a lazy enumeration of results.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes an LDAP search and returns a lazy enumeration of matching entries.
     * Returns {@code null} if an unrecoverable error occurs (the monitor will have the error).
     *
     * @param searchBase                  The DN string to search under.
     * @param filter                      The LDAP filter expression.
     * @param searchControls              Scope, size limit, time limit, and attribute list.
     * @param aliasesDereferencingMethod  How alias entries should be dereferenced.
     * @param referralsHandlingMethod     Whether to follow, ignore, or treat-as-entries referrals.
     * @param controls                    Optional LDAP controls to attach to the request.
     * @param monitor                     Progress monitor for cancellation and error reporting.
     * @param referralsInfo               Tracks referrals encountered during the operation.
     * @return  A {@link StudioSearchResultEnumeration}, or {@code null} on failure.
     */
    StudioSearchResultEnumeration search( final String searchBase, final String filter,
        final SearchControls searchControls, final AliasDereferencingMethod aliasesDereferencingMethod,
        final ReferralHandlingMethod referralsHandlingMethod, final Control[] controls,
        final StudioProgressMonitor monitor, final ReferralsInfo referralsInfo );


    // ── MODIFY ENTRY — FIRE THE MODIFY CANNONS ────────────────────────────────────
    // Han fires the modify cannons at the target entry, applying the attribute
    // change list.  We send an LDAP modify request.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sends an LDAP modify request to update attributes of the named entry.
     *
     * @param dn             The DN of the entry to modify.
     * @param modifications  The list of attribute modifications to apply.
     * @param controls       Optional LDAP controls.
     * @param monitor        Progress monitor for cancellation and error reporting.
     * @param referralsInfo  Referral tracking context.
     */
    void modifyEntry( final Dn dn, final Collection<Modification> modifications, final Control[] controls,
        final StudioProgressMonitor monitor, final ReferralsInfo referralsInfo );


    // ── RENAME ENTRY — MOVE THE SHIP TO NEW COORDINATES ──────────────────────────
    // Han plots a new jump to new coordinates — the entry moves to a new position
    // in the directory tree.  We send an LDAP modDN request.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sends an LDAP modDN (rename or move) request.
     *
     * @param oldDn          The current DN of the entry.
     * @param newDn          The target DN after the operation.
     * @param deleteOldRdn   Whether the old RDN attribute value should be removed.
     * @param controls       Optional LDAP controls.
     * @param monitor        Progress monitor for cancellation and error reporting.
     * @param referralsInfo  Referral tracking context.
     */
    void renameEntry( final Dn oldDn, final Dn newDn, final boolean deleteOldRdn,
        final Control[] controls, final StudioProgressMonitor monitor, final ReferralsInfo referralsInfo );


    // ── CREATE ENTRY — ADD A NEW SHIP TO THE DIRECTORY ────────────────────────────
    // Han registers a new ship in the Imperial registry — we send an LDAP add request.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sends an LDAP add request to create a new entry.
     *
     * @param entry          The entry to create (includes DN and all attributes).
     * @param controls       Optional LDAP controls.
     * @param monitor        Progress monitor for cancellation and error reporting.
     * @param referralsInfo  Referral tracking context.
     */
    void createEntry( final Entry entry, final Control[] controls, final StudioProgressMonitor monitor,
        final ReferralsInfo referralsInfo );


    // ── DELETE ENTRY — REMOVE A SHIP FROM THE REGISTRY ────────────────────────────
    // Han fires a proton torpedo at the target — we send an LDAP delete request.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sends an LDAP delete request to remove the named entry.
     *
     * @param dn             The DN of the entry to delete.
     * @param controls       Optional LDAP controls (e.g. tree-delete for subtrees).
     * @param monitor        Progress monitor for cancellation and error reporting.
     * @param referralsInfo  Referral tracking context.
     */
    void deleteEntry( final Dn dn, final Control[] controls, final StudioProgressMonitor monitor,
        final ReferralsInfo referralsInfo );


    // ── EXTENDED — SEND A CUSTOM LDAP EXTENDED OPERATION ─────────────────────────
    // Han sends a custom command to the server (e.g. StartTLS, password modify).
    // We send an LDAP extended request and return the server's response.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sends an LDAP extended operation request and returns the server's response.
     * Extended operations are custom commands beyond the standard LDAP set
     * (e.g. StartTLS, Password Modify, Who Am I).
     *
     * @param request  The extended request to send.
     * @param monitor  Progress monitor for cancellation and error reporting.
     * @return  The server's {@link ExtendedResponse}.
     */
    ExtendedResponse extended( ExtendedRequest request, final StudioProgressMonitor monitor );

}
