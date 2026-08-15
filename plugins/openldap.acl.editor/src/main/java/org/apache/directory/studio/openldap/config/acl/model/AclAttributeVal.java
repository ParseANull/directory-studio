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

import java.util.ArrayList;
import java.util.List;

// ── CLASS: AclAttributeVal — DEATH STAR MANIFEST: ATTRIBUTE + VALUE MATCHER ──
// Tarkin's manifest sometimes protects not just "these attributes" but only
// those attributes where the value matches a specific pattern — for example
// "userPassword where value.exact equals a certain string." The AclAttributeVal
// bean captures all those qualifiers together: the list of AclAttributes, an
// optional val flag (does the rule apply only to matching values?), an optional
// matchingRule, an attribute-matching style (exact, regex, etc.), and the value
// pattern itself.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A data bag holding the full attribute specification for an ACL what-clause:
 * the list of attribute names, plus the optional {@code val} qualifier that
 * restricts the rule to entries where a specific attribute value matches.
 * Written in ACL text as {@code attrs=IDENT[,IDENT]* [val[/mr][.style]="regex"]}.
 * Think of this class as Tarkin's fine-grained attribute manifest: not just
 * "protect userPassword" but "protect userPassword only when its value is one
 * of these specific patterns."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclAttributeVal
{
    /** The list of AclAttributes */
    List<AclAttribute> aclAttributes = new ArrayList<AclAttribute>();

    /** The attribute style*/
    private AclAttributeStyleEnum style;

    /** The val flag */
    private boolean val;

    /** The MatchingRule flag */
    private boolean matchingRule;

    /** The regex */
    private String regex;
    private String value;


    // ── Reading the Attribute List ─────────────────────────────────────────────
    // Tarkin's adjutant reads the list of attribute names that this clause covers.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of {@link AclAttribute} objects representing the attribute
     * names to which this what-clause applies.
     *
     * <p>For example — reading the attribute list:</p>
     * <pre>
     *   val.getAclAttributes() // → [AclAttribute("userPassword"), AclAttribute("uid")]
     * </pre>
     *
     * @return  The list of attributes; never {@code null}.
     */
    public List<AclAttribute> getAclAttributes()
    {
        return aclAttributes;
    }

    // ── Stamping the Attribute List ────────────────────────────────────────────
    // The parser stamps the full attribute list onto this bean after reading
    // all the comma-separated attribute names from the ACL text.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the entire attribute list with the given list. Called by the
     * parser or the visual editor when rebuilding the attribute set from scratch.
     *
     * @param aclAttributes  The new attribute list.
     */
    public void setAclAttributes( List<AclAttribute> aclAttributes )
    {
        this.aclAttributes = aclAttributes;
    }

    // ── Reading the Value-Matching Style ──────────────────────────────────────
    // Tarkin reads the style of the value matcher — exact, regex, etc.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the style qualifier used in the {@code val} matcher (exact, base,
     * baseobject, or regex). {@code null} or {@code NONE} if no val qualifier
     * was specified.
     *
     * @return  The {@link AclAttributeStyleEnum}; may be {@code null}.
     */
    public AclAttributeStyleEnum getStyle()
    {
        return style;
    }

    // ── Stamping the Value-Matching Style ─────────────────────────────────────
    // The parser stamps the style keyword after the dot in "val.exact=...".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the value-matching style. Called by the parser when it finds
     * a dot-qualifier on the {@code val} token (e.g. {@code val.exact}).
     *
     * @param style  The {@link AclAttributeStyleEnum} to apply.
     */
    public void setStyle( AclAttributeStyleEnum style )
    {
        this.style = style;
    }

    // ── Checking Whether a Val Qualifier Is Present ───────────────────────────
    // Tarkin checks whether this clause has a value-matching qualifier at all.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the {@code val} qualifier is present, meaning the
     * rule applies only when the attribute value matches the specified pattern.
     *
     * @return  {@code true} if val mode is active.
     */
    public boolean hasVal()
    {
        return val;
    }

    // ── Enabling/Disabling the Val Qualifier ──────────────────────────────────
    // The parser enables val mode when it sees the "val" keyword after the
    // attribute list in the ACL text.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the {@code val} qualifier mode.
     *
     * @param val  {@code true} if the clause has a value-matching qualifier.
     */
    public void setVal( boolean val )
    {
        this.val = val;
    }

    // ── Checking Whether a MatchingRule Is Present ────────────────────────────
    // The "val/mr" form adds a custom matching rule to the value comparison.
    // Tarkin checks whether one was specified.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if a custom matching rule (the {@code /mr} qualifier)
     * was specified in the val clause.
     *
     * @return  {@code true} if a matching rule is active.
     */
    public boolean hasMatchingRule()
    {
        return matchingRule;
    }

    // ── Enabling/Disabling the MatchingRule Qualifier ─────────────────────────
    // The parser enables matchingRule mode when it sees "/mr" after "val".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the matching-rule mode.
     *
     * @param matchingRule  {@code true} if a matching rule qualifier is present.
     */
    public void setMatchingRule( boolean matchingRule )
    {
        this.matchingRule = matchingRule;
    }

    // ── Reading the Value Pattern ─────────────────────────────────────────────
    // Tarkin reads the pattern (regex or exact string) used to match attribute
    // values when val mode is active.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value pattern used for matching in val mode. This is the
     * quoted string after the {@code val[/mr][.style]=} portion.
     *
     * @return  The value pattern string; may be {@code null} if not set.
     */
    public String getValue()
    {
        return value;
    }

    // ── Stamping the Value Pattern ────────────────────────────────────────────
    // The parser stamps the quoted value pattern after the "=" in "val.exact=...".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the value pattern for this attribute val matcher.
     *
     * @param value  The quoted value pattern extracted from the ACL text.
     */
    public void setValue( String value )
    {
        this.value = value;
    }
}
