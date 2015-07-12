/** This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.text.DecimalFormat;
import java.math.*;
import myjava.gui.*;
import myjava.util.*;
import myjava.util.math.*;

public class TinyCalc extends JFrame implements DocumentListener, ActionListener, FontConstants
{
	//version number
	private static final String VERSION_NO = "2.0";
	private static final String BETA_NO = "";
	//
	private static final File SETTING_FILE = new File(getSettingFilePath(),"TINYCALC_MEMORY");
	static
	{
		if (!SETTING_FILE.exists())
		{
			try (PrintWriter writer = new PrintWriter(SETTING_FILE,"UTF-8")) {} catch (IOException ex) {}
		}
	}
	private Properties prop = new Properties();
	//data
	private final BigDecimal[] memory = new BigDecimal[10];
	private final BigDecimal[] tmpVar = new BigDecimal[10];
	private BigDecimal lastAns = new BigDecimal(0);
	//components
	private JTextField tf1 = new JTextField(50);
	private JLabel answer = new JLabel("N/A");
	private JPanel centerPanel = new JPanel(new BorderLayout());
	private JPopupMenu popup = new JPopupMenu();
	private static TinyCalc w;
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				UIManager.put("OptionPane.buttonFont", f13);
				UIManager.put("OptionPane.messageFont", f13);
				UIManager.put("OptionPane.okButtonText", "OK");
				UIManager.put("ToolTip.font",f13);
				if (!UIManager.getLookAndFeel().getName().contains("Windows"))
				{
					UIManager.put("Button.background", Color.WHITE);
					UIManager.put("ToolTip.background",Color.WHITE);
				}
				ToolTipManager.sharedInstance().setInitialDelay(50);
				ToolTipManager.sharedInstance().setDismissDelay(20000);
				w = new TinyCalc();
				w.setVisible(true);
			}
		});
	}
	
	TinyCalc()
	{
		super("TinyCalc " + VERSION_NO);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent ev)
			{
				TinyCalc.this.close();
			}
		});
		this.setLayout(new BorderLayout());
		this.tf1.setFont(f40);
		this.tf1.setBackground(new Color(255,255,227));
		this.tf1.setForeground(new Color(33,68,72));
		this.tf1.setSelectionColor(new Color(252,190,46));
		this.tf1.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent ev)
			{
				if (ev.isPopupTrigger())
				{
					popup.show(tf1,ev.getX(),ev.getY());
				}
			}
		});
		this.popup.add(new MyMenuItem("Cut",1));
		this.popup.add(new MyMenuItem("Copy",2));
		this.popup.add(TinyCalc.createSeparator());
		this.popup.add(new MyMenuItem("Paste",3));
		this.answer.setFont(f40);
		this.answer.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent ev)
			{
				if (ev.getClickCount() >= 2)
				{
					/* copy answer
					 * value shown: answer.getText();
					 * plain value: lastAnswer.toString();
					 * have to ensure last calculation is successful
					 * not meaningful to copy (N/A)
					 */
					String[] option = new String[]{"Answer", "Plain value", "Cancel"};
					String text = answer.getText();
					if (!("N/A").equals(text))
					{
						int copy = JOptionPane.showOptionDialog(TinyCalc.this, "The answer is " + text + ".\nCopy to clipboard?", "Copy answer", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, option, option[0]);
						if (copy == JOptionPane.YES_OPTION)
						{
							Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text),null);
						}
						else if (copy == JOptionPane.NO_OPTION)
						{
							Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(lastAns.toPlainString()),null);
						}
					}
				}
			}
		});
		JPanel bottom = new JPanel();
		bottom.add(this.answer);
		centerPanel.add(tf1, BorderLayout.CENTER);
		centerPanel.add(bottom, BorderLayout.PAGE_END);
		centerPanel.setPreferredSize(new Dimension(500,135));
		this.add(centerPanel, BorderLayout.CENTER);		
		this.tf1.getDocument().addDocumentListener(this);
		this.tf1.addActionListener(this);
		//fill BigDecimal[] memory and tmpVar
		this.loadConfig();
		//main frame:
		try
		{
			ArrayList<Image> image = new ArrayList<>();
			image.add(img("APPICON16"));
			image.add(img("APPICON32"));
			image.add(img("APPICON48"));
			this.setIconImages(image);
		}
		catch (Exception ex)
		{
		}
		this.pack();
		this.setMinimumSize(this.getSize());
		this.setLocationRelativeTo(null);
	}
	
	@Override
	public void changedUpdate(DocumentEvent ev)
	{
		update();
	}
	
	@Override
	public void removeUpdate(DocumentEvent ev)
	{
		update();
	}
	
	@Override
	public void insertUpdate(DocumentEvent ev)
	{
		update();
	}
	
	@Override
	public void actionPerformed(ActionEvent ev)
	{
		/*
		 * respond to "Enter"
		 */
		update();
	}
	
	static String replaceConstants(String str)
	{
		/*
		 * add back *
		 */
		str = str.replace(")(", ")*(");
		str = str.replace("][", "]*[");
		str = str.replace(")[", ")*[");
		str = str.replace("](", "]*(");
		int i=1;
		while (i < str.length())
		{
			try
			{
				//add back '*' sign
				char prev = str.charAt(i-1);
				char now = str.charAt(i);
				if ((Character.isDigit(prev)||(prev == ')'))&&(now != 'd')&&(now != 'r')&&Character.isLetter(now))
				{
					str = str.substring(0,i) + '*' + str.substring(i, str.length());
				}
			}
			catch (Exception ex)
			{
				//skip
			}
			finally
			{
				i++;
			}
		}
		// replace functions:
		str = str.replace("log10", "logten").replace("log1p", "logonep");
		str = str.replace("atan2", "atantwo").replace("expm1", "expmone");
		str = str.replace("simu2", "simutwo");
		str = str.replace("sin-1", "asin");
		str = str.replace("cos-1", "acos");
		str = str.replace("tan-1", "atan");
		str = str.replace("csc-1", "acsc");
		str = str.replace("sec-1", "asec");
		str = str.replace("cot-1", "acot");
		/*
		 * add back brackets
		 */
		int open = 0;
		int close = 0;
		char chars[] = str.toCharArray();
		for (char c: chars)
		{
			if (c == '(')
			{
				open++;
			}
			else if (c == ')')
			{
				close++;
			}
		}
		for (int j=0; j<open; j++)
		{
			str = str + ")";
		}
		for (int j=0; j<close; j++)
		{
			str = "(" + str;
		}
		/*
		 * replace constants
		 */
		for (Constant c: ConstantList.getInstance())
		{
			for (String v: c.getVarNames())
			{
				str = str.replace(v,c.getValue());
			}
		}
		// replace ++ and --
		str = str.replace("++", "+").replace("--", "+").replace("+-", "-").replace("-+", "-");
		// replace angle unit
		str = str.replace("-d", "*" + Math.PI + "/180").replace("-r", "*180/" + Math.PI);
		/*
		 *  replace E
		 */
		str = str.replace("E","10^");
		return str;
	}
	
	static String replaceVariable(String str)
	{
		// remove space
		str = str.replace(" ", "");
		// split
		String[] strs = str.split(";");
		if (strs.length == 1)
		{
			return strs[0];
		}		
		else 
		{
			String[] variables = strs[1].split(",");
			BigDecimal x = null, y = null, z = null;
			if (variables.length >= 1)
			{
				x = evaluate(variables[0])[0];
			}
			if (variables.length >= 2)
			{
				y = evaluate(variables[1])[0];
			}
			if (variables.length >= 3)
			{
				z = evaluate(variables[2])[0];
			}
			return strs[0].replace("[x]",String.valueOf(x)).replace("[y]",String.valueOf(y)).replace("[z]",String.valueOf(z));
		}
	}
	
	static BigDecimal[] evaluate(String expression)
	{
		return EvalUtilities.evaluate(EvalUtilities.toRPN(Symbol.toSymbols(TinyCalc.replaceConstants(expression))));
	}
	
	void update()
	{
		//backup last answer:
		BigDecimal lastAnsSave = lastAns;
		try
		{
			String str = tf1.getText();
			if (str.equals("about"))
			{
				/*
				 * shows about dialog
				 */
				try
				{
					JOptionPane.showMessageDialog(this, "TinyCalc " + VERSION_NO + BETA_NO + " -- a simple calculator written in Java.\nBy tony200910041.\nDistributed under MPL 2.0.", "About", JOptionPane.INFORMATION_MESSAGE, icon("APPICON48"));
				}
				catch (Exception ex)
				{
					JOptionPane.showMessageDialog(this, "TinyCalc " + VERSION_NO + BETA_NO + " -- a simple calculator written in Java.\nBy tony200910041.\nDistributed under MPL 2.0.", "About", JOptionPane.INFORMATION_MESSAGE);
				}
				finally
				{
					return;
				}
			}
			else if (str.equals("exit")||str.equals("leave"))
			{
				this.close();
			}
			else if (str.equals("function"))
			{
				DialogUtilities.showFunctionDialog(this);
			}
			else if (str.equals("const"))
			{
				DialogUtilities.showConstantDialog(this);
			}
			else if (str.equals("doc"))
			{
				try
				{
					DialogUtilities.showDocumetationDialog(this);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
			else
			{
				/*
				 * evaluates expression
				 */
				//settings:
				boolean isFraction = false;
				boolean isTrigo = false;
				boolean isSixty = false;
				boolean isSurd = false;
				boolean use981 = false;
				int toMemory;
				int toTmpVar;
				/*
				 *  toMemory:
				 * -1 indicates "don't save", and 0-9 indicates "save to 0-9"
				 */ 
				// replace M, tmpVar and ans
				str = str.replace("[ans]", "("+String.valueOf(lastAns)+")");
				for (int i=0; i<memory.length; i++)
				{
					String replace = "("+String.valueOf(memory[i])+")";
					str = str.replace("[M" + i + "]", replace);
					str = str.replace("[mem" + i + "]", replace);
				}
				for (int i=0; i<tmpVar.length; i++)
				{
					String replace = "("+String.valueOf(tmpVar[i])+")";
					str = str.replace("[tv" + i + "]", replace);
					str = str.replace("[tmpvar" + i + "]", replace);
					str = str.replace("[var" + i + "]", replace);
				}
				/*
				 * toMemory and toTmpVar
				 * can override multi-return functions' results
				 */
				//toMemory
				if (str.matches(".*save[0123456789]")) //ends with save0, save1 etc
				{					
					try
					{
						int number = Integer.parseInt(str.substring(str.length()-1));
						if ((number<0)||(number>9))
						{
							throw new SyntaxException("Invalid variable index: " + number);
						}
						toMemory = number;
					}
					catch (NumberFormatException ex)
					{
						throw new SyntaxException("Invalid variable index");
					}
					str = str.substring(0,str.length()-5);
				}
				else
				{
					toMemory = -1;
				}
				// to tmpVar
				if (str.matches(".*tv[0123456789]")) //ends with tv0, tv1 etc
				{					
					try
					{
						int number = Integer.parseInt(str.substring(str.length()-1));
						if ((number<0)||(number>9))
						{
							throw new SyntaxException("Invalid variable index: " + number);
						}
						toTmpVar = number;
					}
					catch (NumberFormatException ex)
					{
						throw new SyntaxException("Invalid variable index");
					}
					str = str.substring(0,str.length()-3);
				}
				else
				{
					toTmpVar = -1;
				}
				// fraction, sixty degree and surd
				if (str.endsWith("f"))
				{
					isFraction = true;
					str = str.substring(0,str.length()-1);
				}
				else if (str.endsWith("sixty"))
				{
					isSixty = true;
					str = str.substring(0,str.length()-5);
				}
				else if (str.endsWith("surd"))
				{
					isSurd = true;
					str = str.substring(0,str.length()-4);
				}			
				/*
				 * replace variables:
				 */
				str = TinyCalc.replaceVariable(str);
				/*
				 * test trigo functions:
				 */
				isTrigo = TinyCalc.isTrigoString(str);
				/*
				 * evaluate answer:
				 */
				BigDecimal[] result = TinyCalc.evaluate(str);
				lastAns = result[0];
				//copy other answers into memory
				if (result.length >= 2)
				{
					System.arraycopy(result,0,tmpVar,0,result.length);
				}
				// trigo function calibration
				if (isTrigo)
				{
					double x = lastAns.doubleValue();
					if (Math.round(x) == MathUtilities.toFix(x,13))
					{
						//round lastAns
						lastAns = lastAns.setScale(0,RoundingMode.HALF_UP);
					}
					if (Math.abs(x)>Math.pow(10,15))
					{
						throw new ArithmeticException("Too large value of trigonometric operations: " + lastAns.toString()); //tan(pi/2)
					}
				}
				// three cases
				double lastAnsDouble = lastAns.doubleValue(); //use double for better performance
				String tooltip;
				if (isFraction)
				{
					str = new Fraction(lastAns).toString();
				}
				else if (isSixty)
				{
					str = new Sixty(lastAnsDouble).toString();
				}
				else if (isSurd)
				{
					str = new Surd(lastAnsDouble).toString();
				}
				// normal: format answer in exponent form
				else
				{
					str = MathUtilities.formatNormalAnswer(lastAns);
				}
				answer.setText(str);
				answer.setToolTipText("<html>Plain value: " + lastAns.toPlainString() + (str.contains("9.80665")?"<br>Close to Earth":"") + "</html>");
				// toMemory and toTmpVar
				if (toMemory != -1)
				{
					memory[toMemory] = lastAns;
				}
				if (toTmpVar != -1)
				{
					tmpVar[toTmpVar] = lastAns;
				}
			}
		}
		catch (ArithmeticException ex)
		{
			//retain old answer
			lastAns = lastAnsSave;
			answer.setText("Math ERROR!");
			answer.setToolTipText("<html>Math ERROR!<br>" + ex.getMessage() + "</html>");
		}
		catch (Exception ex)
		{
			lastAns = lastAnsSave;
			answer.setText("N/A");
			if (tf1.getText().isEmpty())
			{
				answer.setToolTipText(null);
			}
			else
			{
				answer.setToolTipText(ex.getMessage());
			}
		}
	}
	
	class MyMenuItem extends JMenuItem implements ActionListener
	{
		private int x;
		MyMenuItem(String text, int x)
		{
			super(text);
			this.setFont(f13);
			this.setBackground(Color.WHITE);
			this.addActionListener(this);
			this.x = x;
		}
		
		@Override
		public void actionPerformed(ActionEvent ev)
		{
			switch (this.x)
			{
				case 1: //cut
				tf1.cut();
				break;
				
				case 2: //copy
				tf1.copy();
				break;
				
				case 3: //paste
				tf1.paste();
				break;
			}
		}
	}
	
	static boolean isTrigoString(String str)
	{
		String[] names = new String[]{"sin","cos","tan","csc","sec","cot","area","heron","opside","area","angle"};
		for (String s: names)
		{
			if (str.contains(s)) return true;
		}
		return false;
	}
	
	static JSeparator createSeparator()
	{
		JSeparator separator = new JSeparator();
		separator.setBackground(Color.WHITE);
		return separator;
	}
	
	static ImageIcon icon(String name)
	{
		return new ImageIcon(TinyCalc.class.getResource("/"+name+".PNG"));
	}
	
	static Image img(String name)
	{
		return icon(name).getImage();
	}
	
	static String getSettingFilePath()
	{
		try
		{			
			return (new File(TinyCalc.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath())).getParentFile().getPath();
		}
		catch (Exception ex)
		{
			return null;
		}
	}
	
	void loadConfig()
	{
		try
		{
			prop.load(new FileInputStream(SETTING_FILE));
			//load memory and tmpVar
			for (int i=0; i<10; i++)
			{
				try
				{
					memory[i] = new BigDecimal(prop.getProperty("memory" + i));
				}
				catch (Exception ex)
				{
					memory[i] = BigDecimal.ZERO;
				}
				try
				{
					tmpVar[i] = new BigDecimal(prop.getProperty("tmpVar" + i));
				}
				catch (Exception ex)
				{
					tmpVar[i] = BigDecimal.ZERO;
				}
			}
		}
		catch (IOException ex)
		{
		}
	}
	
	void saveConfig()
	{
		for (int i=0; i<10; i++)
		{
			prop.setProperty("memory" + i, memory[i].toPlainString());
			prop.setProperty("tmpVar" + i, tmpVar[i].toPlainString());
		}
		try
		{
			prop.store(new FileOutputStream(SETTING_FILE), "TinyCalc variables: DO NOT MODIFY");
		}
		catch (IOException ex)
		{
		}
	}
	
	void close()
	{
		this.saveConfig();
		System.exit(0);
	}
}
