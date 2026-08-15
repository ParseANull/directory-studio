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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.ObjectClassTypeEnum;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.eclipse.core.runtime.Status;

import org.apache.directory.studio.templateeditor.model.Template;


// ── CLASS: EntryTemplatePluginUtils — HAN SOLO'S TOOLBOX OF TRICKS ───────────────
// Han Solo always has a trick up his sleeve: when the hyperdrive fails, he jury-rigs
// it with whatever is in the Falcon's toolbox. He doesn't own a star destroyer — he
// just makes things work with what he has. This class is that toolbox: a collection
// of static utilities that any part of the plugin can reach into. Logging helpers,
// file copy routines, and — critically — the LDAP schema walking logic that figures
// out which templates match a given LDAP entry. None of these belong in any
// particular class, so they all live here.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Static utility class for the Entry Template plugin. Provides logging helpers
 * (delegating to Eclipse's platform log), a file-copy utility, and the key
 * algorithm that resolves which {@link Template}s apply to a given
 * {@link IEntry} by walking the LDAP object-class hierarchy.
 * Think of this class as Han Solo's toolbox on the Falcon: miscellaneous,
 * indispensable, and available to anyone who needs it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryTemplatePluginUtils
{
    /** The line separator */
    public static final String LINE_SEPARATOR = System.getProperty( "line.separator" ); //$NON-NLS-1$

    /** The default schema */
    private static final Schema DEFAULT_SCHEMA = Schema.DEFAULT_SCHEMA;


    // ── LOG ERROR: HAN REPORTS A CRITICAL SYSTEM FAILURE ─────────────────────────
    // Han slaps the Falcon's console and says "Something's definitely wrong" —
    // he doesn't know the exact cause but he makes sure everyone hears about it.
    // This method pipes an ERROR-level log entry to Eclipse's platform log so
    // operations teams and support can diagnose problems after the fact.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Logs a message at ERROR severity using Eclipse's platform log.
     * Use this when something has failed and the user's workflow is broken.
     *
     * <p>For example — Han reports a hyperdrive failure:</p>
     * <pre>
     *   logError(e, "Template file {0} could not be parsed: {1}",
     *             filePath, e.getMessage());
     * </pre>
     *
     * @param exception  the root cause; may be {@code null} if there is no exception
     * @param message    a {@link MessageFormat} pattern describing what went wrong
     * @param args       substitution arguments for the message pattern
     */
    public static void logError( Throwable exception, String message, Object... args )
    {
        EntryTemplatePlugin.getDefault().getLog().log(
            new Status( Status.ERROR, EntryTemplatePlugin.getDefault().getBundle().getSymbolicName(), Status.OK,
                MessageFormat.format( message, args ), exception ) );
    }


    // ── LOG WARNING: HAN MUTTERS "I HAVE A BAD FEELING ABOUT THIS" ───────────────
    // Han senses something off — the sensors are flickering but nothing has blown
    // up yet. A WARNING means we're still operational but someone should look at
    // this before it becomes an ERROR.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Logs a message at WARNING severity using Eclipse's platform log.
     * Use when something unexpected happened but the plugin can continue working.
     *
     * <p>For example — Han notices the sensors flickering:</p>
     * <pre>
     *   logWarning(null, "Template {0} has no title; using id as fallback.", id);
     * </pre>
     *
     * @param exception  the root cause; may be {@code null}
     * @param message    a {@link MessageFormat} pattern describing the concern
     * @param args       substitution arguments for the message pattern
     */
    public static void logWarning( Throwable exception, String message, Object... args )
    {
        EntryTemplatePlugin.getDefault().getLog().log(
            new Status( Status.WARNING, EntryTemplatePlugin.getDefault().getBundle().getSymbolicName(), Status.OK,
                MessageFormat.format( message, args ), exception ) );
    }


    // ── LOG INFO: HAN GIVES A ROUTINE STATUS UPDATE ───────────────────────────────
    // Han checks in over the comm: "We've made the jump to hyperspace, all systems
    // normal." INFO messages are for informational milestones — nothing is wrong,
    // we're just narrating what happened.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Logs a message at INFO severity using Eclipse's platform log.
     * Use for routine milestones that are worth noting in the log but indicate
     * normal operation.
     *
     * <p>For example — Han confirms a successful jump:</p>
     * <pre>
     *   logInfo(null, "Loaded {0} templates from extension points.", count);
     * </pre>
     *
     * @param exception  the root cause; may be {@code null}
     * @param message    a {@link MessageFormat} pattern for the informational message
     * @param args       substitution arguments for the message pattern
     */
    public static void logInfo( Throwable exception, String message, Object... args )
    {
        EntryTemplatePlugin.getDefault().getLog().log(
            new Status( Status.INFO, EntryTemplatePlugin.getDefault().getBundle().getSymbolicName(), Status.OK,
                MessageFormat.format( message, args ), exception ) );
    }


    // ── LOG OK: HAN GIVES THE ALL-CLEAR SIGNAL ───────────────────────────────────
    // "Everything's fine up here. How are you?" — Han's all-clear. An OK-level
    // log entry means we completed an operation successfully and we're recording
    // it for audit purposes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Logs a message at OK (success) severity using Eclipse's platform log.
     * Rarely needed — use this to record successful completion of significant
     * operations that deserve an audit trail.
     *
     * <p>For example — Han signals all-clear after a successful operation:</p>
     * <pre>
     *   logOk(null, "Template {0} was successfully imported.", templateId);
     * </pre>
     *
     * @param exception  usually {@code null} for an OK status
     * @param message    a {@link MessageFormat} pattern for the success message
     * @param args       substitution arguments for the message pattern
     */
    public static void logOk( Throwable exception, String message, Object... args )
    {
        EntryTemplatePlugin.getDefault().getLog().log(
            new Status( Status.OK, EntryTemplatePlugin.getDefault().getBundle().getSymbolicName(), Status.OK,
                MessageFormat.format( message, args ), exception ) );
    }


    // ── COPY FILE (FILE): HAN TRANSFERS CARGO BETWEEN TWO SHIPS ─────────────────
    // Han loads cargo onto the Falcon from one freighter and drops it at another
    // port. Here we open the streams from both File objects and delegate to the
    // stream-based overload — just a convenient wrapper so callers don't have to
    // open streams themselves.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Copies the contents of {@code source} to {@code destination} as a raw byte
     * stream. Convenience overload of {@link #copyFile(InputStream, OutputStream)}.
     *
     * <p>For example — Han transfers a cargo pod:</p>
     * <pre>
     *   copyFile(templateFile, destinationFile);
     *   // "Cargo transferred, Captain."
     * </pre>
     *
     * @param source       the file to read from
     * @param destination  the file to write to (created or overwritten)
     * @throws IOException  if the source can't be read or the destination can't be written
     */
    public static void copyFile( File source, File destination ) throws IOException
    {
        copyFile( new FileInputStream( source ), new FileOutputStream( destination ) );
    }


    // ── COPY FILE (STREAMS): HAN PUMPS FUEL FROM ONE TANK TO ANOTHER ─────────────
    // Han rigs a hose between two tanks and pumps until empty — 1 KB at a time
    // so we don't blow out memory on huge files. This is the workhorse behind
    // the File-based overload above.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Copies all bytes from {@code inputStream} to {@code outputStream} using a
     * 1-KB buffer. Neither stream is closed when we're done — the caller owns them.
     *
     * <p>For example — Han pumps fuel between tanks:</p>
     * <pre>
     *   copyFile(templateInputStream, pluginFolderOutputStream);
     *   // "Full tank, ready to fly."
     * </pre>
     *
     * @param inputStream   the source byte stream
     * @param outputStream  the destination byte stream
     * @throws IOException  if reading or writing fails mid-copy
     */
    public static void copyFile( InputStream inputStream, OutputStream outputStream ) throws IOException
    {
        byte[] buf = new byte[1024];
        int i = 0;
        while ( ( i = inputStream.read( buf ) ) != -1 )
        {
            outputStream.write( buf, 0, i );
        }
    }


    // ── GET MATCHING TEMPLATES: HAN RUNS A RECON PASS ON THE TARGET ──────────────
    // Han scopes out the target base from orbit, figures out what type of facility
    // it is, and radios back which strike packages are appropriate. Here we examine
    // the LDAP entry's object classes and return all templates whose structural
    // class matches (walking up the class hierarchy if needed).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of {@link Template}s that are applicable to the given
     * LDAP {@link IEntry}. We look at the entry's object classes, determine the
     * most-specific structural class via schema walking, and match against the
     * registered template registry. An empty list means no template applies.
     *
     * <p>For example — Han identifies which strike packages fit the target:</p>
     * <pre>
     *   List&lt;Template&gt; templates = getMatchingTemplates(entry);
     *   // returns [UserAccountTemplate, PersonTemplate] for an inetOrgPerson entry
     * </pre>
     *
     * @param entry  the LDAP entry to find templates for; {@code null} returns empty list
     * @return a mutable list of matching templates, never {@code null}
     */
    public static List<Template> getMatchingTemplates( IEntry entry )
    {
        if ( entry != null )
        {
            // Looking for the highest (most specialized one) structural object class in the entry
            ObjectClass highestStructuralObjectClass = getHighestStructuralObjectClassFromEntry( entry );
            if ( highestStructuralObjectClass != null )
            {
                // We were able to determine the highest object class in the entry.

                // Based on that information, we will use the entry's schema to retrieve the list of matching templates
                return getTemplatesFromHighestObjectClass( highestStructuralObjectClass, entry.getBrowserConnection()
                    .getSchema() );
            }
            else
            {
                // We were not able to determine the highest object class in the entry.
                // This means that either the schema information we received from the server is not sufficient,
                // or the list of object classes in the entry is not complete.

                // In that case we can't use the schema information to determine the list of templates.
                // Instead we're going to gather all the templates associated with each object class description.
                return getTemplatesFromObjectClassDescriptions( entry.getObjectClassDescriptions() );
            }
        }

        return new ArrayList<Template>();
    }


    // ── GET HIGHEST STRUCTURAL OC: HAN IDENTIFIES THE COMMAND STRUCTURE ──────────
    // Han scouts an Imperial base and works out who's really in charge — not just
    // the officers visible on patrol, but the commanding general behind the scenes.
    // In LDAP terms, an entry may have many structural object classes but only one
    // is at the top of the inheritance chain; we need that one to pick the right
    // template.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Walks the entry's object-class list and finds the single most-specialized
     * structural object class (the "leaf" in the inheritance tree). Uses a
     * candidate-elimination approach: we start with all structural classes and
     * remove any that are superiors of another candidate until only one remains.
     *
     * <p>For example — Han finds who's really in command:</p>
     * <pre>
     *   // Entry has: [top, person, organizationalPerson, inetOrgPerson]
     *   // Returns: inetOrgPerson  (the most specialized structural class)
     * </pre>
     *
     * @param entry  the LDAP entry; never {@code null}
     * @return the most-specialized structural {@link ObjectClass}, or {@code null}
     *         if the schema is insufficient to resolve it
     */
    private static ObjectClass getHighestStructuralObjectClassFromEntry( IEntry entry )
    {
        if ( entry != null )
        {
            if ( ( entry.getBrowserConnection() != null ) && ( entry.getBrowserConnection().getSchema() != null ) )
            {
                // Getting the schema from the entry
                Schema schema = entry.getBrowserConnection().getSchema();

                // Getting object class descriptions
                Collection<ObjectClass> objectClassDescriptions = entry.getObjectClassDescriptions();
                if ( objectClassDescriptions != null )
                {
                    // Creating the candidates list based on the initial list
                    List<ObjectClass> candidatesList = new ArrayList<ObjectClass>();

                    // Adding each structural object class description to the list
                    for ( ObjectClass objectClassDescription : objectClassDescriptions )
                    {
                        if ( objectClassDescription.getType() == ObjectClassTypeEnum.STRUCTURAL )
                        {
                            candidatesList.add( objectClassDescription );
                        }
                    }

                    // Looping on the given collection of ObjectClassDescription until the end of the list,
                    // or until the candidates list is reduced to one.
                    Iterator<ObjectClass> iterator = objectClassDescriptions.iterator();
                    while ( ( candidatesList.size() > 1 ) && ( iterator.hasNext() ) )
                    {
                        ObjectClass ocd = iterator.next();
                        removeSuperiors( ocd, candidatesList, schema );
                    }

                    // Looking if we've found the highest object class description
                    if ( candidatesList.size() == 1 )
                    {
                        return candidatesList.get( 0 );
                    }
                }
            }
        }

        return null;
    }


    // ── REMOVE SUPERIORS: HAN ELIMINATES ANYONE WHO OUTRANKS THE TARGET ──────────
    // If Han's trying to identify the head of a cell, he removes from his list
    // anyone who reports *to* that person — because that person is clearly not the
    // top dog. This recursive helper removes a class's superior (parent) from the
    // candidate list so that more-specific subclasses can rise to the top.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Recursively removes {@code ocd}'s superior classes (and their superiors)
     * from {@code ocdList}. This eliminates "parent" classes so that only the
     * most-specialized (leaf) class remains in the candidates list.
     *
     * <p>For example — Han eliminates anyone who outranks the target:</p>
     * <pre>
     *   // If inetOrgPerson is a candidate, we remove person, organizationalPerson,
     *   // and top from the list — they are all superiors (parents) of inetOrgPerson.
     * </pre>
     *
     * @param ocd      the object class whose superiors we want to remove
     * @param ocdList  the mutable candidate list to prune
     * @param schema   the schema used to resolve superior OIDs to objects
     */
    private static void removeSuperiors( ObjectClass ocd, List<ObjectClass> ocdList, Schema schema )
    {
        if ( ocd != null )
        {
            for ( String superior : ocd.getSuperiorOids() )
            {
                // Getting the ObjectClassDescription associated with the superior
                ObjectClass superiorOcd = getObjectClass( superior, schema );

                // Removing it from the list and recursively removing its superiors
                ocdList.remove( superiorOcd );
                removeSuperiors( superiorOcd, ocdList, schema );
            }
        }
    }


    // ── GET TEMPLATES FROM HIGHEST OC: HAN BREADTH-FIRST SEARCHES THE CHAIN ──────
    // Han starts at the command centre and fans out in concentric rings — he checks
    // this level, then the one above it, then above that — until he's covered the
    // whole chain of command. That's exactly what the BFS here does: start at the
    // most-specialized class and walk up the inheritance hierarchy, collecting
    // matching templates at every level.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Collects all templates that match {@code objectClassDescription} or any of
     * its ancestors, using a breadth-first traversal of the object-class hierarchy.
     * More-specific classes are checked first, so their templates appear earlier
     * in the returned list.
     *
     * <p>For example — Han fans out from HQ through the whole chain:</p>
     * <pre>
     *   // Start at inetOrgPerson → find "User Account" template
     *   // Move up to organizationalPerson → find "Staff Record" template
     *   // Move up to person → find nothing
     *   // Returns [UserAccountTemplate, StaffRecordTemplate]
     * </pre>
     *
     * @param objectClassDescription  the most-specialized structural class to start from
     * @param schema                  the schema used to resolve superior OIDs
     * @return a list of matching templates in most-specific-first order
     */
    private static List<Template> getTemplatesFromHighestObjectClass( ObjectClass objectClassDescription,
        Schema schema )
    {
        // Creating a set to hold all the matching templates
        List<Template> matchingTemplates = new ArrayList<Template>();

        // Getting the templates manager
        TemplatesManager manager = EntryTemplatePlugin.getDefault().getTemplatesManager();

        // Getting the list of all the available templates
        Template[] templates = manager.getTemplates();

        // Creating a MultiValueMap that holds the templates ordered by ObjectClassDescription object
        MultiValuedMap<ObjectClass, Template> templatesByOcd = new ArrayListValuedHashMap<>();

        // Populating this map
        for ( Template template : templates )
        {
            templatesByOcd.put( getObjectClass( template.getStructuralObjectClass(), schema ), template );
        }

        // Initializing the LIFO queue with the highest ObjectClassDescription object
        LinkedList<ObjectClass> ocdQueue = new LinkedList<ObjectClass>();
        ocdQueue.add( objectClassDescription );

        // Looking if we need to test a new ObjectClassDescription object
        while ( !ocdQueue.isEmpty() )
        {
            // Dequeuing the last object for testing
            ObjectClass currentOcd = ocdQueue.removeLast();

            // Adds the templates for the current object class description to the list of matching templates
            addTemplatesForObjectClassDescription( currentOcd, matchingTemplates, manager );

            // Adding each superior object to the queue
            List<String> currentOcdSups = currentOcd.getSuperiorOids();
            if ( currentOcdSups != null )
            {
                for ( String currentOcdSup : currentOcdSups )
                {
                    ocdQueue.addFirst( getObjectClass( currentOcdSup, schema ) );
                }
            }
        }

        return matchingTemplates;
    }


    // ── GET TEMPLATES FROM OC DESCRIPTIONS: HAN CHECKS EVERY CONTACT ────────────
    // When Han can't figure out the chain of command, he just calls every contact
    // in his datapad and asks "do you know anything about this?" — it's less
    // precise but covers all the bases. Similarly, when the schema hierarchy is
    // unavailable we fall back to checking every object class directly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Collects templates by checking each object class in {@code objectClasses}
     * individually, without trying to walk the inheritance hierarchy. Used as a
     * fallback when the schema is insufficient to determine the highest class.
     *
     * <p>For example — Han checks every contact in his datapad:</p>
     * <pre>
     *   // objectClasses = [top, person, inetOrgPerson]
     *   // For each, ask: "do you have a matching template?"
     *   // Collect all positives into one list.
     * </pre>
     *
     * @param objectClasses  the object class descriptions to check; may be {@code null}
     * @return a list of all matching templates, or {@code null} if input is {@code null}
     */
    private static List<Template> getTemplatesFromObjectClassDescriptions(
        Collection<ObjectClass> objectClasses )
    {
        if ( objectClasses != null )
        {
            // Creating a set to hold all the matching templates
            List<Template> matchingTemplates = new ArrayList<Template>();

            // Getting the templates manager
            TemplatesManager manager = EntryTemplatePlugin.getDefault().getTemplatesManager();

            for ( ObjectClass objectClassDescription : objectClasses )
            {
                // Adds the templates for the current object class description to the list of matching templates
                addTemplatesForObjectClassDescription( objectClassDescription, matchingTemplates, manager );
            }

            return matchingTemplates;
        }

        return null;
    }


    // ── ADD TEMPLATES FOR OCD: HAN CHECKS ONE CONTACT AND LOGS THE RESULT ────────
    // Han radios one specific contact, gets whatever templates they know about,
    // and adds the unique ones to the running list — duplicates get skipped, the
    // default template goes first, and disabled templates are excluded.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Resolves all names and OIDs for {@code ocd} and, for each, asks the manager
     * for the default template and the full list of templates. Adds any new ones
     * (not already in {@code matchingTemplates}, enabled, not a duplicate) to the
     * list. The default template is always added first.
     *
     * <p>For example — Han logs one contact's intelligence:</p>
     * <pre>
     *   // ocd = inetOrgPerson (OID: 2.16.840.1.113730.3.2.2)
     *   // Add "User Account" template (default) → check for others → add enabled ones
     * </pre>
     *
     * @param ocd              the object class description to look up
     * @param matchingTemplates the accumulator list; templates are appended here
     * @param manager           the template registry to query
     */
    private static void addTemplatesForObjectClassDescription( ObjectClass ocd,
        List<Template> matchingTemplates, TemplatesManager manager )
    {
        // Creating a list of containing the names and OID of the current ObjectClassDescription object
        List<String> namesAndOid = new ArrayList<String>();
        for ( String name : ocd.getNames() )
        {
            namesAndOid.add( name );
        }
        String currentOcdOid = ocd.getOid();
        if ( ( currentOcdOid != null ) && ( !"".equals( currentOcdOid ) ) ) //$NON-NLS-1$
        {
            namesAndOid.add( currentOcdOid );
        }

        // Looping on the names and OID to find all corresponding templates
        for ( String nameOrOid : namesAndOid )
        {
            // Getting the default template and complete list of templates for the given name or OID
            Template currentOcdDefaultTemplate = manager.getDefaultTemplate( nameOrOid );
            List<Template> currentOcdTemplates = manager.getTemplatesByObjectClass( nameOrOid );

            // Adding the default template
            if ( currentOcdDefaultTemplate != null )
            {
                if ( !matchingTemplates.contains( currentOcdDefaultTemplate ) )
                {
                    matchingTemplates.add( currentOcdDefaultTemplate );
                }
            }

            // Adding the other templates
            if ( currentOcdTemplates != null )
            {
                for ( Template template : currentOcdTemplates )
                {
                    // Adding the template only if it is different from the default one (which is already added)
                    if ( ( !template.equals( currentOcdDefaultTemplate ) ) && ( manager.isEnabled( template ) )
                        && ( !matchingTemplates.contains( template ) ) )
                    {
                        matchingTemplates.add( template );
                    }
                }
            }
        }
    }


    // ── GET OC FROM DEFAULT SCHEMA: HAN CHECKS THE FALCON'S OWN DATABANKS ────────
    // The Falcon carries a copy of the galactic registry — Han can look up a planet
    // or ship by name even when he's out of comm range. This is the public version
    // of the lookup that uses our own bundled default schema rather than the live
    // server schema.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up an {@link ObjectClass} by name or OID in the plugin's built-in
     * default schema. If nothing is found, a synthetic ObjectClass is created
     * with the given name so the caller always gets a non-null result.
     *
     * <p>For example — Han checks the Falcon's onboard databanks:</p>
     * <pre>
     *   ObjectClass oc = getObjectClassDescriptionFromDefaultSchema("inetOrgPerson");
     *   // returns the known inetOrgPerson descriptor from the default schema
     * </pre>
     *
     * @param nameOrOid  the object class name (e.g. "inetOrgPerson") or numeric OID
     * @return the resolved {@link ObjectClass}; never {@code null}
     */
    public static ObjectClass getObjectClassDescriptionFromDefaultSchema( String nameOrOid )
    {
        return getObjectClass( nameOrOid, DEFAULT_SCHEMA );
    }


    // ── GET OBJECT CLASS: HAN CHECKS THE SPECIFIC SCHEMA DATABASE ────────────────
    // Han queries a specific port's trading registry — if they have a record for
    // the ship or cargo he's looking for, great; if not, he invents a placeholder
    // entry so the rest of the code doesn't explode on a null.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up an {@link ObjectClass} in the given {@code schema}. If the schema
     * is {@code null} or the class isn't registered, creates and returns a
     * synthetic {@link ObjectClass} with a lowercase version of {@code nameOrOid}
     * as its single name. The synthetic object is a safe stand-in that lets
     * downstream code continue without NPEs.
     *
     * <p>For example — Han queries a port registry, invents a placeholder if missing:</p>
     * <pre>
     *   ObjectClass oc = getObjectClass("unknownClass", serverSchema);
     *   // If not found: returns a synthetic ObjectClass named "unknownclass"
     * </pre>
     *
     * @param nameOrOid  the class name or OID to look up
     * @param schema     the schema to search; may be {@code null}
     * @return the found or synthetic {@link ObjectClass}; never {@code null}
     */
    private static ObjectClass getObjectClass( String nameOrOid, Schema schema )
    {
        ObjectClass ocd = null;

        // Looking for the object class description in the given schema
        if ( schema != null )
        {
            ocd = schema.getObjectClassDescription( nameOrOid );
        }

        // Creating a new object class description if none could be found in the given schema
        if ( ocd == null )
        {
            ocd = new ObjectClass( null );
            ocd.setNames( Arrays.asList( new String[]
                { nameOrOid.toLowerCase() } ) );
        }

        return ocd;
    }
}
