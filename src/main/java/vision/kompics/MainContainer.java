package vision.kompics;

import java.net.InetAddress;
import java.net.UnknownHostException;

import components.SREComponent;

import porttypes.SlRequest;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Kompics;
import se.sics.kompics.web.Web;
import se.sics.kompics.web.jetty.JettyWebServer;
import se.sics.kompics.web.jetty.JettyWebServerConfiguration;
import se.sics.kompics.web.jetty.JettyWebServerInit;
import web.SREJettyWebServer;

public class MainContainer extends ComponentDefinition {
	public static void main(String[] args) {
		//selfId = Integer.parseInt(args[0]);
		Kompics.createAndStart(MainContainer.class, 8);
	}
	
	public MainContainer() throws UnknownHostException {
		Component webServer = create(SREJettyWebServer.class);
		//Configuration config = new Configuration();
		Component sre = create(SREComponent.class);
		
		JettyWebServerConfiguration jwsc = new JettyWebServerConfiguration(InetAddress.getLocalHost(),8080,1,2,"<h2>This is my page</h2>");
		JettyWebServerInit init = new JettyWebServerInit(jwsc);
		connect(sre.getPositive(SlRequest.class), webServer.getNegative(SlRequest.class));
		trigger(init, webServer.getControl());
		System.out.println("MyFirst Kompics App");
	}
}
