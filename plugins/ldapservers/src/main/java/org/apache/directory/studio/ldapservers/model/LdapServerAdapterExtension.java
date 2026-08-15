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

package org.apache.directory.studio.ldapservers.model;


import org.apache.directory.studio.ldapservers.LdapServerAdapterExtensionsManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: LdapServerAdapterExtension — C-3PO'S LANGUAGE MODULE DATA CARD ────────────────
// Each of C-3PO's language modules is a self-contained data card: it carries the language's
// name, version, vendor, the actual translation class, an icon, and a configuration page
// reference so the engineering team can customize how that language is used.
// This class is exactly that: a container for everything declared about one adapter extension
// in a third-party plugin's plugin.xml.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Holds all the metadata and runtime instance for one LDAP server adapter extension,
 * as loaded from Eclipse's extension registry by {@link LdapServerAdapterExtensionsManager}.
 * Each contributing plugin declares an extension with an ID, name, version, vendor, adapter class,
 * optional icon, and optional configuration-page class — all of which land here.
 * Think of this as C-3PO's data card for a specific language module.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapServerAdapterExtension
{
    /** The extension point configuration */
    private IConfigurationElement extensionPointConfiguration;

    /** The ID */
    private String id;

    /** The name*/
    private String name;

    /** The version */
    private String version;

    /** The vendor */
    private String vendor;

    /** The class name */
    private String className;

    /** The {@link LdapServerAdapter} instance */
    private LdapServerAdapter instance;

    /** The description */
    private String description;

    /** The icon */
    private ImageDescriptor icon;

    /** The configuration page class name */
    private String configurationPageClassName;

    /** The flag to enable the open configuration action */
    private boolean openConfigurationActionEnabled;


    // ── Reading The Class Name Off The Data Card ─────────────────────────────────────────────
    // The data card lists the adapter class — "com.vendor.MyLdapServerAdapter" — so we know
    // which Java class to instantiate when we need to start or stop a server.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the fully qualified class name of the {@link LdapServerAdapter} implementation.
     *
     * @return the adapter class name as declared in plugin.xml
     */
    public String getClassName()
    {
        return className;
    }


    // ── Reading The Configuration Page Class Name ────────────────────────────────────────────
    // The data card also lists the optional configuration-page class — the wizard tab that lets
    // users set adapter-specific options like ports and installation paths.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the fully qualified class name of the optional {@link LdapServerAdapterConfigurationPage}
     * implementation, or {@code null} if this adapter has no custom configuration page.
     *
     * @return the configuration page class name, or {@code null}
     */
    public String getConfigurationPageClassName()
    {
        return configurationPageClassName;
    }


    // ── Reading The Module's Description ────────────────────────────────────────────────────
    // The data card includes a short human-readable description of what this language module
    // (adapter) covers.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable description of this adapter extension.
     *
     * @return the description string
     */
    public String getDescription()
    {
        return description;
    }


    // ── Accessing The Raw Plugin.xml Configuration Element ───────────────────────────────────
    // The raw IConfigurationElement is the actual Eclipse registry entry — needed when we want
    // to instantiate the configuration page class via createExecutableExtension().
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw Eclipse {@link IConfigurationElement} this extension was loaded from.
     * Needed when instantiating the configuration-page class via
     * {@link #getNewConfigurationPageInstance()}.
     *
     * @return the extension point configuration element
     */
    public IConfigurationElement getExtensionPointConfiguration()
    {
        return extensionPointConfiguration;
    }


    // ── Reading The Module's Icon ────────────────────────────────────────────────────────────
    // The data card has a visual icon so the wizard can show a recognizable image next to each
    // adapter type in the selection list.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ImageDescriptor} for this adapter's icon, or {@code null} if none.
     * Displayed in the "New Server" wizard's selection list.
     *
     * @return the icon descriptor, or {@code null}
     */
    public ImageDescriptor getIcon()
    {
        return icon;
    }


    // ── Reading The Module's Unique ID ───────────────────────────────────────────────────────
    // The data card's serial number — unique across all installed adapters.
    // Used as the primary key in {@link LdapServerAdapterExtensionsManager}'s map.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns this extension's unique identifier, as declared in plugin.xml.
     *
     * @return the adapter extension ID
     */
    public String getId()
    {
        return id;
    }


    // ── Retrieving The Live Adapter Instance ─────────────────────────────────────────────────
    // The data card references the live translation object — the actual C-3PO module you call
    // to start/stop/configure the server.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the live {@link LdapServerAdapter} instance created from this extension.
     * This is what the framework calls to start, stop, delete, and open-configuration a server.
     *
     * @return the adapter instance, or {@code null} if instantiation failed
     */
    public LdapServerAdapter getInstance()
    {
        return instance;
    }


    // ── Reading The Module's Display Name ────────────────────────────────────────────────────
    // The data card has a human-readable name — "ApacheDS 2.x" — shown in the wizard list.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable display name of this adapter extension.
     *
     * @return the name string
     */
    public String getName()
    {
        return name;
    }


    // ── Instantiating A Fresh Configuration Page ─────────────────────────────────────────────
    // When the wizard needs a configuration page for this adapter, it asks C-3PO to produce
    // a fresh one — using the Eclipse extension mechanism to create it from the declared class.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and returns a new instance of this adapter's configuration page.
     * Uses Eclipse's {@code createExecutableExtension} mechanism so the configuration-page class
     * is loaded from the contributing plugin's classloader, not ours.
     * Returns {@code null} if no configuration page class is declared or instantiation fails.
     *
     * @return a fresh {@link LdapServerAdapterConfigurationPage}, or {@code null}
     */
    public LdapServerAdapterConfigurationPage getNewConfigurationPageInstance()
    {
        try
        {
            return ( LdapServerAdapterConfigurationPage ) extensionPointConfiguration
                .createExecutableExtension( LdapServerAdapterExtensionsManager.CONFIGURATION_PAGE_ATTR );
        }
        catch ( CoreException e )
        {
            return null;
        }
    }


    // ── Reading The Module's Vendor ──────────────────────────────────────────────────────────
    // The data card lists who produced this language module — "Apache Software Foundation".
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the vendor string declared for this adapter extension.
     *
     * @return the vendor name
     */
    public String getVendor()
    {
        return vendor;
    }


    // ── Reading The Module's Version ─────────────────────────────────────────────────────────
    // The data card notes the version — "2.0.0" — so users can distinguish between adapters
    // for different server generations.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the version string declared for this adapter extension.
     *
     * @return the version string
     */
    public String getVersion()
    {
        return version;
    }


    // ── Checking Whether The Config Action Is Available ──────────────────────────────────────
    // Some server types don't have a configuration editor — this flag controls whether the
    // "Open Configuration" toolbar button is visible for servers using this adapter.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the "Open Configuration" action should be shown for servers using this adapter.
     * Adapters that don't support a configuration editor declare this as {@code false} in plugin.xml.
     *
     * @return {@code true} if the action is enabled (the default); {@code false} to hide it
     */
    public boolean isOpenConfigurationActionEnabled()
    {
        return openConfigurationActionEnabled;
    }


    // Setters — standard JavaBean setters used by LdapServerAdapterExtensionsManager during loading.

    /**
     * Sets the adapter class name.
     * @param className  the fully qualified class name of the adapter implementation
     */
    public void setClassName( String className )
    {
        this.className = className;
    }

    /**
     * Sets the configuration page class name.
     * @param configurationPageClassName  the fully qualified class name of the config page
     */
    public void setConfigurationPageClassName( String configurationPageClassName )
    {
        this.configurationPageClassName = configurationPageClassName;
    }

    /**
     * Sets the description.
     * @param description  the human-readable description of this adapter
     */
    public void setDescription( String description )
    {
        this.description = description;
    }

    /**
     * Sets the raw extension point configuration element.
     * @param extensionPointConfiguration  the Eclipse IConfigurationElement from the registry
     */
    public void setExtensionPointConfiguration( IConfigurationElement extensionPointConfiguration )
    {
        this.extensionPointConfiguration = extensionPointConfiguration;
    }

    /**
     * Sets the icon.
     * @param icon  the ImageDescriptor for this adapter's icon
     */
    public void setIcon( ImageDescriptor icon )
    {
        this.icon = icon;
    }

    /**
     * Sets the unique ID.
     * @param id  the adapter extension ID as declared in plugin.xml
     */
    public void setId( String id )
    {
        this.id = id;
    }

    /**
     * Sets the live adapter instance.
     * @param instance  the instantiated LdapServerAdapter
     */
    public void setInstance( LdapServerAdapter instance )
    {
        this.instance = instance;
    }

    /**
     * Sets the display name.
     * @param name  the human-readable name for this adapter
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * Sets the open-configuration-action flag.
     * @param openConfigurationActionEnabled  true to show the action; false to hide it
     */
    public void setOpenConfigurationActionEnabled( boolean openConfigurationActionEnabled )
    {
        this.openConfigurationActionEnabled = openConfigurationActionEnabled;
    }

    /**
     * Sets the vendor name.
     * @param vendor  the vendor string for this adapter
     */
    public void setVendor( String vendor )
    {
        this.vendor = vendor;
    }

    /**
     * Sets the version string.
     * @param version  the version declared in plugin.xml
     */
    public void setVersion( String version )
    {
        this.version = version;
    }
}
