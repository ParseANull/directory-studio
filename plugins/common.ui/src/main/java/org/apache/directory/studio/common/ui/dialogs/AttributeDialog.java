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


// ── CLASS: AttributeDialog — JEDI COUNCIL SELECTING A MISSION ATTRIBUTE ──────
// The Jedi Council convenes to choose the right attribute for the mission at
// hand.  They consult the holocron (a drop-down combo) which lists every known
// attribute type and OID, and the Council refuses to sanction the mission until
// a valid attribute has been selected — the OK button stays dark until a real
// value is chosen.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We present a simple dialog that lets the user pick or type an attribute type
 * name or OID.  The dialog pre-populates the combo with every known attribute
 * type so the user can select one quickly, and we keep the OK button disabled
 * until the field is non-empty to prevent empty submissions.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeDialog extends AddEditDialog<StringValueWrapper>
{
    /** The possible attribute types and OIDs. */
    private String[] attributeTypesAndOids;

    /** The combo containing the list of attributes. */
    private Combo typeOrOidCombo;


    // ── CONSTRUCTOR AttributeDialog — OPENING THE COUNCIL CHAMBER ────────────
    // The Council chamber opens its doors when we receive the parent shell.
    // We simply wire the dialog to its parent — the real setup happens when
    // createDialogArea is called later.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a new AttributeDialog and attach it to the given parent shell.
     * The combo contents must be provided separately via
     * {@link #setAttributeNamesAndOids} before the dialog is opened.
     *
     * @param parentShell the shell that will own this dialog
     */
    public AttributeDialog( Shell parentShell )
    {
        super( parentShell );
    }


    // ── METHOD setAttributeNamesAndOids — LOADING THE COUNCIL'S HOLOCRON ─────
    // Before the Council session, we load the holocron with the full list of
    // known attribute names and OIDs.  This is the data the combo will display
    // so the user can pick from a known set or type a custom value.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We store the array of attribute type names and OIDs that will populate
     * the combo box.  Call this before opening the dialog so the user has a
     * complete list to choose from.
     *
     * @param attributeNamesAndOids the list of available attribute names and OIDs
     */
    public void setAttributeNamesAndOids( String[] attributeNamesAndOids )
    {
        this.attributeTypesAndOids = attributeNamesAndOids;
    }


    // ── METHOD configureShell — NAMING THE COUNCIL CHAMBER ───────────────────
    // Every Council chamber has a nameplate above the door.  We set the shell's
    // title text so the user knows exactly what they are selecting here.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We set the dialog window title to the localized "Select Attribute Type
     * or OID" string so the user immediately understands what they are doing.
     *
     * @param newShell the shell to configure
     * {@inheritDoc}
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "AttributeDialog.SelectAttributeTypeOrOID" ) ); //$NON-NLS-1$
    }


    // ── METHOD okPressed — THE COUNCIL RENDERS ITS VERDICT ───────────────────
    // The Council has deliberated and chosen an attribute.  We wrap the selected
    // text in a StringValueWrapper and store it as the edited element so the
    // caller can retrieve it after the dialog closes.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We capture the combo's current text as a {@link StringValueWrapper} and
     * store it via {@link #setEditedElement} so the caller can retrieve the
     * chosen attribute after the dialog closes.  Then we delegate to the parent
     * to perform the standard OK-close sequence.
     * {@inheritDoc}
     */
    protected void okPressed()
    {
        setEditedElement( new StringValueWrapper( typeOrOidCombo.getText(), false ) );
        super.okPressed();
    }


    // ── METHOD createButton — GUARDING THE COUNCIL'S CONFIRMATION LEVER ──────
    // The Council will not pull the confirmation lever until they have actually
    // chosen an attribute.  We disable the OK button right after it is created
    // if the edited element has an empty value, and rely on the modify listener
    // to re-enable it once the user types something valid.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We override button creation so we can immediately disable the OK button
     * when the current edited element has no value.  This prevents the user
     * from confirming without first entering an attribute type or OID.
     *
     * @param parent        the composite that holds the button
     * @param id            the button's dialog constant ID
     * @param label         the button label text
     * @param defaultButton whether this is the default (Enter) button
     * @return the created button
     * {@inheritDoc}
     */
    protected Button createButton(Composite parent, int id, String label, boolean defaultButton)
    {
        Button button = super.createButton(parent, id, label, defaultButton);

        if ( id == IDialogConstants.OK_ID )
        {
            String attribute = ((StringValueWrapper)getEditedElement() ).getValue();

            if ( ( attribute == null ) || ( attribute.length() == 0 ) )
            {
                button.setEnabled( false );
            }
        }

        return button;
    }


    // ── METHOD createDialogArea — ASSEMBLING THE COUNCIL CHAMBER ─────────────
    // We arrange the Council chamber: a label naming what to choose on the left,
    // and the attribute-type combo on the right.  If an element was already
    // being edited, we pre-fill the combo with its current value so the user
    // can see and adjust the existing selection.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We build the dialog's content area with a two-column layout: a label on
     * the left and the attribute-type combo on the right.  If an element is
     * already being edited, we pre-fill the combo with its current value.  We
     * also attach a modify listener that enables or disables OK as the user
     * types.
     *
     * <pre>
     * .---------------------------------------------------------.
     * |X| Select Attribute Type or OID                          |
     * +---------------------------------------------------------|
     * | Attribute Type or OID : [                           |v] |
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
        BaseWidgetUtils.createLabel( c, Messages.getString( "AttributeDialog.AttributeTypeOrOID" ), 1 ); //$NON-NLS-1$
        typeOrOidCombo = BaseWidgetUtils.createCombo( c, attributeTypesAndOids, -1, 1 );

        if ( getEditedElement() != null )
        {
            typeOrOidCombo.setText( getEditedElement().getValue() );
        }

        typeOrOidCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                validate();
            }
        } );

        initDialog();

        return composite;
    }


    // ── METHOD initDialog — READING THE PRE-MISSION BRIEF ────────────────────
    // The Council's pre-mission brief is empty here — subclasses can override
    // this to run any additional setup after the combo has been built and
    // populated.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We provide a no-op default implementation.  Subclasses may override this
     * to perform additional initialization of the dialog area after the base
     * controls have been constructed.
     * {@inheritDoc}
     */
    protected void initDialog()
    {
        // Nothing to do
    }


    // ── METHOD validate — CHECKING THE COUNCIL'S DECISION ────────────────────
    // The Council checks whether a real attribute has been typed before they
    // allow the session to close.  We enable OK as soon as the combo is
    // non-empty, and disable it again the moment it becomes blank.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We enable the OK button when the combo contains a non-empty string, and
     * disable it otherwise.  This is called every time the combo text changes
     * so the button state always reflects the current input.
     */
    private void validate()
    {
        Button okButton = getButton( IDialogConstants.OK_ID );

        // This button might be null when the dialog is called.
        if ( okButton == null )
        {
            return;
        }

        okButton.setEnabled( !"".equals( typeOrOidCombo.getText() ) ); //$NON-NLS-1$
    }


    // ── METHOD addNewElement — PREPARING A BLANK MISSION FORM ────────────────
    // The Council hands the user a blank form to fill in when no attribute has
    // been selected yet.  We initialize the edited element to an empty
    // StringValueWrapper so the dialog starts in a clean state.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We initialize the edited element to an empty {@link StringValueWrapper}
     * so the dialog starts with a blank combo.  This is called when the user
     * clicks Add in the table rather than Edit.
     * {@inheritDoc}
     */
    @Override
    public void addNewElement()
    {
        // Default to none
        setEditedElement( new StringValueWrapper( "", false  ) );
    }
}
