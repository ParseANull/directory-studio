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

package org.apache.directory.studio.ldapbrowser.core.jobs;


import org.apache.directory.studio.connection.core.ConnectionServerType;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IRootDSE;


// ── CLASS: ServerTypeDetector — R2-D2 IDENTIFIES WHICH IMPERIAL FACTION RUNS ─
// R2-D2 plugs into a new computer terminal and immediately starts scanning the
// system's banner: "Imperial Star Destroyer? Death Star? Mon Calamari cruiser?"
// He checks vendor stamps and firmware version strings to figure out what kind
// of system he's talking to before he starts hacking.
// This class does the same for LDAP: it reads the {@code vendorName},
// {@code vendorVersion}, {@code objectClass}, and other Root DSE attributes to
// decide whether we're talking to ApacheDS, Active Directory, OpenLDAP, IBM,
// Netscape, Novell, Sun, Siemens, Red Hat 389, or ForgeRock OpenDJ.
// Knowing the server type lets us activate server-specific features elsewhere.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A utility class that detects the type of LDAP server from its Root DSE entry.
 * Different LDAP server implementations expose different attributes, OIDs, and
 * object classes.  By reading those clues from the Root DSE (which we already
 * loaded during {@link InitializeRootDSERunnable}), we can identify the vendor
 * and version and record it in the connection's
 * {@link org.apache.directory.studio.connection.core.DetectedConnectionProperties}.
 * This powers server-specific UI adaptations elsewhere in the tool.
 * Think of R2-D2 reading the terminal banner to know which Empire he's hacking.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ServerTypeDetector
{
    // ── R2-D2 Runs The Full System Identification Scan ────────────────────────
    // The droid checks vendor strings first (the most reliable method), then
    // falls back to structural clues like rootDomainNamingContext (Active
    // Directory) or OpenLDAProotDSE objectClass (OpenLDAP).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Identifies the LDAP server type from the Root DSE entry.
     * We check {@code vendorName} and {@code vendorVersion} attributes for
     * known vendor strings first.  For servers that don't advertise a vendor
     * name we fall back to structural clues (AD's {@code rootDomainNamingContext},
     * OpenLDAP's {@code OpenLDAProotDSE} objectClass, Siemens'
     * {@code cn=LDAPGlobalSchemaSubentry}).
     *
     * <p>Detectable server types:</p>
     * <ul>
     *   <li>ApacheDS</li>
     *   <li>IBM Directory Server, IBM SecureWay, IBM Tivoli DS</li>
     *   <li>Netscape Directory Server</li>
     *   <li>Novell eDirectory</li>
     *   <li>Sun Directory Server</li>
     *   <li>Red Hat 389 Directory Server / ForgeRock OpenDJ</li>
     *   <li>Microsoft Active Directory 2000 / 2003</li>
     *   <li>OpenLDAP 2.0–2.4</li>
     *   <li>Siemens DirX</li>
     * </ul>
     *
     * @param rootDSE the Root DSE of the connected server; must not be {@code null}.
     * @return the detected {@link ConnectionServerType}, or
     *         {@link ConnectionServerType#UNKNOWN} if we can't identify it.
     */
    public static ConnectionServerType detectServerType( IRootDSE rootDSE )
    {
        ConnectionServerType serverType;

        IAttribute vnAttribute = rootDSE.getAttribute( "vendorName" ); //$NON-NLS-1$
        IAttribute vvAttribute = rootDSE.getAttribute( "vendorVersion" ); //$NON-NLS-1$

        if ( vnAttribute != null && vnAttribute.getStringValues().length > 0 && vvAttribute != null
            && vvAttribute.getStringValues().length > 0 )
        {
            String vendorName = vnAttribute.getStringValues()[0];
            String vendorVersion = vvAttribute.getStringValues()[0];

            // ApacheDS
            serverType = detectApacheDS( vendorName );

            if ( !ConnectionServerType.UNKNOWN.equals( serverType ) )
            {
                return serverType;
            }

            // IBM
            serverType = detectIbm( vendorName, vendorVersion );

            if ( !ConnectionServerType.UNKNOWN.equals( serverType ) )
            {
                return serverType;
            }

            // Netscape
            serverType = detectNetscape( vendorName, vendorVersion );

            if ( !ConnectionServerType.UNKNOWN.equals( serverType ) )
            {
                return serverType;
            }

            // Novell
            serverType = detectNovell( vendorName, vendorVersion );

            if ( !ConnectionServerType.UNKNOWN.equals( serverType ) )
            {
                return serverType;
            }

            // Sun
            serverType = detectSun( vendorName, vendorVersion );

            if ( !ConnectionServerType.UNKNOWN.equals( serverType ) )
            {
                return serverType;
            }

            // RedHat 389
            serverType = detectRedHat389( vendorName, vendorVersion );

            if ( !ConnectionServerType.UNKNOWN.equals( serverType ) )
            {
                return serverType;
            }

            // FrgeRock OpenDJ
            serverType = detectOpenDJ( vendorName, vendorVersion );

            if ( !ConnectionServerType.UNKNOWN.equals( serverType ) )
            {
                return serverType;
            }
        }

        // Microsoft
        serverType = detectMicrosoft( rootDSE );

        if ( !ConnectionServerType.UNKNOWN.equals( serverType ) )
        {
            return serverType;
        }

        // OpenLDAP
        serverType = detectOpenLdap( rootDSE );

        if ( !ConnectionServerType.UNKNOWN.equals( serverType ) )
        {
            return serverType;
        }

        // Siemens
        serverType = detectSiemens( rootDSE );

        if ( !ConnectionServerType.UNKNOWN.equals( serverType ) )
        {
            return serverType;
        }

        return ConnectionServerType.UNKNOWN;
    }


    // ── R2-D2 Checks For The Apache Banner ────────────────────────────────────
    // "Apache Software Foundation" in vendorName → ApacheDS confirmed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the {@code vendorName} identifies an ApacheDS server.
     *
     * @param vendorName the {@code vendorName} Root DSE attribute value.
     * @return {@link ConnectionServerType#APACHEDS} or {@link ConnectionServerType#UNKNOWN}.
     */
    private static ConnectionServerType detectApacheDS( String vendorName )
    {
        if ( vendorName.indexOf( "Apache Software Foundation" ) > -1 ) //$NON-NLS-1$
        {
            return ConnectionServerType.APACHEDS;
        }

        return ConnectionServerType.UNKNOWN;
    }


    // ── R2-D2 Checks For IBM's Firmware Stamp ────────────────────────────────
    // "International Business Machines" in vendorName, then version number
    // ranges to distinguish SecureWay, Directory Server, and Tivoli DS.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the vendor strings identify an IBM directory server.
     * Version ranges are used to distinguish IBM SecureWay (3.2.x), IBM
     * Directory Server (4.1, 5.1), and IBM Tivoli Directory Server (5.2+).
     *
     * @param vendorName    the {@code vendorName} attribute value.
     * @param vendorVersion the {@code vendorVersion} attribute value.
     * @return one of the IBM {@link ConnectionServerType} constants, or
     *         {@link ConnectionServerType#UNKNOWN}.
     */
    private static ConnectionServerType detectIbm( String vendorName, String vendorVersion )
    {
        // IBM
        if ( vendorName.indexOf( "International Business Machines" ) > -1 ) //$NON-NLS-1$
        {
            // IBM SecureWay Directory
            String[] iswVersions = { "3.2", "3.2.1", "3.2.2" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            for ( String version : iswVersions )
            {
                if ( vendorVersion.indexOf( version ) > -1 )
                {
                    return ConnectionServerType.IBM_SECUREWAY_DIRECTORY;
                }
            }

            // IBM Directory Server
            String[] idsVersions = { "4.1", "5.1" }; //$NON-NLS-1$ //$NON-NLS-2$

            for ( String version : idsVersions )
            {
                if ( vendorVersion.indexOf( version ) > -1 )
                {
                    return ConnectionServerType.IBM_DIRECTORY_SERVER;
                }
            }

            // IBM Tivoli Directory Server
            String[] tdsVersions = { "5.2", "6.0", "6.1", "6.2" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

            for ( String version : tdsVersions )
            {
                if ( vendorVersion.indexOf( version ) > -1 )
                {
                    return ConnectionServerType.IBM_TIVOLI_DIRECTORY_SERVER;
                }
            }
        }

        return ConnectionServerType.UNKNOWN;
    }


    // ── R2-D2 Looks For The Coruscant Imperial Crest (Active Directory) ───────
    // Active Directory puts a {@code rootDomainNamingContext} in the Root DSE;
    // {@code forestFunctionality} distinguishes 2003 from 2000.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the Root DSE identifies a Microsoft Active Directory server.
     * AD is identified by the presence of {@code rootDomainNamingContext}.
     * The presence of {@code forestFunctionality} further distinguishes AD 2003.
     *
     * @param rootDSE the Root DSE entry.
     * @return {@link ConnectionServerType#MICROSOFT_ACTIVE_DIRECTORY_2003},
     *         {@link ConnectionServerType#MICROSOFT_ACTIVE_DIRECTORY_2000}, or
     *         {@link ConnectionServerType#UNKNOWN}.
     */
    private static ConnectionServerType detectMicrosoft( IRootDSE rootDSE )
    {
        if ( rootDSE.getAttribute( "rootDomainNamingContext" ) != null )
        {
            if ( rootDSE.getAttribute( "forestFunctionality" ) != null )
            {
                return ConnectionServerType.MICROSOFT_ACTIVE_DIRECTORY_2003;
            }
            else
            {
                return ConnectionServerType.MICROSOFT_ACTIVE_DIRECTORY_2000;
            }
        }

        return ConnectionServerType.UNKNOWN;
    }


    // ── R2-D2 Spots The Netscape Logo ─────────────────────────────────────────
    // Either vendorName or vendorVersion contains "Netscape".
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the vendor strings identify a Netscape Directory Server.
     *
     * @param vendorName    the {@code vendorName} attribute value.
     * @param vendorVersion the {@code vendorVersion} attribute value.
     * @return {@link ConnectionServerType#NETSCAPE} or {@link ConnectionServerType#UNKNOWN}.
     */
    private static ConnectionServerType detectNetscape( String vendorName, String vendorVersion )
    {
        if ( vendorName.indexOf( "Netscape" ) > -1 //$NON-NLS-1$
            || vendorVersion.indexOf( "Netscape" ) > -1 ) //$NON-NLS-1$
        {
            return ConnectionServerType.NETSCAPE;
        }

        return ConnectionServerType.UNKNOWN;
    }


    // ── R2-D2 Finds The Novell Insignia ───────────────────────────────────────
    // "Novell" in vendorName or "eDirectory" in vendorVersion.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the vendor strings identify a Novell eDirectory server.
     *
     * @param vendorName    the {@code vendorName} attribute value.
     * @param vendorVersion the {@code vendorVersion} attribute value.
     * @return {@link ConnectionServerType#NOVELL} or {@link ConnectionServerType#UNKNOWN}.
     */
    private static ConnectionServerType detectNovell( String vendorName, String vendorVersion )
    {
        if ( vendorName.indexOf( "Novell" ) > -1 //$NON-NLS-1$
            || vendorVersion.indexOf( "eDirectory" ) > -1 ) //$NON-NLS-1$
        {
            return ConnectionServerType.NOVELL;
        }

        return ConnectionServerType.UNKNOWN;
    }


    // ── R2-D2 Reads The OpenLDAP Structural Clue ──────────────────────────────
    // OpenLDAP puts "OpenLDAProotDSE" in the objectClass of the Root DSE.
    // We then narrow down the version by checking which OIDs are in
    // supportedControl and supportedFeatures.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the Root DSE identifies an OpenLDAP server and which version.
     * OpenLDAP is identified by the {@code OpenLDAProotDSE} objectClass value.
     * We then check for version-specific OIDs to narrow down whether it's
     * OpenLDAP 2.4, 2.3, 2.2, 2.1, 2.0, or just generic OpenLDAP.
     *
     * @param rootDSE the Root DSE entry.
     * @return one of the OpenLDAP {@link ConnectionServerType} constants, or
     *         {@link ConnectionServerType#UNKNOWN}.
     */
    private static ConnectionServerType detectOpenLdap( IRootDSE rootDSE )
    {
        IAttribute ocAttribute = rootDSE.getAttribute( "objectClass" ); //$NON-NLS-1$

        if ( ocAttribute != null )
        {
            for ( int i = 0; i < ocAttribute.getStringValues().length; i++ )
            {
                if ( "OpenLDAProotDSE".equals( ocAttribute.getStringValues()[i] ) ) //$NON-NLS-1$
                {
                    IAttribute scAttribute = rootDSE.getAttribute( "supportedControl" ); //$NON-NLS-1$

                    // Check for the new "Don't Use Copy" Control (RFC 6171) that has been added in OpenLDAP 2.4
                    if ( scAttribute != null )
                    {
                        for ( int sci = 0; sci < scAttribute.getStringValues().length; sci++ )
                        {
                            if ( "1.3.6.1.1.22".equals( scAttribute.getStringValues()[sci] ) ) //$NON-NLS-1$
                            {
                                return ConnectionServerType.OPENLDAP_2_4;
                            }
                        }
                    }

                    // ConfigContext has been added in OpenLDAP 2.3
                    if ( rootDSE.getAttribute( "configContext" ) != null )
                    {
                        return ConnectionServerType.OPENLDAP_2_3;
                    }

                    // Proxy Auth control has been added in OpenLDAP 2.0
                    if ( scAttribute != null )
                    {
                        for ( int sci = 0; sci < scAttribute.getStringValues().length; sci++ )
                        {
                            if ( "2.16.840.1.113730.3.4.18".equals( scAttribute.getStringValues()[sci] ) ) //$NON-NLS-1$
                            {
                                return ConnectionServerType.OPENLDAP_2_2;
                            }
                        }
                    }

                    // Check for the 'Who Am I' extended operation, added in OpenLDAP 2.1
                    IAttribute seAttribute = rootDSE.getAttribute( "supportedExtension" ); //$NON-NLS-1$

                    if ( seAttribute != null )
                    {
                        for ( int sei = 0; sei < seAttribute.getStringValues().length; sei++ )
                        {
                            if ( "1.3.6.1.4.1.4203.1.11.3".equals( seAttribute.getStringValues()[sei] ) ) //$NON-NLS-1$
                            {
                                return ConnectionServerType.OPENLDAP_2_1;
                            }
                        }
                    }

                    // The 'Language Tag' feature has been added in OpenLDAP 2.0
                    IAttribute sfAttribute = rootDSE.getAttribute( "supportedFeatures" ); //$NON-NLS-1$

                    if ( sfAttribute != null )
                    {
                        for ( int sfi = 0; sfi < sfAttribute.getStringValues().length; sfi++ )
                        {
                            if ( "1.3.6.1.4.1.4203.1.5.4".equals( sfAttribute.getStringValues()[sfi] ) ) //$NON-NLS-1$
                            {
                                return ConnectionServerType.OPENLDAP_2_0;
                            }
                        }
                    }

                    return ConnectionServerType.OPENLDAP;
                }
            }
        }

        return ConnectionServerType.UNKNOWN;
    }


    // ── R2-D2 Spots The Siemens DirX Seal ────────────────────────────────────
    // Siemens DirX uses a very specific subSchemaSubentry value that gives it
    // away: "cn=LDAPGlobalSchemaSubentry".
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the Root DSE identifies a Siemens DirX server.
     * Siemens DirX is identified by a specific {@code subSchemaSubentry} value.
     *
     * @param rootDSE the Root DSE entry.
     * @return {@link ConnectionServerType#SIEMENS_DIRX} or {@link ConnectionServerType#UNKNOWN}.
     */
    private static ConnectionServerType detectSiemens( IRootDSE rootDSE )
    {
        IAttribute ssseAttribute = rootDSE.getAttribute( "subSchemaSubentry" ); //$NON-NLS-1$

        if ( ssseAttribute != null )
        {
            for ( int i = 0; i < ssseAttribute.getStringValues().length; i++ )
            {
                if ( "cn=LDAPGlobalSchemaSubentry".equals( ssseAttribute.getStringValues()[i] ) ) //$NON-NLS-1$
                {
                    return ConnectionServerType.SIEMENS_DIRX;
                }
            }
        }

        return ConnectionServerType.UNKNOWN;
    }


    // ── R2-D2 Finds The Sun Microsystems Logo ─────────────────────────────────
    // "Sun" in either vendorName or vendorVersion → Sun Directory Server.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the vendor strings identify a Sun Directory Server.
     *
     * @param vendorName    the {@code vendorName} attribute value.
     * @param vendorVersion the {@code vendorVersion} attribute value.
     * @return {@link ConnectionServerType#SUN_DIRECTORY_SERVER} or {@link ConnectionServerType#UNKNOWN}.
     */
    private static ConnectionServerType detectSun( String vendorName, String vendorVersion )
    {
        if ( vendorName.indexOf( "Sun" ) > -1 //$NON-NLS-1$
            || vendorVersion.indexOf( "Sun" ) > -1 ) //$NON-NLS-1$
        {
            return ConnectionServerType.SUN_DIRECTORY_SERVER;
        }

        return ConnectionServerType.UNKNOWN;
    }


    // ── R2-D2 Reads The Red Hat 389 Patch Notes ───────────────────────────────
    // "389 Project" in vendorName or "389-Directory" in vendorVersion.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the vendor strings identify a Red Hat 389 Directory Server.
     *
     * @param vendorName    the {@code vendorName} attribute value.
     * @param vendorVersion the {@code vendorVersion} attribute value.
     * @return {@link ConnectionServerType#RED_HAT_389} or {@link ConnectionServerType#UNKNOWN}.
     */
    private static ConnectionServerType detectRedHat389( String vendorName, String vendorVersion )
    {
        if ( vendorName.indexOf( "389 Project" ) > -1 //$NON-NLS-1$
            || vendorVersion.indexOf( "389-Directory" ) > -1 ) //$NON-NLS-1$
        {
            return ConnectionServerType.RED_HAT_389;
        }

        return ConnectionServerType.UNKNOWN;
    }


    // ── R2-D2 Finds The ForgeRock OpenDJ Crest ───────────────────────────────
    // "ForgeRock" in vendorName or "OpenDJ" in vendorVersion.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the vendor strings identify a ForgeRock OpenDJ server.
     *
     * @param vendorName    the {@code vendorName} attribute value.
     * @param vendorVersion the {@code vendorVersion} attribute value.
     * @return {@link ConnectionServerType#FORGEROCK_OPEN_DJ} or {@link ConnectionServerType#UNKNOWN}.
     */
    private static ConnectionServerType detectOpenDJ( String vendorName, String vendorVersion )
    {
        if ( vendorName.indexOf( "ForgeRock" ) > -1 //$NON-NLS-1$
            || vendorVersion.indexOf( "OpenDJ" ) > -1 ) //$NON-NLS-1$
        {
            return ConnectionServerType.FORGEROCK_OPEN_DJ;
        }

        return ConnectionServerType.UNKNOWN;
    }
}
