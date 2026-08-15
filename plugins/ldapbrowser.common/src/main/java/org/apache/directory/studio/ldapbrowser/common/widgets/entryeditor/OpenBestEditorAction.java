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

package org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor;


import java.util.Collection;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.apache.directory.studio.valueeditors.IValueEditor;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;


// ── CLASS: OpenBestEditorAction — Luke Trusts the Force in the Trench ────────
// In the Death Star trench run, Luke Skywalker shuts off his targeting computer
// and closes his eyes. Obi-Wan's voice says "Use the Force, Luke." Luke doesn't
// pick just any shot — he trusts the Force to find the perfect one. That's this
// class: it asks the ValueEditorManager to identify the single best editor for
// the selected LDAP value, then fires it — but only after checking for schema
// warnings (like modifying an RDN attribute) that Luke should know about first.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Action that opens the best available editor for the currently selected LDAP
 * value. "Best" means what the {@link ValueEditorManager} recommends given the
 * attribute's schema type — for instance, a binary editor for JPEG photos, a
 * certificate viewer for X.509 data, or a plain text field for everything else.
 *
 * <p>Before launching the editor we validate the selection against the schema:
 * single-valued attributes, attributes not in the entry's subschema, read-only
 * attributes, and RDN parts all get a warning dialog so the user can confirm
 * before we proceed.</p>
 *
 * <p>Think of this class as Luke in the trench — the Force (schema + manager)
 * picks the perfect shot, and Luke fires only after making sure there are no
 * TIE fighters in the way.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenBestEditorAction extends AbstractOpenEditorAction
{

    /** The best value editor. */
    private IValueEditor bestValueEditor;


    // ── Luke Climbs Into His X-Wing and Enters the Trench ───────────────────────
    // Red Leader calls out the approach vector. Luke settles into his cockpit,
    // gets a reference to the squad's targeting system (ValueEditorManager), and
    // joins the trench run. All we do here is call the superclass constructor —
    // the real work happens when isEnabled() and run() are called mid-flight.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Wires up this action with the tree viewer, value editor manager, and action
     * group. We delegate straight to the superclass — there's no extra
     * initialization needed at construction time.
     *
     * <p>For example — Luke joins the trench run with his squad:</p>
     * <pre>
     *   Red Leader: "All wings report in."
     *   Luke: "Luke Skywalker, standing by." → super(viewer, mgr, group)
     * </pre>
     *
     * @param viewer              The tree viewer showing the entry's attributes and
     *                            values; we need it to read the current selection.
     * @param valueEditorManager  Knows which editor is best for each attribute type;
     *                            this is the Force guiding Luke's hand.
     * @param actionGroup         The surrounding action group; lets us deactivate
     *                            competing actions while this one is running.
     */
    public OpenBestEditorAction( TreeViewer viewer, ValueEditorManager valueEditorManager,
        EntryEditorWidgetActionGroup actionGroup )
    {
        super( viewer, valueEditorManager, actionGroup );
    }


    // ── Luke Asks the Force: Which Shot Is Best? ─────────────────────────────────
    // Mid-trench, Luke reaches out with the Force and senses the perfect firing
    // solution. He stores that answer so Wedge and Biggs can see what he's aiming
    // at. We expose the cached best editor so callers can query it without
    // re-running the full isEnabled() evaluation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value editor that was chosen as "best" the last time
     * {@link #isEnabled()} ran successfully. This is {@code null} if the action
     * is currently disabled (nothing selected, or multiple values selected).
     *
     * <p>For example — the Force shows Luke his target:</p>
     * <pre>
     *   bestValueEditor = valueEditorManager.getCurrentValueEditor(value)
     *   → e.g. the PasswordValueEditor for a userPassword attribute
     * </pre>
     *
     * @return  The currently selected best editor, or {@code null} if the action
     *          is not enabled.
     */
    public IValueEditor getBestValueEditor()
    {
        return this.bestValueEditor;
    }


    // ── Luke Powers Down After the Run ──────────────────────────────────────────
    // The Death Star explodes. Luke pulls out of the trench, powers down his
    // targeting systems, and slots back into formation. We clear the cached best
    // editor reference and let the superclass clean up the rest.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Clears the cached best-editor reference and delegates further teardown to
     * the superclass. Call this when the entry editor widget is closing to avoid
     * holding stale references.
     *
     * <p>For example — Luke powers down his targeting computer after the run:</p>
     * <pre>
     *   bestValueEditor = null  → release the editor reference
     *   super.dispose()         → superclass cleans up viewer and action group
     * </pre>
     */
    public void dispose()
    {
        bestValueEditor = null;
        super.dispose();
    }


    // ── Luke Has No Formal Command Code — the Force Guides Him ──────────────────
    // Luke doesn't need a serial number for his shot. He flies on instinct.
    // This action isn't bound to a named Eclipse command (no keyboard shortcut
    // wired through the platform command framework), so we return null here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse platform command ID for this action, used to bind
     * keyboard shortcuts through the command framework. We return {@code null}
     * because this action isn't registered as a named platform command — it's
     * driven purely by context.
     *
     * @return  Always {@code null}.
     */
    public String getCommandId()
    {
        return null;
    }


    // ── The Force Shows Luke the Icon of His Target ──────────────────────────────
    // Obi-Wan's guidance illuminates exactly what Luke is aiming at — a small
    // thermal exhaust port. The icon we show in the toolbar and context menu comes
    // from the best editor itself, so the user sees a meaningful symbol for the
    // type of editor that will open.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the toolbar or context menu entry. We
     * delegate to the best editor's own icon so users get a visual hint about
     * which editor will open — a lock icon for passwords, a calendar for
     * timestamps, and so on. Returns {@code null} when the action is disabled.
     *
     * <p>For example — the Force paints Luke's HUD with his target's image:</p>
     * <pre>
     *   isEnabled() → true
     *   return bestValueEditor.getValueEditorImageDescriptor()
     *   // e.g. a key icon for the PasswordValueEditor
     * </pre>
     *
     * @return  The best editor's image descriptor, or {@code null} if disabled.
     */
    public ImageDescriptor getImageDescriptor()
    {
        return isEnabled() ? bestValueEditor.getValueEditorImageDescriptor() : null;
    }


    // ── Obi-Wan Names the Perfect Approach for Luke ──────────────────────────────
    // "Use the Force, Luke" — Obi-Wan's voice names the strategy. Here the
    // strategy is the best editor's human-readable name (e.g. "Password Editor")
    // which appears as the menu item label so the user knows what will open.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name of the best editor, used as the label for this
     * action in the context menu and toolbar tooltip. Returns {@code null} when
     * the action is disabled so no label appears for an unavailable action.
     *
     * <p>For example — Obi-Wan names the chosen strategy:</p>
     * <pre>
     *   bestValueEditor.getValueEditorName()
     *   → "Password Editor"  (for a userPassword value)
     *   → "Image Editor"     (for a jpegPhoto value)
     * </pre>
     *
     * @return  The best editor's name, or {@code null} if the action is disabled.
     */
    public String getText()
    {
        if ( isEnabled() )
        {
            return bestValueEditor.getValueEditorName();
        }
        else
        {
            return null;
        }
    }


    // ── Luke Stretches Out with the Force — Is the Shot Clear? ──────────────────
    // Luke reaches out, senses the trench, checks for obstacles. One value
    // selected? No attribute node selected? Can the cell modifier handle it?
    // If all clear, he identifies the best editor and caches it for the run.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Determines whether this action can currently run. We require exactly one
     * value selected (not an attribute node), and the cell modifier must confirm
     * the value column is editable. If both conditions are met we ask the
     * ValueEditorManager for the current best editor and cache it for use by
     * {@link #run()}, {@link #getImageDescriptor()}, and {@link #getText()}.
     *
     * <p>For example — Luke checks his firing solution before committing:</p>
     * <pre>
     *   1 value selected, 0 attributes selected → proceed
     *   cellModifier.canModify(value, VALUE_COLUMN) → confirm editable
     *   bestValueEditor = manager.getCurrentValueEditor(value) → cache it
     *   return true  → action lights up in the toolbar
     * </pre>
     *
     * @return  {@code true} if exactly one value is selected and it's editable;
     *          {@code false} otherwise.
     */
    public boolean isEnabled()
    {
        if ( getSelectedValues().length == 1
            && getSelectedAttributes().length == 0
            && viewer.getCellModifier().canModify( getSelectedValues()[0],
                EntryEditorWidgetTableMetadata.VALUE_COLUMN_NAME ) )
        {
            // update value editor
            bestValueEditor = valueEditorManager.getCurrentValueEditor( getSelectedValues()[0] );
            setCellEditor( bestValueEditor.getCellEditor() );

            return true;
        }
        else
        {
            bestValueEditor = null;
            return false;
        }
    }


    // ── Luke Closes His Eyes and Fires the Proton Torpedo ───────────────────────
    // Obi-Wan: "Use the Force, Luke." Luke takes a breath, shuts off targeting,
    // and fires — but not before confirming there are no TIE fighters blocking
    // his shot (schema warnings: single-valued? not in subschema? read-only? RDN?).
    // Only after the user acknowledges any warnings do we actually open the editor.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Validates the selected value against the LDAP schema and, if any concerns
     * exist (single-valued conflict, attribute not in subschema, non-modifiable
     * attribute, RDN part), shows the user a confirmation dialog before proceeding.
     * If the user confirms — or if there were no warnings — we delegate to the
     * superclass to open the best editor's cell editor in the tree. If the user
     * cancels on an empty (placeholder) value, we delete that placeholder so it
     * doesn't litter the entry.
     *
     * <p>For example — Luke checks for obstacles before firing:</p>
     * <pre>
     *   value.isEmpty() && attribute.isSingleValued() → warn about single-valued
     *   !subschema.contains(atd)                      → warn attribute not allowed
     *   !SchemaUtils.isModifiable(atd)                → warn read-only
     *   value.isRdnPart()                             → warn RDN modification
     *   user confirms → super.run() opens the editor
     *   user cancels  → delete the empty placeholder value
     * </pre>
     */
    @Override
    public void run()
    {
        boolean ok = true;

        if ( ( getSelectedValues().length == 1 ) && ( getSelectedAttributes().length == 0 ) )
        {
            IValue value = getSelectedValues()[0];
            StringBuffer message = new StringBuffer();
            IAttribute attribute = value.getAttribute();
            String description = attribute.getDescription();
            AttributeType atd = attribute.getAttributeTypeDescription();

            if ( value.isEmpty() )
            {
                // validate single-valued attributes
                if ( ( attribute.getValueSize() > 1 ) && atd.isSingleValued() )
                {
                    message.append( NLS.bind( Messages.getString( "OpenBestEditorAction.ValueSingleValued" ), description ) );//$NON-NLS-1$
                    message.append( BrowserCoreConstants.LINE_SEPARATOR );
                    message.append( BrowserCoreConstants.LINE_SEPARATOR );
                }

                // validate if value is allowed
                IEntry entry = attribute.getEntry();
                Collection<AttributeType> allAtds = SchemaUtils.getAllAttributeTypeDescriptions( entry );

                if ( !allAtds.contains( atd ) )
                {
                    message.append( NLS.bind( Messages.getString( "OpenBestEditorAction.AttributeNotInSubSchema" ), description ) );//$NON-NLS-1$
                    message.append( BrowserCoreConstants.LINE_SEPARATOR );
                    message.append( BrowserCoreConstants.LINE_SEPARATOR );
                }
            }

            // validate non-modifiable attributes
            if ( !SchemaUtils.isModifiable( atd ) )
            {
                message.append( NLS.bind( Messages.getString( "OpenBestEditorAction.ValueNotModifiable" ), description ) );//$NON-NLS-1$
                message.append( BrowserCoreConstants.LINE_SEPARATOR );
                message.append( BrowserCoreConstants.LINE_SEPARATOR );
            }

            // validate modification of Rdn
            if ( value.isRdnPart() && ( getCellEditor() != valueEditorManager.getRenameValueEditor() ) )
            {
                message.append( NLS.bind( Messages.getString( "OpenBestEditorAction.ValueIsRdnPart" ), description ) );//$NON-NLS-1$
                message.append( BrowserCoreConstants.LINE_SEPARATOR );
                message.append( BrowserCoreConstants.LINE_SEPARATOR );
            }

            if ( message.length() > 0 )
            {
                if ( value.isEmpty() )
                {
                    message.append( Messages.getString( "OpenBestEditorAction.NewValueQuestion" ) ); //$NON-NLS-1$
                }
                else
                {
                    message.append( Messages.getString( "OpenBestEditorAction.EditValueQuestion" ) ); //$NON-NLS-1$
                }

                ok = MessageDialog.openConfirm( getShell(), getText(), message.toString() );
            }

            if ( ok )
            {
                super.run();
            }
            else
            {
                if ( value.isEmpty() )
                {
                    attribute.deleteEmptyValue();
                }
            }
        }
    }
}
