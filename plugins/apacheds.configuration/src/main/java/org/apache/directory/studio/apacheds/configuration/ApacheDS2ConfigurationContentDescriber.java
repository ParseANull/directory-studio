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
package org.apache.directory.studio.apacheds.configuration;


import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.server.constants.ServerDNConstants;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.ITextContentDescriber;


// ── CLASS: ApacheDS2ConfigurationContentDescriber — THE IMPERIAL FILE SNIFF DETECTOR ────────
// Before Eclipse opens a file in the configuration editor, a scout reads the first few
// entries of the LDIF file to check whether it really is an ApacheDS configuration file.
// The scout looks for two specific DNs: "ou=config" and "ads-directoryServiceId=default,ou=config".
// If both appear within the first ten entries, the file is declared VALID and Eclipse routes it
// to the configuration editor.  If not, it's INVALID and gets a generic LDIF editor.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse content describer for ApacheDS 2.x server configuration LDIF files.
 * Registered in plugin.xml to trigger on {@code *.ldif} files.
 * Reads up to {@value #MAX_NUMBER_ENTRIES_SEARCH} entries and checks for the {@code ou=config}
 * and {@code ads-directoryServiceId=default,ou=config} DNs.
 * Returns {@link ITextContentDescriber#VALID} only when both are found.
 * Think of it as the Imperial file-sniff detector: it reads the first few lines to decide
 * whether this file belongs to the Death Star engineering wing.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ApacheDS2ConfigurationContentDescriber implements ITextContentDescriber
{
    /** The maximum number of entries to search before determining the file as invalid */
    private static final int MAX_NUMBER_ENTRIES_SEARCH = 10;

    /** The Dn of the config entry ('ou=config')*/
    private Dn configEntryDn;

    /** The Dn of the directory service entry ('ads-directoryServiceId=default,ou=config') */
    private Dn directoryServiceDn;


    // ── The Scout Pre-Loads The Target DNs ───────────────────────────────────────────────────
    // The scout memorises what to look for — "ou=config" and "ads-directoryServiceId=default,ou=config"
    // — before scanning any file.  If the DN construction fails (it won't), we just get null DNs.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the two target DNs that identify a valid configuration file.
     * DN construction will never throw in practice since both strings are static and valid.
     */
    public ApacheDS2ConfigurationContentDescriber()
    {
        // Initializing DNs
        try
        {
            configEntryDn = new Dn( ServerDNConstants.CONFIG_DN ); //$NON-NLS-1$
            directoryServiceDn = new Dn( "ads-directoryServiceId=default,ou=config" ); //$NON-NLS-1$
        }
        catch ( LdapInvalidDnException e )
        {
            // Will never occur.
        }
    }


    // ── Sniffing A Character-Based Reader ────────────────────────────────────────────────────
    // Eclipse passes us a Reader when it has already decoded the file bytes.
    // We wrap it in a LdifReader and delegate to isValid().
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Determines whether the text read from {@code contents} is a valid ApacheDS 2.x configuration.
     * Wraps the reader in a {@link LdifReader} and delegates to {@link #isValid(LdifReader)}.
     *
     * @param contents     the character stream to inspect
     * @param description  ignored (we don't set any content properties)
     * @return {@link ITextContentDescriber#VALID} or {@link ITextContentDescriber#INVALID}
     * @throws IOException if the reader throws
     */
    public int describe( Reader contents, IContentDescription description ) throws IOException
    {
        LdifReader reader = null;

        try
        {
            reader = new LdifReader( contents );

            return isValid( reader );
        }
        catch ( LdapException e )
        {
            return ITextContentDescriber.INVALID;
        }
        finally
        {
            if ( reader != null )
            {
                reader.close();
            }
        }
    }


    // ── Sniffing A Raw Byte Stream ────────────────────────────────────────────────────────────
    // Eclipse passes us an InputStream when it hasn't decoded the bytes yet.
    // Same as above — wrap and delegate.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Determines whether the bytes read from {@code contents} are a valid ApacheDS 2.x configuration.
     * Wraps the stream in a {@link LdifReader} and delegates to {@link #isValid(LdifReader)}.
     *
     * @param contents     the byte stream to inspect
     * @param description  ignored
     * @return {@link ITextContentDescriber#VALID} or {@link ITextContentDescriber#INVALID}
     * @throws IOException if the stream throws
     */
    public int describe( InputStream contents, IContentDescription description ) throws IOException
    {
        LdifReader reader = null;

        try
        {
            reader = new LdifReader( contents );

            return isValid( reader );
        }
        catch ( LdapException e )
        {
            return ITextContentDescriber.INVALID;
        }
        finally
        {
            if ( reader != null )
            {
                reader.close();
            }
        }
    }


    // ── Reporting Supported Content Properties ────────────────────────────────────────────────
    // We don't extract any additional content properties — just VALID/INVALID.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an empty array — we don't set any Eclipse content description properties.
     *
     * @return an empty {@link QualifiedName} array
     */
    public QualifiedName[] getSupportedOptions()
    {
        return new QualifiedName[0];
    }


    // ── Scanning The LDIF For The Two Required DNs ────────────────────────────────────────────
    // The scout reads up to 10 entries and checks each DN.  As soon as both targets are found
    // we exit early and declare VALID.  If we run out of entries without finding both, INVALID.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads up to {@value #MAX_NUMBER_ENTRIES_SEARCH} entries from the {@link LdifReader}
     * and checks for the presence of both the {@code ou=config} and
     * {@code ads-directoryServiceId=default,ou=config} DNs.
     * Returns {@link ITextContentDescriber#VALID} if both are found; otherwise {@link ITextContentDescriber#INVALID}.
     *
     * <p>For example — a valid config.ldif:</p>
     * <pre>
     *   Entry 1: dn: ou=config         → configEntryFound = true
     *   Entry 2: dn: ads-directoryServiceId=default,ou=config → directoryServiceEntryFound = true
     *   → both found → return VALID
     * </pre>
     *
     * @param reader  the LDIF reader positioned at the start of the file
     * @return VALID or INVALID
     */
    private int isValid( LdifReader reader )
    {
        int checkedEntries = 0;
        boolean configEntryFound = false;
        boolean directoryServiceEntryFound = false;

        while ( reader.hasNext() && ( checkedEntries < MAX_NUMBER_ENTRIES_SEARCH ) )
        {
            if ( configEntryFound && directoryServiceEntryFound )
            {
                // Getting out of the loop if we found both entries
                break;
            }

            LdifEntry entry = reader.next();
            checkedEntries++;

            // Checking if this is the config entry
            if ( ( !configEntryFound ) &&
                 ( configEntryDn.getName().equalsIgnoreCase( entry.getDn().getNormName() ) ) )
            {
                configEntryFound = true;
                continue;
            }

            // Checking if this is the directory service entry
            if ( ( !directoryServiceEntryFound ) &&
                 ( directoryServiceDn.getName().equalsIgnoreCase( entry.getDn().getNormName() ) ) )
            {
                directoryServiceEntryFound = true;
                continue;
            }
        }

        // Checking if we found both entries
        if ( configEntryFound && directoryServiceEntryFound )
        {
            return ITextContentDescriber.VALID;
        }
        else
        {
            return ITextContentDescriber.INVALID;
        }
    }
}
