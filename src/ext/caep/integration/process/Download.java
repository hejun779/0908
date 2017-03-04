package ext.caep.integration.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ext.caep.integration.bean.File;
import ext.caep.integration.bean.Files;
import ext.caep.integration.bean.Global;
import ext.caep.integration.bean.Para;
import ext.caep.integration.bean.Project;
import ext.caep.integration.bean.Software;
import ext.caep.integration.bean.Task;
import ext.caep.integration.util.Constant;
import ext.caep.integration.util.IBAUtil;
import ext.caep.integration.util.IntegrationUtil;
import wt.doc.WTDocument;
import wt.part.WTPart;

/**
 * 可以从任意节点(Global,方案,计算任务,专有软件,计算参数,输入输出文件,附属文件)开始相关文档,如果子节点有内容,则只下载子节点的相关文档,
 * 如果子节点没有内容,则下载全部相关文档
 *
 */
public class Download {
	private Map<String, Object> parameters = new HashMap<String, Object>();
	private String filePath;

	public Download(Map<String, Object> parameters) {
		if (parameters != null) {
			this.parameters = parameters;
			this.filePath = (String) parameters.get("filePath");
		}
	}

	public Object process(Object root) throws Exception {
		if (root instanceof Global) {
			downloadGlobal((Global) root);
		} else if (root instanceof Project) {
			downloadProject((Project) root);
		} else if (root instanceof Task) {
			downloadTask((Task) root);
		} else if (root instanceof Software) {
			downloadSoftware((Software) root);
		} else if (root instanceof Para) {
			downloadPara((Para) root);
		} else if (root instanceof File) {
			((File) root).download(this.parameters, this.filePath, root);
		}
		return root;
	}

	private void downloadGlobal(Global global) throws Exception {
		List<Project> projects = global.getProjects();
		if (projects != null && !projects.isEmpty()) {
			for (Project project : projects) {
				downloadProject(project);
			}
		} else {
			List<WTPart> allProjects = IntegrationUtil.getAllProject();
			if (allProjects != null && !allProjects.isEmpty()) {
				List<Project> newProjects = new ArrayList<Project>();
				for (WTPart projectPart : allProjects) {
					Project project = new Project(projectPart);
					downloadProject(project);
					newProjects.add(project);
				}
				global.setProjects(newProjects);
			}
		}
	}

	private void downloadProject(Project project) throws Exception {
		WTPart projectPart = IntegrationUtil.getPartFromNumber(project.getID());
		if (projectPart != null) {
			this.parameters.put("currentProject", project);
			project.setName(projectPart.getName());
			project.setState("");
			IBAUtil iba = new IBAUtil(projectPart);
			String describe = StringUtils.trimToEmpty(iba.getIBAValue(Constant.ATTR_DESCRIBE));
			project.setDescribe(describe);
			Files files = project.getFiles();
			if (files != null && files.getFiles() != null && !files.getFiles().isEmpty()) {
				for (File file : files.getFiles()) {
					file.download(this.parameters, filePath, project);
				}
			} else {
				List<File> allFiles = downloadAllFileForPart(projectPart, project);
				if (allFiles != null && !allFiles.isEmpty()) {
					Files newFiles = new Files();
					newFiles.setFiles(allFiles);
					project.setFiles(newFiles);
				}
			}
			List<Task> tasks = project.getTasks();
			if (tasks != null && !tasks.isEmpty()) {
				for (Task task : tasks) {
					downloadTask(task);
				}
			} else {
				List<WTPart> taskParts = IntegrationUtil.getChildren(projectPart);
				if (!IntegrationUtil.isAdmin()) {
					taskParts = IntegrationUtil.filter(taskParts);
				}
				if (taskParts != null && !taskParts.isEmpty()) {
					tasks = new ArrayList<Task>();
					for (WTPart taskPart : taskParts) {
						Task task = new Task(taskPart);
						downloadTask(task);
						tasks.add(task);
					}
					project.setTasks(tasks);
				}
			}
		} else {
			throw new Exception("ID为" + project.getID() + "的方案不存在");
		}
	}

