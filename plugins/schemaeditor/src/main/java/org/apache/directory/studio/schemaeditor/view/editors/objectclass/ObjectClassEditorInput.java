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


import java.util.List;

import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;


// ── CLASS: ObjectClassEditorInput — LEIA'S HOLOGRAM MESSAGE ──────────────────
// Princess Leia records her "Help me, Obi-Wan Kenobi" message inside R2-D2,
// wrapping her plea in a container that R2 can carry across the galaxy and
// deliver to exactly the right recipient.
// This class is that container: it wraps an {@link ObjectClass} inside an
// Eclipse {@link IEditorInput} so the workbench can carry it to the
// {@link ObjectClassEditor} and have the right editor open for the right object.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link IEditorInput} implementation that wraps an LDAP {@link ObjectClass}
 * for delivery to the {@link ObjectClassEditor}.
 * Eclipse's editor framework needs an input object to identify what's being edited,
 * provide a tab title, and determine whether two editors are showing the same thing.
 * Think of it as Leia's hologram — it carries the payload (the object class) to
 * the right destination (the editor) and self-identifies so duplicates are avoided.
 */
public class ObjectClassEditorInput implements IEditorInput
{
    /** The input object class */
    private ObjectClass objectClass;


    // ── Leia Records Her Message ───────────────────────────────────────────────
    // Leia presses "record" and embeds her plea inside R2-D2's memory banks —
    // the object is now wrapped and ready for delivery across the galaxy.
    // We do the same: tuck the object class into this input so the workbench
    // can hand it off to the editor that knows how to display it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an editor input that wraps the given {@link ObjectClass}.
     * Eclipse will pass this to {@link ObjectClassEditor#init} when the editor opens.
     *
     * @param obj  the {@link ObjectClass} to edit; should not be {@code null}
     */
    public ObjectClassEditorInput( ObjectClass obj )
    {
        super();
        objectClass = obj;
    }


    // ── Does Leia's Message Still Exist? ──────────────────────────────────────
    // If R2-D2 has already delivered the message, it's gone from his memory —
    // but here we check whether the wrapped object class is null (i.e. already
    // gone), not whether it's been "delivered."
    // Note: the logic here is inverted from what you might expect — this returns
    // true only when the object class is null, which is the "doesn't exist" case.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns {@code true} if the underlying {@link ObjectClass} is {@code null}.
     * The Eclipse contract says this should return {@code true} when the resource
     * the input refers to actually exists — but this implementation returns the
     * opposite. In practice Eclipse rarely uses this for FormEditor inputs.
     *
     * @return  {@code true} when the wrapped object class is {@code null}
     */
    public boolean exists()
    {
        return ( objectClass == null );
    }


    // ── R2 Has No Hologram Thumbnail ──────────────────────────────────────────
    // R2-D2's message capsule doesn't include a thumbnail image of Leia —
    // just the message content itself. Similarly, we return null because
    // the workbench tab icon comes from the editor, not from the input.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns {@code null} because we don't supply an image for the editor tab here.
     * The {@link ObjectClassEditor} itself controls its tab icon via the plugin registry.
     *
     * @return  always {@code null}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── R2 Announces the Sender's Name ────────────────────────────────────────
    // When R2 delivers the hologram, Obi-Wan asks: "Who sent this?" — R2 plays
    // back the object class's primary alias, or falls back to the OID if there
    // are no aliases. This name appears in the editor's tab title.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the display name for the editor tab — the first alias of the object class,
     * or its OID if no aliases are defined.
     * Eclipse shows this in the editor tab strip and in the window title bar.
     *
     * @return  the primary name or OID of the wrapped {@link ObjectClass}
     */
    public String getName()
    {
        List<String> names = objectClass.getNames();
        if ( ( names != null ) && ( names.size() > 0 ) )
        {
            return names.get( 0 );
        }
        else
        {
            return objectClass.getOid();
        }
    }


    // ── R2's Message Cannot Be Saved to Disk ──────────────────────────────────
    // Leia's hologram is a live transmission — it can't be persisted across
    // sessions in a generic way, so we return null to tell Eclipse there's
    // no IPersistableElement here and the editor state won't survive restart.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns {@code null} because we don't support Eclipse's session-persistence
     * mechanism for this editor input. If the user reopens Directory Studio, the
     * editor tab will not be restored automatically.
     *
     * @return  always {@code null}
     */
    public IPersistableElement getPersistable()
    {
        return null;
    }


    // ── R2 Announces Origin and Destination ───────────────────────────────────
    // When hovering over an editor tab, Eclipse shows a tooltip — our tooltip
    // tells the user the object class name and which schema it belongs to,
    // like an address label on R2's message: "From Leia, aboard the Tantive IV."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the tooltip text shown when the user hovers over the editor tab.
     * We format it as "{objectClassName} (from schema {schemaName})" using the
     * NLS message bundle for localization.
     *
     * @return  a localized tooltip identifying the object class and its home schema
     */
    public String getToolTipText()
    {
        return NLS.bind( Messages.getString( "ObjectClassEditorInput.FromSchema" ), new String[] //$NON-NLS-1$
            { getName(), objectClass.getSchemaName() } );
    }


    // ── R2 Cannot Adapt His Message Capsule ───────────────────────────────────
    // R2's hologram capsule is what it is — it doesn't transform into a
    // different kind of container on demand. We return null to signal that
    // this input doesn't adapt to any other Eclipse interface types.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns {@code null} for all adapter requests.
     * This editor input doesn't adapt to any optional Eclipse interfaces
     * (such as {@code IFile} or {@code IResource}).
     *
     * @param adapter  the requested adapter class
     * @return         always {@code null}
     */
    @SuppressWarnings("rawtypes")
    public Object getAdapter( Class adapter )
    {
        return null;
    }


    // ── Is This the Same Message? ──────────────────────────────────────────────
    // If two droids both carry messages from Leia, Eclipse needs to decide
    // whether to open a new editor tab or reuse the existing one — we check
    // whether both inputs wrap the same ObjectClass object.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Two {@link ObjectClassEditorInput} instances are equal if they wrap the
     * same {@link ObjectClass}. Eclipse uses this to avoid opening duplicate
     * editor tabs for the same object class.
     *
     * @param obj  the other object to compare against
     * @return     {@code true} if both inputs wrap the same {@link ObjectClass}
     */
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( !( obj instanceof ObjectClassEditorInput ) )
            return false;
        ObjectClassEditorInput other = ( ObjectClassEditorInput ) obj;
        return other.getObjectClass().equals( this.objectClass );
    }


    // ── Retrieve Leia's Original Message ──────────────────────────────────────
    // Obi-Wan reaches into R2 and pulls out the hologram itself — the actual
    // ObjectClass payload that was bundled inside this input wrapper.
    // The editor calls this during initialization to get the object to edit.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ObjectClass} wrapped by this editor input.
     * The {@link ObjectClassEditor} calls this in its {@code init} method to
     * retrieve the object class it should display and allow the user to edit.
     *
     * @return  the wrapped {@link ObjectClass}; never {@code null} if constructed properly
     */
    public ObjectClass getObjectClass()
    {
        return this.objectClass;
    }
}
