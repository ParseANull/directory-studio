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
package org.apache.directory.studio.apacheds.configuration.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// ── CLASS: SupportedCipher — IMPERIAL SHIELD-FREQUENCY CODEBOOK ──────────────────────────
// The Death Star's communications array supports a long list of encryption protocols.
// Some work on the old Mark I targeting computer (Java 7); some only on the Mark II (Java 8);
// a few are intentionally disabled because they're known to be weak — like leaving the
// exhaust port unguarded.  This enum is that codebook: every supported cipher suite, its
// name, and exactly which Java generations support and enable it.
// The three-state Boolean per Java version (TRUE = enabled, FALSE = disabled, null = not
// implemented) lets the UI show checkboxes that correctly reflect the real JVM capability.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * Enumerates all TLS/SSL cipher suites that ApacheDS can be configured to use, along with
 * their enabled/disabled/unsupported status for Java 7 and Java 8.
 * The three states per Java version are: {@code Boolean.TRUE} = enabled,
 * {@code Boolean.FALSE} = disabled, {@code null} = not implemented in that Java version.
 * Static lists and a lookup map are pre-built in a static initialiser for fast UI access.
 * Think of it as the Imperial shield-frequency codebook: every cipher variant, colour-coded
 * by which Death Star revision can actually use it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum SupportedCipher
{
    // Enabled ciphers
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384( "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384( "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", Boolean.TRUE, Boolean.TRUE),
    TLS_RSA_WITH_AES_256_CBC_SHA256( "TLS_RSA_WITH_AES_256_CBC_SHA256", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384( "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384( "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384", Boolean.TRUE, Boolean.TRUE),
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA256( "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256", Boolean.TRUE, Boolean.TRUE),
    TLS_DHE_DSS_WITH_AES_256_CBC_SHA256( "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA( "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA( "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_RSA_WITH_AES_256_CBC_SHA( "TLS_RSA_WITH_AES_256_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA( "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDH_RSA_WITH_AES_256_CBC_SHA( "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA( "TLS_DHE_RSA_WITH_AES_256_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_DHE_DSS_WITH_AES_256_CBC_SHA( "TLS_DHE_DSS_WITH_AES_256_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256( "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256( "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", Boolean.TRUE, Boolean.TRUE),
    TLS_RSA_WITH_AES_128_CBC_SHA256( "TLS_RSA_WITH_AES_128_CBC_SHA256", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256( "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256( "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256", Boolean.TRUE, Boolean.TRUE),
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA256( "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256", Boolean.TRUE, Boolean.TRUE),
    TLS_DHE_DSS_WITH_AES_128_CBC_SHA256( "TLS_DHE_DSS_WITH_AES_128_CBC_SHA256", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA( "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA( "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_RSA_WITH_AES_128_CBC_SHA( "TLS_RSA_WITH_AES_128_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA( "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDH_RSA_WITH_AES_128_CBC_SHA( "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA( "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_DHE_DSS_WITH_AES_128_CBC_SHA( "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDHE_ECDSA_WITH_RC4_128_SHA( "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDHE_RSA_WITH_RC4_128_SHA( "TLS_ECDHE_RSA_WITH_RC4_128_SHA", Boolean.TRUE, Boolean.TRUE),
    SSL_RSA_WITH_RC4_128_SHA( "SSL_RSA_WITH_RC4_128_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDH_ECDSA_WITH_RC4_128_SHA( "TLS_ECDH_ECDSA_WITH_RC4_128_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDH_RSA_WITH_RC4_128_SHA( "TLS_ECDH_RSA_WITH_RC4_128_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384( "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", null, Boolean.TRUE),
    TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256( "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", null, Boolean.TRUE),
    TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384( "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", null, Boolean.TRUE),
    TLS_RSA_WITH_AES_256_GCM_SHA384( "TLS_RSA_WITH_AES_256_GCM_SHA384", null, Boolean.TRUE),
    TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384( "TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384", null, Boolean.TRUE),
    TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384( "TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384", null, Boolean.TRUE),
    TLS_DHE_RSA_WITH_AES_256_GCM_SHA384( "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384", null, Boolean.TRUE),
    TLS_DHE_DSS_WITH_AES_256_GCM_SHA384( "TLS_DHE_DSS_WITH_AES_256_GCM_SHA384", null, Boolean.TRUE),
    TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256( "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", null, Boolean.TRUE),
    TLS_RSA_WITH_AES_128_GCM_SHA256( "TLS_RSA_WITH_AES_128_GCM_SHA256", null, Boolean.TRUE),
    TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256( "TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256", null, Boolean.TRUE),
    TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256( "TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256", null, Boolean.TRUE),
    TLS_DHE_RSA_WITH_AES_128_GCM_SHA256( "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256", null, Boolean.TRUE),
    TLS_DHE_DSS_WITH_AES_128_GCM_SHA256( "TLS_DHE_DSS_WITH_AES_128_GCM_SHA256", null, Boolean.TRUE),
    TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA( "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA( "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    SSL_RSA_WITH_3DES_EDE_CBC_SHA( "SSL_RSA_WITH_3DES_EDE_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA( "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA( "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA( "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA( "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", Boolean.TRUE, Boolean.TRUE),
    SSL_RSA_WITH_RC4_128_MD5( "SSL_RSA_WITH_RC4_128_MD5", Boolean.TRUE, Boolean.TRUE),
    TLS_EMPTY_RENEGOTIATION_INFO_SCSV( "TLS_EMPTY_RENEGOTIATION_INFO_SCSV", Boolean.TRUE, Boolean.TRUE),

    // Disabled ciphers
    TLS_DH_anon_WITH_AES_256_GCM_SHA384( "TLS_DH_anon_WITH_AES_256_GCM_SHA384", null, Boolean.FALSE ),
    TLS_DH_anon_WITH_AES_128_GCM_SHA256( "TLS_DH_anon_WITH_AES_128_GCM_SHA256", null, Boolean.FALSE ),
    TLS_DH_anon_WITH_AES_256_CBC_SHA256( "TLS_DH_anon_WITH_AES_256_CBC_SHA256", Boolean.FALSE, Boolean.FALSE ),
    TLS_ECDH_anon_WITH_AES_256_CBC_SHA( "TLS_ECDH_anon_WITH_AES_256_CBC_SHA", Boolean.FALSE, Boolean.FALSE ),
    TLS_DH_anon_WITH_AES_256_CBC_SHA( "TLS_DH_anon_WITH_AES_256_CBC_SHA", Boolean.FALSE, Boolean.FALSE ),
    TLS_DH_anon_WITH_AES_128_CBC_SHA256( "TLS_DH_anon_WITH_AES_128_CBC_SHA256", Boolean.FALSE, Boolean.FALSE ),
    TLS_ECDH_anon_WITH_AES_128_CBC_SHA( "TLS_ECDH_anon_WITH_AES_128_CBC_SHA", Boolean.FALSE, Boolean.FALSE ),
    TLS_DH_anon_WITH_AES_128_CBC_SHA( "TLS_DH_anon_WITH_AES_128_CBC_SHA", Boolean.FALSE, Boolean.FALSE ),
    TLS_ECDH_anon_WITH_RC4_128_SHA( "TLS_ECDH_anon_WITH_RC4_128_SHA", Boolean.FALSE, Boolean.FALSE ),
    SSL_DH_anon_WITH_RC4_128_MD5( "SSL_DH_anon_WITH_RC4_128_MD5", Boolean.FALSE, Boolean.FALSE ),
    TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA( "TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA", Boolean.FALSE, Boolean.FALSE ),
    SSL_DH_anon_WITH_3DES_EDE_CBC_SHA( "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA", Boolean.FALSE, Boolean.FALSE ),
    TLS_RSA_WITH_NULL_SHA256( "TLS_RSA_WITH_NULL_SHA256", Boolean.FALSE, Boolean.FALSE ),
    TLS_ECDHE_ECDSA_WITH_NULL_SHA( "TLS_ECDHE_ECDSA_WITH_NULL_SHA", Boolean.FALSE, Boolean.FALSE ),
    TLS_ECDHE_RSA_WITH_NULL_SHA( "TLS_ECDHE_RSA_WITH_NULL_SHA", Boolean.FALSE, Boolean.FALSE ),
    SSL_RSA_WITH_NULL_SHA( "SSL_RSA_WITH_NULL_SHA", Boolean.FALSE, Boolean.FALSE ),
    TLS_ECDH_ECDSA_WITH_NULL_SHA( "TLS_ECDH_ECDSA_WITH_NULL_SHA", Boolean.FALSE, Boolean.FALSE ),
    TLS_ECDH_RSA_WITH_NULL_SHA( "TLS_ECDH_RSA_WITH_NULL_SHA", Boolean.FALSE, Boolean.FALSE ),
    TLS_ECDH_anon_WITH_NULL_SHA( "TLS_ECDH_anon_WITH_NULL_SHA", Boolean.FALSE, Boolean.FALSE ),
    SSL_RSA_WITH_NULL_MD5( "SSL_RSA_WITH_NULL_MD5", Boolean.FALSE, Boolean.FALSE ),
    SSL_RSA_WITH_DES_CBC_SHA( "SSL_RSA_WITH_DES_CBC_SHA", Boolean.FALSE, Boolean.FALSE ),
    SSL_DHE_RSA_WITH_DES_CBC_SHA( "SSL_DHE_RSA_WITH_DES_CBC_SHA", Boolean.FALSE, Boolean.FALSE ),
    SSL_DHE_DSS_WITH_DES_CBC_SHA( "SSL_DHE_DSS_WITH_DES_CBC_SHA", Boolean.FALSE, Boolean.FALSE ),
    SSL_DH_anon_WITH_DES_CBC_SHA( "SSL_DH_anon_WITH_DES_CBC_SHA", Boolean.FALSE, Boolean.FALSE ),
    SSL_RSA_EXPORT_WITH_RC4_40_MD5( "SSL_RSA_EXPORT_WITH_RC4_40_MD5", Boolean.FALSE, Boolean.FALSE ),
    SSL_DH_anon_EXPORT_WITH_RC4_40_MD5( "SSL_DH_anon_EXPORT_WITH_RC4_40_MD5", Boolean.FALSE, Boolean.FALSE ),
    SSL_RSA_EXPORT_WITH_DES40_CBC_SHA( "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", Boolean.FALSE, Boolean.FALSE ),
    SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA( "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", Boolean.FALSE, Boolean.FALSE ),
    SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA( "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", Boolean.FALSE, Boolean.FALSE ),
    SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA( "SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA", Boolean.FALSE, Boolean.FALSE ),
    TLS_KRB5_WITH_RC4_128_SHA( "TLS_KRB5_WITH_RC4_128_SHA", Boolean.FALSE, Boolean.FALSE ),
    TLS_KRB5_WITH_RC4_128_MD5( "TLS_KRB5_WITH_RC4_128_MD5", Boolean.FALSE, Boolean.FALSE ),
    TLS_KRB5_WITH_3DES_EDE_CBC_SHA( "TLS_KRB5_WITH_3DES_EDE_CBC_SHA", Boolean.FALSE, Boolean.FALSE ),
    TLS_KRB5_WITH_3DES_EDE_CBC_MD5( "TLS_KRB5_WITH_3DES_EDE_CBC_MD5", Boolean.FALSE, Boolean.FALSE ),
    TLS_KRB5_WITH_DES_CBC_SHA( "TLS_KRB5_WITH_DES_CBC_SHA", Boolean.FALSE, Boolean.FALSE ),
    TLS_KRB5_WITH_DES_CBC_MD5( "TLS_KRB5_WITH_DES_CBC_MD5", Boolean.FALSE, Boolean.FALSE ),
    TLS_KRB5_EXPORT_WITH_RC4_40_SHA( "TLS_KRB5_EXPORT_WITH_RC4_40_SHA", Boolean.FALSE, Boolean.FALSE ),
    TLS_KRB5_EXPORT_WITH_RC4_40_MD5( "TLS_KRB5_EXPORT_WITH_RC4_40_MD5", Boolean.FALSE, Boolean.FALSE ),
    TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA( "TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA", Boolean.FALSE, Boolean.FALSE ),
    TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5( "TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5", Boolean.FALSE, Boolean.FALSE );


    /**
     * The list of supported ciphers for JAVA 8
     */
    public static final SupportedCipher[] SUPPORTED_CIPHERS =
    {
        TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384,
        TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
        TLS_RSA_WITH_AES_256_CBC_SHA256,
        TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384,
        TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384,
        TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,
        TLS_DHE_DSS_WITH_AES_256_CBC_SHA256,
        TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
        TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
        TLS_RSA_WITH_AES_256_CBC_SHA,
        TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA,
        TLS_ECDH_RSA_WITH_AES_256_CBC_SHA,
        TLS_DHE_RSA_WITH_AES_256_CBC_SHA,
        TLS_DHE_DSS_WITH_AES_256_CBC_SHA,
        TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,
        TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,
        TLS_RSA_WITH_AES_128_CBC_SHA256,
        TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256,
        TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256,
        TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,
        TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,
        TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
        TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
        TLS_RSA_WITH_AES_128_CBC_SHA,
        TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA,
        TLS_ECDH_RSA_WITH_AES_128_CBC_SHA,
        TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
        TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
        TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
        TLS_ECDHE_RSA_WITH_RC4_128_SHA,
        SSL_RSA_WITH_RC4_128_SHA,
        TLS_ECDH_ECDSA_WITH_RC4_128_SHA,
        TLS_ECDH_RSA_WITH_RC4_128_SHA,
        TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
        TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
        TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
        TLS_RSA_WITH_AES_256_GCM_SHA384,
        TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384,
        TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384,
        TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
        TLS_DHE_DSS_WITH_AES_256_GCM_SHA384,
        TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
        TLS_RSA_WITH_AES_128_GCM_SHA256,
        TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256,
        TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256,
        TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
        TLS_DHE_DSS_WITH_AES_128_GCM_SHA256,
        TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA,
        TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA,
        SSL_RSA_WITH_3DES_EDE_CBC_SHA,
        TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA,
        TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA,
        SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA,
        SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA,
        SSL_RSA_WITH_RC4_128_MD5,
        TLS_EMPTY_RENEGOTIATION_INFO_SCSV,
        TLS_DH_anon_WITH_AES_256_GCM_SHA384,
        TLS_DH_anon_WITH_AES_128_GCM_SHA256,
        TLS_DH_anon_WITH_AES_256_CBC_SHA256,
        TLS_ECDH_anon_WITH_AES_256_CBC_SHA,
        TLS_DH_anon_WITH_AES_256_CBC_SHA,
        TLS_DH_anon_WITH_AES_128_CBC_SHA256,
        TLS_ECDH_anon_WITH_AES_128_CBC_SHA,
        TLS_DH_anon_WITH_AES_128_CBC_SHA,
        TLS_ECDH_anon_WITH_RC4_128_SHA,
        SSL_DH_anon_WITH_RC4_128_MD5,
        TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA,
        SSL_DH_anon_WITH_3DES_EDE_CBC_SHA,
        TLS_RSA_WITH_NULL_SHA256,
        TLS_ECDHE_ECDSA_WITH_NULL_SHA,
        TLS_ECDHE_RSA_WITH_NULL_SHA,
        SSL_RSA_WITH_NULL_SHA,
        TLS_ECDH_ECDSA_WITH_NULL_SHA,
        TLS_ECDH_RSA_WITH_NULL_SHA,
        TLS_ECDH_anon_WITH_NULL_SHA,
        SSL_RSA_WITH_NULL_MD5,
        SSL_RSA_WITH_DES_CBC_SHA,
        SSL_DHE_RSA_WITH_DES_CBC_SHA,
        SSL_DHE_DSS_WITH_DES_CBC_SHA,
        SSL_DH_anon_WITH_DES_CBC_SHA,
        SSL_RSA_EXPORT_WITH_RC4_40_MD5,
        SSL_DH_anon_EXPORT_WITH_RC4_40_MD5,
        SSL_RSA_EXPORT_WITH_DES40_CBC_SHA,
        SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA,
        SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA,
        SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA,
        TLS_KRB5_WITH_RC4_128_SHA,
        TLS_KRB5_WITH_RC4_128_MD5,
        TLS_KRB5_WITH_3DES_EDE_CBC_SHA,
        TLS_KRB5_WITH_3DES_EDE_CBC_MD5,
        TLS_KRB5_WITH_DES_CBC_SHA,
        TLS_KRB5_WITH_DES_CBC_MD5,
        TLS_KRB5_EXPORT_WITH_RC4_40_SHA,
        TLS_KRB5_EXPORT_WITH_RC4_40_MD5,
        TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA,
        TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5
    };


    /** The supported cipher name */
    private String cipher;

    /** A flag that tells if the cipher is supported in Java 7 */
    private Boolean java7;

    /** A flag that tells if the cipher is supported in Java 8 */
    private Boolean java8;

    /** A map containing all the values */
    private static Map<String, SupportedCipher> supportedCiphersByName = new HashMap<String, SupportedCipher>();

    /** A list of all the supported ciphers for JAVA 7 */
    public static List<SupportedCipher> supportedCiphersJava7 = new ArrayList<SupportedCipher>();

    /** A list of all the supported cipher names for JAVA 7 */
    public static List<String> supportedCipherNamesJava7 = new ArrayList<String>();

    /** A list of all the supported ciphers for JAVA 8 */
    public static List<SupportedCipher> supportedCiphersJava8 = new ArrayList<SupportedCipher>();

    /** A list of all the supported cipher names for JAVA 8 */
    public static List<String> supportedCipherNamesJava8 = new ArrayList<String>();

    /** Initialization of the previous maps and lists */
    static
    {
        for ( SupportedCipher cipher : SupportedCipher.values() )
        {
            supportedCiphersByName.put( cipher.getCipher(), cipher );

            if ( cipher.isJava7Implemented() )
            {
                supportedCiphersJava7.add( cipher );
                supportedCipherNamesJava7.add( cipher.cipher );
            }

            if ( cipher.isJava8Implemented() )
            {
                supportedCiphersJava8.add( cipher );
                supportedCipherNamesJava8.add( cipher.cipher );
            }
        }
    }


    // ── Stamping A Cipher Into The Codebook ───────────────────────────────────────────────────
    // Each entry in the codebook carries its full name and two per-Java-version flags.
    // TRUE = enabled, FALSE = disabled (weak but known), null = not implemented on that JVM.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the enum constant with its cipher suite name and per-Java-version availability flags.
     *
     * @param cipher  the cipher suite name string (e.g. {@code "TLS_RSA_WITH_AES_256_CBC_SHA256"})
     * @param java7   {@code Boolean.TRUE} if enabled, {@code Boolean.FALSE} if disabled, {@code null} if not available on Java 7
     * @param java8   {@code Boolean.TRUE} if enabled, {@code Boolean.FALSE} if disabled, {@code null} if not available on Java 8
     */
    private SupportedCipher( String cipher, Boolean java7, Boolean java8 )
    {
        this.cipher = cipher;
        this.java7 = java7;
        this.java8 = java8;
    }


    // ── Retrieving The Cipher Suite Name ──────────────────────────────────────────────────────
    // The UI needs the raw string name (e.g. "TLS_RSA_WITH_AES_256_CBC_SHA256") to populate
    // checkboxes and to write back to the configuration bean.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the cipher suite name string.
     *
     * @return the cipher name, e.g. {@code "TLS_RSA_WITH_AES_256_CBC_SHA256"}
     */
    public String getCipher()
    {
        return cipher;
    }


    // ── Checking If The Cipher Is Enabled On Java 7 ───────────────────────────────────────────
    // TRUE means the checkbox should be ticked; FALSE or null means it should not be.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this cipher is enabled (not just implemented) on Java 7.
     * Returns {@code false} if the cipher is disabled or not available on Java 7.
     *
     * @return {@code true} if enabled on Java 7; {@code false} otherwise
     */
    public Boolean isJava7Enabled()
    {
        return java7 != null && java7;
    }


    // ── Checking If The Cipher Exists At All On Java 7 ────────────────────────────────────────
    // Even a disabled cipher is "implemented" — only null means "this JVM doesn't have it".
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this cipher is implemented on Java 7 (enabled or disabled, but not absent).
     *
     * @return {@code true} if the cipher exists on Java 7 in any state; {@code false} if absent
     */
    public boolean isJava7Implemented()
    {
        return java7 != null;
    }


    // ── Checking If The Cipher Is Enabled On Java 8 ───────────────────────────────────────────
    // Same pattern as Java 7: enabled means the checkbox should start ticked.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this cipher is enabled (not just implemented) on Java 8.
     * Returns {@code false} if the cipher is disabled or not available on Java 8.
     *
     * @return {@code true} if enabled on Java 8; {@code false} otherwise
     */
    public boolean isJava8Enabled()
    {
        return java8 != null && java8;
    }


    // ── Checking If The Cipher Exists At All On Java 8 ────────────────────────────────────────
    // Same pattern as Java 7: non-null means the JVM knows about this suite.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this cipher is implemented on Java 8 (enabled or disabled, but not absent).
     *
     * @return {@code true} if the cipher exists on Java 8 in any state; {@code false} if absent
     */
    public boolean isJava8Implemented()
    {
        return java8 != null;
    }


    // ── Looking Up A Cipher By Its Name String ────────────────────────────────────────────────
    // When parsing an existing configuration, we get cipher names as strings and need to look
    // them up in the codebook.  The lookup is case-insensitive to be forgiving with configs.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@code SupportedCipher} enum constant whose name matches the given string,
     * case-insensitively. Returns {@code null} if no match is found or input is {@code null}.
     *
     * @param type  the cipher suite name to look up
     * @return the matching constant, or {@code null} if not found
     */
    public static SupportedCipher getByName( String type )
    {
        if ( type == null )
        {
            return null;
        }

        return supportedCiphersByName.get( type.toUpperCase() );
    }
}
