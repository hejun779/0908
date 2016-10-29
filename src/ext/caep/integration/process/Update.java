package ext.caep.integration.process;

import ext.caep.integration.bean.File;
import ext.caep.integration.bean.Global;
import ext.caep.integration.bean.Para;
import ext.caep.integration.bean.Project;
import ext.caep.integration.bean.Software;
import ext.caep.integration.bean.Task;
import ext.caep.integration.util.IntegrationUtil;
import wt.doc.WTDocument;
import wt.part.WTPart;
import wt.util.WTPropertyVetoException;

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
			IntegrationUtil.updateName(part, project.getName());
			project.setState("");
			// TODO set describe type 修改type的话number会做改动
			// part = (WTPart) IntegrationUtil.checkout(part);
		}
	}

	private static void updateTask(Task task) {
		WTPart part = IntegrationUtil.getPartFromNumber(task.getID());
		if (part != null) {
			IntegrationUtil.updateName(part, task.getName());
			task.setState("");
			// TODO set describe
			// part = (WTPart) IntegrationUtil.checkout(part);

		}
	}

	private static void updateSoftware(Software software) {
		WTPart part = IntegrationUtil.getPartFromNumber(software.getID());
		if (part != null) {
			IntegrationUtil.updateName(part, software.getName());
			software.setState("");
		}
	}

	private static void updatePara(Para para) {
		WTPart part = IntegrationUtil.getPartFromNumber(para.getID());
		if (part != null) {
			IntegrationUtil.updateName(part, para.getName());
			para.setState("");
		}
	}

	private static void updateFile(File file) {
		WTDocument doc = IntegrationUtil.getDocFromNumber(file.getID());
		if (doc != null) {
			doc = (WTDocument) IntegrationUtil.checkout(doc);
			try {
				IntegrationUtil.updateName(doc, file.getName());
				doc.setDescription(file.getDescribe());
				// TODO set author organ type 修改type的话number会做改动
				file.setState("");
				IntegrationUtil.checkin(doc);
			} catch (WTPropertyVetoException e) {
				e.printStackTrace();
			}
		}
	}
}
