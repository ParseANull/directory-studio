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

package org.apache.directory.studio.ldapbrowser.common.wizards;


import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DummyEntry;
import org.apache.directory.studio.ldapbrowser.core.utils.CompoundModification;
import org.apache.directory.studio.ldapbrowser.core.utils.ModelConverter;
import org.apache.directory.studio.ldifparser.model.container.LdifContentRecord;


// ── CLASS: EditEntryWizard — HAN RECALIBRATES THE FALCON MID-MISSION ─────────
// In "The Empire Strikes Back", Han Solo crawls into the Millennium Falcon's
// guts while fleeing the Empire — he doesn't build a new ship, he takes what
// already works, tweaks the hyperdrive components, verifies the diff against
// what was there before, and fires the engines.
// This wizard does the same for LDAP: it takes an existing entry, hands you
// a live editable copy (the prototype), lets you adjust object classes and
// attributes, then computes the delta and sends only the changes to the server.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A wizard for editing an existing LDAP entry offline and then committing the
 * differences back to the server.
 * It creates an in-memory clone of the selected entry (the "prototype"), lets
 * the user modify object classes and attributes through the normal wizard pages,
 * and on finish computes the diff and sends only the changed attributes via
 * a {@link CompoundModification}.
 * Think of this class as Han Solo tinkering with the Falcon mid-flight — he
 * works on a copy of the system, figures out what changed, and pushes just
 * those fixes to the actual ship.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditEntryWizard extends NewEntryWizard
{

    // ── Han Pulls Up the Falcon's Existing Configuration ─────────────────────
    // Han slides under the Falcon with a scanner, reads the current state of
    // every component, and makes a working copy so he can tinker safely without
    // breaking what's still flying.
    // We do the same: convert the live entry to LDIF, parse it back into a
    // DummyEntry prototype so we have a clean mutable snapshot to work on.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EditEntryWizard} targeting the given entry.
     * We immediately snapshot the entry into an LDIF record and parse that
     * record back into a {@link DummyEntry} prototype — that prototype is
     * what the wizard pages modify, while the original entry stays untouched
     * until the user clicks Finish.
     *
     * <p>For example — Han reads the Falcon's current drive spec into a working copy:</p>
     * <pre>
     *   LdifContentRecord record = ModelConverter.entryToLdifContentRecord( entry );
     *   prototypeEntry = ModelConverter.ldifContentRecordToEntry( record, connection );
     *   // original entry is now safe; only prototypeEntry gets edited
     * </pre>
     *
     * @param entry  The live LDAP entry to edit; must not be {@code null}.
     * @throws RuntimeException  if the LDIF round-trip snapshot fails for any reason.
     */
    public EditEntryWizard( IEntry entry )
    {
        setWindowTitle( Messages.getString( "EditEntryWizard.EditEntry" ) ); //$NON-NLS-1$
        setNeedsProgressMonitor( true );

        selectedEntry = entry;
        selectedConnection = entry.getBrowserConnection();

        try
        {
            EventRegistry.suspendEventFiringInCurrentThread();
            LdifContentRecord record = ModelConverter.entryToLdifContentRecord( selectedEntry );
            prototypeEntry = ModelConverter.ldifContentRecordToEntry( record, selectedConnection );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
        finally
        {
            EventRegistry.resumeEventFiringInCurrentThread();
        }
    }


    // ── Han Opens Exactly Two Access Panels ───────────────────────────────────
    // Han knows the Falcon inside-out — he doesn't open every hatch, just the
    // object-class panel and the attribute panel, the two places an edit touches.
    // We register exactly those two wizard pages: the object-class page (for
    // changing which OCs apply) and the attributes page (for changing values).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers the two wizard pages used when editing an existing entry.
     * We skip the type-selection and DN pages that {@link NewEntryWizard} uses
     * because for an edit the entry type (from scratch vs. template) and the DN
     * are both already fixed.
     *
     * <p>For example — Han opens only the two panels that need recalibration:</p>
     * <pre>
     *   ocPage        = new NewEntryObjectclassWizardPage( ... ); // object class panel
     *   attributePage = new NewEntryAttributesWizardPage( ... ); // attributes panel
     * </pre>
     */
    @Override
    public void addPages()
    {
        ocPage = new NewEntryObjectclassWizardPage( NewEntryObjectclassWizardPage.class.getName(), this );
        addPage( ocPage );

        attributePage = new NewEntryAttributesWizardPage( NewEntryAttributesWizardPage.class.getName(), this );
        addPage( attributePage );
    }


    // ── Han Slams the Access Panel Shut, Abort Mission ───────────────────────
    // Han's diagnostic turns up something nasty mid-calibration, and Chewie
    // roars that the TIEs are closing — Han slams the panel shut and they run.
    // No damage is done because he never actually wrote the changes to the drive.
    // Cancelling just discards the prototype; the original entry is unaffected.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Cancels the edit operation cleanly.
     * Because all modifications were made to the in-memory prototype rather than
     * the live entry, cancelling requires no rollback — we just throw away the
     * prototype and walk away.
     *
     * <p>For example — Han aborts without touching the real hyperdrive:</p>
     * <pre>
     *   // prototypeEntry changes discarded automatically
     *   // selectedEntry is exactly as it was before the wizard opened
     * </pre>
     *
     * @return  Always {@code true} — cancellation always succeeds.
     */
    @Override
    public boolean performCancel()
    {
        return true;
    }


    // ── Han Fires Up the Recalibrated Hyperdrive ─────────────────────────────
    // Once Han is happy with the adjustments, he closes every panel and hits the
    // jump lever — the Falcon surges forward carrying only the changes he made.
    // Here we call CompoundModification to diff the prototype against the
    // original entry and push only the changed attributes to the LDAP server.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Commits the changes made to the prototype back to the real LDAP entry.
     * We compute the diff between the prototype (what the user edited) and the
     * selected entry (the live server copy) then send only the changed attributes
     * via a {@link CompoundModification} — efficient, like Han only replacing
     * the broken parts, not rebuilding the whole ship.
     *
     * <p>For example — Han sends just the new drive settings to the Falcon's core:</p>
     * <pre>
     *   new CompoundModification().replaceAttributes( prototypeEntry, selectedEntry, wizard );
     *   // only diffs are transmitted; unchanged attributes go nowhere
     * </pre>
     *
     * @return  Always {@code true}; any server errors surface through the
     *          progress monitor rather than as a {@code false} return.
     */
    @Override
    public boolean performFinish()
    {
        new CompoundModification().replaceAttributes( prototypeEntry, selectedEntry, this );
        return true;
    }


    // ── Han Grabs the Component He's Supposed to Be Fixing ───────────────────
    // Before Han can calibrate anything he needs the specific component in hand —
    // he checks the manifest, pulls the right part off the shelf, and gets to work.
    // This method returns the live LDAP entry that this wizard was opened to edit.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the live LDAP entry that this wizard was opened to edit.
     * This is the original, unmodified server-side entry — not the prototype.
     * Other parts of the UI use this to know which entry was being worked on.
     *
     * <p>For example — Han checks the part number before picking up his wrench:</p>
     * <pre>
     *   IEntry live = wizard.getSelectedEntry();
     *   // live is the unmodified original; prototypeEntry is the scratch copy
     * </pre>
     *
     * @return  The entry that was selected when this wizard was opened.
     */
    public IEntry getSelectedEntry()
    {
        return selectedEntry;
    }


    // ── Han Confirms Which Ship He's Working On ───────────────────────────────
    // Han would never accidentally service somebody else's ship — he always
    // double-checks the docking bay number before picking up a hydrospanner.
    // This method returns the connection (the "ship") the target entry lives on.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP connection that the entry being edited belongs to.
     * We need this to know where to send the diff when the user clicks Finish,
     * and to look up schema information for the object-class and attribute pages.
     *
     * <p>For example — Han verifies he's working on the Falcon, not the Tantive IV:</p>
     * <pre>
     *   IBrowserConnection conn = wizard.getSelectedConnection();
     *   // use conn.getSchema() to look up valid attribute types, etc.
     * </pre>
     *
     * @return  The connection hosting the entry under edit.
     */
    public IBrowserConnection getSelectedConnection()
    {
        return selectedConnection;
    }


    // ── Han Reads His Working Draft of the Drive Config ──────────────────────
    // Han keeps a clipboard with his in-progress calibration notes — that's
    // what he's currently editing, not the live drive spec on the ship's computer.
    // The prototype entry is our equivalent: the editable in-memory snapshot.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the in-memory prototype entry — the editable snapshot of the
     * original entry that the wizard pages operate on.
     * Changes made here won't reach the server until {@link #performFinish()} is called.
     *
     * <p>For example — Han checks his clipboard of pending drive adjustments:</p>
     * <pre>
     *   DummyEntry proto = wizard.getPrototypeEntry();
     *   // proto has the same attributes as selectedEntry, ready to be modified
     * </pre>
     *
     * @return  The mutable prototype entry being edited in-memory.
     */
    public DummyEntry getPrototypeEntry()
    {
        return prototypeEntry;
    }


    // ── Han Swaps In a Revised Set of Calibration Notes ─────────────────────
    // Mid-repair, Chewie hands Han a fresh set of engine schematics that
    // supersede the ones he was using — Han swaps the clipboard and keeps going.
    // We do the same when a template-based copy replaces the initial prototype.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the current prototype entry with the supplied one.
     * This is called when the wizard needs to swap in a freshly constructed
     * prototype — for example, after cloning a template entry in the type page.
     *
     * <p>For example — Han accepts a revised schematic from Chewie mid-repair:</p>
     * <pre>
     *   wizard.setPrototypeEntry( freshClone );
     *   // subsequent wizard pages now see the new prototype
     * </pre>
     *
     * @param getPrototypeEntry  The new prototype entry to use going forward.
     */
    public void setPrototypeEntry( DummyEntry getPrototypeEntry )
    {
        this.prototypeEntry = getPrototypeEntry;
    }

}
