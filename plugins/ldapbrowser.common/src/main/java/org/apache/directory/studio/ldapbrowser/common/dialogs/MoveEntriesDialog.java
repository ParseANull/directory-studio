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

package org.apache.directory.studio.ldapbrowser.common.dialogs;


import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.common.widgets.DnBuilderWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: MoveEntriesDialog — LEIA TRANSFERRED BETWEEN CELLS ────────────────
// Aboard the Death Star, Leia is moved from her cell in Detention Block AA-23 to
// a completely different location — she knows she is being transferred, she just
// needs to know where she is being taken.  The Empire selects a new parent
// location and executes the move; Leia ends up at the new address.
// Moving LDAP entries works exactly this way: the entry's RDN stays the same but
// its parent DN changes, so its full DN becomes different.  This dialog asks the
// user to pick the new parent DN — the destination "cell block" — and that is all
// we need to issue the LDAP ModifyDN operation with a new superior.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A dialog that asks the user to choose a new parent DN for one or more LDAP
 * entries.  Moving entries in LDAP is a ModifyDN operation where we keep the
 * existing RDN but change the parent (the "new superior" parameter).  This dialog
 * uses a {@link DnBuilderWidget} in "parent-only" mode so the user can browse or
 * type the destination parent DN.
 * Think of this class as the scene where Leia is transferred between detention
 * cells: same person, new location — just tell us which block to move her to.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MoveEntriesDialog extends Dialog implements WidgetModifyListener
{

    /** The dialog title. */
    private static final String DIALOG_TITLE = Messages.getString( "MoveEntriesDialog.MoveEntries" ); //$NON-NLS-1$

    /** The entries to move. */
    private IEntry[] entries;

    /** The dn builder widget. */
    private DnBuilderWidget dnBuilderWidget;

    /** The ok button. */
    private Button okButton;

    /** The parent Dn. */
    private Dn parentDn;


    // ── THE EMPIRE ISSUES THE TRANSFER ORDER ─────────────────────────────────
    // An Imperial officer receives the transfer order: "Move prisoner Organa —
    // these are the entries to relocate."  He notes the current location and
    // prepares the transport, destination to be determined.
    // We capture the entries to move and initialise the destination parentDn to
    // null — it will be filled in once the user makes their choice.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new MoveEntriesDialog for the given entries.  The dialog starts
     * with no destination selected ({@code parentDn} is {@code null}) — the user
     * must choose one before OK is enabled.
     *
     * <p>For example — the Empire issues the transfer order:</p>
     * <pre>
     *   MoveEntriesDialog dialog = new MoveEntriesDialog(shell, selectedEntries);
     *   if (dialog.open() == OK) {
     *       Dn destination = dialog.getParentDn();
     *   }
     * </pre>
     *
     * @param parentShell  the shell that owns this dialog
     * @param entries      the LDAP entries to move; must contain at least one entry
     */
    public MoveEntriesDialog( Shell parentShell, IEntry[] entries )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.entries = entries;
        this.parentDn = null;
    }


    // ── THE OFFICER LABELS THE TRANSPORT MANIFEST ────────────────────────────
    // The officer stamps the manifest with the operation title: "Transfer Order —
    // Move Prisoner."  Without a title no one knows which operation is in progress.
    // We apply the dialog window title from the localised messages bundle.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog window title before the shell is shown.
     *
     * <p>For example — the officer stamps the manifest:</p>
     * <pre>
     *   shell.setText("Move Entries");
     * </pre>
     *
     * @param shell  the shell Eclipse hands us to configure
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( DIALOG_TITLE );
    }


    // ── THE TRANSPORT DOCKS AND RELEASES THE PRISONER ────────────────────────
    // When the transport docks at the destination the prisoner is released —
    // but before that happens, the transport crew detaches the tracking beacon
    // and powers down their equipment.
    // We unhook the widget modify listener and dispose the DnBuilderWidget when
    // the dialog closes so we don't leak listeners or SWT resources.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up the {@link DnBuilderWidget} when the dialog is closed.  We
     * remove our {@link WidgetModifyListener} first, then dispose the widget to
     * release any SWT resources it holds.  Forgetting to do this would leak the
     * listener and keep references alive after the dialog is gone.
     *
     * <p>For example — the transport crew powers down:</p>
     * <pre>
     *   dnBuilderWidget.removeWidgetModifyListener(this);
     *   dnBuilderWidget.dispose();
     *   return super.close();
     * </pre>
     *
     * @return  {@code true} if the dialog was successfully closed
     */
    public boolean close()
    {
        dnBuilderWidget.removeWidgetModifyListener( this );
        dnBuilderWidget.dispose();
        return super.close();
    }


    // ── LEIA ARRIVES AT HER NEW DESTINATION ──────────────────────────────────
    // The transport locks on to the destination coordinates and completes the
    // transfer — Leia is now at her new location.  The crew also logs the
    // destination for future reference.
    // We capture the chosen parent DN and persist the widget's state to dialog
    // settings so recently browsed paths appear in future auto-complete suggestions.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user confirms with OK.  We read the chosen parent DN from
     * the widget and save the dialog settings (browse history) so the user's
     * recent destinations appear as suggestions next time.
     *
     * <p>For example — Leia arrives and the location is logged:</p>
     * <pre>
     *   parentDn = dnBuilderWidget.getParentDn();
     *   dnBuilderWidget.saveDialogSettings();
     *   super.okPressed();
     * </pre>
     */
    protected void okPressed()
    {
        parentDn = dnBuilderWidget.getParentDn();
        dnBuilderWidget.saveDialogSettings();
        super.okPressed();
    }


    // ── THE OFFICER PREPARES THE DEPARTURE AND ABORT SEALS ───────────────────
    // Every transfer order has two outcomes: go through with it (OK) or stand
    // down (Cancel).  The officer readies both seals before the transport departs.
    // We create OK (default) and Cancel.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates the OK and Cancel buttons.  OK is the default button and starts
     * disabled until the user selects a valid parent DN via
     * {@link #widgetModified(WidgetModifyEvent)}.
     *
     * <p>For example — the officer prepares both seals:</p>
     * <pre>
     *   createButton(OK);      // "Execute transfer"
     *   createButton(CANCEL);  // "Stand down"
     * </pre>
     *
     * @param parent  the button-bar composite Eclipse provides
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    // ── THE NAVIGATOR PLOTS THE DESTINATION COORDINATES ──────────────────────
    // The Imperial navigator unfolds the star chart and plots the destination
    // coordinates — the prisoner needs to know where she is being taken, and the
    // system checks that the destination actually exists before the transport can
    // depart.
    // We build the description label and the DnBuilderWidget (parent-only mode),
    // pre-seeded with the current parent DN of the first entry so the user can
    // simply navigate from there.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog's content area: a description label followed by a
     * {@link DnBuilderWidget} configured to browse and select a parent DN (no
     * RDN entry — we only need the destination parent, not a rename).  The
     * widget is pre-seeded with the current parent of the first entry so the user
     * starts near the right part of the tree.
     *
     * <p>For example — the navigator plots the destination:</p>
     * <pre>
     *   label("Select the new parent for the selected entries:");
     *   dnBuilderWidget.setInput(connection, null, null, currentParentDn);
     * </pre>
     *
     * @param parent  the parent composite Eclipse provides
     * @return        the completed content area control
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH ) * 3 / 2;
        composite.setLayoutData( gd );

        BaseWidgetUtils.createLabel( composite, Messages.getString( "MoveEntriesDialog.MoveEntriesDescription" ), 1 ); //$NON-NLS-1$

        dnBuilderWidget = new DnBuilderWidget( false, true );
        dnBuilderWidget.addWidgetModifyListener( this );
        dnBuilderWidget.createContents( composite );
        dnBuilderWidget
            .setInput( entries[0].getBrowserConnection(), null, null, entries[0].getDn().getParent() );

        applyDialogFont( composite );
        return composite;
    }


    // ── THE NAVIGATOR CHECKS WHETHER THE DESTINATION EXISTS ──────────────────
    // The navigator scans the destination coordinates — if the location is valid
    // and exists in the system, the transport is cleared to depart.  If not, the
    // departure seal stays locked.
    // We enable or disable the OK button depending on whether the widget holds a
    // non-null parent DN.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called whenever the {@link DnBuilderWidget} changes.  We update the OK
     * button's enabled state: it is enabled only when the widget contains a valid
     * (non-null) parent DN.  This prevents the user from confirming without a
     * destination.
     *
     * <p>For example — the navigator clears the departure only when coords are valid:</p>
     * <pre>
     *   okButton.setEnabled(dnBuilderWidget.getParentDn() != null);
     * </pre>
     *
     * @param event  the widget modification event — we don't use its content, just the fact that something changed
     */
    public void widgetModified( WidgetModifyEvent event )
    {
        if ( okButton != null )
        {
            okButton.setEnabled( dnBuilderWidget.getParentDn() != null );
        }
    }


    // ── THE REBELS DEBRIEF: WHERE DID LEIA END UP? ───────────────────────────
    // After the mission the Rebels need to know exactly where Leia was taken so
    // they can plan the rescue.  The parent DN is that location.
    // Callers retrieve the chosen parent DN here after the dialog closes with OK.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent DN that the user chose as the move destination.  Call
     * this after the dialog closes with OK; returns {@code null} if the user
     * cancelled or if OK was never pressed.
     *
     * <p>For example — the Rebels locate the detention block:</p>
     * <pre>
     *   Dn newParent = dialog.getParentDn();
     *   // "ou=Executives,dc=empire,dc=gov"
     * </pre>
     *
     * @return  the chosen parent {@link Dn}, or {@code null} if the dialog was cancelled
     */
    public Dn getParentDn()
    {
        return parentDn;
    }

}
