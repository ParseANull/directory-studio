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

package org.apache.directory.studio.connection.ui.actions;


import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.dialogs.PreferencesUtil;


// ── CLASS: PropertiesAction — HAN OPENS THE FALCON'S ENGINEERING LOG ──────────────
// When the crew needs to update the Falcon's configuration — change the jump
// coordinates, update the auth codes, adjust the shield frequency — they open the
// engineering log.  PropertiesAction opens Eclipse's Properties dialog for the
// selected connection, pre-navigated to the connection property page.
// Only enabled when exactly one connection is selected.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Opens the Eclipse Properties dialog for the selected LDAP connection.
 *
 * <p>Uses {@link PreferencesUtil#createPropertyDialogOn} to open the property
 * dialog navigated to the connection property page ID from the plugin properties.
 * The dialog title is truncated to 30 characters using {@link Utils#shorten}.</p>
 *
 * <p>Only enabled when exactly one connection is selected.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PropertiesAction extends StudioAction
{
    // ── GET TEXT ──────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getText()
    {
        return Messages.getString( "PropertiesAction.Properties" ); //$NON-NLS-1$
    }


    // ── GET IMAGE DESCRIPTOR — NO ICON ────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns {@code null} — this action uses no icon.
     */
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── GET COMMAND ID — MAPS TO FILE > PROPERTIES ────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getCommandId()
    {
        return IWorkbenchCommandConstants.FILE_PROPERTIES;
    }


    // ── IS ENABLED — EXACTLY ONE CONNECTION SELECTED ──────────────────────────────
    /**
     * {@inheritDoc}
     * Returns {@code true} only when exactly one connection is selected.
     */
    public boolean isEnabled()
    {
        return getSelectedConnections().length == 1;
    }


    // ── RUN — OPEN THE PROPERTY DIALOG ────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Opens the connection property dialog for the selected connection,
     * pre-navigated to the connection property page.
     */
    public void run()
    {
        IAdaptable element = null;
        String pageId = null;
        String title = null;

        if ( getSelectedConnections().length == 1 )
        {
            element = getSelectedConnections()[0];
            pageId = ConnectionUIPlugin.getDefault().getPluginProperties()
                .getString( "Prop_ConnectionPropertyPage_id" ); //$NON-NLS-1$
            title = getSelectedConnections()[0].getName();
        }

        if ( element != null )
        {
            PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn( getShell(), element, pageId, null, null );

            if ( dialog != null )
            {
                title = Utils.shorten( title, 30 );
            }

            dialog.getShell().setText(
                NLS.bind( Messages.getString( "PropertiesAction.PropertiesFor" ), new String[] //$NON-NLS-1$
                { title } ) );
            dialog.open();
        }
    }
}
