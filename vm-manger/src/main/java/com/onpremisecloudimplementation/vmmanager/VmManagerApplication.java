package com.onpremisecloudimplementation.vmmanager;

import java.util.UUID;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.onpremisecloudimplementation.vmmanager.image.ImageRepository;
import com.onpremisecloudimplementation.vmmanager.image.dao.Image;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class VmManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VmManagerApplication.class, args);
    }

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

    @Bean
    NewTopic cloudDatasourceConfigRequestsTopic() {
        return TopicBuilder.name("clouddatasource-instance-config-save-requests")
                .partitions(10)
                .replicas(1)
                .build();
    }
    
    @Bean
    NewTopic cloudDatasourceConfigDeleteTopic() {
        return TopicBuilder.name("clouddatasource-instance-config-delete-requests")
                .partitions(10)
                .replicas(1)
                .build();
    }

    @Bean
    CommandLineRunner runner(ImageRepository imageRepository) {
        return args -> {
            log.info("*** Running method after startup");
            Image image = new Image();
            image.setDownloadUrl("http://mockup.image.com");
            image.setId(UUID.fromString("6d4f7d83-910e-4ea8-95c9-0861f7223046"));
            imageRepository.save(image);
        };
    }

}
