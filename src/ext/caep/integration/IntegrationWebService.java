package ext.caep.integration;

import java.io.File;
import java.util.List;

import org.jdom.Element;

import com.infoengine.object.factory.Att;
import com.infoengine.object.factory.Group;

import ext.caep.integration.bean.Files;
import ext.caep.integration.bean.Global;
import ext.caep.integration.bean.Para;
import ext.caep.integration.bean.Project;
import ext.caep.integration.bean.Software;
import ext.caep.integration.bean.Task;
import ext.caep.integration.process.Create;
import ext.caep.integration.process.Delete;
import ext.caep.integration.process.Download;
import ext.caep.integration.process.Synchronize;
import ext.caep.integration.process.Update;
import ext.caep.integration.util.Constant;
import ext.caep.integration.util.IntegrationUtil;
import ext.caep.integration.util.JaxbUtil;
import ext.caep.integration.util.XMLUtil;
import wt.method.RemoteAccess;

public class IntegrationWebService implements RemoteAccess {

	public static void main(String[] args) {
		userLoginService();
	}

	public static Group userLoginService() {
		Group group = new Group();
		com.infoengine.object.factory.Element el = new com.infoengine.object.factory.Element();
		el.addAtt(new Att("code", "0"));
		el.addAtt(new Att("message", "乱测"));
		if (IntegrationUtil.isAdmin()) {
			el.addAtt(new Att("data", Constant.ROLE_MANAGER));

		} else if (IntegrationUtil.isMember()) {
			el.addAtt(new Att("data", Constant.ROLE_MEMBER));
		} else {
			el.addAtt(new Att("data", Constant.ROLE_NOACCESS));
		}
		group.addElement(el);
		return group;

	}

	/**
	 * 供Task调用,将共享的输入文件作为输入,根据ID和state状态进行数据处理
	 * 
	 * @param sharedFile
	 * @return
	 */
	public static Group dataOperationService(String sharedFile) {
		Group group = new Group();
		String data = "";
		try {
			File file = IntegrationUtil.getSharedFile(sharedFile);
			XMLUtil xml = new XMLUtil(file);
			Element rootEl = (Element) xml.getRoot().getChildren().get(0);
			String rootState = rootEl.getAttributeValue(Constant.STATE);
			String rootID = rootEl.getAttributeValue(Constant.ID);
			Object root = JaxbUtil.xml2Object(file, rootEl);
			processDelegate(root, rootState, rootID);
			// 只有state状态为删除的时候没有输出文件
			if (!rootState.equals(Constant.STATE_DELETE)) {
				File outputFile = IntegrationUtil.createShareFile();
				data = outputFile.getPath();
				JaxbUtil.object2xml(root, outputFile);
			}
			com.infoengine.object.factory.Element el = new com.infoengine.object.factory.Element("");
			el.addAtt(new Att("code", "0"));
			el.addAtt(new Att("message", ""));
			el.addAtt(new Att("data", data));
			group.addElement(el);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return group;
	}

	/**
	 * 根据state状态,ID值进行请求转发
	 * 
	 * @param root
	 */
	private static void processDelegate(Object root, String state, String rootID) {
		// 创建:如果ID为空,不管state的值,表示创建,并且忽略所有子节点的state的状态,全部都视为创建
		if (rootID.equals("")) {
			new Create().process(root);
		}
		// 删除:如果state表示删除,将删除此节点,忽略子节点的状态,并根据角色依次删除所有子节点
		else if (state.equals(Constant.STATE_DELETE)) {
			Delete.process(root);
		}
		// 编辑:如果state表示更新,将更新此节点,且只更新此节点,子节点将根据自身的state状态进行处理
		else if (state.equals(Constant.STATE_UPDATE)) {
			Update.process(root);
			childrenDelegate(root);
		}
		// 同步:如果state表示同步,则同步包括此节点和所有子节点,忽略所有子节点的state状态
		else if (state.equals(Constant.STATE_SYNCHRONIZE)) {
			Synchronize.process(root);
		}
		// 下载:如果state表示下载,则下载此节点和所有子节点的文档主内容,忽略所有子节点的stae状态
		else if (state.equals(Constant.STATE_DOWNLOAD)) {
			Download.process(root);
		}
	}

	/**
	 * 根据root节点的类型获取其子节点,并提交子节点进行处理
	 * 
	 * @param root
	 */
	private static void childrenDelegate(Object root) {
		if (root instanceof Global) {
			Global global = (Global) root;
			List<Project> projects = global.getProjects();
			if (projects != null && !projects.isEmpty()) {
				for (Project project : projects) {
					processDelegate(project, project.getState(), project.getID());
				}
			}
		} else if (root instanceof Project) {
			Project project = (Project) root;
			Files files = project.getFiles();
			if (files != null && files.getClass() != null && !files.getFiles().isEmpty()) {
				for (ext.caep.integration.bean.File file : files.getFiles()) {
					processDelegate(file, file.getState(), file.getID());
				}
			}
			List<Task> tasks = project.getTasks();
			if (tasks != null && !tasks.isEmpty()) {
				for (Task task : tasks) {
					processDelegate(task, task.getState(), task.getID());
				}
			}
		} else if (root instanceof Software) {
			Software software = (Software) root;
			List<Para> paras = software.getParas();
			if (paras != null && !paras.isEmpty()) {
				for (Para para : paras) {
					processDelegate(para, para.getState(), para.getID());
				}
			}
		} else if (root instanceof Files) {
			Files files = (Files) root;
			List<ext.caep.integration.bean.File> fileList = files.getFiles();
			if (fileList != null && !fileList.isEmpty()) {
				for (ext.caep.integration.bean.File file : fileList) {
					processDelegate(file, file.getState(), file.getID());
				}
			}
		}
	}
}
