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
package org.apache.directory.studio.schemaeditor.view.wizards;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.directory.api.ldap.model.schema.AbstractSchemaObject;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.common.ui.dialogs.MessageDialogWithTextarea;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.view.wizards.MergeSchemasSelectionWizardPage.AttributeTypeFolder;
import org.apache.directory.studio.schemaeditor.view.wizards.MergeSchemasSelectionWizardPage.AttributeTypeWrapper;
import org.apache.directory.studio.schemaeditor.view.wizards.MergeSchemasSelectionWizardPage.ObjectClassFolder;
import org.apache.directory.studio.schemaeditor.view.wizards.MergeSchemasSelectionWizardPage.ObjectClassWrapper;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;


// ── CLASS: MergeSchemasWizard — Yoda Lifting Luke's X-Wing From The Swamp ────
// On Dagobah, Yoda stands on the shore, concentrates hard, and lifts the entire
// X-wing out of the murky swamp water — taking something heavy and submerged from
// one place and depositing it cleanly somewhere else.  That's exactly what we do:
// we pull schema objects out of source projects and merge them into the target
// project, handling all the recursive dependency lifting along the way.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The wizard that orchestrates merging selected schema objects from other projects
 * into the currently open schema project.
 * It spans two pages: a selection page (pick what to merge) and an options page
 * (choose merge behaviour flags).  The heavy lifting — cloning attribute types and
 * object classes, resolving dependencies, handling conflicts — happens in
 * {@link #performFinish()}.
 * Think of this class as Yoda lifting the X-wing: it gathers everything selected,
 * lifts each schema object out of its source project, and sets it down cleanly in
 * the target project, recursing into dependencies as needed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MergeSchemasWizard extends Wizard implements IImportWizard
{
    // The pages of the wizard
    private MergeSchemasSelectionWizardPage selectionPage;
    private MergeSchemasOptionsWizardPage optionsPage;


    // ── Yoda Prepares His Concentration ───────────────────────────────────────
    // Before Yoda lifts anything, he plants his feet, closes his eyes, and
    // prepares the two-stage plan: first Luke shows him what's in the swamp
    // (selection page), then Luke tells him how to handle it (options page).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers the two wizard pages that collect the merge specification from the user.
     * Eclipse calls this before the dialog opens so it can build the page navigation
     * buttons.  Page order matters: selection comes first, options second.
     */
    public void addPages()
    {
        // Creating pages
        selectionPage = new MergeSchemasSelectionWizardPage();
        optionsPage = new MergeSchemasOptionsWizardPage();

        // Adding pages
        addPage( selectionPage );
        addPage( optionsPage );
    }


    // ── Yoda Lifts The X-Wing ─────────────────────────────────────────────────
    // Yoda closes his eyes, reaches out through the Force, and the X-wing rises —
    // every component of it, including the engine parts that were hidden under the
    // mud.  If anything can't be moved cleanly (a conflict, a duplicate OID), he
    // surfaces a report rather than leaving pieces behind invisibly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the merge when the user clicks Finish.
     * We read the selected objects and option flags from the two pages, then call
     * {@link #mergeObjects(Object[], List, boolean, boolean, boolean)} to do the
     * actual work.  Any conflicts (duplicate OIDs, alias clashes) are collected into
     * an error list and shown to the user in a text-area dialog rather than silently
     * ignored or treated as fatal.
     *
     * <p>For example — Yoda lifts Luke's X-wing from the swamp:</p>
     * <pre>
     *   Yoda reaches out: every attribute type and object class rises.
     *   Dependencies (super-types, referenced attributes) are pulled along automatically.
     *   Anything that won't fit cleanly in the target gets logged: "Conflicts found, hmm."
     * </pre>
     *
     * @return  {@code true} always — conflicts are reported via a dialog, not by
     *          returning {@code false} and keeping the wizard open.
     */
    public boolean performFinish()
    {
        Object[] sourceObjects = selectionPage.getSelectedObjects();

        boolean replaceUnknownSyntax = optionsPage.isReplaceUnknownSyntax();
        boolean mergeDependencies = optionsPage.isMergeDependencies();
        boolean pullUpAttributes = optionsPage.isPullUpAttributes();

        List<String> errorMessages = new ArrayList<String>();
        mergeObjects( sourceObjects, errorMessages, replaceUnknownSyntax, mergeDependencies, pullUpAttributes );
        if ( !errorMessages.isEmpty() )
        {
            StringBuilder sb = new StringBuilder();
            for ( String errorMessage : errorMessages )
            {
                sb.append( errorMessage );
                sb.append( '\n' );
            }
            new MessageDialogWithTextarea( getShell(), Messages.getString( "MergeSchemasWizard.MergeResultTitle" ), //$NON-NLS-1$
                Messages.getString( "MergeSchemasWizard.MergeResultMessage" ), sb.toString() ).open(); //$NON-NLS-1$
        }

        return true;
    }


    // ── Yoda Guides Each Piece Of The X-Wing Upward ──────────────────────────
    // Yoda doesn't lift the whole ship in one block — he identifies each major
    // component (project, schema, folder, individual part) and lifts each according
    // to its type.  The processedObjects set ensures he doesn't accidentally lift
    // the same bolt twice.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Dispatches each selected tree node to the appropriate merge method based on
     * its runtime type, then registers all newly created target schemas with the
     * target project.
     * We track processed objects in a {@link Set} so recursive dependency resolution
     * doesn't merge the same element twice.  Target schemas are accumulated in a
     * {@link Map} keyed by name so we reuse the same target schema across multiple
     * source objects that belong to the same source project.
     *
     * @param sourceObjects        the raw array of checked tree nodes from the selection page.
     * @param errorMessages        a mutable list we append conflict descriptions to.
     * @param replaceUnknownSyntax if {@code true}, attributes with unrecognised syntax OIDs
     *                             are given the Directory String syntax.
     * @param mergeDependencies    if {@code true}, super-types and referenced attributes are
     *                             recursively included.
     * @param pullUpAttributes     if {@code true}, must/may attributes from super-classes
     *                             already in the target are promoted onto the merged object class.
     */
    private void mergeObjects( Object[] sourceObjects, List<String> errorMessages, boolean replaceUnknownSyntax,
        boolean mergeDependencies, boolean pullUpAttributes )
    {
        /*
         * List of already processed schema objects. Used to avoid that schema objects are process multiple time.
         */
        Set<Object> processedObjects = new HashSet<Object>();

        /*
         * List of created target schemas.
         */
        Map<String, Schema> targetSchemas = new HashMap<String, Schema>();

        Project targetProject = Activator.getDefault().getProjectsHandler().getOpenProject();

        // merge all source objects to the target project
        for ( Object sourceObject : sourceObjects )
        {
            if ( sourceObject instanceof Project )
            {
                Project sourceProject = ( Project ) sourceObject;
                for ( Schema sourceSchema : sourceProject.getSchemaHandler().getSchemas() )
                {
                    Schema targetSchema = getTargetSchema( sourceSchema.getProject(), targetProject, targetSchemas );
                    mergeSchema( sourceSchema, targetProject, targetSchema, processedObjects, errorMessages,
                        replaceUnknownSyntax, mergeDependencies, pullUpAttributes );
                }
            }
            if ( sourceObject instanceof Schema )
            {
                Schema sourceSchema = ( Schema ) sourceObject;
                Schema targetSchema = getTargetSchema( sourceSchema.getProject(), targetProject, targetSchemas );
                mergeSchema( sourceSchema, targetProject, targetSchema, processedObjects, errorMessages,
                    replaceUnknownSyntax, mergeDependencies, pullUpAttributes );
            }
            if ( sourceObject instanceof AttributeTypeFolder )
            {
                AttributeTypeFolder atf = ( AttributeTypeFolder ) sourceObject;
                Schema targetSchema = getTargetSchema( atf.schema.getProject(), targetProject, targetSchemas );
                List<AttributeType> sourceAttributeTypes = atf.schema.getAttributeTypes();
                for ( AttributeType sourceAttributeType : sourceAttributeTypes )
                {
                    mergeAttributeType( sourceAttributeType, targetProject, targetSchema, processedObjects,
                        errorMessages, replaceUnknownSyntax, mergeDependencies, pullUpAttributes );
                }
            }
            if ( sourceObject instanceof ObjectClassFolder )
            {
                ObjectClassFolder ocf = ( ObjectClassFolder ) sourceObject;
                Schema targetSchema = getTargetSchema( ocf.schema.getProject(), targetProject, targetSchemas );
                List<ObjectClass> sourceObjectClasses = ocf.schema.getObjectClasses();
                for ( ObjectClass sourceObjectClass : sourceObjectClasses )
                {
                    mergeObjectClass( sourceObjectClass, targetProject, targetSchema, processedObjects, errorMessages,
                        replaceUnknownSyntax, mergeDependencies, pullUpAttributes );
                }
            }
            if ( sourceObject instanceof AttributeTypeWrapper )
            {
                AttributeTypeWrapper atw = ( AttributeTypeWrapper ) sourceObject;
                Schema targetSchema = getTargetSchema( atw.folder.schema.getProject(), targetProject, targetSchemas );
                mergeAttributeType( atw.attributeType, targetProject, targetSchema, processedObjects, errorMessages,
                    replaceUnknownSyntax, mergeDependencies, pullUpAttributes );
            }
            if ( sourceObject instanceof ObjectClassWrapper )
            {
                ObjectClassWrapper ocw = ( ObjectClassWrapper ) sourceObject;
                Schema targetSchema = getTargetSchema( ocw.folder.schema.getProject(), targetProject, targetSchemas );
                mergeObjectClass( ocw.objectClass, targetProject, targetSchema, processedObjects, errorMessages,
                    replaceUnknownSyntax, mergeDependencies, pullUpAttributes );
            }
        }

        //add created target schemas to project
        for ( Schema targetSchema : targetSchemas.values() )
        {
            if ( !targetProject.getSchemaHandler().getSchemas().contains( targetSchema ) )
            {
                targetProject.getSchemaHandler().addSchema( targetSchema );
            }
        }
    }


    // ── Yoda Finds The Right Landing Spot ────────────────────────────────────
    // Before setting the X-wing down, Yoda locates the right clearing — the target
    // schema named "merge-from-<sourceProject>".  If that schema already exists in
    // the target (from a previous merge run), he removes the stale version first so
    // we don't accumulate phantom duplicates.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Finds or creates the target {@link Schema} that will receive merged objects from
     * the given source project.
     * The target schema is named {@code "merge-from-<sourceProjectName>"}.  If it already
     * exists in the target project (leftover from a previous merge), we remove the old
     * instance so the new merge starts clean.  We also check our local {@code targetSchemas}
     * map in case we already created it earlier in this same run.
     *
     * @param sourceProject  the project the source schema objects are coming from; its name
     *                       is used to derive the target schema name.
     * @param targetProject  the currently open project that will receive the merged objects.
     * @param targetSchemas  accumulator map from schema name to schema instance for this merge run.
     * @return  the {@link Schema} instance to add merged objects to.
     */
    private Schema getTargetSchema( Project sourceProject, Project targetProject, Map<String, Schema> targetSchemas )
    {
        String targetSchemaName = "merge-from-" + sourceProject.getName(); //$NON-NLS-1$
        Schema targetSchema = targetProject.getSchemaHandler().getSchema( targetSchemaName );
        if ( targetSchema != null )
        {
            targetProject.getSchemaHandler().removeSchema( targetSchema );
        }
        else if ( targetSchemas.containsKey( targetSchemaName ) )
        {
            targetSchema = targetSchemas.get( targetSchemaName );
        }
        else
        {
            targetSchema = new Schema( targetSchemaName );
            targetSchema.setProject( targetProject );
        }
        targetSchemas.put( targetSchemaName, targetSchema );
        return targetSchema;
    }


    // ── Yoda Lifts Each Part Of A Schema ─────────────────────────────────────
    // The X-wing has many systems: engine, shields, weapons.  Yoda lifts the
    // attribute-type systems first, then the object-class systems, delegating to
    // specialised lift operations for each.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Merges all attribute types and object classes from {@code sourceSchema} into
     * {@code targetSchema} by delegating each element to the appropriate type-specific
     * merge method.
     *
     * @param sourceSchema       the schema to read elements from.
     * @param targetProject      the project receiving the merged elements.
     * @param targetSchema       the schema within the target project to add elements to.
     * @param processedObjects   set of already-handled objects (prevents double-merging).
     * @param errorMessages      list to append conflict or warning messages to.
     * @param replaceUnknownSyntax  whether to swap unrecognised syntax OIDs for Directory String.
     * @param mergeDependencies  whether to pull in referenced super-types automatically.
     * @param pullUpAttributes   whether to promote attributes from pre-existing super-classes.
     */
    private void mergeSchema( Schema sourceSchema, Project targetProject, Schema targetSchema,
        Set<Object> processedObjects, List<String> errorMessages, boolean replaceUnknownSyntax,
        boolean mergeDependencies, boolean pullUpAttributes )
    {
        List<AttributeType> sourceAttributeTypes = sourceSchema.getAttributeTypes();
        for ( AttributeType sourceAttributeType : sourceAttributeTypes )
        {
            mergeAttributeType( sourceAttributeType, targetProject, targetSchema, processedObjects, errorMessages,
                replaceUnknownSyntax, mergeDependencies, pullUpAttributes );
        }

        List<ObjectClass> sourceObjectClasses = sourceSchema.getObjectClasses();
        for ( ObjectClass sourceObjectClass : sourceObjectClasses )
        {
            mergeObjectClass( sourceObjectClass, targetProject, targetSchema, processedObjects, errorMessages,
                replaceUnknownSyntax, mergeDependencies, pullUpAttributes );
        }
    }


    // ── Yoda Lifts A Single Attribute-Type Component ─────────────────────────
    // Yoda concentrates on one specific component of the X-wing — an attribute type
    // definition.  He checks whether it already exists at the landing spot, handles
    // any conflicts, clones a clean copy, fixes syntax issues if needed, and recursively
    // lifts any upstream dependencies before setting the clone down.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Merges a single {@link AttributeType} from a source project into the target schema.
     * We check whether the attribute type (by OID or any alias) already exists in the
     * target project — if so, we add a conflict message and skip it.  Otherwise we
     * clone it, optionally fix its syntax, and optionally recurse into its super-type.
     * The {@code processedObjects} guard prevents infinite loops in cyclic dependency graphs.
     *
     * @param sourceAttributeType  the attribute type to merge; we clone rather than reference it.
     * @param targetProject        the project receiving the clone.
     * @param targetSchema         the schema within the target project to add the clone to.
     * @param processedObjects     tracks already-handled objects to avoid double processing.
     * @param errorMessages        accumulates conflict or substitution notices.
     * @param replaceUnknownSyntax if {@code true} and the attribute's syntax OID is unknown,
     *                             we substitute Directory String syntax.
     * @param mergeDependencies    if {@code true}, we recursively merge the super attribute type.
     * @param pullUpAttributes     passed through to nested object-class merges (not used here directly).
     */
    private void mergeAttributeType( AttributeType sourceAttributeType, Project targetProject, Schema targetSchema,
        Set<Object> processedObjects, List<String> errorMessages, boolean replaceUnknownSyntax,
        boolean mergeDependencies, boolean pullUpAttributes )
    {
        if ( processedObjects.contains( sourceAttributeType ) )
        {
            return;
        }
        processedObjects.add( sourceAttributeType );

        // check if attribute (identified by OID or name) already exists in the project
        AttributeType targetAttributeType = targetProject.getSchemaHandler().getAttributeType(
            sourceAttributeType.getOid() );
        if ( targetAttributeType == null )
        {
            for ( String name : sourceAttributeType.getNames() )
            {
                targetAttributeType = targetProject.getSchemaHandler().getAttributeType( name );
                if ( targetAttributeType != null )
                {
                    break;
                }
            }
        }

        // check if OID or alias name already exist in target project
        boolean oidOrAliasAlreadyTaken = targetProject.getSchemaHandler().isOidAlreadyTaken(
            sourceAttributeType.getOid() );
        if ( !oidOrAliasAlreadyTaken )
        {
            for ( String name : sourceAttributeType.getNames() )
            {
                oidOrAliasAlreadyTaken = targetProject.getSchemaHandler().isAliasAlreadyTakenForAttributeType( name );
                if ( oidOrAliasAlreadyTaken )
                {
                    break;
                }
            }
        }

        if ( targetAttributeType != null )
        {
            errorMessages.add( NLS.bind( Messages.getString( "MergeSchemasWizard.AttributeTypeExistsInTargetProject" ), //$NON-NLS-1$
                getIdString( sourceAttributeType ) ) );
        }
        else
        {
            if ( oidOrAliasAlreadyTaken )
            {
                errorMessages.add( NLS.bind( Messages.getString( "MergeSchemasWizard.OidOrAliasAlreadyTaken" ), //$NON-NLS-1$
                    getIdString( sourceAttributeType ) ) );
            }
            else
            {
                // remove attribute type if already there from previous merge
                AttributeType at = targetSchema.getAttributeType( sourceAttributeType.getOid() );
                if ( at != null )
                {
                    targetSchema.removeAttributeType( at );
                }

                // clone attribute type
                AttributeType clonedAttributeType = new AttributeType( sourceAttributeType.getOid() );
                clonedAttributeType.setNames( sourceAttributeType.getNames() );
                clonedAttributeType.setDescription( sourceAttributeType.getDescription() );
                clonedAttributeType.setSuperiorOid( sourceAttributeType.getSuperiorOid() );
                clonedAttributeType.setUsage( sourceAttributeType.getUsage() );
                clonedAttributeType.setSyntaxOid( sourceAttributeType.getSyntaxOid() );
                clonedAttributeType.setSyntaxLength( sourceAttributeType.getSyntaxLength() );
                clonedAttributeType.setObsolete( sourceAttributeType.isObsolete() );
                clonedAttributeType.setCollective( sourceAttributeType.isCollective() );
                clonedAttributeType.setSingleValued( sourceAttributeType.isSingleValued() );
                clonedAttributeType.setUserModifiable( sourceAttributeType.isUserModifiable() );
                clonedAttributeType.setEqualityOid( sourceAttributeType.getEqualityOid() );
                clonedAttributeType.setOrderingOid( sourceAttributeType.getOrderingOid() );
                clonedAttributeType.setSubstringOid( sourceAttributeType.getSubstringOid() );
                clonedAttributeType.setSchemaName( targetSchema.getSchemaName() );

                // if no/unknown syntax: set "Directory String" syntax and appropriate matching rules
                if ( replaceUnknownSyntax )
                {
                    if ( clonedAttributeType.getSyntaxOid() == null
                        || targetProject.getSchemaHandler().getSyntax( clonedAttributeType.getSyntaxOid() ) == null )
                    {
                        errorMessages.add( NLS.bind( Messages.getString( "MergeSchemasWizard.ReplacedSyntax" ), //$NON-NLS-1$
                            new String[]
                                {
                                    getIdString( sourceAttributeType ),
                                    clonedAttributeType.getSyntaxOid(),
                                    "1.3.6.1.4.1.1466.115.121.1.15 (Directory String)" } ) ); //$NON-NLS-1$
                        clonedAttributeType.setSyntaxOid( "1.3.6.1.4.1.1466.115.121.1.15" ); //$NON-NLS-1$
                        clonedAttributeType.setEqualityOid( "caseIgnoreMatch" ); //$NON-NLS-1$
                        clonedAttributeType.setOrderingOid( null );
                        clonedAttributeType.setSubstringOid( "caseIgnoreSubstringsMatch" ); //$NON-NLS-1$
                    }
                }
                // TODO: if unknown (single) matching rule: set appropriate matching rule according to syntax
                // TODO: if no (all) matching rules: set appropriate matching rules according to syntax

                // merge dependencies: super attribute type
                if ( mergeDependencies )
                {
                    String superiorName = clonedAttributeType.getSuperiorOid();
                    if ( superiorName != null )
                    {
                        AttributeType superiorAttributeType = Activator.getDefault().getSchemaHandler()
                            .getAttributeType( superiorName );
                        if ( superiorAttributeType != null )
                        {
                            mergeAttributeType( superiorAttributeType, targetProject, targetSchema, processedObjects,
                                errorMessages, replaceUnknownSyntax, mergeDependencies, pullUpAttributes );
                        }
                    }
                }

                targetSchema.addAttributeType( clonedAttributeType );
            }
        }
    }


    // ── Yoda Lifts A Single Object-Class Component ────────────────────────────
    // Yoda identifies an object class in the swamp and lifts it cleanly: he checks
    // whether it already exists at the landing site, clones a fresh copy, recursively
    // brings along super-classes and referenced attribute types (if requested), and
    // promotes any attributes the target's trimmed super-class is missing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Merges a single {@link ObjectClass} from a source project into the target schema.
     * Similar to {@link #mergeAttributeType} but handles the additional complexity of
     * super-class hierarchies and must/may attribute lists.
     * When {@code pullUpAttributes} is true and a super-class already exists in the
     * target project, we diff the source and target super-class attribute lists and
     * copy any missing attributes directly onto the merged object class.
     *
     * @param sourceObjectClass  the object class to merge; we clone rather than reference it.
     * @param targetProject      the project receiving the clone.
     * @param targetSchema       the schema to add the clone to.
     * @param processedObjects   tracks already-handled objects.
     * @param errorMessages      accumulates conflict notices.
     * @param replaceUnknownSyntax  passed through to attribute type merges for referenced attributes.
     * @param mergeDependencies  if {@code true}, super-classes and referenced attributes are recursively merged.
     * @param pullUpAttributes   if {@code true}, attributes from trimmed target super-classes are promoted.
     */
    private void mergeObjectClass( ObjectClass sourceObjectClass, Project targetProject, Schema targetSchema,
        Set<Object> processedObjects, List<String> errorMessages, boolean replaceUnknownSyntax,
        boolean mergeDependencies, boolean pullUpAttributes )
    {
        if ( processedObjects.contains( sourceObjectClass ) )
        {
            return;
        }
        processedObjects.add( sourceObjectClass );

        // check if object class (identified by OID or alias name) already exists in the target project
        ObjectClass targetObjectClass = targetProject.getSchemaHandler()
            .getObjectClass( sourceObjectClass.getOid() );
        if ( targetObjectClass == null )
        {
            for ( String name : sourceObjectClass.getNames() )
            {
                targetObjectClass = targetProject.getSchemaHandler().getObjectClass( name );
                if ( targetObjectClass != null )
                {
                    break;
                }
            }
        }

        // check if OID or alias name already exist in target project
        boolean oidOrAliasAlreadyTaken = targetProject.getSchemaHandler().isOidAlreadyTaken(
            sourceObjectClass.getOid() );
        if ( !oidOrAliasAlreadyTaken )
        {
            for ( String name : sourceObjectClass.getNames() )
            {
                oidOrAliasAlreadyTaken = targetProject.getSchemaHandler().isAliasAlreadyTakenForObjectClass( name );
                if ( oidOrAliasAlreadyTaken )
                {
                    break;
                }
            }
        }

        if ( targetObjectClass != null )
        {
            errorMessages.add( NLS.bind( Messages.getString( "MergeSchemasWizard.ObjectClassExistsInTargetProject" ), //$NON-NLS-1$
                getIdString( sourceObjectClass ) ) );
        }
        else
        {
            if ( oidOrAliasAlreadyTaken )
            {
                errorMessages.add( NLS.bind( Messages.getString( "MergeSchemasWizard.OidOrAliasAlreadyTaken" ), //$NON-NLS-1$
                    getIdString( sourceObjectClass ) ) );
            }
            else
            {
                // remove object class if already there from previous merge
                ObjectClass oc = targetSchema.getObjectClass( sourceObjectClass.getOid() );
                if ( oc != null )
                {
                    targetSchema.removeObjectClass( oc );
                }

                // create object class
                ObjectClass clonedObjectClass = new ObjectClass( sourceObjectClass.getOid() );
                clonedObjectClass.setOid( sourceObjectClass.getOid() );
                clonedObjectClass.setNames( sourceObjectClass.getNames() );
                clonedObjectClass.setDescription( sourceObjectClass.getDescription() );
                clonedObjectClass.setSuperiorOids( sourceObjectClass.getSuperiorOids() );
                clonedObjectClass.setType( sourceObjectClass.getType() );
                clonedObjectClass.setObsolete( sourceObjectClass.isObsolete() );
                clonedObjectClass.setMustAttributeTypeOids( sourceObjectClass.getMustAttributeTypeOids() );
                clonedObjectClass.setMayAttributeTypeOids( sourceObjectClass.getMayAttributeTypeOids() );
                clonedObjectClass.setSchemaName( targetSchema.getSchemaName() );

                // merge dependencies: super object classes and must/may attributes
                if ( mergeDependencies )
                {
                    List<String> superClassesNames = clonedObjectClass.getSuperiorOids();
                    if ( superClassesNames != null )
                    {
                        for ( String superClassName : superClassesNames )
                        {
                            if ( superClassName != null )
                            {
                                ObjectClass superSourceObjectClass = Activator.getDefault().getSchemaHandler()
                                    .getObjectClass( superClassName );
                                ObjectClass superTargetObjectClass = targetProject.getSchemaHandler()
                                    .getObjectClass( superClassName );
                                if ( superSourceObjectClass != null )
                                {
                                    if ( superTargetObjectClass == null )
                                    {
                                        mergeObjectClass( superSourceObjectClass, targetProject, targetSchema,
                                            processedObjects, errorMessages, replaceUnknownSyntax, mergeDependencies,
                                            pullUpAttributes );
                                    }
                                    else
                                    {
                                        // pull-up may and must attributes to this OC if super already exists in target
                                        if ( pullUpAttributes )
                                        {
                                            pullUpAttributes( clonedObjectClass, superSourceObjectClass,
                                                superTargetObjectClass );
                                        }
                                    }
                                }
                            }
                        }
                    }

                    List<String> mustNamesList = clonedObjectClass.getMustAttributeTypeOids();
                    List<String> mayNamesList = clonedObjectClass.getMayAttributeTypeOids();
                    List<String> attributeNames = new ArrayList<String>();
                    if ( mustNamesList != null )
                    {
                        attributeNames.addAll( mustNamesList );
                    }
                    if ( mayNamesList != null )
                    {
                        attributeNames.addAll( mayNamesList );
                    }
                    for ( String attributeName : attributeNames )
                    {
                        if ( attributeName != null )
                        {
                            AttributeType attributeType = Activator.getDefault().getSchemaHandler()
                                .getAttributeType( attributeName );
                            if ( attributeType != null )
                            {
                                mergeAttributeType( attributeType, targetProject, targetSchema, processedObjects,
                                    errorMessages, replaceUnknownSyntax, mergeDependencies, pullUpAttributes );
                            }
                        }
                    }
                }

                targetSchema.addObjectClass( clonedObjectClass );
            }
        }
    }


    // ── Yoda Rescues The Missing Gear From The Mud ────────────────────────────
    // Some parts of the X-wing were buried deeper than the main hull — the target
    // clearing already has a pad for them, but it doesn't have the extra bolts that
    // were stuck in the mud.  Yoda pulls those extra bolts out of the source and
    // attaches them directly to the main hull piece instead.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Promotes must/may attributes from a source super-class onto the target object class
     * when those attributes are not already provided by the trimmed target super-class.
     * This prevents attribute loss: if the target project has a slimmer version of a
     * super-class, the attributes that the source super-class had but the target version
     * dropped get added directly to the merged object class.
     *
     * @param targetObjectClass       the object class being merged into the target; we add
     *                                promoted attribute names to its must/may lists.
     * @param sourceSuperObjectClass  the full super-class from the source project.
     * @param targetSuperObjectClass  the (possibly trimmed) super-class already in the target project.
     */
    private void pullUpAttributes( ObjectClass targetObjectClass, ObjectClass sourceSuperObjectClass,
        ObjectClass targetSuperObjectClass )
    {
        // must
        Set<String> sourceMustAttributeNames = new HashSet<String>();
        fetchAttributes( sourceMustAttributeNames, sourceSuperObjectClass, true );
        Set<String> targetMustAttributeNames = new HashSet<String>();
        fetchAttributes( targetMustAttributeNames, targetSuperObjectClass, true );
        sourceMustAttributeNames.removeAll( targetMustAttributeNames );
        if ( !sourceMustAttributeNames.isEmpty() )
        {
            sourceMustAttributeNames.addAll( targetObjectClass.getMustAttributeTypeOids() );
            targetObjectClass.setMustAttributeTypeOids( new ArrayList<String>( sourceMustAttributeNames ) );
        }

        // may
        Set<String> sourceMayAttributeNames = new HashSet<String>();
        fetchAttributes( sourceMayAttributeNames, sourceSuperObjectClass, false );
        Set<String> targetMayAttributeNames = new HashSet<String>();
        fetchAttributes( targetMayAttributeNames, targetSuperObjectClass, false );
        sourceMayAttributeNames.removeAll( targetMayAttributeNames );
        if ( !sourceMayAttributeNames.isEmpty() )
        {
            sourceMayAttributeNames.addAll( targetObjectClass.getMayAttributeTypeOids() );
            targetObjectClass.setMayAttributeTypeOids( new ArrayList<String>( sourceMayAttributeNames ) );
        }
    }


    // ── Yoda Gathers The Attribute Inventory From Each Level ─────────────────
    // To know which bolts are missing, Yoda recursively inventories every component
    // of the super-class hierarchy — not just the immediate class but everything
    // it inherits from above.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Recursively collects all must or may attribute names from an object class and
     * its full super-class hierarchy into the given set.
     * We recurse because an object class inherits attributes from all its ancestors,
     * not just its direct parent.
     *
     * @param attributeNameList  the set to accumulate attribute names into; modified in place.
     * @param oc                 the object class to inspect.
     * @param must               {@code true} to collect must attributes, {@code false} for may.
     */
    private void fetchAttributes( Set<String> attributeNameList, ObjectClass oc, boolean must )
    {
        List<String> attributeNames = must ? oc.getMustAttributeTypeOids() : oc.getMayAttributeTypeOids();
        attributeNameList.addAll( attributeNames );

        for ( String superClassName : oc.getSuperiorOids() )
        {
            ObjectClass superObjectClass = Activator.getDefault().getSchemaHandler().getObjectClass(
                superClassName );
            fetchAttributes( attributeNameList, superObjectClass, must );
        }
    }


    // ── Yoda Labels Each Component For The Report ─────────────────────────────
    // After the lift, Yoda gives Luke a manifest: "Component [cn,2.5.4.3] — conflict."
    // We need a consistent human-readable ID string for each schema object so the
    // error messages make sense to the user.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds a compact identifier string for a schema object combining its alias
     * names and OID, suitable for inclusion in error messages.
     * The format is {@code [aliasName1,aliasName2,...,oid]}.
     *
     * @param schemaObject  any schema object (attribute type, object class, etc.) whose
     *                      names and OID we want to display.
     * @return  a bracket-enclosed string like {@code [cn,commonName,2.5.4.3]}.
     */
    private String getIdString( AbstractSchemaObject schemaObject )
    {
        StringBuilder sb = new StringBuilder();
        sb.append( '[' );
        if ( schemaObject.getNames() != null )
        {
            for ( String name : schemaObject.getNames() )
            {
                sb.append( name );
                sb.append( ',' );
            }
        }
        sb.append( schemaObject.getOid() );
        sb.append( ']' );
        return sb.toString();
    }


    // ── Yoda Settles Into Position Before The Lift ────────────────────────────
    // Yoda walks to the shore, plants his stick, and takes a breath — nothing to
    // set up here, just readying himself for the work ahead.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the wizard is launched; nothing to initialise here
     * since all context comes from the wizard pages.
     *
     * @param workbench  the Eclipse workbench — required by {@link IImportWizard}, not used.
     * @param selection  the current workbench selection — not used; pages handle input.
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
    }
}
