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

package com.acrocontext.image.svg;

import com.acrocontext.image.svg.model.*;
import com.acrocontext.image.svg.process.*;
import com.acrocontext.image.svg.utils.OperationManager;
import com.acrocontext.image.svg.utils.OperationProgressListener;
import lombok.Data;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Date 12/21/17
 *
 * @author Nam Seob Seo
 */

@Slf4j
@Service
public class ImageSvgConverter {

    @Data
    private static class ImageSvgConvertCtx {
        private final ImageConvertOptions options;
        private ImageData imageData;
        private int[][] paletteColors;
        private byte[][] palette;
        private int[][][] rawLayers;

        private IndexedImage indexedImage;
        private String svgString;
        private com.acrocontext.image.svg.model.ScanPath[] batchPathScan;
        private InterNodeListLayers[] batchInterNodes;

        public ImageSvgConvertCtx(ImageConvertOptions options) {
            this.options = options;
        }
    }


    public void convertImageToSVG(String inputFilename,
                                  String outputFilename,
                                  ImageConvertOptions options,
                                  OperationProgressListener listener) {

        ImageSvgConvertCtx convertCtx = new ImageSvgConvertCtx(options);

        OperationManager<ImageSvgConvertCtx> operationManager = new OperationManager<>(listener);

        imageToSVG(operationManager, inputFilename)
                .addOperation("save it to file", ctx -> {
                    saveString(outputFilename, ctx.getSvgString());
                    return ctx;
                })
                .execute(convertCtx);
    }


    public String convertImageToSVG(BufferedImage bufferedImage,
                                    ImageConvertOptions options,
                                    OperationProgressListener listener) {

        ImageSvgConvertCtx convertCtx = new ImageSvgConvertCtx(options);

        OperationManager<ImageSvgConvertCtx> operationManager = new OperationManager<>(listener);

        return imageToSVG(operationManager, bufferedImage)
                .execute(convertCtx).getSvgString();
    }


    // Container for the color-indexed image before and traceData after vectorizing
    @Value
    static class IndexedImage {
        private final int width;
        private final int height;
        private final int[][] paletteColors; // array[x][y] of palette colors
        private final byte[][] palette;// array[paletteLength][4] RGBA color palette
        private final TracePathLayers[] layers;// traceData

        IndexedImage(int[][] paletteColors, byte[][] palette, TracePathLayers[] layers) {
            this.paletteColors = paletteColors;
            this.palette = palette;
            this.width = paletteColors[0].length - 2;
            this.height = paletteColors.length - 2;// Color quantization adds +2 to the original width and height
            this.layers = layers;
        }

        double valueAtIdx(int idx1, int idx2, int idx3, int idx4) {
            return layers[idx1].valueAt(idx2, idx3, idx4);
        }

        int pathSizeAtIdx(int idx) {
            return layers[idx].getTracePaths().length;
        }

        TracePath tracePathAtIdx(int idx1, int idx2) {
            return layers[idx1].tracePathAt(idx2);
        }
    }


    // https://developer.mozilla.org/en-US/docs/Web/API/ImageData

    // Saving a String as a file
    public static void saveString(String filename, String str) {
        try {
            File file = new File(filename);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(str);
            bw.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }


    // Loading a file to ImageData, ARGB byte order
    private OperationManager<ImageSvgConvertCtx> loadImageData(OperationManager<ImageSvgConvertCtx> operationManager,
                                                               String filename) {

        operationManager.addOperation("loading image data", (ImageSvgConvertCtx ctx) -> {
            try {
                BufferedImage image = ImageIO.read(new File(filename));
                ctx.setImageData(loadImageData(image));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }

            return ctx;
        });

        return operationManager;
    }

    private ImageData loadImageData(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] rawData = image.getRGB(0, 0, width, height, null, 0, width);
        byte[] data = new byte[rawData.length * 4];
        for (int idx = 0; idx < rawData.length; idx++) {
            data[(idx * 4) + 3] = byteTrans((byte) ((rawData[idx] & 0xff000000) >>> 24)); // alpha
            data[(idx * 4) + 0] = byteTrans((byte) ((rawData[idx] & 0xff0000) >>> 16));   // red
            data[(idx * 4) + 1] = byteTrans((byte) ((rawData[idx] & 0xff00) >>> 8));      // green
            data[(idx * 4) + 2] = byteTrans((byte) ((rawData[idx] & 0xff)));              // blue
        }
        return new ImageData(width, height, data);
    }


