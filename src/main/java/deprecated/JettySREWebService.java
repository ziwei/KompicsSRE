package deprecated;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.QueuedThreadPool;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.web.jetty.JettyWebServerConfiguration;
import se.sics.kompics.web.jetty.JettyWebServerInit;

public class JettySREWebService extends ComponentDefinition {

	public JettySREWebService() throws Exception {
		subscribe(handleInit, control);

	}

	private Handler<JettyWebServerInit> handleInit = new Handler<JettyWebServerInit>() {
		public void handle(JettyWebServerInit init) {
			System.out.println("initing");
			JettyWebServerConfiguration config = init.getConfiguration();
			Server server = new Server(config.getPort());

			QueuedThreadPool qtp = new QueuedThreadPool();
			qtp.setMinThreads(1);
			qtp.setMaxThreads(config.getMaxThreads());
			qtp.setDaemon(true);
			server.setThreadPool(qtp);

			Connector connector = new SelectChannelConnector();
			connector.setHost(config.getIp().getCanonicalHostName());
			connector.setPort(config.getPort());
			server.setConnectors(new Connector[] { connector });

			WebAppContext bb = new WebAppContext();
	        bb.setServer(server);
	        bb.setContextPath("/");
	        bb.setWar("src/main/webapp");

	        server.addHandler(bb);
			try {
				System.out
						.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
				server.start();
				while (System.in.available() == 0) {
					Thread.sleep(5000);
				}
				server.stop();
				server.join();
			} catch (Exception e) {
				System.out
				.println(">>> failed EMBEDDED JETTY SERVER");
				e.printStackTrace();
				System.exit(100);
			}
		}
	};
}
