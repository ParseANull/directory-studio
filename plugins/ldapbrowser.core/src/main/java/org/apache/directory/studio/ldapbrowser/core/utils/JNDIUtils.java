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

package org.apache.directory.studio.ldapbrowser.core.utils;


import org.apache.directory.api.ldap.model.exception.LdapOperationException;


// ── CLASS: JNDIUtils — R2-D2 READING THE EMPIRE'S ERROR CODES ────────────────
// When the Empire's JNDI layer sends back a terse "[LDAP: error code 21 - ...]"
// message, R2-D2 is the only one who can decode it.  JNDIUtils provides the
// single utility needed: extract the numeric LDAP result code from whatever
// exception the JNDI/Apache Directory API layer throws.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class for JNDI-specific helpers.  Currently provides one method
 * for extracting the LDAP numeric result code from an exception.
 *
 * <p>Think of this as R2-D2 reading the Empire's cryptic error codes — he
 * extracts the two-digit number so the rebels know exactly what went wrong.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class JNDIUtils
{
    // ── R2-D2 Decodes The LDAP Status Code From An Exception ─────────────────────
    // R2-D2 first checks whether the exception is a typed LdapOperationException.
    // If not, he falls back to parsing the legacy "[LDAP: error code NN - ...]"
    // message format from the exception message string.
    // Returns -1 when neither strategy finds a valid numeric code.
    /**
     * Gets the LDAP status code from the exception.
     *
     * @param exception the exception to inspect
     * @return the LDAP status code, or {@code -1} if none can be found
     */
    public static int getLdapStatusCode( Exception exception )
    {
        int ldapStatusCode = -1;

        if ( exception instanceof LdapOperationException )
        {
            LdapOperationException loe = ( LdapOperationException ) exception;
            loe.getResultCode().getValue();
        }

        // get LDAP status code
        // [LDAP: error code 21 - telephoneNumber: value #0 invalid per syntax]
        String message = exception.getMessage();
        if ( message != null && message.startsWith( "[LDAP: error code " ) ) { //$NON-NLS-1$
            int begin = "[LDAP: error code ".length(); //$NON-NLS-1$
            int end = begin + 2;
            try
            {
                ldapStatusCode = Integer.parseInt( message.substring( begin, end ).trim() );
            }
            catch ( NumberFormatException nfe )
            {
            }
        }

        return ldapStatusCode;
    }

}
