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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.directory.api.util.FileUtils;
import org.apache.directory.api.util.IOUtils;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.io.api.LdifSearchLogger;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


// ── CLASS: ExportSearchLogsWizard — R2-D2 DUMPS THE COMPUTER RECORDS ─────────
// R2-D2 plugs into the Death Star's computer and downloads the search records
// — every query the station received, in order. This wizard does the same:
// it reads the search-log rolling files (stored newest-first), reverses them
// into chronological order, and streams the whole thing to one LDIF file
// that can be analysed offline. It's the search-log twin of
// ExportModificationLogsWizard (which does the same for modification logs).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * One-page wizard that exports the LDAP search logs for the current connection
 * to a single LDIF file.
 * The search log subsystem ({@link LdifSearchLogger}) stores LDAP search
 * operations in a rolling series of files ordered newest-to-oldest.
 * This wizard concatenates them in reverse (oldest-to-newest) so the exported
 * log reads in chronological order. Mirrors
 * {@link ExportModificationLogsWizard} exactly but uses the search-log API.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportSearchLogsWizard extends ExportBaseWizard
{

    /** The to page, used to select the target file. */
    private ExportLogsToWizardPage toPage;


    // ── R2-D2 Boots Up the Download Sequence ─────────────────────────────────────
    // The wizard title announces the search-log export operation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportSearchLogsWizard with the localised
     * "Export Search Logs" window title.
     */
    public ExportSearchLogsWizard()
    {
        super( Messages.getString( "ExportSearchLogsWizard.ExportSearchLogs" ) ); //$NON-NLS-1$
    }


    // ── R2-D2 Opens the Interface Port ───────────────────────────────────────────
    // One page: just pick the destination file. No source-search configuration
    // is needed because we always export all search logs for the current connection.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Adds the single {@link ExportLogsToWizardPage} for picking the destination file.
     */
    public void addPages()
    {
        toPage = new ExportLogsToWizardPage( ExportLogsToWizardPage.class.getName(), this );
        addPage( toPage );
    }


    // ── R2-D2 Downloads the Records ───────────────────────────────────────────────
    // The search-log files are stored newest-first; we iterate backwards so the
    // exported LDIF reads oldest-to-newest (chronological order).
    // Each file is copied into the destination; missing or unreadable files are
    // silently skipped. If the connection is not live, we do nothing.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Saves dialog settings, then concatenates all search log files for the
     * current connection into a single LDIF file in chronological order.
     * Iterates from the last (oldest) log file to the first (newest), skipping
     * null, missing, or unreadable entries. Reports IO errors via the Eclipse
     * exception handler. Does nothing if the browser connection has no live connection.
     *
     * @return  {@code true} always.
     */
    public boolean performFinish()
    {
        toPage.saveDialogSettings();

        if ( search.getBrowserConnection().getConnection() != null )
        {
            try
            {
                File targetFile = new File( exportFilename );
                OutputStream os = FileUtils.openOutputStream( targetFile );

                LdifSearchLogger searchLogger = ConnectionCorePlugin.getDefault().getLdifSearchLogger();
                File[] files = searchLogger.getFiles( search.getBrowserConnection().getConnection() );
                // need to go backward through the files as the 1st file contains the newest entry
                for ( int i = files.length - 1; i >= 0; i-- )
                {
                    File file = files[i];
                    if ( file != null && file.exists() && file.canRead() )
                    {
                        InputStream is = FileUtils.openInputStream( file );
                        IOUtils.copy( is, os );
                        is.close();
                    }
                }
                os.close();
            }
            catch ( IOException e )
            {
                ConnectionUIPlugin.getDefault().getExceptionHandler().handleException(
                    new Status( IStatus.ERROR, BrowserCommonConstants.PLUGIN_ID, IStatus.ERROR, Messages
                        .getString( "ExportSearchLogsWizard.CantExportSearchLogs" ), e ) ); //$NON-NLS-1$
            }
        }

        return true;
    }

}
