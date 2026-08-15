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
package org.apache.directory.studio.scim.core.schema;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.directory.studio.scim.core.ScimCorePlugin;
import org.apache.directory.studio.scim.core.model.ScimSchema;
import org.eclipse.core.runtime.Platform;


public class ScimSchemaRepository
{

    // keyed by connectionId -> (schemaId -> ScimSchema)
    private final Map<String, Map<String, ScimSchema>> schemas = new HashMap<>();


    public List<ScimSchema> getSchemas( String connectionId )
    {
        Map<String, ScimSchema> byId = schemas.get( connectionId );
        if ( byId == null )
        {
            return Collections.emptyList();
        }
        return new ArrayList<>( byId.values() );
    }


    // TODO: implement persistence once scim.client's Jackson dependency is available.
    // State location: Platform.getStateLocation(ScimCorePlugin.getDefault().getBundle())
    public void loadSchemas( String connectionId )
    {
        throw new UnsupportedOperationException( "Schema persistence not yet implemented" );
    }


    // TODO: implement persistence once scim.client's Jackson dependency is available.
    public void saveSchema( String connectionId, ScimSchema schema, String source, String endpoint )
    {
        throw new UnsupportedOperationException( "Schema persistence not yet implemented" );
    }


    // TODO: implement once scim.client's Jackson dependency is available.
    public void exportSchemas( String connectionId, OutputStream out )
    {
        throw new UnsupportedOperationException( "Schema export not yet implemented" );
    }


    // TODO: implement once scim.client's Jackson dependency is available.
    public void importSchemas( InputStream in )
    {
        throw new UnsupportedOperationException( "Schema import not yet implemented" );
    }

}
