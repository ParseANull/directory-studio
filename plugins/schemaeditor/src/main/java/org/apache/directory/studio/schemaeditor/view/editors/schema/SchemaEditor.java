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

package org.apache.directory.studio.schemaeditor.view.editors.schema;


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandlerAdapter;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandlerListener;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;


// ── CLASS: SchemaEditor — REBEL ALLIANCE BRIEFING ROOM ON YAVIN 4 ─────────────
// In the Rebel base on Yavin 4, General Dodonna stands before two display
// panels: the tactical overview showing all ships and defenses at a glance,
// and the technical readout showing the Death Star's raw engineering schematics.
// Both panels draw from the same mission data, stay synchronized, and are
// torn down together when the briefing ends.
// This class is that briefing room: it assembles an Overview page and a
// Source Code page for a single LDAP {@link Schema}, keeps them synchronized
// via a schema handler listener, and cleans up when the editor tab closes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The main Eclipse multi-page {@link FormEditor} for browsing an LDAP {@link Schema}.
 * Unlike the object class editor, this editor is read-only — schemas themselves
 * aren't editable here; individual attribute types and object classes are edited
 * through their own editors. The Schema Editor provides an overview of everything
 * the schema contains, plus a source-code view of its raw OpenLDAP syntax.
 * Think of it as General Dodonna's briefing room: two synchronized views of the
 * same data, giving you both the tactical overview and the engineering details.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaEditor extends FormEditor
{
    /** The ID of the Editor */
    public static final String ID = PluginConstants.EDITOR_SCHEMA_ID;

    /** The editor */
    private SchemaEditor instance;

    /** The Overview Page */
    private SchemaEditorOverviewPage overviewPage;

    /** The Source Code page */
    private SchemaEditorSourceCodePage sourceCodePage;

    /** The associated schema */
    private Schema schema;

    /** The SchemaHandler listener */
    private SchemaHandlerListener schemaHandlerListener = new SchemaHandlerAdapter()
    {
        public void schemaRemoved( Schema s )
        {
            if ( schema.equals( s ) )
            {
                getEditorSite().getPage().closeEditor( instance, false );
            }
        }


        public void schemaRenamed( Schema schema )
        {
            // Refreshing the editor pages
            overviewPage.refreshUI();
            sourceCodePage.refreshUI();

            // Refreshing the part name (in case of a change in the name)
            setPartName( getEditorInput().getName() );

        }
    };


    // ── General Dodonna Opens the Briefing Room ────────────────────────────────
    // General Dodonna calls the briefing to order: the mission data is loaded,
    // the schema is identified, the briefing room monitors are connected to the
    // live feed, and the lights dim so everyone can see the display.
    // We initialize the editor from the input schema, attach the schema handler
    // listener, and set the tab title from the schema name.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Initializes this editor from a {@link SchemaEditorInput}.
     * We extract the {@link Schema} from the input, register a listener on the
     * schema handler so we react to renames and removals, and set the editor tab
     * title to the schema's name.
     *
     * @param site   the Eclipse editor site
     * @param input  must be a {@link SchemaEditorInput} carrying the target schema
     * @throws PartInitException  if initialization fails
     */
    public void init( IEditorSite site, IEditorInput input ) throws PartInitException
    {
        super.init( site, input );

        instance = this;

        setSite( site );
        setInput( input );
        setPartName( input.getName() );

        schema = ( ( SchemaEditorInput ) getEditorInput() ).getSchema();

        Activator.getDefault().getSchemaHandler().addListener( schemaHandlerListener );
    }


    // ── Dodonna Mounts the Display Panels ─────────────────────────────────────
    // Dodonna mounts the two briefing displays: the tactical overview on the
    // left panel, the technical schematics on the right. Both are created
    // and registered with the FormEditor framework so they appear as tabs.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Creates and registers the two editor pages.
     * The Overview page shows the schema's attribute types and object classes in
     * a tabular GUI; the Source Code page shows the raw OpenLDAP schema syntax.
     * Eclipse calls this once after {@link #init}.
     */
    protected void addPages()
    {
        try
        {
            overviewPage = new SchemaEditorOverviewPage( this );
            addPage( overviewPage );
            sourceCodePage = new SchemaEditorSourceCodePage( this );
            addPage( sourceCodePage );
        }
        catch ( PartInitException e )
        {
            PluginUtils.logError( "error when adding pages", e ); //$NON-NLS-1$
        }
    }


    // ── The Briefing Room Is Cleared ──────────────────────────────────────────
    // Once the mission is over, the briefing room is cleared — the displays
    // are powered down and the connection to the live mission feed is severed
    // so no stale events can come in after the fact.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Cleans up when this editor is closed.
     * We remove our {@link SchemaHandlerListener} to avoid memory leaks and
     * stale callbacks after the editor tab is gone.
     */
    public void dispose()
    {
        Activator.getDefault().getSchemaHandler().removeListener( schemaHandlerListener );

        super.dispose();
    }


    // ── Dodonna Doesn't Save the Briefing Slides ──────────────────────────────
    // The briefing data is a live read of the mission intelligence — there's
    // nothing to "save" because individual elements are saved through their own
    // editors. This method intentionally does nothing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * This editor is read-only at the schema level — individual attribute types and
     * object classes are edited in their own editors. There's nothing to save here.
     *
     * @param monitor  the Eclipse progress monitor (unused)
     */
    public void doSave( IProgressMonitor monitor )
    {
        // There's nothing to save
    }


    // ── No "Save As" for the Briefing ─────────────────────────────────────────
    // You can't save a copy of the tactical briefing as a different schema —
    // schemas are managed through the project, not the editor. This is a no-op.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * "Save As" is not supported. This editor is read-only at the schema level.
     */
    public void doSaveAs()
    {
    }


    // ── The Briefing Has No "Save As" Option ──────────────────────────────────
    // Dodonna confirms: this briefing display does not support saving a copy
    // to a different location. We tell Eclipse the same by returning false.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns {@code false} — "Save As" is not supported for schema editors.
     *
     * @return  always {@code false}
     */
    public boolean isSaveAsAllowed()
    {
        return false;
    }


    // ── Dodonna Shares the Mission Data ───────────────────────────────────────
    // Dodonna hands the mission data package to any officer who needs to
    // cross-reference the schema — they call this method to get the Schema object
    // the editor is currently displaying.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link Schema} this editor is currently displaying.
     * The two page classes call this to get the schema they should render.
     *
     * @return  the schema associated with this editor instance
     */
    public Schema getSchema()
    {
        return schema;
    }
}
