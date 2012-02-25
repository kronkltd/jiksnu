Feature: Authentication
  In order to restrict resources to authorized users
  I should be able to login

# Scenario: Login page
#   Given I am not logged in
#   When I go to the login page
#   Then I should see a form
#   And it should have a "username" field
#   And it should have a "password" field

Scenario: Logging in
  Given I am not logged in
  And I am at the "login" page
  And a user exists with the password "hunter2"
  When I put my username in the "username" field
  And I put my password in the "password" field
  And I click the "login" link
  Then I should be at the "public timeline" page
  And I should wait
  And I should be logged in

# Scenario: Logging out
#   Given I am logged in
#   And I am at the home page
#   When I click the "Log out" button
#   Then I should be at the home page
#   And I should not be logged in
