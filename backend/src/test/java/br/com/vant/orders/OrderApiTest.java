package br.com.vant.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderApiTest {

    private static final String INTERNAL_TOKEN = "dev-internal-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void criaPedidoComStatusInicial() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"Teste Ltda","originCep":"30130010","destinationCep":"20040020","weightKg":120.5}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.startsWith("ORD-")))
                .andExpect(jsonPath("$.freightCents").doesNotExist());
    }

    @Test
    void recusaPayloadInvalido() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"","originCep":"123","destinationCep":"20040020","weightKg":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.customerName").exists())
                .andExpect(jsonPath("$.campos.originCep").exists())
                .andExpect(jsonPath("$.campos.weightKg").exists());
    }

    @Test
    void percorreFluxoCriacaoCotacaoDespachoEntrega() throws Exception {
        UUID id = criarPedido("Fluxo Completo ME");

        mockMvc.perform(post("/api/orders/" + id + "/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"freightCents":87500,"carrier":"Rodo Sul Cargas","estimatedDays":3}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/orders/" + id + "/quote")
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"freightCents":87500,"carrier":"Rodo Sul Cargas","estimatedDays":3}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("QUOTED"))
                .andExpect(jsonPath("$.freightCents").value(87500))
                .andExpect(jsonPath("$.carrier").value("Rodo Sul Cargas"));

        mockMvc.perform(post("/api/orders/" + id + "/quote")
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"freightCents":90000,"carrier":"Outra","estimatedDays":5}
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/api/orders/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DELIVERED\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/api/orders/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DISPATCHED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISPATCHED"));

        mockMvc.perform(patch("/api/orders/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DELIVERED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    void filtraPedidosPorStatusETermo() throws Exception {
        UUID alvo = criarPedido("Filtro Alfa");
        criarPedido("Filtro Beta");

        mockMvc.perform(get("/api/orders").param("q", "Filtro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));

        mockMvc.perform(post("/api/orders/" + alvo + "/quote")
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"freightCents":52000,"carrier":"Expresso Sul","estimatedDays":2}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/orders/" + alvo + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DISPATCHED\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/orders/" + alvo + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DELIVERED\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders").param("status", "DELIVERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[*].status", org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("DELIVERED"))));
    }

    @Test
    void devolveResumoE404() throws Exception {
        mockMvc.perform(get("/api/orders/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.CREATED").exists());

        mockMvc.perform(get("/api/orders/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    private UUID criarPedido(String customer) throws Exception {
        String payload = """
                {"customerName":"%s","originCep":"30130010","destinationCep":"20040020","weightKg":75.0}
                """.formatted(customer);

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("code").asText()).startsWith("ORD-");
        return UUID.fromString(body.get("id").asText());
    }
}
