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
package org.apache.directory.studio.schemaeditor.view;


import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.model.schemachecker.SchemaChecker;
import org.apache.directory.studio.schemaeditor.view.wrappers.AttributeTypeWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.Folder;
import org.apache.directory.studio.schemaeditor.view.wrappers.ObjectClassWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.SchemaWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.TreeNode;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;


// ── CLASS: SchemaEditorSchemaCheckerLabelDecorator — MACE WINDU CONFRONTS PALPATINE ──
// Mace Windu stands in the Chancellor's office, four Jedi Masters at his back,
// and inspects Palpatine with absolute focus — looking for errors (full Sith
// corruption) or warnings (suspicious behaviour that hasn't crossed the line yet).
// His verdict is immediate: arrest, caution, or stand down.
// We play Mace here: for every node in the Schema View tree we consult the
// SchemaChecker, then stamp an error or warning overlay onto the node's icon
// so the user sees the problem without having to open the element.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A JFace {@link ILightweightLabelDecorator} that overlays error and warning
 * badges on schema elements — attribute types, object classes, schemas, and
 * folders — based on what the {@link SchemaChecker} found when it validated them.
 * Eclipse calls {@link #decorate} for every visible tree node; we ask the
 * checker whether that node has errors or warnings and stamp the appropriate
 * icon on it so the user can spot problems at a glance.
 * Think of this class as Mace Windu: he inspects each Senate attendee with
 * calm authority, renders his verdict, and stamps it visibly for all to see.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaEditorSchemaCheckerLabelDecorator extends LabelProvider implements ILightweightLabelDecorator
{
    // ── MACE SIZES UP THE CHANCELLOR ─────────────────────────────────────────
    // Mace Windu walks into Palpatine's office, ignites his lightsaber, and
    // scrutinises the man across from him — is this an error (Sith Lord,
    // full corruption) or a warning (suspicious behaviour, not yet confirmed)?
    // He makes his judgement in seconds, based on what the Council's intelligence
    // (the SchemaChecker) has already gathered.
    // We do the same: check the element's type, ask the SchemaChecker for its
    // verdict, and hand the result to decorateState() to apply the badge.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Inspects a schema tree element and overlays an error or warning badge
     * on its icon based on what the {@link SchemaChecker} reports.
     * Eclipse calls this for every visible node in the Schema View tree, so
     * we need to be fast — we delegate all the heavy validation logic to the
     * checker and just act on the outcome here.
     * For folders and schemas we recurse into children to bubble up the worst
     * state found anywhere underneath.
     *
     * <p>For example — Mace Windu inspects the Senate chamber row by row:</p>
     * <pre>
     *   Palpatine (AttributeTypeWrapper) — checker reports ERROR
     *     → Mace stamps a red error badge immediately and exits.
     *   Senator Jar Jar (ObjectClassWrapper) — checker reports WARNING
     *     → Mace stamps a yellow caution badge and moves on.
     *   Empty row (Folder with no bad children) — no issues found
     *     → Mace walks past without marking anyone.
     * </pre>
     *
     * @param element     the tree node being decorated; may be an
     *                    {@link AttributeTypeWrapper}, {@link ObjectClassWrapper},
     *                    {@link SchemaWrapper}, or {@link Folder}
     * @param decoration  the JFace decoration context we add our overlay image to
     */
    public void decorate( Object element, IDecoration decoration )
    {
        SchemaChecker schemaChecker = Activator.getDefault().getSchemaChecker();
        ElementState state = ElementState.NONE;

        if ( element instanceof AttributeTypeWrapper )
        {
            AttributeType at = ( ( AttributeTypeWrapper ) element ).getAttributeType();

            if ( schemaChecker.hasErrors( at ) )
            {
                decorateState( ElementState.ERROR, decoration );
                return;
            }

            if ( schemaChecker.hasWarnings( at ) )
            {
                state = ElementState.WARNING;
            }
        }
        else if ( element instanceof ObjectClassWrapper )
        {
            ObjectClass oc = ( ( ObjectClassWrapper ) element ).getObjectClass();

            if ( schemaChecker.hasErrors( oc ) )
            {
                decorateState( ElementState.ERROR, decoration );
                return;
            }

            if ( schemaChecker.hasWarnings( oc ) )
            {
                state = ElementState.WARNING;
            }
        }
        else if ( element instanceof SchemaWrapper )
        {
            Schema schema = ( ( SchemaWrapper ) element ).getSchema();

            for ( AttributeType at : schema.getAttributeTypes() )
            {
                if ( schemaChecker.hasErrors( at ) )
                {
                    decorateState( ElementState.ERROR, decoration );
                    return;
                }

                if ( schemaChecker.hasWarnings( at ) )
                {
                    state = ElementState.WARNING;
                }
            }

            for ( ObjectClass oc : schema.getObjectClasses() )
            {
                if ( schemaChecker.hasErrors( oc ) )
                {
                    decorateState( ElementState.ERROR, decoration );
                    return;
                }

                if ( schemaChecker.hasWarnings( oc ) )
                {
                    state = ElementState.WARNING;
                }
            }
        }
        else if ( element instanceof Folder )
        {
            Folder folder = ( Folder ) element;

            if ( childrenHasErrors( folder.getChildren(), schemaChecker ) )
            {
                decorateState( ElementState.ERROR, decoration );
                return;
            }

            if ( childrenHasWarnings( folder.getChildren(), schemaChecker ) )
            {
                state = ElementState.WARNING;
            }
        }

        decorateState( state, decoration );
    }


    // ── MACE RENDERS HIS VERDICT ─────────────────────────────────────────────
    // Based on what the inspection revealed, Mace Windu either draws his
    // purple blade fully (ERROR — this is a Sith Lord), holds it ready but
    // doesn't advance (WARNING — suspicious, watch carefully), or sheathes it
    // and nods (NONE — nothing to flag here, move along).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Applies the appropriate icon overlay to a tree node based on its
     * computed {@link ElementState} — error badge, warning badge, or nothing.
     * We keep this separate from {@link #decorate} so the overlay-application
     * logic is in one place regardless of how many different element types
     * trigger it.
     *
     * <p>For example — Mace issues his verdict:</p>
     * <pre>
     *   ElementState.ERROR   → red error overlay stamped bottom-left
     *   ElementState.WARNING → yellow warning overlay stamped bottom-left
     *   ElementState.NONE    → no overlay; node looks normal
     * </pre>
     *
     * @param state       the severity we determined for this element
     * @param decoration  the JFace decoration context to add the overlay image to
     */
    private void decorateState( ElementState state, IDecoration decoration )
    {
        switch ( state )
        {
            case WARNING:
                decoration.addOverlay(
                    Activator.getDefault().getImageDescriptor( PluginConstants.IMG_OVERLAY_WARNING ),
                    IDecoration.BOTTOM_LEFT );
                break;
            case ERROR:
                decoration.addOverlay( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_OVERLAY_ERROR ),
                    IDecoration.BOTTOM_LEFT );
                break;
            default:
                break;
        }
    }


    // ── MACE CHECKS HIS FELLOW JEDI'S REPORT ON THE AIDES ───────────────────
    // Mace asks Kit Fisto and Agen Kolar to scan the Senator's entourage for
    // warning signs — behaviour that doesn't prove dark-side allegiance yet
    // but definitely warrants a yellow flag.
    // We recurse through the child nodes, asking the SchemaChecker whether any
    // attribute type or object class in the subtree has warnings.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Recursively checks whether any descendant of the given node list has
     * schema warnings according to the provided {@link SchemaChecker}.
     * We need this so that a parent folder or schema can "bubble up" a warning
     * badge even when the bad element is several levels deeper in the tree.
     * Returns {@code true} on the first warning found, so we stop early and
     * don't scan the whole subtree unnecessarily.
     *
     * <p>For example — Mace's team sweeps the entourage:</p>
     * <pre>
     *   Child 1 (AttributeType) — hasWarnings? No  → recurse into its children
     *   Child 2 (ObjectClass)   — hasWarnings? Yes → return true immediately
     * </pre>
     *
     * @param children      the list of child {@link TreeNode}s to inspect;
     *                      may be {@code null}, in which case we return {@code false}
     * @param schemaChecker the checker that knows which elements have warnings
     * @return              {@code true} if any descendant carries a warning,
     *                      {@code false} if the subtree is clean
     */
    public boolean childrenHasWarnings( List<TreeNode> children, SchemaChecker schemaChecker )
    {
        if ( children != null )
        {
            for ( TreeNode child : children )
            {
                if ( child instanceof AttributeTypeWrapper )
                {
                    AttributeType at = ( ( AttributeTypeWrapper ) child ).getAttributeType();

                    if ( schemaChecker.hasWarnings( at ) )
                    {
                        return true;
                    }
                    else
                    {
                        if ( childrenHasWarnings( child.getChildren(), schemaChecker ) )
                        {
                            return true;
                        }
                    }
                }
                else if ( child instanceof ObjectClassWrapper )
                {
                    ObjectClass oc = ( ( ObjectClassWrapper ) child ).getObjectClass();

                    if ( schemaChecker.hasWarnings( oc ) )
                    {
                        return true;
                    }
                    else
                    {
                        if ( childrenHasWarnings( child.getChildren(), schemaChecker ) )
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }


    // ── MACE CHECKS FOR FULL SITH CORRUPTION ─────────────────────────────────
    // This is the harder question: not just suspicious behaviour, but actual
    // confirmed dark-side allegiance — an error severe enough to make the
    // element fundamentally broken.
    // Mace asks Saesee Tiin and Agen Kolar to check for it in the entourage;
    // if any one of them has turned, we report it immediately.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Recursively checks whether any descendant of the given node list has
     * schema errors according to the provided {@link SchemaChecker}.
     * Just like {@link #childrenHasWarnings}, this exists so parent nodes can
     * bubble up an error badge when a deeply nested child is broken.
     * Returns {@code true} on the first error found to avoid unnecessary scanning.
     *
     * <p>For example — Mace's team checks for full Sith allegiance:</p>
     * <pre>
     *   Child 1 (AttributeType) — hasErrors? No  → recurse into its children
     *   Child 2 (ObjectClass)   — hasErrors? Yes → return true immediately
     * </pre>
     *
     * @param children      the list of child {@link TreeNode}s to inspect;
     *                      may be {@code null}, in which case we return {@code false}
     * @param schemaChecker the checker that knows which elements have errors
     * @return              {@code true} if any descendant carries an error,
     *                      {@code false} if the subtree is error-free
     */
    public boolean childrenHasErrors( List<TreeNode> children, SchemaChecker schemaChecker )
    {
        if ( children != null )
        {
            for ( TreeNode child : children )
            {
                if ( child instanceof AttributeTypeWrapper )
                {
                    AttributeType at = ( ( AttributeTypeWrapper ) child ).getAttributeType();

                    if ( schemaChecker.hasErrors( at ) )
                    {
                        return true;
                    }
                    else
                    {
                        if ( childrenHasErrors( child.getChildren(), schemaChecker ) )
                        {
                            return true;
                        }
                    }
                }
                else if ( child instanceof ObjectClassWrapper )
                {
                    ObjectClass oc = ( ( ObjectClassWrapper ) child ).getObjectClass();

                    if ( schemaChecker.hasErrors( oc ) )
                    {
                        return true;
                    }
                    else
                    {
                        if ( childrenHasErrors( child.getChildren(), schemaChecker ) )
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Represents the validation outcome for a single schema element after the
     * SchemaChecker has weighed in.
     * Think of it as Mace Windu's verdict: {@code NONE} means all clear,
     * {@code WARNING} means something looks off but isn't fatal, and
     * {@code ERROR} means a full-blown schema violation that needs fixing now.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private enum ElementState
    {
        NONE, WARNING, ERROR;
    }
}
