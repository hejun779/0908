package ext.caep.integration.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import ext.caep.integration.bean.Para;
import ext.caep.integration.bean.Project;
import ext.caep.integration.bean.Software;
import ext.caep.integration.bean.Task;
import wt.method.MethodContext;
import wt.pom.WTConnection;

public class NumberingUtil {

	public static String PROP_TYPEID = "typeId";
	public static String PROP_PROJECT = "Project";
	public static String PROP_GX = "GX";
	public static String PROP_TASK = "Task";
	public static String PROP_SOFTWARE = "Software";
	public static String PROP_PARAM = "Para";
	public static String PROP_FILE = "File";
	public static String PROP_FILE_TYPE = "FileType";
	public static String PROP_ISIOFILE = "isIOFile";

	/**
	 * 
	 * @param typeId
	 *            <br/>
	 *            必须是下列的这些字符串之一 <br/>
	 *            -- "Project" <br/>
	 *            -- "Task" <br/>
	 *            -- "Software" <br/>
	 *            -- "Para" <br/>
	 *            -- "File" <br/>
	 * @param props
	 *            <br/>
	 *            1) 当typeId 的值为 "Project" 时, <br/>
	 *            props 中需要包含下列的 key 值 <br/>
	 *            -- "GX" 必填项 <br/>
	 *            2) 当typeId 的值为 "Task" 时, <br/>
	 *            props 中需要包含如下的 key 值 <br/>
	 *            -- "Project" 必填项 <br/>
	 *            3) 当typeId 的值为 "File" 时, <br/>
	 *            props 中需要包含如下的 key 值 <br/>
	 *            -- "FileType" -- 必填项 <br/>
	 *            -- "Project" -- 与 "Task" 互斥, 只能选择其一 <br/>
	 *            -- "Task" -- 与 "Project" 互斥, 只能选择其一 <br/>
	 *            4) 其他情况下, 不需要此参数, 可以传入 null 值. <br/>
	 * @return 返回根据编号规则生成的编号
	 * @throws Exception
	 */
	private static String getNumber(Map<String, String> props) throws Exception {
		String number = null;
		String typeId = props.get(PROP_TYPEID);
		if (PROP_PROJECT.equalsIgnoreCase(typeId)) {
			number = generateProjectNumber(props);
		} else if (PROP_TASK.equalsIgnoreCase(typeId)) {
			number = generateTaskNumber(props);
		} else if (PROP_SOFTWARE.equalsIgnoreCase(typeId)) {
			number = generateSoftwareNumber();
		} else if (PROP_PARAM.equalsIgnoreCase(typeId)) {
			number = generateParamNumber();
		} else if (PROP_FILE.equalsIgnoreCase(typeId)) {
			number = generateFileNumber(props);
		}
		return number;
	}

	private static String generateProjectNumber(Map<String, String> props) throws Exception {
		String value = null;
		String value1 = props.get(PROP_GX);
		if (value1 != null && value1.length() > 0) {
			value = "P" + value1;
		} else {
			throw new Exception("Failed to get the Project GX Property");
		}
		/*
		 * long number = getSequenceNumber(PROP_PROJECT); number--; int value2 =
		 * (int) (number % 26); int value1 = (int) (number / 26); value = "P" +
		 * String.format("%02d", value1) + (char)(value2 + 65);
		 */
		return value;
	}

	/**
	 * 
	 * @param props
	 *            其中 props 中需要包含如下的Key <br/>
	 *            "Project" --必填项
	 * @return
	 * @throws Exception
	 */
	private static String generateTaskNumber(Map<String, String> props) throws Exception {
		String value = null;
		String value1 = props.get(PROP_PROJECT);
		long number = getSequenceNumber(value1);
		String value2 = String.format("%05d", (int) number);
		value1 = value1 + "-T" + value2;
		number = getSequenceNumber(PROP_TASK);
		String value3 = String.format("%05d", (int) number);
		value = value1 + "-" + value3;
		return value;
	}

	private static String generateSoftwareNumber() throws Exception {
		String value = null;
		long number = getSequenceNumber(PROP_SOFTWARE);
		value = "T" + String.format("%06d", (int) number);
		return value;
	}

	private static String generateParamNumber() throws Exception {
		String value = null;
		long number = getSequenceNumber(PROP_PARAM);
		value = "P" + String.format("%06d", (int) number);
		return value;
	}

