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

import com.acrocontext.image.svg.model.Path;
import com.acrocontext.image.svg.model.ScanPath;
import com.acrocontext.image.svg.utils.ParallelOperationUtils;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Date 12/23/17
 *
 * @author Nam Seob Seo
 */

public class ScanPathGenerator {

    // Lookup tables for createScanPath
    private static final byte[] PATH_SCAN_DIR_LOOKUP = {0, 0, 3, 0, 1, 0, 3, 0, 0, 3, 3, 1, 0, 3, 0, 0};
    private static final boolean[] PATH_SCAN_HOLE_PATH_LOOKUP = {false, false, false, false, false, false, false, true, false, false, false, true, false, true, true, false};
    // PATH_SCAN_COMBINED_LOOKUP[ arr[py][px] ][ dir ] = [nextArrayPyPx, nextDir, deltaPx, deltaPy];
    private static final byte[][][] PATH_SCAN_COMBINED_LOOKUP = {
            {{-1, -1, -1, -1}, {-1, -1, -1, -1}, {-1, -1, -1, -1}, {-1, -1, -1, -1}},// arr[py][px]==0 is invalid
            {{0, 1, 0, -1}, {-1, -1, -1, -1}, {-1, -1, -1, -1}, {0, 2, -1, 0}},
            {{-1, -1, -1, -1}, {-1, -1, -1, -1}, {0, 1, 0, -1}, {0, 0, 1, 0}},
            {{0, 0, 1, 0}, {-1, -1, -1, -1}, {0, 2, -1, 0}, {-1, -1, -1, -1}},

            {{-1, -1, -1, -1}, {0, 0, 1, 0}, {0, 3, 0, 1}, {-1, -1, -1, -1}},
            {{13, 3, 0, 1}, {13, 2, -1, 0}, {7, 1, 0, -1}, {7, 0, 1, 0}},
            {{-1, -1, -1, -1}, {0, 1, 0, -1}, {-1, -1, -1, -1}, {0, 3, 0, 1}},
            {{0, 3, 0, 1}, {0, 2, -1, 0}, {-1, -1, -1, -1}, {-1, -1, -1, -1}},

            {{0, 3, 0, 1}, {0, 2, -1, 0}, {-1, -1, -1, -1}, {-1, -1, -1, -1}},
            {{-1, -1, -1, -1}, {0, 1, 0, -1}, {-1, -1, -1, -1}, {0, 3, 0, 1}},
            {{11, 1, 0, -1}, {14, 0, 1, 0}, {14, 3, 0, 1}, {11, 2, -1, 0}},
            {{-1, -1, -1, -1}, {0, 0, 1, 0}, {0, 3, 0, 1}, {-1, -1, -1, -1}},

            {{0, 0, 1, 0}, {-1, -1, -1, -1}, {0, 2, -1, 0}, {-1, -1, -1, -1}},
            {{-1, -1, -1, -1}, {-1, -1, -1, -1}, {0, 1, 0, -1}, {0, 0, 1, 0}},
            {{0, 1, 0, -1}, {-1, -1, -1, -1}, {-1, -1, -1, -1}, {0, 2, -1, 0}},
            {{-1, -1, -1, -1}, {-1, -1, -1, -1}, {-1, -1, -1, -1}, {-1, -1, -1, -1}}// arr[py][px]==15 is invalid
    };
    // 3. Walking through an edge node array, discarding edge node types 0 and 15 and creating paths from the rest.
    // Walk directions (dir): 0 > ; 1 ^ ; 2 < ; 3 v
    // Edge node types ( ▓:light or 1; ░:dark or 0 )
    // ░░  ▓░  ░▓  ▓▓  ░░  ▓░  ░▓  ▓▓  ░░  ▓░  ░▓  ▓▓  ░░  ▓░  ░▓  ▓▓
    // ░░  ░░  ░░  ░░  ░▓  ░▓  ░▓  ░▓  ▓░  ▓░  ▓░  ▓░  ▓▓  ▓▓  ▓▓  ▓▓
    // 0   1   2   3   4   5   6   7   8   9   10  11  12  13  14  15
    //
    private static ScanPath createScanPath(int[][] layer, float pathOmit) {

        int width = layer[0].length;
        int height = layer.length;

        Path[] pathArray = new Path[width * height];

        for (int row = 0; row < height; row++) {
            rowScanPathOperation(row, width, layer, pathOmit, pathArray);
        }// End of row loop

        List<Path> paths = new ArrayList<>();

        for (Path path : pathArray) {
            if (path != null) {
                paths.add(path);
            }
        }
        return new ScanPath(paths);
    }

    private static void rowScanPathOperation(int row,
                                             int width,
                                             int[][] layer,
                                             float pathOmit,
                                             Path[] pathArray) {
        for (int col = 0; col < width; col++) {
            if ((layer[row][col] != 0) && (layer[row][col] != 15)) {

                int currentIdx = row * width + col;

                // Init
                int px = col;
                int py = row;

                List<Integer[]> thisPath = new ArrayList<>();
                boolean pathFinished = false;

                // fill paths will be drawn, but hole paths are also required to remove unnecessary edge nodes
                int dir = PATH_SCAN_DIR_LOOKUP[layer[py][px]];
                boolean holePath = PATH_SCAN_HOLE_PATH_LOOKUP[layer[py][px]];

                // Path points loop
                while (!pathFinished) {

                    // New path point
                    Integer[] thisPoint = new Integer[3];

                    thisPoint[0] = px - 1;
                    thisPoint[1] = py - 1;
                    thisPoint[2] = layer[py][px];

                    thisPath.add(thisPoint);


                    // Next: look up the replacement, direction and coordinate changes = clear this cell, turn if required, walk forward
                    byte[] lookupRow = PATH_SCAN_COMBINED_LOOKUP[layer[py][px]][dir];
                    layer[py][px] = lookupRow[0];
                    dir = lookupRow[1];
                    px += lookupRow[2];
                    py += lookupRow[3];

                    // Close path
                    if (((px - 1) == thisPath.get(0)[0]) && ((py - 1) == thisPath.get(0)[1])) {
                        pathFinished = true;
                        // Discarding 'hole' type paths and paths shorter than pathOmit
                        if ((!holePath) && (thisPath.size() >= pathOmit)) {
                            pathArray[currentIdx] = new Path(thisPath);
                        }
                    }

                }
            }
        }
    }

    // 3. Batch createScanPath
    public ScanPath[] createBatchScanPath(int[][][] layers, float pathOmit) {
        List<Supplier<Pair<Integer, ScanPath>>> tasks = new ArrayList<>(layers.length);

        IntStream.range(0, layers.length)
                .forEach(idx -> {
                    tasks.add(() -> {
                        ScanPath scanPath = createScanPath(layers[idx], pathOmit);
                        return Pair.of(idx, scanPath);
                    });

                });

        return ParallelOperationUtils.execute(ScanPath.class, tasks);
    }

}
