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


import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.studio.schemaeditor.view.editors.NonExistingSyntax;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;


// ── CLASS: ATESyntaxComboLabelProvider — C-3PO TRANSLATES THE JAWA DIALECT ───────────
// C-3PO is fluent in over six million forms of communication, including the local Jawa
// dialect that no one else in the crew could decipher.  He looks at a Jawa, listens to
// the bleeping chatter, and tells Luke: "This one says the syntax is 'Directory String'."
// He also knows when the Jawa is referring to something that can't be found in his
// records — and flags that clearly for the crew.
// This label provider does the same: it takes a LdapSyntax or NonExistingSyntax object
// and converts it to the human-readable string that appears in the syntax combo dropdown.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * JFace LabelProvider that converts "Syntax" combo items into display strings.
 * For a real {@link LdapSyntax} we prefer the syntax's description, fall back to its
 * name (for older schema files), and append the OID in parentheses.  For a
 * {@link NonExistingSyntax} we delegate to its {@code getDisplayName()} method.
 * Think of this as C-3PO: fluent in both "real LDAP syntax" and "placeholder dialect",
 * always producing clean, comprehensible output for the user.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ATESyntaxComboLabelProvider extends LabelProvider
{
    // ── C-3PO Translates the Syntax Blurt Into English ──────────────────────────────
    // C-3PO intercepts the raw LdapSyntax object, reads its description (or name for
    // backward compatibility), appends the OID, and announces the result to the crew.
    // If it's a phantom placeholder, he reads out that entry's own display text instead.
    // If it's neither, he stays silent — returning null as the JFace "nothing to show."
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display string for a single "Syntax" combo item.
     * For a {@link LdapSyntax}: we prefer the syntax's description string; if the
     * description is null (which can happen with older schema files that only have a
     * name), we fall back to the name.  We then append "  -  (OID)" so the user can
     * see the canonical identifier.  If both description and name are null, we use a
     * localised "(no label)  -  (OID)" fallback via NLS.
     * For a {@link NonExistingSyntax}: returns its {@code getDisplayName()} — either
     * "(None)" or the raw description with a warning annotation.
     * Returns null for unrecognised types.
     *
     * <p>For example — C-3PO's syntax translations:</p>
     * <pre>
     *   // obj = LdapSyntax description="Directory String", oid="1.3.6.1.4.1.1466.115.121.1.15"
     *   //   → "Directory String  -  (1.3.6.1.4.1.1466.115.121.1.15)"
     *
     *   // obj = NonExistingSyntax("(None)")
     *   //   → "(None)"
     *
     *   // obj = NonExistingSyntax("9.9.9.1")
     *   //   → "9.9.9.1   (This syntax doesnt exist)"
     * </pre>
     *
     * @param obj  the combo item — a {@link LdapSyntax} or {@link NonExistingSyntax}
     * @return     the human-readable display string, or null if the type is unrecognised
     */
    public String getText( Object obj )
    {
        if ( obj instanceof LdapSyntax )
        {
            LdapSyntax syntax = ( LdapSyntax ) obj;

            // Getting description (and name for backward compatibility)
            String description = syntax.getDescription();
            String name = syntax.getName();

            if ( ( description != null ) || ( name != null ) )
            {
                StringBuilder sb = new StringBuilder();

                if ( description != null )
                {
                    // Using description
                    sb.append( description );
                }
                else
                {
                    // Using name (for backward compatibility)
                    sb.append( name );
                }

                sb.append( "  -  (" ); //$NON-NLS-1$
                sb.append( syntax.getOid() );
                sb.append( ")" ); //$NON-NLS-1$

                return sb.toString();
            }
            else
            {
                return NLS.bind(
                    Messages.getString( "ATESyntaxComboLabelProvider.None" ), new String[] { syntax.getOid() } ); //$NON-NLS-1$
            }
        }
        else if ( obj instanceof NonExistingSyntax )
        {
            return ( ( NonExistingSyntax ) obj ).getDisplayName();
        }

        // Default
        return null;
    }
}
