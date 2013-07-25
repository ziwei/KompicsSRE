package web;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import msgTypes.SyncSLActivation;

import org.codehaus.jackson.map.ObjectMapper;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.thread.QueuedThreadPool;
import org.apache.log4j.Logger; 
import org.apache.log4j.PropertyConfigurator;

import porttypes.SlRequest;

import eu.visioncloud.storlet.common.EventModel;
import events.SlDelete;
import events.SyncTrigger;
import events.AsyncTrigger;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.web.Web;
import se.sics.kompics.web.jetty.JettyWebServerConfiguration;
import se.sics.kompics.web.jetty.JettyWebServerInit;

public final class SREJettyWebServer extends ComponentDefinition {

	Positive<Web> web = positive(Web.class);
	Positive<SlRequest> sreWeb = positive(SlRequest.class);

	private static final Logger logger = Logger.getLogger(SREJettyWebServer.class);

	private final SREJettyWebServer thisWS = this;

	public SREJettyWebServer() {
		PropertyConfigurator.configure("log4j.properties");
		subscribe(handleInit, control);
	}

	private Handler<JettyWebServerInit> handleInit = new Handler<JettyWebServerInit>() {
		public void handle(JettyWebServerInit init) {
			logger.info("Handling init");
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
			try {
				org.mortbay.jetty.Handler webHandler = new SREJettyHandler(
						thisWS);
				server.setHandler(webHandler);
				server.start();
				// System.out.println("init succ");
			} catch (Exception e) {
				throw new RuntimeException(
						"Cannot initialize the Jetty web server", e);
			}
		}
	};
	
	
	
	
	
	void handleRequest(String target, org.mortbay.jetty.Request request,
			HttpServletResponse response) throws IOException {
		
		String[] args = target.split("/");
		String slID = "";
		String handler = "";
		String activationId = "";
		if (args.length >= 3) {
			//System.out.println("target: " + args[2]);
			slID = args[2];
		}
		if (args.length == 5) {
			handler = args[3];
			activationId = args[4];
		}

		if (slID == null) {
			return;
		}
		
		String line;
		String body = "";
		String method = request.getMethod();
		while ((line = request.getReader().readLine()) != null) {
			body = body.concat(line);
		}
		//logger.info("Handling request: " + method.equals("POST") + slID.equals("syncStorlet"));
		ObjectMapper om = new ObjectMapper();
		
		if (method.equals("POST") && !handler.equals("")){
			logger.info("Handling an Async Trigger with activation id " + 
		activationId + ", slID "+ slID + ", handler " + handler);
			EventModel em = om.readValue(body, EventModel.class);
			trigger(new AsyncTrigger(slID, handler, em, activationId), sreWeb);
		}
		else if (method.equals("POST") && slID.equals("syncStorlet")){
			logger.info("Handling an Sync Trigger");
			SyncSLActivation syncAct = om.readValue(body, SyncSLActivation.class);
			trigger(new SyncTrigger(syncAct), sreWeb);
		}
		else if (method.equals("DELETE")){
			logger.info("Handling Deletion of slID " + slID);
			trigger(new SlDelete(slID), sreWeb);
		}

	}
}
