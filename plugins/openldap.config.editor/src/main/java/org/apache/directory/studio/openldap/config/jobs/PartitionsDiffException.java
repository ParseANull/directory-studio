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
package org.apache.directory.studio.openldap.config.jobs;


// ── CLASS: PartitionsDiffException — An Escape Pod Ejected When Things Go Wrong
// When comparing two LDAP partitions, the strategic comparison room may detect
// a critical inconsistency — a missing base entry, an uninitialized partition,
// or a null suffix.  PartitionsDiffException is the escape pod that carries
// whatever diagnostic information is available out of the failing operation and
// to the surface where the editor can report the failure cleanly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * This exception can be raised when an error occurs when computing the diff
 * between two partitions.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PartitionsDiffException extends Exception
{
    private static final long serialVersionUID = 1L;


    // ── Default Constructor — An Empty Escape Pod ─────────────────────────────
    // The pod launches without any diagnostic information attached.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new PartitionsDiffException with <code>null</code> as its detail message.
     */
    public PartitionsDiffException()
    {
        super();
    }


    // ── Constructor (String, Throwable) — A Pod Carrying Message and Cause ────
    // The pod carries both a human-readable message and the underlying
    // exception that triggered the failure.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new PartitionsDiffException with the specified detail message and cause.
     *
     * @param message
     *      the message
     * @param cause
     *      the cause
     */
    public PartitionsDiffException( String message, Throwable cause )
    {
        super( message, cause );
    }


    // ── Constructor (String) — A Pod Carrying Only a Message ─────────────────
    // The pod carries a human-readable description of what went wrong, without
    // a chained exception.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new PartitionsDiffException with the specified detail message.
     *
     * @param message
     *      the message
     */
    public PartitionsDiffException( String message )
    {
        super( message );
    }


    // ── Constructor (Throwable) — A Pod Carrying Only the Cause ──────────────
    // The pod wraps an existing exception without adding a separate message.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new exception with the specified cause and a detail message
     * of <code>(cause==null ? null : cause.toString())</code>
     *
     * @param cause
     *      the cause
     */
    public PartitionsDiffException( Throwable cause )
    {
        super( cause );
    }
}
