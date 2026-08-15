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
package org.apache.directory.studio.templateeditor.editor.widgets;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Base64;

import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.apache.directory.studio.templateeditor.EntryTemplatePlugin;
import org.apache.directory.studio.templateeditor.EntryTemplatePluginConstants;
import org.apache.directory.studio.templateeditor.EntryTemplatePluginUtils;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateFileChooser;


// ── CLASS: EditorFileChooser — THE TANTIVE IV DATA TRANSFER STATION ──────────────
// On the Tantive IV, the data transfer station lets crew members load files from
// a data cartridge into the ship's memory banks, save stored files back to a
// cartridge, or clear the stored data. An icon shows what's docked, a size
// readout tells you how much data is stored, and the toolbar offers Save As,
// Clear, and Browse actions. This class is that station: it displays the current
// binary LDAP attribute (file content) with a size label, and provides optional
// toolbar actions for transferring the file to/from disk.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A file-chooser widget bound to a single binary LDAP attribute. Displays an
 * optional icon (from the template or a default file icon), a file-size label,
 * and an optional toolbar with Save As, Clear, and Browse actions. The binary
 * content is stored in the LDAP attribute's working copy.
 * Think of this as the Tantive IV data transfer station.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorFileChooser extends EditorWidget<TemplateFileChooser>
{
    /** The widget's composite */
    private Composite composite;

    /** The icon label */
    private Label iconLabel;

    /** The size label */
    private Label sizeLabel;

    /** The 'Save As...' toolbar item */
    private ToolItem saveAsToolItem;

    /** The 'Clear' toolbar item */
    private ToolItem clearToolItem;

    /** The 'Browse...' toolbar item */
    private ToolItem browseToolItem;

    /** The file data as bytes array */
    private byte[] fileBytes;

    /** The icon Image we might have to create */
    private Image iconImage;


    // ── CONSTRUCTOR: INSTALL THE DATA TRANSFER STATION ───────────────────────────
    // The technician installs the file chooser panel. It binds to the binary LDAP
    // attribute declared in templateFileChooser.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EditorFileChooser} bound to the given template model.
     *
     * @param editor               the owning entry editor
     * @param templateFileChooser  the template model specifying attribute type, buttons, icon
     * @param toolkit              the form toolkit
     */
    public EditorFileChooser( IEntryEditor editor, TemplateFileChooser templateFileChooser, FormToolkit toolkit )
    {
        super( templateFileChooser, editor, toolkit );
    }


    // ── CREATE WIDGET: BUILD THE DATA TRANSFER STATION ───────────────────────────
    /**
     * Creates the widget UI, fills it with the current LDAP attribute value, and
     * attaches toolbar button listeners.
     *
     * @param parent  the parent composite
     * @return the file chooser composite
     */
    public Composite createWidget( Composite parent )
    {
        // Creating and initializing the widget UI
        Composite composite = initWidget( parent );

        // Updating the widget's content
        updateWidget();

        // Adding the listeners
        addListeners();

        return composite;
    }


    // ── INIT WIDGET: BUILD THE STATION DISPLAY ────────────────────────────────────
    // We create a composite containing: an optional icon label (showing a file icon
    // or a custom Base64-decoded image), a size label, and an optional toolbar with
    // Save As / Clear / Browse buttons.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the composite: optional icon label, size readout label, and optional
     * Save As / Clear / Browse toolbar. The icon image is decoded from Base64 if
     * configured in the template, or a default file icon is used.
     *
     * @param parent  the parent composite
     * @return the file chooser composite
     */
    private Composite initWidget( Composite parent )
    {
        composite = getToolkit().createComposite( parent );
        composite.setLayoutData( getGridata() );

        // Creating the layout
        GridLayout gl = new GridLayout( getLayoutNumberOfColumns(), false );
        gl.marginHeight = gl.marginWidth = 0;
        gl.horizontalSpacing = gl.verticalSpacing = 0;
        composite.setLayout( gl );

        // Icon Label
        if ( getWidget().isShowIcon() )
        {
            // Creating the label for hosting the icon
            iconLabel = getToolkit().createLabel( composite, null );
            iconLabel.setLayoutData( new GridData( SWT.NONE, SWT.CENTER, false, false ) );

            // Getting the icon (if available)
            ImageData iconData = null;
            String icon = getWidget().getIcon();

            if ( ( icon != null ) && ( !icon.equals( "" ) ) ) //$NON-NLS-1$
            {
                try
                {
                    iconData = new ImageData( new ByteArrayInputStream( Base64.getDecoder().decode( icon ) ) );
                }
                catch ( SWTException e )
                {
                    // Nothing to do, we just need to return the default image.
                }
            }

            // Assigning the icon
            if ( iconData != null )
            {
                iconImage = new Image( PlatformUI.getWorkbench().getDisplay(), iconData );
                iconLabel.setImage( iconImage );
            }
            else
            {
                iconLabel.setImage( EntryTemplatePlugin.getDefault().getImage( EntryTemplatePluginConstants.IMG_FILE ) );
            }
        }

        // Size Label
        sizeLabel = getToolkit().createLabel( composite, null );
        sizeLabel.setLayoutData( new GridData( SWT.NONE, SWT.CENTER, true, false ) );

        // Toolbar (if needed)
        if ( needsToolbar() )
        {
            ToolBar toolbar = new ToolBar( composite, SWT.HORIZONTAL | SWT.FLAT );
            toolbar.setLayoutData( new GridData( SWT.NONE, SWT.CENTER, false, false ) );

            // Save As Button
            if ( getWidget().isShowSaveAsButton() )
            {
                saveAsToolItem = new ToolItem( toolbar, SWT.PUSH );
                saveAsToolItem.setToolTipText( Messages.getString( "EditorFileChooser.SaveAs" ) ); //$NON-NLS-1$
                saveAsToolItem.setImage( EntryTemplatePlugin.getDefault().getImage(
                    EntryTemplatePluginConstants.IMG_TOOLBAR_SAVE_AS ) );
            }

            // Clear Button
            if ( getWidget().isShowClearButton() )
            {
                clearToolItem = new ToolItem( toolbar, SWT.PUSH );
                clearToolItem.setToolTipText( Messages.getString( "EditorFileChooser.Clear" ) ); //$NON-NLS-1$
                clearToolItem.setImage( EntryTemplatePlugin.getDefault().getImage(
                    EntryTemplatePluginConstants.IMG_TOOLBAR_CLEAR ) );
            }
            // Browse Button
            if ( getWidget().isShowBrowseButton() )
            {
                browseToolItem = new ToolItem( toolbar, SWT.PUSH );
                browseToolItem.setToolTipText( Messages.getString( "EditorFileChooser.Browse" ) ); //$NON-NLS-1$
                browseToolItem.setImage( EntryTemplatePlugin.getDefault().getImage(
                    EntryTemplatePluginConstants.IMG_TOOLBAR_BROWSE_FILE ) );
            }
        }

        return composite;
    }


    // ── GET LAYOUT NUMBER OF COLUMNS: COUNT VISIBLE SECTIONS ─────────────────────
    /**
     * Returns the number of columns needed for the composite layout — 1 for the size
     * label, plus 1 for the optional icon, plus 1 for the optional toolbar.
     *
     * @return the number of columns (1, 2, or 3)
     */
    private int getLayoutNumberOfColumns()
    {
        int numberOfColumns = 1;

        // Icon
        if ( getWidget().isShowIcon() )
        {
            numberOfColumns++;
        }

        // Toolbar
        if ( needsToolbar() )
        {
            numberOfColumns++;
        }

        return numberOfColumns;
    }


    // ── NEEDS TOOLBAR: CHECK IF ANY BUTTONS ARE CONFIGURED ───────────────────────
    /**
     * Returns {@code true} if at least one toolbar action button is configured.
     *
     * @return {@code true} if a toolbar should be created
     */
    private boolean needsToolbar()
    {
        return getWidget().isShowSaveAsButton() || getWidget().isShowClearButton() || getWidget().isShowBrowseButton();
    }


    // ── UPDATE WIDGET: REFRESH THE STATION STATE ──────────────────────────────────
    /**
     * Re-reads the binary LDAP attribute, updates the size label, and refreshes
     * the enabled/disabled states of the toolbar buttons.
     */
    private void updateWidget()
    {
        // Initializing the image bytes from the given entry.
        initImageBytesFromEntry();

        // Updating the file label
        updateSizeLabel();

        // Updating the states of the buttons
        updateButtonsStates();
    }


    // ── INIT IMAGE BYTES FROM ENTRY: READ BINARY DATA FROM ATTRIBUTE ─────────────
    /**
     * Reads the binary LDAP attribute value into {@code fileBytes}. Sets
     * {@code fileBytes} to {@code null} if the attribute doesn't exist.
     */
    private void initImageBytesFromEntry()
    {
        // Getting the file bytes in the attribute
        IAttribute attribute = getAttribute();

        if ( ( attribute != null ) && ( attribute.isBinary() ) && ( attribute.getValueSize() > 0 ) )
        {
            fileBytes = attribute.getBinaryValues()[0];
            return;
        }

        fileBytes = null;
    }


    // ── UPDATE SIZE LABEL: SHOW HOW MUCH DATA IS STORED ──────────────────────────
    /**
     * Updates the size label text with a human-readable file size string.
     */
    private void updateSizeLabel()
    {
        sizeLabel.setText( getFileSizeString() );
        sizeLabel.update();
    }


    // ── ADD LISTENERS: WIRE ALL TOOLBAR BUTTON HANDLERS ──────────────────────────
    /**
     * Attaches selection listeners to the Save As, Clear, and Browse toolbar
     * buttons (if present).
     */
    private void addListeners()
    {
        // Save As toolbar item
        if ( ( saveAsToolItem != null ) && ( !saveAsToolItem.isDisposed() ) )
        {
            saveAsToolItem.addSelectionListener( new SelectionAdapter()
            {
                public void widgetSelected( SelectionEvent e )
                {
                    saveAsToolItemAction();
                }
            } );
        }

        // Clear toolbar item
        if ( ( clearToolItem != null ) && ( !clearToolItem.isDisposed() ) )
        {
            clearToolItem.addSelectionListener( new SelectionAdapter()
            {
                public void widgetSelected( SelectionEvent e )
                {
                    clearToolItemAction();
                }
            } );
        }

        // Browse toolbar item
        if ( ( browseToolItem != null ) && ( !browseToolItem.isDisposed() ) )
        {
            browseToolItem.addSelectionListener( new SelectionAdapter()
            {
                public void widgetSelected( SelectionEvent e )
                {
                    browseToolItemAction();
                }
            } );
        }
    }


    // ── GET FILE SIZE STRING: HUMAN-READABLE BYTE COUNT ──────────────────────────
    /**
     * Returns a human-readable string for the current file size (bytes, KB, or MB).
     * Returns a "no value" message if no file is loaded.
     *
     * @return a human-readable size string
     */
    private String getFileSizeString()
    {
        if ( fileBytes != null )
        {
            int length = fileBytes.length;

            if ( length > 1000000 )
            {
                return NLS.bind( Messages.getString( "EditorFileChooser.MB" ), new Object[] //$NON-NLS-1$
                    { ( length / 1000000 ), length } );
            }
            else if ( length > 1000 )
            {
                return NLS.bind( Messages.getString( "EditorFileChooser.KB" ), new Object[] //$NON-NLS-1$
                    { ( length / 1000 ), length } );
            }
            else
            {
                return NLS.bind( Messages.getString( "EditorFileChooser.Bytes" ), new Object[] //$NON-NLS-1$
                    { length } );
            }
        }
        else
        {
            return Messages.getString( "EditorFileChooser.NoValue" ); //$NON-NLS-1$
        }
    }


    // ── SAVE AS TOOL ITEM ACTION: SAVE FILE TO DISK ───────────────────────────────
    /**
     * Opens a Save file dialog and writes the current {@code fileBytes} to the
     * selected path. Shows an error dialog if writing fails.
     */
    private void saveAsToolItemAction()
    {
        // Launching a FileDialog to select where to save the file
        FileDialog fd = new FileDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SAVE );
        String selected = fd.open();

        if ( selected != null )
        {
            // Getting the selected file
            File selectedFile = new File( selected );

            if ( ( !selectedFile.exists() ) || ( selectedFile.canWrite() ) )
            {
                try
                {
                    FileOutputStream fos = new FileOutputStream( selectedFile );
                    fos.write( fileBytes );
                    fos.close();
                }
                catch ( Exception e )
                {
                    // Logging the error
                    EntryTemplatePluginUtils.logError( e, "An error occurred while saving the file to disk.", //$NON-NLS-1$
                        new Object[0] );

                    // Launching an error dialog
                    MessageDialog
                        .openError(
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                            Messages.getString( "EditorFileChooser.ErrorSavingMessageDialogTitle" ), Messages.getString( "EditorFileChooser.ErrorSavingMessageDialogMessage" ) ); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
    }


    // ── CLEAR TOOL ITEM ACTION: REMOVE THE STORED FILE ───────────────────────────
    /**
     * Asks for confirmation, then clears the file bytes and updates the LDAP
     * attribute accordingly.
     */
    private void clearToolItemAction()
    {
        // Launching a confirmation dialog
        if ( MessageDialog.openConfirm( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages
            .getString( "EditorFileChooser.Confirmation" ), Messages //$NON-NLS-1$
            .getString( "EditorFileChooser.ConfirmationClearFile" ) ) ) //$NON-NLS-1$
        {
            // Removing the file bytes
            fileBytes = null;

            // Refreshing the states of the buttons
            updateButtonsStates();

            // Updating the size label
            updateSizeLabel();

            // Updating the entry
            updateEntry();
        }
    }


    // ── BROWSE TOOL ITEM ACTION: LOAD A FILE FROM DISK ───────────────────────────
    /**
     * Opens an Open file dialog, reads the selected file into {@code fileBytes},
     * updates the size label and button states, then writes to the LDAP attribute.
     */
    private void browseToolItemAction()
    {
        // Launching a FileDialog to select the file to load
        FileDialog fd = new FileDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OPEN );
        String selected = fd.open();

        if ( selected != null )
        {
            // Getting the selected file
            File selectedFile = new File( selected );

            if ( ( selectedFile.exists() ) && ( selectedFile.canRead() ) )
            {
                try
                {
                    FileInputStream fis = null;
                    ByteArrayOutputStream baos = null;

                    try
                    {
                        fis = new FileInputStream( selectedFile );
                        baos = new ByteArrayOutputStream( ( int ) selectedFile.length() );
                        byte[] buf = new byte[4096];
                        int len;

                        while ( ( len = fis.read( buf ) ) > 0 )
                        {
                            baos.write( buf, 0, len );
                        }

                        fileBytes = baos.toByteArray();
                    }
                    finally
                    {
                        if ( fis != null )
                        {
                            fis.close();
                        }

                        if ( baos != null )
                        {
                            baos.close();
                        }
                    }
                }
                catch ( Exception e )
                {
                    // Logging the error
                    EntryTemplatePluginUtils.logError( e, "An error occurred while reading the file from disk.", //$NON-NLS-1$
                        new Object[0] );

                    // Launching an error dialog
                    MessageDialog
                        .openError(
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                            Messages.getString( "EditorFileChooser.ErrorReadingMessageDialogTitle" ), Messages.getString( "EditorFileChooser.ErrorReadingMessageDialogMessage" ) ); //$NON-NLS-1$ //$NON-NLS-2$
                }

                // Refreshing the states of the buttons
                updateButtonsStates();
            }
            else
            {
                // Logging the error
                EntryTemplatePluginUtils.logError( null,
                    "An error occurred while reading the file from disk. File does not exist or is not readable.", //$NON-NLS-1$
                    new Object[0] );

                // Launching an error dialog
                MessageDialog
                    .openError(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        Messages.getString( "EditorFileChooser.ErrorReadingMessageDialogTitle" ), Messages.getString( "EditorFileChooser.ErrorReadingMessageDialogMessage" ) ); //$NON-NLS-1$ //$NON-NLS-2$
            }

            // Updating the size label
            updateSizeLabel();

            // Updating the entry
            updateEntry();
        }
    }


    // ── UPDATE ENTRY: WRITE FILE BYTES TO THE LDAP ATTRIBUTE ─────────────────────
    /**
     * Writes the current {@code fileBytes} to the LDAP attribute. Creates, modifies,
     * or deletes the attribute based on whether bytes are present.
     */
    private void updateEntry()
    {
        // Getting the attribute
        IAttribute attribute = getAttribute();

        if ( attribute == null )
        {
            if ( ( fileBytes != null ) && ( fileBytes.length != 0 ) )
            {
                // Creating a new attribute with the value
                addNewAttribute( fileBytes );
            }
        }
        else
        {
            if ( ( fileBytes != null ) && ( fileBytes.length != 0 ) )
            {
                // Modifying the existing attribute
                modifyAttributeValue( fileBytes );
            }
            else
            {
                // Deleting the attribute
                deleteAttribute();
            }
        }
    }


    // ── UPDATE BUTTONS STATES: ENABLE/DISABLE BASED ON DATA PRESENCE ─────────────
    /**
     * Enables Save As and Clear buttons when file data is present; disables them
     * when no data is loaded. Browse is always enabled so the user can load a file.
     */
    private void updateButtonsStates()
    {
        if ( ( fileBytes != null ) && ( fileBytes.length > 0 ) )
        {
            if ( ( saveAsToolItem != null ) && ( !saveAsToolItem.isDisposed() ) )
            {
                saveAsToolItem.setEnabled( true );
            }

            if ( ( clearToolItem != null ) && ( !clearToolItem.isDisposed() ) )
            {
                clearToolItem.setEnabled( true );
            }

            if ( ( browseToolItem != null ) && ( !browseToolItem.isDisposed() ) )
            {
                browseToolItem.setEnabled( true );
            }
        }
        else
        {
            if ( ( saveAsToolItem != null ) && ( !saveAsToolItem.isDisposed() ) )
            {
                saveAsToolItem.setEnabled( false );
            }

            if ( ( clearToolItem != null ) && ( !clearToolItem.isDisposed() ) )
            {
                clearToolItem.setEnabled( false );
            }

            if ( ( browseToolItem != null ) && ( !browseToolItem.isDisposed() ) )
            {
                browseToolItem.setEnabled( true );
            }
        }
    }


    // ── UPDATE: REFRESH THE STATION STATE ────────────────────────────────────────
    /**
     * Refreshes the widget from the current LDAP attribute value.
     */
    public void update()
    {
        updateWidget();
    }


    // ── DISPOSE: RELEASE ICON IMAGE ───────────────────────────────────────────────
    /**
     * Disposes the custom icon {@link Image} if one was created from a Base64 string.
     * Standard SWT controls are disposed by their parent composite.
     */
    public void dispose()
    {
        if ( iconImage != null )
        {
            iconImage.dispose();
        }
    }
}
