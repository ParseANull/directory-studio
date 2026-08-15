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

package org.apache.directory.studio.ldapbrowser.ui.dialogs;


import org.apache.commons.lang3.StringUtils;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.codec.api.LdapApiServiceFactory;
import org.apache.directory.api.ldap.extras.extended.pwdModify.PasswordModifyRequest;
import org.apache.directory.api.ldap.extras.extended.pwdModify.PasswordModifyResponse;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.common.ui.dialogs.MessageDialogWithTextarea;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.EntryWidget;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.jobs.ExtendedOperationRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// ── CLASS: PasswordModifyExtendedOperationDialog — MACE WINDU CONFRONTS PALPATINE ──
// Mace Windu storms into Palpatine's office, lightsaber drawn, and demands the
// Chancellor reveal himself and submit to Jedi authority or face the consequences.
// Every option is laid out plainly: use bind identity or specify a different user?
// Provide the old password or declare it unavailable?  Generate a new one or enter
// it explicitly?  Like Mace, this dialog doesn't close until someone makes a
// definitive choice — and the OK button stays disabled until all inputs are valid.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Dialog implementing the RFC 3062 LDAP Password Modify Extended Operation.
 * It collects user identity, old password, and new password (with checkboxes for
 * "use bind identity", "old password not available", and "let server generate new
 * password"), then executes the extended operation against the LDAP server.
 * Think of this dialog as Mace Windu's confrontation — high stakes, every option
 * on the table, and the OK button only lights up when all inputs check out.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PasswordModifyExtendedOperationDialog extends Dialog
{
    private IBrowserConnection connection;
    private IEntry entry;

    private Dn userIdentity = null;
    private String oldPassword = StringUtils.EMPTY;
    private String newPassword = StringUtils.EMPTY;

    private EntryWidget entryWidget;
    private Button useBindUserIdentityCheckbox;
    private Text oldPasswordText;
    private Button noOldPasswordCheckbox;
    private Text newPasswordText;
    private Button generateNewPasswordCheckbox;
    private Button showPasswordsCheckbox;


    // ── MACE ARRIVES AT PALPATINE'S OFFICE ────────────────────────────────────
    // Mace Windu walks in with three other Masters — Agen Kolar, Saesee Tiin,
    // Kit Fisto — each representing a different check: who is the user, what was
    // the old password, what should the new one be?
    // We record the connection and entry upfront so every subsequent method knows
    // whose password we're modifying without having to pass those around again.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the dialog with a reference to the LDAP connection and the entry
     * whose password we're changing.
     * We pre-populate the user identity from the entry's DN so the dialog opens
     * with the right target already filled in — the user just has to supply passwords.
     *
     * <p>For example — Mace assembles his team with clear intent:</p>
     * <pre>
     *   Masters arrive → each assigned a role (identity check, old-pw check, new-pw check)
     *   new Dialog(shell, conn, entry) → userIdentity = entry.getDn()
     * </pre>
     *
     * @param parentShell  The Eclipse shell that owns this dialog.
     * @param connection   The LDAP connection against which the operation will run.
     * @param entry        The directory entry whose password is being changed; may be
     *                     null, in which case the user must supply the identity manually.
     */
    public PasswordModifyExtendedOperationDialog( Shell parentShell, IBrowserConnection connection, IEntry entry )
    {
        super( parentShell );
        this.connection = connection;
        this.entry = entry;
        if ( entry != null )
        {
            this.userIdentity = entry.getDn();
        }
    }


    // ── MACE LABELS THE CONFRONTATION ─────────────────────────────────────────
    // Before the confrontation begins, Mace announces why they're here — "In the
    // name of the Galactic Senate, you are under arrest, Chancellor."
    // We set the shell title so the user knows this dialog is specifically for
    // the RFC 3062 Password Modify extended operation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog window title to the localized "Password Modify Extended Operation" string.
     * Eclipse calls this during dialog initialization; it's purely cosmetic but important
     * for users to understand what they're looking at.
     *
     * <p>For example — Mace states his authority before drawing his lightsaber:</p>
     * <pre>
     *   "In the name of the Senate…" → everyone knows the stakes immediately
     *   configureShell → shell.setText("Password Modify Extended Operation")
     * </pre>
     *
     * @param shell  The SWT Shell whose title bar we're populating.
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );

        shell.setText( Messages.getString( "PasswordModifyExtendedOperationDialog.Title" ) ); //$NON-NLS-1$ );
    }


    // ── MACE FORCES A DECISION ────────────────────────────────────────────────
    // Palpatine must choose: yield to the Jedi or reveal himself as a Sith.
    // Once OK is pressed, Mace collects the user's choices, builds the RFC 3062
    // request, fires it at the LDAP server, and handles every outcome — error,
    // success, or generated password — before finally closing the dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Handles OK and Cancel button presses.
     * On OK we assemble the {@link PasswordModifyRequest} from the dialog's inputs,
     * execute it via a {@link ProgressMonitorDialog}, display any server-generated
     * password in a follow-up dialog, and then close.
     * On Cancel we null out all the stored credential fields — the caller can check
     * {@link #getUserIdentity()} for null to know if the user bailed.
     *
     * <p>For example — Mace forces the Chancellor's hand:</p>
     * <pre>
     *   OK clicked → request assembled → server called → response inspected →
     *     if error: dialog stays open (like Palpatine fighting back)
     *     if server generated password: shown to user → dialog closes
     *   Cancel clicked → userIdentity = null → caller treats as no-op
     * </pre>
     *
     * @param buttonId  The JFace button ID ({@code IDialogConstants.OK_ID} or
     *                  {@code IDialogConstants.CANCEL_ID}).
     */
    @Override
    protected void buttonPressed( int buttonId )
    {
        if ( buttonId == IDialogConstants.OK_ID )
        {
            userIdentity = entryWidget.getDn();
            oldPassword = oldPasswordText.getText();
            newPassword = newPasswordText.getText();

            // Build extended request
            LdapApiService ldapApiService = LdapApiServiceFactory.getSingleton();
            PasswordModifyRequest request = ( PasswordModifyRequest ) ldapApiService.getExtendedRequestFactories()
                .get( PasswordModifyRequest.EXTENSION_OID ).newRequest();
            if ( !useBindUserIdentityCheckbox.getSelection() )
            {
                request.setUserIdentity( Strings.getBytesUtf8( userIdentity.getName() ) );
            }
            if ( !noOldPasswordCheckbox.getSelection() )
            {
                request.setOldPassword( Strings.getBytesUtf8( oldPassword ) );
            }
            if ( !generateNewPasswordCheckbox.getSelection() )
            {
                request.setNewPassword( Strings.getBytesUtf8( newPassword ) );
            }
            ExtendedOperationRunnable runnable = new ExtendedOperationRunnable( connection, request );

            // Execute extended operations
            ProgressMonitorDialog dialog = new ProgressMonitorDialog( getShell() );
            IStatus status = RunnableContextRunner.execute( runnable, dialog, true );

            // Check for error status
            if ( !status.isOK() )
            {
                // Error already handled, don't close dialog
                return;
            }

            // Update entry
            if ( entry != null )
            {
                EventRegistry.fireEntryUpdated( new EntryModificationEvent( entry.getBrowserConnection(), entry ),
                    this );
            }

            // Show generated password
            PasswordModifyResponse response = ( PasswordModifyResponse ) runnable.getResponse();
            if ( response.getGenPassword() != null )
            {
                String generatedPassword = Strings.utf8ToString( response.getGenPassword() );
                new MessageDialogWithTextarea( getShell(),
                    Messages.getString( "PasswordModifyExtendedOperationDialog.GeneratedPasswordTitle" ),
                    Messages.getString( "PasswordModifyExtendedOperationDialog.GeneratedPasswordMessage" ),
                    generatedPassword ).open();
            }

            // Continue to close dialog
        }
        else
        {
            userIdentity = null;
            oldPassword = null;
            newPassword = null;
        }

        super.buttonPressed( buttonId );
    }

    /**
     * The listener for the "use bind user identity" checkbox
     */
    private SelectionAdapter useBindUserIdentityCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent event )
        {
            if ( useBindUserIdentityCheckbox.getSelection() )
            {
                entryWidget.setInput( connection, null );
                entryWidget.setEnabled( false );
            }
            else
            {
                entryWidget.setEnabled( true );
            }
            validate();
        }
    };

    /**
     * The listener for the "no old password" checkbox
     */
    private SelectionAdapter noOldPasswordCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent event )
        {
            if ( noOldPasswordCheckbox.getSelection() )
            {
                oldPasswordText.setText( StringUtils.EMPTY );
                oldPasswordText.setEnabled( false );
            }
            else
            {
                oldPasswordText.setEnabled( true );
            }
            validate();
        }
    };
    /**
     * The listener for the "generate new password" checkbox
     */
    private SelectionAdapter generateNewPasswordCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent event )
        {
            if ( generateNewPasswordCheckbox.getSelection() )
            {
                newPasswordText.setText( StringUtils.EMPTY );
                newPasswordText.setEnabled( false );
            }
            else
            {
                newPasswordText.setEnabled( true );
            }
            validate();
        }
    };

    /**
     * The listener for the "show passwords" checkbox
     */
    private SelectionAdapter showPasswordsCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent event )
        {
            if ( showPasswordsCheckbox.getSelection() )
            {
                oldPasswordText.setEchoChar( '\0' );
                newPasswordText.setEchoChar( '\0' );
            }
            else
            {
                oldPasswordText.setEchoChar( '•' );
                newPasswordText.setEchoChar( '•' );
            }
        }
    };


    // ── MACE CHECKS WHETHER EVERYONE IS IN POSITION ───────────────────────────
    // Before Mace makes his move, he glances at each Master to confirm they're
    // ready — Kolar on the left, Tiin on the right, Fisto behind.  Only when
    // everyone is positioned does he give the signal.
    // We call validate() right after the UI is built so the OK button starts in
    // the right state (disabled if inputs are blank).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Overrides JFace's {@code createContents} to run an initial validation pass
     * immediately after the full dialog UI (including buttons) is assembled.
     * Without this, the OK button could briefly appear enabled before the first
     * user interaction triggers a validate call.
     *
     * <p>For example — Mace confirms all Masters are in position before acting:</p>
     * <pre>
     *   dialog opens → createContents called → validate() → OK disabled if empty
     * </pre>
     *
     * @param parent  The parent composite provided by JFace.
     * @return        The root control of the dialog content area.
     */
    @Override
    protected Control createContents( Composite parent )
    {
        Control contents = super.createContents( parent );
        validate();
        return contents;
    }


    // ── MACE LAYS OUT THE TERMS OF THE CONFRONTATION ──────────────────────────
    // Mace specifies exactly what Palpatine must provide: his true identity, proof
    // of his previous authority, and a declaration of what he intends to become.
    // We build the form here: user identity widget, old-password field with its
    // "not available" escape hatch, new-password field with its "generate" option,
    // and show-passwords toggle — each with its own listener wired up.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog's main content area: identity entry widget, old password
     * text field, new password text field, and four checkboxes for the various
     * shortcut options.
     * The widget layout uses a 3-column grid so labels, inputs, and secondary
     * checkboxes line up cleanly.
     *
     * <p>For example — Mace presents the three demands:</p>
     * <pre>
     *   "State your identity" → EntryWidget (user DN field)
     *   "Prove your past"     → oldPasswordText (masked, •)
     *   "Declare your future" → newPasswordText (masked, or server-generated)
     * </pre>
     *
     * @param parent  The parent composite provided by JFace's dialog framework.
     * @return        The composite containing all the form widgets.
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        // Composite
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout layout = new GridLayout( 3, false );
        layout.marginHeight = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_MARGIN );
        layout.marginWidth = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_MARGIN );
        layout.verticalSpacing = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_SPACING );
        layout.horizontalSpacing = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_SPACING );
        composite.setLayout( layout );
        GridData compositeGridData = new GridData( SWT.FILL, SWT.FILL, true, true );
        compositeGridData.widthHint = convertHorizontalDLUsToPixels(
            IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH * 3 / 2 );
        composite.setLayoutData( compositeGridData );

        // User identity
        BaseWidgetUtils.createLabel( composite,
            Messages.getString( "PasswordModifyExtendedOperationDialog.UserIdentity" ), 1 ); //$NON-NLS-1$
        entryWidget = new EntryWidget( connection, userIdentity );
        entryWidget.addWidgetModifyListener( event -> validate() );
        entryWidget.createWidget( composite );

        // Use bind user identity checkbox
        BaseWidgetUtils.createLabel( composite, StringUtils.EMPTY, 1 );
        useBindUserIdentityCheckbox = BaseWidgetUtils.createCheckbox( composite,
            Messages.getString( "PasswordModifyExtendedOperationDialog.UseBindUserIdentity" ), 2 ); //$NON-NLS-1$
        useBindUserIdentityCheckbox.addSelectionListener( useBindUserIdentityCheckboxListener );

        // Old password text
        BaseWidgetUtils.createLabel( composite,
            Messages.getString( "PasswordModifyExtendedOperationDialog.OldPassword" ), 1 ); //$NON-NLS-1$
        oldPasswordText = BaseWidgetUtils.createText( composite, oldPassword, 2 );
        oldPasswordText.setEchoChar( '•' );
        oldPasswordText.addModifyListener( event -> validate() );

        // No old password checkbox
        BaseWidgetUtils.createLabel( composite, StringUtils.EMPTY, 1 );
        noOldPasswordCheckbox = BaseWidgetUtils.createCheckbox( composite,
            Messages.getString( "PasswordModifyExtendedOperationDialog.NoOldPassword" ), 2 ); //$NON-NLS-1$
        noOldPasswordCheckbox.addSelectionListener( noOldPasswordCheckboxListener );

        // New password text
        BaseWidgetUtils.createLabel( composite,
            Messages.getString( "PasswordModifyExtendedOperationDialog.NewPassword" ), 1 ); //$NON-NLS-1$
        newPasswordText = BaseWidgetUtils.createText( composite, newPassword, 2 );
        newPasswordText.setEchoChar( '•' );
        newPasswordText.addModifyListener( event -> validate() );

        // Generate new password checkbox
        BaseWidgetUtils.createLabel( composite, StringUtils.EMPTY, 1 );
        generateNewPasswordCheckbox = BaseWidgetUtils.createCheckbox( composite,
            Messages.getString( "PasswordModifyExtendedOperationDialog.GenerateNewPassword" ), 2 ); //$NON-NLS-1$
        generateNewPasswordCheckbox.addSelectionListener( generateNewPasswordCheckboxListener );

        // Show password checkbox
        BaseWidgetUtils.createLabel( composite, StringUtils.EMPTY, 1 );
        showPasswordsCheckbox = BaseWidgetUtils.createCheckbox( composite,
            Messages.getString( "PasswordModifyExtendedOperationDialog.ShowPasswords" ), 2 ); //$NON-NLS-1$
        showPasswordsCheckbox.addSelectionListener( showPasswordsCheckboxListener );

        applyDialogFont( composite );

        return composite;
    }


    // ── MACE CHECKS IF PALPATINE'S ANSWERS ARE SUFFICIENT ────────────────────
    // Mace checks each Master's signal: is the identity valid? Is the old password
    // present or waived? Is the new password specified or delegated to the server?
    // Only when all three signals are green does he allow the operation to proceed.
    // We enable the OK button only when all three input groups are in a valid state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Evaluates whether all required inputs are in a valid state and enables or
     * disables the OK button accordingly.
     * Called after every checkbox toggle and text field modification.
     * There are three independent validity conditions, each with a "bypass" checkbox
     * (use bind identity / old password not available / let server generate new pw).
     *
     * <p>For example — Mace checks three conditions before acting:</p>
     * <pre>
     *   identity valid?    → useBindIdentity checked OR entryWidget has a non-empty DN
     *   old password ok?   → noOldPassword checked OR oldPasswordText non-empty
     *   new password ok?   → generateNewPassword checked OR newPasswordText non-empty
     *   all three true → OK button enabled → Mace gives the signal
     * </pre>
     */
    private void validate()
    {
        if ( getButton( IDialogConstants.OK_ID ) != null )
        {
            boolean userIdentityInputValid = useBindUserIdentityCheckbox.getSelection()
                || ( entryWidget.getDn() != null && !entryWidget.getDn().isEmpty() );
            boolean oldPasswordInputValid = noOldPasswordCheckbox.getSelection()
                || !oldPasswordText.getText().isEmpty();
            boolean newPasswordInputValid = generateNewPasswordCheckbox.getSelection()
                || !newPasswordText.getText().isEmpty();
            getButton( IDialogConstants.OK_ID )
                .setEnabled( userIdentityInputValid && oldPasswordInputValid && newPasswordInputValid );
        }
    }


    // ── MACE RETRIEVES THE CONFIRMED IDENTITY ─────────────────────────────────
    // After the confrontation ends, the other Jedi ask Mace: "Who was it?"
    // He hands them the DN — the definitive answer — or null if the user
    // escaped by cancelling.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the user identity DN collected from the dialog.
     * The caller uses this to know whose password was changed.
     * If the user cancelled the dialog this returns null.
     *
     * <p>For example — Mace reports the confirmed identity:</p>
     * <pre>
     *   "It was Palpatine — uid=palpatine,ou=sith,dc=galaxy,dc=com"
     * </pre>
     *
     * @return  The {@link Dn} of the user whose password was changed, or
     *          {@code null} if the dialog was cancelled.
     */
    public Dn getUserIdentity()
    {
        return userIdentity;
    }


    // ── MACE PRODUCES THE OLD CREDENTIAL ─────────────────────────────────────
    // Mace hands over the old lightsaber configuration — the proof of what the
    // user held before the change — so the server can verify continuity.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the old password as entered by the user.
     * The caller passes this to the RFC 3062 request so the server can verify
     * the user knew the previous credential.
     * Returns an empty string if the "old password not available" checkbox was
     * checked, and {@code null} if the dialog was cancelled.
     *
     * <p>For example — Mace presents the previous credential for verification:</p>
     * <pre>
     *   "This is what he held before" → server validates → change authorized
     * </pre>
     *
     * @return  The old password string, empty string if waived, or {@code null}
     *          if the dialog was cancelled.
     */
    public String getOldPassword()
    {
        return oldPassword;
    }


    // ── MACE DECLARES THE NEW ORDER ──────────────────────────────────────────
    // Mace specifies what authority the new order will carry — the new credential
    // that replaces the old one, or null if the server is delegated to generate it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the new password as entered by the user.
     * If the "generate new password" checkbox was checked this will be an empty
     * string — the server picks the password and returns it in the response.
     * Returns {@code null} if the dialog was cancelled.
     *
     * <p>For example — Mace declares the new authority credential:</p>
     * <pre>
     *   "The new order begins with this" → server stores it → operation complete
     * </pre>
     *
     * @return  The new password string, empty string if server-generated was
     *          requested, or {@code null} if the dialog was cancelled.
     */
    public String getNewPassword()
    {
        return newPassword;
    }
}
