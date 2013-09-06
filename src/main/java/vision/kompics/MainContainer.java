package vision.kompics;

import java.io.IOException;

import components.SREComponent;
import constant.SREConst;

import porttypes.SlRequest;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Kompics;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;

import web.SREJettyWebServer;
import web.SREJettyWebServerConfiguration;
import web.SREJettyWebServerInit;

public class MainContainer extends ComponentDefinition {
	public static void main(String[] args) {
		//PropertyConfigurator.configure(args[1]);
		Kompics.createAndStart(MainContainer.class, Integer.parseInt(SREConst.workerNumber));
	}

	public MainContainer() throws IOException {
		Component webServer = create(SREJettyWebServer.class);
		Component sre = create(SREComponent.class);
		Component timer = create(JavaTimer.class);
		SREJettyWebServerInit init = new SREJettyWebServerInit(
				new SREJettyWebServerConfiguration());
		connect(sre.getPositive(SlRequest.class),
				webServer.getNegative(SlRequest.class));
		
		connect(sre.getNegative(Timer.class),timer.getPositive(Timer.class));
		trigger(init, webServer.getControl());
		
	}
}
