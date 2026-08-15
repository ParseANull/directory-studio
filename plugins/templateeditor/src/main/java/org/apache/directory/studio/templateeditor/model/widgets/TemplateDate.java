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


// ── CLASS: TemplateDate — C-3PO DECODING A TIMESTAMP COMMUNIQUÉ ──────────────────
// In the Imperial communiqué, a date directive tells C-3PO how to render an LDAP
// GeneralizedTime attribute — which display format to use (e.g. "yyyy-MM-dd") and
// whether to show an "Edit..." button so the user can open a date picker. The raw
// value is always stored as LDAP GeneralizedTime in the directory itself.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Model object for a template date widget. Stores the display format for
 * rendering an LDAP GeneralizedTime attribute and whether an "Edit..." button
 * should appear so the user can open an interactive date picker.
 *
 * <p>Think of this as a C-3PO-decoded timestamp directive:</p>
 * <pre>
 *   date.setFormat( "yyyy-MM-dd HH:mm:ss" );
 *   date.setShowEditButton( true );
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateDate extends AbstractTemplateWidget
{
    /** The default format — {@code null} means use the locale default. */
    public static String DEFAULT_FORMAT = null;

    /** The default for the show-edit-button flag — show it by default. */
    public static boolean DEFAULT_SHOW_EDIT_BUTTON = true;

    /** The format value */
    private String format = DEFAULT_FORMAT;

    /** The flag which indicates if an "<em>Edit...</em>" button should be shown */
    private boolean showEditButton = DEFAULT_SHOW_EDIT_BUTTON;


    // ── CONSTRUCTOR: REGISTER THE DATE COMMUNIQUÉ ────────────────────────────────
    // C-3PO receives a new timestamp directive and files it inside the parent
    // communiqué. The template parser will set format and showEditButton if they
    // are specified in the XML.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplateDate} and registers it as a child of the
     * given parent widget.
     *
     * @param parent  the enclosing widget (composite, section, or form)
     */
    public TemplateDate( TemplateWidget parent )
    {
        super( parent );
    }


    // ── GET FORMAT: THE DATE DISPLAY PATTERN ──────────────────────────────────────
    // C-3PO reads the format string from the communiqué — a Java SimpleDateFormat
    // pattern that controls how the GeneralizedTime value is rendered for the user.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Java {@code SimpleDateFormat} pattern used to display the date.
     * A {@code null} value means the editor should use a locale-appropriate default.
     *
     * @return the format string, or {@code null} for locale default
     */
    public String getFormat()
    {
        return format;
    }


    // ── IS SHOW EDIT BUTTON: SHOW OR HIDE THE DATE PICKER BUTTON ─────────────────
    // C-3PO checks whether the communiqué requests an "Edit..." button that opens
    // an interactive date-picker dialog.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if an "Edit..." button should be rendered next to
     * the date display, opening an interactive date-picker dialog when clicked.
     *
     * @return the show-edit-button flag; defaults to {@code true}
     */
    public boolean isShowEditButton()
    {
        return showEditButton;
    }


    // ── SET FORMAT ────────────────────────────────────────────────────────────────
    /**
     * Sets the Java {@code SimpleDateFormat} pattern for rendering the date.
     * Pass {@code null} to use the locale default.
     *
     * @param format  a SimpleDateFormat pattern, or {@code null} for the default
     */
    public void setFormat( String format )
    {
        this.format = format;
    }


    // ── SET SHOW EDIT BUTTON ──────────────────────────────────────────────────────
    /**
     * Sets whether an "Edit..." button should appear next to the date display.
     *
     * @param showEditButton  {@code true} to show the button; {@code false} to hide it
     */
    public void setShowEditButton( boolean showEditButton )
    {
        this.showEditButton = showEditButton;
    }
}
