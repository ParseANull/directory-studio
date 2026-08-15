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
package org.apache.directory.studio.openldap.config;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.util.tree.DnNode;
import org.apache.directory.studio.openldap.config.model.io.ConfigurationException;
import org.apache.directory.studio.openldap.config.model.io.ConfigurationUtils;

// ── CLASS: ExpandedLdifUtils — Yoda Lifts The X-Wing From The Swamp ──────────
// In The Empire Strikes Back, Luke's X-wing is sunk in the Dagobah swamp.
// Piece by piece, Yoda reaches out with the Force and transforms the submerged
// heap of metal into a hovering, flight-ready starfighter — converting raw,
// disorganized physical matter into something structured and useful.
// OpenLDAP's slapd.d directory is the same kind of mess: a hierarchy of
// individual .ldif files, each representing one config entry, buried in nested
// folders. We transform that physical filesystem tree into an in-memory
// DnNode<Entry> hierarchy — and we can write it back the other way too.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class for reading and writing OpenLDAP's "expanded LDIF" directory format.
 * The expanded format is OpenLDAP's slapd.d layout: each config entry lives in its
 * own .ldif file, and child entries live in a subdirectory with the same name as the
 * parent file (minus the .ldif extension). We transform this filesystem hierarchy
 * into a {@link DnNode} tree of {@link Entry} objects, and write it back out again.
 * Think of us as Yoda on Dagobah — we lift what looks like a mess of scattered files
 * and reassemble it into the logical structure the editor needs.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExpandedLdifUtils
{
    // ── Yoda Refuses To Take On An Apprentice ────────────────────────────────
    // Yoda is famously reluctant — "Do, or do not. There is no try." He's not
    // going to let just anyone instantiate him. This utility class is all static
    // methods; there's no meaningful state to construct.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — this is a pure static utility class, there's nothing
     * to instantiate. All the useful methods are static.
     */
    private ExpandedLdifUtils()
    {
        // Nothing to do
    }


    /** The LDIF file extension (.ldif) */
    private static final String LDIF_FILE_EXTENSION = ".ldif";

    /** A filter used to pick all the LDIF files */
    private static FileFilter ldifFileFilter = dir ->
        {
            if ( dir.getName().endsWith( LDIF_FILE_EXTENSION ) )
            {
                return dir.isFile();
            }
            else
            {
                return false;
            }
        };


    // ── Yoda Surveys The Swamp And Begins The Lift ───────────────────────────
    // Yoda stands at the edge of the swamp, eyes closed, arms extended. He takes
    // in the whole scene — every submerged piece of the X-wing — before he starts
    // the transformation. This is the entry point: scan the whole directory tree
    // and turn it into our in-memory data structure.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads an expanded LDIF directory tree rooted at the given directory and
     * returns the corresponding in-memory {@link DnNode} hierarchy.
     * Each .ldif file becomes an {@link Entry} node; subdirectories map to
     * child nodes in the tree. This is the public entry point — it kicks off
     * the recursive readDirectory walk.
     *
     * @param directory  the root directory (typically the slapd.d directory)
     * @return           a DnNode tree where each node holds one LDAP Entry
     * @throws IOException   if the directory doesn't exist, isn't readable, etc.
     * @throws LdapException if any LDIF file contains invalid LDAP content
     */
    public static DnNode<Entry> read( File directory ) throws IOException, LdapException
    {
        DnNode<Entry> tree = new DnNode<>();

        readDirectory( directory, Dn.EMPTY_DN, tree );

        return tree;
    }


    // ── Yoda Lifts Each Piece Of The X-Wing ───────────────────────────────────
    // Yoda doesn't lift the whole X-wing at once. He works through each component:
    // wings, fuselage, engines — each piece rises from the swamp and takes its
    // rightful place in the assembled ship. We do the same recursively: read each
    // .ldif file in this directory, attach it to the tree, then recurse into
    // any child directories.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Recursively reads all .ldif files in the given directory and adds them as
     * nodes in the tree under the given parent DN. After processing each .ldif file,
     * we check if a matching subdirectory exists (same name without the .ldif) and
     * recurse into it for child entries. This mirrors the slapd.d layout exactly.
     *
     * @param directory  the directory to scan for .ldif files
     * @param parentDn   the DN of this directory's parent entry in the tree
     * @param tree       the DnNode tree we're building up
     * @throws IOException   if any directory or file can't be accessed
     * @throws LdapException if any LDIF file contains invalid content
     */
    private static void readDirectory( File directory, Dn parentDn, DnNode<Entry> tree ) throws IOException,
        LdapException
    {
        if ( directory != null )
        {
            // Checking if the directory exists
            if ( !directory.exists() )
            {
                throw new IOException( "Location '" + directory + "' does not exist." );
            }

            // Checking if the directory is a directory
            if ( !directory.isDirectory() )
            {
                throw new IOException( "Location '" + directory + "' is not a directory." );
            }

            // Checking if the directory is readable
            if ( !directory.canRead() )
            {
                throw new IOException( "Directory '" + directory + "' can not be read." );
            }

            // Getting the array of ldif files
            File[] ldifFiles = directory.listFiles( ldifFileFilter );

            if ( ( ldifFiles != null ) && ( ldifFiles.length != 0 ) )
            {
                try ( LdifReader ldifReader = new LdifReader() )
                {
                    // Looping on LDIF files
                    for ( File ldifFile : ldifFiles )
                    {
                        // Checking if the LDIF file is a file
                        if ( !ldifFile.isFile() )
                        {
                            throw new IOException( "Location '" + ldifFile + "' is not a file." );
                        }

                        // Checking if the LDIF file is readable
                        if ( !ldifFile.canRead() )
                        {
                            throw new IOException( "LDIF file '" + ldifFile + "' can not be read." );
                        }

                        // Computing the DN of the entry
                        Dn entryDn = parentDn.add( stripExtension( ldifFile.getName() ) );

                        // Reading the LDIF file
                        List<LdifEntry> ldifEntries = null;

                        try
                        {
                            ldifEntries = ldifReader.parseLdifFile( ldifFile.getAbsolutePath() );
                        }
                        finally
                        {
                            ldifReader.close();
                        }

                        // The LDIF file should have only one entry
                        if ( ( ldifEntries != null ) && ( ldifEntries.size() == 1 ) )
                        {
                            // Getting the LDIF entry
                            LdifEntry ldifEntry = ldifEntries.get( 0 );

                            if ( ldifEntry != null )
                            {
                                // Getting the entry
                                Entry entry = ldifEntry.getEntry();

                                if ( entry != null )
                                {
                                    // Refactoring the DN to set the "FULL" DN of the entry
                                    entry.setDn( entryDn );

                                    // Creating the new entry node
                                    tree.add( entryDn, entry );

                                    // Creating a file without the LDIF extension (corresponding to children directory)
                                    File childrenDirectoryFile = new File( stripExtension( ldifFile.getAbsolutePath() ) );

                                    // If the directory exists, recursively read it
                                    if ( childrenDirectoryFile.exists() )
                                    {
                                        readDirectory( childrenDirectoryFile, entryDn, tree );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    // ── Yoda Trims The Filename To Just The Force-Sensitive Part ─────────────
    // Yoda can feel which part of the X-wing's hull is essential and which is
    // just the outer casing. We strip the ".ldif" extension off the filename
    // to get the bare RDN value that we'll use as the LDAP DN component.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Strips the file extension from the given path string, returning everything
     * before the last dot. We use this to go from "cn=config.ldif" to "cn=config",
     * which becomes the RDN added to the parent DN for this entry.
     *
     * @param path  the filename or path to strip
     * @return      the path without its extension, or null if the path is null/empty
     */
    private static String stripExtension( String path )
    {
        if ( ( path != null ) && ( path.length() > 0 ) )
        {
            return path.substring( 0, path.lastIndexOf( '.' ) );
        }

        return null;
    }


    // ── Yoda Lowers The X-Wing Back Into The Swamp — In A Different Spot ─────
    // Once the X-wing is flight-ready, Yoda can set it down wherever Luke needs it.
    // We do the reverse operation: take the in-memory DnNode tree and write
    // each entry back out as a .ldif file in the right place on disk.
    // This public overload uses the default config DN as the tree root.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Writes the entire DnNode tree to disk in expanded LDIF format.
     * We use the default configuration DN (from {@link ConfigurationUtils}) as
     * the root — this is the "cn=config" entry that anchors the whole slapd.d tree.
     * Delegates to the full {@link #write(DnNode, Dn, File)} overload.
     *
     * @param tree       the in-memory configuration tree to persist
     * @param directory  the target directory (should be an empty or existing slapd.d dir)
     * @throws IOException  if the directory isn't writable or any file write fails
     */
    public static void write( DnNode<Entry> tree, File directory ) throws IOException
    {
        try
        {
            Dn rootDn = ConfigurationUtils.getDefaultConfigurationDn();
            write( tree, rootDn, directory );
        }
        catch ( ConfigurationException e )
        {
            throw new IOException( e );
        }
    }


    // ── Yoda Sets Each Piece Down In Its Proper Place ─────────────────────────
    // Yoda doesn't just dump all the X-wing parts in a pile — he places each
    // component in exactly the right spot relative to all the others. We do the
    // same: for each node in the tree, write the .ldif file for this entry, then
    // create the child directory and recurse for all children.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Writes the entry at the given DN and all its descendants from the DnNode tree
     * to disk in expanded LDIF format. For each entry: we write a .ldif file named
     * after the entry's RDN, and if the entry has children we create a subdirectory
     * with the same base name and recurse into it.
     * Note: we temporarily set the entry's DN to just the RDN before writing (OpenLDAP
     * slapd.d stores only the RDN in each file, not the full DN).
     *
     * @param tree       the full configuration tree
     * @param dn         the DN of the entry to write at this recursion level
     * @param directory  the filesystem directory to write this entry's .ldif file into
     * @throws IOException  if the directory is null, missing, not a directory,
     *                      not writable, or any file write fails
     */
    public static void write( DnNode<Entry> tree, Dn dn, File directory ) throws IOException
    {
        // Checking if the directory is null
        if ( directory == null )
        {
            throw new IOException( "Location is 'null'." );
        }

        // Checking if the directory exists
        if ( !directory.exists() )
        {
            throw new IOException( "Location '" + directory + "' does not exist." );
        }

        // Checking if the directory is a directory
        if ( !directory.isDirectory() )
        {
            throw new IOException( "Location '" + directory + "' is not a directory." );
        }

        // Checking if the directory is writable
        if ( !directory.canWrite() )
        {
            throw new IOException( "Directory '" + directory + "' can not be written." );
        }

        // Getting the entry node
        DnNode<Entry> node = tree.getNode( dn );

        // Only creating a file if the node contains an entry
        if ( node.hasElement() )
        {
            // Getting the entry
            Entry entry = node.getElement();

            // Getting the DN of the entry
            Dn entryDn = entry.getDn();

            // Setting the RDN as DN (specific to OpenLDAP implementation)
            try
            {
                entry.setDn( new Dn( entryDn.getRdn() ) );
            }
            catch ( LdapInvalidDnException e )
            {
                throw new IOException( e );
            }

            // Writing the LDIF file to the disk
            try ( FileWriter fw = new FileWriter( new File( directory, getLdifFilename( entry.getDn() ) ) ) )
            {
                fw.write( new LdifEntry( entry ).toString() );
            }

            // Checking if the entry has children
            if ( node.hasChildren() )
            {
                // Creating the child directory on disk
                File childDirectory = new File( directory, getFilename( entry.getDn() ) );
                childDirectory.mkdir();

                // Iterating on all children
                for ( DnNode<Entry> childNode : node.getChildren().values() )
                {
                    if ( childNode.hasElement() )
                    {
                        // Recursively call the method with the child node and directory
                        write( tree, childNode.getDn(), childDirectory );
                    }
                }
            }
        }
    }


    // ── Yoda Labels The X-Wing Part With A ".ldif" Tag ───────────────────────
    // Every component Yoda places gets a label so it can be found later. We
    // construct the full filename: the RDN string value plus the ".ldif" extension.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the .ldif filename for the given DN (using only the RDN).
     * For a DN like "cn=config", this returns "cn=config.ldif". The caller
     * uses this to name the file on disk.
     *
     * @param dn  the DN whose RDN should become the filename base
     * @return    the full filename with .ldif extension, or null if dn is null/empty
     */
    private static String getLdifFilename( Dn dn )
    {
        String filename = getFilename( dn );

        if ( filename != null )
        {
            return filename + LDIF_FILE_EXTENSION;
        }

        return null;
    }


    // ── Yoda Reads The Part Number ────────────────────────────────────────────
    // Before adding the ".ldif" label, Yoda reads the base identifier of each
    // component — the RDN string. We use this for both the .ldif filename and
    // the subdirectory name.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the base filename (without extension) for the given DN, using its RDN.
     * For a DN like "cn=config", this returns "cn=config". We use this both
     * for building .ldif filenames and for creating child subdirectory names.
     *
     * @param dn  the DN to extract the filename from
     * @return    the RDN string, or null if dn is null or empty
     */
    private static String getFilename( Dn dn )
    {
        if ( ( dn != null ) && ( dn.size() > 0 ) )
        {
            return dn.getRdn().toString();
        }

        return null;
    }
}
