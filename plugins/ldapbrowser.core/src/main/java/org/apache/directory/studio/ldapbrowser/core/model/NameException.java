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


// ── CLASS: NameException — HAN SHOOTING FIRST AT AN INVALID COORDINATE ───────
// Han Solo doesn't wait for an invalid hyperspace coordinate to destroy the
// Falcon — the moment the navicomp detects garbage input he fires a fault
// exception so the navigator knows immediately: "These coordinates make no sense."
// This exception is the old-style signal for an invalid DN or RDN string.
// It has been superseded by the shared-ldap API classes but still exists to
// keep old {@code browserconnections.xml} files parseable.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that a directory name (DN or RDN) string was syntactically invalid.
 *
 * @deprecated This class will be removed in the next version. The Dn/Rdn/RDNPart
 * classes are replaced with the shared-ldap LdapDN/Rdn/ATAV. This class just
 * remains to provide backward compatibility of the old browserconnections.xml
 * file that stores searches and bookmarks.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NameException extends Exception
{

    private static final long serialVersionUID = 1L;


    // ── Han Reports The Invalid Coordinates ──────────────────────────────────────
    // "These coordinates are garbage — here's what the parser found."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new NameException with the given diagnostic message.
     *
     * <p>For example — thrown by legacy DN parsers:</p>
     * <pre>
     *   throw new NameException("Invalid RDN: missing '=' in 'cn'");
     * </pre>
     *
     * @param message a description of what was invalid about the name string.
     */
    public NameException( String message )
    {
        super( message );
    }

}
