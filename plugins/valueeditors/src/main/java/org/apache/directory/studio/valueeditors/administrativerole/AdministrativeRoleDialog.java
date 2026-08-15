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

package org.apache.directory.studio.valueeditors.administrativerole;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.ui.widgets.ExtendedContentAssistCommandAdapter;
import org.apache.directory.studio.ldapbrowser.common.widgets.ListContentProposalProvider;
import org.apache.directory.studio.valueeditors.ValueEditorsActivator;
import org.apache.directory.studio.valueeditors.ValueEditorsConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: AdministrativeRoleDialog — PALPATINE DESIGNATES A SECTOR OVERSEER ─
// In the Imperial Senate chamber, Palpatine selects which administrative role
// a particular subtree will be assigned to — autonomousArea, accessControlSpecificArea,
// and so on — by picking from a fixed list of approved designations with auto-complete.
// The LDAP administrativeRole attribute works the same way: it's a controlled
// vocabulary that names which role manages a given directory subtree.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Modal dialog for entering or selecting an LDAP administrative role value.
 * LDAP administrative roles (like {@code autonomousArea} or
 * {@code accessControlSpecificArea}) are from a fixed controlled vocabulary;
 * this dialog provides a combo box with those known values and content-assist
 * auto-complete so the user doesn't have to remember them all.
 * Used by {@link AdministrativeRoleValueEditor} when the user double-clicks
 * the {@code administrativeRole} attribute.
 * Think of this as Palpatine's sector-assignment panel — pick a role from the
 * approved list and confirm.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AdministrativeRoleDialog extends Dialog
{

    /** The possible administrative role values. */
    private static final String[] administrativeRoleValues = new String[]
        { "autonomousArea", "accessControlSpecificArea", "accessControlInnerArea", "subschemaAdminSpecificArea", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "collectiveAttributeSpecificArea", "collectiveAttributeInnerArea", "triggerExecutionSpecificArea", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "triggerExecutionInnerArea" }; //$NON-NLS-1$

    /** The initial value. */
    private String initialValue;

    /** The administrative role combo. */
    private Combo administrativeRoleCombo;

    /** The return value. */
    private String returnValue;


    // ── Palpatine Opens the Designation Session ───────────────────────────────
    // Palpatine enters the Senate chamber, acknowledges the parent window, and
    // announces he is ready to assign an administrative role to a subtree.
    // He keeps note of the current role so the UI shows where we started.
    // We store the existing value and set up the shell to be resizable.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new AdministrativeRoleDialog ready to edit the given role value.
     * The dialog is configured to allow resizing so long role names are fully visible.
     *
     * <p>For example — Palpatine begins a new sector assignment:</p>
     * <pre>
     *   AdministrativeRoleDialog dialog =
     *       new AdministrativeRoleDialog(shell, "autonomousArea");
     *   dialog.open();
     * </pre>
     *
     * @param parentShell   The SWT shell that owns this dialog.
     * @param initialValue  The current administrative role string pre-filled in the combo.
     */
    public AdministrativeRoleDialog( Shell parentShell, String initialValue )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.initialValue = initialValue;
        this.returnValue = null;
    }


    // ── Palpatine Labels the Chamber ─────────────────────────────────────────
    // Palpatine titles the Senate session "Administrative Role Editor" and
    // displays the Imperial seal on the chamber door.
    // We set the dialog title and toolbar icon here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Configures the dialog's shell — sets the window title and icon.
     * Eclipse calls this just before the dialog becomes visible.
     *
     * @param shell  The SWT Shell we're configuring.
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "AdministrativeRoleDialog.AdministrativeRoleEditor" ) ); //$NON-NLS-1$
        shell.setImage( ValueEditorsActivator.getDefault()
            .getImage( ValueEditorsConstants.IMG_ADMINISTRATIVEROLEEDITOR ) );
    }


    // ── Palpatine Readies the Confirmation Buttons ────────────────────────────
    // Palpatine places the "Confirm Designation" and "Rescind" buttons in the
    // standard positions at the base of the chamber podium.
    // We delegate entirely to the JFace superclass for the standard OK/Cancel bar.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Adds the standard OK and Cancel buttons to the button bar.
     * We don't customise the button bar for this dialog — the defaults are fine.
     *
     * @param parent  The composite hosting the button bar.
     */
    @Override
    protected void createButtonsForButtonBar( Composite parent )
    {
        super.createButtonsForButtonBar( parent );
    }


    // ── Palpatine Records the Chosen Designation ──────────────────────────────
    // Palpatine nods his approval — the chosen role is inscribed in the Imperial
    // record and the session is formally closed.
    // We capture the combo's current text as the return value.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks OK — captures the currently selected or typed
     * role string from the combo and stores it as the return value.
     * Callers retrieve this via {@link #getAdministrativeRole()}.
     */
    @Override
    protected void okPressed()
    {
        returnValue = administrativeRoleCombo.getText();
        super.okPressed();
    }


    // ── Palpatine Presents the Approved Role List ─────────────────────────────
    // Palpatine unfolds the approved list of sector designations on the podium
    // and invites the Senator to pick one (or type a known abbreviation with
    // auto-complete suggestions appearing as he types).
    // We build the combo box, pre-fill it with the known role names, and attach
    // content-assist so typing partial names triggers a suggestion dropdown.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the main content area — a combo box pre-loaded with the known
     * administrative role values, with content-assist auto-complete.
     * The combo allows free text entry in addition to the predefined list, since
     * a directory schema might define custom role names we don't know about.
     *
     * <p>For example — Palpatine presents the roster:</p>
     * <pre>
     *   [ autonomousArea                    ▼ ]
     *   Suggestions: accessControlSpecificArea, accessControlInnerArea, ...
     * </pre>
     *
     * @param parent  The parent composite provided by JFace's dialog framework.
     * @return        The top-level composite containing our combo widget.
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        // create composite
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        composite.setLayoutData( gd );

        // attribute combo with field decoration and content proposal
        administrativeRoleCombo = BaseWidgetUtils.createCombo( composite, new String[0], -1, 1 );
        administrativeRoleCombo.setVisibleItemCount( 20 );
        administrativeRoleCombo.setItems( administrativeRoleValues );
        administrativeRoleCombo.setText( initialValue );
        new ExtendedContentAssistCommandAdapter( administrativeRoleCombo, new ComboContentAdapter(),
            new ListContentProposalProvider( administrativeRoleCombo.getItems() ), null, null, true );

        applyDialogFont( composite );
        return composite;
    }


    // ── Palpatine Announces the Chosen Role ───────────────────────────────────
    // The Imperial Herald reads back the formally chosen designation for the
    // record: "The subtree shall be governed as an autonomousArea."
    // We return whatever the user selected or typed in the combo.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the administrative role string the user confirmed, or {@code null}
     * if the dialog was cancelled.
     *
     * <p>For example — the caller reads back the designation:</p>
     * <pre>
     *   if (dialog.open() == Dialog.OK) {
     *       String role = dialog.getAdministrativeRole();
     *       // → "autonomousArea"
     *   }
     * </pre>
     *
     * @return  The chosen role string, or {@code null} if the user cancelled.
     */
    public String getAdministrativeRole()
    {
        return returnValue;
    }

}
