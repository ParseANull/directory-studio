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

package org.apache.directory.studio.ldapbrowser.ui.editors.entry;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidgetUniversalListener;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;


// ── CLASS: EntryEditorUniversalListener — OBI-WAN SENSING A DISTURBANCE ───────
// Obi-Wan Kenobi closes his eyes and feels the Force ripple around him — someone
// called out across the galaxy and he felt it before the message even arrived.
// EntryEditorUniversalListener is that Force sensitivity: it registers listeners
// for workbench part activation/deactivation events and LDAP model modification events,
// then routes each disturbance to exactly the right responder (action group, outline
// page, or viewer refresh) without the editor itself having to constantly poll.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The "nervous system" of the entry editor — it listens to Eclipse part lifecycle
 * events and LDAP model change events and routes them to the right handler.
 * When the editor gains focus, we activate keyboard shortcuts; when it loses focus,
 * we deactivate them. When an LDAP entry is modified, we refresh the viewer and outline.
 * Think of this class as Obi-Wan: it feels every disturbance in the Force and responds
 * before anyone even asks it to.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorUniversalListener extends EntryEditorWidgetUniversalListener
{
    /** The entry editor */
    private EntryEditor entryEditor;

    /** Token used to activate and deactivate shortcuts in the editor */
    private IContextActivation contextActivation;

    /** The part listener used to activate and deactivate the shortcuts */
    private IPartListener2 partListener = new IPartListener2()
    {
        /**
         * {@inheritDoc}
         *
         * This implementation deactivates the shortcuts when the part is deactivated.
         */
        public void partDeactivated( IWorkbenchPartReference partRef )
        {
            if ( partRef.getPart( false ) == entryEditor && contextActivation != null )
            {

                entryEditor.getActionGroup().deactivateGlobalActionHandlers();

                IContextService contextService = ( IContextService ) PlatformUI.getWorkbench().getAdapter(
                    IContextService.class );
                contextService.deactivateContext( contextActivation );
                contextActivation = null;
            }
        }


        /**
         * {@inheritDoc}
         *
         * This implementation activates the shortcuts when the part is activated.
         */
        public void partActivated( IWorkbenchPartReference partRef )
        {
            if ( partRef.getPart( false ) == entryEditor )
            {

                IContextService contextService = ( IContextService ) PlatformUI.getWorkbench().getAdapter(
                    IContextService.class );
                contextActivation = contextService.activateContext( BrowserCommonConstants.CONTEXT_WINDOWS );
                // org.eclipse.ui.contexts.dialogAndWindow
                // org.eclipse.ui.contexts.window
                // org.eclipse.ui.text_editor_context

                entryEditor.getActionGroup().activateGlobalActionHandlers();
            }
        }


        /**
         * {@inheritDoc}
         */
        public void partBroughtToTop( IWorkbenchPartReference partRef )
        {
        }


        /**
         * {@inheritDoc}
         */
        public void partClosed( IWorkbenchPartReference partRef )
        {
        }


        /**
         * {@inheritDoc}
         */
        public void partOpened( IWorkbenchPartReference partRef )
        {
        }


        /**
         * {@inheritDoc}
         */
        public void partHidden( IWorkbenchPartReference partRef )
        {
        }


        /**
         * {@inheritDoc}
         */
        public void partVisible( IWorkbenchPartReference partRef )
        {
        }


        /**
         * {@inheritDoc}
         */
        public void partInputChanged( IWorkbenchPartReference partRef )
        {
        }
    };


    // ── OBI-WAN OPENS HIMSELF TO THE FORCE AND STARTS LISTENING ──────────────
    // Obi-Wan centers himself, opens his awareness to the Force, and attaches
    // his senses to the flow of events around him — from this point on, nothing
    // happens on the workbench or in the LDAP model without him noticing.
    // We register the part listener with the workbench page so we hear about
    // focus changes, and call super to hook up the LDAP model event listeners.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the universal listener and begins monitoring the workbench and LDAP model.
     * We delegate LDAP model event wiring to the superclass and additionally register
     * a workbench part listener so we can activate/deactivate keyboard shortcuts in sync
     * with the editor's focus state.
     *
     * <p>For example — Obi-Wan opens his awareness:</p>
     * <pre>
     *   super();                            // hook up LDAP model listeners
     *   page.addPartListener(partListener); // watch for focus changes
     * </pre>
     *
     * @param entryEditor  The entry editor this listener serves.
     */
    public EntryEditorUniversalListener( EntryEditor entryEditor )
    {
        super( entryEditor.getMainWidget().getViewer(), entryEditor.getConfiguration(), entryEditor.getActionGroup(),
            entryEditor.getActionGroup().getOpenDefaultEditorAction() );
        this.entryEditor = entryEditor;

        // register listeners
        entryEditor.getSite().getPage().addPartListener( partListener );
    }


    // ── OBI-WAN SEVERS HIS CONNECTION TO THE FORCE DELIBERATELY ──────────────
    // Obi-Wan withdraws his Force sensitivity — not because he's dying, but because
    // the mission is over and leaving the channel open would waste energy.
    // We remove the part listener and null the editor reference to let GC clean up;
    // then we let the superclass unhook the LDAP model listeners.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Detaches all listeners and releases resources.
     * We remove the workbench part listener first (to stop receiving focus events),
     * then null the editor reference, then let the superclass clean up LDAP model listeners.
     */
    public void dispose()
    {
        if ( entryEditor != null )
        {
            // deregister listeners
            entryEditor.getSite().getPage().removePartListener( partListener );
            entryEditor = null;
        }

        super.dispose();
    }


    // ── OBI-WAN FEELS A GREAT DISTURBANCE IN THE FORCE ───────────────────────
    // "I felt a great disturbance in the Force, as if millions of voices suddenly
    // cried out..." — Obi-Wan senses the change immediately and responds.
    // When an LDAP entry is modified, we refresh the main viewer and then
    // tell the outline page to update its tree to reflect the new state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Handles an LDAP entry modification event by refreshing the viewer and outline page.
     * We guard against a disposed or null viewer first (can happen during close), then
     * let the superclass do its standard refresh, expand folded attributes, and finally
     * poke the outline page to sync its tree with the updated entry structure.
     *
     * <p>For example — the disturbance is felt and acted upon:</p>
     * <pre>
     *   super.entryUpdated(event);  // refresh the main attribute table
     *   expandFoldedAttributes();   // make sure nothing is collapsed away
     *   outlinePage.refresh();      // sync the outline tree
     * </pre>
     *
     * @param event  The LDAP model event describing what changed in the entry.
     */
    public void entryUpdated( EntryModificationEvent event )
    {
        if ( viewer == null || viewer.getTree() == null || viewer.getTree().isDisposed() || viewer.getInput() == null )
        {
            return;
        }

        super.entryUpdated( event );
        expandFoldedAttributes();

        EntryEditorOutlinePage outlinePage = ( EntryEditorOutlinePage ) entryEditor
            .getAdapter( IContentOutlinePage.class );
        if ( outlinePage != null )
        {
            outlinePage.refresh();
        }
    }

}
