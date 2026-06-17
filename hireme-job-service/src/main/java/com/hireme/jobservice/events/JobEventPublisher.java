package com.hireme.jobservice.events;

import com.hireme.jobservice.domain.entities.JobOffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Publie {@code JOB_PUBLISHED} sur Kafka quand une offre est mise en ligne. */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishJobPublished(JobOffer offer) {
        JobPublishedEvent event = new JobPublishedEvent(offer.getId(), consolidateText(offer));
        kafkaTemplate.send(JobPublishedEvent.TOPIC, offer.getId().toString(), event);
        log.info("Événement JOB_PUBLISHED publié pour l'offre {}", offer.getId());
    }

    private String consolidateText(JobOffer offer) {
        String skills = Optional.ofNullable(offer.getRequiredSkills()).orElse(java.util.Set.of()).stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + " " + b)
                .orElse("");
        return String.join(" ",
                Optional.ofNullable(offer.getTitle()).orElse(""),
                Optional.ofNullable(offer.getDescription()).orElse(""),
                skills).trim();
    }
}
