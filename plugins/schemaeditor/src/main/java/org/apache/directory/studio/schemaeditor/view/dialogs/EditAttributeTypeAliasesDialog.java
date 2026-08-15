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
package org.apache.directory.studio.schemaeditor.view.dialogs;


import java.util.List;

import org.apache.directory.studio.schemaeditor.Activator;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: EditAttributeTypeAliasesDialog — CLONE TROOPERS EXECUTE ORDER 66 ───
// When Palpatine issues Order 66, every clone trooper carries out the same action —
// but each squad's target is different. The 501st goes for the Jedi Temple;
// Commander Cody goes for Obi-Wan on Utapau. Same order, different regiment, different target.
// This class is the "attribute type regiment" — it extends the general aliases-editing
// machinery from {@link AbstractAliasesDialog} and fills in the two details that are
// specific to attribute types: the right error message and the right registry check.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Concrete dialog for editing the aliases list of an LDAP attribute type.
 * An attribute type's aliases are the human-readable names it goes by — "cn" and "commonName"
 * are both aliases for the same attribute type. This dialog inherits the full add/edit/remove
 * UI from {@link AbstractAliasesDialog} and simply plugs in the attribute-type-specific
 * duplicate check and error message.
 * Think of this class as the clone regiment assigned to the attribute-type registry:
 * it executes the same mission as its sibling for object classes, just against a different target.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditAttributeTypeAliasesDialog extends AbstractAliasesDialog
{
    // ── The 501st Receives Its Briefing ──────────────────────────────────────
    // Commander Appo briefs the 501st with the current status of the Jedi Temple —
    // who's inside, what aliases they're using. The squad copies this roster into
    // their own records before storming in, so they know exactly who was there at the start.
    // We pass the initial alias list up to the parent, which snapshots it as the baseline.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the dialog with the attribute type's current aliases pre-loaded.
     * We pass everything up to {@link AbstractAliasesDialog}, which snapshots the list
     * so it can distinguish between aliases the user changed and aliases that were already there.
     *
     * @param aliases  the attribute type's current alias list; may be empty but not null
     */
    public EditAttributeTypeAliasesDialog( List<String> aliases )
    {
        super( aliases );
    }


    // ── The 501st Labels Its Briefing Room Door ───────────────────────────────
    // "Jedi Temple — Operation: Knightfall" is stencilled on the door so there's
    // no confusion about which squad is in which room. We override the parent's
    // shell title so the dialog window clearly says "Edit Attribute Type Aliases."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Overrides the shell title set by the parent to read "Edit Attribute Type Aliases"
     * instead of the generic "Edit Alias" label.
     * We call {@code super.configureShell(newShell)} first so the parent's logic runs,
     * then overwrite the text.
     *
     * @param newShell  the freshly created Shell that JFace hands us to configure
     */
    @Override
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "EditAttributeTypeAliasesDialog.EditAliases" ) ); //$NON-NLS-1$
    }


    // ── The 501st Reports the Specific Conflict ───────────────────────────────
    // When the 501st trooper finds a Jedi already registered under that name,
    // he radios back the correct tactical report: "Alias already claimed in the
    // attribute type registry, General." Not the object class registry — this one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised error message for the case where the proposed alias is
     * already registered on a different attribute type in the schema.
     * The parent calls this to populate the error strip, so the wording is specific
     * to attribute types rather than using a generic conflict message.
     *
     * @return  a non-null localised string describing the attribute-type alias conflict
     */
    @Override
    protected String getAliasAlreadyExistsErrorMessage()
    {
        return Messages.getString( "EditAttributeTypeAliasesDialog.AliasAlreadyExists" ); //$NON-NLS-1$
    }


    // ── The 501st Checks the Attribute Type Registry ──────────────────────────
    // Before the 501st adds a new name to the temple's registry, they cross-check
    // it against the central attribute-type database to make sure nobody else already
    // holds that name. They don't check the object-class database — wrong department.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the given alias is already registered on a different attribute type
     * in the current schema.
     * We delegate to the schema handler's attribute-type-specific lookup rather than
     * the object-class one, because aliases are namespaced separately between the two kinds.
     *
     * @param alias  the proposed alias string to check
     * @return       {@code true} if another attribute type already claims this alias,
     *               {@code false} if it's available
     */
    @Override
    protected boolean isAliasAlreadyTaken( String alias )
    {
        return Activator.getDefault().getSchemaHandler().isAliasAlreadyTakenForAttributeType( alias );
    }
}
