package com.insurance.api.controller;

import com.insurance.api.model.ClaimRequest;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate; 
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = "*") // El puente para el Frontend
public class ClaimController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // --- EL INTERCEPTOR DE IA LOCAL (OLLAMA) ---
    private String evaluateRiskWithOllama(ClaimRequest request) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:11434/api/generate";

            // El Prompt (Ingeniería de contexto rápida)
            String prompt = String.format(
                "Eres un auditor de fraudes estricto. Analiza este reclamo: Monto $%s, Operador: %s. " +
                "Responde en UNA sola oración corta si es sospechoso o si parece normal.",
                request.getClaimAmount(), request.getUserRole()
            );

            Map<String, Object> body = new HashMap<>();
            body.put("model", "llama3"); 
            body.put("prompt", prompt);
            body.put("stream", false);

            Map<String, Object> response = restTemplate.postForObject(url, body, Map.class);
            if (response != null && response.containsKey("response")) {
                return (String) response.get("response");
            }
        } catch (Exception e) {
            return "Servidor IA inactivo o modelo no encontrado.";
        }
        return "Análisis no disponible.";
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> processClaim(@RequestBody ClaimRequest request) {
        System.out.println("====================================================");
        System.out.println("NUEVO RECLAMO RECIBIDO EN LA ENTRADA MODERNA (API)");
        System.out.println("====================================================");
        System.out.println("Poliza: " + request.getPolicyId());
        System.out.println("Cliente: " + request.getCustomerId());
        System.out.println("Monto reclamado: $" + request.getClaimAmount());
        System.out.println("Operador: " + request.getUserId() + " | Rol: " + request.getUserRole());

        List<String> cobolOutput = new ArrayList<>();
        Map<String, Object> response = new HashMap<>();

        // 1. ANÁLISIS DE INTELIGENCIA ARTIFICIAL ANTES DE COBOL
        System.out.println(">> Consultando a la IA Local (Ollama)...");
        String aiVeredict = evaluateRiskWithOllama(request);
        
        cobolOutput.add("=================================");
        cobolOutput.add(">> OLLAMA AI RISK ANALYSIS <<");
        cobolOutput.add(aiVeredict.trim().replace("\n", " ")); 
        cobolOutput.add("=================================");

        // 2. PROCESAMIENTO CORE (COBOL)
        try {
            String cobolBinaryPath = "/Volumes/FreeDuty/insurance-cobol/claimval";
            ProcessBuilder processBuilder = new ProcessBuilder(cobolBinaryPath);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                cobolOutput.add(line);
                System.out.println(">> COBOL Output: " + line);
            }

            int exitCode = process.waitFor();
            System.out.println(">> COBOL Process exited with code: " + exitCode);

            // 3. MENSAJERÍA ASÍNCRONA (RabbitMQ)
            ObjectMapper mapper = new ObjectMapper();
            String jsonPayload = mapper.writeValueAsString(request);

            rabbitTemplate.convertAndSend("cobol_input_queue", jsonPayload);
            System.out.println(">> EXITO: Mensaje inyectado en RabbitMQ para procesamiento por COBOL. Payload: " + jsonPayload);

            response.put("status", "PROCESSED");
            response.put("mainframe_log", cobolOutput);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("Error al ejecutar el programa COBOL o RabbitMQ: " + e.getMessage());
            response.put("status", "ERROR");
            response.put("message", "Fallo de comunicación con Mainframe o MQ: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}