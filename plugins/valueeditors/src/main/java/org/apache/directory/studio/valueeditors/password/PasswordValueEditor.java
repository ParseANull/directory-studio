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

package org.apache.directory.studio.valueeditors.password;


import org.apache.directory.api.ldap.model.constants.LdapSecurityConstants;
import org.apache.directory.api.ldap.model.password.PasswordUtil;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.ldapbrowser.common.dialogs.TextDialog;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.valueeditors.AbstractDialogBinaryValueEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: PasswordValueEditor — VADER'S CREDENTIAL BADGE SCANNER ─────────────
// On the Death Star docking bay, Vader's badge scanner reads the credential
// stored in each officer's badge: not the raw bytes — just the algorithm type.
// "SSHA-hashed password" or "Plain-text password" is all the table shows.
// When an officer needs to update their credential, the scanner opens the full
// PasswordDialog terminal (the two-tab verification and provisioning console).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Value editor for the LDAP {@code userPassword} attribute.
 * In the table we never reveal the password bytes; instead we show a descriptive
 * label such as "SSHA-hashed password" or "Plain-text password".
 * When the user double-clicks, we open {@link PasswordDialog} which lets them
 * inspect the current hash, test a password against it, and provision a new
 * credential with any supported algorithm.
 * Think of this as Vader's credential badge scanner — reads the badge type in
 * the table and opens the full terminal on demand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PasswordValueEditor extends AbstractDialogBinaryValueEditor
{

    // ── Vader's Scanner Opens the Credential Terminal ─────────────────────────
    // The scanner reads the current password bytes from the wrapper and opens
    // PasswordDialog.  If the user clicks OK with a new credential, the scanner
    // writes the new bytes to the attribute via the cell-editor path.
    // If the wrapper is missing or the dialog is cancelled, we return false.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens {@link PasswordDialog} for the user to inspect or replace the
     * {@code userPassword} value.
     * Returns {@code true} if the user clicked OK (the new password bytes have
     * been set); {@code false} if the dialog was cancelled.
     *
     * <p>For example — the scanner opens the credential terminal:</p>
     * <pre>
     *   boolean changed = editor.openDialog(shell);
     *   if (changed) {
     *       // new password bytes are ready to commit
     *   }
     * </pre>
     *
     * @param shell  The parent SWT shell for PasswordDialog.
     * @return       {@code true} if a new credential was set; {@code false} otherwise.
     */
    protected boolean openDialog( Shell shell )
    {
        Object value = getValue();

        if ( value instanceof PasswordValueEditorRawValueWrapper )
        {
            PasswordValueEditorRawValueWrapper wrapper = ( PasswordValueEditorRawValueWrapper ) value;

            if ( wrapper.password instanceof byte[] )
            {
                byte[] pw = ( byte[] ) wrapper.password;
                PasswordDialog dialog = new PasswordDialog( shell, pw, wrapper.entry );

                if ( dialog.open() == TextDialog.OK )
                {
                    setValue( dialog.getNewPassword() );

                    return true;
                }
            }
        }

        return false;
    }


    // ── Vader's Scanner Reads the Badge Type for the Table ────────────────────
    // In raw-values mode the scanner shows the raw bytes as printable text.
    // In normal mode it reads just enough of the stored value to identify the
    // algorithm: if the value starts with "{ALGO}" it is hashed; if it is empty
    // it is explicitly blank; otherwise it is plain text.
    // The actual password bytes are never displayed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a descriptive label for the password in the table, without ever
     * revealing the credential bytes.
     * Examples:
     * <ul>
     *   <li>{@code "(empty)"} — empty password stored explicitly</li>
     *   <li>{@code "SSHA-hashed password"} — salted SHA hash</li>
     *   <li>{@code "Plain-text password"} — no encoding applied</li>
     * </ul>
     * In raw-values mode the full printable string is returned instead.
     *
     * @param value  The LDAP attribute value holding the password bytes.
     * @return       The descriptive label, or the raw printable string in raw-values mode.
     */
    public String getDisplayValue( IValue value )
    {
        if ( showRawValues() )
        {
            return getPrintableString( value );
        }
        else
        {
            if ( value == null )
            {
                return NULL; //$NON-NLS-1$
            }

            String password = value.getStringValue();

            if ( password == null )
            {
                return NULL; //$NON-NLS-1$
            }
            else
            {
                String text;

                if ( EMPTY.equals( password ) ) //$NON-NLS-1$
                {
                    text = Messages.getString( "PasswordValueEditor.EmptyPassword" ); //$NON-NLS-1$
                }
                else if ( ( password.indexOf( '{' ) == 0 ) && ( password.indexOf( '}' ) > 0 ) )
                {
                    text = NLS.bind(
                        Messages.getString( "PasswordValueEditor.HashedPassword" ), getHashMethodName( password ) ); //$NON-NLS-1$
                }
                else
                {
                    text = Messages.getString( "PasswordValueEditor.PlainTextPassword" ); //$NON-NLS-1$
                }
                return text;
            }
        }
    }


    // ── Vader's Scanner Resolves a Hash Algorithm Prefix ─────────────────────
    // Given the raw password string (e.g. "{SSHA}..."), the scanner extracts
    // and looks up the algorithm name.  If the prefix maps to a known algorithm
    // it returns its canonical name; otherwise the raw prefix string is returned.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Extracts and resolves the hash algorithm name from a password string
     * with a {@code "{ALGO}"} prefix.
     * Returns the algorithm's canonical name (e.g. "SSHA") if known, or the
     * raw prefix string if unrecognised.
     *
     * @param s  The raw password string starting with {@code "{"}.
     * @return   The resolved hash algorithm name.
     */
    private String getHashMethodName( String s )
    {
        LdapSecurityConstants hashMethod = PasswordUtil.findAlgorithm( Strings.getBytesUtf8( s ) );

        if ( hashMethod != null )
        {
            return hashMethod.getName();
        }

        return s;
    }


    // ── Vader's Scanner Builds an Empty Badge Packet ──────────────────────────
    // When the attribute has no existing password value (brand-new slot), the
    // scanner assembles an empty credential packet so PasswordDialog opens in
    // "new password only" mode.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a {@link PasswordValueEditorRawValueWrapper} containing an empty
     * byte array and the owning entry.
     * Used when the {@code userPassword} attribute has no value yet.
     *
     * @param attribute  The empty {@code userPassword} attribute.
     * @return           A wrapper with a zero-length password byte array.
     */
    protected Object getEmptyRawValue( IAttribute attribute )
    {
        return new PasswordValueEditorRawValueWrapper( new byte[0], attribute.getEntry() );
    }


    // ── Vader's Scanner Packages an Existing Credential ───────────────────────
    // When the attribute already holds a password, the scanner wraps the raw
    // bytes and the entry (needed for the live-bind test) into a packet for
    // PasswordDialog.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a {@link PasswordValueEditorRawValueWrapper} wrapping the existing
     * password bytes (from the parent's {@code getRawValue} implementation) and
     * the owning LDAP entry.
     *
     * @param value  The LDAP attribute value holding the current password bytes.
     * @return       A wrapper with the password bytes and the entry.
     */
    public Object getRawValue( IValue value )
    {
        Object password = super.getRawValue( value );

        return new PasswordValueEditorRawValueWrapper( password, value.getAttribute().getEntry() );
    }


    // ── CLASS: PasswordValueEditorRawValueWrapper — VADER'S SEALED CREDENTIAL PACKET
    // Before PasswordDialog is opened, the scanner seals the current password
    // bytes and the owning entry into a single packet.  The entry is needed so
    // the live-bind button in the dialog can use the entry's DN as the bind DN.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Internal transfer object that carries the password bytes and the owning
     * LDAP entry into {@link PasswordDialog}.
     * The entry is required for the "Bind" button, which executes a live LDAP
     * BIND using the entry's DN.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class PasswordValueEditorRawValueWrapper
    {
        /** The password, used as initial value in PasswordDialog */
        private Object password;

        /** The entry, used for the bind operation in PasswordDialog */
        private IEntry entry;


        // ── Vader's Scanner Seals the Credential Packet ───────────────────────
        // The scanner binds the password bytes and the entry into a sealed packet
        // that will be handed to PasswordDialog.
        // ────────────────────────────────────────────────────────────────────────
        /**
         * Creates a new PasswordValueEditorRawValueWrapper.
         *
         * @param password  The password object (typically a byte array).
         * @param entry     The LDAP entry owning this password attribute.
         */
        private PasswordValueEditorRawValueWrapper( Object password, IEntry entry )
        {
            this.password = password;
            this.entry = entry;
        }
    }
}
