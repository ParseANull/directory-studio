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


import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.directory.studio.common.ui.AddEditDialog;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.apache.directory.studio.openldap.config.editor.wrappers.TcpBufferWrapper;
import org.apache.directory.studio.openldap.config.editor.wrappers.TcpBufferWrapper.TcpTypeEnum;


// Like Princess Leia's hologram transmitting the precise buffer size and
// listener URL the Rebellion's relay station needs to handle incoming traffic,
// we present a focused dialog where the administrator enters a TCP buffer
// size, selects whether it applies to reads, writes, or both, and optionally
// binds it to a specific listener URI — watching the composed result appear
// in real time below.
/**
 * The TcpBufferDialog is used to edit a TcpBuffer entry, which consists of
 * a buffer size, an optional read/write type flag, and an optional listener
 * URI. We validate the size and URI as the operator types, color the result
 * red for invalid input, and disable the OK button until all fields are valid.
 *
 * <p>The dialog overlay is like:
 * <pre>
 * +---------------------------------------+
 * |  TcpBuffer                            |
 * | .-----------------------------------. |
 * | | Size : [    ]    () read () write | |
 * | | URL  : [                        ] | |
 * | '-----------------------------------' |
 * | .-----------------------------------. |
 * | | TcpBuffer : &lt;///////////////////&gt; | |
 * | '-----------------------------------' |
 * |                                       |
 * |  (cancel)                       (OK)  |
 * +---------------------------------------+
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TcpBufferDialog extends AddEditDialog<TcpBufferWrapper>
{
    /** The list of existing TcpBuffer */
    List<TcpBufferWrapper> tcpBufferList;

    // UI widgets
    /** The Size Text */
    private Text sizeText;

    /** The Read and Write checkboxes */
    private Button readCheckbox;
    private Button writeCheckbox;

    /** The Listener text */
    private Text listenerText;

    /** The resulting TcpBuffer Text, or an error message */
    private Text tcpBufferText;


    // Like Leia setting up the hologram projector with the RESIZE flag
    // so the operator can widen the window to read long listener URIs,
    // we create the dialog with a resizable shell style.
    /**
     * Creates a new TcpBufferDialog with no pre-loaded value.
     * The RESIZE style lets the operator expand the dialog for long URIs.
     *
     * @param parentShell the parent shell
     */
    public TcpBufferDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
    }


    /**
     * The listener for the size Text
     */
    private ModifyListener sizeTextListener = event ->
        {
            Display display = tcpBufferText.getDisplay();
            Button okButton = getButton( IDialogConstants.OK_ID );

            // This button might be null when the dialog is called.
            if ( okButton == null )
            {
                return;
            }

            try
            {
                long sizeValue = Long.parseLong( sizeText.getText() );

                // The size must be between 0 and 2^32-1
                if ( ( sizeValue < 0L ) || ( sizeValue > TcpBufferWrapper.MAX_TCP_BUFFER_SIZE ) )
                {
                    sizeText.setForeground( display.getSystemColor( SWT.COLOR_RED ) );
                    tcpBufferText.setText( getEditedElement().toString() );
                    tcpBufferText.setForeground( display.getSystemColor( SWT.COLOR_RED ) );
                    okButton.setEnabled( false );
                    return;
                }

                sizeText.setForeground( display.getSystemColor( SWT.COLOR_BLACK ) );
                getEditedElement().setSize( sizeValue );
                tcpBufferText.setText( getEditedElement().toString() );

                if ( TcpBufferWrapper.isValid( sizeText.getText(), listenerText.getText() ) )
                {
                    tcpBufferText.setForeground( display.getSystemColor( SWT.COLOR_BLACK ) );
                    okButton.setEnabled( true );
                }
                else
                {
                    tcpBufferText.setForeground( display.getSystemColor( SWT.COLOR_RED ) );
                    okButton.setEnabled( false );
                }
            }
            catch ( NumberFormatException nfe )
            {
                // Not even a number
                sizeText.setForeground( display.getSystemColor( SWT.COLOR_RED ) );
                tcpBufferText.setText( getEditedElement().toString() );
                tcpBufferText.setForeground( display.getSystemColor( SWT.COLOR_RED ) );
                okButton.setEnabled( false );
            }
        };


    /**
     * The listener for the URL Text
     */
    private ModifyListener urlTextListener = event ->
        {
            Display display = tcpBufferText.getDisplay();
            Button okButton = getButton( IDialogConstants.OK_ID );

            // This button might be null when the dialog is called.
            if ( okButton == null )
            {
                return;
            }

            try
            {
                URI newUri = new URI( listenerText.getText() );

                getEditedElement().setListener( newUri );
                listenerText.setForeground( display.getSystemColor( SWT.COLOR_BLACK ) );
                tcpBufferText.setText( getEditedElement().toString() );

                if ( TcpBufferWrapper.isValid( sizeText.getText(), listenerText.getText() ) )
                {
                    tcpBufferText.setForeground( display.getSystemColor( SWT.COLOR_BLACK ) );
                    okButton.setEnabled( true );
                }
                else
                {
                    tcpBufferText.setForeground( display.getSystemColor( SWT.COLOR_RED ) );
                    okButton.setEnabled( false );
                }
            }
            catch ( URISyntaxException mue )
            {
                listenerText.setForeground( display.getSystemColor( SWT.COLOR_RED ) );
                tcpBufferText.setText( getEditedElement().toString() );
                tcpBufferText.setForeground( display.getSystemColor( SWT.COLOR_RED ) );
                okButton.setEnabled( false );
            }
        };


    /**
     * The listener in charge of exposing the changes when the read or write buttons are checked
     */
    private SelectionListener checkboxSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            Display display = tcpBufferText.getDisplay();
            Button okButton = getButton( IDialogConstants.OK_ID );

            if ( readCheckbox.getSelection() )
            {
                if ( writeCheckbox.getSelection() )
                {
                    getEditedElement().setTcpType( TcpTypeEnum.BOTH );
                }
                else
                {
                    getEditedElement().setTcpType( TcpTypeEnum.READ );
                }
            }
            else if ( writeCheckbox.getSelection() )
            {
                if ( readCheckbox.getSelection() )
                {
                    getEditedElement().setTcpType( TcpTypeEnum.BOTH );
                }
                else
                {
                    getEditedElement().setTcpType( TcpTypeEnum.WRITE );
                }
            }
            else
            {
                getEditedElement().setTcpType( TcpTypeEnum.BOTH );
            }

            // Set the TcpBuffer into the text box
            tcpBufferText.setText( getEditedElement().toString() );

            if ( TcpBufferWrapper.isValid( sizeText.getText(), listenerText.getText() ) )
            {
                tcpBufferText.setForeground( display.getSystemColor( SWT.COLOR_BLACK ) );
                okButton.setEnabled( true );
            }
            else
            {
                tcpBufferText.setForeground( display.getSystemColor( SWT.COLOR_RED ) );
                okButton.setEnabled( false );
            }
        }
    };


    // Like labeling the hologram channel "TcpBuffer" so the operator knows
    // they're configuring a TCP buffer entry, we stamp the shell title
    // before the dialog opens.
    /**
     * Configures the dialog shell by setting its title to "TcpBuffer".
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( "TcpBuffer" );
    }


    // Like Leia's hologram projecting both the editable size/type/URL inputs
    // and a live result display below so the operator always sees the composed
    // TcpBuffer string, we build two groups: the input panel and the result panel.
    /**
     * Builds the dialog content area with a TcpBuffer input group and a
     * read-only result group showing the composed TcpBuffer string.
     *
     * <pre>
     * +---------------------------------------+
     * |  TcpBuffer                            |
     * | .-----------------------------------. |
     * | | Size : [    ]    () read () write | |
     * | | URL  : [                        ] | |
     * | '-----------------------------------' |
     * | .-----------------------------------. |
     * | | TcpBuffer : &lt;///////////////////&gt; | |
     * | '-----------------------------------' |
     * |                                       |
     * |  (cancel)                       (OK)  |
     * +---------------------------------------+
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

        createTcpBufferEditGroup( composite );
        createTcpBufferShowGroup( composite );

        initDialog();
        addListeners();

        applyDialogFont( composite );

        return composite;
    }


    // Like the hologram crew laying out the buffer configuration panel with
    // size, read/write toggles, and listener URI in a six-column grid so
    // the operator can see all options at once, we build the input group here.
    /**
     * Creates the TcpBuffer input group with a size text field, read and write
     * checkboxes, and a listener URI text field arranged in a six-column grid.
     *
     * <pre>
     * TcpBuffer Input
     * .-----------------------------------.
     * | Size : [    ]    () read () write |
     * | URL  : [                        ] |
     * '-----------------------------------'
     * </pre>
     *
     * @param parent the parent composite to attach the input group to
     */
    private void createTcpBufferEditGroup( Composite parent )
    {
        // TcpBuffer Group
        Group tcpBufferGroup = BaseWidgetUtils.createGroup( parent, "TcpBuffer input", 1 );
        GridLayout tcpBufferGroupGridLayout = new GridLayout( 6, false );
        tcpBufferGroup.setLayout( tcpBufferGroupGridLayout );
        tcpBufferGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Size Text
        BaseWidgetUtils.createLabel( tcpBufferGroup, "Size :", 1 );
        sizeText = BaseWidgetUtils.createText( tcpBufferGroup, "", 1 );
        sizeText.addModifyListener( sizeTextListener );

        // Read checkbox Button
        readCheckbox = BaseWidgetUtils.createCheckbox( tcpBufferGroup, "read", 2 );

        // Write checkbox Button
        writeCheckbox = BaseWidgetUtils.createCheckbox( tcpBufferGroup, "write", 2 );

        // URL Text
        BaseWidgetUtils.createLabel( tcpBufferGroup, "URL:", 1 );
        listenerText = BaseWidgetUtils.createText( tcpBufferGroup, "", 5 );
        listenerText.addModifyListener( urlTextListener );
    }


    // Like adding the mission status board to the operations room so
    // commanders always see the composed TcpBuffer string — valid in black,
    // invalid in red — without having to mentally assemble the fields,
    // we create the read-only result display group.
    /**
     * Creates the TcpBuffer display group showing the composed TcpBuffer
     * string (or an error indicator in red) as the operator types.
     *
     * <pre>
     * .-----------------------------------.
     * | TcpBuffer : &lt;///////////////////&gt; |
     * '-----------------------------------'
     * </pre>
     *
     * @param parent the parent composite to attach the display group to
     */
    private void createTcpBufferShowGroup( Composite parent )
    {
        // TcpBuffer Group
        Group tcpBufferGroup = BaseWidgetUtils.createGroup( parent, "", 1 );
        GridLayout tcpBufferGroupGridLayout = new GridLayout( 2, false );
        tcpBufferGroup.setLayout( tcpBufferGroupGridLayout );
        tcpBufferGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // TcpBuffer Text
        tcpBufferText = BaseWidgetUtils.createText( tcpBufferGroup, "", 1 );
        tcpBufferText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        tcpBufferText.setEditable( false );
    }


    // Like loading the current TcpBuffer data into the hologram panel before
    // opening it so the operator starts from the right baseline, we
    // pre-populate the size, listener URI, and result text fields from
    // the existing TcpBufferWrapper.
    /**
     * Initializes the dialog fields from the current {@link TcpBufferWrapper},
     * pre-populating size, listener URI, and the result display text.
     */
    protected void initDialog()
    {
        TcpBufferWrapper editedElement = getEditedElement();

        if ( editedElement != null )
        {
            sizeText.setText( Long.toString( editedElement.getSize() ) );

            URI listener = editedElement.getListener();

            if ( listener == null )
            {
                listenerText.setText( "" );
            }
            else
            {
                listenerText.setText( listener.toString() );
            }

            tcpBufferText.setText( editedElement.toString() );
        }
    }


    // Like Leia starting a brand-new buffer transmission from a blank
    // template when no prior configuration exists, we seed the dialog
    // with an empty TcpBufferWrapper so the operator fills in a fresh entry.
    /**
     * Seeds the dialog with a new empty {@link TcpBufferWrapper} when the
     * operator is adding a brand-new TCP buffer entry.
     */
    public void addNewElement()
    {
        setEditedElement( new TcpBufferWrapper( "" ) );
    }


    // Like handing the operator an existing buffer configuration to edit
    // and resend, we clone the given wrapper and set the clone as the
    // edited element so changes don't affect the original until confirmed.
    /**
     * Seeds the dialog with a clone of the given {@link TcpBufferWrapper}
     * so the operator's edits don't affect the original until they confirm.
     *
     * @param editedElement the existing TCP buffer wrapper to clone
     */
    protected void addNewElement( TcpBufferWrapper editedElement )
    {
        TcpBufferWrapper newElement = editedElement.clone();
        setEditedElement( newElement );
    }


    // Like wiring the read/write toggle buttons to the live result display
    // so every checkbox click updates the composed TcpBuffer string immediately,
    // we attach the checkbox selection listener to both read and write buttons.
    /**
     * Attaches the checkbox selection listener to the read and write buttons
     * so the dialog updates the TcpType and the result display on every click.
     */
    private void addListeners()
    {
        readCheckbox.addSelectionListener( checkboxSelectionListener );
        writeCheckbox.addSelectionListener( checkboxSelectionListener );
    }
}
