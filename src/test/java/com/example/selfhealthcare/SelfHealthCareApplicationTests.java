package com.example.selfhealthcare;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SelfHealthCareApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerAndProfileWorkflowWorks() throws Exception {
        MockHttpSession session = registerUser("tester_profile");

        mockMvc.perform(put("/api/profile")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "测试用户",
                                "gender", "FEMALE",
                                "age", 30,
                                "birthDate", "1996-05-18",
                                "heightCm", 165,
                                "weightKg", 58.2,
                                "careGoals", "保持稳定作息"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("测试用户"))
                .andExpect(jsonPath("$.completionScore").isNumber());

        mockMvc.perform(get("/api/profile/detail").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.fullName").value("测试用户"))
                .andExpect(jsonPath("$.recentRecords").isArray())
                .andExpect(jsonPath("$.recentAlerts").isArray());
    }

    @Test
    void recordsAndAlertsArePaginatedAtTenItems() throws Exception {
        MockHttpSession session = registerUser("tester_pages");
        saveBaseProfile(session);

        for (int i = 0; i < 12; i++) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("recordDate", LocalDate.of(2026, 3, 1).plusDays(i).toString());
            payload.put("weightKg", 80 + i * 0.2);
            payload.put("waistCircumferenceCm", 96);
            payload.put("systolicPressure", 158);
            payload.put("diastolicPressure", 98);
            payload.put("heartRate", 102);
            payload.put("fastingBloodSugar", 8.2);
            payload.put("postprandialBloodSugar", 12.4);
            payload.put("bloodOxygen", 94.5);
            payload.put("sleepHours", 4.8);
            payload.put("exerciseMinutes", 10);
            payload.put("stepsCount", 1200);
            payload.put("stressLevel", 9);
            payload.put("moodScore", 4);
            payload.put("notes", "分页测试记录");

            mockMvc.perform(post("/api/records")
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/records?page=1&size=99").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").value(12))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.items.length()").value(10));

        mockMvc.perform(get("/api/alerts?page=1&size=99").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.items.length()").value(10));
    }

    @Test
    void pdfImportArchivesProfileAndRecord() throws Exception {
        MockHttpSession session = registerUser("tester_import");
        byte[] pdfBytes = createPdf("""
                Name: Import Tester
                Gender: Male
                Age: 32
                Height: 175 cm
                Weight: 71.5 kg
                Date: 2026-03-20
                Blood Pressure: 128/82
                Fasting Glucose: 5.6
                Sleep: 7.2
                Steps: 8200
                Notes: Imported from test pdf
                """);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.pdf",
                "application/pdf",
                pdfBytes);

        mockMvc.perform(multipart("/api/imports/health-document")
                        .file(file)
                        .session(session))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profile.fullName").value("Import Tester"))
                .andExpect(jsonPath("$.archivedRecord.recordDate").value("2026-03-20"))
                .andExpect(jsonPath("$.matchedFields").isArray())
                .andExpect(jsonPath("$.disclaimer").isString());
    }

    private MockHttpSession registerUser(String username) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "displayName", "用户" + username,
                "password", "secret123",
                "confirmPassword", "secret123"));

        return (MockHttpSession) mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getRequest()
                .getSession(false);
    }

    private void saveBaseProfile(MockHttpSession session) throws Exception {
        mockMvc.perform(put("/api/profile")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "分页测试用户",
                                "gender", "MALE",
                                "age", 37,
                                "heightCm", 176,
                                "weightKg", 82.4,
                                "smokingStatus", "NEVER",
                                "alcoholUseStatus", "OCCASIONAL"))))
                .andExpect(status().isOk());
    }

    private byte[] createPdf(String content) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.beginText();
                stream.newLineAtOffset(50, 760);
                for (String line : content.strip().split("\\R")) {
                    stream.showText(line);
                    stream.newLineAtOffset(0, -18);
                }
                stream.endText();
            }

            document.save(output);
            return output.toByteArray();
        }
    }
}
