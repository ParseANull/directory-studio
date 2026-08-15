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


// ── CLASS: TemplateTable — C-3PO DECODING A MULTI-VALUE TABLE COMMUNIQUÉ ─────────
// In the Imperial communiqué, a table directive represents a multi-valued LDAP
// attribute where each value appears as a row in a scrollable table. C-3PO reads
// three button flags — Add, Edit, Delete — that control which operations the user
// can perform on the rows. All three default to true: full CRUD for the attribute.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Model object for a template table widget. Tables display multi-valued LDAP
 * attributes — each value is a row — and expose Add, Edit, and Delete buttons
 * for managing those values. The three flags control which buttons appear.
 *
 * <p>Think of this as a C-3PO-decoded multi-value table communiqué directive:</p>
 * <pre>
 *   table.setShowAddButton( true );
 *   table.setShowEditButton( true );
 *   table.setShowDeleteButton( false ); // read-only rows, but can add
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateTable extends AbstractTemplateWidget
{
    /** The default show-Add-button flag — show it by default. */
    public static boolean DEFAULT_SHOW_ADD_BUTTON = true;

    /** The default show-Edit-button flag — show it by default. */
    public static boolean DEFAULT_SHOW_EDIT_BUTTON = true;

    /** The default show-Delete-button flag — show it by default. */
    public static boolean DEFAULT_SHOW_DELETE_BUTTON = true;

    /** The flag which indicated if a "<em>Add...</em>" button should be shown */
    private boolean showAddButton = DEFAULT_SHOW_ADD_BUTTON;

    /** The flag which indicated if a "<em>Edit...</em>" button should be shown */
    private boolean showEditButton = DEFAULT_SHOW_EDIT_BUTTON;

    /** The flag which indicated if a "<em>Delete...</em>" button should be shown */
    private boolean showDeleteButton = DEFAULT_SHOW_DELETE_BUTTON;


    // ── CONSTRUCTOR: REGISTER THE TABLE COMMUNIQUÉ ────────────────────────────────
    // C-3PO receives a new multi-value table directive and files it inside the
    // parent communiqué. The template parser overrides button flags from the XML
    // if they are explicitly specified.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplateTable} and registers it as a child of the
     * given parent widget.
     *
     * @param parent  the enclosing widget (composite, section, or form)
     */
    public TemplateTable( TemplateWidget parent )
    {
        super( parent );
    }


    // ── IS SHOW ADD BUTTON ────────────────────────────────────────────────────────
    // C-3PO checks whether the communiqué requests an "Add..." button to insert
    // a new value row into the table.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if an "Add..." button should appear above the table,
     * allowing the user to add a new attribute value.
     *
     * @return the show-Add flag; defaults to {@code true}
     */
    public boolean isShowAddButton()
    {
        return showAddButton;
    }


    // ── IS SHOW DELETE BUTTON ─────────────────────────────────────────────────────
    // C-3PO checks whether the communiqué requests a "Delete..." button to remove
    // the selected row.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if a "Delete..." button should appear, allowing the
     * user to remove the currently selected attribute value.
     *
     * @return the show-Delete flag; defaults to {@code true}
     */
    public boolean isShowDeleteButton()
    {
        return showDeleteButton;
    }


    // ── IS SHOW EDIT BUTTON ───────────────────────────────────────────────────────
    // C-3PO checks whether the communiqué requests an "Edit..." button to modify
    // the selected row.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if an "Edit..." button should appear, allowing the
     * user to modify the currently selected attribute value.
     *
     * @return the show-Edit flag; defaults to {@code true}
     */
    public boolean isShowEditButton()
    {
        return showEditButton;
    }


    // ── SET SHOW ADD BUTTON ───────────────────────────────────────────────────────
    /**
     * Sets whether an "Add..." button appears above the table.
     *
     * @param showAddButton  {@code true} to show; {@code false} to hide
     */
    public void setShowAddButton( boolean showAddButton )
    {
        this.showAddButton = showAddButton;
    }


    // ── SET SHOW DELETE BUTTON ────────────────────────────────────────────────────
    /**
     * Sets whether a "Delete..." button appears beside the table.
     *
     * @param showDeleteButton  {@code true} to show; {@code false} to hide
     */
    public void setShowDeleteButton( boolean showDeleteButton )
    {
        this.showDeleteButton = showDeleteButton;
    }


    // ── SET SHOW EDIT BUTTON ──────────────────────────────────────────────────────
    /**
     * Sets whether an "Edit..." button appears beside the table.
     *
     * @param showEditButton  {@code true} to show; {@code false} to hide
     */
    public void setShowEditButton( boolean showEditButton )
    {
        this.showEditButton = showEditButton;
    }
}
