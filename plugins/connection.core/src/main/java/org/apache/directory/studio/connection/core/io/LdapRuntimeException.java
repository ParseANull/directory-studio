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


import org.apache.directory.api.ldap.model.exception.LdapException;


// ── CLASS: LdapRuntimeException — HAN SHOOTS AND DOESN'T STOP TO EXPLAIN ──────
// Sometimes Han doesn't have time for a debrief — he fires and keeps running.
// When LDAP code runs in a context that can't declare checked exceptions (like
// a Comparator or a stream lambda), we need to re-throw the checked LdapException
// as an unchecked RuntimeException so it can propagate without catching.
// This class is that unchecked wrapper: it carries the original LdapException
// as its cause so callers can unwrap it when they finally catch something.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Unchecked (runtime) exception that wraps a checked {@link LdapException}.
 * We use this in contexts where declaring a checked exception is not permitted —
 * for example inside a {@code Comparator.compare()} implementation or a lambda
 * that must conform to a functional interface that doesn't declare checked exceptions.
 * Callers that catch this at a higher level should unwrap the cause via
 * {@link #getCause()} to recover the original {@link LdapException} and handle it.
 * Think of this as Han shooting first and running — the exception propagates
 * unchecked and someone higher up has to deal with it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapRuntimeException extends RuntimeException
{
    private static final long serialVersionUID = 3618077059423567243L;


    // ── CONSTRUCTOR — WRAP THE LDAP EXCEPTION ─────────────────────────────────────
    // Han fires (the LDAP exception happens) and we package the incident into an
    // unchecked wrapper so it can propagate without declaration.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link LdapRuntimeException} wrapping the given checked exception.
     *
     * @param exception  The {@link LdapException} to wrap as a runtime exception.
     */
    public LdapRuntimeException( LdapException exception )
    {
        super( exception );
    }
}
