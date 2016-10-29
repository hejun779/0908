package ext.caep.integration.util;

import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.doc.WTDocumentMasterIdentity;
import wt.fc.IdentityHelper;
import wt.fc.ReferenceFactory;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;

public class UpdateTest {

	public static void main(String[] args) {
		ReferenceFactory factory = new ReferenceFactory();
		try {
			WTDocument doc = (WTDocument) factory.getReference("VR:wt.doc.WTDocument:63498").getObject();
			doc = (WTDocument) IntegrationUtil.checkout(doc);
			WTDocumentMaster master = (WTDocumentMaster) doc.getMaster();
			WTDocumentMasterIdentity partMasterIdentity = (WTDocumentMasterIdentity) master.getIdentificationObject();
			// partMasterIdentity.setNumber(newNumber);
			partMasterIdentity.setName("fdsff");
			IdentityHelper.service.changeIdentity(master, partMasterIdentity);
			IntegrationUtil.checkin(doc);
		} catch (WTRuntimeException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		}
	}

}
