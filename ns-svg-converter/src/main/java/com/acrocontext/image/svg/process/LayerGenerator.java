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

import com.acrocontext.image.svg.utils.ParallelOperationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Date 12/23/17
 *
 * @author Nam Seob Seo
 */
public class LayerGenerator {
  // 2. Layer separation and edge detection
  // Edge node types ( ▓:light or 1; ░:dark or 0 )
  // 12  ░░  ▓░  ░▓  ▓▓  ░░  ▓░  ░▓  ▓▓  ░░  ▓░  ░▓  ▓▓  ░░  ▓░  ░▓  ▓▓
  // 48  ░░  ░░  ░░  ░░  ░▓  ░▓  ░▓  ░▓  ▓░  ▓░  ▓░  ▓░  ▓▓  ▓▓  ▓▓  ▓▓
  //     0   1   2   3   4   5   6   7   8   9   10  11  12  13  14  15
  //
  public int[][][] layering(int[][] paletteColors, byte[][] palette) {
    // Creating layers for each indexed color in arr
    int width = paletteColors[0].length;
    int height = paletteColors.length;
    int[][][] layers = new int[palette.length][height][width];

    // Looping through all pixels and calculating edge node type
    List<Supplier<Integer>> tasks = new ArrayList<>(height - 1);

    for (int row = 1; row < (height - 1); row++) {
      int finalRow = row;
      tasks.add(
          () -> {
            rowLayering(finalRow, width, paletteColors, layers);
            return finalRow;
          });
    } // End of row loop

    ParallelOperationUtils.executeTasks(tasks);

    return layers;
  } // End of layering()

  private static void rowLayering(int row, int width, int[][] paletteColors, int[][][] layers) {
    for (int col = 1; col < (width - 1); col++) {

      // This pixel's indexed color
      int val = paletteColors[row][col];

      // Are neighbor pixel colors the same?
      int n1 = paletteColors[row - 1][col - 1] == val ? 1 : 0;
      int n2 = paletteColors[row - 1][col] == val ? 1 : 0;
      int n3 = paletteColors[row - 1][col + 1] == val ? 1 : 0;
      int n4 = paletteColors[row][col - 1] == val ? 1 : 0;
      int n5 = paletteColors[row][col + 1] == val ? 1 : 0;
      int n6 = paletteColors[row + 1][col - 1] == val ? 1 : 0;
      int n7 = paletteColors[row + 1][col] == val ? 1 : 0;
      int n8 = paletteColors[row + 1][col + 1] == val ? 1 : 0;

      // this pixel"s type and looking back on previous pixels
      layers[val][row + 1][col + 1] = 1 + (n5 * 2) + (n8 * 4) + (n7 * 8);
      if (n4 == 0) {
        layers[val][row + 1][col] = 0 + 2 + (n7 * 4) + (n6 * 8);
      }
      if (n2 == 0) {
        layers[val][row][col + 1] = 0 + (n3 * 2) + (n5 * 4) + 8;
      }
      if (n1 == 0) {
        layers[val][row][col] = 0 + (n2 * 2) + 4 + (n4 * 8);
      }
    } // End of col loop
  }
}
