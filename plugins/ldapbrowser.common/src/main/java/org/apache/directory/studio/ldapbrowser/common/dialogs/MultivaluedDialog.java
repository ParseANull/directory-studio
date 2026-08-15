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

package org.apache.directory.studio.ldapbrowser.common.dialogs;


import java.util.Iterator;

import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidget;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidgetActionGroup;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidgetConfiguration;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidgetUniversalListener;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.OpenDefaultEditorAction;
import org.apache.directory.studio.ldapbrowser.core.events.AttributeDeletedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EmptyValueAddedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EmptyValueDeletedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.ValueAddedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueDeletedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueModifiedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueMultiModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.utils.CompoundModification;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;


// ── CLASS: MultivaluedDialog — C-3PO LISTING ALL SIX MILLION FORMS ───────────
// C-3PO is fluent in over six million forms of communication.  When asked, he
// doesn't just give one example — he enumerates every value in his knowledge
// base, one by one.  You can add a new language, remove an obsolete one, or
// correct a mistranslation on the spot, and C-3PO updates his internal list
// immediately.
// LDAP attributes can also be multi-valued — for example, a person entry might
// have three different email addresses or a dozen telephone numbers.  This dialog
// shows all the values for an attribute hierarchy in a mini entry-editor tree and
// lets the user add, edit, and delete them.  Changes are applied to a cloned copy
// of the entry first; only on OK are the real attribute values replaced.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A dialog for viewing and editing multi-valued LDAP attributes.  It embeds an
 * {@link EntryEditorWidget} (the same tree view used in the main entry editor)
 * restricted to a single {@link AttributeHierarchy}.  Edits are made against a
 * cloned copy of the entry — the original is only updated when the user confirms
 * with OK, keeping changes isolated until then.
 * Think of this class as C-3PO listing and maintaining all six million forms:
 * each value is a language entry you can read, add, edit, or remove.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MultivaluedDialog extends Dialog
{

    /** The dialog title. */
    private static final String DIALOG_TITLE = Messages.getString( "MultivaluedDialog.MultivaluedEditor" ); //$NON-NLS-1$

    /** The attribute hierarchy to edit. */
    private AttributeHierarchy workingAttributeHierarchy;

    /** The original attribute hierarchy. */
    private AttributeHierarchy referenceAttributeHierarchy;

    /** The entry editor widget configuration. */
    private MultiValuedEntryEditorConfiguration configuration;

    /** The entry edtior widget action group. */
    private EntryEditorWidgetActionGroup actionGroup;

    /** The entry editor widget. */
    private EntryEditorWidget mainWidget;

    /** The universal listener. */
    private MultiValuedEntryEditorUniversalListener universalListener;

    /** Token used to activate and deactivate shortcuts in the editor */
    private IContextActivation contextActivation;


    // ── C-3PO OPENS HIS LANGUAGE DATABASE ────────────────────────────────────
    // C-3PO is handed a reference card for one specific language family: "edit
    // the Huttese dialect entries."  He clones his internal record so he can
    // work on a draft without corrupting the master database — only approved
    // changes get written back.
    // We clone the entry and locate the same attribute hierarchy in the clone so
    // all edits happen on the copy, leaving the original untouched until OK.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new MultivaluedDialog for the given attribute hierarchy.  We
     * immediately clone the owning entry so the editor works on a safe copy —
     * this means the user can cancel without any state being changed, and we can
     * compute a diff on OK to apply only the actual changes.
     *
     * <p>For example — C-3PO opens a draft of his language records:</p>
     * <pre>
     *   IEntry clone = new CompoundModification().cloneEntry(original);
     *   workingHierarchy = clone.getAttributeWithSubtypes(attributeDesc);
     *   // all edits happen to clone — original is safe
     * </pre>
     *
     * @param parentShell        the shell that owns this dialog
     * @param attributeHierarchy the attribute hierarchy (e.g. all "mail" values) to edit
     */
    public MultivaluedDialog( Shell parentShell, AttributeHierarchy attributeHierarchy )
    {
        super( parentShell );
        setShellStyle( getShellStyle() | SWT.RESIZE );
        this.referenceAttributeHierarchy = attributeHierarchy;

        // clone the entry and attribute hierarchy
        IEntry entry = attributeHierarchy.getEntry();
        String attributeDescription = attributeHierarchy.getAttributeDescription();
        IEntry clone = new CompoundModification().cloneEntry( entry );
        this.workingAttributeHierarchy = clone.getAttributeWithSubtypes( attributeDescription );
    }


    // ── C-3PO LABELS HIS EDITING TERMINAL ────────────────────────────────────
    // C-3PO wheels over to his language-editing console and stamps the display
    // with "Multi-Valued Editor — Huttese Dialect."  Visitors know at a glance
    // what they are looking at.
    // We set the dialog window title and the multi-valued editor icon.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Applies the dialog title and icon to the shell before it is shown.
     *
     * <p>For example — C-3PO labels his editing console:</p>
     * <pre>
     *   shell.setText("Multi-Valued Editor");
     *   shell.setIcon(MULTIVALUED_EDITOR_ICON);
     * </pre>
     *
     * @param shell  the shell Eclipse hands us to configure
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( DIALOG_TITLE );
        shell.setImage( BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_MULTIVALUEDEDITOR ) );
    }


    // ── C-3PO COMMITS THE APPROVED CHANGES TO THE MASTER DATABASE ────────────
    // After the Rebel council reviews the draft, C-3PO computes the difference
    // between the draft and the master, removes outdated entries from the master,
    // then adds all the new or corrected entries in a single transaction.
    // We compute an LDIF diff between the working copy and the original, then
    // apply only the changes atomically — suspending event firing during the swap
    // to avoid spurious UI updates mid-operation.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user presses OK.  We compute the LDIF diff between the
     * working copy and the original attribute hierarchy.  If there are changes we
     * suspend the event system, swap out the old attribute values for the new
     * ones, then resume event firing and publish a single
     * {@link ValueMultiModificationEvent} so the UI refreshes once cleanly.
     *
     * <p>For example — C-3PO commits approved changes to the master:</p>
     * <pre>
     *   LdifFile diff = computeDiff(reference, working);
     *   if (diff != null) {
     *       EventRegistry.suspend();
     *       entry.deleteAttribute(oldAttrs);
     *       entry.addAttribute(newAttrs);
     *       EventRegistry.resume();
     *       EventRegistry.fire(new ValueMultiModificationEvent(...));
     *   }
     * </pre>
     */
    @Override
    protected void okPressed()
    {
        LdifFile diff = Utils
            .computeDiff( referenceAttributeHierarchy.getEntry(), workingAttributeHierarchy.getEntry() );
        if ( diff != null )
        {
            EventRegistry.suspendEventFiringInCurrentThread();
            IEntry entry = referenceAttributeHierarchy.getEntry();
            for ( IAttribute attribute : referenceAttributeHierarchy.getAttributes() )
            {
                entry.deleteAttribute( attribute );
            }
            for ( IAttribute attribute : workingAttributeHierarchy.getAttributes() )
            {
                entry.addAttribute( attribute );
            }
            EventRegistry.resumeEventFiringInCurrentThread();

            ValueMultiModificationEvent event = new ValueMultiModificationEvent( entry.getBrowserConnection(), entry );
            EventRegistry.fireEntryUpdated( event, this );
        }

        super.okPressed();
    }


    // ── C-3PO OPENS THE FIRST EMPTY SLOT FOR A NEW ENTRY ─────────────────────
    // When there are no language entries yet, C-3PO creates a blank slot in the
    // list and opens the input form immediately so the user can start typing
    // without having to click "Add" first.
    // If the attribute has no values at all we add an empty placeholder so the
    // tree isn't just blank, and rely on {@link #createDialogArea} to open the
    // value editor on that empty slot.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Opens the dialog.  If the attribute currently has no values, we add an
     * empty value first so the tree view has something to select and we can
     * immediately open the value editor, giving the user a smoother "add first
     * value" experience.
     *
     * <p>For example — C-3PO opens a blank slot when the list is empty:</p>
     * <pre>
     *   if (attribute.getValueSize() == 0) {
     *       attribute.addEmptyValue();  // blank slot ready for typing
     *   }
     *   return super.open();
     * </pre>
     *
     * @return  the dialog return code — {@link Dialog#OK} or {@link Dialog#CANCEL}
     */
    public int open()
    {
        if ( workingAttributeHierarchy.getAttribute().getValueSize() == 0 )
        {
            workingAttributeHierarchy.getAttribute().addEmptyValue();
        }

        return super.open();
    }


    // ── C-3PO SHUTS DOWN HIS EDITING TERMINAL ────────────────────────────────
    // When the editing session ends C-3PO powers down the terminal, clears any
    // dangling blank entries from the draft (you can't file an empty language
    // record), and removes attributes that ended up with no values at all.
    // We dispose the widget stack, deactivate keyboard shortcuts, and clean up
    // empty values and empty attributes from the working copy.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Closes the dialog and performs cleanup.  After the superclass closes the
     * shell we call {@link #dispose()} to tear down the widget stack, then sweep
     * through the working attribute hierarchy to remove any empty values and any
     * attributes that have no remaining values.
     *
     * <p>For example — C-3PO tidies up after the editing session:</p>
     * <pre>
     *   dispose();                       // tear down widgets
     *   for each attribute:
     *       removeEmptyValues(attr);     // no blank entries allowed
     *       if (attr.valueSize() == 0) entry.deleteAttribute(attr);
     * </pre>
     *
     * @return  {@code true} if the dialog closed successfully
     */
    public boolean close()
    {
        boolean returnValue = super.close();
        if ( returnValue )
        {
            dispose();

            // cleanup attribute hierarchy after editing
            for ( Iterator<IAttribute> it = workingAttributeHierarchy.iterator(); it.hasNext(); )
            {
                IAttribute attribute = it.next();
                if ( attribute != null )
                {
                    // remove empty values
                    IValue[] values = attribute.getValues();
                    for ( int i = 0; i < values.length; i++ )
                    {
                        if ( values[i].isEmpty() )
                        {
                            attribute.deleteEmptyValue();
                        }
                    }

                    // delete attribute from entry if all values were deleted
                    if ( attribute.getValueSize() == 0 )
                    {
                        attribute.getEntry().deleteAttribute( attribute );
                    }
                }
            }
        }
        return returnValue;
    }


    // ── C-3PO POWERS DOWN ALL HIS SUBSYSTEMS ─────────────────────────────────
    // C-3PO gracefully shuts down each subsystem in order: listener, display,
    // action handlers, configuration.  Skipping any step would leave a subsystem
    // running in the dark, consuming resources and causing mysterious problems.
    // We null each field after disposal so we can safely check whether disposal
    // already happened (the close / dispose cycle can trigger each other).
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Disposes all widgets and subsystems used by the dialog.  We deactivate
     * the global action handlers, dispose the universal listener, the main
     * widget, the action group, and the configuration — and deactivate the
     * keyboard context so our shortcuts no longer steal key bindings from the
     * rest of the workbench.  Each field is nulled after disposal so a double
     * call is safe.
     *
     * <p>For example — C-3PO powers down his subsystems in order:</p>
     * <pre>
     *   universalListener.dispose();
     *   mainWidget.dispose();
     *   actionGroup.deactivate();
     *   configuration.dispose();
     *   contextService.deactivateContext(contextActivation);
     * </pre>
     */
    public void dispose()
    {
        if ( configuration != null )
        {
            universalListener.dispose();
            universalListener = null;
            mainWidget.dispose();
            mainWidget = null;
            actionGroup.deactivateGlobalActionHandlers();
            actionGroup.dispose();
            actionGroup = null;
            configuration.dispose();
            configuration = null;

            if ( contextActivation != null )
            {
                IContextService contextService = ( IContextService ) PlatformUI.getWorkbench().getAdapter(
                    IContextService.class );
                contextService.deactivateContext( contextActivation );
                contextActivation = null;
            }
        }
    }


    // ── C-3PO UNFOLDS HIS LANGUAGE CATALOGUE ─────────────────────────────────
    // C-3PO unrolls the full catalogue of language entries onto the table — the
    // tree view, toolbar, context menu, keyboard shortcuts — everything needed
    // to read, add, edit, and delete values interactively.  If any blank slots
    // exist he immediately opens the editor on the first one so the user can
    // start typing without an extra click.
    // We wire up the entire EntryEditorWidget stack and pre-select any empty value
    // to trigger the edit mode immediately.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Builds the full dialog content area: the {@link EntryEditorWidget} with its
     * toolbar, menus, and keyboard context, the action group, and the universal
     * listener.  After everything is wired up we scan for empty values and
     * immediately open the default editor on the first one so adding a first
     * value feels instant.
     *
     * <p>For example — C-3PO unfolds his catalogue and opens the first blank:</p>
     * <pre>
     *   mainWidget.createWidget(composite);
     *   actionGroup.fillToolBar(toolbar);
     *   universalListener.setInput(workingHierarchy);
     *   // auto-open editor on first empty value
     *   for each value in hierarchy:
     *       if (value.isEmpty()) { selectIt(); openDefaultEditor(); break; }
     * </pre>
     *
     * @param parent  the parent composite Eclipse provides
     * @return        the completed content area control
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );

        // create configuration
        configuration = new MultiValuedEntryEditorConfiguration();

        // create main widget
        mainWidget = new EntryEditorWidget( configuration );
        mainWidget.createWidget( composite );
        mainWidget.getViewer().getTree().setFocus();

        // create actions
        actionGroup = new EntryEditorWidgetActionGroup( mainWidget, configuration );
        actionGroup.fillToolBar( mainWidget.getToolBarManager() );
        actionGroup.fillMenu( mainWidget.getMenuManager() );
        actionGroup.fillContextMenu( mainWidget.getContextMenuManager() );
        IContextService contextService = ( IContextService ) PlatformUI.getWorkbench().getAdapter(
            IContextService.class );
        contextActivation = contextService.activateContext( BrowserCommonConstants.CONTEXT_DIALOGS );
        actionGroup.activateGlobalActionHandlers();

        // create the listener
        universalListener = new MultiValuedEntryEditorUniversalListener( mainWidget.getViewer(), configuration,
            actionGroup, actionGroup.getOpenDefaultEditorAction() );
        universalListener.setInput( workingAttributeHierarchy );

        // start edit mode if an empty value exists
        for ( Iterator<IAttribute> it = workingAttributeHierarchy.iterator(); it.hasNext(); )
        {
            IAttribute attribute = it.next();
            IValue[] values = attribute.getValues();
            for ( int i = 0; i < values.length; i++ )
            {
                IValue value = values[i];
                if ( value.isEmpty() )
                {
                    mainWidget.getViewer().setSelection( new StructuredSelection( value ), true );
                    if ( actionGroup.getOpenDefaultEditorAction().isEnabled() )
                    {
                        actionGroup.getOpenDefaultEditorAction().run();
                        break;
                    }
                }
            }
        }

        applyDialogFont( composite );
        return composite;
    }

    // ── INNER CLASS: MultiValuedEntryEditorUniversalListener ─────────────────
    /**
     * A specialised listener for the {@link MultivaluedDialog}.  It extends
     * {@link EntryEditorWidgetUniversalListener} and overrides
     * {@link #entryUpdated(EntryModificationEvent)} to keep the tree viewer in
     * sync as values are added, modified, deleted, or emptied — and to move the
     * selection to the most relevant row after each change.
     */
    class MultiValuedEntryEditorUniversalListener extends EntryEditorWidgetUniversalListener
    {

        // ── C-3PO TUNES IN THE LIVE FEED ─────────────────────────────────────
        // C-3PO plugs into the real-time translation feed so he is instantly
        // notified whenever a language entry changes — and can update the display
        // without anyone having to poke him.
        // We wire the viewer, configuration, action group, and start-edit action
        // so the listener knows everything it needs to react to events.
        // ────────────────────────────────────────────────────────────────────────
        /**
         * Creates a new listener for the multi-valued editor.  This just passes
         * all arguments through to the superclass; the interesting work happens
         * in {@link #entryUpdated(EntryModificationEvent)}.
         *
         * <p>For example — C-3PO tunes the live translation feed:</p>
         * <pre>
         *   super(viewer, config, actionGroup, startEditAction);
         * </pre>
         *
         * @param treeViewer      the tree viewer to keep in sync
         * @param configuration   the editor widget configuration
         * @param actionGroup     the action group for toolbar/context menu actions
         * @param startEditAction the action that opens the default value editor
         */
        public MultiValuedEntryEditorUniversalListener( TreeViewer treeViewer,
            EntryEditorWidgetConfiguration configuration, EntryEditorWidgetActionGroup actionGroup,
            OpenDefaultEditorAction startEditAction )
        {
            super( treeViewer, configuration, actionGroup, startEditAction );
        }


        // ── C-3PO REACTS TO A LIVE CATALOGUE UPDATE ──────────────────────────
        // A message arrives: "The Huttese vocabulary just changed — update the
        // display!"  C-3PO checks what kind of change it was and moves his finger
        // to the right row in the catalogue: added entry → jump to it, modified
        // entry → track the new version, deleted entry → move to the first
        // surviving sibling.
        // We refresh the viewer and then set the selection to whichever value is
        // most relevant given the type of event received.
        // ────────────────────────────────────────────────────────────────────────
        /**
         * Reacts to entry modification events by refreshing the tree viewer and
         * adjusting the selection.  Each event type gets specific handling: an
         * added value is selected immediately; a modified value's new version is
         * tracked; after a deletion we select the first remaining sibling; an
         * empty-value addition opens the editor straight away.
         *
         * <p>For example — C-3PO updates the catalogue and points to the change:</p>
         * <pre>
         *   viewer.refresh();
         *   if (event instanceof ValueAddedEvent) viewer.setSelection(added);
         *   if (event instanceof ValueModifiedEvent) viewer.setSelection(newVal);
         *   if (event instanceof ValueDeletedEvent) viewer.setSelection(sibling[0]);
         * </pre>
         *
         * @param event  the modification event fired by the {@link EventRegistry}
         */
        public void entryUpdated( EntryModificationEvent event )
        {
            if ( viewer == null || viewer.getTree() == null || viewer.getTree().isDisposed() )
            {
                return;
            }

            if ( viewer.isCellEditorActive() )
            {
                viewer.cancelEditing();
            }

            viewer.refresh();

            // select added/modified value
            if ( event instanceof ValueAddedEvent )
            {
                ValueAddedEvent vaEvent = ( ValueAddedEvent ) event;
                viewer.setSelection( new StructuredSelection( vaEvent.getAddedValue() ), true );
                viewer.refresh();
            }
            else if ( event instanceof ValueModifiedEvent )
            {
                ValueModifiedEvent vmEvent = ( ValueModifiedEvent ) event;
                viewer.setSelection( new StructuredSelection( vmEvent.getNewValue() ), true );
            }
            else if ( event instanceof ValueDeletedEvent )
            {
                ValueDeletedEvent vdEvent = ( ValueDeletedEvent ) event;
                if ( vdEvent.getDeletedValue().getAttribute().getValueSize() > 0 )
                {
                    viewer.setSelection( new StructuredSelection(
                        vdEvent.getDeletedValue().getAttribute().getValues()[0] ), true );
                }
            }
            else if ( event instanceof EmptyValueAddedEvent )
            {
                viewer.refresh();
                EmptyValueAddedEvent evaEvent = ( EmptyValueAddedEvent ) event;
                viewer.setSelection( new StructuredSelection( evaEvent.getAddedValue() ), true );
                if ( startEditAction.isEnabled() )
                    startEditAction.run();
            }
            else if ( event instanceof EmptyValueDeletedEvent )
            {
                EmptyValueDeletedEvent evdEvent = ( EmptyValueDeletedEvent ) event;
                if ( viewer.getSelection().isEmpty() && evdEvent.getDeletedValue().getAttribute().getValueSize() > 0 )
                    viewer.setSelection( new StructuredSelection(
                        evdEvent.getDeletedValue().getAttribute().getValues()[0] ), true );
            }
            else if ( event instanceof AttributeDeletedEvent )
            {
            }
        }
    }

    // ── INNER CLASS: MultiValuedEntryEditorConfiguration ─────────────────────
    /**
     * A specialised {@link EntryEditorWidgetConfiguration} for the
     * {@link MultivaluedDialog}.  It overrides
     * {@link #getValueEditorManager(TreeViewer)} to create a
     * {@link ValueEditorManager} that does not open in-line or dialog editors
     * automatically — the multi-valued dialog manages that itself.
     */
    class MultiValuedEntryEditorConfiguration extends EntryEditorWidgetConfiguration
    {
        // ── C-3PO INSTALLS HIS SPECIALISED TRANSLATION MODULE ────────────────
        // For this particular catalogue session C-3PO uses a stripped-down
        // translation module: no automatic pop-ups, no auto-complete storms —
        // just the essentials needed to display and edit values on demand.
        // We return a ValueEditorManager with both auto-open flags set to false.
        // ────────────────────────────────────────────────────────────────────────
        /**
         * Returns the {@link ValueEditorManager} for this dialog's tree viewer,
         * creating it lazily on first call.  We pass {@code false} for both the
         * "open in place" and "open dialog automatically" flags so the
         * multi-valued dialog's own selection / event handling has full control
         * over when editors open.
         *
         * <p>For example — C-3PO installs the minimal translation module:</p>
         * <pre>
         *   valueEditorManager = new ValueEditorManager(tree, false, false);
         *   // no automatic pop-ups — the dialog handles that itself
         * </pre>
         *
         * @param viewer  the tree viewer that will use the value editor manager
         * @return        the (shared, lazily-created) {@link ValueEditorManager}
         */
        @Override
        public ValueEditorManager getValueEditorManager( TreeViewer viewer )
        {
            if ( valueEditorManager == null )
            {
                valueEditorManager = new ValueEditorManager( viewer.getTree(), false, false );
            }

            return valueEditorManager;
        }
    }
}
