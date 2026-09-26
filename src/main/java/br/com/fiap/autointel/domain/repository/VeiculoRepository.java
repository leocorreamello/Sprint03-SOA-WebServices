package br.com.fiap.autointel.domain.repository;

import br.com.fiap.autointel.domain.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    Optional<Veiculo> findByChaveBusca(String chaveBusca);

    boolean existsByChaveBusca(String chaveBusca);

    @Query("""
            select v from Veiculo v
            where (:marca is null or lower(v.marca) like lower(concat('%', cast(:marca as string), '%')))
              and (:modelo is null or lower(v.modelo) like lower(concat('%', cast(:modelo as string), '%')))
            order by v.marca, v.modelo, v.versao
            """)
    List<Veiculo> buscar(@Param("marca") String marca, @Param("modelo") String modelo);
}
