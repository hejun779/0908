package ext.caep.integration.process;

import java.util.ArrayList;
import java.util.Iterator;
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
import wt.fc.collections.WTSet;
import wt.part.WTPart;
import wt.util.WTException;

public class Synchronize extends DataOperation {
	public static Object process(Object root) {
		if (root instanceof Global) {
			return syncGlobal((Global) root);
		} else if (root instanceof Project) {
			return syncProject((Project) root);
		} else if (root instanceof Task) {
			return syncTask((Task) root);
		} else if (root instanceof Software) {
			return syncSoftware((Software) root);
		} else if (root instanceof Para) {
			return syncPara((Para) root);
		} else if (root instanceof File) {
			return syncFile((File) root);
		}
		return null;
	}

	/**
	 * 同步全部数据
	 * 
	 * @param old
	 * @return
	 */
	private static Global syncGlobal(Global old) {
		Global globalBean = new Global();
		globalBean.setName(old.getName());
		globalBean.setState("");
		List<WTPart> projects = IntegrationUtil.getAllProject();
		List<Project> projectBeans = new ArrayList<Project>();
		for (WTPart project : projects) {
			projectBeans.add(syncProject(project));
		}
		globalBean.setProjects(projectBeans);
		return globalBean;
	}

	/**
	 * 同步方案
	 * 
	 * @param root
	 */

	private static Project syncProject(WTPart project) {
		Project projectBean = new Project(project);
		List<WTPart> tasks = IntegrationUtil.getChildren(project);
		List<Task> taskBeans = new ArrayList<Task>();
		if (!IntegrationUtil.isAdmin()) {
			tasks = IntegrationUtil.filter(tasks);
		}
		for (WTPart task : tasks) {
			taskBeans.add(syncTask(task));
		}
		projectBean.setTasks(taskBeans);
		List<File> files = syncFiles(project);
		if (files != null && !files.isEmpty()) {
			Files file = new Files();
			file.setFiles(files);
			projectBean.setFiles(file);
		}
		return projectBean;
	}

	private static Project syncProject(Project old) {
		WTPart project = IntegrationUtil.getPartFromNumber(old.getID());
		return syncProject(project);
	}

	/**
	 * 同步计算任务
	 * 
	 * @param oldOne
	 * @return
	 */
	private static Task syncTask(Task old) {
		WTPart task = IntegrationUtil.getPartFromNumber(old.getID());
		return syncTask(task);

	}

	private static Task syncTask(WTPart task) {
		Task taskBean = new Task(task);
		List<File> files = syncFiles(task);
		if (files != null && !files.isEmpty()) {
			Files file = new Files();
			file.setFiles(files);
			taskBean.setFiles(file);
		}
		return taskBean;
	}

	/**
	 * 同步专有软件 专有软件没有附属文档
	 * 
	 * @param software
	 * @return
	 */
	private static Software syncSoftware(WTPart software) {
		Software softwareBean = new Software(software);
		return softwareBean;

	}

	private static Software syncSoftware(Software old) {
		WTPart software = IntegrationUtil.getPartFromNumber(old.getID());
		return syncSoftware(software);
	}

	/**
	 * 同步计算参数
	 * 
	 * @param para
	 * @return
	 */
	private static Para syncPara(WTPart para) {
		Para paraBean = new Para(para);
		paraBean.setFiles(syncFiles(para));
		return paraBean;
	}

	private static Para syncPara(Para old) {
		WTPart para = IntegrationUtil.getPartFromNumber(old.getID());
		return syncPara(para);
	}

	/**
	 * 同步文档
	 * 
	 * @param file
	 * @return
	 */
	private static File syncFile(WTDocument file) {
		File fileBean = new File(file);
		return fileBean;
	}

	private static File syncFile(File old) {
		WTDocument file = IntegrationUtil.getDocFromNumber(old.getID());
		return syncFile(file);
	}

	/**
	 * 同步部件下的所有附属文档
	 * 
	 * @param part
	 * @return
	 */
	private static List<File> syncFiles(WTPart part) {
		List<File> result = new ArrayList<File>();
		WTSet files = IntegrationUtil.getDescribeDoc(part);
		Iterator filesIt;
		try {
			filesIt = files.persistableIterator();
			while (filesIt.hasNext()) {
				WTDocument doc = (WTDocument) filesIt.next();
				File file = new File(doc);
				result.add(file);
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return result;
	}

}
