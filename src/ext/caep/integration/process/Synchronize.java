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
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class Synchronize extends DataOperation {

	public void process(Object root) throws Exception {
		if (root instanceof Global) {
			syncGlobal((Global) root);
		} else if (root instanceof Project) {
			syncProject((Project) root);
		} else if (root instanceof Task) {
			syncTask((Task) root);
		} else if (root instanceof Software) {
			syncSoftware((Software) root);
		} else if (root instanceof Para) {
			syncPara((Para) root);
		} else if (root instanceof File) {
			syncFile((File) root);
		}
	}

	/**
	 * 同步全部数据
	 * 
	 * @param old
	 * @return
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	private void syncGlobal(Global old) throws WTPropertyVetoException, WTException {
		old.setState("");
		List<WTPart> projects = IntegrationUtil.getAllProject();
		List<Project> projectBeans = new ArrayList<Project>();
		for (WTPart project : projects) {
			Project oldProject = null;
			// 获取客户端提交的相同ID的方案数据
			if (old.getProjects() != null) {
				for (Project o : old.getProjects()) {
					if (project.getNumber().equals(o.getID())) {
						oldProject = o;
						break;
					}
				}
			}
			projectBeans.add(syncProject(project, oldProject));
		}
		if (!projectBeans.isEmpty()) {
			old.setProjects(projectBeans);
		}
	}

	/**
	 * 同步方案
	 * 
	 * @param root
	 * @throws WTException
	 */

	private Project syncProject(WTPart project, Project old) throws WTException {
		Project projectBean = new Project(project);
		List<WTPart> tasks = IntegrationUtil.getChildren(project);
		List<Task> taskBeans = new ArrayList<Task>();
		if (!IntegrationUtil.isAdmin()) {
			tasks = IntegrationUtil.filter(tasks);
		}
		for (WTPart task : tasks) {
			Task oldTask = null;
			if (old != null && old.getTasks() != null) {
				for (Task o : old.getTasks()) {
					if (task.getNumber().equals(o.getID())) {
						oldTask = o;
						break;
					}
				}
			}
			taskBeans.add(syncTask(task, oldTask));
		}
		if (!taskBeans.isEmpty()) {
			projectBean.setTasks(taskBeans);
		}
		List<File> files = syncFiles(project);
		if (files != null && !files.isEmpty()) {
			Files file = new Files();
			file.setFiles(files);
			if (old != null && old.getFiles() != null) {
				copyFilePath(old.getFiles(), file);
			}
			projectBean.setFiles(file);
		}
		return projectBean;
	}

	private void syncProject(Project old) throws Exception {
		WTPart project = IntegrationUtil.getPartFromNumber(old.getID());
		if (project != null) {
			Project newProject = syncProject(project, old);
			old.setFiles(newProject.getFiles());
			old.setTasks(newProject.getTasks());
			old.setState("");
			old.setName(newProject.getName());
			old.setDescribe(newProject.getDescribe());
			old.setType(newProject.getType());
		} else {
			throw new Exception("ID为" + old.getID() + "的方案不存在");
		}
	}

	/**
	 * 同步计算任务
	 * 
	 * @param oldOne
	 * @return
	 * @throws Exception
	 */
	private void syncTask(Task old) throws Exception {
		WTPart task = IntegrationUtil.getPartFromNumber(old.getID());
		if (task != null) {
			Task updated = syncTask(task, old);
			old.setName(task.getName());
			old.setDescribe(updated.getDescribe());
			old.setFiles(updated.getFiles());
			old.setSoftwares(updated.getSoftwares());
			old.setState("");
		} else {
			throw new Exception("ID为" + old.getID() + "的计算任务不存在");
		}
	}

	private Task syncTask(WTPart task, Task old) throws WTException {
		Task taskBean = new Task(task);
		List<File> files = syncFiles(task);
		if (files != null && !files.isEmpty()) {
			Files file = new Files();
			file.setFiles(files);
			if (old != null && old.getFiles() != null) {
				copyFilePath(old.getFiles(), file);
			}
			taskBean.setFiles(file);
		}
		List<WTPart> softwareParts = IntegrationUtil.getChildren(task);
		if (softwareParts != null && !softwareParts.isEmpty()) {
			List<Software> softwares = new ArrayList<Software>();
			for (WTPart software : softwareParts) {
				Software oldSoftware = null;
				if (old != null && old.getSoftwares() != null) {
					for (Software o : old.getSoftwares()) {
						if (software.getNumber().equals(o.getID())) {
							oldSoftware = o;
						}
					}
				}
				softwares.add(syncSoftware(software, oldSoftware));
			}
			taskBean.setSoftwares(softwares);
		} else {
			taskBean.setSoftwares(null);
		}
		return taskBean;
	}

	/**
	 * 同步专有软件 专有软件没有附属文档
	 * 
	 * @param software
	 * @return
	 * @throws WTException
	 */
	private Software syncSoftware(WTPart software, Software old) throws WTException {
		Software softwareBean = new Software(software);
		List<WTPart> paraParts = IntegrationUtil.getChildren(software);
		if (paraParts != null && !paraParts.isEmpty()) {
			List<Para> paras = new ArrayList<Para>();
			for (WTPart paraPart : paraParts) {
				Para oldPara = null;
				if (old != null && old.getParas() != null) {
					for (Para o : old.getParas()) {
						if (paraPart.getNumber().equals(o.getID())) {
							oldPara = o;
							break;
						}
					}
				}
				paras.add(syncPara(paraPart, oldPara));
			}
			softwareBean.setParas(paras);
		}
		return softwareBean;

	}

	private void syncSoftware(Software old) throws Exception {
		WTPart software = IntegrationUtil.getPartFromNumber(old.getID());
		if (software != null) {
			Software updated = syncSoftware(software, old);
			old.setName(updated.getName());
			old.setParas(updated.getParas());
			old.setState("");
		} else {
			throw new Exception("ID为" + old.getID() + "的专有软件不存在");
		}
	}

	/**
	 * 同步计算参数
	 * 
	 * @param para
	 * @return
	 * @throws WTException
	 */
	private Para syncPara(WTPart para, Para old) throws WTException {
		Para paraBean = new Para(para);
		paraBean.setFiles(syncFiles(para));
		if (old != null && old.getFiles() != null) {
			copyFilePath(old.getFiles(), paraBean.getFiles());
		}
		return paraBean;
	}

	private void syncPara(Para old) throws Exception {
		WTPart para = IntegrationUtil.getPartFromNumber(old.getID());
		if (para != null) {
			Para updated = syncPara(para, old);
			old.setFiles(updated.getFiles());
			old.setName(updated.getName());
			old.setState("");
		} else {
			throw new Exception("ID为" + old.getID() + "的计算参数不存在");
		}
	}

	/**
	 * 同步文档
	 * 
	 * @param file
	 * @return
	 */
	private File syncFile(WTDocument file) {
		File fileBean = new File(file);
		return fileBean;
	}

	private void syncFile(File old) throws Exception {
		WTDocument file = IntegrationUtil.getDocFromNumber(old.getID());
		if (file != null) {
			File updated = syncFile(file);
			old.setName(updated.getName());
			old.setAuthor(updated.getAuthor());
			old.setOrgan(updated.getOrgan());
			old.setType(updated.getType());
			old.setDescribe(updated.getDescribe());
			old.setState("");
		} else {
			throw new Exception("ID为" + old.getID() + "的文件不存在");
		}

	}

	/**
	 * 同步部件下的所有附属文档
	 * 
	 * @param part
	 * @return
	 * @throws WTException
	 */
	private static List<File> syncFiles(WTPart part) throws WTException {
		List<File> result = new ArrayList<File>();
		List<WTDocument> docs = IntegrationUtil.getDescribeDoc(part);
		for (WTDocument doc : docs) {
			File file = new File(doc);
			result.add(file);
		}
		return result;
	}

	/**
	 * 拷贝文档中的path属性
	 */
	private void copyFilePath(Files from, Files to) {
		List<File> fromFiles = from.getFiles();
		List<File> toFiles = to.getFiles();
		if (fromFiles != null && to != null) {
			for (File toFile : toFiles) {
				for (File fromFile : fromFiles) {
					if (fromFile.getID().equals(toFile.getID())) {
						toFile.setPath(fromFile.getPath());
						break;
					}
				}
			}
		}
	}

	/**
	 * 拷贝文档中的path属性
	 */
	private void copyFilePath(List<File> from, List<File> to) {
		if (from != null && to != null) {
			for (File toFile : to) {
				for (File fromFile : from) {
					if (fromFile.getID().equals(toFile.getID())) {
						toFile.setPath(fromFile.getPath());
						break;
					}
				}
			}
		}
	}
}
