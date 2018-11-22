@user_api
Feature: testing user API

  @login_success
  Scenario: This scenario will verify user login and access resource
    When I login /api/v1/login with "read@user.com:password"
    And I expect HTTP status code is 200
    Then the JSON response at "expires" should be:
    """
    7200
    """
    When I send GET "/api/v1/products/all"
    And I expect HTTP status code is 200
    And the JSON response should be:
    """
      [
        {
          "category": "test",
          "description": "test",
          "name": "test"
        }
      ]
    """

    When I send GET "/api/v1/users/operations/profile"
    And I expect HTTP status code is 200
    Then the JSON response at "email" should be:
    """
      "read@user.com"
    """

  @access_resources_without_login
  Scenario: This scenario will verify access failure without login
    When I send GET "/api/v1/products/all"
    And I expect HTTP status code is 403
    Then I expect HTTP JSON error message contains text
    """
    Access denied: invalid bearer token access
    """

  @access_denied_for_other_user
  Scenario: This scenario will verify to not allow to access other user information
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

    When I login /api/v1/login with "test@test.com:password"
    And I expect HTTP status code is 200

    When I send GET "/api/v1/users/operations/profile"
    And I expect HTTP status code is 200
    Then the JSON response at "email" should be:
    """
      "test@test.com"
    """
    When I send GET "/api/v1/users/read@user.com"
    And I expect HTTP status code is 403


  @error_for_registering_same_user
  Scenario: This scenario will verify to not allow to register same user.
    When I register a user "/api/v1/users/register" with email: "test@test.com" and password: "password"
    And I expect HTTP status code is 200

    When I register a user "/api/v1/users/register" with email: "test@test.com" and password: "password"
    And I expect HTTP status code is 405
    Then I expect HTTP JSON error message contains text
    """
    test@test.com already exists
    """

  @admin_password_check
  Scenario: This scenario will verify access failure without login
    When I login /api/v1/login with "admin@admin.com:password"
    And I expect HTTP status code is 200

    When I send GET "/api/v1/users/read@user.com"
    And I expect HTTP status code is 200
    Then the JSON response at "email" should be:
    """
      "read@user.com"
    """



