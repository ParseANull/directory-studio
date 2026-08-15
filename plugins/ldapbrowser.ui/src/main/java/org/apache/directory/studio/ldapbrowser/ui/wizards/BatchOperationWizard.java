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


import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserSelectionUtils;
import org.apache.directory.studio.ldapbrowser.common.actions.CopyAction;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.jobs.ExecuteLdifRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.SearchRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldifeditor.editor.LdifEditor;
import org.apache.directory.studio.ldifeditor.editor.NonExistingLdifEditorInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.texteditor.IDocumentProvider;


// ── CLASS: BatchOperationWizard — LUKE'S JOURNEY FROM TATOOINE TO THRONE ROOM ─
// Luke doesn't just wander — his journey has stages: choose the target (applyOn),
// pick the weapon (type), craft the LDIF or modify fragment, set execution options
// (finish), and then act. The BatchOperationWizard is that five-stage journey
// applied to LDAP: it guides the user from "which entries?" through "what
// operation?" to "where should the result go?" and finally executes it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Multi-step wizard that applies a single LDAP modification (modify, delete, or
 * execute-LDIF) to a batch of directory entries.
 * The wizard has five pages: ApplyOn (pick the entry set), Type (choose the
 * operation), Ldif (write a raw LDIF fragment), Modify (use the attribute GUI),
 * and Finish (choose how to execute the result). Not all pages are shown for
 * every operation type — the {@link #getNextPage} logic skips pages that don't
 * apply to the chosen operation.
 * Think of Luke's hero journey: each wizard page is a stage, and {@code performFinish}
 * is the moment Luke fires the proton torpedo into the exhaust port.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BatchOperationWizard extends Wizard implements INewWizard
{
    /** The connection */
    private IBrowserConnection connection;

    // Wizard pages
    private BatchOperationApplyOnWizardPage applyOnPage;
    private BatchOperationTypeWizardPage typePage;
    private BatchOperationLdifWizardPage ldifPage;
    private BatchOperationModifyWizardPage modifyPage;
    private BatchOperationFinishWizardPage finishPage;


    // ── Luke Prepares for the Journey ────────────────────────────────────────────
    // Luke sets the window title and confirms he'll need a progress monitor —
    // the operation may take time as it touches many entries.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new BatchOperationWizard, setting the window title and enabling
     * the progress monitor so long-running operations don't freeze the UI.
     */
    public BatchOperationWizard()
    {
        super.setWindowTitle( Messages.getString( "BatchOperationWizard.BatchOperation" ) ); //$NON-NLS-1$
        super.setNeedsProgressMonitor( true );
    }


    // ── Luke Looks Up His Starting Point ─────────────────────────────────────────
    // Luke needs to know which mission he's been given before the journey starts.
    // We return the Eclipse wizard ID that identifies this wizard in the registry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse wizard ID for the batch operation wizard.
     * Used by action handlers that open this wizard programmatically.
     *
     * @return  the wizard ID string from {@link BrowserUIConstants}.
     */
    public static String getId()
    {
        return BrowserUIConstants.WIZARD_BATCH_OPERATION;
    }


    // ── Luke Receives the Mission Brief ──────────────────────────────────────────
    // The Rebel briefing room shows Luke his target; we receive the workbench
    // and selection context — but the real setup happens in addPages().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Called by Eclipse when the wizard is opened from a menu or action.
     * The actual page setup happens in {@link #addPages()} where we can
     * inspect the current workbench selection.
     *
     * @param workbench   the current workbench.
     * @param selection   the current structured selection (unused here).
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        // PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection()
    }


    // ── Luke Charts the Course ───────────────────────────────────────────────────
    // Before launching, Luke checks whether there's a valid target: an open
    // connection or selected entries. If there's nothing to operate on, we add a
    // dummy page that explains why the wizard can't proceed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Inspects the current workbench selection and adds wizard pages accordingly.
     * If at least one connected connection or one selected entry/search/bookmark
     * is present, the full five-page flow is added. Otherwise a single DummyWizardPage
     * is shown explaining that no connection is selected.
     * We also derive the {@code connection} from the selection so {@link #performFinish}
     * knows which server to run the operation against.
     */
    public void addPages()
    {

        ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService()
            .getSelection();
        Connection[] connections = BrowserSelectionUtils.getConnections( selection );
        ISearch[] searches = BrowserSelectionUtils.getSearches( selection );
        IEntry[] entries = BrowserSelectionUtils.getEntries( selection );
        ISearchResult[] searchResults = BrowserSelectionUtils.getSearchResults( selection );
        IBookmark[] bookmarks = BrowserSelectionUtils.getBookmarks( selection );
        IAttribute[] attributes = BrowserSelectionUtils.getAttributes( selection );
        IValue[] values = BrowserSelectionUtils.getValues( selection );

        // if(searches.length + entries.length + searchResults.length +
        // bookmarks.length > 0) {
        if ( connections.length > 0
            && connections[0].getConnectionWrapper().isConnected()
            || searches.length + entries.length + searchResults.length + bookmarks.length + attributes.length
                + values.length > 0 )
        {

            ISearch search = BrowserSelectionUtils.getExampleSearch( selection );
            search.setName( null );
            this.connection = search.getBrowserConnection();

            applyOnPage = new BatchOperationApplyOnWizardPage( BatchOperationApplyOnWizardPage.class.getName(), this );
            addPage( applyOnPage );

            typePage = new BatchOperationTypeWizardPage( BatchOperationTypeWizardPage.class.getName(), this );
            addPage( typePage );

            ldifPage = new BatchOperationLdifWizardPage( BatchOperationLdifWizardPage.class.getName(), this );
            addPage( ldifPage );

            modifyPage = new BatchOperationModifyWizardPage( BatchOperationModifyWizardPage.class.getName(), this );
            addPage( modifyPage );

            finishPage = new BatchOperationFinishWizardPage( BatchOperationFinishWizardPage.class.getName() );
            addPage( finishPage );
        }
        else
        {
            IWizardPage page = new DummyWizardPage();
            addPage( page );
        }
    }


    // ── Luke Briefs the Crew ─────────────────────────────────────────────────────
    // After the page controls are built, Luke registers the help context so
    // pressing F1 on any page opens the right documentation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Wires up the Eclipse help context for every page after the SWT controls
     * are created, so F1 on any page opens the batch operation wizard help article.
     *
     * @param pageContainer  the wizard's page container composite.
     */
    public void createPageControls( Composite pageContainer )
    {
        super.createPageControls( pageContainer );

        // set help context ID
        PlatformUI.getWorkbench().getHelpSystem().setHelp( applyOnPage.getControl(),
            BrowserUIConstants.PLUGIN_ID + "." + "tools_batchoperation_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
        PlatformUI.getWorkbench().getHelpSystem().setHelp( typePage.getControl(),
            BrowserUIConstants.PLUGIN_ID + "." + "tools_batchoperation_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
        PlatformUI.getWorkbench().getHelpSystem().setHelp( ldifPage.getControl(),
            BrowserUIConstants.PLUGIN_ID + "." + "tools_batchoperation_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
        PlatformUI.getWorkbench().getHelpSystem().setHelp( modifyPage.getControl(),
            BrowserUIConstants.PLUGIN_ID + "." + "tools_batchoperation_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
        PlatformUI.getWorkbench().getHelpSystem().setHelp( finishPage.getControl(),
            BrowserUIConstants.PLUGIN_ID + "." + "tools_batchoperation_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    // ── Luke's Journey Has a Branching Path ──────────────────────────────────────
    // Luke's path doesn't always go through the same rooms: if the type page
    // says "LDIF", skip Modify; if it says "Modify", skip LDIF; if "Delete",
    // skip both and jump straight to Finish.
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * This private class implements a dummy wizard page that is displayed when no connection is selected.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class DummyWizardPage extends WizardPage
    {
        // ── Luke Has No Mission to Run ────────────────────────────────────────────
        // The Rebel briefing room is empty — no target, no mission. We show the
        // user a polite explanation rather than letting the wizard sit blank.
        // ────────────────────────────────────────────────────────────────────────
        /**
         * Creates a DummyWizardPage shown when no connected connection is selected.
         * The page is immediately complete (the user can't do anything else) but
         * explains that they need to select an open connection first.
         */
        protected DummyWizardPage()
        {
            super( "" ); //$NON-NLS-1$
            super.setTitle( Messages.getString( "BatchOperationWizard.NoConnectionSelected" ) ); //$NON-NLS-1$
            super.setDescription( Messages.getString( "BatchOperationWizard.SelectOpenConnection" ) ); //$NON-NLS-1$
            // super.setImageDescriptor(BrowserUIPlugin.getDefault().getImageDescriptor(BrowserUIConstants.IMG_ENTRY_WIZARD));
            super.setPageComplete( true );
        }


        // ── Luke Looks at an Empty Briefing Room ──────────────────────────────────
        // Nothing to show, just an empty composite — the message is in the title.
        // ────────────────────────────────────────────────────────────────────────
        /**
         * {@inheritDoc}
         *
         * Creates an empty composite — the page's title and description carry
         * the full message; no additional widgets are needed.
         *
         * @param parent  the parent composite.
         */
        public void createControl( Composite parent )
        {
            Composite composite = new Composite( parent, SWT.NONE );
            GridLayout gl = new GridLayout( 1, false );
            composite.setLayout( gl );
            composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

            setControl( composite );
        }
    }


    // ── Luke Navigates the Branching Path ────────────────────────────────────────
    // At each junction Luke checks the type-page decision: LDIF → go to ldifPage,
    // Modify → go to modifyPage, Delete → jump to finishPage.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the next wizard page based on the current page and the selected
     * operation type. The routing logic is:
     * <ul>
     *   <li>applyOnPage → typePage (always)</li>
     *   <li>typePage + CREATE_LDIF → ldifPage</li>
     *   <li>typePage + MODIFY → modifyPage</li>
     *   <li>typePage + DELETE → finishPage (no modification input needed)</li>
     *   <li>modifyPage → finishPage</li>
     *   <li>ldifPage → finishPage</li>
     * </ul>
     *
     * @param page  the page the user is currently on.
     * @return      the next page to show, or {@code null} at the end.
     */
    public IWizardPage getNextPage( IWizardPage page )
    {
        if ( this.applyOnPage != null )
        {

            if ( page == this.applyOnPage )
            {
                return this.typePage;
            }

            else if ( page == this.typePage
                && this.typePage.getOperationType() == BatchOperationTypeWizardPage.OPERATION_TYPE_CREATE_LDIF )
            {
                return this.ldifPage;
            }
            else if ( page == this.typePage
                && this.typePage.getOperationType() == BatchOperationTypeWizardPage.OPERATION_TYPE_MODIFY )
            {
                return this.modifyPage;
            }
            else if ( page == this.typePage
                && this.typePage.getOperationType() == BatchOperationTypeWizardPage.OPERATION_TYPE_DELETE )
            {
                return this.finishPage;
            }

            else if ( page == this.modifyPage )
            {
                return this.finishPage;
            }
            else if ( page == this.ldifPage )
            {
                return this.finishPage;
            }
        }

        return null;
    }


    // ── Luke Checks Whether All Stages Are Complete ───────────────────────────────
    // The proton torpedo can only be fired when every stage of the journey is
    // done: the target chosen, the weapon loaded, the execution method set.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns {@code true} only when every relevant page reports complete.
     * We skip the LDIF or Modify page depending on the chosen operation type
     * so the Finish button activates as soon as all required pages are filled.
     *
     * @return  {@code true} if the wizard is ready to execute.
     */
    public boolean canFinish()
    {
        if ( this.applyOnPage != null )
        {
            if ( !this.applyOnPage.isPageComplete() )
            {
                return false;
            }
            if ( !this.typePage.isPageComplete() )
            {
                return false;
            }

            if ( this.typePage.getOperationType() == BatchOperationTypeWizardPage.OPERATION_TYPE_CREATE_LDIF
                && !this.ldifPage.isPageComplete() )
            {
                return false;
            }
            if ( this.typePage.getOperationType() == BatchOperationTypeWizardPage.OPERATION_TYPE_MODIFY
                && !this.modifyPage.isPageComplete() )
            {
                return false;
            }

            if ( !this.finishPage.isPageComplete() )
            {
                return false;
            }
        }

        return true;
    }


    // ── Luke Cancels the Mission ─────────────────────────────────────────────────
    // Sometimes the mission is aborted — the Death Star plans were already
    // delivered and there's no need to fire. We always allow cancel.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Always returns {@code true} — cancelling the wizard is always safe.
     *
     * @return  {@code true}.
     */
    public boolean performCancel()
    {
        return true;
    }


    // ── Luke Fires the Proton Torpedo ────────────────────────────────────────────
    // The targeting computer locks on, Luke trusts the Force, and the torpedo
    // flies into the exhaust port. This method assembles the full LDIF for all
    // target DNs and either executes it on the server, saves it to a file,
    // opens it in the LDIF editor, or copies it to the clipboard.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Assembles the complete LDIF change record for every target DN, then
     * delivers it according to the finish page's chosen execution method:
     * <ul>
     *   <li>Execute on connection — runs {@link ExecuteLdifRunnable} against the server.</li>
     *   <li>LDIF editor — opens the generated LDIF in the workspace LDIF editor.</li>
     *   <li>LDIF file — saves the generated LDIF to a file chosen by the user.</li>
     *   <li>Clipboard — copies the generated LDIF to the system clipboard.</li>
     * </ul>
     * If the applyOn page chose a search, we run the search first to resolve the DNs.
     *
     * @return  {@code true} if the operation was completed successfully; {@code false} otherwise.
     */
    public boolean performFinish()
    {
        if ( this.applyOnPage != null )
        {
            this.applyOnPage.saveDialogSettings();
            this.finishPage.saveDialogSettings();

            // get LDIF
            String ldifFragment = ""; //$NON-NLS-1$
            if ( typePage.getOperationType() == BatchOperationTypeWizardPage.OPERATION_TYPE_CREATE_LDIF )
            {
                ldifFragment = this.ldifPage.getLdifFragment();
            }
            else if ( typePage.getOperationType() == BatchOperationTypeWizardPage.OPERATION_TYPE_MODIFY )
            {
                ldifFragment = this.modifyPage.getLdifFragment();
            }
            if ( typePage.getOperationType() == BatchOperationTypeWizardPage.OPERATION_TYPE_DELETE )
            {
                ldifFragment = "changetype: delete" + BrowserCoreConstants.LINE_SEPARATOR; //$NON-NLS-1$
            }

            // get DNs
            Dn[] dns = applyOnPage.getApplyOnDns();
            if ( dns == null )
            {
                if ( applyOnPage.getApplyOnSearch() != null )
                {
                    ISearch search = applyOnPage.getApplyOnSearch();
                    if ( search.getBrowserConnection() != null )
                    {
                        search.setSearchResults( null );
                        SearchRunnable runnable = new SearchRunnable( new ISearch[]
                            { search } );
                        IStatus status = RunnableContextRunner.execute( runnable, getContainer(), true );
                        if ( status.isOK() )
                        {
                            ISearchResult[] srs = search.getSearchResults();
                            dns = new Dn[srs.length];
                            for ( int i = 0; i < srs.length; i++ )
                            {
                                dns[i] = srs[i].getDn();
                            }
                        }
                    }
                }
            }

            if ( dns != null )
            {
                StringBuffer ldif = new StringBuffer();
                for ( int i = 0; i < dns.length; i++ )
                {
                    ldif.append( "dn: " ); //$NON-NLS-1$
                    ldif.append( dns[i].getName() );
                    ldif.append( BrowserCoreConstants.LINE_SEPARATOR );
                    ldif.append( ldifFragment );
                    ldif.append( BrowserCoreConstants.LINE_SEPARATOR );
                }

                if ( finishPage.getExecutionMethod() == BatchOperationFinishWizardPage.EXECUTION_METHOD_LDIF_EDITOR )
                {
                    // Opening an LDIF Editor with the LDIF content
                    try
                    {
                        IEditorInput input = new NonExistingLdifEditorInput();
                        IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                            .openEditor( input, LdifEditor.getId() );
                        IDocumentProvider documentProvider = ( ( LdifEditor ) editor ).getDocumentProvider();
                        if ( documentProvider != null )
                        {
                            IDocument document = documentProvider.getDocument( input );
                            if ( document != null )
                            {
                                document.set( ldif.toString() );
                            }
                        }
                    }
                    catch ( PartInitException e )
                    {
                        return false;
                    }

                    return true;
                }
                else if ( finishPage.getExecutionMethod() == BatchOperationFinishWizardPage.EXECUTION_METHOD_LDIF_FILE ) // TODO
                {
                    // Saving the LDIF to a file

                    // Getting the shell
                    Shell shell = Display.getDefault().getActiveShell();

                    // detect IDE or RCP:
                    // check if perspective org.eclipse.ui.resourcePerspective is available
                    boolean isIDE = CommonUIUtils.isIDEEnvironment();

                    if ( isIDE )
                    {
                        // Asking the user for the location where to 'save as' the file
                        SaveAsDialog dialog = new SaveAsDialog( shell );

                        if ( dialog.open() != Dialog.OK )
                        {
                            return false;
                        }

                        // Getting if the resulting file
                        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile( dialog.getResult() );

                        try
                        {
                            // Creating the file if it does not exist
                            if ( !file.exists() )
                            {
                                file.create( new ByteArrayInputStream( "".getBytes() ), true, null ); //$NON-NLS-1$
                            }

                            // Saving the LDIF to the file in the workspace
                            file.setContents( new ByteArrayInputStream( ldif.toString().getBytes() ), true, true,
                                new NullProgressMonitor() );
                        }
                        catch ( Exception e )
                        {
                            return false;
                        }
                    }
                    else
                    {
                        boolean canOverwrite = false;
                        String path = null;

                        while ( !canOverwrite )
                        {
                            // Open FileDialog
                            FileDialog dialog = new FileDialog( shell, SWT.SAVE );
                            path = dialog.open();
                            if ( path == null )
                            {
                                return false;
                            }

                            // Check whether file exists and if so, confirm overwrite
                            final File externalFile = new File( path );
                            if ( externalFile.exists() )
                            {
                                String question = NLS.bind( Messages
                                    .getString( "BatchOperationWizard.TheFileAlreadyExistsReplace" ), path ); //$NON-NLS-1$
                                MessageDialog overwriteDialog = new MessageDialog( shell, Messages
                                    .getString( "BatchOperationWizard.Question" ), null, question, //$NON-NLS-1$
                                    MessageDialog.QUESTION, new String[]
                                        {
                                            IDialogConstants.YES_LABEL,
                                            IDialogConstants.NO_LABEL,
                                            IDialogConstants.CANCEL_LABEL }, 0 );
                                int overwrite = overwriteDialog.open();
                                switch ( overwrite )
                                {
                                    case 0: // Yes
                                        canOverwrite = true;
                                        break;
                                    case 1: // No
                                        break;
                                    case 2: // Cancel
                                    default:
                                        return false;
                                }
                            }
                            else
                            {
                                canOverwrite = true;
                            }
                        }

                        // Saving the LDIF to the file on disk
                        try
                        {
                            BufferedWriter outFile = new BufferedWriter( new FileWriter( path ) );
                            outFile.write( ldif.toString() );
                            outFile.close();
                        }
                        catch ( Exception e )
                        {
                            return false;
                        }
                    }

                    return true;
                }
                else if ( finishPage.getExecutionMethod() == BatchOperationFinishWizardPage.EXECUTION_METHOD_LDIF_CLIPBOARD )
                {
                    // Copying the LDIF to the clipboard
                    CopyAction.copyToClipboard( new Object[]
                        { ldif.toString() }, new Transfer[]
                        { TextTransfer.getInstance() } );

                    return true;
                }
                else if ( finishPage.getExecutionMethod() == BatchOperationFinishWizardPage.EXECUTION_METHOD_ON_CONNECTION )
                {
                    // Executing the LDIF on the connection
                    ExecuteLdifRunnable runnable = new ExecuteLdifRunnable( getConnection(), ldif.toString(), true,
                        finishPage.getContinueOnError() );
                    StudioBrowserJob job = new StudioBrowserJob( runnable );
                    job.execute();

                    return true;
                }
            }

            return false;
        }

        return true;
    }


    // ── Luke Checks the Type Page ─────────────────────────────────────────────────
    // The type page tells Luke which weapon to use — modify, delete, or LDIF.
    // Other pages query this to know what to show.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the type wizard page so other pages can query the chosen operation type.
     * The LDIF page and Modify page each check the type to decide whether they're
     * relevant for the current {@link #isPageComplete} evaluation.
     *
     * @return  the {@link BatchOperationTypeWizardPage}.
     */
    public BatchOperationTypeWizardPage getTypePage()
    {
        return typePage;
    }


    // ── Luke Knows His Server ─────────────────────────────────────────────────────
    // Luke knows which base to attack — the connection is the target server.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the browser connection that the batch operation will run against.
     * Derived from the workbench selection when the wizard was opened.
     *
     * @return  the target {@link IBrowserConnection}.
     */
    public IBrowserConnection getConnection()
    {
        return this.connection;
    }
}
