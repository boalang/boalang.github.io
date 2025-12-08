/*
 * Copyright 2013-2014 Iowa State University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY IOWA STATE UNIVERSITY ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL IOWA STATE UNIVERSITY OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of Iowa State University.
 */
import java.awt.BasicStroke;
import java.text.NumberFormat;
import java.util.*;
import java.io.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

/**
 * Generates Figures 5c-d and 6c-d, Section 5.2.
 *
 * @author Robert Dyer (rdyer@iastate.edu)
 */
public class Densities extends AbstractChart {
	private final String type;
	private final String feature;

    public Densities(final String type, final String feature) {
		this.type = type;
		this.feature = feature;

		final DateAxis xAxis = getDateAxis();
		final NumberAxis yAxis = new NumberAxis();
		yAxis.setNumberFormatOverride(new java.text.DecimalFormat("##0.00%"));
		yAxis.setAutoRange(true);
		yAxis.setLabel(type + "s");

        final XYPlot plot =  new XYPlot(createDataset(), xAxis, yAxis, new StandardXYItemRenderer(StandardXYItemRenderer.LINES));
		final JFreeChart chart = new JFreeChart(/*feature + " Usage"*/null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);

		scaleDateAxis(xAxis);

		setXYTheme(chart, plot);
		setMarkers(plot, feature);
		plot.getRenderer().setStroke(new BasicStroke(2 * fontScale));
		save(chart, "density/" + type + "s/" + feature);
    }

	final static ArrayList<Long> totalsTimes = new ArrayList<Long>();
	final static HashMap<Long, Integer> totalSize = new HashMap<Long, Integer>();

	private static void getTotals(final String type) {
		totalSize.clear();
		totalsTimes.clear();

		final HashMap<Long, Integer> counts = new HashMap<Long, Integer>();

		try (final DataInputStream in = new DataInputStream(new FileInputStream("../section5-2/times.txt"));
				final BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String strLine;
			while ((strLine = br.readLine()) != null) {
				if (!strLine.startsWith(type))
					continue;
				final long key = Long.parseLong(strLine.substring(strLine.indexOf("[") + 1, strLine.indexOf("]"))) / 1000L;
				if (key <= 0) continue;
				totalsTimes.add(key);
				counts.put(key, Integer.parseInt(strLine.substring(strLine.indexOf(" = ") + 3)));
			}
		} catch (final IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		Collections.sort(totalsTimes);

		int last = 0;
		for (long t : totalsTimes) {
			totalSize.put(t, counts.get(t) + last);
			last += counts.get(t);
		}
	}

    private XYDataset createDataset() {
		final DefaultXYDataset dataset = new DefaultXYDataset();

		final ArrayList<Long> times = new ArrayList<Long>();
		final HashMap<Long, Integer> counts = new HashMap<Long, Integer>();

		try (final DataInputStream in = new DataInputStream(new FileInputStream("../section5-1/first-uses.txt"));
				final BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String strLine;
			while ((strLine = br.readLine()) != null) {
				if (!strLine.startsWith(type) || strLine.indexOf(feature) == -1)
					continue;
				final long key = Long.parseLong(strLine.substring(strLine.lastIndexOf("][") + 2, strLine.lastIndexOf("]"))) / 1000L;
				if (key <= 0) continue;
				final int val = Integer.parseInt(strLine.substring(strLine.indexOf(" = ") + 3));
				if (counts.containsKey(key)) {
					counts.put(key, counts.get(key) + val);
				} else {
					times.add(key);
					counts.put(key, val);
				}
			}
		} catch (final IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		Collections.sort(times);

		final HashMap<Long, Integer> totals = new HashMap<Long, Integer>();

		int last = 0;
		for (long t : times) {
			totals.put(t, counts.get(t) + last);
			last += counts.get(t);
		}

		final HashMap<Long, Integer> maxs = new HashMap<Long, Integer>();
		int lastIndex = 0;
		long lastTime = totalsTimes.get(lastIndex);

		final double[] x = new double[totals.size()];
		final double[] y = new double[totals.size()];
		int i = 0;
		for (long t : times) {
			while (lastTime <= t && lastIndex < totalsTimes.size())
				lastTime = totalsTimes.get(lastIndex++);
			x[i] = t;
			y[i] = (double)totals.get(t) / (double)totalSize.get(lastTime);
			i++;
		}

		dataset.addSeries("s1", new double[][] {x, y});

        return dataset;
    }

    public static void main(final String[] args) {
		getTotals("Project");
		for (final String s : AbstractChart.features)
			try {
				new Densities("Project", s);
			} catch (final Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}

		getTotals("File");
		for (final String s : AbstractChart.features)
			try {
				new Densities("File", s);
			} catch (final Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
    }
}
