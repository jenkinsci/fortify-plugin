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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import com.fortify.plugin.jenkins.bean.IssueBean;
import com.fortify.plugin.jenkins.bean.IssueFolderBean;
import com.fortify.plugin.jenkins.steps.FortifyUpload;

import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.StreamBuildListener;
import jenkins.model.Jenkins;

/**
 * Issue tables
 */
public class TableAction implements Action {

	public static enum SortOrder {
		location {
			@Override
			public String getModelSorting() {
				return "File";
			}

			@Override
			public Comparator<IssueBean> getIssueBeanComparator(final boolean reverseSort) {
				return new Comparator<IssueBean>() {
					@Override
					public int compare(IssueBean o1, IssueBean o2) {

						IssueBean first, second;
						if (reverseSort) {
							first = o2;
							second = o1;
						} else {
							first = o1;
							second = o2;
						}
						if (first.getFilePath().equals(second.getFilePath())) {
							return Integer.parseInt(first.getLineNumber()) - (Integer.parseInt(second.getLineNumber()));
						} else {
							return first.getFilePath().toLowerCase().compareTo(second.getFilePath().toLowerCase());
						}
					}
				};
			}
		};
		public abstract String getModelSorting();

		public abstract Comparator<IssueBean> getIssueBeanComparator(final boolean reverseSort);
	}

	private Long lastChanged; // split for different projects

	private long lastUpdated;

	private String projectFullName;
	private FortifyUpload manager;
	private String appName;
	private String appVersion;
	private List<IssueFolderBean> folders;

	public TableAction(Job<?, ?> project, FortifyUpload upload, String appName, String appVersion) {
		this.projectFullName = project.getFullName();
		this.manager = upload;
		this.appName = appName;
		this.appVersion = appVersion;
	}

