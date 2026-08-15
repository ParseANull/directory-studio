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

package org.apache.directory.studio.valueeditors.certificate;


import org.apache.directory.studio.ldapbrowser.common.dialogs.TextDialog;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.valueeditors.AbstractDialogBinaryValueEditor;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: CertificateValueEditor — IMPERIAL VERIFICATION AT THE DOCKING BAY ─
// Admiral Piett's verification officer doesn't examine every ship personally —
// he dispatches a subordinate (this editor) who opens the verification terminal
// (CertificateDialog), lets the officer inspect the seal, and reports back
// whether the vessel's credentials were confirmed.
// For the table cell the subordinate just reads the quick ID tag on the seal:
// "X.509v3: cn=example.com".
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Value editor for LDAP Certificate syntax (OID 1.3.6.1.4.1.1466.115.121.1.8).
 * LDAP stores certificates as raw DER bytes in binary attributes like
 * {@code userCertificate}.  For display we produce a one-line summary
 * (type + version + subject DN); for editing we open {@link CertificateDialog}
 * which lets the user inspect the full certificate details and optionally load
 * a replacement from disk.
 * Think of this as the Imperial docking officer's subordinate — shows a quick
 * ID in the table, opens the verification terminal on double-click.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CertificateValueEditor extends AbstractDialogBinaryValueEditor
{
    // ── The Subordinate Opens the Verification Terminal ───────────────────────
    // The subordinate receives the raw certificate bytes from the attribute,
    // hands them to the verification terminal (CertificateDialog), and waits
    // for the officer's verdict.  If the officer clicks OK, the (possibly
    // replaced) certificate bytes are written back to the directory.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens {@link CertificateDialog} for the user to inspect or replace the
     * raw X.509 certificate bytes stored in the attribute.
     * Returns {@code true} if the user clicked OK and we have a valid (non-null)
     * certificate to commit back to the directory; {@code false} if cancelled.
     *
     * <p>For example — the officer opens the terminal for inspection:</p>
     * <pre>
     *   boolean changed = editor.openDialog(shell);
     *   if (changed) {
     *       // The attribute now holds the new DER certificate bytes
     *   }
     * </pre>
     *
     * @param shell  The parent SWT shell for the CertificateDialog.
     * @return       {@code true} if the certificate was updated; {@code false} otherwise.
     */
    @Override
    protected boolean openDialog( Shell shell )
    {
        Object value = getValue();

        if ( value instanceof byte[] )
        {
            byte[] currentCertificateData = ( byte[] ) value;

            CertificateDialog dialog = new CertificateDialog( shell, currentCertificateData );

            if ( ( dialog.open() == TextDialog.OK ) && ( dialog.getData() != null ) )
            {
                setValue( dialog.getData() );
                return true;
            }
        }

        return false;
    }


    // ── The Subordinate Reads the Quick ID Tag for the Table Cell ────────────
    // For the table view, the subordinate just reads the seal's quick-ID tag
    // rather than opening the full terminal: "X.509v3: CN=www.example.com".
    // In raw-values mode, the raw binary bytes are presented as a printable string.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable summary of the certificate for display in the
     * LDAP browser's attribute table cell.
     * Normally we call {@link CertificateDialog#getCertificateInfo(byte[])} which
     * returns a one-liner like {@code "X.509v3: CN=example.com, O=Acme"}.
     * In raw-values mode we return the raw bytes as a printable hex/ASCII string.
     * Returns {@link #NULL} if the value is null, or an error message if the
     * attribute holds non-binary data where certificate bytes are expected.
     *
     * <p>For example — the subordinate reads the quick ID:</p>
     * <pre>
     *   IValue v = ...;  // contains DER certificate bytes
     *   String display = editor.getDisplayValue(v);
     *   // → "X.509v3: CN=www.example.com, O=Acme Corp, C=US"
     * </pre>
     *
     * @param value  The LDAP attribute value containing raw certificate bytes.
     * @return       A one-line summary string, never {@code null}.
     */
    @Override
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
            else if ( value.isBinary() )
            {
                byte[] data = value.getBinaryValue();
                String text = CertificateDialog.getCertificateInfo( data );

                return text;
            }
            else
            {
                return Messages.getString( "CertificateValueEditor.InvalidCertificateData" ); //$NON-NLS-1$
            }
        }
    }
}
