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


// ── CLASS: OlcBdbConfig — Palpatine's BerkeleyDB Vault (The Classic Vault) ─────
// Before LMDB arrived, Palpatine's vault of choice was BerkeleyDB (BDB) — a proven,
// battle-tested transactional database from Sleepycat Software (now Oracle). BDB
// uses a complex on-disk format with lock detection, shared memory keys, and
// page caching. It's more configurable than MDB but also more maintenance-intensive:
// it needs periodic recovery after crashes and a whole DB environment directory.
// OlcBdbConfig holds all the BDB-specific knobs: directory, cache sizes, lock
// detection, DN and IDL cache sizes, checkpointing, index definitions, and even
// encryption keys for at-rest data protection. OlcHdbConfig inherits from this.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcBdbConfig} object class, representing the
 * configuration of the OpenLDAP BDB (BerkeleyDB) backend database.
 * BDB is the original high-performance backend for OpenLDAP, using the BerkeleyDB
 * embedded database engine with full ACID transactional support.
 * Subclassed by {@link OlcHdbConfig} (which adds hierarchical DN indexing).
 * Think of this as Palpatine's classic BerkeleyDB vault, fully configurable
 * right down to its encryption keys and lock-detection algorithm.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcBdbConfig extends OlcDatabaseConfig
{
    /**
     * Field for the 'olcDbDirectory' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbDirectory", isOptional = false, version="2.4.0")
    private String olcDbDirectory;

    /**
     * Field for the 'olcDbCacheFree' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbCacheFree", version="2.4.0")
    private Integer olcDbCacheFree;

    /**
     * Field for the 'olcDbCacheSize' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbCacheSize", version="2.4.0")
    private Integer olcDbCacheSize;

    /**
     * Field for the 'olcDbCheckpoint' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbCheckpoint", version="2.4.0")
    private String olcDbCheckpoint;

    /**
     * Field for the 'olcDbConfig' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbConfig", version="2.4.0")
    private List<String> olcDbConfig = new ArrayList<>();

    /**
     * Field for the 'olcDbCryptFile' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbCryptFile", version="2.4.8")
    private String olcDbCryptFile;

    /**
     * Field for the 'olcDbCryptKey' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbCryptKey", version="2.4.8")
    private byte[] olcDbCryptKey;

    /**
     * Field for the 'olcDbDirtyRead' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbDirtyRead", version="2.4.0")
    private Boolean olcDbDirtyRead;

    /**
     * Field for the 'olcDbDNcacheSize' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbDNcacheSize", version="2.4.0")
    private Integer olcDbDNcacheSize;

    /**
     * Field for the 'olcDbIDLcacheSize' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbIDLcacheSize", version="2.4.0")
    private Integer olcDbIDLcacheSize;

    /**
     * Field for the 'olcDbIndex' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbIndex", version="2.4.0")
    private List<String> olcDbIndex = new ArrayList<>();

    /**
     * Field for the 'olcDbLinearIndex' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbLinearIndex", version="2.4.0")
    private Boolean olcDbLinearIndex;

    /**
     * Field for the 'olcDbLockDetect' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbLockDetect", version="2.4.0")
    private String olcDbLockDetect;

    /**
     * Field for the 'olcDbMode' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbMode", version="2.4.0")
    private String olcDbMode;

    /**
     * Field for the 'olcDbNoSync' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbNoSync", version="2.4.0")
    private Boolean olcDbNoSync;

    /**
     * Field for the 'olcDbPageSize' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbPageSize", version="2.4.13")
    private List<String> olcDbPageSize = new ArrayList<>();

    /**
     * Field for the 'olcDbSearchStack' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbSearchStack", version="2.4.0")
    private Integer olcDbSearchStack;

    /**
     * Field for the 'olcDbShmKey' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbShmKey", version="2.4.0")
    private Integer olcDbShmKey;


    // ── addOlcDbConfig — Palpatine Adds BDB Config Lines to the Vault ─────────────
    // Palpatine adds raw BerkeleyDB configuration directives — these are lines that
    // get written into a DB_CONFIG file inside the data directory. BDB reads that file
    // at startup to tune things like cache size and log parameters.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more BerkeleyDB DB_CONFIG directive strings to the config list.
     * These lines get written into the DB_CONFIG file in the data directory.
     *
     * <p>For example — Palpatine adds BDB config directives:</p>
     * <pre>
     *   bdbConfig.addOlcDbConfig( "set_cachesize 0 1048576 0" );
     * </pre>
     *
     * @param strings  the DB_CONFIG directive strings to add
     */
    public void addOlcDbConfig( String... strings )
    {
        for ( String string : strings )
        {
            olcDbConfig.add( string );
        }
    }


    // ── addOlcDbIndex — Palpatine Adds Index Definitions to the Vault ─────────────
    // Palpatine augments the vault's lookup tables. Each string specifies which
    // attribute(s) to index and with what type (eq, pres, sub, approx).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more index definition strings to the {@code olcDbIndex} list.
     * Each string describes an attribute and the types of indexes to maintain,
     * e.g. "uid eq,pres".
     *
     * <p>For example — Palpatine adds index definitions:</p>
     * <pre>
     *   bdbConfig.addOlcDbIndex( "uid eq,pres", "cn sub" );
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


    // ── addOlcDbPageSize — Palpatine Configures BDB B-tree Page Sizes ─────────────
    // BerkeleyDB lets you tune the page size for different B-tree databases inside
    // the environment. Palpatine adds page-size specifications here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more BDB B-tree page size specification strings to the
     * {@code olcDbPageSize} list.
     *
     * <p>For example — Palpatine adds page size specs:</p>
     * <pre>
     *   bdbConfig.addOlcDbPageSize( "dn2id 4096" );
     * </pre>
     *
     * @param strings  the page size specification strings to add
     */
    public void addOlcDbPageSize( String... strings )
    {
        for ( String string : strings )
        {
            olcDbPageSize.add( string );
        }
    }


    // ── clearOlcDbConfig — Palpatine Clears All BDB Config Directives ─────────────
    // Palpatine clears all raw BerkeleyDB DB_CONFIG lines.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all BerkeleyDB DB_CONFIG directive strings.
     *
     * <p>For example — Palpatine clears the BDB config directives:</p>
     * <pre>
     *   bdbConfig.clearOlcDbConfig();
     * </pre>
     */
    public void clearOlcDbConfig()
    {
        olcDbConfig.clear();
    }


    // ── clearOlcDbIndex — Palpatine Clears All Index Definitions ──────────────────
    // Palpatine removes all attribute index definitions from the vault.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all index definition strings from the {@code olcDbIndex} list.
     *
     * <p>For example — Palpatine removes all indexes:</p>
     * <pre>
     *   bdbConfig.clearOlcDbIndex();
     * </pre>
     */
    public void clearOlcDbIndex()
    {
        olcDbIndex.clear();
    }


    // ── clearOlcDbPageSize — Palpatine Clears All Page Size Specs ─────────────────
    // Palpatine clears all BDB B-tree page size specifications.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all BDB B-tree page size specification strings from the
     * {@code olcDbPageSize} list.
     *
     * <p>For example — Palpatine clears the page size specs:</p>
     * <pre>
     *   bdbConfig.clearOlcDbPageSize();
     * </pre>
     */
    public void clearOlcDbPageSize()
    {
        olcDbPageSize.clear();
    }


    // ── getOlcDbCacheFree — Palpatine Reads the Cache-Free Entry Count ────────────
    // Palpatine reads how many entries BDB can keep in a "free" state in the cache —
    // that is, evicted from the cache but not yet written back. This is a BDB-
    // specific cache overflow knob.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the number of "free" (overflow) cache entries for this BDB database.
     * BDB uses this to manage how many old entries it keeps available for reuse
     * before evicting them fully.
     *
     * <p>For example — Palpatine reads the cache-free setting:</p>
     * <pre>
     *   Integer free = bdbConfig.getOlcDbCacheFree(); // 1
     * </pre>
     *
     * @return  the cache-free entry count, or null if not set
     */
    public Integer getOlcDbCacheFree()
    {
        return olcDbCacheFree;
    }


    // ── getOlcDbCacheSize — Palpatine Reads the In-Memory Entry Cache Size ────────
    // Palpatine reads the number of entries BDB keeps in its in-memory LRU cache.
    // Larger cache = fewer disk reads = faster searches.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the in-memory entry cache size for this BDB database.
     * This is the number of LDAP entries BDB keeps in its LRU cache before going to disk.
     *
     * <p>For example — Palpatine reads the cache size:</p>
     * <pre>
     *   Integer size = bdbConfig.getOlcDbCacheSize(); // 1000
     * </pre>
     *
     * @return  the cache size in entries, or null if not set
     */
    public Integer getOlcDbCacheSize()
    {
        return olcDbCacheSize;
    }


    // ── getOlcDbCheckpoint — Palpatine Reads the Checkpoint Schedule ──────────────
    // Palpatine reads how often BDB flushes its transaction log to disk. The
    // checkpoint setting is a "kbytes ops" pair controlling flush frequency.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the checkpoint schedule for this BDB database.
     * The value is a string like "1024 100", meaning flush every 1024 kbytes or
     * every 100 operations.
     *
     * <p>For example — Palpatine reads the checkpoint schedule:</p>
     * <pre>
     *   String cp = bdbConfig.getOlcDbCheckpoint(); // "1024 100"
     * </pre>
     *
     * @return  the checkpoint string, or null if not set
     */
    public String getOlcDbCheckpoint()
    {
        return olcDbCheckpoint;
    }


    // ── getOlcDbConfig — Palpatine Reads the Raw BDB Config Lines ─────────────────
    // Palpatine reads the raw BerkeleyDB DB_CONFIG directives — the lines that get
    // written into the DB_CONFIG file at startup.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the BerkeleyDB DB_CONFIG directive strings.
     *
     * <p>For example — Palpatine reads the BDB config directives:</p>
     * <pre>
     *   List&lt;String&gt; conf = bdbConfig.getOlcDbConfig();
     * </pre>
     *
     * @return  a copy of the DB_CONFIG directive list; never null
     */
    public List<String> getOlcDbConfig()
    {
        return copyListString( olcDbConfig );
    }


    // ── getOlcDbCryptFile — Palpatine Reads the Encryption Key File Path ──────────
    // Palpatine reads the path to the file containing the encryption key for
    // at-rest data protection. The BDB engine reads this file to decrypt entries.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the path to the encryption key file for this BDB database.
     * BDB uses this key file to encrypt data at rest, protecting vault contents.
     *
     * <p>For example — Palpatine reads the crypt key file:</p>
     * <pre>
     *   String file = bdbConfig.getOlcDbCryptFile(); // "/etc/ldap/crypt.key"
     * </pre>
     *
     * @return  the crypt key file path, or null if not set
     */
    public String getOlcDbCryptFile()
    {
        return olcDbCryptFile;
    }


    // ── getOlcDbCryptKey — Palpatine Reads the Inline Encryption Key Bytes ────────
    // Palpatine reads the raw encryption key bytes stored directly in the config.
    // We return a defensive copy to prevent callers from mutating the key in memory.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the inline encryption key bytes for this BDB database.
     * These bytes are used by BDB to encrypt data at rest.
     *
     * <p>For example — Palpatine reads the crypt key:</p>
     * <pre>
     *   byte[] key = bdbConfig.getOlcDbCryptKey();
     * </pre>
     *
     * @return  a copy of the crypt key bytes, or null if not set
     */
    public byte[] getOlcDbCryptKey()
    {
        if ( olcDbCryptKey != null )
        {
            byte[] copy = new byte[olcDbCryptKey.length];
            System.arraycopy( olcDbCryptKey, 0, copy, 0, olcDbCryptKey.length );

            return copy;
        }

        return olcDbCryptKey;
    }


    // ── getOlcDbDirectory — Palpatine Reads the Vault Data Directory ──────────────
    // Palpatine reads the directory on disk where BDB stores all its database files —
    // the data.bdb, lock, log, and checkpoint files all live here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the directory path where BDB stores its database files.
     * This is required — BDB won't start without a valid, writable directory here.
     *
     * <p>For example — Palpatine reads the vault directory:</p>
     * <pre>
     *   String dir = bdbConfig.getOlcDbDirectory(); // "/var/lib/ldap"
     * </pre>
     *
     * @return  the directory path string, or null if not yet set
     */
    public String getOlcDbDirectory()
    {
        return olcDbDirectory;
    }


    // ── getOlcDbDirtyRead — Palpatine Reads the Dirty-Read Flag ───────────────────
    // Palpatine reads whether dirty reads are allowed — BDB can read uncommitted
    // data if this is enabled, which is faster but risks reading in-flight changes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether dirty reads (reading uncommitted data) are allowed for this BDB database.
     * Enabling this sacrifices isolation for read performance.
     *
     * <p>For example — Palpatine reads the dirty-read flag:</p>
     * <pre>
     *   Boolean dirty = bdbConfig.getOlcDbDirtyRead(); // false
     * </pre>
     *
     * @return  true if dirty reads are allowed, false if not, null if not set
     */
    public Boolean getOlcDbDirtyRead()
    {
        return olcDbDirtyRead;
    }


    // ── getOlcDbDNcacheSize — Palpatine Reads the DN Cache Size ───────────────────
    // Palpatine reads the size of the in-memory DN lookup cache. Caching DN-to-ID
    // mappings avoids repeated disk lookups when resolving distinguished names.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the DN lookup cache size for this BDB database.
     * A larger DN cache speeds up operations that repeatedly resolve the same DN.
     *
     * <p>For example — Palpatine reads the DN cache size:</p>
     * <pre>
     *   Integer size = bdbConfig.getOlcDbDNcacheSize(); // 1000
     * </pre>
     *
     * @return  the DN cache size in entries, or null if not set
     */
    public Integer getOlcDbDNcacheSize()
    {
        return olcDbDNcacheSize;
    }


    // ── getOlcDbIDLcacheSize — Palpatine Reads the IDL Cache Size ─────────────────
    // Palpatine reads the size of the ID List cache — IDLs map index keys to entry IDs.
    // Caching these in memory dramatically speeds up searches on indexed attributes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the ID List (IDL) cache size for this BDB database.
     * IDLs map indexed attribute values to the list of matching entry IDs.
     * Caching them avoids disk lookups for repeated searches on the same index key.
     *
     * <p>For example — Palpatine reads the IDL cache size:</p>
     * <pre>
     *   Integer size = bdbConfig.getOlcDbIDLcacheSize(); // 4096
     * </pre>
     *
     * @return  the IDL cache size, or null if not set
     */
    public Integer getOlcDbIDLcacheSize()
    {
        return olcDbIDLcacheSize;
    }


    // ── getOlcDbIndex — Palpatine Reads the Index Table Definitions ───────────────
    // Palpatine reads the attribute index definitions maintained by the vault.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the index definition strings for this BDB database.
     * Each string identifies an attribute and the types of indexes maintained for it.
     *
     * <p>For example — Palpatine reads the index definitions:</p>
     * <pre>
     *   List&lt;String&gt; idx = bdbConfig.getOlcDbIndex();
     * </pre>
     *
     * @return  a copy of the index list; never null
     */
    public List<String> getOlcDbIndex()
    {
        return copyListString( olcDbIndex );
    }


    // ── getOlcDbLinearIndex — Palpatine Reads the Linear Index Flag ───────────────
    // Palpatine reads whether linear indexing is in use. When enabled, index B-trees
    // are placed in a single linear database file rather than separate files.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether linear indexing is enabled for this BDB database.
     * Linear indexing places all index B-trees in a single sequential file.
     *
     * <p>For example — Palpatine reads the linear index flag:</p>
     * <pre>
     *   Boolean linear = bdbConfig.getOlcDbLinearIndex(); // false
     * </pre>
     *
     * @return  true if linear indexing is enabled, false if not, null if not set
     */
    public Boolean getOlcDbLinearIndex()
    {
        return olcDbLinearIndex;
    }


    // ── getOlcDbLockDetect — Palpatine Reads the Lock Detection Algorithm ─────────
    // Palpatine reads the configured algorithm for resolving BDB transaction deadlocks.
    // When two transactions deadlock, BDB uses this setting to pick which one to abort.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the lock detection algorithm string for this BDB database.
     * Controls which transaction BDB aborts when deadlock is detected (e.g., "oldest",
     * "random", "youngest", "fewest").
     *
     * <p>For example — Palpatine reads the lock detection algorithm:</p>
     * <pre>
     *   String ld = bdbConfig.getOlcDbLockDetect(); // "random"
     * </pre>
     *
     * @return  the lock detection algorithm string, or null if not set
     */
    public String getOlcDbLockDetect()
    {
        return olcDbLockDetect;
    }


    // ── getOlcDbMode — Palpatine Reads the File Permission Mode ───────────────────
    // Palpatine reads the permission mode for BDB data files on the filesystem.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the file permission mode for BDB data files, typically as an octal string.
     *
     * <p>For example — Palpatine reads the file permissions:</p>
     * <pre>
     *   String mode = bdbConfig.getOlcDbMode(); // "0600"
     * </pre>
     *
     * @return  the file permission mode string, or null if not set
     */
    public String getOlcDbMode()
    {
        return olcDbMode;
    }


    // ── getOlcDbNoSync — Palpatine Reads the Sync-Skip Flag ───────────────────────
    // Palpatine reads whether BDB skips syncing the transaction log to disk after each
    // commit. Disabling sync gives faster writes at the cost of crash safety.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether disk sync is disabled after each write transaction for this BDB database.
     * If {@code true}, transaction log writes are asynchronous (faster, less crash-safe).
     *
     * <p>For example — Palpatine reads the sync-skip flag:</p>
     * <pre>
     *   Boolean noSync = bdbConfig.getOlcDbNoSync(); // false
     * </pre>
     *
     * @return  true if sync is disabled, false if not, null if not set
     */
    public Boolean getOlcDbNoSync()
    {
        return olcDbNoSync;
    }


    // ── getOlcDbPageSize — Palpatine Reads the BDB B-tree Page Sizes ─────────────
    // Palpatine reads the page size specifications for BDB's internal B-tree databases.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the BDB B-tree page size specification strings.
     *
     * <p>For example — Palpatine reads the page size specs:</p>
     * <pre>
     *   List&lt;String&gt; pages = bdbConfig.getOlcDbPageSize();
     * </pre>
     *
     * @return  a copy of the page size list; never null
     */
    public List<String> getOlcDbPageSize()
    {
        return copyListString( olcDbPageSize );
    }


    // ── getOlcDbSearchStack — Palpatine Reads the Search Stack Depth ──────────────
    // Palpatine reads the maximum depth of the search stack used during LDAP searches.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the depth of the internal search stack for this BDB database.
     *
     * <p>For example — Palpatine reads the search stack depth:</p>
     * <pre>
     *   Integer depth = bdbConfig.getOlcDbSearchStack(); // 16
     * </pre>
     *
     * @return  the search stack depth, or null if not set
     */
    public Integer getOlcDbSearchStack()
    {
        return olcDbSearchStack;
    }


    // ── getOlcDbShmKey — Palpatine Reads the Shared-Memory Key ────────────────────
    // Palpatine reads the System V shared memory key. BDB uses this to share its
    // cache between multiple processes, reducing memory usage in multi-process setups.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the System V shared memory key for this BDB database.
     * BDB uses shared memory (identified by this key) to allow multiple processes to
     * share the same database cache.
     *
     * <p>For example — Palpatine reads the shared memory key:</p>
     * <pre>
     *   Integer key = bdbConfig.getOlcDbShmKey(); // 12345
     * </pre>
     *
     * @return  the shared memory key, or null if not set
     */
    public Integer getOlcDbShmKey()
    {
        return olcDbShmKey;
    }


    // ── setOlcDbCacheFree — Palpatine Sets the Cache-Free Entry Count ─────────────
    // Palpatine sets how many entries BDB keeps available in its free-list before
    // evicting them completely. This is a fine-grained BDB cache overflow knob.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the "cache free" entry count for this BDB database.
     *
     * <p>For example — Palpatine sets the cache-free count:</p>
     * <pre>
     *   bdbConfig.setOlcDbCacheFree( 1 );
     * </pre>
     *
     * @param olcDbCacheFree  the cache-free entry count to set
     */
    public void setOlcDbCacheFree( Integer olcDbCacheFree )
    {
        this.olcDbCacheFree = olcDbCacheFree;
    }


    // ── setOlcDbCacheSize — Palpatine Sets the In-Memory Entry Cache Size ─────────
    // Palpatine sets how many entries BDB keeps in its LRU in-memory cache.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the in-memory entry cache size for this BDB database.
     *
     * <p>For example — Palpatine sets the cache size:</p>
     * <pre>
     *   bdbConfig.setOlcDbCacheSize( 1000 );
     * </pre>
     *
     * @param olcDbCacheSize  the cache size in entries to set
     */
    public void setOlcDbCacheSize( Integer olcDbCacheSize )
    {
        this.olcDbCacheSize = olcDbCacheSize;
    }


    // ── setOlcDbCheckpoint — Palpatine Sets the Checkpoint Schedule ───────────────
    // Palpatine sets how often BDB flushes its transaction log.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the checkpoint flush schedule for this BDB database.
     * The format is "kbytes ops" — e.g., "1024 100".
     *
     * <p>For example — Palpatine sets the checkpoint schedule:</p>
     * <pre>
     *   bdbConfig.setOlcDbCheckpoint( "1024 100" );
     * </pre>
     *
     * @param olcDbCheckpoint  the checkpoint string to set
     */
    public void setOlcDbCheckpoint( String olcDbCheckpoint )
    {
        this.olcDbCheckpoint = olcDbCheckpoint;
    }


    // ── setOlcDbConfig — Palpatine Replaces the BDB Config Directives ─────────────
    // Palpatine replaces all BerkeleyDB DB_CONFIG directive strings.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the list of BerkeleyDB DB_CONFIG directive strings.
     *
     * <p>For example — Palpatine replaces the BDB config directives:</p>
     * <pre>
     *   bdbConfig.setOlcDbConfig( Arrays.asList( "set_cachesize 0 1048576 0" ) );
     * </pre>
     *
     * @param olcDbConfig  the new list of DB_CONFIG directive strings
     */
    public void setOlcDbConfig( List<String> olcDbConfig )
    {
        this.olcDbConfig = copyListString( olcDbConfig );
    }


    // ── setOlcDbCryptFile — Palpatine Sets the Encryption Key File Path ───────────
    // Palpatine sets the path to the file containing the encryption key for BDB's
    // at-rest data encryption feature.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the path to the encryption key file for this BDB database.
     *
     * <p>For example — Palpatine sets the crypt key file:</p>
     * <pre>
     *   bdbConfig.setOlcDbCryptFile( "/etc/ldap/crypt.key" );
     * </pre>
     *
     * @param olcDbCryptFile  the crypt key file path to set
     */
    public void setOlcDbCryptFile( String olcDbCryptFile )
    {
        this.olcDbCryptFile = olcDbCryptFile;
    }


    // ── setOlcDbCryptKey — Palpatine Sets the Inline Encryption Key Bytes ─────────
    // Palpatine sets raw encryption key bytes directly in the config — we defensively
    // copy the array so external changes to the caller's byte array don't corrupt ours.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the inline encryption key bytes for this BDB database.
     * We defensively copy the input array to prevent external mutation of the key.
     *
     * <p>For example — Palpatine sets the crypt key bytes:</p>
     * <pre>
     *   bdbConfig.setOlcDbCryptKey( myKeyBytes );
     * </pre>
     *
     * @param olcDbCryptKey  the encryption key bytes to set
     */
    public void setOlcDbCryptKey( byte[] olcDbCryptKey )
    {
        if ( olcDbCryptKey != null )
        {
            this.olcDbCryptKey = new byte[olcDbCryptKey.length];
            System.arraycopy( olcDbCryptKey, 0, this.olcDbCryptKey, 0, olcDbCryptKey.length );
        }
        else
        {
            this.olcDbCryptKey = olcDbCryptKey;
        }
    }


    // ── setOlcDbDirectory — Palpatine Sets the Vault Data Directory ───────────────
    // Palpatine sets the directory on disk where BDB stores all its database files.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the directory path where BDB stores its database files.
     * This is required; the directory must exist and be writable by slapd.
     *
     * <p>For example — Palpatine sets the vault directory:</p>
     * <pre>
     *   bdbConfig.setOlcDbDirectory( "/var/lib/ldap" );
     * </pre>
     *
     * @param olcDbDirectory  the directory path string (required)
     */
    public void setOlcDbDirectory( String olcDbDirectory )
    {
        this.olcDbDirectory = olcDbDirectory;
    }


    // ── setOlcDbDirtyRead — Palpatine Sets the Dirty-Read Flag ────────────────────
    // Palpatine decides whether BDB reads can see uncommitted data from other
    // concurrent transactions.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether dirty reads (reading uncommitted data) are allowed for this BDB database.
     *
     * <p>For example — Palpatine disables dirty reads:</p>
     * <pre>
     *   bdbConfig.setOlcDbDirtyRead( false );
     * </pre>
     *
     * @param olcDbDirtyRead  true to enable dirty reads; false to disallow them
     */
    public void setOlcDbDirtyRead( Boolean olcDbDirtyRead )
    {
        this.olcDbDirtyRead = olcDbDirtyRead;
    }


    // ── setOlcDbDNcacheSize — Palpatine Sets the DN Cache Size ────────────────────
    // Palpatine sets the number of DN-to-ID mappings to cache in memory for fast
    // repeated lookups.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the DN lookup cache size for this BDB database.
     *
     * <p>For example — Palpatine sets the DN cache size:</p>
     * <pre>
     *   bdbConfig.setOlcDbDNcacheSize( 1000 );
     * </pre>
     *
     * @param olcDbDNcacheSize  the DN cache size in entries to set
     */
    public void setOlcDbDNcacheSize( Integer olcDbDNcacheSize )
    {
        this.olcDbDNcacheSize = olcDbDNcacheSize;
    }


    // ── setOlcDbIDLcacheSize — Palpatine Sets the IDL Cache Size ──────────────────
    // Palpatine sets the number of ID List cache slots for fast index-based lookups.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the ID List (IDL) cache size for this BDB database.
     *
     * <p>For example — Palpatine sets the IDL cache size:</p>
     * <pre>
     *   bdbConfig.setOlcDbIDLcacheSize( 4096 );
     * </pre>
     *
     * @param olcDbIDLcacheSize  the IDL cache size to set
     */
    public void setOlcDbIDLcacheSize( Integer olcDbIDLcacheSize )
    {
        this.olcDbIDLcacheSize = olcDbIDLcacheSize;
    }


    // ── setOlcDbIndex — Palpatine Replaces All Index Definitions ──────────────────
    // Palpatine replaces the full set of attribute index definitions for the vault.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the list of index definition strings for this BDB database.
     *
     * <p>For example — Palpatine replaces all indexes:</p>
     * <pre>
     *   bdbConfig.setOlcDbIndex( Arrays.asList( "uid eq,pres", "cn sub" ) );
     * </pre>
     *
     * @param olcDbIndex  the new list of index definition strings
     */
    public void setOlcDbIndex( List<String> olcDbIndex )
    {
        this.olcDbIndex = copyListString( olcDbIndex );
    }


    // ── setOlcDbLinearIndex — Palpatine Sets the Linear Index Flag ────────────────
    // Palpatine enables or disables BDB linear indexing.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether linear indexing is enabled for this BDB database.
     *
     * <p>For example — Palpatine disables linear indexing:</p>
     * <pre>
     *   bdbConfig.setOlcDbLinearIndex( false );
     * </pre>
     *
     * @param olcDbLinearIndex  true to enable linear indexing; false to disable it
     */
    public void setOlcDbLinearIndex( Boolean olcDbLinearIndex )
    {
        this.olcDbLinearIndex = olcDbLinearIndex;
    }


    // ── setOlcDbLockDetect — Palpatine Sets the Lock Detection Algorithm ──────────
    // Palpatine chooses which transaction BDB aborts when it detects a deadlock.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the lock detection algorithm for this BDB database.
     * Common values are "oldest", "youngest", "random", "fewest".
     *
     * <p>For example — Palpatine sets the lock detection algorithm:</p>
     * <pre>
     *   bdbConfig.setOlcDbLockDetect( "random" );
     * </pre>
     *
     * @param olcDbLockDetect  the lock detection algorithm string to set
     */
    public void setOlcDbLockDetect( String olcDbLockDetect )
    {
        this.olcDbLockDetect = olcDbLockDetect;
    }


    // ── setOlcDbMode — Palpatine Sets the File Permission Mode ────────────────────
    // Palpatine sets the filesystem permission mode for BDB data files.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the file permission mode for BDB data files, typically as an octal string.
     *
     * <p>For example — Palpatine sets the file permissions:</p>
     * <pre>
     *   bdbConfig.setOlcDbMode( "0600" );
     * </pre>
     *
     * @param olcDbMode  the file permission mode string to set
     */
    public void setOlcDbMode( String olcDbMode )
    {
        this.olcDbMode = olcDbMode;
    }


    // ── setOlcDbNoSync — Palpatine Toggles the Sync-Skip Flag ─────────────────────
    // Palpatine decides whether BDB writes flush to disk immediately after each commit.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether disk sync is disabled after each write transaction for this BDB database.
     *
     * <p>For example — Palpatine disables sync for speed:</p>
     * <pre>
     *   bdbConfig.setOlcDbNoSync( true );
     * </pre>
     *
     * @param olcDbNoSync  true to disable sync; false to enforce it
     */
    public void setOlcDbNoSync( Boolean olcDbNoSync )
    {
        this.olcDbNoSync = olcDbNoSync;
    }


    // ── setOlcDbPageSize — Palpatine Sets the BDB B-tree Page Sizes ──────────────
    // Palpatine replaces the B-tree page size specifications for BDB's internal
    // database structures.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the list of BDB B-tree page size specification strings.
     *
     * <p>For example — Palpatine sets the page size specs:</p>
     * <pre>
     *   bdbConfig.setOlcDbPageSize( Arrays.asList( "dn2id 4096" ) );
     * </pre>
     *
     * @param olcDbPageSize  the new list of page size specification strings
     */
    public void setOlcDbPageSize( List<String> olcDbPageSize )
    {
        this.olcDbPageSize = copyListString( olcDbPageSize );
    }


    // ── setOlcDbSearchStack — Palpatine Sets the Search Stack Depth ───────────────
    // Palpatine sets the maximum depth of the search stack for LDAP queries.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the depth of the internal search stack for this BDB database.
     *
     * <p>For example — Palpatine sets the search stack depth:</p>
     * <pre>
     *   bdbConfig.setOlcDbSearchStack( 16 );
     * </pre>
     *
     * @param olcDbSearchStack  the search stack depth to set
     */
    public void setOlcDbSearchStack( Integer olcDbSearchStack )
    {
        this.olcDbSearchStack = olcDbSearchStack;
    }


    // ── setOlcDbShmKey — Palpatine Sets the Shared-Memory Key ─────────────────────
    // Palpatine sets the System V IPC shared memory key so multiple slapd processes
    // can share the BDB cache.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the System V shared memory key for this BDB database.
     *
     * <p>For example — Palpatine sets the shared memory key:</p>
     * <pre>
     *   bdbConfig.setOlcDbShmKey( 12345 );
     * </pre>
     *
     * @param olcDbShmKey  the shared memory key to set
     */
    public void setOlcDbShmKey( Integer olcDbShmKey )
    {
        this.olcDbShmKey = olcDbShmKey;
    }


    // ── getOlcDatabaseType — Palpatine Identifies the BDB Vault Type ──────────────
    // Palpatine identifies this vault as the "bdb" type so OpenLDAP loads the
    // correct BerkeleyDB backend plugin.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the database type identifier string "bdb", used as the type
     * discriminator in the cn=config DIT.
     *
     * <p>For example — Palpatine identifies the BDB vault type:</p>
     * <pre>
     *   bdbConfig.getOlcDatabaseType(); // "bdb"
     * </pre>
     *
     * @return  the lowercase database type string "bdb"
     */
    @Override
    public String getOlcDatabaseType()
    {
        return DatabaseTypeEnum.BDB.toString().toLowerCase();
    }
}
