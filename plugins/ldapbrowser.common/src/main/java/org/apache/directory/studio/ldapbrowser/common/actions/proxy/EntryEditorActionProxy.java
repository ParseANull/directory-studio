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

package org.apache.directory.studio.ldapbrowser.common.actions.proxy;


import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: EntryEditorActionProxy — AHSOKA RELAYS ANAKIN'S ORDERS ─────────────
// During the Clone Wars, Ahsoka Tano serves as Anakin Skywalker's Padawan and
// field liaison.  Anakin decides the strategy; Ahsoka relays the specific orders
// to the clone battalion on the ground — interpreting the context (which troopers
// are in front of her, what's happening on the battlefield) and ensuring the
// right commands reach the right units at the right time.  She also explicitly
// implements ISelectionChangedListener, meaning she's directly wired into the
// battlefield feed and responds the moment the situation shifts.
// EntryEditorActionProxy does the same thing for the entry editor: it wraps the
// real BrowserAction (Anakin's strategy), watches the entry editor's selection
// (the battlefield), and routes the command through when the user acts.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete {@link BrowserActionProxy} for use in the LDAP entry editor.
 *
 * <p>This subclass is nearly identical to {@link BrowserViewActionProxy} but is
 * specifically for the entry editor panel (the table of attributes and values).
 * By also implementing {@link ISelectionChangedListener} explicitly, this class
 * signals that it is directly registered as a selection listener — not just
 * through the viewer's selection service — so selection events in the entry
 * editor's attribute table flow into this proxy and update the real action's
 * context correctly.</p>
 *
 * <p>Think of this class as Ahsoka relaying Anakin's orders: she's wired
 * directly into the clone battalion's comms (explicit
 * {@code ISelectionChangedListener} implementation), and the moment the
 * battlefield changes she routes the updated command through.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorActionProxy extends BrowserActionProxy implements ISelectionChangedListener
{
    // ── Ahsoka Takes Her Position With the Battalion ──────────────────────────
    // Ahsoka steps into the command relay position: the right viewer (the entry
    // editor's table), the right orders (the real BrowserAction).  From this
    // moment, she's listening and ready to forward commands.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code EntryEditorActionProxy} tied to the given viewer
     * and wrapping the given real action.
     *
     * <p>The viewer is the entry editor's attribute-value table; it provides
     * the selection context that determines whether actions like "New Value,"
     * "Delete," or "Copy" are enabled for the currently highlighted row.</p>
     *
     * <p>For example — Ahsoka takes her position beside the battalion:</p>
     * <pre>
     *   Ahsoka arrives at the forward command post (new EntryEditorActionProxy).
     *   She's tied directly to the clone unit's status feed (the viewer).
     *   The moment a trooper's situation changes, she relays Anakin's order.
     * </pre>
     *
     * @param viewer  the entry editor viewer that provides the current selection;
     *                must not be {@code null}
     * @param action  the real browser action to delegate to; must not be {@code null}
     */
    public EntryEditorActionProxy( Viewer viewer, BrowserAction action )
    {
        super( viewer, action );
    }
}
