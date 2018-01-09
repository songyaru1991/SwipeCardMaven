package com.swipecard;

import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Reader;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

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

public class SwipeCardLogin extends JFrame {
	private final static String CurrentVersion = "V20171127";//通訊的打包時不卡七休一，零組件的打包卡七休一
	private static Logger logger = Logger.getLogger(SwipeCardLogin.class);
	static JsonFileUtil jsonFileUtil = new JsonFileUtil();
	final String defaultWorkshopNo = jsonFileUtil.getSaveWorkshopNo();
	private static SqlSessionFactory sqlSessionFactory;
	private static Reader reader;
	static Properties pps = new Properties();
	static Reader pr = null;
	static {
		try {
			pr = Resources.getResourceAsReader("db.properties");
			pps.load(pr);
			pps.setProperty("username", DESUtils.getDecryptString(pps.getProperty("username")));
			pps.setProperty("password", DESUtils.getDecryptString(pps.getProperty("password")));
			
			// 读取内部配置文件
			reader = Resources.getResourceAsReader("Configuration.xml");

			/*
			 * // 读取系统外配置文件 (即Jar包外文件) --- 外部工程引用该Jar包时需要在工程下创建config目录存放配置文件
			 * String filePath = System.getProperty("user.dir") +
			 * "/Configuration.xml"; FileReader reader=new FileReader(filePath);
			 */
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader,pps);
			// System.out.println("sqlSessionFactory:"+sqlSessionFactory);
		} catch (Exception e) {
			logger.error("Login時 Error building SqlSession，原因:"+e);
			// e.printStackTrace();
			SwipeCardNoDB d = new SwipeCardNoDB(null);
			throw ExceptionFactory.wrapException("Error building SqlSession.", e);
		}
	}

	public static SqlSessionFactory getSession() {
		return sqlSessionFactory;
	}

	final Object[] WorkshopNo = getWorkshopNo();
	final Object[] LineLeader = getLineLeader();
	final JSONObject LineNoObject = getLineNoObject();
	Object[] lineno = null;
	private JPanel panel1;
	private JLabel label1, label2, label3;
	static JPasswordField text1;
	static JTextField jtf1, jtf3;
	private MyJButton but1;
	static JComboBox comboBox1,comboBox2;

	public SwipeCardLogin() {
		super("管理人員登陸-" + CurrentVersion);
		setBounds(212, 159, 600, 450);
		setResizable(true);

		Container c = getContentPane();
		panel1 = new JPanel();
		panel1.setLayout(null);
		label1 = new JLabel("實時工時系統", JLabel.CENTER);
		label2 = new JLabel("管理人員：");
		label3 = new JLabel("车间：");

		text1 = new JPasswordField("");
		// text1 = new JPasswordField();

		but1 = new MyJButton("確認 ", 2);

		comboBox1 = new JComboBox(WorkshopNo);
		comboBox1.setEditable(false);// 設置comboBox1為不可編輯，此時jtf1不生效
		comboBox1.setFont(new Font("微软雅黑", Font.PLAIN, 18));

		jtf1 = (JTextField) comboBox1.getEditor().getEditorComponent();
		
		if (defaultWorkshopNo != null) {
			comboBox1.setSelectedItem(defaultWorkshopNo);
		}

		label1.setBounds(150, 50, 300, 40);
		label1.setFont(new Font("微软雅黑", Font.BOLD, 40));
		but1.setFont(new Font("微软雅黑", Font.BOLD, 18));
		label2.setBounds(120, 200, 100, 30);
		text1.setBounds(220, 200, 160, 40);
		but1.setBounds(240, 340, 120, 40);

		label3.setBounds(120, 120, 100, 30);
		comboBox1.setBounds(220, 120, 160, 40);
		
		if(defaultWorkshopNo == null || defaultWorkshopNo.equals("")){
			panel1.add(label2);
			panel1.add(text1);
		}
		// 新增label
		JLabel label = new JLabel("線號：");
		label.setBounds(120, 276, 54, 24);
		panel1.add(label);
		// 新增下拉控件
		comboBox2 = new JComboBox();
		comboBox2.setBounds(220, 273, 160, 40);
		lineno = getLineno(comboBox1.getSelectedItem().toString());
		if (lineno != null) {
			for (Object object : lineno) {
				comboBox2.addItem(object);
			}
		} else {
			comboBox2.addItem("不需要選擇線號");
		}
		panel1.add(comboBox2);

		panel1.add(label1);
		panel1.add(label3);
		panel1.add(but1);
		panel1.add(comboBox1);
		c.add(panel1);
		but1.addActionListener(new TextFrame_jButton1_actionAdapter(this));
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		/*
		 * int fraWidth = this.getWidth();//frame的宽 int fraHeight =
		 * this.getHeight();//frame的高 Dimension screenSize =
		 * Toolkit.getDefaultToolkit().getScreenSize(); int screenWidth =
		 * screenSize.width; int screenHeight = screenSize.height;
		 * this.setSize(screenWidth, screenHeight); this.setLocation(0, 0);
		 * float proportionW = screenWidth/fraWidth; float proportionH =
		 * screenHeight/fraHeight; FrameShowUtil frameShow=new FrameShowUtil();
		 * frameShow.modifyComponentSize(this, proportionW,proportionH);
		 * this.toFront();
		 */
		/**
		 * @author
		 */
		comboBox1.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub

				if (e.getStateChange() == ItemEvent.SELECTED) {
					String key = jtf1.getText();
					lineno = getLineno(comboBox1.getSelectedItem().toString());
					comboBox2.removeAllItems();
					if(lineno != null){
						for (Object object : lineno) {
							comboBox2.addItem(object);
						}
					}else{
						comboBox2.addItem("不需要選擇線號");
					}
				}
			}
		});
	}
	
	private JSONObject getLineNoObject() {
		// TODO Auto-generated method stub
		List<User> user;
		JSONObject jsonObject = new JSONObject();
		try {
			SqlSession session = sqlSessionFactory.openSession();
			user = session.selectList("selectLineNoList");
			int con = user.size();
			if (con > 0) {
				for (int i = 0; i < con; i++) {
					jsonObject.put(user.get(i).getWorkshopNo(), user.get(i).getLineNo());
				}
				String fileName = "LineNo.json";
				jsonFileUtil.createWorkshopNoJsonFile(jsonObject.toString(), fileName);
			}
		} catch (Exception e) {
			System.out.println("Error opening session");
			logger.error("取得线体異常,原因" + e);
			dispose();
			SwipeCardNoDB d = new SwipeCardNoDB(defaultWorkshopNo);
			throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
		} finally {
			ErrorContext.instance().reset();
		}
		System.out.println(jsonObject);
		return jsonObject;
	}
	
	public Object[] getLineno(String selectWorkshopNo) {// TODO
		String linenoList;
		Object[] a = null;
		Object[] s = null;
		System.out.println(selectWorkshopNo);
		linenoList = LineNoObject.getString(selectWorkshopNo);
		System.out.println(linenoList);
		if (!(linenoList == null || linenoList.equals(""))) {
			s = linenoList.split(",");
			int con = s.length;
			a = new Object[con + 1];
			a[0] = "請選擇線號";
			for (int i = 1; i < con + 1; i++) {
				a[i] = s[i - 1];
			}
		}
		return a;
	}

	@SuppressWarnings("unchecked")
	public Object[] getWorkshopNo() {// TODO
		List<User> user;
		Object[] a = null;
		try {
			SqlSession session = sqlSessionFactory.openSession();
			JSONArray workshopNoArray = new JSONArray();
			user = session.selectList("selectWorkshopNo");
			int con = user.size();
			if (con > 0) {
				a = new Object[con + 1];
				a[0] = "--請選擇車間--";
				for (int i = 1; i < con + 1; i++) {
					a[i] = user.get(i - 1 ).getWorkshopNo();
					JSONObject workshopNoJson = new JSONObject();
					workshopNoJson.put("workshopNo", a[i].toString());
					workshopNoArray.put(workshopNoJson);
				}
				String fileName = "WorkshopNo.json";
				jsonFileUtil.createWorkshopNoJsonFile(workshopNoArray.toString(), fileName);
			} else {
				a = new Object[1];
				a[0] = "--請選擇車間--";
			}
		} catch (Exception e) {
			System.out.println("Error opening session");
			logger.error("取得車間異常,原因"+e);
			dispose();
			SwipeCardNoDB d = new SwipeCardNoDB(defaultWorkshopNo);
			throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
		} finally {
			ErrorContext.instance().reset();
		}
		return a;
	}

	@SuppressWarnings("rawtypes")
	public Object[] getLineLeader() {
		// TODO
		List<User> user;
		Object[] a = null;
		try {
			SqlSession session = sqlSessionFactory.openSession();
			user = session.selectList("selectUserByPermission");
			if (user.size() > 0) {
				a = new Object[user.size()];
				for (int i = 0; i < user.size(); i++) {
					a[i] = user.get(i).getCardID();
					// System.out.println("Admin: " + a[i]);
				}
			} else {
				a = new Object[1];
				a[0] = "";
			}
		} catch (Exception e) {
			System.out.println("Error opening session");
			logger.error("取得管理員卡號異常，原因:"+e);
			dispose();
			SwipeCardNoDB d = new SwipeCardNoDB(defaultWorkshopNo);
			throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
		} finally {
			ErrorContext.instance().reset();
		}
		return a;
	}

	public static void main(String args[]) {
		InitGlobalFont(new Font("微软雅黑", Font.BOLD, 18));
		SwipeCardLogin d = new SwipeCardLogin();

//		CheckCurrentVersion chkVersion = new CheckCurrentVersion(CurrentVersion);
//		Thread executeCheckVersion = new Thread(chkVersion);
//		executeCheckVersion.start();

	}

	private static void InitGlobalFont(Font font) {
		FontUIResource fontRes = new FontUIResource(font);
		for (Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements();) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource) {
				UIManager.put(key, fontRes);
			}
		}
	}
}

