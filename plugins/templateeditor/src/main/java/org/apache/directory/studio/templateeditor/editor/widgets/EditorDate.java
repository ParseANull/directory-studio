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


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.directory.api.util.GeneralizedTime;
import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.valueeditors.time.GeneralizedTimeValueDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.apache.directory.studio.templateeditor.EntryTemplatePlugin;
import org.apache.directory.studio.templateeditor.EntryTemplatePluginConstants;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateDate;


// ── CLASS: EditorDate — THE TANTIVE IV CHRONOMETER PANEL ─────────────────────────
// The Tantive IV's chronometer displays the current stardate in a human-readable
// format — not raw Imperial ticks, but something the crew can actually read.
// LDAP stores dates in GeneralizedTime format (e.g. "20260101120000Z") which is
// machine-friendly but unpleasant for humans. This widget reads that raw value,
// converts it to a user-configured display format (e.g. "January 1, 2026"), and
// displays it as a read-only text. An optional "Edit..." toolbar button opens the
// GeneralizedTimeValueDialog so the operator can pick a new date from a calendar.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A date display and editor widget bound to a single LDAP attribute that contains
 * a GeneralizedTime string. Displays the date in a human-readable format (configured
 * via the template's format string), and optionally provides an "Edit..." toolbar
 * button that opens a {@link GeneralizedTimeValueDialog} calendar dialog.
 * Think of this as the Tantive IV chronometer panel — converts raw ticks to a
 * date the crew can actually read.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorDate extends EditorWidget<TemplateDate>
{
    /** The main composite */
    private Composite composite;

    /** The date text widget */
    private Text dateText;

    /** The 'Browse...' toolbar item */
    private ToolItem editToolItem;


    // ── CONSTRUCTOR: INSTALL THE CHRONOMETER ─────────────────────────────────────
    // The technician installs the chronometer panel. It will read from the LDAP
    // attribute type declared in templateDate and display the value using the
    // format string defined there.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EditorDate} bound to the given template date model.
     *
     * @param editor        the owning entry editor
     * @param templateDate  the template model specifying attribute type, display format, etc.
     * @param toolkit       the form toolkit
     */
    public EditorDate( IEntryEditor editor, TemplateDate templateDate, FormToolkit toolkit )
    {
        super( templateDate, editor, toolkit );
    }


    // ── CREATE WIDGET: POWER UP THE CHRONOMETER ───────────────────────────────────
    /**
     * Creates the composite with the read-only date text and optional "Edit..."
     * toolbar button, fills it with the current LDAP attribute value, and attaches
     * the toolbar listener.
     *
     * @param parent  the parent composite
     * @return the widget composite
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


    // ── INIT WIDGET: BUILD THE DISPLAY AND TOOLBAR ────────────────────────────────
    // We create a composite with 1 or 2 columns depending on whether the "Edit..."
    // button is configured. The read-only Text displays the formatted date, and the
    // optional toolbar button triggers the calendar dialog.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the composite containing the read-only date text and the optional
     * "Edit…" toolbar button.
     *
     * @param parent  the parent composite
     * @return the parent composite
     */
    private Composite initWidget( Composite parent )
    {
        // Creating the widget composite
        composite = getToolkit().createComposite( parent );
        composite.setLayoutData( getGridata() );

        // Calculating the number of columns needed
        int numberOfColumns = 1;
        if ( getWidget().isShowEditButton() )
        {
            numberOfColumns++;
        }

        // Creating the layout
        GridLayout gl = new GridLayout( numberOfColumns, false );
        gl.marginHeight = gl.marginWidth = 0;
        gl.horizontalSpacing = gl.verticalSpacing = 0;
        composite.setLayout( gl );

        // Creating the label
        dateText = new Text( composite, SWT.NONE );
        dateText.setEditable( false );
        dateText.setBackground( composite.getBackground() );
        dateText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );

        // Creating the edit password button
        if ( getWidget().isShowEditButton() )
        {
            ToolBar toolbar = new ToolBar( composite, SWT.HORIZONTAL | SWT.FLAT );

            editToolItem = new ToolItem( toolbar, SWT.PUSH );
            editToolItem.setToolTipText( Messages.getString( "EditorDate.EditDate" ) ); //$NON-NLS-1$
            editToolItem.setImage( EntryTemplatePlugin.getDefault().getImage(
                EntryTemplatePluginConstants.IMG_TOOLBAR_EDIT_DATE ) );
        }

        return parent;
    }


    // ── CONVERT DATE: DECODE GENERALIZEDTIME TO HUMAN FORMAT ─────────────────────
    // LDAP GeneralizedTime strings look like "20260101120000Z". We parse them to a
    // Java Date, apply the template's SimpleDateFormat pattern (or a default if none
    // is set), and return the formatted string. If parsing fails, we return the raw
    // string so the operator can at least see something.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Converts a GeneralizedTime string to a human-readable date string using the
     * template's configured format pattern. Falls back to the raw string if parsing
     * fails.
     *
     * @param dateString  the LDAP GeneralizedTime string (e.g. "20260101120000Z")
     * @return the formatted date string, or the raw input if parsing fails
     */
    private String convertDate( String dateString )
    {
        try
        {
            // Creating a date
            Date date = ( new GeneralizedTime( dateString ) ).getCalendar().getTime();

            // Setting a default formatter
            SimpleDateFormat formatter = new SimpleDateFormat();

            // Getting the format defined in the template
            String format = getWidget().getFormat();
            if ( ( format != null ) && ( !format.equalsIgnoreCase( "" ) ) ) //$NON-NLS-1$
            {
                // Setting a custom formatter
                formatter = new SimpleDateFormat( format );
            }

            // Returning the formatted date
            return formatter.format( date );
        }
        catch ( ParseException pe )
        {
            // Returning the original value in that case
            return dateString;
        }
    }


    // ── ADD LISTENERS: WIRE THE EDIT BUTTON ──────────────────────────────────────
    /**
     * Attaches a selection listener to the "Edit…" toolbar button (if present) so
     * clicking it opens the {@link GeneralizedTimeValueDialog}.
     */
    private void addListeners()
    {
        // Edit toolbar item
        if ( ( editToolItem != null ) && ( !editToolItem.isDisposed() ) )
        {
            editToolItem.addSelectionListener( new SelectionAdapter()
            {
                public void widgetSelected( SelectionEvent e )
                {
                    editToolItemAction();
                }
            } );
        }
    }


    // ── EDIT TOOL ITEM ACTION: OPEN THE CALENDAR DIALOG ──────────────────────────
    // The operator clicks "Edit..." and a calendar dialog pops up. If they pick a
    // date and click OK, we write the new GeneralizedTime string back to the LDAP
    // attribute's working copy.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens a {@link GeneralizedTimeValueDialog} pre-populated with the current
     * attribute value. If the user confirms, writes the new GeneralizedTime string
     * to the LDAP attribute.
     */
    private void editToolItemAction()
    {
        // Creating and opening a GeneralizedTimeValueDialog
        GeneralizedTimeValueDialog dialog = new GeneralizedTimeValueDialog( PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getShell(), getGeneralizedTimeValueFromAttribute() );
        if ( dialog.open() == Dialog.OK )
        {
            // Updating the attribute with the new value
            updateAttributeValue( dialog.getGeneralizedTime().toGeneralizedTime() );
        }
    }


    // ── GET GENERALIZED TIME VALUE FROM ATTRIBUTE: DECODE THE RAW DATE ───────────
    /**
     * Reads the current LDAP attribute value and parses it as a {@link GeneralizedTime}.
     * Returns {@code null} if the attribute doesn't exist or the value is unparseable.
     *
     * @return the parsed {@link GeneralizedTime}, or {@code null}
     */
    private GeneralizedTime getGeneralizedTimeValueFromAttribute()
    {
        IAttribute attribute = getAttribute();
        if ( ( attribute != null ) && ( attribute.isString() ) && ( attribute.getValueSize() > 0 ) )
        {
            try
            {
                return new GeneralizedTime( attribute.getStringValue() );
            }
            catch ( ParseException e )
            {
                // Nothing to do, will return null
            }
        }

        return null;
    }


    // ── UPDATE WIDGET: REFRESH THE CHRONOMETER DISPLAY ───────────────────────────
    /**
     * Re-reads the LDAP attribute value, converts it to the display format, and
     * updates the text widget. Shows "No value" if the attribute is absent.
     */
    private void updateWidget()
    {
        IAttribute attribute = getAttribute();
        if ( ( attribute != null ) && ( attribute.isString() ) && ( attribute.getValueSize() > 0 ) )
        {
            // Setting the date value
            dateText.setText( convertDate( attribute.getStringValue() ) );
        }
        else
        {
            // No value
            dateText.setText( Messages.getString( "EditorDate.NoValue" ) ); //$NON-NLS-1$
        }

        // Updating the layout of the composite
        composite.layout();
    }


    // ── UPDATE: REFRESH THE DISPLAY ──────────────────────────────────────────────
    /**
     * Refreshes the date display from the current LDAP working copy.
     */
    public void update()
    {
        updateWidget();
    }


    // ── DISPOSE: NOTHING EXTRA TO CLEAN UP ───────────────────────────────────────
    /**
     * No-op — the SWT controls are owned by their parent composite.
     */
    public void dispose()
    {
        // Nothing to do
    }
}
