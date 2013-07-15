package storlets4testing;


import porttypes.SlRequest;

import events.AsyncTrigger;
import events.StorletInit;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;


public class TestStorlet1 extends ComponentDefinition {
	Negative<SlRequest> sreWeb = negative(SlRequest.class);
	int messages = 0;
	public TestStorlet1(){
		subscribe(handleInit, control);
		subscribe(slTriggerH, sreWeb);
	}
	
	private Handler<StorletInit> handleInit = new Handler<StorletInit>() {
		public void handle(StorletInit init) {
			//System.out.println("Storlet " + this.getClass().getSimpleName()+" init");
		}
	};
	
	Handler<AsyncTrigger> slTriggerH = new Handler<AsyncTrigger>() {
	    public void handle(AsyncTrigger slEvent) {
	    	messages++;
	    	System.out.println("Storlet" + this.getClass().getSimpleName()+" got " + messages+ " msgs");
	    	//System.out.println("Storlet1 name "+slEvent.getContent());
	    	//System.out.println("Storlet1 handler "+slEvent.getHandler());
	    	//System.out.println("Storlet1 id "+slEvent.getStorletId());
	    	
	          // trigger(new PongMessage(self, event.getSource()), network );
	          // System.out.println("[Address]: "+self+" sent a pong  message to "+ event.getSource());
	        }
	    };
}
