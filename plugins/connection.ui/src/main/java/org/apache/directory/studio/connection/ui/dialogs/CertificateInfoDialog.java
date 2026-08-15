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
package org.apache.directory.studio.connection.ui.dialogs;


import java.security.cert.X509Certificate;

import org.apache.directory.studio.connection.ui.widgets.CertificateInfoComposite;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: CertificateInfoDialog — MON MOTHMA REVIEWS THE REBEL CREDENTIALS ───────
// Before the Rebel Alliance trusts a new courier, Mon Mothma inspects their
// identification documents — name, issuer, serial number, validity dates, fingerprints.
// CertificateInfoDialog does exactly that: it wraps a CertificateInfoComposite in a
// resizable dialog so the user can examine every field of an X.509 certificate chain.
// There is only a Close button; this dialog is read-only information, not a decision.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Read-only dialog that displays the full details of an X.509 certificate chain.
 *
 * <p>Wraps a {@link CertificateInfoComposite} (which shows "General" and "Details"
 * tabs) in a resizable JFace {@link Dialog}.  The only button is "Close" — there is
 * no accept/reject decision here; that decision is handled by
 * {@link CertificateTrustDialog}.</p>
 *
 * <p>Typically opened from {@link CertificateTrustDialog} when the user clicks
 * "View Certificate", or directly from the certificate trust-store UI.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CertificateInfoDialog extends Dialog
{
    // ── FIELDS ────────────────────────────────────────────────────────────────────

    /** The window title shown in the shell title bar. */
    private String title;

    /** The certificate chain to display; index 0 is the leaf (server) certificate. */
    private X509Certificate[] certificateChain;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link CertificateInfoDialog}.
     *
     * <p>Makes the dialog resizable so users can expand it to see longer field
     * values, then stores the certificate chain for display in
     * {@link #createDialogArea}.</p>
     *
     * @param parentShell       The parent SWT shell.
     * @param certificateChain  The chain to display; element [0] is the leaf cert.
     */
    public CertificateInfoDialog( Shell parentShell, X509Certificate[] certificateChain )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.title = Messages.getString( "CertificateInfoDialog.CertificateViewer" ); //$NON-NLS-1$
        this.certificateChain = certificateChain;
    }


    // ── CONFIGURE SHELL ───────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Sets the shell title to the localised "Certificate Viewer" string.
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( title );
    }


    // ── CREATE BUTTONS FOR BUTTON BAR ─────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Provides only a "Close" button — this dialog is purely informational.
     */
    @Override
    protected void createButtonsForButtonBar( Composite parent )
    {
        // ── CLOSE ONLY — READ-ONLY VIEW ────────────────────────────────────────────
        // We use CANCEL_ID so pressing Escape or the button closes without
        // triggering an OK path.  IDialogConstants.CLOSE_LABEL gives us the
        // localised "Close" text.
        // ──────────────────────────────────────────────────────────────────────────
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false );
    }


    // ── CREATE DIALOG AREA ────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the main area: a sized composite containing a
     * {@link CertificateInfoComposite} populated with the certificate chain.
     */
    @Override
    protected Control createDialogArea( final Composite parent )
    {
        // ── COMPOSITE WITH MINIMUM SIZE HINT ──────────────────────────────────────
        // We set an explicit widthHint and heightHint so the dialog opens large
        // enough to show the "General" tab without immediately needing to be resized.
        // ──────────────────────────────────────────────────────────────────────────
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gridData = new GridData( GridData.FILL_BOTH );
        gridData.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH * 3 / 2 );
        gridData.heightHint = convertVerticalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        composite.setLayoutData( gridData );

        // ── CERTIFICATE INFO COMPOSITE ────────────────────────────────────────────
        // Hand the chain to CertificateInfoComposite which renders the General and
        // Details tabs.
        // ──────────────────────────────────────────────────────────────────────────
        CertificateInfoComposite certificateInfoComposite = new CertificateInfoComposite( composite, SWT.NONE );
        certificateInfoComposite.setInput( certificateChain );

        applyDialogFont( composite );

        return composite;
    }
}
