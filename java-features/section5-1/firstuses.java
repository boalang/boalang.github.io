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
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates Figure 3, Section 5.1.
 *
 * @author Robert Dyer (rdyer@iastate.edu)
 */
public class firstuses {
	private final static HashMap<String, Integer> fileCounts = new HashMap<>();
	private final static HashMap<String, Integer> projCounts = new HashMap<>();
	private final static HashMap<String, Long> mins = new HashMap<>();
	private final static HashMap<String, String> featureMap = new HashMap<>();
	private final static HashMap<String, String> releaseNames = new HashMap<>();
	private final static HashMap<String, Long> releaseDates = new HashMap<>();

	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM YYY");
	private final static NumberFormat numFormat = NumberFormat.getIntegerInstance();

	public static void main(String[] args) throws IOException {
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		releaseDates.put("JLS2", 1020902400000L);
		releaseNames.put("JLS2", "JLS2 (JSR 59)");

		featureMap.put("Assert", "JLS2");

		releaseDates.put("JLS3", 1096502400000L);
		releaseNames.put("JLS3", "JLS3 (JSR 176)");

		featureMap.put("AnnotDefine", "JLS3");
		featureMap.put("AnnotUse", "JLS3");
		featureMap.put("EnhancedFor", "JLS3");
		featureMap.put("Enums", "JLS3");
		featureMap.put("GenDefField", "JLS3");
		featureMap.put("GenDefMethod", "JLS3");
		featureMap.put("GenDefType", "JLS3");
		featureMap.put("GenExtends", "JLS3");
		featureMap.put("GenSuper", "JLS3");
		featureMap.put("GenWildcard", "JLS3");
		featureMap.put("Varargs", "JLS3");

		releaseDates.put("JLS4", 1311120000000L);
		releaseNames.put("JLS4", "JLS4 (JSR 366)");

		featureMap.put("BinaryLit", "JLS4");
		featureMap.put("Diamond", "JLS4");
		featureMap.put("MultiCatch", "JLS4");
		featureMap.put("SafeVarargs", "JLS4");
		featureMap.put("TryResources", "JLS4");
		featureMap.put("UnderscoreLit", "JLS4");

		getData();

		printTableHeader();
		printHeader("JLS2");
		printEntry("Assert", "Assert");
		printFooter();

		printHeader("JLS3", true);
		printEntry("AnnotDefine", "Annotation Declaration", "white");
		printEntry("AnnotUse", "Annotation Use", "gray!10");
		printEntry("EnhancedFor", "Enhanced For", "white");
		printEntry("Enums", "Enums", "gray!10");
		printEntry("GenDefField", "Generic Variable", "white");
		printEntry("GenDefMethod", "Generic Method", "gray!10");
		printEntry("GenDefType", "Generic Type", "white");
		printEntry("GenExtends", "Extends Wildcard", "gray!10");
		printEntry("GenSuper", "Super Wildcard", "white");
		printEntry("GenWildcard", "Other Wildcard", "gray!10");
		printEntry("Varargs", "Varargs", "white");
		printFooter();

		printHeader("JLS4");
		printEntry("BinaryLit", "Binary Literals");
		printEntry("Diamond", "Diamond");
		printEntry("MultiCatch", "MultiCatch");
		printEntry("SafeVarargs", "SafeVarargs");
		printEntry("TryResources", "Try with Resources");
		printEntry("UnderscoreLit", "Underscore Literal");
		printFooter();
		printTableFooter();
	}

	private static void printHeader(final String key) {
		printHeader(key, false);
	}

	private static void printHeader(final String key, final boolean color) {
		System.out.println("\\hline");
		if (color)
			System.out.print("\\multicolumntwo{4}{|c|}{");
		else
			System.out.print("\\multicolumn{4}{|c|}{");
		System.out.println("\\textbf{" + releaseNames.get(key) + " - Released " + dateFormat.format(new Date(releaseDates.get(key))) + "}}\\\\");
		System.out.println("\\hline");
		if (color)
			System.out.print("\\rowcolor{gray!10}");
		System.out.println("\\textbf{Feature} & \\textbf{Earliest Use} & \\textbf{Projects} & \\textbf{Files} \\\\");
		System.out.println("\\hline");
	}

