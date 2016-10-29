package ext.caep.integration.util;

import java.rmi.RemoteException;

import com.ptc.core.meta.common.impl.TypeIdentifierUtilityHelper;

import wt.fc.ReferenceFactory;
import wt.part.WTPart;
import wt.util.WTException;
import wt.util.WTRuntimeException;

public class TypeTest {

	public static void main(String[] args) {
		ReferenceFactory factory = new ReferenceFactory();
		try {
			WTPart part = (WTPart) factory.getReference("VR:wt.part.WTPart:52546").getObject();
			Object obj = new Object();
			System.out.println(TypeIdentifierUtilityHelper.service.getTypeIdentifier(obj).getTypename());
		} catch (WTRuntimeException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

}