    // The bitShift method in loadImageData creates signed bytes where -1 -> 255 unsigned ; -128 -> 128 unsigned ;
    // 127 -> 127 unsigned ; 0 -> 0 unsigned ; These will be converted to -128 (representing 0 unsigned) ...
    // 127 (representing 255 unsigned) and toSvgColorStr will add +128 to create RGB values 0..255
    private static byte byteTrans(byte b) {
        if (b < 0) {
            return (byte) (b + 128);
        } else {
            return (byte) (b - 128);
        }
    }


    ////////////////////////////////////////////////////////////
    //
    //  User friendly functions
    //
    ////////////////////////////////////////////////////////////

    // Loading an image from a file, tracing when loaded, then returning the SVG String
    private OperationManager<ImageSvgConvertCtx> imageToSVG(OperationManager<ImageSvgConvertCtx> operationManager,
                                                            String filename) {
        loadImageData(operationManager, filename);

        return imageDataToSVG(operationManager);
    }// End of imageToSVG()


    private OperationManager<ImageSvgConvertCtx> imageToSVG(OperationManager<ImageSvgConvertCtx> operationManager,
                                                            BufferedImage bufferedImage) {

        operationManager.addOperation("loading an image from buffer", ctx -> {
            ctx.setImageData(loadImageData(bufferedImage));
            return ctx;
        });

        return imageDataToSVG(operationManager);
    }// End of imageToSVG()


    // Tracing ImageData, then returning the SVG String
    private OperationManager<ImageSvgConvertCtx> imageDataToSVG(OperationManager<ImageSvgConvertCtx> operationManager) {
        return imageDataToTraceData(operationManager)
                .addOperation("creating svg string", ctx -> {
                    ctx.svgString = getSvgString(ctx.getIndexedImage(), ctx.getOptions());
                    return ctx;
                });
    }

    // Tracing ImageData, then returning IndexedImage with traceData in layers
    private OperationManager<ImageSvgConvertCtx> imageDataToTraceData(OperationManager<ImageSvgConvertCtx> operationManager) {
        // 0. Generate palette
        PaletteGenerator paletteGenerator = new PaletteGenerator();
        operationManager.addOperation("generating palette", (ctx) -> {
            byte[][] palette = paletteGenerator.generatePalette(ctx.getImageData(), ctx.getOptions());

            ctx.setPalette(palette);
            return ctx;
        });

        // 1. Color quantization
        ColorQuantizator colorQuantizator = new ColorQuantizator();
        operationManager.addOperation("color quantization", ctx -> {
            int[][] paletteColors = colorQuantizator.colorQuantization(ctx.getImageData(), ctx.getPalette(), ctx.getOptions());
            ctx.setPaletteColors(paletteColors);
            return ctx;
        });

        // 2. Layer separation and edge detection
        LayerGenerator layerGenerator = new LayerGenerator();
        operationManager.addOperation("layer separation and edge detection", ctx -> {
            ctx.rawLayers = layerGenerator.layering(ctx.getPaletteColors(), ctx.getPalette());

            return ctx;
        });

        // 3. Batch createScanPath
        ScanPathGenerator scanPathGenerator = new ScanPathGenerator();
        operationManager.addOperation("batch create scan paths", ctx -> {
            ctx.batchPathScan = scanPathGenerator.createBatchScanPath(ctx.getRawLayers(), ctx.getOptions().getPathOmit());
            return ctx;
        });

        // 4. Batch interpolation
        InterNodeGenerator interNodeGenerator = new InterNodeGenerator();
        operationManager.addOperation("batch interpolation", ctx -> {
            ctx.batchInterNodes = interNodeGenerator.createInterNodeListLayers(ctx.getBatchPathScan());
            return ctx;
        });

        // 5. Batch tracing
        TracePathGenerator tracePathGenerator = new TracePathGenerator();
        operationManager.addOperation("batch tracing", ctx -> {
            TracePathLayers[] layers = tracePathGenerator.createBatchTracePathList(ctx.getBatchInterNodes(),
                    ctx.getOptions().getLThreshold(), ctx.getOptions().getQThreshold());
            ctx.indexedImage = new IndexedImage(ctx.getPaletteColors(), ctx.getPalette(), layers);

            return ctx;
        });

        return operationManager;

    }

