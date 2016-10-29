package ext.caep.integration;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import com.ptc.core.meta.common.IdentifierFactory;
import com.ptc.core.meta.common.IllegalFormatException;
import com.ptc.core.meta.common.TypeIdentifier;

import ext.caep.integration.bean.File;
import ext.caep.integration.bean.Project;
import ext.caep.integration.bean.Software;
import ext.caep.integration.bean.Task;
import ext.caep.integration.util.Constant;
import ext.caep.integration.util.IntegrationUtil;
import ext.caep.integration.util.XMLUtil;
import wt.doc.WTDocument;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.collections.WTSet;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.inf.container.WTContainer;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartUsageLink;
import wt.pdmlink.PDMLinkProduct;
import wt.pds.StatementSpec;
import wt.pom.Transaction;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.services.ServiceProviderHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.VersionControlHelper;
import wt.vc.config.IteratedFolderedConfigSpec;

public class DataOperationHelper implements RemoteAccess {
	private static final String projectType = "wt.part.WTPart|com.ptc.project";
	private static final String documentType = "wt.doc.WTDocument|com.ptc.document";

	public static void main(String[] args) {
		crateProject(null);
	}

	public static void crateProject(Project bean) {
		Transaction ts = new Transaction();
		try {
			ts.start();
			PDMLinkProduct product = IntegrationUtil.getProduct();
			TypeDefinitionReference tdr = TypedUtilityServiceHelper.service.getTypeDefinitionReference(projectType);

			WTPart project = WTPart.newWTPart();
			project.setName(bean.getName());
			project.setTypeDefinitionReference(tdr);
			// TODO
			WTContainer container = product.getContainer();
			project.setDomainRef(container.getDefaultDomainReference());
			FolderHelper.assignLocation(project, IntegrationUtil.getFolder("方案任务结构树"));
			PersistenceHelper.manager.save(project);
			if (bean.getFiles() != null && bean.getFiles().getFiles().size() > 0) {
				for (File file : bean.getFiles().getFiles()) {
					WTDocument doc = createDocument(file, "方案任务结构树");
					WTPartDescribeLink link = WTPartDescribeLink.newWTPartDescribeLink(project, doc);
					PersistenceHelper.manager.save(link);
				}
			}
			ts.commit();
		} catch (Exception e) {
			ts.rollback();
		}

	}

	public static void deleteProject(String number) {
		if (IntegrationUtil.isAdmin()) {
			WTPart project = IntegrationUtil.getPartFromNumber(number);
			if (project != null) {
				if (IntegrationUtil.hasTask(project)) {
					// TODO
				} else {
					WTSet docs = IntegrationUtil.getDescribeDoc(project);
					Transaction ts = new Transaction();
					try {
						ts.start();
						PersistenceHelper.manager.delete(docs);
						PersistenceHelper.manager.delete(project);
						ts.commit();
					} catch (WTException e) {
						e.printStackTrace();
						ts.rollback();
					}
				}
			} else {

			}
		} else {
			// TODO
		}
	}

	public static void createTask(WTPart project, Task bean) {
		try {
			WTPart task = WTPart.newWTPart();
			task.setName(bean.getName());
			FolderHelper.assignLocation(task, IntegrationUtil.getFolder(Constant.FOLDER_PROJECT));
			PersistenceHelper.manager.save(task);
			WTPartUsageLink link = new WTPartUsageLink();
			link.setUses(task.getMaster());
			link.setUsedBy(project);
			PersistenceHelper.manager.save(link);
		} catch (WTException e) {
			e.printStackTrace();
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		}
	}

	public static void createSofeware(WTPart task, Software bean) {
		try {
			WTPart software = WTPart.newWTPart();
			software.setName(bean.getName());
			FolderHelper.assignLocation(software, IntegrationUtil.getFolder(software.getName() + "文件"));
			PersistenceHelper.manager.save(software);
			WTPartUsageLink link = new WTPartUsageLink();
			link.setUses(software.getMaster());
			link.setUsedBy(task);
			PersistenceHelper.manager.save(link);
		} catch (WTException e) {
			e.printStackTrace();
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		}
	}

