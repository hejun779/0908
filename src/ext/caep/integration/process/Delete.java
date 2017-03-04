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
import wt.fc.collections.WTSet;
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
import wt.vc.struct.StructHelper;

public class Delete {
	private Global currentGlobal;
	private Project currentProject;
	private Task currentTask;
	private Software currentSoftware;
	private Object currentParent;// XML节点中的父节点
	String parentNumber;// Windchill DB中的父节点

	public Delete(Map<String, Object> parameters) {
		this.currentGlobal = (Global) parameters.get("currentGlobal");
		this.currentProject = (Project) parameters.get("currentProject");
		this.currentTask = (Task) parameters.get("currentTask");
		this.currentSoftware = (Software) parameters.get("currentSoftware");
		this.parentNumber = (String) parameters.get("parentNumber");
	}

	public void process(Object root) throws Exception {
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
			deleteDocFromPart(null, project);
			delete(project);
			if (currentGlobal != null) {
				currentGlobal.removeProject(project.getNumber());
			}
		} else if (hasTask && isMember) {
			List<WTPart> tasksForMe = IntegrationUtil.filter(IntegrationUtil.getChildren(project));
			if (tasksForMe != null && !tasksForMe.isEmpty()) {
				for (WTPart taskPart : tasksForMe) {
					deleteTask(taskPart);
				}
			} else {
				throw new Exception("ID为" + project.getNumber() + "的方案下没有你创建的计算任务可供删除");
			}
		} else if (isAdmin && hasTask) {
			throw new Exception("方案管理员不能删除有计算任务的方案(ID:" + project.getNumber());
		} else if (!hasTask && isMember) {
			throw new Exception("ID为" + project.getNumber() + "的方案下没有你创建的计算任务可供删除");
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
		} else {
			throw new Exception("ID为" + project.getID() + "的方案不存在");
		}
	}

	private void deleteTask(WTPart task) throws Exception {
		List<WTPart> softwares = IntegrationUtil.getChildren(task);
		currentTask = new Task(task);
		deleteDocFromPart(null, task);
		if (currentProject != null) {
			WTPart project = IntegrationUtil.getPartFromNumber(currentProject.getID());
			deletePartFromPart(task, project);
		} else {
			deletePartFromPart(task, null);
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
		} else {
			throw new Exception("ID为" + task.getID() + "的计算任务不存在");
		}
	}

	private void deleteSoftware(WTPart software) throws Exception {
		List<WTPart> paras = IntegrationUtil.getChildren(software);
		currentSoftware = new Software(software);
		if (currentTask != null) {
			currentTask.removeSoftware(software.getNumber());
			WTPart task = IntegrationUtil.getPartFromNumber(currentTask.getID());
			deletePartFromPart(software, task);
		} else {
			deletePartFromPart(software, null);
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
		} else {
			throw new Exception("ID为" + software.getID() + "的专有软件不存在");
		}
	}

	private void deletePara(WTPart para) throws Exception {
		deleteDocFromPart(null, para);
		if (currentSoftware != null) {
			currentSoftware.removePara(para.getNumber());
			WTPart software = IntegrationUtil.getPartFromNumber(currentSoftware.getID());
			deletePartFromPart(para, software);
		} else {
			deletePartFromPart(para, null);
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
		} else {
			throw new Exception("ID为" + para.getID() + "的计算参数不存在");
		}
	}

	private void deleteFile(File file) throws Exception {
		WTDocument doc = IntegrationUtil.getDocFromNumber(file.getID());
		WTPart parentPart = null;
		if (parentNumber != null) {
			parentPart = IntegrationUtil.getPartFromNumber(parentNumber);
		}
		if (doc != null && IntegrationUtil.isOwn(doc)) {
			deleteDocFromPart(doc, parentPart);
			// deleteDocOfPart(doc, parentPart);
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
		} else {
			throw new Exception("ID为" + file.getID() + "的文档不存在");
		}
	}

	private void delete(WTPart part) throws Exception {
		if (part != null) {
			// PersistenceHelper.manager.delete(part);
			deletePartFromPart(part, null);
		} else {
			throw new Exception("对象已经被删除");
		}
	}

	private void deletePartFromPart(WTPart child, WTPart parent) throws PersistenceException, WTException {
		if (child != null) {
			QuerySpec qs = new QuerySpec(WTPartUsageLink.class);
			if (parent != null) {
				QueryResult all = VersionControlHelper.service.allIterationsOf(parent.getMaster());
				WTCollection allParents = new WTHashSet(all);
				Iterator<Persistable> iter = allParents.persistableIterator();
				Collection<Long> ids = new ArrayList<Long>();
				while (iter.hasNext()) {
					Persistable p = iter.next();
					ids.add(p.getPersistInfo().getObjectIdentifier().getId());
				}
				ClassAttribute roleA_ObjectId_Attr = new ClassAttribute(WTPartUsageLink.class, WTAttributeNameIfc.ROLEA_OBJECT_ID);
				Long[] id_array = new Long[0];
				id_array = ids.toArray(id_array);
				qs.appendWhere(new SearchCondition(roleA_ObjectId_Attr, SearchCondition.IN, new ArrayExpression(id_array)), new int[] { 0 });
				qs.appendAnd();
			}
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

	private void deleteDocFromPart(WTDocument deleteDoc, WTPart part) throws WTException {
		WTSet allDescribeParts = new WTHashSet();
		WTSet allDescribeDocs = new WTHashSet();
		if (deleteDoc == null) {
			allDescribeParts.add(part);
		} else {
			QueryResult all = VersionControlHelper.service.allIterationsOf(deleteDoc.getMaster());
			while (all.hasMoreElements()) {
				WTDocument doc = (WTDocument) all.nextElement();
				QueryResult qr = WTPartHelper.service.getDescribesWTParts(doc, false);
				if (qr.size() > 0) {
					while (qr.hasMoreElements()) {
						WTPartDescribeLink link = (WTPartDescribeLink) qr.nextElement();
						WTPart describePart = link.getDescribes();
						if (part != null) {
							if (describePart.getNumber().equalsIgnoreCase(part.getNumber())) {
								PersistenceServerHelper.manager.remove(link);
								allDescribeParts.add(part);
							}
						} else {
							allDescribeParts.add(describePart);
							PersistenceServerHelper.manager.remove(link);
						}
					}
				}
			}
			allDescribeDocs.add(deleteDoc);
		}
		Iterator itParts = allDescribeParts.persistableIterator();
		while (itParts.hasNext()) {
			WTPart describePart = (WTPart) itParts.next();
			QueryResult allversion = VersionControlHelper.service.allIterationsOf(describePart.getMaster());
			while (allversion.hasMoreElements()) {
				WTPart version = (WTPart) allversion.nextElement();
				@SuppressWarnings("deprecation")
				QueryResult qr = StructHelper.service.navigateDescribedBy(version, WTPartDescribeLink.class, false);
				while (qr.hasMoreElements()) {
					WTPartDescribeLink link = (WTPartDescribeLink) qr.nextElement();
					WTDocument describeDoc = link.getDescribedBy();
					if (deleteDoc != null) {
						if (deleteDoc.getNumber().equalsIgnoreCase(describeDoc.getNumber())) {
							PersistenceServerHelper.manager.remove(link);
						}
					} else {
						PersistenceServerHelper.manager.remove(link);
						allDescribeDocs.add(describeDoc);

					}
				}
			}
		}
		Iterator itDocs = allDescribeDocs.persistableIterator();
		while (itDocs.hasNext()) {
			WTDocument describeDoc = (WTDocument) itDocs.next();
			describeDoc.getNumber();
			if (!IntegrationUtil.hasDescribePart(describeDoc) && IntegrationUtil.isOwn(describeDoc)) {
				PersistenceHelper.manager.delete(describeDoc);
			}
		}
	}
}
