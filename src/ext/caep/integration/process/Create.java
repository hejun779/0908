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
import wt.pom.PersistenceException;

/**
 * 
 * 创建方案任务树相关对象
 *
 */
public class Create {

	public Create() {

	}

	private Project currentProject;
	private Task currentTask;
	private Software currentSoftware;
	// private Para currentPara;
	private String currentFolder;
	private String parentNumber;
	// 创建File的时候需要将项目或者Task的作为编号的前缀
	private Object numberPrefixObj;

	public void process(Map<String, Object> parameters, Object root) throws Exception {
		this.currentProject = (Project) parameters.get("currentProject");
		this.currentTask = (Task) parameters.get("currentTask");
		this.currentSoftware = (Software) parameters.get("currentSoftware");
		// this.currentPara = (Para) parameters.get("currentPara");
		this.currentFolder = (String) parameters.get("currentFolder");
		this.parentNumber = (String) parameters.get("parentNumber");
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
				if (numberPrefixObj instanceof Project || numberPrefixObj instanceof Task) {
					file.newDocument(parentNumber, currentProject, currentFolder);
				} else if (numberPrefixObj instanceof Para) {
					file.newDocument(parentNumber, currentTask, currentFolder);
				}
			}
		}

	}

	private void createGlobal(Global root) throws Exception {
		try {
			List<Project> projects = root.getProjects();
			if (projects != null && !projects.isEmpty()) {
				for (Project project : projects) {
					createProject(project);
				}
			}
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
	}

	private void createProject(Project project) throws Exception {
		project.newProject();
		this.currentProject = project;
		this.numberPrefixObj = project;
		this.parentNumber = project.getID();
		Files files = project.getFiles();
		if (files != null && files.getFiles() != null && !files.getFiles().isEmpty()) {
			for (File projectFile : files.getFiles()) {
				projectFile.newDocument(project.getID(), project, Constant.FOLDER_PROJECT);
			}
		}
		List<Task> tasks = project.getTasks();
		if (tasks != null && !tasks.isEmpty()) {
			for (Task task : tasks) {
				createTask(task);
			}
		}
		project.endProject();
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
				taskFile.newDocument(task.getID(), task, Constant.FOLDER_PROJECT);
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
				paraFile.newDocument(para.getID(), currentTask, this.currentFolder);
			}
		}
		para.endPara();
	}
}