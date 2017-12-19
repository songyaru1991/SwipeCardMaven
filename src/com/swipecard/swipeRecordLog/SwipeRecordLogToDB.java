package com.swipecard.swipeRecordLog;

import java.awt.Color;
import java.io.Reader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

import com.swipecard.util.JsonFileUtil;
import com.swipecard.model.User;
import com.swipecard.DateGet;
import com.swipecard.SwipeCardNoDB;

public class SwipeRecordLogToDB {
	private static Logger logger = Logger.getLogger(SwipeRecordLogToDB.class);

	static SqlSessionFactory sqlSessionFactory;
	private static Reader reader;
	static {
		try {
			reader = Resources.getResourceAsReader("Configuration.xml");
			/*
			 * String filePath = System.getProperty("user.dir") +
			 * "/Configuration.xml"; FileReader reader = new
			 * FileReader(filePath);
			 */
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
		} catch (Exception e) {
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
		JSONObject swipeCardRecordJson = jsonFileUtil.getSwipeCardRecordByJson();
		JSONArray swipeDataJsonArray;
		String workshopNo = "", cardID = "", swipeCardTime = "";
		try {
			if (swipeCardRecordJson != null) {
				workshopNo = swipeCardRecordJson.getString("WorkshopNo");
				swipeDataJsonArray = swipeCardRecordJson.getJSONArray("SwipeData");
				if (swipeDataJsonArray.length() > 0) {
					for (int i = 0; i < swipeDataJsonArray.length(); i++) {
						JSONObject swipeCardData = swipeDataJsonArray.getJSONObject(i);
						cardID = swipeCardData.getString("CardID");
						swipeCardTime = swipeCardData.getString("swipeCardTime");
						System.out.println(
								"WorkshopNo:" + workshopNo + ",CardID:" + cardID + ",swipeCardTime:" + swipeCardTime);
						swipeCardlogToDB(workshopNo, cardID, swipeCardTime);
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("刷卡記錄回寫失敗！原因:" + e);
		}

	}

	public static void swipeCardlogToDB(String WorkshopNo, String CardID, String swipeCardTime) {
		SqlSession session = sqlSessionFactory.openSession();
		try {
			// 通過卡號查詢員工個人信息
			// 1、判斷是否今天第一次刷卡
			Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(swipeCardTime);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String swipeDate = df.format(date);
			User eif = (User) session.selectOne("selectUserByCardID", CardID);
			
			//只要刷卡都將記錄至raw_record table
			addRawSwipeRecord(session, eif, CardID,WorkshopNo);
			
			if (eif == null) {

				User selEmp = new User();
				selEmp.setCardID(CardID);
				selEmp.setSwipeDate(swipeDate);
				int lostRows =  session.selectOne("selectLoseEmployee", selEmp);
				if (lostRows > 0) {
					logger.info("已記錄當前" + CardID + "異常刷卡人員，今天不用再次刷卡！");
					return;
				}
				logger.info("當前" + CardID + "刷卡人員不存在；可能是新進人員，或是舊卡丟失補辦，人員資料暫時未更新，請線長記錄，協助助理走原有簽核流程！");

				User user1 = new User();
				user1.setCardID(CardID);
				user1.setWorkshopNo(WorkshopNo);
				user1.setSwipeDate(swipeDate);
				session.insert("insertUserByNoCard", user1);
				session.commit();

			} else {
				//判斷該卡號是否已連續工作六天
				if(!isUserContinuesWorkedOneWeek(session,eif,CardID,WorkshopNo,swipeCardTime)){					
					
					//該卡號是連續工作日小於六天
					
				String Id = eif.getId();
				String name = eif.getName();
				String RC_NO = "";
				String PRIMARY_ITEM_NO = "";
				 User curShiftUser = new User();
				 curShiftUser.setId(Id);
				 curShiftUser.setShiftDay(0);
				     
				 User yesShiftUser = new User();
				 yesShiftUser.setId(Id);
				 yesShiftUser.setShiftDay(1);
				    
				 int empCurShiftCount =  session.selectOne("getShiftCount", curShiftUser);
			     int empYesShiftCount =  session.selectOne("getShiftCount", yesShiftUser);
			     User empYesShift = (User) session.selectOne("getShiftByEmpId", yesShiftUser);
				String yesterdayShift = "";
				if (empYesShiftCount > 0) {
					String yesterdayClassDesc = empYesShift.getClass_desc();

					yesterdayShift = empYesShift.getShift();
					if (yesterdayShift.equals("N")) {

						if (empCurShiftCount == 0) {
							logger.info("ID:" + eif.getId() + " Name: " + eif.getName() + "班別有誤，請聯繫助理核對班別信息!");
						} else {
							User empCurShift = (User) session.selectOne("getShiftByEmpId", curShiftUser);

							String curShift = empCurShift.getShift();
							String curClassDesc = empCurShift.getClass_desc();
							Timestamp curClassStart = empCurShift.getClass_start();
							Timestamp curClassEnd = empCurShift.getClass_end();

							User userNSwipe = new User();
							String SwipeCardTime2 = swipeCardTime;
							userNSwipe.setSwipeCardTime2(SwipeCardTime2);
							userNSwipe.setCardID(CardID);
							userNSwipe.setId(Id);
							userNSwipe.setName(name);
							userNSwipe.setRC_NO(RC_NO);
							userNSwipe.setPRIMARY_ITEM_NO(PRIMARY_ITEM_NO);
							userNSwipe.setShift(yesterdayShift);
							userNSwipe.setWorkshopNo(WorkshopNo);

							if (curShift.equals("N")) {
								Date swipeTime = new Date();
								if (swipeTime.getHours() < 12) {
									int yesterdaygoWorkCardCount =  session.selectOne("selectCountNByCardID",
											userNSwipe);

									// 下班刷卡

									if (yesterdaygoWorkCardCount > 0) {
										int isOutWorkSwipeDuplicate =  session
												.selectOne("isOutWorkSwipeDuplicate", userNSwipe);
										if (isOutWorkSwipeDuplicate > 0) {
											// 下班重複刷卡
											outWorkSwipeDuplicate(session, eif, CardID, swipeCardTime, WorkshopNo,
													yesterdayShift);
										} else {
											logger.info("ID: " + eif.getId() + ",Name: " + eif.getName()
													+ ",今日上下班卡已刷,此次刷卡無效！");
										}
									} else if (yesterdaygoWorkCardCount == 0) {
										// 有可能為昨日上班卡有刷，今日下班卡沒刷 or
										// 昨日上班卡沒刷，今日下班卡也沒刷
										int goWorkNCardCount =  session.selectOne("selectGoWorkNByCardID",
												userNSwipe);// 取得該員工昨日到今日有上刷的筆數(有上刷)
										if (goWorkNCardCount == 0) {
											// 昨日無上刷
											int isOutWoakSwipeDuplicate =  session
													.selectOne("isOutWorkSwipeDuplicate", userNSwipe);// 取得該員工從10分鐘前至現在有無下刷記錄
											if (isOutWoakSwipeDuplicate > 0) {
												// 10分鐘前至現在有下刷記錄，進行重複刷卡處理
												outWorkSwipeDuplicate(session, eif, CardID, swipeCardTime, WorkshopNo,
														yesterdayShift);
											} else {
												// 10分鐘前至現在無下刷記錄
												int outWorkNCardCount =  session
														.selectOne("selectOutWorkByCardID", userNSwipe);// 從今天至明天該員工的刷卡記錄（無上刷，有下刷）

												if (outWorkNCardCount == 0) {
													// 無上刷也無下刷
													logger.info("下班刷卡,ID: " + eif.getId() + ",Name: " + eif.getName()
															+ ",刷卡時間： " + swipeCardTime + ",員工下班刷卡成功！");
													session.insert("insertOutWorkSwipeTime", userNSwipe);
												} else {
													// 無上刷有下刷
													logger.info("ID:" + eif.getId() + ",Name:" + eif.getName()
															+ ",今日上下班卡已刷，此次刷卡無效！");
												}
											}
										} else {
											// 昨日有上刷
											logger.info("下班刷卡," + "ID: " + eif.getId() + ",Name: " + eif.getName()
													+ ",刷卡時間： " + swipeCardTime + ",員工下班刷卡成功！");
											session.update("updateOutWorkNSwipeTime", userNSwipe);
										}
										session.commit();
									}

								} else {
									// 上班刷卡
									swipeCardRecord(session, eif, CardID, swipeCardTime, WorkshopNo);
								}
							} else {
								Timestamp yesClassStart = empYesShift.getClass_start();
								Timestamp yesClassEnd = empYesShift.getClass_end();
								Timestamp goWorkSwipeTime = new Timestamp(new Date().getTime());

								Calendar outWorkc = Calendar.getInstance();
								outWorkc.setTime(yesClassEnd);
								outWorkc.set(Calendar.HOUR_OF_DAY, outWorkc.get(Calendar.HOUR_OF_DAY) + 3);
								outWorkc.set(Calendar.MINUTE, outWorkc.get(Calendar.MINUTE) + 30);
								Date dt = outWorkc.getTime();
								Timestamp afterClassEnd = new Timestamp(dt.getTime());
								int goWorkNCardCount =  session.selectOne("selectGoWorkNByCardID", userNSwipe);
								if (goWorkNCardCount > 0) {
									// 昨日夜班已存在上刷
									int yesterdaygoWorkCardCount =  session.selectOne("selectCountNByCardID",
											userNSwipe);
									if (yesterdaygoWorkCardCount == 0) {
										// 夜班下刷刷卡記錄不存在
									

										if (goWorkSwipeTime.before(afterClassEnd)) {
											// 刷卡在夜班下班3.5小時之內,記為昨日夜班下刷
											logger.info("下班刷卡," + "ID: " + eif.getId() + ",Name:" + eif.getName()
													+ ",刷卡時間： " + swipeCardTime + ",員工下班刷卡成功！");
											session.update("updateOutWorkNSwipeTime", userNSwipe);
											session.commit();
										} else {
											// 刷卡在夜班下班3.5小時之后,記為今日白班上刷
											goOrOutWorkSwipeRecord(session, eif, CardID, swipeCardTime, WorkshopNo,
													curShift, curClassDesc,curClassStart);
										}
									} else {
										// 夜班下刷刷卡記錄已存在
										int isOutWoakSwipeDuplicate =  session
												.selectOne("isOutWorkSwipeDuplicate", userNSwipe);
										if (isOutWoakSwipeDuplicate > 0) {
											outWorkSwipeDuplicate(session, eif, CardID, swipeCardTime, WorkshopNo,
													yesterdayShift);
										} else {
											goOrOutWorkSwipeRecord(session, eif, CardID, swipeCardTime, WorkshopNo,
													curShift, curClassDesc,curClassStart);
										}
									}
								} else {

									// 昨日夜班上刷不存在														
								//	goOrOutWorkSwipeRecord(session, eif, CardID, curShift,
								//			curClassDesc,curClassStart);
						
									int yesterdaygoWorkCardCount =  session
											.selectOne("selectOutWorkByCardID", userNSwipe);
									if (yesterdaygoWorkCardCount == 0) {
										// 夜班下刷刷卡記錄不存在
										
										if (goWorkSwipeTime.before(afterClassEnd)) {
											// 刷卡在夜班下班3.5小時之內,記為昨日夜班下刷
											
											logger.info("下班刷卡:" + ",ID: " + eif.getId()
													+ "Name: " + eif.getName()  
													+",刷卡時間： "+ swipeCardTime  
													+",昨日班別為:"+yesterdayClassDesc
													+ ",員工下班刷卡成功！");
											session.insert("insertOutWorkSwipeTime",
													userNSwipe);
											session.commit();
										} else {
											// 刷卡在夜班下班3.5小時之后,記為今日白班上刷
											goOrOutWorkSwipeRecord(session, eif, CardID, swipeCardTime, WorkshopNo,
													curShift, curClassDesc,curClassStart);
					
										}
									} else {
										// 夜班下刷刷卡記錄已存在
										int isOutWoakSwipeDuplicate =  session
												.selectOne("isOutWorkSwipeDuplicate", userNSwipe);
										if (isOutWoakSwipeDuplicate > 0) {
											outWorkSwipeDuplicate(session, eif, CardID, swipeCardTime, WorkshopNo,
													yesterdayShift);
										} else {
											goOrOutWorkSwipeRecord(session, eif, CardID, swipeCardTime, WorkshopNo,
													curShift, curClassDesc,curClassStart);
										}
									}
								
									
								
								}
							}

						}
					} else {
						swipeCardRecord(session, eif, CardID, swipeCardTime, WorkshopNo);
					}
				} else {
					swipeCardRecord(session, eif, CardID, swipeCardTime, WorkshopNo);
				}
			}			
			else{
			//該卡號已連續工作六天，顯示錯誤訊息
				logger.info("工號："+eif.getId()+" 姓名："+eif.getName()+" 已連續上班七天，此次刷卡不列入記錄！!\n");				
			}
			}
		} catch (Exception ex) {
			logger.info("刷卡記錄回寫失敗！原因:" + ex);
			SwipeCardNoDB d = new SwipeCardNoDB(WorkshopNo);
			throw ExceptionFactory.wrapException("Error opening session.  Cause: " + ex, ex);
		} finally {
			ErrorContext.instance().reset();
			/*if (session != null) {
				session.close();
			}*/
		}
	}

	public static void swipeCardRecord(SqlSession session, User eif, String CardID, String swipeCardTime,
			String WorkshopNo) {

		String Id = eif.getId();
		 User curShiftUser = new User();
		  curShiftUser.setId(Id);
		  curShiftUser.setShiftDay(0);
		int empCurShiftCount =  session.selectOne("getShiftCount", curShiftUser);
		if (empCurShiftCount == 0) {

			logger.info("ID: " + eif.getId() + ",Name: " + eif.getName() + ",班別有誤，請聯繫助理核對班別信息!");
		} else {
			User empCurShift = (User) session.selectOne("getShiftByEmpId", curShiftUser);

			String curShift = empCurShift.getShift();
			String curClassDesc = empCurShift.getClass_desc();

			Timestamp curClassStart = empCurShift.getClass_start();
			Timestamp curClassEnd = empCurShift.getClass_end();
			Timestamp goWorkSwipeTime = new Timestamp(new Date().getTime());

			Calendar goWorkc = Calendar.getInstance();
			goWorkc.setTime(curClassStart);
			goWorkc.set(Calendar.HOUR_OF_DAY, goWorkc.get(Calendar.HOUR_OF_DAY) - 1);
			Date dt = goWorkc.getTime();
			Timestamp oneHBeforClassStart = new Timestamp(dt.getTime());

			Map<String, Object> param = new HashMap<String, Object>();
			param.put("CardID", CardID);
			param.put("Id", Id);
			param.put("WorkshopNo", WorkshopNo);
			param.put("Shift", curShift);
			param.put("SwipeCardTime", swipeCardTime);

			if (goWorkSwipeTime.after(oneHBeforClassStart) && goWorkSwipeTime.before(curClassStart)) {

				int isGoWorkSwipeDuplicate =  session.selectOne("isGoWorkSwipeDuplicate", param);
				if (isGoWorkSwipeDuplicate > 0) {

					goWorkSwipeDuplicate(session, eif, CardID, swipeCardTime, WorkshopNo, curShift);

				} else {
					goOrOutWorkSwipeRecord(session, eif, CardID, swipeCardTime, WorkshopNo, curShift, curClassDesc,curClassStart);
				}

			} else {

				if (curShift.equals("D")) {
					if (goWorkSwipeTime.after(curClassEnd)) {
						String name = eif.getName();
						String RC_NO = "";
						String PRIMARY_ITEM_NO = "";

						User userSwipe = new User();
						String SwipeCardTime2 = swipeCardTime;
						userSwipe.setSwipeCardTime2(SwipeCardTime2);
						userSwipe.setCardID(CardID);
						userSwipe.setId(Id);
						userSwipe.setName(name);
						userSwipe.setRC_NO(RC_NO);
						userSwipe.setPRIMARY_ITEM_NO(PRIMARY_ITEM_NO);
						userSwipe.setShift(curShift);
						userSwipe.setWorkshopNo(WorkshopNo);

						int curDayGoWorkCardCount =  session.selectOne("selectCountAByCardID", userSwipe);

						if (curDayGoWorkCardCount == 0) {

							int isOutWoakSwipeDuplicate =  session.selectOne("isOutWorkSwipeDuplicate",
									userSwipe);
							if (isOutWoakSwipeDuplicate > 0) {
								outWorkSwipeDuplicate(session, eif, CardID, swipeCardTime, WorkshopNo, curShift);
							} else {
								int outWorkCardCount =  session.selectOne("selectOutWorkByCardID", userSwipe);

								if (outWorkCardCount == 0) {

									logger.info("下班刷卡," + "ID: " + eif.getId() + ",Name: " + eif.getName() + ",刷卡時間： "
											+ swipeCardTime + ",員工下班刷卡成功！");
									session.insert("insertOutWorkSwipeTime", userSwipe);
									session.commit();
								} else {

									logger.info("ID: " + eif.getId() + ",Name: " + eif.getName() + ",今日上下班卡已刷，此次刷卡無效！");
								}
							}
						} else {
							outWorkSwipeCard(session, eif, CardID, swipeCardTime, WorkshopNo, curShift, curClassDesc);
						}
					} else {
						goOrOutWorkSwipeRecord(session, eif, CardID, swipeCardTime, WorkshopNo, curShift, curClassDesc,curClassStart);
					}
				} else {

					// 昨天日班，今天夜班刷卡

					if (goWorkSwipeTime.getHours() > 12) {// 刷卡在中午12點后為今日夜班上刷
						goOrOutWorkSwipeRecord(session, eif, CardID, swipeCardTime, WorkshopNo, curShift, curClassDesc,curClassStart);
					} else if (goWorkSwipeTime.getHours() <= 12) {// 刷卡在中午12點前

						logger.info("ID: " + eif.getId() + ",Name: " + eif.getName() + ",班別： " + curClassDesc
								+ ",刷卡時間： " + swipeCardTime + ",昨日班別非夜班，今日班別為夜班，請在夜班上班前刷上班卡");
					}

				}

			}

		}
	}

	public static void goOrOutWorkSwipeRecord(SqlSession session, User eif, String CardID, String swipeCardTime,
			String WorkshopNo, String curShift, String curClassDesc,Timestamp curClassStart) {
		String Id = eif.getId();

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("CardID", CardID);
		param.put("Id", Id);
		param.put("WorkshopNo", WorkshopNo);
		param.put("Shift", curShift);
		param.put("SwipeCardTime", swipeCardTime);

		int curDayGoWorkCardCount =  session.selectOne("selectCountAByCardID", param);
		// 無刷卡記錄
		if (curDayGoWorkCardCount == 0) {
			goWorkSwipeCard(session, eif, CardID, swipeCardTime, WorkshopNo, curShift, curClassDesc,curClassStart);

		} else if (curDayGoWorkCardCount > 0) {

			int isGoWorkSwipeDuplicate =  session.selectOne("isGoWorkSwipeDuplicate", param);
			if (isGoWorkSwipeDuplicate > 0) {
				goWorkSwipeDuplicate(session, eif, CardID, swipeCardTime, WorkshopNo, curShift);
			} else {
				// 下班刷卡
				outWorkSwipeCard(session, eif, CardID, swipeCardTime, WorkshopNo, curShift, curClassDesc);
			}
		}

	}

	public static void goWorkSwipeCard(SqlSession session, User eif, String CardID, String swipeCardTime,
			String WorkshopNo, String curShift, String curClassDesc,Timestamp curClassStart) {
		String Id = eif.getId();
		String name = eif.getName();
		String RC_NO = "";
		String PRIMARY_ITEM_NO = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date parsedDate = dateFormat.parse(swipeCardTime);
			Timestamp swipeTime = new java.sql.Timestamp(parsedDate.getTime());
			/*
			 * 原有逻辑 long
			 * diffMinutes=(swipeTime.getTime()-onDutyClassTime.getTime())/(60*
			 * 1000); if((diffMinutes>=0 && diffMinutes<=15)||diffMinutes>15){
			 */
			long diffMinutes = (curClassStart.getTime() - swipeTime.getTime()) / (60 * 1000);

			if (diffMinutes > 14) {				
				logger.info("上班刷卡\n" + "ID: " + eif.getId() + "\nName: " + eif.getName() + "\n班別： " + curClassDesc
						+ "\n刷卡時間： " + swipeCardTime + "\n" + "超出上班刷卡時間限制，請於上班前15分鐘刷卡！\n------------\n");
			} else {
				// 上刷時間介於班別15分鐘至班別起始時間，則進行記錄
				logger.info("上班刷卡," + "ID: " + Id + ",Name: " + eif.getName() + ",班別： " + curClassDesc + ",刷卡時間： "
						+ swipeCardTime + ",員工上班刷卡成功！");

				User user1 = new User();
				// String shift = "D";
				user1.setCardID(CardID);
				user1.setId(Id);
				user1.setName(name);
				user1.setSwipeCardTime(swipeCardTime);
				user1.setRC_NO(RC_NO);
				user1.setPRIMARY_ITEM_NO(PRIMARY_ITEM_NO);
				user1.setWorkshopNo(WorkshopNo);
				user1.setShift(curShift);
				session.insert("insertUserByOnDNShift", user1);
				session.commit();
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void outWorkSwipeCard(SqlSession session, User eif, String CardID, String swipeCardTime,
			String WorkshopNo, String Shift, String ClassDesc) {

		String Id=eif.getId();
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("CardID", CardID);
		param.put("Id", Id);
		param.put("WorkshopNo", WorkshopNo);
		param.put("Shift", Shift);
		param.put("SwipeCardTime2", swipeCardTime);

		int curDayOutWorkCardCount =  session.selectOne("selectCountBByCardID", param);

		if (curDayOutWorkCardCount > 0) {
			int isOutWoakSwipeDuplicate =  session.selectOne("isOutWorkSwipeDuplicate", param);
			if (isOutWoakSwipeDuplicate > 0) {

				outWorkSwipeDuplicate(session, eif, CardID, swipeCardTime, WorkshopNo, Shift);

			} else {
				logger.info("ID: " + eif.getId() + ",Name: " + eif.getName() + ",今日上下班卡已刷，此次刷卡無效！");
			}
		} else if (curDayOutWorkCardCount == 0) {

			logger.info("下班刷卡," + "ID: " + eif.getId() + ",Name: " + eif.getName() + ",刷卡時間： " + swipeCardTime + ",班別： "
					+ ClassDesc + ",員工下班刷卡成功！");
			User user1 = new User();
			user1.setSwipeCardTime2(swipeCardTime);
			user1.setCardID(CardID);
			user1.setId(Id);
			user1.setShift(Shift);
			user1.setWorkshopNo(WorkshopNo);
			session.update("updateOutWorkDSwipeTime", user1);
			session.commit();
		}
	}

	public static void goWorkSwipeDuplicate(SqlSession session, User eif, String CardID, String swipeCardTime,
			String WorkshopNo, String curShift) {
		String name = eif.getName();
		String Id = eif.getId();
		String RC_NO = "";
		String PRIMARY_ITEM_NO = "";

		logger.info("ID:" + Id + ",Name: " + name + ",上班重複刷卡！");

		User userSwipeDup = new User();

		userSwipeDup.setCardID(CardID);
		userSwipeDup.setName(name);
		userSwipeDup.setId(Id);
		userSwipeDup.setSwipeCardTime(swipeCardTime);
		userSwipeDup.setRC_NO(RC_NO);
		userSwipeDup.setPRIMARY_ITEM_NO(PRIMARY_ITEM_NO);
		userSwipeDup.setWorkshopNo(WorkshopNo);
		userSwipeDup.setShift(curShift);
		session.insert("goWorkSwipeDuplicate", userSwipeDup);
		session.commit();
	}

	public static void outWorkSwipeDuplicate(SqlSession session, User eif, String CardID, String swipeCardTime,
			String WorkshopNo, String curShift) {

		String swipeCardTime2 = swipeCardTime;

		String name = eif.getName();
		String Id = eif.getId();
		String RC_NO = "";
		String PRIMARY_ITEM_NO = "";

		logger.info("ID:" + Id + ",Name: " + name + ",下班重複刷卡！");

		User userSwipeDup = new User();
		userSwipeDup.setCardID(CardID);
		userSwipeDup.setName(name);
		userSwipeDup.setId(Id);
		userSwipeDup.setSwipeCardTime2(swipeCardTime2);
		userSwipeDup.setRC_NO(RC_NO);
		userSwipeDup.setPRIMARY_ITEM_NO(PRIMARY_ITEM_NO);
		userSwipeDup.setWorkshopNo(WorkshopNo);
		userSwipeDup.setShift(curShift);
		session.insert("outWorkSwipeDuplicate", userSwipeDup);
		session.commit();
	}
	
	/*當員工刷卡時，立即記錄一筆刷卡資料至raw_record table中
	 * 
	 * */
	private static void addRawSwipeRecord(SqlSession session, User eif, String CardID,String WorkshopNo) {
		String Id=null;
		try {
			String SwipeCardTime=DateGet.getTime();
			if(eif!=null)
				Id=eif.getId();
			User newRawSwipeRecord=new User();
			newRawSwipeRecord.setCardID(CardID);
			newRawSwipeRecord.setId(Id);
			newRawSwipeRecord.setSwipeCardTime(SwipeCardTime);
			session.insert("addRawSwipeRecord", newRawSwipeRecord);
			session.commit();
		}
		catch(Exception ex) {
			logger.error("刷卡記錄回寫進原始數據表異常,原因:"+ex);
			SwipeCardNoDB d = new SwipeCardNoDB(WorkshopNo);
			ex.printStackTrace();
		}
	}
	
	private static boolean isUserContinuesWorkedOneWeek(SqlSession session, User eif,String cardID,String WorkshopNo,String swipeCardTime){
		boolean isContinuesWorkForAWeek=false;
		try{
			//HashMap<String,Object> workDays=session.selectOne("getContinuesWorker",CardID);
			//System.out.println("員工工作日:"+(long)workDays.get("work_count_week"));
			//if((long)workDays.get("work_count_week")<6)
			
			//今天之前五天的记录，即:大于(new Date()-6 )  且小于new Date()
		//	int workDays=session.selectOne("getContinuesWorker",CardID);
			String Id=eif.getId();
			int workDays=0;
			User yesShiftUser = new User();
			yesShiftUser.setId(Id);
			yesShiftUser.setShiftDay(1);
			
			 User sixDayWorkerUser = new User();
			 sixDayWorkerUser.setId(Id);
			 sixDayWorkerUser.setShiftDay(7);
			 
			workDays=session.selectOne("getOneWeekWorkDays",sixDayWorkerUser);
			  
			int empYesShiftCount =  session.selectOne("getShiftCount", yesShiftUser);			
			/*   			  								
			//今天前六天的刷卡记录,即:new Date()-6
			 User agoSevenDayShiftUser = new User();
			 agoSevenDayShiftUser.setId(Id);
			 agoSevenDayShiftUser.setShiftDay(7);
			 
			 User sevenDayWorkerUser = new User();
			 sevenDayWorkerUser.setId(Id);
			 sevenDayWorkerUser.setShiftDay(7);
            
			int agoSevenDayShiftCount =  session.selectOne("getShiftCount", agoSevenDayShiftUser);
			if (agoSevenDayShiftCount > 0) {
				User empSevenDayAgoUSer = (User) session.selectOne("getShiftByEmpId", agoSevenDayShiftUser);
				String empSevenDayAgoShift=empSevenDayAgoUSer.getShift();
				if(empSevenDayAgoShift.equals("N")){
					//取得今天前，近五天的刷卡数量(包含new Date()-6 天夜班上下刷都完整的记录)
					 
					 User sixDayWorkerUser = new User();
					 sixDayWorkerUser.setId(Id);
					 sixDayWorkerUser.setShiftDay(6);
					 
					workDays=session.selectOne("getContinuesWorkDays",sixDayWorkerUser);
					
					//取得new Date()-6 天白班记录和夜班有上刷但下刷为null的记录
					int sixDayAgoRecord=session.selectOne("getDayRecord",sixDayWorkerUser);
					workDays+=sixDayAgoRecord;
				}
				else{
					//取得今天前，近六天的刷卡数量					
					workDays=session.selectOne("getContinuesWorkDays",sevenDayWorkerUser);
				}
			}else{
				//取得今天前，近六天的刷卡数量
				workDays=session.selectOne("getContinuesWorkDays",sevenDayWorkerUser);
			}						
			*/
			if(empYesShiftCount>0){
				User empYesUSer = (User) session.selectOne("getShiftByEmpId", yesShiftUser);
	            String empYesShift=empYesUSer.getShift();
				if(empYesShift.equals("N")){
					String SwipeCardTime2 = swipeCardTime;
					User userNSwipe = new User();
					userNSwipe.setSwipeCardTime2(SwipeCardTime2);
					userNSwipe.setCardID(cardID);
					userNSwipe.setId(Id);
					userNSwipe.setShift(empYesShift);
					userNSwipe.setWorkshopNo(WorkshopNo);
					int goWorkNCardCount =  session
							.selectOne("selectGoWorkNByCardID", userNSwipe);//昨日夜班上刷记录
									 
					int yesterdaygoWorkCardCount =  session
							.selectOne("selectCountNByCardID", userNSwipe);//昨日夜班下刷记录	
					
					if (goWorkNCardCount > 0) { 
						// 昨日夜班已存在上刷						
						if (yesterdaygoWorkCardCount==0) { //夜班下刷刷卡??不存在
							Timestamp yesClassStart = empYesUSer.getClass_start();
							Timestamp yesClassEnd = empYesUSer.getClass_end();
							Timestamp goWorkSwipeTime = new Timestamp(new Date().getTime());

							Calendar outWorkc = Calendar.getInstance();
							outWorkc.setTime(yesClassEnd);
							outWorkc.set(Calendar.HOUR_OF_DAY,
								outWorkc.get(Calendar.HOUR_OF_DAY) + 3);
							outWorkc.set(Calendar.MINUTE,
								outWorkc.get(Calendar.MINUTE) + 30);
							Date dt = outWorkc.getTime();
							Timestamp afterClassEnd = new Timestamp(dt.getTime());
							if (goWorkSwipeTime.before(afterClassEnd)) {
							// 刷卡在夜班下班3.5小時之內,?為昨日夜班下刷							
								workDays=workDays-1;
							}
							
						}else{
							
							int isOutWorkSwipeDuplicate =  session
									.selectOne("isOutWorkSwipeDuplicate", userNSwipe);
							if (isOutWorkSwipeDuplicate > 0) {
								outWorkSwipeDuplicate(session, eif, cardID, swipeCardTime,WorkshopNo,empYesShift);
								workDays=-1;
							}
						}
					}else{ // 昨日夜班不存在上刷	

						int outWorkNCardCount =  session
								.selectOne("selectOutWorkByCardID", userNSwipe);//夜班昨天无上刷，今天有下刷
									
						
						if(outWorkNCardCount>0){
							workDays=workDays+1;
						}
						int isOutWorkSwipeDuplicate =  session
								.selectOne("isOutWorkSwipeDuplicate", userNSwipe);
						if (isOutWorkSwipeDuplicate > 0) {
							outWorkSwipeDuplicate(session, eif, cardID, swipeCardTime,WorkshopNo,empYesShift);
							workDays=-1;
						}
					}
				}
			}
			
			System.out.println("員工工作日:"+workDays);
			if(workDays<6)
				isContinuesWorkForAWeek=false;
			else
				isContinuesWorkForAWeek=true;
		}
		catch(Exception ex){
			logger.error("七休一異常："+ex);
			SwipeCardNoDB d = new SwipeCardNoDB(WorkshopNo);
			ex.printStackTrace();
		}
		return isContinuesWorkForAWeek;
		}
	
}
