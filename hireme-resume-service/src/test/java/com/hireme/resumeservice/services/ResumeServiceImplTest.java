package com.hireme.resumeservice.services;

import com.hireme.resumeservice.domain.entities.Language;
import com.hireme.resumeservice.domain.entities.Resume;
import com.hireme.resumeservice.domain.entities.Skill;
import com.hireme.resumeservice.domain.enums.Visibility;
import com.hireme.resumeservice.dtos.resume.ResumeRequest;
import com.hireme.resumeservice.dtos.resume.ResumeResponse;
import com.hireme.resumeservice.events.ResumeEventPublisher;
import com.hireme.resumeservice.exception.ApiException;
import com.hireme.resumeservice.exception.ErrorCode;
import com.hireme.resumeservice.repositories.ContactRepository;
import com.hireme.resumeservice.repositories.LanguageRepository;
import com.hireme.resumeservice.repositories.ResumeRepository;
import com.hireme.resumeservice.repositories.SkillRepository;
import com.hireme.resumeservice.repositories.TemplateRepository;
import com.hireme.resumeservice.services.impl.ResumeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du ResumeService (schéma Arrange / Act / Assert).
 * Tous les repositories et le publisher Kafka sont mockés : la logique métier (unicité
 * du slug, résolution des dépendances, association compétences/langues, publication
 * de l'événement RESUME_UPDATED) est isolée, sans contexte Spring ni base de données.
 */
@ExtendWith(MockitoExtension.class)
class ResumeServiceImplTest {

    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private ContactRepository contactRepository;
    @Mock
    private TemplateRepository templateRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private LanguageRepository languageRepository;
    @Mock
    private ResumeEventPublisher resumeEventPublisher;

    @InjectMocks
    private ResumeServiceImpl service;

    private ResumeRequest request(String slug) {
        return new ResumeRequest(
                UUID.randomUUID(), null, null,
                "Développeur Java", slug, "Résumé pro", Visibility.PUBLIC);
    }

    private Resume resume(UUID id, String slug) {
        return Resume.builder()
                .id(id)
                .userId(UUID.randomUUID())
                .title("Développeur Java")
                .portfolioSlug(slug)
                .summary("Résumé pro")
                .visibility(Visibility.PUBLIC)
                .skills(new HashSet<>())
                .languages(new HashSet<>())
                .build();
    }

    @Test
    @DisplayName("findById retourne le CV quand il existe")
    void findById_returnsResume_whenFound() {
        UUID id = UUID.randomUUID();
        when(resumeRepository.findById(id)).thenReturn(Optional.of(resume(id, "jean-dev")));

        ResumeResponse res = service.findById(id);

        assertEquals(id, res.id());
        assertEquals("jean-dev", res.portfolioSlug());
    }

    @Test
    @DisplayName("findById lève RESUME_NOT_FOUND quand le CV est absent")
    void findById_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(resumeRepository.findById(id)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.findById(id));
        assertEquals(ErrorCode.RESUME_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("findBySlug lève RESUME_NOT_FOUND pour un slug inconnu")
    void findBySlug_throwsNotFound_whenMissing() {
        when(resumeRepository.findByPortfolioSlug("inconnu")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.findBySlug("inconnu"));
        assertEquals(ErrorCode.RESUME_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("findAll filtre par userId quand il est fourni")
    void findAll_filtersByUserId_whenProvided() {
        UUID userId = UUID.randomUUID();
        when(resumeRepository.findByUserId(userId))
                .thenReturn(List.of(resume(UUID.randomUUID(), "a"), resume(UUID.randomUUID(), "b")));

        assertEquals(2, service.findAll(userId).size());
        verify(resumeRepository).findByUserId(userId);
        verify(resumeRepository, never()).findAll();
    }

    @Test
    @DisplayName("findAll retourne tous les CV quand userId est null")
    void findAll_returnsAll_whenUserIdNull() {
        when(resumeRepository.findAll()).thenReturn(List.of(resume(UUID.randomUUID(), "a")));

        assertEquals(1, service.findAll(null).size());
        verify(resumeRepository).findAll();
    }

    @Test
    @DisplayName("create persiste le CV et publie RESUME_UPDATED")
    void create_savesAndPublishesEvent() {
        when(resumeRepository.existsByPortfolioSlug("jean-dev")).thenReturn(false);
        when(resumeRepository.save(any(Resume.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(request("jean-dev"));

        verify(resumeRepository).save(any(Resume.class));
        verify(resumeEventPublisher).publishResumeUpdated(any(Resume.class));
    }

    @Test
    @DisplayName("create lève SLUG_ALREADY_EXISTS si le slug est déjà pris")
    void create_throwsConflict_whenSlugTaken() {
        when(resumeRepository.existsByPortfolioSlug("jean-dev")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> service.create(request("jean-dev")));
        assertEquals(ErrorCode.SLUG_ALREADY_EXISTS, ex.getErrorCode());
        verify(resumeRepository, never()).save(any());
        verify(resumeEventPublisher, never()).publishResumeUpdated(any());
    }

    @Test
    @DisplayName("create lève CONTACT_NOT_FOUND quand le contact référencé est introuvable")
    void create_throwsContactNotFound_whenContactMissing() {
        UUID contactId = UUID.randomUUID();
        ResumeRequest req = new ResumeRequest(
                UUID.randomUUID(), contactId, null, "Titre", "slug", "résumé", Visibility.PUBLIC);
        when(resumeRepository.existsByPortfolioSlug("slug")).thenReturn(false);
        when(contactRepository.findById(contactId)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.create(req));
        assertEquals(ErrorCode.CONTACT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("update lève SLUG_ALREADY_EXISTS quand on passe à un slug déjà utilisé par un autre CV")
    void update_throwsConflict_whenSwitchingToTakenSlug() {
        UUID id = UUID.randomUUID();
        when(resumeRepository.findById(id)).thenReturn(Optional.of(resume(id, "ancien-slug")));
        when(resumeRepository.existsByPortfolioSlug("nouveau-slug")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class,
                () -> service.update(id, request("nouveau-slug")));
        assertEquals(ErrorCode.SLUG_ALREADY_EXISTS, ex.getErrorCode());
        verify(resumeRepository, never()).save(any());
    }

    @Test
    @DisplayName("update conserve le même slug sans déclencher le contrôle d'unicité")
    void update_keepsSameSlug_skipsUniquenessCheck() {
        UUID id = UUID.randomUUID();
        when(resumeRepository.findById(id)).thenReturn(Optional.of(resume(id, "jean-dev")));
        when(resumeRepository.save(any(Resume.class))).thenAnswer(inv -> inv.getArgument(0));

        service.update(id, request("jean-dev"));

        verify(resumeRepository, never()).existsByPortfolioSlug(any());
        verify(resumeEventPublisher).publishResumeUpdated(any(Resume.class));
    }

    @Test
    @DisplayName("delete supprime le CV quand il existe")
    void delete_removesResume_whenExists() {
        UUID id = UUID.randomUUID();
        when(resumeRepository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(resumeRepository).deleteById(id);
    }

    @Test
    @DisplayName("delete lève RESUME_NOT_FOUND quand le CV n'existe pas")
    void delete_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(resumeRepository.existsById(id)).thenReturn(false);

        assertThrows(ApiException.class, () -> service.delete(id));
        verify(resumeRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("addSkill associe la compétence au CV et le sauvegarde")
    void addSkill_associatesSkill() {
        UUID resumeId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        Resume resume = resume(resumeId, "jean-dev");
        Skill skill = Skill.builder().id(skillId).title("Java").build();
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(skill));
        when(resumeRepository.save(any(Resume.class))).thenAnswer(inv -> inv.getArgument(0));

        service.addSkill(resumeId, skillId);

        assertTrue(resume.getSkills().contains(skill));
        verify(resumeRepository).save(resume);
    }

    @Test
    @DisplayName("addSkill lève SKILL_NOT_FOUND quand la compétence est introuvable")
    void addSkill_throwsSkillNotFound_whenMissing() {
        UUID resumeId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume(resumeId, "jean-dev")));
        when(skillRepository.findById(skillId)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.addSkill(resumeId, skillId));
        assertEquals(ErrorCode.SKILL_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("removeLanguage retire la langue associée au CV")
    void removeLanguage_detachesLanguage() {
        UUID resumeId = UUID.randomUUID();
        UUID langId = UUID.randomUUID();
        Resume resume = resume(resumeId, "jean-dev");
        Language language = Language.builder().id(langId).title("Anglais").build();
        resume.getLanguages().add(language);
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));
        when(languageRepository.findById(langId)).thenReturn(Optional.of(language));
        when(resumeRepository.save(any(Resume.class))).thenAnswer(inv -> inv.getArgument(0));

        service.removeLanguage(resumeId, langId);

        assertFalse(resume.getLanguages().contains(language));
        verify(resumeRepository).save(resume);
    }
}
