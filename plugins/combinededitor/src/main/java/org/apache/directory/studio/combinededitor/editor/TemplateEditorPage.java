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
package org.apache.directory.studio.combinededitor.editor;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;

import org.apache.directory.studio.templateeditor.EntryTemplatePlugin;
import org.apache.directory.studio.templateeditor.EntryTemplatePluginConstants;
import org.apache.directory.studio.templateeditor.editor.TemplateEditorWidget;
import org.apache.directory.studio.templateeditor.model.Template;


// ── CLASS: TemplateEditorPage — The Tantive IV's Holographic Briefing Panel ──
// In the Rebel briefing room a holographic display shows each mission target
// in a rich, structured layout — not just raw data, but a purpose-built view
// with labels, groupings, and context tailored for that specific type of target.
// General Dodonna picks the right briefing template for each entry type and the
// hologram snaps to that layout instantly.
// TemplateEditorPage is that holographic panel: it wraps a TemplateEditorWidget
// that renders the LDAP entry through a schema-aware display template, showing
// only the attributes relevant to the entry's object class in a human-friendly
// form.  If no matching template exists the parent editor can optionally fall
// back to the Table or LDIF tab.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The "Template Editor" tab page in the combined entry editor.
 * Renders the current LDAP entry using an entry template — a pre-designed
 * form-like display that groups and labels attributes according to the entry's
 * object class.  If no template matches the entry, the parent editor may
 * auto-switch to the Table or LDIF tab depending on user preferences.
 * Think of this as the Rebel briefing room's holographic display: structured,
 * context-aware, and tailored for the specific entry type.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateEditorPage extends AbstractCombinedEntryEditorPage
{
    /** The template editor widget */
    private TemplateEditorWidget templateEditorWidget;


    // ── Briefing Panel Reports for Duty on the Tantive IV Bridge ─────────────
    // The holographic briefing panel is assigned to its tab in the CTabFolder,
    // labelled, and given its icon — but the hologram itself isn't rendered
    // until an officer actually walks into the briefing room (lazy init).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the Template Editor page and its tab item in the editor's tab folder.
     * We create the {@link TemplateEditorWidget} immediately (it is lightweight)
     * and create the tab, but we defer calling {@link TemplateEditorWidget#init}
     * until the user first selects this tab, so the potentially heavy template
     * rendering doesn't happen until needed.
     *
     * @param editor  the combined editor that owns this page.
     */
    public TemplateEditorPage( CombinedEntryEditor editor )
    {
        super( editor );

        // Creating and assigning the tab item
        CTabItem tabItem = new CTabItem( editor.getTabFolder(), SWT.NONE );
        tabItem.setText( Messages.getString( "TemplateEditorPage.TemplateEditor" ) ); //$NON-NLS-1$
        tabItem.setImage( EntryTemplatePlugin.getDefault().getImage( EntryTemplatePluginConstants.IMG_TEMPLATE ) );
        setTabItem( tabItem );

        // Creating the template editor widget
        templateEditorWidget = new TemplateEditorWidget( editor );
    }


    // ── The Hologram Powers Up — Template is Rendered ────────────────────────
    // When the briefing officer clicks the Template tab for the first time,
    // Dodonna activates the holographic display: the template widget is
    // fully initialised, the correct template is selected for the entry's
    // object class, and the rendered form is attached to the CTabFolder.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the template editor widget and attaches its form to the tab.
     * We delegate to {@link TemplateEditorWidget#init} which selects the matching
     * template for the current entry, builds the SWT form, and populates it with
     * attribute values.  We then attach the form as the tab's control and force a
     * layout update so the new content is immediately visible.
     */
    public void init()
    {
        if ( templateEditorWidget != null )
        {
            // Initializing the template editor widget
            templateEditorWidget.init( getEditor().getTabFolder() );

            // Updating the editor's tab folder to force the attachment of the new form
            getTabItem().setControl( templateEditorWidget.getForm() );
            getEditor().getTabFolder().update();
        }
    }


    // ── Holographic Panel Powered Down — Release All Resources ───────────────
    // When the Tantive IV's mission ends the holographic panel shuts down cleanly,
    // freeing the projector hardware and any template resources it was holding.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes the template editor widget and releases its resources.
     * Called by {@link CombinedEntryEditor#dispose()} when the editor closes.
     */
    public void dispose()
    {
        if ( templateEditorWidget != null )
        {
            // Disposing the template editor widget
            templateEditorWidget.dispose();
        }
    }


    // ── Hologram Refreshes — Latest Data Projected ───────────────────────────
    // When new intelligence arrives (another tab edited an attribute) Dodonna
    // instructs the projector to refresh the hologram so the officers always
    // see the current picture.  The form may change shape if the template
    // selection changes (e.g. an object class was added).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the template editor widget from the current shared working copy.
     * Called when the entry's data changes (e.g. another page edited an attribute).
     * We ask the widget to update itself and then re-attach its form in case the
     * template selection changed and produced a new SWT form widget.
     */
    public void update()
    {
        if ( templateEditorWidget != null )
        {
            // Updating the template editor widget
            templateEditorWidget.update();

            // Updating the editor's tab folder to force the attachment of the new form
            getTabItem().setControl( templateEditorWidget.getForm() );
            getEditor().getTabFolder().update();
        }
    }


    // ── Briefing Officer Steps Up — Keyboard Focus to Template Form ───────────
    // Antilles points at the briefing panel and says "Your turn."  The template
    // form takes focus so the user can start editing without an extra click.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Moves keyboard focus to the template editor widget.
     */
    public void setFocus()
    {
        if ( templateEditorWidget != null )
        {
            // Setting focus on the template editor widget
            templateEditorWidget.setFocus();
        }
    }


    // ── New Mission Target — Briefing Panel Switches to New Entry ─────────────
    // When the editor switches to a different LDAP entry, Dodonna replaces the
    // holographic target: the template widget re-selects a matching template for
    // the new entry and re-renders its form.  The form re-attachment ensures the
    // new content replaces the old one in the CTabFolder control slot.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the editor's input switches to a different LDAP entry.
     * We notify the template widget so it can re-select the appropriate template
     * and rebuild the form for the new entry, then re-attach the form to the tab.
     */
    public void editorInputChanged()
    {
        if ( templateEditorWidget != null )
        {
            // Changing the editor input on the template editor widget
            templateEditorWidget.editorInputChanged();

            // Updating the editor's tab folder to force the attachment of the new form
            getTabItem().setControl( templateEditorWidget.getForm() );
            getEditor().getTabFolder().update();
        }
    }


    // ── Dodonna Switches to a Different Briefing Template ────────────────────
    // Mid-mission, Dodonna might decide a different briefing template gives a
    // better view of the target — he swaps the hologram projection and the
    // crew sees the new layout immediately.
    // templateSwitched() is the hook the editor calls when the user or the system
    // changes which template is active for the current entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the editor when a "Switch Template" event occurs.
     * We re-attach the template widget's form to the tab so the new template's
     * form widget replaces the old one in the layout.
     *
     * @param templateEditorWidget  the template editor widget whose form changed.
     * @param template              the newly active template.
     */
    public void templateSwitched( TemplateEditorWidget templateEditorWidget, Template template )
    {
        // Updating the editor's tab folder to force the attachment of the new form
        getTabItem().setControl( templateEditorWidget.getForm() );
        getEditor().getTabFolder().update();
    }
}
