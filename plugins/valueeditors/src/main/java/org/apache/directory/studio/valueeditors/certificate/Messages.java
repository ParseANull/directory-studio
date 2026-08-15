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
package org.apache.directory.studio.valueeditors.certificate;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — THE IMPERIAL VERIFICATION OFFICE PHRASE BOOK ────────────
// Before any ship docks at an Imperial facility, the verification officer checks
// his phrase book for the exact wording of every challenge — "Present your seal",
// "Certificate invalid", "Unable to read credentials".  The book is indexed by
// short codes so the officer never improvises (and never goes silent).
// This class is that phrase book for the certificate sub-package.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Looks up localised UI strings for the certificate value-editor sub-package
 * from its {@code messages.properties} resource bundle.
 * Every error message and button label in the {@code CertificateDialog} and
 * {@code CertificateValueEditor} comes through here.
 * Think of this class as the Imperial verification officer's phrase book —
 * every challenge and error response is indexed and consistent.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── The Officer Looks Up the Challenge Phrase ─────────────────────────────
    // The Imperial verification officer checks his phrase book for the exact
    // wording of a "cannot parse certificate" error before speaking it aloud.
    // If the code isn't in the book he flags it openly — "!key!" — so nobody
    // assumes silence means success.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string from the certificate sub-package's resource bundle by key.
     * Returns the localised text if found; returns {@code !key!} if the key is
     * missing so that absent translations are immediately visible in the UI.
     *
     * <p>For example — the officer retrieves an error message:</p>
     * <pre>
     *   String msg = Messages.getString("CertificateDialog.CantParseCertificate");
     *   // → "Cannot parse the certificate data."
     * </pre>
     *
     * @param key  The property key as defined in messages.properties.
     * @return     The localised string, or {@code !key!} if the key is not found.
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
