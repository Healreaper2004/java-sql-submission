package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;   // ✅ Correct import
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Step 1: Call generateWebhook API
        String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        Map<String, String> requestBody = Map.of(
                "name", "Ayush Arya",
                "regNo", "22BCE3024",
                "email", "ayush.arya2022@vitstudent.ac.in"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(generateUrl, entity, Map.class);
        Map<String, Object> responseBody = response.getBody();

        String webhookUrl = (String) responseBody.get("webhook");
        String accessToken = (String) responseBody.get("accessToken");

        System.out.println("Webhook URL: " + webhookUrl);
        System.out.println("Access Token: " + accessToken);

        // Step 2: Your final SQL query (Question 2 solution)
        String finalQuery =
                "SELECT e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, d.DEPARTMENT_NAME, " +
                "(COUNT(*) OVER (PARTITION BY e.DEPARTMENT)) - " +
                "(RANK() OVER (PARTITION BY e.DEPARTMENT ORDER BY e.DOB)) AS YOUNGER_EMPLOYEES_COUNT " +
                "FROM EMPLOYEE e " +
                "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                "ORDER BY e.EMP_ID DESC;";

        // Step 3: Send query to webhook
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // FIX: plain Authorization header (NOT Bearer)
        authHeaders.set("Authorization", accessToken);

        Map<String, String> finalBody = Map.of("finalQuery", finalQuery);

        HttpEntity<Map<String, String>> finalEntity = new HttpEntity<>(finalBody, authHeaders);

        ResponseEntity<String> finalResp = restTemplate.postForEntity(webhookUrl, finalEntity, String.class);

        System.out.println("Submission Response: " + finalResp.getBody());
    }
}
