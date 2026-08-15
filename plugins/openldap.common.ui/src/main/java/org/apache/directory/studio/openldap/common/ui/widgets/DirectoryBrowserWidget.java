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
import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.Messages;
import org.apache.directory.studio.openldap.common.ui.OpenLdapCommonUiConstants;
import org.apache.directory.studio.openldap.common.ui.OpenLdapCommonUiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.forms.widgets.FormToolkit;


// ── CLASS: DirectoryBrowserWidget — REBEL PATHFINDER LOCATING HOTH BASES ─────
// Picture a Rebel pathfinder using a holoprojector to navigate to base
// locations on ice planets like Hoth. This widget gives the user a combo box
// with a history of recently visited directory paths, plus a "Browse" button
// that opens the OS directory chooser so they can scout new locations. Any
// change to the path fires our change listeners so the parent form stays in
// sync with where the pathfinder is pointing.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We provide a directory-browsing widget consisting of a history combo and a
 * Browse button. We extend {@link AbstractWidget} so callers can register
 * change listeners. We persist the visited paths using the plugin's dialog
 * settings so the history survives restarts.
 *
 * <p>The DirectoryBrowserWidget provides a combo with a history of recently
 * used directory and a browse button to open the directory browser.</p>
 */
public class DirectoryBrowserWidget extends AbstractWidget
{
    /** The combo with the history of recently used directories */
    protected Combo directoryCombo;

    /** The button to launch the file browser */
    protected Button browseButton;

    /** The title */
    protected String title;


    // ── CONSTRUCTOR: DirectoryBrowserWidget — BRIEFING THE PATHFINDER ─────────
    // We record the dialog title that will appear on the OS directory chooser
    // so users know what kind of base location they are selecting.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create a new {@link DirectoryBrowserWidget} with the given dialog title.
     *
     * @param title  the title to display on the OS directory chooser dialog
     */
    public DirectoryBrowserWidget( String title )
    {
        this.title = title;
    }


    // ── METHOD: createWidget(Composite) — DEPLOYING THE PATHFINDER (NO TOOLKIT)
    // We delegate to the toolkit-aware overload with a {@code null} toolkit so
    // there is always a single code path to maintain.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create the widget's SWT controls inside the given parent without a
     * {@link FormToolkit}. Delegates to
     * {@link #createWidget(Composite, FormToolkit)}.
     *
     * @param parent  the parent {@link Composite}
     */
    public void createWidget( Composite parent )
    {
        createWidget( parent, null );
    }


    // ── METHOD: createWidget(Composite, FormToolkit) — DEPLOYING THE PATHFINDER
    // We build the directory history combo and the Browse button. When Browse
    // is clicked we open a {@link DirectoryDialog}, let the user pick a path,
    // and update the combo and the shared recent-file-path dialog setting. We
    // also attach a ModifyListener to fire change events whenever the text changes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create the directory combo and Browse button inside the given parent,
     * optionally adapting them with the supplied {@link FormToolkit}.
     *
     * @param parent   the parent {@link Composite}
     * @param toolkit  the form toolkit, or {@code null} for plain SWT
     */
    public void createWidget( Composite parent, FormToolkit toolkit )
    {
        // Combo
        directoryCombo = new Combo( parent, SWT.DROP_DOWN | SWT.BORDER );
        if ( toolkit != null )
        {
            toolkit.adapt( directoryCombo );
        }
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.widthHint = 50;
        directoryCombo.setLayoutData( gd );
        directoryCombo.setVisibleItemCount( 20 );
        directoryCombo.addModifyListener( new ModifyListener()
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
                DirectoryDialog directoryDialog = new DirectoryDialog( browseButton.getShell() );
                directoryDialog.setText( title );

                File file = new File( directoryCombo.getText() );
                if ( file.isFile() )
                {
                    directoryDialog.setFilterPath( file.getParent() );
                }
                else if ( file.isDirectory() )
                {
                    directoryDialog.setFilterPath( file.getPath() );
                }
                else
                {
                    directoryDialog.setFilterPath( BrowserCommonActivator.getDefault().getDialogSettings().get(
                        BrowserCommonConstants.DIALOGSETTING_KEY_RECENT_FILE_PATH ) );
                }

                String returnedFileName = directoryDialog.open();
                if ( returnedFileName != null )
                {
                    directoryCombo.setText( returnedFileName );
                    File file2 = new File( returnedFileName );
                    BrowserCommonActivator.getDefault().getDialogSettings().put(
                        BrowserCommonConstants.DIALOGSETTING_KEY_RECENT_FILE_PATH, file2.getParent() );
                }
            }
        } );

