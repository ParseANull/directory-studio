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


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.connection.core.Connection.ReferralHandlingMethod;
import org.apache.directory.studio.ldapbrowser.core.BookmarkManager;
import org.apache.directory.studio.ldapbrowser.core.SearchManager;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IQuickSearch;
import org.apache.directory.studio.ldapbrowser.core.model.IRootDSE;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.scim.core.adapter.IScimAdapter;
import org.apache.directory.studio.scim.core.adapter.IScimAdapterFactory;
import org.apache.directory.studio.scim.core.connection.IScimConnectionParameters;
import org.apache.directory.studio.scim.core.connection.ScimConnectionParameters;


public class ScimBrowserConnection implements IBrowserConnection
{
    private static final long serialVersionUID = 1L;

    private final Connection connection;
    private transient IScimAdapter adapter;
    private ScimRootEntry rootDSE;
    private final Map<Dn, IEntry> entryCache = new ConcurrentHashMap<>();


    public ScimBrowserConnection( Connection connection )
    {
        this.connection = connection;
        IScimConnectionParameters params = new ScimConnectionParameters( connection.getConnectionParameter() );
        IScimAdapterFactory factory = ScimAdapterRegistry.findFactory( params.getProviderId() );
        this.adapter = ( factory != null ) ? factory.create( params ) : null;
        this.rootDSE = new ScimRootEntry( this );
    }


    public IScimAdapter getAdapter()
    {
        return adapter;
    }


    @Override
    public Connection getConnection()
    {
        return connection;
    }


    @Override
    public IRootDSE getRootDSE()
    {
        return rootDSE;
    }


    @Override
    public IEntry getEntryFromCache( Dn dn )
    {
        return entryCache.get( dn );
    }


    @Override
    public void cacheEntry( IEntry entry )
    {
        entryCache.put( entry.getDn(), entry );
    }


    @Override
    public void uncacheEntryRecursive( IEntry entry )
    {
        IEntry[] children = entry.getChildren();
        if ( children != null )
        {
            for ( IEntry child : children )
            {
                uncacheEntryRecursive( child );
            }
        }
        entryCache.remove( entry.getDn() );
    }


    @Override
    public void clearCaches()
    {
        entryCache.clear();
    }


    @Override
    public <T> T getAdapter( Class<T> adapter )
    {
        return null;
    }


    // LDAP stubs — not applicable to SCIM

    @Override
    public LdapUrl getUrl()
    {
        return null;
    }


    @Override
    public boolean isFetchBaseDNs()
    {
        return false;
    }


    @Override
    public void setFetchBaseDNs( boolean fetchBaseDNs )
    {
    }


    @Override
    public Dn getBaseDN()
    {
        return Dn.EMPTY_DN;
    }


    @Override
    public void setBaseDN( Dn baseDn )
    {
    }


    @Override
    public int getCountLimit()
    {
        return 0;
    }


    @Override
    public void setCountLimit( int countLimit )
    {
    }


    @Override
    public AliasDereferencingMethod getAliasesDereferencingMethod()
    {
        return null;
    }


    @Override
    public void setAliasesDereferencingMethod( AliasDereferencingMethod aliasesDereferencingMethod )
    {
    }


    @Override
    public ReferralHandlingMethod getReferralsHandlingMethod()
    {
        return null;
    }


    @Override
    public void setReferralsHandlingMethod( ReferralHandlingMethod referralsHandlingMethod )
    {
    }


    @Override
    public int getTimeLimit()
    {
        return 0;
    }


    @Override
    public void setTimeLimit( int timeLimit )
    {
    }


    @Override
    public boolean isFetchSubentries()
    {
        return false;
    }


    @Override
    public void setFetchSubentries( boolean fetchSubentries )
    {
    }


    @Override
    public boolean isManageDsaIT()
    {
        return false;
    }


    @Override
    public void setManageDsaIT( boolean manageDsaIT )
    {
    }


    @Override
    public boolean isFetchOperationalAttributes()
    {
        return false;
    }


    @Override
    public void setFetchOperationalAttributes( boolean fetchOperationalAttributes )
    {
    }


    @Override
    public boolean isPagedSearch()
    {
        return false;
    }


    @Override
    public void setPagedSearch( boolean pagedSearch )
    {
    }


    @Override
    public int getPagedSearchSize()
    {
        return 0;
    }


    @Override
    public void setPagedSearchSize( int pagedSearchSize )
    {
    }


    @Override
    public boolean isPagedSearchScrollMode()
    {
        return false;
    }


    @Override
    public void setPagedSearchScrollMode( boolean pagedSearchScrollMode )
    {
    }


    @Override
    public ModifyMode getModifyMode()
    {
        return null;
    }


    @Override
    public void setModifyMode( ModifyMode mode )
    {
    }


    @Override
    public ModifyMode getModifyModeNoEMR()
    {
        return null;
    }


    @Override
    public void setModifyModeNoEMR( ModifyMode mode )
    {
    }


    @Override
    public ModifyOrder getModifyAddDeleteOrder()
    {
        return null;
    }


    @Override
    public void setModifyAddDeleteOrder( ModifyOrder mode )
    {
    }


    @Override
    public void setQuickSearch( IQuickSearch quickSearch )
    {
    }


    @Override
    public IQuickSearch getQuickSearch()
    {
        return null;
    }


    @Override
    public Schema getSchema()
    {
        return null;
    }


    @Override
    public void setSchema( Schema schema )
    {
    }


    @Override
    public SearchManager getSearchManager()
    {
        return null;
    }


    @Override
    public BookmarkManager getBookmarkManager()
    {
        return null;
    }
}
