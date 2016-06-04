Feature: Users
  In order to track which agents perform actions
  As an administrator
  I want to be able to manage user accounts

# Scenario: Editing profile
#   Given there is a user
#   And I am logged in
#   When I go to the "edit profile" page
#   And I type "John Smith" into the "display-name" field
#   And I submit that form
#   Then that user's name should be "John Smith"

# Scenario: Fetching a User Meta document
#   Given there is a user
#   And I am not logged in
#   When I request the user-meta page for that user with a client
#   Then the alias field matches that user's uri
#   And the content-type is "application/xrds+xml"
#   And the response is sucsessful

Scenario: User timeline, HTML, unauthenticated
  Given I am not logged in
  And another user exists
  And that user posts an activity
  When I go to the "user timeline" page for that user
  Then I should see that activity

Scenario: User index, authenticated
  Given I am logged in as a normal user
  And another user exists
  When I go to the "user index" page
  Then I should see 2 users

# Scenario: User index delete button, unauthenticated
#   Given I am not logged in
#   And another user exists
#   When I go to the "user index" page
#   Then I should not see a "delete" button for that user

# Scenario: User index delete button, authenticated
#   Given I am logged in as a normal user
#   And another user exists
#   When I go to the "user index" page
#   Then I should not see a "delete" button for that user

# Scenario: User index delete button, admin
#   Given I am logged in as an admin
#   And another user exists
#   When I go to the "user index" page
#   And I click the "delete" button for that user
#   # Then I should see the flash message "user has been deleted"
#   And that user should be deleted
#   And I should be at the "user index" page

# Scenario: User index subscribe button, unauthenticated
#   Given I am not logged in
#   And another user exists
#   When I go to the "user index" page
#   And I click the "subscribe" button for that user
#   Then I should be at the "ostatus subscription" page

