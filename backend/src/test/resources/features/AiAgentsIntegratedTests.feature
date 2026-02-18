Feature: Petstore analyzer integrated tests

  Scenario: Get has available rats should not throw exception
    When I get if there are any rats available
    Then the has available rats response should not be null
