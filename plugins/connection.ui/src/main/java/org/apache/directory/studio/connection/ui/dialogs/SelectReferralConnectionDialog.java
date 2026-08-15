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


import java.util.List;

import org.apache.directory.api.ldap.model.exception.LdapURLEncodingException;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.connection.ui.widgets.ConnectionActionGroup;
import org.apache.directory.studio.connection.ui.widgets.ConnectionConfiguration;
import org.apache.directory.studio.connection.ui.widgets.ConnectionUniversalListener;
import org.apache.directory.studio.connection.ui.widgets.ConnectionWidget;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: SelectReferralConnectionDialog — C-3PO ROUTES THE REDIRECT ────────────
// When the LDAP server sends back a referral ("this entry is over there — try
// ldap://other-server/dc=example,dc=com"), we need to follow it using one of our
// known connections.  C-3PO would consult his diplomatic database to figure out
// which ship to route the message through; this dialog does the same.
//
// The dialog shows the referral URL(s), then presents the full ConnectionWidget
// (the same tree of connections the main view uses) so the user can pick which
// connection to use to chase the referral.  If there is already a connection whose
// URL normalises to the same value as one of the referral URLs, we pre-select it.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Dialog that asks the user to select an existing {@link Connection} to use for
 * following an LDAP referral.
 *
 * <p>Displays the referral URL(s) received from the server, then renders a full
 * {@link ConnectionWidget} so the user can pick from their existing connections.
 * Any connection whose normalised URL matches a referral URL is pre-selected.</p>
 *
 * <p>After the dialog closes, the selected connection is available via
 * {@link #getReferralConnection()}.  Returns {@code null} if cancelled.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SelectReferralConnectionDialog extends Dialog
{
    // ── FIELDS ────────────────────────────────────────────────────────────────────

    /** The window title. */
    private String title;

    /** The referral URLs received from the server. */
    private List<String> referralUrls;

    /** The connection the user selected; {@code null} until the user picks one. */
    private Connection selectedConnection;

    /** Configuration (content/label provider, context menu) for the connection widget. */
    private ConnectionConfiguration configuration;

    /** Listens for connection events and refreshes the widget tree. */
    private ConnectionUniversalListener universalListener;

    /** Action group wired into the connection widget toolbar and context menu. */
    private ConnectionActionGroup actionGroup;

    /** The embedded connection tree widget. */
    private ConnectionWidget mainWidget;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link SelectReferralConnectionDialog}.
     *
     * @param parentShell  The parent SWT shell.
     * @param referralUrls The list of referral URL strings received from the server.
     */
    public SelectReferralConnectionDialog( Shell parentShell, List<String> referralUrls )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.title = Messages.getString( "SelectReferralConnectionDialog.SelectReferralConenction" ); //$NON-NLS-1$
        this.referralUrls = referralUrls;
        this.selectedConnection = null;
    }


    // ── CONFIGURE SHELL ───────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Sets the shell title to the localised "Select Referral Connection" string.
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( title );
    }


    // ── CLOSE ─────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Cleans up all resources held by the embedded connection widget before
     * delegating to the superclass close.
     */
    @Override
    public boolean close()
    {
        // ── DISPOSE WIDGET RESOURCES ──────────────────────────────────────────────
        // The connection widget allocates action handlers and listeners that need
        // to be explicitly released; we must do this before the shell is disposed.
        // ──────────────────────────────────────────────────────────────────────────
        if ( mainWidget != null )
        {
            configuration.dispose();
            configuration = null;
            actionGroup.deactivateGlobalActionHandlers();
            actionGroup.dispose();
            actionGroup = null;
            universalListener.dispose();
            universalListener = null;
            mainWidget.dispose();
            mainWidget = null;
        }

        return super.close();
    }


    // ── CANCEL PRESSED ────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Clears the selected connection so {@link #getReferralConnection()} returns
     * {@code null} when the user cancels.
     */
    @Override
    protected void cancelPressed()
    {
        selectedConnection = null;
        super.cancelPressed();
    }


    // ── CREATE BUTTONS FOR BUTTON BAR ─────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Creates OK (focused) and Cancel buttons, then validates so OK starts
     * disabled until a connection is selected.
     */
    @Override
    protected void createButtonsForButtonBar( Composite parent )
    {
        Button okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        okButton.setFocus();
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );

        validate();
    }


    // ── VALIDATE ──────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the OK button based on whether a connection is selected.
     *
     * <p>Called whenever the viewer selection changes.</p>
     */
    private void validate()
    {
        if ( getButton( IDialogConstants.OK_ID ) != null )
        {
            getButton( IDialogConstants.OK_ID ).setEnabled( selectedConnection != null );
        }
    }


    // ── CREATE DIALOG AREA ────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the dialog body:
     * <ol>
     *   <li>A label explaining the referral and listing the referral URL(s).</li>
     *   <li>A full {@link ConnectionWidget} showing all connections.</li>
     * </ol>
     *
     * <p>If any referral URL normalises to an existing connection's URL, that
     * connection is pre-selected in the viewer.</p>
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridLayout gridLayout = new GridLayout();
        composite.setLayout( gridLayout );
        GridData gridData = new GridData( GridData.FILL_BOTH );
        gridData.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        gridData.heightHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH / 2 );
        composite.setLayoutData( gridData );

        // ── HEADER: PROMPT + REFERRAL URL LIST ────────────────────────────────────
        BaseWidgetUtils.createWrappedLabeledText( composite, Messages
            .getString( "SelectReferralConnectionDialog.SelectConnectionToHandleReferral" ), 1 ); //$NON-NLS-1$

        for ( String url : referralUrls )
        {
            BaseWidgetUtils.createWrappedLabeledText( composite, " - " + url, 1 ); //$NON-NLS-1$
        }

        // ── CONNECTION WIDGET ─────────────────────────────────────────────────────
        // We instantiate a full ConnectionWidget (the same one used by the main
        // Connections view) and wire it up with actions and listeners.
        // ──────────────────────────────────────────────────────────────────────────
        configuration = new ConnectionConfiguration();

        mainWidget = new ConnectionWidget( configuration, null );
        mainWidget.createWidget( composite );
        mainWidget.setInput( ConnectionCorePlugin.getDefault().getConnectionFolderManager() );

        actionGroup = new ConnectionActionGroup( mainWidget, configuration );
        actionGroup.fillToolBar( mainWidget.getToolBarManager() );
        actionGroup.fillMenu( mainWidget.getMenuManager() );
        actionGroup.fillContextMenu( mainWidget.getContextMenuManager() );
        actionGroup.activateGlobalActionHandlers();

        universalListener = new ConnectionUniversalListener( mainWidget.getViewer() );

        // ── SELECTION CHANGED LISTENER ────────────────────────────────────────────
        // Update selectedConnection whenever the user clicks a row, then re-validate
        // so the OK button state is refreshed immediately.
        // ──────────────────────────────────────────────────────────────────────────
        mainWidget.getViewer().addSelectionChangedListener( event ->
            {
                selectedConnection = null;

                if ( !event.getSelection().isEmpty() )
                {
                    Object object = ( ( IStructuredSelection ) event.getSelection() ).getFirstElement();

                    if ( object instanceof Connection )
                    {
                        selectedConnection = ( Connection ) object;
                    }
                }

                validate();
            } );

        // ── DOUBLE-CLICK LISTENER ─────────────────────────────────────────────────
        // Double-clicking a connection also captures the selection (but doesn't
        // automatically close the dialog — that requires an explicit OK click).
        // ──────────────────────────────────────────────────────────────────────────
        mainWidget.getViewer().addDoubleClickListener( event ->
            {
                selectedConnection = null;

                if ( !event.getSelection().isEmpty() )
                {
                    Object object = ( ( IStructuredSelection ) event.getSelection() ).getFirstElement();

                    if ( object instanceof Connection )
                    {
                        selectedConnection = ( Connection ) object;
                    }
                }

                validate();
            } );

        // ── PRE-SELECT MATCHING CONNECTION ────────────────────────────────────────
        // Walk all known connections; if any normalises to the same URL as one of
        // the referral URLs, reveal and pre-select it in the viewer.
        // ──────────────────────────────────────────────────────────────────────────
        if ( referralUrls != null )
        {
            Connection[] connections = ConnectionCorePlugin.getDefault().getConnectionManager().getConnections();

            for ( Connection connection : connections )
            {
                LdapUrl connectionUrl = connection.getUrl();
                String normalizedConnectionUrl = Utils.getSimpleNormalizedUrl( connectionUrl );

                for ( String url : referralUrls )
                {
                    try
                    {
                        if ( ( url != null ) &&
                             Utils.getSimpleNormalizedUrl( new LdapUrl( url ) ).equals( normalizedConnectionUrl ) )
                        {
                            mainWidget.getViewer().reveal( connection );
                            mainWidget.getViewer().setSelection( new StructuredSelection( connection ), true );
                            break;
                        }
                    }
                    catch ( LdapURLEncodingException e )
                    {
                        // Malformed URL in the referral — silently skip it.
                    }
                }
            }
        }

        applyDialogFont( composite );
        validate();

        return composite;
    }


    // ── GET REFERRAL CONNECTION ───────────────────────────────────────────────────
    /**
     * Returns the connection the user selected to follow the referral.
     *
     * <p>Returns {@code null} if the dialog was cancelled or if the user clicked
     * OK without selecting a connection (the OK button should prevent that, but
     * callers should defend against {@code null} regardless).</p>
     *
     * @return The selected {@link Connection}, or {@code null} if cancelled.
     */
    public Connection getReferralConnection()
    {
        return selectedConnection;
    }
}
