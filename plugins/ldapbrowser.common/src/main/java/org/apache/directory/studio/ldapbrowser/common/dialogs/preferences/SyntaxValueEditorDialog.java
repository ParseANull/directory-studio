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
import org.apache.directory.studio.ldapbrowser.core.model.schema.SyntaxValueEditorRelation;
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


// ── CLASS: SyntaxValueEditorDialog — C-3PO PICKS THE LANGUAGE MODE FOR A SPECIES ──
// C-3PO arrives at the entrance to Jabba's Palace.  A Gamorrean Guard grunts at
// him — not in Basic, not in Huttese.  C-3PO doesn't panic: he looks up the OID
// for "Gamorrean grunts" in his registry, pairs it with the correct translation
// module from his plugin list, and locks in the mapping before proceeding.
// This dialog does the same thing for LDAP syntaxes: the user picks a syntax OID
// (the species identifier) and matches it to the right value editor plugin
// (the translation module).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A modal dialog for creating or editing a pairing between an LDAP syntax OID
 * and the value editor plugin that should handle it.
 * Think of this class as C-3PO's species-to-language-mode lookup — pick the
 * syntax OID, pick the editor, confirm.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SyntaxValueEditorDialog extends Dialog
{
    /** The initial syntax to value editor relation. */
    private SyntaxValueEditorRelation relation;

    /** Map with class name => value editor extension. */
    private SortedMap<String, ValueEditorExtension> class2ValueEditorExtensionMap;

    /** The syntax OIDs. */
    private String[] syntaxOids;

    /** Map with value editor names => class name. */
    private SortedMap<String, String> veName2classMap;

    /** The selected syntax to value editor relation. */
    private SyntaxValueEditorRelation returnRelation;

    /** The OID combo. */
    private Combo oidCombo;

    /** The value editor combo. */
    private Combo valueEditorCombo;

    /** The OK button of the dialog */
    private Button okButton;


    // ── C-3PO LOADS THE SPECIES-TO-MODULE REGISTRY ───────────────────────────────
    // C-3PO powers up at the palace gate, loads his full registry of known syntax
    // OIDs and their matching translation modules, pre-selects the current pairing
    // so the operator can review it, and builds a reverse index for the combo UI.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the dialog pre-loaded with the existing syntax-to-editor relation
     * (if any), the full plugin registry, and the list of known syntax OIDs to
     * offer in the first combo.  We invert the class-to-extension map into a
     * name-to-class map so the UI can show friendly editor names.
     *
     * <p>For example — C-3PO at the gate:</p>
     * <pre>
     *   relation            = ("1.3.6.1.4.1.1466.115.121.1.5", "InPlaceImageValueEditor")
     *   class2EditorMap     = { "InPlaceImageValueEditor" -> extension, ... }
     *   veName2classMap     = { "Image Editor" -> "InPlaceImageValueEditor", ... }
     * </pre>
     *
     * @param parentShell                   The shell that owns this dialog.
     * @param relation                      The existing syntax-to-editor pair, or
     *                                      null if we're adding a new one.
     * @param class2ValueEditorExtensionMap Map from class name to editor extension
     *                                      metadata — our plugin registry.
     * @param syntaxOids                    All syntax OIDs the user can choose from.
     */
    public SyntaxValueEditorDialog( Shell parentShell, SyntaxValueEditorRelation relation,
        SortedMap<String, ValueEditorExtension> class2ValueEditorExtensionMap, String[] syntaxOids )
    {
        super( parentShell );
        this.relation = relation;
        this.class2ValueEditorExtensionMap = class2ValueEditorExtensionMap;
        this.syntaxOids = syntaxOids;
        this.returnRelation = null;

        this.veName2classMap = new TreeMap<String, String>();
        for ( ValueEditorExtension vee : class2ValueEditorExtensionMap.values() )
        {
            veName2classMap.put( vee.name, vee.className );
        }
    }


    // ── C-3PO ANNOUNCES THE MAPPING SESSION ──────────────────────────────────────
    // C-3PO straightens up, adjusts his photoreceptors, and announces: "Attribute
    // Value Editor configuration."  Just a title bar, but protocol requires it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog's title bar text before it becomes visible.
     *
     * @param newShell  The shell provided by the Eclipse dialog framework.
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "SyntaxValueEditorDialog.AttributeValueEditor" ) ); //$NON-NLS-1$
    }


    // ── C-3PO ARMS THE CONFIRM AND ABORT CONTROLS ────────────────────────────────
    // C-3PO primes two buttons — lock in the pairing (OK) or discard (Cancel).
    // Both start in their initial state; validate() immediately disables OK
    // until both combos have a selection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the OK and Cancel buttons and immediately calls {@link #validate()}
     * so OK starts disabled.  Both a syntax OID and a value editor must be chosen
     * before the mapping can be committed.
     *
     * @param parent  The button bar composite provided by Eclipse.
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );

        validate();
    }


    // ── C-3PO COMMITS THE OID-TO-MODULE MAPPING ──────────────────────────────────
    // The operator confirms the selection.  C-3PO writes the OID-to-module pairing
    // into his active translation table and tells the guard to let the party through.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks OK.  We read the OID from the first combo and
     * look up the class name from our {@code veName2classMap}, then wrap them into
     * a new {@link SyntaxValueEditorRelation} for the caller to retrieve.
     *
     * <p>For example — C-3PO commits the mapping:</p>
     * <pre>
     *   oid         = "1.3.6.1.4.1.1466.115.121.1.5"
     *   editorClass = "org.apache.directory.studio.valueeditors.image.InPlaceImageValueEditor"
     *   returnRelation = new SyntaxValueEditorRelation(oid, editorClass)
     * </pre>
     */
    protected void okPressed()
    {
        returnRelation = new SyntaxValueEditorRelation( oidCombo.getText(), ( String ) veName2classMap
            .get( valueEditorCombo.getText() ) );
        super.okPressed();
    }


    // ── C-3PO PROJECTS THE TWO-COMBO SELECTION INTERFACE ─────────────────────────
    // C-3PO projects two holographic menus: the first lists all known syntax OIDs,
    // the second lists all translation modules.  He pre-selects current values
    // so the operator doesn't have to scroll from scratch when editing.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog's content area with two labeled combo boxes: one for the
     * syntax OID and one for the value editor.  If we have an existing relation we
     * pre-fill both combos.  Both combos have modify listeners that recheck whether
     * OK should be enabled.
     *
     * <p>For example — C-3PO's two-step console:</p>
     * <pre>
     *   Syntax OID:   [1.3.6.1.4.1.1466.115.121.1.5 ▼]
     *   Value Editor: [Image Editor ▼]
     * </pre>
     *
     * @param parent  The parent composite provided by Eclipse.
     * @return        The composite containing all our widgets.
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );

        Composite c = BaseWidgetUtils.createColumnContainer( composite, 2, 1 );
        BaseWidgetUtils.createLabel( c, Messages.getString( "SyntaxValueEditorDialog.SyntaxOID" ), 1 ); //$NON-NLS-1$
        oidCombo = BaseWidgetUtils.createCombo( c, syntaxOids, -1, 1 );
        if ( relation != null && relation.getSyntaxOID() != null )
        {
            oidCombo.setText( relation.getSyntaxOID() );
        }
        oidCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                validate();
            }
        } );

        BaseWidgetUtils.createLabel( c, Messages.getString( "SyntaxValueEditorDialog.ValueEditor" ), 1 ); //$NON-NLS-1$
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
            !"".equals( valueEditorCombo.getText() ) && !"".equals( oidCombo.getText() ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── C-3PO HANDS OVER THE CONFIRMED SYNTAX-TO-EDITOR MAPPING ─────────────────
    // The operator asks what was decided.  C-3PO retrieves the committed OID-to-module
    // relation from his buffer.  If the session was aborted (Cancel), the buffer is
    // empty — null comes back.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link SyntaxValueEditorRelation} committed when the user clicked
     * OK.  Returns null if the dialog was cancelled, because we only populate
     * {@code returnRelation} inside {@link #okPressed()}.
     *
     * @return  The confirmed syntax-to-editor relation, or null if cancelled.
     */
    public SyntaxValueEditorRelation getRelation()
    {
        return returnRelation;
    }

}
