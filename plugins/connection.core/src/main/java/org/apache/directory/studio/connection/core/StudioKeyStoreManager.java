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
package org.apache.directory.studio.connection.core;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;


// ── CLASS: StudioKeyStoreManager — THE FALCON'S SHIELD CERTIFICATE REGISTRY ───
// The Falcon's shields only trust certain transponder codes — unknown ships
// without a valid certificate get no passage.  The trust registry is maintained
// in two forms: a file-based store (persists between sessions) and an in-memory
// store (lasts only for the current session).
// This class wraps a Java KeyStore to manage TLS server certificates that the
// user has chosen to trust — either permanently (file-backed) or just for
// this session (memory-backed).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Manages a Java {@link KeyStore} that holds trusted TLS server certificates.
 * We support two backing modes controlled by the {@link Type} enum:
 * <ul>
 *   <li><b>File</b> — certificates are persisted to a keystore file on disk
 *       (used for permanently trusted certificates)</li>
 *   <li><b>Memory</b> — certificates are held only in memory for the current
 *       session (used for session-trusted certificates)</li>
 * </ul>
 * We use the SHA-1 hex digest of the certificate's DER encoding as the alias
 * so each certificate gets a unique, stable identifier.
 * Think of this class as the Falcon's transponder registry: permanently trusted
 * ships are written to the ship's database; session-trusted ships are noted
 * on a scratch pad that gets wiped when we land.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StudioKeyStoreManager
{
    // ── TYPE — FILE OR MEMORY BACKING ─────────────────────────────────────────────
    // We keep two modes: file-backed (persistent, survives restart) and
    // memory-backed (temporary, discarded when the session ends).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Backing storage type for the keystore.
     * {@link #File} persists to disk; {@link #Memory} lives only in the JVM heap.
     */
    public enum Type
    {
        File, Memory
    }

    /** Whether this manager is file-backed or memory-backed. */
    private Type type;

    /** For file-backed mode: the filename within the plugin state directory. */
    private String filename;

    /** For file-backed mode: the password used to encrypt the keystore file. */
    private String password;

    /** For memory-backed mode: the in-memory KeyStore. */
    private KeyStore memoryKeyStore;


    // ── FACTORY: CREATE FILE KEYSTORE MANAGER ─────────────────────────────────────
    // We build a file-backed manager that stores the trust registry persistently —
    // like writing trusted transponder codes into the Falcon's permanent database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a file-backed {@link StudioKeyStoreManager}.
     * Trusted certificates are persisted to a keystore file in the plugin state
     * directory under the given filename, encrypted with the given password.
     *
     * @param filename  The keystore file name (relative to plugin state location).
     * @param password  The keystore encryption password.
     * @return  A configured {@link StudioKeyStoreManager} of type {@link Type#File}.
     */
    public static StudioKeyStoreManager createFileKeyStoreManager( String filename, String password )
    {
        StudioKeyStoreManager manager = new StudioKeyStoreManager( Type.File, filename, password );
        manager.filename = filename;
        manager.password = password;
        return manager;
    }


    // ── FACTORY: CREATE MEMORY KEYSTORE MANAGER ────────────────────────────────────
    // We build an in-memory manager — a scratch pad of session-trusted certs that
    // vanishes when the session ends.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an in-memory {@link StudioKeyStoreManager}.
     * Trusted certificates are held in memory only and lost when the session ends.
     *
     * @return  A configured {@link StudioKeyStoreManager} of type {@link Type#Memory}.
     */
    public static StudioKeyStoreManager createMemoryKeyStoreManager()
    {
        StudioKeyStoreManager manager = new StudioKeyStoreManager( Type.Memory, null, null );
        return manager;
    }


    // ── PRIVATE CONSTRUCTOR — TYPE, FILENAME, PASSWORD ────────────────────────────
    // Internal initialization only — callers must use the factory methods above.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor. Use {@link #createFileKeyStoreManager(String, String)} or
     * {@link #createMemoryKeyStoreManager()} instead.
     *
     * @param type      The backing type.
     * @param filename  The keystore filename for file-backed mode; {@code null} otherwise.
     * @param password  The keystore password for file-backed mode; {@code null} otherwise.
     */
    private StudioKeyStoreManager( Type type, String filename, String password )
    {
        this.type = type;
        this.filename = filename;
        this.password = password;
    }


    // ── GET KEYSTORE — RETURN THE UNDERLYING KEYSTORE ─────────────────────────────
    // We return the appropriate KeyStore depending on our backing type.
    // The file store is loaded fresh each time so it always reflects the latest
    // on-disk state.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the underlying {@link KeyStore}.
     * For file-backed mode, we load fresh from disk each time.
     * For memory-backed mode, we lazily initialize an empty in-memory store.
     * Thread-safe (synchronized).
     *
     * @return  The {@link KeyStore}.
     * @throws CertificateException  If the keystore cannot be read or initialized.
     */
    public synchronized KeyStore getKeyStore() throws CertificateException
    {
        if ( type == Type.File )
        {
            return getFileKeyStore();
        }
        else
        {
            return getMemoryKeyStore();
        }
    }


    // ── GET MEMORY KEYSTORE — LAZY-INITIALIZE THE IN-MEMORY STORE ─────────────────
    // The scratch pad is created empty the first time we need it and reused
    // for the rest of the session.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns (or lazily initializes) the in-memory {@link KeyStore}.
     *
     * @return  The in-memory {@link KeyStore}.
     * @throws CertificateException  If the in-memory store cannot be initialized.
     */
    private KeyStore getMemoryKeyStore() throws CertificateException
    {
        if ( memoryKeyStore == null )
        {
            try
            {
                memoryKeyStore = KeyStore.getInstance( KeyStore.getDefaultType() );
                memoryKeyStore.load( null, null );
            }
            catch ( Exception e )
            {
                throw new CertificateException( Messages.StudioKeyStoreManager_CantReadTrustStore, e );
            }
        }
        return memoryKeyStore;
    }


    // ── GET FILE KEYSTORE — LOAD FRESH FROM DISK ──────────────────────────────────
    // We read the keystore file every time so we don't risk using a stale snapshot.
    // If the file doesn't exist yet, we return an empty store.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Loads and returns the file-backed {@link KeyStore} from disk.
     * If the file doesn't exist yet, returns an empty keystore.
     *
     * @return  The loaded {@link KeyStore}.
     * @throws CertificateException  If the file exists but cannot be read or decrypted.
     */
    private KeyStore getFileKeyStore() throws CertificateException
    {
        try
        {
            KeyStore fileKeyStore = KeyStore.getInstance( KeyStore.getDefaultType() );
            File file = ConnectionCorePlugin.getDefault().getStateLocation().append( filename ).toFile();
            if ( file.exists() && file.isFile() && file.canRead() )
            {
                try ( FileInputStream in = new FileInputStream( file ) )
                {
                    fileKeyStore.load( in, password.toCharArray() );
                }
            }
            else
            {
                fileKeyStore.load( null, null );
            }

            return fileKeyStore;
        }
        catch ( Exception e )
        {
            throw new CertificateException( Messages.StudioKeyStoreManager_CantReadTrustStore, e );
        }
    }


    // ── ADD CERTIFICATE — REGISTER A TRUSTED CERT ─────────────────────────────────
    // A new ship presents its credentials and we add its certificate to the
    // appropriate trust registry (file or memory).
    // Thread-safe (synchronized).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a trusted {@link X509Certificate} to the keystore.
     * Dispatches to the file or memory keystore based on the manager type.
     * Thread-safe.
     *
     * @param certificate  The certificate to trust.
     * @throws CertificateException  If the certificate cannot be added.
     */
    public synchronized void addCertificate( X509Certificate certificate ) throws CertificateException
    {
        if ( type == Type.File )
        {
            addToFileKeyStore( certificate );
        }
        else
        {
            addToMemoryKeyStore( certificate );
        }
    }


    // ── ADD TO MEMORY KEYSTORE — WRITE TO SCRATCH PAD ─────────────────────────────
    // We add the certificate to the in-memory store only.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds the certificate to the in-memory keystore.
     *
     * @param certificate  The certificate to add.
     * @throws CertificateException  If the addition fails.
     */
    private void addToMemoryKeyStore( X509Certificate certificate ) throws CertificateException
    {
        try
        {
            KeyStore memoryKeyStore = getMemoryKeyStore();
            addToKeyStore( certificate, memoryKeyStore );
        }
        catch ( Exception e )
        {
            throw new CertificateException( Messages.StudioKeyStoreManager_CantAddCertificateToTrustStore, e );
        }
    }


    // ── ADD TO FILE KEYSTORE — WRITE TO PERSISTENT REGISTRY ───────────────────────
    // We add the certificate to the file-backed store, then flush it to disk.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds the certificate to the file-backed keystore, then persists to disk.
     *
     * @param certificate  The certificate to add.
     * @throws CertificateException  If the addition or file save fails.
     */
    private void addToFileKeyStore( X509Certificate certificate ) throws CertificateException
    {
        try
        {
            KeyStore fileKeyStore = getFileKeyStore();
            addToKeyStore( certificate, fileKeyStore );
            File file = ConnectionCorePlugin.getDefault().getStateLocation().append( filename ).toFile();
            try ( FileOutputStream out = new FileOutputStream( file ) )
            {
                fileKeyStore.store( out, password.toCharArray() );
            }
        }
        catch ( Exception e )
        {
            throw new CertificateException( Messages.StudioKeyStoreManager_CantAddCertificateToTrustStore, e );
        }
    }


    // ── ADD TO KEYSTORE — SHARED HELPER: USE SHA-1 AS ALIAS ──────────────────────
    // We compute the SHA-1 hex hash of the certificate's DER encoding and use it
    // as the keystore alias — a stable, unique identifier for any certificate.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Internal helper: adds the certificate to the given {@link KeyStore} using its
     * SHA-1 hex digest as the alias.
     *
     * @param certificate  The certificate to add.
     * @param keyStore     The keystore to add it to.
     * @throws Exception   If encoding or adding the entry fails.
     */
    private void addToKeyStore( X509Certificate certificate, KeyStore keyStore ) throws Exception
    {
        // The alias is not relevant, it just needs to be an unique identifier.
        // The SHA-1 hash of the certificate should be unique.
        byte[] encoded = certificate.getEncoded();
        String shaHex = DigestUtils.shaHex( encoded );
        keyStore.setCertificateEntry( shaHex, certificate );
    }


    // ── GET CERTIFICATES — LIST ALL TRUSTED CERTS ─────────────────────────────────
    // We read every entry in the keystore and return the ones that are X.509
    // certificates — giving the caller the full list of trusted servers.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all trusted {@link X509Certificate}s in the keystore.
     *
     * @return  An array of trusted certificates (empty if the keystore is empty).
     * @throws CertificateException  If the keystore cannot be read.
     */
    public X509Certificate[] getCertificates() throws CertificateException
    {
        try
        {
            List<X509Certificate> certificateList = new ArrayList<X509Certificate>();
            KeyStore keyStore = getKeyStore();
            Enumeration<String> aliases = keyStore.aliases();
            while ( aliases.hasMoreElements() )
            {
                String alias = aliases.nextElement();
                Certificate certificate = keyStore.getCertificate( alias );
                if ( certificate instanceof X509Certificate )
                {
                    certificateList.add( ( X509Certificate ) certificate );
                }
            }
            return certificateList.toArray( new X509Certificate[0] );
        }
        catch ( KeyStoreException e )
        {
            throw new CertificateException( Messages.StudioKeyStoreManager_CantReadTrustStore, e );
        }
    }


    // ── REMOVE CERTIFICATE — REVOKE TRUST ─────────────────────────────────────────
    // We remove a certificate from the appropriate trust registry.
    // Thread-safe (synchronized).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a previously trusted {@link X509Certificate} from the keystore.
     * Dispatches to the file or memory keystore based on the manager type.
     * Thread-safe.
     *
     * @param certificate  The certificate to remove.
     * @throws CertificateException  If the certificate cannot be removed.
     */
    public synchronized void removeCertificate( X509Certificate certificate ) throws CertificateException
    {
        if ( type == Type.File )
        {
            removeFromFileKeyStore( certificate );
        }
        else
        {
            removeFromMemoryKeyStore( certificate );
        }
    }


    // ── REMOVE FROM MEMORY KEYSTORE — CROSS OFF THE SCRATCH PAD ──────────────────
    // We revoke trust from the in-memory store only.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the certificate from the in-memory keystore.
     *
     * @param certificate  The certificate to remove.
     * @throws CertificateException  If the removal fails.
     */
    private void removeFromMemoryKeyStore( X509Certificate certificate ) throws CertificateException
    {
        try
        {
            KeyStore memoryKeyStore = getMemoryKeyStore();
            removeFromKeyStore( certificate, memoryKeyStore );
        }
        catch ( Exception e )
        {
            throw new CertificateException( Messages.StudioKeyStoreManager_CantRemoveCertificateFromTrustStore, e );
        }
    }


    // ── REMOVE FROM FILE KEYSTORE — CROSS OFF THE PERMANENT REGISTRY ──────────────
    // We revoke trust from the file-backed store and flush to disk.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the certificate from the file-backed keystore, then persists to disk.
     *
     * @param certificate  The certificate to remove.
     * @throws CertificateException  If the removal or file save fails.
     */
    private void removeFromFileKeyStore( X509Certificate certificate ) throws CertificateException
    {
        try
        {
            KeyStore fileKeyStore = getFileKeyStore();
            removeFromKeyStore( certificate, fileKeyStore );
            File file = ConnectionCorePlugin.getDefault().getStateLocation().append( filename ).toFile();
            try ( FileOutputStream out = new FileOutputStream( file ) )
            {
                fileKeyStore.store( out, password.toCharArray() );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new CertificateException( Messages.StudioKeyStoreManager_CantRemoveCertificateFromTrustStore, e );
        }
    }


    // ── REMOVE FROM KEYSTORE — SHARED HELPER: FIND AND DELETE ALIAS ──────────────
    // We look up the certificate's alias and delete the entry.
    // If the cert isn't in the store, we silently do nothing.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Internal helper: removes the certificate from the given {@link KeyStore} by alias.
     * Silently does nothing if the certificate is not present.
     *
     * @param certificate  The certificate to remove.
     * @param keyStore     The keystore to remove it from.
     * @throws Exception   If the lookup or deletion fails.
     */
    private void removeFromKeyStore( X509Certificate certificate, KeyStore keyStore ) throws Exception
    {
        String alias = keyStore.getCertificateAlias( certificate );
        if ( alias != null )
        {
            keyStore.deleteEntry( alias );
        }
    }
}
