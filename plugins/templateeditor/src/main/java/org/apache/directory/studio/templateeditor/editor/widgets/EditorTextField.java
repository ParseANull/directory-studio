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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.apache.directory.studio.templateeditor.model.widgets.TemplateTextField;
import org.apache.directory.studio.templateeditor.model.widgets.WidgetAlignment;


// ── CLASS: EditorTextField — THE TANTIVE IV DATA ENTRY TERMINAL ──────────────────
// When Lieutenant Antilles types coordinates into the Tantive IV's navigation
// terminal, every keystroke updates the ship's navicomputer in real time — he
// doesn't press Enter to commit. This class works the same way: every character
// the user types in the text field immediately updates the LDAP entry's shared
// working copy via a ModifyListener. The field can be single-line or multi-line,
// can enforce a character limit, and can treat '$' as a newline character.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * An editable SWT {@link Text} field bound to a single LDAP attribute. Each
 * keystroke is written back to the attribute via a {@link ModifyListener}, so
 * there's no "confirm" step. Supports single or multi-row layout, character
 * limit enforcement, and {@code $}-as-newline translation. Listeners are removed
 * during {@link #update()} to avoid feedback loops.
 * Think of this as the data entry terminal on the Tantive IV bridge.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorTextField extends EditorWidget<TemplateTextField>
{
    /** The text field */
    private Text textfield;

    /** The modify listener */
    private ModifyListener modifyListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            // Getting the value
            String value = textfield.getText();

            // Replacing '$' by '/n' if needed
            if ( getWidget().isDollarSignIsNewLine() )
            {
                value = value.replace( '\n', '$' );
            }

            // Updating the attribute's value
            updateAttributeValue( value );
        }
    };


    // ── CONSTRUCTOR: INSTALL THE DATA ENTRY TERMINAL ──────────────────────────────
    // The technician installs the keyboard terminal at the station. It binds to the
    // LDAP attribute type declared in the template model, using the configured row
    // count, character limit, and alignment settings.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EditorTextField} bound to the given template text field model.
     *
     * @param editor             the owning entry editor
     * @param templateTextField  the template model specifying attribute type, rows, limits, etc.
     * @param toolkit            the form toolkit used to create the SWT text control
     */
    public EditorTextField( IEntryEditor editor, TemplateTextField templateTextField, FormToolkit toolkit )
    {
        super( templateTextField, editor, toolkit );
    }


    // ── CREATE WIDGET: BUILD THE KEYBOARD TERMINAL ────────────────────────────────
    // We create the Text widget with the correct style (single/multi-line, border,
    // alignment), fill it with the current attribute value, and attach the modify
    // listener so keystrokes propagate immediately.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the SWT {@link Text} field, populates it from the current LDAP
     * attribute value, and attaches the modify listener.
     *
     * @param parent  the parent composite
     * @return the parent composite (text field is placed directly in it)
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


    // ── INIT WIDGET: BUILD THE TEXT INPUT BOX ─────────────────────────────────────
    // We create the Text widget with the style computed from the template's row count
    // and alignment, set the character limit if configured, and compute the height
    // hint from font metrics for multi-row fields.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the {@link Text} widget with border, wrap, single/multi style, and
     * alignment flags. Sets the character limit and height hint if configured.
     *
     * @param parent  the parent composite
     * @return the parent composite
     */
    private Composite initWidget( Composite parent )
    {
        // Creating the text field
        textfield = getToolkit().createText( parent, "", getStyle() ); //$NON-NLS-1$
        GridData gd = getGridata();
        textfield.setLayoutData( gd );

        // Setting the characters limit
        if ( getWidget().getCharactersLimit() != -1 )
        {
            textfield.setTextLimit( getWidget().getCharactersLimit() );
        }

        // Calculating height for multiple rows
        int numberOfRows = getWidget().getNumberOfRows();
        if ( numberOfRows != 1 )
        {
            GC gc = new GC( parent );

            try
            {
                gc.setFont( textfield.getFont() );
                FontMetrics fontMetrics = gc.getFontMetrics();
                gd.heightHint = fontMetrics.getHeight() * numberOfRows;
            }
            finally
            {
                gc.dispose();
            }
        }

        textfield.pack();

        return parent;
    }


    // ── GET STYLE: COMPUTE THE SWT TEXT STYLE ────────────────────────────────────
    /**
     * Returns the combined SWT style constant for the text field. Always includes
     * {@link SWT#BORDER} and {@link SWT#WRAP}. Adds {@link SWT#SINGLE} or
     * {@link SWT#MULTI} based on row count, and optional alignment flags.
     *
     * @return combined SWT style constant
     */
    private int getStyle()
    {
        int style = SWT.BORDER | SWT.WRAP;

        // Multiple lines?
        if ( getWidget().getNumberOfRows() == 1 )
        {
            style |= SWT.SINGLE;
        }
        else
        {
            style |= SWT.MULTI;
        }

        // Horizontal alignment set to end?
        if ( getWidget().getHorizontalAlignment() == WidgetAlignment.END )
        {
            style |= SWT.RIGHT;
        }
        // Horizontal alignment set to center?
        else if ( getWidget().getHorizontalAlignment() == WidgetAlignment.CENTER )
        {
            style |= SWT.CENTER;
        }

        return style;
    }


    // ── UPDATE WIDGET: REFRESH THE TEXT FROM THE LDAP ATTRIBUTE ─────────────────
    // We re-read the attribute value, apply the dollar-sign-to-newline conversion
    // if needed, and set the text — preserving the caret position so the user's
    // cursor doesn't jump to the start when the working copy is refreshed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Re-reads the LDAP attribute value and updates the text field content.
     * Preserves the current caret position to avoid disorienting the user.
     * Converts '$' to newline if the template's dollarSignIsNewLine flag is set.
     */
    private void updateWidget()
    {
        IAttribute attribute = getAttribute();
        if ( ( attribute != null ) && ( attribute.isString() ) && ( attribute.getValueSize() > 0 ) )
        {
            // Saving the current caret position by getting the current selection
            Point selection = textfield.getSelection();

            // Getting the text to display
            String text = attribute.getStringValue();

            // Replacing '$' by '/n' (if needed)
            if ( getWidget().isDollarSignIsNewLine() )
            {
                text = text.replace( '$', '\n' );
            }

            // Assigning the text to the label
            textfield.setText( text );

            // Restoring the current caret position
            textfield.setSelection( selection );
        }
        else
        {
            // There's no value to display
            textfield.setText( "" ); //$NON-NLS-1$
        }
    }


    // ── ADD LISTENERS: WIRE THE KEYPRESS HANDLER ─────────────────────────────────
    /**
     * Attaches the modify listener so every keystroke updates the LDAP attribute.
     */
    private void addListeners()
    {
        if ( ( textfield != null ) && ( !textfield.isDisposed() ) )
        {
            textfield.addModifyListener( modifyListener );
        }
    }


    // ── REMOVE LISTENERS: DETACH DURING PROGRAMMATIC UPDATES ─────────────────────
    // We remove the listener before calling updateWidget() so that programmatic
    // setText() calls don't trigger the modify listener and cause a feedback loop.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the modify listener. Called before programmatic {@code setText()} calls
     * to prevent feedback loops where the listener fires during an update.
     */
    private void removeListeners()
    {
        if ( ( textfield != null ) && ( !textfield.isDisposed() ) )
        {
            textfield.removeModifyListener( modifyListener );
        }
    }


    // ── UPDATE: REFRESH WITHOUT TRIGGERING LISTENERS ─────────────────────────────
    /**
     * Removes the modify listener, refreshes the text field from the LDAP attribute,
     * then re-attaches the listener to avoid feedback loops.
     */
    public void update()
    {
        removeListeners();
        updateWidget();
        addListeners();
    }


    // ── DISPOSE: CLEAN UP LISTENERS ──────────────────────────────────────────────
    /**
     * Removes the modify listener. The SWT text widget itself is disposed by its
     * parent composite.
     */
    public void dispose()
    {
        removeListeners();
    }
}
