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


// ── CLASS: PartitionsDiffException — IMPERIAL DATA VAULT AUDIT FAILURE ───────────────────
// When the Imperial audit team tries to compare two versions of the Death Star's data vaults
// to compute what changed, something can go wrong — corrupt vault, incompatible formats, a
// missing section that was supposed to be there.  When that happens they file this exception.
// PartitionsDiffException is thrown by PartitionsDiffComputer when the comparison of two
// partition states fails — corrupt data, schema mismatch, or missing required entries.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * Thrown by {@code PartitionsDiffComputer} when computing the difference between two
 * partition states fails.
 * Wraps the root cause and carries a descriptive message about what went wrong.
 * Think of it as the Imperial data vault audit failure report.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PartitionsDiffException extends Exception
{
    private static final long serialVersionUID = 1L;


    // ── Filing A No-Message Audit Failure Report ──────────────────────────────────────────────
    // The audit failed but we have no details to add to the report.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code PartitionsDiffException} with no detail message.
     */
    public PartitionsDiffException()
    {
        super();
    }


    // ── Filing A Detailed Audit Failure Report With A Root Cause ─────────────────────────────
    // The audit failed; we include both a description and the original cause.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code PartitionsDiffException} with the given detail message and cause.
     *
     * @param message  describes what went wrong during the diff computation
     * @param cause    the underlying exception that triggered this failure
     */
    public PartitionsDiffException( String message, Throwable cause )
    {
        super( message, cause );
    }


    // ── Filing A Detailed Audit Failure Report ────────────────────────────────────────────────
    // The audit failed; we include a description of why.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code PartitionsDiffException} with the given detail message.
     *
     * @param message  describes what went wrong during the diff computation
     */
    public PartitionsDiffException( String message )
    {
        super( message );
    }


    // ── Filing A Root-Cause Audit Failure Report ──────────────────────────────────────────────
    // The audit failed due to another exception; the message is derived from the cause.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code PartitionsDiffException} wrapping the given cause.
     * The detail message is set to {@code cause.toString()}.
     *
     * @param cause  the underlying exception that triggered this failure
     */
    public PartitionsDiffException( Throwable cause )
    {
        super( cause );
    }
}
