package ext.caep.integration.util;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jdom.Element;

import ext.caep.integration.bean.Global;
import ext.caep.integration.bean.Para;
import ext.caep.integration.bean.Project;
import ext.caep.integration.bean.Software;
import ext.caep.integration.bean.Task;

public class JaxbUtil {

	public static void main(String[] args) {

	}

	/**
	 * 将XML文件的内容转换成Java对象
	 * 
	 * @param xmlFile
	 * @param root
	 * @return
	 */
	public static Object xml2Object(File xmlFile, Element root) {
		Object result = null;
		if (root != null) {
			Class cls = null;
			if (root.getName().equalsIgnoreCase(Constant.GLOBAL)) {
				cls = Global.class;
			} else if (root.getName().equalsIgnoreCase(Constant.PROJECT)) {
				cls = Project.class;
			} else if (root.getName().equalsIgnoreCase(Constant.TASK)) {
				cls = Task.class;
			} else if (root.getName().equalsIgnoreCase(Constant.SOFTWARE)) {
				cls = Software.class;
			} else if (root.getName().equalsIgnoreCase(Constant.PARA)) {
				cls = Para.class;
			} else if (root.getName().equalsIgnoreCase(Constant.FILE)) {
				cls = ext.caep.integration.bean.File.class;
			}
			try {
				JAXBContext context = JAXBContext.newInstance(cls);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				result = unmarshaller.unmarshal(xmlFile);
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 将Java对象转换成XML,并输出到文件
	 * 
	 * @param obj
	 * @param output
	 */
	public static void object2xml(Object obj, File output) {
		if (obj != null) {
			Class cls = null;
			if (obj instanceof Global) {
				cls = Global.class;
			} else if (obj instanceof Project) {
				cls = Project.class;
			} else if (obj instanceof Task) {
				cls = Task.class;
			} else if (obj instanceof Software) {
				cls = Software.class;
			} else if (obj instanceof Para) {
				cls = Para.class;
			} else if (obj instanceof File) {
				cls = ext.caep.integration.bean.File.class;
			}
			try {
				JAXBContext context = JAXBContext.newInstance(cls);
				Marshaller marshaller = context.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshaller.marshal(obj, output);
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		}
	}
}
