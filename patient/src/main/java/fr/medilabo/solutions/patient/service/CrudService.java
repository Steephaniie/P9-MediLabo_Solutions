package fr.medilabo.solutions.patient.service;

import java.util.List;

/**
 * Interface de service générique pour gérer les entités.
 *
 * @param <T> le type de l'entité
 * 
 * @author LEULLIETTE Stéphanie
 * @version 1.0
 */
public interface CrudService<T> {

    /**
     * Met à jour une entité.
     *
     * @param Object l'entité à mettre à jour
     * @return l'entité mise à jour
     * @throws Exception si l'opération de mise à jour n'est pas supportée
     */
    public default T create(T Object) throws Exception {
        throw new UnsupportedOperationException("Create operation not supported");
    }

    /**
     * Récupère toutes les entités.
     *
     * @return une liste de toutes les entités
     * @throws Exception si l'opération de récupération n'est pas supportée
     */
    public default List<T> findAll() {
        throw new UnsupportedOperationException("Find all operation not supported");
    }

    /**
     * Récupère une entité par son identifiant.
     *
     * @param id l'identifiant de l'entité à récupérer
     * @return l'entité avec l'identifiant spécifié
     * @throws Exception si l'opération de récupération n'est pas supportée
     */
    public default T findById(int id) {
        throw new UnsupportedOperationException("Find by ID operation not supported");
    }

    /**
     * Creates a new entity.
     *
     * @param Object the entity to create
     * @return the created entity
     * @throws Exception if the create operation is not supported
     */
    public default T update(T Object) {
        throw new UnsupportedOperationException("Update operation not supported");
    }

    /**
     * Supprime une entité.
     *
     * @param Object l'entité à supprimer
     * @throws Exception si l'opération de suppression n'est pas supportée
     */
    public default void delete(T Object) {
        throw new UnsupportedOperationException("Delete operation not supported");
    }

}
