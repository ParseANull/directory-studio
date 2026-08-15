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
package org.apache.directory.studio.templateeditor.editor;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.directory.studio.entryeditors.EntryEditorInput;
import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import org.apache.directory.studio.templateeditor.EntryTemplatePlugin;
import org.apache.directory.studio.templateeditor.EntryTemplatePluginUtils;
import org.apache.directory.studio.templateeditor.actions.DisplayEntryInTemplateAction;
import org.apache.directory.studio.templateeditor.actions.DisplayEntryInTemplateMenuManager;
import org.apache.directory.studio.templateeditor.actions.EditorPagePropertiesAction;
import org.apache.directory.studio.templateeditor.actions.RefreshAction;
import org.apache.directory.studio.templateeditor.actions.SimpleActionProxy;
import org.apache.directory.studio.templateeditor.editor.widgets.EditorCheckbox;
import org.apache.directory.studio.templateeditor.editor.widgets.EditorComposite;
import org.apache.directory.studio.templateeditor.editor.widgets.EditorDate;
import org.apache.directory.studio.templateeditor.editor.widgets.EditorFileChooser;
import org.apache.directory.studio.templateeditor.editor.widgets.EditorImage;
import org.apache.directory.studio.templateeditor.editor.widgets.EditorLabel;
import org.apache.directory.studio.templateeditor.editor.widgets.EditorLink;
import org.apache.directory.studio.templateeditor.editor.widgets.EditorListbox;
import org.apache.directory.studio.templateeditor.editor.widgets.EditorPassword;
import org.apache.directory.studio.templateeditor.editor.widgets.EditorRadioButtons;
import org.apache.directory.studio.templateeditor.editor.widgets.EditorSection;
import org.apache.directory.studio.templateeditor.editor.widgets.EditorSpinner;
import org.apache.directory.studio.templateeditor.editor.widgets.EditorTable;
import org.apache.directory.studio.templateeditor.editor.widgets.EditorTextField;
import org.apache.directory.studio.templateeditor.editor.widgets.EditorWidget;
import org.apache.directory.studio.templateeditor.model.Template;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateCheckbox;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateComposite;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateDate;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateFileChooser;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateForm;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateImage;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateLabel;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateLink;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateListbox;
import org.apache.directory.studio.templateeditor.model.widgets.TemplatePassword;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateRadioButtons;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateSection;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateSpinner;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateTable;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateTextField;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateWidget;


