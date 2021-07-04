@application_status
Feature: application health check and status API
  Spring Boot Actuator API

  Scenario: Application status
    When I login /api/v1/login with "read@user.com:password"
    And I expect HTTP status code is 200

    When I send GET "/actuator/health"
    And I expect HTTP status code is 200

    And the JSON response should be:
    """
      {"status": "UP"}
    """


