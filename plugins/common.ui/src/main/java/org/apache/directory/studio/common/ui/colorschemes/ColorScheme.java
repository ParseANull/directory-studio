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


// ── CLASS: ColorScheme — IMPERIAL CONFIGURATION TERMINAL COLOR PALETTE ────────
// Each Death Star configuration terminal can be re-skinned with a different
// color palette to match the lighting conditions of the control room — Solarized
// for brightly-lit Imperial bridges, Dracula for dimly-lit weapon bays.  This
// class represents one such palette: a named set of eight Base16 colors mapped
// to all fifteen semantic preference keys that Studio uses when rendering LDAP
// elements.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We represent a single named Base16 color scheme.  We store fifteen "R,G,B"
 * strings — one for each of Studio's semantic color preference keys — and
 * provide methods to apply them to the preference store or retrieve them for
 * preview rendering.
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


    // ── CONSTRUCTOR ColorScheme — LOADING A PALETTE INTO THE TERMINAL ─────────
    // The Imperial technician loads a new color cartridge into the terminal:
    // we take the eight Base16 color strings and map them to the fifteen
    // semantic keys according to the palette specification.  The mapping is
    // fixed — base05 is always the foreground, base08 is always red, and so on.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a new color scheme by mapping the eight provided Base16 color
     * strings (already in "R,G,B" format) to our fifteen semantic preference
     * keys.  This constructor is package-private — use {@link ColorSchemes} to
     * obtain instances.
     *
     * @param id     the unique identifier string for this scheme
     * @param label  the human-readable display name
     * @param base03 the muted/comment color
     * @param base05 the default foreground color
     * @param base08 the red (error/delete/separator) color
     * @param base09 the orange (OID/constant) color
     * @param base0A the yellow (object class/modify) color
     * @param base0B the green (value/add) color
     * @param base0D the blue (function/attribute type/rename) color
     * @param base0E the purple (keyword) color
     */
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


    // ── METHOD applyTo — FLASHING THE NEW PALETTE ONTO THE TERMINAL ───────────
    // The technician presses the "apply" button on the Imperial terminal and all
    // fifteen semantic color preferences switch to the values of this palette in
    // one sweep.  The new colors take effect immediately across every editor and
    // tree view that reads from the preference store.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We write all fifteen color preference values from this scheme into the
     * given preference store.  After this call, every editor and renderer that
     * reads from the store will use our colors.
     *
     * @param store the preference store to write into
     */
    public void applyTo( IPreferenceStore store )
    {
        for ( int i = 0; i < KEYS.length; i++ )
        {
            store.setValue( KEYS[i], values[i] );
        }
    }


    // ── METHOD getValues — READING THE PALETTE SWATCHES ──────────────────────
    // The technician reads off the fifteen color values from the terminal's
    // display so the preference page can paint preview swatches.  We return
    // a defensive copy so callers cannot accidentally corrupt our internal
    // array.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return a defensive copy of the fifteen "R,G,B" strings in the same
     * order as {@link #KEYS}.  Use this to render preview swatches without
     * needing to read from the preference store.
     *
     * @return a copy of the color value strings in {@link #KEYS} order
     */
    public String[] getValues()
    {
        return values.clone();
    }
}
