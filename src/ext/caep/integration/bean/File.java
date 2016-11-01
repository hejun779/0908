package ext.caep.integration.bean;

import java.util.Hashtable;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import ext.caep.integration.util.Constant;
import ext.caep.integration.util.IntegrationUtil;
import ext.caep.integration.util.NumberingUtil;
import wt.doc.LoadDoc;
import wt.doc.WTDocument;
import wt.iba.value.service.LoadValue;
import wt.part.LoadPart;
import wt.part.WTPart;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

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
		this.author = doc.getCreator().getName();
		this.describe = doc.getDescription() == null ? "" : doc.getDescription();
		this.type = "";// TODO
		this.state = "";
		// this.organ TODO
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

	public WTDocument newDocument(WTPart part) {

		WTDocument doc = null;
		// 借用文档,只创建连接
		if (this.ID != null && !this.ID.equals("")) {
			doc = IntegrationUtil.getDocFromNumber(this.getID());
		} else {
			try {
				doc = WTDocument.newWTDocument();
				doc.setName(this.name);
				if (this.describe != null) {
					doc.setDescription(this.describe);
				}

				// NumberingUtil.getNumber(doc);
			} catch (WTException e) {
				e.printStackTrace();
			} catch (WTPropertyVetoException e) {
				e.printStackTrace();
			}
		}

		return doc;
	}

	public WTDocument newDocument(String parentNumber, Object parent, String folder) throws Exception {
		if (this.ID == null || this.ID.equals("")) {
			String number = NumberingUtil.getNumber(parent, this);
			this.ID = number;
			docAttrs.put("name", this.name);
			docAttrs.put("number", this.ID);
			String saveIn = "/Default";
			if (folder != null && !folder.equals("")) {
				saveIn = saveIn + "/" + folder;
			}
			docAttrs.put("saveIn", saveIn);
			docAttrs.put("type", "Document");
			docAttrs.put("typedef", Constant.SOFTTYPE_FILE);
			docAttrs.put("parentContainerPath", "/wt.inf.container.OrgContainer=ptc/wt.pdmlink.PDMLinkProduct=" + IntegrationUtil.getProperty("product"));
			docAttrs.put("department", "DESIGN");
			docAttrs.put("primarycontenttype", "ApplicationData");
			docAttrs.put("path", this.path);

			LoadDoc.beginCreateWTDocument(docAttrs, cmd_line, return_objects);

			// 创建软属性
			docAttrs.put("definition", Constant.ATTR_CAEP_LXBS);
			docAttrs.put("value1", this.type);
			LoadValue.createIBAValue(docAttrs, cmd_line, return_objects);

			Hashtable organ = new Hashtable();
			docAttrs.put("definition", Constant.ATTR_CAEP_ORGAN);
			docAttrs.put("value1", this.organ);
			LoadValue.createIBAValue(docAttrs, cmd_line, return_objects);

			docAttrs.put("definition", Constant.ATTR_CAEP_AUTHOR);
			docAttrs.put("value1", this.author);
			LoadValue.createIBAValue(docAttrs, cmd_line, return_objects);

			LoadDoc.endCreateWTDocument(docAttrs, cmd_line, return_objects);

			if (parentNumber != null && !parentNumber.equals("")) {
				createPartDocLink(parentNumber);
			}

			if (return_objects.size() > 0 && return_objects.get(0) instanceof WTDocument) {
				return (WTDocument) return_objects.get(0);
			}
		}
		return null;
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
}
