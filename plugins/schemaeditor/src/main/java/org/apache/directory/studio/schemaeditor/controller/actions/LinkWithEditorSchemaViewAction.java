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
import org.apache.directory.studio.schemaeditor.view.editors.attributetype.AttributeTypeEditor;
import org.apache.directory.studio.schemaeditor.view.editors.objectclass.ObjectClassEditor;
import org.apache.directory.studio.schemaeditor.view.editors.schema.SchemaEditor;
import org.apache.directory.studio.schemaeditor.view.views.SchemaView;
import org.apache.directory.studio.schemaeditor.view.views.SchemaViewContentProvider;
import org.apache.directory.studio.schemaeditor.view.wrappers.AttributeTypeWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.ObjectClassWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.SchemaWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.TreeNode;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: LinkWithEditorSchemaViewAction — OBI-WAN SENSING A DISTURBANCE ────
// On the Millennium Falcon, Obi-Wan sits quietly but never truly stops listening
// to the Force — the moment Alderaan is destroyed he feels it instantly, then
// turns his attention to respond.  The key thing: it works both ways.  He feels
// the galaxy (editors), but the galaxy also reacts to him (the view selection
// drives which editor comes forward).
// This class is a bidirectional Force-link between the Schema View and its
// editors: an editor becoming visible selects the matching item in the view,
// and a selection in the view brings the matching editor to the front.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A toggle action that keeps the Schema View and its editors synchronized in
 * both directions: editor activation updates the Schema View selection, and
 * Schema View selection brings the matching editor to the front.
 * We maintain two listeners — one on editor part visibility, one on Schema View
 * selection — and wire/unwire them together whenever the toggle changes state.
 * Think of this class as Obi-Wan's two-way Force bond: he senses disturbances
 * (editors) and those around him also sense his focus (view selection).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LinkWithEditorSchemaViewAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The String for storing the checked state of the action */
    private static final String LINK_WITH_EDITOR_SCHEMAS_VIEW_DS_KEY = LinkWithEditorSchemaViewAction.class.getName()
        + ".dialogsettingkey"; //$NON-NLS-1$

    /** The associated view */
    private SchemaView view;

    /** The listener listening on changes on editors */
    private IPartListener2 editorListener = new IPartListener2()
    {

        // ── OBI-WAN FEELS THE EDITOR BECOME VISIBLE ──────────────────────────
        // Obi-Wan's eyes open — a new presence has surfaced in the Force.
        // He identifies it as an ObjectClass, AttributeType, or Schema editor
        // and immediately shifts his attention (Schema View selection) to match.
        // We briefly pause the view listener while we update the selection so we
        // don't trigger an infinite echo back to the editor.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Called when a workbench part becomes visible.
         * If it is one of our schema editors, we update the Schema View's
         * selection to highlight the corresponding tree node.
         * We temporarily remove the view listener to avoid selection feedback loops.
         *
         * @param partRef  reference to the part that just became visible
         */
        public void partVisible( IWorkbenchPartReference partRef )
        {
            IWorkbenchPart part = partRef.getPart( true );

            if ( part instanceof ObjectClassEditor )
            {
                view.getSite().getPage().removePostSelectionListener( viewListener );
                linkViewWithEditor( ( ( ObjectClassEditor ) part ).getOriginalObjectClass() );
                view.getSite().getPage().addPostSelectionListener( viewListener );
            }
            else if ( part instanceof AttributeTypeEditor )
            {
                view.getSite().getPage().removePostSelectionListener( viewListener );
                linkViewWithEditor( ( ( AttributeTypeEditor ) part ).getOriginalAttributeType() );
                view.getSite().getPage().addPostSelectionListener( viewListener );
            }
            else if ( part instanceof SchemaEditor )
            {
                view.getSite().getPage().removePostSelectionListener( viewListener );
                linkViewWithEditor( ( ( SchemaEditor ) part ).getSchema() );
                view.getSite().getPage().addPostSelectionListener( viewListener );
            }
        }


        // ── PART ACTIVATED — NOT OUR PRIMARY SIGNAL ──────────────────────────
        // Obi-Wan doesn't react to every flicker; activation alone is too noisy.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Called when a part is activated. We use {@code partVisible} instead.
         *
         * @param partRef  the activated part reference; unused
         */
        public void partActivated( IWorkbenchPartReference partRef )
        {
        }


        /**
         * Called when a part is closed. No response needed.
         *
         * @param partRef  the closed part reference; unused
         */
        public void partClosed( IWorkbenchPartReference partRef )
        {
        }


        /**
         * Called when a part is deactivated. No response needed.
         *
         * @param partRef  the deactivated part reference; unused
         */
        public void partDeactivated( IWorkbenchPartReference partRef )
        {
        }


        /**
         * Called when a part is hidden. No response needed.
         *
         * @param partRef  the hidden part reference; unused
         */
        public void partHidden( IWorkbenchPartReference partRef )
        {
        }


        /**
         * Called when a part's input changes. No response needed.
         *
         * @param partRef  the part reference whose input changed; unused
         */
        public void partInputChanged( IWorkbenchPartReference partRef )
        {
        }


        /**
         * Called when a part is opened. We act on {@code partVisible} instead.
         *
         * @param partRef  the opened part reference; unused
         */
        public void partOpened( IWorkbenchPartReference partRef )
        {
        }


        /**
         * Called when a part is brought to the top of its stack.
         * {@code partVisible} handles this scenario for us.
         *
         * @param partRef  the part reference brought to top; unused
         */
        public void partBroughtToTop( IWorkbenchPartReference partRef )
        {
        }
    };

    /** The listener listening on changes on the view */
    private ISelectionListener viewListener = new ISelectionListener()
    {
        // ── THE GALAXY RESPONDS TO OBI-WAN'S FOCUS ────────────────────────────
        // When Obi-Wan turns his full attention on something in the Force, that
        // thing becomes the center of everyone's awareness — the officers on the
        // bridge scramble to bring it forward.
        // When the user selects a node in the Schema View, we scan all open editors
        // and bring the matching one to the front.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Called when the Schema View's selection changes.
         * If the selection is a schema element wrapper, we find the matching
         * open editor and bring it to the front of the workbench.
         *
         * @param part       the workbench part that fired the selection; unused
         * @param selection  the new selection — we check for schema wrappers
         */
        public void selectionChanged( IWorkbenchPart part, ISelection selection )
        {
            IStructuredSelection iSelection = ( IStructuredSelection ) selection;

            Object selectedObject = iSelection.getFirstElement();

            if ( ( selectedObject instanceof SchemaWrapper ) || ( selectedObject instanceof ObjectClassWrapper )
                || ( selectedObject instanceof AttributeTypeWrapper ) )
            {
                linkEditorWithView( ( TreeNode ) selectedObject );
            }
        }
    };


    // ── OBI-WAN TAKES HIS POSITION AND ATTUNES ───────────────────────────────
    // Obi-Wan finds his seat on the Falcon, closes his eyes, checks how
    // sensitive his Force awareness was set last time, and either opens up
    // fully or stays in passive mode — exactly where he left off.
    // We restore checked state from dialog settings and, if linking was already
    // on, wire both listeners immediately so we don't miss anything.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the Link With Editor action for the Schema View and restores
     * the persisted toggle state from the last session.
     * If linking was already on, we attach both the editor part listener and
     * the view selection listener right away.
     *
     * @param view  the {@link SchemaView} to keep in sync with the active editor
     */
    public LinkWithEditorSchemaViewAction( SchemaView view )
    {
        super( Messages.getString( "LinkWithEditorSchemaViewAction.LinkEditorAction" ), AS_CHECK_BOX ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "LinkWithEditorSchemaViewAction.LinkEditorToolTip" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_LINK_WITH_EDITOR ) );
        setEnabled( false );
        this.view = view;

        // Setting up the default key value (if needed)
        if ( Activator.getDefault().getDialogSettings().get( LINK_WITH_EDITOR_SCHEMAS_VIEW_DS_KEY ) == null )
        {
            Activator.getDefault().getDialogSettings().put( LINK_WITH_EDITOR_SCHEMAS_VIEW_DS_KEY, false );
        }

        // Setting state from the dialog settings
        setChecked( Activator.getDefault().getDialogSettings().getBoolean( LINK_WITH_EDITOR_SCHEMAS_VIEW_DS_KEY ) );

        // Enabling the listeners
        if ( isChecked() )
        {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener( editorListener );
            view.getSite().getPage().addPostSelectionListener( viewListener );
        }
    }


    // ── OBI-WAN OPENS OR SHUTS HIS FORCE AWARENESS ───────────────────────────
    // Obi-Wan either expands his awareness to feel every ripple — registering
    // both listeners and immediately syncing to the current editor — or he
    // shields himself and goes quiet, deregistering both listeners cleanly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Toggles bidirectional linking on or off and persists the state.
     * When turned on we register both listeners and do an immediate sync from
     * whichever editor is currently active.
     * When turned off we deregister both listeners so neither direction fires.
     */
    public void run()
    {
        setChecked( isChecked() );
        Activator.getDefault().getDialogSettings().put( LINK_WITH_EDITOR_SCHEMAS_VIEW_DS_KEY, isChecked() );

        if ( isChecked() ) // Enabling the listeners
        {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener( editorListener );

            IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .getActiveEditor();
            if ( activeEditor instanceof ObjectClassEditor )
            {
                view.getSite().getPage().removePostSelectionListener( viewListener );
                linkViewWithEditor( ( ( ObjectClassEditor ) activeEditor ).getOriginalObjectClass() );
                view.getSite().getPage().addPostSelectionListener( viewListener );
            }
            else if ( activeEditor instanceof AttributeTypeEditor )
            {
                view.getSite().getPage().removePostSelectionListener( viewListener );
                linkViewWithEditor( ( ( AttributeTypeEditor ) activeEditor ).getOriginalAttributeType() );
                view.getSite().getPage().addPostSelectionListener( viewListener );
            }
            else if ( activeEditor instanceof SchemaEditor )
            {
                view.getSite().getPage().removePostSelectionListener( viewListener );
                linkViewWithEditor( ( ( SchemaEditor ) activeEditor ).getSchema() );
                view.getSite().getPage().addPostSelectionListener( viewListener );
            }

            view.getSite().getPage().addPostSelectionListener( viewListener );
        }
        else
        // Disabling the listeners
        {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().removePartListener( editorListener );
            view.getSite().getPage().removePostSelectionListener( viewListener );
        }
    }


    // ── OBI-WAN LOCATES THE PRESENCE IN THE SCHEMA TREE ──────────────────────
    // Obi-Wan senses the specific element and guides everyone's eyes to exactly
    // where it sits in the order of things — pointing at the node in the tree
    // as if saying "there, that one."
    // We ask the content provider for the tree wrapper node that corresponds to
    // the schema object and drive the Schema View's selection to that wrapper.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the Schema View's selection to highlight the tree node that
     * corresponds to the given schema object (schema, object class, or attribute type).
     * If the content provider can't find a matching wrapper — e.g. the item
     * isn't in the current project — we silently do nothing.
     *
     * @param o  the schema object to select in the view (a Schema, ObjectClass,
     *           or AttributeType)
     */
    private void linkViewWithEditor( Object o )
    {
        TreeNode wrapper = ( ( SchemaViewContentProvider ) view.getViewer().getContentProvider() ).getWrapper( o );
        if ( wrapper != null )
        {
            view.getViewer().setSelection( new StructuredSelection( wrapper ) );
        }
    }


    // ── THE GALAXY FOCUSES — EDITOR COMES FORWARD ────────────────────────────
    // When the Force concentrates on a point, every present officer feels
    // compelled to turn and look at the same thing.  We scan all open editors
    // and bring the one that matches the view's selected wrapper to the front.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Brings the editor that matches the selected Schema View tree node to the
     * front of the workbench.
     * We walk all open editor references and call {@code bringToTop} on the first
     * one whose content matches the selected wrapper.
     *
     * @param wrapper  the tree node selected in the Schema View; we use it to
     *                 identify the right editor to surface
     */
    private void linkEditorWithView( TreeNode wrapper )
    {
        IEditorReference[] references = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
            .getEditorReferences();

        for ( IEditorReference reference : references )
        {
            IWorkbenchPart workbenchPart = reference.getPart( true );

            if ( ( workbenchPart instanceof ObjectClassEditor ) && ( wrapper instanceof ObjectClassWrapper ) )
            {
                ObjectClassEditor editor = ( ObjectClassEditor ) workbenchPart;
                ObjectClassWrapper ocw = ( ObjectClassWrapper ) wrapper;
                if ( editor.getOriginalObjectClass().equals( ocw.getObjectClass() ) )
                {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().bringToTop( workbenchPart );
                    return;
                }
            }
            else if ( ( workbenchPart instanceof AttributeTypeEditor ) && ( wrapper instanceof AttributeTypeWrapper ) )
            {
                AttributeTypeEditor editor = ( AttributeTypeEditor ) workbenchPart;
                AttributeTypeWrapper atw = ( AttributeTypeWrapper ) wrapper;
                if ( editor.getOriginalAttributeType().equals( atw.getAttributeType() ) )
                {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().bringToTop( workbenchPart );
                    return;
                }
            }
            else if ( ( workbenchPart instanceof SchemaEditor ) && ( wrapper instanceof SchemaWrapper ) )
            {
                SchemaEditor editor = ( SchemaEditor ) workbenchPart;
                SchemaWrapper sw = ( SchemaWrapper ) wrapper;
                if ( editor.getSchema().equals( sw.getSchema() ) )
                {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().bringToTop( workbenchPart );
                    return;
                }
            }
        }
    }


    // ── CHAIN-OF-COMMAND PASS-THROUGH ────────────────────────────────────────
    // The senior officer relays the toggle command to Obi-Wan unchanged.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} when Eclipse calls us as an
     * {@link IWorkbenchWindowActionDelegate}.
     *
     * @param action  the proxy action; unused
     */
    public void run( IAction action )
    {
        run();
    }


    // ── OBI-WAN ENDS HIS MEDITATION — NO CLEANUP NEEDED ─────────────────────
    // When the session ends, Obi-Wan simply opens his eyes; there is nothing
    // to stow away.  We hold no resources beyond the listeners, which Eclipse
    // will garbage-collect when the part service goes down.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is being disposed.
     * We hold no independent resources to release here.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── BRIEFED ON THE FALCON'S COCKPIT LAYOUT ────────────────────────────────
    // Before taking position, Obi-Wan is shown which controls do what.
    // We don't need the workbench window reference for our logic.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is initialized with the
     * active workbench window.
     * We don't need the window reference here.
     *
     * @param window  the active workbench window; unused
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── OBI-WAN IGNORES UNRELATED DISTURBANCES ───────────────────────────────
    // Not every tremor in the Force demands attention; workbench selection
    // changes don't affect our toggle state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench selection changes.
     * Our enabled state doesn't depend on selection, so we do nothing.
     *
     * @param action     the proxy action; unused
     * @param selection  the current workbench selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
