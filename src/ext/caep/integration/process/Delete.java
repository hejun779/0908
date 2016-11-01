package ext.caep.integration.process;

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
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.collections.WTHashSet;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartHelper;
import wt.pom.PersistenceException;
import wt.util.WTException;
import wt.vc.Mastered;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;

public class Delete {
	private Global currentGlobal;
	private Project currentProject;
	private Task currentTask;
	private Object currentParent;

	public void process(Object root) {
		if (root instanceof Global) {
			deleteGlobal((Global) root);
		} else if (root instanceof Project) {
			deleteProject((Project) root);
		} else if (root instanceof Task) {
			deleteTask((Task) root);
		} else if (root instanceof Software) {
			deleteSoftware((Software) root);
		} else if (root instanceof Para) {
			deletePara((Para) root);
		} else if (root instanceof File) {
			deleteFile((File) root);
		}
	}

	private void deleteGlobal(Global root) {
		if (root.getProjects() != null && !root.getProjects().isEmpty()) {
			List<Project> projects = root.getProjects();
			for (Project project : projects) {
				deleteProject(project);
			}
		} else {
			deleteAllProject();
		}
		root.setProjects(null);
	}

	private void deleteProject(WTPart project) {
		boolean isAdmin = IntegrationUtil.isAdmin();
		boolean isMember = IntegrationUtil.isMember();
		boolean hasTask = IntegrationUtil.hasTask(project);
		if (!hasTask && isAdmin) {
			delete(project);
			deleteDocFromPart(project);
		} else if (hasTask && isMember) {
			List<WTPart> tasksForMe = IntegrationUtil.filter(IntegrationUtil.getChildren(project));
			for (WTPart taskPart : tasksForMe) {
				deleteTask(taskPart);
			}
		}
	}

	private void deleteAllProject() {
		List<WTPart> allProjects = IntegrationUtil.getAllProject();
		for (WTPart project : allProjects) {
			deleteProject(project);
		}
	}

	private void deleteProject(Project project) {
		WTPart projectPart = IntegrationUtil.getPartFromNumber(project.getID());
		if (projectPart != null) {
			deleteProject(projectPart);
			if (currentGlobal != null && currentGlobal.getProjects() != null) {
				currentGlobal.getProjects().remove(project);
			}
		}
	}

	private void deleteTask(WTPart task) {
		if (IntegrationUtil.isOwn(task)) {
			List<WTPart> softwares = IntegrationUtil.getChildren(task);
			task = deleteDocFromPart(task);
			delete(task);
			for (WTPart software : softwares) {
				deleteSoftware(software);
			}
		}
	}

	private void deleteTask(Task task) {
		WTPart part = IntegrationUtil.getPartFromNumber(task.getID());
		if (part != null) {
			deleteTask(part);
			if (currentProject != null && currentProject.getTasks() != null) {
				currentProject.getTasks().remove(task);
			}
		}
	}

	private void deleteSoftware(WTPart software) {
		List<WTPart> paras = IntegrationUtil.getChildren(software);
		delete(software);
		if (paras != null && !paras.isEmpty()) {
			for (WTPart para : paras) {
				deletePara(para);
			}
		}
	}

	private void deleteSoftware(Software software) {
		WTPart part = IntegrationUtil.getPartFromNumber(software.getID());
		if (part != null) {
			deleteSoftware(part);
			if (currentTask != null && currentTask.getSoftwares() != null) {
				currentTask.getSoftwares().remove(software);
			}
		}
	}

	private void deletePara(WTPart para) {
		deleteDocFromPart(para);
		delete(para);
	}

	private void deletePara(Para root) {
		WTPart part = IntegrationUtil.getPartFromNumber(root.getID());
		if (part != null) {
			deletePara(part);
		}
	}

	private void deleteFile(WTDocument file) {
		if (!IntegrationUtil.hasDescribePart(file)) {
			delete(file);
		}
	}

	private void deleteFile(File file) {
		WTDocument doc = IntegrationUtil.getDocFromNumber(file.getID());
		if (doc != null) {
			deleteFile(doc);
			if (currentParent != null) {
				if (currentParent instanceof Files) {
					Files files = (Files) currentParent;
					if (files.getFiles() != null) {
						files.getFiles().remove(file);
					}
				} else if (currentParent instanceof Para) {
					Para para = (Para) currentParent;
					if (para.getFiles() != null) {
						para.getFiles().remove(file);
					}
				}
			}
		}
	}

	private void delete(Versioned versioned) {
		WTHashSet objsToDelete = new WTHashSet();
		Mastered master = versioned.getMaster();
		try {
			QueryResult qr = VersionControlHelper.service.allVersionsOf(master);
			if (qr != null)
				while (qr.hasMoreElements()) {
					Object objQr = qr.nextElement();
					if (objQr instanceof Workable) {
						Workable workable = (Workable) objQr;
						if (WorkInProgressHelper.isWorkingCopy(workable)) {
							continue;
						}
					}
					objsToDelete.add(objQr);
				}
			if (!objsToDelete.isEmpty()) {
				PersistenceHelper.manager.delete(objsToDelete);
			}
		} catch (PersistenceException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
	}

	private WTPart deleteDocFromPart(WTPart part) {
		WTPart result = part;
		try {
			QueryResult qr = WTPartHelper.service.getDescribedByWTDocuments(part, false);
			if (qr.size() > 0) {
				part = (WTPart) IntegrationUtil.checkout(part);
				while (qr.hasMoreElements()) {
					WTPartDescribeLink link = (WTPartDescribeLink) qr.nextElement();
					WTDocument doc = link.getDescribedBy();
					PersistenceHelper.manager.delete(link);
					if (!IntegrationUtil.hasDescribePart(doc)) {
						delete(doc);
					}
				}
				result = (WTPart) IntegrationUtil.checkin(part);
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return result;
	}

}
