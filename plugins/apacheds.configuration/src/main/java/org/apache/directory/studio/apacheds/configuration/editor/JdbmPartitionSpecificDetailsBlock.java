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
package org.apache.directory.studio.apacheds.configuration.editor;


import org.apache.directory.server.config.beans.JdbmPartitionBean;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;


// ── CLASS: JdbmPartitionSpecificDetailsBlock — CONFIGURING THE JDBM VAULT SHELVING ──────────────
// Deep in Palpatine's vault complex, the JDBM wing uses a particular shelving system:
// adjustable cache racks (the cache-size dial) and an automatic shelf sorter
// (the optimizer toggle) that reorganises data for faster retrieval.
// The vault keeper for this wing — this class — lets the administrator set those two dials
// and saves the settings back into the partition bean when the user is done.
// ─────────────────────────────────────────────────────────────────────────────────────────────────
/**
 * The backend-specific UI details block for a JDBM partition. JDBM is a Java-based
 * B-tree engine; its two tunable parameters are the cache size (how many B-tree
 * pages we keep in RAM) and whether the query optimiser is active.
 * We display those two controls in a small composite inside the partition details
 * page, read them from the {@link JdbmPartitionBean} on refresh, and write them
 * back on commit.
 * Think of this class as the vault-keeper assigned to the JDBM wing of Palpatine's
 * data vaults — it knows exactly which dials control that wing's shelving system.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class JdbmPartitionSpecificDetailsBlock extends AbstractPartitionSpecificDetailsBlock<JdbmPartitionBean>
{
    // UI widgets
    private Text cacheSizeText;
    private Button enableOptimizerCheckbox;


    // ── COMMISSIONING THE JDBM VAULT WING ────────────────────────────────────────────────────────
    // Palpatine assigns a specialist vault keeper to the JDBM wing and hands them
    // the corridor map (detailsPage) and the sealed data container (partition).
    // The wing is now ready; createBlockContent will furnish it with dials.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new JDBM-specific details block for the given partition details
     * page and partition bean. Delegates shared initialisation to the parent class.
     *
     * <p>For example — commissioning the JDBM vault wing:</p>
     * <pre>
     *   JdbmPartitionSpecificDetailsBlock block =
     *       new JdbmPartitionSpecificDetailsBlock(detailsPage, jdbmPartitionBean);
     * </pre>
     *
     * @param detailsPage  The owning partition details page; receives dirty notifications.
     * @param partition    The JDBM partition bean whose settings this block manages.
     */
    public JdbmPartitionSpecificDetailsBlock( PartitionDetailsPage detailsPage, JdbmPartitionBean partition )
    {
        super( detailsPage, partition );
    }


    // ── INSTALLING THE CACHE RACK AND OPTIMIZER DIAL ─────────────────────────────────────────────
    // The vault keeper installs the two adjustable shelving controls on the wing's control panel:
    // a numeric dial for cache size and a toggle switch for the automatic shelf sorter.
    // createBlockContent builds those two SWT widgets inside the parent composite.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds and returns the SWT composite that contains the JDBM-specific
     * controls: a numeric text field for cache size and a checkbox for enabling
     * the B-tree query optimiser. The composite is wired into the parent layout.
     *
     * <p>For example — the vault keeper installs the control panel:</p>
     * <pre>
     *   Composite panel = block.createBlockContent(parent, toolkit);
     *   // panel now contains: cache-size text field + optimizer checkbox
     * </pre>
     *
     * @param parent   The parent composite provided by the partition details page.
     * @param toolkit  The Eclipse forms toolkit used to create styled widgets.
     * @return         The filled-in composite containing the JDBM controls.
     */
    public Composite createBlockContent( Composite parent, FormToolkit toolkit )
    {
        // Composite
        Composite composite = toolkit.createComposite( parent );
        composite.setLayout( new GridLayout( 2, false ) );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Cache Size
        toolkit.createLabel( composite, Messages.getString( "PartitionDetailsPage.CacheSize" ) ); //$NON-NLS-1$
        cacheSizeText = toolkit.createText( composite, "" ); //$NON-NLS-1$
        cacheSizeText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
                {
                    e.doit = false;
                }
            }
        } );
        cacheSizeText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Enable Optimizer
        enableOptimizerCheckbox = toolkit.createButton( composite,
            Messages.getString( "PartitionDetailsPage.EnableOptimzer" ), SWT.CHECK ); //$NON-NLS-1$
        enableOptimizerCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

        return composite;
    }


    // ── ARMING THE SHELVING SENSORS ───────────────────────────────────────────────────────────────
    // The vault keeper activates the motion sensors on the two dials so any adjustment
    // immediately triggers the "vault modified" alarm back at the main control room.
    // addListeners wires the dirty-listener trio to our two widgets.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches the shared dirty-notification listeners to both JDBM-specific
     * widgets. After this call, any change in the cache-size field or the
     * optimizer checkbox will propagate a dirty flag to the owning editor.
     *
     * <p>For example — the sensors go live:</p>
     * <pre>
     *   addListeners();
     *   // user edits cacheSizeText -> editor is marked dirty
     * </pre>
     */
    private void addListeners()
    {
        cacheSizeText.addModifyListener( dirtyModifyListener );
        enableOptimizerCheckbox.addSelectionListener( dirtySelectionListener );
    }


    // ── DISARMING THE SHELVING SENSORS ───────────────────────────────────────────────────────────
    // Before the vault keeper refills the shelves from the bean data, she disarms the sensors
    // so the programmatic updates don't trigger false "modified" alarms.
    // removeListeners detaches our listeners before a refresh.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Detaches the dirty-notification listeners from both JDBM-specific widgets.
     * We call this before programmatically refreshing the widget values from the
     * bean, so we don't accidentally mark the editor dirty during a refresh.
     *
     * <p>For example — sensors go quiet before refill:</p>
     * <pre>
     *   removeListeners();
     *   cacheSizeText.setText("512");  // no dirty alarm
     * </pre>
     */
    private void removeListeners()
    {
        cacheSizeText.removeModifyListener( dirtyModifyListener );
        enableOptimizerCheckbox.removeSelectionListener( dirtySelectionListener );
    }


    // ── READING THE CURRENT VAULT SETTINGS INTO THE CONTROL PANEL ────────────────────────────────
    // The vault keeper reads the official settings from the data container and dials them
    // into the control panel: "Cache: 512 pages. Optimizer: ON."
    // refresh pulls values from the JdbmPartitionBean and pushes them into the widgets.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Reloads the widget values from the underlying {@link JdbmPartitionBean}.
     * We temporarily detach listeners during the update so the programmatic
     * value-setting does not trigger dirty notifications.
     *
     * <p>For example — the control panel is updated from the vault records:</p>
     * <pre>
     *   block.refresh();
     *   // cacheSizeText now shows partition.getPartitionCacheSize()
     *   // enableOptimizerCheckbox reflects partition.isJdbmPartitionOptimizerEnabled()
     * </pre>
     */
    public void refresh()
    {
        removeListeners();

        if ( partition != null )
        {
            // Cache Size
            cacheSizeText.setText( "" + partition.getPartitionCacheSize() ); //$NON-NLS-1$

            // Enable Optimizer
            enableOptimizerCheckbox.setSelection( partition.isJdbmPartitionOptimizerEnabled() );
        }

        addListeners();
    }


    // ── SEALING THE VAULT SETTINGS BACK INTO THE DATA CONTAINER ──────────────────────────────────
    // The vault keeper reads the control panel, writes the settings back into the data
    // container, and seals it for transport.  The edited dials become the new official record.
    // commit pushes widget values back into the JdbmPartitionBean.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Writes the current widget values back into the underlying
     * {@link JdbmPartitionBean}. Called by the partition details page when the
     * editor commits changes — either on an explicit save or when the user
     * navigates away.
     *
     * <p>For example — the control panel readings are sealed into the data container:</p>
     * <pre>
     *   block.commit(true);
     *   // partition.getPartitionCacheSize() now matches cacheSizeText
     *   // partition.isJdbmPartitionOptimizerEnabled() now matches checkbox
     * </pre>
     *
     * @param onSave  {@code true} if we are committing because the user pressed
     *                Save; {@code false} for intermediate commits (e.g., page switch).
     */
    public void commit( boolean onSave )
    {
        if ( partition != null )
        {
            // Cache Size
            try
            {
                partition.setPartitionCacheSize( Integer.parseInt( cacheSizeText.getText() ) );
            }
            catch ( NumberFormatException nfe )
            {
                // Nothing to do
            }

            // Enable Optimizer
            partition.setJdbmPartitionOptimizerEnabled( enableOptimizerCheckbox.getSelection() );
        }
    }
}
