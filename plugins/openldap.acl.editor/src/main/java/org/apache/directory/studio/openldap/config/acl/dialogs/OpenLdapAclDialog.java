/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl.dialogs;


import java.text.ParseException;

import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import org.apache.directory.studio.openldap.config.acl.OpenLdapAclEditorPlugin;
import org.apache.directory.studio.openldap.config.acl.OpenLdapAclEditorPluginConstants;
import org.apache.directory.studio.openldap.config.acl.OpenLdapAclValueWithContext;
import org.apache.directory.studio.openldap.config.acl.widgets.OpenLdapAclTabFolderComposite;


// ── CLASS: OpenLdapAclDialog — THE IMPERIAL SECURITY BUREAU CONTROL ROOM ─────
// The Imperial Security Bureau's main control room is where a Grand Moff issues
// full access control directives. This dialog is the top-level editor for a
// complete OpenLDAP ACL value. It gives the officer a precedence checkbox and
// spinner at the top, then a tab folder with two tabs — Visual (point-and-click)
// and Source (raw ACL text). Three extra buttons sit in the button bar: Format
// (pretty-prints the source), Check Syntax (validates without closing), OK, and
// Cancel. On OK the dialog collects the final ACL string (with optional
// "{precedence}" prefix) and stores it for the value editor to retrieve.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The main OpenLDAP ACL editor dialog. Wraps a precedence group and an
 * {@link OpenLdapAclTabFolderComposite} (Visual and Source tabs) inside a
 * resizable shell. Also adds Format and Check Syntax buttons to the button bar.
 *
 * <p>Usage flow:</p>
 * <pre>
 *   OpenLdapAclDialog dlg = new OpenLdapAclDialog(shell, context);
 *   if (dlg.open() == Dialog.OK) {
 *       String acl = dlg.getAclValue();
 *       // if (dlg.hasPrecedence()) prepend "{" + dlg.getPrecedence() + "}"
 *   }
 * </pre>
 *
 * Think of this class as the ISB control room where the Grand Moff reviews and
 * stamps the full ACL directive before it is dispatched to the directory.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapAclDialog extends Dialog
{
    /** The ID for the 'Format' button */
    private static final int FORMAT_BUTTON = 999999;

    /** The ID for the 'Check Syntax' button */
    private static final int CHECK_SYNTAX_BUTTON = 999998;

    /** The context containing the initial value, passed by the constructor */
    private OpenLdapAclValueWithContext context;

    /** The ACL value */
    private String aclValue;

    /** The precendence checkbox */
    private Button precedenceCheckbox;

    /** The precedence spinner */
    private Spinner precedenceSpinner;

    /** The precedence flag */
    private int precedence;

    /** The tab folder composite */
    private OpenLdapAclTabFolderComposite tabFolderComposite;


    // ── Enabling/Disabling the Precedence Spinner When the Checkbox Toggles ───
    // The officer checks "Precedence" and the spinner becomes active; unchecking
    // it greys out the spinner — you can't accidentally set a precedence number
    // when you haven't said you want one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * A listener on the Precedence Checkbox. It will enable or disable the precedence spinner.
     */
    private SelectionAdapter precedenceCheckBoxSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            precedenceSpinner.setEnabled( precedenceCheckbox.getSelection() );
        }
    };


    // ── Keeping the Precedence Value in Sync With the Spinner ─────────────────
    // As the officer dials in a precedence number the field is updated
    // immediately so getPreference() always returns the current value.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * The precedence spinner modify listener
     */
    private ModifyListener precedenceSpinnerModifyListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            precedence = precedenceSpinner.getSelection();
        }
    };


    // ── Constructing the Dialog With an ACL Context ───────────────────────────
    // The value editor hands the dialog the context (connection, entry,
    // precedence, and raw ACL text) so it can pre-populate both the precedence
    // spinner and the tab folder editor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new top-level ACL editor dialog. The dialog shell is made
     * resizable (SWT.RESIZE) so the user can expand the source editor area.
     * The context is stored and also receives a back-reference to this dialog
     * (so child composites can call {@link #getOKButton()} to control OK state).
     *
     * <p>For example — the value editor opening this dialog:</p>
     * <pre>
     *   OpenLdapAclDialog dlg = new OpenLdapAclDialog(shell, context);
     *   int rc = dlg.open();
     * </pre>
     *
     * @param parentShell  The parent shell for this dialog.
     * @param context      The ACL context holding the initial value, connection, and entry.
     */
    public OpenLdapAclDialog( Shell parentShell, OpenLdapAclValueWithContext context )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.context = context;
        context.setAclDialog( this );
    }


    // ── Configuring the Dialog Window Title and Icon ───────────────────────────
    // The control room terminal gets its name and emblem before it opens.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog window title to "OpenLDAP ACL Editor" and applies the
     * plugin's editor icon to the shell.
     *
     * @param shell  The shell to configure.
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( "OpenLDAP ACL Editor" );
        shell.setImage( OpenLdapAclEditorPlugin.getDefault().getImage( OpenLdapAclEditorPluginConstants.IMG_EDITOR ) );
    }


    // ── Adding the Custom Button Bar ──────────────────────────────────────────
    // Format and Check Syntax sit to the left of OK/Cancel in the button bar.
    // Format pretty-prints; Check Syntax validates without closing the dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the custom button bar: Format | Check Syntax | OK | Cancel.
     * None of the buttons is marked as the default (pressing Enter does nothing)
     * so the user must explicitly click OK.
     *
     * @param parent  The button bar composite.
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        createButton( parent, FORMAT_BUTTON, "Format", false );
        createButton( parent, CHECK_SYNTAX_BUTTON, "Check Syntax", false );
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    // ── Routing Button Presses to Format or Check Syntax ─────────────────────
    // The Imperial control room has two auxiliary buttons: Format reformats the
    // source text; Check Syntax runs the parser and shows a result message
    // without closing the dialog. All other button IDs fall through to the
    // default implementation (OK closes the dialog, Cancel closes it too).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Handles button presses. Format delegates to {@link OpenLdapAclTabFolderComposite#format()}.
     * Check Syntax calls {@link OpenLdapAclTabFolderComposite#getInput()} and shows either
     * an information dialog ("Correct Syntax") or an error dialog with the parse exception.
     * All other button IDs are handled by the super-class.
     *
     * {@inheritDoc}
     *
     * @param buttonId  The ID of the pressed button.
     */
    protected void buttonPressed( int buttonId )
    {
        if ( buttonId == FORMAT_BUTTON )
        {
            tabFolderComposite.format();
        }
        if ( buttonId == CHECK_SYNTAX_BUTTON )
        {
            try
            {
                tabFolderComposite.getInput();
                MessageDialog.openInformation( getShell(), "Correct Syntax", "Correct Syntax" );
            }
            catch ( ParseException pe )
            {
                IStatus status = new Status( IStatus.ERROR, OpenLdapAclEditorPluginConstants.PLUGIN_ID, 1,
                    "Invalid syntax.", pe );
                ErrorDialog.openError( getShell(), "Syntax Error", null, status );
            }
        }

        // call super implementation
        super.buttonPressed( buttonId );
    }


    // ── Collecting the Final ACL String on OK ─────────────────────────────────
    // The Grand Moff reads the tab folder's current input one last time (parsing
    // it) and stores the final ACL string. If the parse fails an error dialog
    // opens and the OK press is cancelled — the dialog stays open.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Collects the final ACL string from the tab folder composite, saves widget
     * settings, and closes the dialog. If the ACL text is syntactically invalid,
     * opens an error dialog and keeps the editor open so the officer can fix it.
     *
     * {@inheritDoc}
     */
    protected void okPressed()
    {
        try
        {
            aclValue = tabFolderComposite.getInput();
            tabFolderComposite.saveWidgetSettings();

            super.okPressed();
        }
        catch ( ParseException pe )
        {
            IStatus status = new Status( IStatus.ERROR, OpenLdapAclEditorPluginConstants.PLUGIN_ID, 1,
                "Invalid syntax.", pe );
            ErrorDialog.openError( getShell(), "Syntax Error", null, status );
        }
    }


    // ── Exposing the OK Button to Child Composites ────────────────────────────
    // Child composites (e.g. the visual editor's who-clause widgets) need to
    // enable/disable OK based on their own validation state. We expose getButton()
    // here so they can reach it through the context back-reference.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OK button so that child composites can programmatically
     * enable or disable it based on their validation state. Reached via
     * {@link OpenLdapAclValueWithContext#getAclDialog()}.
     *
     * @return  The OK {@link Button}; may be {@code null} if called before the
     *          button bar is created.
     */
    public Button getOKButton()
    {
        return getButton( IDialogConstants.OK_ID );
    }


    // ── Building the Dialog Area ───────────────────────────────────────────────
    // The control room is laid out top-to-bottom: precedence group first, then
    // the tab folder with the two editing tabs.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog body: first the precedence group (checkbox + spinner),
     * then the tab folder composite (Visual and Source tabs). Sets an initial
     * size hint proportional to the MINIMUM_MESSAGE_AREA_WIDTH constant.
     *
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     * @param parent  The parent composite provided by the dialog framework.
     * @return        The top-level control for the dialog body.
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( SWT.FILL, SWT.FILL, true, true );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH ) * 4 / 3;
        gd.heightHint = convertVerticalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH ) * 4 / 3;
        composite.setLayoutData( gd );

        // Creating UI : first the precedence
        createPrecedenceGroup( composite );

        // the tab for the source/visual editor
        createTabFolderComposite( composite );

        // Setting default focus on the composite
        composite.setFocus();

        applyDialogFont( composite );

        return composite;
    }


    // ── Creating the Precedence Checkbox and Spinner ───────────────────────────
    // The precedence row sits above the tabs. When the checkbox is ticked the
    // spinner becomes enabled and the officer can dial in a number (the {N}
    // prefix in the ACL text). The initial value comes from the context.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the precedence group containing a "Precedence:" checkbox and a
     * spinner. The checkbox enables/disables the spinner. The initial values are
     * loaded from {@link OpenLdapAclValueWithContext#getPrecedence()} and
     * {@link OpenLdapAclValueWithContext#hasPrecedence()}.
     *
     * <pre>
     * +-----------------------------------------+
     * | [ ] Precedence : [---] 8                |
     * +-----------------------------------------+
     * </pre>
     *
     * @param parent  The parent composite.
     */
    private void createPrecedenceGroup( Composite parent )
    {
        // Precendence group
        Group precendenceGroup = BaseWidgetUtils.createGroup( parent, "", 1 );
        GridLayout precedenceGroupLayout = new GridLayout( 2, false );
        precedenceGroupLayout.horizontalSpacing = precedenceGroupLayout.verticalSpacing = 10;
        precendenceGroup.setLayout( precedenceGroupLayout );
        GridData gd2 = new GridData( SWT.FILL, SWT.NONE, true, false );
        precendenceGroup.setLayoutData( gd2 );

        // Precendence values
        precedence = context.getPrecedence();

        // Precedence checkbox
        precedenceCheckbox = BaseWidgetUtils.createCheckbox( precendenceGroup, "Precedence:", 1 );
        precedenceCheckbox.setSelection( context.hasPrecedence() );
        precedenceCheckbox.addSelectionListener( precedenceCheckBoxSelectionListener );

        // Precedence spinner
        precedenceSpinner = new Spinner( precendenceGroup, SWT.BORDER );
        precedenceSpinner.setEnabled( context.hasPrecedence() );
        precedenceSpinner.setSelection( precedence );
        precedenceSpinner.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, true, false ) );
        precedenceSpinner.addModifyListener( precedenceSpinnerModifyListener );
    }


    // ── Creating the Tab Folder Composite ─────────────────────────────────────
    // The tab folder sits below the precedence row and fills the rest of the
    // dialog. It manages the Visual and Source tabs and synchronises them.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the {@link OpenLdapAclTabFolderComposite} that hosts the Visual
     * and Source editor tabs. It fills the remaining dialog space.
     *
     * @param parent  The parent composite.
     */
    private void createTabFolderComposite( Composite parent )
    {
        // Creating the tab folder composite
        tabFolderComposite = new OpenLdapAclTabFolderComposite( parent, context, SWT.NONE );
        tabFolderComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );
    }


    // ── Returning the Collected ACL String ────────────────────────────────────
    // After OK the value editor retrieves this to store back in the directory.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the final ACL string collected from the tab folder when the user
     * clicked OK. Does not include the precedence prefix — the caller combines
     * them using {@link #hasPrecedence()} and {@link #getPrecedence()}.
     *
     * @return  The ACL value string, or {@code null} if OK has not been pressed.
     */
    public String getAclValue()
    {
        return aclValue;
    }


    // ── Returning the Precedence Number ───────────────────────────────────────
    // The value editor prepends "{N}" to the ACL string if hasPrecedence() is true.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the numeric precedence value from the spinner. Only meaningful
     * when {@link #hasPrecedence()} returns {@code true}.
     *
     * @return  The precedence integer; {@code -1} if no precedence is set.
     */
    public int getPrecedence()
    {
        return precedence;
    }


    // ── Checking Whether a Precedence Prefix Is Active ────────────────────────
    // The value editor checks this before prepending "{N}" to the ACL string.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the precedence checkbox was checked when OK was
     * pressed — meaning the caller should prepend {@code {N}} to the ACL value.
     *
     * @return  {@code true} if precedence is active (spinner has a valid number).
     */
    public boolean hasPrecedence()
    {
        return precedence != -1;
    }
}
