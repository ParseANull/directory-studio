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

package org.apache.directory.studio.connection.ui.actions;


import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.connection.core.event.ConnectionUpdateListener;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;


// ── CLASS: StudioActionProxy — THE REBEL PILOT'S WINGMAN ──────────────────────────
// Han Solo never flies alone: he has R2-D2 on standby, Chewie in the co-pilot
// seat, and the rest of the squadron monitoring comms.  StudioActionProxy is the
// wingman for every concrete StudioAction:
//   - It wraps the "real" action (the pilot) and presents the JFace Action interface
//     (text, icon, enabled state, command ID) to the Eclipse toolbar/menu system.
//   - It listens to the JFace selection provider so it can push new selection data
//     into the real action and refresh its enabled/text state.
//   - It listens to ConnectionEventRegistry so it refreshes when connections open,
//     close, are added, removed, or updated — keeping the UI always consistent.
//   - When run(), it deactivates global handlers (weapons-safety-on), runs the
//     real action, then reactivates global handlers (weapons-safety-off).
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Abstract JFace {@link Action} wrapper for a {@link StudioAction}.
 *
 * <p>The proxy subscribes to two event sources:</p>
 * <ul>
 *   <li>The JFace {@link ISelectionProvider} — to push selection changes into the
 *       real action and refresh its enabled/text state.</li>
 *   <li>{@link ConnectionEventRegistry} — to refresh the action state on any
 *       connection lifecycle event (open, close, add, remove, update, folder changes).</li>
 * </ul>
 *
 * <p>During {@link #run()}, the proxy calls
 * {@link ActionHandlerManager#deactivateGlobalActionHandlers()} before and
 * {@link ActionHandlerManager#activateGlobalActionHandlers()} after the real
 * action, preventing Eclipse global handler conflicts.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class StudioActionProxy extends Action implements ISelectionChangedListener, ConnectionUpdateListener
{

    /** The action handler manager used to toggle global action key bindings around run(). */
    private ActionHandlerManager actionHandlerManager;

    /** The real action whose logic we are proxying. */
    protected StudioAction action;

    /** The selection provider this proxy listens to for selection changes. */
    protected ISelectionProvider selectionProvider;


    // ── CONSTRUCTORS ──────────────────────────────────────────────────────────────

    /**
     * Creates a new {@link StudioActionProxy} with an explicit action style.
     *
     * @param selectionProvider   The viewer to listen to for selection changes.
     * @param actionHandlerManager The manager for global action handler activation/deactivation.
     * @param action              The real action to proxy.
     * @param style               The JFace action style (e.g., {@link Action#AS_PUSH_BUTTON}).
     */
    protected StudioActionProxy( ISelectionProvider selectionProvider, ActionHandlerManager actionHandlerManager,
        StudioAction action, int style )
    {
        super( action.getText(), style );
        this.selectionProvider = selectionProvider;
        this.actionHandlerManager = actionHandlerManager;
        this.action = action;

        super.setImageDescriptor( action.getImageDescriptor() );
        super.setActionDefinitionId( action.getCommandId() );

        selectionProvider.addSelectionChangedListener( this );

        ConnectionEventRegistry.addConnectionUpdateListener( this, ConnectionUIPlugin.getDefault().getEventRunner() );

        updateAction();
    }


    /**
     * Creates a new {@link StudioActionProxy} with the default push-button style.
     *
     * @param selectionProvider   The viewer to listen to for selection changes.
     * @param actionHandlerManager The manager for global action handler activation/deactivation.
     * @param action              The real action to proxy.
     */
    protected StudioActionProxy( ISelectionProvider selectionProvider, ActionHandlerManager actionHandlerManager,
        StudioAction action )
    {
        this( selectionProvider, actionHandlerManager, action, Action.AS_PUSH_BUTTON );
    }


    // ── DISPOSE — UNSUBSCRIBE FROM EVENTS ─────────────────────────────────────────
    /**
     * Unsubscribes from the connection event registry and the selection provider,
     * disposes the real action, and nulls the reference to mark this proxy as disposed.
     */
    public void dispose()
    {
        ConnectionEventRegistry.removeConnectionUpdateListener( this );
        selectionProvider.removeSelectionChangedListener( this );

        action.dispose();
        action = null;
    }


    // ── IS DISPOSED ───────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this proxy has been disposed (i.e., the real action is null).
     *
     * @return  {@code true} if disposed.
     */
    public boolean isDisposed()
    {
        return action == null;
    }


    // ── CONNECTION UPDATE LISTENER — REFRESH ON ANY CONNECTION EVENT ───────────────
    // When any connection changes (open/close/add/remove/update), we refresh the
    // proxy's enabled state and text so the toolbar always reflects reality.
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     * Refreshes the proxy's enabled state and text when a connection is updated.
     */
    public final void connectionUpdated( Connection connection )
    {
        if ( !isDisposed() )
        {
            updateAction();
        }
    }


    /** {@inheritDoc} */
    public void connectionAdded( Connection connection )
    {
        connectionUpdated( connection );
    }


    /** {@inheritDoc} */
    public void connectionRemoved( Connection connection )
    {
        connectionUpdated( connection );
    }


    /** {@inheritDoc} */
    public void connectionOpened( Connection connection )
    {
        connectionUpdated( connection );
    }


    /** {@inheritDoc} */
    public void connectionClosed( Connection connection )
    {
        connectionUpdated( connection );
    }


    /** {@inheritDoc} */
    public void connectionFolderModified( ConnectionFolder connectionFolder )
    {
        connectionUpdated( null );
    }


    /** {@inheritDoc} */
    public void connectionFolderAdded( ConnectionFolder connectionFolder )
    {
        connectionUpdated( null );
    }


    /** {@inheritDoc} */
    public void connectionFolderRemoved( ConnectionFolder connectionFolder )
    {
        connectionUpdated( null );
    }


    // ── INPUT CHANGED — UPDATE REAL ACTION'S INPUT AND RESET SELECTION ────────────
    /**
     * Notifies this proxy that the viewer's input has changed.
     * Updates the real action's input and resets the selection state.
     *
     * @param input  The new viewer input object.
     */
    public void inputChanged( Object input )
    {
        if ( !isDisposed() )
        {
            action.setInput( input );
            selectionChanged( new SelectionChangedEvent( this.selectionProvider, new StructuredSelection() ) );
        }
    }


    // ── SELECTION CHANGED — PUSH NEW SELECTION INTO THE REAL ACTION ───────────────
    /**
     * {@inheritDoc}
     * Extracts connections and folders from the new selection and passes them
     * to the real action, then refreshes the proxy state.
     */
    public void selectionChanged( SelectionChangedEvent event )
    {
        if ( !isDisposed() )
        {
            ISelection selection = event.getSelection();
            action.setSelectedConnections( SelectionUtils.getConnections( selection ) );
            action.setSelectedConnectionFolders( SelectionUtils.getConnectionFolders( selection ) );
            updateAction();
        }
    }


    // ── UPDATE ACTION — SYNC PROXY STATE FROM REAL ACTION ─────────────────────────
    /**
     * Reads the current state from the real action (enabled, text, image, checked)
     * and pushes it into this proxy so the toolbar/menu shows the correct values.
     */
    public void updateAction()
    {
        if ( !isDisposed() )
        {
            setText( CommonUIUtils.getTextValue( action.getText() ) );
            setToolTipText( CommonUIUtils.getTextValue( action.getText() ) );
            setEnabled( action.isEnabled() );
            setImageDescriptor( action.getImageDescriptor() );
            setChecked( action.isChecked() );
        }
    }


    // ── RUN — DEACTIVATE HANDLERS, RUN REAL ACTION, REACTIVATE HANDLERS ───────────
    /**
     * {@inheritDoc}
     * Temporarily deactivates global action handlers (to avoid command conflicts),
     * runs the real action, then reactivates them.
     */
    @Override
    public void run()
    {
        if ( !isDisposed() )
        {
            if ( actionHandlerManager != null )
            {
                actionHandlerManager.deactivateGlobalActionHandlers();
            }

            action.run();

            if ( actionHandlerManager != null )
            {
                actionHandlerManager.activateGlobalActionHandlers();
            }
        }
    }


    // ── GET ACTION — ACCESS THE REAL ACTION ───────────────────────────────────────
    /**
     * Returns the real {@link StudioAction} that this proxy wraps.
     *
     * @return  The wrapped action.
     */
    public StudioAction getAction()
    {
        return action;
    }
}
