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

import com.acrocontext.image.svg.model.InterNode;
import com.acrocontext.image.svg.model.InterNodeList;
import com.acrocontext.image.svg.model.InterNodeListLayers;
import com.acrocontext.image.svg.model.Path;
import com.acrocontext.image.svg.model.ScanPath;
import com.acrocontext.image.svg.utils.ParallelOperationUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Date 12/24/17
 *
 * @author Nam Seob Seo
 */
public class InterNodeGenerator {

  private static InterNode[] createInterNode(Path path) {

    int pathLen = path.getPath().size();
    InterNode[] nodes = new InterNode[pathLen];

    Double[] nextPoint = new Double[2];

    // pathPoints loop
    for (int pathIndex = 0; pathIndex < pathLen; pathIndex++) {

      // interpolate between two path points
      int nextIdx = (pathIndex + 1) % pathLen;
      int nextIdx2 = (pathIndex + 2) % pathLen;
      double[] thisPoint = new double[3];

      Integer[] pp1 = path.indexAt(pathIndex);
      Integer[] pp2 = path.indexAt(nextIdx);
      Integer[] pp3 = path.indexAt(nextIdx2);

      thisPoint[0] = (pp1[0] + pp2[0]) / 2.0;
      thisPoint[1] = (pp1[1] + pp2[1]) / 2.0;

      nextPoint[0] = (pp2[0] + pp3[0]) / 2.0;
      nextPoint[1] = (pp2[1] + pp3[1]) / 2.0;

      // line segment direction to the next point
      if (thisPoint[0] < nextPoint[0]) {
        if (thisPoint[1] < nextPoint[1]) {
          thisPoint[2] = 1.0;
        } // SouthEast
        else if (thisPoint[1] > nextPoint[1]) {
          thisPoint[2] = 7.0;
        } // NE
        else {
          thisPoint[2] = 0.0;
        } // E
      } else if (thisPoint[0] > nextPoint[0]) {
        if (thisPoint[1] < nextPoint[1]) {
          thisPoint[2] = 3.0;
        } // SW
        else if (thisPoint[1] > nextPoint[1]) {
          thisPoint[2] = 5.0;
        } // NW
        else {
          thisPoint[2] = 4.0;
        } // W
      } else {
        if (thisPoint[1] < nextPoint[1]) {
          thisPoint[2] = 2.0;
        } // S
        else if (thisPoint[1] > nextPoint[1]) {
          thisPoint[2] = 6.0;
        } // N
        else {
          thisPoint[2] = 8.0;
        } // center, this should not happen
      }

      nodes[pathIndex] = new InterNode(thisPoint);
    }

    return nodes;
  }

  // 4. interpolating between path points for nodes with 8 directions ( E, SE, S, SW, W, NW, N, NE )
  private static InterNodeList[] createInterNodeList(ScanPath scanPath) {
    List<Supplier<Pair<Integer, InterNodeList>>> tasks =
        new ArrayList<>(scanPath.getScanPath().size());

    // paths loop
    IntStream.range(0, scanPath.getScanPath().size())
        .forEach(
            idx -> {
              tasks.add(
                  () -> {
                    InterNodeList interNodeList =
                        new InterNodeList(createInterNode(scanPath.getScanPath().get(idx)));
                    return Pair.of(idx, interNodeList);
                  });
            });

    return ParallelOperationUtils.execute(InterNodeList.class, tasks);
  } // End of createInterNodeList()

  // 4. Batch interpolation
  public InterNodeListLayers[] createInterNodeListLayers(ScanPath[] scanPaths) {

    List<Supplier<Pair<Integer, InterNodeListLayers>>> tasks = new ArrayList<>(scanPaths.length);

    IntStream.range(0, scanPaths.length)
        .forEach(
            idx -> {
              tasks.add(
                  () -> {
                    InterNodeListLayers interNodeListLayers =
                        new InterNodeListLayers(createInterNodeList(scanPaths[idx]));
                    return Pair.of(idx, interNodeListLayers);
                  });
            });

    return ParallelOperationUtils.execute(InterNodeListLayers.class, tasks);
  }
}
