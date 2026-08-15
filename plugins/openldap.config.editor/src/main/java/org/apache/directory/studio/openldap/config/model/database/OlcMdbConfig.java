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
package org.apache.directory.studio.openldap.config.model.database;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.openldap.common.ui.model.DatabaseTypeEnum;
import org.apache.directory.studio.openldap.config.model.ConfigurationElement;


// ── CLASS: OlcMdbConfig — Palpatine's Primary LMDB Data Vault ─────────────────
// Palpatine's most powerful vault uses MDB — the Lightning Memory-Mapped Database,
// which is the current primary backend for modern OpenLDAP deployments. Unlike the
// older BDB backend, MDB uses a single memory-mapped file, is crash-safe, and
// doesn't need periodic recovery. You set a maximum size up front (it can't grow
// automatically) and then it just works. OlcMdbConfig captures all the knobs
// Palpatine can turn: directory location, index configuration, size caps, search
// stack depth, environment flags, and sync behaviour.
// NOTE: the setter for olcDbEnvFlags is spelled `setOlcDbEnvFlagsx` (with a trailing x)
// in the original source — that's a typo, but we preserve it to avoid breaking callers.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcMdbConfig} object class, representing the
 * configuration of the OpenLDAP MDB (Lightning Memory-Mapped Database) backend.
 * There are a few parameters that can be managed for the MDB database:
 * <ul>
 *   <li>{@code olcDbDirectory} — the directory on disk where MDB stores its files (required)</li>
 *   <li>{@code olcDbCheckpoint} — how often to flush dirty data to disk (kbytes ops-interval)</li>
 *   <li>{@code olcDbEnvFlags} — LMDB environment flags (writemap, nometasync, etc.)</li>
 *   <li>{@code olcDbIndex} — attribute indexes to maintain for fast searches</li>
 *   <li>{@code olcDbMaxEntrySize} — cap on the size of a single LDAP entry in bytes</li>
 *   <li>{@code olcDbMaxReaders} — maximum concurrent read transactions</li>
 *   <li>{@code olcDbMaxSize} — maximum database file size in bytes; set it to the expected
 *       peak because LMDB cannot grow the map automatically</li>
 *   <li>{@code olcDbMode} — file permission mode for the database files</li>
 *   <li>{@code olcDbNoSync} — if true, writes are not flushed to disk after each commit
 *       (faster but less crash-safe)</li>
 *   <li>{@code olcDbSearchStack} — depth of the stack used for search operations</li>
 * </ul>
 * Think of this as Palpatine's primary LMDB vault configuration — every setting here
 * controls how the most important data store in the galaxy is tuned and protected.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcMdbConfig extends OlcDatabaseConfig
{
    /**
     * Field for the 'olcDbDirectory' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbDirectory", isOptional = false, version="2.4.0")
    private String olcDbDirectory;

    /**
     * Field for the 'olcDbCheckpoint' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbCheckpoint", version="2.4.0")
    private String olcDbCheckpoint;

    /**
     * Field for the 'olcDbEnvFlags' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbEnvFlags", version="2.4.33")
    private List<String> olcDbEnvFlags = new ArrayList<>();

    /**
     * Field for the 'olcDbIndex' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbIndex", version="2.4.0")
    private List<String> olcDbIndex = new ArrayList<>();

    /**
     * Field for the 'olcDbMaxEntrySize' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbMaxEntrySize", version="2.4.42-2")
    private Integer olcDbMaxEntrySize;

    /**
     * Field for the 'olcDbMaxReaders' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbMaxReaders", version="2.4.27")
    private Integer olcDbMaxReaders;

    /**
     * Field for the 'olcDbMaxSize' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbMaxSize", version="2.4.27")
    private Long olcDbMaxSize;

    /**
     * Field for the 'olcDbMode' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbMode", version="2.4.27")
    private String olcDbMode;

    /**
     * Field for the 'olcDbNoSync' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbNoSync", version="2.4.27")
    private Boolean olcDbNoSync;

    /**
     * Field for the 'olcDbSearchStack' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbSearchStack", version="2.4.27")
    private Integer olcDbSearchStack;


    // ── addOlcDbIndex — Palpatine Adds Index Entries to the Vault ─────────────────
    // Palpatine augments his vault's lookup tables. Indexes speed up searches on
    // specific attributes; without them, every search scans the entire database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more index definition strings to the {@code olcDbIndex} list.
     * Each string describes which attribute(s) to index and how (e.g., "uid eq,pres").
     *
     * <p>For example — Palpatine adds indexes to his vault:</p>
     * <pre>
     *   mdbConfig.addOlcDbIndex( "uid eq,pres", "cn sub" );
     * </pre>
     *
     * @param strings  the index definition strings to add
     */
    public void addOlcDbIndex( String... strings )
    {
        for ( String string : strings )
        {
            olcDbIndex.add( string );
        }
    }


    // ── addOlcDbEnvFlags — Palpatine Sets LMDB Environment Flags ─────────────────
    // Palpatine tunes the LMDB engine with environment flags — options like
    // "writemap" (direct writes into the memory map) or "nosync" (skip disk flushing).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more LMDB environment flag strings to the {@code olcDbEnvFlags} list.
     * Common flags include "writemap", "nometasync", "mapasync".
     *
     * <p>For example — Palpatine adds environment flags:</p>
     * <pre>
     *   mdbConfig.addOlcDbEnvFlags( "writemap", "nometasync" );
     * </pre>
     *
     * @param strings  the environment flag strings to add
     */
    public void addOlcDbEnvFlags( String... strings )
    {
        for ( String string : strings )
        {
            olcDbEnvFlags.add( string );
        }
    }


    // ── clearOlcDbIndex — Palpatine Clears All Index Entries ──────────────────────
    // Palpatine clears the vault's index definitions, removing all maintained indexes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all index definition strings from the {@code olcDbIndex} list.
     *
     * <p>For example — Palpatine removes all indexes:</p>
     * <pre>
     *   mdbConfig.clearOlcDbIndex();
     * </pre>
     */
    public void clearOlcDbIndex()
    {
        olcDbIndex.clear();
    }


    // ── clearOlcDbEnvFlags — Palpatine Clears All Environment Flags ───────────────
    // Palpatine clears all LMDB environment flags, reverting to default engine behaviour.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all LMDB environment flag strings from the {@code olcDbEnvFlags} list.
     *
     * <p>For example — Palpatine removes all environment flags:</p>
     * <pre>
     *   mdbConfig.clearOlcDbEnvFlags();
     * </pre>
     */
    public void clearOlcDbEnvFlags()
    {
        olcDbEnvFlags.clear();
    }


    // ── getOlcDbCheckpoint — Palpatine Reads the Checkpoint Schedule ──────────────
    // Palpatine checks how often the vault flushes its dirty pages to disk — the
    // checkpoint setting is a "kbytes ops" pair controlling flush frequency.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the checkpoint setting for this MDB database.
     * The value is a string like "1024 100", meaning flush to disk every 1024 kbytes
     * written or every 100 operations, whichever comes first.
     *
     * <p>For example — Palpatine reads the flush schedule:</p>
     * <pre>
     *   String cp = mdbConfig.getOlcDbCheckpoint(); // "1024 100"
     * </pre>
     *
     * @return  the checkpoint string, or null if not set
     */
    public String getOlcDbCheckpoint()
    {
        return olcDbCheckpoint;
    }


    // ── getOlcDbDirectory — Palpatine Reads the Vault's Data Directory ────────────
    // Palpatine reads the filesystem path where the MDB data file lives. This is
    // where slapd actually stores the entries — it's required and must exist.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the directory path where MDB stores its data files.
     * This is required — MDB won't start without a valid writable directory here.
     *
     * <p>For example — Palpatine reads the vault path:</p>
     * <pre>
     *   String dir = mdbConfig.getOlcDbDirectory(); // "/var/lib/ldap"
     * </pre>
     *
     * @return  the directory path string, or null if not yet set
     */
    public String getOlcDbDirectory()
    {
        return olcDbDirectory;
    }


    // ── getOlcDbIndex — Palpatine Reads the Index Table Definitions ───────────────
    // Palpatine reads the list of attribute indexes the vault maintains for fast lookup.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the index definition strings.
     * Each string identifies an attribute and the types of indexes maintained for it.
     *
     * <p>For example — Palpatine reads the index definitions:</p>
     * <pre>
     *   List&lt;String&gt; idx = mdbConfig.getOlcDbIndex();
     * </pre>
     *
     * @return  a copy of the index list; never null
     */
    public List<String> getOlcDbIndex()
    {
        return copyListString( olcDbIndex );
    }


    // ── getOlcDbEnvFlags — Palpatine Reads the LMDB Environment Flags ────────────
    // Palpatine reads the LMDB engine tuning flags currently configured for the vault.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the LMDB environment flag strings.
     *
     * <p>For example — Palpatine reads the environment flags:</p>
     * <pre>
     *   List&lt;String&gt; flags = mdbConfig.getOlcDbEnvFlags();
     * </pre>
     *
     * @return  a copy of the environment flags list; never null
     */
    public List<String> getOlcDbEnvFlags()
    {
        return copyListString( olcDbEnvFlags );
    }


    // ── getOlcDbMaxEntrySize — Palpatine Reads the Entry Size Cap ─────────────────
    // Palpatine reads the maximum byte size of a single LDAP entry. Entries larger
    // than this will be rejected. Useful when you want to prevent huge entries from
    // clogging up the database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum byte size of a single LDAP entry for this MDB database.
     * If set, entries larger than this value are rejected on write.
     *
     * <p>For example — Palpatine reads the entry size limit:</p>
     * <pre>
     *   Integer max = mdbConfig.getOlcDbMaxEntrySize(); // 65536
     * </pre>
     *
     * @return  the max entry size in bytes, or null if not set
     */
    public Integer getOlcDbMaxEntrySize()
    {
        return olcDbMaxEntrySize;
    }


    // ── getOlcDbMaxReaders — Palpatine Reads the Concurrent Reader Cap ────────────
    // Palpatine reads the cap on simultaneous read transactions. LMDB supports many
    // concurrent readers without locking, but there's a fixed upper bound.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum number of concurrent read transactions for this MDB database.
     * LMDB has a fixed reader table — once this is full, new read transactions block.
     *
     * <p>For example — Palpatine reads the reader cap:</p>
     * <pre>
     *   Integer max = mdbConfig.getOlcDbMaxReaders(); // 128
     * </pre>
     *
     * @return  the max reader count, or null if not set
     */
    public Integer getOlcDbMaxReaders()
    {
        return olcDbMaxReaders;
    }


    // ── getOlcDbMaxSize — Palpatine Reads the Vault's Maximum Size ────────────────
    // Palpatine reads the maximum size of the MDB memory-mapped database in bytes.
    // This is set once and cannot be grown automatically — the vault can't exceed it.
    // If entries fill it up, writes start failing. Plan ahead and set it generously.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum size of the MDB database file in bytes.
     * LMDB maps the entire database into virtual memory as a fixed-size file.
     * Set this to the expected peak size — it cannot grow automatically.
     *
     * <p>For example — Palpatine reads the vault size limit:</p>
     * <pre>
     *   Long max = mdbConfig.getOlcDbMaxSize(); // 1073741824L (1 GB)
     * </pre>
     *
     * @return  the maximum database size in bytes, or null if not set
     */
    public Long getOlcDbMaxSize()
    {
        return olcDbMaxSize;
    }


    // ── getOlcDbMode — Palpatine Reads the File Permission Mode ───────────────────
    // Palpatine reads the file permission mode for the vault's data files — controls
    // which operating-system users can read or write the MDB files directly.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the file permission mode for the MDB data files, typically in octal notation.
     *
     * <p>For example — Palpatine reads the file permissions:</p>
     * <pre>
     *   String mode = mdbConfig.getOlcDbMode(); // "0600"
     * </pre>
     *
     * @return  the file permission mode string, or null if not set
     */
    public String getOlcDbMode()
    {
        return olcDbMode;
    }


    // ── getOlcDbNoSync — Palpatine Reads the Sync-Skip Flag ───────────────────────
    // Palpatine checks whether the vault skips syncing data to disk after each write.
    // Skipping sync is faster but means data written between crashes might be lost.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether disk sync is disabled after each write transaction.
     * If {@code true}, writes are faster but data may be lost on an unclean shutdown.
     *
     * <p>For example — Palpatine reads the sync-skip flag:</p>
     * <pre>
     *   Boolean noSync = mdbConfig.getOlcDbNoSync(); // true
     * </pre>
     *
     * @return  true if sync is disabled, false if sync is enforced, null if not set
     */
    public Boolean getOlcDbNoSync()
    {
        return olcDbNoSync;
    }


    // ── getOlcDbSearchStack — Palpatine Reads the Search Stack Depth ──────────────
    // Palpatine reads how deep the internal search stack can grow. Search operations
    // navigate the LMDB B-tree using a stack; this is its maximum depth.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the depth of the internal search stack for this MDB database.
     * Higher values let searches traverse deeper trees but use more memory per thread.
     *
     * <p>For example — Palpatine reads the search stack depth:</p>
     * <pre>
     *   Integer depth = mdbConfig.getOlcDbSearchStack(); // 16
     * </pre>
     *
     * @return  the search stack depth, or null if not set
     */
    public Integer getOlcDbSearchStack()
    {
        return olcDbSearchStack;
    }


    // ── setOlcDbCheckpoint — Palpatine Sets the Checkpoint Schedule ───────────────
    // Palpatine sets how often the vault flushes dirty pages to disk. The string
    // encodes two thresholds: bytes written and number of operations.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the checkpoint flush schedule for this MDB database.
     * The format is "kbytes ops" — e.g., "1024 100" means flush every 1024 kbytes
     * or every 100 operations.
     *
     * <p>For example — Palpatine sets the flush schedule:</p>
     * <pre>
     *   mdbConfig.setOlcDbCheckpoint( "1024 100" );
     * </pre>
     *
     * @param olcDbCheckpoint  the checkpoint string to set
     */
    public void setOlcDbCheckpoint( String olcDbCheckpoint )
    {
        this.olcDbCheckpoint = olcDbCheckpoint;
    }


    // ── setOlcDbDirectory — Palpatine Sets the Vault Data Directory ───────────────
    // Palpatine points the vault to a directory on disk where the MDB data file will
    // live. This is required; without it the database won't start.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the directory path where MDB stores its data files.
     * This is required; the directory must exist and be writable by slapd.
     *
     * <p>For example — Palpatine sets the vault path:</p>
     * <pre>
     *   mdbConfig.setOlcDbDirectory( "/var/lib/ldap" );
     * </pre>
     *
     * @param olcDbDirectory  the directory path string (required)
     */
    public void setOlcDbDirectory( String olcDbDirectory )
    {
        this.olcDbDirectory = olcDbDirectory;
    }


    // ── setOlcDbIndex — Palpatine Replaces the Index Table Definitions ────────────
    // Palpatine replaces the entire set of index definitions for the vault.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the list of index definition strings for this MDB database.
     *
     * <p>For example — Palpatine replaces all indexes:</p>
     * <pre>
     *   mdbConfig.setOlcDbIndex( Arrays.asList( "uid eq,pres", "cn sub" ) );
     * </pre>
     *
     * @param olcDbIndex  the new list of index definition strings
     */
    public void setOlcDbIndex( List<String> olcDbIndex )
    {
        this.olcDbIndex = copyListString( olcDbIndex );
    }


    // ── setOlcDbEnvFlagsx — Palpatine Replaces the LMDB Environment Flags ─────────
    // Palpatine replaces the full set of LMDB environment flags.
    // NOTE: this method is intentionally named setOlcDbEnvFlagsx (with trailing x) —
    // that is a typo in the original source code that we preserve to avoid breaking callers.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the list of LMDB environment flag strings for this MDB database.
     * <b>Note:</b> this method is named {@code setOlcDbEnvFlagsx} (with a trailing "x")
     * — that is a typo in the original source. We preserve it as-is to avoid breaking
     * any callers that depend on this exact name.
     *
     * <p>For example — Palpatine replaces the environment flags:</p>
     * <pre>
     *   mdbConfig.setOlcDbEnvFlagsx( Arrays.asList( "writemap" ) );
     * </pre>
     *
     * @param olcDbEnvFlags  the new list of environment flag strings
     */
    public void setOlcDbEnvFlagsx( List<String> olcDbEnvFlags )
    {
        this.olcDbEnvFlags = copyListString( olcDbEnvFlags );
    }


    // ── setOlcDbMaxEntrySize — Palpatine Sets the Maximum Entry Byte Size ─────────
    // Palpatine caps the size of any single LDAP entry stored in the vault. Entries
    // above this threshold are rejected, preventing bloated entries from eating up space.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the maximum byte size of a single LDAP entry for this MDB database.
     * Entries larger than this value are rejected on write.
     *
     * <p>For example — Palpatine sets the entry size limit:</p>
     * <pre>
     *   mdbConfig.setOlcDbMaxEntrySize( 65536 );
     * </pre>
     *
     * @param olcDbMaxEntrySize  the max entry size in bytes
     */
    public void setOlcDbMaxEntrySize( Integer olcDbMaxEntrySize )
    {
        this.olcDbMaxEntrySize = olcDbMaxEntrySize;
    }


    // ── setOlcDbMaxReaders — Palpatine Sets the Concurrent Reader Cap ─────────────
    // Palpatine sets the cap on simultaneous reader transactions. LMDB pre-allocates
    // a fixed reader table — any connection beyond this limit will be blocked.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the maximum number of concurrent read transactions for this MDB database.
     *
     * <p>For example — Palpatine sets the reader cap:</p>
     * <pre>
     *   mdbConfig.setOlcDbMaxReaders( 128 );
     * </pre>
     *
     * @param olcDbMaxReaders  the max reader count
     */
    public void setOlcDbMaxReaders( Integer olcDbMaxReaders )
    {
        this.olcDbMaxReaders = olcDbMaxReaders;
    }


    // ── setOlcDbMaxSize — Palpatine Sets the Vault's Maximum Size ─────────────────
    // Palpatine sets the maximum size of the MDB memory-mapped file. This is a
    // one-time setting — the vault can never exceed it. Too small and writes start
    // failing; too large and you waste virtual address space. Plan generously.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the maximum size of the MDB database file in bytes.
     * LMDB can't grow this limit dynamically — set it larger than you ever expect to need.
     *
     * <p>For example — Palpatine sets the vault size limit to 1 GB:</p>
     * <pre>
     *   mdbConfig.setOlcDbMaxSize( 1073741824L );
     * </pre>
     *
     * @param olcDbMaxSize  the maximum database size in bytes
     */
    public void setOlcDbMaxSize( Long olcDbMaxSize )
    {
        this.olcDbMaxSize = olcDbMaxSize;
    }


    // ── setOlcDbMode — Palpatine Sets the File Permission Mode ────────────────────
    // Palpatine sets the filesystem permission mode for the vault's data files,
    // controlling which OS users can access the raw MDB files.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the file permission mode for MDB data files, typically as an octal string.
     *
     * <p>For example — Palpatine sets file permissions:</p>
     * <pre>
     *   mdbConfig.setOlcDbMode( "0600" );
     * </pre>
     *
     * @param olcDbMode  the file permission mode string
     */
    public void setOlcDbMode( String olcDbMode )
    {
        this.olcDbMode = olcDbMode;
    }


    // ── setOlcDbNoSync — Palpatine Toggles the Sync-Skip Flag ─────────────────────
    // Palpatine decides whether to skip disk sync after writes. Disabling sync is
    // faster but risky — unsynced data can be lost if the process crashes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether disk sync is disabled after each write transaction.
     * Set to {@code true} for better write performance at the cost of crash safety.
     *
     * <p>For example — Palpatine disables sync for speed:</p>
     * <pre>
     *   mdbConfig.setOlcDbNoSync( true );
     * </pre>
     *
     * @param olcDbNoSync  true to disable sync; false to enforce it
     */
    public void setOlcDbNoSync( Boolean olcDbNoSync )
    {
        this.olcDbNoSync = olcDbNoSync;
    }


    // ── setOlcDbSearchStack — Palpatine Sets the Search Stack Depth ───────────────
    // Palpatine sets the maximum depth of the search stack. Deeper stacks support
    // more complex B-tree traversals but consume more per-thread memory.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the depth of the internal search stack for this MDB database.
     *
     * <p>For example — Palpatine sets the search stack depth:</p>
     * <pre>
     *   mdbConfig.setOlcDbSearchStack( 16 );
     * </pre>
     *
     * @param olcDbSearchStack  the search stack depth
     */
    public void setOlcDbSearchStack( Integer olcDbSearchStack )
    {
        this.olcDbSearchStack = olcDbSearchStack;
    }


    // ── getOlcDatabaseType — Palpatine Identifies the MDB Vault Type ──────────────
    // Palpatine identifies this vault as the "mdb" type so OpenLDAP loads the
    // correct LMDB backend plugin. This is the preferred modern backend.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the database type identifier string "mdb", used as the type
     * discriminator in the cn=config DIT.
     *
     * <p>For example — Palpatine identifies the MDB vault type:</p>
     * <pre>
     *   mdbConfig.getOlcDatabaseType(); // "mdb"
     * </pre>
     *
     * @return  the lowercase database type string "mdb"
     */
    @Override
    public String getOlcDatabaseType()
    {
        return DatabaseTypeEnum.MDB.toString().toLowerCase();
    }


    // ── toString — Palpatine Summarises the Vault in One Line ─────────────────────
    // When Palpatine needs a quick reference to the vault, he shows its type plus its
    // first suffix DN — e.g., "mdb:dc=example,dc=com". If there's no suffix yet, he
    // just shows the database type alone.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable summary of this MDB database configuration.
     * Combines the database type with the first configured suffix DN, e.g.,
     * {@code "mdb:dc=example,dc=com"}.
     *
     * <p>For example — Palpatine reads the vault label:</p>
     * <pre>
     *   mdbConfig.toString(); // "mdb:dc=example,dc=com"
     * </pre>
     *
     * @return  a string of the form "type:suffix" or just "type" if no suffix is set
     */
    public String toString()
    {
        if ( !getOlcSuffix().isEmpty() )
        {
            return getOlcDatabase() + ":" + getOlcSuffix().get( 0 );
        }
        else
        {
            return getOlcDatabase();
        }
    }
}
