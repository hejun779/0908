package ext.caep.integration.process;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import ext.caep.integration.bean.File;
import ext.caep.integration.bean.Para;
import ext.caep.integration.bean.Project;
import ext.caep.integration.bean.Software;
import ext.caep.integration.bean.Task;
import ext.caep.integration.util.Constant;
import ext.caep.integration.util.IBAUtil;
import ext.caep.integration.util.IntegrationUtil;
import wt.content.ContentRoleType;
import wt.doc.WTDocument;
import wt.iba.value.service.LoadValue;
import wt.part.LoadPart;
import wt.part.WTPart;
import wt.pom.Transaction;

public class Update {
	private String filePath;

	public Update(Map<String, Object> parameters) {
		this.filePath = (String) parameters.get("filePath");
	}

	public Object process(Object root) throws Exception {

		if (root instanceof Project) {
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

	private void updateProject(Project project) throws Exception {
		WTPart part = IntegrationUtil.getPartFromNumber(project.getID());
		if (part != null) {
			if (!IntegrationUtil.isAdmin()) {
				throw new Exception("非方案管理员不能修改方案(ID:" + project.getID() + ")");
			}
			if (!part.getName().equals(project.getName())) {
				IntegrationUtil.updateName(part, project.getName());
			}

			Vector return_objects = new Vector();
			Hashtable cmd_line = new Hashtable();
			Hashtable partAttrs = new Hashtable();
			partAttrs.put("partNumber", project.getID());
			partAttrs.put("parentContainerPath", IntegrationUtil.getContainerPath());
			partAttrs.put("type", "component");
			partAttrs.put("source", "make");
			partAttrs.put("folder", "/Default/" + Constant.FOLDER_PROJECT);

			LoadPart.beginCreateOrUpdateWTPart(partAttrs, cmd_line, return_objects);

			// partAttrs.put("definition", Constant.ATTR_CAEP_GX);
			// partAttrs.put("value1", project.getType() == null ? "" :
			// project.getType());
			// LoadValue.createIBAValue(partAttrs, cmd_line, return_objects);

			partAttrs.put("definition", Constant.ATTR_DESCRIBE);
			partAttrs.put("value1", project.getDescribe() == null ? "" : project.getDescribe());
			LoadValue.createIBAValue(partAttrs, cmd_line, return_objects);

			LoadPart.endCreateOrUpdateWTPart(partAttrs, cmd_line, return_objects);

			project.setState("");
		} else {
			throw new Exception("ID为" + project.getID() + "的方案不存在");
		}
	}

	private void updateTask(Task task) throws Exception {
		WTPart part = IntegrationUtil.getPartFromNumber(task.getID());
		if (part != null) {
			if (!IntegrationUtil.isOwn(part) && !IntegrationUtil.isAdmin()) {
				throw new Exception("非方案管理员不能修改别人的计算任务(ID:" + task.getID() + ")");
			}
			if (!part.getName().equals(task.getName())) {
				IntegrationUtil.updateName(part, task.getName());
			}
			Vector return_objects = new Vector();
			Hashtable cmd_line = new Hashtable();
			Hashtable partAttrs = new Hashtable();
			partAttrs.put("partNumber", task.getID());
			partAttrs.put("parentContainerPath", IntegrationUtil.getContainerPath());
			partAttrs.put("type", "component");
			partAttrs.put("source", "make");
			partAttrs.put("folder", "/Default/" + Constant.FOLDER_PROJECT);

			LoadPart.beginCreateOrUpdateWTPart(partAttrs, cmd_line, return_objects);

			partAttrs.put("definition", Constant.ATTR_DESCRIBE);
			partAttrs.put("value1", task.getDescribe() == null ? "" : task.getDescribe());
			LoadValue.createIBAValue(partAttrs, cmd_line, return_objects);

			LoadPart.endCreateOrUpdateWTPart(partAttrs, cmd_line, return_objects);
			task.setState("");
		} else {
			throw new Exception("ID为" + task.getID() + "的计算任务不存在");
		}
	}

	private void updateSoftware(Software software) throws Exception {
		WTPart part = IntegrationUtil.getPartFromNumber(software.getID());
		if (part != null) {
			if (!IntegrationUtil.isOwn(part) && !IntegrationUtil.isAdmin()) {
				throw new Exception("非方案管理员不能修改别人的专有软件(ID:" + software.getID() + ")");
			}
			if (!part.getName().equals(software.getName())) {
				IntegrationUtil.updateName(part, software.getName());
			}
			software.setState("");
		} else {
			throw new Exception("ID为" + software.getID() + "的专有软件不存在");
		}
	}

	private void updatePara(Para para) throws Exception {
		WTPart part = IntegrationUtil.getPartFromNumber(para.getID());
		if (part != null) {
			if (!IntegrationUtil.isOwn(part) && !IntegrationUtil.isAdmin()) {
				throw new Exception("非方案管理员不能修改别人的计算参数(ID:" + para.getID() + ")");
			}
			if (!part.getName().equals(para.getName())) {
				IntegrationUtil.updateName(part, para.getName());
			}
			para.setState("");
		} else {
			throw new Exception("ID为" + para.getID() + "的计算参数不存在");
		}
	}

	private void updateFile(File file) throws Exception {
		WTDocument doc = IntegrationUtil.getDocFromNumber(file.getID());
		if (doc != null) {
			if (IntegrationUtil.isProjectFile(doc) && !IntegrationUtil.isAdmin()) {
				throw new Exception("非方案管理员不能修改方案附属文件(ID:" + file.getID() + ")");
			}
			if (!IntegrationUtil.isAdmin() && !IntegrationUtil.isOwn(doc)) {
				throw new Exception("非方案管理员不能修改别人的的文档(ID:" + file.getID() + ")");
			}
			if (!doc.getName().equals(file.getName())) {
				IntegrationUtil.updateName(doc, file.getName());
			}
			Transaction trx = null;
			try {
				trx = new Transaction();
				trx.start();
				doc = (WTDocument) IntegrationUtil.checkout(doc);
				doc.setDescription(StringUtils.trimToEmpty(file.getDescribe()));
				if (file.getType() == null || file.getType().length() == 0) {
					throw new Exception("ID为" + file.getID() + "的类型标识不能为空");
				}
				doc = (WTDocument) IBAUtil.updateIBAValue(doc, Constant.ATTR_CAEP_LXBS, file.getType());
				doc = (WTDocument) IBAUtil.updateIBAValue(doc, Constant.ATTR_CAEP_ORGAN, StringUtils.trimToEmpty(file.getOrgan()));
				doc = (WTDocument) IBAUtil.updateIBAValue(doc, Constant.ATTR_CAEP_AUTHOR, StringUtils.trimToEmpty(file.getAuthor()));
				if (file.getPath() != null && !file.getPath().equals("")) {
					String filePath = IntegrationUtil.getSharedFilePath(this.filePath + java.io.File.separator + file.getPath());
					IntegrationUtil.uploadContent(doc, filePath, ContentRoleType.PRIMARY);
				} else {
					IntegrationUtil.removePrimary(doc);
				}
				IntegrationUtil.checkin(doc);
				// file.setPath("");
				file.setState("");
				trx.commit();
				trx = null;
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			} finally {
				if (trx != null) {
					trx.rollback();
				}
			}
		} else {
			throw new Exception("ID为" + file.getID() + "的文档不存在");
		}
	}
}
