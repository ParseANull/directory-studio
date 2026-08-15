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


// ── CLASS: AuxiliaryObjectClass — THE DEATH STAR SCHEMATICS COVER PAGE ────────
// Princess Leia hides the Death Star plans in R2-D2 — but what makes those plans
// *plans* is the structural contract they represent: any holder of these schematics
// is playing the same role. This interface is that cover page — a pure marker that
// says "objects implementing me carry auxiliary LDAP object-class data."
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Marker interface for auxiliary LDAP object classes in the OpenLDAP config model.
 * Any config bean that can carry auxiliary object-class attributes (extra schema
 * mix-ins layered on top of a structural class) implements this so the rest of the
 * system knows it's a blueprint carrier.
 * Think of this interface as the cover sheet on the Death Star schematics — no
 * content of its own, just a declaration that says "this object holds the plans."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface AuxiliaryObjectClass
{
}
