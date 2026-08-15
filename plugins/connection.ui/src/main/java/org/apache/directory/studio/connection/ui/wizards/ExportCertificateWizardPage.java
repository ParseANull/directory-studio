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

import org.apache.commons.lang3.StringUtils;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.ui.ConnectionUIConstants;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ExportCertificateWizardPage — CHOOSING THE DATAPAD FORMAT ─────────────
// Before the Battle of Yavin, the rebels had to choose whether to transmit the
// Death Star plans as raw binary (easy for droids but unreadable by humans) or
// armoured text (anyone can open it in a text editor).  This page is that choice:
// a file-path text field + Browse button, an "overwrite" checkbox, and a combo
// for DER (binary) vs PEM (Base64 text).
// Validation ensures the path isn't a directory, doesn't already exist without
// the overwrite flag, and is actually writable.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Single wizard page for the {@link ExportCertificateWizard}.
 *
 * <p>Lets the user pick an output file path (via a text field or a native SWT
 * {@link FileDialog}) and choose a certificate export format from a
 * {@link ComboViewer}:</p>
 * <ul>
 *   <li>{@link CertificateExportFormat#DER} — raw ASN.1 binary</li>
 *   <li>{@link CertificateExportFormat#PEM} — Base64-encoded text with BEGIN/END
 *       delimiters</li>
 * </ul>
 *
 * <p>The page is complete (Finish enabled) only when the file path passes all
 * validation checks.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportCertificateWizardPage extends WizardPage
{
    // ── UI WIDGETS ────────────────────────────────────────────────────────────────

    /** Text field for the output file path. */
    private Text fileText;

    /** Checkbox: allow overwriting an existing file. */
    private Button overwriteFileButton;

    /** Combo: DER or PEM export format. */
    private ComboViewer formatComboViewer;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link ExportCertificateWizardPage}.
     *
     * <p>Sets the page title, description, and the wizard banner image from the
     * connection UI plugin image registry.</p>
     */
    public ExportCertificateWizardPage()
    {
        super( "ExportCertificateWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "ExportCertificateWizardPage.ExportCertificate" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "ExportCertificateWizardPage.PleaseSelectFileAndFormat" ) ); //$NON-NLS-1$
        setImageDescriptor( ConnectionUIPlugin.getDefault().getImageDescriptor(
            ConnectionUIConstants.IMG_CERTIFICATE_EXPORT_WIZARD ) );
    }


    // ── CREATE CONTROL ────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Builds the page body:</p>
     * <ol>
     *   <li>A "File" group with a text field, "Browse…" button, and "Overwrite
     *       existing file" checkbox.</li>
     *   <li>A "Format" group with a {@link ComboViewer} for DER / PEM.</li>
     * </ol>
     *
     * <p>The page starts in an incomplete state — the user must supply a path.</p>
     */
    public void createControl( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // ── FILE GROUP ────────────────────────────────────────────────────────────
        Group fileGroup = BaseWidgetUtils.createGroup( composite,
            Messages.getString( "ExportCertificateWizardPage.File" ), 1 ); //$NON-NLS-1$
        fileGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite fileComposite = BaseWidgetUtils.createColumnContainer( fileGroup, 2, 1 );
        fileComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // File path text field — validates on every keystroke
        fileText = BaseWidgetUtils.createText( fileComposite, StringUtils.EMPTY, 1 );
        fileText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        fileText.addModifyListener( event -> validate() );

        // "Browse…" button — opens a native save-file dialog
        Button browseButton = BaseWidgetUtils.createButton( fileComposite,
            Messages.getString( "ExportCertificateWizardPage.Browse" ), 1 ); //$NON-NLS-1$
        browseButton.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                chooseExportFile();
                validate();
            }
        } );

        // "Overwrite existing file" checkbox
        overwriteFileButton = BaseWidgetUtils.createCheckbox( fileComposite,
            Messages.getString( "ExportCertificateWizardPage.OverwriteExistingFile" ), 2 ); //$NON-NLS-1$
        overwriteFileButton.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                validate();
            }
        } );

        // ── FORMAT GROUP ──────────────────────────────────────────────────────────
        Group formatGroup = BaseWidgetUtils.createGroup( composite,
            Messages.getString( "ExportCertificateWizardPage.Format" ), 1 ); //$NON-NLS-1$
        formatGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Format combo: DER or PEM
        formatComboViewer = new ComboViewer( formatGroup );
        formatComboViewer.setContentProvider( new ArrayContentProvider() );
        formatComboViewer.setLabelProvider( new LabelProvider()
        {
            /**
             * {@inheritDoc}
             *
             * Returns the localised display name for DER or PEM format constants.
             */
            @Override
            public String getText( Object element )
            {
                if ( element instanceof CertificateExportFormat )
                {
                    CertificateExportFormat format = ( CertificateExportFormat ) element;

                    if ( format == CertificateExportFormat.DER )
                    {
                        return Messages.getString( "ExportCertificateWizardPage.X509CertificateDER" ); //$NON-NLS-1$
                    }
                    else
                    {
                        return Messages.getString( "ExportCertificateWizardPage.X509CertificatePEM" ); //$NON-NLS-1$
                    }
                }

                return super.getText( element );
            }
        } );
        formatComboViewer.setInput( new CertificateExportFormat[]
            { CertificateExportFormat.DER, CertificateExportFormat.PEM } );
        formatComboViewer.setSelection( new StructuredSelection( CertificateExportFormat.DER ) );
        formatComboViewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        displayErrorMessage( null );
        setPageComplete( false );

        setControl( composite );
    }


    // ── VALIDATE ──────────────────────────────────────────────────────────────────
    /**
     * Validates the file path and updates the page completion state.
     *
     * <p>Error conditions:</p>
     * <ul>
     *   <li>The path resolves to a directory.</li>
     *   <li>The file already exists and "overwrite" is not checked.</li>
     *   <li>The file exists but is not writable.</li>
     *   <li>The parent directory cannot be determined (e.g. root path).</li>
     * </ul>
     */
    private void validate()
    {
        File file = new File( fileText.getText() );

        if ( file.isDirectory() )
        {
            displayErrorMessage( Messages.getString( "ExportCertificateWizardPage.ErrorFileNotAFile" ) ); //$NON-NLS-1$
            return;
        }
        else if ( file.exists() && !overwriteFileButton.getSelection() )
        {
            displayErrorMessage( Messages.getString( "ExportCertificateWizardPage.ErrorFileAlreadyExists" ) ); //$NON-NLS-1$
            return;
        }
        else if ( file.exists() && !file.canWrite() )
        {
            displayErrorMessage( Messages.getString( "ExportCertificateWizardPage.ErrorFileNotWritable" ) ); //$NON-NLS-1$
            return;
        }
        else if ( file.getParentFile() == null )
        {
            displayErrorMessage( Messages.getString( "ExportCertificateWizardPage.ErrorFileDirectoryNotWritable" ) ); //$NON-NLS-1$
            return;
        }

        displayErrorMessage( null );
    }


    // ── DISPLAY ERROR MESSAGE ─────────────────────────────────────────────────────
    /**
     * Sets the error message on the page and marks it complete or incomplete.
     *
     * @param message The error string to display, or {@code null} to clear it.
     */
    protected void displayErrorMessage( String message )
    {
        setMessage( null, DialogPage.NONE );
        setErrorMessage( message );
        setPageComplete( message == null );
    }


    // ── CHOOSE EXPORT FILE ────────────────────────────────────────────────────────
    /**
     * Opens a native SWT {@link FileDialog} in SWT.SAVE mode and populates the
     * file-path text field with the selected path.
     *
     * <p>If a path is already in the text field it is used as the initial
     * filter path of the dialog.</p>
     */
    private void chooseExportFile()
    {
        FileDialog dialog = new FileDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SAVE );
        dialog.setText( Messages.getString( "ExportCertificateWizardPage.ChooseFile" ) ); //$NON-NLS-1$

        if ( !Strings.isEmpty( fileText.getText() ) )
        {
            dialog.setFilterPath( fileText.getText() );
        }

        String selectedFile = dialog.open();
        fileText.setText( CommonUIUtils.getTextValue( selectedFile ) );
    }


    // ── GET EXPORT FILE ───────────────────────────────────────────────────────────
    /**
     * Returns the export file selected by the user.
     *
     * @return The {@link File} corresponding to the text in the file-path field.
     */
    public File getExportFile()
    {
        return new File( fileText.getText() );
    }


    // ── GET CERTIFICATE EXPORT FORMAT ─────────────────────────────────────────────
    /**
     * Returns the {@link CertificateExportFormat} currently selected in the combo.
     *
     * <p>Falls back to {@link CertificateExportFormat#DER} if the combo has no
     * selection (should not happen in practice).</p>
     *
     * @return The selected export format.
     */
    public CertificateExportFormat getCertificateExportFormat()
    {
        StructuredSelection selection = ( StructuredSelection ) formatComboViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            return ( CertificateExportFormat ) selection.getFirstElement();
        }

        return CertificateExportFormat.DER;
    }


    // ── CERTIFICATE EXPORT FORMAT ─────────────────────────────────────────────────
    /**
     * Enumeration of the two supported certificate export file formats.
     *
     * <ul>
     *   <li>{@code DER} — raw binary ASN.1 encoding.</li>
     *   <li>{@code PEM} — Base64-encoded DER wrapped in
     *       {@code -----BEGIN CERTIFICATE-----} / {@code -----END CERTIFICATE-----}
     *       delimiters.</li>
     * </ul>
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    enum CertificateExportFormat
    {
        /** Raw binary DER encoding. */
        DER,

        /** Base64-armoured PEM encoding. */
        PEM
    }
}
