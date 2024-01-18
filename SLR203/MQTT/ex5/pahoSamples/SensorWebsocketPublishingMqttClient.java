// package mqtt;

//added external jar: c:\ada\work\lectures\slr203\mqtt\paho\paho-java-maven\org.eclipse.paho.client.mqttv3-1.2.5.jar 

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class SensorWebsocketPublishingMqttClient {//synchronous client
	
	public static void main(String[] args) {

		String topicPrefix = "/home/Lyon/sido/";
		String[] topics = {"dht22/value", "dht22/value2", "sht30/value", "sht30/value2"};
		int qos = 0;
	    // String brokerURI       = "ws://localhost:9001";
		String brokerURI = "ws://137.194.250.13:9001";
	    String clientId     = "sensor";
		boolean cleanSession = true;
		boolean retainedFlag = false;
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
	    	
	    	////connect the mqtt client to the broker
	    	System.out.println("Mqtt Client: Connecting to Mqtt Broker running at: " + brokerURI);
	    	mqttClient.connect(connectOptions);
            System.out.println("Mqtt Client: sucessfully Connected.");
            
            ////publish a message
			while(true){
			// for (int j = 0; j < 100; j++) {
				for (int i = 0; i < topics.length; i++) {
					int temperatureValue = (int) (Math.random() * 100);
					int humidityValue = (int) (Math.random() * 100);
					// int temperatureValue = 0;
					// int humidityValue = 0;
					int sensorValue = i % 2 == 0 ? temperatureValue : humidityValue;
					MqttMessage message = new MqttMessage(String.valueOf(sensorValue).getBytes());
					message.setQos(qos);
					message.setRetained(retainedFlag);
					mqttClient.publish(topicPrefix + topics[i], message);
					System.out.println("Mqtt Client: Publishing message: " + sensorValue + " to topic: " + topicPrefix + topics[i]);
				}
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
            
            //disconnect the Mqtt Client
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
