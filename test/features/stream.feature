Feature: Streams
  In order to not need to poll for new activities
  As a consumer
  I want to have activities pushed to me

  Scenario: Firehose
    Given I am not logged in
    When I connect to the "firehose" stream
    And a new activity gets posted
    Then I should receive a message from the stream
