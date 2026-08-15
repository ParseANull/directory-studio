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
import org.apache.directory.studio.schemaeditor.view.wrappers.ObjectClassWrapper;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


// ── CLASS: HierarchyViewLabelProvider — Luke's Binary Sunset on Tatooine ─────
// Luke stands at the moisture farm's edge, watching both suns set across the
// desert. He's not gathering data — he's taking in the whole picture at once:
// the horizon, the twin light sources, the dust in the air, what it all means
// together. That's what a label provider does: for every node in the hierarchy
// tree, it surveys the object's names, OIDs, and user preferences all at once
// and renders the single label that tells the user exactly what they're looking
// at — with optional secondary info in brackets, like catching both suns in one
// glance.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Supplies the display text and icon for every node shown in the Hierarchy View tree.
 * The Hierarchy View lets you see how an object class or attribute type fits into
 * its parent/child inheritance chain — think of it as a family tree for schema types.
 * This class reads user preferences (which label format? abbreviate? show secondary
 * info?) and computes exactly the right string and image for each node. Think of it
 * as Luke at the binary sunset: taking in the full picture and deciding what it means.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class HierarchyViewLabelProvider extends LabelProvider
{
    /** The preferences store */
    private IPreferenceStore store;

    /** The TreeViewer */
    private TreeViewer viewer;


    // ── Luke Finds the Right Vantage Point ──────────────────────────────────
    // Before Luke can see the sunset, he has to walk out to the cliff edge and
    // know which direction to face. The constructor sets up our vantage point:
    // we grab the preference store (so we know what display format the user wants)
    // and hold a reference to the viewer (so we can tell which node is the
    // "selected" root and highlight it differently).
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new label provider wired to the given tree viewer.
     * We need the viewer so we can check whether a given node is the "root input"
     * of the current hierarchy display — that node gets a highlighted icon instead
     * of the plain one, so users can instantly spot which type they originally asked
     * to see the hierarchy for.
     *
     * <p>For example — Luke finds his spot:</p>
     * <pre>
     *   new HierarchyViewLabelProvider(viewer)
     *   → grabs the preference store and remembers the viewer
     *   → now ready to render any node with correct label + icon
     * </pre>
     *
     * @param viewer  the tree viewer this label provider will serve; we use it to detect
     *                whether a node is the root input so we can give it a special icon
     */
    public HierarchyViewLabelProvider( TreeViewer viewer )
    {
        store = Activator.getDefault().getPreferenceStore();
        this.viewer = viewer;
    }


    // ── Luke Reads the Whole Horizon in One Sweep ───────────────────────────
    // The binary sunset isn't just "two suns going down." Luke sees the light,
    // the shadows, the color shift, and processes it all into one impression.
    // Here we read the user's label preference (first name? all aliases? OID?),
    // the abbreviation setting, and the secondary-label setting, then compute
    // the complete display string — primary label with optional secondary
    // info appended in brackets.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Computes the display string for a tree node in the Hierarchy View.
     * We check the user's preferences for label format (first name, all aliases,
     * or OID), whether to truncate long labels, and whether to show a secondary
     * label in brackets. The result is everything the user asked to see about this
     * node, rendered as one string.
     *
     * <p>For example — reading both suns at once:</p>
     * <pre>
     *   node = AttributeTypeWrapper for "cn" (commonName)
     *   prefs: label=FIRST_NAME, abbreviate=false, secondary=OID
     *   → getText() returns "cn  [2.5.4.3]"
     * </pre>
     *
     * @param obj  the tree node — either an {@link AttributeTypeWrapper} or {@link ObjectClassWrapper}
     * @return     the formatted display string for this node, never null
     */
    @Override
    public String getText( Object obj )
    {
        String label = ""; //$NON-NLS-1$

        int labelValue = store.getInt( PluginConstants.PREFS_HIERARCHY_VIEW_LABEL );
        boolean abbreviate = store.getBoolean( PluginConstants.PREFS_HIERARCHY_VIEW_ABBREVIATE );
        int abbreviateMaxLength = store.getInt( PluginConstants.PREFS_HIERARCHY_VIEW_ABBREVIATE_MAX_LENGTH );
        boolean secondaryLabelDisplay = store.getBoolean( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_DISPLAY );
        int secondaryLabelValue = store.getInt( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL );
        boolean secondaryLabelAbbreviate = store
            .getBoolean( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_ABBREVIATE );
        int secondaryLabelAbbreviateMaxLength = store
            .getInt( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH );

        if ( obj instanceof AttributeTypeWrapper )
        {
            AttributeType at = ( ( AttributeTypeWrapper ) obj ).getAttributeType();

            // Label
            if ( labelValue == PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_FIRST_NAME )
            {
                List<String> names = at.getNames();
                if ( ( names != null ) && ( names.size() > 0 ) )
                {
                    label = names.get( 0 );
                }
                else
                {
                    label = Messages.getString( "HierarchyViewLabelProvider.None" ); //$NON-NLS-1$
                }
            }
            else if ( labelValue == PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_ALL_ALIASES )
            {
                List<String> names = at.getNames();
                if ( ( names != null ) && ( names.size() > 0 ) )
                {
                    label = ViewUtils.concateAliases( names );
                }
                else
                {
                    label = Messages.getString( "HierarchyViewLabelProvider.None" ); //$NON-NLS-1$
                }
            }
            else if ( labelValue == PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_OID )
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
                    label = Messages.getString( "HierarchyViewLabelProvider.None" ); //$NON-NLS-1$
                }
            }

            // Abbreviate
            if ( abbreviate && ( abbreviateMaxLength < label.length() ) )
            {
                label = label.substring( 0, abbreviateMaxLength ) + "..."; //$NON-NLS-1$
            }
        }
        else if ( obj instanceof ObjectClassWrapper )
        {
            ObjectClass oc = ( ( ObjectClassWrapper ) obj ).getObjectClass();

            // Label
            if ( labelValue == PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_FIRST_NAME )
            {
                List<String> names = oc.getNames();
                if ( ( names != null ) && ( names.size() > 0 ) )
                {
                    label = names.get( 0 );
                }
                else
                {
                    label = Messages.getString( "HierarchyViewLabelProvider.None" ); //$NON-NLS-1$
                }
            }
            else if ( labelValue == PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_ALL_ALIASES )
            {
                List<String> names = oc.getNames();
                if ( ( names != null ) && ( names.size() > 0 ) )
                {
                    label = ViewUtils.concateAliases( names );
                }
                else
                {
                    label = Messages.getString( "HierarchyViewLabelProvider.None" ); //$NON-NLS-1$
                }
            }
            else if ( labelValue == PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_OID )
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
                    label = Messages.getString( "HierarchyViewLabelProvider.None" ); //$NON-NLS-1$
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
            if ( obj instanceof AttributeTypeWrapper )
            {
                AttributeType at = ( ( AttributeTypeWrapper ) obj ).getAttributeType();

                if ( secondaryLabelValue == PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_FIRST_NAME )
                {
                    List<String> names = at.getNames();
                    if ( ( names != null ) && ( names.size() > 0 ) )
                    {
                        secondaryLabel = names.get( 0 );
                    }
                    else
                    {
                        secondaryLabel = Messages.getString( "HierarchyViewLabelProvider.None" ); //$NON-NLS-1$
                    }
                }
                else if ( secondaryLabelValue == PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_ALL_ALIASES )
                {
                    List<String> names = at.getNames();
                    if ( ( names != null ) && ( names.size() > 0 ) )
                    {
                        secondaryLabel = ViewUtils.concateAliases( names );
                    }
                    else
                    {
                        secondaryLabel = Messages.getString( "HierarchyViewLabelProvider.None" ); //$NON-NLS-1$
                    }
                }
                else if ( secondaryLabelValue == PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_OID )
                {
                    secondaryLabel = at.getOid();
                }
            }
            else if ( obj instanceof ObjectClassWrapper )
            {
                ObjectClass oc = ( ( ObjectClassWrapper ) obj ).getObjectClass();

                if ( secondaryLabelValue == PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_FIRST_NAME )
                {
                    List<String> names = oc.getNames();
                    if ( ( names != null ) && ( names.size() > 0 ) )
                    {
                        secondaryLabel = names.get( 0 );
                    }
                    else
                    {
                        secondaryLabel = Messages.getString( "HierarchyViewLabelProvider.None" ); //$NON-NLS-1$
                    }
                }
                else if ( secondaryLabelValue == PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_ALL_ALIASES )
                {
                    List<String> names = oc.getNames();
                    if ( ( names != null ) && ( names.size() > 0 ) )
                    {
                        secondaryLabel = ViewUtils.concateAliases( names );
                    }
                    else
                    {
                        secondaryLabel = Messages.getString( "HierarchyViewLabelProvider.None" ); //$NON-NLS-1$
                    }
                }
                else if ( secondaryLabelValue == PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_OID )
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


    // ── One Sun Brighter Than the Other — the Focal Point ───────────────────
    // Luke's eye is naturally drawn to the sun that's lower, larger, the one
    // that anchors the whole view. In our tree, the "selected" node — the root
    // input that the user asked to explore — deserves a special icon to anchor
    // the viewer's eye, just like that focal sun.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon to display for this hierarchy tree node.
     * The node that is the current root input (the type the user selected to explore)
     * gets a "selected/highlighted" icon so it's visually distinct from its ancestors
     * and descendants. Every other node gets a plain attribute-type or object-class icon.
     *
     * <p>For example — spotting the focal node:</p>
     * <pre>
     *   viewer.getInput() == "cn" (AttributeType)
     *   node "cn"      → IMG_ATTRIBUTE_TYPE_HIERARCHY_SELECTED  (the bright sun)
     *   node "name"    → IMG_ATTRIBUTE_TYPE                     (regular ancestor)
     * </pre>
     *
     * @param obj  the tree node — {@link AttributeTypeWrapper} or {@link ObjectClassWrapper}
     * @return     the {@link Image} for this node; falls back to a warning icon if the
     *             type isn't something we recognize
     */
    @Override
    public Image getImage( Object obj )
    {
        if ( obj instanceof AttributeTypeWrapper )
        {
            if ( ( ( AttributeTypeWrapper ) obj ).getAttributeType().equals( viewer.getInput() ) )
            {
                return Activator.getDefault().getImage( PluginConstants.IMG_ATTRIBUTE_TYPE_HIERARCHY_SELECTED );
            }
            else
            {
                return Activator.getDefault().getImage( PluginConstants.IMG_ATTRIBUTE_TYPE );
            }
        }
        else if ( obj instanceof ObjectClassWrapper )
        {

            if ( ( ( ObjectClassWrapper ) obj ).getObjectClass().equals( viewer.getInput() ) )
            {
                return Activator.getDefault().getImage( PluginConstants.IMG_OBJECT_CLASS_HIERARCHY_SELECTED );
            }
            else
            {
                return Activator.getDefault().getImage( PluginConstants.IMG_OBJECT_CLASS );
            }
        }

        // Default
        return PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_WARN_TSK );
    }
}
