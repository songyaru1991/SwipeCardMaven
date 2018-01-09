package com.swipecard;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.io.Reader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;

import com.swipecard.model.User;
import com.swipecard.util.DESUtils;
import com.swipecard.util.JsonFileUtil;


public class SwipeCard extends JFrame {
	private final static String CurrentVersion="V20171127";//FD通訊的打包時不卡七休一，零組件的卡七休一
	private static Logger logger = Logger.getLogger(SwipeCard.class);
	private static final Timer nowTime = new Timer();
	private Vector<Vector<Object>> rowData = new Vector<Vector<Object>>();
	private JTable table;
	private int count = 0;
	private String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private String time;
	private int ONE_SECOND = 1000;	

	static JTabbedPane tabbedPane;
	static JLabel label1, label3, swipeTimeLable, curTimeLable;
	static JLabel labelS1, labelS2, labelS3;
	static JPanel panel1, panel2, panel3;
	static ImageIcon image;
	static JLabel labelT2_1, labelT2_2, labelT2_3, labelT1_1,workShopNoJlabel, labelT1_3, labelT1_5, labelT1_6, label,linenoLabel,labelT1_4;
	static JComboBox comboBox, comboBox2;
	static MyJButton butT1_3, butT1_4, butT1_5, butT1_6, butT2_1, butT2_2, butT2_3, butT1_7, butT2_rcno;
	static JTextArea jtextT1_1, jtextT1_2;
	static TextField textT2_1, textT2_2, textT1_3, textT1_1, textT1_5, textT1_6;
	static JTextField jtf, jtf2;
	static JScrollPane jspT1_1, jspT2_2, JspTable, myScrollPane;
	// static Object[] str1 = getItems();
	static Object[] str1 = null;
	private MyNewTableModel myModel;
	private JTable mytable;
	Textc textc = null;
	static JsonFileUtil jsonFileUtil = new JsonFileUtil();
	final static String defaultWorkshopNo = jsonFileUtil.getSaveWorkshopNo();
	final static  String defaultLineNo = jsonFileUtil.getSaveLineNo();
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
			logger.error("Error opening session:"+e);
			
