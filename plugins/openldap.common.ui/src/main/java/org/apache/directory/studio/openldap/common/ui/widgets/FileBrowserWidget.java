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
package org.apache.directory.studio.openldap.common.ui.widgets;


import java.io.File;

import org.apache.directory.studio.common.ui.HistoryUtils;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.forms.widgets.FormToolkit;


// ── CLASS: FileBrowserWidget — REBEL TECHNICIAN ACCESSING DEATH STAR PLANS ───
// Picture a Rebel technician using a datapad to locate stolen Death Star plans
// on the server's file system. This widget extends the base
// {@link org.apache.directory.studio.ldapbrowser.common.widgets.FileBrowserWidget}
// to add {@link FormToolkit} support so it can be embedded in Eclipse Forms
// pages. The technician gets a file history combo and a Browse button; choosing
// a file via the OS dialog updates the combo and records the parent directory
// in the shared recent-file-path setting.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We extend the standard {@code FileBrowserWidget} to support {@link FormToolkit}
 * adaptation, enabling our widget to live inside Eclipse Forms pages. We override
 * {@link #createWidget(Composite, FormToolkit)} to adapt the file combo with the
 * toolkit when provided, and override {@link #getFilename()} and
 * {@link #setFilename(String)} for uniform access to the selected file path.
 *
 * <p>The DirectoryBrowserWidget provides a combo with a history of recently
 * used directory and a browse button to open the directory browser.</p>
 */
public class FileBrowserWidget extends org.apache.directory.studio.ldapbrowser.common.widgets.FileBrowserWidget
{
    // ── CONSTRUCTOR: FileBrowserWidget — BRIEFING THE TECHNICIAN ─────────────
    // We forward the dialog title, allowed file extensions, and open/save mode
    // to the parent constructor so the Browse button opens the correct OS file
    // chooser when clicked.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create a new {@link FileBrowserWidget} by forwarding the title,
     * file extension filters, and dialog type to the parent class.
     *
     * @param title       the title to show on the OS file chooser dialog
     * @param extensions  the array of file extension filters (e.g., {@code new String[]{"*.ldif"}})
     * @param type        the dialog type: one of {@link #TYPE_OPEN} or {@link #TYPE_SAVE}
     */
    public FileBrowserWidget( String title, String[] extensions, int type )
    {
        super( title, extensions, type );
    }


    // ── METHOD: createWidget(Composite, FormToolkit) — DEPLOYING THE TECHNICIAN
    // We build the file history combo and the Browse button, adapting the combo
    // with the toolkit when provided. The Browse button opens a {@link FileDialog},
    // lets the user select a file, updates the combo text, and saves the parent
    // directory to the shared recent-file-path dialog setting. We also populate
    // the combo from the shared file history.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create the file combo and Browse button inside the given parent,
     * optionally adapting the combo with the supplied {@link FormToolkit}.
     *
     * @param parent   the parent {@link Composite}
     * @param toolkit  the form toolkit, or {@code null} for plain SWT
     */
    public void createWidget( Composite parent, FormToolkit toolkit )
    {
        // Combo
        fileCombo = new Combo( parent, SWT.DROP_DOWN | SWT.BORDER );
        if ( toolkit != null )
        {
            toolkit.adapt( fileCombo );
        }
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
        if ( toolkit != null )
        {
            browseButton = toolkit.createButton( parent,
                Messages.getString( "FileBrowserWidget.BrowseButton" ), SWT.PUSH ); //$NON-NLS-1$
        }
        else
        {
            browseButton = BaseWidgetUtils.createButton( parent,
                Messages.getString( "FileBrowserWidget.BrowseButton" ), 1 ); //$NON-NLS-1$
        }
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


    // ── METHOD: getFilename — READING THE SELECTED PLANS LOCATION ────────────
    // We return the current combo text as the filename, or {@code null} if the
    // combo is empty — an empty path means the technician has not yet chosen
    // which plans to retrieve.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the currently selected or entered filename from the combo, or
     * {@code null} if the combo is empty.
     *
     * @return the filename string, or {@code null}
     */
    public String getFilename()
    {
        String filename = fileCombo.getText();

        if ( ( filename != null ) && ( !"".equals( filename ) ) )
        {
            return filename;
        }

        return null;
    }


    // ── METHOD: setFilename — LOADING THE PLANS LOCATION INTO THE DATAPAD ─────
    // We push the given filename into the combo. Both a {@code null} and a
    // non-null value set the combo text (the parent class contract).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We set the combo text to the given filename. Both {@code null} and a
     * valid path set the combo (the null branch results in the string "null"
     * being displayed — callers should pass an empty string to clear it instead).
     *
     * @param filename  the filename to display
     */
    public void setFilename( String filename )
    {
        if ( filename == null )
        {
            fileCombo.setText( filename );
        }
        else
        {
            fileCombo.setText( filename );
        }
    }
}
