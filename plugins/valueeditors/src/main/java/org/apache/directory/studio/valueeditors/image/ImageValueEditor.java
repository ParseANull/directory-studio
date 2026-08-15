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

package org.apache.directory.studio.valueeditors.image;


import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.valueeditors.AbstractDialogBinaryValueEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: ImageValueEditor — R2-D2's Holographic Projector ──────────────────
// In the corridor, R2-D2's dome flickers and projects a quick status flash of Leia's message.
// When Luke kneels down and says "Show me everything, R2," R2 opens the full holographic display.
// This editor works the same way: a compact summary inline, the full ImageDialog on demand.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * A value editor for LDAP attributes carrying binary JPEG image data
 * (syntax OID 1.3.6.1.4.1.1466.115.121.1.28).
 * In the attribute table we show a compact info string (format, dimensions, byte size);
 * when the user double-clicks we open the full {@link ImageDialog} for preview and replacement.
 * Think of this class as R2-D2's holographic projector: it flashes a quick inline summary
 * and fires up the full projection only when someone explicitly asks for it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImageValueEditor extends AbstractDialogBinaryValueEditor
{

    // ── Luke Says "Show Me Everything, R2" ───────────────────────────────────────
    // Luke kneels and asks R2 to play the full holographic message, not just the status flash.
    // R2 opens the full projection and, if Luke swaps in a new recording, stores the new bytes.
    // We open the ImageDialog with the current bytes; if the user confirms a new image we setValue.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link ImageDialog} so the user can view the current image and optionally
     * replace it with one loaded from disk.
     * We pass the current attribute bytes in as the "current image" and, if the user clicks OK
     * and picked a valid image, we call {@link #setValue} with the new bytes.
     *
     * <p>For example — Luke asks R2 to play the full message:</p>
     * <pre>
     *   Luke: "Let's see it all, R2."
     *   R2 opens the full holographic display showing the current recording.
     *   Luke picks a new recording; R2 stores it: "New image loaded and ready."
     * </pre>
     *
     * @param shell the SWT shell to use as the dialog's parent window
     * @return {@code true} if the user confirmed a new image, {@code false} if they cancelled
     */
    protected boolean openDialog( Shell shell )
    {
        Object value = getValue();

        if ( value instanceof byte[] )
        {
            byte[] currentImageData = ( byte[] ) value;

            ImageDialog dialog = new ImageDialog( shell, currentImageData, SWT.IMAGE_JPEG );

            if ( ( dialog.open() == ImageDialog.OK ) && ( dialog.getNewImageRawData() != null ) )
            {
                setValue( dialog.getNewImageRawData() );

                return true;
            }
        }

        return false;
    }


    // ── R2 Projects A Quick Status Flash In The Corridor ─────────────────────────
    // Without opening the full display, R2's dome flickers: "JPEG 320x240 15 KB."
    // That quick summary is enough for Obi-Wan to know what kind of image is in the attribute.
    // We delegate to ImageDialog.getImageInfo() to build the compact inline display string.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a compact, human-readable description of the image value for display in the LDAP
     * browser attribute table — without opening any dialog.
     * We delegate to {@link ImageDialog#getImageInfo(byte[])} which decodes just enough of the
     * byte stream to read the format, width, height, and size.
     * If raw-values mode is active we fall back to the default hex/printable display instead.
     *
     * <p>For example — R2 flashes a quick status message in the corridor:</p>
     * <pre>
     *   R2 (inline): "JPEG-320x240 (15234 bytes)"
     *   If the data cannot be parsed as an image: "Invalid image data."
     * </pre>
     *
     * @param value the LDAP attribute value to describe; may be null
     * @return a display string for the attribute table cell
     */
    public String getDisplayValue( IValue value )
    {
        if ( showRawValues() )
        {
            return getPrintableString( value );
        }
        else
        {
            if ( value == null )
            {
                return NULL;
            }
            else if ( value.isBinary() )
            {
                byte[] data = value.getBinaryValue();
                String text = ImageDialog.getImageInfo( data );

                return text;
            }
            else
            {
                return Messages.getString( "ImageValueEditor.InvalidImageData" ); //$NON-NLS-1$
            }
        }
    }
}
