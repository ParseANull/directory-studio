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

package org.apache.directory.studio.valueeditors.objectclass;


import java.util.Arrays;

import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.ui.widgets.ExtendedContentAssistCommandAdapter;
import org.apache.directory.studio.ldapbrowser.common.widgets.ListContentProposalProvider;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.apache.directory.studio.valueeditors.ValueEditorsActivator;
import org.apache.directory.studio.valueeditors.ValueEditorsConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: ObjectClassDialog — YODA EVALUATING A CANDIDATE'S JEDI LINEAGE ────
// In the Jedi Council chamber on Coruscant, Yoda and the Council consult the
// Archives to produce the full roster of known lineages (objectClasses), then
// ask the candidate to select the one that fits — with auto-complete suggestions
// as they begin to type.  The Council confirms or rejects on the final nod.
// This dialog does the same: it presents all known object classes from the
// directory schema (sorted alphabetically, with content-assist auto-complete)
// and lets the user pick one.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Modal dialog for entering or selecting an LDAP object class name.
 * Object classes define the shape of an LDAP entry — each class is like a
 * template that specifies which attributes are required or allowed.
 * We load all known class names from the directory's {@link Schema}, sort them,
 * and present them in a combo box with content-assist auto-complete.
 * Used by {@link ObjectClassValueEditor} when the user double-clicks an
 * {@code objectClass} attribute value.
 * Think of this as the Jedi Council's lineage selection session — Yoda presents
 * the roster and the user picks the appropriate classification.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ObjectClassDialog extends Dialog
{

    /** The schema. */
    private Schema schema;

    /** The initial value. */
    private String initialValue;

    /** The object class combo. */
    private Combo objectClassCombo;

    /** The return value. */
    private String returnValue;


    // ── The Council Opens the Selection Session ───────────────────────────────
    // Yoda opens the Council session with the candidate's current lineage noted
    // and the full schema archives ready for consultation.
    // We store the schema (needed to build the object class list), the current
    // value, and allow the shell to be resized.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ObjectClassDialog.
     *
     * <p>For example — the Council opens the session:</p>
     * <pre>
     *   ObjectClassDialog dialog = new ObjectClassDialog(shell, schema, "person");
     *   dialog.open();
     * </pre>
     *
     * @param parentShell   The SWT shell that owns this dialog.
     * @param schema        The directory's schema — used to build the list of known object classes.
     * @param initialValue  The currently set object class name (pre-filled in the combo).
     */
    public ObjectClassDialog( Shell parentShell, Schema schema, String initialValue )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.initialValue = initialValue;
        this.schema = schema;
        this.returnValue = null;
    }


    // ── Yoda Labels the Council Chamber ──────────────────────────────────────
    // Yoda labels the chamber "Object Class Editor" and hoists the Jedi Council
    // insignia on the door so attendees know which selection is in progress.
    // We set the dialog title and toolbar icon here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Configures the dialog shell — sets the window title and icon.
     *
     * @param shell  The SWT Shell we're configuring.
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "ObjectClassDialog.ObjectClassEditor" ) ); //$NON-NLS-1$
        shell.setImage( ValueEditorsActivator.getDefault().getImage( ValueEditorsConstants.IMG_OCDEDITOR ) );
    }


    // ── The Council Places the Confirmation Buttons ───────────────────────────
    // The Council places "Confirm" and "Reject" before the candidate.
    // We delegate to the JFace superclass for the standard OK/Cancel button bar.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Adds the standard OK and Cancel buttons to the button bar.
     *
     * @param parent  The composite hosting the button bar.
     */
    @Override
    protected void createButtonsForButtonBar( Composite parent )
    {
        super.createButtonsForButtonBar( parent );
    }


    // ── Yoda Records the Chosen Lineage ──────────────────────────────────────
    // Yoda nods — the chosen object class is recorded in the session transcript
    // and the chamber session formally closes.
    // We capture the combo's current text as the return value.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks OK — stores the selected or typed object class
     * name as the return value.  Callers retrieve this via {@link #getObjectClass()}.
     */
    @Override
    protected void okPressed()
    {
        returnValue = objectClassCombo.getText();
        super.okPressed();
    }


    // ── Yoda Presents the Sorted Roster of Lineages ───────────────────────────
    // Yoda reads from the archives the alphabetically sorted list of all known
    // lineages and presents it to the candidate — with auto-complete so even
    // a half-remembered name is enough to find the right entry.
    // We build the combo from the schema's object class names, sorted, and wire
    // up content-assist auto-complete for convenience.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the main content area — a combo box pre-loaded with all known object
     * class names from the directory's schema, sorted alphabetically, with
     * content-assist auto-complete.
     * The combo allows free text entry in case the schema doesn't cover a custom class.
     *
     * <p>For example — Yoda presents the full lineage roster:</p>
     * <pre>
     *   [ person               ▼ ]
     *   Suggestions: organizationalPerson, inetOrgPerson, ...
     * </pre>
     *
     * @param parent  The parent composite provided by JFace's dialog framework.
     * @return        The top-level composite containing the object class combo.
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        // create composite
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        composite.setLayoutData( gd );

        // combo widget
        String[] allOcNames = SchemaUtils.getNamesAsArray( schema.getObjectClassDescriptions() );
        Arrays.sort( allOcNames );

        // attribute combo with field decoration and content proposal
        objectClassCombo = BaseWidgetUtils.createCombo( composite, new String[0], -1, 1 );
        objectClassCombo.setVisibleItemCount( 20 );
        objectClassCombo.setItems( allOcNames );
        objectClassCombo.setText( initialValue );
        new ExtendedContentAssistCommandAdapter( objectClassCombo, new ComboContentAdapter(),
            new ListContentProposalProvider( objectClassCombo.getItems() ), null, null, true );

        applyDialogFont( composite );
        return composite;
    }


    // ── Yoda Announces the Council's Decision ────────────────────────────────
    // The Council session concludes; the chosen lineage is announced and handed
    // back to whoever summoned the session.
    // We return the confirmed object class string (or null if cancelled).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the object class name selected by the user, or {@code null} if
     * the dialog was cancelled.
     *
     * <p>For example — the Council announces the chosen lineage:</p>
     * <pre>
     *   if (dialog.open() == Dialog.OK) {
     *       String oc = dialog.getObjectClass();
     *       // → "inetOrgPerson"
     *   }
     * </pre>
     *
     * @return  The selected object class name, or {@code null} if cancelled.
     */
    public String getObjectClass()
    {
        return returnValue;
    }
}
