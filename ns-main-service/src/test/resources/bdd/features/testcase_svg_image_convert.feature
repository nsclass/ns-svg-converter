@svg_image_converter
Feature: SVG image converter

  @create_svg_successfully
  Scenario: Verifying SVG image conversion
    When I login /api/v1/login with "admin@admin.com:password"
    And I expect HTTP status code is 200
    When I send PUT "/api/v1/settings/svgSettings" with json
    """
    {
        "useLimit": false,
        "numberOfColorLimitation": 256,
        "imageSizeLimitation": 5242880
    }
    """
    And I expect HTTP status code is 200
    And I expect that the JSON include:
    """
      {
        "useLimit": false,
        "numberOfColorLimitation": 256,
        "imageSizeLimitation": 5242880
      }
    """

    Given I keep the value at "IMAGE_DATA" from a file "features/samples/image_sample_base64.txt"

    When I send PUT "/api/v1/svg/conversion" with json
    """
    {
      "imageFilename" : "image_sample_base64.jpg",
      "imageDataBase64": %{IMAGE_DATA}
    }
    """
    And I expect HTTP status code is 200


  @create_svg_failure_case
  Scenario: Verifying SVG image conversion

    When I login /api/v1/login with "admin@admin.com:password"
    And I expect HTTP status code is 200
    When I send PUT "/api/v1/settings/svgSettings" with json
    """
    {
        "useLimit": true,
        "numberOfColorLimitation": 256,
        "imageSizeLimitation": 1024
    }
    """
    And I expect HTTP status code is 200
    And I expect that the JSON include:
    """
      {
        "useLimit": true,
        "numberOfColorLimitation": 256,
        "imageSizeLimitation": 1024
      }
    """

    Given I keep the value at "IMAGE_DATA" from a file "features/samples/image_sample_base64.txt"

    When I send PUT "/api/v1/svg/conversion" with json
    """
    {
      "imageFilename" : "image_sample_base64.jpg",
      "imageDataBase64": %{IMAGE_DATA}
    }
    """
    And I expect HTTP status code is 400
    Then I expect HTTP JSON error message contains text
    """
    Not supported image size
    """


