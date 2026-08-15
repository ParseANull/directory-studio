/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl.editor;


import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
import org.eclipse.swt.widgets.Shell;
import org.apache.directory.studio.openldap.config.acl.OpenLdapAclValueWithContext;
import org.apache.directory.studio.openldap.config.acl.dialogs.OpenLdapAclDialog;


// ── CLASS: OpenLdapAclValueEditor — CASSIAN ANDOR CRACKING THE IMPERIAL ARCHIVE
// Cassian Andor knows how to extract classified data from Imperial records and
// hand it to the rebellion in a format they can use. This value editor does
// the same: when the LDAP browser wants to edit an "olcAccess" attribute, we
// strip the "{N}" precedence prefix (if present), build an
// OpenLdapAclValueWithContext, hand it to the ACL dialog, and — if the user
// clicks OK — put the (optionally re-prefixed) result back into the attribute
// value. The class also exposes helpers for parsing and stripping the precedence
// so tests can exercise them directly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An {@link AbstractDialogStringValueEditor} that handles OpenLDAP ACL string
 * values. Opens {@link OpenLdapAclDialog} when the user double-clicks an
 * {@code olcAccess} attribute value in the LDAP browser. Parses the optional
 * {@code {N}} precedence prefix before opening the dialog and re-applies it
 * (if present and checked) when the dialog returns OK.
 *
 * <p>Think of this class as Cassian Andor extracting and re-filing classified
 * Imperial records — carefully peeling off the index number, editing the
 * content, then re-attaching the index before returning the file to the archive.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapAclValueEditor extends AbstractDialogStringValueEditor
{
    // ── Opening the ACL Dialog for Editing ────────────────────────────────────
    // Cassian identifies the target file (the context), opens the secure editing
    // terminal (the dialog), waits for the operator to finish, then packages the
    // result back with the correct precedence prefix.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link OpenLdapAclDialog} for the ACL value currently held by
     * this editor. If the user confirms (OK) and the result is non-empty, we
     * optionally re-apply the {@code {N}} precedence prefix and call
     * {@link #setValue(Object)} with the final string.
     *
     * <p>For example — Cassian extracting the ACL and returning the edited version:</p>
     * <pre>
     *   // value editor calls openDialog when user double-clicks the cell
     *   boolean changed = openDialog(shell);
     *   // if true, getValue() now returns the updated ACL string
     * </pre>
     *
     * @param shell  The parent shell for the dialog.
     * @return       {@code true} if the user confirmed and the value was updated.
     */
    protected boolean openDialog( Shell shell )
    {
        Object value = getValue();

        if ( value instanceof OpenLdapAclValueWithContext )
        {
            OpenLdapAclValueWithContext context = ( OpenLdapAclValueWithContext ) value;

            OpenLdapAclDialog dialog = new OpenLdapAclDialog( shell, context );

            if ( ( dialog.open() == OpenLdapAclDialog.OK ) && !"".equals( dialog.getAclValue() ) ) //$NON-NLS-1$
            {
                if ( dialog.hasPrecedence() )
                {
                    String aclValue = "{" + dialog.getPrecedence() + "}" + dialog.getAclValue();
                    setValue( aclValue );
                }
                else
                {
                    String aclValue = dialog.getAclValue();
                    setValue( aclValue );
                }

                return true;
            }
        }

        return false;
    }


    // ── Building a Context from an AttributeHierarchy (Single Value) ──────────
    // Cassian checks the attribute hierarchy: if it is empty he creates a blank
    // ACL context; if it has one value he extracts the precedence and builds a
    // context from the bare ACL string.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds an {@link OpenLdapAclValueWithContext} from the given
     * {@link AttributeHierarchy}. The hierarchy must have exactly one attribute
     * with zero or one values; multi-value hierarchies return {@code null} (not
     * editable). If there is one value, the {@code {N}} precedence is stripped
     * before passing the ACL text to the context.
     *
     * <p>For example — Cassian extracting the raw ACL from an olcAccess attribute:</p>
     * <pre>
     *   // Attribute: olcAccess = "{0}access to * by users read"
     *   Object raw = editor.getRawValue(hierarchy);
     *   // raw = OpenLdapAclValueWithContext(conn, entry, 0, "access to * by users read")
     * </pre>
     *
     * @param attributeHierarchy  The attribute hierarchy from the browser; may be {@code null}.
     * @return                    The {@link OpenLdapAclValueWithContext}, or {@code null} if
     *                            the hierarchy is null or has multiple attributes/values.
     */
    public Object getRawValue( AttributeHierarchy attributeHierarchy )
    {
        if ( attributeHierarchy == null )
        {
            return null;
        }

        if ( ( attributeHierarchy.size() == 1 ) && ( attributeHierarchy.getAttribute().getValueSize() == 0 ) )
        {
            IEntry entry = attributeHierarchy.getAttribute().getEntry();
            IBrowserConnection connection = entry.getBrowserConnection();

            if ( attributeHierarchy.getAttribute().getValueSize() == 0 )
            {
                return new OpenLdapAclValueWithContext( connection, entry, -1, "" ); //$NON-NLS-1$
            }
            else if ( attributeHierarchy.getAttribute().getValueSize() == 1 )
            {
                String valueStr = getDisplayValue( attributeHierarchy );
                int precedence = getPrecedence( valueStr );
                String aclValue = valueStr;

                if ( precedence != -1 )
                {
                    aclValue = removePrecedence( valueStr );
                }

                return new OpenLdapAclValueWithContext( connection, entry, precedence, aclValue );
            }
        }

        return null;
    }


    // ── Building a Context from a Single IValue ────────────────────────────────
    // When the browser passes us a single IValue (rather than a hierarchy) we
    // extract the string, strip the precedence prefix, and wrap it in a context.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds an {@link OpenLdapAclValueWithContext} from a single {@link IValue}.
     * Calls {@link AbstractDialogStringValueEditor#getRawValue(IValue)} to get
     * the string, then strips the {@code {N}} prefix and wraps in a context.
     *
     * <p>For example — Cassian filing the extracted credential:</p>
     * <pre>
     *   // value = olcAccess("{1}access to dn.sub=... by self write")
     *   Object raw = editor.getRawValue(value);
     *   // → OpenLdapAclValueWithContext(conn, entry, 1, "access to dn.sub=... by self write")
     * </pre>
     *
     * @param value  The single LDAP browser value to wrap.
     * @return       The {@link OpenLdapAclValueWithContext}, or {@code null} if the
     *               value is not a plain string.
     */
    public Object getRawValue( IValue value )
    {
        Object object = super.getRawValue( value );

        if ( object instanceof String )
        {
            IEntry entry = value.getAttribute().getEntry();
            IBrowserConnection connection = entry.getBrowserConnection();
            String valueStr = ( String ) object;
            int precedence = getPrecedence( valueStr );
            String aclValue = valueStr;

            if ( precedence != -1 )
            {
                aclValue = removePrecedence( valueStr );
            }

            return new OpenLdapAclValueWithContext( connection, entry, precedence, aclValue );
        }

        return null;
    }


    // ── Parsing the Numeric Precedence Prefix ────────────────────────────────
    // Cassian reads the "{N}" index at the start of the ACL record — a sequence
    // of digits between '{' and '}'. If the prefix is absent, malformed, or
    // empty, we return -1 to signal "no precedence".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Parses the optional {@code {N}} precedence prefix from the beginning of
     * the ACL string. Returns the integer {@code N} if present and valid, or
     * {@code -1} if the string does not start with a well-formed {@code {digits}}
     * prefix.
     *
     * <p>For example — parsing the prefix:</p>
     * <pre>
     *   getPrecedence("{0}access to *") // → 0
     *   getPrecedence("{12}access to *") // → 12
     *   getPrecedence("access to *")    // → -1
     *   getPrecedence("{}")             // → -1
     * </pre>
     *
     * @param s  The raw ACL string, possibly starting with {@code {N}}.
     * @return   The precedence integer, or {@code -1} if no valid prefix is present.
     */
    public int getPrecedence( String s )
    {
        // Checking if the acl contains precedence information ("{int}")
        if ( Strings.isCharASCII( s, 0, '{' ) )
        {
            int precedence = 0;
            int pos = 1;

            while ( pos < s.length() )
            {
                char c = s.charAt( pos );

                if ( c == '}' )
                {
                    if ( pos == 1 )
                    {
                        return -1;
                    }
                    else
                    {
                        return precedence;
                    }
                }

                if ( ( c >= '0' ) && ( c <= '9' ) )
                {
                    precedence = precedence*10 + ( c - '0' );
                }
                else
                {
                    // Not a precedence
                    return -1;
                }

                pos++;
            }

            return -1;
        }
        else
        {
            return -1;
        }
    }


    // ── Stripping the Precedence Prefix From the ACL String ───────────────────
    // Cassian removes the "{N}" file index from the front of the document so
    // the ACL editor only sees the raw directive. If there is no valid prefix
    // the string is returned unchanged.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the ACL string with the {@code {N}} precedence prefix removed.
     * If the string does not start with a well-formed {@code {digits}} prefix
     * it is returned unchanged.
     *
     * <p>For example — stripping the prefix before handing to the parser:</p>
     * <pre>
     *   removePrecedence("{0}access to *")  // → "access to *"
     *   removePrecedence("access to *")     // → "access to *" (unchanged)
     * </pre>
     *
     * @param str  The original ACL string, with or without a precedence prefix.
     * @return     The ACL string minus any valid {@code {N}} prefix.
     */
    public String removePrecedence( String str )
    {
        if ( Strings.isCharASCII( str, 0, '{' ) )
        {
            int pos = 1;

            while ( pos < str.length() )
            {
                char c = str.charAt( pos );

                if ( c == '}' )
                {
                    if ( pos == 1 )
                    {
                        // We just have {}, return the string
                        return str;
                    }
                    else
                    {
                        return str.substring( pos + 1 );
                    }
                }

                if ( ( c < '0' ) && ( c > '9' ) )
                {
                    // Not a number, get out
                    return str;
                }

                pos++;
            }

            return str;
        }
        else
        {
            return str;
        }
    }
}
