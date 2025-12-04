package org.example.cosmocats.web;

import java.util.List;
import org.example.cosmocats.featuretoggle.FeatureToggles;
import org.example.cosmocats.featuretoggle.annotation.FeatureToggle;
import org.example.cosmocats.service.CosmoCatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cosmocats")
public class CosmoCatController {

  private final CosmoCatService cosmoCatService;

  public CosmoCatController(CosmoCatService cosmoCatService) {
    this.cosmoCatService = cosmoCatService;
  }

  @GetMapping
  @FeatureToggle(FeatureToggles.COSMO_CATS)
  public ResponseEntity<List<String>> getCats() {
    List<String> cats = cosmoCatService.getCosmoCats();
    return ResponseEntity.ok(cats);
  }
}
