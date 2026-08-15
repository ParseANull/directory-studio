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
package org.apache.directory.studio.schemaeditor.controller.actions;


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.views.HierarchyView;
import org.eclipse.jface.action.Action;


// ── CLASS: ShowTypeHierarchyAction — Luke Scanning Both Above And Below The Horizon ─────────
// Luke raises his macrobinoculars to the level horizon, scanning in all directions at once —
// everything above (supertypes), at level (peers), and below (subtypes) from the selected
// element.  This is the "full type hierarchy" mode that shows the selected type centred
// in the inheritance tree with ancestors above and descendants below.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A radio-button action that switches the {@link HierarchyView} into full type hierarchy mode,
 * showing the selected type centred with both its ancestors and descendants visible.
 * It persists the chosen mode to dialog settings so the view remembers the preference
 * between sessions.
 * Think of this as Luke holding his macrobinoculars level — scanning both up and down
 * the inheritance chain simultaneously for the complete picture.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ShowTypeHierarchyAction extends Action
{
    /** The associated view */
    private HierarchyView view;


    // ── Luke Levels The Macrobinoculars And Restores The Setting ──────────────────────────────
    // Luke holds the macrobinoculars level and checks whether this was his last position,
    // restoring that setting from dialog settings if so.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ShowTypeHierarchyAction, restoring the checked state from dialog settings.
     * If the stored preference matches {@code PREFS_HIERARCHY_VIEW_MODE_TYPE}, this radio
     * button starts out checked.
     *
     * <p>For example — Luke restores last session's level-horizon angle:</p>
     * <pre>
     *   Dialog settings: mode = TYPE → action starts checked
     *   View will display the full type hierarchy (ancestors + descendants)
     * </pre>
     *
     * @param view  the HierarchyView this action will refresh when selected
     */
    public ShowTypeHierarchyAction( HierarchyView view )
    {
        super( Messages.getString( "ShowTypeHierarchyAction.TypeAction" ), AS_RADIO_BUTTON ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "ShowTypeHierarchyAction.TypeToolTip" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SHOW_TYPE_HIERARCHY ) );
        setEnabled( true );
        this.view = view;

        // Setting state from the dialog settings
        setChecked( Activator.getDefault().getDialogSettings().getInt( PluginConstants.PREFS_HIERARCHY_VIEW_MODE ) == PluginConstants.PREFS_HIERARCHY_VIEW_MODE_TYPE );
    }


    // ── Luke Levels His View And Refreshes The Full Horizon ───────────────────────────────────
    // Luke commits to the level angle — he saves "TYPE" as his current mode and
    // asks the HierarchyView to rebuild itself showing the complete inheritance context.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Activates full type hierarchy mode in the HierarchyView — saves the mode preference
     * and then asks the view to refresh so it re-renders the complete inheritance tree.
     *
     * <p>For example — Luke levels his view and refreshes the scan:</p>
     * <pre>
     *   Dialog settings updated: mode = TYPE
     *   view.refresh() → HierarchyView redraws with supertypes above, subtypes below
     * </pre>
     */
    public void run()
    {
        Activator.getDefault().getDialogSettings().put( PluginConstants.PREFS_HIERARCHY_VIEW_MODE,
            PluginConstants.PREFS_HIERARCHY_VIEW_MODE_TYPE );

        view.refresh();
    }
}
