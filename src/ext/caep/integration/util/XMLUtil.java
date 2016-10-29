package ext.caep.integration.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class XMLUtil {
	private Document doc;
	private Element root;

	public Element getRoot() {
		return root;
	}

	public void setRoot(Element root) {
		this.root = root;
	}

	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}

	public XMLUtil(String root) {
		this.root = new Element(root);
		this.doc = new Document(this.root);
	}

	public XMLUtil(File file) {
		SAXBuilder builder = new SAXBuilder();
		try {
			doc = builder.build(file);
			root = doc.getRootElement();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 在根节点添加新节点
	 * 
	 * @param elementContent
	 * @param attrs
	 * @return
	 */
	public Element add(String elementNode, String text, Map<String, String> attrs) {
		Element el = null;
		if (this.root != null) {
			el = new Element(elementNode);
			if (text != null) {
				el.setText(text);
			}
			if (attrs != null && !attrs.isEmpty()) {
				Iterator<Entry<String, String>> it = attrs.entrySet().iterator();
				List<Attribute> attributes = new ArrayList<Attribute>();
				while (it.hasNext()) {
					Entry<String, String> entry = it.next();
					Attribute attr = new Attribute(entry.getKey(), entry.getValue());
					attributes.add(attr);
				}
				el.setAttributes(attributes);
			}
			root.addContent(el);
		}
		return el;
	}

	/**
	 * 在指定节点添加新节点
	 * 
	 * @param parent
	 * @param elementNode
	 * @param text
	 * @param attrs
	 * @return
	 */
	public Element addEl(Element parent, String elementNode, String text, Map<String, String> attrs) {
		Element el = new Element(elementNode);
		if (text != null) {
			el.setText(text);
		}
		if (attrs != null && !attrs.isEmpty()) {
			Iterator<Entry<String, String>> it = attrs.entrySet().iterator();
			List<Attribute> attributes = new ArrayList<Attribute>();
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				Attribute attr = new Attribute(entry.getKey(), entry.getValue());
				attributes.add(attr);
			}
			el.setAttributes(attributes);
		}
		parent.addContent(el);
		return el;
	}

	/**
	 * 为指定的节点添加属性
	 * 
	 * @param el
	 * @param attrs
	 */
	public void addAttr(Element el, Map<String, String> attrs) {
		Iterator<Entry<String, String>> it = attrs.entrySet().iterator();
		List<Attribute> attributes = new ArrayList<Attribute>();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			Attribute attr = new Attribute(entry.getKey(), entry.getValue());
			attributes.add(attr);
		}
		List old = el.getAttributes();
		if (old != null) {
			old.addAll(attributes);
			el.setAttributes(old);
		} else {
			el.setAttributes(attributes);
		}
	}

	/**
	 * 为指定的节点添加属性
	 * 
	 * @param el
	 * @param attrs
	 */
	public void addAttr(Element el, String attName, String attValue) {
		List old = el.getAttributes();
		Attribute attr = new Attribute(attName, attValue);
		if (old != null) {
			old.add(attr);
			el.setAttributes(old);
		} else {
			el.setAttribute(attr);
		}
	}

	/**
	 * 输出XML为字符串
	 * 
	 * @return
	 */
	public String outputString() {
		String output = "";
		if (this.doc != null) {
			Format format = Format.getCompactFormat();
			format.setEncoding("UTF-8");
			format.setLineSeparator("\r\n");
			format.setIndent("    ");
			XMLOutputter out = new XMLOutputter(format);
			output = out.outputString(doc);
		}
		return output;
	}

	/**
	 * 输出Element为字符串
	 * 
	 * @return
	 */
	public String outputString(Element el) {
		String output = "";
		if (this.doc != null) {
			Format format = Format.getCompactFormat();
			format.setEncoding("UTF-8");
			format.setLineSeparator("\r\n");
			format.setIndent("    ");
			XMLOutputter out = new XMLOutputter(format);
			output = out.outputString(el);
		}
		return output;
	}

	/**
	 * 输出XML为文件
	 * 
	 * @return
	 */
	public String outputFile(File file) {
		String output = "";
		if (this.doc != null) {
			Format format = Format.getCompactFormat();
			format.setEncoding("UTF-8");
			format.setLineSeparator("\r\n");
			format.setIndent("    ");
			XMLOutputter out = new XMLOutputter(format);
			FileOutputStream ostream;
			try {
				ostream = new FileOutputStream(file);
				out.output(doc, ostream);
				ostream.close();
				System.out.println(file.getPath());
				output = file.getPath();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return output;
	}
}
