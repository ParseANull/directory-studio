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

package org.apache.directory.studio.ldapbrowser.ui.actions.proxy;


import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.common.actions.proxy.BrowserActionProxy;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: SearchLogsViewActionProxy — LEIA AS BOUSHH IN THE SEARCH LOGS WING ──
// Same infiltration, different palace wing.  Leia-as-Boushh walked into Jabba's
// main hall, but she could have slipped into any corridor of the palace — the
// disguise is the same, the corridor is different.  SearchLogsViewActionProxy is
// the same Boushh costume worn in the Search Logs View: the proxy wraps a real
// BrowserAction and forwards all of Eclipse's action calls to it, but it binds
// to the viewer specific to the Search Logs View rather than the Modification Logs
// View.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A thin proxy that adapts a {@link BrowserAction} for use inside the Search Logs
 * View, handling the viewer-context binding that the view requires.
 * The role is identical to {@link ModificationLogsViewActionProxy} — Eclipse talks
 * to this proxy, which forwards to the real action — but the viewer it binds to
 * belongs to the Search Logs View rather than the Modification Logs View.
 * Think of this as Leia-in-disguise walking a different corridor of Jabba's palace
 * on the same rescue mission.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchLogsViewActionProxy extends BrowserActionProxy
{

    // ── Leia Walks The Search Logs Corridor ─────────────────────────────────────
    // The corridor is different but the costume is the same — Boushh strides in,
    // visor down, mission unchanged.  We accept the Search Logs View's viewer and
    // the real action, pass them to BrowserActionProxy, and let the parent handle
    // the selection-forwarding wiring.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a SearchLogsViewActionProxy that forwards to the given real action.
     * The proxy listens to selection changes on the provided viewer and forwards
     * them to the real action for enablement evaluation.
     *
     * @param viewer  the Viewer from the Search Logs View — supplies the selection
     *                context the underlying action needs to compute its enabled state
     * @param action  the real BrowserAction being proxied; all behaviour ultimately
     *                executes here
     */
    public SearchLogsViewActionProxy( Viewer viewer, BrowserAction action )
    {
        super( viewer, action );
    }

}
