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
package org.apache.directory.studio.schemaeditor.view.views;


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.controller.ProjectsViewController;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;


// ── CLASS: ProjectsView — Lando Running Cloud City ───────────────────────────
// Lando Calrissian stands in Cloud City's operations center, overseeing every
// active operation from a single command room. Projects flow in and out; he
// knows which are open, which are closed, which need attention. He doesn't do
// the detailed work himself — he delegates to the right people. But this room
// is where you come to see everything at once.
// ProjectsView is that operations center: a single Eclipse ViewPart that shows
// all schema-editor projects in a table, wires up the content and label
// providers, and delegates interaction logic to the ProjectsViewController.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Projects View — the top-level Eclipse view where users see and manage all their
 * schema-editor projects. A "project" in our world is either an offline schema (you
 * have schema files locally and aren't connected to a live LDAP server) or an online
 * schema (you're connected to a live server and editing its schema directly).
 * This class creates the SWT table widget, wires it to its content and label providers,
 * and installs a {@link ProjectsViewController} to handle the real interaction logic.
 * Think of it as Lando's command room: the place where the full picture is visible
 * and the right people are put in charge of each part.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ProjectsView extends ViewPart
{
    /** The ID of the View */
    public static final String ID = PluginConstants.VIEW_PROJECTS_VIEW_ID;

    /** The viewer */
    private TableViewer tableViewer;


    // ── Lando Opens the Operations Center ───────────────────────────────────
    // The moment Cloud City goes operational, Lando assigns posts: Lobot at
    // the main console, security at the perimeter, comms on standby. Everything
    // is initialized in order so the city functions from minute one.
    // Here, createPartControl does the same: sets up dynamic help, creates the
    // table viewer, and hands interaction to the controller — all in the right
    // order so the view is fully functional the moment Eclipse shows it.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the UI for this view — called by Eclipse when the view is first shown.
     * We register with the platform help system so the F1 key brings up relevant
     * documentation, then initialize the table viewer, and finally hand off to
     * {@link ProjectsViewController} which wires up all the menus, actions, and
     * event listeners.
     *
     * <p>For example — Lando assigns every post at once:</p>
     * <pre>
     *   createPartControl(parent)
     *     → setHelp(...)           // doc team briefed
     *     → initViewer(parent)     // Lobot at the console
     *     → new ProjectsViewController(this)  // security assigned
     * </pre>
     *
     * @param parent  the SWT composite that Eclipse gives us to draw into
     */
    @Override
    public void createPartControl( Composite parent )
    {
        // Help Context for Dynamic Help
        PlatformUI.getWorkbench().getHelpSystem().setHelp( parent, PluginConstants.PLUGIN_ID + "." + "projects_view" ); //$NON-NLS-1$ //$NON-NLS-2$

        initViewer( parent );

        // Adding the controller
        new ProjectsViewController( this );
    }


    // ── Lobot Sets Up the Main Display ──────────────────────────────────────
    // Lobot doesn't just flip switches — he configures the exact interface that
    // gives Lando the right information at the right level of detail. Multi-select
    // enabled, scroll bars ready, each data source connected.
    // initViewer does the same: creates the SWT TableViewer with the right style
    // flags, plugs in content and label providers, and wraps the label provider
    // in a DecoratingLabelProvider so Eclipse's decorators (like error overlays)
    // can annotate our project icons automatically.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates and configures the {@link TableViewer} for this view.
     * We use a plain table (not a tree) because projects are a flat list — no
     * parent-child nesting. The {@link DecoratingLabelProvider} wrapper lets the
     * Eclipse workbench's decorator framework add overlay icons (like error badges)
     * on top of our project icons automatically.
     *
     * <p>For example — Lobot calibrates the console:</p>
     * <pre>
     *   new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER)
     *   → setContentProvider(new ProjectsViewContentProvider(tableViewer))
     *   → setLabelProvider(new DecoratingLabelProvider(...))
     * </pre>
     *
     * @param parent  the SWT composite to embed the table into
     */
    private void initViewer( Composite parent )
    {
        tableViewer = new TableViewer( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
        tableViewer.setContentProvider( new ProjectsViewContentProvider( tableViewer ) );
        tableViewer.setLabelProvider( new DecoratingLabelProvider( new ProjectsViewLabelProvider(), Activator
            .getDefault().getWorkbench().getDecoratorManager().getLabelDecorator() ) );
    }


    // ── Lando Directs Attention to the Main Screen ──────────────────────────
    // When the briefing starts, Lando points everyone's attention to the central
    // display — that's where the action is. In Eclipse, "focus" means keyboard
    // events go to this widget, so pressing Enter or Delete acts on the table.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Gives keyboard focus to the projects table.
     * Eclipse calls this when the user clicks our view's tab or title bar. We route
     * focus to the underlying SWT table so keyboard navigation (arrows, Enter, Delete)
     * works immediately.
     *
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus()
    {
        tableViewer.getTable().setFocus();
    }


    // ── Lando Hands Over the Display Console to a Delegate ──────────────────
    // The controller needs direct access to the table viewer to refresh it,
    // register selection listeners, and so on. Lando doesn't micromanage —
    // he hands the console reference to whoever needs it.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the underlying {@link TableViewer} for this view.
     * The {@link ProjectsViewController} calls this so it can register listeners,
     * trigger refreshes, and respond to user interactions with the table.
     *
     * <p>For example — the controller takes the wheel:</p>
     * <pre>
     *   ProjectsViewController controller = new ProjectsViewController(this);
     *   TableViewer tv = this.getViewer();
     *   tv.addSelectionChangedListener(controller);
     * </pre>
     *
     * @return  the {@link TableViewer} that displays the list of projects
     */
    public TableViewer getViewer()
    {
        return tableViewer;
    }
}
