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


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.util.Asn1Buffer;
import org.apache.directory.api.ldap.codec.api.ControlFactory;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.codec.api.LdapApiServiceFactory;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.controls.ManageDsaIT;
import org.apache.directory.api.ldap.model.message.controls.ManageDsaITImpl;
import org.apache.directory.api.ldap.model.message.controls.OpaqueControl;
import org.apache.directory.api.ldap.model.message.controls.PagedResults;
import org.apache.directory.api.ldap.model.message.controls.PagedResultsImpl;
import org.apache.directory.api.ldap.model.message.controls.Subentries;
import org.apache.directory.api.ldap.model.message.controls.SubentriesImpl;


// ── CLASS: Controls — THE FALCON'S WEAPON AND SHIELD PRESET PANEL ─────────────
// When Han needs to shoot first, he doesn't fumble with the targeting computer —
// the weapon presets are already configured on a panel: ion cannons locked,
// shields up, tree-delete torpedo armed and ready.
// This class is that preset panel for LDAP controls: pre-built singleton
// instances of the most commonly used controls, plus factory methods for
// creating paged-results controls with custom parameters.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class that provides pre-built singleton instances of common LDAP controls
 * and factory methods for building parameterized controls.
 * LDAP controls are optional extensions attached to requests that modify server
 * behavior — for example paged results (return entries in pages instead of all at once),
 * subentries (include LDAP subentries in search results), or ManageDsaIT (treat
 * referrals as regular entries instead of chasing them).
 * Keeping singletons here avoids recreating them on every request.
 * Think of this class as the Falcon's preset weapon panel: the common weapons are
 * pre-loaded and ready; we just reach out and grab the one we need.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Controls
{

    // ── SUBENTRIES CONTROL — "SHOW ME THE SUBENTRIES TOO" ─────────────────────────
    // Like asking the rebel briefing officer to show sub-fleet orders as well as
    // main orders.  We flip the visibility flag to true (include subentries).
    /** Singleton Subentries control (RFC 3672) with visibility set to {@code true}. */
    public static final Subentries SUBENTRIES_CONTROL = new SubentriesImpl();
    static
    {
        SUBENTRIES_CONTROL.setVisibility( true );
    }

    // ── MANAGE DSA IT CONTROL — "TREAT REFERRALS AS REGULAR ENTRIES" ──────────────
    // Instead of following a referral to another server, we want to inspect
    // the referral entry itself — like asking "show me the redirect sign,
    // not what it's pointing to."
    /** Singleton ManageDsaIT control (RFC 3296). */
    public static final ManageDsaIT MANAGEDSAIT_CONTROL = new ManageDsaITImpl();

    // ── TREE DELETE CONTROL — "DELETE THE WHOLE SUBTREE IN ONE SHOT" ──────────────
    // Instead of deleting entries one by one, the tree-delete control lets us
    // nuke an entire subtree — like using the Death Star instead of individual
    // TIE fighters to clear a sector.
    /** Singleton Tree Delete control (draft-armijo-ldap-treedelete-02, OID 1.2.840.113556.1.4.805). */
    public static final Control TREEDELETE_CONTROL = new OpaqueControl( "1.2.840.113556.1.4.805", false );


    // ── NEW PAGED RESULTS CONTROL (SIZE) — SET UP A PAGED SCAN ───────────────────
    // We configure the scanner to beam back results in chunks of {@code size}
    // entries per pass — like telling R2 to scan only a section of the galaxy at a time.
    // Creates a fresh PagedResults control with the given page size and no cookie.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new paged-results control for the first page of a search.
     * The paged-results control (RFC 2696) tells the server to return at most
     * {@code size} entries per request. We use this to avoid blowing up memory
     * when searching large directories.
     *
     * @param size  Maximum entries per page. Use 0 to request the server's default.
     * @return  A new {@link PagedResults} control set to the given size.
     */
    public static final PagedResults newPagedResultsControl( int size )
    {
        PagedResults control = new PagedResultsImpl();
        control.setSize( size );
        return control;
    }


    // ── NEW PAGED RESULTS CONTROL (SIZE + COOKIE) — CONTINUE A PAGED SCAN ────────
    // R2's scanner resumes exactly where it left off — we send the cookie the
    // server gave us last time so it knows which page we're on.
    // Creates a PagedResults control with the given size and the resume cookie.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new paged-results control for a subsequent page of a search.
     * We include the opaque {@code cookie} returned by the server in the previous
     * response so the server knows where to resume.
     *
     * @param size    Maximum entries per page.
     * @param cookie  The opaque continuation cookie from the previous paged response.
     * @return  A new {@link PagedResults} control set to the given size and cookie.
     */
    public static final PagedResults newPagedResultsControl( int size, byte[] cookie )
    {
        PagedResults control = new PagedResultsImpl();
        control.setSize( size );
        control.setCookie( cookie );
        return control;
    }


    // ── CREATE — DECODES A RAW CONTROL FROM OID + VALUE ───────────────────────────
    // The server beams back an encoded control OID and value bytes; we look up
    // the right factory in the codec registry and decode it into a typed object.
    // Throws RuntimeException if decoding fails (should be rare — known OIDs decode fine).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Decodes a raw LDAP control from its OID, criticality flag, and BER-encoded value bytes.
     * We look up the registered {@link ControlFactory} in the Apache Directory API codec
     * and use it to parse the value into a strongly-typed {@link Control} object.
     *
     * @param oid         The OID string of the control (e.g. {@code "1.2.840.113556.1.4.319"}).
     * @param isCritical  Whether the control is marked critical.
     * @param value       The BER-encoded control value bytes.
     * @return  The decoded {@link Control} instance.
     * @throws RuntimeException  If decoding fails (wraps the underlying {@link DecoderException}).
     */
    public static Control create( String oid, boolean isCritical, byte[] value )
    {
        try
        {
            LdapApiService codec = LdapApiServiceFactory.getSingleton();
            ControlFactory<? extends Control> factory = codec.getRequestControlFactories().get( oid );
            Control control = factory.newControl();
            control.setCritical( isCritical );
            factory.decodeValue( control, value );
            return control;
        }
        catch ( DecoderException e )
        {
            throw new RuntimeException( e );
        }
    }


    // ── GET ENCODED VALUE — ENCODES A CONTROL BACK TO RAW BYTES ──────────────────
    // We need to send a control back out to the server, so we look up its factory
    // and encode the value back to raw bytes — like re-sealing a message in its
    // original encryption before transmitting.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Encodes a {@link Control}'s value to a BER-encoded byte array.
     * We use this when we need to persist or retransmit a control value that
     * was originally decoded from a server response.
     *
     * @param control  The control whose value we want to encode.
     * @return  The BER-encoded control value as a byte array.
     */
    public static byte[] getEncodedValue( Control control )
    {
        LdapApiService codec = LdapApiServiceFactory.getSingleton();
        ControlFactory<? extends Control> factory = codec.getRequestControlFactories().get( control.getOid() );
        Asn1Buffer buffer = new Asn1Buffer();
        factory.encodeValue( buffer, control );
        byte[] bytes = buffer.getBytes().array();
        return bytes;
    }
}
