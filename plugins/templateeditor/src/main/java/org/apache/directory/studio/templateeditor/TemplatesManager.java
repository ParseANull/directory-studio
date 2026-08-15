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
package org.apache.directory.studio.templateeditor;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;

import org.apache.directory.studio.templateeditor.model.ExtensionPointTemplate;
import org.apache.directory.studio.templateeditor.model.FileTemplate;
import org.apache.directory.studio.templateeditor.model.Template;
import org.apache.directory.studio.templateeditor.model.parser.TemplateIO;
import org.apache.directory.studio.templateeditor.model.parser.TemplateIOException;


// ── CLASS: TemplatesManager — MON MOTHMA MANAGING THE REBEL FLEET ────────────────
// Mon Mothma sits at the war table on Yavin 4, tracking every starfighter in the
// Rebel fleet: which squadrons are active, which are stood down, which pilot is the
// default leader for each type of mission. When a new ship arrives, she registers
// it. When one is lost, she removes it. When a commander asks "who should handle
// this target?", she consults her roster and returns the best match. This class does
// exactly that for templates: it loads them from Eclipse extension points and from
// XML files on disk, tracks which are enabled or disabled, records the default
// template per object class, and hands back the right template when the editor asks.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Central registry and lifecycle manager for all {@link Template}s in the plugin.
 * Loads templates from two sources at startup:
 * <ol>
 *   <li><em>Extension points</em> — templates contributed by other Eclipse plugins
 *       via {@code org.apache.directory.studio.templateeditor.templates}</li>
 *   <li><em>File templates</em> — XML files the user has imported, stored in the
 *       plugin's state directory under {@code templates/}</li>
 * </ol>
 * Tracks which templates are enabled/disabled (persisted in the preference store)
 * and which template is the "default" for each LDAP structural object class.
 * Fires {@link TemplatesManagerListener} events whenever the registry changes.
 * Think of this class as Mon Mothma managing the Rebel fleet's roster.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplatesManager
{
    /** The preference delimiter used for default and disabled templates */
    private static String PREFERENCE_DELIMITER = ";"; //$NON-NLS-1$

    /** The preference sub delimiter used for default templates */
    private static String PREFERENCE_SUB_DELIMITER = ":"; //$NON-NLS-1$

    /** The plugin's preference store */
    private IPreferenceStore preferenceStore;

    /** The list containing all the templates */
    private List<Template> templatesList = new ArrayList<Template>();

    /** The maps containing all the templates by their id */
    private Map<String, Template> templatesByIdMap = new HashMap<String, Template>();

    /** The maps containing all the templates by ObjectClassDescription */
    private MultiValuedMap<ObjectClass, Template> templatesByStructuralObjectClassMap = new ArrayListValuedHashMap<>();

    /** The list containing *only* the IDs of the disabled templates */
    private List<String> disabledTemplatesList = new ArrayList<String>();

    /** The map containing the default templates */
    private Map<ObjectClass, String> defaultTemplatesMap = new HashMap<ObjectClass, String>();

    /** The list of listeners */
    private List<TemplatesManagerListener> listeners = new ArrayList<TemplatesManagerListener>();


    // ── CONSTRUCTOR: MON MOTHMA CALLS THE FLEET TO ORDER ─────────────────────────
    // Mon Mothma opens the war council: first she reads out the disabled and default
    // squadrons from the standing orders, then she takes roll call of every ship
    // available (from extension points and from disk), and finally she assigns the
    // default pilots. By the time the constructor returns the registry is fully
    // populated and ready.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Initializes the manager by loading everything from the preference store and
     * the filesystem. After this constructor returns the registry is fully populated.
     *
     * <p>For example — Mon Mothma opens the war council:</p>
     * <pre>
     *   loadDefaultTemplates();   // "Who's the standing default for each mission type?"
     *   loadDisabledTemplates();  // "Which squadrons are currently stood down?"
     *   loadTemplates();          // "Take roll call — extension points then disk files."
     *   setDefaultTemplates();    // "Assign defaults for any class that has none."
     * </pre>
     *
     * @param preferenceStore  the Eclipse preference store used to persist disabled
     *                         and default template selections across sessions
     */
    public TemplatesManager( IPreferenceStore preferenceStore )
    {
        this.preferenceStore = preferenceStore;

        loadDefaultTemplates();
        loadDisabledTemplates();
        loadTemplates();
        setDefaultTemplates();
    }


    // ── ADD LISTENER: MON MOTHMA ADDS AN OBSERVER TO THE WAR COUNCIL ─────────────
    // A new commander takes a seat at the war table. From now on, every status
    // update Mon Mothma announces goes to them too. Listeners fire immediately
    // whenever the registry changes — no polling, no delay.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Registers a {@link TemplatesManagerListener} to receive lifecycle events
     * (template added, removed, enabled, disabled). The listener is added to an
     * internal list and called on every subsequent change. Safe to call multiple
     * times — adding the same listener twice means it fires twice.
     *
     * <p>For example — a new commander joins the war council:</p>
     * <pre>
     *   manager.addListener(myPreferencePageUI);
     *   // Now myPreferencePageUI.templateAdded(...) fires whenever a template arrives.
     * </pre>
     *
     * @param listener  the observer to register; must not be {@code null}
     * @return {@code true} as per {@link java.util.Collection#add(Object)}
     */
    public boolean addListener( TemplatesManagerListener listener )
    {
        return listeners.add( listener );
    }


    // ── REMOVE LISTENER: MON MOTHMA DISMISSES A COMMANDER FROM THE COUNCIL ───────
    // A commander leaves the war table — they no longer need status updates. We
    // remove them from the broadcast list so we don't waste cycles calling them.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Unregisters a previously added {@link TemplatesManagerListener}. After this
     * call the listener will no longer receive any change events.
     *
     * <p>For example — a commander is dismissed from the council:</p>
     * <pre>
     *   manager.removeListener(myPreferencePageUI);
     * </pre>
     *
     * @param listener  the observer to remove
     * @return {@code true} if the listener was found and removed
     */
    public boolean removeListener( TemplatesManagerListener listener )
    {
        return listeners.remove( listener );
    }


    // ── FIRE TEMPLATE ADDED: MON MOTHMA ANNOUNCES A NEW ARRIVAL ─────────────────
    // Mon Mothma stands up and says "A new X-Wing has joined the fleet." Every
    // commander at the table hears it. We snapshot the listener list first (with
    // toArray) so late additions or removals during the callback don't corrupt
    // the iteration.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Broadcasts a "template added" event to all registered listeners.
     * The listener list is snapshotted before iteration so concurrent modification
     * (e.g. a listener removing itself) is safe.
     *
     * @param template  the template that was just added
     */
    private void fireTemplateAdded( Template template )
    {
        for ( TemplatesManagerListener listener : listeners.toArray( new TemplatesManagerListener[0] ) )
        {
            listener.templateAdded( template );
        }
    }


    // ── FIRE TEMPLATE REMOVED: MON MOTHMA ANNOUNCES A LOSS ───────────────────────
    // Mon Mothma solemnly strikes a name from the roster and informs the council.
    // Same snapshot-iteration pattern as fireTemplateAdded.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Broadcasts a "template removed" event to all registered listeners.
     *
     * @param template  the template that was just removed
     */
    private void fireTemplateRemoved( Template template )
    {
        for ( TemplatesManagerListener listener : listeners.toArray( new TemplatesManagerListener[0] ) )
        {
            listener.templateRemoved( template );
        }
    }


    // ── FIRE TEMPLATE ENABLED: MON MOTHMA CLEARS A SQUADRON FOR DUTY ─────────────
    // "Gold Squadron, you are cleared for active duty." The council updates their
    // boards. Same safe iteration pattern.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Broadcasts a "template enabled" event to all registered listeners.
     *
     * @param template  the template that was just enabled
     */
    private void fireTemplateEnabled( Template template )
    {
        for ( TemplatesManagerListener listener : listeners.toArray( new TemplatesManagerListener[0] ) )
        {
            listener.templateEnabled( template );
        }
    }


    // ── FIRE TEMPLATE DISABLED: MON MOTHMA STANDS DOWN A SQUADRON ────────────────
    // "Blue Squadron, stand down." The council marks them inactive. Same pattern.
    // ────────────────────────────────────────────────────────────────────────────
    /**
    * Broadcasts a "template disabled" event to all registered listeners.
    *
    * @param template  the disabled template
    */
    private void fireTemplateDisabled( Template template )
    {
        for ( TemplatesManagerListener listener : listeners.toArray( new TemplatesManagerListener[0] ) )
        {
            listener.templateDisabled( template );
        }
    }


    // ── LOAD TEMPLATES: MON MOTHMA CALLS ROLL ACROSS ALL SOURCES ─────────────────
    // Mon Mothma first checks the Alliance's formal registry (extension points —
    // ships contributed by allied fleets) and then checks the local hangar (XML
    // files the user has manually imported). Both sources populate the same roster.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Orchestrates loading from both template sources: extension-point contributions
     * from other Eclipse plugins, then user-imported XML files from disk.
     */
    private void loadTemplates()
    {
        // Loading the templates added using the extension point
        loadExtensionPointTemplates();

        // Loading the templates added via files on the disk (added by the user)
        loadFileTemplates();
    }


    // ── LOAD EXTENSION POINT TEMPLATES: CHECK THE ALLIANCE FORMAL REGISTRY ───────
    // Mon Mothma contacts allied fleets through the formal Alliance channel and
    // registers every ship they've contributed. Each extension-point element
    // is a different ally's contribution — we parse it and add it to the roster.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Scans the {@code org.apache.directory.studio.templateeditor.templates}
     * Eclipse extension point and loads every contributed template XML. Each
     * successfully parsed template is added to all three internal data structures
     * (list, by-id map, by-object-class map). Parsing errors are logged but do not
     * abort the load of subsequent templates.
     */
    private void loadExtensionPointTemplates()
    {
        // Getting the extension point
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(
            "org.apache.directory.studio.templateeditor.templates" ); //$NON-NLS-1$

        // Getting all the extensions
        IConfigurationElement[] members = extensionPoint.getConfigurationElements();
        if ( members != null )
        {
            // For each extension: load the template
            for ( int m = 0; m < members.length; m++ )
            {
                IConfigurationElement member = members[m];

                // Getting the URL of the file associated with the extension
                String contributorName = member.getContributor().getName();
                String filePathInPlugin = member.getAttribute( "file" ); //$NON-NLS-1$
                URL fileUrl = Platform.getBundle( contributorName ).getResource( filePathInPlugin );

                // Checking if the URL is null
                if ( filePathInPlugin == null )
                {
                    // Logging the error
                    EntryTemplatePluginUtils.logError( new NullPointerException(), Messages
                        .getString( "TemplatesManager.AnErrorOccurredWhenParsingTheTemplate3Params" ), contributorName, //$NON-NLS-1$
                        filePathInPlugin, Messages.getString( "TemplatesManager.URLCreatedForTheTemplateIsNull" ) ); //$NON-NLS-1$
                }

                // Parsing the template and adding it to the templates list
                try
                {
                    InputStream is = fileUrl.openStream();

                    ExtensionPointTemplate template = TemplateIO.readAsExtensionPointTemplate( is );

                    templatesList.add( template );
                    templatesByIdMap.put( template.getId(), template );
                    templatesByStructuralObjectClassMap.put( EntryTemplatePluginUtils
                        .getObjectClassDescriptionFromDefaultSchema( template.getStructuralObjectClass() ), template );

                    is.close();
                }
                catch ( TemplateIOException e )
                {
                    // Logging the error
                    EntryTemplatePluginUtils.logError( e, Messages
                        .getString( "TemplatesManager.AnErrorOccurredWhenParsingTheTemplate3Params" ), //$NON-NLS-1$
                        contributorName, filePathInPlugin, e.getMessage() );
                }
                catch ( IOException e )
                {
                    // Logging the error
                    EntryTemplatePluginUtils.logError( e, Messages
                        .getString( "TemplatesManager.AnErrorOccurredWhenParsingTheTemplate3Params" ), contributorName, //$NON-NLS-1$
                        filePathInPlugin, e.getMessage() );
                }
            }
        }
    }


    // ── LOAD FILE TEMPLATES: CHECK THE LOCAL HANGAR ───────────────────────────────
    // Mon Mothma walks through the local hangar bay and registers every XML-file
    // ship the user has manually delivered. If the hangar folder doesn't exist
    // or is empty, she just moves on — nothing to do.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Scans the plugin's state-location {@code templates/} folder for {@code *.xml}
     * files and parses each one as a {@link FileTemplate}. Successfully loaded
     * templates are added to all three internal data structures. Parsing errors are
     * logged but do not abort loading of subsequent files.
     */
    private void loadFileTemplates()
    {
        // Getting the templates folder
        File templatesFolder = getTemplatesFolder().toFile();

        // If the templates folder does not exist, we exit
        if ( !templatesFolder.exists() )
        {
            return;
        }

        // Loading the templates contained in the templates folder
        String[] templateNames = templatesFolder.list( new FilenameFilter()
        {
            public boolean accept( File dir, String name )
            {
                return name.endsWith( ".xml" ); //$NON-NLS-1$
            }
        } );

        // If there are no templates available, we exit
        if ( ( templateNames == null ) || ( templateNames.length == 0 ) )
        {
            return;
        }

        // Loading each template
        for ( String templateName : templateNames )
        {
            // Creating the template file
            File templateFile = new File( templatesFolder, templateName );

            // Parsing the template and adding it to the templates list
            try
            {
                InputStream is = new FileInputStream( templateFile );

                FileTemplate template = TemplateIO.readAsFileTemplate( is );
                templatesList.add( template );
                templatesByIdMap.put( template.getId(), template );
                templatesByStructuralObjectClassMap.put( EntryTemplatePluginUtils
                    .getObjectClassDescriptionFromDefaultSchema( template.getStructuralObjectClass() ), template );

                is.close();
            }
            catch ( TemplateIOException e )
            {
                // Logging the error
                EntryTemplatePluginUtils.logError( e, Messages
                    .getString( "TemplatesManager.AnErrorOccurredWhenParsingTheTemplate2Params" ), //$NON-NLS-1$
                    templateFile.getAbsolutePath(), e.getMessage() );
            }
            catch ( IOException e )
            {
                // Logging the error
                EntryTemplatePluginUtils.logError( e, Messages
                    .getString( "TemplatesManager.AnErrorOccurredWhenParsingTheTemplate2Params" ), //$NON-NLS-1$
                    templateFile.getAbsolutePath(), e.getMessage() );
            }
        }
    }


    // ── ADD TEMPLATE: MON MOTHMA REGISTERS A NEW SHIP IN THE FLEET ───────────────
    // A Rebel operative delivers a new starfighter by hand — Mon Mothma verifies
    // the paperwork (is the file valid?), checks that no ship with that ID already
    // exists, copies the ship to the official hangar, and adds it to the roster.
    // If anything goes wrong she logs it and returns false.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the given file as a {@link FileTemplate}, validates it, copies it into
     * the plugin's managed {@code templates/} folder, and registers it in all
     * internal data structures. Fires a "template added" event on success.
     *
     * <p>For example — Mon Mothma registers a hand-delivered starfighter:</p>
     * <pre>
     *   boolean ok = manager.addTemplate(new File("/tmp/UserAccount.xml"));
     *   // If ok==true the template is now in the registry and ready to use.
     * </pre>
     *
     * @param templateFile  the XML template file to import; must be readable
     * @return {@code true} if the template was successfully added,
     *         {@code false} if validation, ID conflict, copy, or registration failed
     */
    public boolean addTemplate( File templateFile )
    {
        // Getting the file template
        FileTemplate fileTemplate = getFileTemplate( templateFile );
        if ( fileTemplate == null )
        {
            // If the file is not valid, we simply return
            return false;
        }

        // Verifying if a template with a similar ID does not already exist
        if ( templatesByIdMap.containsKey( fileTemplate.getId() ) )
        {
            // Logging the error
            EntryTemplatePluginUtils.logError( null, Messages
                .getString( "TemplatesManager.TheTemplateFileCouldNotBeAddedBecauseATemplateWithSameIDAlreadyExist" ), //$NON-NLS-1$
                templateFile.getAbsolutePath() );
            return false;
        }

        // Verifying the folder containing the templates already exists
        // If not we create it
        File templatesFolder = getTemplatesFolder().toFile();
        if ( !templatesFolder.exists() )
        {
            // The folder does not exist, we need to create it.
            templatesFolder.mkdirs();
        }

        // Copying the template in the plugin's folder
        try
        {
            // Creating the file object where the template will be saved
            File destinationFile = getTemplatesFolder().append( fileTemplate.getId() + ".xml" ).toFile(); //$NON-NLS-1$

            // Checking if the file does not already exist
            if ( destinationFile.exists() )
            {
                // Logging the error
                EntryTemplatePluginUtils
                    .logError(
                        null,
                        Messages
                            .getString( "TemplatesManager.TheTemplateFileCouldNotBeAddedBecauseATemplateWithSameIDAlreadyExist" ), //$NON-NLS-1$
                        templateFile.getAbsolutePath() );
                return false;
            }

            // Copying the file
            EntryTemplatePluginUtils.copyFile( templateFile, destinationFile );
        }
        catch ( IOException e )
        {
            // Logging the error
            EntryTemplatePluginUtils
                .logError(
                    null,
                    Messages.getString( "TemplatesManager.TheTemplateFileCouldNotBeCopiedToThePluginsFolder" ), templateFile.getAbsolutePath() ); //$NON-NLS-1$
            return false;
        }

        // Adding the template
        templatesList.add( fileTemplate );
        templatesByIdMap.put( fileTemplate.getId(), fileTemplate );
        templatesByStructuralObjectClassMap.put( EntryTemplatePluginUtils
            .getObjectClassDescriptionFromDefaultSchema( fileTemplate.getStructuralObjectClass() ), fileTemplate );

        // Firing the event
        fireTemplateAdded( fileTemplate );

        return true;
    }


    // ── GET FILE TEMPLATE: MON MOTHMA INSPECTS THE INCOMING SHIP ─────────────────
    // Before registering a new ship, Mon Mothma's crew checks: does it physically
    // exist? Can we open the hatch? Is the pilot's license valid? Only once those
    // three checks pass do we consider it legitimate. This method does the same
    // three-step validation before parsing the XML.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Validates and parses a template XML file. Checks existence, readability,
     * and XML validity in that order. Returns {@code null} (and logs an error)
     * at the first check that fails.
     *
     * <p>For example — Mon Mothma's crew inspects the incoming ship:</p>
     * <pre>
     *   // 1. Does the file exist?  If not → log and return null.
     *   // 2. Can we read it?       If not → log and return null.
     *   // 3. Is the XML valid?     If not → log and return null.
     *   // All pass → return the parsed FileTemplate.
     * </pre>
     *
     * @param templateFile  the file to inspect; never {@code null}
     * @return the parsed {@link FileTemplate}, or {@code null} if any check fails
     */
    private FileTemplate getFileTemplate( File templateFile )
    {
        // Checking if the file exists
        if ( !templateFile.exists() )
        {
            // Logging the error
            EntryTemplatePluginUtils.logError( null, Messages
                .getString( "TemplatesManager.TheTemplateFileCouldNotBeAddedBecauseItDoesNotExist" ), templateFile //$NON-NLS-1$
                .getAbsolutePath() );
            return null;
        }

        // Checking if the file is readable
        if ( !templateFile.canRead() )
        {
            // Logging the error
            EntryTemplatePluginUtils
                .logError(
                    null,
                    Messages.getString( "TemplatesManager.TheTemplateFileCouldNotBeAddedBecauseItCantBeRead" ), templateFile.getAbsolutePath() ); //$NON-NLS-1$
            return null;
        }

        // Trying to parse the template file
        FileTemplate fileTemplate = null;
        try
        {
            FileInputStream fis = new FileInputStream( templateFile );
            fileTemplate = TemplateIO.readAsFileTemplate( fis );
        }
        catch ( FileNotFoundException e )
        {
            // Logging the error
            EntryTemplatePluginUtils.logError( e, Messages
                .getString( "TemplatesManager.TheTemplateFileCouldNotBeAddedBecauseOfTheFollowingError" ), templateFile //$NON-NLS-1$
                .getAbsolutePath(), e.getMessage() );
            return null;
        }
        catch ( TemplateIOException e )
        {
            // Logging the error
            EntryTemplatePluginUtils.logError( e, Messages
                .getString( "TemplatesManager.TheTemplateFileCouldNotBeAddedBecauseOfTheFollowingError" ), templateFile //$NON-NLS-1$
                .getAbsolutePath(), e.getMessage() );
            return null;
        }

        // Everything went fine, the file is valid
        return fileTemplate;
    }


    // ── REMOVE TEMPLATE: MON MOTHMA DECOMMISSIONS A SHIP ─────────────────────────
    // Mon Mothma receives the order to decommission a ship: she checks it exists on
    // the roster, locates its physical file, verifies the file can be deleted, deletes
    // it, then strikes it from all records and notifies the council. Any check that
    // fails logs an error and stops the operation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a user-imported {@link FileTemplate} from the registry and deletes
     * its XML file from the plugin's managed folder. Validates existence and
     * writability before deletion. Fires a "template removed" event on success.
     *
     * <p>For example — Mon Mothma decommissions a ship:</p>
     * <pre>
     *   boolean ok = manager.removeTemplate(userTemplate);
     *   // If ok==true the template is gone from the registry and the file is deleted.
     * </pre>
     *
     * @param fileTemplate  the template to remove; must not be {@code null}
     * @return {@code true} if removed successfully, {@code false} on any error
     */
    public boolean removeTemplate( FileTemplate fileTemplate )
    {
        // Checking if the file template is null
        if ( fileTemplate == null )
        {
            return false;
        }

        // Checking if the file template exists in the templates set
        if ( !templatesList.contains( fileTemplate ) )
        {
            // Logging the error
            EntryTemplatePluginUtils
                .logError(
                    null,
                    Messages.getString( "TemplatesManager.TheTemplateCouldNotBeRemovedBecauseOfTheFollowingError" ) //$NON-NLS-1$
                        + Messages.getString( "TemplatesManager.TheTemplateDoesNotExistInTheTemplateManager" ), fileTemplate.getTitle(), fileTemplate //$NON-NLS-1$
                        .getId() );
            return false;
        }

        // Creating the file object associated with the template
        File templateFile = getTemplatesFolder().append( fileTemplate.getId() + ".xml" ).toFile(); //$NON-NLS-1$

        // Checking if the file exists
        if ( !templateFile.exists() )
        {
            // Logging the error
            EntryTemplatePluginUtils
                .logError(
                    null,
                    Messages.getString( "TemplatesManager.TheTemplateCouldNotBeRemovedBecauseOfTheFollowingError" ) //$NON-NLS-1$
                        + Messages.getString( "TemplatesManager.TheFileAssociatedWithTheTemplateCouldNotBeFoundAt" ), fileTemplate.getTitle(), //$NON-NLS-1$
                    fileTemplate.getId(), templateFile.getAbsolutePath() );
            return false;
        }

        // Checking if the file can be written, and thus deleted
        if ( !templateFile.canWrite() )
        {
            // Logging the error
            EntryTemplatePluginUtils
                .logError(
                    null,
                    Messages.getString( "TemplatesManager.TheTemplateCouldNotBeRemovedBecauseOfTheFollowingError" ) //$NON-NLS-1$
                        + Messages.getString( "TemplatesManager.TheFileAssociatedWithTheTemplateCanNotBeModified" ), fileTemplate.getTitle(), //$NON-NLS-1$
                    fileTemplate.getId(), templateFile.getAbsolutePath() );
            return false;
        }

        // Deleting the file
        if ( !templateFile.delete() )
        {
            // Logging the error
            EntryTemplatePluginUtils
                .logError(
                    null,
                    Messages.getString( "TemplatesManager.TheTemplateCouldNotBeRemovedBecauseOfTheFollowingError" ) //$NON-NLS-1$
                        + Messages
                            .getString( "TemplatesManager.AnErrorOccurredWhenRemovingTheFileAssociatedWithTheTemplate" ), fileTemplate //$NON-NLS-1$
                        .getTitle(), fileTemplate.getId(), templateFile.getAbsolutePath() );
            return false;
        }

        // Removing the template from the disabled templates files
        disabledTemplatesList.remove( fileTemplate );

        // Removing the template for the templates list
        templatesList.remove( fileTemplate );
        templatesByIdMap.remove( fileTemplate.getId() );
        templatesByStructuralObjectClassMap.remove( EntryTemplatePluginUtils
            .getObjectClassDescriptionFromDefaultSchema( fileTemplate.getStructuralObjectClass() ) );

        // Firing the event
        fireTemplateRemoved( fileTemplate );

        return true;
    }


    // ── GET TEMPLATES: MON MOTHMA READS OUT THE FULL ROSTER ──────────────────────
    // Mon Mothma reads out every ship on the roster — active or not. The array
    // is a snapshot so callers can iterate without worrying about concurrent changes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a snapshot array of all registered templates, both enabled and
     * disabled. The array is a fresh copy — modifying it has no effect on the
     * internal list.
     *
     * @return an array of all templates; may be empty but never {@code null}
     */
    public Template[] getTemplates()
    {
        return templatesList.toArray( new Template[0] );
    }


    // ── GET TEMPLATES FOLDER: MON MOTHMA LOCATES THE HANGAR BAY ─────────────────
    // The Rebel Alliance has one dedicated hangar bay for user-imported ships —
    // the plugin's state location under "templates/". This helper returns that
    // absolute path so we're never hard-coding it in two places.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the absolute {@link IPath} of the directory where user-imported
     * template XML files are stored. Appends {@code "templates"} to the plugin's
     * Eclipse state location. The directory may not exist yet (callers must
     * create it with {@code mkdirs()} if needed).
     *
     * @return the path to the managed templates folder
     */
    private static IPath getTemplatesFolder()
    {
        return EntryTemplatePlugin.getDefault().getStateLocation().append( "templates" ); //$NON-NLS-1$
    }


    // ── LOAD DISABLED TEMPLATES: MON MOTHMA READS THE STOOD-DOWN LIST ────────────
    // Mon Mothma opens the standing orders and reads out which squadrons are
    // currently stood down. The IDs are stored as a semicolon-delimited string
    // in the preference store; we tokenize and populate our in-memory list.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Populates {@code disabledTemplatesList} from the preference store.
     * The stored value is a semicolon-delimited string of template IDs.
     */
    private void loadDisabledTemplates()
    {
        StringTokenizer tokenizer = new StringTokenizer( preferenceStore
            .getString( EntryTemplatePluginConstants.PREF_DISABLED_TEMPLATES ), PREFERENCE_DELIMITER );
        while ( tokenizer.hasMoreTokens() )
        {
            disabledTemplatesList.add( tokenizer.nextToken() );
        }
    }


    // ── SAVE DISABLED TEMPLATES: MON MOTHMA UPDATES THE STANDING ORDERS ──────────
    // After a squadron changes status, Mon Mothma rewrites the standing orders
    // so the change survives the next reboot. We serialize all disabled IDs to
    // a semicolon-delimited string and write it back to the preference store.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Persists the current {@code disabledTemplatesList} to the preference store
     * as a semicolon-delimited string of template IDs.
     */
    private void saveDisabledTemplates()
    {
        StringBuffer sb = new StringBuffer();
        for ( String disabledTemplateId : disabledTemplatesList )
        {
            sb.append( disabledTemplateId );
            sb.append( PREFERENCE_DELIMITER );
        }
        preferenceStore.setValue( EntryTemplatePluginConstants.PREF_DISABLED_TEMPLATES, sb.toString() );
    }


    // ── ENABLE TEMPLATE: MON MOTHMA CLEARS A SQUADRON FOR ACTIVE DUTY ────────────
    // "Gold Squadron, you're cleared for launch." Mon Mothma removes their name
    // from the stood-down list, updates the standing orders, and notifies the
    // council. Nothing happens if they're already active.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Marks the given template as enabled. If it was not in the disabled list
     * this is a no-op. Persists the change to the preference store and fires
     * a "template enabled" event.
     *
     * <p>For example — Gold Squadron cleared for launch:</p>
     * <pre>
     *   manager.enableTemplate(goldTemplate);
     *   // Preference store updated; listeners notified.
     * </pre>
     *
     * @param template  the template to enable; must not be {@code null}
     */
    public void enableTemplate( Template template )
    {
        if ( disabledTemplatesList.contains( template.getId() ) )
        {
            // Removing the id of the template to the list of disabled templates
            disabledTemplatesList.remove( template.getId() );

            // Saving the disabled templates list
            saveDisabledTemplates();

            // Firing the event
            fireTemplateEnabled( template );
        }
    }


    // ── DISABLE TEMPLATE: MON MOTHMA STANDS DOWN A SQUADRON ──────────────────────
    // "Blue Squadron, stand down." Mon Mothma adds their name to the stood-down
    // list, rewrites the standing orders, and notifies the council. No-op if
    // they're already stood down.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Marks the given template as disabled. If it was already disabled this is a
     * no-op. Persists the change to the preference store and fires a "template
     * disabled" event.
     *
     * <p>For example — Blue Squadron stood down:</p>
     * <pre>
     *   manager.disableTemplate(blueTemplate);
     *   // Now blueTemplate won't match entries until re-enabled.
     * </pre>
     *
     * @param template  the template to disable; must not be {@code null}
     */
    public void disableTemplate( Template template )
    {
        if ( !disabledTemplatesList.contains( template.getId() ) )
        {
            // Adding the id of the template to the list of disabled templates
            disabledTemplatesList.add( template.getId() );

            // Saving the disabled templates list
            saveDisabledTemplates();

            // Firing the event
            fireTemplateDisabled( template );
        }
    }


    // ── IS ENABLED: MON MOTHMA CHECKS IF A SQUADRON IS ACTIVE ────────────────────
    // A quick check of the stood-down list: is this squadron's name on it? If yes,
    // they're inactive. Simple boolean answer.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the given template is currently enabled (i.e. not in the
     * disabled list).
     *
     * @param template  the template to check; must not be {@code null}
     * @return {@code true} if active; {@code false} if stood down
     */
    public boolean isEnabled( Template template )
    {
        return !disabledTemplatesList.contains( template.getId() );
    }


    // ── LOAD DEFAULT TEMPLATES: MON MOTHMA READS THE DEFAULT PILOT ASSIGNMENTS ───
    // Mon Mothma reads from the preference store which template is the standing
    // default for each object class — stored as "inetOrgPerson:UserAccount;person:StaffRecord;".
    // We split by ";" then by ":" to rebuild the in-memory map.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Populates {@code defaultTemplatesMap} from the preference store.
     * The stored value is a semicolon-delimited list of {@code objectClass:templateId}
     * pairs.
     */
    private void loadDefaultTemplates()
    {
        // Getting each default set
        StringTokenizer tokenizer = new StringTokenizer( preferenceStore
            .getString( EntryTemplatePluginConstants.PREF_DEFAULT_TEMPLATES ), PREFERENCE_DELIMITER );
        while ( tokenizer.hasMoreTokens() )
        {
            String token = tokenizer.nextToken();

            // Splitting the default set
            String[] splittedToken = token.split( ":" ); //$NON-NLS-1$
            if ( splittedToken.length == 2 )
            {
                // Adding the default template value
                defaultTemplatesMap.put( EntryTemplatePluginUtils
                    .getObjectClassDescriptionFromDefaultSchema( splittedToken[0] ), splittedToken[1] );
            }
        }
    }


    // ── SAVE DEFAULT TEMPLATES: MON MOTHMA WRITES THE UPDATED ASSIGNMENTS ────────
    // After a default pilot assignment changes, Mon Mothma rewrites the assignment
    // sheet in the standing orders. We serialize the map to "objClass:id;..." and
    // write it back to the preference store.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Persists the current {@code defaultTemplatesMap} to the preference store
     * as a semicolon-delimited list of {@code objectClass:templateId} pairs.
     */
    private void saveDefaultTemplates()
    {
        StringBuffer sb = new StringBuffer();
        for ( ObjectClass objectClassDescription : defaultTemplatesMap.keySet() )
        {
            sb.append( objectClassDescription.getNames().get( 0 ) );
            sb.append( PREFERENCE_SUB_DELIMITER );
            sb.append( defaultTemplatesMap.get( objectClassDescription ) );
            sb.append( PREFERENCE_DELIMITER );
        }
        preferenceStore.setValue( EntryTemplatePluginConstants.PREF_DEFAULT_TEMPLATES, sb.toString() );
    }


    // ── SET DEFAULT TEMPLATES: MON MOTHMA ASSIGNS DEFAULT PILOTS ─────────────────
    // After roll call, Mon Mothma assigns the first available pilot as the default
    // for each mission type that doesn't yet have one. This ensures every object
    // class has at least one default template even before the user picks their own.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * For every enabled template that doesn't yet have a default assigned to its
     * structural object class, assigns that template as the default. Then persists
     * the updated assignments.
     */
    private void setDefaultTemplates()
    {
        for ( Template template : templatesList )
        {
            if ( isEnabled( template ) )
            {
                String structuralObjectClass = template.getStructuralObjectClass();

                // Checking if a default template is defined
                if ( defaultTemplatesMap.get( EntryTemplatePluginUtils
                    .getObjectClassDescriptionFromDefaultSchema( structuralObjectClass ) ) == null )
                {
                    // Assigning this template as the default one
                    defaultTemplatesMap.put( EntryTemplatePluginUtils
                        .getObjectClassDescriptionFromDefaultSchema( structuralObjectClass ), template.getId() );
                }
            }
        }

        // Saving default templates
        saveDefaultTemplates();
    }


    // ── SET DEFAULT TEMPLATE: MON MOTHMA REASSIGNS THE DEFAULT PILOT ─────────────
    // The commander overrides the standing assignment: "Red Five is now the default
    // for reconnaissance missions." We remove the old assignment for that class and
    // record the new one, then persist it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Designates the given template as the default for its structural object class,
     * replacing any previous default. Only applies if the template is currently
     * enabled. Persists the change immediately.
     *
     * <p>For example — Mon Mothma reassigns the default pilot:</p>
     * <pre>
     *   manager.setDefaultTemplate(userAccountTemplate);
     *   // "UserAccount is now the default for inetOrgPerson entries."
     * </pre>
     *
     * @param template  the template to promote to default; must not be {@code null}
     */
    public void setDefaultTemplate( Template template )
    {
        if ( isEnabled( template ) )
        {
            // Removing the old value
            defaultTemplatesMap.remove( EntryTemplatePluginUtils.getObjectClassDescriptionFromDefaultSchema( template
                .getStructuralObjectClass() ) );

            // Setting the new value
            defaultTemplatesMap.put( EntryTemplatePluginUtils.getObjectClassDescriptionFromDefaultSchema( template
                .getStructuralObjectClass() ), template.getId() );

            // Saving default templates
            saveDefaultTemplates();
        }
    }


    // ── UNSET DEFAULT TEMPLATE: MON MOTHMA CLEARS A DEFAULT ASSIGNMENT ───────────
    // "Scratch Red Five from the default slot — the position is temporarily vacant."
    // We remove the template from the default map and persist the change.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Clears the default-template designation for the given template's structural
     * object class. After this call, that class has no default template until
     * another is assigned. Persists the change immediately.
     *
     * @param template  the currently-default template to demote; must not be {@code null}
     */
    public void unSetDefaultTemplate( Template template )
    {
        if ( isDefaultTemplate( template ) )
        {
            defaultTemplatesMap.remove( EntryTemplatePluginUtils.getObjectClassDescriptionFromDefaultSchema( template
                .getStructuralObjectClass() ) );

            // Saving default template
            saveDefaultTemplates();
        }
    }


    // ── IS DEFAULT TEMPLATE: MON MOTHMA CHECKS THE ASSIGNMENT SHEET ──────────────
    // "Is Red Five the standing default for reconnaissance?" Quick lookup in the
    // assignment map — compare the stored ID against the given template's ID.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the given template is currently the default for its
     * structural object class.
     *
     * @param template  the template to check; must not be {@code null}
     * @return {@code true} if it is the default; {@code false} otherwise
     */
    public boolean isDefaultTemplate( Template template )
    {
        String defaultTemplateID = defaultTemplatesMap.get( EntryTemplatePluginUtils
            .getObjectClassDescriptionFromDefaultSchema( template.getStructuralObjectClass() ) );
        if ( defaultTemplateID != null )
        {
            return defaultTemplateID.equalsIgnoreCase( template.getId() );
        }

        return false;
    }


    // ── HAS DEFAULT TEMPLATE: MON MOTHMA CHECKS IF A MISSION TYPE HAS A PILOT ───
    // "Does the reconnaissance mission type have a default pilot assigned?" Quick
    // delegation to getDefaultTemplate — if the result is non-null, yes it does.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether a default template exists for the given object class name
     * or OID.
     *
     * @param nameOrOid  the object class name (e.g. {@code "inetOrgPerson"}) or OID
     * @return {@code true} if a default template is assigned for this class
     */
    public boolean hasDefaultTemplate( String nameOrOid )
    {
        return getDefaultTemplate( nameOrOid ) != null;
    }


    // ── GET DEFAULT TEMPLATE: MON MOTHMA RETURNS THE STANDING DEFAULT PILOT ───────
    // "Who's the default pilot for reconnaissance?" Mon Mothma consults the
    // assignment sheet, looks up the template ID, then fetches the full template
    // object from the by-ID map.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the default {@link Template} for the given object class name or OID,
     * or {@code null} if no default has been assigned.
     *
     * @param nameOrOid  the object class name or OID to look up
     * @return the default template, or {@code null} if none is assigned
     */
    public Template getDefaultTemplate( String nameOrOid )
    {
        return getTemplateById( defaultTemplatesMap.get( EntryTemplatePluginUtils
            .getObjectClassDescriptionFromDefaultSchema( nameOrOid ) ) );
    }


    // ── GET TEMPLATE BY ID: MON MOTHMA FINDS A SHIP BY CALL SIGN ─────────────────
    // "Find me the ship with call sign 'UserAccount.v2'." Direct lookup in the
    // by-ID hash map — O(1), no iteration required.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a template by its unique ID. Returns {@code null} if no template
     * with that ID is registered.
     *
     * @param id  the template ID to look up; {@code null} returns {@code null}
     * @return the matching template, or {@code null}
     */
    private Template getTemplateById( String id )
    {
        return templatesByIdMap.get( id );
    }


    // ── GET TEMPLATES BY OBJECT CLASS: MON MOTHMA LISTS ALL PILOTS FOR A MISSION ─
    // "Who can fly reconnaissance missions?" Mon Mothma returns everyone cleared
    // for that mission type — all templates whose structural class matches the
    // given name or OID.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all templates whose structural object class matches the given name
     * or OID. The list may be empty if no templates are registered for that class.
     *
     * <p>For example — Mon Mothma lists all pilots for a mission type:</p>
     * <pre>
     *   List&lt;Template&gt; templates = manager.getTemplatesByObjectClass("inetOrgPerson");
     *   // returns [UserAccountTemplate, PersonTemplate]
     * </pre>
     *
     * @param nameOrOid  the object class name or OID
     * @return the list of matching templates, or {@code null} if none found
     */
    @SuppressWarnings("unchecked")
    public List<Template> getTemplatesByObjectClass( String nameOrOid )
    {
        return ( List<Template> ) templatesByStructuralObjectClassMap.get( EntryTemplatePluginUtils
            .getObjectClassDescriptionFromDefaultSchema( nameOrOid ) );
    }

}
