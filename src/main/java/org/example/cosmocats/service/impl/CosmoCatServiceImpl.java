package org.example.cosmocats.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cosmocats.service.CosmoCatService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CosmoCatServiceImpl implements CosmoCatService {

  @Override
  public List<String> getCosmoCats() {
    return List.of("Star Cat", "Galaxy Cat", "Comet Cat");
  }
}
