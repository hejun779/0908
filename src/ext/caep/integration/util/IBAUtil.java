package ext.caep.integration.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;

import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.iba.definition.DefinitionLoader;
import wt.iba.definition.litedefinition.AbstractAttributeDefinizerNodeView;
import wt.iba.definition.litedefinition.AbstractAttributeDefinizerView;
import wt.iba.definition.litedefinition.AttributeDefDefaultView;
import wt.iba.definition.litedefinition.AttributeDefNodeView;
import wt.iba.definition.litedefinition.AttributeOrgNodeView;
import wt.iba.definition.litedefinition.BooleanDefView;
import wt.iba.definition.litedefinition.FloatDefView;
import wt.iba.definition.litedefinition.IntegerDefView;
import wt.iba.definition.litedefinition.RatioDefView;
import wt.iba.definition.litedefinition.ReferenceDefView;
import wt.iba.definition.litedefinition.StringDefView;
import wt.iba.definition.litedefinition.TimestampDefView;
import wt.iba.definition.litedefinition.URLDefView;
import wt.iba.definition.litedefinition.UnitDefView;
import wt.iba.definition.service.IBADefinitionHelper;
import wt.iba.definition.service.IBADefinitionService;
import wt.iba.value.AttributeContainer;
import wt.iba.value.DefaultAttributeContainer;
import wt.iba.value.IBAHolder;
import wt.iba.value.IBAValueUtility;
import wt.iba.value.litevalue.AbstractValueView;
import wt.iba.value.litevalue.FloatValueDefaultView;
import wt.iba.value.litevalue.IntegerValueDefaultView;
import wt.iba.value.litevalue.StringValueDefaultView;
import wt.iba.value.service.IBAValueDBService;
import wt.iba.value.service.IBAValueHelper;
import wt.iba.value.service.LoadValue;
import wt.part.WTPart;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;

public class IBAUtil {

	Hashtable ibaContainer;
	private static boolean VERBOSE;
	private static String attrorg;

