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
package org.apache.directory.studio.schemaeditor.view;


import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.ObjectClassTypeEnum;
import org.apache.directory.api.ldap.model.schema.UsageEnum;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.wrappers.AttributeTypeWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.ObjectClassWrapper;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;


// ── CLASS: SchemaEditorTypeLabelDecorator — C-3PO READS THE JAWA DIALECT ─────
// On Tatooine, C-3PO listens to a frantic Jawa and instantly identifies what
// kind of droid they are selling — a protocol unit? an astromech? — then pins
// a coloured badge on the display cage so the buyer knows at a glance.
// We play C-3PO here: given any schema element, we read its type metadata
// (user-application vs. operational attribute, abstract vs. structural vs.
// auxiliary object class) and stamp the matching icon overlay on its tree node.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A JFace {@link ILightweightLabelDecorator} that overlays a small type-badge
 * icon on attribute-type and object-class nodes in the Schema View tree,
 * making the element's specific category visible without the user having to
 * open the editor.
 * Eclipse calls {@link #decorate} for each visible tree node; we read the
 * element's type information and attach the right overlay to the bottom-right
 * corner of its icon.
 * Think of this class as C-3PO translating Jawa into plain Basic: it converts
 * an opaque type enum into a little picture that everyone can understand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaEditorTypeLabelDecorator extends LabelProvider implements ILightweightLabelDecorator
{
    // ── C-3PO READS THE BADGE AND PINS A NEW ONE ─────────────────────────────
    // A Jawa shoves a grimy droid crate in front of C-3PO and rattles off its
    // specs in rapid Jawa dialect. C-3PO translates instantly: "Ah — this is a
    // USER_APPLICATIONS attribute, suitable for general use." He pins the
    // user-application badge on the cage so the moisture farmer can see it.
    // For STRUCTURAL object classes he pins the structural badge; for AUXILIARY
    // ones the auxiliary badge; and so on down the Jawa's inventory list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads the type metadata of a schema tree element and attaches a small
     * icon overlay to its bottom-right corner so the user can identify the
     * element's category at a glance in the tree.
     * Eclipse calls this for every visible node; we handle four distinct element
     * types: {@link AttributeTypeWrapper}, {@link ObjectClassWrapper}, raw
     * {@link AttributeType}, and raw {@link ObjectClass}.
     * For attribute types we distinguish user-application vs. operational usage;
     * for object classes we distinguish abstract, structural, and auxiliary.
     *
     * <p>For example — C-3PO pins badges on the Jawa's droid inventory:</p>
     * <pre>
     *   AttributeTypeWrapper (USER_APPLICATIONS)  → user-application badge
     *   AttributeTypeWrapper (DIRECTORY_OPERATION) → operational badge
     *   ObjectClassWrapper   (STRUCTURAL)          → structural badge
     *   ObjectClassWrapper   (AUXILIARY)            → auxiliary badge
     * </pre>
     *
     * @param element     the tree node to decorate; we handle wrappers and raw
     *                    schema objects from the Apache Directory API
     * @param decoration  the JFace decoration context we add our overlay image to
     */
    public void decorate( Object element, IDecoration decoration )
    {
        if ( element instanceof AttributeTypeWrapper )
        {
            UsageEnum usage = ( ( AttributeTypeWrapper ) element ).getAttributeType().getUsage();
            if ( usage == UsageEnum.USER_APPLICATIONS )
            {
                decoration.addOverlay( Activator.getDefault().getImageDescriptor(
                    PluginConstants.IMG_ATTRIBUTE_TYPE_OVERLAY_USER_APPLICATION ), IDecoration.BOTTOM_RIGHT );
            }
            else if ( ( usage == UsageEnum.DIRECTORY_OPERATION ) || ( usage == UsageEnum.DISTRIBUTED_OPERATION )
                || ( usage == UsageEnum.DSA_OPERATION ) )
            {
                decoration.addOverlay( Activator.getDefault().getImageDescriptor(
                    PluginConstants.IMG_ATTRIBUTE_TYPE_OVERLAY_OPERATION ), IDecoration.BOTTOM_RIGHT );
            }
        }
        else if ( element instanceof ObjectClassWrapper )
        {
            ObjectClassTypeEnum classType = ( ( ObjectClassWrapper ) element ).getObjectClass().getType();
            if ( classType == ObjectClassTypeEnum.ABSTRACT )
            {
                decoration.addOverlay( Activator.getDefault().getImageDescriptor(
                    PluginConstants.IMG_OBJECT_CLASS_OVERLAY_ABSTRACT ), IDecoration.BOTTOM_RIGHT );
            }
            else if ( classType == ObjectClassTypeEnum.STRUCTURAL )
            {
                decoration.addOverlay( Activator.getDefault().getImageDescriptor(
                    PluginConstants.IMG_OBJECT_CLASS_OVERLAY_STRUCTURAL ), IDecoration.BOTTOM_RIGHT );
            }
            else if ( classType == ObjectClassTypeEnum.AUXILIARY )
            {
                decoration.addOverlay( Activator.getDefault().getImageDescriptor(
                    PluginConstants.IMG_OBJECT_CLASS_OVERLAY_AUXILIARY ), IDecoration.BOTTOM_RIGHT );
            }
        }
        else if ( element instanceof AttributeType )
        {
            UsageEnum usage = ( ( AttributeType ) element ).getUsage();
            if ( usage == UsageEnum.USER_APPLICATIONS )
            {
                decoration.addOverlay( Activator.getDefault().getImageDescriptor(
                    PluginConstants.IMG_ATTRIBUTE_TYPE_OVERLAY_USER_APPLICATION ), IDecoration.BOTTOM_RIGHT );
            }
            else if ( ( usage == UsageEnum.DIRECTORY_OPERATION ) || ( usage == UsageEnum.DISTRIBUTED_OPERATION )
                || ( usage == UsageEnum.DSA_OPERATION ) )
            {
                decoration.addOverlay( Activator.getDefault().getImageDescriptor(
                    PluginConstants.IMG_ATTRIBUTE_TYPE_OVERLAY_OPERATION ), IDecoration.BOTTOM_RIGHT );
            }
        }
        else if ( element instanceof ObjectClass )
        {
            ObjectClassTypeEnum classType = ( ( ObjectClass ) element ).getType();
            if ( classType == ObjectClassTypeEnum.ABSTRACT )
            {
                decoration.addOverlay( Activator.getDefault().getImageDescriptor(
                    PluginConstants.IMG_OBJECT_CLASS_OVERLAY_ABSTRACT ), IDecoration.BOTTOM_RIGHT );
            }
            else if ( classType == ObjectClassTypeEnum.STRUCTURAL )
            {
                decoration.addOverlay( Activator.getDefault().getImageDescriptor(
                    PluginConstants.IMG_OBJECT_CLASS_OVERLAY_STRUCTURAL ), IDecoration.BOTTOM_RIGHT );
            }
            else if ( classType == ObjectClassTypeEnum.AUXILIARY )
            {
                decoration.addOverlay( Activator.getDefault().getImageDescriptor(
                    PluginConstants.IMG_OBJECT_CLASS_OVERLAY_AUXILIARY ), IDecoration.BOTTOM_RIGHT );
            }
        }
    }
}
