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

package org.apache.directory.studio.ldapbrowser.ui.wizards;


import java.io.File;

import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.common.widgets.FileBrowserWidget;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.BrowserConnectionWidget;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;


// ── CLASS: ImportDsmlMainWizardPage — C-3PO READS THE MISSION BRIEF ──────────
// C-3PO reads the DSML XML scroll, confirms which LDAP server to send the
// commands to, and decides whether to capture the server's response in a
// separate file. This page covers all three decisions: source file, target
// connection, and optional response capture (with "default" vs. "custom"
// response file path options).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The single page of the DSML import wizard. Provides:
 * <ul>
 *   <li>A {@link FileBrowserWidget} for selecting the source DSML (.xml) file.</li>
 *   <li>A {@link BrowserConnectionWidget} for selecting the target connection.</li>
 *   <li>A "Response" group with a "Save Response" checkbox and radio buttons
 *       for a default (input-filename + ".response.xml") or custom response path.</li>
 * </ul>
 * Validation checks: source file exists and is readable; response file (if enabled)
 * is not the same file, is not a directory, is writable or overwriteable, and has
 * a writable parent directory. A connection must also be selected.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportDsmlMainWizardPage extends WizardPage
{
    /** The wizard the page is attached to */
    private ImportDsmlWizard wizard;

    /** The extensions used by DSML files */
    private static final String[] EXTENSIONS = new String[]
        { "*.xml", "*" }; //$NON-NLS-1$ //$NON-NLS-2$

    /** The dsml file browser widget. */
    private FileBrowserWidget dsmlFileBrowserWidget;

    /** The browser connection widget. */
    private BrowserConnectionWidget browserConnectionWidget;

    /** The save response button. */
    private Button saveResponseButton;

    /** The use default response file button. */
    private Button useDefaultResponseFileButton;

    /** The use custom response file button. */
    private Button useCustomResponseFileButton;

    /** The response file browser widget. */
    private FileBrowserWidget responseFileBrowserWidget;

    /** The overwrite response file button. */
    private Button overwriteResponseFileButton;

    /** The custom response file name. */
    private String customResponseFileName;


    // ── C-3PO Accepts the Assignment ─────────────────────────────────────────────
    // The page stores the wizard reference to push state back (connection,
    // filenames, saveResponse flag) as the user edits each widget.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ImportDsmlMainWizardPage with the DSML import icon and
     * the wizard title as the page title.
     *
     * @param pageName  the wizard page name.
     * @param wizard    the parent DSML import wizard.
     */
    public ImportDsmlMainWizardPage( String pageName, ImportDsmlWizard wizard )
    {
        super( pageName );
        setTitle( wizard.getWindowTitle() );
        setDescription( Messages.getString( "ImportDsmlMainWizardPage.SelectConnectionAndDSMLFile" ) ); //$NON-NLS-1$
        setImageDescriptor( BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_IMPORT_DSML_WIZARD ) );
        setPageComplete( false );
        this.wizard = wizard;
    }


    // ── C-3PO Lays Out the Brief ──────────────────────────────────────────────────
    // Three areas: DSML source file, target connection, response capture options.
    // Each widget change pushes its value to the wizard and calls validate().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the page UI in a three-column grid:
     * <ul>
     *   <li>Row 1: "DSML File" label + FileBrowserWidget (*.xml) for the source.</li>
     *   <li>Row 2: "Import to" label + BrowserConnectionWidget for target connection.</li>
     *   <li>Rows 3+: a "Response" Group with Save Response checkbox, default/custom
     *       radio buttons, response FileBrowserWidget (save mode), and overwrite checkbox.</li>
     * </ul>
     * The default response filename is the source filename with ".response.xml" appended.
     * The response FileBrowserWidget starts disabled — enabled only when "use custom" is selected.
     *
     * @param parent  the parent composite.
     */
    public void createControl( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );

        // DSML file
        BaseWidgetUtils.createLabel( composite, Messages.getString( "ImportDsmlMainWizardPage.DSMLFile" ), 1 ); //$NON-NLS-1$
        dsmlFileBrowserWidget = new FileBrowserWidget(
            Messages.getString( "ImportDsmlMainWizardPage.SelectDSMLFile" ), EXTENSIONS, FileBrowserWidget.TYPE_OPEN ); //$NON-NLS-1$
        dsmlFileBrowserWidget.createWidget( composite );
        dsmlFileBrowserWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                wizard.setDsmlFilename( dsmlFileBrowserWidget.getFilename() );
                if ( useDefaultResponseFileButton.getSelection() )
                {
                    responseFileBrowserWidget.setFilename( dsmlFileBrowserWidget.getFilename() + ".response.xml" ); //$NON-NLS-1$
                }
                validate();
            }
        } );

        // Connection
        BaseWidgetUtils.createLabel( composite, Messages.getString( "ImportDsmlMainWizardPage.ImportTo" ), 1 ); //$NON-NLS-1$
        browserConnectionWidget = new BrowserConnectionWidget( wizard.getImportConnection() );
        browserConnectionWidget.createWidget( composite );
        browserConnectionWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                wizard.setImportConnection( browserConnectionWidget.getBrowserConnection() );
                validate();
            }
        } );

        // Save Response
        Composite responseOuterComposite = BaseWidgetUtils.createColumnContainer( composite, 1, 3 );
        Group responseGroup = BaseWidgetUtils.createGroup( responseOuterComposite, Messages
            .getString( "ImportDsmlMainWizardPage.Response" ), 1 ); //$NON-NLS-1$
        Composite responseContainer = BaseWidgetUtils.createColumnContainer( responseGroup, 3, 1 );

        saveResponseButton = BaseWidgetUtils.createCheckbox( responseContainer, Messages
            .getString( "ImportDsmlMainWizardPage.SaveResponse" ), 3 ); //$NON-NLS-1$
        saveResponseButton.setSelection( true );
        wizard.setSaveResponse( saveResponseButton.getSelection() );
        saveResponseButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                wizard.setSaveResponse( saveResponseButton.getSelection() );
                useDefaultResponseFileButton.setEnabled( saveResponseButton.getSelection() );
                useCustomResponseFileButton.setEnabled( saveResponseButton.getSelection() );
                responseFileBrowserWidget.setEnabled( saveResponseButton.getSelection()
                    && useCustomResponseFileButton.getSelection() );
                overwriteResponseFileButton.setEnabled( saveResponseButton.getSelection() );
                validate();
            }
        } );

        BaseWidgetUtils.createRadioIndent( responseContainer, 1 );
        useDefaultResponseFileButton = BaseWidgetUtils.createRadiobutton( responseContainer, Messages
            .getString( "ImportDsmlMainWizardPage.UseDefaultResponse" ), 2 ); //$NON-NLS-1$
        useDefaultResponseFileButton.setSelection( true );
        useDefaultResponseFileButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                String temp = customResponseFileName;
                responseFileBrowserWidget.setFilename( dsmlFileBrowserWidget.getFilename() + ".response.xml" ); //$NON-NLS-1$
                responseFileBrowserWidget.setEnabled( false );
                customResponseFileName = temp;
                validate();
            }
        } );

        BaseWidgetUtils.createRadioIndent( responseContainer, 1 );
        useCustomResponseFileButton = BaseWidgetUtils.createRadiobutton( responseContainer, Messages
            .getString( "ImportDsmlMainWizardPage.UseCustomResponse" ), //$NON-NLS-1$
            2 );
        useCustomResponseFileButton.setSelection( false );
        useCustomResponseFileButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                responseFileBrowserWidget.setFilename( customResponseFileName != null ? customResponseFileName : "" ); //$NON-NLS-1$
                responseFileBrowserWidget.setEnabled( true );
                validate();
            }
        } );

        BaseWidgetUtils.createRadioIndent( responseContainer, 1 );
        responseFileBrowserWidget = new FileBrowserWidget(
            Messages.getString( "ImportDsmlMainWizardPage.SelectSaveFile" ), EXTENSIONS, FileBrowserWidget.TYPE_SAVE ); //$NON-NLS-1$
        responseFileBrowserWidget.createWidget( responseContainer );
        responseFileBrowserWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                customResponseFileName = responseFileBrowserWidget.getFilename();
                wizard.setResponseFilename( customResponseFileName );
                validate();
            }
        } );
        responseFileBrowserWidget.setEnabled( false );

        BaseWidgetUtils.createRadioIndent( responseContainer, 1 );
        overwriteResponseFileButton = BaseWidgetUtils.createCheckbox( responseContainer, Messages
            .getString( "ImportDsmlMainWizardPage.OverwriteExistingResponseFile" ), 2 ); //$NON-NLS-1$
        overwriteResponseFileButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                validate();
            }
        } );

        setControl( composite );
    }


    /**
     * Validates the page. This method is responsible for displaying errors, as well as enabling/disabling the "Finish" button
     */
    private void validate()
    {
        boolean ok = true;

        File dsmlFile = new File( dsmlFileBrowserWidget.getFilename() );
        if ( "".equals( dsmlFileBrowserWidget.getFilename() ) ) //$NON-NLS-1$
        {
            setErrorMessage( null );
            ok = false;
        }
        else if ( !dsmlFile.isFile() || !dsmlFile.exists() )
        {
            setErrorMessage( Messages.getString( "ImportDsmlMainWizardPage.ErrorSelectedDSMLNotExist" ) ); //$NON-NLS-1$
            ok = false;
        }
        else if ( !dsmlFile.canRead() )
        {
            setErrorMessage( Messages.getString( "ImportDsmlMainWizardPage.ErrorSelectedDSMLNotReadable" ) ); //$NON-NLS-1$
            ok = false;
        }
        else if ( saveResponseButton.getSelection() )
        {
            File responseFile = new File( responseFileBrowserWidget.getFilename() );
            File responseFileDirectory = responseFile.getParentFile();

            if ( responseFile.equals( dsmlFile ) )
            {
                setErrorMessage( Messages.getString( "ImportDsmlMainWizardPage.ErrorDSMLFileAndResponseFileEqual" ) ); //$NON-NLS-1$
                ok = false;
            }
            else if ( responseFile.isDirectory() )
            {
                setErrorMessage( Messages.getString( "ImportDsmlMainWizardPage.ErrorSelectedResponseFileNotFile" ) ); //$NON-NLS-1$
                ok = false;
            }
            else if ( responseFile.exists() && !overwriteResponseFileButton.getSelection() )
            {
                setErrorMessage( Messages.getString( "ImportDsmlMainWizardPage.ErrorSelecedResponseFileExist" ) ); //$NON-NLS-1$
                ok = false;
            }
            else if ( responseFile.exists() && !responseFile.canWrite() )
            {
                setErrorMessage( Messages.getString( "ImportDsmlMainWizardPage.ErrorSelectedResponseFileNotWritable" ) ); //$NON-NLS-1$
                ok = false;
            }
            else if ( responseFile.getParentFile() == null )
            {
                setErrorMessage( Messages
                    .getString( "ImportDsmlMainWizardPage.ErrorSelectedResponseFileDirectoryNotWritable" ) ); //$NON-NLS-1$
                ok = false;
            }
            else if ( !responseFile.exists() && ( responseFileDirectory == null || !responseFileDirectory.canWrite() ) )
            {
                setErrorMessage( Messages
                    .getString( "ImportDsmlMainWizardPage.ErrorSelectedResponseFileDirectoryNotWritable" ) ); //$NON-NLS-1$
                ok = false;
            }
        }

        if ( ( wizard.getImportConnection() == null ) || ( browserConnectionWidget.getBrowserConnection() == null ) )
        {
            setErrorMessage( Messages.getString( "ImportDsmlMainWizardPage.PleaseSelectConnection" ) ); //$NON-NLS-1$
            ok = false;
        }

        if ( ok )
        {
            setErrorMessage( null );
        }
        setPageComplete( ok );
        getContainer().updateButtons();
    }


    // ── C-3PO Logs the File Selection for Next Time ───────────────────────────────
    // Persists the chosen directory so the browser opens in the same place next time.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the dialog settings (source file browser directory) so the
     * FileBrowserWidget opens in the same directory on the next use.
     */
    public void saveDialogSettings()
    {
        dsmlFileBrowserWidget.saveDialogSettings();
    }
}
