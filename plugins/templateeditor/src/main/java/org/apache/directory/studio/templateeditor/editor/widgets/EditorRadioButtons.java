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


import java.util.HashMap;
import java.util.Map;

import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.apache.directory.studio.templateeditor.model.widgets.TemplateRadioButtons;
import org.apache.directory.studio.templateeditor.model.widgets.ValueItem;


// ── CLASS: EditorRadioButtons — THE TANTIVE IV OPTION SELECTOR BANK ──────────────
// On the Tantive IV, certain system configurations are mutually exclusive — the
// ship can be in "Combat", "Patrol", OR "Diplomatic Escort" mode, never two at
// once. The crew chooses one by pressing the corresponding button on the bank.
// This class renders that button bank as a column of SWT radio buttons, one per
// template-configured {@link ValueItem}. Clicking a button writes that item's
// value to the LDAP attribute and clears the previous selection.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A radio button group widget bound to a single LDAP attribute. One radio button
 * is created for each {@link ValueItem} in the template model. Selecting a button
 * writes its value to the LDAP attribute; only one button can be selected at a
 * time. When refreshing, the button matching the current attribute value is
 * automatically selected.
 * Think of this as the Tantive IV option selector button bank.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorRadioButtons extends EditorWidget<TemplateRadioButtons>
{
    /** The map of (ValueItem,Button) elements used in the UI */
    private Map<ValueItem, Button> valueItemsToButtonsMap = new HashMap<ValueItem, Button>();
    private Map<Button, ValueItem> buttonsToValueItemsMap = new HashMap<Button, ValueItem>();

    /** The currently selected item */
    private ValueItem selectedItem;

    /** The selection listener */
    private SelectionListener selectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            // Saving the selected item
            selectedItem = buttonsToValueItemsMap.get( e.getSource() );

            // Updating the entry
            updateEntry();
        }
    };


    // ── CONSTRUCTOR: INSTALL THE OPTION SELECTOR BANK ────────────────────────────
    // The technician installs the radio button bank. The template model provides
    // the list of buttons and the LDAP attribute type they bind to.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EditorRadioButtons} bound to the given template model.
     *
     * @param editor                  the owning entry editor
     * @param templateRadioButtons    the template model specifying buttons and attribute type
     * @param toolkit                 the form toolkit used to create the SWT buttons
     */
    public EditorRadioButtons( IEntryEditor editor, TemplateRadioButtons templateRadioButtons,
        FormToolkit toolkit )
    {
        super( templateRadioButtons, editor, toolkit );
    }


    // ── CREATE WIDGET: BUILD THE RADIO BUTTON GROUP ───────────────────────────────
    /**
     * Creates the composite with one radio button per {@link ValueItem}, selects
     * the button matching the current LDAP attribute value, and attaches selection
     * listeners.
     *
     * @param parent  the parent composite
     * @return the radio button group composite
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


    // ── INIT WIDGET: CREATE THE RADIO BUTTONS ────────────────────────────────────
    // We create a composite with a single-column GridLayout (zero margins for tight
    // packing), then add one radio button per ValueItem. Both lookup maps are
    // populated so we can go from button → ValueItem and ValueItem → button quickly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a composite containing one radio {@link Button} per {@link ValueItem}.
     * Maintains bidirectional maps between buttons and value items for fast lookup.
     *
     * @param parent  the parent composite
     * @return the composite containing the radio buttons
     */
    private Composite initWidget( Composite parent )
    {
        // Creating the widget composite
        Composite composite = getToolkit().createComposite( parent );
        composite.setLayout( new GridLayout() );
        composite.setLayoutData( getGridata() );

        // Creating the layout
        GridLayout gl = new GridLayout();
        gl.marginHeight = gl.marginWidth = 0;
        gl.horizontalSpacing = gl.verticalSpacing = 0;
        composite.setLayout( gl );

        // Creating the Radio Buttons
        for ( ValueItem valueItem : getWidget().getButtons() )
        {
            Button button = getToolkit().createButton( composite, valueItem.getLabel(), SWT.RADIO );
            button.setEnabled( getWidget().isEnabled() );
            valueItemsToButtonsMap.put( valueItem, button );
            buttonsToValueItemsMap.put( button, valueItem );
        }

        return composite;
    }


    // ── UPDATE WIDGET: SELECT THE MATCHING BUTTON ─────────────────────────────────
    // We read the current LDAP attribute string value and find the radio button
    // whose ValueItem.value matches it, selecting that button and deselecting the rest.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the current LDAP attribute value and selects the radio button whose
     * {@link ValueItem} value matches it. Deselects all others.
     */
    private void updateWidget()
    {
        // Getting the attribute value
        IAttribute attribute = getAttribute();
        if ( ( attribute != null ) && ( attribute.isString() ) && ( attribute.getValueSize() > 0 ) )
        {
            String value = attribute.getStringValue();
            for ( ValueItem valueItem : valueItemsToButtonsMap.keySet() )
            {
                Button button = valueItemsToButtonsMap.get( valueItem );
                if ( button != null && !button.isDisposed() )
                {
                    button.setSelection( value.equals( valueItem.getValue() ) );
                }
            }
        }
    }


    // ── ADD LISTENERS: WIRE CLICK HANDLERS TO ALL BUTTONS ────────────────────────
    /**
     * Attaches the selection listener to every radio button in the group.
     */
    private void addListeners()
    {
        for ( final ValueItem valueItem : valueItemsToButtonsMap.keySet() )
        {
            Button button = valueItemsToButtonsMap.get( valueItem );
            if ( button != null )
            {
                button.addSelectionListener( selectionListener );
            }
        }
    }


    // ── UPDATE ENTRY: WRITE THE SELECTED VALUE TO THE ATTRIBUTE ──────────────────
    /**
     * Writes the selected {@link ValueItem}'s value to the LDAP attribute.
     * Creates, modifies, or deletes the attribute as needed.
     */
    private void updateEntry()
    {
        // Getting the  attribute
        IAttribute attribute = getAttribute();
        if ( attribute == null )
        {
            if ( selectedItem != null )
            {
                // Creating a new attribute with the value
                addNewAttribute( selectedItem.getValue() );
            }
        }
        else
        {
            if ( ( selectedItem != null ) && ( !selectedItem.equals( "" ) ) ) //$NON-NLS-1$
            {
                // Modifying the existing attribute
                modifyAttributeValue( selectedItem.getValue() );
            }
            else
            {
                // Deleting the attribute
                deleteAttribute();
            }
        }
    }


    // ── UPDATE: REFRESH THE SELECTION ────────────────────────────────────────────
    /**
     * Refreshes the radio button selection from the current LDAP working copy.
     */
    public void update()
    {
        updateWidget();
    }


    // ── DISPOSE: NOTHING EXTRA TO CLEAN UP ───────────────────────────────────────
    /**
     * No-op — the SWT buttons are owned by their parent composite.
     */
    public void dispose()
    {
        // Nothing to do
    }
}
