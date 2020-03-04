package com.employee;

import com.employee.dto.EmployeeRequest;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        AuthorizationServerConfiguration.class,
        EmployeeCrudApplication.class,
        ResourceServerConfiguration.class
})
@DisplayName("Employee tests")
public class EmployeeControllerTest {

    private static final String BASIC_DATE = "17-09-2012";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mvc;

    private static String token;

    @BeforeAll
    public void setup() throws Exception {
        this.mvc = MockMvcBuilders
                .webAppContextSetup(this.wac)
                .addFilter(springSecurityFilterChain).build();

        obtainAccessToken("user", "secret");
    }

    public void obtainAccessToken(String username, String password) throws Exception {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("username", username);
        params.add("password", password);

        ResultActions result
                = mvc.perform(post("/oauth/token")
                .params(params)
                .header(HttpHeaders.AUTHORIZATION, httpBasic("client","password"))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

        String resultString = result.andReturn().getResponse().getContentAsString();

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        token = jsonParser.parseMap(resultString).get("token_type").toString() + jsonParser.parseMap(resultString).get("access_token").toString();
    }

    @DisplayName("List all employees")
    @Test
    public void getAllEmployees() throws Exception {
        mvc.perform(get("/employees/{page}/{total}", 0, 10)
                .header(HttpHeaders.AUTHORIZATION, token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id").isNotEmpty());
    }

    @DisplayName("Attempt get all employees and has unauthorized status")
    @Test
    public void getAllEmployeesUnAuthorized() throws Exception {
        mvc.perform(get("/employees/{page}/{total}", 0, 10)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Get employee by Id")
    @Test
    public void getEmployeeById() throws Exception {
        mvc.perform(get("/employees/{id}", "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4")
                .header(HttpHeaders.AUTHORIZATION, token)
                .accept(MediaType.APPLICATION_JSON))
                // .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4"));
    }

    @DisplayName("Create a new employee")
    @Test
    public void createEmployee() throws Exception {
        mvc.perform(post("/employees")
                .header(HttpHeaders.AUTHORIZATION, token)
                .content(asJsonString(new EmployeeRequest("Gerardo2", "J", "Luna", BASIC_DATE, BASIC_DATE)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @DisplayName("Update employee")
    @Test
    public void updateEmployee() throws Exception {
        mvc.perform(put("/employees/{id}", "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4")
                .header(HttpHeaders.AUTHORIZATION, token)
                .content(asJsonString(new EmployeeRequest("Gerardo", "J", "Luna", BASIC_DATE, BASIC_DATE)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Gerardo"))
                .andExpect(jsonPath("$.lastName").value("Luna"));
    }

    @DisplayName("Delete employee")
    @Test
    public void deleteEmployee() throws Exception {
        mvc.perform(delete("/employees/{id}", "e26b1d76-a8d0-11e9-a2a3-2a2ae2dbcce4")
                .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk());
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String httpBasic(String clientId, String clientSecret) {
        String auth = clientId + ":" + clientSecret;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes());
        return "Basic " + new String(encodedAuth);
    }

}
