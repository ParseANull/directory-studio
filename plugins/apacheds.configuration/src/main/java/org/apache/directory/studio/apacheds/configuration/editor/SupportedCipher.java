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


// ── CLASS: SupportedCipher — HAN SOLO'S HYPERSPACE ROUTE MANIFEST ────────────────────────
// Han Solo keeps a manifest of every hyperspace route in the galaxy: some are clear and
// open (enabled), some are blockaded or too dangerous (disabled), and some only appeared
// on the newer nav charts that came with the Falcon's upgraded navigation computer (Java 8).
// If a route isn't even listed in your chart edition (null), it simply doesn't exist for
// your version of the nav computer — you can't fly it even if you wanted to.
// This enum is that manifest: every TLS/SSL cipher suite the Death Star's transport layer
// can offer, tagged with whether it's open or closed on Java 7 and Java 8 nav charts.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Enumerates every TLS/SSL cipher suite that ApacheDS can advertise, together with
 * its per-Java-version support status.
 * We store a three-valued {@link Boolean} for each Java generation:
 * <ul>
 *   <li>{@code Boolean.TRUE}  — route is open (cipher enabled) on this JVM generation.</li>
 *   <li>{@code Boolean.FALSE} — route is blocked (cipher disabled) on this JVM generation.</li>
 *   <li>{@code null}          — route is not charted (cipher unavailable) on this JVM generation.</li>
 * </ul>
 * The static lists {@link #supportedCiphersJava7} and {@link #supportedCiphersJava8} are
 * populated at class-load time from the enum constants.
 * Think of this as Han Solo's hyperspace route manifest: some routes are open, some are
 * blockaded, and some only appear in the updated nav charts.
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

    // ── Han Plots Each Route Into The Nav Computer ───────────────────────────────────────────
    // Han keys in the route name (cipher string) and flags it: open on the old charts
    // (java7), open on the new charts (java8).  The Falcon's computer stores all three values.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Associates a cipher name and Java-version status flags with this enum constant.
     *
     * @param cipher  the TLS/SSL cipher suite name string (e.g., {@code "TLS_RSA_WITH_AES_256_CBC_SHA"})
     * @param java7   {@code Boolean.TRUE} if open on Java 7, {@code Boolean.FALSE} if blocked,
     *                {@code null} if not charted in the Java 7 nav database
     * @param java8   same semantics for Java 8
     */
    private SupportedCipher( String cipher, Boolean java7, Boolean java8 )
    {
        this.cipher = cipher;
        this.java7 = java7;
        this.java8 = java8;
    }


    // ── Reading Back The Route Name ───────────────────────────────────────────────────────────
    // Han reads the route identifier off the nav console so the editor can display it.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the TLS/SSL cipher suite name string for this enum constant.
     *
     * @return the cipher name (e.g., {@code "TLS_RSA_WITH_AES_256_CBC_SHA"})
     */
    public String getCipher()
    {
        return cipher;
    }


    // ── Checking Whether The Java 7 Route Is Open ─────────────────────────────────────────────
    // On the old nav charts (Java 7), is this route actually clear to fly?
    // Blocked and uncharted routes both return false — only TRUE means "go ahead, Chewie."
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this cipher is enabled on the Java 7 nav chart.
     * Returns {@code false} if it is disabled or if it is not listed in the Java 7 chart ({@code null}).
     *
     * @return {@code true} only when the Java 7 flag is {@code Boolean.TRUE}
     */
    public Boolean isJava7Enabled()
    {
        return java7 != null && java7;
    }


    // ── Checking Whether The Java 7 Chart Knows About This Route ─────────────────────────────
    // Does the Java 7 nav chart even list this hyperspace lane?
    // Null means the route is completely unknown to Java 7 — it wasn't in the old charts.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this cipher appears in the Java 7 nav chart at all
     * (enabled or disabled), and {@code false} if it is entirely absent ({@code null}).
     *
     * @return {@code true} if the Java 7 flag is non-null
     */
    public boolean isJava7Implemented()
    {
        return java7 != null;
    }


    // ── Checking Whether The Java 8 Route Is Open ─────────────────────────────────────────────
    // Same question for the updated Java 8 charts — is this route currently clear?
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this cipher is enabled on the Java 8 nav chart.
     * Returns {@code false} if it is disabled or not listed ({@code null}).
     *
     * @return {@code true} only when the Java 8 flag is {@code Boolean.TRUE}
     */
    public boolean isJava8Enabled()
    {
        return java8 != null && java8;
    }


    // ── Checking Whether The Java 8 Chart Knows About This Route ─────────────────────────────
    // Does the new Java 8 nav update include this hyperspace lane in its database?
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this cipher appears in the Java 8 nav chart at all
     * (enabled or disabled), and {@code false} if it is entirely absent ({@code null}).
     *
     * @return {@code true} if the Java 8 flag is non-null
     */
    public boolean isJava8Implemented()
    {
        return java8 != null;
    }


    // ── Looking Up A Route By Name In The Full Manifest ──────────────────────────────────────
    // Han wants to check a specific route by its name: "Is the TLS_RSA_WITH_AES_256 lane open?"
    // We look it up in the name-indexed map so the editor can resolve a string to an enum constant.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@code SupportedCipher} enum constant matching the given cipher suite name,
     * or {@code null} if the name is {@code null} or not found in the manifest.
     *
     * <p>For example — Han checks whether a specific lane is in the manifest:</p>
     * <pre>
     *   SupportedCipher route = SupportedCipher.getByName("TLS_RSA_WITH_AES_256_CBC_SHA");
     *   // returns the matching enum constant, or null if not found
     * </pre>
     *
     * @param type  the cipher suite name to look up (case-insensitive via upper-casing)
     * @return the matching enum constant, or {@code null} if not found
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
