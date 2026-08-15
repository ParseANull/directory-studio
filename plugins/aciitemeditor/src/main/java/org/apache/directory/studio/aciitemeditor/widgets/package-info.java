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
 * Composite widgets that make up the ACI item editor dialog.
 * Each widget handles a distinct slice of the ACI structure: the general header fields
 * (tag, precedence, auth level), protected items, user classes, grants and denials,
 * item permissions, user permissions, the raw source editor, and the tab folder that
 * holds the visual and source views side by side.
 * Think of these as the individual instrument panels on the Death Star command bridge —
 * together they give Grand Moff Tarkin full control over every aspect of a security directive.
 */
package org.apache.directory.studio.aciitemeditor.widgets;
