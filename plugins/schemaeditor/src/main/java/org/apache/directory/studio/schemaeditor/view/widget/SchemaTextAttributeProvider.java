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
package org.apache.directory.studio.schemaeditor.view.widget;


import java.util.HashMap;
import java.util.Map;

import org.apache.directory.studio.common.ui.CommonUIConstants;
import org.apache.directory.studio.common.ui.CommonUIPlugin;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;


// ── CLASS: SchemaTextAttributeProvider — OBI-WAN DISABLES THE TRACTOR BEAM ───
// On the Death Star, Obi-Wan moves through the corridors and disables each power
// coupling on the tractor beam array — each junction gets a very specific treatment
// based on what it controls. No junction is handled the same as another. He knows
// exactly which colour of conduit belongs to which system.
// Our class does the same: each type of schema token (keyword, string, OID, etc.)
// gets a precisely configured TextAttribute — a specific colour, optionally bolded —
// loaded once at construction time and served up on demand.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Provides the SWT {@link TextAttribute} objects (colour + style) that the
 * {@link SchemaCodeScanner} uses to colour different categories of schema source text.
 * At construction time we populate a map with one entry per token category; callers
 * retrieve an attribute by passing the matching type constant (e.g.
 * {@link #KEYWORD_ATTRIBUTE}). An unrecognised type falls back to
 * {@link #DEFAULT_ATTRIBUTE} rather than returning {@code null}.
 * Think of it as Obi-Wan's tractor beam mission: every junction (token type) gets
 * exactly the right treatment, and nothing is left unaccounted for.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaTextAttributeProvider
{
    public static final String DEFAULT_ATTRIBUTE = "__pos_schema_default_attribute"; //$NON-NLS-1$
    public static final String STRING_ATTRIBUTE = "__pos_schema_string_attribute"; //$NON-NLS-1$
    public static final String KEYWORD_ATTRIBUTE = "__pos_schema_keyword_attribute"; //$NON-NLS-1$
    public static final String ATTRIBUTETYPE_ATTRIBUTE = "__pos_schema_attributetype_attribute"; //$NON-NLS-1$
    public static final String OBJECTCLASS_ATTRIBUTE = "__pos_schema_objectclass_attribute"; //$NON-NLS-1$
    public static final String OID_ATTRIBUTE = "__pos_schema_oid_attribute"; //$NON-NLS-1$

    private Map<String, TextAttribute> attributes = new HashMap<String, TextAttribute>();


    // ── OBI-WAN PREPARES HIS TOOLS AT EACH JUNCTION ──────────────────────────────
    // Obi-Wan walks the tractor beam array and configures each power junction before
    // any text arrives. Default conduits get a neutral colour; keyword conduits get
    // bold; string conduits get the value colour; AT and OC conduits get their own
    // bold colours so they stand out at the top of each schema block.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the provider and pre-loads the colour+style mapping for all six
     * token categories. We pull colours from {@link CommonUIPlugin} so the schema
     * editor respects the same colour scheme as the rest of Directory Studio.
     * The map is populated once here and never modified again.
     *
     * <p>For example — Obi-Wan configures each power junction:</p>
     * <pre>
     *   DEFAULT_ATTRIBUTE     →  default text colour, plain
     *   KEYWORD_ATTRIBUTE     →  keyword colour, bold
     *   STRING_ATTRIBUTE      →  value colour, plain
     *   ATTRIBUTETYPE_ATTRIBUTE →  attribute-type colour, bold
     *   OBJECTCLASS_ATTRIBUTE →  object-class colour, bold
     *   OID_ATTRIBUTE         →  OID colour, plain
     * </pre>
     */
    public SchemaTextAttributeProvider()
    {
        CommonUIPlugin plugin = CommonUIPlugin.getDefault();
        attributes.put( DEFAULT_ATTRIBUTE, new TextAttribute( plugin.getColor( CommonUIConstants.DEFAULT_COLOR ) ) );
        attributes.put( KEYWORD_ATTRIBUTE, new TextAttribute( plugin.getColor( CommonUIConstants.KEYWORD_2_COLOR ), null, SWT.BOLD ) );
        attributes.put( STRING_ATTRIBUTE, new TextAttribute( plugin.getColor( CommonUIConstants.VALUE_COLOR ) ) );
        attributes.put( ATTRIBUTETYPE_ATTRIBUTE, new TextAttribute( plugin.getColor( CommonUIConstants.ATTRIBUTE_TYPE_COLOR ), null, SWT.BOLD ) );
        attributes.put( OBJECTCLASS_ATTRIBUTE, new TextAttribute( plugin.getColor( CommonUIConstants.OBJECT_CLASS_COLOR ), null, SWT.BOLD ) );
        attributes.put( OID_ATTRIBUTE, new TextAttribute( plugin.getColor( CommonUIConstants.OID_COLOR ) ) );
    }


    // ── OBI-WAN HANDS THE SCANNER THE RIGHT TOOL FOR EACH JUNCTION ───────────────
    // Obi-Wan reaches into his kit for the right tool for each power junction — he
    // never comes up empty-handed, because if the specific tool is missing he defaults
    // to the general-purpose one. We do the same: unknown type keys fall back to
    // DEFAULT_ATTRIBUTE so the scanner always gets a usable TextAttribute.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link TextAttribute} for the given token type key. If the key is
     * not recognised (e.g. a typo or a future token type we have not mapped yet), we
     * fall back to {@link #DEFAULT_ATTRIBUTE} rather than returning {@code null},
     * which would cause a NullPointerException in the scanner.
     *
     * <p>For example — Obi-Wan selects the right tool:</p>
     * <pre>
     *   getAttribute(KEYWORD_ATTRIBUTE)    →  bold keyword colour
     *   getAttribute(OID_ATTRIBUTE)        →  OID colour, plain
     *   getAttribute("unknown_type_key")   →  default colour (fallback)
     * </pre>
     *
     * @param type  one of the type constants defined in this class
     *              ({@link #DEFAULT_ATTRIBUTE}, {@link #KEYWORD_ATTRIBUTE}, etc.)
     * @return      the {@link TextAttribute} for that type, or the default attribute
     *              if the type key is not in our map
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
