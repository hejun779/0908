package ext.caep.integration.bean;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Files")
@XmlAccessorType(XmlAccessType.FIELD)
public class Files {
	@XmlElement(name = "File")
	private List<File> files;// 附属文件或输入输出文件

	public Files() {
		super();
	}

	public List<File> getFiles() {
		return files;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}
}
