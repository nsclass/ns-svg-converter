@user_creation_time
Feature: User creation time

  Scenario: Verifying user creation time
    When I register a user "/api/v1/users/register" with email: "test@test.com" and password: "password"
    And I expect HTTP status code is 200
    Then the JSON response at "email" should be:
    """
      "test@test.com"
    """
    Then the JSON response at "active" should be:
    """
      true
    """
    Then I expect HTTP JSON field creationTime matches to
    """
    (\d{4})-(\d{2})-(\d{2})
    """
    When I login /api/v1/login with "test@test.com:password"
    And I expect HTTP status code is 200

    When I send GET "/api/v1/users/operations/profile"
    And I expect HTTP status code is 200
