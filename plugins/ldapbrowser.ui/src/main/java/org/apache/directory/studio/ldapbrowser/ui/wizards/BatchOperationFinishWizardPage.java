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


import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: BatchOperationFinishWizardPage — LUKE FIRES THE PROTON TORPEDO ────
// Luke is at the Death Star exhaust port. He's chosen the target, loaded the
// torpedo, and defined the attack run. Now he has to decide: fire directly on
// the connection (execute immediately), open the result in the LDIF editor
// to review, save it to a file, or put it on the clipboard for later use.
// This page is that final decision — four radio buttons, and then the trigger.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Final page of the batch operation wizard: lets the user choose how to deliver
 * the generated LDIF change records.
 * Four execution methods are available:
 * <ul>
 *   <li>Execute on connection — runs the LDIF immediately against the server.</li>
 *   <li>Generate LDIF in editor — opens the LDIF in the workspace LDIF editor.</li>
 *   <li>Generate LDIF to file — saves the LDIF to a user-chosen file.</li>
 *   <li>Generate LDIF to clipboard — copies the LDIF to the system clipboard.</li>
 * </ul>
 * The last-used settings are persisted in the dialog settings so the page
 * re-opens in a sensible state.
 * Think of Luke at the exhaust port: the torpedo is ready, now he chooses
 * how to fire it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BatchOperationFinishWizardPage extends WizardPage
{
    /** The continue on error flag key */
    public static final String EXECUTE_METHOD_DIALOGSETTING_KEY = BatchOperationFinishWizardPage.class.getName()
        + ".executeMethod"; //$NON-NLS-1$

    /** The continue on error flag key */
    public static final String CONTINUE_ON_ERROR_DIALOGSETTING_KEY = BatchOperationFinishWizardPage.class.getName()
        + ".continueOnError"; //$NON-NLS-1$

    // Execution Method Values
    /** Sentinel: no execution method chosen. */
    public final static int EXECUTION_METHOD_NONE = -1;
    /** Execute the LDIF directly on the server connection. */
    public final static int EXECUTION_METHOD_ON_CONNECTION = 0;
    /** Open the generated LDIF in the workspace LDIF editor. */
    public final static int EXECUTION_METHOD_LDIF_EDITOR = 1;
    /** Save the generated LDIF to a file. */
    public final static int EXECUTION_METHOD_LDIF_FILE = 2;
    /** Copy the generated LDIF to the system clipboard. */
    public final static int EXECUTION_METHOD_LDIF_CLIPBOARD = 3;

    // UI widgets
    private Button executeOnConnectionButton;
    private Button continueOnErrorButton;
    private Button generateLdifButton;
    private Button generateInLDIFEditorButton;
    private Button generateInFileButton;
    private Button generateInClipboardButton;

    // Listeners
    private SelectionListener validateSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            validate();
        }
    };


    // ── Luke Surveys the Launch Console ──────────────────────────────────────────
    // Luke steps up to the launch console and reads the labels: "Fire direct",
    // "Preview in editor", "Save to file", "Copy to clipboard." He starts
    // incomplete; a choice must be made before the Finish button activates.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new BatchOperationFinishWizardPage, starting incomplete.
     * The user must select an execution method before the wizard can finish.
     *
     * @param pageName  the wizard page name.
     */
    public BatchOperationFinishWizardPage( String pageName )
    {
        super( pageName );
        super.setTitle( Messages.getString( "BatchOperationFinishWizardPage.SelectExecutionMethod" ) ); //$NON-NLS-1$
        super.setDescription( Messages.getString( "BatchOperationFinishWizardPage.PleaseSelectBatchOperation" ) ); //$NON-NLS-1$
        super.setPageComplete( false );
    }


    // ── Luke Reads the Console Layout ────────────────────────────────────────────
    // The console has two top-level options ("Execute on connection" and "Generate
    // LDIF") with sub-options under each. The "Continue on error" checkbox only
    // makes sense when executing directly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the page UI: two top-level radio buttons with sub-options.
     * "Execute on connection" has a "continue on error" sub-checkbox.
     * "Generate LDIF" has three sub-radios: in-editor, in-file, in-clipboard.
     * The page reads the last-used settings from dialog settings so it
     * reopens in the previously chosen state.
     *
     * @param parent  the parent composite.
     */
    public void createControl( Composite parent )
    {
        // Composite
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout gl = new GridLayout();
        composite.setLayout( gl );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        // Execute On Connection Button
        executeOnConnectionButton = BaseWidgetUtils.createRadiobutton( composite, Messages
            .getString( "BatchOperationFinishWizardPage.ExecuteOnConnection" ), 1 ); //$NON-NLS-1$

        // Execute On Connection Composite
        Composite executeOnConnectionComposite = BaseWidgetUtils.createColumnContainer( composite, 2, 1 );

        // Continue On Error Radio Button
        BaseWidgetUtils.createRadioIndent( executeOnConnectionComposite, 1 );
        continueOnErrorButton = BaseWidgetUtils.createCheckbox( executeOnConnectionComposite, Messages
            .getString( "ImportLdifMainWizardPage.ContinueOnError" ), 1 ); //$NON-NLS-1$

        // Generate LDIF Button
        generateLdifButton = BaseWidgetUtils.createRadiobutton( composite, Messages
            .getString( "BatchOperationFinishWizardPage.GenerateLDIF" ), 1 ); //$NON-NLS-1$

        // Generate LDIF Button Composite
        Composite generateLdifButtonComposite = BaseWidgetUtils.createColumnContainer( composite, 2, 1 );

        // In The LDIF Editor Button
        BaseWidgetUtils.createRadioIndent( generateLdifButtonComposite, 1 );
        generateInLDIFEditorButton = BaseWidgetUtils.createRadiobutton( generateLdifButtonComposite, Messages
            .getString( "BatchOperationFinishWizardPage.GenerateLDIFInLDIFEditor" ), 1 ); //$NON-NLS-1$

        // In A File Button
        BaseWidgetUtils.createRadioIndent( generateLdifButtonComposite, 1 );
        generateInFileButton = BaseWidgetUtils.createRadiobutton( generateLdifButtonComposite, Messages
            .getString( "BatchOperationFinishWizardPage.GenerateLDIFInFile" ), 1 ); //$NON-NLS-1$

        // In The Clipboard Button
        BaseWidgetUtils.createRadioIndent( generateLdifButtonComposite, 1 );
        generateInClipboardButton = BaseWidgetUtils.createRadiobutton( generateLdifButtonComposite, Messages
            .getString( "BatchOperationFinishWizardPage.GenerateLDIFInClipBoard" ), 1 ); //$NON-NLS-1$

        init();
        validate();
        addListeners();

        setControl( composite );
    }


    /**
     * Initializes the UI.
     */
    private void init()
    {
        try
        {
            // Default value for the 'Execute Method' dialog setting
            if ( BrowserUIPlugin.getDefault().getDialogSettings().get( EXECUTE_METHOD_DIALOGSETTING_KEY ) == null )
            {
                BrowserUIPlugin.getDefault().getDialogSettings()
                    .put( EXECUTE_METHOD_DIALOGSETTING_KEY, EXECUTION_METHOD_ON_CONNECTION );
            }

            // Default value for the 'Continue On Error' dialog setting
            if ( BrowserUIPlugin.getDefault().getDialogSettings().get( CONTINUE_ON_ERROR_DIALOGSETTING_KEY ) == null )
            {
                BrowserUIPlugin.getDefault().getDialogSettings().put( CONTINUE_ON_ERROR_DIALOGSETTING_KEY, true );
            }

            // Getting the 'Execute Method' dialog setting
            int executeMethod = BrowserUIPlugin.getDefault().getDialogSettings()
                .getInt( EXECUTE_METHOD_DIALOGSETTING_KEY );

            switch ( executeMethod )
            {
                case EXECUTION_METHOD_ON_CONNECTION:
                    executeOnConnectionButton.setSelection( true );
                    generateInLDIFEditorButton.setSelection( true );
                    break;
                case EXECUTION_METHOD_LDIF_EDITOR:
                    generateLdifButton.setSelection( true );
                    generateInLDIFEditorButton.setSelection( true );
                    break;
                case EXECUTION_METHOD_LDIF_FILE:
                    generateLdifButton.setSelection( true );
                    generateInFileButton.setSelection( true );
                    break;
                case EXECUTION_METHOD_LDIF_CLIPBOARD:
                    generateLdifButton.setSelection( true );
                    generateInClipboardButton.setSelection( true );
                    break;
            }

            // Getting the 'Continue On Error' dialog setting
            continueOnErrorButton.setSelection( BrowserUIPlugin.getDefault().getDialogSettings()
                .getBoolean( CONTINUE_ON_ERROR_DIALOGSETTING_KEY ) );
        }
        catch ( Exception e )
        {
            // Nothing to do
        }
    }


    /**
     * Validates the page.
     */
    private void validate()
    {
        continueOnErrorButton.setEnabled( executeOnConnectionButton.getSelection() );
        generateInLDIFEditorButton.setEnabled( generateLdifButton.getSelection() );
        generateInFileButton.setEnabled( generateLdifButton.getSelection() );
        generateInClipboardButton.setEnabled( generateLdifButton.getSelection() );

        setPageComplete( getExecutionMethod() != EXECUTION_METHOD_NONE );
    }


    /**
     * Adds listeners.
     */
    private void addListeners()
    {
        executeOnConnectionButton.addSelectionListener( validateSelectionListener );
        generateLdifButton.addSelectionListener( validateSelectionListener );
    }


    // ── Luke Checks Which Trigger He's Chosen ────────────────────────────────────
    // Luke reads the selected launch mode so the wizard knows how to deliver
    // the assembled LDIF.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the currently selected execution method constant.
     * Returns {@code EXECUTION_METHOD_NONE} if neither top-level radio is
     * selected (shouldn't happen in practice).
     *
     * @return  one of {@code EXECUTION_METHOD_ON_CONNECTION},
     *          {@code EXECUTION_METHOD_LDIF_EDITOR}, {@code EXECUTION_METHOD_LDIF_FILE},
     *          {@code EXECUTION_METHOD_LDIF_CLIPBOARD}, or {@code EXECUTION_METHOD_NONE}.
     */
    public int getExecutionMethod()
    {
        if ( executeOnConnectionButton.getSelection() )
        {
            return EXECUTION_METHOD_ON_CONNECTION;
        }
        else if ( generateLdifButton.getSelection() )
        {
            if ( generateInLDIFEditorButton.getSelection() )
            {
                return EXECUTION_METHOD_LDIF_EDITOR;
            }
            else if ( generateInFileButton.getSelection() )
            {
                return EXECUTION_METHOD_LDIF_FILE;
            }
            else if ( generateInClipboardButton.getSelection() )
            {
                return EXECUTION_METHOD_LDIF_CLIPBOARD;
            }
        }

        return EXECUTION_METHOD_NONE;
    }


    // ── Luke Checks the Error-Recovery Mode ──────────────────────────────────────
    // Should we keep firing even if one torpedo misfires? This flag tells the
    // LDIF executor whether to stop on the first error or press on.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the "continue on error" checkbox is selected.
     * When {@code true}, the LDIF executor will process all entries even if some
     * fail, rather than aborting at the first error.
     *
     * @return  {@code true} if execution should continue past errors.
     */
    public boolean getContinueOnError()
    {
        return continueOnErrorButton.getSelection();
    }


    // ── Luke Logs His Launch Mode ─────────────────────────────────────────────────
    // After firing, Luke records which launch mode he used so next time the
    // console starts in the same configuration.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the selected execution method and continue-on-error flag to the
     * plugin's dialog settings so the page re-opens in the same state.
     * Called by the wizard in {@code performFinish()}.
     */
    public void saveDialogSettings()
    {
        BrowserUIPlugin.getDefault().getDialogSettings().put( EXECUTE_METHOD_DIALOGSETTING_KEY, getExecutionMethod() );
        BrowserUIPlugin.getDefault().getDialogSettings()
            .put( CONTINUE_ON_ERROR_DIALOGSETTING_KEY, getContinueOnError() );
    }
}
