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
package org.apache.directory.studio.ldapservers.properties;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapservers.LdapServersManager;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.model.LdapServerAdapterExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;


// ── CLASS: ServerPropertyPage — THE IMPERIAL DOSSIER ON THIS INSTALLATION ────────────────
// When an officer right-clicks a ship in the hangar manifest and selects "Properties", they
// get a dossier: ship name, type, vendor (manufacturer), and the docking bay number.
// This is that read-only dossier panel for an LDAP server: Name, Type, Vendor, Location — no
// changes allowed here, just inspecting the facts.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * The "Info" tab in a server's Properties dialog.
 * Shows the server's name, adapter type+version, vendor, and on-disk folder path.
 * All fields are read-only — this is the dossier, not the configuration page.
 * Think of it as the Imperial dossier page: you look, you don't edit.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ServerPropertyPage extends PropertyPage implements IWorkbenchPropertyPage
{
    // ── Opening The Dossier — No Default/Apply Buttons Needed ────────────────────────────────
    // A read-only dossier doesn't need a "Save" button — there's nothing to save.
    // We disable the default "Apply" and "Restore Defaults" buttons upfront.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the page and immediately suppresses the "Apply" and "Restore Defaults" buttons —
     * this is a read-only information page, so there's nothing to apply or reset.
     *
     * <p>For example — the dossier is stamped READ-ONLY before it leaves the archive:</p>
     * <pre>
     *   new ServerPropertyPage() → calls noDefaultAndApplyButton() → no Save button appears.
     * </pre>
     */
    public ServerPropertyPage()
    {
        super();
        super.noDefaultAndApplyButton();
    }


    // ── Laying Out The Dossier Fields ────────────────────────────────────────────────────────
    // The clerk lays out the dossier in a two-column grid: left column for the label ("Name:"),
    // right column for the value ("My ApacheDS Server").
    // Each field is a read-only text widget — the user can read and copy, but not edit.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the page's widget tree inside {@code parent}.
     * Lays out a 2-column grid: label on the left, read-only text on the right, for each of
     * Name, Type, Vendor, and Location.
     * Pulls the values from the {@link LdapServer} returned by {@link #getElement()}.
     *
     * @param parent  the parent composite provided by the Properties dialog framework
     * @return the top-level control created here (the composite)
     */
    protected Control createContents( Composite parent )
    {
        // Composite
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 2, 1 );

        // Name
        BaseWidgetUtils.createLabel( composite, Messages.getString( "ServerPropertyPage.Name" ), 1 ); //$NON-NLS-1$
        Text nameText = BaseWidgetUtils.createLabeledText( composite, "", 1 ); //$NON-NLS-1$

        // Type
        BaseWidgetUtils.createLabel( composite, Messages.getString( "ServerPropertyPage.Type" ), 1 ); //$NON-NLS-1$
        Text typeText = BaseWidgetUtils.createLabeledText( composite, "", 1 ); //$NON-NLS-1$

        // Vendor
        BaseWidgetUtils.createLabel( composite, Messages.getString( "ServerPropertyPage.Vendor" ), 1 ); //$NON-NLS-1$
        Text vendorText = BaseWidgetUtils.createLabeledText( composite, "", 1 ); //$NON-NLS-1$

        // Location
        Label locationLabel = BaseWidgetUtils.createLabel( composite,
            Messages.getString( "ServerPropertyPage.Location" ), 1 ); //$NON-NLS-1$
        locationLabel.setLayoutData( new GridData( SWT.NONE, SWT.TOP, false, false ) );
        Text locationText = BaseWidgetUtils.createWrappedLabeledText( composite, "", 1 ); //$NON-NLS-1$
        GridData gd = new GridData( SWT.FILL, SWT.NONE, true, false );
        gd.widthHint = 300;
        locationText.setLayoutData( gd );

        // Getting the server
        LdapServer server = ( LdapServer ) getElement();
        if ( server != null )
        {
            LdapServerAdapterExtension ldapServerAdapterExtension = server.getLdapServerAdapterExtension();

            nameText.setText( server.getName() );
            typeText.setText( ldapServerAdapterExtension.getName() + " " + ldapServerAdapterExtension.getVersion() ); //$NON-NLS-1$
            vendorText.setText( ldapServerAdapterExtension.getVendor() );
            locationText.setText( LdapServersManager.getServersFolder().append( server.getId() ).toOSString() );
        }

        return parent;
    }
}
