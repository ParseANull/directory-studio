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
package org.apache.directory.studio.schemaeditor.model.schemachecker;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.directory.api.ldap.model.exception.LdapSchemaException;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.api.ldap.schema.manager.impl.DefaultSchemaManager;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.controller.ProjectsHandlerAdapter;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandlerAdapter;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandlerListener;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.model.schemamanager.SchemaEditorSchemaLoader;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;


// ── CLASS: SchemaChecker — Mace Windu Confronting Palpatine ──────────────────
// Mace Windu is the one Jedi disciplined enough to detect corruption the others
// miss.  When something in the LDAP schema changes, SchemaChecker is the one
// that runs the full re-validation sweep: it feeds the current schema through
// Apache Directory API's DefaultSchemaManager and maps every LdapSchemaException
// back to the specific SchemaObject that caused it.  It also runs its own
// secondary pass to catch warnings that the API doesn't flag — like schema
// objects with no human-readable name.  Listeners are notified after each pass
// so the Problems view can refresh itself.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Singleton that continuously validates the schema editor's working schema.
 * <p>
 * When modifications listening is enabled it registers a {@link SchemaHandlerListener}
 * that triggers a full re-check via an Eclipse {@link Job} whenever an attribute
 * type, object class, or schema is added, modified, or removed.
 * After each check the errors and warnings are indexed by source {@link SchemaObject}
 * so callers can query {@link #hasErrors(SchemaObject)} or {@link #getWarnings(SchemaObject)}
 * in O(1) time.
 * <p>
 * Think of this class as Mace Windu: relentless, methodical, and the first to
 * confront a problem that the rest of the system might politely ignore.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaChecker
{
    /** The SchemaChecker instance */
    private static SchemaChecker instance;

    /** The schema manager */
    private SchemaManager schemaManager;

    /** The errors map */
    private MultiValuedMap<Object, Object> errorsMap = new ArrayListValuedHashMap<>();

    /** The warnings list */
    private List<SchemaWarning> warningsList = new ArrayList<SchemaWarning>();

    /** The warnings map */
    private MultiValuedMap<Object, Object> warningsMap = new ArrayListValuedHashMap<>();

    /** The lock object used to synchronize accesses to the errors and warnings maps*/
    private static Object lock = new Object();

    /** The 'listening to modifications' flag*/
    private boolean listeningToModifications = false;

    /** The listeners List */
    private List<SchemaCheckerListener> listeners = new ArrayList<SchemaCheckerListener>();

    // ── Mace's Alert Network — Reacting to Every Schema Change ───────────────
    // Mace doesn't wait for a formal report; his inner SchemaHandlerListener
    // responds to every mutation event and schedules an immediate re-check.
    // ─────────────────────────────────────────────────────────────────────────
    /** The SchemaHandlerListener */
    private SchemaHandlerListener schemaHandlerListener = new SchemaHandlerAdapter()
    {
        public void attributeTypeAdded( AttributeType at )
        {
            synchronized ( this )
            {
                recheckWholeSchema();
            }
        }


        public void attributeTypeModified( AttributeType at )
        {
            synchronized ( this )
            {
                recheckWholeSchema();
            }
        }


        public void attributeTypeRemoved( AttributeType at )
        {
            synchronized ( this )
            {
                recheckWholeSchema();
            }
        }


        public void objectClassAdded( ObjectClass oc )
        {
            synchronized ( this )
            {
                recheckWholeSchema();
            }
        }


        public void objectClassModified( ObjectClass oc )
        {
            synchronized ( this )
            {
                recheckWholeSchema();
            }
        }


        public void objectClassRemoved( ObjectClass oc )
        {
            synchronized ( this )
            {
                recheckWholeSchema();
            }
        }


        public void schemaAdded( Schema schema )
        {
            synchronized ( this )
            {
                recheckWholeSchema();
            }
        }


        public void schemaRemoved( Schema schema )
        {
            synchronized ( this )
            {
                recheckWholeSchema();
            }
        }


        public void schemaRenamed( Schema schema )
        {
            // Nothing to do, this is a simple renaming
        }
    };


    // ── Mace Stands Guard from the Very Beginning ─────────────────────────────
    // The private constructor registers a ProjectsHandlerAdapter so that when
    // the open project changes, Mace detaches from the old project's events and
    // attaches to the new one — he's always watching the right senate session.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SchemaChecker.
     * Private — callers must use {@link #getInstance()}.
     * Registers a {@link ProjectsHandlerAdapter} so the schema handler listener
     * tracks project switches automatically.
     */
    private SchemaChecker()
    {
        Activator.getDefault().getProjectsHandler().addListener( new ProjectsHandlerAdapter()
        {
            public void openProjectChanged( Project oldProject, Project newProject )
            {
                if ( oldProject != null )
                {
                    oldProject.getSchemaHandler().removeListener( schemaHandlerListener );
                }

                if ( newProject != null )
                {
                    newProject.getSchemaHandler().addListener( schemaHandlerListener );
                }
            }
        } );
    }


    // ── Mace Answers the Council's Summons — The Singleton Accessor ───────────
    // Only one Mace Windu exists on the Jedi Council.  Returns the single shared
    // instance, creating it on first call.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the singleton SchemaChecker instance, creating it on first call.
     *
     * @return  the singleton SchemaChecker — never null
     */
    public static SchemaChecker getInstance()
    {
        if ( instance == null )
        {
            instance = new SchemaChecker();
        }

        return instance;
    }


    // ── Mace Opens His Eyes — Start Watching for Changes ─────────────────────
    // Mace enters an active watch; he registers the schema handler listener and
    // immediately runs a full re-check to establish a baseline.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Starts listening for schema modifications and triggers an initial full
     * re-check to populate the error and warning maps.
     * No-op if already listening.
     */
    public void enableModificationsListening()
    {
        synchronized ( this )
        {
            if ( !listeningToModifications )
            {
                Activator.getDefault().getSchemaHandler().addListener( schemaHandlerListener );
                listeningToModifications = true;
                recheckWholeSchema();
            }
        }
    }


    // ── Mace Steps Down from Active Duty ─────────────────────────────────────
    // Mace deregisters the schema handler listener; he stops reacting to changes.
    // The last computed error and warning state remains in the maps.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Stops listening for schema modifications.
     * The error and warning maps are not cleared; they retain the last computed state.
     * No-op if not currently listening.
     */
    public void disableModificationsListening()
    {
        synchronized ( this )
        {
            if ( listeningToModifications )
            {
                Activator.getDefault().getSchemaHandler().removeListener( schemaHandlerListener );
                listeningToModifications = false;
            }
        }
    }


    // ── Mace Calls for an Emergency Council Session ───────────────────────────
    // External callers (e.g. after a project import) can force an immediate
    // re-check without waiting for a modification event.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Forces an immediate full re-check of the schema regardless of whether
     * the checker is currently listening to modifications.
     */
    public void reload()
    {
        synchronized ( this )
        {
            recheckWholeSchema();
        }
    }


    // ── Mace Reports His Watch Status ─────────────────────────────────────────
    // Returns whether Mace is currently on active duty — i.e. whether the
    // schema handler listener is registered and schema changes will trigger
    // automatic re-checks.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the SchemaChecker is currently listening to schema modifications,
     * false if not.
     *
     * @return  true if listening, false otherwise
     */
    public boolean isListeningToModifications()
    {
        return listeningToModifications;
    }


    // ── Mace Runs the Full Validation Sweep ───────────────────────────────────
    // Schedules an Eclipse Job that feeds the entire schema through a fresh
    // DefaultSchemaManager, then calls updateErrorsAndWarnings() and notifyListeners().
    // Running as a Job keeps the UI thread free during the (potentially slow)
    // schema load and validation step.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Schedules a background {@link Job} that rebuilds the {@link SchemaManager},
     * loads all schemas, and then calls {@link #updateErrorsAndWarnings()} and
     * {@link #notifyListeners()}.
     */
    private void recheckWholeSchema()
    {
        Job job = new Job( "Checking Schema" )
        {
            protected IStatus run( IProgressMonitor monitor )
            {
                // Checks the whole schema via the schema manager
                try
                {
                    schemaManager = new DefaultSchemaManager( new SchemaEditorSchemaLoader() );
                    schemaManager.loadAllEnabled();
                }
                catch ( Exception e )
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // Updates errors and warnings
                updateErrorsAndWarnings();

                // Notify listeners
                notifyListeners();

                monitor.done();

                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }


    // ── Mace Updates His Intelligence Files ───────────────────────────────────
    // Clears the old errors and warnings maps, re-indexes the schema manager's
    // error list, then re-creates and indexes all warnings.
    // Holds the lock so readers always see a consistent snapshot.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Clears and rebuilds the {@code errorsMap}, {@code warningsList}, and
     * {@code warningsMap} from the current state of the schema manager.
     * Synchronized on {@code lock} so readers cannot observe a partial update.
     */
    private synchronized void updateErrorsAndWarnings()
    {
        synchronized ( lock )
        {
            // Errors
            errorsMap.clear();
            indexErrors();

            // Warnings
            createWarnings();
            warningsMap.clear();
            indexWarnings();
        }
    }


    // ── Mace Maps Each LdapSchemaException to Its Source Object ──────────────
    // The schema manager reports errors as Throwable objects; Mace inspects each
    // one, extracts the source SchemaObject, and maps it in errorsMap so callers
    // can ask "does THIS object have errors?" in O(1).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Iterates the schema manager's error list, extracts the source
     * {@link SchemaObject} from each {@link LdapSchemaException}, and puts the
     * exception into {@code errorsMap} keyed by the live schema handler instance
     * (not the transient object from the schema manager).
     */
    private void indexErrors()
    {
        for ( Throwable error : schemaManager.getErrors() )
        {
            if ( error instanceof LdapSchemaException )
            {
                LdapSchemaException ldapSchemaException = ( LdapSchemaException ) error;
                SchemaObject source = ldapSchemaException.getSourceObject();
                if ( source != null )
                {
                    SchemaHandler schemaHandler = Activator.getDefault().getSchemaHandler();

                    if ( source instanceof AttributeType )
                    {
                        source = schemaHandler.getAttributeType( source.getOid() );
                    }
                    else if ( source instanceof LdapSyntax )
                    {
                        source = schemaHandler.getSyntax( source.getOid() );
                    }
                    else if ( source instanceof MatchingRule )
                    {
                        source = schemaHandler.getMatchingRule( source.getOid() );
                    }
                    else if ( source instanceof ObjectClass )
                    {
                        source = schemaHandler.getObjectClass( source.getOid() );
                    }

                    errorsMap.put( source, ldapSchemaException );
                }
            }
        }
    }


    // ── Mace Looks for Unnamed Suspects ───────────────────────────────────────
    // The schema manager won't flag a missing alias as an error — it's valid LDAP.
    // But Mace notices: any attribute type or object class with no human-readable
    // name is suspicious.  He records a NoAliasWarning for each one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Clears the warnings list and re-populates it by checking every attribute
     * type and object class in the schema handler for the absence of aliases.
     */
    private void createWarnings()
    {
        // Clearing previous warnings
        warningsList.clear();

        // Getting the schema handler to check for schema objects without names (aliases)
        SchemaHandler schemaHandler = Activator.getDefault().getSchemaHandler();

        if ( schemaHandler != null )
        {
            // Checking attribute types
            for ( AttributeType attributeType : schemaHandler.getAttributeTypes() )
            {
                checkSchemaObjectNames( attributeType );
            }

            // Checking object classes
            for ( ObjectClass objectClass : schemaHandler.getObjectClasses() )
            {
                checkSchemaObjectNames( objectClass );
            }
        }
    }


    // ── Mace Checks Whether a Schema Object Has a Name ───────────────────────
    // If the object's names list is null or empty, it has no alias — Mace adds
    // a NoAliasWarning pointing at it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks the names of the given schema object.
     * If the names list is null or empty, adds a {@link NoAliasWarning} for it.
     *
     * @param schemaObject  the schema object to check
     */
    private void checkSchemaObjectNames( SchemaObject schemaObject )
    {
        if ( ( schemaObject.getNames() == null ) || ( schemaObject.getNames().size() == 0 ) )
        {
            warningsList.add( new NoAliasWarning( schemaObject ) );
        }
    }


    // ── Mace Organises His Warning Dossier ────────────────────────────────────
    // Populates warningsMap from warningsList so callers can look up warnings
    // by source SchemaObject in O(1).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Re-indexes the {@code warningsList} into {@code warningsMap}
     * keyed by the warning's source {@link SchemaObject}.
     */
    private void indexWarnings()
    {
        for ( SchemaWarning warning : warningsList )
        {
            warningsMap.put( warning.getSource(), warning );
        }
    }


    // ── Mace Reports All Known Threats ────────────────────────────────────────
    // Returns the full list of errors from the schema manager.  If the schema
    // manager has never run yet, returns an empty list rather than null.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns all errors reported by the schema manager on the last check.
     * Returns an empty list if the schema manager has not yet run.
     *
     * @return  the error list — never null
     */
    public List<Throwable> getErrors()
    {
        if ( schemaManager != null )
        {
            return schemaManager.getErrors();
        }
        else
        {
            return new ArrayList<Throwable>();
        }
    }


    // ── Mace Returns the Full Warnings File ───────────────────────────────────
    // Returns the complete list of all warnings accumulated during the last check.
    // Holds the lock to guarantee a consistent snapshot.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns all warnings accumulated during the last schema check.
     * Synchronized on the internal lock to guarantee a consistent snapshot.
     *
     * @return  the warnings list — never null
     */
    public List<SchemaWarning> getWarnings()
    {
        synchronized ( lock )
        {
            return warningsList;
        }
    }


    // ── Mace Adds a New Observer to the Council ───────────────────────────────
    // Registers a SchemaCheckerListener that will be called after each re-check.
    // De-duplicated: adding the same listener twice has no effect.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers a {@link SchemaCheckerListener} to be notified after each
     * schema re-check.  Adding the same listener twice has no effect.
     *
     * @param listener  the listener to add — must not be null
     */
    public void addListener( SchemaCheckerListener listener )
    {
        if ( !listeners.contains( listener ) )
        {
            listeners.add( listener );
        }
    }


    // ── Mace Releases a Council Observer ──────────────────────────────────────
    // Deregisters a previously added listener.  No-op if the listener was
    // never registered.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes a previously registered {@link SchemaCheckerListener}.
     * No-op if the listener is not currently registered.
     *
     * @param listener  the listener to remove
     */
    public void removeListener( SchemaCheckerListener listener )
    {
        listeners.remove( listener );
    }


    // ── Mace Issues the Council's Verdict ─────────────────────────────────────
    // After a re-check completes Mace calls each registered listener so UI
    // components can refresh their error/warning displays.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Calls {@link SchemaCheckerListener#schemaCheckerUpdated()} on every
     * registered listener.  Called from the background Job thread after each
     * re-check completes.
     */
    private void notifyListeners()
    {
        for ( SchemaCheckerListener listener : listeners )
        {
            listener.schemaCheckerUpdated();
        }
    }


    // ── Mace Checks the Dossier for a Specific Suspect ────────────────────────
    // Returns only the errors associated with a specific SchemaObject — the UI
    // uses this to decorate individual tree nodes with error icons.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the errors associated with the given schema object.
     * Synchronized on the internal lock for consistency.
     *
     * @param so  the schema object to look up
     * @return    the list of errors — may be empty but not null
     */
    public List<?> getErrors( SchemaObject so )
    {
        synchronized ( lock )
        {
            return ( List<?> ) errorsMap.get( so );
        }
    }


    // ── Mace Confirms Whether a Specific Suspect Is Wanted ────────────────────
    // Convenience helper over getErrors(SchemaObject) that returns a boolean.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the given schema object has any errors on the last check.
     *
     * @param so  the schema object to query
     * @return    true if at least one error is associated with it
     */
    public boolean hasErrors( SchemaObject so )
    {
        List<?> errors = getErrors( so );

        if ( errors == null )
        {
            return false;
        }
        else
        {
            return errors.size() > 0;
        }
    }


    // ── Mace Checks the Warning File for a Specific Suspect ───────────────────
    // Returns only the warnings associated with a specific SchemaObject.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the warnings associated with the given schema object.
     *
     * @param so  the schema object to look up
     * @return    the list of warnings — may be empty but not null
     */
    @SuppressWarnings("unchecked")
    public List<Object> getWarnings( SchemaObject so )
    {
        return ( List<Object> ) warningsMap.get( so );
    }


    // ── Mace Confirms Whether a Specific Suspect Has a Warning ────────────────
    // Convenience helper over getWarnings(SchemaObject) that returns a boolean.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the given schema object has any warnings on the last check.
     *
     * @param so  the schema object to query
     * @return    true if at least one warning is associated with it
     */
    public boolean hasWarnings( SchemaObject so )
    {
        List<?> warnings = getWarnings( so );

        if ( warnings == null )
        {
            return false;
        }
        else
        {
            return warnings.size() > 0;
        }
    }

}
