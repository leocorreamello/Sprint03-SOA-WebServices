package br.com.fiap.autointel.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * Padroniza TODAS as respostas de erro da API no formato Problem Details (RFC 9457),
 * com os campos extras {@code codigo}, {@code timestamp} e, quando aplicável, {@code erros}.
 * <pre>
 * {
 *   "type": "https://autointel.fiap.com.br/erros/validacao",
 *   "title": "Requisição inválida",
 *   "status": 400,
 *   "detail": "Um ou mais campos são inválidos.",
 *   "instance": "/api/v1/consultas",
 *   "codigo": "VALIDACAO",
 *   "timestamp": "2026-09-26T21:00:00Z",
 *   "erros": [ { "campo": "marca", "mensagem": "não deve estar em branco" } ]
 * }
 * </pre>
 * Também é usado pelos handlers do Spring Security (401/403), garantindo o mesmo formato.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BASE_TYPE = "https://autointel.fiap.com.br/erros/";

    public record ErroCampo(String campo, String mensagem) {
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ProblemDetail> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", "RECURSO_NAO_ENCONTRADO", ex.getMessage(), req);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ProblemDetail> conflito(ConflitoException ex, HttpServletRequest req) {
        return problema(HttpStatus.CONFLICT, "Conflito", "CONFLITO", ex.getMessage(), req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> integridade(DataIntegrityViolationException ex, HttpServletRequest req) {
        return problema(HttpStatus.CONFLICT, "Conflito", "CONFLITO",
                "A operação viola uma restrição de integridade dos dados.", req);
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<ProblemDetail> requisicaoInvalida(RequisicaoInvalidaException ex, HttpServletRequest req) {
        ResponseEntity<ProblemDetail> resposta = problema(HttpStatus.BAD_REQUEST, "Requisição inválida", "VALIDACAO",
                "Um ou mais campos são inválidos.", req);
        resposta.getBody().setProperty("erros", List.of(new ErroCampo(ex.getCampo(), ex.getMessage())));
        return resposta;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> restricao(ConstraintViolationException ex, HttpServletRequest req) {
        List<ErroCampo> erros = ex.getConstraintViolations().stream()
                .map(v -> new ErroCampo(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        ResponseEntity<ProblemDetail> resposta = problema(HttpStatus.BAD_REQUEST, "Requisição inválida", "VALIDACAO",
                "Um ou mais parâmetros são inválidos.", req);
        resposta.getBody().setProperty("erros", erros);
        return resposta;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> tipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return problema(HttpStatus.BAD_REQUEST, "Requisição inválida", "PARAMETRO_INVALIDO",
                "Valor inválido para o parâmetro '" + ex.getName() + "': " + ex.getValue(), req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> naoAutenticado(AuthenticationException ex, HttpServletRequest req) {
        String detalhe = ex instanceof BadCredentialsException
                ? "E-mail ou senha inválidos."
                : ex.getMessage();
        ResponseEntity<ProblemDetail> base = problema(HttpStatus.UNAUTHORIZED, "Não autenticado", "NAO_AUTENTICADO", detalhe, req);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"autointel\"")
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(base.getBody());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> acessoNegado(AccessDeniedException ex, HttpServletRequest req) {
        String detalhe = ex instanceof AcessoNegadoException
                ? ex.getMessage()
                : "Seu perfil não tem permissão para acessar este recurso.";
        return problema(HttpStatus.FORBIDDEN, "Acesso negado", "ACESSO_NEGADO", detalhe, req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> inesperado(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado em {}", req.getRequestURI(), ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "ERRO_INTERNO",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", req);
    }

    // ---- Exceções do Spring MVC (mantidas no mesmo formato) ----

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        List<ErroCampo> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(this::erroCampo)
                .toList();
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Um ou mais campos são inválidos.");
        pd.setTitle("Requisição inválida");
        pd.setType(URI.create(BASE_TYPE + "validacao"));
        pd.setProperty("codigo", "VALIDACAO");
        pd.setProperty("erros", erros);
        return createResponseEntity(pd, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                            HttpHeaders headers, HttpStatusCode status,
                                                                            WebRequest request) {
        List<ErroCampo> erros = ex.getParameterValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream()
                        .map(e -> new ErroCampo(r.getMethodParameter().getParameterName(), e.getDefaultMessage())))
                .toList();
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Um ou mais parâmetros são inválidos.");
        pd.setTitle("Requisição inválida");
        pd.setType(URI.create(BASE_TYPE + "validacao"));
        pd.setProperty("codigo", "VALIDACAO");
        pd.setProperty("erros", erros);
        return createResponseEntity(pd, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "O corpo da requisição está ausente ou não é um JSON válido.");
        pd.setTitle("Requisição inválida");
        pd.setType(URI.create(BASE_TYPE + "json-invalido"));
        pd.setProperty("codigo", "JSON_INVALIDO");
        return createResponseEntity(pd, headers, HttpStatus.BAD_REQUEST, request);
    }

    /** Enriquece qualquer ProblemDetail gerado pelo Spring MVC (404, 405, 415...) com os campos padrão. */
    @Override
    protected ResponseEntity<Object> createResponseEntity(Object body, HttpHeaders headers,
                                                          HttpStatusCode statusCode, WebRequest request) {
        if (body instanceof ProblemDetail pd) {
            if (pd.getProperties() == null || !pd.getProperties().containsKey("codigo")) {
                pd.setProperty("codigo", codigoPadrao(statusCode));
            }
            pd.setProperty("timestamp", Instant.now());
            if (request instanceof ServletWebRequest swr) {
                pd.setInstance(URI.create(swr.getRequest().getRequestURI()));
            }
        }
        return super.createResponseEntity(body, headers, statusCode, request);
    }

    private ResponseEntity<ProblemDetail> problema(HttpStatus status, String titulo, String codigo, String detalhe,
                                                   HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detalhe);
        pd.setTitle(titulo);
        pd.setType(URI.create(BASE_TYPE + codigo.toLowerCase().replace('_', '-')));
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("codigo", codigo);
        pd.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(pd);
    }

    private ErroCampo erroCampo(FieldError fe) {
        return new ErroCampo(fe.getField(), fe.getDefaultMessage());
    }

    private static String codigoPadrao(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "REQUISICAO_INVALIDA";
            case 404 -> "RECURSO_NAO_ENCONTRADO";
            case 405 -> "METODO_NAO_SUPORTADO";
            case 406 -> "FORMATO_NAO_ACEITO";
            case 415 -> "TIPO_DE_MIDIA_NAO_SUPORTADO";
            default -> "ERRO_HTTP_" + status.value();
        };
    }
}
