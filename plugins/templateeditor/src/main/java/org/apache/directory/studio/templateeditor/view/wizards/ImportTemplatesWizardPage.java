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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import org.apache.directory.studio.templateeditor.EntryTemplatePlugin;
import org.apache.directory.studio.templateeditor.EntryTemplatePluginConstants;


// ── CLASS: ImportTemplatesWizardPage — EMPEROR'S RECRUITMENT STEP ─────────────────
// Step 1 of the Emperor's plan to recruit new standing orders: Luke — the user —
// must pick an outpost directory where the XML template files live, and then select
// which of the discovered files to bring into the Empire. The directory picker
// triggers an automatic scan for .xml files, populating the checkbox table below.
// Nothing is committed until "Finish" is clicked, and the "Finish" button stays
// locked until at least one file is ticked.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * The single page of the {@link ImportTemplatesWizard}. Provides a directory
 * browser that scans for {@code .xml} files and a checkbox table listing the
 * discovered files. The "Finish" button is only enabled when at least one file
 * is checked.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportTemplatesWizardPage extends AbstractWizardPage
{
    // UI Fields
    private Text fromDirectoryText;
    private Button fromDirectoryButton;
    private CheckboxTableViewer templateFilesTableViewer;
    private Button templateFilesTableSelectAllButton;
    private Button templateFilesTableDeselectAllButton;


    // ── CONSTRUCTOR: INITIALISE THE PAGE ──────────────────────────────────────────
    // The Emperor's import step is named, titled, described, and given a banner
    // image so the user knows exactly what this step does.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ImportTemplatesWizardPage} with no pre-selected files.
     */
    public ImportTemplatesWizardPage()
    {
        super( "ImportTemplatesWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "ImportTemplatesWizardPage.WizardPageTitle" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "ImportTemplatesWizardPage.WizardPageDescription" ) ); //$NON-NLS-1$
        setImageDescriptor( EntryTemplatePlugin.getDefault().getImageDescriptor(
            EntryTemplatePluginConstants.IMG_IMPORT_TEMPLATES_WIZARD ) );
    }


    // ── CREATE CONTROL: BUILD THE WIZARD PAGE UI ──────────────────────────────────
    // The Emperor lays out his two-part import form: the "From Directory" group
    // (directory text + Browse button) and the "Template Files" group (checkbox
    // table + Select All / Deselect All). Changing the directory triggers a rescan;
    // ticking files triggers page-completion validation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void createControl( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        // From Directory Group
        Group fromDirectoryGroup = new Group( composite, SWT.NONE );
        fromDirectoryGroup.setText( Messages.getString( "ImportTemplatesWizardPage.FromDirectory" ) ); //$NON-NLS-1$
        fromDirectoryGroup.setLayout( new GridLayout( 3, false ) );
        fromDirectoryGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // From Directory
        Label fromDirectoryLabel = new Label( fromDirectoryGroup, SWT.NONE );
        fromDirectoryLabel.setText( Messages.getString( "ImportTemplatesWizardPage.FromDirectoryColon" ) ); //$NON-NLS-1$
        fromDirectoryText = new Text( fromDirectoryGroup, SWT.BORDER );
        fromDirectoryText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        fromDirectoryText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                dialogChanged();
            }
        } );
        fromDirectoryButton = new Button( fromDirectoryGroup, SWT.PUSH );
        fromDirectoryButton.setText( Messages.getString( "ImportTemplatesWizardPage.Browse" ) ); //$NON-NLS-1$
        fromDirectoryButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                chooseFromDirectory();
            }
        } );

        // Template files Group
        Group templatesFilesGroup = new Group( composite, SWT.NONE );
        templatesFilesGroup.setText( Messages.getString( "ImportTemplatesWizardPage.TemplateFiles" ) ); //$NON-NLS-1$
        templatesFilesGroup.setLayout( new GridLayout( 2, false ) );
        templatesFilesGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Template Files Viewer
        templateFilesTableViewer = new CheckboxTableViewer( new Table( templatesFilesGroup, SWT.BORDER | SWT.CHECK
            | SWT.FULL_SELECTION ) );
        GridData templateFilesTableViewerGridData = new GridData( SWT.FILL, SWT.NONE, true, false, 1, 2 );
        templateFilesTableViewerGridData.heightHint = 125;
        templateFilesTableViewer.getTable().setLayoutData( templateFilesTableViewerGridData );
        templateFilesTableViewer.setContentProvider( new ArrayContentProvider() );
        templateFilesTableViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                if ( element instanceof File )
                {
                    return ( ( File ) element ).getName();
                }

                // Default
                return super.getText( element );
            }


            public Image getImage( Object element )
            {
                if ( element instanceof File )
                {
                    return EntryTemplatePlugin.getDefault().getImage( EntryTemplatePluginConstants.IMG_TEMPLATE );
                }

                // Default
                return super.getImage( element );
            }
        } );
        templateFilesTableViewer.addCheckStateListener( new ICheckStateListener()
        {
            public void checkStateChanged( CheckStateChangedEvent event )
            {
                dialogChanged();
            }
        } );
        templateFilesTableSelectAllButton = new Button( templatesFilesGroup, SWT.PUSH );
        templateFilesTableSelectAllButton.setText( Messages.getString( "ImportTemplatesWizardPage.SelectAll" ) ); //$NON-NLS-1$
        templateFilesTableSelectAllButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        templateFilesTableSelectAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                templateFilesTableViewer.setAllChecked( true );
                dialogChanged();
            }
        } );
        templateFilesTableDeselectAllButton = new Button( templatesFilesGroup, SWT.PUSH );
        templateFilesTableDeselectAllButton.setText( Messages.getString( "ImportTemplatesWizardPage.DeselectAll" ) ); //$NON-NLS-1$
        templateFilesTableDeselectAllButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        templateFilesTableDeselectAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                templateFilesTableViewer.setAllChecked( false );
                dialogChanged();
            }
        } );

        initFields();

        setControl( composite );
    }


    // ── INIT FIELDS: CLEAR ERROR AND BLOCK FINISH ─────────────────────────────────
    /**
     * Sets the initial UI state: no error message, page incomplete (the user must
     * select a directory and tick at least one file before "Finish" is enabled).
     */
    private void initFields()
    {
        displayErrorMessage( null );
        setPageComplete( false );
    }


    // ── CHOOSE FROM DIRECTORY: OPEN DIRECTORY BROWSER ────────────────────────────
    // The Emperor's courier opens a native directory dialog so Luke can pick the
    // outpost folder. Once chosen, the table is automatically scanned for .xml files
    // and populated.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens a native {@link DirectoryDialog} so the user can browse to an import
     * folder. Pre-fills the filter path from the current text field or the saved
     * dialog preference. On selection, updates the text field and triggers a rescan
     * via {@link #fillInTemplatesTable}.
     */
    private void chooseFromDirectory()
    {
        DirectoryDialog dialog = new DirectoryDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        dialog.setText( Messages.getString( "ImportTemplatesWizardPage.ChooseFolder" ) ); //$NON-NLS-1$
        dialog.setMessage( Messages.getString( "ImportTemplatesWizardPage.SelectTheFolderFromWhichImportTheFiles" ) ); //$NON-NLS-1$
        if ( "".equals( fromDirectoryText.getText() ) ) //$NON-NLS-1$
        {
            dialog.setFilterPath( EntryTemplatePlugin.getDefault().getPreferenceStore().getString(
                EntryTemplatePluginConstants.DIALOG_IMPORT_TEMPLATES ) );
        }
        else
        {
            dialog.setFilterPath( fromDirectoryText.getText() );
        }

        String selectedDirectory = dialog.open();
        if ( selectedDirectory != null )
        {
            fromDirectoryText.setText( selectedDirectory );
            fillInTemplatesTable( selectedDirectory );
        }
    }


    // ── FILL IN TEMPLATES TABLE: SCAN DIRECTORY FOR XML FILES ────────────────────
    // The Emperor's scout scans the chosen outpost directory for any .xml files and
    // populates the template-files table with the results so Luke can tick the ones
    // to recruit.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Scans the given directory for {@code .xml} files and populates the template
     * files table. Clears the table first; does nothing if the path does not exist.
     *
     * @param path  the directory path to scan
     */
    private void fillInTemplatesTable( String path )
    {
        List<File> files = new ArrayList<File>();
        File selectedDirectory = new File( path );
        if ( selectedDirectory.exists() )
        {
            // Filter for xml files
            FilenameFilter filter = new FilenameFilter()
            {
                public boolean accept( File dir, String name )
                {
                    return name.endsWith( ".xml" ); //$NON-NLS-1$
                }
            };

            for ( File file : selectedDirectory.listFiles( filter ) )
            {
                files.add( file );
            }
        }

        templateFilesTableViewer.setInput( files );
    }


    // ── DIALOG CHANGED: VALIDATE AND UPDATE PAGE COMPLETION ───────────────────────
    // The Emperor checks whether the plan can proceed: at least one template file
    // must be ticked before "Finish" is allowed. Without a selection, the step is
    // blocked with an error message.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current UI state and updates the error message and page-completion
     * flag. Called whenever the user changes any field.
     */
    private void dialogChanged()
    {
        // Templates table
        if ( templateFilesTableViewer.getCheckedElements().length == 0 )
        {
            displayErrorMessage( Messages
                .getString( "ImportTemplatesWizardPage.OneOrSeveralTemplateFilesMustBeSelected" ) ); //$NON-NLS-1$
            return;
        }

        displayErrorMessage( null );
    }


    // ── GET SELECTED TEMPLATE FILES: RETURN CHECKED FILES AS AN ARRAY ─────────────
    /**
     * Returns the {@link File} objects currently checked in the wizard's table
     * as a typed array.
     *
     * @return the checked template files (may be empty)
     */
    public File[] getSelectedTemplateFiles()
    {
        Object[] selectedTemplateFiles = templateFilesTableViewer.getCheckedElements();

        List<File> templateFiles = new ArrayList<File>();
        for ( Object selectedTemplateFile : selectedTemplateFiles )
        {
            templateFiles.add( ( File ) selectedTemplateFile );
        }

        return templateFiles.toArray( new File[0] );
    }


    // ── SAVE DIALOG SETTINGS: PERSIST THE DIRECTORY PREFERENCE ───────────────────
    /**
     * Persists the chosen import directory to the plugin preference store so it
     * is offered as the default path next time the wizard is opened.
     */
    public void saveDialogSettings()
    {
        EntryTemplatePlugin.getDefault().getPreferenceStore().putValue(
            EntryTemplatePluginConstants.DIALOG_IMPORT_TEMPLATES, fromDirectoryText.getText() );
    }
}
