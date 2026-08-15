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


// -- CLASS: EntryEditorWidgetTableMetadata -- THE MILLENNIUM FALCON MAINTENANCE MANIFEST --
// Hanging on the wall of Docking Bay 94 is the Millennium Falcon's official maintenance
// manifest: a laminated sheet listing every system in the ship, its column number in the
// master log, its column name, and the full ordered list of specs to check.
// Nobody edits the manifest mid-flight -- it's a sealed reference document.
// This class IS that manifest: a final utility class holding the column index constants
// and column name strings that every other part of the entry editor needs to agree on.
// If anyone wants to know "which column is the attribute name?", they come here.
// ---------------------------------------------------------------------------------
/**
 * A non-instantiable constants class that defines the column layout of the entry editor table.
 * Every class that needs to know which column index maps to "attribute name" or "value"
 * references these constants rather than hard-coding magic numbers.
 * Think of this class as the Millennium Falcon's maintenance manifest: a single sealed
 * reference document that every system on the ship reads from, so everyone agrees on
 * what goes in which column.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class EntryEditorWidgetTableMetadata
{

    // -- THE MANIFEST IS SEALED -- NO CONSTRUCTION ALLOWED ----------------------
    // The manifest is a laminated sheet bolted to the wall. Nobody is supposed to
    // instantiate it -- it's just a reference document. The private constructor
    // enforces that; any attempt to call new EntryEditorWidgetTableMetadata()
    // would be a compile error from outside the class.
    // ---------------------------------------------------------------------------------
    /**
     * Private constructor that prevents instantiation.
     * This class is a pure constants holder -- all its members are static.
     * There is never a reason to create an instance of it.
     *
     * <p>For example -- the manifest is bolted to the wall:</p>
     * <pre>
     *   Han tries to fold up the manifest and take it with him.
     *   Chewie blocks the door: "RWARGH." (No. It stays here. Everyone needs it.)
     * </pre>
     */
    private EntryEditorWidgetTableMetadata()
    {
    }

    /** The Constant KEY_COLUMN_INDEX. */
    public static final int KEY_COLUMN_INDEX = 0;

    /** The Constant VALUE_COLUMN_INDEX. */
    public static final int VALUE_COLUMN_INDEX = 1;

    /** The Constant KEY_COLUMN_NAME. */
    public static final String KEY_COLUMN_NAME = Messages
        .getString( "EntryEditorWidgetTableMetadata.AttributeDescription" ); //$NON-NLS-1$

    /** The Constant VALUE_COLUMN_NAME. */
    public static final String VALUE_COLUMN_NAME = Messages.getString( "EntryEditorWidgetTableMetadata.Value" ); //$NON-NLS-1$

    /** The Constant COLUM_NAMES. */
    public static final String[] COLUM_NAMES =
        { KEY_COLUMN_NAME, VALUE_COLUMN_NAME };

}
