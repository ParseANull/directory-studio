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

// ── CLASS: TcpBufferWrapper — The Star Destroyer's Comm Relay Buffer Setting ──
// Every communication relay on a Star Destroyer has a send buffer, a receive
// buffer, and a listener address.  TcpBufferWrapper stores one such entry from
// olcTCPBuffer: an optional listener URL, an optional direction flag
// (read/write/both), and a buffer size up to 2^32-1 bytes.  It parses the
// "listener=URL read|write=size" format and serializes it back on toString.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A wrapper for the olcTCPBuffer attribute value.
 * It holds an optional listener URI, an optional direction (read, write, or
 * both), and a long buffer size (0 to 2^32-1).
 *
 * <pre>
 * TCPBuffer ::= [listener=URL] [{read|write}=]size
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TcpBufferWrapper implements Cloneable, Comparable<TcpBufferWrapper>
{
    /** The maximum buffer size (2^32-1) */
    public static final long MAX_TCP_BUFFER_SIZE = 0xFFFFFFFFL;

    /** The two kind of TCP buffer we can configure */
    public enum TcpTypeEnum
    {
        READ( "read" ),
        WRITE( "write" ),
        BOTH( "" );

        private String value;

        private TcpTypeEnum( String value )
        {
            this.value = value;
        }

        private String getValue()
        {
            return value;
        }
    }

    /** The TCP listener (optional) */
    private URI listener;

    /** The type of TCP buffer (either read or write, or both ) (optional) */
    private TcpTypeEnum tcpType;

    /** The TCP Buffer size (between 0 and 2^32-1) */
    private long size;


    // ── Constructor (long, TcpTypeEnum, String) — Direct Construction ──────────
    // The comm officer configures a buffer directly with a size, a direction,
    // and an optional listener URL.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a TcpBufferWrapper instance
     *
     * @param size The TcpBuffer size
     * @param tcpType read or write, but can be null for both
     * @param url The listener
     */
    public TcpBufferWrapper( long size, TcpTypeEnum tcpType, String url )
    {
        this.size = size;
        this.tcpType = tcpType;

        if ( !Strings.isEmpty( url ) )
        {
            try
            {
                listener = new URI( url );
            }
            catch ( URISyntaxException e )
            {
                e.printStackTrace();
            }
        }
    }


    // ── Constructor (String) — Parse the TCP Buffer Config String ──────────────
    // The comm officer reads the olcTCPBuffer string and extracts the listener,
    // direction, and size components.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a TcpBufferWrapper instance from a String
     *
     * @param tcpBufferStr The String that contain the value
     */
    public TcpBufferWrapper( String tcpBufferStr )
    {
        if ( tcpBufferStr != null )
        {
            // use a lowercase version of the string
            String lowerCaseTcpBuffer = tcpBufferStr.toLowerCase();
            int pos = 0;

            if ( lowerCaseTcpBuffer.startsWith( "listener=" ) )
            {
                // Fine, we have an URL, it's before the first space
                int spacePos = lowerCaseTcpBuffer.indexOf( ' ' );

                if ( spacePos == -1 )
                {
                    // This is wrong...
                }
                else
                {
                    String urlStr = tcpBufferStr.substring( 9, spacePos );

                    try
                    {
                        this.setListener( new URI( urlStr ) );
                    }
                    catch ( URISyntaxException e )
                    {
                        e.printStackTrace();
                    }

                    // Get rid of the following spaces
                    pos = spacePos;

                    while ( pos < lowerCaseTcpBuffer.length() )
                    {
                        if ( lowerCaseTcpBuffer.charAt( pos ) != ' ' )
                        {
                            break;
                        }

                        pos++;
                    }
                }
            }

            // We might have a 'read' or 'write' prefix
            if ( lowerCaseTcpBuffer.startsWith( "read=", pos ) )
            {
                tcpType = TcpTypeEnum.READ;
                pos += 5;
            }
            else if ( lowerCaseTcpBuffer.startsWith( "write=", pos ) )
            {
                tcpType = TcpTypeEnum.WRITE;
                pos += 6;
            }

            // get the integer
            String sizeStr = lowerCaseTcpBuffer.substring( pos );

            if ( !Strings.isEmpty( sizeStr ) )
            {
                size = Long.valueOf( sizeStr );

                if ( ( size < 0L ) || ( size > MAX_TCP_BUFFER_SIZE ) )
                {
                    // This is wrong
                }
            }
        }
    }


    // ── getListener — Return the Listener URI ─────────────────────────────────
    /**
     * @return the listener
     */
    public URI getListener()
    {
        return listener;
    }


    // ── setListener — Update the Listener URI ─────────────────────────────────
    /**
     * @param listener the listener to set
     */
    public void setListener( URI listener )
    {
        this.listener = listener;
    }


    // ── getTcpType — Return the Buffer Direction ───────────────────────────────
    /**
     * @return the tcpType
     */
    public TcpTypeEnum getTcpType()
    {
        return tcpType;
    }


    // ── setTcpType — Update the Buffer Direction ───────────────────────────────
    /**
     * @param tcpType the tcpType to set
     */
    public void setTcpType( TcpTypeEnum tcpType )
    {
        this.tcpType = tcpType;
    }


    // ── getSize — Return the Buffer Size ──────────────────────────────────────
    /**
     * @return the size
     */
    public long getSize()
    {
        return size;
    }


    // ── setSize — Update the Buffer Size ──────────────────────────────────────
    /**
     * @param size the size to set
     */
    public void setSize( long size )
    {
        this.size = size;
    }


    // ── isValid (static) — Validate Size and URL Without Constructing ──────────
    // The comm officer validates the size and URL string without building a
    // wrapper object.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Tells if the TcpBuffer element is valid or not
     * @param sizeStr the TCP buffer size
     * @param urlStr The listener as a String
     * @return true if the values are correct, false otherwise
     */
    public static boolean isValid( String sizeStr, String urlStr )
    {
        // the size must be positive and below 2^32-1
        if ( ( sizeStr != null ) && ( sizeStr.length() > 0 ) )
        {
            try
            {
                long size = Long.parseLong( sizeStr );

                if ( ( size < 0L ) || ( size > MAX_TCP_BUFFER_SIZE ) )
                {
                    return false;
                }
            }
            catch ( NumberFormatException nfe )
            {
                return false;
            }
        }

        // Check the URL
        if ( ( urlStr != null ) && ( urlStr.length() > 0 ) )
        {
            try
            {
                new URL( urlStr );
            }
            catch ( MalformedURLException mue )
            {
                return false;
            }
        }

        return true;
    }


    // ── clone — Duplicate the Buffer Configuration ────────────────────────────
    /**
     * Clone the current object
     */
    public TcpBufferWrapper clone()
    {
        try
        {
            return (TcpBufferWrapper)super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            return null;
        }
    }


    // ── equals — Check If Two Buffer Configurations Match ─────────────────────
    /**
     * @see Object#equals(Object)
     */
    public boolean equals( Object that )
    {
        // Quick test
        if ( this == that )
        {
            return true;
        }

        if ( that instanceof TcpBufferWrapper )
        {
            TcpBufferWrapper thatInstance = (TcpBufferWrapper)that;

            if ( size != thatInstance.size )
            {
                return false;
            }

            if ( tcpType != thatInstance.tcpType )
            {
                return false;
            }

            if ( listener != null )
            {
                return listener.equals( thatInstance.listener );
            }
            else
            {
                return thatInstance.listener == null;
            }
        }
        else
        {
            return false;
        }
    }


    // ── hashCode — Hash from Size, Type, and Listener ─────────────────────────
    /**
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        int h = 37;

        h += h*17 + Long.hashCode( size );
        h += h*17 + tcpType.hashCode();
        h += h*17 + listener.hashCode();

        return h;
    }


    // ── compareTo — Sort by Size Then Listener URL ────────────────────────────
    /**
     * @see Comparable#compareTo()
     */
    public int compareTo( TcpBufferWrapper that )
    {
        // Compare by size first then by URL
        if ( that == null )
        {
            return 1;
        }

        if ( size > that.size )
        {
            return 1;
        }
        else if ( size < that.size )
        {
            return -1;
        }

        // The URL, as a String
        if ( listener == null )
        {
            if ( that.listener == null )
            {
                return 0;
            }
            else
            {
                return -1;
            }
        }
        else
        {
           if ( that.listener == null )
           {
               return 1;
           }
           else
           {
               String thisListener = listener.toString();
               String thatListener = that.listener.toString();

               return thisListener.compareToIgnoreCase( thatListener );
           }
        }
    }


    // ── toString — Serialize to the olcTCPBuffer Format ───────────────────────
    // The comm officer writes out: "listener=URL read|write=size" or just
    // "size" if no listener or direction is set.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        if ( listener != null )
        {
            sb.append( "listener=" ).append( listener ).append( " ");
        }

        if ( ( tcpType != null ) && ( tcpType != TcpTypeEnum.BOTH ) )
        {
            sb.append( tcpType.getValue() ).append( "=" );
        }

        sb.append( size );

        return sb.toString();
    }
}
