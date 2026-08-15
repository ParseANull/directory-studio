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
package org.apache.directory.studio.apacheds.configuration.dialogs;


import org.apache.directory.server.config.beans.MavibotIndexBean;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


// ── CLASS: MavibotIndexDialog — MAVIBOT VAULT ENGINEER SETS UP AN INDEX CARD ────────────
// Mavibot is a newer B-Tree storage engine, leaner than JDBM.
// When a Rebel data engineer wants to add an index to a Mavibot partition, they just need
// one field: which attribute to index.  No cache size — Mavibot manages its own memory.
// This dialog is that one-field form: the engineer types in an attribute ID and clicks OK.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * Single-field dialog for editing a {@link MavibotIndexBean} — just the attribute ID.
 * Mavibot does not expose a configurable cache size, so the form has only one input.
 * Sets the dirty flag when the field changes; writes back to the model on OK.
 * Think of it as the Rebel data engineer's index card for a Mavibot vault.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MavibotIndexDialog extends Dialog
{
    /** The Indexed Attribute */
    private MavibotIndexBean index;

    /** The dirty flag */
    private boolean dirty = false;

    // UI Fields
    private Text attributeIdText;


    // ── Opening The Form Pre-Populated From The Index Bean ────────────────────────────────────
    // We receive the MavibotIndexBean up front so we can pre-populate the text field and write
    // back to the same object when OK is clicked.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the dialog backed by the given Mavibot index configuration bean.
     * The dialog will pre-populate from the bean and write back to it on OK.
     *
     * @param index  the Mavibot index bean to edit (must not be null)
     */
    public MavibotIndexDialog( MavibotIndexBean index )
    {
        super( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        this.index = index;
    }


    // ── Setting The Dialog Window Title ───────────────────────────────────────────────────────
    // The title bar identifies this as the "Indexed Attribute Dialog" for Mavibot.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog window title to the localised "Indexed Attribute Dialog" string.
     *
     * @param newShell  the shell being configured
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "MavibotIndexDialog.IndexedAttributeDialog" ) ); //$NON-NLS-1$
    }


    // ── Building The Single-Field Form ────────────────────────────────────────────────────────
    // Just one label and one text box in a two-column grid.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the form body with a single labelled attribute-ID text field.
     *
     * <p>For example — the resulting form:</p>
     * <pre>
     *   +------------------------------+
     *   | Attribute ID: [           ]  |
     *   +------------------------------+
     * </pre>
     *
     * @param parent  the parent composite provided by the Dialog framework
     * @return the created composite
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout layout = new GridLayout( 2, false );
        composite.setLayout( layout );
        composite.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

        Label attributeIdLabel = new Label( composite, SWT.NONE );
        attributeIdLabel.setText( Messages.getString( "MavibotIndexDialog.AttributeID" ) ); //$NON-NLS-1$

        attributeIdText = new Text( composite, SWT.BORDER );
        attributeIdText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        initFromInput();
        addListeners();

        return composite;
    }


    // ── Pre-Populating The Field From The Index Bean ──────────────────────────────────────────
    // Pull the attribute ID from the bean and stuff it into the text field.
    // Null becomes empty string so the widget doesn't blow up.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the attribute-ID text field from the current state of the {@link MavibotIndexBean}.
     * A null attribute ID is replaced by an empty string.
     */
    private void initFromInput()
    {
        String attributeId = index.getIndexAttributeId();
        attributeIdText.setText( ( attributeId == null ) ? "" : attributeId ); //$NON-NLS-1$
    }


    // ── Attaching A Change Listener To Mark The Form Dirty ───────────────────────────────────
    // Any keystroke sets dirty=true so the caller can tell whether something changed.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches a {@link ModifyListener} to the attribute-ID field; sets the dirty flag
     * to {@code true} when the content changes.
     */
    private void addListeners()
    {
        attributeIdText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                dirty = true;
            }
        } );
    }


    // ── Writing The Field Value Back To The Bean ──────────────────────────────────────────────
    // When the engineer clicks OK, write the attribute ID back to the MavibotIndexBean.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Writes the attribute-ID field value back to the {@link MavibotIndexBean} and closes
     * the dialog.
     */
    protected void okPressed()
    {
        index.setIndexAttributeId( attributeIdText.getText() );

        super.okPressed();
    }


    // ── Exposing The Edited Index Bean To The Caller ──────────────────────────────────────────
    // After the dialog closes the caller retrieves the updated bean from here.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link MavibotIndexBean} that was edited in this dialog.
     * After the dialog closes with OK, this bean contains the updated attribute ID.
     *
     * @return the Mavibot index bean, updated with the value the user entered
     */
    public MavibotIndexBean getIndex()
    {
        return index;
    }


    // ── Reporting Whether The User Changed Anything ───────────────────────────────────────────
    // The caller can skip expensive model updates if dirty is still false.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the user changed anything in the attribute-ID field.
     *
     * @return {@code true} if the field was modified; {@code false} if the dialog was
     *         opened and closed without changes
     */
    public boolean isDirty()
    {
        return dirty;
    }
}
