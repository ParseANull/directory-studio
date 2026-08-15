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


import javax.naming.InvalidNameException;

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Ava;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;


// ── CLASS: DnUtils — R2-D2 TRIMS AND REASSEMBLES NAVIGATION COORDINATES ──────
// When R2 calculates a jump route, he sometimes needs to strip the destination
// suffix off a set of coordinates to find the relative prefix — and sometimes
// needs to assemble a compound waypoint from multiple component values.
// DNs (Distinguished Names) in LDAP are hierarchical addresses: stripping a
// suffix gives the sub-tree prefix; assembling RDN components builds an entry's
// final location key.
// This utility class gives us those two operations cleanly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility methods for working with LDAP Distinguished Names (DNs) and
 * Relative Distinguished Names (RDNs).
 * A DN is the full address of an LDAP entry — for example:
 * {@code cn=Han Solo,ou=Pilots,dc=rebellion,dc=org}.
 * An RDN is the rightmost component that uniquely identifies an entry within
 * its parent container — for example: {@code cn=Han Solo}.
 * We put these helpers here so DN manipulation logic isn't scattered across
 * multiple callers.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DnUtils
{
    // ── GET PREFIX NAME — R2 STRIPS THE DESTINATION SUFFIX FROM THE COORDINATES ─────
    // If the full jump coordinates are "Tatooine > Outer Rim > Galaxy" and we
    // want just the prefix relative to "Galaxy", R2 strips "Galaxy" and gives
    // us "Tatooine > Outer Rim."
    // We strip the given suffix DN from the full DN and return the relative prefix.
    // Returns null if the suffix is empty or if the DN doesn't end with the suffix.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the DN prefix after stripping the given suffix.
     * For example, given {@code cn=admin,ou=users,dc=example,dc=com} and suffix
     * {@code dc=example,dc=com}, we return {@code cn=admin,ou=users}.
     * Returns {@code null} if the suffix is empty, or if the DN doesn't end
     * with the given suffix (in which case there's no valid prefix to return).
     *
     * @param dn      The full DN to strip from.
     * @param suffix  The suffix to remove.
     * @return  The prefix DN, or {@code null} if not applicable.
     */
    public static Dn getPrefixName( Dn dn, Dn suffix )
    {
        if ( suffix.size() < 1 )
        {
            return null;
        }
        else
        {
            try
            {
                Dn prefix = dn.getDescendantOf( suffix );

                return prefix;
            }
            catch ( LdapInvalidDnException lide )
            {
                return null;
            }
        }
    }


    // ── COMPOSE RDN — R2 ASSEMBLES A COMPOUND WAYPOINT FROM COMPONENTS ────────────
    // Some LDAP entries are identified by multiple attributes at once — for example,
    // {@code cn=Han+sn=Solo} is a multi-valued RDN.  R2 takes the array of type
    // names and value strings and assembles them into a single Rdn object.
    // Throws InvalidNameException if any component is invalid.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Assembles an {@link Rdn} from parallel arrays of attribute types and values.
     * We use this when building multi-valued RDNs — for example, a naming form that
     * requires both {@code cn} and {@code sn} together.
     * The two arrays must be the same length; element {@code i} in {@code rdnTypes}
     * pairs with element {@code i} in {@code rdnValues}.
     *
     * @param rdnTypes   Array of attribute type names (e.g. {@code "cn"}, {@code "sn"}).
     * @param rdnValues  Array of attribute values corresponding to each type.
     * @return  The assembled {@link Rdn}.
     * @throws InvalidNameException  If any type+value pair is invalid.
     */
    public static Rdn composeRdn( String[] rdnTypes, String[] rdnValues ) throws InvalidNameException
    {
        try
        {
            Ava[] avas = new Ava[rdnTypes.length];
            for ( int i = 0; i < rdnTypes.length; i++ )
            {
                avas[i] = new Ava( rdnTypes[i], rdnValues[i] );
            }
            Rdn rdn = new Rdn( avas );
            return rdn;
        }
        catch ( LdapInvalidDnException e1 )
        {
            throw new InvalidNameException( Messages.error__invalid_rdn );
        }
    }

}
