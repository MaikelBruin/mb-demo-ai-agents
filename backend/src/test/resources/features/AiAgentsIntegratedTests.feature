Feature: Ai Agents integrated tests

  Scenario: Single endpoint should return something
    When I call the only endpoint of this service
    Then the response should not be null
