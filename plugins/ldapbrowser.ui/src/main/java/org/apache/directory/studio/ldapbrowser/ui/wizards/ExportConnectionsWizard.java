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

package org.apache.directory.studio.ldapbrowser.ui.wizards;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.apache.directory.studio.connection.core.io.ConnectionIO;
import org.apache.directory.studio.ldapbrowser.core.BrowserConnectionIO;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: ExportConnectionsWizard — HAN JUMPS TO HYPERSPACE ─────────────────
// Han Solo punches the Millennium Falcon's coordinates into the navicomputer
// and blasts out of the system — everything important is packaged into the
// hyperdrive motivator and beamed into the void. This wizard does the same:
// it packages all connections, their folder structure, and their browser-layer
// settings into a single ZIP archive that can be shared or moved to another
// installation.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Wizard that exports all connection definitions to a portable ZIP archive.
 * The archive contains three XML entries: connections.xml (the connection
 * parameters), connectionFolders.xml (the folder hierarchy), and
 * browserconnections.xml (the browser-layer settings like bookmarks and searches).
 * Think of Han packaging the navicomputer coordinates into the Falcon's hyperdrive:
 * everything needed to reconstruct the connection setup is bundled together.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportConnectionsWizard extends ExportBaseWizard
{
    /** The wizard page */
    private ExportConnectionsWizardPage page;


    // ── Han Preps the Navicomputer ────────────────────────────────────────────────
    // Han loads the export wizard title — it announces the destination archive
    // to the user before the jump to hyperspace.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportConnectionsWizard with the localised window title.
     */
    public ExportConnectionsWizard()
    {
        super( Messages.getString( "ExportConnectionsWizard.ConnectionsExport" ) ); //$NON-NLS-1$
    }


    // ── Han Checks the Jump Coordinates ──────────────────────────────────────────
    // The hyperspace jump has a registry ID so other parts of the system can
    // open this wizard by name.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse wizard ID for the export connections wizard.
     * Used by actions that open this wizard programmatically.
     *
     * @return  the wizard ID string from {@link BrowserUIConstants}.
     */
    public static String getId()
    {
        return BrowserUIConstants.WIZARD_EXPORT_CONNECTIONS;
    }


    // ── Han Opens the Cargo Bay ───────────────────────────────────────────────────
    // The single page asks where to save the archive — destination file only,
    // no complex source selection since we always export all connections.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Adds the single ExportConnectionsWizardPage for picking the destination file.
     */
    public void addPages()
    {
        page = new ExportConnectionsWizardPage();
        addPage( page );
    }


    // ── Han Wires the Help System ─────────────────────────────────────────────────
    // The help button should lead somewhere useful when pressed mid-wizard.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Creates page controls and registers the help context ID (currently not set —
     * a TODO comment in the original code marks it for future addition).
     *
     * @param pageContainer  the wizard page container.
     */
    public void createPageControls( Composite pageContainer )
    {
        super.createPageControls( pageContainer );

        // set help context ID
        //        PlatformUI.getWorkbench().getHelpSystem().setHelp( fromPage.getControl(),
        //            BrowserUIPlugin.PLUGIN_ID + "." + "tools_ldifexport_wizard" );
        //TODO: Add Help Context
    }


    // ── Han Punches It ───────────────────────────────────────────────────────────
    // The coordinates are loaded, the Falcon is ready — Han punches the
    // hyperdrive lever and the data streams into the destination ZIP file.
    // We write three ZIP entries: connection parameters, folder structure,
    // and browser-layer settings.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Saves all connection data to a ZIP archive at the path chosen by the user.
     * Three entries are written in order:
     * <ol>
     *   <li>connections.xml — connection parameters for all connections.</li>
     *   <li>connectionFolders.xml — the folder hierarchy.</li>
     *   <li>browserconnections.xml — browser-layer data (bookmarks, searches).</li>
     * </ol>
     * Errors during ZIP writing are currently printed to stderr (TODO markers
     * in the original code indicate these should be surfaced to the user).
     *
     * @return  {@code true} always (errors are only logged, not reported to the wizard).
     */
    public boolean performFinish()
    {
        page.saveDialogSettings();

        String exportFileName = page.getExportFileName();

        try
        {
            // Creating the ZipOutputStream
            ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( new File( exportFileName ) ) );
            // Writing the Connections file.
            zos.putNextEntry( new ZipEntry( "connections.xml" ) ); //$NON-NLS-1$
            Connection[] connections = ConnectionCorePlugin.getDefault().getConnectionManager().getConnections();
            Set<ConnectionParameter> connectionParameters = new HashSet<ConnectionParameter>();
            for ( Connection connection : connections )
            {
                connectionParameters.add( connection.getConnectionParameter() );
            }
            ConnectionIO.save( connectionParameters, zos );
            zos.closeEntry();
            // Writing the Connection Folders file.
            zos.putNextEntry( new ZipEntry( "connectionFolders.xml" ) ); //$NON-NLS-1$
            ConnectionFolder[] connectionFolders = ConnectionCorePlugin.getDefault().getConnectionFolderManager()
                .getConnectionFolders();
            Set<ConnectionFolder> connectionFoldersSet = new HashSet<ConnectionFolder>();
            for ( ConnectionFolder connectionFolder : connectionFolders )
            {
                connectionFoldersSet.add( connectionFolder );
            }
            ConnectionIO.saveConnectionFolders( connectionFoldersSet, zos );
            zos.closeEntry();
            // Writing the Browser Connections file.
            zos.putNextEntry( new ZipEntry( "browserconnections.xml" ) ); //$NON-NLS-1$
            IBrowserConnection[] browserConnections = BrowserCorePlugin.getDefault().getConnectionManager()
                .getBrowserConnections();
            Map<String, IBrowserConnection> browserConnectionsMap = new HashMap<String, IBrowserConnection>();
            for ( IBrowserConnection browserConnection : browserConnections )
            {
                browserConnectionsMap.put( browserConnection.getConnection().getId(), browserConnection );
            }
            BrowserConnectionIO.save( zos, browserConnectionsMap );
            zos.closeEntry();
            // Closing the ZipOutputStream
            zos.close();
        }
        catch ( FileNotFoundException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return true;
    }
}
