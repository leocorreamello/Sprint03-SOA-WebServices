package br.com.fiap.autointel.config;

import br.com.fiap.autointel.domain.model.*;
import br.com.fiap.autointel.domain.repository.AtributoRepository;
import br.com.fiap.autointel.domain.repository.UsuarioRepository;
import br.com.fiap.autointel.domain.repository.VeiculoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Carga inicial: usuários de demonstração, catálogo de atributos ({@code seed/atributos.json})
 * e base de veículos ({@code seed/veiculos.json}), incluindo a Ford Ranger Raptor usada na validação.
 * Para ajustar os dados basta editar os arquivos JSON — nenhum código precisa ser alterado.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    record AtributoSeed(String codigo, String nome, CategoriaAtributo categoria, String unidade, Set<String> sinonimos) {
    }

    record VeiculoSeed(String marca, String modelo, String versao, Integer anoModelo, String fonte,
                       LinkedHashMap<String, String> especificacoes) {
    }

    private final UsuarioRepository usuarios;
    private final AtributoRepository atributos;
    private final VeiculoRepository veiculos;
    private final PasswordEncoder passwordEncoder;
    private final JsonMapper jsonMapper;

    public DataSeeder(UsuarioRepository usuarios, AtributoRepository atributos, VeiculoRepository veiculos,
                      PasswordEncoder passwordEncoder, JsonMapper jsonMapper) {
        this.usuarios = usuarios;
        this.atributos = atributos;
        this.veiculos = veiculos;
        this.passwordEncoder = passwordEncoder;
        this.jsonMapper = jsonMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        criarUsuario("Administrador", "admin@autointel.com", "Admin@123", Perfil.ADMIN);
        criarUsuario("Analista de Mercado", "analista@autointel.com", "Analista@123", Perfil.ANALISTA);

        if (atributos.count() == 0) {
            List<AtributoSeed> seeds = ler("seed/atributos.json", new TypeReference<>() {
            });
            seeds.forEach(s -> atributos.save(new Atributo(s.codigo(), s.nome(), s.categoria(), s.unidade(), s.sinonimos())));
            log.info("Catálogo carregado com {} atributos", seeds.size());
        }

        if (veiculos.count() == 0) {
            List<VeiculoSeed> seeds = ler("seed/veiculos.json", new TypeReference<>() {
            });
            for (VeiculoSeed s : seeds) {
                Veiculo veiculo = new Veiculo(s.marca(), s.modelo(), s.versao(), s.anoModelo());
                s.especificacoes().forEach((codigo, valor) -> {
                    Atributo atributo = atributos.findByCodigo(codigo).orElseThrow(() ->
                            new IllegalStateException("seed/veiculos.json referencia atributo inexistente: " + codigo));
                    veiculo.definirEspecificacao(atributo, valor, s.fonte());
                });
                veiculos.save(veiculo);
                log.info("Veículo carregado: {} ({} especificações)", veiculo.descricao(), s.especificacoes().size());
            }
        }
    }

    private void criarUsuario(String nome, String email, String senha, Perfil perfil) {
        if (!usuarios.existsByEmailIgnoreCase(email)) {
            usuarios.save(new Usuario(nome, email, passwordEncoder.encode(senha), perfil));
        }
    }

    private <T> T ler(String caminho, TypeReference<T> tipo) throws IOException {
        try (InputStream in = new ClassPathResource(caminho).getInputStream()) {
            return jsonMapper.readValue(in, tipo);
        }
    }
}
