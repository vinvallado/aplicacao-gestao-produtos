
package br.com.boticario.project.pmp.infrastructure.persistence.repository;

import br.com.boticario.project.pmp.infrastructure.persistence.model.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringProductRepository extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {

    /**
     * O Spring Data JPA implementa este método automaticamente com base no nome.
     * Ele gera a query: "select case when (count(p) > 0) then true else false end from ProductEntity p where p.product = ?1 and p.type = ?2"
     * É uma forma extremamente eficiente de verificar a existência de um registro.
     */
    boolean existsByProductAndType(String product, String type);

}
