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


import org.apache.directory.studio.openldap.config.editor.Messages;
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


// ── CLASS: LoadingPage — R2-D2 Running a Background Diagnostic ───────────────
// When Luke's X-wing is being prepared for the Battle of Yavin, R2-D2 runs a
// preflight diagnostic in the background — a quiet "working on it" signal
// while Luke waits.  LoadingPage is that preflight beep: while the background
// job is reading the OpenLDAP configuration from the server and building the
// in-memory model, we show a spinning progress bar and a "Loading, please wait"
// message so the user knows the editor is alive and working.  Once the job
// completes, this page is replaced by the real editor tabs.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An Eclipse FormPage that displays a loading indicator while the OpenLDAP
 * server configuration is being fetched and parsed in a background job.
 * Without this placeholder the multi-page editor would show blank tabs or
 * throw errors while the model isn't ready yet.
 * Think of it as R2's preflight beep: "Hold on, I'm still running the
 * diagnostic."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LoadingPage extends FormPage
{
    /** The Page ID*/
    public static final String ID = LoadingPage.class.getName(); //$NON-NLS-1$

    /** The Page Title */
    private static final String TITLE = Messages.getString( "LoadingPage.LoadingConfiguration" );


    // ── Constructor — R2 Receives the Mission Briefing ────────────────────────
    // R2-D2 is plugged into the X-wing's central computer and given the task
    // parameters.  He stores the editor reference so he knows which ship he's
    // been assigned to service.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new LoadingPage associated with the given editor.
     * The editor reference is required by FormPage so this page can be
     * registered in the multi-page editor lifecycle.
     *
     * @param editor  the FormEditor that owns this page (the config editor)
     */
    public LoadingPage( FormEditor editor )
    {
        super( editor, ID, TITLE );
    }


    // ── createFormContent — R2 Displays the Preflight Status ──────────────────
    // R2 projects a small status indicator onto the cockpit glass: a spinning
    // progress bar centered on the display and a "Loading the configuration,
    // please wait..." label below it.  No buttons, no inputs — just a clear
    // "we're on it" signal.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the loading indicator UI: a centered indeterminate progress bar
     * and a status label.
     * We use SWT.INDETERMINATE because we don't know how long the background
     * load will take — it's a spinner, not a percentage bar.
     * Called by the Eclipse Forms framework when the page is first shown.
     *
     * @param managedForm  the managed form context provided by the framework
     */
    @Override
    protected void createFormContent( IManagedForm managedForm )
    {
        ScrolledForm form = managedForm.getForm();
        form.setText( Messages.getString( "LoadingPage.LoadingConfigurationEllipsis" ) );

        Composite parent = form.getBody();
        parent.setLayout( new GridLayout() );

        FormToolkit toolkit = managedForm.getToolkit();
        toolkit.decorateFormHeading( form.getForm() );

        Composite composite = toolkit.createComposite( parent );
        composite.setLayout( new GridLayout() );
        composite.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, true, true ) );

        ProgressBar progressBar = new ProgressBar( composite, SWT.INDETERMINATE );
        progressBar.setLayoutData( new GridData( SWT.CENTER, SWT.NONE, false, false ) );

        Label label = toolkit.createLabel( composite, Messages.getString( "LoadingPage.LoadingTheConfigurationPleaseWait" ) );
        label.setLayoutData( new GridData( SWT.CENTER, SWT.NONE, false, false ) );
    }
}
