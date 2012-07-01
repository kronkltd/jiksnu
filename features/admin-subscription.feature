Feature: Subscription Admin
  In order to manage subscriptions
  As an administrator
  I want to be able to perform basic operations on subscriptions

Scenario: Index Feed Subscriptions, admin
  Given I am logged in as an admin
  And a subscription exists
  When I go to the "subscriptions admin index" page
  Then I should see a list of subscriptions

Scenario: Deleting subscriptions from the admin index
  Given I am logged in as an admin
  And a subscription exists
  When I go to the "subscriptions admin index" page
  And I click the "delete" button for that subscription
  Then that subscription should be deleted
  And I should see the flash message "subscription deleted"
  And I should be at the "subscriptions admin index" page
