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


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.apache.directory.api.util.FileUtils;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.connection.ui.widgets.CertificateInfoComposite;
import org.apache.directory.studio.valueeditors.ValueEditorsActivator;
import org.apache.directory.studio.valueeditors.ValueEditorsConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// ── CLASS: CertificateDialog — THE IMPERIAL VERIFICATION SEAL ─────────────────
// Before Admiral Piett's fleet lets any ship dock, the verification officer
// scrutinises the vessel's Imperial seal (an X.509 certificate).  He checks the
// seal's validity, reads the issuer and subject names, and can load a replacement
// seal from disk or save the current one for records.  Only a valid, parseable
// seal lets the OK button illuminate.
// This dialog does exactly that for LDAP certificate attributes: display, validate,
// load from file, save to file, and confirm on OK.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Modal dialog for viewing and replacing an X.509 certificate stored in an LDAP
 * attribute (syntax OID 1.3.6.1.4.1.1466.115.121.1.8).
 * The dialog shows the certificate's subject, issuer, validity period, and other
 * details via a {@link CertificateInfoComposite}.  The user can load a replacement
 * certificate from a file (DER or PEM), save the current one to disk, or click
 * OK to commit the current certificate back to the directory.
 * The OK button is disabled until the current data parses as a valid X.509 cert.
 * Think of this as the Imperial verification terminal — inspect the seal, swap it
 * out if needed, and only authorise when it's valid.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CertificateDialog extends Dialog
{

    public static final String LOAD_FILE_NAME_TOOLTIP = "LoadFileName";

    /** The default title. */
    private static final String DIALOG_TITLE = Messages.getString( "CertificateDialog.CertificateDialog" ); //$NON-NLS-1$

    /** The button ID for the load button. */
    private static final int LOAD_BUTTON_ID = 9998;

    /** The button ID for the save button. */
    private static final int SAVE_BUTTON_ID = 9999;

    /** Hidden text to set the filename, used for UI tests. */
    private Text loadFilenameText;

    /** The current certificate binary data. */
    private byte[] currentData;

    /** The current certificate. */
    private X509Certificate currentCertificate;

    /** The return data, only set if OK button is pressed, null otherwise. */
    private byte[] returnData;

    /** The certificate info composite. */
    private CertificateInfoComposite certificateInfoComposite;


    // ── The Verification Officer Opens the Terminal ───────────────────────────
    // Admiral Piett's verification officer sits down at the terminal and loads
    // the vessel's seal data from the captured datapad for inspection.
    // We store the raw DER bytes and allow the shell to be resized.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new CertificateDialog pre-loaded with the given raw certificate bytes.
     * The bytes may be in DER (binary) or PEM (Base64) format — we parse them
     * when the dialog opens.  If {@code null} or empty, the dialog opens blank
     * and the user must load a certificate from file before clicking OK.
     *
     * <p>For example — the officer loads the vessel's seal for inspection:</p>
     * <pre>
     *   byte[] certDer = attribute.getBinaryValue();
     *   CertificateDialog dialog = new CertificateDialog(shell, certDer);
     *   dialog.open();
     * </pre>
     *
     * @param parentShell  The SWT shell that owns this dialog.
     * @param initialData  The raw certificate bytes (DER or PEM), or {@code null}.
     */
    public CertificateDialog( Shell parentShell, byte[] initialData )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.currentData = initialData;
    }


    // ── The Officer Acts on the Chosen Button ────────────────────────────────
    // OK confirms the seal and stores it; Save exports a copy to disk; Load
    // replaces the current seal with one read from disk; Cancel discards everything.
    // Each button ID routes to the appropriate action.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Routes button clicks to their respective actions.
     * <ul>
     *   <li>OK — stores the current certificate bytes as the return value.</li>
     *   <li>Save — opens a file-save dialog and writes the DER bytes to disk.</li>
     *   <li>Load — opens a file-open dialog, reads bytes, and refreshes the display.</li>
     *   <li>Cancel — sets the return data to {@code null}.</li>
     * </ul>
     *
     * @param buttonId  The ID of the button that was clicked.
     */
    @Override
    protected void buttonPressed( int buttonId )
    {
        if ( buttonId == IDialogConstants.OK_ID )
        {
            returnData = currentData;
        }
        else if ( buttonId == SAVE_BUTTON_ID )
        {
            FileDialog fileDialog = new FileDialog( getShell(), SWT.SAVE );
            fileDialog.setText( Messages.getString( "CertificateDialog.SaveCertificate" ) ); //$NON-NLS-1$
            // fileDialog.setFilterExtensions(new String[]{"*.pem"});
            String returnedFileName = fileDialog.open();
            if ( returnedFileName != null )
            {
                try
                {
                    File file = new File( returnedFileName );
                    FileUtils.writeByteArrayToFile( file, currentData );
                }
                catch ( IOException e )
                {
                    ConnectionUIPlugin.getDefault().getExceptionHandler().handleException(
                        new Status( IStatus.ERROR, ValueEditorsConstants.PLUGIN_ID, IStatus.ERROR, Messages
                            .getString( "CertificateDialog.CantWriteToFile" ), e ) ); //$NON-NLS-1$
                }
            }
        }
        else if ( buttonId == LOAD_BUTTON_ID )
        {
            FileDialog fileDialog = new FileDialog( getShell(), SWT.OPEN );
            fileDialog.setText( Messages.getString( "CertificateDialog.LoadCertificate" ) ); //$NON-NLS-1$
            String returnedFileName = fileDialog.open();
            if ( returnedFileName != null )
            {
                loadFile( returnedFileName );
            }
        }
        else
        {
            returnData = null;
        }

        super.buttonPressed( buttonId );
    }


    // ── The Officer Reads a Replacement Seal from the Archive Vault ──────────
    // An officer hands over a data tape from the Scarif archives and the terminal
    // operator inserts it, reads the bytes, and refreshes the seal display panel.
    // If the tape is unreadable, an error handler is invoked.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads raw certificate bytes from the given file path and refreshes the
     * dialog's display panel.
     * Accepts DER and PEM files — the update step normalises both to DER.
     * Reports any I/O failure through the connection UI exception handler.
     *
     * @param fileName  Absolute path to the certificate file to load.
     */
    private void loadFile( String fileName )
    {
        try
        {
            File file = new File( fileName );
            currentData = FileUtils.readFileToByteArray( file );
            updateInput();
        }
        catch ( IOException e )
        {
            ConnectionUIPlugin.getDefault().getExceptionHandler().handleException(
                new Status( IStatus.ERROR, ValueEditorsConstants.PLUGIN_ID, IStatus.ERROR, Messages
                    .getString( "CertificateDialog.CantReadFile" ), e ) ); //$NON-NLS-1$
        }
    }


    // ── The Officer Labels the Verification Terminal ──────────────────────────
    // The terminal door is labelled "Certificate Editor" and the Imperial seal
    // icon is mounted in the corner so everyone knows what they're looking at.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Configures the dialog shell — sets the window title and icon.
     * Eclipse calls this just before the dialog becomes visible.
     *
     * @param shell  The SWT Shell we're configuring.
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( DIALOG_TITLE );
        shell.setImage( ValueEditorsActivator.getDefault().getImage( ValueEditorsConstants.IMG_CERTIFICATEEDITOR ) );
    }


    // ── The Officer Lays Out the Action Buttons ───────────────────────────────
    // The terminal has four buttons: Load Seal, Save Seal, Confirm (OK), and
    // Reject (Cancel).  There's also a hidden filename text field used by
    // automated UI tests to inject a file path without opening a file chooser.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Adds Load, Save, OK, and Cancel buttons to the button bar.
     * A hidden text widget ({@code loadFilenameText}) is included for automated
     * UI test support — tests can set the filename programmatically via its
     * modify listener instead of opening a native file dialog.
     *
     * @param parent  The composite hosting the button bar.
     */
    @Override
    protected void createButtonsForButtonBar( Composite parent )
    {
        ((GridLayout) parent.getLayout()).numColumns++;
        loadFilenameText = new Text( parent, SWT.NONE );
        loadFilenameText.setToolTipText( LOAD_FILE_NAME_TOOLTIP );
        loadFilenameText.setBackground( parent.getBackground() );
        loadFilenameText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                loadFile( loadFilenameText.getText() );
            }
        } );

        createButton( parent, LOAD_BUTTON_ID, Messages.getString( "CertificateDialog.LoadCertificateButton" ), false ); //$NON-NLS-1$
        createButton( parent, SAVE_BUTTON_ID, Messages.getString( "CertificateDialog.SaveCertificateButton" ), false ); //$NON-NLS-1$
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    // ── The Officer Displays the Seal Details ────────────────────────────────
    // The terminal renders the certificate's subject, issuer, validity, and
    // algorithm in a structured info panel so the officer can see what he's
    // authorising.  If the initial data is present, the panel is populated immediately.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the main content area — a {@link CertificateInfoComposite} that
     * displays the X.509 certificate's details (subject, issuer, validity, etc.).
     * If initial data was provided to the constructor, we parse and display it
     * immediately; otherwise the panel is blank until the user loads a file.
     *
     * @param parent  The parent composite provided by JFace's dialog framework.
     * @return        The top-level composite containing the certificate info panel.
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        // create composite
        Composite composite = ( Composite ) super.createDialogArea( parent );

        Composite certificateInfoContainer = BaseWidgetUtils.createColumnContainer( composite, 1, 1 );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH * 3 / 2 );
        gd.heightHint = convertVerticalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        certificateInfoContainer.setLayoutData( gd );

        certificateInfoComposite = new CertificateInfoComposite( certificateInfoContainer, SWT.NONE );
        if ( currentData != null && currentData.length > 0 )
        {
            updateInput();
        }

        applyDialogFont( composite );
        return composite;
    }


    // ── The Terminal Parses and Validates the Seal ────────────────────────────
    // After loading new bytes, the terminal attempts to parse them as an X.509
    // certificate.  A valid seal illuminates the OK button; an invalid one dims
    // it and triggers an error so the officer knows the seal is fake.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the current raw bytes as an X.509 certificate and refreshes the
     * info composite.
     * On success: normalises {@code currentData} to DER (in case a PEM file was
     * loaded), feeds the certificate into the info composite, and enables the OK
     * button.
     * On failure: logs an error via the exception handler and disables OK so the
     * user can't commit invalid data.
     */
    private void updateInput()
    {
        try
        {
            // parse the certificate
            currentCertificate = generateCertificate( currentData );

            // update the byte[], this must be done for the case that
            // the certificate loaded from file is in PEM format
            currentData = currentCertificate.getEncoded();

            // set the input and update button
            certificateInfoComposite.setInput( new X509Certificate[]
                { currentCertificate } );
            if ( getButton( IDialogConstants.OK_ID ) != null )
            {
                getButton( IDialogConstants.OK_ID ).setEnabled( true );
            }
        }
        catch ( Exception e )
        {
            ConnectionUIPlugin.getDefault().getExceptionHandler().handleException(
                new Status( IStatus.ERROR, ValueEditorsConstants.PLUGIN_ID, IStatus.ERROR, Messages
                    .getString( "CertificateDialog.CantParseCertificate" ), //$NON-NLS-1$
                    e ) );
            if ( getButton( IDialogConstants.OK_ID ) != null )
            {
                getButton( IDialogConstants.OK_ID ).setEnabled( false );
            }
        }
    }


    // ── The Officer Retrieves the Confirmed Seal Bytes ────────────────────────
    // After the officer clicks OK, the terminal hands the verified DER bytes
    // to the fleet commander for filing in the ship's registry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw DER-encoded certificate bytes confirmed by the user on OK.
     * Returns {@code null} if the user cancelled or if OK was never pressed.
     *
     * <p>For example — the editor retrieves the confirmed certificate:</p>
     * <pre>
     *   if (dialog.open() == Dialog.OK) {
     *       byte[] certBytes = dialog.getData();
     *       // Store certBytes as the new attribute value
     *   }
     * </pre>
     *
     * @return  The DER certificate bytes, or {@code null} if cancelled.
     */
    public byte[] getData()
    {
        return returnData;
    }


    // ── The Terminal Summarises the Seal for the Fleet Log ───────────────────
    // For the table cell display, we just need a one-liner: type, version, and
    // subject name.  R2-D2 reads the DER bytes and outputs "X509v3: cn=example".
    // If the bytes are corrupt, we report the byte count so at least something useful appears.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Produces a concise human-readable summary of the certificate stored in
     * the given raw bytes, suitable for display in the LDAP browser table cell.
     * Format: {@code "<type>v<version>: <subject-DN>"}, for example
     * {@code "X.509v3: CN=example.com, O=Acme"}.
     * If the bytes can't be parsed, falls back to
     * {@code "Invalid certificate (NNN bytes)"}.
     *
     * <p>For example — the officer reads the quick vessel ID:</p>
     * <pre>
     *   String info = CertificateDialog.getCertificateInfo(derBytes);
     *   // → "X.509v3: CN=www.example.com, O=Acme Corp"
     * </pre>
     *
     * @param data  Raw DER or PEM bytes of the certificate.
     * @return      A one-line summary string, never {@code null}.
     */
    public static String getCertificateInfo( byte[] data )
    {
        try
        {
            X509Certificate certificate = generateCertificate( data );
            if ( certificate != null )
            {
                String name = certificate.getSubjectX500Principal().getName();
                int version = certificate.getVersion();
                String type = certificate.getType();
                return type + "v" + version + ": " + name; //$NON-NLS-1$ //$NON-NLS-2$
            }
            else
            {
                return NLS.bind( Messages.getString( "CertificateDialog.InvalidCertificate" ), data.length ); //$NON-NLS-1$
            }
        }
        catch ( Exception e )
        {
            return NLS.bind( Messages.getString( "CertificateDialog.InvalidCertificate" ), data.length ); //$NON-NLS-1$
        }
    }


    // ── The Terminal Parses the Raw Seal Into a Certificate Object ───────────
    // The terminal feeds the raw DER bytes into the Java CertificateFactory and
    // gets back a proper X509Certificate object for inspection.
    // If the bytes aren't a valid X.509 cert, null is returned.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a raw byte array into an {@link X509Certificate} using Java's
     * standard {@link CertificateFactory}.
     * Returns {@code null} if the bytes parse to a {@link Certificate} type
     * other than X.509.
     *
     * @param data  The raw DER bytes of the certificate.
     * @return      The parsed {@link X509Certificate}, or {@code null} if not X.509.
     * @throws CertificateException  if the bytes cannot be parsed as any certificate.
     */
    private static X509Certificate generateCertificate( byte[] data ) throws CertificateException
    {
        CertificateFactory cf = CertificateFactory.getInstance( "X.509" ); //$NON-NLS-1$
        Certificate certificate = cf.generateCertificate( new ByteArrayInputStream( data ) );
        if ( certificate instanceof X509Certificate )
        {
            return ( X509Certificate ) certificate;
        }

        return null;
    }

}
