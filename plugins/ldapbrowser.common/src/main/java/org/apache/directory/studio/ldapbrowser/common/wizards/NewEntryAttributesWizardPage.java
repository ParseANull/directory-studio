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

package org.apache.directory.studio.ldapbrowser.common.wizards;


import java.util.Collection;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidget;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidgetActionGroup;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidgetActionGroupWithAttribute;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidgetConfiguration;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidgetUniversalListener;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.OpenDefaultEditorAction;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Attribute;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DummyEntry;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;


// ── CLASS: NewEntryAttributesWizardPage — LUKE FILLS IN HIS JEDI KNIGHT PROFILE
// After Luke's lightsaber is built and his position in the Force is established,
// he sits down with a data slate to fill in every field of his Jedi Knight
// profile: first name, rank, midi-chlorian count — all the "must" fields have
// to be filled or the Alliance records office will send it back with a warning.
// This wizard page embeds a full entry-editor widget so the user can type values
// for every required and optional attribute of the new entry.  When you arrive
// on this page, the mandatory attributes are automatically added with empty
// placeholders, and the editor pops open on the first empty field right away.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The final page of {@link NewEntryWizard} and {@link EditEntryWizard} — lets
 * the user fill in the attribute values for the prototype entry.
 * When the page becomes visible, any "must" attributes required by the selected
 * object classes are automatically added with empty placeholder values, and the
 * in-line editor opens on the first empty one.
 * A warning is shown (not a blocker) if any required attributes are still empty
 * when the user tries to finish.
 * Think of this page as Luke filling in every field of his Jedi Knight profile
 * data slate — required fields show up automatically, and he can't hand it in
 * until each one has something in it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewEntryAttributesWizardPage extends WizardPage implements EntryUpdateListener
{

    /** The wizard. */
    private NewEntryWizard wizard;

    /** The configuration. */
    private EntryEditorWidgetConfiguration configuration;

    /** The action group. */
    private EntryEditorWidgetActionGroup actionGroup;

    /** The main widget. */
    private EntryEditorWidget mainWidget;

    /** The universal listener. */
    private EntryEditorWidgetUniversalListener universalListener;

    /** Token used to activate and deactivate shortcuts in the editor */
    private IContextActivation contextActivation;


    // ── Luke Opens the Jedi Profile Form for the First Time ───────────────────
    // Luke takes the data slate from the Alliance records officer, finds his
    // name pre-filled from the previous pages, and the editor immediately
    // jumps to the first blank required field so he knows exactly where to start.
    // We wire a page-changed listener on the wizard dialog to do exactly that:
    // when this page becomes the active one, find the first empty value and open
    // its inline editor automatically.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code NewEntryAttributesWizardPage} and wires a
     * {@link IPageChangedListener} on the wizard dialog.
     * When this page becomes visible via the dialog's page-change mechanism,
     * the listener locates the first empty attribute value in the prototype entry
     * and automatically opens the default cell editor on it — so the user lands
     * immediately on something they need to fill in.
     *
     * <p>For example — Luke receives the data slate, cursor already on the first blank field:</p>
     * <pre>
     *   for ( IAttribute attr : prototypeEntry.getAttributes() ) {
     *     for ( IValue val : attr.getValues() ) {
     *       if ( val.isEmpty() ) { openDefaultEditorAction.run(); return; }
     *     }
     *   }
     * </pre>
     *
     * @param pageName  Internal wizard page identifier.
     * @param wizard    The parent {@link NewEntryWizard} coordinating all pages.
     */
    public NewEntryAttributesWizardPage( String pageName, NewEntryWizard wizard )
    {
        super( pageName );
        setTitle( Messages.getString( "NewEntryAttributesWizardPage.Attributes" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "NewEntryAttributesWizardPage.PleaseEnterAttributesForEntry" ) ); //$NON-NLS-1$
        setImageDescriptor( BrowserCommonActivator.getDefault().getImageDescriptor(
            BrowserCommonConstants.IMG_ENTRY_WIZARD ) );
        setPageComplete( false );

        this.wizard = wizard;

        IWizardContainer container = wizard.getContainer();
        if ( container instanceof WizardDialog )
        {
            WizardDialog dialog = ( WizardDialog ) container;
            dialog.addPageChangedListener( new IPageChangedListener()
            {
                public void pageChanged( PageChangedEvent event )
                {
                    if ( getControl().isVisible() )
                    {
                        for ( IAttribute attribute : NewEntryAttributesWizardPage.this.wizard.getPrototypeEntry()
                            .getAttributes() )
                        {
                            for ( IValue value : attribute.getValues() )
                            {
                                if ( value.isEmpty() )
                                {
                                    mainWidget.getViewer().setSelection( new StructuredSelection( value ), true );
                                    OpenDefaultEditorAction openDefaultEditorAction = actionGroup
                                        .getOpenDefaultEditorAction();
                                    if ( openDefaultEditorAction.isEnabled() )
                                    {
                                        openDefaultEditorAction.run();
                                    }
                                    return;
                                }
                            }
                        }
                    }
                }
            } );
        }
    }


    // ── Luke Hands the Slate Back — Session Is Over ────────────────────────────
    // When the wizard is closed or navigated away for good, Luke returns the
    // data slate and all the borrowed equipment is returned to storage.
    // We unregister the entry-update listener, dispose the widget and listener,
    // and deactivate the keyboard shortcut context so nothing leaks.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Releases all resources held by this page: the entry-editor widget,
     * action group, configuration, universal listener, and the Eclipse keyboard
     * context activation.
     * Safe to call multiple times — checks for null before each disposal.
     *
     * <p>For example — Luke returns the data slate and pens to the records office:</p>
     * <pre>
     *   EventRegistry.removeEntryUpdateListener( this );
     *   universalListener.dispose();
     *   mainWidget.dispose();
     *   actionGroup.dispose();
     *   configuration.dispose();
     *   contextService.deactivateContext( contextActivation );
     * </pre>
     */
    public void dispose()
    {
        if ( configuration != null )
        {
            EventRegistry.removeEntryUpdateListener( this );
            universalListener.dispose();
            universalListener = null;
            mainWidget.dispose();
            mainWidget = null;
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
        super.dispose();
    }


    // ── Luke Sees the Form — Required Fields Appear on the Slate ─────────────
    // When Luke flips to this page, the records officer adds blank lines for
    // all the "must" attributes his profile now requires (based on the object
    // classes he picked two pages back).  If he navigated back and changed
    // object classes, stale empty required fields are cleaned out first.
    // When the page is hidden, the editor is cleared so it doesn't hold stale data.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when this page is shown or hidden.
     * On becoming visible: removes stale empty "must" attribute values left over
     * from previous visits, adds fresh empty placeholders for all currently
     * required "must" attributes, loads the prototype into the viewer, re-validates,
     * and sets focus to the viewer.
     * On being hidden: clears the viewer and marks the page incomplete.
     *
     * <p>For example — Luke flips to the attributes form; required fields auto-appear:</p>
     * <pre>
     *   // 1. Remove empty must-attr placeholders from previous visits
     *   // 2. Add fresh empty placeholders for current must attrs
     *   // 3. Load prototype into viewer → user fills values
     * </pre>
     *
     * @param visible  {@code true} when this page is being shown, {@code false} when hidden.
     */
    public void setVisible( boolean visible )
    {
        super.setVisible( visible );

        if ( visible )
        {
            DummyEntry newEntry = wizard.getPrototypeEntry();
            try
            {
                EventRegistry.suspendEventFiringInCurrentThread();

                // remove empty must attributes
                // necessary when navigating back, modifying object classes
                // and Dn and navigating forward again.
                Collection<AttributeType> oldMusts = SchemaUtils.getMustAttributeTypeDescriptions( newEntry );
                for ( AttributeType oldMust : oldMusts )
                {
                    IAttribute attribute = newEntry.getAttribute( oldMust.getOid() );
                    if ( attribute != null )
                    {
                        IValue[] values = attribute.getValues();
                        for ( int v = 0; v < values.length; v++ )
                        {
                            if ( values[v].isEmpty() )
                            {
                                attribute.deleteValue( values[v] );
                            }
                        }
                        if ( attribute.getValueSize() == 0 )
                        {
                            newEntry.deleteAttribute( attribute );
                        }
                    }
                }

                // add must attributes
                Collection<AttributeType> newMusts = SchemaUtils.getMustAttributeTypeDescriptions( newEntry );
                for ( AttributeType newMust : newMusts )
                {
                    if ( newEntry.getAttributeWithSubtypes( newMust.getOid() ) == null )
                    {
                        String friendlyIdentifier = SchemaUtils.getFriendlyIdentifier( newMust );
                        IAttribute att = new Attribute( newEntry, friendlyIdentifier );
                        newEntry.addAttribute( att );
                        att.addEmptyValue();
                    }
                }
            }
            finally
            {
                EventRegistry.resumeEventFiringInCurrentThread();
            }

            // set the input
            universalListener.setInput( newEntry );
            mainWidget.getViewer().refresh();
            validate();

            // set focus to the viewer
            mainWidget.getViewer().getControl().setFocus();
        }
        else
        {
            mainWidget.getViewer().setInput( "" ); //$NON-NLS-1$
            mainWidget.getViewer().refresh();
            setPageComplete( false );
        }
    }


    // ── Checking Whether Luke's Profile Is Complete ────────────────────────────
    // The Alliance records officer runs a completeness check on the slate: are
    // all required fields filled?  If something is missing, a warning badge
    // appears — Luke is told what's still blank but can still click Finish.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the prototype entry satisfies all schema requirements.
     * If any "must" attributes are missing values, a WARNING message is shown
     * on the page (not an error — the user can still finish if they want).
     * The page is always marked complete once the prototype is non-null.
     *
     * <p>For example — the records officer reads the completeness report on Luke's slate:</p>
     * <pre>
     *   Collection&lt;String&gt; msgs = SchemaUtils.getEntryIncompleteMessages( prototype );
     *   if ( !msgs.isEmpty() ) { setMessage( joinedMessages, WARNING ); }
     *   setPageComplete( true );
     * </pre>
     */
    private void validate()
    {
        if ( wizard.getPrototypeEntry() != null )
        {
            Collection<String> messages = SchemaUtils.getEntryIncompleteMessages( wizard.getPrototypeEntry() );
            if ( messages != null && !messages.isEmpty() )
            {
                StringBuffer sb = new StringBuffer();
                for ( String message : messages )
                {
                    sb.append( message );
                    sb.append( ' ' );
                }
                setMessage( sb.toString(), WizardPage.WARNING );
            }
            else
            {
                setMessage( null );
            }

            setPageComplete( true );
        }
        else
        {
            setPageComplete( false );
        }
    }


    // ── The Data Slate Form Is Constructed ────────────────────────────────────
    // The Alliance records officer assembles the form: the main entry-editor
    // widget (the table of attribute rows), a toolbar with add/delete/edit
    // buttons, and the context menu — then wires the entry-update listener so
    // every change to the prototype triggers a re-validation automatically.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds all the SWT controls for this page — creates the
     * {@link EntryEditorWidget} with its toolbar, menus, and context menu;
     * activates the dialog keyboard shortcuts; and registers an
     * {@link EntryUpdateListener} so any change to the prototype triggers
     * {@link #validate()}.
     *
     * <p>For example — the data slate form is assembled with all its editing tools:</p>
     * <pre>
     *   configuration = new EntryEditorWidgetConfiguration();
     *   mainWidget    = new EntryEditorWidget( configuration );
     *   actionGroup   = new EntryEditorWidgetActionGroupWithAttribute( ... );
     *   universalListener = new EntryEditorWidgetUniversalListener( ... );
     *   EventRegistry.addEntryUpdateListener( this, ... );
     * </pre>
     *
     * @param parent  The parent composite supplied by the wizard dialog.
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout gl = new GridLayout( 1, false );
        composite.setLayout( gl );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        // create configuration
        configuration = new EntryEditorWidgetConfiguration();

        // create main widget
        mainWidget = new EntryEditorWidget( configuration );
        mainWidget.createWidget( composite );
        mainWidget.getViewer().getTree().setFocus();

        // create actions
        actionGroup = new EntryEditorWidgetActionGroupWithAttribute( mainWidget, configuration );
        actionGroup.fillToolBar( mainWidget.getToolBarManager() );
        actionGroup.fillMenu( mainWidget.getMenuManager() );
        actionGroup.fillContextMenu( mainWidget.getContextMenuManager() );
        IContextService contextService = ( IContextService ) PlatformUI.getWorkbench().getAdapter(
            IContextService.class );
        contextActivation = contextService.activateContext( BrowserCommonConstants.CONTEXT_DIALOGS );
        actionGroup.activateGlobalActionHandlers();

        // create the listener
        universalListener = new EntryEditorWidgetUniversalListener( mainWidget.getViewer(), configuration, actionGroup,
            actionGroup.getOpenDefaultEditorAction() );
        EventRegistry.addEntryUpdateListener( this, BrowserCommonActivator.getDefault().getEventRunner() );

        setControl( composite );
    }


    // ── A Field Changes — Alliance Office Re-Checks the Slate ─────────────────
    // Luke writes something into a field on the data slate — the Alliance records
    // system automatically re-runs its completeness check and updates the warning
    // banner if the entry is now complete (or newly incomplete).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the {@link EventRegistry} whenever the prototype entry is modified.
     * Re-validates the page only if the event targets our prototype and the page
     * control is currently visible and not yet disposed.
     *
     * <p>For example — Luke fills in a field; the records system re-checks for completeness:</p>
     * <pre>
     *   if ( event.getModifiedEntry() == wizard.getPrototypeEntry()
     *        &amp;&amp; !isDisposed() &amp;&amp; getControl().isVisible() ) {
     *     validate();
     *   }
     * </pre>
     *
     * @param event  The modification event; we check whether it targets our prototype.
     */
    public void entryUpdated( EntryModificationEvent event )
    {
        if ( event.getModifiedEntry() == wizard.getPrototypeEntry() && !isDisposed() && getControl().isVisible() )
        {
            validate();
        }
    }


    // ── Has Luke Already Handed Back the Slate? ────────────────────────────────
    // A quick sanity check — if the data slate has already been returned
    // (the configuration is null, meaning dispose() was called), we treat the
    // page as disposed and skip any re-validation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this page has been disposed (i.e. {@link #dispose()}
     * was already called).
     * Used internally to guard against stale event callbacks after the wizard closes.
     *
     * <p>For example — has Luke already returned the data slate to the records office?</p>
     * <pre>
     *   if ( isDisposed() ) return; // nothing to validate, form is gone
     * </pre>
     *
     * @return  {@code true} if the widget configuration is {@code null} (disposed).
     */
    private boolean isDisposed()
    {
        return configuration == null;
    }

}
