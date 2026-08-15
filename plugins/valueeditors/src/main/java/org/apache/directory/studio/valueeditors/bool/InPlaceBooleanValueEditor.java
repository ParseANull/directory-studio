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

package org.apache.directory.studio.valueeditors.bool;


import org.apache.directory.api.ldap.model.schema.syntaxCheckers.BooleanSyntaxChecker;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.valueeditors.AbstractInPlaceStringValueEditor;
import org.eclipse.jface.viewers.ICellEditorValidator;


// ── CLASS: InPlaceBooleanValueEditor — HAN SOLO "NEVER TELL ME THE ODDS" ─────
// In the Hoth asteroid field, Han Solo faces a binary choice: fly into the rocks
// or turn back.  "Never tell me the odds" — he accepts any input that can be
// interpreted as yes-or-no (T, TRUE, Y, YES, 1 for yes; F, FALSE, N, NO, 0 for no)
// and snaps it into the only two valid LDAP outputs: "TRUE" or "FALSE".
// LDAP booleans are not Java booleans — they are literally the strings "TRUE" or
// "FALSE" (RFC 4517 syntax 1.3.6.1.4.1.1466.115.121.1.7).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * In-place (no dialog) value editor for LDAP Boolean syntax
 * (OID 1.3.6.1.4.1.1466.115.121.1.7).
 * LDAP booleans are the literal strings {@code "TRUE"} or {@code "FALSE"}.
 * This editor accepts a variety of shorthand inputs (T/F, Y/N, 1/0, TRUE/FALSE)
 * and normalises them to the correct LDAP form.  An empty value defaults to TRUE.
 * Validation rejects anything that can't be mapped to a valid boolean.
 * Think of this as Han's asteroid-field decision module: take anything that means
 * yes-or-no, snap it to exactly one of two outputs.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InPlaceBooleanValueEditor extends AbstractInPlaceStringValueEditor
{
    /** The 'TRUE' value */
    private static final String TRUE = "TRUE";

    /** The 'FALSE' value */
    private static final String FALSE = "FALSE";

    // ── Han Locks In His Response Mode ────────────────────────────────────────
    // Han calibrates the Falcon's nav system to accept any reasonable yes/no input
    // but reject nonsense — "gorpa" is not a valid course heading.
    // We wire up an ICellEditorValidator that accepts the boolean shorthand
    // aliases and rejects everything else.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the editor and attaches a validator that accepts the boolean
     * shorthand aliases ({@code T}, {@code F}, {@code Y}, {@code N},
     * {@code 1}, {@code 0}, {@code TRUE}, {@code FALSE}, {@code YES}, {@code NO},
     * and empty string which defaults to TRUE).
     * Anything else causes the cell editor to report an error.
     */
    public InPlaceBooleanValueEditor()
    {
        super();

        setValidator(new ICellEditorValidator()
        {
            @Override
            public String isValid( Object value )
            {
                if ( value instanceof String )
                {
                    String stringValue = ( ( String ) value ).toUpperCase();

                    switch ( stringValue )
                    {
                        case "F" :
                        case "FALSE" :
                        case "N" :
                        case "NO" :
                        case "0" :
                        case "T" :
                        case "TRUE" :
                        case "Y" :
                        case "YES" :
                        case "1" :
                        case "" :           // Special case : default to TRUE
                            return null;

                        default :
                            return "Invalid boolean";
                    }
                }
                else
                {
                    return "Invalid boolean";
                }
            }
        });
    }


    // ── Han Reads His Own Answer and Locks It to Yes or No ───────────────────
    // After Han types his navigation choice, the Falcon's computer normalises it:
    // "Y" becomes "YES" (and then "TRUE"), "n" becomes "FALSE", and so on.
    // We case-insensitively map all boolean aliases to exactly "TRUE" or "FALSE".
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the current text from the in-place editor and normalises it to
     * the LDAP boolean strings {@code "TRUE"} or {@code "FALSE"}.
     * Shorthand aliases like {@code T}, {@code Y}, {@code 1} become {@code "TRUE"};
     * {@code F}, {@code N}, {@code 0} become {@code "FALSE"}.
     * An empty string also maps to {@code "TRUE"} (sensible default).
     * Anything unrecognisable is returned as-is so the validator can flag it.
     *
     * <p>For example — Han's nav computer normalises the input:</p>
     * <pre>
     *   "y"  → "TRUE"
     *   "no" → "FALSE"
     *   "1"  → "TRUE"
     *   ""   → "TRUE"   (default)
     * </pre>
     *
     * @return  {@code "TRUE"}, {@code "FALSE"}, or the raw string if unrecognised.
     */
    @Override
    protected Object doGetValue()
    {
        Object value = super.doGetValue();

        if ( value instanceof String )
        {
            String stringValue = ( String ) value;

            switch ( stringValue.toUpperCase() )
            {
                case "F" :
                case "FALSE" :
                case "N" :
                case "NO" :
                case "0" :
                    return FALSE;

                case "T" :
                case "TRUE" :
                case "Y" :
                case "YES" :
                case "1" :
                case "" :           // Special case : default to TRUE
                    return TRUE;

                default :
                    return stringValue;
            }
        }

        return value;
    }


    // ── Han Checks Whether His Answer Is a Real Yes-or-No ────────────────────
    // Before filing the nav course, the Falcon checks: did Han actually give a
    // valid answer, or is it gibberish?  A non-null normalised value means valid.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the current editor value can be normalised to a
     * valid LDAP boolean ({@code "TRUE"} or {@code "FALSE"}).
     * Simply checks that {@link #doGetValue()} returns non-null.
     *
     * @return  {@code true} if the value is valid; {@code false} otherwise.
     */
    @Override
    public boolean isValueValid()
    {
        return doGetValue() != null;
    }


    // ── Han Reads the Stored LDAP Boolean ────────────────────────────────────
    // Before Han can edit his course, the nav system reads the existing boolean
    // from the Falcon's records.  If it's a valid LDAP boolean it's handed back;
    // if the attribute is empty (new entry) it defaults to TRUE.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw LDAP boolean string from the given attribute value, suitable
     * for pre-filling the in-place editor.
     * If the existing value is a syntactically valid LDAP boolean (or empty), we
     * return it as-is.  If the attribute has no value ({@code null}), we return
     * {@code "TRUE"} as a sensible default.  Any other content (non-boolean string)
     * returns {@code null} to signal that this editor can't handle it.
     *
     * <p>For example — the nav system reads the stored boolean:</p>
     * <pre>
     *   IValue v = ...;  // contains "FALSE"
     *   Object raw = editor.getRawValue(v);  // → "FALSE"
     *
     *   IValue empty = ...;  // null value
     *   Object raw = editor.getRawValue(empty);  // → "TRUE" (default)
     * </pre>
     *
     * @param value  The LDAP attribute value to inspect.
     * @return       The raw boolean string, {@code "TRUE"} as default, or {@code null}
     *               if the value is not a boolean.
     */
    @Override
    public Object getRawValue( IValue value )
    {
        Object rawValue = super.getRawValue( value );

        if ( rawValue instanceof String )
        {
            String stringValue = ( String ) rawValue;

            if ( ( stringValue.length() == 0 ) || ( BooleanSyntaxChecker.INSTANCE.isValidSyntax( stringValue ) ) )
            {
                return rawValue;
            }
            else
            {
                return null;
            }
        }
        else if ( rawValue == null )
        {
            return TRUE;
        }
        else
        {
            return null;
        }
    }
}
