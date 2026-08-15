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

package org.apache.directory.studio.schemaeditor.view.editors.attributetype;


import org.apache.directory.api.ldap.model.schema.AttributeType;
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


// ── CLASS: AttributeTypeEditor — TANTIVE IV BRIDGE UNDER FIRE ───────────────────────
// The Tantive IV bridge is the command centre of the whole operation.  Captain Antilles
// coordinates three stations — helm (Overview), comms (Source Code), and weapons
// (Used By) — each doing their own job but all receiving orders from the bridge and
// reporting status back up.  When an alert fires (a schema change from outside the ship),
// the bridge broadcasts it to all stations.  When the mission ends (the attribute type
// is removed), the bridge shuts down all stations and closes the editor.
// This class is that bridge.  It's the Eclipse FormEditor that hosts the three tab pages,
// owns the original/modified attribute type pair, coordinates page-switch validation,
// listens for external schema events, and handles the final Save operation.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse multi-page FormEditor for editing a single LDAP attribute type.
 * It hosts three tabs: an Overview page (the main editable form), a Source Code page
 * (raw OpenLDAP schema syntax), and a Used By page (which object classes reference this
 * attribute type).  The editor maintains two copies of the attribute type — the original
 * (as last saved) and a mutable working copy — so the user's edits can be validated and
 * committed as a single atomic operation via the SchemaHandler.
 * Think of this as the Tantive IV bridge: three crew stations, one captain, one set of
 * orders, one shared mission state.
 */
public class AttributeTypeEditor extends FormEditor
{
    /** The ID of the Editor */
    public static final String ID = PluginConstants.EDITOR_ATTRIBUTE_TYPE_ID;

    /** The editor */
    private AttributeTypeEditor instance;

    /** The dirty state flag */
    private boolean dirty = false;

    // The pages
    private AttributeTypeEditorOverviewPage overviewPage;
    private AttributeTypeEditorSourceCodePage sourceCodePage;
    private AttributeTypeEditorUsedByPage usedByPage;

    /** The original attribute type */
    private AttributeType originalAttributeType;

    /** The attribute type used to save modifications */
    private AttributeType modifiedAttributeType;

    /** The originalSchema */
    private Schema originalSchema;

    /** The listener for page changed */
    private IPageChangedListener pageChangedListener = new IPageChangedListener()
    {
        public void pageChanged( PageChangedEvent event )
        {
            Object selectedPage = event.getSelectedPage();

            if ( selectedPage instanceof AttributeTypeEditorOverviewPage )
            {
                if ( !sourceCodePage.canLeaveThePage() )
                {
                    notifyError( Messages.getString( "AttributeTypeEditor.CodeErrors" ) ); //$NON-NLS-1$
                    return;
                }

                overviewPage.refreshUI();
            }
            else if ( selectedPage instanceof AttributeTypeEditorSourceCodePage )
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
        public void attributeTypeModified( AttributeType at )
        {
            if ( at.equals( originalAttributeType ) )
            {
                // Updating the modified attribute type
                modifiedAttributeType = PluginUtils.getClone( originalAttributeType );

                // Refreshing the editor pages
                overviewPage.refreshUI();
                sourceCodePage.refreshUI();
                usedByPage.refreshUI();

                // Refreshing the part name (in case of a change in the name)
                setPartName( getEditorInput().getName() );
            }
        }


        public void attributeTypeRemoved( AttributeType at )
        {
            if ( at.equals( originalAttributeType ) )
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
                // Updating the modified attribute type
                modifiedAttributeType = PluginUtils.getClone( originalAttributeType );

                // Refreshing the editor pages
                overviewPage.refreshUI();
                sourceCodePage.refreshUI();
                usedByPage.refreshUI();
            }
        }
    };


    // ── Antilles Takes Command of the Bridge ─────────────────────────────────────────
    // Captain Antilles steps onto the bridge, identifies which ship he's captaining,
    // gets briefed on the mission objective (the attribute type to edit), assigns crew
    // to their stations, and starts listening for incoming alerts from the fleet.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the editor: wires the editor to its Eclipse site and input, extracts
     * the attribute type from the input, creates a mutable working copy, locates the
     * original schema, and registers listeners for schema-handler events.
     * If the schema handler fires an event about our attribute type or schema while
     * we're open, we'll respond and refresh all pages.
     *
     * <p>For example — Antilles assumes command:</p>
     * <pre>
     *   // 1. Set site and input (Eclipse bookkeeping).
     *   // 2. Extract originalAttributeType from the input.
     *   // 3. Clone it into modifiedAttributeType — edits go on the clone, not the original.
     *   // 4. Register schemaHandlerListener — react to external schema changes.
     *   // 5. Register pageChangedListener — validate before switching tabs.
     * </pre>
     *
     * @param site   the Eclipse editor site that provides window/page/shell context
     * @param input  the {@link AttributeTypeEditorInput} identifying which attribute
     *               type to open; must be an instance of AttributeTypeEditorInput
     * @throws PartInitException  if the super.init call fails or the input is wrong type
     */
    public void init( IEditorSite site, IEditorInput input ) throws PartInitException
    {
        super.init( site, input );

        instance = this;

        setSite( site );
        setInput( input );
        setPartName( input.getName() );

        originalAttributeType = ( AttributeType ) ( ( AttributeTypeEditorInput ) getEditorInput() )
            .getAttributeType();
        modifiedAttributeType = PluginUtils.getClone( originalAttributeType );

        SchemaHandler schemaHandler = Activator.getDefault().getSchemaHandler();
        originalSchema = schemaHandler.getSchema( originalAttributeType.getSchemaName() );
        schemaHandler.addListener( schemaHandlerListener );

        addPageChangedListener( pageChangedListener );
    }


