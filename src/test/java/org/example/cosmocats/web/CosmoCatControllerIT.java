package org.example.cosmocats.web;

import lombok.SneakyThrows;
import org.example.cosmocats.AbstractIT;
import org.example.cosmocats.featuretoggle.FeatureToggleExtension;
import org.example.cosmocats.featuretoggle.FeatureToggles;
import org.example.cosmocats.featuretoggle.annotation.DisabledFeatureToggle;
import org.example.cosmocats.featuretoggle.annotation.EnabledFeatureToggle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("Cosmo Cat Controller IT")
@ExtendWith(FeatureToggleExtension.class)
public class CosmoCatControllerIT extends AbstractIT {

  @Autowired private MockMvc mockMvc;

  @Test
  @SneakyThrows
  @DisabledFeatureToggle(FeatureToggles.COSMO_CATS)
  void testDisabledFeatureToggle() {
    mockMvc
        .perform(get("/api/v1/cosmocats"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title", is("Feature Not Available")))
        .andExpect(jsonPath("$.type", is("urn:problem-type:feature-not-available")));
  }

  @Test
  @SneakyThrows
  @EnabledFeatureToggle(FeatureToggles.COSMO_CATS)
  void testEnabledFeatureToggle() {
    mockMvc.perform(get("/api/v1/cosmocats")).andExpect(status().isOk());
  }
}
