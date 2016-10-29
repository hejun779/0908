package ext.caep.integration.bean;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class JaxbTest {

	public static void main(String[] args) {
		// String xml = "<global name=\"专用软件集成平台\" state=\"2\"><Project
		// name=\"test\" state=\"2\" ID=\"123\"><File name=\"方案模型.hsf\"
		// author=\"\" type=\"PMF\" describe=\"\" ID=\"\" organ=\"\"
		// state=\"\"></File></Project></global>";
		String xml = "<Project name=\"test\" state=\"2\" ID=\"123\"><File name=\"方案模型.hsf\" author=\"\" type=\"PMF\" describe=\"\" ID=\"\" organ=\"\" state=\"\"></File></Project>";
		try {
			JAXBContext context = JAXBContext.newInstance(Project.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			StringReader sr = new StringReader(xml);
			Project obj = (Project) unmarshaller.unmarshal(sr);

			System.out.println(obj instanceof Project);

		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	public static void marshall() {
		Global global = new Global();
		File file = new File();

	}

}
