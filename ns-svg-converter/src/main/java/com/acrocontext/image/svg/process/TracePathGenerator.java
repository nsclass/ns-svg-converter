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

import com.acrocontext.image.svg.model.InterNodeList;
import com.acrocontext.image.svg.model.InterNodeListLayers;
import com.acrocontext.image.svg.model.TracePath;
import com.acrocontext.image.svg.model.TracePathLayers;
import com.acrocontext.image.svg.utils.ParallelOperationUtils;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Date 12/24/17
 *
 * @author Nam Seob Seo
 */

public class TracePathGenerator {
    // 5. createTracePath() : recursively trying to fit straight and quadratic spline segments on the 8 direction internode path

    // 5.1. Find sequences of points with only 2 segment types
    // 5.2. Fit a straight line on the sequence
    // 5.3. If the straight line fails (an error>ltreshold), find the point with the biggest error
    // 5.4. Fit a quadratic spline through errorpoint (project this to get controlpoint), then measure errors on every point in the sequence
    // 5.5. If the spline fails (an error>qtreshold), find the point with the biggest error, set splitpoint = (fitting point + errorpoint)/2
    // 5.6. Split sequence and recursively apply 5.2. - 5.7. to startpoint-splitpoint and splitpoint-endpoint sequences
    // 5.7. TODO? If splitpoint-endpoint is a spline, try to add new points from the next sequence

    // This returns an SVG Path segment as a double[7] where
    // segment[0] ==1.0 linear  ==2.0 quadratic interpolation
    // segment[1] , segment[2] : x1 , y1
    // segment[3] , segment[4] : x2 , y2 ; middle point of Q curve, endpoint of L line
    // segment[5] , segment[6] : x3 , y3 for Q curve, should be 0.0 , 0.0 for L line
    //
    // path type is discarded, no check for path.size < 3 , which should not happen

    private static TracePath createTracePath(InterNodeList path, float lThreshold, float qThreshold) {
        int pathIndex = 0, sequenceEnd;
        double sequenceType1, sequenceType2;
        ArrayList<Double[]> smp = new ArrayList<>();
        //Double [] thissegment;
        int pathLength = path.getInterNodes().length;

        while (pathIndex < pathLength) {
            // 5.1. Find sequences of points with only 2 segment types
            sequenceType1 = path.getInterNodes()[pathIndex].getPoint()[2];
            sequenceType2 = -1;
            sequenceEnd = pathIndex + 1;
            while (
                    ((path.getInterNodes()[sequenceEnd].getPoint()[2] == sequenceType1) ||
                            (path.getInterNodes()[sequenceEnd].getPoint()[2] == sequenceType2) || (sequenceType2 == -1))
                            && (sequenceEnd < (pathLength - 1))) {
                if ((path.getInterNodes()[sequenceEnd].getPoint()[2] != sequenceType1) && (sequenceType2 == -1)) {
                    sequenceType2 = path.getInterNodes()[sequenceEnd].getPoint()[2];
                }
                sequenceEnd++;
            }
            if (sequenceEnd == (pathLength - 1)) {
                sequenceEnd = 0;
            }

            // 5.2. - 5.6. Split sequence and recursively apply 5.2. - 5.6. to startPoint-splitPoint and splitPoint-endPoint sequences
            smp.addAll(fitSequence(path, lThreshold, qThreshold, pathIndex, sequenceEnd));
            // 5.7. TODO? If splitPoint-endPoint is a spline, try to add new points from the next sequence

            // forward pathIndex;
            if (sequenceEnd > 0) {
                pathIndex = sequenceEnd;
            } else {
                pathIndex = pathLength;
            }

        }

        return new TracePath(smp);

    }


