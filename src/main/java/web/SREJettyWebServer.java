package web;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import msgTypes.SyncSLActivation;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.thread.QueuedThreadPool;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.lf5.util.StreamUtils;

import components.StorletWrapper;

import porttypes.SlRequest;

import eu.visioncloud.storlet.common.EventModel;
import eu.visioncloud.storlet.common.Utils;
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

	private static final Logger logger = Logger
			.getLogger(SREJettyWebServer.class);
	private static final Logger benchLogger = Logger.getLogger("benchmark");
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
		String method = request.getMethod();
		String[] args = target.split("/");
		if (method.equals("POST") || method.equals("DELETE")) {
			long startTime = System.currentTimeMillis();
			String slID = "";
			String handler = "";
			String activationId = "";
			if (args.length >= 3) {
				slID = args[2];
			}
			if (args.length == 5) {
				handler = args[3];
				activationId = args[4];
			}

			if (slID == null) {
				return;
			}
			String header = request.getHeader("Accept");
			//System.out.println(header);
			String line;
			String body = "";
			while ((line = request.getReader().readLine()) != null) {
				body = body.concat(line);
			}
			generateEvent(method, handler, slID, activationId, header, body);
			long endTime = System.currentTimeMillis();
			//benchLogger.info((endTime - startTime));
		}
		else if (method.equals("GET")){
			if (args.length == 3){
				String targetLog = args[2];
				if (targetLog.equals("sre")){
					response.reset();
					copyFileToOutstream(targetLog, response.getOutputStream());
					response.flushBuffer();
				}
				else if (targetLog.equals("storlets")){
					response.reset();
					copyFileToOutstream(targetLog, response.getOutputStream());
					response.flushBuffer();
				}
			}
			else if (args.length == 5){
				if (args[2].equals("logFor")){
					if (args[3].equals("slID")){
						System.out.println(StorletWrapper.slLogTable.get(args[4]));
					}
					else if (args[3].equals("activationID")){
						System.out.println(args[4]);
					}
				}
			}
		}
	}
	private void copyFileToOutstream(String logName, OutputStream out) throws IOException{
	    BufferedInputStream is = new BufferedInputStream(new FileInputStream("/home/ziwei/workspace/KompicsSRE/"+logName+".log"));
	    byte[] buf = new byte[4 * 1024]; // 4K buffer
	    int bytesRead;
	    while ((bytesRead = is.read(buf)) != -1) {
	        out.write(buf, 0, bytesRead);
	    }
	    is.close();
	    out.flush();
	    out.close();
	}
	private void generateEvent(String method, String handler, String slID,
			String activationId, String header, String body) throws JsonParseException,
			JsonMappingException, IOException {
		ObjectMapper om = new ObjectMapper();

		if (method.equals("POST") && !handler.equals("") && header.equals("application/json")) {
			logger.info("Handling an Async Trigger with activation id "
					+ activationId + ", slID " + slID + ", handler " + handler);
			EventModel em = om.readValue(body, EventModel.class);
			trigger(new AsyncTrigger(slID, handler, em, activationId), sreWeb);
		} else if (method.equals("POST") && slID.equals("syncStorlet") && header.equals("application/json")) {
			logger.info("Handling an Sync Trigger");
			SyncSLActivation syncAct = om.readValue(body,
					SyncSLActivation.class);
			trigger(new SyncTrigger(syncAct), sreWeb);
		} else if (method.equals("DELETE")) {
			logger.info("Handling Deletion of slID " + slID);
			trigger(new SlDelete(slID), sreWeb);
		}
	}
}
