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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.view.wrappers.ProjectSorter;
import org.apache.directory.studio.schemaeditor.view.wrappers.ProjectWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.ProjectsViewRoot;
import org.apache.directory.studio.schemaeditor.view.wrappers.TreeNode;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: ProjectsViewContentProvider — Constructing the Second Death Star ──
// During Return of the Jedi, the Empire assembles the second Death Star piece by
// piece at Endor — each section transported in, connected up, catalogued. The
// construction manifest knows exactly what's in place and what order it goes in.
// This content provider does the same: it queries the ProjectsHandler for every
// project that exists, wraps each in a ProjectWrapper, adds it to the root node,
// sorts them alphabetically, and hands the assembled list to the TableViewer.
// The difference is that our construction succeeds.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Supplies the flat list of projects to the Projects View's table viewer.
 * On the first call to {@link #getChildren(Object)}, we query the active
 * {@link org.apache.directory.studio.schemaeditor.controller.ProjectsHandler} for all
 * known projects, wrap each one in a {@link ProjectWrapper}, attach them to the
 * {@link ProjectsViewRoot}, and sort them alphabetically using {@link ProjectSorter}.
 * Subsequent calls return the already-built list. Think of it as the construction
 * manifest for the second Death Star: assembled once, sorted, and ready to inspect.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ProjectsViewContentProvider implements IStructuredContentProvider, ITreeContentProvider
{
    /** The viewer */
    private TableViewer tableViewer;

    /** The Sorter */
    private ProjectSorter projectSorter;


    // ── The Construction Crew Is Assembled ───────────────────────────────────
    // Before the first hull plate is attached, the Empire assembles the construction
    // team and equips them with the tools they need. The constructor does the same:
    // it stores a reference to the table viewer (so other parts of the code can
    // trigger refreshes) and creates the sorter that will keep projects in alphabetical
    // order in the list.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new content provider bound to the given table viewer.
     * We hold the viewer reference so other callers (like the controller) can
     * ask us to trigger a refresh. We also create a {@link ProjectSorter} here
     * so it's ready to sort the project list on every {@link #getChildren(Object)} call.
     *
     * <p>For example — the crew arrives:</p>
     * <pre>
     *   new ProjectsViewContentProvider(tableViewer)
     *   → this.tableViewer = tableViewer
     *   → projectSorter = new ProjectSorter()
     * </pre>
     *
     * @param tableViewer  the table viewer this provider will supply content to
     */
    public ProjectsViewContentProvider( TableViewer tableViewer )
    {
        this.tableViewer = tableViewer;
        projectSorter = new ProjectSorter();
    }


    // ── The Manifest Is Reviewed ──────────────────────────────────────────────
    // The construction overseer checks the manifest — which sections have arrived,
    // which are still in transit. getElements is the first check; it just
    // delegates to getChildren where the actual assembly happens.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the top-level elements for the projects table.
     * Delegates directly to {@link #getChildren(Object)} since the project list is flat
     * (no nested tree structure — projects are just a list).
     *
     * @param inputElement  the root input object (a {@link ProjectsViewRoot})
     * @return              the array of {@link ProjectWrapper} objects to display
     */
    public Object[] getElements( Object inputElement )
    {
        return getChildren( inputElement );
    }


    // ── The Station Is Complete — Nothing to Demolish ────────────────────────
    // Once the second Death Star is assembled, you don't tear it down between
    // inspections. There's nothing to release here — no listeners, no handles.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Called by JFace when this content provider is being released.
     * We hold no listeners or external resources, so this is intentionally empty.
     */
    public void dispose()
    {
        // Nothing to do.
    }


    // ── A New Blueprint Arrives ───────────────────────────────────────────────
    // The Empire receives updated construction plans — but since the construction
    // crew re-checks the manifest fresh every time they're asked, they don't need
    // to react immediately when the plans change.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Called by JFace when the viewer's root input is replaced.
     * Since we re-query the ProjectsHandler on every {@link #getChildren(Object)} call,
     * we don't need to cache or react to input changes here.
     *
     * @param viewer    the viewer whose input changed
     * @param oldInput  the previous root
     * @param newInput  the new root
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
        // Nothing to do.
    }


    // ── The Manifest Is Assembled and Sorted ─────────────────────────────────
    // The construction coordinator walks through the docking bays, catalogues each
    // section, sorts the manifest alphabetically, and hands it to the overseer.
    // getChildren does the same: on first call it fetches all projects, wraps each
    // one, attaches them to the root, sorts them, and returns the sorted list.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the sorted list of project wrappers for the table.
     * On the first call we query the {@link org.apache.directory.studio.schemaeditor.controller.ProjectsHandler}
     * for every known project, create a {@link ProjectWrapper} for each, and attach
     * them to the {@link ProjectsViewRoot}. On subsequent calls the root already has
     * children and we skip the query. Either way, we sort the children alphabetically
     * before returning them.
     *
     * <p>For example — assembling and sorting the manifest:</p>
     * <pre>
     *   ProjectsHandler has: ["ZedProject", "AceProject", "MidProject"]
     *   getChildren(root) → wraps all three → sorts → ["AceProject", "MidProject", "ZedProject"]
     * </pre>
     *
     * @param parentElement  the {@link ProjectsViewRoot}; other types return an empty list
     * @return               sorted array of {@link ProjectWrapper} objects
     */
    public Object[] getChildren( Object parentElement )
    {
        List<TreeNode> children = new ArrayList<TreeNode>();

        if ( parentElement instanceof ProjectsViewRoot )
        {
            ProjectsViewRoot projectsViewRoot = ( ProjectsViewRoot ) parentElement;

            if ( !projectsViewRoot.hasChildren() )
            {
                for ( Project project : Activator.getDefault().getProjectsHandler().getProjects() )
                {
                    projectsViewRoot.addChild( new ProjectWrapper( project, tableViewer ) );
                }
            }

            children = projectsViewRoot.getChildren();

            // Sorting Children
            Collections.sort( children, projectSorter );
        }

        return children.toArray();
    }


    // ── Tracing a Section to Its Docking Bay ─────────────────────────────────
    // Every section of the Death Star is in a specific docking bay — you can
    // trace any part back to its location in the manifest. getParent traces
    // a TreeNode wrapper back to its parent in the tree.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent of the given tree element.
     * Projects are all at the top level (children of the root), so calling this
     * on a {@link ProjectWrapper} returns the {@link ProjectsViewRoot}. JFace uses
     * this to reveal items programmatically.
     *
     * @param element  the tree element whose parent to find
     * @return         the parent {@link TreeNode}, or {@code null} if the element is the root
     */
    public Object getParent( Object element )
    {
        if ( element instanceof TreeNode )
        {
            return ( ( TreeNode ) element ).getParent();
        }

        // Default
        return null;
    }


    // ── Does This Section Have Sub-sections? ─────────────────────────────────
    // Projects are atomic items — they don't expand into sub-items in this flat list.
    // hasChildren lets JFace know whether to draw an expand arrow next to a row.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the given tree element has children.
     * In the Projects View, projects are flat — no project contains sub-projects.
     * JFace calls this to decide whether to draw an expand arrow on a row.
     *
     * @param element  the tree element to test
     * @return         {@code true} if this node has children (won't be true for project wrappers)
     */
    public boolean hasChildren( Object element )
    {
        if ( element instanceof TreeNode )
        {
            return ( ( TreeNode ) element ).hasChildren();
        }

        // Default
        return false;
    }
}
