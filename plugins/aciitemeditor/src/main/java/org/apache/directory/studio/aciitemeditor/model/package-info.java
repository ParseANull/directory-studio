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
/**
 * Model layer for the ACI item editor.
 * Provides wrapper objects ({@link org.apache.directory.studio.aciitemeditor.model.ProtectedItemWrapper}
 * and {@link org.apache.directory.studio.aciitemeditor.model.UserClassWrapper}) that serve as
 * table-viewer rows in the visual editor, and factories that pre-populate those wrappers
 * with all possible protected-item and user-class types.
 * Think of this as the Imperial Security Bureau's structured clearance manifest — every
 * possible category of resource and user class, pre-labelled and ready for the officer
 * to tick or configure.
 */
package org.apache.directory.studio.aciitemeditor.model;
