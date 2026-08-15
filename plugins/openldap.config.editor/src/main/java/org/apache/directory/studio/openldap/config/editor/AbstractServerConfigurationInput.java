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
package org.apache.directory.studio.openldap.config.editor;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;

import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPlugin;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPluginConstants;
import org.apache.directory.studio.openldap.config.jobs.EntryBasedConfigurationPartition;


// ── CLASS: AbstractServerConfigurationInput — Leia's Hologram Template ───────
// In A New Hope, Leia's hologram has a fixed structure: there's always a sender,
// always a recipient (Obi-Wan), always a plea.  Whether it plays in a dusty
// hovel on Tatooine or deep in the Rebel base, the structure is the same.
// This abstract class is that template — it locks in the shared behaviors
// (partition storage, tooltip, name, image) so the concrete subclasses only
// need to fill in what's unique about their specific source (connection vs. directory).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base for all server configuration inputs — the shared scaffolding
 * that every concrete input (connection-based, directory-based, or new)
 * builds on.
 * It implements {@link ServerConfigurationInput} and provides default
 * implementations of the Eclipse {@code IEditorInput} methods so subclasses
 * don't repeat themselves.
 * Think of it as Leia's hologram recorder: the hardware is always the same;
 * only the message inside changes per subclass.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractServerConfigurationInput implements ServerConfigurationInput
{
    /** The original configuration partition — our snapshot of the config as it
     *  was when we first loaded it, used to diff against on save. */
    protected EntryBasedConfigurationPartition originalPartition;


    // ── Hologram Retrieves The Captured Message ────────────────────────────────
    // Obi-Wan reaches into R2-D2 and retrieves the hologram Leia recorded
    // before she was captured — it's exactly as she left it.
    // We return the partition snapshot we stored when the config was first loaded,
    // unchanged, so callers can compute a diff against it.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the original configuration partition we captured at load time.
     * Callers (typically the save logic) use this to compute the minimum set
     * of LDAP modifications needed rather than rewriting the whole config.
     *
     * @return the original partition snapshot, or {@code null} if not yet set
     */
    public EntryBasedConfigurationPartition getOriginalPartition()
    {
        return originalPartition;
    }


    // ── Leia Encodes Her Message Into The Droid ───────────────────────────────
    // Before boarding, Leia's technicians seal her plea into R2-D2's memory —
    // locking in the state of things as they were right then.
    // We store the partition here so the save logic can retrieve it later and
    // know exactly what the config looked like when we started editing.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Stores the original configuration partition for later diff comparison.
     * This is called by the background load job once it finishes reading the
     * config and building the in-memory partition.
     *
     * @param originalPartition  the partition to remember — must not be modified
     *                           after this point so it stays a clean baseline
     */
    public void setOriginalPartition( EntryBasedConfigurationPartition originalPartition )
    {
        this.originalPartition = originalPartition;
    }


    // ── Hologram Displays The Sender's Name ───────────────────────────────────
    // Leia's recording opens with her identity — "I am Leia Organa of the
    // Alderaan system" — so Obi-Wan knows who's talking.
    // We return the same string as getName() because for most inputs the tooltip
    // and the tab label are identical.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the tooltip text shown when the user hovers over the editor tab.
     * By default we just return the same value as {@link #getName()}, but
     * subclasses can override to show a longer path or connection URL.
     *
     * @return the tooltip string — defaults to {@code "OpenLDAP Configuration"}
     */
    public String getToolTipText()
    {
        return getName();
    }


    // ── Hologram Identifies The Sender ───────────────────────────────────────
    // "Help me, Obi-Wan Kenobi" — the first words declare who's speaking and
    // why the recipient should care.
    // We return the label shown in the editor tab, identifying which config
    // we're editing.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the label displayed in the Eclipse editor tab for this input.
     * Subclasses override this when the name should reflect a specific
     * connection name or file path rather than the generic fallback.
     *
     * @return {@code "OpenLDAP Configuration"} unless overridden
     */
    public String getName()
    {
        return "OpenLDAP Configuration";
    }


    // ── Hologram Confirms The Recording Exists ────────────────────────────────
    // When Obi-Wan asks R2-D2 to replay the hologram, the droid confirms
    // it has the data before projecting.
    // We always return true here because the abstract base doesn't know about
    // missing files — subclasses override to check their specific source.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the underlying config source actually exists.
     * The base implementation always returns {@code true}; subclasses that
     * wrap a live connection or a directory on disk override this to do a
     * real existence check.
     *
     * @return {@code true} by default
     */
    public boolean exists()
    {
        return true;
    }


    // ── Hologram Projects The Rebel Crest ─────────────────────────────────────
    // Leia's message is sealed with the Rebel Alliance's starbird emblem —
    // the icon that identifies who sent it and what it represents.
    // We return the editor icon from our plugin's image registry so Eclipse
    // can decorate the editor tab properly.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the editor tab icon.
     * We pull the standard OpenLDAP config editor icon from our plugin's
     * image registry — it's the same icon regardless of whether the input
     * comes from a connection or a local directory.
     *
     * @return the image descriptor for our editor icon, or {@code null} if
     *         the plugin can't find the image
     */
    public ImageDescriptor getImageDescriptor()
    {
        return OpenLdapConfigurationPlugin.getDefault().getImageDescriptor(
            OpenLdapConfigurationPluginConstants.IMG_EDITOR );
    }


    // ── Hologram Has No Return Address ────────────────────────────────────────
    // Leia's message is a one-way broadcast — there's no mechanism built in
    // to save the recording's location back to Eclipse's workspace memento.
    // We return null here because we don't support restoring the editor from
    // Eclipse's workbench state — the user must re-open it manually.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns null because we don't support Eclipse's workbench state
     * persistence for this input.
     * If we returned a real {@link IPersistableElement}, Eclipse would try
     * to restore the editor across restarts — we don't want that for live
     * LDAP connections.
     *
     * @return {@code null} always
     */
    public IPersistableElement getPersistable()
    {
        return null;
    }


    // ── Hologram Knows No Other Adapters ─────────────────────────────────────
    // R2-D2 carries Leia's message but isn't a tool for anything else —
    // if you try to use him as a hyperdrive motivator, you'll be disappointed.
    // We return null because our input doesn't adapt to any other Eclipse
    // framework type — it's purpose-built for our editor.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns null for all adapter requests because we don't implement any
     * other Eclipse framework interfaces.
     * Eclipse calls this when it wants to know if our input can act as, say,
     * an {@code IResource} or a {@code IFile} — it can't, so we return null.
     *
     * @param adapter  the requested adapter class
     * @return {@code null} always
     */
    public Object getAdapter( Class adapter )
    {
        return null;
    }
}
