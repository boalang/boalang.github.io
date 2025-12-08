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
import java.util.*;
import java.io.*;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.text.TextBlock;
import org.jfree.ui.RectangleEdge;

/**
 * Generates Figure 12, Section 5.3.2.
 *
 * @author Hoan Nguyen (hoan@iastate.edu)
 * @author Robert Dyer (rdyer@iastate.edu)
 */
public class GenUsagesLine extends AbstractChart {
	private static Map<String, List<Integer>> data = new HashMap<>();

	public static void main(final String[] args) {
		importData();

		for (final String feature : AbstractChart.features)
			new GenUsagesLine(feature);
	}
	private static void importData() {
		final Map<Integer, Integer> uses = new HashMap<>();
		String lastFeature = "";

		try (final DataInputStream in = new DataInputStream(new FileInputStream("../section5-3/section5-3-2/uses-committers-snapshot.txt"));
			final BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

			String strLine;
			while ((strLine = br.readLine()) != null) {
				final String[] parts = strLine.split("\\]\\[");

				final String feature = parts[0].substring(parts[0].indexOf("[") + 1);
				int user = 0;
				try {
					user = Integer.parseInt(parts[1].substring(0, parts[1].indexOf("]")));
				} catch (final Exception e) { }
				final int val = Integer.parseInt(parts[1].substring(parts[1].indexOf(" = ") + 3));

				// since the input is sorted, we can optimize a bit
				if (!feature.equals(lastFeature)) {
					data.put(lastFeature, new ArrayList<Integer>(uses.values()));
					lastFeature = feature;
					uses.clear();
				}

				uses.put(user, (uses.containsKey(user) ? uses.get(user) : 0) + val);
			}
		} catch (final IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		data.put(lastFeature, new ArrayList<Integer>(uses.values()));
	}

	public GenUsagesLine(final String feature) {
		final DefaultCategoryDataset dataset = createDataset(feature);

		final JFreeChart chart = ChartFactory.createBarChart(
	            null,
	            "Committer Rank",          // domain axis label
	            "Number of Usages",        // range axis label
	            dataset,                   // data
	            PlotOrientation.VERTICAL,  // orientation
	            false,                     // include legend
	            true,                      // tooltips
	            false                      // urls
	        );
				
		final CategoryPlot plot = (CategoryPlot)chart.getPlot();

		final CategoryAxis x = new CategoryAxis(plot.getDomainAxis().getLabel()) {
			@Override
			public void drawTickMarks(Graphics2D g2, double cursor, Rectangle2D dataArea, RectangleEdge edge, AxisState state) {
				final Plot p = getPlot();
				if (p == null)
					return;
				final CategoryPlot plot = (CategoryPlot) p;
				final double il = getTickMarkInsideLength();
				final double ol = getTickMarkOutsideLength();
				final Line2D line = new Line2D.Double();
				final List categories = plot.getCategoriesForAxis(this);
				g2.setPaint(getTickMarkPaint());
				g2.setStroke(getTickMarkStroke());
				final Iterator iterator = categories.iterator();
				int step = (int) Math.pow(10, (int) Math.log10(dataset.getColumnCount()));
				if (step >= 10)
					step /= 10;
				if (dataset.getColumnCount() / step < 5)
					step /= 2;
				int i = 0;
				while (iterator.hasNext()) {
					final Comparable key = (Comparable) iterator.next();
					if (++i % step != 0)
						continue;
					final double x = getCategoryMiddle(key, categories, dataArea, edge);
					line.setLine(x, cursor, x, cursor - il);
					g2.draw(line);
					line.setLine(x, cursor, x, cursor + ol);
					g2.draw(line);
				}
				state.cursorDown(ol);
			}
			@Override
			public List refreshTicks(Graphics2D g2, AxisState state,
					Rectangle2D dataArea, RectangleEdge edge) {
				final int p = (int) Math.log10(dataset.getColumnCount());
				int step = (int) Math.pow(10, p);
				if (dataset.getColumnCount() / step < 5)
					step /= 2;
				final List allTicks = super.refreshTicks(g2, state, dataArea, edge);
				final List myTicks = new ArrayList();

				for (int i = 0; i < allTicks.size(); i++) {
					final CategoryTick tick = (CategoryTick) allTicks.get(i);
					
					if ((i + 1) % step == 0)
						myTicks.add(tick);
					else
						myTicks.add(new CategoryTick(tick.getCategory(), new TextBlock(), tick.getLabelAnchor(), tick.getRotationAnchor(), tick.getAngle()));
				}
				return myTicks;
			}
		};
		x.setLowerMargin(0);
		x.setUpperMargin(0);
		x.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
		plot.setDomainAxis(x);
		
		final LogarithmicAxis y = new LogarithmicAxis("Number of Usages");
		y.setLowerMargin(0);
		y.setUpperMargin(0);
		y.setLowerBound(0);
		y.setUpperBound(max);
		plot.setRangeAxis(y);
		
		setCategoryTheme(chart, plot);

		final BarRenderer r = new BarRenderer();
		plot.setRenderer(r);
		r.setBarPainter(new StandardBarPainter());
        r.setShadowVisible(false);

		save(chart, "developer/frequency-" + feature);
	}
	
	int max = 1;

	private DefaultCategoryDataset createDataset(final String feature) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		final List<Integer> values = data.get(feature);
		Collections.sort(values, Collections.reverseOrder());
		long total = 0;
		for (int i = 0; i < values.size(); i++) {
			dataset.addValue(values.get(i), "", "" + (i + 1));
			total += values.get(i);
		}
		while (values.get(0) > max)
			max *= 10;
		return dataset;
	}
}
