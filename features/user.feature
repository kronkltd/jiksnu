Feature: Users
  In order to track which agents perform actions
  As an administrator
  I want to be able to manage user accounts

Scenario: User admin page, admin
  Given I am logged in as an admin
  When I go to the user admin page
  Then I should be an admin
  Then I should see a list of users

Scenario: Editing profile
  Given I am logged in
  When I go to the edit profile page
  And I type "John Smith" into the "display-name" field
  And I click the "Submit" button
  Then that user's name should be "John Smith"
