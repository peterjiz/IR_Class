package UI;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;

/**
 * Panel for the search engine
 */
public class SearchEnginePanel extends JPanel 
{

	private static final long serialVersionUID = 6915622549267792262L;

	private EventListenerList listerList = new EventListenerList();
	
	public SearchEnginePanel()
	{
		Dimension size = getPreferredSize(); 
		
		size.height = 100;
		
		setPreferredSize(size);
		
		setBorder(BorderFactory.createTitledBorder("Search Engine"));
		
		JLabel query = new JLabel("Query: ");
		
		final JTextField queryField = new JTextField(20);
		
		
		JLabel KValue = new JLabel("K: ");
		final JTextField KField = new JTextField(10);
		
		//KField.setText("Enter K");
		
		
		JLabel iValue = new JLabel("it:");
		final JTextField iField = new JTextField(10);
		
		JLabel kValue = new JLabel("k:");
		final JTextField kField = new JTextField(10);
		
		JLabel bValue = new JLabel("b:");
		final JTextField bField = new JTextField(10);
		
		JLabel titleValue = new JLabel("Tw:");
		final JTextField titleField = new JTextField(10);
		
		JLabel headerValue = new JLabel("Hw:");
		final JTextField headerField = new JTextField(10);
		
		JLabel paragraphValue = new JLabel("Pw:");
		final JTextField paragraphField = new JTextField(10);
		
		JLabel topValue = new JLabel("Top:");
		final JTextField topField = new JTextField(10);
		
		
		KField.setText("5");
		kField.setText("1.2");
		//iField.setText("10");
		iField.setText("2");
		bField.setText("0.75");
		titleField.setText("5");
		headerField.setText("3");
		paragraphField.setText("1");
		topField.setText("5");
		
		
		
		JButton addBtn = new JButton("Search");
		
		
		addBtn.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) 
			{
				String query = queryField.getText();
				String KSetting = KField.getText();
				String iterationsSetting = iField.getText();
				String okapik = kField.getText();
				String okapib = bField.getText();
				String titleWeight = titleField.getText();
				String headerWeight = headerField.getText();
				String paragraphWeight = paragraphField.getText();
				String top = topField.getText();
				
				
				// Sending query to Detail Event
				fireDetailEvent(new ReceiveEvent(this, query, KSetting, iterationsSetting, okapik, okapib, titleWeight, headerWeight, paragraphWeight, top));
			}
			
		});
		
		setLayout(new GridBagLayout());

		GridBagConstraints gc = new GridBagConstraints();
		
		//// First column //////////
		gc.anchor = GridBagConstraints.LINE_END;
		gc.weightx = 0.5;
		gc.weighty = 0.5;
		
		gc.gridx = 0;
		gc.gridy = 0;
		
		add(query, gc);
		
		//// Second column //////
		gc.anchor = GridBagConstraints.FIRST_LINE_START;
		gc.gridx = 1;
		gc.gridy = 0;
		
		add(queryField, gc);
		
		////Final row ///// 
		gc.weighty = 10;
		gc.anchor = GridBagConstraints.FIRST_LINE_START;
		gc.gridx = 1;
		gc.gridy = 2;
		
		add(addBtn, gc);
		
		
		
		
		
		
		
		//K Value
		gc.anchor = GridBagConstraints.LAST_LINE_END;
		gc.weightx = 0.5;
		gc.weighty = 0.5;
		gc.gridx = 1;
		gc.gridy = 0;
		add(KValue, gc);
		
		gc.anchor = GridBagConstraints.PAGE_START;
		gc.gridx = 2;
		gc.gridy = 0;
		add(KField, gc);
		
		
		//i Value
		gc.anchor = GridBagConstraints.LAST_LINE_END;
		gc.weightx = 0.5;
		gc.weighty = 0.5;
		gc.gridx = 2;
		gc.gridy = 0;
		add(iValue, gc);
		
		gc.anchor = GridBagConstraints.PAGE_START;
		gc.gridx = 3;
		gc.gridy = 0;
		add(iField, gc);
		
		
		//okapi k Value
		gc.anchor = GridBagConstraints.LAST_LINE_END;
		gc.weightx = 0.5;
		gc.weighty = 0.5;
		gc.gridx = 3;
		gc.gridy = 0;
		add(kValue, gc);
		
		gc.anchor = GridBagConstraints.PAGE_START;
		gc.gridx = 4;
		gc.gridy = 0;
		add(kField, gc);
		
		
		
		

		
		
		
		
		gc.anchor = GridBagConstraints.LAST_LINE_END;
		gc.weightx = 0.5;
		gc.weighty = 0.5;
		gc.gridx = 4;
		gc.gridy = 0;
		add(bValue, gc);
		
		gc.anchor = GridBagConstraints.PAGE_START;
		gc.gridx = 5;
		gc.gridy = 0;
		add(bField, gc);
		
		
		
		
		
		
		gc.anchor = GridBagConstraints.LINE_END;
		gc.gridx = 1;
		gc.gridy = 2;
		add(titleValue, gc);
		
		gc.anchor = GridBagConstraints.CENTER;
		gc.gridx = 2;
		gc.gridy = 2;
		add(titleField, gc);
		
		
		gc.anchor = GridBagConstraints.LINE_END;
		gc.gridx = 2;
		gc.gridy = 2;
		add(headerValue, gc);
		
		gc.anchor = GridBagConstraints.CENTER;
		gc.gridx = 3;
		gc.gridy = 2;
		add(headerField, gc);
		
		
		gc.anchor = GridBagConstraints.LINE_END;
		gc.gridx = 3;
		gc.gridy = 2;
		add(paragraphValue, gc);
		
		gc.anchor = GridBagConstraints.CENTER;
		gc.gridx = 4;
		gc.gridy = 2;
		add(paragraphField, gc);
		
		
		gc.anchor = GridBagConstraints.LINE_END;
		gc.gridx = 4;
		gc.gridy = 2;
		add(topValue, gc);
		
		gc.anchor = GridBagConstraints.CENTER;
		gc.gridx = 5;
		gc.gridy = 2;
		add(topField, gc);
		
		
		
		
		
		
		
		
		
		
		
	}
	
	public void fireDetailEvent(ReceiveEvent event)
	{
		Object[] listeners = listenerList.getListenerList();
		
		for(int i=0; i< listeners.length; i+=2){
			if(listeners[i] == Listener.class)
			{
				((Listener)listeners[i+1]).detailEventOccurred(event);
			}
		}
	}
	
	public void addDetailListener(Listener listener)
	{
		listenerList.add(Listener.class, listener);
	}
		
	public void removeDetailListener(Listener listener)
	{
		listenerList.remove(Listener.class, listener);
	}
}
