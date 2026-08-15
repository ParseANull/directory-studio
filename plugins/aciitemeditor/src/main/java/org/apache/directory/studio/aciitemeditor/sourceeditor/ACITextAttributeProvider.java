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
package org.apache.directory.studio.aciitemeditor.sourceeditor;


import java.util.HashMap;
import java.util.Map;

import org.apache.directory.studio.common.ui.CommonUIConstants;
import org.apache.directory.studio.common.ui.CommonUIPlugin;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;


// ── CLASS: ACITextAttributeProvider — C-3PO'S COLOUR ANNOTATION KIT ──────────
// C-3PO keeps a kit of colour-coded stickers — one shade for general keywords,
// another for grant values, a bright red for denials, and so on.
// Before he annotates a directive he selects the right sticker for each word.
// ACITextAttributeProvider is that kit: it maps token-type keys to SWT
// TextAttribute objects (colour + bold/normal style).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides the {@link TextAttribute} (colour and font style) for each token type
 * recognised by the ACI source editor.
 * On construction it pre-builds a map from token-type keys (the {@code *_ATTRIBUTE}
 * constants) to {@link TextAttribute} instances that wrap SWT colours sourced from
 * the {@link CommonUIPlugin} colour registry.
 * Think of this class as C-3PO's colour annotation kit: one entry per token category,
 * each carrying the exact shade and weight to paint that token in the editor.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ACITextAttributeProvider
{
    public static final String DEFAULT_ATTRIBUTE = "__pos_aci_default_attribute"; //$NON-NLS-1$
    public static final String KEYWORD_ATTRIBUTE = "__pos_aci_keyword_attribute"; //$NON-NLS-1$
    public static final String STRING_ATTRIBUTE = "__pos_aci_string_attribute"; //$NON-NLS-1$
    public static final String IDENTIFICATION_ATTRIBUTE = "__pos_aci_identification_attribute"; //$NON-NLS-1$
    public static final String PRECEDENCE_ATTRIBUTE = "__pos_aci_precedence_attribute"; //$NON-NLS-1$
    public static final String AUTHENTICATIONLEVEL_ATTRIBUTE = "__pos_aci_authenticationlevel_attribute"; //$NON-NLS-1$
    public static final String ITEMORUSERFIRST_ATTRIBUTE = "__pos_aci_itemoruserfirst_attribute"; //$NON-NLS-1$
    public static final String USER_ATTRIBUTE = "__pos_aci_user_attribute"; //$NON-NLS-1$

    public static final String GRANT_VALUE = "__pos_aci_grant_value"; //$NON-NLS-1$
    public static final String DENY_VALUE = "__pos_aci_deny_value"; //$NON-NLS-1$

    private Map<String, TextAttribute> attributes = new HashMap<String, TextAttribute>();


    // ── FILL THE COLOUR KIT ───────────────────────────────────────────────────
    // C-3PO unpacks his sticker kit and assigns one colour to each token category:
    // default text stays neutral, keywords go bold, strings get the value colour,
    // grant values go green (add colour), and deny values go red (delete colour).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ACITextAttributeProvider} and pre-builds the
     * token-type-to-{@link TextAttribute} map.
     * Colours are sourced from {@link CommonUIPlugin} so they respect the
     * workspace colour preferences.
     *
     * <p>For example — the ACICodeScanner picks up attributes at construction time:</p>
     * <pre>
     *   ACITextAttributeProvider provider = new ACITextAttributeProvider();
     *   TextAttribute keywordAttr = provider.getAttribute(KEYWORD_ATTRIBUTE);
     *   // keywordAttr is bold, painted in the ATTRIBUTE_TYPE_COLOR
     * </pre>
     */
    public ACITextAttributeProvider()
    {
        CommonUIPlugin plugin = CommonUIPlugin.getDefault();
        attributes.put( DEFAULT_ATTRIBUTE, new TextAttribute( plugin.getColor( CommonUIConstants.DEFAULT_COLOR ) ) );
        attributes.put( KEYWORD_ATTRIBUTE, new TextAttribute( plugin.getColor( CommonUIConstants.ATTRIBUTE_TYPE_COLOR ), null, SWT.BOLD ) );
        attributes.put( STRING_ATTRIBUTE, new TextAttribute( plugin.getColor( CommonUIConstants.VALUE_COLOR ) ) );
        attributes.put( GRANT_VALUE, new TextAttribute( plugin.getColor( CommonUIConstants.ADD_COLOR ) ) );
        attributes.put( DENY_VALUE, new TextAttribute( plugin.getColor( CommonUIConstants.DELETE_COLOR ) ) );
        attributes.put( IDENTIFICATION_ATTRIBUTE, new TextAttribute( plugin.getColor( CommonUIConstants.KEYWORD_1_COLOR ), null, SWT.BOLD ) );
        attributes.put( PRECEDENCE_ATTRIBUTE, new TextAttribute( plugin.getColor( CommonUIConstants.KEYWORD_1_COLOR ), null, SWT.BOLD ) );
        attributes.put( AUTHENTICATIONLEVEL_ATTRIBUTE, new TextAttribute( plugin.getColor( CommonUIConstants.KEYWORD_1_COLOR ), null, SWT.BOLD ) );
        attributes.put( ITEMORUSERFIRST_ATTRIBUTE, new TextAttribute( plugin.getColor( CommonUIConstants.KEYWORD_1_COLOR ), null, SWT.BOLD ) );
        attributes.put( USER_ATTRIBUTE, new TextAttribute( plugin.getColor( CommonUIConstants.KEYWORD_1_COLOR ), null, SWT.BOLD ) );
    }


    // ── SELECT THE RIGHT STICKER ──────────────────────────────────────────────
    // C-3PO looks up the sticker for a given token type; if none is found he
    // falls back to the default (plain text) sticker so nothing is left unpainted.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link TextAttribute} for the given token-type key.
     * Falls back to the {@link #DEFAULT_ATTRIBUTE} entry if {@code type} is unknown.
     *
     * <p>For example — fetching the grant-value style:</p>
     * <pre>
     *   TextAttribute attr = provider.getAttribute(ACITextAttributeProvider.GRANT_VALUE);
     *   // attr carries the ADD_COLOR (green) and no bold style
     * </pre>
     *
     * @param type  one of the {@code *_ATTRIBUTE} or {@code *_VALUE} constants defined
     *              on this class
     * @return      the matching {@link TextAttribute}, or the default if {@code type} is unknown
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
