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

// ── CLASS: SsfWrapper — The Death Star's Shield Strength Factor ───────────────
// The Death Star's shield generator has multiple security layers — TLS, SASL,
// transport — each tuned to a minimum bit strength.  SsfWrapper wraps one
// olcSecurity value: a named security feature and its required bit count.
// The constructor handles both the "feature=N" text form and the programmatic
// (enum, int) form.  Comparison is by feature name then bit count.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A wrapper for a single value in the olcSecurity attribute.
 * It stores a {@link SsfFeatureEnum} (the security feature) and a bit count
 * (the minimum Security Strength Factor), and serializes to "featureName=N".
 *
 * <pre>
 * olcSecurity ::= (feature=size)*
 * feature     ::= 'ssf' | 'transport' | 'tls' | 'sasl' | 'simple_bind' |
 *                 'update_ssf' | 'update_transport' | 'update_tls' | 'update_sasl'
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SsfWrapper implements Cloneable, Comparable<SsfWrapper>
{
    /** The feature */
    private SsfFeatureEnum feature;

    /** The number of bits for this feature */
    private int nbBits = 0;


    // ── Constructor (String feature, int nbBits) — Direct Construction ─────────
    // The shield officer sets a specific feature name and bit count directly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an instance using a name of a feature and its size
     *
     * @param feature The feature to use
     * @param nbBits The number of bits
     */
    public SsfWrapper( String feature, int nbBits )
    {
        this.feature = SsfFeatureEnum.getSsfFeature( feature );

        if ( this.feature == SsfFeatureEnum.NONE )
        {
            this.nbBits = 0;
        }
        else
        {
            this.nbBits = nbBits;
        }

        if ( nbBits < 0 )
        {
            this.nbBits = 0;
        }

    }


    // ── Constructor (String) — Parse a "feature=N" Config String ──────────────
    // The shield officer reads a security config string (e.g. "tls=128") and
    // splits it into the feature enum and the bit count.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an instance using a String representation, which format is
     * feature = &lt;nbBits&gt;
     *
     * @param feature The feature to use
     */
    public SsfWrapper( String feature )
    {
        if ( Strings.isEmpty( feature ) )
        {
            this.feature = SsfFeatureEnum.NONE;
            this.nbBits = 0;
        }
        else
        {
            int pos = feature.indexOf( '=' );

            if ( pos < 0 )
            {
                this.feature = SsfFeatureEnum.NONE;
                this.nbBits = 0;
            }

            String name = Strings.trim( feature.substring( 0, pos ) );
            this.feature = SsfFeatureEnum.getSsfFeature( name );

            if ( this.feature == SsfFeatureEnum.NONE )
            {
                this.nbBits = 0;
            }
            else
            {
                String value = Strings.trim( feature.substring( pos + 1 ) );

                try
                {
                    this.nbBits = Integer.parseInt( value );
                }
                catch ( NumberFormatException nfe )
                {
                    this.nbBits = 0;
                }
            }
        }
    }


    // ── isValid (instance) — Check If the SSF Entry Is Usable ─────────────────
    // A shield entry is valid when the feature is recognized and the bit count
    // is non-negative.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Tells if this is a valid SSF. The format is :
     * feature = &lt;nbBits&gt; where nbBits >= 0.
     *
     * @return true if valid
     */
    public boolean isValid()
    {
        return ( feature != null ) && ( feature != SsfFeatureEnum.NONE ) && ( nbBits >= 0 );
    }


    // ── isValid (static) — Validate a Raw Config String ───────────────────────
    // The shield officer validates a config string without constructing a
    // wrapper object.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Tells if the String is a valid SSF. The format is :
     * feature = &lt;nbBits&gt;
     *
     * @param ssf The feature to check
     * @return true if valid
     */
    public static boolean isValid( String ssf )
    {
        if ( Strings.isEmpty( ssf ) )
        {
            return false;
        }
        else
        {
            int pos = ssf.indexOf( '=' );

            if ( pos < 0 )
            {
                return false;
            }

            String name = Strings.trim( ssf.substring( 0, pos ) );
            SsfFeatureEnum ssfFeature = SsfFeatureEnum.getSsfFeature( name );

            if ( ssfFeature == SsfFeatureEnum.NONE )
            {
                return false;
            }
            else
            {
                String value = Strings.trim( ssf.substring( pos + 1 ) );

                try
                {
                    return Integer.parseInt( value ) >= 0 ;
                }
                catch ( NumberFormatException nfe )
                {
                    return false;
                }
            }
        }
    }


    // ── getFeature — Return the Security Feature ───────────────────────────────
    /**
     * @return the feature
     */
    public SsfFeatureEnum getFeature()
    {
        return feature;
    }


    // ── setFeature — Update the Security Feature ───────────────────────────────
    /**
     * @param feature the feature to set
     */
    public void setFeature( SsfFeatureEnum feature )
    {
        this.feature = feature;
    }


    // ── getNbBits — Return the Bit Count ──────────────────────────────────────
    /**
     * @return the nbBits
     */
    public int getNbBits()
    {
        return nbBits;
    }


    // ── setNbBits — Update the Bit Count ──────────────────────────────────────
    /**
     * @param nbBits the nbBits to set
     */
    public void setNbBits( int nbBits )
    {
        this.nbBits = nbBits;
    }


    // ── equals — Check If Two SSF Entries Describe the Same Feature ───────────
    // Equality is based on the feature enum only — bit counts are not compared.
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

        if ( ! ( that instanceof SsfWrapper ) )
        {
            return false;
        }

        // We don't use the nbBits
        return ( feature == ((SsfWrapper)that).getFeature() );
    }


    // ── hashCode — Compute a Hash from the Feature Only ───────────────────────
    /**
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        int h = 37;

        // We don't use the nbBits
        h += h*17 + feature.hashCode();

        return h;
    }


    // ── clone — Duplicate the Shield Entry ────────────────────────────────────
    /**
     * @see Object#clone()
     */
    public SsfWrapper clone()
    {
        try
        {
            return (SsfWrapper)super.clone();
        }
        catch ( CloneNotSupportedException cnse )
        {
            return null;
        }
    }


    // ── compareTo — Sort by Feature Name Then Bit Count ───────────────────────
    /**
     * @see Comparable#compareTo()
     */
    public int compareTo( SsfWrapper that )
    {
        // Compare by feature first then by nbBits
        if ( that == null )
        {
            return 1;
        }

        int comp = feature.getName().compareTo( that.feature.getName() );

        if ( comp == 0 )
        {
            return nbBits - that.nbBits;
        }
        else
        {
            return comp;
        }
    }


    // ── toString — Serialize to "featureName=N" ────────────────────────────────
    /**
     * @see Object#toString()
     */
    public String toString()
    {
        if ( feature != SsfFeatureEnum.NONE )
        {
            return feature.getName() + '=' + nbBits;
        }
        else
        {
            return "";
        }
    }
}
