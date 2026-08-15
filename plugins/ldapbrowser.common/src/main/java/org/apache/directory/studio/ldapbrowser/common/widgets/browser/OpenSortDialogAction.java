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

package org.apache.directory.studio.ldapbrowser.common.widgets.browser;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;


// ── CLASS: OpenSortDialogAction — Lando Calls a Cloud City Priority Meeting ───
// In The Empire Strikes Back, when Cloud City's operations need reordering, Lando
// doesn't shuffle things around himself — he walks to the intercom, calls his
// administrators into the conference room, and lets the meeting decide the rules.
// This action is that intercom call: clicking the sort toolbar button triggers
// Lando to open the BrowserSorterDialog meeting room where priority rules are set.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A JFace {@link Action} that opens the {@link BrowserSorterDialog} when
 * triggered from the browser widget's toolbar or menu.
 * It carries the sort icon and label so Eclipse can render it correctly,
 * and it holds a reference to the current {@link BrowserPreferences} to pass
 * into the dialog for reading and writing settings.
 * Think of this action as Lando picking up the intercom to call the meeting.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenSortDialogAction extends Action
{

    /** The preferences. */
    private BrowserPreferences preferences;


    // ── LANDO PICKS UP THE INTERCOM ───────────────────────────────────────────
    // Lando grabs the intercom handset and announces the meeting: "Attention all
    // department heads — priority-rules conference, conference room two, now."
    // That announcement has a label ("Sorting") and an icon (the sort glyph)
    // so administrators recognise it on the board. We do the same: configure the
    // action text, icon, and enabled state so it shows up correctly in the toolbar.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the action with the "Sorting" label and sort icon, and
     * enables it immediately so the user can click it right away.
     * We store the {@code preferences} reference so we can pass it into the
     * dialog later — the dialog reads current settings from it and writes
     * changes back to the preference store.
     *
     * @param preferences  The browser preferences object holding the current
     *                     sort configuration. Passed through to the dialog on
     *                     {@link #run()}.
     */
    public OpenSortDialogAction( BrowserPreferences preferences )
    {
        super(
            Messages.getString( "OpenSortDialogAction.Sorting" ), BrowserCommonActivator.getDefault().getImageDescriptor( BrowserCommonConstants.IMG_SORT ) ); //$NON-NLS-1$
        super.setEnabled( true );

        this.preferences = preferences;
    }


    // ── LANDO OPENS THE CONFERENCE ROOM DOOR ──────────────────────────────────
    // The intercom call goes out, the administrators file into the room, and the
    // door swings open. Here, "opening the room" means creating a fresh
    // BrowserSorterDialog and calling open() on it. It's modal — Lando stands
    // at the door until the meeting ends (the user clicks OK or Cancel).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates and opens the {@link BrowserSorterDialog} centred on the
     * current active shell.
     * The dialog is modal — this call blocks until the user dismisses it.
     * Any preference changes the user makes are written to the store inside
     * the dialog's {@code buttonPressed} method.
     */
    @Override
    public void run()
    {
        BrowserSorterDialog dlg = new BrowserSorterDialog( PlatformUI.getWorkbench().getDisplay().getActiveShell(),
            preferences );
        dlg.open();
    }


    // ── LANDO STEPS DOWN FROM HIS ROLE ────────────────────────────────────────
    // When Cloud City no longer needs this particular intercom line, Lando hangs
    // up the handset and walks away. We null out the preferences reference so
    // the garbage collector can reclaim it — keeping a stale reference to the
    // preferences after the widget is gone would be a memory leak.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Releases the reference to the browser preferences so this action can be
     * garbage collected cleanly.
     * Call this when the owning browser widget is disposed, so we don't keep
     * a live reference to a preferences object whose widget is gone.
     */
    public void dispose()
    {
        preferences = null;
    }

}
