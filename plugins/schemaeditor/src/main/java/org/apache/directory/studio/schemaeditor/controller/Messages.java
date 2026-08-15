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
package org.apache.directory.studio.schemaeditor.controller;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING FOR R2-D2 ───────────────────────────
// Aboard the Tantive IV, C-3PO listens to R2-D2's frantic beeps and chirps
// and converts them into plain-English sentences the crew can act on.
// This class does the same: it takes a raw message key (the "beep") and
// returns the human-readable string from our resource bundle (the translation).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class for resolving localized message strings in the controller package.
 * We look up each message by key from the {@code messages.properties} resource
 * bundle sitting next to this class on the classpath.
 * Think of this class as C-3PO — we speak the language of both the key-based
 * internal world and the human-readable UI strings, bridging the two.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Decodes R2's Beep ──────────────────────────────────────────────
    // R2-D2 whistles a four-note pattern; C-3PO immediately recognizes it and
    // says "He says the odds of successfully navigating an asteroid field..."
    // This method takes the raw key (R2's beep) and returns the translated
    // message (C-3PO's explanation) — or wraps the key in bangs if it's unknown.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized string for the given message key.
     * We look it up in the resource bundle; if the key doesn't exist we return
     * {@code !key!} rather than blowing up — this makes missing strings obvious
     * in the UI without crashing the whole plugin.
     *
     * <p>For example — C-3PO always has a translation ready:</p>
     * <pre>
     *   Messages.getString( "HierarchyViewController.Error" )
     *   // returns "Error" in the current locale, or "!HierarchyViewController.Error!"
     *   //   if the key is missing from messages.properties
     * </pre>
     *
     * @param key  the message key as defined in messages.properties; must not be null
     * @return     the localized message, or {@code !key!} if the key is absent
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
