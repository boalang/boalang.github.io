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
import java.util.*;
import java.io.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.text.TextBlock;
import org.jfree.text.TextFragment;
import org.jfree.ui.RectangleEdge;
import java.text.DecimalFormat;
import java.awt.geom.Rectangle2D;

/**
 * Generates Figures 10-11, Section 5.3.1.
 *
 * @author Hoan Nguyen (hoan@iastate.edu)
 * @author Robert Dyer (rdyer@iastate.edu)
 */
public class StackedBar extends AbstractChart {
	private final String feature;

	public static void main(final String[] args) {
		for (final String feature : AbstractChart.features) {
			times.put(feature, new HashSet<Long>());
			data.put(feature, new HashMap<Long, Set<Integer>>());
		}
		importData();

		for (final String feature : features)
			try {
				if (times.get(feature).size() > 0)
					new StackedBar(feature);
			} catch (final Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
	}

	private static Map<String, Set<Long>> times = new HashMap<>();
	private static Map<String, Map<Long, Set<Integer>>> data = new HashMap<>();
	
	private static void importData() {
		Set<Long> times = null;
		Map<Long, Set<Integer>> data = null;
		String lastFeature = "";

		try (final DataInputStream in = new DataInputStream(new FileInputStream("../section5-3/section5-3-1/uses-committers.txt"));
			final BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

			String strLine;
			while ((strLine = br.readLine()) != null) {
				final String[] parts = strLine.split("\\]\\[");

				final String feature = parts[0].substring(parts[0].indexOf("[") + 1);
				final long time = Long.parseLong(parts[1]) / 1000L;
				int user = 0;
				try {
					user = Integer.parseInt(parts[2]);
				} catch (final Exception e) { }

				// since the input is sorted, we can optimize a bit
				if (!feature.equals(lastFeature)) {
					lastFeature = feature;
					times = StackedBar.times.get(feature);
					data = StackedBar.data.get(feature);
				}

				if (!times.contains(time))
					times.add(time);

				if (!data.containsKey(time))
					data.put(time, new HashSet<Integer>());
				data.get(time).add(user);
			}
		} catch (final IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public StackedBar(final String feature) {
		fontScale = 1;
		this.feature = feature;

		final JFreeChart chart = ChartFactory.createStackedBarChart(
				null, "", "Committers",
				createDataset(), PlotOrientation.VERTICAL, true, true, false);

		final CategoryPlot plot = chart.getCategoryPlot();
		
		final CategoryAxis x = new CategoryAxis(plot.getDomainAxis().getLabel()) {
			@Override
			public List refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
				List allTicks = super.refreshTicks(g2, state, dataArea, edge);
				List myTicks = new ArrayList();

				int step = 6;
				if (allTicks.size() > 34)
					step = 12;
				
				for (int i = 0; i < allTicks.size(); i++) {
					CategoryTick tick = (CategoryTick) allTicks.get(i);
					if (i % step == 0) {
						TextFragment tf = tick.getLabel().getLastLine().getFirstTextFragment();
						String text = tf.getText();
						if (step == 12)
							text = text.substring(0, text.length() - 2);
						TextBlock txtBlock = new TextBlock();
						txtBlock.addLine(text, tf.getFont(), tf.getPaint());
						CategoryTick myTick = new CategoryTick(tick.getCategory(), 
								txtBlock, 
								tick.getLabelAnchor(), tick.getRotationAnchor(), tick.getAngle());
						myTicks.add(myTick);
					} else {
						myTicks.add(new CategoryTick(tick.getCategory(), new TextBlock(), tick.getLabelAnchor(), tick.getRotationAnchor(), tick.getAngle()));
					}
				}
				return myTicks;
			}
		};
		x.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
		plot.setDomainAxis(x);
		
		plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		
		setCategoryTheme(chart, plot);
		setMarkers(plot, feature);

        final BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(true);
		renderer.setBarPainter(new StandardBarPainter());

		save(chart, "developer/evol-" + feature);
	}

	private Long[] buildTimeLine(final long minTime, final long maxTime) {
		final long span = 31 * 24 * 60 * 60 * 1000L;
		final int size = 1 + (int)((maxTime - minTime) / span);
		final Long[] times = new Long[size];

		for (int i = 0; i < size; i++)
			times[i] = minTime + i * span;

		return times;
	}

	private CategoryDataset createDataset() {
		final List<Long> times = new ArrayList<Long>(this.times.get(feature));
		Collections.sort(times);
		final long minTime = times.get(0);
		final long maxTime = times.get(times.size() - 1);

		final Long[] cats = buildTimeLine(minTime, maxTime);
		final int numOfCats = cats.length;
		final Integer[] catLabels = new Integer[numOfCats];
		for (int i = 0; i < numOfCats; i++)
			catLabels[i] = timestampToDate(cats[i]);

		final Map<Long, Set<Integer>> timeData = StackedBar.data.get(feature);

		final double[][] data = new double[2][numOfCats];

		int j = 0;
		long cat = cats[j + 1];
		Set<Integer> first = new HashSet<Integer>();
		Set<Integer> seen = new HashSet<Integer>();

		for (int i = 0; i < times.size(); i++) {
			long time = times.get(i);
			if (time >= cat && j < numOfCats - 1) {
				cat = cats[++j];
				seen.clear();
			}

			if (!timeData.containsKey(time))
				continue;

			final Set<Integer> users = timeData.get(time);
			for (final int user : users) {
				if (!first.contains(user)) {
					first.add(user);
					data[0][j] += 1;
				} else {
					if (!seen.contains(user)) {
						seen.add(user);
						data[1][j] += 1;
					}
				}
			}
		}

		return DatasetUtilities.createCategoryDataset(new String[] {"First time", "Already used"}, catLabels, data);
	}

	final static Calendar date = Calendar.getInstance();

	private int timestampToDate(final long timestamp) {
		date.setTimeInMillis(timestamp);
		return date.get(Calendar.YEAR) * 100 + date.get(Calendar.MONTH);
	}
}
