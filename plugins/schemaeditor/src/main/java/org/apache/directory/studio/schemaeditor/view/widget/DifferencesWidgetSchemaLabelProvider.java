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


import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.model.difference.AttributeTypeDifference;
import org.apache.directory.studio.schemaeditor.model.difference.ObjectClassDifference;
import org.apache.directory.studio.schemaeditor.model.difference.SchemaDifference;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;


// ── CLASS: DifferencesWidgetSchemaLabelProvider — LUKE SEES THE BIG PICTURE ──
// Standing on the edge of the Lars homestead at dusk, Luke watches the twin suns
// set over Tatooine and sees, for the first time, the full scope of what lies ahead.
// Our label provider does something similar: for every tree node it looks at the
// full context — the element type, the difference type, the label preferences, the
// optional secondary label — and assembles a complete, human-readable view of each
// schema difference, including the optional secondary identifier in brackets.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A JFace {@link LabelProvider} for the left-panel tree inside {@link DifferencesWidget}.
 * It receives tree nodes ({@link SchemaDifference}, {@link AttributeTypeDifference},
 * {@link ObjectClassDifference}, {@link Folder}) and returns the display text and icon
 * for each. The text format respects the user's label and abbreviation preferences:
 * primary label (first name / all aliases / OID), optional secondary label, and
 * abbreviation cut-off. Think of it as Luke's binary-sunset perspective: every element
 * is seen in its full context before we decide how to present it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DifferencesWidgetSchemaLabelProvider extends LabelProvider
{
    /** The preferences store */
    private IPreferenceStore store;


    // ── LUKE TAKES HIS POSITION AT THE HOMESTEAD RIDGE ───────────────────────────
    // Before Luke can appreciate the big picture, he has to be in the right place.
    // We grab the preference store here so getText() can consult the user's label
    // preferences on every call without going back to the activator each time.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new label provider, caching the plugin's preference store so
     * every {@link #getText(Object)} call can read the current label and abbreviation
     * settings without having to look them up from scratch.
     */
    public DifferencesWidgetSchemaLabelProvider()
    {
        store = Activator.getDefault().getPreferenceStore();
    }


    // ── READING THE HORIZON: COMPOSING THE DISPLAY TEXT ──────────────────────────
    // Luke does not just glance at Tatooine — he takes in the twin suns, the desert,
    // the haze on the horizon, the secondary details. Our getText() reads all the
    // display preferences (primary label, secondary label, abbreviation) and assembles
    // a full text string that gives the user as much or as little detail as they asked for.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display text for a tree node. The format varies by node type and
     * by the user's label preferences:
     * <ul>
     *   <li>{@link SchemaDifference}: returns the schema name from source or destination.</li>
     *   <li>{@link AttributeTypeDifference} / {@link ObjectClassDifference}: returns the
     *       primary label (first name, all aliases, or OID), possibly abbreviated, with
     *       an optional secondary label appended in brackets.</li>
     *   <li>{@link Folder}: returns the folder name plus a child count.</li>
     * </ul>
     *
     * <p>For example — Luke reads the whole sunset, not just one part:</p>
     * <pre>
     *   pref = FIRST_NAME, secondary = OID  →  "cn  [2.5.4.3]"
     *   pref = ALL_ALIASES, abbreviate=true  →  "commonName, cn..."
     *   Folder(ATTRIBUTE_TYPE, 12 items)     →  "Attribute Types (12)"
     * </pre>
     *
     * @param element  the tree node to label
     * @return         a formatted display string; never null, may be empty
     */
    @Override
    public String getText( Object element )
    {
        String label = ""; //$NON-NLS-1$

        int labelValue = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_LABEL );
        boolean abbreviate = store.getBoolean( PluginConstants.PREFS_SCHEMA_VIEW_ABBREVIATE );
        int abbreviateMaxLength = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_ABBREVIATE_MAX_LENGTH );
        boolean secondaryLabelDisplay = store.getBoolean( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_DISPLAY );
        int secondaryLabelValue = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL );
        boolean secondaryLabelAbbreviate = store
            .getBoolean( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_ABBREVIATE );
        int secondaryLabelAbbreviateMaxLength = store
            .getInt( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH );

        if ( element instanceof SchemaDifference )
        {
            SchemaDifference sd = ( SchemaDifference ) element;

            switch ( sd.getType() )
            {
                case ADDED:
                    return ( ( Schema ) sd.getDestination() ).getSchemaName();
                case MODIFIED:
                    return ( ( Schema ) sd.getDestination() ).getSchemaName();
                case REMOVED:
                    return ( ( Schema ) sd.getSource() ).getSchemaName();
                case IDENTICAL:
                    return ( ( Schema ) sd.getDestination() ).getSchemaName();
            }
        }
        else if ( element instanceof AttributeTypeDifference )
        {
            AttributeTypeDifference atd = ( AttributeTypeDifference ) element;

            AttributeType at = null;

            switch ( atd.getType() )
            {
                case ADDED:
                    at = ( ( AttributeType ) atd.getDestination() );
                    break;
                case MODIFIED:
                    at = ( ( AttributeType ) atd.getDestination() );
                    break;
                case REMOVED:
                    at = ( ( AttributeType ) atd.getSource() );
                    break;
                case IDENTICAL:
                    at = ( ( AttributeType ) atd.getDestination() );
                    break;
            }

            // Label
            if ( labelValue == PluginConstants.PREFS_SCHEMA_VIEW_LABEL_FIRST_NAME )
            {
                List<String> names = at.getNames();
                if ( ( names != null ) && ( names.size() > 0 ) )
                {
                    label = names.get( 0 );
                }
                else
                {
                    label = Messages.getString( "DifferencesWidgetSchemaLabelProvider.None" ); //$NON-NLS-1$
                }
            }
            else if ( labelValue == PluginConstants.PREFS_SCHEMA_VIEW_LABEL_ALL_ALIASES )
            {
                List<String> names = at.getNames();
                if ( ( names != null ) && ( names.size() > 0 ) )
                {
                    label = ViewUtils.concateAliases( names );
                }
                else
                {
                    label = Messages.getString( "DifferencesWidgetSchemaLabelProvider.None" ); //$NON-NLS-1$
                }
            }
            else if ( labelValue == PluginConstants.PREFS_SCHEMA_VIEW_LABEL_OID )
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
                    label = Messages.getString( "DifferencesWidgetSchemaLabelProvider.None" ); //$NON-NLS-1$
                }
            }

            // Abbreviate
            if ( abbreviate && ( abbreviateMaxLength < label.length() ) )
            {
                label = label.substring( 0, abbreviateMaxLength ) + "..."; //$NON-NLS-1$
            }
        }
        else if ( element instanceof ObjectClassDifference )
        {
            ObjectClassDifference ocd = ( ObjectClassDifference ) element;

            ObjectClass oc = null;

            switch ( ocd.getType() )
            {
                case ADDED:
                    oc = ( ( ObjectClass ) ocd.getDestination() );
                    break;
                case MODIFIED:
                    oc = ( ( ObjectClass ) ocd.getDestination() );
                    break;
                case REMOVED:
                    oc = ( ( ObjectClass ) ocd.getSource() );
                    break;
                case IDENTICAL:
                    oc = ( ( ObjectClass ) ocd.getDestination() );
                    break;
            }

            // Label
            if ( labelValue == PluginConstants.PREFS_SCHEMA_VIEW_LABEL_FIRST_NAME )
            {
                List<String> names = oc.getNames();
                if ( ( names != null ) && ( names.size() > 0 ) )
                {
                    label = names.get( 0 );
                }
                else
                {
                    label = Messages.getString( "DifferencesWidgetSchemaLabelProvider.None" ); //$NON-NLS-1$
                }
            }
            else if ( labelValue == PluginConstants.PREFS_SCHEMA_VIEW_LABEL_ALL_ALIASES )
            {
                List<String> names = oc.getNames();
                if ( ( names != null ) && ( names.size() > 0 ) )
                {
                    label = ViewUtils.concateAliases( names );
                }
                else
                {
                    label = Messages.getString( "DifferencesWidgetSchemaLabelProvider.None" ); //$NON-NLS-1$
                }
            }
            else if ( labelValue == PluginConstants.PREFS_SCHEMA_VIEW_LABEL_OID )
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
                    label = Messages.getString( "DifferencesWidgetSchemaLabelProvider.None" ); //$NON-NLS-1$
                }
            }

            // Abbreviate
            if ( abbreviate && ( abbreviateMaxLength < label.length() ) )
            {
                label = label.substring( 0, abbreviateMaxLength ) + "..."; //$NON-NLS-1$
            }
        }
        else if ( element instanceof Folder )
        {
            Folder folder = ( Folder ) element;

            return folder.getName() + " (" + folder.getChildren().size() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Secondary Label
        if ( secondaryLabelDisplay )
        {
            String secondaryLabel = ""; //$NON-NLS-1$
            if ( element instanceof AttributeTypeDifference )
            {
                AttributeTypeDifference atd = ( AttributeTypeDifference ) element;

                AttributeType at = null;

                switch ( atd.getType() )
                {
                    case ADDED:
                        at = ( ( AttributeType ) atd.getDestination() );
                        break;
                    case MODIFIED:
                        at = ( ( AttributeType ) atd.getDestination() );
                        break;
                    case REMOVED:
                        at = ( ( AttributeType ) atd.getSource() );
                        break;
                    case IDENTICAL:
                        at = ( ( AttributeType ) atd.getDestination() );
                        break;
                }

                if ( secondaryLabelValue == PluginConstants.PREFS_SCHEMA_VIEW_LABEL_FIRST_NAME )
                {
                    List<String> names = at.getNames();
                    if ( ( names != null ) && ( names.size() > 0 ) )
                    {
                        secondaryLabel = names.get( 0 );
                    }
                    else
                    {
                        secondaryLabel = Messages.getString( "DifferencesWidgetSchemaLabelProvider.None" ); //$NON-NLS-1$
                    }
                }
                else if ( secondaryLabelValue == PluginConstants.PREFS_SCHEMA_VIEW_LABEL_ALL_ALIASES )
                {
                    List<String> names = at.getNames();
                    if ( ( names != null ) && ( names.size() > 0 ) )
                    {
                        secondaryLabel = ViewUtils.concateAliases( names );
                    }
                    else
                    {
                        secondaryLabel = Messages.getString( "DifferencesWidgetSchemaLabelProvider.None" ); //$NON-NLS-1$
                    }
                }
                else if ( secondaryLabelValue == PluginConstants.PREFS_SCHEMA_VIEW_LABEL_OID )
                {
                    secondaryLabel = at.getOid();
                }
            }
            else if ( element instanceof ObjectClassDifference )
            {
                ObjectClassDifference ocd = ( ObjectClassDifference ) element;

                ObjectClass oc = null;

                switch ( ocd.getType() )
                {
                    case ADDED:
                        oc = ( ( ObjectClass ) ocd.getDestination() );
                        break;
                    case MODIFIED:
                        oc = ( ( ObjectClass ) ocd.getDestination() );
                        break;
                    case REMOVED:
                        oc = ( ( ObjectClass ) ocd.getSource() );
                        break;
                    case IDENTICAL:
                        oc = ( ( ObjectClass ) ocd.getDestination() );
                        break;
                }

                if ( secondaryLabelValue == PluginConstants.PREFS_SCHEMA_VIEW_LABEL_FIRST_NAME )
                {
                    List<String> names = oc.getNames();
                    if ( ( names != null ) && ( names.size() > 0 ) )
                    {
                        secondaryLabel = names.get( 0 );
                    }
                    else
                    {
                        secondaryLabel = Messages.getString( "DifferencesWidgetSchemaLabelProvider.None" ); //$NON-NLS-1$
                    }
                }
                else if ( secondaryLabelValue == PluginConstants.PREFS_SCHEMA_VIEW_LABEL_ALL_ALIASES )
                {
                    List<String> names = oc.getNames();
                    if ( ( names != null ) && ( names.size() > 0 ) )
                    {
                        secondaryLabel = ViewUtils.concateAliases( names );
                    }
                    else
                    {
                        secondaryLabel = Messages.getString( "DifferencesWidgetSchemaLabelProvider.None" ); //$NON-NLS-1$
                    }
                }
                else if ( secondaryLabelValue == PluginConstants.PREFS_SCHEMA_VIEW_LABEL_OID )
                {
                    secondaryLabel = oc.getOid();
                }
            }

            if ( secondaryLabelAbbreviate && ( secondaryLabelAbbreviateMaxLength < secondaryLabel.length() ) )
            {
                secondaryLabel = secondaryLabel.substring( 0, secondaryLabelAbbreviateMaxLength ) + "..."; //$NON-NLS-1$
            }

            label += "  [" + secondaryLabel + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        return label;
    }


    // ── COLORING WHAT LUKE SEES ON THE HORIZON ────────────────────────────────────
    // The twin suns paint the sky in different colors — red for danger, calm gold for
    // unchanged. We pick the icon that tells the user at a glance whether a schema,
    // attribute type, object class, or folder is added, modified, removed, or unchanged.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for a tree node. The image communicates the change type
     * (ADDED / MODIFIED / REMOVED / IDENTICAL) for schemas, attribute types, and
     * object classes, and a type-specific folder icon for {@link Folder} nodes.
     * Unknown element types return {@code null}.
     *
     * <p>For example — the twin suns color the horizon:</p>
     * <pre>
     *   SchemaDifference(ADDED)             →  IMG_DIFFERENCE_SCHEMA_ADD
     *   AttributeTypeDifference(MODIFIED)   →  IMG_DIFFERENCE_ATTRIBUTE_TYPE_MODIFY
     *   ObjectClassDifference(REMOVED)      →  IMG_DIFFERENCE_OBJECT_CLASS_REMOVE
     *   Folder(ATTRIBUTE_TYPE)              →  IMG_FOLDER_AT
     * </pre>
     *
     * @param element  the tree node to iconify
     * @return         the appropriate {@link Image}, or {@code null} for unknown types
     */
    @Override
    public Image getImage( Object element )
    {
        if ( element instanceof SchemaDifference )
        {
            SchemaDifference sd = ( SchemaDifference ) element;
            switch ( sd.getType() )
            {
                case ADDED:
                    return Activator.getDefault().getImage( PluginConstants.IMG_DIFFERENCE_SCHEMA_ADD );
                case MODIFIED:
                    return Activator.getDefault().getImage( PluginConstants.IMG_DIFFERENCE_SCHEMA_MODIFY );
                case REMOVED:
                    return Activator.getDefault().getImage( PluginConstants.IMG_DIFFERENCE_SCHEMA_REMOVE );
                case IDENTICAL:
                    return Activator.getDefault().getImage( PluginConstants.IMG_SCHEMA );
            }
        }
        else if ( element instanceof AttributeTypeDifference )
        {
            AttributeTypeDifference atd = ( AttributeTypeDifference ) element;
            switch ( atd.getType() )
            {
                case ADDED:
                    return Activator.getDefault().getImage( PluginConstants.IMG_DIFFERENCE_ATTRIBUTE_TYPE_ADD );
                case MODIFIED:
                    return Activator.getDefault().getImage( PluginConstants.IMG_DIFFERENCE_ATTRIBUTE_TYPE_MODIFY );
                case REMOVED:
                    return Activator.getDefault().getImage( PluginConstants.IMG_DIFFERENCE_ATTRIBUTE_TYPE_REMOVE );
                case IDENTICAL:
                    return Activator.getDefault().getImage( PluginConstants.IMG_ATTRIBUTE_TYPE );
            }
        }
        else if ( element instanceof ObjectClassDifference )
        {
            ObjectClassDifference ocd = ( ObjectClassDifference ) element;
            switch ( ocd.getType() )
            {
                case ADDED:
                    return Activator.getDefault().getImage( PluginConstants.IMG_DIFFERENCE_OBJECT_CLASS_ADD );
                case MODIFIED:
                    return Activator.getDefault().getImage( PluginConstants.IMG_DIFFERENCE_OBJECT_CLASS_MODIFY );
                case REMOVED:
                    return Activator.getDefault().getImage( PluginConstants.IMG_DIFFERENCE_OBJECT_CLASS_REMOVE );
                case IDENTICAL:
                    return Activator.getDefault().getImage( PluginConstants.IMG_OBJECT_CLASS );
            }
        }
        else if ( element instanceof Folder )
        {
            Folder folder = ( Folder ) element;

            switch ( folder.getType() )
            {
                case ATTRIBUTE_TYPE:
                    return Activator.getDefault().getImage( PluginConstants.IMG_FOLDER_AT );
                case OBJECT_CLASS:
                    return Activator.getDefault().getImage( PluginConstants.IMG_FOLDER_OC );
                case NONE:
                    return Activator.getDefault().getImage( PluginConstants.IMG_FOLDER );
                default:
                    break;
            }
        }

        // Default
        return null;
    }
}
