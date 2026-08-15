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


import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;

import org.apache.directory.studio.templateeditor.model.Template;


// ── CLASS: TemplatesCheckStateListener — PALPATINE'S ENABLEMENT ORDER RELAY ───────
// When Palpatine ticks or unticks a template in his standing-orders checklist, the
// change needs to propagate immediately to the preferences manager. If he checks an
// entire object-class node, all templates under that node must be enabled; if he
// unchecks it, all must be disabled. This listener plays that relay role: it
// receives the checkbox event and routes it to the manager's enable/disable methods.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Responds to check-state changes in the templates tree viewer on the preference
 * page. When the user checks or unchecks a template or an object-class node, this
 * listener calls {@link PreferencesTemplatesManager#enableTemplate(Template)} or
 * {@link PreferencesTemplatesManager#disableTemplate(Template)} as appropriate.
 *
 * <p>Think of this as Palpatine's enablement-order relay:</p>
 * <pre>
 *   user checks "inetOrgPerson" node → all child templates get enabled
 *   user unchecks a single template → that template gets disabled
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplatesCheckStateListener implements ICheckStateListener
{
    /** The associated content provider */
    private TemplatesContentProvider contentProvider;

    /** The templates manager */
    private PreferencesTemplatesManager manager;


    // ── CONSTRUCTOR: WIRE UP THE RELAY ────────────────────────────────────────────
    // Palpatine's relay is given the content provider (to determine presentation
    // mode) and the manager (to execute enable/disable).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplatesCheckStateListener}.
     *
     * @param contentProvider  the tree viewer's content provider — used to determine
     *                         the current presentation mode and to retrieve child templates
     * @param manager          the preferences-page templates manager that processes
     *                         enable/disable operations
     */
    public TemplatesCheckStateListener( TemplatesContentProvider contentProvider, PreferencesTemplatesManager manager )
    {
        this.contentProvider = contentProvider;
        this.manager = manager;
    }


    // ── CHECK STATE CHANGED: RELAY THE TICK OR UNTICK ────────────────────────────
    // Palpatine's relay reads the checkbox event: if the element is a template,
    // enable or disable it directly; if it is an object-class node, apply the same
    // change to all child templates under that node.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void checkStateChanged( CheckStateChangedEvent event )
    {
        // Getting the element of the event
        Object element = event.getElement();

        // Object class presentation
        if ( contentProvider.isObjectClassPresentation() )
        {
            if ( element instanceof Template )
            {
                setTemplateEnabled( ( Template ) element, event.getChecked() );
            }
            else if ( element instanceof ObjectClass )
            {
                // Getting the children of the node
                Object[] children = contentProvider.getChildren( element );
                if ( children != null )
                {
                    for ( Object child : children )
                    {
                        setTemplateEnabled( ( Template ) child, event.getChecked() );
                    }
                }
            }
        }
        // Template presentation
        else if ( contentProvider.isTemplatePresentation() )
        {
            setTemplateEnabled( ( Template ) element, event.getChecked() );
        }
    }


    // ── SET TEMPLATE ENABLED: ROUTE TO THE MANAGER ───────────────────────────────
    // Palpatine's relay routes the enabled/disabled instruction to the manager.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the given template via the manager.
     *
     * @param template  the template to enable or disable
     * @param enabled   {@code true} to enable; {@code false} to disable
     */
    private void setTemplateEnabled( Template template, boolean enabled )
    {
        if ( enabled )
        {
            manager.enableTemplate( template );
        }
        else
        {
            manager.disableTemplate( template );
        }
    }
}
