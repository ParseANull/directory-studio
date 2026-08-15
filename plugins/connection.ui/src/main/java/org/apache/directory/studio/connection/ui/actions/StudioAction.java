/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache Studio, Version 2.0 (the
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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: StudioAction — THE REBEL PILOT BASE CLASS ──────────────────────────────
// Every pilot in the Rebel Alliance has a set of skills they all share: they know
// how to read the mission briefing, check who's in the flight, know their ship's
// status, and execute a maneuver.  StudioAction is the base class that gives all
// Connections-view actions those shared skills.
// Concrete actions (OpenConnectionAction, DeleteAction, etc.) extend this and fill
// in the mission-specific parts: getText(), getImageDescriptor(), isEnabled(), run().
// This class handles the plumbing:
//   - Stores the current selection (connections + folders) as arrays.
//   - Implements selectionChanged() so the Eclipse workbench can notify us.
//   - Provides getShell() so subclasses can open dialogs.
//   - Proxies run(IAction) to the abstract run().
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for all Connections-view actions.
 *
 * <p>Implements {@link IWorkbenchWindowActionDelegate} so instances can be
 * contributed to the workbench via extension points.  Also usable directly as
 * the inner action of a {@link StudioActionProxy}.</p>
 *
 * <p>Subclasses must implement:</p>
 * <ul>
 *   <li>{@link #getText()} — the human-readable action label.</li>
 *   <li>{@link #getImageDescriptor()} — the toolbar/menu icon.</li>
 *   <li>{@link #getCommandId()} — the Eclipse command ID, or {@code null}.</li>
 *   <li>{@link #isEnabled()} — whether the action is available for the current selection.</li>
 *   <li>{@link #run()} — the action logic.</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class StudioAction implements IWorkbenchWindowActionDelegate
{
    /** The connections currently selected in the Connections view. */
    private Connection[] selectedConnections;

    /** The connection folders currently selected in the Connections view. */
    private ConnectionFolder[] selectedConnectionFolders;

    /** The optional input object set on this action (e.g., from inputChanged). */
    private Object input;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new StudioAction with empty selection arrays.
     */
    protected StudioAction()
    {
        this.init();
    }


    // ── GET STYLE — DEFAULT: PUSH BUTTON ──────────────────────────────────────────
    /**
     * Returns the Eclipse action style.  Defaults to {@link Action#AS_PUSH_BUTTON}.
     *
     * @return  The action style constant.
     */
    public int getStyle()
    {
        return Action.AS_PUSH_BUTTON;
    }


    // ── INIT (WINDOW) — INITIALISE FROM THE WORKBENCH WINDOW ──────────────────────
    /**
     * {@inheritDoc}
     * Resets the action state when it is initialised by the workbench window.
     */
    public void init( IWorkbenchWindow window )
    {
        this.init();
    }


    // ── RUN (IACTION) — BRIDGE FROM IWorkbenchWindowActionDelegate ────────────────
    /**
     * {@inheritDoc}
     * Delegates to the abstract {@link #run()} method.
     */
    public void run( IAction action )
    {
        this.run();
    }


    // ── SELECTION CHANGED — UPDATE SELECTION AND ENABLE STATE ─────────────────────
    /**
     * {@inheritDoc}
     * Extracts connections and folders from the new selection and updates the
     * action's enabled state and text on the IAction wrapper.
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        setSelectedConnections( SelectionUtils.getConnections( selection ) );
        setSelectedConnectionFolders( SelectionUtils.getConnectionFolders( selection ) );

        action.setEnabled( isEnabled() );
        action.setText( CommonUIUtils.getTextValue( getText() ) );
        action.setToolTipText( CommonUIUtils.getTextValue( getText() ) );
    }


    // ── ABSTRACT METHODS — SUBCLASS RESPONSIBILITIES ──────────────────────────────

    /**
     * Returns the human-readable label for this action.
     *
     * @return  The action text.
     */
    public abstract String getText();


    /**
     * Returns the icon for this action, or {@code null} for no icon.
     *
     * @return  The {@link ImageDescriptor}, or {@code null}.
     */
    public abstract ImageDescriptor getImageDescriptor();


    /**
     * Returns the Eclipse command definition ID to bind this action to a key binding,
     * or {@code null} if no binding is needed.
     *
     * @return  The command ID string, or {@code null}.
     */
    public abstract String getCommandId();


    /**
     * Returns {@code true} if this action should be enabled for the current selection.
     *
     * @return  {@code true} if enabled.
     */
    public abstract boolean isEnabled();


    /**
     * Executes this action.  Subclasses implement the actual logic here.
     */
    public abstract void run();


    // ── IS CHECKED — OPTIONAL TOGGLE STATE ────────────────────────────────────────
    /**
     * Returns {@code false} by default.  Override for toggle-style actions.
     *
     * @return  {@code false}.
     */
    public boolean isChecked()
    {
        return false;
    }


    // ── INIT — RESET STATE ────────────────────────────────────────────────────────
    /**
     * Resets the action state: clears selected connections, folders, and input.
     */
    private void init()
    {
        this.selectedConnections = new Connection[0];
        this.selectedConnectionFolders = new ConnectionFolder[0];
        this.input = null;
    }


    // ── DISPOSE — CLEAN UP RESOURCES ──────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Clears selection and input references.
     */
    public void dispose()
    {
        this.selectedConnections = new Connection[0];
        this.selectedConnectionFolders = new ConnectionFolder[0];
        this.input = null;
    }


    // ── GET SHELL — CONVENIENCE ACCESSOR ──────────────────────────────────────────
    /**
     * Returns the active workbench shell, for use by subclasses that need to open dialogs.
     *
     * @return  The active SWT {@link Shell}.
     */
    protected Shell getShell()
    {
        return PlatformUI.getWorkbench().getDisplay().getActiveShell();
    }


    // ── SELECTION GETTERS / SETTERS ───────────────────────────────────────────────

    /**
     * Returns the connections currently selected in the Connections view.
     *
     * @return  The selected connections; never {@code null}.
     */
    public Connection[] getSelectedConnections()
    {
        return selectedConnections;
    }


    /**
     * Sets the selected connections.
     *
     * @param selectedConnections  The connections to set.
     */
    public void setSelectedConnections( Connection[] selectedConnections )
    {
        this.selectedConnections = selectedConnections;
    }


    /**
     * Returns the connection folders currently selected in the Connections view.
     *
     * @return  The selected folders; never {@code null}.
     */
    public ConnectionFolder[] getSelectedConnectionFolders()
    {
        return selectedConnectionFolders;
    }


    /**
     * Sets the selected connection folders.
     *
     * @param selectedConnectionFolders  The folders to set.
     */
    public void setSelectedConnectionFolders( ConnectionFolder[] selectedConnectionFolders )
    {
        this.selectedConnectionFolders = selectedConnectionFolders;
    }


    /**
     * Returns the optional input object for this action.
     *
     * @return  The input object, or {@code null}.
     */
    public Object getInput()
    {
        return input;
    }


    /**
     * Sets the optional input object for this action.
     *
     * @param input  The input object.
     */
    public void setInput( Object input )
    {
        this.input = input;
    }
}
