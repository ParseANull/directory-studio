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
package org.apache.directory.studio.templateeditor.view.wizards;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import org.apache.directory.studio.templateeditor.EntryTemplatePluginUtils;
import org.apache.directory.studio.templateeditor.model.Template;
import org.apache.directory.studio.templateeditor.model.parser.TemplateIO;


// ── CLASS: ExportTemplatesWizard — EMPEROR'S PLAN TO EXPORT STANDING ORDERS ───────
// Step by step, the Emperor dispatches copies of his standing orders (templates) to
// remote outposts on disk. First he presents Luke — the user — with a list of orders
// to choose from and a destination folder. Then, when Luke commits, each selected
// template is written as an XML file. Any orders that fail to reach their destination
// are reported in a clear error dialog so nothing slips through unnoticed.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * JFace {@link Wizard} that guides the user through exporting one or more templates
 * to an on-disk directory as XML files. Pre-checked objects (templates already
 * selected in the preference page) can be passed via {@link #setPreCheckedObjects}.
 *
 * <p>Think of this as the Emperor dispatching copies of standing orders:</p>
 * <pre>
 *   Step 1 — user selects templates and a destination directory
 *   "Finish" — each template is serialised to &lt;id&gt;.xml in the chosen folder
 *   Failures — reported in an error dialog with template title + id
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportTemplatesWizard extends Wizard implements IImportWizard
{
    /** The wizard page */
    private ExportTemplatesWizardPage page;

    /** The pre-checked objects */
    private Object[] preCheckedObjects;


    // ── ADD PAGES: REGISTER THE SINGLE EXPORT PAGE ────────────────────────────────
    // The Emperor presents the first (and only) step of his export plan: choose which
    // standing orders to dispatch and where to send them.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void addPages()
    {
        page = new ExportTemplatesWizardPage( preCheckedObjects );
        addPage( page );
    }


    // ── PERFORM FINISH: EXECUTE THE EXPORT PLAN ───────────────────────────────────
    // The Emperor executes his export plan: for each selected template, a FileOutputStream
    // is opened, TemplateIO serialises the template to XML, and the stream is closed.
    // Any template that fails is logged and added to the failedTemplates list;
    // at the end, a summary error dialog is shown if anything went wrong.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean performFinish()
    {
        // Saving the dialog settings
        page.saveDialogSettings();

        // Getting the selected templates and export directory
        final Template[] selectedTemplates = page.getSelectedTemplates();
        final File exportDirectory = new File( page.getExportDirectory() );

        // Creating a list where all the template files that could not be
        // exported will be stored
        final List<Template> failedTemplates = new ArrayList<Template>();

        if ( selectedTemplates != null )
        {
            try
            {
                getContainer().run( false, false, new IRunnableWithProgress()
                {
                    public void run( IProgressMonitor monitor )
                    {
                        for ( Template selectedTemplate : selectedTemplates )
                        {
                            try
                            {
                                // Creating the output stream
                                FileOutputStream fos = new FileOutputStream( new File( exportDirectory,
                                    selectedTemplate.getId() + ".xml" ) ); //$NON-NLS-1$

                                // Exporting the template
                                TemplateIO.save( selectedTemplate, fos );
                                fos.close();
                            }
                            catch ( FileNotFoundException e )
                            {
                                // Logging the error
                                EntryTemplatePluginUtils
                                    .logError(
                                        e,
                                        Messages
                                            .getString( "ExportTemplatesWizard.TheTemplateCouldNotBeExportedBecauseOfTheFollowingError" ), //$NON-NLS-1$
                                        selectedTemplate.getTitle(), selectedTemplate.getId(), e.getMessage() );

                                // Adding the template to the failed templates list
                                failedTemplates.add( selectedTemplate );
                            }
                            catch ( IOException e )
                            {
                                // Logging the error
                                EntryTemplatePluginUtils
                                    .logError(
                                        e,
                                        Messages
                                            .getString( "ExportTemplatesWizard.TheTemplateCouldNotBeExportedBecauseOfTheFollowingError" ), //$NON-NLS-1$
                                        selectedTemplate.getTitle(), selectedTemplate.getId(), e.getMessage() );

                                // Adding the template to the failed templates list
                                failedTemplates.add( selectedTemplate );
                            }
                        }
                    }
                } );
            }
            catch ( InvocationTargetException e )
            {
                // Nothing to do (it will never occur)
            }
            catch ( InterruptedException e )
            {
                // Nothing to do.
            }
        }

        // Handling the templates that could not be exported
        if ( failedTemplates.size() > 0 )
        {
            String title = null;
            String message = null;

            // Only one template could not be imported
            if ( failedTemplates.size() == 1 )
            {
                // Getting the failed template
                Template failedTemplate = failedTemplates.get( 0 );

                // Creating the title and message
                title = Messages.getString( "ExportTemplatesWizard.ATemplateCouldNotBeExported" ); //$NON-NLS-1$
                message = MessageFormat.format( Messages
                    .getString( "ExportTemplatesWizard.TheTemplateCouldNotBeExported" ), failedTemplate //$NON-NLS-1$
                    .getTitle(), failedTemplate.getId() );
            }
            // Several templates could not be imported
            else
            {
                title = Messages.getString( "ExportTemplatesWizard.SeveralTemplatesCouldNotBeExported" ); //$NON-NLS-1$
                message = Messages.getString( "ExportTemplatesWizard.TheFollowingTemplatesCouldNotBeExported" ); //$NON-NLS-1$
                for ( Template failedTemplate : failedTemplates )
                {
                    message += EntryTemplatePluginUtils.LINE_SEPARATOR + "    - " //$NON-NLS-1$
                        + MessageFormat.format( "{0} ({1})", failedTemplate.getTitle(), failedTemplate.getId() ); //$NON-NLS-1$
                }
            }

            // Common ending message
            message += EntryTemplatePluginUtils.LINE_SEPARATOR + EntryTemplatePluginUtils.LINE_SEPARATOR
                + Messages.getString( "ExportTemplatesWizard.SeeTheLogsFileForMoreInformation" ); //$NON-NLS-1$

            // Creating and opening the dialog
            MessageDialog dialog = new MessageDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                title, null, message, MessageDialog.ERROR, new String[]
                    { IDialogConstants.OK_LABEL }, MessageDialog.OK );
            dialog.open();
        }

        return true;
    }


    // ── INIT: NOTHING TO DO ───────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        // Nothing to do.
    }


    // ── SET PRE-CHECKED OBJECTS: SEED THE TEMPLATE LIST ──────────────────────────
    // The Emperor pre-selects the standing orders he wants dispatched, seeding the
    // wizard's template table with those templates already ticked.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Provides an initial set of objects that will be pre-checked when the wizard
     * page is first displayed. Call this before opening the wizard dialog.
     *
     * @param objects  the objects to pre-check (typically {@link Template} instances)
     */
    public void setPreCheckedObjects( Object[] objects )
    {
        this.preCheckedObjects = objects;
    }
}
