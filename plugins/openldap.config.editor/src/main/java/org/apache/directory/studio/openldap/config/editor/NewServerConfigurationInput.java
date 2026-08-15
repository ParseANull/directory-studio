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


import org.apache.directory.studio.openldap.config.model.OpenLdapConfigFormat;
import org.apache.directory.studio.openldap.config.model.OpenLdapVersion;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;


// ── CLASS: NewServerConfigurationInput — Second Death Star Blueprint Stage ───
// In Return of the Jedi, the Emperor oversees the construction of a new Death
// Star from scratch — workers assemble the superstructure before any occupants
// move in.  The station doesn't have a location yet; it's a blank slate defined
// only by its planned specifications (size, version, weaponry layout).
// This class is that blueprint stage for a new OpenLDAP config: it captures the
// user's chosen version and format before anything has been saved to disk.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Editor input for a brand-new OpenLDAP configuration that doesn't exist on
 * disk or in a server yet.
 * Unlike {@link ConnectionServerConfigurationInput} or
 * {@link DirectoryServerConfigurationInput}, this input doesn't point at any
 * existing resource — it carries only the user's chosen version and format
 * so the editor can scaffold an empty config from scratch.
 * Think of it as the Death Star blueprints before ground is broken: all the
 * design decisions are captured, but nothing has been built yet.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewServerConfigurationInput implements IEditorInput
{
    /** The OpenLDAP version the user selected for this new configuration. */
    private OpenLdapVersion openLdapVersion;

    /** The file format (LDIF, slapd.conf, etc.) the user wants to use. */
    private OpenLdapConfigFormat openLdapConfigFormat;

    // ── Construction Workers Label The Blueprint ──────────────────────────────
    // On the Death Star construction site, every schematic has a placard:
    // "SECOND DEATH STAR — ORBITAL BATTLE STATION — PHASE 1 SCAFFOLDING."
    // We return a descriptive label so the Eclipse editor tab is clearly marked
    // as an unsaved new configuration, not a file.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the tooltip shown when the user hovers over this editor's tab.
     * Since there's no file path yet, we use the localized "new configuration"
     * message from our resource bundle.
     *
     * @return a human-readable label for the unsaved new configuration
     */
    public String getToolTipText()
    {
        return Messages.getString( "NewServerConfigurationInput.NewOpenLDAPConfigurationFile" ); //$NON-NLS-1$
    }


    // ── Blueprint Is Stamped With Its Name ────────────────────────────────────
    // Every design document in the Imperial Engineering Corps gets a name
    // stamped on the cover page — how else does Vader know which set of plans
    // he's reviewing?
    // We return the same localized label as the tooltip so the editor tab
    // reads clearly.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the label shown on the editor tab for this new configuration.
     * It uses the same localized string as {@link #getToolTipText()}.
     *
     * @return the editor tab label for the unsaved new configuration
     */
    public String getName()
    {
        return Messages.getString( "NewServerConfigurationInput.NewOpenLDAPConfigurationFile" ); //$NON-NLS-1$
    }


    // ── Construction Is Already Underway ─────────────────────────────────────
    // The Emperor's plans exist and work has begun — construction is authorized
    // even before the station is operational.
    // We return true because the input itself exists as an object in memory,
    // even though the configuration file doesn't exist on disk yet.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} because the input object exists in memory, even
     * though there's no backing file on disk yet.
     * The editor uses this to distinguish between "no config at all" and
     * "unsaved new config."
     *
     * @return {@code true} always
     */
    public boolean exists()
    {
        return true;
    }


    // ── Blueprint Needs No Emblem Yet ─────────────────────────────────────────
    // Unfinished blueprints on the construction floor don't carry the official
    // Imperial seal — that comes later when the project is approved.
    // We return null because a new, unsaved config doesn't need a custom icon;
    // Eclipse will use its own default.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — we don't provide a custom icon for new
     * configurations.  Eclipse will fall back to its default.
     *
     * @return {@code null} always
     */
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── No Coordinates Exist For The New Station ─────────────────────────────
    // The second Death Star doesn't have a fixed location yet — it's still
    // being assembled and can't be stored in any star chart.
    // We return null because unsaved inputs can't be persisted to Eclipse's
    // workbench state (there's nothing to restore a location from).
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} because new, unsaved configurations can't be
     * persisted to Eclipse's workbench memento — there's no file path yet.
     *
     * @return {@code null} always
     */
    public IPersistableElement getPersistable()
    {
        return null;
    }


    // ── Blueprint Knows No Other Schematics ───────────────────────────────────
    // The Death Star schematics are unique — you can't adapt them into an
    // AT-AT blueprint.  They serve one purpose.
    // We return null because this input doesn't adapt to any other Eclipse
    // framework type.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} for all adapter requests — this input is
     * purpose-built and doesn't implement any Eclipse adapter interfaces.
     *
     * @param adapter  the class being requested
     * @return {@code null} always
     */
    @SuppressWarnings("rawtypes")
    public Object getAdapter( Class adapter )
    {
        return null;
    }


    // ── Architect Checks The Specified Version ────────────────────────────────
    // The lead engineer pulls out the spec sheet to confirm which generation
    // of hyperdrive motivator has been ordered for this Death Star.
    // We return the OpenLDAP version the user chose so the editor can generate
    // the right default configuration structure.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OpenLDAP version the user selected when starting a new
     * configuration.
     * The editor uses this to generate version-appropriate defaults.
     *
     * @return the selected OpenLDAP version, or {@code null} if not set
     */
    public OpenLdapVersion getOpenLdapVersion()
    {
        return openLdapVersion;
    }


    // ── Architect Records The Target Version ──────────────────────────────────
    // The imperial engineer writes the hyperdrive spec on the blueprint so
    // every subsequent decision about layout and power routing is consistent.
    // We store the version here so it's available when the editor initializes
    // the new configuration model.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the OpenLDAP version this new configuration targets.
     * Must be set before the editor opens so it can scaffold the right
     * default entries.
     *
     * @param openLdapVersion  the version to target — see {@link OpenLdapVersion}
     *                         for valid values
     */
    public void setOpenLdapVersion( OpenLdapVersion openLdapVersion )
    {
        this.openLdapVersion = openLdapVersion;
    }


    // ── Architect Checks The Output Format ────────────────────────────────────
    // The engineer confirms whether the final schematics will be printed as
    // LDIF data scrolls or as classic slapd.conf parchment.
    // We return the selected format so the writer knows how to serialize the
    // config when the user eventually saves.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the file format the user chose for this new configuration.
     * The save logic uses this to decide whether to write LDIF files or a
     * slapd.conf-style file.
     *
     * @return the chosen config format, or {@code null} if not yet set
     */
    public OpenLdapConfigFormat getOpenLdapConfigFormat()
    {
        return openLdapConfigFormat;
    }


    // ── Architect Stamps The Format On The Blueprint ─────────────────────────
    // The spec sheet gets a clear marking: "LDIF-BASED CONFIGURATION" so
    // construction crews know which tooling to bring.
    // We store the format choice so the save/write pipeline can read it later.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the file format for this new configuration.
     * Call this before opening the editor to ensure the right format is used
     * when the user saves for the first time.
     *
     * @param openLdapConfigFormat  the format to use — see
     *                              {@link OpenLdapConfigFormat} for options
     */
    public void setOpenLdapConfigFormat( OpenLdapConfigFormat openLdapConfigFormat )
    {
        this.openLdapConfigFormat = openLdapConfigFormat;
    }
}
