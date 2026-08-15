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

package org.apache.directory.studio.ldifeditor.editor.actions;


import org.apache.directory.studio.ldifeditor.editor.LdifEditor;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.apache.directory.studio.ldifparser.model.LdifPart;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifModSpec;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.IUpdate;


// ── CLASS: AbstractLdifAction — REBEL OPERATOR REACHING FOR THE CONTROLS ──────
// Every action an operator takes at the console — format a record, edit an
// attribute, execute a bundle — follows the same choreography: check whether
// the move is legal, then do it.
// AbstractLdifAction captures that choreography: a safe run() gate that only
// calls doRun() when the action is enabled, and a trio of helpers that translate
// the editor's current text selection into LDIF domain objects (containers,
// parts, mod-specs) so subclasses do not have to repeat that plumbing.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Base class for all LDIF editor actions.
 * Wires an {@link Action} to a {@link LdifEditor}, provides a guarded
 * {@link #run()} method (delegates to {@link #doRun()} only when enabled), and
 * offers three helpers for reading the current LDIF selection as
 * {@link LdifContainer}s, {@link LdifPart}s, or a {@link LdifModSpec}.
 * Think of this as the console operator who checks their authority level before
 * touching any control.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractLdifAction extends Action implements IUpdate
{

    /** The LDIF editor this action operates on. */
    protected LdifEditor editor;


    // ── CONSTRUCT WITH LABEL AND EDITOR ───────────────────────────────────────
    // The operator picks up their assignment card (label) and notes which
    // console they are responsible for.
    /**
     * Creates a new LDIF action bound to {@code editor}.
     *
     * @param text    the action label shown in menus or tooltips
     * @param editor  the LDIF editor this action operates on
     */
    public AbstractLdifAction( String text, LdifEditor editor )
    {
        super( text );
        this.editor = editor;
    }


    // ── GUARDED EXECUTION ─────────────────────────────────────────────────────
    // The operator checks that their access card is valid before pressing
    // the button.
    /**
     * Calls {@link #doRun()} only if {@link #isEnabled()} returns {@code true}.
     * Subclasses implement the actual work in {@link #doRun()}.
     */
    public final void run()
    {
        if ( this.isEnabled() )
        {
            doRun();
        }
    }


    // ── CONCRETE ACTION HOOK ──────────────────────────────────────────────────
    /**
     * Performs the action.  Called only if {@link #isEnabled()} returned
     * {@code true}.  Subclasses must implement this method.
     */
    protected abstract void doRun();


    // ── REFRESH-THEN-REPORT ENABLED STATE ────────────────────────────────────
    // The operator presses {@link #update()} to re-evaluate permissions before
    // reporting back.
    /**
     * {@inheritDoc}
     *
     * <p>Calls {@link #update()} (which subclasses override to recompute
     * enablement) before returning the cached enabled state.</p>
     */
    public boolean isEnabled()
    {
        update();
        return super.isEnabled();
    }


    // ── RETURN THE PARSED LDIF MODEL ─────────────────────────────────────────
    /**
     * Returns the current {@link LdifFile} model from the editor.
     *
     * @return the parsed LDIF model, or {@code null} if not available
     */
    protected LdifFile getLdifModel()
    {
        LdifFile model = editor.getLdifModel();
        return model;
    }


    // ── SELECTION AS CONTAINERS ───────────────────────────────────────────────
    // The operator asks: "which LDIF records are covered by my text
    // selection right now?"
    /**
     * Returns the {@link LdifContainer}s that overlap the current text
     * selection in the editor.
     *
     * <p>For example — an operator checks which records are selected:</p>
     * <pre>
     *   LdifContainer[] selected = getSelectedLdifContainers();
     *   // selected[0] is the LdifContentRecord the caret is in
     * </pre>
     *
     * @return a non-null (possibly empty) array of selected containers
     */
    protected LdifContainer[] getSelectedLdifContainers()
    {

        LdifContainer[] containers = null;

        ISourceViewer sourceViewer = ( ISourceViewer ) editor.getAdapter( ISourceViewer.class );
        if ( sourceViewer != null )
        {
            LdifFile model = editor.getLdifModel();
            Point selection = sourceViewer.getSelectedRange();
            containers = LdifFile.getContainers( model, selection.x, selection.y );
        }

        return containers != null ? containers : new LdifContainer[0];

    }


    // ── SELECTION AS PARTS ────────────────────────────────────────────────────
    // The operator asks: "which individual LDIF lines are selected?"
    /**
     * Returns the individual {@link LdifPart}s that overlap the current text
     * selection.
     *
     * <p>For example — find the single attribute-value line under the caret:</p>
     * <pre>
     *   LdifPart[] parts = getSelectedLdifParts();
     *   if (parts.length == 1 &amp;&amp; parts[0] instanceof LdifAttrValLine) { ... }
     * </pre>
     *
     * @return a non-null (possibly empty) array of selected parts
     */
    protected LdifPart[] getSelectedLdifParts()
    {

        LdifPart[] parts = null;

        ISourceViewer sourceViewer = ( ISourceViewer ) editor.getAdapter( ISourceViewer.class );
        if ( sourceViewer != null )
        {
            LdifFile model = editor.getLdifModel();
            Point selection = sourceViewer.getSelectedRange();
            parts = LdifFile.getParts( model, selection.x, selection.y );

        }

        return parts != null ? parts : new LdifPart[0];

    }


    // ── SELECTION AS MOD-SPEC ─────────────────────────────────────────────────
    // The operator asks: "is the caret inside a modify-spec block, and if
    // so, which one?"
    /**
     * Returns the {@link LdifModSpec} at the current caret position within the
     * selected container, or {@code null} if there is not exactly one selected
     * container or the caret is not inside a mod-spec.
     *
     * @return the selected mod-spec, or {@code null}
     */
    protected LdifModSpec getSelectedLdifModSpec()
    {

        LdifModSpec modSpec = null;

        LdifContainer[] containers = getSelectedLdifContainers();
        if ( containers.length == 1 )
        {
            ISourceViewer sourceViewer = ( ISourceViewer ) editor.getAdapter( ISourceViewer.class );
            if ( sourceViewer != null )
            {
                Point selection = sourceViewer.getSelectedRange();
                modSpec = LdifFile.getInnerContainer( containers[0], selection.x );
            }
        }

        return modSpec;

    }

}
