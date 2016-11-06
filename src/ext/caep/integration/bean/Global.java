
package ext.caep.integration.bean;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Global")
@XmlAccessorType(XmlAccessType.FIELD)
public class Global {
	@XmlAttribute(name = "name")
	private String name;
	@XmlAttribute(name = "state")
	private String state = "";
	@XmlAttribute(name = "ID")
	private String ID = "";
	@XmlElement(name = "Project")
	private List<Project> projects;

	public Global() {
		super();
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

	public List<Project> getProjects() {
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

}
