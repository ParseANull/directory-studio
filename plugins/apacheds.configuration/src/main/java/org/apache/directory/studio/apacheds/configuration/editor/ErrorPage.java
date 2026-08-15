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
package org.apache.directory.studio.apacheds.configuration.editor;


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


// ── CLASS: ErrorPage — R2-D2 PROJECTING A CRITICAL SYSTEM FAILURE HOLOGRAM ───────────────────────
// In A New Hope, R2-D2 projects Princess Leia's hologram — but imagine the projector
// overheats and instead of Leia, the astromech beeps frantically and flashes a red
// error indicator across the room: "This unit has experienced a critical malfunction."
// That's exactly what this page does. When the server configuration editor fails to
// load the config (bad file, bad connection, parsing error), instead of showing the
// real config pages we swap in this ErrorPage: R2's emergency distress hologram,
// complete with the exception message and an expandable stack trace for the tech-savvy.
// ─────────────────────────────────────────────────────────────────────────────────────────────────
/**
 * The editor page displayed when the server configuration fails to load.
 * Rather than showing a broken or empty config form, we replace the normal
 * pages with this one, which shows the error message and (optionally) the
 * full Java stack trace. Think of it as R2-D2's critical-failure hologram:
 * it tells you something went badly wrong and gives you the technical details
 * if you press the "Details" button.
 * Used by {@link ServerConfigurationEditor} when the loading job throws an exception.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ErrorPage extends FormPage
{
    /** The Page ID*/
    public static final String ID = ErrorPage.class.getName();

    /** The Page Title */
    private static final String TITLE = Messages.getString( "ErrorPage.ErrorOpeningEditor" ); //$NON-NLS-1$

    private static final String DETAILS_CLOSED = NLS.bind( "{0} >>", Messages.getString( "ErrorPage.Details" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    private static final String DETAILS_OPEN = NLS.bind( "<< {0}", Messages.getString( "ErrorPage.Details" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    /** The exception */
    private Exception exception;

    /** The flag indicating that the details are shown */
    private boolean detailsShown = false;

    // UI Controls
    private FormToolkit toolkit;
    private Composite parent;
    private Button detailsButton;

    private Text detailsText;


    // ── R2 LIGHTS UP THE DISTRESS PROJECTOR ──────────────────────────────────────────────────────
    // R2-D2 boots his emergency hologram projector and receives the fault report
    // from the ship's computer: this is what he'll display once the screen lights up.
    // We store the exception so createFormContent can show the right message later.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new error page and associates it with the given editor and
     * exception. The page will display the exception message and offer a
     * "Details" button to reveal the full stack trace.
     *
     * <p>For example — R2 receives the fault report:</p>
     * <pre>
     *   ErrorPage errorPage = new ErrorPage(editor, caughtException);
     *   editor.addPage(errorPage);  // R2 is ready to project the distress hologram
     * </pre>
     *
     * @param editor     The parent {@link FormEditor} that contains this page.
     * @param exception  The exception that caused the load failure; may be
     *                   {@code null} if the cause is unknown.
     */
    public ErrorPage( FormEditor editor, Exception exception )
    {
        super( editor, ID, TITLE );
        this.exception = exception;
    }


    // ── R2 PROJECTS THE DISTRESS HOLOGRAM ────────────────────────────────────────────────────────
    // R2-D2 spins up and beams the red error hologram across the room.
    // The message reads "Critical malfunction — cannot open editor." and a button
    // offers to expand the technical fault log for the engineer who wants the gory details.
    // createFormContent builds exactly that: error label + optional details button + stack trace pane.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT form content for this page: an error label describing what
     * went wrong, and a "Details" toggle button that reveals (or hides) the
     * full Java stack trace in a scrollable text area.
     *
     * <p>For example — R2 projects the hologram:</p>
     * <pre>
     *   form.setText("Error Opening Editor");
     *   // error label: "Could not open the editor: ..."
     *   // details button: "Details >>"
     * </pre>
     *
     * @param managedForm  The managed form provided by the Eclipse forms framework.
     */
    protected void createFormContent( IManagedForm managedForm )
    {
        ScrolledForm form = managedForm.getForm();
        form.setText( Messages.getString( "ErrorPage.ErrorOpeningEditor" ) ); //$NON-NLS-1$
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
        Label errorLabel = toolkit.createLabel( parent, "" ); //$NON-NLS-1$
        errorLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Details Button
        detailsButton = new Button( parent, SWT.PUSH );
        detailsButton.setText( DETAILS_CLOSED );
        detailsButton.setLayoutData( new GridData( SWT.RIGHT, SWT.NONE, false, false ) );
        detailsButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                showOrHideDetailsView();
            }
        } );

        // Initializing with the exception
        if ( exception == null )
        {
            errorLabel.setText( Messages.getString( "ErrorPage.CouldNotOpenEditor" ) ); //$NON-NLS-1$
            detailsButton.setVisible( false );
        }
        else
        {
            errorLabel.setText( NLS.bind( "Could not open the editor: {0}", exception.getMessage() ) ); //$NON-NLS-1$
        }
    }


    // ── TOGGLING THE TECHNICAL FAULT LOG ─────────────────────────────────────────────────────────
    // When the engineer presses the button on R2's console, the full system fault log
    // either scrolls out of a panel in R2's chassis or retracts back in.
    // showOrHideDetailsView does exactly that: show or hide the stack trace text widget.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Toggles the visibility of the stack trace detail panel. When the panel is
     * hidden we dispose the text widget to reclaim layout space; when it's shown
     * we create it fresh with the full stack trace and re-lay-out the parent.
     *
     * <p>For example — the engineer presses R2's fault-log button:</p>
     * <pre>
     *   // first press: panel appears with full stack trace
     *   // second press: panel disappears, layout compacts
     * </pre>
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


    // ── READING OUT THE FAULT LOG DATA ────────────────────────────────────────────────────────────
    // R2-D2 reads the raw fault log stored in his memory banks and converts it to a
    // human-readable printout that the engineer can actually decipher.
    // getStackTrace does the same: convert the Java exception's stack trace to a plain string.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Converts a Java exception's stack trace to a plain {@link String} so it
     * can be displayed in the scrollable text widget. We use a
     * {@link StringWriter} backed by a {@link PrintWriter} to capture the
     * output of {@link Exception#printStackTrace(PrintWriter)}.
     *
     * <p>For example — R2 prints out the fault log:</p>
     * <pre>
     *   String log = getStackTrace(loadingException);
     *   // "java.io.IOException: Could not read config file\n  at ..."
     * </pre>
     *
     * @param e  The exception whose stack trace we want as a string.
     * @return   The full stack trace as a multi-line string.
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
