package ext.caep.integration.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Vector;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.doc.DocumentType;
import wt.doc.LoadDoc;
import wt.doc.WTDocument;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.iba.value.service.LoadValue;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.LoadPart;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.pom.PersistenceException;
import wt.pom.Transaction;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;

public class SaveTest110 implements RemoteAccess {

	public static void main(String[] args) {
		// loadBOM();
		// createDoc();
		RemoteMethodServer.getDefault().setUserName("demo");
		RemoteMethodServer.getDefault().setPassword("demo");
		docUpdate();
	}

	public static void docUpdate() {
		if (RemoteMethodServer.ServerFlag) {
			ReferenceFactory factory = new ReferenceFactory();
			Transaction trx = null;
			try {
				trx = new Transaction();
				trx.start();
				WTDocument doc = (WTDocument) factory.getReference("VR:wt.doc.WTDocument:65738").getObject();
				doc = (WTDocument) IntegrationUtil.checkout(doc);
				doc.setDescription("newd");
				doc = (WTDocument) IBAUtil.updateIBAValue(doc, "caep_lxbs", "caep_lxbs");
				doc = (WTDocument) IBAUtil.updateIBAValue(doc, "organ", "organ");
				doc = (WTDocument) IBAUtil.updateIBAValue(doc, "author", "author");
				QueryResult primary = ContentHelper.service.getContentsByRole(doc, ContentRoleType.PRIMARY);
				String fileName = "e:\\result.txt";
				while (primary.hasMoreElements()) {
					ApplicationData appData = (ApplicationData) primary.nextElement();
					appData.setFileName("1.xml");
					appData.setUploadedFromPath(fileName);
					InputStream is = new FileInputStream(fileName);
					ContentServerHelper.service.updateContent(doc, appData, is);
				}
				IntegrationUtil.checkin(doc);
				trx.commit();
				trx = null;
			} catch (WTRuntimeException e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			} catch (WTPropertyVetoException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (trx != null) {
					trx.rollback();
				}
			}
		} else {
			try {
				RemoteMethodServer.getDefault().invoke("docUpdate", SaveTest110.class.getName(), null, null, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	public static void updateDoc() {
		if (RemoteMethodServer.ServerFlag) {
			Vector return_objects = new Vector();
			Hashtable cmd_line = new Hashtable();
			Hashtable docAttrs = new Hashtable();
			docAttrs.put("number", "0000000082");
			// docAttrs.put("name", "doctest");
			docAttrs.put("saveIn", "/Default");
			// docAttrs.put("type", "Document");
			docAttrs.put("typedef", "lfrc.caep.File");
			docAttrs.put("parentContainerPath", "/wt.inf.container.OrgContainer=ptc/wt.pdmlink.PDMLinkProduct=demo");
			docAttrs.put("department", "DESIGN");
			docAttrs.put("description", "describe");

			// docAttrs.put("primarycontenttype", "ApplicationData");
			// docAttrs.put("path", "\\\\localhost\\windchill\\result.txt");

			LoadDoc.beginCreateWTDocument(docAttrs, cmd_line, return_objects);

			docAttrs.put("definition", "caep_lxbs");
			docAttrs.put("value1", "PPF");
			LoadValue.createIBAValue(docAttrs, cmd_line, return_objects);

			Hashtable organ = new Hashtable();
			docAttrs.put("definition", "organ");
			docAttrs.put("value1", "design");
			LoadValue.createIBAValue(docAttrs, cmd_line, return_objects);

			docAttrs.put("definition", "author");
			docAttrs.put("value1", "tom");
			LoadValue.createIBAValue(docAttrs, cmd_line, return_objects);

			LoadDoc.endCreateWTDocument(docAttrs, cmd_line, return_objects);
		} else {
			try {
				RemoteMethodServer.getDefault().invoke("updateDoc", SaveTest110.class.getName(), null, null, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	public static void update() {
		if (RemoteMethodServer.ServerFlag) {
			Vector return_objects = new Vector();
			Hashtable cmd_line = new Hashtable();
			Hashtable partAttrs = new Hashtable();
			partAttrs.put("partNumber", "0000000081");
			// partAttrs.put("partName", "project111");
			partAttrs.put("parentContainerPath", "/wt.inf.container.OrgContainer=ptc/wt.pdmlink.PDMLinkProduct=demo");
			partAttrs.put("type", "component");
			// partAttrs.put("typedef",
			// "WCTYPE|wt.part.WTPart|lfrc.caep.Project");
			partAttrs.put("source", "make");
			partAttrs.put("folder", "/Default");
			// partAttrs.put("view", "Design");

			LoadPart.beginCreateOrUpdateWTPart(partAttrs, cmd_line, return_objects);

			partAttrs.put("definition", Constant.ATTR_CAEP_GX);
			partAttrs.put("value1", "test111");
			LoadValue.createIBAValue(partAttrs, cmd_line, return_objects);

			partAttrs.put("definition", Constant.ATTR_DESCRIBE);
			partAttrs.put("value1", "描述111");
			LoadValue.createIBAValue(partAttrs, cmd_line, return_objects);

			// partAttrs.put("parentContainerPath", null);
			LoadPart.endCreateOrUpdateWTPart(partAttrs, cmd_line, return_objects);
		} else {
			try {
				RemoteMethodServer.getDefault().invoke("update", SaveTest110.class.getName(), null, null, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	public static void createDoc() {
		if (RemoteMethodServer.ServerFlag) {
			Transaction ts = new Transaction();
			try {
				ts.start();
				Vector return_objects = new Vector();
				Hashtable cmd_line = new Hashtable();
				// cmd_line.put("-CONT_PATH",
				// "/wt.inf.container.OrgContainer=ptc/wt.pdmlink.PDMLinkProduct=demo");
				Hashtable docAttrs = new Hashtable();
				docAttrs.put("number", "doc0001");
				docAttrs.put("name", "doctest");
				// docAttrs.put("domain", "demo");
				docAttrs.put("saveIn", "/Default");
				docAttrs.put("type", "Document");
				docAttrs.put("typedef", "lfrc.caep.File");
				docAttrs.put("parentContainerPath", "/wt.inf.container.OrgContainer=ptc/wt.pdmlink.PDMLinkProduct=demo");
				docAttrs.put("department", "DESIGN");

				docAttrs.put("primarycontenttype", "ApplicationData");
				// docAttrs.put("path", "e:\\esult.txt");
				// docAttrs.put("path", "file:///e:/result");
				docAttrs.put("path", "\\\\localhost\\windchill\\result.txt");

				// docAttrs.put("author", "tom");

				LoadDoc.beginCreateWTDocument(docAttrs, cmd_line, return_objects);

				docAttrs.put("definition", "caep_lxbs");
				docAttrs.put("value1", "PPF");
				LoadValue.createIBAValue(docAttrs, cmd_line, return_objects);

				Hashtable organ = new Hashtable();
				docAttrs.put("definition", "organ");
				docAttrs.put("value1", "design");
				LoadValue.createIBAValue(docAttrs, cmd_line, return_objects);

				// Hashtable ibas = new Hashtable();
				//
				docAttrs.put("definition", "author");
				docAttrs.put("value1", "tom");
				LoadValue.createIBAValue(docAttrs, cmd_line, return_objects);
				// WTDocument doc = LoadDoc.getDocument();
				// IBAUtil iba = new IBAUtil(doc);
				// iba.setIBAValue("organ", "design");
				// iba.setIBAValue("author", "tom");
				// iba.setIBAValue("caep_lxbs", "ppf");

				// LoadValue.endIBAHolder(ibas, cmd_line, return_objects);
				// LoadValue.saveCurrentIBAHolder();
				// LoadValue.endIBAHolder(docAttrs, cmd_line, return_objects);
				// LoadValue.endIBAHolder(docAttrs, cmd_line, return_objects);
				// LoadValue.endIBAHolder(ibas, cmd_line, return_objects);
				// LoadValue.saveCurrentIBAHolder();
				// Hashtable caep_lxbs = new Hashtable();
				// LoadValue.saveCurrentIBAHolder();
				// LoadValue.endIBAHolder(docAttrs, cmd_line, return_objects);
				// IBAHolder holder = LoadValue.getCurrentIBAHolder();

				// LoadValue.applySoftAttributes(holder);

				LoadDoc.endCreateWTDocument(docAttrs, cmd_line, return_objects);
				ts.commit();
				ts = null;
			} catch (PersistenceException e) {
				e.printStackTrace();
				if (ts != null) {
					ts.rollback();
					ts = null;
				}
			}

			// WTDocument doc = (WTDocument) return_objects.get(0);
			// Calendar cal =
			// Calendar.getInstance(TimeZoneHelper.getTimeZone());
			// Timestamp now = new Timestamp(cal.getTimeInMillis());
			// String time = WTStandardDateFormat.format(now);
			// ArrayList result = new ArrayList();
			// LoaderHelper.storeData(new File("e:\result.txt"), doc, true,
			// "default", time, true, true, true, result);
		} else {
			try {
				RemoteMethodServer.getDefault().invoke("createDoc", SaveTest110.class.getName(), null, null, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}

	}

	public static void bom() {
		Hashtable assmAttrs = new Hashtable();
		assmAttrs.put("assemblyPartNumber", "PARENT001");
		assmAttrs.put("constituentPartNumber", "CHILD001");
		assmAttrs.put("constituentPartQty", "1");
		assmAttrs.put("constituentPartUnit", "ea");
		Hashtable cmd_line = new Hashtable();
		Vector return_objects = new Vector();
		LoadPart.addPartToAssembly(assmAttrs, cmd_line, return_objects);
	}

	public static void save() {
		if (RemoteMethodServer.ServerFlag) {
			try {
				WTPart parent = WTPart.newWTPart("TestSave", "Test Save 0001");
				WTPart child = WTPart.newWTPart("TestSave Child", "Test Save 0002");
				WTDocument doc = WTDocument.newWTDocument("Test Save Doc", "Test Save Doc 01", DocumentType.getDocumentTypeDefault());
				WTPartUsageLink pLink = WTPartUsageLink.newWTPartUsageLink(parent, (WTPartMaster) child.getMaster());
				WTPartDescribeLink dLink = WTPartDescribeLink.newWTPartDescribeLink(parent, doc);
				WTCollection col = new WTArrayList();
				col.add(parent);
				col.add(child);
				col.add(doc);
				col.add(pLink);
				col.add(dLink);
				PersistenceHelper.manager.save(child);
				// PersistenceHelper.manager.store(child);
				Timestamp ts = new Timestamp(System.currentTimeMillis());
				parent = (WTPart) PersistenceServerHelper.manager.store(parent, ts, ts);
				child = (WTPart) PersistenceServerHelper.manager.store(child, ts, ts);
				WTPartUsageLink link = WTPartUsageLink.newWTPartUsageLink(parent, (WTPartMaster) child.getMaster());
				PersistenceServerHelper.manager.store(link, ts, ts);
				// PersistenceHelper.manager.insave(col);
			} catch (WTException e) {
				e.printStackTrace();
			}
		} else {
			try {
				RemoteMethodServer.getDefault().invoke("save", SaveTest110.class.getName(), null, null, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	public static void loadBOM() {
		if (RemoteMethodServer.ServerFlag) {
			Vector return_objects = new Vector();
			Hashtable cmd_line = new Hashtable();
			cmd_line.put("-CONT_PATH", "/wt.inf.container.OrgContainer=ptc/wt.pdmlink.PDMLinkProduct=demo");
			Hashtable parentAttrs = new Hashtable();
			parentAttrs.put("partNumber", "parent001");
			parentAttrs.put("partName", "1.2.3.4");
			parentAttrs.put("parentContainerPath", "/wt.inf.container.OrgContainer=ptc/wt.pdmlink.PDMLinkProduct=demo");
			parentAttrs.put("type", "component");
			parentAttrs.put("source", "make");
			parentAttrs.put("folder", "/Default");
			parentAttrs.put("view", "Design");

			LoadPart.beginCreateWTPart(parentAttrs, cmd_line, return_objects);

			Hashtable childAttrs = new Hashtable();
			childAttrs.put("partNumber", "child001");
			childAttrs.put("partName", "child1");
			childAttrs.put("parentContainerPath", "/wt.inf.container.OrgContainer=ptc/wt.pdmlink.PDMLinkProduct=demo");
			childAttrs.put("type", "component");
			childAttrs.put("source", "make");
			childAttrs.put("folder", "/Default");
			childAttrs.put("view", "Design");

			// LoadPart.beginCreateWTPart(parentAttrs, cmd_line,
			// return_objects);
			System.out.println("return_objects:" + return_objects.size());
			LoadPart.cachePart(parentAttrs, cmd_line, return_objects);
			LoadPart.beginCreateWTPart(childAttrs, cmd_line, return_objects);
			System.out.println("return_objects:" + return_objects.size());

			Hashtable assmAttrs = new Hashtable();
			assmAttrs.put("assemblyPartNumber", "parent001");
			assmAttrs.put("constituentPartNumber", "child001");
			assmAttrs.put("constituentPartQty", "1");
			assmAttrs.put("constituentPartUnit", "ea");

			LoadPart.addPartToAssembly(assmAttrs, cmd_line, return_objects);

			System.out.println("return_objects:" + return_objects.size());

			LoadPart.endCreateWTPart(parentAttrs, cmd_line, return_objects);// 顺序可换
			LoadPart.endCreateWTPart(childAttrs, cmd_line, return_objects);
		} else {
			try {
				RemoteMethodServer.getDefault().invoke("loadBOM", SaveTest110.class.getName(), null, null, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
}
