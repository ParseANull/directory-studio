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


import org.eclipse.jface.viewers.Viewer;


// ── CLASS: ConnectionViewActionProxy — THE CONNECTIONS-VIEW WINGMAN ────────────────
// The Connections view needs its own flavour of StudioActionProxy so we can scope
// the proxy to that particular view's viewer and action-handler-manager.
// This class adds nothing over the parent — it just specialises the constructor
// to accept a Viewer (rather than the broader ISelectionProvider) and passes
// everything up.  Having a named subclass also makes Eclipse's error logs and
// stack traces more readable.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * {@link StudioActionProxy} subclass scoped to the Connections view.
 *
 * <p>Wraps a {@link StudioAction} for use in the Connections view toolbar and
 * context menu.  Inherits all selection-listening and connection-event-listening
 * behaviour from {@link StudioActionProxy}.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionViewActionProxy extends StudioActionProxy
{

    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link ConnectionViewActionProxy}.
     *
     * @param viewer               The Connections view's tree viewer (used as the
     *                             selection provider).
     * @param actionHandlerManager The manager that activates/deactivates global
     *                             action handlers around action execution.
     * @param action               The real {@link StudioAction} to proxy.
     */
    public ConnectionViewActionProxy( Viewer viewer, ActionHandlerManager actionHandlerManager, StudioAction action )
    {
        super( viewer, actionHandlerManager, action );
    }
}
