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

package org.apache.directory.studio.ldifparser.parser;


import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.studio.ldifparser.model.LdifEOFPart;
import org.apache.directory.studio.ldifparser.model.LdifEnumeration;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.apache.directory.studio.ldifparser.model.LdifInvalidPart;
import org.apache.directory.studio.ldifparser.model.LdifPart;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeAddRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeDeleteRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeModDnRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeModifyRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifCommentContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifContentRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifEOFContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifInvalidContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifModSpec;
import org.apache.directory.studio.ldifparser.model.container.LdifRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifSepContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifVersionContainer;
import org.apache.directory.studio.ldifparser.model.lines.LdifAttrValLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifChangeTypeLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifCommentLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifControlLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifDeloldrdnLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifDnLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifModSpecSepLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifModSpecTypeLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifNewrdnLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifNewsuperiorLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifSepLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifVersionLine;


// ── CLASS: LdifParser — C-3PO'S LDIF TRANSLATION ENGINE ──────────────────────
// When C-3PO receives a raw LDIF transmission he reads it token by token,
// assembles each fragment into the correct structural container — content
// record, change-add, change-modify, change-delete, moddn — and hands back an
// ordered enumeration of those containers.
// LdifParser is that translation engine.  It drives an LdifScanner to tokenise
// the input, then applies an RFC 2849 state machine to build a LdifFile (or a
// lazy LdifEnumeration) from the token stream.  The parser handles malformed
// input gracefully by wrapping bad tokens in LdifInvalidPart / LdifInvalidContainer.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * RFC 2849 LDIF parser.
 * Drives an {@link LdifScanner} to tokenise the input stream, then assembles
 * tokens into a hierarchy of model containers ({@link LdifFile},
 * {@link LdifContentRecord}, {@link LdifChangeAddRecord}, etc.).
 * Entry points:
 * <ul>
 *   <li>{@link #parse(String)} — convenience method that parses a complete
 *       LDIF string and returns a fully-populated {@link LdifFile}.</li>
 *   <li>{@link #parse(Reader)} — streaming API that returns a lazy
 *       {@link LdifEnumeration}; the caller retrieves one container at a time.</li>
 * </ul>
 * Think of this as C-3PO's translation engine — he reads the raw transmission,
 * applies the grammar he knows, and hands back a structured model.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifParser
{

    /** The low-level character scanner that produces tokens. */
    private LdifScanner scanner;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────
    // ── Spin Up the Translation Engine ───────────────────────────────────────
    // Before C-3PO can translate anything he initialises his protocol modules.
    // We create a fresh LdifScanner ready to be pointed at an input stream.
    /**
     * Creates a new parser backed by a fresh {@link LdifScanner}.
     */
    public LdifParser()
    {
        scanner = new LdifScanner();
    }


    // ── PUBLIC API ────────────────────────────────────────────────────────────
    // ── Parse a Complete LDIF String ─────────────────────────────────────────
    // The whole transmission arrives at once and C-3PO processes it from start
    // to end, assembling every container before reporting back.
    // We wrap the string in a StringReader, drive the enumeration to completion,
    // and return the fully-populated LdifFile.
    /**
     * Parses a complete LDIF string and returns the model as a
     * {@link LdifFile}.
     *
     * @param ldif  the LDIF text to parse, or {@code null} (returns an empty
     *              model)
     * @return a {@link LdifFile} containing all parsed containers
     */
    public LdifFile parse( String ldif )
    {
        LdifFile model = new LdifFile();

        if ( ldif != null )
        {
            LdifEnumeration enumeration = parse( new StringReader( ldif ) );

            try
            {
                while ( enumeration.hasNext() )
                {
                    LdifContainer container = enumeration.next();
                    model.addContainer( container );
                }
            }
            catch ( Exception e )
            {
            }
        }

        return model;
    }


    // ── Streaming Parse from a Reader ─────────────────────────────────────────
    // The transmission arrives as a live data stream and C-3PO must hand each
    // completed record to the recipient before the next one arrives.
    // We return a lazy LdifEnumeration backed by an anonymous inner class;
    // hasNext() parses just enough to know whether a container is waiting, and
    // next() removes it from the queue.
    /**
     * Parses an LDIF stream lazily, returning one container per {@code next()}
     * call.
     * The parser drives the header (optional comments + optional version)
     * once, then yields content or change records one at a time until EOF.
     *
     * @param ldifReader  the input stream to parse
     * @return a {@link LdifEnumeration} that yields containers on demand
     */
    public LdifEnumeration parse( Reader ldifReader )
    {
        scanner.setLdif( ldifReader );

        LdifEnumeration enumeration = new LdifEnumeration()
        {

            private List<LdifContainer> containerList = new ArrayList<LdifContainer>();

            private boolean headerParsed = false;

            private boolean bodyParsed = false;

            private boolean footerParsed = false;


            public boolean hasNext()
            {
                if ( containerList.isEmpty() )
                {
                    LdifFile model = new LdifFile();

                    // parse header
                    if ( !headerParsed )
                    {
                        checkAndParseComment( model );
                        checkAndParseVersion( model );
                        checkAndParseComment( model );
                        headerParsed = true;
                    }

                    // parse body (in a loop)
                    bodyParsed = ( headerParsed &&
                        !bodyParsed &&
                        !checkAndParseComment( model ) &&   // parse comment lines
                        !checkAndParseRecord( model ) &&    // parse record
                        !checkAndParseOther( model ) );      // parse unknown

                    // parse footer
                    if ( headerParsed && bodyParsed && !footerParsed )
                    {
                        checkAndParseComment( model );
                        footerParsed = true;
                    }

                    List<LdifContainer> containers = model.getContainers();
                    containerList.addAll( containers );

                    return !containerList.isEmpty() && !( containers.get( 0 ) instanceof LdifEOFContainer );
                }
                else
                {
                    return true;
                }
            }


            public LdifContainer next()
            {
                if ( hasNext() )
                {
                    return containerList.remove( 0 );
                }
                else
                {
                    return null;
                }
            }
        };

        return enumeration;
    }


    // ── RECORD PARSING ────────────────────────────────────────────────────────
    // ── Parse a Single Record ─────────────────────────────────────────────────
    // A record starts with "dn:" which tells C-3PO who the record describes;
    // what follows (control lines, changetype) determines which kind of record
    // it is.
    // We match a dn-spec token, peek ahead for control/changetype, then
    // dispatch to the appropriate sub-parser.
    /**
     * Attempts to parse a single LDIF record (content or change) from the
     * current scanner position and add it to {@code model}.
     * Returns {@code false} and does nothing if no {@code dn:} token is found.
     *
     * @param model  the model to add the parsed record to
     * @return {@code true} if a record was parsed, {@code false} otherwise
     */
    private boolean checkAndParseRecord( LdifFile model )
    {
        // record starts with dn-spec
        LdifToken dnSpecToken = scanner.matchDnSpec();

        if ( dnSpecToken == null )
        {
            return false;
        }

        // get Dn
        LdifToken dnValueTypeToken = null;
        LdifToken dnToken = null;
        LdifToken dnSepToken = null;
        dnValueTypeToken = scanner.matchValueType();

        if ( dnValueTypeToken != null )
        {
            dnToken = scanner.matchValue();

            if ( dnToken != null )
            {
                dnSepToken = scanner.matchSep();
            }
        }

        LdifDnLine dnLine = new LdifDnLine( dnSpecToken.getOffset(), getValueOrNull( dnSpecToken ),
            getValueOrNull( dnValueTypeToken ), getValueOrNull( dnToken ), getValueOrNull( dnSepToken ) );
        LdifToken dnErrorToken = null;

        if ( dnSepToken == null )
        {
            dnErrorToken = scanner.matchCleanupLine();
        }

        // save comment lines after dns
        LdifCommentLine[] commentLines = getCommentLines();

        // check record type: to decide the record type we need the next token
        // first check keywords 'control' and 'changetype'
        LdifControlLine controlLine = getControlLine();
        LdifChangeTypeLine changeTypeLine = getChangeTypeLine();

        if ( controlLine != null || changeTypeLine != null )
        {
            LdifChangeRecord record = null;

            // save all parts before changetype line
            List<LdifPart> partList = new ArrayList<LdifPart>();

            if ( dnErrorToken != null )
            {
                partList.add( new LdifInvalidPart( dnErrorToken.getOffset(), dnErrorToken.getValue() ) );
            }

            for ( LdifCommentLine ldifCommentLine : commentLines )
            {
                partList.add( ldifCommentLine );
            }

            if ( controlLine != null )
            {
                partList.add( controlLine );

                if ( !controlLine.isValid() )
                {
                    LdifToken errorToken = cleanupLine();

                    if ( errorToken != null )
                    {
                        partList.add( new LdifInvalidPart( errorToken.getOffset(), errorToken.getValue() ) );
                    }
                }
            }

            // save comments and controls before changetype line
            while ( changeTypeLine == null && ( commentLines.length > 0 || controlLine != null ) )
            {

                commentLines = getCommentLines();

                for ( LdifCommentLine ldifCommentLine : commentLines )
                {
                    partList.add( ldifCommentLine );
                }

                controlLine = getControlLine();

                if ( controlLine != null )
                {
                    partList.add( controlLine );

                    if ( !controlLine.isValid() )
                    {
                        LdifToken errorToken = cleanupLine();

                        if ( errorToken != null )
                        {
                            partList.add( new LdifInvalidPart( errorToken.getOffset(), errorToken.getValue() ) );
                        }
                    }
                }

                changeTypeLine = getChangeTypeLine();
            }

            if ( changeTypeLine != null )
            {

                if ( changeTypeLine.isAdd() )
                {
                    record = new LdifChangeAddRecord( dnLine );
                    append( record, partList );
                    record.setChangeType( changeTypeLine );

                    if ( !changeTypeLine.isValid() )
                    {
                        this.cleanupLine( record );
                    }

                    parseAttrValRecord( record );
                }
                else if ( changeTypeLine.isDelete() )
                {
                    record = new LdifChangeDeleteRecord( dnLine );
                    append( record, partList );
                    record.setChangeType( changeTypeLine );

                    if ( !changeTypeLine.isValid() )
                    {
                        this.cleanupLine( record );
                    }

                    parseChangeDeleteRecord( record );
                }
                else if ( changeTypeLine.isModify() )
                {
                    record = new LdifChangeModifyRecord( dnLine );
                    append( record, partList );
                    record.setChangeType( changeTypeLine );

                    if ( !changeTypeLine.isValid() )
                    {
                        this.cleanupLine( record );
                    }

                    parseChangeModifyRecord( ( LdifChangeModifyRecord ) record );
                }
                else if ( changeTypeLine.isModDn() )
                {
                    record = new LdifChangeModDnRecord( dnLine );
                    append( record, partList );
                    record.setChangeType( changeTypeLine );

                    if ( !changeTypeLine.isValid() )
                    {
                        this.cleanupLine( record );
                    }

                    parseChangeModDnRecord( ( LdifChangeModDnRecord ) record );
                }
                else
                {
                    record = new LdifChangeRecord( dnLine );
                    append( record, partList );
                    record.setChangeType( changeTypeLine );

                    if ( !changeTypeLine.isValid() )
                    {
                        this.cleanupLine( record );
                    }
                }
            }
            else
            {
                record = new LdifChangeRecord( dnLine );
                append( record, partList );
            }

            model.addContainer( record );
        }
        else
        {
            // match attr-val-record
            LdifContentRecord record = new LdifContentRecord( dnLine );

            if ( dnErrorToken != null )
            {
                record.addInvalid( new LdifInvalidPart( dnErrorToken.getOffset(), dnErrorToken.getValue() ) );
            }

            for ( LdifCommentLine ldifCommentLine : commentLines )
            {
                record.addComment( ldifCommentLine );
            }

            parseAttrValRecord( record );
            model.addContainer( record );
        }

        return true;
    }


    // ── Append Preamble Parts to a Change Record ──────────────────────────────
    // Before we know the changetype the parser has collected comments, controls,
    // and invalid tokens into a list.  Once the record type is determined, those
    // parts must be attached to the record in the right slots.
    // We iterate partList and call the appropriate addXxx() on the record.
    /**
     * Attaches all preamble parts (comments, controls, invalid tokens) that
     * were collected before the changetype line to the given change record.
     *
     * @param record    the change record to populate
     * @param partList  accumulated parts collected before the changetype
     */
    private void append( LdifChangeRecord record, List<LdifPart> partList )
    {
        for ( LdifPart ldifPart : partList )
        {
            if ( ldifPart instanceof LdifCommentLine )
            {
                record.addComment( ( LdifCommentLine ) ldifPart );
            }

            if ( ldifPart instanceof LdifControlLine )
            {
                record.addControl( ( LdifControlLine ) ldifPart );
            }

            if ( ldifPart instanceof LdifInvalidPart )
            {
                record.addInvalid( ( LdifInvalidPart ) ldifPart );
            }
        }
    }


    // ── CHANGE RECORD SUB-PARSERS ─────────────────────────────────────────────
    // ── Parse a Change-Delete Record Body ────────────────────────────────────
    // A delete record has no payload — just comments or whitespace until the
    // end-of-record separator.
    // We loop until we see a sep/EOF, consuming any stray comments or invalid
    // tokens as we go.
    /**
     * Parses the body of a {@code changetype: delete} record.
     * The record ends at the first blank-line separator or EOF.  Stray comment
     * or invalid content is attached to the record but doesn't stop parsing.
     *
     * @param record  the change-delete record to populate
     */
    private void parseChangeDeleteRecord( LdifRecord record )
    {
        do
        {
            if ( checkAndParseEndOfRecord( record ) )
            {
                return;
            }

            if ( !checkAndParseComment( record ) && !checkAndParseOther( record ) )
            {
                return;
            }
        }
        while ( true );
    }


    // ── Parse a Change-ModDN Record Body ─────────────────────────────────────
    // A moddn record carries newrdn, deleteoldrdn, and optionally newsuperior
    // lines in that order — plus optional comments scattered between them.
    // We loop until all three have been read or we hit the end of record.
    /**
     * Parses the body of a {@code changetype: moddn} or
     * {@code changetype: modrdn} record.
     * Reads {@code newrdn}, {@code deleteoldrdn}, and the optional
     * {@code newsuperior} lines in any order, interleaving comment handling.
     *
     * @param record  the change-moddn record to populate
     */
    private void parseChangeModDnRecord( LdifChangeModDnRecord record )
    {
        boolean newrdnRead = false;
        boolean deleteoldrdnRead = false;
        boolean newsuperiorRead = false;

        do
        {
            if ( checkAndParseEndOfRecord( record ) )
            {
                return;
            }

            // comments
            checkAndParseComment( record );

            LdifToken newrdnSpecToken = null;
            LdifToken deleteoldrdnSpecToken = null;
            LdifToken newsuperiorSpecToken = null;

            if ( !newrdnRead )
            {
                newrdnSpecToken = scanner.matchNewrdnSpec();
            }

            if ( !deleteoldrdnRead && newrdnSpecToken == null )
            {
                deleteoldrdnSpecToken = scanner.matchDeleteoldrdnSpec();
            }

            if ( !newsuperiorRead && newrdnSpecToken == null && newsuperiorSpecToken == null )
            {
                newsuperiorSpecToken = scanner.matchNewsuperiorSpec();
            }


            if ( newrdnSpecToken != null )
            {
                // read newrdn line
                newrdnRead = true;
                LdifToken newrdnValueTypeToken = scanner.matchValueType();
                LdifToken newrdnValueToken = scanner.matchValue();
                LdifToken newrdnSepToken = null;

                if ( newrdnValueTypeToken != null || newrdnValueToken != null )
                {
                    newrdnSepToken = scanner.matchSep();
                }

                LdifNewrdnLine newrdnLine = new LdifNewrdnLine( newrdnSpecToken.getOffset(),
                    getValueOrNull( newrdnSpecToken ), getValueOrNull( newrdnValueTypeToken ),
                    getValueOrNull( newrdnValueToken ), getValueOrNull( newrdnSepToken ) );
                record.setNewrdn( newrdnLine );

                if ( newrdnSepToken == null )
                {
                    cleanupLine( record );
                }
            }
            else if ( deleteoldrdnSpecToken != null )
            {
                // read deleteoldrdnline
                deleteoldrdnRead = true;
                LdifToken deleteoldrdnValueTypeToken = scanner.matchValueType();
                LdifToken deleteoldrdnValueToken = scanner.matchValue();
                LdifToken deleteoldrdnSepToken = null;

                if ( deleteoldrdnValueTypeToken != null || deleteoldrdnValueToken != null )
                {
                    deleteoldrdnSepToken = scanner.matchSep();
                }

                LdifDeloldrdnLine deloldrdnLine = new LdifDeloldrdnLine( deleteoldrdnSpecToken.getOffset(),
                    getValueOrNull( deleteoldrdnSpecToken ), getValueOrNull( deleteoldrdnValueTypeToken ),
                    getValueOrNull( deleteoldrdnValueToken ), getValueOrNull( deleteoldrdnSepToken ) );
                record.setDeloldrdn( deloldrdnLine );

                if ( deleteoldrdnSepToken == null )
                {
                    cleanupLine( record );
                }
            }
            else if ( newsuperiorSpecToken != null )
            {
                // read newsuperior line
                newsuperiorRead = true;
                LdifToken newsuperiorValueTypeToken = scanner.matchValueType();
                LdifToken newsuperiorValueToken = scanner.matchValue();
                LdifToken newsuperiorSepToken = null;

                if ( newsuperiorValueTypeToken != null || newsuperiorValueToken != null )
                {
                    newsuperiorSepToken = scanner.matchSep();
                }

                LdifNewsuperiorLine newsuperiorLine = new LdifNewsuperiorLine( newsuperiorSpecToken.getOffset(),
                    getValueOrNull( newsuperiorSpecToken ), getValueOrNull( newsuperiorValueTypeToken ),
                    getValueOrNull( newsuperiorValueToken ), getValueOrNull( newsuperiorSepToken ) );
                record.setNewsuperior( newsuperiorLine );

                if ( newsuperiorSepToken == null )
                {
                    this.cleanupLine( record );
                }
            }
            else
            {
                if ( !checkAndParseComment( record ) && !checkAndParseOther( record ) )
                {
                    return;
                }
            }

            // comments
            checkAndParseComment( record );
        }
        while ( true );
    }


    // ── Parse a Change-Modify Record Body ────────────────────────────────────
    // A modify record is a sequence of mod-specs, each opening with
    // "add:/replace:/delete:", zero or more attr-val lines, and a "-" separator.
    // We loop until we see the end-of-record or can't match a mod-type.
    /**
     * Parses the body of a {@code changetype: modify} record.
     * Reads successive mod-specs (type line, zero or more attr-val lines,
     * closing {@code -} line) until the record ends.
     *
     * @param record  the change-modify record to populate
     */
    private void parseChangeModifyRecord( LdifChangeModifyRecord record )
    {
        do
        {
            if ( checkAndParseEndOfRecord( record ) )
            {
                return;
            }

            // match mod type
            LdifToken modSpecTypeSpecToken = scanner.matchModTypeSpec();

            if ( modSpecTypeSpecToken != null )
            {
                // read mod type line
                LdifToken modSpecTypeValueTypeToken = null;
                LdifToken modSpecTypeAttributeDescriptionToken = null;
                LdifToken sepToken = null;
                modSpecTypeValueTypeToken = scanner.matchValueType();

                if ( modSpecTypeValueTypeToken != null )
                {
                    modSpecTypeAttributeDescriptionToken = scanner.matchAttributeDescription();

                    if ( modSpecTypeAttributeDescriptionToken != null )
                    {
                        sepToken = scanner.matchSep();
                    }
                }

                LdifModSpecTypeLine modSpecTypeLine = new LdifModSpecTypeLine( modSpecTypeSpecToken.getOffset(),
                    getValueOrNull( modSpecTypeSpecToken ), getValueOrNull( modSpecTypeValueTypeToken ),
                    getValueOrNull( modSpecTypeAttributeDescriptionToken ), getValueOrNull( sepToken ) );
                LdifModSpec modSpec = new LdifModSpec( modSpecTypeLine );
                record.addModSpec( modSpec );

                // clean line
                if ( sepToken == null )
                {
                    this.cleanupLine( modSpec );
                }

                // comment
                checkAndParseComment( record );

                // read attr-val lines
                do
                {
                    LdifAttrValLine line = this.getAttrValLine();

                    if ( line != null )
                    {
                        modSpec.addAttrVal( line );

                        // clean line
                        if ( "".equals( line.getRawNewLine() ) ) //$NON-NLS-1$
                        {
                            this.cleanupLine( record );
                        }
                    }
                    else
                    {
                        if ( !checkAndParseComment( record ) )
                        {
                            break;
                        }
                    }
                }
                while ( true );

                // comments
                checkAndParseComment( record );

                // read sep line
                LdifToken modSpecSepToken = scanner.matchModSep();

                if ( modSpecSepToken != null )
                {
                    LdifToken modSpecSepSepToken = scanner.matchSep();
                    LdifModSpecSepLine modSpecSepLine = new LdifModSpecSepLine( modSpecSepToken.getOffset(),
                        getValueOrNull( modSpecSepToken ), getValueOrNull( modSpecSepSepToken ) );
                    modSpec.finish( modSpecSepLine );
                }
            }

            if ( modSpecTypeSpecToken == null )
            {
                if ( !checkAndParseComment( record ) && !checkAndParseOther( record ) )
                {
                    return;
                }
            }
        }
        while ( true );
    }


    // ── Parse an Attr-Val Record Body ─────────────────────────────────────────
    // Content and change-add records are bags of attribute-value lines.  We
    // keep reading attr-val lines until we hit a blank separator or EOF.
    // We loop, attaching each attr-val line to the appropriate record subtype.
    /**
     * Parses a sequence of attribute-value lines and appends them to
     * {@code record} (works for both {@link LdifContentRecord} and
     * {@link LdifChangeAddRecord}).
     *
     * @param record  the record to populate with attr-val lines
     */
    private void parseAttrValRecord( LdifRecord record )
    {
        do
        {
            if ( checkAndParseEndOfRecord( record ) )
            {
                return;
            }

            // check attr-val line
            LdifAttrValLine line = this.getAttrValLine();

            if ( line != null )
            {
                if ( record instanceof LdifContentRecord )
                {
                    ( ( LdifContentRecord ) record ).addAttrVal( line );
                }
                else if ( record instanceof LdifChangeAddRecord )
                {
                    ( ( LdifChangeAddRecord ) record ).addAttrVal( line );
                }

                // clean line
                if ( "".equals( line.getRawNewLine() ) ) //$NON-NLS-1$
                {
                    this.cleanupLine( record );
                }
            }
            else
            {
                if ( !checkAndParseComment( record ) && !checkAndParseOther( record ) )
                {
                    return;
                }
            }
        }
        while ( true );
    }


    // ── HELPER METHODS ────────────────────────────────────────────────────────
    // ── Check for Record-Ending Separator or EOF ──────────────────────────────
    // A blank line or end-of-file signals that the current record is complete
    // — time to stop adding parts to it.
    // We try a sep token first, then an EOF token, and finish the record if
    // either is found.
    /**
     * Checks whether the next token is a blank-line separator or EOF.
     * If so, finishes the record and returns {@code true}.
     *
     * @param record  the record to finish if end-of-record is detected
     * @return {@code true} if the record was finished, {@code false} otherwise
     */
    private boolean checkAndParseEndOfRecord( LdifRecord record )
    {
        // check end of record
        LdifToken eorSepToken = scanner.matchSep();

        if ( eorSepToken != null )
        {
            record.finish( new LdifSepLine( eorSepToken.getOffset(), getValueOrNull( eorSepToken ) ) );

            return true;
        }

        // check end of file
        LdifToken eofToken = scanner.matchEOF();

        if ( eofToken != null )
        {
            record.finish( new LdifEOFPart( eofToken.getOffset() ) );
            return true;
        }

        return false;
    }


    // ── Consume Comment Lines Inside a Record ────────────────────────────────
    // Comment lines can appear anywhere inside a record — the parser must
    // consume them without treating them as attr-vals or structural lines.
    // We match as many consecutive comment tokens as possible and attach each
    // as a LdifCommentLine on the record.
    /**
     * Matches zero or more consecutive comment tokens and attaches them to
     * {@code record}.
     *
     * @param record  the record to attach comments to
     * @return {@code true} if at least one comment was consumed
     */
    private boolean checkAndParseComment( LdifRecord record )
    {
        LdifToken commentToken = scanner.matchComment();

        if ( commentToken != null )
        {
            while ( commentToken != null )
            {
                LdifToken sepToken = scanner.matchSep();
                record.addComment( new LdifCommentLine( commentToken.getOffset(), getValueOrNull( commentToken ),
                    getValueOrNull( sepToken ) ) );
                commentToken = scanner.matchComment();
            }

            return true;
        }
        else
        {
            return false;
        }
    }


    // ── Consume Invalid Content Inside a Record ───────────────────────────────
    // Malformed lines that don't match any grammar rule are consumed and
    // wrapped in a LdifInvalidPart so the model preserves the raw text.
    // We match one "other" token and wrap it.
    /**
     * Matches a single unrecognised token and attaches it to {@code record}
     * as a {@link LdifInvalidPart}.
     *
     * @param record  the record to attach invalid content to
     * @return {@code true} if an invalid token was consumed
     */
    private boolean checkAndParseOther( LdifRecord record )
    {
        LdifToken otherToken = scanner.matchOther();

        if ( otherToken != null )
        {
            record.addInvalid( new LdifInvalidPart( otherToken.getOffset(), otherToken.getValue() ) );
            return true;
        }
        else
        {
            return false;
        }
    }


    // ── Parse Version Header ─────────────────────────────────────────────────
    // The version line is optional at the top of an LDIF file.  C-3PO checks
    // for it before reading any records.
    // We match version-spec, value-type, number, and sep; wrap them in a
    // LdifVersionContainer; and add it to the model.
    /**
     * Checks for a {@code version: 1} line.  If present, parses it and adds
     * a {@link LdifVersionContainer} to {@code model}.
     *
     * @param model  the model to add the version container to
     * @return {@code true} if a version line was parsed
     */
    private boolean checkAndParseVersion( LdifFile model )
    {
        LdifToken versionSpecToken = scanner.matchVersionSpec();

        if ( versionSpecToken != null )
        {

            LdifToken versionTypeToken = null;
            LdifToken versionToken = null;
            LdifToken sepToken = null;
            versionTypeToken = scanner.matchValueType();

            if ( versionTypeToken != null )
            {
                versionToken = scanner.matchNumber();
                if ( versionToken != null )
                {
                    sepToken = scanner.matchSep();
                }
            }

            LdifVersionContainer container = new LdifVersionContainer( new LdifVersionLine( versionSpecToken
                .getOffset(), getValueOrNull( versionSpecToken ), getValueOrNull( versionTypeToken ),
                getValueOrNull( versionToken ), getValueOrNull( sepToken ) ) );
            model.addContainer( container );

            // clean line
            if ( sepToken == null )
            {
                this.cleanupLine( container );
            }

            return true;
        }
        else
        {
            return false;
        }
    }


    // ── Consume Top-Level Comments and Separators ─────────────────────────────
    // At the top level (outside any record) blank lines and comment blocks
    // become their own containers in the model — they're not part of any record.
    // We alternate matching sep tokens and comment tokens until we run out of
    // both, wrapping each in the appropriate container type.
    /**
     * Checks for comment lines or blank lines at the top level of the LDIF
     * file.  If found, wraps each in a {@link LdifCommentContainer} or
     * {@link LdifSepContainer} and adds it to {@code model}.
     *
     * @param model  the model to add top-level containers to
     * @return {@code true} if at least one separator or comment was consumed
     */
    private boolean checkAndParseComment( LdifFile model )
    {
        LdifToken sepToken = scanner.matchSep();
        LdifToken commentToken = scanner.matchComment();

        if ( sepToken != null || commentToken != null )
        {
            while ( sepToken != null || commentToken != null )
            {
                if ( sepToken != null )
                {
                    LdifSepLine sepLine = new LdifSepLine( sepToken.getOffset(), getValueOrNull( sepToken ) );
                    LdifSepContainer sepContainer = new LdifSepContainer( sepLine );
                    model.addContainer( sepContainer );
                }

                if ( commentToken != null )
                {
                    LdifCommentContainer commentContainer = null;

                    while ( commentToken != null )
                    {
                        LdifToken commentSepToken = scanner.matchSep();
                        LdifCommentLine commentLine = new LdifCommentLine( commentToken.getOffset(),
                            getValueOrNull( commentToken ), getValueOrNull( commentSepToken ) );

                        if ( commentContainer == null )
                        {
                            commentContainer = new LdifCommentContainer( commentLine );
                        }
                        else
                        {
                            commentContainer.addComment( commentLine );
                        }

                        commentToken = scanner.matchComment();
                    }

                    model.addContainer( commentContainer );
                }

                sepToken = scanner.matchSep();
                commentToken = scanner.matchComment();
            }

            return true;
        }
        else
        {
            return false;
        }
    }


    // ── Consume Unrecognised Top-Level Content ────────────────────────────────
    // Unrecognised top-level content is wrapped in a LdifInvalidContainer and
    // added to the model so the raw text is never silently discarded.
    // We match one "other" token and wrap it.
    /**
     * Checks for an unrecognised top-level token.  If found, wraps it in a
     * {@link LdifInvalidContainer} and adds it to {@code model}.
     *
     * @param model  the model to add the invalid container to
     * @return always {@code true} if a token was matched, {@code false} at EOF
     */
    private boolean checkAndParseOther( LdifFile model )
    {
        LdifToken token = scanner.matchOther();

        if ( token != null )
        {
            LdifInvalidPart unknownLine = new LdifInvalidPart( token.getOffset(), getValueOrNull( token ) );
            LdifInvalidContainer otherContainer = new LdifInvalidContainer( unknownLine );
            model.addContainer( otherContainer );

            return true;
        }
        else
        {
            return false;
        }
    }


    // ── Control Line Extraction ───────────────────────────────────────────────
    // A control line starts with "control:" and optionally carries an OID,
    // criticality, and value.  We assemble all five segments into one object.
    // We match each segment in turn and construct a LdifControlLine.
    /**
     * Attempts to match a {@code control:} line at the current scanner
     * position.
     *
     * @return a {@link LdifControlLine}, or {@code null} if no control line
     *         was found
     */
    private LdifControlLine getControlLine()
    {
        LdifToken controlSpecToken = scanner.matchControlSpec();

        if ( controlSpecToken != null )
        {
            LdifToken controlTypeToken = null;
            LdifToken oidToken = null;
            LdifToken criticalityToken = null;
            LdifToken valueTypeToken = null;
            LdifToken valueToken = null;
            LdifToken sepToken = null;
            controlTypeToken = scanner.matchValueType();

            if ( controlTypeToken != null )
            {
                oidToken = scanner.matchOid();
                if ( oidToken != null )
                {
                    criticalityToken = scanner.matchCriticality();
                    valueTypeToken = scanner.matchValueType();

                    if ( valueTypeToken != null )
                    {
                        valueToken = scanner.matchValue();
                    }

                    sepToken = scanner.matchSep();
                }
            }

            LdifControlLine controlLine = new LdifControlLine( controlSpecToken.getOffset(),
                getValueOrNull( controlSpecToken ), getValueOrNull( controlTypeToken ), getValueOrNull( oidToken ),
                getValueOrNull( criticalityToken ), getValueOrNull( valueTypeToken ), getValueOrNull( valueToken ),
                getValueOrNull( sepToken ) );

            return controlLine;
        }

        return null;
    }


    // ── Changetype Line Extraction ────────────────────────────────────────────
    // A changetype line starts with "changetype:" and carries an operation
    // keyword — it determines which record subtype we are building.
    // We match spec, value-type, changetype keyword, and sep; wrap them.
    /**
     * Attempts to match a {@code changetype:} line at the current scanner
     * position.
     *
     * @return a {@link LdifChangeTypeLine}, or {@code null} if not found
     */
    private LdifChangeTypeLine getChangeTypeLine()
    {
        LdifToken changeTypeSpecToken = scanner.matchChangeTypeSpec();

        if ( changeTypeSpecToken != null )
        {
            LdifToken changeTypeTypeToken = null;
            LdifToken changeTypeToken = null;
            LdifToken sepToken = null;
            changeTypeTypeToken = scanner.matchValueType();

            if ( changeTypeTypeToken != null )
            {
                changeTypeToken = scanner.matchChangeType();

                if ( changeTypeToken != null )
                {
                    sepToken = scanner.matchSep();
                }
            }

            LdifChangeTypeLine ctLine = new LdifChangeTypeLine( changeTypeSpecToken.getOffset(),
                getValueOrNull( changeTypeSpecToken ), getValueOrNull( changeTypeTypeToken ),
                getValueOrNull( changeTypeToken ), getValueOrNull( sepToken ) );

            return ctLine;
        }

        return null;
    }


    // ── Attr-Val Line Extraction ──────────────────────────────────────────────
    // An attribute-value line starts with an attribute description and carries
    // a value type separator and a value.  It is the workhorse of content and
    // change-add records.
    // We match attribute, value-type, value, and sep; wrap them.
    /**
     * Attempts to match an attribute-value line at the current scanner
     * position.
     *
     * @return an {@link LdifAttrValLine}, or {@code null} if none found
     */
    private LdifAttrValLine getAttrValLine()
    {
        LdifToken attrToken = scanner.matchAttributeDescription();

        if ( attrToken != null )
        {
            LdifToken valueTypeToken = null;
            LdifToken valueToken = null;
            LdifToken sepToken = null;
            valueTypeToken = scanner.matchValueType();

            if ( valueTypeToken != null )
            {
                valueToken = scanner.matchValue();

                if ( valueToken != null )
                {
                    sepToken = scanner.matchSep();
                }
            }

            LdifAttrValLine line = new LdifAttrValLine( attrToken.getOffset(), getValueOrNull( attrToken ),
                getValueOrNull( valueTypeToken ), getValueOrNull( valueToken ), getValueOrNull( sepToken ) );

            return line;
        }

        return null;
    }


    // ── Comment Lines Collection ──────────────────────────────────────────────
    // Comment lines scattered between the DN line and the first structural line
    // must be collected before we can decide which record type we are building.
    // We drain all consecutive comment tokens and return them as an array.
    /**
     * Matches all consecutive comment tokens at the current scanner position
     * and returns them as an array.
     *
     * @return an array of {@link LdifCommentLine}s (may be empty)
     */
    private LdifCommentLine[] getCommentLines()
    {
        List<LdifCommentLine> list = new ArrayList<LdifCommentLine>( 1 );
        LdifToken commentToken = scanner.matchComment();

        while ( commentToken != null )
        {
            LdifToken sepToken = scanner.matchSep();
            list
                .add( new LdifCommentLine( commentToken.getOffset(), commentToken.getValue(), getValueOrNull( sepToken ) ) );

            commentToken = scanner.matchComment();
        }

        return list.toArray( new LdifCommentLine[list.size()] );
    }


    // ── Error Recovery — Cleanup Remainder of Line ────────────────────────────
    // When a line is malformed (missing separator, unexpected content) we must
    // consume whatever remains on that line and attach it as invalid content so
    // the parser can continue on the next line.
    // We match a cleanup token and wrap it in LdifInvalidPart on the container.
    /**
     * Consumes the remainder of the current line on error and attaches it as
     * a {@link LdifInvalidPart} to {@code container}.
     *
     * @param container  the container that owns this malformed line
     */
    private void cleanupLine( LdifContainer container )
    {
        LdifToken errorToken = scanner.matchCleanupLine();

        if ( errorToken != null )
        {
            container.addInvalid( new LdifInvalidPart( errorToken.getOffset(), errorToken.getValue() ) );
        }
    }


    // ── Error Recovery — Return Remainder of Line ─────────────────────────────
    // Sometimes the caller wants the raw error token rather than handling it
    // immediately — for instance when collecting preamble parts before the
    // record type is known.
    // We match and return the cleanup token without attaching it anywhere.
    /**
     * Consumes the remainder of the current line on error and returns the raw
     * token, leaving disposal to the caller.
     *
     * @return the raw error token, or {@code null} if nothing to clean up
     */
    private LdifToken cleanupLine()
    {
        LdifToken errorToken = scanner.matchCleanupLine();
        return errorToken;
    }


    // ── Null-Safe Token Value Extraction ─────────────────────────────────────
    // Model constructors expect {@code null} for missing segments rather than
    // a NullPointerException from calling getValue() on a missing token.
    // We return null when the token is null, or its value string otherwise.
    /**
     * Returns the value string of {@code token}, or {@code null} if
     * {@code token} is {@code null}.
     *
     * @param token  the token to extract a value from
     * @return the token's value, or {@code null}
     */
    private static String getValueOrNull( LdifToken token )
    {
        return token == null ? null : token.getValue();
    }

}
