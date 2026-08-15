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

package org.apache.directory.studio.ldapbrowser.common.widgets;


import java.io.File;

import org.apache.directory.studio.common.ui.HistoryUtils;
import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;


// ── CLASS: FileBrowserWidget — R2-D2 AT THE DEATH STAR TERMINAL ──────────────
// R2 rolls up to a Death Star terminal, plugs in, and starts querying the filesystem
// for the schematics — typing a path or browsing until he finds what he needs.
// FileBrowserWidget does exactly that: a combo box for typing file paths (with history)
// and a "Browse" button that opens the OS file-chooser dialog.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A reusable file-selection widget consisting of a drop-down combo (with recently-used
 * file history) and a "Browse..." button that opens the native OS file dialog.
 * Used wherever Directory Studio needs the user to supply a file path — for example
 * when importing or exporting LDIF files.
 * Think of R2-D2 at the Death Star terminal: he can type in the direct path if he knows
 * it, or browse the filesystem interactively until he finds the right schematics.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FileBrowserWidget extends AbstractWidget
{
    /** The Constant TYPE_OPEN is used to create a Open file dialog. */
    public static final int TYPE_OPEN = SWT.OPEN;

    /** The Constant TYPE_SAVE is used to create a Save file dialog. */
    public static final int TYPE_SAVE = SWT.SAVE;

    /** The combo with the history of recently used files */
    protected Combo fileCombo;

    /** The button to launch the file browser */
    protected Button browseButton;

    /** The title */
    protected String title;

    /** File extensions used within the launched file browser */
    protected String[] extensions;

    /** The type */
    protected int type;


    // ── R2 LOADS HIS MISSION PARAMETERS ──────────────────────────────────────────
    // Before R2 plugs into any terminal he knows exactly what he's looking for:
    // the dialog title, the file extensions that match, and whether he's reading or writing.
    // We store those mission parameters so createWidget() can configure the dialog correctly.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a FileBrowserWidget configured for a specific file-selection task.
     * No SWT widgets are built yet — call {@link #createWidget(Composite)} to render them.
     *
     * <p>For example — R2 loads his mission brief:</p>
     * <pre>
     *   title      = "Select LDIF File"      // dialog window title
     *   extensions = new String[]{"*.ldif"}  // only show matching files
     *   type       = TYPE_OPEN               // we're reading, not writing
     * </pre>
     *
     * @param title       The title shown in the native OS file dialog window.
     * @param extensions  An array of file extension filters (e.g. {@code "*.ldif"}) for
     *                    the OS dialog; may be {@code null} to show all files.
     * @param type        Either {@link #TYPE_OPEN} (for reading a file) or {@link #TYPE_SAVE}
     *                    (for writing a file) — maps directly to SWT's {@code SWT.OPEN}/{@code SWT.SAVE}.
     */
    public FileBrowserWidget( String title, String[] extensions, int type )
    {
        this.title = title;
        this.extensions = extensions;
        this.type = type;
    }


    // ── R2 PLUGS INTO THE TERMINAL AND SETS UP HIS INTERFACE ─────────────────────
    // R2 extends his interface arm, connects to the terminal, and loads the recent-access log.
    // He sets up a combo with the history of recently queried paths and a "Browse" button.
    // We build the SWT Combo (populated with history) and the Browse button (opening a FileDialog).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds and attaches the file-selection UI to the given parent composite.
     * Creates a horizontally-filling combo box pre-populated with the file-path history
     * loaded from dialog settings, plus a "Browse..." button that opens an OS file dialog.
     * When the user picks a file via Browse, the combo is updated and the new directory is
     * saved for next time.
     *
     * <p>For example — R2 at the terminal:</p>
     * <pre>
     *   fileCombo.setItems(history);    // pre-fill recent paths
     *   browseButton → FileDialog → returnedFileName → fileCombo.setText(...)
     * </pre>
     *
     * @param parent  The SWT composite to add the combo and button to; must not be {@code null}.
     */
    public void createWidget( Composite parent )
    {
        // Combo
        fileCombo = new Combo( parent, SWT.DROP_DOWN | SWT.BORDER );
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.widthHint = 50;
        fileCombo.setLayoutData( gd );
        fileCombo.setVisibleItemCount( 20 );
        fileCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                notifyListeners();
            }
        } );

        // Button
        browseButton = BaseWidgetUtils.createButton( parent, Messages.getString( "FileBrowserWidget.BrowseButton" ), 1 ); //$NON-NLS-1$
        browseButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                FileDialog fileDialog = new FileDialog( browseButton.getShell(), type );
                fileDialog.setText( title );

                fileDialog.setFilterExtensions( extensions );

                File file = new File( fileCombo.getText() );
                if ( file.isFile() )
                {
                    fileDialog.setFilterPath( file.getParent() );
                    fileDialog.setFileName( file.getName() );
                }
                else if ( file.isDirectory() )
                {
                    fileDialog.setFilterPath( file.getPath() );
                }
                else
                {
                    fileDialog.setFilterPath( BrowserCommonActivator.getDefault().getDialogSettings().get(
                        BrowserCommonConstants.DIALOGSETTING_KEY_RECENT_FILE_PATH ) );
                }

                String returnedFileName = fileDialog.open();
                if ( returnedFileName != null )
                {
                    fileCombo.setText( returnedFileName );
                    File file2 = new File( returnedFileName );
                    BrowserCommonActivator.getDefault().getDialogSettings().put(
                        BrowserCommonConstants.DIALOGSETTING_KEY_RECENT_FILE_PATH, file2.getParent() );
                }
            }
        } );

        // file history
        String[] history = HistoryUtils.load( BrowserCommonActivator.getDefault().getDialogSettings(),
            BrowserCommonConstants.DIALOGSETTING_KEY_FILE_HISTORY );
        fileCombo.setItems( history );
    }


    // ── R2 READS THE CURRENT PATH FROM THE TERMINAL DISPLAY ──────────────────────
    // The mission control asks: "R2, what file path does the terminal currently show?"
    // R2 beeps back the path currently displayed in his combo interface.
    // We return the text currently in the combo box, which may be typed or selected.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the file path currently shown in the combo box.
     * This may be a path the user typed manually or one they picked via the Browse dialog.
     *
     * <p>For example — R2 reads the display:</p>
     * <pre>
     *   fileCombo.getText() → "/home/user/exports/directory.ldif"
     * </pre>
     *
     * @return  The current file path string; may be empty if the user hasn't entered anything yet.
     */
    public String getFilename()
    {
        return fileCombo.getText();
    }


    // ── R2 TYPES A PATH INTO THE TERMINAL ────────────────────────────────────────
    // R2 is handed a file path by the mission controller and types it straight into the terminal.
    // The combo updates immediately so the user can see the pre-filled value.
    // We set the combo text to the given filename, which also triggers the ModifyListener.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Programmatically sets the file path displayed in the combo box.
     * Useful for pre-populating the widget with a default or previously remembered path.
     * Setting the text fires the modify listener and notifies any registered widget listeners.
     *
     * <p>For example — mission control hands R2 a path:</p>
     * <pre>
     *   widget.setFilename("/exports/backup.ldif");
     *   // combo now shows that path; listeners are notified
     * </pre>
     *
     * @param filename  The file path to display; must not be {@code null}.
     */
    public void setFilename( String filename )
    {
        fileCombo.setText( filename );
    }


    // ── R2 LOGS THE SESSION TO THE SHIP'S COMPUTER ───────────────────────────────
    // After completing the mission, R2 beams the file path history back to the Falcon's
    // computer so Chewie can pre-fill it next time they dock at the same terminal.
    // We persist the current filename into the dialog settings history list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the current file path into the persistent file-history list in dialog settings.
     * Call this when the containing dialog is closed successfully so the path appears in the
     * combo's drop-down history on the next run.
     *
     * <p>For example — R2 logs the session:</p>
     * <pre>
     *   HistoryUtils.save(dialogSettings, FILE_HISTORY_KEY, fileCombo.getText());
     *   // "/exports/backup.ldif" added to the recent-paths list
     * </pre>
     */
    public void saveDialogSettings()
    {
        HistoryUtils.save( BrowserCommonActivator.getDefault().getDialogSettings(),
            BrowserCommonConstants.DIALOGSETTING_KEY_FILE_HISTORY, fileCombo.getText() );
    }


    // ── R2 TRAINS HIS OPTICAL SENSOR ON THE INPUT FIELD ──────────────────────────
    // R2 swings his dome around and beams his focus directly at the combo text box.
    // Moving keyboard focus to the file combo lets users start typing immediately.
    // We call setFocus() on the combo so the cursor lands there on dialog open.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Moves keyboard focus to the file-path combo box.
     * Call this after the dialog opens to save the user a click and let them start
     * typing a file path right away.
     *
     * <p>For example — R2 focuses on the terminal input:</p>
     * <pre>
     *   fileCombo.setFocus();  // cursor lands in the combo — start typing
     * </pre>
     */
    public void setFocus()
    {
        fileCombo.setFocus();
    }


    // ── R2 POWERS THE TERMINAL ON OR OFF ──────────────────────────────────────────
    // The mission controller flips R2's power switch: on for active mission, off for standby.
    // Both the combo and the browse button respond together — they're one logical control.
    // We enable or disable both child widgets so the whole widget acts as a single unit.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the file-path combo and the Browse button together.
     * When disabled, both controls go grey and non-interactive — useful when the surrounding
     * form decides file selection doesn't apply in the current state.
     *
     * <p>For example — R2 on standby vs. active:</p>
     * <pre>
     *   setEnabled(false);  // both combo and button go grey — R2 is in low-power mode
     *   setEnabled(true);   // both controls light up — R2 is back on mission
     * </pre>
     *
     * @param b  {@code true} to enable the widget (interactive), {@code false} to disable it.
     */
    public void setEnabled( boolean b )
    {
        fileCombo.setEnabled( b );
        browseButton.setEnabled( b );
    }

}
