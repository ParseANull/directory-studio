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
package org.apache.directory.studio.ldapbrowser.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;

// ── CLASS: SchemaObjectLoader — R2-D2 CACHING THE JEDI ARCHIVES INDEXES ──────
// R2-D2 pre-downloads the Jedi Archives indexes into two flat arrays so that
// auto-completion widgets don't have to re-scan the schema on every keystroke.
// SchemaObjectLoader lazily builds sorted arrays of object class names+OIDs
// and attribute type names+OIDs, falling back to all known connections or the
// default schema when no specific BrowserConnection is set.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Lazily builds and caches sorted arrays of schema object names and OIDs for
 * use by auto-completion widgets.  Covers both object classes and attribute
 * types, drawing from a specific {@link IBrowserConnection} or from all known
 * connections when none is set.
 *
 * <p>Think of this as R2-D2 caching the Jedi Archives indexes: the first
 * query is slow, every subsequent query is instant.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaObjectLoader
{
    /** The browser connection */
    private IBrowserConnection browserConnection;

    /** The array of attributes names and OIDs */
    private String[] attributeNamesAndOids;

    /** The array of ObjectClasses and OIDs */
    private String[] objectClassesAndOids;

    /**
     * An interface to allow the getSchemaObjectNamesAndOid() to be called for any schema object
     */
    private interface SchemaAdder 
    {
        /**
         * Adds the schema object names and OIDs to the given set.
         *
         * @param schema the schema
         * @param schemaObjectNamesList the schema object names list
         * @param oidsList the OIDs name list
         */
        void add( Schema schema, List<String> schemaObjectNamesList, List<String> oidsList );
    }
    
    
    // ── R2-D2 Returns The Cached Object Class Names And OIDs Index ────────────────
    // If the index array is null or empty, R2-D2 rebuilds it from the schema.
    // Names and OIDs are collected separately, de-duplicated, sorted, and merged.
    // The DEFAULT_SCHEMA is always appended after any connection-specific schemas.
    // Returns a flat array: all sorted names first, then all sorted OIDs.
    /**
     * Gets the array containing the object class names and OIDs.
     * The array is built lazily and cached; subsequent calls return the
     * cached result.
     *
     * @return the sorted array of object class names and OIDs
     */
    public String[] getObjectClassNamesAndOids()
    {
        objectClassesAndOids = getSchemaObjectsAnddOids( objectClassesAndOids, new SchemaAdder()
        {
            @Override
            public void add( Schema schema, List<String> objectClassNamesList, List<String> oidsList )
            {
                if ( schema != null )
                {
                    for ( ObjectClass ocd : schema.getObjectClassDescriptions() )
                    {
                        // OID
                        if ( !oidsList.contains( ocd.getOid() ) )
                        {
                            oidsList.add( ocd.getOid() );
                        }

                        // Names
                        for ( String name : ocd.getNames() )
                        {
                            if ( !objectClassNamesList.contains( name ) )
                            {
                                objectClassNamesList.add( name );
                            }
                        }
                    }
                }
            }
        });
        
        return objectClassesAndOids;
    }

    // ── R2-D2 Returns The Cached Attribute Type Names And OIDs Index ─────────────
    // If the index array is null or empty, R2-D2 rebuilds it from the schema.
    // Attribute type names and OIDs are collected, de-duplicated, sorted, merged.
    // Follows the same fallback logic as getObjectClassNamesAndOids.
    // Returns a flat array: all sorted names first, then all sorted OIDs.
    /**
     * Gets the array containing the attribute type names and OIDs.
     * The array is built lazily and cached; subsequent calls return the
     * cached result.
     *
     * @return the sorted array of attribute type names and OIDs
     */
    public String[] getAttributeNamesAndOids()
    {
        attributeNamesAndOids = getSchemaObjectsAnddOids( attributeNamesAndOids, new SchemaAdder()
        {
            @Override
            public void add( Schema schema, List<String> attributeNamesList, List<String> oidsList )
            {
                if ( schema != null )
                {
                    for ( AttributeType atd : schema.getAttributeTypeDescriptions() )
                    {
                        // OID
                        if ( !oidsList.contains( atd.getOid() ) )
                        {
                            oidsList.add( atd.getOid() );
                        }

                        // Names
                        for ( String name : atd.getNames() )
                        {
                            if ( !attributeNamesList.contains( name ) )
                            {
                                attributeNamesList.add( name );
                            }
                        }
                    }
                }
            }
        });
        
        return attributeNamesAndOids;
    }

    
    // ── R2-D2 Builds And Caches A Schema Index Using A Provided Adder Strategy ────
    // If the input array is already populated, R2-D2 returns it unchanged (cache hit).
    // Otherwise he gathers names and OIDs via the SchemaAdder, sorts both lists,
    // then merges them into one flat array (names first, OIDs after).
    // Falls back to all connections when no browserConnection is set.
    private String[] getSchemaObjectsAnddOids( String[] schemaObjects, SchemaAdder schemaAdder )
    {
        // Checking if the array has already be generated
        if ( ( schemaObjects == null ) || ( schemaObjects.length == 0 ) )
        {
            List<String> schemaObjectNamesList = new ArrayList<String>();
            List<String> oidsList = new ArrayList<String>();

            if ( browserConnection == null )
            {
                // Getting all connections in the case where no connection is found
                IBrowserConnection[] connections = BrowserCorePlugin.getDefault().getConnectionManager()
                    .getBrowserConnections();
                
                for ( IBrowserConnection connection : connections )
                {
                    schemaAdder.add( connection.getSchema(), schemaObjectNamesList, oidsList );
                }
            }
            else
            {
                // Only adding schema object names and OIDs from the associated connection
                schemaAdder.add( browserConnection.getSchema(), schemaObjectNamesList, oidsList );
            }

            // Also adding schemaObject names and OIDs from the default schema
            schemaAdder.add( Schema.DEFAULT_SCHEMA, schemaObjectNamesList, oidsList );

            // Sorting the set
            Collections.sort( schemaObjectNamesList );
            Collections.sort( oidsList );

            schemaObjects = new String[schemaObjectNamesList.size() + oidsList.size()];
            System.arraycopy( schemaObjectNamesList.toArray(), 0, schemaObjects, 0, schemaObjectNamesList
                .size() );
            System.arraycopy( oidsList.toArray(), 0, schemaObjects, schemaObjectNamesList
                .size(), oidsList.size() );
        }

        return schemaObjects;
    }
}