    ////////////////////////////////////////////////////////////
    //
    //  SVG Drawing functions
    //
    ////////////////////////////////////////////////////////////

    private static float roundToDec(float val, float places) {
        return (float) (Math.round(val * Math.pow(10, places)) / Math.pow(10, places));
    }

    // Getting SVG path element string from a traced path
    private void generateSvgPathFromTracePaths(StringBuilder sb, String desc, TracePath segments, String colorStr, ImageConvertOptions options) {
        float scale = options.getScale();
        float lcpr = options.getLCpr();
        float qcpr = options.getQCpr();
        float roundCoords = (float) Math.floor(options.getRoundCoords());

        // Path
        sb.append("<path ")
                .append(desc)
                .append(colorStr)
                .append("d=\"")
                .append("M ")
                .append(segments.valueAt(0, 1) * scale)
                .append(" ")
                .append(segments.valueAt(0, 2) * scale).append(" ");

        if (roundCoords == -1) {
            for (Double[] segment : segments.getTracePath()) {
                if (segment[0] == 1.0) {
                    sb.append("L ")
                            .append(segment[3] * scale)
                            .append(" ")
                            .append(segment[4] * scale)
                            .append(" ");
                } else {
                    sb.append("Q ")
                            .append(segment[3] * scale)
                            .append(" ")
                            .append(segment[4] * scale)
                            .append(" ")
                            .append(segment[5] * scale)
                            .append(" ")
                            .append(segment[6] * scale)
                            .append(" ");
                }
            }
        } else {
            for (Double[] segment : segments.getTracePath()) {
                if (segment[0] == 1.0) {
                    sb.append("L ")
                            .append(roundToDec((float) (segment[3] * scale), roundCoords))
                            .append(" ")
                            .append(roundToDec((float) (segment[4] * scale), roundCoords))
                            .append(" ");
                } else {
                    sb.append("Q ")
                            .append(roundToDec((float) (segment[3] * scale), roundCoords))
                            .append(" ")
                            .append(roundToDec((float) (segment[4] * scale), roundCoords))
                            .append(" ")
                            .append(roundToDec((float) (segment[5] * scale), roundCoords))
                            .append(" ")
                            .append(roundToDec((float) (segment[6] * scale), roundCoords))
                            .append(" ");
                }
            }
        }// End of roundCoords check

        sb.append("Z\" />");

        // Rendering control points
        for (Double[] segment : segments.getTracePath()) {
            if ((lcpr > 0) && (segment[0] == 1.0)) {
                sb.append("<circle cx=\"")
                        .append(segment[3] * scale)
                        .append("\" cy=\"")
                        .append(segment[4] * scale)
                        .append("\" r=\"")
                        .append(lcpr)
                        .append("\" fill=\"white\" stroke-width=\"")
                        .append(lcpr * 0.2)
                        .append("\" stroke=\"black\" />");
            }
            if ((qcpr > 0) && (segment[0] == 2.0)) {
                sb.append("<circle cx=\"")
                        .append(segment[3] * scale)
                        .append("\" cy=\"")
                        .append(segment[4] * scale)
                        .append("\" r=\"")
                        .append(qcpr)
                        .append("\" fill=\"cyan\" stroke-width=\"")
                        .append(qcpr * 0.2)
                        .append("\" stroke=\"black\" />");
                sb.append("<circle cx=\"")
                        .append(segment[5] * scale)
                        .append("\" cy=\"")
                        .append(segment[6] * scale)
                        .append("\" r=\"")
                        .append(qcpr)
                        .append("\" fill=\"white\" stroke-width=\"")
                        .append(qcpr * 0.2)
                        .append("\" stroke=\"black\" />");
                sb.append("<line x1=\"")
                        .append(segment[1] * scale)
                        .append("\" y1=\"")
                        .append(segment[2] * scale)
                        .append("\" x2=\"")
                        .append(segment[3] * scale)
                        .append("\" y2=\"")
                        .append(segment[4] * scale)
                        .append("\" stroke-width=\"")
                        .append(qcpr * 0.2)
                        .append("\" stroke=\"cyan\" />");
                sb.append("<line x1=\"")
                        .append(segment[3] * scale)
                        .append("\" y1=\"")
                        .append(segment[4] * scale)
                        .append("\" x2=\"")
                        .append(segment[5] * scale)
                        .append("\" y2=\"")
                        .append(segment[6] * scale)
                        .append("\" stroke-width=\"")
                        .append(qcpr * 0.2)
                        .append("\" stroke=\"cyan\" />");
            }// End of quadratic control points
        }

    }// End of generateSvgPathFromTracePaths()


