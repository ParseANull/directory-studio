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
package org.apache.directory.studio.aciitemeditor.dialogs;


import java.text.ParseException;

import org.apache.directory.studio.aciitemeditor.ACIITemConstants;
import org.apache.directory.studio.aciitemeditor.ACIItemValueWithContext;
import org.apache.directory.studio.aciitemeditor.Activator;
import org.apache.directory.studio.aciitemeditor.widgets.ACIItemTabFolderComposite;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: ACIItemDialog — GRAND MOFF TARKIN REVIEWING A SECURITY DIRECTIVE ──
// Grand Moff Tarkin opens the full ACI security directive on his console:
// he sees a Visual tab for point-and-click editing and a Source tab for raw text.
// Two extra buttons — Format and Check Syntax — sit in the button bar so he can
// tidy the directive and verify it before sealing and filing it.
// ACIItemDialog is that console: the main entry point for the user to edit an
// aciItem LDAP attribute.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Main JFace {@link Dialog} for editing an ACI item string.
 * Contains an {@link ACIItemTabFolderComposite} (Visual + Source tabs) and two
 * extra buttons — Format and Check Syntax — in addition to the standard OK/Cancel.
 * Pressing OK validates the ACI string and stores the result; pressing Cancel
 * discards changes.
 * Think of this class as Grand Moff Tarkin's security-directive console: he opens
 * it, reviews the directive visually or in raw source, checks the syntax, and then
 * signs it off with OK.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ACIItemDialog extends Dialog
{
    private static final int FORMAT_BUTTON = 987654321;
    private static final int CHECK_SYNTAX_BUTTON = 876543210;

    /** The context containing the initial value, passed by the constructor */
    private ACIItemValueWithContext context;

    /** The resulting value returned by getACIItemValue() */
    private String returnValue;

    /** The child composite with the tabs */
    private ACIItemTabFolderComposite tabFolderComposite;


    // ── OPEN THE DIRECTIVE CONSOLE ─────────────────────────────────────────────
    // Grand Moff Tarkin sits at his console, the briefing packet in hand.
    // The context carries the connection, the target entry, and the current
    // ACI string — everything the console needs to pre-populate itself.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ACIItemDialog} for editing the ACI string held in
     * {@code context}.
     * The context must be non-null and must contain a non-null ACI item value and
     * a non-null connection.
     *
     * <p>For example — opening the dialog from ACIItemValueEditor:</p>
     * <pre>
     *   ACIItemDialog dialog = new ACIItemDialog(shell, valueWithContext);
     *   if (dialog.open() == Dialog.OK) {
     *     String newValue = dialog.getACIItemValue();
     *   }
     * </pre>
     *
     * @param parentShell  the parent SWT shell
     * @param context      the DTO carrying the current connection, entry, and ACI string
     */
    public ACIItemDialog( Shell parentShell, ACIItemValueWithContext context )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );

        assert context != null;
        assert context.getACIItemValue() != null;
        assert context.getConnection() != null;

        this.context = context;

        this.returnValue = null;
    }


    // ── TITLE AND ICON ON THE CONSOLE WINDOW ──────────────────────────────────
    // The orderly labels Grand Moff Tarkin's console window with the official
    // title and the ISB shield icon so there is no mistaking its purpose.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog title and icon from the NLS messages bundle.
     *
     * {@inheritDoc}
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "ACIItemDialog.dialog.text" ) ); //$NON-NLS-1$
        shell.setImage( Activator.getDefault().getImage( Messages.getString( "ACIItemDialog.dialog.icon" ) ) ); //$NON-NLS-1$
    }


    // ── ADD FORMAT AND CHECK-SYNTAX BUTTONS ───────────────────────────────────
    // The orderly installs two extra buttons on the console: Format (to tidy the
    // raw text) and Check Syntax (to verify the directive without committing).
    // The standard OK and Cancel buttons follow them in the bar.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the Format and Check Syntax buttons before the standard OK/Cancel buttons.
     *
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        createButton( parent, FORMAT_BUTTON, Messages.getString( "ACIItemDialog.button.format" ), false ); //$NON-NLS-1$
        createButton( parent, CHECK_SYNTAX_BUTTON, Messages.getString( "ACIItemDialog.button.checkSyntax" ), false ); //$NON-NLS-1$
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    // ── DISPATCH THE EXTRA BUTTON CLICKS ─────────────────────────────────────
    // When Grand Moff Tarkin presses Format, the console tidies the raw text.
    // When he presses Check Syntax, the console parses the ACI and either shows
    // a green "syntax OK" dialog or a red error pop-up with the parse exception.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Handles the Format and Check Syntax buttons in addition to the standard
     * OK/Cancel buttons.
     * Format delegates to {@link ACIItemTabFolderComposite#format()};
     * Check Syntax calls {@link ACIItemTabFolderComposite#getInput()} and shows
     * either a success message or an {@link ErrorDialog} with the parse exception.
     *
     * {@inheritDoc}
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
                MessageDialog
                    .openInformation(
                        getShell(),
                        Messages.getString( "ACIItemDialog.syntaxOk.title" ), Messages.getString( "ACIItemDialog.syntaxOk.text" ) ); //$NON-NLS-1$ //$NON-NLS-2$
            }
            catch ( ParseException pe )
            {
                IStatus status = new Status( IStatus.ERROR, ACIITemConstants.PLUGIN_ID, 1, Messages
                    .getString( "ACIItemDialog.error.invalidSyntax" ), pe ); //$NON-NLS-1$
                ErrorDialog.openError( getShell(), Messages.getString( "ACIItemDialog.error.title" ), null, status ); //$NON-NLS-1$
            }
        }

        // call super implementation
        super.buttonPressed( buttonId );
    }


    // ── VALIDATE AND COMMIT ON OK ─────────────────────────────────────────────
    // Grand Moff Tarkin presses OK: the console attempts to parse the current
    // ACI text.  If it parses cleanly, the result is stored and the dialog
    // closes.  If not, an error pop-up appears and the dialog stays open.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current ACI string before closing.
     * Calls {@link ACIItemTabFolderComposite#getInput()} to parse; if successful
     * the result is stored in {@code returnValue} and the dialog closes.
     * If a {@link ParseException} is thrown an {@link ErrorDialog} is shown
     * and the dialog remains open.
     */
    protected void okPressed()
    {
        try
        {
            this.returnValue = tabFolderComposite.getInput();
            super.okPressed();
        }
        catch ( ParseException pe )
        {
            IStatus status = new Status( IStatus.ERROR, ACIITemConstants.PLUGIN_ID, 1, Messages
                .getString( "ACIItemDialog.error.invalidSyntax" ), pe ); //$NON-NLS-1$
            ErrorDialog.openError( getShell(), Messages.getString( "ACIItemDialog.error.title" ), null, status ); //$NON-NLS-1$
        }
    }


    // ── BUILD THE VISUAL AREA ─────────────────────────────────────────────────
    // The orderly installs the two-tab composite into the dialog's content area
    // and pre-loads it with the initial ACI string from the context.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the {@link ACIItemTabFolderComposite} and pre-populates it with
     * the ACI string from the constructor context.
     *
     * @param parent  the parent composite provided by the Dialog framework
     * @return        the fully constructed dialog content area
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH ) * 4 / 3;
        gd.heightHint = convertVerticalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH ) * 4 / 3;
        composite.setLayoutData( gd );

        tabFolderComposite = new ACIItemTabFolderComposite( composite, SWT.NONE );

        // set initial value
        if ( context != null )
        {
            tabFolderComposite.setContext( context );
            tabFolderComposite.setInput( context.getACIItemValue() );
        }

        applyDialogFont( composite );
        return composite;
    }


    // ── RETURN THE COMMITTED ACI STRING ──────────────────────────────────────
    // After Grand Moff Tarkin seals the directive, the value editor retrieves
    // the signed ACI string and stores it back in the LDAP attribute.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the validated ACI item string after the dialog was closed with OK.
     * Returns {@code null} if the dialog was cancelled or has not yet been opened.
     *
     * @return the serialised ACI item string, or {@code null}
     */
    public String getACIItemValue()
    {
        return returnValue;
    }

}
