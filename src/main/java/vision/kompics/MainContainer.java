package vision.kompics;

import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;

import components.SREComponent;

import porttypes.SlRequest;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Kompics;
import se.sics.kompics.web.jetty.JettyWebServerConfiguration;
import se.sics.kompics.web.jetty.JettyWebServerInit;
import util.Configurator;

import web.SREJettyWebServer;
import web.SREJettyWebServerConfiguration;
import web.SREJettyWebServerInit;

public class MainContainer extends ComponentDefinition {
	public static void main(String[] args) {
		//Configurator.init(args[0], args[1]);
		Configurator.init("/home/ziwei/workspace/KompicsSRE/deployment/", "SREEnv.config");
		//PropertyConfigurator.configure(args[1]);
		Kompics.createAndStart(MainContainer.class, Integer.parseInt(Configurator.config("workers")));
	}

	public MainContainer() throws IOException {
		Component webServer = create(SREJettyWebServer.class);
		Component sre = create(SREComponent.class);
		SREJettyWebServerInit init = new SREJettyWebServerInit(
				new SREJettyWebServerConfiguration());
		connect(sre.getPositive(SlRequest.class),
				webServer.getNegative(SlRequest.class));
		trigger(init, webServer.getControl());
		
	}
}
