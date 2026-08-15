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
package org.apache.directory.studio.connection.core.io;


import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.directory.api.ldap.model.exception.LdapContextNotEmptyException;
import org.apache.directory.api.ldap.model.exception.LdapEntryAlreadyExistsException;
import org.apache.directory.api.ldap.model.exception.LdapOperationException;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;


// ── CLASS: StudioLdapException — HAN SHOOTING FIRST AND EXPLAINING WHAT HAPPENED
// Han doesn't let a blaster discharge go unexplained — when he fires at Greedo
// (or the LDAP server fires back), he gives the crew a clear report: "result code
// 49 — invalid credentials" plus the server's own diagnostic message.
// This exception wraps a raw {@link Exception} from the LDAP library and formats
// its message into a human-readable string that includes the LDAP result code,
// its RFC name, and the server's own diagnostic text.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Checked exception that wraps a raw LDAP library exception and formats its
 * {@link #getMessage()} output to be human-readable.
 * When the LDAP operation throws an {@link LdapOperationException}, we extract
 * the {@link ResultCodeEnum} and format it as {@code "[LDAP result code N - name]"};
 * we then append the server's own diagnostic message if it is non-blank.
 * We also expose static helpers to check whether the wrapped cause is a specific
 * well-known error type (entry already exists, context not empty).
 * Think of this as Han's post-engagement debrief: the result code is the blaster
 * exchange, and the diagnostic message is the crew's explanation of why.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StudioLdapException extends Exception
{
    private static final long serialVersionUID = -1L;


    // ── CONSTRUCTOR — WRAP THE LIBRARY EXCEPTION ───────────────────────────────────
    // We wrap the raw library exception so callers get one consistent checked
    // exception type instead of dealing with a zoo of different LDAP library exceptions.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Wraps the given exception in a {@link StudioLdapException}.
     *
     * @param exception  The raw LDAP library exception (from Apache Directory API,
     *                   JNDI, etc.) to wrap.
     */
    public StudioLdapException( Exception exception )
    {
        super( exception );
    }


    // ── GET MESSAGE — FORMAT THE ERROR FOR HUMAN CONSUMPTION ──────────────────────
    // Han gives the crew a clear debrief: if the cause was a known LDAP operation
    // exception, we include the numeric result code and its RFC name.  Then we
    // append whatever the server itself said.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable error message including the LDAP result code (if available)
     * and the server's diagnostic message.
     * Format: {@code "[LDAP result code N - name] server diagnostic text"}.
     *
     * @return  The formatted error message string.
     */
    @Override
    public String getMessage()
    {
        String message = "";
        Throwable cause = getCause();
        if ( cause instanceof LdapOperationException )
        {
            LdapOperationException loe = ( LdapOperationException ) cause;
            ResultCodeEnum rc = loe.getResultCode();
            String template = " [LDAP result code %d - %s]"; //$NON-NLS-1$
            message += String.format( Locale.ROOT, template, rc.getResultCode(), rc.getMessage() );
        }
        if ( StringUtils.isNotBlank( cause.getMessage() ) )
        {
            message += " " + cause.getMessage(); //$NON-NLS-1$
        }
        return message;
    }


    // ── IS ENTRY ALREADY EXISTS EXCEPTION — CHECK FOR DUPLICATE ENTRY ─────────────
    // Han checks whether the server said "that entry already exists" — which means
    // we tried to add something that's already there.
    // We walk the exception chain looking for LdapEntryAlreadyExistsException.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the given exception (or any cause in its chain)
     * is an {@link LdapEntryAlreadyExistsException}.
     * Callers use this to detect "68 - entryAlreadyExists" without unwrapping
     * the cause manually.
     *
     * @param exception  The exception to inspect.
     * @return  {@code true} if an entry-already-exists error is in the cause chain.
     */
    public static boolean isEntryAlreadyExistsException( Exception exception )
    {
        return ExceptionUtils.indexOfThrowable( exception, LdapEntryAlreadyExistsException.class ) > -1;
    }


    // ── IS CONTEXT NOT EMPTY EXCEPTION — CHECK FOR NON-LEAF DELETION ATTEMPT ──────
    // Han checks whether the server complained that we tried to delete a container
    // entry that still has children — like trying to demolish a warehouse while
    // people are still inside.
    // We walk the exception chain looking for LdapContextNotEmptyException.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the given exception (or any cause in its chain)
     * is an {@link LdapContextNotEmptyException}.
     * This corresponds to LDAP result code 66 — notAllowedOnNonLeaf — thrown when
     * we try to delete an entry that still has child entries.
     *
     * @param exception  The exception to inspect.
     * @return  {@code true} if a context-not-empty error is in the cause chain.
     */
    public static boolean isContextNotEmptyException( Exception exception )
    {
        return ExceptionUtils.indexOfThrowable( exception, LdapContextNotEmptyException.class ) > -1;
    }

}
