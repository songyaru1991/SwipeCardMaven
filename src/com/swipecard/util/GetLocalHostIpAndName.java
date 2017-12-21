package com.swipecard.util;

import java.net.InetAddress;
import java.util.Properties;

public class GetLocalHostIpAndName {	
	     /* 获取本机的IP 
	     * @return Ip地址 
	     */ 
	
	
	     public static String getLocalHostIP() { 
	          String ip; 
	          try { 
	               /**返回本地主机。*/ 
	               InetAddress addr = InetAddress.getLocalHost(); 
	               ip = addr.getHostAddress();  
	          } catch(Exception ex) { 
	              ip = ""; 
	          } 
	            
	          return ip; 
	     } 
	       
	     /** 
	      * 获取本机主机名 
	      * @return 主机名
	      */ 
	     public static String getLocalHostName() { 
	          String hostName; 
	          try { 
	               InetAddress addr = InetAddress.getLocalHost(); 
	               hostName = addr.getHostName(); 
	          }catch(Exception ex){ 
	              hostName = ""; 
	          } 
	            
	          return hostName; 
	     } 
	    /* 当前用户名 */
	     public static String getLocalUserName()  {	    	 
	    	 String userName; 
	         try { 
	        	  Properties prop = System.getProperties();
	        	// 获取用户名
	        	  userName=prop.getProperty("user.name");
	 	         
	 	        // 获取操作系统
		         System.out.println("\n当前系统:" + prop.getProperty("os.name"));
	          }catch(Exception ex){ 
	        	  userName = ""; 
	          } 
	            
	          return userName; 
	     }
	     
	     public static void main(String[] args) {
	 		// TODO Auto-generated method stub
	 		 System.out.println("IP：" + getLocalHostIP()); 
	          System.out.println("NAME：" + getLocalHostName());
	          System.out.println("\n当前用户名:" + getLocalUserName());
	 	}
	 	

}
