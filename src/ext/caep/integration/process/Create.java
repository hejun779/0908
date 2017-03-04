package ext.caep.integration.process;

import java.util.List;
import java.util.Map;

import ext.caep.integration.bean.File;
import ext.caep.integration.bean.Files;
import ext.caep.integration.bean.Global;
import ext.caep.integration.bean.Para;
import ext.caep.integration.bean.Project;
import ext.caep.integration.bean.Software;
import ext.caep.integration.bean.Task;
import ext.caep.integration.util.Constant;
import ext.caep.integration.util.IntegrationUtil;
import wt.part.WTPart;

/**
 * 
 * 创建方案任务树相关对象
 *
 */
public class Create {

	private Project currentProject;
	private Task currentTask;
	private Software currentSoftware;
	private String currentFolder;
	private String parentNumber;
	private String filePath;
	// 创建File的时候需要将项目或者Task的作为编号的前缀
	private Object numberPrefixObj;

	public Create(Map<String, Object> parameters) {
		this.currentProject = (Project) parameters.get("currentProject");
		this.currentTask = (Task) parameters.get("currentTask");
		this.currentSoftware = (Software) parameters.get("currentSoftware");
		// this.currentPara = (Para) parameters.get("currentPara");
		this.currentFolder = (String) parameters.get("currentFolder");
		this.parentNumber = (String) parameters.get("parentNumber");
		this.numberPrefixObj = parameters.get("numberPrefixObj");
		this.filePath = (String) parameters.get("filePath");
	}

	public void process(Object root) throws Exception {
		if (root instanceof Global) {
			createGlobal((Global) root);
		} else if (root instanceof Project) {
			createProject((Project) root);
		} else if (root instanceof Task) {
			createTask((Task) root);
		} else if (root instanceof Software) {
			createSoftware((Software) root);
		} else if (root instanceof Para) {
			createPara((Para) root);
		} else if (root instanceof File) {
			File file = (File) root;
			if (numberPrefixObj != null) {
				if (numberPrefixObj instanceof Project) {
					file.newDocument(parentNumber, currentProject, this.filePath, currentFolder);
				} else if (numberPrefixObj instanceof Task) {
					file.newDocument(parentNumber, currentTask, this.filePath, currentFolder);
				} else if (numberPrefixObj instanceof Para) {
					if (this.currentFolder == null) {
						if (this.currentSoftware == null) {
							Para para = (Para) numberPrefixObj;
							WTPart paraPart = IntegrationUtil.getPartFromNumber(para.getID());
							if (paraPart != null) {
								WTPart softwarePart = IntegrationUtil.getParent(paraPart);
								if (softwarePart != null) {
									this.currentSoftware = new Software(softwarePart);
									this.currentFolder = softwarePart.getName() + "文件";
								}
							} else {
								throw new Exception("ID为" + para.getID() + "的计算参数不存在.");
							}
						}
					}

					if (this.currentTask == null) {
						if (this.currentSoftware != null) {
							WTPart softwarePart = IntegrationUtil.getPartFromNumber(currentSoftware.getID());
							WTPart taskPart = IntegrationUtil.getParent(softwarePart);
							if (taskPart != null) {
								this.currentTask = new Task(taskPart);
							} else {
								throw new Exception("ID为" + currentSoftware.getID() + "的专有软件的计算任务不存在.");
							}
						}
					}

					file.newDocument(parentNumber, currentTask, this.filePath, this.currentFolder);
				}
			} else {
				throw new Exception("找不到名称为" + file.getName() + "的文档的父节点.");
			}
		}

	}

	private void createGlobal(Global root) throws Exception {
		List<Project> projects = root.getProjects();
		if (projects != null && !projects.isEmpty()) {
			if (IntegrationUtil.isAdmin()) {
				for (Project project : projects) {
					createProject(project);
				}
			} else {
				throw new Exception("不是方案管理员,不能创建方案");
			}
		}
	}

	private void createProject(Project project) throws Exception {
		if (IntegrationUtil.isAdmin()) {
			project.newProject();
			this.currentProject = project;
			this.numberPrefixObj = project;
			this.parentNumber = project.getID();
			Files files = project.getFiles();
			if (files != null && files.getFiles() != null && !files.getFiles().isEmpty()) {
				for (File projectFile : files.getFiles()) {
					projectFile.newDocument(project.getID(), project, this.filePath, Constant.FOLDER_PROJECT);
				}
			}
			List<Task> tasks = project.getTasks();
			if (tasks != null && !tasks.isEmpty()) {
				for (Task task : tasks) {
					createTask(task);
				}
			}
			project.endProject();
		} else {
			throw new Exception("不是方案管理员,不能创建方案");
		}
	}

	private void createTask(Task task) throws Exception {
		task.newTask(currentProject);
		this.currentTask = task;
		this.numberPrefixObj = task;
		this.parentNumber = task.getID();
		// 创建计算任务附属文件
		Files files = task.getFiles();
		if (files != null && files.getFiles() != null && !files.getFiles().isEmpty()) {
			for (File taskFile : files.getFiles()) {
				taskFile.newDocument(task.getID(), task, this.filePath, Constant.FOLDER_PROJECT);
			}
		}
		// 创建专有软件
		List<Software> softwares = task.getSoftwares();
		if (softwares != null && !softwares.isEmpty()) {
			for (Software software : softwares) {
				createSoftware(software);
			}
		}
		task.endTask();
	}

	private void createSoftware(Software software) throws Exception {
		software.newSoftware(currentTask.getID());
		this.parentNumber = software.getID();
		this.currentSoftware = software;
		this.currentFolder = software.getName() + "文件";
		// 创建参数
		List<Para> paras = software.getParas();
		if (paras != null && !paras.isEmpty()) {
			for (Para para : paras) {
				createPara(para);
			}
		}
		software.endSoftware();
	}

	private void createPara(Para para) throws Exception {
		para.newPara(currentSoftware.getID());
		// 创建输入输出文件
		List<File> paraFiles = para.getFiles();
		if (paraFiles != null && !paraFiles.isEmpty()) {
			for (File paraFile : paraFiles) {
				paraFile.newDocument(para.getID(), currentTask, this.filePath, this.currentFolder);
			}
		}
		para.endPara();
	}
}