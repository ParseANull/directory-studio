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


import java.util.ArrayList;
import java.util.List;


public class ScimServiceProviderConfig
{

    private boolean filterSupported = false;
    private boolean sortSupported = false;
    private boolean patchSupported = false;
    private boolean bulkSupported = false;
    private int bulkMaxOperations = 0;
    private boolean etagSupported = false;
    private List<String> authenticationSchemes = new ArrayList<>();


    public boolean isFilterSupported()
    {
        return filterSupported;
    }


    public void setFilterSupported( boolean filterSupported )
    {
        this.filterSupported = filterSupported;
    }


    public boolean isSortSupported()
    {
        return sortSupported;
    }


    public void setSortSupported( boolean sortSupported )
    {
        this.sortSupported = sortSupported;
    }


    public boolean isPatchSupported()
    {
        return patchSupported;
    }


    public void setPatchSupported( boolean patchSupported )
    {
        this.patchSupported = patchSupported;
    }


    public boolean isBulkSupported()
    {
        return bulkSupported;
    }


    public void setBulkSupported( boolean bulkSupported )
    {
        this.bulkSupported = bulkSupported;
    }


    public int getBulkMaxOperations()
    {
        return bulkMaxOperations;
    }


    public void setBulkMaxOperations( int bulkMaxOperations )
    {
        this.bulkMaxOperations = bulkMaxOperations;
    }


    public boolean isEtagSupported()
    {
        return etagSupported;
    }


    public void setEtagSupported( boolean etagSupported )
    {
        this.etagSupported = etagSupported;
    }


    public List<String> getAuthenticationSchemes()
    {
        return authenticationSchemes;
    }


    public void setAuthenticationSchemes( List<String> authenticationSchemes )
    {
        this.authenticationSchemes = authenticationSchemes;
    }

}
