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

package org.apache.directory.studio.ldapbrowser.ui.editors.searchresult;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.ui.actions.proxy.SearchResultEditorActionProxy;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;


// ── CLASS: OpenDefaultEditorAction — Clone Trooper Relaying the Order ─────────
// Order 66 goes out through a chain of command — not every trooper hears it
// directly from Palpatine.  Some troopers relay the order to others who carry it
// out.  This action is that relay trooper: it catches the "edit value" command
// and immediately forwards it to the best-value-editor proxy.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The default "Edit Value" action that forwards to the {@link OpenBestEditorAction}
 * via a proxy.  It's the action wired to the global {@code ACTION_ID_EDIT_VALUE}
 * command ID (e.g. the F2 key), so it needs to exist as a stable, named action
 * even though it immediately delegates to whichever editor is best for the current cell.
 * Think of this as the relay trooper who passes Order 66 downstream without
 * changing it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenDefaultEditorAction extends AbstractOpenEditorAction
{

    /** The best value editor proxy. */
    private SearchResultEditorActionProxy bestValueEditorProxy;


    // ── Relay Trooper Reports for Duty ────────────────────────────────────────
    // The relay trooper is handed the radio (bestValueEditorProxy) before deployment.
    // When Order 66 arrives, they just retransmit it.  No extra logic — pure delegation.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Wires up this relay action with all its collaborators.
     * The {@code bestValueEditorProxy} is the key dependency — every interesting
     * operation (run, isEnabled, getImageDescriptor, getText) is forwarded to it.
     *
     * @param viewer                  the JFace TableViewer showing search results
     * @param cursor                  tracks the currently selected cell
     * @param valueEditorManager      manages available value editors
     * @param bestValueEditorProxy    the proxy wrapping {@link OpenBestEditorAction}; receives all delegated calls
     * @param actionGroup             manages global action handler lifecycle
     */
    public OpenDefaultEditorAction( TableViewer viewer, SearchResultEditorCursor cursor,
        ValueEditorManager valueEditorManager, SearchResultEditorActionProxy bestValueEditorProxy,
        SearchResultEditorActionGroup actionGroup )
    {
        super( viewer, cursor, valueEditorManager, actionGroup );
        this.bestValueEditorProxy = bestValueEditorProxy;
    }


    // ── Relay Trooper Forwards the Order ─────────────────────────────────────
    // The relay trooper receives "Execute!" and immediately passes it down the line.
    // We don't call super.run() here — we want the best-editor proxy to handle
    // all the logic, including its schema validation warnings.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates execution to the best-value-editor proxy.
     * This is the action triggered by the global "Edit Value" keybinding.
     * All the real work — editor selection, validation, cell-editor lifecycle — happens
     * inside the proxy.
     */
    public void run()
    {
        bestValueEditorProxy.run();
    }


    // ── Relay Trooper Stands Down ─────────────────────────────────────────────
    // Mission over: the relay trooper drops the radio so it can be collected.
    // We null out the proxy reference and let the superclass clean up its own state.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Releases the proxy reference and calls the superclass dispose.
     * Call this when tearing down the action group to avoid holding stale references.
     */
    public void dispose()
    {
        bestValueEditorProxy = null;
        super.dispose();
    }


    // ── Relay Trooper Carries the Standing Order ID ───────────────────────────
    // This trooper carries the official Order 66 badge — the global command ID that
    // Eclipse uses to wire the F2 keybinding to this action.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the global Eclipse command ID this action is bound to.
     * We return {@link BrowserCommonConstants#ACTION_ID_EDIT_VALUE} so that the
     * F2 keybinding (or whatever the workbench maps to "Edit Value") triggers this action.
     *
     * @return the command ID string for the "Edit Value" global action
     */
    public String getCommandId()
    {
        return BrowserCommonConstants.ACTION_ID_EDIT_VALUE;
    }


    // ── Relay Trooper Wears the Proxy's Insignia ──────────────────────────────
    // The relay trooper shows the same badge as the person they're relaying for,
    // so UI consumers see the right icon regardless of which class they interact with.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon from the best-value-editor proxy, or {@code null} if the proxy
     * is gone (already disposed).
     *
     * @return the image descriptor from the proxy, or {@code null}
     */
    public ImageDescriptor getImageDescriptor()
    {
        if ( bestValueEditorProxy != null )
        {
            return bestValueEditorProxy.getImageDescriptor();
        }
        else
        {
            return null;
        }
    }


    // ── Relay Trooper Announces the Default Action Name ───────────────────────
    // The relay trooper always says "Edit Value" regardless of what specific editor
    // is about to open — this is the stable label for the primary edit action.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the user-visible label for this action.
     * We always return the localized "Edit Value" string — this label is stable
     * and doesn't change based on which editor is active, unlike {@link OpenBestEditorAction}.
     *
     * @return the localized "Edit Value" string from the message bundle
     */
    public String getText()
    {
        return Messages.getString( "OpenDefaultEditorAction.EditValue" ); //$NON-NLS-1$
    }


    // ── Relay Trooper Checks If the Line Is Clear ─────────────────────────────
    // The relay trooper asks the best-editor proxy "is the order actionable right now?"
    // before passing it on — if the proxy says no, the relay also says no.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates the enablement check to the best-value-editor proxy.
     * We're enabled if and only if the proxy is enabled — which means the cursor
     * is over a modifiable cell with a usable value editor.
     *
     * @return {@code true} if the proxy is non-null and reports enabled; {@code false} otherwise
     */
    public boolean isEnabled()
    {
        if ( bestValueEditorProxy != null )
        {
            return bestValueEditorProxy.isEnabled();
        }
        else
        {
            return false;
        }
    }

}
