package com.paymentalert.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class HomeController {
    
    /**
     * Home landing page - returns API information
     * GET /
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Payment Alert Backend API");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("description", "Backend API for Payment Alert app with PhonePe integration");
        
        Map<String, Object> endpoints = new HashMap<>();
        endpoints.put("health", "/health");
        endpoints.put("api_docs", "/api-docs");
        endpoints.put("phonepe_payment", "/api/phonepe/payment");
        endpoints.put("phonepe_subscription", "/api/phonepe/subscription");
        endpoints.put("webhook_phonepe", "/webhook/phonepe");
        endpoints.put("webhook_renewal", "/webhook/phonepe/renewal");
        
        response.put("endpoints", endpoints);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check endpoint
     * GET /health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("service", "Payment Alert Backend");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API documentation endpoint
     * GET /api-docs
     */
    @GetMapping("/api-docs")
    public ResponseEntity<Map<String, Object>> apiDocs() {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> phonepeEndpoints = new HashMap<>();
        phonepeEndpoints.put("POST /api/phonepe/payment", "Create payment request");
        phonepeEndpoints.put("POST /api/phonepe/subscription", "Create subscription");
        phonepeEndpoints.put("GET /api/phonepe/payment/{transactionId}", "Get payment status");
        phonepeEndpoints.put("GET /api/phonepe/subscription/{subscriptionId}", "Get subscription details");
        
        Map<String, Object> webhookEndpoints = new HashMap<>();
        webhookEndpoints.put("POST /webhook/phonepe", "PhonePe payment webhook");
        webhookEndpoints.put("POST /webhook/phonepe/renewal", "PhonePe renewal webhook");
        
        Map<String, Object> systemEndpoints = new HashMap<>();
        systemEndpoints.put("GET /", "Home page with API information");
        systemEndpoints.put("GET /health", "Health check");
        systemEndpoints.put("GET /api-docs", "API documentation");
        
        response.put("phonepe_api", phonepeEndpoints);
        response.put("webhooks", webhookEndpoints);
        response.put("system", systemEndpoints);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Welcome page with HTML content
     * GET /welcome
     */
    @GetMapping(value = "/welcome", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> welcome() {
        String html = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Payment Alert Backend API</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\n" +
            "            line-height: 1.6;\n" +
            "            margin: 0;\n" +
            "            padding: 0;\n" +
            "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
            "            min-height: 100vh;\n" +
            "            display: flex;\n" +
            "            align-items: center;\n" +
            "            justify-content: center;\n" +
            "        }\n" +
            "        .container {\n" +
            "            background: white;\n" +
            "            border-radius: 10px;\n" +
            "            box-shadow: 0 15px 35px rgba(0,0,0,0.1);\n" +
            "            padding: 2rem;\n" +
            "            max-width: 800px;\n" +
            "            width: 90%;\n" +
            "            margin: 2rem;\n" +
            "        }\n" +
            "        .header {\n" +
            "            text-align: center;\n" +
            "            margin-bottom: 2rem;\n" +
            "        }\n" +
            "        .header h1 {\n" +
            "            color: #333;\n" +
            "            margin: 0;\n" +
            "            font-size: 2.5rem;\n" +
            "            font-weight: 300;\n" +
            "        }\n" +
            "        .header p {\n" +
            "            color: #666;\n" +
            "            margin: 0.5rem 0 0 0;\n" +
            "            font-size: 1.1rem;\n" +
            "        }\n" +
            "        .status {\n" +
            "            background: #e8f5e8;\n" +
            "            border: 1px solid #4caf50;\n" +
            "            border-radius: 5px;\n" +
            "            padding: 1rem;\n" +
            "            margin: 1rem 0;\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .status h3 {\n" +
            "            color: #2e7d32;\n" +
            "            margin: 0 0 0.5rem 0;\n" +
            "        }\n" +
            "        .status p {\n" +
            "            color: #388e3c;\n" +
            "            margin: 0;\n" +
            "        }\n" +
            "        .endpoints {\n" +
            "            margin: 2rem 0;\n" +
            "        }\n" +
            "        .endpoints h3 {\n" +
            "            color: #333;\n" +
            "            border-bottom: 2px solid #667eea;\n" +
            "            padding-bottom: 0.5rem;\n" +
            "        }\n" +
            "        .endpoint-group {\n" +
            "            margin: 1rem 0;\n" +
            "        }\n" +
            "        .endpoint-group h4 {\n" +
            "            color: #555;\n" +
            "            margin: 1rem 0 0.5rem 0;\n" +
            "        }\n" +
            "        .endpoint {\n" +
            "            background: #f8f9fa;\n" +
            "            border-left: 4px solid #667eea;\n" +
            "            padding: 0.75rem;\n" +
            "            margin: 0.5rem 0;\n" +
            "            border-radius: 0 5px 5px 0;\n" +
            "        }\n" +
            "        .endpoint strong {\n" +
            "            color: #333;\n" +
            "            font-family: 'Monaco', 'Menlo', monospace;\n" +
            "        }\n" +
            "        .endpoint span {\n" +
            "            color: #666;\n" +
            "            margin-left: 1rem;\n" +
            "        }\n" +
            "        .footer {\n" +
            "            text-align: center;\n" +
            "            margin-top: 2rem;\n" +
            "            padding-top: 1rem;\n" +
            "            border-top: 1px solid #eee;\n" +
            "            color: #666;\n" +
            "        }\n" +
            "        .timestamp {\n" +
            "            font-size: 0.9rem;\n" +
            "            color: #999;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"header\">\n" +
            "            <h1>🚨 Payment Alert Backend API</h1>\n" +
            "            <p>Backend service for Payment Alert app with PhonePe integration</p>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"status\">\n" +
            "            <h3>✅ Service Status</h3>\n" +
            "            <p>API is running and ready to handle requests</p>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"endpoints\">\n" +
            "            <h3>📚 Available Endpoints</h3>\n" +
            "            \n" +
            "            <div class=\"endpoint-group\">\n" +
            "                <h4>🔧 System Endpoints</h4>\n" +
            "                <div class=\"endpoint\">\n" +
            "                    <strong>GET /</strong> <span>Home page with API information</span>\n" +
            "                </div>\n" +
            "                <div class=\"endpoint\">\n" +
            "                    <strong>GET /health</strong> <span>Health check endpoint</span>\n" +
            "                </div>\n" +
            "                <div class=\"endpoint\">\n" +
            "                    <strong>GET /api-docs</strong> <span>API documentation</span>\n" +
            "                </div>\n" +
            "                <div class=\"endpoint\">\n" +
            "                    <strong>GET /welcome</strong> <span>This welcome page</span>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "            \n" +
            "            <div class=\"endpoint-group\">\n" +
            "                <h4>💳 PhonePe API</h4>\n" +
            "                <div class=\"endpoint\">\n" +
            "                    <strong>POST /api/phonepe/payment</strong> <span>Create payment request</span>\n" +
            "                </div>\n" +
            "                <div class=\"endpoint\">\n" +
            "                    <strong>POST /api/phonepe/subscription</strong> <span>Create subscription</span>\n" +
            "                </div>\n" +
            "                <div class=\"endpoint\">\n" +
            "                    <strong>GET /api/phonepe/payment/{id}</strong> <span>Get payment status</span>\n" +
            "                </div>\n" +
            "                <div class=\"endpoint\">\n" +
            "                    <strong>GET /api/phonepe/subscription/{id}</strong> <span>Get subscription details</span>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "            \n" +
            "            <div class=\"endpoint-group\">\n" +
            "                <h4>🔗 Webhooks</h4>\n" +
            "                <div class=\"endpoint\">\n" +
            "                    <strong>POST /webhook/phonepe</strong> <span>PhonePe payment webhook</span>\n" +
            "                </div>\n" +
            "                <div class=\"endpoint\">\n" +
            "                    <strong>POST /webhook/phonepe/renewal</strong> <span>PhonePe renewal webhook</span>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"footer\">\n" +
            "            <p>Payment Alert Backend API v1.0.0</p>\n" +
            "            <p class=\"timestamp\">Last updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";
        
        return ResponseEntity.ok(html);
    }
}
