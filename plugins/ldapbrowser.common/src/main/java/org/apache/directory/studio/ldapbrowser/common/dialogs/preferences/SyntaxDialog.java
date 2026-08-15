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
import org.apache.directory.studio.ldapbrowser.core.model.schema.BinarySyntax;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: SyntaxDialog — THE JEDI COUNCIL REVIEWS A CANDIDATE'S TYPE ────────
// In the Jedi Temple on Coruscant, Mace Windu and the Council gather to assess
// Anakin Skywalker.  They don't evaluate him by name — they examine his
// midi-chlorian count, a precise numeric OID that classifies his Force affinity.
// That OID either matches a known category in the Council's registry or it's an
// unprecedented value they must investigate.  This dialog works the same way:
// the user supplies a syntax OID — a numeric identifier like "1.3.6.1.4.1.1466.115.121.1.5"
// — and we verify it's non-empty before accepting it as a binary-syntax entry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A small modal dialog that lets the user enter or select a syntax OID for use
 * in the binary-syntaxes configuration.
 * Think of this class as the Jedi Council's assessment session — pick the
 * correct numeric identifier that categorises how LDAP values of a given type
 * are encoded.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SyntaxDialog extends Dialog
{
    /** The initial syntax. */
    private BinarySyntax currentSyntax;

    /** The possible syntax OIDs. */
    private String[] syntaxOids;

    /** The selected syntax. */
    private BinarySyntax returnSyntax;

    /** The combo. */
    private Combo oidCombo;

    /** The OK button of the dialog */
    private Button okButton;


    // ── THE COUNCIL PREPARES ANAKIN'S ASSESSMENT FILE ────────────────────────────
    // Before the council session, a Temple archivist pulls Anakin's existing file
    // (currentSyntax) and lays out the full registry of known midi-chlorian
    // classifications (syntaxOids) for reference.  If there's no prior file the
    // session starts from scratch.  returnSyntax stays null until the council
    // commits to a verdict.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the dialog pre-loaded with the existing syntax (if any) and the
     * list of all known OIDs to offer in the combo.
     *
     * <p>For example — the archivist prepares the file:</p>
     * <pre>
     *   currentSyntax = BinarySyntax("1.3.6.1.4.1.1466.115.121.1.5")
     *   syntaxOids    = ["1.3.6.1.4.1.1466.115.121.1.5", "1.3.6.1.4.1.1466.115.121.1.28", ...]
     *   returnSyntax  = null  // verdict not yet reached
     * </pre>
     *
     * @param parentShell   The shell that owns this dialog.
     * @param currentSyntax The syntax already on file, or null for a fresh entry.
     * @param syntaxOids    All syntax OIDs available for selection.
     */
    public SyntaxDialog( Shell parentShell, BinarySyntax currentSyntax, String[] syntaxOids )
    {
        super( parentShell );
        this.currentSyntax = currentSyntax;
        this.syntaxOids = syntaxOids;
        this.returnSyntax = null;
    }


    // ── MACE WINDU OPENS THE COUNCIL SESSION ─────────────────────────────────────
    // Mace Windu steps forward and formally opens the session: "Select Syntax OID."
    // The title bar names the purpose so there's no ambiguity about what type of
    // assessment is being conducted.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog's title bar text before it becomes visible.
     * A clear title helps the user understand whether they're picking a syntax
     * OID or an attribute name — these can look similar in a busy UI.
     *
     * @param newShell  The shell provided by the Eclipse dialog framework.
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "SyntaxDialog.SelectSyntaxOID" ) ); //$NON-NLS-1$
    }


    // ── THE COUNCIL READIES ITS VERDICT CONTROLS ─────────────────────────────────
    // Mace Windu sets a verdict stone in front of each councillor: one green (OK),
    // one red (Cancel).  The green stone cannot be turned until Anakin's OID is
    // actually present on the assessment sheet.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the OK and Cancel buttons and immediately validates the dialog so
     * OK starts disabled — the council won't render a verdict on an empty OID.
     *
     * @param parent  The button bar composite provided by Eclipse.
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );

        validate();
    }


    // ── THE COUNCIL RECORDS ITS VERDICT ──────────────────────────────────────────
    // The council turns its verdict stones and the verdict is recorded: the OID
    // in the combo becomes the official classification, wrapped in a BinarySyntax
    // object and filed as the return value.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks OK.  We read the OID from the combo and wrap
     * it in a new {@link BinarySyntax} for the caller to retrieve.  Then we let
     * the superclass close the dialog.
     *
     * <p>For example — the council records Anakin's classification:</p>
     * <pre>
     *   returnSyntax = new BinarySyntax("1.3.6.1.4.1.1466.115.121.1.5");
     *   // verdict committed to the Temple archives
     * </pre>
     */
    protected void okPressed()
    {
        returnSyntax = new BinarySyntax( oidCombo.getText() );
        super.okPressed();
    }


    // ── THE COUNCIL DISPLAYS THE CANDIDATE'S CLASSIFICATION card ─────────────────
    // The archivist places the classification card on the table — a label reading
    // "Syntax OID:" and a combo pre-filled with the candidate's current OID.
    // The councillors can accept it or select a different one from the registry.
    // Any modification to the combo re-triggers validation immediately.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog's content area: a two-column layout with a "Syntax OID"
     * label and a combo pre-filled from {@code currentSyntax} (if provided).
     * A modify listener keeps the OK button's enabled state in sync.
     *
     * @param parent  The parent composite provided by Eclipse.
     * @return        The composite containing all our widgets.
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );

        Composite c = BaseWidgetUtils.createColumnContainer( composite, 2, 1 );
        BaseWidgetUtils.createLabel( c, Messages.getString( "SyntaxDialog.SyntaxOID" ), 1 ); //$NON-NLS-1$
        oidCombo = BaseWidgetUtils.createCombo( c, syntaxOids, -1, 1 );
        if ( currentSyntax != null )
        {
            oidCombo.setText( currentSyntax.getSyntaxNumericOid() );
        }
        oidCombo.addModifyListener( new ModifyListener()
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
        okButton.setEnabled( !"".equals( oidCombo.getText() ) ); //$NON-NLS-1$
    }


    // ── THE ARCHIVIST RETRIEVES THE COUNCIL'S VERDICT ────────────────────────────
    // After the session, the archivist carries the verdict document to the
    // requester.  If the council declined (Cancel), the document is null —
    // no classification was recorded.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link BinarySyntax} the user confirmed by clicking OK.
     * Returns null if the dialog was cancelled, because {@code returnSyntax} is
     * only populated inside {@link #okPressed()}.
     *
     * @return  The selected {@link BinarySyntax}, or null if cancelled.
     */
    public BinarySyntax getSyntax()
    {
        return returnSyntax;
    }

}
