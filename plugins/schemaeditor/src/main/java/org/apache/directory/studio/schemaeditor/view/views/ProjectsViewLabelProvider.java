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
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.Project.ProjectState;
import org.apache.directory.studio.schemaeditor.model.ProjectType;
import org.apache.directory.studio.schemaeditor.view.wrappers.ProjectWrapper;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;


// ── CLASS: ProjectsViewLabelProvider — Vader's Suit Revealing the Inner Power ──
// Anakin Skywalker's burned body is wrapped in black armor, life-support systems,
// and a helmet that tells the galaxy who he is at a glance — powerful, dangerous,
// in a specific state. The suit is the presentation layer on top of the raw entity.
// This class wraps each Project object in a visual presentation: the right icon
// (offline/online, open/closed) and the project's name. It doesn't change the
// project; it just decides how the project looks in the list.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Supplies the icon and display name for each row in the Projects view's table.
 * A JFace {@link LabelProvider} is the thing that knows how to turn any model
 * object into something visual — an image and a text string. We look at each
 * project's type (offline vs. online) and state (open vs. closed) and pick the
 * matching icon from the plugin's image registry. Think of this class as Vader's
 * suit: it wraps the underlying model object in exactly the visual presentation
 * that tells you everything important at a glance.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ProjectsViewLabelProvider extends LabelProvider
{
    // ── The Suit Signals Vader's Current State ───────────────────────────────
    // Walk into any room and Vader's suit tells you instantly: he's here, he's
    // active, he's a Sith Lord with a specific role in the Empire. The glowing
    // chest panel, the breathing — every visual detail carries information.
    // Similarly, this method reads the project's type and open/closed state and
    // returns the icon that immediately communicates that combination to the user.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon to display next to this project row in the table.
     * We combine the project's type (offline = working locally without a live server,
     * online = connected to a real LDAP server) with its state (open or closed) to
     * pick the right image from our plugin's image registry.
     *
     * <p>For example — the suit tells you everything:</p>
     * <pre>
     *   OFFLINE + OPEN   → offline project icon (local schemas, actively editing)
     *   OFFLINE + CLOSED → offline project closed icon (local schemas, not open)
     *   ONLINE  + OPEN   → online project icon (connected to live LDAP server)
     *   ONLINE  + CLOSED → online project closed icon (server project, not open)
     * </pre>
     *
     * @param element  the tree element — we only do something useful if it's a {@link ProjectWrapper}
     * @return         the appropriate {@link Image} for this project's type and state, or the
     *                 default image from the parent class if the element isn't a project
     */
    @Override
    public Image getImage( Object element )
    {
        if ( element instanceof ProjectWrapper )
        {
            Project project = ( ( ProjectWrapper ) element ).getProject();
            ProjectType type = project.getType();
            switch ( type )
            {
                case OFFLINE:
                    ProjectState state = project.getState();
                    switch ( state )
                    {
                        case OPEN:
                            return Activator.getDefault().getImage( PluginConstants.IMG_PROJECT_OFFLINE );
                        case CLOSED:
                            return Activator.getDefault().getImage( PluginConstants.IMG_PROJECT_OFFLINE_CLOSED );
                    }
                case ONLINE:
                    ProjectState state2 = project.getState();
                    switch ( state2 )
                    {
                        case OPEN:
                            return Activator.getDefault().getImage( PluginConstants.IMG_PROJECT_ONLINE );
                        case CLOSED:
                            return Activator.getDefault().getImage( PluginConstants.IMG_PROJECT_ONLINE_CLOSED );
                    }
            }
        }

        // Default
        return super.getImage( element );
    }


    // ── The Suit Speaks Before Vader Does ───────────────────────────────────
    // The name "Darth Vader" doesn't need an introduction — the presence, the
    // suit, the mechanical breathing already announced it. When Vader speaks,
    // you hear the name behind the mask.
    // When the Projects view renders a row, this method delivers the project's
    // name as the text that appears next to its icon.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this project row in the table.
     * We simply unwrap the {@link ProjectWrapper} to get the underlying {@link Project}
     * and return its name. The name is what the user typed when they created the project.
     *
     * <p>For example — the suit announces the name:</p>
     * <pre>
     *   ProjectWrapper("My LDAP Schema") → getText() → "My LDAP Schema"
     * </pre>
     *
     * @param element  the table element — only {@link ProjectWrapper} instances return something meaningful
     * @return         the project's name, or the default string from the parent class if
     *                 the element is something unexpected
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof ProjectWrapper )
        {
            ProjectWrapper projectWrapper = ( ProjectWrapper ) element;
            return projectWrapper.getProject().getName();
        }

        // Default
        return super.getText( element );
    }
}
