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


// ── CLASS: EditObjectClassAliasesDialog — COMMANDER CODY TARGETS OBI-WAN ─────
// While the 501st storms the Jedi Temple, Commander Cody receives Order 66 on
// Utapau and immediately turns on Obi-Wan Kenobi. Same order as the 501st,
// same mission structure — but a completely different target and a different radio
// report when Cody checks in with Coruscant.
// This class is the "object class regiment": it carries out the same aliases-editing
// mission as {@link EditAttributeTypeAliasesDialog} but plugs in the object-class-specific
// duplicate check and error message instead of the attribute-type ones.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Concrete dialog for editing the aliases list of an LDAP object class.
 * An object class's aliases are the names it goes by — "person", "organizationalPerson", etc.
 * This dialog inherits the full add/edit/remove UI from {@link AbstractAliasesDialog} and
 * plugs in the object-class-specific duplicate check and error message.
 * Think of this class as Commander Cody executing Order 66 against Obi-Wan: same tactical
 * framework as the attribute-type regiment, just pointed at a different part of the registry.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditObjectClassAliasesDialog extends AbstractAliasesDialog
{
    // ── Cody Receives the Roster for Utapau ───────────────────────────────────
    // Before departing for Utapau, Commander Cody receives a full briefing on
    // the current status of the Jedi at his position — in our case the current
    // aliases on the object class. He passes this baseline up the chain of command
    // so HQ knows what was there before the operation began.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the dialog with the object class's current aliases pre-loaded.
     * We pass everything up to {@link AbstractAliasesDialog}, which snapshots the list
     * so it can tell the difference between aliases the user changed and ones already on the class.
     *
     * @param aliases  the object class's current alias list; may be empty but not null
     */
    public EditObjectClassAliasesDialog( List<String> aliases )
    {
        super( aliases );
    }


    // ── Cody Labels His Briefing Room ────────────────────────────────────────
    // "Utapau — Target: General Kenobi" is chalked on Cody's briefing board so
    // there's no confusion about who this squad is after. We override the parent's
    // generic shell title to say "Edit Object Class Aliases" instead.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Overrides the shell title set by the parent to read "Edit Object Class Aliases"
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
        newShell.setText( Messages.getString( "EditObjectClassAliasesDialog.EditAliases" ) ); //$NON-NLS-1$
    }


    // ── Cody Files the Correct Tactical Report ────────────────────────────────
    // When Cody's scouts find a Jedi already registered under a proposed alias,
    // Cody radios Coruscant with the precise report: "Alias already claimed in
    // the object class registry." Not the attribute type registry — this regiment
    // only reports on object classes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised error message for the case where the proposed alias is
     * already registered on a different object class in the schema.
     * The parent calls this to populate the error strip.
     *
     * @return  a non-null localised string describing the object-class alias conflict
     */
    @Override
    protected String getAliasAlreadyExistsErrorMessage()
    {
        return Messages.getString( "EditObjectClassAliasesDialog.AliasAlreadyExists" ); //$NON-NLS-1$
    }


    // ── Cody Checks the Object Class Registry ────────────────────────────────
    // Cody cross-references the proposed alias against Coruscant's object-class
    // database — not the attribute-type one. He uses the right radio frequency
    // for the right department, so he gets the right answer.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the given alias is already registered on a different object class
     * in the current schema.
     * We delegate to the schema handler's object-class-specific lookup, which is a
     * separate namespace from the attribute type registry.
     *
     * @param alias  the proposed alias string to check
     * @return       {@code true} if another object class already claims this alias,
     *               {@code false} if it's available
     */
    @Override
    protected boolean isAliasAlreadyTaken( String alias )
    {
        return Activator.getDefault().getSchemaHandler().isAliasAlreadyTakenForObjectClass( alias );
    }
}
