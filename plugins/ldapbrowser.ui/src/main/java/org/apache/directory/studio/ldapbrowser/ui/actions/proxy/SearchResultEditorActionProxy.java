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
import org.apache.directory.studio.ldapbrowser.ui.editors.searchresult.SearchResultEditorCursor;


// ── CLASS: SearchResultEditorActionProxy — LEIA AS BOUSHH IN THE SEARCH RESULT EDITOR ──
// The third corridor of Jabba's palace — same Boushh disguise, different room.
// In the Search Result Editor the "viewer" is actually a {@link SearchResultEditorCursor},
// a specialised table cursor that tracks the currently focused cell.  Leia-as-Boushh
// needs to hand that cursor to the palace's security system (BrowserActionProxy) so it
// knows which cell is active and can tell the real action whether it's enabled.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A thin proxy that adapts a {@link BrowserAction} for use inside the Search Result
 * Editor, binding it to a {@link SearchResultEditorCursor} as its selection source.
 * The cursor tracks the active cell in the editor's table; the proxy listens to it
 * and forwards selection events to the real action so the action's enablement state
 * stays accurate as the user moves through the results.
 * Think of this as Leia-in-disguise navigating the control room of Jabba's palace —
 * same mission, same costume, but she's holding a cell cursor instead of a blaster.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultEditorActionProxy extends BrowserActionProxy
{

    // ── Boushh Enters The Control Room With A Cursor ──────────────────────────────
    // Leia steps into the control room and plugs the cursor into the security panel.
    // We accept a SearchResultEditorCursor (which extends Viewer) and the real action,
    // then pass them to BrowserActionProxy so it can wire up the selection listener
    // that keeps the action's enabled state in sync with the cursor position.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a SearchResultEditorActionProxy bound to a table cursor.
     * The proxy listens to selection changes fired by the cursor and forwards them
     * to the real action, which uses them to decide whether it's currently enabled.
     *
     * @param cursor  the {@link SearchResultEditorCursor} that tracks the active cell
     *                in the Search Result Editor's table; this is the selection source
     * @param action  the real BrowserAction being wrapped; all run/enabled/text calls
     *                ultimately execute here
     */
    public SearchResultEditorActionProxy( SearchResultEditorCursor cursor, BrowserAction action )
    {
        super( cursor, action );
    }

}
