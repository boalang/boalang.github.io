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
import java.awt.Graphics2D;
import java.util.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import java.awt.geom.Rectangle2D;

/**
 * Generates Figure 9b, Section 5.3.
 *
 * @author Hoan Nguyen (hoan@iastate.edu)
 * @author Robert Dyer (rdyer@iastate.edu)
 */
public class FileDeveloperHistogram extends AbstractChart {
	public static void main(String[] args) {
		new FileDeveloperHistogram();
	}

	private SimpleHistogramDataset dataset;
	private final int max = 21;
	
	public FileDeveloperHistogram() {
		final int[] bins = new int[max];
		for (int i = 1; i <= max; i++)
			bins[i - 1] = i;
		createDataset(bins);

		if (dataset == null)
			return;

		final JFreeChart chart = ChartFactory.createHistogram(
				null, 
				"Number of Committers", 
				"Number of Files", 
				this.dataset, 
				PlotOrientation.VERTICAL, 
				false, 
				false, 
				false
				);
		final XYPlot plot = (XYPlot) chart.getPlot();
		
		final NumberAxis y = (NumberAxis) plot.getRangeAxis();
		y.setLowerMargin(0);
		y.setUpperMargin(0);
		
		final NumberAxis x = new NumberAxis(plot.getDomainAxis().getLabel()) {
			@Override
			public List refreshTicks(Graphics2D g2, AxisState state,
					Rectangle2D dataArea, RectangleEdge edge) {
				final List allTicks = super.refreshTicks(g2, state, dataArea, edge);
				final List myTicks = new ArrayList();

				for (int i = 0; i < allTicks.size() - 2; i++)
					myTicks.add(allTicks.get(i));
				final NumberTick tick = (NumberTick) allTicks.get(allTicks.size() - 2);
				final NumberTick myTick = new NumberTick(tick.getNumber(), "+", 
						tick.getTextAnchor(), tick.getRotationAnchor(), tick.getAngle());
				myTicks.add(myTick);
				
				final NumberTick ltick = (NumberTick) allTicks.get(allTicks.size() - 1);
				myTicks.add(new NumberTick(ltick.getNumber(), "", ltick.getTextAnchor(), ltick.getRotationAnchor(), ltick.getAngle()));
				
				return myTicks;
			}
		};
		x.setLowerMargin(0);
		plot.setDomainAxis(x);
		
		setXYTheme(chart, plot);

        final XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(true);
		renderer.setBarPainter(new StandardXYBarPainter());

		save(chart, "developer/committers-file-histogram");
	}

	private void createDataset(final int[] bins) {
		dataset = new SimpleHistogramDataset("test");
		dataset.setAdjustForBinSize(false);
		final int len = bins.length;
		for (int i = 0; i < len - 2; i++)
			dataset.addBin(new SimpleHistogramBin(bins[i], bins[i + 1], true, false));
		dataset.addBin(new SimpleHistogramBin(bins[len - 2], bins[len - 1], true, true));
		final String content = readStringFromFile("../section5-3/committers-file.txt");
		final Scanner sc = new Scanner(content);
		while (sc.hasNextLine()) {
			final String line = sc.nextLine();
			int v = Integer.parseInt(line.substring(line.indexOf("[") + 1, line.indexOf("]")));
			final int count = Integer.parseInt(line.substring(line.indexOf(" = ") + 3));
			if (v < bins[0])
				continue;
			if (v > bins[bins.length - 1])
				v = bins[bins.length - 1];
			for (int i = 0; i < count; i++)
				dataset.addObservation(v);
		}
	}
}