    // Converting traceData to an SVG string, paths are drawn according to a Z-index
    // the optional lCpr and qCpr are linear and quadratic control point radiuses
    private String getSvgString(IndexedImage indexedImage, ImageConvertOptions options) {
        // SVG start
        int w = (int) (indexedImage.width * options.getScale()), h = (int) (indexedImage.height * options.getScale());

        String viewBoxOrViewPort = options.getViewBox() != 0 ? "viewBox=\"0 0 " + w + " " + h + "\" " : "width=\"" + w + "\" height=\"" + h + "\" ";
        StringBuilder svgStringBuilder = new StringBuilder("<svg " + viewBoxOrViewPort + "version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" ");
        if (options.isShowDescription()) {
            svgStringBuilder.append("desc=\"Created by an image SVG converter\"");
        }
        svgStringBuilder.append(">");

        // creating Z-index
        TreeMap<Double, Integer[]> zIndex = new TreeMap<>();
        // Layer loop
        for (int layerIdx = 0; layerIdx < indexedImage.layers.length; layerIdx++) {

            // Path loop
            for (int pathCount = 0; pathCount < indexedImage.pathSizeAtIdx(layerIdx); pathCount++) {

                // Label (Z-index key) is the startPoint of the path, linearized
                double label = (indexedImage.valueAtIdx(layerIdx, pathCount, 0, 2) * w) + indexedImage.valueAtIdx(layerIdx, pathCount, 0, 1);
                // Creating new list if required
                if (!zIndex.containsKey(label)) {
                    zIndex.put(label, new Integer[2]);
                }
                // Adding layer and path number to list
                zIndex.get(label)[0] = layerIdx;
                zIndex.get(label)[1] = pathCount;
            }// End of path loop

        }// End of layer loop

        // Sorting Z-index is not required, TreeMap is sorted automatically

        // Drawing
        // Z-index loop
        String thisDescription;
        for (Map.Entry<Double, Integer[]> entry : zIndex.entrySet()) {
            if (options.isShowDescription()) {
                thisDescription = "desc=\"l " + entry.getValue()[0] + " p " + entry.getValue()[1] + "\" ";
            } else {
                thisDescription = "";
            }
            generateSvgPathFromTracePaths(svgStringBuilder,
                    thisDescription,
                    indexedImage.tracePathAtIdx(entry.getValue()[0], entry.getValue()[1]),
                    toSvgColorStr(indexedImage.palette[entry.getValue()[0]]),
                    options);
        }

        // SVG End
        svgStringBuilder.append("</svg>");

        return svgStringBuilder.toString();

    }

    private static String toSvgColorStr(byte[] c) {
        return "fill=\"rgb(" + (c[0] + 128) + "," + (c[1] + 128) + "," + (c[2] + 128) + ")\" stroke=\"rgb(" + (c[0] + 128) + "," + (c[1] + 128) + "," + (c[2] + 128) + ")\" stroke-width=\"1\" opacity=\"" + ((c[3] + 128) / 255.0) + "\" ";
    }
}
