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
import com.acrocontext.image.svg.utils.ParallelOperationUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Date 12/23/17
 *
 * @author Nam Seob Seo
 */
@Slf4j
public class ColorQuantizator {
  // 1. Color quantization repeated "cycles" times, based on K-means clustering
  // https://en.wikipedia.org/wiki/Color_quantization
  // https://en.wikipedia.org/wiki/K-means_clustering
  public int[][] colorQuantization(
      ImageData imageData, byte[][] palette, ImageConvertOptions options) {
    float minColorRatio = options.getMinColorRatio();
    int cycles = options.getColorQuantCycles();

    // Creating indexed color array indexedDataArray which has a boundary filled with -1 in every
    // direction
    int[][] indexedDataArray = new int[imageData.getHeight() + 2][imageData.getWidth() + 2];

    for (int row = 0; row < (imageData.getHeight() + 2); row++) {
      indexedDataArray[row][0] = -1;
      indexedDataArray[row][imageData.getWidth() + 1] = -1;
    }
    for (int col = 0; col < (imageData.getWidth() + 2); col++) {
      indexedDataArray[0][col] = -1;
      indexedDataArray[imageData.getHeight() + 1][col] = -1;
    }

    // Selective Gaussian blur preprocessing
    if (options.getBlurRadius() > 0) {
      imageData = blur(imageData, options.getBlurRadius(), options.getBlurDelta());
    }

    long[][] paletteAcc = new long[palette.length][5];

    // Repeat clustering step "cycles" times
    for (int loopIdx = 0; loopIdx < cycles; loopIdx++) {
      colorQuantizationIteration(
          loopIdx, minColorRatio, cycles, indexedDataArray, paletteAcc, imageData, palette);
    } // End of Repeat clustering step "cycles" times

    return indexedDataArray;
  } // End of colorQuantization

  private static void colorQuantizationIteration(
      int loopIdx,
      float minColorRatio,
      int cycles,
      int[][] indexedDataArray,
      long[][] paletteAcc,
      ImageData imageData,
      byte[][] palette) {
    // Average colors from the second iteration
    if (loopIdx > 0) {
      // averaging paletteAcc for palette
      for (int k = 0; k < palette.length; k++) {
        // averaging
        if (paletteAcc[k][3] > 0) {
          palette[k][0] = (byte) (-128 + (paletteAcc[k][0] / paletteAcc[k][4]));
          palette[k][1] = (byte) (-128 + (paletteAcc[k][1] / paletteAcc[k][4]));
          palette[k][2] = (byte) (-128 + (paletteAcc[k][2] / paletteAcc[k][4]));
          palette[k][3] = (byte) (-128 + (paletteAcc[k][3] / paletteAcc[k][4]));
        }
        float ratio =
            (float)
                ((double) (paletteAcc[k][4])
                    / (double) (imageData.getWidth() * imageData.getHeight()));

        // Randomizing a color, if there are too few pixels and there will be a new cycle
        if ((ratio < minColorRatio) && (loopIdx < (cycles - 1))) {
          palette[k][0] = (byte) (-128 + Math.floor(Math.random() * 255));
          palette[k][1] = (byte) (-128 + Math.floor(Math.random() * 255));
          palette[k][2] = (byte) (-128 + Math.floor(Math.random() * 255));
          palette[k][3] = (byte) (-128 + Math.floor(Math.random() * 255));
        }
      } // End of palette loop
    } // End of Average colors from the second iteration

    // Resetting palette accumulator for averaging
    for (int i = 0; i < palette.length; i++) {
      paletteAcc[i][0] = 0;
      paletteAcc[i][1] = 0;
      paletteAcc[i][2] = 0;
      paletteAcc[i][3] = 0;
      paletteAcc[i][4] = 0;
    }

    // loop through all pixels
    List<Supplier<Integer>> tasks = new ArrayList<>(imageData.getHeight());

    IntStream.range(0, imageData.getHeight())
        .forEach(
            row ->
                tasks.add(
                    () -> {
                      rowColorQuantizationOnPixel(
                          row, indexedDataArray, paletteAcc, imageData, palette);
                      return row;
                    }));

    ParallelOperationUtils.executeTasks(tasks);
  }

  private static void rowColorQuantizationOnPixel(
      int row,
      int[][] indexedDataArray,
      long[][] paletteAcc,
      ImageData imageData,
      byte[][] palette) {
    for (int col = 0; col < imageData.getWidth(); col++) {

      int idx = ((row * imageData.getWidth()) + col) * 4;

      // find closest color from palette by measuring (rectilinear) color distance between this
      // pixel and all palette colors
      int cdl = 256 + 256 + 256 + 256;
      int ci = 0;
      for (int paletteIdx = 0; paletteIdx < palette.length; paletteIdx++) {

        // In my experience, https://en.wikipedia.org/wiki/Rectilinear_distance works better than
        // https://en.wikipedia.org/wiki/Euclidean_distance
        int c1 = Math.abs(palette[paletteIdx][0] - imageData.dataAt(idx));
        int c2 = Math.abs(palette[paletteIdx][1] - imageData.dataAt(idx + 1));
        int c3 = Math.abs(palette[paletteIdx][2] - imageData.dataAt(idx + 2));
        int c4 = Math.abs(palette[paletteIdx][3] - imageData.dataAt(idx + 3));
        int cd = c1 + c2 + c3 + (c4 * 4); // weighted alpha seems to help images with transparency

        // Remember this color if this is the closest yet
        if (cd < cdl) {
          cdl = cd;
          ci = paletteIdx;
        }
      } // End of palette loop

      // add to paletteAcc
      paletteAcc[ci][0] += 128 + imageData.dataAt(idx);
      paletteAcc[ci][1] += 128 + imageData.dataAt(idx + 1);
      paletteAcc[ci][2] += 128 + imageData.dataAt(idx + 2);
      paletteAcc[ci][3] += 128 + imageData.dataAt(idx + 3);
      paletteAcc[ci][4]++;

      indexedDataArray[row + 1][col + 1] = ci;
    } // End of col loop
  }