// ── CLASS: TemplateEditorWidget — THE BRIEFING ROOM HOLOGRAM ─────────────────────
// In the Rebel briefing room on Yavin 4, Admiral Ackbar activates the holographic
// display and the Death Star schematics materialize in the air — field labels here,
// trench coordinates there, navigation vector controls on the side. Every template
// widget (checkbox, text field, section, image) has an exact holographic panel in
// the room. This class IS that hologram: it takes a Template model object, walks its
// widget tree, and instantiates the corresponding SWT EditorWidget for each node.
// When the template switches or the entry changes, the hologram tears itself down
// and rebuilds from scratch.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * The central SWT/JFace rendering engine for the template entry editor. Builds a
 * scrolled {@link Form} from a {@link Template}'s widget tree, mapping each
 * {@link TemplateWidget} model node to the appropriate {@link EditorWidget}
 * implementation. Manages the toolbar (Refresh, Display-In-Template), context menu,
 * and the full rebuild lifecycle when the template or entry changes.
 * Think of this as the briefing room hologram — it renders whatever the selected
 * template describes and tears itself down cleanly when the view needs to change.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateEditorWidget
{
    /** The associated editor */
    private IEntryEditor editor;

    /** The flag to know whether or not the widget has been initialized */
    private boolean initialized = false;

    /** The parent {@link Composite} of the widget */
    private Composite parent;

    /** The associated {@link FormToolkit} */
    private FormToolkit toolkit;

    /** The associated {@link ScrolledForm} */
    private ScrolledForm form;

    /** The currently selected template */
    private Template selectedTemplate;

    /** The context menu */
    private Menu contextMenu;

    /** The list of editor widgets */
    private Map<TemplateWidget, EditorWidget<? extends TemplateWidget>> editorWidgets = new HashMap<TemplateWidget, EditorWidget<? extends TemplateWidget>>();


    // ── CONSTRUCTOR: WIRING THE HOLOGRAM TO ITS CONTROL CONSOLE ─────────────────
    // Admiral Ackbar connects the holographic display to the command console (the
    // entry editor) so the two can communicate. We store the editor reference so
    // we can read the current LDAP entry and report widget events back to it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplateEditorWidget} bound to the given entry editor.
     * The editor is the source of truth for the current LDAP entry (via
     * {@link IEntryEditor#getEntryEditorInput()}) and the target for dirty
     * notifications and focus requests.
     *
     * <p>For example — wiring the hologram to the command console:</p>
     * <pre>
     *   new TemplateEditorWidget(templateEntryEditor);
     *   // "Display linked to editor. Ready to project."
     * </pre>
     *
     * @param editor  the entry editor that owns this widget; never {@code null}
     */
    public TemplateEditorWidget( IEntryEditor editor )
    {
        this.editor = editor;
    }


    // ── INIT: POWER UP THE HOLOGRAM ──────────────────────────────────────────────
    // Ackbar hits the switch and the holographic display flickers to life: the
    // FormToolkit creates the visual chrome, the toolbar gets its Refresh and
    // Display-In-Template actions, and the context menu is attached. Then we project
    // the initial form content based on whatever entry is currently loaded.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds all SWT controls: creates the {@link FormToolkit}, the scrolled form,
     * toolbar actions (Refresh, Display-In-Template chooser), and the right-click
     * context menu. Calls {@link #createFormContent()} to render the initial template
     * form. Call this once after construction, inside
     * {@link TemplateEntryEditor#createPartControl(Composite)}.
     *
     * <p>For example — powering up the hologram:</p>
     * <pre>
     *   toolkit = new FormToolkit(parent.getDisplay());
     *   form    = toolkit.createScrolledForm(parent);
     *   // Toolbar and context menu attached. Form content rendered.
     * </pre>
     *
     * @param parent  the SWT composite to build inside
     */
    public void init( Composite parent )
    {
        initialized = true;
        this.parent = parent;

        // Creating the toolkit
        toolkit = new FormToolkit( parent.getDisplay() );

        // Creating the new form
        form = toolkit.createScrolledForm( parent );
        form.getBody().setLayout( new GridLayout() );

        form.getToolBarManager().add( new RefreshAction( getEditor() ) );
        form.getToolBarManager().add( new Separator() );
        form.getToolBarManager().add( new DisplayEntryInTemplateAction( this ) );
        form.getToolBarManager().update( true );

        // Creating the new menu manager
        MenuManager menuManager = new MenuManager();
        contextMenu = menuManager.createContextMenu( form );
        form.setMenu( contextMenu );

        // Adding actions to the menu manager
        menuManager.add( new DisplayEntryInTemplateMenuManager( this ) );
        menuManager.add( new Separator() );
        menuManager.add( new RefreshAction( getEditor() ) );
        menuManager.add( new Separator() );
        menuManager.add( new SimpleActionProxy( new EditorPagePropertiesAction( getEditor() ) ) );

        createFormContent();

        parent.layout();
    }


    // ── CREATE FORM CONTENT: PROJECT THE MISSION BRIEF ───────────────────────────
    // The hologram reads the current entry dossier and decides what to display:
    // "No entry?" → error panel. "No template match?" → "no template" message.
    // "Template found?" → render the full interactive form. This is the routing
    // logic that picks the right scene for the current state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the current LDAP entry from the editor's working copy and routes to the
     * appropriate form-building method: error if no input, "no entry" message if
     * the entry is null, or the full template-rendered form if an entry and matching
     * template are available.
     */
    private void createFormContent()
    {
        EntryEditorInput entryEditorInput = getEditor().getEntryEditorInput();

        // Checking if the input is null
        if ( entryEditorInput == null )
        {
            createFormContentUnableToDisplayTheEntry();
        }
        else
        {
            // Getting the entry and the template
            IEntry entry = entryEditorInput.getSharedWorkingCopy( getEditor() );

            // Special case in the case the entry is null
            if ( entry == null )
            {
                // Hiding the context menu
                form.setMenu( null );

                // Creating the form content
                createFormContentNoEntrySelected();
            }
            else
            {
                // Showing the context menu
                form.setMenu( contextMenu );

                // Checking if a template is selected
                if ( selectedTemplate == null )
                {
                    List<Template> matchingTemplates = EntryTemplatePluginUtils.getMatchingTemplates( entry );
                    if ( ( matchingTemplates != null ) && ( matchingTemplates.size() > 0 ) )
                    {
                        // Looking for the default template
                        for ( Template matchingTemplate : matchingTemplates )
                        {
                            if ( EntryTemplatePlugin.getDefault().getTemplatesManager().isDefaultTemplate(
                                matchingTemplate ) )
                            {
                                selectedTemplate = matchingTemplate;
                                break;
                            }
                        }

                        // If no default template has been found,
                        // select the first one
                        if ( selectedTemplate == null )
                        {
                            // Assigning the first template as the selected one
                            selectedTemplate = matchingTemplates.get( 0 );
                        }

                        // Creating the form content
                        createFormContentFromTemplate();
                    }
                    else
                    {
                        // Creating the form content
                        createFormContentNoTemplateMatching();
                    }
                }
                else
                {
                    // Creating the form content
                    createFormContentFromTemplate();
                }
            }
        }

        form.layout( true, true );
    }


    // ── GET EDITOR: RETRIEVE THE COMMAND CONSOLE ─────────────────────────────────
    // Simple getter so internal methods (and the actions attached to this widget)
    // can access the entry editor to read the current entry or fire events.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the entry editor associated with this widget. Used by action classes
     * (e.g., {@link RefreshAction}) that need to reach the editor from the widget.
     *
     * @return the owning {@link IEntryEditor}; never {@code null} after construction
     */
    public IEntryEditor getEditor()
    {
        return editor;
    }


    // ── CREATE FORM CONTENT UNABLE TO DISPLAY: HOLOGRAM ERROR SCREEN ────────────
    // The hologram flashes a red warning: "Cannot display entry." This happens when
    // the editor has no input at all — typically during editor startup or after a
    // connection failure.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Renders an error panel on the form when the entry editor input is {@code null}.
     * Shows the Eclipse "error" icon and a localized "unable to display the entry"
     * message in the form title bar.
     */
    private void createFormContentUnableToDisplayTheEntry()
    {
        // Displaying an error message
        form.setText( Messages.getString( "TemplateEditorWidget.UnableToDisplayTheEntry" ) ); //$NON-NLS-1$
        form.setImage( PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_ERROR_TSK ) );
    }


    // ── CREATE FORM CONTENT FROM TEMPLATE: PROJECT THE FULL HOLOGRAM ─────────────
    // The entry is loaded, the template is chosen — the hologram projects its full
    // interactive display. We set the form title to the template's title, then walk
    // the template's widget tree and instantiate an EditorWidget for each node.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Renders the full template-driven form UI. Sets the form title to the selected
     * template's title, then recursively instantiates {@link EditorWidget} children
     * for each {@link TemplateWidget} in the template's form tree.
     *
     * <p>For example — projecting the full mission hologram:</p>
     * <pre>
     *   form.setText(selectedTemplate.getTitle()); // "User Account Template"
     *   for (TemplateWidget w : templateForm.getChildren()) createFormTemplateWidget(body, w);
     * </pre>
     */
    private void createFormContentFromTemplate()
    {
        form.setText( selectedTemplate.getTitle() );

        // Getting the template form
        TemplateForm templateForm = selectedTemplate.getForm();

        // Creating the children widgets
        if ( templateForm.hasChildren() )
        {
            for ( TemplateWidget templateWidget : templateForm.getChildren() )
            {
                createFormTemplateWidget( form.getBody(), templateWidget );
            }
        }
    }


    // ── CREATE FORM TEMPLATE WIDGET: MATERIALIZE ONE HOLOGRAPHIC PANEL ───────────
    // Each TemplateWidget in the model has a corresponding SWT EditorWidget that
    // knows how to render it. We switch on the runtime type of the model object,
    // instantiate the right editor widget, add it to our tracking map (so we can
    // later update or dispose it), and let it build its SWT composite. Then we
    // recurse on children — sections and composites can contain nested widgets.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the {@link EditorWidget} that corresponds to the given {@link TemplateWidget}
     * and attaches it to the parent SWT composite. Recurses into children for container
     * widgets (e.g., {@link TemplateSection}, {@link TemplateComposite}).
     *
     * <p>For example — materializing one holographic panel:</p>
     * <pre>
     *   if (templateWidget instanceof TemplateTextField) {
     *     EditorTextField editorTextField = new EditorTextField(...);
     *     editorWidgets.put(templateWidget, editorTextField);
     *     widgetComposite = editorTextField.createWidget(parent);
     *   }
     * </pre>
     *
     * @param parent          the SWT composite to attach this widget to
     * @param templateWidget  the model node describing the widget to create
     */
    private void createFormTemplateWidget( Composite parent, TemplateWidget templateWidget )
    {
        // The widget composite
        Composite widgetComposite = null;

        // Creating the widget according to its type
        if ( templateWidget instanceof TemplateCheckbox )
        {
            // Creating the editor checkbox
            EditorCheckbox editorCheckbox = new EditorCheckbox( getEditor(), ( TemplateCheckbox ) templateWidget,
                getToolkit() );
            editorWidgets.put( templateWidget, editorCheckbox );

            // Creating the UI
            widgetComposite = editorCheckbox.createWidget( parent );
        }
        else if ( templateWidget instanceof TemplateComposite )
        {
            // Creating the editor composite
            EditorComposite editorComposite = new EditorComposite( getEditor(), ( TemplateComposite ) templateWidget,
                getToolkit() );
            editorWidgets.put( templateWidget, editorComposite );

            // Creating the UI
            widgetComposite = editorComposite.createWidget( parent );
        }
        else if ( templateWidget instanceof TemplateDate )
        {
            // Creating the editor date
            EditorDate editorDate = new EditorDate( getEditor(), ( TemplateDate ) templateWidget, getToolkit() );
            editorWidgets.put( templateWidget, editorDate );

            // Creating the UI
            widgetComposite = editorDate.createWidget( parent );
        }
        else if ( templateWidget instanceof TemplateFileChooser )
        {
            // Creating the editor file chooser
            EditorFileChooser editorFileChooser = new EditorFileChooser( getEditor(),
                ( TemplateFileChooser ) templateWidget, getToolkit() );
            editorWidgets.put( templateWidget, editorFileChooser );

            // Creating the UI
            widgetComposite = editorFileChooser.createWidget( parent );
        }
        else if ( templateWidget instanceof TemplateImage )
        {
            // Creating the editor image
            EditorImage editorImage = new EditorImage( getEditor(), ( TemplateImage ) templateWidget, getToolkit() );
            editorWidgets.put( templateWidget, editorImage );

            // Creating the UI
            widgetComposite = editorImage.createWidget( parent );
        }
        else if ( templateWidget instanceof TemplateLabel )
        {
            // Creating the editor label
            EditorLabel editorLabel = new EditorLabel( getEditor(), ( TemplateLabel ) templateWidget, getToolkit() );
            editorWidgets.put( templateWidget, editorLabel );

            // Creating the UI
            widgetComposite = editorLabel.createWidget( parent );
        }
        else if ( templateWidget instanceof TemplateLink )
        {
            // Creating the editor link
            EditorLink editorLink = new EditorLink( getEditor(), ( TemplateLink ) templateWidget, getToolkit() );
            editorWidgets.put( templateWidget, editorLink );

            // Creating the UI
            widgetComposite = editorLink.createWidget( parent );
        }
        else if ( templateWidget instanceof TemplateListbox )
        {
            // Creating the editor link
            EditorListbox editorListbox = new EditorListbox( getEditor(), ( TemplateListbox ) templateWidget,
                getToolkit() );
            editorWidgets.put( templateWidget, editorListbox );

            // Creating the UI
            widgetComposite = editorListbox.createWidget( parent );
        }
        else if ( templateWidget instanceof TemplatePassword )
        {
            // Creating the editor password
            EditorPassword editorPassword = new EditorPassword( getEditor(), ( TemplatePassword ) templateWidget,
                getToolkit() );
            editorWidgets.put( templateWidget, editorPassword );

            // Creating the UI
            widgetComposite = editorPassword.createWidget( parent );
        }
        else if ( templateWidget instanceof TemplateRadioButtons )
        {
            // Creating the editor radio buttons
            EditorRadioButtons editorRadioButtons = new EditorRadioButtons( getEditor(),
                ( TemplateRadioButtons ) templateWidget, getToolkit() );
            editorWidgets.put( templateWidget, editorRadioButtons );

            // Creating the UI
            widgetComposite = editorRadioButtons.createWidget( parent );
        }
        else if ( templateWidget instanceof TemplateSection )
        {
            // Creating the editor section
            EditorSection editorSection = new EditorSection( getEditor(), ( TemplateSection ) templateWidget,
                getToolkit() );
            editorWidgets.put( templateWidget, editorSection );

            // Creating the UI
            widgetComposite = editorSection.createWidget( parent );
        }
        else if ( templateWidget instanceof TemplateSpinner )
        {
            // Creating the editor spinner
            EditorSpinner editorSpinner = new EditorSpinner( getEditor(), ( TemplateSpinner ) templateWidget,
                getToolkit() );
            editorWidgets.put( templateWidget, editorSpinner );

            // Creating the UI
            widgetComposite = editorSpinner.createWidget( parent );
        }
        else if ( templateWidget instanceof TemplateTable )
        {
            // Creating the editor table
            EditorTable editorTable = new EditorTable( getEditor(), ( TemplateTable ) templateWidget, getToolkit() );
            editorWidgets.put( templateWidget, editorTable );

            // Creating the UI
            widgetComposite = editorTable.createWidget( parent );
        }
        else if ( templateWidget instanceof TemplateTextField )
        {
            // Creating the editor text field
            EditorTextField editorTextField = new EditorTextField( getEditor(), ( TemplateTextField ) templateWidget,
                getToolkit() );
            editorWidgets.put( templateWidget, editorTextField );

            // Creating the UI
            widgetComposite = editorTextField.createWidget( parent );
        }

        // Recursively looping on children
        if ( templateWidget.hasChildren() )
        {
            for ( TemplateWidget templateWidgetChild : templateWidget.getChildren() )
            {
                createFormTemplateWidget( widgetComposite, templateWidgetChild );
            }
        }
    }


    // ── CREATE FORM CONTENT NO ENTRY SELECTED: IDLE HOLOGRAM ─────────────────────
    // The briefing table is empty — no dossier, no mission. The hologram shows a
    // placeholder: "No entry selected." Shown when the entry is null (user hasn't
    // selected anything in the browser tree yet).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Renders a "no entry selected" placeholder in the form title bar. Shown when
     * the editor's working copy returns a null entry — typically when no LDAP entry
     * is selected in the browser tree.
     */
    private void createFormContentNoEntrySelected()
    {
        // Displaying an error message
        form.setText( Messages.getString( "TemplateEditorWidget.NoEntrySelected" ) ); //$NON-NLS-1$
    }


    // ── CREATE FORM CONTENT NO TEMPLATE MATCHING: NO MAP FOR THIS MISSION ────────
    // Ackbar has the entry dossier but none of the loaded templates have coordinates
    // for this type of target — no template matches the entry's object classes.
    // The hologram shows: "No template is matching this entry."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Renders a "no matching template" placeholder in the form title bar. Shown when
     * the current LDAP entry doesn't match any of the loaded templates — meaning its
     * object classes don't appear in any template's definition.
     */
    private void createFormContentNoTemplateMatching()
    {
        // Displaying an error message
        form.setText( Messages.getString( "TemplateEditorWidget.NoTemplateIsMatchingThisEntry" ) ); //$NON-NLS-1$
    }


    // ── GET TOOLKIT: HAND OVER THE HOLOGRAM'S RENDERING ENGINE ──────────────────
    // The FormToolkit is the factory that creates form-aware SWT widgets (labels,
    // text fields, sections). EditorWidget subclasses need it to create their
    // own controls in a style that matches the rest of the form.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link FormToolkit} used to create all form controls in this widget.
     * EditorWidget subclasses call this to create SWT controls that visually match
     * the parent form's style (flat borders, correct fonts, etc.).
     *
     * @return the {@link FormToolkit}; valid after {@link #init(Composite)} has been called
     */
    public FormToolkit getToolkit()
    {
        return toolkit;
    }


    // ── DISPOSE: POWER DOWN THE HOLOGRAM ─────────────────────────────────────────
    // The mission is over and the briefing room is closing. We shut down the
    // FormToolkit (releases OS-level resources), dispose the scrolled form, and
    // dispose every EditorWidget we created — each of which holds SWT controls that
    // need explicit cleanup.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Releases all SWT resources held by this widget: the {@link FormToolkit},
     * the scrolled form, and every {@link EditorWidget} in the tracker map.
     * Always call this from {@link TemplateEntryEditor#dispose()} — failing to do
     * so leaks OS handles.
     */
    public void dispose()
    {
        //
        // Disposing the toolkit, form and widgets
        //

        // Toolkit
        if ( toolkit != null )
        {
            toolkit.dispose();
        }

        // Form
        if ( ( form != null ) && ( !form.isDisposed() ) )
        {
            form.dispose();
        }

        // Widgets
        for ( TemplateWidget key : editorWidgets.keySet() )
        {
            EditorWidget<?> widget = editorWidgets.get( key );
            widget.dispose();
        }
    }


    // ── UPDATE: REFRESH THE HOLOGRAM WITH LIVE DATA ───────────────────────────────
    // The field data changed — we pulse all active editor widgets so they re-read
    // the current attribute values from the LDAP working copy and repaint their
    // fields. Only runs if the hologram has been powered up (initialized).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes all active {@link EditorWidget}s by calling their {@link EditorWidget#update()}
     * methods. Each widget re-reads its attribute value from the shared working copy
     * and repaints its SWT control. No-op if the widget has not been initialized.
     */
    public void update()
    {
        if ( isInitialized() )
        {
            // Updating widgets
            for ( TemplateWidget key : editorWidgets.keySet() )
            {
                EditorWidget<?> widget = editorWidgets.get( key );
                widget.update();
            }
        }
    }


    // ── SET FOCUS: DIRECT THE OPERATOR'S ATTENTION TO THE DISPLAY ────────────────
    // When the user activates this editor tab, we set keyboard focus to the scrolled
    // form so the next keypress lands in the first focusable field.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Transfers keyboard focus to the scrolled form. Called by
     * {@link TemplateEntryEditor#setFocus()} when Eclipse activates this editor.
     */
    public void setFocus()
    {
        if ( ( form != null ) && ( !form.isDisposed() ) )
        {
            form.setFocus();
        }
    }


    // ── EDITOR INPUT CHANGED: SWAP THE MISSION DOSSIER ───────────────────────────
    // A completely different entry dossier has arrived — the hologram needs to reset.
    // We clear the selected template (since the new entry might match different ones)
    // and trigger a full UI rebuild via disposeAndRecreateUI().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the editor when the LDAP entry input changes (user navigated to a
     * different entry). Clears the currently selected template and triggers a full
     * tear-down and rebuild of the form UI so the new entry's template is used.
     */
    public void editorInputChanged()
    {
        if ( isInitialized() )
        {
            // Resetting the template
            selectedTemplate = null;

            // Updating the UI
            disposeAndRecreateUI();
        }
    }


    // ── GET MATCHING TEMPLATES: WHAT BRIEFINGS FIT THIS DOSSIER? ─────────────────
    // Runs the BFS object-class search (delegated to EntryTemplatePluginUtils) and
    // returns all templates whose object classes overlap with the current entry.
    // Used by the "Display In Template" action to populate its chooser menu.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of all templates that match the current LDAP entry's object
     * classes. Delegates to {@link EntryTemplatePluginUtils#getMatchingTemplates(IEntry)}.
     * Used to populate the template-chooser action menu.
     *
     * @return a {@link List} of matching {@link Template}s; may be empty, never {@code null}
     */
    public List<Template> getMatchingTemplates()
    {
        return EntryTemplatePluginUtils.getMatchingTemplates( getEditor().getEntryEditorInput().getSharedWorkingCopy(
            getEditor() ) );
    }


    // ── GET SELECTED TEMPLATE: WHICH BRIEFING FORM IS ACTIVE? ───────────────────
    // Returns the template currently being rendered — used by the "Display In
    // Template" action to check which menu item to display with a checkmark.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the template that is currently being used to render the form.
     * May be {@code null} if no entry is loaded or no matching template exists.
     *
     * @return the currently selected {@link Template}, or {@code null}
     */
    public Template getSelectedTemplate()
    {
        return selectedTemplate;
    }


    // ── SWITCH TEMPLATE: CHANGE THE HOLOGRAPHIC DISPLAY ON THE FLY ───────────────
    // The operator selects a different briefing template from the chooser menu.
    // We store the new selection and trigger a full UI rebuild so the form updates
    // to the newly chosen template's layout.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Changes the currently displayed template to {@code selectedTemplate} and
     * triggers a full tear-down and rebuild of the form UI. Called by
     * {@link org.apache.directory.studio.templateeditor.actions.SwitchTemplateAction#run()}.
     *
     * <p>For example — changing the holographic display on the fly:</p>
     * <pre>
     *   widget.switchTemplate(staffTemplate);
     *   // "Hologram switching to Staff Record view."
     * </pre>
     *
     * @param selectedTemplate  the template to switch to; must not be {@code null}
     */
    public void switchTemplate( Template selectedTemplate )
    {
        // Assigning the selected template
        this.selectedTemplate = selectedTemplate;

        // Updating the UI
        disposeAndRecreateUI();
    }


    // ── DISPOSE AND RECREATE UI: RESET AND REPROJECT THE HOLOGRAM ────────────────
    // Ackbar clears the holographic display, disposes all the SWT controls, then
    // re-initializes from scratch. This is the nuclear option for when either the
    // template or the entry changes — a full teardown followed by a fresh init().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Tears down all current SWT controls (form and all editor widgets) and rebuilds
     * from scratch by calling {@link #init(Composite)} again. Only runs if the widget
     * has been initialized. This is the correct approach for template switches and
     * entry changes because the entire widget tree needs to be replaced.
     */
    private void disposeAndRecreateUI()
    {
        if ( isInitialized() )
        {
            // Disposing the previously created form
            if ( ( form != null ) && ( !form.isDisposed() ) )
            {
                // Disposing the from (and all it's children elements
                form.dispose();

                // Disposing template widgets
                for ( TemplateWidget key : editorWidgets.keySet() )
                {
                    EditorWidget<?> widget = editorWidgets.get( key );
                    widget.dispose();
                }
            }

            // Clearing all previously created editor widgets (which are now disposed)
            editorWidgets.clear();

            // Recreating the UI
            init( parent );
        }
    }


    // ── GET FORM: EXPOSE THE ROOT SCROLLED FORM ───────────────────────────────────
    // External callers (e.g., the navigation location class) occasionally need
    // direct access to the root SWT form to read or set properties on it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the root {@link ScrolledForm} that contains all template widgets.
     * May be {@code null} before {@link #init(Composite)} is called or after
     * {@link #dispose()}.
     *
     * @return the root {@link ScrolledForm}, or {@code null}
     */
    public ScrolledForm getForm()
    {
        return form;
    }


    // ── IS INITIALIZED: HAS THE HOLOGRAM BEEN POWERED UP? ────────────────────────
    // Guards against calling update() or editorInputChanged() before init() has run.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if {@link #init(Composite)} has been called and the SWT
     * controls have been created. Guards {@link #update()} and {@link #editorInputChanged()}
     * from running before the UI exists.
     *
     * @return {@code true} if initialized; {@code false} otherwise
     */
    public boolean isInitialized()
    {
        return initialized;
    }
}
