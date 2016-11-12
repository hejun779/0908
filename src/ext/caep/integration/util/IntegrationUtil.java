package ext.caep.integration.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ptc.core.meta.common.IdentifierFactory;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.common.impl.TypeIdentifierUtilityHelper;
import com.ptc.windchill.enterprise.team.server.TeamCCHelper;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.content.FormatContentHolder;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.doc.WTDocumentMasterIdentity;
import wt.fc.IdentityHelper;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.collections.WTArrayList;
import wt.folder.Cabinet;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.folder.SubFolder;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartMasterIdentity;
import wt.part.WTPartUsageLink;
import wt.pdmlink.PDMLinkProduct;
import wt.pds.StatementSpec;
import wt.pom.PersistenceException;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.services.ServiceProviderHelper;
import wt.session.SessionHelper;
import wt.team.WTRoleHolder2;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.VersionControlHelper;
import wt.vc.config.IteratedFolderedConfigSpec;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.NonLatestCheckoutException;
import wt.vc.wip.WorkInProgressException;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;

public class IntegrationUtil implements RemoteAccess {
	private static final String proprFile = "ext/caep/integration/config.properties";
	private static Properties prop = null;
	private static PDMLinkProduct product = null;
	private static String shareFileHost = null;
	private static String shareFilePath = null;
	private static String shareFileHostUser = null;
	private static String shareFileHostPassword = null;

	static {
		try {
			InputStream in = IntegrationUtil.class.getClassLoader().getResourceAsStream(proprFile);
			prop = new Properties();
			prop.load(in);
			in.close();
			product = getProduct();
			shareFileHost = prop.getProperty("shareFileHost");
			shareFilePath = prop.getProperty("shareFilePath");
			shareFileHostUser = prop.getProperty("shareFileHostUser");
			shareFileHostPassword = prop.getProperty("shareFileHostPassword");
			login(shareFileHost, shareFileHostUser, shareFileHostPassword);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getProperty(String name) {
		String result = null;
		result = prop.getProperty(name);
		return result;
	}

	public static Properties getProperties() throws InvocationTargetException, IOException {
		prop = new Properties();
		if (!RemoteMethodServer.ServerFlag) {
			Class aclass[] = { String.class };
			Object aobj[] = { proprFile };
			return (Properties) RemoteMethodServer.getDefault().invoke("getProperties", "ext.caep.util.IntegrationUtil", null, aclass, aobj);
		}
		InputStream in = IntegrationUtil.class.getClassLoader().getResourceAsStream(proprFile);
		prop.load(in);
		in.close();
		return prop;
	}

	public static PDMLinkProduct getProduct() throws InvocationTargetException, IOException {
		if (product == null) {
			if (prop == null) {
				getProperties();
			}
			String productName = prop.getProperty("product", "\u5149\u5b66\u8bbe\u8ba1\u4ea7\u54c1\u5e93");
			try {
				QuerySpec spec = new QuerySpec(PDMLinkProduct.class);
				SearchCondition sc = new SearchCondition(PDMLinkProduct.class, PDMLinkProduct.NAME, SearchCondition.EQUAL, productName);
				spec.appendWhere(sc);
				QueryResult qr = PersistenceHelper.manager.find(spec);
				if (qr.hasMoreElements()) {
					product = (PDMLinkProduct) qr.nextElement();
				}
			} catch (QueryException e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
		return product;
	}

	public static File getSharedFile(String sharedFileName) throws Exception {
		File file = null;
		String filePath = getSharedFilePath(sharedFileName);
		if (filePath != null && filePath.length() > 0) {
			file = new File(filePath);
		}
		return file;
	}

	public static String getSharedFilePath(String sharedFileName) throws Exception {
		String filePath = null;
		if (sharedFileName != null && sharedFileName.length() > 0) {
			filePath = "\\\\" + shareFileHost + "\\" + shareFilePath + "\\" + sharedFileName;
		} else {
			filePath = "\\\\" + shareFileHost + "\\" + shareFilePath;
		}
		return filePath;
	}

	public static File createShareFile() throws Exception {
		String userName = SessionHelper.manager.getPrincipal().getName();
		File path = new File("\\\\" + shareFileHost + "\\" + shareFilePath + "\\" + userName);
		path.mkdirs();
		File file = new File(path, System.currentTimeMillis() + ".xml");
		file.createNewFile();
		return file;
	}

	public static File createShareFile(String fileName) throws Exception {
		String userName = SessionHelper.manager.getPrincipal().getName();
		File path = new File("\\\\" + shareFileHost + "\\" + shareFilePath + "\\" + userName);
		path.mkdirs();
		File file = new File(path, fileName);
		file.createNewFile();
		return file;
	}

	/**
	 * 登录网上邻居
	 * 
	 * @param host
	 * @param user
	 * @param pass
	 * @throws Exception
	 */
	public static void login(String host, String user, String pass) throws Exception {
		String info = "net use \\\\" + host + " " + pass + " /user:" + user;
		try {
			Process process = Runtime.getRuntime().exec(info);
			InputStream is = process.getInputStream();
			BufferedReader buf = new BufferedReader(new InputStreamReader(is, "gbk"));
			String data = null;
			while ((data = buf.readLine()) != null) {
				// System.out.println(data);
			}
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new Exception("访问共享文件服务器失败");
		}
	}

	public static Folder getFolder(String foldeName) throws WTException {
		Folder result = null;
		Cabinet root = product.getDefaultCabinet();
		QueryResult qr = FolderHelper.service.findSubFolders(root);
		while (qr.hasMoreElements()) {
			SubFolder folder = (SubFolder) qr.nextElement();
			if (folder.getName().equals(foldeName)) {
				result = folder;
				break;
			}
		}
		return result;
	}

	public static boolean isAdmin() throws WTException {
		boolean result = false;
		WTPrincipal principal = SessionHelper.manager.getPrincipal();
		WTRoleHolder2 roleHolder2 = TeamCCHelper.getTeamFromObject(product);
		HashMap map = TeamCCHelper.getMemberRoleHashMapFromTeam(roleHolder2);
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Entry) it.next();
			WTPrincipalReference user = (WTPrincipalReference) entry.getKey();
			ArrayList roles = (ArrayList) entry.getValue();
			for (Object o : roles) {
				if (principal.getName().equals(user.getName()) && o.toString().equalsIgnoreCase("PROJECT ADMIN")) {
					return true;
				}
			}
		}
		return result;
	}

	public static boolean isMember() throws WTException {
		boolean result = false;
		WTPrincipal principal = SessionHelper.manager.getPrincipal();
		WTRoleHolder2 roleHolder2 = TeamCCHelper.getTeamFromObject(product);
		HashMap map = TeamCCHelper.getMemberRoleHashMapFromTeam(roleHolder2);
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Entry) it.next();
			WTPrincipalReference user = (WTPrincipalReference) entry.getKey();
			ArrayList roles = (ArrayList) entry.getValue();
			for (Object o : roles) {
				if (principal.getName().equals(user.getName()) && o.toString().equalsIgnoreCase("OPTICAL ENGINEER")) {
					return true;
				}
			}
		}
		return result;
	}