  // Gaussian kernels for blur
  private static final double[][] GAUSSIAN_KERNEL_FOR_BLUR = {
    {0.27901, 0.44198, 0.27901},
    {0.135336, 0.228569, 0.272192, 0.228569, 0.135336},
    {0.086776, 0.136394, 0.178908, 0.195843, 0.178908, 0.136394, 0.086776},
    {0.063327, 0.093095, 0.122589, 0.144599, 0.152781, 0.144599, 0.122589, 0.093095, 0.063327},
    {
      0.049692, 0.069304, 0.089767, 0.107988, 0.120651, 0.125194, 0.120651, 0.107988, 0.089767,
      0.069304, 0.049692
    }
  };

  // Selective Gaussian blur for preprocessing
  private static ImageData blur(ImageData imageData, float rad, float del) {

    byte[] imageRawData2 = new byte[imageData.getWidth() * imageData.getHeight() * 4];

    // radius and delta limits, this kernel
    int radius = (int) Math.floor(rad);
    if (radius < 1) {
      return imageData;
    }
    if (radius > 5) {
      radius = 5;
    }
    int delta = (int) Math.abs(del);
    if (delta > 1024) {
      delta = 1024;
    }
    double[] thisGK = GAUSSIAN_KERNEL_FOR_BLUR[radius - 1];

    // loop through all pixels, horizontal blur
    for (int row = 0; row < imageData.getHeight(); row++) {
      for (int col = 0; col < imageData.getWidth(); col++) {

        double rAcc = 0;
        double gAcc = 0;
        double bAcc = 0;
        double aAcc = 0;
        double wAcc = 0;
        // gauss kernel loop
        for (int k = -radius; k < (radius + 1); k++) {
          // add weighted color values
          if (((col + k) > 0) && ((col + k) < imageData.getWidth())) {
            int idx = ((row * imageData.getWidth()) + col + k) * 4;
            rAcc += imageData.dataAt(idx) * thisGK[k + radius];
            gAcc += imageData.dataAt(idx + 1) * thisGK[k + radius];
            bAcc += imageData.dataAt(idx + 2) * thisGK[k + radius];
            aAcc += imageData.dataAt(idx + 3) * thisGK[k + radius];
            wAcc += thisGK[k + radius];
          }
        }
        // The new pixel
        int idx = ((row * imageData.getWidth()) + col) * 4;
        imageRawData2[idx] = (byte) Math.floor(rAcc / wAcc);
        imageRawData2[idx + 1] = (byte) Math.floor(gAcc / wAcc);
        imageRawData2[idx + 2] = (byte) Math.floor(bAcc / wAcc);
        imageRawData2[idx + 3] = (byte) Math.floor(aAcc / wAcc);
      } // End of width loop
    } // End of horizontal blur

    // copying the half blurred imageData2
    byte[] halfImageData = imageRawData2.clone();

    // loop through all pixels, vertical blur
    for (int row = 0; row < imageData.getHeight(); row++) {
      for (int col = 0; col < imageData.getWidth(); col++) {

        double rAcc = 0;
        double gAcc = 0;
        double bAcc = 0;
        double aAcc = 0;
        double wAcc = 0;
        // gauss kernel loop
        for (int k = -radius; k < (radius + 1); k++) {
          // add weighted color values
          if (((row + k) > 0) && ((row + k) < imageData.getHeight())) {
            int idx = (((row + k) * imageData.getWidth()) + col) * 4;
            rAcc += halfImageData[idx] * thisGK[k + radius];
            gAcc += halfImageData[idx + 1] * thisGK[k + radius];
            bAcc += halfImageData[idx + 2] * thisGK[k + radius];
            aAcc += halfImageData[idx + 3] * thisGK[k + radius];
            wAcc += thisGK[k + radius];
          }
        }
        // The new pixel
        int idx = ((row * imageData.getWidth()) + col) * 4;
        imageRawData2[idx] = (byte) Math.floor(rAcc / wAcc);
        imageRawData2[idx + 1] = (byte) Math.floor(gAcc / wAcc);
        imageRawData2[idx + 2] = (byte) Math.floor(bAcc / wAcc);
        imageRawData2[idx + 3] = (byte) Math.floor(aAcc / wAcc);
      } // End of width loop
    } // End of vertical blur

    // Selective blur: loop through all pixels
    for (int row = 0; row < imageData.getHeight(); row++) {
      for (int col = 0; col < imageData.getWidth(); col++) {

        int idx = ((row * imageData.getWidth()) + col) * 4;
        // d is the difference between the blurred and the original pixel
        int d =
            Math.abs(imageRawData2[idx] - imageData.dataAt(idx))
                + Math.abs(imageRawData2[idx + 1] - imageData.dataAt(idx + 1))
                + Math.abs(imageRawData2[idx + 2] - imageData.dataAt(idx + 2))
                + Math.abs(imageRawData2[idx + 3] - imageData.dataAt(idx + 3));
        // selective blur: if d>delta, put the original pixel back
        if (d > delta) {
          imageRawData2[idx] = imageData.dataAt(idx);
          imageRawData2[idx + 1] = imageData.dataAt(idx + 1);
          imageRawData2[idx + 2] = imageData.dataAt(idx + 2);
          imageRawData2[idx + 3] = imageData.dataAt(idx + 3);
        }
      }
    } // End of Selective blur

    return new ImageData(imageData.getWidth(), imageData.getHeight(), imageRawData2);
  } // End of blur()
}
