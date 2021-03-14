@bean_validation
Feature: Bean validation

  Scenario: This will verify bean validation exception
    When I send POST "/api/v1/users/register" with json without token
    """
    {
      "email": "test@test.com",
      "passwordConfirm": "test description"
    }
    """
    And I expect HTTP status code is 400
    Then I expect HTTP JSON error message contains text
    """
    password - must not be blank
    """

