package ext.caep.integration.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.ptc.core.meta.common.IdentifierFactory;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.common.impl.TypeIdentifierUtilityHelper;
import com.ptc.windchill.enterprise.team.server.TeamCCHelper;

import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.doc.WTDocumentMasterIdentity;
import wt.fc.IdentityHelper;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.folder.Cabinet;
import wt.folder.CabinetReference;
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
import wt.vc.Mastered;
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// getFolder("方案任务结构树");
	}

	public static String getProperty(String name) {
		String result = null;
		result = prop.getProperty(name);
		return result;
	}

	public static Properties getProperties() {
		prop = new Properties();
		if (!RemoteMethodServer.ServerFlag) {
			Class aclass[] = { String.class };
			Object aobj[] = { proprFile };
			try {
				return (Properties) RemoteMethodServer.getDefault().invoke("getProperties", "ext.caep.util.IntegrationUtil", null, aclass, aobj);
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		}
		try {
			InputStream in = IntegrationUtil.class.getClassLoader().getResourceAsStream(proprFile);
			prop.load(in);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prop;
	}

	public static PDMLinkProduct getProduct() {
		if (product == null) {
			if (prop == null) {
				getProperties();
			}
			String productName = prop.getProperty("product", "\u5149\u5b66\u8bbe\u8ba1\u4ea7\u54c1\u5e93");
			System.out.println(productName);
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
		login(shareFileHost, shareFileHostUser, shareFileHostPassword);
		File file = null;
		file = new File("\\\\" + shareFileHost + "\\" + shareFilePath + "\\" + sharedFileName);
		System.out.println(file.getName());
		return file;
	}

	public static File createShareFile() throws Exception {
		login(shareFileHost, shareFileHostUser, shareFileHostPassword);
		String userName = SessionHelper.manager.getPrincipal().getName();
		File path = new File("\\\\" + shareFileHost + "\\" + shareFilePath + "\\" + userName);
		path.mkdirs();
		File file = new File(path, System.currentTimeMillis() + ".xml");
		file.createNewFile();
		return file;
	}

	public static File createShareFile(String fileName) throws Exception {
		login(shareFileHost, shareFileHostUser, shareFileHostPassword);
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
				System.out.println(data);
			}
			buf.close();
		} catch (IOException e) {
			System.out.println("login failed: " + e.getMessage());
			throw new Exception("Login file share host failed");
		}
	}

	/**
	 * 获取网上邻居中的一个目录列表
	 */
	public void listFiles() {
		String path = "\\\\10.0.113.158\\log";
		File file = new File(path);
		File[] files = file.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				System.out.println(" dir : " + f.getAbsolutePath());
			} else {
				System.out.println("file : " + f.getAbsolutePath());
			}
		}
	}

	public static Folder getFolder(String foldeName) {
		Folder result = null;
		Cabinet root = product.getDefaultCabinet();
		try {
			CabinetReference rootRef = CabinetReference.newCabinetReference(root);
			QueryResult qr = FolderHelper.service.findSubFolders(root);
			while (qr.hasMoreElements()) {
				SubFolder folder = (SubFolder) qr.nextElement();
				if (folder.getName().equals(foldeName)) {
					result = folder;
					break;
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean isAdmin() {
		boolean result = false;
		try {
			WTPrincipal principal = SessionHelper.manager.getPrincipal();
			WTRoleHolder2 roleHolder2 = TeamCCHelper.getTeamFromObject(product);
			HashMap map = TeamCCHelper.getMemberRoleHashMapFromTeam(roleHolder2);
			Iterator it = map.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Entry) it.next();
				WTPrincipalReference user = (WTPrincipalReference) entry.getKey();
				System.out.println(user.getName());
				ArrayList roles = (ArrayList) entry.getValue();
				for (Object o : roles) {
					if (principal.getName().equals(user.getName()) && o.toString().equalsIgnoreCase("PRODUCT MANAGER")) {
						return true;
					}
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean isMember() {
		boolean result = false;
		try {
			WTPrincipal principal = SessionHelper.manager.getPrincipal();
			WTRoleHolder2 roleHolder2 = TeamCCHelper.getTeamFromObject(product);
			HashMap map = TeamCCHelper.getMemberRoleHashMapFromTeam(roleHolder2);
			Iterator it = map.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Entry) it.next();
				WTPrincipalReference user = (WTPrincipalReference) entry.getKey();
				System.out.println(user.getName());
				ArrayList roles = (ArrayList) entry.getValue();
				for (Object o : roles) {
					if (principal.getName().equals(user.getName())) {
						return true;
					}
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}

		return result;
	}

	public static WTPart getPartFromNumber(String number) {
		WTPart part = null;
		QuerySpec qs;
		try {
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
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		return part;
	}

	public static boolean hasTask(WTPart project) {
		boolean result = false;
		try {
			QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(project);
			if (qr.hasMoreElements()) {
				return true;
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static WTSet getDescribeDoc(WTPart part) {
		WTSet docs = new WTHashSet();
		try {
			QueryResult qr = WTPartHelper.service.getDescribedByWTDocuments(part);
			WTArrayList docList = new WTArrayList(qr);
			Iterator it = docList.persistableIterator();
			while (it.hasNext()) {
				WTDocument doc = (WTDocument) it.next();
				docs.add(doc);
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return docs;
	}

	public static List<WTPart> getChildren(WTPart part) {
		List<WTPart> children = new ArrayList<WTPart>();
		try {
			QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(part);
			while (qr.hasMoreElements()) {
				WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
				QueryResult childrenQR = VersionControlHelper.service.allVersionsOf(link.getUses());
				if (childrenQR.hasMoreElements()) {
					children.add((WTPart) childrenQR.nextElement());
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return children;
	}

	public static List<WTPart> getChildrenForMe(WTPart part) {
		List<WTPart> children = new ArrayList<WTPart>();
		try {
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
		} catch (WTException e) {
			e.printStackTrace();
		}
		return children;
	}

	public static WTDocument getDocFromNumber(String number) {
		WTDocument doc = null;
		QuerySpec qs;
		try {
			qs = new QuerySpec(WTDocument.class);
			SearchCondition sc = new SearchCondition(WTDocument.class, WTDocument.NUMBER, SearchCondition.EQUAL, number.toUpperCase());
			qs.appendWhere(sc);
			QueryResult qr = PersistenceHelper.manager.find(qs);
			if (qr.hasMoreElements()) {
				doc = (WTDocument) qr.nextElement();
				QueryResult all = VersionControlHelper.service.allVersionsOf((Mastered) doc);
				if (all.hasMoreElements()) {
					doc = (WTDocument) all.nextElement();
					return doc;
				}
			}
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		return doc;
	}

	/**
	 * 在指定的光学产品中查询所有方案
	 * 
	 * @return
	 */
	public static List<WTPart> getAllProject() {
		List<WTPart> projects = new ArrayList<WTPart>();

		QuerySpec qs;
		try {
			qs = new QuerySpec();
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
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}

		return projects;
	}

	public static List<WTPart> filter(List<WTPart> parts) {
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

	public static String getType(Object obj) {
		String type = "";
		try {
			type = TypeIdentifierUtilityHelper.service.getTypeIdentifier(obj).getTypename();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		return type;
	}

	public static boolean isOwn(WTPart part) {
		boolean isOwn = false;
		try {
			isOwn = part.getCreator().getName().equalsIgnoreCase(SessionHelper.getPrincipal().getName());
		} catch (WTException e) {
			e.printStackTrace();
		}
		return isOwn;
	}

	public static boolean hasDescribePart(WTDocument doc) {
		boolean hasDescribePart = false;
		try {
			QueryResult qr = WTPartHelper.service.getDescribesWTParts(doc);
			if (qr.size() > 0) {
				hasDescribePart = true;
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return hasDescribePart;
	}

	public static Workable checkout(Workable workable) {
		Workable result = null;
		try {

			if (WorkInProgressHelper.isWorkingCopy(workable)) {
				result = workable;
			} else {
				CheckoutLink clink = WorkInProgressHelper.service.checkout(workable, WorkInProgressHelper.service.getCheckoutFolder(), "");
				result = clink.getWorkingCopy();
			}
		} catch (NonLatestCheckoutException e) {
			e.printStackTrace();
		} catch (WorkInProgressException e) {
			e.printStackTrace();
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		} catch (PersistenceException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static Workable checkin(Workable workable) {
		Workable result = workable;
		if (WorkInProgressHelper.isWorkingCopy(workable)) {
			try {
				result = WorkInProgressHelper.service.checkin(workable, "");
			} catch (WorkInProgressException e) {
				e.printStackTrace();
			} catch (WTPropertyVetoException e) {
				e.printStackTrace();
			} catch (PersistenceException e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static void updateName(WTDocument doc, String newName) {
		if (newName != null && !newName.equals("") && !doc.getName().equals(newName)) {
			doc = (WTDocument) IntegrationUtil.checkout(doc);
			WTDocumentMaster master = (WTDocumentMaster) doc.getMaster();
			WTDocumentMasterIdentity docMasterIdentity;
			try {
				docMasterIdentity = (WTDocumentMasterIdentity) master.getIdentificationObject();
				docMasterIdentity.setName(newName);
				IdentityHelper.service.changeIdentity(master, docMasterIdentity);
			} catch (WTException e) {
				e.printStackTrace();
			} catch (WTPropertyVetoException e) {
				e.printStackTrace();
			}
		}
	}

	public static void updateName(WTPart part, String newName) {
		if (newName != null && !newName.equals("") && !part.getName().equals(newName)) {
			part = (WTPart) IntegrationUtil.checkout(part);
			WTPartMaster master = (WTPartMaster) part.getMaster();
			WTPartMasterIdentity partMasterIdentity;
			try {
				partMasterIdentity = (WTPartMasterIdentity) master.getIdentificationObject();
				partMasterIdentity.setName(newName);
				IdentityHelper.service.changeIdentity(master, partMasterIdentity);
			} catch (WTException e) {
				e.printStackTrace();
			} catch (WTPropertyVetoException e) {
				e.printStackTrace();
			}
		}
	}
}
