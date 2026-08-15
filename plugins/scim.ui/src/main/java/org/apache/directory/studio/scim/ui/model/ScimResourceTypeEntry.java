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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.scim.core.model.ScimResourceType;


public class ScimResourceTypeEntry implements IEntry
{
    private static final long serialVersionUID = 1L;

    private final ScimBrowserConnection browserConnection;
    private final ScimRootEntry parent;
    private final ScimResourceType resourceType;
    private final List<IEntry> children = new ArrayList<>();
    private final List<IAttribute> attributes = new ArrayList<>();
    private boolean childrenInitialized;
    private boolean attributesInitialized;


    public ScimResourceTypeEntry( ScimBrowserConnection browserConnection, ScimRootEntry parent,
        ScimResourceType resourceType )
    {
        this.browserConnection = browserConnection;
        this.parent = parent;
        this.resourceType = resourceType;
    }


    public ScimResourceType getResourceType()
    {
        return resourceType;
    }


    @Override
    public Dn getDn()
    {
        try
        {
            return new Dn( "resourceType=" + resourceType.getName() );
        }
        catch ( LdapInvalidDnException e )
        {
            return Dn.EMPTY_DN;
        }
    }


    @Override
    public Rdn getRdn()
    {
        return getDn().getRdn();
    }


    @Override
    public IBrowserConnection getBrowserConnection()
    {
        return browserConnection;
    }


    @Override
    public IEntry getParententry()
    {
        return parent;
    }


    @Override
    public boolean hasParententry()
    {
        return parent != null;
    }


    @Override
    public void addChild( IEntry childToAdd )
    {
        children.add( childToAdd );
    }


    @Override
    public void deleteChild( IEntry childToDelete )
    {
        children.remove( childToDelete );
    }


    @Override
    public boolean isChildrenInitialized()
    {
        return childrenInitialized;
    }


    @Override
    public void setChildrenInitialized( boolean b )
    {
        this.childrenInitialized = b;
    }


    @Override
    public IEntry[] getChildren()
    {
        if ( children.isEmpty() )
        {
            return null;
        }
        return children.toArray( new IEntry[0] );
    }


    @Override
    public int getChildrenCount()
    {
        return childrenInitialized ? children.size() : -1;
    }


    @Override
    public boolean hasChildren()
    {
        return !children.isEmpty();
    }


    @Override
    public void setHasChildrenHint( boolean b )
    {
    }


    @Override
    public boolean hasMoreChildren()
    {
        return false;
    }


    @Override
    public void setHasMoreChildren( boolean b )
    {
    }


    @Override
    public StudioConnectionBulkRunnableWithProgress getTopPageChildrenRunnable()
    {
        return null;
    }


    @Override
    public void setTopPageChildrenRunnable( StudioConnectionBulkRunnableWithProgress topPageChildrenRunnable )
    {
    }


    @Override
    public StudioConnectionBulkRunnableWithProgress getNextPageChildrenRunnable()
    {
        return null;
    }


    @Override
    public void setNextPageChildrenRunnable( StudioConnectionBulkRunnableWithProgress nextPageChildrenRunnable )
    {
    }


    @Override
    public boolean isAttributesInitialized()
    {
        return attributesInitialized;
    }


    @Override
    public void setAttributesInitialized( boolean b )
    {
        this.attributesInitialized = b;
    }


    @Override
    public IAttribute[] getAttributes()
    {
        if ( attributes.isEmpty() )
        {
            return null;
        }
        return attributes.toArray( new IAttribute[0] );
    }


    @Override
    public IAttribute getAttribute( String attributeDescription )
    {
        for ( IAttribute attr : attributes )
        {
            if ( attr.getDescription().equalsIgnoreCase( attributeDescription ) )
            {
                return attr;
            }
        }
        return null;
    }


    @Override
    public AttributeHierarchy getAttributeWithSubtypes( String attributeDescription )
    {
        return null;
    }


    @Override
    public void addAttribute( IAttribute attributeToAdd ) throws IllegalArgumentException
    {
        attributes.add( attributeToAdd );
    }


    @Override
    public void deleteAttribute( IAttribute attributeToDelete ) throws IllegalArgumentException
    {
        attributes.remove( attributeToDelete );
    }


    @Override
    public void setDirectoryEntry( boolean isDirectoryEntry )
    {
    }


    @Override
    public boolean isAlias()
    {
        return false;
    }


    @Override
    public void setAlias( boolean b )
    {
    }


    @Override
    public boolean isReferral()
    {
        return false;
    }


    @Override
    public void setReferral( boolean b )
    {
    }


    @Override
    public boolean isSubentry()
    {
        return false;
    }


    @Override
    public void setSubentry( boolean b )
    {
    }


    @Override
    public boolean isInitOperationalAttributes()
    {
        return false;
    }


    @Override
    public void setInitOperationalAttributes( boolean b )
    {
    }


    @Override
    public boolean isFetchAliases()
    {
        return false;
    }


    @Override
    public void setFetchAliases( boolean b )
    {
    }


    @Override
    public boolean isFetchReferrals()
    {
        return false;
    }


    @Override
    public void setFetchReferrals( boolean b )
    {
    }


    @Override
    public boolean isFetchSubentries()
    {
        return false;
    }


    @Override
    public void setFetchSubentries( boolean b )
    {
    }


    @Override
    public String getChildrenFilter()
    {
        return null;
    }


    @Override
    public void setChildrenFilter( String filter )
    {
    }


    @Override
    public LdapUrl getUrl()
    {
        return null;
    }


    @Override
    public Collection<ObjectClass> getObjectClassDescriptions()
    {
        return Collections.emptyList();
    }


    @Override
    public <T> T getAdapter( Class<T> adapter )
    {
        return null;
    }
}
