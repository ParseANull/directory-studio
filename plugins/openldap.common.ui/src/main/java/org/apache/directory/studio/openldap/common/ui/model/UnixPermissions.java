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
package org.apache.directory.studio.openldap.common.ui.model;


import java.text.ParseException;
import java.util.regex.Pattern;

// ── CLASS: UnixPermissions — IMPERIAL ACCESS PASS SYSTEM ─────────────────────
// Think of this class as the Imperial checkpoint's access pass book. Every
// pass has three tiers: the owner (the officer who issued it), the group (their
// unit), and others (everyone else in the galaxy). Each tier can grant three
// rights: read the manifest (r), write orders on it (w), or execute a command
// (x). We accept the pass encoded as a four-digit octal string like "0640", as
// a plain decimal integer like "416", or as a symbolic ten-character notation
// like "-rw-r-----". The nine boolean fields record exactly which rights are
// granted.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We model a full set of Unix file-system permissions, covering read, write,
 * and execute rights for owner, group, and others. We can parse an octal
 * string (e.g., {@code "0640"}), a decimal integer string (e.g., {@code "416"}),
 * or a symbolic notation string (e.g., {@code "-rw-r-----"}) into our internal
 * boolean flags. We can also produce all three representations on demand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class UnixPermissions
{
    /** The pattern used to match a symbolic value (e.g. "-rw-------") */
    private static final Pattern SYMBOLIC_FORMAT_PATTERN = Pattern.compile(
        "^-(-|r)(-|w)(-|x)(-|r)(-|w)(-|x)(-|r)(-|w)(-|x)$", Pattern.CASE_INSENSITIVE );

    private boolean ownerRead;
    private boolean ownerWrite;
    private boolean ownerExecute;
    private boolean groupRead;
    private boolean groupWrite;
    private boolean groupExecute;
    private boolean othersRead;
    private boolean othersWrite;
    private boolean othersExecute;

    // ── CONSTRUCTOR: UnixPermissions() — BLANK ACCESS PASS ───────────────────
    // We create an empty pass with all nine permission flags set to false — no
    // rights granted to anyone yet, just like a freshly printed but unstamped
    // Imperial credential.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create a new {@link UnixPermissions} instance with all permission
     * flags initialized to {@code false}.
     */
    public UnixPermissions()
    {
    }


    // ── CONSTRUCTOR: UnixPermissions(String) — SCANNING AN ACCESS PASS ───────
    // We read the permission string and decode it into our nine boolean flags.
    // The string may be octal ("0640"), decimal ("416"), or symbolic
    // ("-rw-r-----"). If none of these formats match, the checkpoint throws a
    // ParseException — an unreadable pass gets no entry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create a new {@link UnixPermissions} instance by parsing the supplied
     * permission string. We accept octal (e.g., {@code "0640"}), decimal
     * (e.g., {@code "416"}), or symbolic (e.g., {@code "-rw-r-----"}) format.
     *
     * @param s                the permission string to parse
     * @throws ParseException  if the format is not recognized
     */
    public UnixPermissions( String s ) throws ParseException
    {
        if ( ( s != null ) && ( !s.isEmpty() ) )
        {
            // First let's trim the value
            String trimmed = s.trim();
            int integerValue = -1;

            try
            {
                integerValue = Integer.parseInt( trimmed );
            }
            catch ( NumberFormatException e )
            {
                // Silent, integerValue will be -1.
            }

            // Is it an octal value?
            if ( trimmed.startsWith( "0" ) )
            {
                if ( trimmed.length() == 4 )
                {
                    readOwnerOctalValue( trimmed.charAt( 1 ) );
                    readGroupOctalValue( trimmed.charAt( 2 ) );
                    readOthersOctalValue( trimmed.charAt( 3 ) );
                }
                else
                {
                    throw new ParseException( "Unable to recognize the format for this Unix Permissions String '" + s
                        + "'.", 0 );
                }
            }
            // Is it a decimal value?
            else if ( integerValue != -1 )
            {
                String octal = Integer.toOctalString( integerValue );

                if ( octal.length() == 1 )
                {
                    octal = "00" + octal;
                }
                else if ( octal.length() == 2 )
                {
                    octal = "0" + octal;
                }

                readOwnerOctalValue( octal.charAt( 0 ) );
                readGroupOctalValue( octal.charAt( 1 ) );
                readOthersOctalValue( octal.charAt( 2 ) );
            }
            // Is it a symbolic value?
            else if ( SYMBOLIC_FORMAT_PATTERN.matcher( trimmed ).matches() )
            {
                readOwnerSymbolicValue( trimmed.substring( 1, 4 ) );
                readGroupSymbolicValue( trimmed.substring( 4, 7 ) );
                readOthersSymbolicValue( trimmed.substring( 7, 10 ) );
            }
            else
            {
                throw new ParseException( "Unable to recognize the format for this Unix Permissions String '" + s
                    + "'.", 0 );
            }
        }
    }


    // ── METHOD: readOwnerOctalValue — DECODING THE OWNER'S OCTAL STAMP ────────
    // We interpret the single octal digit for the owner tier and set the three
    // boolean flags accordingly: bit 2 is read, bit 1 is write, bit 0 is execute.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We decode a single octal character representing the owner's permission
     * bits and set {@link #ownerRead}, {@link #ownerWrite}, and
     * {@link #ownerExecute} accordingly.
     *
     * @param ownerValue  the single octal digit character for the owner tier
     */
    private void readOwnerOctalValue( char ownerValue )
    {
        if ( ownerValue == '1' )
        {
            ownerExecute = true;
        }
        else if ( ownerValue == '2' )
        {
            ownerWrite = true;
        }
        else if ( ownerValue == '3' )
        {
            ownerExecute = true;
            ownerWrite = true;
        }
        else if ( ownerValue == '4' )
        {
            ownerRead = true;
        }
        else if ( ownerValue == '5' )
        {
            ownerExecute = true;
            ownerRead = true;
        }
        else if ( ownerValue == '6' )
        {
            ownerWrite = true;
            ownerRead = true;
        }
        else if ( ownerValue == '7' )
        {
            ownerExecute = true;
            ownerWrite = true;
            ownerRead = true;
        }
    }


    // ── METHOD: readGroupOctalValue — DECODING THE GROUP'S OCTAL STAMP ────────
    // We interpret the single octal digit for the group tier and set the three
    // boolean flags accordingly, just like we do for the owner.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We decode a single octal character representing the group's permission
     * bits and set {@link #groupRead}, {@link #groupWrite}, and
     * {@link #groupExecute} accordingly.
     *
     * @param groupValue  the single octal digit character for the group tier
     */
    private void readGroupOctalValue( char groupValue )
    {
        if ( groupValue == '1' )
        {
            groupExecute = true;
        }
        else if ( groupValue == '2' )
        {
            groupWrite = true;
        }
        else if ( groupValue == '3' )
        {
            groupExecute = true;
            groupWrite = true;
        }
        else if ( groupValue == '4' )
        {
            groupRead = true;
        }
        else if ( groupValue == '5' )
        {
            groupExecute = true;
            groupRead = true;
        }
        else if ( groupValue == '6' )
        {
            groupWrite = true;
            groupRead = true;
        }
        else if ( groupValue == '7' )
        {
            groupExecute = true;
            groupWrite = true;
            groupRead = true;
        }
    }


    // ── METHOD: readOthersOctalValue — DECODING THE OTHERS' OCTAL STAMP ───────
    // We interpret the single octal digit for the others tier and set the three
    // boolean flags accordingly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We decode a single octal character representing the others' permission
     * bits and set {@link #othersRead}, {@link #othersWrite}, and
     * {@link #othersExecute} accordingly.
     *
     * @param othersValue  the single octal digit character for the others tier
     */
    private void readOthersOctalValue( char othersValue )
    {
        if ( othersValue == '1' )
        {
            othersExecute = true;
        }
        else if ( othersValue == '2' )
        {
            othersWrite = true;
        }
        else if ( othersValue == '3' )
        {
            othersExecute = true;
            othersWrite = true;
        }
        else if ( othersValue == '4' )
        {
            othersRead = true;
        }
        else if ( othersValue == '5' )
        {
            othersExecute = true;
            othersRead = true;
        }
        else if ( othersValue == '6' )
        {
            othersWrite = true;
            othersRead = true;
        }
        else if ( othersValue == '7' )
        {
            othersExecute = true;
            othersWrite = true;
            othersRead = true;
        }
    }


    // ── METHOD: readOwnerSymbolicValue — READING THE OWNER'S SYMBOLIC BADGE ───
    // We read the three-character symbolic string for the owner (e.g., "rw-")
    // and set the boolean flags for each letter present.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We parse the three-character symbolic string for the owner tier
     * (e.g., {@code "rw-"}) and set {@link #ownerRead}, {@link #ownerWrite},
     * and {@link #ownerExecute} based on the characters present.
     *
     * @param ownerValue  the three-character symbolic string for the owner tier
     */
    private void readOwnerSymbolicValue( String ownerValue )
    {
        if ( ownerValue.length() == 3 )
        {
            // Read
            if ( ownerValue.charAt( 0 ) == 'r' )
            {
                ownerRead = true;
            }

            // Write
            if ( ownerValue.charAt( 1 ) == 'w' )
            {
                ownerWrite = true;
            }

            // Execute
            if ( ownerValue.charAt( 2 ) == 'x' )
            {
                ownerExecute = true;
            }
        }
    }


    // ── METHOD: readGroupSymbolicValue — READING THE GROUP'S SYMBOLIC BADGE ───
    // We read the three-character symbolic string for the group tier and set
    // the boolean flags exactly as we do for the owner.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We parse the three-character symbolic string for the group tier
     * (e.g., {@code "r--"}) and set {@link #groupRead}, {@link #groupWrite},
     * and {@link #groupExecute} based on the characters present.
     *
     * @param groupValue  the three-character symbolic string for the group tier
     */
    private void readGroupSymbolicValue( String groupValue )
    {
        if ( groupValue.length() == 3 )
        {
            // Read
            if ( groupValue.charAt( 0 ) == 'r' )
            {
                groupRead = true;
            }

            // Write
            if ( groupValue.charAt( 1 ) == 'w' )
            {
                groupWrite = true;
            }

            // Execute
            if ( groupValue.charAt( 2 ) == 'x' )
            {
                groupExecute = true;
            }
        }
    }


    // ── METHOD: readOthersSymbolicValue — READING THE OTHERS' SYMBOLIC BADGE ──
    // We read the three-character symbolic string for the others tier and set
    // the boolean flags exactly as we do for the owner and group.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We parse the three-character symbolic string for the others tier
     * (e.g., {@code "---"}) and set {@link #othersRead}, {@link #othersWrite},
     * and {@link #othersExecute} based on the characters present.
     *
     * @param othersValue  the three-character symbolic string for the others tier
     */
    private void readOthersSymbolicValue( String othersValue )
    {
        if ( othersValue.length() == 3 )
        {
            // Read
            if ( othersValue.charAt( 0 ) == 'r' )
            {
                othersRead = true;
            }

            // Write
            if ( othersValue.charAt( 1 ) == 'w' )
            {
                othersWrite = true;
            }

            // Execute
            if ( othersValue.charAt( 2 ) == 'x' )
            {
                othersExecute = true;
            }
        }
    }


    // ── METHOD: getDecimalValue — PRINTING THE DECIMAL PASS CODE ─────────────
    // We convert our octal string representation to a plain integer so callers
    // that prefer a numeric database value can get it in one call.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We compute and return the decimal integer value of these permissions by
     * parsing our octal string representation as base-8.
     *
     * @return the decimal integer representation of the permission bits
     */
    public Integer getDecimalValue()
    {
        return Integer.parseInt( getOctalValue(), 8 );
    }


    // ── METHOD: getOctalValue — PRINTING THE OCTAL PASS CODE ─────────────────
    // We walk through all nine boolean flags, add up their octal contribution
    // (owner bits contribute hundreds, group bits contribute tens, others bits
    // contribute units), and return a zero-padded four-character string like
    // "0640". This is what the OpenLDAP configuration expects.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We compute and return the zero-padded four-digit octal string for these
     * permissions (e.g., {@code "0640"}).
     *
     * @return the four-character octal representation of the permission bits
     */
    public String getOctalValue()
    {
        int value = 0;

        // Owner Read
        if ( ownerRead )
        {
            value = value + 400;
        }

        // Owner Write
        if ( ownerWrite )
        {
            value = value + 200;
        }

        // Owner Execute
        if ( ownerExecute )
        {
            value = value + 100;
        }

        // Group Read
        if ( groupRead )
        {
            value = value + 40;
        }

        // Group Write
        if ( groupWrite )
        {
            value = value + 20;
        }

        // Group Execute
        if ( groupExecute )
        {
            value = value + 10;
        }

        // Others Read
        if ( othersRead )
        {
            value = value + 4;
        }

        // Others Write
        if ( othersWrite )
        {
            value = value + 2;
        }

        // Others Execute
        if ( othersExecute )
        {
            value = value + 1;
        }

        // Adding zeros before returning the value
        if ( value < 10 )
        {
            return "000" + value;
        }
        else if ( value < 100 )
        {
            return "00" + value;
        }
        else if ( value < 1000 )
        {
            return "0" + value;
        }
        else
        {
            return "" + value;
        }
    }


    // ── METHOD: getSymbolicValue — PRINTING THE SYMBOLIC BADGE ───────────────
    // We walk through all nine boolean flags and build the ten-character
    // symbolic string in the order "-rwxrwxrwx", using '-' for any absent
    // right. The leading '-' indicates a regular file (not a directory).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We build and return the ten-character symbolic permission string
     * (e.g., {@code "-rw-r-----"}). The leading {@code '-'} represents a
     * regular file type prefix.
     *
     * @return the symbolic representation of the permission bits
     */
    public String getSymbolicValue()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( '-' );

        // Owner Read
        if ( ownerRead )
        {
            sb.append( 'r' );
        }
        else
        {
            sb.append( '-' );
        }

        // Owner Write
        if ( ownerWrite )
        {
            sb.append( 'w' );
        }
        else
        {
            sb.append( '-' );
        }

        // Owner Execute
        if ( ownerExecute )
        {
            sb.append( 'x' );
        }
        else
        {
            sb.append( '-' );
        }

        // Group Read
        if ( groupRead )
        {
            sb.append( 'r' );
        }
        else
        {
            sb.append( '-' );
        }

        // Group Write
        if ( groupWrite )
        {
            sb.append( 'w' );
        }
        else
        {
            sb.append( '-' );
        }

        // Group Execute
        if ( groupExecute )
        {
            sb.append( 'x' );
        }
        else
        {
            sb.append( '-' );
        }

        // Others Read
        if ( othersRead )
        {
            sb.append( 'r' );
        }
        else
        {
            sb.append( '-' );
        }

        // Others Write
        if ( othersWrite )
        {
            sb.append( 'w' );
        }
        else
        {
            sb.append( '-' );
        }

        // Others Execute
        if ( othersExecute )
        {
            sb.append( 'x' );
        }
        else
        {
            sb.append( '-' );
        }

        return sb.toString();
    }


    // ── METHOD: isGroupExecute — CHECKING GROUP EXECUTE RIGHT ────────────────
    // We return the group execute flag so callers can read the current state
    // without modifying it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return {@code true} if the group execute permission bit is set.
     *
     * @return {@code true} if group execute is granted
     */
    public boolean isGroupExecute()
    {
        return groupExecute;
    }


    // ── METHOD: isGroupRead — CHECKING GROUP READ RIGHT ──────────────────────
    // We return the group read flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return {@code true} if the group read permission bit is set.
     *
     * @return {@code true} if group read is granted
     */
    public boolean isGroupRead()
    {
        return groupRead;
    }


    // ── METHOD: isGroupWrite — CHECKING GROUP WRITE RIGHT ────────────────────
    // We return the group write flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return {@code true} if the group write permission bit is set.
     *
     * @return {@code true} if group write is granted
     */
    public boolean isGroupWrite()
    {
        return groupWrite;
    }


    // ── METHOD: isOthersExecute — CHECKING OTHERS EXECUTE RIGHT ──────────────
    // We return the others execute flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return {@code true} if the others execute permission bit is set.
     *
     * @return {@code true} if others execute is granted
     */
    public boolean isOthersExecute()
    {
        return othersExecute;
    }


    // ── METHOD: isOthersRead — CHECKING OTHERS READ RIGHT ────────────────────
    // We return the others read flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return {@code true} if the others read permission bit is set.
     *
     * @return {@code true} if others read is granted
     */
    public boolean isOthersRead()
    {
        return othersRead;
    }


    // ── METHOD: isOthersWrite — CHECKING OTHERS WRITE RIGHT ──────────────────
    // We return the others write flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return {@code true} if the others write permission bit is set.
     *
     * @return {@code true} if others write is granted
     */
    public boolean isOthersWrite()
    {
        return othersWrite;
    }


    // ── METHOD: isOwnerExecute — CHECKING OWNER EXECUTE RIGHT ────────────────
    // We return the owner execute flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return {@code true} if the owner execute permission bit is set.
     *
     * @return {@code true} if owner execute is granted
     */
    public boolean isOwnerExecute()
    {
        return ownerExecute;
    }


    // ── METHOD: isOwnerRead — CHECKING OWNER READ RIGHT ──────────────────────
    // We return the owner read flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return {@code true} if the owner read permission bit is set.
     *
     * @return {@code true} if owner read is granted
     */
    public boolean isOwnerRead()
    {
        return ownerRead;
    }


    // ── METHOD: isOwnerWrite — CHECKING OWNER WRITE RIGHT ────────────────────
    // We return the owner write flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return {@code true} if the owner write permission bit is set.
     *
     * @return {@code true} if owner write is granted
     */
    public boolean isOwnerWrite()
    {
        return ownerWrite;
    }


    // ── METHOD: setGroupExecute — STAMPING THE GROUP EXECUTE RIGHT ───────────
    // We update the group execute flag to the supplied value, granting or
    // revoking that right on the access pass.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We set the group execute permission bit to the supplied value.
     *
     * @param groupExecute  {@code true} to grant group execute, {@code false} to revoke it
     */
    public void setGroupExecute( boolean groupExecute )
    {
        this.groupExecute = groupExecute;
    }


    // ── METHOD: setGroupRead — STAMPING THE GROUP READ RIGHT ─────────────────
    // We update the group read flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We set the group read permission bit to the supplied value.
     *
     * @param groupRead  {@code true} to grant group read, {@code false} to revoke it
     */
    public void setGroupRead( boolean groupRead )
    {
        this.groupRead = groupRead;
    }


    // ── METHOD: setGroupWrite — STAMPING THE GROUP WRITE RIGHT ───────────────
    // We update the group write flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We set the group write permission bit to the supplied value.
     *
     * @param groupWrite  {@code true} to grant group write, {@code false} to revoke it
     */
    public void setGroupWrite( boolean groupWrite )
    {
        this.groupWrite = groupWrite;
    }


    // ── METHOD: setOthersExecute — STAMPING THE OTHERS EXECUTE RIGHT ──────────
    // We update the others execute flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We set the others execute permission bit to the supplied value.
     *
     * @param othersExecute  {@code true} to grant others execute, {@code false} to revoke it
     */
    public void setOthersExecute( boolean othersExecute )
    {
        this.othersExecute = othersExecute;
    }


    // ── METHOD: setOthersRead — STAMPING THE OTHERS READ RIGHT ───────────────
    // We update the others read flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We set the others read permission bit to the supplied value.
     *
     * @param othersRead  {@code true} to grant others read, {@code false} to revoke it
     */
    public void setOthersRead( boolean othersRead )
    {
        this.othersRead = othersRead;
    }


    // ── METHOD: setOthersWrite — STAMPING THE OTHERS WRITE RIGHT ─────────────
    // We update the others write flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We set the others write permission bit to the supplied value.
     *
     * @param othersWrite  {@code true} to grant others write, {@code false} to revoke it
     */
    public void setOthersWrite( boolean othersWrite )
    {
        this.othersWrite = othersWrite;
    }


    // ── METHOD: setOwnerExecute — STAMPING THE OWNER EXECUTE RIGHT ───────────
    // We update the owner execute flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We set the owner execute permission bit to the supplied value.
     *
     * @param ownerExecute  {@code true} to grant owner execute, {@code false} to revoke it
     */
    public void setOwnerExecute( boolean ownerExecute )
    {
        this.ownerExecute = ownerExecute;
    }


    // ── METHOD: setOwnerRead — STAMPING THE OWNER READ RIGHT ─────────────────
    // We update the owner read flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We set the owner read permission bit to the supplied value.
     *
     * @param ownerRead  {@code true} to grant owner read, {@code false} to revoke it
     */
    public void setOwnerRead( boolean ownerRead )
    {
        this.ownerRead = ownerRead;
    }


    // ── METHOD: setOwnerWrite — STAMPING THE OWNER WRITE RIGHT ───────────────
    // We update the owner write flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We set the owner write permission bit to the supplied value.
     *
     * @param ownerWrite  {@code true} to grant owner write, {@code false} to revoke it
     */
    public void setOwnerWrite( boolean ownerWrite )
    {
        this.ownerWrite = ownerWrite;
    }
}
