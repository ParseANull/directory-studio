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
package org.apache.directory.studio.scim.client.auth;


import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.directory.studio.scim.core.exception.ScimAuthException;


/**
 * Auth strategy that injects an HTTP Basic Authorization header on every request.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BasicAuthStrategy implements IScimAuthStrategy
{
    private final String username;
    private final String password;


    public BasicAuthStrategy( String username, String password )
    {
        this.username = username;
        this.password = password;
    }


    @Override
    public void applyAuth( java.net.http.HttpRequest.Builder builder ) throws ScimAuthException
    {
        String encoded = Base64.getEncoder().encodeToString(
            ( username + ":" + password ).getBytes( StandardCharsets.UTF_8 ) );
        builder.header( "Authorization", "Basic " + encoded );
    }


    @Override
    public void refresh() throws ScimAuthException
    {
        // credentials are static; nothing to refresh
    }
}
