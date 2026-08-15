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

package org.apache.directory.studio.schemaeditor.view.editors.objectclass;


import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandlerAdapter;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandlerListener;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;


// ── CLASS: ObjectClassEditor — CONSTRUCTION OF THE SECOND DEATH STAR ─────────
// The Emperor's engineers assemble the second Death Star panel by panel —
// the superlaser dish, the equatorial trench, the reactor core — each
// constructed separately but bolted together into one fearsome whole.
// This class is that construction effort: it assembles the Overview page and
// the Source Code page into a single multi-tab Eclipse FormEditor, keeps
// both pages in sync, and orchestrates saving changes back to the schema.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The main Eclipse multi-page {@link FormEditor} for editing LDAP {@link ObjectClass} definitions.
 * It owns two pages — {@link ObjectClassEditorOverviewPage} (form view) and
 * {@link ObjectClassEditorSourceCodePage} (raw source view) — and keeps them synchronized
 * through a shared "modified object class" clone that accumulates edits until the user saves.
 * Think of it as the Death Star construction project: we build the pieces separately,
 * then wire them together into one operational station.
 */
public class ObjectClassEditor extends FormEditor
{
    /** The ID of the Editor */
    public static final String ID = PluginConstants.EDITOR_OBJECT_CLASS_ID;

    /** The editor */
    private ObjectClassEditor instance;

    // The pages
    private ObjectClassEditorOverviewPage overviewPage;
    private ObjectClassEditorSourceCodePage sourceCodePage;

    /** The dirty state flag */
    private boolean dirty = false;

    /** The original object class */
    private ObjectClass originalObjectClass;

    /** The object class used to save modifications */
    private ObjectClass modifiedObjectClass;

    /** The originalSchema */
    private Schema originalSchema;

    /** The listener for page changed */
    private IPageChangedListener pageChangedListener = new IPageChangedListener()
    {
        public void pageChanged( PageChangedEvent event )
        {
            Object selectedPage = event.getSelectedPage();

            if ( selectedPage instanceof ObjectClassEditorOverviewPage )
            {
                if ( !sourceCodePage.canLeaveThePage() )
                {
                    notifyError( Messages.getString( "ObjectClassEditor.CodeError" ) ); //$NON-NLS-1$
                    return;
                }

                overviewPage.refreshUI();
            }
            else if ( selectedPage instanceof ObjectClassEditorSourceCodePage )
            {
                if ( sourceCodePage.canLeaveThePage() )
                {
                    sourceCodePage.refreshUI();
                }
            }
        }
    };

    /** The SchemaHandler listener */
    private SchemaHandlerListener schemaHandlerListener = new SchemaHandlerAdapter()
    {
        public void objectClassModified( ObjectClass oc )
        {
            if ( oc.equals( originalObjectClass ) )
            {
                // Updating the modified object class
                modifiedObjectClass = PluginUtils.getClone( originalObjectClass );

                // Refreshing the editor pages
                overviewPage.refreshUI();
                sourceCodePage.refreshUI();

                // Refreshing the part name (in case of a change in the name)
                setPartName( getEditorInput().getName() );
            }
        }


        public void objectClassRemoved( ObjectClass oc )
        {
            if ( oc.equals( originalObjectClass ) )
            {
                getEditorSite().getPage().closeEditor( instance, false );
            }
        }


        public void schemaRemoved( Schema schema )
        {
            if ( schema.equals( originalSchema ) )
            {
                getEditorSite().getPage().closeEditor( instance, false );
            }
        }


        public void schemaRenamed( Schema schema )
        {
            if ( schema.equals( originalSchema ) )
            {
                // Updating the modified object class
                modifiedObjectClass = PluginUtils.getClone( originalObjectClass );

                // Refreshing the editor pages
                overviewPage.refreshUI();
                sourceCodePage.refreshUI();
            }
        }
    };


