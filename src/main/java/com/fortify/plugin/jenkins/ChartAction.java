/*******************************************************************************
 * (c) Copyright 2019 Micro Focus or one of its affiliates. 
 * 
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://opensource.org/licenses/MIT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.fortify.plugin.jenkins;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.NoSuchFileException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.util.ChartUtil;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.ColorPalette;
import hudson.util.DataSetBuilder;
import jenkins.model.Jenkins;

public class ChartAction implements Action {
	private static long lastChanged;

	private String projectFullName;
	private String appName;
	private String appVersion;
	private boolean isPipeline;

	public ChartAction(Job<?, ?> project, boolean isPipeline, String appName, String appVersion) {
		this.projectFullName = project.getFullName();
		this.appName = appName;
		this.appVersion = appVersion;
		this.isPipeline = isPipeline;
	}

	public String getAppName() {
		return appName;
	}

	public String getAppVersion() {
		return appVersion;
	}

	@Override
	public String getDisplayName() {
		String name = "Fortify Summary";
		if (appName != null || appVersion != null) {
			name += "(";
			if (appName != null) {
				name += appName;
				if (appVersion != null) {
					name += "-";
				}
			}
			if (appVersion != null) {
				name += appVersion;
			}
			name += ")";
		}
		return name;
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getUrlName() {
		String url = "fortify-chart";
		if (isPipeline) {
			try {
				url += URLEncoder.encode("-" + appName + "-" + appVersion, "UTF-8");
			} catch (UnsupportedEncodingException uee) {
				// just return without the app name and version
			}
		}
		return url;
	}

	public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
		DataSetBuilder<String, NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, NumberOnlyBuildLabel>();

		for (Run<?, ?> b : getBuilds()) {
			if (b.isBuilding())
				continue;
			FPRSummary fprData = new FPRSummary();
			try {
				// if the build failed we need to ignore it but not crash
				if (isPipeline) {
					fprData.load(b.getRootDir(), appName, appVersion);
				} else {
					fprData.load(b.getRootDir(), null, null);
				}
				dsb.add(fprData.getNvs(), "NVS", new NumberOnlyBuildLabel(b));
			} catch (FileNotFoundException | NoSuchFileException e) {
			}
		}

		ChartUtil.generateGraph(req, rsp, createChart(dsb.build(), appName, appVersion), 400, 200);
	}

	private Collection<Run<?, ?>> getBuilds() {
		if (projectFullName != null) {
			List<Job> allProjects = Jenkins.get().getAllItems(Job.class);
			for (Job next : allProjects) {
				if (next != null && projectFullName.equals(next.getFullName())) {
					return next.getBuilds();
				}
			}
		}
		return Collections.emptyList();
	}

	// NVS is to be removed in the following releases
	public static JFreeChart createChart(CategoryDataset dataset, String appName, String appVersion)
			throws IOException {
		String title = "Normalized Vulnerability Score (NVS)";
		if (appName != null || appVersion != null) {
			title += "(" + appName + " - " + appVersion + ")";
		}
		JFreeChart chart = ChartFactory.createLineChart(title, // chart title
				"Build ID", // categoryAxisLabel
				null, // valueAxisLabel
				dataset, PlotOrientation.VERTICAL, false, // legend
				true, // tooltips
				false // urls
		);
		chart.setBackgroundPaint(Color.white);

		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlinePaint(null);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);

		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setLowerMargin(0.0);
		domainAxis.setUpperMargin(0.0);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
		renderer.setBaseStroke(new BasicStroke(1.0f));
		ColorPalette.apply(renderer);

		return chart;
	}

	public void doCheckUpdates(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
		try {
			String stamp = req.getParameter("stamp");
			long lastUpdated = Long.parseLong(stamp);
			if (lastUpdated < lastChanged) {
				rsp.setHeader("go", "go");
			}
			rsp.setHeader("yourStampWas", String.valueOf(lastUpdated));
		} catch (NumberFormatException e) {
			rsp.setHeader("yourStampWas", "0");
			// ignore
		}
		rsp.setHeader("myStampWas", String.valueOf(lastChanged));
	}

	@Extension
	public static class RunListenerImpl extends RunListener<Run> {

		public RunListenerImpl() {
			super();
		}

		@Override
		public void onCompleted(Run run, TaskListener listener) {
			lastChanged = System.currentTimeMillis();
		}
	}
}
