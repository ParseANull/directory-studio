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
package org.apache.directory.studio.ldapbrowser.core.utils;


import org.eclipse.osgi.util.NLS;


// ── CLASS: Messages — C-3PO'S PHRASE BOOK FOR THE UTILS PACKAGE ──────────────
// C-3PO is fluent in over six million forms of communication — the utils package
// needs him to translate message keys into human-readable strings for file-size
// labels and other utility outputs.  Messages extends Eclipse NLS and provides
// static public fields that are populated from the messages.properties bundle.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse NLS message bundle for the {@code utils} package.
 * Provides localised strings for file-size labels (byte, kilobyte, megabyte)
 * and other utility messages.
 *
 * <p>Think of this as C-3PO's phrase book for the utils layer — each public
 * field is automatically populated from the messages.properties resource.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.apache.directory.studio.ldapbrowser.core.utils.messages"; //$NON-NLS-1$
    public static String Utils_10;
    public static String Utils_Byte;
    public static String Utils_Bytes;
    public static String Utils_KiloBytes;
    public static String Utils_MegaBytes;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages( BUNDLE_NAME, Messages.class );
    }


    private Messages()
    {
    }
}
