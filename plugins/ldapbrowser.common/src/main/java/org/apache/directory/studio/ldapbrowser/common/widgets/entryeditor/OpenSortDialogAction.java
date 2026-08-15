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


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;


// ── CLASS: OpenSortDialogAction — Lando Calls the Cloud City Scheduling Meeting
// In The Empire Strikes Back, Lando Calrissian runs Cloud City and keeps its
// tibanna gas mining operation orderly by calling scheduling meetings. He opens
// the operations room, the team gathers, and they arrange the mining runs in the
// right sequence. That's this class: clicking the sort button is Lando calling
// the meeting — it opens the sort dialog where the user decides in what order
// the LDAP attributes should appear in the entry editor.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Toolbar and menu action that opens the entry editor's sort configuration
 * dialog. Users reach for this when they want to control the order in which
 * LDAP attributes appear in the tree — alphabetical, by schema type, or some
 * custom arrangement.
 *
 * <p>Think of this class as Lando Calrissian pressing the "convene meeting"
 * button in Cloud City: one click and the {@link EntryEditorWidgetSorterDialog}
 * opens so everything can be arranged properly.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenSortDialogAction extends Action
{
    private EntryEditorWidgetPreferences preferences;


    // ── Lando Sets Up the Scheduling Room Before the Meeting ────────────────────
    // Lando strides into the operations room ahead of the team: he hangs the agenda
    // on the wall (sets the button text), puts the Cloud City crest on the door
    // (sets the icon), and confirms the room is available (setEnabled(true)).
    // Everything is ready before anyone walks in.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Configures this action's label, tooltip, and icon, then stores the
     * preferences object that the sort dialog will read and write. After
     * construction, the action is immediately enabled and ready to open the dialog.
     *
     * <p>For example — Lando preps the scheduling room:</p>
     * <pre>
     *   setText("Sorting")           → agenda label on the wall
     *   setToolTipText("Sorting")    → tooltip on the Cloud City door
     *   setImageDescriptor(IMG_SORT) → Cloud City crest on the button
     *   setEnabled(true)             → room is open and ready
     * </pre>
     *
     * @param preferences  The preferences object that holds the current sort
     *                     settings; the dialog will read from and write back to
     *                     this object when the user confirms their choices.
     */
    public OpenSortDialogAction( EntryEditorWidgetPreferences preferences )
    {
        setText( Messages.getString( "OpenSortDialogAction.Sorting" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "OpenSortDialogAction.Sorting" ) ); //$NON-NLS-1$
        setImageDescriptor( BrowserCommonActivator.getDefault().getImageDescriptor(
            BrowserCommonConstants.IMG_SORT ) );
        setEnabled( true );

        this.preferences = preferences;
    }


    // ── Lando Calls the Meeting to Order ────────────────────────────────────────
    // "Welcome to Cloud City," Lando says, throwing open the operations-room doors.
    // The team files in, the holographic display flickers on, and they start
    // arranging the mining schedule. We open the EntryEditorWidgetSorterDialog
    // on the active shell so the user can configure the sort order.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the sort configuration dialog for the entry editor. The dialog lets
     * the user choose how attributes are ordered in the tree — for instance,
     * mandatory attributes first, then optional ones, all sorted alphabetically
     * within each group. The dialog reads the current settings from our
     * {@code preferences} object and writes any changes back when the user clicks
     * OK.
     *
     * <p>For example — Lando opens the scheduling session:</p>
     * <pre>
     *   new EntryEditorWidgetSorterDialog(activeShell, preferences)
     *   dlg.open()  → user arranges the sort order
     *   // on OK → preferences are updated; viewer refreshes on next paint
     * </pre>
     */
    public void run()
    {
        EntryEditorWidgetSorterDialog dlg = new EntryEditorWidgetSorterDialog( PlatformUI.getWorkbench().getDisplay()
            .getActiveShell(), preferences );
        dlg.open();
    }
}
