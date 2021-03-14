/*
 * Copyright 2017-present, Nam Seob Seo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.acrocontext.image.svg.process;

import com.acrocontext.image.svg.ImageConvertOptions;
import com.acrocontext.image.svg.model.ImageData;

/**
 * Date 12/24/17
 *
 * @author Nam Seob Seo
 */
public class PaletteGenerator {
  public byte[][] generatePalette(ImageData imageData, ImageConvertOptions options) {
    // Use custom palette if pal is defined or sample or generate custom length palette
    if (options.isColorSampling()) {
      return samplePalette(options.getNumberOfColors(), imageData);
    } else {
      return generatePalette(options.getNumberOfColors());
    }
  }

  // Generating a palette with numberOfColors, array[numberOfColors][4] where [i][0] = R ; [i][1] =
  // G ; [i][2] = B ; [i][3] = A
  private static byte[][] generatePalette(int numberOfColors) {
    byte[][] palette = new byte[numberOfColors][4];
    if (numberOfColors < 8) {

      // Gray scale
      double grayStep = 255.0 / (double) (numberOfColors - 1);
      for (byte idx = 0; idx < numberOfColors; idx++) {
        palette[idx][0] = (byte) (-128 + Math.round(idx * grayStep));
        palette[idx][1] = (byte) (-128 + Math.round(idx * grayStep));
        palette[idx][2] = (byte) (-128 + Math.round(idx * grayStep));
        palette[idx][3] = (byte) 127;
      }

    } else {
      // RGB color cube
      int colorCubeCount =
          (int)
              Math.floor(
                  Math.pow(
                      numberOfColors,
                      1.0 / 3.0)); // Number of points on each edge on the RGB color cube
      int colorStep = (int) Math.floor(255 / (colorCubeCount - 1)); // distance between points
      int colorCount = 0;
      for (int redIdx = 0; redIdx < colorCubeCount; redIdx++) {
        for (int greenIdx = 0; greenIdx < colorCubeCount; greenIdx++) {
          for (int blueIdx = 0; blueIdx < colorCubeCount; blueIdx++) {
            palette[colorCount][0] = (byte) (-128 + (redIdx * colorStep));
            palette[colorCount][1] = (byte) (-128 + (greenIdx * colorStep));
            palette[colorCount][2] = (byte) (-128 + (blueIdx * colorStep));
            palette[colorCount][3] = (byte) 127;
            colorCount++;
          } // End of blue loop
        } // End of green loop
      } // End of red loop

      // Rest is random
      for (int idx = colorCount; idx < numberOfColors; idx++) {
        palette[colorCount][0] = (byte) (-128 + Math.floor(Math.random() * 255));
        palette[colorCount][1] = (byte) (-128 + Math.floor(Math.random() * 255));
        palette[colorCount][2] = (byte) (-128 + Math.floor(Math.random() * 255));
        palette[colorCount][3] = (byte) (-128 + Math.floor(Math.random() * 255));
      }
    } // End of numberOfColors check

    return palette;
  }
  // End of generatePalette()

  private static byte[][] samplePalette(int numberOfColors, ImageData imageData) {
    byte[][] palette = new byte[numberOfColors][4];
    for (int colorIdx = 0; colorIdx < numberOfColors; colorIdx++) {
      int idx = (int) (Math.floor((Math.random() * imageData.getData().length) / 4) * 4);
      palette[colorIdx][0] = imageData.dataAt(idx);
      palette[colorIdx][1] = imageData.dataAt(idx + 1);
      palette[colorIdx][2] = imageData.dataAt(idx + 2);
      palette[colorIdx][3] = imageData.dataAt(idx + 3);
    }
    return palette;
  } // End of samplePalette()
}
