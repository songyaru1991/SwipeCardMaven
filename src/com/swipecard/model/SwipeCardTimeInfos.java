package com.swipecard.model;

import java.sql.Date;
import java.sql.Timestamp;

public class SwipeCardTimeInfos {
	
	private String CardID;
	private String Name;
	private String SwipeDate;
	private String SwipeCardTime;
	private String SwipeCardTime2;
	private String CheckState;
	private String ProdLineCode;
	private String WorkshopNo;
	private String PrimaryItemNo;
	private String RCNO;
	private String Shift;
	
	public String getCardID() {
		return CardID;
	}
	public void setCardID(String cardID) {
		CardID = cardID;
	}
	
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	
	public String getSwipeCardTime() {
		return SwipeCardTime;
	}
	public void setSwipeCardTime(String swipeCardTime) {
		SwipeCardTime = swipeCardTime;
	}
	
	public String getSwipeCardTime2() {
		return SwipeCardTime2;
	}
	public void setSwipeCardTime2(String swipeCardTime2) {
		SwipeCardTime2 = swipeCardTime2;
	}
	
	public String getCheckState() {
		return CheckState;
	}
	public void setCheckState(String checkState) {
		CheckState = checkState;
	}
	public String getProdLineCode() {
		return ProdLineCode;
	}
	public void setProdLineCode(String prodLineCode) {
		ProdLineCode = prodLineCode;
	}
	public String getWorkshopNo() {
		return WorkshopNo;
	}
	public void setWorkshopNo(String workshopNo) {
		WorkshopNo = workshopNo;
	}
	public String getPrimaryItemNo() {
		return PrimaryItemNo;
	}
	public void setPrimaryItemNo(String primaryItemNo) {
		PrimaryItemNo = primaryItemNo;
	}
	public String getRCNO() {
		return RCNO;
	}
	public void setRCNO(String rCNO) {
		RCNO = rCNO;
	}
	public String getShift() {
		return Shift;
	}
	public void setShift(String shift) {
		Shift = shift;
	}
	public String getSwipeDate() {
		return SwipeDate;
	}
	public void setSwipeDate(String swipeDate) {
		SwipeDate = swipeDate;
	}
	

}
