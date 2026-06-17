package com.hireme.matchingservice.client;

import java.util.List;

/**
 * Abstraction du moteur Python (NLP). Le MatchingService délègue le calcul scientifique
 * (anonymisation + TF-IDF + cosinus) à ce moteur (§5.5) et ne réimplémente pas le NLP en Java.
 *
 * <p>L'anonymisation amont est réalisée par le moteur lui-même ; {@code knownPii} fournit
 * les identifiants connus du candidat (nom, prénom) à retirer.</p>
 */
public interface MlEngineClient {

    /** Score de pertinence dans [0, 1] entre un CV et une offre, après anonymisation. */
    double computeScore(String resumeText, String jobText, List<String> knownPii);
}
