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
package org.apache.directory.studio.openldap.common.ui.dialogs;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: AttributeDialog — JEDI COUNCIL CHOOSING A FORCE DISCIPLINE ────────
// Picture the Jedi Council chamber where a young Padawan stands in the center
// and the Council selects which Force discipline they will train in. This dialog
// presents every attribute type available in the schema and lets the user pick
// (or type) the one they need. The OK button stays disabled until a valid
// choice is made — the Council will not accept an empty answer.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We present a combo-box dialog that lets the user select or type an LDAP
 * attribute type name. We populate the combo from the schema of the supplied
 * browser connection, sort the names alphabetically, and disable OK until the
 * field is non-empty. Call {@link #getAttribute()} after the dialog closes with
 * OK to retrieve the chosen value.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeDialog extends Dialog
{
    /** The possible attribute types */
    private String[] attributeTypes;

    /** The return attribute */
    private String returnAttribute;

    // UI widgets
    private Button okButton;
    private Combo combo;


    // ── CONSTRUCTOR: AttributeDialog(Shell, IBrowserConnection) ───────────────
    // The Council chamber opens with no pre-selected discipline — the Padawan
    // starts fresh. We extract all attribute names from the schema and sort
    // them so the list is easy to scan. The initial return value is null until
    // a selection is confirmed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create an AttributeDialog with no pre-selected attribute. We read all
     * attribute type names from the schema of {@code browserConnection} and
     * populate the combo in sorted order.
     *
     * @param parentShell        the parent shell
     * @param browserConnection  the connection whose schema provides the list of attribute types
     */
    public AttributeDialog( Shell parentShell, IBrowserConnection browserConnection )
    {
        super( parentShell );
        init( browserConnection, null );
    }


    // ── CONSTRUCTOR: AttributeDialog(Shell, IBrowserConnection, String) ───────
    // The Council chamber opens with a discipline already highlighted — perhaps
    // the user is editing an existing attribute rather than choosing from scratch.
    // We pre-populate the combo with the supplied attribute value.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create an AttributeDialog pre-seeded with an existing attribute value.
     * The combo will show {@code attribute} as the initial text so the user can
     * confirm or change it.
     *
     * @param parentShell        the parent shell
     * @param browserConnection  the connection whose schema provides the list of attribute types
     * @param attribute          the attribute name to pre-populate in the combo
     */
    public AttributeDialog( Shell parentShell, IBrowserConnection browserConnection, String attribute )
    {
        super( parentShell );
        init( browserConnection, attribute );
    }


    // ── METHOD: init — ASSEMBLING THE COUNCIL ROSTER ─────────────────────────
    // Before the chamber doors open we compile the list of available disciplines
    // by reading every attribute type descriptor from the schema. We sort them
    // alphabetically so Masters and Padawans alike can find what they need
    // quickly, then remember any pre-selected value for the combo.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We initialize our attribute-type list and remember the initial attribute
     * value. We read every {@link AttributeType} from the connection schema,
     * collect all name aliases, sort them alphabetically, and convert the result
     * to the String array that the combo will display.
     *
     * @param browserConnection  the connection providing the schema (may be null)
     * @param attribute          the pre-selected attribute name (may be null)
     */
    private void init( IBrowserConnection browserConnection, String attribute )
    {
        List<String> attributeTypes = new ArrayList<String>();

        if ( browserConnection != null )
        {
            Collection<AttributeType> atds = browserConnection.getSchema().getAttributeTypeDescriptions();

            for ( AttributeType atd : atds )
            {
                for ( String name : atd.getNames() )
                {
                    attributeTypes.add( name );
                }
            }

            Collections.sort( attributeTypes );
        }

        this.attributeTypes = attributeTypes.toArray( new String[attributeTypes.size()] );
        returnAttribute = attribute;
    }


    // ── METHOD: configureShell — SETTING THE CHAMBER TITLE PLATE ─────────────
    // Before the Council doors open we inscribe the purpose of this gathering
    // on the title plate above the entrance so all who enter know what we are
    // here to decide.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( "Select Attribute Type" );
    }


    // ── METHOD: createButtonsForButtonBar — POSITIONING THE COUNCIL VOTE ──────
    // We place the OK and Cancel voting stones in the button bar. The OK stone
    // starts active only if there is already a valid selection — we call
    // validate() immediately after creation to set the right initial state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );

        validate();
    }


    // ── METHOD: okPressed — RECORDING THE COUNCIL'S DECISION ─────────────────
    // The Council has spoken: we capture the current combo text as the chosen
    // attribute, then let the standard dialog close sequence carry on. The
    // caller can retrieve the result via getAttribute().
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected void okPressed()
    {
        returnAttribute = combo.getText();
        super.okPressed();
    }


    // ── METHOD: createDialogArea — BUILDING THE COUNCIL CHAMBER ──────────────
    // We construct the interior of the dialog: a two-column row with an
    // "Attribute Type:" label on the left and the sortable combo on the right.
    // A modify listener watches every keystroke so the OK button stays in sync
    // with whether the field is empty or not.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );

        Composite c = BaseWidgetUtils.createColumnContainer( composite, 2, 1 );
        BaseWidgetUtils.createLabel( c, "Attribute Type:", 1 );
        combo = BaseWidgetUtils.createCombo( c, attributeTypes, -1, 1 );

        if ( returnAttribute != null )
        {
            combo.setText( returnAttribute );
        }

        combo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                validate();
            }
        } );

        return composite;
    }


    // ── METHOD: validate — CHECKING THE COUNCIL'S QUORUM ─────────────────────
    // A decision cannot be made on empty air: we check whether the combo
    // contains any text and enable or disable the OK button accordingly.
    // The Council will not let an empty selection through.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We enable the OK button only when the combo contains a non-empty string.
     * This prevents the user from accidentally confirming without choosing an
     * attribute type.
     */
    private void validate()
    {
        okButton.setEnabled( !"".equals( combo.getText() ) ); //$NON-NLS-1$
    }


    // ── METHOD: getAttribute — RECEIVING THE COUNCIL'S VERDICT ───────────────
    // After the dialog closes with OK, we hand the caller the attribute name
    // that was chosen or typed in the combo. If the dialog was cancelled the
    // value reflects whatever was passed at construction time (possibly null).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the attribute type name that was selected or typed in the
     * dialog. This value is updated when the user presses OK; it reflects the
     * constructor argument (possibly {@code null}) if the dialog was cancelled.
     *
     * @return the chosen attribute name, or {@code null} if none was set
     */
    public String getAttribute()
    {
        return returnAttribute;
    }
}
