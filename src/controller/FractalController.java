package controller;

import fractal.FractalDepthPainter;
import fractal.FractalFunction;
import mandelbrot.MandelbrotFractalEvaluator;
import math.CircleArea;
import math.Mfloat;
import math.Number;
import repository.DepthPaintersRepository;
import view.main_frame.MainFrame;
import view.main_frame.MainFrameEventListener;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static controller.FractalConstants.*;

public final class FractalController implements MainFrameEventListener {

    private MainFrame frame;
    private FractalWorkerController workerController;
    private DepthPaintersRepository paintersRepository;
    private FractalImageController imageController;
    private int currentPainterIndex;
    private final MandelbrotFractalEvaluator evaluator;

    private static MandelbrotFractalEvaluator buildEvaluator(int maxDepth, Mfloat edge,
                                                             FractalDepthPainter painter, FractalFunction function) {
        return new MandelbrotFractalEvaluator(maxDepth, edge, painter, function);
    }

    public FractalController(MainFrame frame, FractalWorkerController workerController,
                             DepthPaintersRepository paintersRepository, FractalImageController imageController) {
        this.frame = frame;
        this.workerController = workerController;
        this.paintersRepository = paintersRepository;
        this.imageController = imageController;
        this.currentPainterIndex = 0;
        this.evaluator = buildEvaluator(
                INITIAL_MAX_DEPTH,
                Number.buildFloat(INITIAL_EDGE),
                paintersRepository.getPainter(currentPainterIndex),
                INITIAL_FRACTAL_FUNCTION
        );
        this.imageController.addFractalImageUpdateListener(this::updateFrameCircleAreaLabels);

        this.workerController.updateEvaluator(evaluator);

        frame.addListener(this);
        initFields();
    }

    private void initFields() {
        // Coordinates
        CircleArea fractalArea = imageController.getFractalArea();
        double pixelScale = imageController.getPixelScale();
        int depth = evaluator.getDepth();
        Mfloat edge = evaluator.getEdge();

        SwingUtilities.invokeLater(() -> {
            frame.setXField(fractalArea.getCenterX().toString());
            frame.setYField(fractalArea.getCenterY().toString());
            frame.setFractalViewSizeField(fractalArea.getDiameter().toString());

            frame.setPixelScaleField(String.valueOf(pixelScale));
            frame.setFractalDepthField(String.valueOf(depth));
            frame.setFractalEdgeField(String.valueOf(edge));
        });
    }

    @Override
    public void updateClicked() {
        SwingUtilities.invokeLater(() -> {
            try {
                double pixelScale = Double.parseDouble(frame.getPixelScaleField());
                int depth = Integer.parseInt(frame.getFractalDepthField());
                Mfloat edge = Number.buildFloat(frame.getFractalEdgeField());
                FractalDepthPainter depthPainter = paintersRepository.getDepthPainter(
                        frame.getCurrentDepthPainterName());

                MandelbrotFractalEvaluator newEvaluator = buildEvaluator(depth, edge, depthPainter,
                        evaluator.getFunction()     // TODO: Implement function update feature
                );

                imageController.setPixelScale(pixelScale);
                workerController.updateEvaluator(newEvaluator);
            } catch (NumberFormatException e) {
                System.out.println(String.format("Number field format error: %s", e));
            }
        });
    }

    private void updateFrameCircleAreaLabels(CircleArea area) {
        frame.setXField(area.getCenterX().toString());
        frame.setYField(area.getCenterY().toString());
        frame.setFractalViewSizeField(area.getDiameter().toString());
    }

    private void updateCircleArea(CircleArea area) {
        updateFrameCircleAreaLabels(area);
        imageController.setFractalArea(area);
    }

    @Override
    public void moveClicked() {
        SwingUtilities.invokeLater(() -> {
            try {
                Mfloat centerX = Number.buildFloat(frame.getXField());
                Mfloat centerY = Number.buildFloat(frame.getYField());
                Mfloat diameter = Number.buildFloat(frame.getFractalViewSizeField());
                CircleArea area = new CircleArea(centerX, centerY, diameter);
                updateCircleArea(area);
            } catch (NumberFormatException e) {
                System.out.println(String.format("Number format error: %s", e));
            }
        });
    }

    @Override
    public void resetZoomClicked() {
        SwingUtilities.invokeLater(() -> {
            try {
                updateCircleArea(INITIAL_AREA);
            } catch (NumberFormatException e) {
                System.out.println(String.format("Number format error: %s", e));
            }
        });
    }

    @Override
    public void fractalPainterChanged(String painterName) {
        System.out.println("Painter changed to : " + painterName);
        // TODO: Implement
    }

    @Override
    public void saveImage(File file) {
        new Thread(() -> {
            try {
                BufferedImage image = imageController.getCurrentImage();
                ImageIO.write(image, "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
