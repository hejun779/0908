package ext.caep.integration.util;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import wt.doc.WTDocument;
import wt.fc.PersistenceServerHelper;
import wt.fc.ReferenceFactory;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;

public class UpdateTest6 implements RemoteAccess {

	public static void main(String[] args) {
		// ReferenceFactory factory = new ReferenceFactory();
		// try {
		// WTDocument doc = (WTDocument)
		// factory.getReference("VR:wt.doc.WTDocument:64045").getObject();
		// // doc = (WTDocument) IntegrationUtil.checkout(doc);
		// WTDocumentMaster master = (WTDocumentMaster) doc.getMaster();
		// WTDocumentMasterIdentity partMasterIdentity =
		// (WTDocumentMasterIdentity) master.getIdentificationObject();
		// // partMasterIdentity.setNumber(newNumber);
		// partMasterIdentity.setName("fdsff003");
		// IdentityHelper.service.changeIdentity(master, partMasterIdentity);
		// // IntegrationUtil.checkin(doc);
		// } catch (WTRuntimeException e) {
		// e.printStackTrace();
		// } catch (WTException e) {
		// e.printStackTrace();
		// } catch (WTPropertyVetoException e) {
		// e.printStackTrace();
		// }
		update();
	}

	public static void update() {
		if (RemoteMethodServer.ServerFlag) {
			ReferenceFactory factory = new ReferenceFactory();
			try {
				WTDocument part = (WTDocument) factory.getReference("VR:wt.doc.WTDocument:73010").getObject();
				part.setDescription("testupdate");
				part = (WTDocument) IBAUtility.setIBAValue(part, Constant.ATTR_CAEP_AUTHOR, "atom");
				part = (WTDocument) IBAUtility.setIBAValue(part, Constant.ATTR_CAEP_ORGAN, "organ");
				PersistenceServerHelper.manager.update(part);

			} catch (WTRuntimeException e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			} catch (WTPropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				RemoteMethodServer.getDefault().invoke("update", UpdateTest6.class.getName(), null, null, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

}
