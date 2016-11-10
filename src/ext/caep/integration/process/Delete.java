package ext.caep.integration.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ext.caep.integration.bean.File;
import ext.caep.integration.bean.Files;
import ext.caep.integration.bean.Global;
import ext.caep.integration.bean.Para;
import ext.caep.integration.bean.Project;
import ext.caep.integration.bean.Software;
import ext.caep.integration.bean.Task;
import ext.caep.integration.util.IntegrationUtil;
import wt.doc.WTDocument;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTHashSet;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartHelper;
import wt.part.WTPartUsageLink;
import wt.pom.PersistenceException;
import wt.query.ArrayExpression;
import wt.query.ClassAttribute;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;

public class Delete {
	private Global currentGlobal;
	private Project currentProject;
	private Task currentTask;
	private Software currentSoftware;
	private Object currentParent;// XML节点中的父节点
	String parentNumber;// Windchill DB中的父节点

	public void process(Map<String, Object> parameters, Object root) throws Exception {
		this.currentGlobal = (Global) parameters.get("currentGlobal");
		this.currentProject = (Project) parameters.get("currentProject");
		this.currentTask = (Task) parameters.get("currentTask");
		this.currentSoftware = (Software) parameters.get("currentSoftware");
		this.parentNumber = (String) parameters.get("parentNumber");
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

	private void deleteGlobal(Global root) throws Exception {
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

	private void deleteProject(WTPart project) throws Exception {
		boolean isAdmin = IntegrationUtil.isAdmin();
		boolean isMember = IntegrationUtil.isMember();
		boolean hasTask = IntegrationUtil.hasTask(project);
		currentProject = new Project(project);
		if (!hasTask && isAdmin) {
			deleteDocFromPart(project);
			delete(project);
			if (currentGlobal != null) {
				currentGlobal.removeProject(project.getNumber());
			}
		} else if (hasTask && isMember) {
			List<WTPart> tasksForMe = IntegrationUtil.filter(IntegrationUtil.getChildren(project));
			for (WTPart taskPart : tasksForMe) {
				deleteTask(taskPart);
			}
		} else if (isAdmin && hasTask) {
			throw new Exception("方案管理员不能删除有计算任务的方案");
		}
	}

	private void deleteAllProject() throws Exception {
		List<WTPart> allProjects = IntegrationUtil.getAllProject();
		for (WTPart project : allProjects) {
			deleteProject(project);
		}
	}

	private void deleteProject(Project project) throws Exception {
		WTPart projectPart = IntegrationUtil.getPartFromNumber(project.getID());
		if (projectPart != null) {
			deleteProject(projectPart);
			currentProject = project;
		}
	}

	private void deleteTask(WTPart task) throws Exception {
		List<WTPart> softwares = IntegrationUtil.getChildren(task);
		currentTask = new Task(task);
		deleteDocFromPart(task);
		if (currentProject != null) {
			WTPart project = IntegrationUtil.getPartFromNumber(currentProject.getID());
			if (project != null) {
				deletePartFromPart(task, project);
			} else {
				delete(task);
			}

		} else {
			delete(task);
		}
		for (WTPart software : softwares) {
			deleteSoftware(software);
		}
	}

	private void deleteTask(Task task) throws Exception {
		WTPart part = IntegrationUtil.getPartFromNumber(task.getID());
		if (part != null) {
			if (IntegrationUtil.isOwn(part)) {
				deleteTask(part);
				if (currentProject != null) {
					currentProject.removeTask(task.getID());
				}
			} else {
				throw new Exception("不能删除别人的计算任务");
			}
		}
	}

	private void deleteSoftware(WTPart software) throws Exception {
		List<WTPart> paras = IntegrationUtil.getChildren(software);
		currentSoftware = new Software(software);
		if (currentTask != null) {
			currentTask.removeSoftware(software.getNumber());
			WTPart task = IntegrationUtil.getPartFromNumber(currentTask.getID());
			if (task != null) {
				deletePartFromPart(software, task);
			} else {
				delete(software);
			}
		} else {
			delete(software);
		}
		if (paras != null && !paras.isEmpty()) {
			for (WTPart para : paras) {
				deletePara(para);
			}
		}

	}

	private void deleteSoftware(Software software) throws Exception {
		WTPart softwarePart = IntegrationUtil.getPartFromNumber(software.getID());
		if (softwarePart != null) {
			if (IntegrationUtil.isOwn(softwarePart)) {
				deleteSoftware(softwarePart);
			} else {
				throw new Exception("不能删除别人的专有软件");
			}
		}
	}

	private void deletePara(WTPart para) throws Exception {
		deleteDocFromPart(para);
		if (currentSoftware != null) {
			currentSoftware.removePara(para.getNumber());
			WTPart software = IntegrationUtil.getPartFromNumber(currentSoftware.getID());
			if (software != null) {
				deletePartFromPart(para, software);
			} else {
				delete(para);
			}
		} else {
			delete(para);
		}
	}

	private void deletePara(Para para) throws Exception {
		WTPart paraPart = IntegrationUtil.getPartFromNumber(para.getID());
		if (paraPart != null) {
			if (IntegrationUtil.isOwn(paraPart)) {
				deletePara(paraPart);
			} else {
				throw new Exception("不能删除别人的计算参数");
			}
		}
	}

	private void deleteFile(File file) throws Exception {
		WTDocument doc = IntegrationUtil.getDocFromNumber(file.getID());
		WTPart parentPart = IntegrationUtil.getPartFromNumber(parentNumber);
		if (doc != null && IntegrationUtil.isOwn(doc)) {
			if (parentPart != null) {
				deleteDocFromPart(parentPart, doc);
			} else {
				delete(doc);
			}
			if (currentParent != null) {
				if (currentParent instanceof Files) {
					Files files = (Files) currentParent;
					files.removeFile(file.getID());
				} else if (currentParent instanceof Para) {
					Para para = (Para) currentParent;
					para.removeFile(file.getID());
				}
			}
		} else if (doc != null) {
			throw new Exception("只能删除自己创建的文档");
		}
	}

	private void delete(Versioned versioned) throws Exception {
		if (versioned != null) {
			PersistenceHelper.manager.delete(versioned);
		} else {
			throw new Exception("对象已经被删除");
		}
	}

	private void deletePartFromPart(WTPart child, WTPart parent) throws PersistenceException, WTException {
		if (parent != null && child != null) {
			QueryResult all = VersionControlHelper.service.allIterationsOf(parent.getMaster());
			WTCollection allParents = new WTHashSet(all);
			Iterator<Persistable> iter = allParents.persistableIterator();
			Collection<Long> ids = new ArrayList<Long>();
			while (iter.hasNext()) {
				Persistable p = iter.next();
				ids.add(p.getPersistInfo().getObjectIdentifier().getId());
			}
			QuerySpec qs = new QuerySpec(WTPartUsageLink.class);
			ClassAttribute roleA_ObjectId_Attr = new ClassAttribute(WTPartUsageLink.class, WTAttributeNameIfc.ROLEA_OBJECT_ID);
			Long[] id_array = new Long[0];
			id_array = ids.toArray(id_array);
			qs.appendWhere(new SearchCondition(roleA_ObjectId_Attr, SearchCondition.IN, new ArrayExpression(id_array)), new int[] { 0 });
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(WTPartUsageLink.class, WTAttributeNameIfc.ROLEB_OBJECT_ID, SearchCondition.EQUAL, child.getMaster().getPersistInfo().getObjectIdentifier().getId()),
					new int[] { 0 });
			QueryResult qr = PersistenceHelper.manager.find(qs);
			while (qr.hasMoreElements()) {
				WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
				PersistenceServerHelper.manager.remove(link);
			}
			if (!IntegrationUtil.hasParent(child)) {
				PersistenceHelper.manager.delete(child);
			}
		}
	}

	private void deleteDocFromPart(WTPart part, WTDocument deleteDoc) throws WTException {
		List<WTDocument> docs = new ArrayList<WTDocument>();
		QueryResult all = VersionControlHelper.service.allIterationsOf(part.getMaster());
		while (all.hasMoreElements()) {
			WTPart version = (WTPart) all.nextElement();
			QueryResult qr = WTPartHelper.service.getDescribedByWTDocuments(version, false);
			if (qr.size() > 0) {
				while (qr.hasMoreElements()) {
					WTPartDescribeLink link = (WTPartDescribeLink) qr.nextElement();
					WTDocument doc = link.getDescribedBy();
					if (!docs.contains(doc) && doc.getNumber().equalsIgnoreCase(deleteDoc.getNumber())) {
						docs.add(doc);
						PersistenceServerHelper.manager.remove(link);
					}
				}
			}
		}
		if (!docs.isEmpty()) {
			for (WTDocument doc : docs) {
				if (!IntegrationUtil.hasDescribePart(doc) && IntegrationUtil.isOwn(doc)) {
					PersistenceHelper.manager.delete(doc);
				}
			}
		}
	}

	private void deleteDocFromPart(WTPart part) throws WTException {
		List<WTDocument> docs = new ArrayList<WTDocument>();
		QueryResult all = VersionControlHelper.service.allIterationsOf(part.getMaster());
		while (all.hasMoreElements()) {
			WTPart version = (WTPart) all.nextElement();
			QueryResult qr = WTPartHelper.service.getDescribedByWTDocuments(version, false);
			if (qr.size() > 0) {
				while (qr.hasMoreElements()) {
					WTPartDescribeLink link = (WTPartDescribeLink) qr.nextElement();
					WTDocument doc = link.getDescribedBy();
					if (!docs.contains(doc)) {
						docs.add(doc);
					}
					PersistenceServerHelper.manager.remove(link);
				}
			}
		}
		if (!docs.isEmpty()) {
			for (WTDocument doc : docs) {
				if (!IntegrationUtil.hasDescribePart(doc) && IntegrationUtil.isOwn(doc)) {
					PersistenceHelper.manager.delete(doc);
				}
			}
		}
	}

}
