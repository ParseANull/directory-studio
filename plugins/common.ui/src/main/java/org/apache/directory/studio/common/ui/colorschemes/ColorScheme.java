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
package org.apache.directory.studio.common.ui.colorschemes;


import org.apache.directory.studio.common.ui.CommonUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;


/**
 * A named set of colors derived from a Base16 palette, mapped to Studio's
 * 15 semantic color preference keys.
 *
 * Base16 slots used:
 *   base03 → muted (comments, disabled)
 *   base05 → default foreground
 *   base08 → red   (errors, deletes, separators)
 *   base09 → orange (OIDs, constants)
 *   base0A → yellow (object classes, modify)
 *   base0B → green  (values, adds)
 *   base0D → blue   (functions, attribute types, renames)
 *   base0E → purple (keywords)
 */
public class ColorScheme
{
    public final String id;
    public final String label;

    static final String[] KEYS = {
        CommonUIConstants.DEFAULT_COLOR,       // 0
        CommonUIConstants.DISABLED_COLOR,      // 1
        CommonUIConstants.ERROR_COLOR,         // 2
        CommonUIConstants.COMMENT_COLOR,       // 3
        CommonUIConstants.KEYWORD_1_COLOR,     // 4
        CommonUIConstants.KEYWORD_2_COLOR,     // 5
        CommonUIConstants.OBJECT_CLASS_COLOR,  // 6
        CommonUIConstants.ATTRIBUTE_TYPE_COLOR,// 7
        CommonUIConstants.VALUE_COLOR,         // 8
        CommonUIConstants.OID_COLOR,           // 9
        CommonUIConstants.SEPARATOR_COLOR,     // 10
        CommonUIConstants.ADD_COLOR,           // 11
        CommonUIConstants.DELETE_COLOR,        // 12
        CommonUIConstants.MODIFY_COLOR,        // 13
        CommonUIConstants.RENAME_COLOR,        // 14
    };

    private final String[] values;


    ColorScheme( String id, String label,
        String base03, String base05,
        String base08, String base09, String base0A, String base0B,
        String base0D, String base0E )
    {
        this.id = id;
        this.label = label;
        this.values = new String[] {
            base05, // defaultColor      → foreground
            base03, // disabledColor     → muted/comments
            base08, // errorColor        → red
            base03, // commentColor      → muted/comments
            base0E, // keyword1Color     → keywords (purple)
            base0D, // keyword2Color     → functions/methods (blue)
            base0A, // objectClassColor  → classes (yellow)
            base0D, // attributeTypeColor → attribute IDs (blue)
            base0B, // valueColor        → strings (green)
            base09, // oidColor          → constants (orange)
            base08, // separatorColor    → operators (red)
            base0B, // addColor          → diff inserted (green)
            base08, // deleteColor       → diff deleted (red)
            base0A, // modifyColor       → changed (yellow)
            base0D, // renameColor       → renamed (blue)
        };
    }


    /** Writes all 15 color preferences into the given store. */
    public void applyTo( IPreferenceStore store )
    {
        for ( int i = 0; i < KEYS.length; i++ )
        {
            store.setValue( KEYS[i], values[i] );
        }
    }


    /** Returns the "R,G,B" values in the same order as {@link #KEYS}. */
    public String[] getValues()
    {
        return values.clone();
    }
}
