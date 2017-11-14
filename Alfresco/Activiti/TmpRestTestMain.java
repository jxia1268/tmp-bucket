/**
 * 
 */
package tmp;


import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

/**
 *
 */
@SuppressWarnings("rawtypes")
public class TmpRestTestMain {

	private static final String URL_ID_USERS = "/identity/users";
	private static final String URL_RE_PROCESS_DEFINITIONS = "/repository/process-definitions";
	private static final String URL_RU_PROCESS_INSTANCE = "/runtime/process-instances";
	private static final String URL_RU_TASKS = "/runtime/tasks";

	private static String rootUrl = "http://localhost:8081/activiti-webapp-rest2/service";
	private RestTemplate restTemplate = new RestTemplate();//can be injected
	private String userId = "kermit";//for Basic Auth, not in use yet.
	private String userPw = "Kermit";//for Basic Auth, not in use yet.
	private String basicAuthPass;
	private String basicAuthPass4kermit = "Basic a2VybWl0Omtlcm1pdA==";
	
	private void execute() {
		String uniqueStr = createUniqueStrFromCurrentTime();//HHmmで一意文字列を生成する
		basicAuthPass = basicAuthPass4kermit;

		try {
			// ユーザ管理系
			referUsers();
			createUser("hoge" + uniqueStr);//Activitiのユーザテーブル登録時のPKとなる文字列を引数に指定する
			
			// プロセス制御系
			referProcessDefinitions();
//			startProcess("oneTaskProcess:1:35");
			referProcessInstances();
			referProcessInstance(40);
			
			// タスク制御
			referTasks();
			referTask(49);
			completeTasks(49);
			
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private void completeTasks(int taskInstanceId) {
		// TODO Auto-generated method stub
		
	}

	private void referTask(int taskInstanceId) throws URISyntaxException {
		System.out.println("★ SELECT A SPESIFIC TASK");
		execRestGetCall(URL_RU_TASKS + "/" + taskInstanceId);
	}

	/**
	 * 既存タスクの照会
	 * 
	 * @throws URISyntaxException 
	 */
	private void referTasks() throws URISyntaxException {
		System.out.println("★ SELECT ALL TASKS");
		execRestGetCall(URL_RU_TASKS);
	}

	private void referProcessInstance(int processInstanceId) throws URISyntaxException {
		System.out.println("★ SELECT A SPESIFIC PROCESS INSTANCE");
		execRestGetCall(URL_RU_PROCESS_INSTANCE + "/" + processInstanceId);
	}

	/**
	 * 既存プロセスインスタンスの生成
	 * 
	 * @throws URISyntaxException
	 */
	private void referProcessInstances() throws URISyntaxException {
		System.out.println("★ SELECT ALL PROCESS INSTANCES");
		execRestGetCall(URL_RU_PROCESS_INSTANCE);
	}

	/**
	 * 新規プロセスの開始
	 * 
	 * @param processDefinitionId
	 * @throws URISyntaxException 
	 */
	private void startProcess(String processDefinitionId) throws URISyntaxException {
		System.out.println("★ START A PROCESS");

		JSONObject reqBody = new JSONObject();
		reqBody.put("processDefinitionId", processDefinitionId);
		reqBody.put("businessKey", "myBusinessKey");
		JSONObject nestedVariables = new JSONObject();
		nestedVariables.put("name", "myVar");
		nestedVariables.put("value", "This is a variable");
		JSONArray itemArray = new JSONArray();
		itemArray.put(nestedVariables);
		reqBody.put("variables", itemArray);
		
		execRestPostCall(URL_RU_PROCESS_INSTANCE, reqBody);
	}

	/**
	 * 既存プロセス定義の照会
	 * 
	 * @throws URISyntaxException
	 */
	private void referProcessDefinitions() throws URISyntaxException {
		System.out.println("★ SELECT ALL FROM PROCESS_DEFINITION TABLE");
		execRestGetCall(URL_RE_PROCESS_DEFINITIONS);
	}

	/**
	 * 新規ユーザの登録
	 * 
	 * @param primaryChar
	 * @throws URISyntaxException
	 */
	private void createUser(String primaryChar) throws URISyntaxException {
		System.out.println("★ INSERT A RECORD INTO USER TABLE");

		JSONObject reqBody = new JSONObject();
		reqBody.put("id", primaryChar);
		reqBody.put("firstName", "Tijs");
		reqBody.put("lastName", "Barrez");
		reqBody.put("email", primaryChar + "@alfresco.org");
		reqBody.put("password", "pass123");

		execRestPostCall(URL_ID_USERS, reqBody);
	}

	private void createUser_bk(String primaryChar) throws URISyntaxException {
		System.out.println("★ INSERT A RECORD INTO USER TABLE");

		String requestPath = URL_ID_USERS;
		String str4basicAuth = getStr4basicAuth();

		String reqBody = "{"
				+ "\"id\":\"" + primaryChar + "\","
				+ "\"firstName\":\"Tijs\","
				+ "\"lastName\":\"Barrez\","
				+ "\"email\":\"" + primaryChar + "@alfresco.org\","
				+ "\"password\":\"pass123\""
				+ "}";

		execRestPostCall(URL_ID_USERS, reqBody);
	}

	/**
	 * 既存ユーザの照会
	 * 
	 * @throws URISyntaxException
	 */
	private void referUsers() throws URISyntaxException {
		System.out.println("★ SELECT ALL FROM USER TABLE");
		execRestGetCall(URL_ID_USERS);
	}

	private void execRestPostCall(String requestPath, JSONObject reqBody) throws URISyntaxException {
		System.out.println("REQUEST:");
		System.out.println(reqBody.toString(4));
		execRestPostCall(requestPath, reqBody.toString());
	}

	private void execRestPostCall(String requestPath, String reqBody) throws URISyntaxException {
		String str4basicAuth = getStr4basicAuth();

		// POST用の要求エンティティ
		RequestEntity reqEntity = RequestEntity
				.post(new URI(rootUrl + requestPath))
				.header("Authorization", "Basic a2VybWl0Omtlcm1pdA==")
//				.header("Authorization", str4basicAuth)//FIXME not sure why, doesnt work. needs the prefix??
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(reqBody);
		
		execRestCall(reqEntity);
	}

	private LinkedHashMap execRestGetCall(String requestPath) throws URISyntaxException {
		// GET用の要求エンティティ
		RequestEntity reqEntity = RequestEntity
				.get(new URI(rootUrl + requestPath))
				.header("Authorization", basicAuthPass)
				.accept(MediaType.APPLICATION_JSON)
				.build();
	
		return execRestCall(reqEntity);
	}

	/**
	 * リクエストエンティティに沿ってREST通信し、取得した結果をコンソール出力する。
	 * 
	 * @param reqEntity
	 * @throws URISyntaxException
	 */
	private LinkedHashMap execRestCall(RequestEntity reqEntity) throws URISyntaxException {
		
		ResponseEntity<Object> resEntity = restTemplate.exchange(reqEntity, Object.class);
		
		System.out.println("RESPONSE:");
		HttpStatus statusCode = resEntity.getStatusCode();
		System.out.println(statusCode);
		HttpHeaders headers = resEntity.getHeaders();
		System.out.println(headers);
		Object body = resEntity.getBody();
		System.out.println(body);
		System.out.println();
		
		if (body instanceof LinkedHashMap) {
			System.out.println("formatting..");
			Gson gson = new Gson();
			String inJson = gson.toJson(body, LinkedHashMap.class);
			JSONObject bodyInJson = new JSONObject(inJson);
			System.out.println(bodyInJson.toString(4));
			System.out.println();
			
			return (LinkedHashMap) body;
		}
		return null;
	}

	private String createUniqueStrFromCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
		Date date = new Date();
		return sdf.format(date);
	}

	/**
	 * Basic認証用の文字列の生成
	 * @return
	 */
	private String getStr4basicAuth() {
		byte[] bytes = (userId + ":" + userPw).getBytes();
		return new String(Base64.encodeBase64(bytes));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TmpRestTestMain ttm = new TmpRestTestMain();
		ttm.execute();

	}

}