	public String getAppName() {
		return appName;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public boolean getAccess() {
		return manager.getAccessToProject();
	}

	@Override
	public String getDisplayName() {
		String name = "Fortify Assessment";
		if (appName != null || appVersion != null) {
			name += " (";
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
		return "/plugin/fortify/icons/SSC-32x32.gif";
	}

	@Override
	public String getUrlName() {
		String url = "fortify-issues";
		if (manager.isPipeline()) {
			try {
				url += URLEncoder.encode("-" + appName + "-" + appVersion, "UTF-8");
			} catch (UnsupportedEncodingException uee) {
				// just return without the app name and version
			}
		}
		return url;
	}

	private Run<?, ?> getLastBuild() {
		Job<?, ?> project = getProject();
		if (project != null) {
			Run<?, ?> lastBuild = project.getLastBuild();
			if (lastBuild != null) {
				if (lastBuild.isBuilding()) {
					lastBuild = lastBuild.getPreviousBuild();
				}
			}
			return lastBuild;
		}
		return null;
	}

	public Job<?, ?> getProject() {
		if (projectFullName != null) {
			List<Job> allProjects = Jenkins.get().getAllItems(Job.class);
			for (Job next : allProjects) {
				if (next != null && projectFullName.equals(next.getFullName())) {
					return next;
				}
			}
		}
		return null;
	}

	public MergedBuildStatistics getBuildStats() {
		BuildStatistics lastBuildStats = getLastBuildStats();
		if (lastBuildStats != null) {
			return new MergedBuildStatistics(lastBuildStats, getPreviousBuildStats());
		}
		return null;
	}

	public BuildStatistics getLastBuildStats() {
		Run<?, ?> lastBuild = getLastBuild();
		return lastBuild == null ? null : getBuildStatsFor(lastBuild);
	}

	public BuildStatistics getPreviousBuildStats() {
		Run<?, ?> lastBuild = getLastBuild();
		Run<?, ?> previousBuild = null;
		if (lastBuild != null) {
			previousBuild = lastBuild.getPreviousBuild();
		}
		return previousBuild == null ? null : getBuildStatsFor(previousBuild);
	}

	private BuildStatistics getBuildStatsFor(Run<?, ?> build) {
		FPRSummary buildSummary = new FPRSummary();
		try {
			if (manager.isPipeline()) {
				buildSummary.load(build.getRootDir(), appName, appVersion);
			} else {
				buildSummary.load(build.getRootDir(), null, null);
			}
		} catch (IOException e) {
		}

		List<FolderStatistics> folderStats = new ArrayList<FolderStatistics>();
		for (IssueFolderBean next : buildSummary.getFolderBeans()) {
			folderStats.add(new FolderStatistics(next.getName(), next.getIssueCount()));
		}
		return new BuildStatistics(build.getDisplayName(), buildSummary.getTotalIssues(), buildSummary.getTotalIssues(),
				folderStats);
	}

	public static class FolderStatistics {
		private String name;
		private int issueCount;

		public FolderStatistics(String name, int issueCount) {
			this.name = name;
			this.issueCount = issueCount;
		}

		public String getName() {
			return name;
		}

		public int getIssueCount() {
			return issueCount;
		}
	}

	public static class MergedFolderStatistics extends FolderStatistics {
		private FolderStatistics second;

		public MergedFolderStatistics(FolderStatistics first, FolderStatistics second) {
			super(first.name, first.issueCount);
			this.second = second;
		}

		public boolean isHasPrev() {
			return second != null;
		}

		public int getPrevIssueCount() {
			return isHasPrev() ? second.getIssueCount() : -1;
		}

		public boolean isLess() {
			return isHasPrev() && (getIssueCount() < getPrevIssueCount());
		}

		public boolean isMore() {
			return isHasPrev() && (getIssueCount() > getPrevIssueCount());
		}
	}

	public static class BuildStatistics {
		private String name;
		private int totalIssues;
		private int newIssues;
		private List<? extends FolderStatistics> folderValues;

		public BuildStatistics(String name, int totalIssues, int newIssues,
				List<? extends FolderStatistics> folderValues) {
			this.name = name;
			this.totalIssues = totalIssues;
			this.newIssues = newIssues;
			this.folderValues = folderValues;
		}

		public String getName() {
			return name;
		}

		public String getTotal() {
			return String.valueOf(totalIssues);
		}

		public String getNew() {
			return String.valueOf(newIssues);
		}

		public List<? extends FolderStatistics> getFolders() {
			return folderValues;
		}
	}

	public static final class MergedBuildStatistics extends BuildStatistics {
		private BuildStatistics second;

		public MergedBuildStatistics(BuildStatistics first, BuildStatistics second) {
			super(first.name, first.totalIssues, first.newIssues, mergeFolders(first, second));
			this.second = second;
		}

		private static List<MergedFolderStatistics> mergeFolders(BuildStatistics first, BuildStatistics second) {
			List<MergedFolderStatistics> result = new ArrayList<MergedFolderStatistics>();
			for (FolderStatistics next : first.getFolders()) {
				if (IssueFolderBean.ATTRIBUTE_VALUE_ALL.equals(next.getName())) {
					// skip because we already have 'total'
					continue;
				}
				FolderStatistics prev = null;
				if (second != null) {
					for (FolderStatistics other : second.getFolders()) {
						if (other.getName().equals(next.getName())) {
							prev = other;
							break;
						}
					}
				}
				result.add(new MergedFolderStatistics(next, prev));
			}
			return result;
		}

		public boolean isHasPrev() {
			return second != null;
		}

		public String getPrevBuildName() {
			return isHasPrev() ? second.getName() : null;
		}

		public int getPrevTotal() {
			return isHasPrev() ? second.totalIssues : -1;
		}

		public int getPrevNew() {
			return isHasPrev() ? second.newIssues : -1;
		}

		public boolean isLess() {
			return isHasPrev() && (super.totalIssues < getPrevTotal());
		}

		public boolean isMore() {
			return isHasPrev() && (super.totalIssues > getPrevTotal());
		}
	}

	private boolean isUpdateNeeded() {
		if (manager.isSettingUpdated() || !manager.getAccessToProject()) {
			return true;
		}
		return getLastChanged() > lastUpdated;
	}

	long getLastChanged() {
		if (lastChanged == null) {
			lastChanged = Long.valueOf(0);
		}
		return lastChanged.longValue();
	}

	void setLastChanged(long currentTimeMillis) {
		lastChanged = Long.valueOf(currentTimeMillis);
	}

	public synchronized List<IssueFolderBean> getFolders() {
		if (folders == null || isUpdateNeeded()) {
			lastUpdated = System.currentTimeMillis();
			folders = manager.getFolders(new StreamBuildListener(System.out, Charset.defaultCharset()));
		}
		return folders;
	}

	public void doSetPageSize(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
		String size = req.getParameter("size");
		Integer hadValue = (Integer) req.getSession().getAttribute("pageSize");
		if (hadValue == null) {
			hadValue = Integer.valueOf(manager.getIssuePageSize());
		}
		if (StringUtils.isNotBlank(size)) {
			try {
				int pageSize = Integer.parseInt(size);
				if (pageSize != hadValue.intValue()) {
					View view = (View) req.getSession().getAttribute("currentView");
					if (view != null) {
						view.setPage(0);
						view.setPageSize(pageSize);
					}
				}
				req.getSession().setAttribute("pageSize", Integer.valueOf(pageSize));
			} catch (NumberFormatException e) {
				// ignore
			}
		}
		doAjaxIssues(req, rsp);
	}

	public void doSelectedGrouping(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {

	}

	// we removed that functionality because it stopped working with the REST client
	// API, need to re-think a little
	public void doShowAllNotNew(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
		String all = req.getParameter("all");
		if (StringUtils.isBlank(all)) {
			return;
		}
		boolean showingAllNotNew = "yes".equalsIgnoreCase(all.trim());
		Boolean hadValue = (Boolean) req.getSession().getAttribute("showingAllNotNew");
		if (hadValue == null) {
			hadValue = Boolean.TRUE;
		}
		if (showingAllNotNew != hadValue) {
			View view = (View) req.getSession().getAttribute("currentView");
			if (view != null) {
				view.setPage(0);
				view.setShowingAllNotNew(showingAllNotNew);
			}
		}
		req.getSession().setAttribute("showingAllNotNew", Boolean.valueOf(showingAllNotNew));
		doAjaxIssues(req, rsp);
	}

	public void doUpdateIssueList(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
		String viewName = req.getParameter("folder");
		if (StringUtils.isBlank(viewName)) {
			return;
		}
		String page = req.getParameter("page");
		int pageNum = 0;
		if (StringUtils.isNotBlank(page)) {
			try {
				pageNum = Integer.parseInt(page);
			} catch (NumberFormatException e) {
				// ignore
			}
		}

		View view = (View) req.getSession().getAttribute("currentView");
		if (view != null) {
			view.setFolder(getFolderByName(viewName));
			view.setPage(pageNum);
			req.getSession().setAttribute("currentView", view);
		}
		doAjaxIssues(req, rsp);
	}

	public void doAjaxIssues(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
		Object currView = req.getSession().getAttribute("currentView");
		if ("yes".equalsIgnoreCase(req.getParameter("firstTime")) || currView == null) {
			List<IssueFolderBean> folders = getFolders();
			if (!folders.isEmpty()) {
				currView = new View(folders.get(0), manager, 0);
				req.getSession().setAttribute("currentView", currView);
			}
			req.getSession().setAttribute("showingAllNotNew", Boolean.TRUE);
			req.getSession().setAttribute("pageSize", Integer.valueOf(manager.getIssuePageSize()));
		}
		rsp.setContentType("text/html;charset=UTF-8");
		ensureNoCaching(rsp);
		req.getView(this, "issuesByFriorityTable.jelly").forward(req, rsp);
	}

	public void doAjaxStats(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
		rsp.setContentType("text/html;charset=UTF-8");
		ensureNoCaching(rsp);
		req.getView(this, "issueCountTable.jelly").forward(req, rsp);
	}

	public synchronized void doCheckUpdates(StaplerRequest req, StaplerResponse rsp)
			throws IOException, ServletException {
		long lastChanged = getLastChanged();
		try {
			String stamp = req.getParameter("stamp");
			long lastUpdated = Long.parseLong(stamp);
			if (lastUpdated < lastChanged) {
				rsp.setHeader("go", "go");
				View view = (View) req.getSession().getAttribute("currentView");
				if (view != null) {
					IssueFolderBean folder = getFolderByName(view.getFolder().getName());
					if (folder != null) {
						view.setFolder(folder);
					}
					req.getSession().setAttribute("currentView", view);
				}
			}
			rsp.setHeader("yourStampWas", String.valueOf(lastUpdated));
		} catch (NumberFormatException e) {
			rsp.setHeader("yourStampWas", "0");
			// ignore
		}
		rsp.setHeader("myStampWas", String.valueOf(lastChanged));
		ensureNoCaching(rsp);
	}

	private void ensureNoCaching(StaplerResponse rsp) {
		// To ensure that IE doesn't cache the results of an Ajax call
		rsp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate"); // HTTP 1.1
		rsp.setHeader("Pragma", "no-cache"); // HTTP 1.0
		rsp.setDateHeader("Expires", 0); // prevents caching at the proxy server
	}

	private IssueFolderBean getFolderByName(String name) {
		for (IssueFolderBean next : getFolders()) {
			if (next.getName().equals(name)) {
				return next;
			}
		}
		return null;
	}

	public static class View implements Comparable {
		private final FortifyUpload manager;
		private IssueFolderBean folder;
		private int page;
		private SortOrder sortOrder;
		private boolean sortDownNotUp;
		private List<IssueBean> issuesByFolder;
		private Boolean needsUpdate;
		private int pageSize;
		private boolean showingAllNotNew;
		private String SelectedGrouping;

		public View(IssueFolderBean descriptor, FortifyUpload manager, int pageNum) {
			this.folder = descriptor;
			this.manager = manager;
			this.page = pageNum;
			sortDownNotUp = false;
			sortOrder = SortOrder.location;
			pageSize = manager.getIssuePageSize();
			showingAllNotNew = true;
			SelectedGrouping = "Category";
			scheduleUpdate();
		}

		@JavaScriptMethod
		public String getSelectedGrouping() {
			return SelectedGrouping;
		}

		@JavaScriptMethod
		public void setSelectedGrouping(String selectedGrouping) {

		}

		@JavaScriptMethod
		public String getDisplayName() {
			int issueCount = showingAllNotNew ? folder.getIssueCount() : folder.getIssueNewCount();
			if (pageSize == -1) {
				pageSize = issueCount;
			}
			List<IssueBean> issues = getIssues();
			int hasItems = issues != null ? issues.size() : 0;
			if (hasItems == 0) {
				return String.format(page == 0 ? "%s (No Issues)" : "%s (No New Issues)", folder.getName());
			}
			int firstItem = page * pageSize;
			int shownItems = firstItem + hasItems;
			return String.format("%s (%d to %d out of %d)", folder.getName(), firstItem + 1, shownItems, issueCount);
		}

		@JavaScriptMethod
		public void setPage(int pageNum) {
			if (pageNum != page) {
				this.page = pageNum;
				scheduleUpdate();
			}
		}

		@JavaScriptMethod
		public int getPage() {
			return page;
		}

		@JavaScriptMethod
		public int getNextPage() {
			return page + 1;
		}

		@JavaScriptMethod
		public int getPreviousPage() {
			return page - 1;
		}

		@JavaScriptMethod
		public IssueFolderBean getFolder() {
			return folder;
		}

		@JavaScriptMethod
		public String getSortOrder() {
			return sortOrder == null ? "" : sortOrder.name();
		}

		@JavaScriptMethod
		public boolean getSortDownNotUp() {
			return sortDownNotUp;
		}

		@JavaScriptMethod
		public List<IssueBean> getIssues() {
			if (folder.isEmpty()) {
				return Collections.emptyList();
			}
			if (needsUpdate()) {
				needsUpdate = Boolean.FALSE;
				issuesByFolder = manager.getIssuesByFolder(folder.getId(), page, pageSize, sortOrder, sortDownNotUp,
						showingAllNotNew, this.getSelectedGrouping(),
						new StreamBuildListener(System.out, Charset.defaultCharset()));
			}
			return issuesByFolder;
		}

		private boolean needsUpdate() {
			if (needsUpdate == null) {
				needsUpdate = Boolean.TRUE;
			}
			return needsUpdate.booleanValue();
		}

		@JavaScriptMethod
		public void scheduleUpdate() {
			needsUpdate = Boolean.TRUE;
		}

		@JavaScriptMethod
		public boolean isHasNext() {
			if (pageSize == -1 || folder.isEmpty()) {
				return false;
			}
			int shown = (page + 1) * pageSize;
			int total = showingAllNotNew ? folder.getIssueCount() : folder.getIssueNewCount();
			return total > shown;
		}

		@JavaScriptMethod
		public boolean isHasPrevious() {
			if (pageSize == -1 || folder.isEmpty()) {
				return false;
			}
			return page > 0;
		}

		@Override
		public int compareTo(Object o) {
			View otherView = (View) o;
			return otherView.getFolder().compareTo(getFolder());
		}

		@JavaScriptMethod
		public void setSortOrder(SortOrder order) {
			sortOrder = order;
		}

		@JavaScriptMethod
		public void setFolder(IssueFolderBean folder) {
			this.folder = folder;
			scheduleUpdate();
		}

		@JavaScriptMethod
		public void setPageSize(int pageSize) {
			this.pageSize = pageSize;
			scheduleUpdate();
		}

		@JavaScriptMethod
		public void setShowingAllNotNew(boolean showingAllNotNew) {
			this.showingAllNotNew = showingAllNotNew;
			scheduleUpdate();
		}
	}
}
