@role_hierarchy
Feature: Role hierarchy

  Scenario: Verifying role hierarchy
    When I login /api/v1/login with "admin@admin.com:password"
    And I expect HTTP status code is 200
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

    When I send GET "/api/v1/products/admin"
    And I expect HTTP status code is 200
    And the JSON response should be:
    """
      [
        {
          "category": "test_admin",
          "description": "test_admin",
          "name": "test_admin"
        }
      ]
    """

    # access failure for admin resources
    When I login /api/v1/login with "read@user.com:password"
    And I expect HTTP status code is 200
    When I send GET "/api/v1/products/admin"
    And I expect HTTP status code is 403
