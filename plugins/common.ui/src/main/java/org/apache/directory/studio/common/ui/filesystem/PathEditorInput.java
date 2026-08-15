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

package org.apache.directory.studio.common.ui.filesystem;


import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.ILocationProvider;


// ── CLASS: PathEditorInput — R2-D2 NAVIGATING TO A TARGET FILE ───────────────
// R2-D2 locks onto a target file path on the local filesystem like a hyperdrive
// jump coordinate.  This class wraps an {@link IPath} into an Eclipse editor
// input so any part of the workbench can open, name, and navigate to a file
// purely by its local path — no workspace resource required.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We implement an editor input backed by a local filesystem path rather than
 * a workspace {@code IFile}.  This lets editors open arbitrary files from any
 * location on disk.  We also implement {@link ILocationProvider} so the
 * platform's adapter framework can resolve our path when needed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PathEditorInput implements IPathEditorInput, ILocationProvider
{
    /** The path */
    private IPath path;


    // ── CONSTRUCTOR PathEditorInput — LOCKING ONTO THE TARGET COORDINATE ─────
    // R2-D2 stores the jump coordinate — the file path — so everything else
    // we do can reference it.  The path is the single piece of state this
    // class carries from construction onward.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a new editor input pointing at the given local filesystem path.
     * All subsequent operations — checking existence, resolving names, building
     * icons — use this path.
     *
     * @param path the local filesystem path for the file to open
     */
    public PathEditorInput( IPath path )
    {
        this.path = path;
    }


    // ── METHOD exists — CONFIRMING THE TARGET IS IN RANGE ────────────────────
    // R2-D2 pings the target coordinate to make sure it actually exists before
    // committing to the jump.  We return true only when our path is non-null
    // and the file it points to actually exists on disk.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return {@code true} when our path is non-null and the file it
     * references actually exists on the local filesystem.
     *
     * @return {@code true} if the file exists
     * {@inheritDoc}
     */
    public boolean exists()
    {
        if ( path != null )
        {
            return path.toFile().exists();
        }

        return false;
    }


    // ── METHOD getAdapter — TRANSLATING BETWEEN DROID PROTOCOLS ─────────────
    // R2-D2 knows how to translate between protocols: if the caller wants an
    // ILocationProvider, we hand ourselves over; otherwise we delegate to the
    // platform's adapter manager to find the right translator.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return ourselves if the requested adapter is {@link ILocationProvider},
     * otherwise we delegate to the platform's adapter manager for any other
     * adapter type.
     *
     * @param adapter the adapter interface class being requested
     * @return an adapter instance, or {@code null} if none is available
     * {@inheritDoc}
     */
    public Object getAdapter( Class adapter )
    {
        if ( ILocationProvider.class.equals( adapter ) )
        {
            return this;
        }

        return Platform.getAdapterManager().getAdapter( this, adapter );
    }


    // ── METHOD getImageDescriptor — PROJECTING THE FILE'S ICON HOLOGRAM ───────
    // R2-D2 projects the appropriate icon hologram for the target file by asking
    // the editor registry which image goes with this file's extension.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We look up the icon for our file in Eclipse's editor registry and return
     * the corresponding {@link ImageDescriptor}.  The icon is chosen based on
     * the file's extension.
     *
     * @return the image descriptor for this file type
     * {@inheritDoc}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor( path.toString() );
    }


    // ── METHOD getName — REPORTING THE TARGET'S CALLSIGN ─────────────────────
    // R2-D2 reads the target's callsign off the path — just the filename, not
    // the full route.  Returns an empty string if the path is null so nothing
    // breaks downstream.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return just the filename component of our path (no directory prefix).
     * If our path is {@code null}, we return an empty string so callers always
     * get a safe non-null result.
     *
     * @return the filename, or {@code ""} if the path is null
     * {@inheritDoc}
     */
    public String getName()
    {
        if ( path != null )
        {
            return path.toFile().getName();
        }

        return ""; //$NON-NLS-1$
    }


    // ── METHOD getPath — REVEALING THE FULL JUMP COORDINATE ─────────────────
    // R2-D2 hands over the full hyperspace coordinate — the complete IPath.
    // Returns null if we were constructed without one, which callers should
    // guard against.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return the complete {@link IPath} this editor input was constructed
     * with, or {@code null} if it was not set.
     *
     * @return the file path, or {@code null}
     * {@inheritDoc}
     */
    public IPath getPath()
    {
        if ( path != null )
        {
            return path;
        }

        return null;
    }


    // ── METHOD getPath (ILocationProvider) — EXTRACTING THE COORDINATE FROM AN OBJECT
    // R2-D2 can also extract the jump coordinate from another PathEditorInput
    // object rather than from himself.  This satisfies the ILocationProvider
    // contract so the platform can extract paths through the adapter framework.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return the path stored inside the given element if it is a
     * {@link PathEditorInput}, or {@code null} for any other type.  This
     * method satisfies the {@link ILocationProvider} interface contract.
     *
     * @param element the element to extract a path from
     * @return the element's path, or {@code null} if the element is not a PathEditorInput
     * {@inheritDoc}
     */
    public IPath getPath( Object element )
    {
        if ( element instanceof PathEditorInput )
        {
            return ( ( PathEditorInput ) element ).getPath();
        }

        return null;
    }


    // ── METHOD getPersistable — CONFIRMING THIS IS A ONE-WAY JUMP ────────────
    // R2-D2 checks the mission parameters: this is a one-way, non-persistent
    // session.  We return null because PathEditorInput cannot be persisted
    // across workbench restarts.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return {@code null} because this editor input cannot be persisted
     * across workbench sessions.  Eclipse will not attempt to restore it on
     * next startup.
     *
     * @return {@code null} always
     * {@inheritDoc}
     */
    public IPersistableElement getPersistable()
    {
        return null;
    }


    // ── METHOD getToolTipText — DISPLAYING THE RELATIVE ROUTE ────────────────
    // R2-D2 displays the relative route to the target on the navigation screen
    // so the pilot knows how far away the file is from the current working
    // directory.  Returns an empty string when the path is null.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return a relative OS-style path string suitable for use as a hover
     * tooltip in the editor tab.  If our path is null, we return an empty
     * string so callers always receive a non-null value.
     *
     * @return the relative OS path string, or {@code ""} if the path is null
     * {@inheritDoc}
     */
    public String getToolTipText()
    {
        if ( path != null )
        {
            return path.makeRelative().toOSString();
        }

        return ""; //$NON-NLS-1$
    }


    // ── METHOD hashCode — COMPUTING THE NAVIGATION SIGNATURE ─────────────────
    // R2-D2 computes a unique navigation signature for the target path.  We
    // delegate to the path's own hashCode to ensure two inputs pointing at the
    // same path produce the same signature.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We delegate to our path's {@link IPath#hashCode()} so two
     * {@code PathEditorInput} instances pointing to the same path are
     * considered equal by hash-based collections.  Falls back to
     * {@code super.hashCode()} when the path is null.
     *
     * @return the hash code
     * {@inheritDoc}
     */
    public int hashCode()
    {
        if ( path != null )
        {
            return path.hashCode();
        }

        return super.hashCode();
    }


    // ── METHOD equals — VERIFYING TWO COORDINATES ARE THE SAME TARGET ─────────
    // R2-D2 checks whether two navigation coordinates point to exactly the same
    // target.  Two PathEditorInputs are equal when their paths are equal, which
    // lets the workbench avoid opening the same file in duplicate tabs.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return {@code true} when the given object is the same instance, or is
     * a {@link PathEditorInput} whose path equals ours.  This allows Eclipse
     * to detect when the same file is already open and reuse the existing editor
     * rather than opening a duplicate.
     *
     * @param o the object to compare against
     * @return {@code true} if the other object represents the same file
     * {@inheritDoc}
     */
    public boolean equals( Object o )
    {
        if ( path != null )
        {
            // Shortcut
            if ( this == o )
            {
                return true;
            }

            if ( o instanceof PathEditorInput )
            {
                PathEditorInput input = ( PathEditorInput ) o;

                return path.equals( input.path );
            }
        }

        return super.equals( o );
    }
}
