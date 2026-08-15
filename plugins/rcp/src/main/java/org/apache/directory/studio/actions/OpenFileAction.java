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

package org.apache.directory.studio.actions;


import java.text.MessageFormat;

import org.apache.directory.studio.Messages;
import org.apache.directory.studio.PluginConstants;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;


// ── CLASS: OpenFileAction — Han Solo Reaches for the Falcon's Manifest ────────
// Han Solo stands in the Mos Eisley cantina; Luke asks him to fly them to
// Alderaan and he reaches into his jacket, pulls out the Falcon's cargo manifest,
// and says "I can do that — for the right price."  Quick, decisive, no fuss.
// OpenFileAction works the same way: the user clicks "Open File", we pop the OS
// file-chooser dialog, the user picks one or more files from the filesystem, and
// we open each one in an Eclipse editor — no questions asked.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens one or more files from the local filesystem in Eclipse editors.
 * This is the action behind File &gt; Open File... — it shows an OS native
 * file-chooser dialog, then opens each selected file using the Eclipse IDE
 * infrastructure which picks the appropriate editor based on file type.
 * Think of this as Han Solo opening the Falcon's cargo bay for a quick job.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenFileAction extends Action implements IWorkbenchWindowActionDelegate
{
    private IWorkbenchWindow window;
    private String filterPath;


    // ── Han Gets the Job Without a Specific Ship Yet ──────────────────────────
    // Han agrees to the job in the cantina before he's confirmed which docking
    // bay the Falcon is in — he sets his ID and advertises himself as ready,
    // knowing the window reference will arrive momentarily.
    // This no-arg constructor lets Eclipse create the action via the plugin.xml
    // extension point, before it has called init() with the window reference.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action with its ID, label, and tooltip but no window reference yet.
     * Eclipse can instantiate actions via extension points before the workbench
     * window is available; {@link #init(IWorkbenchWindow)} provides it later.
     */
    public OpenFileAction()
    {
        setId( PluginConstants.ACTION_OPEN_FILE_ID ); //$NON-NLS-1$
        setText( Messages.getString( "OpenFileAction.Open_File" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "OpenFileAction.Open_file_from_filesystem" ) ); //$NON-NLS-1$
        setEnabled( true );
    }


    // ── Han Learns Which Docking Bay — Full Briefing ──────────────────────────
    // Luke confirms "Docking Bay 94, Han" — now Han knows exactly where to find
    // the Falcon and can complete the full setup in one step.
    // This constructor combines the no-arg setup with an immediate init() call
    // so callers who already have the window can do it all in one line.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action and immediately initialises it with the workbench window.
     * Convenience constructor for callers (like {@link org.apache.directory.studio.ApplicationActionBarAdvisor})
     * that already have the window reference at construction time.
     *
     * @param window  the workbench window this action belongs to — stored for use
     *                in {@link #run()} when opening the file dialog.
     */
    public OpenFileAction( IWorkbenchWindow window )
    {
        this();
        init( window );
    }


    // ── Han Decommissions the Falcon After the Mission ────────────────────────
    // After the mission Han parks the Falcon and clears his nav computer — he
    // drops the Alderaan coordinates and his job spec so nothing leaks.
    // dispose() nulls the window and filterPath references so we don't hold
    // stale objects after the workbench window closes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Releases references when the action is no longer needed.
     * Eclipse calls this when the workbench window closes so we don't keep a
     * stale reference to the disposed window.
     */
    public void dispose()
    {
        window = null;
        filterPath = null;
    }


    // ── Han Takes the Coordinates for Docking Bay 94 ─────────────────────────
    // Chewie hands Han the docking bay coordinates and Han stores them in the
    // Falcon's nav system — now he knows where to go when the job fires.
    // init() stores the window reference and seeds the file-chooser starting
    // directory to the user's home folder.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the action with a workbench window reference.
     * Eclipse calls this when our action delegate is wired up to a window.
     * We also seed the file-chooser starting directory to the user's home
     * directory so the dialog opens somewhere sensible by default.
     *
     * @param window  the workbench window — we use it to get the shell for the
     *                file dialog and the active page for opening editors.
     */
    public void init( IWorkbenchWindow window )
    {
        this.window = window;
        filterPath = System.getProperty( "user.home" ); //$NON-NLS-1$
    }


    // ── Han Accepts the Job from the Mission Dispatcher ──────────────────────
    // The Rebel mission dispatcher calls "Solo, you're up" — Han relays the
    // signal to himself and gets moving.
    // run(IAction) is the IWorkbenchWindowActionDelegate entry point; we simply
    // delegate to the no-arg run() that does the real work.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} — the IWorkbenchWindowActionDelegate callback.
     * Eclipse calls this variant when the action is triggered from the menu bar;
     * we ignore the {@code action} parameter and use our own state.
     *
     * @param action  the proxy action from the menu bar — not used.
     */
    public void run( IAction action )
    {
        run();
    }


    // ── Han Shrugs at the Galaxy Map — He Doesn't Need a Selection ───────────
    // Han doesn't care which planet is highlighted on the galaxy map — he already
    // knows where he's going and what the job is.
    // selectionChanged() exists to satisfy the IWorkbenchWindowActionDelegate
    // contract but we don't use the selection to open files.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Not used — we do not change our enabled/disabled state based on selection.
     * This method exists only to satisfy the {@link IWorkbenchWindowActionDelegate} contract.
     *
     * @param action     the proxy action — not used.
     * @param selection  the current workbench selection — not used.
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
    }


    // ── Han Fires Up the Falcon and Goes to Get the Cargo ─────────────────────
    // Han runs to Docking Bay 94, powers up the Falcon, opens the cargo ramp,
    // and loads whatever Luke points at — if something isn't where it should be
    // he calls it out and keeps going with the rest.
    // run() shows the OS file-chooser dialog, then opens each selected file in
    // an Eclipse editor, collecting any "file not found" names for a final error
    // report at the end.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Shows the OS file-chooser dialog and opens each selected file in an Eclipse editor.
     * We remember the last directory the user browsed to (in {@code filterPath}) so
     * the next invocation opens in the same place.  Files that don't exist or are
     * directories are collected and reported in a single error dialog at the end
     * rather than one dialog per missing file.
     */
    public void run()
    {
        FileDialog dialog = new FileDialog( window.getShell(), SWT.OPEN | SWT.MULTI );
        dialog.setText( Messages.getString( "OpenFileAction.Open_File" ) ); //$NON-NLS-1$
        dialog.setFilterPath( filterPath );
        dialog.open();
        String[] names = dialog.getFileNames();

        if ( names != null )
        {
            filterPath = dialog.getFilterPath();

            int numberOfFilesNotFound = 0;
            StringBuffer notFound = new StringBuffer();
            IWorkbenchPage page = window.getActivePage();
            for ( String name : names )
            {
                IFileStore fileStore = EFS.getLocalFileSystem().getStore( new Path( filterPath ) ).getChild( name );
                IFileInfo fetchInfo = fileStore.fetchInfo();
                if ( !fetchInfo.isDirectory() && fetchInfo.exists() )
                {
                    try
                    {
                        IDE.openEditorOnFileStore( page, fileStore );
                    }
                    catch ( PartInitException e )
                    {
                        MessageDialog.openError( window.getShell(), Messages.getString( "OpenFileAction.Error" ), e //$NON-NLS-1$
                            .getMessage() );
                    }
                }
                else
                {
                    if ( ++numberOfFilesNotFound > 1 )
                    {
                        notFound.append( '\n' );
                    }
                    notFound.append( fileStore.getName() );
                }
            }

            if ( numberOfFilesNotFound > 0 )
            {
                String msg = MessageFormat.format( numberOfFilesNotFound == 1 ? Messages
                    .getString( "OpenFileAction.File_not_found" ) : Messages //$NON-NLS-1$
                    .getString( "OpenFileAction.Files_not_found" ), new Object[] //$NON-NLS-1$
                    { notFound.toString() } );
                MessageDialog.openError( window.getShell(), Messages.getString( "OpenFileAction.Error" ), msg ); //$NON-NLS-1$
            }
        }
    }
}
