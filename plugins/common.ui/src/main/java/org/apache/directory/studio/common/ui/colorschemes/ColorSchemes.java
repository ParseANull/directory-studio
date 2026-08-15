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


import java.util.Arrays;
import java.util.Collections;
import java.util.List;


// ── CLASS: ColorSchemes — IMPERIAL DESIGN ARCHIVES CATALOG ───────────────────
// The Imperial Death Star's design archives contain every approved color scheme
// ever commissioned for its control terminals.  This class is that catalog: an
// immutable list of all available Base16 palettes, ready for the configuration
// terminal to browse and apply.  Nobody can instantiate the catalog itself —
// it is a sealed reference document.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We are the central catalog of all available Base16 color schemes.  The
 * {@link #ALL} list is immutable and covers fifteen popular schemes.  Use
 * {@link #findById} to look up a specific scheme by its id string, such as
 * when restoring the user's last chosen scheme from the preference store.
 *
 * Source: https://github.com/tinted-theming/schemes
 *
 * Each entry maps eight Base16 palette slots to Studio's 15 semantic colors.
 * All hex values are sRGB.
 */
public final class ColorSchemes
{
    // ── CONSTRUCTOR ColorSchemes — SEALING THE IMPERIAL ARCHIVES ─────────────
    // The archives are sealed: nobody can construct an instance of this catalog
    // class.  All content is accessed statically through ALL and findById.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We keep this constructor private because this is a constants/catalog
     * class.  All data is accessed through its static fields and methods.
     */
    private ColorSchemes()
    {
    }


    // ── METHOD h — DECODING A HEX COLOR FROM THE ARCHIVE ─────────────────────
    // The archive records colors in compact six-character hex notation.  We
    // decode them into the "R,G,B" format that the Eclipse preference store
    // understands, so every scheme can be applied directly without further
    // conversion.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We convert a six-character sRGB hex string (no leading {@code #}) into
     * the {@code "R,G,B"} comma-separated format that {@link IPreferenceStore}
     * uses for color values.
     *
     * @param hex a six-character hex color string such as {@code "dc322f"}
     * @return the color as a {@code "R,G,B"} string, e.g. {@code "220,50,47"}
     */
    static String h( String hex )
    {
        int r = Integer.parseInt( hex.substring( 0, 2 ), 16 );
        int g = Integer.parseInt( hex.substring( 2, 4 ), 16 );
        int b = Integer.parseInt( hex.substring( 4, 6 ), 16 );
        return r + "," + g + "," + b;
    }


    // ── METHOD s — ASSEMBLING A SCHEME RECORD FROM THE ARCHIVE ───────────────
    // The archive assembler takes the raw hex values and the scheme metadata,
    // converts all the hex strings to "R,G,B" format via h(), and hands the
    // finished ColorScheme record back to be added to the ALL catalog list.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We build a {@link ColorScheme} by converting all eight hex color strings
     * to "R,G,B" format and delegating to the {@link ColorScheme} constructor.
     * This is a private factory shorthand used only inside the {@link #ALL}
     * initializer.
     *
     * @param id  the unique scheme identifier
     * @param label the display name
     * @param b03 base03 hex (muted)
     * @param b05 base05 hex (foreground)
     * @param b08 base08 hex (red)
     * @param b09 base09 hex (orange)
     * @param b0A base0A hex (yellow)
     * @param b0B base0B hex (green)
     * @param b0D base0D hex (blue)
     * @param b0E base0E hex (purple)
     * @return the constructed ColorScheme
     */
    private static ColorScheme s( String id, String label,
        String b03, String b05,
        String b08, String b09, String b0A, String b0B,
        String b0D, String b0E )
    {
        return new ColorScheme( id, label, h( b03 ), h( b05 ), h( b08 ), h( b09 ), h( b0A ), h( b0B ), h( b0D ), h( b0E ) );
    }


    /**
     * All available Base16 color schemes, in display order.
     * Columns: id | label | b03(muted) | b05(fg) | b08(red) | b09(orange) | b0A(yellow) | b0B(green) | b0D(blue) | b0E(purple)
     */
    public static final List<ColorScheme> ALL = Collections.unmodifiableList( Arrays.asList(

        s( "solarized-dark",  "Solarized Dark",   "586e75", "839496", "dc322f", "cb4b16", "b58900", "859900", "268bd2", "6c71c4" ),
        s( "solarized-light", "Solarized Light",  "93a1a1", "657b83", "dc322f", "cb4b16", "b58900", "859900", "268bd2", "6c71c4" ),
        s( "monokai",         "Monokai",          "75715e", "f8f8f2", "f92672", "fd971f", "f4bf75", "a6e22e", "66d9e8", "ae81ff" ),
        s( "nord",            "Nord",             "4c566a", "d8dee9", "bf616a", "d08770", "ebcb8b", "a3be8c", "81a1c1", "b48ead" ),
        s( "one-dark",        "One Dark",         "5c6370", "abb2bf", "e06c75", "d19a66", "e5c07b", "98c379", "61afef", "c678dd" ),
        s( "gruvbox-dark",    "Gruvbox Dark",     "7c6f64", "ebdbb2", "fb4934", "fe8019", "fabd2f", "b8bb26", "83a598", "d3869b" ),
        s( "gruvbox-light",   "Gruvbox Light",    "928374", "3c3836", "cc241d", "d65d0e", "d79921", "98971a", "458588", "b16286" ),
        s( "tomorrow-night",  "Tomorrow Night",   "969896", "c5c8c6", "cc6666", "de935f", "f0c674", "b5bd68", "81a2be", "b294bb" ),
        s( "tomorrow",        "Tomorrow (Light)", "8e908c", "4d4d4c", "c82829", "f5871f", "eab700", "718c00", "4271ae", "8959a8" ),
        s( "dracula",         "Dracula",          "6272a4", "f8f8f2", "ff5555", "ffb86c", "f1fa8c", "50fa7b", "8be9fd", "ff79c6" ),
        s( "github-dark",     "GitHub Dark",      "6e7781", "e6edf3", "ff7b72", "ffa657", "e3b341", "7ee787", "79c0ff", "d2a8ff" ),
        s( "github-light",    "GitHub Light",     "6e7781", "24292f", "cf222e", "bc4c00", "9a6700", "116329", "0969da", "8250df" ),
        s( "catppuccin-mocha","Catppuccin Mocha", "585b70", "cdd6f4", "f38ba8", "fab387", "f9e2af", "a6e3a1", "89b4fa", "cba6f7" ),
        s( "rose-pine",       "Rosé Pine",        "6e6a86", "e0def4", "eb6f92", "f6c177", "f6c177", "31748f", "9ccfd8", "c4a7e7" ),
        s( "tokyo-night",     "Tokyo Night",      "565f89", "c0caf5", "f7768e", "ff9e64", "e0af68", "9ece6a", "7aa2f7", "bb9af7" )

    ) );


    // ── METHOD findById — LOCATING A SCHEME IN THE IMPERIAL ARCHIVES ──────────
    // The archivist scans the catalog for the scheme matching the given ID
    // badge.  If the badge is blank or does not match any known scheme, the
    // archivist returns null — no impersonators are admitted to the archive.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We search {@link #ALL} for a scheme whose {@code id} matches the given
     * string and return it.  Returns {@code null} if the id is null, empty, or
     * does not match any known scheme.  Use this to restore a previously
     * selected scheme from the preference store.
     *
     * @param id the scheme id to look up
     * @return the matching {@link ColorScheme}, or {@code null} if not found
     */
    public static ColorScheme findById( String id )
    {
        if ( id == null || id.isEmpty() )
        {
            return null;
        }
        for ( ColorScheme scheme : ALL )
        {
            if ( scheme.id.equals( id ) )
            {
                return scheme;
            }
        }
        return null;
    }
}
