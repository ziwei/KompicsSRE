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
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import se.sics.kompics.web.Web;
import se.sics.kompics.web.jetty.JettyWebServer;

/**
 * The <code>JettyHandler</code> class.
 * 
 * @author Cosmin Arad <cosmin@sics.se>
 * @version $Id: JettyHandler.java 3862 2010-12-11 21:13:14Z Cosmin $
 */
final class SREJettyHandler extends AbstractHandler {

	private final SREJettyWebServer webComponent;

	private static final int FAVICON_LENGTH = 4286;

	private final byte[] favicon;

	public SREJettyHandler(SREJettyWebServer webComponent) throws IOException {
		super();
		
		this.webComponent = webComponent;
		InputStream iconStrem = JettyWebServer.class//Custom web server has no icon
				.getResourceAsStream("favicon.ico");

		favicon = new byte[FAVICON_LENGTH];
		int ret = iconStrem.read(favicon);
		if (ret == -1)
			throw new RuntimeException("Cannot read icon file");
	}

	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) throws IOException,
			ServletException {

		Request base_request = (request instanceof Request) ? (Request) request
				: HttpConnection.getCurrentConnection().getRequest();
		base_request.setHandled(true);

		if (target.equals("/favicon.ico")) {
			response.setContentType("image/x-icon");
			response.setContentLength(FAVICON_LENGTH);
			response.setStatus(HttpServletResponse.SC_OK);
			response.getOutputStream().write(favicon);
		} else if (target.startsWith("/resource/")) {
			String resource = target.substring(10); 
			InputStream restream = Web.class.getResourceAsStream(resource);
			byte[] b = new byte[4096];
			int ret = 0;
			do {
				ret = restream.read(b);
				if (ret <= 0) continue;
				response.getOutputStream().write(b, 0, ret);
			} while (ret > 0);
			response.setStatus(HttpServletResponse.SC_OK);
			response.getOutputStream().close();
		} else {
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println(
					"<head><link rel=\"icon\" href=\"/favicon."
							+ "ico\" type=\"image/x-icon\" /></head>");

			webComponent.handleRequest(target, base_request, response);
		}
	}
}
