package com.onpremisecloudimplementation.vmorchestrator;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VmOrchestratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(VmOrchestratorApplication.class, args);
	}

    // Topics for creating instances
    @Bean
    NewTopic orchestratorNewInstanceTopic() {
        return TopicBuilder.name("orchestrator-new-instance-requests")
                .partitions(10)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic orchestratorNewInstanceResponsesTopic() {
        return TopicBuilder.name("orchestrator-new-instance-responses")
                .partitions(10)
                .replicas(1)
                .build();
    }

    // Topics for shutting down instances
    @Bean
    NewTopic orchestratorShutdownInstanceRequestsTopic() {
        return TopicBuilder.name("orchestrator-instance-shutdown-requests")
                .partitions(10)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic orchestratorShutdownInstanceResponsesTopic() {
        return TopicBuilder.name("orchestrator-instance-shutdown-responses")
                .partitions(10)
                .replicas(1)
                .build();
    }

    // Topics for deleting instances
    @Bean
    NewTopic orchestratorDeleteInstanceRequestsTopic() {
        return TopicBuilder.name("orchestrator-instance-delete-requests")
                .partitions(10)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic orchestratorDeleteInstanceResponsesTopic() {
        return TopicBuilder.name("orchestrator-instance-delete-responses")
                .partitions(10)
                .replicas(1)
                .build();
    }

}
