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

package org.apache.directory.studio.ldapbrowser.common;


import java.util.ArrayList;
import java.util.Collection;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.schema.AttributeValueEditorRelation;
import org.apache.directory.studio.ldapbrowser.core.model.schema.ObjectClassIconPair;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SyntaxValueEditorRelation;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.apache.directory.studio.valueeditors.ValueEditorManager.ValueEditorExtension;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;


// ── CLASS: BrowserCommonPreferencesInitializer — MON MOTHMA'S STANDING ORDERS
// Before the Battle of Yavin, Mon Mothma gathers her commanders and issues
// standing orders: default patrol routes, standard color codes for ships,
// acceptable search limits, and which pilot gets which assignment. These orders
// take effect immediately and persist until someone explicitly changes them.
// This initializer is those standing orders — it populates Eclipse's preference
// store with sensible defaults the first time the plugin starts, before any
// user has touched the settings screen.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Populates the Eclipse preference store with default values for every
 * setting in the ldapbrowser.common plugin. Eclipse calls this class once
 * at startup (before showing any preference pages) via the
 * {@code org.eclipse.core.runtime.preferences} extension point.
 * Think of this class as Mon Mothma issuing Alliance-wide standing orders —
 * sensible defaults that hold until a user overrides them.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserCommonPreferencesInitializer extends AbstractPreferenceInitializer
{
    // ── MON MOTHMA READS THE STANDING ORDERS ──────────────────────────────────
    // Mon Mothma steps up to the podium in the Rebel briefing room and reads
    // every standing order aloud: patrols start at 1000 entries per sweep, font
    // styles for different types of officers, browser folding limits, icon
    // assignments for known species — the full set of defaults that make the
    // Alliance function before anyone has a chance to customize anything.
    // This method does the same for every preference key in the plugin.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets all default preference values for the ldapbrowser.common plugin.
     * Eclipse calls this automatically before any preference page is opened,
     * ensuring the store has a sane fallback for every key even if the user
     * has never visited a settings screen.
     * Covers: search count/time limits, attribute fonts and colors, value editor
     * mappings, browser display options, entry editor options, and text format
     * settings.
     *
     * <p>For example — Mon Mothma briefing the Alliance on default operations:</p>
     * <pre>
     *   store.setDefault( COUNT_LIMIT, 1000 );      // "No sweep exceeds 1000"
     *   store.setDefault( BROWSER_ENABLE_FOLDING, true ); // "Fold large groups"
     *   // ... forty more standing orders follow
     * </pre>
     */
    @Override
    public void initializeDefaultPreferences()
    {

        IPreferenceStore store = BrowserCommonActivator.getDefault().getPreferenceStore();

        // Common
        store.setDefault( BrowserCommonConstants.PREFERENCE_COUNT_LIMIT, 1000 );
        store.setDefault( BrowserCommonConstants.PREFERENCE_TIME_LIMIT, 0 );

        // Fonts
        FontData[] fontData = Display.getDefault().getSystemFont().getFontData();
        FontData fontDataNormal = new FontData( fontData[0].getName(), fontData[0].getHeight(), SWT.NORMAL );
        FontData fontDataItalic = new FontData( fontData[0].getName(), fontData[0].getHeight(), SWT.ITALIC );
        FontData fontDataBold = new FontData( fontData[0].getName(), fontData[0].getHeight(), SWT.BOLD );
        FontData fontDataBoldItalic = new FontData( fontData[0].getName(), fontData[0].getHeight(), SWT.BOLD
            | SWT.ITALIC );
        // Attributes colors and fonts
        store.setDefault( BrowserCommonConstants.PREFERENCE_OBJECTCLASS_COLOR, IPreferenceStore.STRING_DEFAULT_DEFAULT );
        PreferenceConverter.setDefault( store, BrowserCommonConstants.PREFERENCE_OBJECTCLASS_FONT, fontDataBoldItalic );
        store.setDefault( BrowserCommonConstants.PREFERENCE_MUSTATTRIBUTE_COLOR, IPreferenceStore.STRING_DEFAULT_DEFAULT );
        PreferenceConverter.setDefault( store, BrowserCommonConstants.PREFERENCE_MUSTATTRIBUTE_FONT, fontDataBold );
        store.setDefault( BrowserCommonConstants.PREFERENCE_MAYATTRIBUTE_COLOR, IPreferenceStore.STRING_DEFAULT_DEFAULT );
        PreferenceConverter.setDefault( store, BrowserCommonConstants.PREFERENCE_MAYATTRIBUTE_FONT, fontDataNormal );
        store.setDefault( BrowserCommonConstants.PREFERENCE_MAYATTRIBUTE_COLOR, IPreferenceStore.STRING_DEFAULT_DEFAULT );
        PreferenceConverter.setDefault( store, BrowserCommonConstants.PREFERENCE_OPERATIONALATTRIBUTE_FONT,
            fontDataItalic );

        // Attributes
        store.setDefault( BrowserCommonConstants.PREFERENCE_SHOW_RAW_VALUES, false );

        // Value Editors
        Collection<AttributeValueEditorRelation> avprs = new ArrayList<AttributeValueEditorRelation>();
        Collection<SyntaxValueEditorRelation> svprs = new ArrayList<SyntaxValueEditorRelation>();
        Collection<ValueEditorExtension> valueEditorExtensions = ValueEditorManager.getValueEditorExtensions();

        for ( ValueEditorExtension vee : valueEditorExtensions )
        {
            for ( String attributeType : vee.attributeTypes )
            {
                AttributeValueEditorRelation aver = new AttributeValueEditorRelation( attributeType, vee.className );
                avprs.add( aver );
            }

            for ( String syntaxOid : vee.syntaxOids )
            {
                SyntaxValueEditorRelation sver = new SyntaxValueEditorRelation( syntaxOid, vee.className );
                svprs.add( sver );
            }
        }

        BrowserCommonActivator.getDefault().getValueEditorsPreferences().setDefaultAttributeValueEditorRelations(
            avprs.toArray( new AttributeValueEditorRelation[0] ) );
        BrowserCommonActivator.getDefault().getValueEditorsPreferences().setDefaultSyntaxValueEditorRelations(
            svprs.toArray( new SyntaxValueEditorRelation[0] ) );

        // Browser
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_EXPAND_BASE_ENTRIES, false );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_ENABLE_FOLDING, true );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_FOLDING_SIZE, 100 );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_ENTRY_LABEL, BrowserCommonConstants.SHOW_RDN );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_ENTRY_ABBREVIATE, true );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_ENTRY_ABBREVIATE_MAX_LENGTH, 50 );
        store
            .setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_SEARCH_RESULT_LABEL, BrowserCommonConstants.SHOW_DN );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_SEARCH_RESULT_ABBREVIATE, true );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_SEARCH_RESULT_ABBREVIATE_MAX_LENGTH, 50 );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_QUICK_SEARCH, true );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_DIT, true );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_SEARCHES, true );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_BOOKMARKS, true );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_DIRECTORY_META_ENTRIES, false );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_SORT_BY, BrowserCoreConstants.SORT_BY_RDN_VALUE );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_SORT_ORDER,
            BrowserCoreConstants.SORT_ORDER_ASCENDING );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_SORT_SEARCHES_ORDER,
            BrowserCoreConstants.SORT_ORDER_ASCENDING );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_SORT_BOOKMARKS_ORDER,
            BrowserCoreConstants.SORT_ORDER_ASCENDING );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_SORT_LIMIT, 10000 );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_LEAF_ENTRIES_FIRST, true );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_CONTAINER_ENTRIES_FIRST, false );
        store.setDefault( BrowserCommonConstants.PREFERENCE_BROWSER_META_ENTRIES_LAST, true );

        // default icons
        ObjectClassIconPair[] objectClassIcons = new ObjectClassIconPair[]
            {
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.PERSON_OC_OID, SchemaConstants.ORGANIZATIONAL_PERSON_OC_OID,
                        SchemaConstants.INET_ORG_PERSON_OC_OID }, BrowserCommonConstants.IMG_ENTRY_PERSON ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.POSIX_ACCOUNT_OC_OID }, BrowserCommonConstants.IMG_ENTRY_PERSON ),

                new ObjectClassIconPair( new String[]
                    { SchemaConstants.ORGANIZATION_OC_OID }, BrowserCommonConstants.IMG_ENTRY_ORG ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.ORGANIZATIONAL_UNIT_OC_OID }, BrowserCommonConstants.IMG_ENTRY_ORG ),

                new ObjectClassIconPair( new String[]
                    { SchemaConstants.COUNTRY_OC_OID }, BrowserCommonConstants.IMG_ENTRY_DC ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.LOCALITY_OC_OID }, BrowserCommonConstants.IMG_ENTRY_DC ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.DC_OBJECT_OC_OID }, BrowserCommonConstants.IMG_ENTRY_DC ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.DOMAIN_OC_OID }, BrowserCommonConstants.IMG_ENTRY_DC ),

                new ObjectClassIconPair( new String[]
                    { SchemaConstants.GROUP_OF_NAMES_OC_OID }, BrowserCommonConstants.IMG_ENTRY_GROUP ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.GROUP_OF_UNIQUE_NAMES_OC_OID }, BrowserCommonConstants.IMG_ENTRY_GROUP ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.POSIX_GROUP_OC_OID }, BrowserCommonConstants.IMG_ENTRY_GROUP ),

                new ObjectClassIconPair( new String[]
                    { SchemaConstants.SUBENTRY_OC_OID }, BrowserCommonConstants.IMG_BROWSER_SCHEMABROWSEREDITOR ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.REFERRAL_OC_OID }, BrowserCommonConstants.IMG_ENTRY_REF ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.ALIAS_OC_OID }, BrowserCommonConstants.IMG_ENTRY_ALIAS ),

                new ObjectClassIconPair( new String[]
                    { SchemaConstants.META_SCHEMA_OC_OID }, BrowserCommonConstants.IMG_BROWSER_SCHEMABROWSEREDITOR ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.META_OBJECT_CLASS_OC_OID }, BrowserCommonConstants.IMG_OCD ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.META_ATTRIBUTE_TYPE_OC_OID }, BrowserCommonConstants.IMG_ATD ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.META_MATCHING_RULE_OC_OID }, BrowserCommonConstants.IMG_MRD ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.META_SYNTAX_CHECKER_OC_OID }, BrowserCommonConstants.IMG_SYNTAX_CHECKER ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.META_SYNTAX_OC_OID }, BrowserCommonConstants.IMG_LSD ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.META_COMPARATOR_OC_OID }, BrowserCommonConstants.IMG_COMPARATOR ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.META_NORMALIZER_OC_OID }, BrowserCommonConstants.IMG_NORMALIZER ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.META_DIT_CONTENT_RULE_OC_OID }, BrowserCommonConstants.IMG_DIT_CONTENT_RULE ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.META_DIT_STRUCTURE_RULE_OC_OID }, BrowserCommonConstants.IMG_DIT_STRUCTURE_RULE ),
                new ObjectClassIconPair( new String[]
                    { SchemaConstants.META_NAME_FORM_OC_OID }, BrowserCommonConstants.IMG_NAME_FORM ),

                // Active Directory
                new ObjectClassIconPair( new String[]
                    { "1.2.840.113556.1.5.9" }, BrowserCommonConstants.IMG_ENTRY_PERSON ), // User //$NON-NLS-1$
                new ObjectClassIconPair( new String[]
                    { "1.2.840.113556.1.5.8" }, BrowserCommonConstants.IMG_ENTRY_GROUP ), // Group //$NON-NLS-1$
                new ObjectClassIconPair( new String[]
                    { "1.2.840.113556.1.3.23" }, BrowserCommonConstants.IMG_ENTRY_ORG ), // Container  //$NON-NLS-1$
                new ObjectClassIconPair( new String[]
                    { "1.2.840.113556.1.5.66", "1.2.840.113556.1.5.67" }, BrowserCommonConstants.IMG_ENTRY_DC ), // Domain, DomainDNS //$NON-NLS-1$ //$NON-NLS-2$
                new ObjectClassIconPair( new String[]
                    { "1.2.840.113556.1.3.13" }, BrowserCommonConstants.IMG_OCD ), // ClassSchema //$NON-NLS-1$
                new ObjectClassIconPair( new String[]
                    { "1.2.840.113556.1.3.14" }, BrowserCommonConstants.IMG_ATD ), // AttributeSchema //$NON-NLS-1$
            };
        BrowserCorePlugin.getDefault().getCorePreferences().setDefaultObjectClassIcons( objectClassIcons );

        // Entry Editor
        store.setDefault( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_ENABLE_FOLDING, true );
        store.setDefault( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_FOLDING_THRESHOLD, 10 );
        store.setDefault( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTO_EXPAND_FOLDED_ATTRIBUTES, false );
        store.setDefault( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTOSAVE_SINGLE_TAB, true );
        store.setDefault( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTOSAVE_MULTI_TAB, false );
        store.setDefault( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_OBJECTCLASS_AND_MUST_ATTRIBUTES_FIRST, true );
        store.setDefault( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_OPERATIONAL_ATTRIBUTES_LAST, true );
        store.setDefault( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_DEFAULT_SORT_BY,
            BrowserCoreConstants.SORT_BY_ATTRIBUTE_DESCRIPTION );
        store.setDefault( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_DEFAULT_SORT_ORDER,
            BrowserCoreConstants.SORT_ORDER_ASCENDING );

        // Text Format
        store.setDefault( BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_ATTRIBUTEDELIMITER, "\t" ); //$NON-NLS-1$
        store.setDefault( BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_VALUEDELIMITER, "|" ); //$NON-NLS-1$
        store.setDefault( BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_QUOTECHARACTER, "\"" ); //$NON-NLS-1$
        store.setDefault( BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_LINESEPARATOR,
            BrowserCoreConstants.LINE_SEPARATOR );
        store.setDefault( BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_BINARYENCODING,
            BrowserCoreConstants.BINARYENCODING_IGNORE );
    }

}