	public static WTDocument createDocument(File bean, String folderName) {
		PDMLinkProduct product = IntegrationUtil.getProduct();
		WTDocument doc = null;
		try {
			doc = WTDocument.newWTDocument();
			doc.setName(bean.getName());
			doc.setDescription(bean.getDescribe());
			WTContainer container = product.getContainer();
			TypeDefinitionReference tdr = TypedUtilityServiceHelper.service.getTypeDefinitionReference(documentType);
			doc.setTypeDefinitionReference(tdr);
			doc.setDomainRef(container.getDefaultDomainReference());
			FolderHelper.assignLocation(doc, IntegrationUtil.getFolder(folderName));
		} catch (WTException e) {
			e.printStackTrace();
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return doc;
	}

	/**
	 * 同步数据
	 * 
	 * @param object
	 */
	public static String sycData(Object object) {
		String result = "";
		// 同步所有数据
		if (object == null) {
			try {
				QuerySpec qs = new QuerySpec();
				int partIndex = qs.addClassList(WTPart.class, true);
				// 搜索文件夹
				Folder folder = IntegrationUtil.getFolder(Constant.FOLDER_PROJECT);
				IteratedFolderedConfigSpec folder_cs = IteratedFolderedConfigSpec.newIteratedFolderedConfigSpec(folder);

				// 搜索方案软类型
				// qs.appendAnd();
				IdentifierFactory identifier_factory = (IdentifierFactory) ServiceProviderHelper.getService(IdentifierFactory.class, "logical");
				TypeIdentifier tid = (TypeIdentifier) identifier_factory.get("WCTYPE|wt.part.WTPart|com.ptc.Project");
				SearchCondition sc = TypedUtilityServiceHelper.service.getSearchCondition(tid, true);
				qs.appendWhere(sc, new int[] { partIndex });
				qs.appendAnd();
				// 搜索最新版本
				qs.appendWhere(VersionControlHelper.getSearchCondition(WTPart.class, true));

				qs = folder_cs.appendSearchCriteria(qs);

				QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);

				System.out.println(qr.size());

				XMLUtil xml = new XMLUtil(Constant.GLOBAL);
				xml.addAttr(xml.getRoot(), Constant.NAME, Constant.GLOBAL_NAME);
				xml.addAttr(xml.getRoot(), Constant.STATE, "");
				while (qr.hasMoreElements()) {
					Persistable[] ar = (Persistable[]) qr.nextElement();
					WTPart project = (WTPart) ar[partIndex];
					Map<String, String> projectAttrs = new HashMap<String, String>();
					projectAttrs.put(Constant.NAME, project.getName());
					projectAttrs.put(Constant.ID, project.getNumber());
					projectAttrs.put(Constant.STATE, "");
					projectAttrs.put(Constant.DESCRIBE, "");// TODO
					WTSet projectDocs = IntegrationUtil.getDescribeDoc(project);
					Iterator itDocs = projectDocs.persistableIterator();
					Element projectEl = xml.addEl(xml.getRoot(), Constant.PROJECT, null, projectAttrs);
					// 添加方案附属文档
					while (itDocs.hasNext()) {
						WTDocument doc = (WTDocument) itDocs.next();
						Map<String, String> docAttrs = new HashMap<String, String>();
						docAttrs.put(Constant.NAME, doc.getName());
						docAttrs.put(Constant.ID, doc.getNumber());
						docAttrs.put(Constant.STATE, "");
						docAttrs.put(Constant.DESCRIBE, doc.getDescription() == null ? "" : doc.getDescription());
						// TODO
						xml.addEl(projectEl, Constant.FILE, null, docAttrs);
					}
					// 添加计算任务
					List<WTPart> tasks = IntegrationUtil.getChildren(project);
					for (WTPart task : tasks) {
						Map<String, String> taskAttrs = new HashMap<String, String>();
						taskAttrs.put(Constant.NAME, task.getName());
						taskAttrs.put(Constant.ID, task.getNumber());
						taskAttrs.put(Constant.STATE, "");
						taskAttrs.put(Constant.DESCRIBE, "");// TODO
						Element taskEl = xml.addEl(projectEl, Constant.TASK, null, taskAttrs);
						// 添加专有软件
						List<WTPart> softwares = IntegrationUtil.getChildren(task);
						for (WTPart software : softwares) {
							Map<String, String> softwareAttrs = new HashMap<String, String>();
							softwareAttrs.put(Constant.NAME, software.getName());
							softwareAttrs.put(Constant.ID, software.getNumber());
							softwareAttrs.put(Constant.STATE, "");
							softwareAttrs.put(Constant.DESCRIBE, "");// TODO
							Element softwareEl = xml.addEl(taskEl, Constant.SOFTWARE, null, softwareAttrs);
							List<WTPart> params = IntegrationUtil.getChildren(software);
							for (WTPart param : params) {
								Map<String, String> paramAttrs = new HashMap<String, String>();
								paramAttrs.put(Constant.NAME, param.getName());
								paramAttrs.put(Constant.ID, param.getNumber());
								paramAttrs.put(Constant.STATE, "");
								paramAttrs.put(Constant.DESCRIBE, "");// TODO
								Element paramEl = xml.addEl(softwareEl, Constant.PARA, null, paramAttrs);
								WTSet files = IntegrationUtil.getDescribeDoc(param);
								Iterator filesIt = files.persistableIterator();
								while (filesIt.hasNext()) {
									WTDocument doc = (WTDocument) filesIt.next();
									Map<String, String> docAttrs = new HashMap<String, String>();
									docAttrs.put(Constant.NAME, doc.getName());
									docAttrs.put(Constant.ID, doc.getNumber());
									docAttrs.put(Constant.PATH, "");
									docAttrs.put(Constant.TYPE, "");// TODO
									docAttrs.put(Constant.STATE, "");
									docAttrs.put(Constant.DESCRIBE, "");// TODO
									xml.addEl(paramEl, Constant.FILE, null, docAttrs);
								}

							}
						}
					}
				}
				result = xml.outputFile(IntegrationUtil.createShareFile());
			} catch (QueryException e) {
				e.printStackTrace();
			} catch (IllegalFormatException e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			} catch (WTPropertyVetoException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
