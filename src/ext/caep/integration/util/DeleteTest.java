package ext.caep.integration.util;

import wt.doc.WTDocument;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTHashSet;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;

public class DeleteTest implements RemoteAccess {
	@SuppressWarnings("rawtypes")
	private static Class TaskResultClass;

	public static void main(String[] args) {
		ReferenceFactory factory = new ReferenceFactory();
		try {
			WTPart p = (WTPart) factory.getReference("OR:wt.part.WTPart:63681").getObject();
			PersistenceHelper.manager.delete(p);
		} catch (WTRuntimeException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}

		// deleteDocLink();
	}

	static {
		try {
			TaskResultClass = Class.forName("com.ptc.core.task.TaskResult");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void deleteDocLink() {
		ReferenceFactory factory = new ReferenceFactory();
		try {
			WTPart p = (WTPart) factory.getReference("VR:wt.part.WTPart:63592").getObject();
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
			WTPart p = (WTPart) factory.getReference("VR:wt.part.WTPart:63518").getObject();
			WTPart c = (WTPart) factory.getReference("VR:wt.part.WTPart:63526").getObject();
			WTDocument d = (WTDocument) factory.getReference("VR:wt.doc.WTDocument:63317").getObject();
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
