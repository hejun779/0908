package ext.caep.integration.bean;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ext.caep.integration.util.Constant;
import ext.caep.integration.util.IntegrationUtil;
import ext.caep.integration.util.NumberingUtil;
import wt.iba.value.service.LoadValue;
import wt.part.LoadPart;
import wt.part.WTPart;

@XmlRootElement(name = "Project")
@XmlAccessorType(XmlAccessType.FIELD)
public class Project {
	@XmlAttribute
	private String name;
	@XmlAttribute(name = "ID")
	private String ID;// number
	@XmlAttribute(name = "state")
	private String state = "";
	@XmlAttribute(name = "type")
	private String type;// 构型
	@XmlAttribute(name = "describe")
	private String describe;
	@XmlElement(name = "Task")
	private List<Task> tasks;
	@XmlElement(name = "Files")
	private Files files;
	Vector return_objects = new Vector();
	Hashtable cmd_line = new Hashtable();
	Hashtable partAttrs = new Hashtable();
	boolean create = false;

	public Project() {
		super();
	}

	public Project(WTPart project) {
		this.name = project.getName();
		this.ID = project.getNumber();
		this.state = "";
		// TODO
		this.describe = "";
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Files getFiles() {
		return files;
	}

	public void setFiles(Files files) {
		this.files = files;
	}

	public WTPart newProject() throws Exception {
		if (this.ID == null || this.ID.equals("")) {
			create = true;
			String number = NumberingUtil.getNumber(null, this);
			this.ID = number;
			partAttrs.put("partNumber", number);
			partAttrs.put("partName", this.name);
			partAttrs.put("parentContainerPath", "/wt.inf.container.OrgContainer=ptc/wt.pdmlink.PDMLinkProduct=" + IntegrationUtil.getProperty("product"));
			partAttrs.put("type", "component");
			partAttrs.put("typedef", Constant.SOFTTYPE_PROJECT);
			partAttrs.put("source", "make");
			partAttrs.put("folder", "/Default/" + Constant.FOLDER_PROJECT);
			partAttrs.put("view", "Design");

			partAttrs.put("definition", Constant.ATTR_CAEP_GX);
			partAttrs.put("value1", this.type);
			LoadValue.createIBAValue(partAttrs, cmd_line, return_objects);

			partAttrs.put("definition", Constant.ATTR_DESCRIBE);
			partAttrs.put("value1", this.describe);
			LoadValue.createIBAValue(partAttrs, cmd_line, return_objects);

			LoadPart.beginCreateWTPart(partAttrs, cmd_line, return_objects);
			if (return_objects.size() > 0 && return_objects.get(0) instanceof WTPart) {
				return (WTPart) return_objects.get(0);
			}
		}
		return null;
	}

	public WTPart endProject() {
		if (create) {
			LoadPart.endCreateWTPart(partAttrs, cmd_line, return_objects);
			if (return_objects.size() > 0 && return_objects.get(0) instanceof WTPart) {
				return (WTPart) return_objects.get(0);
			}
		}
		return null;
	}
}
