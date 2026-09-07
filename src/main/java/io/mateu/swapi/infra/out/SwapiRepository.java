package io.mateu.swapi.infra.out;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Every collection needs both halves — the plain CRUD and the criteria executor the declared filters
 * are evaluated through — so they are joined once here rather than at each of the six repositories.
 */
@NoRepositoryBean
public interface SwapiRepository<E> extends JpaRepository<E, Long>, JpaSpecificationExecutor<E> {}
