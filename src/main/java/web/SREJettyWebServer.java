/**
 * This file is part of the Kompics P2P Framework.
 * 
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS)
 * Copyright (C) 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package web;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import porttypes.SlRequest;

import events.SlDelete;
import events.SlOperation;
import events.SyncTrigger;
import events.AsyncTrigger;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Request;
import se.sics.kompics.web.Web;
import se.sics.kompics.web.WebRequest;
import se.sics.kompics.web.WebResponse;
import se.sics.kompics.web.jetty.JettyWebServerConfiguration;
import se.sics.kompics.web.jetty.JettyWebServerInit;

/**
 * The <code>JettyWebServer</code> class.
 * 
 * @author Cosmin Arad <cosmin@sics.se>
 * @version $Id: JettyWebServer.java 1134 2009-09-01 15:27:39Z Cosmin $
 */
public final class SREJettyWebServer extends ComponentDefinition {

	Positive<Web> web = positive(Web.class);
	Positive<SlRequest> sreWeb = positive(SlRequest.class);

	private static final Logger logger = LoggerFactory
			.getLogger(SREJettyWebServer.class);

	private final SREJettyWebServer thisWS = this;

	private String homePage;

	public SREJettyWebServer() {
		subscribe(handleInit, control);
	}

	private Handler<JettyWebServerInit> handleInit = new Handler<JettyWebServerInit>() {
		public void handle(JettyWebServerInit init) {
			logger.debug("Handling init in thread {}", Thread.currentThread());

			JettyWebServerConfiguration config = init.getConfiguration();
			
			homePage = config.getHomePage();
			if (homePage == null) {
				homePage = "<h1>Welcome!</h1>";
			}
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
				org.mortbay.jetty.Handler webHandler = new SREJettyHandler(thisWS);
				server.setHandler(webHandler);
				server.start();
				//System.out.println("init succ");
			} catch (Exception e) {
				throw new RuntimeException(
						"Cannot initialize the Jetty web server", e);
			}
		}
	};

	void handleRequest(String target, org.mortbay.jetty.Request request,
			HttpServletResponse response) throws IOException {
		logger.debug("Handling request {} in thread {}", target, Thread
				.currentThread());
		String[] args = target.split("/");
		String slID = null;
		String handler = "";
		String activationId = "";
		if (args.length >= 3) {
			System.out.println("target: "+args[2]);
			slID = args[2];
		}
		if (args.length == 5) {
			handler = args[3];
			activationId = args[4];
		}
		
		if (slID == null) {
			return;
		}

		logger.debug("Triggering request {}", target);

		String line;
		String body = "";
		String method = request.getMethod();
		while ((line = request.getReader().readLine()) != null) { 
		body = body.concat(line);
		}
		if (method.equals("POST")&&!handler.equals(""))
			trigger(new AsyncTrigger(slID, handler, body), sreWeb);
		else if (method.equals("POST")&&slID.equals("SyncStorlet"))
			trigger(new SyncTrigger(slID, body), sreWeb);
		else if (method.equals("DELETE"))
			trigger(new SlDelete(slID), sreWeb);
		
	}
}
