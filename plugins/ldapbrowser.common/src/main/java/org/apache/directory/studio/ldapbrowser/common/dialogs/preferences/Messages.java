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

package org.apache.directory.studio.ldapbrowser.common.dialogs.preferences;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING AT THE COMMS CONSOLE ─────────────────
// C-3PO sits at the Millennium Falcon's comms console, a steady stream of
// encoded messages arriving from Alliance cells across the galaxy.  Each message
// is tagged with a key — a short identifier that tells him which drawer of his
// vast linguistic database to open.  He returns the plain-language translation,
// or, if the key is unknown, flags it with exclamation marks so the crew knows
// something is missing from the manifest.  This class is that comms console:
// a static lookup utility that translates NLS keys into the locale-appropriate
// string from our resource bundle.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Standard Eclipse NLS helper — looks up localised strings from the
 * {@code messages.properties} file in this package given a string key.
 * Every other class in this package calls {@link #getString(String)} instead
 * of hard-coding UI text, which is how Eclipse supports multiple locales.
 * Think of this class as C-3PO at the comms console — he knows six million
 * forms of communication and will always return a translation, even if it's
 * just a flagged "unknown key" placeholder.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO OPENS THE LINGUISTIC DATABASE AND LOOKS UP THE KEY ─────────────────
    // C-3PO receives a tagged message — say, "AttributeDialog.SelectAttributeTypeOrOID"
    // — and searches his database for the matching plain-language phrase.  If he
    // finds it, he reads it aloud.  If the key is missing from the manifest, he
    // raises an eyebrow and wraps the key in "!" marks so the crew knows there's
    // a gap to fill.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a localised string from the resource bundle by key.
     * If the key exists we return the translated string; if it's missing
     * (perhaps a typo or an entry not yet added to the properties file)
     * we return {@code !key!} so it's immediately obvious in the UI that
     * something is wrong — silent failures would be much harder to spot.
     *
     * <p>For example — C-3PO at the console:</p>
     * <pre>
     *   getString("AttributeDialog.SelectAttributeTypeOrOID")
     *   // returns: "Select Attribute Type or OID"
     *
     *   getString("NoSuchKey")
     *   // returns: "!NoSuchKey!"  (flag for developers to notice)
     * </pre>
     *
     * @param key  The NLS message key as defined in {@code messages.properties}.
     *             Must not be null.
     * @return     The localised string for the current locale, or {@code !key!}
     *             if the key has no entry in the bundle.
     */
    public static String getString( String key )
    {
        try
        {
            return RESOURCE_BUNDLE.getString( key );
        }
        catch ( MissingResourceException e )
        {
            return '!' + key + '!';
        }
    }
}
