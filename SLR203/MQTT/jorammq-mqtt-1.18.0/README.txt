JoramMQ fully supports MQTT (versions 3.1, 3.1.1 and 5.0) and
JMS (1.0, 1.0.1 and 2.0), enabling MQTT and JMS clients to interoperate.

This version is packaged for use as an MQTT broker.
The detailed features provided by the product, its system requirements,
its configuration rules and execution commands, all are described in the
documentation which can be found in the doc/JoramMQ-MQTT-User-Manual.pdf file.

If a high message rate per MQTT publisher is expected at QoS levels 1 or 2, then 
the messages should be asynchronously published and the acknowledgments returned 
by the broker should be concurrently handled, otherwise the message rate per 
producer is limited by the transaction buffer timeout (cf property
Transaction.BufferTimeout).

If a high message rate per MQTT subscription is expected (e.g. when subscribing
to '#'), then a shared subscription should be used for load balancing.
