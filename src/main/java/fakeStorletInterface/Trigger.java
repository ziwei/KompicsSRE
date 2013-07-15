package fakeStorletInterface;

import java.io.IOException;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

//import eu.visioncloud.storlet.common.evaluators.Evaluator;

@XmlRootElement
public final class Trigger {

	public static Trigger[] createTriggers(String input)
			throws StorletException {
		Trigger[] res = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			res = mapper.readValue(input, Trigger[].class);
		} catch (JsonParseException e) {
		} catch (JsonMappingException e) {
		} catch (IOException e) {
		}

		// if no return value try to unexcape things
		if (res == null) {
			input = Utils.jsonUnEscape(input);
			try {
				res = mapper.readValue(input, Trigger[].class);
			} catch (JsonParseException e) {
			} catch (JsonMappingException e) {
				throw new StorletException(e);
			} catch (IOException e) {
				
			}

		}
		if (res == null) throw new StorletException("No Triggerinfo found");
		return res;
	}

	public String getHandlerID() {
		return handlerID;
	}

	public void setHandlerID(String handlerID) {
		this.handlerID = handlerID;
	}

	public boolean isTriggerOnReplica() {
		return triggerOnReplica;
	}

	public void setTriggerOnReplica(boolean triggerOnReplica) {
		this.triggerOnReplica = triggerOnReplica;
	}

	public String getTriggerEvaluator() {
		return triggerEvaluator;
	}

	public void setTriggerEvaluator(String triggerEvaluator) {
		this.triggerEvaluator = triggerEvaluator;
	}

//	@JsonIgnore
//	private Evaluator getEvaluator() {
//		if (evaluator != null)
//			return evaluator;
//		try {
//			this.evaluator = Evaluator.evaluatorFromString(triggerEvaluator);
//		} catch (StorletException e) {
//			e.printStackTrace();
//		}
//		return evaluator;
//	}

	public String[] getTriggerTypes() {
		return triggerTypes;
	}

	public void setTriggerTypes(String[] triggerTypes) {
		this.triggerTypes = triggerTypes;
	}

	private String handlerID = null;
	private String[] triggerTypes = null;
	private boolean executeLocal = false;
	private boolean triggerOnReplica = false;
	private String triggerEvaluator = null;
	private String outputEvaluator = null;
//	@JsonIgnore
//	private Evaluator evaluator = null;

	/**
	 * Test if Event matches Trigger conditions
	 */
	@JsonIgnore
	public boolean evaluate(EventModel event) {
		if (false == Utils.equalityBooleans(triggerOnReplica, event.isrFlag()))
			return false;
		// TODO see if this works
		if (false == Arrays.asList(triggerTypes).contains(event.getEventType()))
			return false;
		return false; //evaluator.evaluate(event.getOldMetadata(),
				//event.getNewMetadata());
	}

	@Override
	@JsonIgnore
	public int hashCode() {
		return handlerID.hashCode();
	}

	@Override
	@JsonIgnore
	public boolean equals(Object object) {
		Trigger other = (Trigger) object;

		if (false == Utils.equalityGeneric(other.triggerEvaluator,
				this.handlerID))
			return false;

		if (false == Utils.equalityBooleans(other.triggerOnReplica,
				this.triggerOnReplica))
			return false;

		if (false == Utils.equalityBooleans(other.executeLocal,
				this.executeLocal))
			return false;

//		if (false == Utils.equalityGeneric(other.evaluator, this.evaluator))
//			return false;

		return true;
	}

	public boolean isExecuteLocal() {
		return executeLocal;
	}

	public void setExecuteLocal(boolean executeLocal) {
		this.executeLocal = executeLocal;
	}

	@Override
	@JsonIgnore
	public String toString() {
		String idString = "\tID : " + handlerID + "\n";
		String isReplicaString = "\tisReplica : " + triggerOnReplica + "\n";
		String isExecuteLocallyString = "\tisExecuteLocally : " + executeLocal
				+ "\n";
		String evaluatorBeforeString = "\tEvaluator : ";
				//+ getEvaluator().toString() + "\n";
		String triggerTypeString = "\tTriggerTypes: "
				+ Arrays.toString(triggerTypes) + "\n";
		return "{\n\t<TRIGGER>\n" + idString + triggerTypeString
				+ isReplicaString + isExecuteLocallyString
				+ evaluatorBeforeString + "}";
	}

	public String getOutputEvaluator() {
		return outputEvaluator;
	}

	public void setOutputEvaluator(String outputEvaluator) {
		this.outputEvaluator = outputEvaluator;
	}
}
