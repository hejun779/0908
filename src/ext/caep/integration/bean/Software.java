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

@XmlRootElement(name = "Software")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Software {

	private String name;

	private String ID;

	private String state;

	private List<Para> paras;

	Vector return_objects = new Vector();
	Hashtable cmd_line = new Hashtable();
	Hashtable partAttrs = new Hashtable();
	boolean create = false;

	public Software() {
		super();
	}

	public Software(WTPart software) {
		this.name = software.getName();
		this.ID = software.getNumber();
		this.state = "";
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

	@XmlAttribute(name = "state")
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@XmlElement(name = "Para")
	public List<Para> getParas() {
		return paras;
	}

	public void setParas(List<Para> paras) {
		this.paras = paras;
	}

	public void newSoftware(String parentNumber) throws Exception {
		if (this.ID == null || this.ID.equals("")) {
			this.create = true;
			String number = NumberingUtil.getNumber(null, this);// TODO
			this.ID = number;
			partAttrs.put("partNumber", number);
			partAttrs.put("partName", this.name);
			partAttrs.put("parentContainerPath", "/wt.inf.container.OrgContainer=ptc/wt.pdmlink.PDMLinkProduct=" + IntegrationUtil.getProperty("product"));
			partAttrs.put("type", "component");
			partAttrs.put("typedef", Constant.SOFTTYPE_SOFTWARE);
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

	public void endSoftware() {
		if (this.create) {
			LoadPart.endCreateWTPart(partAttrs, cmd_line, return_objects);
		}

	}

}
