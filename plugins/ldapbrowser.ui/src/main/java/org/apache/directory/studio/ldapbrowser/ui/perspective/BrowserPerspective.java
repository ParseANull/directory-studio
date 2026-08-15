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

package org.apache.directory.studio.ldapbrowser.ui.perspective;


import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.apache.directory.studio.connection.ui.wizards.NewConnectionWizard;
import org.apache.directory.studio.ldapbrowser.common.wizards.NewContextEntryWizard;
import org.apache.directory.studio.ldapbrowser.common.wizards.NewEntryWizard;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.views.browser.BrowserView;
import org.apache.directory.studio.ldapbrowser.ui.views.connection.ConnectionView;
import org.apache.directory.studio.ldapbrowser.ui.views.modificationlogs.ModificationLogsView;
import org.apache.directory.studio.ldapbrowser.ui.views.searchlogs.SearchLogsView;
import org.apache.directory.studio.ldapbrowser.ui.wizards.BatchOperationWizard;
import org.apache.directory.studio.ldapbrowser.ui.wizards.NewBookmarkWizard;
import org.apache.directory.studio.ldapbrowser.ui.wizards.NewSearchWizard;
import org.apache.directory.studio.ldifeditor.wizards.NewLdifFileWizard;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


// ── CLASS: BrowserPerspective — LUKE'S BINARY SUNSET ON TATOOINE ─────────────
// Luke stands on the ridge outside the Lars homestead and gazes across the desert
// at twin suns setting over the horizon — he can see the whole landscape at once,
// every dune and shadow, the full picture of the world he's about to leave.
// This class does that for Directory Studio: it arranges every view (browser tree,
// connection list, editor area, logs, outline) into the panoramic layout the user
// sees when they first open the LDAP perspective — their binary sunset.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Implements the {@link IPerspectiveFactory} for the LDAP Browser perspective.
 * Eclipse calls {@link #createInitialLayout} once when the user first opens this
 * perspective, and we use it to position the browser tree, connection list, editor
 * area, outline view, log views, and progress view into their default locations.
 * Think of this class as the architect of the panorama: everything in the right
 * place so the user can see the whole LDAP directory landscape at a glance.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserPerspective implements IPerspectiveFactory
{

    private static final String PROGRESS_VIEW_ID = "org.eclipse.ui.views.ProgressView"; //$NON-NLS-1$
    private static final String ERROR_LOG_VIEW_ID = "org.eclipse.pde.runtime.LogView"; //$NON-NLS-1$


    // ── Luke Reads the Desert Coordinates Off the Homestead Sign ─────────────────
    // Before leaving Tatooine, Luke notes the exact location so he can give the
    // fleet a fixed reference point for this perspective.
    // We return the constant ID that Eclipse uses to identify the LDAP Browser perspective.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the unique Eclipse perspective ID for the LDAP Browser perspective.
     * Other code uses this to programmatically switch to or reference this perspective
     * rather than hard-coding the ID string everywhere.
     *
     * @return  the perspective ID string defined in {@link BrowserUIConstants#PERSPECTIVE_LDAP}
     */
    public static String getId()
    {
        return BrowserUIConstants.PERSPECTIVE_LDAP;
    }


    // ── Luke Surveys the Full Horizon Before Departing ───────────────────────────
    // Standing on the ridge, Luke takes in the whole scene in one sweep:
    // he notes the sun positions (actions), traces the landscape contours (layout),
    // and registers the nearby settlements (perspective shortcuts) on his mental map.
    // We do all three: register actions, define the view layout, and add shortcuts.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called once by Eclipse to set up the initial layout of the LDAP Browser perspective.
     * We delegate to {@link #defineActions} to register "New..." wizard shortcuts and
     * "Show View" shortcuts, then to {@link #defineLayout} to position all the views
     * in their folders, and finally add perspective shortcuts for quick switching.
     *
     * @param layout  the page layout object Eclipse provides for us to configure
     */
    public void createInitialLayout( IPageLayout layout )
    {
        defineActions( layout );
        defineLayout( layout );

        layout.addPerspectiveShortcut( BrowserUIConstants.PERSPECTIVE_SCHEMA_EDITOR );
        layout.addPerspectiveShortcut( BrowserUIConstants.PERSPECTIVE_LDAP );
    }


    // ── Luke Notes Which Settlements Are Visible from the Ridge ─────────────────
    // From the high ground, Luke can see several towns and routes — he logs them
    // as waypoints he can jump to quickly without needing the nav computer.
    // We register "New..." wizard shortcuts and "Show View" shortcuts the same way:
    // quick-access entries the user sees in the menus without hunting through submenus.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Registers the "New..." wizard shortcuts and "Show View" shortcuts for this perspective.
     * These appear as quick-access items in the Eclipse "File > New" and "Window > Show View"
     * menus so the user can create connections, entries, searches, and bookmarks without
     * digging through the full wizard/view registry.
     *
     * @param layout  the page layout to register shortcuts on
     */
    private void defineActions( IPageLayout layout )
    {
        // Add "new wizards".
        layout.addNewWizardShortcut( NewConnectionWizard.getId() );
        layout.addNewWizardShortcut( NewEntryWizard.getId() );
        layout.addNewWizardShortcut( NewContextEntryWizard.getId() );
        layout.addNewWizardShortcut( NewSearchWizard.getId() );
        layout.addNewWizardShortcut( NewBookmarkWizard.getId() );
        layout.addNewWizardShortcut( BatchOperationWizard.getId() );
        layout.addNewWizardShortcut( NewLdifFileWizard.getId() );

        // Add "show views".
        layout.addShowViewShortcut( ConnectionView.getId() );
        layout.addShowViewShortcut( BrowserView.getId() );
        layout.addShowViewShortcut( ModificationLogsView.getId() );
        layout.addShowViewShortcut( SearchLogsView.getId() );
        layout.addShowViewShortcut( IPageLayout.ID_OUTLINE );
        layout.addShowViewShortcut( PROGRESS_VIEW_ID );
        layout.addShowViewShortcut( ERROR_LOG_VIEW_ID );
    }


    // ── Luke Arranges the Whole Landscape in His Field of View ──────────────────
    // Luke positions himself so the twin suns are on his left (browser tree), the
    // horizon is straight ahead (editor area), the village is lower-right (logs),
    // and the herd is in the far distance (outline, progress).
    // We position all the Eclipse views into their folders in the same arrangement.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the default view folder layout for the LDAP Browser perspective.
     * We put the browser tree on the left (25% width), the connection list below it,
     * the outline on the right, progress below the outline, and the log views at the
     * bottom of the editor area. In RCP mode (non-IDE) we mark key views as non-closable
     * so the user can't accidentally lose them.
     *
     * @param layout  the page layout to add folders and views to
     */
    private void defineLayout( IPageLayout layout )
    {

        // Editor area
        String editorArea = layout.getEditorArea();

        // Browser folder
        IFolderLayout browserFolder = layout.createFolder( "browserFolder", IPageLayout.LEFT, ( float ) 0.25, //$NON-NLS-1$
            editorArea );
        browserFolder.addView( BrowserView.getId() );

        // Connection folder
        IFolderLayout connectionFolder = layout.createFolder( "connectionFolder", IPageLayout.BOTTOM, ( float ) 0.75, //$NON-NLS-1$
            "browserFolder" ); //$NON-NLS-1$
        connectionFolder.addView( ConnectionView.getId() );

        // Outline folder
        IFolderLayout outlineFolder = layout.createFolder( "outlineFolder", IPageLayout.RIGHT, ( float ) 0.75, //$NON-NLS-1$
            editorArea );
        outlineFolder.addView( IPageLayout.ID_OUTLINE );

        // Progress folder
        IFolderLayout progessFolder = layout.createFolder( "progressFolder", IPageLayout.BOTTOM, ( float ) 0.75, //$NON-NLS-1$
            "outlineFolder" ); //$NON-NLS-1$
        progessFolder.addView( PROGRESS_VIEW_ID );

        // Log folder
        IFolderLayout logFolder = layout.createFolder( "logFolder", IPageLayout.BOTTOM, ( float ) 0.75, editorArea ); //$NON-NLS-1$
        logFolder.addView( ModificationLogsView.getId() );
        logFolder.addView( SearchLogsView.getId() );
        logFolder.addView( ERROR_LOG_VIEW_ID );
        logFolder.addPlaceholder( "*" ); //$NON-NLS-1$

        // non-closable?
        boolean isIDE = CommonUIUtils.isIDEEnvironment();
        if ( !isIDE )
        {
            layout.getViewLayout( BrowserView.getId() ).setCloseable( false );
            layout.getViewLayout( ConnectionView.getId() ).setCloseable( false );
            layout.getViewLayout( IPageLayout.ID_OUTLINE ).setCloseable( false );
            layout.getViewLayout( PROGRESS_VIEW_ID ).setCloseable( false );
            layout.getViewLayout( ModificationLogsView.getId() ).setCloseable( false );
            layout.getViewLayout( SearchLogsView.getId() ).setCloseable( false );
        }
    }

}
