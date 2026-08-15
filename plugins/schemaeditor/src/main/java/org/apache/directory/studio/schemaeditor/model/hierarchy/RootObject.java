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
package org.apache.directory.studio.schemaeditor.model.hierarchy;


// ── CLASS: RootObject — Luke Gazing at the Binary Sunset ─────────────────────
// On Tatooine, Luke stands on the ridge and looks out at the twin suns setting
// over the desert.  Nothing is above him in that frame — the sky starts here.
// He sees everything below: the moisture farm, the hills, the whole world spread
// out beneath that vantage point.  RootObject is exactly that vantage point: the
// invisible top of the hierarchy tree, the node above every attribute type and
// object class, the horizon from which the viewer renders everything else.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A marker object that acts as the invisible root node of the schema hierarchy tree.
 * The HierarchyManager uses it as the universal parent of top-level schema elements,
 * and the hierarchy view's content provider uses it as the input to the tree viewer.
 * Think of it as Luke's binary-sunset vantage point: it has no parent of its own
 * and everything meaningful sits below it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
class RootObject
{

}
