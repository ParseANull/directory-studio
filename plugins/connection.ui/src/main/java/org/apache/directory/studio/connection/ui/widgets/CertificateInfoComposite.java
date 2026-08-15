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


import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.x509.extension.X509ExtensionUtil;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


// ── CLASS: CertificateInfoComposite — REBEL INTELLIGENCE DOSSIER VIEWER ──────────
// When Alliance Intelligence examines the Death Star plans, they have two views:
// a General briefing (who issued it, how long it's valid, fingerprints) and a
// Details drill-down (the full tech spec in a collapsible tree with a text pane
// showing the raw values).
// CertificateInfoComposite is that dual-view dossier for X509 certificates.  The
// General tab shows issued-to, issued-by, validity dates, and SHA-1/MD5 fingerprints
// in labelled text fields.  The Details tab has a three-pane SashForm:
//   1. A hierarchy tree viewer (chain root → leaf) with HierarchyContentProvider /
//      HierarchyLabelProvider and the inner CertificateChainItem model class.
//   2. A certificate fields tree (Version, SerialNumber, Signature, Issuer,
//      Validity, Subject, SubjectPublicKeyInfo, Extensions) built by
//      populateCertificateTree() whenever the hierarchy selection changes.
//   3. A monospaced read-only text widget showing the raw value of the selected
//      certificate field.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Two-tab SWT composite for displaying the contents of an {@link X509Certificate}
 * chain.
 *
 * <p><strong>General tab</strong> shows:</p>
 * <ul>
 *   <li>Issued-to (CN, O, OU, serial number)</li>
 *   <li>Issued-by (CN, O, OU)</li>
 *   <li>Validity (issued-on, expires-on dates)</li>
 *   <li>Fingerprints (SHA-1 and MD5 in colon-separated hex)</li>
 * </ul>
 *
 * <p><strong>Details tab</strong> shows a three-pane {@link SashForm}:</p>
 * <ol>
 *   <li>Certificate hierarchy tree — one node per cert in the chain; selecting a
 *       node populates the fields tree.</li>
 *   <li>Certificate fields tree — decoded ASN.1 structure of the selected cert.</li>
 *   <li>Field-value text pane — monospaced read-only text showing the raw value of
 *       the selected field.</li>
 * </ol>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CertificateInfoComposite extends Composite
{
    // ── X.500 ATTRIBUTE TYPES WE KNOW ABOUT ──────────────────────────────────────

    /**
     * The default X.500 attribute types we parse out of an {@link X500Principal}.
     * Any attribute type not in this list is ignored when building the summary map.
     */
    private static final String[] ATTRIBUTES =
        {
            "CN", //$NON-NLS-1$
            "L", //$NON-NLS-1$
            "ST", //$NON-NLS-1$
            "O", //$NON-NLS-1$
            "OU", //$NON-NLS-1$
            "C", //$NON-NLS-1$
            "STREET", //$NON-NLS-1$
            "DC", //$NON-NLS-1$
            "UID" //$NON-NLS-1$
        };

    // ── TAB INDICES ───────────────────────────────────────────────────────────────

    /** Tab index for the General tab. */
    public static final int GENERAL_TAB_INDEX = 0;

    /** Tab index for the Details tab. */
    public static final int DETAILS_TAB_INDEX = 1;


    // ── WIDGETS ───────────────────────────────────────────────────────────────────

    /** The top-level tab folder. */
    private TabFolder tabFolder;

    // General tab fields
    /** Text: subject CN. */
    private Text issuedToCN;
    /** Text: subject O. */
    private Text issuedToO;
    /** Text: subject OU. */
    private Text issuedToOU;
    /** Text: certificate serial number (hex). */
    private Text serialNumber;
    /** Text: issuer CN. */
    private Text issuedByCN;
    /** Text: issuer O. */
    private Text issuedByO;
    /** Text: issuer OU. */
    private Text issuedByOU;
    /** Text: not-before date. */
    private Text issuesOn;
    /** Text: not-after date. */
    private Text expiresOn;
    /** Text: SHA-1 fingerprint. */
    private Text fingerprintSHA1;
    /** Text: MD5 fingerprint. */
    private Text fingerprintMD5;

    // Details tab widgets
    /** Tree viewer for the certificate chain hierarchy. */
    private TreeViewer hierarchyTreeViewer;
    /** Raw SWT tree for the decoded certificate fields. */
    private Tree certificateTree;
    /** Read-only text pane for the selected field value. */
    private Text valueText;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link CertificateInfoComposite} inside {@code parent}.
     *
     * <p>Builds the tab folder, the General tab, and the Details tab in one shot.
     * Call {@link #setInput(X509Certificate[])} to populate the fields.</p>
     *
     * @param parent The parent SWT composite.
     * @param style  The SWT style bits.
     */
    public CertificateInfoComposite( Composite parent, int style )
    {
        super( parent, style );
        setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        GridLayout layout = new GridLayout( 1, false );
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        setLayout( layout );

        createTabFolder();
        createGeneralTab();
        createDetailsTab();
    }


    // ── CREATE TAB FOLDER ─────────────────────────────────────────────────────────
    /**
     * Creates the top-level {@link TabFolder} with a generous margin so the tab
     * contents have breathing room.
     */
    private void createTabFolder()
    {
        tabFolder = new TabFolder( this, SWT.TOP );
        GridLayout mainLayout = new GridLayout();
        mainLayout.marginWidth = 50;
        mainLayout.marginHeight = 50;
        tabFolder.setLayout( mainLayout );
        tabFolder.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
    }


    // ── CREATE GENERAL TAB ────────────────────────────────────────────────────────
    /**
     * Creates the General tab with four groups: Issued To, Issued By, Validity,
     * and Fingerprints.  Each group contains labelled read-only text fields.
     */
    private void createGeneralTab()
    {
        // ── OUTER CONTAINER ───────────────────────────────────────────────────────
        Composite generalContainer = new Composite( tabFolder, SWT.NONE );
        GridLayout currentLayout = new GridLayout( 1, false );
        currentLayout.marginHeight = 10;
        currentLayout.marginWidth = 10;
        generalContainer.setLayout( currentLayout );
        generalContainer.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        // ── ISSUED TO ─────────────────────────────────────────────────────────────
        Group issuedToGroup = BaseWidgetUtils.createGroup( generalContainer, Messages
            .getString( "CertificateInfoComposite.IssuedToLabel" ), 1 ); //$NON-NLS-1$
        issuedToGroup.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        Composite issuedToComposite = BaseWidgetUtils.createColumnContainer( issuedToGroup, 2, 1 );
        BaseWidgetUtils.createLabel( issuedToComposite,
            Messages.getString( "CertificateInfoComposite.CommonNameLabel" ), 1 ); //$NON-NLS-1$
        issuedToCN = BaseWidgetUtils.createLabeledText( issuedToComposite, StringUtils.EMPTY, 1 );
        BaseWidgetUtils.createLabel( issuedToComposite, Messages
            .getString( "CertificateInfoComposite.OrganizationLabel" ), 1 ); //$NON-NLS-1$
        issuedToO = BaseWidgetUtils.createLabeledText( issuedToComposite, StringUtils.EMPTY, 1 );
        BaseWidgetUtils.createLabel( issuedToComposite, Messages
            .getString( "CertificateInfoComposite.OrganizationalUnitLabel" ), 1 ); //$NON-NLS-1$
        issuedToOU = BaseWidgetUtils.createLabeledText( issuedToComposite, StringUtils.EMPTY, 1 );
        BaseWidgetUtils.createLabel( issuedToComposite, Messages
            .getString( "CertificateInfoComposite.SerialNumberLabel" ), 1 ); //$NON-NLS-1$
        serialNumber = BaseWidgetUtils.createLabeledText( issuedToComposite, StringUtils.EMPTY, 1 );

        // ── ISSUED BY ─────────────────────────────────────────────────────────────
        Group issuedFromGroup = BaseWidgetUtils.createGroup( generalContainer, Messages
            .getString( "CertificateInfoComposite.IssuedByLabel" ), 1 ); //$NON-NLS-1$
        issuedFromGroup.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        Composite issuedFromComposite = BaseWidgetUtils.createColumnContainer( issuedFromGroup, 2, 1 );
        BaseWidgetUtils.createLabel( issuedFromComposite, Messages
            .getString( "CertificateInfoComposite.CommonNameLabel" ), 1 ); //$NON-NLS-1$
        issuedByCN = BaseWidgetUtils.createLabeledText( issuedFromComposite, StringUtils.EMPTY, 1 );
        BaseWidgetUtils.createLabel( issuedFromComposite, Messages
            .getString( "CertificateInfoComposite.OrganizationLabel" ), 1 ); //$NON-NLS-1$
        issuedByO = BaseWidgetUtils.createLabeledText( issuedFromComposite, StringUtils.EMPTY, 1 );
        BaseWidgetUtils.createLabel( issuedFromComposite, Messages
            .getString( "CertificateInfoComposite.OrganizationalUnitLabel" ), 1 ); //$NON-NLS-1$
        issuedByOU = BaseWidgetUtils.createLabeledText( issuedFromComposite, StringUtils.EMPTY, 1 );

        // ── VALIDITY ──────────────────────────────────────────────────────────────
        Group validityGroup = BaseWidgetUtils.createGroup( generalContainer, Messages
            .getString( "CertificateInfoComposite.ValidityLabel" ), 1 ); //$NON-NLS-1$
        validityGroup.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        Composite generalComposite = BaseWidgetUtils.createColumnContainer( validityGroup, 2, 1 );
        BaseWidgetUtils.createLabel( generalComposite,
            Messages.getString( "CertificateInfoComposite.IssuedOnLabel" ), 1 ); //$NON-NLS-1$
        issuesOn = BaseWidgetUtils.createLabeledText( generalComposite, StringUtils.EMPTY, 1 );
        BaseWidgetUtils.createLabel( generalComposite,
            Messages.getString( "CertificateInfoComposite.ExpiresOnLabel" ), 1 ); //$NON-NLS-1$
        expiresOn = BaseWidgetUtils.createLabeledText( generalComposite, StringUtils.EMPTY, 1 );

        // ── FINGERPRINTS ──────────────────────────────────────────────────────────
        Group fingerprintsGroup = BaseWidgetUtils.createGroup( generalContainer, Messages
            .getString( "CertificateInfoComposite.FingerprintsLabel" ), 1 ); //$NON-NLS-1$
        fingerprintsGroup.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        Composite fingerprintsComposite = BaseWidgetUtils.createColumnContainer( fingerprintsGroup, 2, 1 );
        BaseWidgetUtils.createLabel( fingerprintsComposite, Messages
            .getString( "CertificateInfoComposite.SHA1FingerprintLabel" ), 1 ); //$NON-NLS-1$
        fingerprintSHA1 = BaseWidgetUtils.createLabeledText( fingerprintsComposite, StringUtils.EMPTY, 1 );
        BaseWidgetUtils.createLabel( fingerprintsComposite, Messages
            .getString( "CertificateInfoComposite.MD5FingerprintLabel" ), 1 ); //$NON-NLS-1$
        fingerprintMD5 = BaseWidgetUtils.createLabeledText( fingerprintsComposite, StringUtils.EMPTY, 1 );

        // ── CREATE TAB ITEM ───────────────────────────────────────────────────────
        TabItem generalTab = new TabItem( tabFolder, SWT.NONE, GENERAL_TAB_INDEX );
        generalTab.setText( Messages.getString( "CertificateInfoComposite.General" ) ); //$NON-NLS-1$
        generalTab.setControl( generalContainer );
    }


    // ── CREATE DETAILS TAB ────────────────────────────────────────────────────────
    /**
     * Creates the Details tab as a vertical {@link SashForm} with three panes:
     * certificate hierarchy tree viewer, certificate fields tree, and a field
     * value text pane.
     */
    private void createDetailsTab()
    {
        SashForm detailsForm = new SashForm( tabFolder, SWT.VERTICAL );
        detailsForm.setLayout( new FillLayout() );

        // ── PANE 1: CERTIFICATE HIERARCHY ────────────────────────────────────────
        Composite hierarchyContainer = new Composite( detailsForm, SWT.NONE );
        GridLayout hierarchyLayout = new GridLayout( 1, false );
        hierarchyLayout.marginTop = 10;
        hierarchyLayout.marginWidth = 10;
        hierarchyContainer.setLayout( hierarchyLayout );
        BaseWidgetUtils.createLabel( hierarchyContainer, Messages
            .getString( "CertificateInfoComposite.CertificateHierarchyLabel" ), 1 ); //$NON-NLS-1$
        hierarchyTreeViewer = new TreeViewer( hierarchyContainer );
        hierarchyTreeViewer.getTree().setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
        hierarchyTreeViewer.setContentProvider( new HierarchyContentProvider() );
        hierarchyTreeViewer.setLabelProvider( new HierarchyLabelProvider() );
        hierarchyTreeViewer.addSelectionChangedListener( event -> populateCertificateTree() );

        // ── PANE 2: CERTIFICATE FIELDS ────────────────────────────────────────────
        Composite certificateContainer = new Composite( detailsForm, SWT.NONE );
        GridLayout certificateLayout = new GridLayout( 1, false );
        certificateLayout.marginWidth = 10;
        certificateContainer.setLayout( certificateLayout );
        BaseWidgetUtils.createLabel( certificateContainer, Messages
            .getString( "CertificateInfoComposite.CertificateFieldsLabel" ), 1 ); //$NON-NLS-1$
        certificateTree = new Tree( certificateContainer, SWT.BORDER );
        certificateTree.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
        certificateTree.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             *
             * Populates the value text pane with the data stored on the selected
             * tree item.
             */
            @Override
            public void widgetSelected( final SelectionEvent event )
            {
                TreeItem item = ( TreeItem ) event.item;

                if ( ( item == null ) || ( item.getData() == null ) )
                {
                    valueText.setText( StringUtils.EMPTY );
                }
                else
                {
                    valueText.setText( item.getData().toString() );
                }
            }
        } );

        // ── PANE 3: FIELD VALUE ───────────────────────────────────────────────────
        Composite valueContainer = new Composite( detailsForm, SWT.NONE );
        GridLayout valueLayout = new GridLayout( 1, false );
        valueLayout.marginWidth = 10;
        valueLayout.marginBottom = 10;
        valueContainer.setLayout( valueLayout );
        BaseWidgetUtils.createLabel( valueContainer,
            Messages.getString( "CertificateInfoComposite.FieldValuesLabel" ), 1 ); //$NON-NLS-1$
        valueText = new Text( valueContainer, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY );
        valueText.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
        valueText.setFont( JFaceResources.getFont( JFaceResources.TEXT_FONT ) );
        valueText.setBackground( detailsForm.getBackground() );

        // ── CREATE TAB ITEM ───────────────────────────────────────────────────────
        detailsForm.setWeights( new int[]
            { 1, 2, 1 } );
        TabItem detailsTab = new TabItem( tabFolder, SWT.NONE, DETAILS_TAB_INDEX );
        detailsTab.setText( Messages.getString( "CertificateInfoComposite.Details" ) ); //$NON-NLS-1$
        detailsTab.setControl( detailsForm );
    }


    // ── SET INPUT ─────────────────────────────────────────────────────────────────
    /**
     * Populates both tabs from the given certificate chain.
     *
     * <p>The first certificate in the array is the leaf (end-entity) certificate
     * and is used to populate the General tab.  The entire chain is used to build
     * the hierarchy tree in the Details tab.</p>
     *
     * @param certificateChain The certificate chain to display; index 0 is the
     *                         leaf certificate.
     */
    public void setInput( X509Certificate[] certificateChain )
    {
        X509Certificate certificate = certificateChain[0];

        // ── GENERAL TAB: ISSUED TO ────────────────────────────────────────────────
        X500Principal issuedToPrincipal = certificate.getSubjectX500Principal();
        Map<String, String> issuedToAttributes = getAttributeMap( issuedToPrincipal );
        issuedToCN.setText( issuedToAttributes.get( "CN" ) ); //$NON-NLS-1$
        issuedToO.setText( issuedToAttributes.get( "O" ) ); //$NON-NLS-1$
        issuedToOU.setText( issuedToAttributes.get( "OU" ) ); //$NON-NLS-1$
        serialNumber.setText( certificate.getSerialNumber().toString( 16 ) );

        // ── GENERAL TAB: ISSUED BY ────────────────────────────────────────────────
        X500Principal issuedFromPrincipal = certificate.getIssuerX500Principal();
        Map<String, String> issuedFromAttributes = getAttributeMap( issuedFromPrincipal );
        issuedByCN.setText( issuedFromAttributes.get( "CN" ) ); //$NON-NLS-1$
        issuedByO.setText( issuedFromAttributes.get( "O" ) ); //$NON-NLS-1$
        issuedByOU.setText( issuedFromAttributes.get( "OU" ) ); //$NON-NLS-1$

        // ── GENERAL TAB: VALIDITY ─────────────────────────────────────────────────
        issuesOn.setText( DateFormatUtils.ISO_DATE_FORMAT.format( certificate.getNotBefore() ) );
        expiresOn.setText( DateFormatUtils.ISO_DATE_FORMAT.format( certificate.getNotAfter() ) );

        // ── GENERAL TAB: FINGERPRINTS ─────────────────────────────────────────────
        byte[] encoded2 = null;

        try
        {
            encoded2 = certificate.getEncoded();
        }
        catch ( CertificateEncodingException e )
        {
        }

        byte[] md5 = DigestUtils.md5( encoded2 );
        String md5HexString = getHexString( md5 );
        fingerprintMD5.setText( md5HexString );
        byte[] sha = DigestUtils.sha( encoded2 );
        String shaHexString = getHexString( sha );
        fingerprintSHA1.setText( shaHexString );

        // ── DETAILS TAB: BUILD HIERARCHY ──────────────────────────────────────────
        // We walk the chain from leaf to root, chaining CertificateChainItems
        // parent→child so the hierarchy viewer can show root at the top.
        CertificateChainItem parentItem = null;
        CertificateChainItem certificateItem = null;

        for ( X509Certificate cert : certificateChain )
        {
            CertificateChainItem item = new CertificateChainItem( cert );

            if ( parentItem != null )
            {
                item.child = parentItem;
                parentItem.parent = item;
            }

            if ( certificateItem == null )
            {
                certificateItem = item;
            }

            parentItem = item;
        }

        hierarchyTreeViewer.setInput( new CertificateChainItem[]
            { parentItem } );
        hierarchyTreeViewer.expandAll();
        hierarchyTreeViewer.setSelection( new StructuredSelection( certificateItem ), true );

        // ── DETAILS TAB: POPULATE FIELDS TREE ────────────────────────────────────
        certificateTree.removeAll();
        populateCertificateTree();
        valueText.setText( StringUtils.EMPTY );
    }


    // ── POPULATE CERTIFICATE TREE ─────────────────────────────────────────────────
    /**
     * Rebuilds the certificate fields tree from the certificate currently selected
     * in the hierarchy tree viewer.
     *
     * <p>Clears the existing tree and value text, then reconstructs the decoded
     * ASN.1 structure.  Called whenever the hierarchy selection changes.</p>
     */
    private void populateCertificateTree()
    {
        certificateTree.removeAll();
        valueText.setText( StringUtils.EMPTY );

        IStructuredSelection selection = ( IStructuredSelection ) hierarchyTreeViewer.getSelection();

        if ( selection.size() != 1 )
        {
            return;
        }

        CertificateChainItem certificateItem = ( CertificateChainItem ) selection.getFirstElement();
        X509Certificate certificate = certificateItem.certificate;

        // ── ROOT ──────────────────────────────────────────────────────────────────
        TreeItem rootItem = new TreeItem( certificateTree, SWT.NONE );
        Map<String, String> attributeMap = getAttributeMap( certificate.getSubjectX500Principal() );
        rootItem.setText( attributeMap.get( "CN" ) ); //$NON-NLS-1$

        // ── CERTIFICATE SUBTREE ───────────────────────────────────────────────────
        TreeItem certItem = createTreeItem( rootItem,
            Messages.getString( "CertificateInfoComposite.Certificate" ), StringUtils.EMPTY ); //$NON-NLS-1$
        createTreeItem( certItem,
            Messages.getString( "CertificateInfoComposite.Version" ), String.valueOf( certificate.getVersion() ) ); //$NON-NLS-1$
        createTreeItem( certItem,
            Messages.getString( "CertificateInfoComposite.SerialNumber" ), //$NON-NLS-1$
            certificate.getSerialNumber().toString( 16 ) );
        createTreeItem( certItem,
            Messages.getString( "CertificateInfoComposite.Signature" ), certificate.getSigAlgName() ); //$NON-NLS-1$
        createTreeItem( certItem,
            Messages.getString( "CertificateInfoComposite.Issuer" ), certificate.getIssuerX500Principal().getName() ); //$NON-NLS-1$

        TreeItem validityItem = createTreeItem( certItem,
            Messages.getString( "CertificateInfoComposite.Validity" ), StringUtils.EMPTY ); //$NON-NLS-1$
        createTreeItem( validityItem,
            Messages.getString( "CertificateInfoComposite.NotBefore" ), certificate.getNotBefore().toString() ); //$NON-NLS-1$
        createTreeItem( validityItem,
            Messages.getString( "CertificateInfoComposite.NotAfter" ), certificate.getNotAfter().toString() ); //$NON-NLS-1$

        createTreeItem( certItem,
            Messages.getString( "CertificateInfoComposite.Subject" ), certificate.getSubjectX500Principal().getName() ); //$NON-NLS-1$

        TreeItem pkiItem = createTreeItem( certItem, Messages
            .getString( "CertificateInfoComposite.SubjectPublicKeyInfo" ), StringUtils.EMPTY ); //$NON-NLS-1$
        createTreeItem(
            pkiItem,
            Messages.getString( "CertificateInfoComposite.SubjectPublicKeyAlgorithm" ), //$NON-NLS-1$
            certificate.getPublicKey().getAlgorithm() );
        createTreeItem(
            pkiItem,
            Messages.getString( "CertificateInfoComposite.SubjectPublicKey" ), //$NON-NLS-1$
            new String( Hex.encodeHex( certificate.getPublicKey().getEncoded() ) ) );

        TreeItem extItem = createTreeItem( certItem,
            Messages.getString( "CertificateInfoComposite.Extensions" ), StringUtils.EMPTY ); //$NON-NLS-1$
        populateExtensions( extItem, certificate, true );
        populateExtensions( extItem, certificate, false );

        createTreeItem( rootItem,
            Messages.getString( "CertificateInfoComposite.SignatureAlgorithm" ), certificate.getSigAlgName() ); //$NON-NLS-1$
        createTreeItem(
            rootItem,
            Messages.getString( "CertificateInfoComposite.Signature" ), //$NON-NLS-1$
            new String( Hex.encodeHex( certificate.getSignature() ) ) );

        rootItem.setExpanded( true );
        certItem.setExpanded( true );
        validityItem.setExpanded( true );
        pkiItem.setExpanded( true );
        extItem.setExpanded( true );
    }


    // ── CREATE TREE ITEM ─────────────────────────────────────────────────────────
    /**
     * Creates a child {@link TreeItem} under {@code parent} with the given display
     * text and associated data value.
     *
     * <p>The data value is shown in the value text pane when this item is selected.</p>
     *
     * @param parent The parent tree item.
     * @param field  The display text for the item.
     * @param value  The value string to show in the value text pane.
     * @return The newly created tree item.
     */
    private TreeItem createTreeItem( final TreeItem parent, final String field, final String value )
    {
        TreeItem item = new TreeItem( parent, SWT.NONE );
        item.setText( field );
        item.setData( value );

        return item;
    }


    // ── POPULATE EXTENSIONS ───────────────────────────────────────────────────────
    /**
     * Populates the Extensions node with either the critical or the non-critical
     * extension OIDs of the given certificate.
     *
     * <p>For each OID we attempt to parse the extension value to a human-readable
     * ASN.1 string.  If that fails we fall back to raw hex.  Three child items are
     * created under each OID node: OID, Critical flag, and Extension Value.</p>
     *
     * @param extensionsItem The parent tree item to add extension nodes under.
     * @param certificate    The certificate to read extensions from.
     * @param critical       {@code true} to process critical extensions,
     *                       {@code false} for non-critical.
     */
    private void populateExtensions( final TreeItem extensionsItem, final X509Certificate certificate,
        boolean critical )
    {
        Set<String> oids = critical ? certificate.getCriticalExtensionOIDs()
            : certificate.getNonCriticalExtensionOIDs();

        if ( oids != null )
        {
            for ( String oid : oids )
            {
                // ── PARSE EXTENSION VALUE ─────────────────────────────────────────
                // Try BouncyCastle first; fall back to raw hex if parsing fails.
                // ──────────────────────────────────────────────────────────────────
                byte[] extensionValueBin = certificate.getExtensionValue( oid );
                String extensionValue = null;

                try
                {
                    ASN1Object extension = X509ExtensionUtil.fromExtensionValue( extensionValueBin );
                    extensionValue = extension.toString();
                }
                catch ( IOException e )
                {
                    extensionValue = new String( Hex.encodeHex( extensionValueBin ) );
                }

                String value = Messages.getString( "CertificateInfoComposite.ExtensionOIDColon" ) + oid + '\n'; //$NON-NLS-1$
                value += Messages.getString( "CertificateInfoComposite.CriticalColon" ) + Boolean.toString( critical ) //$NON-NLS-1$
                    + '\n';
                value += Messages.getString( "CertificateInfoComposite.ExtensionValueColon" ) + extensionValue + '\n'; //$NON-NLS-1$

                // TODO: OID descriptions
                // TODO: formatting of extension value
                TreeItem item = createTreeItem( extensionsItem, oid, value );
                createTreeItem( item, Messages.getString( "CertificateInfoComposite.ExtensionOID" ), oid ); //$NON-NLS-1$
                createTreeItem( item,
                    Messages.getString( "CertificateInfoComposite.Critical" ), Boolean.toString( critical ) ); //$NON-NLS-1$
                createTreeItem( item, Messages.getString( "CertificateInfoComposite.ExtensionValue" ), extensionValue ); //$NON-NLS-1$
            }
        }
    }


    // ── GET HEX STRING ────────────────────────────────────────────────────────────
    /**
     * Formats a raw byte array as an uppercase colon-separated hex string
     * (e.g. {@code "2B:45:A9:..."}).
     *
     * @param bytes The bytes to format.
     * @return The formatted hex string.
     */
    private String getHexString( byte[] bytes )
    {
        char[] hex = Hex.encodeHex( bytes );
        StringBuilder buffer = new StringBuilder();

        for ( int i = 0; i < hex.length; i++ )
        {
            if ( i % 2 == 0 && i > 0 )
            {
                buffer.append( ':' );
            }

            buffer.append( Character.toUpperCase( hex[i] ) );
        }

        return buffer.toString();
    }


    // ── GET ATTRIBUTE MAP ─────────────────────────────────────────────────────────
    /**
     * Converts an {@link X500Principal} distinguished name into a {@link Map} keyed
     * by attribute type (upper-case, e.g. {@code "CN"}).
     *
     * <p>All types listed in {@link #ATTRIBUTES} are pre-populated with {@code "-"}
     * as a placeholder so callers never get a {@code null} value for a well-known
     * attribute.  Unknown attribute types are ignored.</p>
     *
     * @param principal The principal whose DN to parse.
     * @return A map from attribute type to value.
     */
    private Map<String, String> getAttributeMap( X500Principal principal )
    {
        Map<String, String> map = new HashMap<>();

        // ── DEFAULT PLACEHOLDERS ──────────────────────────────────────────────────
        for ( String attribute : ATTRIBUTES )
        {
            map.put( attribute, "-" ); //$NON-NLS-1$
        }

        // ── PARSE THE PRINCIPAL'S DN ──────────────────────────────────────────────
        try
        {
            String name = principal.getName();
            Dn dn = new Dn( name );

            for ( Rdn rdn : dn )
            {
                map.put( rdn.getType().toUpperCase(), rdn.getValue() );
            }
        }
        catch ( LdapInvalidDnException lide )
        {
            map.put( "CN", lide.getMessage() ); //$NON-NLS-1$
        }

        return map;
    }


    // ── INNER CLASS: HierarchyContentProvider ────────────────────────────────────
    /**
     * {@link ITreeContentProvider} for the certificate-chain hierarchy tree viewer.
     *
     * <p>Navigates a linked list of {@link CertificateChainItem} objects: each
     * item knows its parent (issuer) and its child (subject).  The root input is
     * a {@code CertificateChainItem[]} containing the single root issuer.</p>
     */
    class HierarchyContentProvider implements ITreeContentProvider
    {
        /**
         * Returns the single child of the given {@link CertificateChainItem}, or
         * an empty array if there is none (leaf certificate).
         *
         * @param parentElement The item whose child we want.
         * @return An array of zero or one {@link CertificateChainItem}.
         */
        public Object[] getChildren( Object parentElement )
        {
            if ( parentElement instanceof CertificateChainItem )
            {
                CertificateChainItem item = ( CertificateChainItem ) parentElement;

                if ( item.child != null )
                {
                    return new CertificateChainItem[]
                        { item.child };
                }
            }

            return new Object[0];
        }


        /**
         * Returns the parent {@link CertificateChainItem}, or {@code null} for the
         * root.
         *
         * @param element The item whose parent we want.
         * @return The parent item, or {@code null}.
         */
        public Object getParent( Object element )
        {
            if ( element instanceof CertificateChainItem )
            {
                CertificateChainItem item = ( CertificateChainItem ) element;

                return item.parent;
            }

            return null;
        }


        /**
         * Returns {@code true} if the item has a child.
         *
         * @param element The element to test.
         * @return {@code true} if the item has a child certificate.
         */
        public boolean hasChildren( Object element )
        {
            return getChildren( element ).length > 0;
        }


        /**
         * Returns the top-level elements from the input array.
         *
         * @param inputElement The input set on the viewer — a
         *                     {@code CertificateChainItem[]}.
         * @return The root items.
         */
        public Object[] getElements( Object inputElement )
        {
            if ( inputElement instanceof CertificateChainItem[] )
            {
                return ( CertificateChainItem[] ) inputElement;
            }

            return getChildren( inputElement );
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


    // ── INNER CLASS: HierarchyLabelProvider ──────────────────────────────────────
    /**
     * {@link LabelProvider} for the certificate-chain hierarchy tree viewer.
     *
     * <p>Displays the CN of each certificate's subject as the node label.</p>
     */
    class HierarchyLabelProvider extends LabelProvider
    {
        /**
         * {@inheritDoc}
         *
         * Returns the subject CN of the {@link CertificateChainItem}'s certificate,
         * or {@code null} for unknown element types.
         *
         * @param element The tree element to label.
         * @return The subject CN string.
         */
        @Override
        public String getText( Object element )
        {
            if ( element instanceof CertificateChainItem )
            {
                CertificateChainItem item = ( CertificateChainItem ) element;
                Map<String, String> attributeMap = getAttributeMap( item.certificate.getSubjectX500Principal() );
                return attributeMap.get( "CN" ); //$NON-NLS-1$
            }
            return null;
        }
    }


    // ── INNER CLASS: CertificateChainItem ─────────────────────────────────────────
    /**
     * Model node for the certificate hierarchy tree.
     *
     * <p>Each item wraps one {@link X509Certificate} and holds doubly-linked
     * references to its issuer ({@code parent}) and subject ({@code child}) in the
     * chain.  The root item has {@code parent == null}; the leaf item has
     * {@code child == null}.</p>
     */
    private class CertificateChainItem
    {
        /** The certificate this item represents. */
        private X509Certificate certificate;

        /** The issuer of this certificate (parent in the hierarchy), or {@code null} for the root. */
        private CertificateChainItem parent;

        /** The certificate issued by this one (child in the hierarchy), or {@code null} for the leaf. */
        private CertificateChainItem child;


        /**
         * Creates a new {@link CertificateChainItem} wrapping the given certificate.
         *
         * @param certificate The {@link X509Certificate} to wrap.
         */
        public CertificateChainItem( X509Certificate certificate )
        {
            this.certificate = certificate;
        }
    }
}
