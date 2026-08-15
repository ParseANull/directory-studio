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


import java.io.File;

import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPlugin;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPluginConstants;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: DirectoryServerConfigurationInput — Han Jumps From A Local Docking Bay
// Han Solo occasionally makes shorter jumps — from a local spaceport rather
// than a deep-space rendezvous.  The Falcon still hits hyperspace, but the
// coordinates come from a known, fixed location on the ground rather than a
// live navigation signal from another ship.
// This class is that local-dock variant of our editor input: instead of a live
// LDAP connection, it reads the cn=config tree from a local filesystem
// directory (a slapd.d folder on disk).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Editor input that sources its OpenLDAP configuration from a local
 * filesystem directory rather than a live LDAP connection.
 * This is what we use when the user opens a slapd.d directory from their
 * machine via "Open Directory Configuration" — we wrap the {@link File}
 * here and hand it to the editor, which reads the LDIF files directly.
 * Think of Han jumping to hyperspace from a known local spaceport: we know
 * exactly where the config lives on disk and don't need a live server.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DirectoryServerConfigurationInput extends AbstractServerConfigurationInput
{
    /** The local filesystem directory containing the slapd.d configuration files. */
    private File directory;


    // ── Han Locks In The Local Spaceport Coordinates ──────────────────────────
    // Before the Falcon departs from Mos Eisley, Han makes sure the spaceport's
    // exact coordinates are in the nav computer.
    // We store the directory so every subsequent read and write knows where to
    // find the LDIF files on disk.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new input backed by the given local directory.
     * The directory should be a slapd.d folder containing OpenLDAP LDIF
     * configuration files.  Null is accepted but {@link #exists()} will
     * return false.
     *
     * @param directory  the slapd.d directory to read the config from
     */
    public DirectoryServerConfigurationInput( File directory )
    {
        this.directory = directory;
    }


    // ── Chewie Checks The Docking Bay Address ─────────────────────────────────
    // Chewbacca reads out the spaceport bay number from the nav logs so Han
    // can confirm they're heading to the right spot.
    // We expose the directory so the editor and save utilities can read from
    // and write to the correct path on disk.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the local filesystem directory this input points at.
     * The editor uses this to locate and read the LDIF files that make up
     * the cn=config tree.
     *
     * @return the directory, or {@code null} if not set
     */
    public File getDirectory()
    {
        return directory;
    }


    // ── Falcon Displays Full Path To The Spaceport ────────────────────────────
    // When filing a flight plan, the Falcon submits the full coordinates of
    // the departure spaceport — not just its nickname.
    // We return the full directory path as the tooltip so the user can see
    // exactly where on disk this config lives.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full filesystem path of the directory as the editor tab
     * tooltip — handy when the user has multiple config directories open.
     * Falls back to {@link #getName()} if the directory is null.
     *
     * @return the full path string, or the short name if directory is null
     */
    @Override
    public String getToolTipText()
    {
        if ( directory != null )
        {
            return directory.toString();
        }

        return getName();
    }


    // ── Falcon's Short Name Is The Bay Folder ─────────────────────────────────
    // On the bay assignment board, the Falcon is listed by her hangar folder
    // name — just "Bay 7," not the full coordinates string.
    // We return only the directory's simple name (last path component) as the
    // editor tab label, keeping things tidy.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the short name of the directory (just the last path component)
     * as the editor tab label.
     * Falls back to {@code "OpenLDAP Configuration"} if the directory is null.
     *
     * @return the directory's simple name, or a generic fallback
     */
    @Override
    public String getName()
    {
        if ( directory != null )
        {
            return directory.getName();
        }

        return "OpenLDAP Configuration";
    }


    // ── Falcon Confirms The Bay Is Actually There ─────────────────────────────
    // Before filing the flight plan, Han verifies the docking bay number
    // resolves to a real location — not a demolished wing of the spaceport.
    // We return true only if the directory field is non-null; the editor uses
    // this before trying to open the config files.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the directory field is non-null.
     * Note: we don't check whether the directory actually exists on disk —
     * that happens during the load job.  This just guards against a null
     * reference.
     *
     * @return {@code true} when a directory is set, {@code false} if null
     */
    @Override
    public boolean exists()
    {
        return directory != null;
    }


    // ── Spaceport Gets The Standard Alliance Emblem ───────────────────────────
    // Every Rebel spaceport hangs the same starbird emblem over the landing bay —
    // it's our shared iconography regardless of which base we're docking at.
    // We return the standard OpenLDAP editor icon so the tab looks consistent
    // whether the config came from a directory or a connection.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the standard editor icon image descriptor from our plugin
     * registry.
     * Directory-backed inputs use the same icon as connection-backed ones —
     * we don't differentiate visually.
     *
     * @return the editor icon descriptor, or {@code null} if the plugin
     *         can't find it
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return OpenLdapConfigurationPlugin.getDefault().getImageDescriptor(
            OpenLdapConfigurationPluginConstants.IMG_EDITOR );
    }


    // ── Two Bays In The Same Hangar Are Compared ─────────────────────────────
    // If two pilots both claim Bay 7 at Mos Eisley, we check the actual
    // directory file reference — same directory, same input.
    // We delegate to the File's own equals() so path comparison is
    // platform-correct.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if {@code obj} is a
     * {@link DirectoryServerConfigurationInput} pointing at the same directory.
     * We use {@link File#equals(Object)} for the comparison, which handles
     * path canonicalization for the current platform.
     *
     * @param obj  the object to compare to
     * @return {@code true} when both inputs point at the same directory
     */
    public boolean equals( Object obj )
    {
        if ( obj == null )
        {
            return false;
        }

        if ( obj instanceof DirectoryServerConfigurationInput )
        {
            DirectoryServerConfigurationInput input = ( DirectoryServerConfigurationInput ) obj;

            if ( input.exists() && exists() )
            {
                File inputDirectory = input.getDirectory();

                if ( inputDirectory != null )
                {
                    return inputDirectory.equals( directory );
                }
            }
        }

        return false;
    }


    // ── Bay's Unique Docking Code Is Filed ────────────────────────────────────
    // Each bay has a unique registration code so the control tower can index
    // them without collision.
    // We delegate to File's hashCode (which is path-based) so this input
    // works correctly in hash-based collections.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns a hash code based on the directory's path.
     * Consistent with {@link #equals(Object)}: two inputs pointing at the
     * same directory will return the same hash.
     * Falls back to {@code super.hashCode()} when the directory is null.
     *
     * @return the path-based hash code, or the object identity hash if null
     */
    public int hashCode()
    {
        if ( directory != null )
        {
            return directory.hashCode();
        }

        return super.hashCode();
    }
}
