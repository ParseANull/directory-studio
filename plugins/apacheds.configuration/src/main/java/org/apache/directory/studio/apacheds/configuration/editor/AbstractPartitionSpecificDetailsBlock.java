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


import org.apache.directory.server.config.beans.PartitionBean;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;


// ── CLASS: AbstractPartitionSpecificDetailsBlock — PALPATINE'S IMPERIAL VAULT BLUEPRINT ─────────
// In Revenge of the Sith, Palpatine's secret vaults store the most sensitive Imperial data.
// Each vault wing is specialized — some hold weapon schematics, others hold prisoner records —
// but they all share the same corridor layout, security protocols, and access controls.
// This abstract class is that shared blueprint: every concrete partition-details block
// (JDBM, Mavibot, etc.) inherits the common wiring — the details page reference, the partition
// bean handle, and the standard listener trio that marks the editor dirty on any change.
// ─────────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for the backend-specific UI details block shown inside
 * the partition details page of the ApacheDS configuration editor.
 * Subclasses add backend-specific widgets (e.g., cache-size for JDBM) while
 * we handle the shared scaffolding: the parent page reference, the partition
 * bean, and the three listener instances that mark the editor dirty whenever
 * a widget value changes.
 * Think of this class as Palpatine's vault blueprint — every vault wing follows
 * the same access-control blueprint, but each wing specialises in its own data.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractPartitionSpecificDetailsBlock<P extends PartitionBean> implements
    PartitionSpecificDetailsBlock
{
    /** The details page*/
    protected PartitionDetailsPage detailsPage;

    /** The partition */
    protected P partition;

    // Listeners
    protected ModifyListener dirtyModifyListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            detailsPage.setEditorDirty();
        }
    };
    protected WidgetModifyListener dirtyWidgetModifyListener = new WidgetModifyListener()
    {
        public void widgetModified( WidgetModifyEvent event )
        {
            detailsPage.setEditorDirty();
        }
    };
    protected SelectionListener dirtySelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            detailsPage.setEditorDirty();
        }
    };


    // ── VAULT CORRIDOR ESTABLISHED ───────────────────────────────────────────────────────────────
    // A new wing of Palpatine's vault is being commissioned — the vault keeper receives
    // the master corridor map (detailsPage) and the sealed data container (partition) it will manage.
    // The wing is now ready; subclasses will furnish it with backend-specific shelving.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Wires up the shared infrastructure every details block needs: a reference
     * to the owning partition details page (so we can flag it as dirty) and a
     * handle on the partition bean whose data we are editing.
     *
     * <p>For example — a new vault wing opens, ready to receive its specific shelving:</p>
     * <pre>
     *   vault.assignCorridor(masterMap);      // detailsPage reference
     *   vault.sealDataContainer(partition);   // partition bean handle
     *   // subclass then adds backend-specific widgets on top
     * </pre>
     *
     * @param detailsPage  The owning {@link PartitionDetailsPage}; we call
     *                     {@code setEditorDirty()} on it whenever something changes.
     * @param partition    The typed partition bean whose settings this block displays
     *                     and commits back to the model.
     */
    public AbstractPartitionSpecificDetailsBlock( PartitionDetailsPage detailsPage, P partition )
    {
        this.detailsPage = detailsPage;
        this.partition = partition;
    }


    // ── RETRIEVING THE VAULT CORRIDOR MAP ────────────────────────────────────────────────────────
    // The vault keeper presents the master corridor map to whoever asks for it.
    // Palpatine's officer needs the map reference to find their way back to the main chamber.
    // Here, we hand back the details page so callers can navigate to the parent context.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the partition details page that owns this details block.
     * Callers use this to reach the parent page — for example, to trigger
     * a dirty-flag or to access shared state on the page.
     *
     * <p>For example — the vault keeper hands over the corridor map:</p>
     * <pre>
     *   PartitionDetailsPage parent = block.getDetailsPage();
     *   parent.setEditorDirty();  // trigger the dirty flag up the chain
     * </pre>
     *
     * @return  The {@link PartitionDetailsPage} that contains this block.
     */
    public PartitionDetailsPage getDetailsPage()
    {
        return detailsPage;
    }
}
