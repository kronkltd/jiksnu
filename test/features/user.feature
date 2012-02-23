Feature: Users
  In order to track which agents perform actions
  As an administrator
  I want to be able to manage user accounts

# Scenario: User admin page, admin
#   Given I am logged in as an admin
#   When I go to the user admin page
#   Then I should be an admin
#   Then I should see a list of users

# Scenario: Editing profile
#   Given I am logged in
#   When I go to the edit profile page
#   And I type "John Smith" into the "display-name" field
#   # TODO: This is based on localized text, bad!
#   And I click the "submit" button
#   Then that user's name should be "John Smith"

# Scenario: Fetchin a User Meta document
#   Given I am not logged in
#   And a user exists
#   When I request the user-meta page for that user with a client
#   Then the alias field matches that user's uri
#   And the content-type is "application/xrds+xml"
#   And the response is sucsessful

Scenario: User timeline, HTML, unauthenticated
  Given I am not logged in
  And a user exists
  And that user posts an activity
  When I go to the "user timeline" page for that user
  Then I should wait forever
  Then I should see that activity
