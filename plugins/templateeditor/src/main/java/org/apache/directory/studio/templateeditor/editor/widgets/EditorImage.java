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
import org.apache.directory.studio.templateeditor.model.widgets.TemplateImage;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateWidget;


// ── CLASS: EditorImage — THE TANTIVE IV VISUAL DISPLAY PANEL ─────────────────────
// On the Tantive IV, the visual display panel shows whatever image is stored on the
// ship's data cartridge — a star chart, an ID photo, a technical schematic.
// Operators can save a copy to their own data cartridge, clear the displayed image,
// or browse to load a new one. If the stored image is larger than the panel's
// physical size (400x300), it's automatically scaled down to fit without distortion.
// This class is that display panel: it renders the binary LDAP attribute as a scaled
// SWT {@link Image} in a label, with optional Save As, Clear, and Browse toolbar
// actions for managing the image data.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * An image display widget bound to a single binary LDAP attribute. Reads image
 * bytes from the attribute (or from a Base64 template value), scales the image
 * to fit the configured or default (400×300) bounds while preserving aspect ratio,
 * and displays it in an SWT {@link Label}. Optionally provides Save As, Clear, and
 * Browse toolbar buttons. Think of this as the Tantive IV visual display panel.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorImage extends EditorWidget<TemplateImage>
{
    /** The widget's composite */
    private Composite composite;

    /** The image label, used to display the image */
    private Label imageLabel;

    /** The 'Save As...' button */
    private ToolItem saveAsToolItem;

    /** The 'Clear' button */
    private ToolItem clearToolItem;

    /** The 'Browse...' button */
    private ToolItem browseToolItem;

    /** The current image */
    private Image image;

    /** The image data as bytes array */
    private byte[] imageBytes;

    /** The default width */
    private static int DEFAULT_WIDTH = 400;

    /** The default height */
    private static int DEFAULT_HEIGHT = 300;


    // ── CONSTRUCTOR: INSTALL THE VISUAL DISPLAY PANEL ─────────────────────────────
    // The technician installs the image display panel, binding it to the template
    // model that specifies which LDAP attribute holds the image bytes and which
    // toolbar actions should be available.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EditorImage} bound to the given template image model.
     *
     * @param editor         the owning entry editor
     * @param templateImage  the template model specifying attribute type, image size constraints, and button visibility
     * @param toolkit        the form toolkit
     */
    public EditorImage( IEntryEditor editor, TemplateImage templateImage, FormToolkit toolkit )
    {
        super( templateImage, editor, toolkit );
    }


    // ── CREATE WIDGET: BUILD THE DISPLAY PANEL ───────────────────────────────────
    /**
     * Creates the image label and optional toolbar, loads and scales the image
     * from the LDAP attribute, and attaches toolbar listeners.
     *
     * @param parent  the parent composite
     * @return the image widget composite
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


    // ── INIT WIDGET: CREATE THE LABEL AND TOOLBAR ────────────────────────────────
    // We build a composite with 1 column (image only) or 2 columns (image + toolbar).
    // The imageLabel will hold the rendered image. The toolbar adds Save As / Clear /
    // Browse buttons in a vertical strip to the right of the image.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the composite, image label, and optional vertical toolbar with Save As,
     * Clear, and Browse buttons per the template model's configuration.
     *
     * @param parent  the parent composite
     * @return the image widget composite
     */
    private Composite initWidget( Composite parent )
    {
        // Creating the widget composite
        composite = getToolkit().createComposite( parent );
        composite.setLayoutData( getGridata() );

        // Creating the layout
        GridLayout gl = new GridLayout( ( needsToolbar() ? 2 : 1 ), false );
        gl.marginHeight = gl.marginWidth = 0;
        gl.horizontalSpacing = gl.verticalSpacing = 0;
        composite.setLayout( gl );

        // Image Label
        imageLabel = getToolkit().createLabel( composite, null );
        imageLabel.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );

        // Toolbar (if needed)
        if ( needsToolbar() )
        {
            ToolBar toolbar = new ToolBar( composite, SWT.VERTICAL );
            toolbar.setLayoutData( new GridData( SWT.NONE, SWT.FILL, false, true ) );

            // Save As Button
            if ( getWidget().isShowSaveAsButton() )
            {
                saveAsToolItem = new ToolItem( toolbar, SWT.PUSH );
                saveAsToolItem.setToolTipText( Messages.getString( "EditorImage.SaveAs" ) ); //$NON-NLS-1$
                saveAsToolItem.setImage( EntryTemplatePlugin.getDefault().getImage(
                    EntryTemplatePluginConstants.IMG_TOOLBAR_SAVE_AS ) );
            }

            // Clear Button
            if ( getWidget().isShowClearButton() )
            {
                clearToolItem = new ToolItem( toolbar, SWT.PUSH );
                clearToolItem.setToolTipText( Messages.getString( "EditorImage.Clear" ) ); //$NON-NLS-1$
                clearToolItem.setImage( EntryTemplatePlugin.getDefault().getImage(
                    EntryTemplatePluginConstants.IMG_TOOLBAR_CLEAR ) );
            }
            // Browse Button
            if ( getWidget().isShowBrowseButton() )
            {
                browseToolItem = new ToolItem( toolbar, SWT.PUSH );
                browseToolItem.setToolTipText( Messages.getString( "EditorImage.Browse" ) ); //$NON-NLS-1$
                browseToolItem.setImage( EntryTemplatePlugin.getDefault().getImage(
                    EntryTemplatePluginConstants.IMG_TOOLBAR_BROWSE_IMAGE ) );
            }
        }

        return composite;
    }


    // ── NEEDS TOOLBAR: CHECK IF ANY BUTTONS ARE CONFIGURED ───────────────────────
    /**
     * Returns {@code true} if at least one toolbar action button is configured in
     * the template model.
     *
     * @return {@code true} if a toolbar should be created
     */
    private boolean needsToolbar()
    {
        return getWidget().isShowSaveAsButton() || getWidget().isShowClearButton() || getWidget().isShowBrowseButton();
    }


    // ── UPDATE WIDGET: REFRESH THE DISPLAYED IMAGE ───────────────────────────────
    /**
     * Re-reads the image bytes from the LDAP attribute, re-scales and re-renders
     * the image, then updates the toolbar button states.
     */
    private void updateWidget()
    {
        // Initializing the image bytes from the given entry.
        initImageBytesFromEntry();

        // Constrains and displays it
        constrainAndDisplayImage();

        // Updating the states of the buttons
        updateButtonsStates();
    }


    // ── INIT IMAGE BYTES FROM ENTRY: LOAD IMAGE DATA ──────────────────────────────
    // We check whether the template has an attributeType set. If so, we pull the
    // binary value from the live LDAP entry. If not, we decode the static Base64
    // image string embedded in the template itself (useful for decorative images).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Loads image bytes either from the binary LDAP attribute (when an attribute type
     * is configured) or from a static Base64 string embedded in the template. Sets
     * {@code imageBytes} to {@code null} if no data is available.
     */
    private void initImageBytesFromEntry()
    {
        // Checking is we need to display a value taken from the entry
        // or use the given value
        String attributeType = getWidget().getAttributeType();
        if ( attributeType != null )
        {
            // Getting the image bytes in the attribute
            IAttribute attribute = getAttribute();
            if ( ( attribute != null ) && ( attribute.isBinary() ) && ( attribute.getValueSize() > 0 ) )
            {
                imageBytes = attribute.getBinaryValues()[0];
            }
            else
            {
                imageBytes = null;
            }
        }
        else
        {
            // Getting the image bytes given in the template
            String imageDataString = getWidget().getImageData();
            if ( ( imageDataString != null ) && ( !imageDataString.equals( "" ) ) ) //$NON-NLS-1$
            {
                imageBytes = Base64.getDecoder().decode( imageDataString );
            }
        }
    }


    // ── GET IMAGE DATA (bytes): DECODE BYTES INTO IMAGE DATA ──────────────────────
    /**
     * Decodes the given byte array into an SWT {@link ImageData} object.
     *
     * @param imageBytes  the raw image bytes (JPEG, PNG, GIF, etc.)
     * @return the decoded {@link ImageData}, or {@code null} if bytes are empty
     * @throws SWTException if the bytes cannot be decoded as a supported image format
     */
    private ImageData getImageData( byte[] imageBytes ) throws SWTException
    {
        if ( imageBytes != null && imageBytes.length > 0 )
        {
            return new ImageData( new ByteArrayInputStream( imageBytes ) );
        }
        else
        {
            return null;
        }
    }


    // ── GET IMAGE DATA (no-arg): GET CURRENT IMAGE DATA OR PLACEHOLDER ────────────
    // If we have image bytes, decode them. If decoding fails (corrupt data) or bytes
    // are null, fall back to the "no image" placeholder icon from the plugin registry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ImageData} for the current image bytes. Falls back to a
     * "no image" placeholder if the bytes are null or cannot be decoded.
     *
     * @return the current {@link ImageData} or a placeholder
     */
    private ImageData getImageData()
    {
        if ( imageBytes != null )
        {
            // Getting the image data associated with the bytes
            try
            {
                return getImageData( imageBytes );
            }
            catch ( SWTException e )
            {
                // Nothing to do, we just need to return the default image.
            }
        }

        // No image
        return EntryTemplatePlugin.getDefault().getImage( EntryTemplatePluginConstants.IMG_NO_IMAGE ).getImageData();
    }


    // ── CONSTRAIN AND DISPLAY IMAGE: SCALE AND RENDER ────────────────────────────
    // Like fitting a star chart on the display panel: if the template specifies
    // exact dimensions we use them; if not we check whether the image is larger than
    // 400×300 and scale it down while preserving aspect ratio. Then we create the
    // SWT Image and push it to the imageLabel.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Scales the current image to fit the template-configured or default (400×300)
     * bounds, preserving aspect ratio. Creates a new SWT {@link Image} and assigns
     * it to the image label.
     */
    private void constrainAndDisplayImage()
    {
        // Getting the image data
        ImageData imageData = getImageData();

        // Getting width and height from the template image
        int templateImageWidth = getWidget().getImageWidth();
        int templateImageHeight = getWidget().getImageHeight();

        // No resizing is required
        if ( ( templateImageWidth == TemplateWidget.DEFAULT_SIZE )
            && ( templateImageHeight == TemplateWidget.DEFAULT_SIZE ) )
        {
            // Checking if the dimensions of the image are greater than the default values
            if ( ( imageData.width > DEFAULT_WIDTH ) || ( imageData.height > DEFAULT_HEIGHT ) )
            {
                // Calculating scale factors to determine whether width or height should be used
                float widthScaleFactor = imageData.width / DEFAULT_WIDTH;
                float heightScaleFactor = imageData.height / DEFAULT_HEIGHT;

                // Resizing the image data
                if ( widthScaleFactor >= heightScaleFactor )
                {
                    imageData = getScaledImageData( imageData, DEFAULT_WIDTH, TemplateWidget.DEFAULT_SIZE );
                }
                else
                {
                    imageData = getScaledImageData( imageData, TemplateWidget.DEFAULT_SIZE, DEFAULT_HEIGHT );
                }
            }
        }
        else
        {
            // Resizing the image data
            imageData = getScaledImageData( imageData, templateImageWidth, templateImageHeight );
        }

        // Creating the image
        image = new Image( PlatformUI.getWorkbench().getDisplay(), imageData );

        // Setting the image
        imageLabel.setImage( image );
    }


    // ── GET SCALED IMAGE DATA: ASPECT-RATIO-PRESERVING RESIZE ────────────────────
    // We support three modes: scale to a given width (computing height from the
    // aspect ratio), scale to a given height (computing width), or scale to exact
    // width and height. If both dimensions are DEFAULT_SIZE, no scaling is applied.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a scaled copy of {@code imageData} to the given dimensions, preserving
     * aspect ratio when only one dimension is specified. Pass
     * {@link TemplateWidget#DEFAULT_SIZE} for a dimension to let us compute it.
     *
     * @param imageData  the source image data
     * @param width      the target width, or {@link TemplateWidget#DEFAULT_SIZE} to compute from height
     * @param height     the target height, or {@link TemplateWidget#DEFAULT_SIZE} to compute from width
     * @return the scaled {@link ImageData}
     */
    private ImageData getScaledImageData( ImageData imageData, int width, int height )
    {
        // Resizing the image with the given width value
        if ( ( width != TemplateWidget.DEFAULT_SIZE ) && ( height == TemplateWidget.DEFAULT_SIZE ) )
        {
            // Computing the scale factor
            float scaleFactor = ( float ) imageData.width / ( float ) width;

            // Computing the final height
            int finalHeight = ( int ) ( imageData.height / scaleFactor );

            // Returning the scaled image data
            return imageData.scaledTo( width, finalHeight );
        }
        // Resizing the image with the given height value
        else if ( ( width == TemplateWidget.DEFAULT_SIZE ) && ( height != TemplateWidget.DEFAULT_SIZE ) )
        {
            // Computing the scale factor
            float scaleFactor = ( float ) imageData.height / ( float ) height;

            // Computing the final height
            int finalWidth = ( int ) ( imageData.width / scaleFactor );

            // Returning the scaled image data
            return imageData.scaledTo( finalWidth, height );
        }
        // Resizing the image with the given width and height values
        else if ( ( width != TemplateWidget.DEFAULT_SIZE ) && ( height != TemplateWidget.DEFAULT_SIZE ) )
        {
            // Returning the original image data
            return imageData.scaledTo( width, height );
        }

        // No resizing needed
        return imageData;
    }


    // ── ADD LISTENERS: WIRE ALL TOOLBAR BUTTON HANDLERS ──────────────────────────
    /**
     * Attaches selection listeners to the Save As, Clear, and Browse toolbar
     * buttons (if present).
     */
    private void addListeners()
    {
        // Save As button
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

        // Clear button
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

        // Browse button
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


    // ── SAVE AS TOOL ITEM ACTION: SAVE IMAGE TO DISK ──────────────────────────────
    /**
     * Opens a Save file dialog and writes the current {@code imageBytes} to the
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
                    fos.write( imageBytes );
                    fos.close();
                }
                catch ( Exception e )
                {
                    // Logging the error
                    EntryTemplatePluginUtils.logError( e, "An error occurred while saving the image to disk.", //$NON-NLS-1$
                        new Object[0] );

                    // Launching an error dialog
                    MessageDialog
                        .openError(
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                            Messages.getString( "EditorImage.ErrorSavingMessageDialogTitle" ), Messages.getString( "EditorImage.ErrorSavingMessageDialogMessage" ) ); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
    }


    // ── CLEAR TOOL ITEM ACTION: REMOVE THE DISPLAYED IMAGE ───────────────────────
    /**
     * Asks for confirmation, then clears the image bytes, shows the placeholder
     * image, updates button states, and removes the value from the LDAP attribute.
     */
    private void clearToolItemAction()
    {
        // Launching a confirmation dialog
        if ( MessageDialog.openConfirm( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages
            .getString( "EditorImage.Confirmation" ), Messages.getString( "EditorImage.ConfirmationClearImage" ) ) ) //$NON-NLS-1$ //$NON-NLS-2$
        {
            // Removing the image bytes
            imageBytes = null;

            // Constrains and displays the image
            constrainAndDisplayImage();

            // Refreshing the states of the buttons
            updateButtonsStates();

            // Updating the entry
            updateEntry();

            // Updating the image
            composite.getParent().update();
        }
    }


    // ── BROWSE TOOL ITEM ACTION: LOAD AN IMAGE FROM DISK ─────────────────────────
    /**
     * Opens an Open file dialog, reads the selected image file into {@code imageBytes},
     * re-scales and displays it, updates button states, and writes to the LDAP attribute.
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

                        imageBytes = baos.toByteArray();
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
                    EntryTemplatePluginUtils.logError( e, "An error occurred while reading the image from disk.", //$NON-NLS-1$
                        new Object[0] );

                    // Launching an error dialog
                    MessageDialog
                        .openError(
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                            Messages.getString( "EditorImage.ErrorReadingMessageDialogTitle" ), Messages.getString( "EditorImage.ErrorReadingMessageDialogMessage" ) ); //$NON-NLS-1$ //$NON-NLS-2$
                }

                // Constrains and displays the image
                constrainAndDisplayImage();

                // Refreshing the states of the buttons
                updateButtonsStates();
            }
            else
            {
                // Logging the error
                EntryTemplatePluginUtils
                    .logError(
                        null,
                        "An error occurred while reading the image from disk. Image file does not exist or is not readable.", //$NON-NLS-1$
                        new Object[0] );

                // Launching an error dialog
                MessageDialog
                    .openError(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        Messages.getString( "EditorImage.ErrorReadingMessageDialogTitle" ), Messages.getString( "EditorImage.ErrorReadingMessageDialogMessage" ) ); //$NON-NLS-1$ //$NON-NLS-2$
            }

            // Updating the entry
            updateEntry();

            // Updating the image
            composite.getParent().update();
        }
    }


    // ── UPDATE BUTTONS STATES: ENABLE/DISABLE BASED ON DATA PRESENCE ─────────────
    /**
     * Enables Save As and Clear buttons when image bytes are present; disables them
     * when the display shows only the placeholder. Browse is always enabled.
     */
    private void updateButtonsStates()
    {
        if ( ( imageBytes != null ) && ( imageBytes.length > 0 ) )
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


    // ── UPDATE: REFRESH THE DISPLAYED IMAGE ──────────────────────────────────────
    /**
     * Refreshes the widget from the current LDAP attribute value.
     */
    public void update()
    {
        updateWidget();
    }


    // ── DISPOSE: RELEASE THE SWT IMAGE ───────────────────────────────────────────
    /**
     * Disposes the SWT {@link Image} created during rendering. Failing to call this
     * would leak a native OS graphics handle.
     */
    public void dispose()
    {
        image.dispose();
    }


    // ── UPDATE ENTRY: WRITE IMAGE BYTES TO THE LDAP ATTRIBUTE ────────────────────
    /**
     * Writes the current {@code imageBytes} to the LDAP attribute. Creates, modifies,
     * or deletes the attribute based on whether bytes are present.
     */
    private void updateEntry()
    {
        // Getting the attribute
        IAttribute attribute = getAttribute();
        if ( attribute == null )
        {
            if ( ( imageBytes != null ) && ( imageBytes.length != 0 ) )
            {
                // Creating a new attribute with the value
                addNewAttribute( imageBytes );
            }
        }
        else
        {
            if ( ( imageBytes != null ) && ( imageBytes.length != 0 ) )
            {
                // Modifying the existing attribute
                modifyAttributeValue( imageBytes );
            }
            else
            {
                // Deleting the attribute
                deleteAttribute();
            }
        }
    }
}
