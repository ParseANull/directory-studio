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


import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.apache.directory.studio.templateeditor.model.widgets.TemplateCheckbox;


// ── CLASS: EditorCheckbox — THE TANTIVE IV TOGGLE SWITCH ─────────────────────────
// On the Tantive IV's control panel, the life-support toggle is a simple flip
// switch: up means on, down means off, and sometimes the position is ambiguous
// (a third "unknown" state). This class renders that toggle as a three-state SWT
// checkbox: CHECKED (attribute value matches checkedValue), UNCHECKED (matches
// uncheckedValue), or GRAYED (the attribute exists but with an unrecognized value).
// When the operator flips the switch, we write the appropriate string value —
// or "TRUE"/"FALSE" if no custom values are defined — back to the LDAP attribute.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * An interactive SWT checkbox that is bound to a single LDAP attribute in the
 * template entry editor. The checkbox supports three visual states — checked,
 * unchecked, and grayed — and maps them to configurable string values on the
 * LDAP attribute (defaulting to "TRUE"/"FALSE"). When the user clicks the
 * checkbox, the LDAP attribute's working copy is updated immediately.
 * Think of this as the Tantive IV toggle switch — a simple flip with a clear
 * meaning, and a grayed state when the current value is unrecognized.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorCheckbox extends EditorWidget<TemplateCheckbox>
{
    /** The checkbox */
    private Button checkbox;

    /** The enum used to determine the state of a checkbox*/
    private enum CheckboxState
    {
        UNCHECKED, CHECKED, GRAYED
    }

    /** Constant for the 'false' string value */
    private static final String FALSE_STRING_VALUE = "FALSE"; //$NON-NLS-1$

    /** Constant for the 'true' string value */
    private static final String TRUE_STRING_VALUE = "TRUE"; //$NON-NLS-1$

    /** The current state of the checkbox */
    private CheckboxState currentState = CheckboxState.UNCHECKED;

    /** The selection listener */
    private SelectionListener selectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            // Changing the state of the checkbox
            changeCheckboxState();

            // Getting the value
            boolean value = checkbox.getSelection();

            IAttribute attribute = getAttribute();
            String checkedValue = getWidget().getCheckedValue();
            String uncheckedValue = getWidget().getUncheckedValue();
            if ( attribute == null )
            {
                // The attribute does not exist

                if ( ( checkedValue == null ) && ( uncheckedValue == null ) )
                {
                    // Creating a new attribute with the value
                    addNewAttribute( ( value ? TRUE_STRING_VALUE : FALSE_STRING_VALUE ) );
                }
                else if ( ( checkedValue != null ) && ( uncheckedValue == null ) && value )
                {
                    // Creating a new attribute with the value
                    addNewAttribute( checkedValue );
                }
                else if ( ( checkedValue == null ) && ( uncheckedValue != null ) && !value )
                {
                    // Creating a new attribute with the value
                    addNewAttribute( uncheckedValue );
                }
                else if ( ( checkedValue != null ) && ( uncheckedValue != null ) )
                {
                    // Creating a new attribute with the value
                    addNewAttribute( ( value ? checkedValue : uncheckedValue ) );
                }
            }
            else
            {
                // The attribute exists

                if ( ( checkedValue == null ) && ( uncheckedValue == null ) )
                {
                    // Modifying the attribute
                    modifyAttributeValue( ( value ? TRUE_STRING_VALUE : FALSE_STRING_VALUE ) );
                }
                else if ( ( checkedValue != null ) && ( uncheckedValue == null ) )
                {
                    if ( value )
                    {
                        // Modifying the attribute
                        modifyAttributeValue( checkedValue );
                    }
                    else
                    {
                        // Deleting the attribute
                        deleteAttribute();
                    }
                }
                else if ( ( checkedValue == null ) && ( uncheckedValue != null ) )
                {
                    if ( value )
                    {
                        // Deleting the attribute
                    }
                    else
                    {
                        // Modifying the attribute
                        modifyAttributeValue( uncheckedValue );
                    }
                }
                else if ( ( checkedValue != null ) && ( uncheckedValue != null ) )
                {
                    // Modifying the attribute
                    modifyAttributeValue( ( value ? checkedValue : uncheckedValue ) );
                }
            }
        }
    };


    // ── CONSTRUCTOR: WIRE UP THE TOGGLE SWITCH ────────────────────────────────────
    // The technician plugs the checkbox panel into the control board: it knows which
    // LDAP attribute to read/write ({@code templateCheckbox.getAttributeType()})
    // and which checked/unchecked string values map to each toggle state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EditorCheckbox} bound to the given template checkbox
     * model. The template model specifies the LDAP attribute type, the label, and
     * the optional checked/unchecked string values.
     *
     * <p>For example — wiring up the toggle switch:</p>
     * <pre>
     *   new EditorCheckbox(editor, activeStatusCheckbox, toolkit);
     *   // "Toggle switch installed. Binds to 'active' attribute."
     * </pre>
     *
     * @param editor            the owning entry editor
     * @param templateCheckbox  the template model for this checkbox
     * @param toolkit           the form toolkit used to create the SWT button
     */
    public EditorCheckbox( IEntryEditor editor, TemplateCheckbox templateCheckbox, FormToolkit toolkit  )
    {
        super( templateCheckbox, editor, toolkit );
    }


    // ── CREATE WIDGET: INSTALL THE TOGGLE ON THE CONTROL PANEL ───────────────────
    // We build the SWT checkbox button (initWidget), fill it with the current LDAP
    // attribute value (updateWidget), and attach the click listener (addListeners).
    // After this call, the toggle is live and interactive.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the SWT checkbox button, populates it from the current LDAP attribute
     * value, and attaches the selection listener. Returns the parent composite as
     * the checkbox is mounted directly into it.
     *
     * <p>For example — installing the toggle on the control panel:</p>
     * <pre>
     *   initWidget(parent);   // "Toggle created."
     *   updateWidget();        // "State set from LDAP attribute."
     *   addListeners();        // "Click handler wired."
     * </pre>
     *
     * @param parent  the SWT composite to place the checkbox in
     * @return the parent composite
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


    // ── INIT WIDGET: BUILD THE TOGGLE BUTTON ─────────────────────────────────────
    // We create the SWT check button with the label from the template model. The
    // GridData positions it correctly, and the enabled/disabled state comes from
    // the template's isEnabled() flag.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the SWT {@link Button} with {@link SWT#CHECK} style, sets its label
     * and layout data, and marks it enabled or disabled per the template model.
     *
     * @param parent  the parent composite
     * @return the parent composite (checkbox is placed directly in it)
     */
    private Composite initWidget( Composite parent )
    {
        // Creating the checkbox
        checkbox = getToolkit().createButton( parent, getWidget().getLabel(), SWT.CHECK );
        checkbox.setLayoutData( getGridata() );
        checkbox.setEnabled( getWidget().isEnabled() );

        return parent;
    }


    // ── UPDATE WIDGET: REFRESH THE TOGGLE STATE FROM THE LDAP ATTRIBUTE ──────────
    // The ship's computer sends the current attribute value and we set the toggle
    // to the matching state. If there's no value yet, the toggle stays UNCHECKED.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the current LDAP attribute value and updates the checkbox visual state
     * (checked, unchecked, or grayed). No-op if the attribute doesn't exist.
     */
    private void updateWidget()
    {
        IAttribute attribute = getAttribute();
        if ( ( attribute != null ) && ( attribute.isString() ) && ( attribute.getValueSize() > 0 ) )
        {
            setCheckboxState( attribute.getStringValue() );
        }
    }


    // ── SET CHECKBOX STATE: DECODE THE VALUE AND SET THE TOGGLE ──────────────────
    // The string value from the LDAP attribute is decoded using the template's
    // checkedValue/uncheckedValue mapping. Unrecognized values produce the grayed
    // state — a signal that the value exists but doesn't match expected values.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Translates the given LDAP attribute string value into the appropriate checkbox
     * visual state using the template's checked/unchecked value configuration.
     * Unrecognized values produce the grayed state.
     *
     * @param value  the current string value of the LDAP attribute
     */
    private void setCheckboxState( String value )
    {
        String checkedValue = getWidget().getCheckedValue();
        String uncheckedValue = getWidget().getUncheckedValue();

        if ( ( checkedValue == null ) && ( uncheckedValue == null ) )
        {
            if ( TRUE_STRING_VALUE.equalsIgnoreCase( value ) )
            {
                setCheckboxCheckedState();
            }
            else if ( FALSE_STRING_VALUE.equalsIgnoreCase( value ) )
            {
                setCheckboxUncheckedState();
            }
            else
            {
                setCheckboxGrayedState();
            }
        }
        else if ( ( checkedValue != null ) && ( uncheckedValue == null ) )
        {
            if ( checkedValue.equals( value ) )
            {
                setCheckboxCheckedState();
            }
            else
            {
                setCheckboxUncheckedState();
            }
        }
        else if ( ( checkedValue == null ) && ( uncheckedValue != null ) )
        {
            if ( uncheckedValue.equals( value ) )
            {
                setCheckboxUncheckedState();
            }
            else
            {
                setCheckboxCheckedState();
            }
        }
        else if ( ( checkedValue != null ) && ( uncheckedValue != null ) )
        {
            if ( checkedValue.equals( value ) )
            {
                setCheckboxCheckedState();
            }
            else if ( uncheckedValue.equals( value ) )
            {
                setCheckboxUncheckedState();
            }
            else
            {
                setCheckboxGrayedState();
            }
        }
    }


    // ── SET CHECKBOX CHECKED STATE: TOGGLE IS UP / ON ────────────────────────────
    /**
     * Sets the checkbox to the checked (on) state.
     */
    private void setCheckboxCheckedState()
    {
        checkbox.setGrayed( false );
        checkbox.setSelection( true );
        currentState = CheckboxState.CHECKED;
    }


    // ── SET CHECKBOX UNCHECKED STATE: TOGGLE IS DOWN / OFF ───────────────────────
    /**
     * Sets the checkbox to the unchecked (off) state.
     */
    private void setCheckboxUncheckedState()
    {
        checkbox.setGrayed( false );
        checkbox.setSelection( false );
        currentState = CheckboxState.UNCHECKED;
    }


    // ── SET CHECKBOX GRAYED STATE: TOGGLE IS INDETERMINATE ───────────────────────
    /**
     * Sets the checkbox to the grayed (indeterminate) state — used when the LDAP
     * attribute value doesn't match either the checked or unchecked string.
     */
    private void setCheckboxGrayedState()
    {
        checkbox.setGrayed( true );
        checkbox.setSelection( true );
        currentState = CheckboxState.GRAYED;
    }


    // ── ADD LISTENERS: WIRE THE CLICK HANDLER ────────────────────────────────────
    /**
     * Attaches the selection listener to the checkbox button so user clicks update
     * the LDAP attribute value.
     */
    private void addListeners()
    {
        if ( ( checkbox != null ) && ( !checkbox.isDisposed() ) )
        {
            checkbox.addSelectionListener( selectionListener );
        }
    }


    // ── CHANGE CHECKBOX STATE: CYCLE THROUGH STATES ON CLICK ─────────────────────
    // UNCHECKED → CHECKED → UNCHECKED. GRAYED always cycles to CHECKED. This gives
    // the grayed state a way out — once the operator acknowledges it, it snaps to
    // a definite checked state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Advances the checkbox to the next state in the cycle. UNCHECKED → CHECKED,
     * CHECKED → UNCHECKED, GRAYED → CHECKED. Called at the start of the selection
     * listener before writing the new value to the LDAP attribute.
     */
    private void changeCheckboxState()
    {
        switch ( currentState )
        {
            case UNCHECKED:
                setCheckboxCheckedState();
                currentState = CheckboxState.CHECKED;
                break;
            case CHECKED:
                setCheckboxUncheckedState();
                currentState = CheckboxState.UNCHECKED;
                break;
            case GRAYED:
                setCheckboxCheckedState();
                currentState = CheckboxState.CHECKED;
                break;
        }
    }


    // ── UPDATE: REFRESH THE TOGGLE FROM CURRENT ATTRIBUTE DATA ───────────────────
    /**
     * Refreshes the checkbox state from the current LDAP attribute value. Called
     * by the parent widget manager when the entry's working copy changes.
     */
    public void update()
    {
        updateWidget();
    }


    // ── DISPOSE: NOTHING TO CLEAN UP ─────────────────────────────────────────────
    /**
     * No-op — the SWT button is owned by its parent composite and disposed with it.
     */
    public void dispose()
    {
        // Nothing to do
    }
}
