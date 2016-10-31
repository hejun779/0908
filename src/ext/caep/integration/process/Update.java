package ext.caep.integration.process;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import ext.caep.integration.bean.File;
import ext.caep.integration.bean.Global;
import ext.caep.integration.bean.Para;
import ext.caep.integration.bean.Project;
import ext.caep.integration.bean.Software;
import ext.caep.integration.bean.Task;
import ext.caep.integration.util.Constant;
import ext.caep.integration.util.IBAUtil;
import ext.caep.integration.util.IntegrationUtil;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.doc.WTDocument;
import wt.fc.QueryResult;
import wt.iba.value.service.LoadValue;
import wt.part.LoadPart;
import wt.part.WTPart;
import wt.pom.Transaction;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;

public class Update {
	public static Object process(Object root) {
		if (root instanceof Global) {
			updateGlobal((Global) root);
		} else if (root instanceof Project) {
			updateProject((Project) root);
		} else if (root instanceof Task) {
			updateTask((Task) root);
		} else if (root instanceof Software) {
			updateSoftware((Software) root);
		} else if (root instanceof Para) {
			updatePara((Para) root);
		} else if (root instanceof File) {
			updateFile((File) root);
		}
		return root;
	}

	private static void updateGlobal(Global root) {
		// Do nothing
	}

	private static void updateProject(Project project) {
		WTPart part = IntegrationUtil.getPartFromNumber(project.getID());
		if (part != null) {
			if (!part.getName().equals(project.getName())) {
				IntegrationUtil.updateName(part, project.getName());
			}
			Vector return_objects = new Vector();
			Hashtable cmd_line = new Hashtable();
			Hashtable partAttrs = new Hashtable();
			partAttrs.put("partNumber", project.getID());
			partAttrs.put("parentContainerPath", "/wt.inf.container.OrgContainer=ptc/wt.pdmlink.PDMLinkProduct=" + IntegrationUtil.getProperty("product"));
			partAttrs.put("type", "component");
			partAttrs.put("source", "make");
			partAttrs.put("folder", "/Default");

			LoadPart.beginCreateOrUpdateWTPart(partAttrs, cmd_line, return_objects);

			partAttrs.put("definition", Constant.ATTR_CAEP_GX);
			partAttrs.put("value1", project.getType() == null ? "" : project.getType());
			LoadValue.createIBAValue(partAttrs, cmd_line, return_objects);

			partAttrs.put("definition", Constant.ATTR_DESCRIBE);
			partAttrs.put("value1", project.getDescribe() == null ? "" : project.getDescribe());
			LoadValue.createIBAValue(partAttrs, cmd_line, return_objects);

			LoadPart.endCreateOrUpdateWTPart(partAttrs, cmd_line, return_objects);

			project.setState("");
		}
	}

	private static void updateTask(Task task) {
		WTPart part = IntegrationUtil.getPartFromNumber(task.getID());
		if (part != null) {
			if (!part.getName().equals(task.getName())) {
				IntegrationUtil.updateName(part, task.getName());
			}
			Vector return_objects = new Vector();
			Hashtable cmd_line = new Hashtable();
			Hashtable partAttrs = new Hashtable();
			partAttrs.put("partNumber", task.getID());
			partAttrs.put("parentContainerPath", "/wt.inf.container.OrgContainer=ptc/wt.pdmlink.PDMLinkProduct=" + IntegrationUtil.getProperty("product"));
			partAttrs.put("type", "component");
			partAttrs.put("source", "make");
			partAttrs.put("folder", "/Default");

			LoadPart.beginCreateOrUpdateWTPart(partAttrs, cmd_line, return_objects);

			partAttrs.put("definition", Constant.ATTR_DESCRIBE);
			partAttrs.put("value1", task.getDescribe() == null ? "" : task.getDescribe());
			LoadValue.createIBAValue(partAttrs, cmd_line, return_objects);

			LoadPart.endCreateOrUpdateWTPart(partAttrs, cmd_line, return_objects);
			task.setState("");
		}
	}

	private static void updateSoftware(Software software) {
		WTPart part = IntegrationUtil.getPartFromNumber(software.getID());
		if (part != null) {
			if (!part.getName().equals(software.getName())) {
				IntegrationUtil.updateName(part, software.getName());
			}
			software.setState("");
		}
	}

	private static void updatePara(Para para) {
		WTPart part = IntegrationUtil.getPartFromNumber(para.getID());
		if (part != null) {
			if (!part.getName().equals(para.getName())) {
				IntegrationUtil.updateName(part, para.getName());
			}
			para.setState("");
		}
	}

	private static void updateFile(File file) {
		WTDocument doc = IntegrationUtil.getDocFromNumber(file.getID());
		if (doc != null) {
			if (!doc.getName().equals(file.getName())) {
				IntegrationUtil.updateName(doc, file.getName());
			}
			Transaction trx = null;
			try {
				trx = new Transaction();
				trx.start();
				doc = (WTDocument) IntegrationUtil.checkout(doc);
				doc.setDescription(file.getDescribe() == null ? "" : file.getDescribe());
				doc = (WTDocument) IBAUtil.updateIBAValue(doc, Constant.ATTR_CAEP_LXBS, file.getType() == null ? "" : file.getType());
				doc = (WTDocument) IBAUtil.updateIBAValue(doc, Constant.ATTR_CAEP_ORGAN, file.getOrgan() == null ? "" : file.getOrgan());
				doc = (WTDocument) IBAUtil.updateIBAValue(doc, Constant.ATTR_CAEP_AUTHOR, file.getAuthor() == null ? "" : file.getAuthor());
				if (file.getPath() != null && !file.getPath().equals("")) {
					QueryResult primary = ContentHelper.service.getContentsByRole(doc, ContentRoleType.PRIMARY);
					if (primary.hasMoreElements()) {
						ApplicationData appData = (ApplicationData) primary.nextElement();
						appData.setFileName(file.getName());
						appData.setUploadedFromPath(file.getPath());
						InputStream is = new FileInputStream(file.getPath());
						ContentServerHelper.service.updateContent(doc, appData, is);
					}
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
		}
	}
}
