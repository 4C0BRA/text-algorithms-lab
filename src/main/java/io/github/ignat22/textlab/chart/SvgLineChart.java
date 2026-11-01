package io.github.ignat22.textlab.chart;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class SvgLineChart {

    private static final String[] SERIES_COLORS = {"#2a78d6", "#eb6834", "#1baf7a", "#6250d6"};
    private static final String SURFACE = "#fcfcfb";
    private static final String TEXT_PRIMARY = "#0b0b0b";
    private static final String TEXT_SECONDARY = "#52514e";
    private static final String GRID = "#e4e3df";
    private static final String AXIS = "#b5b4ae";

    private static final int WIDTH = 760;
    private static final int HEIGHT = 440;
    private static final int LEFT = 78;
    private static final int RIGHT = 150;
    private static final int TOP = 74;
    private static final int BOTTOM = 62;

    private final String title;
    private final String subtitle;
    private final String xLabel;
    private final String yLabel;
    private final List<Series> series = new ArrayList<>();

    private record Series(String name, double[] x, double[] y, boolean dashed) {
    }

    public SvgLineChart(String title, String subtitle, String xLabel, String yLabel) {
        this.title = title;
        this.subtitle = subtitle;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
    }

    public SvgLineChart add(String name, double[] x, double[] y) {
        return add(name, x, y, false);
    }

    public SvgLineChart add(String name, double[] x, double[] y, boolean dashed) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("x and y must have the same length");
        }
        if (series.size() == SERIES_COLORS.length) {
            throw new IllegalStateException("at most " + SERIES_COLORS.length + " series per chart");
        }
        series.add(new Series(name, x.clone(), y.clone(), dashed));
        return this;
    }

    public void writeTo(Path file) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Files.writeString(file, render(), StandardCharsets.UTF_8);
    }

    public String render() {
        double xMax = 0;
        double yMax = 0;
        for (Series s : series) {
            for (double v : s.x) {
                xMax = Math.max(xMax, v);
            }
            for (double v : s.y) {
                yMax = Math.max(yMax, v);
            }
        }
        double xStep = niceStep(xMax / 5);
        double yStep = niceStep(yMax / 5);
        double xTop = Math.ceil(xMax / xStep) * xStep;
        double yTop = Math.ceil(yMax * 1.05 / yStep) * yStep;
        double plotWidth = WIDTH - LEFT - RIGHT;
        double plotHeight = HEIGHT - TOP - BOTTOM;

        StringBuilder svg = new StringBuilder();
        svg.append(String.format(Locale.ROOT,
                "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 %d %d\" width=\"%d\" height=\"%d\" font-family=\"Segoe UI, Helvetica, Arial, sans-serif\">%n",
                WIDTH, HEIGHT, WIDTH, HEIGHT));
        svg.append(String.format(Locale.ROOT, "<title>%s</title>%n", escape(title)));
        svg.append(String.format(Locale.ROOT, "<rect width=\"%d\" height=\"%d\" rx=\"10\" fill=\"%s\"/>%n", WIDTH, HEIGHT, SURFACE));
        svg.append(text(LEFT - 50, 30, title, 17, TEXT_PRIMARY, "start", "600"));
        svg.append(text(LEFT - 50, 50, subtitle, 12.5, TEXT_SECONDARY, "start", "400"));

        for (double v = 0; v <= yTop + 1e-9; v += yStep) {
            double py = TOP + plotHeight - v / yTop * plotHeight;
            svg.append(line(LEFT, py, LEFT + plotWidth, py, v == 0 ? AXIS : GRID, 1));
            svg.append(text(LEFT - 10, py + 4, format(v), 11.5, TEXT_SECONDARY, "end", "400"));
        }
        for (double v = 0; v <= xTop + 1e-9; v += xStep) {
            double px = LEFT + v / xTop * plotWidth;
            svg.append(line(px, TOP + plotHeight, px, TOP + plotHeight + 5, AXIS, 1));
            svg.append(text(px, TOP + plotHeight + 20, format(v), 11.5, TEXT_SECONDARY, "middle", "400"));
        }
        svg.append(text(LEFT + plotWidth / 2, HEIGHT - 18, xLabel, 12.5, TEXT_SECONDARY, "middle", "400"));
        svg.append(String.format(Locale.ROOT,
                "<text x=\"20\" y=\"%.1f\" font-size=\"12.5\" fill=\"%s\" text-anchor=\"middle\" transform=\"rotate(-90 20 %.1f)\">%s</text>%n",
                TOP + plotHeight / 2, TEXT_SECONDARY, TOP + plotHeight / 2, escape(yLabel)));

        List<double[]> labelAnchors = new ArrayList<>();
        for (int k = 0; k < series.size(); k++) {
            Series s = series.get(k);
            String color = SERIES_COLORS[k];
            StringBuilder path = new StringBuilder();
            for (int i = 0; i < s.x.length; i++) {
                double px = LEFT + s.x[i] / xTop * plotWidth;
                double py = TOP + plotHeight - s.y[i] / yTop * plotHeight;
                path.append(i == 0 ? "M" : " L").append(String.format(Locale.ROOT, "%.1f %.1f", px, py));
            }
            svg.append(String.format(Locale.ROOT,
                    "<path d=\"%s\" fill=\"none\" stroke=\"%s\" stroke-width=\"2\" stroke-linejoin=\"round\" stroke-linecap=\"round\"%s/>%n",
                    path, color, s.dashed ? " stroke-dasharray=\"6 5\"" : ""));
            for (int i = 0; i < s.x.length; i++) {
                double px = LEFT + s.x[i] / xTop * plotWidth;
                double py = TOP + plotHeight - s.y[i] / yTop * plotHeight;
                svg.append(String.format(Locale.ROOT,
                        "<circle cx=\"%.1f\" cy=\"%.1f\" r=\"4.5\" fill=\"%s\" stroke=\"%s\" stroke-width=\"2\"><title>%s: x=%s, y=%s</title></circle>%n",
                        px, py, color, SURFACE, escape(s.name), format(s.x[i]), format(s.y[i])));
            }
            int last = s.x.length - 1;
            labelAnchors.add(new double[]{k,
                    LEFT + s.x[last] / xTop * plotWidth,
                    TOP + plotHeight - s.y[last] / yTop * plotHeight});
        }

        labelAnchors.sort(Comparator.comparingDouble(a -> a[2]));
        double previous = -1e9;
        for (double[] anchor : labelAnchors) {
            double y = Math.max(anchor[2], previous + 16);
            previous = y;
            int k = (int) anchor[0];
            svg.append(String.format(Locale.ROOT,
                    "<rect x=\"%.1f\" y=\"%.1f\" width=\"10\" height=\"3\" rx=\"1.5\" fill=\"%s\"/>%n",
                    anchor[1] + 12, y - 2.5, SERIES_COLORS[k]));
            svg.append(text(anchor[1] + 27, y + 4, series.get(k).name, 12, TEXT_PRIMARY, "start", "500"));
        }

        double legendX = LEFT;
        for (int k = 0; k < series.size(); k++) {
            Series s = series.get(k);
            svg.append(String.format(Locale.ROOT,
                    "<line x1=\"%.1f\" y1=\"%d\" x2=\"%.1f\" y2=\"%d\" stroke=\"%s\" stroke-width=\"2.5\" stroke-linecap=\"round\"%s/>%n",
                    legendX, TOP - 12, legendX + 18, TOP - 12, SERIES_COLORS[k], s.dashed ? " stroke-dasharray=\"5 4\"" : ""));
            svg.append(text(legendX + 24, TOP - 8, s.name, 12, TEXT_PRIMARY, "start", "400"));
            legendX += 40 + s.name.length() * 6.8;
        }
        svg.append("</svg>\n");
        return svg.toString();
    }

    private static double niceStep(double raw) {
        if (raw <= 0) {
            return 1;
        }
        double magnitude = Math.pow(10, Math.floor(Math.log10(raw)));
        double fraction = raw / magnitude;
        double nice = fraction <= 1 ? 1 : fraction <= 2 ? 2 : fraction <= 2.5 ? 2.5 : fraction <= 5 ? 5 : 10;
        return nice * magnitude;
    }

    private static String format(double v) {
        if (Math.abs(v) >= 1000 && v == Math.rint(v)) {
            return String.format(Locale.ROOT, "%,d", (long) v);
        }
        if (v == Math.rint(v)) {
            return String.format(Locale.ROOT, "%d", (long) v);
        }
        if (Math.abs(v) >= 10) {
            return String.format(Locale.ROOT, "%.1f", v);
        }
        return String.format(Locale.ROOT, "%.2f", v);
    }

    private static String line(double x1, double y1, double x2, double y2, String color, double width) {
        return String.format(Locale.ROOT,
                "<line x1=\"%.1f\" y1=\"%.1f\" x2=\"%.1f\" y2=\"%.1f\" stroke=\"%s\" stroke-width=\"%.1f\"/>%n",
                x1, y1, x2, y2, color, width);
    }

    private static String text(double x, double y, String content, double size, String color, String anchor, String weight) {
        return String.format(Locale.ROOT,
                "<text x=\"%.1f\" y=\"%.1f\" font-size=\"%.1f\" fill=\"%s\" text-anchor=\"%s\" font-weight=\"%s\">%s</text>%n",
                x, y, size, color, anchor, weight, escape(content));
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
