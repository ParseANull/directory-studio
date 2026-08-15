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
package org.apache.directory.studio.schemaeditor.model.io;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;

import org.apache.directory.api.ldap.model.exception.LdapSchemaException;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.parsers.OpenLdapSchemaParser;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.eclipse.osgi.util.NLS;


// ── CLASS: OpenLdapSchemaFileImporter — C-3PO Reading the Jawa Dialect ────────
// The Jawas are speaking their own language — OpenLDAP .schema file syntax —
// and C-3PO has to decode every line and turn it into something the rebel fleet
// (the schema model) can actually work with.  He reads the file, hands it to the
// OpenLdapSchemaParser, and then converts every parsed literal object into the
// proper implementation type the rest of the tool understands.  If the dialect
// contains a syntax error, C-3PO pinpoints the line and column so we can tell
// the user exactly where the Jawa went wrong.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Parses an OpenLDAP {@code .schema} file from an {@link InputStream} and converts
 * it into a {@link Schema} object the schema editor can work with.
 * We use the Apache Directory API's {@link OpenLdapSchemaParser} for the heavy
 * lifting, then convert the parsed literal objects into our own AttributeType and
 * ObjectClass implementations.  Parse errors are wrapped with line/column details.
 * Think of this class as C-3PO translating a Jawa dialect: he decodes the foreign
 * syntax and produces something the rest of the team understands.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapSchemaFileImporter
{
    // ── C-3PO Reads the Jawa Scroll and Translates It ────────────────────────
    // The Jawas hand C-3PO a scroll written in their schema dialect; he reads
    // it cover to cover, translates every attributeType and objectClass entry,
    // and hands back a proper Schema object the rebels can use.
    // If the scroll has a syntax error C-3PO figures out exactly which line
    // and column the mistake is on and reports it clearly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Parses an OpenLDAP {@code .schema} file and returns the corresponding Schema.
     * The schema name is derived from the file path (the filename without extension).
     * Parse errors are wrapped in an {@link OpenLdapSchemaFileImportException}
     * that includes line/column information when available.
     *
     * @param inputStream  the stream to read the .schema file from — must not be null
     * @param path         the filesystem path of the file, used for the schema name and error messages
     * @return             a populated Schema — never null
     * @throws OpenLdapSchemaFileImportException  if the file cannot be read or parsed
     */
    public static Schema getSchema( InputStream inputStream, String path ) throws OpenLdapSchemaFileImportException
    {
        OpenLdapSchemaParser parser = new OpenLdapSchemaParser();

        try
        {
            parser.parse( inputStream );
        }
        catch ( IOException e )
        {
            throw new OpenLdapSchemaFileImportException( NLS.bind( Messages
                .getString( "OpenLdapSchemaFileImporter.NotReadCorrectly" ), new String[] { path } ), e ); //$NON-NLS-1$
        }
        catch ( ParseException | LdapSchemaException e )
        {
            ExceptionMessage exceptionMessage = parseExceptionMessage( e.getMessage() );
            throw new OpenLdapSchemaFileImportException( NLS.bind( Messages
                .getString( "OpenLdapSchemaFileImporter.NotReadCorrectly" ), new String[] //$NON-NLS-1$
                { path } )
                + ( exceptionMessage == null ? "" : NLS.bind( Messages //$NON-NLS-1$
                    .getString( "OpenLdapSchemaFileImporter.ErrorMessage" ), new String[] //$NON-NLS-1$
                    { exceptionMessage.lineNumber, exceptionMessage.columnNumber, exceptionMessage.cause } ) ), e );
        }

        String schemaName = getNameFromPath( path );

        Schema schema = new Schema( schemaName );

        List<?> ats = parser.getAttributeTypes();
        for ( int i = 0; i < ats.size(); i++ )
        {
            AttributeType at = convertAttributeType( ( AttributeType ) ats.get( i ) );
            at.setSchemaName( schemaName );
            schema.addAttributeType( at );
        }

        List<?> ocs = parser.getObjectClasses();
        for ( int i = 0; i < ocs.size(); i++ )
        {
            ObjectClass oc = convertObjectClass( ( ObjectClass ) ocs.get( i ) );
            oc.setSchemaName( schemaName );
            schema.addObjectClass( oc );
        }

        return schema;
    }


    // ── C-3PO Reads the Title from the Scroll's Cover ────────────────────────
    // Every Jawa scroll has a filename; C-3PO strips the ".schema" suffix to get
    // the plain schema name he'll use to label the translated document.
    // If the file doesn't end in ".schema" he just uses the whole filename as-is.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Derives the schema name from the file path by stripping the {@code .schema}
     * extension if present, or using the full filename otherwise.
     *
     * @param path  the filesystem path of the schema file
     * @return      the schema name — never null
     */
    private static String getNameFromPath( String path )
    {
        File file = new File( path );
        String fileName = file.getName();
        if ( fileName.endsWith( ".schema" ) ) //$NON-NLS-1$
        {
            String[] fileNameSplitted = fileName.split( "\\." ); //$NON-NLS-1$
            return fileNameSplitted[0];
        }

        return fileName;
    }


    // ── C-3PO Rewrites a Jawa Attribute-Type Entry in Standard Basic ──────────
    // The parser gives us a rough literal object; C-3PO transcribes every
    // field onto a fresh, properly-typed AttributeType object the model understands.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts a parser-produced {@link AttributeType} literal into the proper
     * implementation used throughout the schema editor.
     * Every property is copied across; this step exists because the parser's
     * output type and our model type are separate class hierarchies.
     *
     * @param at  the literal attribute type from the parser — must not be null
     * @return    a fully-populated AttributeType suitable for the schema model
     */
    private static AttributeType convertAttributeType( AttributeType at )
    {
        AttributeType newAT = new AttributeType( at.getOid() );
        newAT.setNames( at.getNames() );
        newAT.setDescription( at.getDescription() );
        newAT.setSuperiorOid( at.getSuperiorOid() );
        newAT.setUsage( at.getUsage() );
        newAT.setSyntaxOid( at.getSyntaxOid() );
        newAT.setSyntaxLength( at.getSyntaxLength() );
        newAT.setObsolete( at.isObsolete() );
        newAT.setSingleValued( at.isSingleValued() );
        newAT.setCollective( at.isCollective() );
        newAT.setUserModifiable( at.isUserModifiable() );
        newAT.setEqualityOid( at.getEqualityOid() );
        newAT.setOrderingOid( at.getOrderingOid() );
        newAT.setSubstringOid( at.getSubstringOid() );

        return newAT;
    }


    // ── C-3PO Rewrites a Jawa Object-Class Entry in Standard Basic ───────────
    // Same translation job, this time for object class literals — all the
    // superiors, must/may lists, and type flags get copied to a fresh instance.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts a parser-produced {@link ObjectClass} literal into the proper
     * implementation used throughout the schema editor.
     *
     * @param oc  the literal object class from the parser — must not be null
     * @return    a fully-populated ObjectClass suitable for the schema model
     */
    private static ObjectClass convertObjectClass( ObjectClass oc )
    {
        ObjectClass newOC = new ObjectClass( oc.getOid() );
        newOC.setNames( oc.getNames() );
        newOC.setDescription( oc.getDescription() );
        newOC.setSuperiorOids( oc.getSuperiorOids() );
        newOC.setType( oc.getType() );
        newOC.setObsolete( oc.isObsolete() );
        newOC.setMustAttributeTypeOids( oc.getMustAttributeTypeOids() );
        newOC.setMayAttributeTypeOids( oc.getMayAttributeTypeOids() );

        return newOC;
    }


    // ── C-3PO Decodes the Exact Line in the Scroll Where the Error Is ────────
    // When a Jawa scroll has a typo, C-3PO uses a regex to find the "line X:Y"
    // marker in the parser's error message and extracts the line number, column,
    // and cause so the user knows exactly where to look.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Parses the parser's exception message to extract line number, column number,
     * and a human-readable cause description.
     * Returns null if the message doesn't match the expected "line N:M: cause" format.
     *
     * @param message  the exception message to scan — must not be null
     * @return         an {@link ExceptionMessage} with the extracted details, or null
     */
    private static ExceptionMessage parseExceptionMessage( String message )
    {
        Scanner scanner = new Scanner( new ByteArrayInputStream( message.getBytes() ) );
        String foundString = scanner.findWithinHorizon( ".*line (\\d+):(\\d+): *([^\\n]*).*", message.length() ); //$NON-NLS-1$
        if ( foundString != null )
        {
            MatchResult result = scanner.match();
            if ( result.groupCount() == 3 )
            {
                ExceptionMessage exceptionMessage = new ExceptionMessage();
                exceptionMessage.lineNumber = result.group( 1 );
                exceptionMessage.columnNumber = result.group( 2 );
                exceptionMessage.cause = result.group( 3 );

                scanner.close();
                return exceptionMessage;
            }

        }

        scanner.close();
        return null;
    }

    private static class ExceptionMessage
    {
        String lineNumber;
        String columnNumber;
        String cause;
    }
}
