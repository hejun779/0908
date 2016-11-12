package ext.caep.integration.process;

import java.util.ArrayList;
import java.util.List;

import ext.caep.integration.bean.File;
import ext.caep.integration.bean.Files;
import ext.caep.integration.bean.Global;
import ext.caep.integration.bean.Para;
import ext.caep.integration.bean.Project;
import ext.caep.integration.bean.Software;
import ext.caep.integration.bean.Task;
import ext.caep.integration.util.IntegrationUtil;
import wt.doc.WTDocument;
import wt.part.WTPart;

/**
 * 可以从任意节点(Global,方案,计算任务,专有软件,计算参数,输入输出文件,附属文件)开始相关文档,如果子节点有内容,则只下载子节点的相关文档,
 * 如果子节点没有内容,则下载全部相关文档
 *
 */
public class Download {
	public static Object process(Object root) {
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
			((File) root).download();
		}
		return root;
	}

	private static void downloadGlobal(Global global) {
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

	private static void downloadProject(Project project) {
		WTPart projectPart = IntegrationUtil.getPartFromNumber(project.getID());
		if (projectPart != null) {
			Files files = project.getFiles();
			if (files != null && files.getFiles() != null && !files.getFiles().isEmpty()) {
				for (File file : files.getFiles()) {
					file.download();
				}
			} else {
				List<File> allFiles = downloadAllFileForPart(projectPart);
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
		}
	}

	private static void downloadTask(Task task) {
		WTPart taskPart = IntegrationUtil.getPartFromNumber(task.getID());
		if (taskPart != null) {
			Files files = task.getFiles();
			if (files != null && files.getFiles() != null && !files.getFiles().isEmpty()) {
				List<File> fileList = files.getFiles();
				for (File file : fileList) {
					file.download();
				}
			} else {
				List<File> allFiles = downloadAllFileForPart(taskPart);
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
		}
	}

	private static void downloadSoftware(Software software) {
		List<Para> paras = software.getParas();
		if (paras != null && !paras.isEmpty()) {
			for (Para para : paras) {
				downloadPara(para);
			}
		} else {
			WTPart softwarePart = IntegrationUtil.getPartFromNumber(software.getID());
			if (softwarePart != null) {
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
			}
		}
	}

	private static void downloadPara(Para para) {
		List<File> files = para.getFiles();
		if (files != null && !files.isEmpty()) {
			for (File file : files) {
				file.download();
			}
		} else {
			WTPart paraPart = IntegrationUtil.getPartFromNumber(para.getID());
			List<File> allFiles = downloadAllFileForPart(paraPart);
			para.setFiles(allFiles);
		}
	}

	private static List<File> downloadAllFileForPart(WTPart part) {
		List<File> files = new ArrayList<File>();
		List<WTDocument> docs = IntegrationUtil.getDescribeDoc(part);
		if (docs != null && !docs.isEmpty()) {
			for (WTDocument doc : docs) {
				File file = new File(doc);
				file.download();
				files.add(file);
			}
		}
		return files;
	}
}
