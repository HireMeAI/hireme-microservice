package com.hireme.resumeservice.events;

import com.hireme.resumeservice.domain.entities.Resume;
import com.hireme.resumeservice.domain.entities.Skill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Publie {@code RESUME_UPDATED} sur Kafka après création/mise à jour d'un CV. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishResumeUpdated(Resume resume) {
        ResumeUpdatedEvent event = new ResumeUpdatedEvent(
                resume.getId(), resume.getUserId(), consolidateText(resume), List.of());
        kafkaTemplate.send(ResumeUpdatedEvent.TOPIC, resume.getId().toString(), event);
        log.info("Événement RESUME_UPDATED publié pour le CV {}", resume.getId());
    }

    /** Texte consolidé pour le matching : titre + résumé + intitulés de compétences. */
    private String consolidateText(Resume resume) {
        String skills = Optional.ofNullable(resume.getSkills()).orElse(java.util.Set.of()).stream()
                .map(Skill::getTitle)
                .collect(Collectors.joining(" "));
        return String.join(" ",
                Optional.ofNullable(resume.getTitle()).orElse(""),
                Optional.ofNullable(resume.getSummary()).orElse(""),
                skills).trim();
    }
}
