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


import java.util.ArrayList;
import java.util.List;


// ── CLASS: TemplateListbox — C-3PO DECODING A MULTI-ITEM COMMUNIQUÉ ──────────────
// In the Imperial communiqué, a listbox directive gives C-3PO a scrollable list of
// choices. He notes whether the listbox is interactive or read-only, whether the
// user can select multiple items at once, and the list of label-value pairs to
// populate the box. Each pair is a {@link ValueItem} — the label the user sees and
// the raw value stored in the LDAP attribute.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Model object for a template listbox widget. Stores the enabled/disabled flag,
 * whether multiple selections are allowed, and the ordered list of
 * {@link ValueItem}s that populate the listbox.
 *
 * <p>Think of this as a C-3PO-decoded multi-item communiqué directive:</p>
 * <pre>
 *   listbox.setMultipleSelection( false );
 *   listbox.addValue( new ValueItem( "Active",   "TRUE"  ) );
 *   listbox.addValue( new ValueItem( "Inactive", "FALSE" ) );
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateListbox extends AbstractTemplateWidget
{
    /** The default multiple-selection flag — allow multi-select by default. */
    public static boolean DEFAULT_MULTIPLE_SELECTION = true;

    /** The default enabled flag — listboxes start enabled. */
    public static boolean DEFAULT_ENABLED = true;

    /** The enabled flag */
    private boolean enabled = DEFAULT_ENABLED;

    /** The flag which indicates if the listbox allows multiple selection */
    private boolean multipleSelection = DEFAULT_MULTIPLE_SELECTION;

    /** The list of value items */
    private List<ValueItem> items = new ArrayList<ValueItem>();


    // ── CONSTRUCTOR: REGISTER THE LISTBOX COMMUNIQUÉ ──────────────────────────────
    // C-3PO receives a new multi-item directive and files it inside the parent
    // communiqué. The template parser will add items and set flags from the XML.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplateListbox} and registers it as a child of the
     * given parent widget.
     *
     * @param parent  the enclosing widget (composite, section, or form)
     */
    public TemplateListbox( TemplateWidget parent )
    {
        super( parent );
    }


    // ── ADD VALUE: APPEND AN ITEM TO THE LIST ─────────────────────────────────────
    // C-3PO appends a new label-value entry to the listbox's choice list. Each
    // call typically corresponds to one {@code <value>} element in the XML.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Appends a {@link ValueItem} to the listbox's list of choices.
     *
     * @param value  the label-value pair to add
     * @return {@code true} as per {@link java.util.List#add(Object)}
     */
    public boolean addValue( ValueItem value )
    {
        return items.add( value );
    }


    // ── GET ITEMS: ALL LISTBOX CHOICES ────────────────────────────────────────────
    // C-3PO returns the complete list of choices from the communiqué so the editor
    // widget can populate the SWT List control.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the ordered list of {@link ValueItem}s populating this listbox.
     *
     * @return the items list; may be empty if none are configured
     */
    public List<ValueItem> getItems()
    {
        return items;
    }


    // ── IS ENABLED: CHECK THE INTERACTIVE FLAG ────────────────────────────────────
    // C-3PO checks whether the communiqué marks this listbox as interactive or
    // read-only.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the listbox is interactive, {@code false} if it
     * should appear greyed-out.
     *
     * @return the enabled flag; defaults to {@code true}
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    // ── IS MULTIPLE SELECTION: SINGLE OR MULTI-SELECT ─────────────────────────────
    // C-3PO checks whether the communiqué allows the user to select more than one
    // item at a time.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the user may select more than one item simultaneously.
     *
     * @return the multiple-selection flag; defaults to {@code true}
     */
    public boolean isMultipleSelection()
    {
        return multipleSelection;
    }


    // ── SET ENABLED ───────────────────────────────────────────────────────────────
    /**
     * Enables or disables the listbox.
     *
     * @param enabled  {@code true} to make it interactive; {@code false} to grey it out
     */
    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;
    }


    // ── SET ITEMS: REPLACE THE CHOICE LIST ────────────────────────────────────────
    /**
     * Replaces the entire list of choices.
     *
     * @param items  the new list of {@link ValueItem}s
     */
    public void setItems( List<ValueItem> items )
    {
        this.items = items;
    }


    // ── SET MULTIPLE SELECTION ────────────────────────────────────────────────────
    /**
     * Sets whether the user may select more than one item at a time.
     *
     * @param multipleSelection  {@code true} for multi-select; {@code false} for single-select
     */
    public void setMultipleSelection( boolean multipleSelection )
    {
        this.multipleSelection = multipleSelection;
    }
}
