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

package org.apache.directory.studio.ldapbrowser.core.model.impl;


import java.util.Arrays;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IRootDSE;


// ── CLASS: RootDSE — THE DEATH STAR'S MAIN CONTROL ROOM ──────────────────────
// The Death Star has one special control room at the very top of the hierarchy
// that has no parent — it contains the master list of everything: all naming
// contexts, all supported extensions, all supported controls and features.
// RootDSE is that control room: a BaseDNEntry with an empty DN whose parent is
// always null, and that exposes convenience methods to query what extensions,
// controls, and features the directory advertises.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents the root DSE (Directory Specific Entry) of an LDAP directory.
 * Extends {@link BaseDNEntry} with an empty DN and a {@code null} parent.
 * Provides convenience accessors for the root DSE's supported extensions,
 * controls, and features attributes, plus OID-level check methods.
 *
 * <p>Think of this as the Death Star's main control room — no parent, sits at
 * the absolute top of the directory tree, and contains the master capability
 * advertisement.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class RootDSE extends BaseDNEntry implements IRootDSE
{

    private static final long serialVersionUID = -8445018787232919754L;


    // ── No-Arg Constructor For Serialisation ─────────────────────────────────────
    protected RootDSE()
    {
    }


    // ── Control Room Constructor: Just The Connection ─────────────────────────────
    /**
     * Creates a new instance of RootDSE with an empty DN.
     *
     * @param browserConnection the browser connection this root DSE belongs to
     */
    public RootDSE( IBrowserConnection browserConnection )
    {
        super( Dn.EMPTY_DN, browserConnection );
    }


    // ── Root Has No Parent — Always Returns null ──────────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.impl.BaseDNEntry#getParententry()
     */
    public IEntry getParententry()
    {
        return null;
    }


    // ── Control Room: List All Supported Extension OIDs ──────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.IRootDSE#getSupportedExtensions()
     */
    public String[] getSupportedExtensions()
    {
        return getAttributeValues( SchemaConstants.SUPPORTED_EXTENSION_AT );
    }


    // ── Control Room: List All Supported Control OIDs ─────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.IRootDSE#getSupportedControls()
     */
    public String[] getSupportedControls()
    {
        return getAttributeValues( SchemaConstants.SUPPORTED_CONTROL_AT );
    }


    // ── Control Room: List All Supported Feature OIDs ─────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.IRootDSE#getSupportedFeatures()
     */
    public String[] getSupportedFeatures()
    {
        return getAttributeValues( SchemaConstants.SUPPORTED_FEATURES_AT );
    }


    // ── R2-D2 Fetches And Sorts The Values Of One Root DSE Attribute ─────────────
    /**
     * Gets the attribute values.
     *
     * @param attributeDescription the attribute description to look up
     * @return sorted array of string values, or an empty array if the attribute is absent
     */
    private String[] getAttributeValues( String attributeDescription )
    {
        IAttribute supportedFeaturesAttr = getAttribute( attributeDescription );
        if ( supportedFeaturesAttr != null )
        {
            String[] stringValues = supportedFeaturesAttr.getStringValues();
            Arrays.sort( stringValues );
            return stringValues;
        }
        else
        {
            return new String[0];
        }
    }


    // ── The Root DSE Is Never A Subentry ─────────────────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.impl.AbstractEntry#isSubentry()
     */
    public boolean isSubentry()
    {
        return false;
    }


    // ── Mace Windu Checks: Does The Server Support This Extension OID? ───────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.IRootDSE#isExtensionSupported(java.lang.String)
     */
    public boolean isExtensionSupported( String oid )
    {
        String[] supportedExtensions = getSupportedExtensions();
        return Arrays.asList( supportedExtensions ).contains( oid );
    }


    // ── Mace Windu Checks: Does The Server Support This Control OID? ─────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.IRootDSE#isControlSupported(java.lang.String)
     */
    public boolean isControlSupported( String oid )
    {
        String[] supportedControls = getSupportedControls();
        return Arrays.asList( supportedControls ).contains( oid );
    }


    // ── Mace Windu Checks: Does The Server Support This Feature OID? ─────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.IRootDSE#isFeatureSupported(java.lang.String)
     */
    public boolean isFeatureSupported( String oid )
    {
        String[] supportedFeatures = getSupportedFeatures();
        return Arrays.asList( supportedFeatures ).contains( oid );
    }

}
