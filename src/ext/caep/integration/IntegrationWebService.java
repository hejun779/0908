package ext.caep.integration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import wt.method.RemoteAccess;
import wt.pom.Transaction;
import wt.util.WTException;

public class IntegrationWebService implements RemoteAccess {
	private Map<String, Object> parameters = new HashMap<String, Object>();

	public Group userLoginService() throws WTException {
		Group group = new Group();
		com.infoengine.object.factory.Element el = new com.infoengine.object.factory.Element();
		el.addAtt(new Att("code", "0"));
		el.addAtt(new Att("message", ""));
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
	public Group dataOperationService(String sharedFile) {
		Group group = new Group();
		String data = "";
		com.infoengine.object.factory.Element el = new com.infoengine.object.factory.Element("");
		Transaction trx = null;
		try {
			trx = new Transaction();
			trx.start();
			File inputFile = IntegrationUtil.getSharedFile(sharedFile);
			Class rootClass = IntegrationUtil.findRootClass(inputFile);
			Object root = JaxbUtil.xml2Object(inputFile, rootClass);
			String rootState = IntegrationUtil.getState(root);
			String rootID = IntegrationUtil.getID(root);
			// Files节点没有ID state属性,需要特殊处理
			if (root instanceof Files) {
				List<ext.caep.integration.bean.File> files = ((Files) root).getFiles();
				for (ext.caep.integration.bean.File file : files) {
					processDelegate(file, file.getState(), file.getID());
				}
			} else {
				processDelegate(root, rootState, rootID);
			}
			// 只有根节点state状态为删除的时候没有输出文件
			if (!Constant.STATE_DELETE.equals(rootState)) {
				File outputFile = IntegrationUtil.createShareFile();
				data = outputFile.getPath();// TODO
				String parent = IntegrationUtil.getSharedFilePath(null) + File.separator;
				if (data.startsWith(parent)) {
					data = data.substring(parent.length());
				}
				JaxbUtil.object2xml(root, outputFile);
			}
			el.addAtt(new Att("code", "0"));
			el.addAtt(new Att("message", "处理成功"));
			el.addAtt(new Att("data", data));
			// file.delete();//TODO
			trx.commit();
			trx = null;
		} catch (Exception e) {
			if (trx != null) {
				trx.rollback();
			}
			e.printStackTrace();
			el.addAtt(new Att("code", "1"));
			el.addAtt(new Att("message", e.getMessage()));
			el.addAtt(new Att("data", data));
		}
		group.addElement(el);
		return group;
	}

	/**
	 * 根据state状态和ID值进行请求转发
	 * 
	 * @param root
	 * @throws Exception
	 */
	private void processDelegate(Object root, String state, String rootID) throws Exception {
		// 创建:如果ID为空,不管state的值,表示创建,并且忽略所有子节点的state的状态,全部都视为创建
		if ("".equals(rootID)) {
			new Create().process(parameters, root);
		}
		// 删除:如果state表示删除,将删除此节点,忽略子节点的状态,并根据角色依次删除所有子节点
		else if (Constant.STATE_DELETE.equals(state)) {
			new Delete().process(parameters, root);
		}
		// 编辑:如果state表示更新,将更新此节点,且只更新此节点,子节点将根据自身的state状态进行处理
		else if (Constant.STATE_UPDATE.equals(state)) {
			if (!(root instanceof Global)) {
				Update.process(root);
			}
			if (!(root instanceof ext.caep.integration.bean.File)) {
				childrenDelegate(root);
			}
		}
		// 同步:如果state表示同步,则同步包括此节点和所有子节点,忽略所有子节点的state状态
		else if (Constant.STATE_SYNCHRONIZE.equals(state)) {
			Synchronize.process(root);
		}
		// 下载:如果state表示下载,则下载此节点和所有子节点的文档主内容,忽略所有子节点的stae状态
		else if (Constant.STATE_DOWNLOAD.equals(state)) {
			Download.process(root);
		} else if (Constant.STATE_NOCHANGE.equals(state)) {
			if (!(root instanceof ext.caep.integration.bean.File)) {
				childrenDelegate(root);
			}
		}
	}

	/**
	 * 根据root节点的类型获取其子节点,并提交子节点进行处理
	 * 
	 * @param root
	 * @throws Exception
	 */
	private void childrenDelegate(Object root) throws Exception {
		if (root instanceof Global) {
			Global global = (Global) root;
			List<Project> projects = global.getProjects();
			if (projects != null && !projects.isEmpty()) {
				parameters.put("currentGlobal", global);
				for (Project project : projects) {
					processDelegate(project, project.getState(), project.getID());
				}
			}
		} else if (root instanceof Project) {
			Project project = (Project) root;
			parameters.put("currentProject", project);
			parameters.put("parentNumber", project.getID());
			parameters.put("numberPrefixObj", project);
			parameters.put("currentFolder", Constant.FOLDER_PROJECT);

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
		} else if (root instanceof Task) {
			Task task = (Task) root;
			parameters.put("currentTask", task);
			parameters.put("parentNumber", task.getID());
			parameters.put("numberPrefixObj", task);
			parameters.put("currentFolder", Constant.FOLDER_PROJECT);
			Files files = task.getFiles();
			if (files != null && files.getFiles() != null && !files.getFiles().isEmpty()) {
				for (ext.caep.integration.bean.File file : files.getFiles()) {
					processDelegate(file, file.getState(), file.getID());
				}
			}
			List<Software> softwares = task.getSoftwares();
			if (softwares != null && !softwares.isEmpty()) {

				for (Software software : softwares) {
					processDelegate(software, software.getState(), software.getID());
				}
			}
		} else if (root instanceof Software) {
			Software software = (Software) root;
			parameters.put("currentSoftware", software);
			parameters.put("parentNumber", software.getID());
			parameters.put("currentFolder", software.getName() + "文件");
			List<Para> paras = software.getParas();
			if (paras != null && !paras.isEmpty()) {

				for (Para para : paras) {
					processDelegate(para, para.getState(), para.getID());
				}
			}
		} else if (root instanceof Para) {
			Para para = (Para) root;
			parameters.put("parentNumber", para.getID());
			List<ext.caep.integration.bean.File> files = para.getFiles();
			for (ext.caep.integration.bean.File file : files) {
				processDelegate(file, file.getState(), file.getID());
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
