Feature: Home
  In order to give a friendly entry point to the application
  As a user

Scenario: Home page visit
  Given a started server
  When I visit the home page
  Then I should see a list of activities
