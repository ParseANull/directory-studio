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

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IRootDSE;


public class ScimRootEntry implements IRootDSE
{
    private static final long serialVersionUID = 1L;

    private final ScimBrowserConnection browserConnection;
    private final List<IEntry> children = new ArrayList<>();
    private boolean childrenInitialized;
    private StudioConnectionBulkRunnableWithProgress topPageChildrenRunnable;


    public ScimRootEntry( ScimBrowserConnection browserConnection )
    {
        this.browserConnection = browserConnection;
    }


    // IRootDSE methods

    @Override
    public String[] getSupportedExtensions()
    {
        return new String[0];
    }


    @Override
    public String[] getSupportedControls()
    {
        return new String[0];
    }


    @Override
    public String[] getSupportedFeatures()
    {
        return new String[0];
    }


    @Override
    public boolean isExtensionSupported( String oid )
    {
        return false;
    }


    @Override
    public boolean isControlSupported( String oid )
    {
        return false;
    }


    @Override
    public boolean isFeatureSupported( String oid )
    {
        return false;
    }


    // IEntry methods

    @Override
    public Dn getDn()
    {
        return Dn.EMPTY_DN;
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
        return topPageChildrenRunnable;
    }


    @Override
    public void setTopPageChildrenRunnable( StudioConnectionBulkRunnableWithProgress topPageChildrenRunnable )
    {
        this.topPageChildrenRunnable = topPageChildrenRunnable;
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
    public boolean hasParententry()
    {
        return false;
    }


    @Override
    public IEntry getParententry()
    {
        return null;
    }


    @Override
    public boolean isAttributesInitialized()
    {
        return false;
    }


    @Override
    public void setAttributesInitialized( boolean b )
    {
    }


    @Override
    public IAttribute[] getAttributes()
    {
        return null;
    }


    @Override
    public IAttribute getAttribute( String attributeDescription )
    {
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
    }


    @Override
    public void deleteAttribute( IAttribute attributeToDelete ) throws IllegalArgumentException
    {
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
