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
package org.apache.directory.studio.schemaeditor.view.views;


import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;


// ── CLASS: SearchViewLabelProvider — Vader's Suit Presenting the Find ────────
// After R2-D2 finds what he's looking for in the Death Star's computer, you still
// need a way to present that information meaningfully to the humans in the room.
// Vader's suit is the perfect metaphor for a label provider: it wraps something
// powerful and raw underneath in a polished, readable presentation — the breathing,
// the voice modulator, the black armor all signal "this is the thing, and here is
// how to understand it at a glance." Each search result is raw schema data; this
// class wraps it in the label and icon that make it immediately comprehensible.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Supplies the display text and icon for each row in the Search View's results table.
 * After {@link SearchView} runs a search and hands a list of matching schema objects to
 * the table viewer, this label provider decides what label and image each row gets.
 * It reads user preferences for primary label format (first name, all aliases, or OID),
 * optional abbreviation, optional secondary label, and optional schema name display.
 * Think of it as Vader's suit: wrapping each raw schema object in exactly the
 * presentation that tells you what it is at a glance.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchViewLabelProvider extends LabelProvider
{
    /** The preferences store */
    private IPreferenceStore store;


    // ── The Suit Is Assembled ─────────────────────────────────────────────────
    // Before Vader can stride into the room and present himself, the suit must be
    // assembled and its systems initialized — life support, voice modulator, targeting
    // sensors. Here we initialize by loading the preference store so we know which
    // display format the user has configured.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new label provider and hooks it up to the plugin's preference store.
     * The preference store is where label format settings (first name vs. OID,
     * abbreviation lengths, secondary label options) are persisted between sessions.
     *
     * <p>For example — assembling the suit:</p>
     * <pre>
     *   new SearchViewLabelProvider()
     *   → store = Activator.getDefault().getPreferenceStore()
     *   → ready to render any AttributeType or ObjectClass row
     * </pre>
     */
    public SearchViewLabelProvider()
    {
        store = Activator.getDefault().getPreferenceStore();
    }


    // ── The Suit Announces Each Find ─────────────────────────────────────────
    // Vader doesn't just walk in silently — every detail of the suit announces
    // his presence: the breathing, the cape, the name. This method announces
    // each search result: what to call it (name, aliases, or OID), how long
    // the label can be (abbreviation), and what extra context to append
    // (secondary label, schema name) — all based on the user's preferences.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Computes the display string for a search result row.
     * We read preferences for primary label format, abbreviation, secondary label
     * format, and whether to show the schema name, then build the complete string.
     * If the schema object has no names at all, we fall back to "(None)".
     *
     * <p>For example — different suit configurations:</p>
     * <pre>
     *   prefs: FIRST_NAME, no abbreviate, secondary=OID, schema=true
     *   result: AttributeType "cn" (schema "system", OID "2.5.4.3")
     *   → "cn  [2.5.4.3] from schema \"system\""
     * </pre>
     *
     * @param element  an {@link AttributeType} or {@link ObjectClass} from the search results
     * @return         the formatted display string; empty string for unrecognized element types
     */
    @Override
    public String getText( Object element )
    {
        String label = ""; //$NON-NLS-1$

        int labelValue = store.getInt( PluginConstants.PREFS_SEARCH_VIEW_LABEL );
        boolean abbreviate = store.getBoolean( PluginConstants.PREFS_SEARCH_VIEW_ABBREVIATE );
        int abbreviateMaxLength = store.getInt( PluginConstants.PREFS_SEARCH_VIEW_ABBREVIATE_MAX_LENGTH );
        boolean secondaryLabelDisplay = store.getBoolean( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_DISPLAY );
        int secondaryLabelValue = store.getInt( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL );
        boolean secondaryLabelAbbreviate = store
            .getBoolean( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_ABBREVIATE );
        int secondaryLabelAbbreviateMaxLength = store
            .getInt( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH );
        boolean schemaLabelDisplay = store.getBoolean( PluginConstants.PREFS_SEARCH_VIEW_SCHEMA_LABEL_DISPLAY );

        if ( element instanceof AttributeType )
        {
            AttributeType at = ( AttributeType ) element;

            // Label
            if ( labelValue == PluginConstants.PREFS_SEARCH_VIEW_LABEL_FIRST_NAME )
            {
                List<String> names = at.getNames();
                if ( ( names != null ) && ( names.size() > 0 ) )
                {
                    label = names.get( 0 );
                }
                else
                {
                    label = Messages.getString( "SearchViewLabelProvider.None" ); //$NON-NLS-1$
                }
            }
            else if ( labelValue == PluginConstants.PREFS_SEARCH_VIEW_LABEL_ALL_ALIASES )
            {
                List<String> names = at.getNames();
                if ( ( names != null ) && ( names.size() > 0 ) )
                {
                    label = ViewUtils.concateAliases( names );
                }
                else
                {
                    label = Messages.getString( "SearchViewLabelProvider.None" ); //$NON-NLS-1$
                }
            }
            else if ( labelValue == PluginConstants.PREFS_SEARCH_VIEW_LABEL_OID )
            {
                label = at.getOid();
            }
            else
            // Default
            {
                List<String> names = at.getNames();
                if ( ( names != null ) && ( names.size() > 0 ) )
                {
                    label = names.get( 0 );
                }
                else
                {
                    label = Messages.getString( "SearchViewLabelProvider.None" ); //$NON-NLS-1$
                }
            }

            // Abbreviate
            if ( abbreviate && ( abbreviateMaxLength < label.length() ) )
            {
                label = label.substring( 0, abbreviateMaxLength ) + "..."; //$NON-NLS-1$
            }
        }
        else if ( element instanceof ObjectClass )
        {
            ObjectClass oc = ( ObjectClass ) element;

            // Label
            if ( labelValue == PluginConstants.PREFS_SEARCH_VIEW_LABEL_FIRST_NAME )
            {
                List<String> names = oc.getNames();
                if ( ( names != null ) && ( names.size() > 0 ) )
                {
                    label = names.get( 0 );
                }
                else
                {
                    label = Messages.getString( "SearchViewLabelProvider.None" ); //$NON-NLS-1$
                }
            }
            else if ( labelValue == PluginConstants.PREFS_SEARCH_VIEW_LABEL_ALL_ALIASES )
            {
                List<String> names = oc.getNames();
                if ( ( names != null ) && ( names.size() > 0 ) )
                {
                    label = ViewUtils.concateAliases( names );
                }
                else
                {
                    label = Messages.getString( "SearchViewLabelProvider.None" ); //$NON-NLS-1$
                }
            }
            else if ( labelValue == PluginConstants.PREFS_SEARCH_VIEW_LABEL_OID )
            {
                label = oc.getOid();
            }
            else
            // Default
            {
                List<String> names = oc.getNames();
                if ( ( names != null ) && ( names.size() > 0 ) )
                {
                    label = names.get( 0 );
                }
                else
                {
                    label = Messages.getString( "SearchViewLabelProvider.None" ); //$NON-NLS-1$
                }
            }

            // Abbreviate
            if ( abbreviate && ( abbreviateMaxLength < label.length() ) )
            {
                label = label.substring( 0, abbreviateMaxLength ) + "..."; //$NON-NLS-1$
            }
        }

        // Secondary Label
        if ( secondaryLabelDisplay )
        {
            String secondaryLabel = ""; //$NON-NLS-1$
            if ( element instanceof AttributeType )
            {
                AttributeType at = ( AttributeType ) element;

                if ( secondaryLabelValue == PluginConstants.PREFS_SEARCH_VIEW_LABEL_FIRST_NAME )
                {
                    List<String> names = at.getNames();
                    if ( ( names != null ) && ( names.size() > 0 ) )
                    {
                        secondaryLabel = names.get( 0 );
                    }
                    else
                    {
                        secondaryLabel = Messages.getString( "SearchViewLabelProvider.None" ); //$NON-NLS-1$
                    }
                }
                else if ( secondaryLabelValue == PluginConstants.PREFS_SEARCH_VIEW_LABEL_ALL_ALIASES )
                {
                    List<String> names = at.getNames();
                    if ( ( names != null ) && ( names.size() > 0 ) )
                    {
                        secondaryLabel = ViewUtils.concateAliases( names );
                    }
                    else
                    {
                        secondaryLabel = Messages.getString( "SearchViewLabelProvider.None" ); //$NON-NLS-1$
                    }
                }
                else if ( secondaryLabelValue == PluginConstants.PREFS_SEARCH_VIEW_LABEL_OID )
                {
                    secondaryLabel = at.getOid();
                }
            }
            else if ( element instanceof ObjectClass )
            {
                ObjectClass oc = ( ObjectClass ) element;

                if ( secondaryLabelValue == PluginConstants.PREFS_SEARCH_VIEW_LABEL_FIRST_NAME )
                {
                    List<String> names = oc.getNames();
                    if ( ( names != null ) && ( names.size() > 0 ) )
                    {
                        secondaryLabel = names.get( 0 );
                    }
                    else
                    {
                        secondaryLabel = Messages.getString( "SearchViewLabelProvider.None" ); //$NON-NLS-1$
                    }
                }
                else if ( secondaryLabelValue == PluginConstants.PREFS_SEARCH_VIEW_LABEL_ALL_ALIASES )
                {
                    List<String> names = oc.getNames();
                    if ( ( names != null ) && ( names.size() > 0 ) )
                    {
                        secondaryLabel = ViewUtils.concateAliases( names );
                    }
                    else
                    {
                        secondaryLabel = Messages.getString( "SearchViewLabelProvider.None" ); //$NON-NLS-1$
                    }
                }
                else if ( secondaryLabelValue == PluginConstants.PREFS_SEARCH_VIEW_LABEL_OID )
                {
                    secondaryLabel = oc.getOid();
                }
            }

            if ( secondaryLabelAbbreviate && ( secondaryLabelAbbreviateMaxLength < secondaryLabel.length() ) )
            {
                secondaryLabel = secondaryLabel.substring( 0, secondaryLabelAbbreviateMaxLength ) + "..."; //$NON-NLS-1$
            }

            label += " [" + secondaryLabel + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Schema Label
        if ( schemaLabelDisplay )
        {
            if ( element instanceof SchemaObject )
            {
                SchemaObject object = ( SchemaObject ) element;

                label += " " + Messages.getString( "SearchViewLabelProvider.FromSchema" ) + " \"" + object.getSchemaName() + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
        }

        return label;
    }


    // ── The Suit Carries the Right Insignia ──────────────────────────────────
    // Even in silhouette, you know what Vader is by the shape — the distinctive
    // helmet. Each schema type has its own visual signature: the attribute type
    // icon vs. the object class icon. The icon tells you instantly what kind of
    // schema object you're looking at, before you even read the label.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon to display for this search result row.
     * We use a distinct icon for attribute types vs. object classes so users can
     * tell the type of each result at a glance, without reading the name.
     *
     * <p>For example — insignia at a glance:</p>
     * <pre>
     *   AttributeType element → attribute type icon (AT badge)
     *   ObjectClass element   → object class icon  (OC badge)
     *   Anything else         → null (no icon)
     * </pre>
     *
     * @param element  an {@link AttributeType} or {@link ObjectClass} search result
     * @return         the appropriate {@link Image}, or {@code null} for unrecognized types
     */
    @Override
    public Image getImage( Object element )
    {
        if ( element instanceof AttributeType )
        {
            return Activator.getDefault().getImage( PluginConstants.IMG_ATTRIBUTE_TYPE );
        }
        else if ( element instanceof ObjectClass )
        {
            return Activator.getDefault().getImage( PluginConstants.IMG_OBJECT_CLASS );
        }

        // Default
        return null;
    }
}
