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


import java.util.List;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.ProjectType;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;


// ── CLASS: ProjectsExporter — Yoda Lifting Projects from the Swamp ────────────
// Projects sitting in Java memory are like Luke's X-wing in the Dagobah swamp:
// perfectly real but in an unusable form.  Yoda concentrates and transforms
// the sunken ship into a flyable one.  We transform Project objects — with all
// their connection details, schema connectors, and schema data — into a Dom4J
// Document that can be serialised as XML and saved to disk or sent elsewhere.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Converts one or more {@link Project} objects into Dom4J {@link Document}
 * representations, ready to be serialised as XML.
 * We export the project's name, type, optional connection ID and schema-connector ID,
 * plus the full schema payload (via {@link XMLSchemaFileExporter}).
 * Think of this class as Yoda: it lifts the project out of the Java object swamp
 * and into the XML form the rest of the world can work with.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ProjectsExporter
{
    // The tags
    private static final String PROJECT_TAG = "project"; //$NON-NLS-1$
    private static final String PROJECTS_TAG = "projects"; //$NON-NLS-1$
    private static final String NAME_TAG = "name"; //$NON-NLS-1$
    private static final String TYPE_TAG = "type"; //$NON-NLS-1$
    private static final String CONNECTION_TAG = "connection"; //$NON-NLS-1$
    private static final String SCHEMA_CONNECTOR_TAG = "schemaConnector"; //$NON-NLS-1$
    private static final String SCHEMA_BACKUP_TAG = "schemaBackup"; //$NON-NLS-1$


    // ── Yoda Lifts a Single X-Wing from the Swamp ─────────────────────────────
    // One ship, one focused effort — Yoda raises a single project from the Java
    // object swamp into a ready-to-fly XML Document.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts a single {@link Project} to a Dom4J Document with a {@code <project>} root.
     * The resulting document can be serialised directly to a {@code .lsd} project file.
     *
     * @param project  the project to convert — must not be null
     * @return         a Dom4J Document representing the project
     */
    public static Document toDocument( Project project )
    {
        // Creating the Document
        Document document = DocumentHelper.createDocument();

        // Adding the project
        addProject( project, document );

        return document;
    }


    // ── Yoda Lifts an Entire Squadron from the Swamp ─────────────────────────
    // Multiple X-wings, one after another — Yoda raises them all, nesting them
    // under a {@code <projects>} wrapper so they travel together as a fleet.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts an array of {@link Project} objects to a Dom4J Document with a
     * {@code <projects>} root element containing one {@code <project>} child per project.
     * Used when the user exports multiple projects in a single batch operation.
     *
     * @param projects  the projects to convert — may be null or empty
     * @return          a Dom4J Document containing all the projects
     */
    public static Document toDocument( Project[] projects )
    {
        // Creating the Document
        Document document = DocumentHelper.createDocument();
        Element projectsElement = document.addElement( PROJECTS_TAG );

        if ( projects != null )
        {
            for ( Project project : projects )
            {
                addProject( project, projectsElement );
            }
        }

        return document;
    }


    // ── Yoda Examines Each Ship and Records What It Carries ──────────────────
    // Yoda doesn't just lift each X-wing blindly — he inspects its type (OFFLINE
    // or ONLINE), logs the pilot's name, records the connection ID if relevant,
    // and notes the full schema payload before raising it clear.
    // We add name, type, and (for ONLINE projects) connection and schema-connector
    // attributes, plus the full schema XML, to the branch element.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the XML representation of a single project as a child of the given DOM branch.
     * For ONLINE projects we also emit the connection ID, schema-connector ID, and a
     * schema backup snapshot.  All project types emit their full schema payload.
     *
     * @param project  the project to serialise — must not be null
     * @param branch   the DOM branch (Document or Element) to add the project element to
     */
    private static void addProject( Project project, Branch branch )
    {
        Element element = branch.addElement( PROJECT_TAG );

        if ( project != null )
        {
            // Name
            String name = project.getName();
            if ( ( name != null ) && ( !name.equals( "" ) ) ) //$NON-NLS-1$
            {
                element.addAttribute( NAME_TAG, name );
            }

            // Type
            ProjectType type = project.getType();
            if ( type != null )
            {
                element.addAttribute( TYPE_TAG, type.toString() );
            }

            // If project is an Online Schema Project
            if ( type.equals( ProjectType.ONLINE ) )
            {
                // Connection ID
                Connection connection = project.getConnection();

                if ( connection != null )
                {
                    element.addAttribute( CONNECTION_TAG, connection.getId() );
                }

                // Schema Connection ID
                SchemaConnector schemaConnector = project.getSchemaConnector();

                if ( schemaConnector != null )
                {
                    element.addAttribute( SCHEMA_CONNECTOR_TAG, project.getSchemaConnector().getId() );
                }

                // Schema Backup
                Element schemaBackupElement = element.addElement( SCHEMA_BACKUP_TAG );
                List<Schema> backupSchemas = project.getInitialSchema();
                if ( backupSchemas != null )
                {
                    XMLSchemaFileExporter.addSchemas( backupSchemas.toArray( new Schema[0] ), schemaBackupElement );
                }

            }

            // Schemas
            XMLSchemaFileExporter
                .addSchemas( project.getSchemaHandler().getSchemas().toArray( new Schema[0] ), element );
        }
    }
}
