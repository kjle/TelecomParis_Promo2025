package mqtt;

//added external jar: c:\ada\work\lectures\slr203\mqtt\paho\paho-java-maven\org.eclipse.paho.client.mqttv3-1.2.5.jar 

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class PublishingMqttClient {//synchronous client
	
	public static void main(String[] args) {
		
		String topic        = "labs/paho-example-topic-new";
	    String messageContent = "Message from my Lab's Paho Mqtt Client";
	    int qos             = 0;
	    String brokerURI       = "tcp://localhost:1883";
	    String clientId     = "myClientID_Pub";
		boolean cleanSession    = true;
		boolean retainFlag = true;

		String willTopic	= "labs/paho-example-topic-will";
		String willContent	= "Last Will and Testament (LWT) message from my Lab's Paho Mqtt Client";
		int willQos			= 1;
		boolean willRetainFlag = false;
	    //MemoryPersistence persistence = new MemoryPersistence();
	    
	    
	    try(
	    	////instantiate a synchronous MQTT Client to connect to the targeted Mqtt Broker
	    	MqttClient mqttClient = new MqttClient(brokerURI, clientId);) {
	    	
	    	
	    	////specify the Mqtt Client's connection options
	    	MqttConnectOptions connectOptions = new MqttConnectOptions();
	    	//clean session 
	    	connectOptions.setCleanSession(cleanSession);
	    	//customise other connection options here...
	    	//...

			// Last Will and Testament (LWT)
			connectOptions.setWill(willTopic, willContent.getBytes(), willQos, willRetainFlag);
	    	
	    	////connect the mqtt client to the broker
	    	System.out.println("Mqtt Client: Connecting to Mqtt Broker running at: " + brokerURI);
	    	mqttClient.connect(connectOptions);
            System.out.println("Mqtt Client: sucessfully Connected.");

			////publish a message to topic
			messageContent = "Message from my Lab's Paho Mqtt Client";
			System.out.println("Mqtt Client: Publishing message : " + messageContent);
			MqttMessage message = new MqttMessage(messageContent.getBytes());//instantiate the message including its content (payload)
			message.setQos(qos);//set the message's QoS
			message.setRetained(retainFlag);
			mqttClient.publish(topic, message);//publish the message to a given topic
			System.out.println("Mqtt Client: successfully published the message.");

			try {
			        System.out.println("Sleeping for 50 seconds... \nKill the publisher before the 50 seconds are up to see the LWT message.");
			        Thread.sleep(50000);
			    } catch (InterruptedException e) {
			        e.printStackTrace();
			}
            
            ////disconnect the Mqtt Client
            // mqttClient.disconnect();
            // System.out.println("Mqtt Client: Disconnected.");
	    }
	    catch(MqttException e) {
	    	System.out.println("Mqtt Exception reason: " + e.getReasonCode());
            System.out.println("Mqtt Exception message: " + e.getMessage());
            System.out.println("Mqtt Exception location: " + e.getLocalizedMessage());
            System.out.println("Mqtt Exception cause: " + e.getCause());
            System.out.println("Mqtt Exception reason: " + e);
            e.printStackTrace();
	    }
    
	}
    
    

}
