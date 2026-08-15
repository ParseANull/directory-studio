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

// ── CLASS: AbstractAclWhoClause — DEATH STAR MANIFEST BASE ENTRY TEMPLATE ────
// Every row on Tarkin's clearance manifest shares two things regardless of what
// type of subject it describes: an access level (what they may do) and an
// optional flow-control directive (what happens after the rule fires). This
// abstract class implements both, so concrete who-clause subclasses inherit the
// common accessLevel / control storage and the serialiser logic without
// repeating code. Subclasses only add the subject-type prefix ("dn=", "self",
// "anonymous", etc.).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Common base for every concrete "by" clause implementation. Holds the
 * {@link AclAccessLevel} (what operations are permitted) and the optional
 * {@link AclControlEnum} (stop/continue/break). The {@link #toString()} method
 * serialises those two parts; subclasses prepend their own identifying keyword
 * before calling it (or calling {@code super.toString()}).
 * Think of this class as the standard clearance-manifest template — every row
 * gets an access level and a flow-control stamp baked in, no matter what type
 * of subject it describes.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractAclWhoClause implements AclWhoClause
{
    /** The access level */
    protected AclAccessLevel accessLevel;

    /** The control */
    protected AclControlEnum control;


    // ── Reading the Access Level ───────────────────────────────────────────────
    // Tarkin's adjutant reads the access level from this manifest entry so the
    // serialiser knows what permissions to print after the subject keyword.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the access level stored on this who-clause. Implements
     * {@link AclWhoClause#getAccessLevel()}.
     *
     * <p>For example — reading the access level back from a manifest entry:</p>
     * <pre>
     *   AclAccessLevel lvl = clause.getAccessLevel();
     *   // lvl.getLevel() == AclAccessLevelLevelEnum.READ
     * </pre>
     *
     * @return  The {@link AclAccessLevel}; may be {@code null} if not yet set.
     */
    public AclAccessLevel getAccessLevel()
    {
        return accessLevel;
    }


    // ── Reading the Flow-Control Directive ────────────────────────────────────
    // Tarkin's adjutant reads the flow-control stamp so the serialiser can
    // append "stop", "continue", or "break" after the access level.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the evaluation flow-control directive stored on this who-clause.
     * Implements {@link AclWhoClause#getControl()}.
     *
     * <p>For example — reading the flow-control stamp:</p>
     * <pre>
     *   AclControlEnum ctrl = clause.getControl();
     *   // ctrl == AclControlEnum.STOP  →  "stop" appended to ACL text
     * </pre>
     *
     * @return  The {@link AclControlEnum}; may be {@code null} if unset.
     */
    public AclControlEnum getControl()
    {
        return control;
    }


    // ── Stamping the Access Level Onto the Entry ──────────────────────────────
    // The parser or the visual editor stamps the access level onto this entry.
    // Implements the contract from the AclWhoClause interface.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the access level for this who-clause. Called by the ANTLR parser
     * after recognising the access-level token in the ACL text, and by the
     * visual editor when the user changes the level combo box.
     *
     * <p>For example — the parser stamping WRITE onto this entry:</p>
     * <pre>
     *   clause.setAccessLevel(new AclAccessLevel(AclAccessLevelLevelEnum.WRITE));
     * </pre>
     *
     * @param accessLevel  The {@link AclAccessLevel} to apply.
     */
    public void setAccessLevel( AclAccessLevel accessLevel )
    {
        this.accessLevel = accessLevel;
    }


    // ── Stamping the Flow-Control Directive ───────────────────────────────────
    // The parser stamps the flow-control directive after the access level is
    // recognised. Implements the contract from AclWhoClause.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the evaluation flow-control word for this who-clause. Called by the
     * ANTLR parser when it finds a control keyword after the access level.
     *
     * <p>For example — the parser recording CONTINUE on this entry:</p>
     * <pre>
     *   clause.setControl(AclControlEnum.CONTINUE);
     * </pre>
     *
     * @param control  The {@link AclControlEnum} to apply.
     */
    public void setControl( AclControlEnum control )
    {
        this.control = control;
    }


    // ── Serialising the Common Access-Level and Control Suffix ────────────────
    // Every who-clause ends with the access level followed by the optional
    // control word. Subclasses call this (via super.toString()) to get that
    // common suffix and then prepend their own subject-type keyword.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises the access level and control word that follow the subject
     * keyword in a "by" clause. Subclasses prepend their own identifier (e.g.
     * "anonymous", "*", "self") before or around this output. Returns an empty
     * string if neither access level nor control is set.
     *
     * <p>For example — getting the common suffix "read stop":</p>
     * <pre>
     *   clause.setAccessLevel(...READ...);
     *   clause.setControl(AclControlEnum.STOP);
     *   clause.toString(); // → "read stop"  (subclass prepends "anonymous ")
     * </pre>
     *
     * @return  The access-level and control-word suffix, or empty string if both are null.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        // Access Level
        if ( accessLevel != null )
        {
            sb.append( accessLevel );
        }

        // Control
        if ( control != null )
        {
            if ( sb.length() > 0 )
            {
                sb.append( " " );
            }

            sb.append( control );
        }

        return sb.toString();
    }
}
