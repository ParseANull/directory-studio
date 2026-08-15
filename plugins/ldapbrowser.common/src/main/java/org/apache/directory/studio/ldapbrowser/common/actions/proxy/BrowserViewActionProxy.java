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
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: BrowserViewActionProxy — OBI-WAN GUIDES LUKE FROM A DISTANCE ──────
// After his death on the Death Star, Obi-Wan Kenobi continues to guide Luke —
// not by being physically present, but through a distant voice and vision.  In
// The Empire Strikes Back, his holographic form appears on Hoth: "Luke... go to
// the Dagobah system."  He's not in the room, but he's directing the action from
// afar, routing Luke's decisions through the Force.  BrowserViewActionProxy is
// that holographic Obi-Wan: the browser tree view (the Hoth base) holds this
// proxy, but the real intelligence is in the BrowserAction (Obi-Wan in the
// Force) that lives elsewhere.  The proxy just passes the signal through.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete {@link BrowserActionProxy} for use in the LDAP browser tree view.
 *
 * <p>This is a thin subclass of {@link BrowserActionProxy}.  The browser view
 * (the DIT tree, connection list, etc.) creates instances of this class to
 * wire its real {@link BrowserAction} implementations into the Eclipse Action
 * framework.  The viewer acts as the selection provider so selection changes
 * automatically propagate into the proxy and then into the real action.</p>
 *
 * <p>Think of this class as Obi-Wan's holographic apparition: the view holds
 * the hologram (this proxy), but all the wisdom and enablement logic lives in
 * the real action (Obi-Wan in the Force).  The proxy is just the conduit.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserViewActionProxy extends BrowserActionProxy
{

    // ── Obi-Wan's Hologram Appears in the Hoth Base ───────────────────────────
    // Obi-Wan's form materializes in Luke's vision, connected to the specific
    // place (the browser tree viewer) from which Luke will navigate.
    // We hand both the viewer (the "location" in the Force) and the real action
    // (Obi-Wan's actual guidance) up to the parent constructor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code BrowserViewActionProxy} tied to the given viewer
     * and wrapping the given real action.
     *
     * <p>The viewer serves as the {@link org.eclipse.jface.viewers.ISelectionProvider}:
     * any selection change in the browser tree will trigger a
     * {@code selectionChanged} callback on this proxy, which in turn updates
     * the real action's context and re-evaluates the enabled state.</p>
     *
     * <p>For example — Obi-Wan appears to Luke in the Hoth base:</p>
     * <pre>
     *   Obi-Wan's hologram flickers into existence (new BrowserViewActionProxy).
     *   He's tied to Luke's position in the Force (the viewer's selection).
     *   Every time Luke moves, Obi-Wan re-evaluates what to tell him.
     * </pre>
     *
     * @param viewer  the tree or list viewer that provides the current selection;
     *                must not be {@code null}
     * @param action  the real browser action to delegate to; must not be {@code null}
     */
    public BrowserViewActionProxy( Viewer viewer, BrowserAction action )
    {
        super( viewer, action );
    }

}
