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
import java.text.SimpleDateFormat;
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
 * Generates Figures 5a-b and 6a-b, Section 5.2.
 *
 * @author Robert Dyer (rdyer@iastate.edu)
 */
public class Histogram extends AbstractChart {
	private final String type;
	private final String feature;

    public Histogram(final String type, final String feature) {
		this.type = type;
		this.feature = feature;

        final JFreeChart chart = ChartFactory.createHistogram(
            //feature + " Usage",
			null,
            "Date (Unix timestamp)",
            type + "s",
            createDataset(),
            PlotOrientation.VERTICAL,
            false,
            false,
            false
        );
        final XYPlot plot = (XYPlot) chart.getPlot();

		final DateAxis axis = getDateAxis();
		plot.setDomainAxis(axis);
		scaleDateAxis(axis);

		setXYTheme(chart, plot);
		setMarkers(plot, feature);

        final XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(true);
		renderer.setBarPainter(new StandardXYBarPainter());

		save(chart, "histogram/" + type + "s/" + feature);
    }

    private IntervalXYDataset createDataset() {
        final SimpleHistogramDataset dataset = new SimpleHistogramDataset(feature);
		dataset.setAdjustForBinSize(false);

		try (final DataInputStream in = new DataInputStream(new FileInputStream("../section5-1/first-uses.txt"));
			final BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

			final ArrayList<Long> times = new ArrayList<>();
			final HashMap<Long, Integer> counts = new HashMap<>();

			String strLine;
			while ((strLine = br.readLine()) != null) {
				if (!strLine.startsWith(type) || strLine.indexOf(feature) == -1)
					continue;
				final long key = Long.parseLong(strLine.substring(strLine.lastIndexOf("][") + 2, strLine.lastIndexOf("]"))) / 1000L;
				times.add(key);
				counts.put(key, Integer.parseInt(strLine.substring(strLine.indexOf(" = ") + 3)));
			}
			Collections.sort(times);

			final long start = times.get(0);
			final long end = times.get(times.size() - 1);
			final long range = 2592000000L;
			final long bins = (end - start) / range;
			for (long i = 0; i <= bins; i++)
				dataset.addBin(new SimpleHistogramBin(start + range * i, start + range * (i + 1), true, i == bins));

			for (int i = 0; i < times.size(); i++) {
				final long key = times.get(i);
				for (int j = 0; j < counts.get(key); j++)
					dataset.addObservation(key);
			}
		} catch (final IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

        return dataset;
    }

    public static void main(final String[] args) {
		for (final String s : AbstractChart.features) {
			try {
				new Histogram("File", s);
			} catch (final Exception e) {
				e.printStackTrace();
			}
			try {
				new Histogram("Project", s);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
    }
}
