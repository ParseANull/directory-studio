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

package org.apache.directory.studio.ldapbrowser.ui.editors.searchresult;


import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;


// ── CLASS: SearchResultEditorInput — Leia's Hologram Message ─────────────────
// "Help me, Obi-Wan Kenobi, you're my only hope."  Leia's hologram carries the
// plans for the Death Star — it's the data packet that R2-D2 delivers to whoever
// needs to act on it.  The hologram is a wrapper: it holds the real information
// (the Death Star plans) and identifies itself to whoever receives it.
// This input object is that hologram: it wraps an ISearch (the real data) and
// presents it to Eclipse's editor framework, which uses it to decide which editor
// to open and whether this input is "the same" as another one.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The {@link IEditorInput} for the search result editor.
 * Eclipse's editor framework uses this to identify what an editor is displaying.
 * We wrap an {@link ISearch} and implement an "equals trick" to control whether
 * the editor reuses an existing tab ({@code dummy=true} → all inputs are equal, so
 * Eclipse reuses the same editor tab) or creates distinct history entries
 * ({@code dummy=false} → equality is based on the wrapped search).
 * Think of this as Leia's hologram: it carries the real information and
 * identifies itself, but is never saved to disk.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultEditorInput implements IEditorInput
{

    /** The search input */
    private ISearch search;

    /** Flag indicating this is a dummy input */
    private boolean dummy;


    // ── R2-D2 Delivers the Hologram ───────────────────────────────────────────
    // R2 hands the hologram to Luke — it contains the real plans (ISearch) and
    // is not a dummy.  Normal usage: one search, one non-dummy input.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a normal (non-dummy) input wrapping the given search.
     * This is the standard constructor for navigation history entries.
     *
     * @param search the LDAP search whose results will be shown; may be {@code null}
     *               to represent "no search selected"
     */
    public SearchResultEditorInput( ISearch search )
    {
        this( search, false );
    }


    // ── R2-D2 Produces the Hologram (with Options) ───────────────────────────
    // When the dummy flag is set, the hologram's identity is collapsed — every
    // dummy looks the same to the editor framework, which keeps the search result
    // editor reusing a single tab for all searches.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an input wrapping the given search, with an explicit dummy flag.
     * When {@code dummy=true} the {@link #equals} method returns {@code true} for
     * any other dummy input, which causes Eclipse to reuse the same editor tab
     * rather than opening a new one for each different search.
     *
     * @param search the LDAP search to wrap; may be {@code null}
     * @param dummy  {@code true} to make all inputs of this type appear equal
     *               (single-tab mode); {@code false} for navigation history mode
     */
    /*package*/SearchResultEditorInput( ISearch search, boolean dummy )
    {
        this.search = search;
        this.dummy = dummy;
    }


    // ── The Hologram Is Not in the Recent Files List ──────────────────────────
    // Leia's hologram doesn't get saved to a "most recently opened" list —
    // search results are transient.  Always returns false.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} — searches should not appear in the
     * workbench's "File → Most Recently Used" menu.
     *
     * @return {@code false}
     */
    public boolean exists()
    {
        return false;
    }


    // ── The Hologram Has a Distinctive Icon ───────────────────────────────────
    // Leia's hologram glows blue — our input has a search-result-editor icon
    // so it's recognizable in the editor tab.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the search result editor tab icon.
     *
     * @return the image descriptor; never null
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_BROWSER_SEARCHRESULTEDITOR );
    }


    // ── The Hologram Identifies Its Source ────────────────────────────────────
    // "This is Princess Leia of the Alderaan System." — the hologram names itself.
    // We return the search name as the editor tab label.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for the editor tab.
     * If a search is set, we return its name; otherwise we return the
     * "no search selected" message.
     *
     * @return the name string; never null
     */
    public String getName()
    {
        if ( search != null )
        {
            return search.getName();
        }

        return Messages.getString( "SearchResultEditorContentProvider.NoSearchSelected" ); //$NON-NLS-1$
    }


    // ── The Hologram Provides Hover Text ─────────────────────────────────────
    // Hovering over the editor tab shows the full search URL and connection name —
    // more detail than just the search name.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the tooltip text for the editor tab.
     * We include the search URL and the connection name so the user can tell
     * which server this search ran against.
     *
     * @return the tooltip string; never null
     */
    public String getToolTipText()
    {
        if ( search != null )
        {
            String toolTipText = search.getUrl().toString();

            IBrowserConnection browserConnection = search.getBrowserConnection();
            if ( browserConnection != null && browserConnection.getConnection() != null )
            {
                toolTipText += " - " + browserConnection.getConnection().getName();//$NON-NLS-1$
            }
            return toolTipText;
        }

        return Messages.getString( "SearchResultEditorContentProvider.NoSearchSelected" ); //$NON-NLS-1$
    }


    // ── The Hologram Cannot Be Saved ──────────────────────────────────────────
    // Leia's message self-destructs after delivery — it's not persistable.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code null} — search result inputs are not persistable.
     * Eclipse will not try to restore this input across restarts.
     *
     * @return {@code null}
     */
    public IPersistableElement getPersistable()
    {
        return null;
    }


    // ── The Hologram Adapts to Nothing ────────────────────────────────────────
    // No adapter protocol support — we return null for all adapter queries.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} for all adapter types.
     * We don't participate in Eclipse's adapter protocol.
     *
     * @param adapter the requested adapter type
     * @return {@code null}
     */
    public Object getAdapter( Class adapter )
    {
        return null;
    }


    // ── The Hologram Carries the Plans ────────────────────────────────────────
    // The real payload — the ISearch — is accessible here.  May be null if the
    // editor is in a "nothing selected" state.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the wrapped {@link ISearch}, or {@code null} if no search is set.
     * The editor uses this to load the search results into the table.
     *
     * @return the wrapped search, or {@code null}
     */
    public ISearch getSearch()
    {
        return search;
    }


    // ── Every Hologram Has a Unique Signature ────────────────────────────────
    // The hash code is based on the tooltip text (search URL) so equal inputs
    // hash consistently.  Dummy inputs all hash to 0.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a hash code based on the search URL.
     * Dummy inputs all return 0 so they hash to the same bucket and compare equal.
     *
     * @return the hash code
     */
    public int hashCode()
    {
        if ( dummy )
        {
            return 0;
        }

        return getToolTipText().hashCode();
    }


    // ── Two Holograms Are Compared by Their Plans ─────────────────────────────
    // Two dummy holograms are always equal (same tab).  Two non-dummy holograms
    // are equal only if they wrap the same ISearch (for navigation history).
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Compares two inputs by their wrapped searches.
     * The dummy-flag trick controls single-tab vs. multi-tab behavior:
     * two dummy inputs are always equal → single editor tab reused.
     * Two non-dummy inputs are equal only if their wrapped searches are equal →
     * navigation history can distinguish different searches.
     *
     * @param obj the object to compare with
     * @return {@code true} if the inputs represent the same editor content
     */
    public boolean equals( Object obj )
    {
        if ( !( obj instanceof SearchResultEditorInput ) )
        {
            return false;
        }

        SearchResultEditorInput other = ( SearchResultEditorInput ) obj;

        if ( dummy && other.dummy )
        {
            return true;
        }
        if ( dummy != other.dummy )
        {
            return false;
        }

        if ( this.search == null && other.search == null )
        {
            return true;
        }
        else if ( this.search == null || other.search == null )
        {
            return false;
        }
        else
        {
            return other.search.equals( this.search );
        }
    }

}
