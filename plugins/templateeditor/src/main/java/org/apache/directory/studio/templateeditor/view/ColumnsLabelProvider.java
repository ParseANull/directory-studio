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
package org.apache.directory.studio.templateeditor.view;


import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;


// ── CLASS: ColumnsLabelProvider — PALPATINE'S DISPLAY-ROSTER BASE ─────────────────
// When Palpatine issues his standing orders, every item in the roster needs two
// things: a name the ISDs can display, and possibly an icon to identify it at a
// glance. This base class is the default roster formatter — it provides no icon
// (null) and formats each item with its toString() value. Subclasses override to
// add richer presentation.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Base label provider for JFace viewers that have named columns. Implements
 * {@link ITableLabelProvider} so column-aware viewers (tables, trees with columns)
 * can ask for per-column text and images. By default, columns return no image and
 * use {@link Object#toString()} for text. Subclasses override the methods they need.
 *
 * <p>Think of this as Palpatine's default roster formatter — it produces a minimal
 * readable display for every item without special icons:</p>
 * <pre>
 *   getColumnImage( element, 0 ) → null          // no icon by default
 *   getColumnText(  element, 0 ) → element.toString()
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ColumnsLabelProvider extends LabelProvider implements ITableLabelProvider
{
    // ── GET COLUMN IMAGE: NO ICON BY DEFAULT ──────────────────────────────────────
    // Palpatine's default roster has no rank badges — subclasses that need icons
    // for specific column positions override this method to return them.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image for the given element in the given column. The base
     * implementation always returns {@code null}; subclasses override to supply
     * column-specific icons.
     *
     * @param element      the domain object being displayed
     * @param columnIndex  the zero-based column index
     * @return {@code null} by default; override to provide icons
     */
    public Image getColumnImage( Object element, int columnIndex )
    {
        return null;
    }


    // ── GET COLUMN TEXT: DISPLAY THE ELEMENT'S toString() ────────────────────────
    // Palpatine's default roster just reads the name off the element. Subclasses
    // override this to format different columns differently (title, object class, etc.)
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the text for the given element in the given column. The base
     * implementation returns {@code element.toString()}, or an empty string if
     * the element is {@code null}. Subclasses override to provide column-specific
     * formatting.
     *
     * @param element      the domain object being displayed
     * @param columnIndex  the zero-based column index
     * @return the element's {@code toString()} value, or {@code ""} for {@code null}
     */
    public String getColumnText( Object element, int columnIndex )
    {
        return element == null ? "" : element.toString(); //$NON-NLS-1$
    }
}
