package mqtt;

//added external jar: c:\ada\work\lectures\slr203\mqtt\paho\paho-java-maven\org.eclipse.paho.client.mqttv3-1.2.5.jar 

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class SubscribingMqttClient {//synchronous client
	
	public static void main(String[] args) {
		
		String topic        = "labs/paho-example-topic";
	    int qos             = 0;
	    String brokerURI       = "tcp://localhost:1883";
	    String clientId     = "myClientID_Sub";
        boolean cleanSession    = true;
	    //MemoryPersistence persistence = new MemoryPersistence();
	    
	    
	    try(
	    	////instantiate a synchronous MQTT Client to connect to the targeted Mqtt Broker
	    	MqttClient mqttClient = new MqttClient(brokerURI, clientId);) {
	    	
	    	
	    	////specify the Mqtt Client's connection options
	    	MqttConnectOptions connectOptions = new MqttConnectOptions();
	    	//clean session 
	    	connectOptions.setCleanSession(cleanSession);
	    	//customise other connection options here...
            MyMqttCallback myMqttCallback = new MyMqttCallback();
            mqttClient.setCallback(myMqttCallback);
	    	
	    	////connect the mqtt client to the broker
	    	System.out.println("Mqtt Client: Connecting to Mqtt Broker running at: " + brokerURI);
	    	mqttClient.connect(connectOptions);
            System.out.println("Mqtt Client: sucessfully Connected.");
            

            // subscribe to a topic
            System.out.println("Mqtt Client: Subscribing to topic: " + topic);
            mqttClient.subscribe(topic, qos);
            System.out.println("Mqtt Client: successfully subscribed to the topic.");

            while(true) {
                // System.out.println("Mqtt Client: waiting for messages ...");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
            }
            
            
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

class MyMqttCallback implements MqttCallback {

    public void connectionLost(Throwable cause) {
        System.out.println("Mqtt Client: Connection lost: " + cause.getMessage());
    }

    public void messageArrived(String topic, MqttMessage message) {
        System.out.println("Mqtt Client: topic: " + topic);
        System.out.println("Mqtt Client: message: " + message.toString());
    }

    public void deliveryComplete(IMqttDeliveryToken token){
        try{
            System.out.println("Mqtt Client: deliveryComplete: " + token.getMessage());
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }
}
