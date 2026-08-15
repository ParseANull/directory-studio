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

package org.apache.directory.studio.ldapbrowser.ui.dialogs.properties;


import org.apache.commons.lang3.StringUtils;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionServerType;
import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.jobs.ServerTypeDetector;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;


// ── CLASS: RootDSEPropertyPage — LUKE'S BINARY SUNSET ON TATOOINE ─────────────
// The Root DSE is the topmost entry in an LDAP directory — the horizon from which
// every branch of the tree descends.  Luke stands at his viewpoint and sees the
// whole horizon: what kind of server is out there (directory type), what LDAP
// versions it speaks, what authentication mechanisms it supports, which controls
// and extensions it advertises, and which optional features it can handle.
// This property page is that horizon view — four tabs covering Info, Controls,
// Extensions, and Features — all read directly from the Root DSE attributes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse property page that displays Root DSE metadata for an LDAP connection.
 * The Root DSE (DSA-Specific Entry) is the virtual top of the directory tree;
 * it advertises the server's capabilities via operational attributes.
 * This page shows those capabilities organized into four tabs: Info (server
 * type, vendor, LDAP versions, SASL mechanisms), Controls (supported control
 * OIDs), Extensions (supported extended operation OIDs), and Features.
 * Think of this page as Luke's binary sunset — the complete horizon of what a
 * connected LDAP server knows how to do.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RootDSEPropertyPage extends PropertyPage implements IWorkbenchPropertyPage
{
    /** The tab folder. */
    private TabFolder tabFolder;

    /** The info tab. */
    private TabItem infoTab;

    /** The controls tab. */
    private TabItem controlsTab;

    /** The extensions tab. */
    private TabItem extensionsTab;

    /** The features tab. */
    private TabItem featuresTab;


    // ── LUKE STEPS UP TO THE HORIZON VIEWPOINT ────────────────────────────────
    // Luke walks out to the rim of the moisture farm and removes all clutter:
    // no Apply, no Defaults — just the horizon to observe.
    // This is a pure read-only information page; we hide the apply/default buttons.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the property page and suppresses the Default and Apply buttons.
     * The Root DSE page is informational only; there's nothing to apply or reset.
     *
     * <p>For example — Luke clears the viewpoint of distractions:</p>
     * <pre>
     *   noDefaultAndApplyButton() → clean tabbed information display
     * </pre>
     */
    public RootDSEPropertyPage()
    {
        super();
        super.noDefaultAndApplyButton();
    }


    // ── LUKE IDENTIFIES THE PLANET HE'S LOOKING AT ────────────────────────────
    // Before Luke can take in the horizon, he needs to confirm this is actually
    // Tatooine — he asks the element adapter for the IBrowserConnection that
    // represents the LDAP server he's connected to.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Extracts the {@link IBrowserConnection} from the given Eclipse selection element.
     * Tries the direct {@link IBrowserConnection} adapter first; if that fails, tries
     * to get a raw {@link Connection} and look up its browser counterpart via the
     * connection manager.
     * Returns {@code null} if neither approach yields a connection.
     *
     * <p>For example — Luke confirms this is his home system:</p>
     * <pre>
     *   element.getAdapter(IBrowserConnection.class) → connection found → tabs populated
     *   element.getAdapter(IBrowserConnection.class) → null →
     *     element.getAdapter(Connection.class) → look up browser connection
     * </pre>
     *
     * @param element  The Eclipse selection element; typically a connection node.
     * @return         The {@link IBrowserConnection} to display, or {@code null}.
     */
    static IBrowserConnection getConnection( Object element )
    {
        IBrowserConnection browserConnection = null;
        if ( element instanceof IAdaptable )
        {
            browserConnection = ( IBrowserConnection ) ( ( IAdaptable ) element ).getAdapter( IBrowserConnection.class );
            if ( browserConnection == null )
            {
                Connection connection = ( Connection ) ( ( IAdaptable ) element ).getAdapter( Connection.class );
                browserConnection = BrowserCorePlugin.getDefault().getConnectionManager().getBrowserConnection(
                    connection );
            }
        }
        return browserConnection;
    }


    // ── LUKE SCANS THE FULL HORIZON IN FOUR SWEEPS ────────────────────────────
    // Luke turns slowly, taking in the full panorama in four sweeps: general
    // server info, the control OIDs the server speaks, the extended operations
    // it supports, and the optional features it advertises.
    // We build four tabs, each pulling specific Root DSE attributes and rendering
    // them as labeled text widgets.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the property page UI as a four-tab folder: Info, Controls, Extensions,
     * and Features.
     * The Info tab shows directory type (auto-detected), vendor info, LDAP versions,
     * and SASL mechanisms.  The other three tabs each call {@link #addOidInfo} to
     * list the relevant OID attributes from the Root DSE with their human-readable
     * descriptions where known.
     *
     * <p>For example — Luke's four-sweep horizon scan:</p>
     * <pre>
     *   Info: ApacheDS 2.0.0, vendorName=Apache, supportedLDAPVersion=[2,3]
     *   Controls:   1.2.840.113556.1.4.319  (Paged Results)
     *               2.16.840.1.113730.3.4.2 (ManageDsaIT)
     *   Extensions: 1.3.6.1.4.1.1466.20037 (StartTLS)
     *   Features:   (empty for many servers)
     * </pre>
     *
     * @param parent  The parent composite provided by Eclipse's property dialog.
     * @return        The tab folder control.
     */
    protected Control createContents( Composite parent )
    {
        final IBrowserConnection connection = getConnection( getElement() );

        tabFolder = new TabFolder( parent, SWT.TOP );
        RowLayout mainLayout = new RowLayout();
        mainLayout.fill = true;
        mainLayout.marginWidth = 0;
        mainLayout.marginHeight = 0;
        tabFolder.setLayout( mainLayout );

        // Info tab
        Composite infoComposite = new Composite( tabFolder, SWT.NONE );
        GridLayout gl = new GridLayout( 2, false );
        infoComposite.setLayout( gl );
        BaseWidgetUtils.createLabel( infoComposite, Messages.getString( "RootDSEPropertyPage.DirectoryType" ), 1 ); //$NON-NLS-1$
        Text typeText = BaseWidgetUtils.createWrappedLabeledText( infoComposite, "-", 1, 150 ); //$NON-NLS-1$
        if ( connection != null && connection.getRootDSE() != null )
        {
            // Try to detect LDAP server from RootDSE
            ConnectionServerType serverType = ServerTypeDetector.detectServerType( connection.getRootDSE() );
            if ( serverType != null )
            {
                switch ( serverType )
                {
                    case APACHEDS:
                        typeText.setText( Messages.getString( "RootDSEPropertyPage.ApacheDirectoryServer" ) ); //$NON-NLS-1$
                        break;
                    case IBM_DIRECTORY_SERVER:
                        typeText.setText( Messages.getString( "RootDSEPropertyPage.IBMDirectory" ) ); //$NON-NLS-1$
                        break;
                    case IBM_SECUREWAY_DIRECTORY:
                        typeText.setText( Messages.getString( "RootDSEPropertyPage.IBMSecureWay" ) ); //$NON-NLS-1$
                        break;
                    case IBM_TIVOLI_DIRECTORY_SERVER:
                        typeText.setText( Messages.getString( "RootDSEPropertyPage.IBMTivoli" ) ); //$NON-NLS-1$
                        break;
                    case MICROSOFT_ACTIVE_DIRECTORY_2000:
                        typeText.setText( Messages.getString( "RootDSEPropertyPage.MSAD2000" ) ); //$NON-NLS-1$
                        break;
                    case MICROSOFT_ACTIVE_DIRECTORY_2003:
                        typeText.setText( Messages.getString( "RootDSEPropertyPage.MSAD2003" ) ); //$NON-NLS-1$
                        break;
                    case NETSCAPE:
                        typeText.setText( Messages.getString( "RootDSEPropertyPage.NetscapeDirectoryServer" ) ); //$NON-NLS-1$
                        break;
                    case NOVELL:
                        typeText.setText( Messages.getString( "RootDSEPropertyPage.NovellEDirectory" ) ); //$NON-NLS-1$
                        break;
                    case OPENLDAP:
                        typeText.setText( Messages.getString( "RootDSEPropertyPage.OpenLDAP" ) ); //$NON-NLS-1$
                        break;
                    case OPENLDAP_2_0:
                        typeText.setText( Messages.getString( "RootDSEPropertyPage.OpenLDAP20" ) ); //$NON-NLS-1$
                        break;
                    case OPENLDAP_2_1:
                        typeText.setText( Messages.getString( "RootDSEPropertyPage.OpenLDAP21" ) ); //$NON-NLS-1$
                        break;
                    case OPENLDAP_2_2:
                        typeText.setText( Messages.getString( "RootDSEPropertyPage.OpenLDAP22" ) ); //$NON-NLS-1$
                        break;
                    case OPENLDAP_2_3:
                        typeText.setText( Messages.getString( "RootDSEPropertyPage.OpenLDAP23" ) ); //$NON-NLS-1$
                        break;
                    case OPENLDAP_2_4:
                        typeText.setText( Messages.getString( "RootDSEPropertyPage.OpenLDAP24" ) ); //$NON-NLS-1$
                        break;
                    case SIEMENS_DIRX:
                        typeText.setText( Messages.getString( "RootDSEPropertyPage.SiemensDirX" ) ); //$NON-NLS-1$
                        break;
                    case SUN_DIRECTORY_SERVER:
                        typeText.setText( Messages.getString( "RootDSEPropertyPage.SunDirectoryServer" ) ); //$NON-NLS-1$
                        break;
                }
            }
        }
        addInfo( connection, infoComposite, "vendorName", Messages.getString( "RootDSEPropertyPage.VendorName" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        addInfo( connection, infoComposite, "vendorVersion", Messages.getString( "RootDSEPropertyPage.VendorVersion" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        addInfo( connection, infoComposite,
            "supportedLDAPVersion", Messages.getString( "RootDSEPropertyPage.SupportedLDAPVersion" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        addInfo( connection, infoComposite,
            "supportedSASLMechanisms", Messages.getString( "RootDSEPropertyPage.SupportedSASL" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        infoTab = new TabItem( tabFolder, SWT.NONE );
        infoTab.setText( Messages.getString( "RootDSEPropertyPage.Info" ) ); //$NON-NLS-1$
        infoTab.setControl( infoComposite );

        // Controls tab
        Composite controlsComposite = new Composite( tabFolder, SWT.NONE );
        controlsComposite.setLayout( new GridLayout() );
        Composite controlsComposite2 = BaseWidgetUtils.createColumnContainer( controlsComposite, 2, 1 );
        addOidInfo( connection, controlsComposite2, "supportedControl" ); //$NON-NLS-1$
        controlsTab = new TabItem( tabFolder, SWT.NONE );
        controlsTab.setText( Messages.getString( "RootDSEPropertyPage.Controls" ) ); //$NON-NLS-1$
        controlsTab.setControl( controlsComposite );

        // Extensions tab
        Composite extensionComposite = new Composite( tabFolder, SWT.NONE );
        extensionComposite.setLayout( new GridLayout() );
        Composite extensionComposite2 = BaseWidgetUtils.createColumnContainer( extensionComposite, 2, 1 );
        addOidInfo( connection, extensionComposite2, "supportedExtension" ); //$NON-NLS-1$
        extensionsTab = new TabItem( tabFolder, SWT.NONE );
        extensionsTab.setText( Messages.getString( "RootDSEPropertyPage.Extensions" ) ); //$NON-NLS-1$
        extensionsTab.setControl( extensionComposite );

        // Features tab
        Composite featureComposite = new Composite( tabFolder, SWT.NONE );
        featureComposite.setLayout( new GridLayout() );
        Composite featureComposite2 = BaseWidgetUtils.createColumnContainer( featureComposite, 2, 1 );
        addOidInfo( connection, featureComposite2, "supportedFeatures" ); //$NON-NLS-1$
        featuresTab = new TabItem( tabFolder, SWT.NONE );
        featuresTab.setText( Messages.getString( "RootDSEPropertyPage.Features" ) ); //$NON-NLS-1$
        featuresTab.setControl( featureComposite );

        return tabFolder;
    }


    // ── LUKE READS EACH STAR'S OID NUMBER AND ITS COMMON NAME ─────────────────
    // Luke sees each control or extension as a star: a numeric OID on the left
    // (its catalog number) and a human-readable description on the right (its
    // common name from the RFC catalog).  If the OID isn't in the catalog, the
    // description is left blank rather than showing an error.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads all values of the given multi-valued Root DSE attribute and adds a row
     * of two labeled text widgets per value: the raw OID and its human-readable
     * description (looked up via {@link Utils#getOidDescription}).
     * Silently swallows any exception (e.g. attribute absent from the Root DSE).
     *
     * <p>For example — Luke reads each control OID and its catalog description:</p>
     * <pre>
     *   "1.2.840.113556.1.4.319" → "Simple Paged Results"
     *   "2.16.840.1.113730.3.4.2" → "ManageDsaIT"
     *   "9.9.9.9.9.9" → "" (not in catalog)
     * </pre>
     *
     * @param browserConnection  The connection whose Root DSE we're reading.
     * @param composite          The 2-column composite to add label pairs into.
     * @param attributeType      The Root DSE attribute name to read (e.g. "supportedControl").
     */
    private void addOidInfo( final IBrowserConnection browserConnection, Composite composite, String attributeType )
    {
        try
        {
            String[] values = browserConnection.getRootDSE().getAttribute( attributeType ).getStringValues();
            for ( String value : values )
            {
                String description = Utils.getOidDescription( value );
                if ( description == null )
                {
                    description = StringUtils.EMPTY;
                }
                BaseWidgetUtils.createLabeledText( composite, value, 1, 15 );
                BaseWidgetUtils.createLabeledText( composite, description, 1, 15 );
            }
        }
        catch ( Exception e )
        {
        }
    }


    // ── LUKE READS A LABELED ATTRIBUTE FROM THE HORIZON ───────────────────────
    // For the Info tab, each piece of data has a human-readable label (like
    // "Vendor Name:") alongside the attribute's value(s).  Luke reads the attribute,
    // joins multiple values with line separators, and places the result next to
    // the label.  If the attribute is absent, he writes a dash.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads all values of the given Root DSE attribute, joins them with line
     * separators, and adds a label+text pair to the composite.
     * If the attribute is absent or throws, displays {@code "-"} in the text field.
     *
     * <p>For example — Luke reads the vendor name from the horizon:</p>
     * <pre>
     *   "vendorName" = ["Apache Software Foundation"] →
     *     label "Vendor Name:" | text "Apache Software Foundation"
     *   "vendorName" absent →
     *     label "Vendor Name:" | text "-"
     * </pre>
     *
     * @param browserConnection  The connection whose Root DSE to read.
     * @param composite          The 2-column composite to add the label+text pair into.
     * @param attributeType      The Root DSE attribute name to read.
     * @param labelName          The human-readable label to display on the left.
     */
    private void addInfo( final IBrowserConnection browserConnection, Composite composite, String attributeType,
        String labelName )
    {
        StringBuffer sb = new StringBuffer();
        try
        {
            String[] values = browserConnection.getRootDSE().getAttribute( attributeType ).getStringValues();
            boolean isFirst = true;
            for ( String value : values )
            {
                if ( !isFirst )
                {
                    sb.append( BrowserCoreConstants.LINE_SEPARATOR );
                }
                sb.append( value );
                isFirst = false;
            }
        }
        catch ( Exception e )
        {
            sb.append( Messages.getString( "RootDSEPropertyPage.Dash" ) ); //$NON-NLS-1$
        }

        BaseWidgetUtils.createLabel( composite, labelName, 1 );
        BaseWidgetUtils.createWrappedLabeledText( composite, sb.toString(), 1, 150 );
    }

}
