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
package org.apache.directory.studio.scim.core.model;


import java.util.List;
import java.util.Map;


public class ScimResource
{

    private String id;
    private String externalId;
    private List<String> schemas;
    private ScimMeta meta;
    private Map<String, Object> attributes;


    public String getId()
    {
        return id;
    }


    public void setId( String id )
    {
        this.id = id;
    }


    public String getExternalId()
    {
        return externalId;
    }


    public void setExternalId( String externalId )
    {
        this.externalId = externalId;
    }


    public List<String> getSchemas()
    {
        return schemas;
    }


    public void setSchemas( List<String> schemas )
    {
        this.schemas = schemas;
    }


    public ScimMeta getMeta()
    {
        return meta;
    }


    public void setMeta( ScimMeta meta )
    {
        this.meta = meta;
    }


    public Map<String, Object> getAttributes()
    {
        return attributes;
    }


    public void setAttributes( Map<String, Object> attributes )
    {
        this.attributes = attributes;
    }


    public static class ScimMeta
    {

        private String resourceType;
        private String created;
        private String lastModified;
        private String location;
        private String version;


        public String getResourceType()
        {
            return resourceType;
        }


        public void setResourceType( String resourceType )
        {
            this.resourceType = resourceType;
        }


        public String getCreated()
        {
            return created;
        }


        public void setCreated( String created )
        {
            this.created = created;
        }


        public String getLastModified()
        {
            return lastModified;
        }


        public void setLastModified( String lastModified )
        {
            this.lastModified = lastModified;
        }


        public String getLocation()
        {
            return location;
        }


        public void setLocation( String location )
        {
            this.location = location;
        }


        public String getVersion()
        {
            return version;
        }


        public void setVersion( String version )
        {
            this.version = version;
        }

    }

}