        loadDialogSettings();
    }


    // ── METHOD: getDirectoryPath — READING THE PATHFINDER'S CURRENT POSITION ──
    // We read the combo text and return it if non-empty; otherwise we return
    // {@code null} to signal "no base location selected yet".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the currently entered or selected directory path, or {@code null}
     * if the combo is empty.
     *
     * @return the directory path, or {@code null}
     */
    public String getDirectoryPath()
    {
        String directoryPath = directoryCombo.getText();

        if ( ( directoryPath != null ) && ( !"".equals( directoryPath ) ) )
        {
            return directoryPath;
        }

        return null;
    }


    // ── METHOD: setDirectoryPath — UPDATING THE PATHFINDER'S COORDINATES ──────
    // We push the given path into the combo, clearing it if the path is
    // {@code null} so the field shows empty rather than the string "null".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We set the combo text to the given directory path. A {@code null} value
     * clears the combo.
     *
     * @param directoryPath  the path to display, or {@code null} to clear
     */
    public void setDirectoryPath( String directoryPath )
    {
        if ( directoryPath == null )
        {
            directoryCombo.setText( "" );
        }
        else
        {
            directoryCombo.setText( directoryPath );
        }
    }


    // ── METHOD: loadDialogSettings — RESTORING THE PATHFINDER'S ROUTE LOG ─────
    // We read the previously visited directory paths from the plugin's dialog
    // settings and populate the combo's drop-down history so the user can
    // quickly return to a recently scouted location.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We load the directory history from the plugin's dialog settings and
     * populate the combo's drop-down list. If the settings cannot be read we
     * fall back to an empty history.
     */
    public void loadDialogSettings()
    {
        String[] history = null;

        try
        {
            history = HistoryUtils.load( OpenLdapCommonUiPlugin.getDefault().getDialogSettings(),
                OpenLdapCommonUiConstants.DIALOGSETTING_KEY_DIRECTORY_HISTORY );
        }
        catch ( Exception e )
        {
            history = new String[]{};
        }

        directoryCombo.setItems( history );
    }


    // ── METHOD: saveDialogSettings — SAVING THE PATHFINDER'S ROUTE LOG ────────
    // We append the current combo text to the plugin's dialog settings so the
    // pathfinder can find this location again in a future session.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We save the current combo text into the plugin's directory history dialog
     * settings so it persists across Eclipse sessions.
     */
    public void saveDialogSettings()
    {
        OpenLdapCommonUiPlugin plugin = OpenLdapCommonUiPlugin.getDefault();

        if ( plugin != null )
        {
            HistoryUtils.save( OpenLdapCommonUiPlugin.getDefault().getDialogSettings(),
                OpenLdapCommonUiConstants.DIALOGSETTING_KEY_DIRECTORY_HISTORY, directoryCombo.getText() );
        }
    }


    // ── METHOD: setFocus — POINTING THE PATHFINDER'S EYES AT THE COMBO ────────
    // We hand keyboard focus to the combo so the user can immediately start
    // typing or editing the directory path without an extra click.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We move keyboard focus to the directory combo.
     */
    public void setFocus()
    {
        directoryCombo.setFocus();
    }


    // ── METHOD: setEnabled — ACTIVATING OR GROUNDING THE PATHFINDER ───────────
    // We enable or disable both the combo and the Browse button together so
    // the parent form can lock this control when it is not applicable.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We enable or disable both the directory combo and the Browse button.
     *
     * @param b  {@code true} to enable the widget, {@code false} to disable it
     */
    public void setEnabled( boolean b )
    {
        directoryCombo.setEnabled( b );
        browseButton.setEnabled( b );
    }
}
