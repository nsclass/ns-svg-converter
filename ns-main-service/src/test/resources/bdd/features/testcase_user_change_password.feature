@user_change_password
Feature: Change user password

  @change_password_success
  Scenario: This scenario will verify changing a user password successfully
    When I login /api/v1/login with "read@user.com:password"
    And I expect HTTP status code is 200

    When I send PATCH "/api/v1/users/operations/password" with form
    """
    oldpassword=password&
    newpassword=test
    """
    And I expect HTTP status code is 200
    Then the JSON response at "message" should be:
    """
      "Successfully changed a password"
    """

    When I login /api/v1/login with "read@user.com:test"
    And I expect HTTP status code is 200

  @change_password_failure
  Scenario: This scenario will verify a failure case of changing a user password
    When I login /api/v1/login with "read@user.com:password"
    And I expect HTTP status code is 200

    When I send PATCH "/api/v1/users/operations/password" with form
    """
    oldpassword=password1&
    newpassword=test
    """
    And I expect HTTP status code is 403
    Then I expect HTTP JSON error message contains text
    """
    Invalid user/password
    """