	/**
	 * 
	 * @param props
	 *            其中 props 需要包含如下的key <br/>
	 *            "FileType" -- 必填项 <br/>
	 *            "Project" -- 与 "Task" 互斥, 只能选择其一 <br/>
	 *            "Task" -- 与 "Project" 互斥, 只能选择其一 <br/>
	 * @return 返回新生成的文档编号 <br/>
	 * @throws Exception
	 */
	private static String generateFileNumber(Map<String, String> props) throws Exception {
		String value = null;
		String value1 = props.get(PROP_PROJECT);
		String value2 = props.get(PROP_TASK);
		String value3 = props.get(PROP_FILE_TYPE);
		String isIOFile = props.get(PROP_ISIOFILE);
		if (value3 == null || value3.length() == 0) {
			throw new Exception("Failed to get the file type property");
		}
		if (value1 != null && value1.length() > 0) {
			value1 = value1 + "-" + value3;
			long number = getSequenceNumber(value1);
			value = value1 + "-" + String.format("%05d", (int) number);
		} else if (value2 != null && value2.length() > 0) {
			// 输入输出文件
			if (isIOFile != null && isIOFile.equals("true")) {
				int index = value2.lastIndexOf("-");
				if (index > -1) {
					value2 = value2.substring(0, index);
					value2 = value2 + "-" + value3;
					long number = getSequenceNumber(value2);
					value = value2 + "-" + String.format("%05d", (int) number);
				} else {
					throw new Exception("Failed to get the Separator from Task Id");
				}
				// 计算任务说明文件
			} else {
				int index = value2.indexOf("-");
				if (index > -1) {
					value2 = value2.substring(0, index);
					value2 = value2 + "-" + value3;
					long number = getSequenceNumber(value2);
					value = value2 + "-" + String.format("%05d", (int) number);
				} else {
					throw new Exception("Failed to get the Separator from Task Id");
				}
			}

		} else {
			throw new Exception("Failed to get the Project ID or Task ID");
		}
		return value;
	}

	private static long getSequenceNumber(String identifier) throws Exception {
		long amount = get(identifier);
		amount++;
		save(identifier, amount);
		return amount;
	}

	private static void save(String indentifier, long amount) throws Exception {
		// Transaction tr = new Transaction();
		try {
			// tr.start();
			MethodContext mc = MethodContext.getContext();
			WTConnection c = (WTConnection) mc.getConnection();
			PreparedStatement s = null;
			if (amount == 1) {
				s = c.prepareStatement("insert into sequencetbl(seqindex,amount) values ('" + indentifier + "'," + amount + ")");
			} else {
				s = c.prepareStatement("update sequencetbl set amount=" + amount + " where seqindex='" + indentifier + "'");
			}
			s.executeUpdate();
			s.close();

			// tr.commit();
			// mc.freeConnection();
			// tr = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static long get(String indentifier) {
		long amount = 0;
		try {
			MethodContext mc = MethodContext.getContext();
			WTConnection c = (WTConnection) mc.getConnection();
			PreparedStatement s = c.prepareStatement("select amount from sequencetbl where seqindex='" + indentifier + "'");
			ResultSet rs = s.executeQuery();
			if (rs.next()) {
				amount = rs.getLong(1);
			}
			rs.close();
			s.close();
			// mc.freeConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return amount;
	}

	/**
	 * 
	 * @param parent
	 *            为文件或任务产生number的时候需要
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public synchronized static String getNumber(Object parent, Object obj) throws Exception {
		Map<String, String> props = getMapForNumber(parent, obj);
		return getNumber(props);
	}

	public synchronized static String getFileNumber(Object parent, Object obj, boolean isIOFile) throws Exception {
		Map<String, String> props = getMapForNumber(parent, obj);
		props.put(PROP_ISIOFILE, String.valueOf(isIOFile));
		return getNumber(props);
	}

	private static Map<String, String> getMapForNumber(Object parent, Object obj) throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		if (obj instanceof Project) {
			Project project = (Project) obj;
			map.put("typeId", "Project");
			if (project.getType() != null && project.getType().length() > 0) {
				map.put("GX", project.getType());
			} else {
				throw new Exception("方案的构型属性不能为空");
			}
		} else if (obj instanceof Task) {
			map.put("typeId", "Task");
			Project project = (Project) parent;
			map.put("Project", project.getID());
		} else if (obj instanceof Software) {
			map.put("typeId", "Software");
		} else if (obj instanceof Para) {
			map.put("typeId", "Para");
		} else if (obj instanceof ext.caep.integration.bean.File) {
			ext.caep.integration.bean.File file = (ext.caep.integration.bean.File) obj;
			map.put("typeId", "File");
			if (file.getType() != null && file.getType().length() > 0) {
				map.put("FileType", file.getType());
			} else {
				throw new Exception("文档的类型标识不能为空");
			}
			if (parent instanceof Project) {
				Project project = (Project) parent;
				map.put("Project", project.getID());
			} else if (parent instanceof Task) {
				Task task = (Task) parent;
				map.put("Task", task.getID());
			}
		}
		return map;

	}
}
