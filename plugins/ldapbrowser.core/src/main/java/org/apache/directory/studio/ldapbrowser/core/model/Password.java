/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor basic agreements.  See the NOTICE file
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

package org.apache.directory.studio.ldapbrowser.core.model;


import org.apache.directory.api.ldap.model.constants.LdapSecurityConstants;
import org.apache.directory.api.ldap.model.password.PasswordDetails;
import org.apache.directory.api.ldap.model.password.PasswordUtil;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldifparser.LdifUtils;


// ── CLASS: Password — HAN SHOOTING FIRST AT A BAD CREDENTIAL ─────────────────
// Han Solo doesn't give an enemy the chance to draw — the moment a credential
// looks wrong (null input, unknown hash) he fires back immediately with an
// exception.  When the credential is valid he extracts everything needed from
// it: the algorithm used, the hashed bytes, and the salt.  He can also check
// whether a plain-text guess matches the stored hash — without ever exposing
// the original password.
// This class wraps an LDAP {@code userPassword} attribute value: it can be a
// plain-text password or one of several hashed formats ({SHA}, {SSHA},
// {SHA-256}, etc.).  We parse out the hash method and salt, and provide a
// {@link #verify} method for checking guesses.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents a hashed or plain-text LDAP password value.
 * Parses the standard LDAP password format: {@code {ALGORITHM}base64encodedHash}
 * (e.g. {@code {SSHA}...}).  Provides accessors for the algorithm, hash bytes,
 * and salt, and a {@link #verify(String)} method that checks a plain-text
 * candidate against the stored hash without re-exposing it.
 *
 * <p>Supported hash algorithms:</p>
 * <ul>
 *   <li>SHA, SSHA</li>
 *   <li>SHA-256, SSHA-256</li>
 *   <li>SHA-384, SSHA-384</li>
 *   <li>SHA-512, SSHA-512</li>
 *   <li>MD5, SMD5</li>
 *   <li>PKCS5S2</li>
 *   <li>CRYPT</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Password
{
    /** The password, either plain text or in encrypted format. */
    private final byte[] password;

    /** The password details — algorithm, hash bytes, and salt. */
    private final PasswordDetails passwordDetails;


    // ── Han Checks The Incoming Credential Bytes ──────────────────────────────────
    // "Is there actually a credential here?  No?  Shoot first."
    // Null bytes are rejected immediately; otherwise we let the LDAP API
    // parse the format and extract the algorithm and salt.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@link Password} from a raw byte array containing either a
     * plain-text password or a hashed password in LDAP storage format.
     *
     * <p>For example — parsing a value from the directory:</p>
     * <pre>
     *   byte[] raw = attr.getBinaryValues()[0];
     *   Password pw = new Password(raw);
     * </pre>
     *
     * @param password the raw bytes; must not be {@code null}.
     * @throws IllegalArgumentException if {@code password} is {@code null}.
     */
    public Password( byte[] password )
    {
        if ( password == null )
        {
            throw new IllegalArgumentException( BrowserCoreMessages.model__empty_password );
        }
        else
        {
            this.password = password;
            this.passwordDetails = PasswordUtil.splitCredentials( password );
        }
    }


    // ── Han Checks The Incoming Credential String ─────────────────────────────────
    // Same fast-fail logic, but starting from a Java String.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@link Password} from a string containing either a plain-text
     * password or a hashed password in LDAP storage format.
     *
     * @param password the string form; must not be {@code null}.
     * @throws IllegalArgumentException if {@code password} is {@code null}.
     */
    public Password( String password )
    {
        if ( password == null )
        {
            throw new IllegalArgumentException( BrowserCoreMessages.model__empty_password );
        }
        else
        {
            this.password = Strings.getBytesUtf8( password );
            this.passwordDetails = PasswordUtil.splitCredentials( this.password );
        }
    }


    // ── Han Creates A New Hashed Credential On The Spot ───────────────────────────
    // "Give me your plain-text passphrase — I'll hash it with SSHA and store it."
    // We use the LDAP API to compute the storage-format hash so we never store
    // the plain-text anywhere.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@link Password} by hashing the given plain-text password using
     * the specified algorithm.
     *
     * <p>For example — creating an SSHA-hashed password:</p>
     * <pre>
     *   Password pw = new Password(LdapSecurityConstants.HASH_METHOD_SSHA, "secret");
     * </pre>
     *
     * @param hashMethod             the algorithm to use (e.g. {@code HASH_METHOD_SSHA}).
     * @param passwordAsPlaintext    the plain-text password to hash; must not be {@code null}.
     * @throws IllegalArgumentException if {@code passwordAsPlaintext} is {@code null}.
     */
    public Password( LdapSecurityConstants hashMethod, String passwordAsPlaintext )
    {
        if ( passwordAsPlaintext == null )
        {
            throw new IllegalArgumentException( BrowserCoreMessages.model__empty_password );
        }
        else
        {
            this.password = PasswordUtil.createStoragePassword( passwordAsPlaintext, hashMethod );
            this.passwordDetails = PasswordUtil.splitCredentials( this.password );
        }
    }


    // ── Han Checks Whether The Guess Matches Without Exposing The Secret ──────────
    // "Does your plain-text passphrase decode to the same hash?  Yes or no — I'm
    // not telling you the hash."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Verifies whether the given plain-text password matches this stored password.
     * Uses constant-time comparison to avoid timing attacks.
     *
     * <p>For example — checking a login attempt:</p>
     * <pre>
     *   if (!storedPw.verify(userTyped)) { throw new AuthException(); }
     * </pre>
     *
     * @param testPasswordAsPlaintext the plain-text candidate to check; {@code null} always returns {@code false}.
     * @return {@code true} if the candidate matches.
     */
    public boolean verify( String testPasswordAsPlaintext )
    {
        if ( testPasswordAsPlaintext == null )
        {
            return false;
        }

        return PasswordUtil.compareCredentials( Strings.getBytesUtf8( testPasswordAsPlaintext ), this.password );
    }


    // ── Han Identifies Which Hash Algorithm Was Used ──────────────────────────────
    // "It's SSHA — salted SHA-1.  Now we know how to verify it."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the hash algorithm used to store this password, or {@code null}
     * if the password is stored in plain text.
     *
     * @return the {@link LdapSecurityConstants} algorithm constant, or {@code null}.
     */
    public LdapSecurityConstants getHashMethod()
    {
        return passwordDetails.getAlgorithm();
    }


    // ── Han Retrieves The Raw Hash Bytes ──────────────────────────────────────────
    // "Here are the 20 bytes that are the actual SHA-1 digest."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw hashed password bytes (without the algorithm prefix or salt).
     *
     * @return the hash bytes; may be the full password bytes if no hash was detected.
     */
    public byte[] getHashedPassword()
    {
        return passwordDetails.getPassword();
    }


    // ── Han Gives The Hash As A Hex String For Display ────────────────────────────
    // "Here it is in hex — easier to read on screen."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the hashed password bytes encoded as a hexadecimal string.
     * Useful for display in the UI.
     *
     * @return the hex-encoded hash; empty string if no hash bytes.
     */
    public String getHashedPasswordAsHexString()
    {
        return LdifUtils.hexEncode( passwordDetails.getPassword() );
    }


    // ── Han Retrieves The Salt Bytes ──────────────────────────────────────────────
    // "And here's the random salt that was mixed in — also 4 bytes."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the salt bytes appended to the hash (for salted algorithms like SSHA).
     *
     * @return the salt bytes, or {@code null} if the algorithm is unsalted.
     */
    public byte[] getSalt()
    {
        return passwordDetails.getSalt();
    }


    // ── Han Gives The Salt As A Hex String ───────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the salt bytes encoded as a hexadecimal string.
     *
     * @return the hex-encoded salt; empty string if no salt.
     */
    public String getSaltAsHexString()
    {
        return LdifUtils.hexEncode( passwordDetails.getSalt() );
    }


    // ── Han Encodes The Full Password Back To Bytes ───────────────────────────────
    // "{SSHA}base64encoded..." back to its UTF-8 byte representation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full password (as stored, including any algorithm prefix) as
     * a UTF-8 byte array.
     *
     * @return the password bytes; never {@code null}.
     */
    public byte[] toBytes()
    {
        return LdifUtils.utf8encode( toString() );
    }


    // ── Han Prints The Full Password Label ────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full password string as stored in the directory —
     * e.g. {@code {SSHA}base64...} or the plain-text value.
     *
     * @return the password string; never {@code null}.
     */
    public String toString()
    {
        return Strings.utf8ToString( password );
    }

}
