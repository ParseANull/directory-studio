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


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: DeleteDialog — LEIA'S HOLOGRAM ────────────────────────────────────
// Princess Leia records her desperate plea inside R2-D2 before the Empire can
// destroy the Death Star plans — one last chance to confirm a critical,
// irreversible action before everything is lost forever.
// Just like that hologram, this dialog fires one urgent "are you sure?" before
// we permanently erase LDAP entries that can never be recovered.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Confirmation dialog that appears when the user asks to delete one or more
 * entries from the LDAP browser tree.  Because deletion is permanent, we stop
 * and ask before proceeding — and optionally let the user choose whether to use
 * the LDAP Tree Delete Control (which deletes an entry together with all its
 * descendants in a single server-side operation, rather than one-by-one).
 * Think of this class as Leia's hologram: a short, urgent message that demands
 * a decision before something irreversible happens.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DeleteDialog extends MessageDialog
{

    /** The "Use Tree Delete Control" dialog setting . */
    private static final String USE_TREE_DELETE_CONTROL_DIALOGSETTING_KEY = DeleteDialog.class.getName()
        + ".useTreeDeleteControl"; //$NON-NLS-1$

    private Button useTreeDeleteControlCheckbox;

    private boolean askForTreeDeleteControl;

    private boolean useTreeDeleteControl;


    // ── LEIA LOADS HER PLEA INTO R2 ──────────────────────────────────────────
    // Leia is about to be captured; she records "Help me, Obi-Wan" with every
    // vital detail — her identity, the mission, and exactly what she needs done.
    // R2 stores the message ready to play the moment the right moment arrives.
    // We do the same: capture the window title, the warning text, and whether to
    // show the Tree Delete checkbox — everything needed to present the plea later.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Builds a new DeleteDialog, pre-loading all the text and options it needs
     * to present the confirmation prompt to the user.
     * We also read (or initialise) the persisted "use tree delete" preference so
     * the checkbox reflects whatever the user chose last time.
     *
     * <p>For example — Leia records her message before capture:</p>
     * <pre>
     *   leia.record("Help me, Obi-Wan Kenobi — you're my only hope.");
     *   r2d2.store(message, askForTreeDeleteControl=true);
     *   // R2 holds it until someone plugs him in and presses play.
     * </pre>
     *
     * @param parentShell              the shell that owns this dialog — we need it to position the window correctly
     * @param title                    the dialog window title, shown in the title bar
     * @param message                  the warning text displayed inside the dialog body
     * @param askForTreeDeleteControl  {@code true} if we should show the "Use Tree Delete Control" checkbox;
     *                                 pass {@code false} when tree-delete isn't relevant (e.g. single leaf entry)
     */
    public DeleteDialog( Shell parentShell, String title, String message, boolean askForTreeDeleteControl )
    {
        super( parentShell, title, null, message, QUESTION, new String[]
            { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, OK );

        this.askForTreeDeleteControl = askForTreeDeleteControl;
        this.useTreeDeleteControl = false;

        if ( BrowserCommonActivator.getDefault().getDialogSettings().get( USE_TREE_DELETE_CONTROL_DIALOGSETTING_KEY ) == null )
        {
            BrowserCommonActivator.getDefault().getDialogSettings().put( USE_TREE_DELETE_CONTROL_DIALOGSETTING_KEY,
                false );
        }
    }


    // ── R2 PROJECTS THE HOLOGRAM ─────────────────────────────────────────────
    // R2-D2 pops up and projects Leia's hologram into the room — the image
    // flickers into existence only when the conditions are right (someone is
    // watching and the situation calls for it).
    // We render the "Use Tree Delete Control" checkbox into the dialog's custom
    // area only when {@code askForTreeDeleteControl} is true; otherwise we show
    // nothing extra and keep the dialog clean.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Optionally adds a "Use Tree Delete Control" checkbox below the standard
     * message area.  The Tree Delete Control is an LDAP extension that tells the
     * server to delete an entire subtree atomically — handy when the entry has
     * children and the server supports it.  We only add the checkbox when the
     * caller said it's relevant.
     *
     * <p>For example — R2 projects only when the moment is right:</p>
     * <pre>
     *   if (r2.hasHologram() && audienceIsPresent) {
     *       r2.projectHologram();  // show Leia's message
     *   } else {
     *       return null;           // nothing to project
     *   }
     * </pre>
     *
     * @param parent  the composite that hosts the custom area — Eclipse hands us this
     * @return        the checkbox control if we created one, or {@code null} if there was nothing to add
     */
    @Override
    protected Control createCustomArea( Composite parent )
    {
        if ( askForTreeDeleteControl )
        {
            useTreeDeleteControlCheckbox = new Button( parent, SWT.CHECK );
            useTreeDeleteControlCheckbox.setText( Messages.getString( "DeleteDialog.UseTreeDeleteControl" ) ); //$NON-NLS-1$
            useTreeDeleteControlCheckbox.setSelection( BrowserCommonActivator.getDefault().getDialogSettings()
                .getBoolean( USE_TREE_DELETE_CONTROL_DIALOGSETTING_KEY ) );
            return useTreeDeleteControlCheckbox;
        }
        else
        {
            return null;
        }
    }


    // ── OBI-WAN RECEIVES THE MESSAGE AND CHOOSES ──────────────────────────────
    // Obi-Wan watches Leia's plea and then decides: help her, or don't — there
    // is no third option.  Whatever he decides, the galaxy feels the consequence.
    // When the user presses OK we read the checkbox and remember that choice for
    // next time; Cancel means we discard everything and nothing is deleted.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Reacts to the user pressing OK or Cancel.  On OK we capture the current
     * checkbox state and persist it to dialog settings so the user's preference
     * survives across sessions.  On Cancel we do nothing (the superclass handles
     * closing the dialog without any state change).
     *
     * <p>For example — Obi-Wan acts on the message:</p>
     * <pre>
     *   if (userChoice == OK) {
     *       useTreeDelete = checkbox.isChecked();
     *       persist(useTreeDelete);   // remember for next mission
     *   }
     *   // then hand control back to the base class
     * </pre>
     *
     * @param buttonId  the ID of the button the user clicked — {@link IDialogConstants#OK_ID} or
     *                  {@link IDialogConstants#CANCEL_ID}
     */
    @Override
    protected void buttonPressed( int buttonId )
    {
        if ( buttonId == OK )
        {
            useTreeDeleteControl = useTreeDeleteControlCheckbox != null && useTreeDeleteControlCheckbox.getSelection();

            if ( useTreeDeleteControlCheckbox != null )
            {
                BrowserCommonActivator.getDefault().getDialogSettings().put( USE_TREE_DELETE_CONTROL_DIALOGSETTING_KEY,
                    useTreeDeleteControlCheckbox.getSelection() );
            }
        }
        super.buttonPressed( buttonId );
    }


    // ── THE EMPIRE ASKS: HOW SHALL WE PROCEED? ───────────────────────────────
    // After Obi-Wan decides, the Rebels need to know the answer so they can
    // execute the plan — "did he agree to use the special maneuver or not?"
    // Callers need to know whether the user ticked the Tree Delete box so they
    // can pass the right LDAP control to the server.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the user opted in to the Tree Delete Control.  Call this
     * after the dialog closes with OK to decide how to build the LDAP delete
     * request.  Returns {@code false} if the user cancelled, never ticked the
     * box, or if the checkbox was never shown.
     *
     * <p>For example — Rebels check Obi-Wan's decision:</p>
     * <pre>
     *   if (dialog.isUseTreeDeleteControl()) {
     *       ldapRequest.addControl(new TreeDeleteControl());
     *   }
     * </pre>
     *
     * @return  {@code true} if the "Use Tree Delete Control" checkbox was checked when the user pressed OK
     */
    public boolean isUseTreeDeleteControl()
    {
        return useTreeDeleteControl;
    }

}
