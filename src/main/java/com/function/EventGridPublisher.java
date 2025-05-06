package com.function;


import com.microsoft.azure.functions.ExecutionContext;
import com.azure.messaging.eventgrid.*;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;


public class EventGridPublisher {
    private static final String topicEndpoint = "https://g14-eventgrid-users.eastus2-1.eventgrid.azure.net/api/events";
    private static final String topicKey = "69U83H2CGnV7iBqYRpNrL0VyQKjhHlTzrOSLp7u9RlwPUUyqhg91JQQJ99BEACHYHv6XJ3w3AAABAZEGwNUG";

    public static void publishEvent(String eventType, String subject, Object data, ExecutionContext context) {
        try {
            EventGridPublisherClient<EventGridEvent> client = new EventGridPublisherClientBuilder()
                    .endpoint(topicEndpoint)
                    .credential(new AzureKeyCredential(topicKey))
                    .buildEventGridEventPublisherClient();
    
            EventGridEvent event = new EventGridEvent(
                    subject,
                    eventType,
                    BinaryData.fromObject(data), 
                    "1.0"
            );
    
            client.sendEvent(event);
            context.getLogger().info("Evento enviado a Event Grid: " + eventType);
        } catch (Exception e) {
            context.getLogger().severe("Error enviando evento a Event Grid: " + e.getMessage());
        }
    }
}
