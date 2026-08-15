/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl.widgets.composites;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.swt.widgets.Composite;
import org.apache.directory.studio.openldap.config.acl.OpenLdapAclValueWithContext;
import org.apache.directory.studio.openldap.config.acl.model.AclWhatClause;
import org.apache.directory.studio.openldap.config.acl.model.AclWhatClauseAttributes;
import org.apache.directory.studio.openldap.config.acl.widgets.AttributesWidget;


// ── CLASS: WhatClauseAttributesComposite — TARKIN MANAGING ATTRIBUTE MANIFEST
// Grand Moff Tarkin opens the attribute manifest panel: a two-column composite
// that hosts the AttributesWidget. The constructor also guarantees that the
// context's what-clause has an AclWhatClauseAttributes child, creating one if
// needed. This composite is created inline by the WhatClauseWidget when the
// "Attributes" checkbox is checked.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A clause composite for the "Attributes" what-clause. Embeds an
 * {@link AttributesWidget} that lets the user manage the list of attribute
 * names/exclusions and optional Val filter controls.
 *
 * <p>Think of this class as Grand Moff Tarkin's attribute manifest panel —
 * it ensures an {@link AclWhatClauseAttributes} sub-clause exists in the model
 * and presents the {@link AttributesWidget} for editing.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class WhatClauseAttributesComposite extends AbstractClauseComposite
{
    /** The attributes widget */
    private AttributesWidget attributesWidget;

    // ── Constructing the Attributes Composite ─────────────────────────────────
    // Tarkin opens the attribute manifest panel: stores the context, ensures
    // an AclWhatClauseAttributes sub-clause exists in the model, and immediately
    // creates the AttributesWidget inside a two-column sub-composite.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new attributes composite. Ensures the model's what-clause has an
     * {@link AclWhatClauseAttributes} (creating one if absent), then builds the
     * {@link AttributesWidget} inside a two-column sub-composite.
     *
     * <p>For example — the what-clause widget creating the attributes panel:</p>
     * <pre>
     *   attributesClauseComposite =
     *       new WhatClauseAttributesComposite(visualEditor, container, context);
     * </pre>
     *
     * @param visualEditorComposite      The visual editor composite (for layout refresh).
     * @param attributesSubComposite     The parent sub-composite in which to build the widget.
     * @param context                    The ACL context.
     */
    public WhatClauseAttributesComposite( Composite visualEditorComposite, Composite attributesSubComposite, OpenLdapAclValueWithContext context )
    {
        super( context, visualEditorComposite );
        Composite whatComposite = BaseWidgetUtils.createColumnContainer( attributesSubComposite, 2, 1 );

        // Create the Attributes clause if it does not already exist
        AclWhatClause aclWhatClause = context.getAclItem().getWhatClause();

        if ( aclWhatClause.getAttributesClause() == null )
        {
            aclWhatClause.setAttributesClause( new AclWhatClauseAttributes() );
        }

        // The Attribute widget
        BaseWidgetUtils.createLabel( whatComposite, "", 1 );
        attributesWidget = new AttributesWidget();
        attributesWidget.createWidget( whatComposite, connection, aclWhatClause.getAttributesClause() );
    }
}