    // ── The Emperor Commissions the Death Star ────────────────────────────────
    // The Emperor formally commissions the Death Star project: the construction
    // site is designated, a clone of the original blueprints is created for the
    // engineers to modify, and listeners are attached so the project stays aware
    // of any changes in the broader Imperial fleet.
    // This method initializes the editor from the input object class, clones it
    // for editing, and hooks into the schema handler to track external changes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Initializes this editor from an {@link ObjectClassEditorInput}.
     * We clone the original object class into {@code modifiedObjectClass} so edits
     * never touch the live schema until the user explicitly saves.
     * We also register a {@link SchemaHandlerListener} so the editor reacts if
     * the underlying schema is modified or removed externally.
     *
     * @param site   the Eclipse editor site — gives us access to the workbench
     * @param input  must be an {@link ObjectClassEditorInput} carrying the target object class
     * @throws PartInitException  if the input is of the wrong type or initialization fails
     */
    public void init( IEditorSite site, IEditorInput input ) throws PartInitException
    {
        super.init( site, input );

        instance = this;

        setSite( site );
        setInput( input );
        setPartName( input.getName() );

        originalObjectClass = ( ObjectClass ) ( ( ObjectClassEditorInput ) getEditorInput() ).getObjectClass();
        modifiedObjectClass = PluginUtils.getClone( originalObjectClass );

        SchemaHandler schemaHandler = Activator.getDefault().getSchemaHandler();
        originalSchema = schemaHandler.getSchema( originalObjectClass.getSchemaName() );
        schemaHandler.addListener( schemaHandlerListener );

        addPageChangedListener( pageChangedListener );
    }


    // ── The Death Star Project Is Decommissioned ──────────────────────────────
    // When the Rebel Alliance destroys the Death Star, the Imperial command
    // immediately pulls back all associated resource allocations and terminates
    // the monitoring networks that were keeping tabs on the project's status.
    // We do the same here: remove our schema handler listener so we stop
    // receiving events after the editor closes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Cleans up when this editor is closed.
     * We remove our {@link SchemaHandlerListener} to avoid memory leaks and
     * stale event callbacks after the editor tab is gone.
     */
    public void dispose()
    {
        SchemaHandler schemaHandler = Activator.getDefault().getSchemaHandler();
        schemaHandler.removeListener( schemaHandlerListener );

        super.dispose();
    }


