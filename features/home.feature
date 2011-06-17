Feature: Home
  In order to give a friendly entry point to the application
  As a user

Scenario: Home page visit
  Given the user is not logged in
  And an activity exists
  When I visit the home page
  Then I should see an activity
