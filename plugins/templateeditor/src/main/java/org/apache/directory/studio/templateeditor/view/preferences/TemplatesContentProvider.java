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


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.apache.directory.studio.templateeditor.EntryTemplatePlugin;
import org.apache.directory.studio.templateeditor.EntryTemplatePluginConstants;
import org.apache.directory.studio.templateeditor.EntryTemplatePluginUtils;
import org.apache.directory.studio.templateeditor.TemplatesManagerListener;
import org.apache.directory.studio.templateeditor.model.Template;


// ── CLASS: TemplatesContentProvider — PALPATINE ORGANISING HIS STANDING ORDERS ───
// When Palpatine opens the preferences page, he needs his standing orders (templates)
// presented in one of two arrangements: either grouped by object class (a tree where
// each object-class node expands to show its templates) or as a flat alphabetical
// list of all templates. This content provider supplies the correct structure for
// the {@link CheckboxTreeViewer} based on the active presentation preference, and
// also listens for template-added/removed events so the tree stays current.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * JFace {@link ITreeContentProvider} for the templates tree viewer on the
 * Template Entry Editor preference page. Supports two presentation modes:
 * <ul>
 *   <li><b>Object-class mode</b> — templates are grouped under their structural
 *       object class as tree nodes.</li>
 *   <li><b>Template mode</b> — templates appear as a flat sorted list with no
 *       hierarchy.</li>
 * </ul>
 * Also implements {@link TemplatesManagerListener} to keep the viewer in sync when
 * templates are added or removed via Import/Remove.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplatesContentProvider implements ITreeContentProvider, TemplatesManagerListener
{
    /** The associated page */
    private TemplateEntryEditorPreferencePage page;

    /** The templates manager */
    private PreferencesTemplatesManager manager;

    /** The preference store */
    private IPreferenceStore store;

    /** A flag indicating if the content provider has already been initialized */
    private boolean initialized = false;

    /** The list of templates */
    private List<Template> templates;

    /** The map where templates are organized by object classes */
    private MultiValuedMap<ObjectClass, Template> objectClassesTemplatesMap;


    // ── CONSTRUCTOR: WIRE UP THE CONTENT PROVIDER ─────────────────────────────────
    // Palpatine's content-provider clerk registers itself with the manager so it
    // hears about add/remove events, and initialises the two data structures
    // (flat list and object-class map) ready for population on first access.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplatesContentProvider} and registers it as a listener
     * on the given {@link PreferencesTemplatesManager}.
     *
     * @param page     the preference page that owns the viewer — called back to refresh
     *                 the tree when templates change
     * @param manager  the preferences-page templates manager supplying template data
     */
    public TemplatesContentProvider( TemplateEntryEditorPreferencePage page, PreferencesTemplatesManager manager )
    {
        this.page = page;
        this.manager = manager;
        manager.addListener( this );
        store = EntryTemplatePlugin.getDefault().getPreferenceStore();
        templates = new ArrayList<Template>();
        objectClassesTemplatesMap = new ArrayListValuedHashMap<>();
    }


    // ── GET CHILDREN: RETURN THE CHILD ELEMENTS ───────────────────────────────────
    // Palpatine's clerk returns the children of a tree node. In object-class mode,
    // each object-class node's children are the templates registered under it.
    // In template mode there are no children (flat list).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public Object[] getChildren( Object parentElement )
    {
        // Object class presentation
        if ( isObjectClassPresentation() )
        {
            if ( parentElement instanceof ObjectClass )
            {
                List<Template> templates = ( List<Template> ) objectClassesTemplatesMap
                    .get( ( ObjectClass ) parentElement );

                if ( templates != null )
                {
                    return templates.toArray();
                }
            }
        }
        // Template presentation
        else if ( isTemplatePresentation() )
        {
            // Elements have no children
            return new Object[0];
        }

        return new Object[0];
    }


    // ── GET PARENT: RETURN THE PARENT ELEMENT ────────────────────────────────────
    // Palpatine's clerk has no parent tracking — elements at the root have no parent.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public Object getParent( Object element )
    {
        // Elements have no parent, as they have no children
        return null;
    }


    // ── HAS CHILDREN: CHECK FOR CHILD ELEMENTS ───────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean hasChildren( Object element )
    {
        // Object class presentation
        if ( isObjectClassPresentation() )
        {
            if ( element instanceof ObjectClass )
            {
                return objectClassesTemplatesMap.containsKey( ( ObjectClass ) element );
            }
        }
        // Template presentation
        else if ( isTemplatePresentation() )
        {
            // Elements have no children
            return false;
        }

        return false;
    }


    // ── GET ELEMENTS: RETURN THE ROOT ELEMENTS ────────────────────────────────────
    // Palpatine's clerk populates the data structures on first call and then
    // returns the correct root elements: object-class nodes or a flat template list.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public Object[] getElements( Object inputElement )
    {
        if ( !initialized )
        {
            // Looping on the templates
            for ( Template template : manager.getTemplates() )
            {
                // Adding the template
                templates.add( template );

                // Adding the structural object class
                objectClassesTemplatesMap.put( EntryTemplatePluginUtils
                    .getObjectClassDescriptionFromDefaultSchema( template.getStructuralObjectClass() ), template );
            }

            // Setting the initialized flag to true
            initialized = true;
        }

        // Object class presentation
        if ( isObjectClassPresentation() )
        {
            // Returning the object classes
            return objectClassesTemplatesMap.keySet().toArray();

        }
        // Template presentation
        else if ( isTemplatePresentation() )
        {
            // Returning the templates
            return templates.toArray();
        }

        return new Object[0];
    }


    // ── DISPOSE: DEREGISTER AS A LISTENER ─────────────────────────────────────────
    // Palpatine's clerk signs off — removes itself from the manager's listener list
    // to prevent stale references.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void dispose()
    {
        EntryTemplatePlugin.getDefault().getTemplatesManager().removeListener( this );
    }


    // ── INPUT CHANGED: NOTHING TO DO ──────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
        // Nothing to do.
    }


    // ── IS TEMPLATE PRESENTATION: CHECK PRESENTATION MODE ────────────────────────
    /**
     * Returns {@code true} if the preference page is currently in flat-template
     * presentation mode.
     *
     * @return {@code true} for flat-template mode; {@code false} otherwise
     */
    public boolean isTemplatePresentation()
    {
        return ( store.getInt( EntryTemplatePluginConstants.PREF_TEMPLATES_PRESENTATION ) == EntryTemplatePluginConstants.PREF_TEMPLATES_PRESENTATION_TEMPLATE );
    }


    // ── IS OBJECT CLASS PRESENTATION: CHECK PRESENTATION MODE ────────────────────
    /**
     * Returns {@code true} if the preference page is currently in object-class-tree
     * presentation mode.
     *
     * @return {@code true} for object-class-tree mode; {@code false} otherwise
     */
    public boolean isObjectClassPresentation()
    {
        return ( store.getInt( EntryTemplatePluginConstants.PREF_TEMPLATES_PRESENTATION ) == EntryTemplatePluginConstants.PREF_TEMPLATES_PRESENTATION_OBJECT_CLASS );
    }


    // ── TEMPLATE ADDED: UPDATE THE DATA STRUCTURES AND REFRESH ───────────────────
    // Palpatine's clerk files the new standing order in both the flat list and the
    // object-class map, then refreshes the tree viewer.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void templateAdded( Template template )
    {
        // Adding the template
        templates.add( template );

        // Adding the structural object class
        objectClassesTemplatesMap.put( EntryTemplatePluginUtils.getObjectClassDescriptionFromDefaultSchema( template
            .getStructuralObjectClass() ), template );

        // Refreshing the viewer
        page.refreshViewer();
    }


    // ── TEMPLATE REMOVED: UPDATE THE DATA STRUCTURES AND REFRESH ─────────────────
    // Palpatine's clerk removes the standing order from both data structures and
    // refreshes the tree viewer.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void templateRemoved( Template template )
    {
        // Removing the structural object class
        objectClassesTemplatesMap.removeMapping( EntryTemplatePluginUtils.getObjectClassDescriptionFromDefaultSchema( template
            .getStructuralObjectClass() ), template );

        // Removing the template
        templates.remove( template );

        // Refreshing the viewer
        page.refreshViewer();
    }


    // ── TEMPLATE DISABLED: NOTHING TO DO HERE ────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void templateDisabled( Template template )
    {
        // Nothing to do
    }


    // ── TEMPLATE ENABLED: NOTHING TO DO HERE ─────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void templateEnabled( Template template )
    {
        // Nothing to do
    }
}
