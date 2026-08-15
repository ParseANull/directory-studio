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
package org.apache.directory.studio.ldapbrowser.core.model.schema;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO'S PHRASE BOOK FOR THE MODEL.SCHEMA PACKAGE ──────
// C-3PO is fluent in over six million forms of communication — the schema
// package needs him to translate message keys into human-readable strings.
// Messages wraps the resource bundle for the model.schema package,
// returning the localised string for any key or a !key! sentinel on miss.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Loads localised strings from the {@code messages.properties} resource bundle
 * for the {@code model.schema} package.  Returns a {@code !key!} sentinel
 * string when a key is missing rather than propagating an exception.
 *
 * <p>Think of this as C-3PO's phrase book for the schema layer — any key that
 * isn't in the book gets a polite "I don't have that one" marker.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Translates A Message Key Into A Localised String ──────────────────
    // C-3PO consults his phrase book: if the key exists, he speaks the translation.
    // If the page is missing — MissingResourceException — he returns "!key!" so
    // the UI still shows something recognisable instead of crashing.
    // Han would shoot the exception; C-3PO politely wraps it in punctuation.
    // All callers in the schema package route through this single lookup point.
    /**
     * Get back a message from the resource file given a key.
     *
     * @param key The key associated with the message
     * @return The found message, or {@code !key!} if the key is missing
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
