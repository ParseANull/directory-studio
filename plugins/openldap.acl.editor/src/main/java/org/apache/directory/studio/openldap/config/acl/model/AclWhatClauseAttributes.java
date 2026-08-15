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
import java.util.Collection;
import java.util.List;

// ── CLASS: AclWhatClauseAttributes — DEATH STAR MANIFEST: ATTRIBUTE LIST SELECTOR ──
// Tarkin's manifest can protect not just directory entries but specific
// attributes within those entries — and optionally further narrow that to
// entries where a named attribute's value matches a pattern. This class models
// the "attrs=..." portion of an ACL what-clause, delegating all the data
// storage to an AclAttributeVal bean and providing a clean API for the parser
// and the visual editor to work with.
// Grammar it models:
//   attrs=IDENT[,IDENT]* [val[/mr][.style]="regex"]
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete what-clause that targets a comma-separated list of LDAP attributes
 * (and optional value-matching qualifiers). Written as
 * {@code attrs=ATTR[,ATTR]* [val[/mr][.style]="pattern"]}.
 *
 * <p>Grammar reference:</p>
 * <pre>
 * &lt;what-attrs&gt;     ::= ( 'attrs' | 'attr' ) SP '=' SP &lt;attrs&gt;
 * &lt;attrs&gt;          ::= IDENT SP 'val' &lt;mr-e&gt; &lt;attr-val-style&gt; SP? '=' SP? REGEX | &lt;what-attr&gt; &lt;what-attr-list&gt;
 * &lt;attr-val-style&gt; ::= '.' &lt;basic-style&gt; | e
 * &lt;basic-style&gt;    ::= 'exact' | 'base' | 'baseobject' | 'regex'
 * &lt;mr-e&gt;           ::= '/' IDENT | e
 * &lt;what-attr-list&gt; ::= ',' &lt;what-attr&gt; &lt;what-attr-list&gt; | e
 * &lt;what-attr&gt;      ::= IDENT | '@' IDENT | '!' IDENT | 'entry' | 'children'
 * </pre>
 *
 * Think of this class as Tarkin's fine-grained attribute selector: instead of
 * locking down the whole entry, he lists exactly which fields are guarded.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclWhatClauseAttributes extends AclWhatClause
{
    /** The attributeVal element */
    private AclAttributeVal aclAttributeVal;


    // ── Initialising an Empty Attribute Clause ────────────────────────────────
    // The parser creates an empty AclWhatClauseAttributes and then adds
    // attribute names one at a time as it reads comma-separated values.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an empty attribute clause with an empty attribute list and no
     * val qualifier. Attributes are added incrementally via {@link #addAttribute(String)}.
     */
    public AclWhatClauseAttributes()
    {
        aclAttributeVal = new AclAttributeVal();
    }


    // ── Reading a Defensive Copy of the Attribute List ────────────────────────
    // Tarkin's adjutant hands back a copy of the attribute list rather than the
    // live internal list, so callers cannot accidentally modify it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the attribute list. Callers receive a new
     * list containing the same {@link AclAttribute} references, so iterating
     * or clearing the returned list does not affect the stored data.
     *
     * <p>For example — reading the attribute list for display:</p>
     * <pre>
     *   List&lt;AclAttribute&gt; attrs = clause.getAttributes();
     *   // [AclAttribute("uid"), AclAttribute("cn")]
     * </pre>
     *
     * @return  A new list containing the stored {@link AclAttribute} objects.
     */
    public List<AclAttribute> getAttributes()
    {
        List<AclAttribute> copyAttributes = new ArrayList<AclAttribute>( aclAttributeVal.getAclAttributes().size() );

        copyAttributes.addAll( aclAttributeVal.getAclAttributes() );

        return copyAttributes;
    }


    // ── Adding One Attribute Name ─────────────────────────────────────────────
    // The parser adds attribute names one at a time as it reads them from the
    // comma-separated attrs= list in the ACL text.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Appends a single attribute name to the list. The name is wrapped in an
     * {@link AclAttribute} with no connection (for schema lookups pass a
     * connection-aware AclAttribute directly via {@link #addAllAttributes(Collection)}).
     *
     * <p>For example — the parser adding attribute names one by one:</p>
     * <pre>
     *   clause.addAttribute("uid");
     *   clause.addAttribute("cn");
     *   clause.toString(); // → "attrs=uid,cn"
     * </pre>
     *
     * @param attribute  The attribute name string to add (may include @ or ! prefix).
     */
    public void addAttribute( String attribute )
    {
        aclAttributeVal.getAclAttributes().add( new AclAttribute( attribute, null ) );
    }


    // ── Bulk-Adding Attributes ────────────────────────────────────────────────
    // When we have a pre-built collection of AclAttribute objects (from the
    // visual editor, with connection set for schema lookup) we add them all at once.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds all attributes from the given collection to the attribute list.
     * Use this when adding {@link AclAttribute} objects that already have their
     * connection set for schema lookups.
     *
     * @param attributes  Collection of {@link AclAttribute} objects to add.
     */
    public void addAllAttributes( Collection<AclAttribute> attributes )
    {
        aclAttributeVal.getAclAttributes().addAll( attributes );
    }


    // ── Clearing the Attribute List ───────────────────────────────────────────
    // The visual editor clears the list before rebuilding it from current UI state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes all attributes from the list. Called by the visual editor when it
     * rebuilds the attribute list after the user has made changes.
     */
    public void clearAttributes()
    {
        aclAttributeVal.getAclAttributes().clear();
    }


    // ── Checking Whether a Val Qualifier Is Active ────────────────────────────
    // Tarkin checks whether the "val" keyword was in the ACL text, meaning we
    // apply this rule only to entries where the attribute's value matches.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the {@code val} qualifier is active. When true,
     * the rule applies only to attribute values matching the stored pattern.
     *
     * @return  {@code true} if val mode is active.
     */
    public boolean hasVal()
    {
        return aclAttributeVal.hasVal();
    }


    // ── Enabling/Disabling the Val Qualifier ──────────────────────────────────
    // The parser enables this when it encounters the "val" keyword after the
    // attribute list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the {@code val} value-matching qualifier.
     *
     * @param val  {@code true} to enable val mode.
     */
    public void setVal( boolean val )
    {
        aclAttributeVal.setVal( val );
    }


    // ── Checking Whether a MatchingRule Is Present ────────────────────────────
    // The "val/mr" form adds a custom LDAP matching rule to the comparison.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if a custom matching rule was specified.
     *
     * @return  {@code true} if a matching rule qualifier is active.
     */
    public boolean hasMatchingRule()
    {
        return aclAttributeVal.hasMatchingRule();
    }


    // ── Enabling/Disabling the MatchingRule Qualifier ─────────────────────────
    // The parser enables this when it sees "/mr" after "val".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the matching-rule mode.
     *
     * @param matchingRule  {@code true} if a matching rule qualifier is present.
     */
    public void setMatchingRule( boolean matchingRule )
    {
        aclAttributeVal.setMatchingRule( matchingRule );
    }


    // ── Reading the Attribute-Value Matching Style ────────────────────────────
    // Tarkin reads which matching style to use for the value comparison.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the style qualifier for the val matcher.
     *
     * @return  The {@link AclAttributeStyleEnum}; may be {@code null}.
     */
    public AclAttributeStyleEnum getStyle()
    {
        return aclAttributeVal.getStyle();
    }


    // ── Stamping the Attribute-Value Matching Style ───────────────────────────
    // The parser stamps the style after the dot in "val.exact=...".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the value-matching style.
     *
     * @param style  The {@link AclAttributeStyleEnum} to apply.
     */
    public void setStyle( AclAttributeStyleEnum style )
    {
        aclAttributeVal.setStyle( style );
    }


    // ── Reading the Value Pattern ─────────────────────────────────────────────
    // Tarkin reads the quoted pattern used to match attribute values.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value pattern used in the val matcher.
     *
     * @return  The value pattern string; may be {@code null}.
     */
    public String getValue()
    {
        return aclAttributeVal.getValue();
    }


    // ── Stamping the Value Pattern ────────────────────────────────────────────
    // The parser stamps the quoted value after the "=" in "val.exact=...".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the value pattern for the val matcher.
     *
     * @param value  The quoted value pattern extracted from the ACL text.
     */
    public void setValue( String value )
    {
        aclAttributeVal.setValue( value );
    }


    // ── Serialising the Attributes Clause to ACL Text ─────────────────────────
    // Tarkin's adjutant writes the full attribute selector: "attrs=uid,cn" or
    // "attrs=userPassword val.exact=\"secret\"". Every piece is assembled in
    // the correct order.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises this clause to OpenLDAP wire format. Produces
     * {@code attrs=ATTR[,ATTR]*} optionally followed by
     * {@code val[/matchingRule][.style]="pattern"}.
     *
     * <p>For example — serialising an attributes clause with val:</p>
     * <pre>
     *   clause.addAttribute("userPassword");
     *   clause.setVal(true);
     *   clause.setStyle(AclAttributeStyleEnum.EXACT);
     *   clause.setValue("secret");
     *   clause.toString();
     *   // → "attrs=userPassword val.exact=\"secret\""
     * </pre>
     *
     * @return  The ACL text fragment for this attributes what-clause.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        // Attrs
        sb.append( "attrs" );

        // Attributes
        if ( ( aclAttributeVal.getAclAttributes() != null ) && ( aclAttributeVal.getAclAttributes().size() > 0 ) )
        {
            sb.append( '=' );
            boolean isFirst = true;

            for ( AclAttribute attribute : aclAttributeVal.getAclAttributes() )
            {
                if ( isFirst )
                {
                    isFirst = false;
                }
                else
                {
                    sb.append( ',' );
                }

                sb.append( attribute );
            }
        }

        // The val
        if ( aclAttributeVal.hasVal() )
        {
            sb.append( " val" );

            if ( aclAttributeVal.hasMatchingRule() )
            {
                sb.append( "/matchingRule" );
            }

            if ( aclAttributeVal.getStyle() != AclAttributeStyleEnum.NONE )
            {
                sb.append( '.' );
                sb.append( aclAttributeVal.getStyle().getName() );
            }

            sb.append( "=\"" );
            sb.append( aclAttributeVal.getValue() );
            sb.append( '"' );
        }

        return sb.toString();
    }
}
