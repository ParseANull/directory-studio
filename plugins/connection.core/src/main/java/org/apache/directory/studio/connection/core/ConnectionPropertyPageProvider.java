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

package org.apache.directory.studio.connection.core;


import org.eclipse.core.runtime.IAdaptable;


// ── CLASS: ConnectionPropertyPageProvider — THE FALCON'S DIAGNOSTIC PLUG POINT
// When ground crew want to run a full diagnostic on the Falcon, they plug their
// datapad into a standard diagnostic port on the hull. Any ship that has that
// port can be diagnosed.
// This tagging interface is that port: any object that implements it is telling
// Eclipse "you can open my Connection property page on me."
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Tagging interface that marks an object as capable of providing input for
 * the Connection property page in Eclipse.
 * Implementing objects must also implement {@link IAdaptable}, and their
 * {@code getAdapter(Connection.class)} must return the relevant {@link Connection}.
 * Eclipse's property page framework uses this to know which objects are eligible
 * for the connection property page.
 * Think of this interface as the Falcon's diagnostic port: plugging in (implementing)
 * tells the ground crew this ship supports full property inspection.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ConnectionPropertyPageProvider extends IAdaptable
{
}
