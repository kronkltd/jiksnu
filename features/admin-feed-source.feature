Feature: Feed Source Admin
  In order to manage feed sources
  As an administrator
  I want to be able to perform basic operations on sources

Scenario: Index Feed Sources, admin
  Given I am logged in as an admin
  And a feed source exists
  When I go to the "feed source admin index" page
  Then I should see a list of feed-sources
