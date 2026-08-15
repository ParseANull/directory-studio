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
package org.apache.directory.studio.openldap.config.model;


import java.util.ArrayList;
import java.util.List;


// ── CLASS: OlcModuleList — Lando Running Cloud City ──────────────────────────
// Lando Calrissian runs Cloud City as its own self-contained operation — he knows
// exactly which workers are on the roster, where they report from, and which
// specialized contractors to call in for particular jobs.
// OlcModuleList is Cloud City's crew manifest: the path to where the dynamically
// loadable modules live, and the list of specific modules actually loaded at runtime.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Holds the 'olcModuleList' configuration entry — the module load path and the
 * list of overlay or backend modules that OpenLDAP should dynamically load at startup.
 * Think of this as the roster and location card Lando uses to manage his Cloud City
 * workforce: where to find talent, and who's currently on the team.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcModuleList extends OlcConfig
{
    /**
     * Field for the 'cn' attribute.
     */
    @ConfigurationElement(attributeType = "cn", isRdn = true, version="2.4.0")
    private List<String> cn = new ArrayList<>();

    /**
     * Field for the 'olcAllows' attribute.
     */
    @ConfigurationElement(attributeType = "olcModuleLoad", version="2.4.0")
    private List<String> olcModuleLoad = new ArrayList<>();

    /**
     * Field for the 'olcModulePath' attribute.
     */
    @ConfigurationElement(attributeType = "olcModulePath", version="2.4.0")
    private String olcModulePath;


    // ── Add CN — Lando Adds a Registry Entry ─────────────────────────────────────
    // Lando records a new identifier in the Cloud City administrative registry.
    // He can add several names in one batch rather than one at a time.
    // We bulk-add cn values so the naming attribute for this entry can be set quickly.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more cn values to our list.
     * The cn attribute is the RDN for this module-list entry in the DIT.
     *
     * <p>For example — Lando logs a new registry entry:</p>
     * <pre>
     *   moduleList.addCn( "module0" );
     * </pre>
     *
     * @param strings  one or more cn values to append
     */
    public void addCn( String... strings )
    {
        for ( String string : strings )
        {
            cn.add( string );
        }
    }


    // ── Add Module Load — Lando Hires New Crew ───────────────────────────────────
    // Lando signs contracts with new specialist contractors to work in Cloud City.
    // Each name in the list is a module file that slapd will dynamically open.
    // We append module filenames so OpenLDAP knows what to dlopen at startup.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more module filenames to the olcModuleLoad list.
     * These are the actual .so files slapd will load from the module path.
     *
     * <p>For example — Lando signs more contractors:</p>
     * <pre>
     *   moduleList.addOlcModuleLoad( "back_mdb.la", "ppolicy.la" );
     * </pre>
     *
     * @param strings  module filenames to load (e.g., "back_mdb.la", "ppolicy.la")
     */
    public void addOlcModuleLoad( String... strings )
    {
        for ( String string : strings )
        {
            olcModuleLoad.add( string );
        }
    }


    // ── Clear CN — Lando Clears the Registry ─────────────────────────────────────
    // Lando wipes the old registry entries to make room for fresh ones.
    // He's resetting the naming before a full reload.
    // We clear cn so a wholesale replacement can proceed cleanly.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all cn values from this object.
     * Useful when replacing the entire cn list rather than appending.
     *
     * <p>For example — Lando clears the old registry page:</p>
     * <pre>
     *   moduleList.clearCn();
     *   moduleList.addCn( "module0" );
     * </pre>
     */
    public void clearCn()
    {
        cn.clear();
    }


    // ── Clear Module Load — Lando Lets the Crew Go ───────────────────────────────
    // Lando releases all contractors so he can rebuild the roster from scratch.
    // No modules remain once this is called — slapd would load none on restart.
    // We clear the load list before a wholesale update.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears the entire olcModuleLoad list.
     * Call this before adding a new set of modules if you're replacing, not appending.
     *
     * <p>For example — Lando releases all contractors:</p>
     * <pre>
     *   moduleList.clearOlcModuleLoad();
     *   moduleList.addOlcModuleLoad( "back_mdb.la" );
     * </pre>
     */
    public void clearOlcModuleLoad()
    {
        olcModuleLoad.clear();
    }


    // ── Get CN — Lando Reads the Registry ────────────────────────────────────────
    // Lando consults the Cloud City administrative registry and hands over a copy.
    // He never exposes his raw paperwork to visitors — just a printed copy.
    // We return a defensive copy so callers can't mutate our internal list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the cn list.
     *
     * <p>For example — Lando hands over a printed registry copy:</p>
     * <pre>
     *   List&lt;String&gt; names = moduleList.getCn();
     *   // [ "module0" ]
     * </pre>
     *
     * @return  a copy of the cn list; never null
     */
    public List<String> getCn()
    {
        return copyListString( cn );
    }


    // ── Get Module Load — Lando Reads the Crew Roster ────────────────────────────
    // Lando checks who's on the active roster and hands over a copy of the list.
    // The caller sees who's contracted but can't modify Lando's books.
    // We expose the list of module filenames for the I/O layer to serialize.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the olcModuleLoad list.
     * These are the module files slapd will dynamically load from the module path.
     *
     * <p>For example — Lando reads out the active crew roster:</p>
     * <pre>
     *   List&lt;String&gt; mods = moduleList.getOlcModuleLoad();
     *   // [ "back_mdb.la", "ppolicy.la" ]
     * </pre>
     *
     * @return  a copy of the module load list; never null
     */
    public List<String> getOlcModuleLoad()
    {
        return copyListString( olcModuleLoad );
    }


    // ── Get Module Path — Lando Reads the Depot Address ──────────────────────────
    // Lando checks where Cloud City's equipment depot is located.
    // That's the directory slapd scans when looking for module .so files.
    // We expose the path so the I/O layer can serialize it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the olcModulePath — the filesystem directory where slapd looks for
     * dynamically loadable module files (.la/.so).
     *
     * <p>For example — Lando points to the equipment depot:</p>
     * <pre>
     *   String path = moduleList.getOlcModulePath();
     *   // "/usr/lib/ldap"
     * </pre>
     *
     * @return  the module search path, or null if not set
     */
    public String getOlcModulePath()
    {
        return olcModulePath;
    }


    // ── Set CN — Lando Receives New Registry Entries ──────────────────────────────
    // Lando receives a fresh set of administrative identifiers and stores them safely.
    // He makes his own copy so external changes don't scramble his records.
    // We defensively copy the incoming list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the entire cn list with a defensive copy of the given list.
     *
     * <p>For example — Lando logs fresh registry identifiers:</p>
     * <pre>
     *   moduleList.setCn( List.of( "module0" ) );
     * </pre>
     *
     * @param cn  the new cn list to store; may be null (treated as empty)
     */
    public void setCn( List<String> cn )
    {
        this.cn = copyListString( cn );
    }


    // ── Set Module Load — Lando Replaces the Crew Roster ─────────────────────────
    // Lando replaces his entire contractor list with a fresh roster.
    // He stores a copy so the caller can't accidentally change his records later.
    // We replace the entire module load list with a defensive copy.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the entire olcModuleLoad list with a defensive copy of the given list.
     *
     * <p>For example — Lando loads a fresh roster:</p>
     * <pre>
     *   moduleList.setOlcModuleLoad( List.of( "back_mdb.la" ) );
     * </pre>
     *
     * @param olcModuleLoad  the new module list to store; may be null (treated as empty)
     */
    public void setOlcModuleLoad( List<String> olcModuleLoad )
    {
        this.olcModuleLoad = copyListString( olcModuleLoad );
    }


    // ── Set Module Path — Lando Records the Depot Address ────────────────────────
    // Lando records the location of Cloud City's equipment storage facility.
    // That's where slapd will look when it needs to open a module file.
    // We store the path string directly since it's immutable.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the olcModulePath — the directory slapd scans when loading modules.
     *
     * <p>For example — Lando logs the depot address:</p>
     * <pre>
     *   moduleList.setOlcModulePath( "/usr/lib/ldap" );
     * </pre>
     *
     * @param olcModulePath  the filesystem path to the module directory
     */
    public void setOlcModulePath( String olcModulePath )
    {
        this.olcModulePath = olcModulePath;
    }
}
