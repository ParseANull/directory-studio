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


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Translates for the Crew ─────────────────────────
// Aboard the Millennium Falcon, C-3PO stands at the ready, fluent in over six
// million forms of communication. Whenever Han or Leia need a message rendered
// in the local tongue — "We're all fine here, how are you?" — C-3PO looks it
// up in his vast language banks and delivers it flawlessly. If a phrase isn't
// in his banks, he admits it honestly rather than fabricating a translation.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Simple utility class that fetches localized UI strings from the
 * {@code messages.properties} file sitting in this package. Every user-visible
 * label, button text, and warning message in the entry editor goes through here
 * so we can translate the application without touching Java code.
 *
 * <p>Think of this class as C-3PO: hand him a key (like
 * {@code "OpenBestEditorAction.EditValueQuestion"}) and he hands back the
 * right phrase in the user's language.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Receives a Translation Request and Delivers ───────────────────────
    // Han shouts "Threepio, what does it say?" — C-3PO consults his language banks
    // for the key-phrase in question and reads out the translation without missing
    // a beat. If the phrase isn't in his banks, he admits it by wrapping the key
    // in exclamation marks so the developer can spot the gap immediately.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string by its resource key and returns the localized text. We
     * load strings from the {@code messages.properties} file in this package (and
     * any locale-specific variant the JVM can find). If the key is missing — maybe
     * someone forgot to add the translation — we return {@code !key!} instead of
     * crashing, which makes the gap visible in the UI without breaking anything.
     *
     * <p>For example — C-3PO handles a translation request gracefully:</p>
     * <pre>
     *   Messages.getString("OpenBestEditorAction.EditValueQuestion")
     *     → "Do you really want to edit this value?"   // key found
     *
     *   Messages.getString("SomeForgottenKey")
     *     → "!SomeForgottenKey!"                       // key missing
     * </pre>
     *
     * @param key  The resource bundle key, typically in the form
     *             {@code "ClassName.LabelName"}; must not be {@code null}.
     * @return     The localized string for the given key, or {@code !key!} if the
     *             key isn't present in the bundle.
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
