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


import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: ConnectionFolderDialog — HAN NAMES THE CARGO BAY ──────────────────────
// When Han needs to create a new cargo bay on the Falcon — or rename an existing one
// — he types a name and the system checks it doesn't conflict with any existing bay.
// ConnectionFolderDialog is that interaction: a thin wrapper around JFace's
// InputDialog that lets the caller inject an IInputValidator for uniqueness checking.
// The actions package (NewConnectionFolderAction and RenameAction) construct the
// validator and pass it in when opening this dialog.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Simple input dialog for creating or renaming a connection folder.
 *
 * <p>Extends {@link InputDialog} without adding any additional behaviour — the
 * caller is responsible for providing the title, message, initial value, and an
 * {@link IInputValidator} that enforces naming constraints (e.g. uniqueness).</p>
 *
 * <p>Opened by {@code NewConnectionFolderAction} (create) and
 * {@code RenameAction} (rename).</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionFolderDialog extends InputDialog
{
    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link ConnectionFolderDialog}.
     *
     * <p>All parameters are forwarded directly to {@link InputDialog}.  The caller
     * supplies the validator — typically a lambda that checks whether the typed name
     * is already in use by another folder.</p>
     *
     * @param parentShell   The parent SWT shell.
     * @param dialogTitle   Title shown in the dialog shell and title bar.
     * @param dialogMessage Explanatory message shown above the text field.
     * @param initialValue  The text pre-filled into the input field (usually the
     *                      current folder name when renaming, empty when creating).
     * @param validator     Validates the input as the user types; return
     *                      {@code null} for valid, or an error message string
     *                      if the name is invalid.
     */
    public ConnectionFolderDialog( Shell parentShell, String dialogTitle, String dialogMessage, String initialValue,
        IInputValidator validator )
    {
        super( parentShell, dialogTitle, dialogMessage, initialValue, validator );
    }


    // ── CREATE DIALOG AREA ────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Delegates entirely to the superclass — no custom widgets are added.
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        return super.createDialogArea( parent );
    }
}
