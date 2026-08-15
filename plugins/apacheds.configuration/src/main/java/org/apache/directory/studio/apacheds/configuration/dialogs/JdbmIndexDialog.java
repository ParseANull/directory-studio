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


import org.apache.directory.server.config.beans.JdbmIndexBean;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


// ── CLASS: JdbmIndexDialog — IMPERIAL ENGINEER CONFIGURES A DATA VAULT INDEX ────────────
// The Imperial engineers maintain enormous data vaults (JDBM partitions) full of LDAP entries.
// To keep lookups fast, each vault has a set of named indexes — the JDBM equivalent of a
// card-catalogue drawer.  When an engineer wants to add or edit one of those indexes, they
// fill in the attribute ID (which field to index) and the cache size (how many index entries
// to keep in memory at once).
// This dialog lets them do exactly that: two fields, attribute ID and cache size, with a
// numeric guard on the cache field so non-digits can't sneak in.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * Two-field dialog for editing a {@link JdbmIndexBean} — attribute ID and cache size.
 * The cache-size field rejects non-numeric input via a {@link VerifyListener}.
 * Sets the dirty flag when either field changes; writes both back to the model on OK.
 * Think of it as the Imperial engineer's index requisition form for a JDBM data vault.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class JdbmIndexDialog extends Dialog
{
    /** The Indexed Attribute */
    private JdbmIndexBean index;

    /** The dirty flag */
    private boolean dirty = false;

    // UI Fields
    private Text attributeIdText;
    private Text cacheSizeText;


    // ── Opening The Form Pre-Populated From The Index Bean ────────────────────────────────────
    // We receive the JdbmIndexBean up front so we can pre-populate both text fields and write
    // back to the same object when OK is clicked.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the dialog backed by the given JDBM index configuration bean.
     * The dialog will pre-populate from the bean and write back to it on OK.
     *
     * @param index  the JDBM index bean to edit (must not be null)
     */
    public JdbmIndexDialog( JdbmIndexBean index )
    {
        super( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        this.index = index;
    }


    // ── Setting The Dialog Window Title ───────────────────────────────────────────────────────
    // The title bar says "Indexed Attribute Dialog" so the engineer knows what they're editing.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog window title to the localised "Indexed Attribute Dialog" string.
     *
     * @param newShell  the shell being configured
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "JdbmIndexDialog.IndexedAttributeDialog" ) ); //$NON-NLS-1$
    }


    // ── Building The Two-Field Form ───────────────────────────────────────────────────────────
    // Lay out the attribute ID and cache-size fields in a two-column grid.
    // The cache-size field has a numeric verify listener to block non-digit characters.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the form body with two labelled text fields.
     *
     * <p>For example — the resulting form:</p>
     * <pre>
     *   +-----------------------------------------------------+
     *   | Attribute ID: [           ]  Cache Size: [        ] |
     *   +-----------------------------------------------------+
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
        attributeIdLabel.setText( Messages.getString( "JdbmIndexDialog.AttributeID" ) ); //$NON-NLS-1$

        attributeIdText = new Text( composite, SWT.BORDER );
        attributeIdText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        Label cacheSizeLabel = new Label( composite, SWT.NONE );
        cacheSizeLabel.setText( Messages.getString( "JdbmIndexDialog.CacheSize" ) ); //$NON-NLS-1$

        cacheSizeText = new Text( composite, SWT.BORDER );
        cacheSizeText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                // The cache size must be a numeric
                if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
                {
                    e.doit = false;
                }
            }
        } );

        cacheSizeText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        initFromInput();
        addListeners();

        return composite;
    }


    // ── Pre-Populating The Fields From The Index Bean ─────────────────────────────────────────
    // Pull the attribute ID and cache size from the bean and stuff them into the text fields.
    // Null attribute ID becomes an empty string; cache size is always a valid int.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the attribute-ID and cache-size text fields from the current state of the
     * {@link JdbmIndexBean}.  A null attribute ID is replaced by an empty string.
     */
    private void initFromInput()
    {
        String attributeId = index.getIndexAttributeId();
        attributeIdText.setText( ( attributeId == null ) ? "" : attributeId ); //$NON-NLS-1$
        cacheSizeText.setText( "" + index.getIndexCacheSize() ); //$NON-NLS-1$
    }


    // ── Attaching Change Listeners To Mark The Form Dirty ────────────────────────────────────
    // Any keystroke in either field sets dirty=true.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches {@link ModifyListener}s to both text fields; each listener sets the dirty
     * flag to {@code true} when the field content changes.
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

        cacheSizeText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                dirty = true;
            }
        } );
    }


    // ── Writing The Form Values Back To The Bean ──────────────────────────────────────────────
    // When the engineer clicks OK, write both fields back to the JdbmIndexBean.
    // If the cache size text can't be parsed as an integer we silently skip it — the field
    // verify listener normally prevents this from happening.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Writes both field values back to the {@link JdbmIndexBean} and closes the dialog.
     * A non-integer cache-size string (which the verify listener normally prevents) is silently
     * ignored, leaving the bean's cache size unchanged.
     */
    protected void okPressed()
    {
        index.setIndexAttributeId( attributeIdText.getText() );

        try
        {
            index.setIndexCacheSize( Integer.parseInt( cacheSizeText.getText() ) );
        }
        catch ( NumberFormatException e )
        {
            // Nothing to do, it won't happen
        }

        super.okPressed();
    }


    // ── Exposing The Edited Index Bean To The Caller ──────────────────────────────────────────
    // After the dialog closes the caller retrieves the updated bean from here.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link JdbmIndexBean} that was edited in this dialog.
     * After the dialog closes with OK, this bean contains the updated values.
     *
     * @return the JDBM index bean, updated with the values the user entered
     */
    public JdbmIndexBean getIndex()
    {
        return index;
    }


    // ── Reporting Whether The User Changed Anything ───────────────────────────────────────────
    // The caller can skip expensive model updates if dirty is still false.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the user changed anything in either text field.
     *
     * @return {@code true} if any field was modified; {@code false} if the dialog was
     *         opened and closed without changes
     */
    public boolean isDirty()
    {
        return dirty;
    }
}
