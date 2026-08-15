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


import java.util.Arrays;

import org.apache.directory.studio.valueeditors.IValueEditor;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;


// ── CLASS: OpenEditorAction — Han Picks the Right Tool from the Falcon's Kit ─
// In the bowels of the Millennium Falcon, Han Solo reaches into his tool locker
// and closes his hand around the specific hydrospanner he needs for this repair.
// Not the universal wrench — the one right tool for this particular job. That's
// this class: given a specific IValueEditor chosen by the user from the "Open
// With" submenu, we wire it up as the action that launches exactly that editor
// for the selected LDAP value.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Action that opens a specific, user-chosen value editor for the currently
 * selected LDAP value. This powers the "Open With &gt; [Editor Name]" submenu
 * entries — each submenu item is backed by one instance of this class holding
 * a different {@link IValueEditor}.
 *
 * <p>Before launching, we register the chosen editor as the user's explicit
 * preference with the {@link ValueEditorManager}, overriding the "best" editor
 * heuristic for this value type until the user makes another choice.</p>
 *
 * <p>Think of this class as Han reaching for the specific hydrospanner he
 * knows he needs — not the multi-tool, not the closest thing at hand — the
 * right one for this exact repair.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenEditorAction extends AbstractOpenEditorAction
{

    /** The specific value editor. */
    private IValueEditor valueEditor;


    // ── Han Grabs His Specific Tool from the Locker ─────────────────────────────
    // Han reaches into the Falcon's tool locker, pulls out exactly the right
    // hydrospanner, and immediately sets it as the active tool on the workbench.
    // We store the specific editor and register its cell editor with the superclass
    // so the tree viewer knows which widget to activate when editing starts.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets up this action with a concrete, caller-specified value editor. We
     * immediately tell the superclass which cell editor widget to use so it's
     * ready to install into the tree viewer the moment the user triggers this
     * action.
     *
     * <p>For example — Han picks his tool before walking to the repair site:</p>
     * <pre>
     *   setCellEditor(valueEditor.getCellEditor())  → wire up the widget
     *   this.valueEditor = valueEditor              → remember which tool we grabbed
     * </pre>
     *
     * @param viewer              The tree viewer showing the entry's attributes;
     *                            we read the selection from it.
     * @param valueEditorManager  The manager that tracks which editor the user
     *                            has explicitly chosen; we update it on run().
     * @param valueEditor         The specific editor this action represents, e.g.
     *                            the HexEditor or the DNEditor.
     * @param actionGroup         The surrounding action group; used by the
     *                            superclass to coordinate with other actions.
     */
    public OpenEditorAction( TreeViewer viewer, ValueEditorManager valueEditorManager, IValueEditor valueEditor,
        EntryEditorWidgetActionGroup actionGroup )
    {
        super( viewer, valueEditorManager, actionGroup );
        setCellEditor( valueEditor.getCellEditor() );
        this.valueEditor = valueEditor;
    }


    // ── Han Shows You Which Tool He's Holding ───────────────────────────────────
    // "This one," Han says, lifting the hydrospanner so you can see the label.
    // We expose the stored editor reference so callers can inspect it — useful
    // when the action group needs to compare editors or query their capabilities.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the specific value editor this action is bound to. Callers — mainly
     * the action group building the "Open With" submenu — use this to check which
     * editor each action holds.
     *
     * <p>For example — Han shows the crew his tool of choice:</p>
     * <pre>
     *   openEditorAction.getValueEditor()
     *   → e.g. the HexEditor instance
     * </pre>
     *
     * @return  The concrete {@link IValueEditor} this action wraps; never
     *          {@code null} while the action is alive.
     */
    public IValueEditor getValueEditor()
    {
        return valueEditor;
    }


    // ── Han Uses His Specific Tool for the Repair ───────────────────────────────
    // Han plants his feet, sets the hydrospanner as his active instrument, and
    // gets to work. We register this editor as the user's explicit choice with the
    // manager — so the manager doesn't second-guess us with the "best" heuristic —
    // then delegate to the superclass to install the cell editor in the tree.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Runs the action by first registering this editor as the user's explicit
     * preference (overriding the auto-selected "best" editor), then delegating
     * to the superclass to activate the cell editor in the tree viewer for the
     * selected value row.
     *
     * <p>For example — Han locks in his tool choice and starts the repair:</p>
     * <pre>
     *   valueEditorManager.setUserSelectedValueEditor(valueEditor)
     *   // Now the manager remembers "user wants THIS editor for this type"
     *   super.run()
     *   // Tree cell editor activates → user can start typing
     * </pre>
     */
    public void run()
    {
        // ensure that the specific value editor is activated
        valueEditorManager.setUserSelectedValueEditor( valueEditor );

        super.run();
    }


    // ── Han Puts the Tool Back in the Locker ────────────────────────────────────
    // The repair is done. Han wipes the hydrospanner and drops it back in the
    // locker. We clear the editor reference so nothing holds a stale pointer
    // after the entry editor widget has been disposed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Clears the editor reference and delegates remaining cleanup to the
     * superclass. Call this when the entry editor widget is shutting down to
     * prevent memory leaks from a lingering editor reference.
     *
     * <p>For example — Han stows the tool when the Falcon lifts off:</p>
     * <pre>
     *   valueEditor = null  → drop the reference
     *   super.dispose()     → superclass cleans up viewer and action group
     * </pre>
     */
    public void dispose()
    {
        valueEditor = null;
        super.dispose();
    }


    // ── Han's Tool Has No Formal Command Code ───────────────────────────────────
    // Han doesn't file paperwork for every hydrospanner he uses. This action isn't
    // registered as a named platform command — it lives only in the "Open With"
    // submenu at runtime — so we return null for the command ID.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse platform command ID for keyboard binding. We return
     * {@code null} because this action is dynamically created at runtime for
     * each available editor and isn't registered in plugin.xml as a named command.
     *
     * @return  Always {@code null}.
     */
    public String getCommandId()
    {
        return null;
    }


    // ── The Label on Han's Specific Tool ────────────────────────────────────────
    // Every tool in Han's locker has a label stamped on it. We return this editor's
    // icon so the "Open With" menu item shows a recognizable visual cue for the
    // editor it represents.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for this action's menu entry. We delegate directly to the
     * editor's own image descriptor so each "Open With" submenu item has a distinct,
     * recognizable icon matching the editor type.
     *
     * <p>For example — Han reads the icon stamped on his specific tool:</p>
     * <pre>
     *   valueEditor.getValueEditorImageDescriptor()
     *   → e.g. a binary pattern icon for the HexEditor
     * </pre>
     *
     * @return  This editor's image descriptor; never {@code null} while the editor
     *          reference is valid.
     */
    public ImageDescriptor getImageDescriptor()
    {
        return valueEditor.getValueEditorImageDescriptor();
    }


    // ── Han Reads the Name on His Tool ──────────────────────────────────────────
    // "Hydrospanner," Han reads off the handle — that's what he tells the crew.
    // We return the editor's human-readable name as the menu item label so the
    // user knows exactly which editor they're picking from the submenu.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name of this specific editor, shown as the "Open With"
     * submenu item label. Each editor provides its own name (e.g., "Hex Editor,"
     * "DN Editor," "Image Editor").
     *
     * <p>For example — Han reads the name stamped on his tool:</p>
     * <pre>
     *   valueEditor.getValueEditorName()
     *   → "Hex Editor"  (for the HexEditor instance)
     * </pre>
     *
     * @return  The editor's name string; used directly as the menu item label.
     */
    public String getText()
    {
        return valueEditor.getValueEditorName();
    }


    // ── Han Checks If His Tool Actually Fits This Job ───────────────────────────
    // Han picks up the hydrospanner and holds it up to the coupling he needs to
    // fix. Does it fit? He checks: exactly one value selected, no attribute node
    // selected, and the editor appears in the alternative-editors list for this
    // value type. Only then does he confirm it's the right tool.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Determines whether this specific editor is a valid choice for the currently
     * selected value. We require exactly one value selected, no attribute nodes
     * selected, and the editor must appear in the
     * {@link ValueEditorManager#getAlternativeValueEditors} list for this value
     * (meaning the manager considers it a valid alternative, not just a random
     * editor). We also require that the editor can produce a non-null raw value
     * from the selection.
     *
     * <p>For example — Han checks the tool fits before committing:</p>
     * <pre>
     *   getSelectedValues().length == 1, getSelectedAttributes().length == 0
     *   → proceed to compatibility check
     *   Arrays.asList(alternativeVps).contains(valueEditor)
     *   → this editor is in the valid alternatives list
     *   valueEditor.getRawValue(selectedValue) != null
     *   → editor can handle this value → return true
     * </pre>
     *
     * @return  {@code true} if this editor is a valid alternative for the selected
     *          value; {@code false} otherwise.
     */
    public boolean isEnabled()
    {
        if ( getSelectedValues().length == 1
            && getSelectedAttributes().length == 0 )
//            && viewer.getCellModifier().canModify( getSelectedValues()[0],
//                EntryEditorWidgetTableMetadata.VALUE_COLUMN_NAME )
        {
            IValueEditor[] alternativeVps = valueEditorManager.getAlternativeValueEditors( getSelectedValues()[0] );

            return Arrays.asList( alternativeVps ).contains( valueEditor )
                && valueEditor.getRawValue( getSelectedValues()[0] ) != null;
        }
        else
        {
            return false;
        }
    }

}
