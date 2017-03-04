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

@XmlRootElement(name = "Task")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Task {

	private String name;

	private String ID;

	private String state = "";

	private String describe;

	private List<Software> softwares;

	private Files files;

	Vector return_objects = new Vector();
	Hashtable cmd_line = new Hashtable();
	Hashtable partAttrs = new Hashtable();
	boolean create = false;

	public Task() {
		super();
	}

	public Task(WTPart task) {
		this.name = task.getName();
		this.ID = task.getNumber();
		this.state = "";
		IBAUtil iba = new IBAUtil(task);
		String describe = iba.getIBAValue(Constant.ATTR_DESCRIBE);
		this.describe = describe == null ? "" : describe;
	}

	@XmlAttribute(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute(name = "ID")
	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	@XmlAttribute(name = "describe")
	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	@XmlElement(name = "Software")
	public List<Software> getSoftwares() {
		return softwares;
	}

	public void setSoftwares(List<Software> softwares) {
		this.softwares = softwares;
	}

	@XmlElement(name = "Files")
	public Files getFiles() {
		return files;
	}

	public void setFiles(Files files) {
		this.files = files;
	}

	@XmlAttribute(name = "state")
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void newTask(Project project) throws Exception {
		if (this.ID == null || this.ID.equals("")) {
			this.create = true;
			String number = NumberingUtil.getNumber(project, this);
			this.ID = number;
			partAttrs.put("partNumber", number);
			partAttrs.put("partName", this.name);
			partAttrs.put("parentContainerPath", IntegrationUtil.getContainerPath());
			partAttrs.put("type", "component");
			partAttrs.put("typedef", Constant.SOFTTYPE_TASK);
			partAttrs.put("source", "make");
			partAttrs.put("folder", "/Default/" + Constant.FOLDER_PROJECT);
			partAttrs.put("view", "Design");

			LoadPart.beginCreateWTPart(partAttrs, cmd_line, return_objects);
			if (this.describe != null && this.describe.length() > 0) {
				partAttrs.put("definition", Constant.ATTR_DESCRIBE);
				partAttrs.put("value1", this.describe);
				LoadValue.createIBAValue(partAttrs, cmd_line, return_objects);
				// LoadPart.getPart(pNumber, pVersion, pIteration, pView);
				LoadValue.applySoftAttributes(LoadPart.getPart(number, null, null, null));
			}
			if (project != null && project.getID() != null && !project.getID().equals("")) {
				WTPart projectPart = IntegrationUtil.getPartFromNumber(project.getID());
				if (projectPart == null) {
					throw new Exception("ID为" + project.getID() + "的方案不存在");
				}
				Hashtable assmAttrs = new Hashtable();
				assmAttrs.put("assemblyPartNumber", project.getID());
				assmAttrs.put("constituentPartNumber", this.ID);
				assmAttrs.put("constituentPartQty", "1");
				assmAttrs.put("constituentPartUnit", "ea");
				LoadPart.addPartToAssembly(assmAttrs, cmd_line, return_objects);
			}
		}
	}

	public void endTask() {
		if (create) {
			LoadPart.endCreateWTPart(partAttrs, cmd_line, return_objects);
		}
	}

	public void removeSoftware(String softwareID) {
		if (softwareID != null && softwareID.length() > 0 && softwares != null && !softwares.isEmpty()) {
			for (Software software : softwares) {
				if (softwareID.equalsIgnoreCase(software.getID())) {
					int index = softwares.indexOf(software);
					softwares.set(index, null);
					break;
				}
			}
		}
	}

	public void removeFile(String fileID) {
		if (files != null) {
			this.removeFile(fileID);
		}
	}
}
