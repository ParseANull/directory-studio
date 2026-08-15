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


import java.util.List;

import org.apache.directory.server.config.beans.ChangePasswordServerBean;
import org.apache.directory.server.config.beans.DirectoryServiceBean;
import org.apache.directory.server.config.beans.KdcServerBean;
import org.apache.directory.server.config.beans.PartitionBean;
import org.apache.directory.server.config.beans.TransportBean;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;


// ── CLASS: OverviewPage — Tarkin's Four-Quadrant Tactical Briefing ────────
// Grand Moff Tarkin stands before the Death Star's central display, four
// quadrants glowing: comms, encryption, vaults, and Imperial policy — all
// the critical readings in one room before diving into any specialist wing.
// This page is that briefing room: the summary tab that shows the highlights
// of every major server subsystem without making the user dig through tabs.
// ─────────────────────────────────────────────────────────────────────────
/**
 * The Overview tab of the ApacheDS Server Configuration Editor.
 * It exposes the most frequently changed settings across four panels —
 * LDAP/LDAPS transport, Kerberos authentication, Partitions, and Options —
 * arranged in a two-column layout with hyperlinks into each specialist tab.
 * Think of this class as Grand Moff Tarkin's tactical briefing room: every
 * key status indicator visible at once, with a direct line to each department
 * for the full picture.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OverviewPage extends ServerConfigurationEditorPage
{
    /** The Page ID*/
    public static final String ID = OverviewPage.class.getName(); //$NON-NLS-1$

    /** The Page Title */
    private static final String TITLE = Messages.getString( "OverviewPage.Overview" ); //$NON-NLS-1$

    // UI Controls
    /** LDAP Server controls */
    private Button enableLdapCheckbox;
    private Text ldapPortText;
    private Button enableLdapsCheckbox;
    private Text ldapsPortText;
    // This link opens the advanced LDAP/LDAPS configuration tab
    private Hyperlink openLdapConfigurationLink;

    /** Kerberos Server controls */
    private Button enableKerberosCheckbox;
    private Text kerberosPortText;
    private Button enableChangePasswordCheckbox;
    private Text changePasswordPortText;
    // This link opens the advanced kerberos configuration tab
    private Hyperlink openKerberosConfigurationLink;

    /** The Partitions controls */
    private Label partitionsLabel;
    private TableViewer partitionsTableViewer;
    // This link open the advanced partitions configuration Tab */
    private Hyperlink openPartitionsConfigurationLink;

    /** The LDAP Options controls */
    private Button allowAnonymousAccessCheckbox;
    private Button enableAccessControlCheckbox;
    private Button enableHiddenPasswordCheckbox;

    // UI Control Listeners
    /**
     * Fires when the user ticks or unticks the LDAP server checkbox.
     * Enables or disables the LDAP server in the model, and toggles
     * the port text field to match.
     */
    private SelectionAdapter enableLdapCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            boolean enableLdap = enableLdapCheckbox.getSelection();
            LdapLdapsServersPage.getLdapServerTransportBean( getDirectoryServiceBean() ).setEnabled(
                enableLdap );
            setEnabled( ldapPortText, enableLdap );
        }
    };


    /**
     * Fires whenever the user edits the LDAP port number field.
     * Parses the text as an integer and pushes it to the transport bean;
     * quietly ignores the change if the text is not a valid number yet.
     */
    private ModifyListener ldapPortTextListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            try
            {
                int port = Integer.parseInt( ldapPortText.getText() );

                LdapLdapsServersPage.getLdapServerTransportBean( getDirectoryServiceBean() ).setSystemPort( port );
            }
            catch ( NumberFormatException nfe )
            {
                System.out.println( "Wrong LDAP TCP Port : it must be an integer" );
            }
        }
    };


    /**
     * Fires when the user ticks or unticks the LDAPS server checkbox.
     * Enables or disables the LDAPS transport in the model and toggles
     * the LDAPS port text field accordingly.
     */
    private SelectionAdapter enableLdapsCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            boolean enableLdaps = enableLdapsCheckbox.getSelection();
            LdapLdapsServersPage.getLdapTransportBean( getDirectoryServiceBean(), LdapLdapsServersPage.TRANSPORT_ID_LDAPS ).setEnabled(
                enableLdaps );
            setEnabled( ldapsPortText, enableLdaps );
        }
    };


    /**
     * Fires whenever the user edits the LDAPS port number field.
     * Parses the text as an integer and pushes it to the LDAPS transport bean;
     * quietly ignores the change if the value is not a valid integer yet.
     */
    private ModifyListener ldapsPortTextListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            try
            {
                int port = Integer.parseInt( ldapsPortText.getText() );

                LdapLdapsServersPage.getLdapsServerTransportBean( getDirectoryServiceBean() ).setSystemPort( port );
            }
            catch ( NumberFormatException nfe )
            {
                System.out.println( "Wrong LDAPS TCP Port : it must be an integer" );
            }
        }
    };


    /**
     * Fires when the user clicks the "Advanced LDAP/LDAPS Configuration" hyperlink.
     * Navigates the editor to the dedicated LDAP/LDAPS servers page so the user
     * can configure everything beyond port and enable state.
     */
    private HyperlinkAdapter openLdapConfigurationLinkListener = new HyperlinkAdapter()
    {
        public void linkActivated( HyperlinkEvent e )
        {
            getServerConfigurationEditor().showPage( LdapLdapsServersPage.class );
        }
    };


    /**
     * Fires when the user ticks or unticks the Kerberos server checkbox.
     * Enables or disables the KDC in the model and toggles the Kerberos
     * port text field to match.
     */
    private SelectionAdapter enableKerberosCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            boolean enableKerberos = enableKerberosCheckbox.getSelection();
            KerberosServerPage.enableKerberosServer( getDirectoryServiceBean(), enableKerberos );
            setEnabled( kerberosPortText, enableKerberos );
        }
    };

    /**
     * Fires whenever the user edits the Kerberos port field.
     * Delegates the text value directly to the Kerberos server page helper
     * which handles parsing and model update.
     */
    private ModifyListener kerberosPortTextListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            KerberosServerPage.setKerberosPort( getDirectoryServiceBean(), kerberosPortText.getText() );
        }
    };

    /**
     * Fires when the user ticks or unticks the Change Password server checkbox.
     * Updates the Change Password server bean's enabled state in the model and
     * toggles the Change Password port text field to match.
     */
    private SelectionAdapter enableChangePasswordCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            ChangePasswordServerBean changePasswordServerBean = getDirectoryServiceBean().getChangePasswordServerBean();
            boolean enableChangePassword = enableChangePasswordCheckbox.getSelection();
            changePasswordServerBean.setEnabled( enableChangePassword );
            setEnabled( changePasswordPortText, enableChangePassword );
        }
    };

    /**
     * Fires whenever the user edits the Change Password server port field.
     * Delegates the text value to the Kerberos server page helper which
     * handles parsing and model update.
     */
    private ModifyListener changePasswordPortTextListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            KerberosServerPage.setChangePasswordPort( getDirectoryServiceBean(), changePasswordPortText.getText() );
        }
    };

    /**
     * Fires when the user clicks the "Advanced Kerberos Configuration" hyperlink.
     * Navigates the editor to the dedicated Kerberos server page so the user
     * can configure realms, encryption types, and all the details.
     */
    private HyperlinkAdapter openKerberosConfigurationLinkListener = new HyperlinkAdapter()
    {
        public void linkActivated( HyperlinkEvent e )
        {
            getServerConfigurationEditor().showPage( KerberosServerPage.class );
        }
    };


    /**
     * Fires when the user clicks the "Advanced Partitions Configuration" hyperlink.
     * Navigates the editor to the Partitions tab so the user can add, remove,
     * or fully configure individual partitions.
     */
    private HyperlinkAdapter openPartitionsConfigurationLinkListener = new HyperlinkAdapter()
    {
        public void linkActivated( HyperlinkEvent e )
        {
            getServerConfigurationEditor().showPage( PartitionsPage.class );
        }
    };


    /**
     * Fires when the user ticks or unticks the "Allow Anonymous Access" checkbox.
     * Pushes the new boolean directly to the directory service bean.
     */
    private SelectionAdapter allowAnonymousAccessCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            getDirectoryServiceBean().setDsAllowAnonymousAccess( allowAnonymousAccessCheckbox.getSelection() );
        }
    };


    /**
     * Fires when the user ticks or unticks the "Enable Access Control" checkbox.
     * Pushes the new boolean directly to the directory service bean.
     */
    private SelectionAdapter enableAccessControlCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            getDirectoryServiceBean().setDsAccessControlEnabled( enableAccessControlCheckbox.getSelection() );
        }
    };


    /**
     * Fires when the user ticks or unticks the "Enable Hidden Password" checkbox.
     * Pushes the new boolean directly to the directory service bean.
     */
    private SelectionAdapter enableHiddenPasswordCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            getDirectoryServiceBean().setDsPasswordHidden( enableHiddenPasswordCheckbox.getSelection() );
        }
    };


    // ── Tarkin Enters the Briefing Chamber ───────────────────────────────────
    // Grand Moff Tarkin strides into the Death Star's central command room
    // for the first time, ID badge in hand, taking his station at the head
    // of the briefing table where all four quadrant displays await activation.
    // This constructor registers the page with the parent editor under its
    // unique ID so Eclipse Forms can route navigation to us correctly.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the Overview page and registers it with the parent editor.
     * We pass our stable page ID and human-readable title up to the base class
     * so the editor knows where to find us when navigating between tabs.
     *
     * <p>For example — Tarkin claims his station:</p>
     * <pre>
     *   The Grand Moff scans his credentials across the door panel;
     *   the briefing room acknowledges him by name and slot.
     *   From now on, any officer who says "Take me to the Overview"
     *   gets routed straight here.
     * </pre>
     *
     * @param editor  the parent {@link ServerConfigurationEditor} that owns this page
     */
    public OverviewPage( ServerConfigurationEditor editor )
    {
        super( editor, ID, TITLE );
    }


    // ── Four-Quadrant Tactical Display Assembled ─────────────────────────────
    // Tarkin watches as the technicians roll in four display panels and bolt
    // them into the two-column briefing frame — comms on the left, crypto on
    // the right, vaults bottom-left, policy bottom-right.
    // We lay out the two-column TableWrapLayout and delegate each quadrant
    // to its own section-creation helper, then refresh from the live model.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the full Overview tab UI inside the given parent composite.
     * We create a two-column {@link TableWrapLayout}, put the LDAP and
     * Partitions sections in the left column, and the Kerberos and Options
     * sections in the right column, then populate all controls from the model.
     *
     * <p>For example — the briefing room takes shape:</p>
     * <pre>
     *   Left column  : LDAP/LDAPS comms panel, then Partitions vault list.
     *   Right column : Kerberos auth panel, then Imperial policy options.
     *   Each panel is its own Section widget, and a final refreshUI() call
     *   fills every field with the current server configuration values.
     * </pre>
     *
     * @param parent   the parent composite provided by the Eclipse Forms framework
     * @param toolkit  the form toolkit used to create styled widgets
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
        createLdapLdapsServersSection( toolkit, leftComposite );
        createPartitionsSection( toolkit, leftComposite );
        createKerberosChangePasswordServersSection( toolkit, rightComposite );
        createOptionsSection( toolkit, rightComposite );

        // Refreshing the UI
        refreshUI();
    }


    // ── Communications Array Panel Configured ───────────────────────────────
    // An Imperial comms officer patches in the main subspace transceiver bank:
    // two channels — standard LDAP and the encrypted LDAPS variant — each with
    // its own enable switch, port control, and an escape hatch to full settings.
    // We build a four-column Section with enable checkboxes, port text fields,
    // default-value labels, and a hyperlink to the advanced LDAP/LDAPS tab.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the LDAP and LDAPS Servers section in the left column.
     * The section contains an enable/disable checkbox and a port number field
     * for each of the two transports, plus a hyperlink to the full LDAP/LDAPS
     * configuration tab for anything more advanced.
     *
     * <p>For example — the comms bank comes online:</p>
     * <pre>
     *   Row 1: [X] Enable LDAP server          (checkbox spans all 4 cols)
     *   Row 2:   [indent] Port: [text] [default hint]
     *   Row 3: [X] Enable LDAPS server         (checkbox spans all 4 cols)
     *   Row 4:   [indent] Port: [text] [default hint]
     *   Row 5: "Advanced LDAP/LDAPS Configuration" hyperlink
     * </pre>
     *
     * @param toolkit  the form toolkit for creating styled widgets
     * @param parent   the left-column composite to place this section inside
     */
    private void createLdapLdapsServersSection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section
        int nbColumns = 4;
        Composite composite = createSection( toolkit, parent, "OverviewPage.LdapLdapsServers", nbColumns, Section.TITLE_BAR );

        // Enable LDAP Server Checkbox
        enableLdapCheckbox = toolkit.createButton( composite,
            Messages.getString( "OverviewPage.EnableLdapServer" ), SWT.CHECK ); //$NON-NLS-1$
        enableLdapCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, nbColumns, 1 ) );

        // LDAP Server Port Text
        toolkit.createLabel( composite, TABULATION );
        toolkit.createLabel( composite, Messages.getString( "OverviewPage.Port" ) ); //$NON-NLS-1$
        ldapPortText = createPortText( toolkit, composite );
        createDefaultValueLabel( toolkit, composite, Integer.toString( DEFAULT_PORT_LDAP ) ); //$NON-NLS-1$

        // Enable LDAPS Server Checkbox
        enableLdapsCheckbox = toolkit.createButton( composite,
            Messages.getString( "OverviewPage.EnableLdapsServer" ), SWT.CHECK ); //$NON-NLS-1$
        enableLdapsCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, nbColumns, 1 ) );

        // LDAPS Server Port Text
        toolkit.createLabel( composite, TABULATION );
        toolkit.createLabel( composite, Messages.getString( "OverviewPage.Port" ) ); //$NON-NLS-1$
        ldapsPortText = createPortText( toolkit, composite );
        createDefaultValueLabel( toolkit, composite, Integer.toString( DEFAULT_PORT_LDAPS ) ); //$NON-NLS-1$

        // LDAP Configuration Link
        openLdapConfigurationLink = toolkit.createHyperlink( composite,
            Messages.getString( "OverviewPage.AdvancedLdapLdapsConfiguration" ), SWT.NONE ); //$NON-NLS-1$
        openLdapConfigurationLink.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, nbColumns, 1 ) );
        openLdapConfigurationLink.addHyperlinkListener( openLdapConfigurationLinkListener );
    }


    // ── Encryption and Auth Vault Panel Deployed ─────────────────────────────
    // The Imperial security division plugs in the Kerberos authentication array
    // and the password-change relay — two sub-systems that keep enemy agents
    // out of the Death Star's data network. Each gets its own enable toggle
    // and port setting, plus a quick exit to the full Kerberos configuration.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the Kerberos and Change Password Servers section in the right column.
     * Mirrors the structure of the LDAP section: enable checkbox and port field
     * for each service, plus a hyperlink to the full Kerberos configuration tab.
     *
     * <p>For example — the auth relay comes online:</p>
     * <pre>
     *   Row 1: [X] Enable Kerberos Server       (checkbox spans all 4 cols)
     *   Row 2:   [indent] Port: [text] [default hint]
     *   Row 3: [X] Enable Kerberos Change Pwd   (checkbox spans all 4 cols)
     *   Row 4:   [indent] Port: [text] [default hint]
     *   Row 5: "Advanced Kerberos Configuration" hyperlink
     * </pre>
     *
     * @param toolkit  the form toolkit for creating styled widgets
     * @param parent   the right-column composite to place this section inside
     */
    private void createKerberosChangePasswordServersSection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section
        int nbColumns = 4;
        Composite composite = createSection( toolkit, parent, "OverviewPage.KerberosServer", nbColumns, Section.TITLE_BAR );

        // Enable Kerberos Server Checkbox
        enableKerberosCheckbox = toolkit.createButton( composite,
            Messages.getString( "OverviewPage.EnableKerberosServer" ), SWT.CHECK ); //$NON-NLS-1$
        enableKerberosCheckbox
            .setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, nbColumns, 1 ) );

        // Kerberos Server Port Text
        toolkit.createLabel( composite, TABULATION );
        toolkit.createLabel( composite, Messages.getString( "OverviewPage.Port" ) ); //$NON-NLS-1$
        kerberosPortText = createPortText( toolkit, composite );
        createDefaultValueLabel( toolkit, composite, Integer.toString( DEFAULT_PORT_KERBEROS ) ); //$NON-NLS-1$

        // Enable Change Password Server Checkbox
        enableChangePasswordCheckbox = toolkit.createButton( composite,
            Messages.getString( "OverviewPage.EnableKerberosChangePasswordServer" ), //$NON-NLS-1$
            SWT.CHECK );
        enableChangePasswordCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false,
            nbColumns, 1 ) );

        // Change Password Server Port Text
        toolkit.createLabel( composite, TABULATION );
        toolkit.createLabel( composite, Messages.getString( "OverviewPage.Port" ) ); //$NON-NLS-1$
        changePasswordPortText = createPortText( toolkit, composite );
        createDefaultValueLabel( toolkit, composite, Integer.toString( DEFAULT_PORT_CHANGE_PASSWORD ) ); //$NON-NLS-1$

        // Kerberos Configuration Link
        openKerberosConfigurationLink = toolkit.createHyperlink( composite,
            Messages.getString( "OverviewPage.AdvancedKerberosConfiguration" ), SWT.NONE ); //$NON-NLS-1$
        openKerberosConfigurationLink.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false,
            nbColumns, 1 ) );
        openKerberosConfigurationLink.addHyperlinkListener( openKerberosConfigurationLinkListener );
    }


    // ── Vault Registry Panel Installed in Briefing Room ─────────────────────
    // A junior officer wheels in a compact display showing the names of every
    // Imperial data vault currently registered — not the full vault spec, just
    // the roster and a quick count, with a link to the full vault management wing.
    // We build a single-column Section with a count label, a read-only table
    // of partition names, and a hyperlink to the Partitions configuration tab.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the Partitions summary section in the left column.
     * This is an informational panel only — we show how many partitions exist
     * and list their names, but editing happens on the dedicated Partitions tab.
     * A hyperlink at the bottom jumps the user straight there.
     *
     * <p>For example — the vault roster board goes up:</p>
     * <pre>
     *   Label : "There are 3 partitions defined"
     *   Table : | system  |   (read-only, scrollable)
     *           | example |
     *           | ...     |
     *   Link  : "Advanced Partitions Configuration"
     * </pre>
     *
     * @param toolkit  the form toolkit for creating styled widgets
     * @param parent   the left-column composite to place this section inside
     */
    private void createPartitionsSection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section
        int nbColumns = 1;
        Composite composite = createSection( toolkit, parent, "OverviewPage.Partitions", nbColumns, Section.TITLE_BAR );

        // Partitions Label
        partitionsLabel = toolkit.createLabel( composite, "" ); //$NON-NLS-1$
        partitionsLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Partitions Table Viewer
        Table partitionsTable = toolkit.createTable( composite, SWT.NULL );
        GridData gd = new GridData( SWT.FILL, SWT.NONE, true, false );
        gd.heightHint = 45;
        partitionsTable.setLayoutData( gd );
        partitionsTableViewer = new TableViewer( partitionsTable );
        partitionsTableViewer.setContentProvider( new ArrayContentProvider() );
        partitionsTableViewer.setLabelProvider( PartitionsPage.PARTITIONS_LABEL_PROVIDER );
        partitionsTableViewer.setComparator( PartitionsPage.PARTITIONS_COMPARATOR );

        // Partitions Configuration Link
        openPartitionsConfigurationLink = toolkit.createHyperlink( composite,
            Messages.getString( "OverviewPage.AdvancedPartitionsConfiguration" ), SWT.NONE ); //$NON-NLS-1$
        openPartitionsConfigurationLink.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, nbColumns, 1 ) );
        openPartitionsConfigurationLink.addHyperlinkListener( openPartitionsConfigurationLinkListener );
    }


    // ── Imperial Policy Panel Erected in the Briefing Room ──────────────────
    // The policy officer posts three standing orders on the right-side board:
    // whether civilians may enter without credentials, whether access control
    // is actively enforced, and whether passwords are masked in reports.
    // We build a single-column Section with three checkboxes covering the
    // most critical directory service security flags.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the Options section in the right column.
     * Contains three checkboxes for the most commonly toggled security flags:
     * anonymous access, access control enforcement, and hidden passwords.
     * More obscure options live on dedicated specialist tabs.
     *
     * <p>For example — standing orders posted:</p>
     * <pre>
     *   [X] Allow Anonymous Access   — any rebel can walk in unchallenged
     *   [X] Enable Access Control    — ACL rules are actively checked
     *   [X] Password Hidden          — passwords never appear in plain text
     * </pre>
     *
     * @param toolkit  the form toolkit for creating styled widgets
     * @param parent   the right-column composite to place this section inside
     */
    private void createOptionsSection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section
        int nbColumns = 1;
        Composite composite = createSection( toolkit, parent, "OverviewPage.Options", nbColumns, Section.TITLE_BAR );

        // Allow Anonymous Access Checkbox
        allowAnonymousAccessCheckbox = toolkit.createButton( composite,
            Messages.getString( "OverviewPage.AllowAnonymousAccess" ), SWT.CHECK ); //$NON-NLS-1$
        allowAnonymousAccessCheckbox.setLayoutData( new GridData( SWT.NONE, SWT.NONE, true, false ) );

        // Enable Access Control Checkbox
        enableAccessControlCheckbox = toolkit.createButton( composite,
            Messages.getString( "OverviewPage.EnableAccessControl" ), SWT.CHECK ); //$NON-NLS-1$
        enableAccessControlCheckbox.setLayoutData( new GridData( SWT.NONE, SWT.NONE, true, false ) );

        // Enable Hidden Password Checkbox
        enableHiddenPasswordCheckbox = toolkit.createButton( composite,
            Messages.getString( "OverviewPage.EnableHiddenPassword" ), SWT.CHECK ); //$NON-NLS-1$
        enableHiddenPasswordCheckbox.setLayoutData( new GridData( SWT.NONE, SWT.NONE, true, false ) );
    }


    // ── Sentries Posted at Every Control Panel ───────────────────────────────
    // Tarkin gives the order: "Station a guard at every switch, button, and
    // dial on this briefing room floor." Each sentry knows exactly what to
    // report back when their control is touched.
    // We attach all the pre-wired listener instances to their matching widgets,
    // including dirty-marking so the editor knows when something has changed.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Wires all the pre-built listener instances to their matching UI controls.
     * Every checkbox and text field gets both a dirty listener (so the editor's
     * Save button lights up) and its dedicated business-logic listener.
     * Call this after loading data into the widgets to avoid spurious dirty events.
     */
    private void addListeners()
    {
        // Enable LDAP Checkbox
        addDirtyListener( enableLdapCheckbox );
        addSelectionListener( enableLdapCheckbox, enableLdapCheckboxListener );

        // LDAP Port Text
        addDirtyListener( ldapPortText );
        addModifyListener( ldapPortText, ldapPortTextListener );

        // Enable LDAPS Checkbox
        addDirtyListener( enableLdapsCheckbox );
        addSelectionListener( enableLdapsCheckbox, enableLdapsCheckboxListener );

        // LDAPS Port Text
        addDirtyListener( ldapsPortText );
        addModifyListener( ldapsPortText, ldapsPortTextListener );

        // Enable Kerberos Checkbox
        addDirtyListener( enableKerberosCheckbox );
        addSelectionListener( enableKerberosCheckbox, enableKerberosCheckboxListener );

        // Kerberos Port Text
        addDirtyListener( kerberosPortText );
        addModifyListener( kerberosPortText, kerberosPortTextListener );

        // Enable Change Password Checkbox
        addDirtyListener( enableChangePasswordCheckbox );
        addSelectionListener( enableChangePasswordCheckbox, enableChangePasswordCheckboxListener );

        // Change Password Port Text
        addDirtyListener( changePasswordPortText );
        addModifyListener( changePasswordPortText, changePasswordPortTextListener );

        // Allow Anonymous Access Checkbox
        addDirtyListener( allowAnonymousAccessCheckbox );
        addSelectionListener( allowAnonymousAccessCheckbox, allowAnonymousAccessCheckboxListener );

        // Enable Access Control Checkbox
        addDirtyListener( enableAccessControlCheckbox );
        addSelectionListener( enableAccessControlCheckbox, enableAccessControlCheckboxListener );

        // Enable Hidden Password Checkbox
        addDirtyListener( enableHiddenPasswordCheckbox );
        addSelectionListener( enableHiddenPasswordCheckbox, enableHiddenPasswordCheckboxListener );
    }


    // ── Sentries Stood Down Before Data Reload ───────────────────────────────
    // Before the technicians update every readout on the briefing room displays,
    // Tarkin orders the sentries to stand down — otherwise they would file a
    // report for every single number that flips during the data refresh cycle.
    // We detach all listeners before programmatic updates so we don't fire
    // change events (and mark the editor dirty) when we're just loading data.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Detaches all listener instances from their matching UI controls.
     * We always call this before programmatically populating widgets with
     * model data so that the act of loading doesn't trigger dirty events.
     * Pair every call to this method with a subsequent call to {@link #addListeners()}.
     */
    private void removeListeners()
    {
        // Enable LDAP Checkbox
        removeDirtyListener( enableLdapCheckbox );
        removeSelectionListener( enableLdapCheckbox, enableLdapCheckboxListener );

        // LDAP Port Text
        removeDirtyListener( ldapPortText );
        removeModifyListener( ldapPortText, ldapPortTextListener );

        // Enable LDAPS Checkbox
        removeDirtyListener( enableLdapsCheckbox );
        removeSelectionListener( enableLdapsCheckbox, enableLdapsCheckboxListener );

        // LDAPS Port Text
        removeDirtyListener( ldapsPortText );
        removeModifyListener( ldapsPortText, ldapsPortTextListener );

        // Enable Kerberos Checkbox
        removeDirtyListener( enableKerberosCheckbox );
        removeSelectionListener( enableKerberosCheckbox, enableKerberosCheckboxListener );

        // Kerberos Port Text
        removeDirtyListener( kerberosPortText );
        removeModifyListener( kerberosPortText, kerberosPortTextListener );

        // Enable Change Password Checkbox
        removeDirtyListener( enableChangePasswordCheckbox );
        removeSelectionListener( enableChangePasswordCheckbox, enableChangePasswordCheckboxListener );

        // Change Password Port Text
        removeDirtyListener( changePasswordPortText );
        removeModifyListener( changePasswordPortText, changePasswordPortTextListener );

        // Allow Anonymous Access Checkbox
        removeDirtyListener( allowAnonymousAccessCheckbox );
        removeSelectionListener( allowAnonymousAccessCheckbox, allowAnonymousAccessCheckboxListener );

        // Enable Access Control Checkbox
        removeDirtyListener( enableAccessControlCheckbox );
        removeSelectionListener( enableAccessControlCheckbox, enableAccessControlCheckboxListener );

        // Enable Hidden Password Checkbox
        removeDirtyListener( enableHiddenPasswordCheckbox );
        removeSelectionListener( enableHiddenPasswordCheckbox, enableHiddenPasswordCheckboxListener );
    }


    // ── Tarkin Reviews the Updated Status Readout ────────────────────────────
    // A fresh intelligence packet arrives; Tarkin waves the sentries aside,
    // the technicians update every dial and indicator on all four panels, then
    // the sentries resume their posts and Tarkin reads the current state.
    // We pull fresh data from the DirectoryServiceBean, update every widget,
    // and re-attach listeners so user changes are captured going forward.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Reloads all UI controls from the current state of the configuration model.
     * We first stand down all listeners to prevent spurious dirty events during
     * the update, then read each transport bean, partition list, and option flag
     * from the {@link DirectoryServiceBean} and push the values into the widgets,
     * then reattach the listeners.
     * This method is a no-op if the page hasn't finished initializing yet.
     */
    protected void refreshUI()
    {
        if ( isInitialized() )
        {
            removeListeners();

            DirectoryServiceBean directoryServiceBean = getDirectoryServiceBean();

            // LDAP Server
            TransportBean ldapServerTransportBean = LdapLdapsServersPage
                .getLdapServerTransportBean( directoryServiceBean );
            setSelection( enableLdapCheckbox, ldapServerTransportBean.isEnabled() );
            setEnabled( ldapPortText, enableLdapCheckbox.getSelection() );
            setText( ldapPortText, Integer.toString( ldapServerTransportBean.getSystemPort() ) );

            // LDAPS Server
            TransportBean ldapsServerTransportBean = LdapLdapsServersPage
                .getLdapsServerTransportBean( directoryServiceBean );
            setSelection( enableLdapsCheckbox, ldapsServerTransportBean.isEnabled() );
            setEnabled( ldapsPortText, enableLdapsCheckbox.getSelection() );
            setText( ldapsPortText, Integer.toString( ldapsServerTransportBean.getSystemPort() ) );

            // Kerberos Server
            KdcServerBean kdcServerBean = KerberosServerPage.getKdcServerBean( directoryServiceBean );
            setSelection( enableKerberosCheckbox, kdcServerBean.isEnabled() );
            setEnabled( kerberosPortText, enableKerberosCheckbox.getSelection() );
            setText( kerberosPortText, Integer.toString( kdcServerBean.getTransports()[0].getSystemPort() ) );

            // Change Password Server
            ChangePasswordServerBean changePasswordServerBean = KerberosServerPage
                .getChangePasswordServerBean( directoryServiceBean );
            setSelection( enableChangePasswordCheckbox, changePasswordServerBean.isEnabled() );
            setEnabled( changePasswordPortText, enableChangePasswordCheckbox.getSelection() );
            setText( changePasswordPortText,
                Integer.toString( changePasswordServerBean.getTransports()[0].getSystemPort() ) );

            // Partitions
            List<PartitionBean> partitions = directoryServiceBean.getPartitions();

            if ( partitions.size() == 1 )
            {
                partitionsLabel.setText( Messages.getString( "OverviewPage.ThereIsOnePartitionDefined" ) ); //$NON-NLS-1$
            }
            else
            {
                partitionsLabel.setText( NLS.bind(
                    Messages.getString( "OverviewPage.ThereAreXPartitionsDefined" ), partitions.size() ) ); //$NON-NLS-1$
            }

            partitionsTableViewer.setInput( partitions.toArray() );

            // Options
            allowAnonymousAccessCheckbox.setSelection( directoryServiceBean.isDsAllowAnonymousAccess() );
            enableAccessControlCheckbox.setSelection( directoryServiceBean.isDsAccessControlEnabled() );
            enableHiddenPasswordCheckbox.setSelection( directoryServiceBean.isDsPasswordHidden() );

            addListeners();
        }
    }
}
