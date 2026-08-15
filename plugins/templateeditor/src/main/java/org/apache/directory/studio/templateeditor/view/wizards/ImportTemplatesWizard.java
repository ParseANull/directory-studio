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
import org.apache.directory.studio.templateeditor.view.preferences.PreferencesTemplatesManager;


// ── CLASS: ImportTemplatesWizard — EMPEROR'S PLAN TO RECRUIT NEW STANDING ORDERS ──
// Step by step, the Emperor recruits new standing orders from remote outposts on disk.
// He presents Luke — the user — with a directory browser and a list of XML template
// files found there. When Luke commits, each chosen file is parsed and staged in the
// {@link PreferencesTemplatesManager}. Any files that fail to parse are gathered and
// reported in a clear error dialog so no bad order sneaks into the system.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * JFace {@link Wizard} that guides the user through importing one or more template
 * XML files from an on-disk directory into the preferences staging manager. The
 * templates are staged (not committed) until the user clicks "OK" on the preference
 * page.
 *
 * <p>Think of this as the Emperor recruiting new standing orders:</p>
 * <pre>
 *   Step 1 — user browses to a directory and selects XML files
 *   "Finish" — each file is parsed and staged in PreferencesTemplatesManager
 *   Failures — reported in an error dialog with the file path
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportTemplatesWizard extends Wizard implements IImportWizard
{
    /** The wizard page */
    private ImportTemplatesWizardPage page;

    /** The templates manager */
    private PreferencesTemplatesManager manager;


    // ── CONSTRUCTOR: WIRE UP THE STAGING MANAGER ──────────────────────────────────
    // The Emperor's import plan is given the staging desk so imported templates
    // land in the staging area rather than the live manager.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ImportTemplatesWizard} that stages imported templates
     * into the given preferences manager.
     *
     * @param manager  the preferences staging manager that receives imported templates
     */
    public ImportTemplatesWizard( PreferencesTemplatesManager manager )
    {
        this.manager = manager;
    }


    // ── ADD PAGES: REGISTER THE SINGLE IMPORT PAGE ────────────────────────────────
    // The Emperor presents the first (and only) step: browse to a directory and
    // pick which XML files to import.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void addPages()
    {
        page = new ImportTemplatesWizardPage();
        addPage( page );
    }


    // ── PERFORM FINISH: EXECUTE THE IMPORT PLAN ───────────────────────────────────
    // The Emperor executes his import plan: for each selected XML file, the staging
    // manager's addTemplate() is called. Files that fail to parse are added to the
    // failedTemplates list; at the end a summary error dialog is shown if needed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean performFinish()
    {
        // Saving the dialog settings
        page.saveDialogSettings();

        // Getting the templates to be imported
        final File[] selectedTemplateFiles = page.getSelectedTemplateFiles();

        // Creating a list where all the template files that could not be
        // imported will be stored
        final List<File> failedTemplates = new ArrayList<File>();

        // Running the code to add the templates in a separate container
        // with progress monitor
        try
        {
            getContainer().run( false, false, new IRunnableWithProgress()
            {
                public void run( IProgressMonitor monitor )
                {
                    for ( File selectedTemplateFile : selectedTemplateFiles )
                    {
                        if ( !manager.addTemplate( selectedTemplateFile ) )
                        {
                            failedTemplates.add( selectedTemplateFile );
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

        // Handling the templates that could not be added
        if ( failedTemplates.size() > 0 )
        {
            String title = null;
            String message = null;

            // Only one template could not be imported
            if ( failedTemplates.size() == 1 )
            {
                title = Messages.getString( "ImportTemplatesWizard.ATemplateCouldNotBeImported" ); //$NON-NLS-1$
                message = MessageFormat.format( Messages
                    .getString( "ImportTemplatesWizard.TheTemplateCouldNotBeImported" ), failedTemplates.get( 0 ) //$NON-NLS-1$
                    .getAbsolutePath() );
            }
            // Several templates could not be imported
            else
            {
                title = Messages.getString( "ImportTemplatesWizard.SeveralTemplatesCouldNotBeImported" ); //$NON-NLS-1$
                message = Messages.getString( "ImportTemplatesWizard.TheFollowingTemplatesCouldNotBeImported" ); //$NON-NLS-1$
                for ( File failedTemplate : failedTemplates )
                {
                    message += EntryTemplatePluginUtils.LINE_SEPARATOR + "    - " + failedTemplate.getAbsolutePath(); //$NON-NLS-1$
                }
            }

            // Common ending message
            message += EntryTemplatePluginUtils.LINE_SEPARATOR + EntryTemplatePluginUtils.LINE_SEPARATOR
                + Messages.getString( "ImportTemplatesWizard.SeeTheLogsFileForMoreInformation" ); //$NON-NLS-1$

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
}
