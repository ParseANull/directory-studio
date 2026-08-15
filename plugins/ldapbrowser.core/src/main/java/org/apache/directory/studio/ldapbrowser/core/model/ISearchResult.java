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


import java.io.Serializable;

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.connection.core.ConnectionPropertyPageProvider;
import org.apache.directory.studio.ldapbrowser.core.propertypageproviders.EntryPropertyPageProvider;
import org.eclipse.core.runtime.IAdaptable;


// ── CLASS: ISearchResult — ONE DROID THE JAWAS WHEELED OUT OF THE SAND CRAWLER
// After C-3PO delivers the search mission brief to the Jawas, they start
// wheeling out droids one by one.  Each droid that emerges is a single search
// result: it has a serial number (DN), it has attributes (model, condition,
// price), and it can be traced back to the mission that ordered it.
// ISearchResult is one of those droids: a thin wrapper pairing an {@link IEntry}
// (already in the browser cache) with the {@link ISearch} that found it, plus
// a snapshot of the attributes that the search returned for it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents a single LDAP entry returned by an {@link ISearch}.
 * Each search result wraps an {@link IEntry} from the browser cache and holds
 * the subset of attributes that the search requested (which may be a partial
 * view of the entry's full attribute set).
 * Think of this as one droid wheeled out by the Jawas: it has an identity
 * (DN), the data the search asked for (attributes), and a pointer back to the
 * search that found it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ISearchResult extends Serializable, IAdaptable, EntryPropertyPageProvider,
    ConnectionPropertyPageProvider
{
    // ── The Jawa Reads Out The Droid's Serial Number ──────────────────────────────
    // "This one: R2-D2, serial WED-15-1662."  The DN uniquely identifies the entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the distinguished name (DN) of the entry this search result represents.
     *
     * @return the {@link Dn}; never {@code null}.
     */
    Dn getDn();


    // ── The Jawa Lists The Droid's Returned Specs ─────────────────────────────────
    // The search asked for certain attributes — model, condition, price.
    // Only those come back in the result; other attributes may exist on the entry
    // but weren't requested.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attributes included in this search result.
     * Only the attributes specified in the search's returning-attributes list
     * are present here; the entry's full attribute set may be larger.
     *
     * @return the result attributes; may be empty, never {@code null}.
     */
    IAttribute[] getAttributes();


    // ── The Jawa Fetches One Named Spec ───────────────────────────────────────────
    // "Show me just the condition attribute."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute with the given description from this search result,
     * or {@code null} if it isn't present in the result.
     *
     * @param attributeDescription the attribute type name (e.g. {@code "mail"}).
     * @return the matching {@link IAttribute}, or {@code null}.
     */
    IAttribute getAttribute( String attributeDescription );


    // ── The Jawa Fetches A Whole Spec Family ──────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an {@link AttributeHierarchy} for the given attribute type and all
     * of its sub-types present in this search result.
     *
     * @param attributeDescription the attribute type name.
     * @return the hierarchy, or {@code null} if neither the type nor any subtype exists.
     */
    AttributeHierarchy getAttributeWithSubtypes( String attributeDescription );


    // ── The Jawa Points To The Droid In The Inventory ────────────────────────────
    // The search result wraps a cached {@link IEntry}; this gives direct access.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IEntry} from the browser cache that this search result
     * corresponds to.
     *
     * @return the entry; never {@code null}.
     */
    IEntry getEntry();


    // ── The Jawa Points Back To The Mission That Found This Droid ────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ISearch} that produced this result.
     *
     * @return the parent search; never {@code null}.
     */
    ISearch getSearch();


    // ── The Jawa Links This Droid To Its Mission ──────────────────────────────────
    // Called when the search job wires up results to their owning search.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the parent search for this result.
     * Called by the search job after it has collected all results.
     *
     * @param search the parent search; must not be {@code null}.
     */
    void setSearch( ISearch search );
}
