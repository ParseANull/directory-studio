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


// ── CLASS: ModificationLogsViewActionProxy — LEIA AS BOUSHH THE BOUNTY HUNTER ──
// When Leia infiltrated Jabba's palace to rescue Han Solo, she didn't walk in as
// Princess Leia — she came in disguise as Boushh the bounty hunter, delivering
// Chewbacca.  Every command Boushh gave was quietly on Leia's behalf: the name
// at the door was different, but the real actor was Leia underneath.
// ModificationLogsViewActionProxy works the same way: Eclipse interacts with this
// proxy object, but every action it triggers is actually forwarded to the real
// BrowserAction underneath the disguise.  The proxy handles viewer-context plumbing
// specific to the Modification Logs View.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A thin proxy that adapts a {@link BrowserAction} for use inside the Modification
 * Logs View, handling the viewer-context binding that the view requires.
 * Eclipse registers this proxy with the view's action bar; when the user triggers
 * the action, the proxy forwards the call to the real underlying BrowserAction.
 * Think of this class as Leia in the Boushh costume — the exterior is the proxy,
 * but everything of substance comes from the real action inside.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ModificationLogsViewActionProxy extends BrowserActionProxy
{

    // ── Leia Puts On The Boushh Costume ─────────────────────────────────────────
    // Leia steps into the bounty hunter armour, visor down, thermal detonator ready.
    // She needs two things: the palace context (the viewer) and the mission objective
    // (the real action).  Our constructor takes both and passes them to BrowserActionProxy,
    // which wires the selection-listener plumbing between the viewer and the real action.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a ModificationLogsViewActionProxy that forwards to the given real action.
     * The proxy listens to selection changes on the provided viewer and forwards those
     * events to the real action so it can update its enablement state correctly.
     *
     * @param viewer  the Viewer from the Modification Logs View — provides the selection
     *                context that the underlying action needs to evaluate isEnabled()
     * @param action  the real BrowserAction to wrap; all run/enabled/text calls are
     *                delegated here after the proxy handles viewer-context setup
     */
    public ModificationLogsViewActionProxy( Viewer viewer, BrowserAction action )
    {
        super( viewer, action );
    }

}
