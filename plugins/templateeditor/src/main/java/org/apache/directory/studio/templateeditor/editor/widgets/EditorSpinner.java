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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.apache.directory.studio.templateeditor.model.widgets.TemplateSpinner;


// ── CLASS: EditorSpinner — THE TANTIVE IV NUMERIC DIAL ───────────────────────────
// On the Tantive IV, certain systems have a numeric dial for adjusting a bounded
// value — shield strength percentage, hyperdrive power level. The operator clicks
// up/down arrows or types a number, and the system updates. This class is that
// numeric dial: an SWT {@link Spinner} widget configured with the template's min,
// max, increment, page increment, and decimal digits settings. Every change is
// written back to the LDAP attribute as a string.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A numeric spinner widget bound to a single LDAP attribute. Configured from the
 * template model with minimum, maximum, increment, page increment, and digit
 * (decimal) settings. Each value change is written to the LDAP attribute as a
 * string. Gracefully ignores non-numeric existing attribute values.
 * Think of this as the Tantive IV numeric dial — bounded integer adjustment.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorSpinner extends EditorWidget<TemplateSpinner>
{
    /** The spinner */
    private Spinner spinner;

    /** The selection listener */
    private SelectionListener selectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            // Getting the value
            String value = spinner.getText();

            // Updating the attribute's value
            updateAttributeValue( value );
        }
    };


    // ── CONSTRUCTOR: INSTALL THE NUMERIC DIAL ─────────────────────────────────────
    // The technician installs the dial and configures its range and step settings
    // from the template model.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EditorSpinner} bound to the given template spinner model.
     *
     * @param editor           the owning entry editor
     * @param templateSpinner  the template model with min, max, increment, and digits settings
     * @param toolkit          the form toolkit
     */
    public EditorSpinner( IEntryEditor editor, TemplateSpinner templateSpinner, FormToolkit toolkit )
    {
        super( templateSpinner, editor, toolkit );
    }


    // ── CREATE WIDGET: BUILD THE NUMERIC DIAL ────────────────────────────────────
    /**
     * Creates the SWT {@link Spinner}, sets its range and step values, fills it
     * with the current LDAP attribute value, and attaches the selection listener.
     *
     * @param parent  the parent composite
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


    // ── INIT WIDGET: CONFIGURE THE DIAL RANGE ────────────────────────────────────
    // We create the SWT Spinner with a border, then set its digits, increment,
    // maximum, minimum, and page increment from the template model.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the {@link Spinner} widget and configures its numeric range and step
     * settings from the template model.
     *
     * @param parent  the parent composite
     * @return the parent composite
     */
    private Composite initWidget( Composite parent )
    {
        // Creating the spinner
        spinner = new Spinner( parent, SWT.BORDER );
        spinner.setLayoutData( getGridata() );

        // Setting the spinner values
        spinner.setDigits( getWidget().getDigits() );
        spinner.setIncrement( getWidget().getIncrement() );
        spinner.setMaximum( getWidget().getMaximum() );
        spinner.setMinimum( getWidget().getMinimum() );
        spinner.setPageIncrement( getWidget().getPageIncrement() );

        return parent;
    }


    // ── UPDATE WIDGET: DIAL IN THE CURRENT VALUE ─────────────────────────────────
    // We parse the LDAP attribute's string value as an integer and set the spinner
    // selection. If the value can't be parsed (e.g. it's empty or non-numeric),
    // we fail gracefully and leave the spinner at its default.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the current LDAP attribute value and sets the spinner selection.
     * Ignores non-numeric attribute values gracefully.
     */
    private void updateWidget()
    {
        IAttribute attribute = getAttribute();
        if ( ( attribute != null ) && ( attribute.isString() ) && ( attribute.getValueSize() > 0 ) )
        {
            try
            {
                spinner.setSelection( Integer.parseInt( attribute.getStringValue() ) );
            }
            catch ( NumberFormatException e )
            {
                // Nothing to do, we fail gracefully
            }
        }
    }


    // ── ADD LISTENERS: WIRE THE DIAL CHANGE HANDLER ──────────────────────────────
    /**
     * Attaches the selection listener so spinner value changes update the LDAP
     * attribute.
     */
    private void addListeners()
    {
        // Adding the listener
        spinner.addSelectionListener( selectionListener );
    }


    // ── UPDATE: REFRESH THE DIAL VALUE ───────────────────────────────────────────
    /**
     * Refreshes the spinner value from the current LDAP working copy.
     */
    public void update()
    {
        updateWidget();
    }


    // ── DISPOSE: NOTHING EXTRA TO CLEAN UP ───────────────────────────────────────
    /**
     * No-op — the SWT Spinner is owned by its parent composite.
     */
    public void dispose()
    {
        // Nothing to do
    }
}
