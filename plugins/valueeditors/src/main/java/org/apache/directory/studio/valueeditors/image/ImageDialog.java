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

package org.apache.directory.studio.valueeditors.image;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.valueeditors.IValueEditor;
import org.apache.directory.studio.valueeditors.ValueEditorsActivator;
import org.apache.directory.studio.valueeditors.ValueEditorsConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;


// ── CLASS: ImageDialog — R2-D2 Projecting Leia's Holographic Message ─────────
// On Tatooine, R2-D2 projects Princess Leia's hologram: the current recording plays
// on one reel, and Luke can slot in a new one from his satchel on a second reel.
// This dialog works exactly the same way — current image on one tab, new image on another.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * A dialog that lets us view the JPEG image currently stored in an LDAP jpegPhoto
 * attribute and optionally replace it with a new one loaded from disk.
 * It sits between the user and the raw bytes, handling preview, metadata display,
 * format detection, and conversion before writing anything back to the directory.
 * Think of this class as R2-D2's holographic projector unit: it holds the existing
 * message and accepts a new recording when the user wants to swap it out.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImageDialog extends Dialog
{
    /** The dialog setting key for the currently selected tab item */
    private static final String SELECTED_TAB_DIALOGSETTINGS_KEY = ImageDialog.class.getName() + ".tab"; //$NON-NLS-1$

    /** The maximum width for the image */
    private static final int MAX_WIDTH = 400;

    /** The maximum height for the image */
    private static final int MAX_HEIGHT = 400;

    /** The current image tab item */
    private static final int CURRENT_TAB = 0;

    /** The new image tab item */
    private static final int NEW_TAB = 1;

    /** The current image bytes */
    private byte[] currentImageRawData;

    /** The required image type */
    private int requiredImageType;

    /** The new image bytes */
    private byte[] newImageRawData;

    /** The new image bytes in the required image format */
    private byte[] newImageRawDataInRequiredFormat;

    // UI widgets
    private TabFolder tabFolder;

    private TabItem currentTab;
    private Composite currentImageContainer;
    private Image currentImage;
    private Label currentImageLabel;
    private Text currentImageTypeText;
    private Text currentImageWidthText;
    private Text currentImageHeightText;
    private Text currentImageSizeText;
    private Button currentImageSaveButton;

    private TabItem newTab;
    private Composite newImageContainer;
    private Image newImage;
    private Label newImageLabel;
    private Text newImageTypeText;
    private Text newImageWidthText;
    private Text newImageHeightText;
    private Text newImageSizeText;
    private Text newImageFilenameText;
    private Button newImageBrowseButton;

    private Button okButton;


    // ── R2 Powers Up The Holographic Projector ───────────────────────────────────
    // On Tatooine, R2-D2 receives Princess Leia's data module and initializes the unit.
    // He stores her message bytes and notes which hologram format the projector requires.
    // We do the same: store the current LDAP image bytes and the target SWT format constant.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ImageDialog wired up with the current attribute bytes and the image
     * format we need to produce when the user clicks OK.
     * We hold a reference to the raw bytes so we can display them immediately, and we
     * record the required format so we can transcode later if the user picks something
     * different from disk.
     *
     * <p>For example — R2-D2 receives Leia's message and gets ready to project:</p>
     * <pre>
     *   R2 inserts the holographic data module.
     *   He notes the required output format and stands by for projection.
     * </pre>
     *
     * @param parentShell         the SWT shell that owns this dialog
     * @param currentImageRawData the raw bytes of the image currently in the attribute (may be null)
     * @param requiredImageType   the SWT image-type constant (e.g. {@code SWT.IMAGE_JPEG}) we must produce
     */
    public ImageDialog( Shell parentShell, byte[] currentImageRawData, int requiredImageType )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.currentImageRawData = currentImageRawData;
        this.requiredImageType = requiredImageType;

        newImageRawDataInRequiredFormat = null;
    }


    // ── R2 Shuts Down The Projector And Saves State ──────────────────────────────
    // After delivering Leia's message, R2 powers down and retracts the projector dome.
    // He marks which reel was active so next time he can resume from the same spot.
    // We dispose the SWT Image objects to free OS handles and persist the selected tab.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Closes the dialog, disposing any SWT {@link Image} objects we created and saving
     * the currently selected tab index so the dialog reopens on the same tab next time.
     * Forgetting to dispose SWT images leaks OS graphics handles, so we do it here
     * rather than relying on garbage collection.
     *
     * <p>For example — R2-D2 ends the projection and records which reel was playing:</p>
     * <pre>
     *   R2 retracts his projector dome and powers down the lens.
     *   He marks reel slot 2 in memory: "Resume here next time."
     * </pre>
     *
     * @return {@code true} if the dialog closed successfully
     */
    public boolean close()
    {
        // Disposing the current image
        if ( ( currentImage != null ) && !currentImage.isDisposed() )
        {
            currentImage.dispose();
        }

        // Disposing the new image
        if ( ( newImage != null ) && !newImage.isDisposed() )
        {
            newImage.dispose();
        }

        // Saving the selected tab item to dialog settings
        ValueEditorsActivator.getDefault().getDialogSettings().put( SELECTED_TAB_DIALOGSETTINGS_KEY,
            tabFolder.getSelectionIndex() );

        return super.close();
    }


    // ── Obi-Wan Decides Whether To Act On The Message ────────────────────────────
    // Obi-Wan watches the hologram and chooses: accept Leia's plea and set out, or dismiss it.
    // If he accepts, he takes the data crystal and converts it to the format the mission needs.
    // We convert the newly selected image to the required LDAP format when the user clicks OK.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Handles OK and Cancel button presses from the dialog button bar.
     * On OK, if a new image was selected we decode the raw bytes, check whether format
     * conversion is necessary, and if so transcode before handing the bytes back to the caller.
     * On Cancel (or on any decode failure) we null out the result so the caller knows
     * nothing changed.
     *
     * <p>For example — Obi-Wan accepts the mission and takes the data in the right format:</p>
     * <pre>
     *   Obi-Wan: "You must learn the ways of the Force."
     *   He takes the data crystal and checks the format against the mission spec.
     *   If the format doesn't match, he converts it before setting off.
     * </pre>
     *
     * @param buttonId the JFace button-bar constant ({@link IDialogConstants#OK_ID} or similar)
     */
    protected void buttonPressed( int buttonId )
    {
        if ( buttonId == IDialogConstants.OK_ID )
        {
            if ( newImageRawData != null )
            {
                // Preparing the new image bytes for the required format
                try
                {
                    ImageData imageData = new ImageData( new ByteArrayInputStream( newImageRawData ) );

                    if ( imageData.type != requiredImageType )
                    {
                        // Converting the new image in the required format
                        ImageLoader imageLoader = new ImageLoader();
                        imageLoader.data = new ImageData[]
                            { imageData };
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        imageLoader.save( baos, requiredImageType );
                        newImageRawDataInRequiredFormat = baos.toByteArray();
                    }
                    else
                    {
                        // Directly using the new image bytes
                        newImageRawDataInRequiredFormat = newImageRawData;
                    }
                }
                catch ( SWTException swte )
                {
                    newImageRawDataInRequiredFormat = null;
                }
            }
        }
        else
        {
            newImageRawDataInRequiredFormat = null;
        }

        super.buttonPressed( buttonId );
    }


    // ── R2 Labels The Projection Chamber ─────────────────────────────────────────
    // Before projecting, R2 activates the chamber nameplate so everyone knows what they'll see.
    // The nameplate and status light clearly identify the current projection session.
    // We set the shell title and icon so the window is recognizably the image editor.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Configures the dialog shell with the right window title and icon.
     * Called by the JFace framework before the dialog opens, giving us a chance to brand
     * the window so the user knows they're in the image editor rather than some other dialog.
     *
     * <p>For example — R2-D2 activates the chamber nameplate before projecting:</p>
     * <pre>
     *   R2 illuminates the sign above the alcove: "Holographic Message Editor."
     *   His status indicator blinks ready.
     * </pre>
     *
     * @param shell the shell to configure; we set its text and image
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "ImageDialog.ImageEditor" ) ); //$NON-NLS-1$
        shell.setImage( ValueEditorsActivator.getDefault().getImage( ValueEditorsConstants.IMG_IMAGEEDITOR ) );
    }


    // ── Installing The Projector Control Levers ───────────────────────────────────
    // Rebel technicians mount the OK and Cancel levers on R2's control panel.
    // They also restore the last active reel from R2's memory before the show begins.
    // We create the OK/Cancel buttons and restore the previously selected tab index.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the OK and Cancel buttons in the button bar, then restores the previously
     * selected tab from the dialog settings store.
     * The OK button is created as non-default so the user makes a deliberate choice;
     * after both buttons are wired up we replay the last-used tab selection.
     *
     * <p>For example — R2's control levers are installed and the last reel is queued up:</p>
     * <pre>
     *   Technician: "OK lever — check. Cancel lever — check."
     *   R2 reads memory: "Last time we were on reel 2." He cues it.
     * </pre>
     *
     * @param parent the composite that hosts the button bar
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );

        // load dialog settings
        try
        {
            int tabIndex = ValueEditorsActivator.getDefault().getDialogSettings().getInt(
                SELECTED_TAB_DIALOGSETTINGS_KEY );
            tabFolder.setSelection( tabIndex );
        }
        catch ( Exception e )
        {
            // Nothing to do
        }

        // Updating the tab folder on load
        updateTabFolder();
    }


    // ── Assembling R2's Full Holographic Stage ────────────────────────────────────
    // R2 extends every panel: the current-hologram bay, the new-recording bay, all status readouts.
    // He wires up the Browse button so Luke can load a different recording from his satchel.
    // We build the two-tab UI with preview labels, metadata fields, and file-picker controls.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the entire dialog content area: a tab folder with one tab for the current image
     * and one for loading a new image from disk.
     * Each tab gets a preview area, metadata readouts (type, size, width, height), and
     * action buttons — Save for the current image, Browse for the new one.
     * This is the main wiring method; everything the user sees gets assembled here.
     *
     * <p>For example — R2 extends his full projector array for the viewing session:</p>
     * <pre>
     *   R2's dome rotates; the current-message bay slides open on tab 1.
     *   A second bay extends for Luke to slot in a new recording on tab 2.
     *   All status readouts illuminate: type, size, width, height.
     * </pre>
     *
     * @param parent the parent composite provided by the JFace dialog framework
     * @return the top-level control we built, handed back to JFace
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        tabFolder = new TabFolder( composite, SWT.TOP );
        tabFolder.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        tabFolder.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                updateTabFolder();
            }
        } );

        // current image
        if ( currentImageRawData != null && currentImageRawData.length > 0 )
        {
            currentTab = new TabItem( tabFolder, SWT.NONE );
            currentTab.setText( Messages.getString( "ImageDialog.CurrentImage" ) ); //$NON-NLS-1$

            currentImageContainer = createTabItemComposite();
            currentImageLabel = createImageLabel( currentImageContainer );

            Composite currentImageInfoContainer = createImageInfoContainer( currentImageContainer );
            currentImageTypeText = createImageInfo( currentImageInfoContainer, Messages
                .getString( "ImageDialog.ImageType" ) ); //$NON-NLS-1$
            currentImageSizeText = createImageInfo( currentImageInfoContainer, Messages
                .getString( "ImageDialog.ImageSize" ) ); //$NON-NLS-1$
            currentImageWidthText = createImageInfo( currentImageInfoContainer, Messages
                .getString( "ImageDialog.ImageWidth" ) ); //$NON-NLS-1$
            currentImageHeightText = createImageInfo( currentImageInfoContainer, Messages
                .getString( "ImageDialog.ImageHeight" ) ); //$NON-NLS-1$

            Composite currentImageSaveContainer = createImageInfoContainer( currentImageContainer );
            Label dummyLabel = BaseWidgetUtils.createLabel( currentImageSaveContainer, "", 1 ); //$NON-NLS-1$
            GridData gd = new GridData( GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL );
            dummyLabel.setLayoutData( gd );
            currentImageSaveButton = createButton( currentImageSaveContainer, Messages.getString( "ImageDialog.Save" ) ); //$NON-NLS-1$

            currentImageSaveButton.addSelectionListener( new SelectionAdapter()
            {
                public void widgetSelected( SelectionEvent event )
                {
                    FileDialog fileDialog = new FileDialog( ImageDialog.this.getShell(), SWT.SAVE );
                    fileDialog.setText( Messages.getString( "ImageDialog.SaveImage" ) ); //$NON-NLS-1$
                    fileDialog.setFilterExtensions( new String[]
                        { "*.jpg" } ); //$NON-NLS-1$
                    String returnedFileName = fileDialog.open();

                    if ( returnedFileName != null )
                    {
                        try
                        {
                            File file = new File( returnedFileName );
                            FileOutputStream out = new FileOutputStream( file );
                            out.write( currentImageRawData );
                            out.flush();
                            out.close();
                        }
                        catch ( FileNotFoundException e )
                        {
                            ConnectionUIPlugin.getDefault().getExceptionHandler().handleException(
                                new Status( IStatus.ERROR, ValueEditorsConstants.PLUGIN_ID, IStatus.ERROR, Messages
                                    .getString( "ImageDialog.CantWriteFile" ), e ) ); //$NON-NLS-1$
                        }
                        catch ( IOException e )
                        {
                            ConnectionUIPlugin.getDefault().getExceptionHandler().handleException(
                                new Status( IStatus.ERROR, ValueEditorsConstants.PLUGIN_ID, IStatus.ERROR, Messages
                                    .getString( "ImageDialog.CantWriteFile" ), e ) ); //$NON-NLS-1$
                        }
                    }
                }
            } );

            currentTab.setControl( currentImageContainer );
        }

        // new image
        newTab = new TabItem( tabFolder, SWT.NONE );
        newTab.setText( Messages.getString( "ImageDialog.NewImage" ) ); //$NON-NLS-1$

        newImageContainer = createTabItemComposite();
        newImageLabel = createImageLabel( newImageContainer );

        Composite newImageInfoContainer = createImageInfoContainer( newImageContainer );
        newImageTypeText = createImageInfo( newImageInfoContainer, Messages.getString( "ImageDialog.ImageType" ) ); //$NON-NLS-1$
        newImageSizeText = createImageInfo( newImageInfoContainer, Messages.getString( "ImageDialog.ImageSize" ) ); //$NON-NLS-1$
        newImageWidthText = createImageInfo( newImageInfoContainer, Messages.getString( "ImageDialog.ImageWidth" ) ); //$NON-NLS-1$
        newImageHeightText = createImageInfo( newImageInfoContainer, Messages.getString( "ImageDialog.ImageHeight" ) ); //$NON-NLS-1$

        Composite newImageSelectContainer = createImageInfoContainer( newImageContainer );
        newImageFilenameText = new Text( newImageSelectContainer, SWT.SINGLE | SWT.BORDER );
        GridData gd = new GridData( SWT.FILL, SWT.CENTER, true, false );
        newImageFilenameText.setLayoutData( gd );

        newImageFilenameText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                updateNewImageGroup();
            }
        } );

        newImageBrowseButton = createButton( newImageSelectContainer, Messages.getString( "ImageDialog.Browse" ) ); //$NON-NLS-1$

        newImageBrowseButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                FileDialog fileDialog = new FileDialog( ImageDialog.this.getShell(), SWT.OPEN );
                fileDialog.setText( Messages.getString( "ImageDialog.SelectImage" ) ); //$NON-NLS-1$
                fileDialog.setFileName( new File( newImageFilenameText.getText() ).getName() );
                fileDialog.setFilterPath( new File( newImageFilenameText.getText() ).getParent() );

                String returnedFileName = fileDialog.open();

                if ( returnedFileName != null )
                {
                    newImageFilenameText.setText( returnedFileName );
                }
            }
        } );

        newTab.setControl( newImageContainer );
        applyDialogFont( composite );

        return composite;
    }


    // ── R2 Constructs A Self-Contained Reel Bay ───────────────────────────────────
    // Inside the projector, R2 assembles a compartment for each reel — its own frame and spacing.
    // Each bay is properly margined so the hologram has breathing room on every side.
    // We create a Composite with dialog-unit margins to host one tab's content area.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a properly margined {@link Composite} to act as the content pane for a single
     * tab inside the tab folder.
     * We use dialog-unit measurements so spacing is consistent across platforms and font sizes —
     * important for an Eclipse RCP app that runs on Windows, Linux, and macOS.
     *
     * <p>For example — R2 assembles a dedicated reel bay with standard holoprojector clearances:</p>
     * <pre>
     *   R2 deploys a compartment with platform-calibrated margins.
     *   Each bay fits one reel and its metadata readout panel.
     * </pre>
     *
     * @return a new {@link Composite} parented to the tab folder, ready to host widgets
     */
    private Composite createTabItemComposite()
    {
        Composite composite = new Composite( tabFolder, SWT.NONE );

        GridLayout compositeLayout = new GridLayout( 1, false );
        compositeLayout.marginHeight = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_MARGIN );
        compositeLayout.marginWidth = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_MARGIN );
        compositeLayout.verticalSpacing = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_SPACING );
        compositeLayout.horizontalSpacing = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_SPACING );
        composite.setLayout( compositeLayout );

        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        return composite;
    }


    // ── R2 Unfolds The Projection Screen ─────────────────────────────────────────
    // R2's dome opens and a small bordered screen clicks into place for the hologram.
    // The screen is centered and sits against a muted background so the image pops.
    // We create a bordered, centered Label that will hold the SWT Image object.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the image preview area — a bordered, centered {@link Label} inside a container
     * {@link Composite} with a muted shadow background.
     * The border and background give the preview a "screen" feel so the image stands out
     * visually from the metadata fields below it.
     *
     * <p>For example — R2 unfolds his projection screen for the viewing:</p>
     * <pre>
     *   A rectangular screen clicks into place on R2's dome.
     *   The background dims around it so the hologram image pops.
     * </pre>
     *
     * @param parent the composite inside which the screen label will live
     * @return the {@link Label} to which we'll later assign the {@link Image}
     */
    private Label createImageLabel( Composite parent )
    {
        Composite labelComposite = new Composite( parent, SWT.BORDER );
        labelComposite.setLayout( new GridLayout() );
        GridData gd = new GridData( SWT.FILL, SWT.FILL, true, true );
        labelComposite.setLayoutData( gd );
        labelComposite.setBackground( getShell().getDisplay().getSystemColor( SWT.COLOR_WIDGET_NORMAL_SHADOW ) );

        Label imageLabel = new Label( labelComposite, SWT.CENTER );
        gd = new GridData( SWT.CENTER, SWT.CENTER, true, true );
        imageLabel.setLayoutData( gd );

        return imageLabel;
    }


    // ── R2 Re-Renders The Stored Holographic Message ──────────────────────────────
    // Leia's recording is in R2's memory; he decodes it and projects it on the screen.
    // If the recording is corrupted or absent, he displays a "no signal" notice instead.
    // We decode currentImageRawData, resize it to fit the 400x400 box, and fill metadata fields.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the "current image" tab to show whatever bytes are in {@code currentImageRawData}.
     * We decode the raw bytes into an SWT {@link ImageData}, resize to fit the 400x400 preview
     * area, and populate the type/size/width/height metadata fields.
     * If the bytes are null, empty, or undecodable we show appropriate placeholder text instead.
     * The Save button is enabled only when there are valid bytes to export.
     *
     * <p>For example — R2 decodes and projects Leia's stored message:</p>
     * <pre>
     *   R2 reads the data module and projects the hologram on screen.
     *   Beneath it: "JPEG - 15 KB - 320 x 240 px."
     *   If the module is blank or corrupted: "No signal."
     * </pre>
     */
    private void updateCurrentImageGroup()
    {
        if ( currentTab != null )
        {
            if ( ( currentImage != null ) && !currentImage.isDisposed() )
            {
                currentImage.dispose();
                currentImage = null;
            }

            if ( currentImageRawData != null && currentImageRawData.length > 0 )
            {
                try
                {
                    ImageData imageData = new ImageData( new ByteArrayInputStream( currentImageRawData ) );
                    currentImage = new Image( getShell().getDisplay(), resizeImage( imageData ) );
                    currentImageLabel.setText( "" ); //$NON-NLS-1$
                    currentImageLabel.setImage( currentImage );
                    GridData currentImageLabelGridData = new GridData( SWT.CENTER, SWT.CENTER, true, true );
                    currentImageLabelGridData.widthHint = currentImage.getBounds().width;
                    currentImageLabelGridData.heightHint = currentImage.getBounds().height;
                    currentImageLabel.setLayoutData( currentImageLabelGridData );
                    currentImageTypeText.setText( getImageType( imageData.type ) );
                    currentImageSizeText.setText( getSizeString( currentImageRawData.length ) );
                    currentImageWidthText.setText( NLS
                        .bind( Messages.getString( "ImageDialog.Pixel" ), imageData.width ) ); //$NON-NLS-1$
                    currentImageHeightText.setText( NLS.bind(
                        Messages.getString( "ImageDialog.Pixel" ), imageData.height ) ); //$NON-NLS-1$
                }
                catch ( SWTException swte )
                {
                    currentImageLabel.setImage( null );
                    currentImageLabel.setText( Messages.getString( "ImageDialog.UnsupportedFormatSpaces" ) ); //$NON-NLS-1$
                    currentImageTypeText.setText( Messages.getString( "ImageDialog.UnsupportedFormat" ) ); //$NON-NLS-1$
                    currentImageSizeText.setText( getSizeString( currentImageRawData.length ) );
                    currentImageWidthText.setText( "-" ); //$NON-NLS-1$
                    currentImageHeightText.setText( "-" ); //$NON-NLS-1$
                }
            }
            else
            {
                currentImageLabel.setImage( null );
                currentImageLabel.setText( Messages.getString( "ImageDialog.NoImageSpaces" ) ); //$NON-NLS-1$
                currentImageTypeText.setText( Messages.getString( "ImageDialog.NoImage" ) ); //$NON-NLS-1$
                currentImageSizeText.setText( "-" ); //$NON-NLS-1$
                currentImageWidthText.setText( "-" ); //$NON-NLS-1$
                currentImageHeightText.setText( "-" ); //$NON-NLS-1$
            }

            currentImageSaveButton.setEnabled( currentImageRawData != null && currentImageRawData.length > 0 );
        }
    }


    // ── Luke Slots In A New Recording For R2 To Preview ──────────────────────────
    // Luke pulls a new data tape from his satchel and offers it to R2, who previews it at once.
    // If the tape is blank, unreadable, or a different format, R2 reports the problem.
    // We read the picked file, decode it, and update the preview label plus all metadata fields.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the "new image" tab based on the filename currently typed in the text field.
     * We read the file from disk into a byte array, parse it as an image, show a scaled preview,
     * and populate type/size/width/height metadata.
     * If the image format differs from what LDAP expects we note that a conversion will happen.
     * The OK button is enabled only when a preview decoded without error.
     *
     * <p>For example — Luke inserts a new tape and R2 previews it immediately:</p>
     * <pre>
     *   Luke: "Here's a new one, R2."
     *   R2 reads it: "PNG - 42 KB - 512 x 384 px."
     *   R2: "Warning: LDAP expects JPEG; I will convert when you press OK."
     * </pre>
     */
    private void updateNewImageGroup()
    {
        if ( ( newImage != null ) && !newImage.isDisposed() )
        {
            newImage.dispose();
            newImage = null;
        }

        String newImageFileName = newImageFilenameText.getText();

        if ( !Strings.isEmpty( newImageFileName ) ) //$NON-NLS-1$
        {
            try
            {
                File file = new File( newImageFileName );
                FileInputStream in = new FileInputStream( file );
                ByteArrayOutputStream out = new ByteArrayOutputStream( ( int ) file.length() );
                byte[] buf = new byte[4096];
                int len;

                while ( ( len = in.read( buf ) ) > 0 )
                {
                    out.write( buf, 0, len );
                }

                newImageRawData = out.toByteArray();
                out.close();
                in.close();
            }
            catch ( FileNotFoundException e )
            {
                newImageRawData = null;
                newImageLabel.setImage( null );
                newImageLabel.setText( Messages.getString( "ImageDialog.ErrorFileNotFound" ) ); //$NON-NLS-1$
                newImageTypeText.setText( "-" ); //$NON-NLS-1$
                newImageSizeText.setText( "-" ); //$NON-NLS-1$
                newImageWidthText.setText( "-" ); //$NON-NLS-1$
                newImageHeightText.setText( "-" ); //$NON-NLS-1$
            }
            catch ( IOException e )
            {
                newImageRawData = null;
                newImageLabel.setImage( null );
                newImageLabel.setText( NLS.bind(
                    Messages.getString( "ImageDialog.CantReadFile" ), new String[] { e.getMessage() } ) ); //$NON-NLS-1$
                newImageTypeText.setText( "-" ); //$NON-NLS-1$
                newImageSizeText.setText( "-" ); //$NON-NLS-1$
                newImageWidthText.setText( "-" ); //$NON-NLS-1$
                newImageHeightText.setText( "-" ); //$NON-NLS-1$
            }
        }
        else
        {
            newImageRawData = null;
            newImageLabel.setImage( null );
            newImageLabel.setText( Messages.getString( "ImageDialog.NoImageSelected" ) ); //$NON-NLS-1$
            newImageTypeText.setText( "-" ); //$NON-NLS-1$
            newImageSizeText.setText( "-" ); //$NON-NLS-1$
            newImageWidthText.setText( "-" ); //$NON-NLS-1$
            newImageHeightText.setText( "-" ); //$NON-NLS-1$
        }

        if ( ( newImageRawData != null ) && ( newImageRawData.length > 0 ) )
        {
            try
            {
                ImageData imageData = new ImageData( new ByteArrayInputStream( newImageRawData ) );
                newImage = new Image( getShell().getDisplay(), resizeImage( imageData ) );
                newImageLabel.setImage( newImage );
                newImageTypeText.setText( getImageType( imageData.type ) );

                if ( imageData.type != requiredImageType )
                {
                    newImageTypeText
                        .setText( newImageTypeText.getText()
                            + NLS
                                .bind(
                                    Messages.getString( "ImageDialog.WillBeConverted" ), new String[] { getImageType( requiredImageType ) } ) ); //$NON-NLS-1$
                }

                newImageSizeText.setText( getSizeString( newImageRawData.length ) );
                newImageWidthText.setText( NLS.bind( Messages.getString( "ImageDialog.Pixel" ), imageData.width ) ); //$NON-NLS-1$
                newImageHeightText.setText( NLS.bind( Messages.getString( "ImageDialog.Pixel" ), imageData.height ) ); //$NON-NLS-1$
            }
            catch ( SWTException swte )
            {
                newImageLabel.setImage( null );
                newImageLabel.setText( Messages.getString( "ImageDialog.UnsupportedFormatSpaces" ) ); //$NON-NLS-1$
                newImageTypeText.setText( Messages.getString( "ImageDialog.UnsupportedFormat" ) ); //$NON-NLS-1$
                newImageSizeText.setText( getSizeString( newImageRawData.length ) );
                newImageWidthText.setText( "-" ); //$NON-NLS-1$
                newImageHeightText.setText( "-" ); //$NON-NLS-1$
            }
        }

        if ( okButton != null )
        {
            okButton.setEnabled( newImage != null );
        }

        newImageLabel.getParent().layout();
        newImageTypeText.getParent().layout();
    }


    // ── R2 Rotates To The Active Reel Bay ────────────────────────────────────────
    // R2 rotates his reel selector to whichever bay Luke is pointing at right now.
    // The focused button and live readouts update to match the newly active bay.
    // We route keyboard focus and refresh content for whichever tab was just selected.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Keeps the tab folder in sync when the user switches between the "current image"
     * and "new image" tabs.
     * We route keyboard focus to the most relevant button on the newly active tab and
     * refresh that tab's content so the preview and metadata are up to date.
     *
     * <p>For example — R2 rotates to the active reel and refreshes its status readout:</p>
     * <pre>
     *   Luke points to reel 2. R2 rotates, focuses the Browse button, and reads the tape.
     *   The status readout flickers to the new recording's metadata.
     * </pre>
     */
    private void updateTabFolder()
    {
        if ( currentImageSaveButton != null )
        {
            if ( tabFolder.getSelectionIndex() == CURRENT_TAB )
            {
                currentImageSaveButton.setFocus();
            }

            updateCurrentImageGroup();
        }

        if ( newImageBrowseButton != null )
        {
            if ( ( tabFolder.getSelectionIndex() == NEW_TAB ) || ( currentImageSaveButton == null ) )
            {
                newImageBrowseButton.setFocus();
            }

            updateNewImageGroup();
        }
    }


    // ── R2 Scales The Hologram To Fit His Dome Aperture ──────────────────────────
    // Leia's original recording might be huge; R2 must scale it to fit his projector dome.
    // He shrinks proportionally so her face is neither stretched nor cropped.
    // We compute the minimum uniform scale factor that keeps both dimensions within 400x400.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Computes a uniformly scaled version of the given {@link ImageData} that fits within
     * our 400x400 preview box.
     * We calculate separate width and height scale factors and take the minimum so we never
     * exceed either dimension, always preserving the original aspect ratio.
     *
     * <p>For example — R2 shrinks Leia's hologram to fit his dome's aperture:</p>
     * <pre>
     *   R2 measures: width factor 0.5, height factor 0.8.
     *   He picks 0.5 — the smaller value — so neither axis overflows.
     *   Leia appears at half size but with perfect proportions.
     * </pre>
     *
     * @param imageData the original image data, potentially larger than the preview area
     * @return a new {@link ImageData} scaled to fit within {@code MAX_WIDTH} x {@code MAX_HEIGHT}
     */
    private ImageData resizeImage( ImageData imageData )
    {
        // Computing the width scale factor
        double widthScaleFactor = 1.0;

        if ( imageData.width > MAX_WIDTH )
        {
            widthScaleFactor = ( double ) MAX_WIDTH / imageData.width;
        }

        // Computing the height scale factor
        double heightScaleFactor = 1.0;

        if ( imageData.height > MAX_HEIGHT )
        {
            heightScaleFactor = ( double ) MAX_HEIGHT / imageData.height;
        }

        // Taking the minimum of both
        double minScalefactor = Math.min( heightScaleFactor, widthScaleFactor );

        // Resizing the image data
        return resize( imageData, ( int ) ( imageData.width * minScalefactor ),
            ( int ) ( imageData.height * minScalefactor ) );
    }


    // ── R2's High-Fidelity Optical Unit Renders The Target Frame ─────────────────
    // R2 uses his precision optics to render Leia at exactly the pixel count we need.
    // He enables antialiasing so the hologram looks smooth rather than blocky.
    // We use GC drawImage for OS-quality scaling rather than the cheap nearest-neighbor path.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Performs the actual pixel-level resize using SWT's {@link GC#drawImage} with antialiasing
     * and high interpolation enabled.
     * The GC approach gives much better visual quality than {@link ImageData#scaledTo} because
     * it delegates to the OS graphics pipeline.
     * We create a temporary off-screen image, draw into it, extract the raw pixel data, then
     * dispose both temporary images to prevent GDI handle leaks.
     *
     * <p>For example — R2's precision optical unit renders Leia at target resolution:</p>
     * <pre>
     *   R2 activates his high-resolution renderer with antialiasing.
     *   He draws Leia into the target frame.
     *   The hologram looks smooth — no blocky artifacts.
     * </pre>
     *
     * @param imageData the source image data to scale
     * @param width     the desired output width in pixels
     * @param height    the desired output height in pixels
     * @return a new {@link ImageData} at the requested dimensions
     */
    private ImageData resize( ImageData imageData, int width, int height )
    {
        Image image = new Image( Display.getDefault(), imageData );
        Image resizedImage = new Image( Display.getDefault(), width, height );

        try
        {
            GC gc = new GC( resizedImage );

            try
            {
                gc.setAntialias( SWT.ON );
                gc.setInterpolation( SWT.HIGH );
                gc.drawImage( image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, width, height );
            }
            finally
            {
                gc.dispose();
            }

            ImageData resizedImageData = resizedImage.getImageData();

            return resizedImageData;
        }
        finally
        {
            image.dispose();
            resizedImage.dispose();
        }
    }


    // ── Building The Metadata Readout Panel ──────────────────────────────────────
    // Next to R2's projection screen is a small status panel: format, byte count, pixel count.
    // The panel uses two columns — the label caption on the left, the live value on the right.
    // We create a flush two-column Composite to hold label/value pairs side by side.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a two-column {@link Composite} to hold label/value pairs that describe the image
     * (type, size, width, height).
     * Zero margins keep these readouts flush with the surrounding layout; the two-column grid
     * lines up keys and values cleanly without extra whitespace.
     *
     * <p>For example — R2 deploys his status readout panel beside the projection screen:</p>
     * <pre>
     *   Left column:  "Type:"  "Size:"  "Width:"  "Height:"
     *   Right column: values populated as each field is read from the image.
     * </pre>
     *
     * @param parent the composite to nest this panel inside
     * @return the new two-column {@link Composite}, filled horizontally
     */
    private Composite createImageInfoContainer( Composite parent )
    {
        Composite imageInfoContainer = new Composite( parent, SWT.NONE );
        GridLayout gl = new GridLayout( 2, false );
        gl.marginHeight = gl.marginWidth = 0;
        imageInfoContainer.setLayout( gl );
        imageInfoContainer.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        return imageInfoContainer;
    }


    // ── R2 Adds One Status Indicator To The Panel ─────────────────────────────────
    // R2 installs one labeled indicator: a fixed caption and a live value display beside it.
    // Each indicator covers one fact about the hologram — its format, byte count, or pixel count.
    // We create a Label for the caption and return a read-only Text for the value.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates one label/value pair inside a metadata info container.
     * The label string is the caption (e.g. "Image Type:") and the returned {@link Text}
     * widget displays the corresponding value, which we overwrite as images load or change.
     *
     * <p>For example — R2 installs the "Format" indicator on his status panel:</p>
     * <pre>
     *   Label: "Format:"     Value text: [ JPEG ]
     * </pre>
     *
     * @param parent the two-column composite that holds this pair
     * @param label  the caption string shown to the left of the value
     * @return the {@link Text} widget we should write the actual value into
     */
    private Text createImageInfo( Composite parent, String label )
    {
        BaseWidgetUtils.createLabel( parent, label, 1 );
        Text text = BaseWidgetUtils.createLabeledText( parent, "", 1 ); //$NON-NLS-1$

        return text;
    }


    // ── R2 Mounts A Physical Push-Button On His Panel ─────────────────────────────
    // R2's control panel has push-buttons for Save and Browse — one per action.
    // Each button is a simple labeled control whose style matches the rest of the UI.
    // We delegate to BaseWidgetUtils so the style is consistent with all Directory Studio buttons.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a standard push {@link Button} with the given label text.
     * We use {@link BaseWidgetUtils} so the button's style and font match every other button
     * in the Directory Studio UI — consistent with the Eclipse RCP look and feel.
     *
     * <p>For example — R2 adds a labeled push-button to his control panel:</p>
     * <pre>
     *   R2 installs: [ Save ]
     *   The button snaps into the grid, ready for Luke's thumb.
     * </pre>
     *
     * @param parent the composite that will contain this button
     * @param label  the button's display text
     * @return the new {@link Button} widget
     */
    private Button createButton( Composite parent, String label )
    {
        Button button = BaseWidgetUtils.createButton( parent, label, 1 );

        return button;
    }


    // ── R2 Reads The Data Crystal's Capacity ─────────────────────────────────────
    // R2 checks how large the holographic data crystal is and announces it in sensible units.
    // He picks megabytes, kilobytes, or raw bytes depending on the crystal's size.
    // We format the raw byte count into a human-readable string with the appropriate unit.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Formats a raw byte count into a human-readable size string such as "5 MB", "42 KB",
     * or "512 bytes".
     * We threshold at 1 000 000 for MB and 1 000 for KB, matching the convention already
     * used in the message bundle for this package.
     *
     * <p>For example — R2 reports the data crystal's capacity in friendly units:</p>
     * <pre>
     *   15 234 bytes  → "15 KB (15234 bytes)"
     *   2 500 000 bytes → "2 MB (2500000 bytes)"
     *   512 bytes     → "512 bytes"
     * </pre>
     *
     * @param length the raw size in bytes
     * @return a formatted string suitable for display in the size metadata field
     */
    private static String getSizeString( int length )
    {
        if ( length > 1000000 )
        {
            return ( length / 1000000 ) + NLS.bind( Messages.getString( "ImageDialog.MB" ), new Integer[] //$NON-NLS-1$
                { length } ); //$NON-NLS-1$
        }
        else if ( length > 1000 )
        {
            return ( length / 1000 ) + NLS.bind( Messages.getString( "ImageDialog.KB" ), new Integer[] //$NON-NLS-1$
                { length } ); //$NON-NLS-1$
        }
        else
        {
            return length + Messages.getString( "ImageDialog.Bytes" ); //$NON-NLS-1$
        }
    }


    // ── R2 Reads The Hologram's Header For The Crew ───────────────────────────────
    // Before projecting, R2 reads the format tag, width, height, and byte count from the header.
    // He composes a one-liner — "JPEG-320x240 (15234 bytes)" — for anyone who asks.
    // We decode just enough of the bytes to extract that metadata and format it for display.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Produces a compact, human-readable description of an LDAP jpegPhoto attribute value
     * for display in the attribute table without opening the full dialog.
     * We decode the image bytes to read the format code, width, and height, then format them
     * as a string like "JPEG-320x240 (15234 bytes)".
     * If decoding fails we fall back to an "invalid image" message with just the byte count.
     *
     * <p>For example — R2 reads a hologram header and announces the summary to the crew:</p>
     * <pre>
     *   R2: "Message: JPEG, 320 by 240 pixels, 15 234 bytes."
     *   Corrupted recording: "Invalid hologram — 15 234 bytes of noise."
     * </pre>
     *
     * @param imageRawData the raw bytes from the LDAP attribute; may be null
     * @return a display string describing the image, or {@link IValueEditor#NULL} if the data is null
     */
    public static String getImageInfo( byte[] imageRawData )
    {
        if ( imageRawData == null )
        {
            return IValueEditor.NULL;
        }

        String text;
        try
        {
            ByteArrayInputStream bais = new ByteArrayInputStream( imageRawData );
            ImageData imageData = new ImageData( bais );
            String typePrefix = getImageType( imageData.type );

            if ( !Strings.isEmpty( typePrefix ) ) //$NON-NLS-1$
            {
                typePrefix += "-"; //$NON-NLS-1$
            }

            text = NLS
                .bind(
                    Messages.getString( "ImageDialog.Image" ), new Object[] { typePrefix, imageData.width, imageData.height, imageRawData.length } ); //$NON-NLS-1$
        }
        catch ( SWTException swte )
        {
            text = NLS.bind( Messages.getString( "ImageDialog.InvalidImage" ), new Object[] { imageRawData.length } ); //$NON-NLS-1$
        }

        return text;
    }


    // ── R2 Identifies The Recording Format ───────────────────────────────────────
    // R2 checks his format registry to decode what kind of recording this crystal holds.
    // He translates the internal SWT integer code into a name people actually recognize.
    // We map SWT image-type constants to their human-readable format name strings.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Translates an SWT image-type constant (e.g. {@link SWT#IMAGE_JPEG}) into the
     * corresponding format name string (e.g. {@code "JPEG"}).
     * We need this because SWT stores image type as an opaque integer and the user
     * deserves to see something meaningful in the metadata readout, not a raw number.
     *
     * <p>For example — R2 looks up code 4 in his format registry:</p>
     * <pre>
     *   SWT.IMAGE_JPEG (4) → "JPEG"
     *   SWT.IMAGE_PNG  (5) → "PNG"
     *   Unknown code       → "" (empty string, not shown in the UI)
     * </pre>
     *
     * @param swtCode the SWT image-type constant to look up
     * @return the format name, or an empty string for unrecognized types
     */
    private static String getImageType( int swtCode )
    {
        switch ( swtCode )
        {
            case SWT.IMAGE_JPEG :
                return "JPEG"; //$NON-NLS-1$

            case SWT.IMAGE_GIF :
                return "GIF"; //$NON-NLS-1$

            case SWT.IMAGE_PNG :
                return "PNG"; //$NON-NLS-1$

            case SWT.IMAGE_BMP :
            case SWT.IMAGE_BMP_RLE :
                return "BMP"; //$NON-NLS-1$

            default :
                return "";
        }
    }


    // ── R2 Ejects The New Recording For The Caller ───────────────────────────────
    // After the user clicks OK, R2 pops the newly loaded data crystal out for whoever asked.
    // If the user cancelled or no valid image was loaded, R2 returns an empty slot.
    // We return the bytes already converted to the required LDAP format, or null if nothing changed.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the new image bytes in the format required by the LDAP attribute, or {@code null}
     * if the user cancelled or did not successfully select a valid image.
     * The caller — typically {@link ImageValueEditor} — uses this return value to decide whether
     * to write anything back to the directory entry.
     *
     * <p>For example — R2 ejects the converted recording after the user clicks OK:</p>
     * <pre>
     *   R2 pops the data crystal out: "Here is your JPEG, converted and ready."
     *   If no recording was loaded or the user cancelled: R2 returns an empty slot.
     * </pre>
     *
     * @return the new image bytes in the required format, or {@code null} if nothing was selected
     */
    public byte[] getNewImageRawData()
    {
        return newImageRawDataInRequiredFormat;
    }
}
