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

package org.apache.directory.studio.ldapbrowser.core.propertypageproviders;


// ── CLASS: EntryPropertyPageProvider — JEDI BADGE FOR ENTRY OBJECTS ─────────
// Every room on the Death Star blueprint needs a door clearance badge before
// the inspection panel opens.  EntryPropertyPageProvider is that clearance badge
// for IEntry objects: implementing it signals to the Eclipse property-page
// framework that this object should expose entry property pages.
// No methods — the interface is a pure marker.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Marker interface that tags a class as a provider of entry property pages.
 *
 * <p>Think of this as a Death Star door clearance badge: an object implementing
 * this interface announces "I am an LDAP entry — show my property pages."</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface EntryPropertyPageProvider
{

}