    // 5.2. - 5.6. recursively fitting a straight or quadratic line segment on this sequence of path nodes,
    // called from createTracePath()
    private static ArrayList<Double[]> fitSequence(InterNodeList path, float lThreshold, float qThreshold, int seqStart, int seqEnd) {
        ArrayList<Double[]> segment = new ArrayList<>();
        Double[] thisSegment;
        int pathLength = path.getInterNodes().length;

        // return if invalid seqEnd
        if ((seqEnd > pathLength) || (seqEnd < 0)) {
            return segment;
        }

        int errorPoint = seqStart;
        boolean curvePass = true;
        double px, py, errorVal = 0;
        double tl = (seqEnd - seqStart);
        if (tl < 0) {
            tl += pathLength;
        }
        double vx = (path.valueAtWithSequenceIdx(seqEnd, 0) - path.valueAtWithSequenceIdx(seqStart, 0)) / tl;
        double vy = (path.valueAtWithSequenceIdx(seqEnd, 1) - path.valueAtWithSequenceIdx(seqStart, 1)) / tl;

        // 5.2. Fit a straight line on the sequence
        int pathIndex = (seqStart + 1) % pathLength;
        while (pathIndex != seqEnd) {
            double pl = pathIndex - seqStart;
            if (pl < 0) {
                pl += pathLength;
            }
            px = path.valueAtWithSequenceIdx(seqStart, 0) + (vx * pl);
            py = path.valueAtWithSequenceIdx(seqStart, 1) + (vy * pl);
            double dist2 = ((path.valueAtWithSequenceIdx(pathIndex, 0) - px) * (path.valueAtWithSequenceIdx(pathIndex, 0) - px)) +
                    ((path.valueAtWithSequenceIdx(pathIndex, 1) - py) * (path.valueAtWithSequenceIdx(pathIndex, 1) - py));
            if (dist2 > lThreshold) {
                curvePass = false;
            }
            if (dist2 > errorVal) {
                errorPoint = pathIndex;
                errorVal = dist2;
            }
            pathIndex = (pathIndex + 1) % pathLength;
        }

        // return straight line if fits
        if (curvePass) {
            segment.add(new Double[7]);
            thisSegment = segment.get(segment.size() - 1);
            thisSegment[0] = 1.0;
            thisSegment[1] = path.valueAtWithSequenceIdx(seqStart, 0);
            thisSegment[2] = path.valueAtWithSequenceIdx(seqStart, 1);
            thisSegment[3] = path.valueAtWithSequenceIdx(seqEnd, 0);
            thisSegment[4] = path.valueAtWithSequenceIdx(seqEnd, 1);
            thisSegment[5] = 0.0;
            thisSegment[6] = 0.0;
            return segment;
        }

        // 5.3. If the straight line fails (an error>lThreshold), find the point with the biggest error
        int fitPoint = errorPoint;
        curvePass = true;
        errorVal = 0;

        // 5.4. Fit a quadratic spline through this point, measure errors on every point in the sequence
        // helpers and projecting to get control point
        double t = (fitPoint - seqStart) / tl, t1 = (1.0 - t) * (1.0 - t), t2 = 2.0 * (1.0 - t) * t, t3 = t * t;
        double cpx = (((t1 * path.valueAtWithSequenceIdx(seqStart, 0)) + (t3 * path.valueAtWithSequenceIdx(seqEnd, 0))) - path.valueAtWithSequenceIdx(fitPoint, 0)) / -t2;
        double cpy = (((t1 * path.valueAtWithSequenceIdx(seqStart, 1)) + (t3 * path.valueAtWithSequenceIdx(seqEnd, 1))) - path.valueAtWithSequenceIdx(fitPoint, 1)) / -t2;

        // Check every point
        pathIndex = seqStart + 1;
        while (pathIndex != seqEnd) {

            t = (pathIndex - seqStart) / tl;
            t1 = (1.0 - t) * (1.0 - t);
            t2 = 2.0 * (1.0 - t) * t;
            t3 = t * t;
            px = (t1 * path.valueAtWithSequenceIdx(seqStart, 0)) + (t2 * cpx) + (t3 * path.valueAtWithSequenceIdx(seqEnd, 0));
            py = (t1 * path.valueAtWithSequenceIdx(seqStart, 1)) + (t2 * cpy) + (t3 * path.valueAtWithSequenceIdx(seqEnd, 1));

            double dist2 = ((path.valueAtWithSequenceIdx(pathIndex, 0) - px) * (path.valueAtWithSequenceIdx(pathIndex, 0) - px)) +
                    ((path.valueAtWithSequenceIdx(pathIndex, 1) - py) * (path.valueAtWithSequenceIdx(pathIndex, 1) - py));

            if (dist2 > qThreshold) {
                curvePass = false;
            }
            if (dist2 > errorVal) {
                errorPoint = pathIndex;
                errorVal = dist2;
            }
            pathIndex = (pathIndex + 1) % pathLength;
        }

        // return spline if fits
        if (curvePass) {
            segment.add(new Double[7]);
            thisSegment = segment.get(segment.size() - 1);
            thisSegment[0] = 2.0;
            thisSegment[1] = path.valueAtWithSequenceIdx(seqStart, 0);
            thisSegment[2] = path.valueAtWithSequenceIdx(seqStart, 1);
            thisSegment[3] = cpx;
            thisSegment[4] = cpy;
            thisSegment[5] = path.valueAtWithSequenceIdx(seqEnd, 0);
            thisSegment[6] = path.valueAtWithSequenceIdx(seqEnd, 1);
            return segment;
        }

        // 5.5. If the spline fails (an error>qThreshold), find the point with the biggest error,
        // set splitPoint = (fitting point + errorPoint)/2
        int splitPoint = (fitPoint + errorPoint) / 2;

        // 5.6. Split sequence and recursively apply 5.2. - 5.6. to startPoint-splitPoint and splitPoint-endpoint sequences
        segment = fitSequence(path, lThreshold, qThreshold, seqStart, splitPoint);
        segment.addAll(fitSequence(path, lThreshold, qThreshold, splitPoint, seqEnd));
        return segment;

    }


    // 5. Batch tracing paths
    private static TracePath[] batchTracePaths(InterNodeListLayers interNodePaths, float lThreshold, float qThreshold) {
        TracePath[] batchTracePathResult = new TracePath[interNodePaths.getInterNodeLists().length];

        int idx = 0;
        for (InterNodeList interNodePath : interNodePaths.getInterNodeLists()) {
            batchTracePathResult[idx] = createTracePath(interNodePath, lThreshold, qThreshold);
            ++idx;
        }
        return batchTracePathResult;
    }

    // 5. Batch tracing layers
    public TracePathLayers[] createBatchTracePathList(InterNodeListLayers[] batchInterNodes, float lThreshold, float qThreshold) {
        List<Supplier<Pair<Integer, TracePathLayers>>> tasks = new ArrayList<>(batchInterNodes.length);

        IntStream.range(0, batchInterNodes.length)
                .forEach(idx -> {
                    tasks.add(() -> {
                        TracePathLayers tracePathLayers = new TracePathLayers(batchTracePaths(batchInterNodes[idx], lThreshold, qThreshold));
                        return Pair.of(idx, tracePathLayers);
                    });

                });

        return ParallelOperationUtils.execute(TracePathLayers.class, tasks);

    }

}
