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

package org.apache.directory.studio.connection.ui.wizards;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.apache.commons.codec.binary.Base64;
import org.apache.directory.api.util.FileUtils;
import org.apache.directory.studio.connection.ui.wizards.ExportCertificateWizardPage.CertificateExportFormat;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbench;


// ── CLASS: ExportCertificateWizard — TRANSMITTING THE REBEL BATTLE PLANS ─────────
// When Mon Mothma needs to hand the Death Star plans to General Dodonna she can
// choose the format: raw binary (DER) or armoured text (PEM).  Either way, the
// plans are written to a file on the local filesystem.
// ExportCertificateWizard does the same for X509 certificates: the user picks a
// file path and format on the single wizard page, then Finish writes the cert.
// DER is just the raw ASN.1 bytes; PEM is the same bytes Base64-encoded and
// wrapped in BEGIN/END CERTIFICATE delimiters.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Single-page wizard for exporting an {@link X509Certificate} to a local file
 * in either DER (binary) or PEM (Base64 text) format.
 *
 * <p>The wizard is opened from the "Export" button in
 * {@link org.apache.directory.studio.connection.ui.widgets.CertificateListComposite}
 * and from the certificate-info dialog.</p>
 *
 * <p>On Finish, delegates to either {@link #exportAsDerFormat()} or
 * {@link #exportAsPemFormat()} based on the user's combo selection.  Any
 * {@link IOException} or {@link CertificateEncodingException} is caught and
 * shown in an error dialog so the wizard stays open for retry.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportCertificateWizard extends Wizard
{
    // ── FIELDS ────────────────────────────────────────────────────────────────────

    /** The certificate to export. */
    private X509Certificate certificate;

    /** The single wizard page (file-picker + format combo). */
    private ExportCertificateWizardPage page;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link ExportCertificateWizard} for the given certificate.
     *
     * @param certificate The {@link X509Certificate} to export.
     */
    public ExportCertificateWizard( X509Certificate certificate )
    {
        super();
        this.certificate = certificate;
        setWindowTitle( Messages.getString( "ExportCertificateWizard.ExportCertificate" ) ); //$NON-NLS-1$
        setNeedsProgressMonitor( false );
    }


    // ── INIT ──────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Nothing to initialise — we don't need the workbench or selection here.
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        // Nothing to do.
    }


    // ── ADD PAGES ─────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Adds the single {@link ExportCertificateWizardPage} to the wizard.
     */
    @Override
    public void addPages()
    {
        page = new ExportCertificateWizardPage();
        addPage( page );
    }


    // ── PERFORM FINISH ────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Reads the format choice from the wizard page and delegates to the
     * appropriate export method.  On any exception, opens an error dialog and
     * returns {@code false} so the wizard stays open.</p>
     *
     * @return {@code true} if the file was written successfully;
     *         {@code false} on error (wizard remains open).
     */
    public boolean performFinish()
    {
        CertificateExportFormat format = page.getCertificateExportFormat();

        try
        {
            if ( format == CertificateExportFormat.DER )
            {
                return exportAsDerFormat();
            }
            else
            {
                return exportAsPemFormat();
            }
        }
        catch ( Exception e )
        {
            MessageDialog.openError( getShell(),
                Messages.getString( "ExportCertificateWizard.ErrorDialogTitle" ), //$NON-NLS-1$
                NLS.bind( Messages.getString( "ExportCertificateWizard.ErrorDialogMessage" ), //$NON-NLS-1$
                    e.getMessage() ) );
            return false;
        }
    }


    // ── EXPORT AS DER ─────────────────────────────────────────────────────────────
    /**
     * Writes the certificate as raw DER (ASN.1 binary) bytes to the chosen file.
     *
     * @return {@code true} on success.
     * @throws CertificateEncodingException If the certificate cannot be encoded.
     * @throws IOException If the file cannot be written.
     */
    private boolean exportAsDerFormat() throws CertificateEncodingException, IOException
    {
        // ── WRITE RAW BYTES ───────────────────────────────────────────────────────
        // DER is just the ASN.1 binary encoding of the certificate — no armour.
        // ──────────────────────────────────────────────────────────────────────────
        File exportFile = page.getExportFile();
        FileUtils.writeByteArrayToFile( exportFile, certificate.getEncoded() );
        return true;
    }


    // ── EXPORT AS PEM ─────────────────────────────────────────────────────────────
    /**
     * Writes the certificate as PEM (Base64-encoded DER, wrapped in
     * {@code -----BEGIN CERTIFICATE-----} / {@code -----END CERTIFICATE-----}
     * delimiters) to the chosen file.
     *
     * <p>Lines within the Base64 block are wrapped at 64 characters via
     * {@link #stripLineToNChars(String, int)}.</p>
     *
     * @return {@code true} on success.
     * @throws CertificateEncodingException If the certificate cannot be encoded.
     * @throws IOException If the file cannot be written.
     */
    private boolean exportAsPemFormat() throws CertificateEncodingException, IOException
    {
        File exportFile = page.getExportFile();

        try ( FileOutputStream fos = new FileOutputStream( exportFile ) )
        {
            try ( OutputStreamWriter osw = new OutputStreamWriter( fos, Charset.forName( "UTF-8" ) ) ) //$NON-NLS-1$
            {
                // ── WRITE PEM HEADER + BASE64 + FOOTER ───────────────────────────
                osw.write( "-----BEGIN CERTIFICATE-----\n" ); //$NON-NLS-1$
                osw.write( stripLineToNChars( new String( Base64.encodeBase64( certificate.getEncoded() ),
                    Charset.forName( "UTF-8" ) ), 64 ) ); //$NON-NLS-1$
                osw.write( "\n-----END CERTIFICATE-----\n" ); //$NON-NLS-1$
                osw.flush();
            }
        }

        return true;
    }


    // ── STRIP LINE TO N CHARS ─────────────────────────────────────────────────────
    /**
     * Inserts a newline every {@code nbChars} characters in {@code str}, producing
     * a hard-wrapped version of the string suitable for PEM output.
     *
     * <p>If {@code str} is shorter than {@code nbChars} it is returned unchanged.</p>
     *
     * @param str     The string to wrap.
     * @param nbChars The maximum number of characters per line (64 for PEM).
     * @return The wrapped string.
     */
    public static String stripLineToNChars( String str, int nbChars )
    {
        int strLength = str.length();

        if ( strLength <= nbChars )
        {
            return str;
        }

        // ── COMPUTE OUTPUT SIZE ───────────────────────────────────────────────────
        // One '\n' per full line, plus one for the last partial line.
        // ──────────────────────────────────────────────────────────────────────────
        int charsPerLine = nbChars;
        int remaining = ( strLength - nbChars ) % charsPerLine;
        int nbLines = 1 + ( ( strLength - nbChars ) / charsPerLine ) + ( remaining == 0 ? 0 : 1 );
        int nbCharsTotal = strLength + nbLines + nbLines - 2;

        char[] buffer = new char[nbCharsTotal];
        char[] orig = str.toCharArray();

        int posSrc = 0;
        int posDst = 0;

        // ── COPY FIRST CHUNK ──────────────────────────────────────────────────────
        System.arraycopy( orig, posSrc, buffer, posDst, nbChars );
        posSrc += nbChars;
        posDst += nbChars;

        // ── COPY SUBSEQUENT CHUNKS, EACH PRECEDED BY '\n' ────────────────────────
        for ( int i = 0; i < nbLines - 2; i++ )
        {
            buffer[posDst++] = '\n';

            System.arraycopy( orig, posSrc, buffer, posDst, charsPerLine );
            posSrc += charsPerLine;
            posDst += charsPerLine;
        }

        // ── COPY FINAL PARTIAL CHUNK ──────────────────────────────────────────────
        buffer[posDst++] = '\n';
        System.arraycopy( orig, posSrc, buffer, posDst, remaining == 0 ? charsPerLine : remaining );

        return new String( buffer );
    }
}
