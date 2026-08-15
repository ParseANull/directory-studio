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
package org.apache.directory.studio.apacheds.configuration.jobs;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.directory.api.ldap.model.constants.LdapConstants;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.filter.FilterParser;
import org.apache.directory.api.ldap.model.ldif.ChangeType;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.message.AliasDerefMode;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.UsageEnum;
import org.apache.directory.server.core.api.entry.ClonedServerEntry;
import org.apache.directory.server.core.api.filtering.EntryFilteringCursor;
import org.apache.directory.server.core.api.interceptor.context.LookupOperationContext;
import org.apache.directory.server.core.api.interceptor.context.SearchOperationContext;
import org.apache.directory.server.core.api.partition.Partition;
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPlugin;
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPluginConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


// ── CLASS: PartitionsDiffComputer — COMPARING THE DEATH STAR BLUEPRINTS ──────
// General Dodonna's analysts place two sets of Death Star blueprints side by
// side on the light-table: the original version grabbed when the editor opened,
// and the revised version the user just finished editing.  They go through every
// page, every attribute, every single value to find what changed.
// This class is those analysts: it walks two LDAP partitions and produces a list
// of LdifEntry change records (add, modify, delete) that describe the full diff,
// so we can write only the delta back to the live server rather than everything.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Computes the difference between two LDAP partitions and returns the result
 * as a list of LDIF change entries.
 * Used during save-to-connection: we compare the original partition (loaded when
 * the editor opened) against the destination partition (built from the user's
 * edits) to find only what changed and needs writing back to the server.
 * Think of this class as comparing two revisions of the Death Star blueprints —
 * page by page, attribute by attribute — to find exactly what changed between
 * the version we grabbed and the version we want to apply.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PartitionsDiffComputer
{
    /** The original partition */
    private Partition originalPartition;

    /** The destination partition */
    private Partition destinationPartition;


    // ── Analysis Table Ready, No Blueprints Yet ──────────────────────────────
    // The analysis table is set up and waiting — lamps on, notebooks open —
    // but no blueprints have been placed on it yet.  The caller is expected to
    // inject both partitions via the setters before running computeModifications().
    // This no-arg constructor gives you a blank-slate diff computer for the cases
    // where setter injection is more convenient than the two-arg constructor.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates an empty PartitionsDiffComputer with no partitions assigned.
     * Call {@link #setOriginalPartition} and {@link #setDestinationPartition}
     * before invoking {@link #computeModifications()}, otherwise the validation
     * step will throw.
     */
    public PartitionsDiffComputer()
    {
    }


    // ── Both Blueprint Sets Arrive at the Analysis Table ────────────────────
    // Two couriers arrive simultaneously and drop both blueprint sets on the
    // table: the old schematics and the newly edited revision.  Now the analysts
    // have everything they need and can start the comparison immediately.
    // This constructor stores both partitions so computeModifications() can get
    // straight to work without any additional setters being called first.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates a PartitionsDiffComputer pre-loaded with both partitions to compare.
     * Use this when you have both sides ready at construction time and don't want
     * to call setters separately.
     *
     * @param originalPartition     the partition representing the state we started from
     * @param destinationPartition  the partition representing the state we want to reach
     */
    public PartitionsDiffComputer( Partition originalPartition, Partition destinationPartition )
    {
        this.originalPartition = originalPartition;
        this.destinationPartition = destinationPartition;
    }


    // ── Analysts Compare Every Page of Both Blueprint Sets ───────────────────
    // "Compare every page of both blueprint sets — user attributes AND operational
    // attributes — starting right from the very top of the filing hierarchy."
    // This convenience overload uses the original partition's own suffix DN as the
    // starting point and requests all user plus all operational attributes, so
    // absolutely nothing in either partition gets missed.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Computes all modifications between the two partitions, checking every user
     * and operational attribute from the root suffix DN downward.
     * This is the most common entry point — call it when you want a complete diff
     * with no attribute filtering.
     *
     * @return  a list of {@link LdifEntry} objects describing each added, modified,
     *          or deleted entry; empty if the two partitions are identical
     * @throws Exception  if a partition read fails or a validation error occurs
     */
    public List<LdifEntry> computeModifications() throws Exception
    {
        // Using the original partition suffix as base
        // '*' for all user attributes, '+' for all operational attributes
        return computeModifications( originalPartition.getSuffixDn(), new String[]
            { SchemaConstants.ALL_USER_ATTRIBUTES, SchemaConstants.ALL_OPERATIONAL_ATTRIBUTES } );
    }


    // ── Analysts Focus on Specific Blueprint Columns ─────────────────────────
    // "We only care about the power-coupling schematics this time — ignore the
    // exhaust-port measurements entirely."  This overload lets the caller say
    // exactly which attribute IDs matter, while still starting from the partition's
    // own suffix DN so the full tree depth is covered.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Computes modifications between the two partitions, checking only the
     * specified attributes, starting from the original partition's suffix DN.
     * Useful when you only care about a known set of attribute types and want
     * to skip the rest for performance reasons.
     *
     * @param attributeIds  the LDAP attribute IDs to include in the comparison,
     *                      e.g. {@code "*"}, {@code "+"}, or specific OIDs
     * @return              a list of {@link LdifEntry} modifications; empty if identical
     * @throws Exception    if a partition read fails
     */
    public List<LdifEntry> computeModifications( String[] attributeIds ) throws Exception
    {
        return computeModifications( originalPartition.getSuffixDn(), attributeIds );
    }


    // ── Analysts Start Comparing From a Specific Chapter ────────────────────
    // "Start the comparison at chapter 47 — the Reactor Core section — and look
    // at only these specific columns."  This overload is the one all the others
    // ultimately delegate to: it validates both partitions are ready, then kicks
    // off the full recursive comparison from the given base DN.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * The canonical computeModifications entry point — validates both partitions,
     * then delegates to {@link #comparePartitions} starting from {@code baseDn}.
     * All other overloads funnel through here.
     *
     * @param baseDn        the DN to start the comparison from (usually the suffix)
     * @param attributeIds  the attribute IDs to include in the comparison
     * @return              a list of {@link LdifEntry} modifications
     * @throws Exception    if partition validation or the comparison itself fails
     */
    public List<LdifEntry> computeModifications( Dn baseDn, String[] attributeIds ) throws Exception
    {
        // Checking partitions
        checkPartitions();

        return comparePartitions( baseDn, attributeIds );
    }


    // ── Security Guard Verifies Both Blueprint Sets Are Present ──────────────
    // Before any analysis starts, the security guard steps in and verifies that
    // both blueprint sets are actually on the table — not null, not missing their
    // cover page (suffix DN), and properly catalogued (initialized).  If anything
    // is wrong she shuts down the analysis and throws everyone out of the room.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Verifies that both partitions are non-null, initialised, and have a suffix DN.
     * We do this up front so {@link #comparePartitions} can assume well-formed data
     * and skip defensive null checks throughout its inner loops.
     *
     * @throws PartitionsDiffException  if either partition is null, not initialised,
     *                                  or missing its suffix DN
     */
    private void checkPartitions() throws PartitionsDiffException
    {
        // Checking the original partition
        if ( originalPartition == null )
        {
            throw new PartitionsDiffException( Messages.getString( "PartitionDiffComputer.OriginalPartitionIsNull" ) );
        }
        else
        {
            if ( !originalPartition.isInitialized() )
            {
                throw new PartitionsDiffException( Messages.getString( "PartitionDiffComputer.OriginalPartitionNotInitialized" ) );
            }
            else if ( originalPartition.getSuffixDn() == null )
            {
                throw new PartitionsDiffException( Messages.getString( "PartitionDiffComputer.OriginalSuffixIsNull" ) );
            }
        }

        // Checking the destination partition
        if ( destinationPartition == null )
        {
            throw new PartitionsDiffException( Messages.getString( "PartitionDiffComputer.DestinationPartitionIsNull" ) );
        }
        else
        {
            if ( !destinationPartition.isInitialized() )
            {
                throw new PartitionsDiffException( Messages.getString( "PartitionDiffComputer.DestinationPartitionNotInitialized" ) );
            }
            else if ( destinationPartition.getSuffixDn() == null )
            {
                throw new PartitionsDiffException( Messages.getString( "PartitionDiffComputer.DestinationPartitionIsNull" ) );
            }
        }
    }


    // ── Analysts Do the Full Side-by-Side Comparison ─────────────────────────
    // The team lays both blueprint sets on the light-table and methodically works
    // through every entry in the original: if it still exists in the destination
    // they compare the details and mark any differences as Modify; if it's gone
    // they mark it Delete.  Then they flip the process and scan the destination
    // for anything brand-new — those get marked Add.  Deletes are reversed at the
    // end so leaf nodes get removed before their parents, keeping LDAP happy.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Walks both partitions entry-by-entry and builds the complete list of LDIF modifications.
     *
     * <p>The algorithm runs in two passes:</p>
     * <pre>
     *   Pass 1 — original to destination:
     *     For each entry in original: if it still exists in destination, compare
     *     attributes (Modify); if it's gone from destination, mark it Delete.
     *   Pass 2 — destination to original:
     *     For each entry in destination not found in original: mark it Add.
     * </pre>
     * Delete entries are reversed at the end so leaf nodes are deleted before
     * their parents (otherwise we'd be removing non-empty containers first).
     *
     * @param baseDn        the DN to start from (typically the partition suffix DN)
     * @param attributeIds  the attribute IDs to include in the comparison
     * @return              the ordered list of LDIF modifications to apply
     * @throws PartitionsDiffException  if any partition lookup or cursor operation fails
     */
    public List<LdifEntry> comparePartitions( Dn baseDn, String[] attributeIds ) throws PartitionsDiffException
    {
        // Creating the list containing all modifications
        List<LdifEntry> modifications = new ArrayList<>();

        try
        {
            // Looking up the original base entry
            Entry originalBaseEntry = originalPartition.lookup( new LookupOperationContext( null, baseDn, attributeIds ) );

            if ( originalBaseEntry == null )
            {
                throw new PartitionsDiffException( Messages.getString( "PartitionDiffComputer.PartitionNotFound" ) );
            }

            // Creating the list containing all the original entries to be processed
            // and adding it the original base entry
            List<Entry> originalEntries = new ArrayList<>();
            originalEntries.add( originalBaseEntry );

            // Looping until all original entries are being processed
            while ( !originalEntries.isEmpty() )
            {
                // Getting the first original entry from the list
                Entry originalEntry = originalEntries.remove( 0 );

                // Creating a modification entry to hold all modifications
                LdifEntry modificationEntry = new LdifEntry();
                modificationEntry.setDn( originalEntry.getDn() );

                // Looking for the equivalent entry in the destination partition
                Entry destinationEntry = destinationPartition.lookup( new LookupOperationContext( null, originalEntry
                    .getDn(), attributeIds ) );

                if ( destinationEntry != null )
                {
                    // Setting the changetype to delete
                    modificationEntry.setChangeType( ChangeType.Modify );

                    // Comparing both entries
                    compareEntries( originalEntry, destinationEntry, modificationEntry );
                }
                else
                {
                    // The original entry is no longer present in the destination partition

                    // Setting the changetype to delete
                    modificationEntry.setChangeType( ChangeType.Delete );
                }

                // Checking if modifications occurred on the original entry
                ChangeType modificationEntryChangeType = modificationEntry.getChangeType();

                if ( modificationEntryChangeType != ChangeType.None )
                {
                    if ( modificationEntryChangeType == ChangeType.Delete
                        || ( modificationEntryChangeType == ChangeType.Modify && !modificationEntry
                            .getModifications().isEmpty() ) )
                    {
                        // Adding the modification entry to the list
                        modifications.add( modificationEntry );
                    }
                }

                // Creating a search operation context to get the children of the current entry
                SearchOperationContext soc = new SearchOperationContext( null, originalEntry.getDn(),
                    SearchScope.ONELEVEL,
                    FilterParser.parse( originalPartition.getSchemaManager(), LdapConstants.OBJECT_CLASS_STAR ), attributeIds ); //$NON-NLS-1$
                soc.setAliasDerefMode( AliasDerefMode.DEREF_ALWAYS );

                // Looking for the children of the current entry
                EntryFilteringCursor cursor = originalPartition.search( soc );

                while ( cursor.next() )
                {
                    originalEntries.add( ( ( ClonedServerEntry ) cursor.get() ).getClonedEntry() );
                }
            }

            // Reversing the list to allow deletion of leafs first (otherwise we would be deleting
            // higher nodes with children first).
            // Order for modified entries does not matter.
            Collections.reverse( modifications );

            // Looking up the destination base entry
            Entry destinationBaseEntry = destinationPartition
                .lookup( new LookupOperationContext( null, baseDn, attributeIds ) );

            if ( destinationBaseEntry == null )
            {
                ApacheDS2ConfigurationPlugin.getDefault().getLog().log(
                    new Status( IStatus.ERROR, ApacheDS2ConfigurationPluginConstants.PLUGIN_ID,
                        Messages.getString( "PartitionDiffComputer.PartitionNotFound" ) ) );
                throw new PartitionsDiffException( Messages.getString( "PartitionDiffComputer.PartitionNotFound" ) );
            }

            // Creating the list containing all the destination entries to be processed
            // and adding it the destination base entry
            List<Entry> destinationEntries = new ArrayList<>();
            destinationEntries.add( originalBaseEntry );

            // Looping until all destination entries are being processed
            while ( !destinationEntries.isEmpty() )
            {
                // Getting the first destination entry from the list
                Entry destinationEntry = destinationEntries.remove( 0 );

                // Looking for the equivalent entry in the destination partition
                Entry originalEntry = originalPartition.lookup( new LookupOperationContext( null, destinationEntry
                    .getDn(), attributeIds ) );

                // We're only looking for new entries, modified or removed
                // entries have already been computed
                if ( originalEntry == null )
                {
                    // Creating a modification entry to hold all modifications
                    LdifEntry modificationEntry = new LdifEntry();
                    modificationEntry.setDn( destinationEntry.getDn() );

                    // Setting the changetype to addition
                    modificationEntry.setChangeType( ChangeType.Add );

                    // Copying attributes
                    for ( Attribute attribute : destinationEntry )
                    {
                        modificationEntry.addAttribute( attribute );
                    }

                    // Adding the modification entry to the list
                    modifications.add( modificationEntry );
                }

                // Creating a search operation context to get the children of the current entry
                SearchOperationContext soc = new SearchOperationContext( null, destinationEntry.getDn(),
                    SearchScope.ONELEVEL,
                    FilterParser.parse( originalPartition.getSchemaManager(), LdapConstants.OBJECT_CLASS_STAR ), attributeIds ); //$NON-NLS-1$
                soc.setAliasDerefMode( AliasDerefMode.DEREF_ALWAYS );

                // Looking for the children of the current entry
                EntryFilteringCursor cursor = destinationPartition.search( soc );

                while ( cursor.next() )
                {
                    destinationEntries.add( ( ( ClonedServerEntry ) cursor.get() ).getClonedEntry() );
                }
            }
        }
        catch ( Exception e )
        {
            ApacheDS2ConfigurationPlugin.getDefault().getLog().log(
                new Status( IStatus.ERROR, ApacheDS2ConfigurationPluginConstants.PLUGIN_ID,
                    Messages.getString( "PartitionDiffComputer.ComparePartitions" ) ) );

            throw new PartitionsDiffException( e );
        }

        return modifications;
    }


    // ── Analyst Compares Two Specific Blueprint Pages ────────────────────────
    // The analyst places two pages side by side on the light-table — one from
    // each blueprint set — and goes attribute by attribute.  If an attribute
    // exists in the original but is gone from the destination, that's a removal.
    // If the destination has a new attribute the original didn't, that's an add.
    // For anything in both, she hands the pair off to compareAttributes to dig
    // into the individual values.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Compares two LDAP entries attribute by attribute and records any differences
     * as modifications in the provided {@link LdifEntry}.
     * We skip operational attributes (anything whose usage is not
     * {@code USER_APPLICATIONS}) because those are managed by the server itself.
     *
     * @param originalEntry     the entry as it was when the editor opened
     * @param destinationEntry  the entry as it looks after the user's edits
     * @param modificationEntry the LDIF change record we're building up; modifications
     *                          are appended directly to this object
     */
    private void compareEntries( Entry originalEntry, Entry destinationEntry, LdifEntry modificationEntry )
    {
        // Creating a list to store the already evaluated attribute type
        List<AttributeType> evaluatedATs = new ArrayList<>();

        // Checking attributes of the original entry
        for ( Attribute originalAttribute : originalEntry )
        {
            AttributeType originalAttributeType = originalAttribute.getAttributeType();

            // We're only working on 'userApplications' attributes
            if ( originalAttributeType.getUsage() == UsageEnum.USER_APPLICATIONS )
            {
                Attribute destinationAttribute = destinationEntry.get( originalAttributeType );
                if ( destinationAttribute == null )
                {
                    // Creating a modification for the removed AT
                    Modification modification = new DefaultModification();
                    modification.setOperation( ModificationOperation.REMOVE_ATTRIBUTE );
                    modification.setAttribute( new DefaultAttribute( originalAttribute.getAttributeType() ) );

                    modificationEntry.addModification( modification );
                }
                else
                {
                    // Comparing both attributes
                    compareAttributes( originalAttribute, destinationAttribute, modificationEntry );
                }

                evaluatedATs.add( originalAttributeType );
            }
        }

        // Checking attributes of the destination entry
        for ( Attribute destinationAttribute : destinationEntry )
        {
            AttributeType destinationAttributeType = destinationAttribute.getAttributeType();

            // We're only working on 'userApplications' attributes
            if ( destinationAttributeType.getUsage() == UsageEnum.USER_APPLICATIONS )
            {
                // Checking if the current AT has already been evaluated
                if ( !evaluatedATs.contains( destinationAttributeType ) )
                {
                    // Creating a modification for the added AT
                    Modification modification = new DefaultModification();
                    modification.setOperation( ModificationOperation.ADD_ATTRIBUTE );
                    Attribute attribute = new DefaultAttribute( destinationAttributeType );
                    modification.setAttribute( attribute );

                    for ( Value value : destinationAttribute )
                    {
                        try
                        {
                            attribute.add( value );
                        }
                        catch ( LdapInvalidAttributeValueException liave )
                        {
                            ApacheDS2ConfigurationPlugin.getDefault().getLog().log(
                                new Status( IStatus.ERROR, ApacheDS2ConfigurationPluginConstants.PLUGIN_ID,
                                    Messages.getString( "PartitionDiffComputer.InvalidAttributeException" ) ) );

                            ApacheDS2ConfigurationPlugin.getDefault().getLog().log(
                                new Status( IStatus.ERROR, ApacheDS2ConfigurationPluginConstants.PLUGIN_ID,
                                    liave.getLocalizedMessage() ) );
                        }
                    }

                    modificationEntry.addModification( modification );
                }
            }
        }
    }


    // ── Analyst Compares Specific Columns Value by Value ────────────────────
    // Zooming in on a single column of the blueprint, the analyst checks every
    // value listed in the original against the destination.  Values that vanished
    // become REMOVE_ATTRIBUTE modifications; values that are brand-new in the
    // destination become ADD_ATTRIBUTE modifications.  She skips values already
    // seen so she never double-counts anything.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Compares two LDAP attributes value by value and records any differences
     * as modifications in the provided {@link LdifEntry}.
     * We iterate the original attribute's values first, then the destination's,
     * tracking already-evaluated values to avoid creating duplicate modifications.
     *
     * @param originalAttribute    the attribute from the original entry
     * @param destinationAttribute the attribute from the destination entry
     * @param modificationEntry    the LDIF change record we're building up; modifications
     *                             are appended directly to this object
     */
    private void compareAttributes( Attribute originalAttribute, Attribute destinationAttribute,
        LdifEntry modificationEntry )
    {
        // Creating a list to store the already evaluated values
        List<Value> evaluatedValues = new ArrayList<>();

        // Checking values of the original attribute
        for ( Value originalValue : originalAttribute )
        {
            if ( !destinationAttribute.contains( originalValue ) )
            {
                // Creating a modification for the removed AT value
                Modification modification = new DefaultModification();
                modification.setOperation( ModificationOperation.REMOVE_ATTRIBUTE );
                Attribute attribute = new DefaultAttribute( originalAttribute.getAttributeType() );
                modification.setAttribute( attribute );

                try
                {
                    attribute.add( originalValue );
                }
                catch ( LdapInvalidAttributeValueException liave )
                {
                    ApacheDS2ConfigurationPlugin.getDefault().getLog().log(
                        new Status( IStatus.ERROR, ApacheDS2ConfigurationPluginConstants.PLUGIN_ID,
                            Messages.getString( "PartitionDiffComputer.InvalidAttributeException" ) ) );

                    ApacheDS2ConfigurationPlugin.getDefault().getLog().log(
                        new Status( IStatus.ERROR, ApacheDS2ConfigurationPluginConstants.PLUGIN_ID,
                            liave.getLocalizedMessage() ) );
                }

                modificationEntry.addModification( modification );
            }

            evaluatedValues.add( originalValue );
        }

        // Checking values of the destination attribute
        for ( Value destinationValue : destinationAttribute )
        {
            if ( !evaluatedValues.contains( destinationValue ) )
            {
                // Creating a modification for the added AT value
                Modification modification = new DefaultModification();
                modification.setOperation( ModificationOperation.ADD_ATTRIBUTE );
                Attribute attribute = new DefaultAttribute( originalAttribute.getAttributeType() );
                modification.setAttribute( attribute );

                try
                {
                    attribute.add( destinationValue );
                }
                catch ( LdapInvalidAttributeValueException liave )
                {
                    ApacheDS2ConfigurationPlugin.getDefault().getLog().log(
                        new Status( IStatus.ERROR, ApacheDS2ConfigurationPluginConstants.PLUGIN_ID,
                            Messages.getString( "PartitionDiffComputer.InvalidAttributeException" ) ) );

                    ApacheDS2ConfigurationPlugin.getDefault().getLog().log(
                        new Status( IStatus.ERROR, ApacheDS2ConfigurationPluginConstants.PLUGIN_ID,
                            liave.getLocalizedMessage() ) );
                }

                modificationEntry.addModification( modification );
            }
        }
    }


    // ── Archivist Retrieves the Original Blueprint Set ───────────────────────
    // The archivist reaches into the left-hand drawer and hands over the original
    // blueprint set — the one representing the server state as it was when the
    // editor first opened, before any edits were made.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the original partition — the one representing the server state as
     * it was when the editor opened, before any user edits.
     *
     * @return  the original partition, or {@code null} if none has been set
     */
    public Partition getOriginalPartition()
    {
        return originalPartition;
    }


    // ── Archivist Files the Original Blueprint Set ───────────────────────────
    // The archivist takes the original blueprint set and slides it into the
    // left-hand drawer, ready for the side-by-side comparison to begin.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Sets the original partition — the baseline representing pre-edit server state.
     * Call this before {@link #computeModifications()} if you used the no-arg constructor.
     *
     * @param originalPartition  the partition to treat as the "before" side of the diff
     */
    public void setOriginalPartition( Partition originalPartition )
    {
        this.originalPartition = originalPartition;
    }


    // ── Archivist Retrieves the Revised Blueprint Set ────────────────────────
    // The archivist reaches into the right-hand drawer and hands over the
    // revised blueprint set — the one built from the user's edits, representing
    // the state we want the server to reach after saving.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the destination partition — the one representing the desired server
     * state after the user's edits are applied.
     *
     * @return  the destination partition, or {@code null} if none has been set
     */
    public Partition getDestinationPartition()
    {
        return destinationPartition;
    }


    // ── Archivist Files the Revised Blueprint Set ────────────────────────────
    // The archivist takes the freshly edited blueprint set and slides it into the
    // right-hand drawer, completing the pair needed for the comparison to run.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Sets the destination partition — the target representing post-edit server state.
     * Call this before {@link #computeModifications()} if you used the no-arg constructor.
     *
     * @param destinationPartition  the partition to treat as the "after" side of the diff
     */
    public void setDestinationPartition( Partition destinationPartition )
    {
        this.destinationPartition = destinationPartition;
    }
}
