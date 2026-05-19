package ch.ge.police.ui.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.ge.police.core.application.service.EmailChallengeService;
import ch.ge.police.core.domain.model.notification.EmailLanguage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class EmailChallengeControllerTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private EmailChallengeService service;
  private EmailChallengeController controller;

  @BeforeEach
  void setUp() {
    service = mock(EmailChallengeService.class);
    controller = new EmailChallengeController(service);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void request_returns204_andDelegatesToService() throws Exception {
    var body = new EmailChallengeController.RequestCodeRequest("a@b.ch", "draft-123");

    mockMvc.perform(post("/api/email-challenges/request").locale(java.util.Locale.ENGLISH).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body))).andExpect(status().isNoContent());

    verify(service).request("a@b.ch", "draft-123", EmailLanguage.EN);
  }
}
