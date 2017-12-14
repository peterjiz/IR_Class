package UI;

import java.util.EventListener;

/**
 * Connection between UI and Java Code
 */
public interface Listener extends EventListener
{
	public void detailEventOccurred(ReceiveEvent event);
}
