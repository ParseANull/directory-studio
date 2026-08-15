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
package org.apache.directory.studio.schemaeditor.view;


import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.views.HierarchyView;
import org.apache.directory.studio.schemaeditor.view.views.ProblemsView;
import org.apache.directory.studio.schemaeditor.view.views.ProjectsView;
import org.apache.directory.studio.schemaeditor.view.views.SchemaView;
import org.apache.directory.studio.schemaeditor.view.views.SearchView;
import org.apache.directory.studio.schemaeditor.view.wizards.NewAttributeTypeWizard;
import org.apache.directory.studio.schemaeditor.view.wizards.NewObjectClassWizard;
import org.apache.directory.studio.schemaeditor.view.wizards.NewProjectWizard;
import org.apache.directory.studio.schemaeditor.view.wizards.NewSchemaWizard;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


// ── CLASS: Perspective — THE TANTIVE IV BRIDGE ───────────────────────────────
// When the Tantive IV powers up at the start of A New Hope, every crew member
// moves to their designated console — helm at the front, communications to the
// side, engineering below — each station positioned exactly where it needs to
// be before the Star Destroyer appears on sensors.
// This class does the same for the Schema Editor workspace: it places the
// Schema View, Hierarchy View, Projects View, Problems View, and Search View
// into their assigned positions the moment the perspective first opens.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Defines the initial arrangement of views and editors for the Schema Editor
 * perspective — the overall "window layout" that Eclipse shows when a user
 * switches into Schema Editor mode.
 * It hooks into Eclipse's perspective extension point and is invoked once by
 * the workbench when the perspective opens for the first time; after that,
 * Eclipse restores the layout from its workspace memento automatically.
 * Think of it as the Tantive IV bridge: every station is positioned before
 * the captain arrives, so the crew can work together without reorganising on
 * the fly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Perspective implements IPerspectiveFactory
{
    /** The ID of the view */
    public static final String ID = PluginConstants.PERSPECTIVE_SCHEMA_EDITOR_ID;

    /** The ID of the top left folder */
    public static final String topLeftFolderId = PluginConstants.PERSPECTIVE_TOP_LEFT_FOLDER_ID;

    /** The ID of the bottom folder */
    public static final String bottomFolderId = PluginConstants.PERSPECTIVE_BOTTOM_FOLDER_ID;


    // ── CREW TAKES THEIR STATIONS ─────────────────────────────────────────────
    // The Tantive IV bridge comes to life: the helm officer slides into position
    // at the left panel, the engineering team fans out below, and the comms
    // officer heads to the bottom-right station — all without a word.
    // We place the Schema View and Hierarchy View in the top-left folder (30%
    // of the window width), pin the Projects View below them as a standalone
    // panel, and dock Problems and Search at the bottom — each crew member at
    // their console, ready for the Star Destroyer.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Lays out all the Schema Editor views and registers perspective shortcuts
     * the first time Eclipse builds this perspective for a workbench window.
     * Without this method, the user would land in a blank perspective and have
     * to drag every view into place by hand — not a great first impression.
     * Eclipse calls this exactly once per fresh perspective; after that the
     * workspace memento handles restoring whatever layout the user has settled on.
     *
     * <p>For example — the Tantive IV bridge crew taking their stations:</p>
     * <pre>
     *   Helm station   → SchemaView + HierarchyView, top-left, 30% width
     *   Captain's seat → ProjectsView, standalone below helm, non-closeable
     *   Engineering    → ProblemsView, bottom panel, 30% height
     *   Comms          → SearchView, also in the bottom panel
     * </pre>
     *
     * @param layout  the page-layout object Eclipse provides for us to arrange
     *                views and folders into; think of it as the bridge blueprint
     */
    public void createInitialLayout( IPageLayout layout )
    {
        // Allowing the Editor Area
        layout.setEditorAreaVisible( true );
        String editorAreaId = layout.getEditorArea();

        // Creating top left folder
        IFolderLayout topLeftFolder = layout.createFolder( topLeftFolderId, IPageLayout.LEFT, 0.3f, editorAreaId );

        // Creating bottom folder
        IFolderLayout bottomFolder = layout.createFolder( bottomFolderId, IPageLayout.BOTTOM, 0.7f, editorAreaId );

        // Adding Views
        topLeftFolder.addView( SchemaView.ID );
        topLeftFolder.addView( HierarchyView.ID );
        layout.addStandaloneView( ProjectsView.ID, true, IPageLayout.BOTTOM, 0.7f, topLeftFolderId );
        bottomFolder.addView( ProblemsView.ID );
        bottomFolder.addView( SearchView.ID );

        // Setting up non-closeable views
        layout.getViewLayout( SchemaView.ID ).setCloseable( false );
        layout.getViewLayout( ProjectsView.ID ).setCloseable( false );

        // Adding Perspective shortcuts
        layout.addPerspectiveShortcut( PluginConstants.PERSPECTIVE_LDAP_BROWSER_ID );
        layout.addPerspectiveShortcut( Perspective.ID );

        // Adding View shortcuts
        layout.addShowViewShortcut( SchemaView.ID );
        layout.addShowViewShortcut( ProjectsView.ID );
        layout.addShowViewShortcut( ProblemsView.ID );
        layout.addShowViewShortcut( HierarchyView.ID );
        layout.addShowViewShortcut( SearchView.ID );

        // Adding New Wizard shortcuts
        layout.addNewWizardShortcut( NewProjectWizard.ID );
        layout.addNewWizardShortcut( NewSchemaWizard.ID );
        layout.addNewWizardShortcut( NewAttributeTypeWizard.ID );
        layout.addNewWizardShortcut( NewObjectClassWizard.ID );
    }
}
