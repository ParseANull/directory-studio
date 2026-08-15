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
package org.apache.directory.studio.templateeditor.actions;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

import org.apache.directory.studio.templateeditor.EntryTemplatePlugin;
import org.apache.directory.studio.templateeditor.EntryTemplatePluginConstants;
import org.apache.directory.studio.templateeditor.editor.TemplateEditorWidget;
import org.apache.directory.studio.templateeditor.model.Template;


// ── CLASS: DisplayEntryInTemplateMenuManager — CLONE TROOPERS ASSEMBLING THE BRIEFING ─
// Before a mission launch, clone troopers assemble in the hangar bay and sort
// themselves by unit — Alpha, Beta, Gamma — so the commanding officer can see
// exactly what's available at a glance. Every time the briefing room doors open
// (the menu is shown) they snap back to formation. This dynamic menu manager does
// the same: each time the user opens the "Display Entry In" dropdown, it rebuilds
// the list of available templates (sorted by title), marks the currently selected
// one, and always adds a preferences action at the bottom.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A dynamic {@link MenuManager} that builds the "Display Entry In [template]" menu
 * on-demand every time it is about to be shown. It lists all templates that match
 * the current LDAP entry, sorted alphabetically, with the active template checked.
 * A separator and a link to the preferences page are appended at the bottom.
 * Think of this as clone troopers snapping to formation every time the briefing
 * room doors open — always accurate, always sorted.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DisplayEntryInTemplateMenuManager extends MenuManager implements IMenuListener
{
    /** The associated {@link TemplateEditorWidget} */
    private TemplateEditorWidget templateEditorPage;


    // ── CONSTRUCTOR: TROOPER UNIT FORMS UP FOR THE FIRST TIME ────────────────────
    // The unit commander (this object) is assigned to a specific editor widget.
    // We register ourselves as a menu listener so we can rebuild the formation
    // every time the dropdown is opened — because the list of available templates
    // can change as the user navigates to different LDAP entries.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the menu manager and wires it to the given editor widget. Registers
     * {@code this} as its own menu listener so {@link #menuAboutToShow(IMenuManager)}
     * fires every time the dropdown opens, keeping the template list current.
     *
     * <p>For example — the unit forms up for the first time:</p>
     * <pre>
     *   new DisplayEntryInTemplateMenuManager(templateEditorWidget);
     *   // "Unit assembled, reporting to editor widget, ready for dynamic rebuilds."
     * </pre>
     *
     * @param templateEditorPage  the editor widget whose templates populate this menu
     */
    public DisplayEntryInTemplateMenuManager( TemplateEditorWidget templateEditorPage )
    {
        super(
            Messages.getString( "DisplayEntryInTemplateMenuManager.DiplayEntryIn" ), EntryTemplatePlugin.getDefault().getImageDescriptor( //$NON-NLS-1$
                    EntryTemplatePluginConstants.IMG_SWITCH_TEMPLATE ), null );
        addMenuListener( this );
        this.templateEditorPage = templateEditorPage;
    }


    // ── MENU ABOUT TO SHOW: TROOPERS SNAP TO FORMATION ───────────────────────────
    // The briefing room doors swing open. Every trooper snaps into position:
    // ranks sorted, helmets on, the right units checked. We delegate to
    // fillInMenuManager() which does the actual work of building the current list.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by JFace just before the dropdown menu is displayed. Delegates to
     * {@link #fillInMenuManager(IMenuManager, TemplateEditorWidget)} to rebuild
     * the template list from scratch on every open so it always reflects the
     * current entry's available templates.
     *
     * @param manager  the menu manager being populated; same as {@code this}
     */
    public void menuAboutToShow( IMenuManager manager )
    {
        fillInMenuManager( manager, templateEditorPage );
    }


    // ── IS VISIBLE: UNIT IS ALWAYS ON DUTY ───────────────────────────────────────
    // This unit is permanently visible in the menu — it never hides itself.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code true} — this menu item is always visible in the
     * parent menu, whether or not templates exist. The menu itself handles the
     * empty-list case with a disabled placeholder.
     *
     * @return {@code true} always
     */
    public boolean isVisible()
    {
        return true;
    }


    // ── IS DYNAMIC: FORMATION CHANGES WITH EVERY BRIEFING ────────────────────────
    // Different missions, different troopers — the formation is rebuilt from scratch
    // every time the doors open. Returning true here tells JFace to always call
    // menuAboutToShow() rather than caching the menu from the last open.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} so JFace knows to call {@link #menuAboutToShow(IMenuManager)}
     * every time the menu is shown rather than using a cached version.
     *
     * @return {@code true} always
     */
    public boolean isDynamic()
    {
        return true;
    }


    // ── FILL IN MENU MANAGER: ASSEMBLE THE FULL FORMATION ────────────────────────
    // The sergeant calls roll: clear the formation, sort the units alphabetically
    // by name, mark the active unit with a checkmark, fall back to a "No template"
    // placeholder if the hangar is empty, and always append a Preferences link at
    // the bottom. This static method is also called directly by
    // DisplayEntryInTemplateAction.run() when building a fresh context menu.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Populates {@code menuManager} with one checkable action per available template,
     * sorted alphabetically by title, with the currently selected template checked.
     * If no templates are available a single disabled "No template" item is shown.
     * A separator and a link to the preferences page are always appended at the end.
     * This is static so {@link DisplayEntryInTemplateAction} can call it too.
     *
     * <p>For example — the sergeant calls roll and assembles the formation:</p>
     * <pre>
     *   // 1. Clear any stale items from the previous open.
     *   // 2. Sort templates A → Z.
     *   // 3. Create one checked/unchecked action per template.
     *   // 4. Append separator + Preferences link.
     * </pre>
     *
     * @param menuManager           the menu manager to populate; cleared before filling
     * @param templateEditorWidget  the editor widget providing the template list and
     *                              current selection
     */
    protected static void fillInMenuManager( IMenuManager menuManager, TemplateEditorWidget templateEditorWidget )
    {
        // Getting the matching templates and currently selected one from the editor page
        List<Template> matchingTemplates = new ArrayList<Template>( templateEditorWidget.getMatchingTemplates() );
        Template selectedTemplate = templateEditorWidget.getSelectedTemplate();

        // Sorting the list of matching templates by their title
        Collections.sort( matchingTemplates, new Comparator<Template>()
        {
            public int compare( Template o1, Template o2 )
            {
                if ( ( o1 == null ) && ( o2 == null ) )
                {
                    return 0;
                }
                else if ( ( o1 != null ) && ( o2 == null ) )
                {
                    return 1;
                }
                else if ( ( o1 == null ) && ( o2 != null ) )
                {
                    return -1;
                }
                else if ( ( o1 != null ) && ( o2 != null ) )
                {
                    String title1 = o1.getTitle();
                    String title2 = o2.getTitle();
                    if ( ( title1 == null ) && ( title2 == null ) )
                    {
                        return 0;
                    }
                    else if ( ( title1 != null ) && ( title2 == null ) )
                    {
                        return 1;
                    }
                    else if ( ( title1 == null ) && ( title2 != null ) )
                    {
                        return -1;
                    }
                    else if ( ( title1 != null ) && ( title2 != null ) )
                    {
                        return title1.compareTo( title2 );

                    }
                }

                return 0;
            };
        } );

        // As the Menu Manager is dynamic, we need to
        // remove all the previously added actions
        menuManager.removeAll();

        if ( ( matchingTemplates != null ) && ( matchingTemplates.size() > 0 ) )
        {
            // Looping on the matching templates and creating an action for each one
            for ( Template matchingTemplate : matchingTemplates )
            {
                // Creating the action associated with the entry editor
                menuManager.add( createAction( templateEditorWidget, matchingTemplate, ( matchingTemplate
                    .equals( selectedTemplate ) ) ) );
            }
        }
        else
        {
            // Creating a action that will be disabled when no template is available
            Action noTemplateAction = new Action(
                Messages.getString( "DisplayEntryInTemplateMenuManager.NoTemplate" ), Action.AS_CHECK_BOX ) //$NON-NLS-1$
            {
            };
            noTemplateAction.setEnabled( false );
            menuManager.add( noTemplateAction );
        }

        // Separator
        menuManager.add( new Separator() );

        // Preferences Action
        menuManager.add( new EntryTemplatePreferencePageAction() );
    }


    // ── CREATE ACTION: TROOPER TAKES THEIR ASSIGNED POSITION ─────────────────────
    // Each trooper is assigned a slot and told whether to stand at attention
    // (checked = currently active template) or at ease (unchecked). We wrap
    // a SwitchTemplateAction in the right checked state so the UI shows a
    // tick next to the currently displayed template.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a checkable {@link SwitchTemplateAction} for the given template and
     * sets its checked state to {@code isChecked}. This is the individual menu
     * item for one template in the chooser.
     *
     * <p>For example — a trooper takes their assigned position:</p>
     * <pre>
     *   IAction action = createAction(widget, userAccountTemplate, true);
     *   // action.isChecked() == true  →  tick appears next to "User Account" in the menu
     * </pre>
     *
     * @param templateEditorWidget  the editor widget to switch when this action fires
     * @param template              the template this action represents
     * @param isChecked             {@code true} if this template is currently active
     * @return the configured checkable action
     */
    private static IAction createAction( TemplateEditorWidget templateEditorWidget, Template template, boolean isChecked )
    {
        Action action = new SwitchTemplateAction( templateEditorWidget, template );
        action.setChecked( isChecked );

        return action;
    }
}
