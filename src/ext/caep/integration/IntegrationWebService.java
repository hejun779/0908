package ext.caep.integration;

import java.io.File;

import org.jdom.Element;

import com.infoengine.object.factory.Att;
import com.infoengine.object.factory.Group;

import ext.caep.integration.process.Create;
import ext.caep.integration.process.Synchronize;
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
			File outputFile = IntegrationUtil.createShareFile();
			data = outputFile.getPath();
			// 为Global添加假ID
			if (xml.getRoot().getName().equalsIgnoreCase(Constant.GLOBAL)) {
				rootID = "1";
			}
			// 创建
			if (rootID == null || rootID.equals("")) {
				Create.process(root);
				// 同步
			} else if (rootState.equals(Constant.STATE_SYNCHRONIZE)) {
				JaxbUtil.object2xml(Synchronize.process(root), outputFile);
			} else if (rootState.equals(Constant.STATE_DELETE)) {
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

}
