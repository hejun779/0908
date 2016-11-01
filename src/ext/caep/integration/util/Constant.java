package ext.caep.integration.util;

public class Constant {
	// XML constant
	public static final String GLOBAL = "Global";
	public static final String GLOBAL_NAME = "专用软件集成平台";
	public static final String NAME = "name";
	public static final String ID = "ID";
	public static final String PROJECT = "Project";
	public static final String TASK = "Task";
	public static final String SOFTWARE = "Software";
	public static final String PARA = "Para";
	public static final String FILE = "File";
	public static final String STATE = "state";
	public static final String DESCRIBE = "describe";
	public static final String TYPE = "type";
	public static final String PATH = "path";
	public static final String FOLDER_PROJECT = "方案任务结构树";
	public static final String FOLDER_SG99 = "SG99文件";
	public static final String FOLDER_AMP2000 = "AMP2000文件";
	public static final String FOLDER_OTS = "OTS文件";
	public static final String FOLDER_3DView = "3DView文件";
	public static final String ROLE_MANAGER = "manager";
	public static final String ROLE_MEMBER = "member";
	public static final String ROLE_NOACCESS = "noaccess";

	// state状态
	public static final Object STATE_NOCHANGE = "";// 该对象不变
	public static final Object STATE_CREATE = "";// 对象为新增
	public static final Object STATE_DELETE = "0";// 该对象需删除
	public static final Object STATE_UPDATE = "1";// 该对象需编辑
	public static final Object STATE_SYNCHRONIZE = "2";// 该对象需要同步,同时同步所有子对象
	public static final Object STATE_DOWNLOAD = "3";// 需要下载文件内容,同时下载所有子对象的文档

	// 软类型
	public static final String SOFTTYPE_PROJECT = "lfrc.caep.Project";
	public static final String SOFTTYPE_TASK = "lfrc.caep.Task";
	public static final String SOFTTYPE_SOFTWARE = "lfrc.caep.Software";
	public static final String SOFTTYPE_PARA = "lfrc.caep.Para";
	public static final String SOFTTYPE_FILE = "lfrc.caep.File";

	// 软属性
	public static final String ATTR_CAEP_LXBS = "caep_lxbs";// 文档软属性类型标识
	public static final String ATTR_CAEP_AUTHOR = "caep_author";// 文档软属性作者
	public static final String ATTR_CAEP_ORGAN = "caep_organ";// 文档软属性部门
	public static final Object ATTR_CAEP_GX = "caep_gx";// 方案(部件)软属性构型
	public static final Object ATTR_DESCRIBE = "describe";// 方案/计算任务软属性构型
}
