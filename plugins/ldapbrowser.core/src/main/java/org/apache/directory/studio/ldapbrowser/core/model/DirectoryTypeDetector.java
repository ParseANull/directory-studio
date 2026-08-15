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
package org.apache.directory.studio.ldapbrowser.core.model;


// ── CLASS: DirectoryTypeDetector — MACE WINDU READING THE CHANCELLOR'S TRUE IDENTITY ──
// Mace Windu stands before Chancellor Palpatine and interrogates every detail:
// "What powers are you using?  What do your eyes look like?  What's in the
// Force around you?"  From those observations he renders a verdict: Sith Lord.
// A DirectoryTypeDetector does the same thing with an LDAP server's Root DSE:
// it inspects the capabilities, extensions, and vendor attributes listed there
// and decides "this is Apache Directory Server" or "this is Active Directory".
// Knowing the server type lets us enable server-specific features in the UI.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Strategy interface for detecting the type of LDAP server (e.g. Apache
 * Directory Server, Active Directory, OpenLDAP) from its Root DSE.
 * Implementations inspect the capability attributes and vendor information
 * in the Root DSE and return a human-readable server type string.
 * Think of each implementation as Mace Windu confronting a suspect — it
 * examines the evidence and delivers a verdict.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface DirectoryTypeDetector
{
    // ── Mace Windu Reads The Root DSE And Delivers His Verdict ───────────────────
    // "I have examined the evidence in this Root DSE.  My verdict: Apache
    // Directory Server 2.x."  If the evidence is inconclusive, he says nothing
    // (null).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Tries to detect the directory type from the given Root DSE.
     *
     * <p>For example:</p>
     * <pre>
     *   String type = detector.detectDirectoryType(connection.getRootDSE());
     *   if (type != null) { enableServerSpecificFeatures(type); }
     * </pre>
     *
     * @param rootDSE the Root DSE of the server to inspect.
     * @return a string identifying the directory type (e.g. "Apache Directory
     *         Server"), or {@code null} if this detector does not recognise the server.
     */
    String detectDirectoryType( IRootDSE rootDSE );
}
