package ext.caep.integration.process;

import ext.caep.integration.bean.File;
import ext.caep.integration.bean.Global;
import ext.caep.integration.bean.Para;
import ext.caep.integration.bean.Project;
import ext.caep.integration.bean.Software;
import ext.caep.integration.bean.Task;
import ext.caep.integration.util.IntegrationUtil;
import wt.doc.WTDocument;
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

	}

	private static void updateProject(Project root) {

	}

	private static void updateTask(Task root) {

	}

	private static void updateSoftware(Software root) {

	}

	private static void updatePara(Para root) {

	}

	private static void updateFile(File file) {
		WTDocument doc = IntegrationUtil.getDocFromNumber(file.getID());
		doc = (WTDocument) IntegrationUtil.checkout(doc);
		try {
			doc.setName(file.getName());
			doc.setDescription(file.getDescribe());
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		}

	}
}
