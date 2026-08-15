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

package org.apache.directory.studio.ldifparser.model.lines;


import org.apache.directory.studio.ldifparser.LdifParserConstants;
import org.apache.directory.studio.ldifparser.LdifUtils;


// ── CLASS: LdifControlLine — IMPERIAL SECURITY BUREAU CONTROL DIRECTIVE ──────
// Before the main operation in a change record, the Imperial Security Bureau
// may attach one or more control directives: an OID that identifies the
// extension, an optional criticality flag, and an optional encoded value.
// If the criticality is "true" and the server doesn't recognise the OID,
// the operation must be rejected.
// LdifControlLine models that control directive: extends LdifValueLineBase
// (reusing lineStart=controlSpec, valueType=controlType, value=OID) and adds
// three extra fields (criticality, controlValueType, controlValue) for the
// segments that follow the OID on the same line.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF {@code control: <oid> [criticality] [:: <base64value>]} line.
 * Extends {@link LdifValueLineBase} where the line-start segment is the
 * {@code "control"} keyword, the value-type is the separator after
 * {@code "control"}, and the value is the OID.  Three additional fields
 * represent the optional criticality, control-value separator, and control
 * value.
 * Factory methods ({@link #create(String, String, String)},
 * {@link #create(String, boolean, String)}, etc.) cover the common variants.
 * Think of this as the ISB control directive — an extension the server must
 * honour (or reject the operation if it cannot).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifControlLine extends LdifValueLineBase
{
    /** Optional raw criticality string ({@code " true"} or {@code " false"}). */
    private String rawCriticality;

    /** Optional separator before the control value ({@code ":"} or {@code "::"}). */
    private String rawControlValueType;

    /** Optional raw control value (may be Base64-encoded). */
    private String rawControlValue;


    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a control line with all raw segments.
     *
     * @param offset                byte offset in the document
     * @param rawControlSpec        the raw {@code "control"} keyword
     * @param rawControlType        the separator after {@code "control"}
     * @param rawOid                the raw OID string
     * @param rawCriticality        the optional raw criticality string, or {@code null}
     * @param rawControlValueType   the optional raw value separator, or {@code null}
     * @param rawControlValue       the optional raw control value, or {@code null}
     * @param rawNewLine            the raw line-ending characters
     */
    public LdifControlLine( int offset, String rawControlSpec, String rawControlType, String rawOid,
        String rawCriticality, String rawControlValueType, String rawControlValue, String rawNewLine )
    {
        super( offset, rawControlSpec, rawControlType, rawOid, rawNewLine );
        this.rawCriticality = rawCriticality;
        this.rawControlValueType = rawControlValueType;
        this.rawControlValue = rawControlValue;
    }


    // ── CONTROL SPEC ACCESSORS ────────────────────────────────────────────────
    /**
     * Returns the raw {@code "control"} keyword.
     *
     * @return the raw control-spec segment
     */
    public String getRawControlSpec()
    {
        return super.getRawLineStart();
    }


    /**
     * Returns the unfolded {@code "control"} keyword.
     *
     * @return the unfolded control-spec segment
     */
    public String getUnfoldedControlSpec()
    {
        return super.getUnfoldedLineStart();
    }


    /**
     * Returns the raw control type separator.
     *
     * @return the raw control-type segment
     */
    public String getRawControlType()
    {
        return super.getRawValueType();
    }


    /**
     * Returns the unfolded control type separator.
     *
     * @return the unfolded control-type segment
     */
    public String getUnfoldedControlType()
    {
        return super.getUnfoldedValueType();
    }


    // ── OID ACCESSORS ─────────────────────────────────────────────────────────
    /**
     * Returns the raw OID string.
     *
     * @return the raw OID
     */
    public String getRawOid()
    {
        return super.getRawValue();
    }


    /**
     * Returns the unfolded OID string.
     *
     * @return the unfolded OID
     */
    public String getUnfoldedOid()
    {
        return super.getUnfoldedValue();
    }


    // ── CRITICALITY ACCESSORS ─────────────────────────────────────────────────
    /**
     * Returns the raw criticality string (e.g. {@code " true"}), or empty
     * string if not present.
     *
     * @return the raw criticality segment, never {@code null}
     */
    public String getRawCriticality()
    {
        return getNonNull( rawCriticality );
    }


    /**
     * Returns the unfolded criticality string.
     *
     * @return the unfolded criticality segment
     */
    public String getUnfoldedCriticality()
    {
        return unfold( getRawCriticality() );
    }


    /**
     * Returns {@code true} if the criticality ends with {@code "true"}.
     *
     * @return {@code true} for critical controls
     */
    public boolean isCritical()
    {
        return getUnfoldedCriticality().endsWith( "true" ); //$NON-NLS-1$
    }


    // ── CONTROL VALUE ACCESSORS ───────────────────────────────────────────────
    /**
     * Returns the raw control value separator, or empty string if not present.
     *
     * @return the raw control value type
     */
    public String getRawControlValueType()
    {
        return getNonNull( rawControlValueType );
    }


    /**
     * Returns the unfolded control value separator.
     *
     * @return the unfolded control value type
     */
    public String getUnfoldedControlValueType()
    {
        return unfold( getRawControlValueType() );
    }


    /**
     * Returns the raw control value string, or empty string if not present.
     *
     * @return the raw control value
     */
    public String getRawControlValue()
    {
        return getNonNull( rawControlValue );
    }


    /**
     * Returns the unfolded control value string.
     *
     * @return the unfolded control value
     */
    public String getUnfoldedControlValue()
    {
        return unfold( getRawControlValue() );
    }


    // ── SERIALISATION ─────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Concatenates all seven raw segments plus the newline.</p>
     */
    public String toRawString()
    {
        return getRawControlSpec() + getRawControlType() + getRawOid() + getRawCriticality()
            + getRawControlValueType() + getRawControlValue() + getRawNewLine();
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Valid when: controlSpec and controlType and OID are non-empty; if
     * criticality is present it must be {@code "true"} or {@code "false"};
     * if a control value is present both the type and value must be present;
     * and the newline must be present.</p>
     */
    public boolean isValid()
    {
        return getUnfoldedControlSpec().length() > 0
            && getUnfoldedControlType().length() > 0
            && getUnfoldedOid().length() > 0
            && ( rawCriticality == null || getUnfoldedCriticality().endsWith( "true" ) || this //$NON-NLS-1$
                .getUnfoldedCriticality().endsWith( "false" ) ) //$NON-NLS-1$
            && ( ( rawControlValueType == null && rawControlValue == null ) || ( rawControlValueType != null && rawControlValue != null ) )
            && getUnfoldedNewLine().length() > 0;
    }


    /**
     * {@inheritDoc}
     *
     * <p>Returns a descriptive message for the first invalid segment.</p>
     */
    public String getInvalidString()
    {
        if ( getUnfoldedControlSpec().length() == 0 )
        {
            return "Missing 'control'";
        }
        else if ( getUnfoldedOid().length() == 0 )
        {
            return "Missing OID";
        }
        else if ( ( rawCriticality != null && !getUnfoldedCriticality().endsWith( "true" ) && !this //$NON-NLS-1$
            .getUnfoldedCriticality().endsWith( "false" ) ) ) //$NON-NLS-1$
        {
            return "Invalid criticality, must be 'true' or 'false'";
        }
        else
        {
            return super.getInvalidString();
        }
    }


    // ── DECODED CONTROL VALUE ─────────────────────────────────────────────────
    /**
     * Returns the decoded control value as a byte array.
     *
     * @return the UTF-8 bytes of the decoded control value (never {@code null})
     */
    public final byte[] getControlValueAsBinary()
    {
        Object o = getControlValueAsObject();
        if ( o instanceof String )
        {
            return LdifUtils.utf8encode( ( String ) o );
        }
        else if ( o instanceof byte[] )
        {
            return ( byte[] ) o;
        }
        else
        {
            return new byte[0];
        }
    }


    /**
     * Returns the decoded control value as a String or byte[] depending on
     * the control value type.
     *
     * @return the decoded value, or {@code null}
     */
    public final Object getControlValueAsObject()
    {
        if ( isControlValueTypeSafe() )
        {
            return getUnfoldedControlValue();
        }
        else if ( isControlValueTypeBase64() )
        {
            return LdifUtils.base64decodeToByteArray( getUnfoldedControlValue() );
        }
        else
        {
            return null;
        }
    }


    /**
     * Returns {@code true} if the control value separator is {@code "::"}.
     *
     * @return {@code true} for Base64-encoded control values
     */
    public boolean isControlValueTypeBase64()
    {
        return getUnfoldedControlValueType().startsWith( "::" ); //$NON-NLS-1$
    }


    /**
     * Returns {@code true} if the control value separator is plain {@code ":"}
     * (not Base64).
     *
     * @return {@code true} for plain string control values
     */
    public boolean isControlValueTypeSafe()
    {
        return getUnfoldedControlValueType().startsWith( ":" ) && !isControlValueTypeBase64(); //$NON-NLS-1$
    }


    // ── FACTORY METHODS ───────────────────────────────────────────────────────
    /**
     * Creates a control line for {@code oid} with optional string criticality
     * and string control value.  Applies Base64 encoding automatically.
     *
     * @param oid           the LDAP control OID
     * @param criticality   the raw criticality string (e.g. {@code " true"}),
     *                      or {@code null}
     * @param controlValue  the string control value, or {@code null}
     * @return a new {@link LdifControlLine}
     */
    public static LdifControlLine create( String oid, String criticality, String controlValue )
    {
        if ( LdifUtils.mustEncode( controlValue ) )
        {
            return create( oid, criticality, LdifUtils.utf8encode( controlValue ) );
        }
        else
        {
            LdifControlLine controlLine = new LdifControlLine( 0, "control", ":", oid, criticality, //$NON-NLS-1$ //$NON-NLS-2$
                controlValue != null ? ":" : null, controlValue != null ? controlValue : null, //$NON-NLS-1$
                LdifParserConstants.LINE_SEPARATOR );
            return controlLine;
        }
    }


    /**
     * Creates a control line for {@code oid} with optional string criticality
     * and binary control value.
     *
     * @param oid           the LDAP control OID
     * @param criticality   the raw criticality string, or {@code null}
     * @param controlValue  the binary control value, or {@code null}
     * @return a new {@link LdifControlLine}
     */
    public static LdifControlLine create( String oid, String criticality, byte[] controlValue )
    {
        LdifControlLine controlLine = new LdifControlLine( 0, "control", ":", oid, criticality, controlValue != null //$NON-NLS-1$ //$NON-NLS-2$
            && controlValue.length > 0 ? "::" : null, controlValue != null && controlValue.length > 0 ? LdifUtils //$NON-NLS-1$
            .base64encode( controlValue ) : null, LdifParserConstants.LINE_SEPARATOR );
        return controlLine;
    }


    /**
     * Creates a control line for {@code oid} with a boolean criticality and
     * a string control value.
     *
     * @param oid           the LDAP control OID
     * @param isCritical    {@code true} to mark this control as critical
     * @param controlValue  the string control value, or {@code null}
     * @return a new {@link LdifControlLine}
     */
    public static LdifControlLine create( String oid, boolean isCritical, String controlValue )
    {
        return create( oid, isCritical ? " true" : " false", controlValue ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    /**
     * Creates a control line for {@code oid} with a boolean criticality and
     * a binary control value.
     *
     * @param oid           the LDAP control OID
     * @param isCritical    {@code true} to mark this control as critical
     * @param controlValue  the binary control value, or {@code null}
     * @return a new {@link LdifControlLine}
     */
    public static LdifControlLine create( String oid, boolean isCritical, byte[] controlValue )
    {
        return create( oid, isCritical ? " true" : " false", controlValue ); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
