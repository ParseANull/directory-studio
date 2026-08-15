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


// ── CLASS: ShowSupertypeHierarchyAction — Luke Tilting The Macrobinoculars Upward ───────────
// Luke raises his macrobinoculars toward the horizon — scanning up the inheritance chain
// to see where the selected type came from, its parents and grandparents in the schema.
// This action switches the HierarchyView into "supertype" mode, revealing the ancestors
// that the selected type was derived from.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A radio-button action that switches the {@link HierarchyView} into supertype hierarchy mode,
 * showing the ancestors (supertypes) of the currently focused type.
 * It persists the chosen mode to dialog settings so the view remembers the preference
 * between sessions.
 * Think of this as Luke tilting his macrobinoculars upward to scan the inheritance chain
 * above the selected type — seeing where it came from, not where it leads.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ShowSupertypeHierarchyAction extends Action
{
    /** The associated view */
    private HierarchyView view;


    // ── Luke Tilts The Macrobinoculars Up And Restores The Angle ─────────────────────────────
    // Luke lifts the macrobinoculars and checks whether he last left them tilted upward
    // (supertype mode), restoring that angle from dialog settings if so.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ShowSupertypeHierarchyAction, restoring the checked state from dialog settings.
     * If the stored preference matches {@code PREFS_HIERARCHY_VIEW_MODE_SUPERTYPE}, this radio
     * button starts out checked.
     *
     * <p>For example — Luke restores last session's upward tilt:</p>
     * <pre>
     *   Dialog settings: mode = SUPERTYPE → action starts checked
     *   View will display supertypes (ancestors) when first loaded
     * </pre>
     *
     * @param view  the HierarchyView this action will refresh when selected
     */
    public ShowSupertypeHierarchyAction( HierarchyView view )
    {
        super( Messages.getString( "ShowSupertypeHierarchyAction.SupertypeAction" ), AS_RADIO_BUTTON ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "ShowSupertypeHierarchyAction.SupertypeToolTip" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SHOW_SUPERTYPE_HIERARCHY ) );
        setEnabled( true );
        this.view = view;

        // Setting state from the dialog settings
        setChecked( Activator.getDefault().getDialogSettings().getInt( PluginConstants.PREFS_HIERARCHY_VIEW_MODE ) == PluginConstants.PREFS_HIERARCHY_VIEW_MODE_SUPERTYPE );
    }


    // ── Luke Locks In The Upward Angle And Refreshes The View ────────────────────────────────
    // Luke commits to looking upward — he saves "SUPERTYPE" as his current mode and
    // asks the HierarchyView to rebuild itself around the ancestors of the selected type.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Activates supertype mode in the HierarchyView — saves the mode preference to dialog settings
     * and then asks the view to refresh so it re-renders the tree in ancestor perspective.
     *
     * <p>For example — Luke tilts up and refreshes the scan:</p>
     * <pre>
     *   Dialog settings updated: mode = SUPERTYPE
     *   view.refresh() → HierarchyView redraws showing parent/ancestor types
     * </pre>
     */
    public void run()
    {
        Activator.getDefault().getDialogSettings().put( PluginConstants.PREFS_HIERARCHY_VIEW_MODE,
            PluginConstants.PREFS_HIERARCHY_VIEW_MODE_SUPERTYPE );

        view.refresh();
    }
}
