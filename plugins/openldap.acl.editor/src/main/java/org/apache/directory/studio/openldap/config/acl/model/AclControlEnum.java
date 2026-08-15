/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl.model;


// ── CLASS: AclControlEnum — IMPERIAL CHECKPOINT FLOW CONTROL ─────────────────
// At an Imperial checkpoint, after the stormtroopers check your credentials,
// a supervisor decides what happens next: "stop" means you go no further and
// the decision is final; "continue" means we keep checking the next rule on
// the list anyway; "break" means we stop this ACL list but move to the next
// ACL list. OpenLDAP's ACL evaluation follows the same three-way branching.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The optional control word that appears after the access level in a "by"
 * clause. It tells OpenLDAP what to do after this ACL rule is matched:
 * stop evaluating entirely ({@code stop}), keep evaluating the remaining rules
 * in this list ({@code continue}), or stop this list and move to the next one
 * ({@code break}).
 * Think of this enum as an Imperial checkpoint supervisor's decision after
 * checking your ID — halt, wave you through to the next checkpoint, or pass
 * you up to the next security tier entirely.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum AclControlEnum
{
    STOP,
    CONTINUE,
    BREAK;


    // ── Rendering the Flow-Control Word for the Wire ──────────────────────────
    // The supervisor announces his decision in plain text — "stop", "continue",
    // or "break" — and we match that exactly for the ACL string output.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the lower-case keyword that OpenLDAP places after the access
     * level in a "by" clause to control evaluation flow. This goes straight
     * into the serialised ACL text.
     *
     * <p>For example — a checkpoint supervisor announcing the decision:</p>
     * <pre>
     *   AclControlEnum.STOP.toString()     // → "stop"
     *   AclControlEnum.CONTINUE.toString() // → "continue"
     *   AclControlEnum.BREAK.toString()    // → "break"
     * </pre>
     *
     * @return  The OpenLDAP control keyword for this enum constant.
     */
    public String toString()
    {
        switch ( this )
        {
            case STOP:
                return "stop";
            case CONTINUE:
                return "continue";
            case BREAK:
                return "break";
        }

        return super.toString();
    }
}
