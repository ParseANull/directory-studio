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

package org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.common.actions.proxy.EntryEditorActionProxy;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;


// ── CLASS: OpenDefaultEditorAction — R2-D2 Defaults to the Reliable System ──
// In The Empire Strikes Back, R2-D2 arrives at Cloud City and discovers the
// Millennium Falcon's hyperdrive has been sabotaged. Without hesitation R2
// plugs into the nearest computer port and switches to his most reliable
// diagnostic subroutine — the one that always works, the safe default. That's
// this class: it wraps the best-value-editor proxy and exposes it as the
// "Edit Value" command, the reliable go-to action the user reaches for every
// time they want to edit an LDAP attribute value.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The default "Edit Value" action for the entry editor toolbar and context
 * menu. It delegates entirely to a {@link EntryEditorActionProxy} that wraps
 * the best-value-editor action — so in practice it opens the same editor that
 * {@link OpenBestEditorAction} would, but it's registered under the well-known
 * platform command ID {@code ACTION_ID_EDIT_VALUE} so it can be triggered from
 * the keyboard shortcut and other platform-level hooks.
 *
 * <p>Think of this class as R2-D2 routing everything through the most reliable
 * available system. The proxy is R2's go-to subroutine; this class is the
 * button that triggers it.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenDefaultEditorAction extends BrowserAction
{

    /** The best value editor proxy. */
    private EntryEditorActionProxy bestValueEditorProxy;


    // ── R2-D2 Powers Up His Reliable Editor Subroutine ──────────────────────────
    // R2 rolls up to the terminal, identifies which subroutine to load — the best
    // value editor proxy — and stores a reference so every subsequent call can
    // reach it instantly. The viewer parameter is accepted for consistency with
    // the superclass constructor signature but isn't used directly here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the default editor action backed by the given proxy. We store the
     * proxy reference so every method in this class can delegate to it. There's no
     * extra setup needed — the proxy already knows how to evaluate its own state.
     *
     * <p>For example — R2 connects to his most reliable subsystem:</p>
     * <pre>
     *   this.bestValueEditorProxy = bestValueEditorProxy
     *   // Ready to respond to getText(), isEnabled(), run()
     * </pre>
     *
     * @param viewer               The tree viewer for the entry editor; accepted for
     *                             API consistency but not stored or used here.
     * @param bestValueEditorProxy The proxy wrapping the best-value-editor action;
     *                             every call on this action is forwarded to it.
     */
    public OpenDefaultEditorAction( TreeViewer viewer, EntryEditorActionProxy bestValueEditorProxy )
    {
        this.bestValueEditorProxy = bestValueEditorProxy;
    }


    // ── R2-D2 Shuts Down Cleanly After the Mission ──────────────────────────────
    // With the Falcon's hyperdrive repaired and the crew safely away, R2 powers
    // down his repair subroutine and clears his working memory. We null the proxy
    // reference here so nothing holds a stale pointer once the editor closes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Clears the proxy reference and delegates further cleanup to the superclass.
     * Call this when the entry editor widget is disposed — skipping it leaves a
     * dangling reference to the proxy that could prevent garbage collection.
     *
     * <p>For example — R2 wraps up and powers down:</p>
     * <pre>
     *   bestValueEditorProxy = null  → release the reference
     *   super.dispose()              → BrowserAction cleanup
     * </pre>
     *
     * @see org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction#dispose()
     */
    public void dispose()
    {
        bestValueEditorProxy = null;
        super.dispose();
    }


    // ── R2-D2 Returns the Standard Command Identifier ───────────────────────────
    // R2's standard "edit value" subroutine is registered under a well-known ID
    // in the Rebel Alliance's command registry, so any part of the system can
    // invoke it by name without knowing who implemented it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse platform command ID that binds this action to keyboard
     * shortcuts and other platform-level invocations. We return
     * {@link BrowserCommonConstants#ACTION_ID_EDIT_VALUE} so the "Edit Value"
     * keyboard shortcut wired in plugin.xml triggers this action.
     *
     * <p>For example — R2's beep-code for "start editing":</p>
     * <pre>
     *   return BrowserCommonConstants.ACTION_ID_EDIT_VALUE
     *   // maps to the keybinding defined in plugin.xml
     * </pre>
     *
     * @return  The platform command ID string for "Edit Value."
     * @see org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction#getCommandId()
     */
    public String getCommandId()
    {
        return BrowserCommonConstants.ACTION_ID_EDIT_VALUE;
    }


    // ── R2-D2 Pulls Up the Proxy's Icon ─────────────────────────────────────────
    // R2 checks the face of his reliable subroutine — what icon should appear on
    // the button? He asks the proxy, which asks the best editor, which supplies
    // the right image for the current value type.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the toolbar button and context menu entry.
     * We delegate to the proxy, which in turn fetches the icon from whichever
     * concrete editor the proxy currently wraps. Returns {@code null} if the proxy
     * hasn't been initialized or has already been disposed.
     *
     * <p>For example — R2 asks his subroutine for the right icon:</p>
     * <pre>
     *   bestValueEditorProxy != null
     *     → return bestValueEditorProxy.getImageDescriptor()
     *   else → return null  (no icon while proxy is absent)
     * </pre>
     *
     * @return  The proxy's image descriptor, or {@code null} if unavailable.
     * @see org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction#getImageDescriptor()
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


    // ── R2-D2 Displays the Standard "Edit Value" Label ──────────────────────────
    // R2's beep translates to a clear, predictable label: "Edit Value." Users
    // know exactly what this button does — no guesswork, no surprise. The text
    // comes from the resource bundle so it can be localized.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display label for this action — always the localized string for
     * "Edit Value." This is the text shown in the context menu item and toolbar
     * tooltip. It's intentionally stable and generic, unlike
     * {@link OpenBestEditorAction#getText()} which returns the specific editor's name.
     *
     * <p>For example — R2's reliable label never changes:</p>
     * <pre>
     *   Messages.getString("OpenDefaultEditorAction.EditValue")
     *   → "Edit Value"   (English)
     *   → "Wert bearbeiten"  (German, if locale is set)
     * </pre>
     *
     * @return  The localized "Edit Value" label string.
     * @see org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction#getText()
     */
    public String getText()
    {
        return Messages.getString( "OpenDefaultEditorAction.EditValue" ); //$NON-NLS-1$
    }


    // ── R2-D2 Checks If His Reliable System Is Ready ────────────────────────────
    // R2 runs a quick self-check: is the proxy loaded and operational? If the
    // proxy says it can run, R2 says yes. If the proxy is missing or says no,
    // R2 beeps apologetically and the button greys out.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the action can currently execute. We ask the proxy — if the
     * proxy says it's enabled (meaning the best-editor action is ready to go),
     * we return {@code true}. If the proxy is null or disabled, we return
     * {@code false} and the toolbar button greys out.
     *
     * <p>For example — R2 checks his subroutine status before committing:</p>
     * <pre>
     *   bestValueEditorProxy != null && bestValueEditorProxy.isEnabled()
     *   → true  → button is active
     *   otherwise → false → button is greyed
     * </pre>
     *
     * @return  {@code true} if the proxy exists and reports itself enabled;
     *          {@code false} otherwise.
     * @see org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction#isEnabled()
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


    // ── R2-D2 Executes the Reliable Edit Operation ──────────────────────────────
    // R2 has done the check, the proxy is ready, and R2 simply executes: he
    // forwards the run() call to the proxy, which opens the best editor in the
    // tree's cell editor slot. Simple, reliable, no drama.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the edit-value operation by forwarding to the proxy. We guard
     * with a null and enabled check — if the proxy has been cleared or isn't
     * ready, we do nothing rather than throw.
     *
     * <p>For example — R2 fires his reliable repair subroutine:</p>
     * <pre>
     *   bestValueEditorProxy != null && bestValueEditorProxy.isEnabled()
     *   → bestValueEditorProxy.run()  → cell editor opens in the tree
     * </pre>
     *
     * @see org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction#run()
     */
    public void run()
    {
        if ( bestValueEditorProxy != null && bestValueEditorProxy.isEnabled() )
        {
            bestValueEditorProxy.run();
        }
    }

}
