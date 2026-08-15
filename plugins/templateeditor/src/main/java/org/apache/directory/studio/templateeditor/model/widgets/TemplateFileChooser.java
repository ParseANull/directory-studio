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


import java.util.HashSet;
import java.util.Set;


// ── CLASS: TemplateFileChooser — C-3PO DECODING A BINARY-FILE COMMUNIQUÉ ─────────
// In the Imperial communiqué, a file-chooser directive tells C-3PO that this LDAP
// attribute holds binary file data. The directive specifies an icon path, which
// file extensions are allowed in the browser dialog, and which action buttons
// (Save As, Clear, Browse) should appear in the editor widget's toolbar.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Model object for a template file-chooser widget. Stores the icon image path,
 * the set of allowed file extensions, and the visibility flags for the Save As,
 * Clear, and Browse toolbar buttons. The widget binds to an LDAP binary attribute
 * (e.g. {@code jpegPhoto}, {@code userCertificate}).
 *
 * <p>Think of this as a C-3PO-decoded binary-file communiqué directive:</p>
 * <pre>
 *   fileChooser.setIcon( "icons/document.gif" );
 *   fileChooser.addExtension( "pdf" );
 *   fileChooser.setShowBrowseButton( true );
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateFileChooser extends AbstractTemplateWidget
{
    /** The default icon — {@code null} means no icon is shown unless configured. */
    public static String DEFAULT_ICON = null;

    /** The default show-icon flag — the icon area is shown by default. */
    public static boolean DEFAULT_SHOW_ICON = true;

    /** The default show-Save As-button flag — show it by default. */
    public static boolean DEFAULT_SHOW_SAVE_AS_BUTTON = true;

    /** The default show-Clear-button flag — show it by default. */
    public static boolean DEFAULT_SHOW_CLEAR_BUTTON = true;

    /** The default show-Browse-button flag — show it by default. */
    public static boolean DEFAULT_SHOW_BROWSE_BUTTON = true;

    /** The icon */
    private String icon = DEFAULT_ICON;

    /** The set of extensions for the file */
    private Set<String> extensions = new HashSet<String>();

    /** The flag which indicates if an icon should be shown */
    private boolean showIcon = DEFAULT_SHOW_ICON;

    /** The flag which indicates if a "<em>Save As...</em>" button should be shown */
    private boolean showSaveAsButton = DEFAULT_SHOW_SAVE_AS_BUTTON;

    /** The flag which indicates if a "<em>Clear</em>" button should be shown */
    private boolean showClearButton = DEFAULT_SHOW_CLEAR_BUTTON;

    /** The flag which indicates if a "<em>Browse...</em>" button should be shown */
    private boolean showBrowseButton = DEFAULT_SHOW_BROWSE_BUTTON;


    // ── CONSTRUCTOR: REGISTER THE FILE-CHOOSER COMMUNIQUÉ ────────────────────────
    // C-3PO receives a new binary-file directive and files it inside the parent
    // communiqué. The template parser will populate icon, extensions, and button
    // flags from the XML.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplateFileChooser} and registers it as a child of
     * the given parent widget.
     *
     * @param parent  the enclosing widget (composite, section, or form)
     */
    public TemplateFileChooser( TemplateWidget parent )
    {
        super( parent );
    }


    // ── ADD EXTENSION: REGISTER AN ALLOWED FILE EXTENSION ────────────────────────
    // C-3PO appends an extension to the allowed-types list in the communiqué.
    // The browser dialog uses this list to filter which files the user can pick.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a file extension to the set of allowed types shown in the file browser
     * dialog (e.g. {@code "pdf"}, {@code "png"}). Duplicates are silently ignored.
     *
     * @param extension  the extension string without a leading dot
     * @return {@code true} if the extension was not already in the set
     */
    public boolean addExtension( String extension )
    {
        return extensions.add( extension );
    }


    // ── GET EXTENSIONS: ALL ALLOWED FILE EXTENSIONS ───────────────────────────────
    // C-3PO returns the full set of allowed file-type filters from the communiqué.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the set of file extensions the browse dialog should filter to.
     *
     * @return the extension set; may be empty if no filter is configured
     */
    public Set<String> getExtensions()
    {
        return extensions;
    }


    // ── GET ICON: THE ICON IMAGE PATH ─────────────────────────────────────────────
    // C-3PO reads the icon path from the communiqué — a plugin-relative path used
    // to load and display a small image representing the file type.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the plugin-relative path to the icon image displayed in the widget.
     *
     * @return the icon path, or {@code null} if no icon is configured
     */
    public String getIcon()
    {
        return icon;
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


    // ── IS SHOW ICON ──────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the icon area should be rendered next to the file
     * size label.
     *
     * @return the show-icon flag; defaults to {@code true}
     */
    public boolean isShowIcon()
    {
        return showIcon;
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


    // ── SET EXTENSIONS ────────────────────────────────────────────────────────────
    /**
     * Replaces the entire set of allowed file extensions.
     *
     * @param extensions  the new set of extension strings
     */
    public void setExtensions( Set<String> extensions )
    {
        this.extensions = extensions;
    }


    // ── SET ICON ──────────────────────────────────────────────────────────────────
    /**
     * Sets the plugin-relative path to the icon image for this file-chooser.
     *
     * @param icon  the icon path, or {@code null} to show no icon
     */
    public void setIcon( String icon )
    {
        this.icon = icon;
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


    // ── SET SHOW ICON ─────────────────────────────────────────────────────────────
    /**
     * Sets whether the icon area is rendered.
     *
     * @param showIcon  {@code true} to show the icon; {@code false} to hide it
     */
    public void setShowIcon( boolean showIcon )
    {
        this.showIcon = showIcon;
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
}
