@token_settings
Feature: Token settings

  Scenario: Verifying token expire
    When I login /api/v1/login with "read@user.com:password"
    And I expect HTTP status code is 200
    Then the JSON response at "expires" should be:
    """
    7200
    """

    When I send GET "/api/v1/settings"
    And I expect HTTP status code is 200
    And I expect that the JSON include:
    """
    {
      "appTokenSettings": {
         "expireInSeconds": 7200
      }
    }
    """

    When I login /api/v1/login with "admin@admin.com:password"
    And I expect HTTP status code is 200
    When I send PUT "/api/v1/settings/tokenSettings" with json
    """
    {
       "expireInSeconds": 2
    }
    """
    And I expect HTTP status code is 200
    And I expect that the JSON include:
    """
    {
       "expireInSeconds": 2
    }
    """

    When I login /api/v1/login with "read@user.com:password"
    And I expect HTTP status code is 200
    Then the JSON response at "expires" should be:
    """
    2
    """

    When I wait for 3 seconds

    When I send GET "/api/v1/products/all"
    And I expect HTTP status code is 403
    Then I expect HTTP JSON error message contains text
    """
    JWT expired
    """