	static {
		try {
			WTProperties wtproperties = WTProperties.getLocalProperties();
			VERBOSE = wtproperties.getProperty("ext.generic.iba.verbose", false);
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private IBAUtil() {
		ibaContainer = new Hashtable();
	}

	public IBAUtil(IBAHolder ibaholder) {
		initializeIBAPart(ibaholder);
	}

	public IBAUtil(IBAHolder ibaholder, String attrOrg) {
		initializeIBAPart(ibaholder, attrOrg);
	}

	@Override
	public String toString() {
		StringBuffer stringbuffer = new StringBuffer();
		Enumeration enumeration = ibaContainer.keys();
		try {
			while (enumeration.hasMoreElements()) {
				String s = (String) enumeration.nextElement();
				AbstractValueView abstractvalueview = (AbstractValueView) ((Object[]) ibaContainer.get(s))[1];
				stringbuffer.append(s + " - " + IBAValueUtility.getLocalizedIBAValueDisplayString(abstractvalueview, SessionHelper.manager.getLocale()));
				stringbuffer.append('\n');
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return stringbuffer.toString();
	}

	public String getIBAValue(String s) {
		try {
			return getIBAValue(s, SessionHelper.manager.getLocale());
		} catch (WTException wte) {
			wte.printStackTrace();
		}
		return null;
	}

	public String getIBAValue(String s, Locale locale) {
		Object[] obj = (Object[]) ibaContainer.get(s);
		if (obj == null)
			return null;
		AbstractValueView avv = (AbstractValueView) obj[1];
		if (avv == null)
			return null;
		try {
			return IBAValueUtility.getLocalizedIBAValueDisplayString(avv, locale);
		} catch (WTException wte) {
			wte.printStackTrace();
		}
		return null;
	}

	private void initializeIBAPart(IBAHolder ibaholder) {
		ibaContainer = new Hashtable();
		try {
			ibaholder = IBAValueHelper.service.refreshAttributeContainer(ibaholder, null, SessionHelper.manager.getLocale(), null);
			DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer) ibaholder.getAttributeContainer();
			if (defaultattributecontainer != null) {
				AttributeDefDefaultView aattributedefdefaultview[] = defaultattributecontainer.getAttributeDefinitions();
				for (int i = 0; i < aattributedefdefaultview.length; i++) {
					AbstractValueView aabstractvalueview[] = defaultattributecontainer.getAttributeValues(aattributedefdefaultview[i]);
					if (aabstractvalueview != null) {
						Object aobj[] = new Object[2];
						aobj[0] = aattributedefdefaultview[i];
						aobj[1] = aabstractvalueview[0];
						ibaContainer.put(aattributedefdefaultview[i].getName(), ((aobj)));
						// System.out.println("The ibaContainer:" +
						// aattributedefdefaultview[i].getName());
					}
				}

			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private void initializeIBAPart(IBAHolder ibaholder, String attrOrg) {
		ibaContainer = new Hashtable();
		attrorg = attrOrg;
		try {
			ibaholder = IBAValueHelper.service.refreshAttributeContainer(ibaholder, null, SessionHelper.manager.getLocale(), null);
			DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer) ibaholder.getAttributeContainer();
			if (defaultattributecontainer != null) {
				AttributeDefDefaultView aattributedefdefaultview[] = defaultattributecontainer.getAttributeDefinitions();
				for (int i = 0; i < aattributedefdefaultview.length; i++) {
					AbstractValueView aabstractvalueview[] = defaultattributecontainer.getAttributeValues(aattributedefdefaultview[i]);
					if (aabstractvalueview != null) {
						Object aobj[] = new Object[2];
						aobj[0] = aattributedefdefaultview[i];
						aobj[1] = aabstractvalueview[0];
						ibaContainer.put(aattributedefdefaultview[i].getName(), ((aobj)));
					}
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public IBAHolder updateIBAPart(IBAHolder ibaholder) throws Exception {

		ibaholder = IBAValueHelper.service.refreshAttributeContainer(ibaholder, null, SessionHelper.manager.getLocale(), null);
		DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer) ibaholder.getAttributeContainer();
		for (Enumeration enumeration = ibaContainer.elements(); enumeration.hasMoreElements();)
			try {
				Object aobj[] = (Object[]) enumeration.nextElement();
				AbstractValueView abstractvalueview = (AbstractValueView) aobj[1];
				AttributeDefDefaultView attributedefdefaultview = (AttributeDefDefaultView) aobj[0];
				if (abstractvalueview.getState() == 1) {
					defaultattributecontainer.deleteAttributeValues(attributedefdefaultview);
					abstractvalueview.setState(3);
					defaultattributecontainer.addAttributeValue(abstractvalueview);
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}

		ibaholder.setAttributeContainer(defaultattributecontainer);

		return ibaholder;
	}

	public void setIBAValue(String s, String s1) throws WTPropertyVetoException {
		AbstractValueView abstractvalueview = null;
		AttributeDefDefaultView attributedefdefaultview = null;
		Object aobj[] = (Object[]) ibaContainer.get(s);
		if (aobj != null) {
			abstractvalueview = (AbstractValueView) aobj[1];
			attributedefdefaultview = (AttributeDefDefaultView) aobj[0];
		}
		if (abstractvalueview == null)
			attributedefdefaultview = getAttributeDefinition(s);
		if (attributedefdefaultview == null) {
			if (VERBOSE)
				System.out.println("definition is null ...");
			return;
		}
		abstractvalueview = internalCreateValue(attributedefdefaultview, s1);
		if (abstractvalueview == null) {
			if (VERBOSE)
				System.out.println("after creation, iba value is null ..");
			return;
		} else {
			abstractvalueview.setState(1);
			Object aobj1[] = new Object[2];
			aobj1[0] = attributedefdefaultview;
			aobj1[1] = abstractvalueview;
			ibaContainer.put(attributedefdefaultview.getName(), ((aobj1)));
			return;
		}
	}

	private AttributeDefDefaultView getAttributeDefinition(String s) {
		AttributeDefDefaultView attributedefdefaultview = null;
		try {
			attributedefdefaultview = IBADefinitionHelper.service.getAttributeDefDefaultViewByPath(s);
			if (attributedefdefaultview == null) {
				AbstractAttributeDefinizerView abstractattributedefinizerview = DefinitionLoader.getAttributeDefinition(s);
				if (abstractattributedefinizerview != null)
					attributedefdefaultview = IBADefinitionHelper.service.getAttributeDefDefaultView((AttributeDefNodeView) abstractattributedefinizerview);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return attributedefdefaultview;
	}

	private AbstractValueView internalCreateValue(AbstractAttributeDefinizerView abstractattributedefinizerview, String s) {
		AbstractValueView abstractvalueview = null;
		if (abstractattributedefinizerview instanceof FloatDefView)
			abstractvalueview = LoadValue.newFloatValue(abstractattributedefinizerview, s, null);
		else if (abstractattributedefinizerview instanceof StringDefView)
			abstractvalueview = LoadValue.newStringValue(abstractattributedefinizerview, s);
		else if (abstractattributedefinizerview instanceof IntegerDefView)
			abstractvalueview = LoadValue.newIntegerValue(abstractattributedefinizerview, s);
		else if (abstractattributedefinizerview instanceof RatioDefView)
			abstractvalueview = LoadValue.newRatioValue(abstractattributedefinizerview, s, null);
		else if (abstractattributedefinizerview instanceof TimestampDefView)
			abstractvalueview = LoadValue.newTimestampValue(abstractattributedefinizerview, s);
		else if (abstractattributedefinizerview instanceof BooleanDefView)
			abstractvalueview = LoadValue.newBooleanValue(abstractattributedefinizerview, s);
		else if (abstractattributedefinizerview instanceof URLDefView)
			abstractvalueview = LoadValue.newURLValue(abstractattributedefinizerview, s, null);
		else if (abstractattributedefinizerview instanceof ReferenceDefView)
			abstractvalueview = LoadValue.newReferenceValue(abstractattributedefinizerview, "ClassificationNode", s);
		else if (abstractattributedefinizerview instanceof UnitDefView)
			abstractvalueview = LoadValue.newUnitValue(abstractattributedefinizerview, s, null);
		return abstractvalueview;
	}

	// ����Ҫcheckout-in part ��ʹ��Iba��dbservice ��Iba���Խ����޸�
	// ʹ��ibadbservice����Iba���Եı���
	public WTPart setGeneralStringIBAProperty(String propertyName, String value, WTPart part) throws wt.introspection.WTIntrospectionException, wt.util.WTException, java.rmi.RemoteException {
		if (VERBOSE)
			System.out.println("*** in setGeneralStringIBAProperty, propertyName = " + propertyName);
		Locale locale = Locale.SIMPLIFIED_CHINESE;
		if (part == null)
			return part;

		if (VERBOSE)
			System.out.println("*** in addIBA ...");

		IBAHolder ibaHolder = part;
		PersistenceServerHelper.manager.update((Persistable) ibaHolder);
		ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder, null, locale, null);

		if (ibaHolder.getAttributeContainer() == null) {
			if (VERBOSE)
				System.out.println("*** AttributeContainer is null, set new Container.");
			try {
				ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder, null, locale, null);
				if (ibaHolder.getAttributeContainer() == null) {
					if (VERBOSE)
						System.out.println("*** AttributeContainer is still null.");
					ibaHolder.setAttributeContainer(new DefaultAttributeContainer());
				}
			} catch (wt.util.WTException e) {
				ibaHolder.setAttributeContainer(new DefaultAttributeContainer());
			} catch (java.rmi.RemoteException e) {
				ibaHolder.setAttributeContainer(new DefaultAttributeContainer());
			}
		}

		// Fill IBA container
		AbstractAttributeDefinizerNodeView ibaDefNode, ibaDefNode1, ibaDefNode2;
		AbstractAttributeDefinizerNodeView ibaDefNodes[];
		AttributeDefDefaultView ibaDefView = null;
		StringValueDefaultView ibaValueView = null;
		AbstractValueView ibaValueViews[];
		DefaultAttributeContainer container = (DefaultAttributeContainer) (ibaHolder.getAttributeContainer());

		try {
			if (VERBOSE)
				System.out.println("**** Begin set IBA ...");

			// �õ���һ�㣬ע��˴��Ǹ����Ŀ��������д
			ibaDefNode = getAttributeOrganizer(attrorg);
			ibaDefNodes = IBADefinitionHelper.service.getAttributeChildren(ibaDefNode);
			ibaDefNode = getNode(ibaDefNodes, propertyName);

			ibaDefView = getDefaultViewObject(ibaDefNode);
			container.deleteAttributeValues(ibaDefView);
			ibaValueView = new StringValueDefaultView((StringDefView) ibaDefView, value);
			container.addAttributeValue(ibaValueView);
			if (VERBOSE)
				System.out.println("*** fill " + propertyName + " completed.");
		} catch (wt.iba.value.IBAValueException ibave) {
			ibave.printStackTrace();
		} catch (java.rmi.RemoteException re) {
			re.printStackTrace();
		} catch (wt.util.WTException wte) {
			wte.printStackTrace();
		}
		// ʹ��dbservice ����Iba���Եı���
		IBAValueDBService ibavaluedbservice = new IBAValueDBService();
		Object obj = container.getConstraintParameter();
		AttributeContainer attributecontainer1 = ibavaluedbservice.updateAttributeContainer(ibaHolder, obj, null, null);
		ibaHolder.setAttributeContainer(attributecontainer1);

		if (VERBOSE)
			System.out.println("*** exit addIBA ...");
		return (WTPart) ibaHolder;
	}

	public IBAHolder setGeneralIntegerIBAProperty(String propertyName, long value, IBAHolder ibaHolder)
			throws wt.introspection.WTIntrospectionException, wt.util.WTException, java.rmi.RemoteException {
		if (VERBOSE)
			System.out.println("*** in setGeneralIntegerIBAProperty, propertyName = " + propertyName);
		Locale locale = Locale.SIMPLIFIED_CHINESE;
		if (ibaHolder == null)
			return ibaHolder;

		PersistenceServerHelper.manager.update((Persistable) ibaHolder);
		ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder, null, locale, null);

		if (ibaHolder.getAttributeContainer() == null) {
			if (VERBOSE)
				System.out.println("*** AttributeContainer is null, set new Container.");
			try {
				ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder, null, locale, null);
				if (ibaHolder.getAttributeContainer() == null) {
					if (VERBOSE)
						System.out.println("*** AttributeContainer is still null.");
					ibaHolder.setAttributeContainer(new DefaultAttributeContainer());
				}
			} catch (wt.util.WTException e) {
				ibaHolder.setAttributeContainer(new DefaultAttributeContainer());
			} catch (java.rmi.RemoteException e) {
				ibaHolder.setAttributeContainer(new DefaultAttributeContainer());
			}
		}

		// Fill IBA container
		AbstractAttributeDefinizerNodeView ibaDefNode, ibaDefNode1, ibaDefNode2;
		AbstractAttributeDefinizerNodeView ibaDefNodes[];
		AttributeDefDefaultView ibaDefView = null;
		IntegerValueDefaultView ibaValueView = null;
		AbstractValueView ibaValueViews[];
		DefaultAttributeContainer container = (DefaultAttributeContainer) (ibaHolder.getAttributeContainer());

		try {
			if (VERBOSE)
				System.out.println("**** Begin set IBA ...");

			// �õ���һ�㣬ע��˴��Ǹ����Ŀ��������д
			ibaDefNode = getAttributeOrganizer(attrorg);
			ibaDefNodes = IBADefinitionHelper.service.getAttributeChildren(ibaDefNode);
			ibaDefNode = getNode(ibaDefNodes, propertyName);

			ibaDefView = getDefaultViewObject(ibaDefNode);
			container.deleteAttributeValues(ibaDefView);
			ibaValueView = new IntegerValueDefaultView((IntegerDefView) ibaDefView, value);
			container.addAttributeValue(ibaValueView);
			if (VERBOSE)
				System.out.println("*** fill " + propertyName + " completed.");
		} catch (wt.iba.value.IBAValueException ibave) {
			ibave.printStackTrace();
		} catch (java.rmi.RemoteException re) {
			re.printStackTrace();
		} catch (wt.util.WTException wte) {
			wte.printStackTrace();
		}
		// ʹ��dbservice ����Iba���Եı���
		IBAValueDBService ibavaluedbservice = new IBAValueDBService();
		Object obj = container.getConstraintParameter();
		AttributeContainer attributecontainer1 = ibavaluedbservice.updateAttributeContainer(ibaHolder, obj, null, null);
		ibaHolder.setAttributeContainer(attributecontainer1);

		if (VERBOSE)
			System.out.println("*** exit addIBA ...");
		return ibaHolder;
	}

	public IBAHolder setGeneralFloatIBAProperty(String propertyName, float value, IBAHolder ibaHolder) throws wt.introspection.WTIntrospectionException, wt.util.WTException, java.rmi.RemoteException {
		if (VERBOSE)
			System.out.println("*** in setGeneralFloatIBAProperty, propertyName = " + propertyName);
		Locale locale = Locale.SIMPLIFIED_CHINESE;

		PersistenceServerHelper.manager.update((Persistable) ibaHolder);
		ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder, null, locale, null);

		if (ibaHolder.getAttributeContainer() == null) {
			if (VERBOSE)
				System.out.println("*** AttributeContainer is null, set new Container.");
			try {
				ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder, null, locale, null);
				if (ibaHolder.getAttributeContainer() == null) {
					if (VERBOSE)
						System.out.println("*** AttributeContainer is still null.");
					ibaHolder.setAttributeContainer(new DefaultAttributeContainer());
				}
			} catch (wt.util.WTException e) {
				ibaHolder.setAttributeContainer(new DefaultAttributeContainer());
			} catch (java.rmi.RemoteException e) {
				ibaHolder.setAttributeContainer(new DefaultAttributeContainer());
			}
		}

		// Fill IBA container
		AbstractAttributeDefinizerNodeView ibaDefNode, ibaDefNode1, ibaDefNode2;
		AbstractAttributeDefinizerNodeView ibaDefNodes[];
		AttributeDefDefaultView ibaDefView = null;
		FloatValueDefaultView ibaValueView = null;
		AbstractValueView ibaValueViews[];
		DefaultAttributeContainer container = (DefaultAttributeContainer) (ibaHolder.getAttributeContainer());

		try {
			if (VERBOSE)
				System.out.println("**** Begin set IBA ...");

			// �õ���һ�㣬ע��˴��Ǹ����Ŀ��������д
			ibaDefNode = getAttributeOrganizer(attrorg);
			ibaDefNodes = IBADefinitionHelper.service.getAttributeChildren(ibaDefNode);
			ibaDefNode = getNode(ibaDefNodes, propertyName);

			ibaDefView = getDefaultViewObject(ibaDefNode);
			container.deleteAttributeValues(ibaDefView);
			String temp = String.valueOf(value);
			if (VERBOSE)
				System.out.println("value=" + value);

			ibaValueView = new FloatValueDefaultView((FloatDefView) ibaDefView);
			ibaValueView.setValue(value);
			ibaValueView.setPrecision(temp.length() - 1);

			container.addAttributeValue(ibaValueView);
			if (VERBOSE)
				System.out.println("*** fill " + propertyName + " completed.");
		} catch (wt.iba.value.IBAValueException ibave) {
			ibave.printStackTrace();
		} catch (java.rmi.RemoteException re) {
			re.printStackTrace();
		} catch (wt.util.WTException wte) {
			wte.printStackTrace();
		} catch (wt.util.WTPropertyVetoException wte) {
			wte.printStackTrace();
		}
		// ʹ��dbservice ����Iba���Եı���
		IBAValueDBService ibavaluedbservice = new IBAValueDBService();
		Object obj = container.getConstraintParameter();
		AttributeContainer attributecontainer1 = ibavaluedbservice.updateAttributeContainer(ibaHolder, obj, null, null);
		ibaHolder.setAttributeContainer(attributecontainer1);

		if (VERBOSE)
			System.out.println("*** exit addIBA ...");
		return ibaHolder;
	}

	public IBAHolder setGeneralStringIBAProperty(String propertyName, String value, IBAHolder ibaHolder)
			throws wt.introspection.WTIntrospectionException, wt.util.WTException, java.rmi.RemoteException {
		if (VERBOSE)
			System.out.println("*** in setGeneralStringIBAProperty, propertyName = " + propertyName);
		Locale locale = Locale.SIMPLIFIED_CHINESE;
		if (ibaHolder == null)
			return ibaHolder;

		PersistenceServerHelper.manager.update((Persistable) ibaHolder);
		ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder, null, locale, null);

		if (ibaHolder.getAttributeContainer() == null) {
			if (VERBOSE)
				System.out.println("*** AttributeContainer is null, set new Container.");
			try {
				ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder, null, locale, null);
				if (ibaHolder.getAttributeContainer() == null) {
					if (VERBOSE)
						System.out.println("*** AttributeContainer is still null.");
					ibaHolder.setAttributeContainer(new DefaultAttributeContainer());
				}
			} catch (wt.util.WTException e) {
				ibaHolder.setAttributeContainer(new DefaultAttributeContainer());
			} catch (java.rmi.RemoteException e) {
				ibaHolder.setAttributeContainer(new DefaultAttributeContainer());
			}
		}

		// Fill IBA container
		AbstractAttributeDefinizerNodeView ibaDefNode, ibaDefNode1, ibaDefNode2;
		AbstractAttributeDefinizerNodeView ibaDefNodes[];
		AttributeDefDefaultView ibaDefView = null;
		StringValueDefaultView ibaValueView = null;
		AbstractValueView ibaValueViews[];
		DefaultAttributeContainer container = (DefaultAttributeContainer) (ibaHolder.getAttributeContainer());

		try {
			if (VERBOSE)
				System.out.println("**** Begin set IBA ...");

			// �õ���һ�㣬ע��˴��Ǹ����Ŀ��������д
			ibaDefNode = getAttributeOrganizer(attrorg);
			ibaDefNodes = IBADefinitionHelper.service.getAttributeChildren(ibaDefNode);
			ibaDefNode = getNode(ibaDefNodes, propertyName);

			ibaDefView = getDefaultViewObject(ibaDefNode);
			container.deleteAttributeValues(ibaDefView);
			ibaValueView = new StringValueDefaultView((StringDefView) ibaDefView, value);
			container.addAttributeValue(ibaValueView);
			if (VERBOSE)
				System.out.println("*** fill " + propertyName + " completed.");
		} catch (wt.iba.value.IBAValueException ibave) {
			ibave.printStackTrace();
		} catch (java.rmi.RemoteException re) {
			re.printStackTrace();
		} catch (wt.util.WTException wte) {
			wte.printStackTrace();
		}
		// ʹ��dbservice ����Iba���Եı���
		IBAValueDBService ibavaluedbservice = new IBAValueDBService();
		Object obj = container.getConstraintParameter();
		AttributeContainer attributecontainer1 = ibavaluedbservice.updateAttributeContainer(ibaHolder, obj, null, null);
		ibaHolder.setAttributeContainer(attributecontainer1);

		if (VERBOSE)
			System.out.println("*** exit addIBA ...");
		return ibaHolder;
	}

	public IBAHolder setRepeatStringIBAProperty(String propertyName, String value, IBAHolder ibaHolder)
			throws wt.introspection.WTIntrospectionException, wt.util.WTException, java.rmi.RemoteException {
		if (VERBOSE)
			System.out.println("*** in setRepeatStringIBAProperty, propertyName = " + propertyName);
		Locale locale = Locale.SIMPLIFIED_CHINESE;
		if (ibaHolder == null)
			return ibaHolder;

		PersistenceServerHelper.manager.update((Persistable) ibaHolder);
		ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder, null, locale, null);

		if (ibaHolder.getAttributeContainer() == null) {
			if (VERBOSE)
				System.out.println("*** AttributeContainer is null, set new Container.");
			try {
				ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder, null, locale, null);
				if (ibaHolder.getAttributeContainer() == null) {
					if (VERBOSE)
						System.out.println("*** AttributeContainer is still null.");
					ibaHolder.setAttributeContainer(new DefaultAttributeContainer());
				}
			} catch (wt.util.WTException e) {
				ibaHolder.setAttributeContainer(new DefaultAttributeContainer());
			} catch (java.rmi.RemoteException e) {
				ibaHolder.setAttributeContainer(new DefaultAttributeContainer());
			}
		}

		// Fill IBA container
		AbstractAttributeDefinizerNodeView ibaDefNode, ibaDefNode1, ibaDefNode2;
		AbstractAttributeDefinizerNodeView ibaDefNodes[];
		AttributeDefDefaultView ibaDefView = null;
		StringValueDefaultView ibaValueView = null;
		AbstractValueView ibaValueViews[];
		DefaultAttributeContainer container = (DefaultAttributeContainer) (ibaHolder.getAttributeContainer());

		try {
			if (VERBOSE)
				System.out.println("**** Begin set IBA ...");

			// �õ���һ�㣬ע��˴��Ǹ����Ŀ��������д
			ibaDefNode = getAttributeOrganizer(attrorg);
			ibaDefNodes = IBADefinitionHelper.service.getAttributeChildren(ibaDefNode);
			ibaDefNode = getNode(ibaDefNodes, propertyName);

			ibaDefView = getDefaultViewObject(ibaDefNode);
			// container.deleteAttributeValues(ibaDefView);
			ibaValueView = new StringValueDefaultView((StringDefView) ibaDefView, value);
			container.addAttributeValue(ibaValueView);
			if (VERBOSE)
				System.out.println("*** fill " + propertyName + " completed.");
		} catch (wt.iba.value.IBAValueException ibave) {
			ibave.printStackTrace();
		} catch (java.rmi.RemoteException re) {
			re.printStackTrace();
		} catch (wt.util.WTException wte) {
			wte.printStackTrace();
		}
		// ʹ��dbservice ����Iba���Եı���
		IBAValueDBService ibavaluedbservice = new IBAValueDBService();
		Object obj = container.getConstraintParameter();
		AttributeContainer attributecontainer1 = ibavaluedbservice.updateAttributeContainer(ibaHolder, obj, null, null);
		ibaHolder.setAttributeContainer(attributecontainer1);

		if (VERBOSE)
			System.out.println("*** exit addIBA ...");
		return ibaHolder;
	}

	// ���´���Ϊ����IBA��������ķ�������������޸�
	private AttributeOrgNodeView getAttributeOrganizer(String s) {
		if (s == null || s.length() == 0)
			return null;

		int i;
		AttributeOrgNodeView aattributeorgnodeview[] = null;
		try {
			aattributeorgnodeview = IBADefinitionHelper.service.getAttributeOrganizerRoots();
			for (i = 0; i < aattributeorgnodeview.length; i++) {
				if (aattributeorgnodeview[i] == null)
					continue;
				if (aattributeorgnodeview[i].getName().equalsIgnoreCase(s))
					return aattributeorgnodeview[i];
			}
		} catch (java.rmi.RemoteException remoteexception) {
			remoteexception.printStackTrace();
		} catch (WTException wte) {
			wte.printStackTrace();
		}

		return null;
	}

	private AbstractAttributeDefinizerNodeView getNodeChild(AbstractAttributeDefinizerNodeView node, String s) {
		int i;
		AbstractAttributeDefinizerNodeView abstractattributedefinizernodeview = null;
		AbstractAttributeDefinizerNodeView aabstractattributedefinizernodeview[] = null;

		if (!(node instanceof AbstractAttributeDefinizerNodeView) || s == null || s.length() == 0)
			return null;

		try {
			aabstractattributedefinizernodeview = IBADefinitionHelper.service.getAttributeChildren(node);
		} catch (java.rmi.RemoteException remoteexception) {
			remoteexception.printStackTrace();
		} catch (WTException wte) {
			wte.printStackTrace();
		}

		if (aabstractattributedefinizernodeview == null)
			return null;

		for (i = 0; i < aabstractattributedefinizernodeview.length; i++) {
			if (aabstractattributedefinizernodeview[i] == null)
				continue;
			if (aabstractattributedefinizernodeview[i].getName().equalsIgnoreCase(s))
				return aabstractattributedefinizernodeview[i];
		}

		return null;
	}

	private AbstractAttributeDefinizerNodeView getNode(AbstractAttributeDefinizerNodeView[] nodes, String s) {
		int i;

		if (nodes == null || nodes.length == 0 || s == null || s.length() == 0)
			return null;

		for (i = 0; i < nodes.length; i++) {
			if (nodes[i] == null)
				continue;
			if (nodes[i].getName().equalsIgnoreCase(s))
				return nodes[i];
		}

		return null;
	}

	private AttributeDefDefaultView getDefaultViewObject(Object obj) {
		AttributeDefDefaultView attributedefdefaultview = null;
		IBADefinitionService ibadefinitionservice = IBADefinitionHelper.service;
		try {
			if (obj instanceof AttributeDefNodeView)
				attributedefdefaultview = ibadefinitionservice.getAttributeDefDefaultView((AttributeDefNodeView) obj);
		} catch (java.rmi.RemoteException remoteexception) {
			remoteexception.printStackTrace();
		} catch (WTException wte) {
			wte.printStackTrace();
		}

		return attributedefdefaultview;
	}

	public IBAHolder removeStringIBAProperty(String propertyName, IBAHolder ibaHolder) throws wt.introspection.WTIntrospectionException, wt.util.WTException, java.rmi.RemoteException {
		if (VERBOSE)
			System.out.println("*** in removeStringIBAProperty, propertyName = " + propertyName);
		Locale locale = Locale.SIMPLIFIED_CHINESE;

		PersistenceServerHelper.manager.update((Persistable) ibaHolder);
		ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder, null, locale, null);

		if (ibaHolder.getAttributeContainer() == null) {
			if (VERBOSE)
				System.out.println("*** AttributeContainer is null, set new Container.");
			try {
				ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder, null, locale, null);
				if (ibaHolder.getAttributeContainer() == null) {
					if (VERBOSE)
						System.out.println("*** AttributeContainer is still null.");
					ibaHolder.setAttributeContainer(new DefaultAttributeContainer());
				}
			} catch (wt.util.WTException e) {
				ibaHolder.setAttributeContainer(new DefaultAttributeContainer());
			} catch (java.rmi.RemoteException e) {
				ibaHolder.setAttributeContainer(new DefaultAttributeContainer());
			}
		}

