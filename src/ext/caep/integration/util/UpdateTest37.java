package ext.caep.integration.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Locale;

import com.ptc.core.lwc.server.LWCNormalizedObject;
import com.ptc.core.meta.common.CreateOperationIdentifier;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentRoleType;
import wt.content.DataFormat;
import wt.content.DataFormatReference;
import wt.content.StreamData;
import wt.doc.WTDocument;
import wt.fc.ObjectReference;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.pom.Transaction;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;

public class UpdateTest37 implements RemoteAccess {

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
		RemoteMethodServer.getDefault().setUserName("demo");
		RemoteMethodServer.getDefault().setPassword("demo");
		updateLob();

	}

	public static void updateLob() {
		if (RemoteMethodServer.ServerFlag) {
			RemoteMethodServer.getDefault().setUserName("demo");
			RemoteMethodServer.getDefault().setPassword("demo");
			// update();
			ReferenceFactory factory = new ReferenceFactory();
			Transaction trx = new Transaction();
			try {
				trx.start();
				WTDocument doc = (WTDocument) factory.getReference("VR:wt.doc.WTDocument:73010").getObject();
				QueryResult primary = ContentHelper.service.getContentsByRole(doc, ContentRoleType.PRIMARY);
				if (primary.hasMoreElements()) {
					ApplicationData appData = (ApplicationData) primary.nextElement();
					StreamData streamData = (StreamData) appData.getStreamData().getObject();
					File file = new File("e:\\msdia80.dll");
					FileInputStream is = new FileInputStream(file);
					PersistenceServerHelper.manager.lock(streamData);

					// PersistenceServerHelper.manager.updateLob(streamData,
					// streamData.getLobLoc(), is, file.length(), true);
					long count = PersistenceServerHelper.manager.updateLob(streamData, streamData.getLobLoc(), is, true);

					appData.setFileName(file.getName());
					appData.setFileSize(count);
					appData.setUploadedFromPath(file.getPath());
					appData.setFormatName("application/x-msdownload");

					DataFormat format = new DataFormat();
					format.setMimeType("application/x-msdownload");
					DataFormatReference dfr = DataFormatReference.newDataFormatReference(format);
					appData.setFormat(dfr);
					ObjectReference dataRef = ObjectReference.newObjectReference(streamData);
					appData.setStreamData(dataRef);
					PersistenceHelper.manager.save(appData);

				}
				trx.commit();
				trx = null;
			} catch (WTRuntimeException e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (WTPropertyVetoException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (trx != null) {
					trx.rollback();
				}
			}
		} else {
			try {
				RemoteMethodServer.getDefault().invoke("updateLob", UpdateTest37.class.getName(), null, null, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	public static void updateIBA() {
		if (RemoteMethodServer.ServerFlag) {
			RemoteMethodServer.getDefault().setUserName("demo");
			RemoteMethodServer.getDefault().setPassword("demo");
			// update();
			ReferenceFactory factory = new ReferenceFactory();
			try {
				WTDocument doc = (WTDocument) factory.getReference("VR:wt.doc.WTDocument:73010").getObject();

				// doc = (WTDocument) IntegrationUtil.checkout(doc);
				LWCNormalizedObject obj = new LWCNormalizedObject(doc, null, Locale.US, new CreateOperationIdentifier());
				obj.load(Constant.ATTR_CAEP_AUTHOR);
				/* Set value of IBAName soft attribute to IBAValue */
				obj.set(Constant.ATTR_CAEP_AUTHOR, "11atom");
				obj.apply();
				// doc.setDescription("" + System.currentTimeMillis());
				// PersistentObjectManager.getPom().
				// MultiObjIBAValueDBService.newMultiObjIBAValueDBService().PersistenceServerHelper.manager.update(doc);
				// IntegrationUtil.checkin(doc);
				PersistenceServerHelper.manager.update(doc);
			} catch (WTRuntimeException e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			}
		} else {
			try {
				RemoteMethodServer.getDefault().invoke("updateIBA", UpdateTest37.class.getName(), null, null, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	public static void update() {
		if (RemoteMethodServer.ServerFlag) {
			ReferenceFactory factory = new ReferenceFactory();
			try {
				WTDocument part = (WTDocument) factory.getReference("VR:wt.doc.WTDocument:73010").getObject();
				part.setDescription("testupdate");
				part = (WTDocument) IBAUtility.setIBAValue(part, Constant.ATTR_CAEP_AUTHOR, "atom");
				part = (WTDocument) IBAUtility.setIBAValue(part, Constant.ATTR_CAEP_ORGAN, "organ");
			} catch (WTRuntimeException e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			} catch (WTPropertyVetoException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				RemoteMethodServer.getDefault().invoke("update", UpdateTest37.class.getName(), null, null, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

}
