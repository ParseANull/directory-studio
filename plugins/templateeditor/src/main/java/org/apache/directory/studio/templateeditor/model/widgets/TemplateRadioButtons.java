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


// ── CLASS: TemplateRadioButtons — C-3PO DECODING A SINGLE-CHOICE COMMUNIQUÉ ──────
// In the Imperial communiqué, a radio-buttons directive presents a fixed set of
// mutually exclusive choices — the user picks exactly one. C-3PO reads the enabled
// flag and the list of label-value pairs (one per button), where the label is what
// the user sees and the value is what gets stored in the LDAP attribute.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Model object for a template radio-buttons widget. Stores the enabled/disabled
 * flag and the ordered list of {@link ValueItem}s — one per radio button — where
 * each item's label is displayed on screen and its value is written to the LDAP
 * attribute when that button is selected.
 *
 * <p>Think of this as a C-3PO-decoded single-choice communiqué directive:</p>
 * <pre>
 *   radios.setEnabled( true );
 *   radios.addButton( new ValueItem( "Yes", "TRUE"  ) );
 *   radios.addButton( new ValueItem( "No",  "FALSE" ) );
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateRadioButtons extends AbstractTemplateWidget
{
    /** The default enabled flag — radio buttons start enabled. */
    public static boolean DEFAULT_ENABLED = true;

    /** The enable flag */
    private boolean enabled = DEFAULT_ENABLED;

    /** The list of buttons */
    private List<ValueItem> buttons = new ArrayList<ValueItem>();


    // ── CONSTRUCTOR: REGISTER THE RADIO-BUTTONS COMMUNIQUÉ ───────────────────────
    // C-3PO receives a new single-choice directive and files it inside the parent
    // communiqué. The template parser will add button items and set the enabled
    // flag from the XML.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplateRadioButtons} and registers it as a child of
     * the given parent widget.
     *
     * @param parent  the enclosing widget (composite, section, or form)
     */
    public TemplateRadioButtons( TemplateWidget parent )
    {
        super( parent );
    }


    // ── ADD BUTTON: APPEND A RADIO BUTTON ─────────────────────────────────────────
    // C-3PO appends a new label-value entry to the button list. Each call typically
    // corresponds to one {@code <button>} element in the XML.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Appends a {@link ValueItem} representing one radio button (label + stored value).
     *
     * @param button  the button's label-value pair
     * @return {@code true} as per {@link java.util.List#add(Object)}
     */
    public boolean addButton( ValueItem button )
    {
        return buttons.add( button );
    }


    // ── GET BUTTONS: ALL RADIO BUTTON DEFINITIONS ─────────────────────────────────
    // C-3PO returns the complete list of button definitions from the communiqué so
    // the editor widget can create one SWT radio button per item.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the ordered list of {@link ValueItem}s, one per radio button.
     *
     * @return the buttons list; may be empty if none are configured
     */
    public List<ValueItem> getButtons()
    {
        return buttons;
    }


    // ── IS ENABLED: CHECK THE INTERACTIVE FLAG ────────────────────────────────────
    // C-3PO checks whether the communiqué marks these radio buttons as interactive
    // or read-only (greyed out).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the radio buttons are interactive (enabled).
     *
     * @return the enabled flag; defaults to {@code true}
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    // ── SET BUTTONS: REPLACE THE BUTTON LIST ──────────────────────────────────────
    /**
     * Replaces the entire list of radio button definitions.
     *
     * @param buttons  the new list of {@link ValueItem}s
     */
    public void setButtons( List<ValueItem> buttons )
    {
        this.buttons = buttons;
    }


    // ── SET ENABLED ───────────────────────────────────────────────────────────────
    /**
     * Enables or disables the radio buttons.
     *
     * @param enabled  {@code true} to make them interactive; {@code false} to grey them out
     */
    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;
    }
}
