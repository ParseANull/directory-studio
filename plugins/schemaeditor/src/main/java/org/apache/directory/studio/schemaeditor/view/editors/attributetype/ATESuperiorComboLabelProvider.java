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
package org.apache.directory.studio.schemaeditor.view.editors.attributetype;


import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.apache.directory.studio.schemaeditor.view.editors.NonExistingAttributeType;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;


// ── CLASS: ATESuperiorComboLabelProvider — C-3PO TRANSLATES FOR R2 ───────────────────
// C-3PO speaks over six million forms of communication, and one of his most useful
// skills is taking R2's cryptic binary data and rendering it as a clean, human-readable
// sentence.  He knows the difference between a real droid he can name and a phantom
// entity he's only heard about second-hand.
// This label provider does the same for the "Superior Type" combo: it takes an opaque
// Object — either a real AttributeType or a NonExistingAttributeType placeholder — and
// converts it into the "name(s)  -  (OID)" string that appears in the combo dropdown.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * JFace LabelProvider that converts "Superior Type" combo items into display strings.
 * For a real {@link AttributeType} we format all its aliases (via {@link ViewUtils#concateAliases})
 * followed by its OID in parentheses.  For a {@link NonExistingAttributeType} we
 * delegate to its {@code getDisplayName()} method.
 * Think of this as C-3PO: he knows "AttributeType" and "NonExistingAttributeType" as
 * two distinct dialects and produces correct English for both.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ATESuperiorComboLabelProvider extends LabelProvider
{
    // ── C-3PO Converts the Binary Data to Human Speech ──────────────────────────────
    // C-3PO reads the incoming object, identifies whether it's a real AttributeType
    // (with a full name and OID) or a phantom placeholder, and produces the appropriate
    // announcement.  If it's something he's never seen before, he stays silent (null).
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display string for a single "Superior Type" combo item.
     * For a real {@link AttributeType}: concatenates all its aliases (or just the first
     * if only one) separated by commas, then appends "  -  (OID)".  If the type has no
     * names at all, falls back to a localised "(no name)  -  (OID)" format via NLS.
     * For a {@link NonExistingAttributeType}: returns its {@code getDisplayName()} string,
     * which is either "(None)" or the raw name with a warning annotation.
     * Returns null for unrecognised types (the JFace convention for "nothing to show").
     *
     * <p>For example — C-3PO's translations:</p>
     * <pre>
     *   // obj = AttributeType names=["cn","commonName"], oid="2.5.4.3"
     *   //   → "cn, commonName  -  (2.5.4.3)"
     *
     *   // obj = NonExistingAttributeType("(None)")
     *   //   → "(None)"
     *
     *   // obj = NonExistingAttributeType("unknownAT")
     *   //   → "unknownAT   (This attribute type doesnt exist)"
     * </pre>
     *
     * @param obj  the combo item — an {@link AttributeType} or
     *             {@link NonExistingAttributeType}
     * @return     the human-readable display string, or null if the type is unrecognised
     */
    public String getText( Object obj )
    {
        if ( obj instanceof AttributeType )
        {
            AttributeType at = ( AttributeType ) obj;

            List<String> names = at.getNames();
            if ( ( names != null ) && ( names.size() > 0 ) )
            {
                return ViewUtils.concateAliases( names ) + "  -  (" + at.getOid() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            else
            {
                return NLS.bind(
                    Messages.getString( "ATESuperiorComboLabelProvider.None" ), new String[] { at.getOid() } ); //$NON-NLS-1$
            }
        }
        else if ( obj instanceof NonExistingAttributeType )
        {
            return ( ( NonExistingAttributeType ) obj ).getDisplayName();
        }

        // Default
        return null;
    }
}
