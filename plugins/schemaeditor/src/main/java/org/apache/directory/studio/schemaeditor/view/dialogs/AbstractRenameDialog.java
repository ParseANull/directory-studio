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
package org.apache.directory.studio.schemaeditor.view.dialogs;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


// ── CLASS: AbstractRenameDialog — MACE WINDU CONFRONTING PALPATINE ────────────
// Mace Windu marches into Palpatine's office, ignites his purple lightsaber, and
// demands that Palpatine account for himself before the Senate is dissolved.
// The confrontation is all about validation — Palpatine must prove his legitimacy
// or face immediate consequences.
// Every rename operation has to go through the same kind of challenge: the new
// name must not already be claimed by someone else, or the dialog blocks the user
// from proceeding, red error strip blazing like Mace's blade.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Base dialog for renaming a schema artifact — a project or a schema.
 * It presents a single text field pre-filled with the current name, validates the new name
 * as the user types, and blocks the OK button if the name is already taken.
 * Think of this class as Mace Windu's confrontation: it demands that every proposed name
 * prove its uniqueness before we allow the rename to go through.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractRenameDialog extends Dialog
{
    /** The original name*/
    private String originalName;

    /** The new name */
    private String newName;

    // UI Fields
    private Text newNameText;
    private Composite errorComposite;
    private Image errorImage;
    private Label errorLabel;
    private Button okButton;


    // ── Mace Arrives Armed and Ready ─────────────────────────────────────────
    // Mace doesn't walk into Palpatine's office unarmed — he brings three other
    // Masters and knows exactly who he's there to confront. He has the original
    // situation (the current Chancellor) fixed in his mind as the baseline.
    // We record the original name so we can compare against it later, and so we
    // can pre-populate the text field. The new name starts as a copy of the original.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the rename dialog, recording the current name as the baseline.
     * We initialise {@code newName} to the same value as {@code originalName} so that
     * if the user clicks OK without typing anything, we hand back the original name —
     * a no-op rename is harmless.
     *
     * @param originalName  the current name of the item being renamed; shown pre-filled in the text field
     */
    public AbstractRenameDialog( String originalName )
    {
        super( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        this.originalName = originalName;
        this.newName = originalName;
    }


    // ── Mace Labels the Courtroom Door ───────────────────────────────────────
    // Before the confrontation begins, someone has to put the right sign on the
    // door so everyone knows this is an official proceeding — not an ambush.
    // We set the shell's title text so the user knows exactly what they're renaming.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the title bar text on the dialog's shell window.
     * JFace calls this before the window becomes visible, giving us one chance
     * to label it correctly. The concrete subclass can override again after our
     * call to {@code super} to provide a more specific title.
     *
     * @param newShell  the freshly created Shell JFace hands us to configure
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "RenameProjectDialog.Rename" ) ); //$NON-NLS-1$
    }


    // ── Mace Lays Out the Evidence on the Table ───────────────────────────────
    // Mace places his evidence on the table: Palpatine's old identity (the original name)
    // is written in one column, the space for the new declaration is left blank for
    // Palpatine to fill in. A red warning strip waits off to the side, ready to
    // light up the moment the name he offers is already on someone else's registry.
    // We build the text field (pre-filled with the original name), the error composite,
    // and wire a ModifyListener to run the uniqueness check on every keystroke.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog's content area: a labelled text field pre-filled with the original name
     * and a hidden error strip that appears when the proposed name is already taken.
     * The {@link ModifyListener} on the text field is the heart of the dialog — every keystroke
     * re-evaluates whether the OK button should be enabled.
     * Note that if the user types the same name back (case-insensitive match), we treat that as
     * valid and re-enable OK, since renaming something to its own name is effectively a no-op.
     *
     * @param parent  the parent composite supplied by JFace
     * @return        our assembled composite, handed back to JFace for embedding
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayout( new GridLayout( 2, false ) );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // New Name
        Label newNameLabel = new Label( composite, SWT.NONE );
        newNameLabel.setText( Messages.getString( "AbstractRenameDialog.NewName" ) ); //$NON-NLS-1$
        newNameText = new Text( composite, SWT.BORDER );
        newNameText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        newNameText.setText( originalName );
        newNameText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                // Getting the new name
                newName = newNameText.getText();

                if ( !newName.equalsIgnoreCase( originalName ) )
                {
                    // Checking if the new is already taken
                    boolean checkNewName = isNewNameAlreadyTaken();

                    // Enabling (or not) the ok button and showing (or not) the error composite
                    okButton.setEnabled( !checkNewName );
                    errorComposite.setVisible( checkNewName );
                }
                else
                {
                    // Enabling the ok button and showing the error composite
                    okButton.setEnabled( true );
                    errorComposite.setVisible( false );
                }
            }
        } );

        // Error Composite
        errorComposite = new Composite( composite, SWT.NONE );
        errorComposite.setLayout( new GridLayout( 2, false ) );
        errorComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );
        errorComposite.setVisible( false );

        // Error Image
        errorImage = PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_ERROR_TSK );
        Label label = new Label( errorComposite, SWT.NONE );
        label.setImage( errorImage );
        label.setSize( 16, 16 );

        // Error Label
        errorLabel = new Label( errorComposite, SWT.NONE );
        errorLabel.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        errorLabel.setText( getErrorMessage() );

        newNameText.setFocus();
        newNameText.selectAll();

        return composite;
    }


    // ── Mace Offers Only Two Options ─────────────────────────────────────────
    // Mace gives Palpatine exactly two choices: stand down and face trial (Cancel),
    // or accept the judgement (OK). There's no third button, no side door.
    // We build the button bar with Cancel first, then OK — and we put focus on OK
    // by passing {@code true} as the default flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the dialog's button bar with Cancel (left) and OK (right, default).
     * We hold on to a reference to the OK button so the {@link ModifyListener} in
     * {@link #createDialogArea} can enable or disable it as the user types.
     *
     * @param parent  the button bar composite JFace hands us
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
        okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
    }


    // ── Mace Reports the Verdict ─────────────────────────────────────────────
    // After the confrontation is over, Mace delivers the verdict to the Jedi
    // Council: here is what Palpatine's new identity will be (or isn't, if it was
    // blocked). The caller retrieves this to apply the actual rename operation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the new name the user typed, or the original name if they didn't change it.
     * Call this after the dialog closes with OK to get the value to apply.
     *
     * @return  the non-null name string from the text field at the time the user confirmed
     */
    public String getNewName()
    {
        return newName;
    }


    // ── Mace Delegates the Charge Sheet ──────────────────────────────────────
    // Mace knows there's a crime, but the specific charge sheet — "this project name
    // already exists" versus "this schema name already exists" — has to come from the
    // arresting officer (the concrete subclass) who knows which registry was violated.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised error message to show when the proposed new name is already taken.
     * We leave this abstract so each concrete subclass (project rename, schema rename) can
     * provide the right wording for its context.
     *
     * @return  a non-null localised error string describing the naming conflict
     */
    protected abstract String getErrorMessage();


    // ── Mace Checks the Senate Registry ──────────────────────────────────────
    // Mace doesn't make up rules on the spot — he checks whether the identity
    // Palpatine is claiming is already on the Senate's official registry.
    // Only the concrete subclass knows which registry to look in: the projects
    // handler, the schema handler, etc.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the name currently in {@link #getNewName()} is already claimed by
     * another item of the same kind (project, schema, etc.).
     * We delegate to the concrete subclass because the look-up mechanism differs per type.
     *
     * @return  {@code true} if the name is already taken and the rename should be blocked,
     *          {@code false} if the name is available
     */
    protected abstract boolean isNewNameAlreadyTaken();
}
