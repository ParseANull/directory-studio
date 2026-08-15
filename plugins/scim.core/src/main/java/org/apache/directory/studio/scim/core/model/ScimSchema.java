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


public class ScimSchema
{

    private String id;
    private String name;
    private String description;
    private List<ScimSchemaAttribute> attributes;


    public String getId()
    {
        return id;
    }


    public void setId( String id )
    {
        this.id = id;
    }


    public String getName()
    {
        return name;
    }


    public void setName( String name )
    {
        this.name = name;
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription( String description )
    {
        this.description = description;
    }


    public List<ScimSchemaAttribute> getAttributes()
    {
        return attributes;
    }


    public void setAttributes( List<ScimSchemaAttribute> attributes )
    {
        this.attributes = attributes;
    }


    public static class ScimSchemaAttribute
    {

        private String name;
        private String type;
        private boolean multiValued;
        private boolean required;
        private String description;


        public String getName()
        {
            return name;
        }


        public void setName( String name )
        {
            this.name = name;
        }


        public String getType()
        {
            return type;
        }


        public void setType( String type )
        {
            this.type = type;
        }


        public boolean isMultiValued()
        {
            return multiValued;
        }


        public void setMultiValued( boolean multiValued )
        {
            this.multiValued = multiValued;
        }


        public boolean isRequired()
        {
            return required;
        }


        public void setRequired( boolean required )
        {
            this.required = required;
        }


        public String getDescription()
        {
            return description;
        }


        public void setDescription( String description )
        {
            this.description = description;
        }

    }

}
