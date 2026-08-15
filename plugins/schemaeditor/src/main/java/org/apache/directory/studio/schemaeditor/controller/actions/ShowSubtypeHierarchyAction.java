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


// ── CLASS: ShowSubtypeHierarchyAction — Luke Tilting The Macrobinoculars Downward ───────────
// Luke stands on the Tatooine ridge and tilts his macrobinoculars down to look at
// everything below him — the smaller, more specialised elements that descended from
// the thing he's studying.  This action switches the HierarchyView into "subtype" mode,
// showing all the children and grandchildren that inherit from the selected type.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A radio-button action that switches the {@link HierarchyView} into subtype hierarchy mode,
 * showing the elements that inherit from (are subtypes of) the currently focused type.
 * It persists the chosen mode to dialog settings so the view remembers the preference
 * between sessions.
 * Think of this as Luke tilting his view downward to scan everything below the selected
 * type in the inheritance tree.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ShowSubtypeHierarchyAction extends Action
{
    /** The associated view */
    private HierarchyView view;


    // ── Luke Picks Up The Macrobinoculars And Tilts Them Down ────────────────────────────────
    // Luke picks up his macrobinoculars and checks whether he left them tilted down
    // (subtype mode) last time, restoring that position if so.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ShowSubtypeHierarchyAction, restoring the checked state from dialog settings.
     * If the stored preference matches {@code PREFS_HIERARCHY_VIEW_MODE_SUBTYPE}, this radio
     * button starts out checked.
     *
     * <p>For example — Luke restores the tilt angle from last session:</p>
     * <pre>
     *   Dialog settings: mode = SUBTYPE → action starts checked
     *   View will display subtypes when first loaded
     * </pre>
     *
     * @param view  the HierarchyView this action will refresh when selected
     */
    public ShowSubtypeHierarchyAction( HierarchyView view )
    {
        super( Messages.getString( "ShowSubtypeHierarchyAction.SubtypeAction" ), AS_RADIO_BUTTON ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "ShowSubtypeHierarchyAction.SubtypeToolTip" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SHOW_SUBTYPE_HIERARCHY ) );
        setEnabled( true );
        this.view = view;

        // Setting state from the dialog settings
        setChecked( Activator.getDefault().getDialogSettings().getInt( PluginConstants.PREFS_HIERARCHY_VIEW_MODE ) == PluginConstants.PREFS_HIERARCHY_VIEW_MODE_SUBTYPE );
    }


    // ── Luke Locks The Tilt Angle And Refreshes The View ─────────────────────────────────────
    // Luke commits to looking downward — he saves "SUBTYPE" as his current mode and
    // tells the HierarchyView to rebuild itself around that perspective.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Activates subtype mode in the HierarchyView — saves the mode preference to dialog settings
     * and then asks the view to refresh so it re-renders the tree in subtype perspective.
     *
     * <p>For example — Luke tilts down and refreshes the scan:</p>
     * <pre>
     *   Dialog settings updated: mode = SUBTYPE
     *   view.refresh() → HierarchyView redraws showing children/subtypes
     * </pre>
     */
    public void run()
    {
        Activator.getDefault().getDialogSettings().put( PluginConstants.PREFS_HIERARCHY_VIEW_MODE,
            PluginConstants.PREFS_HIERARCHY_VIEW_MODE_SUBTYPE );

        view.refresh();
    }
}
