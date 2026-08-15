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
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPlugin;
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPluginConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;


// ── CLASS: PartitionsPage — The Imperial Vault Wing Configuration Tab ─────
// The Partitions tab is the dedicated wing of the Death Star configuration
// editor where every data vault is catalogued and managed: vault engineers
// can browse the complete roster on the left, click into any vault to view
// its full specification on the right, and add or decommission vaults as
// the Empire's data storage needs evolve.
// ─────────────────────────────────────────────────────────────────────────
/**
 * The Partitions tab of the ApacheDS Server Configuration Editor.
 * Hosts a {@link PartitionsMasterDetailsBlock} that splits the page into
 * a master list of partitions on the left and a details panel on the right.
 * Think of this class as the entrance to the Imperial vault wing: it provides
 * the tab itself and delegates all actual vault-management work to the
 * master/details block inside it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PartitionsPage extends ServerConfigurationEditorPage
{
    /** The Page ID*/
    public static final String ID = PartitionsPage.class.getName(); //$NON-NLS-1$

    /** The Page Title */
    private static final String TITLE = Messages.getString( "PartitionsPage.Partitions" ); //$NON-NLS-1$

    /** The Master Details Block */
    private PartitionsMasterDetailsBlock masterDetailsBlock;

    /**
     * Shared label provider for partition table viewers.
     * Formats each partition as "id (suffix) [type]" and returns the appropriate
     * icon (system vault icon or standard vault icon) depending on partition type.
     */
    public static LabelProvider PARTITIONS_LABEL_PROVIDER = new LabelProvider()
    {
        public String getText( Object element )
        {
            if ( element instanceof PartitionBean )
            {
                PartitionBean partition = ( PartitionBean ) element;

                return NLS
                    .bind(
                        "{0} ({1}) [{2}]", new Object[] { partition.getPartitionId(), partition.getPartitionSuffix(), getPartitionType( partition ) } ); //$NON-NLS-1$
            }
            else if ( element instanceof PartitionWrapper )
            {
                return getText( ( ( PartitionWrapper ) element ).getPartition() );
            }

            return super.getText( element );
        }


        private String getPartitionType( PartitionBean partition )
        {
            PartitionType type = PartitionType.fromPartition( partition );

            if ( type != null )
            {
                return type.toString();
            }
            else
            {
                return "Unknown";
            }
        }


        public Image getImage( Object element )
        {
            if ( element instanceof PartitionBean )
            {
                PartitionBean partition = ( PartitionBean ) element;

                if ( isSystemPartition( partition ) )
                {
                    return ApacheDS2ConfigurationPlugin.getDefault().getImage(
                        ApacheDS2ConfigurationPluginConstants.IMG_PARTITION_SYSTEM );
                }
                else
                {
                    return ApacheDS2ConfigurationPlugin.getDefault().getImage(
                        ApacheDS2ConfigurationPluginConstants.IMG_PARTITION );
                }
            }
            else if ( element instanceof PartitionWrapper )
            {
                return getImage( ( ( PartitionWrapper ) element ).getPartition() );
            }

            return super.getImage( element );
        }
    };

    /**
     * Shared comparator for partition table viewers.
     * Sorts partitions alphabetically by their ID string so the roster is
     * always presented in a predictable order regardless of insertion sequence.
     */
    public static ViewerComparator PARTITIONS_COMPARATOR = new ViewerComparator()
    {
        public int compare( Viewer viewer, Object e1, Object e2 )
        {
            if ( ( e1 instanceof PartitionBean ) && ( e2 instanceof PartitionBean ) )
            {
                PartitionBean partition1 = ( PartitionBean ) e1;
                PartitionBean partition2 = ( PartitionBean ) e2;

                String partition1Id = partition1.getPartitionId();
                String partition2Id = partition2.getPartitionId();

                if ( ( partition1Id != null ) && ( partition2Id != null ) )
                {
                    return partition1Id.compareTo( partition2Id );
                }
            }
            if ( ( e1 instanceof PartitionWrapper ) && ( e2 instanceof PartitionWrapper ) )
            {
                return compare( viewer, ( ( PartitionWrapper ) e1 ).getPartition(),
                    ( ( PartitionWrapper ) e2 ).getPartition() );
            }

            return super.compare( viewer, e1, e2 );
        }
    };


    // ── Partitions Tab Registered with the Parent Editor ─────────────────────
    // The vault wing tab clicks into place on the editor's tab bar, claiming its
    // unique ID and its human-readable title so the editor framework can route
    // the user to this page when the "Partitions" tab is clicked.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the Partitions page and registers it with the parent editor.
     * Passes the stable page ID and the translated "Partitions" title up to
     * the base class so the Eclipse editor knows how to find and label this tab.
     *
     * <p>For example — the vault wing door is labelled and unlocked:</p>
     * <pre>
     *   The tab bar gains a "Partitions" entry that, when clicked, routes
     *   the user to this page and its master/details vault registry block.
     * </pre>
     *
     * @param editor  the parent {@link ServerConfigurationEditor} that owns this page
     */
    public PartitionsPage( ServerConfigurationEditor editor )
    {
        super( editor, ID, TITLE );
    }


    // ── Vault Wing Interior Laid Out with Master/Details Block ───────────────
    // The vault wing interior is simple: the whole page is taken up by the
    // master/details block — roster on the left, vault dossier on the right.
    // We instantiate the block and ask it to build its content inside our form.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the Partitions tab UI by creating and delegating to the
     * {@link PartitionsMasterDetailsBlock}.
     * The entire page content is owned by the block — we just wire it up here.
     *
     * @param parent   the parent composite provided by the Eclipse Forms framework
     * @param toolkit  the form toolkit (passed through to the block)
     */
    protected void createFormContent( Composite parent, FormToolkit toolkit )
    {
        masterDetailsBlock = new PartitionsMasterDetailsBlock( this );
        masterDetailsBlock.createContent( getManagedForm() );
    }


    // ── Vault Registry Board Refreshed if Already Initialized ────────────────
    // If the vault wing is already open and active, a refresh signal comes in —
    // perhaps the underlying config changed — and we relay it to the master/details
    // block so the roster reloads. We skip the refresh if the wing isn't open yet.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Reloads the partition list from the configuration model if the page is ready.
     * Delegates to {@link PartitionsMasterDetailsBlock#refreshUI()}.
     * Does nothing if the page hasn't finished initializing.
     */
    protected void refreshUI()
    {
        if ( isInitialized() )
        {
            masterDetailsBlock.refreshUI();
        }
    }


    // ── System Vault Identification Check ─────────────────────────────────────
    // Among all the vaults in the registry, one is special and untouchable:
    // the "system" partition that ApacheDS uses internally. We identify it by
    // its partition ID so it can be protected from user deletion.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the given partition is the built-in system partition.
     * The system partition has the reserved ID {@code "system"} and must not
     * be deleted or renamed by the user.
     *
     * <p>For example — the protected vault is identified:</p>
     * <pre>
     *   partition.getPartitionId() == "system" → true  (protected, no Delete button)
     *   partition.getPartitionId() == "example" → false (user-managed, deletable)
     * </pre>
     *
     * @param partition  the partition to check; may be {@code null}
     * @return           {@code true} if the partition ID is {@code "system"} (case-insensitive),
     *                   {@code false} for any other ID or for a {@code null} argument
     */
    public static boolean isSystemPartition( PartitionBean partition )
    {
        if ( partition != null )
        {
            return "system".equalsIgnoreCase( partition.getPartitionId() ); //$NON-NLS-1$
        }

        return false;
    }


    // ── Vault Registry Saved to the Official Config Archive ──────────────────
    // When the user hits Save, we relay the call down to the master/details block
    // which knows how to commit every vault's current state back into the model.
    // We guard against a null block in case Save is called before the page loads.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Saves all partition data by delegating to {@link PartitionsMasterDetailsBlock#doSave(IProgressMonitor)}.
     * Guards against the case where the master/details block hasn't been created yet.
     *
     * @param monitor  the progress monitor provided by the Eclipse save infrastructure
     */
    public void doSave( IProgressMonitor monitor )
    {
        if ( masterDetailsBlock != null )
        {
            masterDetailsBlock.doSave( monitor );
        }
    }
}
