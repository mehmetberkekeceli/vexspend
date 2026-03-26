package com.wallet.vexspend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiSmokeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSmokeTestAllMainApis() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "smoke_" + suffix;
        String email = "smoke_" + suffix + "@example.com";
        String password = "Aa1!test123";
        LocalDate today = LocalDate.now();

        JsonNode registerBody = postJson("/api/v1/auth/register", mapOf(
                "username", username,
                "email", email,
                "password", password
        ), null, 201);
        String registerToken = registerBody.get("accessToken").asText();
        assertNotNull(registerToken);

        JsonNode loginBody = postJson("/api/v1/auth/login", mapOf(
                "usernameOrEmail", username,
                "password", password
        ), null, 200);
        assertNotNull(loginBody.get("accessToken").asText());

        JsonNode meBody = getJson("/api/v1/users/me", registerToken, 200);
        assertNotNull(meBody.get("id").asText());

        putJson("/api/v1/users/me", mapOf(
                "fullName", "Smoke Test User",
                "profilePhotoUrl", "https://example.com/avatar.png"
        ), registerToken, 200);

        JsonNode expenseCategory = postJson("/api/v1/categories", mapOf(
                "name", "market-" + suffix,
                "type", "EXPENSE",
                "colorHex", "#22c55e",
                "icon", "cart"
        ), registerToken, 201);
        String expenseCategoryId = expenseCategory.get("id").asText();

        JsonNode incomeCategory = postJson("/api/v1/categories", mapOf(
                "name", "salary-" + suffix,
                "type", "INCOME",
                "colorHex", "#0ea5e9",
                "icon", "wallet"
        ), registerToken, 201);
        String incomeCategoryId = incomeCategory.get("id").asText();

        getJson("/api/v1/categories", registerToken, 200);
        getJson("/api/v1/categories/" + expenseCategoryId, registerToken, 200);
        putJson("/api/v1/categories/" + incomeCategoryId, mapOf(
                "icon", "briefcase"
        ), registerToken, 200);

        JsonNode account = postJson("/api/v1/accounts", mapOf(
                "name", "main-" + suffix,
                "type", "BANK",
                "currencyCode", "USD",
                "initialBalance", 1000
        ), registerToken, 201);
        String accountId = account.get("id").asText();

        getJson("/api/v1/accounts", registerToken, 200);
        getJson("/api/v1/accounts/" + accountId, registerToken, 200);
        putJson("/api/v1/accounts/" + accountId, mapOf(
                "name", "main-updated-" + suffix
        ), registerToken, 200);

        JsonNode budget = postJson("/api/v1/budgets", mapOf(
                "name", "monthly-" + suffix,
                "currencyCode", "USD",
                "periodStart", today.withDayOfMonth(1).toString(),
                "periodEnd", today.withDayOfMonth(today.lengthOfMonth()).toString(),
                "totalLimit", 1200
        ), registerToken, 201);
        String budgetId = budget.get("id").asText();

        getJson("/api/v1/budgets", registerToken, 200);
        getJson("/api/v1/budgets/" + budgetId, registerToken, 200);
        putJson("/api/v1/budgets/" + budgetId, mapOf(
                "name", "monthly-updated-" + suffix,
                "totalLimit", 1400
        ), registerToken, 200);

        JsonNode budgetItem = postJson("/api/v1/budgets/" + budgetId + "/items", mapOf(
                "name", "groceries-" + suffix,
                "categoryId", expenseCategoryId,
                "allocatedAmount", 600
        ), registerToken, 201);
        String budgetItemId = budgetItem.get("id").asText();

        getJson("/api/v1/budgets/" + budgetId + "/items", registerToken, 200);
        putJson("/api/v1/budgets/" + budgetId + "/items/" + budgetItemId, mapOf(
                "allocatedAmount", 700
        ), registerToken, 200);

        postJson("/api/v1/transactions", mapOf(
                "accountId", accountId,
                "type", "EXPENSE",
                "amount", 75.50,
                "transactionDate", today.toString(),
                "categoryId", expenseCategoryId,
                "budgetId", budgetId,
                "budgetItemId", budgetItemId,
                "merchant", "market",
                "note", "smoke expense"
        ), registerToken, 201);

        postJson("/api/v1/transactions", mapOf(
                "accountId", accountId,
                "type", "INCOME",
                "amount", 250,
                "transactionDate", today.toString(),
                "categoryId", incomeCategoryId,
                "merchant", "company",
                "note", "smoke income"
        ), registerToken, 201);

        getJson("/api/v1/transactions", registerToken, 200);
        getJson("/api/v1/transactions?accountId=" + accountId, registerToken, 200);

        JsonNode recurring = postJson("/api/v1/recurring-transactions", mapOf(
                "accountId", accountId,
                "categoryId", expenseCategoryId,
                "budgetId", budgetId,
                "budgetItemId", budgetItemId,
                "type", "EXPENSE",
                "amount", 25,
                "frequency", "DAILY",
                "startDate", today.toString(),
                "merchant", "recurring-merchant",
                "note", "recurring smoke"
        ), registerToken, 201);
        String recurringId = recurring.get("id").asText();

        getJson("/api/v1/recurring-transactions", registerToken, 200);
        getJson("/api/v1/recurring-transactions/" + recurringId, registerToken, 200);
        putJson("/api/v1/recurring-transactions/" + recurringId, mapOf(
                "note", "updated recurring note"
        ), registerToken, 200);
        postJson("/api/v1/recurring-transactions/process-due?date=" + today, mapOf(), registerToken, 200);

        getJson("/api/v1/reports/dashboard?trendMonths=3", registerToken, 200);

        deleteCall("/api/v1/recurring-transactions/" + recurringId, registerToken, 204);
        JsonNode allTransactions = getJson("/api/v1/transactions", registerToken, 200);
        for (JsonNode transaction : allTransactions) {
            deleteCall("/api/v1/transactions/" + transaction.get("id").asText(), registerToken, 204);
        }
        deleteCall("/api/v1/budgets/" + budgetId + "/items/" + budgetItemId, registerToken, 204);
        deleteCall("/api/v1/budgets/" + budgetId, registerToken, 204);
        deleteCall("/api/v1/categories/" + expenseCategoryId, registerToken, 204);
        deleteCall("/api/v1/categories/" + incomeCategoryId, registerToken, 204);
        deleteCall("/api/v1/accounts/" + accountId, registerToken, 204);
    }

    private JsonNode postJson(String url, Map<String, Object> payload, String token, int expectedStatus) throws Exception {
        String json = objectMapper.writeValueAsString(payload);
        MvcResult result = (token == null
                ? mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                : mockMvc.perform(post(url).header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(json)))
                .andExpect(status().is(expectedStatus))
                .andReturn();
        String body = result.getResponse().getContentAsString();
        return body == null || body.isEmpty() ? objectMapper.createObjectNode() : objectMapper.readTree(body);
    }

    private JsonNode putJson(String url, Map<String, Object> payload, String token, int expectedStatus) throws Exception {
        String json = objectMapper.writeValueAsString(payload);
        MvcResult result = mockMvc.perform(put(url)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is(expectedStatus))
                .andReturn();
        String body = result.getResponse().getContentAsString();
        return body == null || body.isEmpty() ? objectMapper.createObjectNode() : objectMapper.readTree(body);
    }

    private JsonNode getJson(String url, String token, int expectedStatus) throws Exception {
        MvcResult result = mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus))
                .andReturn();
        String body = result.getResponse().getContentAsString();
        return body == null || body.isEmpty() ? objectMapper.createObjectNode() : objectMapper.readTree(body);
    }

    private void deleteCall(String url, String token, int expectedStatus) throws Exception {
        mockMvc.perform(delete(url).header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    private Map<String, Object> mapOf(Object... items) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < items.length; i += 2) {
            map.put(String.valueOf(items[i]), items[i + 1]);
        }
        return map;
    }
}
