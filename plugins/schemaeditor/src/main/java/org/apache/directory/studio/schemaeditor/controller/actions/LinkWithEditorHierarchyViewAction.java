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


import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.editors.attributetype.AttributeTypeEditor;
import org.apache.directory.studio.schemaeditor.view.editors.objectclass.ObjectClassEditor;
import org.apache.directory.studio.schemaeditor.view.views.HierarchyView;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;


// ── CLASS: LinkWithEditorHierarchyViewAction — OBI-WAN SENSING A DISTURBANCE ─
// On the Millennium Falcon, Obi-Wan suddenly closes his eyes and goes still —
// he felt the destruction of Alderaan through the Force before any sensor
// reported it.  He's not waiting for a message; he's permanently tuned in,
// and the moment something changes he reacts.
// This class works the same way: when the user enables linking, we attach a
// part listener to the workbench and react the instant any editor becomes
// visible — immediately updating the Hierarchy View to reflect what's now open.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A toggle action that keeps the Hierarchy View synchronized with whichever
 * schema editor is currently visible in the workbench.
 * When enabled, we register an {@link IPartListener2} that fires whenever an
 * editor becomes visible and pushes its schema element into the Hierarchy View
 * so the user always sees the type tree for what they're editing.
 * Think of this class as Obi-Wan's permanent connection to the Force: once
 * switched on, it senses every relevant disturbance (editor activation) and
 * responds without the user having to do anything extra.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LinkWithEditorHierarchyViewAction extends Action
{
    /** The String for storing the checked state of the action */
    private static final String LINK_WITH_EDITOR_HIERARCHY_VIEW_DS_KEY = LinkWithEditorHierarchyViewAction.class
        .getName()
        + ".dialogsettingkey"; //$NON-NLS-1$

    /** The associated view */
    private HierarchyView view;

    /** The listener listening on changes on editors */
    private IPartListener2 editorListener = new IPartListener2()
    {

        // ── OBI-WAN FEELS A DISTURBANCE — PART BECOMES VISIBLE ───────────────
        // Obi-Wan's eyes snap open the moment Alderaan is destroyed — he didn't
        // need to see it; the Force told him instantly.
        // This callback fires when any workbench part becomes visible; if it's
        // one of our schema editors we immediately sync the Hierarchy View.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Called by Eclipse when a workbench part becomes visible.
         * If the newly visible part is an ObjectClass or AttributeType editor,
         * we push its schema element into the Hierarchy View right away.
         *
         * @param partRef  reference to the part that just became visible
         */
        public void partVisible( IWorkbenchPartReference partRef )
        {
            IWorkbenchPart part = partRef.getPart( true );

            if ( part instanceof ObjectClassEditor )
            {
                linkViewWithEditor( ( ( ObjectClassEditor ) part ).getOriginalObjectClass() );
            }
            else if ( part instanceof AttributeTypeEditor )
            {
                linkViewWithEditor( ( ( AttributeTypeEditor ) part ).getOriginalAttributeType() );
            }
        }


        // ── PART ACTIVATED — NOT OUR TRIGGER ─────────────────────────────────
        // Obi-Wan is selective about what he reacts to; minor shuffling on the
        // ship doesn't break his meditation.
        // We only care about "visible" events, not "activated", so this is empty.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Called when a part is activated. We react on {@code partVisible} instead,
         * so we do nothing here.
         *
         * @param partRef  the activated part reference; unused
         */
        public void partActivated( IWorkbenchPartReference partRef )
        {
        }


        // ── PART CLOSED — WE LET IT GO ────────────────────────────────────────
        // When a door closes on the Falcon, Obi-Wan doesn't chase after it —
        // he stays focused on what's still open in front of him.
        // We don't react to close events here.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Called when a part is closed. We take no action on close.
         *
         * @param partRef  the closed part reference; unused
         */
        public void partClosed( IWorkbenchPartReference partRef )
        {
        }


        // ── PART DEACTIVATED — KEEPING FOCUS ─────────────────────────────────
        // Obi-Wan doesn't lose his connection to the Force just because Han
        // walks out of the room.
        // Deactivation doesn't require any response from us.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Called when a part is deactivated. No response needed.
         *
         * @param partRef  the deactivated part reference; unused
         */
        public void partDeactivated( IWorkbenchPartReference partRef )
        {
        }


        // ── PART HIDDEN — OUT OF SIGHT, OUT OF MIND ──────────────────────────
        // When a part hides behind another window, Obi-Wan turns his attention
        // elsewhere — the visible world takes priority.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Called when a part is hidden. We take no action.
         *
         * @param partRef  the hidden part reference; unused
         */
        public void partHidden( IWorkbenchPartReference partRef )
        {
        }


        // ── INPUT CHANGED — NOT RELEVANT HERE ────────────────────────────────
        // Obi-Wan reacts to the type of disturbance, not every tremor; input
        // changes on a part don't register as something we need to handle.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Called when a part's input changes. We do nothing here.
         *
         * @param partRef  the part reference whose input changed; unused
         */
        public void partInputChanged( IWorkbenchPartReference partRef )
        {
        }


        // ── PART OPENED — WE WAIT FOR IT TO BECOME VISIBLE ───────────────────
        // Opening a door is different from stepping through it; Obi-Wan waits
        // for the full event — we fire on partVisible, not partOpened.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Called when a part is opened. We react on {@code partVisible} instead.
         *
         * @param partRef  the opened part reference; unused
         */
        public void partOpened( IWorkbenchPartReference partRef )
        {
        }


        // ── PART BROUGHT TO TOP — VISIBLE HANDLES IT ─────────────────────────
        // If a part surfaces to the top it will also trigger partVisible, which
        // is where we actually do our work.  No need to act here too.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Called when a part is brought to the top of its stack.
         * We rely on {@code partVisible} for this scenario.
         *
         * @param partRef  the part reference brought to top; unused
         */
        public void partBroughtToTop( IWorkbenchPartReference partRef )
        {
        }
    };


    // ── OBI-WAN OPENS HIS EYES AND ATTUNES TO THE FORCE ─────────────────────
    // Aboard the Falcon, Obi-Wan sits cross-legged, breathes slowly, and opens
    // his connection to the Force — reading the persisted "sensitivity level"
    // he established before the last jump to hyperspace, then either engaging
    // full attention or remaining in passive standby.
    // We restore checked state from dialog settings (was linking enabled last
    // session?) and conditionally register the part listener right away.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the Link With Editor action for the Hierarchy View and restores
     * whatever checked state the user left us in last session.
     * If linking was already on, we immediately attach the part listener so
     * we don't miss any editors that open before the user touches this toggle.
     *
     * @param view  the {@link HierarchyView} we will push schema elements into
     *              when an editor becomes visible
     */
    public LinkWithEditorHierarchyViewAction( HierarchyView view )
    {
        super( Messages.getString( "LinkWithEditorHierarchyViewAction.LinkEditorAction" ), AS_CHECK_BOX ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "LinkWithEditorHierarchyViewAction.LinkEditorToolTip" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_LINK_WITH_EDITOR ) );
        setEnabled( true );
        this.view = view;

        // Setting up the default key value (if needed)
        if ( Activator.getDefault().getDialogSettings().get( LINK_WITH_EDITOR_HIERARCHY_VIEW_DS_KEY ) == null )
        {
            Activator.getDefault().getDialogSettings().put( LINK_WITH_EDITOR_HIERARCHY_VIEW_DS_KEY, false );
        }

        // Setting state from the dialog settings
        setChecked( Activator.getDefault().getDialogSettings().getBoolean( LINK_WITH_EDITOR_HIERARCHY_VIEW_DS_KEY ) );

        // Enabling the listeners
        if ( isChecked() )
        {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener( editorListener );
        }
    }


    // ── OBI-WAN TOGGLES HIS FORCE SENSITIVITY ────────────────────────────────
    // Obi-Wan either opens fully to the Force — registering every disturbance
    // as it ripples past — or settles into a quiet, shielded meditative state
    // where the outside world stops reaching him.
    // When the toggle turns on we add the part listener (and immediately sync
    // to whatever editor is active); when it turns off we remove the listener.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Toggles the link-with-editor behavior on or off and persists the new state
     * to dialog settings so it survives session restarts.
     * When turned on we also do an immediate sync: if an ObjectClass or
     * AttributeType editor is already active, we push its element to the view
     * right away rather than waiting for the next editor activation.
     */
    public void run()
    {
        setChecked( isChecked() );
        Activator.getDefault().getDialogSettings().put( LINK_WITH_EDITOR_HIERARCHY_VIEW_DS_KEY, isChecked() );

        if ( isChecked() ) // Enabling the listeners
        {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener( editorListener );

            IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .getActiveEditor();
            if ( activeEditor instanceof ObjectClassEditor )
            {
                linkViewWithEditor( ( ( ObjectClassEditor ) activeEditor ).getOriginalObjectClass() );
            }
            else if ( activeEditor instanceof AttributeTypeEditor )
            {
                linkViewWithEditor( ( ( AttributeTypeEditor ) activeEditor ).getOriginalAttributeType() );
            }
        }
        else
        // Disabling the listeners
        {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().removePartListener( editorListener );
        }
    }


    // ── OBI-WAN IDENTIFIES THE DISTURBANCE AND RESPONDS ─────────────────────
    // Obi-Wan senses the specific nature of the tremor in the Force — is it
    // a planet being destroyed or just a mynock chewing on a power cable? —
    // then responds appropriately, redirecting his full attention to the source.
    // We check whether the schema element is an AttributeType or ObjectClass
    // and push it into the Hierarchy View either way.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Pushes a schema element into the Hierarchy View so the view's tree
     * updates to show the type hierarchy for whatever the active editor is showing.
     * Works for both {@link AttributeType} and {@link ObjectClass} elements.
     *
     * @param schemaElement  the schema object from the active editor to display
     *                       in the Hierarchy View
     */
    private void linkViewWithEditor( SchemaObject schemaElement )
    {
        if ( schemaElement instanceof AttributeType )
        {
            view.setInput( schemaElement );
        }
        else if ( schemaElement instanceof ObjectClass )
        {
            view.setInput( schemaElement );
        }
    }
}
