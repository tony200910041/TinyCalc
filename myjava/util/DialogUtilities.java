/** This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package myjava.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.html.*;
import java.util.*;
import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import myjava.gui.*;
import myjava.util.*;
import myjava.util.math.*;

public final class DialogUtilities implements FontConstants, HyperlinkListener
{	
	public static void showFunctionDialog(Frame parent)
	{
		/*
		 * show function list (load by reflection)
		 */
		TreeSet<String> set = new TreeSet<>();
		for (int i=1; i<=2; i++)
		{
			for (Method method: (i==1?Math.class:MathUtilities.class).getDeclaredMethods())
			{
				int modifier = method.getModifiers();
				if (Modifier.isPublic(modifier))
				{
					String name = method.getName();
					if (name.equals(name.toLowerCase()))
					{
						set.add(name.replace("one","1").replace("two","2").replace("ten","10"));
					}
				}
			}
		}
		JList<String> list = new JList<>(set.toArray(new String[0]));
		list.setFont(f13);
		list.ensureIndexIsVisible(0);
		JDialog dialog = new JDialog(parent,"Function list",true);
		dialog.setLayout(new BorderLayout());
		JScrollPane pane = new JScrollPane(list);
		pane.setPreferredSize(new Dimension(250,300));
		dialog.add(pane, BorderLayout.CENTER);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}
	
	public static void showConstantDialog(Frame parent)
	{
		/*
		 * show constants list
		 */
		DefaultTableModel tm = new DefaultTableModel();
		JTable table = new JTable(tm)
		{
			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};
		tm.addColumn("Constant");
		tm.addColumn("Value");
		tm.addColumn("Symbol");
		table.setRowHeight(23);
		table.setFont(f13);
		table.setAutoCreateRowSorter(true);
		table.getTableHeader().setFont(f13);
		//get constants
		for (Constant c: ConstantList.getInstance())
		{
			tm.addRow(new Object[]{c.getName(),c.toString(),c.getVarString()});
		}
		//
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		table.getColumnModel().getColumn(1).setPreferredWidth(166);
		//
		JDialog dialog = new JDialog(parent,"Constants",true);
		dialog.setLayout(new BorderLayout());		
		dialog.add(new JScrollPane(table), BorderLayout.CENTER);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}
	
	public static void showDocumetationDialog(Frame parent) throws IOException
	{
		/*
		 * html files URL:
		 */
		URL url = parent.getClass().getResource("/doc/index.html");
		/*
		 * dialog:
		 */
		JDialog dialog = new JDialog(parent,"Documentation",true);
		dialog.setLayout(new BorderLayout());
		JEditorPane editorPane = new JEditorPane();		
		editorPane.setPage(url);
		editorPane.setEditable(false);
		editorPane.addHyperlinkListener(new DialogUtilities());
		dialog.add(new JScrollPane(editorPane), BorderLayout.CENTER);
		//
		dialog.setSize(550,390);
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}
	
	@Override
	public void hyperlinkUpdate(HyperlinkEvent ev)
	{
		if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
		{
			JEditorPane editorPane = (JEditorPane)(ev.getSource());
			if (ev instanceof HTMLFrameHyperlinkEvent)
			{
				HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent)ev;
				HTMLDocument doc = (HTMLDocument)(editorPane.getDocument());
				doc.processHTMLFrameHyperlinkEvent(evt);
			}
			else
			{
				try
				{
					editorPane.setPage(ev.getURL());
				}
				catch (Exception ex)
				{
				}
			}
		}
	}
}
