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


import java.io.IOException;

import javax.naming.ldap.PagedResultsControl;


// ── CLASS: StudioPagedResultsControl — R2's SENSOR SCAN SET TO PAGE MODE ──────
// R2's sensor array can scan the whole galaxy at once, but that would
// overwhelm the Falcon's computers.  Instead, we configure his scanner for
// "page mode": scan N entries at a time, note the bookmark (cookie) for
// where we stopped, and resume from there on the next scan.
// This class extends StudioControl with the paged-results-specific fields:
// page size, the opaque resumption cookie from the server, and a "scroll mode"
// flag that changes how the UI paginates through results.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Extends {@link StudioControl} with fields specific to the RFC 2696
 * Simple Paged Results control (OID {@code 1.2.840.113556.1.4.319}).
 * When we send a search with this control, the server returns at most {@code size}
 * entries per response and includes an opaque {@code cookie} we must echo back in
 * the next request to continue from where it left off.
 * The {@code isScrollMode} flag lets the UI distinguish between "manual paging"
 * (the user clicks Next Page) and "automatic scroll" (keep loading as the user scrolls).
 * Each time {@code size} or {@code cookie} is changed, we re-encode the raw BER bytes
 * so the parent class's {@code controlValue} stays in sync.
 * Think of this class as R2's scanner in page mode: limited batch size, bookmark
 * for resumption, and a flag for whether the user is scrolling or clicking.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StudioPagedResultsControl extends StudioControl
{

    /** The serialVersionUID. */
    private static final long serialVersionUID = -6219375680879062812L;

    /** The OID of the Simple Paged Results control (RFC 2696). */
    public static final String OID = "1.2.840.113556.1.4.319"; //$NON-NLS-1$

    /** Display name for the Simple Paged Results control. */
    public static final String NAME = "Simple Paged Results"; //$NON-NLS-1$

    /** Maximum entries per page. The server will return at most this many entries per response. */
    private int size;

    /** Opaque resumption cookie returned by the server in its paged response. */
    private byte[] cookie;

    /** True if the UI is in "scroll mode" — continuous loading; false for manual page-by-page. */
    private boolean isScrollMode;


    // ── CONSTRUCTOR (NO-ARG) — BLANK SCANNER CONFIGURATION ───────────────────────
    // We create an uninitialized control — used when deserializing a persisted
    // preferences value.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an uninitialized {@link StudioPagedResultsControl}.
     * Setters must be called before use.
     */
    public StudioPagedResultsControl()
    {
        super();
    }


    // ── CONSTRUCTOR (FULL) — CONFIGURE THE SCANNER IN ONE SHOT ───────────────────
    // We configure page size, cookie, criticality, and scroll mode all at once,
    // then immediately encode the raw BER bytes so the parent class is consistent.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a fully-initialized {@link StudioPagedResultsControl}.
     * Immediately encodes the size and cookie into the parent's {@code controlValue}.
     *
     * @param size          Maximum entries per page (use 0 for server default).
     * @param cookie        Resumption cookie from a previous paged response, or {@code null}
     *                      for the first page.
     * @param critical      Whether the control is critical.
     * @param isScrollMode  {@code true} for continuous scroll; {@code false} for manual paging.
     */
    public StudioPagedResultsControl( int size, byte[] cookie, boolean critical, boolean isScrollMode )
    {
        super( NAME, OID, critical, null );
        this.size = size;
        this.cookie = cookie;
        this.isScrollMode = isScrollMode;

        encode();
    }


    // ── GET SIZE — HOW MANY ENTRIES PER SCAN BATCH ────────────────────────────────
    // We return the configured page size for the scanner.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the configured page size (maximum entries per search response).
     *
     * @return  The page size.
     */
    public int getSize()
    {
        return size;
    }


    // ── SET SIZE — ADJUST THE BATCH SIZE AND RE-ENCODE ────────────────────────────
    // Han adjusts the scanner's batch size.  We re-encode immediately so the
    // BER bytes stay in sync.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the page size and re-encodes the control value.
     *
     * @param size  The new maximum entries per page.
     */
    public void setSize( int size )
    {
        this.size = size;
        encode();
    }


    // ── GET COOKIE — READ THE SCAN RESUMPTION BOOKMARK ────────────────────────────
    // R2 reads the bookmark where the last scan stopped so we can resume there.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the opaque resumption cookie from the server's previous paged response.
     * Pass this back to the server in the next request to continue where it left off.
     *
     * @return  The cookie bytes, or {@code null} for the first page.
     */
    public byte[] getCookie()
    {
        return cookie;
    }


    // ── SET COOKIE — UPDATE THE RESUMPTION BOOKMARK AND RE-ENCODE ─────────────────
    // R2 updates his scan bookmark.  We re-encode immediately.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the resumption cookie and re-encodes the control value.
     *
     * @param cookie  The new cookie bytes from the server's paged response.
     */
    public void setCookie( byte[] cookie )
    {
        this.cookie = cookie;
        encode();
    }


    // ── IS SCROLL MODE — CHECK IF WE'RE IN CONTINUOUS SCROLL MODE ────────────────
    // We check whether the UI is in continuous scroll mode or manual page mode.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the UI is in continuous scroll mode.
     *
     * @return  {@code true} for scroll mode; {@code false} for manual page-by-page.
     */
    public boolean isScrollMode()
    {
        return isScrollMode;
    }


    // ── SET SCROLL MODE — SWITCH BETWEEN SCROLL AND MANUAL PAGING ────────────────
    // We switch the scanner between continuous-scroll and manual-page modes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether the UI should use continuous scroll mode.
     *
     * @param isScrollMode  {@code true} for continuous scroll; {@code false} for manual paging.
     */
    public void setScrollMode( boolean isScrollMode )
    {
        this.isScrollMode = isScrollMode;
    }


    // ── ENCODE — PACK SIZE AND COOKIE INTO BER BYTES ──────────────────────────────
    // We use the JDK's PagedResultsControl to encode our size and cookie into
    // the compact BER wire format, and store it in the parent's controlValue field.
    // On IOException we silently skip — the encode should never fail for valid inputs.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Re-encodes the page size and cookie into the parent's {@code controlValue} field.
     * We use the JDK's {@link PagedResultsControl} to produce the BER encoding.
     * Called automatically whenever {@code size} or {@code cookie} changes.
     */
    private void encode()
    {
        try
        {
            controlValue = new PagedResultsControl( size, cookie, critical ).getEncodedValue();
        }
        catch ( IOException e )
        {
        }
    }


    // ── HASH CODE — CONSISTENT WITH EQUALS (ADDS SCROLL MODE) ─────────────────────
    // We include the scroll mode flag in the hash so scroll-mode controls hash
    // differently from otherwise-identical non-scroll controls.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ( isScrollMode ? 1231 : 1237 );
        return result;
    }


    // ── EQUALS — LDIF REPRESENTATION PLUS SCROLL MODE ─────────────────────────────
    // Two StudioPagedResultsControls are equal if they serialize the same and
    // have the same scroll mode flag.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean equals( Object obj )
    {
        if ( !( obj instanceof StudioPagedResultsControl ) )
        {
            return false;
        }

        StudioPagedResultsControl other = ( StudioPagedResultsControl ) obj;

        return this.toString().equals( other.toString() ) && this.isScrollMode == other.isScrollMode;
    }

}
