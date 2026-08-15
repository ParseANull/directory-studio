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
package org.apache.directory.studio.scim.ui.model;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeDescription;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;


public class ScimAttributeEntry implements IAttribute
{
    private static final long serialVersionUID = 1L;

    private final String name;
    private final List<IValue> values = new ArrayList<>();
    private final IEntry entry;


    public ScimAttributeEntry( String name, IEntry entry )
    {
        this.name = name;
        this.entry = entry;
    }


    @Override
    public IEntry getEntry()
    {
        return entry;
    }


    @Override
    public String getDescription()
    {
        return name;
    }


    @Override
    public String getType()
    {
        int semi = name.indexOf( ';' );
        return semi >= 0 ? name.substring( 0, semi ) : name;
    }


    @Override
    public IValue[] getValues()
    {
        return values.toArray( new IValue[0] );
    }


    @Override
    public int getValueSize()
    {
        return values.size();
    }


    @Override
    public void addValue( IValue valueToAdd ) throws IllegalArgumentException
    {
        values.add( valueToAdd );
    }


    @Override
    public void deleteValue( IValue valueToDelete ) throws IllegalArgumentException
    {
        values.remove( valueToDelete );
    }


    @Override
    public void modifyValue( IValue oldValue, IValue newValue ) throws IllegalArgumentException
    {
        int idx = values.indexOf( oldValue );
        if ( idx >= 0 )
        {
            values.set( idx, newValue );
        }
    }


    @Override
    public void addEmptyValue()
    {
    }


    @Override
    public void deleteEmptyValue()
    {
    }


    @Override
    public boolean isConsistent()
    {
        return !values.isEmpty();
    }


    @Override
    public boolean isMustAttribute()
    {
        return false;
    }


    @Override
    public boolean isMayAttribute()
    {
        return true;
    }


    @Override
    public boolean isOperationalAttribute()
    {
        return false;
    }


    @Override
    public boolean isObjectClassAttribute()
    {
        return "objectClass".equalsIgnoreCase( getType() );
    }


    @Override
    public boolean isString()
    {
        return true;
    }


    @Override
    public boolean isBinary()
    {
        return false;
    }


    @Override
    public byte[][] getBinaryValues()
    {
        byte[][] result = new byte[values.size()][];
        for ( int i = 0; i < values.size(); i++ )
        {
            String sv = values.get( i ).getStringValue();
            result[i] = sv != null ? sv.getBytes( java.nio.charset.StandardCharsets.UTF_8 ) : new byte[0];
        }
        return result;
    }


    @Override
    public String getStringValue()
    {
        if ( values.isEmpty() )
        {
            return null;
        }
        return values.get( 0 ).getStringValue();
    }


    @Override
    public String[] getStringValues()
    {
        String[] result = new String[values.size()];
        for ( int i = 0; i < values.size(); i++ )
        {
            result[i] = values.get( i ).getStringValue();
        }
        return result;
    }


    @Override
    public AttributeType getAttributeTypeDescription()
    {
        return null;
    }


    @Override
    public AttributeDescription getAttributeDescription()
    {
        return null;
    }


    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof IAttribute ) )
        {
            return false;
        }
        IAttribute other = ( IAttribute ) o;
        return entry.equals( other.getEntry() ) && name.equalsIgnoreCase( other.getDescription() );
    }


    @Override
    public int hashCode()
    {
        return 31 * entry.hashCode() + name.toLowerCase().hashCode();
    }


    @Override
    public <T> T getAdapter( Class<T> adapter )
    {
        return null;
    }
}
