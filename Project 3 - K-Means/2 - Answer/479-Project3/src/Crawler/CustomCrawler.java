package Crawler;
// MyCrawler.java

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import websphinx.Crawler;
import websphinx.DownloadParameters;
import websphinx.Page;

public abstract class CustomCrawler extends Crawler {

	private static final long serialVersionUID = 2383514014091378008L;

	protected final Log log = LogFactory.getLog(getClass());

	public CustomCrawler() {
		super();
		DownloadParameters dp = new DownloadParameters();
		dp.changeObeyRobotExclusion(true);
		dp.changeUserAgent("MyCrawler Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.8.1.4) " + 
				"WebSPHINX 0.5 contact me_at_mycompany_dot_com");
		setDownloadParameters(dp);
		setDomain(Crawler.SUBTREE);
		setLinkType(Crawler.HYPERLINKS);
	}

	@Override
	public void visit(Page page)
	{
		doVisit(page);
		try
		{
			Thread.sleep(1000L);
		}
		catch (InterruptedException e)
		{
			
		}
	}

	/**
	 * Extend me, not visit(Page)!
	 */
	protected abstract void doVisit(Page page);
	
}