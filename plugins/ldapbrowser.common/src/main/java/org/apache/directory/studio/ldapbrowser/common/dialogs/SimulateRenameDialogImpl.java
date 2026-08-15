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
import org.apache.directory.studio.ldapbrowser.core.jobs.SimulateRenameDialog;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: SimulateRenameDialogImpl — THE REBEL DECOY MANEUVER ───────────────
// During the Battle of Scarif, the Rebel fleet executes a decoy maneuver: they
// appear to be going one way — a conventional LDAP ModifyDN — but when the
// server doesn't support it, they quietly go another: add the entry under the
// new name, copy all its children recursively, and delete the old one.
// The result looks the same from the outside: the entry ends up at the new DN.
// But the implementation is completely different.  This dialog warns the user
// that the server doesn't support native rename for this entry and asks whether
// to proceed with the simulation — the decoy maneuver — instead.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A dialog that implements {@link SimulateRenameDialog} and warns the user that
 * the server cannot perform a native ModifyDN rename (for example, because the
 * entry has children and the server doesn't support subtree rename).  We offer
 * to simulate the rename by recursively copying the entry and its children to
 * the new DN and then deleting the originals — a slower but functionally
 * equivalent approach.
 * Think of it as the Rebel decoy maneuver: we appear to do one thing but
 * actually execute a completely different sequence of operations to get the same
 * result.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SimulateRenameDialogImpl extends Dialog implements SimulateRenameDialog
{

    /** The dialog title. */
    private String dialogTitle = Messages.getString( "SimulateRenameDialogImpl.SimulateRename" ); //$NON-NLS-1$

    /** The simulate rename flag */
    private boolean isSimulateRename;

    /** The browser connection. */
    private IBrowserConnection browserConnection;

    /** The old Dn. */
    private Dn oldDn;

    /** The new Dn. */
    private Dn newDn;


    // ── THE REBEL COMMANDER BRIEFS THE DECOY CREW ────────────────────────────
    // Commander Draven tells the crew: "We may need to execute the decoy maneuver.
    // Stand by."  No details yet — the target coordinates come later.  For now
    // the dialog just prepares itself; the isSimulateRename flag starts false
    // so if anything goes wrong before the user sees the dialog we don't
    // accidentally simulate a rename.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SimulateRenameDialogImpl.  The dialog is resizable so long
     * DN strings don't get clipped.  {@code isSimulateRename} starts {@code false}
     * — it becomes {@code true} only when the user explicitly clicks OK.
     *
     * <p>For example — the crew stands by for orders:</p>
     * <pre>
     *   SimulateRenameDialogImpl dialog = new SimulateRenameDialogImpl(shell);
     *   dialog.setEntryInfo(connection, oldDn, newDn);
     *   dialog.open();
     * </pre>
     *
     * @param parentShell  the shell that owns this dialog
     */
    public SimulateRenameDialogImpl( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
    }


    // ── THE COMMANDER LABELS THE MISSION BRIEFING ROOM ───────────────────────
    // Above the door of the briefing room a sign reads "Simulate Rename."  Crew
    // members need to know what kind of operation they are being briefed on.
    // We apply the window title to the shell so users understand they are looking
    // at a simulate-rename confirmation, not a simple rename dialog.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Applies the "Simulate Rename" title to the dialog shell before it is shown.
     *
     * <p>For example — the briefing room door is labelled:</p>
     * <pre>
     *   shell.setText("Simulate Rename");
     * </pre>
     *
     * @param shell  the shell Eclipse hands us to configure
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( dialogTitle );
    }


    // ── THE CREW SAYS "GO" — EXECUTE THE DECOY ───────────────────────────────
    // The crew receives the "go" signal and flips the execute switch.  From this
    // point on they are committed to the decoy maneuver.
    // On OK we set isSimulateRename to true — the calling job will see this and
    // execute the copy-then-delete sequence instead of a simple ModifyDN.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks OK.  We set {@code isSimulateRename} to
     * {@code true} so the rename job knows to execute the simulation path
     * (recursive copy + delete) rather than a native ModifyDN.
     *
     * <p>For example — the crew flips the execute switch:</p>
     * <pre>
     *   isSimulateRename = true;   // "Go" signal received
     *   super.okPressed();
     * </pre>
     */
    protected void okPressed()
    {
        isSimulateRename = true;
        super.okPressed();
    }


    // ── DRAVEN PREPARES EXECUTE AND ABORT ORDERS ─────────────────────────────
    // Two orders are on the table: "Execute decoy" (OK) and "Stand down"
    // (Cancel).  Neither is the default — the commander must commit deliberately.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates OK and Cancel buttons.  Neither is the dialog default — this is a
     * significant, potentially slow operation and the user should make an
     * explicit choice.
     *
     * <p>For example — Draven prepares execute and abort:</p>
     * <pre>
     *   createButton(OK,     defaultButton=false);
     *   createButton(CANCEL, defaultButton=false);
     * </pre>
     *
     * @param parent  the button-bar composite Eclipse provides
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    // ── THE COMMANDER READS THE MISSION BRIEFING ALOUD ───────────────────────
    // Commander Draven reads the briefing to the assembled crew: "We need to
    // rename entry X to Y on server Z, but the server can't do it natively —
    // here's the alternative.  Click OK to authorise the decoy."
    // We build three labels explaining what will happen: old DN → new DN, which
    // connection it's on, and a reminder that the user can click the Simulate
    // button to proceed.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog content area: three informational labels describing the
     * rename operation and explaining that the server cannot perform it natively,
     * so we will simulate it via recursive add + delete.
     *
     * <p>For example — the commander reads the mission briefing:</p>
     * <pre>
     *   label("Rename 'cn=Jyn,ou=Rebels' to 'cn=Jyn Erso,ou=Heroes' ...");
     *   label("... on connection 'Scarif LDAP' cannot be done natively.");
     *   label("Click OK to simulate the rename (add/delete recursively).");
     * </pre>
     *
     * @param parent  the parent composite Eclipse provides
     * @return        the completed content area control
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        composite.setLayoutData( gd );

        Composite innerComposite = BaseWidgetUtils.createColumnContainer( composite, 1, 1 );
        gd = new GridData( GridData.FILL_BOTH );
        innerComposite.setLayoutData( gd );

        String text1 = NLS
            .bind(
                Messages.getString( "SimulateRenameDialogImpl.SimulateRenameDescription1" ), oldDn.getName(), newDn.getName() ); //$NON-NLS-1$
        BaseWidgetUtils.createLabel( innerComposite, text1, 1 );

        String text2 = NLS.bind( Messages.getString( "SimulateRenameDialogImpl.SimulateRenameDescription2" ), //$NON-NLS-1$
            browserConnection.getConnection().getName() );
        BaseWidgetUtils.createLabel( innerComposite, text2, 1 );

        String text3 = Messages.getString( "SimulateRenameDialogImpl.SimulateButton" ); //$NON-NLS-1$
        BaseWidgetUtils.createLabel( innerComposite, text3, 1 );

        applyDialogFont( composite );
        return composite;
    }


    // ── THE CREW EXECUTES THE DECOY FROM A BACKGROUND THREAD ─────────────────
    // The decoy operation runs aboard a background ship — but someone still needs
    // to relay the order to the crew on the bridge (the UI thread).  We
    // syncExec so the background job thread can show the dialog on the UI thread
    // and block until the user responds.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Opens the dialog from any thread safely.  The rename job runs on a
     * background worker thread, but all SWT widgets must be accessed on the UI
     * thread.  We use {@link Display#syncExec(Runnable)} to hop onto the UI
     * thread, open the dialog, block until the user responds, and then return
     * the result to the calling thread.
     *
     * <p>For example — the background ship relays the order to the bridge:</p>
     * <pre>
     *   Display.getDefault().syncExec(() -> {
     *       result = super.open();   // runs on UI thread, blocks here
     *   });
     *   return result;
     * </pre>
     *
     * @return  the dialog return code — {@link Dialog#OK} or {@link Dialog#CANCEL}
     */
    public int open()
    {
        final int[] result = new int[1];
        Display.getDefault().syncExec( new Runnable()
        {
            public void run()
            {
                result[0] = SimulateRenameDialogImpl.super.open();
            }
        } );
        return result[0];
    }


    // ── DRAVEN RECEIVES THE MISSION COORDINATES ───────────────────────────────
    // Before the briefing can happen Draven needs the coordinates: "which entry
    // is being renamed, on which server, and what is the new DN?"  Without this
    // the labels in the dialog would be blank.
    // The rename job must call this before {@link #open()} to populate the
    // dialog with the actual DNs and connection.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Supplies the rename context before the dialog is opened.  The rename job
     * must call this immediately after constructing the dialog and before calling
     * {@link #open()} — without it the dialog labels will throw a
     * {@code NullPointerException} when building the content area.
     *
     * <p>For example — Draven receives the mission coordinates:</p>
     * <pre>
     *   dialog.setEntryInfo(connection,
     *       Dn.of("cn=Jyn,ou=Rebels,dc=scarif,dc=org"),
     *       Dn.of("cn=Jyn Erso,ou=Heroes,dc=scarif,dc=org"));
     * </pre>
     *
     * @param browserConnection  the connection that hosts the entry being renamed
     * @param oldDn              the current (old) DN of the entry
     * @param newDn              the desired (new) DN after the rename
     */
    public void setEntryInfo( IBrowserConnection browserConnection, Dn oldDn, Dn newDn )
    {
        this.browserConnection = browserConnection;
        this.oldDn = oldDn;
        this.newDn = newDn;
    }


    // ── DRAVEN REPORTS: DID WE GO WITH THE DECOY? ────────────────────────────
    // After the dialog closes, mission control asks: "Did we authorise the decoy
    // maneuver?"  Draven reports back — yes or no.
    // The rename job calls this to decide whether to execute the simulation path.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the user authorised the simulated rename.  Call this after
     * the dialog closes; {@code true} means the user clicked OK and the rename
     * job should proceed with the recursive copy + delete approach.
     * {@code false} means the user cancelled and the rename should be aborted.
     *
     * <p>For example — mission control checks the authorisation:</p>
     * <pre>
     *   if (dialog.isSimulateRename()) {
     *       job.executeSimulation();
     *   }
     * </pre>
     *
     * @return  {@code true} if the user confirmed the simulated rename
     */
    public boolean isSimulateRename()
    {
        return isSimulateRename;
    }

}
