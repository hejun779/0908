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
import wt.part.LoadPart;
import wt.part.WTPart;

@XmlRootElement(name = "Para")
@XmlAccessorType(XmlAccessType.FIELD)
public class Para {
	@XmlAttribute(name = "name")
	private String name;
	@XmlAttribute(name = "ID")
	private String ID;
	@XmlAttribute(name = "state")
	private String state = "";
	@XmlElement(name = "File")
	private List<File> files;
	Vector return_objects = new Vector();
	Hashtable cmd_line = new Hashtable();
	Hashtable partAttrs = new Hashtable();
	boolean create = false;

	public Para() {
		super();
	}

	public Para(WTPart para) {
		this.name = para.getName();
		this.ID = para.getNumber();
		this.state = "";

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

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public List<File> getFiles() {
		return files;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}

	public void newPara(String parentNumber) throws Exception {
		if (this.ID == null || this.ID.equals("")) {
			this.create = true;
			String number = NumberingUtil.getNumber(null, this);// TODO
			this.ID = number;
			partAttrs.put("partNumber", number);
			partAttrs.put("partName", this.name);
			partAttrs.put("parentContainerPath", "/wt.inf.container.OrgContainer=ptc/wt.pdmlink.PDMLinkProduct=" + IntegrationUtil.getProperty("product"));
			partAttrs.put("type", "component");
			partAttrs.put("typedef", Constant.SOFTTYPE_PARA);
			partAttrs.put("source", "make");
			partAttrs.put("folder", "/Default/" + Constant.FOLDER_PROJECT);
			partAttrs.put("view", "Design");

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

	public void endPara() {
		if (this.create) {
			LoadPart.endCreateWTPart(partAttrs, cmd_line, return_objects);
		}

	}
}
