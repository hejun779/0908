package ext.caep.integration.util;

import java.io.File;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.jdom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import ext.caep.integration.bean.Project;

public class TestJaxb {

	public static void main(String[] args) {
		JAXBContext context;
		try {
			XMLUtil xml = new XMLUtil(new File("D:\\1.xml"));
			Element el = (Element) xml.getRoot().getChildren().get(0);
			// xml.outputString();
			String xmlString = xml.outputString(el);
			context = JAXBContext.newInstance(Project.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			InputSource source = null;
			Node node = null;
			StringReader reader = new StringReader(xmlString);
			// Reader reader = new FileReader(new File("D:\\1.xml"));
			Object obj = unmarshaller.unmarshal(reader);
			System.out.println(obj);

		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

}
