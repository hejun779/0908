package ext.caep.integration.bean;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

import ext.caep.integration.util.Constant;
import ext.caep.integration.util.IBAUtil;
import ext.caep.integration.util.IntegrationUtil;
import ext.caep.integration.util.NumberingUtil;
import wt.doc.LoadDoc;
import wt.doc.WTDocument;
import wt.iba.value.service.LoadValue;
import wt.part.LoadPart;

@XmlRootElement(name = "File")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class File {

	private String name;

	private String ID;

	private String path;

	private String type;// 类型标识

	private String author;

	private String describe;

	private String state;

	private String organ;
	Hashtable docAttrs = new Hashtable();
	Vector return_objects = new Vector();
	Hashtable cmd_line = new Hashtable();

	@XmlAttribute(name = "organ")
	public String getOrgan() {
		return organ;
	}

	public void setOrgan(String organ) {
		this.organ = organ;
	}

	public File() {
		super();
	}

	public File(WTDocument doc) {
		this.name = doc.getName();
		this.ID = doc.getNumber();
		this.path = "";
		this.describe = doc.getDescription() == null ? "" : doc.getDescription();
		IBAUtil iba = new IBAUtil(doc);
		String author = iba.getIBAValue(Constant.ATTR_CAEP_AUTHOR);
		String type = iba.getIBAValue(Constant.ATTR_CAEP_LXBS);
		String organ = iba.getIBAValue(Constant.ATTR_CAEP_ORGAN);
		this.author = author == null ? "" : author;
		this.type = type == null ? "" : type;
		this.organ = organ == null ? "" : organ;
		this.state = "";
	}

	@XmlAttribute(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute(name = "state")
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@XmlAttribute(name = "path")
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@XmlAttribute(name = "author")
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	@XmlAttribute(name = "describe")
	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	@XmlAttribute(name = "ID")
	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	@XmlAttribute(name = "type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void newDocument(String parentNumber, Object parent, String filePath, String folder) throws Exception {
		if (this.ID == null || this.ID.equals("")) {

			if (IntegrationUtil.isPara(parentNumber)) {
				docAttrs.put("typedef", Constant.SOFTTYPE_IOFILE);
				String number = NumberingUtil.getFileNumber(parent, this, true);
				this.ID = number;
			} else {
				docAttrs.put("typedef", Constant.SOFTTYPE_FILE);
				String number = NumberingUtil.getFileNumber(parent, this, false);
				this.ID = number;
			}

			docAttrs.put("name", this.name);
			docAttrs.put("number", this.ID);
			if (this.getDescribe() != null && this.getDescribe().length() > 0) {
				docAttrs.put("description", this.getDescribe());
			}
			String saveIn = "/Default";
			if (folder != null && !folder.equals("")) {
				saveIn = saveIn + "/" + folder;
			}
			docAttrs.put("saveIn", saveIn);
			docAttrs.put("type", "Document");

			docAttrs.put("parentContainerPath", IntegrationUtil.getContainerPath());
			docAttrs.put("department", "DESIGN");
			docAttrs.put("primarycontenttype", "ApplicationData");
			if (this.path != null && this.path.length() > 0) {
				String path = IntegrationUtil.getSharedFilePath(filePath + java.io.File.separator + this.path);
				docAttrs.put("path", path);
			}

			LoadDoc.beginCreateWTDocument(docAttrs, cmd_line, return_objects);

			// 创建软属性
			docAttrs.put("definition", Constant.ATTR_CAEP_LXBS);
			docAttrs.put("value1", this.type);
			LoadValue.createIBAValue(docAttrs, cmd_line, return_objects);

			if (this.organ != null && this.organ.length() > 0) {
				Hashtable organ = new Hashtable();
				docAttrs.put("definition", Constant.ATTR_CAEP_ORGAN);
				docAttrs.put("value1", this.organ);
				LoadValue.createIBAValue(docAttrs, cmd_line, return_objects);
			}
			if (this.author != null && this.author.length() > 0) {
				docAttrs.put("definition", Constant.ATTR_CAEP_AUTHOR);
				docAttrs.put("value1", this.author);
				LoadValue.createIBAValue(docAttrs, cmd_line, return_objects);
			}
			LoadDoc.endCreateWTDocument(docAttrs, cmd_line, return_objects);

			if (parentNumber != null && !parentNumber.equals("")) {
				createPartDocLink(parentNumber);
			}
			// this.path = "";
		}
	}

	/**
	 * 创建文档和部件的连接
	 * 
	 * @param parentNumber
	 */
	public void createPartDocLink(String parentNumber) {
		Hashtable linkAttrs = new Hashtable();
		linkAttrs.put("docNumber", this.ID);
		linkAttrs.put("partNumber", parentNumber);
		LoadPart.createPartDocDescribes(linkAttrs, cmd_line, return_objects);
	}

	/**
	 * 
	 * @param parameters
	 * @param filePath
	 *            username/timestamp
	 * @param hierarchyIndex
	 * @throws Exception
	 */
	public void download(Map<String, Object> parameters, String filePath, Object hierarchyIndex) throws Exception {
		WTDocument doc = IntegrationUtil.getDocFromNumber(this.ID);
		if (doc != null) {
			String path = IntegrationUtil.downloadFile(doc, parameters, filePath, hierarchyIndex);
			this.path = path;
			this.state = "";
			this.name = doc.getName();
			this.describe = doc.getDescription() == null ? "" : doc.getDescription();
			IBAUtil iba = new IBAUtil(doc);
			String organ = StringUtils.trimToEmpty(iba.getIBAValue(Constant.ATTR_CAEP_ORGAN));
			String type = StringUtils.trimToEmpty(iba.getIBAValue(Constant.ATTR_CAEP_LXBS));
			String author = StringUtils.trimToEmpty(iba.getIBAValue(Constant.ATTR_CAEP_AUTHOR));
			this.organ = organ;
			this.type = type;
			this.author = author;
		} else {
			throw new Exception("下载失败,ID为" + this.getID() + "的文档不存在");
		}

	}
}