	public static WTPart getPartFromNumber(String number) throws WTException {
		WTPart part = null;
		QuerySpec qs;
		qs = new QuerySpec(WTPart.class);
		SearchCondition sc = new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, number.toUpperCase());
		qs.appendWhere(sc);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.hasMoreElements()) {
			part = (WTPart) qr.nextElement();
			QueryResult all = VersionControlHelper.service.allVersionsOf(part.getMaster());
			if (all.hasMoreElements()) {
				part = (WTPart) all.nextElement();
				return part;
			}
		}
		return part;
	}

	public static boolean hasTask(WTPart project) throws WTException {
		boolean result = false;
		QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(project);
		if (qr.hasMoreElements()) {
			return true;
		}
		return result;
	}

	public static List<WTDocument> getDescribeDoc(WTPart part) throws WTException {
		List<WTDocument> result = new ArrayList<WTDocument>();
		QueryResult qr = WTPartHelper.service.getDescribedByWTDocuments(part);
		WTArrayList docList = new WTArrayList(qr);
		Iterator it = docList.persistableIterator();
		while (it.hasNext()) {
			WTDocument doc = (WTDocument) it.next();
			result.add(doc);
		}
		return result;
	}

	public static List<WTPart> getChildren(WTPart part) throws WTException {
		List<WTPart> children = new ArrayList<WTPart>();
		QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(part);
		while (qr.hasMoreElements()) {
			WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
			QueryResult childrenQR = VersionControlHelper.service.allVersionsOf(link.getUses());
			if (childrenQR.hasMoreElements()) {
				children.add((WTPart) childrenQR.nextElement());
			}
		}
		return children;
	}

	public static List<WTPart> getChildrenForMe(WTPart part) throws WTException {
		List<WTPart> children = new ArrayList<WTPart>();
		QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(part);
		while (qr.hasMoreElements()) {
			WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
			QueryResult childrenQR = VersionControlHelper.service.allVersionsOf(link.getUses());
			if (childrenQR.hasMoreElements()) {
				WTPart child = (WTPart) childrenQR.nextElement();
				if (child.getCreator().getName().equals(SessionHelper.manager.getPrincipal().getName())) {
					children.add(child);
				}
			}
		}
		return children;
	}

	public static WTDocument getDocFromNumber(String number) throws WTException {
		WTDocument doc = null;
		QuerySpec qs = new QuerySpec(WTDocumentMaster.class);
		SearchCondition sc = new SearchCondition(WTDocumentMaster.class, WTDocumentMaster.NUMBER, SearchCondition.EQUAL, number.toUpperCase());
		qs.appendWhere(sc);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.hasMoreElements()) {
			WTDocumentMaster master = (WTDocumentMaster) qr.nextElement();
			QueryResult all = VersionControlHelper.service.allVersionsOf(master);
			if (all.hasMoreElements()) {
				doc = (WTDocument) all.nextElement();
				return doc;
			}
		}
		return doc;
	}

	/**
	 * 在指定的光学产品中查询所有方案
	 * 
	 * @return
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static List<WTPart> getAllProject() throws WTException, WTPropertyVetoException {
		List<WTPart> projects = new ArrayList<WTPart>();
		QuerySpec qs = new QuerySpec();
		int partIndex = qs.addClassList(WTPart.class, true);
		// 搜索文件夹
		Folder folder = IntegrationUtil.getFolder(Constant.FOLDER_PROJECT);
		IteratedFolderedConfigSpec folder_cs = IteratedFolderedConfigSpec.newIteratedFolderedConfigSpec(folder);

		// 搜索方案软类型
		IdentifierFactory identifier_factory = (IdentifierFactory) ServiceProviderHelper.getService(IdentifierFactory.class, "logical");
		TypeIdentifier tid = (TypeIdentifier) identifier_factory.get(Constant.SOFTTYPE_PROJECT);
		SearchCondition sc = TypedUtilityServiceHelper.service.getSearchCondition(tid, true);
		qs.appendWhere(sc, new int[] { partIndex });
		qs.appendAnd();
		// 搜索最新版本
		qs.appendWhere(VersionControlHelper.getSearchCondition(WTPart.class, true));

		qs = folder_cs.appendSearchCriteria(qs);

		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		while (qr.hasMoreElements()) {
			Persistable[] ar = (Persistable[]) qr.nextElement();
			WTPart project = (WTPart) ar[partIndex];
			projects.add(project);
		}
		return projects;
	}

	public static List<WTPart> filter(List<WTPart> parts) throws WTException {
		List<WTPart> filtered = new ArrayList<WTPart>();
		if (isAdmin()) {
			return parts;
		} else {
			for (WTPart part : parts) {
				try {
					if (part.getCreator().getName().equalsIgnoreCase(SessionHelper.getPrincipal().getName())) {
						filtered.add(part);
					}
				} catch (WTException e) {
					e.printStackTrace();
				}
			}
		}
		return filtered;
	}

	public static String getType(Object obj) throws RemoteException, WTException {
		return TypeIdentifierUtilityHelper.service.getTypeIdentifier(obj).getTypename();
	}

	public static boolean isOwn(WTPart part) throws WTException {
		return part.getCreator().getName().equalsIgnoreCase(SessionHelper.getPrincipal().getName());
	}

	public static boolean isOwn(WTDocument doc) throws WTException {
		return doc.getCreator().getName().equalsIgnoreCase(SessionHelper.getPrincipal().getName());
	}

	public static boolean hasDescribePart(WTDocument doc) throws WTException {
		boolean hasDescribePart = false;
		QueryResult qr = WTPartHelper.service.getDescribesWTParts(doc);
		if (qr.size() > 0) {
			hasDescribePart = true;
		}
		return hasDescribePart;
	}

	public static Workable checkout(Workable workable) throws NonLatestCheckoutException, WorkInProgressException, WTPropertyVetoException, PersistenceException, WTException {
		Workable result = null;
		if (WorkInProgressHelper.isCheckedOut(workable)) {
			result = WorkInProgressHelper.service.workingCopyOf(workable);
		} else {
			CheckoutLink clink = WorkInProgressHelper.service.checkout(workable, WorkInProgressHelper.service.getCheckoutFolder(), "");
			result = clink.getWorkingCopy();
		}
		return result;
	}

	public static boolean isCheckout(Workable workable) throws WTException {
		boolean result = false;
		if (WorkInProgressHelper.isCheckedOut(workable) || WorkInProgressHelper.isWorkingCopy(workable)) {
			result = true;
		}
		return result;
	}

	public static Workable checkin(Workable workable) throws WorkInProgressException, WTPropertyVetoException, PersistenceException, WTException {
		Workable result = workable;
		if (WorkInProgressHelper.isWorkingCopy(workable)) {
			result = WorkInProgressHelper.service.checkin(workable, "");
		}
		return result;
	}

	public static Workable undoCheckout(Workable workable) throws WorkInProgressException, WTPropertyVetoException, PersistenceException, WTException {
		Workable result = workable;
		if (WorkInProgressHelper.isWorkingCopy(workable)) {
			result = WorkInProgressHelper.service.undoCheckout(workable);
		}
		return result;
	}

	public static void updateName(WTDocument doc, String newName) throws WTException, WTPropertyVetoException {
		if (newName != null && !newName.equals("") && !doc.getName().equals(newName)) {
			WTDocumentMaster master = (WTDocumentMaster) doc.getMaster();
			WTDocumentMasterIdentity docMasterIdentity;
			docMasterIdentity = (WTDocumentMasterIdentity) master.getIdentificationObject();
			docMasterIdentity.setName(newName);
			IdentityHelper.service.changeIdentity(master, docMasterIdentity);
		}
	}

	public static void updateName(WTPart part, String newName) throws WTException, WTPropertyVetoException {
		if (newName != null && !newName.equals("") && !part.getName().equals(newName)) {
			WTPartMaster master = (WTPartMaster) part.getMaster();
			WTPartMasterIdentity partMasterIdentity;
			partMasterIdentity = (WTPartMasterIdentity) master.getIdentificationObject();
			partMasterIdentity.setName(newName);
			IdentityHelper.service.changeIdentity(master, partMasterIdentity);
		}
	}

	public static String trim(Object obj) {
		String result = "";
		if (obj != null) {
			if (obj instanceof String) {
				result = ((String) obj).trim();
			} else {
				result = String.valueOf(obj);
			}
		}
		return result;
	}

	public static String trimQuery(Object obj) {
		String result = trim(obj);
		if (result.contains("*")) {
			result = result.replaceAll("[*]", "");
		}
		return result;
	}

	public static List<WTDocument> queryFilesForProject(String projectName, String projectID, String projectType, String projectDescribe) throws WTException, WTPropertyVetoException {
		List<WTDocument> result = new ArrayList<WTDocument>();
		QuerySpec qs = new QuerySpec();
		int partIndex = qs.addClassList(WTPart.class, true);
		// 搜索文件夹
		Folder folder = IntegrationUtil.getFolder(Constant.FOLDER_PROJECT);
		IteratedFolderedConfigSpec folder_cs = IteratedFolderedConfigSpec.newIteratedFolderedConfigSpec(folder);
		// 搜索方案软类型
		IdentifierFactory identifier_factory = (IdentifierFactory) ServiceProviderHelper.getService(IdentifierFactory.class, "logical");
		TypeIdentifier tid = (TypeIdentifier) identifier_factory.get(Constant.SOFTTYPE_PROJECT);
		SearchCondition sc = TypedUtilityServiceHelper.service.getSearchCondition(tid, true);
		qs.appendWhere(sc, new int[] { partIndex });
		qs.appendAnd();
		// 搜索最新版本
		qs.appendWhere(VersionControlHelper.getSearchCondition(WTPart.class, true));
		if (projectID != null && projectID.length() > 0) {
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.LIKE, "%" + projectID.toUpperCase() + "%"));
		}
		if (projectName != null && projectName.length() > 0) {
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NAME, SearchCondition.LIKE, "%" + projectName + "%"));
		}
		qs = folder_cs.appendSearchCriteria(qs);

		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		while (qr.hasMoreElements()) {
			Persistable[] ar = (Persistable[]) qr.nextElement();
			WTPart project = (WTPart) ar[partIndex];
			IBAUtil iba = new IBAUtil(project);
			if (projectType.length() > 0 && !compare(projectType, iba.getIBAValue(Constant.ATTR_CAEP_GX))) {
				continue;
			}
			if (projectDescribe.length() > 0 && !compare(projectDescribe, iba.getIBAValue(Constant.DESCRIBE))) {
				continue;
			}
			List<WTPart> tasks = getChildren(project);
			for (WTPart task : tasks) {
				List<WTPart> softwares = getChildren(task);
				for (WTPart software : softwares) {
					List<WTPart> paras = getChildren(software);
					for (WTPart para : paras) {
						List<WTDocument> files = getDescribeDoc(para);
						for (WTDocument file : files) {
							if (!result.contains(file)) {
								result.add(file);
							}
						}
					}
				}
			}
		}
		return result;
	}

	public static List<WTDocument> queryFilesForTask(String taskName, String taskID, String taskDescribe) throws WTPropertyVetoException, WTException {
		List<WTDocument> result = new ArrayList<WTDocument>();
		QuerySpec qs = new QuerySpec();
		int partIndex = qs.addClassList(WTPart.class, true);
		// 搜索文件夹
		Folder folder = IntegrationUtil.getFolder(Constant.FOLDER_PROJECT);
		IteratedFolderedConfigSpec folder_cs = IteratedFolderedConfigSpec.newIteratedFolderedConfigSpec(folder);
		// 搜索方案软类型
		IdentifierFactory identifier_factory = (IdentifierFactory) ServiceProviderHelper.getService(IdentifierFactory.class, "logical");
		TypeIdentifier tid = (TypeIdentifier) identifier_factory.get(Constant.SOFTTYPE_TASK);
		SearchCondition sc = TypedUtilityServiceHelper.service.getSearchCondition(tid, true);
		qs.appendWhere(sc, new int[] { partIndex });
		qs.appendAnd();
		// 搜索最新版本
		qs.appendWhere(VersionControlHelper.getSearchCondition(WTPart.class, true));
		if (taskID != null && taskID.length() > 0) {
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.LIKE, "%" + taskID.toUpperCase() + "%"));
		}
		if (taskName != null && taskName.length() > 0) {
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NAME, SearchCondition.LIKE, "%" + taskName + "%"));
		}
		qs = folder_cs.appendSearchCriteria(qs);
		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		while (qr.hasMoreElements()) {
			Persistable[] ar = (Persistable[]) qr.nextElement();
			WTPart task = (WTPart) ar[partIndex];
			IBAUtil iba = new IBAUtil(task);
			if (taskDescribe.length() > 0 && !compare(taskDescribe, iba.getIBAValue(Constant.ATTR_DESCRIBE))) {
				continue;
			}
			List<WTPart> softwares = getChildren(task);
			for (WTPart software : softwares) {
				List<WTPart> paras = getChildren(software);
				for (WTPart para : paras) {
					List<WTDocument> files = getDescribeDoc(para);
					for (WTDocument file : files) {
						if (!result.contains(file)) {
							result.add(file);
						}
					}
				}
			}
		}
		return result;
	}

	public static List<WTDocument> queryFilesForSoftware(String softwareName, String softwareID) throws WTException, WTPropertyVetoException {
		List<WTDocument> result = new ArrayList<WTDocument>();
		QuerySpec qs = new QuerySpec();
		int partIndex = qs.addClassList(WTPart.class, true);
		// 搜索文件夹
		Folder folder = IntegrationUtil.getFolder(Constant.FOLDER_PROJECT);
		IteratedFolderedConfigSpec folder_cs = IteratedFolderedConfigSpec.newIteratedFolderedConfigSpec(folder);
		// 搜索方案软类型
		IdentifierFactory identifier_factory = (IdentifierFactory) ServiceProviderHelper.getService(IdentifierFactory.class, "logical");
		TypeIdentifier tid = (TypeIdentifier) identifier_factory.get(Constant.SOFTTYPE_SOFTWARE);
		SearchCondition sc = TypedUtilityServiceHelper.service.getSearchCondition(tid, true);
		qs.appendWhere(sc, new int[] { partIndex });
		qs.appendAnd();
		// 搜索最新版本
		qs.appendWhere(VersionControlHelper.getSearchCondition(WTPart.class, true));
		if (softwareID != null && softwareID.length() > 0) {
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.LIKE, "%" + softwareID.toUpperCase() + "%"));
		}
		if (softwareName != null && softwareName.length() > 0) {
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NAME, SearchCondition.LIKE, "%" + softwareName + "%"));
		}
		qs = folder_cs.appendSearchCriteria(qs);
		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		while (qr.hasMoreElements()) {
			Persistable[] ar = (Persistable[]) qr.nextElement();
			WTPart software = (WTPart) ar[partIndex];
			List<WTPart> paras = getChildren(software);
			for (WTPart para : paras) {
				List<WTDocument> files = getDescribeDoc(para);
				for (WTDocument file : files) {
					if (!result.contains(file)) {
						result.add(file);
					}
				}
			}
		}
		return result;
	}

	public static List<WTDocument> queryFilesForPara(String paraName, String paraID) throws WTPropertyVetoException, WTException {
		List<WTDocument> result = new ArrayList<WTDocument>();
		QuerySpec qs = new QuerySpec();
		int partIndex = qs.addClassList(WTPart.class, true);
		// 搜索文件夹
		Folder folder = IntegrationUtil.getFolder(Constant.FOLDER_PROJECT);
		IteratedFolderedConfigSpec folder_cs = IteratedFolderedConfigSpec.newIteratedFolderedConfigSpec(folder);
		// 搜索方案软类型
		IdentifierFactory identifier_factory = (IdentifierFactory) ServiceProviderHelper.getService(IdentifierFactory.class, "logical");
		TypeIdentifier tid = (TypeIdentifier) identifier_factory.get(Constant.SOFTTYPE_PARA);
		SearchCondition sc = TypedUtilityServiceHelper.service.getSearchCondition(tid, true);
		qs.appendWhere(sc, new int[] { partIndex });
		qs.appendAnd();
		// 搜索最新版本
		qs.appendWhere(VersionControlHelper.getSearchCondition(WTPart.class, true));
		if (paraID != null && paraID.length() > 0) {
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.LIKE, "%" + paraID.toUpperCase() + "%"));
		}
		if (paraName != null && paraName.length() > 0) {
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NAME, SearchCondition.LIKE, "%" + paraName + "%"));
		}
		qs = folder_cs.appendSearchCriteria(qs);
		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		while (qr.hasMoreElements()) {
			Persistable[] ar = (Persistable[]) qr.nextElement();
			WTPart para = (WTPart) ar[partIndex];
			List<WTDocument> files = getDescribeDoc(para);
			for (WTDocument file : files) {
				if (!result.contains(file)) {
					result.add(file);
				}
			}
		}
		return result;
	}

	public static List<WTDocument> queryFiles(String fileName, String fileID, String fileType, String fileDescribe, String fileAuthor, String fileOrgan) throws WTException {
		List<WTDocument> result = new ArrayList<WTDocument>();
		QuerySpec qs = new QuerySpec();
		int partIndex = qs.addClassList(WTDocument.class, true);
		// 搜索方案软类型
		IdentifierFactory identifier_factory = (IdentifierFactory) ServiceProviderHelper.getService(IdentifierFactory.class, "logical");
		TypeIdentifier tid = (TypeIdentifier) identifier_factory.get(Constant.SOFTTYPE_IOFILE);
		SearchCondition sc = TypedUtilityServiceHelper.service.getSearchCondition(tid, true);
		qs.appendWhere(sc, new int[] { partIndex });
		qs.appendAnd();
		// 搜索最新版本
		qs.appendWhere(VersionControlHelper.getSearchCondition(WTDocument.class, true));
		if (fileID != null && fileID.length() > 0) {
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(WTDocument.class, WTDocument.NUMBER, SearchCondition.LIKE, "%" + fileID.toUpperCase() + "%"));
		}
		if (fileName != null && fileName.length() > 0) {
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(WTDocument.class, WTDocument.NAME, SearchCondition.LIKE, "%" + fileName + "%"));
		}
		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		while (qr.hasMoreElements()) {
			Persistable[] ar = (Persistable[]) qr.nextElement();
			WTDocument file = (WTDocument) ar[0];
			IBAUtil iba = new IBAUtil(file);
			if (fileType.length() > 0 && !compare(fileType, iba.getIBAValue(Constant.ATTR_CAEP_LXBS))) {
				continue;
			}
			if (fileDescribe.length() > 0 && !compare(fileDescribe, file.getDescription())) {
				continue;
			}
			if (fileAuthor.length() > 0 && !compare(fileAuthor, iba.getIBAValue(Constant.ATTR_CAEP_AUTHOR))) {
				continue;
			}
			if (fileOrgan.length() > 0 && !compare(fileOrgan, iba.getIBAValue(Constant.ATTR_CAEP_ORGAN))) {
				continue;
			}
			if (!result.contains(file)) {
				result.add(file);
			}
		}
		return result;

	}

	public static boolean compare(String str1, String str2) {
		str1 = trim(str1).toUpperCase();
		str2 = trim(str2).toUpperCase();
		boolean result = false;
		if (str2.contains(str1)) {
			result = true;
		}
		return result;
	}

	/**
	 * 根据编号规则判断是不是计算参数的ID 例:p000001
	 * 
	 * @param ID
	 * @return
	 */
	public static boolean isPara(String ID) {
		boolean result = false;
		if (ID != null) {
			ID = ID.toUpperCase();
			if (ID.startsWith("P") && ID.length() == 7) {
				ID = ID.substring(1);
				Pattern pattern = Pattern.compile("[0-9]*");
				Matcher isNum = pattern.matcher(ID);
				result = isNum.matches();
			}
		}
		return result;
	}

	public static void uploadContent(ContentHolder ctHolder, String filePath, ContentRoleType contentType) throws Exception {
		ApplicationData app_data = ApplicationData.newApplicationData(ctHolder);
		FileInputStream fis = null;
		try {
			File file = new File(filePath);
			fis = new FileInputStream(file);
			app_data.setFileName(ContentServerHelper.getFileName(filePath));
			app_data.setUploadedFromPath(filePath);
			app_data.setRole(contentType);
			app_data.setFileSize(file.length());
			if ((ctHolder != null) && ((ctHolder instanceof FormatContentHolder)) && (ContentRoleType.PRIMARY.equals(contentType))) {
				ctHolder = ContentServerHelper.service.updateHolderFormat((FormatContentHolder) ctHolder);
				ContentItem primary = ContentHelper.getPrimary((FormatContentHolder) ctHolder);
				if (primary != null) {
					ContentServerHelper.service.deleteContent(ctHolder, primary);
				}
			}
			app_data = ContentServerHelper.service.updateContent(ctHolder, app_data, fis);
		} catch (Exception e) {
			e.printStackTrace();
			throw new WTException(e);
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (Exception localException1) {
				throw new Exception(localException1.getMessage());
			}
		}
		if ((ctHolder instanceof FormatContentHolder)) {
			try {
				ctHolder = ContentServerHelper.service.updateHolderFormat((FormatContentHolder) ctHolder);
			} catch (Exception wtpve) {
				throw new WTException(wtpve);
			}
		}
	}

	public static boolean hasParent(WTPart part) {
		boolean result = false;
		if (part != null) {
			try {
				QueryResult qr = WTPartHelper.service.getUsedByWTParts((WTPartMaster) part.getMaster());
				if (qr.size() > 0) {
					result = true;
				}
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static String downloadFile(WTDocument doc) throws Exception {
		String path = "";
		QueryResult primary = ContentHelper.service.getContentsByRole(doc, ContentRoleType.PRIMARY);
		while (primary.hasMoreElements()) {
			ApplicationData data = (ApplicationData) primary.nextElement();
			String fullName = data.getFileName();
			InputStream is = ContentServerHelper.service.findContentStream(data);
			java.io.File contentFile = IntegrationUtil.createShareFile(fullName);
			FileOutputStream os = new FileOutputStream(contentFile);
			byte buff[] = new byte[1024];
			int len = 0;
			while ((len = is.read(buff)) > 0) {
				os.write(buff, 0, len);
			}
			os.close();
			is.close();
			path = contentFile.getPath();
		}
		return path;
	}
}
