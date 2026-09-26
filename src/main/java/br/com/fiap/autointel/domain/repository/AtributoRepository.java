package br.com.fiap.autointel.domain.repository;

import br.com.fiap.autointel.domain.model.Atributo;
import br.com.fiap.autointel.domain.model.CategoriaAtributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AtributoRepository extends JpaRepository<Atributo, Long> {

    Optional<Atributo> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Atributo> findAllByOrderByCategoriaAscNomeAsc();

    List<Atributo> findByCategoriaOrderByNomeAsc(CategoriaAtributo categoria);

    @Query("select count(e) > 0 from Especificacao e where e.atributo = :atributo")
    boolean estaEmUso(@Param("atributo") Atributo atributo);
}
