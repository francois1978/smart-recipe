Feature: recipe test

  Scenario: check if service is up
    Given client wants to health check service
         | id |
         | 2 |
    When the client calls the end point to check
    Then the client receives response Alive
