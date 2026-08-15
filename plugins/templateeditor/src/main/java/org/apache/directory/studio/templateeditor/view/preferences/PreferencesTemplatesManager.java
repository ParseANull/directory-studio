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
package org.apache.directory.studio.templateeditor.view.preferences;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import org.apache.directory.studio.templateeditor.EntryTemplatePluginUtils;
import org.apache.directory.studio.templateeditor.TemplatesManager;
import org.apache.directory.studio.templateeditor.TemplatesManagerListener;
import org.apache.directory.studio.templateeditor.model.FileTemplate;
import org.apache.directory.studio.templateeditor.model.Template;
import org.apache.directory.studio.templateeditor.model.parser.TemplateIO;
import org.apache.directory.studio.templateeditor.model.parser.TemplateIOException;


// ── CLASS: PreferencesTemplatesManager — PALPATINE'S PREFERENCE-PAGE STAGING DESK
// Palpatine never edits the Empire's permanent records directly in the field —
// he drafts orders on a staging desk first, reviews them, and only commits them
// when he taps "OK". This class is that staging desk for the preference page.
// It mirrors the real {@link TemplatesManager} in memory — same templates, same
// enabled/disabled states, same default assignments — and accumulates adds, removes,
// and state changes. When the user clicks OK, {@link #saveModifications()} replays
// the accumulated changes onto the real manager.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * An in-memory snapshot of the plugin's {@link TemplatesManager} used exclusively
 * by the Template Entry Editor preference page. All add/remove/enable/disable
 * operations performed in the UI go through this class; they are not written to
 * the real manager until {@link #saveModifications()} is called when the user
 * clicks "OK".
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PreferencesTemplatesManager
{
    /** The templates manager */
    private TemplatesManager manager;

    /** The list containing all the templates */
    private List<Template> templatesList = new ArrayList<Template>();

    /** The map containing the templates based on their IDs */
    private Map<String, Template> templatesByIdMap = new HashMap<String, Template>();

    /** The map containing the default templates */
    private Map<ObjectClass, String> defaultTemplatesMap = new HashMap<ObjectClass, String>();

    /** The set containing *only* the IDs of the disabled templates */
    private List<String> disabledTemplatesList = new ArrayList<String>();

    /** The list of listeners */
    private List<TemplatesManagerListener> listeners = new ArrayList<TemplatesManagerListener>();


    // ── CONSTRUCTOR: SNAPSHOT THE REAL MANAGER ────────────────────────────────────
    // Palpatine's staging desk is initialised from the real manager's current state:
    // all templates, their enabled/disabled flags, and the current default assignments
    // are copied into the in-memory data structures.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code PreferencesTemplatesManager} that starts as a snapshot
     * of the given real {@link TemplatesManager}.
     *
     * @param manager  the live plugin templates manager to snapshot
     */
    public PreferencesTemplatesManager( TemplatesManager manager )
    {
        this.manager = manager;

        init();
    }


    // ── ADD LISTENER ──────────────────────────────────────────────────────────────
    /**
     * Registers a {@link TemplatesManagerListener} to receive add/remove/enable/disable
     * events.
     *
     * @param listener  the listener to add
     * @return {@code true} as per {@link java.util.Collection#add(Object)}
     */
    public boolean addListener( TemplatesManagerListener listener )
    {
        return listeners.add( listener );
    }


    // ── REMOVE LISTENER ───────────────────────────────────────────────────────────
    /**
     * Deregisters a {@link TemplatesManagerListener}.
     *
     * @param listener  the listener to remove
     * @return {@code true} if the listener was registered
     */
    public boolean removeListener( TemplatesManagerListener listener )
    {
        return listeners.remove( listener );
    }


    // ── FIRE TEMPLATE ADDED ───────────────────────────────────────────────────────
    /**
     * Notifies all listeners that a template has been added.
     *
     * @param template  the added template
     */
    private void fireTemplateAdded( Template template )
    {
        for ( TemplatesManagerListener listener : listeners.toArray( new TemplatesManagerListener[0] ) )
        {
            listener.templateAdded( template );
        }
    }


    // ── FIRE TEMPLATE REMOVED ─────────────────────────────────────────────────────
    /**
     * Notifies all listeners that a template has been removed.
     *
     * @param template  the removed template
     */
    private void fireTemplateRemoved( Template template )
    {
        for ( TemplatesManagerListener listener : listeners.toArray( new TemplatesManagerListener[0] ) )
        {
            listener.templateRemoved( template );
        }
    }


    // ── FIRE TEMPLATE ENABLED ─────────────────────────────────────────────────────
    /**
     * Notifies all listeners that a template has been enabled.
     *
     * @param template  the enabled template
     */
    private void fireTemplateEnabled( Template template )
    {
        for ( TemplatesManagerListener listener : listeners.toArray( new TemplatesManagerListener[0] ) )
        {
            listener.templateEnabled( template );
        }
    }


    // ── FIRE TEMPLATE DISABLED ────────────────────────────────────────────────────
    /**
     * Notifies all listeners that a template has been disabled.
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


    // ── INIT: SNAPSHOT THE REAL MANAGER ───────────────────────────────────────────
    // Palpatine's staging desk copies the real manager's current state into its
    // in-memory data structures.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the in-memory snapshot from the real {@link TemplatesManager}.
     * Called once from the constructor.
     */
    private void init()
    {
        // Getting the templates from the plugin manager
        Template[] pluginTemplates = manager.getTemplates();
        for ( Template pluginTemplate : pluginTemplates )
        {
            templatesList.add( pluginTemplate );
            templatesByIdMap.put( pluginTemplate.getId(), pluginTemplate );

            // Is the template enabled?
            if ( !manager.isEnabled( pluginTemplate ) )
            {
                disabledTemplatesList.add( pluginTemplate.getId() );
            }

            // Is it the default template?
            if ( manager.isDefaultTemplate( pluginTemplate ) )
            {
                defaultTemplatesMap.put( EntryTemplatePluginUtils
                    .getObjectClassDescriptionFromDefaultSchema( pluginTemplate.getStructuralObjectClass() ),
                    pluginTemplate.getId() );
            }
        }
    }


    // ── SAVE MODIFICATIONS: COMMIT THE STAGING DESK TO THE REAL MANAGER ──────────
    // Palpatine ratifies the standing orders: he walks through the real manager's
    // templates, applies any changed enabled/default states, removes deleted
    // templates, and adds new ones. If anything fails, an error dialog is shown
    // and false is returned so the preference page can keep the dialog open.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Commits all staged changes to the real {@link TemplatesManager}. Handles
     * removals, additions, enable/disable state changes, and default-template changes.
     * Shows an error dialog and returns {@code false} if any operation fails so the
     * preference page can stay open.
     *
     * @return {@code true} if all changes were applied successfully;
     *         {@code false} if any operation failed
     */
    public boolean saveModifications()
    {
        // Getting original templates
        Template[] originalTemplates = manager.getTemplates();

        // Creating a list of original templates
        List<Template> originalTemplatesList = new ArrayList<Template>();

        // Looping on original templates
        for ( Template originalTemplate : originalTemplates )
        {
            // Checking if the enablement state has been changed
            boolean isEnabled = isEnabled( originalTemplate );
            if ( manager.isEnabled( originalTemplate ) != isEnabled )
            {
                if ( isEnabled )
                {
                    manager.enableTemplate( originalTemplate );
                }
                else
                {
                    manager.disableTemplate( originalTemplate );
                }
            }

            // Checking if the default state has been changed
            boolean isDefaultTemplate = isDefaultTemplate( originalTemplate );
            if ( manager.isDefaultTemplate( originalTemplate ) != isDefaultTemplate )
            {
                if ( isDefaultTemplate )
                {
                    manager.setDefaultTemplate( originalTemplate );
                }
                else
                {
                    manager.unSetDefaultTemplate( originalTemplate );
                }
            }

            // Checking if the original template has been removed
            if ( !templatesList.contains( originalTemplate ) )
            {
                if ( !manager.removeTemplate( ( FileTemplate ) originalTemplate ) )
                {
                    // Creating and opening the error dialog
                    String dialogTitle = Messages.getString( "PreferencesTemplatesManager.UnableToRemoveTheTemplate" ); //$NON-NLS-1$
                    String dialogMessage = MessageFormat
                        .format(
                            Messages.getString( "PreferencesTemplatesManager.TheTemplateCouldNotBeRemoved" ) //$NON-NLS-1$
                                + EntryTemplatePluginUtils.LINE_SEPARATOR
                                + EntryTemplatePluginUtils.LINE_SEPARATOR
                                + Messages.getString( "PreferencesTemplatesManager.SeeTheLogsFileForMoreInformation" ), originalTemplate.getTitle() ); //$NON-NLS-1$
                    MessageDialog dialog = new MessageDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getShell(), dialogTitle, null, dialogMessage, MessageDialog.ERROR, new String[]
                        { IDialogConstants.OK_LABEL }, MessageDialog.OK );
                    dialog.open();
                    return false;
                }
            }

            // Adding the template to the list
            originalTemplatesList.add( originalTemplate );
        }

        // Looping on the new templates list
        for ( Template template : templatesList )
        {
            // Checking if the template has been added
            if ( !originalTemplatesList.contains( template ) )
            {
                // Adding the new template
                if ( !manager.addTemplate( new File( ( ( PreferencesFileTemplate ) template ).getFilePath() ) ) )
                {
                    // Creating and opening the error dialog
                    String dialogTitle = Messages.getString( "PreferencesTemplatesManager.UnableToAddTheTemplate" ); //$NON-NLS-1$
                    String dialogMessage = MessageFormat
                        .format(
                            Messages.getString( "PreferencesTemplatesManager.TheTemplateCouldNotBeAdded" ) //$NON-NLS-1$
                                + EntryTemplatePluginUtils.LINE_SEPARATOR
                                + EntryTemplatePluginUtils.LINE_SEPARATOR
                                + Messages.getString( "PreferencesTemplatesManager.SeeTheLogsFileForMoreInformation" ), template.getTitle() ); //$NON-NLS-1$
                    MessageDialog dialog = new MessageDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getShell(), dialogTitle, null, dialogMessage, MessageDialog.ERROR, new String[]
                        { IDialogConstants.OK_LABEL }, MessageDialog.OK );
                    dialog.open();
                    return false;
                }

                // Setting the enablement state to the new template
                boolean isEnabled = isEnabled( template );
                if ( isEnabled )
                {
                    manager.enableTemplate( template );
                }
                else
                {
                    manager.disableTemplate( template );
                }

                // Setting the default state has been changed
                boolean isDefaultTemplate = isDefaultTemplate( template );
                if ( isDefaultTemplate )
                {
                    manager.setDefaultTemplate( template );
                }
                else
                {
                    manager.unSetDefaultTemplate( template );
                }
            }
        }

        return true;
    }


    // ── GET TEMPLATES: RETURN ALL STAGED TEMPLATES ────────────────────────────────
    /**
     * Returns all templates currently on the staging desk as an array.
     *
     * @return all staged templates (may be empty)
     */
    public Template[] getTemplates()
    {
        return templatesList.toArray( new Template[0] );
    }


    // ── IS ENABLED: CHECK STAGED ENABLED STATE ────────────────────────────────────
    /**
     * Returns {@code true} if the given template is currently staged as enabled.
     *
     * @param template  the template to check
     * @return {@code true} if enabled; {@code false} if on the disabled list
     */
    public boolean isEnabled( Template template )
    {
        return !disabledTemplatesList.contains( template.getId() );
    }


    // ── ADD TEMPLATE: STAGE A NEW TEMPLATE FILE ───────────────────────────────────
    // Palpatine's staging desk reads the template file, validates it, and adds it
    // to the in-memory list. It won't be written to the real manager until OK is clicked.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads a template XML file from disk, parses it, and adds the resulting
     * {@link PreferencesFileTemplate} to the staging desk. Returns {@code false}
     * (and logs an error) if the file doesn't exist, can't be read, or fails to parse.
     *
     * @param templateFile  the template XML file to add
     * @return {@code true} if the template was successfully added; {@code false} otherwise
     */
    public boolean addTemplate( File templateFile )
    {
        // Getting the template
        PreferencesFileTemplate template = getTemplateFromFile( templateFile );
        if ( template == null )
        {
            // If the file is not valid, we simply return
            return false;
        }

        if ( templatesByIdMap.containsKey( template.getId() ) )
        {
            // Logging the error
            EntryTemplatePluginUtils
                .logError(
                    null,
                    Messages
                        .getString( "PreferencesTemplatesManager.TheTemplateFileCouldNotBeAddedBecauseATemplateWithSameIDAlreadyExist" ), //$NON-NLS-1$
                    templateFile.getAbsolutePath() );
            return false;
        }

        // Adding the template
        templatesList.add( template );
        templatesByIdMap.put( template.getId(), template );

        // If there's no default template, then set this one as default one
        if ( !defaultTemplatesMap.containsKey( EntryTemplatePluginUtils
            .getObjectClassDescriptionFromDefaultSchema( template.getStructuralObjectClass() ) ) )
        {
            setDefaultTemplate( template );
        }

        // Firing the event
        fireTemplateAdded( template );

        return true;
    }


    // ── GET TEMPLATE FROM FILE: PARSE AND VALIDATE THE FILE ───────────────────────
    /**
     * Attempts to parse the given template XML file into a {@link PreferencesFileTemplate}.
     * Returns {@code null} (and logs an error) if the file does not exist, is
     * unreadable, or fails to parse.
     *
     * @param templateFile  the file to read
     * @return the parsed template, or {@code null} on failure
     */
    private PreferencesFileTemplate getTemplateFromFile( File templateFile )
    {
        // Checking if the file exists
        if ( !templateFile.exists() )
        {
            // Logging the error
            EntryTemplatePluginUtils
                .logError(
                    null,
                    Messages
                        .getString( "PreferencesTemplatesManager.TheTemplateFileCouldNotBeAddedBecauseItDoesNotExist" ), templateFile //$NON-NLS-1$
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
                    Messages
                        .getString( "PreferencesTemplatesManager.TheTemplateFileCouldNotBeAddedBecauseItCantBeRead" ), templateFile.getAbsolutePath() ); //$NON-NLS-1$
            return null;
        }

        // Trying to parse the template file
        PreferencesFileTemplate fileTemplate = null;
        try
        {
            InputStream is = new FileInputStream( templateFile );

            fileTemplate = TemplateIO.readAsPreferencesFileTemplate( is );
            fileTemplate.setFilePath( templateFile.getAbsolutePath() );

            is.close();
        }
        catch ( IOException e )
        {
            // Logging the error
            EntryTemplatePluginUtils
                .logError(
                    e,
                    Messages
                        .getString( "PreferencesTemplatesManager.TheTemplateFileCouldNotBeAddedBecauseOfTheFollowingError" ), templateFile //$NON-NLS-1$
                        .getAbsolutePath(), e.getMessage() );
            return null;
        }
        catch ( TemplateIOException e )
        {
            // Logging the error
            EntryTemplatePluginUtils
                .logError(
                    e,
                    Messages
                        .getString( "PreferencesTemplatesManager.TheTemplateFileCouldNotBeAddedBecauseOfTheFollowingError" ), templateFile //$NON-NLS-1$
                        .getAbsolutePath(), e.getMessage() );
            return null;
        }

        // Everything went fine, the file is valid
        return fileTemplate;
    }


    // ── REMOVE TEMPLATE: STAGE A TEMPLATE FOR DELETION ────────────────────────────
    /**
     * Removes a template from the staging desk. If it was the default template for
     * its object class, a new default is automatically assigned. Returns {@code false}
     * if the template is not found on the staging desk.
     *
     * @param template  the template to remove
     * @return {@code true} if removed; {@code false} if not found
     */
    public boolean removeTemplate( Template template )
    {
        // Checking if the file template exists in the templates set
        if ( !templatesList.contains( template ) )
        {
            // Logging the error
            EntryTemplatePluginUtils
                .logError(
                    null,
                    Messages
                        .getString( "PreferencesTemplatesManager.TheTemplateFileCouldNotBeRemovedBecauseOfTheFollowingError" ) //$NON-NLS-1$
                        + Messages
                            .getString( "PreferencesTemplatesManager.TheTemplateDoesNotExistInTheTemplateManager" ), template.getTitle(), template.getId() ); //$NON-NLS-1$
            return false;
        }

        // Removing the template from the disabled templates list
        if ( disabledTemplatesList.contains( template.getId() ) )
        {
            disabledTemplatesList.remove( template.getId() );
        }

        // Removing the template for the templates list
        templatesList.remove( template );
        templatesByIdMap.remove( template.getId() );

        // Checking if the template is the default one
        if ( isDefaultTemplate( template ) )
        {
            // Unsetting the template as default
            unSetDefaultTemplate( template );

            // Assign another default template.
            setNewAutoDefaultTemplate( template.getStructuralObjectClass() );
        }

        // Firing the event
        fireTemplateRemoved( template );

        return true;
    }


    // ── ENABLE TEMPLATE: MARK AS ENABLED ON THE STAGING DESK ─────────────────────
    /**
     * Marks the given template as enabled on the staging desk. If no default template
     * exists for the template's object class, this template is automatically set as
     * the default.
     *
     * @param template  the template to enable
     */
    public void enableTemplate( Template template )
    {
        // Removing the id of the template to the list of disabled templates
        disabledTemplatesList.remove( template.getId() );

        // If there's no default template, then set this one as default one
        if ( !defaultTemplatesMap.containsKey( EntryTemplatePluginUtils
            .getObjectClassDescriptionFromDefaultSchema( template.getStructuralObjectClass() ) ) )
        {
            setDefaultTemplate( template );
        }

        // Firing the event
        fireTemplateEnabled( template );
    }


    // ── DISABLE TEMPLATE: MARK AS DISABLED ON THE STAGING DESK ───────────────────
    /**
     * Marks the given template as disabled on the staging desk. If it was the default
     * template for its object class, a new default is automatically chosen.
     *
     * @param template  the template to disable
     */
    public void disableTemplate( Template template )
    {
        if ( !disabledTemplatesList.contains( template.getId() ) )
        {
            // Adding the id of the template to the list of disabled templates
            disabledTemplatesList.add( template.getId() );

            // Checking if the template is the default one
            if ( isDefaultTemplate( template ) )
            {
                // Unsetting the template as default
                unSetDefaultTemplate( template );

                // Assign another default template.
                setNewAutoDefaultTemplate( template.getStructuralObjectClass() );
            }

            // Firing the event
            fireTemplateDisabled( template );
        }
    }


    // ── SET DEFAULT TEMPLATE ──────────────────────────────────────────────────────
    /**
     * Sets the given template as the default for its structural object class.
     * Only effective if the template is currently enabled.
     *
     * @param template  the template to designate as default
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
        }
    }


    // ── UNSET DEFAULT TEMPLATE ────────────────────────────────────────────────────
    /**
     * Removes the given template's designation as the default for its structural
     * object class.
     *
     * @param template  the template to remove from default status
     */
    public void unSetDefaultTemplate( Template template )
    {
        if ( isDefaultTemplate( template ) )
        {
            defaultTemplatesMap.remove( EntryTemplatePluginUtils.getObjectClassDescriptionFromDefaultSchema( template
                .getStructuralObjectClass() ) );
        }
    }


    // ── SET NEW AUTO DEFAULT TEMPLATE ─────────────────────────────────────────────
    /**
     * Automatically assigns a new default template for the given structural object
     * class by picking the first enabled template found for that class. Does nothing
     * if no enabled candidate is found.
     *
     * @param structuralObjectClass  the object class name whose default has been vacated
     */
    public void setNewAutoDefaultTemplate( String structuralObjectClass )
    {
        ObjectClass structuralOcd = EntryTemplatePluginUtils
            .getObjectClassDescriptionFromDefaultSchema( structuralObjectClass );

        for ( Template templateCandidate : templatesList )
        {
            ObjectClass templateCandidateOcd = EntryTemplatePluginUtils
                .getObjectClassDescriptionFromDefaultSchema( templateCandidate.getStructuralObjectClass() );
            if ( structuralOcd.equals( templateCandidateOcd ) )
            {
                if ( isEnabled( templateCandidate ) )
                {
                    // Setting the new value
                    defaultTemplatesMap.put( templateCandidateOcd, templateCandidate.getId() );
                    return;
                }
            }
        }
    }


    // ── IS DEFAULT TEMPLATE ───────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the given template is currently designated as the
     * default for its structural object class on the staging desk.
     *
     * @param template  the template to check
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

}
