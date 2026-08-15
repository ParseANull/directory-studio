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
package org.apache.directory.studio.openldap.config.editor.dialogs;


import org.apache.directory.studio.common.ui.AddEditDialog;
import org.apache.directory.studio.common.ui.CommonUIConstants;
import org.apache.directory.studio.common.ui.CommonUIPlugin;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.apache.directory.studio.openldap.config.editor.wrappers.LimitWrapper;
import org.apache.directory.studio.openldap.config.editor.wrappers.SizeLimitWrapper;
import org.apache.directory.studio.openldap.config.editor.wrappers.TimeLimitWrapper;


// Like Princess Leia's hologram presenting a dual-channel briefing where
// the operator can choose between a size-limit directive and a time-limit
// directive — but not both simultaneously — we give the administrator a
// radio-button selector to pick the limit type and then either type the
// value directly or open the appropriate sub-dialog via an Edit button.
/**
 * The SizeTimeLimitDialog is used to edit either a size limit or a time
 * limit parameter. The operator selects which type they want via radio
 * buttons, then enters or edits the value in the corresponding text field
 * (or via the "Edit..." button that opens the dedicated SizeLimitDialog or
 * TimeLimitDialog).
 *
 * <p>The dialog overlay is like:
 * <pre>
 * +--------------------------------------------------------------------------+
 * | Limit                                                                    |
 * | .----------------------------------------------------------------------. |
 * | | (o) Size Limit  : [                                      ] (Edit...) | |
 * | | (o) TimeLimit :   [                                      ] (Edit...) | |
 * | '----------------------------------------------------------------------' |
 * |                                                                          |
 * |  (Cancel)                                                         (OK)   |
 * +--------------------------------------------------------------------------+
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SizeTimeLimitDialog extends AddEditDialog<LimitWrapper>
{
    /** The TimeLimit radio button */
    private Button timeLimitButton;

    /** The Text that contains the TimeLimit (either as typed or as built from the TimeLimitDialog) */
    private Text timeLimitText;

    /** A Button used to edit the TimeLimit value */
    private Button timeLimitEditButton;

    /** The SizeLimit radio button */
    private Button sizeLimitButton;

    /** The Text that contains the SizeLimit (either as typed or as built from the SizeLimitDialog) */
    private Text sizeLimitText;

    /** A Button used to edit the SizeLimit value */
    private Button sizeLimitEditButton;

    /**
     * Listeners for the Selector radioButtons. It will enable or disable the dnSpec or Group accordingly
     * to the selection.
     **/
    private SelectionListener sizeTimeButtonsSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent event )
        {
            if ( event.getSource() instanceof Button )
            {
                Button button = ( Button ) event.getSource();

                if ( button == sizeLimitButton )
                {
                    if ( button.getSelection() )
                    {
                        setEditedElement( new SizeLimitWrapper( "" ) );

                        // Enable the SizeLimit elements, disable the TimeLimit ones
                        sizeLimitEditButton.setEnabled( true );
                        sizeLimitText.setEnabled( true );
                        timeLimitEditButton.setEnabled( false );
                        timeLimitText.setEnabled( false );
                        timeLimitText.clearSelection();
                    }
                }
                else
                {
                    setEditedElement( new TimeLimitWrapper( "" ) );

                    // Enable the TimeLimit elements, disable the SizeLimit ones
                    timeLimitEditButton.setEnabled( true );
                    timeLimitText.setEnabled( true );
                    sizeLimitEditButton.setEnabled( false );
                    sizeLimitText.setEnabled( false );
                    sizeLimitText.clearSelection();
                }
            }
        }
    };


    /**
     * The listener for the sizeLimit Text
     */
    private SelectionListener sizeLimitEditSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            SizeLimitDialog dialog = new SizeLimitDialog( sizeLimitText.getShell(), sizeLimitText.getText() );

            if ( dialog.open() == OverlayDialog.OK )
            {
                String newSizeLimitStr = dialog.getNewLimit();

                if ( newSizeLimitStr != null )
                {
                    sizeLimitText.setText( newSizeLimitStr );
                }
            }
        }
    };


    /**
     * The listener for the timeLimit Text
     */
    private SelectionListener timeLimitEditSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            TimeLimitDialog dialog = new TimeLimitDialog( timeLimitText.getShell(), timeLimitText.getText() );

            if ( dialog.open() == OverlayDialog.OK )
            {
                String newTimeLimitStr = dialog.getNewLimit();

                if ( newTimeLimitStr != null )
                {
                    timeLimitText.setText( newTimeLimitStr );
                }
            }
        }
    };


    protected ModifyListener sizeLimitTextListener = event ->
        {
            Button okButton = getButton( IDialogConstants.OK_ID );

            // This button might be null when the dialog is called.
            if ( okButton == null )
            {
                return;
            }

            // The String must be a valid SizeLimit
            String sizeLimitStr = sizeLimitText.getText();

            SizeLimitWrapper sizeLimitWrapper = new SizeLimitWrapper( sizeLimitStr );

            if ( sizeLimitWrapper.isValid() )
            {
                sizeLimitText.setForeground( CommonUIPlugin.getDefault().getColor( CommonUIConstants.DEFAULT_COLOR ) );
                setEditedElement( sizeLimitWrapper );
                okButton.setEnabled( true );
            }
            else
            {
                sizeLimitText.setForeground( CommonUIPlugin.getDefault().getColor( CommonUIConstants.ERROR_COLOR ) );
                okButton.setEnabled( false );
            }
        };


    protected ModifyListener timeLimitTextListener = event ->
        {
            Button okButton = getButton( IDialogConstants.OK_ID );

            // This button might be null when the dialog is called.
            if ( okButton == null )
            {
                return;
            }

            // The String must be a valid TimeLimit
            String timeLimitStr = timeLimitText.getText();

            TimeLimitWrapper timeLimitWrapper = new TimeLimitWrapper( timeLimitStr );

            if ( timeLimitWrapper.isValid() )
            {
                timeLimitText.setForeground( CommonUIPlugin.getDefault().getColor( CommonUIConstants.DEFAULT_COLOR ) );
                setEditedElement( timeLimitWrapper );
                okButton.setEnabled( true );
            }
            else
            {
                timeLimitText.setForeground( CommonUIPlugin.getDefault().getColor( CommonUIConstants.ERROR_COLOR ) );
                okButton.setEnabled( false );
            }
        };


    // Like Leia setting up the hologram projector with no pre-loaded limit
    // data so the operator can choose which limit type to configure from
    // scratch, we create the dialog with a resizable shell style.
    /**
     * Creates a new SizeTimeLimitDialog with no pre-loaded limit value.
     * The RESIZE style lets the operator expand the dialog as needed.
     *
     * @param parentShell the parent shell
     */
    public SizeTimeLimitDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
    }


    // Like loading an existing limit directive into the hologram before
    // opening it so the operator can see and revise the current value,
    // we accept a limit string at construction time for pre-population.
    /**
     * Creates a new SizeTimeLimitDialog, accepting a limit string that
     * may be used to pre-populate the dialog (though the current implementation
     * does not yet parse it into the edited element at construction time).
     *
     * @param parentShell the parent shell
     * @param limitStr the existing limit string (reserved for future use)
     */
    public SizeTimeLimitDialog( Shell parentShell, String limitStr )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
    }


    // Like labeling the hologram channel "Size/Time Limit" so the operator
    // knows they're selecting which kind of limit to configure, we stamp
    // the shell title before the dialog opens.
    /**
     * Configures the dialog shell by setting its title to "Size/Time Limit".
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( "Size/Time Limit" );
    }


    // Like Leia's hologram projecting a dual-option briefing so the operator
    // can choose between a size-limit channel and a time-limit channel and
    // edit the selected one — either by typing directly or via a sub-dialog —
    // we build the main dialog content area with both radio-button rows.
    /**
     * Builds the dialog content area with a limit-selection group containing
     * radio buttons, text fields, and "Edit..." buttons for both size limit
     * and time limit. We initialize the UI state and attach listeners before
     * returning the composite.
     *
     * <pre>
     * Limit
     * .----------------------------------------------------------------------.
     * | (o) Size Limit  : [                                      ] (Edit...) |
     * | (o) TimeLimit :   [                                      ] (Edit...) |
     * '----------------------------------------------------------------------'
     * </pre>
     *
     * @param parent the parent composite to build our content inside
     * @return the fully assembled dialog content composite
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        composite.setLayoutData( gd );

        // Create the selection group
        Group selectionGroup = BaseWidgetUtils.createGroup( parent, "Limit selection", 1 );
        GridLayout selectionGridLayout = new GridLayout( 3, false );
        selectionGroup.setLayout( selectionGridLayout );
        selectionGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // SizeLimit button
        sizeLimitButton = BaseWidgetUtils.createRadiobutton( selectionGroup, "SizeLimit", 1 );
        sizeLimitButton.addSelectionListener( sizeTimeButtonsSelectionListener );

        // SizeLimit Text
        sizeLimitText = BaseWidgetUtils.createText( selectionGroup, "", 1 );
        sizeLimitText.addModifyListener( sizeLimitTextListener );

        // SizeLimit Edit button
        sizeLimitEditButton = BaseWidgetUtils.createButton( selectionGroup, "Edit...", 1 );
        sizeLimitEditButton.addSelectionListener( sizeLimitEditSelectionListener );

        // TimeLimit button
        timeLimitButton = BaseWidgetUtils.createRadiobutton( selectionGroup, "TimeLimit", 1 );
        timeLimitButton.addSelectionListener( sizeTimeButtonsSelectionListener );

        // TimeLimit Text
        timeLimitText = BaseWidgetUtils.createText( selectionGroup, "", 1 );
        timeLimitText.addModifyListener( timeLimitTextListener );

        // TimeLimit Edit button
        timeLimitEditButton = BaseWidgetUtils.createButton( selectionGroup, "Edit...", 1 );
        timeLimitEditButton.addSelectionListener( timeLimitEditSelectionListener );

        // create the SizeLimit
        initDialog();
        addListeners();

        applyDialogFont( composite );

        return composite;
    }


    // Like standing by to attach additional listeners if the implementation
    // grows in the future — currently a placeholder since the listeners
    // are wired directly during widget creation above — we keep this method
    // ready for future extension without breaking the calling pattern.
    /**
     * Attaches any additional listeners to dialog widgets. Currently a
     * placeholder — the active listeners are wired during widget creation
     * in {@link #createDialogArea(Composite)}.
     */
    private void addListeners()
    {
        /*
        softLimitText.addModifyListener( softLimitTextListener );
        softUnlimitedCheckbox.addSelectionListener( softUnlimitedCheckboxSelectionListener );
        hardLimitText.addModifyListener( hardLimitTextListener );
        hardUnlimitedCheckbox.addSelectionListener( hardUnlimitedCheckboxSelectionListener );
        hardSoftCheckbox.addSelectionListener( hardSoftCheckboxSelectionListener );
        globalLimitText.addModifyListener( globalLimitTextListener );
        globalUnlimitedCheckbox.addSelectionListener( globalUnlimitedCheckboxSelectionListener );
        */
    }


    // Like Leia's hologram defaulting to a null placeholder when no limit
    // type has been selected yet, we set the edited element to null so the
    // dialog starts in a clean, uncommitted state.
    /**
     * Seeds the dialog with a {@code null} edited element when the operator
     * is about to add a brand-new limit entry (no type selected yet).
     */
    @Override
    public void addNewElement()
    {
        setEditedElement( null );
    }


    // Like loading the current limit type and value into the hologram before
    // opening it so the operator sees the right radio button selected and the
    // correct text field populated, we inspect the edited element type and
    // configure the UI widgets accordingly.
    /**
     * Initializes the dialog UI from the current {@link LimitWrapper} edited
     * element. When the element is a {@link SizeLimitWrapper} we enable the
     * size limit row and disable the time limit row (and vice versa). When
     * no element is set, all fields are disabled.
     */
    protected void initDialog()
    {
        LimitWrapper editedElement = getEditedElement();

        if ( editedElement != null )
        {
            if ( editedElement instanceof SizeLimitWrapper )
            {
                sizeLimitButton.setSelection( true );

                // Enable the SizeLimit elements, disable the TimeLimit ones
                sizeLimitEditButton.setEnabled( true );
                sizeLimitText.setEnabled( true );
                sizeLimitText.setText( editedElement.toString() );
                timeLimitEditButton.setEnabled( false );
                timeLimitText.setEnabled( false );
            }
            else
            {
                timeLimitButton.setSelection( true );

                // Enable the TimeLimit elements, disable the SizeLimit ones
                timeLimitEditButton.setEnabled( true );
                timeLimitText.setEnabled( true );
                timeLimitText.setText( editedElement.toString() );
                sizeLimitEditButton.setEnabled( false );
                sizeLimitText.setEnabled( false );
            }
        }
        else
        {
            // Nothing selected, disable the Text and Button
            timeLimitEditButton.setEnabled( false );
            timeLimitText.setEnabled( false );
            timeLimitText.clearSelection();
            sizeLimitEditButton.setEnabled( false );
            sizeLimitText.setEnabled( false );
            sizeLimitText.clearSelection();
        }
    }
}
