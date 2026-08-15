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
package org.apache.directory.studio.connection.core.io;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.directory.api.ldap.model.constants.SaslQoP;
import org.apache.directory.api.ldap.model.constants.SaslSecurityStrength;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.apache.directory.studio.connection.core.ConnectionParameter.AuthenticationMethod;
import org.apache.directory.studio.connection.core.ConnectionParameter.ConnectionProtocol;
import org.apache.directory.studio.connection.core.ConnectionParameter.EncryptionMethod;
import org.apache.directory.studio.connection.core.ConnectionParameter.Krb5Configuration;
import org.apache.directory.studio.connection.core.ConnectionParameter.Krb5CredentialConfiguration;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;


// ── CLASS: ConnectionIO — CHEWIE'S ROUTE MANIFEST FILE READER/WRITER ──────────
// Chewie keeps the Falcon's route manifests in two XML files on disk:
// one for the connections (connections.xml) and one for the folder hierarchy
// (connectionFolders.xml).  When the app starts, we read those files and
// reconstruct the in-memory model.  When the user saves, we write them back.
// This class handles exactly that: it uses DOM4J to parse and generate XML,
// and maps each XML attribute to the matching field on ConnectionParameter
// or ConnectionFolder.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Serialiser/deserialiser for the {@code connections.xml} and
 * {@code connectionFolders.xml} persistence files.
 * All public methods are static; there is no instance state.
 * The XML format uses a flat list of {@code <connection>} elements (each carrying
 * all settings as XML attributes) inside a {@code <connections>} root, plus a
 * separate {@code <connectionFolders>} document for the folder hierarchy.
 * We use DOM4J as the XML library throughout.
 * Think of this as Chewie's filing system reader/writer: it translates between the
 * on-disk XML manifest and the in-memory {@link ConnectionParameter} /
 * {@link ConnectionFolder} objects.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionIO
{
    // ── XML TAG CONSTANTS — THE FIELD NAMES IN THE MANIFEST ──────────────────────
    // These mirror the attribute and element names in connections.xml and
    // connectionFolders.xml.  Keeping them as constants prevents typos.
    // ─────────────────────────────────────────────────────────────────────────────

    /** Root element for the connections document. */
    private static final String CONNECTIONS_TAG = "connections"; //$NON-NLS-1$
    /** Element for a single connection entry. */
    private static final String CONNECTION_TAG = "connection"; //$NON-NLS-1$
    /** Attribute: connection UUID. */
    private static final String ID_TAG = "id"; //$NON-NLS-1$
    /** Attribute: human-readable connection name. */
    private static final String NAME_TAG = "name"; //$NON-NLS-1$
    /** Attribute: server hostname or IP. */
    private static final String HOST_TAG = "host"; //$NON-NLS-1$
    /** Attribute: server TCP port. */
    private static final String PORT_TAG = "port"; //$NON-NLS-1$
    /** Attribute: encryption method enum name (NONE, LDAPS, START_TLS). */
    private static final String ENCRYPTION_METHOD_TAG = "encryptionMethod"; //$NON-NLS-1$
    /** Attribute: authentication method enum name (ANONYMOUS, SIMPLE, SASL_*). */
    private static final String AUTH_METHOD_TAG = "authMethod"; //$NON-NLS-1$
    /** Attribute: bind DN or SASL principal. */
    private static final String BIND_PRINCIPAL_TAG = "bindPrincipal"; //$NON-NLS-1$
    /** Attribute: bind password (stored in cleartext — real passwords go through the keystore). */
    private static final String BIND_PASSWORD_TAG = "bindPassword"; //$NON-NLS-1$
    /** Attribute: SASL realm string. */
    private static final String SASL_REALM_TAG = "saslRealm"; //$NON-NLS-1$
    /** Attribute: SASL quality-of-protection enum. */
    private static final String SASL_QOP_TAG = "saslQop"; //$NON-NLS-1$
    /** Attribute: SASL security strength enum. */
    private static final String SASL_SEC_STRENGTH_TAG = "saslSecStrenght"; //$NON-NLS-1$
    /** Attribute: SASL mutual authentication flag. */
    private static final String SASL_MUTUAL_AUTH_TAG = "saslMutualAuth"; //$NON-NLS-1$
    /** Attribute: Kerberos 5 credential configuration enum. */
    private static final String KRB5_CREDENTIALS_CONF_TAG = "krb5CredentialsConf"; //$NON-NLS-1$
    /** Attribute: Kerberos 5 configuration source enum. */
    private static final String KRB5_CONFIG_TAG = "krb5Config"; //$NON-NLS-1$
    /** Attribute: path to a custom krb5.conf file. */
    private static final String KRB5_CONFIG_FILE_TAG = "krb5ConfigFile"; //$NON-NLS-1$
    /** Attribute: Kerberos 5 realm. */
    private static final String KRB5_REALM_TAG = "krb5Realm"; //$NON-NLS-1$
    /** Attribute: Kerberos 5 KDC hostname. */
    private static final String KRB5_KDC_HOST_TAG = "krb5KdcHost"; //$NON-NLS-1$
    /** Attribute: Kerberos 5 KDC port. */
    private static final String KRB5_KDC_PORT_TAG = "krb5KdcPort"; //$NON-NLS-1$
    /** Attribute: whether the connection is read-only. */
    private static final String READ_ONLY_TAG = "readOnly"; //$NON-NLS-1$
    /** Attribute: connection timeout in milliseconds. */
    private static final String TIMEOUT_TAG = "timeout"; //$NON-NLS-1$
    /** Attribute: wire protocol discriminator (LDAP or SCIM); absent means LDAP for backward compat. */
    private static final String CONNECTION_PROTOCOL_TAG = "connectionProtocol"; //$NON-NLS-1$

    /** Parent element for plugin-specific extended properties. */
    private static final String EXTENDED_PROPERTIES_TAG = "extendedProperties"; //$NON-NLS-1$
    /** Element for a single extended property key/value pair. */
    private static final String EXTENDED_PROPERTY_TAG = "extendedProperty"; //$NON-NLS-1$
    /** Attribute: extended property key. */
    private static final String KEY_TAG = "key"; //$NON-NLS-1$
    /** Attribute: extended property value. */
    private static final String VALUE_TAG = "value"; //$NON-NLS-1$

    /** Root element for the connection folders document. */
    private static final String CONNECTION_FOLDERS_TAG = "connectionFolders"; //$NON-NLS-1$
    /** Element for a single folder entry. */
    private static final String CONNECTION_FOLDER_TAG = "connectionFolder"; //$NON-NLS-1$
    /** Parent element for the folder's child-folder ID list. */
    private static final String SUB_FOLDERS_TAG = "subFolders"; //$NON-NLS-1$
    /** Element for a single child-folder ID reference. */
    private static final String SUB_FOLDER_TAG = "subFolder"; //$NON-NLS-1$


    // ── LOAD — READ CONNECTIONS.XML FROM A STREAM ─────────────────────────────────
    // Chewie pulls out the route manifest and reads every ship's entry.
    // We parse the XML, verify the root element name, then iterate all
    // <connection> elements and call readConnection() on each.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads all {@link ConnectionParameter} records from the given {@code connections.xml}
     * input stream and returns them as a {@link Set}.
     * Throws {@link ConnectionIOException} if the XML cannot be parsed or the root
     * element is wrong.
     *
     * @param stream  The input stream pointing to {@code connections.xml}.
     * @return  A {@link Set} of {@link ConnectionParameter} instances.
     * @throws ConnectionIOException  If the XML is malformed or has the wrong root element.
     */
    public static Set<ConnectionParameter> load( InputStream stream ) throws ConnectionIOException
    {
        Set<ConnectionParameter> connections = new HashSet<>();

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
        if ( !rootElement.getName().equals( CONNECTIONS_TAG ) )
        {
            throw new ConnectionIOException( "The file does not seem to be a valid Connections file." ); //$NON-NLS-1$
        }

        for ( Iterator<?> i = rootElement.elementIterator( CONNECTION_TAG ); i.hasNext(); )
        {
            Element connectionElement = ( Element ) i.next();
            connections.add( readConnection( connectionElement ) );
        }

        return connections;
    }


    // ── READ CONNECTION — PARSE ONE <connection> ELEMENT ──────────────────────────
    // We read each attribute off the element, parse its value to the right type,
    // and set it on a fresh ConnectionParameter.
    // Bad enum values or non-integer numbers throw ConnectionIOException so the
    // user gets a clear message about which field in which connection is broken.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a single {@code <connection>} DOM element into a {@link ConnectionParameter}.
     * All attributes are optional — missing ones leave the corresponding field at its
     * default value.  Invalid enum names or non-numeric values throw
     * {@link ConnectionIOException} with a descriptive message.
     *
     * @param element  The DOM element to parse.
     * @return  The populated {@link ConnectionParameter}.
     * @throws ConnectionIOException  If any attribute value cannot be parsed.
     */
    private static ConnectionParameter readConnection( Element element ) throws ConnectionIOException
    {
        ConnectionParameter connection = new ConnectionParameter();

        // ID
        Attribute idAttribute = element.attribute( ID_TAG );

        if ( idAttribute != null )
        {
            connection.setId( idAttribute.getValue() );
        }

        // Name
        Attribute nameAttribute = element.attribute( NAME_TAG );

        if ( nameAttribute != null )
        {
            connection.setName( nameAttribute.getValue() );
        }

        // Host
        Attribute hostAttribute = element.attribute( HOST_TAG );

        if ( hostAttribute != null )
        {
            connection.setHost( hostAttribute.getValue() );
        }

        // Port
        Attribute portAttribute = element.attribute( PORT_TAG );

        if ( portAttribute != null )
        {
            try
            {
                connection.setPort( Integer.parseInt( portAttribute.getValue() ) );
            }
            catch ( NumberFormatException e )
            {
                throw new ConnectionIOException( "Unable to parse 'Port' of connection '" + connection.getName() //$NON-NLS-1$
                    + "' as int value. Port value :" + portAttribute.getValue() ); //$NON-NLS-1$
            }
        }

        // Timeout
        Attribute timeoutAttribute = element.attribute( TIMEOUT_TAG );

        if ( timeoutAttribute != null )
        {
            try
            {
                connection.setTimeoutMillis( Long.parseLong( timeoutAttribute.getValue() ) );
            }
            catch ( NumberFormatException e )
            {
                throw new ConnectionIOException( "Unable to parse 'Timeout' of connection '" + connection.getName() //$NON-NLS-1$
                    + "' as int value. Timeout value :" + timeoutAttribute.getValue() ); //$NON-NLS-1$
            }
        }

        // Encryption Method
        Attribute encryptionMethodAttribute = element.attribute( ENCRYPTION_METHOD_TAG );

        if ( encryptionMethodAttribute != null )
        {
            try
            {
                connection.setEncryptionMethod( EncryptionMethod.valueOf( encryptionMethodAttribute.getValue() ) );
            }
            catch ( IllegalArgumentException e )
            {
                throw new ConnectionIOException( "Unable to parse 'Encryption Method' of connection '" //$NON-NLS-1$
                    + connection.getName() + "' as int value. Encryption Method value :" //$NON-NLS-1$
                    + encryptionMethodAttribute.getValue() );
            }
        }

        // Auth Method
        Attribute authMethodAttribute = element.attribute( AUTH_METHOD_TAG );

        if ( authMethodAttribute != null )
        {
            try
            {
                connection.setAuthMethod( AuthenticationMethod.valueOf( authMethodAttribute.getValue() ) );
            }
            catch ( IllegalArgumentException e )
            {
                throw new ConnectionIOException( "Unable to parse 'Authentication Method' of connection '" //$NON-NLS-1$
                    + connection.getName() + "' as int value. Authentication Method value :" //$NON-NLS-1$
                    + authMethodAttribute.getValue() );
            }
        }

        // Bind Principal
        Attribute bindPrincipalAttribute = element.attribute( BIND_PRINCIPAL_TAG );

        if ( bindPrincipalAttribute != null )
        {
            connection.setBindPrincipal( bindPrincipalAttribute.getValue() );
        }

        // Bind Password
        Attribute bindPasswordAttribute = element.attribute( BIND_PASSWORD_TAG );

        if ( bindPasswordAttribute != null )
        {
            connection.setBindPassword( bindPasswordAttribute.getValue() );
        }

        // SASL Realm
        Attribute saslRealmAttribute = element.attribute( SASL_REALM_TAG );

        if ( saslRealmAttribute != null )
        {
            connection.setSaslRealm( saslRealmAttribute.getValue() );
        }

        // SASL Quality of Protection
        Attribute saslQopAttribute = element.attribute( SASL_QOP_TAG );

        if ( saslQopAttribute != null )
        {
            if ( "AUTH_INT_PRIV".equals( saslQopAttribute.getValue() ) ) //$NON-NLS-1$
            {
                // Used for legacy setting (before we used SaslQop enum from Shared)
                connection.setSaslQop( SaslQoP.AUTH_CONF );
            }
            else
            {
                try
                {
                    connection.setSaslQop( SaslQoP.valueOf( saslQopAttribute.getValue() ) );
                }
                catch ( IllegalArgumentException e )
                {
                    throw new ConnectionIOException( "Unable to parse 'SASL Quality of Protection' of connection '" //$NON-NLS-1$
                        + connection.getName() + "' as int value. SASL Quality of Protection value :" //$NON-NLS-1$
                        + saslQopAttribute.getValue() );
                }
            }
        }

        // SASL Security Strength
        Attribute saslSecStrengthAttribute = element.attribute( SASL_SEC_STRENGTH_TAG );

        if ( saslSecStrengthAttribute != null )
        {
            try
            {
                connection
                    .setSaslSecurityStrength( SaslSecurityStrength.valueOf( saslSecStrengthAttribute.getValue() ) );
            }
            catch ( IllegalArgumentException e )
            {
                throw new ConnectionIOException( "Unable to parse 'SASL Security Strength' of connection '" //$NON-NLS-1$
                    + connection.getName() + "' as int value. SASL Security Strength value :" //$NON-NLS-1$
                    + saslSecStrengthAttribute.getValue() );
            }
        }

        // SASL Mutual Authentication
        Attribute saslMutualAuthAttribute = element.attribute( SASL_MUTUAL_AUTH_TAG );

        if ( saslMutualAuthAttribute != null )
        {
            connection.setSaslMutualAuthentication( Boolean.parseBoolean( saslMutualAuthAttribute.getValue() ) );
        }

        // KRB5 Credentials Conf
        Attribute krb5CredentialsConf = element.attribute( KRB5_CREDENTIALS_CONF_TAG );

        if ( krb5CredentialsConf != null )
        {
            try
            {
                connection.setKrb5CredentialConfiguration( Krb5CredentialConfiguration.valueOf( krb5CredentialsConf
                    .getValue() ) );
            }
            catch ( IllegalArgumentException e )
            {
                throw new ConnectionIOException( "Unable to parse 'KRB5 Credentials Conf' of connection '" //$NON-NLS-1$
                    + connection.getName() + "' as int value. KRB5 Credentials Conf value :" //$NON-NLS-1$
                    + krb5CredentialsConf.getValue() );
            }
        }

        // KRB5 Configuration
        Attribute krb5Config = element.attribute( KRB5_CONFIG_TAG );

        if ( krb5Config != null )
        {
            try
            {
                connection.setKrb5Configuration( Krb5Configuration.valueOf( krb5Config.getValue() ) );
            }
            catch ( IllegalArgumentException e )
            {
                throw new ConnectionIOException( "Unable to parse 'KRB5 Configuration' of connection '" //$NON-NLS-1$
                    + connection.getName() + "' as int value. KRB5 Configuration value :" //$NON-NLS-1$
                    + krb5Config.getValue() );
            }
        }

        // KRB5 Configuration File
        Attribute krb5ConfigFile = element.attribute( KRB5_CONFIG_FILE_TAG );

        if ( krb5ConfigFile != null )
        {
            connection.setKrb5ConfigurationFile( krb5ConfigFile.getValue() );
        }

        // KRB5 REALM
        Attribute krb5Realm = element.attribute( KRB5_REALM_TAG );

        if ( krb5Realm != null )
        {
            connection.setKrb5Realm( krb5Realm.getValue() );
        }

        // KRB5 KDC Host
        Attribute krb5KdcHost = element.attribute( KRB5_KDC_HOST_TAG );

        if ( krb5KdcHost != null )
        {
            connection.setKrb5KdcHost( krb5KdcHost.getValue() );
        }

        // KRB5 KDC Port
        Attribute krb5KdcPort = element.attribute( KRB5_KDC_PORT_TAG );

        if ( krb5KdcPort != null )
        {
            try
            {
                connection.setKrb5KdcPort( Integer.valueOf( krb5KdcPort.getValue() ) );
            }
            catch ( NumberFormatException e )
            {
                throw new ConnectionIOException(
                    "Unable to parse 'KRB5 KDC Port' of connection '" + connection.getName() //$NON-NLS-1$
                        + "' as int value. KRB5 KDC Port value :" + krb5KdcPort.getValue() ); //$NON-NLS-1$
            }
        }

        // Read Only
        Attribute readOnly = element.attribute( READ_ONLY_TAG );

        if ( readOnly != null )
        {
            connection.setReadOnly( Boolean.parseBoolean( readOnly.getValue() ) );
        }

        // Connection Protocol (absent in legacy files → default LDAP)
        Attribute connectionProtocolAttribute = element.attribute( CONNECTION_PROTOCOL_TAG );

        if ( connectionProtocolAttribute != null )
        {
            try
            {
                connection.setConnectionProtocol( ConnectionProtocol.valueOf( connectionProtocolAttribute.getValue() ) );
            }
            catch ( IllegalArgumentException e )
            {
                throw new ConnectionIOException( "Unable to parse 'Connection Protocol' of connection '" //$NON-NLS-1$
                    + connection.getName() + "': " + connectionProtocolAttribute.getValue() ); //$NON-NLS-1$
            }
        }

        // Extended Properties
        Element extendedPropertiesElement = element.element( EXTENDED_PROPERTIES_TAG );

        if ( extendedPropertiesElement != null )
        {
            for ( Object elementObject : extendedPropertiesElement.elements( EXTENDED_PROPERTY_TAG ) )
            {
                Element extendedPropertyElement = ( Element ) elementObject;

                Attribute keyAttribute = extendedPropertyElement.attribute( KEY_TAG );
                Attribute valueAttribute = extendedPropertyElement.attribute( VALUE_TAG );

                if ( keyAttribute != null && valueAttribute != null )
                {
                    connection.setExtendedProperty( keyAttribute.getValue(), valueAttribute.getValue() );
                }
            }
        }

        return connection;
    }


    // ── SAVE — WRITE CONNECTIONS.XML TO A STREAM ──────────────────────────────────
    // Chewie writes all the route entries back into the manifest file.
    // We create a fresh DOM document, add a <connection> element for each
    // ConnectionParameter, then write it to the stream in pretty-printed UTF-8.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Serialises the given set of {@link ConnectionParameter} records to the output
     * stream as UTF-8 pretty-printed XML in the {@code connections.xml} format.
     *
     * @param connections  The set of connections to serialise.  May be {@code null}
     *                     (produces an empty {@code <connections/>} document).
     * @param stream       The output stream to write to.
     * @throws IOException  If an I/O error occurs during writing.
     */
    public static void save( Set<ConnectionParameter> connections, OutputStream stream ) throws IOException
    {
        // Creating the Document
        Document document = DocumentHelper.createDocument();

        // Creating the root element
        Element root = document.addElement( CONNECTIONS_TAG );

        if ( connections != null )
        {
            for ( ConnectionParameter connection : connections )
            {
                addConnection( root, connection );
            }
        }

        // Writing the file to disk
        OutputFormat outformat = OutputFormat.createPrettyPrint();
        outformat.setEncoding( "UTF-8" ); //$NON-NLS-1$
        XMLWriter writer = new XMLWriter( stream, outformat );
        writer.write( document );
        writer.flush();
    }


    // ── ADD CONNECTION — APPEND A <connection> ELEMENT TO THE DOCUMENT ────────────
    // We create a <connection> child element under the parent and set every
    // attribute from the ConnectionParameter, including the nested
    // <extendedProperties> block if any plugin-specific properties are present.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@code <connection>} child element under the given parent, populated
     * with all attributes from the given {@link ConnectionParameter}.
     *
     * @param parent      The parent DOM element (the {@code <connections>} root).
     * @param connection  The connection to serialise.
     */
    private static void addConnection( Element parent, ConnectionParameter connection )
    {
        Element connectionElement = parent.addElement( CONNECTION_TAG );

        // ID
        connectionElement.addAttribute( ID_TAG, connection.getId() );

        // Name
        connectionElement.addAttribute( NAME_TAG, connection.getName() );

        // Host
        connectionElement.addAttribute( HOST_TAG, connection.getHost() );

        // Port
        connectionElement.addAttribute( PORT_TAG, Integer.toString( connection.getPort() ) ); //$NON-NLS-1$

        // Encryption Method
        connectionElement.addAttribute( ENCRYPTION_METHOD_TAG, connection.getEncryptionMethod().toString() );

        // Auth Method
        connectionElement.addAttribute( AUTH_METHOD_TAG, connection.getAuthMethod().toString() );

        // Bind Principal
        connectionElement.addAttribute( BIND_PRINCIPAL_TAG, connection.getBindPrincipal() );

        // Bind Password
        connectionElement.addAttribute( BIND_PASSWORD_TAG, connection.getBindPassword() );

        // SASL Realm
        connectionElement.addAttribute( SASL_REALM_TAG, connection.getSaslRealm() );

        // SASL Quality of Protection
        connectionElement.addAttribute( SASL_QOP_TAG, connection.getSaslQop().toString() );

        // SASL Security Strength
        connectionElement.addAttribute( SASL_SEC_STRENGTH_TAG, connection.getSaslSecurityStrength().toString() );

        // SASL Mutual Authentication
        connectionElement.addAttribute( SASL_MUTUAL_AUTH_TAG, Boolean.toString( connection.isSaslMutualAuthentication() ) ); //$NON-NLS-1$

        // KRB5 Credentials Conf
        connectionElement.addAttribute( KRB5_CREDENTIALS_CONF_TAG, connection.getKrb5CredentialConfiguration()
            .toString() );

        // KRB5 Configuration
        connectionElement.addAttribute( KRB5_CONFIG_TAG, connection.getKrb5Configuration().toString() );

        // KRB5 Configuration File
        connectionElement.addAttribute( KRB5_CONFIG_FILE_TAG, connection.getKrb5ConfigurationFile() );

        // KRB5 REALM
        connectionElement.addAttribute( KRB5_REALM_TAG, connection.getKrb5Realm() );

        // KRB5 KDC Host
        connectionElement.addAttribute( KRB5_KDC_HOST_TAG, connection.getKrb5KdcHost() );

        // KRB5 KDC Port
        connectionElement.addAttribute( KRB5_KDC_PORT_TAG, Integer.toString( connection.getKrb5KdcPort() ) ); //$NON-NLS-1$

        // Read Only
        connectionElement.addAttribute( READ_ONLY_TAG, Boolean.toString( connection.isReadOnly() ) ); //$NON-NLS-1$

        // Connection Protocol (only written when non-default to keep LDAP files backward-compatible)
        if ( connection.getConnectionProtocol() != ConnectionProtocol.LDAP )
        {
            connectionElement.addAttribute( CONNECTION_PROTOCOL_TAG, connection.getConnectionProtocol().name() );
        }

        // Connection timeout
        connectionElement.addAttribute( TIMEOUT_TAG, Long.toString( connection.getTimeoutMillis() ) ); //$NON-NLS-1$

        // Extended Properties
        Element extendedPropertiesElement = connectionElement.addElement( EXTENDED_PROPERTIES_TAG );
        Map<String, String> extendedProperties = connection.getExtendedProperties();

        if ( extendedProperties != null )
        {
            for ( Map.Entry<String, String> element : extendedProperties.entrySet() )
            {
                Element extendedPropertyElement = extendedPropertiesElement.addElement( EXTENDED_PROPERTY_TAG );
                extendedPropertyElement.addAttribute( KEY_TAG, element.getKey() );
                extendedPropertyElement.addAttribute( VALUE_TAG, element.getValue() );
            }
        }
    }


    // ── LOAD CONNECTION FOLDERS — READ CONNECTIONFOLDERS.XML ─────────────────────
    // Chewie reads the folder binder manifest — which folders exist, what they
    // contain, and how they nest.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads all {@link ConnectionFolder} records from the given
     * {@code connectionFolders.xml} input stream and returns them as a {@link Set}.
     *
     * @param stream  The input stream pointing to {@code connectionFolders.xml}.
     * @return  A {@link Set} of {@link ConnectionFolder} instances.
     * @throws ConnectionIOException  If the XML is malformed or has the wrong root element.
     */
    public static Set<ConnectionFolder> loadConnectionFolders( InputStream stream ) throws ConnectionIOException
    {
        Set<ConnectionFolder> connectionFolders = new HashSet<>();

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
        if ( !rootElement.getName().equals( CONNECTION_FOLDERS_TAG ) )
        {
            throw new ConnectionIOException( "The file does not seem to be a valid ConnectionFolders file." ); //$NON-NLS-1$
        }

        for ( Iterator<?> i = rootElement.elementIterator( CONNECTION_FOLDER_TAG ); i.hasNext(); )
        {
            Element connectionFolderElement = ( Element ) i.next();
            connectionFolders.add( readConnectionFolder( connectionFolderElement ) );
        }

        return connectionFolders;
    }


    // ── READ CONNECTION FOLDER — PARSE ONE <connectionFolder> ELEMENT ─────────────
    // We read the folder's ID, name, the IDs of its member connections, and the
    // IDs of its child sub-folders.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a single {@code <connectionFolder>} DOM element into a {@link ConnectionFolder}.
     *
     * @param element  The DOM element to parse.
     * @return  The populated {@link ConnectionFolder}.
     */
    private static ConnectionFolder readConnectionFolder( Element element )
    {
        ConnectionFolder connectionFolder = new ConnectionFolder();

        // ID
        Attribute idAttribute = element.attribute( ID_TAG );
        if ( idAttribute != null )
        {
            connectionFolder.setId( idAttribute.getValue() );
        }

        // Name
        Attribute nameAttribute = element.attribute( NAME_TAG );

        if ( nameAttribute != null )
        {
            connectionFolder.setName( nameAttribute.getValue() );
        }

        // Connections
        Element connectionsElement = element.element( CONNECTIONS_TAG );

        if ( connectionsElement != null )
        {
            for ( Iterator<?> i = connectionsElement.elementIterator( CONNECTION_TAG ); i.hasNext(); )
            {
                Element connectionElement = ( Element ) i.next();

                Attribute connectionIdAttribute = connectionElement.attribute( ID_TAG );

                if ( connectionIdAttribute != null )
                {
                    connectionFolder.addConnectionId( connectionIdAttribute.getValue() );
                }
            }
        }

        // Sub-folders
        Element foldersElement = element.element( SUB_FOLDERS_TAG );

        if ( foldersElement != null )
        {
            for ( Iterator<?> i = foldersElement.elementIterator( SUB_FOLDER_TAG ); i.hasNext(); )
            {
                Element folderElement = ( Element ) i.next();

                Attribute folderIdAttribute = folderElement.attribute( ID_TAG );

                if ( folderIdAttribute != null )
                {
                    connectionFolder.addSubFolderId( folderIdAttribute.getValue() );
                }
            }
        }

        return connectionFolder;
    }


    // ── SAVE CONNECTION FOLDERS — WRITE CONNECTIONFOLDERS.XML ─────────────────────
    // Chewie writes the folder binder manifest — all folders, their members, and
    // their child-folder references.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Serialises the given set of {@link ConnectionFolder} records to the output
     * stream as UTF-8 pretty-printed XML in the {@code connectionFolders.xml} format.
     *
     * @param connectionFolders  The set of folders to serialise.  May be {@code null}
     *                           (produces an empty {@code <connectionFolders/>} document).
     * @param stream             The output stream to write to.
     * @throws IOException  If an I/O error occurs during writing.
     */
    public static void saveConnectionFolders( Set<ConnectionFolder> connectionFolders, OutputStream stream )
        throws IOException
    {
        // Creating the Document
        Document document = DocumentHelper.createDocument();

        // Creating the root element
        Element root = document.addElement( CONNECTION_FOLDERS_TAG );

        if ( connectionFolders != null )
        {
            for ( ConnectionFolder connectionFolder : connectionFolders )
            {
                addFolderConnection( root, connectionFolder );
            }
        }

        // Writing the file to disk
        OutputFormat outformat = OutputFormat.createPrettyPrint();
        outformat.setEncoding( "UTF-8" ); //$NON-NLS-1$
        XMLWriter writer = new XMLWriter( stream, outformat );
        writer.write( document );
        writer.flush();
    }


    // ── ADD FOLDER CONNECTION — APPEND A <connectionFolder> ELEMENT ───────────────
    // We create a <connectionFolder> child element and set its ID, name,
    // <connections> child (with IDs of member connections), and <subFolders>
    // child (with IDs of child folders).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@code <connectionFolder>} child element under the given parent, populated
     * with ID, name, connection ID references, and sub-folder ID references.
     *
     * @param parent            The parent DOM element (the {@code <connectionFolders>} root).
     * @param connectionFolder  The folder to serialise.
     */
    private static void addFolderConnection( Element parent, ConnectionFolder connectionFolder )
    {
        Element connectionFolderElement = parent.addElement( CONNECTION_FOLDER_TAG );

        // ID
        connectionFolderElement.addAttribute( ID_TAG, connectionFolder.getId() );

        // Name
        connectionFolderElement.addAttribute( NAME_TAG, connectionFolder.getName() );

        // Connections
        Element connectionsElement = connectionFolderElement.addElement( CONNECTIONS_TAG );

        for ( String connectionId : connectionFolder.getConnectionIds() )
        {
            Element connectionElement = connectionsElement.addElement( CONNECTION_TAG );
            connectionElement.addAttribute( ID_TAG, connectionId );
        }

        // Sub-folders
        Element foldersElement = connectionFolderElement.addElement( SUB_FOLDERS_TAG );

        for ( String folderId : connectionFolder.getSubFolderIds() )
        {
            Element folderElement = foldersElement.addElement( SUB_FOLDER_TAG );
            folderElement.addAttribute( ID_TAG, folderId );
        }
    }
}
