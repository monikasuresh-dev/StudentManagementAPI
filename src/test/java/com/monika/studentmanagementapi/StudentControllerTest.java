package com.monika.studentmanagementapi;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * End-to-end tests of the REST endpoints using MockMvc (no real server needed).
 *
 * Each test creates its own student with a unique email and never assumes the
 * store is empty, so the tests pass in any order.
 */
@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerTest {

    private static final long MISSING_ID = 999_999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------- helpers ----------

    private String uniqueEmail() {
        return "student-" + UUID.randomUUID() + "@example.com";
    }

    private String studentJson(String name, String email, String department, Object year) {
        return """
                {"name":"%s","email":"%s","department":"%s","year":%s}
                """.formatted(name, email, department, year);
    }

    /** Creates a student through the API and returns the parsed response body. */
    private com.fasterxml.jackson.databind.JsonNode createStudent(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("Monika S.", email, "Information Technology", 2)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    // ---------- 1. create ----------

    @Test
    void createStudent_returns201AndGeneratedId() throws Exception {
        String email = uniqueEmail();

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("Monika S.", email, "Information Technology", 2)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Monika S."))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.department").value("Information Technology"))
                .andExpect(jsonPath("$.year").value(2));
    }

    @Test
    void createStudent_ignoresIdSentByClient_andGeneratesUniqueIds() throws Exception {
        String body = """
                {"id":12345,"name":"A","email":"%s","department":"IT","year":1}
                """.formatted(uniqueEmail());

        long first = objectMapper.readTree(mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString())
                .get("id").asLong();
        long second = objectMapper.readTree(mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString())
                .get("id").asLong();

        org.assertj.core.api.Assertions.assertThat(first).isNotEqualTo(12345L);
        org.assertj.core.api.Assertions.assertThat(second).isNotEqualTo(first);
    }

    // ---------- 2. get all ----------

    @Test
    void getAllStudents_returns200AndContainsCreatedStudent() throws Exception {
        String email = uniqueEmail();
        createStudent(email);

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItem(email)));
    }

    // ---------- 3. get by id ----------

    @Test
    void getStudentById_returns200ForExistingStudent() throws Exception {
        String email = uniqueEmail();
        long id = createStudent(email).get("id").asLong();

        mockMvc.perform(get("/api/students/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value(email));
    }

    // ---------- 4. not found ----------

    @Test
    void getStudentById_returns404ForMissingStudent() throws Exception {
        mockMvc.perform(get("/api/students/{id}", MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Student not found with id " + MISSING_ID))
                .andExpect(jsonPath("$.details", hasSize(0)));
    }

    // ---------- 5. update ----------

    @Test
    void updateStudent_returns200AndChangesFields() throws Exception {
        long id = createStudent(uniqueEmail()).get("id").asLong();
        String newEmail = uniqueEmail();

        mockMvc.perform(put("/api/students/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("Monika Updated", newEmail, "Computer Science", 3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Monika Updated"))
                .andExpect(jsonPath("$.department").value("Computer Science"))
                .andExpect(jsonPath("$.year").value(3));

        // The change must really be stored, not just echoed back.
        mockMvc.perform(get("/api/students/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(newEmail));
    }

    @Test
    void updateStudent_returns404ForMissingStudent() throws Exception {
        mockMvc.perform(put("/api/students/{id}", MISSING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("Nobody", uniqueEmail(), "IT", 1)))
                .andExpect(status().isNotFound());
    }

    // ---------- 6. delete ----------

    @Test
    void deleteStudent_returns204ThenStudentIsGone() throws Exception {
        long id = createStudent(uniqueEmail()).get("id").asLong();

        mockMvc.perform(delete("/api/students/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/students/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteStudent_returns404ForMissingStudent() throws Exception {
        mockMvc.perform(delete("/api/students/{id}", MISSING_ID))
                .andExpect(status().isNotFound());
    }

    // ---------- 7. invalid input ----------

    @Test
    void createStudent_returns400ForBlankNameAndInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("   ", "not-an-email", "IT", 2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details", hasItem("name: name must not be blank")))
                .andExpect(jsonPath("$.details", hasItem("email: email must be a valid email address")));
    }

    @Test
    void createStudent_returns400ForYearOutOfRange() throws Exception {
        for (int badYear : new int[] {0, 5, -1}) {
            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(studentJson("Monika S.", uniqueEmail(), "IT", badYear)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", hasItem("year: year must be between 1 and 4")));
        }
    }

    @Test
    void createStudent_returns400WhenYearIsMissing() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"A\",\"email\":\"" + uniqueEmail() + "\",\"department\":\"IT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasItem("year: year is required")));
    }

    @Test
    void createStudent_returns400ForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ this is not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or is not valid JSON"));
    }

    @Test
    void updateStudent_returns400ForInvalidInput() throws Exception {
        long id = createStudent(uniqueEmail()).get("id").asLong();

        mockMvc.perform(put("/api/students/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("", uniqueEmail(), "IT", 2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasItem("name: name must not be blank")));
    }

    // ---------- 8. invalid or missing ids ----------

    @Test
    void getStudentById_returns400WhenIdIsNotANumber() throws Exception {
        mockMvc.perform(get("/api/students/{id}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getStudentById_returns400WhenIdIsZeroOrNegative() throws Exception {
        mockMvc.perform(get("/api/students/{id}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasItem("id must be a positive number")));

        mockMvc.perform(get("/api/students/{id}", -5))
                .andExpect(status().isBadRequest());
    }
}
