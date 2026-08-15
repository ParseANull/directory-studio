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

package org.apache.directory.studio.ldifeditor.editor;


import org.apache.directory.studio.ldifeditor.LdifEditorActivator;
import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.editors.text.ILocationProvider;


// ── CLASS: NonExistingLdifEditorInput — REBEL BLANK COMMUNIQUÉ FORM ──────────
// A Rebel operator sometimes starts a brand-new communiqué before they know
// which relay station will store it.  They use a blank form stamped with a
// temporary serial number ("LDIF 1", "LDIF 2", ...).
// NonExistingLdifEditorInput is that blank form: it satisfies Eclipse's need
// for a writeable path (pointing to the plugin's state location) without
// corresponding to a real file on disk.
// Inspired by org.eclipse.ui.internal.editors.text.NonExistingFileEditorInput.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * {@link IPathEditorInput} for LDIF files that do not yet exist on disk.
 * Used by File-&gt;New, the embedded modification-view LDIF editor, the batch
 * operation wizard, and the LDIF preference page.
 * Returns {@code false} from {@link #exists()} and {@code null} from
 * {@link #getPersistable()}, signalling to Eclipse that the file is transient.
 * The writeable path points to the plugin state location so the editor remains
 * editable.
 * Think of this as the blank communiqué form — serial-numbered, ready to fill in.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NonExistingLdifEditorInput implements IPathEditorInput, ILocationProvider
{
    /** The counter to create unique names */
    private static int counter = 0;

    /** The name, displayed in Editor tab */
    private String name;


    // ── ASSIGN A UNIQUE SERIAL NUMBER ─────────────────────────────────────────
    // The archivist stamps the next available serial number on the blank form.
    /**
     * Creates a new {@code NonExistingLdifEditorInput} and assigns it a unique
     * name ("LDIF 1", "LDIF 2", ...) from the shared counter.
     */
    public NonExistingLdifEditorInput()
    {
        counter++;
        name = "LDIF " + counter; //$NON-NLS-1$
    }


    // ── REPORT NON-EXISTENCE ──────────────────────────────────────────────────
    // The form does not correspond to a filed document yet.
    /**
     * Always returns {@code false} — this input does not correspond to an
     * existing file.
     *
     * @return {@code false}
     */
    public boolean exists()
    {
        return false;
    }


    // ── RETURN THE LDIF FILE ICON ─────────────────────────────────────────────
    // The form carries the standard LDIF communiqué icon.
    /**
     * Returns the image descriptor for the LDIF editor icon.
     *
     * @return the image descriptor
     */
    public ImageDescriptor getImageDescriptor()
    {
        return LdifEditorActivator.getDefault().getImageDescriptor( LdifEditorConstants.IMG_BROWSER_LDIFEDITOR );
    }


    // ── RETURN THE TAB LABEL ──────────────────────────────────────────────────
    /**
     * Returns the editor-tab label ("LDIF 1", "LDIF 2", ...).
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }


    // ── REPORT NON-PERSISTABILITY ─────────────────────────────────────────────
    // The form cannot be restored across Eclipse sessions.
    /**
     * Always returns {@code null} — this input is not persistable.
     *
     * @return {@code null}
     */
    public IPersistableElement getPersistable()
    {
        return null;
    }


    // ── RETURN THE TOOLTIP ────────────────────────────────────────────────────
    /**
     * Returns the editor tooltip (same as the name).
     *
     * @return the name
     */
    public String getToolTipText()
    {
        return name;
    }


    // ── ADAPT TO ILocationProvider ────────────────────────────────────────────
    // The editor needs this adapter to be editable — without a valid location
    // provider Eclipse treats the editor as read-only.
    /**
     * Returns {@code this} when {@code adapter} is {@link ILocationProvider},
     * otherwise delegates to the platform adapter manager.
     *
     * @param adapter  the requested adapter type
     * @return         the adapter, or {@code null}
     */
    public Object getAdapter( Class adapter )
    {
        if ( ILocationProvider.class.equals( adapter ) )
        {
            return this;
        }

        return Platform.getAdapterManager().getAdapter( this, adapter );
    }


    // ── PROVIDE A WRITEABLE PATH (ILocationProvider) ──────────────────────────
    // We route the path through getPath() so the location is always the
    // plugin state folder — a platform-independent, writeable location.
    /**
     * Returns the path for {@code element} if it is a
     * {@link NonExistingLdifEditorInput}, otherwise {@code null}.
     * Delegates to {@link #getPath()}.
     *
     * @param element  the element to get the path for
     * @return         the path, or {@code null}
     */
    public IPath getPath( Object element )
    {
        if ( element instanceof NonExistingLdifEditorInput )
        {
            NonExistingLdifEditorInput input = ( NonExistingLdifEditorInput ) element;
            return input.getPath();
        }

        return null;
    }


    // ── EQUALITY BY NAME ──────────────────────────────────────────────────────
    /**
     * Returns {@code true} if {@code o} is a {@code NonExistingLdifEditorInput}
     * with the same name.
     *
     * @param o  the object to compare
     * @return   {@code true} if equal
     */
    public boolean equals( Object o )
    {
        if ( o == this )
        {
            return true;
        }

        if ( o instanceof NonExistingLdifEditorInput )
        {
            NonExistingLdifEditorInput input = ( NonExistingLdifEditorInput ) o;
            return name.equals( input.name );
        }

        return false;
    }


    // ── HASH BY NAME ──────────────────────────────────────────────────────────
    /**
     * Returns the hash code of the name string.
     *
     * @return the hash code
     */
    public int hashCode()
    {
        return name.hashCode();
    }


    // ── RETURN THE PLUGIN STATE PATH ──────────────────────────────────────────
    // The plugin state location is platform-independent and writeable, so
    // the editor can read and write the temporary file there.
    /**
     * Returns the path {@code <stateLocation>/<name>.ldif} — a writeable,
     * platform-independent location that keeps the editor editable.
     *
     * @return the path
     */
    public IPath getPath()
    {
        return LdifEditorActivator.getDefault().getStateLocation().append( name + ".ldif" ); //$NON-NLS-1$
    }

}
