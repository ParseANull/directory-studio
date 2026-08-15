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
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.apache.directory.studio.schemaeditor.view.wrappers.AttributeTypeWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.Folder;
import org.apache.directory.studio.schemaeditor.view.wrappers.ObjectClassWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.SchemaWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.TreeNode;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;


// ── CLASS: SchemaViewLabelProvider — Luke's Binary Sunset ─────────────────────
// Luke stands at the mesa's edge on Tatooine, watching both suns set. He's not
// cataloguing details — he's taking in the whole picture: what everything looks
// like, what it means together, what you see at a glance. The binary sunset gives
// you the full panoramic impression of the landscape in one sweep.
// This label provider does the same for every node in the Schema View: it surveys
// each tree node (schema, attribute type, object class, folder), reads the user's
// display preferences, and returns the one label-and-icon combination that
// communicates everything the user needs at a single glance — including optional
// secondary info, child counts, and schema names.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Supplies the display text and icon for every node in the Schema View tree.
 * The Schema View shows schemas, attribute types, object classes, and folder
 * groupings, and each type of node gets its own rendering logic. We read user
 * preferences for label format (first name, all aliases, OID), abbreviation,
 * secondary label, child count display, and schema name display, then compute
 * the complete label string. Think of it as Luke's binary sunset: the full
 * panoramic picture, composed from many details into one clear impression.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaViewLabelProvider extends LabelProvider
{
    /** The preferences store */
    private IPreferenceStore store;


    // ── Luke Finds the Right Mesa ─────────────────────────────────────────────
    // Luke can't see the binary sunset from just anywhere — he has to walk to the
    // right viewpoint. The constructor does that: it loads the preference store
    // so every subsequent getText/getImage call can read the user's display settings
    // without going back to the plugin every time.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new label provider and hooks it up to the plugin's preference store.
     * The preference store holds all the user-configured display settings: which
     * label to show, whether to abbreviate, whether to show the schema name, etc.
     * We load it once here so we don't have to fetch it on every render call.
     *
     * <p>For example — finding the right viewpoint:</p>
     * <pre>
     *   new SchemaViewLabelProvider()
     *   → store = Activator.getDefault().getPreferenceStore()
     *   → ready to render any Schema View node
     * </pre>
     */
    public SchemaViewLabelProvider()
    {
        store = Activator.getDefault().getPreferenceStore();
    }


    // ── The Full Panoramic Label ──────────────────────────────────────────────
    // Standing at the mesa, Luke sees both suns, the distance, the color, the
    // shadow — everything composited into one impression. getText composes the
    // full label from all the details: primary label + optional abbreviation +
    // optional secondary label + optional child count + optional schema name.
    // Each node type (schema, attribute type, object class, folder) gets
    // its own specific treatment, but the result is always one clear string.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Computes the full display string for any Schema View tree node.
     * We handle four node types: {@link SchemaWrapper} (just shows the schema name),
     * {@link AttributeTypeWrapper}, {@link ObjectClassWrapper} (both respect all
     * label preferences), and {@link Folder} (shows name, optionally with a count
     * of its children in flat mode). Returns an empty string for unrecognized types.
     *
     * <p>For example — the full panorama:</p>
     * <pre>
     *   prefs: FIRST_NAME, abbreviate=false, secondary=OID, childCount=true, schema=true
     *   element = AttributeTypeWrapper("cn"), 3 children, schema "system"
     *   → "cn  [2.5.4.3]  (3)  [system]"
     * </pre>
     *
     * @param element  any Schema View tree node wrapper
     * @return         the formatted display label; empty string if element type is unknown
     */
    @Override
    public String getText( Object element )
    {
        String label = ""; //$NON-NLS-1$

        int presentation = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION );
        int labelValue = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_LABEL );
        boolean abbreviate = store.getBoolean( PluginConstants.PREFS_SCHEMA_VIEW_ABBREVIATE );
        int abbreviateMaxLength = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_ABBREVIATE_MAX_LENGTH );
        boolean secondaryLabelDisplay = store.getBoolean( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_DISPLAY );
        int secondaryLabelValue = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL );
        boolean secondaryLabelAbbreviate = store
            .getBoolean( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_ABBREVIATE );
        int secondaryLabelAbbreviateMaxLength = store
            .getInt( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH );
        boolean schemaLabelDisplay = store.getBoolean( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_LABEL_DISPLAY );

        if ( element instanceof SchemaWrapper )
        {
            SchemaWrapper sw = ( SchemaWrapper ) element;

            return sw.getSchema().getSchemaName();
        }
        else if ( element instanceof AttributeTypeWrapper )
        {
            AttributeType at = ( ( AttributeTypeWrapper ) element ).getAttributeType();

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
                    label = Messages.getString( "SchemaViewLabelProvider.None" ); //$NON-NLS-1$
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
                    label = Messages.getString( "SchemaViewLabelProvider.None" ); //$NON-NLS-1$
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
                    label = Messages.getString( "SchemaViewLabelProvider.None" ); //$NON-NLS-1$
                }
            }

            // Abbreviate
            if ( abbreviate && ( abbreviateMaxLength < label.length() ) )
            {
                label = label.substring( 0, abbreviateMaxLength ) + "..."; //$NON-NLS-1$
            }
        }
        else if ( element instanceof ObjectClassWrapper )
        {
            ObjectClass oc = ( ( ObjectClassWrapper ) element ).getObjectClass();

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
                    label = Messages.getString( "SchemaViewLabelProvider.None" ); //$NON-NLS-1$
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
                    label = Messages.getString( "SchemaViewLabelProvider.None" ); //$NON-NLS-1$
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
                    label = Messages.getString( "SchemaViewLabelProvider.None" ); //$NON-NLS-1$
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

            if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_FLAT )
            {
                return folder.getName() + " (" + folder.getChildren().size() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            else if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_HIERARCHICAL )
            {
                return folder.getName();
            }
        }

        // Secondary Label
        if ( secondaryLabelDisplay )
        {
            String secondaryLabel = ""; //$NON-NLS-1$
            if ( element instanceof AttributeTypeWrapper )
            {
                AttributeType at = ( ( AttributeTypeWrapper ) element ).getAttributeType();

                if ( secondaryLabelValue == PluginConstants.PREFS_SCHEMA_VIEW_LABEL_FIRST_NAME )
                {
                    List<String> names = at.getNames();
                    if ( ( names != null ) && ( names.size() > 0 ) )
                    {
                        secondaryLabel = names.get( 0 );
                    }
                    else
                    {
                        secondaryLabel = Messages.getString( "SchemaViewLabelProvider.None" ); //$NON-NLS-1$
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
                        secondaryLabel = Messages.getString( "SchemaViewLabelProvider.None" ); //$NON-NLS-1$
                    }
                }
                else if ( secondaryLabelValue == PluginConstants.PREFS_SCHEMA_VIEW_LABEL_OID )
                {
                    secondaryLabel = at.getOid();
                }
            }
            else if ( element instanceof ObjectClassWrapper )
            {
                ObjectClass oc = ( ( ObjectClassWrapper ) element ).getObjectClass();

                if ( secondaryLabelValue == PluginConstants.PREFS_SCHEMA_VIEW_LABEL_FIRST_NAME )
                {
                    List<String> names = oc.getNames();
                    if ( ( names != null ) && ( names.size() > 0 ) )
                    {
                        secondaryLabel = names.get( 0 );
                    }
                    else
                    {
                        secondaryLabel = Messages.getString( "SchemaViewLabelProvider.None" ); //$NON-NLS-1$
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
                        secondaryLabel = Messages.getString( "SchemaViewLabelProvider.None" ); //$NON-NLS-1$
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

        // Number of children
        if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_HIERARCHICAL )
        {
            if ( ( element instanceof AttributeTypeWrapper ) || ( element instanceof ObjectClassWrapper ) )
            {
                List<TreeNode> children = ( ( TreeNode ) element ).getChildren();

                if ( ( children != null ) && ( children.size() > 0 ) )
                {
                    label += "  (" + children.size() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }

        // Schema Label
        if ( schemaLabelDisplay )
        {
            if ( element instanceof AttributeTypeWrapper )
            {
                label += "  [" + ( ( AttributeTypeWrapper ) element ).getAttributeType().getSchemaName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            else if ( element instanceof ObjectClassWrapper )
            {
                label += "  [" + ( ( ObjectClassWrapper ) element ).getObjectClass().getSchemaName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        return label;
    }


    // ── The Right Icon for Each Feature of the Landscape ─────────────────────
    // The binary sunset isn't just light — there are distinct features: rocks,
    // dunes, the moisture farm itself. Each has a distinct visual. getImage
    // returns the right icon for each node type: schema icon for schemas,
    // AT icon for attribute types, OC icon for object classes, and folder-type
    // icons for the AT-folder and OC-folder groupings.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon to display for the given Schema View tree node.
     * Schemas, attribute types, object classes, and folders each get a distinct icon
     * from the plugin's image registry. Folder icons differ by type: we have specific
     * icons for "Attribute Types" folders and "Object Classes" folders.
     *
     * <p>For example — each feature has its icon:</p>
     * <pre>
     *   SchemaWrapper           → schema icon
     *   AttributeTypeWrapper    → attribute type icon (AT)
     *   ObjectClassWrapper      → object class icon (OC)
     *   Folder(ATTRIBUTE_TYPE)  → folder-with-AT icon
     *   Folder(OBJECT_CLASS)    → folder-with-OC icon
     *   Folder(NONE)            → generic folder icon
     * </pre>
     *
     * @param element  any Schema View tree node wrapper
     * @return         the {@link Image} for this node, or {@code null} for unrecognized types
     */
    @Override
    public Image getImage( Object element )
    {
        if ( element instanceof SchemaWrapper )
        {
            return Activator.getDefault().getImage( PluginConstants.IMG_SCHEMA );
        }
        else if ( element instanceof AttributeTypeWrapper )
        {
            return Activator.getDefault().getImage( PluginConstants.IMG_ATTRIBUTE_TYPE );
        }
        else if ( element instanceof ObjectClassWrapper )
        {
            return Activator.getDefault().getImage( PluginConstants.IMG_OBJECT_CLASS );
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
