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


import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;


// ── CLASS: BrowserCoreConstants — LANDO'S CLOUD CITY OPERATIONS BOARD ────────
// Lando Calrissian runs Cloud City from a central operations board that lists
// every protocol, every frequency, every docking bay designation — all the
// canonical names and codes that keep the city running smoothly.
// This class is that board: every magic string and integer the browser core
// needs is defined here once so nothing is hardcoded anywhere else.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Holds every named constant used across the Browser Core plugin.
 * Centralizing constants here means we never scatter magic strings or numbers
 * through the codebase — change one value here and it propagates everywhere.
 * Think of this class as Lando's operations board: the single authoritative
 * source for every code name and setting in Cloud City.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class BrowserCoreConstants
{
    // ── Lando Seals The Operations Board ────────────────────────────────────────
    // Lando locks the operations board so no uninvited administrator can tamper
    // with the canonical codes — "These settings are not up for negotiation."
    // We do the same: a private constructor blocks instantiation because this
    // class is purely a namespace for constants, not an object to be created.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Prevents instantiation of this utility-constants class.
     * There is nothing to construct here — every constant is static.
     * We follow the private-constructor pattern to make that intention explicit
     * and to satisfy tools like PMD that flag implicit public constructors.
     *
     * <p>For example — Lando tells his staff:</p>
     * <pre>
     *   "Nobody touches the operations board directly.
     *    You look at the readouts; you don't own the console."
     * </pre>
     */
    private BrowserCoreConstants()
    {
    }

    /** The plug-in ID */
    public static final String PLUGIN_ID = BrowserCoreConstants.class.getPackage().getName();

    public static final String PREFERENCE_BINARY_SYNTAXES = "binarySyntaxes"; //$NON-NLS-1$

    public static final String PREFERENCE_BINARY_ATTRIBUTES = "binaryAttributes"; //$NON-NLS-1$

    public static final String PREFERENCE_OBJECT_CLASS_ICONS = "objectClassIcons"; //$NON-NLS-1$

    public static final String BINARY = "BINARY"; //$NON-NLS-1$

    public static final String LINE_SEPARATOR = System.getProperty( "line.separator" ); //$NON-NLS-1$

    public static final String DEFAULT_ENCODING = new OutputStreamWriter( new ByteArrayOutputStream() ).getEncoding();

    public static final String PREFERENCE_CHECK_FOR_CHILDREN = "checkForChildren"; //$NON-NLS-1$

    public static final String PREFERENCE_FORMAT_CSV_ATTRIBUTEDELIMITER = "formatCsvAttributeDelimiter"; //$NON-NLS-1$

    public static final String PREFERENCE_FORMAT_CSV_VALUEDELIMITER = "formatCsvValueDelimiter"; //$NON-NLS-1$

    public static final String PREFERENCE_FORMAT_CSV_QUOTECHARACTER = "formatCsvQuoteCharacter"; //$NON-NLS-1$

    public static final String PREFERENCE_FORMAT_CSV_LINESEPARATOR = "formatCsvLineSeparator"; //$NON-NLS-1$

    public static final String PREFERENCE_FORMAT_CSV_BINARYENCODING = "formatCsvBinaryEncoding"; //$NON-NLS-1$

    public static final String PREFERENCE_FORMAT_CSV_ENCODING = "formatCsvEncoding"; //$NON-NLS-1$

    public static final String PREFERENCE_FORMAT_XLS_VALUEDELIMITER = "formatXlsValueDelimiter"; //$NON-NLS-1$

    public static final String PREFERENCE_FORMAT_XLS_BINARYENCODING = "formatXlsBinaryEncoding"; //$NON-NLS-1$

    public static final String PREFERENCE_FORMAT_ODF_VALUEDELIMITER = "formatOdfValueDelimiter"; //$NON-NLS-1$

    public static final String PREFERENCE_FORMAT_ODF_BINARYENCODING = "formatOdfBinaryEncoding"; //$NON-NLS-1$

    public static final String PREFERENCE_LDIF_LINE_WIDTH = "ldifLineWidth"; //$NON-NLS-1$

    public static final String PREFERENCE_LDIF_LINE_SEPARATOR = "ldifLineSeparator"; //$NON-NLS-1$

    public static final String PREFERENCE_LDIF_SPACE_AFTER_COLON = "ldifSpaceAfterColon"; //$NON-NLS-1$

    public static final String PREFERENCE_LDIF_INCLUDE_VERSION_LINE = "ldifIncludeVersionLine"; //$NON-NLS-1$

    public static final int BINARYENCODING_IGNORE = 0;

    public static final int BINARYENCODING_BASE64 = 1;

    public static final int BINARYENCODING_HEX = 2;

    public static final int SORT_BY_NONE = 0;

    public static final int SORT_BY_RDN = 1;

    public static final int SORT_BY_RDN_VALUE = 2;

    public static final int SORT_BY_ATTRIBUTE_DESCRIPTION = 3;

    public static final int SORT_BY_VALUE = 4;

    public static final int SORT_ORDER_NONE = 0;

    public static final int SORT_ORDER_ASCENDING = 1;

    public static final int SORT_ORDER_DESCENDING = 2;

    public static final String LDAP_SEARCH_PAGE_ID = "org.apache.directory.studio.ldapbrowser.ui.search.SearchPage"; //$NON-NLS-1$
}
