Feature: Like Admin
  In order to manage likes
  As an administrator
  I want to be able to perform basic operations on likes

Scenario: index, as admin
  Given I am logged in as an admin
  And a like exists
  When I go to the "like admin index" page
  Then I should see this like

Scenario: delete
  Given I am logged in as an admin
  And a like exists
  When I go to the "like admin index" page
  And I click the "delete" button for this like
  Then I should be at the "like admin index" page
  And I should see the flash message "like deleted"
  And this like should be deleted

