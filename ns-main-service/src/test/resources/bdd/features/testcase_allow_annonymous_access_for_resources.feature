@allow_anonymous_access
Feature: Allow anonymous access

  Scenario: This scenario will verify to access the URL without login
    When I send GET "/api/v1/products"
    And I expect HTTP status code is 200
    And the JSON response should be:
    """
      [
        {
          "category": "test1",
          "description": "test1",
          "name": "test1"
        }
      ]
    """
