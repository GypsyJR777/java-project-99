package hexlet.code;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

abstract class ControllerTestSupport {

    protected static final String ADMIN_EMAIL = "hexlet@example.com";
    protected static final String ADMIN_PASSWORD = "qwerty";

    @Autowired
    protected MockMvc mockMvc;

    protected String adminToken() throws Exception {
        return loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    protected RequestPostProcessor auth(String token) {
        return request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        };
    }

    protected ResultActions performAuthorized(MockHttpServletRequestBuilder request, String token) throws Exception {
        return mockMvc.perform(request.with(auth(token)));
    }

    protected ResultActions performJson(
        MockHttpServletRequestBuilder request,
        String token,
        String content
    ) throws Exception {
        return performAuthorized(request.contentType(MediaType.APPLICATION_JSON).content(content), token);
    }

    protected String loginAs(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "%s",
                      "password": "%s"
                    }
                    """.formatted(username, password)))
            .andExpect(status().isOk())
            .andReturn();

        return result.getResponse().getContentAsString();
    }
}
