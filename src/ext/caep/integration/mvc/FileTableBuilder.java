package ext.caep.integration.mvc;

import java.util.ArrayList;
import java.util.List;

import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.mvc.components.AbstractComponentBuilder;
import com.ptc.mvc.components.ComponentBuilder;
import com.ptc.mvc.components.ComponentConfig;
import com.ptc.mvc.components.ComponentConfigFactory;
import com.ptc.mvc.components.ComponentParams;
import com.ptc.mvc.components.TableConfig;
import com.ptc.mvc.util.ClientMessageSource;

import ext.caep.integration.resource.IntegrationResource;
import ext.caep.integration.util.Constant;
import ext.caep.integration.util.IntegrationUtil;
import wt.doc.WTDocument;
import wt.util.WTException;

@ComponentBuilder("caep.integration.filesTable")
public class FileTableBuilder extends AbstractComponentBuilder {
	private static final String RESOURCE = "ext.caep.integration.resourcce.IntegrationResource";

	@Override
	public Object buildComponentData(ComponentConfig arg0, ComponentParams arg1) throws Exception {

		List<WTDocument> results = new ArrayList<WTDocument>();

		JcaComponentParams params = (JcaComponentParams) arg1;

		String projectName = IntegrationUtil.trimQuery(params.getParameter("projectName"));
		String projectID = IntegrationUtil.trimQuery(params.getParameter("projectID"));
		String projectType = IntegrationUtil.trimQuery(params.getParameter("projectType"));
		String projectDescribe = IntegrationUtil.trimQuery(params.getParameter("projectDescribe"));
		String taskName = IntegrationUtil.trimQuery(params.getParameter("taskName"));
		String taskID = IntegrationUtil.trimQuery(params.getParameter("taskID"));
		String taskDescribe = IntegrationUtil.trimQuery(params.getParameter("taskDescribe"));
		String softwareName = IntegrationUtil.trimQuery(params.getParameter("softwareName"));
		String softwareID = IntegrationUtil.trimQuery(params.getParameter("softwareID"));
		String paraName = IntegrationUtil.trimQuery(params.getParameter("paraName"));
		String paraID = IntegrationUtil.trimQuery(params.getParameter("paraID"));
		String fileName = IntegrationUtil.trimQuery(params.getParameter("fileName"));
		String fileID = IntegrationUtil.trimQuery(params.getParameter("fileID"));
		String fileType = IntegrationUtil.trimQuery(params.getParameter("fileType"));
		String fileDescribe = IntegrationUtil.trimQuery(params.getParameter("fileDescribe"));
		String fileAuthor = IntegrationUtil.trimQuery(params.getParameter("fileAuthor"));
		String fileOrgan = IntegrationUtil.trimQuery(params.getParameter("fileOrgan"));
		StringBuffer sbFile = new StringBuffer(fileName).append(fileID).append(fileType).append(fileDescribe).append(fileAuthor).append(fileOrgan);
		StringBuffer sbPara = new StringBuffer(paraName).append(paraID);
		StringBuffer sbSoftware = new StringBuffer(softwareName).append(softwareID);
		StringBuffer sbTask = new StringBuffer(taskName).append(taskID).append(taskDescribe);
		StringBuffer sbProject = new StringBuffer(projectName).append(projectID).append(projectType).append(projectDescribe);
		StringBuffer all = new StringBuffer(sbFile).append(sbPara).append(sbSoftware).append(sbTask).append(sbProject);

		// 所有查询条件都没有输入,查询所有输入输出文件
		if (all.length() == 0) {
			return IntegrationUtil.queryFiles(fileName, fileID, fileType, fileDescribe, fileAuthor, fileOrgan);
		}
		List<List<WTDocument>> searchResults = new ArrayList<List<WTDocument>>();
		// 输入输出文件有查询条件,查询输入输出文件
		if (sbFile.length() > 0) {
			List<WTDocument> files = IntegrationUtil.queryFiles(fileName, fileID, fileType, fileDescribe, fileAuthor, fileOrgan);
			if (!files.isEmpty()) {
				searchResults.add(files);
			} else {
				return results;
			}
		}
		// 计算参数有查询条件,查询计算参数下的输入输出文件
		if (sbPara.length() > 0) {
			List<WTDocument> files = IntegrationUtil.queryFilesForPara(paraName, paraID);
			if (!files.isEmpty()) {
				searchResults.add(files);
			} else {
				return results;
			}
		}
		// 专有软件有查询条件,查询专有软件下的输入输出文件
		if (sbSoftware.length() > 0) {
			List<WTDocument> files = IntegrationUtil.queryFilesForSoftware(softwareName, softwareID);
			if (!files.isEmpty()) {
				searchResults.add(files);
			} else {
				return results;
			}
		}
		// 计算任务有查询条件,查询计算任务下的输入输出文件
		if (sbTask.length() > 0) {
			List<WTDocument> files = IntegrationUtil.queryFilesForTask(taskName, taskID, taskDescribe);
			if (!files.isEmpty()) {
				searchResults.add(files);
			} else {
				return results;
			}
		}
		// 方案有查询条件,查询方案下的输入输出文件
		if (sbProject.length() > 0) {
			List<WTDocument> files = IntegrationUtil.queryFilesForProject(projectName, projectID, projectType, projectDescribe);
			if (!files.isEmpty()) {
				searchResults.add(files);
			} else {
				return results;
			}
		}
		// 求交集
		if (!searchResults.isEmpty()) {
			results = searchResults.remove(0);
			for (List<WTDocument> searchResult : searchResults) {
				results.retainAll(searchResult);
			}
		}
		return results;
	}

	@Override
	public ComponentConfig buildComponentConfig(ComponentParams arg0) throws WTException {
		ComponentConfigFactory factory = getComponentConfigFactory();
		ClientMessageSource messageSource = getMessageSource(RESOURCE);

		TableConfig table = factory.newTableConfig();
		table.setLabel(messageSource.getMessage(IntegrationResource.INPUTOUTPUTTABLE));
		// table.setSelectable(true);
		// set the actionModel that comes in the TableToolBar
		table.setActionModel("mvc_tables_toolbar");

		// add columns
		table.addComponent(factory.newColumnConfig("number", true));
		table.addComponent(factory.newColumnConfig("name", true));
		table.addComponent(factory.newColumnConfig(Constant.ATTR_CAEP_LXBS, true));
		table.addComponent(factory.newColumnConfig(Constant.ATTR_CAEP_AUTHOR, true));
		table.addComponent(factory.newColumnConfig(Constant.ATTR_CAEP_ORGAN, true));
		table.addComponent(factory.newColumnConfig("description", true));

		table.addComponent(factory.newColumnConfig("version", true));
		table.addComponent(factory.newColumnConfig("state", false));
		table.addComponent(factory.newColumnConfig("thePersistInfo.modifyStamp", true));
		return table;

	}

}
