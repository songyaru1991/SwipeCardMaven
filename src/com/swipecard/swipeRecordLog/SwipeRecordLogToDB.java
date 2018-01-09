package com.swipecard.swipeRecordLog;

import java.io.File;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.swipecard.util.DESUtils;
import com.swipecard.util.JsonFileUtil;
import com.swipecard.model.User;
import com.swipecard.SwipeCardNoDB;

public class SwipeRecordLogToDB {
	private static Logger logger = Logger.getLogger(SwipeRecordLogToDB.class);

	static SqlSessionFactory sqlSessionFactory;
	private static Reader reader;
	static Properties pps = new Properties();
	static Reader pr = null;
	static {
		try {
			pr = Resources.getResourceAsReader("db.properties");
			pps.load(pr);
			pps.setProperty("username", DESUtils.getDecryptString(pps.getProperty("username")));
			pps.setProperty("password", DESUtils.getDecryptString(pps.getProperty("password")));
			
			reader = Resources.getResourceAsReader("Configuration.xml");
			/*
			 * String filePath = System.getProperty("user.dir") +
			 * "/Configuration.xml"; FileReader reader = new
			 * FileReader(filePath);
			 */
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader,pps);
		} catch (Exception e) {
			logger.error("無DB刷卡數據回寫時異常，原因:"+e);
			e.printStackTrace();
		}
	}

	public static SqlSessionFactory getSession() {
		return sqlSessionFactory;
	}

	public static void main(String args[]) {
		SwipeRecordLogToDB();
	}

	static JsonFileUtil jsonFileUtil = new JsonFileUtil();

	public static void SwipeRecordLogToDB() {
		try {
			String filePath = "D:/SwipeCard/logs/SwipeCardRecordLogs/";
			File swipeCardLogfile = new File(filePath);
			File[] swipeCardLogList = swipeCardLogfile.listFiles();
			System.out.println("swipeCardLog目录下文件个数：" + swipeCardLogList.length);
			for (int i = 0; i < swipeCardLogList.length; i++) {
				if (swipeCardLogList[i].isFile()) {
					// 读取某个文件夹下的所有文件
					System.out.println("文件：" + swipeCardLogList[i]);
					File swipeCardRecordFile = swipeCardLogList[i];
					JSONObject swipeCardRecordJson = jsonFileUtil.getSwipeCardRecordByJson(swipeCardRecordFile);
					JSONArray swipeDataJsonArray;
					String workshopNo = "", cardID = "", swipeCardTime = "",ip = "";
					if (swipeCardRecordJson != null) {
						workshopNo = swipeCardRecordJson.getString("WorkshopNo");
						swipeDataJsonArray = swipeCardRecordJson.getJSONArray("SwipeData");
						if (swipeDataJsonArray.length() > 0) {
							for (int j = 0; j < swipeDataJsonArray.length(); j++) {
								JSONObject swipeCardData = swipeDataJsonArray.getJSONObject(j);
								cardID = swipeCardData.getString("CardID");
								swipeCardTime = swipeCardData.getString("swipeCardTime");
								ip = swipeCardData.getString("ip_address");
								System.out.println("WorkshopNo:" + workshopNo + ",CardID:" + cardID + ",swipeCardTime:"
										+ swipeCardTime);
								swipeCardlogToDB(workshopNo, cardID, swipeCardTime,ip);
								logger.info("回写刷卡记录:" + "WorkshopNo:" + workshopNo + ",CardID:" + cardID + ",swipeCardTime:"
										+ swipeCardTime + "成功");
							}
							jsonFileUtil.changeSwipeRecordFileName(swipeCardRecordFile);
						}
					}
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("刷卡記錄回寫失敗！原因:" + e);
		}

	}

		public static void swipeCardlogToDB(String WorkshopNo, String CardID, String swipeCardTime, String ip) {
			SqlSession session = sqlSessionFactory.openSession();
			try {
				// 通過卡號查詢員工個人信息
				// 1、判斷是否今天第一次刷卡
				Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(swipeCardTime);
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				String swipeDate = df.format(date);
				User eif = (User) session.selectOne("selectUserByCardID", CardID);
				
				//回寫刷卡資料至raw_record table中
				addRawSwipeRecord(session, eif, CardID,swipeCardTime,WorkshopNo,ip);			
				
			} catch (Exception ex) {
				logger.info("刷卡記錄回寫失敗！原因:" + ex);
				SwipeCardNoDB d = new SwipeCardNoDB(WorkshopNo);
				throw ExceptionFactory.wrapException("Error opening session.  Cause: " + ex, ex);
			} finally {
				ErrorContext.instance().reset();
				if (session != null) {
					session.close();
				}
			}
		}

		/*回寫刷卡資料至raw_record table中
		 * 
		 * */
		private static void addRawSwipeRecord(SqlSession session, User eif, String cardID,String swipeCardTime,String workshopNo, String ip) {
			String Id=null;
			try {
				if(eif!=null)
					Id=eif.getId();
				
				User newRawSwipeRecord=new User();
				newRawSwipeRecord.setCardID(cardID);
				newRawSwipeRecord.setId(Id);
				newRawSwipeRecord.setSwipeCardTime(swipeCardTime);
				newRawSwipeRecord.setRecord_status("7");
				newRawSwipeRecord.setIP_ADDRESS(ip);
				session.insert("addRawSwipeRecord", newRawSwipeRecord);
				session.commit();				
			}
			catch(Exception ex) {
				logger.error("無DB刷卡數據回寫時異常，原因:"+ex);
				ex.printStackTrace();
			}
		}
}
