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
import ext.caep.integration.util.IBAUtil;
import ext.caep.integration.util.IntegrationUtil;
import ext.caep.integration.util.NumberingUtil;
import wt.iba.value.service.LoadValue;
import wt.part.LoadPart;
import wt.part.WTPart;

@XmlRootElement(name = "Project")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Project {

	private String name;

	private String ID;// number

	private String state = "";

	private String type;// 构型

	private String describe;

	private List<Task> tasks;

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
		IBAUtil iba = new IBAUtil(project);
		String describe = iba.getIBAValue(Constant.ATTR_DESCRIBE);
		String type = iba.getIBAValue(Constant.ATTR_CAEP_GX);
		this.describe = describe == null ? "" : describe;
		this.type = type == null ? "" : type;
	}

	@XmlAttribute(name = "ID")
	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	@XmlAttribute
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

	@XmlElement(name = "Task")
	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	@XmlAttribute(name = "describe")
	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	@XmlAttribute(name = "type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlElement(name = "Files")
	public Files getFiles() {
		return files;
	}

	public void setFiles(Files files) {
		this.files = files;
	}

	public void newProject() throws Exception {
		if (this.ID == null || this.ID.equals("")) {
			create = true;
			String number = NumberingUtil.getNumber(null, this);
			WTPart part = IntegrationUtil.getPartFromNumber(number);
			if (part != null) {
				throw new Exception("不能重复创建构型为" + this.type + "的方案.");
			}
			this.ID = number;
			partAttrs.put("partNumber", number);
			partAttrs.put("partName", this.name);
			partAttrs.put("parentContainerPath", IntegrationUtil.getContainerPath());
			partAttrs.put("type", "component");
			partAttrs.put("typedef", Constant.SOFTTYPE_PROJECT);
			partAttrs.put("source", "make");
			partAttrs.put("folder", "/Default/" + Constant.FOLDER_PROJECT);
			partAttrs.put("view", "Design");

			LoadPart.beginCreateWTPart(partAttrs, cmd_line, return_objects);

			partAttrs.put("definition", Constant.ATTR_CAEP_GX);
			partAttrs.put("value1", this.type);
			LoadValue.createIBAValue(partAttrs, cmd_line, return_objects);

			if (this.describe != null && this.describe.length() > 0) {
				partAttrs.put("definition", Constant.ATTR_DESCRIBE);
				partAttrs.put("value1", this.describe);
				LoadValue.createIBAValue(partAttrs, cmd_line, return_objects);
			}
			LoadValue.applySoftAttributes(LoadPart.getPart());
		}
	}

	public void endProject() {
		if (create) {
			LoadPart.endCreateWTPart(partAttrs, cmd_line, return_objects);
		}
	}

	public void removeTask(String taskID) {
		if (taskID != null && taskID.length() > 0 && tasks != null && !tasks.isEmpty()) {
			for (Task task : tasks) {
				if (taskID.equalsIgnoreCase(task.getID())) {
					int index = tasks.indexOf(task);
					tasks.set(index, null);
					break;
				}
			}
		}
	}

	public void removeFile(String fileID) {
		if (this.files != null) {
			files.removeFile(fileID);
		}
	}
}