	private static void printFooter() {
		System.out.println("\\hline");
	}

	private static void printTableHeader() {
		System.out.println("% $Id: firstuses.java,v 1.1 2013/09/15 19:50:10 rdyer Exp $");
		System.out.println("% DO NOT EDIT - This file automatically generated by section5-1/firstuses.java");
		System.out.println("\\let\\oldmc\\multicolumn");
		System.out.println("\\makeatletter");
		System.out.println("\\newcommand{\\mcinherit}{% Activate \\multicolumn inheritance");
		System.out.println("\\renewcommand{\\multicolumn}[3]{%");
		System.out.println("\\oldmc{##1}{##2}{\\ifodd\\rownum \\@oddrowcolor\\else\\@evenrowcolor\\fi ##3}%");
		System.out.println("}%");
		System.out.println("\\newcommand{\\multicolumntwo}[3]{%");
		System.out.println("\\oldmc{##1}{##2}{\\ifodd\\rownum \\@evenrowcolor\\else\\@oddrowcolor\\fi ##3}%");
		System.out.println("}}");
		System.out.println("\\makeatother");
		System.out.println("\\begin{figure}[ht]");
		System.out.println("\\centering");
		System.out.println("\\rowcolors{2}{white}{gray!10}\\mcinherit");
		System.out.println("\\begin{tabular}{|c|c|r|r|}");
	}

	private static void printTableFooter() {
		System.out.println("\\end{tabular}");
		System.out.println("\\caption{Language features are used before their release. (Note: cutoff times were midnight UTC on release date)}");
		System.out.println("\\label{tab:beta}");
		System.out.println("\\end{figure}");
	}

	private static void printEntry(final String key, final String name) {
		printEntry(key, name, null);
	}

	private static void printEntry(final String key, final String name, final String color) {
		if (!fileCounts.containsKey(key))
			fileCounts.put(key, 0);
		if (!projCounts.containsKey(key))
			projCounts.put(key, 0);
		if (color != null)
			System.out.println("\\rowcolor{" + color + "}");
		System.out.println(name + " & " +
			dateFormat.format(new Date(mins.get(key))) + " & " +
			numFormat.format(projCounts.get(key)) + " & " +
			numFormat.format(fileCounts.get(key)) + "\\\\");
	}

	private static final Matcher fileMatcher = Pattern.compile("FileUses\\[([^\\]]+)\\]\\[[^\\]]+\\]\\[(\\d+)\\] = (\\d+)").matcher("");
	private static final Matcher projMatcher = Pattern.compile("ProjectUses\\[([^\\]]+)\\]\\[(\\d+)\\] = (\\d+)").matcher("");

	private static void getData() throws IOException {
		try(
			final DataInputStream in = new DataInputStream(new FileInputStream("../section5-1/first-uses.txt"));
			final BufferedReader br = new BufferedReader(new InputStreamReader(in));
		) {
			String strLine;

			while ((strLine = br.readLine()) != null) {
				fileMatcher.reset(strLine);
				if (fileMatcher.matches()) {
					final String key = fileMatcher.group(1);
					final long time = Long.parseLong(fileMatcher.group(2)) / 1000L;

					if (time < releaseDates.get(featureMap.get(key))) {
						final int val = fileCounts.containsKey(key) ? fileCounts.get(key) : 0;
						fileCounts.put(key, Integer.parseInt(fileMatcher.group(3)) + val);

						final long min = mins.containsKey(key) ? mins.get(key) : Long.MAX_VALUE;
						if (time < min)
							mins.put(key, time);
					}

					continue;
				}

				projMatcher.reset(strLine);
				if (projMatcher.matches()) {
					final String key = projMatcher.group(1);
					final long time = Long.parseLong(projMatcher.group(2)) / 1000L;

					if (time < releaseDates.get(featureMap.get(key))) {
						final int val = projCounts.containsKey(key) ? projCounts.get(key) : 0;
						projCounts.put(key, Integer.parseInt(projMatcher.group(3)) + val);
					}
				}
			}
		}
	}
}
