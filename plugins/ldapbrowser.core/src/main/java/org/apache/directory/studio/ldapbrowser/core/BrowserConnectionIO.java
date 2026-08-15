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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Controls;
import org.apache.directory.studio.connection.core.StudioControl;
import org.apache.directory.studio.connection.core.io.ConnectionIOException;
import org.apache.directory.studio.ldapbrowser.core.model.BookmarkParameter;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.SearchParameter;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Bookmark;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Search;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.eclipse.osgi.util.NLS;


// ── CLASS: BrowserConnectionIO — R2-D2 READS AND WRITES THE DEATH STAR PLANS ─
// R2-D2 plugs into the Death Star's computer terminal and downloads the full
// station schematics to a local file, then later re-uploads them from that
// file when needed.  He handles all the encoding details — the binary-to-text
// conversion, the slot-by-slot structure of the plans — transparently.
// This class does exactly that for browser connections: it serialises the
// entire set of saved searches, bookmarks, and connection IDs to an XML file
// ({@code browserconnections.xml}) and deserialises them back on startup.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Reads and writes the {@code browserconnections.xml} persistence file that
 * stores all saved searches and bookmarks for each browser connection.
 * We use dom4j for XML parsing and writing, and Base64 for encoding LDAP
 * control values (which are raw bytes).
 * Think of this class as R2-D2 transferring the Death Star plans — he handles
 * the low-level I/O so nobody else has to.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserConnectionIO
{
    // XML tags
    private static final String BROWSER_CONNECTIONS_TAG = "browserConnections"; //$NON-NLS-1$

    private static final String BROWSER_CONNECTION_TAG = "browserConnection"; //$NON-NLS-1$
    private static final String ID_TAG = "id"; //$NON-NLS-1$

    private static final String SEARCHES_TAG = "searches"; //$NON-NLS-1$
    private static final String SEARCH_PARAMETER_TAG = "searchParameter"; //$NON-NLS-1$
    private static final String NAME_TAG = "name"; //$NON-NLS-1$
    private static final String SEARCH_BASE_TAG = "searchBase"; //$NON-NLS-1$
    private static final String FILTER_TAG = "filer"; //$NON-NLS-1$
    private static final String RETURNING_ATTRIBUTES_TAG = "returningAttributes"; //$NON-NLS-1$
    private static final String RETURNING_ATTRIBUTE_TAG = "returningAttribute"; //$NON-NLS-1$
    private static final String VALUE_TAG = "value"; //$NON-NLS-1$
    private static final String SCOPE_TAG = "scope"; //$NON-NLS-1$
    private static final String TIME_LIMIT_TAG = "timeLimit"; //$NON-NLS-1$
    private static final String COUNT_LIMIT_TAG = "countLimit"; //$NON-NLS-1$
    private static final String ALIASES_DEREFERENCING_METHOD_TAG = "aliasesDereferencingMethod"; //$NON-NLS-1$
    private static final String REFERRALS_HANDLING_METHOD_TAG = "referralsHandlingMethod"; //$NON-NLS-1$
    private static final String PAGED_SEARCH_SCROLL_MODE = "pagedSearchScrollMode"; //$NON-NLS-1$
    private static final String CONTROLS_TAG = "controls"; //$NON-NLS-1$
    private static final String CONTROL_TAG = "control"; //$NON-NLS-1$
    private static final String OID_TAG = "oid"; //$NON-NLS-1$
    private static final String IS_CRITICAL_TAG = "isCritical"; //$NON-NLS-1$

    private static final String BOOKMARKS_TAG = "bookmarks"; //$NON-NLS-1$
    private static final String BOOKMARK_PARAMETER_TAG = "bookmarkParameter"; //$NON-NLS-1$
    private static final String DN_TAG = "dn"; //$NON-NLS-1$

    // Scope values
    private static final String SCOPE_OBJECT = "OBJECT"; //$NON-NLS-1$
    private static final String SCOPE_ONELEVEL = "ONELEVEL"; //$NON-NLS-1$
    private static final String SCOPE_SUBTREE = "SUBTREE"; //$NON-NLS-1$
    private static final String SCOPE_OBJECT_2 = "base"; //$NON-NLS-1$
    private static final String SCOPE_ONELEVEL_2 = "one"; //$NON-NLS-1$
    private static final String SCOPE_SUBTREE_2 = "sub"; //$NON-NLS-1$


    // ── R2-D2 Downloads The Station Plans From Storage ───────────────────────────
    // R2-D2 opens the storage socket, reads the raw data stream, and
    // reconstructs the full set of Death Star schematics in memory.
    // If the storage is corrupt or in an unrecognised format, he beeps an error.
    // We parse the XML stream and populate the connection map with saved searches
    // and bookmarks; a malformed document triggers a ConnectionIOException.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code browserconnections.xml} input stream and populates the
     * given connection map with saved searches and bookmarks.
     * The stream is expected to contain a valid {@code <browserConnections>}
     * XML document.  Any connection ID in the XML that doesn't match a known
     * connection in the map is silently skipped (the connection may have been
     * deleted since the file was written).
     *
     * <p>For example — R2-D2 downloading the plans:</p>
     * <pre>
     *   BrowserConnectionIO.load(new FileInputStream("browserconnections.xml"), connectionMap);
     *   // connectionMap now has searches and bookmarks populated
     * </pre>
     *
     * @param stream               the XML input stream to parse; must be a valid
     *                             {@code browserconnections.xml} document.
     * @param browserConnectionMap the live map of connection IDs to
     *                             {@link IBrowserConnection} objects to populate.
     * @throws ConnectionIOException if the XML cannot be parsed or the root
     *                               element is not {@code <browserConnections>}.
     */
    public static void load( InputStream stream, Map<String, IBrowserConnection> browserConnectionMap )
        throws ConnectionIOException
    {
        SAXReader saxReader = new SAXReader();
        Document document = null;

        try
        {
            document = saxReader.read( stream );
        }
        catch ( DocumentException e )
        {
            throw new ConnectionIOException( e.getMessage() );
        }

        Element rootElement = document.getRootElement();
        if ( !rootElement.getName().equals( BROWSER_CONNECTIONS_TAG ) )
        {
            throw new ConnectionIOException( BrowserCoreMessages.BrowserConnectionIO_TheFileDoesNotSeemToBeValid );
        }

        for ( Iterator<?> i = rootElement.elementIterator( BROWSER_CONNECTION_TAG ); i.hasNext(); )
        {
            Element browserConnectionElement = ( Element ) i.next();
            readBrowserConnection( browserConnectionElement, browserConnectionMap );
        }
    }


    // ── R2-D2 Decodes One Section Of The Plans ───────────────────────────────────
    // R2-D2 opens one panel of the Death Star schematics, reads the section ID,
    // locates the corresponding slot in memory, and fills it with searches and
    // bookmarks from that panel.
    // We read one {@code <browserConnection>} XML element and populate its
    // search manager and bookmark manager with the persisted data.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads one {@code <browserConnection>} element and adds its saved searches
     * and bookmarks to the matching in-memory browser connection.
     * If no browser connection with the stored ID exists in the map, we skip
     * the element silently.
     *
     * <p>For example — R2-D2 decodes one panel:</p>
     * <pre>
     *   // element = &lt;browserConnection id="abc123"&gt; ... &lt;/browserConnection&gt;
     *   readBrowserConnection(element, connectionMap);
     * </pre>
     *
     * @param element              the {@code <browserConnection>} XML element.
     * @param browserConnectionMap the live connection map.
     * @throws ConnectionIOException if any attribute value within the element
     *                               cannot be parsed.
     */
    private static void readBrowserConnection( Element element, Map<String, IBrowserConnection> browserConnectionMap )
        throws ConnectionIOException
    {
        // ID
        Attribute idAttribute = element.attribute( ID_TAG );
        if ( idAttribute != null )
        {
            String id = idAttribute.getValue();
            IBrowserConnection browserConnection = browserConnectionMap.get( id );

            if ( browserConnection != null )
            {
                Element searchesElement = element.element( SEARCHES_TAG );
                if ( searchesElement != null )
                {
                    for ( Iterator<?> i = searchesElement.elementIterator( SEARCH_PARAMETER_TAG ); i.hasNext(); )
                    {
                        Element searchParameterElement = ( Element ) i.next();
                        SearchParameter searchParameter = readSearch( searchParameterElement, browserConnection );
                        ISearch search = new Search( browserConnection, searchParameter );
                        browserConnection.getSearchManager().addSearch( search );
                    }
                }

                Element bookmarksElement = element.element( BOOKMARKS_TAG );
                if ( bookmarksElement != null )
                {
                    for ( Iterator<?> i = bookmarksElement.elementIterator( BOOKMARK_PARAMETER_TAG ); i.hasNext(); )
                    {
                        Element bookmarkParameterElement = ( Element ) i.next();
                        BookmarkParameter bookmarkParameter = readBookmark( bookmarkParameterElement, browserConnection );
                        IBookmark bookmark = new Bookmark( browserConnection, bookmarkParameter );
                        browserConnection.getBookmarkManager().addBookmark( bookmark );
                    }
                }
            }
        }
    }


    // ── R2-D2 Reads One Search Panel From The Plans ───────────────────────────────
    // R2-D2 reads one labelled diagram block in the schematics — the search
    // base, filter, scope, limits, and any attached control circuits — and
    // reconstructs the full search parameter object from those encoded fields.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads a {@code <searchParameter>} XML element and constructs the
     * corresponding {@link SearchParameter} object.
     * We handle backward-compatibility for old Java-serialization-encoded
     * LDAP controls in addition to the current Base64 format.
     *
     * <p>For example — R2-D2 reads a search diagram:</p>
     * <pre>
     *   SearchParameter sp = readSearch(searchElement, connection);
     *   // sp.getFilter() == "(cn=*)", sp.getScope() == SUBTREE, etc.
     * </pre>
     *
     * @param searchParameterElement the {@code <searchParameter>} element.
     * @param browserConnection      the browser connection this search belongs to.
     * @return a fully populated {@link SearchParameter}.
     * @throws ConnectionIOException if any attribute value (DN, scope, limits,
     *                               control bytes) cannot be parsed.
     */
    private static SearchParameter readSearch( Element searchParameterElement, IBrowserConnection browserConnection )
        throws ConnectionIOException
    {
        SearchParameter searchParameter = new SearchParameter();

        // Name
        Attribute nameAttribute = searchParameterElement.attribute( NAME_TAG );
        if ( nameAttribute != null )
        {
            searchParameter.setName( nameAttribute.getValue() );
        }

        // Search base
        Attribute searchBaseAttribute = searchParameterElement.attribute( SEARCH_BASE_TAG );
        if ( searchBaseAttribute != null )
        {
            try
            {
                searchParameter.setSearchBase( new Dn( searchBaseAttribute.getValue() ) );
            }
            catch ( LdapInvalidDnException e )
            {
                throw new ConnectionIOException( NLS.bind(
                    BrowserCoreMessages.BrowserConnectionIO_UnableToParseSearchBase,
                    new String[]
                        { searchParameter.getName(), searchBaseAttribute.getValue() } ) );
            }
        }

        // Filter
        Attribute filterAttribute = searchParameterElement.attribute( FILTER_TAG );
        if ( filterAttribute != null )
        {
            searchParameter.setFilter( filterAttribute.getValue() );
        }

        // Returning Attributes
        Element returningAttributesElement = searchParameterElement.element( RETURNING_ATTRIBUTES_TAG );
        if ( returningAttributesElement != null )
        {
            List<String> returningAttributes = new ArrayList<String>();
            for ( Iterator<?> i = returningAttributesElement.elementIterator( RETURNING_ATTRIBUTE_TAG ); i.hasNext(); )
            {
                Element returningAttributeElement = ( Element ) i.next();

                Attribute valueAttribute = returningAttributeElement.attribute( VALUE_TAG );
                if ( valueAttribute != null )
                {
                    returningAttributes.add( valueAttribute.getValue() );
                }
            }
            searchParameter.setReturningAttributes( returningAttributes
                .toArray( new String[returningAttributes.size()] ) );
        }

        // Scope
        Attribute scopeAttribute = searchParameterElement.attribute( SCOPE_TAG );
        if ( scopeAttribute != null )
        {
            try
            {
                searchParameter.setScope( convertSearchScope( scopeAttribute.getValue() ) );
            }
            catch ( IllegalArgumentException e )
            {
                throw new ConnectionIOException( NLS.bind(
                    BrowserCoreMessages.BrowserConnectionIO_UnableToParseScope, new String[]
                        { searchParameter.getName(), scopeAttribute.getValue() } ) );
            }
        }

        // Time limit
        Attribute timeLimitAttribute = searchParameterElement.attribute( TIME_LIMIT_TAG );
        if ( timeLimitAttribute != null )
        {
            try
            {
                searchParameter.setTimeLimit( Integer.parseInt( timeLimitAttribute.getValue() ) );
            }
            catch ( NumberFormatException e )
            {
                throw new ConnectionIOException( NLS.bind(
                    BrowserCoreMessages.BrowserConnectionIO_UnableToParseTimeLimit,
                    new String[]
                        { searchParameter.getName(), timeLimitAttribute.getValue() } ) );
            }
        }

        // Count limit
        Attribute countLimitAttribute = searchParameterElement.attribute( COUNT_LIMIT_TAG );
        if ( countLimitAttribute != null )
        {
            try
            {
                searchParameter.setCountLimit( Integer.parseInt( countLimitAttribute.getValue() ) );
            }
            catch ( NumberFormatException e )
            {
                throw new ConnectionIOException( NLS.bind(
                    BrowserCoreMessages.BrowserConnectionIO_UnableToParseCountLimit,
                    new String[]
                        { searchParameter.getName(), countLimitAttribute.getValue() } ) );
            }
        }

        // Alias dereferencing method
        Attribute aliasesDereferencingMethodAttribute = searchParameterElement
            .attribute( ALIASES_DEREFERENCING_METHOD_TAG );
        if ( aliasesDereferencingMethodAttribute != null )
        {
            try
            {
                searchParameter.setAliasesDereferencingMethod( Connection.AliasDereferencingMethod
                    .valueOf( aliasesDereferencingMethodAttribute.getValue() ) );
            }
            catch ( IllegalArgumentException e )
            {
                throw new ConnectionIOException(
                    NLS.bind(
                        BrowserCoreMessages.BrowserConnectionIO_UnableToParseAliasesDereferencingMethod,
                        new String[]
                            { searchParameter.getName(), aliasesDereferencingMethodAttribute.getValue() } ) );
            }
        }

        // Referrals handling method
        Attribute referralsHandlingMethodAttribute = searchParameterElement.attribute( REFERRALS_HANDLING_METHOD_TAG );
        if ( referralsHandlingMethodAttribute != null )
        {
            try
            {
                searchParameter.setReferralsHandlingMethod( Connection.ReferralHandlingMethod
                    .valueOf( referralsHandlingMethodAttribute.getValue() ) );
            }
            catch ( IllegalArgumentException e )
            {
                throw new ConnectionIOException(
                    NLS.bind(
                        BrowserCoreMessages.BrowserConnectionIO_UnableToParseReferralsHandlingMethod,
                        new String[]
                            { searchParameter.getName(), referralsHandlingMethodAttribute.getValue() } ) );
            }
        }

        // Paged search scroll mode
        Attribute pagedSearchScrollModeAttribute = searchParameterElement.attribute( PAGED_SEARCH_SCROLL_MODE );
        if ( pagedSearchScrollModeAttribute != null )
        {
            searchParameter.setPagedSearchScrollMode( Boolean.valueOf( pagedSearchScrollModeAttribute.getValue() ) );
        }

        // Controls
        Element controlsElement = searchParameterElement.element( CONTROLS_TAG );
        if ( controlsElement != null )
        {
            for ( Iterator<?> i = controlsElement.elementIterator( CONTROL_TAG ); i.hasNext(); )
            {
                Element controlElement = ( Element ) i.next();
                Attribute oidAttribute = controlElement.attribute( OID_TAG );
                Attribute isCriticalAttribute = controlElement.attribute( IS_CRITICAL_TAG );
                Attribute valueAttribute = controlElement.attribute( VALUE_TAG );

                try
                {
                    if ( oidAttribute != null && isCriticalAttribute != null && valueAttribute != null )
                    {
                        byte[] bytes = Base64.getDecoder().decode( valueAttribute.getValue() );
                        Control control = Controls.create( oidAttribute.getValue(),
                            Boolean.valueOf( isCriticalAttribute.getValue() ), bytes );
                        searchParameter.getControls().add( control );
                    }
                    else if ( valueAttribute != null )
                    {
                        // Backward compatibility: read objects using Java serialization
                        byte[] bytes = Base64.getDecoder().decode( valueAttribute.getValue() );
                        ByteArrayInputStream bais = null;
                        ObjectInputStream ois = null;
                        bais = new ByteArrayInputStream( bytes );
                        ois = new ObjectInputStream( bais );
                        StudioControl studioControl = ( StudioControl ) ois.readObject();
                        Control control = Controls.create( studioControl.getOid(),
                            studioControl.isCritical(), studioControl.getControlValue() );
                        searchParameter.getControls().add( control );
                        ois.close();
                    }
                }
                catch ( Exception e )
                {
                    throw new ConnectionIOException( NLS.bind(
                        BrowserCoreMessages.BrowserConnectionIO_UnableToParseControl, new String[]
                        { searchParameter.getName(), valueAttribute.getValue() } ) );
                }
            }
        }

        return searchParameter;
    }


    // ── R2-D2 Reads One Bookmark Waypoint From The Plans ─────────────────────────
    // R2-D2 reads a waypoint marker from the schematics — it has a name label
    // and a coordinate (DN) that pinpoints the exact location in the directory.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads a {@code <bookmarkParameter>} XML element and constructs the
     * corresponding {@link BookmarkParameter} object (name + DN).
     *
     * <p>For example — R2-D2 reads a waypoint:</p>
     * <pre>
     *   BookmarkParameter bp = readBookmark(bookmarkElement, connection);
     *   // bp.getName() == "My Users", bp.getDn() == "ou=Users,dc=example,dc=com"
     * </pre>
     *
     * @param bookmarkParameterElement the {@code <bookmarkParameter>} element.
     * @param browserConnection        the browser connection this bookmark belongs to.
     * @return a {@link BookmarkParameter} with name and DN populated.
     * @throws ConnectionIOException if the DN string is not a valid LDAP DN.
     */
    private static BookmarkParameter readBookmark( Element bookmarkParameterElement,
        IBrowserConnection browserConnection ) throws ConnectionIOException
    {
        BookmarkParameter bookmarkParameter = new BookmarkParameter();

        // Name
        Attribute nameAttribute = bookmarkParameterElement.attribute( NAME_TAG );
        if ( nameAttribute != null )
        {
            bookmarkParameter.setName( nameAttribute.getValue() );
        }

        // Dn
        Attribute dnAttribute = bookmarkParameterElement.attribute( DN_TAG );
        if ( dnAttribute != null )
        {
            try
            {
                bookmarkParameter.setDn( new Dn( dnAttribute.getValue() ) );
            }
            catch ( LdapInvalidDnException e )
            {
                throw new ConnectionIOException( NLS.bind( BrowserCoreMessages.BrowserConnectionIO_UnableToParseDn,
                    new String[]
                        { bookmarkParameter.getName(), dnAttribute.getValue() } ) );
            }
        }

        return bookmarkParameter;
    }


    // ── R2-D2 Uploads The Full Plans Back To Storage ─────────────────────────────
    // R2-D2 assembles the complete schematic document from each section in memory,
    // then writes it to the target storage socket in a structured, pretty-printed
    // format so it's human-readable if anyone opens it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Serialises all browser connections (their searches and bookmarks) to an
     * XML output stream in the {@code browserconnections.xml} format.
     * The output is pretty-printed UTF-8 XML.  Callers should write to a temp
     * file first, then move it into place to avoid corruption on partial writes.
     *
     * <p>For example — R2-D2 uploading the plans:</p>
     * <pre>
     *   BrowserConnectionIO.save(new FileOutputStream("browserconnections.xml-temp"), connectionMap);
     *   // then rename the temp file to the real filename
     * </pre>
     *
     * @param stream               the output stream to write to.
     * @param browserConnectionMap all browser connections to persist.
     * @throws IOException if writing to the stream fails.
     */
    public static void save( OutputStream stream, Map<String, IBrowserConnection> browserConnectionMap )
        throws IOException
    {
        // Creating the Document
        Document document = DocumentHelper.createDocument();

        // Creating the root element
        Element root = document.addElement( BROWSER_CONNECTIONS_TAG );

        if ( browserConnectionMap != null )
        {
            for ( IBrowserConnection browserConnection : browserConnectionMap.values() )
            {
                writeBrowserConnection( root, browserConnection );
            }
        }

        // Writing the file to disk
        OutputFormat outformat = OutputFormat.createPrettyPrint();
        outformat.setEncoding( "UTF-8" ); //$NON-NLS-1$
        XMLWriter writer = new XMLWriter( stream, outformat );
        writer.write( document );
        writer.flush();
    }


    // ── R2-D2 Writes One Connection Section Into The Document ────────────────────
    // R2-D2 picks up one section of the schematics, writes the section header
    // (connection ID), then sub-sections for all the search diagrams and
    // bookmark waypoints it found in that section.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Appends a {@code <browserConnection>} element for the given connection
     * to the parent XML element, including nested {@code <searches>} and
     * {@code <bookmarks>} sub-elements.
     *
     * <p>For example — R2-D2 writes one section:</p>
     * <pre>
     *   // appends: &lt;browserConnection id="abc123"&gt;...&lt;/browserConnection&gt;
     *   writeBrowserConnection(root, myBrowserConnection);
     * </pre>
     *
     * @param parent            the parent XML element to append to.
     * @param browserConnection the browser connection to serialise.
     * @throws IOException if writing the control value bytes fails.
     */
    private static void writeBrowserConnection( Element parent, IBrowserConnection browserConnection )
        throws IOException
    {
        Element browserConnectionElement = parent.addElement( BROWSER_CONNECTION_TAG );

        // ID
        browserConnectionElement.addAttribute( ID_TAG, browserConnection.getConnection().getId() );

        // Searches
        Element searchesElement = browserConnectionElement.addElement( SEARCHES_TAG );
        List<ISearch> searches = browserConnection.getSearchManager().getSearches();
        for ( ISearch search : searches )
        {
            Element searchParameterElement = searchesElement.addElement( SEARCH_PARAMETER_TAG );
            writeSearch( searchParameterElement, search.getSearchParameter() );
        }

        // Bookmarks
        Element bookmarksElement = browserConnectionElement.addElement( BOOKMARKS_TAG );
        IBookmark[] bookmarks = browserConnection.getBookmarkManager().getBookmarks();
        for ( IBookmark bookmark : bookmarks )
        {
            Element bookmarkParameterElement = bookmarksElement.addElement( BOOKMARK_PARAMETER_TAG );
            writeBookmark( bookmarkParameterElement, bookmark.getBookmarkParameter() );
        }
    }


    // ── R2-D2 Encodes One Search Diagram Into The Plans ──────────────────────────
    // R2-D2 takes one search diagram from memory and encodes every field
    // (filter, scope, limits, controls) into the XML attributes of the plan
    // section, Base64-encoding any raw bytes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Writes all fields of a {@link SearchParameter} as XML attributes and
     * child elements on the given {@code <searchParameter>} element.
     * LDAP controls are Base64-encoded so the XML stays valid text.
     *
     * <p>For example — R2-D2 encodes a search diagram:</p>
     * <pre>
     *   writeSearch(searchElement, mySearchParameter);
     *   // searchElement now has name, searchBase, filer, scope, timeLimit, etc.
     * </pre>
     *
     * @param searchParameterElement the {@code <searchParameter>} element to
     *                               populate.
     * @param searchParameter        the search parameter to encode.
     * @throws IOException if encoding a control value's bytes fails.
     */
    private static void writeSearch( Element searchParameterElement, SearchParameter searchParameter )
        throws IOException
    {
        // Name
        searchParameterElement.addAttribute( NAME_TAG, searchParameter.getName() );

        // Search base
        String searchBase = searchParameter.getSearchBase() != null ? searchParameter.getSearchBase().getName() : ""; //$NON-NLS-1$
        searchParameterElement.addAttribute( SEARCH_BASE_TAG, searchBase );

        // Filter
        searchParameterElement.addAttribute( FILTER_TAG, searchParameter.getFilter() );

        // Returning Attributes
        Element returningAttributesElement = searchParameterElement.addElement( RETURNING_ATTRIBUTES_TAG );
        for ( String ra : searchParameter.getReturningAttributes() )
        {
            Element raElement = returningAttributesElement.addElement( RETURNING_ATTRIBUTE_TAG );
            raElement.addAttribute( VALUE_TAG, ra );
        }

        // Scope
        searchParameterElement.addAttribute( SCOPE_TAG, convertSearchScope( searchParameter.getScope() ) );

        // Time limit
        searchParameterElement.addAttribute( TIME_LIMIT_TAG, "" + searchParameter.getTimeLimit() ); //$NON-NLS-1$

        // Count limit
        searchParameterElement.addAttribute( COUNT_LIMIT_TAG, "" + searchParameter.getCountLimit() ); //$NON-NLS-1$

        // Alias dereferencing method
        searchParameterElement.addAttribute( ALIASES_DEREFERENCING_METHOD_TAG, searchParameter
            .getAliasesDereferencingMethod().toString() );

        // Referrals handling method
        searchParameterElement.addAttribute( REFERRALS_HANDLING_METHOD_TAG, searchParameter
            .getReferralsHandlingMethod().toString() );

        // Paged search scroll mode
        searchParameterElement.addAttribute( PAGED_SEARCH_SCROLL_MODE, "" + searchParameter.isPagedSearchScrollMode() );

        // Controls
        Element controlsElement = searchParameterElement.addElement( CONTROLS_TAG );
        for ( Control control : searchParameter.getControls() )
        {
            byte[] bytes = Controls.getEncodedValue( control );
            String controlsValue = new String( Base64.getEncoder().encode( bytes ), StandardCharsets.UTF_8 );

            Element controlElement = controlsElement.addElement( CONTROL_TAG );
            controlElement.addAttribute( OID_TAG, control.getOid() );
            controlElement.addAttribute( IS_CRITICAL_TAG, "" + control.isCritical() );
            controlElement.addAttribute( VALUE_TAG, controlsValue );
        }
    }


    // ── R2-D2 Encodes One Bookmark Waypoint ──────────────────────────────────────
    // R2-D2 writes the waypoint name and DN coordinate into the XML element —
    // simple, but must be done carefully so the DN round-trips correctly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Writes the name and DN of a {@link BookmarkParameter} as XML attributes
     * on the given {@code <bookmarkParameter>} element.
     *
     * <p>For example — R2-D2 writes a waypoint:</p>
     * <pre>
     *   writeBookmark(bookmarkElement, myBookmarkParameter);
     *   // bookmarkElement has: name="My Users", dn="ou=Users,dc=example,dc=com"
     * </pre>
     *
     * @param bookmarkParameterElement the {@code <bookmarkParameter>} element
     *                                 to populate.
     * @param bookmarkParameter        the bookmark parameter to encode.
     */
    private static void writeBookmark( Element bookmarkParameterElement, BookmarkParameter bookmarkParameter )
    {
        // Name
        bookmarkParameterElement.addAttribute( NAME_TAG, bookmarkParameter.getName() );

        // Dn
        String dn = bookmarkParameter.getDn() != null ? bookmarkParameter.getDn().getName() : ""; //$NON-NLS-1$
        bookmarkParameterElement.addAttribute( DN_TAG, dn );
    }


    // ── R2-D2 Translates A Scope Value To Printable Text ─────────────────────────
    // R2-D2 converts the internal scope enum to the string token that goes into
    // the XML — "OBJECT", "ONELEVEL", or "SUBTREE".
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Converts a {@link SearchScope} enum value to its XML string token.
     * We use ALL-CAPS tokens in the new format.  Unrecognised or null scope
     * defaults to {@code "SUBTREE"}.
     *
     * <p>For example — R2-D2 translates a scope code:</p>
     * <pre>
     *   convertSearchScope(SearchScope.ONELEVEL) == "ONELEVEL"
     * </pre>
     *
     * @param scope the scope to convert; {@code null} returns {@code "SUBTREE"}.
     * @return the string token for the XML attribute.
     * @see <a href="https://issues.apache.org/jira/browse/DIRSTUDIO-771">DIRSTUDIO-771</a>
     */
    private static String convertSearchScope( SearchScope scope )
    {
        if ( scope != null )
        {
            switch ( scope )
            {
                case OBJECT:
                    return SCOPE_OBJECT;
                case ONELEVEL:
                    return SCOPE_ONELEVEL;
                case SUBTREE:
                    return SCOPE_SUBTREE;
            }
        }

        return SCOPE_SUBTREE;
    }


    // ── R2-D2 Translates A Text Token Back To A Scope Value ──────────────────────
    // R2-D2 reads the scope string from the XML and maps it back to the enum —
    // he understands both the new ALL-CAPS form and the old lowercase form for
    // backward compatibility.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Converts an XML scope string token back to a {@link SearchScope} enum
     * value.
     * Accepts both the new ALL-CAPS tokens ({@code "OBJECT"}, {@code "ONELEVEL"},
     * {@code "SUBTREE"}) and the old lowercase aliases ({@code "base"},
     * {@code "one"}, {@code "sub"}) for backward compatibility with files
     * written by older versions.
     *
     * <p>For example — R2-D2 translates a text token:</p>
     * <pre>
     *   convertSearchScope("one") == SearchScope.ONELEVEL  // old format
     *   convertSearchScope("SUBTREE") == SearchScope.SUBTREE // new format
     * </pre>
     *
     * @param scope the scope string to parse; must not be {@code null}.
     * @return the corresponding {@link SearchScope}.
     * @throws IllegalArgumentException if the string doesn't match any known
     *                                   token in either format.
     * @see <a href="https://issues.apache.org/jira/browse/DIRSTUDIO-771">DIRSTUDIO-771</a>
     */
    private static SearchScope convertSearchScope( String scope ) throws IllegalArgumentException
    {
        if ( ( SCOPE_OBJECT.equalsIgnoreCase( scope ) || SCOPE_OBJECT_2.equalsIgnoreCase( scope ) ) )
        {
            return SearchScope.OBJECT;
        }
        else if ( ( SCOPE_ONELEVEL.equalsIgnoreCase( scope ) || SCOPE_ONELEVEL_2.equalsIgnoreCase( scope ) ) )
        {
            return SearchScope.ONELEVEL;
        }
        else if ( ( SCOPE_SUBTREE.equalsIgnoreCase( scope ) || SCOPE_SUBTREE_2.equalsIgnoreCase( scope ) ) )
        {
            return SearchScope.SUBTREE;
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
}
