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

package org.apache.directory.studio.common.ui.dialogs;


import org.apache.directory.studio.common.ui.AddEditDialog;
import org.apache.directory.studio.common.ui.Messages;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.wrappers.StringValueWrapper;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: ObjectClassDialog — JEDI COUNCIL SELECTING AN OBJECT CLASS ────────
// Like the Jedi Council choosing which Order a Padawan belongs to, this dialog
// presents a drop-down of known object class names and OIDs and asks the user
// to pick one.  The Council refuses to close the session until a real
// selection has been made — the OK button stays dark until the combo is
// non-empty.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We present a dialog that lets the user choose an object class name or OID.
 * The combo is pre-loaded with every known object class so the user can select
 * one quickly or type a custom value.  The OK button is disabled until the
 * field is non-empty to prevent invalid submissions.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ObjectClassDialog extends AddEditDialog<StringValueWrapper>
{
    /** The possible objectClasses and OIDs. */
    private String[] objectClassesAndOids;

    /** The combo containing the list of objectClasses. */
    private Combo objectClassOrOidCombo;


    // ── CONSTRUCTOR ObjectClassDialog — OPENING THE COUNCIL CHAMBER ───────────
    // The Council chamber opens for the object-class selection session.  We
    // wire the dialog to its parent shell here; the real UI comes together when
    // createDialogArea is invoked.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a new ObjectClassDialog and attach it to the given parent shell.
     * The combo contents must be provided separately via
     * {@link #setAttributeNamesAndOids} before opening.
     *
     * @param parentShell the shell that will own this dialog
     */
    public ObjectClassDialog( Shell parentShell )
    {
        super( parentShell );
    }


    // ── METHOD setAttributeNamesAndOids — LOADING THE COUNCIL'S ARCHIVE ───────
    // Before the session starts, we load the Council's archive of all known
    // object classes and OIDs into the combo so the user has a complete list
    // to choose from during deliberation.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We store the array of object class names and OIDs that will populate
     * the combo box.  Call this before opening the dialog so the user has a
     * complete list to choose from.
     *
     * @param objectClassesAndOids the list of available object class names and OIDs
     */
    public void setAttributeNamesAndOids( String[] objectClassesAndOids )
    {
        this.objectClassesAndOids = objectClassesAndOids;
    }


    // ── METHOD configureShell — NAMING THE COUNCIL CHAMBER ───────────────────
    // We put the nameplate on the Council chamber door so the user knows this
    // session is specifically about selecting an object class.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We set the dialog window title to the localized "Select ObjectClass or OID"
     * string so the user immediately understands what they are being asked
     * to choose.
     *
     * @param newShell the shell to configure
     * {@inheritDoc}
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "ObjectClassDialog.SelectObjectClassOrOID" ) ); //$NON-NLS-1$
    }


    // ── METHOD okPressed — THE COUNCIL RENDERS ITS VERDICT ───────────────────
    // The Council has chosen an object class.  We wrap the selected text in a
    // StringValueWrapper and store it so the caller can retrieve the chosen
    // value once the dialog has closed.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We capture the combo's current text as a {@link StringValueWrapper} and
     * store it via {@link #setEditedElement}.  Then we delegate to the parent
     * to close the dialog in the standard OK manner.
     * {@inheritDoc}
     */
    protected void okPressed()
    {
        setEditedElement( new StringValueWrapper( objectClassOrOidCombo.getText(), false ) );
        super.okPressed();
    }


    // ── METHOD createButton — GUARDING THE COUNCIL'S CONFIRMATION LEVER ───────
    // The Council will not pull the confirmation lever without a real choice.
    // We disable the OK button immediately after creation when the current
    // edited element is empty, and let the modify listener re-enable it.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We override button creation so we can immediately disable the OK button
     * when the current edited element has no value.  This prevents an empty
     * object class from being submitted.
     *
     * @param parent        the composite that holds the button
     * @param id            the button's dialog constant ID
     * @param label         the button label text
     * @param defaultButton whether this is the default (Enter) button
     * @return the created button
     * {@inheritDoc}
     */
    protected Button createButton( Composite parent, int id, String label, boolean defaultButton )
    {
        Button button = super.createButton(parent, id, label, defaultButton);

        if ( id == IDialogConstants.OK_ID )
        {
            String objectClass = ((StringValueWrapper)getEditedElement() ).getValue();

            if ( ( objectClass == null ) || ( objectClass.length() == 0 ) )
            {
                button.setEnabled( false );
            }
        }

        return button;
    }


    // ── METHOD createDialogArea — ASSEMBLING THE COUNCIL CHAMBER ─────────────
    // We lay out the Council chamber: a descriptive label on the left and the
    // object-class combo on the right.  If an element is already being edited
    // we pre-fill the combo so the user can see and revise the current value.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We build the dialog content area with a two-column layout containing
     * a label and the object-class combo.  If an element is being edited, we
     * pre-fill the combo with its current value.  A modify listener keeps the
     * OK button in sync with the combo's content.
     *
     * <pre>
     * .---------------------------------------------------------.
     * |X| Select ObjectClass or OID                             |
     * +---------------------------------------------------------|
     * | ObjectClass or OID : [                              |v] |
     * |                                                         |
     * |                                      (CANCEL)  (  OK  ) |
     * '---------------------------------------------------------'
     * </pre>
     *
     * @param parent the parent composite provided by the dialog framework
     * @return the top-level control for the dialog's content area
     * {@inheritDoc}
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );

        Composite c = BaseWidgetUtils.createColumnContainer( composite, 2, 1 );
        BaseWidgetUtils.createLabel( c, Messages.getString( "ObjectClassDialog.ObjectClassOrOID" ), 1 ); //$NON-NLS-1$
        objectClassOrOidCombo = BaseWidgetUtils.createCombo( c, objectClassesAndOids, -1, 1 );

        if ( getEditedElement() != null )
        {
            objectClassOrOidCombo.setText( getEditedElement().getValue() );
        }

        objectClassOrOidCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                validate();
            }
        } );

        initDialog();

        return composite;
    }


    // ── METHOD initDialog — READING THE PRE-SESSION BRIEF ────────────────────
    // The Council's pre-session brief is empty by default here.  Subclasses
    // can override this to run any additional setup after the combo is ready.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We provide a no-op default implementation.  Subclasses may override this
     * to perform additional initialization after the base controls are built.
     * {@inheritDoc}
     */
    protected void initDialog()
    {
        // Nothing to do
    }


    // ── METHOD validate — CHECKING THE COUNCIL'S SELECTION STATUS ────────────
    // The Council checks whether the combo has a value before allowing the
    // session to conclude.  We enable or disable OK based on whether the
    // field is currently empty.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We enable the OK button when the combo contains a non-empty string, and
     * disable it otherwise.  Called every time the combo text changes so the
     * button state always matches the current input.
     */
    private void validate()
    {
        Button okButton = getButton( IDialogConstants.OK_ID );

        // This button might be null when the dialog is called.
        if ( okButton == null )
        {
            return;
        }

        okButton.setEnabled( !"".equals( objectClassOrOidCombo.getText() ) ); //$NON-NLS-1$
    }


    // ── METHOD addNewElement — PREPARING A BLANK ENROLLMENT FORM ─────────────
    // The Council prepares a blank enrollment form when no object class has
    // been chosen yet.  We set the edited element to an empty wrapper so the
    // dialog starts with a clean, blank combo.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We initialize the edited element to an empty {@link StringValueWrapper}
     * so the dialog starts with a blank combo field.  This is called when the
     * user clicks Add rather than Edit in the parent table.
     * {@inheritDoc}
     */
    @Override
    public void addNewElement()
    {
        // Default to none
        setEditedElement( new StringValueWrapper( "", false  ) );
    }
}
