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
package org.apache.directory.studio.openldap.config.model;

// ── CLASS: OpenLdapConfigFormat — C-3PO Selecting the Right Protocol Dialect ─
// When C-3PO arrives on Tatooine he quickly recognises whether Jawas are using
// their guttural native dialect or a trade-pidgin he knows better — and switches
// modes accordingly so he can actually read what they're saying.
// OpenLDAP can store its config in two formats: the old flat slapd.conf file
// (STATIC) or the modern cn=config directory tree (DYNAMIC). This enum lets us
// declare which dialect we're working with so the I/O layer reads/writes correctly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Identifies which on-disk format an OpenLDAP configuration uses.
 * STATIC means the classic single slapd.conf text file; DYNAMIC means the
 * live cn=config LDAP directory tree (slapd.d).
 * Think of this as C-3PO checking which protocol dialect is in use before
 * attempting to read or write the configuration.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum OpenLdapConfigFormat
{
    STATIC,         // slapd.d
    DYNAMIC;        // slapd.conf
}