		// Fill IBA container
		AbstractAttributeDefinizerNodeView ibaDefNode, ibaDefNode1, ibaDefNode2;
		AbstractAttributeDefinizerNodeView ibaDefNodes[];
		AttributeDefDefaultView ibaDefView = null;
		StringValueDefaultView ibaValueView = null;
		AbstractValueView ibaValueViews[];
		DefaultAttributeContainer container = (DefaultAttributeContainer) (ibaHolder.getAttributeContainer());

		try {
			if (VERBOSE)
				System.out.println("**** Begin remove IBA ...");

			// �õ���һ�㣬ע��˴��Ǹ����Ŀ��������д
			ibaDefNode = getAttributeOrganizer(attrorg);
			ibaDefNodes = IBADefinitionHelper.service.getAttributeChildren(ibaDefNode);
			ibaDefNode = getNode(ibaDefNodes, propertyName);

			ibaDefView = getDefaultViewObject(ibaDefNode);
			container.deleteAttributeValues(ibaDefView);
		} catch (wt.iba.value.IBAValueException ibave) {
			ibave.printStackTrace();
		} catch (java.rmi.RemoteException re) {
			re.printStackTrace();
		} catch (wt.util.WTException wte) {
			wte.printStackTrace();
		}
		// ʹ��dbservice ����Iba���Եı���
		IBAValueDBService ibavaluedbservice = new IBAValueDBService();
		Object obj = container.getConstraintParameter();
		AttributeContainer attributecontainer1 = ibavaluedbservice.updateAttributeContainer(ibaHolder, obj, null, null);
		ibaHolder.setAttributeContainer(attributecontainer1);

