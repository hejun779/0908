package ext.caep.integration.process;

import java.util.List;

import ext.caep.integration.bean.File;
import ext.caep.integration.bean.Files;
import ext.caep.integration.bean.Global;
import ext.caep.integration.bean.Para;
import ext.caep.integration.bean.Project;
import ext.caep.integration.bean.Software;
import ext.caep.integration.bean.Task;
import ext.caep.integration.util.Constant;
import wt.pom.PersistenceException;
import wt.pom.Transaction;

/**
 * 
 * 创建方案任务树相关对象
 *
 */
public class Create {

	public Create() {

	}

	public void process(Object root) {
		if (root instanceof Global) {
			createGlobal((Global) root);
		} else if (root instanceof Project) {
			createProject((Project) root);
		} else if (root instanceof Task) {
			createTask(null, (Task) root);
		} else if (root instanceof Software) {
			createSoftware(null, (Software) root);
		} else if (root instanceof Para) {
			createPara(null, null, (Para) root);
		} else if (root instanceof File) {
			File file = (File) root;
			file.newDocument(null, "/Default");
		}
	}

	private void createGlobal(Global root) {
		Transaction ts = new Transaction();
		try {
			ts.start();
			List<Project> projects = root.getProjects();
			for (Project project : projects) {
				createProject(project);
			}
			ts.commit();
			ts = null;
		} catch (PersistenceException e) {
			e.printStackTrace();

		} finally {
			if (ts != null) {
				ts.rollback();
				ts = null;
			}
		}
	}

	private void createProject(Project project) {
		project.newProject();
		Files files = project.getFiles();
		if (files != null && files.getFiles() != null && !files.getFiles().isEmpty()) {
			for (File projectFile : files.getFiles()) {
				projectFile.newDocument(project.getID(), Constant.FOLDER_PROJECT);
			}
		}
		List<Task> tasks = project.getTasks();
		if (tasks != null && !tasks.isEmpty()) {
			for (Task task : tasks) {
				createTask(project.getID(), task);
			}
		}
		project.endProject();
	}

	private void createTask(String parentNumber, Task task) {
		task.newTask(parentNumber);
		// 创建计算任务附属文件
		Files files = task.getFiles();
		if (files != null && files.getFiles() != null && !files.getFiles().isEmpty()) {
			for (File taskFile : files.getFiles()) {
				taskFile.newDocument(task.getID(), Constant.FOLDER_PROJECT);
			}
		}
		// 创建专有软件
		List<Software> softwares = task.getSoftwares();
		if (softwares != null && !softwares.isEmpty()) {
			for (Software software : softwares) {
				createSoftware(task.getID(), software);
			}
		}
		task.endTask();
	}

	private void createSoftware(String parentNumber, Software software) {
		software.newSoftware(parentNumber);
		// 创建参数
		List<Para> paras = software.getParas();
		if (paras != null && !paras.isEmpty()) {
			for (Para para : paras) {
				createPara(software.getID(), software.getName() + "文件", para);
			}
		}
		software.endSoftware();
	}

	private void createPara(String parentNumber, String folder, Para para) {
		para.newPara(parentNumber);
		// 创建输入输出文件
		List<File> paraFiles = para.getFiles();
		if (paraFiles != null && !paraFiles.isEmpty()) {
			for (File paraFile : paraFiles) {
				createFile(para.getID(), folder, paraFile);
			}
		}
		para.endPara();
	}

	private void createFile(String parentNumber, String folder, File file) {
		file.newDocument(parentNumber, folder);
	}
}