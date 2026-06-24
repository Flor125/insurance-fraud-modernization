package com.insurance.api.controller;

import com.insurance.api.model.ClaimRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/claims")

public class ClaimController {
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

        response.put("status", "PROCESSED");
        response.put("mainframe_log", cobolOutput);

        return ResponseEntity.ok(response);
    } catch (Exception e) {
        System.out.println("Error al ejecutar el programa COBOL: " + e.getMessage());
        response.put("status", "ERROR");
        response.put("message", "Fallo de comunicación con Mainframe: " + e.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    }
}
