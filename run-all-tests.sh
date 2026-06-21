#!/bin/bash

echo "=========================================="
echo "🚀 Démarrage de tous les tests..."
echo "=========================================="

# Liste des services Java (Maven)
JAVA_SERVICES=(
    "eureka-server"
    "hireme-gateway"
    "hireme-auth-service"
    "hireme-resume-service"
    "hireme-job-service"
    "hireme-matching-service"
)

# Fonction pour tester les projets Maven
test_java() {
    for service in "${JAVA_SERVICES[@]}"; do
        if [ -d "$service" ]; then
            echo "------------------------------------------"
            echo "☕ Lancement des tests pour : $service"
            echo "------------------------------------------"
            (cd "$service" && mvn test)
            if [ $? -ne 0 ]; then
                echo "❌ Échec des tests dans $service"
                exit 1
            fi
        fi
    done
}

# Fonction pour tester le projet Python
test_python() {
    local ml_dir="hireme-ml-engine"
    if [ -d "$ml_dir" ]; then
        echo "------------------------------------------"
        echo "🐍 Lancement des tests pour : $ml_dir"
        echo "------------------------------------------"
        (cd "$ml_dir" && source .venv/bin/activate && pytest)
        if [ $? -ne 0 ]; then
            echo "❌ Échec des tests Python dans $ml_dir"
            exit 1
        fi
    fi
}

test_java
test_python

echo "=========================================="
echo "✅ TOUS LES TESTS SONT PASSÉS AVEC SUCCÈS !"
echo "=========================================="
