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


import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.core.model.schema.AttributeValueEditorRelation;
import org.apache.directory.studio.valueeditors.ValueEditorManager.ValueEditorExtension;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: AttributeValueEditorDialog — C-3PO SELECTS THE RIGHT PROTOCOL ─────
// C-3PO stands at the Rebel comms console on Yavin 4, confronted with an incoming
// message in an unfamiliar dialect.  He cycles through his registry of six million
// communication forms, pairs the dialect identifier (the attribute type or OID)
// with exactly the right translation module (the value editor), and locks in the
// selection.  That's precisely what this dialog does: it lets the user bind a
// specific LDAP attribute to the value editor plugin best equipped to display it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A modal dialog for creating or editing a pairing between an LDAP attribute
 * type (or OID) and the value editor plugin that should handle it.
 * Think of this class as C-3PO's protocol selection screen — pick the dialect,
 * pick the translator, confirm.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeValueEditorDialog extends Dialog
{
    /** The initial attribute to value editor relation. */
    private AttributeValueEditorRelation relation;

    /** Map with class name => value editor extension. */
    private SortedMap<String, ValueEditorExtension> class2ValueEditorExtensionMap;

    /** The attribute types and OIDs. */
    private String[] attributeTypesAndOids;

    /** Map with value editor names => class name. */
    private SortedMap<String, String> veName2classMap;

    /** The selected attribute to value editor relation. */
    private AttributeValueEditorRelation returnRelation;

    /** The type or OID combo. */
    private Combo typeOrOidCombo;

    /** The value editor combo. */
    private Combo valueEditorCombo;

    /** The OK button of the dialog */
    private Button okButton;


    // ── C-3PO LOADS HIS COMMUNICATION REGISTRY ───────────────────────────────────
    // C-3PO powers up at the console, loads the full registry of known languages
    // (the value editor map), and pre-selects the current dialect pair so the
    // operator can review what's already configured before changing anything.
    // He builds a reverse index — editor name to class name — so the combo can
    // show friendly names while we store fully-qualified class names underneath.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the dialog pre-loaded with the existing attribute-to-editor
     * relation (if any), the full map of available value editor plugins, and the
     * list of known attribute types and OIDs to offer in the first combo.
     * We also build an inverted name-to-class map so the UI can show friendly
     * names while we track class names internally.
     *
     * <p>For example — C-3PO initialises at the console:</p>
     * <pre>
     *   relation            = (cn, TextValueEditor)  // current pairing
     *   class2EditorMap     = { "TextValueEditor" -> extension, ... }
     *   veName2classMap     = { "Text Editor" -> "TextValueEditor", ... }
     *   returnRelation      = null  // nothing confirmed yet
     * </pre>
     *
     * @param parentShell                   The shell that owns this dialog.
     * @param relation                      The existing attribute-to-editor pair to
     *                                      pre-fill, or null if we're adding a new one.
     * @param class2ValueEditorExtensionMap Map from class name to value editor
     *                                      extension metadata — our plugin registry.
     * @param attributeTypesAndOids         All attribute names and OIDs the user can
     *                                      choose from in the first combo.
     */
    public AttributeValueEditorDialog( Shell parentShell, AttributeValueEditorRelation relation,
        SortedMap<String, ValueEditorExtension> class2ValueEditorExtensionMap, String[] attributeTypesAndOids )
    {
        super( parentShell );
        this.relation = relation;
        this.class2ValueEditorExtensionMap = class2ValueEditorExtensionMap;
        this.attributeTypesAndOids = attributeTypesAndOids;
        this.returnRelation = null;

        this.veName2classMap = new TreeMap<String, String>();
        for ( ValueEditorExtension vee : class2ValueEditorExtensionMap.values() )
        {
            veName2classMap.put( vee.name, vee.className );
        }
    }


    // ── C-3PO ANNOUNCES THE NAME OF THE TRANSLATION SESSION ──────────────────────
    // C-3PO straightens up and announces the purpose of the current operation to
    // the room: "Attribute Value Editor configuration."  It's just a title bar,
    // but clarity matters — especially when you're dealing with six million forms.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog's title bar text before it becomes visible.
     * Without a title the window is ambiguous, especially when multiple dialogs
     * are open at once.
     *
     * @param newShell  The shell provided by the Eclipse dialog framework.
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "AttributeValueEditorDialog.AttributeValueEditor" ) ); //$NON-NLS-1$
    }


    // ── C-3PO READIES THE CONFIRM AND ABORT CONTROLS ─────────────────────────────
    // C-3PO primes two controls: one to lock in the selected pairing (OK), one to
    // discard it (Cancel).  Neither should be usable until both combos have a
    // selection — you can't confirm a translation without both dialect and module.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the OK and Cancel buttons and immediately calls {@link #validate()}
     * so OK starts disabled until the user has chosen both an attribute type and
     * a value editor.  An incomplete pair isn't useful.
     *
     * @param parent  The button bar composite provided by Eclipse.
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );

        validate();
    }


    // ── C-3PO LOCKS IN THE DIALECT-TO-MODULE PAIRING ─────────────────────────────
    // The operator confirms the selection — C-3PO writes the pairing to his active
    // translation table.  From this point on, incoming messages tagged with that
    // dialect will be routed to the specified module automatically.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks OK.  We read the attribute type from the first
     * combo and look up the class name for the chosen editor name from our
     * {@code veName2classMap}, then wrap them into a new
     * {@link AttributeValueEditorRelation} for the caller to retrieve.
     *
     * <p>For example — C-3PO commits the pairing:</p>
     * <pre>
     *   attribute = "jpegPhoto"
     *   editorClass = "org.apache.directory.studio.valueeditors.image.InPlaceImageValueEditor"
     *   returnRelation = new AttributeValueEditorRelation(attribute, editorClass)
     * </pre>
     */
    protected void okPressed()
    {
        returnRelation = new AttributeValueEditorRelation( typeOrOidCombo.getText(), veName2classMap
            .get( valueEditorCombo.getText() ) );
        super.okPressed();
    }


    // ── C-3PO LAYS OUT THE TWO-STEP SELECTION INTERFACE ──────────────────────────
    // C-3PO projects two drop-down menus side by side: the first lists all known
    // dialects (attribute types and OIDs), the second lists all available
    // translation modules (value editors).  Pre-selecting current values makes it
    // easy for the operator to review without having to scroll from scratch.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog's content area with two labeled combo boxes: one for the
     * attribute type or OID, one for the value editor.  If we were given an
     * existing relation to edit, we pre-fill both combos from it.  Both combos
     * have modify listeners that recheck whether OK should be enabled.
     *
     * <p>For example — C-3PO's selection console:</p>
     * <pre>
     *   Attribute Type or OID:  [cn ▼]
     *   Value Editor:           [Text Editor ▼]
     * </pre>
     *
     * @param parent  The parent composite provided by Eclipse.
     * @return        The composite containing all our widgets.
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );

        Composite c = BaseWidgetUtils.createColumnContainer( composite, 2, 1 );
        BaseWidgetUtils.createLabel( c, Messages.getString( "AttributeValueEditorDialog.AttributeTypeOrOID" ), 1 ); //$NON-NLS-1$
        typeOrOidCombo = BaseWidgetUtils.createCombo( c, attributeTypesAndOids, -1, 1 );
        if ( relation != null && relation.getAttributeNumericOidOrType() != null )
        {
            typeOrOidCombo.setText( relation.getAttributeNumericOidOrType() );
        }
        typeOrOidCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                validate();
            }
        } );

        BaseWidgetUtils.createLabel( c, Messages.getString( "AttributeValueEditorDialog.ValueEditor" ), 1 ); //$NON-NLS-1$
        valueEditorCombo = BaseWidgetUtils.createReadonlyCombo( c, veName2classMap.keySet().toArray( new String[0] ),
            -1, 1 );
        if ( relation != null && relation.getValueEditorClassName() != null
            && class2ValueEditorExtensionMap.containsKey( relation.getValueEditorClassName() ) )
        {
            valueEditorCombo.setText( ( class2ValueEditorExtensionMap.get( relation.getValueEditorClassName() ) ).name );
        }
        valueEditorCombo.addModifyListener( new ModifyListener()
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
        okButton.setEnabled(
            !"".equals( valueEditorCombo.getText() ) && !"".equals( typeOrOidCombo.getText() ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── C-3PO HANDS OVER THE CONFIRMED PAIRING ───────────────────────────────────
    // The operator asks C-3PO: "What did we end up with?"  He retrieves the
    // committed dialect-to-module pairing from his short-term buffer and hands it
    // back.  If the operator cancelled, there is nothing to hand back — null.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link AttributeValueEditorRelation} committed when the user
     * clicked OK.  Returns null if the dialog was cancelled, because we only
     * populate {@code returnRelation} inside {@link #okPressed()}.
     *
     * <p>For example — C-3PO delivers the result:</p>
     * <pre>
     *   relation.getAttributeNumericOidOrType() == "jpegPhoto"
     *   relation.getValueEditorClassName()       == "InPlaceImageValueEditor"
     * </pre>
     *
     * @return  The confirmed attribute-to-editor relation, or null if cancelled.
     */
    public AttributeValueEditorRelation getRelation()
    {
        return returnRelation;
    }

}
