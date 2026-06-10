Feature: AI Agents integrated tests

  # removed some endpoints from the spec to reduce gemini token usage
  Scenario: Test openapi spec with help of agent
    Given I test the openapi spec "petstore-reduced.yaml" with help of my agent
    Then there should be test results
    And I create an html report

  Scenario: Test openapi spec without payloads
    Given I test the openapi spec "petstore-no-payloads.yaml" with help of my agent
    Then there should be test results
    And I create an html report

  Scenario: Review github PR
    Given I review a github pull request
    Then there should be review comments

  @skip
  Scenario: Test soccer api without payloads
    Given I test the openapi spec "soccer-no-payloads.json" with help of my agent
    Then there should be test results

  @skip
  Scenario: Test secret api with token
    Given I test the secret openapi spec "test-01.yaml" with help of my agent
    Then there should be test results
    And I create an html report
