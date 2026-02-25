Feature: AI Agents integrated tests

  Scenario: Test openapi spec with help of agent
    Given I test the openapi spec "petstore.yaml" with help of my agent
    Then there should be test results

  Scenario: Test openapi spec without payloads
    Given I test the openapi spec "petstore-no-payloads.yaml" with help of my agent
    Then there should be test results

  Scenario: Test soccer api without payloads
    Given I test the openapi spec "soccer-no-payloads.json" with help of my agent
    Then there should be test results

