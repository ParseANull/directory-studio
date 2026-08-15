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
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.directory.api.util.FileUtils;


// ── CLASS: PasswordsKeyStoreManager — THE FALCON'S SECURE VAULT ───────────────
// Han doesn't keep his access codes written on a napkin — he stores them in a
// secure vault aboard the Falcon, locked with a master combination.
// To read any code, you need the master combination first; then the vault opens
// and you can retrieve any individual connection's stored password.
// This class is that vault: a PKCS12 KeyStore on disk, locked by a master
// password, holding per-connection passwords encoded as PBE SecretKey entries.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Manages a PKCS12 keystore on disk for persisting per-connection bind passwords.
 * Rather than storing passwords in plain text in connections.xml, we encrypt them
 * in a password-protected keystore file ({@code passwords.jks} by default).
 * The user supplies a master password at startup to unlock the vault; after that,
 * individual connection passwords can be read and written freely.
 * Think of this class as the Falcon's secure vault: one master combination unlocks
 * everything, and inside each connection gets its own labeled compartment.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PasswordsKeyStoreManager
{
    /** Default filename for the passwords keystore on disk. */
    private static final String KEYSTORE_DEFAULT_FILENAME = "passwords.jks";

    /** The filename this manager uses (may be overridden by the constructor). */
    private String filename = KEYSTORE_DEFAULT_FILENAME;

    /** The master password that unlocks the keystore. Null when not loaded. */
    private String masterPassword;

    /** The in-memory KeyStore instance. Null until {@link #load(String)} is called. */
    private KeyStore keystore;


    // ── CONSTRUCTOR (DEFAULT) — DEFAULT VAULT CONFIGURATION ────────────────────────
    // We open the vault manager with its default filename.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@link PasswordsKeyStoreManager} using the default keystore filename
     * ({@code passwords.jks}).
     */
    public PasswordsKeyStoreManager()
    {
    }


    // ── CONSTRUCTOR (FILENAME) — CUSTOM VAULT FILE LOCATION ────────────────────────
    // We open the vault manager pointed at a custom filename — useful for tests
    // or for storing passwords in a different file.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@link PasswordsKeyStoreManager} using a custom keystore filename.
     * The file lives in the plugin's state location directory.
     *
     * @param filename  The filename for the keystore file.
     */
    public PasswordsKeyStoreManager( String filename )
    {
        this.filename = filename;
    }


    // ── IS LOADED — CHECK IF THE VAULT IS UNLOCKED ────────────────────────────────
    // We check whether the vault has been opened (keystore is non-null).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the keystore has been successfully loaded into memory.
     * Call this before attempting to read or write passwords.
     *
     * @return  {@code true} if the keystore is loaded; {@code false} otherwise.
     */
    public boolean isLoaded()
    {
        return keystore != null;
    }


    // ── LOAD — UNLOCK THE VAULT WITH THE MASTER PASSWORD ──────────────────────────
    // Han punches in the master combination (master password).  The vault either
    // opens (loading the keystore from disk) or stays locked (wrong password or
    // corrupt file — exception thrown).
    // If the keystore file doesn't exist yet, we initialize an empty one.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Loads the keystore from disk, unlocking it with the given master password.
     * If the keystore file doesn't exist yet, we initialize an empty in-memory store.
     * On any error, both {@code keystore} and {@code masterPassword} are reset to null
     * so we stay in a consistent "not loaded" state.
     *
     * @param masterPassword  The master password for the keystore.
     * @throws KeyStoreException  If the keystore file exists but cannot be read or
     *                            the master password is wrong.
     */
    public void load( String masterPassword ) throws KeyStoreException
    {
        this.masterPassword = masterPassword;

        try
        {
            keystore = KeyStore.getInstance( "PKCS12" ); //$NON-NLS-1$

            // Getting the keystore file
            File keystoreFile = getKeyStoreFile();

            // Checking if the keystore file is available on disk
            if ( keystoreFile.exists() && keystoreFile.isFile() && keystoreFile.canRead() )
            {
                try ( FileInputStream fis = new FileInputStream( keystoreFile ) )
                {
                    keystore.load( fis, masterPassword.toCharArray() );
                }
            }
            else
            {
                keystore.load( null, null );
            }
        }
        // Catch for the following exceptions that may be raised while
        // handling the keystore:
        // - java.security.KeyStoreException
        // - java.security.NoSuchAlgorithmException
        // - java.security.cert.CertificateException
        catch ( GeneralSecurityException e )
        {
            this.masterPassword = null;
            this.keystore = null;

            throw new KeyStoreException( e );
        }
        // Catch for the following exceptions that may be raised while
        // handling the file:
        // - java.io.IOException
        // - java.io.FileNotFoundException
        catch ( IOException e )
        {
            this.masterPassword = null;
            this.keystore = null;

            throw new KeyStoreException( e );
        }
    }


    // ── SAVE — FLUSH THE VAULT CONTENTS TO DISK ────────────────────────────────────
    // Han re-seals the vault and writes the updated contents to the file system.
    // We only save if the vault is loaded and we have a master password to re-lock it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Persists the in-memory keystore to disk.
     * We serialize the keystore to the configured file, re-encrypting with the
     * master password.  Does nothing if the keystore isn't loaded.
     *
     * @throws KeyStoreException  If writing the keystore file fails.
     */
    public void save() throws KeyStoreException
    {
        if ( isLoaded() && ( masterPassword != null ) )
        {
            try ( FileOutputStream fos = new FileOutputStream( getKeyStoreFile() ) )
            {
                keystore.store( fos, masterPassword.toCharArray() );
            }
            // Catch for the following exceptions that may be raised while
            // handling the keystore:
            // - java.security.KeyStoreException
            // - java.security.NoSuchAlgorithmException
            // - java.security.cert.CertificateException
            catch ( GeneralSecurityException e )
            {
                throw new KeyStoreException( e );
            }
            // Catch for the following exceptions that may be raised while
            // handling the file:
            // - java.io.IOException
            // - java.io.FileNotFoundException
            catch ( IOException e )
            {
                throw new KeyStoreException( e );
            }
        }
    }


    // ── CHECK MASTER PASSWORD — VERIFY THE COMBINATION WITHOUT UNLOCKING ──────────
    // Han tries the combination without fully opening the vault, just to check
    // whether it's correct before committing to the full load operation.
    // If the vault is already loaded, we compare in-memory; otherwise we try to load.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Verifies whether the given master password matches the current one.
     * If the keystore is already loaded, we compare directly.
     * If not yet loaded, we attempt to load it; success means the password is correct.
     *
     * @param masterPassword  The password to verify.
     * @return  {@code true} if the password is correct; {@code false} otherwise.
     * @throws KeyStoreException  If loading fails for a reason other than a bad password.
     */
    public boolean checkMasterPassword( String masterPassword ) throws KeyStoreException
    {
        // If the keystore is already loaded, we compare the master password directly
        if ( isLoaded() )
        {
            return ( ( this.masterPassword != null ) && ( this.masterPassword.equals( masterPassword ) ) );
        }
        // The keystore is not loaded yet
        else
        {
            try
            {
                // Loading the keystore
                load( masterPassword );

                // Returning the check value
                return isLoaded();
            }
            catch ( KeyStoreException e )
            {
                throw e;
            }
        }
    }


    // ── SET MASTER PASSWORD — RE-KEY THE VAULT ────────────────────────────────────
    // Han changes the vault's master combination.  He first reads all stored codes
    // into a temporary map, re-enters them under the new combination, then discards
    // the old one.  All previously stored passwords survive the re-keying operation.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Changes the master password, migrating all existing stored passwords to the new one.
     * We read every connection's password under the old master password, then
     * re-store each one under the new master password.
     * The keystore must already be loaded before calling this.
     *
     * @param masterPassword  The new master password.
     */
    public void setMasterPassword( String masterPassword )
    {
        // Creating a map to store previously stored passwords
        Map<String, String> passwordsMap = new HashMap<String, String>();

        if ( isLoaded() )
        {
            // Getting the connection IDs
            String[] connectionIds = getConnectionIds();

            // Storing the password of each connection in the map
            for ( String connectionId : connectionIds )
            {
                // Getting the connection password
                String connectionPassword = getConnectionPassword( connectionId );

                // Checking if we got a password
                if ( connectionPassword != null )
                {
                    // Storing the password of the connection in the map
                    passwordsMap.put( connectionId, connectionPassword );
                }

                // Removing the password from the keystore
                storeConnectionPassword( connectionId, null, false );
            }
        }

        // Assigning the new master password
        this.masterPassword = masterPassword;

        // Storing the previous passwords back in the keystore
        if ( passwordsMap.size() > 0 )
        {
            Set<String> connectionIds = passwordsMap.keySet();

            // Storing the password of each connection in the keystore
            if ( connectionIds != null )
            {
                for ( String connectionId : connectionIds )
                {
                    String connectionPassword = passwordsMap.get( connectionId );

                    if ( connectionPassword != null )
                    {
                        // Storing the password of the connection in the keystore
                        storeConnectionPassword( connectionId, connectionPassword, false );
                    }
                }
            }
        }
    }


    // ── GET KEYSTORE FILE — WHERE ON DISK THE VAULT LIVES ─────────────────────────
    // We compute the full path to the keystore file in the plugin's state directory.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link File} object pointing to the keystore on disk.
     * The file lives in the plugin's Eclipse state location directory.
     *
     * @return  The keystore {@link File}.
     */
    public File getKeyStoreFile()
    {
        return ConnectionCorePlugin.getDefault().getStateLocation().append( filename ).toFile();
    }


    // ── DELETE KEYSTORE FILE — DESTROY THE VAULT ──────────────────────────────────
    // Han decides to wipe the vault entirely — removes the file from disk.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Deletes the keystore file from disk.
     * We check that the file exists and is writable before attempting to delete it.
     */
    public void deleteKeystoreFile()
    {
        // Getting the keystore file
        File keystoreFile = getKeyStoreFile();

        // Checking if the keystore file is available on disk
        if ( keystoreFile.exists() && keystoreFile.isFile() && keystoreFile.canRead() && keystoreFile.canWrite() )
        {
            keystoreFile.delete();
        }
    }


    // ── GET CONNECTION IDS — LIST ALL COMPARTMENT LABELS IN THE VAULT ─────────────
    // Han opens the vault and reads the labels on every compartment.
    // We return the aliases (connection IDs) stored in the keystore.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the connection IDs (keystore aliases) for all stored password entries.
     * Returns an empty array if the keystore isn't loaded or is empty.
     *
     * @return  Array of connection ID strings.
     */
    public String[] getConnectionIds()
    {
        if ( keystore != null )
        {
            try
            {
                return Collections.list( keystore.aliases() ).toArray( new String[0] );
            }
            catch ( KeyStoreException e )
            {
                // Silent
            }
        }

        return new String[0];
    }


    // ── STORE CONNECTION PASSWORD (CONNECTION) — SAVE A CODE BY CONNECTION ─────────
    // Han labels a compartment by connection object and stores the code inside.
    // We delegate to the ID-based overload.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the given password for the specified connection, then saves the keystore.
     * Convenience overload that extracts the connection ID from the {@link Connection} object.
     *
     * @param connection  The connection whose password to store.
     * @param password    The password string to store, or {@code null} to remove.
     */
    public void storeConnectionPassword( Connection connection, String password )
    {
        if ( connection != null )
        {
            storeConnectionPassword( connection.getId(), password );
        }
    }


    // ── STORE CONNECTION PASSWORD (CONNECTION + SAVE FLAG) ─────────────────────────
    // Same as above but with an explicit save flag.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the given password for the specified connection.
     * Convenience overload extracting the ID from the {@link Connection} object.
     *
     * @param connection    The connection whose password to store.
     * @param password      The password string, or {@code null} to remove.
     * @param saveKeystore  If {@code true}, flush the keystore to disk after storing.
     */
    public void storeConnectionPassword( Connection connection, String password, boolean saveKeystore )
    {
        if ( connection != null )
        {
            storeConnectionPassword( connection.getId(), password, true );
        }
    }


    // ── STORE CONNECTION PASSWORD (ID) — SAVE BY ID, ALWAYS SAVE AFTER ────────────
    // Store by connection ID and always flush to disk.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the given password for the given connection ID, flushing the keystore to disk.
     *
     * @param connectionId  The connection ID (used as the keystore alias).
     * @param password      The password string, or {@code null} to remove the entry.
     */
    public void storeConnectionPassword( String connectionId, String password )
    {
        storeConnectionPassword( connectionId, password, true );
    }


    // ── STORE CONNECTION PASSWORD (ID + SAVE FLAG) — CORE STORAGE LOGIC ───────────
    // This is the real implementation.  We encode the password as a PBE SecretKey
    // and store it under the connection's ID alias, protecting it with the master
    // password.  Pass null for password to remove the entry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores or removes a connection's password in the keystore.
     * Passing a non-null {@code password} creates/updates the entry by encoding it
     * as a PBE {@link SecretKey}.  Passing {@code null} removes the entry.
     * If {@code saveKeystore} is {@code true}, we flush the keystore to disk after.
     * Silently ignores failures (keystore not loaded, crypto errors, I/O errors).
     *
     * @param connectionId   The connection ID used as the keystore alias.
     * @param password       The password to store, or {@code null} to delete the entry.
     * @param saveKeystore   If {@code true}, save the keystore to disk after the change.
     */
    public void storeConnectionPassword( String connectionId, String password, boolean saveKeystore )
    {
        if ( isLoaded() && ( connectionId != null ) )
        {
            try
            {
                // Checking if the password is null
                if ( password == null )
                {
                    // We need to remove the corresponding entry in the keystore
                    if ( keystore.containsAlias( connectionId ) )
                    {
                        keystore.deleteEntry( connectionId );
                    }
                }
                else
                {
                    // Generating a secret key from the password
                    SecretKeyFactory factory = SecretKeyFactory.getInstance( "PBE" );
                    SecretKey generatedSecret = factory.generateSecret( new PBEKeySpec( password.toCharArray() ) );

                    // Setting the entry in the keystore
                    keystore.setEntry( connectionId, new KeyStore.SecretKeyEntry( generatedSecret ),
                        new KeyStore.PasswordProtection( masterPassword.toCharArray() ) );
                }

                // Saving
                if ( saveKeystore )
                {
                    save();
                }
            }
            catch ( Exception e )
            {
                // Silent
            }
        }
    }


    // ── GET CONNECTION PASSWORD (CONNECTION) — READ A CODE BY CONNECTION ───────────
    // Han opens the compartment labeled with the connection object and reads the code.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the stored password for the given connection, or {@code null}.
     * Convenience overload that extracts the ID from the {@link Connection} object.
     *
     * @param connection  The connection whose password to retrieve.
     * @return  The stored password string, or {@code null} if not found.
     */
    public String getConnectionPassword( Connection connection )
    {
        if ( connection != null )
        {
            return getConnectionPassword( connection.getId() );
        }

        return null;
    }


    // ── GET CONNECTION PASSWORD (ID) — READ A CODE BY ID ──────────────────────────
    // Han opens the compartment labeled with the connection ID, decodes the
    // PBE SecretKey back to a plain String, and returns it.
    // Returns null if the keystore isn't loaded, the ID doesn't exist, or any
    // crypto operation fails.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the stored password for the given connection ID, or {@code null}.
     * We retrieve the {@link SecretKeyEntry}, recover the {@link PBEKeySpec},
     * and convert the char[] back to a String.
     * Returns {@code null} if the keystore isn't loaded, the alias doesn't exist,
     * or any crypto operation throws.
     *
     * @param connectionId  The connection ID used as the keystore alias.
     * @return  The stored password string, or {@code null}.
     */
    public String getConnectionPassword( String connectionId )
    {
        if ( isLoaded() && ( connectionId != null ) )
        {
            try
            {
                SecretKeyFactory factory = SecretKeyFactory.getInstance( "PBE" );
                SecretKeyEntry ske = ( SecretKeyEntry ) keystore.getEntry( connectionId,
                    new KeyStore.PasswordProtection( masterPassword.toCharArray() ) );

                if ( ske != null )
                {
                    PBEKeySpec keySpec = ( PBEKeySpec ) factory.getKeySpec( ske.getSecretKey(), PBEKeySpec.class );

                    if ( keySpec != null )
                    {
                        char[] password = keySpec.getPassword();

                        if ( password != null )
                        {
                            return new String( password );
                        }
                    }
                }
            }
            catch ( Exception e )
            {
                return null;
            }
        }

        return null;
    }


    // ── RESET — WIPE THE VAULT COMPLETELY ─────────────────────────────────────────
    // Han decides the vault has been compromised — he wipes it in memory and
    // deletes the file from disk.  Everything starts fresh from zero.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Resets the keystore to a blank slate.
     * We clear both the in-memory keystore and the master password, then delete
     * the keystore file from disk if it exists.
     * After this call, {@link #isLoaded()} returns {@code false}.
     */
    public void reset()
    {
        // Reseting the fields
        this.keystore = null;
        this.masterPassword = null;

        // Getting the keystore file
        File keystoreFile = getKeyStoreFile();

        // If the keystore file exists, we need to remove it
        if ( keystoreFile.exists() )
        {
            // Deleting the file
            FileUtils.deleteQuietly( keystoreFile );
        }
    }


    // ── UNLOAD — LOCK THE VAULT WITHOUT WIPING IT ────────────────────────────────
    // Han locks the vault and steps away — the file stays on disk but the
    // in-memory copy is cleared.  Requires another {@code load()} call to reopen.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears the in-memory keystore and master password without deleting the file.
     * The keystore can be reloaded via {@link #load(String)} or {@link #reload(String)}.
     */
    public void unload()
    {
        // Reseting the fields
        this.keystore = null;
        this.masterPassword = null;
    }


    // ── RELOAD — LOCK AND REOPEN WITH A (POSSIBLY NEW) PASSWORD ──────────────────
    // Han steps away briefly and comes back to reopen the vault — maybe the master
    // password just changed and he needs a fresh load with the new combination.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Unloads then reloads the keystore using the given master password.
     * Useful after changing the master password or when the on-disk file changes.
     *
     * @param masterPassword  The master password to use for the reload.
     * @throws KeyStoreException  If the load fails.
     */
    public void reload( String masterPassword ) throws KeyStoreException
    {
        unload();

        load( masterPassword );
    }


    // ── GET MASTER PASSWORD — READ THE CURRENT COMBINATION ────────────────────────
    // Han checks what the current master combination is — normally only needed
    // internally, but exposed for the preferences UI.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current master password, or {@code null} if the keystore is not loaded.
     *
     * @return  The master password string.
     */
    public String getMasterPassword()
    {
        return masterPassword;
    }
}
