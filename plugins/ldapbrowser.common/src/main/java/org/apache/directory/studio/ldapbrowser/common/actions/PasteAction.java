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

package org.apache.directory.studio.ldapbrowser.common.actions;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;


// ── CLASS: PasteAction — LEIA HIDES THE DEATH STAR PLANS IN R2-D2 ────────────
// At the opening of A New Hope, Princess Leia intercepts the Death Star plans
// and, with the Empire closing in, presses the data disc into R2-D2's slot.
// The information leaves one vessel and lands in another, ready to be delivered
// where it's needed.  That's the paste metaphor: data that was copied somewhere
// else gets inserted into a new destination.  This abstract base class owns the
// standard "paste" identity — the label, icon, and command ID — while concrete
// subclasses decide exactly what gets pasted and where.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for paste actions in the LDAP browser.
 *
 * <p>We take care of the boilerplate here — the standard paste icon from
 * Eclipse's shared image library, the workbench command ID that ties us to
 * Ctrl+V — so concrete subclasses only have to implement {@code run()} and
 * {@code isEnabled()} for their specific paste target (entries, attributes,
 * values, etc.).</p>
 *
 * <p>Think of this class as the moment Leia hands the plans to R2: the
 * mechanism of transferring data is universal; what matters is the concrete
 * subclass deciding what the data is and where it lands.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class PasteAction extends BrowserAction
{
    // ── Leia Reaches for R2's Data Port ──────────────────────────────────────
    // Leia finds R2 in the corridor — she doesn't need any special ceremony,
    // just a direct hand-off.  The constructor is the same: no preamble,
    // just initialize so we're ready to receive and forward data.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code PasteAction}.  Delegates to the parent
     * constructor for standard Eclipse action setup.  Concrete subclasses
     * will typically call {@code super()} and then configure anything
     * specific to their paste target.
     */
    public PasteAction()
    {
        super();
    }


    // ── R2-D2 Carries the Standard Alliance Insignia ─────────────────────────
    // R2 is instantly recognizable by his Rebel insignia — everyone in the
    // Alliance knows what he carries and who he works for.
    // We use the standard Eclipse paste icon for the same reason: universal
    // recognition across the entire workbench.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the standard Eclipse "paste" icon from the workbench's shared
     * image library.  We deliberately use the platform shared image rather
     * than a custom one so the paste action looks consistent with every other
     * paste operation in the Eclipse IDE.
     *
     * @return the {@link ImageDescriptor} for the shared paste icon
     */
    public ImageDescriptor getImageDescriptor()
    {
        return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor( ISharedImages.IMG_TOOL_PASTE );
    }


    // ── R2 Responds to the Universal Alliance Frequency ──────────────────────
    // No matter which Rebel ship calls, R2 answers on the same frequency.
    // Our command ID is the standard workbench paste ID — Ctrl+V on any
    // platform will find and fire this action through Eclipse's command
    // framework.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the standard Eclipse workbench paste command ID.  This ties us
     * to the Ctrl+V (or platform equivalent) keybinding via the command
     * framework — without this, our paste action wouldn't respond to the
     * keyboard shortcut.
     *
     * @return the workbench paste command ID from
     *         {@link IWorkbenchActionDefinitionIds#PASTE}
     */
    public String getCommandId()
    {
        return IWorkbenchActionDefinitionIds.PASTE;
    }

}
