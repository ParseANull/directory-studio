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
package org.apache.directory.studio.schemaeditor.model;


// ── CLASS: ProjectType — Palpatine's Imperial Operating Permits ───────────────
// The Emperor classifies every operation in the galaxy: either it's an isolated
// outpost (OFFLINE — no outside comms, works from local files) or a fully
// networked Imperial installation connected to the central directory (ONLINE —
// live link to an LDAP server). There's no middle ground; every project must
// declare which kind it is.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Enum that classifies a Project as either working entirely from local files
 * (OFFLINE) or fetching its schemas from a live LDAP server (ONLINE).
 * Think of this as Palpatine's operating permits: every project must declare
 * which class of operation it is — isolated outpost or networked installation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum ProjectType
{
    /** A schema project not linked to any LDAP Server */
    OFFLINE,
    /** A schema project linked to a Directory Server */
    ONLINE
}
