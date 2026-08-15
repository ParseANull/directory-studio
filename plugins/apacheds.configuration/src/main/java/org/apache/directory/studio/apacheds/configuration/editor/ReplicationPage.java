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


import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;


// ── CLASS: ReplicationPage — THE REBEL INTELLIGENCE RELAY NETWORK TAB ────────────────────
// General Draven's rebel intelligence network relies on synchronized data relays: each rebel
// cell on a distant planet keeps a live copy of the master intelligence database, receiving
// streaming updates from Yavin Base.  This tab is the control center for configuring those
// replication consumers — the relay nodes that pull synchronized copies of directory data
// from a master LDAP server.  The tab itself is thin: it hosts a ReplicationMasterDetailsBlock
// that does the real work of listing and editing each consumer's synchronization settings.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * The "Replication" tab page in the {@link ServerConfigurationEditor}.
 * Hosts a {@link ReplicationMasterDetailsBlock} that manages the list of LDAP replication
 * consumers — their source servers, search bases, credentials, and refresh intervals.
 * Think of this as the rebel intelligence relay network control panel: each consumer
 * is a rebel cell receiving a synchronized copy of the master directory feed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ReplicationPage extends ServerConfigurationEditorPage
{
    /** The Page ID */
    public static final String ID = ReplicationPage.class.getName();

    /** The Page Title */
    private static final String TITLE = Messages.getString( "ReplicationPage.Replication" ); //$NON-NLS-1$

    /** The Master Details Block */
    private ReplicationMasterDetailsBlock masterDetailsBlock;


    // ── Registering This Tab In The Editor ───────────────────────────────────────────────────
    // Draven registers the relay network tab with its unique ID and "Replication" title so the
    // editor tab strip can wire it into the multi-page configuration editor.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ReplicationPage} and registers it with the parent editor.
     *
     * @param editor  the parent {@link ServerConfigurationEditor}
     */
    public ReplicationPage( ServerConfigurationEditor editor )
    {
        super( editor, ID, TITLE );
    }


    // ── Handing The Page Off To The Master Details Block ──────────────────────────────────────
    // The page itself doesn't build widgets directly — it delegates to the master/details block
    // which builds the split view (consumer list on the left, consumer settings on the right).
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates widget creation to a new {@link ReplicationMasterDetailsBlock}.
     * The block builds a split view: consumer list (master) on the left,
     * per-consumer settings (details) on the right.
     *
     * @param parent   the parent composite provided by the Eclipse forms framework
     * @param toolkit  the form toolkit for themed widget creation
     */
    protected void createFormContent( Composite parent, FormToolkit toolkit )
    {
        masterDetailsBlock = new ReplicationMasterDetailsBlock( this );
        masterDetailsBlock.createContent( getManagedForm() );
    }


    // ── Refreshing The Relay Network View After Config Changes ────────────────────────────────
    // When the config is reloaded (e.g., after an import), the master/details block re-reads
    // the list of consumers and refreshes all widgets — but only if the page has been built.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the master/details block if this page has been fully initialised.
     * Delegates to {@link ReplicationMasterDetailsBlock#refreshUI()}.
     */
    protected void refreshUI()
    {
        if ( isInitialized() )
        {
            masterDetailsBlock.refreshUI();
        }
    }
}
