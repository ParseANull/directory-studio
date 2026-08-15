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


import java.io.Serializable;

import org.apache.directory.studio.ldifparser.model.lines.LdifControlLine;


// ── CLASS: StudioControl — THE FALCON'S WEAPON FIRE CONTROL UNIT ─────────────
// Every weapon on the Millennium Falcon has a fire control unit: a name
// (so the crew knows what it does), an OID identifier (the targeting code),
// a criticality flag (whether missing this causes abort), and an encoded
// value (the targeting parameters in compact binary form).
// This class represents one such LDAP control: a serializable value object
// with pre-built singletons for the most common controls (Subentries,
// ManageDsaIT, TreeDelete) and a toString() that serializes to LDIF format.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents an LDAP control as defined in RFC 4511.
 * LDAP controls are optional extensions attached to requests or responses to
 * modify behavior — for example, asking the server to include subentries,
 * treat referrals as regular entries, or delete a whole subtree at once.
 * Each control has:
 * <ul>
 *   <li>an OID that identifies the control type</li>
 *   <li>a criticality flag: if {@code true}, the server must honor the control or reject the request</li>
 *   <li>an optional binary value (control-specific parameters)</li>
 * </ul>
 * We pre-build singleton instances for the three most common controls so callers
 * don't have to construct them manually each time.
 * Think of this class as the Falcon's fire control unit: named, identified by
 * targeting code (OID), critical or not, with its configuration payload.
 *
 * <pre>
 * Control ::= SEQUENCE {
 *     controlType             LDAPOID,
 *     criticality             BOOLEAN DEFAULT FALSE,
 *     controlValue            OCTET STRING OPTIONAL }
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StudioControl implements Serializable
{

    /** The serialVersionUID. */
    private static final long serialVersionUID = -1289018814649849178L;

    // ── SUBENTRIES CONTROL — RFC 3672 ──────────────────────────────────────────────
    // The Subentries control tells the server to include LDAP subentries in the
    // search scope.  Like asking the briefing officer to also show sub-fleet orders.
    /**
     * Singleton Subentries control (RFC 3672).
     * Including this in a search request causes the server to return LDAP subentries
     * in addition to regular entries within the search scope.
     */
    public static final StudioControl SUBENTRIES_CONTROL = new StudioControl( "Subentries", "1.3.6.1.4.1.4203.1.10.1", //$NON-NLS-1$ //$NON-NLS-2$
        false, new byte[]
            { 0x01, 0x01, ( byte ) 0xFF } );

    // ── MANAGE DSA IT CONTROL — RFC 3296 ──────────────────────────────────────────
    // The ManageDsaIT control tells the server to return referral objects as
    // regular entries instead of chasing them — like saying "show me the sign,
    // not where it points."
    /**
     * Singleton ManageDsaIT control (RFC 3296).
     * Instructs the server to return referral objects as regular entries rather
     * than following the referral.
     */
    public static final StudioControl MANAGEDSAIT_CONTROL = new StudioControl( "Manage DSA IT", //$NON-NLS-1$
        "2.16.840.1.113730.3.4.2", false, null ); //$NON-NLS-1$

    // ── TREE DELETE CONTROL — DRAFT-ARMIJO ────────────────────────────────────────
    // The Tree Delete control asks the server to delete an entire subtree in one
    // shot instead of requiring the client to delete each entry one by one —
    // like using a thermal detonator instead of individual ion blasts.
    /**
     * Singleton Tree Delete control (draft-armijo-ldap-treedelete-02).
     * Instructs the server to recursively delete an entire subtree in a single
     * delete operation.
     */
    public static final StudioControl TREEDELETE_CONTROL = new StudioControl( "Tree Delete", "1.2.840.113556.1.4.805", //$NON-NLS-1$ //$NON-NLS-2$
        false, null );

    /** Human-readable name of the control (for display purposes). */
    protected String name;

    /** The LDAP OID string uniquely identifying this control type. */
    protected String oid;

    /** Whether the control is critical — if true, the server must honor it or reject the request. */
    protected boolean critical;

    /** The BER-encoded control value bytes; may be null if the control has no value. */
    protected byte[] controlValue;


    // ── CONSTRUCTOR (NO-ARG) — BLANK FIRE CONTROL UNIT ───────────────────────────
    // We create an empty control so callers can set fields individually —
    // used when deserializing or constructing a custom control.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an empty {@link StudioControl} with all fields uninitialized.
     * Callers should set the OID, name, criticality, and value via the setters.
     */
    public StudioControl()
    {
    }


    // ── CONSTRUCTOR (FULL) — FULLY ARMED AND OPERATIONAL ─────────────────────────
    // We set every field in one constructor call — name, OID, criticality, value.
    // The pre-built singleton constants use this constructor.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a fully-initialized {@link StudioControl}.
     *
     * @param name          Human-readable name (defaults to empty string if null).
     * @param oid           LDAP OID string identifying this control type.
     * @param critical      Whether the control is marked critical.
     * @param controlValue  BER-encoded control value bytes, or {@code null} if none.
     */
    public StudioControl( String name, String oid, boolean critical, byte[] controlValue )
    {
        super();
        this.name = name == null ? "" : name; //$NON-NLS-1$
        this.oid = oid;
        this.critical = critical;
        this.controlValue = controlValue;
    }


    // ── GET CONTROL VALUE — READ THE TARGETING PARAMETERS ────────────────────────
    // We read the binary targeting parameters packed into this control's payload.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the BER-encoded control value bytes.
     *
     * @return  The control value byte array, or {@code null} if this control has no value.
     */
    public byte[] getControlValue()
    {
        return controlValue;
    }


    // ── GET OID — READ THE TARGETING CODE ─────────────────────────────────────────
    // We return the OID string that uniquely identifies this control type.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP OID string identifying this control type.
     *
     * @return  The OID string.
     */
    public String getOid()
    {
        return oid;
    }


    // ── IS CRITICAL — CHECK IF THIS IS A MUST-HONOR INSTRUCTION ──────────────────
    // We return whether the server must honor this control or abort the request.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this control is critical.
     * If {@code true}, the server must either support and honor the control or
     * return an error — it may not silently ignore it.
     *
     * @return  {@code true} if critical; {@code false} otherwise.
     */
    public boolean isCritical()
    {
        return critical;
    }


    // ── GET NAME — READ THE FIRE CONTROL UNIT'S LABEL ────────────────────────────
    // We return the human-readable name so the UI can display it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable display name of this control.
     *
     * @return  The name string.
     */
    public String getName()
    {
        return name;
    }


    // ── TO STRING — SERIALIZE TO LDIF CONTROL FORMAT ──────────────────────────────
    // We render the control the way an LDIF file would: OID + criticality + value.
    // We strip the "control: " prefix and trailing newline so we get a compact
    // one-line representation for display and equality comparison.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a compact string representation of this control in LDIF format.
     * We strip the "control: " prefix and trailing newline, returning just the
     * OID, criticality, and value portion.  Returns an empty string if the OID is null.
     * This string is also used as the basis for {@link #hashCode()} and {@link #equals(Object)}.
     *
     * @return  An LDIF-style string for this control.
     */
    public String toString()
    {
        if ( oid == null )
        {
            return ""; //$NON-NLS-1$
        }

        LdifControlLine line = LdifControlLine.create( getOid(), isCritical(), getControlValue() );
        String s = line.toRawString();
        s = s.substring( line.getRawControlSpec().length(), s.length() );
        s = s.substring( line.getRawControlType().length(), s.length() );
        s = s.substring( 0, s.length() - line.getRawNewLine().length() );
        return s;
    }


    // ── SET CONTROL VALUE — UPDATE THE TARGETING PARAMETERS ──────────────────────
    // We update the binary payload of this control.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the BER-encoded control value bytes.
     *
     * @param controlValue  The new control value, or {@code null} to clear it.
     */
    public void setControlValue( byte[] controlValue )
    {
        this.controlValue = controlValue;
    }


    // ── SET CRITICAL — MARK AS MUST-HONOR OR OPTIONAL ────────────────────────────
    // We update the criticality flag.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the criticality flag for this control.
     *
     * @param critical  {@code true} to mark the control as critical.
     */
    public void setCritical( boolean critical )
    {
        this.critical = critical;
    }


    // ── SET NAME — RELABEL THE FIRE CONTROL UNIT ──────────────────────────────────
    // We update the human-readable display name.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the human-readable display name for this control.
     *
     * @param name  The new name string.
     */
    public void setName( String name )
    {
        this.name = name;
    }


    // ── SET OID — UPDATE THE TARGETING CODE ───────────────────────────────────────
    // We update the OID that identifies this control type.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the LDAP OID string for this control.
     *
     * @param oid  The new OID string.
     */
    public void setOid( String oid )
    {
        this.oid = oid;
    }


    // ── HASH CODE — CONSISTENT WITH EQUALS ────────────────────────────────────────
    // We hash based on the LDIF string representation — consistent with equals().
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + toString().hashCode();
        return result;
    }


    // ── EQUALS — COMPARE BY LDIF REPRESENTATION ────────────────────────────────────
    // Two controls are equal if their LDIF string representations are equal.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean equals( Object obj )
    {
        if ( !( obj instanceof StudioControl ) )
        {
            return false;
        }

        StudioControl other = ( StudioControl ) obj;

        return this.toString().equals( other.toString() );
    }

}
