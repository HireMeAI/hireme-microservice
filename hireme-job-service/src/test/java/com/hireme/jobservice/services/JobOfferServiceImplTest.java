package com.hireme.jobservice.services;

import com.hireme.jobservice.domain.entities.JobOffer;
import com.hireme.jobservice.domain.enums.ContractType;
import com.hireme.jobservice.domain.enums.JobStatus;
import com.hireme.jobservice.domain.enums.RemotePolicy;
import com.hireme.jobservice.dtos.JobOfferRequest;
import com.hireme.jobservice.dtos.JobOfferResponse;
import com.hireme.jobservice.events.JobEventPublisher;
import com.hireme.jobservice.exception.ApiException;
import com.hireme.jobservice.exception.ErrorCode;
import com.hireme.jobservice.repositories.JobOfferRepository;
import com.hireme.jobservice.services.impl.JobOfferServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du JobOfferService (schéma Arrange / Act / Assert).
 * Le repository et le publisher Kafka sont mockés : la logique métier (valeurs par
 * défaut, filtres de recherche, publication d'événement, mises à jour partielles)
 * est isolée, sans contexte Spring ni base de données.
 */
@ExtendWith(MockitoExtension.class)
class JobOfferServiceImplTest {

    @Mock
    private JobOfferRepository repository;

    @Mock
    private JobEventPublisher jobEventPublisher;

    @InjectMocks
    private JobOfferServiceImpl service;

    private JobOfferRequest baseRequest() {
        JobOfferRequest req = new JobOfferRequest();
        req.setRecruiterId(UUID.randomUUID());
        req.setTitle("Développeur Java");
        req.setCompany("HireMe");
        req.setContractType(ContractType.FULL_TIME);
        return req;
    }

    private JobOffer offerWith(JobStatus status, ContractType contract, RemotePolicy remote,
                               String title, String company) {
        return JobOffer.builder()
                .id(UUID.randomUUID())
                .recruiterId(UUID.randomUUID())
                .title(title)
                .company(company)
                .contractType(contract)
                .remotePolicy(remote)
                .status(status)
                .requiredSkills(Set.of())
                .build();
    }

    @Test
    @DisplayName("create applique les valeurs par défaut (DRAFT, ON_SITE) et ne publie rien")
    void create_appliesDefaults_andDoesNotPublishWhenDraft() {
        when(repository.save(any(JobOffer.class))).thenAnswer(inv -> inv.getArgument(0));

        JobOfferResponse res = service.create(baseRequest());

        assertEquals(JobStatus.DRAFT, res.getStatus());
        assertEquals(RemotePolicy.ON_SITE, res.getRemotePolicy());
        assertNotNull(res.getRequiredSkills());
        verify(jobEventPublisher, never()).publishJobPublished(any());
    }

    @Test
    @DisplayName("create publie JOB_PUBLISHED quand l'offre est créée directement en OPEN")
    void create_publishesEvent_whenStatusOpen() {
        JobOfferRequest req = baseRequest();
        req.setStatus(JobStatus.OPEN);
        when(repository.save(any(JobOffer.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(req);

        verify(jobEventPublisher).publishJobPublished(any(JobOffer.class));
    }

    @Test
    @DisplayName("getById retourne l'offre quand elle existe")
    void getById_returnsOffer_whenFound() {
        JobOffer offer = offerWith(JobStatus.OPEN, ContractType.FULL_TIME, RemotePolicy.REMOTE, "Dev", "HireMe");
        when(repository.findById(offer.getId())).thenReturn(Optional.of(offer));

        JobOfferResponse res = service.getById(offer.getId());

        assertEquals(offer.getId(), res.getId());
        assertEquals("Dev", res.getTitle());
    }

    @Test
    @DisplayName("getById lève NOT_FOUND quand l'offre est absente")
    void getById_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.getById(id));
        assertEquals(ErrorCode.NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("getByRecruiter délègue au repository et mappe les résultats")
    void getByRecruiter_delegatesAndMaps() {
        UUID recruiterId = UUID.randomUUID();
        when(repository.findByRecruiterId(recruiterId))
                .thenReturn(List.of(
                        offerWith(JobStatus.OPEN, ContractType.FULL_TIME, RemotePolicy.REMOTE, "A", "HireMe"),
                        offerWith(JobStatus.DRAFT, ContractType.PART_TIME, RemotePolicy.ON_SITE, "B", "HireMe")));

        assertEquals(2, service.getByRecruiter(recruiterId).size());
        verify(repository).findByRecruiterId(recruiterId);
    }

    @Test
    @DisplayName("searchOpen ne considère que les offres OPEN et filtre par contrat, télétravail et mot-clé")
    void searchOpen_filtersByContractRemoteAndKeyword() {
        when(repository.findByStatus(JobStatus.OPEN)).thenReturn(List.of(
                offerWith(JobStatus.OPEN, ContractType.FULL_TIME, RemotePolicy.REMOTE, "Java Backend", "HireMe"),
                offerWith(JobStatus.OPEN, ContractType.FULL_TIME, RemotePolicy.ON_SITE, "Java Frontend", "OtherCo"),
                offerWith(JobStatus.OPEN, ContractType.INTERNSHIP, RemotePolicy.REMOTE, "Python Stage", "HireMe")));

        List<JobOfferResponse> res = service.searchOpen(ContractType.FULL_TIME, RemotePolicy.REMOTE, "java");

        assertEquals(1, res.size());
        assertEquals("Java Backend", res.get(0).getTitle());
    }

    @Test
    @DisplayName("searchOpen retrouve aussi par nom d'entreprise (insensible à la casse)")
    void searchOpen_matchesByCompanyCaseInsensitive() {
        when(repository.findByStatus(JobStatus.OPEN)).thenReturn(List.of(
                offerWith(JobStatus.OPEN, ContractType.FULL_TIME, RemotePolicy.REMOTE, "Backend", "HireMe"),
                offerWith(JobStatus.OPEN, ContractType.FULL_TIME, RemotePolicy.REMOTE, "Backend", "OtherCo")));

        List<JobOfferResponse> res = service.searchOpen(null, null, "hireme");

        assertEquals(1, res.size());
        assertEquals("HireMe", res.get(0).getCompany());
    }

    @Test
    @DisplayName("searchOpen sans filtre retourne toutes les offres OPEN")
    void searchOpen_noFilters_returnsAllOpen() {
        when(repository.findByStatus(JobStatus.OPEN)).thenReturn(List.of(
                offerWith(JobStatus.OPEN, ContractType.FULL_TIME, RemotePolicy.REMOTE, "A", "HireMe"),
                offerWith(JobStatus.OPEN, ContractType.PART_TIME, RemotePolicy.HYBRID, "B", "HireMe")));

        assertEquals(2, service.searchOpen(null, null, null).size());
    }

    @Test
    @DisplayName("update modifie uniquement les champs non nuls et conserve les autres")
    void update_appliesOnlyNonNullFields() {
        JobOffer existing = offerWith(JobStatus.DRAFT, ContractType.FULL_TIME, RemotePolicy.ON_SITE, "Ancien titre", "HireMe");
        when(repository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(repository.save(any(JobOffer.class))).thenAnswer(inv -> inv.getArgument(0));

        JobOfferRequest patch = new JobOfferRequest();
        patch.setTitle("Nouveau titre");

        JobOfferResponse res = service.update(existing.getId(), patch);

        assertEquals("Nouveau titre", res.getTitle());
        assertEquals("HireMe", res.getCompany());
        assertEquals(ContractType.FULL_TIME, res.getContractType());
    }

    @Test
    @DisplayName("update publie JOB_PUBLISHED quand l'offre passe à OPEN")
    void update_publishesEvent_whenBecomesOpen() {
        JobOffer existing = offerWith(JobStatus.DRAFT, ContractType.FULL_TIME, RemotePolicy.ON_SITE, "Titre", "HireMe");
        when(repository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(repository.save(any(JobOffer.class))).thenAnswer(inv -> inv.getArgument(0));

        JobOfferRequest patch = new JobOfferRequest();
        patch.setStatus(JobStatus.OPEN);

        service.update(existing.getId(), patch);

        verify(jobEventPublisher).publishJobPublished(any(JobOffer.class));
    }

    @Test
    @DisplayName("update lève NOT_FOUND quand l'offre n'existe pas")
    void update_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> service.update(id, baseRequest()));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("delete supprime l'offre quand elle existe")
    void delete_removesOffer_whenExists() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    @DisplayName("delete lève NOT_FOUND quand l'offre n'existe pas")
    void delete_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () -> service.delete(id));
        assertEquals(ErrorCode.NOT_FOUND, ex.getErrorCode());
        verify(repository, never()).deleteById(any());
    }
}
