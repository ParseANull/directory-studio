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
package org.apache.directory.studio.templateeditor.model;


// ── CLASS: ExtensionPointTemplate — THE DEATH STAR BLUEPRINTS FROM THE ARCHIVE ───
// Some Death Star blueprints were encoded directly in the Imperial Archive — not
// carried on a data cartridge, but baked into the central records via an official
// Imperial communiqué (i.e. the plugin.xml file). This class represents a template
// contributed via an Eclipse extension point rather than a loose file on disk.
// It has no extra fields — everything it needs is already in {@link AbstractTemplate}.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A {@link Template} contributed via an Eclipse extension point declaration in a
 * plugin's {@code plugin.xml}. The template manager discovers these at startup by
 * scanning the Eclipse registry rather than the filesystem. No extra fields are
 * needed beyond what {@link AbstractTemplate} already provides.
 * Think of this as the Death Star blueprints delivered via an Imperial communiqué.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExtensionPointTemplate extends AbstractTemplate
{
}
