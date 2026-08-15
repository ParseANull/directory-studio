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

package org.apache.directory.studio.valueeditors.oid;


import org.apache.directory.api.ldap.model.schema.syntaxCheckers.OidSyntaxChecker;
import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.valueeditors.AbstractInPlaceStringValueEditor;


// ── CLASS: InPlaceOidValueEditor — C-3PO TRANSLATING A CRYPTIC DROID SERIAL ──
// When handed the identification code "1.3.6.1.4.1.1466.115.121.1.38", C-3PO
// doesn't just recite the number — he consults his fleet registry and announces
// "OID Syntax" alongside the raw serial.  If the registry holds no matching name,
// he reads the raw number as-is without complaint.
// For input validation he applies a relaxed rule: Oracle and DirX directories
// sometimes use underscores in OIDs where hyphens are standard, so he
// swaps them before checking (DIRSTUDIO-1216).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * In-place value editor for LDAP OID syntax
 * (OID 1.3.6.1.4.1.1466.115.121.1.38).
 * An OID is a globally unique dotted-decimal identifier like
 * {@code "1.3.6.1.4.1.1466.115.121.1.38"} that labels schema elements
 * (syntaxes, matching rules, attribute types, object classes).
 * We annotate the raw OID in the table with its human-readable description if
 * one is known (e.g. {@code "1.3.6.1.4.1.1466.115.121.1.38 (OID Syntax)"}),
 * and validate input against the OID syntax checker with a relaxed rule that
 * treats underscores as hyphens to accommodate some vendor directories.
 * Think of this as C-3PO translating a cryptic droid serial number into its
 * fleet registry name.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InPlaceOidValueEditor extends AbstractInPlaceStringValueEditor
{

    // ── C-3PO Looks Up the Fleet Registry Name ────────────────────────────────
    // C-3PO receives a raw OID serial number and checks his fleet registry
    // (Utils.getOidDescription) for a matching human-readable name.
    // If a name is found, he appends it in parentheses: "1.2.3 (Common Name)".
    // If the registry is empty or the raw-values flag is set, he reads the
    // serial verbatim.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OID string decorated with its human-readable description if one
     * is known, e.g. {@code "1.3.6.1.4.1.1466.115.121.1.38 (OID Syntax)"}.
     * If raw-values mode is active or no description is found, returns the plain
     * OID string.
     *
     * <p>For example — C-3PO reads the fleet registry:</p>
     * <pre>
     *   "1.3.6.1.4.1.1466.115.121.1.38" → "1.3.6.1.4.1.1466.115.121.1.38 (OID Syntax)"
     *   "1.2.3.4.5.6"                   → "1.2.3.4.5.6"  (no description found)
     * </pre>
     *
     * @param value  The LDAP attribute value containing the raw OID string.
     * @return       The annotated OID display string.
     */
    public String getDisplayValue( IValue value )
    {
        String displayValue = super.getDisplayValue( value );

        if ( !showRawValues() )
        {
            String description = Utils.getOidDescription( displayValue );

            if ( description != null )
            {
                displayValue = displayValue + " (" + description + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        return displayValue;
    }


    // ── C-3PO Validates the Serial Number Before Logging It ──────────────────
    // Before C-3PO logs a newly entered OID, he checks that it conforms to the
    // OID syntax rules.  Some vendor droids (Oracle, DirX) use underscores in
    // their serials instead of the regulation hyphens; C-3PO swaps them before
    // the check so those serials pass validation (DIRSTUDIO-1216).
    // If the serial still fails the format check, C-3PO returns null to prevent
    // the invalid value from being written to the directory.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw OID string if it is a syntactically valid OID, or
     * {@code null} if it fails validation.
     * As a relaxed extension (DIRSTUDIO-1216) we replace any underscores with
     * hyphens before the check, allowing OIDs such as those found in Oracle or
     * DirX directories that deviate from the standard.
     *
     * <p>For example — C-3PO validates a vendor OID:</p>
     * <pre>
     *   "1.3.6.1_4_1.1"  → underscore swapped to hyphen → valid → returns "1.3.6.1_4_1.1"
     *   "not-an-oid"      → fails syntax check → returns null
     * </pre>
     *
     * @param value  The LDAP attribute value holding the candidate OID string.
     * @return       The raw OID string if valid; {@code null} if invalid.
     */
    @Override
    public Object getRawValue( IValue value )
    {
        Object rawValue = super.getRawValue( value );

        // DIRSTUDIO-1216: allows relaxed OID syntax with underscore, e.g. for Oracle or DirX
        if ( rawValue instanceof String
            && OidSyntaxChecker.INSTANCE.isValidSyntax( ( ( String ) rawValue ).replace( "_", "-" ) ) )
        {
            return rawValue;
        }
        else
        {
            return null;
        }
    }
}
