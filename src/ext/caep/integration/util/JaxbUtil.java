package ext.caep.integration.util;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import ext.caep.integration.bean.Files;
import ext.caep.integration.bean.Global;
import ext.caep.integration.bean.Para;
import ext.caep.integration.bean.Project;
import ext.caep.integration.bean.Software;
import ext.caep.integration.bean.Task;

public class JaxbUtil {

	/**
	 * 将XML文件的内容转换成Java对象
	 * 
	 * @param xmlFile
	 * @param root
	 * @return
	 * @throws Exception
	 */
	public static Object xml2Object(File xmlFile, Class rootCls) throws Exception {
		Object result = null;
		if (rootCls != null) {
			try {
				JAXBContext context = JAXBContext.newInstance(rootCls);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				result = unmarshaller.unmarshal(xmlFile);
			} catch (JAXBException e) {
				throw new Exception("输入XML文件格式错误");
			}
		}
		return result;
	}

	/**
	 * 将Java对象转换成XML,并输出到文件
	 * 
	 * @param obj
	 * @param output
	 * @throws Exception
	 */
	public static void object2xml(Object obj, File output) throws Exception {
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
			} else if (obj instanceof ext.caep.integration.bean.File) {
				cls = ext.caep.integration.bean.File.class;
			} else if (obj instanceof Files) {
				cls = Files.class;
			}
			try {
				JAXBContext context = JAXBContext.newInstance(cls);
				Marshaller marshaller = context.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshaller.marshal(obj, output);
			} catch (JAXBException e) {
				throw new Exception("输出XML转化异常");
			}
		}
	}
}
