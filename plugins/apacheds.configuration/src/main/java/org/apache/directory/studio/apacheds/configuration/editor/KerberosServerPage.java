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
package org.apache.directory.studio.apacheds.configuration.editor;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.server.config.beans.ChangePasswordServerBean;
import org.apache.directory.server.config.beans.DirectoryServiceBean;
import org.apache.directory.server.config.beans.InterceptorBean;
import org.apache.directory.server.config.beans.KdcServerBean;
import org.apache.directory.server.config.beans.TransportBean;
import org.apache.directory.shared.kerberos.codec.types.EncryptionType;
import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;


// ── CLASS: KerberosServerPage — THE EMPIRE ENGINEERS THE AUTHENTICATION FORTRESS ─────────────────
// In Return of the Jedi, Moff Jerjerrod and his engineers oversee the construction of
// the second Death Star's security systems — the tractor beams, the energy shields,
// the access-credential systems that let only authorised ships dock.
// Kerberos is exactly that kind of security fortress: a ticket-based authentication
// protocol that makes every user prove they have a valid credential before they're
// let anywhere near the LDAP directory.  This page lets the administrator engineer
// that fortress: which port the Kerberos KDC listens on, which encryption algorithms
// are permitted, how long tickets last, and whether the Change Password service runs
// alongside.  Think of every widget on this page as a switch in the engineering bay.
// ─────────────────────────────────────────────────────────────────────────────────────────────────
/**
 * The "Kerberos Server" page of the ApacheDS server configuration editor.
 * Kerberos is a ticket-based network authentication protocol; ApacheDS can act as
 * a Key Distribution Centre (KDC) issuing service tickets for users who authenticate
 * to the directory.  This page covers:
 * <ul>
 *   <li>Enabling/disabling the KDC server and setting its port and address</li>
 *   <li>Enabling/disabling the Change Password server (port 464)</li>
 *   <li>The primary KDC realm and LDAP search base DN</li>
 *   <li>Supported encryption types (AES, DES, RC4, etc.)</li>
 *   <li>Ticket policy flags (renewable, forwardable, proxiable, postdated, etc.)</li>
 *   <li>Ticket lifetime and clock-skew tolerance</li>
 * </ul>
 * Think of this page as Moff Jerjerrod's engineering console for the authentication fortress.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class KerberosServerPage extends ServerConfigurationEditorPage
{
    /** The Page ID*/
    public static final String ID = KerberosServerPage.class.getName(); //$NON-NLS-1$

    /** The Page Title */
    private static final String TITLE = Messages.getString( "KerberosServerPage.KerberosServer" ); //$NON-NLS-1$

    /** The encryption types supported by ApacheDS */
    private static final EncryptionType[] SUPPORTED_ENCRYPTION_TYPES = new EncryptionType[]
        {
            EncryptionType.DES_CBC_MD5,
            EncryptionType.DES3_CBC_SHA1_KD,
            EncryptionType.AES128_CTS_HMAC_SHA1_96,
            EncryptionType.AES256_CTS_HMAC_SHA1_96,
            EncryptionType.RC4_HMAC
    };

    // UI Controls
    // The Kerberos transport
    private Button enableKerberosCheckbox;
    private Text kerberosPortText;
    private Text kerberosAddressText;

    // The ChangePassword transport
    private Button enableChangePasswordCheckbox;
    private Text changePasswordPortText;
    private Text changePasswordAddressText;

    // The basic Kerberos settings
    private Text primaryKdcRealmText;
    private Text kdcSearchBaseDnText;
    private CheckboxTableViewer encryptionTypesTableViewer;

    // The kerberos Tickets settings
    private Button verifyBodyChecksumCheckbox;
    private Button allowEmptyAddressesCheckbox;
    private Button allowForwardableAddressesCheckbox;
    private Button requirePreAuthByEncryptedTimestampCheckbox;
    private Button allowPostdatedTicketsCheckbox;
    private Button allowRenewableTicketsCheckbox;
    private Button allowProxiableTicketsCheckbox;
    private Text maximumRenewableLifetimeText;
    private Text maximumTicketLifetimeText;
    private Text allowableClockSkewText;

    // UI Controls Listeners
    /**
     * The Kerberos checkbox listener
     */
    private SelectionAdapter enableKerberosCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            boolean enabled = enableKerberosCheckbox.getSelection();
            enableKerberosServer( getDirectoryServiceBean(), enabled );

            setEnabled( kerberosPortText, enabled );
            setEnabled( kerberosAddressText, enabled );
        }
    };

    /**
     * The Kerberos port listener
     */
    private ModifyListener kerberosPortTextListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            setKerberosPort( getDirectoryServiceBean(), kerberosPortText.getText() );
        }
    };

    /**
     * The Kerberos address modify listener
     */
    private ModifyListener kerberosAddressTextListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            setKerberosAddress( getDirectoryServiceBean(), kerberosAddressText.getText() );
        }
    };

    /**
     * The ChangePassword checkbox listener
     */
    private SelectionAdapter enableChangePasswordCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            boolean enabled = enableChangePasswordCheckbox.getSelection();

            getChangePasswordServerBean().setEnabled( enabled );
            setEnabled( changePasswordPortText, enabled );
            setEnabled( changePasswordAddressText, enabled );
        }
    };


    /**
     * The ChangePassword port listener
     */
    private ModifyListener changePasswordPortTextListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            setChangePasswordPort( getDirectoryServiceBean(), changePasswordPortText.getText() );
        }
    };


    /**
     * The ChangePassword address modify listener
     */
    private ModifyListener changePasswordAddressTextListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            setChangePasswordAddress( getDirectoryServiceBean(), changePasswordAddressText.getText() );
        }
    };


    private ModifyListener primaryKdcRealmTextListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            getKdcServerBean().setKrbPrimaryRealm( primaryKdcRealmText.getText() );
        }
    };
    private ModifyListener kdcSearchBaseDnTextListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            String searchBaseDnValue = kdcSearchBaseDnText.getText();

            try
            {
                Dn searchBaseDn = new Dn( searchBaseDnValue );
                getKdcServerBean().setSearchBaseDn( searchBaseDn );
            }
            catch ( LdapInvalidDnException e1 )
            {
                // Stay silent
            }
        }
    };
    private ICheckStateListener encryptionTypesTableViewerListener = new ICheckStateListener()
    {
        public void checkStateChanged( CheckStateChangedEvent event )
        {
            // Checking if the last encryption type is being unchecked
            if ( ( getKdcServerBean().getKrbEncryptionTypes().size() == 1 ) && ( event.getChecked() == false ) )
            {
                // Displaying an error to the user
                CommonUIUtils.openErrorDialog( Messages
                    .getString( "KerberosServerPage.AtLeastOneEncryptionTypeMustBeSelected" ) ); //$NON-NLS-1$

                // Reverting the current checked state
                encryptionTypesTableViewer.setChecked( event.getElement(), !event.getChecked() );

                // Exiting
                return;
            }

            // Setting the editor as dirty
            setEditorDirty();

            // Clearing previous encryption types
            getKdcServerBean().getKrbEncryptionTypes().clear();

            // Getting all selected encryption types
            Object[] selectedEncryptionTypeObjects = encryptionTypesTableViewer.getCheckedElements();

            // Adding each encryption type
            for ( Object encryptionTypeObject : selectedEncryptionTypeObjects )
            {
                if ( encryptionTypeObject instanceof EncryptionType )
                {
                    EncryptionType encryptionType = ( EncryptionType ) encryptionTypeObject;

                    getKdcServerBean().addKrbEncryptionTypes( encryptionType.getName() );
                }
            }
        }
    };
    private SelectionAdapter verifyBodyChecksumCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            getKdcServerBean().setKrbBodyChecksumVerified( verifyBodyChecksumCheckbox.getSelection() );
        }
    };
    private SelectionAdapter allowEmptyAddressesCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            getKdcServerBean().setKrbEmptyAddressesAllowed( allowEmptyAddressesCheckbox.getSelection() );
        }
    };
    private SelectionAdapter allowForwardableAddressesCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            getKdcServerBean().setKrbForwardableAllowed( allowForwardableAddressesCheckbox.getSelection() );
        }
    };
    private SelectionAdapter requirePreAuthByEncryptedTimestampCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            getKdcServerBean().setKrbPaEncTimestampRequired(
                requirePreAuthByEncryptedTimestampCheckbox.getSelection() );
        }
    };
    private SelectionAdapter allowPostdatedTicketsCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            getKdcServerBean().setKrbPostdatedAllowed( allowPostdatedTicketsCheckbox.getSelection() );
        }
    };


    /**
     * The Allow Renewable Tickets listener
     */
    private SelectionAdapter allowRenewableTicketsCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            getKdcServerBean().setKrbRenewableAllowed( allowRenewableTicketsCheckbox.getSelection() );
        }
    };


    /**
     * The Allow Proxiable Tickets listener
     */
    private SelectionAdapter allowProxiableTicketsCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            getKdcServerBean().setKrbProxiableAllowed( allowProxiableTicketsCheckbox.getSelection() );
        }
    };


    private ModifyListener maximumRenewableLifetimeTextListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            getKdcServerBean()
                .setKrbMaximumRenewableLifetime( Long.parseLong( maximumRenewableLifetimeText.getText() ) );
        }
    };
    private ModifyListener maximumTicketLifetimeTextListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            getKdcServerBean().setKrbMaximumTicketLifetime( Long.parseLong( maximumTicketLifetimeText.getText() ) );
        }
    };
    private ModifyListener allowableClockSkewTextListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            getKdcServerBean().setKrbAllowableClockSkew( Long.parseLong( allowableClockSkewText.getText() ) );
        }
    };


    // ── MOFF JERJERROD OPENS THE ENGINEERING CONSOLE ─────────────────────────────────────────────
    // Jerjerrod powers up the security engineering console and registers it with the
    // Death Star's central command system (the editor), ready to receive configuration orders.
    // We hand the editor reference and our page ID/title to the superclass.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new Kerberos server configuration page and associates it with the
     * given editor. The page registers itself under its unique ID and title so Eclipse
     * can add it as a tab in the multi-page editor.
     *
     * <p>For example — Jerjerrod opens the engineering console:</p>
     * <pre>
     *   KerberosServerPage page = new KerberosServerPage(editor);
     *   editor.addPage(page);
     * </pre>
     *
     * @param editor  The parent {@link ServerConfigurationEditor}.
     */
    public KerberosServerPage( ServerConfigurationEditor editor )
    {
        super( editor, ID, TITLE );
    }


    // ── LAYING OUT THE ENGINEERING CONSOLE PANELS ────────────────────────────────────────────────
    // The Imperial engineers arrange the three main console panels in the engineering bay:
    // the KDC server controls on the left, the encryption/realm settings below those,
    // and the ticket-policy switches in the right column.
    // createFormContent assembles the two-column layout and delegates to the section builders.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the two-column form layout for this page and delegates the creation
     * of each section to the private helper methods. The left column holds the
     * KDC server controls and the Kerberos settings; the right column holds the
     * ticket-policy section.
     *
     * <p>For example — the engineering bay panels are arranged:</p>
     * <pre>
     *   // left column: Kerberos Server section + Kerberos Settings section
     *   // right column: Ticket Settings section
     * </pre>
     *
     * @param parent   The parent composite provided by the Eclipse forms framework.
     * @param toolkit  The form toolkit used to create styled SWT widgets.
     */
    protected void createFormContent( Composite parent, FormToolkit toolkit )
    {
        TableWrapLayout twl = new TableWrapLayout();
        twl.numColumns = 2;
        parent.setLayout( twl );

        // Left Composite
        Composite leftComposite = toolkit.createComposite( parent );
        leftComposite.setLayout( new GridLayout() );
        TableWrapData leftCompositeTableWrapData = new TableWrapData( TableWrapData.FILL, TableWrapData.TOP );
        leftCompositeTableWrapData.grabHorizontal = true;
        leftComposite.setLayoutData( leftCompositeTableWrapData );

        // Right Composite
        Composite rightComposite = toolkit.createComposite( parent );
        rightComposite.setLayout( new GridLayout() );
        TableWrapData rightCompositeTableWrapData = new TableWrapData( TableWrapData.FILL, TableWrapData.TOP );
        rightCompositeTableWrapData.grabHorizontal = true;
        rightComposite.setLayoutData( rightCompositeTableWrapData );

        // Creating the sections
        createKerberosServerSection( toolkit, leftComposite );
        createKerberosSettingsSection( toolkit, leftComposite );
        createTicketSettingsSection( toolkit, rightComposite );

        // Refreshing the UI
        refreshUI();
    }


    // ── WIRING UP THE KDC AND CHANGE-PASSWORD TRANSPORT CONTROLS ─────────────────────────────────
    // The engineers install the control switches for the two listening posts:
    // the main KDC checkpoint (port 60088) and the Change Password sub-station (port 60464).
    // Each post has an enable toggle, a port dial, and an address selector.
    // createKerberosServerSection builds those controls.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the "Kerberos Server" section containing the enable/disable checkbox
     * for the KDC server (port 60088) and the Change Password server (port 60464),
     * plus address and port text fields for each.
     *
     * <p>For example — two listening-post control panels go up:</p>
     * <pre>
     *   // [X] Enable Kerberos Server
     *   //     Port: [60088]  Address: [0.0.0.0]
     *   // [X] Enable Change Password Server
     *   //     Port: [60464]  Address: [0.0.0.0]
     * </pre>
     *
     * @param toolkit  The form toolkit for creating styled widgets.
     * @param parent   The parent composite (left column of the page layout).
     */
    private void createKerberosServerSection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section
        Section section = toolkit.createSection( parent, Section.TITLE_BAR );
        section.setText( Messages.getString( "KerberosServerPage.KerberosServer" ) ); //$NON-NLS-1$
        section.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite composite = toolkit.createComposite( section );
        toolkit.paintBordersFor( composite );
        GridLayout gridLayout = new GridLayout( 4, false );
        gridLayout.marginHeight = gridLayout.marginWidth = 0;
        composite.setLayout( gridLayout );
        section.setClient( composite );

        // Enable Kerberos Server Checkbox
        enableKerberosCheckbox = toolkit.createButton( composite,
            Messages.getString( "KerberosServerPage.EnableKerberosServer" ), SWT.CHECK ); //$NON-NLS-1$
        enableKerberosCheckbox
            .setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, gridLayout.numColumns, 1 ) );

        // Kerberos Server Port Text
        toolkit.createLabel( composite, TABULATION );
        toolkit.createLabel( composite, Messages.getString( "KerberosServerPage.Port" ) ); //$NON-NLS-1$
        kerberosPortText = createPortText( toolkit, composite );
        createDefaultValueLabel( toolkit, composite, "60088" ); //$NON-NLS-1$

        // Kerberos Server Address Text
        toolkit.createLabel( composite, TABULATION );
        toolkit.createLabel( composite, Messages.getString( "KerberosServerPage.Address" ) ); //$NON-NLS-1$
        kerberosAddressText = createAddressText( toolkit, composite );
        createDefaultValueLabel( toolkit, composite, DEFAULT_ADDRESS ); //$NON-NLS-1$

        // Enable Change Password Server Checkbox
        enableChangePasswordCheckbox = toolkit.createButton( composite,
            Messages.getString( "KerberosServerPage.EnableKerberosChangePassword" ), //$NON-NLS-1$
            SWT.CHECK );
        enableChangePasswordCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false,
            gridLayout.numColumns, 1 ) );

        // Change Password Server Port Text
        toolkit.createLabel( composite, TABULATION );
        toolkit.createLabel( composite, Messages.getString( "KerberosServerPage.Port" ) ); //$NON-NLS-1$
        changePasswordPortText = createPortText( toolkit, composite );
        createDefaultValueLabel( toolkit, composite, "60464" ); //$NON-NLS-1$

        // Change Password Server Address Text
        toolkit.createLabel( composite, TABULATION );
        toolkit.createLabel( composite, Messages.getString( "KerberosServerPage.Address" ) ); //$NON-NLS-1$
        changePasswordAddressText = createAddressText( toolkit, composite );
        createDefaultValueLabel( toolkit, composite, DEFAULT_ADDRESS ); //$NON-NLS-1$
    }


    // ── CONFIGURING THE REALM AND ENCRYPTION ALGORITHMS ──────────────────────────────────────────
    // The engineers program the KDC's realm name (like an Imperial district code —
    // "EXAMPLE.COM") and which encryption ciphers are permitted for ticket signing.
    // They also set the search base DN: the subtree of the LDAP directory where
    // the KDC looks up user principals.
    // createKerberosSettingsSection builds those controls.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the "Kerberos Settings" section containing the primary KDC realm
     * name, the LDAP search base DN for principal lookups, and the multi-select
     * table of supported encryption types (AES-128, AES-256, DES3, RC4, etc.).
     *
     * <p>For example — the realm and cipher controls are wired in:</p>
     * <pre>
     *   // Primary KDC realm:  [EXAMPLE.COM]
     *   // Search base DN:     [ou=users,dc=example,dc=com]
     *   // Encryption types:   [X] AES-256  [X] AES-128  [ ] DES  ...
     * </pre>
     *
     * @param toolkit  The form toolkit for creating styled widgets.
     * @param parent   The parent composite (left column of the page layout).
     */
    private void createKerberosSettingsSection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section
        Section section = toolkit.createSection( parent, Section.TITLE_BAR );
        section.setText( Messages.getString( "KerberosServerPage.KerberosSettings" ) ); //$NON-NLS-1$
        section.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite composite = toolkit.createComposite( section );
        toolkit.paintBordersFor( composite );
        GridLayout glayout = new GridLayout( 2, false );
        composite.setLayout( glayout );
        section.setClient( composite );

        // SASL Principal Text
        toolkit.createLabel( composite, Messages.getString( "KerberosServerPage.PrimaryKdcRealm" ) ); //$NON-NLS-1$
        primaryKdcRealmText = toolkit.createText( composite, "" ); //$NON-NLS-1$
        setGridDataWithDefaultWidth( primaryKdcRealmText, new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Label defaultSaslPrincipalLabel = createDefaultValueLabel( toolkit, composite, "EXAMPLE.COM" ); //$NON-NLS-1$
        defaultSaslPrincipalLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

        // Search Base Dn Text
        toolkit.createLabel( composite, Messages.getString( "KerberosServerPage.SearchBaseDn" ) ); //$NON-NLS-1$
        kdcSearchBaseDnText = toolkit.createText( composite, "" ); //$NON-NLS-1$
        setGridDataWithDefaultWidth( kdcSearchBaseDnText, new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Label defaultSaslSearchBaseDnLabel = createDefaultValueLabel( toolkit, composite, "ou=users,dc=example,dc=com" ); //$NON-NLS-1$
        defaultSaslSearchBaseDnLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

        // Encryption Types Table Viewer
        Label encryptionTypesLabel = toolkit.createLabel( composite,
            Messages.getString( "KerberosServerPage.EncryptionTypes" ) ); //$NON-NLS-1$
        encryptionTypesLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.TOP, false, false ) );
        encryptionTypesTableViewer = new CheckboxTableViewer( new Table( composite, SWT.BORDER | SWT.CHECK ) );
        encryptionTypesTableViewer.setContentProvider( new ArrayContentProvider() );
        encryptionTypesTableViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                if ( element instanceof EncryptionType )
                {
                    EncryptionType encryptionType = ( EncryptionType ) element;

                    return encryptionType.getName().toUpperCase();
                }

                return super.getText( element );
            }
        } );
        encryptionTypesTableViewer.setInput( SUPPORTED_ENCRYPTION_TYPES );
        GridData encryptionTypesTableViewerGridData = new GridData( SWT.FILL, SWT.NONE, true, false );
        encryptionTypesTableViewerGridData.heightHint = 60;
        encryptionTypesTableViewer.getControl().setLayoutData( encryptionTypesTableViewerGridData );
    }


    // ── SETTING THE TICKET POLICY DIALS ───────────────────────────────────────────────────────────
    // The engineers fine-tune the Imperial access-credential policy: can tickets be
    // renewed? Can they be delegated (proxiable)? How long do they last?
    // Is a timestamp required to prevent replay attacks?  Each dial covers one ticket rule.
    // createTicketSettingsSection builds all those policy checkboxes and lifetime fields.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the "Ticket Settings" section on the right column, containing all
     * ticket-policy flags (renewable, forwardable, proxiable, postdated,
     * empty-addresses, pre-auth, body-checksum) and the numeric fields for
     * maximum ticket lifetime, maximum renewable lifetime, and allowable clock skew.
     *
     * <p>For example — the ticket-policy panel is wired in:</p>
     * <pre>
     *   // [X] Verify Body Checksum
     *   // [X] Allow Empty Addresses
     *   // [X] Allow Forwardable Addresses
     *   // ...
     *   // Max Renewable Lifetime: [604800000]
     *   // Max Ticket Lifetime:    [86400000]
     *   // Allowable Clock Skew:   [300000]
     * </pre>
     *
     * @param toolkit  The form toolkit for creating styled widgets.
     * @param parent   The parent composite (right column of the page layout).
     */
    private void createTicketSettingsSection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section
        Section section = toolkit.createSection( parent, Section.TITLE_BAR );
        section.setText( Messages.getString( "KerberosServerPage.TicketSettings" ) ); //$NON-NLS-1$
        section.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite composite = toolkit.createComposite( section );
        toolkit.paintBordersFor( composite );
        GridLayout layout = new GridLayout( 2, false );
        composite.setLayout( layout );
        section.setClient( composite );

        // Verify Body Checksum Checkbox
        verifyBodyChecksumCheckbox = toolkit.createButton( composite,
            Messages.getString( "KerberosServerPage.VerifyBodyChecksum" ), SWT.CHECK ); //$NON-NLS-1$
        verifyBodyChecksumCheckbox
            .setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, layout.numColumns, 1 ) );

        // Allow Empty Addresse Checkbox
        allowEmptyAddressesCheckbox = toolkit.createButton( composite,
            Messages.getString( "KerberosServerPage.AllowEmptyAddresses" ), SWT.CHECK ); //$NON-NLS-1$
        allowEmptyAddressesCheckbox
            .setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, layout.numColumns, 1 ) );

        // Allow Forwardable Addresses Checkbox
        allowForwardableAddressesCheckbox = toolkit.createButton( composite,
            Messages.getString( "KerberosServerPage.AllowForwadableAddresses" ), //$NON-NLS-1$
            SWT.CHECK );
        allowForwardableAddressesCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false,
            layout.numColumns, 1 ) );

        // Require Pre-Authentication By Encrypted Timestamp Checkbox
        requirePreAuthByEncryptedTimestampCheckbox = toolkit.createButton( composite,
            Messages.getString( "KerberosServerPage.RequirePreAuthentication" ), SWT.CHECK ); //$NON-NLS-1$
        requirePreAuthByEncryptedTimestampCheckbox
            .setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, layout.numColumns, 1 ) );

        // Allow Postdated Tickets Checkbox
        allowPostdatedTicketsCheckbox = toolkit.createButton( composite,
            Messages.getString( "KerberosServerPage.AllowPostdatedTickets" ), SWT.CHECK ); //$NON-NLS-1$
        allowPostdatedTicketsCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, layout.numColumns,
            1 ) );

        // Allow Renewable Tickets Checkbox
        allowRenewableTicketsCheckbox = toolkit.createButton( composite,
            Messages.getString( "KerberosServerPage.AllowRenewableTickets" ), SWT.CHECK ); //$NON-NLS-1$
        allowRenewableTicketsCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, layout.numColumns,
            1 ) );

        // Allow Proxiable Tickets Checkbox
        allowProxiableTicketsCheckbox = toolkit.createButton( composite,
            Messages.getString( "KerberosServerPage.AllowProxiableTickets" ), SWT.CHECK ); //$NON-NLS-1$
        allowProxiableTicketsCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, layout.numColumns,
            1 ) );

        // Max Renewable Lifetime Text
        toolkit.createLabel( composite, Messages.getString( "KerberosServerPage.MaxRenewableLifetime" ) ); //$NON-NLS-1$
        maximumRenewableLifetimeText = BaseWidgetUtils.createIntegerText( toolkit, composite );
        setGridDataWithDefaultWidth( maximumRenewableLifetimeText, new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Max Ticket Lifetime Text
        toolkit.createLabel( composite, Messages.getString( "KerberosServerPage.MaxTicketLifetime" ) ); //$NON-NLS-1$
        maximumTicketLifetimeText = BaseWidgetUtils.createIntegerText( toolkit, composite );
        setGridDataWithDefaultWidth( maximumTicketLifetimeText, new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Allowable Clock Skew Text
        toolkit.createLabel( composite, Messages.getString( "KerberosServerPage.AllowableClockSkew" ) ); //$NON-NLS-1$
        allowableClockSkewText = BaseWidgetUtils.createIntegerText( toolkit, composite );
        setGridDataWithDefaultWidth( allowableClockSkewText, new GridData( SWT.FILL, SWT.NONE, true, false ) );
    }


    // ── READING THE CURRENT FORTRESS SETTINGS INTO THE CONSOLE ───────────────────────────────────
    // Jerjerrod's console technician reads all current settings from the data store and
    // dials them into the control panel: KDC enabled? Port 60088? Realm EXAMPLE.COM?
    // refreshUI pulls every field from the KdcServerBean and updates the widgets.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes all widget values on this page from the underlying
     * {@link KdcServerBean} and {@link ChangePasswordServerBean}. We detach
     * listeners first to suppress dirty notifications during the programmatic update,
     * then re-attach them afterwards.
     *
     * <p>For example — the console technician reads in current settings:</p>
     * <pre>
     *   refreshUI();
     *   // every checkbox, text field, and table viewer now reflects
     *   // what is stored in the configuration model
     * </pre>
     */
    protected void refreshUI()
    {
        if ( isInitialized() )
        {
            removeListeners();

            // Kerberos Server
            KdcServerBean kdcServerBean = getKdcServerBean();
            setSelection( enableKerberosCheckbox, kdcServerBean.isEnabled() );
            setEnabled( kerberosPortText, enableKerberosCheckbox.getSelection() );
            setEnabled( kerberosAddressText, enableKerberosCheckbox.getSelection() );
            setText( kerberosPortText, Integer.toString( kdcServerBean.getTransports()[0].getSystemPort() ) );
            setText( kerberosAddressText, kdcServerBean.getTransports()[0].getTransportAddress() );

            // Change Password Checkbox
            ChangePasswordServerBean changePasswordServerBean = getChangePasswordServerBean();
            setSelection( enableChangePasswordCheckbox, changePasswordServerBean.isEnabled() );
            setEnabled( changePasswordPortText, enableChangePasswordCheckbox.getSelection() );
            setEnabled( changePasswordAddressText, enableChangePasswordCheckbox.getSelection() );
            setText( changePasswordPortText, Integer.toString( changePasswordServerBean.getTransports()[0].getSystemPort() ) );
            setText( changePasswordAddressText, changePasswordServerBean.getTransports()[0].getTransportAddress() );

            // Kerberos Settings
            setText( primaryKdcRealmText, kdcServerBean.getKrbPrimaryRealm() );
            setText( kdcSearchBaseDnText, kdcServerBean.getSearchBaseDn().toString() );

            // Encryption Types
            List<String> encryptionTypesNames = kdcServerBean.getKrbEncryptionTypes();
            List<EncryptionType> encryptionTypes = new ArrayList<EncryptionType>();
            for ( String encryptionTypesName : encryptionTypesNames )
            {
                EncryptionType encryptionType = EncryptionType.getByName( encryptionTypesName );

                if ( !EncryptionType.UNKNOWN.equals( encryptionType ) )
                {
                    encryptionTypes.add( encryptionType );
                }
            }
            encryptionTypesTableViewer.setCheckedElements( encryptionTypes.toArray() );

            // Ticket Settings
            setSelection( verifyBodyChecksumCheckbox, kdcServerBean.isKrbBodyChecksumVerified() );
            setSelection( allowEmptyAddressesCheckbox, kdcServerBean.isKrbEmptyAddressesAllowed() );
            setSelection( allowForwardableAddressesCheckbox, kdcServerBean.isKrbForwardableAllowed() );
            setSelection( requirePreAuthByEncryptedTimestampCheckbox, kdcServerBean.isKrbPaEncTimestampRequired() );
            setSelection( allowPostdatedTicketsCheckbox, kdcServerBean.isKrbPostdatedAllowed() );
            setSelection( allowRenewableTicketsCheckbox, kdcServerBean.isKrbRenewableAllowed() );
            setSelection( allowProxiableTicketsCheckbox, kdcServerBean.isKrbProxiableAllowed() );
            setText( maximumRenewableLifetimeText, Long.toString( kdcServerBean.getKrbMaximumRenewableLifetime() ) );
            setText( maximumTicketLifetimeText, Long.toString( kdcServerBean.getKrbMaximumTicketLifetime() ) );
            setText( allowableClockSkewText, Long.toString( kdcServerBean.getKrbAllowableClockSkew() ) );

            addListeners();
        }
    }


    // ── ARMING ALL THE CONSOLE SENSORS ────────────────────────────────────────────────────────────
    // The engineering crew arms every sensor on the console so any dial adjustment
    // immediately signals "fortress modified" back to Jerjerrod's command display.
    // addListeners wires every widget's dirty-and-model-update listener in one pass.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches all listeners — dirty-notification and model-update — to every
     * interactive widget on this page. Called after a refresh so user changes
     * are picked up and reflected in both the dirty flag and the underlying bean.
     *
     * <p>For example — the console sensors go live:</p>
     * <pre>
     *   addListeners();
     *   // user changes any field -> editor is marked dirty
     *   //                        -> corresponding bean setter is called
     * </pre>
     */
    private void addListeners()
    {
        // Enable Kerberos Server Checkbox
        addDirtyListener( enableKerberosCheckbox );
        addSelectionListener( enableKerberosCheckbox, enableKerberosCheckboxListener );

        // Kerberos Server Port Text
        addDirtyListener( kerberosPortText );
        addModifyListener( kerberosPortText, kerberosPortTextListener );

        // Kerberos Server Address Text
        addDirtyListener( kerberosAddressText );
        addModifyListener( kerberosAddressText, kerberosAddressTextListener );

        // Enable Change Password Server Checkbox
        addDirtyListener( enableChangePasswordCheckbox );
        addSelectionListener( enableChangePasswordCheckbox, enableChangePasswordCheckboxListener );

        // Change Password Server Port Text
        addDirtyListener( changePasswordPortText );
        addModifyListener( changePasswordPortText, changePasswordPortTextListener );

        // Change Password Server Address Text
        addDirtyListener( changePasswordAddressText );
        addModifyListener( changePasswordAddressText, changePasswordAddressTextListener );

        // Primary KDC Text
        addDirtyListener( primaryKdcRealmText );
        addModifyListener( primaryKdcRealmText, primaryKdcRealmTextListener );

        // KDC Search Base Dn Text
        addDirtyListener( kdcSearchBaseDnText );
        addModifyListener( kdcSearchBaseDnText, kdcSearchBaseDnTextListener );

        // Encryption Types Table Viewer
        encryptionTypesTableViewer.addCheckStateListener( encryptionTypesTableViewerListener );

        // Verify Body Checksum Checkbox
        addDirtyListener( verifyBodyChecksumCheckbox );
        addSelectionListener( verifyBodyChecksumCheckbox, verifyBodyChecksumCheckboxListener );

        // Allow Empty Addresses Checkbox
        addDirtyListener( allowEmptyAddressesCheckbox );
        addSelectionListener( allowEmptyAddressesCheckbox, allowEmptyAddressesCheckboxListener );

        // Allow Forwardable Addresses Checkbox
        addDirtyListener( allowForwardableAddressesCheckbox );
        addSelectionListener( allowForwardableAddressesCheckbox, allowForwardableAddressesCheckboxListener );

        // Require Pre-Authentication By Encrypted Timestamp Checkbox
        addDirtyListener( requirePreAuthByEncryptedTimestampCheckbox );
        addSelectionListener( requirePreAuthByEncryptedTimestampCheckbox,
            requirePreAuthByEncryptedTimestampCheckboxListener );

        // Allow Postdated Tickets Checkbox
        addDirtyListener( allowPostdatedTicketsCheckbox );
        addSelectionListener( allowPostdatedTicketsCheckbox, allowPostdatedTicketsCheckboxListener );

        // Allow Renewable Tickets Checkbox
        addDirtyListener( allowRenewableTicketsCheckbox );
        addSelectionListener( allowRenewableTicketsCheckbox, allowRenewableTicketsCheckboxListener );

        // Allow Proxiable Tickets Checkbox
        addDirtyListener( allowProxiableTicketsCheckbox );
        addSelectionListener( allowProxiableTicketsCheckbox, allowProxiableTicketsCheckboxListener );

        // Maximum Renewable Lifetime Text
        addDirtyListener( maximumRenewableLifetimeText );
        addModifyListener( maximumRenewableLifetimeText, maximumRenewableLifetimeTextListener );

        // Maximum Ticket Lifetime Text
        addDirtyListener( maximumTicketLifetimeText );
        addModifyListener( maximumTicketLifetimeText, maximumTicketLifetimeTextListener );

        // Allowable Clock Skew Text
        addDirtyListener( allowableClockSkewText );
        addModifyListener( allowableClockSkewText, allowableClockSkewTextListener );
    }


    // ── STANDING DOWN THE CONSOLE SENSORS ─────────────────────────────────────────────────────────
    // Before the crew refreshes the console with new data from the data store,
    // they disarm all the sensors so the programmatic updates don't trigger false alarms.
    // removeListeners detaches every listener in one pass.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Detaches all listeners from every interactive widget on this page.
     * Called before a programmatic refresh so we don't accidentally mark the
     * editor dirty when we are just populating the widgets from the model.
     *
     * <p>For example — the crew stands down the sensors before a console refresh:</p>
     * <pre>
     *   removeListeners();
     *   // programmatic widget updates happen without triggering dirty flag
     *   addListeners();
     * </pre>
     */
    private void removeListeners()
    {
        // Enable Kerberos Server Checkbox
        removeDirtyListener( enableKerberosCheckbox );
        removeSelectionListener( enableKerberosCheckbox, enableKerberosCheckboxListener );

        // Kerberos Server Port Text
        removeDirtyListener( kerberosPortText );
        removeModifyListener( kerberosPortText, kerberosPortTextListener );

        // Kerberos Server Address Text
        removeDirtyListener( kerberosAddressText );
        removeModifyListener( kerberosAddressText, kerberosAddressTextListener );

        // Enable Change Password Server Checkbox
        removeDirtyListener( enableChangePasswordCheckbox );
        removeSelectionListener( enableChangePasswordCheckbox, enableChangePasswordCheckboxListener );

        // Change Password Server Port Text
        removeDirtyListener( changePasswordPortText );
        removeModifyListener( changePasswordPortText, changePasswordPortTextListener );

        // Change Password Server Address Text
        removeDirtyListener( changePasswordAddressText );
        removeModifyListener( changePasswordAddressText, changePasswordAddressTextListener );

        // Primary KDC Text
        removeDirtyListener( primaryKdcRealmText );
        removeModifyListener( primaryKdcRealmText, primaryKdcRealmTextListener );

        // KDC Search Base Dn Text
        removeDirtyListener( kdcSearchBaseDnText );
        removeModifyListener( kdcSearchBaseDnText, kdcSearchBaseDnTextListener );

        // Encryption Types Table Viewer
        encryptionTypesTableViewer.removeCheckStateListener( encryptionTypesTableViewerListener );

        // Verify Body Checksum Checkbox
        removeDirtyListener( verifyBodyChecksumCheckbox );
        removeSelectionListener( verifyBodyChecksumCheckbox, verifyBodyChecksumCheckboxListener );

        // Allow Empty Addresses Checkbox
        removeDirtyListener( allowEmptyAddressesCheckbox );
        removeSelectionListener( allowEmptyAddressesCheckbox, allowEmptyAddressesCheckboxListener );

        // Allow Forwardable Addresses Checkbox
        removeDirtyListener( allowForwardableAddressesCheckbox );
        removeSelectionListener( allowForwardableAddressesCheckbox, allowForwardableAddressesCheckboxListener );

        // Require Pre-Authentication By Encrypted Timestamp Checkbox
        removeDirtyListener( requirePreAuthByEncryptedTimestampCheckbox );
        removeSelectionListener( requirePreAuthByEncryptedTimestampCheckbox,
            requirePreAuthByEncryptedTimestampCheckboxListener );

        // Allow Postdated Tickets Checkbox
        removeDirtyListener( allowPostdatedTicketsCheckbox );
        removeSelectionListener( allowPostdatedTicketsCheckbox, allowPostdatedTicketsCheckboxListener );

        // Allow Renewable Tickets Checkbox
        removeDirtyListener( allowRenewableTicketsCheckbox );
        removeSelectionListener( allowRenewableTicketsCheckbox, allowRenewableTicketsCheckboxListener );

        // Allow Proxiable Tickets Checkbox
        removeDirtyListener( allowProxiableTicketsCheckbox );
        removeSelectionListener( allowProxiableTicketsCheckbox, allowProxiableTicketsCheckboxListener );

        // Maximum Renewable Lifetime Text
        removeDirtyListener( maximumRenewableLifetimeText );
        removeModifyListener( maximumRenewableLifetimeText, maximumRenewableLifetimeTextListener );

        // Maximum Ticket Lifetime Text
        removeDirtyListener( maximumTicketLifetimeText );
        removeModifyListener( maximumTicketLifetimeText, maximumTicketLifetimeTextListener );

        // Allowable Clock Skew Text
        removeDirtyListener( allowableClockSkewText );
        removeModifyListener( allowableClockSkewText, allowableClockSkewTextListener );
    }


    // ── LOCATING THE KDC COMMAND MODULE ──────────────────────────────────────────────────────────
    // A console shortcut: Jerjerrod asks for the KDC module without specifying which
    // fortress — we know it's our own, so we fetch the directory service bean implicitly.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link KdcServerBean} from the current directory service bean.
     * Convenience wrapper around the static version; uses the page's own
     * {@code getDirectoryServiceBean()} to locate the directory service.
     *
     * <p>For example — Jerjerrod checks his own fortress's KDC module:</p>
     * <pre>
     *   KdcServerBean kdc = getKdcServerBean();
     *   kdc.setKrbPrimaryRealm("EXAMPLE.COM");
     * </pre>
     *
     * @return  The KDC server bean, created on demand if absent.
     */
    private KdcServerBean getKdcServerBean()
    {
        return getKdcServerBean( getDirectoryServiceBean() );
    }


    // ── LOCATING THE KDC MODULE IN ANY FORTRESS ───────────────────────────────────────────────────
    // Given any fortress's service record (DirectoryServiceBean), find — or create — its KDC module.
    // This static version is called both by the page and by external save/load code.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link KdcServerBean} from the given {@link DirectoryServiceBean},
     * creating a new one and registering it if none exists yet. This is a static
     * utility so the save job and other non-page code can call it without a page reference.
     *
     * <p>For example — locating the KDC module in any fortress:</p>
     * <pre>
     *   KdcServerBean kdc = KerberosServerPage.getKdcServerBean(directoryService);
     *   // kdc is guaranteed non-null
     * </pre>
     *
     * @param directoryServiceBean  The directory service configuration to search.
     * @return                      The existing or newly-created KDC server bean.
     */
    public static KdcServerBean getKdcServerBean( DirectoryServiceBean directoryServiceBean )
    {
        KdcServerBean kdcServerBean = directoryServiceBean.getKdcServerBean();

        if ( kdcServerBean == null )
        {
            kdcServerBean = new KdcServerBean();
            directoryServiceBean.addServers( kdcServerBean );
        }

        return kdcServerBean;
    }


    // ── ENABLING OR DISABLING THE AUTHENTICATION FORTRESS ────────────────────────────────────────
    // Jerjerrod throws the master switch for the security checkpoint system:
    // the KDC and its associated Key Derivation Interceptor (which derives Kerberos
    // session keys from LDAP password entries) are both enabled or disabled together.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the Kerberos KDC server together with its companion
     * Key Derivation Interceptor. Kerberos requires the interceptor to derive
     * session keys from stored passwords — disabling one without the other
     * leaves the system in an inconsistent state.
     *
     * <p>For example — throwing the master security switch:</p>
     * <pre>
     *   KerberosServerPage.enableKerberosServer(directoryService, true);
     *   // kdcServerBean.isEnabled() == true
     *   // keyDerivationInterceptor.isEnabled() == true
     * </pre>
     *
     * @param directoryServiceBean   The directory service to configure.
     * @param enableKerberosServer   {@code true} to enable, {@code false} to disable.
     */
    public static void enableKerberosServer( DirectoryServiceBean directoryServiceBean, boolean enableKerberosServer )
    {
        // Enabling the KDC Server
        getKdcServerBean( directoryServiceBean ).setEnabled( enableKerberosServer );

        // Getting the Key Derivation Interceptor
        InterceptorBean keyDerivationInterceptor = getKeyDerivationInterceptor( directoryServiceBean );

        if ( keyDerivationInterceptor != null )
        {
            // Enabling the Key Derivation Interceptor
            keyDerivationInterceptor.setEnabled( enableKerberosServer );
        }
    }


    // ── LOCATING THE CHANGE-PASSWORD SUB-STATION ──────────────────────────────────────────────────
    // Convenience shortcut: Jerjerrod checks the Change Password sub-station in our own fortress.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ChangePasswordServerBean} from the current directory service.
     * Convenience wrapper around the static version.
     *
     * <p>For example — Jerjerrod checks the Change Password sub-station:</p>
     * <pre>
     *   ChangePasswordServerBean cpServer = getChangePasswordServerBean();
     * </pre>
     *
     * @return  The Change Password server bean, created on demand if absent.
     */
    private ChangePasswordServerBean getChangePasswordServerBean()
    {
        return getChangePasswordServerBean( getDirectoryServiceBean() );
    }


    // ── LOCATING THE CHANGE-PASSWORD SUB-STATION IN ANY FORTRESS ─────────────────────────────────
    // Given any fortress record, find — or create — its Change Password sub-station.
    // This static version is reusable by save/load code outside the page.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ChangePasswordServerBean} from the given
     * {@link DirectoryServiceBean}, creating one on demand if none exists.
     * The Change Password server listens on port 464 and lets Kerberos principals
     * change their passwords without an LDAP bind.
     *
     * <p>For example — locating the sub-station in any fortress:</p>
     * <pre>
     *   ChangePasswordServerBean cpServer =
     *       KerberosServerPage.getChangePasswordServerBean(directoryService);
     * </pre>
     *
     * @param directoryServiceBean  The directory service configuration to search.
     * @return                      The existing or newly-created Change Password server bean.
     */
    public static ChangePasswordServerBean getChangePasswordServerBean( DirectoryServiceBean directoryServiceBean )
    {
        ChangePasswordServerBean changePasswordServerBean = directoryServiceBean.getChangePasswordServerBean();

        if ( changePasswordServerBean == null )
        {
            changePasswordServerBean = new ChangePasswordServerBean();
            directoryServiceBean.addServers( changePasswordServerBean );
        }

        return changePasswordServerBean;
    }


    // ── FINDING THE KEY DERIVATION UNIT IN THE INTERCEPTOR CHAIN ─────────────────────────────────
    // Deep in the fortress's security pipeline, there is one interceptor responsible for
    // deriving Kerberos session keys from stored password entries.
    // We scan the interceptor chain to find it by class name.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Searches the interceptor chain of the given directory service for the
     * {@code KeyDerivationInterceptor}. This interceptor is responsible for
     * computing Kerberos-compatible password hashes when a password is set in
     * the directory. We need its reference so we can enable/disable it in sync
     * with the KDC server.
     *
     * <p>For example — scanning the pipeline for the key-derivation unit:</p>
     * <pre>
     *   InterceptorBean kdi = getKeyDerivationInterceptor(directoryService);
     *   if (kdi != null) { kdi.setEnabled(false); }
     * </pre>
     *
     * @param directoryServiceBean  The directory service whose interceptors we search.
     * @return                      The interceptor bean, or {@code null} if not found.
     */
    private static InterceptorBean getKeyDerivationInterceptor( DirectoryServiceBean directoryServiceBean )
    {
        if ( directoryServiceBean != null )
        {
            List<InterceptorBean> interceptors = directoryServiceBean.getInterceptors();

            for ( InterceptorBean interceptor : interceptors )
            {
                if ( "org.apache.directory.server.core.kerberos.KeyDerivationInterceptor".equalsIgnoreCase( interceptor
                    .getInterceptorClassName() ) )
                {
                    return interceptor;
                }
            }
        }

        return null;
    }


    // ── DIALLING IN THE KDC LISTENING PORT ───────────────────────────────────────────────────────
    // The engineer turns the port dial on the KDC listening post: "60088 UDP/TCP."
    // We parse the text, validate it's an integer, and apply it to all KDC transport beans.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the given port string and applies it to every transport configured
     * on the KDC server. Invalid (non-integer) values are logged to stdout and
     * silently ignored — the port remains unchanged.
     *
     * <p>For example — dialling in the port:</p>
     * <pre>
     *   KerberosServerPage.setKerberosPort(directoryService, "60088");
     *   // all KDC transports now report systemPort == 60088
     * </pre>
     *
     * @param directoryServiceBean  The directory service whose KDC port we are updating.
     * @param portAsText            The new port number as a string (e.g., {@code "60088"}).
     */
    public static void setKerberosPort( DirectoryServiceBean directoryServiceBean, String portAsText )
    {
        try
        {
            int port = Integer.parseInt( portAsText );
            KdcServerBean kdcServerBean = directoryServiceBean.getKdcServerBean();
            for ( TransportBean transportBean : kdcServerBean.getTransports() )
            {
                transportBean.setSystemPort( port );
            }
        }
        catch ( NumberFormatException nfe )
        {
            System.out.println( "Wrong Kerberos TCP/UDP Port : it must be an integer" );
        }
    }


    // ── UPDATING THE KDC LISTENING ADDRESS ────────────────────────────────────────────────────────
    // The engineer updates the network address the KDC checkpoint listens on.
    // We apply the new address to every transport bean registered with the KDC.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the network address for every transport configured on the KDC server.
     * Typically this is {@code "0.0.0.0"} (all interfaces) or a specific IP address.
     *
     * <p>For example — updating the listening address:</p>
     * <pre>
     *   setKerberosAddress(directoryService, "192.168.1.10");
     *   // all KDC transports now bind to 192.168.1.10
     * </pre>
     *
     * @param directoryServiceBean  The directory service whose KDC address we are updating.
     * @param address               The new bind address string.
     */
    private void setKerberosAddress( DirectoryServiceBean directoryServiceBean, String address )
    {
        KdcServerBean kdcServerBean = directoryServiceBean.getKdcServerBean();
        for ( TransportBean transportBean : kdcServerBean.getTransports() )
        {
            transportBean.setTransportAddress( address );
        }
    }


    // ── DIALLING IN THE CHANGE-PASSWORD LISTENING PORT ────────────────────────────────────────────
    // The engineer turns the port dial on the Change Password sub-station: "60464."
    // We parse and apply it to all Change Password transport beans.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the given port string and applies it to every transport configured
     * on the Change Password server. Invalid values are logged and ignored.
     *
     * <p>For example — dialling in the Change Password port:</p>
     * <pre>
     *   KerberosServerPage.setChangePasswordPort(directoryService, "60464");
     *   // all Change Password transports now report systemPort == 60464
     * </pre>
     *
     * @param directoryServiceBean  The directory service whose Change Password port we update.
     * @param portAsText            The new port number as a string (e.g., {@code "60464"}).
     */
    public static void setChangePasswordPort( DirectoryServiceBean directoryServiceBean, String portAsText )
    {
        try
        {
            int port = Integer.parseInt( portAsText );
            ChangePasswordServerBean changePasswordServerBean = directoryServiceBean.getChangePasswordServerBean();
            for ( TransportBean transportBean : changePasswordServerBean.getTransports() )
            {
                transportBean.setSystemPort( port );
            }
        }
        catch ( NumberFormatException nfe )
        {
            System.out.println( "Wrong ChangePassword TCP/UDP Port : it must be an integer" );
        }
    }


    // ── UPDATING THE CHANGE-PASSWORD LISTENING ADDRESS ────────────────────────────────────────────
    // The engineer updates the network address the Change Password sub-station binds to.
    // We apply the new address to every Change Password transport bean.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the network address for every transport configured on the Change Password
     * server. Same semantics as {@link #setKerberosAddress} but targets the Change
     * Password service.
     *
     * <p>For example — updating the sub-station's listening address:</p>
     * <pre>
     *   setChangePasswordAddress(directoryService, "0.0.0.0");
     * </pre>
     *
     * @param directoryServiceBean  The directory service whose Change Password address we update.
     * @param address               The new bind address string.
     */
    private void setChangePasswordAddress( DirectoryServiceBean directoryServiceBean, String address )
    {
        ChangePasswordServerBean changePasswordServerBean = directoryServiceBean.getChangePasswordServerBean();
        for ( TransportBean transportBean : changePasswordServerBean.getTransports() )
        {
            transportBean.setTransportAddress( address );
        }
    }

}
