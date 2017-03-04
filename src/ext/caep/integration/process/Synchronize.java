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
			projectBeans.add(syncProject(project));
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

	private Project syncProject(WTPart project) throws WTException {
		Project projectBean = new Project(project);
		List<WTPart> tasks = IntegrationUtil.getChildren(project);
		List<Task> taskBeans = new ArrayList<Task>();
		if (!IntegrationUtil.isAdmin()) {
			tasks = IntegrationUtil.filter(tasks);
		}
		for (WTPart task : tasks) {
			taskBeans.add(syncTask(task));
		}
		if (!taskBeans.isEmpty()) {
			projectBean.setTasks(taskBeans);
		}
		List<File> files = syncFiles(project);
		if (files != null && !files.isEmpty()) {
			Files file = new Files();
			file.setFiles(files);
			projectBean.setFiles(file);
		}
		return projectBean;
	}

	private void syncProject(Project old) throws Exception {
		WTPart project = IntegrationUtil.getPartFromNumber(old.getID());
		if (project != null) {
			Project newProject = syncProject(project);
			old.setFiles(newProject.getFiles());
			old.setTasks(newProject.getTasks());
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
			Task updated = syncTask(task);
			old.setName(task.getName());
			old.setDescribe(updated.getDescribe());
			old.setFiles(updated.getFiles());
			old.setSoftwares(updated.getSoftwares());
			old.setState(updated.getState());
		} else {
			throw new Exception("ID为" + old.getID() + "的计算任务不存在");
		}
	}

	private Task syncTask(WTPart task) throws WTException {
		Task taskBean = new Task(task);
		List<File> files = syncFiles(task);
		if (files != null && !files.isEmpty()) {
			Files file = new Files();
			file.setFiles(files);
			taskBean.setFiles(file);
		}
		List<WTPart> softwareParts = IntegrationUtil.getChildren(task);
		if (softwareParts != null && !softwareParts.isEmpty()) {
			List<Software> softwares = new ArrayList<Software>();
			for (WTPart software : softwareParts) {
				softwares.add(syncSoftware(software));
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
	private Software syncSoftware(WTPart software) throws WTException {
		Software softwareBean = new Software(software);
		List<WTPart> paraParts = IntegrationUtil.getChildren(software);
		if (paraParts != null && !paraParts.isEmpty()) {
			List<Para> paras = new ArrayList<Para>();
			for (WTPart paraPart : paraParts) {
				paras.add(syncPara(paraPart));
			}
			softwareBean.setParas(paras);
		}
		return softwareBean;

	}

	private void syncSoftware(Software old) throws Exception {
		WTPart software = IntegrationUtil.getPartFromNumber(old.getID());
		if (software != null) {
			Software updated = syncSoftware(software);
			old.setName(updated.getName());
			old.setParas(updated.getParas());
			old.setState(updated.getState());
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
	private Para syncPara(WTPart para) throws WTException {
		Para paraBean = new Para(para);
		paraBean.setFiles(syncFiles(para));
		return paraBean;
	}

	private void syncPara(Para old) throws Exception {
		WTPart para = IntegrationUtil.getPartFromNumber(old.getID());
		if (para != null) {
			Para updated = syncPara(para);
			old.setFiles(updated.getFiles());
			old.setName(updated.getName());
			old.setState(updated.getState());
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
}