    // ── Antilles Powers Down the Bridge ──────────────────────────────────────────────
    // The mission is over; Antilles removes all listeners before handing off to
    // the standard shutdown sequence so no dangling callbacks fire on a dead editor.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up the editor on close: deregisters the schema handler listener (so we
     * stop receiving events from outside) and delegates to the super-class dispose
     * which tears down the SWT/JFace form and its pages.
     * Always call removeListener before super.dispose() — the reverse order would risk
     * a listener firing against already-disposed page widgets.
     */
    public void dispose()
    {
        SchemaHandler schemaHandler = Activator.getDefault().getSchemaHandler();
        schemaHandler.removeListener( schemaHandlerListener );

        super.dispose();
    }


    // ── Antilles Assigns Crew to Their Stations ───────────────────────────────────────
    // Before the mission starts, Antilles assigns each officer to their console:
    // overview station, source-code comms desk, and the "used by" intel board.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and registers the three tab pages of the editor.
     * Eclipse calls this once, after {@link #init}, to build the editor UI.  We create
     * the Overview, Source Code, and Used By pages in that order — the order also
     * determines their left-to-right tab position.
     * Errors during page creation are logged via PluginUtils rather than thrown, so the
     * editor can still open even if one page fails to initialise.
     *
     * <p>For example — the crew takes their stations:</p>
     * <pre>
     *   addPage( new AttributeTypeEditorOverviewPage( this ) );   // tab 1
     *   addPage( new AttributeTypeEditorSourceCodePage( this ) ); // tab 2
     *   addPage( new AttributeTypeEditorUsedByPage( this ) );     // tab 3
     * </pre>
     */
    protected void addPages()
    {
        try
        {
            overviewPage = new AttributeTypeEditorOverviewPage( this );
            addPage( overviewPage );
            sourceCodePage = new AttributeTypeEditorSourceCodePage( this );
            addPage( sourceCodePage );
            usedByPage = new AttributeTypeEditorUsedByPage( this );
            addPage( usedByPage );
        }
        catch ( PartInitException e )
        {
            PluginUtils.logError( "error when adding pages", e ); //$NON-NLS-1$
        }
    }


    // ── Antilles Orders the Fleet to Commit the Changes ──────────────────────────────
    // When the mission is declared a success, Antilles orders the changes committed to
    // the official record — but first he makes sure the source-code page has no errors
    // (you don't file a battle report with typos in the coordinates).  If the source
    // code is invalid, he cancels the commit and tells the crew why.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the modified attribute type back to the schema handler.
     * We validate the source-code page first: if it has parse errors (canLeaveThePage
     * returns false), we show an error dialog and cancel the save via the monitor rather
     * than persisting invalid schema.
     * On a successful save, we push the working copy into the schema handler which will
     * broadcast a "attributeTypeModified" event, causing all open views to refresh.
     *
     * <p>For example — Antilles commits the battle report:</p>
     * <pre>
     *   if ( !sourceCodePage.canLeaveThePage() ) { notifyError(...); cancel; return; }
     *   schemaHandler.modifyAttributeType( original, modified );  // committed
     *   setDirty( false );  // editor is clean again
     * </pre>
     *
     * @param monitor  the Eclipse progress monitor; we call setCanceled(true) on it if
     *                 validation fails so Eclipse knows the save didn't complete
     */
    public void doSave( IProgressMonitor monitor )
    {
        // Verifying if there is an error on the source code page
        if ( !sourceCodePage.canLeaveThePage() )
        {
            notifyError( Messages.getString( "AttributeTypeEditor.AttributeErrors" ) ); //$NON-NLS-1$
            monitor.setCanceled( true );
            return;
        }

        Activator.getDefault().getSchemaHandler().modifyAttributeType( originalAttributeType, modifiedAttributeType );

        setPartName( getEditorInput().getName() );
        if ( !monitor.isCanceled() )
        {
            setDirty( false );
        }
    }


    // ── Antilles Has No Second Ship to Save Onto ─────────────────────────────────────
    // The Tantive IV can only file its report with fleet command — there's no "save a
    // copy to a different ship" operation in this workflow.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — "Save As" is not supported for attribute type editors.
     * The attribute type is always saved back to the same schema it came from; there
     * is no concept of "save a copy somewhere else" in this editor.
     */
    public void doSaveAs()
    {
        // Nothing to do.
    }


    // ── Checking if There's a Second Ship Available ──────────────────────────────────
    // Is there another ship to save onto?  No.  The Tantive IV operates alone on this
    // mission.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns false because "Save As" is not allowed for this editor type.
     * Eclipse checks this to decide whether to show a "Save As" menu item; returning
     * false keeps the menu item disabled.
     *
     * @return  always {@code false}
     */
    public boolean isSaveAsAllowed()
    {
        return false;
    }


    // ── Bridge Reports Current Readiness State ───────────────────────────────────────
    // Any crew member can query the bridge: "Are we in a modified state that needs
    // saving?"  The bridge checks its dirty flag and reports back.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the editor has unsaved changes.
     * Eclipse calls this to decide whether to show the unsaved-changes indicator on the
     * tab and whether to prompt the user on close.  Subpages call
     * {@link #setDirty(boolean)} to update this flag when the user makes changes.
     *
     * @return  {@code true} if the editor has unsaved changes, {@code false} if clean
     */
    public boolean isDirty()
    {
        return this.dirty;
    }


    // ── Antilles Updates the Ship's Status Log ────────────────────────────────────────
    // When something changes on the bridge, Antilles updates the mission status log
    // and broadcasts the new status to the Eclipse framework via editorDirtyStateChanged.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the editor's dirty state and notifies Eclipse so the UI reflects the change.
     * After setting the flag, we call {@link #editorDirtyStateChanged()} which is the
     * Eclipse hook that triggers the tab asterisk ("*") and enables/disables the Save
     * action.  Pages should call this through {@link AbstractAttributeTypeEditorPage#setEditorDirty()}.
     *
     * <p>For example — marking the editor dirty after a field change:</p>
     * <pre>
     *   getModifiedAttributeType().setObsolete( true );
     *   setDirty( true );   // Eclipse tab now shows "* cn"
     * </pre>
     *
     * @param dirty  {@code true} to mark the editor as having unsaved changes,
     *               {@code false} after a successful save
     */
    public void setDirty( boolean dirty )
    {
        this.dirty = dirty;
        editorDirtyStateChanged();
    }


    // ── Bridge Provides the Original Mission Briefing ────────────────────────────────
    // Any crew station can ask the bridge for the original mission briefing — the last
    // committed version of the attribute type, before any in-progress edits.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the original (pre-edit) attribute type.
     * This is the version that was in the schema when the editor was opened (or when
     * it was last saved).  It doesn't change while the editor is open unless an external
     * schema event comes in and the schemaHandlerListener updates it.
     * Pages use this for comparison — e.g. to check whether the OID was changed.
     *
     * @return  the unmodified baseline AttributeType; callers must not mutate this
     */
    public AttributeType getOriginalAttributeType()
    {
        return originalAttributeType;
    }


    // ── Bridge Provides the Live Working State ────────────────────────────────────────
    // Any crew station can ask the bridge for the current working state — the mutable
    // copy that has all the user's in-progress edits applied.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the mutable working copy of the attribute type.
     * Pages mutate this object directly as the user edits widgets.  When the user saves,
     * this copy is pushed to the schema handler via {@link #doSave}.
     *
     * @return  the mutable in-progress AttributeType; callers are expected to mutate it
     */
    public AttributeType getModifiedAttributeType()
    {
        return modifiedAttributeType;
    }


    // ── Antilles Accepts a New Working State from the Source Code Page ────────────────
    // The source-code comms desk has just parsed a fresh set of coordinates from the
    // raw source and hands the new state to the bridge captain for distribution.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the current working copy with a new one.
     * The Source Code page calls this after successfully parsing user-edited schema text
     * so that the Overview and Used By pages will show the updated state on their next
     * refresh.
     *
     * <p>For example — the source page hands a freshly-parsed AT to the bridge:</p>
     * <pre>
     *   // Source code page parsed the text and built a new AttributeType:
     *   getEditor().setModifiedAttributeType( freshlyParsedAT );
     * </pre>
     *
     * @param modifiedAttributeType  the new working-copy AttributeType to use from this
     *                               point forward
     */
    public void setModifiedAttributeType( AttributeType modifiedAttributeType )
    {
        this.modifiedAttributeType = modifiedAttributeType;
    }


    // ── Antilles Sounds the General Alarm ────────────────────────────────────────────
    // When something goes wrong that the crew needs to know about immediately, Antilles
    // opens the shipwide intercom and broadcasts the error message to everyone on board.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens a modal error dialog with the given message.
     * We use this when validation fails — for example if the source code page has parse
     * errors when the user tries to switch tabs or save.  The dialog blocks until the
     * user acknowledges it so we can safely abort the failing operation afterward.
     *
     * <p>For example — Antilles sounds the alarm:</p>
     * <pre>
     *   // Source code page has errors:
     *   notifyError( Messages.getString( "AttributeTypeEditor.CodeErrors" ) );
     *   // User clicks OK; we return; the tab switch or save is aborted by the caller.
     * </pre>
     *
     * @param message  the error message to display in the dialog
     */
    private void notifyError( String message )
    {
        MessageDialog.openError( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
            Messages.getString( "AttributeTypeEditor.Error" ), message ); //$NON-NLS-1$
    }
}
