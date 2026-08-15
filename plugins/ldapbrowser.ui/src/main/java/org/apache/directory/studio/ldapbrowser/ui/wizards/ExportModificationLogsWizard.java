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
import org.apache.directory.studio.connection.core.io.api.LdifModificationLogger;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


// ── CLASS: ExportModificationLogsWizard — THE IMPERIAL RECORDS ROOM OPENS ────
// The Empire keeps meticulous records of every change to every system —
// every modification, every delete, every add, stamped and filed in the
// Imperial archives. This wizard is the records archivist: it opens the
// modification-log files (stored in a rolling series of LDIF files ordered
// newest-first), reassembles them in chronological order (oldest-first),
// and streams the whole history to a single destination file.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * One-page wizard that exports the modification logs for the current connection
 * to a single LDIF file.
 * The modification log subsystem ({@link LdifModificationLogger}) stores LDAP
 * change operations in a rolling series of files ordered newest-to-oldest.
 * This wizard concatenates them in reverse (oldest-to-newest) into the user's
 * chosen destination file so the exported log reads in chronological order.
 * If the connection has no associated connection (i.e., it is not live),
 * the export is skipped silently.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportModificationLogsWizard extends ExportBaseWizard
{

    /** The to page, used to select the target file. */
    private ExportLogsToWizardPage toPage;


    // ── The Archivist Opens the Records Room ──────────────────────────────────────
    // The wizard title announces the modification-logs export operation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportModificationLogsWizard with the localised
     * "Export Modification Logs" window title.
     */
    public ExportModificationLogsWizard()
    {
        super( Messages.getString( "ExportModificationLogsWizard.ExportModificationLogs" ) ); //$NON-NLS-1$
    }


    // ── The Archivist Opens the Filing Cabinet ────────────────────────────────────
    // Only one page needed: just the destination file picker (no source-search
    // configuration because we always export all modification logs for the
    // current connection).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Adds the single {@link ExportLogsToWizardPage} for picking the destination file.
     * There is no From page because the source is always the full modification-log
     * history of the wizard's current connection.
     */
    public void addPages()
    {
        toPage = new ExportLogsToWizardPage( ExportLogsToWizardPage.class.getName(), this );
        addPage( toPage );
    }


    // ── The Archivist Assembles the Record ────────────────────────────────────────
    // The log files are stored newest-first (file[0] is the most recent).
    // We iterate backwards from the last file to file[0] so the exported LDIF
    // is in chronological order (oldest entry first). Each log file that exists
    // and is readable is streamed into the output file.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Saves dialog settings, then assembles all modification log files for the
     * current connection's underlying connection into a single LDIF file.
     * Iterates from the last (oldest) log file to the first (newest) so the
     * output is in chronological order. Skips null, missing, or unreadable files.
     * Reports IO errors to the user via the Eclipse exception handler.
     * Does nothing if the browser connection has no associated live connection.
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

                LdifModificationLogger modificationLogger = ConnectionCorePlugin.getDefault()
                    .getLdifModificationLogger();
                File[] files = modificationLogger.getFiles( search.getBrowserConnection().getConnection() );
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
                        .getString( "ExportModificationLogsWizard.CantExportModificationLogs" ), e ) ); //$NON-NLS-1$
            }
        }

        return true;
    }

}
