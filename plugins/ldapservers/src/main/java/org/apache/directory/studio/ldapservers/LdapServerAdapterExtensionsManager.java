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
package org.apache.directory.studio.ldapservers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.directory.studio.ldapservers.model.LdapServerAdapter;
import org.apache.directory.studio.ldapservers.model.LdapServerAdapterExtension;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;


// ── CLASS: LdapServerAdapterExtensionsManager — C-3PO'S LANGUAGE REGISTRY ─────────────────
// C-3PO is fluent in over six million forms of communication — each language module
// plugged in at startup so he can translate between any species and the Rebellion.
// We do the same: at plugin startup we scan Eclipse's extension registry for every
// declared LDAP server adapter (ApacheDS, OpenLDAP, etc.) and load them into a map
// so the rest of the application can ask "give me the adapter for this server type."
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * Loads and manages all LDAP server adapter extensions declared via Eclipse's extension point system.
 * Different LDAP server types (ApacheDS, OpenLDAP, etc.) each supply their own adapter plugin —
 * this class is the central registry that collects them all at startup.
 * Think of this class as C-3PO's language module registry: every adapter is a language pack
 * that gets loaded once so the app can communicate with any server type.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapServerAdapterExtensionsManager
{
    // Attributes names used in 'plugin.xml' file
    public static final String ID_ATTR = "id"; //$NON-NLS-1$
    public static final String NAME_ATTR = "name"; //$NON-NLS-1$
    public static final String VERSION_ATTR = "version"; //$NON-NLS-1$
    public static final String VENDOR_ATTR = "vendor"; //$NON-NLS-1$
    public static final String CLASS_ATTR = "class"; //$NON-NLS-1$
    public static final String DESCRIPTION_ATTR = "description"; //$NON-NLS-1$
    public static final String ICON_ATTR = "icon"; //$NON-NLS-1$
    public static final String CONFIGURATION_PAGE_ATTR = "configurationPage"; //$NON-NLS-1$
    public static final String OPEN_CONFIGURATION_ACTION_ENABLED_ATTR = "openConfigurationActionEnabled"; //$NON-NLS-1$

    /** The default instance */
    private static LdapServerAdapterExtensionsManager instance;

    /** The list and map for LDAP Server Adapter Extensions */
    private List<LdapServerAdapterExtension> ldapServerAdapterExtensionsList;

    /** The map and map for LDAP Server Adapter Extensions */
    private Map<String, LdapServerAdapterExtension> ldapServerAdapterExtensionsByIdMap;


    // ── C-3PO Powers Up His Language Circuits ───────────────────────────────────────────────
    // Before the Millennium Falcon's first diplomatic mission, C-3PO's language banks are
    // initialized — empty at first, waiting for language modules to be loaded in.
    // We create the manager with no adapters yet; {@link #loadLdapServerAdapterExtensions()} fills it.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the singleton instance — called only once via {@link #getDefault()}.
     * We don't load any adapters here; the loading happens explicitly in
     * {@link #loadLdapServerAdapterExtensions()} so startup order stays predictable.
     *
     * <p>For example — C-3PO initializes his circuits:</p>
     * <pre>
     *   C-3PO: "Language banks initialized. Ready to receive modules."
     *   Lists: empty. Maps: empty. Awaiting loadLdapServerAdapterExtensions().
     * </pre>
     */
    private LdapServerAdapterExtensionsManager()
    {
    }


    // ── C-3PO Reads Every Language Data Card ────────────────────────────────────────────────
    // At the start of the mission, C-3PO slots in all available language data cards — one per
    // species — reading the ID, name, and capabilities from each card.
    // We read every {@code ldapServerAdapters} extension from the Eclipse registry and build
    // a list and map so lookups are O(1) by adapter ID.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Scans Eclipse's extension registry for all contributed LDAP server adapter extensions
     * and populates our internal list and map.
     * This must be called once during plugin startup (see {@link LdapServersPlugin#start}).
     * After this, callers can ask for adapters by ID instantly via {@link #getLdapServerAdapterExtensionById}.
     *
     * <p>For example — C-3PO slots in language cards:</p>
     * <pre>
     *   Card 1: ApacheDS 2.x adapter — id, name, version, vendor, class loaded.
     *   Card 2: OpenLDAP adapter — same fields, different values.
     *   All cards indexed by ID for instant lookup later.
     * </pre>
     */
    public void loadLdapServerAdapterExtensions()
    {
        // Initializing the list and map for LDAP Server Adapter Extensions
        ldapServerAdapterExtensionsList = new ArrayList<LdapServerAdapterExtension>();
        ldapServerAdapterExtensionsByIdMap = new HashMap<String, LdapServerAdapterExtension>();

        // Getting members of LDAP Server Adapters Extension Point
        IConfigurationElement[] members = Platform.getExtensionRegistry()
            .getExtensionPoint( LdapServersPluginConstants.LDAP_SERVER_ADAPTERS_EXTENSION_POINT )
            .getConfigurationElements();

        // Creating an object associated with each member
        for ( IConfigurationElement member : members )
        {
            // Creating the LdapServerAdapterExtension object container
            LdapServerAdapterExtension ldapServerAdapterExtension = new LdapServerAdapterExtension();

            // Getting the ID of the extending plugin
            String extendingPluginId = member.getDeclaringExtension().getNamespaceIdentifier();

            // Setting each parameter to the LDAP Server Adapter Extension
            ldapServerAdapterExtension.setExtensionPointConfiguration( member );
            ldapServerAdapterExtension.setId( member.getAttribute( ID_ATTR ) );
            ldapServerAdapterExtension.setName( member.getAttribute( NAME_ATTR ) );
            ldapServerAdapterExtension.setVersion( member.getAttribute( VERSION_ATTR ) );
            ldapServerAdapterExtension.setVendor( member.getAttribute( VENDOR_ATTR ) );
            ldapServerAdapterExtension.setClassName( member.getAttribute( CLASS_ATTR ) );
            try
            {
                ldapServerAdapterExtension.setInstance( ( LdapServerAdapter ) member
                    .createExecutableExtension( CLASS_ATTR ) );
            }
            catch ( CoreException e )
            {
                // Will never happen
            }
            ldapServerAdapterExtension.setDescription( member.getAttribute( DESCRIPTION_ATTR ) );
            String iconPath = member.getAttribute( ICON_ATTR );
            if ( iconPath != null )
            {
                ImageDescriptor icon = AbstractUIPlugin.imageDescriptorFromPlugin( extendingPluginId, iconPath );
                if ( icon == null )
                {
                    icon = ImageDescriptor.getMissingImageDescriptor();
                }
                ldapServerAdapterExtension.setIcon( icon );
            }
            ldapServerAdapterExtension.setConfigurationPageClassName( member.getAttribute( CONFIGURATION_PAGE_ATTR ) );
            String openConfigurationActionEnabled = member.getAttribute( OPEN_CONFIGURATION_ACTION_ENABLED_ATTR );
            if ( openConfigurationActionEnabled != null )
            {
                ldapServerAdapterExtension.setOpenConfigurationActionEnabled( Boolean
                    .parseBoolean( openConfigurationActionEnabled ) );
            }
            else
            {
                // Enabled by default
                ldapServerAdapterExtension.setOpenConfigurationActionEnabled( true );
            }

            ldapServerAdapterExtensionsList.add( ldapServerAdapterExtension );
            ldapServerAdapterExtensionsByIdMap.put( ldapServerAdapterExtension.getId(), ldapServerAdapterExtension );
        }
    }


    // ── Finding The Same Translator Desk ────────────────────────────────────────────────────
    // The Rebellion has one shared translation desk — every officer who needs a translator goes
    // to the same desk, not a new one each time.
    // We use the singleton pattern to ensure one shared manager across the whole application.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the singleton {@link LdapServerAdapterExtensionsManager}, creating it on first call.
     * Sharing one instance means every part of the app sees the same loaded adapter registry.
     *
     * <p>For example — C-3PO's translation desk is shared:</p>
     * <pre>
     *   Component A asks: "Give me the adapter manager." → gets the singleton.
     *   Component B asks: same question → same object, not a new one.
     * </pre>
     *
     * @return the single shared {@link LdapServerAdapterExtensionsManager}
     */
    public static LdapServerAdapterExtensionsManager getDefault()
    {
        if ( instance == null )
        {
            instance = new LdapServerAdapterExtensionsManager();
        }

        return instance;
    }


    // ── C-3PO Recites The Full Language Roster ──────────────────────────────────────────────
    // When asked "how many languages do you speak?", C-3PO lists them all in sequence.
    // We return the ordered list of every adapter extension that was loaded at startup.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the complete list of all loaded LDAP server adapter extensions.
     * Useful for populating the "new server" wizard's selection list — every adapter
     * in this list represents a supported server type.
     *
     * <p>For example — C-3PO lists his languages:</p>
     * <pre>
     *   "I am fluent in: ApacheDS 2.x, OpenLDAP, 389-DS... and many more."
     *   Caller iterates the list to build the UI.
     * </pre>
     *
     * @return the ordered list of {@link LdapServerAdapterExtension} objects; never null after
     *         {@link #loadLdapServerAdapterExtensions()} has been called
     */
    public List<LdapServerAdapterExtension> getLdapServerAdapterExtensions()
    {
        return ldapServerAdapterExtensionsList;
    }


    // ── C-3PO Locates A Specific Language Module ────────────────────────────────────────────
    // An officer hands C-3PO a language code — "Bocce, dialect 7" — and C-3PO pulls that
    // exact module from his indexed language bank.
    // We do a direct map lookup by adapter ID, returning the matching extension or null.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Finds and returns the adapter extension registered under the given ID.
     * This is used when we need to associate a persisted server (which stores its adapter ID
     * in the XML file) with the live adapter object at load time.
     *
     * <p>For example — C-3PO retrieves a language by code:</p>
     * <pre>
     *   Code: "org.apache.directory.studio.apacheds.configuration.2"
     *   C-3PO checks his index → returns the ApacheDS 2.x adapter module.
     *   Unknown code → returns null (adapter plugin might not be installed).
     * </pre>
     *
     * @param id  the unique adapter extension ID, as declared in the contributing plugin's plugin.xml
     * @return the matching {@link LdapServerAdapterExtension}, or {@code null} if not found
     */
    public LdapServerAdapterExtension getLdapServerAdapterExtensionById( String id )
    {
        return ldapServerAdapterExtensionsByIdMap.get( id );
    }
}
