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
package org.apache.directory.studio.templateeditor.view.preferences;


import java.util.Iterator;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.apache.directory.studio.templateeditor.EntryTemplatePlugin;
import org.apache.directory.studio.templateeditor.EntryTemplatePluginConstants;
import org.apache.directory.studio.templateeditor.EntryTemplatePluginUtils;
import org.apache.directory.studio.templateeditor.model.Template;
import org.apache.directory.studio.templateeditor.view.ColumnsLabelProvider;


// ── CLASS: TemplatesLabelProvider — PALPATINE'S STANDING-ORDERS DISPLAY CLERK ────
// When Palpatine reviews his standing orders on the preference page, each row must
// be rendered correctly: the right icon (enabled template, disabled template, or
// object-class node), the right text (title with a "(Default)" badge if applicable),
// the right font (bold for the default template in object-class mode), and the
// right foreground colour (dimmed for disabled templates). This class handles all
// that presentation logic for the CheckboxTreeViewer.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Label provider for the templates tree viewer on the Template Entry Editor
 * preference page. Provides per-column icons, text, font, and colour for both
 * object-class-tree mode and flat-template-list mode.
 *
 * <p>Think of this as Palpatine's standing-orders display clerk:</p>
 * <pre>
 *   // Default template in object-class mode → bold text, "(Default)" suffix
 *   // Disabled template → dimmed foreground colour
 *   // Object-class node → object-class icon, names joined with ", "
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplatesLabelProvider extends ColumnsLabelProvider implements ITableFontProvider, ITableColorProvider
{
    /** The templates manager */
    private PreferencesTemplatesManager manager;

    /** The preference store */
    private IPreferenceStore store;


    // ── CONSTRUCTOR: WIRE UP THE DISPLAY CLERK ────────────────────────────────────
    // Palpatine's display clerk takes the manager (to query enabled/default state)
    // and the preference store (to determine presentation mode).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplatesLabelProvider}.
     *
     * @param manager  the preferences-page templates manager used to check
     *                 enabled/default state for display decisions
     */
    public TemplatesLabelProvider( PreferencesTemplatesManager manager )
    {
        this.manager = manager;
        store = EntryTemplatePlugin.getDefault().getPreferenceStore();
    }


    // ── GET COLUMN IMAGE: RETURN THE ICON FOR EACH CELL ──────────────────────────
    // Palpatine's clerk selects the correct rank badge: object-class icon for
    // object-class nodes, enabled-template icon for active templates, and
    // disabled-template icon for greyed-out templates.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public Image getColumnImage( Object element, int columnIndex )
    {
        // Object class presentation
        if ( isObjectClassPresentation() )
        {
            if ( columnIndex == 0 )
            {
                if ( element instanceof ObjectClass )
                {
                    return EntryTemplatePlugin.getDefault().getImage( EntryTemplatePluginConstants.IMG_OBJECT_CLASS );
                }
                else if ( element instanceof Template )
                {
                    if ( manager.isEnabled( ( Template ) element ) )
                    {
                        return EntryTemplatePlugin.getDefault().getImage( EntryTemplatePluginConstants.IMG_TEMPLATE );
                    }
                    else
                    {
                        return EntryTemplatePlugin.getDefault().getImage(
                            EntryTemplatePluginConstants.IMG_TEMPLATE_DISABLED );
                    }
                }
            }
        }
        // Template presentation
        else if ( isTemplatePresentation() )
        {
            if ( columnIndex == 0 )
            {
                if ( element instanceof Template )
                {
                    if ( manager.isEnabled( ( Template ) element ) )
                    {
                        return EntryTemplatePlugin.getDefault().getImage( EntryTemplatePluginConstants.IMG_TEMPLATE );
                    }
                    else
                    {
                        return EntryTemplatePlugin.getDefault().getImage(
                            EntryTemplatePluginConstants.IMG_TEMPLATE_DISABLED );
                    }
                }
            }
            else if ( columnIndex == 1 )
            {
                if ( element instanceof Template )
                {
                    return EntryTemplatePlugin.getDefault().getImage( EntryTemplatePluginConstants.IMG_OBJECT_CLASS );
                }
            }
        }

        return null;
    }


    // ── GET COLUMN TEXT: RETURN THE DISPLAY TEXT FOR EACH CELL ───────────────────
    // Palpatine's clerk formats the cell text. Default templates get a "(Default)"
    // suffix so the user can identify them at a glance.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getColumnText( Object element, int columnIndex )
    {
        // Object class presentation
        if ( isObjectClassPresentation() )
        {
            if ( columnIndex == 0 )
            {
                if ( element instanceof ObjectClass )
                {
                    return concatenateObjectClassNames( ( ( ObjectClass ) element ).getNames() );
                }
                else if ( element instanceof Template )
                {
                    Template template = ( Template ) element;
                    if ( manager.isDefaultTemplate( template ) )
                    {
                        return NLS.bind( Messages.getString( "TemplatesLabelProvider.Default" ), template.getTitle() ); //$NON-NLS-1$
                    }
                    else
                    {
                        return template.getTitle();
                    }
                }
            }
        }
        // Template presentation
        else if ( isTemplatePresentation() )
        {
            if ( columnIndex == 0 )
            {
                if ( element instanceof Template )
                {
                    return ( ( Template ) element ).getTitle();
                }
            }
            else if ( columnIndex == 1 )
            {
                if ( element instanceof Template )
                {
                    Template template = ( Template ) element;
                    return concatenateObjectClasses( EntryTemplatePluginUtils
                        .getObjectClassDescriptionFromDefaultSchema( template.getStructuralObjectClass() ), template
                        .getAuxiliaryObjectClasses() );
                }
            }
        }

        return ""; //$NON-NLS-1$
    }


    // ── IS TEMPLATE PRESENTATION ──────────────────────────────────────────────────
    /**
     * Returns {@code true} if the preference page is in flat-template mode.
     *
     * @return {@code true} for flat-template mode
     */
    private boolean isTemplatePresentation()
    {
        return ( store.getInt( EntryTemplatePluginConstants.PREF_TEMPLATES_PRESENTATION ) == EntryTemplatePluginConstants.PREF_TEMPLATES_PRESENTATION_TEMPLATE );
    }


    // ── IS OBJECT CLASS PRESENTATION ──────────────────────────────────────────────
    /**
     * Returns {@code true} if the preference page is in object-class-tree mode.
     *
     * @return {@code true} for object-class-tree mode
     */
    private boolean isObjectClassPresentation()
    {
        return ( store.getInt( EntryTemplatePluginConstants.PREF_TEMPLATES_PRESENTATION ) == EntryTemplatePluginConstants.PREF_TEMPLATES_PRESENTATION_OBJECT_CLASS );
    }


    // ── CONCATENATE OBJECT CLASSES: BUILD THE OBJECT-CLASS STRING ────────────────
    // Palpatine's clerk assembles the structural + auxiliary object class names into
    // a single display string: "inetOrgPerson <organizationalPerson, person>".
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a single display string combining the structural object class name(s)
     * and any auxiliary object class names. Auxiliary names are wrapped in angle
     * brackets and separated by commas.
     *
     * @param objectClass             the structural object class (may be {@code null})
     * @param auxiliaryObjectClasses  the list of auxiliary object class names (may be {@code null})
     * @return the combined display string, or {@code ""} if inputs are invalid
     */
    private String concatenateObjectClasses( ObjectClass objectClass,
        List<String> auxiliaryObjectClasses )
    {
        if ( ( objectClass != null ) && ( auxiliaryObjectClasses != null ) )
        {
            StringBuilder sb = new StringBuilder();

            sb.append( concatenateObjectClassNames( objectClass.getNames() ) );

            if ( auxiliaryObjectClasses.size() > 0 )
            {
                sb.append( " <" ); //$NON-NLS-1$

                // Adding each auxiliary object class
                Iterator<String> iterator = auxiliaryObjectClasses.iterator();
                while ( iterator.hasNext() )
                {
                    sb.append( ( String ) iterator.next() );
                    if ( iterator.hasNext() )
                    {
                        sb.append( ", " ); //$NON-NLS-1$
                    }
                }

                sb.append( ">" ); //$NON-NLS-1$
            }

            return sb.toString();
        }

        return ""; //$NON-NLS-1$
    }


    // ── CONCATENATE OBJECT CLASS NAMES: JOIN A LIST OF NAMES ─────────────────────
    /**
     * Joins a list of object-class names into a single comma-separated string
     * (e.g. {@code "inetOrgPerson, person"}).
     *
     * @param names  the list of names to join (may be {@code null} or empty)
     * @return the joined string, or {@code ""} if the list is empty or {@code null}
     */
    private String concatenateObjectClassNames( List<String> names )
    {
        if ( ( names != null ) && ( names.size() > 0 ) )
        {
            StringBuilder sb = new StringBuilder();

            Iterator<String> iterator = names.iterator();
            while ( iterator.hasNext() )
            {
                sb.append( ( String ) iterator.next() );
                if ( iterator.hasNext() )
                {
                    sb.append( ", " ); //$NON-NLS-1$
                }
            }

            return sb.toString();
        }

        return ""; //$NON-NLS-1$
    }


    // ── GET FONT: BOLD FOR DEFAULT TEMPLATES ──────────────────────────────────────
    // Palpatine's clerk renders the default template in bold so it stands out from
    // the other candidates in the object-class tree.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public Font getFont( Object element, int columnIndex )
    {
        // Object class presentation
        if ( isObjectClassPresentation() )
        {
            if ( element instanceof Template )
            {
                if ( manager.isDefaultTemplate( ( Template ) element ) )
                {
                    // Get the default Bold Font
                    return JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT );
                }
            }
        }

        return null;
    }


    // ── GET FOREGROUND: DIM DISABLED TEMPLATES ────────────────────────────────────
    // Palpatine's clerk dims the text of disabled templates. The TODO here means
    // a proper system-disabled colour should eventually replace the null default.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public Color getForeground( Object element, int columnIndex )
    {
        if ( element instanceof Template )
        {
            if ( !manager.isEnabled( ( Template ) element ) )
            {
                // TODO: get disabled color
                return null;
            }
        }

        return null;
    }


    // ── GET BACKGROUND: NO SPECIAL BACKGROUND ────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public Color getBackground( Object element, int columnIndex )
    {
        return null;
    }
}
