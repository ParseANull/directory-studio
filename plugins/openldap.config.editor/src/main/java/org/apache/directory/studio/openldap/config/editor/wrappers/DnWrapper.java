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
package org.apache.directory.studio.openldap.config.editor.wrappers;

// ── CLASS: DnWrapper — R2-D2's Navigation Coordinate ─────────────────────────
// Every astromech stores navigation coordinates — an immutable, precise
// description of a destination in the galaxy.  DnWrapper stores an LDAP Dn
// (Distinguished Name) the same way: the Dn itself is immutable, so clone
// simply returns this.  Comparison uses LDAP tree semantics — descendants come
// before ancestors, then RDNs are compared component by component.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A wrapper for an LDAP {@link Dn} used in configuration table widgets.
 * Comparison uses LDAP tree ordering: a descendant sorts after its ancestor,
 * and otherwise RDNs are compared lexicographically from the root.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DnWrapper implements Cloneable, Comparable<DnWrapper>
{
    /** The interned DN */
    private Dn dn;


    // ── Constructor — Loading a Destination Coordinate ─────────────────────────
    // R2-D2 receives a Dn coordinate and stores it in his navigation buffer.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Build a DnWrapper from a String containing the DN
     *
     * @param dn The DN to store
     */
    public DnWrapper( Dn dn )
    {
        this.dn = dn;
    }


    // ── getDn — Read the Stored Coordinate ────────────────────────────────────
    // R2-D2 reports back the stored navigation coordinate.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @return the dn
     */
    public Dn getDn()
    {
        return dn;
    }


    // ── setDn — Update the Navigation Coordinate ──────────────────────────────
    // R2-D2 overwrites his coordinate buffer with a new Dn.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @param dn the dn to set
     */
    public void setDn( Dn dn )
    {
        this.dn = dn;
    }


    // ── clone — The Coordinate Is Already Immutable ───────────────────────────
    // Dn is immutable — no deep copy is needed; we return this.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#clone()
     */
    public DnWrapper clone()
    {
        // No need to clone, DN is immutable
        return this;
    }


    // ── hashCode — Compute a Hash from the DN ─────────────────────────────────
    // We delegate to the Dn's own hashCode for consistency with equals.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        return dn.hashCode();
    }


    // ── equals — Check If Two Wrappers Point to the Same Coordinate ───────────
    // Two DnWrapper instances are equal if their DNs compare as equal.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#equals()
     */
    public boolean equals( Object that )
    {
        if ( that == this )
        {
            return true;
        }

        if ( ! ( that instanceof DnWrapper ) )
        {
            return false;
        }

        return compareTo( (DnWrapper)that ) == 0;
    }


    // ── compareTo — Compare Coordinates by LDAP Tree Ordering ─────────────────
    // Descendants sort after ancestors; when neither is a descendant of the
    // other we compare RDN by RDN from the root until one differs.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Comparable#compareTo()
     */
    public int compareTo( DnWrapper that )
    {
        if ( that == null )
        {
            return 1;
        }

        if ( dn.equals( that.dn ) )
        {
            return 0;
        }

        if ( dn.isDescendantOf( that.dn ) )
        {
            return 1;
        }
        else if ( that.dn.isDescendantOf( dn ) )
        {
            return -1;
        }
        else
        {
            // Find the common ancestor, if any
            int upperBound = Math.min( dn.size(), that.dn.size() );
            int result = 0;

            for ( int i = 0; i < upperBound; i++ )
            {
                result = dn.getRdn( i ).compareTo( that.dn.getRdn( i ) );

                if ( result != 0 )
                {
                    return result;
                }
            }
            // We have exhausted one of the DN
            if ( dn.size() > upperBound )
            {
                return 1;
            }
            else
            {
                return -1;
            }
        }
    }


    // ── toString — Print the Coordinate as a String ───────────────────────────
    // R2-D2 outputs the Dn in its standard string form.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return dn.toString();
    }
}
