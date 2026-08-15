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
package org.apache.directory.studio.common.ui;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING REBEL COMMUNICATIONS ────────────────
// C-3PO is fluent in over six million forms of communication, and this class
// is his translation module for the common.ui plugin.  We look up localizable
// strings from the messages resource bundle so the rest of the code can use
// simple keys without worrying about the actual text or locale.  If a key is
// missing from the bundle, we wrap it in exclamation marks so it is obvious
// something needs fixing.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We provide a single static entry point for retrieving localized strings from
 * this plugin's {@code messages} resource bundle.  Callers pass a key and get
 * back the corresponding translated text.  If the key is not found we return
 * {@code !key!} so missing translations are immediately visible during testing.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── METHOD getString — REQUESTING A TRANSLATION FROM C-3PO ───────────────
    // C-3PO consults his language banks and returns the translated phrase for
    // the given key.  If the phrase is missing from the bundle — perhaps the
    // translation team forgot it — he wraps the key in exclamation marks so
    // nobody misses the gap.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We look up the given {@code key} in our resource bundle and return the
     * corresponding localized string.  If the key is not present we return
     * {@code "!key!"} so the missing entry stands out clearly during
     * development and testing.
     *
     * @param key the message key to look up
     * @return the localized string, or {@code "!key!"} if not found
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