class TextFrame_jButton1_actionAdapter implements ActionListener {
	private SwipeCardLogin adaptee;

	private static SqlSessionFactory sqlSessionFactory;
	private static Reader reader;
	static {
		try {
			reader = Resources.getResourceAsReader("Configuration.xml");
			/*
			 * String filePath = System.getProperty("user.dir") +
			 * "/Configuration.xml"; FileReader reader=new FileReader(filePath);
			 */
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
		} catch (Exception e) {
			JsonFileUtil jsonFileUtil = new JsonFileUtil();
			final String defaultWorkshopNo = jsonFileUtil.getSaveWorkshopNo();
			SwipeCardNoDB d = new SwipeCardNoDB(defaultWorkshopNo);
			e.printStackTrace();
		}
	}

	public static SqlSessionFactory getSession() {
		return sqlSessionFactory;
	}

	public TextFrame_jButton1_actionAdapter(SwipeCardLogin adaptee) {
		this.adaptee = adaptee;
	}

	public static boolean isHave(Object[] a, String s) {
		/*
		 * 此方法有两个参数，第一个是要查找的字符串数组，第二个是要查找的字符或字符串
		 */
		for (int i = 0; i < a.length; i++) {
			// if (obj[i].indexOf(s) != -1) {// 循环查找字符串数组中的每个字符串中是否包含所有查找的内容
			if (a[i].equals(s)) {
				return true;// 查找到了就返回真，不在继续查询
			}
		}
		return false;// 没找到返回false
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */

	@Override
	public void actionPerformed(ActionEvent e) {
		if(adaptee.defaultWorkshopNo == null || adaptee.defaultWorkshopNo.equals("")){
			try {
				char[] pass = adaptee.text1.getPassword();
				String CardID = new String(pass);
				System.out.println("CardID: " + CardID);
				String pattern = "^[0-9]\\d{9}$";
				Pattern r = Pattern.compile(pattern, Pattern.DOTALL);
				Matcher m = r.matcher(CardID);
				// String WorkshopNo = SwipeCardLogin.jtf1.getText();
				String selectWorkShopNo = adaptee.comboBox1.getSelectedItem().toString();
				String selectLineNo = adaptee.comboBox2.getSelectedItem().toString();
				if (selectWorkShopNo.equals("--請選擇車間--")) {
					JOptionPane.showMessageDialog(adaptee, "請選擇車間!");
				} else {
					if(selectLineNo.equals("請選擇線號")){
						JOptionPane.showMessageDialog(adaptee, "請選擇線號!");
					}else{
						if(selectLineNo == "不需要選擇線號"){
							selectLineNo = null;
						}
					if (m.matches() == true) {
						Object[] a = adaptee.LineLeader;
						/*
						 * if(ShiftName.equals("日班")){ Shift="D"; }else
						 * if(ShiftName.equals("夜班")){ Shift="N"; }
						 */
						if (isHave(a, CardID)) {// 调用自己定义的函数isHave，如果包含则返回true,否则返回false
							System.out.println("成功登陸！");
							/*
							 * 将WorkShopNo保存到本地，以便下次自动进入相应的车间
							 */
							JsonFileUtil jsonFileUtil = new JsonFileUtil();
							String fileName = "saveSelectWorkshopNo.json";
							JSONObject selectWorkshopNoJson = new JSONObject();
							selectWorkshopNoJson.put("workshopNo", selectWorkShopNo);
							jsonFileUtil.saveSelectWorkshopNo(selectWorkshopNoJson.toString(), fileName);
							String lfileName = "saveLineNo.json";
							if(selectLineNo != null){
								JSONObject selectLineNoJson = new JSONObject();
								selectLineNoJson.put("lineNo", selectLineNo);
								jsonFileUtil.saveSelectWorkshopNo(selectLineNoJson.toString(), lfileName);
							}else{
								jsonFileUtil.deleteSaveLineNo(lfileName);
							}
							adaptee.dispose();
							SwipeCard swipe = new SwipeCard(selectWorkShopNo,selectLineNo);
							 System.out.println("WorkShopNo: " +
							 selectWorkShopNo);
						} else {
							JOptionPane.showMessageDialog(adaptee, "此卡無管理員權限");
							System.out.println("此管理员不存在");// 打印结果
						}
					} else {
						JOptionPane.showMessageDialog(adaptee, "不合法卡號");
						System.out.println("不合法卡號，含有非數字字符或卡號長度不正確");
					}
					}
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}else{
			try {
				/*char[] pass = adaptee.text1.getPassword();
				String CardID = new String(pass);
				System.out.println("CardID: " + CardID);
				String pattern = "^[0-9]\\d{9}$";
				Pattern r = Pattern.compile(pattern, Pattern.DOTALL);
				Matcher m = r.matcher(CardID);*/
				// String WorkshopNo = SwipeCardLogin.jtf1.getText();
				String selectWorkShopNo = adaptee.comboBox1.getSelectedItem().toString();
				String selectLineNo = adaptee.comboBox2.getSelectedItem().toString();
				if (selectWorkShopNo.equals("--請選擇車間--")) {
					JOptionPane.showMessageDialog(adaptee, "請選擇車間!");
				} else {
					if(selectLineNo.equals("請選擇線號")){
						JOptionPane.showMessageDialog(adaptee, "請選擇線號!");
					}else{
						if(selectLineNo == "不需要選擇線號"){
							selectLineNo = null;
						}
					/*if (m.matches() == true) {*/
//						Object[] a = adaptee.LineLeader;
						/*
						 * if(ShiftName.equals("日班")){ Shift="D"; }else
						 * if(ShiftName.equals("夜班")){ Shift="N"; }
						 */
						/*if (isHave(a, CardID)) {*/// 调用自己定义的函数isHave，如果包含则返回true,否则返回false
							System.out.println("成功登陸！");
							/*
							 * 将WorkShopNo保存到本地，以便下次自动进入相应的车间
							 */
							JsonFileUtil jsonFileUtil = new JsonFileUtil();
							String fileName = "saveSelectWorkshopNo.json";
							JSONObject selectWorkshopNoJson = new JSONObject();
							selectWorkshopNoJson.put("workshopNo", selectWorkShopNo);
							jsonFileUtil.saveSelectWorkshopNo(selectWorkshopNoJson.toString(), fileName);
							String lfileName = "saveLineNo.json";
							if(selectLineNo != null){
								JSONObject selectLineNoJson = new JSONObject();
								selectLineNoJson.put("lineNo", selectLineNo);
								jsonFileUtil.saveSelectWorkshopNo(selectLineNoJson.toString(), lfileName);
							}else{
								jsonFileUtil.deleteSaveLineNo(lfileName);
							}
							adaptee.dispose();
							SwipeCard swipe = new SwipeCard(selectWorkShopNo,selectLineNo);
							// System.out.println("WorkShopNo: " +
							// selectWorkShopNo);
						/*} else {
							JOptionPane.showMessageDialog(adaptee, "此卡無管理員權限");
							System.out.println("此管理员不存在");// 打印结果
						}*/
					/*} else {
						JOptionPane.showMessageDialog(adaptee, "不合法卡號");
						System.out.println("不合法卡號，含有非數字字符或卡號長度不正確");
					}*/
					}
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
