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


import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPlugin;
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPluginConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;


// ── CLASS: ServerConfigurationInput — THE MASTER CONFIGURATION BRIEFING DOSSIER ─────────
// Before the Emperor can review the Death Star's configuration, his staff prepare a briefing
// dossier cover sheet: who it belongs to, what icon to show on the desk, whether the report
// already exists.  This is that cover sheet — not the configuration itself, just the metadata
// Eclipse needs to identify and open the configuration editor.
// This base implementation represents a generic (non-file-backed) editor input.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * Base Eclipse {@link IEditorInput} for the {@link ServerConfigurationEditor}.
 * Provides the editor icon and a static name; returns {@code null} for persistence
 * since this input type is not backed by a workspace resource.
 * Think of it as the cover sheet on the Emperor's configuration briefing dossier.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ServerConfigurationInput implements IEditorInput
{
    // ── Returning The Tooltip Text For The Editor Tab ─────────────────────────────────────────
    // Eclipse shows this in the tooltip when the user hovers over the editor tab.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the tooltip text for the editor tab — delegates to {@link #getName()}.
     *
     * @return the editor tab tooltip
     */
    public String getToolTipText()
    {
        return getName();
    }


    // ── Returning The Editor's Display Name ───────────────────────────────────────────────────
    // This is the static identifier shown in the editor tab title.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the static display name for this editor input ({@code "ServerConfigurationInput"}).
     *
     * @return the editor name
     */
    public String getName()
    {
        return "ServerConfigurationInput"; //$NON-NLS-1$
    }


    // ── Reporting Whether This Input Refers To Something That Exists ──────────────────────────
    // Always true here — the editor is backed by an in-memory configuration, not a file.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} — this input is always considered to exist.
     *
     * @return {@code true}
     */
    public boolean exists()
    {
        return true;
    }


    // ── Providing The Editor Tab Icon ─────────────────────────────────────────────────────────
    // The configuration editor icon from the plugin registry goes in the tab.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the configuration editor icon.
     *
     * @return the editor icon descriptor
     */
    public ImageDescriptor getImageDescriptor()
    {
        return ApacheDS2ConfigurationPlugin.getDefault().getImageDescriptor(
            ApacheDS2ConfigurationPluginConstants.IMG_EDITOR );
    }


    // ── Reporting Whether This Input Can Be Persisted ─────────────────────────────────────────
    // This base implementation is not persistable — subclasses that back a file override this.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — this input type is not persistable.
     *
     * @return {@code null}
     */
    public IPersistableElement getPersistable()
    {
        return null;
    }


    // ── Adapting To Other Types ───────────────────────────────────────────────────────────────
    // Standard Eclipse adapter pattern — this input adapts to nothing extra.
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
