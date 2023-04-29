@swagger_api
Feature: Swagger UI API

  Scenario: Verifying existence of swagger UI API
    When I send GET "/v3/api-docs"
    And I expect HTTP status code is 200
