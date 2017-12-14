package UI;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class MainFrame extends JFrame {
	
	private static final long serialVersionUID = 9161755168067822140L;
	
	private SearchEnginePanel searchEnginePanel;
	
	public MainFrame(String title)
	{
		super(title);
		
		//Set Layout Manager 
		setLayout(new BorderLayout());
		
		
		JPanel bottomPanel = new JPanel ();
	    bottomPanel.setBorder ( new TitledBorder ( new EtchedBorder (), "Search Results" ));
		
	    
		//Create Text Area
		final JTextArea textArea = new JTextArea(40,70);		
		textArea.setEditable(false);
		
		//Putting text Area inside Scroll bar
		JScrollPane scroll = new JScrollPane(textArea);
		scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		bottomPanel.add(scroll);
		
		searchEnginePanel = new SearchEnginePanel();
		
		searchEnginePanel.addDetailListener(new Listener()
		{
			public void detailEventOccurred(ReceiveEvent event)
			{
				String text = event.getText();
				
				textArea.append(text);
			}
		});
		
		//Add Swing components to content pane
		Container c = getContentPane();
		
		c.add(bottomPanel, BorderLayout.CENTER);
		c.add(searchEnginePanel, BorderLayout.NORTH);
		
	}
}
