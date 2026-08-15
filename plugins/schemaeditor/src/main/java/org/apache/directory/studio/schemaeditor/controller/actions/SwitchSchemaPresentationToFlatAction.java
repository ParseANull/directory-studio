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


// ── CLASS: SwitchSchemaPresentationToFlatAction — Clone Trooper Levels The Formation ────────
// Order 66 comes through and the clone troopers break from their ranked formation into
// a flat line — every trooper side-by-side, same level, no hierarchy among them.
// This action switches the SchemaView from a nested, hierarchical display into a flat list
// where all elements appear at the same depth, sorted but not nested.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A radio-button action that switches the schema view's presentation mode to "flat,"
 * where all schema elements are shown in a single-level list rather than nested by schema.
 * The selected mode is written to the preference store so it persists across workbench restarts.
 * Think of this as the clone troopers breaking formation into a flat line — same elements,
 * same data, but arranged without the hierarchical grouping.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SwitchSchemaPresentationToFlatAction extends Action implements IWorkbenchWindowActionDelegate
{
    // ── Trooper Breaks Formation Into Flat Line ───────────────────────────────────────────────
    // The clone trooper receives the new standing order and checks whether "flat formation"
    // was the last active arrangement — if so, he marks himself as already checked (active).
    // Note: the action starts disabled; it's enabled by the controller once a project is open.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SwitchSchemaPresentationToFlatAction, checking the preference store to
     * restore the previously chosen presentation mode.
     * The action starts disabled — it's enabled by the controller only after a project is loaded,
     * because there's nothing to present until there's a schema to show.
     *
     * <p>For example — the trooper checks if flat formation was the last order:</p>
     * <pre>
     *   Preference store: FLAT → action starts checked (flat mode active)
     *   Preference store: HIERARCHICAL → action starts unchecked
     * </pre>
     */
    public SwitchSchemaPresentationToFlatAction()
    {
        super( Messages.getString( "SwitchSchemaPresentationToFlatAction.FlatAction" ), AS_RADIO_BUTTON ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "SwitchSchemaPresentationToFlatAction.FlatToolTip" ) ); //$NON-NLS-1$
        setEnabled( false );

        // Setting up the state of the action
        if ( Activator.getDefault().getPreferenceStore().getInt( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION ) == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_FLAT )
        {
            setChecked( true );
        }
    }


    // ── Trooper Steps Out Of Ranks Into The Flat Line ─────────────────────────────────────────
    // The trooper executes the order — he saves "FLAT" to the preference store,
    // which triggers the schema view to redraw itself in the new formation.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Writes the flat presentation mode to the preference store.
     * The schema view listens for preference changes and redraws itself automatically
     * when it sees this value change, so we don't need to call refresh() explicitly.
     *
     * <p>For example — the trooper files the new formation order:</p>
     * <pre>
     *   Preference store: SCHEMA_PRESENTATION = FLAT
     *   SchemaView preference listener fires → view redraws as flat list
     * </pre>
     */
    public void run()
    {
        Activator.getDefault().getPreferenceStore().setValue( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION,
            PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_FLAT );
    }


    // ── Trooper Relays The Order Through The Workbench Channel ───────────────────────────────
    // When Eclipse fires this action through the workbench delegate path,
    // the trooper passes it straight to run() — same order, same execution.
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
    // The window assignment is noted; we need nothing from it since all context is
    // in the preference store.
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
