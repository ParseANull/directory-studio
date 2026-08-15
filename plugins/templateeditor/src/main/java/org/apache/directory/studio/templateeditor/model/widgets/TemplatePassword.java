/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor contract agreements.  See the NOTICE file
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


// ── CLASS: TemplatePassword — C-3PO DECODING A CLASSIFIED COMMUNIQUÉ ─────────────
// In the Imperial communiqué, a password directive is marked classified: the value
// is masked by default, the user needs to click "Edit..." to open a secure change
// dialog, and a "Show Password" checkbox can reveal the text if needed. C-3PO
// records these three flags so the editor widget knows exactly how to render the
// secured field.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Model object for a template password widget. Stores three visibility flags:
 * whether the password is displayed as bullets (hidden), whether an "Edit..."
 * button should appear to open a change-password dialog, and whether a
 * "Show Password" checkbox should be rendered.
 *
 * <p>Think of this as a C-3PO-decoded classified communiqué directive:</p>
 * <pre>
 *   password.setHidden( true );
 *   password.setShowEditButton( true );
 *   password.setShowShowPasswordCheckbox( true );
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplatePassword extends AbstractTemplateWidget
{
    /** The default hidden flag — passwords are masked (bullet characters) by default. */
    public static boolean DEFAULT_HIDDEN = true;

    /** The default show-Edit-button flag — show it by default. */
    public static boolean DEFAULT_SHOW_EDIT_BUTTON = true;

    /** The default show-Show-Password-checkbox flag — show it by default. */
    public static boolean DEFAULT_SHOW_PASSWORD_CHECKBOX = true;

    /** The flag which indicated if the password should be hidden */
    private boolean hidden = DEFAULT_HIDDEN;

    /** The flag which indicated if a "<em>Edit...</em>" button should be shown */
    private boolean showEditButton = DEFAULT_SHOW_EDIT_BUTTON;

    /** The flag which indicated if a "<em>Show Password</em>" checkbox should be shown */
    private boolean showShowPasswordCheckbox = DEFAULT_SHOW_PASSWORD_CHECKBOX;


    // ── CONSTRUCTOR: REGISTER THE PASSWORD COMMUNIQUÉ ────────────────────────────
    // C-3PO receives a new classified directive and files it inside the parent
    // communiqué. The template parser sets the three flags from the XML.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplatePassword} and registers it as a child of the
     * given parent widget.
     *
     * @param parent  the enclosing widget (composite, section, or form)
     */
    public TemplatePassword( TemplateWidget parent )
    {
        super( parent );
    }


    // ── IS HIDDEN: DISPLAY AS BULLET CHARACTERS ───────────────────────────────────
    // C-3PO checks whether the classified communiqué should have its content
    // masked — rendered as bullet characters rather than plain text.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the password should be displayed as bullet characters
     * (hidden), {@code false} to show it in plain text.
     *
     * @return the hidden flag; defaults to {@code true}
     */
    public boolean isHidden()
    {
        return hidden;
    }


    // ── IS SHOW EDIT BUTTON: OPEN THE CHANGE-PASSWORD DIALOG ─────────────────────
    // C-3PO checks whether the communiqué requests an "Edit..." button that opens
    // a dedicated change-password dialog.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if an "Edit..." button should appear to open the
     * change-password dialog.
     *
     * @return the show-Edit flag; defaults to {@code true}
     */
    public boolean isShowEditButton()
    {
        return showEditButton;
    }


    // ── IS SHOW SHOW PASSWORD CHECKBOX: TOGGLE VISIBILITY ────────────────────────
    // C-3PO checks whether the communiqué requests a "Show Password" checkbox
    // that lets the user toggle between masked and plain-text display.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if a "Show Password" checkbox should be rendered,
     * allowing the user to toggle between masked and plain-text display.
     *
     * @return the show-Show-Password flag; defaults to {@code true}
     */
    public boolean isShowShowPasswordCheckbox()
    {
        return showShowPasswordCheckbox;
    }


    // ── SET HIDDEN ────────────────────────────────────────────────────────────────
    /**
     * Sets whether the password is displayed as bullet characters.
     *
     * @param hidden  {@code true} to mask the password; {@code false} to show plain text
     */
    public void setHidden( boolean hidden )
    {
        this.hidden = hidden;
    }


    // ── SET SHOW EDIT BUTTON ──────────────────────────────────────────────────────
    /**
     * Sets whether an "Edit..." button appears to open the change-password dialog.
     *
     * @param showEditButton  {@code true} to show the button; {@code false} to hide it
     */
    public void setShowEditButton( boolean showEditButton )
    {
        this.showEditButton = showEditButton;
    }


    // ── SET SHOW SHOW PASSWORD CHECKBOX ───────────────────────────────────────────
    /**
     * Sets whether a "Show Password" checkbox is rendered.
     *
     * @param showShowPasswordCheckbox  {@code true} to show the checkbox; {@code false} to hide it
     */
    public void setShowShowPasswordCheckbox( boolean showShowPasswordCheckbox )
    {
        this.showShowPasswordCheckbox = showShowPasswordCheckbox;
    }
}
