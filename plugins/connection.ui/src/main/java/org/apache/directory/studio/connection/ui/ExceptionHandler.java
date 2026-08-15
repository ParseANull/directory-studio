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

package org.apache.directory.studio.connection.ui;


import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;


// ── CLASS: ExceptionHandler — HAN'S EMERGENCY ALARM KLAXON ───────────────────────
// When something goes wrong in the Falcon — a blaster overheating, a failed
// hyperdrive jump — the alarms go off so the crew knows.  ExceptionHandler is that
// alarm system for the connection.ui plugin.
// When something blows up (connection failure, credential error, etc.), code calls
// handleException() with an IStatus.  We need to show an Eclipse ErrorDialog, but
// those must run on the SWT UI thread.  If we're already on the UI thread, we show
// it synchronously.  If we're on a background LDAP job thread, we asyncExec it so
// the UI thread picks it up on its next event-loop pass.
// Either way, we also log the status to the Eclipse error log (via
// ConnectionCorePlugin) so the Platform Error view can show it.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Displays runtime errors to the user via an {@link ErrorDialog} and logs them
 * to the Eclipse error log.
 *
 * <p>If the caller is already on the SWT UI thread the dialog is shown synchronously.
 * If the caller is on a background thread it is queued via
 * {@link Display#asyncExec} so the UI thread shows it on the next event-loop pass.</p>
 *
 * <p>All errors are also forwarded to the {@link ConnectionCorePlugin} log so they
 * appear in the Eclipse Error Log view.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExceptionHandler
{
    // ── HANDLE EXCEPTION — PUBLIC ENTRY POINT ────────────────────────────────────
    /**
     * Displays the given status as an error dialog and writes it to the error log.
     * Thread-safe: may be called from any thread.
     *
     * @param status  The error status to display and log.
     */
    public void handleException( IStatus status )
    {
        display( null, status );
    }


    // ── DISPLAY — ROUTE TO THE UI THREAD ──────────────────────────────────────────
    // If we're on the SWT thread already, show the dialog directly.
    // If not, post it asynchronously so we don't deadlock a background thread.
    // Either way, log the status to the error log so nothing is lost.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Opens an {@link ErrorDialog} with the given message and status.
     * If called on the SWT UI thread, shows the dialog synchronously.
     * If called on a background thread, queues it via {@link Display#asyncExec}.
     * In both cases, also logs the status via {@link ConnectionCorePlugin#getLog()}.
     *
     * @param message  Optional override message (shown above the status detail),
     *                 or {@code null} to show the status message directly.
     * @param status   The error status to display and log.
     */
    private void display( final String message, final IStatus status )
    {
        if ( Thread.currentThread() == Display.getDefault().getThread() )
        {
            // ── ON THE UI THREAD — SHOW THE DIALOG DIRECTLY ───────────────────────
            ErrorDialog.openError( Display.getDefault().getActiveShell(),
                Messages.getString( "ExceptionHandler.Error" ), message, status ); //$NON-NLS-1$
        }
        else
        {
            // ── ON A BACKGROUND THREAD — POST ASYNCHRONOUSLY ──────────────────────
            Runnable runnable = () ->
                ErrorDialog.openError( Display.getDefault().getActiveShell(), Messages
                    .getString( "ExceptionHandler.Error" ), message, status ); //$NON-NLS-1$

            Display.getDefault().asyncExec( runnable );
        }

        ConnectionCorePlugin.getDefault().getLog().log( status );
    }
}
