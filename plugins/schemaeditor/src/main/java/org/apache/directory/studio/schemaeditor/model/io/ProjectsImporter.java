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


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.ProjectType;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.osgi.util.NLS;


// ── CLASS: ProjectsImporter — C-3PO Reading a Jawa Project File ───────────────
// The project file is another alien dialect: XML with its own vocabulary of
// <project>, <schemas>, <connection> elements.  C-3PO reads the scroll, decodes
// every element, and assembles a proper Project object the rest of the tool
// understands.  If the scroll is malformed he reports exactly what dialect
// error he encountered so the user knows how to fix it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Reads project XML files (single {@code <project>} or multi-project
 * {@code <projects>}) from an {@link InputStream} and assembles {@link Project}
 * objects, including their schemas, connection references, and schema connectors.
 * Think of this class as C-3PO translating a project-file dialect into the
 * Java objects the schema editor's model understands.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ProjectsImporter
{
    // The tags
    private static final String PROJECT_TAG = "project"; //$NON-NLS-1$
    private static final String PROJECTS_TAG = "projects"; //$NON-NLS-1$
    private static final String NAME_TAG = "name"; //$NON-NLS-1$
    private static final String SCHEMAS_TAG = "schemas"; //$NON-NLS-1$
    private static final String TYPE_TAG = "type"; //$NON-NLS-1$
    private static final String CONNECTION_TAG = "connection"; //$NON-NLS-1$
    private static final String SCHEMA_CONNECTOR_TAG = "schemaConnector"; //$NON-NLS-1$
    private static final String SCHEMA_BACKUP_TAG = "schemaBackup"; //$NON-NLS-1$


    // ── C-3PO Reads a Single-Project Scroll ──────────────────────────────────
    // The scroll has one <project> root element; C-3PO reads it, decodes every
    // attribute and child element, and hands back a single Project object.
    // If the scroll's structure doesn't match what we expect, C-3PO throws
    // a translation error with the file path so the user knows which scroll failed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads a single-project XML file from the given stream and returns a
     * populated {@link Project}.
     * The root element must be {@code <project>}; anything else raises an exception.
     *
     * @param inputStream  the stream to read — must not be null
     * @param path         the file path, used in error messages
     * @return             the populated Project — never null
     * @throws ProjectsImportException  if the file can't be read or isn't a valid project file
     */
    public static Project getProject( InputStream inputStream, String path ) throws ProjectsImportException
    {
        Project project = new Project();

        SAXReader reader = new SAXReader();
        Document document = null;
        try
        {
            document = reader.read( inputStream );
        }
        catch ( DocumentException e )
        {
            throw new ProjectsImportException( NLS.bind( Messages.getString( "ProjectsImporter.NotReadCorrectly" ), //$NON-NLS-1$
                new String[]
                    { path } ) );
        }

        Element rootElement = document.getRootElement();
        if ( !rootElement.getName().equals( PROJECT_TAG ) )
        {
            throw new ProjectsImportException( NLS.bind( Messages.getString( "ProjectsImporter.NotValidProject" ), //$NON-NLS-1$
                new String[]
                    { path } ) );
        }

        readProject( rootElement, project, path );

        return project;
    }


    // ── C-3PO Reads a Multi-Project Scroll ───────────────────────────────────
    // The scroll has a <projects> root wrapping several <project> children;
    // C-3PO iterates over each child element and translates it into a Project.
    // Any one child that fails to decode causes the whole batch to fail.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads a multi-project XML file from the given stream and returns an
     * array of populated {@link Project} objects.
     * The root element must be {@code <projects>} containing one or more
     * {@code <project>} children.
     *
     * @param inputStream  the stream to read — must not be null
     * @param path         the file path, used in error messages
     * @return             an array of Project objects — never null, may be empty
     * @throws ProjectsImportException  if the file can't be read or isn't a valid projects file
     */
    public static Project[] getProjects( InputStream inputStream, String path ) throws ProjectsImportException
    {
        List<Project> projects = new ArrayList<Project>();

        SAXReader reader = new SAXReader();
        Document document = null;
        try
        {
            document = reader.read( inputStream );
        }
        catch ( DocumentException e )
        {
            PluginUtils.logError( NLS.bind( Messages.getString( "ProjectsImporter.NotReadCorrectly" ), new String[] //$NON-NLS-1$
                { path } ), e );
            throw new ProjectsImportException( NLS.bind( Messages.getString( "ProjectsImporter.NotReadCorrectly" ), //$NON-NLS-1$
                new String[]
                    { path } ) );
        }

        Element rootElement = document.getRootElement();
        if ( !rootElement.getName().equals( PROJECTS_TAG ) )
        {
            throw new ProjectsImportException( NLS.bind( Messages.getString( "ProjectsImporter.NotValidProject" ), //$NON-NLS-1$
                new String[]
                    { path } ) );
        }

        for ( Iterator<?> i = rootElement.elementIterator( PROJECT_TAG ); i.hasNext(); )
        {
            Element projectElement = ( Element ) i.next();
            Project project = new Project();
            readProject( projectElement, project, path );
            projects.add( project );
        }

        return projects.toArray( new Project[0] );
    }


    // ── C-3PO Decodes a Single Project Element ───────────────────────────────
    // C-3PO reads each field on the project element: name, type, connection,
    // schema-connector, schema backup, and the full schema list.  For ONLINE
    // projects there are extra fields to decode.  Any unknown value for the
    // type field causes an immediate translation error.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates the given {@link Project} from the given DOM element.
     * Reads name, type, and (for ONLINE projects) connection ID, schema-connector ID,
     * schema backup, and the current schema list.
     *
     * @param element  the {@code <project>} DOM element to decode
     * @param project  the Project to populate
     * @param path     the file path, used in error messages
     * @throws ProjectsImportException  if any value can't be decoded or a reference can't be resolved
     */
    private static void readProject( Element element, Project project, String path ) throws ProjectsImportException
    {
        // Name
        Attribute nameAttribute = element.attribute( NAME_TAG );
        if ( ( nameAttribute != null ) && ( !nameAttribute.getValue().equals( "" ) ) ) //$NON-NLS-1$
        {
            project.setName( nameAttribute.getValue() );
        }

        // Type
        Attribute typeAttribute = element.attribute( TYPE_TAG );
        if ( ( typeAttribute != null ) && ( !typeAttribute.getValue().equals( "" ) ) ) //$NON-NLS-1$
        {
            try
            {
                project.setType( ProjectType.valueOf( typeAttribute.getValue() ) );
            }
            catch ( IllegalArgumentException e )
            {
                throw new ProjectsImportException( Messages.getString( "ProjectsImporter.NotConvertableValue" ) ); //$NON-NLS-1$
            }
        }

        // If project is an Online Schema Project
        if ( project.getType().equals( ProjectType.ONLINE ) )
        {
            // Connection
            Attribute connectionAttribute = element.attribute( CONNECTION_TAG );
            if ( ( connectionAttribute != null ) && ( !connectionAttribute.getValue().equals( "" ) ) ) //$NON-NLS-1$
            {
                project.setConnection( PluginUtils.getConnection( connectionAttribute.getValue() ) );
            }

            // Schema Connector
            Attribute schemaConnectorAttribute = element.attribute( SCHEMA_CONNECTOR_TAG );
            if ( ( schemaConnectorAttribute != null ) && ( !schemaConnectorAttribute.getValue().equals( "" ) ) ) //$NON-NLS-1$
            {
                String schemaConnectorId = schemaConnectorAttribute.getValue();

                SchemaConnector schemaConnector = null;
                List<SchemaConnector> schemaConnectors = PluginUtils.getSchemaConnectors();
                for ( SchemaConnector sc : schemaConnectors )
                {
                    if ( sc.getId().equalsIgnoreCase( schemaConnectorId ) )
                    {
                        schemaConnector = sc;
                    }
                }

                if ( schemaConnector == null )
                {
                    throw new ProjectsImportException( NLS.bind( Messages
                        .getString( "ProjectsImporter.NoSchemaConnectorIDFound" ), new String[] //$NON-NLS-1$
                        { schemaConnectorId } ) ); //$NON-NLS-1$
                }

                project.setSchemaConnector( schemaConnector );
            }

            // SchemaBackup
            Element schemaBackupElement = element.element( SCHEMA_BACKUP_TAG );
            if ( schemaBackupElement != null )
            {
                Element schemasElement = schemaBackupElement.element( SCHEMAS_TAG );
                if ( schemasElement != null )
                {
                    Schema[] schemas = null;
                    try
                    {
                        schemas = XMLSchemaFileImporter.readSchemas( schemasElement, path );
                        for ( Schema schema : schemas )
                        {
                            schema.setProject( project );
                        }
                    }
                    catch ( XMLSchemaFileImportException e )
                    {
                        throw new ProjectsImportException( Messages.getString( "ProjectsImporter.NotConvertableSchema" ) ); //$NON-NLS-1$
                    }

                    project.setInitialSchema( Arrays.asList( schemas ) );
                }
            }
        }

        // Schemas
        Element schemasElement = element.element( SCHEMAS_TAG );
        if ( schemasElement != null )
        {
            Schema[] schemas = null;
            try
            {
                schemas = XMLSchemaFileImporter.readSchemas( schemasElement, path );
            }
            catch ( XMLSchemaFileImportException e )
            {
                throw new ProjectsImportException( Messages.getString( "ProjectsImporter.NotConvertableSchema" ) ); //$NON-NLS-1$
            }
            for ( Schema schema : schemas )
            {
                schema.setProject( project );
                project.getSchemaHandler().addSchema( schema );
            }
        }
    }

    /**
     * This enum represents the different types of project files.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public enum ProjectFileType
    {
        SINGLE, MULTIPLE
    }


    // ── C-3PO Checks Whether the Scroll Is Single or Multi-Project ───────────
    // Before C-3PO starts translating he peeks at the root element of the scroll
    // to decide whether it holds one project or several, then returns the
    // appropriate enum so the caller knows which read method to use.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Determines whether the XML file at the given stream is a single-project
     * or multi-project file by inspecting its root element name.
     * Use the returned {@link ProjectFileType} to decide whether to call
     * {@link #getProject} or {@link #getProjects}.
     *
     * @param inputStream  the stream to read — consumed by this call
     * @param path         the file path, used in error messages
     * @return             {@code SINGLE} if the root is {@code <project>}, {@code MULTIPLE} if {@code <projects>}
     * @throws ProjectsImportException  if the file can't be read or has an unrecognised root element
     */
    public static ProjectFileType getProjectFileType( InputStream inputStream, String path )
        throws ProjectsImportException
    {
        SAXReader reader = new SAXReader();
        Document document = null;
        try
        {
            document = reader.read( inputStream );
        }
        catch ( DocumentException e )
        {
            throw new ProjectsImportException( NLS.bind( Messages.getString( "ProjectsImporter.NotReadCorrectly" ), //$NON-NLS-1$
                new String[]
                    { path } ) );
        }

        Element rootElement = document.getRootElement();
        if ( rootElement.getName().equals( PROJECT_TAG ) )
        {
            return ProjectFileType.SINGLE;
        }
        else if ( rootElement.getName().equals( PROJECTS_TAG ) )
        {
            return ProjectFileType.MULTIPLE;
        }
        else
        {
            throw new ProjectsImportException( NLS.bind( Messages.getString( "ProjectsImporter.NotValidProject" ), //$NON-NLS-1$
                new String[]
                    { path } ) );
        }
    }
}
