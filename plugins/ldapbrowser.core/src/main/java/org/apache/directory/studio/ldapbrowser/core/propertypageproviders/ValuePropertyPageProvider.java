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


// ── CLASS: ValuePropertyPageProvider — JEDI BADGE FOR VALUE OBJECTS ─────────
// A single data slot on a Death Star blueprint panel holds one value — and that
// value must show its access badge before the detailed inspection panel opens.
// ValuePropertyPageProvider is that badge for IValue objects: implementing it
// tells the Eclipse property-page framework that this object should expose value
// property pages.  No methods — the interface is a pure marker.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Marker interface that tags a class as a provider of value property pages.
 *
 * <p>Think of this as a data-slot access badge: an object implementing this
 * interface announces "I am an LDAP value — show my property pages."</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ValuePropertyPageProvider
{

}
