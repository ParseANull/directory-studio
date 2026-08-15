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
package org.apache.directory.studio.connection.ui.widgets;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Iterator;

import org.apache.directory.api.util.FileUtils;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.core.StudioKeyStoreManager;
import org.apache.directory.studio.connection.ui.ConnectionUIConstants;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.connection.ui.dialogs.CertificateInfoDialog;
import org.apache.directory.studio.connection.ui.wizards.ExportCertificateWizard;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;


// ── CLASS: CertificateListComposite — REBEL TRUST REGISTRY PANEL ─────────────────
// Alliance Intelligence maintains a register of trusted agents — officers whose
// identity documents have been verified and added to the database.  The panel has a
// scrollable list on the left and four buttons on the right: View (inspect the
// dossier), Add (load a new identity document from a file), Remove (revoke trust),
// and Export (save the document to disk for sharing).
// CertificateListComposite is that panel for X509 certificates.  It wraps a
// StudioKeyStoreManager and displays its certificates in a TableViewer using
// KeyStoreContentProvider (reads the keystore) and KeyStoreLabelProvider (shows the
// subject DN and the certificate icon).  The four buttons perform exactly the actions
// described above.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * SWT composite that displays a list of trusted {@link X509Certificate} objects
 * and provides View, Add, Remove, and Export operations.
 *
 * <pre>
 * +-------------------------------------------------+
 * | +------------------------------------+          |
 * | | abc                                | (View)   |
 * | | xyz                                | (Add)    |
 * | |                                    | (Remove) |
 * | |                                    | (Export) |
 * | +------------------------------------+          |
 * +-------------------------------------------------+
 * </pre>
 *
 * <p>The widget is backed by a {@link StudioKeyStoreManager} set via
 * {@link #setInput(StudioKeyStoreManager)}.  All four buttons update the keystore
 * through the manager's API and then refresh the table viewer.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CertificateListComposite extends Composite
{
    // ── FIELDS ────────────────────────────────────────────────────────────────────

    /** The keystore that backs this list. */
    private StudioKeyStoreManager keyStoreManager;

    /** The table viewer showing the certificates. */
    private TableViewer tableViewer;

    /** "View" button — opens {@link CertificateInfoDialog}. */
    private Button viewButton;

    /** "Add" button — opens a FileDialog to load a certificate file. */
    private Button addButton;

    /** "Remove" button — removes selected certificates from the keystore. */
    private Button removeButton;

    /** "Export" button — opens the {@link ExportCertificateWizard}. */
    private Button exportButton;


    // ── LISTENERS ─────────────────────────────────────────────────────────────────

    /**
     * Selection-changed listener: adjusts button enabled state based on how many
     * items are selected.
     *
     * <ul>
     *   <li>1 item: View, Remove, Export all enabled.</li>
     *   <li>Multiple items: only Remove enabled.</li>
     *   <li>No items: all three disabled.</li>
     * </ul>
     */
    private ISelectionChangedListener tableViewerSelectionListener = event -> {
        viewButton.setEnabled( ( ( IStructuredSelection ) event.getSelection() ).size() == 1 );
        removeButton.setEnabled( !event.getSelection().isEmpty() );
        exportButton.setEnabled( ( ( IStructuredSelection ) event.getSelection() ).size() == 1 );
    };

    /**
     * Double-click listener: opens {@link CertificateInfoDialog} for the double-
     * clicked certificate.
     */
    private IDoubleClickListener tableViewerDoubleClickListener = event -> openCertificate( event.getSelection() );

    /**
     * View button listener: opens {@link CertificateInfoDialog} for the selected
     * certificate.
     */
    private SelectionAdapter viewButtonSelectionListener = new SelectionAdapter()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void widgetSelected( SelectionEvent event )
        {
            openCertificate( tableViewer.getSelection() );
        }
    };

    /**
     * Add button listener: opens a native {@link FileDialog}, reads the selected
     * file as an X.509 certificate, adds it to the keystore, and refreshes the
     * table.
     */
    private SelectionAdapter addButtonSelectionListener = new SelectionAdapter()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void widgetSelected( SelectionEvent event )
        {
            // ── OPEN FILE DIALOG ──────────────────────────────────────────────────
            FileDialog dialog = new FileDialog( getShell(), SWT.OPEN );
            dialog.setText( Messages.getString( "CertificateListComposite.LoadCertificate" ) ); //$NON-NLS-1$
            String returnedFileName = dialog.open();

            if ( returnedFileName != null )
            {
                try
                {
                    // ── READ AND ADD ──────────────────────────────────────────────
                    X509Certificate certificate = generateCertificate( FileUtils.readFileToByteArray( new File(
                        returnedFileName ) ) );

                    keyStoreManager.addCertificate( certificate );

                    tableViewer.refresh();
                    tableViewer.setSelection( new StructuredSelection( certificate ) );
                }
                catch ( Exception ex )
                {
                    MessageDialog.openError( addButton.getShell(),
                        Messages.getString( "CertificateListComposite.ErrorDialogTitle" ), //$NON-NLS-1$
                        NLS.bind( Messages.getString( "CertificateListComposite.ErrorDialogMessage" ), //$NON-NLS-1$
                            ex.getMessage() ) );
                }
            }
        }
    };

    /**
     * Remove button listener: removes all selected certificates from the keystore
     * and refreshes the table.
     */
    private SelectionAdapter removeButtonSelectionListener = new SelectionAdapter()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void widgetSelected( SelectionEvent event )
        {
            IStructuredSelection selection = ( IStructuredSelection ) tableViewer.getSelection();
            Iterator<?> iterator = selection.iterator();

            while ( iterator.hasNext() )
            {
                X509Certificate certificate = ( X509Certificate ) iterator.next();

                try
                {
                    keyStoreManager.removeCertificate( certificate );
                }
                catch ( CertificateException ce )
                {
                    throw new RuntimeException( ce );
                }
            }

            tableViewer.refresh();
        }
    };

    /**
     * Export button listener: opens the {@link ExportCertificateWizard} for the
     * selected certificate.
     */
    private SelectionAdapter exportButtonSelectionListener = new SelectionAdapter()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void widgetSelected( SelectionEvent event )
        {
            X509Certificate certificate = ( X509Certificate ) ( ( IStructuredSelection ) tableViewer.getSelection() )
                .getFirstElement();

            WizardDialog dialog = new WizardDialog( getShell(), new ExportCertificateWizard( certificate ) );
            dialog.open();
        }
    };


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link CertificateListComposite} inside {@code parent}.
     *
     * <p>Builds the two-column layout (table | buttons) and wires up all
     * listeners.  Call {@link #setInput(StudioKeyStoreManager)} to populate the
     * table.</p>
     *
     * @param parent The parent SWT composite.
     * @param style  The SWT style bits.
     */
    public CertificateListComposite( Composite parent, int style )
    {
        super( parent, style );
        GridLayout layout = new GridLayout( 1, false );
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        setLayout( layout );
        setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // ── INNER CONTAINER (2 COLUMNS: TABLE | BUTTONS) ──────────────────────────
        Composite container = new Composite( this, SWT.NONE );
        layout = new GridLayout( 2, false );
        container.setLayout( layout );
        container.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

        // ── TABLE VIEWER ──────────────────────────────────────────────────────────
        tableViewer = new TableViewer( container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
        GridData gridData = new GridData( GridData.FILL, GridData.FILL, true, true );
        gridData.widthHint = 360;
        gridData.heightHint = 10;
        tableViewer.getTable().setLayoutData( gridData );
        tableViewer.setContentProvider( new KeyStoreContentProvider() );
        tableViewer.setLabelProvider( new KeyStoreLabelProvider() );
        tableViewer.addSelectionChangedListener( tableViewerSelectionListener );
        tableViewer.addDoubleClickListener( tableViewerDoubleClickListener );

        createButtons( container );
    }


    // ── CREATE BUTTONS ────────────────────────────────────────────────────────────
    /**
     * Creates the four action buttons (View, Add, Remove, Export) in a single-
     * column composite and wires their listeners.
     *
     * @param container The parent composite for the button column.
     */
    private void createButtons( Composite container )
    {
        Composite buttonContainer = BaseWidgetUtils.createColumnContainer( container, 1, 1 );
        buttonContainer.setLayoutData( new GridData( GridData.FILL, GridData.FILL, false, false ) );

        // ── VIEW BUTTON ───────────────────────────────────────────────────────────
        viewButton = BaseWidgetUtils.createButton( buttonContainer, Messages
            .getString( "CertificateListComposite.ViewButton" ), 1 );//$NON-NLS-1$
        viewButton.setEnabled( false );
        viewButton.addSelectionListener( viewButtonSelectionListener );

        // ── ADD BUTTON ────────────────────────────────────────────────────────────
        addButton = BaseWidgetUtils.createButton( buttonContainer, Messages
            .getString( "CertificateListComposite.AddButton" ), 1 ); //$NON-NLS-1$
        addButton.addSelectionListener( addButtonSelectionListener );

        // ── REMOVE BUTTON ─────────────────────────────────────────────────────────
        removeButton = BaseWidgetUtils.createButton( buttonContainer, Messages
            .getString( "CertificateListComposite.RemoveButton" ), 1 ); //$NON-NLS-1$
        removeButton.setEnabled( false );
        removeButton.addSelectionListener( removeButtonSelectionListener );

        // ── EXPORT BUTTON ─────────────────────────────────────────────────────────
        exportButton = BaseWidgetUtils.createButton( buttonContainer, Messages
            .getString( "CertificateListComposite.ExportButton" ), 1 ); //$NON-NLS-1$
        exportButton.setEnabled( false );
        exportButton.addSelectionListener( exportButtonSelectionListener );
    }


    // ── GENERATE CERTIFICATE ─────────────────────────────────────────────────────
    /**
     * Parses a raw byte array as an X.509 certificate using the JCE
     * {@link CertificateFactory}.
     *
     * @param data The DER or PEM-encoded certificate bytes.
     * @return The parsed {@link X509Certificate}, or {@code null} if the factory
     *         returns a non-X.509 {@link Certificate}.
     * @throws CertificateException If the bytes cannot be parsed.
     */
    private static X509Certificate generateCertificate( byte[] data ) throws CertificateException
    {
        CertificateFactory certificateFactory = CertificateFactory.getInstance( "X.509" ); //$NON-NLS-1$
        Certificate certificate = certificateFactory.generateCertificate( new ByteArrayInputStream( data ) );

        if ( certificate instanceof X509Certificate )
        {
            return ( X509Certificate ) certificate;
        }

        return null;
    }


    // ── SET INPUT ─────────────────────────────────────────────────────────────────
    /**
     * Sets the {@link StudioKeyStoreManager} that backs this composite and
     * refreshes the table viewer.
     *
     * @param keyStoreManager The keystore manager whose certificates to display.
     */
    public void setInput( StudioKeyStoreManager keyStoreManager )
    {
        this.keyStoreManager = keyStoreManager;
        tableViewer.setInput( keyStoreManager );
    }


    // ── INNER CLASS: KeyStoreContentProvider ──────────────────────────────────────
    /**
     * {@link IStructuredContentProvider} for the certificate table viewer.
     *
     * <p>Reads all certificates from the {@link StudioKeyStoreManager} on each
     * refresh.  Any {@link CertificateException} is wrapped and rethrown as a
     * {@link RuntimeException} since content providers cannot throw checked
     * exceptions.</p>
     */
    private class KeyStoreContentProvider implements IStructuredContentProvider
    {
        /**
         * {@inheritDoc}
         *
         * Returns all certificates stored in the {@link StudioKeyStoreManager}.
         *
         * @param inputElement A {@link StudioKeyStoreManager} set as the viewer
         *                     input.
         * @return An array of {@link X509Certificate} objects, or an empty array
         *         for unknown input types.
         */
        public Object[] getElements( Object inputElement )
        {
            if ( inputElement instanceof StudioKeyStoreManager )
            {
                try
                {
                    return ( ( StudioKeyStoreManager ) inputElement ).getCertificates();
                }
                catch ( CertificateException e )
                {
                    throw new RuntimeException( e );
                }
            }

            return new Object[]
                {};
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose()
        {
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
        {
        }

    }


    // ── INNER CLASS: KeyStoreLabelProvider ────────────────────────────────────────
    /**
     * {@link LabelProvider} for the certificate table viewer.
     *
     * <p>Displays the subject DN of each {@link X509Certificate} as the row text,
     * falling back to a localised "Untitled Certificate" placeholder for certs with
     * an empty subject.  Also returns the certificate icon from the image
     * registry.</p>
     */
    class KeyStoreLabelProvider extends LabelProvider
    {
        /**
         * {@inheritDoc}
         *
         * Returns the subject DN of the certificate, or a localised
         * "Untitled Certificate" placeholder if the subject is empty.
         *
         * @param element An {@link X509Certificate} from the table.
         * @return The display label.
         */
        @Override
        public String getText( Object element )
        {
            if ( element instanceof X509Certificate )
            {
                X509Certificate certificate = ( X509Certificate ) element;

                String certificateName = certificate.getSubjectX500Principal().getName();

                if ( Strings.isEmpty( certificateName ) )
                {
                    return Messages.getString( "CertificateListComposite.UntitledCertificate" ); //$NON-NLS-1$
                }
                else
                {
                    return certificateName;
                }
            }

            return super.getText( element );
        }


        /**
         * {@inheritDoc}
         *
         * Returns the certificate icon from the connection UI image registry.
         *
         * @param element An {@link X509Certificate} from the table.
         * @return The certificate {@link Image}, or the default icon for other
         *         element types.
         */
        @Override
        public Image getImage( Object element )
        {
            if ( element instanceof X509Certificate )
            {
                return ConnectionUIPlugin.getDefault().getImage( ConnectionUIConstants.IMG_CERTIFICATE );
            }

            return super.getImage( element );
        }
    }


    // ── OPEN CERTIFICATE ─────────────────────────────────────────────────────────
    /**
     * Opens a {@link CertificateInfoDialog} for the first certificate in the
     * given selection.
     *
     * @param selection The current table selection.
     */
    private void openCertificate( ISelection selection )
    {
        IStructuredSelection structuredSelection = ( IStructuredSelection ) selection;
        X509Certificate certificate = ( X509Certificate ) structuredSelection.getFirstElement();
        new CertificateInfoDialog( getShell(), new X509Certificate[]
            { certificate } ).open();
    }
}
