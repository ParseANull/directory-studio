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

package org.apache.directory.studio.ldifeditor.editor.text;


import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;


// ── CLASS: LdifExternalAnnotationModel — REBEL BASE CODE-FOLDING MANIFEST ─────
// The Rebel base keeps a separate manifest of which corridor sections are
// currently collapsed, independent of the file itself.
// LdifExternalAnnotationModel is that manifest: a plain subclass of
// ProjectionAnnotationModel that gives the LDIF editor its own named type
// so {@code LdifDocumentProvider.createAnnotationModel()} can return it
// and callers can check {@code instanceof} against it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF-editor-specific {@link ProjectionAnnotationModel}.
 * Returned by
 * {@link org.apache.directory.studio.ldifeditor.editor.LdifDocumentProvider#createAnnotationModel}
 * so that the LDIF editor has its own named annotation-model type for code
 * folding.  No additional behaviour is added.
 * Think of this as the Rebel base manifest of collapsed corridor sections.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifExternalAnnotationModel extends ProjectionAnnotationModel
{

}
