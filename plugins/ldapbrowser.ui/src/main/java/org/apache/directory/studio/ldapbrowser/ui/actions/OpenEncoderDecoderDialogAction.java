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

package org.apache.directory.studio.ldapbrowser.ui.actions;


import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.ui.dialogs.EncoderDecoderDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;


// ── CLASS: OpenEncoderDecoderDialogAction — LEIA'S HOLOGRAM MESSAGE ──────────
// Leia recorded a holographic message — "Help me, Obi-Wan Kenobi, you're my only
// hope" — and R2-D2 projected it into the room, then waited while Obi-Wan processed
// what he'd seen.  The EncoderDecoder dialog works the same way: this action
// projects the dialog into the workspace and waits for the user to interact with it.
// The dialog itself lets users convert attribute values between encodings (Base64,
// UTF-8, hex, etc.), so it's a translation device — a holographic projector for data.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the Encoder/Decoder dialog, a utility popup for converting LDAP attribute
 * values between different encodings (Base64, hex, UTF-8, and more).
 * It's useful when you have a binary attribute value and need to inspect or modify
 * it as human-readable text, or vice versa.
 * Think of this class as R2-D2 projecting Leia's hologram: it simply activates the
 * dialog and steps back while the user does the important work.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenEncoderDecoderDialogAction extends BrowserAction
{
    // ── R2 Powers Up The Hologram Projector ─────────────────────────────────────
    // Before R2 can project Leia's message he has to spin up his holographic
    // projector dome.  Our constructor calls super() to initialise the BrowserAction
    // machinery — connection to the selection framework, plugin context, all of it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OpenEncoderDecoderDialogAction, wired into the BrowserAction
     * framework.
     * The parent class sets up selection listeners; we just initialise ourselves.
     */
    public OpenEncoderDecoderDialogAction()
    {
        super();
    }


    // ── R2 Projects The Hologram ────────────────────────────────────────────────
    // R2 extends the projector, Leia's image flickers into existence, and the room
    // fills with her message.  run() does the same: it instantiates the
    // EncoderDecoderDialog on the active shell and calls open() to show it.
    // The dialog is non-blocking by default — the user can keep the workspace active
    // while they use the encoder/decoder as a helper tool.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the Encoder/Decoder dialog on the currently active shell.
     * The dialog stays open until the user explicitly closes it, letting them
     * repeatedly encode or decode values without reopening it each time.
     */
    public void run()
    {
        EncoderDecoderDialog dlg = new EncoderDecoderDialog( PlatformUI.getWorkbench().getDisplay().getActiveShell() );
        dlg.open();
    }


    // ── Hologram Carries A Title ─────────────────────────────────────────────────
    // Leia's message had a spoken title: "General Kenobi…"  Our getText() is the
    // printed title of this action in the menu — the name the user clicks.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised display label for this action.
     *
     * @return the menu-item text, e.g. "Open Encoder/Decoder"
     */
    public String getText()
    {
        return Messages.getString( "OpenEncoderDecoderDialogAction.OpenEndoderDecoder" ); //$NON-NLS-1$
    }


    // ── No Icon On The Projector ─────────────────────────────────────────────────
    // R2's projector was identified by context, not a sticker on the side.
    // This action has no dedicated toolbar icon so we return null.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for this action's icon.
     * No dedicated icon is registered, so null is returned and Eclipse will show
     * a text-only menu item.
     *
     * @return always null
     */
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── No Standard Broadcast Code ───────────────────────────────────────────────
    // Leia's personal message wasn't a standard protocol transmission — it was
    // ad-hoc.  This action has no global Eclipse command ID.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for keybinding.
     * No global command is registered for this action, so null is returned.
     *
     * @return always null
     */
    public String getCommandId()
    {
        return null;
    }


    // ── Projector Is Always Ready ────────────────────────────────────────────────
    // R2's holographic projector doesn't need any particular precondition — he can
    // play the message any time.  Opening the encoder/decoder has no selection
    // requirement, so we always return true.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reports whether this action is available.
     * The encoder/decoder dialog is always openable regardless of selection state.
     *
     * @return always true
     */
    public boolean isEnabled()
    {
        return true;
    }
}
