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

package org.apache.directory.studio.connection.core;


import java.util.List;


// ── CLASS: DetectedConnectionProperties — R2-D2 REPORTS BACK FROM HIS SCAN ───
// After R2-D2 plugs into an unknown ship's computer, he runs a full scan and
// comes back with a report: "it's an X-Wing, Mark IV, supports these shields
// and these weapons systems."
// This class holds the results of that scan — everything we auto-discover about
// a server by reading its rootDSE after we connect: vendor name, server type,
// supported LDAP controls, extensions, and features.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Holds server properties that we auto-detect by reading the rootDSE immediately
 * after connecting.
 * The rootDSE is the LDAP server's "about me" entry — it advertises supported
 * controls, extensions, features, vendor info, and LDAP version.
 * We cache all of this in the connection's extended properties map so it persists
 * across sessions without requiring a fresh scan every time.
 * Think of this class as R2-D2's scan report: he connects, reads the ship's
 * manifest, and files a structured report that the crew can query.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DetectedConnectionProperties
{
    /** The key for the connection parameter "Vendor name" */
    public static final String CONNECTION_PARAMETER_VENDOR_NAME = "detectedProperties.vendorName"; //$NON-NLS-1$

    /** The key for the connection parameter "Vendor version" */
    public static final String CONNECTION_PARAMETER_VENDOR_VERSION = "detectedProperties.vendorVersion"; //$NON-NLS-1$

    /** The key for the connection parameter "Server type" */
    public static final String CONNECTION_PARAMETER_SERVER_TYPE = "detectedProperties.serverType"; //$NON-NLS-1$

    /** The key for the connection parameter "Supported LDAP versions" */
    public static final String CONNECTION_PARAMETER_SUPPORTED_LDAP_VERSIONS = "detectedProperties.supportedLdapVersions"; //$NON-NLS-1$

    /** The key for the connection parameter "Supported SASL mechanisms" */
    public static final String CONNECTION_PARAMETER_SUPPORTED_SASL_MECHANISMS = "detectedProperties.supportedSaslMechanisms"; //$NON-NLS-1$

    /** The key for the connection parameter "Supported controls" */
    public static final String CONNECTION_PARAMETER_SUPPORTED_CONTROLS = "detectedProperties.supportedControls"; //$NON-NLS-1$

    /** The key for the connection parameter "Supported extensions" */
    public static final String CONNECTION_PARAMETER_SUPPORTED_EXTENSIONS = "detectedProperties.supportedExtensions"; //$NON-NLS-1$

    /** The key for the connection parameter "Supported features" */
    public static final String CONNECTION_PARAMETER_SUPPORTED_FEATURES = "detectedProperties.supportedFeatures"; //$NON-NLS-1$

    /** The connection */
    public Connection connection;


    // ── CONSTRUCTOR — R2 INITIALIZES HIS SCAN LOG FOR THIS SHIP ──────────────────
    // R2-D2 opens a fresh scan log for the target ship, ready to record whatever
    // his sensors find when he plugs in.
    // We store a reference to the connection so we can read/write its extended
    // properties as our persistence layer.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link DetectedConnectionProperties} for the given connection.
     * We store data in the connection's extended-properties map rather than in separate
     * fields so everything persists together in connections.xml.
     *
     * @param connection  The connection whose rootDSE properties we represent.
     */
    public DetectedConnectionProperties( Connection connection )
    {
        this.connection = connection;
    }


    // ── GET SERVER TYPE — R2 IDENTIFIES THE SHIP CLASS ────────────────────────────
    // R2 reads the ship-class identifier out of the scan log and translates it
    // into a known type from his database.
    // We parse the stored string back to a ConnectionServerType enum constant.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the auto-detected server type.
     * We read the stored string from the extended properties map and parse it back
     * to a {@link ConnectionServerType} enum constant.
     * Returns {@link ConnectionServerType#UNKNOWN} if the value is missing or unrecognizable.
     *
     * @return  The detected {@link ConnectionServerType}.
     */
    public ConnectionServerType getServerType()
    {
        try
        {
            String serverType = connection.getConnectionParameter().getExtendedProperty(
                CONNECTION_PARAMETER_SERVER_TYPE );

            if ( serverType != null )
            {
                return ConnectionServerType.valueOf( serverType );
            }
            else
            {
                return ConnectionServerType.UNKNOWN;
            }
        }
        catch ( IllegalArgumentException e )
        {
            return ConnectionServerType.UNKNOWN;
        }
    }


    // ── GET SUPPORTED CONTROLS — R2 LISTS THE SHIP'S WEAPON SYSTEMS ──────────────
    // R2 reads the scan log entry for supported weapon systems (LDAP control OIDs).
    // We return the list of OID strings stored in the extended properties.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of LDAP control OIDs advertised by the server in its rootDSE.
     * Controls are extensions to LDAP operations — for example paged results (1.2.840.113556.1.4.319).
     * We read the semicolon-delimited list from extended properties.
     *
     * @return  A list of OID strings, or {@code null} if not detected yet.
     */
    public List<String> getSupportedControls()
    {
        return connection.getConnectionParameter().getExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_CONTROLS );
    }


    // ── GET SUPPORTED EXTENSIONS — R2 LISTS THE SHIP'S DOCKING EXTENSIONS ─────────
    // R2 reads which extended docking protocols the ship supports.
    // We return the list of LDAP extension OIDs.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of LDAP extended-operation OIDs advertised by the server.
     * Extended operations are custom commands beyond the standard LDAP set —
     * for example StartTLS (1.3.6.1.4.1.1466.20037) or password modify.
     *
     * @return  A list of OID strings, or {@code null} if not detected yet.
     */
    public List<String> getSupportedExtensions()
    {
        return connection.getConnectionParameter().getExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_EXTENSIONS );
    }


    // ── GET SUPPORTED FEATURES — R2 LISTS THE SHIP'S OPTIONAL CAPABILITIES ────────
    // R2 reads which optional fleet capabilities the ship has enabled.
    // We return the list of LDAP feature OIDs.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of LDAP feature OIDs advertised by the server.
     * Features are optional RFC capabilities like "modify-increment" or "pre/post-read".
     *
     * @return  A list of OID strings, or {@code null} if not detected yet.
     */
    public List<String> getSupportedFeatures()
    {
        return connection.getConnectionParameter().getExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_FEATURES );
    }


    // ── GET SUPPORTED LDAP VERSIONS — R2 READS THE PROTOCOL VERSION LIST ──────────
    // R2 reads which versions of the LDAP protocol the server speaks.
    // We return the list of version strings (typically ["3"]).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP protocol versions supported by the server.
     * Virtually every modern server will return just {@code ["3"]}, but it's worth
     * checking.
     *
     * @return  A list of version strings, or {@code null} if not detected yet.
     */
    public List<String> getSupportedLdapVersions()
    {
        return connection.getConnectionParameter().getExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_LDAP_VERSIONS );
    }


    // ── GET SUPPORTED SASL MECHANISMS — R2 LISTS THE AUTH HANDSHAKE TYPES ─────────
    // R2 reads which authentication protocols the ship's security system accepts.
    // We return the list of SASL mechanism names.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SASL mechanisms advertised by the server.
     * Common values: PLAIN, DIGEST-MD5, GSSAPI, EXTERNAL.
     * We use this to know which SASL auth methods are actually available.
     *
     * @return  A list of SASL mechanism name strings, or {@code null} if not detected.
     */
    public List<String> getSupportedSaslMechanisms()
    {
        return connection.getConnectionParameter().getExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_SASL_MECHANISMS );
    }


    // ── GET VENDOR NAME — R2 READS THE MANUFACTURER PLATE ────────────────────────
    // R2 scans the manufacturer's plate on the ship's hull: "OpenLDAP Foundation."
    // We return the vendorName attribute from the rootDSE.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the server's vendor name as reported in its rootDSE.
     * For example: {@code "Apache Software Foundation"} or {@code "Microsoft Corporation"}.
     *
     * @return  The vendor name string, or {@code null} if the server didn't report one.
     */
    public String getVendorName()
    {
        return connection.getConnectionParameter().getExtendedProperty( CONNECTION_PARAMETER_VENDOR_NAME );
    }


    // ── GET VENDOR VERSION — R2 READS THE FIRMWARE VERSION ───────────────────────
    // R2 reads the firmware version sticker: "OpenLDAP 2.6.3."
    // We return the vendorVersion attribute from the rootDSE.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the server's vendor version string as reported in its rootDSE.
     * For example: {@code "2.6.3"} or {@code "6.3.1"}.
     *
     * @return  The vendor version string, or {@code null} if not reported.
     */
    public String getVendorVersion()
    {
        return connection.getConnectionParameter().getExtendedProperty( CONNECTION_PARAMETER_VENDOR_VERSION );
    }


    // ── SET SERVER TYPE — R2 STAMPS THE SHIP CLASS IN THE LOG ────────────────────
    // R2 finishes his scan, identifies the ship class, and stamps it in the log.
    // We persist the server type as a string in the extended properties.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the detected server type.
     * We call this after probing the rootDSE and matching it against known server signatures.
     *
     * @param serverType  The detected {@link ConnectionServerType} (passed as Object so
     *                    callers can use the enum without a cast).
     */
    public void setServerType( Object serverType )
    {
        connection.getConnectionParameter().setExtendedProperty( CONNECTION_PARAMETER_SERVER_TYPE,
            serverType.toString() );
    }


    // ── SET SUPPORTED CONTROLS — R2 RECORDS THE WEAPON SYSTEM LIST ───────────────
    // R2 writes the list of supported weapon systems (control OIDs) into the log.
    // We persist the list as a semicolon-delimited string in extended properties.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the list of LDAP control OIDs supported by the server.
     *
     * @param supportedControls  The list of OID strings to store.
     */
    public void setSupportedControls( List<String> supportedControls )
    {
        connection.getConnectionParameter().setExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_CONTROLS,
            supportedControls );
    }


    // ── SET SUPPORTED EXTENSIONS — R2 RECORDS THE DOCKING EXTENSION LIST ──────────
    // R2 records which extended docking protocols this ship supports.
    // We persist the list as a semicolon-delimited string.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the list of LDAP extended-operation OIDs supported by the server.
     *
     * @param supportedExtensions  The list of OID strings to store.
     */
    public void setSupportedExtensions( List<String> supportedExtensions )
    {
        connection.getConnectionParameter().setExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_EXTENSIONS,
            supportedExtensions );
    }


    // ── SET SUPPORTED FEATURES — R2 RECORDS THE OPTIONAL CAPABILITY LIST ──────────
    // R2 records which optional fleet capabilities this ship has enabled.
    // We persist the list as a semicolon-delimited string.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the list of LDAP feature OIDs supported by the server.
     *
     * @param supportedFeatures  The list of OID strings to store.
     */
    public void setSupportedFeatures( List<String> supportedFeatures )
    {
        connection.getConnectionParameter().setExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_FEATURES,
            supportedFeatures );
    }


    // ── SET SUPPORTED LDAP VERSIONS — R2 RECORDS THE PROTOCOL VERSION LIST ────────
    // R2 records which LDAP protocol versions this server speaks.
    // We persist the list as a semicolon-delimited string.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the list of LDAP protocol versions supported by the server.
     *
     * @param supportedLdapVersions  The list of version strings to store.
     */
    public void setSupportedLdapVersions( List<String> supportedLdapVersions )
    {
        connection.getConnectionParameter().setExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_LDAP_VERSIONS,
            supportedLdapVersions );
    }


    // ── SET SUPPORTED SASL MECHANISMS — R2 RECORDS THE AUTH PROTOCOL LIST ─────────
    // R2 records which authentication handshake types the security system accepts.
    // We persist the list as a semicolon-delimited string.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the list of SASL mechanisms supported by the server.
     *
     * @param supportedSaslMechanisms  The list of SASL mechanism name strings to store.
     */
    public void setSupportedSaslMechanisms( List<String> supportedSaslMechanisms )
    {
        connection.getConnectionParameter().setExtendedListStringProperty(
            CONNECTION_PARAMETER_SUPPORTED_SASL_MECHANISMS,
            supportedSaslMechanisms );
    }


    // ── SET VENDOR NAME — R2 STAMPS THE MANUFACTURER ON THE LOG ──────────────────
    // R2 writes the manufacturer's name into the scan log.
    // We store the vendor name in the extended properties.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the server's vendor name.
     *
     * @param vendorName  The vendor name string from the rootDSE.
     */
    public void setVendorName( String vendorName )
    {
        connection.getConnectionParameter().setExtendedProperty( CONNECTION_PARAMETER_VENDOR_NAME, vendorName );
    }


    // ── SET VENDOR VERSION — R2 STAMPS THE FIRMWARE VERSION ON THE LOG ────────────
    // R2 writes the firmware version into the scan log.
    // We store the vendor version in the extended properties.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the server's vendor version string.
     *
     * @param vendorVersion  The vendor version string from the rootDSE.
     */
    public void setVendorVersion( String vendorVersion )
    {
        connection.getConnectionParameter().setExtendedProperty( CONNECTION_PARAMETER_VENDOR_VERSION, vendorVersion );
    }
}
