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

package org.apache.directory.studio.connection.ui.properties;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.apache.directory.studio.connection.core.PasswordsKeyStoreManager;
import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.connection.core.jobs.CloseConnectionsRunnable;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionJob;
import org.apache.directory.studio.connection.ui.ConnectionParameterPage;
import org.apache.directory.studio.connection.ui.ConnectionParameterPageManager;
import org.apache.directory.studio.connection.ui.ConnectionParameterPageModifyListener;
import org.apache.directory.studio.connection.ui.ConnectionUIConstants;
import org.apache.directory.studio.connection.ui.PasswordsKeyStoreManagerUtils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;


// ── CLASS: ConnectionPropertyPage — EDITING THE SHIP'S MANIFEST AT THE REBEL BASE ─
// When a Rebel commander wants to update the specs of one of their ships — change the
// hyperdrive coordinates, update the pilot credentials, tweak the sensor calibration —
// they open the ship's properties page.  Each category of settings lives in its own
// tab.  This class is that properties page: it wraps all registered
// ConnectionParameterPage implementations into a tabbed JFace PropertyPage.
//
// On OK, we collect the new parameters, update the live Connection object, and if
// anything requiring a reconnection changed (e.g. the hostname), we close the
// connection so it will re-open cleanly next time.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link PropertyPage} that shows and edits the parameters of a
 * {@link Connection}.
 *
 * <p>The page is a tab folder.  Each tab corresponds to one
 * {@link ConnectionParameterPage} registered via the extension point
 * {@code org.apache.directory.studio.connection.ui.connectionParameterPages}.
 * Typical tabs: "Network Parameter", "Authentication", "Browser Options".</p>
 *
 * <p>Layout sketch:</p>
 * <pre>
 *  +----------------------------------------------------------------------+
 *  | Connection                                                   <- -> v |
 *  +----------------------------------------------------------------------+
 *  |       .-------------------.--------------.---.                       |
 *  | .-----| Network Parameter | Authentication |  |---.                  |
 *  | |     `-------------------'--------------'---'   |                  |
 *  | |                                                 |                  |
 *  | ...                                               ...                |
 *  | `---------------------------------------------------'                |
 *  +----------------------------------------------------------------------+
 * </pre>
 *
 * <p>If the passwords keystore is enabled but not yet unlocked, the page
 * prompts the user to unlock it.  If unlocking fails the page renders a plain
 * "keystore required" label instead of the tab folder.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionPropertyPage extends PropertyPage implements ConnectionParameterPageModifyListener
{
    // ── FIELDS ────────────────────────────────────────────────────────────────────

    /** Tab folder containing one tab per {@link ConnectionParameterPage}. */
    private TabFolder tabFolder;

    /**
     * The ordered array of parameter pages loaded from the extension registry.
     * Mirrors the tabs in {@link #tabFolder}.
     */
    private ConnectionParameterPage[] pages;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link ConnectionPropertyPage}.
     *
     * <p>Calls {@link #noDefaultAndApplyButton()} to remove the standard
     * "Restore Defaults" / "Apply" buttons — saves happen only on OK.</p>
     */
    public ConnectionPropertyPage()
    {
        super();
        super.noDefaultAndApplyButton();
    }


    // ── CONNECTION PARAMETER PAGE MODIFIED ────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Called by each parameter page whenever the user changes a value.
     * We forward to {@link #validate()} to update the OK button state and
     * error/warning messages.</p>
     */
    public void connectionParameterPageModified()
    {
        validate();
    }


    // ── GET TEST CONNECTION PARAMETERS ────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Collects a {@link ConnectionParameter} snapshot from all pages — used
     * by the "Check Network Parameter" test button to open a transient connection
     * without saving the changes first.</p>
     *
     * @return A new {@link ConnectionParameter} built from the current field values.
     */
    public ConnectionParameter getTestConnectionParameters()
    {
        ConnectionParameter connectionParameter = new ConnectionParameter();

        for ( ConnectionParameterPage page : pages )
        {
            page.saveParameters( connectionParameter );
        }

        return connectionParameter;
    }


    // ── SET MESSAGE ───────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Overridden to always use the WARNING severity level so messages appear
     * in the standard yellow-triangle area of a {@link PropertyPage}.</p>
     */
    @Override
    public void setMessage( String message )
    {
        super.setMessage( message, PropertyPage.WARNING );
    }


    // ── VALIDATE ──────────────────────────────────────────────────────────────────
    /**
     * Reads the message and valid state from the currently selected tab's
     * {@link ConnectionParameterPage} and propagates them to the property page
     * header and OK button.
     *
     * <p>If no tab is selected we walk all pages and stop at the first error.</p>
     */
    private void validate()
    {
        int index = tabFolder.getSelectionIndex();

        if ( index >= 0 )
        {
            // ── VALIDATE SELECTED TAB ONLY ────────────────────────────────────────
            ConnectionParameterPage page = pages[tabFolder.getSelectionIndex()];

            if ( page.getMessage() != null )
            {
                setMessage( page.getMessage() );
            }
            else if ( page.getInfoMessage() != null )
            {
                setMessage( page.getInfoMessage() );
            }
            else
            {
                setMessage( null );
            }

            if ( page.getErrorMessage() != null )
            {
                setErrorMessage( page.getErrorMessage() );
            }

            setValid( page.isValid() );
        }
        else
        {
            // ── NO SELECTION: check all pages ─────────────────────────────────────
            for ( ConnectionParameterPage page : pages )
            {
                if ( page.getMessage() != null )
                {
                    setMessage( page.getMessage() );
                }
                else if ( page.getInfoMessage() != null )
                {
                    setMessage( page.getInfoMessage() );
                }
                else
                {
                    setMessage( null );
                }

                if ( page.getErrorMessage() != null )
                {
                    setErrorMessage( page.getErrorMessage() );
                    setValid( page.isValid() );
                    return;
                }
            }

            setMessage( null );
            setErrorMessage( null );
            setValid( true );
        }
    }


    // ── GET CONNECTION ────────────────────────────────────────────────────────────
    /**
     * Adapts the given workbench element to a {@link Connection}.
     *
     * <p>The workbench passes us an {@link IAdaptable} object representing the
     * selected tree node.  We ask it to adapt itself to {@code Connection.class}.</p>
     *
     * @param element The selected workbench element.
     * @return The adapted {@link Connection}, or {@code null} if the element is
     *         not adaptable to a connection.
     */
    static Connection getConnection( Object element )
    {
        Connection connection = null;

        if ( element instanceof IAdaptable )
        {
            connection = ( ( IAdaptable ) element ).getAdapter( Connection.class );
        }

        return connection;
    }


    // ── CREATE CONTENTS ───────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Builds the tab folder.  Before creating tabs:</p>
     * <ol>
     *   <li>If the passwords keystore is enabled but not loaded, calls
     *       {@link PasswordsKeyStoreManagerUtils#askUserToLoadKeystore()}.  If
     *       the user cancels, renders a plain "keystore required" label.</li>
     *   <li>Adapts the page element to a {@link Connection}.  If no connection is
     *       found, renders a "no connection" label.</li>
     * </ol>
     */
    protected Control createContents( Composite parent )
    {
        PlatformUI.getWorkbench().getHelpSystem().setHelp( parent,
            ConnectionUIConstants.PLUGIN_ID + "." + "tools_connection_properties" ); //$NON-NLS-1$ //$NON-NLS-2$

        // ── KEYSTORE GATE ─────────────────────────────────────────────────────────
        // If the keystore is enabled we need it to be unlocked before we can show
        // password-related fields.
        // ──────────────────────────────────────────────────────────────────────────
        if ( PasswordsKeyStoreManagerUtils.isPasswordsKeystoreEnabled() )
        {
            PasswordsKeyStoreManager passwordsKeyStoreManager = ConnectionCorePlugin.getDefault()
                .getPasswordsKeyStoreManager();

            if ( !passwordsKeyStoreManager.isLoaded() && !PasswordsKeyStoreManagerUtils.askUserToLoadKeystore() )
            {
                return BaseWidgetUtils
                    .createLabel(
                        parent,
                        Messages
                            .getString( "ConnectionPropertyPage.AccessToPasswordsKeystoreRequiredToViewProperties" ), 1 ); //$NON-NLS-1$
            }
        }

        // ── BUILD TAB FOLDER ──────────────────────────────────────────────────────
        Connection connection = getConnection( getElement() );

        if ( connection != null )
        {
            super.setMessage( Messages.getString( "ConnectionPropertyPage.Connection" ) //$NON-NLS-1$
                + Utils.shorten( connection.getName(), 30 ) );

            pages = ConnectionParameterPageManager.getConnectionParameterPages();

            tabFolder = new TabFolder( parent, SWT.TOP );

            TabItem[] tabs = new TabItem[pages.length];

            for ( int i = 0; i < pages.length; i++ )
            {
                // ── ONE TAB PER PARAMETER PAGE ────────────────────────────────────
                Composite composite = new Composite( tabFolder, SWT.NONE );
                GridLayout gl = new GridLayout( 1, false );
                composite.setLayout( gl );

                pages[i].init( composite, this, connection.getConnectionParameter() );

                tabs[i] = new TabItem( tabFolder, SWT.NONE );
                tabs[i].setText( pages[i].getPageName() );
                tabs[i].setControl( composite );
            }

            return tabFolder;
        }
        else
        {
            return BaseWidgetUtils.createLabel( parent,
                Messages.getString( "ConnectionPropertyPage.NoConnection" ), 1 ); //$NON-NLS-1$
        }
    }


    // ── PERFORM OK ────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Saves parameters from all pages into a fresh {@link ConnectionParameter},
     * updates the live {@link Connection}, and, if any page flagged a required
     * reconnection (e.g. the host or port changed), closes the connection so it
     * will be re-established on next use.</p>
     *
     * <p>If the keystore is enabled but not loaded, returns {@code true} without
     * saving — the page is in read-only mode anyway.</p>
     *
     * @return Always {@code true}.
     */
    public boolean performOk()
    {
        // ── KEYSTORE NOT LOADED: skip save ────────────────────────────────────────
        if ( PasswordsKeyStoreManagerUtils.isPasswordsKeystoreEnabled() )
        {
            if ( !ConnectionCorePlugin.getDefault().getPasswordsKeyStoreManager().isLoaded() )
            {
                return true;
            }
        }

        Connection connection = getConnection( getElement() );

        // ── COLLECT PARAMETERS FROM ALL PAGES ────────────────────────────────────
        boolean parametersModified = false;
        boolean reconnectionRequired = false;
        ConnectionParameter connectionParameter = new ConnectionParameter();
        connectionParameter.setId( connection.getConnectionParameter().getId() );

        for ( ConnectionParameterPage page : pages )
        {
            page.saveParameters( connectionParameter );
            page.saveDialogSettings();
            parametersModified |= page.areParametersModifed();
            reconnectionRequired |= page.isReconnectionRequired();
        }

        if ( parametersModified )
        {
            // ── APPLY NEW PARAMETERS ──────────────────────────────────────────────
            connection.setConnectionParameter( connectionParameter );

            if ( reconnectionRequired )
            {
                // ── CLOSE SO IT RE-OPENS WITH THE NEW SETTINGS ────────────────────
                new StudioConnectionJob( new CloseConnectionsRunnable( connection ) ).execute();
            }
        }

        return true;
    }
}
