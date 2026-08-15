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


import org.apache.directory.studio.schemaeditor.model.Schema;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;


// ── CLASS: SchemaEditorInput — HAN SOLO'S LANDING PERMIT AT MOS EISLEY ────────
// Han Solo needs a valid landing permit and docking authorization to bring the
// Millennium Falcon into Mos Eisley. He packages his credentials into a sealed
// document that the spaceport authority checks before directing him to the
// right docking bay — without the permit, the door doesn't open.
// This class is that permit: it wraps a {@link Schema} inside an Eclipse
// {@link IEditorInput} so the workbench can identify what we want to open,
// provide a tab title, and route the request to the correct {@link SchemaEditor}.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link IEditorInput} implementation that wraps an LDAP {@link Schema}
 * for delivery to the {@link SchemaEditor}.
 * Eclipse's editor framework needs an input object to identify what's being shown,
 * produce a tab title, and determine whether two editors are showing the same schema.
 * Think of it as Han's docking permit: it identifies the payload (the schema),
 * names the bay it should open (the editor tab), and prevents the same schema
 * from opening twice as separate tabs.
 */
public class SchemaEditorInput implements IEditorInput
{
    private Schema schema;


    // ── Han Stamps His Docking Permit ──────────────────────────────────────────
    // Han fills in the destination schema on his permit and hands it to the
    // spaceport authority — the schema is now bundled in this input and ready
    // to direct the workbench to open the right editor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an editor input wrapping the given {@link Schema}.
     * Eclipse will pass this to {@link SchemaEditor#init} when the editor opens.
     *
     * @param schema  the {@link Schema} to display; should not be {@code null}
     */
    public SchemaEditorInput( Schema schema )
    {
        super();
        this.schema = schema;
    }


    // ── Is the Docking Bay Still Available? ───────────────────────────────────
    // Han checks whether the docking bay he's aiming for actually exists in
    // this spaceport — if the schema is null, there's no bay to land in.
    // Note: this returns true only when the schema is null, which is the
    // "doesn't exist" case, matching the inverted semantics used in this codebase.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns {@code true} if the underlying {@link Schema} is {@code null}.
     * In practice Eclipse rarely uses this for FormEditor inputs.
     *
     * @return  {@code true} when the wrapped schema is {@code null}
     */
    public boolean exists()
    {
        return ( this.schema == null );
    }


    // ── The Permit Has No Thumbnail ───────────────────────────────────────────
    // Han's permit is a text document — no holographic thumbnail of the Falcon
    // attached. The editor tab icon comes from the editor class itself, not here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns {@code null} — the tab icon is provided by the {@link SchemaEditor} itself,
     * not by this input object.
     *
     * @return  always {@code null}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── Han Reads the Schema Name Off the Permit ──────────────────────────────
    // The spaceport authority reads the destination bay name off Han's permit
    // and announces it on the public-address system — that name goes in the
    // editor tab title so the user knows which schema they're viewing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the schema's name for use as the editor tab title.
     * Eclipse shows this in the editor tab strip and the window title bar.
     *
     * @return  the schema name as reported by {@link Schema#getSchemaName()}
     */
    public String getName()
    {
        return this.schema.getSchemaName();
    }


    // ── Han's Permit Is Single-Use ────────────────────────────────────────────
    // The docking permit can't be filed away for the next session — once the
    // Falcon lifts off, the permit expires. We return null to tell Eclipse that
    // this editor state won't be restored automatically on restart.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns {@code null} because we don't support Eclipse's session-persistence
     * mechanism. The schema editor tab will not be restored after an application restart.
     *
     * @return  always {@code null}
     */
    public IPersistableElement getPersistable()
    {
        return null;
    }


    // ── Han Announces the Bay Name ────────────────────────────────────────────
    // When the user hovers over the editor tab, the tooltip shows up — we
    // return the schema name so they know exactly what's displayed in this tab.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the tooltip text shown when hovering over the editor tab.
     * We return the schema name — same as {@link #getName()}.
     *
     * @return  the schema name
     */
    public String getToolTipText()
    {
        return getName();
    }


    // ── Han's Permit Doesn't Transform ────────────────────────────────────────
    // The docking permit is a docking permit — it doesn't become a different
    // kind of document just because someone asks nicely. We return null for
    // all adapter requests since this input doesn't adapt to other types.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns {@code null} for all adapter requests.
     * This editor input doesn't adapt to any optional Eclipse interfaces.
     *
     * @param adapter  the requested adapter class
     * @return         always {@code null}
     */
    @SuppressWarnings("rawtypes")
    public Object getAdapter( Class adapter )
    {
        return null;
    }


    // ── Is This the Same Docking Bay? ─────────────────────────────────────────
    // If two permits reference the same bay, the spaceport won't open a second
    // hangar door — Eclipse uses this to avoid duplicate editor tabs for the
    // same schema. We compare the wrapped Schema objects for equality.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Two {@link SchemaEditorInput} instances are equal if they wrap the same {@link Schema}.
     * Eclipse uses this to avoid opening duplicate editor tabs for the same schema.
     *
     * @param obj  the other object to compare against
     * @return     {@code true} if both inputs wrap the same {@link Schema}
     */
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( !( obj instanceof SchemaEditorInput ) )
            return false;
        SchemaEditorInput other = ( SchemaEditorInput ) obj;
        return other.getSchema().equals( this.schema );
    }


    // ── Han Hands Over the Schema ──────────────────────────────────────────────
    // The spaceport authority reaches through the permit window and takes the
    // schema document that was bundled inside. The editor calls this to get
    // the schema it should display.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link Schema} wrapped by this editor input.
     * The {@link SchemaEditor} calls this in its {@code init} method to retrieve
     * the schema it should display across its pages.
     *
     * @return  the wrapped {@link Schema}; never {@code null} if constructed properly
     */
    public Schema getSchema()
    {
        return this.schema;
    }
}
