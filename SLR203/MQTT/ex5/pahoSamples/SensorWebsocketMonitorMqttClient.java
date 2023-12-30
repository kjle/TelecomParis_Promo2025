// @description calculate the average temperature and humidity values and send them to the broker
// package mqtt;

//added external jar: c:\ada\work\lectures\slr203\mqtt\paho\paho-java-maven\org.eclipse.paho.client.mqttv3-1.2.5.jar 

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class SensorWebsocketMonitorMqttClient {//synchronous client
	
	public static void main(String[] args) {
		
		String topicPrefix = "/home/Lyon/sido/";
		String[] topics = {"dht22/value", "dht22/value2", "sht30/value", "sht30/value2", "average/value", "average/value2"};
		int qos = 0;
	    String brokerURI       = "ws://localhost:9001";
	    String clientId     = "monitor";
		boolean cleanSession = true;

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
            for (int i = 0; i < topics.length; i++) {
                String topic = topicPrefix + topics[i];
                System.out.println("Mqtt Client: Subscribing to topic: " + topic);
                mqttClient.subscribe(topic, qos);
                System.out.println("Mqtt Client: successfully subscribed to the topic.");
            }


            while(true) {
                
                // send average values to the broker
                double averageTemperature = myMqttCallback.getAverageTemperature();
                double averageHumidity = myMqttCallback.getAverageHumidity();
                MqttMessage message = new MqttMessage(String.valueOf(averageTemperature).getBytes());
                message.setQos(qos);
                message.setRetained(false);
                mqttClient.publish(topicPrefix + "average/value", message);

                message = new MqttMessage(String.valueOf(averageHumidity).getBytes());
                message.setQos(qos);
                message.setRetained(false);
                mqttClient.publish(topicPrefix + "average/value2", message);

                try {
                    Thread.sleep(1000);
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

    private String topic;
    private String message;

    private static final int MAX_VALUES = 100;

    private double[] temperatureValues = new double[MAX_VALUES];
    private double[] humidityValues = new double[MAX_VALUES];
    private int tempIndex = 0;
    private int humidityIndex = 0;
    private int tempCount = 0;
    private int humidityCount = 0;
    private double averageTemperature = 0;
    private double averageHumidity = 0;

    public void connectionLost(Throwable cause) {
        System.out.println("Mqtt Client: Connection lost: " + cause.getMessage());
    }

    public void messageArrived(String topic, MqttMessage message) {
        this.topic = topic;
        this.message = message.toString();
        System.out.println("Mqtt Client: topic: " + topic);
        System.out.println("Mqtt Client: message: " + message.toString());

        double value = Double.parseDouble(new String(message.getPayload()));

        if (topic.endsWith("value")) {
            temperatureValues[tempIndex] = value;
            tempIndex = (tempIndex + 1) % MAX_VALUES;
            if (tempCount < MAX_VALUES) tempCount++;
        } else {
            humidityValues[humidityIndex] = value;
            humidityIndex = (humidityIndex + 1) % MAX_VALUES;
            if (humidityCount < MAX_VALUES) humidityCount++;
        }

        averageTemperature = calculateAverage(temperatureValues, tempCount);
        averageHumidity = calculateAverage(humidityValues, humidityCount);

        System.out.println("Average Temperature: " + averageTemperature);
        System.out.println("Average Humidity: " + averageHumidity);

    }

    public void deliveryComplete(IMqttDeliveryToken token){
        try{
            System.out.println("Mqtt Client: deliveryComplete: " + token.getMessage());
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }

    public String getTopic() {
        return this.topic;
    }

    public String getMessage() {
        return this.message;
    }

    private double calculateAverage(double[] values, int count) {
        double sum = 0;
        for (int i = 0; i < count; i++) {
            sum += values[i];
        }
        return count > 0 ? sum / count : 0;
    }

    public double getAverageTemperature() {
        return this.averageTemperature;
    }

    public double getAverageHumidity() {
        return this.averageHumidity;
    }
}
