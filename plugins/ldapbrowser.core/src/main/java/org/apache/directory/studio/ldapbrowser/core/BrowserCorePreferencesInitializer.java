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

package org.apache.directory.studio.ldapbrowser.core;


import org.apache.directory.studio.ldapbrowser.core.model.schema.BinaryAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.schema.BinarySyntax;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;


// ── CLASS: BrowserCorePreferencesInitializer — FOUNDING MASTER WRITES THE JEDI CODEX ──
// When the Jedi Order is founded, the very first Grand Master writes the
// canonical rules into the Jedi Codex before any apprentice has been trained
// or any mission flown.  These are the defaults that the rest of the Order
// falls back to if no one has explicitly overridden them.
// This class does exactly that for the browser-core plugin: Eclipse calls it
// once at startup (via the {@code org.eclipse.core.runtime.preferences}
// extension point) so we can register all the default preference values
// before any UI page or model code tries to read them.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Sets up the default preference values for the browser-core plugin.
 * Eclipse's preference framework calls {@link #initializeDefaultPreferences()}
 * exactly once, before any user code reads the preferences.  We use it to
 * register sensible defaults for CSV/XLS/ODF/LDIF export settings and the
 * canonical lists of binary attributes and binary syntaxes that ships with
 * Apache Directory Studio out of the box.
 * Think of this class as the founding Jedi Grand Master writing the defaults
 * into the Codex before the Order opens for business.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserCorePreferencesInitializer extends AbstractPreferenceInitializer
{
    // ── The Founding Grand Master Writes The Codex ───────────────────────────────
    // The first Grand Master sits at the writing desk in the Jedi Temple and
    // inscribes every foundational rule: which syntaxes are physical, which
    // artefacts must be handled as binary cargo, what the standard line separator
    // is for transmitted scrolls, and so on.
    // Eclipse calls this method once at startup — we write every default into
    // the Eclipse preference store so any later read that hasn't been explicitly
    // overridden by the user gets the right built-in value.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Registers all default preference values for the browser-core plugin.
     * Eclipse calls this exactly once, before the first preference read.
     * We cover:
     * <ul>
     *   <li>Whether to check for children when expanding a tree node.</li>
     *   <li>CSV, XLS, ODF, and LDIF export formatting defaults.</li>
     *   <li>The canonical list of binary LDAP attribute types (like
     *       {@code jpegPhoto}, {@code userCertificate}).</li>
     *   <li>The canonical list of binary LDAP syntaxes (like Certificate,
     *       Octet String).</li>
     * </ul>
     *
     * <p>For example — the Grand Master's inscription:</p>
     * <pre>
     *   store.setDefault(PREFERENCE_FORMAT_CSV_ATTRIBUTEDELIMITER, ",");
     *   prefs.setDefaultBinaryAttributes(new BinaryAttribute[]{ ... });
     *   prefs.setDefaultBinarySyntaxes(new BinarySyntax[]{ ... });
     * </pre>
     */
    @Override
    public void initializeDefaultPreferences()
    {
        Preferences store = BrowserCorePlugin.getDefault().getPluginPreferences();

        store.setDefault( BrowserCoreConstants.PREFERENCE_CHECK_FOR_CHILDREN, true );

        store.setDefault( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_ATTRIBUTEDELIMITER, "," ); //$NON-NLS-1$
        store.setDefault( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_VALUEDELIMITER, "|" ); //$NON-NLS-1$
        store.setDefault( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_QUOTECHARACTER, "\"" ); //$NON-NLS-1$
        store
            .setDefault( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_LINESEPARATOR, BrowserCoreConstants.LINE_SEPARATOR );
        store.setDefault( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_BINARYENCODING,
            BrowserCoreConstants.BINARYENCODING_IGNORE );
        store.setDefault( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_ENCODING, BrowserCoreConstants.DEFAULT_ENCODING );

        store.setDefault( BrowserCoreConstants.PREFERENCE_FORMAT_XLS_VALUEDELIMITER, "|" ); //$NON-NLS-1$
        store.setDefault( BrowserCoreConstants.PREFERENCE_FORMAT_XLS_BINARYENCODING,
            BrowserCoreConstants.BINARYENCODING_IGNORE );

        store.setDefault( BrowserCoreConstants.PREFERENCE_FORMAT_ODF_VALUEDELIMITER, "|" ); //$NON-NLS-1$
        store.setDefault( BrowserCoreConstants.PREFERENCE_FORMAT_ODF_BINARYENCODING,
            BrowserCoreConstants.BINARYENCODING_IGNORE );

        store.setDefault( BrowserCoreConstants.PREFERENCE_LDIF_LINE_WIDTH, 76 );
        store.setDefault( BrowserCoreConstants.PREFERENCE_LDIF_LINE_SEPARATOR, BrowserCoreConstants.LINE_SEPARATOR );
        store.setDefault( BrowserCoreConstants.PREFERENCE_LDIF_SPACE_AFTER_COLON, true );
        store.setDefault( BrowserCoreConstants.PREFERENCE_LDIF_INCLUDE_VERSION_LINE, true );

        // default binary attributes
        BinaryAttribute[] defaultBinaryAttributes = new BinaryAttribute[]
            { new BinaryAttribute( "0.9.2342.19200300.100.1.7" ), // photo //$NON-NLS-1$
                new BinaryAttribute( "0.9.2342.19200300.100.1.53" ), // personalSignature //$NON-NLS-1$
                new BinaryAttribute( "0.9.2342.19200300.100.1.55" ), // audio //$NON-NLS-1$
                new BinaryAttribute( "0.9.2342.19200300.100.1.60" ), // jpegPhoto //$NON-NLS-1$
                new BinaryAttribute( "1.3.6.1.4.1.42.2.27.4.1.8" ), // javaSerializedData //$NON-NLS-1$
                new BinaryAttribute( "1.3.6.1.4.1.1466.101.120.35" ), // thumbnailPhoto //$NON-NLS-1$
                new BinaryAttribute( "1.3.6.1.4.1.1466.101.120.36" ), // thumbnailLogo //$NON-NLS-1$
                new BinaryAttribute( "2.5.4.35" ), // userPassword //$NON-NLS-1$
                new BinaryAttribute( "2.5.4.36" ), // userCertificate //$NON-NLS-1$
                new BinaryAttribute( "2.5.4.37" ), // cACertificate //$NON-NLS-1$
                new BinaryAttribute( "2.5.4.38" ), // authorityRevocationList //$NON-NLS-1$
                new BinaryAttribute( "2.5.4.39" ), // certificateRevocationList //$NON-NLS-1$
                new BinaryAttribute( "2.5.4.40" ), // crossCertificatePair //$NON-NLS-1$
                new BinaryAttribute( "2.5.4.45" ), // x500UniqueIdentifier //$NON-NLS-1$
                new BinaryAttribute( "1.2.840.113556.1.4.2" ), // objectGUID //$NON-NLS-1$
                new BinaryAttribute( "1.2.840.113556.1.4.146" ), // objectSid //$NON-NLS-1$
            };
        BrowserCorePlugin.getDefault().getCorePreferences().setDefaultBinaryAttributes( defaultBinaryAttributes );

        // default binary syntaxes
        BinarySyntax[] defaultBinarySyntaxes = new BinarySyntax[]
            { new BinarySyntax( "1.3.6.1.4.1.1466.115.121.1.5" ), // Binary //$NON-NLS-1$
                new BinarySyntax( "1.3.6.1.4.1.1466.115.121.1.8" ), // Certificate //$NON-NLS-1$
                new BinarySyntax( "1.3.6.1.4.1.1466.115.121.1.9" ), // Certificate List //$NON-NLS-1$
                new BinarySyntax( "1.3.6.1.4.1.1466.115.121.1.10" ), // Certificate Pair //$NON-NLS-1$
                new BinarySyntax( "1.3.6.1.4.1.1466.115.121.1.23" ), // Fax //$NON-NLS-1$
                new BinarySyntax( "1.3.6.1.4.1.1466.115.121.1.28" ), // JPEG //$NON-NLS-1$
                new BinarySyntax( "1.3.6.1.4.1.1466.115.121.1.40" ), // Octet String //$NON-NLS-1$
                new BinarySyntax( "1.3.6.1.4.1.1466.115.121.1.49" ), // Supported Algorithm //$NON-NLS-1$
                new BinarySyntax( "1.3.6.1.1.16.1" ), // UUID //$NON-NLS-1$
                new BinarySyntax( "2.5.5.10" ), // MS-specific: Octet,Replica-Link //$NON-NLS-1$
            };
        BrowserCorePlugin.getDefault().getCorePreferences().setDefaultBinarySyntaxes( defaultBinarySyntaxes );
    }
}
