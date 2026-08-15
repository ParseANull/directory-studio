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


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;


// ── CLASS: LoadingPage — R2-D2 DOWNLOADING THE DEATH STAR PLANS ──────────────────────────────────
// In Rogue One, after Jyn triggers the transmission, R2-D2 spins up and beeps anxiously
// while the Death Star plans are being copied onto the transmission beam.
// You can't see the plans yet — R2 is still processing.  A little indicator spins,
// and the message says "Please stand by, transmission in progress..."
// That's exactly what this page does.  While the background job loads the ApacheDS
// configuration (from disk or over a live LDAP connection), we replace all the real
// config pages with this single placeholder: a progress bar and a "Loading, please wait" label.
// Once the plans are downloaded, the real pages take over.
// ─────────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A placeholder editor page shown while the server configuration is being loaded
 * in the background. The user sees a spinning progress bar and a "Loading, please
 * wait" message instead of the (not-yet-ready) configuration pages.
 * Think of this as R2-D2 downloading the Death Star plans: we know the data is coming,
 * we just can't show it yet. Once the load completes the real pages replace this one.
 * Used by {@link ServerConfigurationEditor}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LoadingPage extends FormPage
{
    /** The Page ID*/
    public static final String ID = LoadingPage.class.getName(); //$NON-NLS-1$

    /** The Page Title */
    private static final String TITLE = Messages.getString( "LoadingPage.LoadingConfiguration" ); //$NON-NLS-1$


    // ── R2 BOOTS UP THE DOWNLOAD SEQUENCE ────────────────────────────────────────────────────────
    // R2-D2 powers up his transmission receiver and registers himself with the ship's
    // communication relay (the editor) so he can project the loading indicator.
    // We pass the editor reference and our page ID/title to the superclass.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new loading page and associates it with the given editor.
     * The page shows a spinning progress bar until the editor's background
     * loading job finishes and replaces this page with the real config pages.
     *
     * <p>For example — R2 boots up his receiver:</p>
     * <pre>
     *   LoadingPage loading = new LoadingPage(editor);
     *   editor.addPage(loading);
     *   // user sees the progress bar while config loads in the background
     * </pre>
     *
     * @param editor  The parent {@link FormEditor} that contains this page.
     */
    public LoadingPage( FormEditor editor )
    {
        super( editor, ID, TITLE );
    }


    // ── PROJECTING THE "TRANSMISSION IN PROGRESS" HOLOGRAM ───────────────────────────────────────
    // R2 spins up his indicator dome and projects a simple message:
    // spinning progress bar + "Loading the configuration, please wait."
    // createFormContent builds that — no interactive controls, just the waiting display.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT form content for this page: a centred, indeterminate
     * progress bar and a "Loading the configuration, please wait" label.
     * Neither widget is interactive — they are purely informational while
     * the background job is running.
     *
     * <p>For example — R2's loading indicator spins up:</p>
     * <pre>
     *   // form shows: [spinning progress bar]
     *   //             "Loading the configuration, please wait"
     * </pre>
     *
     * @param managedForm  The managed form provided by the Eclipse forms framework.
     */
    protected void createFormContent( IManagedForm managedForm )
    {
        ScrolledForm form = managedForm.getForm();
        form.setText( Messages.getString( "LoadingPage.LoadingConfigurationEllipsis" ) ); //$NON-NLS-1$

        Composite parent = form.getBody();
        parent.setLayout( new GridLayout() );

        FormToolkit toolkit = managedForm.getToolkit();
        toolkit.decorateFormHeading( form.getForm() );

        Composite composite = toolkit.createComposite( parent );
        composite.setLayout( new GridLayout() );
        composite.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, true, true ) );

        ProgressBar progressBar = new ProgressBar( composite, SWT.INDETERMINATE );
        progressBar.setLayoutData( new GridData( SWT.CENTER, SWT.NONE, false, false ) );

        Label label = toolkit.createLabel( composite,
            Messages.getString( "LoadingPage.LoadingTheConfigurationPleaseWait" ) ); //$NON-NLS-1$
        label.setLayoutData( new GridData( SWT.CENTER, SWT.NONE, false, false ) );
    }
}
