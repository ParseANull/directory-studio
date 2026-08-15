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

package org.apache.directory.studio.ldapbrowser.common.dialogs.preferences;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.core.model.schema.BinaryAttribute;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: AttributeDialog — LEIA PRESENTS DIPLOMATIC CREDENTIALS ─────────────
// Princess Leia, aboard the Tantive IV moments before capture, steps forward and
// calmly identifies herself by rank and authority — producing the exact credential
// document that defines who she is and what she represents.  This dialog does the
// same thing for LDAP attribute types: the user picks a specific attribute name or
// OID that establishes the identity of a binary attribute before we accept it.
// ──────────────────────────────────────────────────────────────────────────────
/**
 * A small modal dialog that lets the user enter or select an LDAP attribute type
 * (by name or numeric OID) for use in binary-attribute configuration.
 * Think of this class as Leia producing her diplomatic papers — we need to know
 * exactly which attribute we're dealing with before we proceed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeDialog extends Dialog
{
    /** The initial attribute. */
    private BinaryAttribute currentAttribute;

    /** The possible attribute types and OIDs. */
    private String[] attributeTypesAndOids;

    /** The selected attribute. */
    private BinaryAttribute returnAttribute;

    /** The combo. */
    private Combo typeOrOidCombo;

    /** The OK button of the dialog */
    private Button okButton;


    // ── LEIA BRIEFS HER PROTOCOL DROID ON THE COVER STORY ────────────────────────
    // Just before the Imperial boarding party arrives, Leia quickly hands C-3PO a
    // datapad listing all the diplomatic aliases she might need to claim.
    // She also specifies which identity she's currently using, so he doesn't
    // contradict her.  We do the same here: stash the current attribute and the
    // full list of valid names so the combo can offer sensible suggestions.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new AttributeDialog wired to a parent shell.
     * We pre-load the current attribute so the combo can show what's already
     * selected, and we take the full list of known attribute names/OIDs so the
     * user gets helpful autocomplete.
     *
     * <p>For example — Leia confirms her identity to the captain:</p>
     * <pre>
     *   currentAttribute  = "cn"          // what's already on file
     *   attributeNamesAndOids = ["cn", "sn", "2.5.4.3", ...]
     *   returnAttribute   = null           // nothing confirmed yet
     * </pre>
     *
     * @param parentShell          The Eclipse shell that owns this dialog — we
     *                             need it to position and modal-lock the window.
     * @param currentAttribute     The attribute that's already chosen, or null if
     *                             we're starting fresh.
     * @param attributeNamesAndOids All the names and OIDs the user can pick from —
     *                             the complete diplomatic registry, so to speak.
     */
    public AttributeDialog( Shell parentShell, BinaryAttribute currentAttribute, String[] attributeNamesAndOids )
    {
        super( parentShell );
        this.currentAttribute = currentAttribute;
        this.attributeTypesAndOids = attributeNamesAndOids;
        this.returnAttribute = null;
    }


    // ── LEIA ANNOUNCES HERSELF TO THE IMPERIAL CAPTAIN ───────────────────────────
    // Leia steps to the doorway and states her full title so the captain can log
    // the visitor correctly — "Senator Leia Organa of Alderaan."  We set the
    // dialog's title bar text here for the same reason: the user needs to know
    // what they're looking at before they start typing.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog window's title text.  Eclipse calls this before the
     * dialog becomes visible, so it's our chance to label the window clearly.
     * Without this, users would see a blank title bar — not helpful.
     *
     * <p>For example — Leia identifies the purpose of the meeting:</p>
     * <pre>
     *   newShell.setText("Select Attribute Type or OID");
     *   // like Leia saying: "I am here on diplomatic business."
     * </pre>
     *
     * @param newShell  The shell whose title we're setting — handed to us by
     *                  the Eclipse dialog framework.
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "AttributeDialog.SelectAttributeTypeOrOID" ) ); //$NON-NLS-1$
    }


    // ── IMPERIAL GUARDS TAKE THEIR POSITIONS ─────────────────────────────────────
    // The moment the boarding hatch opens, two stormtroopers flank each side —
    // one to proceed (OK), one to stand down (Cancel).  Neither is usable until
    // Leia's credentials are verified.  That's exactly what we wire up here:
    // two buttons, with OK disabled until the combo has real content.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the OK and Cancel buttons in the button bar area at the bottom of
     * the dialog.  We keep a reference to the OK button so we can enable or
     * disable it as the user types — an empty selection shouldn't be committable.
     *
     * <p>For example — guards standing by for Leia's signal:</p>
     * <pre>
     *   okButton = createButton(OK_ID, "OK", true);   // proceed
     *   createButton(CANCEL_ID, "Cancel", false);     // stand down
     *   validate();  // Leia hasn't spoken yet — OK stays disabled
     * </pre>
     *
     * @param parent  The composite that holds the button bar — provided by the
     *                Eclipse dialog framework.
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );

        validate();
    }


    // ── LEIA FORMALLY PRESENTS HER CREDENTIALS ───────────────────────────────────
    // Leia hands over the sealed document — that's the moment of commitment.
    // Whatever name or OID is showing in the combo right now becomes the official
    // BinaryAttribute we'll carry back to the preference page.  No going back
    // once she hands it over.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks OK.  We read whatever is in the combo, wrap it
     * in a new {@link BinaryAttribute}, and stash it so the caller can retrieve it
     * via {@link #getAttribute()}.  Then we let the superclass close the dialog.
     *
     * <p>For example — Leia commits to her cover story:</p>
     * <pre>
     *   returnAttribute = new BinaryAttribute("jpegPhoto");
     *   // the sealed credential is now in Imperial hands
     * </pre>
     */
    protected void okPressed()
    {
        returnAttribute = new BinaryAttribute( typeOrOidCombo.getText() );
        super.okPressed();
    }


    // ── LEIA LAYS HER PAPERS ON THE TABLE ────────────────────────────────────────
    // The captain gestures to a table and Leia spreads out her documents —
    // a label naming what they are, and the actual credential card the captain
    // can inspect (or replace with a different one from the registry).
    // Our combo works the same way: a label, then a dropdown pre-filled with the
    // current value so the user can review or change it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog's main content area — a two-column layout with a label
     * and a combo box where the user types or selects an attribute type or OID.
     * If an attribute was passed into the constructor we pre-fill the combo so
     * the user sees what's currently configured.
     *
     * <p>For example — Leia's papers spread on the table:</p>
     * <pre>
     *   label:  "Attribute Type or OID:"
     *   combo:  ["cn", "sn", "2.5.4.3", ...]  pre-selected: "cn"
     *   // inspector can change the selection or type a custom OID
     * </pre>
     *
     * @param parent  The parent composite provided by the Eclipse dialog framework.
     * @return        The outermost composite containing all our widgets.
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );

        Composite c = BaseWidgetUtils.createColumnContainer( composite, 2, 1 );
        BaseWidgetUtils.createLabel( c, Messages.getString( "AttributeDialog.AttributeTypeOrOID" ), 1 ); //$NON-NLS-1$
        typeOrOidCombo = BaseWidgetUtils.createCombo( c, attributeTypesAndOids, -1, 1 );
        if ( currentAttribute != null )
        {
            typeOrOidCombo.setText( currentAttribute.getAttributeNumericOidOrName() );
        }
        typeOrOidCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                validate();
            }
        } );

        return composite;
    }


    private void validate()
    {
        okButton.setEnabled( !"".equals( typeOrOidCombo.getText() ) ); //$NON-NLS-1$
    }


    // ── VADER'S AIDE RETRIEVES THE CONFIRMED CREDENTIAL ──────────────────────────
    // After Vader has reviewed Leia's papers, his aide picks them up and carries
    // them to the records office.  This method is that retrieval step — after the
    // dialog closes with OK, the caller asks for the committed BinaryAttribute and
    // we hand it over.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link BinaryAttribute} the user confirmed by clicking OK.
     * If the user cancelled, this returns null because we initialised
     * {@code returnAttribute} to null and only set it in {@link #okPressed()}.
     *
     * <p>For example — Vader's aide delivers the credential to records:</p>
     * <pre>
     *   BinaryAttribute attr = dialog.getAttribute();
     *   // attr.getAttributeNumericOidOrName() == "jpegPhoto"
     * </pre>
     *
     * @return  The selected {@link BinaryAttribute}, or null if the user cancelled.
     */
    public BinaryAttribute getAttribute()
    {
        return returnAttribute;
    }

}