	private void downloadTask(Task task) throws Exception {
		WTPart taskPart = IntegrationUtil.getPartFromNumber(task.getID());
		if (taskPart != null) {
			this.parameters.put("currentTask", task);
			task.setState("");
			IBAUtil iba = new IBAUtil(taskPart);
			String describe = StringUtils.trimToEmpty(iba.getIBAValue(Constant.ATTR_DESCRIBE));
			task.setDescribe(describe);
			task.setName(taskPart.getName());
			Files files = task.getFiles();
			if (files != null && files.getFiles() != null && !files.getFiles().isEmpty()) {
				List<File> fileList = files.getFiles();
				for (File file : fileList) {
					file.download(this.parameters, filePath, task);
				}
			} else {
				List<File> allFiles = downloadAllFileForPart(taskPart, task);
				if (allFiles != null && !allFiles.isEmpty()) {
					Files newFiles = new Files();
					newFiles.setFiles(allFiles);
					task.setFiles(newFiles);
				}
			}
			List<Software> softwares = task.getSoftwares();
			if (softwares != null && !softwares.isEmpty()) {
				for (Software software : softwares) {
					downloadSoftware(software);
				}
			} else {
				List<WTPart> softwareParts = IntegrationUtil.getChildren(taskPart);
				if (softwareParts != null && !softwareParts.isEmpty()) {
					List<Software> allSoftwares = new ArrayList<Software>();
					for (WTPart softwarePart : softwareParts) {
						Software software = new Software(softwarePart);
						downloadSoftware(software);
						allSoftwares.add(software);
					}
					task.setSoftwares(allSoftwares);
				}
			}
		} else {
			throw new Exception("ID为" + task.getID() + "的计算任务不存在");
		}
	}

	private void downloadSoftware(Software software) throws Exception {
		List<Para> paras = software.getParas();
		if (paras != null && !paras.isEmpty()) {
			for (Para para : paras) {
				downloadPara(para);
			}
		} else {
			WTPart softwarePart = IntegrationUtil.getPartFromNumber(software.getID());
			if (softwarePart != null) {
				this.parameters.put("currentSoftware", software);
				software.setState("");
				software.setName(softwarePart.getName());
				List<WTPart> allParas = IntegrationUtil.getChildren(softwarePart);
				if (allParas != null && !allParas.isEmpty()) {
					paras = new ArrayList<Para>();
					for (WTPart paraPart : allParas) {
						Para para = new Para(paraPart);
						downloadPara(para);
						paras.add(para);
					}
					software.setParas(paras);
				}
			} else {
				throw new Exception("ID为" + software.getID() + "的专有软件不存在");
			}
		}
	}

	private void downloadPara(Para para) throws Exception {
		List<File> files = para.getFiles();
		if (files != null && !files.isEmpty()) {
			for (File file : files) {
				file.download(this.parameters, filePath, para);
			}
		} else {
			WTPart paraPart = IntegrationUtil.getPartFromNumber(para.getID());
			if (paraPart != null) {
				this.parameters.put("currentPara", para);
				para.setState("");
				para.setName(paraPart.getName());
				List<File> allFiles = downloadAllFileForPart(paraPart, para);
				para.setFiles(allFiles);
			} else {
				throw new Exception("ID为" + para.getID() + "的计算参数不存在");
			}
		}
	}

	private List<File> downloadAllFileForPart(WTPart part, Object hierarchyIndex) throws Exception {
		List<File> files = new ArrayList<File>();
		List<WTDocument> docs = IntegrationUtil.getDescribeDoc(part);
		if (docs != null && !docs.isEmpty()) {
			for (WTDocument doc : docs) {
				File file = new File(doc);
				file.download(this.parameters, this.filePath, hierarchyIndex);
				files.add(file);
			}
		}
		return files;
	}
}
