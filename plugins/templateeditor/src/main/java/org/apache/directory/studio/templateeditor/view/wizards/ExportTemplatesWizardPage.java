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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import org.apache.directory.studio.templateeditor.model.Template;


// ── CLASS: ExportTemplatesWizardPage — EMPEROR'S EXPORT-ORDER STEP ────────────────
// This is Step 1 of the Emperor's plan to dispatch standing orders to disk. Luke —
// the user — is presented with a sorted table of all known templates (pre-ticked
// for the ones already selected in the preference page) and a directory picker.
// When both a template selection and a valid directory are confirmed, the "Finish"
// button lights up and the export can proceed. Until then, the Emperor's plan
// remains blocked with a clear error message.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * The single page of the {@link ExportTemplatesWizard}. Shows a checkbox table
 * of all known templates and a directory-picker field. The "Finish" button is only
 * enabled when at least one template is checked and the export directory is valid.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportTemplatesWizardPage extends AbstractWizardPage
{
    /** The pre-checked objects */
    private Object[] preCheckedObjects;

    // UI Fields
    private CheckboxTableViewer templatesTableViewer;
    private Button templatesTableSelectAllButton;
    private Button templatesTableDeselectAllButton;
    private Label exportDirectoryLabel;
    private Text exportDirectoryText;
    private Button exportDirectoryButton;


    // ── CONSTRUCTOR: INITIALISE THE PAGE ──────────────────────────────────────────
    // The Emperor's export step is named, titled, described, and given a banner
    // image. The pre-checked objects (templates already selected on the pref page)
    // are stored so they can seed the checkbox table at init time.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ExportTemplatesWizardPage} with the given pre-checked objects.
     *
     * @param preCheckedObjects  templates to pre-check in the table; may be empty or {@code null}
     */
    public ExportTemplatesWizardPage( Object[] preCheckedObjects )
    {
        super( "ExportTemplatesWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "ExportTemplatesWizardPage.WizardPageTitle" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "ExportTemplatesWizardPage.WizardPageDescription" ) ); //$NON-NLS-1$
        setImageDescriptor( EntryTemplatePlugin.getDefault().getImageDescriptor(
            EntryTemplatePluginConstants.IMG_EXPORT_TEMPLATES_WIZARD ) );

        this.preCheckedObjects = preCheckedObjects;
    }


    // ── CREATE CONTROL: BUILD THE WIZARD PAGE UI ──────────────────────────────────
    // The Emperor lays out his two-part export form: the Templates group (checkbox
    // table + Select All / Deselect All) and the Export Destination group (directory
    // text + Browse button). Both groups must be satisfied before "Finish" is enabled.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void createControl( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        // Templates Group
        Group templatesGroup = new Group( composite, SWT.NONE );
        templatesGroup.setText( Messages.getString( "ExportTemplatesWizardPage.Templates" ) ); //$NON-NLS-1$
        templatesGroup.setLayout( new GridLayout( 2, false ) );
        templatesGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Templates TableViewer
        Label templatesLabel = new Label( templatesGroup, SWT.NONE );
        templatesLabel.setText( Messages.getString( "ExportTemplatesWizardPage.SelectTheTemplatesToExport" ) ); //$NON-NLS-1$
        templatesLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );
        templatesTableViewer = new CheckboxTableViewer( new Table( templatesGroup, SWT.BORDER | SWT.CHECK
            | SWT.FULL_SELECTION ) );
        GridData templatesTableViewerGridData = new GridData( SWT.FILL, SWT.NONE, true, false, 1, 2 );
        templatesTableViewerGridData.heightHint = 125;
        templatesTableViewer.getTable().setLayoutData( templatesTableViewerGridData );
        templatesTableViewer.setContentProvider( new ArrayContentProvider() );
        templatesTableViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                return ( ( Template ) element ).getTitle();
            }


            public Image getImage( Object element )
            {
                return EntryTemplatePlugin.getDefault().getImage( EntryTemplatePluginConstants.IMG_TEMPLATE );
            }
        } );
        templatesTableViewer.addCheckStateListener( new ICheckStateListener()
        {
            /**
             * Notifies of a change to the checked state of an element.
             *
             * @param event
             *      event object describing the change
             */
            public void checkStateChanged( CheckStateChangedEvent event )
            {
                dialogChanged();
            }
        } );

        templatesTableSelectAllButton = new Button( templatesGroup, SWT.PUSH );
        templatesTableSelectAllButton.setText( Messages.getString( "ExportTemplatesWizardPage.SelectAll" ) ); //$NON-NLS-1$
        templatesTableSelectAllButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        templatesTableSelectAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                templatesTableViewer.setAllChecked( true );
                dialogChanged();
            }
        } );
        templatesTableDeselectAllButton = new Button( templatesGroup, SWT.PUSH );
        templatesTableDeselectAllButton.setText( Messages.getString( "ExportTemplatesWizardPage.DeselectAll" ) ); //$NON-NLS-1$
        templatesTableDeselectAllButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        templatesTableDeselectAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                templatesTableViewer.setAllChecked( false );
                dialogChanged();
            }
        } );

        // Export Destination Group
        Group exportDestinationGroup = new Group( composite, SWT.NULL );
        exportDestinationGroup.setText( Messages.getString( "ExportTemplatesWizardPage.ExportDestination" ) ); //$NON-NLS-1$
        exportDestinationGroup.setLayout( new GridLayout( 3, false ) );
        exportDestinationGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        exportDirectoryLabel = new Label( exportDestinationGroup, SWT.NONE );
        exportDirectoryLabel.setText( Messages.getString( "ExportTemplatesWizardPage.Directory" ) ); //$NON-NLS-1$
        exportDirectoryText = new Text( exportDestinationGroup, SWT.BORDER );
        exportDirectoryText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        exportDirectoryText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                dialogChanged();
            }
        } );
        exportDirectoryButton = new Button( exportDestinationGroup, SWT.PUSH );
        exportDirectoryButton.setText( Messages.getString( "ExportTemplatesWizardPage.Browse" ) ); //$NON-NLS-1$
        exportDirectoryButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                chooseExportDirectory();
                dialogChanged();
            }
        } );

        initFields();

        setControl( composite );
    }


    // ── INIT FIELDS: POPULATE TEMPLATES TABLE WITH PRE-CHECKED STATE ──────────────
    // The Emperor seeds the export table with all known templates sorted
    // alphabetically, then ticks those already selected in the preference page.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the templates table with all templates known to the plugin's
     * {@link org.apache.directory.studio.templateeditor.TemplatesManager}, sorted
     * case-insensitively by title. Pre-checks the objects supplied at construction
     * time.
     */
    private void initFields()
    {
        // Filling the templates table
        List<Template> templates = new ArrayList<Template>();
        templates.addAll( Arrays.asList( EntryTemplatePlugin.getDefault().getTemplatesManager().getTemplates() ) );
        Collections.sort( templates, new Comparator<Template>()
        {
            public int compare( Template o1, Template o2 )
            {
                return o1.getTitle().compareToIgnoreCase( o2.getTitle() );
            }

        } );
        templatesTableViewer.setInput( templates );
        templatesTableViewer.setCheckedElements( preCheckedObjects );

        displayErrorMessage( null );
        setPageComplete( false );
    }


    // ── CHOOSE EXPORT DIRECTORY: OPEN DIRECTORY BROWSER ──────────────────────────
    // The Emperor's courier opens a native directory dialog so Luke can pick the
    // outpost folder. The last-used path from preferences is offered as the
    // starting location.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens a native {@link DirectoryDialog} so the user can browse to an export
     * folder. Pre-fills the filter path from the current text field or the saved
     * dialog preference. Updates the text field with the chosen directory.
     */
    private void chooseExportDirectory()
    {
        DirectoryDialog dialog = new DirectoryDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        dialog.setText( Messages.getString( "ExportTemplatesWizardPage.ChooseFolder" ) ); //$NON-NLS-1$
        dialog.setMessage( Messages.getString( "ExportTemplatesWizardPage.SelectTheFolderInWhichExportTheFiles" ) ); //$NON-NLS-1$
        if ( "".equals( exportDirectoryText.getText() ) ) //$NON-NLS-1$
        {
            dialog.setFilterPath( EntryTemplatePlugin.getDefault().getPreferenceStore().getString(
                EntryTemplatePluginConstants.DIALOG_EXPORT_TEMPLATES ) );
        }
        else
        {
            dialog.setFilterPath( exportDirectoryText.getText() );
        }

        String selectedDirectory = dialog.open();
        if ( selectedDirectory != null )
        {
            exportDirectoryText.setText( selectedDirectory );
        }
    }


    // ── DIALOG CHANGED: VALIDATE AND UPDATE PAGE COMPLETION ───────────────────────
    // The Emperor checks whether the plan can proceed: at least one template must be
    // selected and the directory must be a non-empty existing folder. Each violated
    // condition produces an error message that blocks "Finish".
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current UI state and updates the error message and page-completion
     * flag. Called whenever the user changes any field.
     */
    private void dialogChanged()
    {
        // Templates table
        if ( templatesTableViewer.getCheckedElements().length == 0 )
        {
            displayErrorMessage( Messages.getString( "ExportTemplatesWizardPage.OneOrSeveralTemplatesMustBeSelected" ) ); //$NON-NLS-1$
            return;
        }

        // Export Directory
        String directory = exportDirectoryText.getText();
        if ( ( directory == null ) || ( directory.equals( "" ) ) ) //$NON-NLS-1$
        {
            displayErrorMessage( Messages.getString( "ExportTemplatesWizardPage.ADirectoryMustBeSelected" ) ); //$NON-NLS-1$
            return;
        }
        else
        {
            File directoryFile = new File( directory );
            if ( !directoryFile.exists() )
            {
                displayErrorMessage( Messages.getString( "ExportTemplatesWizardPage.TheSelectedDirectoryDoesNotExist" ) ); //$NON-NLS-1$
                return;
            }
            else if ( !directoryFile.isDirectory() )
            {
                displayErrorMessage( Messages
                    .getString( "ExportTemplatesWizardPage.TheSelectedDirectoryIsNotADirectory" ) ); //$NON-NLS-1$
                return;
            }
        }

        displayErrorMessage( null );
    }


    // ── GET SELECTED TEMPLATES: RETURN CHECKED TEMPLATES AS AN ARRAY ──────────────
    /**
     * Returns the templates currently checked in the wizard's table as a typed array.
     *
     * @return the checked templates (may be empty)
     */
    public Template[] getSelectedTemplates()
    {
        Object[] selectedTemplates = templatesTableViewer.getCheckedElements();

        List<Template> templates = new ArrayList<Template>();
        for ( Object selectedTemplate : selectedTemplates )
        {
            templates.add( ( Template ) selectedTemplate );
        }

        return templates.toArray( new Template[0] );
    }


    // ── GET EXPORT DIRECTORY: RETURN THE CHOSEN DIRECTORY PATH ────────────────────
    /**
     * Returns the text currently in the export directory field.
     *
     * @return the chosen export directory path (may be empty)
     */
    public String getExportDirectory()
    {
        return exportDirectoryText.getText();
    }


    // ── SAVE DIALOG SETTINGS: PERSIST THE DIRECTORY PREFERENCE ───────────────────
    /**
     * Persists the chosen export directory to the plugin preference store so it
     * is offered as the default path next time the wizard is opened.
     */
    public void saveDialogSettings()
    {
        EntryTemplatePlugin.getDefault().getPreferenceStore().putValue(
            EntryTemplatePluginConstants.DIALOG_EXPORT_TEMPLATES, exportDirectoryText.getText() );
    }
}
