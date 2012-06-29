Feature: Home
  In order to give a friendly entry point to the application
  As a user

Scenario: Home page visit
  Given I am not logged in
  And there is a public activity
  When I go to the "home" page
  Then I should see an activity
