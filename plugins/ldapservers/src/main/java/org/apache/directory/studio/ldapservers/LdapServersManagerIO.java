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
package org.apache.directory.studio.ldapservers;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.model.LdapServerAdapterExtension;
import org.apache.directory.studio.ldapservers.model.UnknownLdapServerAdapterExtension;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;


// ── CLASS: LdapServersManagerIO — THE IMPERIAL ARCHIVE READ/WRITE DESK ───────────────────
// The Empire keeps a permanent record of every active installation in an XML archive file
// called ldapServers.xml.  When Studio starts, the archivist reads the file and reconstructs
// the server list.  When servers are added, updated, or removed, the archivist writes the
// updated list back to the archive.
// This class is that archivist: static methods to read from and write to the stream-backed
// XML store, using DOM4J for both SAX parsing (read) and pretty-printed serialization (write).
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Reads and writes the {@code ldapServers.xml} archive file for {@link LdapServersManager}.
 * Uses DOM4J's SAXReader for parsing and XMLWriter for pretty-printed serialization.
 * On read, unknown adapter IDs produce an {@link UnknownLdapServerAdapterExtension} so the
 * server record is still surfaced in the UI with a warning.
 * On write, all configuration parameters are serialized with their Java type so they survive
 * a round-trip (Integer and Boolean are type-tagged; String is the implicit default).
 * Think of it as the Imperial Archive read/write desk.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapServersManagerIO
{
    // XML tags and attributes
    private static final String LDAP_SERVERS_TAG = "ldapServers"; //$NON-NLS-1$
    private static final String LDAP_SERVER_TAG = "ldapServer"; //$NON-NLS-1$
    private static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
    private static final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
    private static final String ADAPTER_ID_ATTRIBUTE = "adapterId"; //$NON-NLS-1$
    private static final String ADAPTER_NAME_ATTRIBUTE = "adapterName"; //$NON-NLS-1$
    private static final String ADAPTER_VENDOR_ATTRIBUTE = "adapterVendor"; //$NON-NLS-1$
    private static final String ADAPTER_VERSION_ATTRIBUTE = "adapterVersion"; //$NON-NLS-1$
    private static final String CONFIGURATION_PARAMETERS_TAG = "configurationParameters"; //$NON-NLS-1$
    private static final String ENTRY_TAG = "entry"; //$NON-NLS-1$
    private static final String KEY_ATTRIBUTE = "key"; //$NON-NLS-1$
    private static final String TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$
    private static final String VALUE_ATTRIBUTE = "value"; //$NON-NLS-1$


    // ── Reading The Archive Back Into A Live Server List ──────────────────────────────────────
    // The archivist opens the XML file and reads out every <ldapServer> element, rehydrating
    // each one into a live LdapServer object.  If the root element is wrong, we throw
    // LdapServersManagerIOException — that's not a valid archive.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the given input stream as the {@code ldapServers.xml} XML and returns the
     * list of reconstructed {@link LdapServer} objects.
     * Throws {@link LdapServersManagerIOException} if the stream is malformed or has the wrong root tag.
     *
     * <p>For example — the archivist unpacks the archive:</p>
     * <pre>
     *   read(stream) → [LdapServer("My ApacheDS"), LdapServer("Test Server")]
     * </pre>
     *
     * @param stream  the input stream containing the XML data
     * @return the list of servers read from the stream
     * @throws LdapServersManagerIOException if the stream cannot be parsed or is not a valid servers file
     */
    public static List<LdapServer> read( InputStream stream ) throws LdapServersManagerIOException
    {
        List<LdapServer> servers = new ArrayList<LdapServer>();

        SAXReader saxReader = new SAXReader();
        Document document = null;

        try
        {
            document = saxReader.read( stream );
        }
        catch ( DocumentException e )
        {
            throw new LdapServersManagerIOException( e.getMessage() );
        }

        Element rootElement = document.getRootElement();
        if ( !rootElement.getName().equals( LDAP_SERVERS_TAG ) )
        {
            throw new LdapServersManagerIOException(
                Messages.getString( "LdapServersManagerIO.ErrorNotValidServersFile" ) ); //$NON-NLS-1$
        }

        for ( Iterator<?> i = rootElement.elementIterator( LDAP_SERVER_TAG ); i.hasNext(); )
        {
            servers.add( readLdapServer( ( Element ) i.next() ) );
        }

        return servers;
    }


    // ── Rehydrating A Single Server Element From The Archive ──────────────────────────────────
    // Each <ldapServer> element carries the ID, name, adapter info, and configuration parameters.
    // If the adapter ID matches a known extension, we use it directly.
    // If not (plugin was uninstalled), we create an UnknownLdapServerAdapterExtension with
    // the saved name/vendor/version so the server still shows up with a warning.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads a single {@code <ldapServer>} XML element and reconstructs a {@link LdapServer}.
     * Looks up the adapter extension by ID in {@link LdapServerAdapterExtensionsManager}.
     * If the extension is not found, creates an {@link UnknownLdapServerAdapterExtension}
     * with the saved adapter metadata.
     *
     * @param element  the DOM4J element representing one server record
     * @return the reconstructed LdapServer
     */
    private static LdapServer readLdapServer( Element element )
    {
        LdapServer server = new LdapServer();

        // ID
        Attribute idAttribute = element.attribute( ID_ATTRIBUTE );
        if ( idAttribute != null )
        {
            server.setId( idAttribute.getValue() );
        }

        // Name
        Attribute nameAttribute = element.attribute( NAME_ATTRIBUTE );
        if ( nameAttribute != null )
        {
            server.setName( nameAttribute.getValue() );
        }

        // Adapter ID
        Attribute adapterIdAttribute = element.attribute( ADAPTER_ID_ATTRIBUTE );
        if ( adapterIdAttribute != null )
        {
            // Getting the id
            String adapterId = adapterIdAttribute.getValue();

            // Looking for the correct LDAP Server Adapter Extension object
            LdapServerAdapterExtension ldapServerAdapterExtension = LdapServerAdapterExtensionsManager.getDefault()
                .getLdapServerAdapterExtensionById( adapterId );
            if ( ldapServerAdapterExtension != null )
            {
                // The Adapter Extension has been found
                // Assigning it to the server
                server.setLdapServerAdapterExtension( ldapServerAdapterExtension );
            }
            else
            {
                // The Adapter Extension has not been found
                // Creating an "unknown" Adapter Extension
                UnknownLdapServerAdapterExtension unknownLdapServerAdapterExtension = new UnknownLdapServerAdapterExtension();

                // Adapter Id
                unknownLdapServerAdapterExtension.setId( adapterId );

                // Adapter Name
                Attribute adapterNameAttribute = element.attribute( ADAPTER_NAME_ATTRIBUTE );
                if ( adapterNameAttribute != null )
                {
                    unknownLdapServerAdapterExtension.setName( adapterNameAttribute.getValue() );
                }

                // Adapter Vendor
                Attribute adapterVendorAttribute = element.attribute( ADAPTER_VENDOR_ATTRIBUTE );
                if ( adapterVendorAttribute != null )
                {
                    unknownLdapServerAdapterExtension.setVendor( adapterVendorAttribute.getValue() );
                }

                // Adapter Version
                Attribute adapterVersionAttribute = element.attribute( ADAPTER_VERSION_ATTRIBUTE );
                if ( adapterVersionAttribute != null )
                {
                    unknownLdapServerAdapterExtension.setVersion( adapterVersionAttribute.getValue() );
                }

                // Assigning the "unknown" Adapter Extension to the server
                server.setLdapServerAdapterExtension( unknownLdapServerAdapterExtension );
            }
        }
        else
        {
            // TODO No Adapter ID, throw an error ?
        }

        // Configuration Parameters
        Element configurationParametersElement = element.element( CONFIGURATION_PARAMETERS_TAG );
        if ( configurationParametersElement != null )
        {
            for ( Iterator<?> i = configurationParametersElement.elementIterator( ENTRY_TAG ); i.hasNext(); )
            {
                readConfigurationParameter( server, ( Element ) i.next() );
            }
        }

        return server;
    }


    // ── Reading A Single Configuration Parameter Entry ────────────────────────────────────────
    // Each <entry key="..." value="..." type="..."/> element holds one parameter.
    // Integer and Boolean parameters are type-tagged; everything else defaults to String.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads a single {@code <entry>} configuration parameter element and stores it on the server.
     * Type-coerces Integer and Boolean values from the string representation using the {@code type}
     * attribute; defaults to String for missing or unrecognised types.
     *
     * @param server   the server to store the parameter on
     * @param element  the DOM4J element representing one configuration parameter
     */
    private static void readConfigurationParameter( LdapServer server, Element element )
    {
        // Key
        Attribute keyAttribute = element.attribute( KEY_ATTRIBUTE );
        String key = null;
        if ( keyAttribute != null )
        {
            key = keyAttribute.getValue();

            // Value
            Attribute valueAttribute = element.attribute( VALUE_ATTRIBUTE );
            String value = null;
            if ( valueAttribute != null )
            {
                value = valueAttribute.getValue();
            }

            // Type
            Attribute typeAttribute = element.attribute( TYPE_ATTRIBUTE );
            String type = null;
            if ( typeAttribute != null )
            {
                type = typeAttribute.getValue();
            }

            // Integer value
            if ( ( type != null ) && ( type.equalsIgnoreCase( Integer.class.getCanonicalName() ) ) )
            {
                server.putConfigurationParameter( key, Integer.parseInt( value ) );
            }
            // Boolean value
            else if ( ( type != null ) && ( type.equalsIgnoreCase( Boolean.class.getCanonicalName() ) ) )
            {
                server.putConfigurationParameter( key, Boolean.parseBoolean( value ) );
            }
            // String value (default type)
            else
            {
                server.putConfigurationParameter( key, value );
            }
        }
    }


    // ── Writing The Live Server List Back To The Archive ──────────────────────────────────────
    // When servers change, the archivist rebuilds the XML document from scratch and writes
    // the whole thing to the output stream, UTF-8 encoded, pretty-printed.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Serializes the list of servers to the given output stream as UTF-8 encoded, pretty-printed XML.
     * Creates a fresh DOM4J document, adds one {@code <ldapServer>} element per server,
     * then writes it with {@link XMLWriter}.
     *
     * @param servers       the live list of servers to serialize
     * @param outputStream  the stream to write the XML to
     * @throws IOException  if writing to the stream fails
     */
    public static void write( List<LdapServer> servers, OutputStream outputStream ) throws IOException
    {
        // Creating the Document
        Document document = DocumentHelper.createDocument();

        // Creating the root element
        Element root = document.addElement( LDAP_SERVERS_TAG );

        if ( servers != null )
        {
            for ( LdapServer server : servers )
            {
                addLdapServer( server, root );
            }
        }

        // Writing the file to the stream
        OutputFormat outformat = OutputFormat.createPrettyPrint();
        outformat.setEncoding( "UTF-8" ); //$NON-NLS-1$
        XMLWriter writer = new XMLWriter( outputStream, outformat );
        writer.write( document );
        writer.flush();
    }


    // ── Serializing A Single Server Into The XML Document ─────────────────────────────────────
    // For each server we write: ID, name, adapter ID/name/vendor/version as attributes, and
    // configuration parameters as a child <configurationParameters> block (if any).
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds the XML representation of a single {@link LdapServer} as a child element of the parent.
     * Writes adapter metadata and configuration parameters as XML attributes and child elements.
     *
     * @param server  the server to serialize
     * @param parent  the parent DOM4J element (the root {@code <ldapServers>} element)
     */
    private static void addLdapServer( LdapServer server, Element parent )
    {
        // Server element
        Element serverElement = parent.addElement( LDAP_SERVER_TAG );

        // ID
        serverElement.addAttribute( ID_ATTRIBUTE, server.getId() );

        // Name
        serverElement.addAttribute( NAME_ATTRIBUTE, server.getName() );

        // Adapter ID
        serverElement.addAttribute( ADAPTER_ID_ATTRIBUTE, server.getLdapServerAdapterExtension().getId() );

        // Adapter Name
        serverElement.addAttribute( ADAPTER_NAME_ATTRIBUTE, server.getLdapServerAdapterExtension().getName() );

        // Adapter Vendor
        serverElement.addAttribute( ADAPTER_VENDOR_ATTRIBUTE, server.getLdapServerAdapterExtension().getVendor() );

        // Adapter Version
        serverElement.addAttribute( ADAPTER_VERSION_ATTRIBUTE, server.getLdapServerAdapterExtension().getVersion() );

        // Configuration Parameters
        Map<String, Object> configurationParametersMap = server.getConfigurationParameters();
        if ( ( configurationParametersMap != null ) && ( configurationParametersMap.size() > 0 ) )
        {
            addConfigurationParameters( configurationParametersMap, serverElement );
        }
    }


    // ── Serializing A Server's Configuration Parameters ───────────────────────────────────────
    // Configuration parameters are stored as key/value/type triplets.
    // Non-String types get an explicit type attribute so they survive a round-trip read.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code <configurationParameters>/<entry>} block to the given server element.
     * Each map entry becomes one {@code <entry key="..." value="..." type="..."/>} child.
     * The {@code type} attribute is omitted for String values (the implicit default).
     *
     * @param map     the server's configuration parameters map
     * @param parent  the parent {@code <ldapServer>} element to add the block to
     */
    private static void addConfigurationParameters( Map<String, Object> map, Element parent )
    {
        // Configuration Parameters element
        Element configurationParametersElement = parent.addElement( CONFIGURATION_PARAMETERS_TAG );

        // Get the keys of the map
        Set<Entry<String, Object>> entriesSet = map.entrySet();

        for ( Entry<String, Object> entry : entriesSet )
        {
            // Entry element
            Element entryElement = configurationParametersElement.addElement( ENTRY_TAG );

            // Key
            entryElement.addAttribute( KEY_ATTRIBUTE, entry.getKey() );

            // Value
            Object value = entry.getValue();
            entryElement.addAttribute( VALUE_ATTRIBUTE, value.toString() );

            // Type
            if ( value.getClass() != String.class )
            {
                entryElement.addAttribute( TYPE_ATTRIBUTE, value.getClass().getCanonicalName() );
            }
        }
    }
}
