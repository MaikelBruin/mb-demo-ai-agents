Feature: AI Agents integrated tests

  Scenario: Test openapi spec with help of agent
    Given I test the openapi spec "petstore.yaml" with help of my agent
    Then there should be test results

#  Scenario: Single endpoint should return something
#    When I call the only endpoint of this service
#    Then the response should not be null
