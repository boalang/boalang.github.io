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
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.text.SimpleDateFormat;
import java.util.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;

import features.java.LanguageFeature;

/**
 * Generates Figure 14, Section 5.3.4.
 *
 * @author Hoan Nguyen (hoan@iastate.edu)
 * @author Robert Dyer (rdyer@iastate.edu)
 */
public class Scattered extends AbstractChart {
	private int series = 1;

	public static void main(String[] args) {
		new Scattered();
	}
	
	public Scattered() {
		fontBase = 15;

		final XYSeriesCollection dataset = createDataset();
		final JFreeChart chart = ChartFactory.createScatterPlot(
				null,						// chart title
				"Time",						// x axis label
				"Committer",				// y axis label
				dataset,					// data
				PlotOrientation.VERTICAL,
				true,						// include legend
				true,						// tooltips
				false						// urls
				);
		final XYPlot plot = (XYPlot) chart.getPlot();
		
		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		
		for (int i = 0; i < dataset.getSeries().size(); i++)
			renderer.setSeriesLinesVisible(i, false);
		Shape s = ShapeUtilities.createDownTriangle(fontScale * 6);
		renderer.setSeriesShape(0, s);
		s = ShapeUtilities.createDiagonalCross(fontScale * 5, fontScale * 1);
		for (int i = 1; i <= 5; i++)
			renderer.setSeriesShape(i, s);
		s = new Ellipse2D.Double(0, 0, fontScale * 9, fontScale * 9);
		for (int i = 6; i < dataset.getSeries().size(); i++)
			renderer.setSeriesShape(i, s);
		plot.setRenderer(renderer);
		
		final DateAxis x = new DateAxis();
		x.setDateFormatOverride(new SimpleDateFormat("yyyy"));
		x.setTickUnit(new DateTickUnit(DateTickUnitType.YEAR, 1));
		x.setVerticalTickLabels(true);
		plot.setDomainAxis(x);
		
		final NumberAxis y = new NumberAxis(plot.getRangeAxis().getLabel()) {
			@Override
			public List refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
				List allTicks = super.refreshTicks(g2, state, dataArea, edge);
				List myTicks = new ArrayList();

				for (int i = 0; i < allTicks.size(); i++)
					if (i % 2 == 1) {
						myTicks.add(allTicks.get(i));
					} else {
						NumberTick tick = (NumberTick) allTicks.get(i);
						myTicks.add(new NumberTick(tick.getNumber(), "", tick.getTextAnchor(), tick.getRotationAnchor(), tick.getAngle()));
					}
				return myTicks;
			}
		};
		y.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		y.setTickUnit(new NumberTickUnit(1.0));
		plot.setRangeAxis(y);
		
		setXYTheme(chart, plot);

		save(chart, "developer/uses-track" + series);
	}

	private XYSeriesCollection createDataset() {
		final HashMap<String, HashMap<Integer, Long>> featureDevTime = new HashMap<String, HashMap<Integer,Long>>();
		final HashMap<String, HashMap<Integer, Long>> featureDevTime1 = new HashMap<String, HashMap<Integer,Long>>();
		final HashMap<String, HashMap<Integer, Long>> featureDevTime2 = new HashMap<String, HashMap<Integer,Long>>();

		final XYSeriesCollection[] datasets = new XYSeriesCollection[3];
		for (int i = 0; i < 3; i++)
			datasets[i] = new XYSeriesCollection();

		String content = readStringFromFile("../section5-3/section5-3-4/uses.txt");
		Scanner sc = new Scanner(content);
		String line;
		final HashMap<String, Integer> ranks = new HashMap<>();
		while (sc.hasNextLine()) {
			line = sc.nextLine();
			if (!line.startsWith("Uses"))
				continue;
			final String[] parts = line.split("\\]\\[");
			final String n = parts[0].substring(5);
			if (!ranks.containsKey(n))
				ranks.put(n, 0);
			ranks.put(n, ranks.get(n) + 1);
		}
		sort(ranks, 1);

		content = readStringFromFile("../section5-3/section5-3-4/uses.txt");
		sc = new Scanner(content);

		while (sc.hasNextLine()) {
			line = sc.nextLine();
			if (!line.startsWith("Uses"))
				continue;
			final String[] parts = line.split("\\]");
			if (parts.length > 1) {
				final String f = parts[1].substring(1);
				final String n = parts[0].substring(5);
				String ts = parts[2].substring(6, parts[2].length() - 3).replaceAll("\\.", "");
				for (int i = ts.length(); i < 13; i++)
					ts += "0";
				final long t = Long.parseLong(ts);
				final int rank = ranks.get(n);
				add(featureDevTime, f, rank, t);
				add(featureDevTime1, LanguageFeature.getSuperFeature(f), rank, t);
				add(featureDevTime2, LanguageFeature.getJlsVersion(f), rank, t);
			}
		}
		add(datasets[0], featureDevTime);
		add(datasets[1], featureDevTime1);
		add(datasets[2], featureDevTime2);
		return datasets[series];
	}

	private void sort(final HashMap<String, Integer> ranks, final int start) {
		if (start > ranks.size()) {
			for (final String k : ranks.keySet())
				ranks.put(k, ranks.size() - ranks.get(k) + 1);
			return;
		}
		boolean found = false;
		for (String k : ranks.keySet())
			if (ranks.get(k) == start) {
				if (found)
					ranks.put(k, start + 1);
				else
					found = true;
			}
		sort(ranks, start + 1);
	}

	private void add(final XYSeriesCollection dataset, final HashMap<String, HashMap<Integer, Long>> featureDevTime) {
		ArrayList<String> l = new ArrayList<String>();
		if (featureDevTime.size() == 10) {
			l.add(LanguageFeature.Feature_Assert);
			l.add(LanguageFeature.Super_Feature_Annotation);
			l.add(LanguageFeature.Feature_EnhancedFor);
			l.add(LanguageFeature.Feature_Enums);
			l.add(LanguageFeature.Super_Feature_Generics);
			l.add(LanguageFeature.Feature_Varargs);
			l.add(LanguageFeature.Feature_Diamond);
			l.add(LanguageFeature.Super_Feature_Literal);
			l.add(LanguageFeature.Feature_SafeVarargs);
			l.add(LanguageFeature.Super_Feature_Try_Catch);
		} else {
			l = new ArrayList<String>(featureDevTime.keySet());
			Collections.sort(l);
		}

		for (final String f : l) {
			XYSeries s = new XYSeries(f);
			HashMap<Integer, Long> devTime = featureDevTime.get(f);
			for (final int r : devTime.keySet())
				s.add((double)devTime.get(r), (double)r);
			if (!s.isEmpty())
				dataset.addSeries(s);
		}
	}

	private void add(final HashMap<String, HashMap<Integer, Long>> featureDevTime, final String f, final int rank, final long t) {
		if (f == null)
			System.out.println();
		HashMap<Integer, Long> devTime = featureDevTime.get(f);
		if (devTime == null)
			devTime = new HashMap<Integer, Long>();
		if (devTime.containsKey(rank)) {
			if (t < devTime.get(rank))
				devTime.put(rank, t);
		} else {
			devTime.put(rank, t);
		}
		featureDevTime.put(f, devTime);
	}
}
