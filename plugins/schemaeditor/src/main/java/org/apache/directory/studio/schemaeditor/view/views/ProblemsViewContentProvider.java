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
package org.apache.directory.studio.schemaeditor.view.views;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.exception.LdapSchemaException;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.model.schemachecker.SchemaChecker;
import org.apache.directory.studio.schemaeditor.model.schemachecker.SchemaWarning;
import org.apache.directory.studio.schemaeditor.view.wrappers.Folder;
import org.apache.directory.studio.schemaeditor.view.wrappers.Folder.FolderType;
import org.apache.directory.studio.schemaeditor.view.wrappers.ProblemsViewRoot;
import org.apache.directory.studio.schemaeditor.view.wrappers.SchemaErrorWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.SchemaWarningWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.TreeNode;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: ProblemsViewContentProvider — Han Shooting First ──────────────────
// In the Mos Eisley Cantina, Han Solo doesn't wait for Greedo to make a move.
// He sizes up the situation, identifies the threat, and acts preemptively —
// before the problem can escalate. That's exactly what this content provider does:
// it intercepts the raw schema checker output and organizes it into a structured
// "Errors" folder and "Warnings" folder before the UI even asks. By the time
// the user opens the Problems View, the threats are already catalogued, wrapped,
// and sorted into the right buckets. Han didn't wait; neither do we.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Supplies the tree structure for the Problems View — organizing raw schema checker
 * results into a two-level tree of Errors and Warnings folders. The first time
 * {@link #getChildren(Object)} is called on the root, we query the {@link SchemaChecker},
 * sort errors and warnings into separate {@link Folder} wrappers, and wrap each
 * individual problem in a {@link SchemaErrorWrapper} or {@link SchemaWarningWrapper}.
 * Think of it as Han shooting first: we intercept the raw problems and organize them
 * before anyone else has a chance to mishandle them.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ProblemsViewContentProvider implements IStructuredContentProvider, ITreeContentProvider
{
    // ── Han Surveys the Room for Threats ─────────────────────────────────────
    // The moment Han walks into the cantina he's scanning faces, counting exits,
    // assessing who's a problem. getElements is the first sweep — it delegates
    // straight to getChildren which does all the real assessment work.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the top-level elements for the Problems View tree.
     * Delegates to {@link #getChildren(Object)} since the root-level logic is identical
     * to child-level logic in this tree.
     *
     * @param inputElement  the root input (a {@link ProblemsViewRoot})
     * @return              the top-level folder nodes (Errors folder, Warnings folder)
     */
    public Object[] getElements( Object inputElement )
    {
        return getChildren( inputElement );
    }


    // ── Holster Slides Back, No Cleanup Needed ───────────────────────────────
    // Han doesn't apologize after neutralizing a threat — he just moves on.
    // There's nothing to clean up here either; we hold no listeners or resources.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Called by JFace when this content provider is being released.
     * We hold no listeners or resources to free, so this is intentionally empty.
     */
    public void dispose()
    {
        // Nothing to do.
    }


    // ── A New Mark Sits Down — But Han Already Knows ─────────────────────────
    // When a new stranger enters the cantina, Han notes it — but he doesn't
    // rebuild his mental model from scratch unless something fundamentally changes.
    // inputChanged fires when the viewer's root input is swapped out, but since
    // we re-query fresh on every getChildren call, we don't need to react here.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Called by JFace when the viewer's input object changes.
     * Since we re-query the {@link SchemaChecker} fresh every time the root asks
     * for children, we don't need to cache or react to input changes here.
     *
     * @param viewer    the viewer whose input changed
     * @param oldInput  the previous root input
     * @param newInput  the new root input
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
        // Nothing to do.
    }


    // ── Han Identifies the Threats Before They Can Act ────────────────────────
    // Han has Greedo (and every other problem in that cantina) identified and
    // categorized the moment he walks in. getChildren does the same: on first
    // call against the root, it queries the SchemaChecker, builds an Errors folder
    // for LdapSchemaExceptions and a Warnings folder for SchemaWarnings, wraps
    // each item, and returns the whole organized structure. Subsequent calls just
    // return the pre-built children.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the children of the given tree element, building the folder structure
     * the first time it's called on the root.
     * For the {@link ProblemsViewRoot} we lazily build an Errors folder (containing
     * {@link SchemaErrorWrapper} children) and a Warnings folder (containing
     * {@link SchemaWarningWrapper} children). For {@link Folder} nodes we just return
     * their pre-built children. Error and warning wrappers are leaf nodes with empty
     * children.
     *
     * <p>For example — Han draws first:</p>
     * <pre>
     *   getChildren(ProblemsViewRoot)
     *   → schemaChecker.getErrors()   → wraps each in SchemaErrorWrapper under Errors folder
     *   → schemaChecker.getWarnings() → wraps each in SchemaWarningWrapper under Warnings folder
     *   → returns [Errors folder, Warnings folder]
     * </pre>
     *
     * @param parentElement  a {@link ProblemsViewRoot}, {@link Folder}, {@link SchemaErrorWrapper},
     *                       or {@link SchemaWarningWrapper}
     * @return               the array of child {@link TreeNode} objects; never null
     */
    public Object[] getChildren( Object parentElement )
    {
        List<TreeNode> children = null;

        if ( parentElement instanceof ProblemsViewRoot )
        {
            ProblemsViewRoot root = ( ProblemsViewRoot ) parentElement;

            if ( root.getChildren().isEmpty() )
            {
                SchemaChecker schemaChecker = Activator.getDefault().getSchemaChecker();

                if ( schemaChecker != null )
                {
                    List<Throwable> errors = schemaChecker.getErrors();
                    if ( !( errors.size() == 0 ) )
                    {
                        Folder errorsFolder = new Folder( FolderType.ERROR, root );
                        root.addChild( errorsFolder );
                        for ( Throwable error : errors )
                        {
                            if ( error instanceof LdapSchemaException )
                            {
                                errorsFolder.addChild( new SchemaErrorWrapper( ( LdapSchemaException ) error,
                                    errorsFolder ) );
                            }
                        }
                    }

                    SchemaWarning[] warnings = schemaChecker.getWarnings().toArray( new SchemaWarning[0] );
                    if ( !( warnings.length == 0 ) )
                    {
                        Folder warningsFolder = new Folder( FolderType.WARNING, root );
                        root.addChild( warningsFolder );
                        for ( SchemaWarning warning : warnings )
                        {
                            warningsFolder.addChild( new SchemaWarningWrapper( warning, warningsFolder ) );
                        }
                    }
                }
            }

            children = root.getChildren();
        }
        else if ( parentElement instanceof Folder )
        {
            Folder folder = ( Folder ) parentElement;

            children = folder.getChildren();
        }
        else if ( parentElement instanceof SchemaErrorWrapper )
        {
            children = new ArrayList<TreeNode>();
        }
        else if ( parentElement instanceof SchemaWarningWrapper )
        {
            children = new ArrayList<TreeNode>();
        }

        return children.toArray();
    }


    // ── Tracing the Threat Back to Its Source ────────────────────────────────
    // Greedo was sitting at that particular table for a reason — he came from
    // somewhere. getParent traces any tree node back to its parent folder or root,
    // which JFace needs when it wants to reveal or expand to a specific item.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent of the given tree element.
     * JFace uses this to expand the tree to a specific node. We delegate to the
     * {@link TreeNode#getParent()} method on wrapper objects.
     *
     * @param element  a {@link TreeNode} wrapper whose parent we need
     * @return         the parent {@link TreeNode}, or {@code null} for root-level nodes
     */
    public Object getParent( Object element )
    {
        if ( element instanceof TreeNode )
        {
            return ( ( TreeNode ) element ).getParent();
        }

        // Default
        return null;
    }


    // ── Does This Threat Have Accomplices? ───────────────────────────────────
    // Some threats travel in groups — folders have children. Individual error and
    // warning wrappers are lone wolves with no sub-items. hasChildren lets JFace
    // know whether to show an expand arrow, without triggering a full child build.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the given tree element has any children.
     * Folders (Errors, Warnings) have children; individual error and warning wrappers
     * are leaf nodes. JFace calls this before fetching children so it can decide
     * whether to draw an expand arrow.
     *
     * @param element  a {@link TreeNode} to test
     * @return         {@code true} if this node has children (i.e., it's a folder with items)
     */
    public boolean hasChildren( Object element )
    {
        if ( element instanceof TreeNode )
        {
            return ( ( TreeNode ) element ).hasChildren();
        }

        // Default
        return false;
    }
}
