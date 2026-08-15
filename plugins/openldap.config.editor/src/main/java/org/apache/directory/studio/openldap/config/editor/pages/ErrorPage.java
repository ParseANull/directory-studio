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
package org.apache.directory.studio.openldap.config.editor.pages;


import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;


// ── CLASS: ErrorPage — Han Shooting First ───────────────────────────────────
// In the Mos Eisley Cantina, before Greedo can fire, Han draws and shoots first.
// He doesn't wait for the situation to get worse — he surfaces the problem
// immediately and deals with it on the spot.  ErrorPage is our Cantina moment:
// when something goes badly wrong loading the configuration editor, we don't
// silently swallow the exception.  Instead we immediately surface a clear
// error message to the user, and offer to expand the technical stack trace on
// demand so they (or support) can figure out exactly what happened.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An Eclipse FormPage that displays a human-readable error message when the
 * OpenLDAP server configuration editor fails to open.
 * Instead of showing a blank or broken editor, we replace the editor content
 * with this page so the user understands something went wrong and can see
 * the full stack trace if needed.
 * Think of it as Han shooting first — we surface the problem loudly rather
 * than letting it fester silently.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ErrorPage extends FormPage
{
    /** The Page ID*/
    public static final String ID = ErrorPage.class.getName();

    /** The Page Title */
    private static final String TITLE = "Error opening the editor";

    private static final String DETAILS_CLOSED = NLS.bind( "{0} >>", "Details" );
    private static final String DETAILS_OPEN = NLS.bind( "<< {0}", "Details" );

    /** The exception */
    private Exception exception;

    /** The flag indicating that the details are shown */
    private boolean detailsShown = false;

    // UI Controls
    private FormToolkit toolkit;
    private Composite parent;
    private Button detailsButton;

    private Text detailsText;


    // ── Constructor — Han Draws His Blaster ───────────────────────────────────
    // The moment trouble walks through the door, Han doesn't freeze — he grabs
    // his weapon and prepares to deal with the situation directly.
    // We store the exception so we can display it on demand; without it we'd
    // just show a generic "something broke" message with no details.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ErrorPage for the given editor and exception.
     * We need both: the editor is required by FormPage to register this page
     * in the multi-page editor, and the exception gives us the error message
     * and stack trace to show the user.
     *
     * @param editor     the FormEditor that owns this page (the config editor)
     * @param exception  the exception that caused the editor to fail to open — may be null if no details are available
     */
    public ErrorPage( FormEditor editor, Exception exception )
    {
        super( editor, ID, TITLE );
        this.exception = exception;
    }


    // ── createFormContent — Han Lays the Problem on the Table ─────────────────
    // Han doesn't hide the incident report — he puts it on the table in front
    // of everyone: here's what happened, here's the error, and here's a button
    // to pull up the gory details if you want them.
    // We build a simple two-row form: a label with the error message and a
    // "Details >>" button that expands or collapses the stack trace text area.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the error page UI: an error message label and a toggle button
     * that shows or hides the full exception stack trace.
     * Called by the Eclipse Forms framework when the page is first displayed.
     *
     * @param managedForm  the managed form context provided by the framework
     */
    @Override
    protected void createFormContent( IManagedForm managedForm )
    {
        ScrolledForm form = managedForm.getForm();
        form.setText( "Error opening the editor" );
        form.setImage( Display.getCurrent().getSystemImage( SWT.ICON_ERROR ) );

        parent = form.getBody();
        GridLayout gl = new GridLayout( 2, false );
        gl.marginHeight = 10;
        gl.marginWidth = 10;
        parent.setLayout( gl );
        parent.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        toolkit = managedForm.getToolkit();
        toolkit.decorateFormHeading( form.getForm() );

        // Error Label
        Label errorLabel = toolkit.createLabel( parent, "" );
        errorLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Details Button
        detailsButton = new Button( parent, SWT.PUSH );
        detailsButton.setText( DETAILS_CLOSED );
        detailsButton.setLayoutData( new GridData( SWT.RIGHT, SWT.NONE, false, false ) );
        detailsButton.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent e )
            {
                showOrHideDetailsView();
            }
        } );

        // Initializing with the exception
        if ( exception == null )
        {
            errorLabel.setText( "Could not open the editor." );
            detailsButton.setVisible( false );
        }
        else
        {
            errorLabel.setText( NLS.bind( "Could not open the editor: {0}", exception.getMessage() ) ); //$NON-NLS-1$
        }
    }


    // ── showOrHideDetailsView — Han Flips the Compartment Panel Open or Closed ─
    // Han reaches behind the pilot's seat in the Falcon and flips a panel open
    // to show the ship's diagnostic readout — or shoves it closed again when
    // the mechanic is done.  Each call toggles the stack trace text area in or
    // out of the form layout, and re-runs layout so the UI adapts.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Toggles the exception stack trace detail panel on or off.
     * When the details are hidden, clicking "Details >>" creates a scrollable
     * text area with the full stack trace.  Clicking "<< Details" disposes it.
     * We relayout the parent composite after each toggle so the form resizes
     * correctly.
     */
    private void showOrHideDetailsView()
    {
        if ( detailsShown )
        {
            detailsButton.setText( DETAILS_CLOSED );

            detailsText.dispose();
        }
        else
        {
            detailsButton.setText( DETAILS_OPEN );

            detailsText = toolkit.createText( parent, getStackTrace( exception ), SWT.H_SCROLL | SWT.V_SCROLL );
            detailsText.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );
        }

        parent.layout( true, true );

        detailsShown = !detailsShown;
    }


    // ── getStackTrace — Han Reads the Damage Report ───────────────────────────
    // After an incident, Han grabs the damage report printout and reads the
    // full technical readout — every failed system, every error code.
    // We do the same: print the exception's full stack trace to a StringWriter
    // so we can hand back a plain string the text area can display.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts an exception's stack trace into a plain string suitable for
     * display in a text widget.
     * We use a StringWriter/PrintWriter pair because that's what
     * Exception.printStackTrace() requires; there's no built-in way to get the
     * stack as a string without this indirection.
     *
     * @param e  the exception whose stack trace we want as a string
     * @return   the full stack trace as a newline-separated string
     */
    private String getStackTrace( Exception e )
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw, true );
        e.printStackTrace( pw );
        pw.flush();
        sw.flush();
        return sw.toString();
    }
}
