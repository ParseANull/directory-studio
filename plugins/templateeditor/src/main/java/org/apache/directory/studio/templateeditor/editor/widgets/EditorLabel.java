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
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.apache.directory.studio.templateeditor.model.widgets.TemplateLabel;
import org.apache.directory.studio.templateeditor.model.widgets.WidgetAlignment;


// ── CLASS: EditorLabel — THE TANTIVE IV STATUS READOUT DISPLAY ───────────────────
// On the Tantive IV's bridge, the status readout displays current values — shield
// strength, fuel level, whatever the crew needs to see — but the operator can't
// type into it. It's read-only. This class is that readout: it displays the current
// value of an LDAP attribute (or a static string) as a non-editable Text widget.
// Multi-line readouts are sized by font metrics so they show the right number of
// rows. '$' characters can be interpreted as newlines if the template says so.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A read-only display widget that shows an LDAP attribute's value (or a static
 * string from the template) as a non-editable SWT {@link Text} control. The label
 * can display single or multiple rows, and can convert {@code $} characters to
 * newlines. It doesn't allow editing — it's purely informational.
 * Think of this as the status readout panel on the Tantive IV.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorLabel extends EditorWidget<TemplateLabel>
{
    /** The label widget */
    private Text label;


    // ── CONSTRUCTOR: MOUNT THE READOUT DISPLAY ────────────────────────────────────
    // The technician installs the status readout panel. It reads from a specific
    // LDAP attribute type (or uses a static value) as declared in the template model.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EditorLabel} that displays the bound LDAP attribute's
     * value (or a static string) in a non-editable text widget.
     *
     * @param editor         the owning entry editor
     * @param templateLabel  the template model specifying attribute type, value, rows, etc.
     * @param toolkit        the form toolkit
     */
    public EditorLabel( IEntryEditor editor, TemplateLabel templateLabel, FormToolkit toolkit )
    {
        super( templateLabel, editor, toolkit );
    }


    // ── CREATE WIDGET: BUILD THE READOUT ─────────────────────────────────────────
    // We create the non-editable Text widget with the correct style and size, then
    // fill it with the current attribute value.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the read-only {@link Text} control, sizes it for the declared number
     * of rows, fills it with the current LDAP attribute value, and returns the parent.
     *
     * @param parent  the parent composite
     * @return the parent composite (label is placed directly in it)
     */
    public Composite createWidget( Composite parent )
    {
        // Creating and initializing the widget UI
        Composite composite = initWidget( parent );

        // Updating the widget's content
        updateWidget();

        return composite;
    }


    // ── INIT WIDGET: CONFIGURE THE READOUT DISPLAY ───────────────────────────────
    // We create the Text widget in read-only mode, set its background to match the
    // parent's so it looks like a label not an editable field, and compute the height
    // hint if multiple rows are requested.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a non-editable, background-matched {@link Text} widget. Computes the
     * height hint from font metrics if the template requests more than one row.
     *
     * @param parent  the parent composite
     * @return the parent composite
     */
    private Composite initWidget( Composite parent )
    {
        // Creating the label
        label = new Text( parent, getStyle() );
        label.setEditable( false );
        label.setBackground( parent.getBackground() );

        // Setting the layout data
        GridData gd = getGridata();
        label.setLayoutData( gd );

        // Calculating height for multiple rows
        int numberOfRows = getWidget().getNumberOfRows();
        if ( numberOfRows != 1 )
        {
            GC gc = new GC( parent );

            try
            {
                gc.setFont( label.getFont() );
                FontMetrics fontMetrics = gc.getFontMetrics();
                gd.heightHint = fontMetrics.getHeight() * numberOfRows;
            }
            finally
            {
                gc.dispose();
            }
        }

        return parent;
    }


    // ── GET STYLE: CALCULATE THE TEXT WIDGET STYLE ───────────────────────────────
    /**
     * Returns the SWT style flags for the Text widget based on the template's row
     * count and horizontal alignment settings.
     *
     * @return combined SWT style constant
     */
    private int getStyle()
    {
        int style = SWT.WRAP;

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


    // ── UPDATE WIDGET: REFRESH THE READOUT VALUE ─────────────────────────────────
    // The status readout refreshes: we look up the current LDAP attribute value (or
    // static value), apply the dollar-sign-to-newline conversion if needed, and
    // set it on the text widget.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Re-reads the LDAP attribute value (or static template value) and updates the
     * text display. Converts '$' to newline if the template's dollarSignIsNewLine
     * flag is set.
     */
    private void updateWidget()
    {
        // Checking if we need to display a value taken from the entry
        // or use the given value
        String attributeType = getWidget().getAttributeType();
        if ( ( attributeType != null ) || ( "".equals( attributeType ) ) ) //$NON-NLS-1$
        {
            IEntry entry = getEntry();
            if ( entry != null )
            {
                // Getting the text to display
                String text = EditorWidgetUtils.getConcatenatedValues( entry, attributeType );

                // Replacing '$' by '/n' if needed
                if ( getWidget().isDollarSignIsNewLine() )
                {
                    text = text.replace( '$', '\n' );
                }

                // Assigning the text to the label
                label.setText( text );
            }
        }
        else
        {
            // Getting the text to display
            String text = getWidget().getValue();

            // Replacing '$' by '/n' if needed
            if ( getWidget().isDollarSignIsNewLine() )
            {
                text = text.replace( '$', '\n' );
            }

            // Assigning the text to the label
            label.setText( text );
        }

        // Forcing the re-layout of the label from its parent
        label.getParent().layout();
    }


    // ── UPDATE: REFRESH THE DISPLAY ──────────────────────────────────────────────
    /**
     * Refreshes the label's display value from the current LDAP working copy.
     */
    public void update()
    {
        updateWidget();
    }


    // ── DISPOSE: SHUT DOWN THE READOUT ───────────────────────────────────────────
    /**
     * Disposes the underlying SWT Text widget.
     */
    public void dispose()
    {
        // Nothing to do
        label.dispose();
    }
}