			SwipeCardNoDB d = new SwipeCardNoDB(defaultWorkshopNo);
			e.printStackTrace();
		}
	}

	public static SqlSessionFactory getSession() {
		return sqlSessionFactory;
	}

	/**
	 * Timer task to update the time display area
	 *
	 */
	protected class JLabelTimerTask extends TimerTask {
		@Override
		public void run() {
			SimpleDateFormat dateFormatter = new SimpleDateFormat(DEFAULT_TIME_FORMAT);
			time = dateFormatter.format(Calendar.getInstance().getTime());
			curTimeLable.setText(time);
		}
	}

	public SwipeCard(final String WorkshopNo, String LineNo) {

		super("產線端刷卡程式-"+CurrentVersion);
		
		setBounds(12, 84, 1000, 630);
		setResizable(true);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
	/*	
		int fraWidth = this.getWidth();//frame的宽  
        int fraHeight = this.getHeight();//frame的高  
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();  
        int screenWidth = screenSize.width;  
        int screenHeight = screenSize.height;  
        this.setSize(screenWidth, screenHeight);  
        this.setLocation(0, 0);  
        float proportionW = screenWidth/fraWidth;  
        float proportionH = screenHeight/fraHeight;  
        FrameShowUtil frameShow=new FrameShowUtil();
        frameShow.modifyComponentSize(this, proportionW,proportionH);  
        this.toFront();  
*/		
		Container c = getContentPane();
		tabbedPane = new JTabbedPane(JTabbedPane.LEFT); // 创建选项卡面板对象
		// 创建标签
		labelS1 = new JLabel("指示單號");
		labelS2 = new JLabel("料號");
		labelS3 = new JLabel("標準人數");

		panel1 = new JPanel();
		panel1.setLayout(null);
		panel2 = new JPanel();
		panel2.setLayout(null);
		panel3 = new JPanel();
		panel1.setBackground(Color.WHITE);
		panel2.setBackground(Color.WHITE);
		panel3.setBackground(Color.WHITE);

		labelT2_1 = new JLabel("班別：");// 指示單號

		str1 = getItems();
		if (str1 != null) {
			comboBox = new JComboBox(str1);
		} else {
			comboBox = new JComboBox();
		}

		comboBox.setEditable(true);

		comboBox.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		jtf = (JTextField) comboBox.getEditor().getEditorComponent();

		comboBox2 = new JComboBox();// getLineNoByWorkNo
		// comboBox2.addItem("");
		comboBox2.addItem("日班");
		comboBox2.addItem("夜班");
		comboBox2.setEditable(false);// 可編輯
		comboBox2.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		jtf2 = (JTextField) comboBox2.getEditor().getEditorComponent();

		textT1_1 = new TextField(15);// 車間
		textT1_1.setFont(new Font("微软雅黑", Font.PLAIN, 25));

		textT1_3 = new TextField(15);// 上班
		textT1_3.setFont(new Font("微软雅黑", Font.PLAIN, 25));

		jtextT1_1 = new JTextArea();// 刷卡人員信息,JTextArea(int rows, int columns)
		jtextT1_1.setBackground(Color.WHITE);
		jtextT1_2 = new JTextArea();// 備註
		textT2_1 = new TextField(15);// "料號"
		textT2_2 = new TextField(15);// "標準人數"

		// text3 = new JTextArea(2, 20);

		labelT1_1 = new JLabel("車間:");
		labelT1_1.setFont(new Font("微软雅黑", Font.BOLD, 25));
		
		workShopNoJlabel = new JLabel("車間:");
		workShopNoJlabel.setFont(new Font("微软雅黑", Font.BOLD, 25));		

		labelT1_3 = new JLabel("刷卡:");
		labelT1_3.setFont(new Font("微软雅黑", Font.BOLD, 25));

		labelT1_5 = new JLabel("實際人數:");
		labelT1_6 = new JLabel("備註:");
		labelT2_2 = new JLabel("指示單號:");
		labelT2_3 = new JLabel("標準人數:");

		curTimeLable = new JLabel();
		curTimeLable.setFont(new Font("微软雅黑", Font.BOLD, 35));

		swipeTimeLable = new JLabel();
		swipeTimeLable.setFont(new Font("微软雅黑", Font.BOLD, 35));

		// 未補充指示單號人員信息
		Vector<String> columnNames = new Vector<String>();
		columnNames.add("姓名");
		columnNames.add("刷卡時間1");
		columnNames.add("刷卡時間2");
		// columnNames.add("確認");
		table = new JTable(new DefaultTableModel(rowData, columnNames));
		JspTable = new JScrollPane(table);
		JspTable.setBounds(310, 40, 520, 400);

		Object ShiftName = comboBox2.getSelectedItem();
		// System.out.println("comboBox2"+ShiftName);
		String ShiftRcNo = "";
		if (ShiftName.equals("夜班")) {
			ShiftRcNo = "N";
		} else {
			ShiftRcNo = "D";
		}

		myModel = new MyNewTableModel(WorkshopNo, ShiftRcNo);
		mytable = new JTable(myModel);
		setTable();
		myScrollPane = new JScrollPane(mytable);
		myScrollPane.setBounds(310, 40, 520, 400);

		int x1 = 15, x2 = 100, x3 = 200, x4 = 400, x5 = 130, x6 = 460, x7 = 90;
		int y1 = 40, y4 = 180;

		labelT2_1.setBounds(x1, y1, x7, y1);
		labelT2_2.setBounds(x1, 2 * y1 + 10, x7, y1);
		comboBox2.setBounds(x1 + x7, y1, x3, y1); // 半夜班
		comboBox.setBounds(x1 + x7, 2 * y1 + 10, x3, y1);// 指示單號

		labelT2_3.setBounds(x1, 2 * y1 + 10, x7, y1);

		labelT1_1.setBounds(x1 + 20, y1, x7, y1);
		labelT1_3.setBounds(x1 + 20, 2 * y1 + 20, x7, y1);

		labelT1_6.setBounds(x1, 8 * y1, x7, y1);

		//textT1_1.setBounds(x1 + x7, 1 * y1, y4 + 100, y1);
		workShopNoJlabel.setBounds(x1 + x7, 1 * y1, y4 + 100, y1);
		textT1_3.setBounds(x1 + x7, 2 * y1 + 20, y4 + 100, y1);

		jtextT1_2.setBounds(x1 + x7, 9 * y1, x4, y1);

		textT2_1.setBounds(x1 + x7, 1 * y1, y4, y1);
		textT2_2.setBounds(x1 + x7, 2 * y1 + 10, y4, y1);

		swipeTimeLable.setBounds(400, y1, x4, 50);
		curTimeLable.setBounds(x1 + 10, 3 * y1 + 40, 400, 50);

		jspT1_1 = new JScrollPane(jtextT1_1);
		jspT1_1.setBounds(400, 2 * y1 + 20, x4, 250);

		jspT2_2 = new JScrollPane(jtextT1_2);
		jspT2_2.setBounds(x1, 9 * y1, x3 + x7, 150);
		int cc = 240;
		Color d = new Color(cc, cc, cc);// 这里可以设置颜色的rgb

		// 将标签面板加入到选项卡面板对象上
		tabbedPane.addTab("上下班刷卡界面", null, panel1, "First panel");
		tabbedPane.addTab("補充指示單號", null, panel2, "Second panel");
		tabbedPane.setSelectedIndex(0); // 设置默认选中的
		// tabbedPane.setEnabledAt(1,false);
		this.setVisible(true);

		textT1_1.setEditable(false);
		textT1_3.setEditable(true);
		// 使用swing的线程做獲取焦點的界面绘制，避免获取不到的情况。
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textT1_3.requestFocusInWindow();
			}
		});

		jtextT1_1.setEditable(false);
		jtextT1_2.setEditable(false);

		textT2_1.setEditable(false);
		textT2_2.setEditable(false);

		jtextT1_1.setLineWrap(true);
		jtextT1_2.setLineWrap(true);

		// textT1_3.setBackground(Color.GRAY);
		jtextT1_2.setBackground(d);

		butT1_5 = new MyJButton("更換車間", 2);
		butT1_6 = new MyJButton("退出程式", 2);
		// butT1_7 = new MyJButton("換線上班刷卡", 2);

		butT2_1 = new MyJButton("換料 ", 2);
		butT2_2 = new MyJButton("確認提交", 2);
		butT2_3 = new MyJButton("人員刷新", 2);
		butT2_rcno = new MyJButton("刷新指示單", 2);

		butT1_5.setBounds(497, 410, x5, y1);
		butT1_6.setBounds(651, 410, x5, y1);
		butT2_1.setBounds(x4, 400, x5, y1);
		butT2_3.setBounds(x6 + 60, 12 * y1, x5, y1);

		butT2_rcno.setBounds(x2, 3 * y1 + 30, 100, y1);
		butT2_2.setBounds(x2 + 110, 3 * y1 + 30, 90, y1);
		
		label = new JLabel("更換車間刷卡：");
		label.setFont(new Font("微软雅黑", Font.BOLD, 25));
		label.setBounds(35, 410, 177, 40);
		label.setVisible(false);
		panel1.add(label);
		
		textT1_6 = new TextField(15);// 管理员刷卡
		textT1_6.setFont(new Font("微软雅黑", Font.PLAIN, 25));
		textT1_6.setVisible(false);
		textT1_6.setBounds(214, 410, 263, 40);
		textT1_6.setEditable(false);
		panel1.add(textT1_6);
		//panel1.add(textT1_1);
		panel1.add(textT1_3);

		panel1.add(labelT1_1);
		panel1.add(workShopNoJlabel);
		
		panel1.add(labelT1_3);
		panel1.add(swipeTimeLable);
		panel1.add(curTimeLable);

		panel1.add(jspT1_1);
		// panel1.add(jspT2_2);

		// panel1.add(butT1_1);
		// panel1.add(butT1_2);
		// panel1.add(butT1_3);
		// panel1.add(butT1_4);
		panel1.add(butT1_5);
		panel1.add(butT1_6);
		// panel1.add(butT1_7);

		// panel2.add(butT2_1);
		panel2.add(butT2_2);

		panel2.add(butT2_3);
		panel2.add(butT2_rcno);

		panel2.add(labelT2_1);
		panel2.add(labelT2_2);
		// panel2.add(labelT2_3);
		panel2.add(comboBox);
		panel2.add(comboBox2);
		// panel2.add(textT2_1);
		// panel2.add(textT2_2);
		// panel2.add(JspTable);
		panel2.add(myScrollPane);
		
		Timer tmr = new Timer();
		tmr.scheduleAtFixedRate(new JLabelTimerTask(), new Date(), ONE_SECOND);

		// ItemListene取得用户选取的项目,ActionListener在JComboBox上自行输入完毕后按下[Enter]键,运作相对应的工作
		comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO

				if (e.getStateChange() == ItemEvent.SELECTED) {
					// System.out.println("-----------e.getItem():"+e.getStateChange()+"-------------");
					String RC_NO = jtf.getText();
					if (RC_NO.length() == 0) {
						textT2_1.setText("");
						textT2_2.setText("");
					} else {
						SqlSession session = sqlSessionFactory.openSession();
						try {
							User eif = (User) session.selectOne("selectUserByRCNo", RC_NO);
							if (eif != null) {
								textT2_1.setText(eif.getPRIMARY_ITEM_NO());
								textT2_2.setText(eif.getSTD_MAN_POWER());
							}

						} catch (Exception e1) {
							logger.error(e1);
							System.out.println("Error opening session");
							dispose();
							SwipeCardNoDB d = new SwipeCardNoDB(WorkshopNo);
							logger.error(e1);
							throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e1, e1);
						} finally {
							ErrorContext.instance().reset();
							if (session != null) {
								session.close();
							}
						}
					}

				}
			}
		});

		// TODO addKeyListener用于接收键盘事件（击键）的侦听器接口
		jtf.addKeyListener(new KeyListener() {

			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				String key = jtf.getText();
				comboBox.removeAllItems();
				// for (Object item : getItems()) {
				if (str1 != null) {
					for (Object item : str1) {
						// 可以把contains改成startsWith就是筛选以key开头的项目
						// contains(key)/startsWith(key)
						if (((String) item).startsWith(key)) {
							comboBox.addItem(item);
						}
					}
				}
				jtf.setText(key);
			}

			public void keyPressed(KeyEvent e) {
			}
		});

		//更換車間或更換上下班刷卡
		butT1_5.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String bt = butT1_5.getText();
				if (bt.equals("更換車間")) {
					butT1_5.setText("刷卡上下班");
					jtextT1_1.setText("請管理員刷卡");
					jtextT1_1.setBackground(Color.WHITE);
					textT1_3.setEditable(false);
					textT1_6.setEditable(true);
					textT1_6.setVisible(true);
					label.setVisible(true);
					// 使用swing的线程做獲取焦點的界面绘制，避免获取不到的情况。
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							textT1_6.requestFocusInWindow();
						}
					});
				} else {
					butT1_5.setText("更換車間");
					jtextT1_1.setText("刷卡上下班");
					jtextT1_1.setBackground(Color.WHITE);
					textT1_3.setEditable(true);
					textT1_6.setEditable(false);
					textT1_6.setVisible(false);
					label.setVisible(false);
					// 使用swing的线程做獲取焦點的界面绘制，避免获取不到的情况。
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							textT1_3.requestFocusInWindow();
						}
					});
				}
			}
		});
		
		/*
		 * l 刷管理员的卡选择车间
		 */
		textT1_6.addTextListener(new TextListener() {

			@Override
			public void textValueChanged(TextEvent e) {
				SqlSession session = sqlSessionFactory.openSession();
				// TODO Auto-generated method stub
				String cardID = textT1_6.getText();
				if (cardID.length() > 10) {
					jtextT1_1.setBackground(Color.RED);
					jtextT1_1.setText("卡號輸入有誤，請再次刷卡\n");
					textT1_6.setText("");
				} else {
					String pattern = "^[0-9]\\d{9}$";
					Pattern r = Pattern.compile(pattern, Pattern.DOTALL);
					Matcher m = r.matcher(cardID);
					if (m.matches() == true) {
						boolean admin = IsAdminByCardID(cardID, session);
						if (admin) {
							dispose();
							SwipeCardLogin swipeCardLogin = new SwipeCardLogin();
							textT1_6.setText("");
						} else {
							jtextT1_1.setBackground(Color.RED);
							jtextT1_1.setText("您的卡权限不够\n请刷管理员的卡");
							textT1_6.setText("");
						}
					} else {
						System.out.println("無輸入內容或輸入錯誤!");
					}
				}

			}
		});

		butT1_6.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO
				System.exit(0);
			}
		});

		butT2_1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// 中途刷卡原因
				jtf.setEditable(true);
			}
		});

		butT2_2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int countRow = mytable.getRowCount();

				Boolean State = null;
				User user1 = new User();
			//	String WorkshopNo = textT1_1.getText();
				String WorkshopNo = workShopNoJlabel.getText();
				String RC_NO = jtf.getText();
				String PRIMARY_ITEM_NO = textT2_1.getText();
				String Name = "", empID = "";
				try {
					SqlSession session = sqlSessionFactory.openSession();
					StringBuilder strBuilder = new StringBuilder();
					for (int i = 0; i < RC_NO.length(); i++) {
						char charAt = RC_NO.charAt(i);
						if (charAt == ' ')
							continue;
						strBuilder.append(charAt);
					}
					RC_NO = strBuilder.toString();

					if (!RC_NO.equals("") && RC_NO != "" && RC_NO != null) {
						// user1.setPROD_LINE_CODE(LineNo);
						user1.setWorkshopNo(WorkshopNo);
						user1.setRC_NO(RC_NO);
						user1.setPRIMARY_ITEM_NO(PRIMARY_ITEM_NO);
						boolean isaddItem = false;
						str1 = getItems();
						if (str1 != null) {
							for (Object item : str1) {
								if (((String) item).equals(RC_NO)) {
									isaddItem = false;
									// JOptionPane.showMessageDialog(null,"工單號已存在!",
									// "提示",JOptionPane.WARNING_MESSAGE);
									break;
								} else {
									isaddItem = true;
								}
							}
						}
						if (isaddItem) {
							synchronized (this){
							session.insert("insertRCInfo", user1);
							}
							session.commit();
						}
						for (int i = 0; i < countRow; i++) {
							State = (Boolean) mytable.getValueAt(i, 0);
							if (State == true) {
								empID = (String) mytable.getValueAt(i, 2);
								Name = (String) mytable.getValueAt(i, 3);
								user1.setId(empID);
								user1.setName(Name);

								session.update("Update_rcno_ByLineNOandCardID", user1);
								session.commit();
								System.out.println("Name: " + Name);
							}
						}
					} else {
						JOptionPane.showMessageDialog(null, "指示單號不得為空!", "提示", JOptionPane.WARNING_MESSAGE);
					}

					panel2.remove(myScrollPane);
					// myModel = new MyNewTableModel(LineNo, "D");
					myModel = new MyNewTableModel(WorkshopNo, "D");
					mytable = new JTable(myModel);
					setTable();
					myScrollPane = new JScrollPane(mytable);
					myScrollPane.setBounds(310, 40, 520, 400);
					panel2.add(myScrollPane);
					panel2.updateUI(); // 重绘
					panel2.repaint(); // 重绘此组件。
					// System.out.println("State!"+ mytable.getColumnClass(0));
				} catch (Exception e1) {					
					System.out.println("Error opening session");
					logger.error("綁定指示單號失敗,原因:"+e1);
					dispose();
					SwipeCardNoDB d = new SwipeCardNoDB(WorkshopNo);
					throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e1, e1);
				} finally {
					ErrorContext.instance().reset();
				}
			}
		});

		butT2_3.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				update();
			}
		});

		butT2_rcno.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				str1 = getItems();
			}
		});

		// TODO 刷卡模式
		textT1_3.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if(e.getKeyChar() == KeyEvent.VK_ENTER){
				SqlSession session = sqlSessionFactory.openSession();

				String CardID = textT1_3.getText();

				// text1.setText("");

				String swipeCardTime = DateGet.getTime();
				//String WorkshopNo = textT1_1.getText();
				String WorkshopNo = workShopNoJlabel.getText();
				// 驗證是否為10位整數，是則繼續執行，否則提示
				// System.out.println(m.matches());
				if (CardID.length() > 10) {
					jtextT1_1.setBackground(Color.RED);
					jtextT1_1.setText("卡號輸入有誤，請再次刷卡\n");
					textT1_3.setText("");
				} else if(CardID.length()<10){
					jtextT1_1.setBackground(Color.RED);
					jtextT1_1.setText("無效卡號！\n");
					textT1_3.setText("");
				} else {
					String pattern = "^[0-9]\\d{9}$";
					Pattern r = Pattern.compile(pattern, Pattern.DOTALL);
					Matcher m = r.matcher(CardID);
					if (m.matches() == true) {
						try {
							// 通過卡號查詢員工個人信息
							// 1、判斷是否今天第一次刷卡
							// System.out.println("getRowsa: " +
							// rows.getRowsa());
							swipeTimeLable.setText(swipeCardTime);
							User eif = (User) session.selectOne("selectUserByCardID", CardID);
							
							//只要刷卡都將記錄至raw_record table
							addRawSwipeRecord(session, eif, CardID,swipeCardTime,WorkshopNo);
							if (eif == null) {
								String swipeDate = DateGet.getDate();
								User selEmp = new User();
								selEmp.setCardID(CardID);
								selEmp.setSwipeDate(swipeDate);
								int lostRows = session.selectOne("selectLoseEmployee", selEmp);	
								User user1 = new User();								
								user1.setCardID(CardID);
								user1.setWorkshopNo(WorkshopNo);
								user1.setSwipeDate(swipeDate);
								user1.setSwipeCardTime(swipeCardTime);
								user1.setRecord_status("1");
								
								if (lostRows > 0) {
									// JOptionPane.showMessageDialog(null,"已記錄當前異常刷卡人員，今天不用再次刷卡！");
									jtextT1_1.setText("已記錄當前異常刷卡人員，今天不用再次刷卡！\n");
									jtextT1_1.setBackground(Color.RED);
									textT1_3.setText("");								
									session.update("updateRawRecordStatus",user1);
									session.commit();
									return;
								}
								/*
								 * JOptionPane.showMessageDialog(null,
								 * "當前刷卡人員不存在；可能是新進人員，或是舊卡丟失補辦，人員資料暫時未更新，請線長記錄，協助助理走原有簽核流程！"
								 * );
								 */
								jtextT1_1.setText("當前刷卡人員不存在；可能是新進人員，或是舊卡丟失補辦，人員資料暫時未更新，請線長記錄，協助助理走原有簽核流程！\n");
								jtextT1_1.setBackground(Color.RED);								
								session.update("updateRawRecordStatus",user1);
								session.commit();

							} else {
								String name = eif.getName();
								//String costId=
								String RC_NO = jtf.getText();
								String PRIMARY_ITEM_NO = textT2_1.getText();
								String Id = eif.getId();						
								//判斷該卡號是否已連續工作六天								
								if(!isUserContinuesWorkedOneWeek(session,eif,CardID,WorkshopNo,swipeCardTime)){					
									
									//該卡號是連續工作日小於六天
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
											
											Timestamp yesClassStart = empYesShift.getClass_start();
											Timestamp yesClassEnd = empYesShift.getClass_end();
											Timestamp goWorkSwipeTime = new Timestamp(new Date().getTime());

											Calendar outWorkc = Calendar.getInstance();
											outWorkc.setTime(yesClassEnd);
											outWorkc.set(Calendar.HOUR_OF_DAY,
													outWorkc.get(Calendar.HOUR_OF_DAY) + 3);
											outWorkc.set(Calendar.MINUTE,
													outWorkc.get(Calendar.MINUTE) + 30);
											Date dt = outWorkc.getTime();
											Timestamp afterClassEnd = new Timestamp(dt.getTime());									
																					
											if (empCurShiftCount == 0) {
												if (goWorkSwipeTime.before(afterClassEnd)) {
													// 刷卡在夜班下班3.5小時之內,記為昨日夜班下刷
													outWorkNSwipeCard(session,eif,CardID,swipeCardTime,yesterdayShift,yesterdayClassDesc);
												}else{
													// 刷卡在夜班下班3.5小時之后,今日班別有誤
													jtextT1_1.setBackground(Color.red);
													jtextT1_1.append("ID: " + eif.getId() + " Name: " + eif.getName() + "\n班別有誤，請聯繫助理核對班別信息!\n");
													User user1 = new User();
													user1.setCardID(CardID);
													user1.setId(Id);
													user1.setSwipeCardTime(swipeCardTime);
													user1.setRecord_status("2");
													session.update("updateRawRecordStatus",user1);
													session.commit();
												}
												
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
														
														outWorkNSwipeCard(session,eif,CardID,swipeCardTime,yesterdayShift,yesterdayClassDesc);
														
													} else {
														// 上班刷卡
														swipeCardRecord(session, eif, CardID,swipeCardTime);
													}
												} else {
													
													
													int goWorkNCardCount =  session
															.selectOne("selectGoWorkNByCardID", userNSwipe);
													if (goWorkNCardCount > 0) { 
														// 昨日夜班已存在上刷
														int yesterdaygoWorkCardCount =  session
																.selectOne("selectCountNByCardID", userNSwipe);
														if (yesterdaygoWorkCardCount == 0) {
															// 夜班下刷刷卡記錄不存在
															
															if (goWorkSwipeTime.before(afterClassEnd)) {
																// 刷卡在夜班下班3.5小時之內,記為昨日夜班下刷
																jtextT1_1.setBackground(Color.WHITE);
																jtextT1_1.setText(
																		"下班刷卡\n" + "ID: " + eif.getId() + "\nName: "
																				+ eif.getName() + "\n刷卡時間： " + swipeCardTime
																				+"\n昨日班別為:"+yesterdayClassDesc
																				+ "\n" + "員工下班刷卡成功！\n------------\n");
																session.update("updateOutWorkNSwipeTime", userNSwipe);
																session.commit();
															} else {
																// 刷卡在夜班下班3.5小時之后,記為今日白班上刷
															//	goOrOutWorkSwipeRecord(session, eif, CardID, curShift,
																//		curClassDesc,curClassStart);
																swipeCardRecord(session, eif,CardID,swipeCardTime);
															}
														} else {
															// 夜班下刷刷卡記錄已存在
															int isOutWoakSwipeDuplicate =  session
																	.selectOne("isOutWorkSwipeDuplicate", userNSwipe);
															if (isOutWoakSwipeDuplicate > 0) {
																outWorkSwipeDuplicate(session, eif, CardID,swipeCardTime, yesterdayShift);
															} else {
																swipeCardRecord(session, eif,CardID,swipeCardTime);
															}
														}
													} else {
														// 昨天夜班，今天白班的，昨日夜班上刷不存在，直接記為今天白班刷卡														
													  swipeCardRecord(session, eif,CardID,swipeCardTime);
										            	/*
														int yesterdaygoWorkCardCount =  session
																.selectOne("selectOutWorkByCardID", userNSwipe);
														if (yesterdaygoWorkCardCount == 0) {
															// 夜班下刷刷卡記錄不存在
															
															if (goWorkSwipeTime.before(afterClassEnd)) {
																// 刷卡在夜班下班3.5小時之內,記為昨日夜班下刷
																jtextT1_1.setBackground(Color.WHITE);
																jtextT1_1.setText("下班刷卡\n" + "ID: " + eif.getId()
																		+ "\nName: " + eif.getName() + "\n刷卡時間： "
																		+ swipeCardTime + "\n"
																		+"昨日班別為:"+yesterdayClassDesc
																		+ "\n員工下班刷卡成功！\n------------\n");
																session.insert("insertOutWorkSwipeTime",
																		userNSwipe);
																session.commit();
															} else {
																// 刷卡在夜班下班3.5小時之后,記為今日白班上刷
															//	goOrOutWorkSwipeRecord(session, eif, CardID, curShift,
															//			curClassDesc,curClassStart);
																
																swipeCardRecord(session, eif,CardID);
															}
														} else {
															// 夜班下刷刷卡記錄已存在
															int isOutWoakSwipeDuplicate =  session
																	.selectOne("isOutWorkSwipeDuplicate", userNSwipe);
															if (isOutWoakSwipeDuplicate > 0) {
																outWorkSwipeDuplicate(session, eif, CardID, yesterdayShift);
															} else {
																goOrOutWorkSwipeRecord(session, eif, CardID, curShift,
																		curClassDesc,curClassStart);
															}
														}																											
													*/
														}
												}

											}
										} else {
											swipeCardRecord(session, eif, CardID,swipeCardTime);
										}
									} else {
										swipeCardRecord(session, eif, CardID,swipeCardTime);
									}
									
								}
								else{
									//該卡號已連續工作六天，顯示錯誤訊息
									jtextT1_1.append("工號："+eif.getId()+" 姓名："+eif.getName()+" 已連續上班六天，此次刷卡不列入記錄！!\n");
									jtextT1_1.setBackground(Color.RED);
									User user1 = new User();
									user1.setCardID(CardID);
									user1.setId(Id);
									user1.setSwipeCardTime(swipeCardTime);
									user1.setRecord_status("4");
									session.update("updateRawRecordStatus",user1);
									session.commit();
								}	
																							
							}
						} catch (Exception e1) {
							System.out.println("Error opening session");
							logger.error("刷卡異常,原因:"+e1);
							dispose();
							SwipeCardNoDB d = new SwipeCardNoDB(WorkshopNo);
							throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e1, e1);
						} finally {
							ErrorContext.instance().reset();
							if (session != null) {
								session.close();
							}
							textT1_3.setText("");
						}
						textT1_3.setText("");
					} else {
						System.out.println("無輸入內容或輸入錯誤!");
					}
				}
			}
			}
		});

		c.add(tabbedPane);
		c.setBackground(Color.lightGray);
		linenoLabel = new JLabel("線號");
		linenoLabel.setBounds(105, 236, 277, 40);
		linenoLabel.setFont(new Font("微软雅黑", Font.BOLD, 25));
		panel1.add(linenoLabel);
		labelT1_4 = new JLabel("線號：");
		labelT1_4.setBounds(35, 236, 90, 40);
		labelT1_4.setFont(new Font("微软雅黑", Font.BOLD, 25));
		panel1.add(labelT1_4);
		if(LineNo == null || LineNo.equals("")){
			linenoLabel.setText("");
			labelT1_4.setVisible(false);
		}else{
			linenoLabel.setText(LineNo);
		}

		//textT1_1.setText(WorkshopNo);// 綁定車間
		workShopNoJlabel.setText(WorkshopNo);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	protected boolean IsAdminByCardID(String cardID, SqlSession session) {
		// TODO Auto-generated method stub
		try {

			int isAdmin = session.selectOne("isAdminByCardID", cardID);
			if (isAdmin > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("判断是否管理员错误，原因：" + e);
			dispose();
			SwipeCardNoDB d = new SwipeCardNoDB(defaultWorkshopNo);
		}
		return false;
	}

	public void swipeCardRecord(SqlSession session, User eif, String CardID, String swipeCardTime) {

		String Id = eif.getId();
		//String WorkshopNo = textT1_1.getText();
		String WorkshopNo = workShopNoJlabel.getText();
		
		  User curShiftUser = new User();
		  curShiftUser.setId(Id);
		  curShiftUser.setShiftDay(0);
		  
		int empCurShiftCount =  session.selectOne("getShiftCount", curShiftUser);
		if (empCurShiftCount == 0) {
			jtextT1_1.setBackground(Color.red);
			jtextT1_1.append("ID: " + eif.getId() + " Name: " + eif.getName() + "\n班別有誤，請聯繫助理核對班別信息!\n");
			User user1 = new User();
			user1.setCardID(CardID);
			user1.setId(Id);
			user1.setSwipeCardTime(swipeCardTime);
			user1.setRecord_status("2");
			session.update("updateRawRecordStatus",user1);
			session.commit();
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

					goWorkSwipeDuplicate(session, eif, CardID,swipeCardTime, curShift);

				} else {
					goOrOutWorkSwipeRecord(session, eif, CardID,swipeCardTime, curShift, curClassDesc,curClassStart);
				}

			} else {

				if (curShift.equals("D")) {
					if (goWorkSwipeTime.after(curClassEnd)) {					
						String name = eif.getName();
						String RC_NO = jtf.getText();
						String PRIMARY_ITEM_NO = textT2_1.getText();
						String PROD_LINE_CODE = linenoLabel.getText();

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
						userSwipe.setPROD_LINE_CODE(PROD_LINE_CODE);

						int curDayGoWorkCardCount =  session.selectOne("selectCountAByCardID", userSwipe);

						if (curDayGoWorkCardCount == 0) {

							int isOutWoakSwipeDuplicate =  session.selectOne("isOutWorkSwipeDuplicate", userSwipe);
							if (isOutWoakSwipeDuplicate > 0) {
								outWorkSwipeDuplicate(session, eif, CardID,swipeCardTime, curShift);
							} else {
								int outWorkCardCount =  session.selectOne("selectOutWorkByCardID", userSwipe);

								if (outWorkCardCount == 0) {
									jtextT1_1.setBackground(Color.WHITE);
									jtextT1_1.setText("下班刷卡\n" + "ID: " + Id + "\nName: " + eif.getName()
											+ "\n刷卡時間： " + swipeCardTime + "\n今日班別為："+curClassDesc+ "\n" + "員工下班刷卡成功！\n------------\n");
									synchronized (this){
										synchronized (this){
									session.insert("insertOutWorkSwipeTime", userSwipe);
										}
									}
									session.commit();
								} else {
									jtextT1_1.setBackground(Color.red);
									jtextT1_1.append("ID: " + eif.getId() + " Name: " + eif.getName() + "\n"
											+ "今日上下班卡已刷，此次刷卡無效！\n");
									User user1 = new User();
									user1.setCardID(CardID);
									user1.setId(Id);
									user1.setSwipeCardTime(swipeCardTime);
									user1.setRecord_status("6");
									session.update("updateRawRecordStatus",user1);
									session.commit();
								}
							}
						} else {
							outWorkSwipeCard(session, eif, CardID,swipeCardTime, curShift, curClassDesc);
						}
					} else {
						goOrOutWorkSwipeRecord(session, eif, CardID,swipeCardTime, curShift, curClassDesc,curClassStart);
					}
				} else {

					// 昨天日班，今天夜班刷卡
					// goOrOutWorkSwipeRecord(session, eif, CardID, curShift,
					// curClassDesc);

					if (goWorkSwipeTime.getHours() > 12) {// 刷卡在中午12點后為今日夜班上刷
						goOrOutWorkSwipeRecord(session, eif, CardID,swipeCardTime, curShift, curClassDesc,curClassStart);
					} else if (goWorkSwipeTime.getHours() <= 12) {// 刷卡在中午12點前
						jtextT1_1.setBackground(Color.RED);
						jtextT1_1.append("ID: " + eif.getId() + " Name: " + eif.getName() + "\n班別： " + curClassDesc
								+ "\n刷卡時間： " + swipeCardTime + "\n" + "昨日班別非夜班，今日班別為夜班，請在夜班上班前刷上班卡！\n");
						User user1 = new User();
						user1.setCardID(CardID);
						user1.setId(Id);
						user1.setSwipeCardTime(swipeCardTime);
						user1.setRecord_status("3");
						session.update("updateRawRecordStatus",user1);
						session.commit();
					}

				}

			}

		}
	}

	public void goOrOutWorkSwipeRecord(SqlSession session, User eif, String CardID, String swipeCardTime, String curShift,
			String curClassDesc,Timestamp curClassStart) {
		String Id = eif.getId();	
		//String WorkshopNo = textT1_1.getText();
		String WorkshopNo = workShopNoJlabel.getText();
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("CardID", CardID);
		param.put("Id", Id);
		param.put("WorkshopNo", WorkshopNo);
		param.put("Shift", curShift);
		param.put("SwipeCardTime", swipeCardTime);
		
		int curDayGoWorkCardCount =  session.selectOne("selectCountAByCardID", param);
		// 無刷卡記錄
		if (curDayGoWorkCardCount == 0) {
				
			goWorkSwipeCard(session, eif, CardID,swipeCardTime, curShift, curClassDesc,curClassStart);

		} else if (curDayGoWorkCardCount > 0) {

			int isGoWorkSwipeDuplicate = session.selectOne("isGoWorkSwipeDuplicate", param);
			if (isGoWorkSwipeDuplicate > 0) {
				goWorkSwipeDuplicate(session, eif, CardID,swipeCardTime, curShift);
			} else {
				// 下班刷卡
				outWorkSwipeCard(session, eif, CardID,swipeCardTime, curShift, curClassDesc);
			}
		}

	}

	public void goWorkSwipeCard(SqlSession session, User eif, String CardID, String swipeCardTime, String curShift, String curClassDesc,Timestamp curClassStart) {

		String WorkshopNo = workShopNoJlabel.getText();
		String name = eif.getName();
		String Id = eif.getId();
		String RC_NO = jtf.getText();
		String PRIMARY_ITEM_NO = textT2_1.getText();
		String PROD_LINE_CODE = linenoLabel.getText();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    try {

			Date parsedDate = dateFormat.parse(swipeCardTime);
			Timestamp swipeTime = new java.sql.Timestamp(parsedDate.getTime());
	
			long diffMinutes=(curClassStart.getTime() - swipeTime.getTime())/(60*1000);
			
			if(diffMinutes>14){//14精确到秒
				jtextT1_1.setBackground(Color.RED);
				jtextT1_1.setText("上班刷卡\n" + "ID: " + eif.getId() + "\nName: " + eif.getName() + "\n班別： " + curClassDesc
						+ "\n刷卡時間： " + swipeCardTime + "\n" + "超出上班刷卡時間限制，請於上班前15分鐘刷卡！\n------------\n");
				User user1 = new User();
				user1.setCardID(CardID);
				user1.setId(Id);
				user1.setSwipeCardTime(swipeCardTime);
				user1.setRecord_status("3");
				session.update("updateRawRecordStatus",user1);
				session.commit();
			}
			else{
				//上刷時間介於班別15分鐘至班別起始時間，則進行記錄
				jtextT1_1.setBackground(Color.WHITE);
				jtextT1_1.setText("上班刷卡\n" + "ID: " + Id + "\nName: " + eif.getName() + "\n班別： " + curClassDesc
						+ "\n刷卡時間： " + swipeCardTime + "\n" + "員工上班刷卡成功！\n------------\n");

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
				user1.setPROD_LINE_CODE(PROD_LINE_CODE);
				synchronized (this){
				session.insert("insertUserByOnDNShift", user1);
				}
				session.commit();
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			logger.error("上班刷卡異常，原因："+e);
			dispose();
			SwipeCardNoDB d = new SwipeCardNoDB(WorkshopNo);
			e.printStackTrace();
		}
	}

	public void outWorkSwipeCard(SqlSession session, User eif, String CardID, String swipeCardTime, String Shift, String ClassDesc) {
		String WorkshopNo = workShopNoJlabel.getText();
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

				outWorkSwipeDuplicate(session, eif, CardID,swipeCardTime, Shift);

			} else {
				jtextT1_1.setBackground(Color.red);
				jtextT1_1.append("ID: " + eif.getId() + " Name: " + eif.getName() + "\n" + "今日上下班卡已刷，此次刷卡無效！\n");
				User user1 = new User();
				user1.setCardID(CardID);
				user1.setId(Id);
				user1.setSwipeCardTime(swipeCardTime);
				user1.setRecord_status("6");
				session.update("updateRawRecordStatus",user1);
				session.commit();
			}
		} else if (curDayOutWorkCardCount == 0) {
			jtextT1_1.setBackground(Color.WHITE);
			jtextT1_1.setText("下班刷卡\n" + "ID: " + eif.getId() + "\nName: " + eif.getName() + "\n刷卡時間： " + swipeCardTime
					+ "\n班別： " + ClassDesc + "\n員工下班刷卡成功！\n------------\n");
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
	
	public void outWorkNSwipeCard(SqlSession session, User eif, String CardID, String swipeCardTime, String yesterdayShift, String yesterdayClassDesc) 
	{
		String Id=eif.getId();
		String name=eif.getName();
		String WorkshopNo = workShopNoJlabel.getText();
		String PROD_LINE_CODE = linenoLabel.getText();
		String RC_NO = jtf.getText();
		String PRIMARY_ITEM_NO = textT2_1.getText();
		User userNSwipe = new User();
		String SwipeCardTime2 = swipeCardTime;														
		userNSwipe.setId(Id);
		userNSwipe.setName(name);
		userNSwipe.setCardID(CardID);
		userNSwipe.setSwipeCardTime2(SwipeCardTime2);
		userNSwipe.setRC_NO(RC_NO);
		userNSwipe.setPRIMARY_ITEM_NO(PRIMARY_ITEM_NO);
		userNSwipe.setShift(yesterdayShift);
		userNSwipe.setWorkshopNo(WorkshopNo);
		userNSwipe.setPROD_LINE_CODE(PROD_LINE_CODE);
		
		int yesterdaygoWorkCardCount = session
				.selectOne("selectCountNByCardID", userNSwipe);

		// 下班刷卡

		if (yesterdaygoWorkCardCount > 0) {
			//已有上刷和下刷記錄
			int isOutWoakSwipeDuplicate =  session
					.selectOne("isOutWorkSwipeDuplicate", userNSwipe);
			if (isOutWoakSwipeDuplicate > 0) {

				outWorkSwipeDuplicate(session, eif, CardID,swipeCardTime, yesterdayShift);

			} else {
				jtextT1_1.setBackground(Color.red);
				jtextT1_1.append("ID: " + eif.getId() + " Name: "
						+ eif.getName() + "\n" + "今日上下班卡已刷，此次刷卡無效！\n");
				User user1 = new User();
				user1.setCardID(CardID);
				user1.setId(Id);
				user1.setSwipeCardTime(swipeCardTime);
				user1.setRecord_status("6");
				session.update("updateRawRecordStatus",user1);
				session.commit();
			}
		} else if (yesterdaygoWorkCardCount == 0) {
			//昨日上班卡有刷，今日下班卡沒刷 or 昨日上班卡沒刷，今日下班卡也沒刷
			int goWorkNCardCount =  session.selectOne("selectGoWorkNByCardID", userNSwipe);//取得該員工昨日到今日有上刷的筆數(有上刷)
			if (goWorkNCardCount == 0) {
				//昨日無上刷
				int isOutWoakSwipeDuplicate =  session.selectOne("isOutWorkSwipeDuplicate", userNSwipe);//取得該員工從10分鐘前至現在有無下刷記錄
				if (isOutWoakSwipeDuplicate > 0) {
					//10分鐘前至現在有下刷記錄，進行重複刷卡處理
					outWorkSwipeDuplicate(session, eif, CardID,swipeCardTime,yesterdayShift);
				} else {
					//10分鐘前至現在無下刷記錄
					int outWorkNCardCount =  session
							.selectOne("selectOutWorkByCardID", userNSwipe);//從今天至明天該員工的刷卡記錄（無上刷，有下刷）

					if (outWorkNCardCount == 0) {
						//無上刷也無下刷
						jtextT1_1.setBackground(Color.WHITE);
						jtextT1_1.setText("下班刷卡\n" + "ID: " + eif.getId()
								+ "\nName: " + eif.getName() + "\n刷卡時間： "
								+ swipeCardTime + "\n"
								+"昨日班別為:"+yesterdayClassDesc
								+ "\n員工下班刷卡成功！\n------------\n");
						synchronized (this){
						session.insert("insertOutWorkSwipeTime",userNSwipe);
						}
					} else {
						//無上刷有下刷
						jtextT1_1.setBackground(Color.red);
						jtextT1_1.append("ID: " + eif.getId() + " Name: "
								+ eif.getName() + "\n"
								+ "今日上下班卡已刷，此次刷卡無效！\n");
						User user1 = new User();
						user1.setCardID(CardID);
						user1.setId(Id);
						user1.setSwipeCardTime(swipeCardTime);
						user1.setRecord_status("6");
						session.update("updateRawRecordStatus",user1);
						session.commit();
					}
				}
			} else {
				//昨日有上刷
				jtextT1_1.setBackground(Color.WHITE);
				jtextT1_1.setText(
						"下班刷卡\n" + "ID: " + eif.getId() + "\nName: "
								+ eif.getName() + "\n刷卡時間： " + swipeCardTime
								+"\n昨日班別為:"+yesterdayClassDesc
								+ "\n" + "員工下班刷卡成功！\n------------\n");
				session.update("updateOutWorkNSwipeTime", userNSwipe);
			}
			session.commit();
		}

	}

	public void goWorkSwipeDuplicate(SqlSession session, User eif, String CardID, String swipeCardTime, String curShift) {

		//String WorkshopNo = textT1_1.getText();
		String WorkshopNo = workShopNoJlabel.getText();
		String PROD_LINE_CODE = linenoLabel.getText();
		String name = eif.getName();
		String Id = eif.getId();
		String RC_NO = jtf.getText();
		String PRIMARY_ITEM_NO = textT2_1.getText();

		jtextT1_1.setBackground(Color.WHITE);
		jtextT1_1.append("ID: " + Id + " Name: " + name + "\n" + "上班重複刷卡！\n");

		User userSwipeDup = new User();
		// String shift = "D";
		userSwipeDup.setCardID(CardID);
		userSwipeDup.setName(name);
		userSwipeDup.setId(Id);
		userSwipeDup.setSwipeCardTime(swipeCardTime);
		userSwipeDup.setRC_NO(RC_NO);
		userSwipeDup.setPRIMARY_ITEM_NO(PRIMARY_ITEM_NO);
		userSwipeDup.setWorkshopNo(WorkshopNo);
		userSwipeDup.setShift(curShift);		
		userSwipeDup.setRecord_status("5");
		userSwipeDup.setPROD_LINE_CODE(PROD_LINE_CODE);
		synchronized (this){
		session.update("updateRawRecordStatus",userSwipeDup);
		session.insert("goWorkSwipeDuplicate", userSwipeDup);
		}
		session.commit();
	}

	public void outWorkSwipeDuplicate(SqlSession session, User eif, String CardID, String swipeCardTime, String curShift) {

		//String WorkshopNo = textT1_1.getText();
		String WorkshopNo = workShopNoJlabel.getText();
		String name = eif.getName();
		String Id = eif.getId();
		String RC_NO = jtf.getText();
		String PRIMARY_ITEM_NO = textT2_1.getText();
		String PROD_LINE_CODE = linenoLabel.getText();

		jtextT1_1.setBackground(Color.WHITE);
		jtextT1_1.append("ID: " + Id + " Name: " + name + "\n" + "下班重複刷卡！\n");

		User userSwipeDup = new User();
		// String shift = "D";
		userSwipeDup.setCardID(CardID);
		userSwipeDup.setName(name);
		userSwipeDup.setId(Id);
		userSwipeDup.setSwipeCardTime2(swipeCardTime);
		userSwipeDup.setRC_NO(RC_NO);
		userSwipeDup.setPRIMARY_ITEM_NO(PRIMARY_ITEM_NO);
		userSwipeDup.setWorkshopNo(WorkshopNo);
		userSwipeDup.setPROD_LINE_CODE(PROD_LINE_CODE);
		userSwipeDup.setShift(curShift);
		
		userSwipeDup.setRecord_status("5");
		userSwipeDup.setSwipeCardTime(swipeCardTime);
		synchronized (this){
		session.update("updateRawRecordStatus",userSwipeDup);
		session.insert("outWorkSwipeDuplicate", userSwipeDup);
		}
		session.commit();
	}

	public String getShiftByClassDesc(String classDesc) {
		String shift = null;
		if (classDesc.indexOf("日") != -1 || classDesc.indexOf("中") != -1) {
			shift = "D";
		} else if (classDesc.indexOf("夜") != -1) {
			shift = "N";
		}
		return shift;
	}

	/**
	 * TODO
	 * 
	 * @return 指示單號
	 */
	public Object[] getItems() {
		List<User> user;
		SqlSession session = sqlSessionFactory.openSession();
		try {
			user = session.selectList("selectRCNo");
			int con = user.size();
			System.out.println(user.size());
			Object[] a = null;
			if (con > 0) {
				a = new Object[con + 1];
				a[0] = "";
				for (int i = 1; i < con + 1; i++) {
					// System.out.println(user.get(i).getRC_NO());
					a[i] = user.get(i - 1).getRC_NO();
					// a.add(user.get(i).getRC_NO());
				}
			}
			else {
				a = new Object[1];
				a[0] = "";
			}
			
			final Object[] s = a;
			return a;
		} catch (Exception e1) {
			System.out.println("Error opening session");
			logger.error("取得指示單號異常:"+e1);
			dispose();
			SwipeCardNoDB d = new SwipeCardNoDB(defaultWorkshopNo);
			throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e1, e1);
		} finally {
			ErrorContext.instance().reset();
			if (session != null) {
				session.close();
			}
		}
	}
	
	/*當員工刷卡時，立即記錄一筆刷卡資料至raw_record table中
	 * 
	 * */
	private void addRawSwipeRecord(SqlSession session, User eif, String CardID, String swipeCardTime,String WorkshopNo) {
		String Id=null;
		String ip = getLocalIp();
		try {
			if(eif!=null)
				Id=eif.getId();
			User newRawSwipeRecord=new User();
			newRawSwipeRecord.setCardID(CardID);
			newRawSwipeRecord.setId(Id);
			newRawSwipeRecord.setSwipeCardTime(swipeCardTime);
			newRawSwipeRecord.setIP_ADDRESS(ip);
			newRawSwipeRecord.setRecord_status(null);
			session.insert("addRawSwipeRecord", newRawSwipeRecord);
			session.commit();
		}
		catch(Exception ex) {
			logger.error("寫入原始刷卡記錄異常"+ex);
			dispose();
			SwipeCardNoDB d = new SwipeCardNoDB(WorkshopNo);
			ex.printStackTrace();
		}
	}
	
	

	private static String getLocalIp() {
		// TODO Auto-generated method stub
				Enumeration allNetInterfaces = null;
				try {
					allNetInterfaces = NetworkInterface.getNetworkInterfaces();
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				InetAddress ip = null;
				String ipv4 = "";
				while (allNetInterfaces.hasMoreElements()) {
					NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
//					System.out.println(netInterface.getName());
					Enumeration addresses = netInterface.getInetAddresses();
					while (addresses.hasMoreElements()) {
						ip = (InetAddress) addresses.nextElement();
						if (ip != null && ip instanceof Inet4Address) {
							if(ip.getHostAddress().equals("127.0.0.1")){  
		                        continue;  
		                    }
							ipv4 += ip.getHostAddress()+"/";
						}
					}
				}
				return ipv4;
	}

	/**
	 * 員工刷入卡號判斷是否無記錄，並作出相應對策
	 */
	private String[] showIDDialog() {
		String[] aArray = new String[2];
		String inputID = JOptionPane.showInputDialog("Please input a id");
		String inputName = null;
		aArray[0] = inputID;

		if (inputID == null) {

			return null;
		} else if (inputID.isEmpty()) {
			showIDDialog();
		} else {
			inputName = showNameDialog();
			aArray[1] = inputName;
		}
		// return aArray;
		return aArray;

	}

	private void breakShow() {
		return;
	}

	private String showNameDialog() {
		String inputName = JOptionPane.showInputDialog("Please input a Name");
		if (inputName == null) {
			return null;
		}
		if (inputName.isEmpty()) {
			showNameDialog();
		}
		return inputName;
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
	
	private boolean isUserContinuesWorkedOneWeek(SqlSession session, User eif, String cardID, String WorkshopNo,
			String swipeCardTime) {
		boolean isContinuesWorkForAWeek = false;
		try {
			// HashMap<String,Object>
			// workDays=session.selectOne("getContinuesWorker",CardID);
			// System.out.println("員工工作日:"+(long)workDays.get("work_count_week"));
			// if((long)workDays.get("work_count_week")<6)

			// 今天之前五天的记录，即:大于(new Date()-6 ) 且小于new Date()
			// int workDays=session.selectOne("getContinuesWorker",CardID);
			String Id = eif.getId();
			int workDays = 0;
			User yesShiftUser = new User();
			yesShiftUser.setId(Id);
			yesShiftUser.setShiftDay(1);
			
			User curShiftUser = new User();
			curShiftUser.setId(Id);
			curShiftUser.setShiftDay(0);

			User sixDayWorkerUser = new User();
			sixDayWorkerUser.setId(Id);
			sixDayWorkerUser.setShiftDay(7);

			workDays = session.selectOne("getOneWeekWorkDays", sixDayWorkerUser);

			int empYesShiftCount = session.selectOne("getShiftCount", yesShiftUser);
			int empCurShiftCount = session.selectOne("getShiftCount", curShiftUser);
			
			/*
			 * //今天前六天的刷卡记录,即:new Date()-6 User agoSevenDayShiftUser = new
			 * User(); agoSevenDayShiftUser.setId(Id);
			 * agoSevenDayShiftUser.setShiftDay(7);
			 * 
			 * User sevenDayWorkerUser = new User();
			 * sevenDayWorkerUser.setId(Id); sevenDayWorkerUser.setShiftDay(7);
			 * 
			 * int agoSevenDayShiftCount = session.selectOne("getShiftCount",
			 * agoSevenDayShiftUser); if (agoSevenDayShiftCount > 0) { User
			 * empSevenDayAgoUSer = (User) session.selectOne("getShiftByEmpId",
			 * agoSevenDayShiftUser); String
			 * empSevenDayAgoShift=empSevenDayAgoUSer.getShift();
			 * if(empSevenDayAgoShift.equals("N")){ //取得今天前，近五天的刷卡数量(包含new
			 * Date()-6 天夜班上下刷都完整的记录)
			 * 
			 * User sixDayWorkerUser = new User(); sixDayWorkerUser.setId(Id);
			 * sixDayWorkerUser.setShiftDay(6);
			 * 
			 * workDays=session.selectOne("getContinuesWorkDays",
			 * sixDayWorkerUser);
			 * 
			 * //取得new Date()-6 天白班记录和夜班有上刷但下刷为null的记录 int
			 * sixDayAgoRecord=session.selectOne("getDayRecord",sixDayWorkerUser
			 * ); workDays+=sixDayAgoRecord; } else{ //取得今天前，近六天的刷卡数量
			 * workDays=session.selectOne("getContinuesWorkDays",
			 * sevenDayWorkerUser); } }else{ //取得今天前，近六天的刷卡数量
			 * workDays=session.selectOne("getContinuesWorkDays",
			 * sevenDayWorkerUser); }
			 */
						
			if (empYesShiftCount > 0) {				
				
				User empYesUSer = (User) session.selectOne("getShiftByEmpId", yesShiftUser);
				String empYesShift = empYesUSer.getShift();
				if (empYesShift.equals("N")) {
					String SwipeCardTime2 = swipeCardTime;
					User userNSwipe = new User();
					userNSwipe.setSwipeCardTime2(SwipeCardTime2);
					userNSwipe.setCardID(cardID);
					userNSwipe.setId(Id);
					userNSwipe.setShift(empYesShift);
					userNSwipe.setWorkshopNo(WorkshopNo);
					int goWorkNCardCount = session.selectOne("selectGoWorkNByCardID", userNSwipe);// 昨日夜班上刷记录

					int yesterdaygoWorkCardCount = session.selectOne("selectCountNByCardID", userNSwipe);// 昨日夜班下刷记录

					Timestamp yesClassStart = empYesUSer.getClass_start();
					Timestamp yesClassEnd = empYesUSer.getClass_end();
					Timestamp goWorkSwipeTime = new Timestamp(new Date().getTime());

					Calendar outWorkc = Calendar.getInstance();
					outWorkc.setTime(yesClassEnd);
					outWorkc.set(Calendar.HOUR_OF_DAY, outWorkc.get(Calendar.HOUR_OF_DAY) + 3);
					outWorkc.set(Calendar.MINUTE, outWorkc.get(Calendar.MINUTE) + 30);
					Date dt = outWorkc.getTime();
					Timestamp afterClassEnd = new Timestamp(dt.getTime());
					if (goWorkNCardCount > 0) {
						// 昨日夜班已存在上刷
						if (yesterdaygoWorkCardCount == 0) { // 夜班下刷刷卡記錄不存在
							if (empCurShiftCount > 0) {
								User curYesUSer = (User) session.selectOne("getShiftByEmpId",
										curShiftUser);
								String empCurShift = curYesUSer.getShift();
								if (empCurShift.equals("N")) {
									if (goWorkSwipeTime.getHours() < 12) {
										// 刷卡在12点之前,記為昨日夜班下刷
										workDays = workDays - 1;
									}
								} else {
									if (goWorkSwipeTime.before(afterClassEnd)) {
										// 刷卡在夜班下班3.5小時之內,記為昨日夜班下刷
										workDays = workDays - 1;
									}
								}
							} else {
								if (goWorkSwipeTime.getHours() < 12) {
									// 刷卡在12点之前,記為昨日夜班下刷
									workDays = workDays - 1;
								}
							}

						} else {

							int isOutWorkSwipeDuplicate = session.selectOne("isOutWorkSwipeDuplicate", userNSwipe);
							if (isOutWorkSwipeDuplicate > 0) {
								outWorkSwipeDuplicate(session, eif, cardID, swipeCardTime, empYesShift);
								workDays = -1;
							}
						}
					} else { // 昨日夜班不存在上刷
						if (empCurShiftCount>0) {
							User curYesUSer = (User) session.selectOne("getShiftByEmpId", curShiftUser);
							String empCurShift=curYesUSer.getShift();
						if (empCurShift.equals("N")) {
							if (goWorkSwipeTime.getHours() <= 12) {
								// 刷卡在12點前的,記為昨日夜班下刷
								int twoDayBeforworkDays = session.selectOne("getTwoDayBeforWorkDays", sixDayWorkerUser);
								if (twoDayBeforworkDays < 6) {
									int outWorkNCardCount = session.selectOne("selectOutWorkByCardID", userNSwipe);// 夜班昨天无上刷，今天有下刷

									/*
									 * if (outWorkNCardCount > 0) { workDays =
									 * workDays + 1; }
									 */
									int isOutWorkSwipeDuplicate = session.selectOne("isOutWorkSwipeDuplicate",
											userNSwipe);
									if (isOutWorkSwipeDuplicate > 0) {
										outWorkSwipeDuplicate(session, eif, cardID, swipeCardTime, empYesShift);
										workDays = -1;
									}
								} else {
									workDays = twoDayBeforworkDays;
								}
							}
						}
					  }
					}
				}
			}

			System.out.println("員工工作日:" + workDays);
			if (workDays < 6)
				isContinuesWorkForAWeek = false;
			else
				isContinuesWorkForAWeek = true;
		} catch (Exception ex) {
			logger.error("七休一異常：" + ex);
			dispose();
			SwipeCardNoDB d = new SwipeCardNoDB(WorkshopNo);
			ex.printStackTrace();
		}
		return isContinuesWorkForAWeek;
	}

	public static void main(String args[]) {
		boolean OneWindow = OpenOneWindow.checkLock();
		
		if (OneWindow) {
			InitGlobalFont(new Font("微软雅黑", Font.BOLD, 18));
			final String defaultWorkshopNo = jsonFileUtil.getSaveWorkshopNo();
			String WorkShopNo = null;
			String LineNo =null;
			if(defaultLineNo != null){
				LineNo = defaultLineNo;
			}
			if (defaultWorkshopNo != null) {
				WorkShopNo = defaultWorkshopNo;
				SwipeCard d = new SwipeCard(WorkShopNo,LineNo);
			} else {
				SwipeCardLogin d = new SwipeCardLogin();
			}
			//檢測ip是否可用
			String ip = getLocalIp();
			CheckIp checkIp = new CheckIp(ip);
			Thread executeCheckIp = new Thread(checkIp);
			executeCheckIp.start();
			//检测版本是否最新
			CheckCurrentVersion chkVersion = new CheckCurrentVersion(CurrentVersion);
			Thread executeCheckVersion = new Thread(chkVersion);
			executeCheckVersion.start();
		} else {
			JOptionPane.showConfirmDialog(null, "程序已經開啟，請不要重複開啟", "程序重複打開", JOptionPane.DEFAULT_OPTION);
			System.exit(0);
		}
	}

	public void update() {
		// String LineNo = textT1_2.getText();
		//String WorkshopNo = textT1_1.getText();
		String WorkshopNo = workShopNoJlabel.getText();
		Object ShiftName = comboBox2.getSelectedItem();
		System.out.println("comboBox2" + ShiftName);
		String ShiftRcNo = "";
		if (ShiftName.equals("夜班")) {
			ShiftRcNo = "N";
		} else {
			ShiftRcNo = "D";
		}

		panel2.remove(myScrollPane);
		myModel = new MyNewTableModel(WorkshopNo, ShiftRcNo);
		mytable = new JTable(myModel);

		myScrollPane = new JScrollPane(mytable);
		myScrollPane.setBounds(310, 40, 520, 400);
		setTable();
		panel2.add(myScrollPane);
		panel2.updateUI();
		panel2.repaint();
	}

	public void setTable() {
		mytable.getColumnModel().getColumn(0).setMaxWidth(40);
		mytable.getColumnModel().getColumn(1).setMaxWidth(40);
		mytable.getColumnModel().getColumn(2).setMaxWidth(60);
		mytable.setRowHeight(25);
		mytable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		JTableHeader header = mytable.getTableHeader();
		header.setFont(new Font("微软雅黑", Font.BOLD, 16));
		header.setPreferredSize(new Dimension(header.getWidth(), 30));
	}

}
