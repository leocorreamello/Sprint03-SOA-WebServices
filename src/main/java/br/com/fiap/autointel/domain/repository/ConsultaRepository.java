package br.com.fiap.autointel.domain.repository;

import br.com.fiap.autointel.domain.model.Consulta;
import br.com.fiap.autointel.domain.model.Veiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    Page<Consulta> findBySolicitanteIgnoreCase(String solicitante, Pageable pageable);

    /** Desvincula o histórico de um veículo removido (as consultas mantêm a "fotografia" dos dados). */
    @Modifying
    @Query("update Consulta c set c.veiculo = null where c.veiculo = :veiculo")
    void desvincularVeiculo(@Param("veiculo") Veiculo veiculo);
}
