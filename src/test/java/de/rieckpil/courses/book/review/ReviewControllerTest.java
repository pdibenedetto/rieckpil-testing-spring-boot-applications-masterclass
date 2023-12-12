package de.rieckpil.courses.book.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.rieckpil.courses.config.WebSecurityConfig;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
// see https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.7-Release-Notes#migrating-from-websecurityconfigureradapter-to-securityfilterchain
@Import(WebSecurityConfig.class)
class ReviewControllerTest {

  @MockBean
  private ReviewService reviewService;

  @Autowired
  private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @Test
  void shouldReturnTwentyReviewsWithoutAnyOrderWhenNoParametersAreSpecified() throws Exception {
  }

  @Test
  void shouldNotReturnReviewStatisticsWhenUserIsUnauthenticated() throws Exception {
    this.mockMvc
      .perform(get("/api/books/reviews/statistics"))
      .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = "duke")
  void shouldReturnReviewStatisticsWhenUserIsAuthenticated() throws Exception {
    this.mockMvc
      .perform(get("/api/books/reviews/statistics"))
      .andExpect(status().isOk());
  }

  @Test
  void shouldCreateNewBookReviewForAuthenticatedUserWithValidPayload() throws Exception {
    // given
    String requestBody = """
      {
        "reviewTitle": "Great Java Book!",
        "reviewContent": "Book is off the chain!",
        "rating": 4
      }
      """;

    // when
    when(reviewService.createBookReview(eq("42"), any(BookReviewRequest.class), eq("duke"), endsWith("spring.io"))).thenReturn(84L);

    this.mockMvc
      .perform(post("/api/books/{isbn}/reviews", 42)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody)
        .with(jwt().jwt(builder -> builder
          .claim("email", "duke@spring.io")
          .claim("preferred_username", "duke"))))
      .andExpect(status().isCreated())
      .andExpect(header().exists("Location"))
      .andExpect(header().string("Location", Matchers.containsString("/books/42/reviews/84")));


  }

  @Test
  void shouldRejectNewBookReviewForAuthenticatedUsersWithInvalidPayload() throws Exception {
  }

  @Test
  void shouldNotAllowDeletingReviewsWhenUserIsAuthenticatedWithoutModeratorRole() throws Exception {
  }

  @Test
  @WithMockUser(roles = "moderator")
  void shouldAllowDeletingReviewsWhenUserIsAuthenticatedAndHasModeratorRole() throws Exception {
  }
}
