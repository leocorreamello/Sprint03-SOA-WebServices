package br.com.fiap.autointel.domain.model;

/**
 * Perfis de acesso da aplicação.
 * <ul>
 *     <li>{@code ADMIN}: mantém o catálogo de atributos e a base de veículos/especificações, além de ver todas as consultas.</li>
 *     <li>{@code ANALISTA}: realiza consultas de especificações e visualiza apenas o próprio histórico.</li>
 * </ul>
 */
public enum Perfil {
    ADMIN,
    ANALISTA;

    public String authority() {
        return "ROLE_" + name();
    }
}
