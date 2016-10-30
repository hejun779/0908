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

@XmlRootElement(name = "Task")
@XmlAccessorType(XmlAccessType.FIELD)
public class Task {
	@XmlAttribute(name = "name")
	private String name;
	@XmlAttribute(name = "ID")
	private String ID;
	@XmlAttribute(name = "state")
	private String state = "";

	@XmlAttribute(name = "describe")
	private String describe;
	@XmlElement(name = "Software")
	private List<Software> softwares;
	@XmlElement(name = "Files")
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
		// TODO
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public List<Software> getSoftwares() {
		return softwares;
	}

	public void setSoftwares(List<Software> softwares) {
		this.softwares = softwares;
	}

	public Files getFiles() {
		return files;
	}

	public void setFiles(Files files) {
		this.files = files;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void newTask(String parentNumber) {
		if (this.ID == null || this.ID.equals("")) {
			this.create = true;
			String number = NumberingUtil.getNumber(null, null);// TODO
			this.ID = number;
			partAttrs.put("partNumber", number);
			partAttrs.put("partName", this.name);
			partAttrs.put("parentContainerPath", "/wt.inf.container.OrgContainer=ptc/wt.pdmlink.PDMLinkProduct=" + IntegrationUtil.getProperty("product"));
			partAttrs.put("type", "component");
			partAttrs.put("typedef", Constant.SOFTTYPE_TASK);
			partAttrs.put("source", "make");
			partAttrs.put("folder", "/Default/" + Constant.FOLDER_PROJECT);
			partAttrs.put("view", "Design");

			partAttrs.put("definition", Constant.ATTR_DESCRIBE);
			partAttrs.put("value1", this.describe);
			LoadValue.createIBAValue(partAttrs, cmd_line, return_objects);

			LoadPart.beginCreateWTPart(partAttrs, cmd_line, return_objects);

			if (parentNumber != null && !parentNumber.equals("")) {
				Hashtable assmAttrs = new Hashtable();
				assmAttrs.put("assemblyPartNumber", parentNumber);
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
}
