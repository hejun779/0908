package ext.caep.integration.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import ext.caep.integration.bean.Files;
import ext.caep.integration.bean.Global;
import ext.caep.integration.bean.Project;
import wt.associativity.BomHelper;
import wt.doc.WTDocument;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTHashSet;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.pom.PersistenceException;
import wt.query.ArrayExpression;
import wt.query.ClassAttribute;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;
import wt.vc.Mastered;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;

public class DeleteTest22 implements RemoteAccess {
	@SuppressWarnings("rawtypes")
	private static Class TaskResultClass;

	public static void main(String[] args) {
		// ReferenceFactory factory = new ReferenceFactory();
		// try {
		// WTPart p = (WTPart)
		// factory.getReference("OR:wt.part.WTPart:93991").getObject();
		// PersistenceHelper.manager.delete(p);
		// } catch (WTRuntimeException e) {
		// e.printStacjkTrace();
		// } catch (WTException e) {
		// e.printStackTrace();
		// }

		// deleteDocLink();
		RemoteMethodServer.getDefault().setUserName("demo");
		RemoteMethodServer.getDefault().setPassword("demo");
		deletePartTest();
	}

	public static void deletePartTest() {
		if (RemoteMethodServer.ServerFlag) {
			ReferenceFactory factory = new ReferenceFactory();
			try {
				WTPart parent = (WTPart) factory.getReference("VR:wt.part.WTPart:85386").getObject();
				WTPart child = (WTPart) factory.getReference("VR:wt.part.WTPart:85417").getObject();
				deletePartFromPart(child, parent);
			} catch (WTRuntimeException e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			}
		} else {
			try {
				RemoteMethodServer.getDefault().invoke("deletePartTest", DeleteTest22.class.getName(), null, null, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	private static void deletePartFromPart(WTPart child, WTPart parent) throws PersistenceException, WTException {
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

	public static void deleteDocTest() {
		if (RemoteMethodServer.ServerFlag) {
			ReferenceFactory factory = new ReferenceFactory();
			try {
				WTPart parent = (WTPart) factory.getReference("VR:wt.part.WTPart:85386").getObject();
				deleteDocFromPart(parent);
			} catch (WTRuntimeException e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			}
		} else {
			try {
				RemoteMethodServer.getDefault().invoke("deleteDocTest", DeleteTest22.class.getName(), null, null, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	private static void deleteDocFromPart(WTPart part, WTDocument deleteDoc) throws WTException {
		QueryResult all = VersionControlHelper.service.allIterationsOf(deleteDoc.getMaster());
		while (all.hasMoreElements()) {
			WTDocument doc = (WTDocument) all.nextElement();
			QueryResult qr = WTPartHelper.service.getDescribesWTParts(doc, false);
			if (qr.size() > 0) {
				while (qr.hasMoreElements()) {
					WTPartDescribeLink link = (WTPartDescribeLink) qr.nextElement();
					WTPart describePart = link.getDescribes();
					if (describePart.getNumber().equalsIgnoreCase(describePart.getNumber())) {
						PersistenceServerHelper.manager.remove(link);
					}
				}
			}
		}
		if (!IntegrationUtil.hasDescribePart(deleteDoc)) {
			PersistenceHelper.manager.delete(deleteDoc);
		}
	}

	private static void deleteDocFromPart(WTPart part) throws WTException {
		if (RemoteMethodServer.ServerFlag) {
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
							deleteDocFromPart(part, doc);
						}
					}
				}
			}
		} else {
			try {
				RemoteMethodServer.getDefault().invoke("deleteDocFromPart", DeleteTest22.class.getName(), null, null, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	public static void searchPartUsageLink() {
		if (RemoteMethodServer.ServerFlag) {
			try {
				ReferenceFactory factory = new ReferenceFactory();
				WTPart parent = (WTPart) factory.getReference("VR:wt.part.WTPart:274981").getObject();
				WTPart child = (WTPart) factory.getReference("VR:wt.part.WTPart:274997").getObject();
				WTPartMaster master = (WTPartMaster) child.getMaster();
				Collection<Long> ids = new ArrayList<Long>();
				QueryResult parentQr = VersionControlHelper.service.allIterationsOf(parent.getMaster());
				WTCollection allParents = new WTHashSet(parentQr);
				Iterator<Persistable> iter = allParents.persistableIterator();
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
				qs.appendWhere(new SearchCondition(WTPartUsageLink.class, WTAttributeNameIfc.ROLEB_OBJECT_ID, SearchCondition.EQUAL, master.getPersistInfo().getObjectIdentifier().getId()),
						new int[] { 0 });

				QueryResult qr = PersistenceHelper.manager.find(qs);
				while (qr.hasMoreElements()) {
					WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
					PersistenceServerHelper.manager.remove(link);
				}
			} catch (QueryException e) {
				e.printStackTrace();
			} catch (WTRuntimeException e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			}
		} else {
			try {
				RemoteMethodServer.getDefault().invoke("searchPartUsageLink", DeleteTest22.class.getName(), null, null, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	public static void deletePart() {
		if (RemoteMethodServer.ServerFlag) {
			ReferenceFactory factory = new ReferenceFactory();
			try {
				WTPart parent = (WTPart) factory.getReference("VR:wt.part.WTPart:274989").getObject();
				WTPart child = (WTPart) factory.getReference("VR:wt.part.WTPart:274997").getObject();
				HashMap<String, WTPartUsageLink> map = BomHelper.service.getAssemblyUsages(parent);
				Iterator<Entry<String, WTPartUsageLink>> entries = map.entrySet().iterator();
				while (entries.hasNext()) {
					Entry<String, WTPartUsageLink> entry = entries.next();
					System.out.println(entry.getKey());
					WTPart usedBy = entry.getValue().getUsedBy();
					System.out.println(usedBy.getNumber());
					if (entry.getValue().getUses().getNumber().equals(child.getNumber())) {
						PersistenceServerHelper.manager.remove(entry.getValue());
					}
				}
				QueryResult qr = WTPartHelper.service.getUsedByWTParts((WTPartMaster) child.getMaster());
				if (qr.size() == 0) {
					PersistenceHelper.manager.delete(child);
				}
			} catch (WTRuntimeException e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			}
		} else {
			try {
				RemoteMethodServer.getDefault().invoke("deletePart", DeleteTest22.class.getName(), null, null, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	public static void deleteDoc() {
		if (RemoteMethodServer.ServerFlag) {
			ReferenceFactory factory = new ReferenceFactory();
			try {
				List<WTDocument> docs = new ArrayList<WTDocument>();
				WTPart part = (WTPart) factory.getReference("VR:wt.part.WTPart:63791").getObject();
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
						if (!IntegrationUtil.hasDescribePart(doc)) {
							PersistenceHelper.manager.delete(doc);
						}
					}
				}

			} catch (WTRuntimeException e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			}
		} else {
			try {
				RemoteMethodServer.getDefault().invoke("deleteDoc", DeleteTest22.class.getName(), null, null, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	private static void delete(Versioned versioned) {
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
				PersistenceServerHelper.manager.remove(objsToDelete);
			}
		} catch (PersistenceException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
	}

	public static void testNull() {
		Global g = new Global();
		g.setName("test");

		List<Project> projects = new ArrayList<Project>();
		Project p = new Project();
		p.setName("p01");
		projects.add(p);
		Files files = new Files();

		ext.caep.integration.bean.File f = new ext.caep.integration.bean.File();
		f.setName("filename.txt");

		List<ext.caep.integration.bean.File> fileList = new ArrayList<ext.caep.integration.bean.File>();
		fileList.add(f);

		files.setFiles(fileList);

		p.setFiles(files);

		g.setProjects(null);
		File file = new File("d:\\test.xml");
		JaxbUtil.object2xml(g, file);
	}

	public static void deleteDocLink() {
		ReferenceFactory factory = new ReferenceFactory();
		try {
			WTPart p = (WTPart) factory.getReference("VR:wt.part.WTPart:93592").getObject();
			QueryResult qr = WTPartHelper.service.getDescribedByDocuments(p, false);
			System.out.println(qr.size());
			while (qr.hasMoreElements()) {
				WTPartDescribeLink link = (WTPartDescribeLink) qr.nextElement();
				WTPart linkPart = link.getDescribes();
				if (p.equals(linkPart)) {
					CheckoutLink clink = WorkInProgressHelper.service.checkout(p, WorkInProgressHelper.service.getCheckoutFolder(), "");
					// clink.get
				}
				// PersistenceHelper.manager.delete(link);
			}
		} catch (WTRuntimeException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		}

	}

	public static void delete() {
		// try {
		// ArrayList delete_list = new ArrayList();
		// Object deleteTask = ConsoleClassProxy.createDeleteTask();
		// Object myTaskData = ConsoleClassProxy.createTaskData();
		// ConsoleClassProxy.setTaskObjects(myTaskData, delete_list);
		// ConsoleClassProxy.setTaskData(deleteTask, myTaskData);
		// SessionHelper.getLocale(); // Sets up WTContext locale for task
		// Object objTaskResult = ConsoleClassProxy.runTask(deleteTask);
		// Method succMethod = TaskResultClass.getMethod("isSuccess", new
		// Class[] {});
		// Object retObj = succMethod.invoke(objTaskResult, new Object[] {});
		// if (retObj instanceof Boolean && !((Boolean) retObj).booleanValue())
		// { // isSuccess?
		// NmConsoleOpenException coe = new NmConsoleOpenException("");
		// throw coe;
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		WTHashSet objsToDelete = new WTHashSet();
		ReferenceFactory factory = new ReferenceFactory();
		try {
			WTPart p = (WTPart) factory.getReference("VR:wt.part.WTPart:93519").getObject();
			WTPart c = (WTPart) factory.getReference("VR:wt.part.WTPart:93529").getObject();
			WTDocument d = (WTDocument) factory.getReference("VR:wt.doc.WTDocument:93319").getObject();
			// if (obj instanceof Workable) {
			// Workable workable = (Workable) obj;
			// if (WorkInProgressHelper.isWorkingCopy(workable)) {
			// obj = WorkInProgressHelper.service.originalCopyOf(workable);
			// }
			// }
			WTCollection col = new WTHashSet();
			col.add(d.getMaster());
			col.add(c.getMaster());
			col.add(p.getMaster());
			QueryResult qr = VersionControlHelper.service.allVersionsOf(col, Versioned.class);
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
			PersistenceHelper.manager.delete(objsToDelete);
		} catch (WTRuntimeException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		// SandboxHelper.service.removeObjects(objsToDelete, false);
	}
}
