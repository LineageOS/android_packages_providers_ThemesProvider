/*
 * Copyright (C) 2015 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cyanogenmod.themes.provider.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import android.graphics.Canvas;
import com.koushikdutta.ion.gif.GifAction;
import com.koushikdutta.ion.gif.GifDecoder;
import com.koushikdutta.ion.gif.GifFrame;

import java.io.IOException;
import java.io.InputStream;

public class GifUtils {
    private static final String TAG = "GifUtils";

    public static Bitmap getBitmapFromAsset(Context ctx, String path, int reqWidth, int reqHeight) {
        if (ctx == null || path == null) {
            return null;
        }

        Bitmap bitmap = null;
        try {
            // Load Gif
            AssetManager assets = ctx.getAssets();
            InputStream is = assets.open(path);
            GifUtilsAction ga = new GifUtilsAction();
            GifDecoder gd = new GifDecoder(is, ga);
            gd.run();

            // Grab the middle frame
            int frameCount = gd.getFrameCount();
            Bitmap fullBitmap = gd.getFrameImage(frameCount/2);

            // Scale and crop to fit required dimensions.
            Bitmap croppedBitmap = Bitmap.createBitmap(reqWidth, reqHeight, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas (croppedBitmap);
            calculateScale(canvas, fullBitmap, reqWidth, reqHeight);
            canvas.drawBitmap(fullBitmap, 0, 0, null);

            if (croppedBitmap != null) {
                bitmap = croppedBitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private static void calculateScale(Canvas canvas, Bitmap bitmap, int reqWidth, int reqHeight) {
        float xScale = 1;
        float yScale = 1;
        float xPoint = 0;
        float yPoint = 0;

        if (reqWidth != bitmap.getWidth() || reqHeight != bitmap.getHeight()) {
            // Loss of precision here, but scale requires float not double.
            xScale = (float) reqWidth / (float) bitmap.getWidth();
            yScale = (float) reqHeight / (float) bitmap.getHeight();

            // Keep ratios the same, pick the largest scale change.
            if (xScale >= yScale) {
                yScale = xScale;

                // Find center
                yPoint = calculatePivotPoint(yScale, (float) bitmap.getHeight(), reqHeight);
            } else {
                xScale = yScale;

                // Find center
                xPoint = calculatePivotPoint(xScale, (float) bitmap.getWidth(), reqWidth);
            }
        }
        canvas.translate(xPoint, yPoint);
        canvas.scale(xScale, yScale);
    }

    private static float calculatePivotPoint(float scale, float srcSize, float dstSize) {
        float scaled = srcSize * scale;
        return ((dstSize - scaled) / 2);
    }

    private static class GifUtilsAction implements GifAction {
        public boolean mParseStatus = false;
        public int mFrameIndex = 0;

        public boolean parseOk(boolean parseStatus, int frameIndex) {
            mParseStatus = parseStatus;
            mFrameIndex = frameIndex;
            return mParseStatus;
        }
    }
}
