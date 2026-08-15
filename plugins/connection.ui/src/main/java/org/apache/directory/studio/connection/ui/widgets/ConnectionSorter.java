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

package org.apache.directory.studio.connection.ui.widgets;


import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;


// ── CLASS: ConnectionSorter — REBEL BASE DIRECTORY FILING ORDER ───────────────────
// When Mon Mothma's staff look at the connection list they expect folders first,
// then individual connections — just like a filing cabinet with labelled dividers
// before the loose sheets.  ConnectionSorter enforces that order.  It gives
// ConnectionFolder objects category 1 and everything else (i.e. plain Connection
// objects) category 2, so JFace's tree viewer renders them in separate sorted
// groups.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * {@link ViewerSorter} for the connection tree widget.
 *
 * <p>Folders are placed before individual connections by assigning them a lower
 * sort category (1 vs. 2).  Within each group the default alphabetical ordering
 * from {@link ViewerSorter} applies.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionSorter extends ViewerSorter
{

    // ── CONNECT ───────────────────────────────────────────────────────────────────
    /**
     * Installs this sorter on the given tree viewer.
     *
     * <p>After this call the viewer will re-sort its contents using
     * {@link #category(Object)} every time it refreshes.</p>
     *
     * @param viewer The {@link TreeViewer} to sort.
     */
    public void connect( TreeViewer viewer )
    {
        viewer.setSorter( this );
    }


    // ── DISPOSE ───────────────────────────────────────────────────────────────────
    /**
     * Releases any resources held by this sorter.
     *
     * <p>Nothing to release in this implementation, but the method is here so
     * callers can follow the standard connect/dispose lifecycle.</p>
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── CATEGORY ─────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code 1} for {@link ConnectionFolder} elements so they sort
     * before plain {@link org.apache.directory.studio.connection.core.Connection}
     * elements, which get category {@code 2}.  Within each category the default
     * alphabetical comparator from {@link ViewerSorter} is used.</p>
     *
     * @param element The tree element to categorise.
     * @return {@code 1} for folders, {@code 2} for everything else.
     */
    @Override
    public int category( Object element )
    {
        // ── FOLDERS FIRST ─────────────────────────────────────────────────────────
        // Category 1 < category 2, so folders always appear above connections.
        // ──────────────────────────────────────────────────────────────────────────
        if ( element instanceof ConnectionFolder )
        {
            return 1;
        }
        else
        {
            return 2;
        }
    }
}
