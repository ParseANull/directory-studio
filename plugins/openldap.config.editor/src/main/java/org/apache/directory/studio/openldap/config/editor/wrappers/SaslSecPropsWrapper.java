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

// ── CLASS: SaslSecPropsWrapper — The Rebel Alliance Authentication Committee ──
// Before any officer can join the Rebel Alliance's secure channel, they must
// satisfy a checklist of SASL security requirements: no plaintext passwords
// (noplain), no active attacks (noactive), minimum encryption strength
// (minssf=128), and so on.  SaslSecPropsWrapper wraps the olcSaslSecProps
// attribute value — a comma-separated list of flags and key=value pairs —
// parsing it into a set of boolean flags and three integer parameters.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A wrapper for the olcSaslSecProps attribute.
 * It holds a set of boolean security flags (noplain, noactive, nodict, etc.)
 * and three integer parameters (minSSF, maxSSF, maxBufSize), parsed from a
 * comma-separated property string.
 *
 * <pre>
 * saslSecProp ::= ( 'none' | 'noplain' | 'noactive' | 'nodict' | 'noanonymous' |
 *                   'forwardsec' | 'passcred' |
 *                   'minssf' '=' INT | 'maxssf' '=' INT | 'maxbufsize' '=' INT )*
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SaslSecPropsWrapper implements Cloneable
{
    /** The flags for properties with no arguments */
    private Set<SaslSecPropEnum> flags = new HashSet<>();

    /** The value of the minSSF parameter */
    private Integer minSsf;

    /** The value of the maxSSF parameter */
    private Integer maxSsf;

    /** The max buffer size parameter */
    private Integer maxBufSize;


    // ── Default Constructor — An Empty Checklist ───────────────────────────────
    // The authentication committee starts with a blank checklist — no flags
    // or numeric parameters are set.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an instance of a SaslSecProps
     **/
    public SaslSecPropsWrapper()
    {
    }


    // ── Constructor (String) — Parse the Property String ──────────────────────
    // The committee clerk reads the olcSaslSecProps string, splits it on
    // commas, and fills in the flags and numeric parameters.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an instance of a SaslSecProps parameter using a String.
     *
     * @param parameters The list of parameters to parse
     */
    public SaslSecPropsWrapper( String parameters )
    {
        if ( !Strings.isEmpty( Strings.trim( parameters ) ) )
        {
            // Split the string along the spaces
            String[] properties = parameters.split( "," );

            for ( String property : properties )
            {
                if ( Strings.isEmpty( Strings.trim( property ) ) )
                {
                    continue;
                }

                int pos = property.indexOf( '=' );

                if ( pos == -1 )
                {
                    // No value
                    SaslSecPropEnum flag = SaslSecPropEnum.getSaslSecProp( Strings.trim( property ) );

                    switch ( flag )
                    {
                        case FORWARD_SEC :
                        case NO_ACTIVE :
                        case NO_ANONYMOUS :
                        case NO_DICT :
                        case NO_PLAIN :
                        case PASS_CRED :
                        case NONE :
                            flags.add( flag );
                            break;

                        case MAX_BUF_SIZE :
                        case MAX_SSF :
                        case MIN_SSF :
                        case UNKNOWN :
                            // Nothing to do...
                    }
                }
                else
                {
                    // Fetch the name
                    String name = property.substring( 0, pos );
                    SaslSecPropEnum flag = SaslSecPropEnum.getSaslSecProp( Strings.trim( name ) );

                    try
                    {
                        int value = Integer.parseInt( Strings.trim( property.substring( pos + 1 ) ) );

                        if ( value >= 0 )
                        {
                            switch ( flag )
                            {
                                case MAX_BUF_SIZE :
                                    maxBufSize = Integer.valueOf( value );
                                    break;

                                case MAX_SSF :
                                    maxSsf = Integer.valueOf( value );
                                    break;

                                case MIN_SSF :
                                    minSsf = Integer.valueOf( value );
                                    break;

                                case FORWARD_SEC :
                                case NO_ACTIVE :
                                case NO_ANONYMOUS :
                                case NO_DICT :
                                case NO_PLAIN :
                                case PASS_CRED :
                                case NONE :
                                case UNKNOWN :
                                    // Nothing to do... This is an error
                            }
                        }
                    }
                    catch ( NumberFormatException nfe )
                    {
                        // Nothing to do
                    }
                }
            }
        }
    }


    // ── isValid (static) — Validate a Raw Property String ─────────────────────
    // The committee clerk checks whether a raw property string is valid before
    // constructing a wrapper from it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Check if a given String is a valid SaslSecProp parameter
     *
     * @param str The string to check
     * @return true if the string is a valid SaslSecProp parameter
     */
    public static boolean isValid( String str )
    {
        if ( !Strings.isEmpty( Strings.trim( str ) ) )
        {
            // Split the string along the spaces
            String[] properties = str.split( "," );

            if ( ( properties == null ) || ( properties.length == 0 ) )
            {
                return true;
            }

            for ( String property : properties )
            {
                if ( Strings.isEmpty( Strings.trim( property ) ) )
                {
                    continue;
                }

                int pos = property.indexOf( '=' );

                if ( pos == -1 )
                {
                    // No value
                    SaslSecPropEnum flag = SaslSecPropEnum.getSaslSecProp( Strings.trim( property ) );

                    switch ( flag )
                    {
                        case FORWARD_SEC :
                        case NO_ACTIVE :
                        case NO_ANONYMOUS :
                        case NO_DICT :
                        case NO_PLAIN :
                        case PASS_CRED :
                        case NONE :
                            break;

                        default :
                            return false;
                    }
                }
                else
                {
                    // Fetch the name
                    String name = property.substring( 0, pos );
                    SaslSecPropEnum flag = SaslSecPropEnum.getSaslSecProp( Strings.trim( name ) );

                    try
                    {
                        int value = Integer.parseInt( Strings.trim( property.substring( pos + 1 ) ) );

                        if ( value < 0 )
                        {
                            return false;
                        }

                        switch ( flag )
                        {
                            case MAX_BUF_SIZE :
                            case MAX_SSF :
                            case MIN_SSF :
                                break;

                            default :
                                return false;
                        }
                    }
                    catch ( NumberFormatException nfe )
                    {
                        // wrong
                        return false;
                    }
                }
            }

            return true;
        }
        else
        {
            return true;
        }
    }


    // ── getFlags — Return the Boolean Flag Set ─────────────────────────────────
    /**
     * @return the flag
     */
    public Set<SaslSecPropEnum> getFlags()
    {
        return flags;
    }


    // ── addFlag — Add a Boolean Flag ───────────────────────────────────────────
    /**
     * @param flag the flag to set
     */
    public void addFlag( SaslSecPropEnum flag )
    {
        this.flags.add( flag );
    }


    // ── removeFlag — Remove a Boolean Flag ────────────────────────────────────
    /**
     * @param flag the flag to remove
     */
    public void removeFlag( SaslSecPropEnum flag )
    {
        this.flags.remove( flag );
    }


    // ── clearFlags — Clear All Boolean Flags ──────────────────────────────────
    /**
     * Clear the flag's set
     */
    public void clearFlags()
    {
        this.flags.clear();
    }


    // ── getMinSsf — Return the Minimum SSF Requirement ────────────────────────
    /**
     * @return the minSsf
     */
    public Integer getMinSsf()
    {
        return minSsf;
    }


    // ── setMinSsf — Set the Minimum SSF Requirement ───────────────────────────
    /**
     * @param minSsf the minSsf to set
     */
    public void setMinSsf( Integer minSsf )
    {
        this.minSsf = minSsf;
    }


    // ── getMaxSsf — Return the Maximum SSF Ceiling ────────────────────────────
    /**
     * @return the maxSsf
     */
    public Integer getMaxSsf()
    {
        return maxSsf;
    }


    // ── setMaxSsf — Set the Maximum SSF Ceiling ───────────────────────────────
    /**
     * @param maxSsf the maxSsf to set
     */
    public void setMaxSsf( Integer maxSsf )
    {
        this.maxSsf = maxSsf;
    }


    // ── getMaxBufSize — Return the Maximum Buffer Size ────────────────────────
    /**
     * @return the maxBufSize
     */
    public Integer getMaxBufSize()
    {
        return maxBufSize;
    }


    // ── setMaxBufSize — Set the Maximum Buffer Size ───────────────────────────
    /**
     * @param maxBufSize the maxBufSize to set
     */
    public void setMaxBufSize( Integer maxBufSize )
    {
        this.maxBufSize = maxBufSize;
    }


    // ── equals (private helper) — Null-Safe Integer Comparison ────────────────
    /**
     * Compare two Integer instance and return true if they are equal
     */
    private boolean equals( Integer int1, Integer int2 )
    {
        if ( int1 == null )
        {
            return int2 == null;
        }

        return int1.equals( int2 );
    }


    // ── equals — Check If Two Wrapper Instances Represent the Same Properties ──
    /**
     * @see Object#equals(Object)
     */
    public boolean equals( Object that )
    {
        if ( this == that )
        {
            return true;
        }

        if ( !( that instanceof SaslSecPropsWrapper ) )
        {
            return false;
        }

        SaslSecPropsWrapper thatInstance = (SaslSecPropsWrapper)that;

        return ( ( flags.size() == thatInstance.flags.size() ) &&
                 ( thatInstance.flags.containsAll( flags ) ) &&
                 ( equals( minSsf, thatInstance.minSsf ) ) &&
                 ( equals( maxSsf, thatInstance.maxSsf ) ) &&
                 ( equals( maxBufSize, thatInstance.maxBufSize ) ) );
    }


    // ── hashCode — Hash Based on All Fields ───────────────────────────────────
    /**
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        int h = 37;

        if ( minSsf != null )
        {
            h += h*17 + minSsf.intValue();
        }

        if ( maxSsf != null )
        {
            h += h*17 + maxSsf.intValue();
        }

        if ( maxBufSize != null )
        {
            h += h*17 + maxBufSize.intValue();
        }

        for ( SaslSecPropEnum saslSecProp : flags )
        {
            h += h*17 + saslSecProp.hashCode();
        }

        return h;
    }


    // ── clone — Duplicate the Checklist ───────────────────────────────────────
    /**
     * Clone the current object
     */
    public SaslSecPropsWrapper clone()
    {
        try
        {
            return (SaslSecPropsWrapper)super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            return null;
        }
    }


    // ── toString — Serialize to the olcSaslSecProps Format ────────────────────
    // The clerk writes out the checklist in a comma-separated format.  Boolean
    // flags come first (in their natural order), followed by numeric parameters.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        boolean isFirst = true;

        for ( SaslSecPropEnum saslSecProp : flags )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                sb.append( ',' );
            }

            sb.append( saslSecProp.getName() );
        }

        // The minSSF properties
        if ( minSsf != null )
        {
            if ( sb.length() > 0 )
            {
                sb.append( ',' );
            }

            sb.append( SaslSecPropEnum.MIN_SSF.getName() ).append( '=' ).append( minSsf.intValue() );
        }


        // The maxSSF properties
        if ( maxSsf != null )
        {
            if ( sb.length() > 0 )
            {
                sb.append( ',' );
            }

            sb.append( SaslSecPropEnum.MAX_SSF.getName() ).append( '=' ).append( maxSsf.intValue() );
        }

        // The maxbufsize properties
        if ( maxBufSize != null )
        {
            if ( sb.length() > 0 )
            {
                sb.append( ',' );
            }

            sb.append( SaslSecPropEnum.MAX_BUF_SIZE.getName() ).append( '=' ).append( maxBufSize.intValue() );
        }

        return sb.toString();
    }
}
