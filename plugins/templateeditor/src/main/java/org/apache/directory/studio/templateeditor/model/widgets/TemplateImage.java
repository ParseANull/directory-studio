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
package org.apache.directory.studio.templateeditor.model.widgets;


// ── CLASS: TemplateImage — C-3PO DECODING AN IMAGE-DISPLAY COMMUNIQUÉ ────────────
// In the Imperial communiqué, an image directive tells C-3PO that this LDAP
// attribute holds raw image bytes (e.g. jpegPhoto). He notes the optional
// placeholder image data for when no photo is stored, the preferred display size,
// and which action buttons (Save As, Clear, Browse) should appear in the toolbar.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Model object for a template image widget. Stores the placeholder image data,
 * display dimensions, and visibility flags for the Save As, Clear, and Browse
 * toolbar buttons. The widget binds to a binary LDAP attribute that contains
 * raw image bytes (e.g. {@code jpegPhoto}).
 *
 * <p>Think of this as a C-3PO-decoded image-display communiqué directive:</p>
 * <pre>
 *   image.setImageData( "data:image/png;base64,..." ); // placeholder
 *   image.setImageWidth( 200 );
 *   image.setImageHeight( 200 );
 *   image.setShowBrowseButton( true );
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateImage extends AbstractTemplateWidget
{
    /** The default image data — {@code null} means show nothing when no image is stored. */
    public static String DEFAULT_IMAGE_DATA = null;

    /** The default show-Save As-button flag — show it by default. */
    public static boolean DEFAULT_SHOW_SAVE_AS_BUTTON = true;

    /** The default show-Clear-button flag — show it by default. */
    public static boolean DEFAULT_SHOW_CLEAR_BUTTON = true;

    /** The default show-Browse-button flag — show it by default. */
    public static boolean DEFAULT_SHOW_BROWSE_BUTTON = true;

    /** The image data */
    private String imageData = DEFAULT_IMAGE_DATA;

    /** The flag which indicates if a "<em>Save As...</em>" button should be shown */
    private boolean showSaveAsButton = DEFAULT_SHOW_SAVE_AS_BUTTON;

    /** The flag which indicates if a "<em>Clear</em>" button should be shown */
    private boolean showClearButton = DEFAULT_SHOW_CLEAR_BUTTON;

    /** The flag which indicates if a "<em>Browse...</em>" button should be shown */
    private boolean showBrowseButton = DEFAULT_SHOW_BROWSE_BUTTON;

    /** The width of the image */
    private int imageWidth = TemplateWidget.DEFAULT_SIZE;

    /** The height of the image */
    private int imageHeight = TemplateWidget.DEFAULT_SIZE;


    // ── CONSTRUCTOR: REGISTER THE IMAGE COMMUNIQUÉ ───────────────────────────────
    // C-3PO receives a new image-display directive and files it inside the parent
    // communiqué. The template parser will set image data, dimensions, and button
    // flags from the XML.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplateImage} and registers it as a child of the
     * given parent widget.
     *
     * @param parent  the enclosing widget (composite, section, or form)
     */
    public TemplateImage( TemplateWidget parent )
    {
        super( parent );
    }


    // ── GET IMAGE HEIGHT: RETRIEVE THE DISPLAY HEIGHT ─────────────────────────────
    // C-3PO reads the height specification from the communiqué — the maximum
    // number of pixels the image should occupy vertically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the preferred display height of the image in pixels, or
     * {@link TemplateWidget#DEFAULT_SIZE} ({@code -1}) if no explicit height is set.
     *
     * @return display height in pixels, or {@code -1} for no preference
     */
    public int getImageHeight()
    {
        return imageHeight;
    }


    // ── GET IMAGE DATA: PLACEHOLDER OR DEFAULT IMAGE ──────────────────────────────
    // C-3PO reads the placeholder image data field — shown when the LDAP attribute
    // contains no image yet.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the placeholder image data (a base-64 encoded data URI or a
     * plugin-relative path) shown when the bound LDAP attribute has no value.
     *
     * @return the placeholder image data, or {@code null} for no placeholder
     */
    public String getImageData()
    {
        return imageData;
    }


    // ── GET IMAGE WIDTH: RETRIEVE THE DISPLAY WIDTH ───────────────────────────────
    // C-3PO reads the width specification from the communiqué.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the preferred display width of the image in pixels, or
     * {@link TemplateWidget#DEFAULT_SIZE} ({@code -1}) if no explicit width is set.
     *
     * @return display width in pixels, or {@code -1} for no preference
     */
    public int getImageWidth()
    {
        return imageWidth;
    }


    // ── IS SHOW BROWSE BUTTON ─────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the "Browse..." button should appear in the toolbar.
     *
     * @return the show-Browse flag; defaults to {@code true}
     */
    public boolean isShowBrowseButton()
    {
        return showBrowseButton;
    }


    // ── IS SHOW CLEAR BUTTON ──────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the "Clear" button should appear in the toolbar.
     *
     * @return the show-Clear flag; defaults to {@code true}
     */
    public boolean isShowClearButton()
    {
        return showClearButton;
    }


    // ── IS SHOW SAVE AS BUTTON ────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the "Save As..." button should appear in the toolbar.
     *
     * @return the show-Save As flag; defaults to {@code true}
     */
    public boolean isShowSaveAsButton()
    {
        return showSaveAsButton;
    }


    // ── SET IMAGE HEIGHT ──────────────────────────────────────────────────────────
    /**
     * Sets the preferred display height in pixels. Use {@link TemplateWidget#DEFAULT_SIZE}
     * ({@code -1}) to leave it at the widget's own default.
     *
     * @param imageHeight  height in pixels, or {@code -1} for no preference
     */
    public void setImageHeight( int imageHeight )
    {
        this.imageHeight = imageHeight;
    }


    // ── SET IMAGE DATA ────────────────────────────────────────────────────────────
    /**
     * Sets the placeholder image data shown when no LDAP attribute value is present.
     *
     * @param imageData  a data URI or plugin-relative path, or {@code null} for none
     */
    public void setImageData( String imageData )
    {
        this.imageData = imageData;
    }


    // ── SET SHOW BROWSE BUTTON ────────────────────────────────────────────────────
    /**
     * Sets whether the "Browse..." button appears in the toolbar.
     *
     * @param showBrowseButton  {@code true} to show; {@code false} to hide
     */
    public void setShowBrowseButton( boolean showBrowseButton )
    {
        this.showBrowseButton = showBrowseButton;
    }


    // ── SET SHOW CLEAR BUTTON ─────────────────────────────────────────────────────
    /**
     * Sets whether the "Clear" button appears in the toolbar.
     *
     * @param showClearButton  {@code true} to show; {@code false} to hide
     */
    public void setShowClearButton( boolean showClearButton )
    {
        this.showClearButton = showClearButton;
    }


    // ── SET SHOW SAVE AS BUTTON ───────────────────────────────────────────────────
    /**
     * Sets whether the "Save As..." button appears in the toolbar.
     *
     * @param showSaveAsButton  {@code true} to show; {@code false} to hide
     */
    public void setShowSaveAsButton( boolean showSaveAsButton )
    {
        this.showSaveAsButton = showSaveAsButton;
    }


    // ── SET IMAGE WIDTH ───────────────────────────────────────────────────────────
    /**
     * Sets the preferred display width in pixels. Use {@link TemplateWidget#DEFAULT_SIZE}
     * ({@code -1}) to leave it at the widget's own default.
     *
     * @param imageWidth  width in pixels, or {@code -1} for no preference
     */
    public void setImageWidth( int imageWidth )
    {
        this.imageWidth = imageWidth;
    }
}
