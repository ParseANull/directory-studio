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


import java.security.cert.CertPathValidatorException.BasicReason;
import java.security.cert.CertPathValidatorException.Reason;
import java.security.cert.X509Certificate;
import java.util.Collection;

import org.apache.directory.api.ldap.model.exception.LdapTlsHandshakeFailCause;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.core.ICertificateHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: CertificateTrustDialog — THE REBEL ALLIANCE SECURITY CHECKPOINT ────────
// When a Rebel courier arrives with credentials the Alliance hasn't seen before
// (expired, self-signed, wrong name ...), a security officer has to decide:
// "Do we trust this person?  Just for today?  Permanently?  Or not at all?"
// CertificateTrustDialog is that checkpoint.  It shows why the TLS certificate
// failed validation and gives the user three radio buttons to choose a trust level.
//
// Star Wars scene: Admiral Ackbar at the briefing — "It's a trap!" — reviewing
// the suspicious Imperial flight plan before deciding whether to proceed.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Dialog that presents an untrusted X.509 certificate to the user and asks them
 * to choose a trust level.
 *
 * <p>Shown when a TLS handshake fails because the server's certificate is not
 * trusted by the JVM's trust store.  The dialog lists the specific failure
 * causes (expired, self-signed, hostname mismatch, no valid certification path)
 * and offers three radio-button choices:</p>
 * <ul>
 *   <li><b>Do Not Trust</b> — abort the connection.</li>
 *   <li><b>Trust for this session</b> — accept until the application closes.</li>
 *   <li><b>Always trust</b> — add permanently to the trust store.</li>
 * </ul>
 *
 * <p>A "View Certificate" button opens {@link CertificateInfoDialog} so the user
 * can inspect the full certificate chain before deciding.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CertificateTrustDialog extends Dialog
{
    // ── FIELDS ────────────────────────────────────────────────────────────────────

    /** The window title. */
    private String title;

    /**
     * The user's trust decision.  Starts as {@code null} (no decision yet), then
     * set by the radio-button listeners.  Callers read it via {@link #getTrustLevel()}.
     */
    private ICertificateHandler.TrustLevel trustLevel;

    /** The host whose certificate we are inspecting (used in the dialog text). */
    private String host;

    /** The full certificate chain; element [0] is the leaf (server) certificate. */
    private X509Certificate[] certificateChain;

    /** The reasons the TLS handshake failed, shown as bullet points in the dialog. */
    private Collection<LdapTlsHandshakeFailCause> failCauses;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link CertificateTrustDialog}.
     *
     * <p>The dialog defaults to "Do Not Trust" so that closing without choosing
     * always means "reject" — we never accidentally trust.</p>
     *
     * @param parentShell       The parent SWT shell.
     * @param host              The hostname whose certificate is being reviewed.
     * @param certificateChain  The certificate chain; element [0] is the leaf cert.
     * @param failCauses        The reasons validation failed (expired, self-signed, …).
     */
    public CertificateTrustDialog( Shell parentShell, String host, X509Certificate[] certificateChain,
        Collection<LdapTlsHandshakeFailCause> failCauses )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        title = Messages.getString( "CertificateTrustDialog.CertificateTrust" ); //$NON-NLS-1$
        this.host = host;
        this.certificateChain = certificateChain;
        this.failCauses = failCauses;
        trustLevel = null;
    }


    // ── CONFIGURE SHELL ───────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Sets the shell title to the localised "Certificate Trust" string.
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
     * Adds a "View Certificate" details button (opens {@link CertificateInfoDialog})
     * and an "OK" button to confirm the trust level chosen via the radio buttons.
     */
    @Override
    protected void createButtonsForButtonBar( Composite parent )
    {
        createButton( parent, IDialogConstants.DETAILS_ID, Messages
            .getString( "CertificateTrustDialog.ViewCertificate" ), false ); //$NON-NLS-1$
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false );
    }


    // ── BUTTON PRESSED ────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Intercepts the "View Certificate" button to open {@link CertificateInfoDialog}
     * rather than closing this dialog.  All other button IDs delegate to the
     * superclass.
     */
    @Override
    protected void buttonPressed( int buttonId )
    {
        // ── VIEW CERTIFICATE — OPEN INFO DIALOG ───────────────────────────────────
        // The Details button opens the certificate info dialog on top of this one;
        // it does NOT close the trust dialog.
        // ──────────────────────────────────────────────────────────────────────────
        if ( buttonId == IDialogConstants.DETAILS_ID )
        {
            new CertificateInfoDialog( getShell(), certificateChain ).open();
        }

        super.buttonPressed( buttonId );
    }


    // ── CREATE DIALOG AREA ────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the dialog body: a header explaining the certificate is invalid for
     * {@code host}, a bulleted list of specific failure reasons, and three radio
     * buttons for the trust level selection.
     */
    @Override
    protected Control createDialogArea( final Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gridData = new GridData( GridData.FILL_BOTH );
        gridData.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        gridData.heightHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH / 2 );
        composite.setLayoutData( gridData );

        // ── INVALID CERTIFICATE HEADER ────────────────────────────────────────────
        // Tell the user which host has a certificate problem.
        // ──────────────────────────────────────────────────────────────────────────
        BaseWidgetUtils.createWrappedLabel( composite, NLS.bind( Messages
            .getString( "CertificateTrustDialog.InvalidCertificate" ), host ), 1 ); //$NON-NLS-1$

        // ── FAILURE CAUSES ────────────────────────────────────────────────────────
        // Walk the fail causes and render a human-readable label for each one.
        // We handle the five most common reasons explicitly; anything else gets
        // the raw message from the exception.
        // ──────────────────────────────────────────────────────────────────────────
        Composite failedCauseContainer = BaseWidgetUtils.createColumnContainer( composite, 1, 1 );

        for ( LdapTlsHandshakeFailCause failCause : failCauses )
        {
            Reason reason = failCause.getReason();

            if ( reason == BasicReason.EXPIRED )
            {
                BaseWidgetUtils.createWrappedLabel( failedCauseContainer, Messages
                    .getString( "CertificateTrustDialog.CertificateExpired" ), 1 ); //$NON-NLS-1$
            }
            else if ( reason == BasicReason.NOT_YET_VALID )
            {
                BaseWidgetUtils.createWrappedLabel( failedCauseContainer, Messages
                    .getString( "CertificateTrustDialog.CertificateNotYetValid" ), 1 ); //$NON-NLS-1$
            }
            else if ( reason == LdapTlsHandshakeFailCause.LdapApiReason.HOST_NAME_VERIFICATION_FAILED )
            {
                BaseWidgetUtils.createWrappedLabel( failedCauseContainer, Messages
                    .getString( "CertificateTrustDialog.HostnameVerificationFailed" ), 1 ); //$NON-NLS-1$
            }
            else if ( reason == LdapTlsHandshakeFailCause.LdapApiReason.NO_VALID_CERTIFICATION_PATH )
            {
                BaseWidgetUtils.createWrappedLabel( failedCauseContainer, Messages
                    .getString( "CertificateTrustDialog.NoValidCertificationPath" ), 1 ); //$NON-NLS-1$
            }
            else if ( reason == LdapTlsHandshakeFailCause.LdapApiReason.SELF_SIGNED )
            {
                BaseWidgetUtils.createWrappedLabel( failedCauseContainer, Messages
                    .getString( "CertificateTrustDialog.SelfSignedCertificate" ), 1 ); //$NON-NLS-1$
            }
            else
            {
                BaseWidgetUtils.createWrappedLabel( failedCauseContainer, "- " + failCause.getMessage(), 1 );
            }
        }

        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );

        // ── TRUST LEVEL SELECTION ─────────────────────────────────────────────────
        // Three radio buttons: Not, Session, Permanent.
        // Default is "Do Not Trust" — we pre-select it and set trustLevel accordingly.
        // ──────────────────────────────────────────────────────────────────────────
        BaseWidgetUtils.createWrappedLabel( composite, NLS.bind( Messages
            .getString( "CertificateTrustDialog.ChooseTrustLevel" ), host ), 1 ); //$NON-NLS-1$

        // The "Don't trust" radio button — sets TrustLevel.Not
        Button trustNotButton = BaseWidgetUtils.createRadiobutton( composite, Messages
            .getString( "CertificateTrustDialog.DoNotTrust" ), 1 ); //$NON-NLS-1$

        trustNotButton.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             * Sets the trust level to {@link ICertificateHandler.TrustLevel#Not}.
             */
            @Override
            public void widgetSelected( final SelectionEvent event )
            {
                CertificateTrustDialog.this.trustLevel = ICertificateHandler.TrustLevel.Not;
            }
        } );

        // The "Trust for this session only" radio button — sets TrustLevel.Session
        Button trustSessionButton = BaseWidgetUtils.createRadiobutton( composite, Messages
            .getString( "CertificateTrustDialog.TrustForThisSession" ), 1 ); //$NON-NLS-1$

        trustSessionButton.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             * Sets the trust level to {@link ICertificateHandler.TrustLevel#Session}.
             */
            @Override
            public void widgetSelected( final SelectionEvent event )
            {
                CertificateTrustDialog.this.trustLevel = ICertificateHandler.TrustLevel.Session;
            }
        } );

        // The "Always trust (permanent)" radio button — sets TrustLevel.Permanent
        Button trustPermanentButton = BaseWidgetUtils.createRadiobutton( composite, Messages
            .getString( "CertificateTrustDialog.AlwaysTrust" ), 1 ); //$NON-NLS-1$

        trustPermanentButton.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             * Sets the trust level to {@link ICertificateHandler.TrustLevel#Permanent}.
             */
            @Override
            public void widgetSelected( final SelectionEvent event )
            {
                CertificateTrustDialog.this.trustLevel = ICertificateHandler.TrustLevel.Permanent;
            }
        } );

        // ── DEFAULT SELECTION — DO NOT TRUST ──────────────────────────────────────
        // Pre-select "Do Not Trust" so closing or pressing Escape defaults to reject.
        // ──────────────────────────────────────────────────────────────────────────
        trustNotButton.setSelection( true );
        trustLevel = ICertificateHandler.TrustLevel.Not;

        return composite;
    }


    // ── GET TRUST LEVEL ───────────────────────────────────────────────────────────
    /**
     * Returns the trust level the user selected.
     *
     * <p>This is only meaningful after {@link #open()} returns {@link Dialog#OK}.
     * If the dialog was cancelled, the caller should ignore this value and treat
     * it as {@link ICertificateHandler.TrustLevel#Not}.</p>
     *
     * @return The selected {@link ICertificateHandler.TrustLevel}.
     */
    public ICertificateHandler.TrustLevel getTrustLevel()
    {
        return trustLevel;
    }
}
