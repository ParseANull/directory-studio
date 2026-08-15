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
package org.apache.directory.studio.openldap.config.acl.sourceeditor;


import java.util.HashMap;
import java.util.Map;

import org.apache.directory.studio.common.ui.CommonUIConstants;
import org.apache.directory.studio.common.ui.CommonUIPlugin;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;


// ── CLASS: OpenLdapAclTextAttributeProvider — CASSIAN'S COLOUR-CODED DISPLAY ─
// At Cassian's rebel intelligence terminal, different categories of text appear
// in different colours: plain text is white/default, ACL keywords (access, by,
// dn, read, …) are highlighted in bold attribute-type colour, and quoted string
// values are shown in a distinct value colour. This class maps each category
// constant to the correct SWT TextAttribute so the code scanner can colour the
// source text accordingly. It is instantiated once (stored in the plugin
// singleton) and shared across all scanner instances.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides {@link TextAttribute} objects for each syntactic category used in the
 * OpenLDAP ACL source editor. The scanner requests attributes by their constant
 * key and receives the correct colour/style combination to apply to the matching
 * text range.
 *
 * <p>Three categories are defined:</p>
 * <ul>
 *   <li>{@link #DEFAULT_ATTRIBUTE} — plain text (default foreground colour)</li>
 *   <li>{@link #KEYWORD_ATTRIBUTE} — ACL keywords (attribute-type colour, bold)</li>
 *   <li>{@link #STRING_ATTRIBUTE}  — quoted string values (value colour)</li>
 * </ul>
 *
 * <p>Think of this class as Cassian's colour-coded display map: each token type
 * is assigned a shade so the officer can instantly distinguish keywords from
 * values in the ACL stream.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapAclTextAttributeProvider
{
    /** Constant key for the default (plain text) attribute. */
    public static final String DEFAULT_ATTRIBUTE = "__pos_acl_default_attribute"; //$NON-NLS-1$

    /** Constant key for the keyword (access/by/dn/…) attribute — bold. */
    public static final String KEYWORD_ATTRIBUTE = "__pos_acl_keyword_attribute"; //$NON-NLS-1$

    /** Constant key for the quoted string value attribute. */
    public static final String STRING_ATTRIBUTE = "__pos_acl_string_attribute"; //$NON-NLS-1$

    private Map<String, TextAttribute> attributes = new HashMap<String, TextAttribute>();


    // ── Building the Attribute Map ─────────────────────────────────────────────
    // Cassian sets up the colour-coded display: default foreground for plain text,
    // bold attribute-type colour for keywords, and value colour for strings.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the provider and populates the three attribute entries using colours
     * from the common UI plugin:
     * <ul>
     *   <li>DEFAULT_ATTRIBUTE → {@link CommonUIConstants#DEFAULT_COLOR}</li>
     *   <li>KEYWORD_ATTRIBUTE → {@link CommonUIConstants#ATTRIBUTE_TYPE_COLOR}, bold</li>
     *   <li>STRING_ATTRIBUTE  → {@link CommonUIConstants#VALUE_COLOR}</li>
     * </ul>
     */
    public OpenLdapAclTextAttributeProvider()
    {
        CommonUIPlugin plugin = CommonUIPlugin.getDefault();
        attributes.put( DEFAULT_ATTRIBUTE, new TextAttribute( plugin.getColor( CommonUIConstants.DEFAULT_COLOR ) ) );
        attributes.put( KEYWORD_ATTRIBUTE,
            new TextAttribute( plugin.getColor( CommonUIConstants.ATTRIBUTE_TYPE_COLOR ), null, SWT.BOLD ) );
        attributes.put( STRING_ATTRIBUTE, new TextAttribute( plugin.getColor( CommonUIConstants.VALUE_COLOR ) ) );
    }


    // ── Looking Up a TextAttribute by Type Key ────────────────────────────────
    // The code scanner calls getAttribute() with one of the three constants above
    // to get the correct colour+style combination for each token type.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link TextAttribute} registered for the given type key. If the
     * key is not found (e.g. an unrecognised type constant), falls back to the
     * default attribute.
     *
     * <p>For example — the scanner requesting the keyword colour:</p>
     * <pre>
     *   TextAttribute kw = provider.getAttribute(KEYWORD_ATTRIBUTE);
     *   // kw.getStyle() == SWT.BOLD, colour = ATTRIBUTE_TYPE_COLOR
     * </pre>
     *
     * @param type  One of {@link #DEFAULT_ATTRIBUTE}, {@link #KEYWORD_ATTRIBUTE}, or
     *              {@link #STRING_ATTRIBUTE}.
     * @return      The matching {@link TextAttribute}; falls back to DEFAULT_ATTRIBUTE
     *              if the type is unknown.
     */
    public TextAttribute getAttribute( String type )
    {
        TextAttribute attr = ( TextAttribute ) attributes.get( type );
        if ( attr == null )
        {
            attr = ( TextAttribute ) attributes.get( DEFAULT_ATTRIBUTE );
        }
        return attr;
    }
}
