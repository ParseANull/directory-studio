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


// ── CLASS: TemplateCheckbox — C-3PO DECODING A BINARY-CHOICE COMMUNIQUÉ ──────────
// In the Imperial communiqué, a checkbox directive is the simplest binary choice:
// checked means one string value gets written to the LDAP attribute; unchecked
// means another. C-3PO decodes the directive's label (what the user sees), the
// enabled flag (whether it's interactive), and the two possible stored values.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Model object for a template checkbox widget. Stores the display label, the
 * enabled/disabled flag, and the two LDAP attribute values that correspond to
 * checked and unchecked states.
 *
 * <p>Think of this as a C-3PO-decoded binary-choice communiqué directive:</p>
 * <pre>
 *   checkbox.setLabel( "Active account?" );
 *   checkbox.setCheckedValue( "TRUE" );
 *   checkbox.setUncheckedValue( "FALSE" );
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateCheckbox extends AbstractTemplateWidget
{
    /** The default enabled value — checkboxes start enabled. */
    public static boolean DEFAULT_ENABLED = true;

    /** The default label — empty string so the checkbox renders without a label if none is set. */
    public static String DEFAULT_LABEL = ""; //$NON-NLS-1$

    /** The default value written when the checkbox is checked — {@code null} means not configured. */
    public static String DEFAULT_CHECKED_VALUE = null;

    /** The default value written when the checkbox is unchecked — {@code null} means not configured. */
    public static String DEFAULT_UNCHECKED_VALUE = null;

    /** The label associated with the checkbox */
    private String label = DEFAULT_LABEL;

    /** The enabled flag */
    private boolean enabled = DEFAULT_ENABLED;

    /** The value when the checkbox is checked */
    private String checkedValue = DEFAULT_CHECKED_VALUE;

    /** The value when the checkbox is unchecked */
    private String uncheckedValue = DEFAULT_UNCHECKED_VALUE;


    // ── CONSTRUCTOR: REGISTER THE CHECKBOX COMMUNIQUÉ ────────────────────────────
    // C-3PO receives a new binary-choice directive and files it inside the parent
    // communiqué. Default values are applied; the template parser fills in the
    // specifics afterwards.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplateCheckbox} and registers it as a child of the
     * given parent widget.
     *
     * @param parent  the enclosing widget (composite, section, or form)
     */
    public TemplateCheckbox( TemplateWidget parent )
    {
        super( parent );
    }


    // ── GET CHECKED VALUE: WHAT GETS STORED WHEN CHECKED ─────────────────────────
    // C-3PO reads the "checked" field from the directive — the string that is
    // written to the LDAP attribute when the user ticks the box.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP attribute value written when the checkbox is checked.
     *
     * @return the checked value string, or {@code null} if not configured
     */
    public String getCheckedValue()
    {
        return checkedValue;
    }


    // ── GET LABEL: THE DISPLAY TEXT BESIDE THE CHECKBOX ──────────────────────────
    // C-3PO reads the label field — the human-readable text the user sees next
    // to the checkbox.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display label shown next to the checkbox in the editor.
     *
     * @return the label string; defaults to an empty string
     */
    public String getLabel()
    {
        return label;
    }


    // ── GET UNCHECKED VALUE: WHAT GETS STORED WHEN UNCHECKED ─────────────────────
    // C-3PO reads the "unchecked" field — the string written to the LDAP attribute
    // when the user clears the box.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP attribute value written when the checkbox is unchecked.
     *
     * @return the unchecked value string, or {@code null} if not configured
     */
    public String getUncheckedValue()
    {
        return uncheckedValue;
    }


    // ── IS ENABLED: CHECK THE INTERACTIVE FLAG ────────────────────────────────────
    // C-3PO checks whether the communiqué marks this checkbox as interactive or
    // read-only (greyed out).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the checkbox should be interactive (enabled), or
     * {@code false} if it should appear greyed-out.
     *
     * @return the enabled flag; defaults to {@code true}
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    // ── SET CHECKED VALUE ─────────────────────────────────────────────────────────
    /**
     * Sets the LDAP attribute value to write when the checkbox is checked.
     *
     * @param checkedValue  the value string to store when checked
     */
    public void setCheckedValue( String checkedValue )
    {
        this.checkedValue = checkedValue;
    }


    // ── SET ENABLED ───────────────────────────────────────────────────────────────
    /**
     * Enables or disables this checkbox.
     *
     * @param enabled  {@code true} to make it interactive; {@code false} to grey it out
     */
    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;
    }


    // ── SET LABEL ─────────────────────────────────────────────────────────────────
    /**
     * Sets the display label shown next to the checkbox.
     *
     * @param label  the label text
     */
    public void setLabel( String label )
    {
        this.label = label;
    }


    // ── SET UNCHECKED VALUE ───────────────────────────────────────────────────────
    /**
     * Sets the LDAP attribute value to write when the checkbox is unchecked.
     *
     * @param uncheckedValue  the value string to store when unchecked
     */
    public void setUncheckedValue( String uncheckedValue )
    {
        this.uncheckedValue = uncheckedValue;
    }
}
