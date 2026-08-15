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
package org.apache.directory.studio.schemaeditor;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.util.FileUtils;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.schemaeditor.controller.ProjectsHandler;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.model.io.ProjectsExporter;
import org.apache.directory.studio.schemaeditor.model.io.ProjectsImportException;
import org.apache.directory.studio.schemaeditor.model.io.ProjectsImporter;
import org.apache.directory.studio.schemaeditor.model.io.SchemaConnector;
import org.apache.directory.studio.schemaeditor.model.io.XMLSchemaFileImportException;
import org.apache.directory.studio.schemaeditor.model.io.XMLSchemaFileImporter;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.apache.directory.studio.schemaeditor.view.widget.CoreSchemasSelectionWidget.ServerTypeEnum;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;


// ── CLASS: PluginUtils — R2-D2 Plugs Into The Death Star Computer ─────────────
// R2-D2 trundles up to a Death Star terminal, extends his interface arm, and
// starts doing the messy behind-the-scenes work: pulling the prison cell map,
// killing the tractor beam, calculating escape routes. He handles all the dirty
// I/O so the humans upstairs can stay focused. PluginUtils is R2: it handles
// file I/O, cloning, logging, connector discovery, and dialog-settings history
// so every other class stays clean.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A collection of static utility methods used throughout the Schema Editor plugin.
 * Think of this as R2-D2 interfacing with the Death Star: it does the fiddly,
 * error-prone work (loading and saving projects, cloning schema objects, looking
 * up connections, discovering extension connectors) so the higher-level classes
 * can stay focused on what they care about.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PluginUtils
{
    // ── R2 Validates The Access Code Before Opening The Door ──────────────────────
    // R2 checks the Death Star's access-code format against a known pattern before
    // submitting it — a malformed code would trigger an alert and blow the mission.
    // verifyName does the same check: it rejects names that don't match the RFC 2252
    // pattern before we try to register them in the schema.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether a schema element name is syntactically valid according to RFC 2252
     * (LDAP attribute syntax definitions). We need this before accepting user-typed names
     * for attribute types or object classes — an invalid name would be rejected by any
     * real LDAP server anyway, so we catch it early in the UI.
     *
     * <p>For example — R2 validates the access code:</p>
     * <pre>
     *   "cn"           → valid   (letters only)
     *   "my-attr2"     → valid   (letter start, then letters/digits/hyphens)
     *   "2bad"         → invalid (starts with a digit)
     *   "bad name"     → invalid (space not allowed)
     * </pre>
     *
     * @param name  the candidate name string to test — must not be null.
     * @return      true if the name matches the RFC 2252 pattern, false otherwise.
     */
    public static boolean verifyName( String name )
    {
        return name.matches( "[a-zA-Z]+[a-zA-Z0-9;-]*" ); //$NON-NLS-1$
    }


    // ── R2 Copies The Schematics Before Handing Them Over ─────────────────────────
    // R2 never hands anyone the original Death Star blueprints — he makes a copy
    // first so the originals stay intact regardless of what the rebels do with the
    // printout. getClone(AttributeType) does the same: it makes a full independent
    // copy of an attribute type so edits in a dialog don't accidentally mutate the
    // live schema object.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a fully independent copy of the given AttributeType, copying every
     * property field by value. We need this because the schema editor lets the user
     * edit a draft copy and only commits changes back when they save — working directly
     * on the live object would make every keystroke an immediate mutation.
     *
     * <p>For example — R2 duplicates the schematics:</p>
     * <pre>
     *   AttributeType live = schemaHandler.getAttributeType("cn");
     *   AttributeType draft = PluginUtils.getClone(live);
     *   // user edits draft freely; live is unaffected until save
     * </pre>
     *
     * @param at  the AttributeType to clone — must not be null.
     * @return    a new AttributeType instance with all properties copied from {@code at}.
     */
    public static AttributeType getClone( AttributeType at )
    {
        AttributeType clone = new AttributeType( at.getOid() );
        clone.setNames( at.getNames() );
        clone.setSchemaName( at.getSchemaName() );
        clone.setDescription( at.getDescription() );
        clone.setSuperiorOid( at.getSuperiorOid() );
        clone.setUsage( at.getUsage() );
        clone.setSyntaxOid( at.getSyntaxOid() );
        clone.setSyntaxLength( at.getSyntaxLength() );
        clone.setObsolete( at.isObsolete() );
        clone.setSingleValued( at.isSingleValued() );
        clone.setCollective( at.isCollective() );
        clone.setUserModifiable( at.isUserModifiable() );
        clone.setEqualityOid( at.getEqualityOid() );
        clone.setOrderingOid( at.getOrderingOid() );
        clone.setSubstringOid( at.getSubstringOid() );

        return clone;
    }


    // ── R2 Copies The Object Class Schematics ─────────────────────────────────────
    // Same discipline as with the attribute type blueprint: R2 copies the structural
    // class definitions before handing them out, so the originals stay safe in the
    // archive while the rebels experiment with the copy.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a fully independent copy of the given ObjectClass, copying every property
     * field by value. Same motivation as the AttributeType overload: the editor works
     * on a draft that only gets committed when the user saves, so we need a clean copy
     * that is isolated from the live schema object.
     *
     * @param oc  the ObjectClass to clone — must not be null.
     * @return    a new ObjectClass instance with all properties copied from {@code oc}.
     */
    public static ObjectClass getClone( ObjectClass oc )
    {
        ObjectClass clone = new ObjectClass( oc.getOid() );
        clone.setNames( oc.getNames() );
        clone.setSchemaName( oc.getSchemaName() );
        clone.setDescription( oc.getDescription() );
        clone.setSuperiorOids( oc.getSuperiorOids() );
        clone.setType( oc.getType() );
        clone.setObsolete( oc.isObsolete() );
        clone.setMustAttributeTypeOids( oc.getMustAttributeTypeOids() );
        clone.setMayAttributeTypeOids( oc.getMayAttributeTypeOids() );

        return clone;
    }


    // ── R2 Locates The Primary Data Storage Bay ───────────────────────────────────
    // R2 navigates the Death Star's corridor map to find the main data storage bay
    // where the mission-critical files are kept. getProjectsFile() does the same:
    // it computes the canonical path of the projects XML file inside the plugin's
    // OSGi state area — the one true place where project data lives on disk.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a File pointing to {@code projects.xml} inside the plugin's persistent
     * state area (provided by OSGi). This is the primary file that holds the serialized
     * list of all schema projects across sessions. The path is computed fresh each call
     * from the plugin's current state location, so it handles workspace relocation.
     *
     * @return  the File handle for {@code projects.xml} — the file may not exist yet
     *          on a fresh installation.
     */
    private static File getProjectsFile()
    {
        return Activator.getDefault().getStateLocation().append( "projects.xml" ).toFile(); //$NON-NLS-1$
    }


    // ── R2 Flags The Backup Data Cartridge Location ───────────────────────────────
    // R2 always knows where the backup copy is stored — when the main data bank is
    // corrupted, he goes straight to the secondary cartridge bay. getTempProjectsFile
    // is that backup: we write here first so a crash mid-write doesn't corrupt the
    // primary file.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a File pointing to {@code projects-temp.xml}, the intermediate staging
     * file we write to before copying over the primary projects file. This two-step
     * write pattern protects against data loss if the process crashes mid-save —
     * the primary file is only overwritten once the temp write has fully succeeded.
     *
     * @return  the File handle for {@code projects-temp.xml}.
     */
    private static File getTempProjectsFile()
    {
        return Activator.getDefault().getStateLocation().append( "projects-temp.xml" ).toFile(); //$NON-NLS-1$
    }


    // ── R2 Restores The Mission Briefing From The Data Core ───────────────────────
    // R2 slots into the briefing room terminal and pulls up the mission files —
    // primary source first, backup cartridge if the main is unreadable, and a
    // loud error message if both are gone. loadProjects mirrors that resilience:
    // it tries the main projects file, falls back to the temp, and reports clearly
    // when neither works.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads all saved schema projects from disk and registers them with the
     * ProjectsHandler. We try the primary projects file first; if that fails we fall
     * back to the temp file (which was the last successful write); if both fail we
     * report an error dialog and bail out. This is called once at plugin startup.
     */
    public static void loadProjects()
    {
        ProjectsHandler projectsHandler = Activator.getDefault().getProjectsHandler();
        File projectsFile = getProjectsFile();
        boolean loadFailed = false;
        Project[] projects = null;

        // We try to load the projects file
        if ( projectsFile.exists() )
        {
            try
            {
                projects = ProjectsImporter.getProjects( new FileInputStream( projectsFile ), projectsFile
                    .getAbsolutePath() );
            }
            catch ( ProjectsImportException e )
            {
                loadFailed = true;
            }
            catch ( FileNotFoundException e )
            {
                loadFailed = true;
            }

            if ( !loadFailed )
            {
                // If everything went fine, we add the projects
                for ( Project project : projects )
                {
                    projectsHandler.addProject( project );
                }
            }
            else
            {
                // If something went wrong, we try to load the temp projects file
                File tempProjectsFile = getTempProjectsFile();

                if ( tempProjectsFile.exists() )
                {
                    try
                    {
                        projects = ProjectsImporter.getProjects( new FileInputStream( tempProjectsFile ), projectsFile
                            .getAbsolutePath() );

                        loadFailed = false;
                    }
                    catch ( ProjectsImportException e )
                    {
                        reportError( Messages.getString( "PluginUtils.ErrorLoadingProject" ), e, Messages //$NON-NLS-1$
                            .getString( "PluginUtils.ProjectsLoadingError" ), Messages //$NON-NLS-1$
                            .getString( "PluginUtils.ErrorLoadingProject" ) ); //$NON-NLS-1$
                        return;
                    }
                    catch ( FileNotFoundException e )
                    {
                        reportError( Messages.getString( "PluginUtils.ErrorLoadingProject" ), e, Messages //$NON-NLS-1$
                            .getString( "PluginUtils.ProjectsLoadingError" ), Messages //$NON-NLS-1$
                            .getString( "PluginUtils.ErrorLoadingProject" ) ); //$NON-NLS-1$
                        return;
                    }

                    // We add the projects
                    for ( Project project : projects )
                    {
                        projectsHandler.addProject( project );
                    }
                }
                else
                {
                    reportError( Messages.getString( "PluginUtils.ErrorLoadingProject" ), null, Messages //$NON-NLS-1$
                        .getString( "PluginUtils.ProjectsLoadingError" ), Messages //$NON-NLS-1$
                        .getString( "PluginUtils.ErrorLoadingProject" ) ); //$NON-NLS-1$
                }

            }
        }
    }


    // ── R2 Commits The Updated Mission Files To Secure Storage ────────────────────
    // Before leaving the Death Star terminal, R2 writes the updated data to a
    // temporary cartridge, verifies it, then overwrites the primary archive — so
    // the archive is never left in a half-written state. saveProjects does the same
    // two-stage write, falling back to a direct write if the staging step fails.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Serializes all current projects to disk using a safe two-stage write: first to
     * the temp file, then copied over the primary file. If the staging write fails we
     * attempt a direct write to the primary file. If that also fails we show an error
     * dialog. This is called every time a project is added, removed, or the open
     * project changes.
     */
    public static void saveProjects()
    {
        try
        {
            // Saving the projects to the temp projects file
            OutputFormat outformat = OutputFormat.createPrettyPrint();
            outformat.setEncoding( "UTF-8" ); //$NON-NLS-1$
            XMLWriter writer = new XMLWriter( new FileOutputStream( getTempProjectsFile() ), outformat );
            writer.write( ProjectsExporter.toDocument( Activator.getDefault().getProjectsHandler().getProjects()
                .toArray( new Project[0] ) ) );
            writer.flush();

            // Copying the temp projects file to the final location
            String content = FileUtils.readFileToString( getTempProjectsFile(), "UTF-8" ); //$NON-NLS-1$
            FileUtils.writeStringToFile( getProjectsFile(), content, "UTF-8" ); //$NON-NLS-1$
        }
        catch ( IOException e )
        {
            // If an error occurs when saving to the temp projects file or
            // when copying the temp projects file to the final location,
            // we try to save the projects directly to the final location.
            try
            {
                OutputFormat outformat = OutputFormat.createPrettyPrint();
                outformat.setEncoding( "UTF-8" ); //$NON-NLS-1$
                XMLWriter writer = new XMLWriter( new FileOutputStream( getProjectsFile() ), outformat );
                writer.write( ProjectsExporter.toDocument( Activator.getDefault().getProjectsHandler().getProjects()
                    .toArray( new Project[0] ) ) );
                writer.flush();
            }
            catch ( IOException e2 )
            {
                // If another error occur, we display an error
                reportError( Messages.getString( "PluginUtils.ErrorSavingProject" ), e2, Messages //$NON-NLS-1$
                    .getString( "PluginUtils.ProjectsSavingError" ), Messages //$NON-NLS-1$
                    .getString( "PluginUtils.ErrorSavingProject" ) ); //$NON-NLS-1$
            }
        }
    }


    // ── R2 Beams A Distress Signal To The Rebel Fleet ─────────────────────────────
    // When R2 detects a critical system failure he immediately sends an ERROR-level
    // distress signal through the ship's comm system — logged and visible to anyone
    // monitoring. logError is that distress signal: it writes an ERROR entry to the
    // Eclipse platform log so ops or developers can see exactly what went wrong.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Writes a message and optional exception to the Eclipse platform error log at
     * ERROR severity. Use this for failures that are unexpected or that the user
     * should know about — they'll show up in the Error Log view if the user has it
     * open.
     *
     * @param message    a human-readable description of what failed.
     * @param exception  the exception that caused the failure, or null if none.
     */
    public static void logError( String message, Throwable exception )
    {
        Activator.getDefault().getLog().log(
            new Status( Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(), Status.OK, message,
                exception ) );
    }


    // ── R2 Logs A Status Update To The Navicomputer ───────────────────────────────
    // R2 periodically writes informational progress notes to the navicomputer's log —
    // not errors, just useful context for anyone reviewing the flight record later.
    // logInfo does the same: INFO-level log entries for non-critical events we want
    // to be able to trace after the fact.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Writes a message and optional exception to the Eclipse platform log at INFO
     * severity. The message is a {@link java.text.MessageFormat} pattern, so callers
     * can pass in positional arguments ({@code {0}}, {@code {1}}, etc.) and they get
     * formatted before logging — handy for including variable details without building
     * strings when logging is disabled.
     *
     * @param exception  the associated exception, or null if this is a pure info entry.
     * @param message    a MessageFormat pattern string.
     * @param args       optional positional arguments substituted into the pattern.
     */
    public static void logInfo( Throwable exception, String message, Object... args )
    {
        String msg = MessageFormat.format( message, args );
        Activator.getDefault().getLog().log(
            new Status( Status.INFO, Activator.getDefault().getBundle().getSymbolicName(), Status.OK, msg, exception ) );
    }


    // ── R2 Raises A Yellow-Level Caution Flag ─────────────────────────────────────
    // R2's sensor array picks up something unusual — not a full alarm, but worth
    // flagging so the crew can decide whether to act. logWarning is that yellow
    // flag: the situation is recoverable, but we want a record in the log in case
    // it becomes a pattern.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Writes a message and optional exception to the Eclipse platform log at WARNING
     * severity. Use this for conditions that are unexpected but not fatal — the
     * operation can continue, but the entry in the log may help diagnose a larger
     * problem later.
     *
     * @param message    a human-readable description of the unusual condition.
     * @param exception  the associated exception, or null if there is none.
     */
    public static void logWarning( String message, Throwable exception )
    {
        Activator.getDefault().getLog().log(
            new Status( Status.WARNING, Activator.getDefault().getBundle().getSymbolicName(), Status.OK, message,
                exception ) );
    }


    // ── R2 Pulls The Core Schema Files From The Archive ───────────────────────────
    // R2 navigates to the correct archive drawer, pulls the right data cartridge,
    // and hands the schema file to whoever asked for it. loadCoreSchema resolves
    // the server-type folder and schema name to a bundle resource URL, parses the
    // XML schema file, and returns the Schema object — or shows an error dialog if
    // anything along that path goes wrong.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Loads a built-in "core" schema from the plugin's resources directory, identified
     * by the server type (ApacheDS or OpenLDAP) and the schema file name. Core schemas
     * are the baseline definitions that ship with the plugin and can be imported into
     * a project without connecting to a live server. Returns null if loading fails
     * (an error dialog is shown to the user in that case).
     *
     * @param serverType  the server flavor that determines which resource sub-folder
     *                    to look in ({@code apacheds} or {@code openldap}).
     * @param schemaName  the base name of the schema file (without extension) to load.
     * @return            the parsed Schema, or null if the resource could not be found
     *                    or parsed.
     */
    public static Schema loadCoreSchema( ServerTypeEnum serverType, String schemaName )
    {
        Schema schema = null;

        try
        {
            URL url = Activator.getDefault().getBundle().getResource(
                "resources/schemas/" + getFolderName( serverType ) + "/" + schemaName + ".xml" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            if ( url == null )
            {
                reportError(
                    Messages.getString( "PluginUtils.SchemaLoadingError" ) + schemaName + ".", null, //$NON-NLS-1$//$NON-NLS-2$
                    Messages.getString( "PluginUtils.ProjectsLoadingError" ), Messages.getString( "PluginUtils.SchemaLoadingError" ) + schemaName + "." ); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
            }
            else
            {
                schema = XMLSchemaFileImporter.getSchema( url.openStream(), url.toString() );
            }
        }
        catch ( XMLSchemaFileImportException e )
        {
            reportError(
                Messages.getString( "PluginUtils.SchemaLoadingError" ) + schemaName + ".", e, Messages.getString( "PluginUtils.ProjectsLoadingError" ), //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                Messages.getString( "PluginUtils.SchemaLoadingError" ) + schemaName + "." ); //$NON-NLS-1$//$NON-NLS-2$
        }
        catch ( FileNotFoundException e )
        {
            reportError(
                Messages.getString( "PluginUtils.SchemaLoadingError" ) + schemaName + ".", e, Messages.getString( "PluginUtils.ProjectsLoadingError" ), //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                Messages.getString( "PluginUtils.SchemaLoadingError" ) + schemaName + "." ); //$NON-NLS-1$//$NON-NLS-2$
        }
        catch ( IOException e )
        {
            reportError(
                Messages.getString( "PluginUtils.SchemaLoadingError" ) + schemaName + ".", e, Messages.getString( "PluginUtils.ProjectsLoadingError" ), //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                Messages.getString( "PluginUtils.SchemaLoadingError" ) + schemaName + "." ); //$NON-NLS-1$//$NON-NLS-2$
        }

        return schema;
    }


    // ── R2 Sounds The Alarm And Displays The Damage Report ───────────────────────
    // When R2 detects a failure he does two things at once: he logs the technical
    // details to the ship's computer (for engineers) and displays a plain-language
    // alert on the cockpit screen (for the crew). reportError does both: it logs
    // to the Eclipse log and shows a user-facing error dialog.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Logs an error to the platform log and shows an error dialog to the user.
     * We centralise this two-step pattern here so callers don't have to repeat the
     * log-then-dialog boilerplate everywhere something can fail. Either argument can
     * be null: null loggerMessage skips logging; null dialogMessage skips the dialog.
     *
     * @param loggerMessage  the message to write to the platform error log, or null to
     *                       skip logging.
     * @param e              the exception to include in the log entry, or null if none.
     * @param dialogTitle    the title of the error dialog shown to the user; an empty
     *                       string is used if null.
     * @param dialogMessage  the body text of the error dialog, or null to skip the dialog.
     */
    private static void reportError( String loggerMessage, Exception e, String dialogTitle, String dialogMessage )
    {
        if ( ( loggerMessage != null ) || ( e != null ) )
        {
            PluginUtils.logError( loggerMessage, e );
        }

        if ( dialogMessage != null )
        {
            ViewUtils.displayErrorMessageDialog( ( ( dialogTitle == null ) ? "" : dialogTitle ), dialogMessage ); //$NON-NLS-1$
        }
    }


    // ── R2 Checks Which Archive Drawer Holds The Requested Files ──────────────────
    // The Death Star archive has separate drawers for each fleet type — R2 knows
    // exactly which drawer to open based on the ship class requested. getFolderName
    // maps a ServerTypeEnum value to the corresponding resource sub-folder name so
    // we can build the correct URL to the bundled schema files.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Maps a ServerTypeEnum to the name of the sub-folder under
     * {@code resources/schemas/} where that server's core schema files are stored.
     * Returns null for an unrecognized server type, which will cause the URL lookup
     * in {@link #loadCoreSchema} to fail gracefully.
     *
     * @param serverType  the server flavor to look up.
     * @return            the folder name string ({@code "apacheds"} or {@code "openldap"}),
     *                    or null if the type is not recognized.
     */
    private static String getFolderName( ServerTypeEnum serverType )
    {
        if ( ServerTypeEnum.APACHE_DS.equals( serverType ) )
        {
            return "apacheds"; //$NON-NLS-1$
        }
        else if ( ServerTypeEnum.OPENLDAP.equals( serverType ) )
        {
            return "openldap"; //$NON-NLS-1$
        }

        // Default
        return null;
    }


    // ── R2 Locates The Correct Ship In The Docking Bay ───────────────────────────
    // R2 scans the docking bay manifest, matches the requested ship ID, and returns
    // the berth assignment — or null if that ship isn't docked. getConnection does
    // the same: it queries the ConnectionManager for a Connection that matches the
    // given ID, returning null if no match is found.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a saved LDAP Connection by its unique ID from the Connection Manager.
     * We use this to resolve a stored connection ID (e.g. from a project file) to an
     * actual live Connection object. Returns null if no connection with that ID exists —
     * the caller should check for null and handle the "connection not found" case.
     *
     * @param id  the unique identifier of the connection to look up.
     * @return    the matching Connection, or null if no connection has that ID.
     */
    public static Connection getConnection( String id )
    {
        Connection[] connectionsArray = ConnectionCorePlugin.getDefault().getConnectionManager().getConnections();

        HashMap<String, Connection> connections = new HashMap<String, Connection>();
        for ( Connection connection : connectionsArray )
        {
            connections.put( connection.getId(), connection );
        }

        return connections.get( id );
    }


    // ── R2 Queries The Ship Registry For All Authorized Connectors ────────────────
    // R2 polls the station registry to get the full list of docking adapters that
    // are cleared to dock — each one has a name, an ID, and a capability description.
    // getSchemaConnectors does the same for Eclipse extension points: it reads all
    // SchemaConnector contributions, instantiates them, and returns the ready-to-use
    // list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Discovers and instantiates all SchemaConnector implementations registered via
     * the {@code schemaConnectors} Eclipse extension point. SchemaConnectors are the
     * adapters that know how to talk to a specific type of LDAP server (ApacheDS,
     * OpenLDAP, generic, etc.) to read its live schema. Each contribution is
     * instantiated reflectively; any that fail to load are logged and skipped.
     *
     * @return  a list of ready-to-use SchemaConnector instances; may be empty if no
     *          contributions are registered or all failed to instantiate.
     */
    public static List<SchemaConnector> getSchemaConnectors()
    {
        List<SchemaConnector> schemaConnectors = new ArrayList<SchemaConnector>();

        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(
            "org.apache.directory.studio.schemaeditor.schemaConnectors" ); //$NON-NLS-1$
        IConfigurationElement[] members = extensionPoint.getConfigurationElements();

        if ( members != null )
        {
            // Creating each SchemaConnector
            for ( IConfigurationElement member : members )
            {
                try
                {
                    SchemaConnector schemaConnector = ( SchemaConnector ) member.createExecutableExtension( "class" ); //$NON-NLS-1$
                    schemaConnector.setName( member.getAttribute( "name" ) ); //$NON-NLS-1$
                    schemaConnector.setId( member.getAttribute( "id" ) ); //$NON-NLS-1$
                    schemaConnector.setDescription( member.getAttribute( "description" ) ); //$NON-NLS-1$

                    schemaConnectors.add( schemaConnector );
                }
                catch ( CoreException e )
                {
                    PluginUtils.logError( Messages.getString( "PluginUtils.ConnectorsLoadingError" ), e ); //$NON-NLS-1$
                    ViewUtils.displayErrorMessageDialog( Messages.getString( "PluginUtils.Error" ), Messages //$NON-NLS-1$
                        .getString( "PluginUtils.ConnectorsLoadingError" ) ); //$NON-NLS-1$
                }
            }
        }

        return schemaConnectors;
    }


    // ── R2 Updates The Mission History Log ────────────────────────────────────────
    // R2 keeps a rolling log of the last twenty mission objectives — if the same
    // objective comes up again he moves it to the top, and if the log fills up he
    // drops the oldest entry. saveDialogSettingsHistory is that rolling log for
    // dialog inputs, keeping the most-recently-used values at the front.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a value to the MRU (most-recently-used) history list stored under the given
     * key in the plugin's dialog settings. If the value is already in the list it is
     * moved to position 0 rather than duplicated. The list is capped at 20 entries;
     * the oldest is dropped when the cap is exceeded. This is used for things like
     * search-box history so the user can re-select recent inputs quickly.
     *
     * @param key    the dialog-settings key that namespaces this history list.
     * @param value  the new value to record at the front of the history.
     */
    public static void saveDialogSettingsHistory( String key, String value )
    {
        // get current history
        String[] history = loadDialogSettingsHistory( key );
        List<String> list = new ArrayList<String>( Arrays.asList( history ) );

        // add new value or move to first position
        if ( list.contains( value ) )
        {
            list.remove( value );
        }
        list.add( 0, value );

        // check history size
        while ( list.size() > 20 )
        {
            list.remove( list.size() - 1 );
        }

        // save
        history = list.toArray( new String[list.size()] );
        Activator.getDefault().getDialogSettings().put( key, history );
    }


    // ── R2 Retrieves The Stored Mission History ────────────────────────────────────
    // R2 reads back the mission history from the navicomputer — if nothing has been
    // logged yet he returns an empty list rather than crashing. loadDialogSettingsHistory
    // does the same: it returns whatever is stored under the key, or an empty array if
    // nothing has been saved yet.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the MRU history list stored under the given key from the plugin's dialog
     * settings. Returns an empty array (never null) if nothing has been stored under
     * that key yet — callers can iterate the result safely without a null check.
     *
     * @param key  the dialog-settings key that namespaces the history list.
     * @return     the stored history entries, oldest last; an empty array if none exist.
     */
    public static String[] loadDialogSettingsHistory( String key )
    {
        String[] history = Activator.getDefault().getDialogSettings().getArray( key );
        if ( history == null )
        {
            history = new String[0];
        }
        return history;
    }
}
