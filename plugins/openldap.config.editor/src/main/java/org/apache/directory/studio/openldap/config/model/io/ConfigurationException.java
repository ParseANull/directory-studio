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
package org.apache.directory.studio.openldap.config.model.io;


import org.apache.directory.api.ldap.model.exception.LdapException;


// ── CLASS: ConfigurationException — Han Shoots First ─────────────────────────
// In the Mos Eisley cantina, Han doesn't wait for Greedo to pull the trigger —
// he fires first the moment he senses the threat, before things get out of hand.
// When the I/O layer detects something wrong during read or write (bad LDAP data,
// bad DN, reflection failure), it fires a ConfigurationException immediately
// rather than letting the problem silently corrupt the configuration model.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Exception thrown by the configuration I/O layer when reading from or writing
 * to the OpenLDAP cn=config DIT fails.
 * This wraps LdapException so callers higher up the stack can catch it as an
 * LDAP-level error without coupling to I/O internals.
 * Think of this as Han pulling the trigger first — we surface the problem
 * loudly and early rather than letting it propagate silently.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConfigurationException extends LdapException
{
    /** The serial version UUID */
    private static final long serialVersionUID = 1L;


    // ── Message-Only Constructor — Han Yells the Problem ─────────────────────────
    // Han shouts exactly what went wrong ("I've got a bad feeling about this!")
    // without pointing at an underlying exception — the cause is implicit.
    // Use this when we know what went wrong and can describe it in plain text.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a ConfigurationException with a descriptive message explaining what
     * went wrong during the configuration I/O operation.
     *
     * <p>For example — Han yells the problem:</p>
     * <pre>
     *   throw new ConfigurationException(
     *       "Cannot find the 'olcDatabase' attribute in entry cn=config" );
     * </pre>
     *
     * @param message  a human-readable description of what went wrong
     */
    public ConfigurationException( String message )
    {
        super( message );
    }


    // ── Cause-Only Constructor — Han Fires Because Something Already Went Wrong ──
    // Han fires his blaster because the guy across the table already made a move —
    // we're wrapping a pre-existing exception that caused the failure.
    // Use this when we caught another exception and want to propagate it up.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a ConfigurationException that wraps an underlying cause.
     * Useful when an LDAP API or reflection exception triggered the failure
     * and we want to preserve the original stack trace.
     *
     * <p>For example — Han fires because Greedo already moved:</p>
     * <pre>
     *   catch ( LdapInvalidDnException e ) {
     *       throw new ConfigurationException( e );
     *   }
     * </pre>
     *
     * @param cause  the underlying exception that triggered this failure
     */
    public ConfigurationException( Throwable cause )
    {
        super( cause );
    }


    // ── Message-and-Cause Constructor — Han Explains and Shows the Evidence ──────
    // Han explains what he saw AND hands over the blaster that proves he fired first.
    // Both a description and the underlying cause are captured.
    // Use this when you have context to add on top of the original exception.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a ConfigurationException with both a message describing what went wrong
     * and the underlying exception that caused it.
     *
     * <p>For example — Han explains and shows the evidence:</p>
     * <pre>
     *   catch ( Exception e ) {
     *       throw new ConfigurationException(
     *           "Cannot store value '" + val + "' into attribute " + attrId, e );
     *   }
     * </pre>
     *
     * @param message  a human-readable description of what went wrong
     * @param cause    the underlying exception that triggered this failure
     */
    public ConfigurationException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
