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
package org.apache.directory.studio.ldapservers.actions;


import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;


// ── CLASS: CreateConnectionActionHelper — HAN'S BACK-CHANNEL DEAL AT CLOUD CITY ─────────────
// Han Solo is deep in Cloud City's comm tunnels, patching a covert line so the Rebellion can
// talk to the network without going through any Imperial checkpoint.  He registers the new
// connection, drops it into the root folder, and — if the operator isn't already watching the
// right screen — nudges them to flip over to the LDAP Browser perspective.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Utility helper that does the heavy lifting when we create an LDAP Browser connection
 * from a running server definition.  The actual "Create Connection" action calls us
 * once it has a {@link Connection} object ready to go; we register it, park it in the
 * root folder, and prompt the user to switch to the LDAP Browser perspective if they
 * aren't there already.
 * Think of this class as Han Solo making a back-channel deal at Cloud City — he gets
 * the Rebellion patched into the right comm channel and then asks "do you want to flip
 * over to that frequency, or stay where you are?"
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CreateConnectionActionHelper
{
    // ── HAN PATCHES THE CONNECTION INTO LEIA'S COMMS ─────────────────────────────────────────
    // Han is hunched over a Cloud City terminal, routing the new comm channel into the
    // Rebellion's active frequency list so everyone can reach it.
    // He checks which screen Leia is already watching and either tells her the channel name
    // or asks if she wants to switch to the right display.
    // We do the same here: register the connection, figure out the current perspective, and
    // either announce "connection created" or offer to switch to the LDAP Browser view.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Registers a new LDAP Browser connection and notifies the user that it exists.
     * We add the connection to both the connection manager and the root connection folder,
     * then check whether we're already in the LDAP Browser perspective.  If we are, we
     * just tell the user the connection name.  If we're not, we ask whether they want
     * to switch over.
     *
     * <p>For example — Han finishes the patch and calls over to Leia:</p>
     * <pre>
     *   han.registerChannel( connection );       // added to the fleet comms list
     *   if ( leia.isWatchingLdapScreen() ) {
     *       leia.notify( "New channel: " + connection.getName() );
     *   } else {
     *       leia.ask( "Switch to LDAP screen? Yes / No" );
     *   }
     * </pre>
     *
     * @param server      The {@link LdapServer} that the connection was created from — we use
     *                    it indirectly through the perspective and dialog context.
     * @param connection  The fully-built {@link Connection} object ready to be registered —
     *                    Han already negotiated the deal; this is the signed agreement.
     */
    public static void createLdapBrowserConnection( LdapServer server, Connection connection )
    {
        // Adding the connection to the connection manager
        ConnectionCorePlugin.getDefault().getConnectionManager().addConnection( connection );

        // Adding the connection to the root connection folder
        ConnectionCorePlugin.getDefault().getConnectionFolderManager().getRootConnectionFolder()
            .addConnectionId( connection.getId() );

        // Getting the window, LDAP perspective and current perspective
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IPerspectiveDescriptor ldapPerspective = getLdapPerspective();
        IPerspectiveDescriptor currentPerspective = window.getActivePage().getPerspective();

        // Checking if we are already in the LDAP perspective
        if ( ( ldapPerspective != null ) && ( ldapPerspective.equals( currentPerspective ) ) )
        {
            // As we're already in the LDAP perspective, we only indicate to the user
            // the name of the connection that has been created
            MessageDialog dialog = new MessageDialog(
                window.getShell(),
                Messages.getString( "CreateConnectionActionHelper.ConnectionCreated" ), null, //$NON-NLS-1$
                NLS.bind(
                    Messages.getString( "CreateConnectionActionHelper.ConnectionCalledCreated" ), new String[] { connection.getName() } ), MessageDialog.INFORMATION, //$NON-NLS-1$
                new String[]
                    { IDialogConstants.OK_LABEL }, MessageDialog.OK );
            dialog.open();
        }
        else
        {
            // We're not already in the LDAP perspective, we indicate to the user
            // the name of the connection that has been created and we ask him
            // if we wants to switch to the LDAP perspective
            MessageDialog dialog = new MessageDialog(
                window.getShell(),
                Messages.getString( "CreateConnectionActionHelper.ConnectionCreated" ), null, //$NON-NLS-1$
                NLS.bind(
                    Messages.getString( "CreateConnectionActionHelper.ConnectionCalledCreatedSwitch" ), new String[] { connection.getName() } ), //$NON-NLS-1$
                MessageDialog.INFORMATION, new String[]
                    { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, MessageDialog.OK );
            if ( dialog.open() == MessageDialog.OK )
            {
                // Switching to the LDAP perspective
                window.getActivePage().setPerspective( ldapPerspective );
            }
        }
    }


    // ── HAN SCANS CLOUD CITY FOR THE RIGHT FREQUENCY ─────────────────────────────────────────
    // Han flicks through Cloud City's comm registry looking for the channel ID that matches
    // the Rebellion's secure LDAP Browser frequency.
    // He checks each registered perspective in turn until he finds the one with the right ID.
    // We mirror that here: we walk the workbench's perspective registry and return the one
    // whose ID matches the LDAP Browser perspective.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Finds and returns the LDAP Browser perspective descriptor from Eclipse's perspective
     * registry.  We need this so we can tell whether the user is already there and, if not,
     * switch them over when they say yes.
     *
     * <p>For example — Han scans the frequency list:</p>
     * <pre>
     *   for ( channel : cloudCityComms.getAllFrequencies() ) {
     *       if ( channel.getId().equalsIgnoreCase( REBEL_LDAP_FREQUENCY ) ) {
     *           return channel;   // found it — this is the one
     *       }
     *   }
     *   return null;   // not in the registry; LDAP Browser isn't installed
     * </pre>
     *
     * @return  The {@link IPerspectiveDescriptor} for the LDAP Browser perspective, or
     *          {@code null} if the perspective isn't registered (e.g., plugin not installed).
     */
    private static IPerspectiveDescriptor getLdapPerspective()
    {
        for ( IPerspectiveDescriptor perspective : PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives() )
        {
            if ( "org.apache.directory.studio.ldapbrowser.ui.perspective.BrowserPerspective" //$NON-NLS-1$
                .equalsIgnoreCase( perspective.getId() ) )
            {
                return perspective;
            }
        }

        return null;
    }


    // ── HAN CHECKS EVERY PIECE OF GEAR BEFORE COMMITTING ─────────────────────────────────────
    // Before Han commits to the jump to hyperspace he walks the Falcon's checklist —
    // hyperdrive motivator, nav computer, comm array — refusing to leave until every
    // system is confirmed active.
    // Here we walk the list of required LDAP Browser OSGi bundles and return false the
    // moment we find one that is missing or uninstalled.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether all the LDAP Browser-related OSGi plugins are present and active.
     * We need several bundles to be installed and running before we can offer the
     * "Create Connection" option — if any one of them is missing, the feature won't work
     * and we shouldn't even show the button.
     *
     * <p>For example — Han runs through the Falcon's pre-flight list:</p>
     * <pre>
     *   String[] requiredSystems = { "hyperdrive", "navComputer", "commArray", ... };
     *   for ( String system : requiredSystems ) {
     *       if ( falcon.getSystem( system ) == null || !falcon.getSystem( system ).isOnline() ) {
     *           return false;   // can't fly without this
     *       }
     *   }
     *   return true;   // all systems go
     * </pre>
     *
     * @return  {@code true} if every required bundle is installed and not in the
     *          {@code UNINSTALLED} state; {@code false} if any one of them is missing.
     */
    public static boolean isLdapBrowserPluginsAvailable()
    {
        String[] bundleNames = new String[]
            {
                "org.apache.directory.studio.connection.core",          // Connection Core Plugin
                "org.apache.directory.studio.connection.ui",            // Connection UI Plugin
                "org.apache.directory.studio.ldapbrowser.common",       // LDAP Browser Common Plugin
                "org.apache.directory.studio.ldapbrowser.core",         // LDAP Browser Core Plugin
                "org.apache.directory.studio.ldapbrowser.ui",           // LDAP Browser UI Plugin
                "org.apache.directory.studio.ldifeditor",               // LDIF Editor Plugin
                "org.apache.directory.studio.ldifparser"                // LDIF Parser Plugin
            };

        for ( String bundleName : bundleNames )
        {
            Bundle bundle = Platform.getBundle( bundleName );

            if ( ( bundle == null ) || ( bundle.getState() == Bundle.UNINSTALLED ) )
            {
                return false;
            }
        }

        return true;
    }
}