		if (VERBOSE)
			System.out.println("*** exit remove ... ");
		return ibaHolder;
	}

	public static IBAHolder updateIBAValue(IBAHolder ibaHolder, String attributeidentifier, String value) throws Exception {
		IBAUtil util = new IBAUtil(ibaHolder);
		util.setIBAValue(attributeidentifier, value);
		// util.setIBAValue("com.saicmotor.le",value);
		ibaHolder = util.updateIBAPart(ibaHolder);
		ibaHolder = (IBAHolder) PersistenceHelper.manager.save((Persistable) ibaHolder);
		return ibaHolder;
	}

	public void setIBAValue(HashMap ibas) throws WTPropertyVetoException {
		if (ibas == null || ibas.isEmpty())
			return;
		Iterator iter = ibas.keySet().iterator();
		String key = null;
		String value = null;
		while (iter.hasNext()) {
			key = (String) iter.next();
			value = (String) ibas.get(key);
			if (value != null && !"".equals(value))
				setIBAValue(key, value);
		}
	}

	public IBAHolder store(IBAHolder ibaholder) throws Exception {
		ibaholder = IBAValueHelper.service.refreshAttributeContainer(ibaholder, null, SessionHelper.manager.getLocale(), null);
		DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer) ibaholder.getAttributeContainer();
		for (Enumeration enumeration = ibaContainer.elements(); enumeration.hasMoreElements();)
			try {
				Object aobj[] = (Object[]) enumeration.nextElement();
				AbstractValueView abstractvalueview = (AbstractValueView) aobj[1];
				AttributeDefDefaultView attributedefdefaultview = (AttributeDefDefaultView) aobj[0];
				if (abstractvalueview.getState() == 1) {
					defaultattributecontainer.deleteAttributeValues(attributedefdefaultview);
					abstractvalueview.setState(3);
					defaultattributecontainer.addAttributeValue(abstractvalueview);
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}

		ibaholder.setAttributeContainer(defaultattributecontainer);
		return ibaholder;
	}
}