    // ── Engineers Add Each Station to the Death Star ──────────────────────────
    // One by one, the construction crews bolt the superlaser platform, the
    // equatorial trench section, and the reactor core into the Death Star frame.
    // We do the same: create the Overview page and the Source Code page, then
    // hand them off to Eclipse's FormEditor framework to display as tabs.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Creates and registers the two editor pages.
     * The Overview page provides a form-based GUI; the Source Code page shows
     * raw OpenLDAP schema syntax. Eclipse calls this once after {@link #init}.
     */
    protected void addPages()
    {
        try
        {
            overviewPage = new ObjectClassEditorOverviewPage( this );
            addPage( overviewPage );
            sourceCodePage = new ObjectClassEditorSourceCodePage( this );
            addPage( sourceCodePage );
        }
        catch ( PartInitException e )
        {
            PluginUtils.logError( "error when adding pages", e ); //$NON-NLS-1$
        }
    }


    // ── The Emperor Inspects and Approves the Final Plans ─────────────────────
    // Before committing to a galactic strike, the Emperor personally reviews
    // the Death Star's readiness — if the reactor core design is flawed, he
    // halts the inspection and demands corrections before proceeding.
    // Here, we validate that the source code page parses cleanly, then commit
    // the modified object class back to the live schema via the schema handler.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Saves the modified object class back to the schema.
     * If the Source Code page contains a parse error, we refuse to save and
     * cancel the monitor so Eclipse knows the save was aborted.
     * On success we push the modified clone to the schema handler and clear
     * the dirty flag.
     *
     * @param monitor  the progress monitor Eclipse supplies — we cancel it on parse error
     */
    public void doSave( IProgressMonitor monitor )
    {
        // Verifying if there is an error on the source code page
        if ( !sourceCodePage.canLeaveThePage() )
        {
            notifyError( Messages.getString( "ObjectClassEditor.CodeErrorObject" ) ); //$NON-NLS-1$
            monitor.setCanceled( true );
            return;
        }

        Activator.getDefault().getSchemaHandler().modifyObjectClass( originalObjectClass, modifiedObjectClass );

        setPartName( getEditorInput().getName() );
        if ( !monitor.isCanceled() )
        {
            setDirty( false );
        }
    }


    // ── The Emperor Declines a "Save As" Maneuver ─────────────────────────────
    // The Emperor does not negotiate alternate destinations — there is only
    // one Death Star, and saving it "as" something else is not on the agenda.
    // Object class editors don't support "Save As" — it's a no-op by design.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * "Save As" is not supported for object class editors.
     * We always save back to the same schema entry, so this method intentionally
     * does nothing.
     */
    public void doSaveAs()
    {
    }


    // ── The Emperor Forbids Alternative Blueprints ────────────────────────────
    // There are no alternative blueprints for the Death Star — the Emperor
    // makes it clear that "Save As" is off the table entirely.
    // We tell Eclipse the same thing by returning false here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Reports whether "Save As" is allowed — it is not.
     * Returning {@code false} hides the "Save As" toolbar button for this editor.
     *
     * @return  always {@code false}
     */
    public boolean isSaveAsAllowed()
    {
        return false;
    }


    // ── Mission Control Checks the Alert Status ───────────────────────────────
    // Imperial mission control checks whether the Death Star project has
    // any unsaved changes — a red alert that the plans still need authorization.
    // This method answers Eclipse's question: "does the editor have unsaved work?"
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns whether the editor has unsaved changes.
     * Eclipse uses this to show the asterisk in the tab title and to prompt
     * for saving when the editor is closed.
     *
     * @return  {@code true} if there are unsaved changes, {@code false} otherwise
     */
    public boolean isDirty()
    {
        return dirty;
    }


    // ── The Emperor Raises or Lowers the Alert Level ──────────────────────────
    // The Emperor personally controls the Imperial alert level — he sets
    // "red alert" when plans are in flux and cancels it once things are settled.
    // We call this after every user change (set to true) and after a successful
    // save (set to false), and we notify Eclipse so the tab updates immediately.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dirty state of the editor and notifies Eclipse to update the UI.
     * Call with {@code true} when the user makes a change; call with {@code false}
     * after a successful save. Eclipse uses the dirty state for the asterisk
     * in the tab title and for the "Save before closing?" prompt.
     *
     * @param dirty  {@code true} to mark the editor as having unsaved changes
     */
    public void setDirty( boolean dirty )
    {
        this.dirty = dirty;
        editorDirtyStateChanged();
    }


    // ── Engineers Retrieve the Original Death Star Blueprint ──────────────────
    // The original Death Star blueprints are kept in a secure vault — untouched,
    // the reference copy that everything else is measured against.
    // Pages call this when they need to compare the user's edits to the baseline
    // or when they need to tell the schema handler which object to replace.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the original, unmodified {@link ObjectClass} this editor was opened on.
     * We keep this around as the "before" snapshot — the schema handler needs it
     * to identify which entry to replace when we commit the user's changes.
     *
     * @return  the original {@link ObjectClass} as it existed when the editor was opened
     */
    public ObjectClass getOriginalObjectClass()
    {
        return originalObjectClass;
    }


    // ── Engineers Retrieve the Working Copy ───────────────────────────────────
    // Alongside the vault copy, the construction crews work from a modifiable
    // set of blueprints — every change they pencil in goes here, not into
    // the original. Pages read from and write to this working copy.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the working (modified) copy of the {@link ObjectClass} being edited.
     * All user changes accumulate here until the editor saves, at which point
     * this object replaces the original in the live schema.
     *
     * @return  the modified {@link ObjectClass} holding the current in-progress edits
     */
    public ObjectClass getModifiedObjectClass()
    {
        return modifiedObjectClass;
    }


    // ── Engineers Replace the Working Blueprints ──────────────────────────────
    // When the Source Code page parses a newly typed schema definition, the
    // resulting object replaces the old working blueprint entirely — the
    // construction crew throws out the old draft and starts from the new one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the entire working copy of the {@link ObjectClass} being edited.
     * The Source Code page calls this after parsing a user-typed schema definition;
     * the new parsed object becomes the authoritative working copy going forward.
     *
     * @param modifiedObjectClass  the new working copy to adopt; must not be {@code null}
     */
    public void setModifiedObjectClass( ObjectClass modifiedObjectClass )
    {
        this.modifiedObjectClass = modifiedObjectClass;
    }


    // ── Imperial Command Issues an Error Bulletin ─────────────────────────────
    // When something goes wrong on the Death Star construction project, the
    // Emperor doesn't stay silent — he broadcasts a clear, stark error message
    // across all Imperial channels so everyone knows what happened.
    // We pop an Eclipse error dialog for the same reason: clear user feedback.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens an Eclipse error dialog to tell the user something went wrong.
     * We use this when the Source Code page contains invalid schema syntax and
     * we need to block a page switch or save until it's fixed.
     *
     * @param message  the error text to display in the dialog body
     */
    private void notifyError( String message )
    {
        MessageDialog.openError( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
            Messages.getString( "ObjectClassEditor.Error" ), message ); //$NON-NLS-1$
    }
}
