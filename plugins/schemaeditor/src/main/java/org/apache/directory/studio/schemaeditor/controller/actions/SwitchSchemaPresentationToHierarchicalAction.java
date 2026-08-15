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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


// ── CLASS: SwitchSchemaPresentationToHierarchicalAction — Clone Troopers Reform Ranked Ranks ─
// After the flat formation order, a new command comes through: reform into ranked columns,
// each squad nested under its battalion, each battalion under its legion.
// This action switches the SchemaView from a flat list back into the hierarchical tree,
// where elements are grouped by schema and nested under their parent schemas.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A radio-button action that switches the schema view's presentation mode to "hierarchical,"
 * where schema elements are grouped and nested within their parent schema containers.
 * The selected mode is written to the preference store so it persists across workbench restarts.
 * Think of this as the clone troopers reforming into ranked columns — the same elements
 * now visually arranged to show their belonging and nesting within each schema.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SwitchSchemaPresentationToHierarchicalAction extends Action implements IWorkbenchWindowActionDelegate
{
    // ── Trooper Reforms Into Ranked Columns ───────────────────────────────────────────────────
    // The clone trooper receives the reform order and checks whether "ranked formation"
    // was already the active arrangement — if so, he marks himself as already checked (active).
    // Like the flat action, this starts disabled until a project is open.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SwitchSchemaPresentationToHierarchicalAction, checking the preference store
     * to restore the previously chosen presentation mode.
     * The action starts disabled — it's enabled by the controller only after a project is loaded.
     *
     * <p>For example — the trooper checks if hierarchical formation was the last order:</p>
     * <pre>
     *   Preference store: HIERARCHICAL → action starts checked (hierarchical mode active)
     *   Preference store: FLAT → action starts unchecked
     * </pre>
     */
    public SwitchSchemaPresentationToHierarchicalAction()
    {
        super( Messages.getString( "SwitchSchemaPresentationToHierarchicalAction.HierarchicalAction" ), AS_RADIO_BUTTON ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "SwitchSchemaPresentationToHierarchicalAction.HierarchicalToolTip" ) ); //$NON-NLS-1$
        setEnabled( false );

        // Setting up the state of the action
        if ( Activator.getDefault().getPreferenceStore().getInt( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION ) == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_HIERARCHICAL )
        {
            setChecked( true );
        }
    }


    // ── Trooper Steps Back Into The Ranked Column Formation ───────────────────────────────────
    // The trooper executes the reform — saves "HIERARCHICAL" to the preference store,
    // which triggers the schema view to redraw itself with elements nested under schemas.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Writes the hierarchical presentation mode to the preference store.
     * The schema view listens for preference changes and redraws automatically,
     * so no explicit refresh call is needed here.
     *
     * <p>For example — the trooper files the ranked-formation order:</p>
     * <pre>
     *   Preference store: SCHEMA_PRESENTATION = HIERARCHICAL
     *   SchemaView preference listener fires → view redraws as nested tree
     * </pre>
     */
    public void run()
    {
        Activator.getDefault().getPreferenceStore().setValue( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION,
            PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_HIERARCHICAL );
    }


    // ── Trooper Relays The Order Through The Workbench Channel ───────────────────────────────
    // When Eclipse fires this action through the workbench delegate path,
    // the trooper passes it straight to run() — same reform order, same execution.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} when triggered via the workbench action delegate path.
     *
     * @param action  the workbench action proxy; unused
     */
    public void run( IAction action )
    {
        run();
    }


    // ── Trooper Stands Down, Nothing To Clean Up ──────────────────────────────────────────────
    // Formation dismissed — no resources held, nothing to release.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes this action's resources.
     * We hold nothing requiring explicit cleanup, so this is intentionally empty.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Trooper Acknowledges Window Assignment ────────────────────────────────────────────────
    // The window is noted but we need nothing from it — all context is in the preference store.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action is bound to a workbench window.
     * We need nothing from the window, so this is intentionally empty.
     *
     * @param window  the workbench window; unused
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── Trooper Ignores Workbench Selection Broadcasts ───────────────────────────────────────
    // This presentation-mode toggle doesn't change with selection — it's about how the view
    // is laid out, not what's selected in it.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench selection changes.
     * This action doesn't react to selection changes, so this is intentionally empty.
     *
     * @param action     the workbench action proxy; unused
     * @param selection  the current workbench selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
