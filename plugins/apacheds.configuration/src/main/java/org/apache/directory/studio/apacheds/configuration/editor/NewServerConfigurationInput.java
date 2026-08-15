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
package org.apache.directory.studio.apacheds.configuration.editor;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;


// ── CLASS: NewServerConfigurationInput — BLANK IMPERIAL REQUISITION FORM ─────────────────
// When an Imperial engineer wants to create a brand-new ApacheDS configuration from scratch,
// they pick up a blank requisition form — not tied to any existing data vault or connection,
// just an empty template waiting to be filled in.
// This is that blank form: an editor input that tells Eclipse "open a fresh configuration
// editor with default settings, no backing file".
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link IEditorInput} used when creating a brand-new ApacheDS configuration.
 * Has no backing file or connection — the editor starts from scratch with default settings.
 * Think of it as the blank Imperial requisition form: all fields empty, ready to fill in.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewServerConfigurationInput implements IEditorInput
{
    // ── Returning The Tooltip Text For The Editor Tab ─────────────────────────────────────────
    // Eclipse shows this in the tooltip when the user hovers over the editor tab.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised "New ApacheDS 2.0 Configuration File" tooltip text for the editor tab.
     *
     * @return the tooltip string
     */
    public String getToolTipText()
    {
        return Messages.getString( "NewServerConfigurationInput.NewApacheDS20ConfigurationFile" ); //$NON-NLS-1$
    }


    // ── Returning The Editor Tab Display Name ─────────────────────────────────────────────────
    // The same string appears as the editor's tab title.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised display name for the editor tab ("New ApacheDS 2.0 Configuration File").
     *
     * @return the tab title
     */
    public String getName()
    {
        return Messages.getString( "NewServerConfigurationInput.NewApacheDS20ConfigurationFile" ); //$NON-NLS-1$
    }


    // ── Reporting Whether This Input Exists ───────────────────────────────────────────────────
    // The blank form "exists" in the sense that the editor can open it immediately.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} — the blank form is always openable.
     *
     * @return {@code true}
     */
    public boolean exists()
    {
        return true;
    }


    // ── Providing No Icon ─────────────────────────────────────────────────────────────────────
    // A blank form has no specific icon — Eclipse picks up a generic one.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — no specific icon for the new-configuration editor tab.
     *
     * @return {@code null}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── Reporting Whether This Input Can Be Persisted ─────────────────────────────────────────
    // Blank forms aren't saved to Eclipse's session restore — the user saves them explicitly.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — new-configuration inputs are not persistable across sessions.
     *
     * @return {@code null}
     */
    public IPersistableElement getPersistable()
    {
        return null;
    }


    // ── Adapting To Other Types ───────────────────────────────────────────────────────────────
    // Standard Eclipse adapter pattern — no additional adapters supported.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} for all adapter types — no additional adapter support.
     *
     * @param adapter  the requested adapter type
     * @return {@code null}
     */
    @SuppressWarnings("rawtypes")
    public Object getAdapter( Class adapter )
    {
        return null;
    }
}
