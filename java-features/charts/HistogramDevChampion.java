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
import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.*;
import java.io.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;

/**
 * Generates Figure 13, Section 5.3.3.
 *
 * @author Hoan Nguyen (hoan@iastate.edu)
 * @author Robert Dyer (rdyer@iastate.edu)
 */
public class HistogramDevChampion extends AbstractChart {
	private SimpleHistogramDataset dataset;
	
	public static void main(final String[] args) {
		for (final String feature : AbstractChart.features) {
			top1.put(feature, new ArrayList<Double>());
			top2.put(feature, new ArrayList<Double>());
			top3.put(feature, new ArrayList<Double>());
		}

		importData();

		for (final String feature : AbstractChart.features) {
			new HistogramDevChampion(1, top1, feature);
			new HistogramDevChampion(2, top2, feature);
			new HistogramDevChampion(3, top3, feature);
		}
	}

	private static final Map<String, List<Double>> top1 = new HashMap<>();
	private static final Map<String, List<Double>> top2 = new HashMap<>();
	private static final Map<String, List<Double>> top3 = new HashMap<>();

	private static void importCommitters(final Map<String, Map<Integer, Map<Integer, Integer>>> counts) {
		try (final DataInputStream in = new DataInputStream(new FileInputStream("../section5-3/section5-3-3/committers-project.txt"));
				final BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String strLine;
			while ((strLine = br.readLine()) != null) {
				final String[] parts = strLine.split("\\] = ");
				int count = Integer.parseInt(parts[1]);

				if (count < 6)
					continue;

				int projId = Integer.parseInt(parts[0].substring(parts[0].indexOf("[") + 1));
				for (final String feature : AbstractChart.features)
					counts.get(feature).put(projId, new HashMap<Integer, Integer>());
			}
		} catch (final IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private static void importData() {
		final Map<String, Map<Integer, Map<Integer, Integer>>> counts = new HashMap<>();
		for (final String feature : AbstractChart.features)
			counts.put(feature, new HashMap<Integer, Map<Integer, Integer>>());

		importCommitters(counts);

		try (final DataInputStream in = new DataInputStream(new FileInputStream("../section5-3/section5-3-1/uses-committers.txt"));
				final BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String strLine;
			while ((strLine = br.readLine()) != null) {
				final String[] parts = strLine.split("\\]\\[");

				final String feature = parts[0].substring(parts[0].indexOf("[") + 1);
				final Map<Integer, Map<Integer, Integer>> map = counts.get(feature);

				final int project = Integer.parseInt(parts[3].substring(0, parts[3].indexOf("]")));
				if (!map.containsKey(project))
					continue;
				final Map<Integer, Integer> p = map.get(project);

				final int committer = parts[2].isEmpty() ? -1 : Integer.parseInt(parts[2]);
				final int uses = Integer.parseInt(parts[3].substring(parts[3].indexOf(" = ") + 3));
				if (uses > 0)
					p.put(committer, (p.containsKey(committer) ? p.get(committer) : 0) + uses);
			}
		} catch (final IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		for (final String feature : AbstractChart.features) {
			final Map<Integer, Map<Integer, Integer>> map = counts.get(feature);

			for (final int projId : map.keySet()) {
				final Map<Integer, Integer> projMap = map.get(projId);
				if (projMap.size() == 0)
					continue;

				double total = 0.0;
				for (final int i : projMap.values())
					total += (double)i;

				final List<Integer> keys = new ArrayList<Integer>(projMap.keySet());

				Collections.sort(keys, new Comparator<Integer>() {
					@Override
					public int compare(final Integer s1, final Integer s2) {
						return Integer.compare(projMap.get(s2), projMap.get(s1));
					}
				});

				int first = keys.size() > 0 ? projMap.get(keys.get(0)) : 0;
				int second = keys.size() > 1 ? projMap.get(keys.get(1)) : 0;
				int third = keys.size() > 2 ? projMap.get(keys.get(2)) : 0;

				top1.get(feature).add(first / total);
				top2.get(feature).add((first + second) / total);
				top3.get(feature).add((first + second + third) / total);
			}
		}
	}

	public HistogramDevChampion(final int top, final Map<String, List<Double>> data, final String feature) {
		fontBase = 20;

		createDataset(data.get(feature));

		final JFreeChart chart = ChartFactory.createHistogram(
				null,
				"Proportion of Usages", 
				"Number of Projects", 
				this.dataset, 
				PlotOrientation.VERTICAL, 
				false, 
				false, 
				false);
		final XYPlot plot = (XYPlot) chart.getPlot();
		
		final NumberAxis y = (NumberAxis) plot.getRangeAxis();
		y.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		
		final NumberAxis x = (NumberAxis) plot.getDomainAxis();
		x.setNumberFormatOverride(new DecimalFormat("#%"));
		x.setTickUnit(new NumberTickUnit(0.2));
		
		setXYTheme(chart, plot);

        final XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(true);
		renderer.setBarPainter(new StandardXYBarPainter());

		save(chart, "developer/project-" + top + "-" + feature);
	}

	private void createDataset(final List<Double> data) {
		dataset = new SimpleHistogramDataset("test");
		dataset.setAdjustForBinSize(false);

		dataset.addBin(new SimpleHistogramBin(0.0, 0.1));
		for (double i = 1; i < 10; i++)
			dataset.addBin(new SimpleHistogramBin(0.1 * i, 0.1 * i + 0.1, false, true));

		for (final double d : data)
			try {
				dataset.addObservation(d);
			} catch (final Throwable e) {
				System.err.println("Invalid datapoint - " + d);
			}
	}
}
